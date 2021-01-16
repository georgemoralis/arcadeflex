/***********************************************************************

Namco System 1

Preliminary driver by:
Ernesto Corvi
ernesto@imagina.com

Updates by:
Vernon C. Brooks


Notes:
- The berabohm buttons don't work too well. The real thing has a special
  pressure sensitive button, with two switches. The harder you push the
  button, the faster the two switches are closed one after another.
  Due to MAME's limited input sample rate (once per frame) it is difficult
  to measure the time between the two.



Namco System 1 hardware
=======================

Processors:

6809  - Main CPU
6809  - Sub CPU
6809  - Sound CPU (PSG,FM)
63701 - MCU (input,EEPROM,DAC)

Inter-processor communication is done via a 2K shared memory area.

Bankswitching:

Main/Sub - a 10-bit value is written to location Ex00 to select the 8K
bank (RAM or PRG0-PRG7) which is accessed at offset x000. (x is even)

Sound - a 3-bit value is written to location C000 or C001, bits 4-6 to
select the 16K bank (SND0-SND1) which is accessed at offset 0000.

MCU - a 8-bit value is written to location D800 to select the 32K bank
(VOI0-VOI5) which is accessed at offset 4000. Bits 2-7 are a bitmask
which specify the ROM to access and bits 0-1 specify the ROM offset.

Graphics:

Visible screen resolution: 288x244 pixels (36x28 tiles)

3 scrolling 64x64 tilemapped playfields
1 scrolling 64x32 tilemapped playfield
2 fixed 36x28 tilemapped playfields

Each playfield uses one of 8 color palettes, can be enabled or disabled,
and has programmable priorities.

Each tile is a 8x8, 8 bit-per-pixel character from a selection of up to
16384 characters (CHR0-CHR7). A separate 1 bit-per-pixel character mask
(CHR8) defines the character shape.

127 displayable 32x32, 4 bit-per-pixel sprites from a selection of up to
2048 sprites (OBJ0-OBJ7). Each sprite uses one of 127 color palettes or
a special shadow/highlight effect and has programmable priorities and
x-y flipping. Sprites may also be displayed as a smaller portion of a
32x32 object with a programmable size and position. The height and width
are programmed separately and may be 4,8,16,or 32 pixels.

3 24-bit programmable RGB palette tables, 8 bits per color as follows:

127 16-color entries for the sprites
8 256-color entries for the playfields
8 256-color entries for the playfields shadow/highlight effects

Sound:

Namco custom 8 channel 16-bit stereo PSG for sound effects
registor array based 2 channel 8-bit DAC for voice
Yamaha YM2151+YM3012 FM chip for background music

Controls:

The standard hardware supports one or two 8-way joysticks with up to
three buttons for each player, two start buttons, a service switch, two
coin slots, and one dipswitch block. Game settings are accessed via
service mode and are saved in EEPROM.

Games:

Date  Name									Key  Screen
----- ------------------------------------- ---- ------
 4/87 Yokai Douchuuki / Shadowland			NONE H
 6/87 Dragon Spirit (old version)			136  V
??/87 Dragon Spirit (new version)			136  V
 7/87 Blazer								144  V
 9/87 Quester								A	 V
??/87 Quester (special edition) 			A	 V
11/87 Pac-Mania 							151  V-FLIP
11/87 Pac-Mania (Japanese version)			151  V
12/87 Galaga '88                            153  V-FLIP
12/87 Galaga '88 (Japanese version)         153  V
 3/88 World Stadium 						154  H
 5/88 Beraboh Man							B	 H
??/88 Beraboh Man (standard NS1 hardware)	NONE H
 7/88 Marchen Maze / Alice In Wonderland	152  H
 8/88 Bakutotsu Kijuutei / Baraduke 2		155  H
10/88 World Court							143  H
11/88 Splatter House						181  H
12/88 Face Off								C	 H
 2/89 Rompers								182  V
 3/89 Blast Off 							183  V
 7/89 World Stadium '89                     184  H
12/89 Dangerous Seed						308  V
 7/90 World Stadium '90                     310  H
10/90 Pistol Daimyo no Bouken				309  H-FLIP
11/90 Souko Ban Deluxe						311  H-FLIP
??/90 Puzzle Club (prototype)				?	 V
12/91 Tank Force							185  H-FLIP

A - uses sub board with paddle control(s)
B - uses sub board with pressure sensitive controls
C - uses sub board with support for player 3 and 4 controls

***********************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.drivers;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.cpu.m6800.hd63701.hd63701_internal_registers_r;
import static gr.codebb.arcadeflex.v036.cpu.m6800.hd63701.hd63701_internal_registers_w;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.sound._2151intf.*;
import static gr.codebb.arcadeflex.v036.sound._2151intfH.*;
import static gr.codebb.arcadeflex.v036.sound.namco.*;
import static gr.codebb.arcadeflex.v037b7.machine.namcos1.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.sound.dac.*;
import static gr.codebb.arcadeflex.v036.cpu.m6800.hd63701.*;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.M6809_FIRQ_LINE;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static gr.codebb.arcadeflex.v036.sound.namcoH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.sound.dacH.*;
import static gr.codebb.arcadeflex.v036.platform.fileio.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b16.vidhrdw.namcos1.*;

public class namcos1
{
/*TODO*///	
/*TODO*///	/* from vidhrdw */
/*TODO*///	extern extern extern extern 
/*TODO*///	/* from machine */
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	extern 
/*TODO*///	extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern 

	static UBytePtr nvram;
	static int[] nvram_size = new int[1];
	
	public static nvramPtr nvram_handler  = new nvramPtr() { public void handler(Object file, int read_or_write) 
	{
		if (read_or_write != 0)
			osd_fwrite(file,nvram,nvram_size[0]);
		else
		{
			if (file != null)
				osd_fread(file,nvram,nvram_size[0]);
		}
	} };
	
	/**********************************************************************/
	
	static MemoryReadAddress main_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x1fff, namcos1_0_banked_area0_r ),
		new MemoryReadAddress( 0x2000, 0x3fff, namcos1_0_banked_area1_r ),
		new MemoryReadAddress( 0x4000, 0x5fff, namcos1_0_banked_area2_r ),
		new MemoryReadAddress( 0x6000, 0x7fff, namcos1_0_banked_area3_r ),
		new MemoryReadAddress( 0x8000, 0x9fff, namcos1_0_banked_area4_r ),
		new MemoryReadAddress( 0xa000, 0xbfff, namcos1_0_banked_area5_r ),
		new MemoryReadAddress( 0xc000, 0xdfff, namcos1_0_banked_area6_r ),
		new MemoryReadAddress( 0xe000, 0xffff, namcos1_0_banked_area7_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress main_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x1fff, namcos1_0_banked_area0_w ),
		new MemoryWriteAddress( 0x2000, 0x3fff, namcos1_0_banked_area1_w ),
		new MemoryWriteAddress( 0x4000, 0x5fff, namcos1_0_banked_area2_w ),
		new MemoryWriteAddress( 0x6000, 0x7fff, namcos1_0_banked_area3_w ),
		new MemoryWriteAddress( 0x8000, 0x9fff, namcos1_0_banked_area4_w ),
		new MemoryWriteAddress( 0xa000, 0xbfff, namcos1_0_banked_area5_w ),
		new MemoryWriteAddress( 0xc000, 0xdfff, namcos1_0_banked_area6_w ),
		new MemoryWriteAddress( 0xe000, 0xefff, namcos1_bankswitch_w ),
		new MemoryWriteAddress( 0xf000, 0xf000, namcos1_cpu_control_w ),
		new MemoryWriteAddress( 0xf200, 0xf200, MWA_NOP ), /* watchdog? */
	//	new MemoryWriteAddress( 0xf400, 0xf400, MWA_NOP ), /* unknown */
	//	new MemoryWriteAddress( 0xf600, 0xf600, MWA_NOP ), /* unknown */
		new MemoryWriteAddress( 0xfc00, 0xfc01, namcos1_subcpu_bank_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress sub_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x1fff, namcos1_1_banked_area0_r ),
		new MemoryReadAddress( 0x2000, 0x3fff, namcos1_1_banked_area1_r ),
		new MemoryReadAddress( 0x4000, 0x5fff, namcos1_1_banked_area2_r ),
		new MemoryReadAddress( 0x6000, 0x7fff, namcos1_1_banked_area3_r ),
		new MemoryReadAddress( 0x8000, 0x9fff, namcos1_1_banked_area4_r ),
		new MemoryReadAddress( 0xa000, 0xbfff, namcos1_1_banked_area5_r ),
		new MemoryReadAddress( 0xc000, 0xdfff, namcos1_1_banked_area6_r ),
		new MemoryReadAddress( 0xe000, 0xffff, namcos1_1_banked_area7_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress sub_writemem[] =
	{
		new MemoryWriteAddress( 0xe000, 0xefff, namcos1_bankswitch_w ),
		new MemoryWriteAddress( 0x0000, 0x1fff, namcos1_1_banked_area0_w ),
		new MemoryWriteAddress( 0x2000, 0x3fff, namcos1_1_banked_area1_w ),
		new MemoryWriteAddress( 0x4000, 0x5fff, namcos1_1_banked_area2_w ),
		new MemoryWriteAddress( 0x6000, 0x7fff, namcos1_1_banked_area3_w ),
		new MemoryWriteAddress( 0x8000, 0x9fff, namcos1_1_banked_area4_w ),
		new MemoryWriteAddress( 0xa000, 0xbfff, namcos1_1_banked_area5_w ),
		new MemoryWriteAddress( 0xc000, 0xdfff, namcos1_1_banked_area6_w ),
		new MemoryWriteAddress( 0xf000, 0xf000, MWA_NOP ), /* IO Chip */
		new MemoryWriteAddress( 0xf200, 0xf200, MWA_NOP ), /* watchdog? */
	//	new MemoryWriteAddress( 0xf400, 0xf400, MWA_NOP ), /* unknown */
	//	new MemoryWriteAddress( 0xf600, 0xf600, MWA_NOP ), /* unknown */
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x3fff, MRA_BANK1 ),	/* Banked ROMs */
		new MemoryReadAddress( 0x4000, 0x4001, YM2151_status_port_0_r ),
		new MemoryReadAddress( 0x5000, 0x50ff, namcos1_wavedata_r ), /* PSG ( Shared ) */
		new MemoryReadAddress( 0x5100, 0x513f, namcos1_sound_r ),	/* PSG ( Shared ) */
		new MemoryReadAddress( 0x5140, 0x54ff, MRA_RAM ),	/* Sound RAM 1 - ( Shared ) */
		new MemoryReadAddress( 0x7000, 0x77ff, MRA_BANK2 ),	/* Sound RAM 2 - ( Shared ) */
		new MemoryReadAddress( 0x8000, 0x9fff, MRA_RAM ),	/* Sound RAM 3 */
		new MemoryReadAddress( 0xc000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x3fff, MWA_ROM ),	/* Banked ROMs */
		new MemoryWriteAddress( 0x4000, 0x4000, YM2151_register_port_0_w ),
		new MemoryWriteAddress( 0x4001, 0x4001, YM2151_data_port_0_w ),
		new MemoryWriteAddress( 0x5000, 0x50ff, namcos1_wavedata_w,namco_wavedata ), /* PSG ( Shared ) */
		new MemoryWriteAddress( 0x5100, 0x513f, namcos1_sound_w,namco_soundregs ),	/* PSG ( Shared ) */
		new MemoryWriteAddress( 0x5140, 0x54ff, MWA_RAM ),	/* Sound RAM 1 - ( Shared ) */
		new MemoryWriteAddress( 0x7000, 0x77ff, MWA_BANK2 ),	/* Sound RAM 2 - ( Shared ) */
		new MemoryWriteAddress( 0x8000, 0x9fff, MWA_RAM ),	/* Sound RAM 3 */
		new MemoryWriteAddress( 0xc000, 0xc001, namcos1_sound_bankswitch_w ), /* bank selector */
		new MemoryWriteAddress( 0xd001, 0xd001, MWA_NOP ),	/* watchdog? */
		new MemoryWriteAddress( 0xe000, 0xe000, MWA_NOP ),	/* IRQ clear ? */
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	public static ReadHandlerPtr dsw_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int ret = readinputport(2);
	
		if (offset == 0)
			ret >>= 4;
	
		return 0xf0 | (ret & 0x0f);
	} };
	
	public static WriteHandlerPtr namcos1_coin_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		coin_lockout_global_w.handler(0,~data & 1);
		coin_counter_w.handler(0,data & 2);
		coin_counter_w.handler(1,data & 4);
	} };
	
	static int dac0_value ,dac1_value, dac0_gain=0, dac1_gain=0;
	
	static void namcos1_update_DACs()
	{
		DAC_signed_data_16_w.handler(0,0x8000+(dac0_value * dac0_gain)+(dac1_value * dac1_gain));
	}
	
	public static WriteHandlerPtr namcos1_dac_gain_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int value;
		/* DAC0 */
		value = (data&1)|((data>>1)&2); /* GAIN0,GAIN1 */
		dac0_gain = 0x0101 * (value+1) /4 /2;
		/* DAC1 */
		value = (data>>3)&3; /* GAIN2,GAIN3 */
		dac1_gain = 0x0101 * (value+1) / 4 /2;
		namcos1_update_DACs();
	} };
	
	public static WriteHandlerPtr namcos1_dac0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		dac0_value = data-0x80; /* shift zero point */
		namcos1_update_DACs();
	} };
	
	public static WriteHandlerPtr namcos1_dac1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		dac1_value = data-0x80; /* shift zero point */
		namcos1_update_DACs();
	} };
	
/*TODO*///	static int num=0, strobe=0;
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr quester_in0_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int ret;
/*TODO*///	
/*TODO*///		if (!num)
/*TODO*///			ret = (readinputport(0)&0x90) | strobe | (readinputport(4)&0x0f);
/*TODO*///		else
/*TODO*///			ret = (readinputport(0)&0x90) | strobe | (readinputport(5)&0x0f);
/*TODO*///	
/*TODO*///		strobe ^= 0x40;
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr quester_in1_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int ret;
/*TODO*///	
/*TODO*///		if (!num)
/*TODO*///			ret = (readinputport(1)&0x90) | num | (readinputport(4)>>4);
/*TODO*///		else
/*TODO*///			ret = (readinputport(1)&0x90) | num | (readinputport(5)>>4);
/*TODO*///	
/*TODO*///		if (!strobe) num ^= 0x20;
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr faceoff_in0_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int ret;
/*TODO*///	
/*TODO*///		if (!num)
/*TODO*///			ret = (readinputport(0)&0x80) | (readinputport(4)&0x1f);
/*TODO*///		else if (num==3)
/*TODO*///			ret = (readinputport(0)&0x80) | (readinputport(5)&0x1f);
/*TODO*///		else
/*TODO*///			ret = (readinputport(0)&0x80) | (readinputport(6)&0x1f);
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr faceoff_in1_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int ret;
/*TODO*///	
/*TODO*///		if (strobe != 0)
/*TODO*///		{
/*TODO*///			if (!num)
/*TODO*///				ret = (readinputport(1)&0x80) | strobe | ((readinputport(7)&0x07)<<3);
/*TODO*///			else
/*TODO*///				ret = (readinputport(1)&0x80) | strobe | (readinputport(7)&0x18);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if (num==0) num=3;
/*TODO*///			else if (num==3) num=4;
/*TODO*///			else if (num==4) num=0;
/*TODO*///			ret = (readinputport(1)&0x80) | num;
/*TODO*///		}
/*TODO*///	
/*TODO*///		strobe ^= 0x40;
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	} };
	
	static MemoryReadAddress mcu_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x001f, hd63701_internal_registers_r ),
		new MemoryReadAddress( 0x0080, 0x00ff, MRA_RAM ), /* built in RAM */
		new MemoryReadAddress( 0x1400, 0x1400, input_port_0_r ),
		new MemoryReadAddress( 0x1401, 0x1401, input_port_1_r ),
		new MemoryReadAddress( 0x1000, 0x1002, dsw_r ),
		new MemoryReadAddress( 0x4000, 0xbfff, MRA_BANK4 ), /* banked ROM */
		new MemoryReadAddress( 0xc000, 0xc7ff, MRA_BANK3 ),
		new MemoryReadAddress( 0xc800, 0xcfff, MRA_RAM ), /* EEPROM */
		new MemoryReadAddress( 0xf000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress mcu_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x001f, hd63701_internal_registers_w ),
		new MemoryWriteAddress( 0x0080, 0x00ff, MWA_RAM ), /* built in RAM */
		new MemoryWriteAddress( 0x4000, 0xbfff, MWA_ROM ),
		new MemoryWriteAddress( 0xc000, 0xc000, namcos1_mcu_patch_w ),
		new MemoryWriteAddress( 0xc000, 0xc7ff, MWA_BANK3 ),
		new MemoryWriteAddress( 0xc800, 0xcfff, MWA_RAM, nvram, nvram_size ), /* EEPROM */
		new MemoryWriteAddress( 0xd000, 0xd000, namcos1_dac0_w ),
		new MemoryWriteAddress( 0xd400, 0xd400, namcos1_dac1_w ),
		new MemoryWriteAddress( 0xd800, 0xd800, namcos1_mcu_bankswitch_w ), /* BANK selector */
		new MemoryWriteAddress( 0xf000, 0xf000, MWA_NOP ), /* IRQ clear ? */
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
/*TODO*///	static MemoryReadAddress quester_mcu_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x0000, 0x001f, hd63701_internal_registers_r ),
/*TODO*///		new MemoryReadAddress( 0x0080, 0x00ff, MRA_RAM ), /* built in RAM */
/*TODO*///		new MemoryReadAddress( 0x1400, 0x1400, quester_in0_r ),
/*TODO*///		new MemoryReadAddress( 0x1401, 0x1401, quester_in1_r ),
/*TODO*///		new MemoryReadAddress( 0x1000, 0x1002, dsw_r ),
/*TODO*///		new MemoryReadAddress( 0x4000, 0xbfff, MRA_BANK4 ), /* banked ROM */
/*TODO*///		new MemoryReadAddress( 0xc000, 0xc7ff, MRA_BANK3 ),
/*TODO*///		new MemoryReadAddress( 0xc800, 0xcfff, MRA_RAM ), /* EEPROM */
/*TODO*///		new MemoryReadAddress( 0xf000, 0xffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( -1 )	/* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryReadAddress faceoff_mcu_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x0000, 0x001f, hd63701_internal_registers_r ),
/*TODO*///		new MemoryReadAddress( 0x0080, 0x00ff, MRA_RAM ), /* built in RAM */
/*TODO*///		new MemoryReadAddress( 0x1400, 0x1400, faceoff_in0_r ),
/*TODO*///		new MemoryReadAddress( 0x1401, 0x1401, faceoff_in1_r ),
/*TODO*///		new MemoryReadAddress( 0x1000, 0x1002, dsw_r ),
/*TODO*///		new MemoryReadAddress( 0x4000, 0xbfff, MRA_BANK4 ), /* banked ROM */
/*TODO*///		new MemoryReadAddress( 0xc000, 0xc7ff, MRA_BANK3 ),
/*TODO*///		new MemoryReadAddress( 0xc800, 0xcfff, MRA_RAM ), /* EEPROM */
/*TODO*///		new MemoryReadAddress( 0xf000, 0xffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( -1 )	/* end of table */
/*TODO*///	};
	
	static IOReadPort mcu_readport[] =
	{
		new IOReadPort( HD63701_PORT1, HD63701_PORT1, input_port_3_r ),
		new IOReadPort( -1 )	/* end of table */
	};
	
	static IOWritePort mcu_writeport[] =
	{
		new IOWritePort( HD63701_PORT1, HD63701_PORT1, namcos1_coin_w ),
		new IOWritePort( HD63701_PORT2, HD63701_PORT2, namcos1_dac_gain_w ),
		new IOWritePort( -1 )	/* end of table */
	};
	
	
	
	/* Standard Namco System 1 input port definition */
	static InputPortPtr input_ports_ns1 = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 		/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP	  | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START(); 		/* DSW1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_DIPNAME( 0x08, 0x08, "Auto Data Sampling" );
		PORT_DIPSETTING(	0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_DIPNAME( 0x40, 0x40, "Freeze" );
		PORT_DIPSETTING(	0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
		PORT_START(); 		/* IN2 : mcu PORT2 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_SPECIAL );/* OUT:coin lockout */
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_SPECIAL );/* OUT:coin counter 1 */
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_SPECIAL );/* OUT:coin counter 2 */
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_SERVICE, "Service Button", KEYCODE_F1, IP_JOY_NONE );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
/*TODO*///	/* Dragon Spirit input port definition - dip switches are different */
/*TODO*///	static InputPortPtr input_ports_dspirit = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START(); 		/* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///	
/*TODO*///		PORT_START(); 		/* IN1 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP	  | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///	
/*TODO*///		PORT_START(); 		/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x7f, 0x7f, "Life" );
/*TODO*///		PORT_DIPSETTING(	0x7f, "2" );
/*TODO*///		PORT_DIPSETTING(	0x16, "3" );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	
/*TODO*///		PORT_START(); 		/* IN2 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_SPECIAL );/* OUT:coin lockout */
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_SPECIAL );/* OUT:coin counter 1 */
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_SPECIAL );/* OUT:coin counter 2 */
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_SERVICE, "Service Button", KEYCODE_F1, IP_JOY_NONE );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_SERVICE1 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/* Quester input port definition - paddle controls */
/*TODO*///	static InputPortPtr input_ports_quester = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START(); 		/* IN0 */
/*TODO*///		PORT_BIT( 0x0f, IP_ACTIVE_LOW, IPT_UNUSED );	/* paddle */
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///	
/*TODO*///		PORT_START(); 		/* IN1 */
/*TODO*///		PORT_BIT( 0x0f, IP_ACTIVE_LOW, IPT_UNUSED );	/* paddle */
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///	
/*TODO*///		PORT_START(); 		/* DSW1 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Auto Data Sampling" );
/*TODO*///		PORT_DIPSETTING(	0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "Freeze" );
/*TODO*///		PORT_DIPSETTING(	0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	
/*TODO*///		PORT_START(); 		/* IN2 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_SPECIAL );/* OUT:coin lockout */
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_SPECIAL );/* OUT:coin counter 1 */
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_SPECIAL );/* OUT:coin counter 2 */
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_SERVICE, "Service Button", KEYCODE_F1, IP_JOY_NONE );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_SERVICE1 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///	
/*TODO*///		PORT_START(); 		/* IN4 - fake input port for player 1 paddle */
/*TODO*///		PORT_ANALOG( 0xff, 0x00, IPT_DIAL, 30, 15, 0, 0 );
/*TODO*///	
/*TODO*///		PORT_START(); 		/* IN5 - fake input port for player 2 paddle */
/*TODO*///		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER2, 30, 15, 0, 0 );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/* Face Off input port definition - 4 player controls */
/*TODO*///	static InputPortPtr input_ports_faceoff = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START(); 		/* IN0 */
/*TODO*///		PORT_BIT( 0x7f, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///	
/*TODO*///		PORT_START(); 		/* IN1 */
/*TODO*///		PORT_BIT( 0x7f, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///	
/*TODO*///		PORT_START(); 		/* DSW1 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Auto Data Sampling" );
/*TODO*///		PORT_DIPSETTING(	0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "Freeze" );
/*TODO*///		PORT_DIPSETTING(	0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	
/*TODO*///		PORT_START(); 		/* IN2 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_SPECIAL );/* OUT:coin lockout */
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_SPECIAL );/* OUT:coin counter 1 */
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_SPECIAL );/* OUT:coin counter 2 */
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_SERVICE, "Service Button", KEYCODE_F1, IP_JOY_NONE );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_SERVICE1 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///	
/*TODO*///		PORT_START(); 		/* IN4 - fake input port for player 1 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
/*TODO*///		PORT_BIT( 0xe0, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///	
/*TODO*///		PORT_START(); 		/* IN5 - fake input port for player 2 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP	  | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1		  | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0xe0, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///	
/*TODO*///		PORT_START(); 		/* IN6 - fake input port for player 3 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP	  | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1		  | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0xe0, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///	
/*TODO*///		PORT_START(); 		/* IN7 - fake input port for player 4 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER4 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER4 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER4 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP	  | IPF_PLAYER4 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1		  | IPF_PLAYER4 );
/*TODO*///		PORT_BIT( 0xe0, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/* Beraboh Man input port definition - controls are different */
/*TODO*///	static InputPortPtr input_ports_berabohm = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START(); 		/* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///	
/*TODO*///		PORT_START(); 		/* IN1 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_COCKTAIL );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_COCKTAIL );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_COCKTAIL );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP	  | IPF_COCKTAIL );
/*TODO*///		PORT_BIT( 0x70, IP_ACTIVE_LOW, IPT_SPECIAL );/* timing from the buttons interface */
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///	
/*TODO*///		PORT_START(); 		/* DSW1 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Auto Data Sampling" );
/*TODO*///		PORT_DIPSETTING(	0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "Freeze" );
/*TODO*///		PORT_DIPSETTING(	0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	
/*TODO*///		PORT_START(); 		/* IN2 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_SPECIAL );/* OUT:coin lockout */
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_SPECIAL );/* OUT:coin counter 1 */
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_SPECIAL );/* OUT:coin counter 2 */
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_SERVICE, "Service Button", KEYCODE_F1, IP_JOY_NONE );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_SERVICE1 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///	
/*TODO*///		/*
/*TODO*///		buttons (pressure sensitive)
/*TODO*///		each button has two switches: the first is closed as soon as the button is
/*TODO*///		pressed, the second a little later, depending on how hard the button is
/*TODO*///		pressed.
/*TODO*///		bits 0-5 control strength (0x00 = max 0x3f = min)
/*TODO*///		bit 6 indicates the button is pressed
/*TODO*///		bit 7 is not actually read by the game but I use it to simulate the second
/*TODO*///		      switch
/*TODO*///		*/
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x3f, 0x00, IPT_SPECIAL );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON3 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON4 );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x3f, 0x00, IPT_SPECIAL );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON1 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON2 );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x3f, 0x00, IPT_SPECIAL );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_COCKTAIL );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON4 | IPF_COCKTAIL );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x3f, 0x00, IPT_SPECIAL );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL );
/*TODO*///	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		16384,	/* 16384 characters max */
		1,		/* 1 bit per pixel */
		new int[] { 0 },	/* bitplanes offset */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8 	/* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout tilelayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		16384,	/* 16384 characters max */
		8,		/* 8 bits per pixel */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 }, 	/* bitplanes offset */
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		new int[] { 0*64, 1*64, 2*64, 3*64, 4*64, 5*64, 6*64, 7*64 },
		64*8	/* every char takes 64 consecutive bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		32,32,	/* 32*32 sprites */
		2048,	/* 2048 sprites max */
		4,		/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },  /* the bitplanes are packed */
		new int[] {  0*4,  1*4,  2*4,  3*4,  4*4,  5*4,  6*4,  7*4,
		   8*4,  9*4, 10*4, 11*4, 12*4, 13*4, 14*4, 15*4,
		 256*4,257*4,258*4,259*4,260*4,261*4,262*4,263*4,
		 264*4,265*4,266*4,267*4,268*4,269*4,270*4,271*4},
		new int[] { 0*4*16, 1*4*16,  2*4*16,	3*4*16,  4*4*16,  5*4*16,  6*4*16,	7*4*16,
		  8*4*16, 9*4*16, 10*4*16, 11*4*16, 12*4*16, 13*4*16, 14*4*16, 15*4*16,
		 32*4*16,33*4*16, 34*4*16, 35*4*16, 36*4*16, 37*4*16, 38*4*16, 39*4*16,
		 40*4*16,41*4*16, 42*4*16, 43*4*16, 44*4*16, 45*4*16, 46*4*16, 47*4*16 },
		32*4*8*4  /* every sprite takes 512 consecutive bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,		 0,   1 ),	/* character mask */
		new GfxDecodeInfo( REGION_GFX2, 0, tilelayout,	128*16,   6 ),	/* characters */
		new GfxDecodeInfo( REGION_GFX3, 0, spritelayout,	 0, 128 ),	/* sprites 32/16/8/4 dots */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static WriteYmHandlerPtr namcos1_sound_interrupt = new WriteYmHandlerPtr() {
            @Override
            public void handler(int irq) {
                cpu_set_irq_line( 2, M6809_FIRQ_LINE , irq!=0 ? ASSERT_LINE : CLEAR_LINE);
            }
        };
	
	static YM2151interface ym2151_interface = new YM2151interface
	(
		1,			/* 1 chip */
		3579580,	/* 3.58 MHz */
		new int[] { YM3012_VOL(80,MIXER_PAN_LEFT,80,MIXER_PAN_RIGHT) },
		new WriteYmHandlerPtr[] { namcos1_sound_interrupt },
		new WriteHandlerPtr[] { null }
	);
	
	static namco_interface namco_interface = new namco_interface
	(
		23920/2,	/* sample rate (approximate value) */
		8,			/* number of voices */
		50, 		/* playback volume */
		-1, 		/* memory region */
		1			/* stereo */
	);
	
	/*
		namcos1 has tow 8bit dac channel. But They are mixed before pre-amp.
		And,they are connected with pre-amp through active LPF.
		LFP info : Fco = 3.3KHz , g = -12dB/oct
	*/
	static DACinterface dac_interface = new DACinterface
	(
		1,			/* 2 channel , but they are mixed by the driver */
		new int[]{ 100 	}	/* mixing level */
	);
	
	static MachineDriver machine_driver_ns1 = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6809,
				49152000/32,	/* Not sure if divided by 32 or 24 */
				main_readmem,main_writemem,null,null,
				interrupt,1
			),
			new MachineCPU(
				CPU_M6809,
				49152000/32,	/* Not sure if divided by 32 or 24 */
				sub_readmem,sub_writemem,null,null,
				interrupt,1
			),
			new MachineCPU(
				CPU_M6809,
				49152000/32,	/* Not sure if divided by 32 or 24 */
				sound_readmem,sound_writemem,null,null,
				interrupt,1
			),
			new MachineCPU(
				CPU_HD63701,	/* or compatible 6808 with extra instructions */
				49152000/8/4,
				mcu_readmem,mcu_writemem,mcu_readport,mcu_writeport,
				interrupt,1
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		0, /* CPU slice timer is made by machine_init */
		init_namcos1,
	
		/* video hardware */
		36*8, 28*8, new rectangle( 0*8, 36*8-1, 0*8, 28*8-1 ),
		gfxdecodeinfo,
		128*16+6*256+6*256+1,	/* sprites, tiles, shadowed tiles, background */
			128*16+6*256+1,
/*TODO*///		namcos1_vh_convert_color_prom,
                null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
		null,
		namcos1_vh_start,
		namcos1_vh_stop,
		namcos1_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			),
			new MachineSound(
				SOUND_NAMCO,
				namco_interface
			),
			new MachineSound(
				SOUND_DAC,
				dac_interface
			)
		},
	
		nvram_handler
	);
	
/*TODO*///	static MachineDriver machine_driver_quester = new MachineDriver
/*TODO*///	(
/*TODO*///		/* basic machine hardware */
/*TODO*///		new MachineCPU[] {
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_M6809,
/*TODO*///				49152000/32,	/* Not sure if divided by 32 or 24 */
/*TODO*///				main_readmem,main_writemem,null,null,
/*TODO*///				interrupt,1,
/*TODO*///			),
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_M6809,
/*TODO*///				49152000/32,	/* Not sure if divided by 32 or 24 */
/*TODO*///				sub_readmem,sub_writemem,null,null,
/*TODO*///				interrupt,1,
/*TODO*///			),
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_M6809,
/*TODO*///				49152000/32,	/* Not sure if divided by 32 or 24 */
/*TODO*///				sound_readmem,sound_writemem,null,null,
/*TODO*///				interrupt,1
/*TODO*///			),
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_HD63701,	/* or compatible 6808 with extra instructions */
/*TODO*///				49152000/8/4,
/*TODO*///				quester_mcu_readmem,mcu_writemem,mcu_readport,mcu_writeport,
/*TODO*///				interrupt,1
/*TODO*///			)
/*TODO*///		},
/*TODO*///		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
/*TODO*///		null, /* CPU slice timer is made by machine_init */
/*TODO*///		init_namcos1,
/*TODO*///	
/*TODO*///		/* video hardware */
/*TODO*///		36*8, 28*8, new rectangle( 0*8, 36*8-1, 0*8, 28*8-1 ),
/*TODO*///		gfxdecodeinfo,
/*TODO*///		128*16+6*256+6*256+1,	/* sprites, tiles, shadowed tiles, background */
/*TODO*///			128*16+6*256+1,
/*TODO*///		namcos1_vh_convert_color_prom,
/*TODO*///	
/*TODO*///		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
/*TODO*///		0,
/*TODO*///		namcos1_vh_start,
/*TODO*///		namcos1_vh_stop,
/*TODO*///		namcos1_vh_screenrefresh,
/*TODO*///	
/*TODO*///		/* sound hardware */
/*TODO*///		SOUND_SUPPORTS_STEREO,0,0,0,
/*TODO*///		new MachineSound[] {
/*TODO*///			new MachineSound(
/*TODO*///				SOUND_YM2151,
/*TODO*///				ym2151_interface
/*TODO*///			),
/*TODO*///			new MachineSound(
/*TODO*///				SOUND_NAMCO,
/*TODO*///				namco_interface
/*TODO*///			),
/*TODO*///			new MachineSound(
/*TODO*///				SOUND_DAC,
/*TODO*///				dac_interface
/*TODO*///			)
/*TODO*///		},
/*TODO*///	
/*TODO*///		nvram_handler
/*TODO*///	);
/*TODO*///	
/*TODO*///	static MachineDriver machine_driver_faceoff = new MachineDriver
/*TODO*///	(
/*TODO*///		/* basic machine hardware */
/*TODO*///		new MachineCPU[] {
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_M6809,
/*TODO*///				49152000/32,	/* Not sure if divided by 32 or 24 */
/*TODO*///				main_readmem,main_writemem,null,null,
/*TODO*///				interrupt,1,
/*TODO*///			),
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_M6809,
/*TODO*///				49152000/32,	/* Not sure if divided by 32 or 24 */
/*TODO*///				sub_readmem,sub_writemem,null,null,
/*TODO*///				interrupt,1,
/*TODO*///			),
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_M6809,
/*TODO*///				49152000/32,	/* Not sure if divided by 32 or 24 */
/*TODO*///				sound_readmem,sound_writemem,null,null,
/*TODO*///				interrupt,1
/*TODO*///			),
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_HD63701,	/* or compatible 6808 with extra instructions */
/*TODO*///				49152000/8/4,
/*TODO*///				faceoff_mcu_readmem,mcu_writemem,mcu_readport,mcu_writeport,
/*TODO*///				interrupt,1
/*TODO*///			)
/*TODO*///		},
/*TODO*///		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
/*TODO*///		null, /* CPU slice timer is made by machine_init */
/*TODO*///		init_namcos1,
/*TODO*///	
/*TODO*///		/* video hardware */
/*TODO*///		36*8, 28*8, new rectangle( 0*8, 36*8-1, 0*8, 28*8-1 ),
/*TODO*///		gfxdecodeinfo,
/*TODO*///		128*16+6*256+6*256+1,	/* sprites, tiles, shadowed tiles, background */
/*TODO*///			128*16+6*256+1,
/*TODO*///		namcos1_vh_convert_color_prom,
/*TODO*///	
/*TODO*///		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
/*TODO*///		0,
/*TODO*///		namcos1_vh_start,
/*TODO*///		namcos1_vh_stop,
/*TODO*///		namcos1_vh_screenrefresh,
/*TODO*///	
/*TODO*///		/* sound hardware */
/*TODO*///		SOUND_SUPPORTS_STEREO,0,0,0,
/*TODO*///		new MachineSound[] {
/*TODO*///			new MachineSound(
/*TODO*///				SOUND_YM2151,
/*TODO*///				ym2151_interface
/*TODO*///			),
/*TODO*///			new MachineSound(
/*TODO*///				SOUND_NAMCO,
/*TODO*///				namco_interface
/*TODO*///			),
/*TODO*///			new MachineSound(
/*TODO*///				SOUND_DAC,
/*TODO*///				dac_interface
/*TODO*///			)
/*TODO*///		},
/*TODO*///	
/*TODO*///		nvram_handler
/*TODO*///	);
	
	
	/***********************************************************************
	
	  Game drivers
	
	***********************************************************************/
	/* load half size ROM to full size space */
	static void ROM_LOAD_HS(String name, int start, int length, int crc){
		ROM_LOAD(name,start,length,crc);
		ROM_RELOAD(start+length,length);
        }
	
	/* Shadowland */
	static RomLoadPtr rom_shadowld = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
	
		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
	
		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
		ROM_LOAD( "yd1.sd0",			0x0c000, 0x10000, 0xa9cb51fb );
		ROM_LOAD( "yd1.sd1",			0x1c000, 0x10000, 0x65d1dc0d );
	
		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
		ROM_LOAD_HS( "yd3.p7",			0x00000, 0x10000, 0xf1c271a0 );
		ROM_LOAD_HS( "yd3.p6",			0x20000, 0x10000, 0x93d6811c );
		ROM_LOAD_HS( "yd1.p5",			0x40000, 0x10000, 0x29a78bd6 );
		ROM_LOAD_HS( "yd1.p3",			0x80000, 0x10000, 0xa4f27c24 );
		ROM_LOAD_HS( "yd1.p2",			0xa0000, 0x10000, 0x62e5bbec );
		ROM_LOAD_HS( "yd1.p1",			0xc0000, 0x10000, 0xa8ea6bd3 );
		ROM_LOAD_HS( "yd1.p0",			0xe0000, 0x10000, 0x07e49883 );
	
		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
		/* 0x00000 - 0x08000 = RAM6 ( 4 * 8k ) */
		/* 0x08000 - 0x0c000 = RAM1 ( 2 * 8k ) */
		/* 0x0c000 - 0x14000 = RAM3 ( 4 * 8k ) */
	
		ROM_REGION( 0xd0000, REGION_CPU4 );	/* the MCU & voice */
		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
		ROM_LOAD_HS( "yd1.v0",			0x10000, 0x10000, 0xcde1ee23 );
		ROM_LOAD_HS( "yd1.v1",			0x30000, 0x10000, 0xc61f462b );
		ROM_LOAD_HS( "yd1.v2",			0x50000, 0x10000, 0x821ad462 );
		ROM_LOAD_HS( "yd1.v3",			0x70000, 0x10000, 0x1e003489 );
		ROM_LOAD_HS( "yd1.v4",			0x90000, 0x10000, 0xa106e6f6 );
		ROM_LOAD_HS( "yd1.v5",			0xb0000, 0x10000, 0xde72f38f );
	
		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
		ROM_LOAD( "yd.ch8", 			0x00000, 0x20000, 0x0c8e69d0 );
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
		ROM_LOAD( "yd.ch0", 			0x00000, 0x20000, 0x717441dd );
		ROM_LOAD( "yd.ch1", 			0x20000, 0x20000, 0xc1be6e35 );
		ROM_LOAD( "yd.ch2", 			0x40000, 0x20000, 0x2df8d8cc );
		ROM_LOAD( "yd.ch3", 			0x60000, 0x20000, 0xd4e15c9e );
		ROM_LOAD( "yd.ch4", 			0x80000, 0x20000, 0xc0041e0d );
		ROM_LOAD( "yd.ch5", 			0xa0000, 0x20000, 0x7b368461 );
		ROM_LOAD( "yd.ch6", 			0xc0000, 0x20000, 0x3ac6a90e );
		ROM_LOAD( "yd.ch7", 			0xe0000, 0x20000, 0x8d2cffa5 );
	
		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
		ROM_LOAD( "yd.ob0", 			0x00000, 0x20000, 0xefb8efe3 );
		ROM_LOAD( "yd.ob1", 			0x20000, 0x20000, 0xbf4ee682 );
		ROM_LOAD( "yd.ob2", 			0x40000, 0x20000, 0xcb721682 );
		ROM_LOAD( "yd.ob3", 			0x60000, 0x20000, 0x8a6c3d1c );
		ROM_LOAD( "yd.ob4", 			0x80000, 0x20000, 0xef97bffb );
		ROM_LOAD_HS( "yd3.ob5", 		0xa0000, 0x10000, 0x1e4aa460 );
	ROM_END(); }}; 
	
/*TODO*///	/* Youkai Douchuuki (Shadowland Japan) */
/*TODO*///	static RomLoadPtr rom_youkaidk = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "yd1.sd0",			0x0c000, 0x10000, 0xa9cb51fb );
/*TODO*///		ROM_LOAD( "yd1.sd1",			0x1c000, 0x10000, 0x65d1dc0d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD_HS( "yd2prg7b.bin",	0x00000, 0x10000, 0xa05bf3ae )
/*TODO*///		ROM_LOAD_HS( "yd1-prg6.bin",	0x20000, 0x10000, 0x785a2772 )
/*TODO*///		ROM_LOAD_HS( "yd1.p5",			0x40000, 0x10000, 0x29a78bd6 )
/*TODO*///		ROM_LOAD_HS( "yd1.p3",			0x80000, 0x10000, 0xa4f27c24 )
/*TODO*///		ROM_LOAD_HS( "yd1.p2",			0xa0000, 0x10000, 0x62e5bbec )
/*TODO*///		ROM_LOAD_HS( "yd1.p1",			0xc0000, 0x10000, 0xa8ea6bd3 )
/*TODO*///		ROM_LOAD_HS( "yd1.p0",			0xe0000, 0x10000, 0x07e49883 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0xd0000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD_HS( "yd1.v0",			0x10000, 0x10000, 0xcde1ee23 )
/*TODO*///		ROM_LOAD_HS( "yd1.v1",			0x30000, 0x10000, 0xc61f462b )
/*TODO*///		ROM_LOAD_HS( "yd1.v2",			0x50000, 0x10000, 0x821ad462 )
/*TODO*///		ROM_LOAD_HS( "yd1.v3",			0x70000, 0x10000, 0x1e003489 )
/*TODO*///		ROM_LOAD_HS( "yd1.v4",			0x90000, 0x10000, 0xa106e6f6 )
/*TODO*///		ROM_LOAD_HS( "yd1.v5",			0xb0000, 0x10000, 0xde72f38f )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "yd.ch8", 			0x00000, 0x20000, 0x0c8e69d0 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "yd.ch0", 			0x00000, 0x20000, 0x717441dd );
/*TODO*///		ROM_LOAD( "yd.ch1", 			0x20000, 0x20000, 0xc1be6e35 );
/*TODO*///		ROM_LOAD( "yd.ch2", 			0x40000, 0x20000, 0x2df8d8cc );
/*TODO*///		ROM_LOAD( "yd.ch3", 			0x60000, 0x20000, 0xd4e15c9e );
/*TODO*///		ROM_LOAD( "yd.ch4", 			0x80000, 0x20000, 0xc0041e0d );
/*TODO*///		ROM_LOAD( "yd.ch5", 			0xa0000, 0x20000, 0x7b368461 );
/*TODO*///		ROM_LOAD( "yd.ch6", 			0xc0000, 0x20000, 0x3ac6a90e );
/*TODO*///		ROM_LOAD( "yd.ch7", 			0xe0000, 0x20000, 0x8d2cffa5 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "yd.ob0", 			0x00000, 0x20000, 0xefb8efe3 );
/*TODO*///		ROM_LOAD( "yd.ob1", 			0x20000, 0x20000, 0xbf4ee682 );
/*TODO*///		ROM_LOAD( "yd.ob2", 			0x40000, 0x20000, 0xcb721682 );
/*TODO*///		ROM_LOAD( "yd.ob3", 			0x60000, 0x20000, 0x8a6c3d1c );
/*TODO*///		ROM_LOAD( "yd.ob4", 			0x80000, 0x20000, 0xef97bffb );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* Youkai Douchuuki (Shadowland Japan old version) */
/*TODO*///	static RomLoadPtr rom_yokaidko = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "yd1.sd0",			0x0c000, 0x10000, 0xa9cb51fb );
/*TODO*///		ROM_LOAD( "yd1.sd1",			0x1c000, 0x10000, 0x65d1dc0d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD_HS( "yd2_p7.bin",		0x00000, 0x10000, 0x3d39098c )
/*TODO*///		ROM_LOAD_HS( "yd1-prg6.bin",	0x20000, 0x10000, 0x785a2772 )
/*TODO*///		ROM_LOAD_HS( "yd1.p5",			0x40000, 0x10000, 0x29a78bd6 )
/*TODO*///		ROM_LOAD_HS( "yd1.p3",			0x80000, 0x10000, 0xa4f27c24 )
/*TODO*///		ROM_LOAD_HS( "yd1.p2",			0xa0000, 0x10000, 0x62e5bbec )
/*TODO*///		ROM_LOAD_HS( "yd1.p1",			0xc0000, 0x10000, 0xa8ea6bd3 )
/*TODO*///		ROM_LOAD_HS( "yd1.p0",			0xe0000, 0x10000, 0x07e49883 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0xd0000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD_HS( "yd1.v0",			0x10000, 0x10000, 0xcde1ee23 )
/*TODO*///		ROM_LOAD_HS( "yd1.v1",			0x30000, 0x10000, 0xc61f462b )
/*TODO*///		ROM_LOAD_HS( "yd1.v2",			0x50000, 0x10000, 0x821ad462 )
/*TODO*///		ROM_LOAD_HS( "yd1.v3",			0x70000, 0x10000, 0x1e003489 )
/*TODO*///		ROM_LOAD_HS( "yd1.v4",			0x90000, 0x10000, 0xa106e6f6 )
/*TODO*///		ROM_LOAD_HS( "yd1.v5",			0xb0000, 0x10000, 0xde72f38f )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "yd.ch8", 			0x00000, 0x20000, 0x0c8e69d0 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "yd.ch0", 			0x00000, 0x20000, 0x717441dd );
/*TODO*///		ROM_LOAD( "yd.ch1", 			0x20000, 0x20000, 0xc1be6e35 );
/*TODO*///		ROM_LOAD( "yd.ch2", 			0x40000, 0x20000, 0x2df8d8cc );
/*TODO*///		ROM_LOAD( "yd.ch3", 			0x60000, 0x20000, 0xd4e15c9e );
/*TODO*///		ROM_LOAD( "yd.ch4", 			0x80000, 0x20000, 0xc0041e0d );
/*TODO*///		ROM_LOAD( "yd.ch5", 			0xa0000, 0x20000, 0x7b368461 );
/*TODO*///		ROM_LOAD( "yd.ch6", 			0xc0000, 0x20000, 0x3ac6a90e );
/*TODO*///		ROM_LOAD( "yd.ch7", 			0xe0000, 0x20000, 0x8d2cffa5 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "yd.ob0", 			0x00000, 0x20000, 0xefb8efe3 );
/*TODO*///		ROM_LOAD( "yd.ob1", 			0x20000, 0x20000, 0xbf4ee682 );
/*TODO*///		ROM_LOAD( "yd.ob2", 			0x40000, 0x20000, 0xcb721682 );
/*TODO*///		ROM_LOAD( "yd.ob3", 			0x60000, 0x20000, 0x8a6c3d1c );
/*TODO*///		ROM_LOAD( "yd.ob4", 			0x80000, 0x20000, 0xef97bffb );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* Dragon Spirit */
/*TODO*///	static RomLoadPtr rom_dspirit = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "dssnd-0.bin",		0x0c000, 0x10000, 0x27100065 );
/*TODO*///		ROM_LOAD( "dssnd-1.bin",		0x1c000, 0x10000, 0xb398645f );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD_HS( "ds3-p7.bin",		0x00000, 0x10000, 0x820bedb2 )
/*TODO*///		ROM_LOAD_HS( "ds3-p6.bin",		0x20000, 0x10000, 0xfcc01bd1 )
/*TODO*///		ROM_LOAD_HS( "dsprg-5.bin", 	0x40000, 0x10000, 0x9a3a1028 )
/*TODO*///		ROM_LOAD_HS( "dsprg-4.bin", 	0x60000, 0x10000, 0xf3307870 )
/*TODO*///		ROM_LOAD_HS( "dsprg-3.bin", 	0x80000, 0x10000, 0xc6e5954b )
/*TODO*///		ROM_LOAD_HS( "dsprg-2.bin", 	0xa0000, 0x10000, 0x3c9b0100 )
/*TODO*///		ROM_LOAD_HS( "dsprg-1.bin", 	0xc0000, 0x10000, 0xf7e3298a )
/*TODO*///		ROM_LOAD_HS( "dsprg-0.bin", 	0xe0000, 0x10000, 0xb22a2856 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0xb0000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD_HS( "dsvoi-0.bin", 	0x10000, 0x10000, 0x313b3508 )
/*TODO*///		ROM_LOAD( "dsvoi-1.bin",		0x30000, 0x20000, 0x54790d4e );
/*TODO*///		ROM_LOAD( "dsvoi-2.bin",		0x50000, 0x20000, 0x05298534 );
/*TODO*///		ROM_LOAD( "dsvoi-3.bin",		0x70000, 0x20000, 0x13e84c7e );
/*TODO*///		ROM_LOAD( "dsvoi-4.bin",		0x90000, 0x20000, 0x34fbb8cd );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "dschr-8.bin",		0x00000, 0x20000, 0x946eb242 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "dschr-0.bin",		0x00000, 0x20000, 0x7bf28ac3 );
/*TODO*///		ROM_LOAD( "dschr-1.bin",		0x20000, 0x20000, 0x03582fea );
/*TODO*///		ROM_LOAD( "dschr-2.bin",		0x40000, 0x20000, 0x5e05f4f9 );
/*TODO*///		ROM_LOAD( "dschr-3.bin",		0x60000, 0x20000, 0xdc540791 );
/*TODO*///		ROM_LOAD( "dschr-4.bin",		0x80000, 0x20000, 0xffd1f35c );
/*TODO*///		ROM_LOAD( "dschr-5.bin",		0xa0000, 0x20000, 0x8472e0a3 );
/*TODO*///		ROM_LOAD( "dschr-6.bin",		0xc0000, 0x20000, 0xa799665a );
/*TODO*///		ROM_LOAD( "dschr-7.bin",		0xe0000, 0x20000, 0xa51724af );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "dsobj-0.bin",		0x00000, 0x20000, 0x03ec3076 );
/*TODO*///		ROM_LOAD( "dsobj-1.bin",		0x20000, 0x20000, 0xe67a8fa4 );
/*TODO*///		ROM_LOAD( "dsobj-2.bin",		0x40000, 0x20000, 0x061cd763 );
/*TODO*///		ROM_LOAD( "dsobj-3.bin",		0x60000, 0x20000, 0x63225a09 );
/*TODO*///		ROM_LOAD_HS( "dsobj-4.bin", 	0x80000, 0x10000, 0xa6246fcb )
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* Dragon Spirit (old version) */
/*TODO*///	static RomLoadPtr rom_dspirito = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "dssnd-0.bin",		0x0c000, 0x10000, 0x27100065 );
/*TODO*///		ROM_LOAD( "dssnd-1.bin",		0x1c000, 0x10000, 0xb398645f );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD_HS( "dsprg-7.bin", 	0x00000, 0x10000, 0xf4c0d75e )
/*TODO*///		ROM_LOAD_HS( "dsprg-6.bin", 	0x20000, 0x10000, 0xa82737b4 )
/*TODO*///		ROM_LOAD_HS( "dsprg-5.bin", 	0x40000, 0x10000, 0x9a3a1028 )
/*TODO*///		ROM_LOAD_HS( "dsprg-4.bin", 	0x60000, 0x10000, 0xf3307870 )
/*TODO*///		ROM_LOAD_HS( "dsprg-3.bin", 	0x80000, 0x10000, 0xc6e5954b )
/*TODO*///		ROM_LOAD_HS( "dsprg-2.bin", 	0xa0000, 0x10000, 0x3c9b0100 )
/*TODO*///		ROM_LOAD_HS( "dsprg-1.bin", 	0xc0000, 0x10000, 0xf7e3298a )
/*TODO*///		ROM_LOAD_HS( "dsprg-0.bin", 	0xe0000, 0x10000, 0xb22a2856 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0xb0000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD_HS( "dsvoi-0.bin", 	0x10000, 0x10000, 0x313b3508 )
/*TODO*///		ROM_LOAD( "dsvoi-1.bin",		0x30000, 0x20000, 0x54790d4e );
/*TODO*///		ROM_LOAD( "dsvoi-2.bin",		0x50000, 0x20000, 0x05298534 );
/*TODO*///		ROM_LOAD( "dsvoi-3.bin",		0x70000, 0x20000, 0x13e84c7e );
/*TODO*///		ROM_LOAD( "dsvoi-4.bin",		0x90000, 0x20000, 0x34fbb8cd );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "dschr-8.bin",		0x00000, 0x20000, 0x946eb242 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "dschr-0.bin",		0x00000, 0x20000, 0x7bf28ac3 );
/*TODO*///		ROM_LOAD( "dschr-1.bin",		0x20000, 0x20000, 0x03582fea );
/*TODO*///		ROM_LOAD( "dschr-2.bin",		0x40000, 0x20000, 0x5e05f4f9 );
/*TODO*///		ROM_LOAD( "dschr-3.bin",		0x60000, 0x20000, 0xdc540791 );
/*TODO*///		ROM_LOAD( "dschr-4.bin",		0x80000, 0x20000, 0xffd1f35c );
/*TODO*///		ROM_LOAD( "dschr-5.bin",		0xa0000, 0x20000, 0x8472e0a3 );
/*TODO*///		ROM_LOAD( "dschr-6.bin",		0xc0000, 0x20000, 0xa799665a );
/*TODO*///		ROM_LOAD( "dschr-7.bin",		0xe0000, 0x20000, 0xa51724af );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "dsobj-0.bin",		0x00000, 0x20000, 0x03ec3076 );
/*TODO*///		ROM_LOAD( "dsobj-1.bin",		0x20000, 0x20000, 0xe67a8fa4 );
/*TODO*///		ROM_LOAD( "dsobj-2.bin",		0x40000, 0x20000, 0x061cd763 );
/*TODO*///		ROM_LOAD( "dsobj-3.bin",		0x60000, 0x20000, 0x63225a09 );
/*TODO*///		ROM_LOAD_HS( "dsobj-4.bin", 	0x80000, 0x10000, 0xa6246fcb )
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* Blazer */
/*TODO*///	static RomLoadPtr rom_blazer = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x1c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "bz1_snd0.bin",		0x0c000, 0x10000, 0x6c3a580b );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD_HS( "bz1_prg7.bin",	0x00000, 0x10000, 0x2d4cbb95 )
/*TODO*///		ROM_LOAD( "bz1_prg6.bin",		0x20000, 0x20000, 0x81c48fc0 );
/*TODO*///		ROM_LOAD( "bz1_prg5.bin",		0x40000, 0x20000, 0x900da191 );
/*TODO*///		ROM_LOAD( "bz1_prg4.bin",		0x60000, 0x20000, 0x65ef6f05 );
/*TODO*///		ROM_LOAD_HS( "bz1_prg3.bin",	0x80000, 0x10000, 0x81b32a1a )
/*TODO*///		ROM_LOAD_HS( "bz1_prg2.bin",	0xa0000, 0x10000, 0x5d700aed )
/*TODO*///		ROM_LOAD_HS( "bz1_prg1.bin",	0xc0000, 0x10000, 0xc54bbbf4 )
/*TODO*///		ROM_LOAD_HS( "bz1_prg0.bin",	0xe0000, 0x10000, 0xa7dd195b )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0xb0000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD_HS( "bz1_voi0.bin",	0x10000, 0x10000, 0x3d09d32e )
/*TODO*///		ROM_LOAD( "bz1_voi1.bin",		0x30000, 0x20000, 0x2043b141 );
/*TODO*///		ROM_LOAD( "bz1_voi2.bin",		0x50000, 0x20000, 0x64143442 );
/*TODO*///		ROM_LOAD( "bz1_voi3.bin",		0x70000, 0x20000, 0x26cfc510 );
/*TODO*///		ROM_LOAD( "bz1_voi4.bin",		0x90000, 0x20000, 0xd206b1bd );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "bz1_chr8.bin",		0x00000, 0x20000, 0xdb28bfca );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "bz1_chr0.bin",		0x00000, 0x20000, 0xd346ba61 );
/*TODO*///		ROM_LOAD( "bz1_chr1.bin",		0x20000, 0x20000, 0xe45eb2ea );
/*TODO*///		ROM_LOAD( "bz1_chr2.bin",		0x40000, 0x20000, 0x599079ee );
/*TODO*///		ROM_LOAD( "bz1_chr3.bin",		0x60000, 0x20000, 0xd5182e36 );
/*TODO*///		ROM_LOAD( "bz1_chr4.bin",		0x80000, 0x20000, 0xe788259e );
/*TODO*///		ROM_LOAD( "bz1_chr5.bin",		0xa0000, 0x20000, 0x107e6814 );
/*TODO*///		ROM_LOAD( "bz1_chr6.bin",		0xc0000, 0x20000, 0x0312e2ba );
/*TODO*///		ROM_LOAD( "bz1_chr7.bin",		0xe0000, 0x20000, 0xd9d9a90f );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "bz1_obj0.bin",		0x00000, 0x20000, 0x22aee927 );
/*TODO*///		ROM_LOAD( "bz1_obj1.bin",		0x20000, 0x20000, 0x7cb10112 );
/*TODO*///		ROM_LOAD( "bz1_obj2.bin",		0x40000, 0x20000, 0x34b23bb7 );
/*TODO*///		ROM_LOAD( "bz1_obj3.bin",		0x60000, 0x20000, 0x9bc1db71 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* Quester */
/*TODO*///	static RomLoadPtr rom_quester = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "qs1_s0.bin", 		0x0c000, 0x10000, 0xc2ef3af9 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD_HS( "qs1_p7b.bin", 	0x00000, 0x10000, 0xf358a944 )
/*TODO*///		ROM_LOAD( "qs1_p5.bin", 		0x40000, 0x10000, 0xc8e11f30 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD_HS( "qs1_v0.bin",		0x10000, 0x10000, 0x6a2f3038 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "qs1_c8.bin", 		0x00000, 0x10000, 0x06730d54 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "qs1_c0.bin", 		0x00000, 0x20000, 0xca69bd7a );
/*TODO*///		ROM_LOAD( "qs1_c1.bin", 		0x20000, 0x20000, 0xd660ba71 );
/*TODO*///		ROM_LOAD( "qs1_c2.bin", 		0x40000, 0x20000, 0x4686f656 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "qs1_o0.bin", 		0x00000, 0x10000, 0xe24f0bf1 );
/*TODO*///		ROM_LOAD( "qs1_o1.bin", 		0x20000, 0x10000, 0xe4aab0ca );
/*TODO*///	ROM_END(); }}; 
	
	/* Pac-Mania */
	static RomLoadPtr rom_pacmania = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
	
		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
	
		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
		ROM_LOAD( "pm_snd0.bin",		0x0c000, 0x10000, 0xc10370fa );
		ROM_LOAD( "pm_snd1.bin",		0x1c000, 0x10000, 0xf761ed5a );
	
		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
		ROM_LOAD_HS( "pm_prg7.bin", 	0x00000, 0x10000, 0x462fa4fd );
		ROM_LOAD( "pm_prg6.bin",		0x20000, 0x20000, 0xfe94900c );
	
		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
	
		ROM_REGION( 0x30000, REGION_CPU4 );	/* the MCU & voice */
		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
		ROM_LOAD_HS( "pm_voice.bin",	0x10000, 0x10000, 0x1ad5788f );
	
		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
		ROM_LOAD( "pm_chr8.bin",		0x00000, 0x10000, 0xf3afd65d );
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
		ROM_LOAD( "pm_chr0.bin",		0x00000, 0x20000, 0x7c57644c );
		ROM_LOAD( "pm_chr1.bin",		0x20000, 0x20000, 0x7eaa67ed );
		ROM_LOAD( "pm_chr2.bin",		0x40000, 0x20000, 0x27e739ac );
		ROM_LOAD( "pm_chr3.bin",		0x60000, 0x20000, 0x1dfda293 );
	
		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
		ROM_LOAD( "pm_obj0.bin",		0x00000, 0x20000, 0xfda57e8b );
		ROM_LOAD( "pm_obj1.bin",		0x20000, 0x20000, 0x4c08affe );
	ROM_END(); }}; 
	
/*TODO*///	/* Pac-Mania (Japan) diff o1,s0,s1,p7,v0 */
/*TODO*///	static RomLoadPtr rom_pacmanij = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "pm-s0.a10",			0x0c000, 0x10000, 0xd5ef5eee );
/*TODO*///		ROM_LOAD( "pm-s1.b10",			0x1c000, 0x10000, 0x411bc134 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD_HS( "pm-p7.t10",		0x00000, 0x10000, 0x2aa99e2b )
/*TODO*///		ROM_LOAD( "pm_prg6.bin",		0x20000, 0x20000, 0xfe94900c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD_HS( "pm-v0.b5",		0x10000, 0x10000, 0xe2689f79 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "pm_chr8.bin",		0x00000, 0x10000, 0xf3afd65d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "pm_chr0.bin",		0x00000, 0x20000, 0x7c57644c );
/*TODO*///		ROM_LOAD( "pm_chr1.bin",		0x20000, 0x20000, 0x7eaa67ed );
/*TODO*///		ROM_LOAD( "pm_chr2.bin",		0x40000, 0x20000, 0x27e739ac );
/*TODO*///		ROM_LOAD( "pm_chr3.bin",		0x60000, 0x20000, 0x1dfda293 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "pm_obj0.bin",		0x00000, 0x20000, 0xfda57e8b );
/*TODO*///		ROM_LOAD( "pm-01.b9",			0x20000, 0x20000, 0x27bdf440 );
/*TODO*///	ROM_END(); }}; 
	
	/* Galaga '88 */
	static RomLoadPtr rom_galaga88 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
	
		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
	
		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
		ROM_LOAD( "g88_snd0.rom",		0x0c000, 0x10000, 0x164a3fdc );
		ROM_LOAD( "g88_snd1.rom",		0x1c000, 0x10000, 0x16a4b784 );
	
		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
		ROM_LOAD_HS( "g88_prg7.rom",	0x00000, 0x10000, 0xdf75b7fc );
		ROM_LOAD_HS( "g88_prg6.rom",	0x20000, 0x10000, 0x7e3471d3 );
		ROM_LOAD_HS( "g88_prg5.rom",	0x40000, 0x10000, 0x4fbd3f6c );
		ROM_LOAD_HS( "g88_prg1.rom",	0xc0000, 0x10000, 0xe68cb351 );
		ROM_LOAD_HS( "g88_prg0.rom",	0xe0000, 0x10000, 0x0f0778ca );
	
		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
	
		ROM_REGION( 0xd0000, REGION_CPU4 );	/* the MCU & voice */
		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
		ROM_LOAD_HS( "g88_vce0.rom",	0x10000, 0x10000, 0x86921dd4 );
		ROM_LOAD_HS( "g88_vce1.rom",	0x30000, 0x10000, 0x9c300e16 );
		ROM_LOAD_HS( "g88_vce2.rom",	0x50000, 0x10000, 0x5316b4b0 );
		ROM_LOAD_HS( "g88_vce3.rom",	0x70000, 0x10000, 0xdc077af4 );
		ROM_LOAD_HS( "g88_vce4.rom",	0x90000, 0x10000, 0xac0279a7 );
		ROM_LOAD_HS( "g88_vce5.rom",	0xb0000, 0x10000, 0x014ddba1 );
	
		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
		ROM_LOAD( "g88_chr8.rom",		0x00000, 0x20000, 0x3862ed0a );
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
		ROM_LOAD( "g88_chr0.rom",		0x00000, 0x20000, 0x68559c78 );
		ROM_LOAD( "g88_chr1.rom",		0x20000, 0x20000, 0x3dc0f93f );
		ROM_LOAD( "g88_chr2.rom",		0x40000, 0x20000, 0xdbf26f1f );
		ROM_LOAD( "g88_chr3.rom",		0x60000, 0x20000, 0xf5d6cac5 );
	
		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
		ROM_LOAD( "g88_obj0.rom",		0x00000, 0x20000, 0xd7112e3f );
		ROM_LOAD( "g88_obj1.rom",		0x20000, 0x20000, 0x680db8e7 );
		ROM_LOAD( "g88_obj2.rom",		0x40000, 0x20000, 0x13c97512 );
		ROM_LOAD( "g88_obj3.rom",		0x60000, 0x20000, 0x3ed3941b );
		ROM_LOAD( "g88_obj4.rom",		0x80000, 0x20000, 0x370ff4ad );
		ROM_LOAD( "g88_obj5.rom",		0xa0000, 0x20000, 0xb0645169 );
	ROM_END(); }}; 
	
/*TODO*///	static RomLoadPtr rom_galag88b = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "g88_snd0.rom",		0x0c000, 0x10000, 0x164a3fdc );
/*TODO*///		ROM_LOAD( "g88_snd1.rom",		0x1c000, 0x10000, 0x16a4b784 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD_HS( "g88_prg7.rom",	0x00000, 0x10000, 0xdf75b7fc )
/*TODO*///		ROM_LOAD_HS( "prg6",			0x20000, 0x10000, 0x403d01c1 )
/*TODO*///		ROM_LOAD_HS( "g88_prg5.rom",	0x40000, 0x10000, 0x4fbd3f6c )
/*TODO*///		ROM_LOAD_HS( "g88_prg1.rom",	0xc0000, 0x10000, 0xe68cb351 )
/*TODO*///		ROM_LOAD_HS( "g88_prg0.rom",	0xe0000, 0x10000, 0x0f0778ca )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0xd0000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD_HS( "g88_vce0.rom",	0x10000, 0x10000, 0x86921dd4 )
/*TODO*///		ROM_LOAD_HS( "g88_vce1.rom",	0x30000, 0x10000, 0x9c300e16 )
/*TODO*///		ROM_LOAD_HS( "g88_vce2.rom",	0x50000, 0x10000, 0x5316b4b0 )
/*TODO*///		ROM_LOAD_HS( "g88_vce3.rom",	0x70000, 0x10000, 0xdc077af4 )
/*TODO*///		ROM_LOAD_HS( "g88_vce4.rom",	0x90000, 0x10000, 0xac0279a7 )
/*TODO*///		ROM_LOAD_HS( "g88_vce5.rom",	0xb0000, 0x10000, 0x014ddba1 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "g88_chr8.rom",		0x00000, 0x20000, 0x3862ed0a );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "g88_chr0.rom",		0x00000, 0x20000, 0x68559c78 );
/*TODO*///		ROM_LOAD( "g88_chr1.rom",		0x20000, 0x20000, 0x3dc0f93f );
/*TODO*///		ROM_LOAD( "g88_chr2.rom",		0x40000, 0x20000, 0xdbf26f1f );
/*TODO*///		ROM_LOAD( "g88_chr3.rom",		0x60000, 0x20000, 0xf5d6cac5 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "g88_obj0.rom",		0x00000, 0x20000, 0xd7112e3f );
/*TODO*///		ROM_LOAD( "g88_obj1.rom",		0x20000, 0x20000, 0x680db8e7 );
/*TODO*///		ROM_LOAD( "g88_obj2.rom",		0x40000, 0x20000, 0x13c97512 );
/*TODO*///		ROM_LOAD( "g88_obj3.rom",		0x60000, 0x20000, 0x3ed3941b );
/*TODO*///		ROM_LOAD( "g88_obj4.rom",		0x80000, 0x20000, 0x370ff4ad );
/*TODO*///		ROM_LOAD( "g88_obj5.rom",		0xa0000, 0x20000, 0xb0645169 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* Galaga '88 (Japan) */
/*TODO*///	static RomLoadPtr rom_galag88j = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "g88_snd0.rom",		0x0c000, 0x10000, 0x164a3fdc );
/*TODO*///		ROM_LOAD( "g88_snd1.rom",		0x1c000, 0x10000, 0x16a4b784 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD_HS( "g88jprg7.rom",	0x00000, 0x10000, 0x7c10965d )
/*TODO*///		ROM_LOAD_HS( "g88jprg6.rom",	0x20000, 0x10000, 0xe7203707 )
/*TODO*///		ROM_LOAD_HS( "g88_prg5.rom",	0x40000, 0x10000, 0x4fbd3f6c )
/*TODO*///		ROM_LOAD_HS( "g88_prg1.rom",	0xc0000, 0x10000, 0xe68cb351 )
/*TODO*///		ROM_LOAD_HS( "g88_prg0.rom",	0xe0000, 0x10000, 0x0f0778ca )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0xd0000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD_HS( "g88_vce0.rom",	0x10000, 0x10000, 0x86921dd4 )
/*TODO*///		ROM_LOAD_HS( "g88_vce1.rom",	0x30000, 0x10000, 0x9c300e16 )
/*TODO*///		ROM_LOAD_HS( "g88_vce2.rom",	0x50000, 0x10000, 0x5316b4b0 )
/*TODO*///		ROM_LOAD_HS( "g88_vce3.rom",	0x70000, 0x10000, 0xdc077af4 )
/*TODO*///		ROM_LOAD_HS( "g88_vce4.rom",	0x90000, 0x10000, 0xac0279a7 )
/*TODO*///		ROM_LOAD_HS( "g88_vce5.rom",	0xb0000, 0x10000, 0x014ddba1 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "g88_chr8.rom",		0x00000, 0x20000, 0x3862ed0a );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "g88_chr0.rom",		0x00000, 0x20000, 0x68559c78 );
/*TODO*///		ROM_LOAD( "g88_chr1.rom",		0x20000, 0x20000, 0x3dc0f93f );
/*TODO*///		ROM_LOAD( "g88_chr2.rom",		0x40000, 0x20000, 0xdbf26f1f );
/*TODO*///		ROM_LOAD( "g88_chr3.rom",		0x60000, 0x20000, 0xf5d6cac5 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "g88_obj0.rom",		0x00000, 0x20000, 0xd7112e3f );
/*TODO*///		ROM_LOAD( "g88_obj1.rom",		0x20000, 0x20000, 0x680db8e7 );
/*TODO*///		ROM_LOAD( "g88_obj2.rom",		0x40000, 0x20000, 0x13c97512 );
/*TODO*///		ROM_LOAD( "g88_obj3.rom",		0x60000, 0x20000, 0x3ed3941b );
/*TODO*///		ROM_LOAD( "g88_obj4.rom",		0x80000, 0x20000, 0x370ff4ad );
/*TODO*///		ROM_LOAD( "g88_obj5.rom",		0xa0000, 0x20000, 0xb0645169 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* World Stadium */
/*TODO*///	static RomLoadPtr rom_ws = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "ws1-s0.bin", 		0x0c000, 0x10000, 0x45a87810 );
/*TODO*///		ROM_LOAD( "ws1-s1.bin", 		0x1c000, 0x10000, 0x31bf74c1 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD_HS( "ws1-p7.bin",		0x00000, 0x10000, 0x28712eba )
/*TODO*///		ROM_LOAD_HS( "ws1-p2.bin",		0xa0000, 0x10000, 0xbb09fa9b )
/*TODO*///		ROM_LOAD_HS( "ws1-p1.bin",		0xc0000, 0x10000, 0xdfd72bed )
/*TODO*///		ROM_LOAD_HS( "ws1-p0.bin",		0xe0000, 0x10000, 0xb0234298 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD_HS( "ws1-v0.bin",		0x10000, 0x10000, 0xf6949199 )
/*TODO*///		ROM_LOAD( "ws1-v1.bin", 		0x30000, 0x20000, 0x210e2af9 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "ws1-c8.bin", 		0x00000, 0x20000, 0xd1897b9b );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "ws1-c0.bin", 		0x00000, 0x20000, 0x3e3e96b4 );
/*TODO*///		ROM_LOAD( "ws1-c1.bin", 		0x20000, 0x20000, 0x897dfbc1 );
/*TODO*///		ROM_LOAD( "ws1-c2.bin", 		0x40000, 0x20000, 0xe142527c );
/*TODO*///		ROM_LOAD( "ws1-c3.bin", 		0x60000, 0x20000, 0x907d4dc8 );
/*TODO*///		ROM_LOAD( "ws1-c4.bin", 		0x80000, 0x20000, 0xafb11e17 );
/*TODO*///		ROM_LOAD( "ws1-c6.bin", 		0xc0000, 0x20000, 0xa16a17c2 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "ws1-o0.bin", 		0x00000, 0x20000, 0x12dc83a6 );
/*TODO*///		ROM_LOAD( "ws1-o1.bin", 		0x20000, 0x20000, 0x68290a46 );
/*TODO*///		ROM_LOAD( "ws1-o2.bin", 		0x40000, 0x20000, 0xcd5ba55d );
/*TODO*///		ROM_LOAD_HS( "ws1-o3.bin",		0x60000, 0x10000, 0xf2ed5309 )
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* Beraboh Man */
/*TODO*///	static RomLoadPtr rom_berabohm = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "bm1-snd0.bin",		0x0c000, 0x10000, 0xd5d53cb1 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD( "bm1prg7b.bin",		0x10000, 0x10000, 0xe0c36ddd );
/*TODO*///		ROM_CONTINUE(					0x00000, 0x10000 );
/*TODO*///		ROM_LOAD_HS( "bm1-prg6.bin",	0x20000, 0x10000, 0xa51b69a5 )
/*TODO*///		ROM_LOAD( "bm1-prg4.bin",		0x60000, 0x20000, 0xf6cfcb8c );
/*TODO*///		ROM_LOAD( "bm1-prg1.bin",		0xc0000, 0x20000, 0xb15f6407 );
/*TODO*///		ROM_LOAD( "bm1-prg0.bin",		0xe0000, 0x20000, 0xb57ff8c1 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x70000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD_HS( "bm1-voi0.bin",	0x10000, 0x10000, 0x4e40d0ca )
/*TODO*///		ROM_LOAD(	 "bm1-voi1.bin",	0x30000, 0x20000, 0xbe9ce0a8 );
/*TODO*///		ROM_LOAD_HS( "bm1-voi2.bin",	0x50000, 0x10000, 0x41225d04 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "bm-chr8.bin",		0x00000, 0x20000, 0x92860e95 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "bm-chr0.bin",		0x00000, 0x20000, 0xeda1d92e );
/*TODO*///		ROM_LOAD( "bm-chr1.bin",		0x20000, 0x20000, 0x8ae1891e );
/*TODO*///		ROM_LOAD( "bm-chr2.bin",		0x40000, 0x20000, 0x774cdf4e );
/*TODO*///		ROM_LOAD( "bm-chr3.bin",		0x60000, 0x20000, 0x6d81e6c9 );
/*TODO*///		ROM_LOAD( "bm-chr4.bin",		0x80000, 0x20000, 0xf4597683 );
/*TODO*///		ROM_LOAD( "bm-chr5.bin",		0xa0000, 0x20000, 0x0e0abde0 );
/*TODO*///		ROM_LOAD( "bm-chr6.bin",		0xc0000, 0x20000, 0x4a61f08c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "bm-obj0.bin",		0x00000, 0x20000, 0x15724b94 );
/*TODO*///		ROM_LOAD( "bm-obj1.bin",		0x20000, 0x20000, 0x5d21f962 );
/*TODO*///		ROM_LOAD( "bm-obj2.bin",		0x40000, 0x20000, 0x5d48e924 );
/*TODO*///		ROM_LOAD( "bm-obj3.bin",		0x60000, 0x20000, 0xcbe56b7f );
/*TODO*///		ROM_LOAD( "bm-obj4.bin",		0x80000, 0x20000, 0x76dcc24c );
/*TODO*///		ROM_LOAD( "bm-obj5.bin",		0xa0000, 0x20000, 0xfe70201d );
/*TODO*///		ROM_LOAD( "bm-obj7.bin",		0xe0000, 0x20000, 0x377c81ed );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* Marchen Maze */
/*TODO*///	static RomLoadPtr rom_mmaze = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "mm.sd0", 			0x0c000, 0x10000, 0x25d25e07 );
/*TODO*///		ROM_LOAD( "mm.sd1", 			0x1c000, 0x10000, 0x2c5849c8 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD_HS( "mm1.p7",			0x00000, 0x10000, 0x085e58cc )
/*TODO*///		ROM_LOAD_HS( "mm1.p6",			0x20000, 0x10000, 0xeaf530d8 )
/*TODO*///		ROM_LOAD( "mm.p2",				0xa0000, 0x20000, 0x91bde09f );
/*TODO*///		ROM_LOAD( "mm.p1",				0xc0000, 0x20000, 0x6ba14e41 );
/*TODO*///		ROM_LOAD( "mm.p0",				0xe0000, 0x20000, 0xe169a911 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD( "mm.v0",				0x20000, 0x10000, 0xee974cff );
/*TODO*///		ROM_CONTINUE(					0x10000, 0x10000 );
/*TODO*///		ROM_LOAD( "mm.v1",				0x30000, 0x20000, 0xd09b5830 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "mm.ch8", 			0x00000, 0x20000, 0xa3784dfe );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "mm.ch0", 			0x00000, 0x20000, 0x43ff2dfc );
/*TODO*///		ROM_LOAD( "mm.ch1", 			0x20000, 0x20000, 0xb9b4b72d );
/*TODO*///		ROM_LOAD( "mm.ch2", 			0x40000, 0x20000, 0xbee28425 );
/*TODO*///		ROM_LOAD( "mm.ch3", 			0x60000, 0x20000, 0xd9f41e5c );
/*TODO*///		ROM_LOAD( "mm.ch4", 			0x80000, 0x20000, 0x3484f4ae );
/*TODO*///		ROM_LOAD( "mm.ch5", 			0xa0000, 0x20000, 0xc863deba );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "mm.ob0", 			0x00000, 0x20000, 0xd4b7e698 );
/*TODO*///		ROM_LOAD( "mm.ob1", 			0x20000, 0x20000, 0x1ce49e04 );
/*TODO*///		ROM_LOAD( "mm.ob2", 			0x40000, 0x20000, 0x3d3d5de3 );
/*TODO*///		ROM_LOAD( "mm.ob3", 			0x60000, 0x20000, 0xdac57358 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* Bakutotsu Kijuutei */
/*TODO*///	static RomLoadPtr rom_bakutotu = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "bk1-snd0.bin",		0x0c000, 0x10000, 0xc35d7df6 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD( "bk1-prg7.bin",		0x10000, 0x10000, 0xfac1c1bf );
/*TODO*///		ROM_CONTINUE(					0x00000, 0x10000 );
/*TODO*///		ROM_LOAD(	 "bk1-prg6.bin",	0x20000, 0x20000, 0x57a3ce42 );
/*TODO*///		ROM_LOAD_HS( "bk1-prg5.bin",	0x40000, 0x10000, 0xdceed7cb )
/*TODO*///		ROM_LOAD_HS( "bk1-prg4.bin",	0x60000, 0x10000, 0x96446d48 )
/*TODO*///		ROM_LOAD(	 "bk1-prg3.bin",	0x80000, 0x20000, 0xe608234f );
/*TODO*///		ROM_LOAD_HS( "bk1-prg2.bin",	0xa0000, 0x10000, 0x7a686daa )
/*TODO*///		ROM_LOAD_HS( "bk1-prg1.bin",	0xc0000, 0x10000, 0xd389d6d4 )
/*TODO*///		ROM_LOAD(	 "bk1-prg0.bin",	0xe0000, 0x20000, 0x4529c362 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD_HS( "bk1-voi0.bin",	0x10000, 0x10000, 0x008e290e )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "bk-chr8.bin",		0x00000, 0x20000, 0x6c8d4029 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "bk-chr0.bin",		0x00000, 0x20000, 0x4e011058 );
/*TODO*///		ROM_LOAD( "bk-chr1.bin",		0x20000, 0x20000, 0x496fcb9b );
/*TODO*///		ROM_LOAD( "bk-chr2.bin",		0x40000, 0x20000, 0xdc812e28 );
/*TODO*///		ROM_LOAD( "bk-chr3.bin",		0x60000, 0x20000, 0x2b6120f4 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "bk-obj0.bin",		0x00000, 0x20000, 0x88c627c1 );
/*TODO*///		/* obj1 */
/*TODO*///		/* obj2 */
/*TODO*///		ROM_LOAD( "bk-obj3.bin",		0x60000, 0x20000, 0xf7d1909a );
/*TODO*///		ROM_LOAD( "bk-obj4.bin",		0x80000, 0x20000, 0x27ed1441 );
/*TODO*///		ROM_LOAD( "bk-obj5.bin",		0xa0000, 0x20000, 0x790560c0 );
/*TODO*///		ROM_LOAD( "bk-obj6.bin",		0xc0000, 0x20000, 0x2cd4d2ea );
/*TODO*///		ROM_LOAD( "bk-obj7.bin",		0xe0000, 0x20000, 0x809aa0e6 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* World Court */
/*TODO*///	static RomLoadPtr rom_wldcourt = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "wc1-snd0.bin",		0x0c000, 0x10000, 0x17a6505d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD_HS( "wc1-prg7.bin",	0x00000, 0x10000, 0x8a7c6cac )
/*TODO*///		ROM_LOAD_HS( "wc1-prg6.bin",	0x20000, 0x10000, 0xe9216b9e )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD_HS( "wc1-voi0.bin",	0x10000, 0x10000, 0xb57919f7 )
/*TODO*///		ROM_LOAD( "wc1-voi1.bin",		0x30000, 0x20000, 0x97974b4b );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "wc1-chr8.bin",		0x00000, 0x20000, 0x23e1c399 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "wc1-chr0.bin",		0x00000, 0x20000, 0x9fb07b9b );
/*TODO*///		ROM_LOAD( "wc1-chr1.bin",		0x20000, 0x20000, 0x01bfbf60 );
/*TODO*///		ROM_LOAD( "wc1-chr2.bin",		0x40000, 0x20000, 0x7e8acf45 );
/*TODO*///		ROM_LOAD( "wc1-chr3.bin",		0x60000, 0x20000, 0x924e9c81 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "wc1-obj0.bin",		0x00000, 0x20000, 0x70d562f8 );
/*TODO*///		ROM_LOAD( "wc1-obj1.bin",		0x20000, 0x20000, 0xba8b034a );
/*TODO*///		ROM_LOAD( "wc1-obj2.bin",		0x40000, 0x20000, 0xc2bd5f0f );
/*TODO*///		ROM_LOAD( "wc1-obj3.bin",		0x60000, 0x10000, 0x1aa2dbc8 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* Splatter House */
/*TODO*///	static RomLoadPtr rom_splatter = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "sound0", 			0x0c000, 0x10000, 0x90abd4ad );
/*TODO*///		ROM_LOAD( "sound1", 			0x1c000, 0x10000, 0x8ece9e0a );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD_HS( "prg7",			0x00000, 0x10000, 0x24c8cbd7 )
/*TODO*///		ROM_LOAD_HS( "prg6",			0x20000, 0x10000, 0x97a3e664 )
/*TODO*///		ROM_LOAD_HS( "prg5",			0x40000, 0x10000, 0x0187de9a )
/*TODO*///		ROM_LOAD_HS( "prg4",			0x60000, 0x10000, 0x350dee5b )
/*TODO*///		ROM_LOAD_HS( "prg3",			0x80000, 0x10000, 0x955ce93f )
/*TODO*///		ROM_LOAD_HS( "prg2",			0xa0000, 0x10000, 0x434dbe7d )
/*TODO*///		ROM_LOAD_HS( "prg1",			0xc0000, 0x10000, 0x7a3efe09 )
/*TODO*///		ROM_LOAD_HS( "prg0",			0xe0000, 0x10000, 0x4e07e6d9 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x90000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD( "voice0", 			0x20000, 0x10000, 0x2199cb66 );
/*TODO*///		ROM_CONTINUE(					0x10000, 0x10000 );
/*TODO*///		ROM_LOAD( "voice1", 			0x30000, 0x20000, 0x9b6472af );
/*TODO*///		ROM_LOAD( "voice2", 			0x50000, 0x20000, 0x25ea75b6 );
/*TODO*///		ROM_LOAD( "voice3", 			0x70000, 0x20000, 0x5eebcdb4 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "chr8",				0x00000, 0x20000, 0x321f483b );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "chr0",				0x00000, 0x20000, 0x4dd2ef05 );
/*TODO*///		ROM_LOAD( "chr1",				0x20000, 0x20000, 0x7a764999 );
/*TODO*///		ROM_LOAD( "chr2",				0x40000, 0x20000, 0x6e6526ee );
/*TODO*///		ROM_LOAD( "chr3",				0x60000, 0x20000, 0x8d05abdb );
/*TODO*///		ROM_LOAD( "chr4",				0x80000, 0x20000, 0x1e1f8488 );
/*TODO*///		ROM_LOAD( "chr5",				0xa0000, 0x20000, 0x684cf554 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "obj0",				0x00000, 0x20000, 0x1cedbbae );
/*TODO*///		ROM_LOAD( "obj1",				0x20000, 0x20000, 0xe56e91ee );
/*TODO*///		ROM_LOAD( "obj2",				0x40000, 0x20000, 0x3dfb0230 );
/*TODO*///		ROM_LOAD( "obj3",				0x60000, 0x20000, 0xe4e5a581 );
/*TODO*///		ROM_LOAD( "obj4",				0x80000, 0x20000, 0xb2422182 );
/*TODO*///		ROM_LOAD( "obj5",				0xa0000, 0x20000, 0x24d0266f );
/*TODO*///		ROM_LOAD( "obj6",				0xc0000, 0x20000, 0x80830b0e );
/*TODO*///		ROM_LOAD( "obj7",				0xe0000, 0x20000, 0x08b1953a );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* Face Off */
/*TODO*///	static RomLoadPtr rom_faceoff = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "fo1-s0.bin", 		0x0c000, 0x10000, 0x9a00d97d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD_HS( "fo1-p7.bin",		0x00000, 0x10000, 0x6791d221 )
/*TODO*///		ROM_LOAD_HS( "fo1-p6.bin",		0x20000, 0x10000, 0xa48ee82b )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD_HS( "fo1-v0.bin",		0x10000, 0x10000, 0xe6edf63e )
/*TODO*///		ROM_LOAD_HS( "fo1-v1.bin",		0x30000, 0x10000, 0x132a5d90 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "fo1-c8.bin", 		0x00000, 0x10000, 0xd397216c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "fo1-c0.bin", 		0x00000, 0x20000, 0x27884ac0 );
/*TODO*///		ROM_LOAD( "fo1-c1.bin", 		0x20000, 0x20000, 0x4d423499 );
/*TODO*///		ROM_LOAD( "fo1-c2.bin", 		0x40000, 0x20000, 0xd62d86f1 );
/*TODO*///		ROM_LOAD( "fo1-c3.bin", 		0x60000, 0x20000, 0xc2a08694 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "fo1-o0.bin", 		0x00000, 0x20000, 0x41af669d );
/*TODO*///		ROM_LOAD( "fo1-o1.bin", 		0x20000, 0x20000, 0xad5fbaa7 );
/*TODO*///		ROM_LOAD( "fo1-o2.bin", 		0x40000, 0x20000, 0xc1f7eb52 );
/*TODO*///		ROM_LOAD( "fo1-o3.bin", 		0x60000, 0x20000, 0xaa95d2e0 );
/*TODO*///		ROM_LOAD( "fo1-o4.bin", 		0x80000, 0x20000, 0x985f04c7 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* Rompers */
/*TODO*///	static RomLoadPtr rom_rompers = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "rp1-snd0.bin",		0x0c000, 0x10000, 0xc7c8d649 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD_HS( "rp1-prg7.bin",	0x00000, 0x10000, 0x49d057e2 )
/*TODO*///		ROM_LOAD_HS( "rp1-prg6.bin",	0x20000, 0x10000, 0x80821065 )
/*TODO*///		ROM_LOAD_HS( "rp1-prg5.bin",	0x40000, 0x10000, 0x98bd4133 )
/*TODO*///		ROM_LOAD_HS( "rp1-prg4.bin",	0x60000, 0x10000, 0x0918f06d )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD( "rp1-voi0.bin",		0x20000, 0x10000, 0x11caef7e );
/*TODO*///		ROM_CONTINUE(					0x10000, 0x10000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "rp1-chr8.bin",		0x00000, 0x10000, 0x69cfe46a );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "rp1-chr0.bin",		0x00000, 0x20000, 0x41b10ef3 );
/*TODO*///		ROM_LOAD( "rp1-chr1.bin",		0x20000, 0x20000, 0xc18cd24e );
/*TODO*///		ROM_LOAD( "rp1-chr2.bin",		0x40000, 0x20000, 0x6c9a3c79 );
/*TODO*///		ROM_LOAD( "rp1-chr3.bin",		0x60000, 0x20000, 0x473aa788 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "rp1-obj0.bin",		0x00000, 0x20000, 0x1dcbf8bb );
/*TODO*///		ROM_LOAD( "rp1-obj1.bin",		0x20000, 0x20000, 0xcb98e273 );
/*TODO*///		ROM_LOAD( "rp1-obj2.bin",		0x40000, 0x20000, 0x6ebd191e );
/*TODO*///		ROM_LOAD( "rp1-obj3.bin",		0x60000, 0x20000, 0x7c9828a1 );
/*TODO*///		ROM_LOAD( "rp1-obj4.bin",		0x80000, 0x20000, 0x0348220b );
/*TODO*///		ROM_LOAD( "rp1-obj5.bin",		0xa0000, 0x10000, 0x9e2ba243 );
/*TODO*///		ROM_LOAD( "rp1-obj6.bin",		0xc0000, 0x10000, 0x6bf2aca6 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* Rompers (old version) */
/*TODO*///	static RomLoadPtr rom_romperso = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "rp1-snd0.bin",		0x0c000, 0x10000, 0xc7c8d649 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD_HS( "rp1-p7.bin",		0x00000, 0x10000, 0x8d49f28a )
/*TODO*///		ROM_LOAD_HS( "rp1-p6.bin",		0x20000, 0x10000, 0xfc183345 )
/*TODO*///		ROM_LOAD_HS( "rp1-prg5.bin",	0x40000, 0x10000, 0x98bd4133 )
/*TODO*///		ROM_LOAD_HS( "rp1-prg4.bin",	0x60000, 0x10000, 0x0918f06d )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD( "rp1-voi0.bin",		0x20000, 0x10000, 0x11caef7e );
/*TODO*///		ROM_CONTINUE(					0x10000, 0x10000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "rp1-chr8.bin",		0x00000, 0x10000, 0x69cfe46a );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "rp1-chr0.bin",		0x00000, 0x20000, 0x41b10ef3 );
/*TODO*///		ROM_LOAD( "rp1-chr1.bin",		0x20000, 0x20000, 0xc18cd24e );
/*TODO*///		ROM_LOAD( "rp1-chr2.bin",		0x40000, 0x20000, 0x6c9a3c79 );
/*TODO*///		ROM_LOAD( "rp1-chr3.bin",		0x60000, 0x20000, 0x473aa788 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "rp1-obj0.bin",		0x00000, 0x20000, 0x1dcbf8bb );
/*TODO*///		ROM_LOAD( "rp1-obj1.bin",		0x20000, 0x20000, 0xcb98e273 );
/*TODO*///		ROM_LOAD( "rp1-obj2.bin",		0x40000, 0x20000, 0x6ebd191e );
/*TODO*///		ROM_LOAD( "rp1-obj3.bin",		0x60000, 0x20000, 0x7c9828a1 );
/*TODO*///		ROM_LOAD( "rp1-obj4.bin",		0x80000, 0x20000, 0x0348220b );
/*TODO*///		ROM_LOAD( "rp1-obj5.bin",		0xa0000, 0x10000, 0x9e2ba243 );
/*TODO*///		ROM_LOAD( "rp1-obj6.bin",		0xc0000, 0x10000, 0x6bf2aca6 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* Blast Off */
/*TODO*///	static RomLoadPtr rom_blastoff = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "bo1-snd0.bin",		0x0c000, 0x10000, 0x2ecab76e );
/*TODO*///		ROM_LOAD( "bo1-snd1.bin",		0x1c000, 0x10000, 0x048a6af1 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD( "bo1prg7b.bin",		0x10000, 0x10000, 0xb630383c );
/*TODO*///		ROM_CONTINUE(					0x00000, 0x10000 );
/*TODO*///		ROM_LOAD( "bo1-prg6.bin",		0x20000, 0x20000, 0xd60da63e );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x70000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD( "bo1-voi0.bin",		0x20000, 0x10000, 0x47065e18 );
/*TODO*///		ROM_CONTINUE(					0x10000, 0x10000 );
/*TODO*///		ROM_LOAD( "bo1-voi1.bin",		0x30000, 0x20000, 0x0308b18e );
/*TODO*///		ROM_LOAD( "bo1-voi2.bin",		0x50000, 0x20000, 0x88cab230 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "bo1-chr8.bin",		0x00000, 0x20000, 0xe8b5f2d4 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "bo1-chr0.bin",		0x00000, 0x20000, 0xbdc0afb5 );
/*TODO*///		ROM_LOAD( "bo1-chr1.bin",		0x20000, 0x20000, 0x963d2639 );
/*TODO*///		ROM_LOAD( "bo1-chr2.bin",		0x40000, 0x20000, 0xacdb6894 );
/*TODO*///		ROM_LOAD( "bo1-chr3.bin",		0x60000, 0x20000, 0x214ec47f );
/*TODO*///		ROM_LOAD( "bo1-chr4.bin",		0x80000, 0x20000, 0x08397583 );
/*TODO*///		ROM_LOAD( "bo1-chr5.bin",		0xa0000, 0x20000, 0x20402429 );
/*TODO*///		ROM_LOAD( "bo1-chr7.bin",		0xe0000, 0x20000, 0x4c5c4603 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "bo1-obj0.bin",		0x00000, 0x20000, 0xb3308ae7 );
/*TODO*///		ROM_LOAD( "bo1-obj1.bin",		0x20000, 0x20000, 0xc9c93c47 );
/*TODO*///		ROM_LOAD( "bo1-obj2.bin",		0x40000, 0x20000, 0xeef77527 );
/*TODO*///		ROM_LOAD( "bo1-obj3.bin",		0x60000, 0x20000, 0xe3d9ed58 );
/*TODO*///		ROM_LOAD( "bo1-obj4.bin",		0x80000, 0x20000, 0xc2c1b9cb );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* World Stadium '89 */
/*TODO*///	static RomLoadPtr rom_ws89 = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "w89-s0.bin", 		0x0c000, 0x10000, 0x52b84d5a );
/*TODO*///		ROM_LOAD( "ws1-s1.bin", 		0x1c000, 0x10000, 0x31bf74c1 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD_HS( "w89-p7.bin",		0x00000, 0x10000, 0x611ed964 )
/*TODO*///		ROM_LOAD_HS( "w89-p2.bin",		0xa0000, 0x10000, 0x522e5441 )
/*TODO*///		ROM_LOAD_HS( "w89-p1.bin",		0xc0000, 0x10000, 0x7ad8768f )
/*TODO*///		ROM_LOAD_HS( "ws1-p0.bin",		0xe0000, 0x10000, 0xb0234298 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD_HS( "ws1-v0.bin",		0x10000, 0x10000, 0xf6949199 )
/*TODO*///		ROM_LOAD( "ws1-v1.bin", 		0x30000, 0x20000, 0x210e2af9 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "ws1-c8.bin", 		0x00000, 0x20000, 0xd1897b9b );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "ws1-c0.bin", 		0x00000, 0x20000, 0x3e3e96b4 );
/*TODO*///		ROM_LOAD( "ws1-c1.bin", 		0x20000, 0x20000, 0x897dfbc1 );
/*TODO*///		ROM_LOAD( "ws1-c2.bin", 		0x40000, 0x20000, 0xe142527c );
/*TODO*///		ROM_LOAD( "ws1-c3.bin", 		0x60000, 0x20000, 0x907d4dc8 );
/*TODO*///		ROM_LOAD( "ws1-c4.bin", 		0x80000, 0x20000, 0xafb11e17 );
/*TODO*///		ROM_LOAD( "ws1-c6.bin", 		0xc0000, 0x20000, 0xa16a17c2 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "ws1-o0.bin", 		0x00000, 0x20000, 0x12dc83a6 );
/*TODO*///		ROM_LOAD( "ws1-o1.bin", 		0x20000, 0x20000, 0x68290a46 );
/*TODO*///		ROM_LOAD( "ws1-o2.bin", 		0x40000, 0x20000, 0xcd5ba55d );
/*TODO*///		ROM_LOAD_HS( "w89-o3.bin",		0x60000, 0x10000, 0x8ee76105 )
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* Dangerous Seed */
/*TODO*///	static RomLoadPtr rom_dangseed = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "dr1-snd0.bin",		0x0c000, 0x20000, 0xbcbbb21d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD( "dr1-prg7.bin",		0x10000, 0x10000, 0xd7d2f653 );
/*TODO*///		ROM_CONTINUE(					0x00000, 0x10000 );
/*TODO*///		ROM_LOAD_HS( "dr1-prg6.bin",	0x20000, 0x10000, 0xcc68262b )
/*TODO*///		ROM_LOAD( "dr1-prg5.bin",		0x40000, 0x20000, 0x7986bbdd );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD( "dr1-voi0.bin",		0x20000, 0x10000, 0xde4fdc0e );
/*TODO*///		ROM_CONTINUE(					0x10000, 0x10000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "dr1-chr8.bin",		0x00000, 0x20000, 0x0fbaa10e );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "dr1-chr0.bin",		0x00000, 0x20000, 0x419bacc7 );
/*TODO*///		ROM_LOAD( "dr1-chr1.bin",		0x20000, 0x20000, 0x55ce77e1 );
/*TODO*///		ROM_LOAD( "dr1-chr2.bin",		0x40000, 0x20000, 0x6f913419 );
/*TODO*///		ROM_LOAD( "dr1-chr3.bin",		0x60000, 0x20000, 0xfe1f1a25 );
/*TODO*///		ROM_LOAD( "dr1-chr4.bin",		0x80000, 0x20000, 0xc34471bc );
/*TODO*///		ROM_LOAD( "dr1-chr5.bin",		0xa0000, 0x20000, 0x715c0720 );
/*TODO*///		ROM_LOAD( "dr1-chr6.bin",		0xc0000, 0x20000, 0x5c1b71fa );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "dr1-obj0.bin",		0x00000, 0x20000, 0xabb95644 );
/*TODO*///		ROM_LOAD( "dr1-obj1.bin",		0x20000, 0x20000, 0x24d6db51 );
/*TODO*///		ROM_LOAD( "dr1-obj2.bin",		0x40000, 0x20000, 0x7e3a78c0 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* World Stadium '90 */
/*TODO*///	static RomLoadPtr rom_ws90 = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "w89-s0.bin", 		0x0c000, 0x10000, 0x52b84d5a );
/*TODO*///		ROM_LOAD( "ws1-s1.bin", 		0x1c000, 0x10000, 0x31bf74c1 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD_HS( "w90-p7.bin",		0x00000, 0x10000, 0x37ae1b25 )
/*TODO*///		ROM_LOAD_HS( "w90-p2.bin",		0xa0000, 0x10000, 0xb9e98e2f )
/*TODO*///		ROM_LOAD_HS( "w89-p1.bin",		0xc0000, 0x10000, 0x7ad8768f )
/*TODO*///		ROM_LOAD_HS( "ws1-p0.bin",		0xe0000, 0x10000, 0xb0234298 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD_HS( "ws1-v0.bin",		0x10000, 0x10000, 0xf6949199 )
/*TODO*///		ROM_LOAD( "ws1-v1.bin", 		0x30000, 0x20000, 0x210e2af9 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "ws1-c8.bin", 		0x00000, 0x20000, 0xd1897b9b );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "ws1-c0.bin", 		0x00000, 0x20000, 0x3e3e96b4 );
/*TODO*///		ROM_LOAD( "ws1-c1.bin", 		0x20000, 0x20000, 0x897dfbc1 );
/*TODO*///		ROM_LOAD( "ws1-c2.bin", 		0x40000, 0x20000, 0xe142527c );
/*TODO*///		ROM_LOAD( "ws1-c3.bin", 		0x60000, 0x20000, 0x907d4dc8 );
/*TODO*///		ROM_LOAD( "ws1-c4.bin", 		0x80000, 0x20000, 0xafb11e17 );
/*TODO*///		ROM_LOAD( "ws1-c6.bin", 		0xc0000, 0x20000, 0xa16a17c2 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "ws1-o0.bin", 		0x00000, 0x20000, 0x12dc83a6 );
/*TODO*///		ROM_LOAD( "ws1-o1.bin", 		0x20000, 0x20000, 0x68290a46 );
/*TODO*///		ROM_LOAD( "ws1-o2.bin", 		0x40000, 0x20000, 0xcd5ba55d );
/*TODO*///		ROM_LOAD_HS( "w90-o3.bin",		0x60000, 0x10000, 0x7d0b8961 )
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* Pistol Daimyo no Bouken */
/*TODO*///	static RomLoadPtr rom_pistoldm = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "pd1-snd0.bin",		0x0c000, 0x20000, 0x026da54e );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD( "pd1prg7b.bin",		0x10000, 0x10000, 0x7189b797 );
/*TODO*///		ROM_CONTINUE(					0x00000, 0x10000 );
/*TODO*///		ROM_LOAD( "pd1-prg0.bin",		0xe0000, 0x20000, 0x9db9b89c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD( "pd-voi0.bin",		0x20000, 0x10000, 0xad1b8128 );
/*TODO*///		ROM_CONTINUE(					0x10000, 0x10000 );
/*TODO*///		ROM_LOAD( "pd-voi1.bin",		0x30000, 0x20000, 0x2871c494 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "pd-chr8.bin",		0x00000, 0x20000, 0xa5f516db );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "pd-chr0.bin",		0x00000, 0x20000, 0xadbbaf5c );
/*TODO*///		ROM_LOAD( "pd-chr1.bin",		0x20000, 0x20000, 0xb4e4f554 );
/*TODO*///		ROM_LOAD( "pd-chr2.bin",		0x40000, 0x20000, 0x84592540 );
/*TODO*///		ROM_LOAD( "pd-chr3.bin",		0x60000, 0x20000, 0x450bdaa9 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "pd-obj0.bin",		0x00000, 0x20000, 0x7269821d );
/*TODO*///		ROM_LOAD( "pd-obj1.bin",		0x20000, 0x20000, 0x4f9738e5 );
/*TODO*///		ROM_LOAD( "pd-obj2.bin",		0x40000, 0x20000, 0x33208776 );
/*TODO*///		ROM_LOAD( "pd-obj3.bin",		0x60000, 0x20000, 0x0dbd54ef );
/*TODO*///		ROM_LOAD( "pd-obj4.bin",		0x80000, 0x20000, 0x58e838e2 );
/*TODO*///		ROM_LOAD( "pd-obj5.bin",		0xa0000, 0x20000, 0x414f9a9d );
/*TODO*///		ROM_LOAD( "pd-obj6.bin",		0xc0000, 0x20000, 0x91b4e6e0 );
/*TODO*///		ROM_LOAD( "pd-obj7.bin",		0xe0000, 0x20000, 0x00d4a8f0 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* Souko Ban Deluxe */
/*TODO*///	static RomLoadPtr rom_soukobdx = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "sb1-snd0.bin",		0x0c000, 0x10000, 0xbf46a106 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD_HS( "sb1-prg7.bin",	0x00000, 0x10000, 0xc3bd418a )
/*TODO*///		ROM_LOAD( "sb1-prg1.bin",		0xc0000, 0x20000, 0x5d1fdd94 );
/*TODO*///		ROM_LOAD( "sb1-prg0.bin",		0xe0000, 0x20000, 0x8af8cb73 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD_HS( "sb1-voi0.bin",	0x10000, 0x10000, 0x63d9cedf )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "sb1-chr8.bin",		0x00000, 0x10000, 0x5692b297 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "sb1-chr0.bin",		0x00000, 0x20000, 0x267f1331 );
/*TODO*///		ROM_LOAD( "sb1-chr1.bin",		0x20000, 0x20000, 0xe5ff61ad );
/*TODO*///		ROM_LOAD( "sb1-chr2.bin",		0x40000, 0x20000, 0x099b746b );
/*TODO*///		ROM_LOAD( "sb1-chr3.bin",		0x60000, 0x20000, 0x1551bb7c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "sb1-obj0.bin",		0x00000, 0x10000, 0xed810da4 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* Puzzle Club */
/*TODO*///	static RomLoadPtr rom_puzlclub = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "pc1_s0.bin", 		0x0c000, 0x10000, 0x44737c02 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD( "pc1_p7.bin", 		0x10000, 0x10000, 0xf0638260 );
/*TODO*///		ROM_LOAD( "pc1_p1.bin", 		0xc0000, 0x10000, 0xdfd9108a );
/*TODO*///		ROM_LOAD( "pc1_p0.bin", 		0xe0000, 0x10000, 0x2db477c8 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		/* no voices */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "pc1-c8.bin", 		0x00000, 0x20000, 0x4e196bcd );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "pc1-c0.bin", 		0x00000, 0x20000, 0xad7b134e );
/*TODO*///		ROM_LOAD( "pc1-c1.bin", 		0x20000, 0x20000, 0x10cb3207 );
/*TODO*///		ROM_LOAD( "pc1-c2.bin", 		0x40000, 0x20000, 0xd98d2c8f );
/*TODO*///		ROM_LOAD( "pc1-c3.bin", 		0x60000, 0x20000, 0x91a61d96 );
/*TODO*///		ROM_LOAD( "pc1-c4.bin", 		0x80000, 0x20000, 0xf1c95296 );
/*TODO*///		ROM_LOAD( "pc1-c5.bin", 		0xa0000, 0x20000, 0xbc443c27 );
/*TODO*///		ROM_LOAD( "pc1-c6.bin", 		0xc0000, 0x20000, 0xec0a3dc5 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		/* no sprites */
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* Tank Force */
/*TODO*///	static RomLoadPtr rom_tankfrce = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "tf1-snd0.bin",		0x0c000, 0x20000, 0x4d9cf7aa );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD( "tf1prg7.bin",		0x10000, 0x10000, 0x2ec28a87 );
/*TODO*///		ROM_CONTINUE(					0x00000, 0x10000 );
/*TODO*///		ROM_LOAD( "tf1-prg1.bin",		0xc0000, 0x20000, 0x4a8bb251 );
/*TODO*///		ROM_LOAD( "tf1-prg0.bin",		0xe0000, 0x20000, 0x2ae4b9eb );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD( "tf1-voi0.bin",		0x20000, 0x10000, 0xf542676a );
/*TODO*///		ROM_CONTINUE(					0x10000, 0x10000 );
/*TODO*///		ROM_LOAD( "tf1-voi1.bin",		0x30000, 0x20000, 0x615d09cd );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "tf1-chr8.bin",		0x00000, 0x20000, 0x7d53b31e );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "tf1-chr0.bin",		0x00000, 0x20000, 0x9e91794e );
/*TODO*///		ROM_LOAD( "tf1-chr1.bin",		0x20000, 0x20000, 0x76e1bc56 );
/*TODO*///		ROM_LOAD( "tf1-chr2.bin",		0x40000, 0x20000, 0xfcb645d9 );
/*TODO*///		ROM_LOAD( "tf1-chr3.bin",		0x60000, 0x20000, 0xa8dbf080 );
/*TODO*///		ROM_LOAD( "tf1-chr4.bin",		0x80000, 0x20000, 0x51fedc8c );
/*TODO*///		ROM_LOAD( "tf1-chr5.bin",		0xa0000, 0x20000, 0xe6c6609a );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "tf1-obj0.bin",		0x00000, 0x20000, 0x4bedd51a );
/*TODO*///		ROM_LOAD( "tf1-obj1.bin",		0x20000, 0x20000, 0xdf674d6d );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/* Tank Force (Japan) */
/*TODO*///	static RomLoadPtr rom_tankfrcj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for the main cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );	/* 64k for the sub cpu */
/*TODO*///		/* Nothing loaded here. Bankswitching makes sure this gets the necessary code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x2c000, REGION_CPU3 );	/* 176k for the sound cpu */
/*TODO*///		ROM_LOAD( "tf1-snd0.bin",		0x0c000, 0x20000, 0x4d9cf7aa );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_USER1 );/* 1M for ROMs */
/*TODO*///		ROM_LOAD( "tf1-prg7.bin",		0x10000, 0x10000, 0x9dfa0dd5 );
/*TODO*///		ROM_CONTINUE(					0x00000, 0x10000 );
/*TODO*///		ROM_LOAD( "tf1-prg1.bin",		0xc0000, 0x20000, 0x4a8bb251 );
/*TODO*///		ROM_LOAD( "tf1-prg0.bin",		0xe0000, 0x20000, 0x2ae4b9eb );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x14000, REGION_USER2 );	/* 80k for RAM */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000, REGION_CPU4 );	/* the MCU & voice */
/*TODO*///		ROM_LOAD( "ns1-mcu.bin",		0x0f000, 0x01000, 0xffb5c0bd );
/*TODO*///		ROM_LOAD( "tf1-voi0.bin",		0x20000, 0x10000, 0xf542676a );
/*TODO*///		ROM_CONTINUE(					0x10000, 0x10000 );
/*TODO*///		ROM_LOAD( "tf1-voi1.bin",		0x30000, 0x20000, 0x615d09cd );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE ); /* character mask */
/*TODO*///		ROM_LOAD( "tf1-chr8.bin",		0x00000, 0x20000, 0x7d53b31e );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* characters */
/*TODO*///		ROM_LOAD( "tf1-chr0.bin",		0x00000, 0x20000, 0x9e91794e );
/*TODO*///		ROM_LOAD( "tf1-chr1.bin",		0x20000, 0x20000, 0x76e1bc56 );
/*TODO*///		ROM_LOAD( "tf1-chr2.bin",		0x40000, 0x20000, 0xfcb645d9 );
/*TODO*///		ROM_LOAD( "tf1-chr3.bin",		0x60000, 0x20000, 0xa8dbf080 );
/*TODO*///		ROM_LOAD( "tf1-chr4.bin",		0x80000, 0x20000, 0x51fedc8c );
/*TODO*///		ROM_LOAD( "tf1-chr5.bin",		0xa0000, 0x20000, 0xe6c6609a );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
/*TODO*///		ROM_LOAD( "tf1-obj0.bin",		0x00000, 0x20000, 0x4bedd51a );
/*TODO*///		ROM_LOAD( "tf1-obj1.bin",		0x20000, 0x20000, 0xdf674d6d );
/*TODO*///	ROM_END(); }}; 
	
	
	
	//    YEAR, NAME,     PARENT,   MACHINE, INPUT,   INIT,     MONITOR,      COMPANY, FULLNAME
	public static GameDriver driver_shadowld	   = new GameDriver("1987"	,"shadowld"	,"namcos1.java"	,rom_shadowld,null	,machine_driver_ns1	,input_ports_ns1	,init_shadowld	,ROT0_16BIT	,	"Namco", "Shadow Land" );
/*TODO*///	public static GameDriver driver_youkaidk	   = new GameDriver("1987"	,"youkaidk"	,"namcos1.java"	,rom_youkaidk,driver_shadowld	,machine_driver_ns1	,input_ports_ns1	,init_shadowld	,ROT0_16BIT	,	"Namco", "Yokai Douchuuki (Japan new version)" )
/*TODO*///	public static GameDriver driver_yokaidko	   = new GameDriver("1987"	,"yokaidko"	,"namcos1.java"	,rom_yokaidko,driver_shadowld	,machine_driver_ns1	,input_ports_ns1	,init_shadowld	,ROT0_16BIT	,	"Namco", "Yokai Douchuuki (Japan old version)" )
/*TODO*///	public static GameDriver driver_dspirit	   = new GameDriver("1987"	,"dspirit"	,"namcos1.java"	,rom_dspirit,null	,machine_driver_ns1	,input_ports_dspirit	,init_dspirit	,ROT270	,	"Namco", "Dragon Spirit (new version)" )
/*TODO*///	public static GameDriver driver_dspirito	   = new GameDriver("1987"	,"dspirito"	,"namcos1.java"	,rom_dspirito,driver_dspirit	,machine_driver_ns1	,input_ports_dspirit	,init_dspirit	,ROT270	,	"Namco", "Dragon Spirit (old version)" )
/*TODO*///	public static GameDriver driver_blazer	   = new GameDriver("1987"	,"blazer"	,"namcos1.java"	,rom_blazer,null	,machine_driver_ns1	,input_ports_ns1	,init_blazer	,ROT270	,	"Namco", "Blazer (Japan)" )
/*TODO*///	public static GameDriver driver_quester	   = new GameDriver("1987"	,"quester"	,"namcos1.java"	,rom_quester,null	,machine_driver_quester	,input_ports_quester	,init_quester	,ROT270	,	"Namco", "Quester (Japan)" )
	public static GameDriver driver_pacmania	   = new GameDriver("1987"	,"pacmania"	,"namcos1.java"	,rom_pacmania,null	,machine_driver_ns1	,input_ports_ns1	,init_pacmania	,ROT90_16BIT	,	"Namco", "Pac-Mania" );
/*TODO*///	public static GameDriver driver_pacmanij	   = new GameDriver("1987"	,"pacmanij"	,"namcos1.java"	,rom_pacmanij,driver_pacmania	,machine_driver_ns1	,input_ports_ns1	,init_pacmania	,ROT270_16BIT	,	"Namco", "Pac-Mania (Japan)" )
	public static GameDriver driver_galaga88	   = new GameDriver("1987"	,"galaga88"	,"namcos1.java"	,rom_galaga88,null	,machine_driver_ns1	,input_ports_ns1	,init_galaga88	,ROT90_16BIT	,	"Namco", "Galaga '88 (set 1)" );
/*TODO*///	public static GameDriver driver_galag88b	   = new GameDriver("1987"	,"galag88b"	,"namcos1.java"	,rom_galag88b,driver_galaga88	,machine_driver_ns1	,input_ports_ns1	,init_galaga88	,ROT90_16BIT	,	"Namco", "Galaga '88 (set 2)" )
/*TODO*///	public static GameDriver driver_galag88j	   = new GameDriver("1987"	,"galag88j"	,"namcos1.java"	,rom_galag88j,driver_galaga88	,machine_driver_ns1	,input_ports_ns1	,init_galaga88	,ROT270_16BIT	,	"Namco", "Galaga '88 (Japan)" )
/*TODO*///	public static GameDriver driver_ws	   = new GameDriver("1988"	,"ws"	,"namcos1.java"	,rom_ws,null	,machine_driver_ns1	,input_ports_ns1	,init_ws	,ROT0	,	"Namco", "World Stadium (Japan)" )
/*TODO*///	public static GameDriver driver_berabohm	   = new GameDriver("1988"	,"berabohm"	,"namcos1.java"	,rom_berabohm,null	,machine_driver_ns1	,input_ports_berabohm	,init_berabohm	,ROT0_16BIT	,	"Namco", "Beraboh Man (Japan)" )
/*TODO*///	//public static GameDriver driver_alice	   = new GameDriver("1988"	,"alice"	,"namcos1.java"	,rom_alice,null	,machine_driver_ns1	,input_ports_ns1	,init_alice	,ROT0_16BIT	,	"Namco", "Alice In Wonderland" )
/*TODO*///	public static GameDriver driver_mmaze	   = new GameDriver("1988"	,"mmaze"	,"namcos1.java"	,rom_mmaze,null	,machine_driver_ns1	,input_ports_ns1	,init_alice	,ROT0_16BIT	,	"Namco", "Marchen Maze (Japan)" )
/*TODO*///	public static GameDriver driver_bakutotu	   = new GameDriver("1988"	,"bakutotu"	,"namcos1.java"	,rom_bakutotu,null	,machine_driver_ns1	,input_ports_ns1	,init_bakutotu	,ROT0	,	"Namco", "Bakutotsu Kijuutei", GAME_NOT_WORKING )
/*TODO*///	public static GameDriver driver_wldcourt	   = new GameDriver("1988"	,"wldcourt"	,"namcos1.java"	,rom_wldcourt,null	,machine_driver_ns1	,input_ports_ns1	,init_wldcourt	,ROT0	,	"Namco", "World Court (Japan)" )
/*TODO*///	public static GameDriver driver_splatter	   = new GameDriver("1988"	,"splatter"	,"namcos1.java"	,rom_splatter,null	,machine_driver_ns1	,input_ports_ns1	,init_splatter	,ROT0_16BIT	,	"Namco", "Splatter House (Japan)" )
/*TODO*///	public static GameDriver driver_faceoff	   = new GameDriver("1988"	,"faceoff"	,"namcos1.java"	,rom_faceoff,null	,machine_driver_faceoff	,input_ports_faceoff	,init_faceoff	,ROT0	,	"Namco", "Face Off (Japan)" )
/*TODO*///	public static GameDriver driver_rompers	   = new GameDriver("1989"	,"rompers"	,"namcos1.java"	,rom_rompers,null	,machine_driver_ns1	,input_ports_ns1	,init_rompers	,ROT270_16BIT	,	"Namco", "Rompers (Japan)" )
/*TODO*///	public static GameDriver driver_romperso	   = new GameDriver("1989"	,"romperso"	,"namcos1.java"	,rom_romperso,driver_rompers	,machine_driver_ns1	,input_ports_ns1	,init_rompers	,ROT270_16BIT	,	"Namco", "Rompers (Japan old version)" )
/*TODO*///	public static GameDriver driver_blastoff	   = new GameDriver("1989"	,"blastoff"	,"namcos1.java"	,rom_blastoff,null	,machine_driver_ns1	,input_ports_ns1	,init_blastoff	,ROT270	,	"Namco", "Blast Off (Japan)" )
/*TODO*///	public static GameDriver driver_ws89	   = new GameDriver("1989"	,"ws89"	,"namcos1.java"	,rom_ws89,driver_ws	,machine_driver_ns1	,input_ports_ns1	,init_ws89	,ROT0	,	"Namco", "World Stadium '89 (Japan)" )
/*TODO*///	public static GameDriver driver_dangseed	   = new GameDriver("1989"	,"dangseed"	,"namcos1.java"	,rom_dangseed,null	,machine_driver_ns1	,input_ports_ns1	,init_dangseed	,ROT270_16BIT	,	"Namco", "Dangerous Seed (Japan)" )
/*TODO*///	public static GameDriver driver_ws90	   = new GameDriver("1990"	,"ws90"	,"namcos1.java"	,rom_ws90,driver_ws	,machine_driver_ns1	,input_ports_ns1	,init_ws90	,ROT0	,	"Namco", "World Stadium '90 (Japan)" )
/*TODO*///	public static GameDriver driver_pistoldm	   = new GameDriver("1990"	,"pistoldm"	,"namcos1.java"	,rom_pistoldm,null	,machine_driver_ns1	,input_ports_ns1	,init_pistoldm	,ROT180	,	"Namco", "Pistol Daimyo no Bouken (Japan)" )
/*TODO*///	public static GameDriver driver_soukobdx	   = new GameDriver("1990"	,"soukobdx"	,"namcos1.java"	,rom_soukobdx,null	,machine_driver_ns1	,input_ports_ns1	,init_soukobdx	,ROT180_16BIT	,	"Namco", "Souko Ban Deluxe (Japan)" )
/*TODO*///	public static GameDriver driver_puzlclub	   = new GameDriver("1990"	,"puzlclub"	,"namcos1.java"	,rom_puzlclub,null	,machine_driver_ns1	,input_ports_ns1	,init_puzlclub	,ROT270	,	"Namco", "Puzzle Club (Japan prototype)" )
/*TODO*///	public static GameDriver driver_tankfrce	   = new GameDriver("1991"	,"tankfrce"	,"namcos1.java"	,rom_tankfrce,null	,machine_driver_ns1	,input_ports_ns1	,init_tankfrce	,ROT180	,	"Namco", "Tank Force (US)" )
/*TODO*///	public static GameDriver driver_tankfrcj	   = new GameDriver("1991"	,"tankfrcj"	,"namcos1.java"	,rom_tankfrcj,driver_tankfrce	,machine_driver_ns1	,input_ports_ns1	,init_tankfrce	,ROT180	,	"Namco", "Tank Force (Japan)" )
}
