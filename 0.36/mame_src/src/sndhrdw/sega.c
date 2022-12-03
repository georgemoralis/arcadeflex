#include "driver.h"

/* History:



 * 4/25/99 Tac-Scan now makes Credit Noises with $2c                    (Jim Hernandez)
 * 4/9/99 Zektor Discrete Sound Support mixed with voice samples.       (Jim Hernandez)
          Zektor uses some Eliminator sounds.

 * 2/5/99 Extra Life sound constant found $1C after fixing main driver. (Jim Hernandez)
 * 1/29/99 Supports Tac Scan new 44.1Khz sample set.                    (Jim Hernandez)

 * -Stuff to do -
 * Find hex bit for warp.wav sound calls.
 *
 * 2/05/98 now using the new sample_*() functions. BW
 *
 */

/*
	Tac/Scan sound constants

	There are some sounds that are unknown:
	$09 Tunnel Warp Sound?
	$0a
	$0b Formation Change
        $0c
        $0e
        $0f
        $1c 1up (Extra Life)
        $2c Credit
        $30 - $3f  Hex numbers for ship position flight sounds
	$41

	Some sound samples are missing:
   	- I use the one bullet and one explosion sound for all 3 for example.

Star Trk Sounds (USB Loaded from 5400 in main EPROMs)

8     PHASER
a     PHOTON
e     TARGETING
10    DENY
12    SHEILD HIT
14    ENTERPRISE HIT
16    ENT EXPLOSION
1a    KLINGON EXPLOSION
1c    DOCK
1e    STARBASE HIT
11    STARBASE RED
22    STARBASE EXPLOSION
24    SMALL BONUS
25    LARGE BONUS
26    STARBASE INTRO
27    KLINGON INTRO
28    ENTERPRISE INTRO
29    PLAYER CHANGE
2e    KLINGON FIRE
4,5   IMPULSE
6,7   WARP
c,d   RED ALERT
18,2f WARP SUCK
19,2f SAUCER EXIT
2c,21 NOMAD MOTION
2d,21 NOMAD STOPPED
2b    COIN DROP MUSIC
2a    HIGH SCORE MUSIC


Eliminator Sound Board (800-3174)
---------------------------------

inputs
0x3c-0x3f

d0 speech ready

outputs ( 0 = ON)

0x3e (076)

d7      torpedo 2
d6      torpedo 1
d5      bounce
d4      explosion 3
d3      explosion 2
d2      explosion 1
d1      fireball
d0      -

0x3f (077)

d7      background msb
d6      background lsb
d5      enemy ship
d4      skitter
d3      thrust msb
d2      thrust lsb
d1      thrust hi
d0      thrust lo

Space Fury Sound Board (800-0241)
---------------------------------

0x3e (076) (0 = ON)

d7      partial warship, low frequency oscillation
d6      star spin
d5      -
d4      -
d3      -
d2      thrust, low frequency noise
d1      fire, metalic buzz
d0      craft scale, rising tone

0x3f (077)

d7      -
d6      -
d5      docking bang
d4      large explosion
d3      small explosion, low frequency noise
d2      fireball
d1      shot
d0      crafts joining

*/

/* Some Tac/Scan sound constants */
#define	shipStop 0x10
#define shipLaser 0x18
#define	shipExplosion 0x20
#define	shipDocking 0x28
#define	shipRoar 0x40
#define	tunnelHigh 0x48
#define	stingerThrust 0x50
#define	stingerLaser 0x51
#define	stingerStop 0x52
#define	stingerExplosion 0x54
#define	enemyBullet0 0x61
#define	enemyBullet1 0x62
#define	enemyBullet2 0x63
#define	enemyExplosion0 0x6c
#define	enemyExplosion1 0x6d
#define	enemyExplosion2 0x6e
#define tunnelw   0x09
#define flight1   0x36
#define flight2   0x3b
#define flight3   0x3d
#define flight4   0x3e
#define flight5   0x3f
#define warp      0x37
#define formation 0x0b
#define nothing1  0x1a
#define nothing2  0x1b
#define extralife 0x1c
#define credit    0x2c


#define	kVoiceShipRoar 5
#define	kVoiceShip 1
#define	kVoiceTunnel 2
#define	kVoiceStinger 3
#define	kVoiceEnemy 4
#define kVoiceExtra 8
#define kVoiceForm 7
#define kVoiceWarp 6
#define kVoiceExtralife 9

static int roarPlaying;	/* Is the ship roar noise playing? */

/*
 * The speech samples are queued in the order they are received
 * in sega_sh_speech_w (). sega_sh_update () takes care of playing
 * and updating the sounds in the order they were queued.
 */

#define MAX_SPEECH      16      /* Number of speech samples which can be queued */
#define NOT_PLAYING	-1	/* Queue position empty */

static int queue[MAX_SPEECH];
static int queuePtr = 0;

int sega_sh_start (const struct MachineSound *msound)
{
	int i;

	for (i = 0; i < MAX_SPEECH; i ++)
		queue[i] = NOT_PLAYING;

	return 0;
}

int sega_sh_r (int offset)
{
	/* 0x80 = universal sound board ready */
	/* 0x01 = speech ready */

	if (sample_playing(0))
		return 0x81;
	else
		return 0x80;
}

void sega_sh_speech_w (int offset,int data)
{
	int sound;

	sound = data & 0x7f;
	/* The sound numbers start at 1 but the sample name array starts at 0 */
	sound --;

	if (sound < 0)	/* Can this happen? */
		return;

	if (!(data & 0x80))
   	{
		/* This typically comes immediately after a speech command. Purpose? */
		return;
	}
   	else if (Machine->samples != 0 && Machine->samples->sample[sound] != 0)
   	{
		int newPtr;

		/* Queue the new sound */
		newPtr = queuePtr;
		while (queue[newPtr] != NOT_PLAYING)
	   	{
			newPtr ++;
			if (newPtr >= MAX_SPEECH)
				newPtr = 0;
			if (newPtr == queuePtr)
		   	{
				/* The queue has overflowed. Oops. */
				if (errorlog) fprintf (errorlog, "*** Queue overflow! queuePtr: %02d\n", queuePtr);
				return;
			}
		}
		queue[newPtr] = sound;
	}
}

void sega_sh_update (void)
{
	int sound;

	/* if a sound is playing, return */
	if (sample_playing(0)) return;

	/* Check the queue position. If a sound is scheduled, play it */
	if (queue[queuePtr] != NOT_PLAYING)
   	{
		sound = queue[queuePtr];
		sample_start(0, sound, 0);

		/* Update the queue pointer to the next one */
		queue[queuePtr] = NOT_PLAYING;
		++ queuePtr;
		if (queuePtr >= MAX_SPEECH)
			queuePtr = 0;
		}
	}

int tacscan_sh_start (const struct MachineSound *msound)
{
	roarPlaying = 0;
	return 0;
}

void tacscan_sh_w (int offset,int data)
{
	int sound;   /* index into the sample name array in drivers/sega.c */
	int voice=0; /* which voice to play the sound on */
	int loop;    /* is this sound continuous? */

	loop = 0;
	switch (data)
   	{
		case shipRoar:
			/* Play the increasing roar noise */
			voice = kVoiceShipRoar;
			sound = 0;
			roarPlaying = 1;
			break;
		case shipStop:
			/* Play the decreasing roar noise */
			voice = kVoiceShipRoar;
			sound = 2;
			roarPlaying = 0;
			break;
		case shipLaser:
			voice = kVoiceShip;
			sound = 3;
			break;
		case shipExplosion:
			voice = kVoiceShip;
			sound = 4;
			break;
		case shipDocking:
			voice = kVoiceShip;
			sound = 5;
			break;
		case tunnelHigh:
			voice = kVoiceTunnel;
			sound = 6;
			break;
		case stingerThrust:
			voice = kVoiceStinger;
			sound = 7;
                        loop = 0; //leave off sound gets stuck on
			break;
		case stingerLaser:
			voice = kVoiceStinger;
			sound = 8;
                        loop = 0;
			break;
		case stingerExplosion:
			voice = kVoiceStinger;
			sound = 9;
			break;
		case stingerStop:
			voice = kVoiceStinger;
			sound = -1;
			break;
		case enemyBullet0:
		case enemyBullet1:
		case enemyBullet2:
			voice = kVoiceEnemy;
			sound = 10;
			break;
		case enemyExplosion0:
		case enemyExplosion1:
		case enemyExplosion2:
			voice = kVoiceTunnel;
			sound = 11;
			break;
                case tunnelw: voice = kVoiceShip;
                              sound = 12;
                              break;
                case flight1: voice = kVoiceExtra;
                              sound = 13;
                              break;
                case flight2: voice = kVoiceExtra;
                              sound = 14;
                              break;
                case flight3: voice = kVoiceExtra;
                              sound = 15;
                              break;
                case flight4: voice = kVoiceExtra;
                              sound = 16;
                              break;
                case flight5: voice = kVoiceExtra;
                              sound = 17;
                              break;
		case formation:
                              voice = kVoiceForm;
              	              sound = 18;
			      break;
	       	case warp:    voice = kVoiceExtra;
                              sound = 19;
                              break;
                case extralife: voice = kVoiceExtralife;
                                sound = 20;
                                break;
                case credit:    voice = kVoiceExtra;
                                sound = 21;
                                break;

                default:

			/* don't play anything */
			sound = -1;
			break;
                      }
	if (sound != -1)
   	{
		sample_stop (voice);
		/* If the game is over, turn off the stinger noise */
		if (data == shipStop)
			sample_stop (kVoiceStinger);
		sample_start (voice, sound, loop);
	}
}

void tacscan_sh_update (void)
{
	/* If the ship roar has started playing but the sample stopped */
	/* play the intermediate roar noise */

	if ((roarPlaying) && (!sample_playing(kVoiceShipRoar)))
		sample_start (kVoiceShipRoar, 1, 1);
}


void elim1_sh_w (int offset,int data)
{
	data ^= 0xff;

	/* Play fireball sample */
	if (data & 0x02)
		sample_start (0, 0, 0);

	/* Play explosion samples */
	if (data & 0x04)
		sample_start (1, 10, 0);
	if (data & 0x08)
		sample_start (1, 9, 0);
	if (data & 0x10)
		sample_start (1, 8, 0);

	/* Play bounce sample */
	if (data & 0x20)
   	{
		if (sample_playing(2))
			sample_stop (2);
		sample_start (2, 1, 0);
	}

	/* Play lazer sample */
	if (data & 0xc0)
   	{
		if (sample_playing(3))
			sample_stop (3);
		sample_start (3, 5, 0);
	}
}

void elim2_sh_w (int offset,int data)
{
	data ^= 0xff;

	/* Play thrust sample */
	if (data & 0x0f)
		sample_start (4, 6, 0);
	else
		sample_stop (4);

	/* Play skitter sample */
	if (data & 0x10)
		sample_start (5, 2, 0);

	/* Play eliminator sample */
	if (data & 0x20)
		sample_start (6, 3, 0);

	/* Play electron samples */
	if (data & 0x40)
		sample_start (7, 7, 0);
	if (data & 0x80)
		sample_start (7, 4, 0);
}


void zektor1_sh_w (int offset,int data)
{
	data ^= 0xff;

	/* Play fireball sample */
	if (data & 0x02)
                sample_start (0, 19, 0);

	/* Play explosion samples */
	if (data & 0x04)
                sample_start (1, 29, 0);
 	if (data & 0x08)
                  sample_start (1, 28, 0);
 	if (data & 0x10)
                  sample_start (1, 27, 0);

	/* Play bounce sample */
	if (data & 0x20)
   	{
                if (sample_playing(2))
                        sample_stop (2);
                sample_start (2, 20, 0);
	}

	/* Play lazer sample */
	if (data & 0xc0)
   	{
		if (sample_playing(3))
			sample_stop (3);
                sample_start (3, 24, 0);
	}
}

void zektor2_sh_w (int offset,int data)
{
	data ^= 0xff;

	/* Play thrust sample */
	if (data & 0x0f)
            sample_start (4, 25, 0);
	else
		sample_stop (4);

	/* Play skitter sample */
	if (data & 0x10)
                sample_start (5, 21, 0);

	/* Play eliminator sample */
	if (data & 0x20)
                sample_start (6, 22, 0);

	/* Play electron samples */
	if (data & 0x40)
                sample_start (7, 40, 0);
	if (data & 0x80)
                sample_start (7, 41, 0);
}



void startrek_sh_w (int offset,int data)
{
	switch (data)
   	{
		case 0x08: /* phaser - trek1.wav */
			sample_start (1, 0x17, 0);
			break;
		case 0x0a: /* photon - trek2.wav */
			sample_start (1, 0x18, 0);
			break;
		case 0x0e: /* targeting - trek3.wav */
			sample_start (1, 0x19, 0);
			break;
		case 0x10: /* dent - trek4.wav */
			sample_start (2, 0x1a, 0);
			break;
		case 0x12: /* shield hit - trek5.wav */
			sample_start (2, 0x1b, 0);
			break;
		case 0x14: /* enterprise hit - trek6.wav */
			sample_start (2, 0x1c, 0);
			break;
		case 0x16: /* enterprise explosion - trek7.wav */
			sample_start (2, 0x1d, 0);
			break;
		case 0x1a: /* klingon explosion - trek8.wav */
			sample_start (2, 0x1e, 0);
			break;
		case 0x1c: /* dock - trek9.wav */
			sample_start (1, 0x1f, 0);
			break;
		case 0x1e: /* starbase hit - trek10.wav */
			sample_start (1, 0x20, 0);
			break;
		case 0x11: /* starbase red - trek11.wav */
			sample_start (1, 0x21, 0);
			break;
		case 0x22: /* starbase explosion - trek12.wav */
			sample_start (2, 0x22, 0);
			break;
		case 0x24: /* small bonus - trek13.wav */
			sample_start (3, 0x23, 0);
			break;
		case 0x25: /* large bonus - trek14.wav */
			sample_start (3, 0x24, 0);
			break;
		case 0x26: /* starbase intro - trek15.wav */
			sample_start (1, 0x25, 0);
			break;
		case 0x27: /* klingon intro - trek16.wav */
			sample_start (1, 0x26, 0);
			break;
		case 0x28: /* enterprise intro - trek17.wav */
			sample_start (1, 0x27, 0);
			break;
		case 0x29: /* player change - trek18.wav */
			sample_start (1, 0x28, 0);
			break;
		case 0x2e: /* klingon fire - trek19.wav */
			sample_start (2, 0x29, 0);
			break;
		case 0x04: /* impulse start - trek20.wav */
			sample_start (3, 0x2a, 0);
			break;
		case 0x06: /* warp start - trek21.wav */
			sample_start (3, 0x2b, 0);
			break;
		case 0x0c: /* red alert start - trek22.wav */
			sample_start (4, 0x2c, 0);
			break;
		case 0x18: /* warp suck - trek23.wav */
			sample_start (4, 0x2d, 0);
			break;
		case 0x19: /* saucer exit - trek24.wav */
			sample_start (4, 0x2e, 0);
			break;
		case 0x2c: /* nomad motion - trek25.wav */
			sample_start (5, 0x2f, 0);
			break;
		case 0x2d: /* nomad stopped - trek26.wav */
			sample_start (5, 0x30, 0);
			break;
		case 0x2b: /* coin drop music - trek27.wav */
			sample_start (1, 0x31, 0);
			break;
		case 0x2a: /* high score music - trek28.wav */
			sample_start (1, 0x32, 0);
			break;
	}
}

void spacfury1_sh_w (int offset,int data)
{
	data ^= 0xff;

	/* craft growing */
	if (data & 0x01)
		sample_start (1, 0x15, 0);

	/* craft moving */
	if (data & 0x02)
   	{
		if (!sample_playing(2))
			sample_start (2, 0x16, 1);
	}
	else
		sample_stop (2);

	/* Thrust */
	if (data & 0x04)
   	{
		if (!sample_playing(3))
			sample_start (3, 0x19, 1);
	}
	else
		sample_stop (3);

	/* star spin */
	if (data & 0x40)
		sample_start (4, 0x1d, 0);

	/* partial warship? */
	if (data & 0x80)
		sample_start (4, 0x1e, 0);

}

void spacfury2_sh_w (int offset,int data)
{
	if (Machine->samples == 0) return;

	data ^= 0xff;

	/* craft joining */
	if (data & 0x01)
		sample_start (5, 0x17, 0);

	/* ship firing */
	if (data & 0x02)
   	{
		if (sample_playing(6))
			sample_stop(6);
		sample_start(6, 0x18, 0);

        }

	/* fireball */
	if (data & 0x04)
		sample_start (7, 0x1b, 0);

	/* small explosion */
	if (data & 0x08)
		sample_start (7, 0x1b, 0);
	/* large explosion */
	if (data & 0x10)
		sample_start (7, 0x1a, 0);

	/* docking bang */
	if (data & 0x20)
		sample_start (8, 0x1c, 0);

}

