/***************************************************************************

Atari Video Pinball Machine

***************************************************************************/


/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package machine;

public class videopin
{
	//
	int ball_position;
	int attract = 0;		/* Turns off sound in attract mode */
	int volume = 0;			/* Volume of the noise signal */
	
	static int NMI_mask = 1; // Active LOW
	
	static int plunger_counter = -1;
	
	public static InterruptPtr videopin_interrupt = new InterruptPtr() { public int handler() 
	{
		static int prev,counter;
		int curr;
	
		/* Plunger simulation
		 *	1) We look at pressure time on the plunger key to evaluate a strength
		 *     The chosen interval is from 0 to 3 seconds, the longer gives more strength
		 *	2) When the key is depressed we activate the NMI,
		 *     which is responsable for reading plunger hardware input.
		 *  3) Thereafter we feed input to the software corresponding to the strength
		 *     The NMI will make 1792 read before setting force to 0.
		 *     The quicker we drop PLUNGER2 LOW, the greater the strength
		 */
		if (cpu_getiloops() == 0)
		{
			curr = input_port_3_r(0) & 1;
			if (curr != prev)
			{
				if (curr != 0)	/* key pressed; initiate count */
				{
					counter = 2*60;
				}
				else	/* key released; cause NMI */
				{
					plunger_counter = counter*5 + 3;
					cpu_cause_interrupt(0,M6502_INT_NMI);
				}
			}
			if (curr != 0)
			{
				if (counter > 0) counter--;
			}
			prev = curr;
		}
	
		return interrupt();
	} };
	
	public static WriteHandlerPtr videopin_out1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		NMI_mask = data & 0x10;
		if (NMI_mask != 0)
			{
				logerror("out1_w, NMI mask OFF\n");
			}
		else logerror("out1_w, NMI mask ON\n");
	
	//	if ((data & 0x10) != 0) logerror("out1_w, NMI mask\n");
	//	if ((data & 0x08) != 0) logerror("out1_w, lockout coil\n");
	//	if ((data & 0x07) != 0) logerror("out1_w, audio frequency : %02x\n", data & 0x07);
	} };
	
	public static WriteHandlerPtr videopin_out2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	//	if ((data & 0x80) != 0) logerror("out2_w, audio disable during attract\n");
	//	if ((data & 0x40) != 0) logerror("out2_w, bell audio gen enable\n");
	//	if ((data & 0x20) != 0) logerror("out2_w, bong audio gen enable\n");
	//	if ((data & 0x10) != 0) logerror("out2_w, coin counter\n");
	//	if ((data & 0x07) != 0) logerror("out2_w, audio volume : %02x\n", data & 0x07);
	} };
	
	public static WriteHandlerPtr videopin_led_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		// No LEDs for now
	//	logerror("led_w, LED write : %02x\n", data);
	} };
	
	
	public static WriteHandlerPtr videopin_watchdog_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	//	logerror("watchdog_w, counter clear %02x:%02x\n", offset, data);
	} };
	
	public static WriteHandlerPtr videopin_ball_position_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		//logerror("ball_position_w, ball position : %02x\n", data);
		ball_position = data;
	} };
	
	// No sound yet
	// Load audio frequency
	public static WriteHandlerPtr videopin_note_dvslrd_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	//	logerror("note_dvslrd_w, load audio frequency : %02x\n", data);
	} };
	
	
	public static ReadHandlerPtr videopin_in0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	//	logerror("in0_r\n");
		return input_port_0_r.handler(offset);
	} };
	
	public static ReadHandlerPtr videopin_in1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	//	logerror("in1_r\n");
		return input_port_1_r.handler(offset);
	} };
	
	public static ReadHandlerPtr videopin_in2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int res;
	
		res = input_port_2_r.handler(0);
	
		if (plunger_counter >= 0) plunger_counter--;	/* will stop at -1 */
		if (plunger_counter != 0)
			res |= 2;
		if (plunger_counter == -1)
			res |= 1;
	
		return res;
	} };
	
}
