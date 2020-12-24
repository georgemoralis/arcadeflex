/***************************************************************************

Super Space invaders

driver by Richard Bush, Howie Cohen, Alex Pasadyn

***************************************************************************/
/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package gr.codebb.arcadeflex.v036.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.cpu.m68000.m68000H.MC68000_IRQ_5;
import static gr.codebb.arcadeflex.v036.cpu.m68000.m68000H.MC68000_IRQ_6;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_r;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_w;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;
import static gr.codebb.arcadeflex.v036.cpu.z80.z80H.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.memory.*;
import gr.codebb.arcadeflex.v036.mame.sndintrfH.MachineSound;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.SOUND_K007232;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.SOUND_YM2151;
import static gr.codebb.arcadeflex.v036.vidhrdw.snk68.*;
import static gr.codebb.arcadeflex.v036.sound.k007232.*;
import static gr.codebb.arcadeflex.v036.sound.k007232H.*;
import static gr.codebb.arcadeflex.v036.sound._2151intf.*;
import static gr.codebb.arcadeflex.v036.sound._2151intfH.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.aerofgt.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.SOUND_YM2610;
import static gr.codebb.arcadeflex.v036.sound._2610intf.*;
import static gr.codebb.arcadeflex.v036.sound._2610intfH.*;
import static gr.codebb.arcadeflex.v036.sndhrdw.rastan.*;
import static gr.codebb.arcadeflex.v037b7.mame.timer.*;
import static gr.codebb.arcadeflex.v037b7.mame.timerH.*;
import static gr.codebb.arcadeflex.v036.machine.cchip.*;
import static gr.codebb.arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.ssi.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.taitof2.*;
public class ssi
{
	
	public static WriteHandlerPtr bankswitch_w = new WriteHandlerPtr() { public void handler(int offset, int data){
	
		UBytePtr RAM = memory_region(REGION_CPU2);
	
		int banknum = ( data - 1 ) & 3;
	
		cpu_setbank( 2, new UBytePtr(RAM, 0x10000 + ( banknum * 0x4000 ) ));
	} };
	
	public static ReadHandlerPtr ssi_input_r = new ReadHandlerPtr() { public int handler(int offset)
	{
	    switch (offset)
	    {
	         case 0x00:
	              return readinputport(3); /* DSW A */
	
	         case 0x02:
	              return readinputport(4); /* DSW B */
	
	         case 0x04:
	              return readinputport(0); /* IN0 */
	
	         case 0x06:
	              return readinputport(1); /* IN1 */
	
	         case 0x0e:
	              return readinputport(2); /* IN2 */
	    }
	
	if (errorlog!=null) fprintf(errorlog,"CPU #0 PC %06x: warning - read unmapped memory address %06x\n",cpu_get_pc(),0x100000+offset);
	
		return 0xff;
	} };
	
	public static WriteHandlerPtr ssi_sound_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (offset == 0)
			rastan_sound_port_w.handler(0,(data >> 8) & 0xff);
		else if (offset == 2)
			rastan_sound_comm_w.handler(0,(data >> 8) & 0xff);
	} };
	
	public static ReadHandlerPtr ssi_sound_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (offset == 2)
			return ( ( rastan_sound_comm_r.handler(0) & 0xff ) << 8 );
		else return 0;
	} };
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10000f, ssi_input_r ),
		new MemoryReadAddress( 0x200000, 0x20ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x300000, 0x301fff, paletteram_word_r ),
		new MemoryReadAddress( 0x400000, 0x400003, ssi_sound_r ),
		new MemoryReadAddress( 0x600000, 0x60ffff, MRA_BANK3 ),
		new MemoryReadAddress( 0x800000, 0x80ffff, ssi_videoram_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10000f, watchdog_reset_w ),	/* ? */
		new MemoryWriteAddress( 0x200000, 0x20ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x300000, 0x301fff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ),
		new MemoryWriteAddress( 0x400000, 0x400003, ssi_sound_w ),
	//	new MemoryWriteAddress( 0x500000, 0x500001, MWA_NOP ),	/* ?? */
		new MemoryWriteAddress( 0x600000, 0x60ffff, MWA_BANK3 ), /* unused f2 video layers */
	//	new MemoryWriteAddress( 0x620000, 0x62000f, MWA_NOP ), /* unused f2 video control registers */
		new MemoryWriteAddress( 0x800000, 0x80ffff, ssi_videoram_w, videoram, videoram_size ), /* sprite ram */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new MemoryReadAddress( 0x4000, 0x7fff, MRA_BANK2 ),
		new MemoryReadAddress( 0xc000, 0xdfff, MRA_RAM ),
		new MemoryReadAddress( 0xe000, 0xe000, YM2610_status_port_0_A_r ),
		new MemoryReadAddress( 0xe001, 0xe001, YM2610_read_port_0_r ),
		new MemoryReadAddress( 0xe002, 0xe002, YM2610_status_port_0_B_r ),
		new MemoryReadAddress( 0xe200, 0xe200, MRA_NOP ),
		new MemoryReadAddress( 0xe201, 0xe201, r_rd_a001 ),
		new MemoryReadAddress( 0xea00, 0xea00, MRA_NOP ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0xc000, 0xdfff, MWA_RAM ),
		new MemoryWriteAddress( 0xe000, 0xe000, YM2610_control_port_0_A_w ),
		new MemoryWriteAddress( 0xe001, 0xe001, YM2610_data_port_0_A_w ),
		new MemoryWriteAddress( 0xe002, 0xe002, YM2610_control_port_0_B_w ),
		new MemoryWriteAddress( 0xe003, 0xe003, YM2610_data_port_0_B_w ),
		new MemoryWriteAddress( 0xe200, 0xe200, r_wr_a000 ),
		new MemoryWriteAddress( 0xe201, 0xe201, r_wr_a001 ),
		new MemoryWriteAddress( 0xe400, 0xe403, MWA_NOP ), /* pan */
		new MemoryWriteAddress( 0xee00, 0xee00, MWA_NOP ), /* ? */
		new MemoryWriteAddress( 0xf000, 0xf000, MWA_NOP ), /* ? */
		new MemoryWriteAddress( 0xf200, 0xf200, bankswitch_w ),	/* ?? */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	
	static InputPortPtr input_ports_ssi = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1);
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2);
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
	
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );
		PORT_DIPSETTING(    0x03, "Normal" );
		PORT_DIPSETTING(    0x01, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x0c, 0x0c, "Shields" );
		PORT_DIPSETTING(    0x00, "None");
		PORT_DIPSETTING(    0x0c, "1");
		PORT_DIPSETTING(    0x04, "2");
		PORT_DIPSETTING(    0x08, "3");
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "2");
		PORT_DIPSETTING(    0x10, "3");
		PORT_DIPNAME( 0xa0, 0xa0, "2 Players Mode" );
		PORT_DIPSETTING(    0xa0, "Simultaneous");
		PORT_DIPSETTING(    0x80, "Alternate, Single");
		PORT_DIPSETTING(    0x00, "Alternate, Dual");
		PORT_DIPSETTING(    0x20, "Not Allowed");
		PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_majest12 = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1);
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2);
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_2C") );
	
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );
		PORT_DIPSETTING(    0x03, "Normal" );
		PORT_DIPSETTING(    0x01, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x0c, 0x0c, "Shields" );
		PORT_DIPSETTING(    0x00, "None");
		PORT_DIPSETTING(    0x0c, "1");
		PORT_DIPSETTING(    0x04, "2");
		PORT_DIPSETTING(    0x08, "3");
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "2");
		PORT_DIPSETTING(    0x10, "3");
		PORT_DIPNAME( 0xa0, 0xa0, "2 Players Mode" );
		PORT_DIPSETTING(    0xa0, "Simultaneous");
		PORT_DIPSETTING(    0x80, "Alternate, Single Controls");
		PORT_DIPSETTING(    0x00, "Alternate, Dual Controls");
		PORT_DIPSETTING(    0x20, "Not Allowed");
		PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout tilelayout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		8192,	/* 8192 sprites */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 1*4, 0*4, 3*4, 2*4, 5*4, 4*4, 7*4, 6*4, 9*4, 8*4, 11*4, 10*4, 13*4, 12*4, 15*4, 14*4 },
		new int[] { 0*64, 1*64, 2*64, 3*64, 4*64, 5*64, 6*64, 7*64, 8*64, 9*64, 10*64, 11*64, 12*64, 13*64, 14*64, 15*64 },
		128*8	/* every sprite takes 32 consecutive bytes */
	);
	
	static GfxDecodeInfo ssi_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x000000, tilelayout, 0, 256 ),         /* sprites  playfield */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static YM2610interface ym2610_interface = new YM2610interface
	(
		1,	/* 1 chip */
		8000000,	/* 8 MHz ?????? */
		new int[]{ 30 },
		new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
		new WriteYmHandlerPtr[]{ rastan_irq_handler },
		new int[]{ REGION_SOUND1 },
		new int[]{ REGION_SOUND1 },
		new int[]{ YM3012_VOL(60,MIXER_PAN_LEFT,60,MIXER_PAN_RIGHT) }
	
                );
	
	
	
	static MachineDriver machine_driver_ssi = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				12000000,	/* 12 MHz ? */
				readmem,writemem,null,null,
				m68_level5_irq,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				4000000,	/* 4 MHz ??? */
				sound_readmem, sound_writemem,null,null,
				ignore_interrupt,0	/* IRQs are triggered by the YM2610 */
			)
		},
		60, 1800,	/* frames per second, vblank duration hand tuned to avoid flicker */
		10,
		null,
	
		/* video hardware */
		40*8, 28*8, new rectangle( 0*8, 40*8-1, 0*8, 28*8-1 ),
	
		ssi_gfxdecodeinfo,
		4096, 4096,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_AFTER_VBLANK,
		null,
		ssi_vh_start,
		ssi_vh_stop,
		ssi_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,/*TODO*///SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2610,
				ym2610_interface
			)
		}
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_ssi = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );    /* 512k for 68000 code */
		ROM_LOAD_EVEN( "ssi_15-1.rom", 0x00000, 0x40000, 0xce9308a6 );
		ROM_LOAD_ODD ( "ssi_16-1.rom", 0x00000, 0x40000, 0x470a483a );
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "ssi_09.rom",   0x00000, 0x04000, 0x88d7f65c );
		ROM_CONTINUE(             0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ssi_m01.rom",  0x00000, 0x100000, 0xa1b4f486 );
	
		ROM_REGION( 0x20000, REGION_SOUND1 );/* 2610 samples */
		ROM_LOAD( "ssi_m02.rom",  0x00000, 0x20000, 0x3cb0b907 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_majest12 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );    /* 512k for 68000 code */
		ROM_LOAD_EVEN( "c64-07.bin", 0x00000, 0x20000, 0xf29ed5c9 );
		ROM_LOAD_EVEN( "c64-06.bin", 0x40000, 0x20000, 0x18dc71ac );
		ROM_LOAD_ODD ( "c64-08.bin", 0x00000, 0x20000, 0xddfd33d5 );
		ROM_LOAD_ODD ( "c64-05.bin", 0x40000, 0x20000, 0xb61866c0 );
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "ssi_09.rom",   0x00000, 0x04000, 0x88d7f65c );
		ROM_CONTINUE(             0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ssi_m01.rom",  0x00000, 0x100000, 0xa1b4f486 );
	
		ROM_REGION( 0x20000, REGION_SOUND1 );/* 2610 samples */
		ROM_LOAD( "ssi_m02.rom",  0x00000, 0x20000, 0x3cb0b907 );
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_ssi	   = new GameDriver("1990"	,"ssi"	,"ssi.java"	,rom_ssi,null	,machine_driver_ssi	,input_ports_ssi	,null	,ROT270	,	"Taito Corporation Japan", "Super Space Invaders '91 (World)", GAME_NO_COCKTAIL );
	public static GameDriver driver_majest12	   = new GameDriver("1990"	,"majest12"	,"ssi.java"	,rom_majest12,driver_ssi	,machine_driver_ssi	,input_ports_majest12	,null	,ROT270	,	"Taito Corporation", "Majestic Twelve - The Space Invaders Part IV (Japan)", GAME_NO_COCKTAIL );
}
