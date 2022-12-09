
/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package gr.codebb.arcadeflex.v036.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.centiped.*;
import static gr.codebb.arcadeflex.v036.machine.centiped.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static common.libc.cstdlib.rand;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.pokeyH.*;
import static gr.codebb.arcadeflex.v036.sound.pokey.*;
import static arcadeflex.v036.sound.ay8910H.*;
import static arcadeflex.v036.sound.ay8910.*;
import static gr.codebb.arcadeflex.v037b7.machine.atari_vg.*;

public class centiped
{

	public static WriteHandlerPtr centiped_led_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		//osd_led_w(offset,~data >> 7);
	} };
	
	public static ReadHandlerPtr centipdb_rand_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return rand() % 0xff;
	} };
	
	public static WriteHandlerPtr centipdb_AY8910_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		AY8910_control_port_0_w.handler(0, offset);
		AY8910_write_port_0_w.handler(0, data);
	} };
	
	public static ReadHandlerPtr centipdb_AY8910_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		AY8910_control_port_0_w.handler(0, offset);
		return AY8910_read_port_0_r.handler(0);
	} };
	
	static MemoryReadAddress centiped_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x03ff, MRA_RAM ),
		new MemoryReadAddress( 0x0400, 0x07ff, MRA_RAM ),
		new MemoryReadAddress( 0x0800, 0x0800, input_port_4_r ),	/* DSW1 */
		new MemoryReadAddress( 0x0801, 0x0801, input_port_5_r ),	/* DSW2 */
		new MemoryReadAddress( 0x0c00, 0x0c00, centiped_IN0_r ),	/* IN0 */
		new MemoryReadAddress( 0x0c01, 0x0c01, input_port_1_r ),	/* IN1 */
		new MemoryReadAddress( 0x0c02, 0x0c02, centiped_IN2_r ),	/* IN2 */	/* JB 971220 */
		new MemoryReadAddress( 0x0c03, 0x0c03, input_port_3_r ),	/* IN3 */
		new MemoryReadAddress( 0x1000, 0x100f, pokey1_r ),
		new MemoryReadAddress( 0x1700, 0x173f, atari_vg_earom_r ),
		new MemoryReadAddress( 0x2000, 0x3fff, MRA_ROM ),
		new MemoryReadAddress( 0xf800, 0xffff, MRA_ROM ),	/* for the reset / interrupt vectors */
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	/* Same as the regular one, except it uses an AY8910 and an external RNG */
	static MemoryReadAddress centipdb_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x03ff, MRA_RAM ),
		new MemoryReadAddress( 0x0400, 0x07ff, MRA_RAM ),
		new MemoryReadAddress( 0x0800, 0x0800, input_port_4_r ),	/* DSW1 */
		new MemoryReadAddress( 0x0801, 0x0801, input_port_5_r ),	/* DSW2 */
		new MemoryReadAddress( 0x0c00, 0x0c00, centiped_IN0_r ),	/* IN0 */
		new MemoryReadAddress( 0x0c01, 0x0c01, input_port_1_r ),	/* IN1 */
		new MemoryReadAddress( 0x0c02, 0x0c02, centiped_IN2_r ),	/* IN2 */	/* JB 971220 */
		new MemoryReadAddress( 0x0c03, 0x0c03, input_port_3_r ),	/* IN3 */
		new MemoryReadAddress( 0x1000, 0x100f, centipdb_AY8910_r ),
		new MemoryReadAddress( 0x1700, 0x173f, atari_vg_earom_r ),
		new MemoryReadAddress( 0x1780, 0x1780, centipdb_rand_r ),
		new MemoryReadAddress( 0x2000, 0x3fff, MRA_ROM ),
		new MemoryReadAddress( 0xf800, 0xffff, MRA_ROM ),	/* for the reset / interrupt vectors */
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress centipb2_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x03ff, MRA_RAM ),
		new MemoryReadAddress( 0x0400, 0x07ff, MRA_RAM ),
		new MemoryReadAddress( 0x0800, 0x0800, input_port_4_r ),	/* DSW1 */
		new MemoryReadAddress( 0x0801, 0x0801, input_port_5_r ),	/* DSW2 */
		new MemoryReadAddress( 0x0c00, 0x0c00, centiped_IN0_r ),	/* IN0 */
		new MemoryReadAddress( 0x0c01, 0x0c01, input_port_1_r ),	/* IN1 */
		new MemoryReadAddress( 0x0c02, 0x0c02, centiped_IN2_r ),	/* IN2 */	/* JB 971220 */
		new MemoryReadAddress( 0x0c03, 0x0c03, input_port_3_r ),	/* IN3 */
		new MemoryReadAddress( 0x1001, 0x1001, AY8910_read_port_0_r ),
		new MemoryReadAddress( 0x1700, 0x173f, atari_vg_earom_r ),
		new MemoryReadAddress( 0x2000, 0x3fff, MRA_ROM ),
		new MemoryReadAddress( 0x6000, 0x67ff, MRA_ROM ),
		new MemoryReadAddress( 0xf800, 0xffff, MRA_ROM ),	/* for the reset / interrupt vectors */
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress centiped_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x03ff, MWA_RAM ),
		new MemoryWriteAddress( 0x0400, 0x07bf, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x07c0, 0x07ff, MWA_RAM, spriteram ),
		new MemoryWriteAddress( 0x1000, 0x100f, pokey1_w ),
		new MemoryWriteAddress( 0x1400, 0x140f, centiped_paletteram_w, paletteram ),
		new MemoryWriteAddress( 0x1600, 0x163f, atari_vg_earom_w ),
		new MemoryWriteAddress( 0x1680, 0x1680, atari_vg_earom_ctrl_w ),
		new MemoryWriteAddress( 0x1800, 0x1800, MWA_NOP ),	/* IRQ acknowldege */
		new MemoryWriteAddress( 0x1c00, 0x1c02, coin_counter_w ),
		new MemoryWriteAddress( 0x1c03, 0x1c04, centiped_led_w ),
		new MemoryWriteAddress( 0x1c07, 0x1c07, centiped_vh_flipscreen_w ),
		new MemoryWriteAddress( 0x2000, 0x2000, watchdog_reset_w ),
		new MemoryWriteAddress( 0x2000, 0x3fff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	/* Same as the regular one, except it uses an AY8910 */
	static MemoryWriteAddress centipdb_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x03ff, MWA_RAM ),
		new MemoryWriteAddress( 0x0400, 0x07bf, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x07c0, 0x07ff, MWA_RAM, spriteram ),
		new MemoryWriteAddress( 0x1000, 0x100f, centipdb_AY8910_w ),
		new MemoryWriteAddress( 0x1400, 0x140f, centiped_paletteram_w, paletteram ),
		new MemoryWriteAddress( 0x1600, 0x163f, atari_vg_earom_w ),
		new MemoryWriteAddress( 0x1680, 0x1680, atari_vg_earom_ctrl_w ),
		new MemoryWriteAddress( 0x1800, 0x1800, MWA_NOP ),	/* IRQ acknowldege */
		new MemoryWriteAddress( 0x1c00, 0x1c02, coin_counter_w ),
		new MemoryWriteAddress( 0x1c03, 0x1c04, centiped_led_w ),
		new MemoryWriteAddress( 0x1c07, 0x1c07, centiped_vh_flipscreen_w ),
		new MemoryWriteAddress( 0x2000, 0x2000, watchdog_reset_w ),
		new MemoryWriteAddress( 0x2000, 0x3fff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress centipb2_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x03ff, MWA_RAM ),
		new MemoryWriteAddress( 0x0400, 0x07bf, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x07c0, 0x07ff, MWA_RAM, spriteram ),
		new MemoryWriteAddress( 0x1000, 0x1000, AY8910_write_port_0_w ),
		new MemoryWriteAddress( 0x1001, 0x1001, AY8910_control_port_0_w ),
		new MemoryWriteAddress( 0x1400, 0x140f, centiped_paletteram_w, paletteram ),
		new MemoryWriteAddress( 0x1600, 0x163f, atari_vg_earom_w ),
		new MemoryWriteAddress( 0x1680, 0x1680, atari_vg_earom_ctrl_w ),
		new MemoryWriteAddress( 0x1800, 0x1800, MWA_NOP ),	/* IRQ acknowldege */
		new MemoryWriteAddress( 0x1c00, 0x1c02, coin_counter_w ),
		new MemoryWriteAddress( 0x1c03, 0x1c04, centiped_led_w ),
		new MemoryWriteAddress( 0x1c07, 0x1c07, centiped_vh_flipscreen_w ),
		new MemoryWriteAddress( 0x2000, 0x2000, watchdog_reset_w ),
		new MemoryWriteAddress( 0x2000, 0x3fff, MWA_ROM ),
		new MemoryWriteAddress( 0x6000, 0x67ff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	/* The input ports are identical for the real one and the bootleg one, except
	   that one of the languages is Italian in the bootleg one instead of Spanish */										
																					
	static InputPortPtr input_ports_centiped = new InputPortPtr(){ public void handler() { 										
		PORT_START(); 	/* IN0 */														
		/* The lower 4 bits and bit 7 are for trackball x input. */					
		/* They are handled by fake input port 6 and a custom routine. */			
		PORT_BIT ( 0x0f, IP_ACTIVE_HIGH, IPT_UNKNOWN );							
		PORT_DIPNAME(0x10, 0x00, DEF_STR( "Cabinet") );								
		PORT_DIPSETTING (   0x00, DEF_STR( "Upright") );								
		PORT_DIPSETTING (   0x10, DEF_STR( "Cocktail") );								
		PORT_SERVICE( 0x20, IP_ACTIVE_LOW );										
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_VBLANK );							
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );							
																					
		PORT_START(); 	/* IN1 */														
		PORT_BIT ( 0x01, IP_ACTIVE_LOW, IPT_START1 );							
		PORT_BIT ( 0x02, IP_ACTIVE_LOW, IPT_START2 );							
		PORT_BIT ( 0x04, IP_ACTIVE_LOW, IPT_BUTTON1 );							
		PORT_BIT ( 0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );				
		PORT_BIT ( 0x10, IP_ACTIVE_LOW, IPT_TILT );								
		PORT_BIT ( 0x20, IP_ACTIVE_LOW, IPT_COIN1 );								
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_COIN2 );								
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_COIN3 );								
																					
		PORT_START(); 	/* IN2 */														
		PORT_ANALOGX( 0xff, 0x00, IPT_TRACKBALL_Y, 50, 10, 0, 0, IP_KEY_NONE, IP_KEY_NONE, IP_JOY_NONE, IP_JOY_NONE );
		/* The lower 4 bits are the input, and bit 7 is the direction. */			
		/* The state of bit 7 does not change if the trackball is not moved.*/		
																					
		PORT_START(); 	/* IN3 */														
		PORT_BIT ( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT ( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL );	
		PORT_BIT ( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT ( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );			
		PORT_BIT ( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );			
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );			
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );			
																					
		PORT_START(); 	/* IN4 */														
		PORT_DIPNAME(0x03, 0x00, "Language" );									
		PORT_DIPSETTING (   0x00, "English" );									
		PORT_DIPSETTING (   0x01, "German" );									
		PORT_DIPSETTING (   0x02, "French" );									
		PORT_DIPSETTING (   0x03, "Spanish" );								
		PORT_DIPNAME(0x0c, 0x04, DEF_STR( "Lives") );									
		PORT_DIPSETTING (   0x00, "2" );											
		PORT_DIPSETTING (   0x04, "3" );											
		PORT_DIPSETTING (   0x08, "4" );											
		PORT_DIPSETTING (   0x0c, "5" );											
		PORT_DIPNAME(0x30, 0x10, DEF_STR( "Bonus_Life") );							
		PORT_DIPSETTING (   0x00, "10000" );										
		PORT_DIPSETTING (   0x10, "12000" );										
		PORT_DIPSETTING (   0x20, "15000" );										
		PORT_DIPSETTING (   0x30, "20000" );										
		PORT_DIPNAME(0x40, 0x40, DEF_STR( "Difficulty") );							
		PORT_DIPSETTING (   0x40, "Easy" );										
		PORT_DIPSETTING (   0x00, "Hard" );										
		PORT_DIPNAME(0x80, 0x00, "Credit Minimum" );								
		PORT_DIPSETTING (   0x00, "1" );											
		PORT_DIPSETTING (   0x80, "2" );											
																					
		PORT_START(); 	/* IN5 */														
		PORT_DIPNAME(0x03, 0x02, DEF_STR( "Coinage") );								
		PORT_DIPSETTING (   0x03, DEF_STR( "2C_1C") );								
		PORT_DIPSETTING (   0x02, DEF_STR( "1C_1C") );								
		PORT_DIPSETTING (   0x01, DEF_STR( "1C_2C") );								
		PORT_DIPSETTING (   0x00, DEF_STR( "Free_Play") );							
		PORT_DIPNAME(0x0c, 0x00, "Right Coin" );									
		PORT_DIPSETTING (   0x00, "*1" );										
		PORT_DIPSETTING (   0x04, "*4" );										
		PORT_DIPSETTING (   0x08, "*5" );										
		PORT_DIPSETTING (   0x0c, "*6" );										
		PORT_DIPNAME(0x10, 0x00, "Left Coin" );									
		PORT_DIPSETTING (   0x00, "*1" );										
		PORT_DIPSETTING (   0x10, "*2" );										
		PORT_DIPNAME(0xe0, 0x00, "Bonus Coins" );								
		PORT_DIPSETTING (   0x00, "None" );										
		PORT_DIPSETTING (   0x20, "3 credits/2 coins" );							
		PORT_DIPSETTING (   0x40, "5 credits/4 coins" );							
		PORT_DIPSETTING (   0x60, "6 credits/4 coins" );							
		PORT_DIPSETTING (   0x80, "6 credits/5 coins" );							
		PORT_DIPSETTING (   0xa0, "4 credits/3 coins" );							
																					
		PORT_START(); 	/* IN6, fake trackball input port. */							
		PORT_ANALOGX( 0xff, 0x00, IPT_TRACKBALL_X | IPF_REVERSE, 50, 10, 0, 0, IP_KEY_NONE, IP_KEY_NONE, IP_JOY_NONE, IP_JOY_NONE );	
	INPUT_PORTS_END(); }}; 

	static InputPortPtr input_ports_centipdb = new InputPortPtr(){ public void handler() { 										
		PORT_START(); 	/* IN0 */														
		/* The lower 4 bits and bit 7 are for trackball x input. */					
		/* They are handled by fake input port 6 and a custom routine. */			
		PORT_BIT ( 0x0f, IP_ACTIVE_HIGH, IPT_UNKNOWN );							
		PORT_DIPNAME(0x10, 0x00, DEF_STR( "Cabinet") );								
		PORT_DIPSETTING (   0x00, DEF_STR( "Upright") );								
		PORT_DIPSETTING (   0x10, DEF_STR( "Cocktail") );								
		PORT_SERVICE( 0x20, IP_ACTIVE_LOW );										
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_VBLANK );							
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );							
																					
		PORT_START(); 	/* IN1 */														
		PORT_BIT ( 0x01, IP_ACTIVE_LOW, IPT_START1 );							
		PORT_BIT ( 0x02, IP_ACTIVE_LOW, IPT_START2 );							
		PORT_BIT ( 0x04, IP_ACTIVE_LOW, IPT_BUTTON1 );							
		PORT_BIT ( 0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );				
		PORT_BIT ( 0x10, IP_ACTIVE_LOW, IPT_TILT );								
		PORT_BIT ( 0x20, IP_ACTIVE_LOW, IPT_COIN1 );								
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_COIN2 );								
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_COIN3 );								
																					
		PORT_START(); 	/* IN2 */														
		PORT_ANALOGX( 0xff, 0x00, IPT_TRACKBALL_Y, 50, 10, 0, 0, IP_KEY_NONE, IP_KEY_NONE, IP_JOY_NONE, IP_JOY_NONE );
		/* The lower 4 bits are the input, and bit 7 is the direction. */			
		/* The state of bit 7 does not change if the trackball is not moved.*/		
																					
		PORT_START(); 	/* IN3 */														
		PORT_BIT ( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT ( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL );	
		PORT_BIT ( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT ( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );			
		PORT_BIT ( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );			
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );			
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );			
																					
		PORT_START(); 	/* IN4 */														
		PORT_DIPNAME(0x03, 0x00, "Language" );									
		PORT_DIPSETTING (   0x00, "English" );									
		PORT_DIPSETTING (   0x01, "German" );									
		PORT_DIPSETTING (   0x02, "French" );									
		PORT_DIPSETTING (   0x03, "Italian");								
		PORT_DIPNAME(0x0c, 0x04, DEF_STR( "Lives") );									
		PORT_DIPSETTING (   0x00, "2" );											
		PORT_DIPSETTING (   0x04, "3" );											
		PORT_DIPSETTING (   0x08, "4" );											
		PORT_DIPSETTING (   0x0c, "5" );											
		PORT_DIPNAME(0x30, 0x10, DEF_STR( "Bonus_Life") );							
		PORT_DIPSETTING (   0x00, "10000" );										
		PORT_DIPSETTING (   0x10, "12000" );										
		PORT_DIPSETTING (   0x20, "15000" );										
		PORT_DIPSETTING (   0x30, "20000" );										
		PORT_DIPNAME(0x40, 0x40, DEF_STR( "Difficulty") );							
		PORT_DIPSETTING (   0x40, "Easy" );										
		PORT_DIPSETTING (   0x00, "Hard" );										
		PORT_DIPNAME(0x80, 0x00, "Credit Minimum" );								
		PORT_DIPSETTING (   0x00, "1" );											
		PORT_DIPSETTING (   0x80, "2" );											
																					
		PORT_START(); 	/* IN5 */														
		PORT_DIPNAME(0x03, 0x02, DEF_STR( "Coinage") );								
		PORT_DIPSETTING (   0x03, DEF_STR( "2C_1C") );								
		PORT_DIPSETTING (   0x02, DEF_STR( "1C_1C") );								
		PORT_DIPSETTING (   0x01, DEF_STR( "1C_2C") );								
		PORT_DIPSETTING (   0x00, DEF_STR( "Free_Play") );							
		PORT_DIPNAME(0x0c, 0x00, "Right Coin" );									
		PORT_DIPSETTING (   0x00, "*1" );										
		PORT_DIPSETTING (   0x04, "*4" );										
		PORT_DIPSETTING (   0x08, "*5" );										
		PORT_DIPSETTING (   0x0c, "*6" );										
		PORT_DIPNAME(0x10, 0x00, "Left Coin" );									
		PORT_DIPSETTING (   0x00, "*1" );										
		PORT_DIPSETTING (   0x10, "*2" );										
		PORT_DIPNAME(0xe0, 0x00, "Bonus Coins" );								
		PORT_DIPSETTING (   0x00, "None" );										
		PORT_DIPSETTING (   0x20, "3 credits/2 coins" );							
		PORT_DIPSETTING (   0x40, "5 credits/4 coins" );							
		PORT_DIPSETTING (   0x60, "6 credits/4 coins" );							
		PORT_DIPSETTING (   0x80, "6 credits/5 coins" );							
		PORT_DIPSETTING (   0xa0, "4 credits/3 coins" );							
																					
		PORT_START(); 	/* IN6, fake trackball input port. */							
		PORT_ANALOGX( 0xff, 0x00, IPT_TRACKBALL_X | IPF_REVERSE, 50, 10, 0, 0, IP_KEY_NONE, IP_KEY_NONE, IP_JOY_NONE, IP_JOY_NONE );	
	INPUT_PORTS_END(); }}; 
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		256,	/* 256 characters */
		2,	/* 2 bits per pixel */
		new int[] { 256*8*8, 0 },	/* the two bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every char takes 8 consecutive bytes */
	);
	static GfxLayout spritelayout = new GfxLayout
	(
		8,16,	/* 16*8 sprites */
		128,	/* 64 sprites */
		2,	/* 2 bits per pixel */
		new int[] { 128*16*8, 0 },	/* the two bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		16*8	/* every sprite takes 16 consecutive bytes */
	);
	
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,   4, 4 ),	/* 4 color codes to support midframe */
													/* palette changes in test mode */
		new GfxDecodeInfo( REGION_GFX1, 0, spritelayout, 0, 1 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static POKEYinterface pokey_interface = new POKEYinterface
	(
		1,	/* 1 chip */
		12096000/8,	/* 1.512 MHz */
		new int[]{ 100 },
		/* The 8 pot handlers */
		new ReadHandlerPtr[]{ null },
		new ReadHandlerPtr[]{ null },
		new ReadHandlerPtr[]{ null },
		new ReadHandlerPtr[]{ null },
		new ReadHandlerPtr[]{ null },
		new ReadHandlerPtr[]{ null },
		new ReadHandlerPtr[]{ null },
		new ReadHandlerPtr[]{ null },
		/* The allpot handler */
		new ReadHandlerPtr[]{ null }
        );
	
	static AY8910interface centipdb_ay8910_interface = new AY8910interface
	(
		1,	/* 1 chips */
		12096000/8,	/* 1.512 MHz */
		new int[] { 50 },
		new ReadHandlerPtr[] { null },
		new ReadHandlerPtr[] { null },
		new WriteHandlerPtr[] { null },
		new WriteHandlerPtr[] { null }
	);
	
	static AY8910interface centipb2_ay8910_interface = new AY8910interface
	(
		1,	/* 1 chips */
		12096000/8,	/* 1.512 MHz */
		new int[] { 50 },
		new ReadHandlerPtr[] { centipdb_rand_r },
		new ReadHandlerPtr[] { null },
		new WriteHandlerPtr[] { null },
		new WriteHandlerPtr[] { null }
	);
	
													
	static MachineDriver machine_driver_centiped = new MachineDriver
	(																				
		/* basic machine hardware */												
		new MachineCPU[] {																			
			new MachineCPU(																		
				CPU_M6502,															
				12096000/8,	/* 1.512 Mhz (slows down to 0.75MHz while accessing playfield RAM) */	
				centiped_readmem,centiped_writemem,null,null,							
				centiped_interrupt,4												
			)																		
		},																			
		60, 1460,	/* frames per second, vblank duration */						
		1,	/* single CPU, no need for interleaving */								
		centiped_init_machine,														
																					
		/* video hardware */														
		32*8, 32*8, new rectangle(0*8, 32*8-1, 0*8, 30*8-1 ),									
		gfxdecodeinfo,																
		4+4*4, 4+4*4,																
		null,																			
																					
		VIDEO_TYPE_RASTER|VIDEO_SUPPORTS_DIRTY|VIDEO_MODIFIES_PALETTE,				
		null,																			
		generic_vh_start,															
		generic_vh_stop,															
		centiped_vh_screenrefresh,													
																					
		/* sound hardware */														
		0,0,0,0,																	
		new MachineSound[] {																			
			new MachineSound(																		
				SOUND_POKEY, 
                                pokey_interface													
			)																		
		},																			
																					
		atari_vg_earom_handler														
	);
	static MachineDriver machine_driver_centipdb = new MachineDriver
	(																				
		/* basic machine hardware */												
		new MachineCPU[] {																			
			new MachineCPU(																		
				CPU_M6502,															
				12096000/8,	/* 1.512 Mhz (slows down to 0.75MHz while accessing playfield RAM) */	
				centipdb_readmem,centipdb_writemem,null,null,							
				centiped_interrupt,4												
			)																		
		},																			
		60, 1460,	/* frames per second, vblank duration */						
		1,	/* single CPU, no need for interleaving */								
		centiped_init_machine,														
																					
		/* video hardware */														
		32*8, 32*8, new rectangle(0*8, 32*8-1, 0*8, 30*8-1 ),									
		gfxdecodeinfo,																
		4+4*4, 4+4*4,																
		null,																			
																					
		VIDEO_TYPE_RASTER|VIDEO_SUPPORTS_DIRTY|VIDEO_MODIFIES_PALETTE,				
		null,																			
		generic_vh_start,															
		generic_vh_stop,															
		centiped_vh_screenrefresh,													
																					
		/* sound hardware */														
		0,0,0,0,																	
		new MachineSound[] {																			
			new MachineSound(																		
				SOUND_AY8910, 
                                centipdb_ay8910_interface													
			)																		
		},																			
																					
		atari_vg_earom_handler														
	);
	static MachineDriver machine_driver_centipb2 = new MachineDriver
	(																				
		/* basic machine hardware */												
		new MachineCPU[] {																			
			new MachineCPU(																		
				CPU_M6502,															
				12096000/8,	/* 1.512 Mhz (slows down to 0.75MHz while accessing playfield RAM) */	
				centipb2_readmem,centipb2_writemem,null,null,							
				centiped_interrupt,4												
			)																		
		},																			
		60, 1460,	/* frames per second, vblank duration */						
		1,	/* single CPU, no need for interleaving */								
		centiped_init_machine,														
																					
		/* video hardware */														
		32*8, 32*8, new rectangle(0*8, 32*8-1, 0*8, 30*8-1 ),									
		gfxdecodeinfo,																
		4+4*4, 4+4*4,																
		null,																			
																					
		VIDEO_TYPE_RASTER|VIDEO_SUPPORTS_DIRTY|VIDEO_MODIFIES_PALETTE,				
		null,																			
		generic_vh_start,															
		generic_vh_stop,															
		centiped_vh_screenrefresh,													
																					
		/* sound hardware */														
		0,0,0,0,																	
		new MachineSound[] {																			
			new MachineSound(																		
				SOUND_AY8910, 
                                centipb2_ay8910_interface													
			)																		
		},																			
																					
		atari_vg_earom_handler														
	);
		
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadHandlerPtr rom_centiped = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "centiped.307", 0x2000, 0x0800, 0x5ab0d9de );
		ROM_LOAD( "centiped.308", 0x2800, 0x0800, 0x4c07fd3e );
		ROM_LOAD( "centiped.309", 0x3000, 0x0800, 0xff69b424 );
		ROM_LOAD( "centiped.310", 0x3800, 0x0800, 0x44e40fa4 );
		ROM_RELOAD(               0xf800, 0x0800 );/* for the reset and interrupt vectors */
	
		ROM_REGION( 0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "centiped.211", 0x0000, 0x0800, 0x880acfb9 );
		ROM_LOAD( "centiped.212", 0x0800, 0x0800, 0xb1397029 );
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_centipd2 = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "centiped.207", 0x2000, 0x0800, 0xb2909e2f );
		ROM_LOAD( "centiped.208", 0x2800, 0x0800, 0x110e04ff );
		ROM_LOAD( "centiped.209", 0x3000, 0x0800, 0xcc2edb26 );
		ROM_LOAD( "centiped.210", 0x3800, 0x0800, 0x93999153 );
		ROM_RELOAD(               0xf800, 0x0800 );/* for the reset and interrupt vectors */
	
		ROM_REGION( 0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "centiped.211", 0x0000, 0x0800, 0x880acfb9 );
		ROM_LOAD( "centiped.212", 0x0800, 0x0800, 0xb1397029 );
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_centipdb = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "olympia.c28",  0x2000, 0x0800, 0x8a744e57 );
		ROM_LOAD( "olympia.c29",  0x2800, 0x0800, 0xbb897b10 );
		ROM_LOAD( "olympia.c30",  0x3000, 0x0800, 0x2297c2ac );
		ROM_LOAD( "olympia.c31",  0x3800, 0x0800, 0xcc529d6b );
		ROM_RELOAD(               0xf800, 0x0800 );/* for the reset and interrupt vectors */
	
		ROM_REGION( 0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "olympia.c32",  0x0000, 0x0800, 0xd91b9724 );
		ROM_LOAD( "olympia.c33",  0x0800, 0x0800, 0x1a6acd02 );
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_centipb2 = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "d1",  		  0x2000, 0x0800, 0xb17b8e0b );
		ROM_LOAD( "e1",  		  0x2800, 0x0800, 0x7684398e );
		ROM_LOAD( "h1",  		  0x3000, 0x0800, 0x74580fe4 );
		ROM_LOAD( "j1",  		  0x3800, 0x0800, 0x84600161 );
		ROM_RELOAD(               0xf800, 0x0800 );/* for the reset and interrupt vectors */
		ROM_LOAD( "k1",  		  0x6000, 0x0800, 0xf1aa329b );
	
		ROM_REGION( 0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "centiped.211", 0x0000, 0x0800, 0x880acfb9 );
		ROM_LOAD( "centiped.212", 0x0800, 0x0800, 0xb1397029 );
	ROM_END(); }}; 
	
	
	public static GameDriver driver_centiped	   = new GameDriver("1980"	,"centiped"	,"centiped.java"	,rom_centiped,null	,machine_driver_centiped	,input_ports_centiped	,null	,ROT270	,	"Atari", "Centipede (revision 3)" );
	public static GameDriver driver_centipd2	   = new GameDriver("1980"	,"centipd2"	,"centiped.java"	,rom_centipd2,driver_centiped	,machine_driver_centiped	,input_ports_centiped	,null	,ROT270	,	"Atari", "Centipede (revision 2)" );
	public static GameDriver driver_centipdb	   = new GameDriver("1980"	,"centipdb"	,"centiped.java"	,rom_centipdb,driver_centiped	,machine_driver_centipdb	,input_ports_centipdb	,null	,ROT270	,	"bootleg", "Centipede (bootleg set 1)" );
	public static GameDriver driver_centipb2	   = new GameDriver("1980"	,"centipb2"	,"centiped.java"	,rom_centipb2,driver_centiped	,machine_driver_centipb2	,input_ports_centiped	,null	,ROT270	,	"bootleg", "Centipede (bootleg set 2)" );
}
