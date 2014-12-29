/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.commonH.*;
import static mame.inputport.*;
import static mame.drawgfxH.*;
import static vidhrdw.generic.*;
import static mame.cpuintrf.*;
import static mame.cpuintrfH.*;
import static mame.inputportH.*;
import static mame.mame.*;
import static arcadeflex.libc_old.*;
import static arcadeflex.libc.*;
import static mame.sndintrf.*;
import static cpu.m6809.m6809H.*;
import static cpu.z80.z80H.*;
import static mame.common.*;
import static mame.commonH.*;
import static mame.palette.*;
import static mame.memory.*;
import static vidhrdw.airbustr.*;
import static mame.tilemapC.*;
import static mame.tilemapH.*;
import static arcadeflex.ptrlib.*;
import mame.sndintrfH.MachineSound;
import static mame.sndintrfH.SOUND_YM2203;
import static sound._2203intf.*;
import static sound._2203intfH.*;

public class airbustr
{
	
	static UBytePtr devram=new UBytePtr();
        static UBytePtr sharedram=new UBytePtr();
        static int soundlatch_status, soundlatch2_status;
	
	
	/* Debug stuff (bound to go away sometime) */
	static int u1, u2, u3, u4;
        
        public static InitMachinePtr airbustr_init_machine = new InitMachinePtr() { public void handler() 
        {
		soundlatch_status = soundlatch2_status = 0;
		bankswitch_w.handler(0,2);
		bankswitch2_w.handler(0,2);
		sound_bankswitch_w.handler(0,2);
	} };
	
	
	/*
	**
	**				Main cpu data
	**
	*/
	
	/*	Runs in IM 2	fd-fe	address of int: 0x38
						ff-100	address of int: 0x16	*/
	static int addr_air = 0xff;
	public static InterruptPtr airbustr_interrupt = new InterruptPtr() { public int handler() 
	{
		addr_air = addr_air ^ 0x02;
	
		return addr_air;
	} };
	
	
	public static ReadHandlerPtr sharedram_r = new ReadHandlerPtr() { public int handler(int offs){ return sharedram.read(offs); } };
	public static WriteHandlerPtr sharedram_w = new WriteHandlerPtr() { public void handler(int offs, int data){ sharedram.write(offs,data); } };
	
	
	/* There's an MCU here, possibly */
	public static ReadHandlerPtr devram_r = new ReadHandlerPtr() { public int handler(int offs)
	{
		switch (offs)
		{
			/* Reading efe0 probably resets a watchdog mechanism
			   that would reset the main cpu. We avoid this and patch
			   the rom instead (main cpu has to be reset once at startup) */
			case 0xfe0:
				return 0/*watchdog_reset_r(0)*/;
				//break;
	
			/* Reading a word at eff2 probably yelds the product
	   		   of the words written to eff0 and eff2 */
			case 0xff2:
			case 0xff3:
			{
				int	x = (devram.read(0xff0) + devram.read(0xff1) * 256) *
						(devram.read(0xff2) + devram.read(0xff3) * 256);
				if (offs == 0xff2)	return (x & 0x00FF) >> 0;
				else				return (x & 0xFF00) >> 8;
			}	//break;
	
			/* Reading eff4, F0 times must yield at most 80-1 consecutive
			   equal values */
			case 0xff4:
			{
				return rand();
			}	//break;
	
			default:	{ return devram.read(offs); /*break;*/}
		}
	
	} };
	public static WriteHandlerPtr devram_w = new WriteHandlerPtr() { public void handler(int offs, int data){	devram.write(offs,data); } };
	
	
	public static WriteHandlerPtr bankswitch_w = new WriteHandlerPtr() { public void handler(int offs, int data)
	{
                UBytePtr RAM = memory_region(REGION_CPU1);
	
		if ((data & 7) <  3)	RAM = new UBytePtr(RAM,0x4000 * (data & 7));
		else					RAM = new UBytePtr(RAM,0x10000 + 0x4000 * ((data & 7)-3));
	
		cpu_setbank(1,RAM);
	//	if (errorlog && (data > 7))	fprintf(errorlog, "CPU #0 - suspicious bank: %d ! - PC = %04X\n", data, cpu_get_pc());
	
		u1 = data & 0xf8;
	} };
	
	/* Memory */
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0xbfff, MRA_BANK1 ),
		new MemoryReadAddress( 0xc000, 0xcfff, MRA_RAM ),
		new MemoryReadAddress( 0xd000, 0xdfff, MRA_RAM ),
		new MemoryReadAddress( 0xe000, 0xefff, devram_r ),
		new MemoryReadAddress( 0xf000, 0xffff, sharedram_r ),
		new MemoryReadAddress( -1 )
	};
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),	// writing at 0 should cause a reset
		new MemoryWriteAddress( 0xc000, 0xcfff, MWA_RAM, spriteram ),			// RAM 0/1
		new MemoryWriteAddress( 0xd000, 0xdfff, MWA_RAM ),						// RAM 2
		new MemoryWriteAddress( 0xe000, 0xefff, devram_w, devram ),				// RAM 3
		new MemoryWriteAddress( 0xf000, 0xffff, sharedram_w, sharedram ),
		new MemoryWriteAddress( -1 )
	};
	
	/* Ports */
	
	public static WriteHandlerPtr cause_nmi = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		cpu_cause_interrupt(1,Z80_NMI_INT);	// cause a nmi to sub cpu
	} };
	
	static IOReadPort readport[] =
	{
		new IOReadPort( -1 )
	};
	static IOWritePort writeport[] =
	{
		new IOWritePort( 0x00, 0x00, bankswitch_w ),
	//	new IOWritePort( 0x01, 0x01, IOWP_NOP ),	// ?? only 2 (see 378b)
		new IOWritePort( 0x02, 0x02, cause_nmi ),	// always 0. Cause a nmi to sub cpu
		new IOWritePort( -1 )
	};
	
	
	
	
	
	
	
	
	
	
	/*
	**
	**				Sub cpu data
	**
	**
	*/
	
	/*	Runs in IM 2	fd-fe	address of int: 0x36e	(same as 0x38)
						ff-100	address of int: 0x4b0	(only writes to port 38h)	*/
	static int addr_air2 = 0xfd;
	public static InterruptPtr airbustr_interrupt2 = new InterruptPtr() { public int handler() 
	{
		addr_air2 ^= 0x02;
	
		return addr_air2;
	} };
	
	
	public static WriteHandlerPtr bankswitch2_w = new WriteHandlerPtr() { public void handler(int offs, int data)
	{
	UBytePtr RAM = memory_region(REGION_CPU2);
	
		if ((data & 7) <  3)	RAM = new UBytePtr(RAM,0x4000 * (data & 7));
		else					RAM = new UBytePtr(RAM,0x10000 + 0x4000 * ((data & 7)-3));
	
		cpu_setbank(2,RAM);
	//	if (errorlog && (data > 7))	fprintf(errorlog, "CPU #1 - suspicious bank: %d ! - PC = %04X\n", data, cpu_get_pc());
	
		flipscreen = data & 0x10;	// probably..
		tilemap_set_flip(ALL_TILEMAPS,flipscreen!=0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
	
		u2 = data & 0xf8;
	} };
	
	
	public static WriteHandlerPtr airbustr_paletteram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	int r,g,b;
	
		/*	! byte 1 ! ! byte 0 !	*/
		/*	xGGG GGRR 	RRRB BBBB	*/
		/*	x432 1043 	2104 3210	*/
	
		paletteram.write(offset,data);
		data = (paletteram.read(offset | 1) << 8) | paletteram.read(offset & ~1);
	
		g = (data >> 10) & 0x1f;
		r = (data >>  5) & 0x1f;
		b = (data >>  0) & 0x1f;
	
		palette_change_color(offset/2,	(r * 0xff) / 0x1f,
										(g * 0xff) / 0x1f,
										(b * 0xff) / 0x1f );
	} };
	
	
	/* Memory */
	
	static MemoryReadAddress readmem2[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0xbfff, MRA_BANK2 ),
		new MemoryReadAddress( 0xc000, 0xcfff, MRA_RAM ),
		new MemoryReadAddress( 0xd000, 0xd5ff, paletteram_r ),
		new MemoryReadAddress( 0xd600, 0xdfff, MRA_RAM ),
		new MemoryReadAddress( 0xe000, 0xefff, MRA_RAM ),
		new MemoryReadAddress( 0xf000, 0xffff, sharedram_r ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress writemem2[] =
	{
		new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new MemoryWriteAddress( 0xc000, 0xc7ff, airbustr_fgram_w, airbustr_fgram ),
		new MemoryWriteAddress( 0xc800, 0xcfff, airbustr_bgram_w, airbustr_bgram ),
		new MemoryWriteAddress( 0xd000, 0xd5ff, airbustr_paletteram_w, paletteram ),
		new MemoryWriteAddress( 0xd600, 0xdfff, MWA_RAM ),
		new MemoryWriteAddress( 0xe000, 0xefff, MWA_RAM ),
		new MemoryWriteAddress( 0xf000, 0xffff, sharedram_w ),
		new MemoryWriteAddress( -1 )
	};
	
	
	/* Ports */
	
	/*
	   Sub cpu and Sound cpu communicate bidirectionally:
	
		   Sub   cpu writes to soundlatch,  reads from soundlatch2
		   Sound cpu writes to soundlatch2, reads from soundlatch
	
	   Each latch raises a flag when it's been written.
	   The flag is cleared when the latch is read.
	
	Code at 505: waits for bit 1 to go low, writes command, waits for bit
	0 to go low, reads back value. Code at 3b2 waits bit 2 to go high
	(called during int fd)
	
	*/
	
	
	public static ReadHandlerPtr soundcommand_status_r = new ReadHandlerPtr() { public int handler(int offset)
	{
	/* bits: 2 <. ?	1 <. soundlatch full	0 <. soundlatch2 empty */
		return 4 + soundlatch_status * 2 + (1-soundlatch2_status);
	} };
	
	
	public static ReadHandlerPtr soundcommand2_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		soundlatch2_status = 0;				// soundlatch2 has been read
		return soundlatch2_r.handler(0);
	} };
	
	
	public static WriteHandlerPtr soundcommand_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		soundlatch_w.handler(0,data);
		soundlatch_status = 1;				// soundlatch has been written
		cpu_cause_interrupt(2,Z80_NMI_INT);	// cause a nmi to sub cpu
	} };
	
	
	public static WriteHandlerPtr port_38_w = new WriteHandlerPtr() { public void handler(int offset, int data){	u4 = data; } }; // for debug
	
	
	static IOReadPort readport2[] =
	{
		new IOReadPort( 0x02, 0x02, soundcommand2_r ),		// from sound cpu
		new IOReadPort( 0x0e, 0x0e, soundcommand_status_r ),	// status of the latches ?
		new IOReadPort( 0x20, 0x20, input_port_0_r ),			// player 1
		new IOReadPort( 0x22, 0x22, input_port_1_r ),			// player 2
		new IOReadPort( 0x24, 0x24, input_port_2_r ),			// service
		new IOReadPort( -1 )
	};
	
	static IOWritePort writeport2[] =
	{
		new IOWritePort( 0x00, 0x00, bankswitch2_w ),			// bits 2-0 bank; bit 4 (on if dsw1-1 active)? ; bit 5?
		new IOWritePort( 0x02, 0x02, soundcommand_w ),			// to sound cpu
		new IOWritePort( 0x04, 0x0c, airbustr_scrollregs_w ),	// Scroll values
	//	new IOWritePort( 0x28, 0x28, port_38_w ),				// ??
	//	new IOWritePort( 0x38, 0x38, IOWP_NOP ),				// ?? Followed by EI. Value isn't important
		new IOWritePort( -1 )
	};
	
	
	
	
	
	
	
	
	
	
	/*
	**
	** 				Sound cpu data
	**
	*/
	
	public static WriteHandlerPtr sound_bankswitch_w = new WriteHandlerPtr() { public void handler(int offs, int data)
	{
	UBytePtr RAM = memory_region(REGION_CPU3);
	
		if ((data & 7) <  3)	RAM = new UBytePtr(RAM,0x4000 * (data & 7));
		else					RAM = new UBytePtr(RAM,0x10000 + 0x4000 * ((data & 7)-3));
	
		cpu_setbank(3,RAM);
	//	if (errorlog && (data > 7))	fprintf(errorlog, "CPU #2 - suspicious bank: %d ! - PC = %04X\n", data, cpu_get_pc());
	
		u3 = data & 0xf8;
	} };
	
	
	/* Memory */
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0xbfff, MRA_BANK3 ),
		new MemoryReadAddress( 0xc000, 0xdfff, MRA_RAM ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new MemoryWriteAddress( 0xc000, 0xdfff, MWA_RAM ),
		new MemoryWriteAddress( -1 )
	};
	
	
	/* Ports */
	
	public static ReadHandlerPtr soundcommand_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		soundlatch_status = 0;		// soundlatch has been read
		return soundlatch_r.handler(0);
	} };
	
	
	public static WriteHandlerPtr soundcommand2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		soundlatch2_status = 1;		// soundlatch2 has been written
		soundlatch2_w.handler(0,data);
	} };
	
	
	static IOReadPort sound_readport[] =
	{
		new IOReadPort( 0x02, 0x02, YM2203_status_port_0_r ),
		new IOReadPort( 0x03, 0x03, YM2203_read_port_0_r ),
/*TODO*///		new IOReadPort( 0x04, 0x04, OKIM6295_status_0_r ),
		new IOReadPort( 0x06, 0x06, soundcommand_r ),			// read command from sub cpu
		new IOReadPort( -1 )
	};
	
	static IOWritePort sound_writeport[] =
	{
		new IOWritePort( 0x00, 0x00, sound_bankswitch_w ),
		new IOWritePort( 0x02, 0x02, YM2203_control_port_0_w ),
		new IOWritePort( 0x03, 0x03, YM2203_write_port_0_w ),
/*TODO*///		new IOWritePort( 0x04, 0x04, OKIM6295_data_0_w ),
		new IOWritePort( 0x06, 0x06, soundcommand2_w ),		// write command result to sub cpu
		new IOWritePort( -1 )
	};
	
	
	
	
	/*	Input Ports:
		[0] Player 1		[1] Player 2
		[2] Service
		[3] Dsw 1			[4] Dsw 2	*/
	
	static InputPortPtr input_ports_airbustr = new InputPortPtr(){ public void handler() { 
	
		PORT_START(); 	// IN0 - Player 1
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	// IN1 - Player 2
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	// IN2 - Service
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );// used
	
		PORT_START(); 	// IN3 - DSW-1
		PORT_DIPNAME( 0x01, 0x01, "Unknown 1-0" );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );	// if active, bit 4 of cpu2 bank is on ..
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );			// is this a flip screen?
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );			// it changes the scroll offsets
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, "Unknown 1-3" );	//	routine 56d:	11 21 12 16 (bit 3 active)
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );		//					11 21 13 14 (bit 3 not active)
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );	//	routine 546:	11 12 21 23
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x40, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_2C") );
	
		PORT_START(); 	// IN4 - DSW-2
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );
		PORT_DIPSETTING(    0x03, "Normal" );
		PORT_DIPSETTING(    0x01, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x04, 0x04, "Unknown 2-2" );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x30, "3" );
		PORT_DIPSETTING(    0x20, "4" );
		PORT_DIPSETTING(    0x10, "5" );
		PORT_DIPSETTING(    0x00, "7" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "Unknown 2-7" );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
	INPUT_PORTS_END(); }}; 
	
	
	
	/*
	**
	** 				Gfx data
	**
	*/
	
	
	/* displacement in bits to lower part of gfx */ 
	static GfxLayout tilelayout = new GfxLayout
	(
		16,16,
		0x080000*8/(16*16*4),
		4,
		new int[] {0, 1, 2, 3},
		new int[] {0*4,1*4,2*4,3*4,4*4,5*4,6*4,7*4, 
		 0*4+32*8,1*4+32*8,2*4+32*8,3*4+32*8,4*4+32*8,5*4+32*8,6*4+32*8,7*4+32*8},
		new int[] {0*32,1*32,2*32,3*32,4*32,5*32,6*32,7*32,
		 0*32+8*8*4*2,1*32+8*8*4*2,2*32+8*8*4*2,3*32+8*8*4*2,4*32+8*8*4*2,5*32+8*8*4*2,6*32+8*8*4*2,7*32+8*8*4*2}, 
		16*16*4
	);
         static GfxLayout spritelayout = new GfxLayout
	(
		16,16,
		0x100000*8/(16*16*4),
		4,
		new int[] {0, 1, 2, 3},
		new int[] {0*4,1*4,2*4,3*4,4*4,5*4,6*4,7*4, 
		 0*4+32*8,1*4+32*8,2*4+32*8,3*4+32*8,4*4+32*8,5*4+32*8,6*4+32*8,7*4+32*8},
		new int[] {0*32,1*32,2*32,3*32,4*32,5*32,6*32,7*32,
		 0*32+8*8*4*2,1*32+8*8*4*2,2*32+8*8*4*2,3*32+8*8*4*2,4*32+8*8*4*2,5*32+8*8*4*2,6*32+8*8*4*2,7*32+8*8*4*2}, 
		16*16*4
	);          
                 
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, tilelayout,   256*0, (256*2) / 16 ), // [0] Layers
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout, 256*2, (256*1) / 16 ), // [1] Sprites
		new GfxDecodeInfo( -1 )
	};
	
	
	static YM2203interface ym2203_interface = new YM2203interface(
            1, 
            3000000,					/* ?? */
            new int[]{YM2203_VOL(0xff,0xff)},/* gain,volume */
            new ReadHandlerPtr[]{input_port_3_r},/* DSW-1 connected to port A */
            new ReadHandlerPtr[]{input_port_4_r},/* DSW-2 connected to port B */
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );

	
/*	static struct OKIM6295interface okim6295_interface =
	{
		1,
		{ 18000 },		/* ?? */
/*		{ REGION_SOUND1 },
		{ 50 }
	};*/
	
	
	static MachineDriver machine_driver_airbustr = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				6000000,	/* ?? */
				readmem,writemem,readport,writeport,
				airbustr_interrupt, 2	/* nmi caused by sub cpu?, ? */
			),
			new MachineCPU(
				CPU_Z80,
				6000000,	/* ?? */
				readmem2,writemem2,readport2,writeport2,
				airbustr_interrupt2, 2	/* nmi caused by main cpu, ? */
			),
			new MachineCPU(
				CPU_Z80,	/* Sound CPU, reads DSWs. Hence it can't be disabled */
				6000000,	/* ?? */
				sound_readmem,sound_writemem,sound_readport,sound_writeport,
				interrupt,1	/* nmi are caused by sub cpu writing a sound command */
			),
		},
		60,DEFAULT_60HZ_VBLANK_DURATION,
		100,	/* Palette RAM is filled by sub cpu with data supplied by main cpu */
				/* Maybe an high value is safer in order to avoid glitches */
		airbustr_init_machine,
	
		/* video hardware */
		256, 256, new rectangle( 0, 256-1, 0+16, 256-16-1 ),
		gfxdecodeinfo,
		256 * 3, 256 * 3,
		null,
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		airbustr_vh_start,
		null,
		airbustr_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			)/*,
			new MachineSound(
				SOUND_OKIM6295,
				okim6295_interface
			)*/
		}
                
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_airbustr = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x24000, REGION_CPU1 );
		ROM_LOAD( "pr-14j.bin", 0x00000, 0x0c000, 0x6b9805bd );
		ROM_CONTINUE(           0x10000, 0x14000 );
	
		ROM_REGION( 0x24000, REGION_CPU2 );
		ROM_LOAD( "pr-11j.bin", 0x00000, 0x0c000, 0x85464124 );
		ROM_CONTINUE(           0x10000, 0x14000 );
	
		ROM_REGION( 0x24000, REGION_CPU3 );
		ROM_LOAD( "pr-21.bin",  0x00000, 0x0c000, 0x6e0a5df0 );
		ROM_CONTINUE(           0x10000, 0x14000 );
	
		ROM_REGION( 0x080000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "pr-000.bin", 0x000000, 0x80000, 0x8ca68f0d );// scrolling layers
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "pr-001.bin", 0x000000, 0x80000, 0x7e6cb377 );// sprites
		ROM_LOAD( "pr-02.bin",  0x080000, 0x10000, 0x6bbd5e46 );
	
		ROM_REGION( 0x40000, REGION_SOUND1 );/* OKI-M6295 samples */
		ROM_LOAD( "pr-200.bin", 0x00000, 0x40000, 0xa4dd3390 );
	ROM_END(); }}; 
	
	
	
	public static InitDriverPtr init_airbustr = new InitDriverPtr() { public void handler() 
	{
	int i;
	UBytePtr RAM;
	
		/* One gfx rom seems to have scrambled data (bad read?): */
		/* let's swap even and odd nibbles */
		RAM = new UBytePtr(memory_region(REGION_GFX1) , 0x000000);
		for (i = 0; i < 0x80000; i ++)
		{
			RAM.write(i,((RAM.read(i) & 0xF0)>>4) + ((RAM.read(i) & 0x0F)<<4));
		}
	
		RAM = memory_region(REGION_CPU1);
		RAM.write(0x37f4,0x00);		RAM.write(0x37f5,0x00);	// startup check. We need a reset
													// so I patch a busy loop with jp 0
	
		RAM = memory_region(REGION_CPU2);
		RAM.write(0x0258,0x53); // include EI in the busy loop.
							// It's an hack to repair nested nmi troubles
	} };
	
	
	
	public static GameDriver driver_airbustr	   = new GameDriver("1990"	,"airbustr"	,"airbustr.java"	,rom_airbustr,null	,machine_driver_airbustr	,input_ports_airbustr	,init_airbustr	,ROT0	,	"Kaneko (Namco license)", "Air Buster (Japan)" );
}
