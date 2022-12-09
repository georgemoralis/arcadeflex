/***************************************************************************
						WEC Le Mans 24  &   Hot Chase

					      (C)   1986 & 1988 Konami

					driver by	Luca Elia (eliavit@unina.it)


----------------------------------------------------------------------
Hardware				Main 	Sub		Sound	Sound Chips
----------------------------------------------------------------------
[WEC Le Mans 24]		68000	68000	Z-80	YM2151 YM3012 1x007232

[Hot Chase]				68000	68000	68B09E	              3x007232

 [CPU PCB GX763 350861B]
 						007641	007770	3x007232	051550

 [VID PCB GX763 350860A AI AM-1]
 						007634	007635	3x051316	007558	007557
----------------------------------------------------------------------


----------------------------------------------------------------
Main CPU				[WEC Le Mans 24]		[Hot Chase]
----------------------------------------------------------------
ROM				R		000000-03ffff			<
Work RAM		RW		040000-043fff			040000-063fff*
?				RW		060000-060007			-
Blitter			 W		080000-080011			<
Page RAM		RW		100000-103fff			-
Text RAM		RW		108000-108fff			-
Palette RAM		RW		110000-110fff			110000-111fff**
Shared RAM		RW		124000-127fff			120000-123fff
Sprites RAM		RW		130000-130fff			<
Input Ports		RW		1400xx-1400xx			<
Background		RW		-						100000-100fff
Background Ctrl	 W		-						101000-10101f
Foreground		RW		-						102000-102fff
Foreground Ctrl	 W		-						103000-10301f

* weird					** only half used

----------------------------------------------------------------
Sub CPU					[WEC Le Mans 24]		[Hot Chase]
----------------------------------------------------------------

ROM				R		000000-00ffff			000000-01ffff
Work RAM		RW		-						060000-060fff
Road RAM		RW		060000-060fff			020000-020fff
Shared RAM		RW		070000-073fff			040000-043fff


---------------------------------------------------------------------------
								Game code
							[WEC Le Mans 24]
---------------------------------------------------------------------------

					Interesting locations (main cpu)
					--------------------------------

There's some 68000 assembly code in ASCII around d88 :-)

040000+
7-9				*** hi score/10 (BCD 3 bytes) ***
b-d				*** score/10 (BCD 3 bytes) ***
1a,127806		<- 140011.b
1b,127807		<- 140013.b
1c,127808		<- 140013.b
1d,127809		<- 140015.b
1e,12780a		<- 140017.b
1f				<- 140013.b
30				*** credits ***
3a,3b,3c,3d		<-140021.b
3a = accelerator   3b = ??   3c = steering   3d = table

d2.w			. 108f24 fg y scroll
112.w			. 108f26 bg y scroll

16c				influences 140031.b
174				screen address
180				input port selection (.140003.b .140021.b)
181				.140005.b
185				bit 7 high . must copy sprite data to 130000
1dc+(1da).w		.140001.b

40a.w,c.w		*** time (BCD) ***
411				EF if brake, 0 otherwise
416				?
419				gear: 0=lo,1=hi
41e.w			speed related .127880
424.w			speed BCD
43c.w			accel?
440.w			level?

806.w			scrollx related
80e.w			scrolly related

c08.b			routine select: 1>1e1a4	2>1e1ec	3>1e19e	other>1e288 (map screen)

117a.b			selected letter when entering name in hi-scores
117e.w			cycling color in hi-scores

12c0.w			?time,pos,len related?
12c2.w
12c4.w
12c6.w

1400-1bff		color data (0000-1023 chars)
1c00-23ff		color data (1024-2047 sprites?)

2400			Sprite data: 40 entries x  4 bytes =  100
2800			Sprite data: 40 entries x 40 bytes = 1000
3800			Sprite data: 40 entries x 10 bytes =  400

					Interesting routines (main cpu)
					-------------------------------

804				mem test
818				end mem test (cksums at 100, addresses at A90)
82c				other cpu test
a0c				rom test
c1a				prints string (a1)
1028			end test
204c			print 4*3 box of chars to a1, made up from 2 2*6 (a0)=0xLR (Left,Righ index)
4e62			raws in the fourth page of chars
6020			test screen (print)
60d6			test screen
62c4			motor test?
6640			print input port values ( 6698 = scr_disp.w,ip.b,bit.b[+/-] )

819c			prepares sprite data
8526			blitter: 42400.130000
800c			8580	sprites setup on map screen

1833a			cycle cols on hi-scores
18514			hiscores: main loop
185e8			hiscores: wheel selects letter

TRAP#0			prints string: A0. addr.l, attr.w, (char.b)*, 0

IRQs			1,3,6]	602
				2,7]	1008.12dc	ORI.W    #$2700,(A7) RTE
				4]	1004.124c
				5]	106c.1222	calls sequence: $3d24 $1984 $28ca $36d2 $3e78




					Interesting locations (sub cpu)
					-------------------------------

					Interesting routines (sub cpu)
					------------------------------

1028	'wait for command' loop.
1138	lev4 irq
1192	copies E0*4 bytes: (a1)+ . (a0)+





---------------------------------------------------------------------------
								 Game code
								[Hot Chase]
---------------------------------------------------------------------------

This game has been probably coded by the same programmers of WEC Le Mans 24
It shares some routines and there is the (hidden?) string "WEC 2" somewhere

							Main CPU		Sub CPU

Interrupts:		1, 7] 		FFFFFFFF		FFFFFFFF
				2,3,4,5,6]	221c			1288

Self Test:
 0] pause,120002==55,pause,120002==AA,pause,120002==CC, (on error set bit d7.0)
 6] 60000-63fff(d7.1),40000-41fff(d7.2)
 8] 40000/2<-chksum 0-20000(e/o);40004/6<-chksum 20000-2ffff(e/o) (d7.3456)
 9] chksums from sub cpu: even.40004	odd.(40006)	(d7.78)
 A] 110000-111fff(even)(d7.9),102000-102fff(odd)(d7.a)
 C] 100000-100fff(odd)(d7.b),pause,pause,pause
10] 120004==0(d7.c),120006==0(d7.d),130000-1307ff(first $A of every $10 bytes only)(d7.e),pause
14] 102000<-hw screen+(d7==0)? jmp 1934/1000
15] 195c start of game


					Interesting locations (main cpu)
					--------------------------------

60024.b			<- !140017.b (DSW 1 - coinage)
60025.b			<- !140015.b (DSW 2 - options)
6102c.w			*** time ***

					Interesting routines (main cpu)
					-------------------------------

18d2			(d7.d6)?print BAD/OK to (a5)+, jmp(D0)
1d58			print d2.w to (a4)+, jmp(a6)
580c			writes at 60000
61fc			print test strings
18cbe			print "game over"




---------------------------------------------------------------------------
								   Issues
							  [WEC Le Mans 24]
---------------------------------------------------------------------------

- Wrong colours (only the text layer is ok at the moment. Note that the top
  half of colours is written by the blitter, 16 colours a time, the bottom
  half by the cpu, 8 colours a time)
- The parallactic scrolling is sometimes wrong

---------------------------------------------------------------------------
								   Issues
								[Hot Chase]
---------------------------------------------------------------------------

- Samples pitch is too low
- No zoom and rotation of the layers

---------------------------------------------------------------------------
							   Common Issues
---------------------------------------------------------------------------

- One ROM unused (32K in hotchase, 16K in wecleman)
- Incomplete DSWs
- No shadow sprites
- Sprite ram is not cleared by the game and no sprite list end-marker
  is written. We cope with that with an hack in the Blitter but there
  must be a register to do the trick


***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static common.libc.cstring.memcpy;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.M6809_INT_FIRQ;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.M6809_IRQ_LINE;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inputH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.wecleman.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static gr.codebb.arcadeflex.v036.mame.memory.*;
import static arcadeflex.v036.sound._2151intf.*;
import arcadeflex.v036.sound._2151intfH.YM2151interface;
import static arcadeflex.v036.sound.k007232.*;
import arcadeflex.v036.sound.k007232H;
import static arcadeflex.v036.sound.k007232H.K007232_VOL;
import arcadeflex.v036.sound.k007232H.K007232_interface;
import arcadeflex.v036.sound.k007232H.portwritehandlerPtr;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;

public class wecleman
{
	
	/* Variables only used here: */
	
	static UBytePtr sharedram=new UBytePtr(), blitter_regs=new UBytePtr();
	static int[] multiply_reg = new int[2];
	
	
	
	/* Variables that vidhrdw has acces to: */
	
	public static int wecleman_selected_ip, wecleman_irqctrl;
	
	
	/* This macro is used to decipher the gfx ROMs */
	
	static void BITSWAP(int i, UBytePtr _from, int _len, int _14, int _13, int _12, int _11, int _10, int _f, int _e, int _d, int _c, int _b, int _a, int _9, int _8, int _7, int _6, int _5, int _4, int _3, int _2, int _1, int _0)
        {	UBytePtr buffer;
		UBytePtr src = new UBytePtr(_from);
		if ((buffer = new UBytePtr(_len)) != null)
		{
			for (i = 0 ; i < _len ; i++)
				buffer.write(i,
				 src.read((((i & (1 << _0))!=0?(1<<0x0):0) + 
					 ((i & (1 << _1))!=0?(1<<0x1):0) + 
					 ((i & (1 << _2))!=0?(1<<0x2):0) + 
					 ((i & (1 << _3))!=0?(1<<0x3):0) + 
					 ((i & (1 << _4))!=0?(1<<0x4):0) + 
					 ((i & (1 << _5))!=0?(1<<0x5):0) + 
					 ((i & (1 << _6))!=0?(1<<0x6):0) + 
					 ((i & (1 << _7))!=0?(1<<0x7):0) + 
					 ((i & (1 << _8))!=0?(1<<0x8):0) + 
					 ((i & (1 << _9))!=0?(1<<0x9):0) + 
					 ((i & (1 << _a))!=0?(1<<0xa):0) + 
					 ((i & (1 << _b))!=0?(1<<0xb):0) + 
					 ((i & (1 << _c))!=0?(1<<0xc):0) + 
					 ((i & (1 << _d))!=0?(1<<0xd):0) + 
					 ((i & (1 << _e))!=0?(1<<0xe):0) + 
					 ((i & (1 << _f))!=0?(1<<0xf):0) + 
					 ((i & (1 << _10))!=0?(1<<0x10):0) + 
					 ((i & (1 << _11))!=0?(1<<0x11):0) + 
					 ((i & (1 << _12))!=0?(1<<0x12):0) + 
					 ((i & (1 << _13))!=0?(1<<0x13):0) + 
					 ((i & (1 << _14))!=0?(1<<0x14):0)))); 
			memcpy(src, buffer, _len); 
			buffer=null; 
		} 
	}
	
	
	
	/***************************************************************************
								Common routines
	***************************************************************************/
	
	
	/* 140005.b (WEC Le Mans 24 Schematics)
	
	 COMMAND
	 ___|____
	|   CK  8|--/			7
	| LS273 7| TV-KILL		6
	|       6| SCR-VCNT		5
	|       5| SCR-HCNT		4
	|   5H  4| SOUND-RST	3
	|       3| SOUND-ON		2
	|       2| NSUBRST		1
	|       1| SUBINT		0
	|__CLR___|
	    |
	  NEXRES
	
	 Schems: SUBRESET does a RST+HALT
			 Sub CPU IRQ 4 generated by SUBINT, no other IRQs
	*/
	
	public static WriteHandlerPtr irqctrl_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	
	//	logerror("CPU #0 - PC = %06X - $140005 <- %02X (old value: %02X)\n",cpu_get_pc(), data&0xFF, old_data&0xFF);
	
	//	Bit 0 : SUBINT
		if ( (wecleman_irqctrl & 1)!=0 && ((data & 1)==0) )	// 1.0 transition
			cpu_set_irq_line(1,4,HOLD_LINE);
	
	
	//	Bit 1 : NSUBRST
		if ((data & 2) != 0)
			cpu_set_reset_line(1,CLEAR_LINE);
		else
			cpu_set_reset_line(1,ASSERT_LINE);
	
	
	//	Bit 2 : SOUND-ON
	//	Bit 3 : SOUNDRST
	//	Bit 4 : SCR-HCNT
	//	Bit 5 : SCR-VCNT
	//	Bit 6 : TV-KILL
	
		wecleman_irqctrl = data;	// latch the value
	} };
	
	
	
	
	
	public static ReadHandlerPtr accelerator_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
                int MAX_ACCEL = 0x80;
	
		return (readinputport(4) & 1)!=0 ? MAX_ACCEL : 0;
	} };
	
	
	/* This function allows the gear to be handled using two buttons
	   A macro is needed because wecleman sees the high gear when a
	   bit is on, hotchase when a bit is off */
	
	//#define READ_GEAR(_name_,_high_gear_) \
        static int ret = 0;
        
	//READ_GEAR(wecleman_gear_r,1)
        public static ReadHandlerPtr wecleman_gear_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		ret = (1 ^ 1) << 5; /* start with low gear */
		switch ( (readinputport(4) >> 2) & 3 )
		{
			case 1 : ret = (1 ^ 1) << 5;	break;	/* low gear */
			case 2 : ret = (1    ) << 5;	break;	/*  high gear */
		}
		return (ret | readinputport(0));	/* previous value */
	} };
        
	//READ_GEAR(hotchase_gear_r,0)
        public static ReadHandlerPtr hotchase_gear_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		ret = (0 ^ 1) << 5; /* start with low gear */
		switch ( (readinputport(4) >> 2) & 3 )
		{
			case 1 : ret = (0 ^ 1) << 5;	break;	/* low gear */
			case 2 : ret = (0    ) << 5;	break;	/*  high gear */
		}
		return (ret | readinputport(0));	/* previous value */
	} };
	
	
	
	/* 140003.b (usually paired with a write to 140021.b)
	
		Bit:
	
		7-------	?
		-65-----	input selection (0-3)
		---43---	?
		-----2--	start light
		------10	? out 1/2
	
	*/
	public static WriteHandlerPtr selected_ip_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		wecleman_selected_ip = data;	// latch the value
	} };
	
	
	/* $140021.b - Return the previously selected input port's value */
	public static ReadHandlerPtr selected_ip_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch ( (wecleman_selected_ip >> 5) & 3 )
		{													// From WEC Le Mans Schems:
			case 0: 	return accelerator_r.handler(offset);		// Accel - Schems: Accelevr
			case 1: 	return 0xffff;						// ????? - Schems: Not Used
			case 2:		return input_port_5_r.handler(offset);		// Wheel - Schems: Handlevr
			case 3:		return 0xffff;						// Table - Schems: Turnvr
	
			default:	return 0xffff;
		}
	} };
	
	
	
	/* Data is read from and written to *sharedram* */
	public static ReadHandlerPtr sharedram_r  = new ReadHandlerPtr() { public int handler(int offset)				{ return sharedram.READ_WORD(offset); } };
	public static WriteHandlerPtr sharedram_w = new WriteHandlerPtr() {public void handler(int offset, int data)	{ COMBINE_WORD_MEM(sharedram,offset, data); } };
	
	
	/* Data is read from and written to *spriteram* */
	public static ReadHandlerPtr spriteram_word_r  = new ReadHandlerPtr() { public int handler(int offset)			{ return spriteram.READ_WORD(offset); } };
	public static WriteHandlerPtr spriteram_word_w = new WriteHandlerPtr() {public void handler(int offset, int data)	{ COMBINE_WORD_MEM(spriteram,offset, data); } };
	
	
	
	
	/*	Word Blitter	-	Copies data around (Work RAM, Sprite RAM etc.)
							It's fed with a list of blits to do
	
		Offset:
	
		00.b		? Number of words - 1 to add to address per transfer
		01.b		? logic function / blit mode
		02.w		? (always 0)
		04.l		Source address (Base address of source data)
		08.l		List of blits address
		0c.l		Destination address
		01.b		? Number of transfers
		10.b		Triggers the blit
		11.b		Number of words per transfer
	
		The list contains 4 bytes per blit:
	
		Offset:
	
		00.w		?
		02.w		offset from Base address
	
	*/
	
	public static ReadHandlerPtr blitter_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return blitter_regs.READ_WORD(offset);
	} };
	
	public static WriteHandlerPtr blitter_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(blitter_regs, offset, data);
	
		/* do a blit if $80010.b has been written */
		if ((offset == 0x10) && (data&0x00FF0000)!=0)
		{
			/* 80000.b = ?? usually 0 - other values: 02 ; 00 - ? logic function ? */
			/* 80001.b = ?? usually 0 - other values: 3f ; 01 - ? height ? */
			int minterm		=	(blitter_regs.READ_WORD(0x0) & 0xFF00 ) >> 8;
			int list_len	=	(blitter_regs.READ_WORD(0x0) & 0x00FF ) >> 0;
	
			/* 80002.w = ?? always 0 - ? increment per horizontal line ? */
			/* no proof at all, it's always 0 */
	//		int srcdisp		=	READ_WORD(&blitter_regs[0x2])&0xFF00;
	//		int destdisp	=	READ_WORD(&blitter_regs[0x2])&0x00FF;
	
			/* 80004.l = source data address */
			int src  =	(blitter_regs.READ_WORD(0x4)<<16)+
						 blitter_regs.READ_WORD(0x6);
	
			/* 80008.l = list of blits address */
			int list =	(blitter_regs.READ_WORD(0x8)<<16)+
					 	 blitter_regs.READ_WORD(0xA);
	
			/* 8000C.l = destination address */
			int dest =	(blitter_regs.READ_WORD(0xC)<<16)+
						 blitter_regs.READ_WORD(0xE);
	
			/* 80010.b = number of words to move */
			int size =	(blitter_regs.READ_WORD(0x10))&0x00FF;
	
/*TODO*///	#if 0
/*TODO*///			{
/*TODO*///				int i;
/*TODO*///				logerror("Blitter (PC = %06X): ",cpu_get_pc());
/*TODO*///				for (i=0;i<0x12;i+=2) logerror("%04X ",READ_WORD(&blitter_regs[i]) );
/*TODO*///				logerror("\n");
/*TODO*///			}
/*TODO*///	#endif
	
			/* Word aligned transfers only */
			src  &= (~1);	list &= (~1);	dest &= (~1);
	
	
			/* Two minterms / blit modes are used */
			if (minterm != 2)
			{
				/* One single blit */
				for ( ; size > 0 ; size--)
				{
					/* maybe slower than a memcpy but safer (and errors are logged) */
					cpu_writemem24_word(dest,cpu_readmem24_word(src));
					src += 2;		dest += 2;
				}
	//			src  += srcdisp;	dest += destdisp;
			}
			else
			{
				/* Number of blits in the list */
				for ( ; list_len > 0 ; list_len-- )
				{
				int j;
	
					/* Read offset of source from the list of blits */
					int addr = src + cpu_readmem24_word( list + 2 );
	
					for (j = size; j > 0; j--)
					{
						cpu_writemem24_word(dest,cpu_readmem24_word(addr));
						dest += 2;	addr += 2;
					}
					dest += 16-size*2;	/* hack for the blit to Sprites RAM */
					list +=  4;
				}
	
				/* hack for the blit to Sprites RAM - Sprite list end-marker */
				cpu_writemem24_word(dest,0xFFFF);
			}
		} /* end blit */
	} };
	
	
	
	/*
	**
	**	Main cpu data
	**
	**
	*/
	
	
	
	/***************************************************************************
									WEC Le Mans 24
	***************************************************************************/
	
	/***************************************************************************
									Hot Chase
	***************************************************************************/
	
	/* 140001.b */
	public static WriteHandlerPtr hotchase_soundlatch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		soundlatch_w.handler(0,data & 0xFF);
		cpu_set_irq_line(2,M6809_IRQ_LINE, HOLD_LINE);
	} };
        
	static MemoryReadAddress wecleman_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM				),	// ROM
		new MemoryReadAddress( 0x040000, 0x043fff, MRA_BANK1				),	// RAM
		new MemoryReadAddress( 0x060000, 0x060007, MRA_BANK2				),	// Video Registers? (only 60006.w is read)
		new MemoryReadAddress( 0x080000, 0x080011, blitter_r				),	// Blitter (reading is for debug)
		new MemoryReadAddress( 0x100000, 0x103fff, wecleman_pageram_r	),	// Background Layers
		new MemoryReadAddress( 0x108000, 0x108fff, wecleman_txtram_r		),	// Text Layer
		new MemoryReadAddress( 0x110000, 0x110fff, paletteram_word_r		),	// Palette
		new MemoryReadAddress( 0x124000, 0x127fff, sharedram_r			),	// Shared with sub CPU
		new MemoryReadAddress( 0x130000, 0x130fff, spriteram_word_r		),	// Sprites
		// Input Ports:
		new MemoryReadAddress( 0x140010, 0x140011, wecleman_gear_r		),	// Coins + brake + gear
		new MemoryReadAddress( 0x140012, 0x140013, input_port_1_r		),	// ??
		new MemoryReadAddress( 0x140014, 0x140015, input_port_2_r		),	// DSW
		new MemoryReadAddress( 0x140016, 0x140017, input_port_3_r		),	// DSW
		new MemoryReadAddress( 0x140020, 0x140021, selected_ip_r			),	// Accelerator or Wheel or ..
		new MemoryReadAddress( -1 )
	};
        
        /***************************************************************************
									WEC Le Mans 24
	***************************************************************************/
	
	/* 140001.b */
	public static WriteHandlerPtr wecleman_soundlatch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		soundlatch_w.handler(0,data & 0xFF);
		cpu_set_irq_line(2,0, HOLD_LINE);
	} };
	
	static MemoryWriteAddress wecleman_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM											),	// ROM (03c000-03ffff used as RAM sometimes!)
		new MemoryWriteAddress( 0x040000, 0x043fff, MWA_BANK1											),	// RAM
		new MemoryWriteAddress( 0x060000, 0x060007, MWA_BANK2, wecleman_unknown						),	// Video Registers?
		new MemoryWriteAddress( 0x080000, 0x080011, blitter_w, blitter_regs							),	// Blitter
		new MemoryWriteAddress( 0x100000, 0x103fff, wecleman_pageram_w, wecleman_pageram				),	// Background Layers
		new MemoryWriteAddress( 0x108000, 0x108fff, wecleman_txtram_w, wecleman_txtram				),	// Text Layer
		new MemoryWriteAddress( 0x110000, 0x110fff, paletteram_SBGRBBBBGGGGRRRR_word_w, paletteram	),	// Palette
		new MemoryWriteAddress( 0x124000, 0x127fff, sharedram_w, sharedram							),	// Shared with main CPU
		new MemoryWriteAddress( 0x130000, 0x130fff, spriteram_word_w, spriteram, spriteram_size		),	// Sprites
		new MemoryWriteAddress( 0x140000, 0x140001, wecleman_soundlatch_w						),	// To sound CPU
		new MemoryWriteAddress( 0x140002, 0x140003, selected_ip_w								),	// Selects accelerator / wheel / ..
		new MemoryWriteAddress( 0x140004, 0x140005, irqctrl_w									),	// Main CPU controls the other CPUs
		new MemoryWriteAddress( 0x140006, 0x140007, MWA_NOP									),	// Watchdog reset
		new MemoryWriteAddress( 0x140020, 0x140021, MWA_NOP									),	// Paired with writes to $140003
		new MemoryWriteAddress( 0x140030, 0x140031, MWA_BANK3									),	// ??
		new MemoryWriteAddress( -1 )
	};
	
	
	
	
	
	
	
	
	
	/***************************************************************************
									Hot Chase
	***************************************************************************/
	
	public static ReadHandlerPtr hotchase_K051316_0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K051316_0_r.handler(offset >> 1);
	} };
	
	public static ReadHandlerPtr hotchase_K051316_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K051316_1_r.handler(offset >> 1);
	} };
	
	public static WriteHandlerPtr hotchase_K051316_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			K051316_0_w.handler(offset >> 1, data & 0xff);
	} };
	
	public static WriteHandlerPtr hotchase_K051316_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			K051316_1_w.handler(offset >> 1, data & 0xff);
	} };
	
	public static WriteHandlerPtr hotchase_K051316_ctrl_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			K051316_ctrl_0_w.handler(offset >> 1, data & 0xff);
	} };
	
	public static WriteHandlerPtr hotchase_K051316_ctrl_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			K051316_ctrl_1_w.handler(offset >> 1, data & 0xff);
	} };
	
	
	
	static MemoryReadAddress hotchase_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM				),	// ROM
		new MemoryReadAddress( 0x040000, 0x063fff, MRA_BANK1				),	// RAM (weird size!?)
		new MemoryReadAddress( 0x080000, 0x080011, MRA_BANK2				),	// Blitter
		new MemoryReadAddress( 0x100000, 0x100fff, hotchase_K051316_0_r ),	// Background
		new MemoryReadAddress( 0x102000, 0x102fff, hotchase_K051316_1_r ),	// Foreground
		new MemoryReadAddress( 0x110000, 0x111fff, paletteram_word_r		),	// Palette (only the first 2048 colors used)
		new MemoryReadAddress( 0x120000, 0x123fff, sharedram_r			),	// Shared with sub CPU
		new MemoryReadAddress( 0x130000, 0x130fff, spriteram_word_r		),	// Sprites
		// Input Ports:
		new MemoryReadAddress( 0x140006, 0x140007, MRA_NOP				),	// Watchdog reset
		new MemoryReadAddress( 0x140010, 0x140011, hotchase_gear_r		),	// Coins + brake + gear
		new MemoryReadAddress( 0x140012, 0x140013, input_port_1_r		),	// ?? bit 4 from sound cpu
		new MemoryReadAddress( 0x140014, 0x140015, input_port_2_r		),	// DSW 2
		new MemoryReadAddress( 0x140016, 0x140017, input_port_3_r		),	// DSW 1
		new MemoryReadAddress( 0x140020, 0x140021, selected_ip_r			),	// Accelerator or Wheel or ..
	//	new MemoryReadAddress( 0x140022, 0x140023, MRA_NOP				),	// ??
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress hotchase_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM								),	// ROM
		new MemoryWriteAddress( 0x040000, 0x063fff, MWA_BANK1								),	// RAM (weird size!?)
		new MemoryWriteAddress( 0x080000, 0x080011, blitter_w, blitter_regs				),	// Blitter
		new MemoryWriteAddress( 0x100000, 0x100fff, hotchase_K051316_0_w ),		// Background
		new MemoryWriteAddress( 0x101000, 0x10101f, hotchase_K051316_ctrl_0_w ),	// Background Ctrl
		new MemoryWriteAddress( 0x102000, 0x102fff, hotchase_K051316_1_w ),		// Foreground
		new MemoryWriteAddress( 0x103000, 0x10301f, hotchase_K051316_ctrl_1_w ),	// Foreground Ctrl
		new MemoryWriteAddress( 0x110000, 0x111fff, paletteram_SBGRBBBBGGGGRRRR_word_w, paletteram	),	// Palette
		new MemoryWriteAddress( 0x120000, 0x123fff, sharedram_w, sharedram							),	// Shared with sub CPU
		new MemoryWriteAddress( 0x130000, 0x130fff, spriteram_word_w, spriteram, spriteram_size		),	// Sprites
		// Input Ports:
		new MemoryWriteAddress( 0x140000, 0x140001, hotchase_soundlatch_w					),	// To sound CPU
		new MemoryWriteAddress( 0x140002, 0x140003, selected_ip_w							),	// Selects accelerator / wheel / ..
		new MemoryWriteAddress( 0x140004, 0x140005, irqctrl_w								),	// Main CPU controls the other CPUs
		new MemoryWriteAddress( 0x140020, 0x140021, MWA_NOP								),	// Paired with writes to $140003
	//	new MemoryWriteAddress( 0x140030, 0x140031, MWA_NOP								),	// ??
		new MemoryWriteAddress( -1 )
	};
	
	
	
	
	
	
	/*
	**
	**	Sub cpu data
	**
	**
	*/
	
	/***************************************************************************
									WEC Le Mans 24
	***************************************************************************/
	
	static MemoryReadAddress wecleman_sub_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x00ffff, MRA_ROM		),	// ROM
		new MemoryReadAddress( 0x060000, 0x060fff, MRA_BANK8		),	// Road
		new MemoryReadAddress( 0x070000, 0x073fff, sharedram_r	),	// RAM (Shared with main CPU)
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress wecleman_sub_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x00ffff, MWA_ROM		),	// ROM
		new MemoryWriteAddress( 0x060000, 0x060fff, MWA_BANK8, wecleman_roadram, wecleman_roadram_size ),	// Road
		new MemoryWriteAddress( 0x070000, 0x073fff, sharedram_w	),	// RAM (Shared with main CPU)
		new MemoryWriteAddress( -1 )
	};
	
	
	
	
	
	
	
	
	
	
	
	
	/***************************************************************************
									Hot Chase
	***************************************************************************/
	
	
	static MemoryReadAddress hotchase_sub_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x01ffff, MRA_ROM		),	// ROM
		new MemoryReadAddress( 0x020000, 0x020fff, MRA_BANK7		),	// Road
		new MemoryReadAddress( 0x060000, 0x060fff, MRA_BANK8		),	// RAM
		new MemoryReadAddress( 0x040000, 0x043fff, sharedram_r	),	// Shared with main CPU
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress hotchase_sub_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x01ffff, MWA_ROM		),	// ROM
		new MemoryWriteAddress( 0x020000, 0x020fff, MWA_BANK7, wecleman_roadram, wecleman_roadram_size ),	// Road
		new MemoryWriteAddress( 0x060000, 0x060fff, MWA_BANK8		),	// RAM
		new MemoryWriteAddress( 0x040000, 0x043fff, sharedram_w	),	// Shared with main CPU
		new MemoryWriteAddress( -1 )
	};
	
	
	
	
	
	
	
	
	
	
	/*
	**
	**	Sound cpu data
	**
	**
	*/
	
	
	
	
	/* Protection - an external multiplyer connected to the sound CPU */
	public static ReadHandlerPtr multiply_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return (multiply_reg[0] * multiply_reg[1]) & 0xFF;
	} };
	public static WriteHandlerPtr multiply_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		multiply_reg[offset] = data;
	} };
	
	
	/*	K007232 registers reminder:
	
	[Ch A]	[Ch B]		[Meaning]
	00		06			address step	(low  byte)
	01		07			address step	(high byte, max 1)
	02		08			sample address	(low  byte)
	03		09			sample address	(mid  byte)
	04		0a			sample address	(high byte, max 1 . max rom size: $20000)
	05		0b			Reading this byte triggers the sample
	
	[Ch A & B]
	0c					volume
	0d					play sample once or looped (2 channels . 2 bits (0&1))
	
	** sample playing ends when a byte with bit 7 set is reached **/
	
	public static WriteHandlerPtr wecleman_K007232_bank_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		K007232_bankswitch(0, memory_region(REGION_SOUND1),
							  memory_region((data & 1)!=0 ? REGION_SOUND1 : REGION_SOUND2) );
	} };
	
	static MemoryReadAddress wecleman_sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM					),	// ROM
		new MemoryReadAddress( 0x8000, 0x83ff, MRA_RAM					),	// RAM
		new MemoryReadAddress( 0x9000, 0x9000, multiply_r				),	// Protection
		new MemoryReadAddress( 0xa000, 0xa000, soundlatch_r				),	// From main CPU
		new MemoryReadAddress( 0xb000, 0xb00d, K007232_read_port_0_r		),	// K007232 (Reading offset 5/b triggers the sample)
		new MemoryReadAddress( 0xc001, 0xc001, YM2151_status_port_0_r	),	// YM2151
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress wecleman_sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM					),	// ROM
		new MemoryWriteAddress( 0x8000, 0x83ff, MWA_RAM					),	// RAM
	//	new MemoryWriteAddress( 0x8500, 0x8500, MWA_NOP					),	// incresed with speed (global volume)?
		new MemoryWriteAddress( 0x9000, 0x9001, multiply_w				),	// Protection
	//	new MemoryWriteAddress( 0x9006, 0x9006, MWA_NOP					),	// ?
		new MemoryWriteAddress( 0xb000, 0xb00d, K007232_write_port_0_w	),	// K007232
		new MemoryWriteAddress( 0xc000, 0xc000, YM2151_register_port_0_w	),	// YM2151
		new MemoryWriteAddress( 0xc001, 0xc001, YM2151_data_port_0_w		),
		new MemoryWriteAddress( 0xf000, 0xf000, wecleman_K007232_bank_w	),	// Samples banking
		new MemoryWriteAddress( -1 )
	};
	
	
	
	
	static K007232_interface hotchase_k007232_interface = new K007232_interface
	(
		3,
		new int[]{ REGION_SOUND1, REGION_SOUND2, REGION_SOUND3 },
		new int[]{ K007232_VOL( 33,MIXER_PAN_CENTER, 33,MIXER_PAN_CENTER ),
		  K007232_VOL( 33,MIXER_PAN_LEFT,   33,MIXER_PAN_RIGHT  ),
		  K007232_VOL( 33,MIXER_PAN_LEFT,   33,MIXER_PAN_RIGHT  ) },
		new portwritehandlerPtr[] { null,null,null }
	);
	
	public static WriteHandlerPtr hotchase_sound_control_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int[] reg=new int[8];
	
		reg[offset] = data;
	
		switch (offset)
		{
			case 0x0:	/* Change volume of voice A (l&r speaker at once) */
			case 0x2:	/* for 3 chips.. */
			case 0x4:
									// chip, channel (l/r), volA, volB
				K007232_set_volume( offset / 2,	0, (data >> 4) * 0x11, (reg[offset^1] >> 4) * 0x11);
				K007232_set_volume( offset / 2,	1, (data & 15) * 0x11, (reg[offset^1] & 15) * 0x11);
				break;
	
			case 0x1:	/* Change volume of voice B (l&r speaker at once) */
			case 0x3:	/* for 3 chips.. */
			case 0x5:
									// chip, channel (l/r), volA, volB
				K007232_set_volume( offset / 2,	0, (reg[offset^1] >> 4) * 0x11, (data >> 4) * 0x11);
				K007232_set_volume( offset / 2,	1, (reg[offset^1] & 15) * 0x11, (data & 15) * 0x11);
				break;
	
			case 0x06:	/* Bankswitch for chips 0 & 1 */
			{
				UBytePtr RAM0 = memory_region(hotchase_k007232_interface.bank[0]);
				UBytePtr RAM1 = memory_region(hotchase_k007232_interface.bank[1]);
	
				int bank0_a = (data >> 1) & 1;
				int bank1_a = (data >> 2) & 1;
				int bank0_b = (data >> 3) & 1;
				int bank1_b = (data >> 4) & 1;
				// bit 6: chip 2 - ch0 ?
				// bit 7: chip 2 - ch1 ?
	
				K007232_bankswitch(0, new UBytePtr(RAM0, bank0_a*0x20000), new UBytePtr(RAM0, bank0_b*0x20000));
				K007232_bankswitch(1, new UBytePtr(RAM1, bank1_a*0x20000), new UBytePtr(RAM1, bank1_b*0x20000));
			}
			break;
	
			case 0x07:	/* Bankswitch for chip 2 */
			{
				UBytePtr RAM2 = memory_region(hotchase_k007232_interface.bank[2]);
	
				int bank2_a = (data >> 0) & 7;
				int bank2_b = (data >> 3) & 7;
	
				K007232_bankswitch(2, new UBytePtr(RAM2, bank2_a*0x20000), new UBytePtr(RAM2, bank2_b*0x20000));
			}
			break;
		}
	} };
	
	
	/* Read and write handlers for one K007232 chip:
	   even and odd register are mapped swapped */
	
	public static ReadHandlerPtr hotchase_K007232_0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K007232_read_port_0_r.handler(offset ^ 1);
	} };
	public static WriteHandlerPtr hotchase_K007232_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		K007232_write_port_0_w.handler(offset ^ 1, data);
	} };
	
	/* 3 x K007232 */
	//HOTCHASE_K007232_RW(0)
	//HOTCHASE_K007232_RW(1)
        public static ReadHandlerPtr hotchase_K007232_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K007232_read_port_1_r.handler(offset ^ 1);
	} };
	public static WriteHandlerPtr hotchase_K007232_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		K007232_write_port_1_w.handler(offset ^ 1, data);
	} };
	
        //HOTCHASE_K007232_RW(2)
	public static ReadHandlerPtr hotchase_K007232_2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K007232_read_port_2_r.handler(offset ^ 1);
	} };
	public static WriteHandlerPtr hotchase_K007232_2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		K007232_write_port_2_w.handler(offset ^ 1, data);
	} };
	
	
	static MemoryReadAddress hotchase_sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x07ff, MRA_RAM					),	// RAM
		new MemoryReadAddress( 0x1000, 0x100d, hotchase_K007232_0_r		),	// 3 x  K007232
		new MemoryReadAddress( 0x2000, 0x200d, hotchase_K007232_1_r		),
		new MemoryReadAddress( 0x3000, 0x300d, hotchase_K007232_2_r		),
		new MemoryReadAddress( 0x6000, 0x6000, soundlatch_r				),	// From main CPU (Read on IRQ)
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM					),	// ROM
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress hotchase_sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x07ff, MWA_RAM					),	// RAM
		new MemoryWriteAddress( 0x1000, 0x100d, hotchase_K007232_0_w		),	// 3 x K007232
		new MemoryWriteAddress( 0x2000, 0x200d, hotchase_K007232_1_w		),
		new MemoryWriteAddress( 0x3000, 0x300d, hotchase_K007232_2_w		),
		new MemoryWriteAddress( 0x4000, 0x4007, hotchase_sound_control_w	),	// Sound volume, banking, etc.
		new MemoryWriteAddress( 0x5000, 0x5000, MWA_NOP					),	// ? (written with 0 on IRQ, 1 on FIRQ)
		new MemoryWriteAddress( 0x7000, 0x7000, MWA_NOP					),	// Command acknowledge ?
		new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM					),	// ROM
		new MemoryWriteAddress( -1 )
	};
	
	
	
	
	
	/***************************************************************************
	
								Input Ports
	
	***************************************************************************/
	
	// Fake input port to read the status of the four buttons
	// Used to implement both the accelerator and the shift using 2 buttons
	
	static void BUTTONS_STATUS() {
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_BUTTON3 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_BUTTON4 );
        }
	
	
	
	static void DRIVING_WHEEL() {
	 PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X | IPF_CENTER, 50, 5, 0, 0xff);
        }
	
	
	
	static void CONTROLS_AND_COINS(int _default_){
		PORT_BIT(  0x01, _default_, IPT_COIN1   );
		PORT_BIT(  0x02, _default_, IPT_COIN2   );
		PORT_BITX( 0x04, _default_, IPT_SERVICE, DEF_STR( "Service_Mode"), KEYCODE_F2, IP_JOY_NONE );
		PORT_BIT(  0x08, _default_, IPT_COIN3   );				/* Called "service" */
		PORT_BIT(  0x10, _default_, IPT_START1  );				/* Start */
	/*	PORT_BIT(  0x20, _default_, IPT_BUTTON3 | IPF_TOGGLE );*/	/* Shift (we handle this with 2 buttons) */
		PORT_BIT(  0x40, _default_, IPT_BUTTON2 );				/* Brake */
		PORT_BIT(  0x80, _default_, IPT_UNKNOWN );				/* ? */
        }
	
	/***************************************************************************
									WEC Le Mans 24
	***************************************************************************/
	
	static InputPortPtr input_ports_wecleman = new InputPortPtr(){ public void handler() { 
	
		PORT_START();       /* IN0 - Controls and Coins - $140011.b */
		CONTROLS_AND_COINS(IP_ACTIVE_HIGH);
	
		PORT_START();       /* IN1 - Motor? - $140013.b */
		PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_UNKNOWN );// ? right sw
		PORT_BIT( 0x02, IP_ACTIVE_LOW,  IPT_UNKNOWN );// ? left  sw
		PORT_BIT( 0x04, IP_ACTIVE_LOW,  IPT_UNKNOWN );// ? thermo
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );// ? from sound cpu ?
		PORT_BIT( 0x10, IP_ACTIVE_LOW,  IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW,  IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW,  IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN );
	
		PORT_START(); 	/* IN2 - DSW A (Coinage) - $140015.b */
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
		PORT_DIPSETTING(    0x00, DEF_STR( "Unknown") );
	
		PORT_START(); 	/* IN3 - DSW B (options) - $140017.b */
		PORT_DIPNAME( 0x01, 0x01, "Speed Unit" );
		PORT_DIPSETTING(    0x01, "Km/h" );
		PORT_DIPSETTING(    0x00, "mph" );
		PORT_DIPNAME( 0x02, 0x02, "Unknown B-1" );// single
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, "Unknown B-2" );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x18, "Easy" );		// 66 seconds at the start
		PORT_DIPSETTING(    0x10, "Normal" );	// 64
		PORT_DIPSETTING(    0x08, "Hard" );		// 62
		PORT_DIPSETTING(    0x00, "Hardest" );	// 60
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "Unknown B-6" );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "Unknown B-7" );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* IN4 - Fake input port - Buttons status */
		BUTTONS_STATUS();
	
		PORT_START(); 	/* IN5 - Driving Wheel - $140021.b (2) */
		DRIVING_WHEEL();
	
	INPUT_PORTS_END(); }}; 
	
	
	
	
	
	
	
	
	
	
	
	
	
	/***************************************************************************
									Hot Chase
	***************************************************************************/
	
	static InputPortPtr input_ports_hotchase = new InputPortPtr(){ public void handler() { 
	
		PORT_START();       /* IN0 - Controls and Coins - $140011.b */
		CONTROLS_AND_COINS(IP_ACTIVE_LOW);
	
		PORT_START();       /* IN1 - Motor? - $140013.b */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );// ? right sw
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );// ? left  sw
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );// ? thermo
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );// ? from sound cpu ?
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN2 - DSW 2 (options) - $140015.b */
		PORT_DIPNAME( 0x01, 0x01, "Unknown 2-0" );// single
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Unknown 2-1" );// single (wheel related)
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, "Unknown 2-2" );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x18, 0x18, "Unknown 2-3&4" );
		PORT_DIPSETTING(    0x18, "0" );
		PORT_DIPSETTING(    0x10, "4" );
		PORT_DIPSETTING(    0x08, "8" );
		PORT_DIPSETTING(    0x00, "c" );
		PORT_DIPNAME( 0x20, 0x20, "Unknown 2-5" );// single
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	/* wheel <. brake ; accel . start */
		PORT_DIPNAME( 0x40, 0x40, "Unknown 2-6" );// single (wheel<.brake)
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "Unknown 2-7" );// single
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* IN3 - DSW 1 (Coinage) - $140017.b */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "5C_3C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x20, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "5C_3C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x00, "1 Coin/99 Credits" );
	//	PORT_DIPSETTING(    0x40, "0C_0C" );// Coin B insertion freezes the game!
	
		PORT_START(); 	/* IN4 - Fake input port - Buttons status */
		BUTTONS_STATUS();
	
		PORT_START(); 	/* IN5 - Driving Wheel - $140021.b (2) */
		DRIVING_WHEEL();
	
	INPUT_PORTS_END(); }}; 
	
	
	
	
	
	
	
	
	
	/***************************************************************************
	
									Graphics Layout
	
	***************************************************************************/
	
	
	/***************************************************************************
									WEC Le Mans 24
	***************************************************************************/
	
	static GfxLayout wecleman_bg_layout = new GfxLayout
	(
		8,8,
		8*0x8000*3/(8*8*3),
		3,
		new int[] { 0,0x8000*8,0x8000*8*2 },
		new int[] {0,7,6,5,4,3,2,1},
		new int[] {0*8,1*8,2*8,3*8,4*8,5*8,6*8,7*8},
		8*8
	);
	
	/* We draw the road, made of 512 pixel lines, using 64x1 tiles */
	static GfxLayout wecleman_road_layout = new GfxLayout
	(
		64,1,
		8*0x4000*3/(64*1*3),
		3,
		new int[] { 0x4000*8*2,0x4000*8*1,0x4000*8*0 },
		new int[] {0,7,6,5,4,3,2,1,
		 8,15,14,13,12,11,10,9,
		 16,23,22,21,20,19,18,17,
		 24,31,30,29,28,27,26,25,
	
		 0+32,7+32,6+32,5+32,4+32,3+32,2+32,1+32,
		 8+32,15+32,14+32,13+32,12+32,11+32,10+32,9+32,
		 16+32,23+32,22+32,21+32,20+32,19+32,18+32,17+32,
		 24+32,31+32,30+32,29+32,28+32,27+32,26+32,25+32},
		new int[] {0},
		64*1
	);
	
	
	static GfxDecodeInfo wecleman_gfxdecodeinfo[] =
	{
	//	  REGION_GFX1 holds sprite, which are not decoded here
		new GfxDecodeInfo( REGION_GFX2, 0, wecleman_bg_layout,   0, 2048/8 ), // [0] bg + fg + txt
		new GfxDecodeInfo( REGION_GFX3, 0, wecleman_road_layout, 0, 2048/8 ), // [1] road
		new GfxDecodeInfo( -1 )
	};
	
	
	
	/***************************************************************************
									Hot Chase
	***************************************************************************/
	
	
	/* We draw the road, made of 512 pixel lines, using 64x1 tiles */
	static GfxLayout hotchase_road_layout = new GfxLayout
	(
		64,1,
		8*0x20000/(64*1*4),
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] {0*4,1*4,2*4,3*4,4*4,5*4,6*4,7*4,
		 8*4,9*4,10*4,11*4,12*4,13*4,14*4,15*4,
		 16*4,17*4,18*4,19*4,20*4,21*4,22*4,23*4,
		 24*4,25*4,26*4,27*4,28*4,29*4,30*4,31*4,
	
		 32*4,33*4,34*4,35*4,36*4,37*4,38*4,39*4,
		 40*4,41*4,42*4,43*4,44*4,45*4,46*4,47*4,
		 48*4,49*4,50*4,51*4,52*4,53*4,54*4,55*4,
		 56*4,57*4,58*4,59*4,60*4,61*4,62*4,63*4},
		new int[] {0},
		64*1*4
	);
	
	
	static GfxDecodeInfo hotchase_gfxdecodeinfo[] =
	{
	//	REGION_GFX1 holds sprite, which are not decoded here
	//	REGION_GFX2 and 3 are for the 051316
		new GfxDecodeInfo( REGION_GFX4, 0, hotchase_road_layout, 0x70*16, 16 ),	// road
		new GfxDecodeInfo( -1 )
	};
	
	
	
	
	/***************************************************************************
									WEC Le Mans 24
	***************************************************************************/
	
	
	
	
	public static InterruptHandlerPtr wecleman_interrupt = new InterruptHandlerPtr() { public int handler() 
	{
		if (cpu_getiloops() == 0)	return 4;	/* once */
		else						return 5;	/* to read input ports */
	} };
	
	
	static YM2151interface ym2151_interface = new YM2151interface
	(
		1,
		3579545,	/* same as sound cpu */
		new int[] { 80 },
		new WriteYmHandlerPtr[] { null  }
	);
	
	
	
	static K007232_interface wecleman_k007232_interface = new K007232_interface
	(
		1,
		new int[]{ REGION_SOUND1 },	/* but the 2 channels use different ROMs !*/
		new int[]{ K007232_VOL( 20,MIXER_PAN_LEFT, 20,MIXER_PAN_RIGHT ) },
		new portwritehandlerPtr[] {null}
	);
	
	
	
	public static InitMachineHandlerPtr wecleman_init_machine = new InitMachineHandlerPtr() { public void handler() 
	{
		K007232_bankswitch(0,	memory_region(REGION_SOUND1), /* the 2 channels use different ROMs */
								memory_region(REGION_SOUND2) );
	} };
	
	
	static MachineDriver machine_driver_wecleman = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				10000000,		/* Schems show 10MHz */
				wecleman_readmem,wecleman_writemem,null,null,
				wecleman_interrupt, 5 + 1	/* in order to read the inputs once per frame */
			),
			new MachineCPU(
				CPU_M68000,
				10000000,		/* Schems show 10MHz */
				wecleman_sub_readmem,wecleman_sub_writemem,null,null,
				ignore_interrupt,1		/* lev 4 irq generated by main CPU */
			),
			new MachineCPU(
	/* Schems: can be reset, no nmi, soundlatch, 3.58MHz */
				CPU_Z80 | CPU_AUDIO_CPU,
				3579545,
				wecleman_sound_readmem,wecleman_sound_writemem,null,null,
				ignore_interrupt,1 /* irq caused by main cpu */
			),
		},
		60,DEFAULT_60HZ_VBLANK_DURATION,
		1,
		wecleman_init_machine,
	
		/* video hardware */
		320, 224, new rectangle( 0, 320-1, 0, 224-1 ),
	
		wecleman_gfxdecodeinfo,
		2048, 2048,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		wecleman_vh_start,
		null,
		wecleman_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			),
			new MachineSound(
				SOUND_K007232,
				wecleman_k007232_interface
			),
		}
	);
	
	
	
	
	
	/***************************************************************************
									Hot Chase
	***************************************************************************/
	
	
	
	public static InitMachineHandlerPtr hotchase_init_machine = new InitMachineHandlerPtr() { public void handler() 		{						} };
	public static InterruptHandlerPtr hotchase_interrupt = new InterruptHandlerPtr() { public int handler() 			{return 4;				} };
	public static InterruptHandlerPtr hotchase_sound_interrupt = new InterruptHandlerPtr() { public int handler() 		{return M6809_INT_FIRQ;	} };
	
	static MachineDriver machine_driver_hotchase = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				10000000,		/* 10 MHz - PCB is drawn in one set's readme */
				hotchase_readmem,hotchase_writemem,null,null,
				hotchase_interrupt,1
			),
			new MachineCPU(
				CPU_M68000,
				10000000,		/* 10 MHz - PCB is drawn in one set's readme */
				hotchase_sub_readmem,hotchase_sub_writemem,null,null,
				ignore_interrupt,1		/* lev 4 irq generated by main CPU */
			),
			new MachineCPU(
				CPU_M6809 | CPU_AUDIO_CPU,
				3579545,		/* 3.579 MHz - PCB is drawn in one set's readme */
				hotchase_sound_readmem,hotchase_sound_writemem,null,null,
				hotchase_sound_interrupt,8 /* FIRQ, while IRQ is caused by main cpu */
											/* Amuse: every 2 ms */
			),
		},
		60,DEFAULT_60HZ_VBLANK_DURATION,
		1,
		hotchase_init_machine,
	
		/* video hardware */
		320, 224, new rectangle( 0, 320-1, 0, 224-1 ),
	
		hotchase_gfxdecodeinfo,
		2048, 2048,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		hotchase_vh_start,
		hotchase_vh_stop,
		hotchase_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_K007232,
				hotchase_k007232_interface
			)
		}
	);
	
	
	
	/***************************************************************************
	
									ROMs Loading
	
	***************************************************************************/
	
	
	
	/***************************************************************************
									WEC Le Mans 24
	***************************************************************************/
	
	static RomLoadHandlerPtr rom_wecleman = new RomLoadHandlerPtr(){ public void handler(){ 
	
		ROM_REGION( 0x40000, REGION_CPU1 );	/* Main CPU Code */
		ROM_LOAD_EVEN( "602f08.17h", 0x00000, 0x10000, 0x493b79d3 );
		ROM_LOAD_ODD ( "602f11.23h", 0x00000, 0x10000, 0x6bb4f1fa );
		ROM_LOAD_EVEN( "602a09.18h", 0x20000, 0x10000, 0x8a9d756f );
		ROM_LOAD_ODD ( "602a10.22h", 0x20000, 0x10000, 0x569f5001 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );	/* Sub CPU Code */
		ROM_LOAD_EVEN( "602a06.18a", 0x00000, 0x08000, 0xe12c0d11 );
		ROM_LOAD_ODD(  "602a07.20a", 0x00000, 0x08000, 0x47968e51 );
	
		ROM_REGION( 0x10000, REGION_CPU3 );	/* Sound CPU Code */
		ROM_LOAD( "602a01.6d",  0x00000, 0x08000, 0xdeafe5f1 );
	
		ROM_REGION( 0x200000 * 2, REGION_GFX1 );/* x2, do not dispose */
		ROM_LOAD( "602a25.12e", 0x000000, 0x20000, 0x0eacf1f9 );// zooming sprites
		ROM_LOAD( "602a26.14e", 0x020000, 0x20000, 0x2182edaf );
		ROM_LOAD( "602a27.15e", 0x040000, 0x20000, 0xb22f08e9 );
		ROM_LOAD( "602a28.17e", 0x060000, 0x20000, 0x5f6741fa );
		ROM_LOAD( "602a21.6e",  0x080000, 0x20000, 0x8cab34f1 );
		ROM_LOAD( "602a22.7e",  0x0a0000, 0x20000, 0xe40303cb );
		ROM_LOAD( "602a23.9e",  0x0c0000, 0x20000, 0x75077681 );
		ROM_LOAD( "602a24.10e", 0x0e0000, 0x20000, 0x583dadad );
		ROM_LOAD( "602a17.12c", 0x100000, 0x20000, 0x31612199 );
		ROM_LOAD( "602a18.14c", 0x120000, 0x20000, 0x3f061a67 );
		ROM_LOAD( "602a19.15c", 0x140000, 0x20000, 0x5915dbc5 );
		ROM_LOAD( "602a20.17c", 0x160000, 0x20000, 0xf87e4ef5 );
		ROM_LOAD( "602a13.6c",  0x180000, 0x20000, 0x5d3589b8 );
		ROM_LOAD( "602a14.7c",  0x1a0000, 0x20000, 0xe3a75f6c );
		ROM_LOAD( "602a15.9c",  0x1c0000, 0x20000, 0x0d493c9f );
		ROM_LOAD( "602a16.10c", 0x1e0000, 0x20000, 0xb08770b3 );
	
		ROM_REGION( 0x18000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "602a31.26g", 0x000000, 0x08000, 0x01fa40dd );// layers
		ROM_LOAD( "602a30.24g", 0x008000, 0x08000, 0xbe5c4138 );
		ROM_LOAD( "602a29.23g", 0x010000, 0x08000, 0xf1a8d33e );
	
		ROM_REGION( 0x0c000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "602a04.11e", 0x000000, 0x08000, 0xade9f359 );// road
		ROM_LOAD( "602a05.13e", 0x008000, 0x04000, 0xf22b7f2b );
	
		ROM_REGION( 0x20000, REGION_SOUND1 );/* Samples (Channel A) */
		ROM_LOAD( "602a03.10a", 0x00000, 0x20000, 0x31392b01 );
	
		ROM_REGION( 0x20000, REGION_SOUND2 );/* Samples (Channel B) */
		ROM_LOAD( "602a02.8a",  0x00000, 0x20000, 0xe2be10ae );
	
		ROM_REGION( 0x04000, REGION_USER1 );
		ROM_LOAD( "602a12.1a",  0x000000, 0x04000, 0x77b9383d );// ??
	
	ROM_END(); }}; 
	
	
	
	static void wecleman_unpack_sprites()
	{
		int region		=	REGION_GFX1;	// sprites
	
		int len	=	memory_region_length(region);
		UBytePtr src		=	new UBytePtr(memory_region(region), len / 2 - 1);
		UBytePtr dst		=	new UBytePtr(memory_region(region), len - 1);
	
		while(dst.offset > src.offset)
		{
			int data = src.read();
                        src.dec();
			if( (data&0xf0) == 0xf0 ) data &= 0x0f;
			if( (data&0x0f) == 0x0f ) data &= 0xf0;
			dst.write( data & 0xF); dst.dec();	dst.write( data >> 4); dst.dec();
		}
	}
	
	
	
	/* Unpack sprites data and do some patching */
	public static InitDriverHandlerPtr init_wecleman = new InitDriverHandlerPtr() { public void handler() 
	{
		UBytePtr RAM;
		int i;
	
	/* Optional code patches */
	
		/* Main CPU patches */
		RAM = memory_region(REGION_CPU1);
	//	WRITE_WORD (&RAM[0x08c2],0x601e);	// faster self test
	
		/* Sub CPU patches */
		RAM = memory_region(REGION_CPU2);
	
		/* Sound CPU patches */
		RAM = memory_region(REGION_CPU3);
	
	
	/* Decode GFX Roms - Compensate for the address lines scrambling */
	
		/*	Sprites - decrypting the sprites nearly KILLED ME!
			It's been the main cause of the delay of this driver ...
			I hope you'll appreciate this effort!	*/
	
		/* let's swap even and odd *pixels* of the sprites */
		RAM = memory_region(REGION_GFX1);
		for (i = 0; i < memory_region_length(REGION_GFX1); i ++)
		{
			int x = RAM.read(i);
			/* TODO: could be wrong, colors have to be fixed.       */
			/* The only certain thing is that 87 must convert to f0 */
			/* otherwise stray lines appear, made of pens 7 & 8     */
			x = ((x & 0x07) << 5) | ((x & 0xf8) >> 3);
			RAM.write(i, x);
		}
	
		BITSWAP(i, new UBytePtr(memory_region(REGION_GFX1)), memory_region_length(REGION_GFX1),
				0,1,20,19,18,17,14,9,16,6,4,7,8,15,10,11,13,5,12,3,2);
	
		/* Now we can unpack each nibble of the sprites into a pixel (one byte) */
		wecleman_unpack_sprites();
	
	
	
		/* Bg & Fg & Txt */
		BITSWAP(i, memory_region(REGION_GFX2), memory_region_length(REGION_GFX2),
				20,19,18,17,16,15,12,7,14,4,2,5,6,13,8,9,11,3,10,1,0);
	
	
	
		/* Road */
		BITSWAP(i, memory_region(REGION_GFX3), memory_region_length(REGION_GFX3),
				20,19,18,17,16,15,14,7,12,4,2,5,6,13,8,9,11,3,10,1,0);
	} };
	
	
	
	
	
	/***************************************************************************
									Hot Chase
	***************************************************************************/
	
	
	static RomLoadHandlerPtr rom_hotchase = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );		/* Main Code */
		ROM_LOAD_EVEN( "763k05", 0x000000, 0x010000, 0xf34fef0b );
		ROM_LOAD_ODD ( "763k04", 0x000000, 0x010000, 0x60f73178 );
		ROM_LOAD_EVEN( "763k03", 0x020000, 0x010000, 0x28e3a444 );
		ROM_LOAD_ODD ( "763k02", 0x020000, 0x010000, 0x9510f961 );
	
		ROM_REGION( 0x20000, REGION_CPU2 );		/* Sub Code */
		ROM_LOAD_EVEN( "763k07", 0x000000, 0x010000, 0xae12fa90 );
		ROM_LOAD_ODD ( "763k06", 0x000000, 0x010000, 0xb77e0c07 );
	
		ROM_REGION( 0x10000, REGION_CPU3 );		/* Sound Code */
		ROM_LOAD( "763f01", 0x8000, 0x8000, 0x4fddd061 );
	
		ROM_REGION( 0x300000 * 2, REGION_GFX1 );/* x2, do not dispose */
		ROM_LOAD( "763e17", 0x000000, 0x080000, 0x8db4e0aa );// zooming sprites
		ROM_LOAD( "763e20", 0x080000, 0x080000, 0xa22c6fce );
		ROM_LOAD( "763e18", 0x100000, 0x080000, 0x50920d01 );
		ROM_LOAD( "763e21", 0x180000, 0x080000, 0x77e0e93e );
		ROM_LOAD( "763e19", 0x200000, 0x080000, 0xa2622e56 );
		ROM_LOAD( "763e22", 0x280000, 0x080000, 0x967c49d1 );
	
		ROM_REGION( 0x20000, REGION_GFX2 );
		ROM_LOAD( "763e14", 0x000000, 0x020000, 0x60392aa1 );// bg
	
		ROM_REGION( 0x10000, REGION_GFX3 );
		ROM_LOAD( "763a13", 0x000000, 0x010000, 0x8bed8e0d );// fg (patched)
	
		ROM_REGION( 0x20000, REGION_GFX4 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "763e15", 0x000000, 0x020000, 0x7110aa43 );// road
	
		ROM_REGION( 0x40000, REGION_SOUND1 );	/* Samples */
		ROM_LOAD( "763e11", 0x000000, 0x040000, 0x9d99a5a7 );// 2 banks
	
		ROM_REGION( 0x40000, REGION_SOUND2 );	/* Samples */
		ROM_LOAD( "763e10", 0x000000, 0x040000, 0xca409210 );// 2 banks
	
		ROM_REGION( 0x100000, REGION_SOUND3 );	/* Samples */
		ROM_LOAD( "763e08", 0x000000, 0x080000, 0x054a9a63 );// 4 banks
		ROM_LOAD( "763e09", 0x080000, 0x080000, 0xc39857db );// 4 banks
	
		ROM_REGION( 0x08000, REGION_USER1 );
		ROM_LOAD( "763a12", 0x000000, 0x008000, 0x05f1e553 );// ??
	ROM_END(); }}; 
	
	
	
	
	/*	Important: you must leave extra space when listing sprite ROMs
		in a ROM module definition.  This routine unpacks each sprite nibble
		into a byte, doubling the memory consumption. */
	
	static void hotchase_sprite_decode( int num_banks, int bank_size )
	{
		UBytePtr base, temp;
		int i;
	
		base = memory_region(REGION_GFX1);	// sprites
		temp = new UBytePtr( bank_size );
		if( temp==null ) return;
	
		for( i = num_banks; i >0; i-- ){
			UBytePtr finish	= new UBytePtr(base, 2*bank_size*i);
			UBytePtr dest 	= new UBytePtr(finish, - 2*bank_size);
	
			UBytePtr p1 = temp;
			UBytePtr p2 = new UBytePtr(temp, bank_size/2);
	
			int data;
	
			memcpy (temp, new UBytePtr(base, bank_size*(i-1)), bank_size);
	
			do {
				data = p1.readinc();
				if( (data&0xf0) == 0xf0 ) data &= 0x0f;
				if( (data&0x0f) == 0x0f ) data &= 0xf0;
				dest.writeinc( data >> 4 );
				dest.writeinc( data & 0xF );
				data = p1.readinc();
				if( (data&0xf0) == 0xf0 ) data &= 0x0f;
				if( (data&0x0f) == 0x0f ) data &= 0xf0;
				dest.writeinc( data >> 4 );
				dest.writeinc( data & 0xF );
	
	
				data = p2.readinc();
				if( (data&0xf0) == 0xf0 ) data &= 0x0f;
				if( (data&0x0f) == 0x0f ) data &= 0xf0;
				dest.writeinc( data >> 4 );
				dest.writeinc( data & 0xF );
				data = p2.readinc();
				if( (data&0xf0) == 0xf0 ) data &= 0x0f;
				if( (data&0x0f) == 0x0f ) data &= 0xf0;
				dest.writeinc( data >> 4 );
				dest.writeinc( data & 0xF );
			} while( dest.offset<finish.offset );
		}
		temp = null;
	}
	
	
	
	
	/* Unpack sprites data and do some patching */
	public static InitDriverHandlerPtr init_hotchase = new InitDriverHandlerPtr() { public void handler() 
	{
		UBytePtr RAM;
		int i;
	
	/* Optional code patches */
	
		/* Main CPU patches */
		RAM = memory_region(REGION_CPU1);
		RAM.WRITE_WORD (0x1140,0x0015);	RAM.WRITE_WORD (0x195c,0x601A);	// faster self test
	
		/* Sub CPU patches */
		RAM = memory_region(REGION_CPU2);
	
		/* Sound CPU patches */
		RAM = memory_region(REGION_CPU3);
	
	
	/* Decode GFX Roms */
	
		/* Let's swap even and odd bytes of the sprites gfx roms */
		RAM = memory_region(REGION_GFX1);
		for (i = 0; i < memory_region_length(REGION_GFX1); i += 2)
		{
			int x = RAM.read(i);
			RAM.write(i, RAM.read(i+1));
			RAM.write(i+1, x);
		}
	
		/* Now we can unpack each nibble of the sprites into a pixel (one byte) */
		hotchase_sprite_decode(3,0x80000*2);	// num banks, bank len
	
	
		/* Let's copy the second half of the fg layer gfx (charset) over the first */
		RAM = memory_region(REGION_GFX3);
		memcpy(new UBytePtr(RAM, 0), new UBytePtr(RAM, 0x10000/2), 0x10000/2);
	
	} };
	
	
	
	/***************************************************************************
	
									Game driver(s)
	
	***************************************************************************/
	
	public static GameDriver driver_wecleman	   = new GameDriver("1986"	,"wecleman"	,"wecleman.java"	,rom_wecleman,null	,machine_driver_wecleman	,input_ports_wecleman	,init_wecleman	,ROT0	,	"Konami", "WEC Le Mans 24", GAME_WRONG_COLORS );
	public static GameDriver driver_hotchase	   = new GameDriver("1988"	,"hotchase"	,"wecleman.java"	,rom_hotchase,null	,machine_driver_hotchase	,input_ports_hotchase	,init_hotchase	,ROT0	,	"Konami", "Hot Chase" );
}
