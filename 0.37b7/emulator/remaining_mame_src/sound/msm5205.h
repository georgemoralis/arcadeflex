#ifndef MSM5205_H
#define MSM5205_H

/* an interface for the MSM5205 and similar chips */

#define MAX_MSM5205 4

/* priscaler selector defines   */
/* default master clock is 384KHz */
#define MSM5205_S96_3B 0     /* prsicaler 1/96(4KHz) , data 3bit */
#define MSM5205_S48_3B 1     /* prsicaler 1/48(8KHz) , data 3bit */
#define MSM5205_S64_3B 2     /* prsicaler 1/64(6KHz) , data 3bit */
#define MSM5205_SEX_3B 3     /* VCLK slave mode      , data 3bit */
#define MSM5205_S96_4B 4     /* prsicaler 1/96(4KHz) , data 4bit */
#define MSM5205_S48_4B 5     /* prsicaler 1/48(8KHz) , data 4bit */
#define MSM5205_S64_4B 6     /* prsicaler 1/64(6KHz) , data 4bit */
#define MSM5205_SEX_4B 7     /* VCLK slave mode      , data 4bit */

struct MSM5205interface
{
	int num;                       /* total number of chips                 */
	int baseclock;                 /* master clock (default = 384KHz)       */
	void (*vclk_interrupt[MAX_MSM5205])(int);   /* VCLK interrupt callback  */
	int select[MAX_MSM5205];       /* prescaler / bit width selector        */
	int mixing_level[MAX_MSM5205]; /* master volume                         */
};

int MSM5205_sh_start (const struct MachineSound *msound);
void MSM5205_sh_stop (void);   /* empty this function */
void MSM5205_sh_update (void); /* empty this function */
void MSM5205_sh_reset (void);

/* reset signal should keep for 2cycle of VCLK      */
void MSM5205_reset_w (int num, int reset);
/* adpcmata is latched after vclk_interrupt callback */
void MSM5205_data_w (int num, int data);
/* VCLK slave mode option                                        */
/* if VCLK and reset or data is changed at the same time,        */
/* Call MSM5205_vclk_w after MSM5205_data_w and MSM5205_reset_w. */
void MSM5205_vclk_w (int num, int reset);
/* option , selected pin seletor */
void MSM5205_playmode_w(int num,int _select);

void MSM5205_set_volume(int num,int volume);

#endif
