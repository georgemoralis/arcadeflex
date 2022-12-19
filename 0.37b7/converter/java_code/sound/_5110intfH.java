#ifndef intf5110_h
#define intf5110_h

struct TMS5110interface
{
	int baseclock;				/* clock rate = 80 * output sample rate,     */
								/* usually 640000 for 8000 Hz sample rate or */
								/* usually 800000 for 10000 Hz sample rate.  */
	int mixing_level;
	void (*irq)(int state);		/* IRQ callback function */
	int (*M0_callback)(void);	/* function to be called when chip requests another bit*/
};




void tms5110_set_frequency(int frequency);

#endif

