#ifndef saa1099_h
#define saa1099_h

/**********************************************
	Philips SAA1099 Sound driver
**********************************************/

#define MAX_SAA1099 1

/* interface */
struct SAA1099_interface
{
	int numchips;						/* number of chips */
	int volume[MAX_SAA1099][2];			/* playback volume */	
};

int saa1099_sh_start(const struct MachineSound *msound);
void saa1099_sh_stop(void);

WRITE_HANDLER( saa1099_control_port_0_w );
WRITE_HANDLER( saa1099_write_port_0_w );


#endif
