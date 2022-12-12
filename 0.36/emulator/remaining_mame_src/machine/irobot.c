/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

#include "driver.h"
#include "cpuintrf.h"
#include "cpu/m6809/m6809.h"

/* Note:
 * There's probably something wrong with the way the Mathbox gets started.
 * Try compiling with IR_TIMING=1, run with logging on and take a look at
 * the resulting logilfe.
 * The mathbox is started in short intervals (<10 scanlines) without (!)
 * checking its idle status.
 * It also seems that the mathbox in this emulation would have to cope with
 * approx. 5000 instructions per scanline [look at the number of instructions
 * and the number of scanlines to the next mathbox start]. This seems a bit
 * too high.
 */

#define IR_TIMING 				1		/* try to emulate MB and VG running time */
#define DISASSEMBLE_MB_ROM		0		/* generate a disassembly of the mathbox ROMs */

#define IR_CPU_STATE \
	if (errorlog) fprintf (errorlog, \
			"pc: %4x, scanline: %d\n", cpu_getpreviouspc(), cpu_getscanline())


UINT8 irvg_clear;
static UINT8 irvg_vblank;
static UINT8 irvg_running;
static UINT8 irmb_running;
static void *irscanline_timer;

#if IR_TIMING
static void *irvg_timer;
static void *irmb_timer;
#endif
extern void run_video(void);

extern void irobot_poly_clear(void);

extern struct osd_bitmap *polybitmapt;

static UINT8 *comRAM[2], *mbRAM, *mbROM;
static UINT8 irobot_control_num = 0;
static UINT8 irobot_statwr;
static UINT8 irobot_out0;
static UINT8 irobot_outx,irobot_mpage;

static UINT8 *irobot_combase_mb;
UINT8 *irobot_combase;
UINT8 irobot_bufsel;
UINT8 irobot_alphamap;

static void irmb_run(void);


/* mathbox and vector data is stored in big-endian format */
#ifdef LSB_FIRST
#define BYTE_XOR_LE(x)	((x) ^ 1)
#else
#define BYTE_XOR_LE(x)	(x)
#endif


/***********************************************************************/



int irobot_sharedmem_r(int offset)
{
	if (irobot_outx == 3)
		return mbRAM[BYTE_XOR_LE(offset)];

	if (irobot_outx == 2)
		return irobot_combase[BYTE_XOR_LE(offset & 0xFFF)];

	if (irobot_outx == 0)
		return mbROM[((irobot_mpage & 1) << 13) + BYTE_XOR_LE(offset)];

	if (irobot_outx == 1)
		return mbROM[0x4000 + ((irobot_mpage & 3) << 13) + BYTE_XOR_LE(offset)];

	return 0xFF;
}

/* Comment out the mbRAM =, comRAM2 = or comRAM1 = and it will start working */
void irobot_sharedmem_w(int offset,int data)
{
	if (irobot_outx == 3)
		mbRAM[BYTE_XOR_LE(offset)] = data;

	if (irobot_outx == 2)
		irobot_combase[BYTE_XOR_LE(offset & 0xFFF)] = data;
}

static void irvg_done_callback (int param)
{
	if (errorlog)
		fprintf (errorlog, "vg done. ");
	IR_CPU_STATE;
	irvg_running = 0;
}

void irobot_statwr_w(int offset, int data)
{
	if (errorlog)
	{
		fprintf (errorlog, "write %2x ", data);
		IR_CPU_STATE;
	}

	irobot_combase = comRAM[data >> 7];
	irobot_combase_mb = comRAM[(data >> 7) ^ 1];
	irobot_bufsel = data & 0x02;
	if (((data & 0x01) == 0x01) && (irvg_clear == 0))
		irobot_poly_clear();

	irvg_clear = data & 0x01;

	if ((data & 0x04) && !(irobot_statwr & 0x04))
	{
		run_video();
#if IR_TIMING
		if (irvg_running == 0)
		{
			if (errorlog) fprintf(errorlog,"vg start ");
			IR_CPU_STATE;
			irvg_timer = timer_set (TIME_IN_MSEC(10), 0, irvg_done_callback);
		}
		else
		{
			if (errorlog) fprintf (errorlog, "vg start [busy!] ");
			IR_CPU_STATE;
			timer_reset (irvg_timer , TIME_IN_MSEC(10));
		}
#endif
		irvg_running=1;
	}
	if ((data & 0x10) && !(irobot_statwr & 0x10))
		irmb_run();
	irobot_statwr = data;
}

void irobot_out0_w(int offset, int data)
{
	UINT8 *RAM = memory_region(REGION_CPU1);

	irobot_out0 = data;
	switch (data & 0x60)
	{
		case 0:
			cpu_setbank(2, &RAM[0x1C000]);
			break;
		case 0x20:
			cpu_setbank(2, &RAM[0x1C800]);
			break;
		case 0x40:
			cpu_setbank(2, &RAM[0x1D000]);
			break;
	}
	irobot_outx = (data & 0x18) >> 3;
	irobot_mpage = (data & 0x06) >> 1;
	irobot_alphamap = (data & 0x80);
}

void irobot_rom_banksel(int offset, int data)
{
	UINT8 *RAM = memory_region(REGION_CPU1);

	switch ((data & 0x0E) >> 1)
	{
		case 0:
			cpu_setbank(1, &RAM[0x10000]);
			break;
		case 1:
			cpu_setbank(1, &RAM[0x12000]);
			break;
		case 2:
			cpu_setbank(1, &RAM[0x14000]);
			break;
		case 3:
			cpu_setbank(1, &RAM[0x16000]);
			break;
		case 4:
			cpu_setbank(1, &RAM[0x18000]);
			break;
		case 5:
			cpu_setbank(1, &RAM[0x1A000]);
			break;
	}
	osd_led_w(0,data & 0x10);
	osd_led_w(1,data & 0x20);
}

static void scanline_callback(int scanline)
{
    if (scanline == 0) irvg_vblank=0;
    if (scanline == 224) irvg_vblank=1;
    if (errorlog) fprintf(errorlog,"SCANLINE CALLBACK %d\n",scanline);
    /* set the IRQ line state based on the 32V line state */
    cpu_set_irq_line(0, M6809_IRQ_LINE, (scanline & 32) ? ASSERT_LINE : CLEAR_LINE);

    /* set a callback for the next 32-scanline increment */
    scanline += 32;
    if (scanline >= 256) scanline = 0;
    irscanline_timer = timer_set(cpu_getscanlinetime(scanline), scanline, scanline_callback);
}

void irobot_init_machine(void)
{
	UINT8 *MB = memory_region(REGION_CPU2);

	/* initialize the memory regions */
	mbROM 		= MB + 0x00000;
	mbRAM 		= MB + 0x0c000;
	comRAM[0]	= MB + 0x0e000;
	comRAM[1]	= MB + 0x0f000;

	irvg_vblank=0;
	irvg_running = 0;
	irmb_running = 0;

	/* set an initial timer to go off on scanline 0 */
	irscanline_timer = timer_set(cpu_getscanlinetime(0), 0, scanline_callback);

	irobot_rom_banksel(0,0);
	irobot_out0_w(0,0);
	irobot_combase = comRAM[0];
	irobot_combase_mb = comRAM[1];
	irobot_outx = 0;
}

void irobot_control_w (int offset, int data)
{

	irobot_control_num = offset & 0x03;
}

int irobot_control_r (int offset)
{

	if (irobot_control_num == 0)
		return readinputport (5);
	else if (irobot_control_num == 1)
		return readinputport (6);
	return 0;

}

/*  we allow irmb_running and irvg_running to appear running before clearing
	them to simulate the mathbox and vector generator running in real time */
int irobot_status_r(int offset)
{
	int d=0;

	if (errorlog)
	{
		fprintf (errorlog, "status read. ");
		IR_CPU_STATE;
	}

	if (!irmb_running) d |= 0x20;
	if (irvg_running) d |= 0x40;

	//        d = (irmb_running * 0x20) | (irvg_running * 0x40);
	if (irvg_vblank) d = d | 0x80;
#if IR_TIMING
	/* flags are cleared by callbacks */
#else
	irmb_running=0;
	irvg_running=0;
#endif
	return d;
}


/***********************************************************************

	I-Robot Mathbox

    Based on 4 2901 chips slice processors connected to form a 16-bit ALU

    Microcode roms:
    6N: bits 0..3: Address of ALU A register
    5P: bits 0..3: Address of ALU B register
    6M: bits 0..3: ALU Function bits 5..8
    7N: bits 0..3: ALU Function bits 1..4
    8N: bits 0,1: Memory write timing
        bit 2: Hardware multiply mode
        bit 3: ALU Function bit 0
    6P: bits 0,1: Direct addressing bits 0,1
        bits 2,3: Jump address bits 0,1
    8M: bits 0..3: Jump address bits 6..9
    9N: bits 0..3: Jump address bits 2..5
    8P: bits 0..3: Memory address bits 2..5
    9M: bit 0: Shift control
        bits 1..3: Jump type
            0 = No Jump
            1 = On carry
            2 = On zero
            3 = On positive
            4 = On negative
            5 = Unconditional
            6 = Jump to Subroutine
            7 = Return from Subroutine
    7M: Bit 0: Mathbox memory enable
        Bit 1: Latch data to address bus
        Bit 2: Carry in select
        Bit 3: Carry in value
        (if 2,3 = 11 then mathbox is done)
    9P: Bit 0: Hardware divide enable
        Bits 1,2: Memory select
        Bit 3: Memory R/W
    7P: Bits 0,1: Direct addressing bits 6,7
        Bits 2,3: Unused

***********************************************************************/

#define FL_MULT	0x01
#define FL_shift 0x02
#define FL_MBMEMDEC 0x04
#define FL_ADDEN 0x08
#define FL_DPSEL 0x10
#define FL_carry 0x20
#define FL_DIV 0x40
#define FL_MBRW 0x80

typedef struct irmb_ops
{
	const struct irmb_ops *nxtop;
	UINT32 func;
	UINT32 diradd;
	UINT32 latchmask;
	UINT32 *areg;
	UINT32 *breg;
	UINT8 cycles;
	UINT8 diren;
	UINT8 flags;
	UINT8 ramsel;
} irmb_ops;

static irmb_ops *mbops;

static const irmb_ops *irmb_stack[16];
static UINT32 irmb_regs[16];
static UINT32 irmb_latch;

#if DISASSEMBLE_MB_ROM
void disassemble_instruction(irmb_ops *op);
#endif


UINT32 irmb_din(const irmb_ops *curop)
{
	UINT32 d = 0;

	if (!(curop->flags & FL_MBMEMDEC) && (curop->flags & FL_MBRW))
	{
		UINT32 ad = curop->diradd | (irmb_latch & curop->latchmask);

		if (curop->diren || (irmb_latch & 0x6000) == 0)
			d = READ_WORD(&mbRAM[(ad << 1) & 0x1fff]);		/* MB RAM read */
		else if (irmb_latch & 0x4000)
			d = READ_WORD(&mbROM[(ad << 1) + 0x4000]);		/* MB ROM read, CEMATH = 1 */
		else
			d = READ_WORD(&mbROM[(ad << 1) & 0x3fff]);		/* MB ROM read, CEMATH = 0 */
	}
	return d;
}


void irmb_dout(const irmb_ops *curop, UINT32 d)
{
	/* Write to video com ram */
	if (curop->ramsel == 3)
		WRITE_WORD(&irobot_combase_mb[(irmb_latch << 1) & 0xfff], d);

    /* Write to mathox ram */
	if (!(curop->flags & FL_MBMEMDEC))
	{
		UINT32 ad = curop->diradd | (irmb_latch & curop->latchmask);

		if (curop->diren || (irmb_latch & 0x6000) == 0)
			WRITE_WORD(&mbRAM[(ad << 1) & 0x1fff], d);		/* MB RAM write */
	}
}


/* Convert microcode roms to a more usable form */
void load_oproms(void)
{
	UINT8 *MB = memory_region(REGION_CPU2);
	int i;

	/* allocate RAM */
	mbops = malloc(sizeof(irmb_ops) * 1024);
	if (!mbops) return;

	for (i = 0; i < 1024; i++)
	{
		int nxtadd, func, ramsel, diradd, latchmask, dirmask, time;

		mbops[i].areg = &irmb_regs[MB[0xC000 + i] & 0x0F];
		mbops[i].breg = &irmb_regs[MB[0xC400 + i] & 0x0F];
		func = (MB[0xC800 + i] & 0x0F) << 5;
		func |= ((MB[0xCC00 +i] & 0x0F) << 1);
		func |= (MB[0xD000 + i] & 0x08) >> 3;
		time = MB[0xD000 + i] & 0x03;
		mbops[i].flags = (MB[0xD000 + i] & 0x04) >> 2;
		nxtadd = (MB[0xD400 + i] & 0x0C) >> 2;
		diradd = MB[0xD400 + i] & 0x03;
		nxtadd |= ((MB[0xD800 + i] & 0x0F) << 6);
		nxtadd |= ((MB[0xDC00 + i] & 0x0F) << 2);
		diradd |= (MB[0xE000 + i] & 0x0F) << 2;
		func |= (MB[0xE400 + i] & 0x0E) << 9;
		mbops[i].flags |= (MB[0xE400 + i] & 0x01) << 1;
		mbops[i].flags |= (MB[0xE800 + i] & 0x0F) << 2;
		mbops[i].flags |= ((MB[0xEC00 + i] & 0x01) << 6);
		mbops[i].flags |= (MB[0xEC00 + i] & 0x08) << 4;
		ramsel = (MB[0xEC00 + i] & 0x06) >> 1;
		diradd |= (MB[0xF000 + i] & 0x03) << 6;

		if (mbops[i].flags & FL_shift) func |= 0x200;

		mbops[i].func = func;
		mbops[i].nxtop = &mbops[nxtadd];

		/* determine the number of 12MHz cycles for this operation */
		if (time == 3)
			mbops[i].cycles = 2;
		else
			mbops[i].cycles = 3 + time;

		/* precompute the hardcoded address bits and the mask to be used on the latch value */
		if (ramsel == 0)
		{
			dirmask = 0x00FC;
			latchmask = 0x3000;
		}
		else
		{
			dirmask = 0x0000;
			latchmask = 0x3FFC;
		}
		if (ramsel & 2)
			latchmask |= 0x0003;
		else
			dirmask |= 0x0003;

		mbops[i].ramsel = ramsel;
		mbops[i].diradd = diradd & dirmask;
		mbops[i].latchmask = latchmask;
		mbops[i].diren = (ramsel == 0);

#if DISASSEMBLE_MB_ROM
		disassemble_instruction(&mbops[i]);
#endif
	}
}


/* Init mathbox (only called once) */
void init_irobot(void)
{
	int i;
	for (i = 0; i < 16; i++)
	{
		irmb_stack[i] = &mbops[0];
		irmb_regs[i] = 0;
	}
	irmb_latch=0;
	load_oproms();
}

static void irmb_done_callback (int param)
{
    if (errorlog)
		fprintf (errorlog, "mb done. ");
	IR_CPU_STATE;
	irmb_running = 0;
	cpu_set_irq_line(0, M6809_FIRQ_LINE, ASSERT_LINE);
}


#define COMPUTE_CI \
	CI = 0;\
	if (curop->flags & FL_DPSEL)\
		CI = cflag;\
	else\
	{\
		if (curop->flags & FL_carry)\
			CI = 1;\
		if (!(prevop->flags & FL_DIV) && !nflag)\
			CI = 1;\
	}

#define ADD(r,s) \
	COMPUTE_CI;\
	result = r + s + CI;\
	cflag = (result >> 16) & 1;\
	vflag = (((r & 0x7fff) + (s & 0x7fff) + CI) >> 15) ^ cflag

#define SUBR(r,s) \
	COMPUTE_CI;\
	result = (r ^ 0xFFFF) + s + CI;         /*S - R + CI - 1*/ \
	cflag = (result >> 16) & 1;\
	vflag = (((s & 0x7fff) + ((r ^ 0xffff) & 0x7fff) + CI) >> 15) ^ cflag

#define SUB(r,s) \
	COMPUTE_CI;\
	result = r + (s ^ 0xFFFF) + CI;      /*R - S + CI - 1*/ \
	cflag = (result >> 16) & 1;\
	vflag = (((r & 0x7fff) + ((s ^ 0xffff) & 0x7fff) + CI) >> 15) ^ cflag

#define OR(r,s) \
	result = r | s;\
	vflag = cflag = 0

#define AND(r,s) \
	result = r & s;\
	vflag = cflag = 0

#define IAND(r,s) \
	result = (r ^ 0xFFFF) & s;\
	vflag = cflag = 0

#define XOR(r,s) \
	result = r ^ s;\
	vflag = cflag = 0

#define IXOR(r,s) \
	result = (r ^ s) ^ 0xFFFF;\
	vflag = cflag = 0


#define DEST0 \
	Q = Y = zresult

#define DEST1 \
	Y = zresult

#define DEST2 \
	Y = *curop->areg;\
	*curop->breg = zresult

#define DEST3 \
	*curop->breg = zresult;\
	Y = zresult

#define DEST4_NOSHIFT \
	*curop->breg = (zresult >> 1) | ((curop->flags & 0x20) << 10);\
	Q = (Q >> 1) | ((curop->flags & 0x20) << 10);\
	Y = zresult

#define DEST4_SHIFT \
	*curop->breg = (zresult >> 1) | ((nflag ^ vflag) << 15);\
	Q = (Q >> 1) | ((zresult & 0x01) << 15);\
	Y = zresult

#define DEST5_NOSHIFT \
	*curop->breg = (zresult >> 1) | ((curop->flags & 0x20) << 10);\
	Y = zresult

#define DEST5_SHIFT \
	*curop->breg = (zresult >> 1) | ((nflag ^ vflag) << 15);\
	Y = zresult

#define DEST6_NOSHIFT \
	*curop->breg = zresult << 1;\
	Q = ((Q << 1) & 0xffff) | (nflag ^ 1);\
	Y = zresult

#define DEST6_SHIFT \
	*curop->breg = (zresult << 1) | ((Q & 0x8000) >> 15);\
	Q = (Q << 1) & 0xffff;\
	Y = zresult

#define DEST7_NOSHIFT \
	*curop->breg = zresult << 1;\
	Y = zresult

#define DEST7_SHIFT \
	*curop->breg = (zresult << 1) | ((Q & 0x8000) >> 15);\
	Y = zresult


#define JUMP0 	curop++;
#define JUMP1	if (cflag) curop = curop->nxtop; else curop++;
#define JUMP2	if (!zresult) curop = curop->nxtop; else curop++;
#define JUMP3	if (!nflag) curop = curop->nxtop; else curop++;
#define JUMP4	if (nflag) curop = curop->nxtop; else curop++;
#define JUMP5	curop = curop->nxtop;
#define JUMP6	irmb_stack[SP] = curop + 1; SP = (SP + 1) & 15; curop = curop->nxtop;
#define JUMP7	SP = (SP - 1) & 15; curop = irmb_stack[SP];


/* Run mathbox */
void irmb_run(void)
{
	const irmb_ops *prevop = &mbops[0];
	const irmb_ops *curop = &mbops[0];

	UINT32 Q = 0;
	UINT32 Y = 0;
	UINT32 nflag = 0;
	UINT32 vflag = 0;
	UINT32 cflag = 0;
	UINT32 zresult = 1;
	UINT32 CI = 0;
	UINT32 SP = 0;
	UINT32 icount = 0;

	profiler_mark(PROFILER_USER1);

	while ((prevop->flags & (FL_DPSEL | FL_carry)) != (FL_DPSEL | FL_carry))
	{
		UINT32 result;
		UINT32 fu;
		UINT32 tmp;

		icount += curop->cycles;

		/* Get function code */
		fu = curop->func;

		/* Modify function for MULT */
		if (!(prevop->flags & FL_MULT) || (Q & 1))
			fu = fu ^ 0x02;
		else
			fu = fu | 0x02;

		/* Modify function for DIV */
		if ((prevop->flags & FL_DIV) || nflag)
			fu = fu ^ 0x08;
		else
			fu = fu | 0x08;

		/* Do source and operation */
		switch (fu & 0x03f)
		{
			case 0x00:	ADD(*curop->areg, Q);								break;
			case 0x01:	ADD(*curop->areg, *curop->breg);					break;
			case 0x02:	ADD(0, Q);											break;
			case 0x03:	ADD(0, *curop->breg);								break;
			case 0x04:	ADD(0, *curop->areg);								break;
			case 0x05:	tmp = irmb_din(curop); ADD(tmp, *curop->areg);		break;
			case 0x06:	tmp = irmb_din(curop); ADD(tmp, Q);					break;
			case 0x07:	tmp = irmb_din(curop); ADD(tmp, 0);					break;
			case 0x08:	SUBR(*curop->areg, Q);								break;
			case 0x09:	SUBR(*curop->areg, *curop->breg);					break;
			case 0x0a:	SUBR(0, Q);											break;
			case 0x0b:	SUBR(0, *curop->breg);								break;
			case 0x0c:	SUBR(0, *curop->areg);								break;
			case 0x0d:	tmp = irmb_din(curop); SUBR(tmp, *curop->areg);		break;
			case 0x0e:	tmp = irmb_din(curop); SUBR(tmp, Q);				break;
			case 0x0f:	tmp = irmb_din(curop); SUBR(tmp, 0);				break;
			case 0x10:	SUB(*curop->areg, Q);								break;
			case 0x11:	SUB(*curop->areg, *curop->breg);					break;
			case 0x12:	SUB(0, Q);											break;
			case 0x13:	SUB(0, *curop->breg);								break;
			case 0x14:	SUB(0, *curop->areg);								break;
			case 0x15:	tmp = irmb_din(curop); SUB(tmp, *curop->areg);		break;
			case 0x16:	tmp = irmb_din(curop); SUB(tmp, Q);					break;
			case 0x17:	tmp = irmb_din(curop); SUB(tmp, 0);					break;
			case 0x18:	OR(*curop->areg, Q);								break;
			case 0x19:	OR(*curop->areg, *curop->breg);						break;
			case 0x1a:	OR(0, Q);											break;
			case 0x1b:	OR(0, *curop->breg);								break;
			case 0x1c:	OR(0, *curop->areg);								break;
			case 0x1d:	OR(irmb_din(curop), *curop->areg);					break;
			case 0x1e:	OR(irmb_din(curop), Q);								break;
			case 0x1f:	OR(irmb_din(curop), 0);								break;
			case 0x20:	AND(*curop->areg, Q);								break;
			case 0x21:	AND(*curop->areg, *curop->breg);					break;
			case 0x22:	AND(0, Q);											break;
			case 0x23:	AND(0, *curop->breg);								break;
			case 0x24:	AND(0, *curop->areg);								break;
			case 0x25:	AND(irmb_din(curop), *curop->areg);					break;
			case 0x26:	AND(irmb_din(curop), Q);							break;
			case 0x27:	AND(irmb_din(curop), 0);							break;
			case 0x28:	IAND(*curop->areg, Q);								break;
			case 0x29:	IAND(*curop->areg, *curop->breg);					break;
			case 0x2a:	IAND(0, Q);											break;
			case 0x2b:	IAND(0, *curop->breg);								break;
			case 0x2c:	IAND(0, *curop->areg);								break;
			case 0x2d:	IAND(irmb_din(curop), *curop->areg);				break;
			case 0x2e:	IAND(irmb_din(curop), Q);							break;
			case 0x2f:	IAND(irmb_din(curop), 0);							break;
			case 0x30:	XOR(*curop->areg, Q);								break;
			case 0x31:	XOR(*curop->areg, *curop->breg);					break;
			case 0x32:	XOR(0, Q);											break;
			case 0x33:	XOR(0, *curop->breg);								break;
			case 0x34:	XOR(0, *curop->areg);								break;
			case 0x35:	XOR(irmb_din(curop), *curop->areg);					break;
			case 0x36:	XOR(irmb_din(curop), Q);							break;
			case 0x37:	XOR(irmb_din(curop), 0);							break;
			case 0x38:	IXOR(*curop->areg, Q);								break;
			case 0x39:	IXOR(*curop->areg, *curop->breg);					break;
			case 0x3a:	IXOR(0, Q);											break;
			case 0x3b:	IXOR(0, *curop->breg);								break;
			case 0x3c:	IXOR(0, *curop->areg);								break;
			case 0x3d:	IXOR(irmb_din(curop), *curop->areg);				break;
			case 0x3e:	IXOR(irmb_din(curop), Q);							break;
default:	case 0x3f:	IXOR(irmb_din(curop), 0);							break;
		}

		/* Evaluate flags */
		zresult = result & 0xFFFF;
		nflag = zresult >> 15;

		prevop = curop;

		/* Do destination and jump */
		switch (fu >> 6)
		{
			case 0x00:
			case 0x08:	DEST0;			JUMP0;	break;
			case 0x01:
			case 0x09:	DEST1;			JUMP0;	break;
			case 0x02:
			case 0x0a:	DEST2;			JUMP0;	break;
			case 0x03:
			case 0x0b:	DEST3;			JUMP0;	break;
			case 0x04:	DEST4_NOSHIFT;	JUMP0;	break;
			case 0x05:	DEST5_NOSHIFT;	JUMP0;	break;
			case 0x06:	DEST6_NOSHIFT;	JUMP0;	break;
			case 0x07:	DEST7_NOSHIFT;	JUMP0;	break;
			case 0x0c:	DEST4_SHIFT;	JUMP0;	break;
			case 0x0d:	DEST5_SHIFT;	JUMP0;	break;
			case 0x0e:	DEST6_SHIFT;	JUMP0;	break;
			case 0x0f:	DEST7_SHIFT;	JUMP0;	break;

			case 0x10:
			case 0x18:	DEST0;			JUMP1;	break;
			case 0x11:
			case 0x19:	DEST1;			JUMP1;	break;
			case 0x12:
			case 0x1a:	DEST2;			JUMP1;	break;
			case 0x13:
			case 0x1b:	DEST3;			JUMP1;	break;
			case 0x14:	DEST4_NOSHIFT;	JUMP1;	break;
			case 0x15:	DEST5_NOSHIFT;	JUMP1;	break;
			case 0x16:	DEST6_NOSHIFT;	JUMP1;	break;
			case 0x17:	DEST7_NOSHIFT;	JUMP1;	break;
			case 0x1c:	DEST4_SHIFT;	JUMP1;	break;
			case 0x1d:	DEST5_SHIFT;	JUMP1;	break;
			case 0x1e:	DEST6_SHIFT;	JUMP1;	break;
			case 0x1f:	DEST7_SHIFT;	JUMP1;	break;

			case 0x20:
			case 0x28:	DEST0;			JUMP2;	break;
			case 0x21:
			case 0x29:	DEST1;			JUMP2;	break;
			case 0x22:
			case 0x2a:	DEST2;			JUMP2;	break;
			case 0x23:
			case 0x2b:	DEST3;			JUMP2;	break;
			case 0x24:	DEST4_NOSHIFT;	JUMP2;	break;
			case 0x25:	DEST5_NOSHIFT;	JUMP2;	break;
			case 0x26:	DEST6_NOSHIFT;	JUMP2;	break;
			case 0x27:	DEST7_NOSHIFT;	JUMP2;	break;
			case 0x2c:	DEST4_SHIFT;	JUMP2;	break;
			case 0x2d:	DEST5_SHIFT;	JUMP2;	break;
			case 0x2e:	DEST6_SHIFT;	JUMP2;	break;
			case 0x2f:	DEST7_SHIFT;	JUMP2;	break;

			case 0x30:
			case 0x38:	DEST0;			JUMP3;	break;
			case 0x31:
			case 0x39:	DEST1;			JUMP3;	break;
			case 0x32:
			case 0x3a:	DEST2;			JUMP3;	break;
			case 0x33:
			case 0x3b:	DEST3;			JUMP3;	break;
			case 0x34:	DEST4_NOSHIFT;	JUMP3;	break;
			case 0x35:	DEST5_NOSHIFT;	JUMP3;	break;
			case 0x36:	DEST6_NOSHIFT;	JUMP3;	break;
			case 0x37:	DEST7_NOSHIFT;	JUMP3;	break;
			case 0x3c:	DEST4_SHIFT;	JUMP3;	break;
			case 0x3d:	DEST5_SHIFT;	JUMP3;	break;
			case 0x3e:	DEST6_SHIFT;	JUMP3;	break;
			case 0x3f:	DEST7_SHIFT;	JUMP3;	break;

			case 0x40:
			case 0x48:	DEST0;			JUMP4;	break;
			case 0x41:
			case 0x49:	DEST1;			JUMP4;	break;
			case 0x42:
			case 0x4a:	DEST2;			JUMP4;	break;
			case 0x43:
			case 0x4b:	DEST3;			JUMP4;	break;
			case 0x44:	DEST4_NOSHIFT;	JUMP4;	break;
			case 0x45:	DEST5_NOSHIFT;	JUMP4;	break;
			case 0x46:	DEST6_NOSHIFT;	JUMP4;	break;
			case 0x47:	DEST7_NOSHIFT;	JUMP4;	break;
			case 0x4c:	DEST4_SHIFT;	JUMP4;	break;
			case 0x4d:	DEST5_SHIFT;	JUMP4;	break;
			case 0x4e:	DEST6_SHIFT;	JUMP4;	break;
			case 0x4f:	DEST7_SHIFT;	JUMP4;	break;

			case 0x50:
			case 0x58:	DEST0;			JUMP5;	break;
			case 0x51:
			case 0x59:	DEST1;			JUMP5;	break;
			case 0x52:
			case 0x5a:	DEST2;			JUMP5;	break;
			case 0x53:
			case 0x5b:	DEST3;			JUMP5;	break;
			case 0x54:	DEST4_NOSHIFT;	JUMP5;	break;
			case 0x55:	DEST5_NOSHIFT;	JUMP5;	break;
			case 0x56:	DEST6_NOSHIFT;	JUMP5;	break;
			case 0x57:	DEST7_NOSHIFT;	JUMP5;	break;
			case 0x5c:	DEST4_SHIFT;	JUMP5;	break;
			case 0x5d:	DEST5_SHIFT;	JUMP5;	break;
			case 0x5e:	DEST6_SHIFT;	JUMP5;	break;
			case 0x5f:	DEST7_SHIFT;	JUMP5;	break;

			case 0x60:
			case 0x68:	DEST0;			JUMP6;	break;
			case 0x61:
			case 0x69:	DEST1;			JUMP6;	break;
			case 0x62:
			case 0x6a:	DEST2;			JUMP6;	break;
			case 0x63:
			case 0x6b:	DEST3;			JUMP6;	break;
			case 0x64:	DEST4_NOSHIFT;	JUMP6;	break;
			case 0x65:	DEST5_NOSHIFT;	JUMP6;	break;
			case 0x66:	DEST6_NOSHIFT;	JUMP6;	break;
			case 0x67:	DEST7_NOSHIFT;	JUMP6;	break;
			case 0x6c:	DEST4_SHIFT;	JUMP6;	break;
			case 0x6d:	DEST5_SHIFT;	JUMP6;	break;
			case 0x6e:	DEST6_SHIFT;	JUMP6;	break;
			case 0x6f:	DEST7_SHIFT;	JUMP6;	break;

			case 0x70:
			case 0x78:	DEST0;			JUMP7;	break;
			case 0x71:
			case 0x79:	DEST1;			JUMP7;	break;
			case 0x72:
			case 0x7a:	DEST2;			JUMP7;	break;
			case 0x73:
			case 0x7b:	DEST3;			JUMP7;	break;
			case 0x74:	DEST4_NOSHIFT;	JUMP7;	break;
			case 0x75:	DEST5_NOSHIFT;	JUMP7;	break;
			case 0x76:	DEST6_NOSHIFT;	JUMP7;	break;
			case 0x77:	DEST7_NOSHIFT;	JUMP7;	break;
			case 0x7c:	DEST4_SHIFT;	JUMP7;	break;
			case 0x7d:	DEST5_SHIFT;	JUMP7;	break;
			case 0x7e:	DEST6_SHIFT;	JUMP7;	break;
			case 0x7f:	DEST7_SHIFT;	JUMP7;	break;
		}

		/* Do write */
		if (!(prevop->flags & FL_MBRW))
			irmb_dout(prevop, Y);

		/* ADDEN */
		if (!(prevop->flags & FL_ADDEN))
		{
			if (prevop->flags & FL_MBRW)
				irmb_latch = irmb_din(prevop);
			else
				irmb_latch = Y;
		}
	}
	profiler_mark(PROFILER_END);

	if (errorlog)
	{
		fprintf (errorlog, "%d instructions for Mathbox \n", icount);
	}


#if IR_TIMING
	if (irmb_running == 0)
	{
		irmb_timer = timer_set (TIME_IN_HZ(12000000) * icount, 0, irmb_done_callback);
		if (errorlog)
			fprintf (errorlog, "mb start ");
		IR_CPU_STATE;
	}
	else
	{
		if (errorlog)
			fprintf (errorlog, "mb start [busy!] ");
		IR_CPU_STATE;
		timer_reset (irmb_timer, TIME_IN_NSEC(200) * icount);
	}
#else
	cpu_set_irq_line(0, M6809_FIRQ_LINE, ASSERT_LINE);
#endif
	irmb_running=1;
}




#if DISASSEMBLE_MB_ROM
void disassemble_instruction(irmb_ops *op)
{
	int lp;

	if (!errorlog)
		return;

	if (i==0)
		fprintf(errorlog," Address  a b func stor: Q :Y, R, S RDCSAESM da m rs\n");
	fprintf(errorlog,"%04X    : ",i);
	fprintf(errorlog,"%X ",op->areg);
	fprintf(errorlog,"%X ",op->breg);

	lp=(op->func & 0x38)>>3;
	if ((lp&1)==0)
		lp|=1;
	else if((op->flags & FL_DIV) != 0)
		lp&=6;
	else
		fprintf(errorlog,"*");

	switch (lp)
	{
		case 0:
			fprintf(errorlog,"ADD  ");
			break;
		case 1:
			fprintf(errorlog,"SUBR ");
			break;
		case 2:
			fprintf(errorlog,"SUB  ");
			break;
		case 3:
			fprintf(errorlog,"OR   ");
			break;
		case 4:
			fprintf(errorlog,"AND  ");
			break;
		case 5:
			fprintf(errorlog,"AND  ");
			break;
		case 6:
			fprintf(errorlog,"XOR  ");
			break;
		case 7:
			fprintf(errorlog,"XNOR ");
			break;
	}

	switch ((op->func & 0x1c0)>>6)
	{
		case 0:
			fprintf(errorlog,"  - : Q :F,");
			break;
		case 1:
			fprintf(errorlog,"  - : - :F,");
			break;
		case 2:
			fprintf(errorlog,"  R%x: - :A,",op->breg);
			break;
		case 3:
			fprintf(errorlog,"  R%x: - :F,",op->breg);
			break;
		case 4:
			fprintf(errorlog,">>R%x:>>Q:F,",op->breg);
			break;
		case 5:
			fprintf(errorlog,">>R%x: - :F,",op->breg);
			break;
		case 6:
			fprintf(errorlog,"<<R%x:<<Q:F,",op->breg);
			break;
		case 7:
			fprintf(errorlog,"<<R%x: - :F,",op->breg);
			break;
	}

	lp=(op->func & 0x7);
	if ((lp&2)==0)
		lp|=2;
	else if((op->flags & FL_MULT) == 0)
		lp&=5;
	else
		fprintf(errorlog,"*");

	switch (lp)
	{
		case 0:
			fprintf(errorlog,"R%x, Q ",op->areg);
			break;
		case 1:
			fprintf(errorlog,"R%x,R%x ",op->areg,op->breg);
			break;
		case 2:
			fprintf(errorlog,"00, Q ");
			break;
		case 3:
			fprintf(errorlog,"00,R%x ",op->breg);
			break;
		case 4:
			fprintf(errorlog,"00,R%x ",op->areg);
			break;
		case 5:
			fprintf(errorlog," D,R%x ",op->areg);
			break;
		case 6:
			fprintf(errorlog," D, Q ");
			break;
		case 7:
			fprintf(errorlog," D,00 ");
			break;
	}

	for (lp=0;lp<8;lp++)
		if (op->flags & (0x80>>lp))
			fprintf(errorlog,"1");
		else
			fprintf(errorlog,"0");

	fprintf(errorlog," %02X ",op->diradd);
	fprintf(errorlog,"%X\n",op->ramsel);
	if (op->jtype)
	{
		fprintf(errorlog,"              ");
		switch (op->jtype)
		{
			case 1:
				fprintf(errorlog,"BO ");
				break;
			case 2:
				fprintf(errorlog,"BZ ");
				break;
			case 3:
				fprintf(errorlog,"BH ");
				break;
			case 4:
				fprintf(errorlog,"BL ");
				break;
			case 5:
				fprintf(errorlog,"B  ");
				break;
			case 6:
				fprintf(errorlog,"Cl ");
				break;
			case 7:
				fprintf(errorlog,"Return\n\n");
				break;
		}
		if (op->jtype != 7) fprintf(errorlog,"  %04X    \n",op->nxtadd);
		if (op->jtype == 5) fprintf(errorlog,"\n");
		}
	}
}
#endif
