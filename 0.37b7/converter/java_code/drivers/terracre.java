/******************************************************************
Terra Cresta (preliminary)
Nichibutsu 1985
68000 + Z80

driver by Carlos A. Lozano

TODO: I'm playing samples with a DAC, but they could be ADPCM

Carlos A. Lozano (calb@gsyc.inf.uc3m.es)

MEMORY MAP
0x000000 - 0x01ffff   ROM
0x020000 - 0x02006f   VRAM (Sprites)???
0x020070 - 0x021fff   RAM
0x022000 - 0x022fff   VRAM (Background)???
0x024000 - 0x24000f   Input Ports
0x026000 - 0x26000f   Output Ports
0x028000 - 0x287fff   VRAM (Tiles)

VRAM(Background)
0x22000 - 32 bytes (16 tiles)
0x22040 - 32 bytes (16 tiles)
0x22080 - 32 bytes (16 tiles)
0x220c0 - 32 bytes (16 tiles)
0x22100 - 32 bytes (16 tiles)
...
0x22fc0 - 32 bytes (16 tiles)

VRAM(Tiles)
0x28000-0x287ff (1024 tiles 8x8 tiles, 2 bytes every tile)

VRAM(Sprites)
0x20000-0x201ff

******************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class terracre
{
	
	UBytePtr  terrac_ram;
	
	//void terracre_vh_screenrefresh(struct osd_bitmap *bitmap);
	
	extern UBytePtr terrac_videoram;
	extern size_t terrac_videoram_size;
	extern unsigned char terrac_scrolly[];
	
	
	public static WriteHandlerPtr terracre_misc_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch (offset)
		{
	//		case 0: /* ??? */
	//			break;
			case 2: /* Scroll Y */
				COMBINE_WORD_MEM(terrac_scrolly,data);
				return;
				break;
	//		case 4: /* ??? */
	//			break;
	//		case 0xa: /* ??? */
	//			break;
			case 0xc: /* sound command */
				soundlatch_w.handler(offset,((data & 0x7f) << 1) | 1);
				return;
				break;
	//		case 0xe: /* ??? */
	//			break;
		}
	
		logerror("OUTPUT [%x] <- %08x\n", offset,data );
	} };
	
	public static ReadHandlerPtr terracre_ports_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (offset)
		{
			case 0: /* Player controls */
				return readinputport(0);
	
			case 2: /* Dipswitch 0xf000*/
				return readinputport(1);
	
			case 4: /* Start buttons & Dipswitch */
				return readinputport(2) << 8;
	
			case 6: /* Dipswitch???? */
				return (readinputport(4) << 8) | readinputport(3);
		}
		return 0xffff;
	} };
	
	
	public static ReadHandlerPtr soundlatch_clear_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		soundlatch_clear_w(0,0);
		return 0;
	} };
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x01ffff, MRA_ROM ),
		new MemoryReadAddress( 0x020000, 0x0201ff, MRA_BANK1 ),
		new MemoryReadAddress( 0x020200, 0x021fff, MRA_BANK2 ),
		new MemoryReadAddress( 0x022000, 0x022fff, terrac_videoram2_r ),
		new MemoryReadAddress( 0x023000, 0x023fff, MRA_BANK3 ),
		new MemoryReadAddress( 0x024000, 0x024007, terracre_ports_r ),
		new MemoryReadAddress( 0x028000, 0x0287ff, MRA_BANK4 ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x01ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x020000, 0x0201ff, MWA_BANK1, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x020200, 0x021fff, MWA_BANK2, terrac_ram ),
		new MemoryWriteAddress( 0x022000, 0x022fff, terrac_videoram2_w, terrac_videoram, terrac_videoram_size ),
		new MemoryWriteAddress( 0x023000, 0x023fff, MWA_BANK3 ),
		new MemoryWriteAddress( 0x026000, 0x02600f, terracre_misc_w ),
		new MemoryWriteAddress( 0x028000, 0x0287ff, MWA_BANK4, videoram, videoram_size ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0xbfff, MRA_ROM ),
		new MemoryReadAddress( 0xc000, 0xcfff, MRA_RAM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new MemoryWriteAddress( 0xc000, 0xcfff, MWA_RAM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	static IOReadPort sound_readport[] =
	{
		new IOReadPort( 0x04, 0x04, soundlatch_clear_r ),
		new IOReadPort( 0x06, 0x06, soundlatch_r ),
		new IOReadPort( -1 )	/* end of table */
	};
	
	static IOWritePort sound_writeport_3526[] =
	{
		new IOWritePort( 0x00, 0x00, YM3526_control_port_0_w ),
		new IOWritePort( 0x01, 0x01, YM3526_write_port_0_w ),
		new IOWritePort( 0x02, 0x02, DAC_0_signed_data_w ),
		new IOWritePort( 0x03, 0x03, DAC_1_signed_data_w ),
		new IOWritePort( -1 )	/* end of table */
	};
	
	static IOWritePort sound_writeport_2203[] =
	{
		new IOWritePort( 0x00, 0x00, YM2203_control_port_0_w ),
		new IOWritePort( 0x01, 0x01, YM2203_write_port_0_w ),
		new IOWritePort( 0x02, 0x02, DAC_0_signed_data_w ),
		new IOWritePort( 0x03, 0x03, DAC_1_signed_data_w ),
		new IOWritePort( -1 )	/* end of table */
	};
	
	
	static InputPortPtr input_ports_terracre = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* Player 1 controls */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 	/* Player 2 controls */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL  );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 	/* Coin, Start, Test, Dipswitch */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN3 );	PORT_SERVICE( 0x20, IP_ACTIVE_LOW );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 	/* Dipswitch */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "3" );	PORT_DIPSETTING(    0x02, "4" );	PORT_DIPSETTING(    0x01, "5" );	PORT_DIPSETTING(    0x00, "6" );	PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x0c, "20000 60000" );	PORT_DIPSETTING(    0x08, "30000 70000" );	PORT_DIPSETTING(    0x04, "40000 80000" );	PORT_DIPSETTING(    0x00, "50000 90000" );	PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* Dipswitch */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x10, "Easy" );	PORT_DIPSETTING(    0x00, "Hard" );	PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BITX(    0x40, 0x40, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		256,	/* 256 characters */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 4, 0, 12, 8, 20, 16, 28, 24},
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8	/* every char takes 32 consecutive bytes */
	);
	
	static GfxLayout backlayout = new GfxLayout
	(
		16,16,	/* 16*16 chars */
		512,	/* 512 characters */
		4,		/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 }, /* plane offset */
		new int[] { 4, 0, 12, 8, 20, 16, 28, 24,
			32+4, 32+0, 32+12, 32+8, 32+20, 32+16, 32+28, 32+24, },
		new int[] { 0*64, 1*64, 2*64, 3*64, 4*64, 5*64, 6*64, 7*64,
			8*64, 9*64, 10*64, 11*64, 12*64, 13*64, 14*64, 15*64 },
		128*8   /* every char takes 128 consecutive bytes  */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 characters */
		512,	/* 512 sprites */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 4, 0, 4+0x8000*8, 0+0x8000*8, 12, 8, 12+0x8000*8, 8+0x8000*8,
			20, 16, 20+0x8000*8, 16+0x8000*8, 28, 24, 28+0x8000*8, 24+0x8000*8 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32,
	          8*32, 9*32, 10*32, 11*32, 12*32, 13*32, 14*32, 15*32 },
		64*8	/* every char takes 64 consecutive bytes  */
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,            0,   1 ),
		new GfxDecodeInfo( REGION_GFX2, 0, backlayout,         1*16,  16 ),
		new GfxDecodeInfo( REGION_GFX3, 0, spritelayout, 1*16+16*16, 256 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	
	static YM3526interface ym3526_interface = new YM3526interface
	(
		1,			/* 1 chip (no more supported) */
		4000000,	/* 4 MHz ? (hand tuned) */
		new int[] { 255 }		/* (not supported) */
	);
	
	static YM2203interface ym2203_interface = new YM2203interface
	(
		1,			/* 1 chip */
		4000000,	/* 4 MHz ???? */
		new int[] { YM2203_VOL(40,20), YM2203_VOL(40,20) },
		new ReadHandlerPtr[] { 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 }
	);
	
	static DACinterface dac_interface = new DACinterface
	(
		2,	/* 2 channels */
		new int[] { 50, 50 }
	);
	
	
	static MachineDriver machine_driver_ym3526 = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				8000000, /* 8 MHz?? */
				readmem,writemem,null,null,
				m68_level1_irq,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				4000000,	/* 4 MHz???? */
				sound_readmem,sound_writemem,sound_readport,sound_writeport_3526,
				interrupt,128	/* ??? */
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		256, 1*16+16*16+16*256,
		terrac_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		terrac_vh_start,
		terrac_vh_stop,
		terracre_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
			   SOUND_YM3526,
			   ym3526_interface
			),
			new MachineSound(
				SOUND_DAC,
				dac_interface
			)
		}
	);
	
	static MachineDriver machine_driver_ym2203 = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				8000000, /* 8 MHz?? */
				readmem,writemem,null,null,
				m68_level1_irq,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				4000000,	/* 4 MHz???? */
				sound_readmem,sound_writemem,sound_readport,sound_writeport_2203,
				interrupt,128	/* ??? */
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		256, 1*16+16*16+16*256,
		terrac_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		terrac_vh_start,
		terrac_vh_stop,
		terracre_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
			   SOUND_YM2203,
			   ym2203_interface
			),
			new MachineSound(
				SOUND_DAC,
				dac_interface
			)
		}
	);
	
	
	
	static RomLoadPtr rom_terracre = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000, REGION_CPU1 );/* 128K for 68000 code */
		ROM_LOAD_ODD ( "1a_4b.rom",    0x00000, 0x4000, 0x76f17479 );	ROM_LOAD_EVEN( "1a_4d.rom",    0x00000, 0x4000, 0x8119f06e );	ROM_LOAD_ODD ( "1a_6b.rom",    0x08000, 0x4000, 0xba4b5822 );	ROM_LOAD_EVEN( "1a_6d.rom",    0x08000, 0x4000, 0xca4852f6 );	ROM_LOAD_ODD ( "1a_7b.rom",    0x10000, 0x4000, 0xd0771bba );	ROM_LOAD_EVEN( "1a_7d.rom",    0x10000, 0x4000, 0x029d59d9 );	ROM_LOAD_ODD ( "1a_9b.rom",    0x18000, 0x4000, 0x69227b56 );	ROM_LOAD_EVEN( "1a_9d.rom",    0x18000, 0x4000, 0x5a672942 );
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for sound cpu */
		ROM_LOAD( "2a_15b.rom",   0x0000, 0x4000, 0x604c3b11 );	ROM_LOAD( "2a_17b.rom",   0x4000, 0x4000, 0xaffc898d );	ROM_LOAD( "2a_18b.rom",   0x8000, 0x4000, 0x302dc0ab );
		ROM_REGION( 0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "2a_16b.rom",   0x00000, 0x2000, 0x591a3804 );/* tiles */
	
		ROM_REGION( 0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "1a_15f.rom",   0x00000, 0x8000, 0x984a597f );/* Background */
		ROM_LOAD( "1a_17f.rom",   0x08000, 0x8000, 0x30e297ff );
		ROM_REGION( 0x10000, REGION_GFX3 | REGIONFLAG_DISPOSE );	ROM_LOAD( "2a_6e.rom",    0x00000, 0x4000, 0xbcf7740b );/* Sprites */
		ROM_LOAD( "2a_7e.rom",    0x04000, 0x4000, 0xa70b565c );	ROM_LOAD( "2a_6g.rom",    0x08000, 0x4000, 0x4a9ec3e6 );	ROM_LOAD( "2a_7g.rom",    0x0c000, 0x4000, 0x450749fc );
		ROM_REGION( 0x0500, REGION_PROMS );	ROM_LOAD( "tc1a_10f.bin", 0x0000, 0x0100, 0xce07c544 );/* red component */
		ROM_LOAD( "tc1a_11f.bin", 0x0100, 0x0100, 0x566d323a );/* green component */
		ROM_LOAD( "tc1a_12f.bin", 0x0200, 0x0100, 0x7ea63946 );/* blue component */
		ROM_LOAD( "tc2a_2g.bin",  0x0300, 0x0100, 0x08609bad );/* sprite lookup table */
		ROM_LOAD( "tc2a_4e.bin",  0x0400, 0x0100, 0x2c43991f );/* sprite palette bank */
	ROM_END(); }}; 
	
	/**********************************************************/
	/* Notes: All the roms are the same except the SOUND ROMs */
	/**********************************************************/
	
	static RomLoadPtr rom_terracrb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000, REGION_CPU1 );/* 128K for 68000 code */
		ROM_LOAD_ODD ( "1a_4b.rom",    0x00000, 0x4000, 0x76f17479 );	ROM_LOAD_EVEN( "1a_4d.rom",    0x00000, 0x4000, 0x8119f06e );	ROM_LOAD_ODD ( "1a_6b.rom",    0x08000, 0x4000, 0xba4b5822 );	ROM_LOAD_EVEN( "1a_6d.rom",    0x08000, 0x4000, 0xca4852f6 );	ROM_LOAD_ODD ( "1a_7b.rom",    0x10000, 0x4000, 0xd0771bba );	ROM_LOAD_EVEN( "1a_7d.rom",    0x10000, 0x4000, 0x029d59d9 );	ROM_LOAD_ODD ( "1a_9b.rom",    0x18000, 0x4000, 0x69227b56 );	ROM_LOAD_EVEN( "1a_9d.rom",    0x18000, 0x4000, 0x5a672942 );
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for sound cpu */
		ROM_LOAD( "2a_15b.rom",   0x0000, 0x4000, 0x604c3b11 );	ROM_LOAD( "dg.12",        0x4000, 0x4000, 0x9e9b3808 );	ROM_LOAD( "2a_18b.rom",   0x8000, 0x4000, 0x302dc0ab );
		ROM_REGION( 0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "2a_16b.rom",   0x00000, 0x2000, 0x591a3804 );/* tiles */
	
		ROM_REGION( 0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "1a_15f.rom",   0x00000, 0x8000, 0x984a597f );/* Background */
		ROM_LOAD( "1a_17f.rom",   0x08000, 0x8000, 0x30e297ff );
		ROM_REGION( 0x10000, REGION_GFX3 | REGIONFLAG_DISPOSE );	ROM_LOAD( "2a_6e.rom",    0x00000, 0x4000, 0xbcf7740b );/* Sprites */
		ROM_LOAD( "2a_7e.rom",    0x04000, 0x4000, 0xa70b565c );	ROM_LOAD( "2a_6g.rom",    0x08000, 0x4000, 0x4a9ec3e6 );	ROM_LOAD( "2a_7g.rom",    0x0c000, 0x4000, 0x450749fc );
		ROM_REGION( 0x0500, REGION_PROMS );	ROM_LOAD( "tc1a_10f.bin", 0x0000, 0x0100, 0xce07c544 );/* red component */
		ROM_LOAD( "tc1a_11f.bin", 0x0100, 0x0100, 0x566d323a );/* green component */
		ROM_LOAD( "tc1a_12f.bin", 0x0200, 0x0100, 0x7ea63946 );/* blue component */
		ROM_LOAD( "tc2a_2g.bin",  0x0300, 0x0100, 0x08609bad );/* sprite lookup table */
		ROM_LOAD( "tc2a_4e.bin",  0x0400, 0x0100, 0x2c43991f );/* sprite palette bank */
	ROM_END(); }}; 
	
	/**********************************************************/
	/* Notes: All the roms are the same except the SOUND ROMs */
	/**********************************************************/
	
	static RomLoadPtr rom_terracra = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000, REGION_CPU1 );/* 128K for 68000 code */
		ROM_LOAD_ODD ( "1a_4b.rom",    0x00000, 0x4000, 0x76f17479 );	ROM_LOAD_EVEN( "1a_4d.rom",    0x00000, 0x4000, 0x8119f06e );	ROM_LOAD_ODD ( "1a_6b.rom",    0x08000, 0x4000, 0xba4b5822 );	ROM_LOAD_EVEN( "1a_6d.rom",    0x08000, 0x4000, 0xca4852f6 );	ROM_LOAD_ODD ( "1a_7b.rom",    0x10000, 0x4000, 0xd0771bba );	ROM_LOAD_EVEN( "1a_7d.rom",    0x10000, 0x4000, 0x029d59d9 );	ROM_LOAD_ODD ( "1a_9b.rom",    0x18000, 0x4000, 0x69227b56 );	ROM_LOAD_EVEN( "1a_9d.rom",    0x18000, 0x4000, 0x5a672942 );
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k to sound cpu */
		ROM_LOAD( "tc2a_15b.bin", 0x0000, 0x4000, 0x790ddfa9 );	ROM_LOAD( "tc2a_17b.bin", 0x4000, 0x4000, 0xd4531113 );
		ROM_REGION( 0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "2a_16b.rom",   0x00000, 0x2000, 0x591a3804 );/* tiles */
	
		ROM_REGION( 0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "1a_15f.rom",   0x00000, 0x8000, 0x984a597f );/* Background */
		ROM_LOAD( "1a_17f.rom",   0x08000, 0x8000, 0x30e297ff );
		ROM_REGION( 0x10000, REGION_GFX3 | REGIONFLAG_DISPOSE );	ROM_LOAD( "2a_6e.rom",    0x00000, 0x4000, 0xbcf7740b );/* Sprites */
		ROM_LOAD( "2a_7e.rom",    0x04000, 0x4000, 0xa70b565c );	ROM_LOAD( "2a_6g.rom",    0x08000, 0x4000, 0x4a9ec3e6 );	ROM_LOAD( "2a_7g.rom",    0x0c000, 0x4000, 0x450749fc );
		ROM_REGION( 0x0500, REGION_PROMS );	ROM_LOAD( "tc1a_10f.bin", 0x0000, 0x0100, 0xce07c544 );/* red component */
		ROM_LOAD( "tc1a_11f.bin", 0x0100, 0x0100, 0x566d323a );/* green component */
		ROM_LOAD( "tc1a_12f.bin", 0x0200, 0x0100, 0x7ea63946 );/* blue component */
		ROM_LOAD( "tc2a_2g.bin",  0x0300, 0x0100, 0x08609bad );/* sprite lookup table */
		ROM_LOAD( "tc2a_4e.bin",  0x0400, 0x0100, 0x2c43991f );/* sprite palette bank */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_terracre	   = new GameDriver("1985"	,"terracre"	,"terracre.java"	,rom_terracre,null	,machine_driver_ym3526	,input_ports_terracre	,null	,ROT270	,	"Nichibutsu", "Terra Cresta (YM3526 set 1)" )
	public static GameDriver driver_terracrb	   = new GameDriver("1985"	,"terracrb"	,"terracre.java"	,rom_terracrb,driver_terracre	,machine_driver_ym3526	,input_ports_terracre	,null	,ROT270	,	"Nichibutsu", "Terra Cresta (YM3526 set 2)" )
	public static GameDriver driver_terracra	   = new GameDriver("1985"	,"terracra"	,"terracre.java"	,rom_terracra,driver_terracre	,machine_driver_ym2203	,input_ports_terracre	,null	,ROT270	,	"Nichibutsu", "Terra Cresta (YM2203)" )
}
