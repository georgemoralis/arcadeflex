#ifndef DAC_H
#define DAC_H

#define MAX_DAC 4

struct DACinterface
{
	int num;	/* total number of DACs */
	int mixing_level[MAX_DAC];
};

int DAC_sh_start(const struct MachineSound *msound);
void DAC_data_w(int num,int data);
void DAC_signed_data_w(int num,int data);
void DAC_data_16_w(int num,int data);
void DAC_signed_data_16_w(int num,int data);

#endif
