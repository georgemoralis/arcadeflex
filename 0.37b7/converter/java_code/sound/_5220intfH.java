#ifndef intf5220_h
#define intf5220_h

struct TMS5220interface
{
	int baseclock;				/* clock rate = 80 * output sample rate,     */
								/* usually 640000 for 8000 Hz sample rate or */
								/* usually 800000 for 10000 Hz sample rate.  */
	int mixing_level;
	void (*irq)(int state);		/* IRQ callback function */
};



void tms5220_set_frequency(int frequency);

#endif

