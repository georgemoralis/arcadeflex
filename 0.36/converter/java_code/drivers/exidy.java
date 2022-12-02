/***************************************************************************

Exidy memory map

0000-00FF R/W Zero Page RAM
0100-01FF R/W Stack RAM
0200-03FF R/W Scratchpad RAM
0800-3FFF  R  Program ROM              (Targ, Spectar only)
1A00       R  PX3 (Player 2 inputs)    (Fax only)
			  bit 4  D
			  bit 5  C
			  bit 6  B
			  bit 7  A
1C00       R  PX2 (Player 1 inputs)    (Fax only)
			  bit null  2 player start
			  bit 1  1 player start
			  bit 4  D
			  bit 5  C
			  bit 6  B
			  bit 7  A
2000-3FFF  R  Banked question ROM      (Fax only)
4000-43FF R/W Screen RAM
4800-4FFF R/W Character Generator RAM (except Pepper II and Fax)
5000       W  Motion Object 1 Horizontal Position Latch (sprite 1 X)
5040       W  Motion Object 1 Vertical Position Latch   (sprite 1 Y)
5080       W  Motion Object 2 Horizontal Position Latch (sprite 2 X)
50C0       W  Motion Object 2 Vertical Position Latch   (sprite 2 Y)
5100       R  Option Dipswitch Port
			  bit null  coin 2 (NOT inverted) (must activate together with $5103 bit 5)
			  bit 1-2  bonus
			  bit 3-4  coins per play
			  bit 5-6  lives
			  bit 7  US/UK coins
5100       W  Motion Objects Image Latch
			  Sprite number  bits null-3 Sprite #1  4-7 Sprite #2
5101       R  Control Inputs Port
			  bit null  start 1
			  bit 1  start 2
			  bit 2  right
			  bit 3  left
			  bit 5  up
			  bit 6  down
			  bit 7  coin 1 (must activate together with $5103 bit 6)
5101       W  Output Control Latch (not used in PEPPER II upright)
			  bit 7  Enable sprite #1
			  bit 6  Enable sprite #2
5103       R  Interrupt Condition Latch
			  bit null  LNG0 - supposedly a language DIP switch
			  bit 1  LNG1 - supposedly a language DIP switch
			  bit 2  different for each game, but generally a collision bit
			  bit 3  TABLE - supposedly a cocktail table DIP switch
			  bit 4  different for each game, but generally a collision bit
			  bit 5  coin 2 (must activate together with $5100 bit null)
			  bit 6  coin 1 (must activate together with $5101 bit 7)
			  bit 7  L256 - VBlank?
5213       R  IN2 (Mouse Trap)
			  bit 3  blue button
			  bit 2  free play
			  bit 1  red button
			  bit null  yellow button
52XX      R/W Audio/Color Board Communications
6000-6FFF R/W Character Generator RAM (Pepper II, Fax only)
8000-FFF9  R  Program memory space
FFFA-FFFF  R  Interrupt and Reset Vectors

Exidy Sound Board:
0000-07FF R/W RAM (mirrored every 0x7f)
0800-0FFF R/W 6532 Timer
1000-17FF R/W 6520 PIA
1800-1FFF R/W 8253 Timer
2000-27FF bit null Channel 1 Filter 1 enable
		  bit 1 Channel 1 Filter 2 enable
		  bit 2 Channel 2 Filter 1 enable
		  bit 3 Channel 2 Filter 2 enable
		  bit 4 Channel 3 Filter 1 enable
		  bit 5 Channel 3 Filter 2 enable
2800-2FFF 6840 Timer
3000      Bit null..1 Noise select
3001	  Bit null..2 Channel 1 Amplitude
3002	  Bit null..2 Channel 2 Amplitude
3003	  Bit null..2 Channel 3 Amplitude
5800-7FFF ROM

Targ:
5200    Sound board control
		bit null Music
		bit 1 Shoot
		bit 2 unused
		bit 3 Swarn
		bit 4 Sspec
		bit 5 crash
		bit 6 long
		bit 7 game

5201    Sound board control
		bit null note
		bit 1 upper

MouseTrap Digital Sound:
0000-3FFF ROM

IO:
	A7 = null: R Communication from sound processor
	A6 = null: R CVSD Clock State
	A5 = null: W Busy to sound processor
	A4 = null: W Data to CVSD

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class exidy
{
	
	/* also in machine/exidy.c */
	#define PALETTE_LEN 8
	#define COLORTABLE_LEN 20
	
	/* These are defined in sndhrdw/exidy.c */
	public static WriteHandlerPtr mtrap_voiceio_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static ReadHandlerPtr mtrap_voiceio_r = new ReadHandlerPtr() { public int handler(int offset);
	
	/* These are defined in sndhrdw/exidy.c */
	public static WriteHandlerPtr exidy_shriot_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static ReadHandlerPtr exidy_shriot_r = new ReadHandlerPtr() { public int handler(int offset);
	
	public static WriteHandlerPtr exidy_sfxctrl_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	
	public static WriteHandlerPtr exidy_sh8253_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static ReadHandlerPtr exidy_sh8253_r = new ReadHandlerPtr() { public int handler(int offset);
	
	public static ReadHandlerPtr exidy_sh6840_r = new ReadHandlerPtr() { public int handler(int offset);
	public static WriteHandlerPtr exidy_sh6840_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	
	int exidy_sh_start(const struct MachineSound *msound);
	void exidy_sh_stop(void);
	
	/* These are defined in vidhrdw/exidy.c */
	
	int exidy_vh_start(void);
	void exidy_vh_stop(void);
	public static WriteHandlerPtr exidy_characterram_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	void exidy_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh);
	public static WriteHandlerPtr exidy_color_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	
	
	
	/* These are defined in machine/exidy.c */
	
	public static WriteHandlerPtr fax_bank_select_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static ReadHandlerPtr exidy_input_port_2_r = new ReadHandlerPtr() { public int handler(int offset);
	void exidy_init_machine(void);
	public static InterruptPtr venture_interrupt = new InterruptPtr() { public int handler() ;
	int venture_shinterrupt(void);
	
	public static InitDriverPtr init_sidetrac = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_targ = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_spectar = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_venture = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_mtrap = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_pepper2 = new InitDriverPtr() { public void handler() ;
	public static InitDriverPtr init_fax = new InitDriverPtr() { public void handler() ;
	void exidy_vh_init_palette(unsigned char *game_palette, unsigned short *game_colortable,const unsigned char *color_prom);
	
	public static InterruptPtr exidy_interrupt = new InterruptPtr() { public int handler() ;
	
	
	/* These are defined in sndhrdw/targ.c */
	public static WriteHandlerPtr targ_sh_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	int targ_sh_start(const struct MachineSound *msound);
	void targ_sh_stop(void);
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x03ff, MRA_RAM ),
		new MemoryReadAddress( 0x0800, 0x3fff, MRA_ROM ), /* Targ, Spectar only */
		new MemoryReadAddress( 0x4000, 0x43ff, videoram_r ),
		new MemoryReadAddress( 0x4400, 0x47ff, videoram_r ),	/* mirror (sidetrac requires this) */
		new MemoryReadAddress( 0x4800, 0x4fff, MRA_RAM ),
		new MemoryReadAddress( 0x5100, 0x5100, input_port_0_r ), /* DSW */
		new MemoryReadAddress( 0x5101, 0x5101, input_port_1_r ), /* IN0 */
		new MemoryReadAddress( 0x5103, 0x5103, exidy_input_port_2_r ), /* IN1 */
		new MemoryReadAddress( 0x5105, 0x5105, input_port_4_r ), /* IN3 - Targ, Spectar only */
		new MemoryReadAddress( 0x5200, 0x520F, pia_0_r ),
		new MemoryReadAddress( 0x5213, 0x5213, input_port_3_r ),     /* IN2 */
		new MemoryReadAddress( 0x6000, 0x6fff, MRA_RAM ), /* Pepper II only */
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x03ff, MWA_RAM ),
		new MemoryWriteAddress( 0x0800, 0x3fff, MWA_ROM ),
		new MemoryWriteAddress( 0x4000, 0x43ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x4800, 0x4fff, exidy_characterram_w, exidy_characterram ),
		new MemoryWriteAddress( 0x5000, 0x5000, MWA_RAM, exidy_sprite1_xpos ),
		new MemoryWriteAddress( 0x5040, 0x5040, MWA_RAM, exidy_sprite1_ypos ),
		new MemoryWriteAddress( 0x5080, 0x5080, MWA_RAM, exidy_sprite2_xpos ),
		new MemoryWriteAddress( 0x50C0, 0x50C0, MWA_RAM, exidy_sprite2_ypos ),
		new MemoryWriteAddress( 0x5100, 0x5100, MWA_RAM, exidy_sprite_no ),
		new MemoryWriteAddress( 0x5101, 0x5101, MWA_RAM, exidy_sprite_enable ),
		new MemoryWriteAddress( 0x5200, 0x520F, pia_0_w ),
		new MemoryWriteAddress( 0x5210, 0x5212, exidy_color_w, exidy_color_latch ),
		new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress targ_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x03ff, MWA_RAM ),
		new MemoryWriteAddress( 0x0800, 0x3fff, MWA_ROM ),
		new MemoryWriteAddress( 0x4000, 0x43ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x4800, 0x4fff, exidy_characterram_w, exidy_characterram ),
		new MemoryWriteAddress( 0x5000, 0x5000, MWA_RAM, exidy_sprite1_xpos ),
		new MemoryWriteAddress( 0x5040, 0x5040, MWA_RAM, exidy_sprite1_ypos ),
		new MemoryWriteAddress( 0x5080, 0x5080, MWA_RAM, exidy_sprite2_xpos ),
		new MemoryWriteAddress( 0x50C0, 0x50C0, MWA_RAM, exidy_sprite2_ypos ),
		new MemoryWriteAddress( 0x5100, 0x5100, MWA_RAM, exidy_sprite_no ),
		new MemoryWriteAddress( 0x5101, 0x5101, MWA_RAM, exidy_sprite_enable ),
		new MemoryWriteAddress( 0x5200, 0x5201, targ_sh_w ),
		new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress pepper2_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x03ff, MWA_RAM ),
		new MemoryWriteAddress( 0x4000, 0x43ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x5000, 0x5000, MWA_RAM, exidy_sprite1_xpos ),
		new MemoryWriteAddress( 0x5040, 0x5040, MWA_RAM, exidy_sprite1_ypos ),
		new MemoryWriteAddress( 0x5080, 0x5080, MWA_RAM, exidy_sprite2_xpos ),
		new MemoryWriteAddress( 0x50C0, 0x50C0, MWA_RAM, exidy_sprite2_ypos ),
		new MemoryWriteAddress( 0x5100, 0x5100, MWA_RAM, exidy_sprite_no ),
		new MemoryWriteAddress( 0x5101, 0x5101, MWA_RAM, exidy_sprite_enable ),
		new MemoryWriteAddress( 0x5200, 0x520F, pia_0_w ),
		new MemoryWriteAddress( 0x5210, 0x5212, exidy_color_w, exidy_color_latch ),
		new MemoryWriteAddress( 0x5213, 0x5217, MWA_NOP ), /* empty control lines on color/sound board */
		new MemoryWriteAddress( 0x6000, 0x6fff, exidy_characterram_w, exidy_characterram ), /* two 6116 character RAMs */
		new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress fax_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x03ff, MRA_RAM ),
		new MemoryReadAddress( 0x0400, 0x07ff, MRA_RAM ), /* Fax only */
		new MemoryReadAddress( 0x1a00, 0x1a00, input_port_4_r ), /* IN3 - Fax only */
		new MemoryReadAddress( 0x1c00, 0x1c00, input_port_3_r ), /* IN2 - Fax only */
		new MemoryReadAddress( 0x2000, 0x3fff, MRA_BANK1 ), /* Fax only */
		new MemoryReadAddress( 0x4000, 0x43ff, MRA_RAM ),
		new MemoryReadAddress( 0x5100, 0x5100, input_port_0_r ), /* DSW */
		new MemoryReadAddress( 0x5101, 0x5101, input_port_1_r ), /* IN0 */
		new MemoryReadAddress( 0x5103, 0x5103, exidy_input_port_2_r ), /* IN1 */
		new MemoryReadAddress( 0x5200, 0x520F, pia_0_r ),
		new MemoryReadAddress( 0x5213, 0x5213, input_port_3_r ),     /* IN2 */
		new MemoryReadAddress( 0x6000, 0x6fff, MRA_RAM ), /* Fax, Pepper II only */
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress fax_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x03ff, MWA_RAM ),
		new MemoryWriteAddress( 0x0400, 0x07ff, MWA_RAM ), /* Fax only */
		new MemoryWriteAddress( 0x2000, 0x2000, fax_bank_select_w ), /* Fax only */
		new MemoryWriteAddress( 0x4000, 0x43ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x5000, 0x5000, MWA_RAM, exidy_sprite1_xpos ),
		new MemoryWriteAddress( 0x5040, 0x5040, MWA_RAM, exidy_sprite1_ypos ),
		new MemoryWriteAddress( 0x5080, 0x5080, MWA_RAM, exidy_sprite2_xpos ),
		new MemoryWriteAddress( 0x50C0, 0x50C0, MWA_RAM, exidy_sprite2_ypos ),
		new MemoryWriteAddress( 0x5100, 0x5100, MWA_RAM, exidy_sprite_no ),
		new MemoryWriteAddress( 0x5101, 0x5101, MWA_RAM, exidy_sprite_enable ),
		new MemoryWriteAddress( 0x5200, 0x520F, pia_0_w ),
		new MemoryWriteAddress( 0x5210, 0x5212, exidy_color_w, exidy_color_latch ),
		new MemoryWriteAddress( 0x5213, 0x5217, MWA_NOP ), /* empty control lines on color/sound board */
		new MemoryWriteAddress( 0x6000, 0x6fff, exidy_characterram_w, exidy_characterram ), /* two 6116 character RAMs */
		new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x07ff, MRA_RAM ),
		new MemoryReadAddress( 0x0800, 0x0FFF, exidy_shriot_r ),
		new MemoryReadAddress( 0x1000, 0x100F, pia_1_r ),
		new MemoryReadAddress( 0x1800, 0x1FFF, exidy_sh8253_r ),
		new MemoryReadAddress( 0x2000, 0x27FF, MRA_RAM ),
		new MemoryReadAddress( 0x2800, 0x2FFF, exidy_sh6840_r ),
		new MemoryReadAddress( 0x5800, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0xf7ff, MRA_RAM ),
		new MemoryReadAddress( 0xf800, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x07FF, MWA_RAM ),
		new MemoryWriteAddress( 0x0800, 0x0FFF, exidy_shriot_w ),
		new MemoryWriteAddress( 0x1000, 0x100F, pia_1_w ),
		new MemoryWriteAddress( 0x1800, 0x1FFF, exidy_sh8253_w ),
		new MemoryWriteAddress( 0x2000, 0x27FF, MWA_RAM ),
		new MemoryWriteAddress( 0x2800, 0x2FFF, exidy_sh6840_w ),
		new MemoryWriteAddress( 0x3000, 0x3700, exidy_sfxctrl_w ),
		new MemoryWriteAddress( 0x5800, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0xf7ff, MWA_RAM ),
		new MemoryWriteAddress( 0xf800, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress dac_writemem[] =
	{
	        new MemoryWriteAddress(0x0000,0x3fff, MWA_ROM ),
		new MemoryWriteAddress( -1 ) /* end of table */
	};
	
	static MemoryReadAddress dac_readmem[] =
	{
	        new MemoryReadAddress(0x0000,0x3fff, MRA_ROM ),
	        new MemoryReadAddress(0x4000,0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 ) /* end of table */
	};
	
	static IOWritePort dac_iowrite[] =
	{
	        new IOWritePort( 0x00, 0xFF, mtrap_voiceio_w ),
		new IOWritePort( -1 )	/* end of table */
	};
	
	static IOReadPort dac_ioread[] =
	{
		new IOReadPort( 0x00, 0xFF, mtrap_voiceio_r ),
		new IOReadPort( -1 )
	};
	
	/***************************************************************************
	Input Ports
	***************************************************************************/
	
	static InputPortPtr input_ports_sidetrac = new InputPortPtr(){ public void handler() { 
		PORT_START();               /* DSW0 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "2");
		PORT_DIPSETTING(    0x01, "3");
		PORT_DIPSETTING(    0x02, "4");
		PORT_DIPSETTING(    0x03, "5");
		PORT_DIPNAME( 0x0c, 0x04, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_2C") );
	/* 0x0c 2C_1C */
		PORT_DIPNAME( 0x10, 0x10, "Top Score Award" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNUSED );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0xFF, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* IN3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_targ = new InputPortPtr(){ public void handler() { 
		PORT_START();               /* DSW0 */
		PORT_BIT ( 0x01, IP_ACTIVE_LOW, IPT_COIN2 );/* upright/cocktail switch? */
		PORT_DIPNAME( 0x02, 0x00, "P Coinage" );
		PORT_DIPSETTING(    0x00, "10P/1 C 50P Coin/6 Cs" );
		PORT_DIPSETTING(    0x02, "2x10P/1 C 50P Coin/3 Cs" );
		PORT_DIPNAME( 0x04, 0x00, "Top Score Award" );
		PORT_DIPSETTING(    0x00, "Credit" );
		PORT_DIPSETTING(    0x04, "Extended Play" );
		PORT_DIPNAME( 0x18, 0x08, "Q Coinage" );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, "1C/1C (no display); )
		PORT_DIPSETTING(    0x18, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x60, 0x40, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x60, "2" );
		PORT_DIPSETTING(    0x40, "3" );
		PORT_DIPSETTING(    0x20, "4" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x80, 0x80, "Currency" );
		PORT_DIPSETTING(    0x80, "Quarters" );
		PORT_DIPSETTING(    0x00, "Pence" );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x7F, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x1f, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0xFF, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* IN3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	/* identical to Targ, the only difference is the additional Language dip switch */
	static InputPortPtr input_ports_spectar = new InputPortPtr(){ public void handler() { 
		PORT_START();               /* DSW0 */
		PORT_BIT ( 0x01, IP_ACTIVE_LOW, IPT_COIN2 );/* upright/cocktail switch? */
		PORT_DIPNAME( 0x02, 0x00, "P Coinage" );
		PORT_DIPSETTING(    0x00, "10P/1 C 50P Coin/6 Cs" );
		PORT_DIPSETTING(    0x02, "2x10P/1 C 50P Coin/3 Cs" );
		PORT_DIPNAME( 0x04, 0x00, "Top Score Award" );
		PORT_DIPSETTING(    0x00, "Credit" );
		PORT_DIPSETTING(    0x04, "Extended Play" );
		PORT_DIPNAME( 0x18, 0x08, "Q Coinage" );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, "1C/1C (no display); )
		PORT_DIPSETTING(    0x18, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x60, 0x40, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x60, "2" );
		PORT_DIPSETTING(    0x40, "3" );
		PORT_DIPSETTING(    0x20, "4" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x80, 0x80, "Currency" );
		PORT_DIPSETTING(    0x80, "Quarters" );
		PORT_DIPSETTING(    0x00, "Pence" );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x7f, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START();       /* IN1 */
		PORT_DIPNAME( 0x03, 0x00, "Language" );
		PORT_DIPSETTING(    0x00, "English" );
		PORT_DIPSETTING(    0x01, "French" );
		PORT_DIPSETTING(    0x02, "German" );
		PORT_DIPSETTING(    0x03, "Spanish" );
		PORT_BIT( 0x1c, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* IN3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_mtrap = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* DSW0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x06, "30000" );
		PORT_DIPSETTING(    0x04, "40000" );
		PORT_DIPSETTING(    0x02, "50000" );
		PORT_DIPSETTING(    0x00, "60000" );
		PORT_DIPNAME( 0x98, 0x98, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x90, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, "Coin A 2C/1C Coin B 1C/3C" );
		PORT_DIPSETTING(    0x98, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x10, "Coin A 1C/1C Coin B 1C/4C" );
		PORT_DIPSETTING(    0x18, "Coin A 1C/1C Coin B 1C/5C" );
		PORT_DIPSETTING(    0x88, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x08, "Coin A 1C/3C Coin B 2C/7C" );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x60, 0x40, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x60, "2" );
		PORT_DIPSETTING(    0x40, "3" );
		PORT_DIPSETTING(    0x20, "4" );
		PORT_DIPSETTING(    0x00, "5" );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY );
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_BUTTON1, "Dog Button", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START();       /* IN1 */
	/*
		The schematics claim these exist, but there's nothing in
		the ROMs to support that claim (as far as I can see):
	
		PORT_DIPNAME( 0x03, 0x00, "Language" );
		PORT_DIPSETTING(    0x00, "English" );
		PORT_DIPSETTING(    0x01, "French" );
		PORT_DIPSETTING(    0x02, "German" );
		PORT_DIPSETTING(    0x03, "Spanish" );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Cocktail") );
	*/
	
		PORT_BIT( 0x1f, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START();               /* IN2 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_BUTTON2, "Yellow Button", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_BUTTON3, "Red Button", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BITX(0x04, 0x04, IPT_DIPSWITCH_NAME, DEF_STR( "Free_Play") ); IP_KEY_NONE, IP_JOY_NONE )
		PORT_DIPSETTING(0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(0x00, DEF_STR( "On") );
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_BUTTON4, "Blue Button", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_venture = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* DSW0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x06, "20000" );
		PORT_DIPSETTING(    0x04, "30000" );
		PORT_DIPSETTING(    0x02, "40000" );
		PORT_DIPSETTING(    0x00, "50000" );
		PORT_DIPNAME( 0x98, 0x80, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x88, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x98, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x00, "Pence: A 2C/1C B 1C/3C" );
		PORT_DIPSETTING(    0x18, "Pence: A 1C/1C B 1C/6C" );
		/*0x10 same as 0x00 */
		/*0x90 same as 0x80 */
		PORT_DIPNAME( 0x60, 0x20, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "2" );
		PORT_DIPSETTING(    0x20, "3" );
		PORT_DIPSETTING(    0x40, "4" );
		PORT_DIPSETTING(    0x60, "5" );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START();       /* IN1 */
	/*
		The schematics claim these exist, but there's nothing in
		the ROMs to support that claim (as far as I can see):
	
		PORT_DIPNAME( 0x03, 0x00, "Language" );
		PORT_DIPSETTING(    0x00, "English" );
		PORT_DIPSETTING(    0x01, "French" );
		PORT_DIPSETTING(    0x02, "German" );
		PORT_DIPSETTING(    0x03, "Spanish" );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Cocktail") );
	*/
	
		PORT_BIT( 0x1f, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_pepper2 = new InputPortPtr(){ public void handler() { 
		PORT_START();               /* DSW */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x06, "40000" );
		PORT_DIPSETTING(    0x04, "50000" );
		PORT_DIPSETTING(    0x02, "60000" );
		PORT_DIPSETTING(    0x00, "70000" );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x60, "2" );
		PORT_DIPSETTING(    0x40, "3" );
		PORT_DIPSETTING(    0x20, "4" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x98, 0x98, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x90, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, "Coin A 2C/1C Coin B 1C/3C" );
		PORT_DIPSETTING(    0x98, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x10, "Coin A 1C/1C Coin B 1C/4C" );
		PORT_DIPSETTING(    0x18, "Coin A 1C/1C Coin B 1C/5C" );
		PORT_DIPSETTING(    0x88, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x08, "1 Coin/3 Credits 2C/7C" );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_4C") );
	
		PORT_START();               /* IN0 */
		PORT_BIT ( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT ( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT ( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT ( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT ( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START();       /* IN1 */
	/*
		The schematics claim these exist, but there's nothing in
		the ROMs to support that claim (as far as I can see):
	
		PORT_DIPNAME( 0x03, 0x00, "Language" );
		PORT_DIPSETTING(    0x00, "English" );
		PORT_DIPSETTING(    0x01, "French" );
		PORT_DIPSETTING(    0x02, "German" );
		PORT_DIPSETTING(    0x03, "Spanish" );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Cocktail") );
	*/
	
		PORT_BIT( 0x1F, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_fax = new InputPortPtr(){ public void handler() { 
		PORT_START();               /* DSW */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_DIPNAME( 0x06, 0x06, "Bonus Time" );
		PORT_DIPSETTING(    0x06, "8000" );
		PORT_DIPSETTING(    0x04, "13000" );
		PORT_DIPSETTING(    0x02, "18000" );
		PORT_DIPSETTING(    0x00, "25000" );
		PORT_DIPNAME( 0x60, 0x60, "Game/Bonus Times" );
		PORT_DIPSETTING(    0x60, ":32/:24" );
		PORT_DIPSETTING(    0x40, ":48/:36" );
		PORT_DIPSETTING(    0x20, "1:04/:48" );
		PORT_DIPSETTING(    0x00, "1:12/1:04" );
		PORT_DIPNAME( 0x98, 0x98, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x90, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, "Coin A 2C/1C Coin B 1C/3C" );
		PORT_DIPSETTING(    0x98, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x10, "Coin A 1C/1C Coin B 1C/4C" );
		PORT_DIPSETTING(    0x18, "Coin A 1C/1C Coin B 1C/5C" );
		PORT_DIPSETTING(    0x88, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x08, "1 Coin/3 Credits 2C/7C" );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_4C") );
	
		PORT_START();               /* IN0 */
		PORT_BIT ( 0x7f, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START();       /* IN1 */
	/*
		The schematics claim these exist, but there's nothing in
		the ROMs to support that claim (as far as I can see):
	
		PORT_DIPNAME( 0x03, 0x00, "Language" );
		PORT_DIPSETTING(    0x00, "English" );
		PORT_DIPSETTING(    0x01, "French" );
		PORT_DIPSETTING(    0x02, "German" );
		PORT_DIPSETTING(    0x03, "Spanish" );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Cocktail") );
	*/
	
		PORT_BIT( 0x1b, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );   /* Set when motion object 1 is drawn? */
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );  /* VBlank */
	
		PORT_START();  /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON4 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON3 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 );
	
		PORT_START();  /* IN3 */
		PORT_BIT( 0x0f, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
	
	INPUT_PORTS_END(); }}; 
	
	
	/***************************************************************************
	Graphics Layout
	***************************************************************************/
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,    /* 8*8 characters */
		256,    /* 256 characters */
		1,      /* 1 bits per pixel */
		new int[] { 0 }, /* No info needed for bit offsets */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8 /* every char takes 8 consecutive bytes */
	);
	
	/* Pepper II and Fax used a special Plane Generation Board for 2-bit graphics */
	
	static GfxLayout pepper2_charlayout = new GfxLayout
	(
		8,8,    /* 8*8 characters */
		256,    /* 256 characters */
		2,      /* 2 bits per pixel */
		new int[] { 0, 256*8*8 }, /* 2 bits separated by 0x0800 bytes */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8 /* every char takes 8 consecutive bytes */
	);
	
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,  /* 16*16 sprites */
		16*4,   /* 64 characters */
		1,      /* 1 bit per pixel */
		new int[] { 0 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7},
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8, 8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8},
		8*32    /* every char takes 32 consecutive bytes */
	);
	
	static GfxLayout targ_spritelayout = new GfxLayout
	(
		16,16,  /* 16*16 sprites */
		16*2,   /* 32 characters for Targ/Spectar */
		1,      /* 1 bit per pixel */
		new int[] { 0 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7},
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8, 8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8},
		8*32    /* every char takes 32 consecutive bytes */
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( 0,           0x4800, charlayout,       0, 4 ),         /* the game dynamically modifies this */
		new GfxDecodeInfo( REGION_GFX1, 0x0000, spritelayout, 8, 2 ),  /* Sprites */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo pepper2_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( 0,           0x6000, pepper2_charlayout,       0, 4 ),    /* the game dynamically modifies this */
		new GfxDecodeInfo( REGION_GFX1, 0x0000, spritelayout, 16, 2 ),  /* Angel/Devil/Zipper Ripper */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo targ_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( 0,           0x4800, charlayout,       0, 4 ),         /* the game dynamically modifies this */
		new GfxDecodeInfo( REGION_GFX1, 0x0000, targ_spritelayout, 8, 2 ),  /* Sprites */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	/***************************************************************************
	  Game drivers
	***************************************************************************/
	
	static const char *targ_sample_names[] =
	{
		"*targ",
		"expl.wav",
		"shot.wav",
		"sexpl.wav",
		"spslow.wav",
		"spfast.wav",
		0       /* end of array */
	};
	
	static struct Samplesinterface targ_samples_interface =
	{
		3,	/* 3 Channels */
		25,	/* volume */
		targ_sample_names
	};
	
	static struct CustomSound_interface targ_custom_interface =
	{
		targ_sh_start,
		targ_sh_stop,
		0
	};
	
	static DACinterface targ_DAC_interface = new DACinterface
	(
		1,
	    new int[] { 100 }
	);
	
	
	
	static MachineDriver machine_driver_targ = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				11289000/16,    /* .705562 MHz */
				readmem,targ_writemem,null,null,
				exidy_interrupt,1
			),
		},
		57, DEFAULT_60HZ_VBLANK_DURATION,       /* frames per second, vblank duration */
		1,
		exidy_init_machine,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 31*8-1, 0*8, 32*8-1 ),
		targ_gfxdecodeinfo,
		PALETTE_LEN, COLORTABLE_LEN,
		exidy_vh_init_palette,
	
		VIDEO_TYPE_RASTER|VIDEO_SUPPORTS_DIRTY,
		null,
		exidy_vh_start,
		exidy_vh_stop,
		exidy_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_CUSTOM,
				targ_custom_interface
			),
			new MachineSound(
				SOUND_SAMPLES,
				targ_samples_interface
			),
			new MachineSound(
				SOUND_DAC,
				targ_DAC_interface
			)
		}
	);
	
	static struct hc55516_interface cvsd_interface =
	{
		1,          /* 1 chip */
	        { 80 }
	};
	
	static struct Samplesinterface venture_samples_interface=
	{
	    6,       /* 6 Channels */
	    20  /* volume */
	};
	
	static struct CustomSound_interface exidy_custom_interface =
	{
	    exidy_sh_start,
	    exidy_sh_stop,
		0
	};
	
	static MachineDriver machine_driver_mtrap = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				11289000/16,
				readmem,writemem,null,null,
				exidy_interrupt,1
			),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				3579545/4,
				sound_readmem,sound_writemem,null,null,
		    	ignore_interrupt,0
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				3579545/2,
				dac_readmem,dac_writemem,dac_ioread,dac_iowrite,
				ignore_interrupt,0
			)
		},
		57, DEFAULT_60HZ_VBLANK_DURATION,       /* frames per second, vblank duration */
	    32, /* 10 CPU slices per frame - enough for the sound CPU to read all commands */
		exidy_init_machine,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 31*8-1, 0*8, 32*8-1 ),
		gfxdecodeinfo,
		PALETTE_LEN, COLORTABLE_LEN,
		exidy_vh_init_palette,
	
		VIDEO_TYPE_RASTER|VIDEO_SUPPORTS_DIRTY|VIDEO_MODIFIES_PALETTE,
		null,
		exidy_vh_start,
		exidy_vh_stop,
		exidy_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_HC55516,
				cvsd_interface
	        ),
			new MachineSound(
				SOUND_SAMPLES,
				venture_samples_interface
			),
			new MachineSound(
				SOUND_CUSTOM,
				exidy_custom_interface
	        )
		}
	
	
	);
	
	
	static MachineDriver machine_driver_venture = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				11289000/16,
				readmem,writemem,null,null,
				venture_interrupt,32 /* Need to have multiple IRQs per frame if there's a collision */
			),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				3579545/4,
				sound_readmem,sound_writemem,null,null,
				ignore_interrupt,0
			)
		},
		57, DEFAULT_60HZ_VBLANK_DURATION,       /* frames per second, vblank duration */
		10, /* 10 CPU slices per frame - enough for the sound CPU to read all commands */
		exidy_init_machine,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 31*8-1, 0*8, 32*8-1 ),
		gfxdecodeinfo,
		PALETTE_LEN, COLORTABLE_LEN,
		exidy_vh_init_palette,
	
		VIDEO_TYPE_RASTER|VIDEO_SUPPORTS_DIRTY|VIDEO_MODIFIES_PALETTE,
		null,
		exidy_vh_start,
		exidy_vh_stop,
		exidy_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_SAMPLES,
				venture_samples_interface
			),
			new MachineSound(
				SOUND_CUSTOM,
				exidy_custom_interface
			)
		}
	);
	
	
	static MachineDriver machine_driver_pepper2 = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				11289000/16,
				readmem,pepper2_writemem,null,null,
				exidy_interrupt,1
			),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				3579545/4,
				sound_readmem,sound_writemem,null,null,
				ignore_interrupt,0
			)
		},
		57, DEFAULT_60HZ_VBLANK_DURATION,       /* frames per second, vblank duration */
		10, /* 10 CPU slices per frame - enough for the sound CPU to read all commands */
		exidy_init_machine,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 31*8-1, 0*8, 32*8-1 ),
		pepper2_gfxdecodeinfo,
		PALETTE_LEN, COLORTABLE_LEN,
		exidy_vh_init_palette,
	
		VIDEO_TYPE_RASTER|VIDEO_SUPPORTS_DIRTY|VIDEO_MODIFIES_PALETTE,
		null,
		exidy_vh_start,
		exidy_vh_stop,
		exidy_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_SAMPLES,
				venture_samples_interface
			),
			new MachineSound(
				SOUND_CUSTOM,
				exidy_custom_interface
			)
		}
	
	);
	
	
	static MachineDriver machine_driver_fax = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				11289000/16,
				fax_readmem,fax_writemem,null,null,
				exidy_interrupt,1
			),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				3579545/4,
				sound_readmem,sound_writemem,null,null,
				ignore_interrupt,0
			)
		},
		57, DEFAULT_60HZ_VBLANK_DURATION,       /* frames per second, vblank duration */
		10, /* 10 CPU slices per frame - enough for the sound CPU to read all commands */
		exidy_init_machine,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 31*8-1, 0*8, 32*8-1 ),
		pepper2_gfxdecodeinfo,
		PALETTE_LEN, COLORTABLE_LEN,
		exidy_vh_init_palette,
	
		VIDEO_TYPE_RASTER|VIDEO_SUPPORTS_DIRTY|VIDEO_MODIFIES_PALETTE,
		null,
		exidy_vh_start,
		exidy_vh_stop,
		exidy_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_SAMPLES,
				venture_samples_interface
			),
			new MachineSound(
				SOUND_CUSTOM,
				exidy_custom_interface
			)
		}
	);
	
	
	/***************************************************************************
	  Game ROMs
	***************************************************************************/
	
	static RomLoadPtr rom_sidetrac = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "stl8a-1",     0x2800, 0x0800, 0xe41750ff );
		ROM_LOAD( "stl7a-2",     0x3000, 0x0800, 0x57fb28dc );
		ROM_LOAD( "stl6a-2",     0x3800, 0x0800, 0x4226d469 );
		ROM_RELOAD(              0xf800, 0x0800 );/* for the reset/interrupt vectors */
		ROM_LOAD( "stl9c-1",     0x4800, 0x0400, 0x08710a84 );/* prom instead of ram chr gen*/
	
		ROM_REGION( 0x0800, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "stl11d",      0x0000, 0x0200, 0x3bd1acc1 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_targ = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "targ10a1",    0x1800, 0x0800, 0x969744e1 );
		ROM_LOAD( "targ09a1",    0x2000, 0x0800, 0xa177a72d );
		ROM_LOAD( "targ08a1",    0x2800, 0x0800, 0x6e6928a5 );
		ROM_LOAD( "targ07a4",    0x3000, 0x0800, 0xe2f37f93 );
		ROM_LOAD( "targ06a3",    0x3800, 0x0800, 0xa60a1bfc );
		ROM_RELOAD(              0xf800, 0x0800 );/* for the reset/interrupt vectors */
	
		ROM_REGION( 0x0800, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "targ11d1",    0x0000, 0x0400, 0x9f03513e );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_spectar = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "spl11a-3",    0x1000, 0x0800, 0x08880aff );
		ROM_LOAD( "spl10a-2",    0x1800, 0x0800, 0xfca667c1 );
		ROM_LOAD( "spl9a-3",     0x2000, 0x0800, 0x9d4ce8ba );
		ROM_LOAD( "spl8a-2",     0x2800, 0x0800, 0xcfacbadf );
		ROM_LOAD( "spl7a-2",     0x3000, 0x0800, 0x4c4741ff );
		ROM_LOAD( "spl6a-2",     0x3800, 0x0800, 0x0cb46b25 );
		ROM_RELOAD(              0xf800, 0x0800 ); /* for the reset/interrupt vectors */
	
		ROM_REGION( 0x0800, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "hrl11d-2",    0x0000, 0x0400, 0xc55b645d ); /* this is actually not used (all FF) */
		ROM_CONTINUE(            0x0000, 0x0400 ); /* overwrite with the real one */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_spectar1 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "spl12a1",     0x0800, 0x0800, 0x7002efb4 );
		ROM_LOAD( "spl11a1",     0x1000, 0x0800, 0x8eb8526a );
		ROM_LOAD( "spl10a1",     0x1800, 0x0800, 0x9d169b3d );
		ROM_LOAD( "spl9a1",      0x2000, 0x0800, 0x40e3eba1 );
		ROM_LOAD( "spl8a1",      0x2800, 0x0800, 0x64d8eb84 );
		ROM_LOAD( "spl7a1",      0x3000, 0x0800, 0xe08b0d8d );
		ROM_LOAD( "spl6a1",      0x3800, 0x0800, 0xf0e4e71a );
		ROM_RELOAD(              0xf800, 0x0800 );  /* for the reset/interrupt vectors */
	
		ROM_REGION( 0x0800, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "hrl11d-2",    0x0000, 0x0400, 0xc55b645d ); /* this is actually not used (all FF) */
		ROM_CONTINUE(            0x0000, 0x0400 ); /* overwrite with the real one */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_mtrap = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "mtl11a.bin",  0xa000, 0x1000, 0xbd6c3eb5 );
		ROM_LOAD( "mtl10a.bin",  0xb000, 0x1000, 0x75b0593e );
		ROM_LOAD( "mtl9a.bin",   0xc000, 0x1000, 0x28dd20ff );
		ROM_LOAD( "mtl8a.bin",   0xd000, 0x1000, 0xcc09f7a4 );
		ROM_LOAD( "mtl7a.bin",   0xe000, 0x1000, 0xcaafbb6d );
		ROM_LOAD( "mtl6a.bin",   0xf000, 0x1000, 0xd85e52ca );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for audio */
		ROM_LOAD( "mta5a.bin",   0x6800, 0x0800, 0xdbe4ec02 );
		ROM_LOAD( "mta6a.bin",   0x7000, 0x0800, 0xc00f0c05 );
		ROM_LOAD( "mta7a.bin",   0x7800, 0x0800, 0xf3f16ca7 );
		ROM_RELOAD(              0xf800, 0x0800 );
	
		ROM_REGION( 0x10000, REGION_CPU3 );/* 64k for digital sound processor */
		ROM_LOAD( "mta2a.bin", 0x0000,0x1000,0x13db8ed3 );
		ROM_LOAD( "mta3a.bin", 0x1000,0x1000,0x31bdfe5c );
		ROM_LOAD( "mta4a.bin", 0x2000,0x1000,0x1502d0e8 );
		ROM_LOAD( "mta1a.bin", 0x3000,0x1000,0x658482a6 );
	
		ROM_REGION( 0x0800, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "mtl11d.bin",  0x0000, 0x0800, 0xc6e4d339 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_mtrap3 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "mtl-3.11a",   0xa000, 0x1000, 0x4091be6e );
		ROM_LOAD( "mtl-3.10a",   0xb000, 0x1000, 0x38250c2f );
		ROM_LOAD( "mtl-3.9a",    0xc000, 0x1000, 0x2eec988e );
		ROM_LOAD( "mtl-3.8a",    0xd000, 0x1000, 0x744b4b1c );
		ROM_LOAD( "mtl-3.7a",    0xe000, 0x1000, 0xea8ec479 );
		ROM_LOAD( "mtl-3.6a",    0xf000, 0x1000, 0xd72ba72d );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for audio */
		ROM_LOAD( "mta5a.bin",   0x6800, 0x0800, 0xdbe4ec02 );
		ROM_LOAD( "mta6a.bin",   0x7000, 0x0800, 0xc00f0c05 );
		ROM_LOAD( "mta7a.bin",   0x7800, 0x0800, 0xf3f16ca7 );
		ROM_RELOAD(              0xf800, 0x0800 );
	
		ROM_REGION( 0x10000, REGION_CPU3 );/* 64k for digital sound processor */
		ROM_LOAD( "mta2a.bin", 0x0000,0x1000,0x13db8ed3 );
		ROM_LOAD( "mta3a.bin", 0x1000,0x1000,0x31bdfe5c );
		ROM_LOAD( "mta4a.bin", 0x2000,0x1000,0x1502d0e8 );
		ROM_LOAD( "mta1a.bin", 0x3000,0x1000,0x658482a6 );
	
		ROM_REGION( 0x0800, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "mtl11d.bin",  0x0000, 0x0800, 0xc6e4d339 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_mtrap4 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "mta411a.bin",  0xa000, 0x1000, 0x2879cb8d );
		ROM_LOAD( "mta410a.bin",  0xb000, 0x1000, 0xd7378af9 );
		ROM_LOAD( "mta49.bin",    0xc000, 0x1000, 0xbe667e64 );
		ROM_LOAD( "mta48a.bin",   0xd000, 0x1000, 0xde0442f8 );
		ROM_LOAD( "mta47a.bin",   0xe000, 0x1000, 0xcdf8c6a8 );
		ROM_LOAD( "mta46a.bin",   0xf000, 0x1000, 0x77d3f2e6 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for audio */
		ROM_LOAD( "mta5a.bin",    0x6800, 0x0800, 0xdbe4ec02 );
		ROM_LOAD( "mta6a.bin",    0x7000, 0x0800, 0xc00f0c05 );
		ROM_LOAD( "mta7a.bin",    0x7800, 0x0800, 0xf3f16ca7 );
		ROM_RELOAD(               0xf800, 0x0800 );
	
		ROM_REGION( 0x10000, REGION_CPU3 );/* 64k for digital sound processor */
		ROM_LOAD( "mta2a.bin", 0x0000,0x1000,0x13db8ed3 );
		ROM_LOAD( "mta3a.bin", 0x1000,0x1000,0x31bdfe5c );
		ROM_LOAD( "mta4a.bin", 0x2000,0x1000,0x1502d0e8 );
		ROM_LOAD( "mta1a.bin", 0x3000,0x1000,0x658482a6 );
	
		ROM_REGION( 0x0800, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "mtl11d.bin",   0x0000, 0x0800, 0xc6e4d339 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_venture = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "13a-cpu",      0x8000, 0x1000, 0xf4e4d991 );
		ROM_LOAD( "12a-cpu",      0x9000, 0x1000, 0xc6d8cb04 );
		ROM_LOAD( "11a-cpu",      0xa000, 0x1000, 0x3bdb01f4 );
		ROM_LOAD( "10a-cpu",      0xb000, 0x1000, 0x0da769e9 );
		ROM_LOAD( "9a-cpu",       0xc000, 0x1000, 0x0ae05855 );
		ROM_LOAD( "8a-cpu",       0xd000, 0x1000, 0x4ae59676 );
		ROM_LOAD( "7a-cpu",       0xe000, 0x1000, 0x48d66220 );
		ROM_LOAD( "6a-cpu",       0xf000, 0x1000, 0x7b78cf49 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for audio */
		ROM_LOAD( "3a-ac",        0x5800, 0x0800, 0x4ea1c3d9 );
		ROM_LOAD( "4a-ac",        0x6000, 0x0800, 0x5154c39e );
		ROM_LOAD( "5a-ac",        0x6800, 0x0800, 0x1e1e3916 );
		ROM_LOAD( "6a-ac",        0x7000, 0x0800, 0x80f3357a );
		ROM_LOAD( "7a-ac",        0x7800, 0x0800, 0x466addc7 );
		ROM_RELOAD(               0xf800, 0x0800 );
	
		ROM_REGION( 0x0800, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "11d-cpu",      0x0000, 0x0800, 0xb4bb2503 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_venture2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "vent_a13.cpu", 0x8000, 0x1000, 0x4c833f99 );
		ROM_LOAD( "vent_a12.cpu", 0x9000, 0x1000, 0x8163cefc );
		ROM_LOAD( "vent_a11.cpu", 0xa000, 0x1000, 0x324a5054 );
		ROM_LOAD( "vent_a10.cpu", 0xb000, 0x1000, 0x24358203 );
		ROM_LOAD( "vent_a9.cpu",  0xc000, 0x1000, 0x04428165 );
		ROM_LOAD( "vent_a8.cpu",  0xd000, 0x1000, 0x4c1a702a );
		ROM_LOAD( "vent_a7.cpu",  0xe000, 0x1000, 0x1aab27c2 );
		ROM_LOAD( "vent_a6.cpu",  0xf000, 0x1000, 0x767bdd71 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for audio */
		ROM_LOAD( "3a-ac",        0x5800, 0x0800, 0x4ea1c3d9 );
		ROM_LOAD( "4a-ac",        0x6000, 0x0800, 0x5154c39e );
		ROM_LOAD( "5a-ac",        0x6800, 0x0800, 0x1e1e3916 );
		ROM_LOAD( "6a-ac",        0x7000, 0x0800, 0x80f3357a );
		ROM_LOAD( "7a-ac",        0x7800, 0x0800, 0x466addc7 );
		ROM_RELOAD(               0xf800, 0x0800 );
	
		ROM_REGION( 0x0800, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "11d-cpu",      0x0000, 0x0800, 0xb4bb2503 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_venture4 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "vel13a-4",     0x8000, 0x1000, 0x1c5448f9 );
		ROM_LOAD( "vel12a-4",     0x9000, 0x1000, 0xe62491cc );
		ROM_LOAD( "vel11a-4",     0xa000, 0x1000, 0xe91faeaf );
		ROM_LOAD( "vel10a-4",     0xb000, 0x1000, 0xda3a2991 );
		ROM_LOAD( "vel9a-4",      0xc000, 0x1000, 0xd1887b11 );
		ROM_LOAD( "vel8a-4",      0xd000, 0x1000, 0x8e8153fc );
		ROM_LOAD( "vel7a-4",      0xe000, 0x1000, 0x0a091701 );
		ROM_LOAD( "vel6a-4",      0xf000, 0x1000, 0x7b165f67 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for audio */
		ROM_LOAD( "vea3a-2",      0x5800, 0x0800, 0x83b8836f );
		ROM_LOAD( "4a-ac",        0x6000, 0x0800, 0x5154c39e );
		ROM_LOAD( "5a-ac",        0x6800, 0x0800, 0x1e1e3916 );
		ROM_LOAD( "6a-ac",        0x7000, 0x0800, 0x80f3357a );
		ROM_LOAD( "7a-ac",        0x7800, 0x0800, 0x466addc7 );
		ROM_RELOAD(               0xf800, 0x0800 );
	
		ROM_REGION( 0x0800, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "vel11d-2",     0x0000, 0x0800, 0xea6fd981 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_pepper2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "main_12a",     0x9000, 0x1000, 0x33db4737 );
		ROM_LOAD( "main_11a",     0xa000, 0x1000, 0xa1f43b1f );
		ROM_LOAD( "main_10a",     0xb000, 0x1000, 0x4d7d7786 );
		ROM_LOAD( "main_9a",      0xc000, 0x1000, 0xb3362298 );
		ROM_LOAD( "main_8a",      0xd000, 0x1000, 0x64d106ed );
		ROM_LOAD( "main_7a",      0xe000, 0x1000, 0xb1c6f07c );
		ROM_LOAD( "main_6a",      0xf000, 0x1000, 0x515b1046 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for audio */
		ROM_LOAD( "audio_5a",     0x6800, 0x0800, 0x90e3c781 );
		ROM_LOAD( "audio_6a",     0x7000, 0x0800, 0xdd343e34 );
		ROM_LOAD( "audio_7a",     0x7800, 0x0800, 0xe02b4356 );
		ROM_RELOAD(               0xf800, 0x0800 );
	
		ROM_REGION( 0x0800, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "main_11d",     0x0000, 0x0800, 0xb25160cd );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_hardhat = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "hhl-2.11a",    0xa000, 0x1000, 0x7623deea );
		ROM_LOAD( "hhl-2.10a",    0xb000, 0x1000, 0xe6bf2fb1 );
		ROM_LOAD( "hhl-2.9a",     0xc000, 0x1000, 0xacc2bce5 );
		ROM_LOAD( "hhl-2.8a",     0xd000, 0x1000, 0x23c7a2f8 );
		ROM_LOAD( "hhl-2.7a",     0xe000, 0x1000, 0x6f7ce1c2 );
		ROM_LOAD( "hhl-2.6a",     0xf000, 0x1000, 0x2a20cf10 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for audio */
		ROM_LOAD( "hha-1.5a",     0x6800, 0x0800, 0x16a5a183 );
		ROM_LOAD( "hha-1.6a",     0x7000, 0x0800, 0xbde64021 );
		ROM_LOAD( "hha-1.7a",     0x7800, 0x0800, 0x505ee5d3 );
		ROM_RELOAD(               0xf800, 0x0800 );
	
		ROM_REGION( 0x0800, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "hhl-1.11d",    0x0000, 0x0800, 0xdbcdf353 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_fax = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );/* 64k for code + 192k for extra memory */
		ROM_LOAD( "fxl8-13a.32",  0x8000, 0x1000, 0x8e30bf6b );
		ROM_LOAD( "fxl8-12a.32",  0x9000, 0x1000, 0x60a41ff1 );
		ROM_LOAD( "fxl8-11a.32",  0xA000, 0x1000, 0x2c9cee8a );
		ROM_LOAD( "fxl8-10a.32",  0xB000, 0x1000, 0x9b03938f );
		ROM_LOAD( "fxl8-9a.32",   0xC000, 0x1000, 0xfb869f62 );
		ROM_LOAD( "fxl8-8a.32",   0xD000, 0x1000, 0xdb3470bc );
		ROM_LOAD( "fxl8-7a.32",   0xE000, 0x1000, 0x1471fef5 );
		ROM_LOAD( "fxl8-6a.32",   0xF000, 0x1000, 0x812e39f3 );
		/* Banks of question ROMs */
		ROM_LOAD( "fxd-1c.64",          0x10000, 0x2000, 0xfd7e3137 );
		ROM_LOAD( "fxd-2c.64",          0x12000, 0x2000, 0xe78cb16f );
		ROM_LOAD( "fxd-3c.64",          0x14000, 0x2000, 0x57a94c6f );
		ROM_LOAD( "fxd-4c.64",          0x16000, 0x2000, 0x9036c5a2 );
		ROM_LOAD( "fxd-5c.64",          0x18000, 0x2000, 0x38c03405 );
		ROM_LOAD( "fxd-6c.64",          0x1A000, 0x2000, 0xf48fc308 );
		ROM_LOAD( "fxd-7c.64",          0x1C000, 0x2000, 0xcf93b924 );
		ROM_LOAD( "fxd-8c.64",          0x1E000, 0x2000, 0x607b48da );
		ROM_LOAD( "fxd-1b.64",          0x20000, 0x2000, 0x62872d4f );
		ROM_LOAD( "fxd-2b.64",          0x22000, 0x2000, 0x625778d0 );
		ROM_LOAD( "fxd-3b.64",          0x24000, 0x2000, 0xc3473dee );
		ROM_LOAD( "fxd-4b.64",          0x26000, 0x2000, 0xe39a15f5 );
		ROM_LOAD( "fxd-5b.64",          0x28000, 0x2000, 0x101a9d70 );
		ROM_LOAD( "fxd-6b.64",          0x2A000, 0x2000, 0x374a8f05 );
		ROM_LOAD( "fxd-7b.64",          0x2C000, 0x2000, 0xf7e7f824 );
		ROM_LOAD( "fxd-8b.64",          0x2E000, 0x2000, 0x8f1a5287 );
		ROM_LOAD( "fxd-1a.64",          0x30000, 0x2000, 0xfc5e6344 );
		ROM_LOAD( "fxd-2a.64",          0x32000, 0x2000, 0x43cf60b3 );
		ROM_LOAD( "fxd-3a.64",          0x34000, 0x2000, 0x6b7d29cb );
		ROM_LOAD( "fxd-4a.64",          0x36000, 0x2000, 0xb9de3c2d );
		ROM_LOAD( "fxd-5a.64",          0x38000, 0x2000, 0x67285bc6 );
		ROM_LOAD( "fxd-6a.64",          0x3A000, 0x2000, 0xba67b7b2 );
		/* The last two ROM sockets were apparently never populated */
	//	ROM_LOAD( "fxd-7a.64",          0x3C000, 0x2000, 0x00000000 );
	//	ROM_LOAD( "fxd-8a.64",          0x3E000, 0x2000, 0x00000000 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for audio */
		ROM_LOAD( "fxa2-5a.16",   0x6800, 0x0800, 0x7c525aec );
		ROM_LOAD( "fxa2-6a.16",   0x7000, 0x0800, 0x2b3bfc44 );
		ROM_LOAD( "fxa2-7a.16",   0x7800, 0x0800, 0x578c62b7 );
		ROM_RELOAD(               0xf800, 0x0800 );
	
		ROM_REGION( 0x0800, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "fxl1-11d.32",  0x0000, 0x0800, 0x54fc873d );
		ROM_CONTINUE(             0x0000, 0x0800 );      /* overwrite with the real one - should be a 2716? */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_sidetrac	   = new GameDriver("1979"	,"sidetrac"	,"exidy.java"	,rom_sidetrac,null	,machine_driver_targ	,input_ports_sidetrac	,init_sidetrac	,ROT0	,	"Exidy", "Side Track" )
	public static GameDriver driver_targ	   = new GameDriver("1980"	,"targ"	,"exidy.java"	,rom_targ,null	,machine_driver_targ	,input_ports_targ	,init_targ	,ROT0	,	"Exidy", "Targ" )
	public static GameDriver driver_spectar	   = new GameDriver("1980"	,"spectar"	,"exidy.java"	,rom_spectar,null	,machine_driver_targ	,input_ports_spectar	,init_spectar	,ROT0	,	"Exidy", "Spectar (revision 3)" )
	public static GameDriver driver_spectar1	   = new GameDriver("1980"	,"spectar1"	,"exidy.java"	,rom_spectar1,driver_spectar	,machine_driver_targ	,input_ports_spectar	,init_spectar	,ROT0	,	"Exidy", "Spectar (revision 1?)" )
	GAMEX(1981, mtrap,    null,       mtrap,   mtrap,    mtrap,    ROT0, "Exidy", "Mouse Trap (version 5)", GAME_IMPERFECT_SOUND )
	GAMEX(1981, mtrap3,   mtrap,   mtrap,   mtrap,    mtrap,    ROT0, "Exidy", "Mouse Trap (version 3)", GAME_IMPERFECT_SOUND )
	GAMEX(1981, mtrap4,   mtrap,   mtrap,   mtrap,    mtrap,    ROT0, "Exidy", "Mouse Trap (version 4)", GAME_IMPERFECT_SOUND )
	GAMEX(1981, venture,  null,       venture, venture,  venture,  ROT0, "Exidy", "Venture (version 5 set 1)", GAME_IMPERFECT_SOUND )
	GAMEX(1981, venture2, venture, venture, venture,  venture,  ROT0, "Exidy", "Venture (version 5 set 2)", GAME_IMPERFECT_SOUND )
	GAMEX(1981, venture4, venture, venture, venture,  venture,  ROT0, "Exidy", "Venture (version 4)", GAME_IMPERFECT_SOUND )
	GAMEX(1982, pepper2,  null,       pepper2, pepper2,  pepper2,  ROT0, "Exidy", "Pepper II", GAME_IMPERFECT_SOUND )
	GAMEX(1982, hardhat,  null,       pepper2, pepper2,  pepper2,  ROT0, "Exidy", "Hard Hat", GAME_IMPERFECT_SOUND )
	GAMEX(1983, fax,      null,       fax,     fax,      fax,      ROT0, "Exidy", "Fax", GAME_IMPERFECT_SOUND )
}
