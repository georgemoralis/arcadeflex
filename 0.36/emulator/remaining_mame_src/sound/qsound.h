/*********************************************************

    Capcom Q-Sound system

*********************************************************/

#ifndef __QSOUND_H__
#define __QSOUND_H__

#define QSOUND_CLOCK    4000000   /* default 4MHz clock */

struct QSound_interface {
	int clock;					/* clock */
	int region;					/* memory region of sample ROM(s) */
	int mixing_level[2];		/* volume */
};

int  qsound_sh_start( const struct MachineSound *msound );
void qsound_sh_stop( void );

void qsound_data_h_w(int offset,int data);
void qsound_data_l_w(int offset,int data);
void qsound_cmd_w(int offset,int data);
int  qsound_status_r(int offset);

#endif /* __QSOUND_H__ */
