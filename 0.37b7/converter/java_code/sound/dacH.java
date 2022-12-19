#ifndef DAC_H
#define DAC_H

#ifdef __cplusplus
extern "C" {
#endif

#define MAX_DAC 4

struct DACinterface
{
	int num;	/* total number of DACs */
	int mixing_level[MAX_DAC];
};

void DAC_data_w(int num,int data);
void DAC_signed_data_w(int num,int data);
void DAC_data_16_w(int num,int data);
void DAC_signed_data_16_w(int num,int data);


#ifdef __cplusplus
}
#endif

#endif
