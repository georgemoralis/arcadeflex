#ifndef __2610INTF_H__
#define __2610INTF_H__

#include "fm.h"
#ifdef BUILD_YM2610
  void YM2610UpdateRequest(int chip);
#endif

#define   MAX_2610    (2)

#ifndef VOL_YM3012
/* #define YM3014_VOL(Vol,Pan) VOL_YM3012((Vol)/2,Pan,(Vol)/2,Pan) */
#define YM3012_VOL(LVol,LPan,RVol,RPan) (MIXER(LVol,LPan)|(MIXER(RVol,RPan) << 16))
#endif

struct YM2610interface
{
	int num;	/* total number of 8910 in the machine */
	int baseclock;
	int volumeSSG[MAX_2610]; /* for SSG sound */
	int ( *portAread[MAX_2610] )( int offset );
	int ( *portBread[MAX_2610] )( int offset );
	void ( *portAwrite[MAX_2610] )( int offset, int data );
	void ( *portBwrite[MAX_2610] )( int offset, int data );
	void ( *handler[MAX_2610] )( int irq );	/* IRQ handler for the YM2610 */
	int pcmromb[MAX_2610];		/* Delta-T rom region */
	int pcmroma[MAX_2610];		/* ADPCM   rom region */
	int volumeFM[MAX_2610];		/* use YM3012_VOL macro */
};

/************************************************/
/* Sound Hardware Start							*/
/************************************************/
int YM2610_sh_start(const struct MachineSound *msound);
int YM2610B_sh_start(const struct MachineSound *msound);

/************************************************/
/* Sound Hardware Stop							*/
/************************************************/
void YM2610_sh_stop(void);

void YM2610_sh_reset(void);

/************************************************/
/* Chip 0 functions								*/
/************************************************/
int YM2610_status_port_0_A_r( int offset );
int YM2610_status_port_0_B_r( int offset );
int YM2610_read_port_0_r(int offset);
void YM2610_control_port_0_A_w(int offset,int data);
void YM2610_control_port_0_B_w(int offset,int data);
void YM2610_data_port_0_A_w(int offset,int data);
void YM2610_data_port_0_B_w(int offset,int data);

/************************************************/
/* Chip 1 functions								*/
/************************************************/
int YM2610_status_port_1_A_r( int offset );
int YM2610_status_port_1_B_r( int offset );
int YM2610_read_port_1_r(int offset);
void YM2610_control_port_1_A_w(int offset,int data);
void YM2610_control_port_1_B_w(int offset,int data);
void YM2610_data_port_1_A_w(int offset,int data);
void YM2610_data_port_1_B_w(int offset,int data);

#endif
/**************** end of file ****************/
