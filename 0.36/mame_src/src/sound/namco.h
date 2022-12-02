#ifndef namco_h
#define namco_h

struct namco_interface
{
	int samplerate;	/* sample rate */
	int voices;		/* number of voices */
	int volume;		/* playback volume */
	int region;		/* memory region; -1 to use RAM (pointed to by namco_wavedata) */
	int stereo;		/* set to 1 to indicate stereo (e.g., System 1) */
};

int namco_sh_start(const struct MachineSound *msound);
void namco_sh_stop(void);

void pengo_sound_enable_w(int offset,int data);
void pengo_sound_w(int offset,int data);

void polepos_sound_enable_w(int offset,int data);
void polepos_sound_w(int offset,int data);

void mappy_sound_enable_w(int offset,int data);
void mappy_sound_w(int offset,int data);

void namcos1_sound_w(int offset,int data);
void namcos1_wavedata_w(int offset,int data);
int namcos1_sound_r(int offset);
int namcos1_wavedata_r(int offset);

extern unsigned char *namco_soundregs;
extern unsigned char *namco_wavedata;

#define mappy_soundregs namco_soundregs
#define pengo_soundregs namco_soundregs
#define polepos_soundregs namco_soundregs

#endif

