/***************************************************************************

  Karnov (USA version)                   (c) 1987 Data East USA
  Karnov (Japanese version)              (c) 1987 Data East Corporation
  Wonder Planet (Japanese version)       (c) 1987 Data East Corporation
  Chelnov (USA version)                  (c) 1988 Data East USA
  Chelnov (Japanese version)             (c) 1987 Data East Corporation


  Emulation by Bryan McPhail, mish@tendril.co.uk


NOTE!  Karnov USA  Karnov Japan sets have different gameplay!
  and Chelnov USA  Chelnov Japan sets have different gameplay!

These games use a 68000 main processor with a 6502, YM2203C and YM3526 for
sound.  Karnov was a major pain to get going because of the
'protection' on the main player sprite, probably connected to the Intel
microcontroller on the board.  The game is very sensitive to the wrong values
at the input ports...

There is another Karnov rom set - a bootleg version of the Japanese roms with
the Data East copyright removed - not supported because the original Japanese
roms work fine.

One of the two color PROMs for chelnov and chelnoj is different; one is most
likely a bad read, but I don't know which one.

Thanks to Oliver Stabel <stabel@rhein-neckar.netsurf.de> for confirming some
of the sprite  control information :)

Cheats:

Karnov - put 0x30 at 0x60201 to skip a level
Chelnov - level number at 0x60189 - enter a value at cartoon intro

*******************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package gr.codebb.arcadeflex.v036.drivers;
import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.cpu.m6502.m6502H.M6502_INT_NMI;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_r;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_w;
import static gr.codebb.arcadeflex.v037b7.sound._2203intf.*;
import static gr.codebb.arcadeflex.v037b7.sound._2203intfH.*;
import static gr.codebb.arcadeflex.v037b7.sound._3526intf.*;
import static gr.codebb.arcadeflex.v037b7.sound._3812intfH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.karnov.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.install_mem_read_handler;

public class karnov
{
		
	static int i8751_return;
	static int KARNOV, CHELNOV, WNDRPLNT; /* :) */
	static UBytePtr karnov_ram=new UBytePtr();
	
	/******************************************************************************/
	
	/* Emulation of the protected microcontroller - for coins & general protection */
	static void karnov_i8751_w(int data)
	{
		i8751_return=0;
		if (data==0x100 && KARNOV==2) i8751_return=0x56a; /* Japan version */
		if (data==0x100 && KARNOV==1) i8751_return=0x56b; /* USA version */
		if ((data&0xf00)==0x300) i8751_return=(data&0xff)*0x12; /* Player sprite mapping */
	
		/* I'm not sure the ones marked ^ appear in the right order */
		if (data==0x400) i8751_return=0x4000; /* Get The Map... */
		if (data==0x402) i8751_return=0x40a6; /* Ancient Ruins */
		if (data==0x403) i8751_return=0x4054; /* Forest... */
		if (data==0x404) i8751_return=0x40de; /* ^Rocky hills */
		if (data==0x405) i8751_return=0x4182; /* Sea */
		if (data==0x406) i8751_return=0x41ca; /* Town */
		if (data==0x407) i8751_return=0x421e; /* Desert */
		if (data==0x401) i8751_return=0x4138; /* ^Whistling wind */
		if (data==0x408) i8751_return=0x4276; /* ^Heavy Gates */
	
	//	if (errorlog && !i8751_return && data!=0x300) fprintf(errorlog,"CPU %04x - Unknown Write %02x intel\n",cpu_get_pc(),data);
	
		cpu_cause_interrupt(0,6); /* Signal main cpu task is complete */
	}
	
	static void wndrplnt_i8751_w(int data)
	{
	//	static int level;
	
		i8751_return=0;
		if (data==0x100) i8751_return=0x67a;
	//	if (data==0x200) i8751_return=0x214;
	
		/* USA version will have different values for these commands */
	
		if (data==0x300) i8751_return=0x17; /* Copyright text on title screen */
	//	if (data==0x300) i8751_return=0x1; /* (USA) Copyright text on title screen */
	
	if (errorlog!=null && data!=0x600) fprintf(errorlog,"CPU %04x - Unknown Write %02x intel\n",cpu_get_pc(),data);
	
		cpu_cause_interrupt(0,6); /* Signal main cpu task is complete */
	}
	static int level;
	static void chelnov_i8751_w(int data)
	{
		i8751_return=0;
		if (data==0x200 && CHELNOV==2) i8751_return=0x7734; /* Japan version */
		if (data==0x200 && CHELNOV==1) i8751_return=0x783e; /* USA version */
		if (data==0x100 && CHELNOV==2) i8751_return=0x71a; /* Japan version */
		if (data==0x100 && CHELNOV==1) i8751_return=0x71b; /* USA version */
		if (data>=0x6000 && data<0x8000) i8751_return=1;  /* patched */
		if ((data&0xf000)==0x1000) level=1; /* Level 1 */
		if ((data&0xf000)==0x2000) level++; /* Level Increment */
		if ((data&0xf000)==0x3000) {        /* Sprite table mapping */
			int b=data&0xff;
			switch (level) {
				case 1: /* Level 1, Sprite mapping tables */
					if (CHELNOV==1) { /* USA */
						if (b<2) i8751_return=0;
						else if (b<6) i8751_return=1;
						else if (b<0xb) i8751_return=2;
						else if (b<0xf) i8751_return=3;
						else if (b<0x13) i8751_return=4;
						else i8751_return=5;
					} else { /* Japan */
						if (b<3) i8751_return=0;
						else if (b<8) i8751_return=1;
						else if (b<0xc) i8751_return=2;
						else if (b<0x10) i8751_return=3;
						else if (b<0x19) i8751_return=4;
						else if (b<0x1b) i8751_return=5;
						else if (b<0x22) i8751_return=6;
						else if (b<0x28) i8751_return=7;
						else i8751_return=8;
					}
					break;
				case 2: /* Level 2, Sprite mapping tables, USA & Japan are the same */
					if (b<3) i8751_return=0;
					else if (b<9) i8751_return=1;
					else if (b<0x11) i8751_return=2;
					else if (b<0x1b) i8751_return=3;
					else if (b<0x21) i8751_return=4;
					else if (b<0x28) i8751_return=5;
					else i8751_return=6;
					break;
				case 3: /* Level 3, Sprite mapping tables, USA & Japan are the same */
					if (b<5) i8751_return=0;
					else if (b<9) i8751_return=1;
					else if (b<0xd) i8751_return=2;
					else if (b<0x11) i8751_return=3;
					else if (b<0x1b) i8751_return=4;
					else if (b<0x1c) i8751_return=5;
					else if (b<0x22) i8751_return=6;
					else if (b<0x27) i8751_return=7;
					else i8751_return=8;
					break;
				case 4: /* Level 4, Sprite mapping tables, USA & Japan are the same */
					if (b<4) i8751_return=0;
					else if (b<0xc) i8751_return=1;
					else if (b<0xf) i8751_return=2;
					else if (b<0x19) i8751_return=3;
					else if (b<0x1c) i8751_return=4;
					else if (b<0x22) i8751_return=5;
					else if (b<0x29) i8751_return=6;
					else i8751_return=7;
					break;
				case 5: /* Level 5, Sprite mapping tables */
					if (b<7) i8751_return=0;
					else if (b<0xe) i8751_return=1;
					else if (b<0x14) i8751_return=2;
					else if (b<0x1a) i8751_return=3;
					else if (b<0x23) i8751_return=4;
					else if (b<0x27) i8751_return=5;
					else i8751_return=6;
					break;
				case 6: /* Level 6, Sprite mapping tables */
					if (b<3) i8751_return=0;
					else if (b<0xb) i8751_return=1;
					else if (b<0x11) i8751_return=2;
					else if (b<0x17) i8751_return=3;
					else if (b<0x1d) i8751_return=4;
					else if (b<0x24) i8751_return=5;
					else i8751_return=6;
					break;
				case 7: /* Level 7, Sprite mapping tables */
					if (b<5) i8751_return=0;
					else if (b<0xb) i8751_return=1;
					else if (b<0x11) i8751_return=2;
					else if (b<0x1a) i8751_return=3;
					else if (b<0x21) i8751_return=4;
					else if (b<0x27) i8751_return=5;
					else i8751_return=6;
					break;
			}
		}
	
	//	if (errorlog && !i8751_return) fprintf(errorlog,"CPU %04x - Unknown Write %02x intel\n",cpu_get_pc(),data);
	
		cpu_cause_interrupt(0,6); /* Signal main cpu task is complete */
	}
	
	/******************************************************************************/
	
	public static WriteHandlerPtr karnov_control_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* Mnemonics filled in from the schematics, brackets are my comments */
		switch (offset) {
			case 0: /* SECLR (Interrupt ack for Level 6 i8751 interrupt) */
				return;
	
			case 2: /* SONREQ (Sound CPU byte) */
				soundlatch_w.handler(0,data&0xff);
				cpu_cause_interrupt (1, M6502_INT_NMI);
				break;
	
			case 4: /* DM (DMA to buffer spriteram) */
				buffer_spriteram_w.handler(0,0);
				break;
	
			case 6: /* SECREQ (Interrupt & Data to i8751) */
				if (KARNOV!=0) karnov_i8751_w(data);
				if (CHELNOV!=0) chelnov_i8751_w(data);
				if (WNDRPLNT!=0) wndrplnt_i8751_w(data);
				break;
	
			case 8: /* HSHIFT (9 bits) - Top bit indicates video flip */
				karnov_scroll_1.WRITE_WORD(0, data);
				break;
	
			case 0xa: /* VSHIFT */
				karnov_scroll_2.WRITE_WORD(0, data);
				break;
	
			case 0xc: /* SECR (Reset i8751) */
				if (errorlog!=null) fprintf(errorlog,"Reset i8751\n");
				i8751_return=0;
				break;
	
			case 0xe: /* INTCLR (Interrupt ack for Level 7 vbl interrupt) */
				break;
		}
	} };
	
	/******************************************************************************/
	
	public static ReadHandlerPtr karnov_control_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (offset) {
			case 0: /* Player controls */
				return ( readinputport(0) + (readinputport(1)<<8));
			case 2: /* Start buttons & VBL */
				return readinputport(2);
			case 4: /* Dipswitch A & B */
				return ( readinputport(4) + (readinputport(5)<<8));
			case 6: /* i8751 return values */
				return i8751_return;
		}
	
		return 0xffff;
	} };
	
	/******************************************************************************/
	
	public static WriteHandlerPtr videoram_mirror = new WriteHandlerPtr() { public void handler(int offset, int data)
        { 
            COMBINE_WORD_MEM(videoram,offset,data);
        } };
	
	static MemoryReadAddress karnov_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x05ffff, MRA_ROM ),
		new MemoryReadAddress( 0x060000, 0x063fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x080000, 0x080fff, MRA_BANK2 ),
		new MemoryReadAddress( 0x0a0000, 0x0a07ff, MRA_BANK3 ),
		new MemoryReadAddress( 0x0c0000, 0x0c0007, karnov_control_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress karnov_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x05ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x060000, 0x063fff, MWA_BANK1 , karnov_ram ),
		new MemoryWriteAddress( 0x080000, 0x080fff, MWA_BANK2 , spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x0a0000, 0x0a07ff, MWA_BANK3 , videoram, videoram_size ),
		new MemoryWriteAddress( 0x0a0800, 0x0a0fff, videoram_mirror ), /* Wndrplnt only */
		new MemoryWriteAddress( 0x0a1000, 0x0a1fff, karnov_foreground_w ),
		new MemoryWriteAddress( 0x0c0000, 0x0c000f, karnov_control_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	/******************************************************************************/
	
	static MemoryReadAddress karnov_s_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x05ff, MRA_RAM),
		new MemoryReadAddress( 0x0800, 0x0800, soundlatch_r ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress karnov_s_writemem[] =
	{
	 	new MemoryWriteAddress( 0x0000, 0x05ff, MWA_RAM),
		new MemoryWriteAddress( 0x1000, 0x1000, YM2203_control_port_0_w ), /* OPN */
		new MemoryWriteAddress( 0x1001, 0x1001, YM2203_write_port_0_w ),
		new MemoryWriteAddress( 0x1800, 0x1800, YM3526_control_port_0_w ), /* OPL */
		new MemoryWriteAddress( 0x1801, 0x1801, YM3526_write_port_0_w ),
	 	new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	/******************************************************************************/
	
	static InputPortPtr input_ports_karnov = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* Player 1 controls */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );/* Button 4 on schematics */
	
		PORT_START(); 	/* Player 2 controls */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );/* Button 4 on schematics */
	
		PORT_START(); 	/* start buttons */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );/* PL1 Button 5 on schematics */
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );/* PL2 Button 5 on schematics */
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1  );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START2  );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK );
	
		PORT_START(); 	/* Dummy input for i8751 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0xf8, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* Dip switch bank 1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	/* 0x80 called No Die Mode according to the manual, but it doesn't seem
	to have any effect */
	
		PORT_START(); 	/* Dip switch bank 2 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x01, "1" );
		PORT_DIPSETTING(    0x03, "3" );
		PORT_DIPSETTING(    0x02, "5" );
		PORT_BITX(0,        0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite", IP_KEY_NONE, IP_JOY_NONE );
		PORT_DIPNAME( 0x0c, 0x0c, "K needed for Bonus Life" );
		PORT_DIPSETTING(    0x0c, "50" );
		PORT_DIPSETTING(    0x08, "70" );
		PORT_DIPSETTING(    0x04, "90" );
		PORT_DIPSETTING(    0x00, "100" );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x30, "Easy" );
		PORT_DIPSETTING(    0x10, "Normal" );
		PORT_DIPSETTING(    0x20, "Hard" );
		PORT_DIPSETTING(    0x00, "Very Hard" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "Timer Speed" );
		PORT_DIPSETTING(    0x80, "Normal" );
		PORT_DIPSETTING(    0x00, "Fast" );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_chelnov = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* Player controls */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* Player 2 controls */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* start buttons */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1  );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START2  );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK );  /* Active_low is strange! */
	
		PORT_START(); 	/* Dummy input for i8751 */
		PORT_BIT( 0x1f, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN3 );
	
		PORT_START(); 	/* Dip switch bank 1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Cocktail") );
	
		PORT_START(); 	/* Dip switch bank 2 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x01, "1" );
		PORT_DIPSETTING(    0x03, "3" );
		PORT_DIPSETTING(    0x02, "5" );
		PORT_BITX(0,        0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite", IP_KEY_NONE, IP_JOY_NONE );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x04, "Easy" );
		PORT_DIPSETTING(    0x0c, "Normal" );
		PORT_DIPSETTING(    0x08, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x10, 0x10, "Allow Continue" );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "Freeze" );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	/******************************************************************************/
	
	static GfxLayout chars = new GfxLayout
	(
		8,8,
		1024,
		3,
		new int[] { 0x6000*8,0x4000*8,0x2000*8 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every sprite takes 8 consecutive bytes */
	);
	
	static GfxLayout sprites = new GfxLayout
	(
		16,16,
		4096,
		4,
	 	new int[] { 0x60000*8,0x00000*8,0x20000*8,0x40000*8 },
		new int[] { 16*8, 1+(16*8), 2+(16*8), 3+(16*8), 4+(16*8), 5+(16*8), 6+(16*8), 7+(16*8),
	  	0,1,2,3,4,5,6,7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 ,8*8,9*8,10*8,11*8,12*8,13*8,14*8,15*8},
		16*16
	);
	
	
	/* 16x16 tiles, 4 Planes, each plane is 0x10000 bytes */
	static GfxLayout tiles = new GfxLayout
	(
		16,16,
		2048,
		4,
	 	new int[] { 0x30000*8,0x00000*8,0x10000*8,0x20000*8 },
		new int[] { 16*8, 1+(16*8), 2+(16*8), 3+(16*8), 4+(16*8), 5+(16*8), 6+(16*8), 7+(16*8),
	  	0,1,2,3,4,5,6,7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 ,8*8,9*8,10*8,11*8,12*8,13*8,14*8,15*8},
		16*16
	);
	
	static GfxDecodeInfo karnov_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, chars,     0,  4 ),	/* colors 0-31 */
		new GfxDecodeInfo( REGION_GFX2, 0, tiles,   512, 16 ),	/* colors 512-767 */
		new GfxDecodeInfo( REGION_GFX3, 0, sprites, 256, 16 ),	/* colors 256-511 */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	/******************************************************************************/
	static int latch;
	public static InterruptPtr karnov_interrupt = new InterruptPtr() { public int handler() 
	{
		
	
		/* Coin input to the i8751 generates an interrupt to the main cpu */
		if (readinputport(3) == 0xff) latch=1;
		if (readinputport(3) != 0xff && latch!=0) {
			i8751_return=readinputport(3) | 0x8000;
			cpu_cause_interrupt(0,6);
			latch=0;
		}
	
		return 7;	/* VBL */
	} };
	public static WriteYmHandlerPtr sound_irq = new WriteYmHandlerPtr() {
        public void handler(int linestate) {
		cpu_set_irq_line(1,0,linestate); /* IRQ */
	}};
	
	static YM2203interface ym2203_interface = new YM2203interface
	(
		1,
		1500000,	/* Accurate */
		new int[] { YM2203_VOL(20,20) },
		new ReadHandlerPtr[] { null },
		new ReadHandlerPtr[] { null },
		new WriteHandlerPtr[] { null },
		new WriteHandlerPtr[] { null }
	);
	
	static YM3526interface ym3526_interface = new YM3526interface
	(
		1,			/* 1 chip */
		3000000,	/* Accurate */
		new int[] { 60 },	/*  */
		new WriteYmHandlerPtr[] { sound_irq }
	);
	
	/******************************************************************************/
	public static InitMachinePtr karnov_reset_init = new InitMachinePtr() {
        public void handler() {
		memset(karnov_ram,0,0x4000); /* Chelnov likes ram clear on reset.. */
	}};
	
	static MachineDriver machine_driver_karnov = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				10000000,	/* 10 Mhz */
				karnov_readmem,karnov_writemem,null,null,
				karnov_interrupt,1
			),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				1500000,	/* Accurate */
				karnov_s_readmem,karnov_s_writemem,null,null,
				ignore_interrupt,0	/* Interrupts from OPL chip */
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION*2,
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		karnov_reset_init,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 1*8, 31*8-1 ),
	
		karnov_gfxdecodeinfo,
		1024, 1024,
		karnov_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER | VIDEO_BUFFERS_SPRITERAM,
		null,
		karnov_vh_start,
		karnov_vh_stop,
		karnov_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			),
			new MachineSound(
				SOUND_YM3526,
				ym3526_interface
			)
		}
	);
	
	static MachineDriver machine_driver_wndrplnt = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				10000000,	/* 10 Mhz */
				karnov_readmem,karnov_writemem,null,null,
				karnov_interrupt,1
			),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				1500000,	/* Accurate */
				karnov_s_readmem,karnov_s_writemem,null,null,
				ignore_interrupt,0	/* Interrupts from OPL chip */
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION*2,
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		karnov_reset_init,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 1*8, 31*8-1 ),
	
		karnov_gfxdecodeinfo,
		1024, 1024,
		karnov_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER | VIDEO_BUFFERS_SPRITERAM,
		null,
		karnov_vh_start,
		karnov_vh_stop,
		wndrplnt_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			),
			new MachineSound(
				SOUND_YM3526,
				ym3526_interface
			)
		}
	);
	
	/******************************************************************************/
	
	static RomLoadPtr rom_karnov = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x60000, REGION_CPU1 );/* 6*64k for 68000 code */
		ROM_LOAD_EVEN( "dn08-5",       0x00000, 0x10000, 0xdb92c264 );
		ROM_LOAD_ODD ( "dn11-5",       0x00000, 0x10000, 0x05669b4b );
		ROM_LOAD_EVEN( "dn07-",        0x20000, 0x10000, 0xfc14291b );
		ROM_LOAD_ODD ( "dn10-",        0x20000, 0x10000, 0xa4a34e37 );
		ROM_LOAD_EVEN( "dn06-5",       0x40000, 0x10000, 0x29d64e42 );
		ROM_LOAD_ODD ( "dn09-5",       0x40000, 0x10000, 0x072d7c49 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 6502 Sound CPU */
		ROM_LOAD( "dn05-5",       0x8000, 0x8000, 0xfa1a31a8 );
	
		ROM_REGION( 0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "dn00-",        0x00000, 0x08000, 0x0ed77c6d );/* Characters */
	
		ROM_REGION( 0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "dn04-",        0x00000, 0x10000, 0xa9121653 );/* Backgrounds */
		ROM_LOAD( "dn01-",        0x10000, 0x10000, 0x18697c9e );
		ROM_LOAD( "dn03-",        0x20000, 0x10000, 0x90d9dd9c );
		ROM_LOAD( "dn02-",        0x30000, 0x10000, 0x1e04d7b9 );
	
		ROM_REGION( 0x80000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "dn12-",        0x00000, 0x10000, 0x9806772c );/* Sprites - 2 sets of 4, interleaved here */
		ROM_LOAD( "dn14-5",       0x10000, 0x08000, 0xac9e6732 );
		ROM_LOAD( "dn13-",        0x20000, 0x10000, 0xa03308f9 );
		ROM_LOAD( "dn15-5",       0x30000, 0x08000, 0x8933fcb8 );
		ROM_LOAD( "dn16-",        0x40000, 0x10000, 0x55e63a11 );
		ROM_LOAD( "dn17-5",       0x50000, 0x08000, 0xb70ae950 );
		ROM_LOAD( "dn18-",        0x60000, 0x10000, 0x2ad53213 );
		ROM_LOAD( "dn19-5",       0x70000, 0x08000, 0x8fd4fa40 );
	
		ROM_REGION( 0x0800, REGION_PROMS );
		ROM_LOAD( "karnprom.21",  0x0000, 0x0400, 0xaab0bb93 );
		ROM_LOAD( "karnprom.20",  0x0400, 0x0400, 0x02f78ffb );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_karnovj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x60000, REGION_CPU1 );/* 6*64k for 68000 code */
		ROM_LOAD_EVEN( "kar8",         0x00000, 0x10000, 0x3e17e268 );
		ROM_LOAD_ODD ( "kar11",        0x00000, 0x10000, 0x417c936d );
		ROM_LOAD_EVEN( "dn07-",        0x20000, 0x10000, 0xfc14291b );
		ROM_LOAD_ODD ( "dn10-",        0x20000, 0x10000, 0xa4a34e37 );
		ROM_LOAD_EVEN( "kar6",         0x40000, 0x10000, 0xc641e195 );
		ROM_LOAD_ODD ( "kar9",         0x40000, 0x10000, 0xd420658d );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 6502 Sound CPU */
		ROM_LOAD( "kar5",         0x8000, 0x8000, 0x7c9158f1 );
	
		ROM_REGION( 0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "dn00-",        0x00000, 0x08000, 0x0ed77c6d );/* Characters */
	
		ROM_REGION( 0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "dn04-",        0x00000, 0x10000, 0xa9121653 );/* Backgrounds */
		ROM_LOAD( "dn01-",        0x10000, 0x10000, 0x18697c9e );
		ROM_LOAD( "dn03-",        0x20000, 0x10000, 0x90d9dd9c );
		ROM_LOAD( "dn02-",        0x30000, 0x10000, 0x1e04d7b9 );
	
		ROM_REGION( 0x80000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "dn12-",        0x00000, 0x10000, 0x9806772c );/* Sprites - 2 sets of 4, interleaved here */
		ROM_LOAD( "kar14",        0x10000, 0x08000, 0xc6b39595 );
		ROM_LOAD( "dn13-",        0x20000, 0x10000, 0xa03308f9 );
		ROM_LOAD( "kar15",        0x30000, 0x08000, 0x2f72cac0 );
		ROM_LOAD( "dn16-",        0x40000, 0x10000, 0x55e63a11 );
		ROM_LOAD( "kar17",        0x50000, 0x08000, 0x7851c70f );
		ROM_LOAD( "dn18-",        0x60000, 0x10000, 0x2ad53213 );
		ROM_LOAD( "kar19",        0x70000, 0x08000, 0x7bc174bb );
	
		ROM_REGION( 0x0800, REGION_PROMS );
		ROM_LOAD( "karnprom.21",  0x0000, 0x0400, 0xaab0bb93 );
		ROM_LOAD( "karnprom.20",  0x0400, 0x0400, 0x02f78ffb );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_wndrplnt = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x60000, REGION_CPU1 );/* 6*64k for 68000 code */
		ROM_LOAD_EVEN( "ea08.bin",   0x00000, 0x10000, 0xb0578a14 );
		ROM_LOAD_ODD ( "ea11.bin",   0x00000, 0x10000, 0x271edc6c );
		ROM_LOAD_EVEN( "ea07.bin",   0x20000, 0x10000, 0x7095a7d5 );
		ROM_LOAD_ODD ( "ea10.bin",   0x20000, 0x10000, 0x81a96475 );
		ROM_LOAD_EVEN( "ea06.bin",   0x40000, 0x10000, 0x5951add3 );
		ROM_LOAD_ODD ( "ea09.bin",   0x40000, 0x10000, 0xc4b3cb1e );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 6502 Sound CPU */
		ROM_LOAD( "ea05.bin",     0x8000, 0x8000, 0x8dbb6231 );
	
		ROM_REGION( 0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ea00.bin",    0x00000, 0x08000, 0x9f3cac4c );/* Characters */
	
		ROM_REGION( 0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ea04.bin",    0x00000, 0x10000, 0x7d701344 );/* Backgrounds */
		ROM_LOAD( "ea01.bin",    0x10000, 0x10000, 0x18df55fb );
		ROM_LOAD( "ea03.bin",    0x20000, 0x10000, 0x922ef050 );
		ROM_LOAD( "ea02.bin",    0x30000, 0x10000, 0x700fde70 );
	
		ROM_REGION( 0x80000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ea12.bin",    0x00000, 0x10000, 0xa6d4e99d );/* Sprites - 2 sets of 4, interleaved here */
		ROM_LOAD( "ea14.bin",    0x10000, 0x10000, 0x915ffdc9 );
		ROM_LOAD( "ea13.bin",    0x20000, 0x10000, 0xcd839f3a );
		ROM_LOAD( "ea15.bin",    0x30000, 0x10000, 0xa1f14f16 );
		ROM_LOAD( "ea16.bin",    0x40000, 0x10000, 0x7a1d8a9c );
		ROM_LOAD( "ea17.bin",    0x50000, 0x10000, 0x21a3223d );
		ROM_LOAD( "ea18.bin",    0x60000, 0x10000, 0x3fb2cec7 );
		ROM_LOAD( "ea19.bin",    0x70000, 0x10000, 0x87cf03b5 );
	
		ROM_REGION( 0x0800, REGION_PROMS );
		ROM_LOAD( "ea21.prm",      0x0000, 0x0400, 0xc8beab49 );
		ROM_LOAD( "ea20.prm",      0x0400, 0x0400, 0x619f9d1e );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_chelnov = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x60000, REGION_CPU1 );/* 6*64k for 68000 code */
		ROM_LOAD_EVEN( "ee08-a.j15",   0x00000, 0x10000, 0x2f2fb37b );
		ROM_LOAD_ODD ( "ee11-a.j20",   0x00000, 0x10000, 0xf306d05f );
		ROM_LOAD_EVEN( "ee07-a.j14",   0x20000, 0x10000, 0x9c69ed56 );
		ROM_LOAD_ODD ( "ee10-a.j18",   0x20000, 0x10000, 0xd5c5fe4b );
		ROM_LOAD_EVEN( "ee06-e.j13",   0x40000, 0x10000, 0x55acafdb );
		ROM_LOAD_ODD ( "ee09-e.j17",   0x40000, 0x10000, 0x303e252c );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 6502 Sound CPU */
		ROM_LOAD( "ee05-.f3",     0x8000, 0x8000, 0x6a8936b4 );
	
		ROM_REGION( 0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ee00-e.c5",    0x00000, 0x08000, 0xe06e5c6b );/* Characters */
	
		ROM_REGION( 0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ee04-.d18",    0x00000, 0x10000, 0x96884f95 );/* Backgrounds */
		ROM_LOAD( "ee01-.c15",    0x10000, 0x10000, 0xf4b54057 );
		ROM_LOAD( "ee03-.d15",    0x20000, 0x10000, 0x7178e182 );
		ROM_LOAD( "ee02-.c18",    0x30000, 0x10000, 0x9d7c45ae );
	
		ROM_REGION( 0x80000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ee12-.f8",     0x00000, 0x10000, 0x9b1c53a5 );/* Sprites */
		ROM_LOAD( "ee13-.f9",     0x20000, 0x10000, 0x72b8ae3e );
		ROM_LOAD( "ee14-.f13",    0x40000, 0x10000, 0xd8f4bbde );
		ROM_LOAD( "ee15-.f15",    0x60000, 0x10000, 0x81e3e68b );
	
		ROM_REGION( 0x0800, REGION_PROMS );
		ROM_LOAD( "ee21.k8",      0x0000, 0x0400, 0xb1db6586 );/* different from the other set; */
																/* might be bad */
		ROM_LOAD( "ee20.l6",      0x0400, 0x0400, 0x41816132 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_chelnovj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x60000, REGION_CPU1 );/* 6*64k for 68000 code */
		ROM_LOAD_EVEN( "a-j15.bin",    0x00000, 0x10000, 0x1978cb52 );
		ROM_LOAD_ODD ( "a-j20.bin",    0x00000, 0x10000, 0xe0ed3d99 );
		ROM_LOAD_EVEN( "a-j14.bin",    0x20000, 0x10000, 0x51465486 );
		ROM_LOAD_ODD ( "a-j18.bin",    0x20000, 0x10000, 0xd09dda33 );
		ROM_LOAD_EVEN( "a-j13.bin",    0x40000, 0x10000, 0xcd991507 );
		ROM_LOAD_ODD ( "a-j17.bin",    0x40000, 0x10000, 0x977f601c );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 6502 Sound CPU */
		ROM_LOAD( "ee05-.f3",     0x8000, 0x8000, 0x6a8936b4 );
	
		ROM_REGION( 0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "a-c5.bin",     0x00000, 0x08000, 0x1abf2c6d );/* Characters */
	
		ROM_REGION( 0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ee04-.d18",    0x00000, 0x10000, 0x96884f95 );/* Backgrounds */
		ROM_LOAD( "ee01-.c15",    0x10000, 0x10000, 0xf4b54057 );
		ROM_LOAD( "ee03-.d15",    0x20000, 0x10000, 0x7178e182 );
		ROM_LOAD( "ee02-.c18",    0x30000, 0x10000, 0x9d7c45ae );
	
		ROM_REGION( 0x80000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ee12-.f8",     0x00000, 0x10000, 0x9b1c53a5 );/* Sprites */
		ROM_LOAD( "ee13-.f9",     0x20000, 0x10000, 0x72b8ae3e );
		ROM_LOAD( "ee14-.f13",    0x40000, 0x10000, 0xd8f4bbde );
		ROM_LOAD( "ee15-.f15",    0x60000, 0x10000, 0x81e3e68b );
	
		ROM_REGION( 0x0800, REGION_PROMS );
		ROM_LOAD( "a-k7.bin",     0x0000, 0x0400, 0x309c49d8 );/* different from the other set; */
																/* might be bad */
		ROM_LOAD( "ee20.l6",      0x0400, 0x0400, 0x41816132 );
	ROM_END(); }}; 
	
	/******************************************************************************/
	
	public static ReadHandlerPtr karnov_cycle_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (cpu_get_pc()==0x8f2 && (karnov_ram.READ_WORD(0)&0xff00)!=0) 
                {cpu_spinuntil_int(); return 0;} return karnov_ram.READ_WORD(0);
	} };
	
	public static ReadHandlerPtr karnovj_cycle_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (cpu_get_pc()==0x8ec && (karnov_ram.READ_WORD(0)&0xff00)!=0) {cpu_spinuntil_int(); return 0;} return karnov_ram.READ_WORD(0);
	} };
	
	public static ReadHandlerPtr chelnov_cycle_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (cpu_get_pc()==0xdfe && (karnov_ram.READ_WORD(0)&0xff00)!=0) {cpu_spinuntil_int(); return 0;} return karnov_ram.READ_WORD(0);
	} };
	
	public static ReadHandlerPtr chelnovj_cycle_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (cpu_get_pc()==0xe06 && (karnov_ram.READ_WORD(0)&0xff00)!=0) {cpu_spinuntil_int(); return 0;} return karnov_ram.READ_WORD(0);
	} };
	
	public static InitDriverPtr init_karnov = new InitDriverPtr() { public void handler() 
	{
		if (strcmp(Machine.gamedrv.name,"karnov")==0) {
			install_mem_read_handler(0, 0x60000, 0x60001, karnov_cycle_r);
			KARNOV=1;
			CHELNOV=WNDRPLNT=0;
		}
	
		if (strcmp(Machine.gamedrv.name,"karnovj")==0) {
			install_mem_read_handler(0, 0x60000, 0x60001, karnovj_cycle_r);
			KARNOV=2;
			CHELNOV=WNDRPLNT=0;
		}
	
		if (strcmp(Machine.gamedrv.name,"wndrplnt")==0) {
	//		install_mem_read_handler(0, 0x60000, 0x60001, karnovj_cycle_r);
			KARNOV=CHELNOV=0;
			WNDRPLNT=1;
		}
	
		if (strcmp(Machine.gamedrv.name,"chelnov")==0) {
			install_mem_read_handler(0, 0x60000, 0x60001, chelnov_cycle_r);
			KARNOV=WNDRPLNT=0;
			CHELNOV=1;
		}
	
		if (strcmp(Machine.gamedrv.name,"chelnovj")==0) {
			install_mem_read_handler(0, 0x60000, 0x60001, chelnovj_cycle_r);
			KARNOV=WNDRPLNT=0;
			CHELNOV=2;
		}
	} };
	
	public static InitDriverPtr init_wndrplnt = new InitDriverPtr() { public void handler() 
	{
	//	UBytePtr RAM = memory_region(REGION_CPU1);
	
		init_karnov.handler();
	
	//	WRITE_WORD (&RAM[0x1106],0x4E71);
	//	WRITE_WORD (&RAM[0x110e],0x4E71);
	//	WRITE_WORD (&RAM[0xc0c],0x4E71);
	//	WRITE_WORD (&RAM[0xc0e],0x4E71);
	//	WRITE_WORD (&RAM[0xc4c],0x4E71);
	//	WRITE_WORD (&RAM[0xc0e],0x4E71);
	//WRITE_WORD (&RAM[0x5b0a],0x4E71);
	//WRITE_WORD (&RAM[0x5b0c],0x4E71);
	//WRITE_WORD (&RAM[0x5b0e],0x4E71);
	//WRITE_WORD (&RAM[0x5b1e],0x4E71);
	//WRITE_WORD (&RAM[0xd58],0x4E71);
	} };
	
	public static InitDriverPtr init_chelnov = new InitDriverPtr() { public void handler() 
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		init_karnov.handler();
	
		RAM.WRITE_WORD(0x0A26,0x4E71);  /* removes a protection lookup table */
		RAM.WRITE_WORD(0x062a,0x4E71);  /* hangs waiting on i8751 int */
	} };
	
	public static InitDriverPtr init_chelnovj = new InitDriverPtr() { public void handler() 
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		init_karnov.handler();
	
		RAM.WRITE_WORD(0x0A2E,0x4E71);  /* removes a protection lookup table */
		RAM.WRITE_WORD(0x062a,0x4E71);  /* hangs waiting on i8751 int */
	} };
	
	/******************************************************************************/
	
	public static GameDriver driver_karnov	   = new GameDriver("1987"	,"karnov"	,"karnov.java"	,rom_karnov,null	,machine_driver_karnov	,input_ports_karnov	,init_karnov	,ROT0	,	"Data East USA", "Karnov (US)", GAME_NO_COCKTAIL );
	public static GameDriver driver_karnovj	   = new GameDriver("1987"	,"karnovj"	,"karnov.java"	,rom_karnovj,driver_karnov	,machine_driver_karnov	,input_ports_karnov	,init_karnov	,ROT0	,	"Data East Corporation", "Karnov (Japan)", GAME_NO_COCKTAIL );
	public static GameDriver driver_wndrplnt	   = new GameDriver("1987"	,"wndrplnt"	,"karnov.java"	,rom_wndrplnt,null	,machine_driver_wndrplnt	,input_ports_karnov	,init_wndrplnt	,ROT270	,	"Data East Corporation", "Wonder Planet (Japan)", GAME_NOT_WORKING | GAME_NO_COCKTAIL );
	public static GameDriver driver_chelnov	   = new GameDriver("1988"	,"chelnov"	,"karnov.java"	,rom_chelnov,null	,machine_driver_karnov	,input_ports_chelnov	,init_chelnov	,ROT0	,	"Data East USA", "Chelnov - Atomic Runner (US)", GAME_NO_COCKTAIL );
	public static GameDriver driver_chelnovj	   = new GameDriver("1988"	,"chelnovj"	,"karnov.java"	,rom_chelnovj,driver_chelnov	,machine_driver_karnov	,input_ports_chelnov	,init_chelnovj	,ROT0	,	"Data East Corporation", "Chelnov - Atomic Runner (Japan)", GAME_NO_COCKTAIL );
}
