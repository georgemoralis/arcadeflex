/***************************************************************************

Taito F2 System

driver by David Graves, Bryan McPhail, Brad Oliver, Andrew Prime, Brian
Troha, Nicola Salmoria with some initial help from Richard Bush

The Taito F2 system is a fairly flexible hardware platform. The main board
supports three 64x64 tiled scrolling background planes of 8x8 tiles, and a
powerful sprite engine capable of handling all the video chores by itself
(used in e.g. Super Space Invaders). The front tilemap has characters which
are generated in RAM for maximum versatility (fading effects etc.).
The expansion board can have additional gfx chip e.g. for a zooming/rotating
tilemap, or additional tilemap planes.

Sound is handled by a Z80 with a YM2610 connected to it.

The memory map for each of the games is similar but not identical.

Notes:
- Metal Black has secret command to select stage.
  Start the machine with holding service switch.
  Then push 1p start, 1p start, 1p start, service SW, 1p start
  while error message is displayed.


Custom chips
------------
The old version of the F2 main board (larger) has
TC0100SCN (tilemaps)
TC0200OBJ+TC0210FBC (sprites)
TC0140SYT (sound communication & other stuff)

The new version has
TC0100SCN (tilemaps)
TC0540OBN+TC0520TBC (sprites)
TC0530SYC (sound communication & other stuff)

            I/O    Priority / Palette      Additional gfx                 Other
         --------- ------------------- ----------------------- ----------------------------
finalb   TC0220IOC TC0110PCR TC0070RGB
dondokod TC0220IOC TC0360PRI TC0260DAR TC0280GRD(x2)(zoom/rot)
megab    TC0220IOC TC0360PRI TC0260DAR                         TC0030CMD(C-Chip protection)
thundfox TC0220IOC TC0360PRI TC0260DAR TC0100SCN (so it has two)
cameltry TC0220IOC TC0360PRI TC0260DAR TC0280GRD(x2)(zoom/rot)
qtorimon TC0220IOC TC0110PCR TC0070RGB
liquidk  TC0220IOC TC0360PRI TC0260DAR
quizhq   TMP82C265 TC0110PCR TC0070RGB
ssi      TC0510NIO 			 TC0260DAR
gunfront TC0510NIO TC0360PRI TC0260DAR
growl    TMP82C265 TC0360PRI TC0260DAR                         TC0190FMC(4 players input?sprite banking?)
mjnquest           TC0110PCR TC0070RGB
footchmp TE7750    TC0360PRI TC0260DAR TC0480SCP(tilemaps)     TC0190FMC(4 players input?sprite banking?)
koshien  TC0510NIO TC0360PRI TC0260DAR
yuyugogo TC0510NIO 			 TC0260DAR
ninjak   TE7750    TC0360PRI TC0260DAR                         TC0190FMC(4 players input?sprite banking?)
solfigtr ?         TC0360PRI TC0260DAR ?
qzquest  TC0510NIO 			 TC0260DAR
pulirula TC0510NIO TC0360PRI TC0260DAR TC0430GRW(zoom/rot)
metalb   TC0510NIO TC0360PRI TC0260DAR TC0480SCP(tilemaps)
qzchikyu TC0510NIO 			 TC0260DAR
yesnoj   TMP82C265           TC0260DAR                         TC8521AP(RTC?)
deadconx           TC0360PRI TC0260DAR TC0480SCP(tilemaps)     TC0190FMC(4 players input?sprite banking?)
dinorex  TC0510NIO TC0360PRI TC0260DAR
qjinsei  TC0510NIO TC0360PRI TC0260DAR
qcrayon  TC0510NIO TC0360PRI TC0260DAR
qcrayon2 TC0510NIO TC0360PRI TC0260DAR
driftout TC0510NIO TC0360PRI TC0260DAR TC0430GRW(zoom/rot)



F2 Game List
------------
. Final Blow                                                                       (1)
. Don Doko Don                                                                     (2)
. Mega Blast               http://www.taito.co.jp/game-history/80b/megabla.html    (3)
. Quiz Torimonochou        http://www.taito.co.jp/game-history/90a/qui_tori.html   (4)
. Quiz HQ                  http://www.taito.co.jp/game-history/90a/quiz_hq.html
. Thunder Fox              http://www.taito.co.jp/game-history/90a/thu_fox.html
. Liquid Kids              http://www.taito.co.jp/game-history/90a/miz_bak.html    (7)
. SSI / Majestic 12        http://www.taito.co.jp/game-history/90a/mj12.html       (8)
. Gun Frontier             http://www.taito.co.jp/game-history/90a/gunfro.html     (9)
. Growl / Runark           http://www.taito.co.jp/game-history/90a/runark.html    (10)
. Hat Trick Hero           http://www.taito.co.jp/game-history/90a/hthero.html    (11)
. Mahjong Quest            http://www.taito.co.jp/game-history/90a/mahque.html    (12)
. Yuu-yu no Quiz de Go!Go! http://www.taito.co.jp/game-history/90a/youyu.html     (13)
. Ah Eikou no Koshien      http://www.taito.co.jp/game-history/90a/koshien.html   (14)
. Ninja Kids               http://www.taito.co.jp/game-history/90a/ninjakids.html (15)
. Quiz Quest               http://www.taito.co.jp/game-history/90a/q_quest.html
. Metal Black              http://www.taito.co.jp/game-history/90a/metabla.html
. Quiz Chikyu Boueigun     http://www.taito.co.jp/game-history/90a/qui_tik.html
. Dinorex                  http://www.taito.co.jp/game-history/90a/dinorex.html
. Pulirula
. Dead Connection          http://www.taito.co.jp/game-history/90a/deadconn.html
. Quiz Jinsei Gekijou      http://www.taito.co.jp/game-history/90a/qui_jin.html
. Quiz Crayon Shinchan     http://www.taito.co.jp/game-history/90a/qcrashin.html
. Crayon Shinchan Orato Asobo


This list is translated version of
http://www.aianet.or.jp/~eisetu/rom/rom_tait.html
This page also contains info for other Taito boards.

F2 Motherboard ( Big ) K1100432A, J1100183A
               (Small) K1100608A, J1100242A

Apr.1989 Final Blow (B82, M4300123A, K1100433A)
Jul.1989 Don Doko Don (B95, M4300131A, K1100454A, J1100195A)
Oct.1989 Mega Blast (C11)
Feb.1990 Quiz Torimonochou (C41, K1100554A)
Apr.1990 Cameltry (C38, M4300167A, K1100556A)
Jul.1990 Quiz H.Q. (C53, K1100594A)
Aug.1990 Thunder Fox (C28, M4300181A, K1100580A) (exists in F1 version too)
Sep.1990 Liquid Kids/Mizubaku Daibouken (C49, K1100593A)
Nov.1990 MJ-12/Super Space Invaders (C64, M4300195A, K1100616A, J1100248A)
Jan.1991 Gun Frontier (C71, M4300199A, K1100625A, K1100629A(overseas))
Feb.1991 Growl/Runark (C74, M4300210A, K1100639A)
Mar.1991 Hat Trick Hero/Euro Football Championship (C80, K11J0646A)
Mar.1991 Yuu-yu no Quiz de Go!Go! (C83, K11J0652A)
Apr.1991 Ah Eikou no Koshien (C81, M43J0214A, K11J654A)
Apr.1991 Ninja Kids (C85, M43J0217A, K11J0659A)
May.1991 Mahjong Quest (C77, K1100637A, K1100637B)
Jul.1991 Quiz Quest (C92, K11J0678A)
Sep.1991 Metal Black (D12)
Oct.1991 Drift Out (Visco) (M43X0241A, K11X0695A)
Nov.1991 PuLiRuLa (C98, M43J0225A, K11J0672A)
Feb.1992 Quiz Chikyu Boueigun (D19, K11J0705A)
Jul.1992 Dead Connection (D28, K11J0715A)
Nov.1992 Dinorex (D39, K11J0254A)
Mar.1993 Quiz Jinsei Gekijou (D48, M43J0262A, K11J0742A)
Aug.1993 Quiz Crayon Shinchan (D55, K11J0758A)
Dec.1993 Crayon Shinchan Orato Asobo (D63, M43J0276A, K11J0779A)

Mar.1992 Yes.No. Shinri Tokimeki Chart (Fortune teller machine) (D20, K11J0706B)

Thunder Fox, Drift Out, "Quiz Crayon Shinchan", and "Crayon Shinchan
Orato Asobo" has "Not F2" version PCB.
Foreign version of Cameltry uses different hardware (B89's PLD,
K1100573A, K1100574A).




Sprite extension area types
===========================

These games need a special value for f2_spriteext:

Yuyugogo = 1
Pulirula = 2
Dinorex = 3
Quiz Crayon 1&2 = 3
Quiz Jinsei = 3
(all other games need it to be zero)

TODO Lists
==========

- The sprite system is still partly a mystery, and not an accurate emulation.
  A lot of sprite glitches are caused by data in sprite ram not being correct,
  part from one frame and part from the previous one. There has to be some
  buffering inside the chip but it's not clear how. See below the irq section
  for a long list of observations on sprite glitches.

- TC0480SCP emulation (footchmp, metalb, deadconx) has incorrect zoom.

- Some DIPS are wrong [many in the Japanese quiz games].

- Macros for common input port definitions.


Dondokod
--------

Roz layer is one pixel out vertically when screen flipped.


Pulirula
--------

In level 3, the mask sprites used for the door are misaligned by one pixel to
the left.


Cameltru
--------

Missing text layer; instead of using the normal TC0100SCN text layer,
the program writes char data to 811000-811fff and tilemap to 812000-813fff.
The tilemap is 128x32 instead of the usual 64x64.


Driftout
--------

Sprites don't stay flipped in screenflip (watch attract).


Driveout
--------

No sound, I think it uses different hardware.


Gun Frontier
------------

There are mask sprites used on the waterfall in the first round
of attract demo, however it's not clear what they should mask since
there don't seem to be sprites below them. Shadow maybe?


Metal Black
-----------

Tilemap screenflip support has a possible issue; the blue planet in early
attract should be 1 pixel left.


Yesnoj
------

Input mapping incomplete (there's a 0x02 one which only seems to be
used in what is probably test mode).

The end summary is black on black on half of the screen. Is there a
bg color selector somewhere?

Test mode (?) sounds some alarm and shows a few japanese chars (black
on black again).

The timer stays at 00:00. Missing RTC emulation?

Only first section of scr rom decodes properly at 1bpp: rest looked
plausible at 4bpp (but pixel order not the standard layout). Could
the rest be data for the 68000??


Quiz Crayon 2
-------------

There should be a highlight circle around the player while it moves on the
map. This is done by a sprite which doesn't have priority over the
background. This is probably the same thing as the waterfall in Gun Frontier.


***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class taito_f2
{
	
	
	
	
	extern UBytePtr f2_sprite_extension;
	extern size_t f2_spriteext_size;
	
	int mjnquest_input;
	int yesnoj_dsw = 0;
	
	
	
	//
	
	extern UBytePtr cchip_ram;
	
	
	
	public static WriteHandlerPtr TC0360PRI_halfword_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
		{
			TC0360PRI_w(offset >> 1,data & 0xff);
	if ((data & 0xff00) != 0)
	logerror("CPU #0 PC %06x: warning - write %02x to MSB of TC0360PRI address %02x\n",cpu_get_pc(),data,offset);
		}
		else
		{
	logerror("CPU #0 PC %06x: warning - write %02x to MSB of TC0360PRI address %02x\n",cpu_get_pc(),data,offset);
		}
	} };
	
	public static WriteHandlerPtr TC0360PRI_halfword_swap_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0xff000000) == 0)
		{
			TC0360PRI_w(offset >> 1,(data >> 8) & 0xff);
	if ((data & 0xff) != 0)
	logerror("CPU #0 PC %06x: warning - write %02x to LSB of TC0360PRI address %02x\n",cpu_get_pc(),data,offset);
		}
		else
		{
	logerror("CPU #0 PC %06x: warning - write %02x to LSB of TC0360PRI address %02x\n",cpu_get_pc(),data,offset);
		}
	} };
	
	/**********************************************************
				GAME INPUTS
	**********************************************************/
	
	public static ReadHandlerPtr TC0220IOC_halfword_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return TC0220IOC_r(offset >> 1);
	} };
	
	public static WriteHandlerPtr TC0220IOC_halfword_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			TC0220IOC_w(offset >> 1,data & 0xff);
		else
		{
			/* qtorimon writes here the coin counters - bug? */
	logerror("CPU #0 PC %06x: warning - write to MSB of TC0220IOC address %02x\n",cpu_get_pc(),offset);
			TC0220IOC_w(offset >> 1,(data >> 8) & 0xff);
		}
	} };
	
	public static ReadHandlerPtr TC0510NIO_halfword_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return TC0510NIO_r(offset >> 1);
	} };
	
	public static WriteHandlerPtr TC0510NIO_halfword_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			TC0510NIO_w(offset >> 1,data & 0xff);
		else
		{
			/* driftout writes here the coin counters - bug? */
	logerror("CPU #0 PC %06x: warning - write to MSB of TC0510NIO address %02x\n",cpu_get_pc(),offset);
			TC0510NIO_w(offset >> 1,(data >> 8) & 0xff);
		}
	} };
	
	public static ReadHandlerPtr TC0510NIO_halfword_wordswap_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return TC0510NIO_halfword_r(offset ^ 2);
	} };
	
	public static WriteHandlerPtr TC0510NIO_halfword_wordswap_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		TC0510NIO_halfword_w(offset ^ 2,data);
	} };
	
	public static ReadHandlerPtr growl_dsw_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    switch (offset)
	    {
	         case 0x00:
	              return readinputport(3); /* DSW A */
	
	         case 0x02:
	              return readinputport(4); /* DSW B */
	    }
	
	logerror("CPU #0 dsw_r offset %06x: warning - read unmapped memory address %06x\n",cpu_get_pc(),offset);
	
		return 0xff;
	} };
	
	public static ReadHandlerPtr growl_input_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    switch (offset)
	    {
	         case 0x00:
	              return readinputport(0); /* IN0 */
	
	         case 0x02:
	              return readinputport(1); /* IN1 */
	
	         case 0x04:
	              return readinputport(2); /* IN2 */
	
	    }
	
	logerror("CPU #0 input_r offset %06x: warning - read unmapped memory address %06x\n",cpu_get_pc(),offset);
	
		return 0xff;
	} };
	
	public static ReadHandlerPtr footchmp_input_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    switch (offset)
	    {
	         case 0x00:
	              return readinputport(3); /* DSW A */
	
	         case 0x02:
	              return readinputport(4); /* DSW B */
	
	         case 0x04:
	              return readinputport(2); /* IN2 */
	
	         case 0x0a:
	              return readinputport(0); /* IN0 */
	
	         case 0x0c:
	              return readinputport(1); /* IN1 */
	
	         case 0x0e:
	              return readinputport(5); /* IN3 */
	
	         case 0x10:
	              return readinputport(6); /* IN4 */
	    }
	
	logerror("CPU #0 PC %06x: warning - read unmapped memory address %06x\n",cpu_get_pc(),0x700000+offset);
	
		return 0xff;
	} };
	
	public static ReadHandlerPtr ninjak_input_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    switch (offset)
	    {
	         case 0x00:
	              return (readinputport(3) << 8); /* DSW A */
	
	         case 0x02:
	              return (readinputport(4) << 8); /* DSW B */
	
	         case 0x04:
	              return (readinputport(0) << 8); /* IN 0 */
	
	         case 0x06:
	              return (readinputport(1) << 8); /* IN 1 */
	
	         case 0x08:
	              return (readinputport(5) << 8); /* IN 3 */
	
	         case 0x0a:
	              return (readinputport(6) << 8); /* IN 4 */
	
	         case 0x0c:
	              return (readinputport(2) << 8); /* IN 2 */
	
	    }
	
	logerror("CPU #0 PC %06x: warning - read unmapped memory address %06x\n",cpu_get_pc(),0x300000+offset);
	
		return 0xff;
	} };
	
	public static ReadHandlerPtr cameltry_paddle_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		static int last[2];
		int curr,res = 0xff;
	
		switch (offset)
		{
			case 0x00:
				curr = readinputport(5); /* Paddle A */
				res = curr - last[0];
				last[0] = curr;
				break;
	
			case 0x04:
				curr = readinputport(6); /* Paddle B */
				res = curr - last[1];
				last[1] = curr;
				break;
		}
	
	logerror("CPU #0 PC %06x: warning - read unmapped memory address %06x\n",cpu_get_pc(),0x300018+offset);
	
		return res;
	} };
	
	public static ReadHandlerPtr driftout_paddle_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    switch (offset)
	    {
	         case 0x00:
	              return readinputport(5); /* Paddle A */
	
	         case 0x02:
	              return readinputport(6); /* Paddle B */
	    }
	
	logerror("CPU #0 PC %06x: warning - read unmapped memory address %06x\n",cpu_get_pc(),0xb00018+offset);
	
	        return 0xff;
	} };
	
	public static ReadHandlerPtr deadconx_input_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    switch (offset)
	    {
	         case 0x00:
	              return readinputport(3); /* DSW A */
	
	         case 0x02:
	              return readinputport(4); /* DSW B */
	
	         case 0x04:
	              return readinputport(2); /* IN2 */
	
	         case 0x0a:
	              return readinputport(0); /* IN0 */
	
	         case 0x0c:
	              return readinputport(1); /* IN1 */
	
	         case 0x0e:
	              return readinputport(5); /* IN3 */
	
	         case 0x10:
	              return readinputport(6); /* IN4 */
	    }
	
	logerror("CPU #0 PC %06x: warning - read unmapped memory address %06x\n",cpu_get_pc(),0x700000+offset);
	
		return 0xff;
	} };
	
	public static ReadHandlerPtr mjnquest_dsw_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    switch (offset)
	    {
	        case 0x00:
	        {
				return (readinputport(5) << 8) + readinputport(7); /* DSW A */
	        }
	
	        case 0x02:
	        {
				return (readinputport(6) << 8) + readinputport(8); /* DSW B */
	        }
	    }
	
	    logerror("CPU #0 dsw_r offset %06x: warning - read unmapped memory address %06x\n",cpu_get_pc(),offset);
	
	    return 0xff;
	} };
	
	public static ReadHandlerPtr mjnquest_input_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    switch (mjnquest_input)
	    {
	         case 0x01:
	              return readinputport(0); /* IN0 */
	
	         case 0x02:
	              return readinputport(1); /* IN1 */
	
	         case 0x04:
	              return readinputport(2); /* IN2 */
	
	         case 0x08:
	              return readinputport(3); /* IN3 */
	
	         case 0x10:
	              return readinputport(4); /* IN4 */
	
	    }
	
	logerror("CPU #0 mjnquest_input %06x: warning - read unknown input %06x\n",cpu_get_pc(),mjnquest_input);
	
		return 0xff;
	} };
	
	public static WriteHandlerPtr mjnquest_inputselect_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	    mjnquest_input = (data >> 6);
	} };
	
	public static ReadHandlerPtr quizhq_input1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    switch (offset)
	    {
	         case 0x00:
	              return readinputport(4); /* DSW B */
	
	         case 0x02:
	              return readinputport(0); /* IN0 */
	    }
	
	logerror("CPU #0 input_r offset %06x: warning - read unmapped memory address %06x\n",cpu_get_pc(),offset);
	
		return 0xff;
	} };
	
	public static ReadHandlerPtr quizhq_input2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    switch (offset)
	    {
	         case 0x00:
	              return readinputport(3); /* DSW A */
	
	         case 0x02:
	              return readinputport(1); /* IN1 */
	
	         case 0x04:
	              return readinputport(2); /* IN2 */
	    }
	
	logerror("CPU #0 input_r offset %06x: warning - read unmapped memory address %06x\n",cpu_get_pc(),offset);
	
		return 0xff;
	} };
	
	public static ReadHandlerPtr yesnoj_input_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    switch (offset)
	    {
	         case 0x00:
	              return readinputport(0); /* IN0 */
	
	// case 0x02 only used if "service" "DSW" bit is clear...
	
	         case 0x04:
	              return readinputport(1); /* IN1 */
	    }
	
	logerror("CPU #0 input_r offset %06x: warning - read unmapped memory address %06x\n",cpu_get_pc(),offset);
	
		return 0x0;
	} };
	
	public static ReadHandlerPtr yesnoj_dsw_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	#ifdef MAME_DEBUG
		logerror("CPU #0 pc = %06x: read yesnoj DSW %01x\n",cpu_get_pc(),yesnoj_dsw);
	#endif
	
		yesnoj_dsw = 1 - yesnoj_dsw;   /* game reads same word twice to get DSW A then B so we toggle */
	
		if (yesnoj_dsw != 0)
		{
			return readinputport(2);
		}
		else
		{
			return readinputport(3);
		}
	} };
	
	/******************************************************************
					INTERRUPTS (still a WIP)
	
	The are two interrupt request signals: VBL and DMA. DMA comes
	from the sprite generator (maybe when it has copied the data to
	a private buffer, or rendered the current frame, or who knows what
	else).
	The requests are mapped through a PAL so no hardwiring, but the PAL
	could be the same across all the games. All the games have just two
	valid vectors, IRQ5 and IRQ6.
	
	It seems that usually VBL maps to IRQ5 and DMA to IRQ6. However
	there are jumpers on the board allowing to swap the two interrupt
	request signals, so this could explain a need for certain games to
	have them in the opposite order.
	
	There are lots of sprite glitches in many games because the sprite ram
	is often updated in two out-of-sync chunks. I am almost sure there is
	some partial buffering going on in the sprite chip, and DMA has to
	play a part in it.
	
	
	             sprite ctrl regs   	  interrupts & sprites
	          0006 000a    8006 800a
	          ----------------------	-----------------------------------------------
	finalb    8000 0300    0000 0000	Needs partial buffering like dondokod to avoid glitches
	dondokod  8000 0000/8  0000 0000	IRQ6 just sets a flag. IRQ5 waits for that flag,
	                                	toggles ctrl register 0000<.0008, and copies bytes
										0 and 8 *ONLY* of sprite data (code, color, flip,
										ctrl). The other bytes of sprite data (coordinates
										and zoom) are updated by the main program.
										Caching sprite data and using bytes 0 and 8 from
										previous frame and the others from *TWO* frames
										before is enough to get glitch-free sprites that seem
										to be perfectly in sync with scrolling (check the tree
										mouths during level change).
	thundfox  8000 0000    0000 0000	IRQ6 copies bytes 0 and 8 *ONLY* of sprite data (code,
										color, flip, ctrl). The other bytes of sprite data
										(coordinates and zoom) are updated (I think) by the
										main program.
										The same sprite data caching that works for dondokod
										improves sprites, but there are still glitches related
										to zoom (check third round of attract mode). Those
										glitches can be fixed by buffering also the zoom ctrl
										byte.
										Moreover, sprites are not in perfect sync with the
										background (sometimes they are one frame behind, but
										not always).
	qtorimon  8000 0000    0000 0000    IRQ6 does some stuff but doesn't seem to deal with
										sprites. IRQ5 copies bytes 0, 8 *AND ALSO 2* of sprite
										data in one routine, and immediately after that the
										remaining bytes 4 and 6 in another routine, without
										doing, it seems, any waiting inbetween.
										Nevertheless, separated sprite data caching like in
										dondokod is still required to avoid glitches.
	liquidk   8000 0000/8  0000 0000	Same as dondokod. An important difference is that
										the sprite ctrl register doesn't toggle every frame
										(because the handler can't complete the frame in
										time?). This can be seen easily in the attract mode,
										where sprite glitches appear.
										Correctly handling the ctrl register and sprite data
										caching seems to be vital to avoid sprite glitches.
	quizhq    8000 0000    0000 0000	Both IRQ5 and IRQ6 do stuff, I haven't investigated.
										There is a very subtle sprite glitch if sprite data
										buffering is not used: the blinking INSERT COIN in
										the right window will get moved as garbage chars on
										the left window score and STOCK for one frame when
										INSERT COINS disappears from the right. This happens
										because bytes 0 and 8 of the sprite data are one
										frame behind and haven't been cleared yet.
	ssi       8000 0000    0000 0000	IRQ6 does nothing. IRQ5 copies bytes 0 and 8 *ONLY*
										of sprite data (code, color, flip, ctrl). The other
										bytes of sprite data (coordinates and zoom) are
										updated by the main program.
										The same sprite data caching that works for dondokod
										avoids major glitches, but I'm not sure it's working
										right when the big butterfly (time bonus) is on
										screen (it flickers on and off every frame).
	gunfront  8000 1000/1  8001 1000/1	The toggling bit in the control register selects the
										sprite bank used. It normally toggles every frame but
										sticks for two frame when lots of action is going on
										(see smart bombs in attract mode) and glitches will
										appear if it is not respected.
										IRQ6 writes the sprite ctrl registers, and also writes
										related data to the sprites at 9033e0/90b3e0. The
										active one gets 8000/8001 in byte 6 and 1001/1000 in
										byte 10, while the other gets 0. Note that the value
										in byte 10 is inverted from the active bank, as if it
										were a way to tell the sprite hardware "after this, go
										to the other bank".
										Note also that IRQ6 isn't the only one writing to the
										sprite ctrl registers, this is done also in the parts
										that actually change the sprite data (I think it's
										main program, not interrupt), so it's not clear who's
										"in charge". Actually it seems that what IRQ6 writes
										is soon overwritten so that what I outlined above
										regarding 9033e0/90b3e0 is no longer true, and they
										are no longer in sync with the ctrl registers, messing
										up smart bombs.
										There don't seem to be other glitches even without
										sprite data buffering.
	growl     8000 0000    8001 0001	IRQ6 just sets a flag. I haven't investigated who
										updates the sprite ram.
										This game uses sprite banks like gunfront, but unlike
										gunfront it doesn't change the ctrl registers. What it
										does is change the sprites at 903210/90b210; 8000/8001
										is always written in byte 6, while byte 10 receives
										the active bank (1000 or 1001). There are also end of
										list markers placed before that though, and those seem
										to always match what's stored in the ctrl registers
										(8000 1000 for the first bank and 8001 1001 for the
										second).
										There don't seem to be sprite glitches even without
										sprite data buffering, but sprites are not in sync with
										the background.
	mjnquest  8000 0800/8  0000 0000
	footchmp  8000 0000    8001 0001	IRQ6 just sets a flag (and writes to an unknown memory
										location).
										This games uses sprite banks as well, this time it
										writes markers at 2033e0/20b3e0, it always writes
										1000/1001 to byte 10, while it writes 8000 or 8001 to
										byte 6 depending on the active bank. This is the exact
										opposite of growl...
	hthero
	koshien   8000 0000    8001 0001	Another game using banks.The markers are again at
										9033e0/90b3e0 but this time byte 6 receives 9000/9001.
										Byte 10 is 1000 or 1001 depending on the active bank.
	yuyugogo  8000 0800/8  0000 0000
	ninjak    8000 0000    8001 0001	uses banks
	solfigtr  8000 0000    8001 0001	uses banks
	qzquest   8000 0000    0000 0000	Separated sprite data caching like in dondokod is
										required to avoid glitches.
	pulirula  8000 0000    8001 0001	uses banks
	qzchikyu  8000 0000    0000 0000	With this game there are glitches and the sprite data
										caching done in dondokod does NOT fix them.
	deadconx 8/9000 0000/1 8/9001 0000/1 I guess it's not a surprise that this game uses banks
										in yet another different way.
	dinorex   8000 0000    8001 0001	uses banks
	driftout  8000 0000/8  0000 0000	The first control changes from 8000 to 0000 at the end
										of the attract demo and seems to stay that way.
	
	
	******************************************************************/
	
	void taitof2_interrupt6(int x)
	{
		cpu_cause_interrupt(0,MC68000_IRQ_6);
	}
	
	public static InterruptPtr taitof2_interrupt = new InterruptPtr() { public int handler() 
	{
		timer_set(TIME_IN_CYCLES(500,0),0, taitof2_interrupt6);
		return MC68000_IRQ_5;
	} };
	
	
	/*****************************************
				SOUND
	*****************************************/
	
	public static WriteHandlerPtr bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU2);
		int banknum = (data - 1) & 7;
	
	#ifdef MAME_DEBUG
		if (banknum>3) logerror("CPU #1 switch to ROM bank %06x: should only happen if Z80 prg rom is 128K!\n",banknum);
	#endif
		cpu_setbank (2, &RAM [0x10000 + (banknum * 0x4000)]);
	} };
	
	public static WriteHandlerPtr taitof2_sound_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (offset == 0)
			rastan_sound_port_w (0, data & 0xff);
		else if (offset == 2)
			rastan_sound_comm_w (0, data & 0xff);
	#ifdef MAME_DEBUG
		if ((data & 0xff00) != 0)
		{
			char buf[80];
	
			sprintf(buf,"taitof2_sound_w to high byte: %04x",data);
			usrintf_showmessage(buf);
		}
	#endif
	} };
	
	public static ReadHandlerPtr taitof2_sound_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (offset == 2)
			return ((rastan_sound_comm_r (0) & 0xff));
		else return 0;
	} };
	
	public static WriteHandlerPtr taitof2_msb_sound_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (offset == 0)
			rastan_sound_port_w (0,(data >> 8) & 0xff);
		else if (offset == 2)
			rastan_sound_comm_w (0,(data >> 8) & 0xff);
	#ifdef MAME_DEBUG
		if ((data & 0xff) != 0)
		{
			char buf[80];
	
			sprintf(buf,"taitof2_msb_sound_w to low byte: %04x",data);
			usrintf_showmessage(buf);
		}
	#endif
	} };
	
	public static ReadHandlerPtr taitof2_msb_sound_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (offset == 2)
			return ((rastan_sound_comm_r (0) & 0xff) << 8);
		else return 0;
	} };
	
	
	
	/***********************************************************
				 MEMORY STRUCTURES
	***********************************************************/
	
	static MemoryReadAddress finalb_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x200000, 0x200007, TC0110PCR_word_r ),	/* palette */
		new MemoryReadAddress( 0x300000, 0x30000f, TC0220IOC_halfword_r ),	/* I/O */
		new MemoryReadAddress( 0x320000, 0x320003, taitof2_sound_r ),
		new MemoryReadAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0x900000, 0x90ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress finalb_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x200000, 0x200007, TC0110PCR_word_w ),	/* palette */
		new MemoryWriteAddress( 0x300000, 0x30000f, TC0220IOC_halfword_w ),	/* I/O */
		new MemoryWriteAddress( 0x320000, 0x320003, taitof2_sound_w ),
		new MemoryWriteAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x810000, 0x81ffff, MWA_NOP ),   /* error in game init code ? */
		new MemoryWriteAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0x900000, 0x90ffff, taitof2_spriteram_w, spriteram, spriteram_size  ),
		new MemoryWriteAddress( 0xb00002, 0xb00003, MWA_NOP ),   /* ?? */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress dondokod_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x200000, 0x201fff, paletteram_word_r ),
		new MemoryReadAddress( 0x300000, 0x30000f, TC0220IOC_halfword_r ),	/* I/O */
		new MemoryReadAddress( 0x320000, 0x320003, taitof2_msb_sound_r ),
		new MemoryReadAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0x900000, 0x90ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( 0xa00000, 0xa01fff, TC0280GRD_word_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress dondokod_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x200000, 0x201fff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ),
		new MemoryWriteAddress( 0x300000, 0x30000f, TC0220IOC_halfword_w ),	/* I/O */
		new MemoryWriteAddress( 0x320000, 0x320003, taitof2_msb_sound_w ),
		new MemoryWriteAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0x900000, 0x90ffff, taitof2_spriteram_w, spriteram, spriteram_size  ),
		new MemoryWriteAddress( 0xa00000, 0xa01fff, TC0280GRD_word_w ),	/* ROZ tilemap */
		new MemoryWriteAddress( 0xa02000, 0xa0200f, TC0280GRD_ctrl_word_w ),
		new MemoryWriteAddress( 0xb00000, 0xb0001f, TC0360PRI_halfword_w ),	/* ?? */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress megab_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x100003, taitof2_msb_sound_r ),
		new MemoryReadAddress( 0x120000, 0x12000f, TC0220IOC_halfword_r ),	/* I/O */
		new MemoryReadAddress( 0x180000, 0x180fff, cchip2_r ),
		new MemoryReadAddress( 0x200000, 0x20ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x300000, 0x301fff, paletteram_word_r ),
		new MemoryReadAddress( 0x600000, 0x60ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x610000, 0x61ffff, MRA_BANK8 ), /* unused? */
		new MemoryReadAddress( 0x620000, 0x62000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0x800000, 0x80ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress megab_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x200000, 0x20ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x300000, 0x301fff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ),
		new MemoryWriteAddress( 0x100000, 0x100003, taitof2_msb_sound_w ),
		new MemoryWriteAddress( 0x120000, 0x12000f, TC0220IOC_halfword_w ),	/* I/O */
		new MemoryWriteAddress( 0x180000, 0x180fff, cchip2_w, cchip_ram ),
		new MemoryWriteAddress( 0x400000, 0x40001f, TC0360PRI_halfword_w ),	/* ?? */
		new MemoryWriteAddress( 0x600000, 0x60ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x610000, 0x61ffff, MWA_BANK8 ),   /* unused? */
		new MemoryWriteAddress( 0x620000, 0x62000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0x800000, 0x80ffff, taitof2_spriteram_w, spriteram, spriteram_size  ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress thundfox_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x101fff, paletteram_word_r ),
		new MemoryReadAddress( 0x200000, 0x20000f, TC0220IOC_halfword_r ),	/* I/O */
		new MemoryReadAddress( 0x220000, 0x220003, taitof2_msb_sound_r ),
		new MemoryReadAddress( 0x300000, 0x30ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x400000, 0x40ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x420000, 0x42000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0x500000, 0x50ffff, TC0100SCN_word_1_r ),	/* tilemaps */
		new MemoryReadAddress( 0x520000, 0x52000f, TC0100SCN_ctrl_word_1_r ),
		new MemoryReadAddress( 0x600000, 0x60ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress thundfox_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x101fff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ),
		new MemoryWriteAddress( 0x200000, 0x20000f, TC0220IOC_halfword_w ),	/* I/O */
		new MemoryWriteAddress( 0x220000, 0x220003, taitof2_msb_sound_w ),
		new MemoryWriteAddress( 0x300000, 0x30ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x400000, 0x40ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x420000, 0x42000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0x500000, 0x50ffff, TC0100SCN_word_1_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x520000, 0x52000f, TC0100SCN_ctrl_word_1_w ),
		new MemoryWriteAddress( 0x600000, 0x60ffff, taitof2_spriteram_w, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x800000, 0x80001f, TC0360PRI_halfword_swap_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress cameltry_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x200000, 0x201fff, paletteram_word_r ),
		new MemoryReadAddress( 0x300000, 0x30000f, TC0220IOC_halfword_r ),	/* I/O */
		new MemoryReadAddress( 0x300018, 0x30001f, cameltry_paddle_r ),
		new MemoryReadAddress( 0x320000, 0x320003, taitof2_msb_sound_r ),
		new MemoryReadAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0x900000, 0x90ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( 0xa00000, 0xa01fff, TC0280GRD_word_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress cameltry_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x200000, 0x201fff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ),
		new MemoryWriteAddress( 0x300000, 0x30000f, TC0220IOC_halfword_w ),	/* I/O */
		new MemoryWriteAddress( 0x320000, 0x320003, taitof2_msb_sound_w ),
		new MemoryWriteAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0x900000, 0x90ffff, taitof2_spriteram_w, spriteram, spriteram_size  ),
		new MemoryWriteAddress( 0xa00000, 0xa01fff, TC0280GRD_word_w ),	/* ROZ tilemap */
		new MemoryWriteAddress( 0xa02000, 0xa0200f, TC0280GRD_ctrl_word_w ),
		new MemoryWriteAddress( 0xd00000, 0xd0001f, TC0360PRI_halfword_w ),	/* ?? */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress qtorimon_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x200000, 0x200007, TC0110PCR_word_r ),	/* palette */
		new MemoryReadAddress( 0x500000, 0x50000f, TC0220IOC_halfword_r ),	/* I/O */
		new MemoryReadAddress( 0x600000, 0x600003, taitof2_msb_sound_r ),
		new MemoryReadAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0x900000, 0x90ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress qtorimon_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x200000, 0x200007, TC0110PCR_word_w ),	/* palette */
		new MemoryWriteAddress( 0x500000, 0x50000f, TC0220IOC_halfword_w ),	/* I/O */
		new MemoryWriteAddress( 0x600000, 0x600003, taitof2_msb_sound_w ),
		new MemoryWriteAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0x900000, 0x90ffff, taitof2_spriteram_w, spriteram, spriteram_size  ),
		new MemoryWriteAddress( 0x910000, 0x9120ff, MWA_NOP ),   /* error in init code ? */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress liquidk_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x200000, 0x201fff, paletteram_word_r ),
		new MemoryReadAddress( 0x300000, 0x30000f, TC0220IOC_halfword_r ),	/* I/O */
		new MemoryReadAddress( 0x320000, 0x320003, taitof2_sound_r ),
		new MemoryReadAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0x900000, 0x90ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress liquidk_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x200000, 0x201fff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ),
		new MemoryWriteAddress( 0x300000, 0x30000f, TC0220IOC_halfword_w ),	/* I/O */
		new MemoryWriteAddress( 0x320000, 0x320003, taitof2_sound_w ),
		new MemoryWriteAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0x900000, 0x90ffff, taitof2_spriteram_w, spriteram, spriteram_size  ),
		new MemoryWriteAddress( 0xb00000, 0xb0001f, TC0360PRI_halfword_w ),	/* ?? */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress quizhq_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x0bffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x200000, 0x200007, TC0110PCR_word_r ),	/* palette */
		new MemoryReadAddress( 0x500000, 0x50000f, quizhq_input1_r ),
		new MemoryReadAddress( 0x580000, 0x58000f, quizhq_input2_r ),
		new MemoryReadAddress( 0x600000, 0x600003, taitof2_sound_r ),
		new MemoryReadAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0x900000, 0x90ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress quizhq_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x200000, 0x200007, TC0110PCR_word_w ),	/* palette */
		new MemoryWriteAddress( 0x500004, 0x500005, MWA_NOP ),   /* irq ack ? */
		new MemoryWriteAddress( 0x580000, 0x580001, MWA_NOP ),   /* irq ack ? */
		new MemoryWriteAddress( 0x580006, 0x580007, MWA_NOP ),   /* irq ack ? */
		new MemoryWriteAddress( 0x600000, 0x600003, taitof2_sound_w ),
		new MemoryWriteAddress( 0x680000, 0x680001, MWA_NOP ),   /* watchdog ?? */
		new MemoryWriteAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x810000, 0x81ffff, MWA_NOP ),   /* error in init code ? */
		new MemoryWriteAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0x900000, 0x90ffff, taitof2_spriteram_w, spriteram, spriteram_size  ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress ssi_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10000f, TC0510NIO_halfword_r ),
		new MemoryReadAddress( 0x200000, 0x20ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x300000, 0x301fff, paletteram_word_r ),
		new MemoryReadAddress( 0x400000, 0x400003, taitof2_msb_sound_r ),
		new MemoryReadAddress( 0x600000, 0x60ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x620000, 0x62000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0x800000, 0x80ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress ssi_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10000f, TC0510NIO_halfword_w ),
		new MemoryWriteAddress( 0x200000, 0x20ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x300000, 0x301fff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ),
		new MemoryWriteAddress( 0x400000, 0x400003, taitof2_msb_sound_w ),
	//	new MemoryWriteAddress( 0x500000, 0x500001, MWA_NOP ),   /* ?? */
		new MemoryWriteAddress( 0x600000, 0x60ffff, TC0100SCN_word_0_w ),	/* tilemaps (not used) */
		new MemoryWriteAddress( 0x620000, 0x62000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0x800000, 0x80ffff, taitof2_spriteram_w, spriteram, spriteram_size ),   /* sprite ram */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress gunfront_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x0bffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x200000, 0x201fff, paletteram_word_r ),
		new MemoryReadAddress( 0x300000, 0x30000f, TC0510NIO_halfword_wordswap_r ),
		new MemoryReadAddress( 0x320000, 0x320003, taitof2_msb_sound_r ),
		new MemoryReadAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0x900000, 0x90ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress gunfront_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x0bffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x200000, 0x201fff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ),
		new MemoryWriteAddress( 0x300000, 0x30000f, TC0510NIO_halfword_wordswap_w ),
		new MemoryWriteAddress( 0x320000, 0x320003, taitof2_msb_sound_w ),
		new MemoryWriteAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0x900000, 0x90ffff, taitof2_spriteram_w, spriteram, spriteram_size ),
	//	new MemoryWriteAddress( 0xa00000, 0xa00001, MWA_NOP ),   /* ?? */
		new MemoryWriteAddress( 0xb00000, 0xb0001f, TC0360PRI_halfword_w ),	/* ?? */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress growl_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x0fffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x200000, 0x201fff, paletteram_word_r ),
		new MemoryReadAddress( 0x300000, 0x30000f, growl_dsw_r ),
		new MemoryReadAddress( 0x320000, 0x32000f, growl_input_r ),
		new MemoryReadAddress( 0x400000, 0x400003, taitof2_msb_sound_r ),
		new MemoryReadAddress( 0x508000, 0x50800f, input_port_5_r ),   /* IN3 */
		new MemoryReadAddress( 0x50c000, 0x50c00f, input_port_6_r ),   /* IN4 */
		new MemoryReadAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0x900000, 0x90ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress growl_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x0fffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x200000, 0x201fff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ),
		new MemoryWriteAddress( 0x340000, 0x340001, MWA_NOP ),   /* irq ack? */
		new MemoryWriteAddress( 0x400000, 0x400003, taitof2_msb_sound_w ),
		new MemoryWriteAddress( 0x500000, 0x50000f, taitof2_spritebank_w ),
		new MemoryWriteAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0x900000, 0x90ffff, taitof2_spriteram_w, spriteram, spriteram_size  ),
		new MemoryWriteAddress( 0xb00000, 0xb0001f, TC0360PRI_halfword_w ),	/* ?? */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress mjnquest_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x0fffff, MRA_ROM ),
		new MemoryReadAddress( 0x110000, 0x11ffff, MRA_BANK8 ),   /* sram ? */
		new MemoryReadAddress( 0x120000, 0x12ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x200000, 0x200007, TC0110PCR_word_r ),	/* palette */
		new MemoryReadAddress( 0x300000, 0x30000f, mjnquest_dsw_r ),
		new MemoryReadAddress( 0x310000, 0x310001, mjnquest_input_r ),
		new MemoryReadAddress( 0x360000, 0x360003, taitof2_msb_sound_r ),
		new MemoryReadAddress( 0x400000, 0x40ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x420000, 0x42000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0x500000, 0x50ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress mjnquest_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x110000, 0x11ffff, MWA_BANK8 ),   /* sram ? */
		new MemoryWriteAddress( 0x120000, 0x12ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x200000, 0x200007, TC0110PCR_word_w ),	/* palette */
		new MemoryWriteAddress( 0x320000, 0x320001, mjnquest_inputselect_w ),
		new MemoryWriteAddress( 0x330000, 0x330001, MWA_NOP ),   /* watchdog ? */
		new MemoryWriteAddress( 0x350000, 0x350001, MWA_NOP ),   /* watchdog ? */
		new MemoryWriteAddress( 0x360000, 0x360003, taitof2_msb_sound_w ),
		new MemoryWriteAddress( 0x380000, 0x380001, taitof2_scrbank_w ),   /* scr bank */
		new MemoryWriteAddress( 0x400000, 0x40ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x420000, 0x42000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0x500000, 0x50ffff, taitof2_spriteram_w, spriteram, spriteram_size  ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress footchmp_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x200000, 0x20ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( 0x400000, 0x40ffff, TC0480SCP_word_r ),   /* tilemaps */
		new MemoryReadAddress( 0x430000, 0x43002f, TC0480SCP_ctrl_word_r ),
		new MemoryReadAddress( 0x600000, 0x601fff, paletteram_word_r ),
		new MemoryReadAddress( 0x700000, 0x70001f, footchmp_input_r ),
		new MemoryReadAddress( 0xa00000, 0xa00003, taitof2_sound_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress footchmp_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x200000, 0x20ffff, taitof2_spriteram_w, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x300000, 0x30000f, taitof2_spritebank_w ),
		new MemoryWriteAddress( 0x400000, 0x40ffff, TC0480SCP_word_w ),	  /* tilemaps */
		new MemoryWriteAddress( 0x430000, 0x43002f, TC0480SCP_ctrl_word_w ),
		new MemoryWriteAddress( 0x500000, 0x50001f, TC0360PRI_halfword_w ),
		new MemoryWriteAddress( 0x600000, 0x601fff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ),
		new MemoryWriteAddress( 0x800000, 0x800001, MWA_NOP ),   /* watchdog ? */
		new MemoryWriteAddress( 0xa00000, 0xa00003, taitof2_sound_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress koshien_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x0fffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x200000, 0x201fff, paletteram_word_r ),
		new MemoryReadAddress( 0x300000, 0x30000f, TC0510NIO_halfword_r ),
		new MemoryReadAddress( 0x320000, 0x320003, taitof2_msb_sound_r ),
		new MemoryReadAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0x900000, 0x90ffff, taitof2_spriteram_r ),
	//	new MemoryReadAddress( 0xa20000, 0xa20001, koshien_spritebank_r ),   /* for debugging spritebank */
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress koshien_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x200000, 0x201fff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ),
		new MemoryWriteAddress( 0x300000, 0x30000f, TC0510NIO_halfword_w ),
		new MemoryWriteAddress( 0x320000, 0x320003, taitof2_msb_sound_w ),
		new MemoryWriteAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0x900000, 0x90ffff, taitof2_spriteram_w, spriteram, spriteram_size  ),
		new MemoryWriteAddress( 0xa20000, 0xa20001, koshien_spritebank_w ),   /* spritebank word ?? */
		new MemoryWriteAddress( 0xb00000, 0xb0001f, TC0360PRI_halfword_swap_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress yuyugogo_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
		new MemoryReadAddress( 0x200000, 0x20000f, TC0510NIO_halfword_r ),
		new MemoryReadAddress( 0x400000, 0x400003, taitof2_msb_sound_r ),
		new MemoryReadAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0x900000, 0x90ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( 0xa00000, 0xa01fff, paletteram_word_r ),
		new MemoryReadAddress( 0xb00000, 0xb10fff, MRA_BANK1 ),
		new MemoryReadAddress( 0xd00000, 0xdfffff, MRA_BANK8 ),   /* extra data rom */
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress yuyugogo_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x200000, 0x20000f, TC0510NIO_halfword_w ),
		new MemoryWriteAddress( 0x400000, 0x400003, taitof2_msb_sound_w ),
		new MemoryWriteAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0x900000, 0x90ffff, taitof2_spriteram_w, spriteram, spriteram_size  ),
		new MemoryWriteAddress( 0xa00000, 0xa01fff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ),
		new MemoryWriteAddress( 0xb00000, 0xb10fff, MWA_BANK1 ),   /* deliberate writes to $b10xxx, I think */
		new MemoryWriteAddress( 0xc00000, 0xc01fff, taitof2_sprite_extension_w, f2_sprite_extension, f2_spriteext_size ),
		new MemoryWriteAddress( 0xd00000, 0xdfffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress ninjak_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x200000, 0x201fff, paletteram_word_r ),
		new MemoryReadAddress( 0x300000, 0x30000f, ninjak_input_r ),
		new MemoryReadAddress( 0x400000, 0x400003, taitof2_msb_sound_r ),
		new MemoryReadAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0x900000, 0x90ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress ninjak_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x200000, 0x201fff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ),
		new MemoryWriteAddress( 0x380000, 0x380001, MWA_NOP ),   /* irq ack? */
		new MemoryWriteAddress( 0x400000, 0x400003, taitof2_msb_sound_w ),
		new MemoryWriteAddress( 0x600000, 0x60000f, taitof2_spritebank_w ),
		new MemoryWriteAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0x900000, 0x90ffff, taitof2_spriteram_w, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0xb00000, 0xb0001f, TC0360PRI_halfword_w ),	/* ?? */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress solfigtr_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x200000, 0x201fff, paletteram_word_r ),
		new MemoryReadAddress( 0x300000, 0x30000f, growl_dsw_r ),
		new MemoryReadAddress( 0x320000, 0x32000f, growl_input_r ),
		new MemoryReadAddress( 0x400000, 0x400003, taitof2_msb_sound_r ),
		new MemoryReadAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0x900000, 0x90ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress solfigtr_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x200000, 0x201fff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ),
		new MemoryWriteAddress( 0x400000, 0x400003, taitof2_msb_sound_w ),
		new MemoryWriteAddress( 0x500000, 0x50000f, taitof2_spritebank_w ),
		new MemoryWriteAddress( 0x504000, 0x504001, MWA_NOP ),   /* irq ack? */
		new MemoryWriteAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0x900000, 0x90ffff, taitof2_spriteram_w, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0xb00000, 0xb0001f, TC0360PRI_halfword_w ),	/* ?? */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress qzquest_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x17ffff, MRA_ROM ),
		new MemoryReadAddress( 0x200000, 0x20000f, TC0510NIO_halfword_r ),
		new MemoryReadAddress( 0x300000, 0x300003, taitof2_sound_r ),
		new MemoryReadAddress( 0x400000, 0x401fff, paletteram_word_r ),
		new MemoryReadAddress( 0x500000, 0x50ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x600000, 0x60ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( 0x700000, 0x70ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x720000, 0x72000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress qzquest_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x200000, 0x20000f, TC0510NIO_halfword_w ),
		new MemoryWriteAddress( 0x300000, 0x300003, taitof2_sound_w ),
		new MemoryWriteAddress( 0x400000, 0x401fff, paletteram_xRRRRRGGGGGBBBBB_word_w, paletteram ),
		new MemoryWriteAddress( 0x500000, 0x50ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x600000, 0x60ffff, taitof2_spriteram_w, spriteram, spriteram_size  ),
		new MemoryWriteAddress( 0x700000, 0x70ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x720000, 0x72000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress pulirula_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x0bffff, MRA_ROM ),
		new MemoryReadAddress( 0x200000, 0x200003, taitof2_msb_sound_r ),
		new MemoryReadAddress( 0x300000, 0x30ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x400000, 0x401fff, TC0430GRW_word_r ),
		new MemoryReadAddress( 0x700000, 0x701fff, paletteram_word_r ),
		new MemoryReadAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0x900000, 0x90ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( 0xb00000, 0xb0000f, TC0510NIO_halfword_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress pulirula_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x0bffff, MWA_ROM ),
		new MemoryWriteAddress( 0x200000, 0x200003, taitof2_msb_sound_w ),
		new MemoryWriteAddress( 0x300000, 0x30ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x400000, 0x401fff, TC0430GRW_word_w ),	/* ROZ tilemap */
		new MemoryWriteAddress( 0x402000, 0x40200f, TC0430GRW_ctrl_word_w ),
	//	new MemoryWriteAddress( 0x500000, 0x500001, MWA_NOP ),   /* ??? */
		new MemoryWriteAddress( 0x600000, 0x603fff, taitof2_sprite_extension_w, f2_sprite_extension, f2_spriteext_size ),
		new MemoryWriteAddress( 0x700000, 0x701fff, paletteram_xRRRRRGGGGGBBBBB_word_w, paletteram ),
		new MemoryWriteAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0x900000, 0x90ffff, taitof2_spriteram_w, spriteram, spriteram_size  ),
		new MemoryWriteAddress( 0xa00000, 0xa0001f, TC0360PRI_halfword_swap_w ),
		new MemoryWriteAddress( 0xb00000, 0xb0000f, TC0510NIO_halfword_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress metalb_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x0bffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x300000, 0x30ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( 0x500000, 0x50ffff, TC0480SCP_word_r ),   /* tilemaps */
		new MemoryReadAddress( 0x530000, 0x53002f, TC0480SCP_ctrl_word_r ),
		new MemoryReadAddress( 0x700000, 0x703fff, paletteram_word_r ),
		new MemoryReadAddress( 0x800000, 0x80000f, TC0510NIO_halfword_wordswap_r ),
		new MemoryReadAddress( 0x900000, 0x900003, taitof2_msb_sound_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress metalb_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x0bffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x300000, 0x30ffff, taitof2_spriteram_w, spriteram, spriteram_size ),
	//	new MemoryWriteAddress( 0x42000c, 0x42000f, MWA_NOP ),   /* zeroed */
		new MemoryWriteAddress( 0x500000, 0x50ffff, TC0480SCP_word_w ),	  /* tilemaps */
		new MemoryWriteAddress( 0x530000, 0x53002f, TC0480SCP_ctrl_word_w ),
		new MemoryWriteAddress( 0x600000, 0x60001f, TC0360PRI_halfword_w ),
		new MemoryWriteAddress( 0x700000, 0x703fff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ),
		new MemoryWriteAddress( 0x800000, 0x80000f, TC0510NIO_halfword_wordswap_w ),
		new MemoryWriteAddress( 0x900000, 0x900003, taitof2_msb_sound_w ),
	//	new MemoryWriteAddress( 0xa00000, 0xa00001, MWA_NOP ),   /* ??? */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress qzchikyu_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x17ffff, MRA_ROM ),
		new MemoryReadAddress( 0x200000, 0x20000f, TC0510NIO_halfword_r ),
		new MemoryReadAddress( 0x300000, 0x300003, taitof2_sound_r ),
		new MemoryReadAddress( 0x400000, 0x401fff, paletteram_word_r ),
		new MemoryReadAddress( 0x500000, 0x50ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x600000, 0x60ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( 0x700000, 0x70ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x720000, 0x72000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress qzchikyu_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x200000, 0x20000f, TC0510NIO_halfword_w ),
		new MemoryWriteAddress( 0x300000, 0x300003, taitof2_sound_w ),
		new MemoryWriteAddress( 0x400000, 0x401fff, paletteram_xRRRRRGGGGGBBBBB_word_w, paletteram ),
		new MemoryWriteAddress( 0x500000, 0x50ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x600000, 0x60ffff, taitof2_spriteram_w, spriteram, spriteram_size  ),
		new MemoryWriteAddress( 0x700000, 0x70ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x720000, 0x72000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress yesnoj_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x200000, 0x20ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x400000, 0x40ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( 0x500000, 0x50ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x520000, 0x52000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0x600000, 0x601fff, paletteram_word_r ),
	//	new MemoryReadAddress( 0x700000, 0x70000b, yesnoj_unknown_r ),   /* what's this? */
		new MemoryReadAddress( 0x800000, 0x800003, taitof2_msb_sound_r ),
		new MemoryReadAddress( 0xa00000, 0xa0000f, yesnoj_input_r ),
		new MemoryReadAddress( 0xb00000, 0xb00001, yesnoj_dsw_r ),   /* ?? (reads this twice in init) */
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress yesnoj_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x200000, 0x20ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x400000, 0x40ffff, taitof2_spriteram_w, spriteram, spriteram_size  ),
		new MemoryWriteAddress( 0x500000, 0x50ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x520000, 0x52000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0x600000, 0x601fff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ),
		new MemoryWriteAddress( 0x800000, 0x800003, taitof2_msb_sound_w ),
		new MemoryWriteAddress( 0x900002, 0x900003, MWA_NOP ),   /* lots of similar writes */
		new MemoryWriteAddress( 0xc00000, 0xc00001, MWA_NOP ),   /* watchdog ?? */
		new MemoryWriteAddress( 0xd00000, 0xd00001, MWA_NOP ),   /* lots of similar writes */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress deadconx_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x0fffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x200000, 0x20ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( 0x400000, 0x40ffff, TC0480SCP_word_r ),   /* tilemaps */
		new MemoryReadAddress( 0x430000, 0x43002f, TC0480SCP_ctrl_word_r ),
		new MemoryReadAddress( 0x600000, 0x601fff, paletteram_word_r ),
		new MemoryReadAddress( 0x700000, 0x70001f, deadconx_input_r ),
		new MemoryReadAddress( 0xa00000, 0xa00003, taitof2_msb_sound_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress deadconx_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x0fffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x200000, 0x20ffff, taitof2_spriteram_w, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x300000, 0x30000f, taitof2_spritebank_w ),
		new MemoryWriteAddress( 0x400000, 0x40ffff, TC0480SCP_word_w ),	  /* tilemaps */
	//	new MemoryWriteAddress( 0x42000c, 0x42000f, MWA_NOP ),   /* zeroed */
		new MemoryWriteAddress( 0x430000, 0x43002f, TC0480SCP_ctrl_word_w ),
		new MemoryWriteAddress( 0x500000, 0x50001f, TC0360PRI_halfword_w ),
		new MemoryWriteAddress( 0x600000, 0x601fff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ),
		new MemoryWriteAddress( 0x800000, 0x800001, MWA_NOP ),   /* watchdog ? */
		new MemoryWriteAddress( 0xa00000, 0xa00003, taitof2_msb_sound_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress dinorex_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x2fffff, MRA_ROM ),
		new MemoryReadAddress( 0x300000, 0x30000f, TC0510NIO_halfword_r ),
		new MemoryReadAddress( 0x500000, 0x501fff, paletteram_word_r ),
		new MemoryReadAddress( 0x600000, 0x60ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x800000, 0x80ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( 0x900000, 0x90ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x920000, 0x92000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0xa00000, 0xa00003, taitof2_msb_sound_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress dinorex_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x2fffff, MWA_ROM ),
		new MemoryWriteAddress( 0x300000, 0x30000f, TC0510NIO_halfword_w ),
		new MemoryWriteAddress( 0x400000, 0x400fff, taitof2_sprite_extension_w, f2_sprite_extension, f2_spriteext_size ),
		new MemoryWriteAddress( 0x500000, 0x501fff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ),
		new MemoryWriteAddress( 0x600000, 0x60ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x700000, 0x70001f, TC0360PRI_halfword_w ),	/* ?? */
		new MemoryWriteAddress( 0x800000, 0x80ffff, taitof2_spriteram_w, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x900000, 0x90ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x920000, 0x92000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0xa00000, 0xa00003, taitof2_msb_sound_w ),
		new MemoryWriteAddress( 0xb00000, 0xb00001, MWA_NOP ),   /* watchdog? */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress qjinsei_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x1fffff, MRA_ROM ),
		new MemoryReadAddress( 0x200000, 0x200003, taitof2_msb_sound_r ),
		new MemoryReadAddress( 0x300000, 0x30ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x700000, 0x701fff, paletteram_word_r ),
		new MemoryReadAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0x900000, 0x90ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( 0xb00000, 0xb0000f, TC0510NIO_halfword_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress qjinsei_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x200000, 0x200003, taitof2_msb_sound_w ),
		new MemoryWriteAddress( 0x300000, 0x30ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x500000, 0x500001, MWA_NOP ),   /* watchdog ? */
		new MemoryWriteAddress( 0x600000, 0x603fff, taitof2_sprite_extension_w, f2_sprite_extension, f2_spriteext_size ),
		new MemoryWriteAddress( 0x700000, 0x701fff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ),
		new MemoryWriteAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0x900000, 0x90ffff, taitof2_spriteram_w, spriteram, spriteram_size  ),
		new MemoryWriteAddress( 0xa00000, 0xa0001f, TC0360PRI_halfword_w ),	/* ?? */
		new MemoryWriteAddress( 0xb00000, 0xb0000f, TC0510NIO_halfword_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress qcrayon_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x300000, 0x3fffff, MRA_BANK8 ),   /* extra data rom */
		new MemoryReadAddress( 0x500000, 0x500003, taitof2_msb_sound_r ),
		new MemoryReadAddress( 0x700000, 0x701fff, paletteram_word_r ),
		new MemoryReadAddress( 0x800000, 0x80ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( 0x900000, 0x90ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x920000, 0x92000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0xa00000, 0xa0000f, TC0510NIO_halfword_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress qcrayon_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1 ),
	//	new MemoryWriteAddress( 0x200000, 0x200001, MWA_NOP ),   /* unknown */
		new MemoryWriteAddress( 0x300000, 0x3fffff, MWA_ROM ),
		new MemoryWriteAddress( 0x500000, 0x500003, taitof2_msb_sound_w ),
		new MemoryWriteAddress( 0x600000, 0x603fff, taitof2_sprite_extension_w, f2_sprite_extension, f2_spriteext_size ),
		new MemoryWriteAddress( 0x700000, 0x701fff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ),
		new MemoryWriteAddress( 0x800000, 0x80ffff, taitof2_spriteram_w, spriteram, spriteram_size  ),
		new MemoryWriteAddress( 0x900000, 0x90ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x920000, 0x92000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0xa00000, 0xa0000f, TC0510NIO_halfword_w ),
		new MemoryWriteAddress( 0xb00000, 0xb0001f, TC0360PRI_halfword_w ),	/* ?? */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress qcrayon2_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x200000, 0x20ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x300000, 0x301fff, paletteram_word_r ),
		new MemoryReadAddress( 0x400000, 0x40ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( 0x500000, 0x50ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x520000, 0x52000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0x600000, 0x67ffff, MRA_BANK8 ),   /* extra data rom */
		new MemoryReadAddress( 0x700000, 0x70000f, TC0510NIO_halfword_r ),
		new MemoryReadAddress( 0xa00000, 0xa00003, taitof2_msb_sound_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress qcrayon2_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x200000, 0x20ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x300000, 0x301fff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ),
		new MemoryWriteAddress( 0x400000, 0x40ffff, taitof2_spriteram_w, spriteram, spriteram_size  ),
		new MemoryWriteAddress( 0x500000, 0x50ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x520000, 0x52000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0x600000, 0x67ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x700000, 0x70000f, TC0510NIO_halfword_w ),
		new MemoryWriteAddress( 0x900000, 0x90001f, TC0360PRI_halfword_w ),	/* ?? */
		new MemoryWriteAddress( 0xa00000, 0xa00003, taitof2_msb_sound_w ),
		new MemoryWriteAddress( 0xb00000, 0xb017ff, taitof2_sprite_extension_w, f2_sprite_extension, f2_spriteext_size ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress driftout_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x0fffff, MRA_ROM ),
		new MemoryReadAddress( 0x200000, 0x200003, taitof2_msb_sound_r ),
		new MemoryReadAddress( 0x300000, 0x30ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x400000, 0x401fff, TC0430GRW_word_r ),
		new MemoryReadAddress( 0x700000, 0x701fff, paletteram_word_r ),
		new MemoryReadAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_r ),	/* tilemaps */
		new MemoryReadAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_r ),
		new MemoryReadAddress( 0x900000, 0x90ffff, taitof2_spriteram_r ),
		new MemoryReadAddress( 0xb00000, 0xb0000f, TC0510NIO_halfword_r ),
		new MemoryReadAddress( 0xb00018, 0xb0001f, driftout_paddle_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress driftout_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x0fffff, MWA_ROM ),
		new MemoryWriteAddress( 0x200000, 0x200003, taitof2_msb_sound_w ),
		new MemoryWriteAddress( 0x300000, 0x30ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x400000, 0x401fff, TC0430GRW_word_w ),	/* ROZ tilemap */
		new MemoryWriteAddress( 0x402000, 0x40200f, TC0430GRW_ctrl_word_w ),
		new MemoryWriteAddress( 0x700000, 0x701fff, paletteram_xRRRRRGGGGGBBBBB_word_w, paletteram ),
		new MemoryWriteAddress( 0x800000, 0x80ffff, TC0100SCN_word_0_w ),	/* tilemaps */
		new MemoryWriteAddress( 0x820000, 0x82000f, TC0100SCN_ctrl_word_0_w ),
		new MemoryWriteAddress( 0x900000, 0x90ffff, taitof2_spriteram_w, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0xa00000, 0xa0001f, TC0360PRI_halfword_swap_w ),
		new MemoryWriteAddress( 0xb00000, 0xb0000f, TC0510NIO_halfword_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	/***************************************************************************/
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new MemoryReadAddress( 0x4000, 0x7fff, MRA_BANK2 ),
		new MemoryReadAddress( 0xc000, 0xdfff, MRA_RAM ),
		new MemoryReadAddress( 0xe000, 0xe000, YM2610_status_port_0_A_r ),
		new MemoryReadAddress( 0xe001, 0xe001, YM2610_read_port_0_r ),
		new MemoryReadAddress( 0xe002, 0xe002, YM2610_status_port_0_B_r ),
		new MemoryReadAddress( 0xe200, 0xe200, MRA_NOP ),
		new MemoryReadAddress( 0xe201, 0xe201, rastan_a001_r ),
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
		new MemoryWriteAddress( 0xe200, 0xe200, rastan_a000_w ),
		new MemoryWriteAddress( 0xe201, 0xe201, rastan_a001_w ),
		new MemoryWriteAddress( 0xe400, 0xe403, MWA_NOP ), /* pan */
		new MemoryWriteAddress( 0xee00, 0xee00, MWA_NOP ), /* ? */
		new MemoryWriteAddress( 0xf000, 0xf000, MWA_NOP ), /* ? */
		new MemoryWriteAddress( 0xf200, 0xf200, bankswitch_w ),	/* ?? */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	/***********************************************************
				 INPUT PORTS, DIPs
	***********************************************************/
	
	static InputPortPtr input_ports_finalb = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Medium" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );/* 1P sen.sw.? */
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );/* 1P ducking? */
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* 2P sen.sw.? */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* 2P ducking? */
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_finalbj = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x40, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_2C") );
	
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Medium" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );/* 1P sen.sw.? */
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );/* 1P ducking? */
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* 2P sen.sw.? */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* 2P ducking? */
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_dondokod = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x40, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_2C") );
	
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Medium" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x0c, "10k 100k" );	PORT_DIPSETTING(    0x08, "10k 150k" );	PORT_DIPSETTING(    0x04, "10k 250k" );	PORT_DIPSETTING(    0x00, "10k 350k" );	PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x20, "2" );	PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x00, "4" );	PORT_DIPSETTING(    0x10, "5" );	PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_megab = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
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
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x0c, "10K, 110K, 210K, 310K..." );	PORT_DIPSETTING(    0x08, "20K, 220K, 420K, 620K..." );	PORT_DIPSETTING(    0x04, "15K, 165K, 365K, 515K..." );	PORT_DIPSETTING(    0x00, "No Bonus" );	PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x20, "4" );	PORT_DIPSETTING(    0x10, "1" );	PORT_DIPSETTING(    0x00, "2" );	PORT_DIPNAME( 0x40, 0x40, "Upright Controls" );/* ie single or two players at once */
		PORT_DIPSETTING(    0x00, "Single" );	PORT_DIPSETTING(    0x40, "Dual" );	PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_megabj = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x40, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_2C") );
	
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x0c, "10K, 110K, 210K, 310K..." );	PORT_DIPSETTING(    0x08, "20K, 220K, 420K, 620K..." );	PORT_DIPSETTING(    0x04, "15K, 165K, 365K, 515K..." );	PORT_DIPSETTING(    0x00, "No Bonus" );	PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x20, "4" );	PORT_DIPSETTING(    0x10, "1" );	PORT_DIPSETTING(    0x00, "2" );	PORT_DIPNAME( 0x40, 0x40, "Upright Controls" );/* ie single or two players at once */
		PORT_DIPSETTING(    0x00, "Single" );	PORT_DIPSETTING(    0x40, "Dual" );	PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_thundfox = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x04, 0x04, "Timer" );	PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x20, "2" );	PORT_DIPSETTING(    0x10, "4" );	PORT_DIPSETTING(    0x00, "5" );	PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x80, "Game Type" );	PORT_DIPSETTING(    0x80, "Double Control Panel" );	PORT_DIPSETTING(    0x00, "Single Control Panel" );
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_cameltry = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x40, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_2C") );
	
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x0c, 0x0c, "Start remain time" );	PORT_DIPSETTING(    0x00, "35" );	PORT_DIPSETTING(    0x04, "40" );	PORT_DIPSETTING(    0x0c, "50" );	PORT_DIPSETTING(    0x08, "60" );	PORT_DIPNAME( 0x30, 0x30, "Continue play time" );	PORT_DIPSETTING(    0x00, "+20" );	PORT_DIPSETTING(    0x10, "+25" );	PORT_DIPSETTING(    0x30, "+30" );	PORT_DIPSETTING(    0x20, "+40" );	PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );	PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "Pair Play" );	PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();   /* Paddle A */
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER1, 100, 20, 0, 0 );
		PORT_START();   /* Paddle B */
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER2, 100, 20, 0, 0 );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_qtorimon = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x40, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_2C") );
	
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x03, 0x03, "Difficulty?" );	PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_liquidk = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x0c, "30k 100k" );	PORT_DIPSETTING(    0x08, "30k 150k" );	PORT_DIPSETTING(    0x04, "50k 250k" );	PORT_DIPSETTING(    0x00, "50k 350k" );	PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x20, "2" );	PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x00, "4" );	PORT_DIPSETTING(    0x10, "5" );	PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_mizubaku = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x0c, "30k 100k" );	PORT_DIPSETTING(    0x08, "30k 150k" );	PORT_DIPSETTING(    0x04, "50k 250k" );	PORT_DIPSETTING(    0x00, "50k 350k" );	PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x20, "2" );	PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x00, "4" );	PORT_DIPSETTING(    0x10, "5" );	PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_ssi = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x0c, 0x0c, "Shields" );	PORT_DIPSETTING(    0x00, "None");	PORT_DIPSETTING(    0x0c, "1");	PORT_DIPSETTING(    0x04, "2");	PORT_DIPSETTING(    0x08, "3");	PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "2");	PORT_DIPSETTING(    0x10, "3");	PORT_DIPNAME( 0xa0, 0xa0, "2 Players Mode" );	PORT_DIPSETTING(    0xa0, "Simultaneous");	PORT_DIPSETTING(    0x80, "Alternate, Single");	PORT_DIPSETTING(    0x00, "Alternate, Dual");	PORT_DIPSETTING(    0x20, "Not Allowed");	PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_majest12 = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x0c, 0x0c, "Shields" );	PORT_DIPSETTING(    0x00, "None");	PORT_DIPSETTING(    0x0c, "1");	PORT_DIPSETTING(    0x04, "2");	PORT_DIPSETTING(    0x08, "3");	PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "2");	PORT_DIPSETTING(    0x10, "3");	PORT_DIPNAME( 0xa0, 0xa0, "2 Players Mode" );	PORT_DIPSETTING(    0xa0, "Simultaneous");	PORT_DIPSETTING(    0x80, "Alternate, Single Controls");	PORT_DIPSETTING(    0x00, "Alternate, Dual Controls");	PORT_DIPSETTING(    0x20, "Not Allowed");	PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_growl = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Medium" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, "Cabinet Type" );	PORT_DIPSETTING(    0x30, "2 Players" );	PORT_DIPSETTING(    0x20, "4 Players / 4 Coin Slots" );// Push Player button A to start
		PORT_DIPSETTING(    0x10, "4 Players / 2 cabinets combined" );	PORT_DIPSETTING(    0x00, "4 Players / 2 Coin Slots" );	PORT_DIPNAME( 0x40, 0x40, "Final Boss Continue" );	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN3 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );	PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );	PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER3 );	PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_START3 );	PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );	PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 );	PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER4 );	PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_START4 );
		PORT_START();       /* IN4 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN3 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN4 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_runark = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Medium" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, "Cabinet Type" );	PORT_DIPSETTING(    0x30, "2 Players" );	PORT_DIPSETTING(    0x20, "4 Players / 4 Coin Slots" );// Push Player button A to start
		PORT_DIPSETTING(    0x10, "4 Players / 2 cabinets combined" );	PORT_DIPSETTING(    0x00, "4 Players / 2 Coin Slots" );	PORT_DIPNAME( 0x40, 0x40, "Final Boss Continue" );	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN3 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );	PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );	PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER3 );	PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_START3 );	PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );	PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 );	PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER4 );	PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_START4 );
		PORT_START();       /* IN4 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN3 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN4 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_pulirula = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_pulirulj = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_qzquest = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_qzchikyu = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_footchmp = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN4 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_SERVICE2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_SERVICE3 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_TILT );
		PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x00, "Game Over Type" );// 2p simultaneous play
		PORT_DIPSETTING(    0x01, "Both Teams' Games Over" );	PORT_DIPSETTING(    0x00, "Losing Team's Game is Over" );	PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, "Coin Slot A" );	PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0xc0, 0xc0, "Coin Slot B" );	PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
	
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Very Hard" );	PORT_DIPNAME( 0x0c, 0x0c, "Game Time" );	PORT_DIPSETTING(    0x00, "1.5 Minutes" );	PORT_DIPSETTING(    0x0c, " 2  Minutes" );	PORT_DIPSETTING(    0x04, "2.5 Minutes" );	PORT_DIPSETTING(    0x08, " 3  Minutes" );	PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x30, "2 Players" );	PORT_DIPSETTING(    0x20, "4 Players / 4 Coin Slots" );// Push Player button A to start
		PORT_DIPSETTING(    0x10, "4 Players / 2 cabinets combined" );	PORT_DIPSETTING(    0x00, "4 Players / 2 Coin Slots" );	PORT_DIPNAME( 0x40, 0x40, "Continue" );	PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, "Game Version" );// Not used for Hat Trick Hero / Euro Champ '92
		PORT_DIPSETTING(    0x00, "Normal" );	PORT_DIPSETTING(    0x80, "European" );
		PORT_START();       /* IN3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START3);
		PORT_START();       /* IN4 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START4);INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_hthero = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN4 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_SERVICE2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_SERVICE3 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_TILT );
		PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_SERVICE( 0x20, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, "Game Over Type" );// 2p simultaneous play
		PORT_DIPSETTING(    0x80, "Both Teams' Games Over" );	PORT_DIPSETTING(    0x00, "Losing Team's Game is Over" );
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Allow Continue" );	PORT_DIPSETTING(    0x02, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x0c, "2 Players" );	PORT_DIPSETTING(    0x04, "4 Players / 4 Coin Slots" );// Push Player button A to start
		PORT_DIPSETTING(    0x08, "4 Players / 2 cabinets combined" );	PORT_DIPSETTING(    0x00, "4 Players / 2 Coin Slots" );	PORT_DIPNAME( 0x30, 0x30, "Game Time" );	PORT_DIPSETTING(    0x00, "1.5 Minutes" );	PORT_DIPSETTING(    0x30, " 2  Minutes" );	PORT_DIPSETTING(    0x20, "2.5 Minutes" );	PORT_DIPSETTING(    0x10, " 3  Minutes" );	PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x40, "Easy" );	PORT_DIPSETTING(    0xc0, "Normal" );	PORT_DIPSETTING(    0x80, "Hard" );	PORT_DIPSETTING(    0x00, "Very Hard" );
		PORT_START();       /* IN3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START3);
		PORT_START();       /* IN4 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START4);INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_ninjak = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN3 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN4 );
		PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x0c, 0x0c, "Cabinet Type" );	PORT_DIPSETTING(    0x0c, "2 players" );	PORT_DIPSETTING(    0x08, "TROG (4 players / 2 coin slots); )
		PORT_DIPSETTING(    0x04, "MTX2 (4 players / 2 cabinets combined); )
		PORT_DIPSETTING(    0x00, "TMNT (4 players / 4 coin slots); )
		PORT_DIPNAME( 0x30, 0x10, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x20, "2" );	PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x10, "4" );	PORT_DIPSETTING(    0x00, "5" );	PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x80, "Game Type" );	PORT_DIPSETTING(    0x00, "1 Player only" );	PORT_DIPSETTING(    0x80, "Multiplayer" );
		PORT_START();       /* IN3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START3 );
		PORT_START();       /* IN4 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START4 );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_ninjakj = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN3 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN4 );
		PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x0c, 0x0c, "Cabinet Type" );	PORT_DIPSETTING(    0x0c, "2 players" );	PORT_DIPSETTING(    0x08, "TROG (4 players / 2 coin slots); )
		PORT_DIPSETTING(    0x04, "MTX2 (4 players / 2 cabinets combined); )
		PORT_DIPSETTING(    0x00, "TMNT (4 players / 4 coin slots); )
		PORT_DIPNAME( 0x30, 0x10, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x20, "2" );	PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x10, "4" );	PORT_DIPSETTING(    0x00, "5" );	PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x80, "Game Type" );	PORT_DIPSETTING(    0x00, "1 Player only" );	PORT_DIPSETTING(    0x80, "Multiplayer" );
		PORT_START();       /* IN3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START3 );
		PORT_START();       /* IN4 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START4 );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_driftout = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x40, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_2C") );
	
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x0c, 0x0c, "Control" );  /* correct acc. to service mode */
		PORT_DIPSETTING(    0x0c, "Joystick" );	PORT_DIPSETTING(    0x08, "Paddle" );	PORT_DIPSETTING(    0x04, "Joystick" );	PORT_DIPSETTING(    0x00, "Steering wheel" );	PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNKNOWN );/* 2P not used? */
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();   /* Paddle A */
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER1, 50, 10, 0, 0 );
		PORT_START();   /* Paddle B */
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER2, 50, 10, 0, 0 );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_gunfront = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Medium" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Unknown") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x20, "1" );	PORT_DIPSETTING(    0x10, "2" );	PORT_DIPSETTING(    0x00, "5" );	PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x80, "Pair Play" );	PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1);	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_gunfronj = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Medium" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Unknown") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x20, "1" );	PORT_DIPSETTING(    0x10, "2" );	PORT_DIPSETTING(    0x00, "5" );	PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x80, "Pair Play" );	PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1);	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_metalb = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Medium" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Unknown") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x20, "5" );	PORT_DIPSETTING(    0x10, "2" );	PORT_DIPSETTING(    0x00, "1" );	PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x80, "Pair Play" );	PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1);	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_metalbj = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Medium" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Unknown") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x20, "5" );	PORT_DIPSETTING(    0x10, "2" );	PORT_DIPSETTING(    0x00, "1" );	PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x80, "Pair Play" );	PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1);	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_deadconx = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN4 );	PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_SERVICE, "Service A", KEYCODE_9, IP_JOY_NONE );	PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_SERVICE, "Service B", KEYCODE_0, IP_JOY_NONE );	PORT_BITX(0x40, IP_ACTIVE_LOW, IPT_SERVICE, "Service C", KEYCODE_MINUS, IP_JOY_NONE );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_TILT );
		PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
	
		PORT_START();  /* DSW B, missing a timer speed maybe? */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Medium" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On"));
		PORT_DIPNAME( 0x18, 0x18, "Damage" );	PORT_DIPSETTING(    0x18, "Normal" );/* Hero can take 10 gun shots */
		PORT_DIPSETTING(    0x10, "Small" );	/* Hero can take 12 gun shots */
		PORT_DIPSETTING(    0x08, "Big" );	/* Hero can take 8 gun shots */
		PORT_DIPSETTING(    0x00, "Biggest" );/* Hero can take 5 gun shots */
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On"));
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "Game Type" );	PORT_DIPSETTING(    0x00, "1 Player only" );	PORT_DIPSETTING(    0x80, "Multiplayer" );
		PORT_START();       /* IN3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START3 );
		PORT_START();       /* IN4 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START4 );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_deadconj = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN4 );	PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_SERVICE, "Service A", KEYCODE_9, IP_JOY_NONE );	PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_SERVICE, "Service B", KEYCODE_0, IP_JOY_NONE );	PORT_BITX(0x40, IP_ACTIVE_LOW, IPT_SERVICE, "Service C", KEYCODE_MINUS, IP_JOY_NONE );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_TILT );
		PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x20, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_2C") );
	
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x40, "Easy" );	PORT_DIPSETTING(    0xc0, "Medium" );	PORT_DIPSETTING(    0x80, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On"));
		PORT_DIPNAME( 0x18, 0x18, "Damage" );	PORT_DIPSETTING(    0x18, "Normal" );/* Hero can take 10 gun shots */
		PORT_DIPSETTING(    0x08, "Small" );	/* Hero can take 12 gun shots */
		PORT_DIPSETTING(    0x10, "Big" );	/* Hero can take 8 gun shots */
		PORT_DIPSETTING(    0x00, "Biggest" );/* Hero can take 5 gun shots */
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On"));
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x01, 0x01, "Game Type" );	PORT_DIPSETTING(    0x00, "1 Player only" );	PORT_DIPSETTING(    0x01, "Multiplayer" );
		PORT_START();       /* IN3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START3 );
		PORT_START();       /* IN4 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START4 );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_dinorex = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x0c, 0x0c, "Damage" );	PORT_DIPSETTING(    0x0c, "Normal" );	PORT_DIPSETTING(    0x08, "Small" );	PORT_DIPSETTING(    0x04, "Big" );	PORT_DIPSETTING(    0x00, "Biggest" );	PORT_DIPNAME( 0x10, 0x10, "Timer Speed" ); // Appears to make little difference
		PORT_DIPSETTING(    0x10, "Normal" );	PORT_DIPSETTING(    0x00, "Fast" );	PORT_DIPNAME( 0x20, 0x20, "Match Type" );// Raine says "Points to Win"
		PORT_DIPSETTING(    0x20, "Best of 3" );	PORT_DIPSETTING(    0x00, "Single" );	PORT_DIPNAME( 0x40, 0x40, "2 Player Mode" );	PORT_DIPSETTING(    0x40, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_dinorexj = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x0c, 0x0c, "Damage" );	PORT_DIPSETTING(    0x0c, "Normal" );	PORT_DIPSETTING(    0x08, "Small" );	PORT_DIPSETTING(    0x04, "Big" );	PORT_DIPSETTING(    0x00, "Biggest" );	PORT_DIPNAME( 0x10, 0x10, "Timer Speed" ); // Appears to make little difference
		PORT_DIPSETTING(    0x10, "Normal" );	PORT_DIPSETTING(    0x00, "Fast" );	PORT_DIPNAME( 0x20, 0x20, "Match Type" );// Raine says "Points to Win"
		PORT_DIPSETTING(    0x20, "Best of 3" );	PORT_DIPSETTING(    0x00, "Single" );	PORT_DIPNAME( 0x40, 0x40, "2 Player Mode" );	PORT_DIPSETTING(    0x40, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_solfigtr = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );	/* For North America Coin B is "Buy in"/Continue */
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") );	/* Buy in = Same as current Coin A */
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_3C") );	/* Buy in = 1 Coin to Continue */
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_4C") );	/* Buy in = 2 Coin to Continue */
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );	/* Buy in = 3 Coin to Continue */
	
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_koshien = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
	
		PORT_START();  /* DSW B, some wrong ? */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x04, 0x04, "Timer" );	PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x20, "2" );	PORT_DIPSETTING(    0x10, "4" );	PORT_DIPSETTING(    0x00, "5" );	PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x80, "Game Type" );	PORT_DIPSETTING(    0x80, "Double Control Panel" );	PORT_DIPSETTING(    0x00, "Single Control Panel" );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_quizhq = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER1 );// ??
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER2 );// ??
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
	
		PORT_START();  /* DSW B, wrong */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_qjinsei = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
	
		PORT_START();  /* DSW B, wrong */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER1 );// ??
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER2 );// ??
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_qcrayon = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
	
		PORT_START();  /* DSW B, wrong */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER1 );// ??
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER2 );// ??
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_qcrayon2 = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
	
		PORT_START();  /* DSW B, wrong */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_yuyugogo = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
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
	
		PORT_START();  /* DSW B, wrong */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER1 );// ??
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER2 );// ??
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_mjnquest = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* IN0 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 A", KEYCODE_A, IP_JOY_NONE );	PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 E", KEYCODE_E, IP_JOY_NONE );	PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 I", KEYCODE_I, IP_JOY_NONE );	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 M", KEYCODE_M, IP_JOY_NONE );	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Kan", KEYCODE_LCONTROL, IP_JOY_NONE );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0xc0, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START();       /* IN1 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 B", KEYCODE_B, IP_JOY_NONE );	PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 F", KEYCODE_F, IP_JOY_NONE );	PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 J", KEYCODE_J, IP_JOY_NONE );	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 N", KEYCODE_N, IP_JOY_NONE );	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Reach", KEYCODE_LSHIFT, IP_JOY_NONE );	PORT_BIT( 0xe0, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START();       /* IN2 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 C", KEYCODE_C, IP_JOY_NONE );	PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 G", KEYCODE_G, IP_JOY_NONE );	PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 K", KEYCODE_K, IP_JOY_NONE );	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Chi", KEYCODE_SPACE, IP_JOY_NONE );	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Ron", KEYCODE_Z, IP_JOY_NONE );	PORT_BIT( 0xe0, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START();       /* IN3 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 D", KEYCODE_D, IP_JOY_NONE );	PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 H", KEYCODE_H, IP_JOY_NONE );	PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 L", KEYCODE_L, IP_JOY_NONE );	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Pon", KEYCODE_LALT, IP_JOY_NONE );	PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START();       /* IN4 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START();       /* IN5 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );	// ?
		PORT_BIT( 0xfc, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START();       /* IN6 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0xfc, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START();       /* IN7:DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x00, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* IN8:DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Rank A (Easy); )
		PORT_DIPSETTING(    0x03, "Rank B (Normal); )
		PORT_DIPSETTING(    0x01, "Rank C (Hard); )
		PORT_DIPSETTING(    0x00, "Rank D (Hardest); )
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_yesnoj = new InputPortPtr(){ public void handler() {    // want to get into test mode to be sure of this lot
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START();  /* DSW A ??? */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();  /* DSW B ? */
		PORT_SERVICE( 0x01, IP_ACTIVE_HIGH );  // ??? makes strange siren sounds happen
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coinage") );   // not verified; coinage seems same for both slots
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );   // 2c-3c ??
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );   // 1c-2c ??
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	/***********************************************************
					GFX DECODING
	***********************************************************/
	
	static GfxLayout finalb_tilelayout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		RGN_FRAC(1,2),
		6,	/* 6 bits per pixel */
		new int[] { RGN_FRAC(1,2)+0, RGN_FRAC(1,2)+1, 0, 1, 2, 3 },
		new int[] { 3*4, 2*4, 1*4, 0*4, 7*4, 6*4, 5*4, 4*4,
				11*4, 10*4, 9*4, 8*4, 15*4, 14*4, 13*4, 12*4 },
		new int[] { 0*64, 1*64, 2*64, 3*64, 4*64, 5*64, 6*64, 7*64,
				8*64, 9*64, 10*64, 11*64, 12*64, 13*64, 14*64, 15*64 },
		128*8	/* every sprite takes 128 consecutive bytes */
	);
	
	static GfxLayout tilelayout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		RGN_FRAC(1,1),
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 1*4, 0*4, 3*4, 2*4, 5*4, 4*4, 7*4, 6*4, 9*4, 8*4, 11*4, 10*4, 13*4, 12*4, 15*4, 14*4 },
		new int[] { 0*64, 1*64, 2*64, 3*64, 4*64, 5*64, 6*64, 7*64, 8*64, 9*64, 10*64, 11*64, 12*64, 13*64, 14*64, 15*64 },
		128*8	/* every sprite takes 128 consecutive bytes */
	);
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		RGN_FRAC(1,1),
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 2*4, 3*4, 0*4, 1*4, 6*4, 7*4, 4*4, 5*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8	/* every sprite takes 32 consecutive bytes */
	);
	
	static GfxLayout yuyugogo_charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		RGN_FRAC(1,1),
		1,	/* 1 bit per pixel */
		new int[] { 0 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every sprite takes 8 consecutive bytes */
	);
	
	static GfxLayout pivotlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		RGN_FRAC(1,1),
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8	/* every sprite takes 32 consecutive bytes */
	);
	
	static GfxDecodeInfo finalb_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX2, 0, finalb_tilelayout,  0, 64 ),	/* sprites  playfield, 6-bit deep */
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,  0, 256 ),	/* sprites  playfield */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo taitof2_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX2, 0, tilelayout,  0, 256 ),	/* sprites  playfield */
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,  0, 256 ),	/* sprites  playfield */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo pivot_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX2, 0, tilelayout,  0, 256 ),	/* sprites  playfield */
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,  0, 256 ),	/* sprites  playfield */
		new GfxDecodeInfo( REGION_GFX3, 0, pivotlayout, 0, 256 ),	/* sprites  playfield */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo yuyugogo_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX2, 0, tilelayout,  0, 256 ),	/* sprites  playfield */
		new GfxDecodeInfo( REGION_GFX1, 0, yuyugogo_charlayout,  0, 256 ),	/* sprites  playfield */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo thundfox_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX2, 0, tilelayout,  0, 256 ),	/* sprites  playfield */
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,  0, 256 ),	/* TC0100SCN #1 */
		new GfxDecodeInfo( REGION_GFX3, 0, charlayout,  0, 256 ),	/* TC0100SCN #2 */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxLayout deadconx_charlayout = new GfxLayout
	(
		16,16,    /* 16*16 characters */
		8192,     /* 8192 total characters */
		4,        /* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 1*4, 0*4, 5*4, 4*4, 3*4, 2*4, 7*4, 6*4, 9*4, 8*4, 13*4, 12*4, 11*4, 10*4, 15*4, 14*4 },
		new int[] { 0*64, 1*64, 2*64, 3*64, 4*64, 5*64, 6*64, 7*64, 8*64, 9*64, 10*64, 11*64, 12*64, 13*64, 14*64, 15*64 },
		128*8     /* every sprite takes 128 consecutive bytes */
	);
	
	static GfxDecodeInfo deadconx_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX2, 0, tilelayout,  0, 256 ),	/* sprites  playfield */
		new GfxDecodeInfo( REGION_GFX1, 0, deadconx_charlayout,  0, 256 ),	/* sprites  playfield */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	/* handler called by the YM2610 emulator when the internal timers cause an IRQ */
	static void irqhandler(int irq)
	{
		cpu_set_irq_line(1,0,irq ? ASSERT_LINE : CLEAR_LINE);
	}
	
	static YM2610interface ym2610_interface = new YM2610interface
	(
		1,	/* 1 chip */
		16000000/2,	/* 8 MHz */
		new int[] { 30 },
		new ReadHandlerPtr[] { 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteYmHandlerPtr[] { irqhandler },
		new int[] { REGION_SOUND2 },	/* Delta-T */
		new int[] { REGION_SOUND1 },	/* ADPCM */
		new int[] { YM3012_VOL(60,MIXER_PAN_LEFT,60,MIXER_PAN_RIGHT) }
	);
	
	
	/***********************************************************
				     MACHINE DRIVERS
	***********************************************************/
	
	static public static InitMachinePtr init_machine_qcrayon = new InitMachinePtr() { public void handler() 
	{
		/* point to the extra ROM */
		cpu_setbank(8,memory_region(REGION_USER1));
	} };
	
	#define init_machine_0 0
	
	#define MACHINE_DRIVER(NAME,INIT,MAXCOLS,GFX,VHSTART,VHREFRESH,EOF)							\
	static MachineDriver machine_driver_##NAME = new MachineDriver\
	(																					\
		/* basic machine hardware */													\
		new MachineCPU[] {																				\
			new MachineCPU(																			\
				CPU_M68000,																\
				24000000/2,	/* 12 MHz */												\
				NAME##_readmem, NAME##_writemem,null,null,									\
				taitof2_interrupt,1														\
			),																			\
			new MachineCPU(																			\
				CPU_Z80 | CPU_AUDIO_CPU,												\
				16000000/4,	/* 4 MHz */													\
				sound_readmem, sound_writemem,null,null,										\
				ignore_interrupt,0	/* IRQs are triggered by the YM2610 */				\
			)																			\
		},																				\
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */	\
		1,																				\
		init_machine_##INIT,															\
																						\
		/* video hardware */															\
		40*8, 32*8, new rectangle( 0*8, 40*8-1, 2*8, 30*8-1 ),										\
																						\
		GFX##_gfxdecodeinfo,															\
		MAXCOLS, MAXCOLS,																		\
		null,																				\
																						\
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,										\
		taitof2_##EOF##_eof_callback,													\
		taitof2_##VHSTART##_vh_start,													\
		taitof2_vh_stop,																\
		VHREFRESH##_vh_screenrefresh,													\
																						\
		/* sound hardware */															\
		SOUND_SUPPORTS_STEREO,0,0,0,													\
		new MachineSound[] {																				\
			new MachineSound(																			\
				SOUND_YM2610,															\
				ym2610_interface														\
			)																			\
		}																				\
	);
	
	#define hthero_readmem		footchmp_readmem
	#define hthero_writemem		footchmp_writemem
	#define deadconj_readmem	deadconx_readmem
	#define deadconj_writemem	deadconx_writemem
	
	/*              NAME      INIT     MAXCOLS	GFX       VHSTART   VHREFRESH        EOF*/
	MACHINE_DRIVER( finalb,   0,       4096,		finalb,   finalb,   taitof2,         partial_buffer_delayed )
	MACHINE_DRIVER( dondokod, 0,       4096,		pivot,    dondokod, taitof2_pri_roz, partial_buffer_delayed )
	MACHINE_DRIVER( megab,    0,       4096,		taitof2,  3p,       taitof2_pri,     no_buffer )
	MACHINE_DRIVER( thundfox, 0,       4096,		thundfox, thundfox, thundfox,        partial_buffer_delayed_thundfox )
	MACHINE_DRIVER( cameltry, 0,       4096,		pivot,    dondokod, taitof2_pri_roz, no_buffer )
	MACHINE_DRIVER( qtorimon, 0,       4096,		yuyugogo, default,  taitof2,         partial_buffer_delayed )
	MACHINE_DRIVER( liquidk,  0,       4096,		taitof2,  3p,       taitof2_pri,     partial_buffer_delayed )
	MACHINE_DRIVER( quizhq,   0,       4096,		yuyugogo, default,  taitof2,         partial_buffer_delayed )
	MACHINE_DRIVER( ssi,      0,       4096,		taitof2,  ssi,      ssi,             partial_buffer_delayed )
	MACHINE_DRIVER( gunfront, 0,       4096,		taitof2,  gunfront, taitof2_pri,     no_buffer )
	MACHINE_DRIVER( growl,    0,       4096,		taitof2,  growl,    taitof2_pri,     no_buffer )
	MACHINE_DRIVER( mjnquest, 0,       4096,		taitof2,  mjnquest, taitof2,         no_buffer )
	MACHINE_DRIVER( footchmp, 0,       4096,		deadconx, footchmp, deadconx,        no_buffer )
	MACHINE_DRIVER( hthero,   0,       4096,		deadconx, hthero,   deadconx,        no_buffer )
	MACHINE_DRIVER( koshien,  0,       4096,		taitof2,  3p_buf,   taitof2_pri,     no_buffer )
	MACHINE_DRIVER( yuyugogo, qcrayon, 4096,		yuyugogo, yuyugogo, yesnoj,          no_buffer )
	MACHINE_DRIVER( ninjak,   0,       4096,		taitof2,  ninjak,   taitof2_pri,     no_buffer )
	MACHINE_DRIVER( solfigtr, 0,       4096,		taitof2,  3p_buf,   taitof2_pri,     no_buffer )
	MACHINE_DRIVER( qzquest,  0,       4096,		taitof2,  default,  taitof2,         partial_buffer_delayed )
	MACHINE_DRIVER( pulirula, 0,       4096,		pivot,    pulirula, taitof2_pri_roz, no_buffer )
	MACHINE_DRIVER( metalb,   0,       8192,		deadconx, metalb,   metalb,          no_buffer )
	MACHINE_DRIVER( qzchikyu, 0,       4096,		taitof2,  default,  taitof2,         no_buffer )
	MACHINE_DRIVER( yesnoj,   0,       4096,		yuyugogo, yesnoj,   yesnoj,          no_buffer )
	MACHINE_DRIVER( deadconx, 0,       4096,		deadconx, deadconx, deadconx,        no_buffer )
	MACHINE_DRIVER( deadconj, 0,       4096,		deadconx, deadconj, deadconx,        no_buffer )
	MACHINE_DRIVER( dinorex,  0,       4096,		taitof2,  dinorex,  taitof2_pri,     no_buffer )
	MACHINE_DRIVER( qjinsei,  0,       4096,		taitof2,  c,        taitof2_pri,     no_buffer )
	MACHINE_DRIVER( qcrayon,  qcrayon, 4096,		taitof2,  c,        taitof2_pri,     no_buffer )
	MACHINE_DRIVER( qcrayon2, qcrayon, 4096,		taitof2,  c,        taitof2_pri,     no_buffer )
	MACHINE_DRIVER( driftout, 0,       4096,		pivot,    driftout, taitof2_pri_roz, no_buffer )
	
	
	
	/***************************************************************************
						DRIVERS
	***************************************************************************/
	
	static RomLoadPtr rom_finalb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );    /* 256k for 68000 code */
		ROM_LOAD_EVEN( "b82-09",     0x00000, 0x20000, 0x632f1ecd );	ROM_LOAD_ODD ( "b82-17",     0x00000, 0x20000, 0xe91b2ec9 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD_GFX_EVEN( "b82-06",     0x000000, 0x020000, 0xfc450a25 )
		ROM_LOAD_GFX_ODD ( "b82-07",     0x000000, 0x020000, 0xec3df577 )
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD_GFX_EVEN( "b82-04",     0x000000, 0x080000, 0x6346f98e ) /* sprites 4-bit format*/
		ROM_LOAD_GFX_ODD ( "b82-03",     0x000000, 0x080000, 0xdaa11561 ) /* sprites 4-bit format*/
	
		/*Note:
		**this is intentional to load at 0x180000, not at 0x100000
		**because finalb_driver_init will move some bits around before data will be 'gfxdecoded'.
		**The whole thing is because this data is 2bits- while above is 4bits-packed format,
		**for a total of 6 bits per pixel.
		*/
		ROM_LOAD         ( "b82-05",     0x180000, 0x080000, 0xaa90b93a );/* sprites 2-bit format*/
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "b82-10",      0x00000, 0x04000, 0xa38aaaed );	ROM_CONTINUE(            0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "b82-02",      0x00000, 0x80000, 0x5dd06bdd );
		ROM_REGION( 0x80000, REGION_SOUND2 );/* Delta-T samples */
		ROM_LOAD( "b82-01",      0x00000, 0x80000, 0xf0eb6846 );ROM_END(); }}; 
	
	static RomLoadPtr rom_finalbj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );    /* 256k for 68000 code */
		ROM_LOAD_EVEN( "b82-09",     0x00000, 0x20000, 0x632f1ecd );	ROM_LOAD_ODD ( "b82-08",     0x00000, 0x20000, 0x07154fe5 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD_GFX_EVEN( "b82-06",     0x000000, 0x020000, 0xfc450a25 )
		ROM_LOAD_GFX_ODD ( "b82-07",     0x000000, 0x020000, 0xec3df577 )
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD_GFX_EVEN( "b82-04",     0x000000, 0x080000, 0x6346f98e ) /* sprites 4-bit format*/
		ROM_LOAD_GFX_ODD ( "b82-03",     0x000000, 0x080000, 0xdaa11561 ) /* sprites 4-bit format*/
	
		/*Note:
		**this is intentional to load at 0x180000, not at 0x100000
		**because finalb_driver_init will move some bits around before data will be 'gfxdecoded'.
		**The whole thing is because this data is 2bits- while above is 4bits-packed format,
		**for a total of 6 bits per pixel.
		*/
		ROM_LOAD         ( "b82-05",     0x180000, 0x080000, 0xaa90b93a );/* sprites 2-bit format*/
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "b82-10",      0x00000, 0x04000, 0xa38aaaed );	ROM_CONTINUE(            0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "b82-02",      0x00000, 0x80000, 0x5dd06bdd );
		ROM_REGION( 0x80000, REGION_SOUND2 );/* Delta-T samples */
		ROM_LOAD( "b82-01",      0x00000, 0x80000, 0xf0eb6846 );ROM_END(); }}; 
	
	static RomLoadPtr rom_dondokod = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );    /* 512k for 68000 code */
		ROM_LOAD_EVEN( "b95-12.bin",   0x00000, 0x20000, 0xd0fce87a );	ROM_LOAD_ODD ( "b95-11-1.bin", 0x00000, 0x20000, 0xdad40cd3 );	ROM_LOAD_EVEN( "b95-10.bin",   0x40000, 0x20000, 0xa46e1f0b );	ROM_LOAD_ODD ( "b95-09.bin",   0x40000, 0x20000, 0xd8c86d39 );
		ROM_REGION( 0x080000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "b95-02.bin", 0x000000, 0x080000, 0x67b4e979 ); /* background/foreground */
	
		ROM_REGION( 0x080000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "b95-01.bin", 0x000000, 0x080000, 0x51c176ce ); /* sprites */
	
		ROM_REGION( 0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE );	ROM_LOAD( "b95-03.bin", 0x000000, 0x080000, 0x543aa0d1 );    /* pivot graphics */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "b95-08.bin",  0x00000, 0x04000, 0xb5aa49e1 );	ROM_CONTINUE(            0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* ADPCM samples? */
		ROM_LOAD( "b95-04.bin",  0x00000, 0x80000, 0xac4c1716 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_megab = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );    /* 256k for 68000 code */
		ROM_LOAD_EVEN( "c11-07",    0x00000, 0x20000, 0x11d228b6 );	ROM_LOAD_ODD ( "c11-08",    0x00000, 0x20000, 0xa79d4dca );	ROM_LOAD_EVEN( "c11-06",    0x40000, 0x20000, 0x7c249894 );/* ?? */
		ROM_LOAD_ODD ( "c11-11",    0x40000, 0x20000, 0x263ecbf9 );/* ?? */
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c11-05", 0x000000, 0x080000, 0x733e6d8e );
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD_GFX_EVEN( "c11-03", 0x000000, 0x080000, 0x46718c7a )
		ROM_LOAD_GFX_ODD ( "c11-04", 0x000000, 0x080000, 0x663f33cc )
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "c11-12", 0x00000, 0x04000, 0xb11094f1 );	ROM_CONTINUE(       0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "c11-01", 0x00000, 0x80000, 0xfd1ea532 );
		ROM_REGION( 0x80000, REGION_SOUND2 );/* Delta-T samples */
		ROM_LOAD( "c11-02", 0x00000, 0x80000, 0x451cc187 );ROM_END(); }}; 
	
	static RomLoadPtr rom_megabj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );    /* 256k for 68000 code */
		ROM_LOAD_EVEN( "c11-07",    0x00000, 0x20000, 0x11d228b6 );	ROM_LOAD_ODD ( "c11-08",    0x00000, 0x20000, 0xa79d4dca );	ROM_LOAD_EVEN( "c11-06",    0x40000, 0x20000, 0x7c249894 );/* ?? */
		ROM_LOAD_ODD ( "c11-09.18", 0x40000, 0x20000, 0xc830aad5 );/* ?? */
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c11-05", 0x000000, 0x080000, 0x733e6d8e );
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD_GFX_EVEN( "c11-03", 0x000000, 0x080000, 0x46718c7a )
		ROM_LOAD_GFX_ODD ( "c11-04", 0x000000, 0x080000, 0x663f33cc )
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "c11-12", 0x00000, 0x04000, 0xb11094f1 );	ROM_CONTINUE(       0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "c11-01", 0x00000, 0x80000, 0xfd1ea532 );
		ROM_REGION( 0x80000, REGION_SOUND2 );/* Delta-T samples */
		ROM_LOAD( "c11-02", 0x00000, 0x80000, 0x451cc187 );ROM_END(); }}; 
	
	static RomLoadPtr rom_thundfox = new RomLoadPtr(){ public void handler(){ 		/* Thunder Fox */
		ROM_REGION( 0x80000, REGION_CPU1 );    /* 512k for 68000 code */
		ROM_LOAD_EVEN( "c28mainh.13", 0x00000, 0x20000, 0xacb07013 );	ROM_LOAD_ODD ( "c28mainl.12", 0x00000, 0x20000, 0xf04db477 );	ROM_LOAD_EVEN( "c28hi.08",    0x40000, 0x20000, 0x38e038f1 );	ROM_LOAD_ODD ( "c28lo.07",    0x40000, 0x20000, 0x24419abb );
		ROM_REGION( 0x080000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c28scr1.01", 0x000000, 0x080000, 0x6230a09d );/* TC0100SCN #1 */
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD_GFX_EVEN( "c28objl.03", 0x000000, 0x080000, 0x51bdc7af )
		ROM_LOAD_GFX_ODD ( "c28objh.04", 0x000000, 0x080000, 0xba7ed535 )
	
		ROM_REGION( 0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c28scr2.01", 0x000000, 0x080000, 0x44552b25 );/* TC0100SCN #2 */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "c28snd.14", 0x00000, 0x04000, 0x45ef3616 );	ROM_CONTINUE(          0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "c28snda.06", 0x00000, 0x80000, 0xdb6983db );
		ROM_REGION( 0x80000, REGION_SOUND2 );/* Delta-T samples */
		ROM_LOAD( "c28sndb.05", 0x00000, 0x80000, 0xd3b238fa );ROM_END(); }}; 
	
	static RomLoadPtr rom_cameltry = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );    /* 256k for 68000 code */
		ROM_LOAD_EVEN( "c38-09.bin", 0x00000, 0x20000, 0x2ae01120 );	ROM_LOAD_ODD ( "c38-10.bin", 0x00000, 0x20000, 0x48d8ff56 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );     /* UNUSED! */
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c38-01.bin", 0x000000, 0x080000, 0xc170ff36 ); /* sprites */
	
		ROM_REGION( 0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c38-02.bin", 0x000000, 0x020000, 0x1a11714b ); /* pivot graphics */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "c38-08.bin", 0x00000, 0x04000, 0x7ff78873 );	ROM_CONTINUE(           0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "c38-03.bin", 0x000000, 0x020000, 0x59fa59a7 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_cameltru = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );    /* 256k for 68000 code */
		ROM_LOAD_EVEN( "c38-11", 0x00000, 0x20000, 0xbe172da0 );	ROM_LOAD_ODD ( "c38-14", 0x00000, 0x20000, 0xffa430de );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );     /* UNUSED! */
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c38-01.bin", 0x000000, 0x080000, 0xc170ff36 ); /* sprites */
	
		ROM_REGION( 0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c38-02.bin", 0x000000, 0x020000, 0x1a11714b ); /* pivot graphics */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "c38-08.bin", 0x00000, 0x04000, 0x7ff78873 );	ROM_CONTINUE(           0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "c38-03.bin", 0x000000, 0x020000, 0x59fa59a7 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_qtorimon = new RomLoadPtr(){ public void handler(){ 	/* Quiz Torimonochou */
		ROM_REGION( 0x80000, REGION_CPU1 );    /* 256k for 68000 code */
		ROM_LOAD_EVEN( "c41-04.bin",  0x00000, 0x20000, 0x0fbf5223 );/* Prog1 */
		ROM_LOAD_ODD ( "c41-05.bin",  0x00000, 0x20000, 0x174bd5db );/* Prog2 */
		ROM_LOAD_EVEN( "mask-51.bin", 0x40000, 0x20000, 0x12e14aca );/* char defs, read by cpu */
		ROM_LOAD_ODD ( "mask-52.bin", 0x40000, 0X20000, 0xb3ef66f3 );/* char defs, read by cpu */
	
		ROM_REGION( 0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE );     /* UNUSED! */
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD_GFX_EVEN( "c41-02.bin",  0x000000, 0x20000, 0x05dcd36d ) /* Object */
		ROM_LOAD_GFX_ODD ( "c41-01.bin",  0x000000, 0x20000, 0x39ff043c ) /* Object */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "c41-06.bin",    0x00000, 0x04000, 0x753a98d8 );/* AUD Prog */
		ROM_CONTINUE(          0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x080000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "c41-03.bin",  0x000000, 0x020000, 0xb2c18e89 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_liquidk = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );    /* 512k for 68000 code */
		ROM_LOAD_EVEN( "lq09.bin",  0x00000, 0x20000, 0x6ae09eb9 );	ROM_LOAD_ODD ( "lq11.bin",  0x00000, 0x20000, 0x42d2be6e );	ROM_LOAD_EVEN( "lq10.bin",  0x40000, 0x20000, 0x50bef2e0 );	ROM_LOAD_ODD ( "lq12.bin",  0x40000, 0x20000, 0xcb16bad5 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "lk_scr.bin",  0x000000, 0x080000, 0xc3364f9b );
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "lk_obj0.bin", 0x000000, 0x080000, 0x67cc3163 );	ROM_LOAD( "lk_obj1.bin", 0x080000, 0x080000, 0xd2400710 );
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "lq08.bin",    0x00000, 0x04000, 0x413c310c );	ROM_CONTINUE(            0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "lk_snd.bin",  0x00000, 0x80000, 0x474d45a4 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_liquidku = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );    /* 512k for 68000 code */
		ROM_LOAD_EVEN( "lq09.bin",  0x00000, 0x20000, 0x6ae09eb9 );	ROM_LOAD_ODD ( "lq11.bin",  0x00000, 0x20000, 0x42d2be6e );	ROM_LOAD_EVEN( "lq10.bin",  0x40000, 0x20000, 0x50bef2e0 );	ROM_LOAD_ODD ( "lq14.bin",  0x40000, 0x20000, 0xbc118a43 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "lk_scr.bin",  0x000000, 0x080000, 0xc3364f9b );
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "lk_obj0.bin", 0x000000, 0x080000, 0x67cc3163 );	ROM_LOAD( "lk_obj1.bin", 0x080000, 0x080000, 0xd2400710 );
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "lq08.bin",    0x00000, 0x04000, 0x413c310c );	ROM_CONTINUE(            0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "lk_snd.bin",  0x00000, 0x80000, 0x474d45a4 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_mizubaku = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );    /* 512k for 68000 code */
		ROM_LOAD_EVEN( "lq09.bin",  0x00000, 0x20000, 0x6ae09eb9 );	ROM_LOAD_ODD ( "lq11.bin",  0x00000, 0x20000, 0x42d2be6e );	ROM_LOAD_EVEN( "lq10.bin",  0x40000, 0x20000, 0x50bef2e0 );	ROM_LOAD_ODD ( "c49-13",    0x40000, 0x20000, 0x2518dbf9 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "lk_scr.bin",  0x000000, 0x080000, 0xc3364f9b );
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "lk_obj0.bin", 0x000000, 0x080000, 0x67cc3163 );	ROM_LOAD( "lk_obj1.bin", 0x080000, 0x080000, 0xd2400710 );
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "lq08.bin",    0x00000, 0x04000, 0x413c310c );	ROM_CONTINUE(            0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "lk_snd.bin",  0x00000, 0x80000, 0x474d45a4 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_quizhq = new RomLoadPtr(){ public void handler(){ 	/* Quiz HQ */
		ROM_REGION( 0xc0000, REGION_CPU1 );    /* 256k for 68000 code */
		ROM_LOAD_EVEN( "c53-05.bin",  0x00000, 0x20000, 0xc798fc20 );/* Prog1 */
		ROM_LOAD_ODD ( "c53-01.bin",  0x00000, 0x20000, 0xbf44c93e );/* Prog2 */
		ROM_LOAD_EVEN( "c53-52.bin",  0x80000, 0x20000, 0x12e14aca );/* char defs, read by cpu */
		ROM_LOAD_ODD ( "c53-51.bin",  0x80000, 0X20000, 0xb3ef66f3 );/* char defs, read by cpu */
	
		ROM_REGION( 0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE );     /* UNUSED! */
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD_GFX_EVEN( "c53-03.bin",  0x00000, 0x20000, 0x47596e70 ) /* Object */
		ROM_LOAD_GFX_ODD ( "c53-07.bin",  0x00000, 0x20000, 0x4f9fa82f ) /* Object */
		ROM_LOAD_GFX_EVEN( "c53-02.bin",  0x40000, 0x20000, 0xd704c6f4 ) /* Object */
		ROM_LOAD_GFX_ODD ( "c53-06.bin",  0x40000, 0x20000, 0xf77f63fc ) /* Object */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "c53-08.bin",    0x00000, 0x04000, 0x25187e81 );/* AUD Prog */
		ROM_CONTINUE(          0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x080000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "c53-04.bin",  0x000000, 0x020000, 0x99890ad4 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ssi = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );    /* 512k for 68000 code */
		ROM_LOAD_EVEN( "ssi_15-1.rom", 0x00000, 0x40000, 0xce9308a6 );	ROM_LOAD_ODD ( "ssi_16-1.rom", 0x00000, 0x40000, 0x470a483a );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	/* empty! */
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "ssi_m01.rom",  0x00000, 0x100000, 0xa1b4f486 );
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "ssi_09.rom",   0x00000, 0x04000, 0x88d7f65c );	ROM_CONTINUE(             0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x20000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "ssi_m02.rom",  0x00000, 0x20000, 0x3cb0b907 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_majest12 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );    /* 512k for 68000 code */
		ROM_LOAD_EVEN( "c64-07.bin", 0x00000, 0x20000, 0xf29ed5c9 );	ROM_LOAD_EVEN( "c64-06.bin", 0x40000, 0x20000, 0x18dc71ac );	ROM_LOAD_ODD ( "c64-08.bin", 0x00000, 0x20000, 0xddfd33d5 );	ROM_LOAD_ODD ( "c64-05.bin", 0x40000, 0x20000, 0xb61866c0 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	/* empty! */
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "ssi_m01.rom",  0x00000, 0x100000, 0xa1b4f486 );
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "ssi_09.rom",   0x00000, 0x04000, 0x88d7f65c );	ROM_CONTINUE(             0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x20000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "ssi_m02.rom",  0x00000, 0x20000, 0x3cb0b907 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_gunfront = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0xc0000, REGION_CPU1 );    /* 768k for 68000 code */
		ROM_LOAD_EVEN( "c71-09.rom",  0x00000, 0x20000, 0x10a544a2 );	ROM_LOAD_ODD ( "c71-08.rom",  0x00000, 0x20000, 0xc17dc0a0 );	ROM_LOAD_EVEN( "c71-10.rom",  0x40000, 0x20000, 0xf39c0a06 );	ROM_LOAD_ODD ( "c71-14.rom",  0x40000, 0x20000, 0x312da036 );	ROM_LOAD_EVEN( "c71-16.rom",  0x80000, 0x20000, 0x1bbcc2d4 );	ROM_LOAD_ODD ( "c71-15.rom",  0x80000, 0x20000, 0xdf3e00bb );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c71-02.rom", 0x000000, 0x100000, 0x2a600c92 );    /* characters */
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c71-03.rom", 0x000000, 0x100000, 0x9133c605 );    /* sprites */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "c71-12.rom", 0x00000, 0x04000, 0x0038c7f8 );	ROM_CONTINUE(           0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "c71-01.rom", 0x000000, 0x100000, 0x0e73105a );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_gunfronj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0xc0000, REGION_CPU1 );    /* 768k for 68000 code */
		ROM_LOAD_EVEN( "c71-09.rom",  0x00000, 0x20000, 0x10a544a2 );	ROM_LOAD_ODD ( "c71-08.rom",  0x00000, 0x20000, 0xc17dc0a0 );	ROM_LOAD_EVEN( "c71-10.rom",  0x40000, 0x20000, 0xf39c0a06 );	ROM_LOAD_ODD ( "c71-11.3",    0x40000, 0x20000, 0xdf23c11a );	ROM_LOAD_EVEN( "c71-16.rom",  0x80000, 0x20000, 0x1bbcc2d4 );/* C71-05 */
		ROM_LOAD_ODD ( "c71-15.rom",  0x80000, 0x20000, 0xdf3e00bb );/* C71-04 */
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c71-02.rom", 0x000000, 0x100000, 0x2a600c92 );    /* characters */
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c71-03.rom", 0x000000, 0x100000, 0x9133c605 );    /* sprites */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "c71-12.rom", 0x00000, 0x04000, 0x0038c7f8 );	ROM_CONTINUE(           0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "c71-01.rom", 0x000000, 0x100000, 0x0e73105a );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_growl = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 1024k for 68000 code */
		ROM_LOAD_EVEN( "c74-10",        0x00000, 0x40000, 0xca81a20b );	ROM_LOAD_ODD ( "c74-08",        0x00000, 0x40000, 0xaa35dd9e );	ROM_LOAD_EVEN( "c74-11",        0x80000, 0x40000, 0xee3bd6d5 );	ROM_LOAD_ODD ( "c74-14",        0x80000, 0x40000, 0xb6c24ec7 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c74-01",       0x000000, 0x100000, 0x3434ce80 );/* characters */
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c74-03",       0x000000, 0x100000, 0x1a0d8951 );/* sprites */
		ROM_LOAD( "c74-02",       0x100000, 0x100000, 0x15a21506 );/* sprites */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "c74-12",       0x00000, 0x04000, 0xbb6ed668 );	ROM_CONTINUE(             0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "c74-04",       0x000000, 0x100000, 0x2d97edf2 );
		ROM_REGION( 0x080000, REGION_SOUND2 );/* Delta-T samples */
		ROM_LOAD( "c74-05",       0x000000, 0x080000, 0xe29c0828 );ROM_END(); }}; 
	
	static RomLoadPtr rom_growlu = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 1024k for 68000 code */
		ROM_LOAD_EVEN( "c74-10",        0x00000, 0x40000, 0xca81a20b );	ROM_LOAD_ODD ( "c74-08",        0x00000, 0x40000, 0xaa35dd9e );	ROM_LOAD_EVEN( "c74-11",        0x80000, 0x40000, 0xee3bd6d5 );	ROM_LOAD_ODD ( "c74-13",        0x80000, 0x40000, 0xc1c57e51 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c74-01",       0x000000, 0x100000, 0x3434ce80 );/* characters */
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c74-03",       0x000000, 0x100000, 0x1a0d8951 );/* sprites */
		ROM_LOAD( "c74-02",       0x100000, 0x100000, 0x15a21506 );/* sprites */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "c74-12",       0x00000, 0x04000, 0xbb6ed668 );	ROM_CONTINUE(             0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "c74-04",       0x000000, 0x100000, 0x2d97edf2 );
		ROM_REGION( 0x080000, REGION_SOUND2 );/* Delta-T samples */
		ROM_LOAD( "c74-05",       0x000000, 0x080000, 0xe29c0828 );ROM_END(); }}; 
	
	static RomLoadPtr rom_runark = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 1024k for 68000 code */
		ROM_LOAD_EVEN( "c74-10",        0x00000, 0x40000, 0xca81a20b );	ROM_LOAD_ODD ( "c74-08",        0x00000, 0x40000, 0xaa35dd9e );	ROM_LOAD_EVEN( "c74-11",        0x80000, 0x40000, 0xee3bd6d5 );	ROM_LOAD_ODD ( "c74-09.14",     0x80000, 0x40000, 0x58cc2feb );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c74-01",       0x000000, 0x100000, 0x3434ce80 );/* characters */
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c74-03",       0x000000, 0x100000, 0x1a0d8951 );/* sprites */
		ROM_LOAD( "c74-02",       0x100000, 0x100000, 0x15a21506 );/* sprites */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "c74-12",       0x00000, 0x04000, 0xbb6ed668 );	ROM_CONTINUE(             0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "c74-04",       0x000000, 0x100000, 0x2d97edf2 );
		ROM_REGION( 0x080000, REGION_SOUND2 );/* Delta-T samples */
		ROM_LOAD( "c74-05",       0x000000, 0x080000, 0xe29c0828 );ROM_END(); }}; 
	
	static RomLoadPtr rom_mjnquest = new RomLoadPtr(){ public void handler(){ 	/* Mahjong Quest */
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 256k for 68000 code */
		ROM_LOAD_EVEN( "c77-09",  0x000000, 0x020000, 0x0a005d01 );/* Prog1 */
		ROM_LOAD_ODD ( "c77-08",  0x000000, 0x020000, 0x4244f775 );/* Prog2 */
		ROM_LOAD_WIDE_SWAP( "c77-04",  0x080000, 0x080000, 0xc2e7e038 );/* data */
	
		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );  /* temporary space for graphics (disposed after conversion) */
		ROM_LOAD( "c77-01", 0x000000, 0x100000, 0x5ba51205 );     /* Screen 0 */
		ROM_LOAD( "c77-02", 0x100000, 0x100000, 0x6a6f3040 );     /* Screen 1 */
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );  /* temporary space for graphics (disposed after conversion) */
		ROM_LOAD( "c77-05", 0x000000, 0x080000, 0xc5a54678 );     /* Object */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "c77-10",    0x00000, 0x04000, 0xf16b2c1e );/* AUD Prog */
		ROM_CONTINUE(          0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x080000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "c77-03",  0x000000, 0x080000, 0x312f17b1 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_mjnquesb = new RomLoadPtr(){ public void handler(){ 	/* Mahjong Quest (No Nudity) */
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 256k for 68000 code */
		ROM_LOAD_EVEN( "c77-09a", 0x000000, 0x020000, 0xfc17f1c2 );/* Prog1 */
		ROM_LOAD_ODD ( "c77-08",  0x000000, 0x020000, 0x4244f775 );/* Prog2 */
		ROM_LOAD_WIDE_SWAP( "c77-04",  0x080000, 0x080000, 0xc2e7e038 );/* data */
	
		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );  /* temporary space for graphics (disposed after conversion) */
		ROM_LOAD( "c77-01", 0x000000, 0x100000, 0x5ba51205 );     /* Screen 0 */
		ROM_LOAD( "c77-02", 0x100000, 0x100000, 0x6a6f3040 );     /* Screen 1 */
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );  /* temporary space for graphics (disposed after conversion) */
		ROM_LOAD( "c77-05", 0x000000, 0x080000, 0xc5a54678 );     /* Object */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "c77-10",    0x00000, 0x04000, 0xf16b2c1e );/* AUD Prog */
		ROM_CONTINUE(          0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x080000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "c77-03",  0x000000, 0x080000, 0x312f17b1 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_footchmp = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );    /* 512k for 68000 code */
		ROM_LOAD_EVEN( "c80-11",     0x00000, 0x20000, 0xf78630fb );	ROM_LOAD_ODD ( "c80-10",     0x00000, 0x20000, 0x32c109cb );	ROM_LOAD_EVEN( "c80-12",     0x40000, 0x20000, 0x80d46fef );	ROM_LOAD_ODD ( "c80-14",     0x40000, 0x20000, 0x40ac4828 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD_GFX_EVEN( "c80-04", 0x000000, 0x080000, 0x9a17fe8c ) /* characters */
		ROM_LOAD_GFX_ODD ( "c80-05", 0x000000, 0x080000, 0xacde7071 ) /* characters */
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c80-01", 0x000000, 0x100000, 0xf43782e6 );         /* sprites */
		ROM_LOAD( "c80-02", 0x100000, 0x100000, 0x060a8b61 );         /* sprites */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );    /* 64k for Z80 code */
		ROM_LOAD( "c80-15", 0x00000, 0x04000, 0x05aa7fd7 );	ROM_CONTINUE(       0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );    /* YM2610 samples */
		ROM_LOAD( "c80-03", 0x000000, 0x100000, 0x609938d5 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_hthero = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );    /* 512k for 68000 code */
		ROM_LOAD_EVEN( "c80-16",  0x00000, 0x20000, 0x4e795b52 );	ROM_LOAD_ODD ( "c80-17",  0x00000, 0x20000, 0x42c0a838 );	ROM_LOAD_EVEN( "c80-12",  0x40000, 0x20000, 0x80d46fef );	ROM_LOAD_ODD ( "c80-18",  0x40000, 0x20000, 0xaea22904 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD_GFX_EVEN( "c80-04", 0x000000, 0x080000, 0x9a17fe8c ) /* characters */
		ROM_LOAD_GFX_ODD ( "c80-05", 0x000000, 0x080000, 0xacde7071 ) /* characters */
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c80-01", 0x000000, 0x100000, 0xf43782e6 );         /* sprites */
		ROM_LOAD( "c80-02", 0x100000, 0x100000, 0x060a8b61 );         /* sprites */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "c80-15", 0x00000, 0x04000, 0x05aa7fd7 );	ROM_CONTINUE(       0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "c80-03", 0x000000, 0x100000, 0x609938d5 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_euroch92 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );    /* 512k for 68000 code */
		ROM_LOAD_EVEN( "ec92_25.rom", 0x00000, 0x20000, 0x98482202 );	ROM_LOAD_ODD ( "ec92_23.rom", 0x00000, 0x20000, 0xae5e75e9 );	ROM_LOAD_EVEN( "ec92_26.rom", 0x40000, 0x20000, 0xb986ccb2 );	ROM_LOAD_ODD ( "ec92_24.rom", 0x40000, 0x20000, 0xb31d94ac );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD_GFX_EVEN( "ec92_21.rom", 0x000000, 0x080000, 0x5759ed37 )
		ROM_LOAD_GFX_ODD ( "ec92_22.rom", 0x000000, 0x080000, 0xd9a0d38e )
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "ec92_19.rom", 0x000000, 0x100000, 0x219141a5 );	ROM_LOAD( "c80-02",      0x100000, 0x100000, 0x060a8b61 );// ec92_20.rom
	
		ROM_REGION( 0x1c000, REGION_CPU2 );    /* 64k for Z80 code */
		ROM_LOAD( "ec92_27.rom", 0x00000, 0x04000, 0x2db48e65 );	ROM_CONTINUE(            0x10000, 0x0c000 );
		ROM_REGION( 0x100000, REGION_SOUND1 );    /* YM2610 samples */
		ROM_LOAD( "c80-03", 0x000000, 0x100000, 0x609938d5 );// ec92_03.rom
	
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_koshien = new RomLoadPtr(){ public void handler(){ 	/* Ah Eikou no Koshien */
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 256k for 68000 code */
		ROM_LOAD_EVEN( "c81-11.bin", 0x000000, 0x020000, 0xb44ea8c9 );/* Prog1 */
		ROM_LOAD_ODD ( "c81-10.bin", 0x000000, 0x020000, 0x8f98c40a );/* Prog2 */
		ROM_LOAD_WIDE_SWAP( "c81-04.bin", 0x080000, 0x080000, 0x1592b460 );/* data */
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );   /* temporary space for graphics (disposed after conversion) */
		ROM_LOAD( "c81-03.bin", 0x000000, 0x100000, 0x29bbf492 );  /* Screen */
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );   /* temporary space for graphics (disposed after conversion) */
		ROM_LOAD( "c81-01.bin", 0x000000, 0x100000, 0x64b15d2a );  /* Object 0 */
		ROM_LOAD( "c81-02.bin", 0x100000, 0x100000, 0x962461e8 );  /* Object 1 */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "c81-12.bin",    0x00000, 0x04000, 0x6e8625b6 );/* AUD Prog */
		ROM_CONTINUE(          0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x080000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "c81-05.bin",  0x00000, 0x80000, 0x9c3d71be );
		ROM_REGION( 0x080000, REGION_SOUND2 );/* Delta-T samples */
		ROM_LOAD( "c81-06.bin",  0x00000, 0x80000, 0x927833b4 );ROM_END(); }}; 
	
	static RomLoadPtr rom_yuyugogo = new RomLoadPtr(){ public void handler(){ 	/* Yuuyu no QUIZ de GO!GO! */
		ROM_REGION( 0x40000, REGION_CPU1 );    /* 256k for 68000 code */
		ROM_LOAD_EVEN( "c83-10.bin",  0x00000,  0x20000, 0x4d185d03 );	ROM_LOAD_ODD ( "c83-09.bin",  0x00000,  0x20000, 0xf9892792 );
		ROM_REGION( 0x100000, REGION_USER1 );	/* extra ROM mapped at d00000 */
		ROM_LOAD( "c83-03.bin", 0x000000, 0x100000, 0xeed9acc2 );  /* data rom */
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c83-05.bin", 0x000000, 0x020000, 0xeca57fb1 );     /* screen */
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD_GFX_EVEN( "c83-01.bin", 0x000000, 0x100000, 0x8bf0d416 ) /* Object */
		ROM_LOAD_GFX_ODD ( "c83-02.bin", 0x000000, 0x100000, 0x20bb1c15 ) /* Object */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "c83-11.bin",    0x00000, 0x04000, 0x461e702a );/* AUD Prog */
		ROM_CONTINUE(          0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "c83-04.bin",  0x000000, 0x100000, 0x2600093a );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ninjak = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );    /* 512k for 68000 code */
		ROM_LOAD_EVEN( "nk_0h.rom",   0x00000, 0x20000, 0xba7e6e74 );	ROM_LOAD_ODD ( "nk_0l.rom",   0x00000, 0x20000, 0x0ac2cba2 );	ROM_LOAD_EVEN( "nk_1lh.rom",  0x40000, 0x20000, 0x3eccfd0a );	ROM_LOAD_ODD ( "nk_1ll.rom",  0x40000, 0x20000, 0xd126ded1 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "nk_scrn.rom", 0x000000, 0x080000, 0x4cc7b9df );   /* characters */
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "nk_obj-0.rom", 0x000000, 0x100000, 0xa711977c );  /* sprites */
		ROM_LOAD( "nk_obj-1.rom", 0x100000, 0x100000, 0xa6ad0f3d );  /* sprites */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "nk_snd.rom", 0x00000, 0x04000, 0xf2a52a51 );	ROM_CONTINUE(             0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "nk_sch-a.rom", 0x00000, 0x80000, 0x5afb747e );
		ROM_REGION( 0x80000, REGION_SOUND2 );/* Delta-T samples */
		ROM_LOAD( "nk_sch-b.rom", 0x00000, 0x80000, 0x3c1b0ed0 );ROM_END(); }}; 
	
	static RomLoadPtr rom_ninjakj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );    /* 512k for 68000 code */
		ROM_LOAD_EVEN( "nk_0h.rom",   0x00000, 0x20000, 0xba7e6e74 );	ROM_LOAD_ODD ( "c85-11l",     0x00000, 0x20000, 0xe4ccaa8e );	ROM_LOAD_EVEN( "nk_1lh.rom",  0x40000, 0x20000, 0x3eccfd0a );	ROM_LOAD_ODD ( "nk_1ll.rom",  0x40000, 0x20000, 0xd126ded1 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "nk_scrn.rom", 0x000000, 0x080000, 0x4cc7b9df );   /* characters */
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "nk_obj-0.rom", 0x000000, 0x100000, 0xa711977c );  /* sprites */
		ROM_LOAD( "nk_obj-1.rom", 0x100000, 0x100000, 0xa6ad0f3d );  /* sprites */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "nk_snd.rom", 0x00000, 0x04000, 0xf2a52a51 );	ROM_CONTINUE(             0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "nk_sch-a.rom", 0x00000, 0x80000, 0x5afb747e );
		ROM_REGION( 0x80000, REGION_SOUND2 );/* Delta-T samples */
		ROM_LOAD( "nk_sch-b.rom", 0x00000, 0x80000, 0x3c1b0ed0 );ROM_END(); }}; 
	
	static RomLoadPtr rom_solfigtr = new RomLoadPtr(){ public void handler(){ 	/* Solitary Fighter */
		ROM_REGION( 0x80000, REGION_CPU1 );    /* 512k for 68000 code */
		ROM_LOAD_EVEN( "c91-05",  0x00000, 0x40000, 0xc1260e7c );	ROM_LOAD_ODD ( "c91-09",  0x00000, 0x40000, 0xd82b5266 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );   /* temporary space for graphics (disposed after conversion) */
		ROM_LOAD( "c91-03", 0x000000, 0x100000, 0x8965da12 );      /* Characters */
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );   /* temporary space for graphics (disposed after conversion) */
		ROM_LOAD( "c91-01", 0x000000, 0x100000, 0x0f3f4e00 );      /* Object 0 */
		ROM_LOAD( "c91-02", 0x100000, 0x100000, 0xe14ab98e );      /* Object 1 */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "c91-07",    0x00000, 0x04000, 0xe471a05a );/* AUD Prog */
		ROM_CONTINUE(          0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "c91-04",  0x00000, 0x80000, 0x390b1065 );/* Channel A */
	
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_qzquest = new RomLoadPtr(){ public void handler(){ 	/* Quiz Quest */
		ROM_REGION( 0x180000, REGION_CPU1 );    /* 256k for 68000 code */
		ROM_LOAD_EVEN( "c92-06", 0x000000, 0x020000, 0x424be722 );/* Prog1 */
		ROM_LOAD_ODD ( "c92-05", 0x000000, 0x020000, 0xda470f93 );/* Prog2 */
		ROM_LOAD_WIDE_SWAP( "c92-03", 0x100000, 0x080000, 0x1d697606 );/* data */
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );  /* temporary space for graphics (disposed after conversion) */
		ROM_LOAD( "c92-02", 0x000000, 0x100000, 0x2daccecf );     /* Screen */
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );  /* temporary space for graphics (disposed after conversion) */
		ROM_LOAD( "c92-01", 0x000000, 0x100000, 0x9976a285 );     /* Object */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "c92-07",    0x00000, 0x04000, 0x3e313db9 );/* AUD Prog */
		ROM_CONTINUE(          0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x080000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "c92-04",  0x000000, 0x080000, 0xe421bb43 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_pulirula = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0xc0000, REGION_CPU1 );    /* 768k for 68000 code */
		ROM_LOAD_EVEN( "c98-12.rom", 0x00000, 0x40000, 0x816d6cde );	ROM_LOAD_ODD ( "c98-16.rom", 0x00000, 0x40000, 0x59df5c77 );	ROM_LOAD_EVEN( "c98-06.rom", 0x80000, 0x20000, 0x64a71b45 );	ROM_LOAD_ODD ( "c98-07.rom", 0x80000, 0x20000, 0x90195bc0 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c98-04.rom", 0x000000, 0x100000, 0x0e1fe3b2 );  /* background/foreground */
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c98-02.rom", 0x000000, 0x100000, 0x4a2ad2b3 );  /* sprites */
		ROM_LOAD( "c98-03.rom", 0x100000, 0x100000, 0x589a678f );  /* sprites */
	
		ROM_REGION( 0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE );   /* temporary space for graphics (disposed after conversion) */
		ROM_LOAD( "c98-05.rom", 0x000000, 0x080000, 0x9ddd9c39 );  /* pivot graphics */
	
		ROM_REGION( 0x2c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "c98-14.rom", 0x00000, 0x04000, 0xa858e17c );	ROM_CONTINUE(           0x10000, 0x1c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "c98-01.rom", 0x000000, 0x100000, 0x197f66f5 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_pulirulj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0xc0000, REGION_CPU1 );    /* 768k for 68000 code */
		ROM_LOAD_EVEN( "c98-12.rom", 0x00000, 0x40000, 0x816d6cde );	ROM_LOAD_ODD ( "c98-13",     0x00000, 0x40000, 0xb7d13d5b );	ROM_LOAD_EVEN( "c98-06.rom", 0x80000, 0x20000, 0x64a71b45 );	ROM_LOAD_ODD ( "c98-07.rom", 0x80000, 0x20000, 0x90195bc0 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c98-04.rom", 0x000000, 0x100000, 0x0e1fe3b2 );  /* background/foreground */
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "c98-02.rom", 0x000000, 0x100000, 0x4a2ad2b3 );  /* sprites */
		ROM_LOAD( "c98-03.rom", 0x100000, 0x100000, 0x589a678f );  /* sprites */
	
		ROM_REGION( 0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE );   /* temporary space for graphics (disposed after conversion) */
		ROM_LOAD( "c98-05.rom", 0x000000, 0x080000, 0x9ddd9c39 );  /* pivot graphics */
	
		ROM_REGION( 0x2c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "c98-14.rom", 0x00000, 0x04000, 0xa858e17c );	ROM_CONTINUE(           0x10000, 0x1c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "c98-01.rom", 0x000000, 0x100000, 0x197f66f5 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_metalb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0xc0000, REGION_CPU1 );    /* 768k for 68000 code */
		ROM_LOAD_EVEN( "d16-16.8",   0x00000, 0x40000, 0x3150be61 );	ROM_LOAD_ODD ( "d16-18.7",   0x00000, 0x40000, 0x5216d092 );	ROM_LOAD_EVEN( "d12-07.9",   0x80000, 0x20000, 0xe07f5136 );	ROM_LOAD_ODD ( "d12-06.6",   0x80000, 0x20000, 0x131df731 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD_GFX_EVEN( "d12-03.14", 0x000000, 0x080000, 0x46b498c0 ) /* characters */
		ROM_LOAD_GFX_ODD ( "d12-04.13", 0x000000, 0x080000, 0xab66d141 ) /* characters */
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "d12-01.20", 0x000000, 0x100000, 0xb81523b9 );         /* sprites */
	
		ROM_REGION( 0x2c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "d12-13.5", 0x00000, 0x04000, 0xbcca2649 );	ROM_CONTINUE(         0x10000, 0x1c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "d12-02.10", 0x000000, 0x100000, 0x79263e74 );
		ROM_REGION( 0x080000, REGION_SOUND2 );  /* Delta-T samples */
		ROM_LOAD( "d12-05.16", 0x000000, 0x080000, 0x7fd036c5 );ROM_END(); }}; 
	
	static RomLoadPtr rom_metalbj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0xc0000, REGION_CPU1 );    /* 768k for 68000 code */
		ROM_LOAD_EVEN( "d12-12.8",   0x00000, 0x40000, 0x556f82b2 );	ROM_LOAD_ODD ( "d12-11.7",   0x00000, 0x40000, 0xaf9ee28d );	ROM_LOAD_EVEN( "d12-07.9",   0x80000, 0x20000, 0xe07f5136 );	ROM_LOAD_ODD ( "d12-06.6",   0x80000, 0x20000, 0x131df731 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD_GFX_EVEN( "d12-03.14", 0x000000, 0x080000, 0x46b498c0 ) /* characters */
		ROM_LOAD_GFX_ODD ( "d12-04.13", 0x000000, 0x080000, 0xab66d141 ) /* characters */
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "d12-01.20", 0x000000, 0x100000, 0xb81523b9 );         /* sprites */
	
		ROM_REGION( 0x2c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "d12-13.5", 0x00000, 0x04000, 0xbcca2649 );	ROM_CONTINUE(         0x10000, 0x1c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "d12-02.10", 0x000000, 0x100000, 0x79263e74 );
		ROM_REGION( 0x080000, REGION_SOUND2 );  /* Delta-T samples */
		ROM_LOAD( "d12-05.16", 0x000000, 0x080000, 0x7fd036c5 );ROM_END(); }}; 
	
	static RomLoadPtr rom_qzchikyu = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x180000, REGION_CPU1 );    /* 256k for 68000 code */
		ROM_LOAD_EVEN( "d19-06.bin", 0x000000, 0x020000, 0xde8c8e55 );	ROM_LOAD_ODD ( "d19-05.bin", 0x000000, 0x020000, 0xc6d099d0 );	ROM_LOAD_WIDE_SWAP( "d19-03.bin", 0x100000, 0x080000, 0x5c1b92c0 );    /* data rom */
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "d19-02.bin", 0x000000, 0x100000, 0xf2dce2f2 );    /* screen */
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "d19-01.bin", 0x000000, 0x100000, 0x6c4342d0 ); /* sprites */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "d19-07.bin", 0x00000, 0x04000, 0xa8935f84 );	ROM_CONTINUE(           0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "d19-04.bin", 0x000000, 0x080000, 0xd3c44905 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_yesnoj = new RomLoadPtr(){ public void handler(){ 	/* Yes/No Sinri Tokimeki Chart */
		ROM_REGION( 0x80000, REGION_CPU1 );    /* 512k for 68000 code */
		ROM_LOAD_EVEN( "d20-05-2.2",  0x00000, 0x40000, 0x68adb929 );	ROM_LOAD_ODD ( "d20-04-2.4",  0x00000, 0x40000, 0xa84762f8 );
		ROM_REGION( 0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "d20-01.11", 0x00000, 0x80000, 0x9d8a4d57 );         /* Screen */
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD_GFX_EVEN( "d20-02.12",  0x00000, 0x80000, 0xe71a8e40 ) /* Object */
		ROM_LOAD_GFX_ODD ( "d20-03.13",  0x00000, 0x80000, 0x6a51a1b4 ) /* Object */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "d20-06.5",  0x00000, 0x04000, 0x3eb537dc );/* AUD Prog */
		ROM_CONTINUE(          0x10000, 0x0c000 );/* banked stuff */
	
		/* no ADPCM samples */
	
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_deadconx = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 1024k for 68000 code */
		ROM_LOAD_EVEN( "d28_06.3",  0x00000, 0x40000, 0x5b4bff51 );	ROM_LOAD_ODD ( "d28_12.5",  0x00000, 0x40000, 0x9b74e631 );	ROM_LOAD_EVEN( "d28_09.2",  0x80000, 0x40000, 0x143a0cc1 );	ROM_LOAD_ODD ( "d28_08.4",  0x80000, 0x40000, 0x4c872bd9 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD_GFX_EVEN( "d28_04.16", 0x000000, 0x080000, 0xdcabc26b ) /* characters */
		ROM_LOAD_GFX_ODD ( "d28_05.17", 0x000000, 0x080000, 0x862f9665 ) /* characters */
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "d28_01.8", 0x000000, 0x100000, 0x181d7b69 );         /* sprites */
		ROM_LOAD( "d28_02.9", 0x100000, 0x100000, 0xd301771c );         /* sprites */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "d28_10.6", 0x00000, 0x04000, 0x40805d74 );	ROM_CONTINUE(         0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "d28_03.10", 0x000000, 0x100000, 0xa1804b52 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_deadconj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 1024k for 68000 code */
		ROM_LOAD_EVEN( "d28_06.3",  0x00000, 0x40000, 0x5b4bff51 );	ROM_LOAD_ODD ( "d28_07.5",  0x00000, 0x40000, 0x3fb8954c );	ROM_LOAD_EVEN( "d28_09.2",  0x80000, 0x40000, 0x143a0cc1 );	ROM_LOAD_ODD ( "d28_08.4",  0x80000, 0x40000, 0x4c872bd9 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD_GFX_EVEN( "d28_04.16", 0x000000, 0x080000, 0xdcabc26b ) /* characters */
		ROM_LOAD_GFX_ODD ( "d28_05.17", 0x000000, 0x080000, 0x862f9665 ) /* characters */
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "d28_01.8", 0x000000, 0x100000, 0x181d7b69 );         /* sprites */
		ROM_LOAD( "d28_02.9", 0x100000, 0x100000, 0xd301771c );         /* sprites */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "d28_10.6", 0x00000, 0x04000, 0x40805d74 );	ROM_CONTINUE(         0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "d28_03.10", 0x000000, 0x100000, 0xa1804b52 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_dinorex = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x300000, REGION_CPU1 );    /* 1Mb for 68000 code */
		ROM_LOAD_EVEN( "drex_14.rom",  0x000000, 0x080000, 0xe6aafdac );	ROM_LOAD_ODD ( "drex_16.rom",  0x000000, 0x080000, 0xcedc8537 );	ROM_LOAD_WIDE_SWAP( "drex_04m.rom", 0x100000, 0x100000, 0x3800506d ); /* data rom */
		ROM_LOAD_WIDE_SWAP( "drex_05m.rom", 0x200000, 0x100000, 0xe2ec3b5d ); /* data rom */
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "drex_06m.rom", 0x000000, 0x100000, 0x52f62835 );  /* characters */
	
		ROM_REGION( 0x600000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "drex_01m.rom", 0x000000, 0x200000, 0xd10e9c7d );  /* sprites */
		ROM_LOAD( "drex_02m.rom", 0x200000, 0x200000, 0x6c304403 );  /* sprites */
		ROM_LOAD( "drex_03m.rom", 0x400000, 0x200000, 0xfc9cdab4 );  /* sprites */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "drex_12.rom", 0x00000, 0x04000, 0x8292c7c1 );	ROM_CONTINUE(             0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "drex_07m.rom", 0x000000, 0x100000, 0x28262816 );
		ROM_REGION( 0x080000, REGION_SOUND2 );  /* Delta-T samples */
		ROM_LOAD( "drex_08m.rom", 0x000000, 0x080000, 0x377b8b7b );ROM_END(); }}; 
	
	static RomLoadPtr rom_dinorexj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x300000, REGION_CPU1 );    /* 1Mb for 68000 code */
		ROM_LOAD_EVEN( "drex_14.rom",  0x000000, 0x080000, 0xe6aafdac );	ROM_LOAD_ODD ( "d39-13.rom",   0x000000, 0x080000, 0xae496b2f );	ROM_LOAD_WIDE_SWAP( "drex_04m.rom", 0x100000, 0x100000, 0x3800506d ); /* data rom */
		ROM_LOAD_WIDE_SWAP( "drex_05m.rom", 0x200000, 0x100000, 0xe2ec3b5d ); /* data rom */
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "drex_06m.rom", 0x000000, 0x100000, 0x52f62835 );  /* characters */
	
		ROM_REGION( 0x600000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "drex_01m.rom", 0x000000, 0x200000, 0xd10e9c7d );  /* sprites */
		ROM_LOAD( "drex_02m.rom", 0x200000, 0x200000, 0x6c304403 );  /* sprites */
		ROM_LOAD( "drex_03m.rom", 0x400000, 0x200000, 0xfc9cdab4 );  /* sprites */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "drex_12.rom", 0x00000, 0x04000, 0x8292c7c1 );	ROM_CONTINUE(             0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "drex_07m.rom", 0x000000, 0x100000, 0x28262816 );
		ROM_REGION( 0x080000, REGION_SOUND2 );  /* Delta-T samples */
		ROM_LOAD( "drex_08m.rom", 0x000000, 0x080000, 0x377b8b7b );ROM_END(); }}; 
	
	static RomLoadPtr rom_dinorexu = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x300000, REGION_CPU1 );    /* 1Mb for 68000 code */
		ROM_LOAD_EVEN( "drex_14.rom",  0x000000, 0x080000, 0xe6aafdac );	ROM_LOAD_ODD ( "drex_16u.rom", 0x000000, 0x080000, 0xfe96723b );	ROM_LOAD_WIDE_SWAP( "drex_04m.rom", 0x100000, 0x100000, 0x3800506d ); /* data rom */
		ROM_LOAD_WIDE_SWAP( "drex_05m.rom", 0x200000, 0x100000, 0xe2ec3b5d ); /* data rom */
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "drex_06m.rom", 0x000000, 0x100000, 0x52f62835 );  /* characters */
	
		ROM_REGION( 0x600000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "drex_01m.rom", 0x000000, 0x200000, 0xd10e9c7d );  /* sprites */
		ROM_LOAD( "drex_02m.rom", 0x200000, 0x200000, 0x6c304403 );  /* sprites */
		ROM_LOAD( "drex_03m.rom", 0x400000, 0x200000, 0xfc9cdab4 );  /* sprites */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "drex_12.rom", 0x00000, 0x04000, 0x8292c7c1 );	ROM_CONTINUE(             0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "drex_07m.rom", 0x000000, 0x100000, 0x28262816 );
		ROM_REGION( 0x080000, REGION_SOUND2 );  /* Delta-T samples */
		ROM_LOAD( "drex_08m.rom", 0x000000, 0x080000, 0x377b8b7b );ROM_END(); }}; 
	
	static RomLoadPtr rom_qjinsei = new RomLoadPtr(){ public void handler(){ 	/* Quiz Jinsei Gekijoh */
		ROM_REGION( 0x200000, REGION_CPU1 );    /* 512k for 68000 code */
		ROM_LOAD_EVEN( "d48-09",  0x000000, 0x040000, 0xa573b68d );/* Prog1 */
		ROM_LOAD_ODD ( "d48-10",  0x000000, 0x040000, 0x37143a5b );/* Prog2 */
		ROM_LOAD_WIDE_SWAP( "d48-03",  0x100000, 0x100000, 0xfb5ea8dc );/* data */
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "d48-04", 0x000000, 0x100000, 0x61e4b078 );         /* Screen */
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD_GFX_EVEN( "d48-02", 0x000000, 0x100000, 0xa7b68e63 ) /* Object */
		ROM_LOAD_GFX_ODD ( "d48-01", 0x000000, 0x100000, 0x72a94b73 ) /* Object */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "d48-11",    0x00000, 0x04000, 0x656c5b54 );/* AUD Prog */
		ROM_CONTINUE(          0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x080000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "d48-05",  0x000000, 0x080000, 0x3fefd058 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_qcrayon = new RomLoadPtr(){ public void handler(){ 	/* Quiz Crayon */
		ROM_REGION( 0x80000, REGION_CPU1 );    /* 512k for 68000 code */
		ROM_LOAD_EVEN( "d55-13",  0x00000, 0x40000, 0x16afbfc7 );	ROM_LOAD_ODD ( "d55-14",  0x00000, 0x40000, 0x2fb3057f );
		ROM_REGION( 0x100000, REGION_USER1 );	/* extra ROM mapped 0x300000 */
		ROM_LOAD_WIDE_SWAP( "d55-03", 0x000000, 0x100000, 0x4d161e76 );  /* data rom */
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "d55-02", 0x000000, 0x100000, 0xf3db2f1c );         /* Screen */
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD_GFX_EVEN( "d55-05", 0x000000, 0x100000, 0xf0e59902 ) /* Object */
		ROM_LOAD_GFX_ODD ( "d55-04", 0x000000, 0x100000, 0x412975ce ) /* Object */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "d55-15",    0x00000, 0x04000, 0xba782eff );/* AUD Prog */
		ROM_CONTINUE(          0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "d55-01",  0x000000, 0x100000, 0xa8309af4 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_qcrayon2 = new RomLoadPtr(){ public void handler(){ 	/* Quiz Crayon 2 */
		ROM_REGION( 0x80000, REGION_CPU1 );    /* 512k for 68000 code */
		ROM_LOAD_EVEN( "d63-12",  0x00000, 0x40000, 0x0f445a38 );	ROM_LOAD_ODD ( "d63-13",  0x00000, 0x40000, 0x74455752 );
		ROM_REGION( 0x080000, REGION_USER1 );	/* extra ROM mapped at 600000 */
		ROM_LOAD_WIDE_SWAP( "d63-01", 0x000000, 0x080000, 0x872e38b4 );  /* data rom */
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );   /* temporary space for graphics (disposed after conversion) */
		ROM_LOAD( "d63-03", 0x000000, 0x100000, 0xd24843af );      /* Screen */
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );   /* temporary space for graphics (disposed after conversion) */
		ROM_LOAD( "d63-06", 0x000000, 0x200000, 0x58b1e4a8 );      /* Object */
	
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "d63-11",    0x00000, 0x04000, 0x2c7ac9e5 );/* AUD Prog */
		ROM_CONTINUE(          0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "d63-02",  0x000000, 0x100000, 0x162ae165 );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_driftout = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 1024k for 68000 code */
		ROM_LOAD_EVEN( "do_46.rom",  0x00000, 0x80000, 0xf960363e );	ROM_LOAD_ODD ( "do_45.rom",  0x00000, 0x80000, 0xe3fe66b9 );
		ROM_REGION( 0x080000, REGION_GFX1 | REGIONFLAG_DISPOSE );     /* UNUSED! */
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );     /* temporary space for sprites (disposed after conversion) */
		ROM_LOAD( "do_obj.rom", 0x000000, 0x080000, 0x5491f1c4 );
		ROM_REGION( 0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE );     /* temporary space for pivot chars (disposed after conversion) */
		ROM_LOAD( "do_piv.rom",  0x000000, 0x080000, 0xc4f012f7 );
		ROM_REGION( 0x1c000, REGION_CPU2 );     /* sound cpu */
		ROM_LOAD( "do_50.rom",   0x00000, 0x04000, 0xffe10124 );	ROM_CONTINUE(            0x10000, 0x0c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "do_snd.rom",  0x00000, 0x80000, 0xf2deb82b );
		/* no Delta-T samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_driveout = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );    /* 1024k for 68000 code */
		ROM_LOAD_EVEN( "driveout.003",  0x00000, 0x80000, 0xdc431e4e );	ROM_LOAD_ODD ( "driveout.002",  0x00000, 0x80000, 0x6f9063f4 );
		ROM_REGION( 0x080000, REGION_GFX1 | REGIONFLAG_DISPOSE );     /* UNUSED! */
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );     /* temporary space for sprites (disposed after conversion) */
		ROM_LOAD_GFX_EVEN( "driveout.084", 0x000000, 0x040000, 0x530ac420 )
		ROM_LOAD_GFX_ODD ( "driveout.081", 0x000000, 0x040000, 0x0e9a3e9e )
	
		ROM_REGION( 0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE );     /* temporary space for pivot chars (disposed after conversion) */
		ROM_LOAD( "do_piv.rom",  0x000000, 0x080000, 0xc4f012f7 );
		ROM_REGION( 0x2c000, REGION_CPU2 );     /* sound cpu, why is prg longer than Driftout? */
		ROM_LOAD( "driveout.029",   0x00000, 0x04000, 0x0aba2026 );	ROM_CONTINUE(            0x10000, 0x1c000 );/* banked stuff */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* ADPCM samples? */
		ROM_LOAD( "driveout.028",  0x00000, 0x80000, 0xcbde0b66 );
		ROM_REGION( 0x10000, REGION_USER1 );/* Z80 code. Sample player? */
		ROM_LOAD( "driveout.020",  0x0000, 0x8000, 0x99aaeb2e );ROM_END(); }}; 
	
	
	
	public static InitDriverPtr init_finalb = new InitDriverPtr() { public void handler() 
	{
		int i;
		unsigned char data;
		unsigned int offset;
		UINT8 *gfx = memory_region(REGION_GFX2);
	
		offset = 0x100000;
		for (i = 0x180000; i<0x200000; i++)
		{
			int d1,d2,d3,d4;
	
			/* convert from 2bits into 4bits format */
			data = gfx[i];
			d1 = (data>>0) & 3;
			d2 = (data>>2) & 3;
			d3 = (data>>4) & 3;
			d4 = (data>>6) & 3;
	
			gfx[offset] = (d3<<2) | (d4<<6);
			offset++;
	
			gfx[offset] = (d1<<2) | (d2<<6);
			offset++;
		}
	} };
	
	public static InitDriverPtr init_mjnquest = new InitDriverPtr() { public void handler() 
	{
		int i;
		UINT8 *gfx = memory_region(REGION_GFX2);
	
		/* the bytes in each longword are in reversed order, put them in the
		   order used by the other games. */
		for (i = 0;i < memory_region_length(REGION_GFX2);i += 2)
		{
			int t;
	
			t = gfx[i];
			gfx[i] = (gfx[i+1] >> 4) | (gfx[i+1] << 4);
			gfx[i+1] = (t >> 4) | (t << 4);
		}
	} };
	
	
	
	public static GameDriver driver_finalb	   = new GameDriver("1988"	,"finalb"	,"taito_f2.java"	,rom_finalb,null	,machine_driver_finalb	,input_ports_finalb	,init_finalb	,ROT0	,	"Taito Corporation Japan", "Final Blow (World)" )
	public static GameDriver driver_finalbj	   = new GameDriver("1988"	,"finalbj"	,"taito_f2.java"	,rom_finalbj,driver_finalb	,machine_driver_finalb	,input_ports_finalbj	,init_finalb	,ROT0	,	"Taito Corporation", "Final Blow (Japan)" )
	public static GameDriver driver_dondokod	   = new GameDriver("1989"	,"dondokod"	,"taito_f2.java"	,rom_dondokod,null	,machine_driver_dondokod	,input_ports_dondokod	,null	,ROT0	,	"Taito Corporation", "Don Doko Don (Japan)" )
	public static GameDriver driver_megab	   = new GameDriver("1989"	,"megab"	,"taito_f2.java"	,rom_megab,null	,machine_driver_megab	,input_ports_megab	,null	,ROT0	,	"Taito Corporation Japan", "Mega Blast (World)" )
	public static GameDriver driver_megabj	   = new GameDriver("1989"	,"megabj"	,"taito_f2.java"	,rom_megabj,driver_megab	,machine_driver_megab	,input_ports_megabj	,null	,ROT0	,	"Taito Corporation", "Mega Blast (Japan)" )
	public static GameDriver driver_thundfox	   = new GameDriver("1990"	,"thundfox"	,"taito_f2.java"	,rom_thundfox,null	,machine_driver_thundfox	,input_ports_thundfox	,null	,ROT0	,	"Taito Corporation", "Thunder Fox (Japan)" )
	public static GameDriver driver_cameltry	   = new GameDriver("1989"	,"cameltry"	,"taito_f2.java"	,rom_cameltry,null	,machine_driver_cameltry	,input_ports_cameltry	,null	,ROT0	,	"Taito Corporation", "Camel Try (Japan)"  )
	public static GameDriver driver_cameltru	   = new GameDriver("1989"	,"cameltru"	,"taito_f2.java"	,rom_cameltru,driver_cameltry	,machine_driver_cameltry	,input_ports_cameltry	,null	,ROT0	,	"Taito America Corporation", "Camel Try (US)" )
	public static GameDriver driver_qtorimon	   = new GameDriver("1990"	,"qtorimon"	,"taito_f2.java"	,rom_qtorimon,null	,machine_driver_qtorimon	,input_ports_qtorimon	,null	,ROT0	,	"Taito Corporation", "Quiz Torimonochou (Japan)" )
	public static GameDriver driver_liquidk	   = new GameDriver("1990"	,"liquidk"	,"taito_f2.java"	,rom_liquidk,null	,machine_driver_liquidk	,input_ports_liquidk	,null	,ROT0	,	"Taito Corporation Japan", "Liquid Kids (World)" )
	public static GameDriver driver_liquidku	   = new GameDriver("1990"	,"liquidku"	,"taito_f2.java"	,rom_liquidku,driver_liquidk	,machine_driver_liquidk	,input_ports_liquidk	,null	,ROT0	,	"Taito America Corporation", "Liquid Kids (US)" )
	public static GameDriver driver_mizubaku	   = new GameDriver("1990"	,"mizubaku"	,"taito_f2.java"	,rom_mizubaku,driver_liquidk	,machine_driver_liquidk	,input_ports_mizubaku	,null	,ROT0	,	"Taito Corporation", "Mizubaku Daibouken (Japan)" )
	public static GameDriver driver_quizhq	   = new GameDriver("1990"	,"quizhq"	,"taito_f2.java"	,rom_quizhq,null	,machine_driver_quizhq	,input_ports_quizhq	,null	,ROT0	,	"Taito Corporation", "Quiz HQ (Japan)" )
	public static GameDriver driver_ssi	   = new GameDriver("1990"	,"ssi"	,"taito_f2.java"	,rom_ssi,null	,machine_driver_ssi	,input_ports_ssi	,null	,ROT270	,	"Taito Corporation Japan", "Super Space Invaders '91 (World)" )
	public static GameDriver driver_majest12	   = new GameDriver("1990"	,"majest12"	,"taito_f2.java"	,rom_majest12,driver_ssi	,machine_driver_ssi	,input_ports_majest12	,null	,ROT270	,	"Taito Corporation", "Majestic Twelve - The Space Invaders Part IV (Japan)" )
	public static GameDriver driver_gunfront	   = new GameDriver("1990"	,"gunfront"	,"taito_f2.java"	,rom_gunfront,null	,machine_driver_gunfront	,input_ports_gunfront	,null	,ROT270	,	"Taito Corporation Japan", "Gun & Frontier (World)" )
	public static GameDriver driver_gunfronj	   = new GameDriver("1990"	,"gunfronj"	,"taito_f2.java"	,rom_gunfronj,driver_gunfront	,machine_driver_gunfront	,input_ports_gunfronj	,null	,ROT270	,	"Taito Corporation", "Gun Frontier (Japan)" )
	public static GameDriver driver_growl	   = new GameDriver("1990"	,"growl"	,"taito_f2.java"	,rom_growl,null	,machine_driver_growl	,input_ports_growl	,null	,ROT0	,	"Taito Corporation Japan", "Growl (World)" )
	public static GameDriver driver_growlu	   = new GameDriver("1990"	,"growlu"	,"taito_f2.java"	,rom_growlu,driver_growl	,machine_driver_growl	,input_ports_growl	,null	,ROT0	,	"Taito America Corporation", "Growl (US)" )
	public static GameDriver driver_runark	   = new GameDriver("1990"	,"runark"	,"taito_f2.java"	,rom_runark,driver_growl	,machine_driver_growl	,input_ports_runark	,null	,ROT0	,	"Taito Corporation", "Runark (Japan)" )
	public static GameDriver driver_mjnquest	   = new GameDriver("1990"	,"mjnquest"	,"taito_f2.java"	,rom_mjnquest,null	,machine_driver_mjnquest	,input_ports_mjnquest	,init_mjnquest	,ROT0	,	"Taito Corporation", "Mahjong Quest (Japan)" )
	public static GameDriver driver_mjnquesb	   = new GameDriver("1990"	,"mjnquesb"	,"taito_f2.java"	,rom_mjnquesb,driver_mjnquest	,machine_driver_mjnquest	,input_ports_mjnquest	,init_mjnquest	,ROT0	,	"Taito Corporation", "Mahjong Quest (No Nudity)" )
	public static GameDriver driver_footchmp	   = new GameDriver("1990"	,"footchmp"	,"taito_f2.java"	,rom_footchmp,null	,machine_driver_footchmp	,input_ports_footchmp	,null	,ROT0	,	"Taito Corporation Japan", "Football Champ (World)" )
	public static GameDriver driver_hthero	   = new GameDriver("1990"	,"hthero"	,"taito_f2.java"	,rom_hthero,driver_footchmp	,machine_driver_hthero	,input_ports_hthero	,null	,ROT0	,	"Taito Corporation", "Hat Trick Hero (Japan)" )
	public static GameDriver driver_euroch92	   = new GameDriver("1992"	,"euroch92"	,"taito_f2.java"	,rom_euroch92,driver_footchmp	,machine_driver_footchmp	,input_ports_footchmp	,null	,ROT0	,	"Taito Corporation Japan", "Euro Champ '92 (World)" )
	public static GameDriver driver_koshien	   = new GameDriver("1990"	,"koshien"	,"taito_f2.java"	,rom_koshien,null	,machine_driver_koshien	,input_ports_koshien	,null	,ROT0	,	"Taito Corporation", "Ah Eikou no Koshien (Japan)" )
	public static GameDriver driver_yuyugogo	   = new GameDriver("1990"	,"yuyugogo"	,"taito_f2.java"	,rom_yuyugogo,null	,machine_driver_yuyugogo	,input_ports_yuyugogo	,null	,ROT0	,	"Taito Corporation", "Yuuyu no Quiz de GO!GO! (Japan)" )
	public static GameDriver driver_ninjak	   = new GameDriver("1990"	,"ninjak"	,"taito_f2.java"	,rom_ninjak,null	,machine_driver_ninjak	,input_ports_ninjak	,null	,ROT0	,	"Taito Corporation Japan", "Ninja Kids (World)" )
	public static GameDriver driver_ninjakj	   = new GameDriver("1990"	,"ninjakj"	,"taito_f2.java"	,rom_ninjakj,driver_ninjak	,machine_driver_ninjak	,input_ports_ninjakj	,null	,ROT0	,	"Taito Corporation", "Ninja Kids (Japan)" )
	public static GameDriver driver_solfigtr	   = new GameDriver("1991"	,"solfigtr"	,"taito_f2.java"	,rom_solfigtr,null	,machine_driver_solfigtr	,input_ports_solfigtr	,null	,ROT0	,	"Taito Corporation Japan", "Solitary Fighter (World)" )
	public static GameDriver driver_qzquest	   = new GameDriver("1991"	,"qzquest"	,"taito_f2.java"	,rom_qzquest,null	,machine_driver_qzquest	,input_ports_	,init_qzquest	,0	,	ROT0,       "Taito Corporation", "Quiz Quest - Hime to Yuusha no Monogatari (Japan)" )
	public static GameDriver driver_pulirula	   = new GameDriver("1991"	,"pulirula"	,"taito_f2.java"	,rom_pulirula,null	,machine_driver_pulirula	,input_ports_pulirula	,null	,ROT0	,	"Taito Corporation Japan", "PuLiRuLa (World)" )
	public static GameDriver driver_pulirulj	   = new GameDriver("1991"	,"pulirulj"	,"taito_f2.java"	,rom_pulirulj,driver_pulirula	,machine_driver_pulirula	,input_ports_pulirulj	,null	,ROT0	,	"Taito Corporation", "PuLiRuLa (Japan)" )
	public static GameDriver driver_metalb	   = new GameDriver("1991"	,"metalb"	,"taito_f2.java"	,rom_metalb,null	,machine_driver_metalb	,input_ports_metalb	,null	,ROT0_16BIT	,	"Taito Corporation Japan", "Metal Black (World)" )
	public static GameDriver driver_metalbj	   = new GameDriver("1991"	,"metalbj"	,"taito_f2.java"	,rom_metalbj,driver_metalb	,machine_driver_metalb	,input_ports_metalbj	,null	,ROT0_16BIT	,	"Taito Corporation", "Metal Black (Japan)" )
	public static GameDriver driver_qzchikyu	   = new GameDriver("1991"	,"qzchikyu"	,"taito_f2.java"	,rom_qzchikyu,null	,machine_driver_qzchikyu	,input_ports_qzchikyu	,null	,ROT0	,	"Taito Corporation", "Quiz Chikyu Bouei Gun (Japan)" )
	public static GameDriver driver_yesnoj	   = new GameDriver("1992"	,"yesnoj"	,"taito_f2.java"	,rom_yesnoj,null	,machine_driver_yesnoj	,input_ports_yesnoj	,null	,ROT0	,	"Taito Corporation", "Yes/No Sinri Tokimeki Chart" )
	public static GameDriver driver_deadconx	   = new GameDriver("1992"	,"deadconx"	,"taito_f2.java"	,rom_deadconx,null	,machine_driver_deadconx	,input_ports_deadconx	,null	,ROT0	,	"Taito Corporation Japan", "Dead Connection (World)" )
	public static GameDriver driver_deadconj	   = new GameDriver("1992"	,"deadconj"	,"taito_f2.java"	,rom_deadconj,driver_deadconx	,machine_driver_deadconj	,input_ports_deadconj	,null	,ROT0	,	"Taito Corporation", "Dead Connection (Japan)" )
	public static GameDriver driver_dinorex	   = new GameDriver("1992"	,"dinorex"	,"taito_f2.java"	,rom_dinorex,null	,machine_driver_dinorex	,input_ports_dinorex	,null	,ROT0	,	"Taito Corporation Japan", "Dino Rex (World)" )
	public static GameDriver driver_dinorexj	   = new GameDriver("1992"	,"dinorexj"	,"taito_f2.java"	,rom_dinorexj,driver_dinorex	,machine_driver_dinorex	,input_ports_dinorexj	,null	,ROT0	,	"Taito Corporation", "Dino Rex (Japan)" )
	public static GameDriver driver_dinorexu	   = new GameDriver("1992"	,"dinorexu"	,"taito_f2.java"	,rom_dinorexu,driver_dinorex	,machine_driver_dinorex	,input_ports_dinorex	,null	,ROT0	,	"Taito America Corporation", "Dino Rex (US)" )
	public static GameDriver driver_qjinsei	   = new GameDriver("1992"	,"qjinsei"	,"taito_f2.java"	,rom_qjinsei,null	,machine_driver_qjinsei	,input_ports_qjinsei	,null	,ROT0	,	"Taito Corporation", "Quiz Jinsei Gekijoh (Japan)" )
	public static GameDriver driver_qcrayon	   = new GameDriver("1993"	,"qcrayon"	,"taito_f2.java"	,rom_qcrayon,null	,machine_driver_qcrayon	,input_ports_qcrayon	,null	,ROT0	,	"Taito Corporation", "Quiz Crayon Shinchan (Japan)" )
	public static GameDriver driver_qcrayon2	   = new GameDriver("1993"	,"qcrayon2"	,"taito_f2.java"	,rom_qcrayon2,null	,machine_driver_qcrayon2	,input_ports_qcrayon2	,null	,ROT0	,	"Taito Corporation", "Crayon Shinchan Orato Asobo (Japan)" )
	public static GameDriver driver_driftout	   = new GameDriver("1991"	,"driftout"	,"taito_f2.java"	,rom_driftout,null	,machine_driver_driftout	,input_ports_driftout	,null	,ROT270	,	"Visco", "Drift Out (Japan)", GAME_NO_COCKTAIL )
	public static GameDriver driver_driveout	   = new GameDriver("1991"	,"driveout"	,"taito_f2.java"	,rom_driveout,driver_driftout	,machine_driver_driftout	,input_ports_driftout	,null	,ROT270	,	"bootleg", "Drive Out", GAME_NO_SOUND )
}
