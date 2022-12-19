/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package sndhrdw;

public class irem
{
	
	
	
	public static WriteHandlerPtr irem_sound_cmd_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x80) == 0)
			soundlatch_w.handler(0,data & 0x7f);
		else
			cpu_set_irq_line(1,0,HOLD_LINE);
	} };
	
	
	static int port1,port2;
	
	public static WriteHandlerPtr irem_port1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		port1 = data;
	} };
	
	public static WriteHandlerPtr irem_port2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* write latch */
		if ((port2 & 0x01) && !(data & 0x01))
		{
			/* control or data port? */
			if ((port2 & 0x04) != 0)
			{
				/* PSG 0 or 1? */
				if ((port2 & 0x08) != 0)
					AY8910_control_port_0_w(0,port1);
				if ((port2 & 0x10) != 0)
					AY8910_control_port_1_w(0,port1);
			}
			else
			{
				/* PSG 0 or 1? */
				if ((port2 & 0x08) != 0)
					AY8910_write_port_0_w(0,port1);
				if ((port2 & 0x10) != 0)
					AY8910_write_port_1_w(0,port1);
			}
		}
		port2 = data;
	} };
	
	
	public static ReadHandlerPtr irem_port1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* PSG 0 or 1? */
		if ((port2 & 0x08) != 0)
			return AY8910_read_port_0_r(0);
		if ((port2 & 0x10) != 0)
			return AY8910_read_port_1_r(0);
		return 0xff;
	} };
	
	public static ReadHandlerPtr irem_port2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return 0;
	} };
	
	
	
	public static WriteHandlerPtr irem_msm5205_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* bits 2-4 select MSM5205 clock & 3b/4b playback mode */
		MSM5205_playmode_w(0,(data >> 2) & 7);
		MSM5205_playmode_w(1,((data >> 2) & 4) | 3);	/* always in slave mode */
	
		/* bits 0 and 1 reset the two chips */
		MSM5205_reset_w(0,data & 1);
		MSM5205_reset_w(1,data & 2);
	} };
	
	public static WriteHandlerPtr irem_adpcm_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		MSM5205_data_w(offset,data);
	} };
	
	public static vclk_interruptPtr irem_adpcm_int = new vclk_interruptPtr() { public void handler(int data) 
	{
		cpu_set_nmi_line(1,PULSE_LINE);
	
		/* the first MSM5205 clocks the second */
		MSM5205_vclk_w(1,1);
		MSM5205_vclk_w(1,0);
	} };
	
	public static WriteHandlerPtr irem_analog_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	#ifdef MAME_DEBUG
	if ((data & 0x0f) != 0) usrintf_showmessage("analog sound %x",data&0x0f);
	#endif
	} };
	
	
	static AY8910interface irem_ay8910_interface = new AY8910interface
	(
		2,	/* 2 chips */
		3579545/4,
		new int[] { 20, 20 },
		new ReadHandlerPtr[] { soundlatch_r, 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0, irem_analog_w },
		new WriteHandlerPtr[] { irem_msm5205_w, 0 }
	);
	
	static MSM5205interface irem_msm5205_interface = new MSM5205interface
	(
		2,					/* 2 chips            */
		384000,				/* 384KHz             */
		new vclk_interruptPtr[] { irem_adpcm_int, 0 },/* interrupt function */
		new int[] { MSM5205_S96_4B,MSM5205_SEX_4B },	/* default to 4KHz, but can be changed at run time */
		{ 100, 100 }
	);
	
	
	
	static MemoryReadAddress irem_sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x001f, m6803_internal_registers_r ),
		new MemoryReadAddress( 0x0080, 0x00ff, MRA_RAM ),
		new MemoryReadAddress( 0x4000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress irem_sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x001f, m6803_internal_registers_w ),
		new MemoryWriteAddress( 0x0080, 0x00ff, MWA_RAM ),
		new MemoryWriteAddress( 0x0800, 0x0800, MWA_NOP ),    /* IACK */
		new MemoryWriteAddress( 0x0801, 0x0802, irem_adpcm_w ),
		new MemoryWriteAddress( 0x9000, 0x9000, MWA_NOP ),    /* IACK */
		new MemoryWriteAddress( 0x4000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static IOReadPort irem_sound_readport[] =
	{
		new IOReadPort( M6803_PORT1, M6803_PORT1, irem_port1_r ),
		new IOReadPort( M6803_PORT2, M6803_PORT2, irem_port2_r ),
		new IOReadPort( -1 )	/* end of table */
	};
	
	static IOWritePort irem_sound_writeport[] =
	{
		new IOWritePort( M6803_PORT1, M6803_PORT1, irem_port1_w ),
		new IOWritePort( M6803_PORT2, M6803_PORT2, irem_port2_w ),
		new IOWritePort( -1 )	/* end of table */
	};
}
