/***************************************************************************

	Off the Wall

    driver by Aaron Giles

****************************************************************************/


/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.drivers;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import gr.codebb.arcadeflex.common.SubArrays.IntSubArray;
import gr.codebb.arcadeflex.common.SubArrays.UShortArray;
import static common.libc.cstring.memcpy;
import static gr.codebb.arcadeflex.v037b7.machine.atarigen.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigenH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v037b7.sndhrdw.atarijsaH.*;
import static gr.codebb.arcadeflex.v037b7.sndhrdw.atarijsa.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.offtwall.*;

public class offtwall
{
	/*************************************
	 *
	 *	Interrupt handling
	 *
	 *************************************/
	
	static atarigen_int_callbackPtr update_interrupts = new atarigen_int_callbackPtr() {
            @Override
            public void handler() {
		int newstate = 0;
	
		if (atarigen_scanline_int_state != 0)
			newstate = 4;
		if (atarigen_sound_int_state != 0)
			newstate = 6;
	
		if (newstate != 0)
			cpu_set_irq_line(0, newstate, ASSERT_LINE);
		else
			cpu_set_irq_line(0, 7, CLEAR_LINE);
            }
        };
	
	
	/*************************************
	 *
	 *	Initialization
	 *
	 *************************************/
	
	public static InitMachinePtr init_machine = new InitMachinePtr() { public void handler() 
	{
		atarigen_eeprom_reset();
		atarigen_video_control_reset();
		atarigen_interrupt_reset(update_interrupts);
		atarigen_scanline_timer_reset(offtwall_scanline_update, 8);
		atarijsa_reset();
	} };
	
	
	
	/*************************************
	 *
	 *	I/O handling
	 *
	 *************************************/
	
	public static ReadHandlerPtr special_port3_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int result = input_port_3_r.handler(offset);
		if (atarigen_cpu_to_sound_ready != 0) result ^= 0x0020;
		return result;
	} };
	
	
	public static WriteHandlerPtr io_latch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* lower byte */
		if ((data & 0x00ff0000)==0)
		{
			/* bit 4 resets the sound CPU */
			cpu_set_reset_line(1, (data & 0x10)!=0 ? CLEAR_LINE : ASSERT_LINE);
			if ((data & 0x10)==0) atarijsa_reset();
		}
	
		logerror("sound control = %04X\n", data);
	} };
	
	
	
	/*************************************
	 *
	 *	Son-of-slapstic workarounds
	 *
	 *************************************/
	
	
	/*-------------------------------------------------------------------------
	
		Bankswitching
	
		Like the slapstic, the SoS bankswitches memory using A13 and A14.
		Unlike the slapstic, the exact addresses to trigger the bankswitch
		are unknown.
	
		Fortunately, Off the Wall uses a common routine for the important
		bankswitching. The playfield data is stored in the banked area of
		ROM, and by comparing the playfields to a real system, a mechanism
		to bankswitch at the appropriate time was discovered. Fortunately,
		it's really basic.
	
		OtW looks up the address to read playfield data from a table which
		is 58 words long. Word 0 assumes the bank is 0, word 1 assumes the
		bank is 1, etc. So we just trigger off of the table read and cause
		the bank to switch then.
	
		In addition, there is code which checksums longs from $40000 down to
		$3e000. The code wants that checksum to be $aaaa5555, but there is
		no obvious way for this to happen. To work around this, we watch for
		the final read from $3e000 and tweak the value such that the checksum
		will come out the $aaaa5555 magically.
	
	-------------------------------------------------------------------------*/
	
	static UBytePtr bankswitch_base=new UBytePtr();
	static UBytePtr bankrom_base=new UBytePtr();
	static int bank_offset;
	
	public static ReadHandlerPtr bankswitch_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* this is the table lookup; the bank is determined by the address that was requested */
		bank_offset = ((offset / 2) & 3) * 0x2000;
		logerror("Bankswitch index %d . %04X\n", offset, bank_offset);
	
		return bankswitch_base.READ_WORD(offset);
	} };
	
	public static ReadHandlerPtr bankrom_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* this is the banked ROM read */
		logerror("%06X: %04X\n", cpu_getpreviouspc(), offset);
	
		/* if the values are $3e000 or $3e002 are being read by code just below the
			ROM bank area, we need to return the correct value to give the proper checksum */
/*TODO*///		if ((offset == 0x6000 || offset == 0x6002) && cpu_getpreviouspc() > 0x37000)
/*TODO*///		{
/*TODO*///			int checksum = cpu_readmem24bew_dword(0x3fd210);
/*TODO*///			int us = 0xaaaa5555 - checksum;
/*TODO*///			if (offset == 0x6002)
/*TODO*///				return us & 0xffff;
/*TODO*///			else
/*TODO*///				return us >> 16;
/*TODO*///		}
	
		return bankrom_base.READ_WORD((bank_offset + offset) & 0x7fff);
	} };
	
	
	/*-------------------------------------------------------------------------
	
		Sprite Cache
	
		Somewhere in the code, if all the hardware tests are met properly,
		some additional dummy sprites are added to the sprite cache before
		they are copied to sprite RAM. The sprite RAM copy routine computes
		the total width of all sprites as they are copied and if the total
		width is less than or equal to 38, it adds a "HARDWARE ERROR" sprite
		to the end.
	
		Here we detect the read of the sprite count from within the copy
		routine, and add some dummy sprites to the cache ourself if there
		isn't enough total width.
	
	-------------------------------------------------------------------------*/
	
	static UBytePtr spritecache_count = new UBytePtr();
	
	public static ReadHandlerPtr spritecache_count_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int prevpc = cpu_getpreviouspc();
	
		/* if this read is coming from $99f8 or $9992, it's in the sprite copy loop */
		if (prevpc == 0x99f8 || prevpc == 0x9992)
		{
			UShortArray data = new UShortArray(spritecache_count, -0x200);
			int oldword = spritecache_count.READ_WORD(0);
			int count = oldword >> 8;
			int i, width = 0;
	
			/* compute the current total width */
			for (i = 0; i < count; i++)
				width += 1 + ((data.read(i * 4 + 1) >> 4) & 7);
	
			/* if we're less than 39, keep adding dummy sprites until we hit it */
			if (width <= 38)
			{
				while (width <= 38)
				{
					data.write(count * 4 + 0, (42 * 8) << 7);
					data.write(count * 4 + 1, ((30 * 8) << 7) | (7 << 4));
					data.write(count * 4 + 2, 0);
					width += 8;
					count++;
				}
	
				/* update the final count in memory */
				spritecache_count.WRITE_WORD(0, (count << 8) | (oldword & 0xff));
			}
		}
	
		/* and then read the data */
		return spritecache_count.READ_WORD(offset);
	} };
	
	
	/*-------------------------------------------------------------------------
	
		Unknown Verify
	
		In several places, the value 1 is stored to the byte at $3fdf1e. A
		fairly complex subroutine is called, and then $3fdf1e is checked to
		see if it was set to zero. If it was, "HARDWARE ERROR" is displayed.
	
		To avoid this, we just return 1 when this value is read within the
		range of PCs where it is tested.
	
	-------------------------------------------------------------------------*/
	
	static UBytePtr unknown_verify_base = new UBytePtr();
	
	public static ReadHandlerPtr unknown_verify_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int prevpc = cpu_getpreviouspc();
		if (prevpc < 0x5c5e || prevpc > 0xc432)
			return unknown_verify_base.READ_WORD(offset);
		else
			return unknown_verify_base.READ_WORD(offset) | 0x100;
	} };
	
	
	
	/*************************************
	 *
	 *	Main CPU memory handlers
	 *
	 *************************************/
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x037fff, MRA_ROM ),
		new MemoryReadAddress( 0x038000, 0x03ffff, bankrom_r ),
		new MemoryReadAddress( 0x120000, 0x120fff, atarigen_eeprom_r ),
		new MemoryReadAddress( 0x260000, 0x260001, input_port_0_r ),
		new MemoryReadAddress( 0x260002, 0x260003, input_port_1_r ),
		new MemoryReadAddress( 0x260010, 0x260011, special_port3_r ),
		new MemoryReadAddress( 0x260012, 0x260013, input_port_4_r ),
		new MemoryReadAddress( 0x260020, 0x260021, input_port_5_r ),
		new MemoryReadAddress( 0x260022, 0x260023, input_port_6_r ),
		new MemoryReadAddress( 0x260024, 0x260025, input_port_7_r ),
		new MemoryReadAddress( 0x260030, 0x260031, atarigen_sound_r ),
		new MemoryReadAddress( 0x3e0000, 0x3e0fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x3effc0, 0x3effff, atarigen_video_control_r ),
		new MemoryReadAddress( 0x3f4000, 0x3f7fff, MRA_BANK3 ),
		new MemoryReadAddress( 0x3f8000, 0x3fcfff, MRA_BANK4 ),
		new MemoryReadAddress( 0x3fd000, 0x3fd3ff, MRA_BANK5 ),
		new MemoryReadAddress( 0x3fd400, 0x3fffff, MRA_BANK6 ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x037fff, MWA_ROM ),
		new MemoryWriteAddress( 0x038000, 0x03ffff, MWA_ROM, bankrom_base ),
		new MemoryWriteAddress( 0x120000, 0x120fff, atarigen_eeprom_w, atarigen_eeprom, atarigen_eeprom_size ),
		new MemoryWriteAddress( 0x260040, 0x260041, atarigen_sound_w ),
		new MemoryWriteAddress( 0x260050, 0x260051, io_latch_w ),
		new MemoryWriteAddress( 0x260060, 0x260061, atarigen_eeprom_enable_w ),
		new MemoryWriteAddress( 0x2a0000, 0x2a0001, watchdog_reset_w ),
		new MemoryWriteAddress( 0x3e0000, 0x3e0fff, atarigen_666_paletteram_w, paletteram ),
		new MemoryWriteAddress( 0x3effc0, 0x3effff, atarigen_video_control_w, atarigen_video_control_data ),
		new MemoryWriteAddress( 0x3f4000, 0x3f7fff, offtwall_playfieldram_w, atarigen_playfieldram, atarigen_playfieldram_size ),
		new MemoryWriteAddress( 0x3f8000, 0x3fcfff, MWA_BANK4 ),
		new MemoryWriteAddress( 0x3fd000, 0x3fd3ff, MWA_BANK5, atarigen_spriteram ),
		new MemoryWriteAddress( 0x3fd400, 0x3fffff, MWA_BANK6 ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	
	/*************************************
	 *
	 *	Port definitions
	 *
	 *************************************/
	
	static InputPortPtr input_ports_offtwall = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* 260000 */
		PORT_BIT(  0x0001, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
		PORT_BIT(  0x0002, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT(  0x0002, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT(  0x0004, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT(  0x0008, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT(  0x0010, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
		PORT_BIT(  0x0020, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 );
		PORT_BIT(  0x0040, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 );
		PORT_BIT(  0x0080, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 );
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT(  0x0400, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT(  0x0800, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT(  0x1000, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );
		PORT_BIT(  0x2000, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1 );
		PORT_BIT(  0x4000, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1 );
		PORT_BIT(  0x8000, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1 );
	
		PORT_START(); 	/* 260002 */
		PORT_BIT(  0x00ff, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER3 );
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_START3 );
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
		PORT_BIT(  0x0400, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );
		PORT_BIT(  0x0800, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER3 );
		PORT_BIT(  0x1000, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER3 );
		PORT_BIT(  0x2000, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER3 );
		PORT_BIT(  0x4000, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER3 );
		PORT_BIT(  0x8000, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER3 );
	
		JSA_III_PORT();	/* audio board port */
	
		PORT_START(); 	/* 260010 */
		PORT_BIT(  0x0001, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_DIPNAME( 0x0002, 0x0000, "Controls" );
		PORT_DIPSETTING(      0x0000, "Whirly-gigs" );/* this is official Atari terminology! */
		PORT_DIPSETTING(      0x0002, "Joysticks" );
		PORT_BIT(  0x0004, IP_ACTIVE_LOW, IPT_UNUSED );/* tested at a454 */
		PORT_BIT(  0x0008, IP_ACTIVE_LOW, IPT_UNUSED );/* tested at a466 */
		PORT_BIT(  0x0010, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT(  0x0020, IP_ACTIVE_LOW, IPT_UNUSED );/* tested before writing to 260040 */
		PORT_SERVICE( 0x0040, IP_ACTIVE_LOW );
		PORT_BIT(  0x0080, IP_ACTIVE_LOW, IPT_VBLANK );
		PORT_BIT(  0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* 260012 */
		PORT_BIT(  0xffff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* 260020 */
	    PORT_ANALOG( 0xff, 0, IPT_DIAL_V | IPF_PLAYER1, 50, 10, 0, 0 );
		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* 260022 */
	    PORT_ANALOG( 0xff, 0, IPT_DIAL | IPF_PLAYER2, 50, 10, 0, 0 );
		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* 260024 */
	    PORT_ANALOG( 0xff, 0, IPT_DIAL_V | IPF_PLAYER3 | IPF_REVERSE, 50, 10, 0, 0 );
		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Graphics definitions
	 *
	 *************************************/
	
	static GfxLayout pfmolayout = new GfxLayout
	(
		8,8,	/* 8*8 sprites */
		24576,	/* 8192 of them */
		4,		/* 4 bits per pixel */
		new int[] { 0+0x60000*8, 4+0x60000*8, 0, 4 },
		new int[] { 0, 1, 2, 3, 8, 9, 10, 11 },
		new int[] { 0*8, 2*8, 4*8, 6*8, 8*8, 10*8, 12*8, 14*8 },
		16*8	/* every sprite takes 16 consecutive bytes */
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, pfmolayout,  256, 32 ),		/* sprites  playfield */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	/*************************************
	 *
	 *	Machine driver
	 *
	 *************************************/
	
	static MachineDriver machine_driver_offtwall = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				ATARI_CLOCK_14MHz/2,
				readmem,writemem,null,null,
				ignore_interrupt,1
			),
			//JSA_III_CPU
                        new MachineCPU(														
                                CPU_M6502,											
                                ATARI_CLOCK_14MHz/8,								
                                atarijsa3_readmem,atarijsa3_writemem,null,null,			
                                null,0,
                                atarigen_6502_irq_gen,(int)(1000000000.0/((double)ATARI_CLOCK_14MHz/4/4/16/16/14))
                        )
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		init_machine,
	
		/* video hardware */
		42*8, 30*8, new rectangle( 0*8, 42*8-1, 0*8, 30*8-1 ),
		gfxdecodeinfo,
		2048, 2048,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
		null,
		offtwall_vh_start,
		offtwall_vh_stop,
		offtwall_vh_screenrefresh,
	
		/* sound hardware */
		//JSA_III_MONO_NO_SPEECH,
                0, 0, 0, 0,
                new MachineSound[]{
                    new MachineSound(
                            SOUND_YM2151, 									
                            atarijsa_ym2151_interface_mono
                    )
                },
	
		atarigen_nvram_handler
	);
	
	
	
	/*************************************
	 *
	 *	ROM decoding
	 *
	 *************************************/
	
	static void rom_decode()
	{
		int i;
	
		for (i = 0; i < memory_region_length(REGION_GFX1); i++)
			memory_region(REGION_GFX1).write(i, memory_region(REGION_GFX1).read(i) ^ 0xff);
	}
	
	
	
	/*************************************
	 *
	 *	ROM definition(s)
	 *
	 *************************************/
	
	static RomLoadPtr rom_offtwall = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );/* 4*64k for 68000 code */
		ROM_LOAD_EVEN( "otw2012.bin", 0x00000, 0x20000, 0xd08d81eb );
		ROM_LOAD_ODD ( "otw2013.bin", 0x00000, 0x20000, 0x61c2553d );
	
		ROM_REGION( 0x14000, REGION_CPU2 );/* 64k for 6502 code */
		ROM_LOAD( "otw1020.bin", 0x10000, 0x4000, 0x488112a5 );
		ROM_CONTINUE(            0x04000, 0xc000 );
	
		ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "otw1014.bin", 0x000000, 0x20000, 0x4d64507e );
		ROM_LOAD( "otw1016.bin", 0x020000, 0x20000, 0xf5454f3a );
		ROM_LOAD( "otw1018.bin", 0x040000, 0x20000, 0x17864231 );
		ROM_LOAD( "otw1015.bin", 0x060000, 0x20000, 0x271f7856 );
		ROM_LOAD( "otw1017.bin", 0x080000, 0x20000, 0x7f7f8012 );
		ROM_LOAD( "otw1019.bin", 0x0a0000, 0x20000, 0x9efe511b );
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_offtwalc = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );/* 4*64k for 68000 code */
		ROM_LOAD_EVEN( "090-2612.rom", 0x00000, 0x20000, 0xfc891a3f );
		ROM_LOAD_ODD ( "090-2613.rom", 0x00000, 0x20000, 0x805d79d4 );
	
		ROM_REGION( 0x14000, REGION_CPU2 );/* 64k for 6502 code */
		ROM_LOAD( "otw1020.bin", 0x10000, 0x4000, 0x488112a5 );
		ROM_CONTINUE(            0x04000, 0xc000 );
	
		ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "090-1614.rom", 0x000000, 0x20000, 0x307ed447 );
		ROM_LOAD( "090-1616.rom", 0x020000, 0x20000, 0xa5bd3d9b );
		ROM_LOAD( "090-1618.rom", 0x040000, 0x20000, 0xc7d9df5d );
		ROM_LOAD( "090-1615.rom", 0x060000, 0x20000, 0xac3642c7 );
		ROM_LOAD( "090-1617.rom", 0x080000, 0x20000, 0x15208a89 );
		ROM_LOAD( "090-1619.rom", 0x0a0000, 0x20000, 0x8a5d79b3 );
	ROM_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Driver initialization
	 *
	 *************************************/
	
	static char default_eeprom[] =
	{
		0x0001,0x011A,0x012A,0x0146,0x0100,0x0168,0x0300,0x011E,
		0x0700,0x0122,0x0600,0x0120,0x0400,0x0102,0x0300,0x017E,
		0x0200,0x0128,0x0104,0x0100,0x014E,0x0100,0x013E,0x0122,
		0x011A,0x012A,0x0146,0x0100,0x0168,0x0300,0x011E,0x0700,
		0x0122,0x0600,0x0120,0x0400,0x0102,0x0300,0x017E,0x0200,
		0x0128,0x0104,0x0100,0x014E,0x0100,0x013E,0x0122,0x1A00,
		0x0154,0x0125,0x01DC,0x0100,0x0192,0x0105,0x01DC,0x0181,
		0x012E,0x0106,0x0100,0x0105,0x0179,0x0132,0x0101,0x0100,
		0x01D3,0x0105,0x0116,0x0127,0x0134,0x0100,0x0104,0x01B0,
		0x0165,0x0102,0x1600,0x0000
	};
	
	
	public static InitDriverPtr init_offtwall = new InitDriverPtr() { public void handler() 
	{
		atarigen_eeprom_default = new UShortArray( default_eeprom );
	
		atarijsa_init(1, 2, 3, 0x0040);
	
		/* speed up the 6502 */
		atarigen_init_6502_speedup(1, 0x41dd, 0x41f5);
	
		/* install son-of-slapstic workarounds */
		spritecache_count = install_mem_read_handler(0, 0x3fde42, 0x3fde43, spritecache_count_r);
		bankswitch_base = install_mem_read_handler(0, 0x037ec2, 0x037f39, bankswitch_r);
		unknown_verify_base = install_mem_read_handler(0, 0x3fdf1e, 0x3fdf1f, unknown_verify_r);
	
		/* display messages */
		atarigen_show_sound_message();
	
		rom_decode();
	} };
	
	
	public static InitDriverPtr init_offtwalc = new InitDriverPtr() { public void handler() 
	{
		atarigen_eeprom_default = new UShortArray( default_eeprom );
	
		atarijsa_init(1, 2, 3, 0x0040);
	
		/* speed up the 6502 */
		atarigen_init_6502_speedup(1, 0x41dd, 0x41f5);
	
		/* install son-of-slapstic workarounds */
		spritecache_count = install_mem_read_handler(0, 0x3fde42, 0x3fde43, spritecache_count_r);
		bankswitch_base = install_mem_read_handler(0, 0x037eca, 0x037f43, bankswitch_r);
		unknown_verify_base = install_mem_read_handler(0, 0x3fdf24, 0x3fdf25, unknown_verify_r);
	
		/* display messages */
		atarigen_show_sound_message();
	
		rom_decode();
	} };
	
	
	/*************************************
	 *
	 *	Game driver(s)
	 *
	 *************************************/
	
	public static GameDriver driver_offtwall	   = new GameDriver("1991"	,"offtwall"	,"offtwall.java"	,rom_offtwall,null	,machine_driver_offtwall	,input_ports_offtwall	,init_offtwall	,ROT0	,	"Atari Games", "Off the Wall (2/3-player upright)" );
	public static GameDriver driver_offtwalc	   = new GameDriver("1991"	,"offtwalc"	,"offtwall.java"	,rom_offtwalc,driver_offtwall	,machine_driver_offtwall	,input_ports_offtwall	,init_offtwalc	,ROT0	,	"Atari Games", "Off the Wall (2-player cocktail)" );
}
