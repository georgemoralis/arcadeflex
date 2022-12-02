#ifndef AY8910_H
#define AY8910_H


#define MAX_8910 5
#define ALL_8910_CHANNELS -1

struct AY8910interface
{
	int num;	/* total number of 8910 in the machine */
	int baseclock;
	int mixing_level[MAX_8910];
	int (*portAread[MAX_8910])(int offset);
	int (*portBread[MAX_8910])(int offset);
	void (*portAwrite[MAX_8910])(int offset,int data);
	void (*portBwrite[MAX_8910])(int offset,int data);
	void (*handler[MAX_8910])(int irq);	/* IRQ handler for the YM2203 */
};

void AY8910_reset(int chip);

void AY8910_set_clock(int chip,int _clock);
void AY8910_set_volume(int chip,int channel,int volume);


void AY8910Write(int chip,int a,int data);
int AY8910Read(int chip);


int AY8910_read_port_0_r(int offset);
int AY8910_read_port_1_r(int offset);
int AY8910_read_port_2_r(int offset);
int AY8910_read_port_3_r(int offset);
int AY8910_read_port_4_r(int offset);

void AY8910_control_port_0_w(int offset,int data);
void AY8910_control_port_1_w(int offset,int data);
void AY8910_control_port_2_w(int offset,int data);
void AY8910_control_port_3_w(int offset,int data);
void AY8910_control_port_4_w(int offset,int data);

void AY8910_write_port_0_w(int offset,int data);
void AY8910_write_port_1_w(int offset,int data);
void AY8910_write_port_2_w(int offset,int data);
void AY8910_write_port_3_w(int offset,int data);
void AY8910_write_port_4_w(int offset,int data);

int AY8910_sh_start(const struct MachineSound *msound);

#endif
