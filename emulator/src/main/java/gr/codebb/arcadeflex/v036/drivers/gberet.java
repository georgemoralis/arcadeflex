/*
 * ported to v0.36
 * using automatic conversion tool v0.08 + manual
 */ 
package gr.codebb.arcadeflex.v036.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.gberet.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.sn76496H.*;
import static gr.codebb.arcadeflex.v036.sound.sn76496.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.platform.input.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;

public class gberet
{
	
	
	public static WriteHandlerPtr gberet_coincounter_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* bits 0/1 = coin counters */
		coin_counter_w.handler(0,data & 1);
		coin_counter_w.handler(1,data & 2);
	} };
	
	public static WriteHandlerPtr mrgoemon_bankswitch_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
		int offs;
	
		/* bits 0/1 = coin counters */
		coin_counter_w.handler(0,data & 1);
		coin_counter_w.handler(1,data & 2);
	
		/* bits 5-7 = ROM bank select */
		offs = 0x10000 + ((data & 0xe0) >> 5) * 0x800;
		cpu_setbank(1,new UBytePtr(RAM,offs));
	} };
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0xbfff, MRA_ROM ),
		new MemoryReadAddress( 0xc000, 0xe03f, MRA_RAM ),
		new MemoryReadAddress( 0xf200, 0xf200, input_port_4_r ),	/* DSW1 */
		new MemoryReadAddress( 0xf400, 0xf400, input_port_5_r ),	/* DSW2 */
		new MemoryReadAddress( 0xf600, 0xf600, input_port_3_r ),	/* DSW0 */
		new MemoryReadAddress( 0xf601, 0xf601, input_port_1_r ),	/* IN1 */
		new MemoryReadAddress( 0xf602, 0xf602, input_port_0_r ),	/* IN0 */
		new MemoryReadAddress( 0xf603, 0xf603, input_port_2_r ),	/* IN2 */
		new MemoryReadAddress( 0xf800, 0xf800, MRA_NOP ),	/* gberetb only - IRQ acknowledge */
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new MemoryWriteAddress( 0xc000, 0xc7ff, gberet_colorram_w, gberet_colorram ),
		new MemoryWriteAddress( 0xc800, 0xcfff, gberet_videoram_w, gberet_videoram ),
		new MemoryWriteAddress( 0xd000, 0xd0bf, MWA_RAM, spriteram_2 ),
		new MemoryWriteAddress( 0xd100, 0xd1bf, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0xd200, 0xdfff, MWA_RAM ),
		new MemoryWriteAddress( 0xe000, 0xe03f, gberet_scroll_w, gberet_scrollram ),
		new MemoryWriteAddress( 0xe043, 0xe043, MWA_RAM, gberet_spritebank ),
		new MemoryWriteAddress( 0xe044, 0xe044, gberet_e044_w ),
		new MemoryWriteAddress( 0xf000, 0xf000, gberet_coincounter_w ),
		new MemoryWriteAddress( 0xf200, 0xf200, MWA_NOP ),		/* Loads the snd command into the snd latch */
		new MemoryWriteAddress( 0xf400, 0xf400, SN76496_0_w ),	/* This address triggers the SN chip to read the data port. */
	//	new MemoryWriteAddress( 0xf600, 0xf600, MWA_NOP ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress gberetb_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new MemoryWriteAddress( 0xc000, 0xc7ff, gberet_colorram_w, gberet_colorram ),
		new MemoryWriteAddress( 0xc800, 0xcfff, gberet_videoram_w, gberet_videoram ),
		new MemoryWriteAddress( 0xd000, 0xd0ff, MWA_RAM ),
		new MemoryWriteAddress( 0xd100, 0xd1ff, MWA_RAM ),
		new MemoryWriteAddress( 0xd200, 0xdfff, MWA_RAM ),
		new MemoryWriteAddress( 0xe000, 0xe03f, MWA_RAM ),
	//	new MemoryWriteAddress( 0xe800, 0xe8ff, MWA_RAM ),
		new MemoryWriteAddress( 0xe900, 0xe9ff, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0xf800, 0xf800, MWA_NOP ),	/* NMI acknowledge */
		new MemoryWriteAddress( 0xf900, 0xf901, gberetb_scroll_w ),
	//	new MemoryWriteAddress( 0xe043, 0xe043, MWA_RAM, gberet_spritebank ),
		new MemoryWriteAddress( 0xe044, 0xe044, gberet_e044_w ),
		new MemoryWriteAddress( 0xf400, 0xf400, SN76496_0_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress mrgoemon_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0xbfff, MRA_ROM ),
		new MemoryReadAddress( 0xc000, 0xe03f, MRA_RAM ),
		new MemoryReadAddress( 0xf200, 0xf200, input_port_4_r ),	/* DSW1 */
		new MemoryReadAddress( 0xf400, 0xf400, input_port_5_r ),	/* DSW2 */
		new MemoryReadAddress( 0xf600, 0xf600, input_port_3_r ),	/* DSW0 */
		new MemoryReadAddress( 0xf601, 0xf601, input_port_1_r ),	/* IN1 */
		new MemoryReadAddress( 0xf602, 0xf602, input_port_0_r ),	/* IN0 */
		new MemoryReadAddress( 0xf603, 0xf603, input_port_2_r ),	/* IN2 */
		new MemoryReadAddress( 0xf800, 0xffff, MRA_BANK1 ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress mrgoemon_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new MemoryWriteAddress( 0xc000, 0xc7ff, gberet_colorram_w, gberet_colorram ),
		new MemoryWriteAddress( 0xc800, 0xcfff, gberet_videoram_w, gberet_videoram ),
		new MemoryWriteAddress( 0xd000, 0xd0bf, MWA_RAM, spriteram_2 ),
		new MemoryWriteAddress( 0xd100, 0xd1bf, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0xd200, 0xdfff, MWA_RAM ),
		new MemoryWriteAddress( 0xe000, 0xe03f, gberet_scroll_w, gberet_scrollram ),
		new MemoryWriteAddress( 0xe043, 0xe043, MWA_RAM, gberet_spritebank ),
		new MemoryWriteAddress( 0xe044, 0xe044, gberet_e044_w ),
		new MemoryWriteAddress( 0xf000, 0xf000, mrgoemon_bankswitch_w ),	/* + coin counters */
		new MemoryWriteAddress( 0xf200, 0xf200, MWA_NOP ),		/* Loads the snd command into the snd latch */
		new MemoryWriteAddress( 0xf400, 0xf400, SN76496_0_w ),	/* This address triggers the SN chip to read the data port. */
		new MemoryWriteAddress( 0xf800, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	
	static InputPortPtr input_ports_gberet = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
		/* 0x00 is invalid */
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x03, 0x02, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "2" );
		PORT_DIPSETTING(    0x02, "3" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPSETTING(    0x00, "7" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x18, "30000 70000" );
		PORT_DIPSETTING(    0x10, "40000 80000" );
		PORT_DIPSETTING(    0x08, "50000 100000" );
		PORT_DIPSETTING(    0x00, "50000 200000" );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x60, "Easy" );
		PORT_DIPSETTING(    0x40, "Medium" );
		PORT_DIPSETTING(    0x20, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW2 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Controls" );
		PORT_DIPSETTING(    0x02, "Single" );
		PORT_DIPSETTING(    0x00, "Dual" );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR ( "Unknown" ));
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR ( "Unknown" ));
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	/* IN2 is different and IN1 and DSW0 are swapped */
	static InputPortPtr input_ports_gberetb = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
		/* 0x00 is invalid */
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x03, 0x02, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "2" );
		PORT_DIPSETTING(    0x02, "3" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPSETTING(    0x00, "7" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x18, "30000 70000" );
		PORT_DIPSETTING(    0x10, "40000 80000" );
		PORT_DIPSETTING(    0x08, "50000 100000" );
		PORT_DIPSETTING(    0x00, "50000 200000" );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x60, "Easy" );
		PORT_DIPSETTING(    0x40, "Medium" );
		PORT_DIPSETTING(    0x20, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW2 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Controls" );
		PORT_DIPSETTING(    0x02, "Single" );
		PORT_DIPSETTING(    0x00, "Dual" );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR ( "Unknown" ));
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR ( "Unknown" ));
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_mrgoemon = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
		/* 0x00 is invalid */
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x03, 0x02, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "2" );
		PORT_DIPSETTING(    0x02, "3" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPSETTING(    0x00, "7" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x18, "20000 and every 60000" );
		PORT_DIPSETTING(    0x10, "30000 and every 70000" );
		PORT_DIPSETTING(    0x08, "40000 and every 80000" );
		PORT_DIPSETTING(    0x00, "50000 and every 90000" );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x60, "Easy" );
		PORT_DIPSETTING(    0x40, "Medium" );
		PORT_DIPSETTING(    0x20, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW2 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Controls" );
		PORT_DIPSETTING(    0x02, "Single" );
		PORT_DIPSETTING(    0x00, "Dual" );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR ( "Unknown" ));
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		512,	/* 512 characters */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },	/* the four bitplanes are packed in one nibble */
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8	/* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		512,	/* 512 sprites */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },	/* the four bitplanes are packed in one nibble */
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4,
			32*8+0*4, 32*8+1*4, 32*8+2*4, 32*8+3*4, 32*8+4*4, 32*8+5*4, 32*8+6*4, 32*8+7*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32,
			64*8+0*32, 64*8+1*32, 64*8+2*32, 64*8+3*32, 64*8+4*32, 64*8+5*32, 64*8+6*32, 64*8+7*32 },
		128*8	/* every sprite takes 128 consecutive bytes */
	);
	
	static GfxLayout gberetb_charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		512,	/* 512 characters */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },	/* the four bitplanes are packed in one nibble */
		new int[] { 6*4, 7*4, 0*4, 1*4, 2*4, 3*4, 4*4, 5*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8	/* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout gberetb_spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		512,	/* 512 sprites */
		4,	/* 4 bits per pixel */
		new int[] { 0*0x4000*8, 1*0x4000*8, 2*0x4000*8, 3*0x4000*8 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,
			16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
			8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		32*8	/* every sprite takes 32 consecutive bytes */
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,       0, 16 ),
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout, 16*16, 16 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo gberetb_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, gberetb_charlayout,       0, 16 ),
		new GfxDecodeInfo( REGION_GFX2, 0, gberetb_spritelayout, 16*16, 16 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static SN76496interface sn76496_interface = new SN76496interface
	(
		1,	/* 1 chip */
		new int[]{ 18432000/12 },	/* 2H (generated by a custom IC) */
		new int[]{ 100 }
	);
	
	
	
	static MachineDriver machine_driver_gberet = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				18432000/6,	/* X1S (generated by a custom IC) */
				readmem,writemem,null,null,
				gberet_interrupt,32	/* 1 IRQ + 16 NMI (generated by a custom IC) */
			)
		},
		30, DEFAULT_30HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 1*8, 31*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		32,2*16*16,
		gberet_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		gberet_vh_start,
		null,
		gberet_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
                    new MachineSound
                    (
				SOUND_SN76496,
				sn76496_interface
                     )
		}
	);
	
	static MachineDriver machine_driver_gberetb = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				3072000,	/* 3.072 MHz ?? */
				readmem,gberetb_writemem,null,null,
				gberet_interrupt,16	/* 1 IRQ + 8 NMI */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 1*8, 31*8-1, 2*8, 30*8-1 ),
		gberetb_gfxdecodeinfo,
		32,2*16*16,
		gberet_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		gberet_vh_start,
		null,
		gberet_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
                    new MachineSound
                    (
				SOUND_SN76496,
				sn76496_interface
                     )
		}
	);
	
	static MachineDriver machine_driver_mrgoemon = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				18432000/6,	/* X1S (generated by a custom IC) */
				mrgoemon_readmem,mrgoemon_writemem,null,null,
				gberet_interrupt,16	/* 1 IRQ + 8 NMI */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 1*8, 31*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		32,2*16*16,
		gberet_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		gberet_vh_start,
		null,
		gberet_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
                    new MachineSound
                    (
				SOUND_SN76496,
				sn76496_interface
                     )
		}
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_gberet = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "c10_l03.bin",  0x0000, 0x4000, 0xae29e4ff );
		ROM_LOAD( "c08_l02.bin",  0x4000, 0x4000, 0x240836a5 );
		ROM_LOAD( "c07_l01.bin",  0x8000, 0x4000, 0x41fa3e1f );
	
		ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "f03_l07.bin",  0x00000, 0x4000, 0x4da7bd1b );
	
		ROM_REGION( 0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "e05_l06.bin",  0x00000, 0x4000, 0x0f1cb0ca );
		ROM_LOAD( "e04_l05.bin",  0x04000, 0x4000, 0x523a8b66 );
		ROM_LOAD( "f04_l08.bin",  0x08000, 0x4000, 0x883933a4 );
		ROM_LOAD( "e03_l04.bin",  0x0c000, 0x4000, 0xccecda4c );
	
		ROM_REGION( 0x0220, REGION_PROMS );
		ROM_LOAD( "577h09",       0x0000, 0x0020, 0xc15e7c80 );/* palette */
		ROM_LOAD( "577h10",       0x0020, 0x0100, 0xe9de1e53 );/* sprites */
		ROM_LOAD( "577h11",       0x0120, 0x0100, 0x2a1a992b );/* characters */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_rushatck = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "rush_h03.10c", 0x0000, 0x4000, 0x4d276b52 );
		ROM_LOAD( "rush_h02.8c",  0x4000, 0x4000, 0xb5802806 );
		ROM_LOAD( "rush_h01.7c",  0x8000, 0x4000, 0xda7c8f3d );
	
		ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "rush_h07.3f",  0x00000, 0x4000, 0x03f9815f );
	
		ROM_REGION( 0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "e05_l06.bin",  0x00000, 0x4000, 0x0f1cb0ca );
		ROM_LOAD( "rush_h05.4e",  0x04000, 0x4000, 0x9d028e8f );
		ROM_LOAD( "f04_l08.bin",  0x08000, 0x4000, 0x883933a4 );
		ROM_LOAD( "e03_l04.bin",  0x0c000, 0x4000, 0xccecda4c );
	
		ROM_REGION( 0x0220, REGION_PROMS );
		ROM_LOAD( "577h09",       0x0000, 0x0020, 0xc15e7c80 );/* palette */
		ROM_LOAD( "577h10",       0x0020, 0x0100, 0xe9de1e53 );/* sprites */
		ROM_LOAD( "577h11",       0x0120, 0x0100, 0x2a1a992b );/* characters */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_gberetb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "2-ic82.10g",   0x0000, 0x8000, 0x6d6fb494 );
		ROM_LOAD( "3-ic81.10f",   0x8000, 0x4000, 0xf1520a0a );
	
		ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "1-ic92.12c",   0x00000, 0x4000, 0xb0189c87 );
	
		ROM_REGION( 0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "7-1c8.2b",     0x00000, 0x4000, 0x86334522 );
		ROM_LOAD( "6-ic9.2c",     0x04000, 0x4000, 0xbda50d3e );
		ROM_LOAD( "5-ic10.2d",    0x08000, 0x4000, 0x6a7b3881 );
		ROM_LOAD( "4-ic11.2e",    0x0c000, 0x4000, 0x3fb186c9 );
	
		ROM_REGION( 0x0220, REGION_PROMS );
		ROM_LOAD( "577h09",       0x0000, 0x0020, 0xc15e7c80 );/* palette */
		ROM_LOAD( "577h10",       0x0020, 0x0100, 0xe9de1e53 );/* sprites */
		ROM_LOAD( "577h11",       0x0120, 0x0100, 0x2a1a992b );/* characters */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_mrgoemon = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x14000, REGION_CPU1 );/* 64k for code + banked ROM */
		ROM_LOAD( "621d01.10c",   0x00000, 0x8000, 0xb2219c56 );
		ROM_LOAD( "621d02.12c",   0x08000, 0x4000, 0xc3337a97 );
		ROM_CONTINUE(             0x10000, 0x4000 );
	
		ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "621a05.6d",   0x00000, 0x4000, 0xf0a6dfc5 );
	
		ROM_REGION( 0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "621d03.4d",   0x00000, 0x8000, 0x66f2b973 );
		ROM_LOAD( "621d04.5d",   0x08000, 0x8000, 0x47df6301 );
	
		ROM_REGION( 0x0220, REGION_PROMS );
		ROM_LOAD( "621a06.5f",    0x0000, 0x0020, 0x7c90de5f );/* palette */
		ROM_LOAD( "621a07.6f",    0x0020, 0x0100, 0x3980acdc );/* sprites */
		ROM_LOAD( "621a08.7f",    0x0120, 0x0100, 0x2fb244dd );/* characters */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_gberet	   = new GameDriver("1985"	,"gberet"	,"gberet.java"	,rom_gberet,null	,machine_driver_gberet	,input_ports_gberet	,init_gberet	,ROT0	,	"Konami", "Green Beret" );
	public static GameDriver driver_rushatck   = new GameDriver("1985"	,"rushatck"	,"gberet.java"	,rom_rushatck,driver_gberet	,machine_driver_gberet	,input_ports_gberet	,init_gberet	,ROT0	,	"Konami", "Rush'n Attack" );
	public static GameDriver driver_gberetb	   = new GameDriver("1985"	,"gberetb"	,"gberet.java"	,rom_gberetb,driver_gberet	,machine_driver_gberetb	,input_ports_gberetb	,init_gberetb	,ROT0	,	"bootleg", "Green Beret (bootleg)" );
	public static GameDriver driver_mrgoemon   = new GameDriver("1986"	,"mrgoemon"	,"gberet.java"	,rom_mrgoemon,null	,machine_driver_mrgoemon	,input_ports_mrgoemon	,init_gberet	,ROT0	,	"Konami", "Mr. Goemon (Japan)" );
	
}
