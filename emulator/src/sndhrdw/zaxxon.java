package sndhrdw;

import mame.driverH.*;

public class zaxxon {
    /*TODO*///#define TOTAL_SOUNDS 22
    /*TODO*///int soundplaying[TOTAL_SOUNDS];
    /*TODO*///
    /*TODO*///struct sa
    /*TODO*///{
    /*TODO*///	int channel;
    /*TODO*///	int num;
    /*TODO*///	int looped;
    /*TODO*///	int stoppable;
    /*TODO*///	int restartable;
    /*TODO*///};
    /*TODO*///
    /*TODO*///struct sa sa[TOTAL_SOUNDS] =
    /*TODO*///{
    /*TODO*///	{  0,  0, 1, 1, 1 },	/* Line  4 - Homing Missile  (channel 1) */
    /*TODO*///	{  1,  1, 0, 1, 1 },	/* Line  5 - Base Missile */
    /*TODO*///	{  2,  2, 1, 1, 1 },	/* Line  6 - Laser (force field) (channel 1) */
    /*TODO*///	{  3,  3, 1, 1, 1 },	/* Line  7 - Battleship (end of level boss) (channel 1) */
    /*TODO*///	{ -1 },					/* Line  8 - unused */
    /*TODO*///	{ -1 },					/* Line  9 - unused */
    /*TODO*///	{ -1 },					/* Line 10 - unused */
    /*TODO*///	{ -1 },					/* Line 11 - unused */
    /*TODO*///	{  4,  4, 0, 0, 1 },	/* Line 12 - S-Exp (enemy explosion) */
    /*TODO*///	{  5,  5, 0, 0, 0 },	/* Line 13 - M-Exp (ship explosion) (channel 1) */
    /*TODO*///	{ -1 },					/* Line 14 - unused */
    /*TODO*///	{  6,  6, 0, 0, 1 },	/* Line 15 - Cannon (ship fire) */
    /*TODO*///	{  7,  7, 0, 0, 1 },	/* Line 16 - Shot (enemy fire) */
    /*TODO*///	{ -1 },					/* Line 17 - unused */
    /*TODO*///	{  8,  8, 0, 0, 1 },	/* Line 18 - Alarm 2 (target lock) */
    /*TODO*///	{  9,  9, 0, 0, 0 },	/* Line 19 - Alarm 3 (low fuel) (channel 1) */
    /*TODO*///	{ -1 },					/* Line 20 - unused */
    /*TODO*///	{ -1 },					/* Line 21 - unused */
    /*TODO*///	{ -1 },					/* Line 22 - unused */
    /*TODO*///	{ -1 },					/* Line 23 - unused */
    /*TODO*///	{ 10, 10, 1, 1, 1 },	/* background */
    /*TODO*///	{ 11, 11, 1, 1, 1 },	/* background */
    /*TODO*///};
    /*TODO*///
    /*TODO*///
    /*TODO*///
    public static WriteHandlerPtr zaxxon_sound_w = new WriteHandlerPtr() { public void handler(int offset, int data)
    {
    /*TODO*///	int line;
    /*TODO*///	int noise;
    /*TODO*///
    /*TODO*///
    /*TODO*///	if (offset == 0)
    /*TODO*///	{
    /*TODO*///		/* handle background rumble */
    /*TODO*///		switch (data & 0x0c)
    /*TODO*///		{
    /*TODO*///			case 0x04:
    /*TODO*///				soundplaying[20] = 0;
    /*TODO*///				sample_stop(sa[20].channel);
    /*TODO*///				if (soundplaying[21] == 0)
    /*TODO*///				{
    /*TODO*///					soundplaying[21] = 1;
    /*TODO*///					sample_start(sa[21].channel,sa[21].num,sa[21].looped);
    /*TODO*///				}
    /*TODO*///				sample_set_volume(sa[21].channel,128 + 40 * (data & 0x03));
    /*TODO*///				break;
    /*TODO*///			case 0x00:
    /*TODO*///			case 0x08:
    /*TODO*///				if (soundplaying[20] == 0)
    /*TODO*///				{
    /*TODO*///					soundplaying[20] = 1;
    /*TODO*///					sample_start(sa[20].channel,sa[20].num,sa[20].looped);
    /*TODO*///				}
    /*TODO*///				sample_set_volume(sa[20].channel,128 + 40 * (data & 0x03));
    /*TODO*///				soundplaying[21] = 0;
    /*TODO*///				sample_stop(sa[21].channel);
    /*TODO*///				break;
    /*TODO*///			case 0x0c:
    /*TODO*///				soundplaying[20] = 0;
    /*TODO*///				sample_stop(sa[20].channel);
    /*TODO*///				soundplaying[21] = 0;
    /*TODO*///				sample_stop(sa[21].channel);
    /*TODO*///				break;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	for (line = 0;line < 8;line++)
    /*TODO*///	{
    /*TODO*///		noise = 8 * offset + line - 4;
    /*TODO*///
    /*TODO*///		/* the first four sound lines are handled separately */
    /*TODO*///		if (noise >= 0)
    /*TODO*///		{
    /*TODO*///			if ((data & (1 << line)) == 0)
    /*TODO*///			{
    /*TODO*///				/* trigger sound */
    /*TODO*///				if (soundplaying[noise] == 0)
    /*TODO*///				{
    /*TODO*///					soundplaying[noise] = 1;
    /*TODO*///					if (sa[noise].channel != -1)
    /*TODO*///					{
    /*TODO*///						if (sa[noise].restartable || !sample_playing(sa[noise].channel))
    /*TODO*///							sample_start(sa[noise].channel,sa[noise].num,sa[noise].looped);
    /*TODO*///					}
    /*TODO*///				}
    /*TODO*///			}
    /*TODO*///			else
    /*TODO*///			{
    /*TODO*///				if (soundplaying[noise])
    /*TODO*///				{
    /*TODO*///					soundplaying[noise] = 0;
    /*TODO*///					if (sa[noise].channel != -1 && sa[noise].stoppable)
    /*TODO*///						sample_stop(sa[noise].channel);
    /*TODO*///				}
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///	}
    }};  
}
