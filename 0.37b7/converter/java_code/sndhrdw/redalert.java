/***************************************************************************

Irem Red Alert sound hardware

The manual lists two sets of sounds.

Analogue:
- Formation Aircraft
- Dive bombers
- Helicopters
- Launcher firing
- Explosion #1
- Explosion #2
- Explosion #3

Digital:
- Melody #1.  Starting sound.
- Melody #2.  Ending sound
- Time signal
- Chirping birds
- Alarm
- Excellent
- Coin insertion
- MIRV division
- Megaton bomb - long
- Megaton bomb - short
- Megaton bomb landing

If you have any questions about how this driver works, don't hesitate to
ask.  - Mike Balfour (mab22@po.cwru.edu)
***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package sndhrdw;

public class redalert
{
	
	static int AY8910_A_input_data = 0;
	static int c030_data = 0;
	static int sound_register_IC1 = 0;
	static int sound_register_IC2 = 0;
	
	public static WriteHandlerPtr redalert_c030_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		c030_data = data & 0x3F;
	
		/* Is this some type of sound command? */
		if ((data & 0x80) != 0)
			/* Cause an NMI on the voice CPU here? */
			cpu_cause_interrupt(2,I8085_RST75);
	} };
	
	public static ReadHandlerPtr redalert_voicecommand_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return c030_data;
	} };
	
	public static WriteHandlerPtr redalert_soundlatch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* The byte is connected to Port A of the AY8910 */
		AY8910_A_input_data = data;
	
		/* Bit D7 is also connected to the NMI input of the CPU */
		if ((data & 0x80)!=0x80)
			cpu_cause_interrupt(1,M6502_INT_NMI);
	} };
	
	public static ReadHandlerPtr redalert_AY8910_A_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return AY8910_A_input_data;
	} };
	
	public static WriteHandlerPtr redalert_AY8910_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* BC2 is connected to a pull-up resistor, so BC2=1 always */
		switch (data)
		{
			case 0x00:
				/* BC1=0, BDIR=0 : INACTIVE */
				break;
			case 0x01:
				/* BC1=1, BDIR=0 : READ FROM PSG */
				sound_register_IC1 = AY8910_read_port_0_r(offset);
				break;
			case 0x02:
				/* BC1=0, BDIR=1 : WRITE TO PSG */
				AY8910_write_port_0_w(offset,sound_register_IC2);
				break;
			case 0x03:
				/* BC1=1, BDIR=1 : LATCH ADDRESS */
				AY8910_control_port_0_w(offset,sound_register_IC2);
				break;
			default:
				logerror("Invalid Sound Command: %02X\n",data);
				break;
		}
	} };
	
	public static ReadHandlerPtr redalert_sound_register_IC1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return sound_register_IC1;
	} };
	
	public static WriteHandlerPtr redalert_sound_register_IC2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		sound_register_IC2 = data;
	} };
	
	public static WriteHandlerPtr redalert_AY8910_B_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* I'm fairly certain this port triggers analog sounds */
		logerror("Port B Trigger: %02X\n",data);
		/* D0 = Formation Aircraft? */
		/* D1 = Dive bombers? */
		/* D2 = Helicopters? */
		/* D3 = Launcher firing? */
		/* D4 = Explosion #1? */
		/* D5 = Explosion #2? */
		/* D6 = Explosion #3? */
	} };
	
}
