/*****************************************************************************
 *
 *	 f8.c
 *	 Portable F8 emulator (Fairchild 3850)
 *
 *	 Copyright (c) 2000 Juergen Buchmueller, all rights reserved.
 *
 *	 - This source code is released as freeware for non-commercial purposes.
 *	 - You are free to use and redistribute this code in modified or
 *	   unmodified form, provided you list me in the credits.
 *	 - If you modify this source code, you must add a notice to each modified
 *	   source file that it has been changed.  If you're a nice person, you
 *	   will clearly mark each change too.  :)
 *	 - If you wish to use this for commercial purposes, please contact me at
 *	   pullmoll@t-online.de
 *	 - The author of this copywritten work reserves the right to change the
 *     terms of its usage and license at any time, including retroactively
 *   - This entire notice must remain in the source code.
 *
 *	This work is based on Frank Palazzolo's F8 emulation in a standalone
 *	Fairchild Channel F emulator and the 'Fairchild F3850 CPU' data sheets.
 *
 *****************************************************************************/

#include <stdio.h>
#include "driver.h"
#include "state.h"
#include "mamedbg.h"
#include "f8.h"

#define S	0x01
#define C	0x02
#define Z	0x04
#define O	0x08
#define I	0x10

#define cS  4
#define cL	6

typedef struct {
	UINT16	pc0;	/* program counter 0 */
	UINT16	pc1;	/* program counter 1 */
	UINT16	dc0;	/* data counter 0 */
	UINT16	dc1;	/* data counter 1 */
	UINT8	a;		/* accumulator */
	UINT8	w;		/* processor status */
	UINT8	is; 	/* scratchpad pointer */
	UINT8	dbus;	/* data bus value */
	UINT16	io; 	/* last I/O address */
    UINT16  irq_vector;
	int 	(*irq_callback)(int irqline);
    UINT8   r[64];  /* scratchpad RAM */
}	F8;

int f8_icount;

static F8 f8;

/* timer shifter polynome values (will be used for timer interrupts) */
static UINT8 timer_shifter[256];

/* clear all flags */
#define CLR_OZCS                \
	f8.w &= ~(O|Z|C|S)

/* set sign and zero flags (note: the S flag is complementary) */
#define SET_SZ(n)               \
	if (n == 0) 				\
		f8.w |= Z | S;			\
	else						\
	if (n < 128)				\
		f8.w |= S

/* set overflow and carry flags */
#define SET_OC(n,m)             \
	if (n + m > 255)			\
		f8.w |= C;				\
	if ((n&127)+(m&127) > 127)	\
	{							\
		if (!(f8.w & C))		\
			f8.w |= O;			\
	}							\
	else						\
	{							\
		if (f8.w & C)			\
			f8.w |= O;			\
	}

/* Layout of the registers in the debugger */
static UINT8 f8_reg_layout[] = {
	F8_PC0, F8_PC1, F8_DC0, F8_DC1, F8_W, F8_A, F8_IS, -1,
	F8_J, F8_HU, F8_HL, F8_KU, F8_KL, F8_QU, F8_QL, 0
};

/* Layout of the debugger windows x,y,w,h */
static UINT8 f8_win_layout[] = {
	 0, 0,80, 2,	/* register window (top rows) */
	 0, 3,24,19,	/* disassembler window (left colums) */
	25, 3,55, 9,	/* memory #1 window (right, upper middle) */
	25,13,55, 9,	/* memory #2 window (right, lower middle) */
     0,23,80, 1,    /* command line window (bottom rows) */
};

/******************************************************************************
 * ROMC (ROM cycles)
 * This is what the Fairchild F8 CPUs use instead of an address bus
 * There are 5 control lines and each combination of those lines has
 * a special meaning. The devices attached to those control lines all
 * have their own program counters (PC0 and PC1) and at least one
 * data counter (DC0).
 * Currently the emulation does not handle distinct PCs and DCs, but
 * only one instance inside the CPU context.
 ******************************************************************************/
static void ROMC_00(void)
{
    /*
     * Instruction Fetch. The device whose address space includes the
     * contents of the PC0 register must place on the data bus the op
     * code addressed by PC0; then all devices increment the contents
     * of PC0.
     */

	f8.dbus = cpu_readop(f8.pc0);
    f8.pc0 += 1;
    f8_icount -= cS + cL;
}

static void ROMC_01(void)
{
    /*
     * The device whose address space includes the contents of the PC0
     * register must place on the data bus the contents of the memory
     * location addressed by PC0; then all devices add the 8-bit value
     * on the data bus as signed binary number to PC0.
     */
	f8.dbus = cpu_readop_arg(f8.pc0);
	f8.pc0 += (INT8)f8.dbus;
    f8_icount -= cL;
}

static void ROMC_02(void)
{
    /*
     * The device whose DC0 addresses a memory word within the address
     * space of that device must place on the data bus the contents of
     * the memory location addressed by DC0; then all devices increment
     * DC0.
     */
    f8.dbus = cpu_readmem16(f8.dc0);
    f8.dc0 += 1;
    f8_icount -= cL;
}

static void ROMC_03(void)
{
    /*
     * Similiar to 0x00, except that it is used for immediate operands
     * fetches (using PC0) instead of instruction fetches.
     */
    f8.dbus = f8.io = cpu_readop_arg(f8.pc0);
    f8.pc0 += 1;
    f8_icount -= cL + cS;
}

static void ROMC_04(void)
{
    /*
     * Copy the contents of PC1 into PC0
     */
    f8.pc0 = f8.pc1;
    f8_icount -= cS;
}

static void ROMC_05(void)
{
    /*
     * Store the data bus contents into the memory location pointed
     * to by DC0; increment DC0.
     */
    cpu_writemem16(f8.dc0, f8.dbus);
    f8.dc0 += 1;
    f8_icount -= cL;
}

static void ROMC_06(void)
{
    /*
     * Place the high order byte of DC0 on the data bus.
     */
    f8.dbus = f8.dc0 >> 8;
    f8_icount -= cL;
}

static void ROMC_07(void)
{
    /*
     * Place the high order byte of PC1 on the data bus.
     */
    f8.dbus = f8.pc1 >> 8;
    f8_icount -= cL;
}

static void ROMC_08(void)
{
    /*
     * All devices copy the contents of PC0 into PC1. The CPU outputs
     * zero on the data bus in this ROMC state. Load the data bus into
     * both halves of PC0, thus clearing the register.
     */
    f8.pc1 = f8.pc0;
    f8.dbus = 0;
    f8.pc0 = 0;
    f8_icount -= cL;
}

static void ROMC_09(void)
{
    /*
     * The device whose address space includes the contents of the DC0
     * register must place the low order byte of DC0 onto the data bus.
     */
    f8.dbus = f8.dc0 & 0xff;
    f8_icount -= cL;
}

static void ROMC_0A(void)
{
    /*
     * All devices add the 8-bit value on the data bus, treated as
     * signed binary number, to the data counter.
     */
	f8.dc0 += (INT8)f8.dbus;
    f8_icount -= cL;
}

static void ROMC_0B(void)
{
    /*
     * The device whose address space includes the value in PC1
     * must place the low order byte of PC1 onto the data bus.
     */
    f8.dbus = f8.pc1 & 0xff;
    f8_icount -= cL;
}

static void ROMC_0C(void)
{
    /*
     * The device whose address space includes the contents of the PC0
     * register must place the contents of the memory word addressed
     * by PC0 into the data bus; then all devices move the value that
     * has just been placed on the data bus into the low order byte of PC0.
     */
    f8.dbus = cpu_readmem16(f8.pc0);
    f8.pc0 = (f8.pc0 & 0xff00) | f8.dbus;
    f8_icount -= cL;
}

static void ROMC_0D(void)
{
    /*
     * All devices store in PC1 the current contents of PC0, incremented
     * by 1; PC0 is unaltered.
     */
    f8.pc1 = f8.pc0 + 1;
    f8_icount -= cS;
}

static void ROMC_0E(void)
{
    /*
     * The device whose address space includes the contents of the PC0
     * register must place the word addressed by PC0 into the data bus.
     * The value on the data bus is then moved to the low order byte
     * of DC0 by all devices.
     */
    f8.dbus = cpu_readmem16(f8.pc0);
    f8.dc0 = (f8.dc0 & 0xff00) | f8.dbus;
    f8_icount -= cL;
}

static void ROMC_0F(void)
{
    /*
     * The interrupting device with highest priority must place the
     * low order byte of the interrupt vector on the data bus.
     * All devices must copy the contents of PC0 into PC1. All devices
     * must move the contents of the data bus into the low order
     * byte of PC0.
     */
    f8.dbus = f8.irq_vector & 0x00ff;
    f8.pc1 = f8.pc0;
    f8.pc0 = (f8.pc0 & 0xff00) | f8.dbus;
    f8_icount -= cL;
}

static void ROMC_10(void)
{
    /*
     * Inhibit any modification to the interrupt priority logic.
     */
    f8.w |= 0x20;   /* ???? */
    f8_icount -= cL;
}

static void ROMC_11(void)
{
    /*
     * The device whose address space includes the contents of PC0
     * must place the contents of the addressed memory word on the
     * data bus. All devices must then move the contents of the
     * data bus to the upper byte of DC0.
     */
    f8.dbus = cpu_readmem16(f8.pc0);
	f8.dc0 = (f8.dc0 & 0x00ff) | (f8.dbus << 8);
    f8_icount -= cL;
}

static void ROMC_12(void)
{
    /*
     * All devices copy the contents of PC0 into PC1. All devices then
     * move the contents of the data bus into the low order byte of PC0.
     */
    f8.pc1 = f8.pc0;
    f8.pc0 = (f8.pc0 & 0xff00) | f8.dbus;
    f8_icount -= cL;
}

static void ROMC_13(void)
{
    /*
     * The interrupting device with highest priority must move the high
     * order half of the interrupt vector onto the data bus. All devices
     * must then move the contents of the data bus into the high order
     * byte of PC0. The interrupting device resets its interrupt circuitry
     * (so that it is no longer requesting CPU servicing and can respond
     * to another interrupt).
     */
    f8.dbus = f8.irq_vector >> 8;
    f8.pc0 = (f8.pc0 & 0x00ff) | (f8.dbus << 8);
    f8_icount -= cL;
    f8.irq_vector = (*f8.irq_callback)(0);
}

static void ROMC_14(void)
{
    /*
     * All devices move the contents of the data bus into the high
     * order byte of PC0.
     */
    f8.pc0 = (f8.pc0 & 0x00ff) | (f8.dbus << 8);
    f8_icount -= cL;
}

static void ROMC_15(void)
{
    /*
     * All devices move the contents of the data bus into the high
     * order byte of PC1.
     */
    f8.pc1 = (f8.pc1 & 0x00ff) | (f8.dbus << 8);
    f8_icount -= cL;
}

static void ROMC_16(void)
{
    /*
     * All devices move the contents of the data bus into the high
     * order byte of DC0.
     */
    f8.dc0 = (f8.dc0 & 0x00ff) | (f8.dbus << 8);
    f8_icount -= cL;
}

static void ROMC_17(void)
{
    /*
     * All devices move the contents of the data bus into the low
     * order byte of PC0.
     */
    f8.pc0 = (f8.pc0 & 0xff00) | f8.dbus;
    f8_icount -= cL;
}

static void ROMC_18(void)
{
    /*
     * All devices move the contents of the data bus into the low
     * order byte of PC1.
     */
    f8.pc1 = (f8.pc1 & 0xff00) | f8.dbus;
    f8_icount -= cL;
}

static void ROMC_19(void)
{
    /*
     * All devices move the contents of the data bus into the low
     * order byte of DC0.
     */
    f8.dc0 = (f8.dc0 & 0xff00) | f8.dbus;
    f8_icount -= cL;
}

static void ROMC_1A(void)
{
    /*
     * During the prior cycle, an I/O port timer or interrupt control
     * register was addressed; the device containing the addressed port
     * must place the contents of the data bus into the address port.
     */
    cpu_writeport(f8.io, f8.dbus);
    f8_icount -= cL;
}

static void ROMC_1B(void)
{
    /*
     * During the prior cycle, the data bus specified the address of an
     * I/O port. The device containing the addressed I/O port must place
     * the contents of the I/O port on the data bus. (Note that the
     * contents of timer and interrupt control registers cannot be read
     * back onto the data bus).
     */
	f8.dbus = cpu_readport(f8.io);
    f8_icount -= cL;
}

static void ROMC_1C(void)
{
    /*
     * None.
     */
    f8_icount -= cL;
}

static void ROMC_1D(void)
{
    /*
     * Devices with DC0 and DC1 registers must switch registers.
     * Devices without a DC1 register perform no operation.
     */
    UINT16 tmp = f8.dc0;
    f8.dc0 = f8.dc1;
    f8.dc1 = tmp;
    f8_icount -= cS;
}

static void ROMC_1E(void)
{
    /*
     * The devices whose address space includes the contents of PC0
     * must place the low order byte of PC0 onto the data bus.
     */
    f8.dbus = f8.pc0 & 0xff;
    f8_icount -= cL;
}

static void ROMC_1F(void)
{
    /*
     * The devices whose address space includes the contents of PC0
     * must place the high order byte of PC0 onto the data bus.
     */
	f8.dbus = (f8.pc0 >> 8) & 0xff;
    f8_icount -= cL;
}

/***********************************
 *	illegal opcodes
 ***********************************/
static void illegal(void)
{
	logerror("f8 illegal opcode at 0x%04x: %02x\n", f8.pc0, f8.dbus);
	ROMC_00();
}

/***************************************************
 *	O Z C S 0000 0000
 *	- - - - LR	A,KU
 ***************************************************/
static void f8_lr_a_ku(void)
{
	f8.a = f8.r[12];
	ROMC_00();
}

/***************************************************
 *	O Z C S 0000 0001
 *	- - - - LR	A,KL
 ***************************************************/
static void f8_lr_a_kl(void)
{
	f8.a = f8.r[13];
	ROMC_00();
}

/***************************************************
 *	O Z C S 0000 0010
 *	- - - - LR	A,QU
 ***************************************************/
static void f8_lr_a_qu(void)
{
	f8.a = f8.r[14];
	ROMC_00();
}

/***************************************************
 *	O Z C S 0000 0011
 *	- - - - LR	A,QL
 ***************************************************/
static void f8_lr_a_ql(void)
{
	f8.a = f8.r[15];
	ROMC_00();
}

/***************************************************
 *	O Z C S 0000 0100
 *	- - - - LR	KU,A
 ***************************************************/
static void f8_lr_ku_a(void)
{
	f8.r[12] = f8.a;
	ROMC_00();
}

/***************************************************
 *	O Z C S 0000 0101
 *	- - - - LR	KL,A
 ***************************************************/
static void f8_lr_kl_a(void)
{
	f8.r[13] = f8.a;
	ROMC_00();
}

/***************************************************
 *	O Z C S 0000 0110
 *	- - - - LR	QU,A
 ***************************************************/
static void f8_lr_qu_a(void)
{
	f8.r[14] = f8.a;
	ROMC_00();
}

/***************************************************
 *	O Z C S 0000 0111
 *	- - - - LR	QL,A
 ***************************************************/
static void f8_lr_ql_a(void)
{
	f8.r[15] = f8.a;
	ROMC_00();
}

/***************************************************
 *	O Z C S 0000 1000
 *	- - - - LR	K,P
 ***************************************************/
static void f8_lr_k_p(void)
{
	ROMC_07();
	f8.r[12] = f8.dbus;
	ROMC_0B();
	f8.r[13] = f8.dbus;
	ROMC_00();
}

/***************************************************
 *	O Z C S 0000 1001
 *	- - - - LR	P,K
 ***************************************************/
static void f8_lr_p_k(void)
{
	f8.dbus = f8.r[12];
	ROMC_15();
	f8.dbus = f8.r[13];
	ROMC_18();
	ROMC_00();
}

/***************************************************
 *	O Z C S 0000 1010
 *	- - - - LR	A,IS
 ***************************************************/
static void f8_lr_a_is(void)
{
	f8.a = f8.is;
	ROMC_00();
}

/***************************************************
 *	O Z C S 0000 1011
 *	- - - - LR	IS,A
 ***************************************************/
static void f8_lr_is_a(void)
{
	f8.is = f8.a & 0x3f;
	ROMC_00();
}

/***************************************************
 *	O Z C S 0000 1100
 *	- - - - PK
 ***************************************************/
static void f8_pk(void)
{
	f8.dbus = f8.r[13];
	ROMC_12();
	f8.dbus = f8.r[12];
	ROMC_14();
	ROMC_00();
}

/***************************************************
 *	O Z C S 0000 1101
 *	- - - - LR	P0,Q
 ***************************************************/
static void f8_lr_p0_q(void)
{
	f8.dbus = f8.r[15];
	ROMC_17();
	f8.dbus = f8.r[14];
	ROMC_14();
	ROMC_00();
}

/***************************************************
 *	O Z C S 0000 1110
 *	- - - - LR	 Q,DC
 ***************************************************/
static void f8_lr_q_dc(void)
{
	ROMC_06();
	f8.r[14] = f8.dbus;
	ROMC_09();
	f8.r[15] = f8.dbus;
	ROMC_00();
}

/***************************************************
 *	O Z C S 0000 1111
 *	- - - - LR	 DC,Q
 ***************************************************/
static void f8_lr_dc_q(void)
{
    f8.dbus = f8.r[14];
	ROMC_16();
	f8.dbus = f8.r[15];
	ROMC_19();
	ROMC_00();
}

/***************************************************
 *	O Z C S 0001 0000
 *	- - - - LR	 DC,H
 ***************************************************/
static void f8_lr_dc_h(void)
{
	f8.dbus = f8.r[10];
	ROMC_16();
	f8.dbus = f8.r[11];
	ROMC_19();
	ROMC_00();
}

/***************************************************
 *	O Z C S 0001 0001
 *	- - - - LR	 H,DC
 ***************************************************/
static void f8_lr_h_dc(void)
{
	ROMC_06();
	f8.r[10] = f8.dbus;
	ROMC_09();
	f8.r[11] = f8.dbus;
	ROMC_00();
}

/***************************************************
 *	O Z C S 0001 0010
 *	0 x 0 1 SR	 1
 ***************************************************/
static void f8_sr_1(void)
{
	f8.a >>= 1;
	CLR_OZCS;
	SET_SZ(f8.a);
	ROMC_00();
}

/***************************************************
 *	O Z C S 0001 0011
 *	0 x 0 x SL	 1
 ***************************************************/
static void f8_sl_1(void)
{
	f8.a <<= 1;
	CLR_OZCS;
	SET_SZ(f8.a);
	ROMC_00();
}

/***************************************************
 *	O Z C S 0001 0100
 *	0 x 0 1 SR	 4
 ***************************************************/
static void f8_sr_4(void)
{
	f8.a >>= 4;
	CLR_OZCS;
	SET_SZ(f8.a);
	ROMC_00();
}

/***************************************************
 *	O Z C S 0001 0101
 *	0 x 0 x SL	 4
 ***************************************************/
static void f8_sl_4(void)
{
	f8.a <<= 4;
	CLR_OZCS;
	SET_SZ(f8.a);
	ROMC_00();
}

/***************************************************
 *	O Z C S 0001 0110
 *	- - - - LM
 ***************************************************/
static void f8_lm(void)
{
	ROMC_02();
	f8.a = f8.dbus;
	ROMC_00();
}

/***************************************************
 *	O Z C S 0001 0111
 *	- - - - ST
 ***************************************************/
static void f8_st(void)
{
	f8.dbus = f8.a;
	ROMC_05();
	ROMC_00();
}

/***************************************************
 *	O Z C S 0001 1000
 *	0 x 0 x COM
 ***************************************************/
static void f8_com(void)
{
	f8.a = ~f8.a;
	CLR_OZCS;
	SET_SZ(f8.a);
	ROMC_00();
}

/***************************************************
 *	O Z C S 0001 1001
 *	x x x x LNK
 ***************************************************/
static void f8_lnk(void)
{
    if (f8.w & C)
	{
		CLR_OZCS;
		SET_OC(f8.a,1);
		f8.a += 1;
	}
	else
	{
		CLR_OZCS;
		SET_OC(f8.a,0);
    }
	SET_SZ(f8.a);
	ROMC_00();
}

/***************************************************
 *	O Z C S 0001 1010
 *			DI
 ***************************************************/
static void f8_di(void)
{
	ROMC_1C();
    f8.w &= ~I;
	ROMC_00();
}

/***************************************************
 *	O Z C S 0001 1011
 *			EI
 ***************************************************/
static void f8_ei(void)
{
	ROMC_1C();
    f8.w |= I;
	ROMC_00();
}

/***************************************************
 *	O Z C S 0001 1100
 *			POP
 ***************************************************/
static void f8_pop(void)
{
	ROMC_04();
	ROMC_00();
}

/***************************************************
 *	O Z C S 0001 1101
 *	x x x x LR	 W,J
 ***************************************************/
static void f8_lr_w_j(void)
{
	ROMC_1C();
	f8.w = f8.r[9];
	ROMC_00();
}

/***************************************************
 *	O Z C S 0001 1110
 *	- - - - LR	 J,W
 ***************************************************/
static void f8_lr_j_w(void)
{
	f8.r[9] = f8.w;
	ROMC_00();
}

/***************************************************
 *	O Z C S 0001 1111
 *	x x x x INC
 ***************************************************/
static void f8_inc(void)
{
	CLR_OZCS;
	SET_OC(f8.a,1);
	f8.a += 1;
	SET_SZ(f8.a);
	ROMC_00();
}

/***************************************************
 *	O Z C S 0010 0000	aaaa aaaa
 *	- - - - LI	aa
 ***************************************************/
static void f8_li(void)
{
	ROMC_03();
    f8.a = f8.dbus;
	ROMC_00();
}

/***************************************************
 *	O Z C S 0010 0001	aaaa aaaa
 *	0 x 0 x NI	 aa
 ***************************************************/
static void f8_ni(void)
{
	ROMC_03();
    CLR_OZCS;
	f8.a &= f8.dbus;
	SET_SZ(f8.a);
	ROMC_00();
}

/***************************************************
 *	O Z C S 0010 0010	aaaa aaaa
 *	0 x 0 x OI	 aa
 ***************************************************/
static void f8_oi(void)
{
	ROMC_03();
    CLR_OZCS;
	f8.a |= f8.dbus;
	SET_SZ(f8.a);
	ROMC_00();
}

/***************************************************
 *	O Z C S 0010 0011	aaaa aaaa
 *	0 x 0 x XI	 aa
 ***************************************************/
static void f8_xi(void)
{
	ROMC_03();
    CLR_OZCS;
	f8.a ^= f8.dbus;
	SET_SZ(f8.a);
	ROMC_00();
}

/***************************************************
 *	O Z C S 0010 0100	aaaa aaaa
 *	x x x x AI	 aa
 ***************************************************/
static void f8_ai(void)
{
	ROMC_03();
	CLR_OZCS;
	SET_OC(f8.a,f8.dbus);
	f8.a += f8.dbus;
	SET_SZ(f8.a);
	ROMC_00();
}

/***************************************************
 *	O Z C S 0010 0101	aaaa aaaa
 *	x x x x CI	 aa
 ***************************************************/
static void f8_ci(void)
{
	UINT8 tmp = ~f8.a + 1;
	ROMC_03();
	CLR_OZCS;
	SET_OC(tmp,f8.dbus);
	tmp += f8.dbus;
	SET_SZ(tmp);
	ROMC_00();
}

/***************************************************
 *	O Z C S 0010 0110	aaaa aaaa
 *	0 x 0 x IN	 aa
 ***************************************************/
static void f8_in(void)
{
	ROMC_03();
    CLR_OZCS;
	ROMC_1B();
    f8.a = f8.dbus;
	SET_SZ(f8.a);
    ROMC_00();
}

/***************************************************
 *	O Z C S 0010 0111	aaaa aaaa
 *	- - - - OUT  aa
 ***************************************************/
static void f8_out(void)
{
	ROMC_03();
    f8.dbus = f8.a;
	ROMC_1A();
    ROMC_00();
}

/***************************************************
 *	O Z C S 0010 1000	iiii iiii	jjjj jjjj
 *	- - - - PI	 iijj
 ***************************************************/
static void f8_pi(void)
{
	ROMC_03();
	f8.a = f8.dbus;
	ROMC_0D();
	ROMC_0C();
    f8.dbus = f8.a;
	ROMC_14();
    ROMC_00();
}

/***************************************************
 *	O Z C S 0010 1001	iiii iiii	jjjj jjjj
 *	- - - - JMP  iijj
 ***************************************************/
static void f8_jmp(void)
{
	ROMC_03();
	f8.a = f8.dbus;
	ROMC_0C();
    f8.dbus = f8.a;
	ROMC_14();
    ROMC_00();
}

/***************************************************
 *	O Z C S 0010 1010	iiii iiii	jjjj jjjj
 *	- - - - DCI  iijj
 ***************************************************/
static void f8_dci(void)
{
	ROMC_11();
	ROMC_03();
	ROMC_0E();
    ROMC_03();
    ROMC_00();
}

/***************************************************
 *	O Z C S 0010 1011
 *	- - - - NOP
 ***************************************************/
static void f8_nop(void)
{
    ROMC_00();
}

/***************************************************
 *	O Z C S 0010 1100
 *	- - - - XDC
 ***************************************************/
static void f8_xdc(void)
{
	ROMC_1D();
    ROMC_00();
}

/***************************************************
 *	O Z C S 0011 rrrr
 *	x x x x DS	 r
 ***************************************************/
static void f8_ds_r(int r)
{
	CLR_OZCS;
	SET_OC(f8.r[r], 0xff);
	f8.r[r] = f8.r[r] + 0xff;
	SET_SZ(f8.r[r]);
    ROMC_00();
}

/***************************************************
 *	O Z C S 0011 1100
 *	x x x x DS	 ISAR
 ***************************************************/
static void f8_ds_isar(void)
{
	CLR_OZCS;
	SET_OC(f8.r[f8.is], 0xff);
	f8.r[f8.is] = f8.r[f8.is] + 0xff;
	SET_SZ(f8.r[f8.is]);
    ROMC_00();
}

/***************************************************
 *	O Z C S 0011 1101
 *	x x x x DS	 ISAR++
 ***************************************************/
static void f8_ds_isar_i(void)
{
	CLR_OZCS;
	SET_OC(f8.r[f8.is], 0xff);
	f8.r[f8.is] = f8.r[f8.is] + 0xff;
	SET_SZ(f8.r[f8.is]);
	f8.is = (f8.is & 0x38) | ((f8.is + 1) & 0x07);
    ROMC_00();
}

/***************************************************
 *	O Z C S 0011 1110
 *	x x x x DS	ISAR--
 ***************************************************/
static void f8_ds_isar_d(void)
{
	CLR_OZCS;
	SET_OC(f8.r[f8.is], 0xff);
	f8.r[f8.is] = f8.r[f8.is] + 0xff;
	SET_SZ(f8.r[f8.is]);
	f8.is = (f8.is & 0x38) | ((f8.is - 1) & 0x07);
    ROMC_00();
}

/***************************************************
 *	O Z C S 0100 rrrr
 *	- - - - LR	A,r
 ***************************************************/
static void f8_lr_a_r(int r)
{
	f8.a = f8.r[r];
    ROMC_00();
}

/***************************************************
 *	O Z C S 0100 1100
 *	- - - - LR	A,ISAR
 ***************************************************/
static void f8_lr_a_isar(void)
{
	f8.a = f8.r[f8.is];
    ROMC_00();
}

/***************************************************
 *	O Z C S 0100 1101
 *	- - - - LR	A,ISAR++
 ***************************************************/
static void f8_lr_a_isar_i(void)
{
	f8.a = f8.r[f8.is];
	f8.is = (f8.is & 0x38) | ((f8.is + 1) & 0x07);
    ROMC_00();
}

/***************************************************
 *	O Z C S 0100 1110
 *	- - - - LR	A,ISAR--
 ***************************************************/
static void f8_lr_a_isar_d(void)
{
	f8.a = f8.r[f8.is];
	f8.is = (f8.is & 0x38) | ((f8.is - 1) & 0x07);
    ROMC_00();
}

/***************************************************
 *	O Z C S 0101 rrrr
 *	- - - - LR	r,A
 ***************************************************/
static void f8_lr_r_a(int r)
{
	f8.r[r] = f8.a;
    ROMC_00();
}

/***************************************************
 *	O Z C S 0101 1100
 *	- - - - LR	ISAR,A
 ***************************************************/
static void f8_lr_isar_a(void)
{
	f8.r[f8.is] = f8.a;
    ROMC_00();
}

/***************************************************
 *	O Z C S 0101 1101
 *	- - - - LR	ISAR++,A
 ***************************************************/
static void f8_lr_isar_i_a(void)
{
	f8.r[f8.is] = f8.a;
	f8.is = (f8.is & 0x38) | ((f8.is + 1) & 0x07);
    ROMC_00();
}

/***************************************************
 *	O Z C S 0101 1110
 *	- - - - LR	ISAR--,A
 ***************************************************/
static void f8_lr_isar_d_a(void)
{
	f8.r[f8.is] = f8.a;
	f8.is = (f8.is & 0x38) | ((f8.is - 1) & 0x07);
    ROMC_00();
}

/***************************************************
 *	O Z C S 0110 0eee
 *	- - - - LISU e
 ***************************************************/
static void f8_lisu(int e)
{
	f8.is = (f8.is & 0x07) | e;
    ROMC_00();
}

/***************************************************
 *	O Z C S 0110 1eee
 *	- - - - LISL e
 ***************************************************/
static void f8_lisl(int e)
{
	f8.is = (f8.is & 0x38) | e;
    ROMC_00();
}

/***************************************************
 *	O Z C S 0111 iiii
 *	- - - - LIS  i
 ***************************************************/
static void f8_lis(int i)
{
	f8.a = i;
    ROMC_00();
}

/***************************************************
 *	O Z C S 1000 0eee	aaaa aaaa
 *			BT	 e,aa
 ***************************************************/
static void f8_bt(int e)
{
	ROMC_1C();
    if (f8.w & e)
		ROMC_01();	   /* take the relative branch */
	else
		ROMC_03();	   /* just read the argument on the data bus */
    ROMC_00();
}

/***************************************************
 *	O Z C S 1000 1000
 *	x x x x AM
 ***************************************************/
static void f8_am(void)
{
	ROMC_02();
	CLR_OZCS;
	SET_OC(f8.a, f8.dbus);
	f8.a += f8.dbus;
	SET_SZ(f8.a);
    ROMC_00();
}

/***************************************************
 *	O Z C S 1000 1001
 *	x x x x AMD
 ***************************************************/
static void f8_amd(void)
{
	UINT8 tmp = f8.a - 0x66, adj = 0x00;
	int sum;
    ROMC_02();
	sum = (tmp & 0x0f) + (f8.dbus & 0x0f);
	if (sum > 0x09)
		adj += 0x06;
	sum = tmp + f8.dbus + adj;
	if (sum > 0x99)
		adj += 0x60;
	tmp += adj;
	CLR_OZCS;
	SET_OC(tmp,f8.dbus);
	f8.a = tmp + f8.dbus;
	SET_SZ(f8.a);
    ROMC_00();
}

/***************************************************
 *	O Z C S 1000 1010
 *	0 x 0 x NM
 ***************************************************/
static void f8_nm(void)
{
	ROMC_02();
	CLR_OZCS;
	f8.a &= f8.dbus;
	SET_SZ(f8.a);
    ROMC_00();
}

/***************************************************
 *	O Z C S 1000 1011
 *	0 x 0 x OM
 ***************************************************/
static void f8_om(void)
{
	ROMC_02();
	CLR_OZCS;
	f8.a |= f8.dbus;
	SET_SZ(f8.a);
    ROMC_00();
}

/***************************************************
 *	O Z C S 1000 1100
 *	0 x 0 x XM
 ***************************************************/
static void f8_xm(void)
{
    ROMC_02();
	CLR_OZCS;
	f8.a ^= f8.dbus;
	SET_SZ(f8.a);
    ROMC_00();
}

/***************************************************
 *	O Z C S 1000 1101
 *	0 x 0 x CM
 ***************************************************/
static void f8_cm(void)
{
	UINT8 tmp = ~f8.a + 1;
	ROMC_02();
	CLR_OZCS;
	SET_OC(tmp,f8.dbus);
	tmp += f8.dbus;
	SET_SZ(tmp);
    ROMC_00();
}

/***************************************************
 *	O Z C S 1000 1110
 *	- - - - ADC
 ***************************************************/
static void f8_adc(void)
{
	f8.dbus = f8.a;
	ROMC_0A();			/* add data bus value to DC0 */
	ROMC_00();
}

/***************************************************
 *	O Z C S 1000 1111
 *	- - - - BR7
 ***************************************************/
static void f8_br7(void)
{
	if ((f8.is & 7) == 7)
		ROMC_03();		/* just read the argument on the data bus */
	else
		ROMC_01();		/* take the relative branch */
	ROMC_00();
}

/***************************************************
 *	O Z C S 1001 tttt	aaaa aaaa
 *	- - - - BF	 t,aa
 ***************************************************/
static void f8_bf(int t)
{
	ROMC_1C();
    if (f8.w & t)
        ROMC_03();      /* just read the argument on the data bus */
	else
        ROMC_01();      /* take the relative branch */
	ROMC_00();
}

/***************************************************
 *	O Z C S 1010 000n
 *	0 x 0 x INS  n				(n = 0-1)
 ***************************************************/
static void f8_ins_0(int n)
{
	ROMC_1C();
	CLR_OZCS;
    f8.a = cpu_readport(n);
	SET_SZ(f8.a);
    ROMC_00();
}

/***************************************************
 *	O Z C S 1010 nnnn
 *	0 x 0 x INS  n				(n = 4-F)
 ***************************************************/
static void f8_ins_1(int n)
{
	ROMC_1C();
	f8.io = n;
    ROMC_1B();
	CLR_OZCS;
	f8.a = f8.dbus;
	SET_SZ(f8.a);
    ROMC_00();
}

/***************************************************
 *	O Z C S 1011 000n
 *	- - - - OUTS n				(n = 0-1)
 ***************************************************/
static void f8_outs_0(int n)
{
	ROMC_1C();
	cpu_writeport(n, f8.a);
    ROMC_00();
}

/***************************************************
 *	O Z C S 1011 nnnn
 *	- - - - OUTS n				(n = 4-F)
 ***************************************************/
static void f8_outs_1(int n)
{
	ROMC_1C();
    f8.io = n;
	f8.dbus = f8.a;
    ROMC_1A();
    ROMC_00();
}

/***************************************************
 *	O Z C S 1100 rrrr
 *	x x x x AS	 r
 ***************************************************/
static void f8_as(int r)
{
	CLR_OZCS;
	SET_OC(f8.a, f8.r[r]);
	f8.a += f8.r[r];
	SET_SZ(f8.a);
	ROMC_00();
}

/***************************************************
 *	O Z C S 1100 1100
 *	x x x x AS	 ISAR
 ***************************************************/
static void f8_as_isar(void)
{
	CLR_OZCS;
	SET_OC(f8.a, f8.r[f8.is]);
	f8.a += f8.r[f8.is];
	SET_SZ(f8.a);
	ROMC_00();
}

/***************************************************
 *	O Z C S 1100 1101
 *	x x x x AS	 ISAR++
 ***************************************************/
static void f8_as_isar_i(void)
{
	CLR_OZCS;
	SET_OC(f8.a, f8.r[f8.is]);
	f8.a += f8.r[f8.is];
	SET_SZ(f8.a);
	f8.is = (f8.is & 0x38) | ((f8.is + 1) & 0x07);
	ROMC_00();
}

/***************************************************
 *	O Z C S 1100 1110
 *	x x x x AS	 ISAR--
 ***************************************************/
static void f8_as_isar_d(void)
{
	CLR_OZCS;
	SET_OC(f8.a, f8.r[f8.is]);
	f8.a += f8.r[f8.is];
	SET_SZ(f8.a);
	f8.is = (f8.is & 0x38) | ((f8.is - 1) & 0x07);
	ROMC_00();
}

/***************************************************
 *	O Z C S 1101 rrrr
 *	x x x x ASD  r
 ***************************************************/
static void f8_asd(int r)
{
	UINT8 tmp = f8.a - 0x66, adj = 0x00;
	int sum;
	ROMC_1C();
	sum = (tmp & 0x0f) + (f8.r[r] & 0x0f);
	if (sum > 0x09)
		adj += 0x06;
	sum = tmp + f8.r[r] + adj;
	if (sum > 0x99)
		adj += 0x60;
	tmp += adj;
	CLR_OZCS;
	SET_OC(tmp, f8.r[r]);
	f8.a = tmp + f8.r[r];
	SET_SZ(f8.a);
	ROMC_00();
}

/***************************************************
 *	O Z C S 1101 1100
 *	x x x x ASD  ISAR
 ***************************************************/
static void f8_asd_isar(void)
{
	UINT8 tmp = f8.a - 0x66, adj = 0x00;
	int sum;
	ROMC_1C();
	sum = (tmp & 0x0f) + (f8.r[f8.is] & 0x0f);
	if (sum > 0x09)
		adj += 0x06;
	sum = tmp + f8.r[f8.is] + adj;
	if (sum > 0x99)
		adj += 0x60;
	tmp += adj;
	CLR_OZCS;
	SET_OC(tmp, f8.r[f8.is]);
	f8.a = tmp + f8.r[f8.is];
	SET_SZ(f8.a);
	ROMC_00();
}

/***************************************************
 *	O Z C S 1101 1101
 *	x x x x ASD  ISAR++
 ***************************************************/
static void f8_asd_isar_i(void)
{
	UINT8 tmp = f8.a - 0x66, adj = 0x00;
	int sum;
	ROMC_1C();
	sum = (tmp & 0x0f) + (f8.r[f8.is] & 0x0f);
	if (sum > 0x09)
		adj += 0x06;
	sum = tmp + f8.r[f8.is] + adj;
	if (sum > 0x99)
		adj += 0x60;
	tmp += adj;
	CLR_OZCS;
	SET_OC(tmp, f8.r[f8.is]);
	f8.a = tmp + f8.r[f8.is];
	SET_SZ(f8.a);
	f8.is = (f8.is & 0x38) | ((f8.is + 1) & 0x07);
	ROMC_00();
}

/***************************************************
 *	O Z C S 1101 1110
 *	x x x x ASD  ISAR--
 ***************************************************/
static void f8_asd_isar_d(void)
{
	UINT8 tmp = f8.a - 0x66, adj = 0x00;
	int sum;
	ROMC_1C();
	sum = (tmp & 0x0f) + (f8.r[f8.is] & 0x0f);
	if (sum > 0x09)
		adj += 0x06;
	sum = tmp + f8.r[f8.is] + adj;
	if (sum > 0x99)
		adj += 0x60;
	tmp += adj;
	CLR_OZCS;
	SET_OC(tmp, f8.r[f8.is]);
	f8.a = tmp + f8.r[f8.is];
	SET_SZ(f8.a);
	f8.is = (f8.is & 0x38) | ((f8.is - 1) & 0x07);
	ROMC_00();
}

/***************************************************
 *	O Z C S 1110 rrrr
 *	0 x 0 x XS	 r
 ***************************************************/
static void f8_xs(int r)
{
	CLR_OZCS;
	f8.a ^= f8.r[r];
	SET_SZ(f8.a);
	ROMC_00();
}

/***************************************************
 *	O Z C S 1110 1100
 *	0 x 0 x XS	 ISAR
 ***************************************************/
static void f8_xs_isar(void)
{
	CLR_OZCS;
	f8.a ^= f8.r[f8.is];
	SET_SZ(f8.a);
	ROMC_00();
}

/***************************************************
 *	O Z C S 1110 1101
 *	0 x 0 x XS	 ISAR++
 ***************************************************/
static void f8_xs_isar_i(void)
{
	CLR_OZCS;
	f8.a ^= f8.r[f8.is];
	SET_SZ(f8.a);
	f8.is = (f8.is & 0x38) | ((f8.is + 1) & 0x07);
	ROMC_00();
}

/***************************************************
 *	O Z C S 1110 1110
 *	0 x 0 x XS	 ISAR--
 ***************************************************/
static void f8_xs_isar_d(void)
{
	CLR_OZCS;
	f8.a ^= f8.r[f8.is];
	SET_SZ(f8.a);
	f8.is = (f8.is & 0x38) | ((f8.is - 1) & 0x07);
	ROMC_00();
}

/***************************************************
 *	O Z C S 1111 rrrr
 *	0 x 0 x NS	 r
 ***************************************************/
static void f8_ns(int r)
{
	CLR_OZCS;
	f8.a &= f8.r[r];
	SET_SZ(f8.a);
	ROMC_00();
}

/***************************************************
 *	O Z C S 1111 1100
 *	0 x 0 x NS	 ISAR
 ***************************************************/
static void f8_ns_isar(void)
{
	CLR_OZCS;
	f8.a &= f8.r[f8.is];
	SET_SZ(f8.a);
	ROMC_00();
}

/***************************************************
 *	O Z C S 1111 1101
 *	0 x 0 x NS	 ISAR++
 ***************************************************/
static void f8_ns_isar_i(void)
{
	CLR_OZCS;
	f8.a &= f8.r[f8.is];
	SET_SZ(f8.a);
	f8.is = (f8.is & 0x38) | ((f8.is + 1) & 0x07);
	ROMC_00();
}

/***************************************************
 *	O Z C S 1111 1110
 *	0 x 0 x NS	 ISAR--
 ***************************************************/
static void f8_ns_isar_d(void)
{
	CLR_OZCS;
	f8.a &= f8.r[f8.is];
	SET_SZ(f8.a);
	f8.is = (f8.is & 0x38) | ((f8.is - 1) & 0x07);
	ROMC_00();
}

void f8_reset(void *param)
{
	UINT8 data;
	int i;

	memset(&f8, 0, sizeof(F8));

	/* save PC0 to PC1 and reset PC0 */
	ROMC_08();
	/* fetch the first opcode */
	ROMC_00();

	/* initialize the timer shift register
	 * this is an 8 bit polynome counter which can be loaded parallel
	 * with 0xff the outputs never change and thus the timer is disabled.
	 * with 0xfe the shifter starts cycling through 255 states until it
	 * reaches 0xfe again (and then issues an interrupt).
	 * the counter output values are not sequential, but go like this:
	 * 0xfe, 0xfd, 0xfb, 0xf7, 0xee, 0xdc ... etc. :-)
	 * We have to build a lookup table to tell how many cycles a write

	 */
	data = 0xfe;	/* initial value */
	for (i = 0; i < 256; i++)
	{
		timer_shifter[i] = data;
		if ( (((data >> 3) ^ (data >> 4)) ^
			  ((data >> 5) ^ (data >> 7))) & 1 )
			data <<= 1;
		else
			data = (data << 1) | 1;
	}
}
/* Shut down CPU core */
void f8_exit(void)
{
	/* nothing to do */
}

/* Execute cycles - returns number of cycles actually run */
int f8_execute(int cycles)
{
	f8_icount = cycles;

    do
    {
        CALL_MAME_DEBUG;

		switch( f8.dbus )
        {
		/* opcode  bitmask */
		case 0x00: /* 0000 0000 */	f8_lr_a_ku();		break;
		case 0x01: /* 0000 0001 */	f8_lr_a_kl();		break;
		case 0x02: /* 0000 0010 */	f8_lr_a_qu();		break;
		case 0x03: /* 0000 0011 */	f8_lr_a_ql();		break;
		case 0x04: /* 0000 0100 */	f8_lr_ku_a();		break;
		case 0x05: /* 0000 0101 */	f8_lr_kl_a();		break;
		case 0x06: /* 0000 0110 */	f8_lr_qu_a();		break;
		case 0x07: /* 0000 0111 */	f8_lr_ql_a();		break;
		case 0x08: /* 0000 1000 */	f8_lr_k_p();		break;
		case 0x09: /* 0000 1001 */	f8_lr_p_k();		break;
		case 0x0a: /* 0000 1010 */	f8_lr_a_is();		break;
		case 0x0b: /* 0000 1011 */	f8_lr_is_a();		break;
		case 0x0c: /* 0000 1100 */	f8_pk();			break;
		case 0x0d: /* 0000 1101 */	f8_lr_p0_q();		break;
		case 0x0e: /* 0000 1110 */	f8_lr_q_dc();		break;
		case 0x0f: /* 0000 1111 */	f8_lr_dc_q();		break;

        case 0x10: /* 0001 0000 */  f8_lr_dc_h();       break;
		case 0x11: /* 0001 0001 */	f8_lr_h_dc();		break;
		case 0x12: /* 0001 0010 */	f8_sr_1();			break;
		case 0x13: /* 0001 0011 */	f8_sl_1();			break;
		case 0x14: /* 0001 0100 */	f8_sr_4();			break;
		case 0x15: /* 0001 0101 */	f8_sl_4();			break;
		case 0x16: /* 0001 0110 */	f8_lm();			break;
		case 0x17: /* 0001 0111 */	f8_st();			break;
		case 0x18: /* 0001 1000 */	f8_com();			break;
		case 0x19: /* 0001 1001 */	f8_lnk();			break;
		case 0x1a: /* 0001 1010 */	f8_di();			break;
		case 0x1b: /* 0001 1011 */	f8_ei();			break;
		case 0x1c: /* 0001 1100 */	f8_pop();			break;
		case 0x1d: /* 0001 1101 */	f8_lr_w_j();		break;
		case 0x1e: /* 0001 1110 */	f8_lr_j_w();		break;
		case 0x1f: /* 0001 1111 */	f8_inc();			break;

        case 0x20: /* 0010 0000 */  f8_li();            break;
		case 0x21: /* 0010 0001 */	f8_ni();			break;
		case 0x22: /* 0010 0010 */	f8_oi();			break;
		case 0x23: /* 0010 0011 */	f8_xi();			break;
		case 0x24: /* 0010 0100 */	f8_ai();			break;
		case 0x25: /* 0010 0101 */	f8_ci();			break;
		case 0x26: /* 0010 0110 */	f8_in();			break;
		case 0x27: /* 0010 0111 */	f8_out();			break;
		case 0x28: /* 0010 1000 */	f8_pi();			break;
		case 0x29: /* 0010 1001 */	f8_jmp();			break;
		case 0x2a: /* 0010 1010 */	f8_dci();			break;
		case 0x2b: /* 0010 1011 */	f8_nop();			break;
		case 0x2c: /* 0010 1100 */	f8_xdc();			break;
		case 0x2d: /* 0010 1101 */	illegal();			break;
		case 0x2e: /* 0010 1110 */	illegal();			break;
		case 0x2f: /* 0010 1111 */	illegal();			break;

        case 0x30: /* 0011 0000 */  f8_ds_r( 0);        break;
		case 0x31: /* 0011 0001 */	f8_ds_r( 1);		break;
		case 0x32: /* 0011 0010 */	f8_ds_r( 2);		break;
		case 0x33: /* 0011 0011 */	f8_ds_r( 3);		break;
		case 0x34: /* 0011 0100 */	f8_ds_r( 4);		break;
		case 0x35: /* 0011 0101 */	f8_ds_r( 5);		break;
		case 0x36: /* 0011 0110 */	f8_ds_r( 6);		break;
		case 0x37: /* 0011 0111 */	f8_ds_r( 7);		break;
		case 0x38: /* 0011 1000 */	f8_ds_r( 8);		break;
		case 0x39: /* 0011 1001 */	f8_ds_r( 9);		break;
		case 0x3a: /* 0011 1010 */	f8_ds_r(10);		break;
		case 0x3b: /* 0011 1011 */	f8_ds_r(11);		break;
		case 0x3c: /* 0011 1100 */	f8_ds_isar();		break;
		case 0x3d: /* 0011 1101 */	f8_ds_isar_i(); 	break;
		case 0x3e: /* 0011 1110 */	f8_ds_isar_d(); 	break;
		case 0x3f: /* 0011 1111 */	illegal();			break;

        case 0x40: /* 0100 0000 */  f8_lr_a_r( 0);      break;
		case 0x41: /* 0100 0001 */	f8_lr_a_r( 1);		break;
		case 0x42: /* 0100 0010 */	f8_lr_a_r( 2);		break;
		case 0x43: /* 0100 0011 */	f8_lr_a_r( 3);		break;
		case 0x44: /* 0100 0100 */	f8_lr_a_r( 4);		break;
		case 0x45: /* 0100 0101 */	f8_lr_a_r( 5);		break;
		case 0x46: /* 0100 0110 */	f8_lr_a_r( 6);		break;
		case 0x47: /* 0100 0111 */	f8_lr_a_r( 7);		break;
		case 0x48: /* 0100 1000 */	f8_lr_a_r( 8);		break;
		case 0x49: /* 0100 1001 */	f8_lr_a_r( 9);		break;
		case 0x4a: /* 0100 1010 */	f8_lr_a_r(10);		break;
		case 0x4b: /* 0100 1011 */	f8_lr_a_r(11);		break;
		case 0x4c: /* 0100 1100 */	f8_lr_a_isar(); 	break;
		case 0x4d: /* 0100 1101 */	f8_lr_a_isar_i();	break;
		case 0x4e: /* 0100 1110 */	f8_lr_a_isar_d();	break;
		case 0x4f: /* 0100 1111 */	illegal();			break;

		case 0x50: /* 0101 0000 */	f8_lr_r_a( 0);		break;
		case 0x51: /* 0101 0001 */	f8_lr_r_a( 1);		break;
		case 0x52: /* 0101 0010 */	f8_lr_r_a( 2);		break;
		case 0x53: /* 0101 0011 */	f8_lr_r_a( 3);		break;
		case 0x54: /* 0101 0100 */	f8_lr_r_a( 4);		break;
		case 0x55: /* 0101 0101 */	f8_lr_r_a( 5);		break;
		case 0x56: /* 0101 0110 */	f8_lr_r_a( 6);		break;
		case 0x57: /* 0101 0111 */	f8_lr_r_a( 7);		break;
		case 0x58: /* 0101 1000 */	f8_lr_r_a( 8);		break;
		case 0x59: /* 0101 1001 */	f8_lr_r_a( 9);		break;
		case 0x5a: /* 0101 1010 */	f8_lr_r_a(10);		break;
		case 0x5b: /* 0101 1011 */	f8_lr_r_a(11);		break;
		case 0x5c: /* 0101 1100 */	f8_lr_isar_a(); 	break;
		case 0x5d: /* 0101 1101 */	f8_lr_isar_i_a();	break;
		case 0x5e: /* 0101 1110 */	f8_lr_isar_d_a();	break;
		case 0x5f: /* 0101 1111 */	illegal();			break;

		case 0x60: /* 0110 0000 */	f8_lisu(0x00);		break;
		case 0x61: /* 0110 0001 */	f8_lisu(0x08);		break;
		case 0x62: /* 0110 0010 */	f8_lisu(0x10);		break;
		case 0x63: /* 0110 0011 */	f8_lisu(0x18);		break;
		case 0x64: /* 0110 0100 */	f8_lisu(0x20);		break;
		case 0x65: /* 0110 0101 */	f8_lisu(0x28);		break;
		case 0x66: /* 0110 0110 */	f8_lisu(0x30);		break;
		case 0x67: /* 0110 0111 */	f8_lisu(0x38);		break;
		case 0x68: /* 0110 1000 */	f8_lisl(0x00);		break;
		case 0x69: /* 0110 1001 */	f8_lisl(0x01);		break;
		case 0x6a: /* 0110 1010 */	f8_lisl(0x02);		break;
		case 0x6b: /* 0110 1011 */	f8_lisl(0x03);		break;
		case 0x6c: /* 0110 1100 */	f8_lisl(0x04);		break;
		case 0x6d: /* 0110 1101 */	f8_lisl(0x05);		break;
		case 0x6e: /* 0110 1110 */	f8_lisl(0x06);		break;
		case 0x6f: /* 0110 1111 */	f8_lisl(0x07);		break;

		case 0x70: /* 0111 0000 */	f8_lis(0x0);		break;
		case 0x71: /* 0111 0001 */	f8_lis(0x1);		break;
		case 0x72: /* 0111 0010 */	f8_lis(0x2);		break;
		case 0x73: /* 0111 0011 */	f8_lis(0x3);		break;
		case 0x74: /* 0111 0100 */	f8_lis(0x4);		break;
		case 0x75: /* 0111 0101 */	f8_lis(0x5);		break;
		case 0x76: /* 0111 0110 */	f8_lis(0x6);		break;
		case 0x77: /* 0111 0111 */	f8_lis(0x7);		break;
		case 0x78: /* 0111 1000 */	f8_lis(0x8);		break;
		case 0x79: /* 0111 1001 */	f8_lis(0x9);		break;
		case 0x7a: /* 0111 1010 */	f8_lis(0xa);		break;
		case 0x7b: /* 0111 1011 */	f8_lis(0xb);		break;
		case 0x7c: /* 0111 1100 */	f8_lis(0xc);		break;
		case 0x7d: /* 0111 1101 */	f8_lis(0xd);		break;
		case 0x7e: /* 0111 1110 */	f8_lis(0xe);		break;
		case 0x7f: /* 0111 1111 */	f8_lis(0xf);		break;

		case 0x80: /* 1000 0000 */	f8_bt(0);			break;
		case 0x81: /* 1000 0001 */	f8_bt(1);			break;
		case 0x82: /* 1000 0010 */	f8_bt(2);			break;
		case 0x83: /* 1000 0011 */	f8_bt(3);			break;
		case 0x84: /* 1000 0100 */	f8_bt(4);			break;
		case 0x85: /* 1000 0101 */	f8_bt(5);			break;
		case 0x86: /* 1000 0110 */	f8_bt(6);			break;
		case 0x87: /* 1000 0111 */	f8_bt(7);			break;
		case 0x88: /* 1000 1000 */	f8_am();			break;
		case 0x89: /* 1000 1001 */	f8_amd();			break;
		case 0x8a: /* 1000 1010 */	f8_nm();			break;
		case 0x8b: /* 1000 1011 */	f8_om();			break;
		case 0x8c: /* 1000 1100 */	f8_xm();			break;
		case 0x8d: /* 1000 1101 */	f8_cm();			break;
		case 0x8e: /* 1000 1110 */	f8_adc();			break;
		case 0x8f: /* 1000 1111 */	f8_br7();			break;

		case 0x90: /* 1001 0000 */	f8_bf(0x0); 		break;
		case 0x91: /* 1001 0001 */	f8_bf(0x1); 		break;
		case 0x92: /* 1001 0010 */	f8_bf(0x2); 		break;
		case 0x93: /* 1001 0011 */	f8_bf(0x3); 		break;
		case 0x94: /* 1001 0100 */	f8_bf(0x4); 		break;
		case 0x95: /* 1001 0101 */	f8_bf(0x5); 		break;
		case 0x96: /* 1001 0110 */	f8_bf(0x6); 		break;
		case 0x97: /* 1001 0111 */	f8_bf(0x7); 		break;
		case 0x98: /* 1001 1000 */	f8_bf(0x8); 		break;
		case 0x99: /* 1001 1001 */	f8_bf(0x9); 		break;
		case 0x9a: /* 1001 1010 */	f8_bf(0xa); 		break;
		case 0x9b: /* 1001 1011 */	f8_bf(0xb); 		break;
		case 0x9c: /* 1001 1100 */	f8_bf(0xc); 		break;
		case 0x9d: /* 1001 1101 */	f8_bf(0xd); 		break;
		case 0x9e: /* 1001 1110 */	f8_bf(0xe); 		break;
		case 0x9f: /* 1001 1111 */	f8_bf(0xf); 		break;

		case 0xa0: /* 1010 0000 */	f8_ins_0(0x0);		break;
		case 0xa1: /* 1010 0001 */	f8_ins_0(0x1);		break;
		case 0xa2: /* 1010 0010 */	illegal();			break;
		case 0xa3: /* 1010 0011 */	illegal();			break;
		case 0xa4: /* 1010 0100 */	f8_ins_1(0x4);		break;
		case 0xa5: /* 1010 0101 */	f8_ins_1(0x5);		break;
		case 0xa6: /* 1010 0110 */	f8_ins_1(0x6);		break;
		case 0xa7: /* 1010 0111 */	f8_ins_1(0x7);		break;
		case 0xa8: /* 1010 1000 */	f8_ins_1(0x8);		break;
		case 0xa9: /* 1010 1001 */	f8_ins_1(0x9);		break;
		case 0xaa: /* 1010 1010 */	f8_ins_1(0xa);		break;
		case 0xab: /* 1010 1011 */	f8_ins_1(0xb);		break;
		case 0xac: /* 1010 1100 */	f8_ins_1(0xc);		break;
		case 0xad: /* 1010 1101 */	f8_ins_1(0xd);		break;
		case 0xae: /* 1010 1110 */	f8_ins_1(0xe);		break;
		case 0xaf: /* 1010 1111 */	f8_ins_1(0xf);		break;

		case 0xb0: /* 1011 0000 */	f8_outs_0(0x0); 	break;
		case 0xb1: /* 1011 0001 */	f8_outs_0(0x1); 	break;
		case 0xb2: /* 1011 0010 */	illegal();			break;
		case 0xb3: /* 1011 0011 */	illegal();			break;
		case 0xb4: /* 1011 0100 */	f8_outs_1(0x4); 	break;
		case 0xb5: /* 1011 0101 */	f8_outs_1(0x5); 	break;
		case 0xb6: /* 1011 0110 */	f8_outs_1(0x6); 	break;
		case 0xb7: /* 1011 0111 */	f8_outs_1(0x7); 	break;
		case 0xb8: /* 1011 1000 */	f8_outs_1(0x8); 	break;
		case 0xb9: /* 1011 1001 */	f8_outs_1(0x9); 	break;
		case 0xba: /* 1011 1010 */	f8_outs_1(0xa); 	break;
		case 0xbb: /* 1011 1011 */	f8_outs_1(0xb); 	break;
		case 0xbc: /* 1011 1100 */	f8_outs_1(0xc); 	break;
		case 0xbd: /* 1011 1101 */	f8_outs_1(0xd); 	break;
		case 0xbe: /* 1011 1110 */	f8_outs_1(0xe); 	break;
		case 0xbf: /* 1011 1111 */	f8_outs_1(0xf); 	break;

		case 0xc0: /* 1100 0000 */	f8_as(0x0); 		break;
		case 0xc1: /* 1100 0001 */	f8_as(0x1); 		break;
		case 0xc2: /* 1100 0010 */	f8_as(0x2); 		break;
		case 0xc3: /* 1100 0011 */	f8_as(0x3); 		break;
		case 0xc4: /* 1100 0100 */	f8_as(0x4); 		break;
		case 0xc5: /* 1100 0101 */	f8_as(0x5); 		break;
		case 0xc6: /* 1100 0110 */	f8_as(0x6); 		break;
		case 0xc7: /* 1100 0111 */	f8_as(0x7); 		break;
		case 0xc8: /* 1100 1000 */	f8_as(0x8); 		break;
		case 0xc9: /* 1100 1001 */	f8_as(0x9); 		break;
		case 0xca: /* 1100 1010 */	f8_as(0xa); 		break;
		case 0xcb: /* 1100 1011 */	f8_as(0xb); 		break;
		case 0xcc: /* 1100 1100 */	f8_as_isar(); 		break;
		case 0xcd: /* 1100 1101 */	f8_as_isar_i(); 	break;
		case 0xce: /* 1100 1110 */	f8_as_isar_d(); 	break;
		case 0xcf: /* 1100 1111 */	illegal(); 			break;

		case 0xd0: /* 1101 0000 */	f8_asd(0x0);		break;
		case 0xd1: /* 1101 0001 */	f8_asd(0x1);		break;
		case 0xd2: /* 1101 0010 */	f8_asd(0x2);		break;
		case 0xd3: /* 1101 0011 */	f8_asd(0x3);		break;
		case 0xd4: /* 1101 0100 */	f8_asd(0x4);		break;
		case 0xd5: /* 1101 0101 */	f8_asd(0x5);		break;
		case 0xd6: /* 1101 0110 */	f8_asd(0x6);		break;
		case 0xd7: /* 1101 0111 */	f8_asd(0x7);		break;
		case 0xd8: /* 1101 1000 */	f8_asd(0x8);		break;
		case 0xd9: /* 1101 1001 */	f8_asd(0x9);		break;
		case 0xda: /* 1101 1010 */	f8_asd(0xa);		break;
		case 0xdb: /* 1101 1011 */	f8_asd(0xb);		break;
		case 0xdc: /* 1101 1100 */	f8_asd_isar();		break;
		case 0xdd: /* 1101 1101 */	f8_asd_isar_i();	break;
		case 0xde: /* 1101 1110 */	f8_asd_isar_d();	break;
		case 0xdf: /* 1101 1111 */	illegal();			break;

		case 0xe0: /* 1110 0000 */	f8_xs(0x0); 		break;
		case 0xe1: /* 1110 0001 */	f8_xs(0x1); 		break;
		case 0xe2: /* 1110 0010 */	f8_xs(0x2); 		break;
		case 0xe3: /* 1110 0011 */	f8_xs(0x3); 		break;
		case 0xe4: /* 1110 0100 */	f8_xs(0x4); 		break;
		case 0xe5: /* 1110 0101 */	f8_xs(0x5); 		break;
		case 0xe6: /* 1110 0110 */	f8_xs(0x6); 		break;
		case 0xe7: /* 1110 0111 */	f8_xs(0x7); 		break;
		case 0xe8: /* 1110 1000 */	f8_xs(0x8); 		break;
		case 0xe9: /* 1110 1001 */	f8_xs(0x9); 		break;
		case 0xea: /* 1110 1010 */	f8_xs(0xa); 		break;
		case 0xeb: /* 1110 1011 */	f8_xs(0xb); 		break;
		case 0xec: /* 1110 1100 */	f8_xs_isar();		break;
		case 0xed: /* 1110 1101 */	f8_xs_isar_i();		break;
		case 0xee: /* 1110 1110 */	f8_xs_isar_d();		break;
		case 0xef: /* 1110 1111 */	illegal();			break;

		case 0xf0: /* 1111 0000 */	f8_ns(0x0); 		break;
		case 0xf1: /* 1111 0001 */	f8_ns(0x1); 		break;
		case 0xf2: /* 1111 0010 */	f8_ns(0x2); 		break;
		case 0xf3: /* 1111 0011 */	f8_ns(0x3); 		break;
		case 0xf4: /* 1111 0100 */	f8_ns(0x4); 		break;
		case 0xf5: /* 1111 0101 */	f8_ns(0x5); 		break;
		case 0xf6: /* 1111 0110 */	f8_ns(0x6); 		break;
		case 0xf7: /* 1111 0111 */	f8_ns(0x7); 		break;
		case 0xf8: /* 1111 1000 */	f8_ns(0x8); 		break;
		case 0xf9: /* 1111 1001 */	f8_ns(0x9); 		break;
		case 0xfa: /* 1111 1010 */	f8_ns(0xa); 		break;
		case 0xfb: /* 1111 1011 */	f8_ns(0xb); 		break;
		case 0xfc: /* 1111 1100 */	f8_ns_isar();		break;
		case 0xfd: /* 1111 1101 */	f8_ns_isar_i();		break;
		case 0xfe: /* 1111 1110 */	f8_ns_isar_d();		break;
		case 0xff: /* 1111 1111 */	illegal();			break;
        }
	} while( f8_icount > 0 );

	return cycles - f8_icount;
}

/* Get registers, return context size */
unsigned f8_get_context(void *dst)
{
	if( dst )
		memcpy(dst, &f8, sizeof(F8));
	return sizeof(F8);
}

/* Set registers */
void f8_set_context(void *src)
{
	if( src )
		memcpy(&f8, src, sizeof(F8));
}

/* Get program counter */
unsigned f8_get_pc(void)
{
	return (f8.pc0 - 1) & 0xffff;
}

/* Set program counter */
void f8_set_pc(unsigned val)
{
	f8.pc0 = val;
	ROMC_00();
}

/* Get stack pointer */
unsigned f8_get_sp(void)
{
	return f8.pc1;
}

/* Set stack pointer */
void f8_set_sp(unsigned val)
{
	f8.pc1 = val;
}


WRITE_HANDLER( f8_internal_w )
{
    f8.r[ offset & 0x3f ] = data;
}

READ_HANDLER( f8_internal_r )
{
    return f8.r[ offset & 0x3f ];
}

unsigned f8_get_reg(int regnum)
{
	switch( regnum )
	{
	case F8_PC0: return f8.pc0;
	case F8_PC1: return f8.pc1;
	case F8_DC0: return f8.dc0;
	case F8_DC1: return f8.dc1;
    case F8_W:   return f8.w;
	case F8_A:	 return f8.a;
	case F8_IS:  return f8.is;
	case F8_J:	 return f8.r[ 9];
	case F8_HU:  return f8.r[10];
	case F8_HL:  return f8.r[11];
	case F8_KU:  return f8.r[12];
	case F8_KL:  return f8.r[13];
	case F8_QU:  return f8.r[14];
	case F8_QL:  return f8.r[15];
	}
	return 0;
}

void f8_set_reg (int regnum, unsigned val)
{
	switch( regnum )
	{
	case F8_PC0: f8.pc0 = val; break;
	case F8_PC1: f8.pc1 = val; break;
	case F8_DC0: f8.dc0 = val; break;
	case F8_DC1: f8.dc1 = val; break;
	case F8_W:	 f8.w = val; break;
	case F8_A:	 f8.a = val; break;
	case F8_IS:  f8.is = val & 0x3f; break;
	case F8_J:	 f8.r[ 9] = val; break;
	case F8_HU:  f8.r[10] = val; break;
	case F8_HL:  f8.r[11] = val; break;
	case F8_KU:  f8.r[12] = val; break;
	case F8_KL:  f8.r[13] = val; break;
	case F8_QU:  f8.r[14] = val; break;
	case F8_QL:  f8.r[15] = val; break;
    }
}

void f8_set_nmi_line(int state)
{
	/* not applicable */
}

void f8_set_irq_line(int irqline, int state)
{
	switch( irqline )
	{
	}
}

void f8_set_irq_callback(int (*callback)(int irqline))
{
	f8.irq_callback = callback;
}

void f8_state_save(void *file)
{
}

void f8_state_load(void *file)
{
}

const char *f8_info(void *context, int regnum)
{
	static char buffer[8][15+1];
	static int which = 0;
	F8 *r = context;

	which = ++which % 8;
	buffer[which][0] = '\0';
	if( !context )
		r = &f8;

    switch( regnum )
	{
		case CPU_INFO_REG+F8_PC0:sprintf(buffer[which], "PC0:%04X", r->pc0); break;
		case CPU_INFO_REG+F8_PC1:sprintf(buffer[which], "PC1:%04X", r->pc1); break;
		case CPU_INFO_REG+F8_DC0:sprintf(buffer[which], "DC0:%04X", r->dc0); break;
		case CPU_INFO_REG+F8_DC1:sprintf(buffer[which], "DC1:%04X", r->dc1); break;
        case CPU_INFO_REG+F8_W:  sprintf(buffer[which], "W  :%02X", r->w); break;
		case CPU_INFO_REG+F8_A:  sprintf(buffer[which], "A  :%02X", r->a); break;
		case CPU_INFO_REG+F8_IS: sprintf(buffer[which], "IS :%02X", r->is); break;
		case CPU_INFO_REG+F8_J:  sprintf(buffer[which], "J  :%02X", r->r[9]); break;
		case CPU_INFO_REG+F8_HU: sprintf(buffer[which], "HU :%02X", r->r[10]); break;
		case CPU_INFO_REG+F8_HL: sprintf(buffer[which], "HL :%02X", r->r[11]); break;
        case CPU_INFO_REG+F8_KU: sprintf(buffer[which], "KU :%02X", r->r[12]); break;
		case CPU_INFO_REG+F8_KL: sprintf(buffer[which], "KL :%02X", r->r[13]); break;
		case CPU_INFO_REG+F8_QU: sprintf(buffer[which], "QU :%02X", r->r[14]); break;
		case CPU_INFO_REG+F8_QL: sprintf(buffer[which], "QL :%02X", r->r[15]); break;
        case CPU_INFO_FLAGS:
			sprintf(buffer[which], "%c%c%c%c%c",
				r->w & 0x10 ? 'I':'.',
				r->w & 0x08 ? 'O':'.',
				r->w & 0x04 ? 'Z':'.',
				r->w & 0x02 ? 'C':'.',
				r->w & 0x01 ? 'S':'.');
			break;
		case CPU_INFO_NAME: return "F8";
		case CPU_INFO_FAMILY: return "Fairchild F8";
		case CPU_INFO_VERSION: return "1.0";
		case CPU_INFO_FILE: return __FILE__;
		case CPU_INFO_CREDITS: return "Copyright (c) 2000 Juergen Buchmueller, all rights reserved.";
		case CPU_INFO_REG_LAYOUT: return (const char*)f8_reg_layout;
		case CPU_INFO_WIN_LAYOUT: return (const char*)f8_win_layout;
	}
    return buffer[which];
}

unsigned f8_dasm(char *buffer, unsigned pc)
{
#ifdef MAME_DEBUG
	return DasmF8( buffer, pc );
#else
	sprintf( buffer, "$%02X", cpu_readop(pc) );
	return 1;
#endif
}

