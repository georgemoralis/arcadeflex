#ifndef __2608INTF_H__
#define __2608INTF_H__

#include "fm.h"
#ifdef BUILD_YM2608
  void YM2608UpdateRequest(int chip);
#endif

#define   MAX_2608    (2)

#ifndef VOL_YM3012
/* #define YM3014_VOL(Vol,Pan) VOL_YM3012((Vol)/2,Pan,(Vol)/2,Pan) */
#define YM3012_VOL(LVol,LPan,RVol,RPan) (MIXER(LVol,LPan)|(MIXER(RVol,RPan) << 16))
#endif

struct YM2608interface{
	int num;	/* total number of 8910 in the machine */
	int baseclock;
	int volumeSSG[MAX_2608]; /* for SSG sound */
	int ( *portAread[MAX_2608] )( int offset );
	int ( *portBread[MAX_2608] )( int offset );
	void ( *portAwrite[MAX_2608] )( int offset, int data );
	void ( *portBwrite[MAX_2608] )( int offset, int data );
	void ( *handler[MAX_2608] )( int irq );	/* IRQ handler for the YM2608 */
	int pcmrom[MAX_2608];		/* Delta-T memory region ram/rom */
	int volumeFM[MAX_2608];		/* use YM3012_VOL macro */
};

/************************************************/
/* Sound Hardware Start							*/
/************************************************/
int YM2608_sh_start(const struct MachineSound *msound);

/************************************************/
/* Sound Hardware Stop							*/
/************************************************/
void YM2608_sh_stop(void);

void YM2608_sh_reset(void);

/************************************************/
/* Chip 0 functions								*/
/************************************************/
int YM2608_status_port_0_A_r( int offset );
int YM2608_status_port_0_B_r( int offset );
int YM2608_read_port_0_r(int offset);
void YM2608_control_port_0_A_w(int offset,int data);
void YM2608_control_port_0_B_w(int offset,int data);
void YM2608_data_port_0_A_w(int offset,int data);
void YM2608_data_port_0_B_w(int offset,int data);

/************************************************/
/* Chip 1 functions								*/
/************************************************/
int YM2608_status_port_1_A_r( int offset );
int YM2608_status_port_1_B_r( int offset );
int YM2608_read_port_1_r(int offset);
void YM2608_control_port_1_A_w(int offset,int data);
void YM2608_control_port_1_B_w(int offset,int data);
void YM2608_data_port_1_A_w(int offset,int data);
void YM2608_data_port_1_B_w(int offset,int data);

#endif
/**************** end of file ****************/
