/*************************************************************************

  Driver for Williams/Midway games using the TMS34010 processor.

  Created by Alex Pasadyn and Zsolt Vasvari with some help from Kurt Mahan
  Enhancements by Aaron Giles

  Currently playable:
  ------------------

  	 - Smash Tv
	 - Total Carnage
	 - Mortal Kombat (except Rev 5)
	 - Narc
	 - Strike Force
	 - Trog (prototype and release versions)
	 - Hi Impact Football


  Not Playable:
  ------------

	 - Mortal Kombat Rev 5
	 - Mortal Kombat II (protection, some bankswitching)
	 - NBA Jam          (protection)
	 - Super Hi Impact  (plays end immaturely, TMS34010 core problem?)
	 - Terminator 2     (hangs before entering SkyNet)


  Known Bugs:
  ----------

	 - Strike Force hangs after beating the mother alien. Might be a
	   protecion issue, but it's purely a speculation.

	 - Once in a while the "Milky Way" portion of the background in
	   Strike Force is miscolored

	 - When the Porsche spins in Narc, the wheels are missing for a single
	   frame. This actually might be there on the original, because if the
	   game runs over 60% (as it does now on my PII 266) it's very hard to
	   notice. With 100% framerate, it would be invisible.

	 - Save state is commented out because it only works without sound

  To Do:
  -----

     - Check for auto-erase more than once per frame
	   (not sure if this feature is actually used)

	 - Verify screen sizes

	 - Verify unknown DIP switches

	 - Verify inputs

	 - More cleanups


  Theory:
  ------

     - BANK1 = program ROM
	 - BANK2 = RAM
	 - BANK4 = RAM (palette RAM included here)
	 - BANK5 = sound ROM
	 - BANK6 = sound ROM
	 - BANK8 = graphics ROM

**************************************************************************/
/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class smashtv
{
	
	#define TMS34010_CLOCK_DIVIDER		8
	
	/* these are accurate for MK Rev 5 according to measurements done by Bryan on a real board */
	//#define MKLA5_VBLANK_DURATION		263 /* ms */
	#define MKLA5_VBLANK_DURATION		null
	#define MKLA5_FPS					53.204950
	
	
	/* Variables in vidhrdw/smashtv.c */
	
	/* Variables in machine/smashtv.c */
	static int    wms_bank4_size;
	
	/* Functions in vidhrdw/smashtv.c */
	int wms_vh_start(void);
	int wms_t_vh_start(void);
	void wms_vh_stop (void);
	void wms_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh);
	void wms_vh_eof(void);
	public static WriteHandlerPtr wms_vram_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static ReadHandlerPtr wms_vram_r = new ReadHandlerPtr() { public int handler(int offset);
	void wms_display_addr_changed(UINT32 offs, int rowbytes, int scanline);
	void wms_display_interrupt(int scanline);
	
	/* Functions in machine/smashtv.c */
	void smashtv_init_machine(void);
	void mk_init_machine(void);
	void term2_init_machine(void);
	void trog_init_machine(void);
	void narc_init_machine(void);
	void mk2_init_machine(void);
	void nbajam_init_machine(void);
	
	void wms_to_shiftreg(unsigned int address, unsigned short* shiftreg);
	void wms_from_shiftreg(unsigned int address, unsigned short* shiftreg);
	
	public static WriteHandlerPtr wms_sysreg_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static WriteHandlerPtr wms_sysreg2_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	
	public static WriteHandlerPtr wms_cmos_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static ReadHandlerPtr wms_cmos_r = new ReadHandlerPtr() { public int handler(int offset);
	
	public static WriteHandlerPtr wms_01c00060_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static ReadHandlerPtr wms_01c00060_r = new ReadHandlerPtr() { public int handler(int offset);
	
	public static WriteHandlerPtr wms_unk1_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static WriteHandlerPtr wms_unk2_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	
	public static ReadHandlerPtr wms_dma_r = new ReadHandlerPtr() { public int handler(int offset);
	public static WriteHandlerPtr wms_dma_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static WriteHandlerPtr wms_dma2_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	
	int wms_input_r (int offset);
	
	public static InitDriverPtr init_narc = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_smashtv = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_smashtv4 = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_trog = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_trog3 = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_trogp = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_mk = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_mkla1 = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_mkla2 = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_mkla3 = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_mkla4 = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_mk2 = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_mk2r14 = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_nbajam = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_totcarn = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_totcarnp = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_hiimpact = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_shimpact = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_strkforc = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_term2 = new InitDriverPtr() { public void handler() ;
	
	/* Functions in sndhrdw/smashtv.c */
	void narc_ym2151_int (int irq);
	public static ReadHandlerPtr narc_DAC_r = new ReadHandlerPtr() { public int handler(int offset);
	void narc_slave_DAC_w (int offset,int data);
	void narc_slave_cmd_w (int offset,int data);
	
	
	
	static void nvram_handler(void *file,int read_or_write)
	{
		if (read_or_write)
			osd_fwrite(file,wms_cmos_ram,0x8000);
		else
		{
			if (file)
				osd_fread(file,wms_cmos_ram,0x8000);
			else
				memset(wms_cmos_ram,0,0x8000);
		}
	} };
	
	
	
	static MemoryReadAddress smashtv_readmem[] =
	{
		new MemoryReadAddress( TOBYTE(0x00000000), TOBYTE(0x001fffff), wms_vram_r ), /* VRAM */
		new MemoryReadAddress( TOBYTE(0x01000000), TOBYTE(0x010fffff), MRA_BANK2 ), /* RAM */
		new MemoryReadAddress( TOBYTE(0x01400000), TOBYTE(0x0140ffff), wms_cmos_r ), /* CMOS RAM */
	/*	new MemoryReadAddress( TOBYTE(0x0181f000), TOBYTE(0x0181ffff), paletteram_word_r ), */
	/*	new MemoryReadAddress( TOBYTE(0x01810000), TOBYTE(0x0181ffff), paletteram_word_r ), */
	/*	new MemoryReadAddress( TOBYTE(0x01800000), TOBYTE(0x0181ffff), paletteram_word_r ), */
		new MemoryReadAddress( TOBYTE(0x01800000), TOBYTE(0x019fffff), MRA_BANK4 ), /* RAM */
		new MemoryReadAddress( TOBYTE(0x01a80000), TOBYTE(0x01a8001f), wms_dma_r ),
		new MemoryReadAddress( TOBYTE(0x01c00000), TOBYTE(0x01c0005f), wms_input_r ),
		new MemoryReadAddress( TOBYTE(0x01c00060), TOBYTE(0x01c0007f), wms_01c00060_r ),
		new MemoryReadAddress( TOBYTE(0x02000000), TOBYTE(0x05ffffff), MRA_BANK8 ), /* GFX ROMS */
		new MemoryReadAddress( TOBYTE(0xc0000000), TOBYTE(0xc00001ff), TMS34010_io_register_r ),
		new MemoryReadAddress( TOBYTE(0xff800000), TOBYTE(0xffffffff), MRA_BANK1 ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress smashtv_writemem[] =
	{
		new MemoryWriteAddress( TOBYTE(0x00000000), TOBYTE(0x001fffff), wms_vram_w ), /* VRAM */
		new MemoryWriteAddress( TOBYTE(0x01000000), TOBYTE(0x010fffff), MWA_BANK2, 0, wms_bank2_size ), /* RAM */
		new MemoryWriteAddress( TOBYTE(0x01400000), TOBYTE(0x0140ffff), wms_cmos_w ), /* CMOS RAM */
	/*	new MemoryWriteAddress( TOBYTE(0x0181f000), TOBYTE(0x0181ffff), paletteram_xRRRRRGGGGGBBBBB_word_w, 0 ), */
	/*	new MemoryWriteAddress( TOBYTE(0x01810000), TOBYTE(0x0181ffff), paletteram_xRRRRRGGGGGBBBBB_word_w, 0 ), */
	/*	new MemoryWriteAddress( TOBYTE(0x01800000), TOBYTE(0x0181ffff), paletteram_xRRRRRGGGGGBBBBB_word_w, 0 ), */
		new MemoryWriteAddress( TOBYTE(0x01800000), TOBYTE(0x019fffff), MWA_BANK4, 0, wms_bank4_size ), /* RAM */
		new MemoryWriteAddress( TOBYTE(0x01a00000), TOBYTE(0x01a0009f), wms_dma_w ),
		new MemoryWriteAddress( TOBYTE(0x01a80000), TOBYTE(0x01a8009f), wms_dma_w ),
		new MemoryWriteAddress( TOBYTE(0x01c00060), TOBYTE(0x01c0007f), wms_01c00060_w ),
		new MemoryWriteAddress( TOBYTE(0x01e00000), TOBYTE(0x01e0001f), MWA_NOP ), /* sound */
	/*	new MemoryWriteAddress( TOBYTE(0x01e00000), TOBYTE(0x01e0001f), smashtv_sound_w ), */
	/*	new MemoryWriteAddress( TOBYTE(0x01e00000), TOBYTE(0x01e0001f), mk_sound_w ),		 */
	/*	new MemoryWriteAddress( TOBYTE(0x01e00000), TOBYTE(0x01e0001f), narc_sound_w ),	 */
		new MemoryWriteAddress( TOBYTE(0x01f00000), TOBYTE(0x01f0001f), wms_sysreg_w ),
		new MemoryWriteAddress( TOBYTE(0x02000000), TOBYTE(0x05ffffff), MWA_BANK8, 0, wms_gfx_rom_size ), /* GFX ROMS */
		new MemoryWriteAddress( TOBYTE(0xc0000000), TOBYTE(0xc00001ff), TMS34010_io_register_w ),
		new MemoryWriteAddress( TOBYTE(0xff800000), TOBYTE(0xffffffff), MWA_BANK1, 0, wms_code_rom_size ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress mk2_readmem[] =
	{
		new MemoryReadAddress( TOBYTE(0x00000000), TOBYTE(0x003fffff), wms_vram_r ), /* VRAM */
		new MemoryReadAddress( TOBYTE(0x01000000), TOBYTE(0x013fffff), MRA_BANK2 ), /* Sratch RAM UJ4/5/6/7 */
		new MemoryReadAddress( TOBYTE(0x01400000), TOBYTE(0x0141ffff), wms_cmos_r ),
		new MemoryReadAddress( TOBYTE(0x01600000), TOBYTE(0x016000ff), wms_input_r ),
	//	new MemoryReadAddress( TOBYTE(0x01800000), TOBYTE(0x0181ffff), paletteram_word_r ),
		new MemoryReadAddress( TOBYTE(0x01800000), TOBYTE(0x019fffff), MRA_BANK4 ), /* RAM */
		new MemoryReadAddress( TOBYTE(0x01a80000), TOBYTE(0x01a8001f), wms_dma_r ),
		new MemoryReadAddress( TOBYTE(0x01b14000), TOBYTE(0x01b23fff), MRA_BANK3 ), /* ???? */
	//	new MemoryReadAddress( TOBYTE(0x01d00000), TOBYTE(0x01d0005f), MRA_NOP ), /* ??? */
		new MemoryReadAddress( TOBYTE(0x01d00000), TOBYTE(0x01d0001f), MRA_NOP ), /* ??? */
		/* checks 1d00000 for 0x8000 */
		new MemoryReadAddress( TOBYTE(0x02000000), TOBYTE(0x07ffffff), MRA_BANK8 ), /* GFX ROMS */
		new MemoryReadAddress( TOBYTE(0x04000000), TOBYTE(0x05ffffff), MRA_BANK7 ), /* banked GFX ROMS */
		new MemoryReadAddress( TOBYTE(0xc0000000), TOBYTE(0xc00001ff), TMS34010_io_register_r ),
		new MemoryReadAddress( TOBYTE(0xff800000), TOBYTE(0xffffffff), MRA_BANK1 ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress mk2_writemem[] =
	{
		new MemoryWriteAddress( TOBYTE(0x00000000), TOBYTE(0x003fffff), wms_vram_w ), /* VRAM */
		new MemoryWriteAddress( TOBYTE(0x01000000), TOBYTE(0x013fffff), MWA_BANK2, 0, wms_bank2_size ), /* Scratch RAM */
		new MemoryWriteAddress( TOBYTE(0x01400000), TOBYTE(0x0141ffff), wms_cmos_w ), /* ??? */
	//	new MemoryWriteAddress( TOBYTE(0x01480000), TOBYTE(0x0148001f), MWA_NOP ),  /* w from ffa4d3a0 (mk2) */
	//	new MemoryWriteAddress( TOBYTE(0x014fffe0), TOBYTE(0x014fffff), MWA_NOP ), /* w from ff9daed0 (nbajam) */
	//	new MemoryWriteAddress( TOBYTE(0x01800000), TOBYTE(0x0181ffff), paletteram_xRRRRRGGGGGBBBBB_word_w, 0 ),
		new MemoryWriteAddress( TOBYTE(0x01800000), TOBYTE(0x019fffff), MWA_BANK4, 0, wms_bank4_size ), /* RAM */
		new MemoryWriteAddress( TOBYTE(0x01a80000), TOBYTE(0x01a800ff), wms_dma2_w ),
		new MemoryWriteAddress( TOBYTE(0x01b00000), TOBYTE(0x01b0001f), wms_unk1_w ), /* sysreg (mk2) */
		new MemoryWriteAddress( TOBYTE(0x01b14000), TOBYTE(0x01b23fff), MWA_BANK3), /* ???? */
		new MemoryWriteAddress( TOBYTE(0x01c00060), TOBYTE(0x01c0007f), wms_01c00060_w ),
		new MemoryWriteAddress( TOBYTE(0x01d01000), TOBYTE(0x01d010ff), wms_unk2_w ), /* ???? */
		new MemoryWriteAddress( TOBYTE(0x01d01020), TOBYTE(0x01d0103f), MWA_NOP ), /* sound */
		new MemoryWriteAddress( TOBYTE(0x01d81060), TOBYTE(0x01d8107f), MWA_NOP ), /* ???? */
		/* 1d01070, 1d81070 == watchdog?? */
		new MemoryWriteAddress( TOBYTE(0x01f00000), TOBYTE(0x01f0001f), wms_sysreg2_w ),  /* only nbajam */
		new MemoryWriteAddress( TOBYTE(0x02000000), TOBYTE(0x07ffffff), MWA_BANK8, 0, wms_gfx_rom_size ), /* GFX ROMS */
		new MemoryWriteAddress( TOBYTE(0xc0000000), TOBYTE(0xc00001ff), TMS34010_io_register_w ),
		new MemoryWriteAddress( TOBYTE(0xff800000), TOBYTE(0xffffffff), MWA_BANK1, 0, wms_code_rom_size ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static InputPortPtr input_ports_narc = new InputPortPtr(){ public void handler() { 
	
		PORT_START();       /* IN0 - player 1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
	
		PORT_START();       /* IN1 - player 2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
	
		PORT_START(); 	    /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_TILT );/* Slam Switch */
		PORT_BITX(0x10, IP_ACTIVE_LOW,  0, "Advance", KEYCODE_F2, IP_JOY_NONE );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "Vault Switch", KEYCODE_7, IP_JOY_NONE );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN4 );
	
		PORT_START(); 	    /* IN3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );/* T/B strobe */
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );/* memory protect */
		PORT_BIT( 0x30, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_DIPNAME( 0xc0, 0xc0, "Language" );
		PORT_DIPSETTING(    0xc0, "English" );
		PORT_DIPSETTING(    0x80, "French" );
		PORT_DIPSETTING(    0x40, "German" );
		PORT_DIPSETTING(    0x00, "unknown" );
	
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_trog = new InputPortPtr(){ public void handler() { 
	
		PORT_START();       /* IN0 - player 1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* IN1 - player 2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_TILT );/* Slam Switch */
		PORT_BITX(0x10, IP_ACTIVE_LOW,  0, "Test", KEYCODE_F2, IP_JOY_NONE );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "Service Credit", KEYCODE_9, IP_JOY_NONE );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN3 );/* coin3 */
	
		PORT_START(); 	    /* IN3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );/* video freeze */
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START3 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START4 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER3 | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER3 | IPF_8WAY );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER3 | IPF_8WAY );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER3 | IPF_8WAY );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
	
		PORT_START(); 	    /* IN4 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER4 | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER4 | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER4 | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER4 | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN4 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* IN5 */
		PORT_DIPNAME( 0xff, 0xff, "IN5" );
		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	    /* DS1 */
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x40, 0x40, "Coinage Select" );
		PORT_DIPSETTING(    0x40, "Dipswitch Coinage" );
		PORT_DIPSETTING(    0x00, "CMOS Coinage" );
		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x38, "1" );
		PORT_DIPSETTING(    0x18, "2" );
		PORT_DIPSETTING(    0x28, "3" );
		PORT_DIPSETTING(    0x08, "4" );
		PORT_DIPSETTING(    0x30, "ECA" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x07, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	    /* DS2 */
		PORT_DIPNAME( 0xc0, 0xc0, "Country" );
		PORT_DIPSETTING(    0xc0, "USA" );
		PORT_DIPSETTING(    0x80, "French" );
		PORT_DIPSETTING(    0x40, "German" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Unused") );
		PORT_DIPNAME( 0x20, 0x00, "Powerup Test" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, "Counters" );
		PORT_DIPSETTING(    0x10, "One Counter" );
		PORT_DIPSETTING(    0x00, "Two Counters" );
		PORT_DIPNAME( 0x0c, 0x0c, "Players" );
		PORT_DIPSETTING(    0x0c, "4 Players" );
		PORT_DIPSETTING(    0x04, "3 Players" );
		PORT_DIPSETTING(    0x08, "2 Players" );
		PORT_DIPSETTING(    0x00, "1 Player" );
		PORT_DIPNAME( 0x02, 0x02, "Video Freeze" );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x01, 0x01, "Test Switch" );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	    /* IN8 */
		PORT_DIPNAME( 0xff, 0xff, "IN8" );
		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_START(); 	    /* IN9 */
		PORT_DIPNAME( 0xff, 0xff, "IN9" );
		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_smashtv = new InputPortPtr(){ public void handler() { 
	
		PORT_START();       /* IN0 - player 1 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP, "Move Up", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN, "Move Down", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT, "Move Left", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT, "Move Right", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP, "Fire Up", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN, "Fire Down", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x40, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_LEFT, "Fire Left", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x80, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_RIGHT, "Fire Right", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
	
		PORT_START();       /* IN1 - player 2 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP | IPF_PLAYER2, "2 Move Up", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN | IPF_PLAYER2, "2 Move Down", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT | IPF_PLAYER2, "2 Move Left", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT | IPF_PLAYER2, "2 Move Right", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP | IPF_PLAYER2, "2 Fire Up", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN | IPF_PLAYER2, "2 Fire Down", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x40, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_LEFT | IPF_PLAYER2, "2 Fire Left", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x80, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_RIGHT | IPF_PLAYER2, "2 Fire Right", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
	
		PORT_START(); 	    /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_TILT );/* Slam Switch */
		PORT_BITX(0x10, IP_ACTIVE_LOW,  0, "Test", KEYCODE_F2, IP_JOY_NONE );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "Service Credit", KEYCODE_7, IP_JOY_NONE );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN3 );/* coin3 */
	
		PORT_START(); 	    /* IN3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );/* video freeze */
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN4 );/* coin4 */
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0xf8, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* IN4 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* IN5 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* DS1 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* DS2 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* IN8 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* IN9 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_strkforc = new InputPortPtr(){ public void handler() { 
	
		PORT_START();       /* IN0 - player 1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* IN1 - player 2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_TILT );/* Slam Switch */
		PORT_BITX(0x10, IP_ACTIVE_LOW,  0, "Test", KEYCODE_F2, IP_JOY_NONE );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "Service Credit", KEYCODE_7, IP_JOY_NONE );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN3 );/* coin3 */
	
		PORT_START(); 	    /* IN3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN4 );
		PORT_BIT( 0xfc, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* IN4 */
		PORT_DIPNAME( 0xff, 0xff, "IN4" );
		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_START(); 	    /* IN5 */
		PORT_DIPNAME( 0xff, 0xff, "IN5" );
		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	    /* DS1 */
		PORT_DIPNAME( 0xe0, 0xe0, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0xe0, "7?" );
		PORT_DIPSETTING(    0xc0, "6?" );
		PORT_DIPSETTING(    0xa0, "5?" );
		PORT_DIPSETTING(    0x80, "4?" );
		PORT_DIPSETTING(    0x60, "3?" );
		PORT_DIPSETTING(    0x40, "2?" );
		PORT_DIPSETTING(    0x20, "1?" );
		PORT_DIPSETTING(    0x00, "0?" );
		PORT_DIPNAME( 0x10, 0x10, "Ships" );
		PORT_DIPSETTING(    0x10, "1?" );
		PORT_DIPSETTING(    0x00, "0?" );
		PORT_DIPNAME( 0x0c, 0x0c, "Points for Ship" );
		PORT_DIPSETTING(    0x0c, "c?" );
		PORT_DIPSETTING(    0x08, "8?" );
		PORT_DIPSETTING(    0x04, "4?" );
		PORT_DIPSETTING(    0x00, "0?" );
		PORT_DIPNAME( 0x02, 0x02, "Credits to Start" );
		PORT_DIPSETTING(    0x02, "2" );
		PORT_DIPSETTING(    0x00, "1" );
		PORT_DIPNAME( 0x01, 0x01, "Coin Meter" );
		PORT_DIPSETTING(    0x01, "1?" );
		PORT_DIPSETTING(    0x00, "0?" );
	
		PORT_START(); 	    /* DS2 */
		PORT_DIPNAME( 0x80, 0x80, "Test Switch" );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x78, 0x78, "Coin 1" );
		PORT_DIPSETTING(    0x78, "????" );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x07, 0x07, "Coin 2" );
		PORT_DIPSETTING(    0x07, "???" );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	    /* IN8 */
		PORT_DIPNAME( 0xff, 0xff, "IN8" );
		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	    /* IN9 */
		PORT_DIPNAME( 0xff, 0xff, "IN9" );
		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_mk = new InputPortPtr(){ public void handler() { 
	
		PORT_START();       /* IN0 - player 1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* IN1 - player 2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_TILT );/* Slam Switch */
		PORT_BITX(0x10, IP_ACTIVE_LOW,  0, "Test", KEYCODE_F2, IP_JOY_NONE );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "Service Credit", KEYCODE_7, IP_JOY_NONE );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN3 );/* coin3 */
	
		PORT_START(); 	    /* IN3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN4 );/* video freeze */
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
	
		PORT_START(); 	    /* IN4 */
		PORT_DIPNAME( 0xff, 0xff, "IN4" );
		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_START(); 	    /* IN5 */
		PORT_DIPNAME( 0xff, 0xff, "IN5" );
		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	    /* DS1 */
		PORT_DIPNAME( 0x80, 0x00, "Coinage Source" );
		PORT_DIPSETTING(    0x80, "Dipswitch" );
		PORT_DIPSETTING(    0x00, "CMOS" );
		PORT_DIPNAME( 0x7c, 0x7c, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x7c, "USA-1" );
		PORT_DIPSETTING(    0x3c, "USA-2" );
		PORT_DIPSETTING(    0x5c, "USA-3" );
		PORT_DIPSETTING(    0x1c, "USA-4" );
		PORT_DIPSETTING(    0x6c, "USA-ECA" );
		PORT_DIPSETTING(    0x74, "German-1" );
		PORT_DIPSETTING(    0x34, "German-2" );
		PORT_DIPSETTING(    0x54, "German-3" );
		PORT_DIPSETTING(    0x14, "German-4" );
		PORT_DIPSETTING(    0x64, "German-5" );
		PORT_DIPSETTING(    0x78, "French-1" );
		PORT_DIPSETTING(    0x38, "French-2" );
		PORT_DIPSETTING(    0x58, "French-3" );
		PORT_DIPSETTING(    0x18, "French-4" );
		PORT_DIPSETTING(    0x68, "French-ECA" );
		PORT_DIPSETTING(    0x0c, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x02, 0x00, "Counters" );
		PORT_DIPSETTING(    0x02, "One" );
		PORT_DIPSETTING(    0x00, "Two" );
		PORT_DIPNAME( 0x01, 0x01, "Test Switch" );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	    /* DS2 */
		PORT_DIPNAME( 0x80, 0x80, "Violence" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "Blood" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, "Low Blows" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, "Attract Sound" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, "Comic Book Offer" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
	
		PORT_START(); 	    /* IN8 */
		PORT_DIPNAME( 0xff, 0xff, "IN8" );
		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	    /* IN9 */
		PORT_DIPNAME( 0xff, 0xff, "IN9" );
		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_mkla1 = new InputPortPtr(){ public void handler() { 
	
		PORT_START();       /* IN0 - player 1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* IN1 - player 2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_TILT );/* Slam Switch */
		PORT_BITX(0x10, IP_ACTIVE_LOW,  0, "Test", KEYCODE_F2, IP_JOY_NONE );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "Service Credit", KEYCODE_7, IP_JOY_NONE );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN3 );/* coin3 */
	
		PORT_START(); 	    /* IN3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN4 );/* video freeze */
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
	
		PORT_START(); 	    /* IN4 */
		PORT_DIPNAME( 0xff, 0xff, "IN4" );
		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_START(); 	    /* IN5 */
		PORT_DIPNAME( 0xff, 0xff, "IN5" );
		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	    /* DS1 */
		PORT_DIPNAME( 0x80, 0x80, "Violence" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "Blood" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, "Low Blows" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, "Attract Sound" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, "Comic Book Offer" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
	
		PORT_START(); 	    /* DS2 */
		PORT_DIPNAME( 0x80, 0x00, "Coinage Source" );
		PORT_DIPSETTING(    0x80, "Dipswitch" );
		PORT_DIPSETTING(    0x00, "CMOS" );
		PORT_DIPNAME( 0x7c, 0x7c, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x7c, "USA-1" );
		PORT_DIPSETTING(    0x3c, "USA-2" );
		PORT_DIPSETTING(    0x5c, "USA-3" );
		PORT_DIPSETTING(    0x1c, "USA-4" );
		PORT_DIPSETTING(    0x6c, "USA-ECA" );
		PORT_DIPSETTING(    0x74, "German-1" );
		PORT_DIPSETTING(    0x34, "German-2" );
		PORT_DIPSETTING(    0x54, "German-3" );
		PORT_DIPSETTING(    0x14, "German-4" );
		PORT_DIPSETTING(    0x64, "German-5" );
		PORT_DIPSETTING(    0x78, "French-1" );
		PORT_DIPSETTING(    0x38, "French-2" );
		PORT_DIPSETTING(    0x58, "French-3" );
		PORT_DIPSETTING(    0x18, "French-4" );
		PORT_DIPSETTING(    0x68, "French-ECA" );
		PORT_DIPSETTING(    0x0c, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x02, 0x00, "Counters" );
		PORT_DIPSETTING(    0x02, "One" );
		PORT_DIPSETTING(    0x00, "Two" );
		PORT_DIPNAME( 0x01, 0x01, "Test Switch" );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	    /* IN8 */
		PORT_DIPNAME( 0xff, 0xff, "IN8" );
		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	    /* IN9 */
		PORT_DIPNAME( 0xff, 0xff, "IN9" );
		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_term2 = new InputPortPtr(){ public void handler() { 
	
		PORT_START();       /* IN0 - player 1 */
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );/* trigger */
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );/* bomb */
		PORT_BIT( 0xcf, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* IN1 - player 2 */
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );/* trigger */
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );/* bomb */
		PORT_BIT( 0xcf, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_TILT );/* Slam Switch */
		PORT_BITX(0x10, IP_ACTIVE_LOW,  0, "Test", KEYCODE_F2, IP_JOY_NONE );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "Service Credit", KEYCODE_7, IP_JOY_NONE );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN3 );/* coin3 */
	
		PORT_START(); 	    /* IN3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );/* video freeze */
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN4 );/* coin4 */
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* IN4 */
		PORT_ANALOG( 0xff, 0x00, IPT_AD_STICK_X | IPF_REVERSE | IPF_PLAYER1, 20, 10, 0, 0xff);
	
		PORT_START(); 	    /* IN5 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* DS1 */
		PORT_DIPNAME( 0x80, 0x00, "Normal Display" );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "Dipswitch Coinage" );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x38, "1" );
		PORT_DIPSETTING(    0x18, "2" );
		PORT_DIPSETTING(    0x28, "3" );
		PORT_DIPSETTING(    0x08, "4" );
		PORT_DIPSETTING(    0x30, "USA ECA" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x07, 0x03, "Credits" );
		PORT_DIPSETTING(    0x07, "2 Start/1 Continue" );
		PORT_DIPSETTING(    0x06, "4 Start/1 Continue" );
		PORT_DIPSETTING(    0x05, "2 Start/2 Continue" );
		PORT_DIPSETTING(    0x04, "4 Start/2 Continue" );
		PORT_DIPSETTING(    0x03, "1 Start/1 Continue" );
		PORT_DIPSETTING(    0x02, "3 Start/2 Continue" );
		PORT_DIPSETTING(    0x01, "3 Start/1 Continue" );
		PORT_DIPSETTING(    0x00, "3 Start/3 Continue" );
	
		PORT_START(); 	    /* DS2 */
		PORT_DIPNAME( 0xc0, 0xc0, "Country" );
		PORT_DIPSETTING(    0xc0, "USA" );
		PORT_DIPSETTING(    0x80, "French" );
		PORT_DIPSETTING(    0x40, "German" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Unused") );
		PORT_DIPNAME( 0x20, 0x00, "Powerup Test" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, "Two Counters" );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, "Players" );
		PORT_DIPSETTING(    0x08, "2 Players" );
		PORT_DIPSETTING(    0x00, "1 Player" );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Video Freeze" );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x01, 0x01, "Test Switch" );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	    /* IN8 */
		PORT_DIPNAME( 0xff, 0xff, "IN8" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0xff, DEF_STR( "On") );
	
		PORT_START(); 	    /* IN9 */
		PORT_DIPNAME( 0xff, 0xff, "IN9" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0xff, DEF_STR( "On") );
	
		PORT_START(); 	    /* IN10 */
		PORT_ANALOG( 0xff, 0x00, IPT_AD_STICK_Y | IPF_PLAYER1, 20, 10, 0, 0xff);
	
		PORT_START(); 	    /* IN11 */
		PORT_ANALOG( 0xff, 0x00, IPT_AD_STICK_X | IPF_REVERSE | IPF_PLAYER2, 20, 10, 0, 0xff);
	
		PORT_START(); 	    /* IN12 */
		PORT_ANALOG( 0xff, 0x00, IPT_AD_STICK_Y | IPF_PLAYER2, 20, 10, 0, 0xff);
	
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_totcarn = new InputPortPtr(){ public void handler() { 
	
		PORT_START();       /* IN0 - player 1 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP, "Move Up", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN, "Move Down", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT, "Move Left", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT, "Move Right", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP, "Fire Up", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN, "Fire Down", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x40, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_LEFT, "Fire Left", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x80, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_RIGHT, "Fire Right", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
	
		PORT_START();       /* IN1 - player 2 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP | IPF_PLAYER2, "2 Move Up", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN | IPF_PLAYER2, "2 Move Down", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT | IPF_PLAYER2, "2 Move Left", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT | IPF_PLAYER2, "2 Move Right", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP | IPF_PLAYER2, "2 Fire Up", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN | IPF_PLAYER2, "2 Fire Down", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x40, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_LEFT | IPF_PLAYER2, "2 Fire Left", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x80, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_RIGHT | IPF_PLAYER2, "2 Fire Right", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
	
		PORT_START(); 	    /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_TILT );/* Slam Switch */
		PORT_BITX(0x10, IP_ACTIVE_LOW,  0, "Test", KEYCODE_F2, IP_JOY_NONE );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "Service Credit", KEYCODE_7, IP_JOY_NONE );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN3 );/* coin3 */
	
		PORT_START(); 	    /* IN3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );/* video freeze */
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN4 );/* coin4 */
		PORT_BIT( 0xfc, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* IN4 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* IN5 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* DS1 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* DS2 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* IN8 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* IN9 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_mk2 = new InputPortPtr(){ public void handler() { 
	
		PORT_START();       /* IN0 - player 1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* IN1 - player 2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_TILT );/* Slam Switch */
		PORT_BITX(0x10, IP_ACTIVE_LOW,  0, "Test", KEYCODE_F2, IP_JOY_NONE );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "Service Credit", KEYCODE_7, IP_JOY_NONE );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN3 );/* coin3 */
	
		PORT_START(); 	    /* IN3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN4 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );/* volume down */
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );/* volume up */
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
	
		PORT_START(); 	    /* IN4 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON4 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON5 );
		PORT_BIT( 0x0c, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER2 );
		PORT_BIT( 0xc0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* IN5 */
		PORT_DIPNAME( 0xff, 0xff, "IN5" );
		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	    /* DS1 */
		PORT_DIPNAME( 0x80, 0x00, "Coinage Source" );
		PORT_DIPSETTING(    0x80, "Dipswitch" );
		PORT_DIPSETTING(    0x00, "CMOS" );
		PORT_DIPNAME( 0x7c, 0x7c, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x7c, "USA-1" );
		PORT_DIPSETTING(    0x3c, "USA-2" );
		PORT_DIPSETTING(    0x5c, "USA-3" );
		PORT_DIPSETTING(    0x1c, "USA-4" );
		PORT_DIPSETTING(    0x6c, "USA-ECA" );
		PORT_DIPSETTING(    0x74, "German-1" );
		PORT_DIPSETTING(    0x34, "German-2" );
		PORT_DIPSETTING(    0x54, "German-3" );
		PORT_DIPSETTING(    0x14, "German-4" );
		PORT_DIPSETTING(    0x64, "German-5" );
		PORT_DIPSETTING(    0x78, "French-1" );
		PORT_DIPSETTING(    0x38, "French-2" );
		PORT_DIPSETTING(    0x58, "French-3" );
		PORT_DIPSETTING(    0x18, "French-4" );
		PORT_DIPSETTING(    0x68, "French-ECA" );
		PORT_DIPSETTING(    0x0c, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x02, 0x00, "Counters" );
		PORT_DIPSETTING(    0x02, "One" );
		PORT_DIPSETTING(    0x00, "Two" );
		PORT_DIPNAME( 0x01, 0x01, "Test Switch" );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	    /* DS2 */
		PORT_DIPNAME( 0x80, 0x80, "Violence" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "Blood" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, "Low Blows" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, "Attract Sound" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, "Comic Book Offer" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, "Bill Validator" );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, "Powerup Test" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x01, 0x01, "Circuit Boards" );
		PORT_DIPSETTING(    0x01, "2" );
		PORT_DIPSETTING(    0x00, "1" );
	
	
		PORT_START(); 	    /* IN8 */
		PORT_DIPNAME( 0xff, 0xff, "IN8" );
		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_START(); 	    /* IN9 */
		PORT_DIPNAME( 0xff, 0xff, "IN9" );
		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_nbajam = new InputPortPtr(){ public void handler() { 
	
		PORT_START();       /* IN0 - player 1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* IN1 - player 2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_TILT );/* Slam Switch */
		PORT_BITX(0x10, IP_ACTIVE_LOW,  0, "Test", KEYCODE_F2, IP_JOY_NONE );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "Service Credit", KEYCODE_7, IP_JOY_NONE );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN3 );/* coin3 */
	
		PORT_START(); 	    /* IN3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN4 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );/* volume down */
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );/* volume up */
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
	
		PORT_START(); 	    /* IN4 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER3 | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER3 | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER3 | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER3 | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER3 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* IN5 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER4 | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER4 | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER4 | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER4 | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER4 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	    /* DS1 */
		PORT_DIPNAME( 0x80, 0x80, "Players" );
		PORT_DIPSETTING(    0x00, "4" );
		PORT_DIPSETTING(    0x80, "2" );
		PORT_DIPNAME( 0x40, 0x40, "Validator" );
		PORT_DIPSETTING(    0x00, "Installed" );
		PORT_DIPSETTING(    0x40, "None" );
		PORT_DIPNAME( 0x20, 0x20, "Video" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, "Show" );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_DIPNAME( 0x02, 0x00, "Powerup Test" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x01, 0x01, "Test Switch" );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	    /* DS2 */
		PORT_DIPNAME( 0x80, 0x00, "Coinage Source" );
		PORT_DIPSETTING(    0x80, "Dipswitch" );
		PORT_DIPSETTING(    0x00, "CMOS" );
		PORT_DIPNAME( 0x7c, 0x7c, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x7c, "USA-1" );
		PORT_DIPSETTING(    0x3c, "USA-2" );
		PORT_DIPSETTING(    0x5c, "USA-3" );
		PORT_DIPSETTING(    0x1c, "USA-4" );
		PORT_DIPSETTING(    0x6c, "USA-ECA" );
		PORT_DIPSETTING(    0x74, "German-1" );
		PORT_DIPSETTING(    0x34, "German-2" );
		PORT_DIPSETTING(    0x54, "German-3" );
		PORT_DIPSETTING(    0x14, "German-4" );
		PORT_DIPSETTING(    0x64, "German-5" );
		PORT_DIPSETTING(    0x78, "French-1" );
		PORT_DIPSETTING(    0x38, "French-2" );
		PORT_DIPSETTING(    0x58, "French-3" );
		PORT_DIPSETTING(    0x18, "French-4" );
		PORT_DIPSETTING(    0x68, "French-ECA" );
		PORT_DIPSETTING(    0x0c, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x03, 0x00, "Coin Counters" );
		PORT_DIPSETTING(    0x03, "1 Counter, 1 count/coin" );
		PORT_DIPSETTING(    0x02, "1 Counter, Totalizing" );
		PORT_DIPSETTING(    0x01, "2 Counters, 1 count/coin" );
		PORT_DIPSETTING(    0x00, "1 Counter, 1 count/coin" );
	
	
		PORT_START(); 	    /* IN8 */
		PORT_DIPNAME( 0xff, 0xff, "IN8" );
		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_START(); 	    /* IN9 */
		PORT_DIPNAME( 0xff, 0xff, "IN9" );
		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
	INPUT_PORTS_END(); }}; 
	
	
	static struct tms34010_config cpu_config =
	{
		0,							/* halt on reset */
		NULL,						/* generate interrupt */
		wms_to_shiftreg,			/* write to shiftreg function */
		wms_from_shiftreg,			/* read from shiftreg function */
		wms_display_addr_changed,	/* display address changed */
		wms_display_interrupt		/* display interrupt callback */
	};
	
	
	/* Y-unit games */
	static MachineDriver machine_driver_smashtv = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_TMS34010,
				50000000/TMS34010_CLOCK_DIVIDER,	/* 50 Mhz */
				smashtv_readmem,smashtv_writemem,null,null,
				ignore_interrupt,0,
				null,null,cpu_config
			),
			SOUND_CPU_WILLIAMS_CVSD
		},
		MKLA5_FPS, MKLA5_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		smashtv_init_machine,
	
		/* video hardware */
		512, 288, new rectangle( 0, 395, 20, 275 ),
	
		0,
		65536,null,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		wms_vh_eof,
		wms_vh_start,
		wms_vh_stop,
		wms_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			SOUND_WILLIAMS_CVSD
		},
	
		nvram_handler
	);
	
	/* Z-Unit */
	static MachineDriver machine_driver_narc = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_TMS34010,
				48000000/TMS34010_CLOCK_DIVIDER,	/* 48 Mhz */
				smashtv_readmem,smashtv_writemem,null,null,
				ignore_interrupt,0,
				null,null,cpu_config
			),
			SOUND_CPU_WILLIAMS_NARC
		},
		57, 0,//2500,	/* frames per second, vblank duration */
		1,
		narc_init_machine,
	
		/* video hardware */
	    512, 432, { 0, 511, 27, 426 },
	
		0,
		65536,null,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		wms_vh_eof,
		wms_vh_start,
		wms_vh_stop,
		wms_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			SOUND_WILLIAMS_NARC
		},
	
		nvram_handler
	);
	
	static MachineDriver machine_driver_trog = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_TMS34010,
				50000000/TMS34010_CLOCK_DIVIDER,	/* 50 Mhz */
				smashtv_readmem,smashtv_writemem,null,null,
				ignore_interrupt,0,
				null,null,cpu_config
			),
			SOUND_CPU_WILLIAMS_CVSD
		},
		MKLA5_FPS, MKLA5_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		trog_init_machine,
	
		/* video hardware */
		512, 288, new rectangle( 0, 395, 20, 275 ),
	
		0,
		65536,null,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		wms_vh_eof,
		wms_vh_start,
		wms_vh_stop,
		wms_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			SOUND_WILLIAMS_CVSD
		},
	
		nvram_handler
	);
	
	/* Y-Unit */
	static MachineDriver machine_driver_mk = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_TMS34010,
				48000000/TMS34010_CLOCK_DIVIDER,	/* 48 Mhz */
				smashtv_readmem,smashtv_writemem,null,null,
				ignore_interrupt,0,
				null,null,cpu_config
			),
			SOUND_CPU_WILLIAMS_ADPCM
		},
		MKLA5_FPS, MKLA5_VBLANK_DURATION,	/* frames per second, vblank duration */
		1, /* cpu slices */
		mk_init_machine,
	
		/* video hardware */
		512, 304, new rectangle( 0, 399, 27, 281 ),
	
		0,
		65536,null,
	    null,
	
	    VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		wms_vh_eof,
		wms_vh_start,
		wms_vh_stop,
		wms_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			SOUND_WILLIAMS_ADPCM(REGION_SOUND1)
		},
	
		nvram_handler
	);
	
	/* Y-Unit */
	static MachineDriver machine_driver_term2 = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_TMS34010,
				50000000/TMS34010_CLOCK_DIVIDER,	/* 50 Mhz */
				smashtv_readmem,smashtv_writemem,null,null,
				ignore_interrupt,0,
				null,null,cpu_config
			),
			SOUND_CPU_WILLIAMS_ADPCM
		},
		MKLA5_FPS, MKLA5_VBLANK_DURATION,	/* frames per second, vblank duration */
		1, /* cpu slices */
		term2_init_machine,
	
		/* video hardware */
		512, 304, new rectangle( 0, 399, 27, 281 ),
	
		0,
		65536,null,
	    null,
	
	    VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		wms_vh_eof,
		wms_vh_start,
		wms_vh_stop,
		wms_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			SOUND_WILLIAMS_ADPCM(REGION_SOUND1)
		},
	
		nvram_handler
	);
	
	/* T-Unit */
	static MachineDriver machine_driver_mk2 = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_TMS34010,
				50000000/TMS34010_CLOCK_DIVIDER,	/* 50 Mhz */
				mk2_readmem,mk2_writemem,null,null,
				ignore_interrupt,0,
				null,null,cpu_config
			),
		},
		MKLA5_FPS, MKLA5_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		mk2_init_machine,
	
		/* video hardware */
		512, 512, new rectangle( 54, 452, 0, 255 ),
	
		0,
		65536,null,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		wms_vh_eof,
		wms_t_vh_start,
		wms_vh_stop,
		wms_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound( 0 )
		},
	
		nvram_handler
	);
	
	/* T-Unit */
	static MachineDriver machine_driver_nbajam = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_TMS34010,
				50000000/TMS34010_CLOCK_DIVIDER,	/* 50 Mhz */
				mk2_readmem,mk2_writemem,null,null,
				ignore_interrupt,0,
				null,null,cpu_config
			),
			SOUND_CPU_WILLIAMS_ADPCM
		},
		MKLA5_FPS, MKLA5_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		nbajam_init_machine,
	
		/* video hardware */
		512, 512, new rectangle( 54, 452, 0, 255 ),
	
		0,
		65536,null,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		wms_vh_eof,
		wms_t_vh_start,
		wms_vh_stop,
		wms_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			SOUND_WILLIAMS_ADPCM(REGION_SOUND1)
		},
	
		nvram_handler
	);
	
	
	
	#if 0
	void wms_stateload(void)
	{
		void *f;
		if ((f = osd_fopen(Machine.gamedrv.name,0,OSD_FILETYPE_STATE,0)) != 0)
		{
			if (errorlog) fprintf(errorlog,"Loading State...\n");
			TMS34010_State_Load(0,f);
			osd_fread(f,wms_videoram,wms_videoram_size);
			osd_fread(f,cpu_bankbase[2],wms_bank2_size);
			//osd_fread(f,cpu_bankbase[4],wms_bank4_size);
			osd_fread(f,wms_cmos_ram,0x8000);
			osd_fread(f,cpu_bankbase[8],wms_gfx_rom_size);
			if (errorlog) fprintf(errorlog,"State loaded\n");
			osd_fclose(f);
		}
	}
	
	void wms_statesave(void)
	{
		void *f;
		if ((f = osd_fopen(Machine.gamedrv.name,0,OSD_FILETYPE_STATE,1))!= 0)
		{
			if (errorlog) fprintf(errorlog,"Saving State...\n");
			TMS34010_State_Save(0,f);
			osd_fwrite(f,wms_videoram,wms_videoram_size);
			osd_fwrite(f,cpu_bankbase[2],wms_bank2_size);
			//osd_fwrite(f,cpu_bankbase[4],wms_bank4_size);
			osd_fwrite(f,wms_cmos_ram,0x8000);
			osd_fwrite(f,cpu_bankbase[8],wms_gfx_rom_size);
			if (errorlog) fprintf(errorlog,"State saved\n");
			osd_fclose(f);
		}
	}
	#endif
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_narc = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /*34010 code */
		ROM_LOAD_ODD ( "u42",  0x80000, 0x20000, 0xd1111b76 )  /* even */
		ROM_LOAD_EVEN( "u24",  0x80000, 0x20000, 0xaa0d3082 )  /* odd  */
		ROM_LOAD_ODD ( "u41",  0xc0000, 0x20000, 0x3903191f )  /* even */
		ROM_LOAD_EVEN( "u23",  0xc0000, 0x20000, 0x7a316582 )  /* odd  */
	
		ROM_REGION( 0x30000, REGION_CPU2 );    /* sound CPU */
		ROM_LOAD ( "u4-snd", 0x10000, 0x10000, 0x450a591a );
		ROM_LOAD ( "u5-snd", 0x20000, 0x10000, 0xe551e5e3 );
	
		ROM_REGION( 0x50000, REGION_CPU3 );    /* slave sound CPU */
		ROM_LOAD ( "u35-snd", 0x10000, 0x10000, 0x81295892 );
		ROM_LOAD ( "u36-snd", 0x20000, 0x10000, 0x16cdbb13 );
		ROM_LOAD ( "u37-snd", 0x30000, 0x10000, 0x29dbeffd );
		ROM_LOAD ( "u38-snd", 0x40000, 0x10000, 0x09b03b80 );
	
		ROM_REGION( 0x800000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "u94",  0x000000, 0x10000, 0xca3194e4 ); /* even */
		ROM_LOAD ( "u76",  0x200000, 0x10000, 0x1cd897f4 ); /* odd  */
		ROM_LOAD ( "u93",  0x010000, 0x10000, 0x0ed7f7f5 ); /* even */
		ROM_LOAD ( "u75",  0x210000, 0x10000, 0x78abfa01 ); /* odd  */
		ROM_LOAD ( "u92",  0x020000, 0x10000, 0x40d2fc66 ); /* even */
		ROM_LOAD ( "u74",  0x220000, 0x10000, 0x66d2a234 ); /* odd  */
		ROM_LOAD ( "u91",  0x030000, 0x10000, 0xf39325e0 ); /* even */
		ROM_LOAD ( "u73",  0x230000, 0x10000, 0xefa5cd4e ); /* odd  */
		ROM_LOAD ( "u90",  0x040000, 0x10000, 0x0132aefa ); /* even */
		ROM_LOAD ( "u72",  0x240000, 0x10000, 0x70638eb5 ); /* odd  */
		ROM_LOAD ( "u89",  0x050000, 0x10000, 0xf7260c9e ); /* even */
		ROM_LOAD ( "u71",  0x250000, 0x10000, 0x61226883 ); /* odd  */
		ROM_LOAD ( "u88",  0x060000, 0x10000, 0xedc19f42 ); /* even */
		ROM_LOAD ( "u70",  0x260000, 0x10000, 0xc808849f ); /* odd  */
		ROM_LOAD ( "u87",  0x070000, 0x10000, 0xd9b42ff9 ); /* even */
		ROM_LOAD ( "u69",  0x270000, 0x10000, 0xe7f9c34f ); /* odd  */
		ROM_LOAD ( "u86",  0x080000, 0x10000, 0xaf7daad3 ); /* even */
		ROM_LOAD ( "u68",  0x280000, 0x10000, 0x88a634d5 ); /* odd  */
		ROM_LOAD ( "u85",  0x090000, 0x10000, 0x095fae6b ); /* even */
		ROM_LOAD ( "u67",  0x290000, 0x10000, 0x4ab8b69e ); /* odd  */
		ROM_LOAD ( "u84",  0x0a0000, 0x10000, 0x3fdf2057 ); /* even */
		ROM_LOAD ( "u66",  0x2a0000, 0x10000, 0xe1da4b25 ); /* odd  */
		ROM_LOAD ( "u83",  0x0b0000, 0x10000, 0xf2d27c9f ); /* even */
		ROM_LOAD ( "u65",  0x2b0000, 0x10000, 0x6df0d125 ); /* odd  */
		ROM_LOAD ( "u82",  0x0c0000, 0x10000, 0x962ce47c ); /* even */
		ROM_LOAD ( "u64",  0x2c0000, 0x10000, 0xabab1b16 ); /* odd  */
		ROM_LOAD ( "u81",  0x0d0000, 0x10000, 0x00fe59ec ); /* even */
		ROM_LOAD ( "u63",  0x2d0000, 0x10000, 0x80602f31 ); /* odd  */
		ROM_LOAD ( "u80",  0x0e0000, 0x10000, 0x147ba8e9 ); /* even */
		ROM_LOAD ( "u62",  0x2e0000, 0x10000, 0xc2a476d1 ); /* odd  */
	
		ROM_LOAD ( "u58",  0x400000, 0x10000, 0x8a7501e3 ); /* even */
		ROM_LOAD ( "u40",  0x600000, 0x10000, 0x7fcaebc7 ); /* odd  */
		ROM_LOAD ( "u57",  0x410000, 0x10000, 0xa504735f ); /* even */
		ROM_LOAD ( "u39",  0x610000, 0x10000, 0x7db5cf52 ); /* odd  */
		ROM_LOAD ( "u56",  0x420000, 0x10000, 0x55f8cca7 ); /* even */
		ROM_LOAD ( "u38",  0x620000, 0x10000, 0x3f9f3ef7 ); /* odd  */
		ROM_LOAD ( "u55",  0x430000, 0x10000, 0xd3c932c1 ); /* even */
		ROM_LOAD ( "u37",  0x630000, 0x10000, 0xed81826c ); /* odd  */
		ROM_LOAD ( "u54",  0x440000, 0x10000, 0xc7f4134b ); /* even */
		ROM_LOAD ( "u36",  0x640000, 0x10000, 0xe5d855c0 ); /* odd  */
		ROM_LOAD ( "u53",  0x450000, 0x10000, 0x6be4da56 ); /* even */
		ROM_LOAD ( "u35",  0x650000, 0x10000, 0x3a7b1329 ); /* odd  */
		ROM_LOAD ( "u52",  0x460000, 0x10000, 0x1ea36a4a ); /* even */
		ROM_LOAD ( "u34",  0x660000, 0x10000, 0xfe982b0e ); /* odd  */
		ROM_LOAD ( "u51",  0x470000, 0x10000, 0x9d4b0324 ); /* even */
		ROM_LOAD ( "u33",  0x670000, 0x10000, 0x6bc7eb0f ); /* odd  */
		ROM_LOAD ( "u50",  0x480000, 0x10000, 0x6f9f0c26 ); /* even */
		ROM_LOAD ( "u32",  0x680000, 0x10000, 0x5875a6d3 ); /* odd  */
		ROM_LOAD ( "u49",  0x490000, 0x10000, 0x80386fce ); /* even */
		ROM_LOAD ( "u31",  0x690000, 0x10000, 0x2fa4b8e5 ); /* odd  */
		ROM_LOAD ( "u48",  0x4a0000, 0x10000, 0x05c16185 ); /* even */
		ROM_LOAD ( "u30",  0x6a0000, 0x10000, 0x7e4bb8ee ); /* odd  */
		ROM_LOAD ( "u47",  0x4b0000, 0x10000, 0x4c0151f1 ); /* even */
		ROM_LOAD ( "u29",  0x6b0000, 0x10000, 0x45136fd9 ); /* odd  */
		ROM_LOAD ( "u46",  0x4c0000, 0x10000, 0x5670bfcb ); /* even */
		ROM_LOAD ( "u28",  0x6c0000, 0x10000, 0xd6cdac24 ); /* odd  */
		ROM_LOAD ( "u45",  0x4d0000, 0x10000, 0x27f10d98 ); /* even */
		ROM_LOAD ( "u27",  0x6d0000, 0x10000, 0x4d33bbec ); /* odd  */
		ROM_LOAD ( "u44",  0x4e0000, 0x10000, 0x93b8eaa4 ); /* even */
		ROM_LOAD ( "u26",  0x6e0000, 0x10000, 0xcb19f784 ); /* odd  */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_narc3 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /*34010 code */
		ROM_LOAD_ODD ( "narcrev3.u78",  0x80000, 0x10000, 0x388581b0 )  /* even */
		ROM_LOAD_EVEN( "narcrev3.u60",  0x80000, 0x10000, 0xf273bc04 )  /* odd  */
		ROM_LOAD_ODD ( "narcrev3.u77",  0xa0000, 0x10000, 0xbdafaccc )  /* even */
		ROM_LOAD_EVEN( "narcrev3.u59",  0xa0000, 0x10000, 0x96314a99 )  /* odd  */
		ROM_LOAD_ODD ( "narcrev3.u42",  0xc0000, 0x10000, 0x56aebc81 )  /* even */
		ROM_LOAD_EVEN( "narcrev3.u24",  0xc0000, 0x10000, 0x11d7e143 )  /* odd  */
		ROM_LOAD_ODD ( "narcrev3.u41",  0xe0000, 0x10000, 0x6142fab7 )  /* even */
		ROM_LOAD_EVEN( "narcrev3.u23",  0xe0000, 0x10000, 0x98cdd178 )  /* odd  */
	
		ROM_REGION( 0x30000, REGION_CPU2 );    /* sound CPU */
		ROM_LOAD ( "u4-snd", 0x10000, 0x10000, 0x450a591a );
		ROM_LOAD ( "u5-snd", 0x20000, 0x10000, 0xe551e5e3 );
	
		ROM_REGION( 0x50000, REGION_CPU3 );    /* slave sound CPU */
		ROM_LOAD ( "u35-snd", 0x10000, 0x10000, 0x81295892 );
		ROM_LOAD ( "u36-snd", 0x20000, 0x10000, 0x16cdbb13 );
		ROM_LOAD ( "u37-snd", 0x30000, 0x10000, 0x29dbeffd );
		ROM_LOAD ( "u38-snd", 0x40000, 0x10000, 0x09b03b80 );
	
		ROM_REGION( 0x800000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "u94",  0x000000, 0x10000, 0xca3194e4 ); /* even */
		ROM_LOAD ( "u76",  0x200000, 0x10000, 0x1cd897f4 ); /* odd  */
		ROM_LOAD ( "u93",  0x010000, 0x10000, 0x0ed7f7f5 ); /* even */
		ROM_LOAD ( "u75",  0x210000, 0x10000, 0x78abfa01 ); /* odd  */
		ROM_LOAD ( "u92",  0x020000, 0x10000, 0x40d2fc66 ); /* even */
		ROM_LOAD ( "u74",  0x220000, 0x10000, 0x66d2a234 ); /* odd  */
		ROM_LOAD ( "u91",  0x030000, 0x10000, 0xf39325e0 ); /* even */
		ROM_LOAD ( "u73",  0x230000, 0x10000, 0xefa5cd4e ); /* odd  */
		ROM_LOAD ( "u90",  0x040000, 0x10000, 0x0132aefa ); /* even */
		ROM_LOAD ( "u72",  0x240000, 0x10000, 0x70638eb5 ); /* odd  */
		ROM_LOAD ( "u89",  0x050000, 0x10000, 0xf7260c9e ); /* even */
		ROM_LOAD ( "u71",  0x250000, 0x10000, 0x61226883 ); /* odd  */
		ROM_LOAD ( "u88",  0x060000, 0x10000, 0xedc19f42 ); /* even */
		ROM_LOAD ( "u70",  0x260000, 0x10000, 0xc808849f ); /* odd  */
		ROM_LOAD ( "u87",  0x070000, 0x10000, 0xd9b42ff9 ); /* even */
		ROM_LOAD ( "u69",  0x270000, 0x10000, 0xe7f9c34f ); /* odd  */
		ROM_LOAD ( "u86",  0x080000, 0x10000, 0xaf7daad3 ); /* even */
		ROM_LOAD ( "u68",  0x280000, 0x10000, 0x88a634d5 ); /* odd  */
		ROM_LOAD ( "u85",  0x090000, 0x10000, 0x095fae6b ); /* even */
		ROM_LOAD ( "u67",  0x290000, 0x10000, 0x4ab8b69e ); /* odd  */
		ROM_LOAD ( "u84",  0x0a0000, 0x10000, 0x3fdf2057 ); /* even */
		ROM_LOAD ( "u66",  0x2a0000, 0x10000, 0xe1da4b25 ); /* odd  */
		ROM_LOAD ( "u83",  0x0b0000, 0x10000, 0xf2d27c9f ); /* even */
		ROM_LOAD ( "u65",  0x2b0000, 0x10000, 0x6df0d125 ); /* odd  */
		ROM_LOAD ( "u82",  0x0c0000, 0x10000, 0x962ce47c ); /* even */
		ROM_LOAD ( "u64",  0x2c0000, 0x10000, 0xabab1b16 ); /* odd  */
		ROM_LOAD ( "u81",  0x0d0000, 0x10000, 0x00fe59ec ); /* even */
		ROM_LOAD ( "u63",  0x2d0000, 0x10000, 0x80602f31 ); /* odd  */
		ROM_LOAD ( "u80",  0x0e0000, 0x10000, 0x147ba8e9 ); /* even */
		ROM_LOAD ( "u62",  0x2e0000, 0x10000, 0xc2a476d1 ); /* odd  */
	
		ROM_LOAD ( "u58",  0x400000, 0x10000, 0x8a7501e3 ); /* even */
		ROM_LOAD ( "u40",  0x600000, 0x10000, 0x7fcaebc7 ); /* odd  */
		ROM_LOAD ( "u57",  0x410000, 0x10000, 0xa504735f ); /* even */
		ROM_LOAD ( "u39",  0x610000, 0x10000, 0x7db5cf52 ); /* odd  */
		ROM_LOAD ( "u56",  0x420000, 0x10000, 0x55f8cca7 ); /* even */
		ROM_LOAD ( "u38",  0x620000, 0x10000, 0x3f9f3ef7 ); /* odd  */
		ROM_LOAD ( "u55",  0x430000, 0x10000, 0xd3c932c1 ); /* even */
		ROM_LOAD ( "u37",  0x630000, 0x10000, 0xed81826c ); /* odd  */
		ROM_LOAD ( "u54",  0x440000, 0x10000, 0xc7f4134b ); /* even */
		ROM_LOAD ( "u36",  0x640000, 0x10000, 0xe5d855c0 ); /* odd  */
		ROM_LOAD ( "u53",  0x450000, 0x10000, 0x6be4da56 ); /* even */
		ROM_LOAD ( "u35",  0x650000, 0x10000, 0x3a7b1329 ); /* odd  */
		ROM_LOAD ( "u52",  0x460000, 0x10000, 0x1ea36a4a ); /* even */
		ROM_LOAD ( "u34",  0x660000, 0x10000, 0xfe982b0e ); /* odd  */
		ROM_LOAD ( "u51",  0x470000, 0x10000, 0x9d4b0324 ); /* even */
		ROM_LOAD ( "u33",  0x670000, 0x10000, 0x6bc7eb0f ); /* odd  */
		ROM_LOAD ( "u50",  0x480000, 0x10000, 0x6f9f0c26 ); /* even */
		ROM_LOAD ( "u32",  0x680000, 0x10000, 0x5875a6d3 ); /* odd  */
		ROM_LOAD ( "u49",  0x490000, 0x10000, 0x80386fce ); /* even */
		ROM_LOAD ( "u31",  0x690000, 0x10000, 0x2fa4b8e5 ); /* odd  */
		ROM_LOAD ( "u48",  0x4a0000, 0x10000, 0x05c16185 ); /* even */
		ROM_LOAD ( "u30",  0x6a0000, 0x10000, 0x7e4bb8ee ); /* odd  */
		ROM_LOAD ( "u47",  0x4b0000, 0x10000, 0x4c0151f1 ); /* even */
		ROM_LOAD ( "u29",  0x6b0000, 0x10000, 0x45136fd9 ); /* odd  */
		ROM_LOAD ( "u46",  0x4c0000, 0x10000, 0x5670bfcb ); /* even */
		ROM_LOAD ( "u28",  0x6c0000, 0x10000, 0xd6cdac24 ); /* odd  */
		ROM_LOAD ( "u45",  0x4d0000, 0x10000, 0x27f10d98 ); /* even */
		ROM_LOAD ( "u27",  0x6d0000, 0x10000, 0x4d33bbec ); /* odd  */
		ROM_LOAD ( "u44",  0x4e0000, 0x10000, 0x93b8eaa4 ); /* even */
		ROM_LOAD ( "u26",  0x6e0000, 0x10000, 0xcb19f784 ); /* odd  */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_trog = new RomLoadPtr(){ public void handler(){ 	/* released version */
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "trogu105.bin",  0xc0000, 0x20000, 0xe6095189 ) /* even */
		ROM_LOAD_EVEN( "trogu89.bin",   0xc0000, 0x20000, 0xfdd7cc65 ) /* odd */
	
		ROM_REGION( 0x70000, REGION_CPU2 );/* sound CPU */
		ROM_LOAD (   "trogu4.bin", 0x10000, 0x10000, 0x759d0bf4 );
		ROM_LOAD (  "trogu19.bin", 0x30000, 0x10000, 0x960c333d );
		ROM_LOAD (  "trogu20.bin", 0x50000, 0x10000, 0x67f1658a );
	
		ROM_REGION( 0x800000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "trogu111.bin",  0x000000, 0x20000, 0x9ded08c1 ); /* even */
		ROM_LOAD ( "trogu112.bin",  0x020000, 0x20000, 0x42293843 ); /* even */
		ROM_LOAD ( "trogu113.bin",  0x040000, 0x20000, 0x77f50cbb ); /* even */
	
		ROM_LOAD (  "trogu95.bin",  0x200000, 0x20000, 0xf3ba2838 ); /* odd  */
		ROM_LOAD (  "trogu96.bin",  0x220000, 0x20000, 0xcfed2e77 ); /* odd  */
		ROM_LOAD (  "trogu97.bin",  0x240000, 0x20000, 0x3262d1f8 ); /* odd  */
	
		ROM_LOAD ( "trogu106.bin",  0x080000, 0x20000, 0xaf2eb0d8 ); /* even */
	 	ROM_LOAD ( "trogu107.bin",  0x0a0000, 0x20000, 0x88a7b3f6 ); /* even */
	
		ROM_LOAD (  "trogu90.bin",  0x280000, 0x20000, 0x16e06753 ); /* odd  */
		ROM_LOAD (  "trogu91.bin",  0x2a0000, 0x20000, 0x880a02c7 ); /* odd  */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_trog3 = new RomLoadPtr(){ public void handler(){ 	/* released version */
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "u105-la3",  0xc0000, 0x20000, 0xd09cea97 ) /* even */
		ROM_LOAD_EVEN( "u89-la3",   0xc0000, 0x20000, 0xa61e3572 ) /* odd */
	
		ROM_REGION( 0x70000, REGION_CPU2 );/* sound CPU */
		ROM_LOAD (   "trogu4.bin", 0x10000, 0x10000, 0x759d0bf4 );
		ROM_LOAD (  "trogu19.bin", 0x30000, 0x10000, 0x960c333d );
		ROM_LOAD (  "trogu20.bin", 0x50000, 0x10000, 0x67f1658a );
	
		ROM_REGION( 0x800000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "trogu111.bin",  0x000000, 0x20000, 0x9ded08c1 ); /* even */
		ROM_LOAD ( "trogu112.bin",  0x020000, 0x20000, 0x42293843 ); /* even */
		ROM_LOAD ( "trogu113.bin",  0x040000, 0x20000, 0x77f50cbb ); /* even */
	
		ROM_LOAD (  "trogu95.bin",  0x200000, 0x20000, 0xf3ba2838 ); /* odd  */
		ROM_LOAD (  "trogu96.bin",  0x220000, 0x20000, 0xcfed2e77 ); /* odd  */
		ROM_LOAD (  "trogu97.bin",  0x240000, 0x20000, 0x3262d1f8 ); /* odd  */
	
		ROM_LOAD ( "trogu106.bin",  0x080000, 0x20000, 0xaf2eb0d8 ); /* even */
	 	ROM_LOAD ( "trogu107.bin",  0x0a0000, 0x20000, 0x88a7b3f6 ); /* even */
	
		ROM_LOAD (  "trogu90.bin",  0x280000, 0x20000, 0x16e06753 ); /* odd  */
		ROM_LOAD (  "trogu91.bin",  0x2a0000, 0x20000, 0x880a02c7 ); /* odd  */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_trogp = new RomLoadPtr(){ public void handler(){    /* prototype version */
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "trog105.dat",  0xc0000, 0x20000, 0x526a3f5b ) /* even */
		ROM_LOAD_EVEN( "trog89.dat",   0xc0000, 0x20000, 0x38d68685 ) /* odd */
	
		ROM_REGION( 0x70000, REGION_CPU2 );/* sound CPU */
		ROM_LOAD (   "trogu4.bin", 0x10000, 0x10000, 0x759d0bf4 );
		ROM_LOAD (  "trogu19.bin", 0x30000, 0x10000, 0x960c333d );
		ROM_LOAD (  "trogu20.bin", 0x50000, 0x10000, 0x67f1658a );
	
		ROM_REGION( 0x800000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "trogu111.bin",  0x000000, 0x20000, 0x9ded08c1 ); /* even */
		ROM_LOAD ( "trogu112.bin",  0x020000, 0x20000, 0x42293843 ); /* even */
		ROM_LOAD ( "trog113.dat",  0x040000, 0x20000, 0x2980a56f ); /* even */
	
		ROM_LOAD (  "trogu95.bin",  0x200000, 0x20000, 0xf3ba2838 ); /* odd  */
		ROM_LOAD (  "trogu96.bin",  0x220000, 0x20000, 0xcfed2e77 ); /* odd  */
		ROM_LOAD (  "trog97.dat",  0x240000, 0x20000, 0xf94b77c1 ); /* odd  */
	
		ROM_LOAD ( "trogu106.bin",  0x080000, 0x20000, 0xaf2eb0d8 ); /* even */
	 	ROM_LOAD ( "trogu107.bin",  0x0a0000, 0x20000, 0x88a7b3f6 ); /* even */
	
		ROM_LOAD (  "trogu90.bin",  0x280000, 0x20000, 0x16e06753 ); /* odd  */
		ROM_LOAD (  "trogu91.bin",  0x2a0000, 0x20000, 0x880a02c7 ); /* odd  */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_smashtv = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "u105.l8",  0xc0000, 0x20000, 0x48cd793f ) /* even */
		ROM_LOAD_EVEN( "u89.l8",   0xc0000, 0x20000, 0x8e7fe463 ) /* odd */
	
		ROM_REGION( 0x70000, REGION_CPU2 );/* sound CPU */
		ROM_LOAD (  "u4.snd", 0x10000, 0x10000, 0x29d3f6c8 );
		ROM_LOAD ( "u19.snd", 0x30000, 0x10000, 0xac5a402a );
		ROM_LOAD ( "u20.snd", 0x50000, 0x10000, 0x875c66d9 );
	
		ROM_REGION( 0x800000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "u111.gam",  0x000000, 0x20000, 0x72f0ba84 ); /* even */
		ROM_LOAD ( "u112.gam",  0x020000, 0x20000, 0x436f0283 ); /* even */
		ROM_LOAD ( "u113.gam",  0x040000, 0x20000, 0x4a4b8110 ); /* even */
	
		ROM_LOAD (  "u95.gam",  0x200000, 0x20000, 0xe864a44b ); /* odd  */
		ROM_LOAD (  "u96.gam",  0x220000, 0x20000, 0x15555ea7 ); /* odd  */
		ROM_LOAD (  "u97.gam",  0x240000, 0x20000, 0xccac9d9e ); /* odd  */
	
	 	ROM_LOAD ( "u106.gam",  0x400000, 0x20000, 0x5c718361 ); /* even */
	 	ROM_LOAD ( "u107.gam",  0x420000, 0x20000, 0x0fba1e36 ); /* even */
	 	ROM_LOAD ( "u108.gam",  0x440000, 0x20000, 0xcb0a092f ); /* even */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_smashtv6 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "la6-u105",  0xc0000, 0x20000, 0xf1666017 ) /* even */
		ROM_LOAD_EVEN( "la6-u89",   0xc0000, 0x20000, 0x908aca5d ) /* odd */
	
		ROM_REGION( 0x70000, REGION_CPU2 );/* sound CPU */
		ROM_LOAD (  "u4.snd", 0x10000, 0x10000, 0x29d3f6c8 );
		ROM_LOAD ( "u19.snd", 0x30000, 0x10000, 0xac5a402a );
		ROM_LOAD ( "u20.snd", 0x50000, 0x10000, 0x875c66d9 );
	
		ROM_REGION( 0x800000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "u111.gam",  0x000000, 0x20000, 0x72f0ba84 ); /* even */
		ROM_LOAD ( "u112.gam",  0x020000, 0x20000, 0x436f0283 ); /* even */
		ROM_LOAD ( "u113.gam",  0x040000, 0x20000, 0x4a4b8110 ); /* even */
	
		ROM_LOAD (  "u95.gam",  0x200000, 0x20000, 0xe864a44b ); /* odd  */
		ROM_LOAD (  "u96.gam",  0x220000, 0x20000, 0x15555ea7 ); /* odd  */
		ROM_LOAD (  "u97.gam",  0x240000, 0x20000, 0xccac9d9e ); /* odd  */
	
	 	ROM_LOAD ( "u106.gam",  0x400000, 0x20000, 0x5c718361 ); /* even */
	 	ROM_LOAD ( "u107.gam",  0x420000, 0x20000, 0x0fba1e36 ); /* even */
	 	ROM_LOAD ( "u108.gam",  0x440000, 0x20000, 0xcb0a092f ); /* even */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_smashtv5 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "u105-v5",  0xc0000, 0x20000, 0x81f564b9 ) /* even */
		ROM_LOAD_EVEN( "u89-v5",   0xc0000, 0x20000, 0xe5017d25 ) /* odd */
	
		ROM_REGION( 0x70000, REGION_CPU2 );/* sound CPU */
		ROM_LOAD (  "u4.snd", 0x10000, 0x10000, 0x29d3f6c8 );
		ROM_LOAD ( "u19.snd", 0x30000, 0x10000, 0xac5a402a );
		ROM_LOAD ( "u20.snd", 0x50000, 0x10000, 0x875c66d9 );
	
		ROM_REGION( 0x800000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "u111.gam",  0x000000, 0x20000, 0x72f0ba84 ); /* even */
		ROM_LOAD ( "u112.gam",  0x020000, 0x20000, 0x436f0283 ); /* even */
		ROM_LOAD ( "u113.gam",  0x040000, 0x20000, 0x4a4b8110 ); /* even */
	
		ROM_LOAD (  "u95.gam",  0x200000, 0x20000, 0xe864a44b ); /* odd  */
		ROM_LOAD (  "u96.gam",  0x220000, 0x20000, 0x15555ea7 ); /* odd  */
		ROM_LOAD (  "u97.gam",  0x240000, 0x20000, 0xccac9d9e ); /* odd  */
	
	 	ROM_LOAD ( "u106.gam",  0x400000, 0x20000, 0x5c718361 ); /* even */
	 	ROM_LOAD ( "u107.gam",  0x420000, 0x20000, 0x0fba1e36 ); /* even */
	 	ROM_LOAD ( "u108.gam",  0x440000, 0x20000, 0xcb0a092f ); /* even */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_smashtv4 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "la4-u105",  0xc0000, 0x20000, 0xa50ccb71 ) /* even */
		ROM_LOAD_EVEN( "la4-u89",   0xc0000, 0x20000, 0xef0b0279 ) /* odd */
	
		ROM_REGION( 0x70000, REGION_CPU2 );/* sound CPU */
		ROM_LOAD (  "u4.snd", 0x10000, 0x10000, 0x29d3f6c8 );
		ROM_LOAD ( "u19.snd", 0x30000, 0x10000, 0xac5a402a );
		ROM_LOAD ( "u20.snd", 0x50000, 0x10000, 0x875c66d9 );
	
		ROM_REGION( 0x800000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "u111.gam",  0x000000, 0x20000, 0x72f0ba84 ); /* even */
		ROM_LOAD ( "u112.gam",  0x020000, 0x20000, 0x436f0283 ); /* even */
		ROM_LOAD ( "u113.gam",  0x040000, 0x20000, 0x4a4b8110 ); /* even */
	
		ROM_LOAD (  "u95.gam",  0x200000, 0x20000, 0xe864a44b ); /* odd  */
		ROM_LOAD (  "u96.gam",  0x220000, 0x20000, 0x15555ea7 ); /* odd  */
		ROM_LOAD (  "u97.gam",  0x240000, 0x20000, 0xccac9d9e ); /* odd  */
	
	 	ROM_LOAD ( "u106.gam",  0x400000, 0x20000, 0x5c718361 ); /* even */
	 	ROM_LOAD ( "u107.gam",  0x420000, 0x20000, 0x0fba1e36 ); /* even */
	 	ROM_LOAD ( "u108.gam",  0x440000, 0x20000, 0xcb0a092f ); /* even */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_hiimpact = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "la3u105.bin",  0xc0000, 0x20000, 0xb9190c4a ) /* even */
		ROM_LOAD_EVEN( "la3u89.bin",   0xc0000, 0x20000, 0x1cbc72a5 ) /* odd */
	
		ROM_REGION( 0x70000, REGION_CPU2 );/* sound CPU */
		ROM_LOAD (  "sl1u4.bin", 0x10000, 0x20000, 0x28effd6a );
		ROM_LOAD ( "sl1u19.bin", 0x30000, 0x20000, 0x0ea22c89 );
		ROM_LOAD ( "sl1u20.bin", 0x50000, 0x20000, 0x4e747ab5 );
	
		ROM_REGION( 0x800000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "la1u111.bin",  0x000000, 0x20000, 0x49560560 ); /* even */
		ROM_LOAD ( "la1u112.bin",  0x020000, 0x20000, 0x4dd879dc ); /* even */
		ROM_LOAD ( "la1u113.bin",  0x040000, 0x20000, 0xb67aeb70 ); /* even */
		ROM_LOAD ( "la1u114.bin",  0x060000, 0x20000, 0x9a4bc44b ); /* even */
	
		ROM_LOAD (  "la1u95.bin",  0x200000, 0x20000, 0xe1352dc0 ); /* odd  */
		ROM_LOAD (  "la1u96.bin",  0x220000, 0x20000, 0x197d0f34 ); /* odd  */
		ROM_LOAD (  "la1u97.bin",  0x240000, 0x20000, 0x908ea575 ); /* odd  */
		ROM_LOAD (  "la1u98.bin",  0x260000, 0x20000, 0x6dcbab11 ); /* odd  */
	
	 	ROM_LOAD ( "la1u106.bin",  0x400000, 0x20000, 0x7d0ead0d ); /* even */
		ROM_LOAD ( "la1u107.bin",  0x420000, 0x20000, 0xef48e8fa ); /* even */
		ROM_LOAD ( "la1u108.bin",  0x440000, 0x20000, 0x5f363e12 ); /* even */
		ROM_LOAD ( "la1u109.bin",  0x460000, 0x20000, 0x3689fbbc ); /* even */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_shimpact = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "shiu105.bin",  0xc0000, 0x20000, 0xf2cf8de3 ) /* even */
		ROM_LOAD_EVEN( "shiu89.bin",   0xc0000, 0x20000, 0xf97d9b01 ) /* odd */
	
		ROM_REGION( 0x70000, REGION_CPU2 );/* sound CPU */
		ROM_LOAD (   "shiu4.bin", 0x10000, 0x20000, 0x1e5a012c );
		ROM_LOAD (  "shiu19.bin", 0x30000, 0x20000, 0x10f9684e );
		ROM_LOAD (  "shiu20.bin", 0x50000, 0x20000, 0x1b4a71c1 );
	
		ROM_REGION( 0x800000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "shiu111.bin",  0x000000, 0x40000, 0x80ae2a86 );/* even */
		ROM_LOAD ( "shiu112.bin",  0x040000, 0x40000, 0x3ffc27e9 );/* even */
		ROM_LOAD ( "shiu113.bin",  0x080000, 0x40000, 0x01549d00 );/* even */
		ROM_LOAD ( "shiu114.bin",  0x0c0000, 0x40000, 0xa68af319 );/* even */
	
		ROM_LOAD (  "shiu95.bin",  0x200000, 0x40000, 0xe8f56ef5 );/* odd  */
		ROM_LOAD (  "shiu96.bin",  0x240000, 0x40000, 0x24ed04f9 );/* odd  */
		ROM_LOAD (  "shiu97.bin",  0x280000, 0x40000, 0xdd7f41a9 );/* odd  */
		ROM_LOAD (  "shiu98.bin",  0x2c0000, 0x40000, 0x23ef65dd );/* odd  */
	
		ROM_LOAD ( "shiu106.bin",  0x400000, 0x40000, 0x6f5bf337 );/* even */
		ROM_LOAD ( "shiu107.bin",  0x440000, 0x40000, 0xa8815dad );/* even */
		ROM_LOAD ( "shiu108.bin",  0x480000, 0x40000, 0xd39685a3 );/* even */
		ROM_LOAD ( "shiu109.bin",  0x4c0000, 0x40000, 0x36e0b2b2 );/* even */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_strkforc = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "sfu105.bin",  0xc0000, 0x20000, 0x7895e0e3 ) /* even */
		ROM_LOAD_EVEN( "sfu89.bin",   0xc0000, 0x20000, 0x26114d9e ) /* odd */
	
		ROM_REGION( 0x70000, REGION_CPU2 );/* sound CPU */
		ROM_LOAD (  "sfu4.bin", 0x10000, 0x10000, 0x8f747312 );
		ROM_LOAD ( "sfu19.bin", 0x30000, 0x10000, 0xafb29926 );
		ROM_LOAD ( "sfu20.bin", 0x50000, 0x10000, 0x1bc9b746 );
	
		ROM_REGION( 0x800000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "sfu111.bin",  0x000000, 0x20000, 0x878efc80 ); /* even */
		ROM_LOAD ( "sfu112.bin",  0x020000, 0x20000, 0x93394399 ); /* even */
		ROM_LOAD ( "sfu113.bin",  0x040000, 0x20000, 0x9565a79b ); /* even */
		ROM_LOAD ( "sfu114.bin",  0x060000, 0x20000, 0xb71152da ); /* even */
	
		ROM_LOAD (  "sfu95.bin",  0x200000, 0x20000, 0x519cb2b4 ); /* odd  */
		ROM_LOAD (  "sfu96.bin",  0x220000, 0x20000, 0x61214796 ); /* odd  */
		ROM_LOAD (  "sfu97.bin",  0x240000, 0x20000, 0xeb5dee5f ); /* odd  */
		ROM_LOAD (  "sfu98.bin",  0x260000, 0x20000, 0xc5c079e7 ); /* odd  */
	
	 	ROM_LOAD ( "sfu106.bin",  0x080000, 0x20000, 0xa394d4cf ); /* even */
	 	ROM_LOAD ( "sfu107.bin",  0x0a0000, 0x20000, 0xedef1419 ); /* even */
	
		ROM_LOAD (  "sfu90.bin",  0x280000, 0x20000, 0x607bcdc0 ); /* odd  */
		ROM_LOAD (  "sfu91.bin",  0x2a0000, 0x20000, 0xda02547e ); /* odd  */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_mk = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "mkt-uj12.bin",  0x00000, 0x80000, 0xf4990bf2 )  /* even */
		ROM_LOAD_EVEN( "mkt-ug12.bin",  0x00000, 0x80000, 0xb06aeac1 )  /* odd  */
	
		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
		ROM_LOAD (  "mks-u3.rom", 0x10000, 0x40000, 0xc615844c );
	
		ROM_REGION( 0xc00000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "mkt-ug14.bin",  0x000000, 0x80000, 0x9e00834e ); /* even */
		ROM_LOAD ( "mkt-ug16.bin",  0x080000, 0x80000, 0x52c9d1e5 ); /* even */
		ROM_LOAD ( "mkt-ug17.bin",  0x100000, 0x80000, 0xe34fe253 ); /* even */
	
		ROM_LOAD ( "mkt-uj14.bin",  0x300000, 0x80000, 0xf4b0aaa7 ); /* odd  */
		ROM_LOAD ( "mkt-uj16.bin",  0x380000, 0x80000, 0xc94c58cf ); /* odd  */
		ROM_LOAD ( "mkt-uj17.bin",  0x400000, 0x80000, 0xa56e12f5 ); /* odd  */
	
		ROM_LOAD ( "mkt-ug19.bin",  0x600000, 0x80000, 0x2d8c7ba1 ); /* even */
		ROM_LOAD ( "mkt-ug20.bin",  0x680000, 0x80000, 0x2f7e55d3 ); /* even */
		ROM_LOAD ( "mkt-ug22.bin",  0x700000, 0x80000, 0xb537bb4e ); /* even */
	
		ROM_LOAD ( "mkt-uj19.bin",  0x900000, 0x80000, 0x33b9b7a4 ); /* odd  */
		ROM_LOAD ( "mkt-uj20.bin",  0x980000, 0x80000, 0xeae96df0 ); /* odd  */
		ROM_LOAD ( "mkt-uj22.bin",  0xa00000, 0x80000, 0x5e12523b ); /* odd  */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* sound */
		ROM_LOAD ( "mks-u12.rom", 0x00000, 0x40000, 0x258bd7f9 );
		ROM_LOAD ( "mks-u13.rom", 0x40000, 0x40000, 0x7b7ec3b6 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_mkla1 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "mkg-u105.la1",  0x00000, 0x80000, 0xe1f7b4c9 )  /* even */
		ROM_LOAD_EVEN(  "mkg-u89.la1",  0x00000, 0x80000, 0x9d38ac75 )  /* odd  */
	
		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
		ROM_LOAD (  "mks-u3.rom", 0x10000, 0x40000, 0xc615844c );
	
		ROM_REGION( 0x800000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "mkg-u111.rom",  0x000000, 0x80000, 0xd17096c4 ); /* even */
		ROM_LOAD ( "mkg-u112.rom",  0x080000, 0x80000, 0x993bc2e4 ); /* even */
		ROM_LOAD ( "mkg-u113.rom",  0x100000, 0x80000, 0x6fb91ede ); /* even */
		ROM_LOAD ( "mkg-u114.rom",  0x180000, 0x80000, 0xed1ff88a ); /* even */
	
	 	ROM_LOAD (  "mkg-u95.rom",  0x200000, 0x80000, 0xa002a155 ); /* odd  */
	 	ROM_LOAD (  "mkg-u96.rom",  0x280000, 0x80000, 0xdcee8492 ); /* odd  */
		ROM_LOAD (  "mkg-u97.rom",  0x300000, 0x80000, 0xde88caef ); /* odd  */
		ROM_LOAD (  "mkg-u98.rom",  0x380000, 0x80000, 0x37eb01b4 ); /* odd  */
	
		ROM_LOAD ( "mkg-u106.rom",  0x400000, 0x80000, 0x45acaf21 ); /* even */
	 	ROM_LOAD ( "mkg-u107.rom",  0x480000, 0x80000, 0x2a6c10a0 ); /* even */
	  	ROM_LOAD ( "mkg-u108.rom",  0x500000, 0x80000, 0x23308979 ); /* even */
	 	ROM_LOAD ( "mkg-u109.rom",  0x580000, 0x80000, 0xcafc47bb ); /* even */
	
		ROM_REGION( 0xc0000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD ( "mks-u12.rom", 0x00000, 0x40000, 0x258bd7f9 );
		ROM_LOAD ( "mks-u13.rom", 0x40000, 0x40000, 0x7b7ec3b6 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_mkla2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "mkg-u105.la2",  0x00000, 0x80000, 0x8531d44e )  /* even */
		ROM_LOAD_EVEN(  "mkg-u89.la2",  0x00000, 0x80000, 0xb88dc26e )  /* odd  */
	
		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
		ROM_LOAD (  "mks-u3.rom", 0x10000, 0x40000, 0xc615844c );
	
		ROM_REGION( 0x800000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "mkg-u111.rom",  0x000000, 0x80000, 0xd17096c4 ); /* even */
		ROM_LOAD ( "mkg-u112.rom",  0x080000, 0x80000, 0x993bc2e4 ); /* even */
		ROM_LOAD ( "mkg-u113.rom",  0x100000, 0x80000, 0x6fb91ede ); /* even */
		ROM_LOAD ( "mkg-u114.rom",  0x180000, 0x80000, 0xed1ff88a ); /* even */
	
	 	ROM_LOAD (  "mkg-u95.rom",  0x200000, 0x80000, 0xa002a155 ); /* odd  */
	 	ROM_LOAD (  "mkg-u96.rom",  0x280000, 0x80000, 0xdcee8492 ); /* odd  */
		ROM_LOAD (  "mkg-u97.rom",  0x300000, 0x80000, 0xde88caef ); /* odd  */
		ROM_LOAD (  "mkg-u98.rom",  0x380000, 0x80000, 0x37eb01b4 ); /* odd  */
	
		ROM_LOAD ( "mkg-u106.rom",  0x400000, 0x80000, 0x45acaf21 ); /* even */
	 	ROM_LOAD ( "mkg-u107.rom",  0x480000, 0x80000, 0x2a6c10a0 ); /* even */
	  	ROM_LOAD ( "mkg-u108.rom",  0x500000, 0x80000, 0x23308979 ); /* even */
	 	ROM_LOAD ( "mkg-u109.rom",  0x580000, 0x80000, 0xcafc47bb ); /* even */
	
		ROM_REGION( 0xc0000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD ( "mks-u12.rom", 0x00000, 0x40000, 0x258bd7f9 );
		ROM_LOAD ( "mks-u13.rom", 0x40000, 0x40000, 0x7b7ec3b6 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_mkla3 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "mkg-u105.la3",  0x00000, 0x80000, 0x2ce843c5 )  /* even */
		ROM_LOAD_EVEN(  "mkg-u89.la3",  0x00000, 0x80000, 0x49a46e10 )  /* odd  */
	
		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
		ROM_LOAD (  "mks-u3.rom", 0x10000, 0x40000, 0xc615844c );
	
		ROM_REGION( 0x800000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "mkg-u111.rom",  0x000000, 0x80000, 0xd17096c4 ); /* even */
		ROM_LOAD ( "mkg-u112.rom",  0x080000, 0x80000, 0x993bc2e4 ); /* even */
		ROM_LOAD ( "mkg-u113.rom",  0x100000, 0x80000, 0x6fb91ede ); /* even */
		ROM_LOAD ( "mkg-u114.rom",  0x180000, 0x80000, 0xed1ff88a ); /* even */
	
	 	ROM_LOAD (  "mkg-u95.rom",  0x200000, 0x80000, 0xa002a155 ); /* odd  */
	 	ROM_LOAD (  "mkg-u96.rom",  0x280000, 0x80000, 0xdcee8492 ); /* odd  */
		ROM_LOAD (  "mkg-u97.rom",  0x300000, 0x80000, 0xde88caef ); /* odd  */
		ROM_LOAD (  "mkg-u98.rom",  0x380000, 0x80000, 0x37eb01b4 ); /* odd  */
	
		ROM_LOAD ( "mkg-u106.rom",  0x400000, 0x80000, 0x45acaf21 ); /* even */
	 	ROM_LOAD ( "mkg-u107.rom",  0x480000, 0x80000, 0x2a6c10a0 ); /* even */
	  	ROM_LOAD ( "mkg-u108.rom",  0x500000, 0x80000, 0x23308979 ); /* even */
	 	ROM_LOAD ( "mkg-u109.rom",  0x580000, 0x80000, 0xcafc47bb ); /* even */
	
		ROM_REGION( 0xc0000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD ( "mks-u12.rom", 0x00000, 0x40000, 0x258bd7f9 );
		ROM_LOAD ( "mks-u13.rom", 0x40000, 0x40000, 0x7b7ec3b6 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_mkla4 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "mkg-u105.la4",  0x00000, 0x80000, 0x29af348f )  /* even */
		ROM_LOAD_EVEN(  "mkg-u89.la4",  0x00000, 0x80000, 0x1ad76662 )  /* odd  */
	
		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
		ROM_LOAD (  "mks-u3.rom", 0x10000, 0x40000, 0xc615844c );
	
		ROM_REGION( 0x800000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "mkg-u111.rom",  0x000000, 0x80000, 0xd17096c4 ); /* even */
		ROM_LOAD ( "mkg-u112.rom",  0x080000, 0x80000, 0x993bc2e4 ); /* even */
		ROM_LOAD ( "mkg-u113.rom",  0x100000, 0x80000, 0x6fb91ede ); /* even */
		ROM_LOAD ( "mkg-u114.rom",  0x180000, 0x80000, 0xed1ff88a ); /* even */
	
	 	ROM_LOAD (  "mkg-u95.rom",  0x200000, 0x80000, 0xa002a155 ); /* odd  */
	 	ROM_LOAD (  "mkg-u96.rom",  0x280000, 0x80000, 0xdcee8492 ); /* odd  */
		ROM_LOAD (  "mkg-u97.rom",  0x300000, 0x80000, 0xde88caef ); /* odd  */
		ROM_LOAD (  "mkg-u98.rom",  0x380000, 0x80000, 0x37eb01b4 ); /* odd  */
	
		ROM_LOAD ( "mkg-u106.rom",  0x400000, 0x80000, 0x45acaf21 ); /* even */
	 	ROM_LOAD ( "mkg-u107.rom",  0x480000, 0x80000, 0x2a6c10a0 ); /* even */
	  	ROM_LOAD ( "mkg-u108.rom",  0x500000, 0x80000, 0x23308979 ); /* even */
	 	ROM_LOAD ( "mkg-u109.rom",  0x580000, 0x80000, 0xcafc47bb ); /* even */
	
		ROM_REGION( 0xc0000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD ( "mks-u12.rom", 0x00000, 0x40000, 0x258bd7f9 );
		ROM_LOAD ( "mks-u13.rom", 0x40000, 0x40000, 0x7b7ec3b6 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_term2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "t2.105",  0x00000, 0x80000, 0x34142b28 )  /* even */
		ROM_LOAD_EVEN( "t2.89",   0x00000, 0x80000, 0x5ffea427 )  /* odd  */
	
		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
		ROM_LOAD (  "t2_snd.3", 0x10000, 0x20000, 0x73c3f5c4 );
		ROM_RELOAD (            0x30000, 0x20000 );
	
		ROM_REGION( 0x800000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "t2.111",  0x000000, 0x80000, 0x916d0197 ); /* even */
		ROM_LOAD ( "t2.112",  0x080000, 0x80000, 0x39ae1c87 ); /* even */
		ROM_LOAD ( "t2.113",  0x100000, 0x80000, 0xcb5084e5 ); /* even */
		ROM_LOAD ( "t2.114",  0x180000, 0x80000, 0x53c516ec ); /* even */
	
		ROM_LOAD (  "t2.95",  0x200000, 0x80000, 0xdd39cf73 ); /* odd  */
		ROM_LOAD (  "t2.96",  0x280000, 0x80000, 0x31f4fd36 ); /* odd  */
		ROM_LOAD (  "t2.97",  0x300000, 0x80000, 0x7f72e775 ); /* odd  */
		ROM_LOAD (  "t2.98",  0x380000, 0x80000, 0x1a20ce29 ); /* odd  */
	
		ROM_LOAD ( "t2.106",  0x400000, 0x80000, 0xf08a9536 ); /* even */
	 	ROM_LOAD ( "t2.107",  0x480000, 0x80000, 0x268d4035 ); /* even */
	 	ROM_LOAD ( "t2.108",  0x500000, 0x80000, 0x379fdaed ); /* even */
	 	ROM_LOAD ( "t2.109",  0x580000, 0x80000, 0x306a9366 ); /* even */
	
		ROM_REGION( 0xc0000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD ( "t2_snd.12", 0x00000, 0x40000, 0xe192a40d );
		ROM_LOAD ( "t2_snd.13", 0x40000, 0x40000, 0x956fa80b );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_totcarn = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "tcu105.bin",  0x80000, 0x40000, 0x7c651047 )  /* even */
		ROM_LOAD_EVEN( "tcu89.bin",   0x80000, 0x40000, 0x6761daf3 )  /* odd  */
	
		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
		ROM_LOAD (  "tcu3.bin", 0x10000, 0x20000, 0x5bdb4665 );
		ROM_RELOAD (            0x30000, 0x20000 );
	
		ROM_REGION( 0x800000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "tcu111.bin",  0x000000, 0x40000, 0x13f3f231 ); /* even */
		ROM_LOAD ( "tcu112.bin",  0x040000, 0x40000, 0x72e45007 ); /* even */
		ROM_LOAD ( "tcu113.bin",  0x080000, 0x40000, 0x2c8ec753 ); /* even */
		ROM_LOAD ( "tcu114.bin",  0x0c0000, 0x40000, 0x6210c36c ); /* even */
	
		ROM_LOAD (  "tcu95.bin",  0x200000, 0x40000, 0x579caeba ); /* odd  */
		ROM_LOAD (  "tcu96.bin",  0x240000, 0x40000, 0xf43f1ffe ); /* odd  */
		ROM_LOAD (  "tcu97.bin",  0x280000, 0x40000, 0x1675e50d ); /* odd  */
		ROM_LOAD (  "tcu98.bin",  0x2c0000, 0x40000, 0xab06c885 ); /* odd  */
	
		ROM_LOAD ( "tcu106.bin",  0x400000, 0x40000, 0x146e3863 ); /* even */
	 	ROM_LOAD ( "tcu107.bin",  0x440000, 0x40000, 0x95323320 ); /* even */
	 	ROM_LOAD ( "tcu108.bin",  0x480000, 0x40000, 0xed152acc ); /* even */
	 	ROM_LOAD ( "tcu109.bin",  0x4c0000, 0x40000, 0x80715252 ); /* even */
	
		ROM_REGION( 0xc0000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD ( "tcu12.bin", 0x00000, 0x40000, 0xd0000ac7 );
		ROM_LOAD ( "tcu13.bin", 0x40000, 0x40000, 0xe48e6f0c );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_totcarnp = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "u105",  0x80000, 0x40000, 0x7a782cae )  /* even */
		ROM_LOAD_EVEN( "u89",   0x80000, 0x40000, 0x1c899a8d )  /* odd  */
	
		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
		ROM_LOAD (  "tcu3.bin", 0x10000, 0x20000, 0x5bdb4665 );
		ROM_RELOAD (            0x30000, 0x20000 );
	
		ROM_REGION( 0x800000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "tcu111.bin",  0x000000, 0x40000, 0x13f3f231 ); /* even */
		ROM_LOAD ( "tcu112.bin",  0x040000, 0x40000, 0x72e45007 ); /* even */
		ROM_LOAD ( "tcu113.bin",  0x080000, 0x40000, 0x2c8ec753 ); /* even */
		ROM_LOAD ( "tcu114.bin",  0x0c0000, 0x40000, 0x6210c36c ); /* even */
	
		ROM_LOAD (  "tcu95.bin",  0x200000, 0x40000, 0x579caeba ); /* odd  */
		ROM_LOAD (  "tcu96.bin",  0x240000, 0x40000, 0xf43f1ffe ); /* odd  */
		ROM_LOAD (  "tcu97.bin",  0x280000, 0x40000, 0x1675e50d ); /* odd  */
		ROM_LOAD (  "tcu98.bin",  0x2c0000, 0x40000, 0xab06c885 ); /* odd  */
	
		ROM_LOAD ( "tcu106.bin",  0x400000, 0x40000, 0x146e3863 ); /* even */
	 	ROM_LOAD ( "tcu107.bin",  0x440000, 0x40000, 0x95323320 ); /* even */
	 	ROM_LOAD ( "tcu108.bin",  0x480000, 0x40000, 0xed152acc ); /* even */
	 	ROM_LOAD ( "tcu109.bin",  0x4c0000, 0x40000, 0x80715252 ); /* even */
	
		ROM_REGION( 0xc0000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD ( "tcu12.bin", 0x00000, 0x40000, 0xd0000ac7 );
		ROM_LOAD ( "tcu13.bin", 0x40000, 0x40000, 0xe48e6f0c );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_mk2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "uj12.l31",  0x00000, 0x80000, 0xcf100a75 )  /* even */
		ROM_LOAD_EVEN( "ug12.l31",  0x00000, 0x80000, 0x582c7dfd )  /* odd  */
	
		ROM_REGION( 0xc00000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "ug14-vid",  0x000000, 0x100000, 0x01e73af6 ); /* even */
		ROM_LOAD ( "ug16-vid",  0x200000, 0x100000, 0x8ba6ae18 ); /* even */
		ROM_LOAD ( "ug17-vid",  0x100000, 0x100000, 0x937d8620 ); /* even */
	
		ROM_LOAD ( "uj14-vid",  0x300000, 0x100000, 0xd4985cbb ); /* odd  */
		ROM_LOAD ( "uj16-vid",  0x500000, 0x100000, 0x39d885b4 ); /* odd  */
		ROM_LOAD ( "uj17-vid",  0x400000, 0x100000, 0x218de160 ); /* odd  */
	
		ROM_LOAD ( "ug19-vid",  0x600000, 0x100000, 0xfec137be ); /* even */
		ROM_LOAD ( "ug20-vid",  0x800000, 0x100000, 0x809118c1 ); /* even */
		ROM_LOAD ( "ug22-vid",  0x700000, 0x100000, 0x154d53b1 ); /* even */
	
		ROM_LOAD ( "uj19-vid",  0x900000, 0x100000, 0x2d763156 ); /* odd  */
		ROM_LOAD ( "uj20-vid",  0xb00000, 0x100000, 0xb96824f0 ); /* odd  */
		ROM_LOAD ( "uj22-vid",  0xa00000, 0x100000, 0x8891d785 ); /* odd  */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* sound */
		ROM_LOAD (   "su2.l1",  0x000000, 0x80000, 0x5f23d71d );
		ROM_LOAD (   "su3.l1",  0x080000, 0x80000, 0xd6d92bf9 );
		ROM_LOAD (   "su4.l1",  0x000000, 0x80000, 0xeebc8e0f );
		ROM_LOAD (   "su5.l1",  0x080000, 0x80000, 0x2b0b7961 );
		ROM_LOAD (   "su6.l1",  0x000000, 0x80000, 0xf694b27f );
		ROM_LOAD (   "su7.l1",  0x080000, 0x80000, 0x20387e0a );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_mk2r32 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "uj12.l32",  0x00000, 0x80000, 0x43f773a6 )  /* even */
		ROM_LOAD_EVEN( "ug12.l32",  0x00000, 0x80000, 0xdcde9619 )  /* odd  */
	
		ROM_REGION( 0xc00000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "ug14-vid",  0x000000, 0x100000, 0x01e73af6 ); /* even */
		ROM_LOAD ( "ug16-vid",  0x200000, 0x100000, 0x8ba6ae18 ); /* even */
		ROM_LOAD ( "ug17-vid",  0x100000, 0x100000, 0x937d8620 ); /* even */
	
		ROM_LOAD ( "uj14-vid",  0x300000, 0x100000, 0xd4985cbb ); /* odd  */
		ROM_LOAD ( "uj16-vid",  0x500000, 0x100000, 0x39d885b4 ); /* odd  */
		ROM_LOAD ( "uj17-vid",  0x400000, 0x100000, 0x218de160 ); /* odd  */
	
		ROM_LOAD ( "ug19-vid",  0x600000, 0x100000, 0xfec137be ); /* even */
		ROM_LOAD ( "ug20-vid",  0x800000, 0x100000, 0x809118c1 ); /* even */
		ROM_LOAD ( "ug22-vid",  0x700000, 0x100000, 0x154d53b1 ); /* even */
	
		ROM_LOAD ( "uj19-vid",  0x900000, 0x100000, 0x2d763156 ); /* odd  */
		ROM_LOAD ( "uj20-vid",  0xb00000, 0x100000, 0xb96824f0 ); /* odd  */
		ROM_LOAD ( "uj22-vid",  0xa00000, 0x100000, 0x8891d785 ); /* odd  */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* sound */
		ROM_LOAD (   "su2.l1",  0x000000, 0x80000, 0x5f23d71d );
		ROM_LOAD (   "su3.l1",  0x080000, 0x80000, 0xd6d92bf9 );
		ROM_LOAD (   "su4.l1",  0x000000, 0x80000, 0xeebc8e0f );
		ROM_LOAD (   "su5.l1",  0x080000, 0x80000, 0x2b0b7961 );
		ROM_LOAD (   "su6.l1",  0x000000, 0x80000, 0xf694b27f );
		ROM_LOAD (   "su7.l1",  0x080000, 0x80000, 0x20387e0a );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_mk2r14 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "uj12.l14",  0x00000, 0x80000, 0x6d43bc6d )  /* even */
		ROM_LOAD_EVEN( "ug12.l14",  0x00000, 0x80000, 0x42b0da21 )  /* odd  */
	
		ROM_REGION( 0xc00000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "ug14-vid",  0x000000, 0x100000, 0x01e73af6 ); /* even */
		ROM_LOAD ( "ug16-vid",  0x200000, 0x100000, 0x8ba6ae18 ); /* even */
		ROM_LOAD ( "ug17-vid",  0x100000, 0x100000, 0x937d8620 ); /* even */
	
		ROM_LOAD ( "uj14-vid",  0x300000, 0x100000, 0xd4985cbb ); /* odd  */
		ROM_LOAD ( "uj16-vid",  0x500000, 0x100000, 0x39d885b4 ); /* odd  */
		ROM_LOAD ( "uj17-vid",  0x400000, 0x100000, 0x218de160 ); /* odd  */
	
		ROM_LOAD ( "ug19-vid",  0x600000, 0x100000, 0xfec137be ); /* even */
		ROM_LOAD ( "ug20-vid",  0x800000, 0x100000, 0x809118c1 ); /* even */
		ROM_LOAD ( "ug22-vid",  0x700000, 0x100000, 0x154d53b1 ); /* even */
	
		ROM_LOAD ( "uj19-vid",  0x900000, 0x100000, 0x2d763156 ); /* odd  */
		ROM_LOAD ( "uj20-vid",  0xb00000, 0x100000, 0xb96824f0 ); /* odd  */
		ROM_LOAD ( "uj22-vid",  0xa00000, 0x100000, 0x8891d785 ); /* odd  */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* sound */
		ROM_LOAD (   "su2.l1",  0x000000, 0x80000, 0x5f23d71d );
		ROM_LOAD (   "su3.l1",  0x080000, 0x80000, 0xd6d92bf9 );
		ROM_LOAD (   "su4.l1",  0x000000, 0x80000, 0xeebc8e0f );
		ROM_LOAD (   "su5.l1",  0x080000, 0x80000, 0x2b0b7961 );
		ROM_LOAD (   "su6.l1",  0x000000, 0x80000, 0xf694b27f );
		ROM_LOAD (   "su7.l1",  0x080000, 0x80000, 0x20387e0a );
	ROM_END(); }}; 
	
	/*
	    equivalences for the extension board version (same contents, split in half)
	
		ROM_LOAD ( "ug14.l1",   0x000000, 0x080000, 0x74f5aaf1 );
		ROM_LOAD ( "ug16.l11",  0x080000, 0x080000, 0x1cf58c4c );
		ROM_LOAD ( "u8.l1",     0x200000, 0x080000, 0x56e22ff5 );
		ROM_LOAD ( "u11.l1",    0x280000, 0x080000, 0x559ca4a3 );
		ROM_LOAD ( "ug17.l1",   0x100000, 0x080000, 0x4202d8bf );
		ROM_LOAD ( "ug18.l1",   0x180000, 0x080000, 0xa3deab6a );
	
		ROM_LOAD ( "uj14.l1",   0x300000, 0x080000, 0x869a3c55 );
		ROM_LOAD ( "uj16.l11",  0x380000, 0x080000, 0xc70cf053 );
		ROM_LOAD ( "u9.l1",     0x500000, 0x080000, 0x67da0769 );
		ROM_LOAD ( "u10.l1",    0x580000, 0x080000, 0x69000ac3 );
		ROM_LOAD ( "uj17.l1",   0x400000, 0x080000, 0xec3e1884 );
		ROM_LOAD ( "uj18.l1",   0x480000, 0x080000, 0xc9f5aef4 );
	
		ROM_LOAD ( "u6.l1",     0x600000, 0x080000, 0x8d4c496a );
		ROM_LOAD ( "u13.l11",   0x680000, 0x080000, 0x7fb20a45 );
		ROM_LOAD ( "ug19.l1",   0x800000, 0x080000, 0xd6c1f75e );
		ROM_LOAD ( "ug20.l1",   0x880000, 0x080000, 0x19a33cff );
		ROM_LOAD ( "ug22.l1",   0x700000, 0x080000, 0xdb6cfa45 );
		ROM_LOAD ( "ug23.l1",   0x780000, 0x080000, 0xbfd8b656 );
	
		ROM_LOAD ( "u7.l1",     0x900000, 0x080000, 0x3988aac8 );
		ROM_LOAD ( "u12.l11",   0x980000, 0x080000, 0x2ef12cc6 );
		ROM_LOAD ( "uj19.l1",   0xb00000, 0x080000, 0x4eed6f18 );
		ROM_LOAD ( "uj20.l1",   0xb80000, 0x080000, 0x337b1e20 );
		ROM_LOAD ( "uj22.l1",   0xa00000, 0x080000, 0xa6546b15 );
		ROM_LOAD ( "uj23.l1",   0xa80000, 0x080000, 0x45867c6f );
	*/
	
	
	static RomLoadPtr rom_nbajam = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "nbauj12.bin",  0x00000, 0x80000, 0xb93e271c )  /* even */
		ROM_LOAD_EVEN( "nbaug12.bin",  0x00000, 0x80000, 0x407d3390 )  /* odd  */
	
		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
		ROM_LOAD (  "nbau3.bin",  0x010000, 0x20000, 0x3a3ea480 );
		ROM_RELOAD (              0x030000, 0x20000 );
	
		ROM_REGION( 0xc00000, REGION_GFX1 );      /* graphics - get disposed of later */
		ROM_LOAD ( "nbaug14.bin",  0x000000, 0x80000, 0x04bb9f64 ); /* even */
		ROM_LOAD ( "nbaug16.bin",  0x080000, 0x80000, 0x8591c572 ); /* even */
		ROM_LOAD ( "nbaug17.bin",  0x100000, 0x80000, 0x6f921886 ); /* even */
		ROM_LOAD ( "nbaug18.bin",  0x180000, 0x80000, 0x5162d3d6 ); /* even */
	
		ROM_LOAD ( "nbauj14.bin",  0x300000, 0x80000, 0xb34b7af3 ); /* odd  */
		ROM_LOAD ( "nbauj16.bin",  0x380000, 0x80000, 0xd2e554f1 ); /* odd  */
		ROM_LOAD ( "nbauj17.bin",  0x400000, 0x80000, 0xb2e14981 ); /* odd  */
		ROM_LOAD ( "nbauj18.bin",  0x480000, 0x80000, 0xfdee0037 ); /* odd  */
	
		ROM_LOAD ( "nbaug19.bin",  0x600000, 0x80000, 0xa8f22fbb ); /* even */
		ROM_LOAD ( "nbaug20.bin",  0x680000, 0x80000, 0x44fd6221 ); /* even */
		ROM_LOAD ( "nbaug22.bin",  0x700000, 0x80000, 0xab05ed89 ); /* even */
		ROM_LOAD ( "nbaug23.bin",  0x780000, 0x80000, 0x7b934c7a ); /* even */
	
		ROM_LOAD ( "nbauj19.bin",  0x900000, 0x80000, 0x8130a8a2 ); /* odd  */
		ROM_LOAD ( "nbauj20.bin",  0x980000, 0x80000, 0xf9cebbb6 ); /* odd  */
		ROM_LOAD ( "nbauj22.bin",  0xa00000, 0x80000, 0x59a95878 ); /* odd  */
		ROM_LOAD ( "nbauj23.bin",  0xa80000, 0x80000, 0x427d2eee ); /* odd  */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM sample */
		ROM_LOAD ( "nbau12.bin",  0x000000, 0x80000, 0xb94847f1 );
		ROM_LOAD ( "nbau13.bin",  0x080000, 0x80000, 0xb6fe24bd );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_nbajamr2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "jam2uj12.bin",  0x00000, 0x80000, 0x0fe80b36 )  /* even */
		ROM_LOAD_EVEN( "jam2ug12.bin",  0x00000, 0x80000, 0x5d106315 )  /* odd  */
	
		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
		ROM_LOAD (  "nbau3.bin",  0x010000, 0x20000, 0x3a3ea480 );
		ROM_RELOAD (              0x030000, 0x20000 );
	
		ROM_REGION( 0xc00000, REGION_GFX1 );     /* graphics */
		ROM_LOAD ( "nbaug14.bin",  0x000000, 0x80000, 0x04bb9f64 ); /* even */
		ROM_LOAD ( "nbaug16.bin",  0x080000, 0x80000, 0x8591c572 ); /* even */
		ROM_LOAD ( "nbaug17.bin",  0x100000, 0x80000, 0x6f921886 ); /* even */
		ROM_LOAD ( "nbaug18.bin",  0x180000, 0x80000, 0x5162d3d6 ); /* even */
	
		ROM_LOAD ( "nbauj14.bin",  0x300000, 0x80000, 0xb34b7af3 ); /* odd  */
		ROM_LOAD ( "nbauj16.bin",  0x380000, 0x80000, 0xd2e554f1 ); /* odd  */
		ROM_LOAD ( "nbauj17.bin",  0x400000, 0x80000, 0xb2e14981 ); /* odd  */
		ROM_LOAD ( "nbauj18.bin",  0x480000, 0x80000, 0xfdee0037 ); /* odd  */
	
		ROM_LOAD ( "nbaug19.bin",  0x600000, 0x80000, 0xa8f22fbb ); /* even */
		ROM_LOAD ( "nbaug20.bin",  0x680000, 0x80000, 0x44fd6221 ); /* even */
		ROM_LOAD ( "nbaug22.bin",  0x700000, 0x80000, 0xab05ed89 ); /* even */
		ROM_LOAD ( "nbaug23.bin",  0x780000, 0x80000, 0x7b934c7a ); /* even */
	
		ROM_LOAD ( "nbauj19.bin",  0x900000, 0x80000, 0x8130a8a2 ); /* odd  */
		ROM_LOAD ( "nbauj20.bin",  0x980000, 0x80000, 0xf9cebbb6 ); /* odd  */
		ROM_LOAD ( "nbauj22.bin",  0xa00000, 0x80000, 0x59a95878 ); /* odd  */
		ROM_LOAD ( "nbauj23.bin",  0xa80000, 0x80000, 0x427d2eee ); /* odd  */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM sample */
		ROM_LOAD ( "nbau12.bin",  0x000000, 0x80000, 0xb94847f1 );
		ROM_LOAD ( "nbau13.bin",  0x080000, 0x80000, 0xb6fe24bd );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_nbajamte = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 34010 code */
		ROM_LOAD_ODD ( "te-uj12.bin",  0x00000, 0x80000, 0xd7c21bc4 )  /* even */
		ROM_LOAD_EVEN( "te-ug12.bin",  0x00000, 0x80000, 0x7ad49229 )  /* odd  */
	
		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
		ROM_LOAD (  "te-u3.bin",  0x010000, 0x20000, 0xd4551195 );
		ROM_RELOAD (              0x030000, 0x20000 );
	
		ROM_REGION( 0xc00000, REGION_GFX1 );     /* graphics */
		ROM_LOAD ( "nbaug14.bin",  0x000000, 0x80000, 0x04bb9f64 ); /* even  same as nbajam */
		ROM_LOAD ( "te-ug16.bin",  0x080000, 0x80000, 0xc7ce74d0 ); /* even */
		ROM_LOAD ( "te-ug17.bin",  0x100000, 0x80000, 0x9401be62 ); /* even */
		ROM_LOAD ( "te-ug18.bin",  0x180000, 0x80000, 0x6fd08f57 ); /* even */
	
		ROM_LOAD ( "nbauj14.bin",  0x300000, 0x80000, 0xb34b7af3 ); /* odd  same as nbajam */
		ROM_LOAD ( "te-uj16.bin",  0x380000, 0x80000, 0x905ad88b ); /* odd  */
		ROM_LOAD ( "te-uj17.bin",  0x400000, 0x80000, 0x44cf3151 ); /* odd  */
		ROM_LOAD ( "te-uj18.bin",  0x480000, 0x80000, 0x4eb73c26 ); /* odd  */
	
		ROM_LOAD ( "nbaug19.bin",  0x600000, 0x80000, 0xa8f22fbb ); /* even  same as nbajam */
		ROM_LOAD ( "te-ug20.bin",  0x680000, 0x80000, 0x8a48728c ); /* even */
		ROM_LOAD ( "te-ug22.bin",  0x700000, 0x80000, 0x3b05133b ); /* even */
		ROM_LOAD ( "te-ug23.bin",  0x780000, 0x80000, 0x854f73bc ); /* even */
	
		ROM_LOAD ( "nbauj19.bin",  0x900000, 0x80000, 0x8130a8a2 ); /* odd  same as nbajam */
		ROM_LOAD ( "te-uj20.bin",  0x980000, 0x80000, 0xbf263d61 ); /* odd  */
		ROM_LOAD ( "te-uj22.bin",  0xa00000, 0x80000, 0x39791051 ); /* odd  */
		ROM_LOAD ( "te-uj23.bin",  0xa80000, 0x80000, 0xf8c30998 ); /* odd  */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM sample */
		ROM_LOAD ( "te-u12.bin",  0x000000, 0x80000, 0x4fac97bc );
		ROM_LOAD ( "te-u13.bin",  0x080000, 0x80000, 0x6f27b202 );
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_narc	   = new GameDriver("1988"	,"narc"	,"smashtv.java"	,rom_narc,null	,machine_driver_narc	,input_ports_narc	,init_narc	,ROT0_16BIT	,	"Williams", "Narc (rev 7.00)" )
	GAMEX(1988, narc3,    narc,    narc,    narc,    narc,     ROT0_16BIT, "Williams", "Narc (rev 3.00)", GAME_NOT_WORKING )
	public static GameDriver driver_smashtv	   = new GameDriver("1990"	,"smashtv"	,"smashtv.java"	,rom_smashtv,null	,machine_driver_smashtv	,input_ports_smashtv	,init_smashtv	,ROT0_16BIT	,	"Williams", "Smash T.V. (rev 8.00)" )
	public static GameDriver driver_smashtv6	   = new GameDriver("1990"	,"smashtv6"	,"smashtv.java"	,rom_smashtv6,driver_smashtv	,machine_driver_smashtv	,input_ports_smashtv	,init_smashtv	,ROT0_16BIT	,	"Williams", "Smash T.V. (rev 6.00)" )
	public static GameDriver driver_smashtv5	   = new GameDriver("1990"	,"smashtv5"	,"smashtv.java"	,rom_smashtv5,driver_smashtv	,machine_driver_smashtv	,input_ports_smashtv	,init_smashtv	,ROT0_16BIT	,	"Williams", "Smash T.V. (rev 5.00)" )
	public static GameDriver driver_smashtv4	   = new GameDriver("1990"	,"smashtv4"	,"smashtv.java"	,rom_smashtv4,driver_smashtv	,machine_driver_smashtv	,input_ports_smashtv	,init_smashtv4	,ROT0_16BIT	,	"Williams", "Smash T.V. (rev 4.00)" )
	public static GameDriver driver_hiimpact	   = new GameDriver("1990"	,"hiimpact"	,"smashtv.java"	,rom_hiimpact,null	,machine_driver_smashtv	,input_ports_trog	,init_hiimpact	,ROT0_16BIT	,	"Williams", "High Impact Football (rev LA3 12/27/90)" )
	GAMEX(1991, shimpact, null,       smashtv, trog,    shimpact, ROT0_16BIT, "Williams", "Super High Impact (rev LA1 09/30/91)", GAME_NOT_WORKING )
	public static GameDriver driver_trog	   = new GameDriver("1990"	,"trog"	,"smashtv.java"	,rom_trog,null	,machine_driver_trog	,input_ports_trog	,init_trog	,ROT0_16BIT	,	"Midway",   "Trog (rev LA4 03/11/91)" )
	public static GameDriver driver_trog3	   = new GameDriver("1990"	,"trog3"	,"smashtv.java"	,rom_trog3,driver_trog	,machine_driver_trog	,input_ports_trog	,init_trog3	,ROT0_16BIT	,	"Midway",   "Trog (rev LA3 02/14/91)" )
	public static GameDriver driver_trogp	   = new GameDriver("1990"	,"trogp"	,"smashtv.java"	,rom_trogp,driver_trog	,machine_driver_trog	,input_ports_trog	,init_trogp	,ROT0_16BIT	,	"Midway",   "Trog (prototype, rev 4.00 07/27/90)" )
	public static GameDriver driver_strkforc	   = new GameDriver("1991"	,"strkforc"	,"smashtv.java"	,rom_strkforc,null	,machine_driver_trog	,input_ports_strkforc	,init_strkforc	,ROT0_16BIT	,	"Midway",   "Strike Force (rev 1 02/25/91)" )
	GAMEX(1992, mk,       null,       nbajam,  mk,      mk,       ROT0_16BIT, "Midway",   "Mortal Kombat (rev 5.null T-Unit 03/19/93)", GAME_NOT_WORKING )
	public static GameDriver driver_mkla1	   = new GameDriver("1992"	,"mkla1"	,"smashtv.java"	,rom_mkla1,driver_mk	,machine_driver_mk	,input_ports_mkla1	,init_mkla1	,ROT0_16BIT	,	"Midway",   "Mortal Kombat (rev 1.null 08/08/92)" )
	public static GameDriver driver_mkla2	   = new GameDriver("1992"	,"mkla2"	,"smashtv.java"	,rom_mkla2,driver_mk	,machine_driver_mk	,input_ports_mkla1	,init_mkla2	,ROT0_16BIT	,	"Midway",   "Mortal Kombat (rev 2.null 08/18/92)" )
	public static GameDriver driver_mkla3	   = new GameDriver("1992"	,"mkla3"	,"smashtv.java"	,rom_mkla3,driver_mk	,machine_driver_mk	,input_ports_mkla1	,init_mkla3	,ROT0_16BIT	,	"Midway",   "Mortal Kombat (rev 3.null 08/31/92)" )
	public static GameDriver driver_mkla4	   = new GameDriver("1992"	,"mkla4"	,"smashtv.java"	,rom_mkla4,driver_mk	,machine_driver_mk	,input_ports_mkla1	,init_mkla4	,ROT0_16BIT	,	"Midway",   "Mortal Kombat (rev 4.null 09/28/92)" )
	GAMEX(1991, term2,    null,       term2,   term2,   term2,    ROT0_16BIT, "Midway",   "Terminator 2 - Judgment Day (rev LA3 03/27/92)", GAME_NOT_WORKING )
	public static GameDriver driver_totcarn	   = new GameDriver("1992"	,"totcarn"	,"smashtv.java"	,rom_totcarn,null	,machine_driver_mk	,input_ports_totcarn	,init_totcarn	,ROT0_16BIT	,	"Midway",   "Total Carnage (rev LA1 03/10/92)" )
	public static GameDriver driver_totcarnp	   = new GameDriver("1992"	,"totcarnp"	,"smashtv.java"	,rom_totcarnp,driver_totcarn	,machine_driver_mk	,input_ports_totcarn	,init_totcarnp	,ROT0_16BIT	,	"Midway",   "Total Carnage (prototype, rev 1.null 01/25/92)" )
	GAMEX(1993, mk2,      null,       mk2,     mk2,     mk2,      ROT0_16BIT, "Midway",   "Mortal Kombat II (rev L3.1)", GAME_NOT_WORKING )
	GAMEX(1993, mk2r32,   mk2,     mk2,     mk2,     mk2,      ROT0_16BIT, "Midway",   "Mortal Kombat II (rev L3.2 (European))", GAME_NOT_WORKING )
	GAMEX(1993, mk2r14,   mk2,     mk2,     mk2,     mk2r14,   ROT0_16BIT, "Midway",   "Mortal Kombat II (rev L1.4)", GAME_NOT_WORKING )
	GAMEX(1993, nbajam,   null,       nbajam,  nbajam,  nbajam,   ROT0_16BIT, "Midway",   "NBA Jam (rev 3.01 04/07/93)", GAME_NOT_WORKING )
	GAMEX(1993, nbajamr2, nbajam,  nbajam,  nbajam,  nbajam,   ROT0_16BIT, "Midway",   "NBA Jam (rev 2.00 02/10/93)", GAME_NOT_WORKING )
	GAMEX(1994, nbajamte, nbajam,  nbajam,  nbajam,  nbajam,   ROT0_16BIT, "Midway",   "NBA Jam TE (rev 4.null 03/23/94)", GAME_NOT_WORKING )
}
