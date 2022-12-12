#ifndef __2612INTF_H__
#define __2612INTF_H__

#include "fm.h"
#ifdef BUILD_YM2612
  void YM2612UpdateRequest(int chip);
#endif
#include "ay8910.h"

#define   MAX_2612    (2)

#define YM2612interface AY8910interface

int  YM2612_sh_start(const struct MachineSound *msound);
void YM2612_sh_stop(void);
void YM2612_sh_reset(void);

/************************************************/
/* Chip 0 functions								*/
/************************************************/
int YM2612_status_port_0_A_r( int offset );  /* A=0 : OPN status */
int YM2612_status_port_0_B_r( int offset );  /* A=2 : don't care */
int YM2612_read_port_0_r(int offset);        /* A=1 : don't care */
void YM2612_control_port_0_A_w(int offset,int data); /* A=0:OPN  address */
void YM2612_control_port_0_B_w(int offset,int data); /* A=2:OPN2 address */
void YM2612_data_port_0_A_w(int offset,int data);    /* A=1:OPN  data    */
void YM2612_data_port_0_B_w(int offset,int data);    /* A=3:OPN2 data    */

/************************************************/
/* Chip 1 functions								*/
/************************************************/
int YM2612_status_port_1_A_r( int offset );
int YM2612_status_port_1_B_r( int offset );
int YM2612_read_port_1_r(int offset);
void YM2612_control_port_1_A_w(int offset,int data);
void YM2612_control_port_1_B_w(int offset,int data);
void YM2612_data_port_1_A_w(int offset,int data);
void YM2612_data_port_1_B_w(int offset,int data);

/**************************************************/
/*   YM2612 left/right position change (TAITO)    */
/**************************************************/

#endif
/**************** end of file ****************/
