#ifndef YM2151INTF_H
#define YM2151INTF_H

#define MAX_2151 3

#ifndef VOL_YM3012
/* YM2151interface.volume optionaly macro */
/* #define YM3014_VOL(Vol,Pan) VOL_YM3012((Vol)/2,Pan,(Vol)/2,Pan) */
#define YM3012_VOL(LVol,LPan,RVol,RPan) (MIXER(LVol,LPan)|(MIXER(RVol,RPan) << 16))
#endif

struct YM2151interface
{
	int num;
	int baseclock;
	int volume[MAX_2151]; /* need for use YM3012()_VOL macro */
	void (*irqhandler[MAX_2151])(int irq);
	mem_write_handler portwritehandler[MAX_2151];
};




void YM2151UpdateRequest(int chip);
#endif
