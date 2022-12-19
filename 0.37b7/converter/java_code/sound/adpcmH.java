#ifndef ADPCM_H
#define ADPCM_H

#define MAX_ADPCM 8


/* a generic ADPCM interface, for unknown chips */

struct ADPCMinterface
{
	int num;			       /* total number of ADPCM decoders in the machine */
	int frequency;             /* playback frequency */
	int region;                /* memory region where the samples come from */
	int mixing_level[MAX_ADPCM];     /* master volume */
};


void ADPCM_play(int num, int offset, int length);
void ADPCM_setvol(int num, int vol);
void ADPCM_stop(int num);
int ADPCM_playing(int num);


/* an interface for the OKIM6295 and similar chips */

#define MAX_OKIM6295 			2
#define MAX_OKIM6295_VOICES		4
#define ALL_VOICES				-1

struct OKIM6295interface
{
	int num;                  		/* total number of chips */
	int frequency[MAX_OKIM6295];	/* playback frequency */
	int region[MAX_OKIM6295];		/* memory region where the sample ROM lives */
	int mixing_level[MAX_OKIM6295];	/* master volume */
};

void OKIM6295_set_bank_base(int which, int voice, int base);	/* set voice to ALL_VOICES to set all banks at once */
void OKIM6295_set_frequency(int which, int voice, int frequency);	/* set voice to ALL_VOICES to set all banks at once */



#endif
