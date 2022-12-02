#ifndef k005289_h
#define k005289_h

struct k005289_interface
{
	int master_clock;	/* clock speed */
	int volume;			/* playback volume */
	int region;			/* memory region */
};

int K005289_sh_start(const struct MachineSound *msound);
void K005289_sh_stop(void);

void k005289_control_A_w(int offset, int data);
void k005289_control_B_w(int offset, int data);
void k005289_pitch_A_w (int offset, int data);
void k005289_pitch_B_w (int offset, int data);
void k005289_keylatch_A_w (int offset, int data);
void k005289_keylatch_B_w (int offset, int data);

#endif
