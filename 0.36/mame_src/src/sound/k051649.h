#ifndef k051649_h
#define k051649_h

struct k051649_interface
{
	int master_clock;	/* master clock */
	int volume;			/* playback volume */
};

int K051649_sh_start(const struct MachineSound *msound);
void K051649_sh_stop(void);

void K051649_waveform_w(int offset, int data);
void K051649_volume_w(int offset, int data);
void K051649_frequency_w(int offset, int data);
void K051649_keyonoff_w(int offset, int data);

#endif
