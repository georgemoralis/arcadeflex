/***************************************************************************

TODO:
- Radarscope does a check on bit 6 of 7d00 which prevent it from working.
  It's a sound status flag, maybe signaling whan a tune is finished.
  For now, we comment it out.

- radarscp_grid_color_w() is wrong, it probably isn't supposed to change
  the grid color. There are reports of the grid being constantly blue in
  the real game, the flyer confirms this.


Donkey Kong and Donkey Kong Jr. memory map (preliminary) (DKong 3 follows)

0000-3fff ROM (Donkey Kong Jr.and Donkey Kong 3: 0000-5fff)
6000-6fff RAM
6900-6a7f sprites
7000-73ff ?
7400-77ff Video RAM
8000-9fff ROM (DK3 only)



memory mapped ports:

read:
7c00      IN0
7c80      IN1
7d00      IN2 (DK3: DSW2)
7d80      DSW1

*
 * IN0 (bits NOT inverted)
 * bit 7 : ?
 * bit 6 : reset (when player 1 active)
 * bit 5 : ?
 * bit 4 : JUMP player 1
 * bit 3 : DOWN player 1
 * bit 2 : UP player 1
 * bit 1 : LEFT player 1
 * bit 0 : RIGHT player 1
 *
*
 * IN1 (bits NOT inverted)
 * bit 7 : ?
 * bit 6 : reset (when player 2 active)
 * bit 5 : ?
 * bit 4 : JUMP player 2
 * bit 3 : DOWN player 2
 * bit 2 : UP player 2
 * bit 1 : LEFT player 2
 * bit 0 : RIGHT player 2
 *
*
 * IN2 (bits NOT inverted)
 * bit 7 : COIN (IS inverted in Radarscope)
 * bit 6 : ? Radarscope does some wizardry with this bit
 * bit 5 : ?
 * bit 4 : ?
 * bit 3 : START 2
 * bit 2 : START 1
 * bit 1 : ?
 * bit 0 : ? if this is 1, the code jumps to $4000, outside the rom space
 *
*
 * DSW1 (bits NOT inverted)
 * bit 7 : COCKTAIL or UPRIGHT cabinet (1 = UPRIGHT)
 * bit 6 : \ 000 = 1 coin 1 play   001 = 2 coins 1 play  010 = 1 coin 2 plays
 * bit 5 : | 011 = 3 coins 1 play  100 = 1 coin 3 plays  101 = 4 coins 1 play
 * bit 4 : / 110 = 1 coin 4 plays  111 = 5 coins 1 play
 * bit 3 : \bonus at
 * bit 2 : / 00 = 7000  01 = 10000  10 = 15000  11 = 20000
 * bit 1 : \ 00 = 3 lives  01 = 4 lives
 * bit 0 : / 10 = 5 lives  11 = 6 lives
 *

write:
7800-7803 ?
7808      ?
7c00      Background sound/music select:
          00 - nothing
		  01 - Intro tune
		  02 - How High? (intermisson) tune
		  03 - Out of time
		  04 - Hammer
		  05 - Rivet level 2 completed (end tune)
		  06 - Hammer hit
		  07 - Standard level end
		  08 - Background 1	(first screen)
		  09 - ???
		  0A - Background 3	(springs)
		  0B - Background 2 (rivet)
		  0C - Rivet level 1 completed (end tune)
		  0D - Rivet removed
		  0E - Rivet level completed
		  0F - Gorilla roar
7c80      gfx bank select (Donkey Kong Jr. only)
7d00      digital sound trigger - walk
7d01      digital sound trigger - jump
7d02      digital sound trigger - boom (gorilla stomps foot)
7d03      digital sound trigger - coin input/spring
7d04      digital sound trigger	- gorilla fall
7d05      digital sound trigger - barrel jump/prize
7d06      ?
7d07      ?
7d80      digital sound trigger - dead
7d82      flip screen
7d83      ?
7d84      interrupt enable
7d85      0/1 toggle
7d86-7d87 palette bank selector (only bit 0 is significant: 7d86 = bit 0 7d87 = bit 1)


8035 Memory Map:

0000-07ff ROM
0800-0fff Compressed sound sample (Gorilla roar in DKong)

Read ports:
0x20   Read current tune
P2.5   Active low when jumping
T0     Select sound for jump (Normal or Barrell?)
T1     Active low when gorilla is falling

Write ports:
P1     Digital out
P2.7   External decay
P2.6   Select second ROM reading (MOVX instruction will access area 800-fff)
P2.2-0 Select the bank of 256 bytes for second ROM



Donkey Kong 3 memory map (preliminary):

RAM and read ports same as above;

write:
7d00      ?
7d80      ?
7e00      ?
7e80
7e81      char bank selector
7e82      flipscreen
7e83      ?
7e84      interrupt enable
7e85      ?
7e86-7e87 palette bank selector (only bit 0 is significant: 7e86 = bit 0 7e87 = bit 1)


I/O ports

write:
00        ?

Changes:
	Apr 7 98 Howie Cohen
	* Added samples for the climb, jump, land and walking sounds

	Jul 27 99 Chad Hendrickson
	* Added cocktail mode flipscreen

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class dkong
{
	
	static int page = 0,mcustatus;
	static int p[8] = { 255,255,255,255,255,255,255,255 };
	static int t[2] = { 1,1 };
	
	
	
	
	
	#define ACTIVELOW_PORT_BIT(P,A,D)   ((P & (~(1 << A))) | ((D ^ 1) << A))
	
	
	public static WriteHandlerPtr dkong_sh_sound3_w = new WriteHandlerPtr() {public void handler(int offset, int data)     { p[2] = ACTIVELOW_PORT_BIT(p[2],5,data); } };
	public static WriteHandlerPtr dkong_sh_sound4_w = new WriteHandlerPtr() {public void handler(int offset, int data)    { t[1] = ~data & 1; } };
	public static WriteHandlerPtr dkong_sh_sound5_w = new WriteHandlerPtr() {public void handler(int offset, int data)    { t[0] = ~data & 1; } };
	public static WriteHandlerPtr dkong_sh_tuneselect_w = new WriteHandlerPtr() {public void handler(int offset, int data) { soundlatch_w.handler(offset,data ^ 0x0f); } };
	
	public static WriteHandlerPtr dkongjr_sh_test6_w = new WriteHandlerPtr() {public void handler(int offset, int data)      { p[2] = ACTIVELOW_PORT_BIT(p[2],6,data); } };
	public static WriteHandlerPtr dkongjr_sh_test5_w = new WriteHandlerPtr() {public void handler(int offset, int data)      { p[2] = ACTIVELOW_PORT_BIT(p[2],5,data); } };
	public static WriteHandlerPtr dkongjr_sh_test4_w = new WriteHandlerPtr() {public void handler(int offset, int data)      { p[2] = ACTIVELOW_PORT_BIT(p[2],4,data); } };
	public static WriteHandlerPtr dkongjr_sh_tuneselect_w = new WriteHandlerPtr() {public void handler(int offset, int data) { soundlatch_w.handler(offset,data); } };
	
	
	public static ReadHandlerPtr dkong_sh_p1_r  = new ReadHandlerPtr() { public int handler(int offset)   { return p[1]; } };
	public static ReadHandlerPtr dkong_sh_p2_r  = new ReadHandlerPtr() { public int handler(int offset)   { return p[2]; } };
	public static ReadHandlerPtr dkong_sh_t0_r  = new ReadHandlerPtr() { public int handler(int offset)   { return t[0]; } };
	public static ReadHandlerPtr dkong_sh_t1_r  = new ReadHandlerPtr() { public int handler(int offset)   { return t[1]; } };
	public static ReadHandlerPtr dkong_sh_tune_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UBytePtr SND = memory_region(REGION_CPU2);
		if ((page & 0x40) != 0)
		{
			switch (offset)
			{
				case 0x20:  return soundlatch_r(0);
			}
		}
		return (SND[2048+(page & 7)*256+offset]);
	} };
	
	
	static double envelope,tt;
	static int decay;
	
	#define TSTEP 0.001
	
	public static WriteHandlerPtr dkong_sh_p1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		envelope=exp(-tt);
		DAC_data_w(0,(int)(data*envelope));
		if (decay != 0) tt+=TSTEP;
		else tt=0;
	} };
	
	public static WriteHandlerPtr dkong_sh_p2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/*   If P2.Bit7 . is apparently an external signal decay or other output control
		 *   If P2.Bit6 . activates the external compressed sample ROM
		 *   If P2.Bit4 . status code to main cpu
		 *   P2.Bit2-0  . select the 256 byte bank for external ROM
		 */
	
		decay = !(data & 0x80);
		page = (data & 0x47);
		mcustatus = ((~data & 0x10) >> 4);
	} };
	
	
	public static ReadHandlerPtr dkong_in2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return input_port_2_r.handler(offset) | (mcustatus << 6);
	} };
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x5fff, MRA_ROM ),	/* DK: 0000-3fff */
		new MemoryReadAddress( 0x6000, 0x6fff, MRA_RAM ),	/* including sprites RAM */
		new MemoryReadAddress( 0x7400, 0x77ff, MRA_RAM ),	/* video RAM */
		new MemoryReadAddress( 0x7c00, 0x7c00, input_port_0_r ),	/* IN0 */
		new MemoryReadAddress( 0x7c80, 0x7c80, input_port_1_r ),	/* IN1 */
		new MemoryReadAddress( 0x7d00, 0x7d00, dkong_in2_r ),	/* IN2/DSW2 */
		new MemoryReadAddress( 0x7d80, 0x7d80, input_port_3_r ),	/* DSW1 */
		new MemoryReadAddress( 0x8000, 0x9fff, MRA_ROM ),	/* DK3 and bootleg DKjr only */
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress dkong3_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x5fff, MRA_ROM ),	/* DK: 0000-3fff */
		new MemoryReadAddress( 0x6000, 0x6fff, MRA_RAM ),	/* including sprites RAM */
		new MemoryReadAddress( 0x7400, 0x77ff, MRA_RAM ),	/* video RAM */
		new MemoryReadAddress( 0x7c00, 0x7c00, input_port_0_r ),	/* IN0 */
		new MemoryReadAddress( 0x7c80, 0x7c80, input_port_1_r ),	/* IN1 */
		new MemoryReadAddress( 0x7d00, 0x7d00, input_port_2_r ),	/* IN2/DSW2 */
		new MemoryReadAddress( 0x7d80, 0x7d80, input_port_3_r ),	/* DSW1 */
		new MemoryReadAddress( 0x8000, 0x9fff, MRA_ROM ),	/* DK3 and bootleg DKjr only */
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress radarscp_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x5fff, MWA_ROM ),
		new MemoryWriteAddress( 0x6000, 0x68ff, MWA_RAM ),
		new MemoryWriteAddress( 0x6900, 0x6a7f, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x6a80, 0x6fff, MWA_RAM ),
		new MemoryWriteAddress( 0x7000, 0x73ff, MWA_RAM ),    /* ???? */
		new MemoryWriteAddress( 0x7400, 0x77ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x7800, 0x7803, MWA_RAM ),	/* ???? */
		new MemoryWriteAddress( 0x7808, 0x7808, MWA_RAM ),	/* ???? */
		new MemoryWriteAddress( 0x7c00, 0x7c00, dkong_sh_tuneselect_w ),
		new MemoryWriteAddress( 0x7c80, 0x7c80, radarscp_grid_color_w ),
		new MemoryWriteAddress( 0x7d00, 0x7d02, dkong_sh1_w ),	/* walk/jump/boom sample trigger */
		new MemoryWriteAddress( 0x7d03, 0x7d03, dkong_sh_sound3_w ),
		new MemoryWriteAddress( 0x7d04, 0x7d04, dkong_sh_sound4_w ),
		new MemoryWriteAddress( 0x7d05, 0x7d05, dkong_sh_sound5_w ),
		new MemoryWriteAddress( 0x7d80, 0x7d80, dkong_sh_w ),
		new MemoryWriteAddress( 0x7d81, 0x7d81, radarscp_grid_enable_w ),
		new MemoryWriteAddress( 0x7d82, 0x7d82, dkong_flipscreen_w ),
		new MemoryWriteAddress( 0x7d83, 0x7d83, MWA_RAM ),
		new MemoryWriteAddress( 0x7d84, 0x7d84, interrupt_enable_w ),
		new MemoryWriteAddress( 0x7d85, 0x7d85, MWA_RAM ),
		new MemoryWriteAddress( 0x7d86, 0x7d87, dkong_palettebank_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress dkong_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x5fff, MWA_ROM ),
		new MemoryWriteAddress( 0x6000, 0x68ff, MWA_RAM ),
		new MemoryWriteAddress( 0x6900, 0x6a7f, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x6a80, 0x6fff, MWA_RAM ),
		new MemoryWriteAddress( 0x7000, 0x73ff, MWA_RAM ),    /* ???? */
		new MemoryWriteAddress( 0x7400, 0x77ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x7800, 0x7803, MWA_RAM ),	/* ???? */
		new MemoryWriteAddress( 0x7808, 0x7808, MWA_RAM ),	/* ???? */
		new MemoryWriteAddress( 0x7c00, 0x7c00, dkong_sh_tuneselect_w ),
	//	new MemoryWriteAddress( 0x7c80, 0x7c80,  ),
		new MemoryWriteAddress( 0x7d00, 0x7d02, dkong_sh1_w ),	/* walk/jump/boom sample trigger */
		new MemoryWriteAddress( 0x7d03, 0x7d03, dkong_sh_sound3_w ),
		new MemoryWriteAddress( 0x7d04, 0x7d04, dkong_sh_sound4_w ),
		new MemoryWriteAddress( 0x7d05, 0x7d05, dkong_sh_sound5_w ),
		new MemoryWriteAddress( 0x7d80, 0x7d80, dkong_sh_w ),
		new MemoryWriteAddress( 0x7d81, 0x7d81, MWA_RAM ),	/* ???? */
		new MemoryWriteAddress( 0x7d82, 0x7d82, dkong_flipscreen_w ),
		new MemoryWriteAddress( 0x7d83, 0x7d83, MWA_RAM ),
		new MemoryWriteAddress( 0x7d84, 0x7d84, interrupt_enable_w ),
		new MemoryWriteAddress( 0x7d85, 0x7d85, MWA_RAM ),
		new MemoryWriteAddress( 0x7d86, 0x7d87, dkong_palettebank_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	public static ReadHandlerPtr herbiedk_iack_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		s2650_set_sense(1);
	    return 0;
	} };
	
	static MemoryReadAddress hunchbkd_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x0fff, MRA_ROM ),
		new MemoryReadAddress( 0x2000, 0x2fff, MRA_ROM ),
		new MemoryReadAddress( 0x4000, 0x4fff, MRA_ROM ),
		new MemoryReadAddress( 0x6000, 0x6fff, MRA_ROM ),
		new MemoryReadAddress( 0x1400, 0x1400, input_port_0_r ),		/* IN0 */
		new MemoryReadAddress( 0x1480, 0x1480, input_port_1_r ),		/* IN1 */
		new MemoryReadAddress( 0x1500, 0x1500, input_port_2_r ),		/* IN2/DSW2 */
	    new MemoryReadAddress( 0x1507, 0x1507, herbiedk_iack_r ),  	/* Clear Int */
		new MemoryReadAddress( 0x1580, 0x1580, input_port_3_r ),		/* DSW1 */
		new MemoryReadAddress( 0x1600, 0x1bff, MRA_RAM ),			/* video RAM */
		new MemoryReadAddress( 0x1c00, 0x1fff, MRA_RAM ),
	    new MemoryReadAddress( 0x3000, 0x3fff, hunchbks_mirror_r ),
	    new MemoryReadAddress( 0x5000, 0x5fff, hunchbks_mirror_r ),
	    new MemoryReadAddress( 0x7000, 0x7fff, hunchbks_mirror_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress hunchbkd_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x0fff, MWA_ROM ),
		new MemoryWriteAddress( 0x2000, 0x2fff, MWA_ROM ),
		new MemoryWriteAddress( 0x4000, 0x4fff, MWA_ROM ),
		new MemoryWriteAddress( 0x6000, 0x6fff, MWA_ROM ),
		new MemoryWriteAddress( 0x1400, 0x1400, dkong_sh_tuneselect_w ),
		new MemoryWriteAddress( 0x1480, 0x1480, dkongjr_gfxbank_w ),
		new MemoryWriteAddress( 0x1580, 0x1580, dkong_sh_w ),
		new MemoryWriteAddress( 0x1582, 0x1582, dkong_flipscreen_w ),
		new MemoryWriteAddress( 0x1584, 0x1584, MWA_RAM ),			/* Possibly still interupt enable */
		new MemoryWriteAddress( 0x1585, 0x1585, MWA_RAM ),			/* written a lot - every int */
		new MemoryWriteAddress( 0x1586, 0x1587, dkong_palettebank_w ),
		new MemoryWriteAddress( 0x1600, 0x17ff, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x1800, 0x1bff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x1C00, 0x1fff, MWA_RAM ),
	    new MemoryWriteAddress( 0x3000, 0x3fff, hunchbks_mirror_w ),
	    new MemoryWriteAddress( 0x5000, 0x5fff, hunchbks_mirror_w ),
	    new MemoryWriteAddress( 0x7000, 0x7fff, hunchbks_mirror_w ),
	    new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	int hunchloopback;
	
	public static WriteHandlerPtr hunchbkd_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		hunchloopback=data;
	} };
	
	public static ReadHandlerPtr hunchbkd_port0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		logerror("port 0 : pc = %4x\n",s2650_get_pc());
	
		switch (s2650_get_pc())
		{
			case 0x00e9:  return 0xff;
			case 0x0114:  return 0xfb;
		}
	
	    return 0;
	} };
	
	public static ReadHandlerPtr hunchbkd_port1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return hunchloopback;
	} };
	
	public static ReadHandlerPtr herbiedk_port1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (s2650_get_pc())
		{
	        case 0x002b:
			case 0x09dc:  return 0x0;
		}
	
	    return 1;
	} };
	
	static IOWritePort hunchbkd_writeport[] =
	{
		new IOWritePort( 0x101, 0x101, hunchbkd_data_w ),
		new IOWritePort( -1 )	/* end of table */
	};
	
	static IOReadPort hunchbkd_readport[] =
	{
		new IOReadPort( 0x00, 0x00, hunchbkd_port0_r ),
		new IOReadPort( 0x01, 0x01, hunchbkd_port1_r ),
		new IOReadPort( -1 )	/* end of table */
	};
	
	static IOReadPort herbiedk_readport[] =
	{
		new IOReadPort( 0x01, 0x01, herbiedk_port1_r ),
		new IOReadPort( -1 )	/* end of table */
	};
	
	static MemoryReadAddress readmem_sound[] =
	{
		new MemoryReadAddress( 0x0000, 0x0fff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	static MemoryWriteAddress writemem_sound[] =
	{
		new MemoryWriteAddress( 0x0000, 0x0fff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	static IOReadPort readport_sound[] =
	{
		new IOReadPort( 0x00,     0xff,     dkong_sh_tune_r ),
		new IOReadPort( I8039_p1, I8039_p1, dkong_sh_p1_r ),
		new IOReadPort( I8039_p2, I8039_p2, dkong_sh_p2_r ),
		new IOReadPort( I8039_t0, I8039_t0, dkong_sh_t0_r ),
		new IOReadPort( I8039_t1, I8039_t1, dkong_sh_t1_r ),
		new IOReadPort( -1 )	/* end of table */
	};
	static IOWritePort writeport_sound[] =
	{
		new IOWritePort( I8039_p1, I8039_p1, dkong_sh_p1_w ),
		new IOWritePort( I8039_p2, I8039_p2, dkong_sh_p2_w ),
		new IOWritePort( -1 )	/* end of table */
	};
	
	static IOReadPort readport_hunchbkd_sound[] =
	{
		new IOReadPort( I8039_bus,I8039_bus,soundlatch_r ),
		new IOReadPort( I8039_p1, I8039_p1, dkong_sh_p1_r ),
		new IOReadPort( I8039_p2, I8039_p2, dkong_sh_p2_r ),
		new IOReadPort( I8039_t0, I8039_t0, dkong_sh_t0_r ),
		new IOReadPort( I8039_t1, I8039_t1, dkong_sh_t1_r ),
		new IOReadPort( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress dkongjr_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x5fff, MWA_ROM ),
		new MemoryWriteAddress( 0x6000, 0x68ff, MWA_RAM ),
		new MemoryWriteAddress( 0x6900, 0x6a7f, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x6a80, 0x6fff, MWA_RAM ),
		new MemoryWriteAddress( 0x7400, 0x77ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x7800, 0x7803, MWA_RAM ),	/* ???? */
		new MemoryWriteAddress( 0x7808, 0x7808, MWA_RAM ),	/* ???? */
		new MemoryWriteAddress( 0x7c00, 0x7c00, dkongjr_sh_tuneselect_w ),
		new MemoryWriteAddress( 0x7c80, 0x7c80, dkongjr_gfxbank_w ),
		new MemoryWriteAddress( 0x7c81, 0x7c81, dkongjr_sh_test6_w ),
		new MemoryWriteAddress( 0x7d00, 0x7d00, dkongjr_sh_climb_w ), /* HC - climb sound */
		new MemoryWriteAddress( 0x7d01, 0x7d01, dkongjr_sh_jump_w ), /* HC - jump */
		new MemoryWriteAddress( 0x7d02, 0x7d02, dkongjr_sh_land_w ), /* HC - climb sound */
		new MemoryWriteAddress( 0x7d03, 0x7d03, dkongjr_sh_roar_w ),
		new MemoryWriteAddress( 0x7d04, 0x7d04, dkong_sh_sound4_w ),
		new MemoryWriteAddress( 0x7d05, 0x7d05, dkong_sh_sound5_w ),
		new MemoryWriteAddress( 0x7d06, 0x7d06, dkongjr_sh_snapjaw_w ),
		new MemoryWriteAddress( 0x7d07, 0x7d07, dkongjr_sh_walk_w ),	/* controls pitch of the walk/climb? */
		new MemoryWriteAddress( 0x7d80, 0x7d80, dkongjr_sh_death_w ),
		new MemoryWriteAddress( 0x7d81, 0x7d81, dkongjr_sh_drop_w ),   /* active when Junior is falling */new MemoryWriteAddress( 0x7d84, 0x7d84, interrupt_enable_w ),
		new MemoryWriteAddress( 0x7d82, 0x7d82, dkong_flipscreen_w ),
		new MemoryWriteAddress( 0x7d86, 0x7d87, dkong_palettebank_w ),
		new MemoryWriteAddress( 0x8000, 0x9fff, MWA_ROM ),	/* bootleg DKjr only */
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	
	
	
	
	
	public static WriteHandlerPtr dkong3_2a03_reset_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 1) != 0)
		{
			cpu_set_reset_line(1,CLEAR_LINE);
			cpu_set_reset_line(2,CLEAR_LINE);
		}
		else
		{
			cpu_set_reset_line(1,ASSERT_LINE);
			cpu_set_reset_line(2,ASSERT_LINE);
		}
	} };
	
	static MemoryWriteAddress dkong3_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x5fff, MWA_ROM ),
		new MemoryWriteAddress( 0x6000, 0x68ff, MWA_RAM ),
		new MemoryWriteAddress( 0x6900, 0x6a7f, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x6a80, 0x6fff, MWA_RAM ),
		new MemoryWriteAddress( 0x7400, 0x77ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x7c00, 0x7c00, soundlatch_w ),
		new MemoryWriteAddress( 0x7c80, 0x7c80, soundlatch2_w ),
		new MemoryWriteAddress( 0x7d00, 0x7d00, soundlatch3_w ),
		new MemoryWriteAddress( 0x7d80, 0x7d80, dkong3_2a03_reset_w ),
		new MemoryWriteAddress( 0x7e81, 0x7e81, dkong3_gfxbank_w ),
		new MemoryWriteAddress( 0x7e82, 0x7e82, dkong_flipscreen_w ),
		new MemoryWriteAddress( 0x7e84, 0x7e84, interrupt_enable_w ),
		new MemoryWriteAddress( 0x7e85, 0x7e85, MWA_NOP ),	/* ??? */
		new MemoryWriteAddress( 0x7e86, 0x7e87, dkong_palettebank_w ),
		new MemoryWriteAddress( 0x8000, 0x9fff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static IOWritePort dkong3_writeport[] =
	{
		new IOWritePort( 0x00, 0x00, IOWP_NOP ),	/* ??? */
		new IOWritePort( -1 )	/* end of table */
	};
	
	static MemoryReadAddress dkong3_sound1_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x01ff, MRA_RAM ),
		new MemoryReadAddress( 0x4016, 0x4016, soundlatch_r ),
		new MemoryReadAddress( 0x4017, 0x4017, soundlatch2_r ),
		new MemoryReadAddress( 0x4000, 0x4017, NESPSG_0_r ),
		new MemoryReadAddress( 0xe000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress dkong3_sound1_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x01ff, MWA_RAM ),
		new MemoryWriteAddress( 0x4000, 0x4017, NESPSG_0_w ),
		new MemoryWriteAddress( 0xe000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress dkong3_sound2_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x01ff, MRA_RAM ),
		new MemoryReadAddress( 0x4016, 0x4016, soundlatch3_r ),
		new MemoryReadAddress( 0x4000, 0x4017, NESPSG_1_r ),
		new MemoryReadAddress( 0xe000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress dkong3_sound2_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x01ff, MWA_RAM ),
		new MemoryWriteAddress( 0x4000, 0x4017, NESPSG_1_w ),
		new MemoryWriteAddress( 0xe000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	
	static InputPortPtr input_ports_dkong = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY );	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY );	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 );	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL );	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START();       /* IN2 */
		PORT_BITX(0x01, IP_ACTIVE_HIGH, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
	//	PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Service_Mode") );
	//	PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
	//	PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START2 );	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* status from sound cpu */
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_START();       /* DSW0 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );	PORT_DIPSETTING(    0x01, "4" );	PORT_DIPSETTING(    0x02, "5" );	PORT_DIPSETTING(    0x03, "6" );	PORT_DIPNAME( 0x0c, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "7000" );	PORT_DIPSETTING(    0x04, "10000" );	PORT_DIPSETTING(    0x08, "15000" );	PORT_DIPSETTING(    0x0c, "20000" );	PORT_DIPNAME( 0x70, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x70, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_dkong3 = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY );	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY );	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 );	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_START1 );	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_START2 );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_COIN3 );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL );	PORT_BIT_IMPULSE( 0x20, IP_ACTIVE_HIGH, IPT_COIN1, 1 );	PORT_BIT_IMPULSE( 0x40, IP_ACTIVE_HIGH, IPT_COIN2, 1 );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START();       /* DSW0 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x02, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_BITX(0x40, IP_ACTIVE_HIGH, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Cocktail") );
	
		PORT_START();       /* DSW1 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );	PORT_DIPSETTING(    0x01, "4" );	PORT_DIPSETTING(    0x02, "5" );	PORT_DIPSETTING(    0x03, "6" );	PORT_DIPNAME( 0x0c, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "30000" );	PORT_DIPSETTING(    0x04, "40000" );	PORT_DIPSETTING(    0x08, "50000" );	PORT_DIPSETTING(    0x0c, "None" );	PORT_DIPNAME( 0x30, 0x00, "Additional Bonus" );	PORT_DIPSETTING(    0x00, "30000" );	PORT_DIPSETTING(    0x10, "40000" );	PORT_DIPSETTING(    0x20, "50000" );	PORT_DIPSETTING(    0x30, "None" );	PORT_DIPNAME( 0xc0, 0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x00, "Easy" );	PORT_DIPSETTING(    0x40, "Medium" );	PORT_DIPSETTING(    0x80, "Hard" );	PORT_DIPSETTING(    0xc0, "Hardest" );INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_hunchbdk = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY );	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY );	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 );	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL );	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START();       /* IN2 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START2 );	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* status from sound cpu */
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_START();       /* DSW0 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );	PORT_DIPSETTING(    0x02, "5" );	PORT_DIPNAME( 0x0c, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "10000" );	PORT_DIPSETTING(    0x04, "20000" );	PORT_DIPSETTING(    0x08, "40000" );	PORT_DIPSETTING(    0x0c, "80000" );	PORT_DIPNAME( 0x70, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x70, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_herbiedk = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY );	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY );	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 );	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL );	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START();       /* IN2 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START2 );	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* status from sound cpu */
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_START();       /* DSW0 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x70, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x70, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
	INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout dkong_charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		256,	/* 256 characters */
		2,	/* 2 bits per pixel */
		new int[] { 256*8*8, 0 },	/* the two bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },	/* pretty straightforward layout */
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every char takes 8 consecutive bytes */
	);
	static GfxLayout dkongjr_charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		512,	/* 512 characters */
		2,	/* 2 bits per pixel */
		new int[] { 512*8*8, 0 },	/* the two bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },	/* pretty straightforward layout */
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every char takes 8 consecutive bytes */
	);
	static GfxLayout dkong_spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		128,	/* 128 sprites */
		2,	/* 2 bits per pixel */
		new int[] { 128*16*16, 0 },	/* the two bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,	/* the two halves of the sprite are separated */
				64*16*16+0, 64*16*16+1, 64*16*16+2, 64*16*16+3, 64*16*16+4, 64*16*16+5, 64*16*16+6, 64*16*16+7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		16*8	/* every sprite takes 16 consecutive bytes */
	);
	static GfxLayout dkong3_spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		256,	/* 256 sprites */
		2,	/* 2 bits per pixel */
		new int[] { 256*16*16, 0 },	/* the two bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,	/* the two halves of the sprite are separated */
				128*16*16+0, 128*16*16+1, 128*16*16+2, 128*16*16+3, 128*16*16+4, 128*16*16+5, 128*16*16+6, 128*16*16+7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		16*8	/* every sprite takes 16 consecutive bytes */
	);
	
	
	
	static GfxDecodeInfo dkong_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x0000, dkong_charlayout,   0, 64 ),
		new GfxDecodeInfo( REGION_GFX2, 0x0000, dkong_spritelayout, 0, 64 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	static GfxDecodeInfo dkongjr_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x0000, dkongjr_charlayout, 0, 64 ),
		new GfxDecodeInfo( REGION_GFX2, 0x0000, dkong_spritelayout, 0, 64 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	static GfxDecodeInfo dkong3_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x0000, dkongjr_charlayout,   0, 64 ),
		new GfxDecodeInfo( REGION_GFX2, 0x0000, dkong3_spritelayout,  0, 64 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	static DACinterface dkong_dac_interface = new DACinterface
	(
		1,
		new int[] { 55 }
	);
	
	static const char *dkong_sample_names[] =
	{
		"*dkong",
		"effect00.wav",
		"effect01.wav",
		"effect02.wav",
		0	/* end of array */
	};
	
	static const char *dkongjr_sample_names[] =
	{
		"*dkongjr",
		"jump.wav",
		"land.wav",
		"roar.wav",
		"climb.wav",   /* HC */
		"death.wav",  /* HC */
		"drop.wav",  /* HC */
		"walk.wav", /* HC */
		"snapjaw.wav",  /* HC */
		0	/* end of array */
	};
	
	static Samplesinterface dkong_samples_interface = new Samplesinterface
	(
		8,	/* 8 channels */
		25,	/* volume */
		dkong_sample_names
	);
	
	static Samplesinterface dkongjr_samples_interface = new Samplesinterface
	(
		8,	/* 8 channels */
		25,	/* volume */
		dkongjr_sample_names
	);
	
	static MachineDriver machine_driver_radarscp = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				3072000,	/* 3.072 MHz (?) */
				readmem,radarscp_writemem,null,null,
				nmi_interrupt,1
			),
			new MachineCPU(
				CPU_I8035 | CPU_AUDIO_CPU,
				6000000/15,	/* 6MHz crystal */
				readmem_sound,writemem_sound,readport_sound,writeport_sound,
				ignore_interrupt,1
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		dkong_gfxdecodeinfo,
		256+2, 64*4,	/* two extra colors for stars and radar grid */
		dkong_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		dkong_vh_start,
		generic_vh_stop,
		radarscp_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_DAC,
				dkong_dac_interface
			),
			new MachineSound(
				SOUND_SAMPLES,
				dkong_samples_interface
			)
		}
	);
	
	static MachineDriver machine_driver_dkong = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				3072000,	/* 3.072 MHz (?) */
				readmem,dkong_writemem,null,null,
				nmi_interrupt,1
			),
			new MachineCPU(
				CPU_I8035 | CPU_AUDIO_CPU,
				6000000/15,	/* 6MHz crystal */
				readmem_sound,writemem_sound,readport_sound,writeport_sound,
				ignore_interrupt,1
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		dkong_gfxdecodeinfo,
		256, 64*4,
		dkong_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		dkong_vh_start,
		generic_vh_stop,
		dkong_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_DAC,
				dkong_dac_interface
			),
			new MachineSound(
				SOUND_SAMPLES,
				dkong_samples_interface
			)
		}
	);
	
	public static InterruptPtr hunchbkd_interrupt = new InterruptPtr() { public int handler() 
	{
		return 0x03;	/* hunchbkd S2650 interrupt vector */
	} };
	
	static MachineDriver machine_driver_hunchbkd = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_S2650,
				3072000,
				hunchbkd_readmem,hunchbkd_writemem,hunchbkd_readport,hunchbkd_writeport,
				hunchbkd_interrupt,1
			),
	        new MachineCPU(
				CPU_I8035 | CPU_AUDIO_CPU,
				6000000/15,	/* 6MHz crystal */
				readmem_sound,writemem_sound,readport_hunchbkd_sound,writeport_sound,
				ignore_interrupt,1
			)
	    },
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		dkong_gfxdecodeinfo,
		256, 64*4,
		dkong_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		dkong_vh_start,
		generic_vh_stop,
		dkong_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_DAC,
				dkong_dac_interface
			)
		}
	);
	
	public static InterruptPtr herbiedk_interrupt = new InterruptPtr() { public int handler() 
	{
		s2650_set_sense(0);
		return ignore_interrupt();
	} };
	
	static MachineDriver machine_driver_herbiedk = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_S2650,
				3072000,
				hunchbkd_readmem,hunchbkd_writemem,herbiedk_readport,hunchbkd_writeport,
				herbiedk_interrupt,1
			),
	        new MachineCPU(
				CPU_I8035 | CPU_AUDIO_CPU,
				6000000/15,	/* 6MHz crystal */
				readmem_sound,writemem_sound,readport_hunchbkd_sound,writeport_sound,
				ignore_interrupt,1
			)
	    },
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		dkong_gfxdecodeinfo,
		256, 64*4,
		dkong_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		dkong_vh_start,
		generic_vh_stop,
		dkong_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_DAC,
				dkong_dac_interface
			)
		}
	);
	
	static MachineDriver machine_driver_dkongjr = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				3072000,	/* 3.072 MHz (?) */
				readmem,dkongjr_writemem,null,null,
				nmi_interrupt,1
			),
			new MachineCPU(
				CPU_I8035 | CPU_AUDIO_CPU,
				6000000/15,	/* 6MHz crystal */
				readmem_sound,writemem_sound,readport_sound,writeport_sound,
				ignore_interrupt,1
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		dkongjr_gfxdecodeinfo,
		256, 64*4,
		dkong_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		dkong_vh_start,
		generic_vh_stop,
		dkong_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_DAC,
				dkong_dac_interface
			),
			new MachineSound(
				SOUND_SAMPLES,
				dkongjr_samples_interface
			)
		}
	);
	
	
	
	static NESinterface nes_interface = new NESinterface
	(
		2,
		new int[] { REGION_CPU2, REGION_CPU3 },
		new int[] { 50, 50 },
	);
	
	
	static MachineDriver machine_driver_dkong3 = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				8000000/2,	/* 4 MHz */
				dkong3_readmem,dkong3_writemem,null,dkong3_writeport,
				nmi_interrupt,1
			),
			new MachineCPU(
				CPU_N2A03 | CPU_AUDIO_CPU,
				N2A03_DEFAULTCLOCK,
				dkong3_sound1_readmem,dkong3_sound1_writemem,null,null,
				nmi_interrupt,1
			),
			new MachineCPU(
				CPU_N2A03 | CPU_AUDIO_CPU,
				N2A03_DEFAULTCLOCK,
				dkong3_sound2_readmem,dkong3_sound2_writemem,null,null,
				nmi_interrupt,1
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		dkong3_gfxdecodeinfo,
		256, 64*4,
		dkong3_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		dkong_vh_start,
		generic_vh_stop,
		dkong_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_NES,
				nes_interface
			)
		}
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_radarscp = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "trs2c5fc",     0x0000, 0x1000, 0x40949e0d );	ROM_LOAD( "trs2c5gc",     0x1000, 0x1000, 0xafa8c49f );	ROM_LOAD( "trs2c5hc",     0x2000, 0x1000, 0x51b8263d );	ROM_LOAD( "trs2c5kc",     0x3000, 0x1000, 0x1f0101f7 );	/* space for diagnostic ROM */
	
		ROM_REGION( 0x1000, REGION_CPU2 );/* sound */
		ROM_LOAD( "trs2s3i",      0x0000, 0x0800, 0x78034f14 );	/* socket 3J is empty */
	
		ROM_REGION( 0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "trs2v3gc",     0x0000, 0x0800, 0xf095330e );	ROM_LOAD( "trs2v3hc",     0x0800, 0x0800, 0x15a316f0 );
		ROM_REGION( 0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "trs2v3dc",     0x0000, 0x0800, 0xe0bb0db9 );	ROM_LOAD( "trs2v3cc",     0x0800, 0x0800, 0x6c4e7dad );	ROM_LOAD( "trs2v3bc",     0x1000, 0x0800, 0x6fdd63f1 );	ROM_LOAD( "trs2v3ac",     0x1800, 0x0800, 0xbbf62755 );
		ROM_REGION( 0x0800, REGION_GFX3 );/* radar/star timing table */
		ROM_LOAD( "trs2v3ec",     0x0000, 0x0800, 0x0eca8d6b );
		ROM_REGION( 0x0300, REGION_PROMS );	ROM_LOAD( "rs2-x.xxx",    0x0000, 0x0100, 0x54609d61 );/* palette low 4 bits (inverted) */
		ROM_LOAD( "rs2-c.xxx",    0x0100, 0x0100, 0x79a7d831 );/* palette high 4 bits (inverted) */
		ROM_LOAD( "rs2-v.1hc",    0x0200, 0x0100, 0x1b828315 );/* character color codes on a per-column basis */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_dkong = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "dk.5e",        0x0000, 0x1000, 0xba70b88b );	ROM_LOAD( "dk.5c",        0x1000, 0x1000, 0x5ec461ec );	ROM_LOAD( "dk.5b",        0x2000, 0x1000, 0x1c97d324 );	ROM_LOAD( "dk.5a",        0x3000, 0x1000, 0xb9005ac0 );	/* space for diagnostic ROM */
	
		ROM_REGION( 0x1000, REGION_CPU2 );/* sound */
		ROM_LOAD( "dk.3h",        0x0000, 0x0800, 0x45a4ed06 );	ROM_LOAD( "dk.3f",        0x0800, 0x0800, 0x4743fe92 );
		ROM_REGION( 0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "dk.3n",        0x0000, 0x0800, 0x12c8c95d );	ROM_LOAD( "dk.3p",        0x0800, 0x0800, 0x15e9c5e9 );
		ROM_REGION( 0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "dk.7c",        0x0000, 0x0800, 0x59f8054d );	ROM_LOAD( "dk.7d",        0x0800, 0x0800, 0x672e4714 );	ROM_LOAD( "dk.7e",        0x1000, 0x0800, 0xfeaa59ee );	ROM_LOAD( "dk.7f",        0x1800, 0x0800, 0x20f2ef7e );
		ROM_REGION( 0x0300, REGION_PROMS );	ROM_LOAD( "dkong.2k",     0x0000, 0x0100, 0x1e82d375 );/* palette low 4 bits (inverted) */
		ROM_LOAD( "dkong.2j",     0x0100, 0x0100, 0x2ab01dc8 );/* palette high 4 bits (inverted) */
		ROM_LOAD( "dkong.5f",     0x0200, 0x0100, 0x44988665 );/* character color codes on a per-column basis */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_dkongjp = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "5f.cpu",       0x0000, 0x1000, 0x424f2b11 );	ROM_LOAD( "5g.cpu",       0x1000, 0x1000, 0xd326599b );	ROM_LOAD( "5h.cpu",       0x2000, 0x1000, 0xff31ac89 );	ROM_LOAD( "5k.cpu",       0x3000, 0x1000, 0x394d6007 );
		ROM_REGION( 0x1000, REGION_CPU2 );/* sound */
		ROM_LOAD( "dk.3h",        0x0000, 0x0800, 0x45a4ed06 );	ROM_LOAD( "dk.3f",        0x0800, 0x0800, 0x4743fe92 );
		ROM_REGION( 0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "dk.3n",        0x0000, 0x0800, 0x12c8c95d );	ROM_LOAD( "5k.vid",       0x0800, 0x0800, 0x3684f914 );
		ROM_REGION( 0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "dk.7c",        0x0000, 0x0800, 0x59f8054d );	ROM_LOAD( "dk.7d",        0x0800, 0x0800, 0x672e4714 );	ROM_LOAD( "dk.7e",        0x1000, 0x0800, 0xfeaa59ee );	ROM_LOAD( "dk.7f",        0x1800, 0x0800, 0x20f2ef7e );
		ROM_REGION( 0x0300, REGION_PROMS );	ROM_LOAD( "dkong.2k",     0x0000, 0x0100, 0x1e82d375 );/* palette low 4 bits (inverted) */
		ROM_LOAD( "dkong.2j",     0x0100, 0x0100, 0x2ab01dc8 );/* palette high 4 bits (inverted) */
		ROM_LOAD( "dkong.5f",     0x0200, 0x0100, 0x44988665 );/* character color codes on a per-column basis */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_dkongjpo = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "5f.cpu",       0x0000, 0x1000, 0x424f2b11 );	ROM_LOAD( "5g.cpu",       0x1000, 0x1000, 0xd326599b );	ROM_LOAD( "5h.bin",       0x2000, 0x1000, 0x1d28895d );	ROM_LOAD( "5k.bin",       0x3000, 0x1000, 0x7961599c );
		ROM_REGION( 0x1000, REGION_CPU2 );/* sound */
		ROM_LOAD( "dk.3h",        0x0000, 0x0800, 0x45a4ed06 );	ROM_LOAD( "dk.3f",        0x0800, 0x0800, 0x4743fe92 );
		ROM_REGION( 0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "dk.3n",        0x0000, 0x0800, 0x12c8c95d );	ROM_LOAD( "5k.vid",       0x0800, 0x0800, 0x3684f914 );
		ROM_REGION( 0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "dk.7c",        0x0000, 0x0800, 0x59f8054d );	ROM_LOAD( "dk.7d",        0x0800, 0x0800, 0x672e4714 );	ROM_LOAD( "dk.7e",        0x1000, 0x0800, 0xfeaa59ee );	ROM_LOAD( "dk.7f",        0x1800, 0x0800, 0x20f2ef7e );
		ROM_REGION( 0x0300, REGION_PROMS );	ROM_LOAD( "dkong.2k",     0x0000, 0x0100, 0x1e82d375 );/* palette low 4 bits (inverted) */
		ROM_LOAD( "dkong.2j",     0x0100, 0x0100, 0x2ab01dc8 );/* palette high 4 bits (inverted) */
		ROM_LOAD( "dkong.5f",     0x0200, 0x0100, 0x44988665 );/* character color codes on a per-column basis */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_dkongjr = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "dkj.5b",       0x0000, 0x1000, 0xdea28158 );	ROM_CONTINUE(             0x3000, 0x1000 );	ROM_LOAD( "dkj.5c",       0x2000, 0x0800, 0x6fb5faf6 );	ROM_CONTINUE(             0x4800, 0x0800 );	ROM_CONTINUE(             0x1000, 0x0800 );	ROM_CONTINUE(             0x5800, 0x0800 );	ROM_LOAD( "dkj.5e",       0x4000, 0x0800, 0xd042b6a8 );	ROM_CONTINUE(             0x2800, 0x0800 );	ROM_CONTINUE(             0x5000, 0x0800 );	ROM_CONTINUE(             0x1800, 0x0800 );
		ROM_REGION( 0x1000, REGION_CPU2 );/* sound */
		ROM_LOAD( "dkj.3h",       0x0000, 0x1000, 0x715da5f8 );
		ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "dkj.3n",       0x0000, 0x1000, 0x8d51aca9 );	ROM_LOAD( "dkj.3p",       0x1000, 0x1000, 0x4ef64ba5 );
		ROM_REGION( 0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "dkj.7c",       0x0000, 0x0800, 0xdc7f4164 );	ROM_LOAD( "dkj.7d",       0x0800, 0x0800, 0x0ce7dcf6 );	ROM_LOAD( "dkj.7e",       0x1000, 0x0800, 0x24d1ff17 );	ROM_LOAD( "dkj.7f",       0x1800, 0x0800, 0x0f8c083f );
		ROM_REGION( 0x0300, REGION_PROMS );	ROM_LOAD( "dkjrprom.2e",  0x0000, 0x0100, 0x463dc7ad );/* palette low 4 bits (inverted) */
		ROM_LOAD( "dkjrprom.2f",  0x0100, 0x0100, 0x47ba0042 );/* palette high 4 bits (inverted) */
		ROM_LOAD( "dkjrprom.2n",  0x0200, 0x0100, 0xdbf185bf );/* character color codes on a per-column basis */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_dkngjrjp = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "dkjr1",        0x0000, 0x1000, 0xec7e097f );	ROM_CONTINUE(             0x3000, 0x1000 );	ROM_LOAD( "dkjr2",        0x2000, 0x0800, 0xc0a18f0d );	ROM_CONTINUE(             0x4800, 0x0800 );	ROM_CONTINUE(             0x1000, 0x0800 );	ROM_CONTINUE(             0x5800, 0x0800 );	ROM_LOAD( "dkjr3",        0x4000, 0x0800, 0xa81dd00c );	ROM_CONTINUE(             0x2800, 0x0800 );	ROM_CONTINUE(             0x5000, 0x0800 );	ROM_CONTINUE(             0x1800, 0x0800 );
		ROM_REGION( 0x1000, REGION_CPU2 );/* sound */
		ROM_LOAD( "dkj.3h",       0x0000, 0x1000, 0x715da5f8 );
		ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "dkjr9",        0x0000, 0x1000, 0xa95c4c63 );	ROM_LOAD( "dkjr10",       0x1000, 0x1000, 0xadc11322 );
		ROM_REGION( 0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "dkj.7c",       0x0000, 0x0800, 0xdc7f4164 );	ROM_LOAD( "dkj.7d",       0x0800, 0x0800, 0x0ce7dcf6 );	ROM_LOAD( "dkj.7e",       0x1000, 0x0800, 0x24d1ff17 );	ROM_LOAD( "dkj.7f",       0x1800, 0x0800, 0x0f8c083f );
		ROM_REGION( 0x0300, REGION_PROMS );	ROM_LOAD( "dkjrprom.2e",  0x0000, 0x0100, 0x463dc7ad );/* palette low 4 bits (inverted) */
		ROM_LOAD( "dkjrprom.2f",  0x0100, 0x0100, 0x47ba0042 );/* palette high 4 bits (inverted) */
		ROM_LOAD( "dkjrprom.2n",  0x0200, 0x0100, 0xdbf185bf );/* character color codes on a per-column basis */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_dkjrjp = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "dkjp.5b",      0x0000, 0x1000, 0x7b48870b );	ROM_CONTINUE(             0x3000, 0x1000 );	ROM_LOAD( "dkjp.5c",      0x2000, 0x0800, 0x12391665 );	ROM_CONTINUE(             0x4800, 0x0800 );	ROM_CONTINUE(             0x1000, 0x0800 );	ROM_CONTINUE(             0x5800, 0x0800 );	ROM_LOAD( "dkjp.5e",      0x4000, 0x0800, 0x6c9f9103 );	ROM_CONTINUE(             0x2800, 0x0800 );	ROM_CONTINUE(             0x5000, 0x0800 );	ROM_CONTINUE(             0x1800, 0x0800 );
		ROM_REGION( 0x1000, REGION_CPU2 );/* sound */
		ROM_LOAD( "dkj.3h",       0x0000, 0x1000, 0x715da5f8 );
		ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "dkj.3n",       0x0000, 0x1000, 0x8d51aca9 );	ROM_LOAD( "dkj.3p",       0x1000, 0x1000, 0x4ef64ba5 );
		ROM_REGION( 0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "dkj.7c",       0x0000, 0x0800, 0xdc7f4164 );	ROM_LOAD( "dkj.7d",       0x0800, 0x0800, 0x0ce7dcf6 );	ROM_LOAD( "dkj.7e",       0x1000, 0x0800, 0x24d1ff17 );	ROM_LOAD( "dkj.7f",       0x1800, 0x0800, 0x0f8c083f );
		ROM_REGION( 0x0300, REGION_PROMS );	ROM_LOAD( "dkjrprom.2e",  0x0000, 0x0100, 0x463dc7ad );/* palette low 4 bits (inverted) */
		ROM_LOAD( "dkjrprom.2f",  0x0100, 0x0100, 0x47ba0042 );/* palette high 4 bits (inverted) */
		ROM_LOAD( "dkjrprom.2n",  0x0200, 0x0100, 0xdbf185bf );/* character color codes on a per-column basis */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_dkjrbl = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "djr1-c.5b",    0x0000, 0x1000, 0xffe9e1a5 );	ROM_CONTINUE(             0x3000, 0x1000 );	ROM_LOAD( "djr1-c.5c",    0x2000, 0x0800, 0x982e30e8 );	ROM_CONTINUE(             0x4800, 0x0800 );	ROM_CONTINUE(             0x1000, 0x0800 );	ROM_CONTINUE(             0x5800, 0x0800 );	ROM_LOAD( "djr1-c.5e",    0x4000, 0x0800, 0x24c3d325 );	ROM_CONTINUE(             0x2800, 0x0800 );	ROM_CONTINUE(             0x5000, 0x0800 );	ROM_CONTINUE(             0x1800, 0x0800 );	ROM_LOAD( "djr1-c.5a",    0x8000, 0x1000, 0xbb5f5180 );
		ROM_REGION( 0x1000, REGION_CPU2 );/* sound */
		ROM_LOAD( "dkj.3h",       0x0000, 0x1000, 0x715da5f8 );
		ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "dkj.3n",       0x0000, 0x1000, 0x8d51aca9 );	ROM_LOAD( "dkj.3p",       0x1000, 0x1000, 0x4ef64ba5 );
		ROM_REGION( 0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "dkj.7c",       0x0000, 0x0800, 0xdc7f4164 );	ROM_LOAD( "dkj.7d",       0x0800, 0x0800, 0x0ce7dcf6 );	ROM_LOAD( "dkj.7e",       0x1000, 0x0800, 0x24d1ff17 );	ROM_LOAD( "dkj.7f",       0x1800, 0x0800, 0x0f8c083f );
		ROM_REGION( 0x0300, REGION_PROMS );	ROM_LOAD( "dkjrprom.2e",  0x0000, 0x0100, 0x463dc7ad );/* palette low 4 bits (inverted) */
		ROM_LOAD( "dkjrprom.2f",  0x0100, 0x0100, 0x47ba0042 );/* palette high 4 bits (inverted) */
		ROM_LOAD( "dkjrprom.2n",  0x0200, 0x0100, 0xdbf185bf );/* character color codes on a per-column basis */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_dkong3 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "dk3c.7b",      0x0000, 0x2000, 0x38d5f38e );	ROM_LOAD( "dk3c.7c",      0x2000, 0x2000, 0xc9134379 );	ROM_LOAD( "dk3c.7d",      0x4000, 0x2000, 0xd22e2921 );	ROM_LOAD( "dk3c.7e",      0x8000, 0x2000, 0x615f14b7 );
		ROM_REGION( 0x10000, REGION_CPU2 );/* sound #1 */
		ROM_LOAD( "dk3c.5l",      0xe000, 0x2000, 0x7ff88885 );
		ROM_REGION( 0x10000, REGION_CPU3 );/* sound #2 */
		ROM_LOAD( "dk3c.6h",      0xe000, 0x2000, 0x36d7200c );
		ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "dk3v.3n",      0x0000, 0x1000, 0x415a99c7 );	ROM_LOAD( "dk3v.3p",      0x1000, 0x1000, 0x25744ea0 );
		ROM_REGION( 0x4000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "dk3v.7c",      0x0000, 0x1000, 0x8ffa1737 );	ROM_LOAD( "dk3v.7d",      0x1000, 0x1000, 0x9ac84686 );	ROM_LOAD( "dk3v.7e",      0x2000, 0x1000, 0x0c0af3fb );	ROM_LOAD( "dk3v.7f",      0x3000, 0x1000, 0x55c58662 );
		ROM_REGION( 0x0300, REGION_PROMS );	ROM_LOAD( "dkc1-c.1d",    0x0000, 0x0200, 0xdf54befc );/* palette red & green component */
		ROM_LOAD( "dkc1-c.1c",    0x0100, 0x0200, 0x66a77f40 );/* palette blue component */
		ROM_LOAD( "dkc1-v.2n",    0x0200, 0x0100, 0x50e33434 );/* character color codes on a per-column basis */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_dkong3j = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "dk3c.7b",      0x0000, 0x2000, 0x38d5f38e );	ROM_LOAD( "dk3c.7c",      0x2000, 0x2000, 0xc9134379 );	ROM_LOAD( "dk3c.7d",      0x4000, 0x2000, 0xd22e2921 );	ROM_LOAD( "dk3cj.7e",     0x8000, 0x2000, 0x25b5be23 );
		ROM_REGION( 0x10000, REGION_CPU2 );/* sound #1 */
		ROM_LOAD( "dk3c.5l",      0xe000, 0x2000, 0x7ff88885 );
		ROM_REGION( 0x10000, REGION_CPU3 );/* sound #2 */
		ROM_LOAD( "dk3c.6h",      0xe000, 0x2000, 0x36d7200c );
		ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "dk3v.3n",      0x0000, 0x1000, 0x415a99c7 );	ROM_LOAD( "dk3v.3p",      0x1000, 0x1000, 0x25744ea0 );
		ROM_REGION( 0x4000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "dk3v.7c",      0x0000, 0x1000, 0x8ffa1737 );	ROM_LOAD( "dk3v.7d",      0x1000, 0x1000, 0x9ac84686 );	ROM_LOAD( "dk3v.7e",      0x2000, 0x1000, 0x0c0af3fb );	ROM_LOAD( "dk3v.7f",      0x3000, 0x1000, 0x55c58662 );
		ROM_REGION( 0x0300, REGION_PROMS );	ROM_LOAD( "dkc1-c.1d",    0x0000, 0x0200, 0xdf54befc );/* palette red & green component */
		ROM_LOAD( "dkc1-c.1c",    0x0100, 0x0200, 0x66a77f40 );/* palette blue component */
		ROM_LOAD( "dkc1-v.2n",    0x0200, 0x0100, 0x50e33434 );/* character color codes on a per-column basis */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_hunchbkd = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x8000, REGION_CPU1 );/* 32k for code */
		ROM_LOAD( "hb.5e",        0x0000, 0x1000, 0x4c3ac070 );	ROM_LOAD( "hbsc-1.5c",    0x2000, 0x1000, 0x9b0e6234 );	ROM_LOAD( "hb.5b",        0x4000, 0x1000, 0x4cde80f3 );	ROM_LOAD( "hb.5a",        0x6000, 0x1000, 0xd60ef5b2 );
		ROM_REGION( 0x1000, REGION_CPU2 );/* sound */
		ROM_LOAD( "hb.3h",        0x0000, 0x0800, 0xa3c240d4 );
		ROM_REGION( 0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "hb.3n",        0x0000, 0x0800, 0x443ed5ac );	ROM_LOAD( "hb.3p",        0x0800, 0x0800, 0x073e7b0c );
		ROM_REGION( 0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "hb.7c",        0x0000, 0x0800, 0x3ba71686 );	ROM_LOAD( "hb.7d",        0x0800, 0x0800, 0x5786948d );	ROM_LOAD( "hb.7e",        0x1000, 0x0800, 0xf845e8ca );	ROM_LOAD( "hb.7f",        0x1800, 0x0800, 0x52d20fea );
		ROM_REGION( 0x0300, REGION_PROMS );	ROM_LOAD( "hbprom.2e",    0x0000, 0x0100, 0x37aab98f );/* palette low 4 bits (inverted) */
		ROM_LOAD( "hbprom.2f",    0x0100, 0x0100, 0x845b8dcc );/* palette high 4 bits (inverted) */
		ROM_LOAD( "hbprom.2n",    0x0200, 0x0100, 0xdff9070a );/* character color codes on a per-column basis */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_herbiedk = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x8000, REGION_CPU1 );/* 32k for code */
		ROM_LOAD( "5f.cpu",        0x0000, 0x1000, 0xc7ab3ac6 );	ROM_LOAD( "5g.cpu",        0x2000, 0x1000, 0xd1031aa6 );	ROM_LOAD( "5h.cpu",        0x4000, 0x1000, 0xc0daf551 );	ROM_LOAD( "5k.cpu",        0x6000, 0x1000, 0x67442242 );
		ROM_REGION( 0x1000, REGION_CPU2 );/* sound */
		ROM_LOAD( "3i.snd",        0x0000, 0x0800, 0x20e30406 );
		ROM_REGION( 0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "5h.vid",        0x0000, 0x0800, 0xea2a2547 );	ROM_LOAD( "5k.vid",        0x0800, 0x0800, 0xa8d421c9 );
		ROM_REGION( 0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "7c.clk",        0x0000, 0x0800, 0xaf646166 );	ROM_LOAD( "7d.clk",        0x0800, 0x0800, 0xd8e15832 );	ROM_LOAD( "7e.clk",        0x1000, 0x0800, 0x2f7e65fa );	ROM_LOAD( "7f.clk",        0x1800, 0x0800, 0xad32d5ae );
		ROM_REGION( 0x0300, REGION_PROMS );	ROM_LOAD( "74s287.2k",     0x0000, 0x0100, 0x7dc0a381 );/* palette high 4 bits (inverted) */
		ROM_LOAD( "74s287.2j",     0x0100, 0x0100, 0x0a440c00 );/* palette low 4 bits (inverted) */
		ROM_LOAD( "74s287.vid",    0x0200, 0x0100, 0x5a3446cc );/* character color codes on a per-column basis */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_herocast = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		/* the loading addresses are most likely wrong */
		/* the ROMs are probably not contiguous. */
		/* For example there's a table which suddenly stops at */
		/* 1dff and resumes at 3e00 */
		ROM_LOAD( "red-dot.rgt",  0x0000, 0x2000, 0x9c4af229 );/* encrypted */
		ROM_LOAD( "wht-dot.lft",  0x2000, 0x2000, 0xc10f9235 );/* encrypted */
		/* space for diagnostic ROM */
		ROM_LOAD( "2532.3f",      0x4000, 0x1000, 0x553b89bb );/* ??? contains unencrypted */
														/* code mapped at 3000 */
	
		ROM_REGION( 0x1000, REGION_CPU2 );/* sound */
		ROM_LOAD( "silver.3h",    0x0000, 0x0800, 0x67863ce9 );
		ROM_REGION( 0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "pnk.3n",       0x0000, 0x0800, 0x574dfd7a );	ROM_LOAD( "blk.3p",       0x0800, 0x0800, 0x16f7c040 );
		ROM_REGION( 0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "gold.7c",      0x0000, 0x0800, 0x5f5282ed );	ROM_LOAD( "orange.7d",    0x0800, 0x0800, 0x075d99f5 );	ROM_LOAD( "yellow.7e",    0x1000, 0x0800, 0xf6272e96 );	ROM_LOAD( "violet.7f",    0x1800, 0x0800, 0xca020685 );
		ROM_REGION( 0x0300, REGION_PROMS );	ROM_LOAD( "82s126.2e",    0x0000, 0x0100, 0x463dc7ad );/* palette low 4 bits (inverted) */
		ROM_LOAD( "82s126.2f",    0x0100, 0x0100, 0x47ba0042 );/* palette high 4 bits (inverted) */
		ROM_LOAD( "82s126.2n",    0x0200, 0x0100, 0x37aece4b );/* character color codes on a per-column basis */
	ROM_END(); }}; 
	
	
	
	static public static InitDriverPtr init_herocast = new InitDriverPtr() { public void handler() 
	{
		int A;
		UBytePtr RAM = memory_region(REGION_CPU1);
	
	
		/* swap data lines D3 and D4, this fixes the text but nothing more. */
		for (A = 0;A < 0x4000;A++)
		{
			int v;
	
			v = RAM[A];
			RAM[A] = (v & 0xe7) | ((v & 0x10) >> 1) | ((v & 0x08) << 1);
		}
	} };
	
	
	
	static public static InitDriverPtr init_radarscp = new InitDriverPtr() { public void handler() 
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
	
		/* TODO: Radarscope does a check on bit 6 of 7d00 which prevent it from working. */
		/* It's a sound status flag, maybe signaling when a tune is finished. */
		/* For now, we comment it out. */
		RAM[0x1e9c] = 0xc3;
		RAM[0x1e9d] = 0xbd;
	} };
	
	
	
	public static GameDriver driver_radarscp	   = new GameDriver("1980"	,"radarscp"	,"dkong.java"	,rom_radarscp,null	,machine_driver_radarscp	,input_ports_dkong	,init_radarscp	,ROT90	,	"Nintendo", "Radar Scope", GAME_IMPERFECT_SOUND )
	public static GameDriver driver_dkong	   = new GameDriver("1981"	,"dkong"	,"dkong.java"	,rom_dkong,null	,machine_driver_dkong	,input_ports_dkong	,null	,ROT90	,	"Nintendo of America", "Donkey Kong (US)" )
	public static GameDriver driver_dkongjp	   = new GameDriver("1981"	,"dkongjp"	,"dkong.java"	,rom_dkongjp,driver_dkong	,machine_driver_dkong	,input_ports_dkong	,null	,ROT90	,	"Nintendo", "Donkey Kong (Japan set 1)" )
	public static GameDriver driver_dkongjpo	   = new GameDriver("1981"	,"dkongjpo"	,"dkong.java"	,rom_dkongjpo,driver_dkong	,machine_driver_dkong	,input_ports_dkong	,null	,ROT90	,	"Nintendo", "Donkey Kong (Japan set 2)" )
	public static GameDriver driver_dkongjr	   = new GameDriver("1982"	,"dkongjr"	,"dkong.java"	,rom_dkongjr,null	,machine_driver_dkongjr	,input_ports_dkong	,null	,ROT90	,	"Nintendo of America", "Donkey Kong Junior (US)" )
	public static GameDriver driver_dkngjrjp	   = new GameDriver("1982"	,"dkngjrjp"	,"dkong.java"	,rom_dkngjrjp,driver_dkongjr	,machine_driver_dkongjr	,input_ports_dkong	,null	,ROT90	,	"bootleg?", "Donkey Kong Jr. (Original Japanese)" )
	public static GameDriver driver_dkjrjp	   = new GameDriver("1982"	,"dkjrjp"	,"dkong.java"	,rom_dkjrjp,driver_dkongjr	,machine_driver_dkongjr	,input_ports_dkong	,null	,ROT90	,	"Nintendo", "Donkey Kong Junior (Japan)" )
	public static GameDriver driver_dkjrbl	   = new GameDriver("1982"	,"dkjrbl"	,"dkong.java"	,rom_dkjrbl,driver_dkongjr	,machine_driver_dkongjr	,input_ports_dkong	,null	,ROT90	,	"Nintendo of America", "Donkey Kong Junior (bootleg?)" )
	public static GameDriver driver_dkong3	   = new GameDriver("1983"	,"dkong3"	,"dkong.java"	,rom_dkong3,null	,machine_driver_dkong3	,input_ports_dkong3	,null	,ROT90	,	"Nintendo of America", "Donkey Kong 3 (US)" )
	public static GameDriver driver_dkong3j	   = new GameDriver("1983"	,"dkong3j"	,"dkong.java"	,rom_dkong3j,driver_dkong3	,machine_driver_dkong3	,input_ports_dkong3	,null	,ROT90	,	"Nintendo", "Donkey Kong 3 (Japan)" )
	
	public static GameDriver driver_hunchbkd	   = new GameDriver("1983"	,"hunchbkd"	,"dkong.java"	,rom_hunchbkd,null	,machine_driver_hunchbkd	,input_ports_hunchbdk	,null	,ROT90	,	"Century", "Hunchback (Donkey Kong conversion)", GAME_WRONG_COLORS )
	public static GameDriver driver_herbiedk	   = new GameDriver("1984"	,"herbiedk"	,"dkong.java"	,rom_herbiedk,null	,machine_driver_herbiedk	,input_ports_herbiedk	,null	,ROT90	,	"CVS", "Herbie at the Olympics (DK conversion)", GAME_WRONG_COLORS )	/*"Seatongrove UK Ltd"*/
	public static GameDriver driver_herocast	   = new GameDriver("1984"	,"herocast"	,"dkong.java"	,rom_herocast,null	,machine_driver_dkong	,input_ports_dkong	,init_herocast	,ROT90	,	"Seatongrove (Crown license)", "herocast", GAME_NOT_WORKING )
}
