/***************************************************************************

	Victory video system

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"


/* globally-accessible storage */
UINT8 *victory_charram;

/* local allocated storage */
static struct osd_bitmap *bgbitmap;
static struct osd_bitmap *fgbitmap;
static UINT8 *bgdirty;
static UINT8 *chardirty;
static UINT8 *scandirty;
static UINT8 *rram, *gram, *bram;

/* interrupt, collision, and control states */
static UINT8 vblank_irq;
static UINT8 fgcoll, fgcollx, fgcolly;
static UINT8 bgcoll, bgcollx, bgcolly;
static UINT8 scrollx, scrolly;
static UINT8 update_complete;
static UINT8 video_control;

/* microcode state */
static struct
{
	UINT16	i;
	UINT16	pc;
	UINT8	r,g,b;
	UINT8	x,xp,y,yp;
	UINT8	cmd,cmdlo;
	void *	timer;
	double	endtime;
} micro;


/* number of ticks per clock of the microcode state machine   */
/* from what I can tell, this should be divided by 32, not 8  */
/* but the interrupt test does some precise timing, and fails */
/* if it's not 8 */
#define MICRO_STATE_CLOCK_PERIOD	TIME_IN_HZ(11827000/8)


/* debugging constants */
#define LOG_MICROCODE		0
#define LOG_COLLISION		0


/* function prototypes */
void victory_vh_stop(void);

static int command2(void);
static int command3(void);
static int command4(void);
static int command5(void);
static int command6(void);
static int command7(void);



/*************************************
 *
 *	Initialize the video system
 *
 *************************************/

int victory_vh_start(void)
{
	/* allocate bitmapram */
	rram = malloc(0x4000);
	gram = malloc(0x4000);
	bram = malloc(0x4000);

	/* allocate bitmaps */
	bgbitmap = bitmap_alloc(32 * 8, 32 * 8);
	fgbitmap = bitmap_alloc(32 * 8, 32 * 8);

	/* allocate dirty maps */
	bgdirty = malloc(32 * 32);
	chardirty = malloc(256);
	scandirty = malloc(512);

	/* fail if something went wrong */
	if (!rram || !gram || !bram || !bgbitmap || !fgbitmap || !bgdirty || !chardirty || !scandirty)
	{
		victory_vh_stop();
		return 1;
	}

	/* mark everything dirty */
	memset(bgdirty, 1, 32 * 32);
	memset(chardirty, 1, 256);
	memset(scandirty, 1, 512);

	/* reset globals */
	vblank_irq = 0;
	fgcoll = fgcollx = fgcolly = 0;
	bgcoll = bgcollx = bgcolly = 0;
	scrollx = scrolly = 0;
	update_complete = 0;
	video_control = 0;
	memset(&micro, 0, sizeof(micro));

	return 0;
}



/*************************************
 *
 *	Tear down the video system
 *
 *************************************/

void victory_vh_stop(void)
{
	/* free dirty maps */
	if (bgdirty)
		free(bgdirty);
	bgdirty = NULL;
	if (chardirty)
		free(chardirty);
	chardirty = NULL;
	if (scandirty)
		free(scandirty);
	scandirty = NULL;

	/* free bitmaps */
	if (bgbitmap)
		bitmap_free(bgbitmap);
	bgbitmap = NULL;
	if (fgbitmap)
		bitmap_free(fgbitmap);
	fgbitmap = NULL;

	/* free bitmapram */
	if (rram)
		free(rram);
	rram = NULL;
	if (gram)
		free(gram);
	gram = NULL;
	if (bram)
		free(bram);
	bram = NULL;
}



/*************************************
 *
 *	Interrupt generation
 *
 *************************************/

void victory_update_irq(void)
{
	if (vblank_irq || fgcoll || (bgcoll && (video_control & 0x20)))
		cpu_set_irq_line(0, 0, ASSERT_LINE);
	else
		cpu_set_irq_line(0, 0, CLEAR_LINE);
}


int victory_vblank_interrupt(void)
{
	vblank_irq = 1;
	victory_update_irq();
	logerror("------------- VBLANK ----------------\n");
	return ignore_interrupt();
}



/*************************************
 *
 *	Video RAM write
 *
 *************************************/

WRITE_HANDLER( victory_videoram_w )
{
	if (videoram[offset] != data)
	{
		videoram[offset] = data;
		bgdirty[offset] = 1;
	}
}



/*************************************
 *
 *	Character RAM write
 *
 *************************************/

WRITE_HANDLER( victory_charram_w )
{
	if (victory_charram[offset] != data)
	{
		victory_charram[offset] = data;
		chardirty[(offset / 8) % 256] = 1;
	}
}



/*************************************
 *
 *	Palette RAM write
 *
 *************************************/

WRITE_HANDLER( victory_paletteram_w )
{
	int red = ((offset & 0x80) >> 5) | ((data & 0xc0) >> 6);
	int blue = (data & 0x38) >> 3;
	int green = data & 0x07;

	/* shift up to 8 bits */
	red   = (red << 5)   | (red << 2)   | (red >> 1);
	green = (green << 5) | (green << 2) | (green >> 1);
	blue  = (blue << 5)  | (blue << 2)  | (blue >> 1);

	/* set the color */
	palette_change_color(offset & 0x3f, red, green, blue);
}



/*************************************
 *
 *	Video control read
 *
 *************************************/

READ_HANDLER( victory_video_control_r )
{
	int result = 0;

	switch (offset)
	{
		case 0:		/* 5XFIQ */
			result = fgcollx;
			if (LOG_COLLISION) logerror("%04X:5XFIQ read = %02X\n", cpu_getpreviouspc(), result);
			return result;

		case 1:		/* 5CLFIQ */
			result = fgcolly;
			if (fgcoll)
			{
				fgcoll = 0;
				victory_update_irq();
			}
			if (LOG_COLLISION) logerror("%04X:5CLFIQ read = %02X\n", cpu_getpreviouspc(), result);
			return result;

		case 2:		/* 5BACKX */
			result = bgcollx & 0xfc;
			if (LOG_COLLISION) logerror("%04X:5BACKX read = %02X\n", cpu_getpreviouspc(), result);
			return result;

		case 3:		/* 5BACKY */
			result = bgcolly;
			if (bgcoll)
			{
				bgcoll = 0;
				victory_update_irq();
			}
			if (LOG_COLLISION) logerror("%04X:5BACKY read = %02X\n", cpu_getpreviouspc(), result);
			return result;

		case 4:		/* 5STAT */
			// D7 = BUSY (9A1) -- microcode
			// D6 = 5FCIRQ (3B1)
			// D5 = 5VIRQ
			// D4 = 5BCIRQ (3B1)
			// D3 = SL256
			if (micro.timer && timer_timeelapsed(micro.timer) < micro.endtime)
				result |= 0x80;
			result |= (~fgcoll & 1) << 6;
			result |= (~vblank_irq & 1) << 5;
			result |= (~bgcoll & 1) << 4;
			result |= (cpu_getscanline() & 0x100) >> 5;
			if (LOG_COLLISION) logerror("%04X:5STAT read = %02X\n", cpu_getpreviouspc(), result);
			return result;

		default:
			logerror("%04X:victory_video_control_r(%02X)\n", cpu_getpreviouspc(), offset);
			break;
	}
	return 0;
}



/*************************************
 *
 *	Video control write
 *
 *************************************/

WRITE_HANDLER( victory_video_control_w )
{
	switch (offset)
	{
		case 0:		/* LOAD IL */
			if (LOG_MICROCODE) logerror("%04X:IL=%02X\n", cpu_getpreviouspc(), data);
			micro.i = (micro.i & 0xff00) | (data & 0x00ff);
			break;

		case 1:		/* LOAD IH */
			if (LOG_MICROCODE) logerror("%04X:IH=%02X\n", cpu_getpreviouspc(), data);
			micro.i = (micro.i & 0x00ff) | ((data << 8) & 0xff00);
			if (micro.cmdlo == 5)
			{
				if (LOG_MICROCODE) logerror("  Command 5 triggered by write to IH\n");
				command5();
			}
			break;

		case 2:		/* LOAD CMD */
			if (LOG_MICROCODE) logerror("%04X:CMD=%02X\n", cpu_getpreviouspc(), data);
			micro.cmd = data;
			micro.cmdlo = data & 7;
			if (micro.cmdlo == 0)
				logerror("  Command 0 triggered\n");
			else if (micro.cmdlo == 1)
				logerror("  Command 1 triggered\n");
			else if (micro.cmdlo == 6)
			{
				if (LOG_MICROCODE) logerror("  Command 6 triggered\n");
				command6();
			}
			break;

		case 3:		/* LOAD G */
			if (LOG_MICROCODE) logerror("%04X:G=%02X\n", cpu_getpreviouspc(), data);
			micro.g = data;
			break;

		case 4:		/* LOAD X */
			if (LOG_MICROCODE) logerror("%04X:X=%02X\n", cpu_getpreviouspc(), data);
			micro.xp = data;
			if (micro.cmdlo == 3)
			{
				if (LOG_MICROCODE) logerror(" Command 3 triggered by write to X\n");
				command3();
			}
			break;

		case 5:		/* LOAD Y */
			if (LOG_MICROCODE) logerror("%04X:Y=%02X\n", cpu_getpreviouspc(), data);
			micro.yp = data;
			if (micro.cmdlo == 4)
			{
				if (LOG_MICROCODE) logerror("  Command 4 triggered by write to Y\n");
				command4();
			}
			break;

		case 6:		/* LOAD R */
			if (LOG_MICROCODE) logerror("%04X:R=%02X\n", cpu_getpreviouspc(), data);
			micro.r = data;
			break;

		case 7:		/* LOAD B */
			if (LOG_MICROCODE) logerror("%04X:B=%02X\n", cpu_getpreviouspc(), data);
			micro.b = data;
			if (micro.cmdlo == 2)
			{
				if (LOG_MICROCODE) logerror("  Command 2 triggered by write to B\n");
				command2();
			}
			else if (micro.cmdlo == 7)
			{
				if (LOG_MICROCODE) logerror("  Command 7 triggered by write to B\n");
				command7();
			}
			break;

		case 8:		/* SCROLLX */
			if (LOG_MICROCODE) logerror("%04X:SCROLLX write = %02X\n", cpu_getpreviouspc(), data);
			scrollx = data;
			break;

		case 9:		/* SCROLLY */
			if (LOG_MICROCODE) logerror("%04X:SCROLLY write = %02X\n", cpu_getpreviouspc(), data);
			scrolly = data;
			break;

		case 10:	/* CONTROL */
			// D7 = HLMBK
			// D6 = VLMBK
			// D5 = BIRQEA
			// D4 = SEL5060
			// D3 = SINVERT
			// D2 = BIR12
			// D1 = SELOVER
			if (LOG_MICROCODE) logerror("%04X:CONTROL write = %02X\n", cpu_getpreviouspc(), data);
			video_control = data;
			break;

		case 11:	/* CLRVIRQ */
			if (LOG_MICROCODE) logerror("%04X:CLRVIRQ write = %02X\n", cpu_getpreviouspc(), data);
			vblank_irq = 0;
			victory_update_irq();
			break;

		default:
			if (LOG_MICROCODE) logerror("%04X:victory_video_control_w(%02X) = %02X\n", cpu_getpreviouspc(), offset, data);
			break;
	}
}


/***************************************************************************************************

	Victory Microcode
	-----------------

	The cool thing about this hardware is the use of microcode, which is like having a little
	graphics coprocessor around to do the hard stuff. The operations that can be performed by
	this bit of circuitry include pixel plotting, line drawing, sprite drawing, and data
	transfer, all with optional collision detection. In addition, data can be uploaded into
	the $2000-$21FF address range and then "executed" as mini subroutines.

	Commands to the microcode are written to the command register at $C102, followed by
	whatever parameters are needed. Parameters are stored in registers. There are a number
	of registers, accessed at these addresses:

		C100-C101:	I (16 bits)
		C102:		CMD (8 bits)
		C103:		G (8 bits)
		C104:		X' (8 bits)
		C105:		Y' (8 bits)
		C106:		R (8 bits)
		C107:		B (8 bits)

	Writing the last parameter triggers the command. There are a total of 6 commands supported:

		command 2: copy data
			when register B is written, take the bytes from R, G and B and transfer them
			into video RAM at address I

		command 3: draw sprite
			when register X is written, draw a sprite at location (X,Y) using the data from
			video RAM address I; the width is given by (R >> 5) * 8, and then height is
			given by (R & 31) * 2; data is XORed with the current VRAM contents

		command 4: execute program
			when register Y is written, copy Y * 2 to the PC and begin executing the commands
			at ($2000 + PC); each command loads 6 bytes from VRAM into registers CMD,X,Y,I and R;
			the program stops executing after it receives a command with the high bit off

		command 5: draw vector
			when register IH is written, draw a vector of length IL starting at location (X,Y);
			IH serves as the bresenhem increment for the minor axis; bits 4-6 of the command
			select which octant to draw into; each VRAM write XORs the data from R,G and B
			with the current VRAM contents

		command 6: copy data
			when the command is written, copy (R & 31) * 2 bytes of data from video RAM location
			I to video RAM location ($2000 + PC)

		command 7: plot pixel
			when register B is written, take the bytes from R, G and B and XOR them with the
			video RAM contents at (X,Y)

	The command register is actually broken down into bitfields as follows:

		D7    -> must be high for a program to continue execution; otherwise, it will stop
		D4-D6 -> for non-vector commands, enables VRAM writes to the red, blue and green planes
		D3    -> enable collision detection for commands 3,5,7
		D0-D2 -> command

	The microcode is actually a big state machine, driven by the 4 PROMs at 19B,19C,19D and 19E.
	Below are some of the gory details of the state machine.

***************************************************************************************************

	19E:
		D7 -> inverter -> ZERO RAM [11C8, 13D8]
		D6 -> select on the mux at 18F
		D5 -> BUSY [4B6]
		D4 -> D on flip flop at 16E
		D3 -> D3 of alternate selection from mux at 18F
		D2 -> D2 of alternate selection from mux at 18F
		D1 -> D1 of alternate selection from mux at 18F
		D0 -> D0 of alternate selection from mux at 18F

	19B:
		D7 -> S LOAD LH [11B8]
		D6 -> INC I (AND with WRITE EA) [8A8]
		D5 -> S INC Y (AND with WRITE EA) [8C8]
		D4 -> SXFERY (AND with WRITE EA) [8C8]
		D3 -> D on flip flop at 15E, output goes to SADDX [8C8]
		D2 -> S LOAD PC [8B8]
		D1 -> CPU0 [11C8, 13C7]
		D0 -> INC X (AND with WRITE EA) [8C8]

	19C:
		D7 -> SXFERX/INC X (AND with WRITE EA) [8C8, 11B8, 12C8]
		D6 -> see D5
		D5 -> selects one of 4 with D6:
				0 -> SEA VDATA
				1 -> SEA BUFF
				2 -> SEA SR 1
				3 -> SEA SR 2
		D4 -> ADD 128 [11C8, 12C8]
		      also: S ACC CLEAR (AND with WRITE EA) [10B8]
		D3 -> S ACC CLK (AND with S SEQ CLK) [10B8]
		D2 -> INC PC [8B8]
		D1 -> INC L [11B8]
		D0 -> INC H [11B8]

	19D:
		D7 -> S W VRAM (AND with WRITE EA) [14A8]
		D6 -> S WRITE BUSS1 (AND with WRITE EA) [7A8]
		D5 -> S WRITE BUSS2 (AND with WRITE EA) [7A8]
		D4 -> D2 of alternate selection from mux at 18E
		D3 -> D1 of alternate selection from mux at 18E
		D2 -> D0 of alternate selection from mux at 18E
		D1 -> ASEL1 (AND with WRITE EA) [8D8]
		D0 -> ASEL0 (AND with WRITE EA) [8D8]


	Always on in non-zero states: BUSY, CPU0

	State	Next				ASEL SEA Interesting bits
	-----	----				---- --- --------------------------------------------
	  00	/SETRDY ? 00 : 01	  3   0  None
	  01	CM0-2				  0   0
	  02	00					  0   0  ZERORAM, INCI, SWVRAM
	  03	1C					  2   0  SLOADLH, SXFERY
	  04	1A					  2   0  SLOADPC
	  05	0A					  1   0  SXFERY, ADD128+SACCCLEAR, SACCCLK
	  06	0C					  0   0  SLOADLH, SLOADPC
	  07	08					  1   0  SXFERY, SXFERX+INCX
	  08	09					  1   2  INCX, SWVRAM
	  09	00					  1   3  SWVRAM
	  0A	VFIN ? 19 : 0B		  1   0  SXFERX+INCX
	  0B	0A					  1   2  INCI, SACCCLK, SWVRAM
	  0C	0D					  0   1  INCI, SXFERX+INCX, INCL
	  0D	/LTC ? 0C : 0E		  2   2  ZERORAM, INCPC, SWVRAM
	  0E	19					  2   2

	  19	/CM7 ? 00 : 1A		  2   0
	  1A	1B					  2   0  INCPC, SWRITEBUSS1
	  1B	01					  2   0  INCPC, SWRITEBUSS2
	  1C	HTC ? 19 : 1D		  0   1
	  1D	1E					  1   2  INCX, SXFERX+INCX, INCL, SWVRAM
	  1E	/LTC ? 1C : 1F		  1   3  INCI, SINCY, SWVRAM
	  1F	1C					  1   0  ZERORAM, SXFERY, SADDX, INCH

Registers:

	X' = 8-bit value = 2 x 4-bit counters at 11B/13B
			SADDX  -> enables clock to count
			LF/RT  -> controls direction of counting
			SLDX   -> loads data from RED VRAM or D0-D7 into X'
			OUT    -> to X

	X  = 8-bit value = 2 x 4-bit counters at 12D/13D
			SINCX  -> enables clock to count
			SXFERX -> loads data from X' into X, with an XOR of 7
			OUT    -> to X1-X128

	Y' = 8-bit value = 8-bit latch
			SLDY   -> loads data from BLUE VRAM or D0-D7 into Y'
			OUT    -> to Y

	Y  = 8-bit value = 2 x 4-bit counters at 10B/8B
			SINCY  -> enables clock to count
			SXFERY -> loads data from Y' into Y
			OUT    -> to Y1-Y128

	I  = 16-bit value = 4 x 4-bit counters at 12C/11C/12B/14B
			INCI   -> enables clock to count
			SLDIH  -> loads data from BLUE VRAM or D0-D7 into upper 8 bits of I
			SLDIL  -> loads data from RED VRAM or D0-D7 into lower 8 bits of I
			OUT    -> to I1-I32000

	PC = 9-bit value = 2 x 4-bit counters at 9B/7B plus JK flip-flop at 12E
			INCPC  -> toggles flip-flop and increments
			SLOADPC-> loads data from Y' into PC

	L  = 5-bit value = 2 x 4-bit counters at 3H/4H
			INCL   -> enables clock to count
			SLOADLH-> loads data from SEA

	H  = 3-bit value = 1 x 4-bit counter at 5H
			INCH   -> enables clock to count
			SLOADLH-> loads data from SEA

	14-bit VRAM address comes from one of several sources, depending on ASEL
		ASEL0 -> I & 0x3fff
		ASEL1 -> ((Y & 0xff) << 5) | ((X & 0xff) >> 3)
		ASEL2 -> 0x2000 | (PC & 0x1ff)
		ASEL3 -> ((L & 0xff) << 5) | ((E & 0xff) >> 3) 	[video refresh]

***************************************************************************************************/


/*************************************
 *
 *	Microcode timing
 *
 *************************************/

INLINE void count_states(int states)
{
	if (!micro.timer)
	{
		micro.timer = timer_set(TIME_NEVER, 0, NULL);
		micro.endtime = (double)states * MICRO_STATE_CLOCK_PERIOD;
	}
	else if (timer_timeelapsed(micro.timer) > micro.endtime)
	{
		timer_reset(micro.timer, TIME_NEVER);
		micro.endtime = (double)states * MICRO_STATE_CLOCK_PERIOD;
	}
	else
		micro.endtime += (double)states * MICRO_STATE_CLOCK_PERIOD;
}


/*************************************
 *
 *	Microcode command 2:
 *		Load data from R/G/B
 *
 *************************************/

static int command2(void)
{
/*
	Actual microcode:
		  02	00					  0   0  ZERORAM, INCI, SWVRAM

	Basic gist of things:
		WRITE
		I++
		goto state00
*/
	int addr = micro.i++ & 0x3fff;

	if (micro.cmd & 0x10)
		gram[addr] = micro.g;
	if (micro.cmd & 0x20)
		bram[addr] = micro.b;
	if (micro.cmd & 0x40)
		rram[addr] = micro.r;

	scandirty[addr >> 5] = 1;
	count_states(3);
	return 0;
}


/*************************************
 *
 *	Microcode command 3:
 *		Draw sprite from I to (X,Y)
 *
 *************************************/

static int command3(void)
{
/*
	Actual microcode:
		  03	1C					  2   0  SLOADLH, SXFERY
		  1C	HTC ? 19 : 1D		  0   1
		  1D	1E					  1   2  INCX, SXFERX+INCX, INCL, SWVRAM
		  1E	/LTC ? 1C : 1F		  1   3  INCI, SINCY, SWVRAM
		  1F	1C					  1   0  ZERORAM, SXFERY, SADDX, INCH

	Basic gist of things:
		H = R >> 5
		L = (R & 0x1f) << 1
		Y = Y'
		state1C:
			if (H & 8) goto state19
			X = X'; L++
			WRITE
			I++; Y++
			if ((L & 0x20) == 0) goto state1C
			Y = Y'; X'++; H++
			goto state1C
*/
	int ycount = 64 - (micro.r & 31) * 2;
	int xcount = 8 - (micro.r >> 5);
	int shift = micro.xp & 7;
	int nshift = 8 - shift;
	int x, y, sy;

	for (x = 0; x < xcount; x++, micro.xp += 8)
	{
		sy = micro.yp;

		for (y = 0; y < ycount; y++)
		{
			int srcoffs = micro.i++ & 0x3fff;
			int dstoffs = (sy++ & 0xff) * 32 + micro.xp / 8;
			UINT8 src;

			/* non-collision-detect case */
			if (!(micro.cmd & 0x08) || fgcoll)
			{
				if (micro.cmd & 0x10)
				{
					src = gram[srcoffs];
					gram[dstoffs + 0] ^= src >> shift;
					gram[dstoffs + 1] ^= src << nshift;
				}
				if (micro.cmd & 0x20)
				{
					src = bram[srcoffs];
					bram[dstoffs + 0] ^= src >> shift;
					bram[dstoffs + 1] ^= src << nshift;
				}
				if (micro.cmd & 0x40)
				{
					src = rram[srcoffs];
					rram[dstoffs + 0] ^= src >> shift;
					rram[dstoffs + 1] ^= src << nshift;
				}
			}

			/* collision-detect case */
			else
			{
				if (micro.cmd & 0x10)
				{
					src = gram[srcoffs];
					if ((gram[dstoffs + 0] & (src >> shift)) | (gram[dstoffs + 1] & (src << nshift)))
						fgcoll = 1, fgcollx = micro.xp, fgcolly = sy - 1;
					gram[dstoffs + 0] ^= src >> shift;
					gram[dstoffs + 1] ^= src << nshift;
				}
				if (micro.cmd & 0x20)
				{
					src = bram[srcoffs];
					if ((bram[dstoffs + 0] & (src >> shift)) | (bram[dstoffs + 1] & (src << nshift)))
						fgcoll = 1, fgcollx = micro.xp, fgcolly = sy - 1;
					bram[dstoffs + 0] ^= src >> shift;
					bram[dstoffs + 1] ^= src << nshift;
				}
				if (micro.cmd & 0x40)
				{
					src = rram[srcoffs];
					if ((rram[dstoffs + 0] & (src >> shift)) | (rram[dstoffs + 1] & (src << nshift)))
						fgcoll = 1, fgcollx = micro.xp, fgcolly = sy - 1;
					rram[dstoffs + 0] ^= src >> shift;
					rram[dstoffs + 1] ^= src << nshift;
				}
				if (fgcoll) victory_update_irq();
			}
		}
	}

	/* mark scanlines dirty */
	sy = micro.yp;
	for (y = 0; y < ycount; y++)
		scandirty[sy++ & 0xff] = 1;

	count_states(3 + (2 + 2 * ycount) * xcount);

	return micro.cmd & 0x80;
}


/*************************************
 *
 *	Microcode command 4:
 *		Execute commands at (Y * 2)
 *
 *************************************/

static int command4(void)
{
/*
	Actual microcode:
		  04	1A					  2   0  SLOADPC
		  1A	1B					  2   0  INCPC, SWRITEBUSS1
		  1B	01					  2   0  INCPC, SWRITEBUSS2

	Basic gist of things:
		PC = Y' << 1
		CM = GREEN[PC]
		I = (BLUE[PC] << 8) + RED[PC]
		PC++
		R = GREEN[PC]
		X' = RED[PC]
		Y' = BLUE[PC]
		PC++
		goto state01
*/
	int keep_going = 0;

	if (LOG_MICROCODE) logerror("================= EXECUTE BEGIN\n");

	count_states(4);

	micro.pc = micro.yp << 1;
	do
	{
		micro.cmd = gram[0x2000 + micro.pc];
		micro.cmdlo = micro.cmd & 7;
		micro.i = (bram[0x2000 + micro.pc] << 8) | rram[0x2000 + micro.pc];
		micro.r = gram[0x2001 + micro.pc];
		micro.xp = rram[0x2001 + micro.pc];
		micro.yp = bram[0x2001 + micro.pc];
		if (LOG_MICROCODE) logerror("PC=%03X  CMD=%02X I=%04X R=%02X X=%02X Y=%02X\n", micro.pc, micro.cmd, micro.i, micro.r, micro.xp, micro.yp);
		micro.pc = (micro.pc + 2) & 0x1ff;

		switch (micro.cmdlo)
		{
			case 0:												break;
			case 1:												break;
			case 2:	keep_going = command2();					break;
			case 3:	keep_going = command3();					break;
			case 4:	micro.pc = micro.yp << 1; keep_going = 1;	break;
			case 5:	keep_going = command5();					break;
			case 6:	keep_going = command6();					break;
			case 7:	keep_going = command7();					break;
		}
	} while (keep_going);

	if (LOG_MICROCODE) logerror("================= EXECUTE END\n");

	return micro.cmd & 0x80;
}


/*************************************
 *
 *	Microcode command 5:
 *		Draw vector from (X,Y)
 *
 *************************************/

static int command5(void)
{
/*
	Actual microcode:
		  05	0A					  1   0  SXFERY, ADD128+SACCCLEAR, SACCCLK
		  0A	VFIN ? 19 : 0B		  1   0  SXFERX+INCX
		  0B	0A					  1   2  INCI, SACCCLK, SWVRAM

	Basic gist of things:
		Y = Y'; ACC = 128
		X = X'/CLOCK SR
		while (!(IL & 0x100))
		{
			IL++; ACC += IH
			adjust X,Y based on carry
			WRITE(X,Y)  [SR1]
		}

	line draw: one of 8 cases based on VDATA

				no carry			carry
				--------			-----
		case 0: 1011 -> X++, Y		1101 -> X++, Y--
		case 1:	0101 -> X, Y--		1101 -> X++, Y--
		case 2: 0101 -> X, Y--		1100 -> X--, Y--
		case 3:	1010 -> X--, Y		1100 -> X--, Y--
		case 4: 1010 -> X--, Y		1110 -> X--, Y++
		case 5: 0111 -> X, Y++		1110 -> X--, Y++
		case 6: 0111 -> X, Y++		1111 -> X++, Y++
		case 7: 1011 -> X++, Y		1111 -> X++, Y++

*/
	static const INT8 inctable[8][4] =
	{
		{  1, 0, 1,-1 },
		{  0,-1, 1,-1 },
		{  0,-1,-1,-1 },
		{ -1, 0,-1,-1 },
		{ -1, 0,-1, 1 },
		{  0, 1,-1, 1 },
		{  0, 1, 1, 1 },
		{  1, 0, 1, 1 }
	};

	int xinc = inctable[(micro.cmd >> 4) & 7][0];
	int yinc = inctable[(micro.cmd >> 4) & 7][1];
	int xincc = inctable[(micro.cmd >> 4) & 7][2];
	int yincc = inctable[(micro.cmd >> 4) & 7][3];
	UINT8 x = micro.xp;
	UINT8 y = micro.yp;
	int acc = 0x80;
	int i = micro.i >> 8;
	int c;

	/* non-collision-detect case */
	if (!(micro.cmd & 0x08) || fgcoll)
	{
		for (c = micro.i & 0xff; c < 0x100; c++)
		{
			int addr = y * 32 + x / 8;
			int shift = x & 7;
			int nshift = 8 - shift;

			gram[addr + 0] ^= micro.g >> shift;
			gram[addr + 1] ^= micro.g << nshift;
			bram[addr + 0] ^= micro.b >> shift;
			bram[addr + 1] ^= micro.b << nshift;
			rram[addr + 0] ^= micro.r >> shift;
			rram[addr + 1] ^= micro.r << nshift;
			scandirty[y] = 1;

			acc += i;
			if (acc & 0x100)
			{
				x += xincc;
				y += yincc;
			}
			else
			{
				x += xinc;
				y += yinc;
			}
			acc &= 0xff;
		}
	}

	/* collision-detect case */
	else
	{
		for (c = micro.i & 0xff; c < 0x100; c++)
		{
			int addr = y * 32 + x / 8;
			int shift = x & 7;
			int nshift = 8 - shift;

			if ((gram[addr + 0] & (micro.g >> shift)) | (gram[addr + 1] & (micro.g << nshift)) |
				(bram[addr + 0] & (micro.b >> shift)) | (bram[addr + 1] & (micro.b << nshift)) |
				(rram[addr + 0] & (micro.r >> shift)) | (rram[addr + 1] & (micro.r << nshift)))
				fgcoll = 1, fgcollx = x, fgcolly = y;

			gram[addr + 0] ^= micro.g >> shift;
			gram[addr + 1] ^= micro.g << nshift;
			bram[addr + 0] ^= micro.b >> shift;
			bram[addr + 1] ^= micro.b << nshift;
			rram[addr + 0] ^= micro.r >> shift;
			rram[addr + 1] ^= micro.r << nshift;
			scandirty[y] = 1;

			acc += i;
			if (acc & 0x100)
			{
				x += xincc;
				y += yincc;
			}
			else
			{
				x += xinc;
				y += yinc;
			}
			acc &= 0xff;
		}
		if (fgcoll) victory_update_irq();
	}

	micro.xp = x;

	count_states(3 + 2 * (0x100 - (micro.i & 0xff)));

	return micro.cmd & 0x80;
}


/*************************************
 *
 *	Microcode command 6:
 *		Copy data from I to (Y * 2)
 *
 *************************************/

static int command6(void)
{
/*
	Actual microcode:
		  06	0C					  0   0  SLOADLH, SLOADPC
		  0C	0D					  0   1  INCI, SXFERX+INCX, INCL
		  0D	/LTC ? 0C : 0E		  2   2  ZERORAM, INCPC, SWVRAM
		  0E	19					  2   2

	Basic gist of things:
		H = R >> 5
		L = (R & 0x1f) << 1
		PC = Y'
		state0C:
			I++; X = X'; L++
			WRITE(I, *PC)
			PC++
			if ((L & 0x20) == 0) goto state1C
*/
	int i;

	micro.pc = micro.yp << 1;
	for (i = (micro.r & 31) << 1; i < 64; i++)
	{
		int saddr = micro.i++ & 0x3fff;
		int daddr = 0x2000 + micro.pc++;
		micro.pc &= 0x1ff;

		if (micro.cmd & 0x10)
			gram[daddr] = gram[saddr];
		if (micro.cmd & 0x20)
			bram[daddr] = bram[saddr];
		if (micro.cmd & 0x40)
			rram[daddr] = rram[saddr];

		scandirty[daddr >> 5] = 1;
	}

	count_states(3 + 2 * (64 - (micro.r & 31) * 2));

	return micro.cmd & 0x80;
}


/*************************************
 *
 *	Microcode command 7:
 *		Draw pixels to (X,Y)
 *
 *************************************/

static int command7(void)
{
/*
	Actual microcode:
		  07	08					  1   0  SXFERY, SXFERX+INCX
		  08	09					  1   2  INCX, SWVRAM
		  09	00					  1   3  SWVRAM

	Basic gist of things:
		Y = Y'
		X = X'/CLOCK SR
		WRITE SR1
		X++
		WRITE SR2
*/
	int addr = micro.yp * 32 + micro.xp / 8;
	int shift = micro.xp & 7;
	int nshift = 8 - shift;

	/* non-collision-detect case */
	if (!(micro.cmd & 0x08) || fgcoll)
	{
		if (micro.cmd & 0x10)
		{
			gram[addr + 0] ^= micro.g >> shift;
			gram[addr + 1] ^= micro.g << nshift;
		}
		if (micro.cmd & 0x20)
		{
			bram[addr + 0] ^= micro.b >> shift;
			bram[addr + 1] ^= micro.b << nshift;
		}
		if (micro.cmd & 0x40)
		{
			rram[addr + 0] ^= micro.r >> shift;
			rram[addr + 1] ^= micro.r << nshift;
		}
	}

	/* collision-detect case */
	else
	{
		if (micro.cmd & 0x10)
		{
			if ((gram[addr + 0] & (micro.g >> shift)) | (gram[addr + 1] & (micro.g << nshift)))
				fgcoll = 1, fgcollx = micro.xp + 8, fgcolly = micro.yp;
			gram[addr + 0] ^= micro.g >> shift;
			gram[addr + 1] ^= micro.g << nshift;
		}
		if (micro.cmd & 0x20)
		{
			if ((bram[addr + 0] & (micro.b >> shift)) | (bram[addr + 1] & (micro.b << nshift)))
				fgcoll = 1, fgcollx = micro.xp + 8, fgcolly = micro.yp;
			bram[addr + 0] ^= micro.b >> shift;
			bram[addr + 1] ^= micro.b << nshift;
		}
		if (micro.cmd & 0x40)
		{
			if ((rram[addr + 0] & (micro.r >> shift)) | (rram[addr + 1] & (micro.r << nshift)))
				fgcoll = 1, fgcollx = micro.xp + 8, fgcolly = micro.yp;
			rram[addr + 0] ^= micro.r >> shift;
			rram[addr + 1] ^= micro.r << nshift;
		}
		if (fgcoll) victory_update_irq();
	}

	count_states(4);

	scandirty[micro.yp] = 1;
	return micro.cmd & 0x80;
}


/*************************************
 *
 *	Background update
 *
 *************************************/

static void update_background(void)
{
	int x, y, offs;

	/* update the background and any dirty characters in it */
	for (y = offs = 0; y < 32; y++)
		for (x = 0; x < 32; x++, offs++)
		{
			int code = videoram[offs];

			/* see if the character is dirty */
			if (chardirty[code] == 1)
			{
				decodechar(Machine->gfx[0], code, victory_charram, Machine->drv->gfxdecodeinfo[0].gfxlayout);
				chardirty[code] = 2;
			}

			/* see if the bitmap is dirty */
			if (bgdirty[offs] || chardirty[code])
			{
				drawgfx(bgbitmap, Machine->gfx[0], code, 0, 0, 0, x * 8, y * 8, NULL, TRANSPARENCY_NONE_RAW, 0);
				bgdirty[offs] = 0;
			}
		}

	/* reset the char dirty array */
	for (y = 0; y < 256; y++)
		if (chardirty[y] == 2)
			chardirty[y] = 0;
}


/*************************************
 *
 *	Foreground update
 *
 *************************************/

static void update_foreground(void)
{
	int x, y;

	/* update the foreground's dirty scanlines */
	for (y = 0; y < 256; y++)
		if (scandirty[y])
		{
			for (x = 0; x < 256; x += 8)
			{
				UINT8 g = gram[y * 32 + x / 8];
				UINT8 b = bram[y * 32 + x / 8];
				UINT8 r = rram[y * 32 + x / 8];
				plot_pixel(fgbitmap, x + 0, y, ((r & 0x80) >> 5) | ((b & 0x80) >> 6) | ((g & 0x80) >> 7));
				plot_pixel(fgbitmap, x + 1, y, ((r & 0x40) >> 4) | ((b & 0x40) >> 5) | ((g & 0x40) >> 6));
				plot_pixel(fgbitmap, x + 2, y, ((r & 0x20) >> 3) | ((b & 0x20) >> 4) | ((g & 0x20) >> 5));
				plot_pixel(fgbitmap, x + 3, y, ((r & 0x10) >> 2) | ((b & 0x10) >> 3) | ((g & 0x10) >> 4));
				plot_pixel(fgbitmap, x + 4, y, ((r & 0x08) >> 1) | ((b & 0x08) >> 2) | ((g & 0x08) >> 3));
				plot_pixel(fgbitmap, x + 5, y, ((r & 0x04)     ) | ((b & 0x04) >> 1) | ((g & 0x04) >> 2));
				plot_pixel(fgbitmap, x + 6, y, ((r & 0x02) << 1) | ((b & 0x02)     ) | ((g & 0x02) >> 1));
				plot_pixel(fgbitmap, x + 7, y, ((r & 0x01) << 2) | ((b & 0x01) << 1) | ((g & 0x01)     ));
			}
			scandirty[y] = 0;
		}
}


/*************************************
 *
 *	Determine the time when the beam
 *	will intersect a given pixel
 *
 *************************************/

static double pixel_time(int x, int y)
{
	/* assuming this is called at refresh time, compute how long until we
	 * hit the given x,y position */
	return cpu_getscanlinetime(y) + (cpu_getscanlineperiod() * (double)x * (1.0 / 256.0));
}


static void bgcoll_irq_callback(int param)
{
	bgcollx = param & 0xff;
	bgcolly = param >> 8;
	bgcoll = 1;
	victory_update_irq();
}



/*************************************
 *
 *	End-of-frame callback
 *
 *************************************/

void victory_vh_eof(void)
{
	int bgcollmask = (video_control & 4) ? 4 : 7;
	int count = 0;
	int x, y;

	/* if we already did it, skip it */
	if (update_complete)
	{
		update_complete = 0;
		return;
	}
	update_complete = 0;

	/* update the foreground & background */
	update_foreground();
	update_background();

	/* blend the bitmaps and do collision detection */
	for (y = 0; y < 256; y++)
	{
		int sy = (scrolly + y) & 255;
		if (fgbitmap->depth == 8)
		{
			UINT8 *fg = &fgbitmap->line[sy][0];
			UINT8 *bg = &bgbitmap->line[y][0];

			for (x = 0; x < 256; x++)
			{
				int fpix = *fg++;
				int bpix = bg[(x + scrollx) & 255];
				if (fpix && (bpix & bgcollmask) && count++ < 128)
					timer_set(pixel_time(x, y), x | (y << 8), bgcoll_irq_callback);
			}
		}
		else
		{
			UINT16 *fg = &((UINT16 *)fgbitmap->line[sy])[0];
			UINT16 *bg = &((UINT16 *)bgbitmap->line[y])[0];

			for (x = 0; x < 256; x++)
			{
				int fpix = *fg++;
				int bpix = bg[(x + scrollx) & 255];
				if (fpix && (bpix & bgcollmask) && count++ < 128)
					timer_set(pixel_time(x, y), x | (y << 8), bgcoll_irq_callback);
			}
		}
	}
}



/*************************************
 *
 *	Standard screen refresh callback
 *
 *************************************/

void victory_vh_screenrefresh(struct osd_bitmap *bitmap, int full_refresh)
{
	int bgcollmask = (video_control & 4) ? 4 : 7;
	int count = 0;
	int x, y;

	/* recalc the palette */
	palette_recalc();

	/* update the foreground & background */
	update_foreground();
	update_background();

	/* blend the bitmaps and do collision detection */
	for (y = 0; y < 256; y++)
	{
		int sy = (scrolly + y) & 255;
		if (bitmap->depth == 8)
		{
			UINT8 *fg = &fgbitmap->line[y][0];
			UINT8 *bg = &bgbitmap->line[sy][0];
			UINT8 *dst = &bitmap->line[y][0];

			for (x = 0; x < 256; x++)
			{
				int fpix = *fg++;
				int bpix = bg[(x + scrollx) & 255];
				*dst++ = Machine->pens[bpix | (fpix << 3)];
				if (fpix && (bpix & bgcollmask) && count++ < 128)
					timer_set(pixel_time(x, y), x | (y << 8), bgcoll_irq_callback);
			}
		}
		else
		{
			UINT16 *fg = &((UINT16 *)fgbitmap->line[y])[0];
			UINT16 *bg = &((UINT16 *)bgbitmap->line[sy])[0];
			UINT16 *dst = &((UINT16 *)bitmap->line[y])[0];

			for (x = 0; x < 256; x++)
			{
				int fpix = *fg++;
				int bpix = bg[(x + scrollx) & 255];
				*dst++ = Machine->pens[bpix | (fpix << 3)];
				if (fpix && (bpix & bgcollmask) && count++ < 128)
					timer_set(pixel_time(x, y), x | (y << 8), bgcoll_irq_callback);
			}
		}
	}

	/* indicate that we already did collision detection */
	update_complete = 1;
}
