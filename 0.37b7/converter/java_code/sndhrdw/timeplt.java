/***************************************************************************

	This code is used by the following module:

	timeplt.c
	pooyan.c
	locomotn.c
	tutankhm.c
	rocnrope.c

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package sndhrdw;

public class timeplt
{
	
	
	static MemoryReadAddress timeplt_sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x1fff, MRA_ROM ),
		new MemoryReadAddress( 0x2000, 0x23ff, MRA_RAM ),
		new MemoryReadAddress( 0x3000, 0x33ff, MRA_RAM ),
		new MemoryReadAddress( 0x4000, 0x4000, AY8910_read_port_0_r ),
		new MemoryReadAddress( 0x6000, 0x6000, AY8910_read_port_1_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress timeplt_sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x1fff, MWA_ROM ),
		new MemoryWriteAddress( 0x2000, 0x23ff, MWA_RAM ),
		new MemoryWriteAddress( 0x3000, 0x33ff, MWA_RAM ),
		new MemoryWriteAddress( 0x4000, 0x4000, AY8910_write_port_0_w ),
		new MemoryWriteAddress( 0x5000, 0x5000, AY8910_control_port_0_w ),
		new MemoryWriteAddress( 0x6000, 0x6000, AY8910_write_port_1_w ),
		new MemoryWriteAddress( 0x7000, 0x7000, AY8910_control_port_1_w ),
		new MemoryWriteAddress( 0x8000, 0x8fff, timeplt_filter_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	static AY8910interface timeplt_ay8910_interface = new AY8910interface
	(
		2,				/* 2 chips */
		14318180/8,		/* 1.789772727 MHz */
		new int[] { MIXERG(30,MIXER_GAIN_2x,MIXER_PAN_CENTER), MIXERG(30,MIXER_GAIN_2x,MIXER_PAN_CENTER) },
		new ReadHandlerPtr[] { soundlatch_r },
		new ReadHandlerPtr[] { timeplt_portB_r },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 }
	);
	
	
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
	
	static int timeplt_timer[10] =
	{
		0x00, 0x10, 0x20, 0x30, 0x40, 0x90, 0xa0, 0xb0, 0xa0, 0xd0
	};
	
	public static ReadHandlerPtr timeplt_portB_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* need to protect from totalcycles overflow */
		static int last_totalcycles = 0;
	
		/* number of Z80 clock cycles to count */
		static int clock;
	
		int current_totalcycles;
	
		current_totalcycles = cpu_gettotalcycles();
		clock = (clock + (current_totalcycles-last_totalcycles)) % 5120;
	
		last_totalcycles = current_totalcycles;
	
		return timeplt_timer[clock/512];
	} };
	
	
	static void filter_w(int chip, int channel, int data)
	{
		int C = 0;
	
		if ((data & 1) != 0) C += 220000;	/* 220000pF = 0.220uF */
		if ((data & 2) != 0) C +=  47000;	/*  47000pF = 0.047uF */
		set_RC_filter(3*chip + channel,1000,5100,0,C);
	}
	
	public static WriteHandlerPtr timeplt_filter_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		filter_w(0, 0, (offset >>  6) & 3);
		filter_w(0, 1, (offset >>  8) & 3);
		filter_w(0, 2, (offset >> 10) & 3);
		filter_w(1, 0, (offset >>  0) & 3);
		filter_w(1, 1, (offset >>  2) & 3);
		filter_w(1, 2, (offset >>  4) & 3);
	} };
	
	
	public static WriteHandlerPtr timeplt_sh_irqtrigger_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static int last;
	
		if (last == 0 && data)
		{
			/* setting bit 0 low then high triggers IRQ on the sound CPU */
			cpu_cause_interrupt(1,0xff);
		}
	
		last = data;
	} };
	
}
