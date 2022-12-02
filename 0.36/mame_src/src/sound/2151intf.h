#ifndef YM2151INTF_H
#define YM2151INTF_H

#define MAX_2151 3

#ifndef VOL_YM3012
/* YM2151interface->volume optionaly macro */
/* #define YM3014_VOL(Vol,Pan) VOL_YM3012((Vol)/2,Pan,(Vol)/2,Pan) */
#define YM3012_VOL(LVol,LPan,RVol,RPan) (MIXER(LVol,LPan)|(MIXER(RVol,RPan) << 16))
#endif

struct YM2151interface
{
	int num;
	int baseclock;
	int volume[MAX_2151]; /* need for use YM3012()_VOL macro */
	void (*irqhandler[MAX_2151])(int irq);
	void (*portwritehandler[MAX_2151])(int,int);
};

int YM2151_status_port_0_r(int offset);
int YM2151_status_port_1_r(int offset);
int YM2151_status_port_2_r(int offset);

void YM2151_register_port_0_w(int offset,int data);
void YM2151_register_port_1_w(int offset,int data);
void YM2151_register_port_2_w(int offset,int data);

void YM2151_data_port_0_w(int offset,int data);
void YM2151_data_port_1_w(int offset,int data);
void YM2151_data_port_2_w(int offset,int data);
int YM2151_sh_start(const struct MachineSound *msound);
int YM2151_ALT_sh_start(const struct MachineSound *msound);
void YM2151_sh_stop(void);
void YM2151_sh_reset(void);

void YM2151UpdateRequest(int chip);
#endif
