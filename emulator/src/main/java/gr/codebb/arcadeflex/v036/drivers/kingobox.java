/***************************************************************************

King of Boxer - (c) 1985 Woodplace Inc.
Ring King - (c) 1985 Data East USA Inc.

Preliminary driver by:
Ernesto Corvi
ernesto@imagina.com

Notes:
-----
Main CPU:
- Theres a memory area from 0xf000 to 0xf7ff, which is clearly
  initialized at startup and never used anymore.

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
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.kingobox.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.sound.dacH.*;


public class kingobox
{
	
	public static UBytePtr video_shared=new UBytePtr();
	public static UBytePtr sprite_shared=new UBytePtr();
	public static int kingofb_nmi_enable =0;
	
        public static ReadHandlerPtr video_shared_r = new ReadHandlerPtr() { public int handler(int offset)
        {
		return video_shared.read(offset);
        }};
	public static WriteHandlerPtr video_shared_w = new WriteHandlerPtr() { public void handler(int offset, int data)
        {
		video_shared.write(offset,data);
	}};
	
        public static ReadHandlerPtr sprite_shared_r = new ReadHandlerPtr() { public int handler(int offset)
        {
		return sprite_shared.read(offset);
	}};
	public static WriteHandlerPtr sprite_shared_w = new WriteHandlerPtr() { public void handler(int offset, int data)
        {
		sprite_shared.write(offset,data);
	}};
	
	public static WriteHandlerPtr video_interrupt_w = new WriteHandlerPtr() { public void handler(int offs, int data){
		cpu_cause_interrupt( 1, 0xff );
	} };
	
	public static WriteHandlerPtr sprite_interrupt_w = new WriteHandlerPtr() { public void handler(int offs, int data){
		cpu_cause_interrupt( 2, 0xff );
	} };
	
	public static WriteHandlerPtr scroll_interrupt_w = new WriteHandlerPtr() { public void handler(int offset, int data)
        {
		sprite_interrupt_w.handler(offset, data );
		kingobox_scroll_y.write(data);
	}};
	public static WriteHandlerPtr sound_command_w = new WriteHandlerPtr() { public void handler(int offset, int data)
        {
		soundlatch_w.handler(0, data );
		cpu_cause_interrupt( 3, 0xff );
	}};
	
	static MemoryReadAddress main_readmem[] =
	{
	    new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
	    new MemoryReadAddress( 0xc000, 0xc3ff, MRA_RAM ), /* work ram */
	    new MemoryReadAddress( 0xe000, 0xe7ff, sprite_shared_r ),
	    new MemoryReadAddress( 0xe800, 0xefff, video_shared_r ),
	    new MemoryReadAddress( 0xf000, 0xf7ff, MRA_RAM ), /* ???? */
	    new MemoryReadAddress( 0xfc00, 0xfc00, input_port_0_r ), /* DSW 0 */
	    new MemoryReadAddress( 0xfc01, 0xfc01, input_port_1_r ), /* DSW 1 */
	    new MemoryReadAddress( 0xfc02, 0xfc02, input_port_2_r ), /* Player 1 controls */
	    new MemoryReadAddress( 0xfc03, 0xfc03, input_port_3_r ), /* Player 2 controls */
	    new MemoryReadAddress( 0xfc04, 0xfc04, input_port_4_r ), /* Coin  Start */
	    new MemoryReadAddress( 0xfc05, 0xfc05, input_port_5_r ), /* Player 1  2 button 3 */
	    new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress main_writemem[] =
	{
	    new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
	    new MemoryWriteAddress( 0xc000, 0xc3ff, MWA_RAM ), /* work ram */
	    new MemoryWriteAddress( 0xe000, 0xe7ff, sprite_shared_w ), /* shared with sprite cpu */
	    new MemoryWriteAddress( 0xe800, 0xefff, video_shared_w ), /* shared with video cpu */
	    new MemoryWriteAddress( 0xf000, 0xf7ff, MWA_RAM ), /* ???? */
	    new MemoryWriteAddress( 0xf800, 0xf800, kingofb_f800_w ),	/* NMI enable, palette bank */
	    new MemoryWriteAddress( 0xf801, 0xf801, MWA_NOP ), /* ???? */
	    new MemoryWriteAddress( 0xf802, 0xf802, MWA_RAM, kingobox_scroll_y ),
	    new MemoryWriteAddress( 0xf803, 0xf803, scroll_interrupt_w  ),
	    new MemoryWriteAddress( 0xf804, 0xf804, video_interrupt_w ),
	    new MemoryWriteAddress( 0xf807, 0xf807, sound_command_w ), /* sound latch */
	    new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress video_readmem[] =
	{
	    new MemoryReadAddress( 0x0000, 0x3fff, MRA_ROM ),
	    new MemoryReadAddress( 0x8000, 0x87ff, MRA_RAM ), /* work ram */
	    new MemoryReadAddress( 0xa000, 0xa7ff, video_shared_r ), /* shared with main */
	    new MemoryReadAddress( 0xc000, 0xc0ff, videoram_r ), /* background vram */
	    new MemoryReadAddress( 0xc400, 0xc4ff, colorram_r ), /* background colorram */
	    new MemoryReadAddress( 0xc800, 0xcbff, MRA_RAM ), /* foreground vram */
	    new MemoryReadAddress( 0xcc00, 0xcfff, MRA_RAM ), /* foreground colorram */
	    new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress video_writemem[] =
	{
	    new MemoryWriteAddress( 0x0000, 0x3fff, MWA_ROM ),
	    new MemoryWriteAddress( 0x8000, 0x87ff, MWA_RAM ), /* work ram */
	    new MemoryWriteAddress( 0xa000, 0xa7ff, video_shared_w, video_shared ), /* shared with main */
	    new MemoryWriteAddress( 0xc000, 0xc0ff, videoram_w, videoram, videoram_size ), /* background vram */
	    new MemoryWriteAddress( 0xc400, 0xc4ff, colorram_w, colorram ), /* background colorram */
	    new MemoryWriteAddress( 0xc800, 0xcbff, MWA_RAM, kingobox_videoram1, kingobox_videoram1_size ), /* foreground vram */
	    new MemoryWriteAddress( 0xcc00, 0xcfff, MWA_RAM, kingobox_colorram1 ), /* foreground colorram */
	    new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress sprite_readmem[] =
	{
	    new MemoryReadAddress( 0x0000, 0x1fff, MRA_ROM ),
	    new MemoryReadAddress( 0x8000, 0x87ff, MRA_RAM ), /* work ram */
	    new MemoryReadAddress( 0xa000, 0xa7ff, sprite_shared_r ), /* shared with main */
	    new MemoryReadAddress( 0xc000, 0xc3ff, spriteram_r ), /* sprite ram */
	    new MemoryReadAddress( 0xc400, 0xc43f, MRA_RAM ), /* something related to scroll? */
	    new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sprite_writemem[] =
	{
	    new MemoryWriteAddress( 0x0000, 0x1fff, MWA_ROM ),
	    new MemoryWriteAddress( 0x8000, 0x87ff, MWA_RAM ), /* work ram */
	    new MemoryWriteAddress( 0xa000, 0xa7ff, sprite_shared_w, sprite_shared ), /* shared with main */
	    new MemoryWriteAddress( 0xc000, 0xc3ff, spriteram_w, spriteram, spriteram_size ), /* sprite ram */
	    new MemoryWriteAddress( 0xc400, 0xc43f, MWA_RAM ),  /* something related to scroll? */
	    new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress sound_readmem[] =
	{
	    new MemoryReadAddress( 0x0000, 0xbfff, MRA_ROM ),
	    new MemoryReadAddress( 0xc000, 0xc3ff, MRA_RAM ), /* work ram */
	    new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
	    new MemoryWriteAddress( 0x8000, 0x8000, MWA_NOP ), /* ??? */
	    new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),
	    new MemoryWriteAddress( 0xc000, 0xc3ff, MWA_RAM ), /* work ram */
	    new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static IOReadPort sound_readport[] =
	{
		new IOReadPort( 0x08, 0x08, AY8910_read_port_0_r ),
		new IOReadPort( -1 )  /* end of table */
	};
	
	static IOWritePort sound_writeport[] =
	{
/*TODO*///		new IOWritePort( 0x00, 0x00, DAC_data_w ),
		new IOWritePort( 0x08, 0x08, AY8910_write_port_0_w ),
		new IOWritePort( 0x0c, 0x0c, AY8910_control_port_0_w ),
		new IOWritePort( -1 )  /* end of table */
	};
	
	/* Ring King */
	static MemoryReadAddress rk_main_readmem[] =
	{
	    new MemoryReadAddress( 0x0000, 0xbfff, MRA_ROM ),
	    new MemoryReadAddress( 0xc000, 0xc3ff, MRA_RAM ), /* work ram */
	    new MemoryReadAddress( 0xc800, 0xcfff, sprite_shared_r ),
	    new MemoryReadAddress( 0xd000, 0xd7ff, video_shared_r ),
	    new MemoryReadAddress( 0xe000, 0xe000, input_port_0_r ), /* DSW 0 */
	    new MemoryReadAddress( 0xe001, 0xe001, input_port_1_r ), /* DSW 1 */
	    new MemoryReadAddress( 0xe002, 0xe002, input_port_2_r ), /* Player 1 controls */
	    new MemoryReadAddress( 0xe003, 0xe003, input_port_3_r ), /* Player 2 controls */
	    new MemoryReadAddress( 0xe004, 0xe004, input_port_4_r ), /* Coin  Start */
	    new MemoryReadAddress( 0xe005, 0xe005, input_port_5_r ), /* Player 1  2 button 3 */
	    new MemoryReadAddress( 0xf000, 0xf7ff, MRA_RAM ), /* ???? */
	    new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress rk_main_writemem[] =
	{
	    new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),
	    new MemoryWriteAddress( 0xc000, 0xc3ff, MWA_RAM ), /* work ram */
	    new MemoryWriteAddress( 0xc800, 0xcfff, sprite_shared_w ),
	    new MemoryWriteAddress( 0xd000, 0xd7ff, video_shared_w ),
	    new MemoryWriteAddress( 0xd800, 0xd800, kingofb_f800_w ),
	    new MemoryWriteAddress( 0xd801, 0xd801, sprite_interrupt_w ),
	    new MemoryWriteAddress( 0xd802, 0xd802, video_interrupt_w ),
	    new MemoryWriteAddress( 0xd803, 0xd803, sound_command_w ),
	    new MemoryWriteAddress( 0xe800, 0xe800, MWA_RAM, kingobox_scroll_y ),
	    new MemoryWriteAddress( 0xf000, 0xf7ff, MWA_RAM ), /* ???? */
	    new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress rk_video_readmem[] =
	{
	    new MemoryReadAddress( 0x0000, 0x3fff, MRA_ROM ),
	    new MemoryReadAddress( 0x8000, 0x87ff, MRA_RAM ), /* work ram */
	    new MemoryReadAddress( 0xc000, 0xc7ff, video_shared_r ), /* shared with main */
	    new MemoryReadAddress( 0xa800, 0xa8ff, videoram_r ), /* background vram */
	    new MemoryReadAddress( 0xac00, 0xacff, colorram_r ), /* background colorram */
	    new MemoryReadAddress( 0xa000, 0xa3ff, MRA_RAM ), /* foreground vram */
	    new MemoryReadAddress( 0xa400, 0xa7ff, MRA_RAM ), /* foreground colorram */
	    new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress rk_video_writemem[] =
	{
	    new MemoryWriteAddress( 0x0000, 0x3fff, MWA_ROM ),
	    new MemoryWriteAddress( 0x8000, 0x87ff, MWA_RAM ), /* work ram */
	    new MemoryWriteAddress( 0xc000, 0xc7ff, video_shared_w, video_shared ), /* shared with main */
	    new MemoryWriteAddress( 0xa800, 0xa8ff, videoram_w, videoram, videoram_size ), /* background vram */
	    new MemoryWriteAddress( 0xac00, 0xacff, colorram_w, colorram ), /* background colorram */
	    new MemoryWriteAddress( 0xa000, 0xa3ff, MWA_RAM, kingobox_videoram1, kingobox_videoram1_size ), /* foreground vram */
	    new MemoryWriteAddress( 0xa400, 0xa7ff, MWA_RAM, kingobox_colorram1 ), /* foreground colorram */
	    new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress rk_sprite_readmem[] =
	{
	    new MemoryReadAddress( 0x0000, 0x1fff, MRA_ROM ),
	    new MemoryReadAddress( 0x8000, 0x87ff, MRA_RAM ), /* work ram */
	    new MemoryReadAddress( 0xc800, 0xcfff, sprite_shared_r ), /* shared with main */
	    new MemoryReadAddress( 0xa000, 0xa3ff, spriteram_r ), /* sprite ram */
	    new MemoryReadAddress( 0xa400, 0xa43f, MRA_RAM ), /* something related to scroll? */
	    new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress rk_sprite_writemem[] =
	{
	    new MemoryWriteAddress( 0x0000, 0x1fff, MWA_ROM ),
	    new MemoryWriteAddress( 0x8000, 0x87ff, MWA_RAM ), /* work ram */
	    new MemoryWriteAddress( 0xc800, 0xcfff, sprite_shared_w, sprite_shared ), /* shared with main */
	    new MemoryWriteAddress( 0xa000, 0xa3ff, spriteram_w, spriteram, spriteram_size ), /* sprite ram */
	    new MemoryWriteAddress( 0xa400, 0xa43f, MWA_RAM ),  /* something related to scroll? */
	    new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static IOReadPort rk_sound_readport[] =
	{
		new IOReadPort( 0x02, 0x02, AY8910_read_port_0_r ),
		new IOReadPort( -1 )  /* end of table */
	};
	
	static IOWritePort rk_sound_writeport[] =
	{
/*TODO*///		new IOWritePort( 0x00, 0x00, DAC_data_w ),
		new IOWritePort( 0x02, 0x02, AY8910_write_port_0_w ),
		new IOWritePort( 0x03, 0x03, AY8910_control_port_0_w ),
		new IOWritePort( -1 )  /* end of table */
	};
	
	static InputPortPtr input_ports_kingofb = new InputPortPtr(){ public void handler() { 
	    PORT_START();  /* DSW0 - 0xfc01 */
	    PORT_DIPNAME( 0x03, 0x01, "Rest Up Points" );
	    PORT_DIPSETTING(    0x02, "70000" );
	    PORT_DIPSETTING(    0x01, "100000" );
	    PORT_DIPSETTING(    0x03, "150000" );
	    PORT_DIPSETTING(    0x00, DEF_STR( "No") );
	    PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Demo_Sounds") );
	    PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	    PORT_DIPNAME( 0x18, 0x00, DEF_STR( "Difficulty") );
	    PORT_DIPSETTING(    0x00, "Easy" );
	    PORT_DIPSETTING(    0x08, "Medium" );
	    PORT_DIPSETTING(    0x10, "Hard" );
	    PORT_DIPSETTING(    0x18, "Hardest" );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Cabinet") );
	    PORT_DIPSETTING(    0x20, DEF_STR( "Upright") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
	    PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
	    PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_SERVICE( 0x80, IP_ACTIVE_HIGH );
	
	    PORT_START();  /* DSW1 - 0xfc01 */
	    PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
	    PORT_DIPSETTING(    0x07, DEF_STR( "4C_1C") );
	    PORT_DIPSETTING(    0x06, DEF_STR( "3C_1C") );
	    PORT_DIPSETTING(    0x05, DEF_STR( "2C_1C") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
	    PORT_DIPSETTING(    0x01, DEF_STR( "1C_2C") );
	    PORT_DIPSETTING(    0x02, DEF_STR( "1C_3C") );
	    PORT_DIPSETTING(    0x03, DEF_STR( "1C_4C") );
	    PORT_DIPSETTING(    0x04, DEF_STR( "1C_5C") );
	    PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
	    PORT_DIPSETTING(    0x08, DEF_STR( "On") );
	    PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
	    PORT_DIPSETTING(    0x10, DEF_STR( "On") );
	    PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
	    PORT_DIPSETTING(    0x20, DEF_STR( "On") );
	    PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
	    PORT_DIPSETTING(    0x40, DEF_STR( "On") );
	    PORT_DIPNAME( 0x80, 0x00, "Freeze" );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
	    PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
	    PORT_START();  /* IN 0 - 0xfc02 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
	    PORT_START();  /* IN 1 - 0xfc03 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START();  /* IN 2 - 0xfc04 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
	    PORT_START();  /* IN 3 - 0xfc05 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON3 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	/* Ring King */
	static InputPortPtr input_ports_ringking = new InputPortPtr(){ public void handler() { 
	    PORT_START();  /* DSW0 - 0xe000 */
	    PORT_DIPNAME( 0x03, 0x03, "Replay" );
	    PORT_DIPSETTING(    0x01, "70000" );
	    PORT_DIPSETTING(    0x02, "100000" );
	    PORT_DIPSETTING(    0x00, "150000" );
	    PORT_DIPSETTING(    0x03, DEF_STR( "No") );
	    PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Demo_Sounds") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
	    PORT_DIPSETTING(    0x04, DEF_STR( "On") );
	    PORT_DIPNAME( 0x18, 0x10, "Difficulty(2P)" );
	    PORT_DIPSETTING(    0x18, "Easy" );
	    PORT_DIPSETTING(    0x10, "Medium" );
	    PORT_DIPSETTING(    0x08, "Hard" );
	    PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Cabinet") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
	    PORT_DIPSETTING(    0x20, DEF_STR( "Cocktail") );
	    PORT_BIT(			0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
	    PORT_START();  /* DSW1 - 0xe001 */
	    PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_A") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
	    PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
	    PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
	    PORT_DIPSETTING(    0x01, DEF_STR( "1C_3C") );
	    PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_B") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
	    PORT_DIPSETTING(    0x0c, DEF_STR( "1C_1C") );
	    PORT_DIPSETTING(    0x08, DEF_STR( "1C_2C") );
	    PORT_DIPSETTING(    0x04, DEF_STR( "1C_3C") );
	    PORT_DIPNAME( 0x30, 0x10, "Difficulty(1P)" );
	    PORT_DIPSETTING(    0x30, "Easy" );
	    PORT_DIPSETTING(    0x10, "Medium" );
	    PORT_DIPSETTING(    0x20, "Hard" );
	    PORT_DIPSETTING(    0x00, "Hardest" );
	    PORT_DIPNAME( 0x40, 0x40, "Boxing Match" );
	    PORT_DIPSETTING(    0x40, "2 Win,End" );
	    PORT_DIPSETTING(    0x00, "1 Win,End" );
	    PORT_DIPNAME( 0x80, 0x80, "Freeze" );
	    PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
	    PORT_START();  /* IN 0 - 0xe002 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
	    PORT_START();  /* IN 1 - 0xe003 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();  /* IN 2 - 0xe004 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_TILT );/* Service Switch */
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Sound busy??? */
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_VBLANK );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
	    PORT_START();  /* IN 3 - 0xfc05 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON3 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,    /* 8*8 characters */
		512,   /* 1024 characters */
		1,      /* 1 bits per pixel */
		new int[] { 0 },     /* only 1 plane */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8     /* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 chars */
		1024, 	/* 1024 characters */
		3,		/* bits per pixel */
		new int[] { 2*0x4000*8, 1*0x4000*8, 0*0x4000*8 },
		new int[] { 3*0x4000*8+0,3*0x4000*8+1,3*0x4000*8+2,3*0x4000*8+3,
				3*0x4000*8+4,3*0x4000*8+5,3*0x4000*8+6,3*0x4000*8+7,
				0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		16*8
	);
	
	static GfxLayout tilelayout = new GfxLayout
	(
		16,16,	/* 16*16 chars */
		512,	/* 512 characters */
		3,		/* bits per pixel */
		new int[] { 2*0x2000*8, 1*0x2000*8, 0*0x2000*8 },
		new int[] { 3*0x2000*8+0,3*0x2000*8+1,3*0x2000*8+2,3*0x2000*8+3,
				3*0x2000*8+4,3*0x2000*8+5,3*0x2000*8+6,3*0x2000*8+7,
				0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		16*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x00000, charlayout,   256,  8 ),	/* characters */
		new GfxDecodeInfo( REGION_GFX1, 0x01000, charlayout,   256,  8 ),	/* characters */
		new GfxDecodeInfo( REGION_GFX2, 0x00000, spritelayout,   0, 32 ),	/* sprites */
		new GfxDecodeInfo( REGION_GFX3, 0x00000, tilelayout,     0, 32 ),	/* bg tiles */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	/* Ring King */
	static GfxLayout rk_charlayout1 = new GfxLayout
	(
		8,8,    /* 8*8 characters */
		512,   /* 1024 characters */
		1,      /* 1 bits per pixel */
		new int[] { 0 },     /* only 1 plane */
		new int[] { 7, 6, 5, 4, (0x1000*8)+7, (0x1000*8)+6, (0x1000*8)+5, (0x1000*8)+4 },
		new int[] { 7*8, 6*8, 5*8, 4*8, 3*8, 2*8, 1*8, 0*8 },
		8*8     /* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout rk_charlayout2 = new GfxLayout
	(
		8,8,    /* 8*8 characters */
		512,   /* 1024 characters */
		1,      /* 1 bits per pixel */
		new int[] { 0 },     /* only 1 plane */
		new int[] { 3, 2, 1, 0, (0x1000*8)+3, (0x1000*8)+2, (0x1000*8)+1, (0x1000*8)+0 },
		new int[] { 7*8, 6*8, 5*8, 4*8, 3*8, 2*8, 1*8, 0*8 },
		8*8     /* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout rk_spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 chars */
		1024, 	/* 1024 characters */
		3,		/* bits per pixel */
		new int[] { 0*0x8000*8, 1*0x8000*8, 2*0x8000*8 },
		new int[] { 7, 6, 5, 4, 3, 2, 1, 0,
			16*8+7, 16*8+6, 16*8+5, 16*8+4, 16*8+3, 16*8+2, 16*8+1, 16*8+0 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		32*8
	);
	
	static GfxLayout rk_tilelayout = new GfxLayout
	(
		16,16,	/* 16*16 chars */
		512, 	/* 1024 characters */
		3,		/* bits per pixel */
		new int[] { 0*0x4000*8, 1*0x4000*8, 2*0x4000*8 },
		new int[] { 7, 6, 5, 4, 3, 2, 1, 0,
			16*8+7, 16*8+6, 16*8+5, 16*8+4, 16*8+3, 16*8+2, 16*8+1, 16*8+0 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		32*8
	);
	
	static GfxLayout rk_bglayout = new GfxLayout
	(
		16,16,	/* 16*16 chars */
		256, 	/* 1024 characters */
		3,		/* bits per pixel */
		new int[] { 0x4000*8+4, 0, 4 },
		new int[] { 16*8+3, 16*8+2, 16*8+1, 16*8+0, 0x2000*8+3, 0x2000*8+2, 0x2000*8+1, 0x2000*8+0,
			3, 2, 1, 0, 0x2010*8+3, 0x2010*8+2, 0x2010*8+1, 0x2010*8+0 },
		new int[] { 15*8, 14*8, 13*8, 12*8, 11*8, 10*8, 9*8, 8*8,
				7*8, 6*8, 5*8, 4*8, 3*8, 2*8, 1*8, 0*8 },
		32*8
	);
	
	
	static GfxDecodeInfo rk_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x00000, rk_charlayout1,  256,  8 ),	/* characters */
		new GfxDecodeInfo( REGION_GFX1, 0x00000, rk_charlayout2,  256,  8 ),	/* characters */
		new GfxDecodeInfo( REGION_GFX2, 0x00000, rk_spritelayout,   0, 32 ),	/* sprites */
		new GfxDecodeInfo( REGION_GFX3, 0x00000, rk_tilelayout,     0, 32 ),	/* sprites/bg tiles */
		new GfxDecodeInfo( REGION_GFX4, 0x00000, rk_bglayout,       0, 32 ),	/* bg tiles */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
        static AY8910interface ay8910_interface = new AY8910interface
	(
		1,      /* 1 chips */
		1500000,	/* 1.5 MHz? */
		new int[]{ 25 },
		new ReadHandlerPtr[]{soundlatch_r},
		new ReadHandlerPtr[]{ null },
		new WriteHandlerPtr[]{ null},
		new WriteHandlerPtr[]{ null }
	);
	
	static DACinterface dac_interface = new DACinterface
	(
		1,
		new int[] { 25 }
	);
	
	public static InterruptPtr kingobox_interrupt = new InterruptPtr() { public int handler()  {
	
		if ( kingofb_nmi_enable!=0 )
			return nmi_interrupt.handler();
	
		return ignore_interrupt.handler();
	} };
	
	static MachineDriver machine_driver_kingofb = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				4000000,        /* 4.0 Mhz */
				main_readmem, main_writemem,null,null,
				kingobox_interrupt,1
			),
			new MachineCPU(
				CPU_Z80,
				4000000,        /* 4.0 Mhz */
				video_readmem, video_writemem,null,null,
				kingobox_interrupt,1
			),
			new MachineCPU(
				CPU_Z80,
				4000000,        /* 4.0 Mhz */
				sprite_readmem, sprite_writemem,null,null,
				kingobox_interrupt,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				4000000,        /* 4.0 Mhz */
				sound_readmem, sound_writemem,sound_readport,sound_writeport,
				ignore_interrupt, 0,
				nmi_interrupt, 6000	/* Hz */
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,  /* frames per second, vblank duration */
		100, /* We really need heavy synching among the processors */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		256+8, 256+8*2,
		kingobox_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		generic_vh_start,
		generic_vh_stop,
		kingobox_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				ay8910_interface
			),
			new MachineSound(
				SOUND_DAC,
				dac_interface
			)
		}
	);
	
	
	/* Ring King */
	static MachineDriver machine_driver_ringking = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				4000000,        /* 4.0 Mhz */
				rk_main_readmem, rk_main_writemem,null,null,
				kingobox_interrupt,1
			),
			new MachineCPU(
				CPU_Z80,
				4000000,        /* 4.0 Mhz */
				rk_video_readmem, rk_video_writemem,null,null,
				kingobox_interrupt,1
			),
			new MachineCPU(
				CPU_Z80,
				4000000,        /* 4.0 Mhz */
				rk_sprite_readmem, rk_sprite_writemem,null,null,
				kingobox_interrupt,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				4000000,        /* 4.0 Mhz */
				sound_readmem, sound_writemem,rk_sound_readport,rk_sound_writeport,
				ignore_interrupt, 0,
				nmi_interrupt, 6000	/* Hz */
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,  /* frames per second, vblank duration */
		100, /* We really need heavy synching among the processors */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		rk_gfxdecodeinfo,
		256+8, 256+8*2,
		ringking_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		generic_vh_start,
		generic_vh_stop,
		ringking_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				ay8910_interface
			),
			new MachineSound(
				SOUND_DAC,
				dac_interface
			)
		}
	);
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_kingofb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "d09_22.bin",   0x00000, 0x4000, 0x6220bfa2 );
		ROM_LOAD( "e09_23.bin",   0x04000, 0x4000, 0x5782fdd8 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the video cpu */
		ROM_LOAD( "b09_21.bin",   0x00000, 0x4000, 0x3fb39489 );
	
		ROM_REGION( 0x10000, REGION_CPU3 );    /* 64k for the sprite cpu */
		ROM_LOAD( "j09_dcr.bin",  0x00000, 0x2000, 0x379f4f84 );
	
		ROM_REGION( 0x10000, REGION_CPU4 );    /* 64k for the audio cpu */
		ROM_LOAD( "f05_18.bin",   0x00000, 0x4000, 0xc057e28e );
		ROM_LOAD( "h05_19.bin",   0x04000, 0x4000, 0x060253dd );
		ROM_LOAD( "j05_20.bin",   0x08000, 0x4000, 0x64c137a4 );
	
		ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "vd15_13.bin",  0x00000, 0x2000, 0xe36d4f4f );/* characters */
	
		ROM_REGION( 0x18000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* sprites */
		ROM_LOAD( "vb01_01.bin",  0x00000, 0x4000, 0xce6580af );
		ROM_LOAD( "vb04_03.bin",  0x04000, 0x4000, 0xcf74ea50 );
		ROM_LOAD( "vb07_05.bin",  0x08000, 0x4000, 0xd8b53975 );
		ROM_LOAD( "vb03_02.bin",  0x0c000, 0x4000, 0x4ab506d2 );
		ROM_LOAD( "vb05_04.bin",  0x10000, 0x4000, 0xecf95a2c );
		ROM_LOAD( "vb08_06.bin",  0x14000, 0x4000, 0x8200cb2b );
	
		ROM_REGION( 0xc000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* tiles */
		ROM_LOAD( "vd01_07.bin",  0x00000, 0x2000, 0x3d472a22 );
		ROM_LOAD( "vd04_09.bin",  0x02000, 0x2000, 0xcc002ea9 );
		ROM_LOAD( "vd07_11.bin",  0x04000, 0x2000, 0x23c1b3ee );
		ROM_LOAD( "vd03_08.bin",  0x06000, 0x2000, 0xd6b1b8fe );
		ROM_LOAD( "vd05_10.bin",  0x08000, 0x2000, 0xfce71e5a );
		ROM_LOAD( "vd08_12.bin",  0x0a000, 0x2000, 0x3f68b991 );
	
		ROM_REGION( 0x0300, REGION_PROMS );
		ROM_LOAD( "vb14_col.bin", 0x0000, 0x0100, 0xc58e5121 );/* red component */
		ROM_LOAD( "vb15_col.bin", 0x0100, 0x0100, 0x5ab06f25 );/* green component */
		ROM_LOAD( "vb16_col.bin", 0x0200, 0x0100, 0x1171743f );/* blue component */
	ROM_END(); }}; 
	
	/* Ring King */
	static RomLoadPtr rom_ringking = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "cx13.9f",      0x00000, 0x8000, 0x93e38c02 );
		ROM_LOAD( "cx14.11f",     0x08000, 0x4000, 0xa435acb0 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the video cpu */
		ROM_LOAD( "cx07.10c",     0x00000, 0x4000, 0x9f074746 );
	
		ROM_REGION( 0x10000, REGION_CPU3 );    /* 64k for the sprite cpu */
		ROM_LOAD( "cx00.4c",      0x00000, 0x2000, 0x880b8aa7 );
	
		ROM_REGION( 0x10000, REGION_CPU4 );    /* 64k for the audio cpu */
		ROM_LOAD( "cx12.4ef",     0x00000, 0x8000, 0x1d5d6c6b );
		ROM_LOAD( "j05_20.bin",   0x08000, 0x4000, 0x64c137a4 );
	
		ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cx08.13b",     0x00000, 0x2000, 0xdbd7c1c2 );/* characters */
	
		ROM_REGION( 0x18000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cx04.11j",     0x00000, 0x8000, 0x506a2ed9 );
		ROM_LOAD( "cx02.8j",      0x08000, 0x8000, 0x009dde6a );
		ROM_LOAD( "cx06.13j",     0x10000, 0x8000, 0xd819a3b2 );
	
		ROM_REGION( 0xc000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cx03.9j",      0x00000, 0x4000, 0x682fd1c4 );/* sprites */
		ROM_LOAD( "cx01.7j",      0x04000, 0x4000, 0x85130b46 );
		ROM_LOAD( "cx05.12j",     0x08000, 0x4000, 0xf7c4f3dc );
	
		ROM_REGION( 0x8000, REGION_GFX4 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cx09.17d",     0x00000, 0x4000, 0x37a082cf );/* tiles */
		ROM_LOAD( "cx10.17e",     0x04000, 0x4000, 0xab9446c5 );
	
		ROM_REGION( 0x0200, REGION_PROMS );
		ROM_LOAD( "82s135.2a",    0x0000, 0x0100, 0x0e723a83 );/* red and green component */
		ROM_LOAD( "82s129.1a",    0x0100, 0x0100, 0xd345cbb3 );/* blue component */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ringkin2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "rkngm1.bin",   0x00000, 0x8000, 0x086921ea );
		ROM_LOAD( "rkngm2.bin",   0x08000, 0x4000, 0xc0b636a4 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the video cpu */
		ROM_LOAD( "rkngtram.bin", 0x00000, 0x4000, 0xd9dc1a0a );
	
		ROM_REGION( 0x10000, REGION_CPU3 );    /* 64k for the sprite cpu */
		ROM_LOAD( "cx00.4c",      0x00000, 0x2000, 0x880b8aa7 );
	
		ROM_REGION( 0x10000, REGION_CPU4 );    /* 64k for the audio cpu */
		ROM_LOAD( "cx12.4ef",     0x00000, 0x8000, 0x1d5d6c6b );
		ROM_LOAD( "j05_20.bin",   0x08000, 0x4000, 0x64c137a4 );
	
		ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cx08.13b",     0x00000, 0x2000, 0xdbd7c1c2 );/* characters */
	
		ROM_REGION( 0x18000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cx04.11j",     0x00000, 0x8000, 0x506a2ed9 );
		ROM_LOAD( "cx02.8j",      0x08000, 0x8000, 0x009dde6a );
		ROM_LOAD( "cx06.13j",     0x10000, 0x8000, 0xd819a3b2 );
	
		ROM_REGION( 0xc000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cx03.9j",      0x00000, 0x4000, 0x682fd1c4 );/* sprites */
		ROM_LOAD( "cx01.7j",      0x04000, 0x4000, 0x85130b46 );
		ROM_LOAD( "cx05.12j",     0x08000, 0x4000, 0xf7c4f3dc );
	
		ROM_REGION( 0x8000, REGION_GFX4 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cx09.17d",     0x00000, 0x4000, 0x37a082cf );/* tiles */
		ROM_LOAD( "cx10.17e",     0x04000, 0x4000, 0xab9446c5 );
	
		ROM_REGION( 0x0200, REGION_PROMS );
		ROM_LOAD( "82s135.2a",    0x0000, 0x0100, 0x0e723a83 );/* red and green component */
		ROM_LOAD( "82s129.1a",    0x0100, 0x0100, 0xd345cbb3 );/* blue component */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ringkin3 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "14.9d",        0x00000, 0x4000, 0x63627b8b );
		ROM_LOAD( "15.9e",        0x04000, 0x4000, 0xe7557489 );
		ROM_LOAD( "16.9f",        0x08000, 0x4000, 0xa3b3bb16 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the video cpu */
		ROM_LOAD( "13.9b",        0x00000, 0x4000, 0xf33f94a2 );
	
		ROM_REGION( 0x10000, REGION_CPU3 );    /* 64k for the sprite cpu */
		ROM_LOAD( "j09_dcr.bin",  0x00000, 0x2000, 0x379f4f84 );
	
		ROM_REGION( 0x10000, REGION_CPU4 );    /* 64k for the audio cpu */
		ROM_LOAD( "f05_18.bin",   0x00000, 0x4000, 0xc057e28e );
		ROM_LOAD( "h05_19.bin",   0x04000, 0x4000, 0x060253dd );
		ROM_LOAD( "j05_20.bin",   0x08000, 0x4000, 0x64c137a4 );
	
		ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "12.15d",       0x00000, 0x2000, 0x988a77bf );/* characters (Japanese) */
	
		ROM_REGION( 0x18000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* sprites */
		ROM_LOAD( "vb01_01.bin",  0x00000, 0x4000, 0xce6580af );
		ROM_LOAD( "vb04_03.bin",  0x04000, 0x4000, 0xcf74ea50 );
		ROM_LOAD( "vb07_05.bin",  0x08000, 0x4000, 0xd8b53975 );
		ROM_LOAD( "vb03_02.bin",  0x0c000, 0x4000, 0x4ab506d2 );
		ROM_LOAD( "vb05_04.bin",  0x10000, 0x4000, 0xecf95a2c );
		ROM_LOAD( "vb08_06.bin",  0x14000, 0x4000, 0x8200cb2b );
	
		ROM_REGION( 0xc000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* tiles */
		ROM_LOAD( "7.1d",         0x00000, 0x2000, 0x019a88b0 );
		ROM_LOAD( "9.4d",         0x02000, 0x2000, 0xbfdc741a );
		ROM_LOAD( "11.7d",        0x04000, 0x2000, 0x3cc7bdc5 );
		ROM_LOAD( "8.3d",         0x06000, 0x2000, 0x65f1281b );
		ROM_LOAD( "10.5d",        0x08000, 0x2000, 0xaf5013e7 );
		ROM_LOAD( "12.8d",        0x0a000, 0x2000, 0x00000000 );
	
		ROM_REGION( 0x0300, REGION_PROMS );
		/* we load the ringking PROMs and then expand the first to look like the kingobox ones... */
		ROM_LOAD( "82s135.2a",    0x0100, 0x0100, 0x0e723a83 );/* red and green component */
		ROM_LOAD( "82s129.1a",    0x0200, 0x0100, 0xd345cbb3 );/* blue component */
	ROM_END(); }}; 
	
	
	public static InitDriverPtr init_ringkin3 = new InitDriverPtr() { public void handler() 
	{
		int i;
		UBytePtr RAM = memory_region(REGION_PROMS);
	
		/* expand the first color PROM to look like the kingobox ones... */
		for (i = 0;i < 0x100;i++)
			RAM.write(i,RAM.read(i + 0x100) >> 4);
	} };
	
	
	
	public static GameDriver driver_kingofb	   = new GameDriver("1985"	,"kingofb"	,"kingobox.java"	,rom_kingofb,null	,machine_driver_kingofb	,input_ports_kingofb	,null	,ROT90	,	"Woodplace Inc.", "King of Boxer (English)" );
	public static GameDriver driver_ringking	   = new GameDriver("1985"	,"ringking"	,"kingobox.java"	,rom_ringking,driver_kingofb	,machine_driver_ringking	,input_ports_ringking	,null	,ROT90	,	"Data East USA", "Ring King (set 1)" );
        public static GameDriver driver_ringkin2	   = new GameDriver("1985"	,"ringkin2"	,"kingobox.java"	,rom_ringkin2,driver_kingofb	,machine_driver_ringking,input_ports_kingofb	,null	,ROT90, "<unknown>", "Ring King (set 2)", GAME_NOT_WORKING );
	public static GameDriver driver_ringkin3	   = new GameDriver("1985"	,"ringkin3"	,"kingobox.java"	,rom_ringkin3,driver_kingofb	,machine_driver_kingofb	,input_ports_kingofb	,init_ringkin3	,ROT90	,	"Data East USA", "Ring King (set 3)" );
}
