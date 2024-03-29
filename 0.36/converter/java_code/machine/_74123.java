/*****************************************************************************

  74123 monoflop emulator - there are 2 monoflops per chips

  74123 pins and assigned interface variables/functions

	TTL74123_trigger_comp_w	[ 1] /1TR		   VCC [16]
	TTL74123_trigger_w		[ 2]  1TR		1RCext [15]
  	TTL74123_reset_comp_w	[ 3] /1RST		 1Cext [14]
	TTL74123_output_comp_r	[ 4] /1Q		    1Q [13] TTL74123_output_r
	TTL74123_output_r		[ 5]  2Q	       /2Q [12] TTL74123_output_comp_r
		 					[ 6]  2Cext	     /2RST [11] TTL74123_reset_comp_w
		 					[ 7]  2RCext       2TR [10] TTL74123_trigger_w
   							[ 8]  GND	      /2TR [9]  TTL74123_trigger_comp_w

	All resistor values in Ohms.
	All capacitor values in Farads.

	Truth table:
	R	A	B | Q  /Q
	----------|-------
	L	X	X | L	H
	X	H	X | L	H
	X	X	L | L	H
	H	L  _- |_-_ -_-
	H  -_	H |_-_ -_-
	_-	L	H |_-_ -_-
	------------------
	A	= trigger_comp
	B	= trigger
	R	= reset_comp
	L	= lo (0)
	H	= hi (1)
	X	= any state
	_-	= raising edge
	-_	= falling edge
	_-_ = positive pulse
	-_- = negative pulse

 *****************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package machine;

public class _74123
{
	
	#define VERBOSE 0
	
	#if VERBOSE
	#define LOG(x) if (errorlog != 0) fprintf x
	#else
	#define LOG(x)
	#endif
	
	struct TTL74123 {
		const struct TTL74123_interface *intf;
		int trigger;			/* pin 2/10 */
		int trigger_comp;		/* pin 1/9 */
		int reset_comp;			/* pin 3/11 */
		int output;				/* pin 13/5 */
		void *timer;
	};
	
	static struct TTL74123 chip[MAX_TTL74123];
	
	
	static public static WriteHandlerPtr set_output = new WriteHandlerPtr() { public void handler(int which, int data)
	{
		chip[which].output = data;
		chip[which].intf.output_changed_cb();
	} };
	
	
	void TTL74123_config(int which, const struct TTL74123_interface *intf)
	{
		if (which >= MAX_TTL74123) return;
		chip[which].intf = intf;
		/* all inputs are open first */
	    chip[which].trigger = 1;
		chip[which].trigger_comp = 1;
		chip[which].reset_comp = 1;
		set_output(which, 1);
	}
	
	
	void TTL74123_unconfig(void)
	{
		int i;
	
		for (i = 0; i < MAX_TTL74123; i++)
		{
			if (chip[i].timer)  timer_remove(chip[i].timer);
		}
	
		memset(&chip, 0, sizeof(chip));
	}
	
	
	static void clear_callback(int which)
	{
		struct TTL74123 *c = chip + which;
	
	    c.timer = 0;
		set_output(which, 0);
	}
	
	
	#define CHECK_TRIGGER(TRIG) 												\
		if (TRIG != 0)																\
		{																		\
			double duration = TIME_IN_SEC(0.68 * c.intf.res * c.intf.cap);	\
																				\
			if (c.timer)														\
				timer_reset(c.timer, duration);								\
			else																\
			{																	\
				set_output(which, 1);											\
				c.timer = timer_set(duration, which, clear_callback);			\
			}																	\
		}
	
	
	public static WriteHandlerPtr TTL74123_trigger_w = new WriteHandlerPtr() { public void handler(int which, int data)
	{
		struct TTL74123 *c = chip + which;
	
		/* trigger_comp=lo and rising edge on trigger (while reset_comp is hi) */
		CHECK_TRIGGER(!c.trigger_comp && data && !c.trigger && c.reset_comp)
	
		c.trigger = data;
	} };
	
	
	public static WriteHandlerPtr TTL74123_trigger_comp_w = new WriteHandlerPtr() { public void handler(int which, int data)
	{
		struct TTL74123 *c = chip + which;
	
		/* trigger=hi and falling edge on trigger_comp (while reset_comp is hi) */
		CHECK_TRIGGER(c.trigger && !data && c.trigger_comp && c.reset_comp)
	
		c.trigger_comp = data;
	} };
	
	
	public static WriteHandlerPtr TTL74123_reset_comp_w = new WriteHandlerPtr() { public void handler(int which, int data)
	{
		struct TTL74123 *c = chip + which;
	
		/* trigger=hi, trigger_comp=lo and rising edge on reset_comp */
	    CHECK_TRIGGER(c.trigger && !c.trigger_comp && data && !c.reset_comp)
	
		/* reset_comp=lo and timer running */
		if (!data && c.timer)
	    {
			timer_reset(c.timer, TIME_NOW);
	    }
	
		c.reset_comp = data;
	} };
	
	
	public static ReadHandlerPtr TTL74123_output_r = new ReadHandlerPtr() { public int handler(int which)
	{
		return chip[which].output;
	} };
	
	
	public static ReadHandlerPtr TTL74123_output_comp_r = new ReadHandlerPtr() { public int handler(int which)
	{
		return !chip[which].output;
	} };
}
