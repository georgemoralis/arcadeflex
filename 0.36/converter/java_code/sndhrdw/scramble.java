/***************************************************************************

  This sound driver is used by the Scramble, Super Cobra  and Amidar drivers.

***************************************************************************/


/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package sndhrdw;

public class scramble
{
	
	
	
	/* The timer clock which feeds the upper 4 bits of    					*/
	/* AY-3-8910 port A is based on the same clock        					*/
	/* feeding the sound CPU Z80.  It is a divide by      					*/
	/* 5120, formed by a standard divide by 512,        					*/
	/* followed by a divide by 10 using a 4 bit           					*/
	/* bi-quinary count sequence. (See LS90 data sheet    					*/
	/* for an example).                                   					*/
	/*																		*/
	/* Bit 4 comes from the output of the divide by 1024  					*/
	/*       0, 1, 0, 1, 0, 1, 0, 1, 0, 1									*/
	/* Bit 5 comes from the QC output of the LS90 producing a sequence of	*/
	/* 		 0, 0, 1, 1, 0, 0, 1, 1, 1, 0									*/
	/* Bit 6 comes from the QD output of the LS90 producing a sequence of	*/
	/*		 0, 0, 0, 0, 1, 0, 0, 0, 0, 1									*/
	/* Bit 7 comes from the QA output of the LS90 producing a sequence of	*/
	/*		 0, 0, 0, 0, 0, 1, 1, 1, 1, 1			 						*/
	
	static int scramble_timer[10] =
	{
		0x00, 0x10, 0x20, 0x30, 0x40, 0x90, 0xa0, 0xb0, 0xa0, 0xd0
	};
	
	public static ReadHandlerPtr scramble_portB_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* need to protect from totalcycles overflow */
		static int last_totalcycles = 0;
	
		/* number of Z80 clock cycles to count */
		static int clock;
	
		int current_totalcycles;
	
		current_totalcycles = cpu_gettotalcycles();
		clock = (clock + (current_totalcycles-last_totalcycles)) % 5120;
	
		last_totalcycles = current_totalcycles;
	
		return scramble_timer[clock/512];
	} };
	
	
	
	public static WriteHandlerPtr scramble_sh_irqtrigger_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		static int last;
	
	
		if (last == 0 && (data & 0x08) != 0)
		{
			/* setting bit 3 low then high triggers IRQ on the sound CPU */
			cpu_cause_interrupt(1, Z80_IRQ_INT);
		}
	
		last = data & 0x08;
	} };
	
	public static WriteHandlerPtr hotshock_sh_irqtrigger_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		cpu_cause_interrupt(1, Z80_IRQ_INT);
	} };
	
	
	static void filter_w(int chip, int channel, int data)
	{
		int C;
	
	
		C = 0;
		if ((data & 1) != 0) C += 220000;	/* 220000pF = 0.220uF */
		if ((data & 2) != 0) C +=  47000;	/*  47000pF = 0.047uF */
		set_RC_filter(3*chip + channel,1000,5100,0,C);
	}
	
	public static WriteHandlerPtr scramble_filter_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		filter_w(1, 0, (offset >>  0) & 3);
		filter_w(1, 1, (offset >>  2) & 3);
		filter_w(1, 2, (offset >>  4) & 3);
		filter_w(0, 0, (offset >>  6) & 3);
		filter_w(0, 1, (offset >>  8) & 3);
		filter_w(0, 2, (offset >> 10) & 3);
	} };
}
