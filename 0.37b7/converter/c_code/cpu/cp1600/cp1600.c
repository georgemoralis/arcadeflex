/*****************************************************************************
 *
 *	 cp1600.c
 *	 Portable CP1600 emulator (General Instrument CP1600)
 *
 *	 Copyright (c) 2000 Frank Palazzolo, all rights reserved.
 *
 *	 - This source code is released as freeware for non-commercial purposes.
 *	 - You are free to use and redistribute this code in modified or
 *	   unmodified form, provided you list me in the credits.
 *	 - If you modify this source code, you must add a notice to each modified
 *	   source file that it has been changed.  If you're a nice person, you
 *	   will clearly mark each change too.  :)
 *	 - If you wish to use this for commercial purposes, please contact me at
 *	   palazzol@home.com
 *	 - The author of this copywritten work reserves the right to change the
 *     terms of its usage and license at any time, including retroactively
 *   - This entire notice must remain in the source code.
 *
 *	This work is based on Juergen Buchmueller's F8 emulation,
 *  and the 'General Instruments CP1600' data sheets.
 *  Special thanks to Joe Zbiciak for his GPL'd CP1600 emulator
 *
 *****************************************************************************/

#include <stdio.h>
#include "driver.h"
#include "state.h"
#include "mamedbg.h"
#include "cp1600.h"

#define S  0x80
#define Z  0x40
#define OV 0x20
#define C  0x10

typedef struct {
	UINT16	r[8];	/* registers */
	UINT8	flags;	/* flags */
	UINT16	dbus;	/* data bus value */
	UINT16	io; 	/* last I/O address */
    UINT16  irq_vector;
	int 	(*irq_callback)(int irqline);
}	CP1600;

int cp1600_icount;

static CP1600 cp1600;

/* Layout of the registers in the debugger */
static UINT8 cp1600_reg_layout[] = {
	CP1600_R0, CP1600_R1, CP1600_R2, CP1600_R3, -1,
	CP1600_R4, CP1600_R5, CP1600_R6, CP1600_R7, 0
};

/* Layout of the debugger windows x,y,w,h */
static UINT8 cp1600_win_layout[] = {
	 0, 0,80, 2,	/* register window (top rows) */
	 0, 3,24,19,	/* disassembler window (left colums) */
	25, 3,55, 9,	/* memory #1 window (right, upper middle) */
	25,13,55, 9,	/* memory #2 window (right, lower middle) */
     0,23,80, 1,    /* command line window (bottom rows) */
};

/* clear all flags */
#define CLR_OZCS                \
	cp1600.flags &= ~(S|Z|C|OV)

/* clear sign and zero flags */
#define CLR_SZ                	\
	cp1600.flags &= ~(S|Z)

/* clear sign,zero, and carry flags */
#define CLR_SZC                	\
	cp1600.flags &= ~(S|Z|C)

/* set sign and zero flags */
#define SET_SZ(n)               \
	if (n == 0) 				\
		cp1600.flags |= Z;		\
	else						\
	if (n >= 128)				\
		cp1600.flags |= S

/* set sign zero, and carry flags */
#define SET_SZC(n)              \
	if (n == 0) 				\
		cp1600.flags |= Z;		\
	else						\
	if (n >= 128)				\
		cp1600.flags |= S;		\
	if (n + m > 255)			\
		cp1600.flags |= C

/* set carry and overflow flags */
#define SET_COV(n,m)            \
	if (n + m > 255)			\
		cp1600.flags |= C;		\
	if ((n&127)+(m&127) > 127)	\
	{							\
		if (!(cp1600.flags & C))\
			cp1600.flags |= OV;	\
	}							\
	else						\
	{							\
		if (cp1600.flags & C)	\
			cp1600.flags |= OV;	\
	}

/***********************************
 *	illegal opcodes
 ***********************************/
static void illegal(void)
{
	logerror("cp1600 illegal opcode at 0x%04x: %02x\n", cp1600.r[0], cp1600.dbus);
}

/***************************************************
 *	S Z C OV 0 000 000 000
 *	- - - -  HLT
 ***************************************************/
static void cp1600_hlt(void)
{
}

/***************************************************
 *	S Z C OV 0 000 000 001
 *	- - - -  SDBD
 ***************************************************/
static void cp1600_sdbd(void)
{
}

/***************************************************
 *	S Z C OV 0 000 000 010
 *	- - - -  EIS
 ***************************************************/
static void cp1600_eis(void)
{
}

/***************************************************
 *	S Z C OV 0 000 000 011
 *	- - - -  DIS
 ***************************************************/
static void cp1600_dis(void)
{
}
/***************************************************
 *	S Z C OV 0 000 000 100
 *	- - - -  JUMP
 ***************************************************/
static void cp1600_jump(void)
{
}

/***************************************************
 *	S Z C OV 0 000 000 101
 *	- - - -  TCI
 ***************************************************/
static void cp1600_tci(void)
{
}

/***************************************************
 *	S Z C OV 0 000 000 110
 *	- - 0 -  CLRC
 ***************************************************/
static void cp1600_clrc(void)
{
	cp1600.flags &= ~C;
}

/***************************************************
 *	S Z C OV 0 000 000 111
 *	- - 1 -  SETC
 ***************************************************/
static void cp1600_setc(void)
{
	cp1600.flags |= C;
}

/***************************************************
 *	S Z C OV 0 000 001 ddd
 *	x x - -  INCR  Rd
 ***************************************************/
static void cp1600_incr(int n)
{
	cp1600.r[n]++;
	CLR_SZ;
	SET_SZ(cp1600.r[n]);
}

/***************************************************
 *	S Z C OV 0 000 010 ddd
 *	x x - -  DECR  Rd
 ***************************************************/
static void cp1600_decr(int n)
{
	cp1600.r[n]--;
	CLR_SZ;
	SET_SZ(cp1600.r[n]);
}

/***************************************************
 *	S Z C OV 0 000 011 ddd
 *	x x - -  COMR  Rd
 ***************************************************/
static void cp1600_comr(int n)
{
	cp1600.r[n] ^= 0xff;
	CLR_SZ;
	SET_SZ(cp1600.r[n]);
}

/***************************************************
 *	S Z C OV 0 000 100 ddd
 *	x x x x  NEGR  Rd
 ***************************************************/
static void cp1600_negr(int n)
{
}

/***************************************************
 *	S Z C OV 0 000 101 ddd
 *	x x x x  ADCR  Rd
 ***************************************************/
static void cp1600_adcr(int n)
{
}

/***************************************************
 *	S Z C OV 0 000 110 0dd
 *	- - - -  GSWD  Rd
 ***************************************************/
static void cp1600_gswd(int n)
{
}

/***************************************************
 *	S Z C OV 0 000 110 10x
 *	- - - -  NOP
 ***************************************************/
static void cp1600_nop(void)
{
}

/***************************************************
 *	S Z C OV 0 000 110 11x
 *	- - - -  SIN
 ***************************************************/
static void cp1600_sin(void)
{
}

/***************************************************
 *	S Z C OV 0 000 111 sss
 *	x x x x  RSWD Rs
 ***************************************************/
static void cp1600_rswd(int n)
{
}

/***************************************************
 *	S Z C OV 0 001 000 nrr
 *	x x - -  SWAP Rr,n+1
 ***************************************************/
static void cp1600_swap(int r, int n)
{
}

/***************************************************
 *	S Z C OV 0 001 001 nrr
 *	x x - -  SLL Rr,n+1
 ***************************************************/
static void cp1600_sll(int r, int n)
{
}

/***************************************************
 *	S Z C OV 0 001 010 nrr
 *	x x x -x RLC Rr,n+1
 ***************************************************/
static void cp1600_rlc(int r, int n)
{
}

/***************************************************
 *	S Z C OV 0 001 011 nrr
 *	x x x -x SLLC Rr,n+1
 ***************************************************/
static void cp1600_sllc(int r, int n)
{
}

/***************************************************
 *	S Z C OV 0 001 100 nrr
 *	x x - -  SLR Rr,n+1
 ***************************************************/
static void cp1600_slr(int r, int n)
{
}

/***************************************************
 *	S Z C OV 0 001 101 nrr
 *	x x - -  SAR Rr,n+1
 ***************************************************/
static void cp1600_sar(int r, int n)
{
}

/***************************************************
 *	S Z C OV 0 001 110 nrr
 *	x x x -x RRC Rr,n+1
 ***************************************************/
static void cp1600_rrc(int r, int n)
{
}

/***************************************************
 *	S Z C OV 0 001 111 nrr
 *	x x x -x SARC Rr,n+1
 ***************************************************/
static void cp1600_sarc(int r, int n)
{
}

/***************************************************
 *	S Z C OV 0 010 sss sss
 *	x x - -  TSTR Rs
 ***************************************************/
static void cp1600_tstr(int n)
{
}

/***************************************************
 *	S Z C OV 0 010 sss ddd		(sss != ddd)
 *	x x - -  MOVR Rs,Rd
 ***************************************************/
static void cp1600_movr(int s, int d)
{
}

/***************************************************
 *	S Z C OV 0 010 sss 111
 *	x x - -  JR Rs
 ***************************************************/
static void cp1600_jr(int s)
{
}

/***************************************************
 *	S Z C OV 0 011 sss ddd
 *	x x x x  ADDR Rs, Rd
 ***************************************************/
static void cp1600_addr(int s, int d)
{
}

/***************************************************
 *	S Z C OV 0 100 sss ddd
 *	x x x x  SUBR Rs, Rd
 ***************************************************/
static void cp1600_subr(int s, int d)
{
}

/***************************************************
 *	S Z C OV 0 101 sss ddd
 *	x x x x  CMPR Rs, Rd
 ***************************************************/
static void cp1600_cmpr(int s, int d)
{
}

/***************************************************
 *	S Z C OV 0 110 sss ddd
 *	x x - -  ANDR Rs, Rd
 ***************************************************/
static void cp1600_andr(int s, int d)
{
}

/***************************************************
 *	S Z C OV 0 111 sss ddd
 *	x x - -  XORR Rs, Rd
 ***************************************************/
static void cp1600_xorr(int s, int d)
{
}

/***************************************************
 *	S Z C OV 0 111 ddd ddd
 *	x x - -  CLRR Rd
 ***************************************************/
static void cp1600_clrr(int d)
{
}

/***************************************************
 *	S Z C OV 1 000 xxx xxx
 *	- - - -  BRANCH
 ***************************************************/
static void cp1600_branch(void)
{
}

/***************************************************
 *	S Z C OV 1 001 000 sss a aaa aaa aaa aaa aaa
 *	- - - -  MVO Rs, ADDR
 ***************************************************/
static void cp1600_mvo(int s)
{
}

/***************************************************
 *	S Z C OV 1 001 mmm sss
 *	- - - -  MVO@ Rs, Rm
 ***************************************************/
static void cp1600_mvoat(int s, int m)
{
}

/***************************************************
 *	S Z C OV 1 001 110 sss
 *	- - - -  PSHR Rs
 ***************************************************/
static void cp1600_pshr(int s)
{
}

/***************************************************
 *	S Z C OV 1 001 111 sss I III III III III III
 *	- - - -  MVOI Rs, II
 ***************************************************/
static void cp1600_mvoi(int s)
{
}

/***************************************************
 *	S Z C OV 1 010 000 ddd a aaa aaa aaa aaa aaa
 *	- - - -  MVI ADDR, Rd
 ***************************************************/
static void cp1600_mvi(int d)
{
}

/***************************************************
 *	S Z C OV 1 010 mmm ddd
 *	- - - -  MVI@ Rm, Rd
 ***************************************************/
static void cp1600_mviat(int m, int d)
{
}

/***************************************************
 *	S Z C OV 1 010 110 ddd
 *	- - - -  PULR Rd
 ***************************************************/
static void cp1600_pulr(int d)
{
}

/***************************************************
 *	S Z C OV 1 010 111 ddd I III III III III III
 *	- - - -  MVII II, Rd
 ***************************************************/
static void cp1600_mvii(int d)
{
}

/***************************************************
 *	S Z C OV 1 011 000 ddd a aaa aaa aaa aaa aaa
 *	x x x x  ADD ADDR, Rd
 ***************************************************/
static void cp1600_add(int d)
{
}

/***************************************************
 *	S Z C OV 1 011 mmm ddd
 *	x x x x  ADD@ Rm, Rd
 ***************************************************/
static void cp1600_addat(int m, int d)
{
}

/***************************************************
 *	S Z C OV 1 011 111 ddd I III III III III III
 *	x x x x  ADDI II, Rd
 ***************************************************/
static void cp1600_addi(int d)
{
}

/***************************************************
 *	S Z C OV 1 100 000 ddd a aaa aaa aaa aaa aaa
 *	x x x x  SUB ADDR, Rd
 ***************************************************/
static void cp1600_sub(int d)
{
}

/***************************************************
 *	S Z C OV 1 100 mmm ddd
 *	x x x x  SUB@ Rm, Rd
 ***************************************************/
static void cp1600_subat(int m, int d)
{
}

/***************************************************
 *	S Z C OV 1 100 111 ddd I III III III III III
 *	x x x x  SUBI II, Rd
 ***************************************************/
static void cp1600_subi(int d)
{
}

/***************************************************
 *	S Z C OV 1 101 000 ddd a aaa aaa aaa aaa aaa
 *	x x x x  CMP ADDR, Rd
 ***************************************************/
static void cp1600_cmp(int d)
{
}

/***************************************************
 *	S Z C OV 1 101 mmm ddd
 *	x x x x  CMP@ Rm, Rd
 ***************************************************/
static void cp1600_cmpat(int m, int d)
{
}

/***************************************************
 *	S Z C OV 1 101 111 ddd I III III III III III
 *	x x x x  CMPI II, Rd
 ***************************************************/
static void cp1600_cmpi(int d)
{
}

/***************************************************
 *	S Z C OV 1 110 000 ddd a aaa aaa aaa aaa aaa
 *	x x - -  AND ADDR, Rd
 ***************************************************/
static void cp1600_and(int d)
{
}

/***************************************************
 *	S Z C OV 1 110 mmm ddd
 *	x x - -  AND@ Rm, Rd
 ***************************************************/
static void cp1600_andat(int m, int d)
{
}

/***************************************************
 *	S Z C OV 1 110 111 ddd I III III III III III
 *	x x - -  AND II, Rd
 ***************************************************/
static void cp1600_andi(int d)
{
}

/***************************************************
 *	S Z C OV 1 111 000 ddd a aaa aaa aaa aaa aaa
 *	x x - -  XOR ADDR, Rd
 ***************************************************/
static void cp1600_xor(int d)
{
}

/***************************************************
 *	S Z C OV 1 111 mmm ddd
 *	x x - -  XOR@ Rm, Rd
 ***************************************************/
static void cp1600_xorat(int m, int d)
{
}

/***************************************************
 *	S Z C OV 1 111 111 ddd I III III III III III
 *	x x - -  XOR II, Rd
 ***************************************************/
static void cp1600_xori(int d)
{
}

void cp1600_reset(void *param)
{
	/* ready to fetch the first opcode */
	cp1600.r[7] = 0x1000; /* This actually comes from a ROM chip */
}

/* Shut down CPU core */
void cp1600_exit(void)
{
	/* nothing to do */
}

/* Execute cycles - returns number of cycles actually run */
int cp1600_execute(int cycles)
{
	cp1600_icount = cycles;

    do
    {
        CALL_MAME_DEBUG;

		switch( cp1600.dbus )
        {
		/* opcode  bitmask */
		case 0x000: /* 0 000 000 000 */	cp1600_hlt();		break;
		case 0x001: /* 0 000 000 001 */	cp1600_sdbd();		break;
		case 0x002: /* 0 000 000 010 */	cp1600_eis();		break;
		case 0x003: /* 0 000 000 011 */	cp1600_dis();		break;
		case 0x004: /* 0 000 000 100 */	cp1600_jump();		break;
		case 0x005: /* 0 000 000 101 */	cp1600_tci();		break;
		case 0x006: /* 0 000 000 110 */	cp1600_clrc();		break;
		case 0x007: /* 0 000 000 111 */	cp1600_setc();		break;

		case 0x008: /* 0 000 001 000 */	cp1600_incr(0);		break;
		case 0x009: /* 0 000 001 001 */	cp1600_incr(1);		break;
		case 0x00a: /* 0 000 001 010 */	cp1600_incr(2);		break;
		case 0x00b: /* 0 000 001 011 */	cp1600_incr(3);		break;
		case 0x00c: /* 0 000 001 100 */	cp1600_incr(4);		break;
		case 0x00d: /* 0 000 001 101 */	cp1600_incr(5);		break;
		case 0x00e: /* 0 000 001 110 */	cp1600_incr(6);		break;
		case 0x00f: /* 0 000 001 111 */	cp1600_incr(7);		break;

		case 0x010: /* 0 000 010 000 */	cp1600_decr(0);		break;
		case 0x011: /* 0 000 010 001 */	cp1600_decr(1);		break;
		case 0x012: /* 0 000 010 010 */	cp1600_decr(2);		break;
		case 0x013: /* 0 000 010 011 */	cp1600_decr(3);		break;
		case 0x014: /* 0 000 010 100 */	cp1600_decr(4);		break;
		case 0x015: /* 0 000 010 101 */	cp1600_decr(5);		break;
		case 0x016: /* 0 000 010 110 */	cp1600_decr(6);		break;
		case 0x017: /* 0 000 010 111 */	cp1600_decr(7);		break;

		case 0x018: /* 0 000 011 000 */	cp1600_comr(0);		break;
		case 0x019: /* 0 000 011 001 */	cp1600_comr(1);		break;
		case 0x01a: /* 0 000 011 010 */	cp1600_comr(2);		break;
		case 0x01b: /* 0 000 011 011 */	cp1600_comr(3);		break;
		case 0x01c: /* 0 000 011 100 */	cp1600_comr(4);		break;
		case 0x01d: /* 0 000 011 101 */	cp1600_comr(5);		break;
		case 0x01e: /* 0 000 011 110 */	cp1600_comr(6);		break;
		case 0x01f: /* 0 000 011 111 */	cp1600_comr(7);		break;

		case 0x020: /* 0 000 100 000 */	cp1600_negr(0);		break;
		case 0x021: /* 0 000 100 001 */	cp1600_negr(1);		break;
		case 0x022: /* 0 000 100 010 */	cp1600_negr(2);		break;
		case 0x023: /* 0 000 100 011 */	cp1600_negr(3);		break;
		case 0x024: /* 0 000 100 100 */	cp1600_negr(4);		break;
		case 0x025: /* 0 000 100 101 */	cp1600_negr(5);		break;
		case 0x026: /* 0 000 100 110 */	cp1600_negr(6);		break;
		case 0x027: /* 0 000 100 111 */	cp1600_negr(7);		break;

		case 0x028: /* 0 000 101 000 */	cp1600_adcr(0);		break;
		case 0x029: /* 0 000 101 001 */	cp1600_adcr(1);		break;
		case 0x02a: /* 0 000 101 010 */	cp1600_adcr(2);		break;
		case 0x02b: /* 0 000 101 011 */	cp1600_adcr(3);		break;
		case 0x02c: /* 0 000 101 100 */	cp1600_adcr(4);		break;
		case 0x02d: /* 0 000 101 101 */	cp1600_adcr(5);		break;
		case 0x02e: /* 0 000 101 110 */	cp1600_adcr(6);		break;
		case 0x02f: /* 0 000 101 111 */	cp1600_adcr(7);		break;

		case 0x030: /* 0 000 110 000 */ cp1600_gswd(0);		break;
		case 0x031: /* 0 000 110 001 */ cp1600_gswd(1);		break;
		case 0x032: /* 0 000 110 010 */ cp1600_gswd(2);		break;
		case 0x033: /* 0 000 110 011 */ cp1600_gswd(3);		break;
		case 0x034: /* 0 000 110 100 */ cp1600_nop();		break;
		case 0x035: /* 0 000 110 101 */ cp1600_nop();		break;
		case 0x036: /* 0 000 110 110 */ cp1600_sin();		break;
		case 0x037: /* 0 000 110 111 */ cp1600_sin();		break;

		case 0x038: /* 0 000 111 000 */	cp1600_rswd(0);		break;
		case 0x039: /* 0 000 111 001 */	cp1600_rswd(1);		break;
		case 0x03a: /* 0 000 111 010 */	cp1600_rswd(2);		break;
		case 0x03b: /* 0 000 111 011 */	cp1600_rswd(3);		break;
		case 0x03c: /* 0 000 111 100 */	cp1600_rswd(4);		break;
		case 0x03d: /* 0 000 111 101 */	cp1600_rswd(5);		break;
		case 0x03e: /* 0 000 111 110 */	cp1600_rswd(6);		break;
		case 0x03f: /* 0 000 111 111 */	cp1600_rswd(7);		break;

		case 0x040: /* 0 001 000 000 */ cp1600_swap(0,1);	break;
		case 0x041: /* 0 001 000 001 */ cp1600_swap(1,1);	break;
		case 0x042: /* 0 001 000 010 */ cp1600_swap(2,1);	break;
		case 0x043: /* 0 001 000 011 */ cp1600_swap(3,1);	break;
		case 0x044: /* 0 001 000 100 */ cp1600_swap(0,2);	break;
		case 0x045: /* 0 001 000 101 */ cp1600_swap(1,2);	break;
		case 0x046: /* 0 001 000 110 */ cp1600_swap(2,2);	break;
		case 0x047: /* 0 001 000 111 */ cp1600_swap(3,2);	break;

		case 0x048: /* 0 001 001 000 */ cp1600_sll(0,1);	break;
		case 0x049: /* 0 001 001 001 */ cp1600_sll(1,1);	break;
		case 0x04a: /* 0 001 001 010 */ cp1600_sll(2,1);	break;
		case 0x04b: /* 0 001 001 011 */ cp1600_sll(3,1);	break;
		case 0x04c: /* 0 001 001 100 */ cp1600_sll(0,2);	break;
		case 0x04d: /* 0 001 001 101 */ cp1600_sll(1,2);	break;
		case 0x04e: /* 0 001 001 110 */ cp1600_sll(2,2);	break;
		case 0x04f: /* 0 001 001 111 */ cp1600_sll(3,2);	break;

		case 0x050: /* 0 001 010 000 */ cp1600_rlc(0,1);	break;
		case 0x051: /* 0 001 010 001 */ cp1600_rlc(1,1);	break;
		case 0x052: /* 0 001 010 010 */ cp1600_rlc(2,1);	break;
		case 0x053: /* 0 001 010 011 */ cp1600_rlc(3,1);	break;
		case 0x054: /* 0 001 010 100 */ cp1600_rlc(0,2);	break;
		case 0x055: /* 0 001 010 101 */ cp1600_rlc(1,2);	break;
		case 0x056: /* 0 001 010 110 */ cp1600_rlc(2,2);	break;
		case 0x057: /* 0 001 010 111 */ cp1600_rlc(3,2);	break;

		case 0x058: /* 0 001 011 000 */ cp1600_sllc(0,1);	break;
		case 0x059: /* 0 001 011 001 */ cp1600_sllc(1,1);	break;
		case 0x05a: /* 0 001 011 010 */ cp1600_sllc(2,1);	break;
		case 0x05b: /* 0 001 011 011 */ cp1600_sllc(3,1);	break;
		case 0x05c: /* 0 001 011 100 */ cp1600_sllc(0,2);	break;
		case 0x05d: /* 0 001 011 101 */ cp1600_sllc(1,2);	break;
		case 0x05e: /* 0 001 011 110 */ cp1600_sllc(2,2);	break;
		case 0x05f: /* 0 001 011 111 */ cp1600_sllc(3,2);	break;

		case 0x060: /* 0 001 100 000 */ cp1600_slr(0,1);	break;
		case 0x061: /* 0 001 100 001 */ cp1600_slr(1,1);	break;
		case 0x062: /* 0 001 100 010 */ cp1600_slr(2,1);	break;
		case 0x063: /* 0 001 100 011 */ cp1600_slr(3,1);	break;
		case 0x064: /* 0 001 100 100 */ cp1600_slr(0,2);	break;
		case 0x065: /* 0 001 100 101 */ cp1600_slr(1,2);	break;
		case 0x066: /* 0 001 100 110 */ cp1600_slr(2,2);	break;
		case 0x067: /* 0 001 100 111 */ cp1600_slr(3,2);	break;

		case 0x068: /* 0 001 101 000 */ cp1600_sar(0,1);	break;
		case 0x069: /* 0 001 101 001 */ cp1600_sar(1,1);	break;
		case 0x06a: /* 0 001 101 010 */ cp1600_sar(2,1);	break;
		case 0x06b: /* 0 001 101 011 */ cp1600_sar(3,1);	break;
		case 0x06c: /* 0 001 101 100 */ cp1600_sar(0,2);	break;
		case 0x06d: /* 0 001 101 101 */ cp1600_sar(1,2);	break;
		case 0x06e: /* 0 001 101 110 */ cp1600_sar(2,2);	break;
		case 0x06f: /* 0 001 101 111 */ cp1600_sar(3,2);	break;

		case 0x070: /* 0 001 110 000 */ cp1600_rrc(0,1);	break;
		case 0x071: /* 0 001 110 001 */ cp1600_rrc(1,1);	break;
		case 0x072: /* 0 001 110 010 */ cp1600_rrc(2,1);	break;
		case 0x073: /* 0 001 110 011 */ cp1600_rrc(3,1);	break;
		case 0x074: /* 0 001 110 100 */ cp1600_rrc(0,2);	break;
		case 0x075: /* 0 001 110 101 */ cp1600_rrc(1,2);	break;
		case 0x076: /* 0 001 110 110 */ cp1600_rrc(2,2);	break;
		case 0x077: /* 0 001 110 111 */ cp1600_rrc(3,2);	break;

		case 0x078: /* 0 001 111 000 */ cp1600_sarc(0,1);	break;
		case 0x079: /* 0 001 111 001 */ cp1600_sarc(1,1);	break;
		case 0x07a: /* 0 001 111 010 */ cp1600_sarc(2,1);	break;
		case 0x07b: /* 0 001 111 011 */ cp1600_sarc(3,1);	break;
		case 0x07c: /* 0 001 111 100 */ cp1600_sarc(0,2);	break;
		case 0x07d: /* 0 001 111 101 */ cp1600_sarc(1,2);	break;
		case 0x07e: /* 0 001 111 110 */ cp1600_sarc(2,2);	break;
		case 0x07f: /* 0 001 111 111 */ cp1600_sarc(3,2);	break;

		case 0x080: /* 0 010 000 000 */	cp1600_tstr(0);		break;
		case 0x081: /* 0 010 000 001 */	cp1600_movr(0,1);	break;
		case 0x082: /* 0 010 000 010 */	cp1600_movr(0,2);	break;
		case 0x083: /* 0 010 000 011 */	cp1600_movr(0,3);	break;
		case 0x084: /* 0 010 000 100 */	cp1600_movr(0,4);	break;
		case 0x085: /* 0 010 000 101 */	cp1600_movr(0,5);	break;
		case 0x086: /* 0 010 000 110 */	cp1600_movr(0,6);	break;
		case 0x087: /* 0 010 000 111 */	cp1600_jr(0);		break;

		case 0x088: /* 0 010 001 000 */	cp1600_movr(1,0);	break;
		case 0x089: /* 0 010 001 001 */	cp1600_tstr(1);		break;
		case 0x08a: /* 0 010 001 010 */	cp1600_movr(1,2);	break;
		case 0x08b: /* 0 010 001 011 */	cp1600_movr(1,3);	break;
		case 0x08c: /* 0 010 001 100 */	cp1600_movr(1,4);	break;
		case 0x08d: /* 0 010 001 101 */	cp1600_movr(1,5);	break;
		case 0x08e: /* 0 010 001 110 */	cp1600_movr(1,6);	break;
		case 0x08f: /* 0 010 001 111 */	cp1600_jr(1);		break;

		case 0x090: /* 0 010 010 000 */	cp1600_movr(2,0);	break;
		case 0x091: /* 0 010 010 001 */	cp1600_movr(2,1);	break;
		case 0x092: /* 0 010 010 010 */	cp1600_tstr(2);		break;
		case 0x093: /* 0 010 010 011 */	cp1600_movr(2,3);	break;
		case 0x094: /* 0 010 010 100 */	cp1600_movr(2,4);	break;
		case 0x095: /* 0 010 010 101 */	cp1600_movr(2,5);	break;
		case 0x096: /* 0 010 010 110 */	cp1600_movr(2,6);	break;
		case 0x097: /* 0 010 010 111 */	cp1600_jr(2);		break;

		case 0x098: /* 0 010 011 000 */	cp1600_movr(3,0);	break;
		case 0x099: /* 0 010 011 001 */	cp1600_movr(3,1);	break;
		case 0x09a: /* 0 010 011 010 */	cp1600_movr(3,2);	break;
		case 0x09b: /* 0 010 011 011 */	cp1600_tstr(3);		break;
		case 0x09c: /* 0 010 011 100 */	cp1600_movr(3,4);	break;
		case 0x09d: /* 0 010 011 101 */	cp1600_movr(3,5);	break;
		case 0x09e: /* 0 010 011 110 */	cp1600_movr(3,6);	break;
		case 0x09f: /* 0 010 011 111 */	cp1600_jr(3);		break;

		case 0x0a0: /* 0 010 100 000 */	cp1600_movr(4,0);	break;
		case 0x0a1: /* 0 010 100 001 */	cp1600_movr(4,1);	break;
		case 0x0a2: /* 0 010 100 010 */	cp1600_movr(4,2);	break;
		case 0x0a3: /* 0 010 100 011 */	cp1600_movr(4,3);	break;
		case 0x0a4: /* 0 010 100 100 */	cp1600_tstr(4);		break;
		case 0x0a5: /* 0 010 100 101 */	cp1600_movr(4,5);	break;
		case 0x0a6: /* 0 010 100 110 */	cp1600_movr(4,6);	break;
		case 0x0a7: /* 0 010 100 111 */	cp1600_jr(4);		break;

		case 0x0a8: /* 0 010 101 000 */	cp1600_movr(5,0);	break;
		case 0x0a9: /* 0 010 101 001 */	cp1600_movr(5,1);	break;
		case 0x0aa: /* 0 010 101 010 */	cp1600_movr(5,2);	break;
		case 0x0ab: /* 0 010 101 011 */	cp1600_movr(5,3);	break;
		case 0x0ac: /* 0 010 101 100 */	cp1600_movr(5,4);	break;
		case 0x0ad: /* 0 010 101 101 */	cp1600_tstr(5);		break;
		case 0x0ae: /* 0 010 101 110 */	cp1600_movr(5,6);	break;
		case 0x0af: /* 0 010 101 111 */	cp1600_jr(3);		break;

		case 0x0b0: /* 0 010 110 000 */	cp1600_movr(6,0);	break;
		case 0x0b1: /* 0 010 110 001 */	cp1600_movr(6,1);	break;
		case 0x0b2: /* 0 010 110 010 */	cp1600_movr(6,2);	break;
		case 0x0b3: /* 0 010 110 011 */	cp1600_movr(6,3);	break;
		case 0x0b4: /* 0 010 110 100 */	cp1600_movr(6,4);	break;
		case 0x0b5: /* 0 010 110 101 */	cp1600_movr(6,5);	break;
		case 0x0b6: /* 0 010 110 110 */	cp1600_tstr(6);		break;
		case 0x0b7: /* 0 010 110 111 */	cp1600_jr(4);		break;

		case 0x0b8: /* 0 010 111 000 */	cp1600_movr(7,0);	break;
		case 0x0b9: /* 0 010 111 001 */	cp1600_movr(7,1);	break;
		case 0x0ba: /* 0 010 111 010 */	cp1600_movr(7,2);	break;
		case 0x0bb: /* 0 010 111 011 */	cp1600_movr(7,3);	break;
		case 0x0bc: /* 0 010 111 100 */	cp1600_movr(7,4);	break;
		case 0x0bd: /* 0 010 111 101 */	cp1600_movr(7,5);	break;
		case 0x0be: /* 0 010 111 110 */	cp1600_movr(7,6);	break;
		case 0x0bf: /* 0 010 111 111 */	cp1600_tstr(7);		break;

		case 0x0c0: /* 0 011 000 000 */	cp1600_addr(0,0);	break;
		case 0x0c1: /* 0 011 000 001 */	cp1600_addr(0,1);	break;
		case 0x0c2: /* 0 011 000 010 */	cp1600_addr(0,2);	break;
		case 0x0c3: /* 0 011 000 011 */	cp1600_addr(0,3);	break;
		case 0x0c4: /* 0 011 000 100 */	cp1600_addr(0,4);	break;
		case 0x0c5: /* 0 011 000 101 */	cp1600_addr(0,5);	break;
		case 0x0c6: /* 0 011 000 110 */	cp1600_addr(0,6);	break;
		case 0x0c7: /* 0 011 000 111 */	cp1600_addr(0,7);	break;

		case 0x0c8: /* 0 011 001 000 */	cp1600_addr(1,0);	break;
		case 0x0c9: /* 0 011 001 001 */	cp1600_addr(1,1);	break;
		case 0x0ca: /* 0 011 001 010 */	cp1600_addr(1,2);	break;
		case 0x0cb: /* 0 011 001 011 */	cp1600_addr(1,3);	break;
		case 0x0cc: /* 0 011 001 100 */	cp1600_addr(1,4);	break;
		case 0x0cd: /* 0 011 001 101 */	cp1600_addr(1,5);	break;
		case 0x0ce: /* 0 011 001 110 */	cp1600_addr(1,6);	break;
		case 0x0cf: /* 0 011 001 111 */	cp1600_addr(1,7);	break;

		case 0x0d0: /* 0 011 010 000 */	cp1600_addr(2,0);	break;
		case 0x0d1: /* 0 011 010 001 */	cp1600_addr(2,1);	break;
		case 0x0d2: /* 0 011 010 010 */	cp1600_addr(2,2);	break;
		case 0x0d3: /* 0 011 010 011 */	cp1600_addr(2,3);	break;
		case 0x0d4: /* 0 011 010 100 */	cp1600_addr(2,4);	break;
		case 0x0d5: /* 0 011 010 101 */	cp1600_addr(2,5);	break;
		case 0x0d6: /* 0 011 010 110 */	cp1600_addr(2,6);	break;
		case 0x0d7: /* 0 011 010 111 */	cp1600_addr(2,7);	break;

		case 0x0d8: /* 0 011 011 000 */	cp1600_addr(3,0);	break;
		case 0x0d9: /* 0 011 011 001 */	cp1600_addr(3,1);	break;
		case 0x0da: /* 0 011 011 010 */	cp1600_addr(3,2);	break;
		case 0x0db: /* 0 011 011 011 */	cp1600_addr(3,3);	break;
		case 0x0dc: /* 0 011 011 100 */	cp1600_addr(3,4);	break;
		case 0x0dd: /* 0 011 011 101 */	cp1600_addr(3,5);	break;
		case 0x0de: /* 0 011 011 110 */	cp1600_addr(3,6);	break;
		case 0x0df: /* 0 011 011 111 */	cp1600_addr(3,7);	break;

		case 0x0e0: /* 0 011 100 000 */	cp1600_addr(4,0);	break;
		case 0x0e1: /* 0 011 100 001 */	cp1600_addr(4,1);	break;
		case 0x0e2: /* 0 011 100 010 */	cp1600_addr(4,2);	break;
		case 0x0e3: /* 0 011 100 011 */	cp1600_addr(4,3);	break;
		case 0x0e4: /* 0 011 100 100 */	cp1600_addr(4,4);	break;
		case 0x0e5: /* 0 011 100 101 */	cp1600_addr(4,5);	break;
		case 0x0e6: /* 0 011 100 110 */	cp1600_addr(4,6);	break;
		case 0x0e7: /* 0 011 100 111 */	cp1600_addr(4,7);	break;

		case 0x0e8: /* 0 011 101 000 */	cp1600_addr(5,0);	break;
		case 0x0e9: /* 0 011 101 001 */	cp1600_addr(5,1);	break;
		case 0x0ea: /* 0 011 101 010 */	cp1600_addr(5,2);	break;
		case 0x0eb: /* 0 011 101 011 */	cp1600_addr(5,3);	break;
		case 0x0ec: /* 0 011 101 100 */	cp1600_addr(5,4);	break;
		case 0x0ed: /* 0 011 101 101 */	cp1600_addr(5,5);	break;
		case 0x0ee: /* 0 011 101 110 */	cp1600_addr(5,6);	break;
		case 0x0ef: /* 0 011 101 111 */	cp1600_addr(5,7);	break;

		case 0x0f0: /* 0 011 110 000 */	cp1600_addr(6,0);	break;
		case 0x0f1: /* 0 011 110 001 */	cp1600_addr(6,1);	break;
		case 0x0f2: /* 0 011 110 010 */	cp1600_addr(6,2);	break;
		case 0x0f3: /* 0 011 110 011 */	cp1600_addr(6,3);	break;
		case 0x0f4: /* 0 011 110 100 */	cp1600_addr(6,4);	break;
		case 0x0f5: /* 0 011 110 101 */	cp1600_addr(6,5);	break;
		case 0x0f6: /* 0 011 110 110 */	cp1600_addr(6,6);	break;
		case 0x0f7: /* 0 011 110 111 */	cp1600_addr(6,7);	break;

		case 0x0f8: /* 0 011 111 000 */	cp1600_addr(7,0);	break;
		case 0x0f9: /* 0 011 111 001 */	cp1600_addr(7,1);	break;
		case 0x0fa: /* 0 011 111 010 */	cp1600_addr(7,2);	break;
		case 0x0fb: /* 0 011 111 011 */	cp1600_addr(7,3);	break;
		case 0x0fc: /* 0 011 111 100 */	cp1600_addr(7,4);	break;
		case 0x0fd: /* 0 011 111 101 */	cp1600_addr(7,5);	break;
		case 0x0fe: /* 0 011 111 110 */	cp1600_addr(7,6);	break;
		case 0x0ff: /* 0 011 111 111 */	cp1600_addr(7,7);	break;

		case 0x100: /* 0 100 000 000 */	cp1600_subr(0,0);	break;
		case 0x101: /* 0 100 000 001 */	cp1600_subr(0,1);	break;
		case 0x102: /* 0 100 000 010 */	cp1600_subr(0,2);	break;
		case 0x103: /* 0 100 000 011 */	cp1600_subr(0,3);	break;
		case 0x104: /* 0 100 000 100 */	cp1600_subr(0,4);	break;
		case 0x105: /* 0 100 000 101 */	cp1600_subr(0,5);	break;
		case 0x106: /* 0 100 000 110 */	cp1600_subr(0,6);	break;
		case 0x107: /* 0 100 000 111 */	cp1600_subr(0,7);	break;

		case 0x108: /* 0 100 001 000 */	cp1600_subr(1,0);	break;
		case 0x109: /* 0 100 001 001 */	cp1600_subr(1,1);	break;
		case 0x10a: /* 0 100 001 010 */	cp1600_subr(1,2);	break;
		case 0x10b: /* 0 100 001 011 */	cp1600_subr(1,3);	break;
		case 0x10c: /* 0 100 001 100 */	cp1600_subr(1,4);	break;
		case 0x10d: /* 0 100 001 101 */	cp1600_subr(1,5);	break;
		case 0x10e: /* 0 100 001 110 */	cp1600_subr(1,6);	break;
		case 0x10f: /* 0 100 001 111 */	cp1600_subr(1,7);	break;

		case 0x110: /* 0 100 010 000 */	cp1600_subr(2,0);	break;
		case 0x111: /* 0 100 010 001 */	cp1600_subr(2,1);	break;
		case 0x112: /* 0 100 010 010 */	cp1600_subr(2,2);	break;
		case 0x113: /* 0 100 010 011 */	cp1600_subr(2,3);	break;
		case 0x114: /* 0 100 010 100 */	cp1600_subr(2,4);	break;
		case 0x115: /* 0 100 010 101 */	cp1600_subr(2,5);	break;
		case 0x116: /* 0 100 010 110 */	cp1600_subr(2,6);	break;
		case 0x117: /* 0 100 010 111 */	cp1600_subr(2,7);	break;

		case 0x118: /* 0 100 011 000 */	cp1600_subr(3,0);	break;
		case 0x119: /* 0 100 011 001 */	cp1600_subr(3,1);	break;
		case 0x11a: /* 0 100 011 010 */	cp1600_subr(3,2);	break;
		case 0x11b: /* 0 100 011 011 */	cp1600_subr(3,3);	break;
		case 0x11c: /* 0 100 011 100 */	cp1600_subr(3,4);	break;
		case 0x11d: /* 0 100 011 101 */	cp1600_subr(3,5);	break;
		case 0x11e: /* 0 100 011 110 */	cp1600_subr(3,6);	break;
		case 0x11f: /* 0 100 011 111 */	cp1600_subr(3,7);	break;

		case 0x120: /* 0 100 100 000 */	cp1600_subr(4,0);	break;
		case 0x121: /* 0 100 100 001 */	cp1600_subr(4,1);	break;
		case 0x122: /* 0 100 100 010 */	cp1600_subr(4,2);	break;
		case 0x123: /* 0 100 100 011 */	cp1600_subr(4,3);	break;
		case 0x124: /* 0 100 100 100 */	cp1600_subr(4,4);	break;
		case 0x125: /* 0 100 100 101 */	cp1600_subr(4,5);	break;
		case 0x126: /* 0 100 100 110 */	cp1600_subr(4,6);	break;
		case 0x127: /* 0 100 100 111 */	cp1600_subr(4,7);	break;

		case 0x128: /* 0 100 101 000 */	cp1600_subr(5,0);	break;
		case 0x129: /* 0 100 101 001 */	cp1600_subr(5,1);	break;
		case 0x12a: /* 0 100 101 010 */	cp1600_subr(5,2);	break;
		case 0x12b: /* 0 100 101 011 */	cp1600_subr(5,3);	break;
		case 0x12c: /* 0 100 101 100 */	cp1600_subr(5,4);	break;
		case 0x12d: /* 0 100 101 101 */	cp1600_subr(5,5);	break;
		case 0x12e: /* 0 100 101 110 */	cp1600_subr(5,6);	break;
		case 0x12f: /* 0 100 101 111 */	cp1600_subr(5,7);	break;

		case 0x130: /* 0 100 110 000 */	cp1600_subr(6,0);	break;
		case 0x131: /* 0 100 110 001 */	cp1600_subr(6,1);	break;
		case 0x132: /* 0 100 110 010 */	cp1600_subr(6,2);	break;
		case 0x133: /* 0 100 110 011 */	cp1600_subr(6,3);	break;
		case 0x134: /* 0 100 110 100 */	cp1600_subr(6,4);	break;
		case 0x135: /* 0 100 110 101 */	cp1600_subr(6,5);	break;
		case 0x136: /* 0 100 110 110 */	cp1600_subr(6,6);	break;
		case 0x137: /* 0 100 110 111 */	cp1600_subr(6,7);	break;

		case 0x138: /* 0 100 111 000 */	cp1600_subr(7,0);	break;
		case 0x139: /* 0 100 111 001 */	cp1600_subr(7,1);	break;
		case 0x13a: /* 0 100 111 010 */	cp1600_subr(7,2);	break;
		case 0x13b: /* 0 100 111 011 */	cp1600_subr(7,3);	break;
		case 0x13c: /* 0 100 111 100 */	cp1600_subr(7,4);	break;
		case 0x13d: /* 0 100 111 101 */	cp1600_subr(7,5);	break;
		case 0x13e: /* 0 100 111 110 */	cp1600_subr(7,6);	break;
		case 0x13f: /* 0 100 111 111 */	cp1600_subr(7,7);	break;

		case 0x140: /* 0 101 000 000 */	cp1600_cmpr(0,0);	break;
		case 0x141: /* 0 101 000 001 */	cp1600_cmpr(0,1);	break;
		case 0x142: /* 0 101 000 010 */	cp1600_cmpr(0,2);	break;
		case 0x143: /* 0 101 000 011 */	cp1600_cmpr(0,3);	break;
		case 0x144: /* 0 101 000 100 */	cp1600_cmpr(0,4);	break;
		case 0x145: /* 0 101 000 101 */	cp1600_cmpr(0,5);	break;
		case 0x146: /* 0 101 000 110 */	cp1600_cmpr(0,6);	break;
		case 0x147: /* 0 101 000 111 */	cp1600_cmpr(0,7);	break;

		case 0x148: /* 0 101 001 000 */	cp1600_cmpr(1,0);	break;
		case 0x149: /* 0 101 001 001 */	cp1600_cmpr(1,1);	break;
		case 0x14a: /* 0 101 001 010 */	cp1600_cmpr(1,2);	break;
		case 0x14b: /* 0 101 001 011 */	cp1600_cmpr(1,3);	break;
		case 0x14c: /* 0 101 001 100 */	cp1600_cmpr(1,4);	break;
		case 0x14d: /* 0 101 001 101 */	cp1600_cmpr(1,5);	break;
		case 0x14e: /* 0 101 001 110 */	cp1600_cmpr(1,6);	break;
		case 0x14f: /* 0 101 001 111 */	cp1600_cmpr(1,7);	break;

		case 0x150: /* 0 101 010 000 */	cp1600_cmpr(2,0);	break;
		case 0x151: /* 0 101 010 001 */	cp1600_cmpr(2,1);	break;
		case 0x152: /* 0 101 010 010 */	cp1600_cmpr(2,2);	break;
		case 0x153: /* 0 101 010 011 */	cp1600_cmpr(2,3);	break;
		case 0x154: /* 0 101 010 100 */	cp1600_cmpr(2,4);	break;
		case 0x155: /* 0 101 010 101 */	cp1600_cmpr(2,5);	break;
		case 0x156: /* 0 101 010 110 */	cp1600_cmpr(2,6);	break;
		case 0x157: /* 0 101 010 111 */	cp1600_cmpr(2,7);	break;

		case 0x158: /* 0 101 011 000 */	cp1600_cmpr(3,0);	break;
		case 0x159: /* 0 101 011 001 */	cp1600_cmpr(3,1);	break;
		case 0x15a: /* 0 101 011 010 */	cp1600_cmpr(3,2);	break;
		case 0x15b: /* 0 101 011 011 */	cp1600_cmpr(3,3);	break;
		case 0x15c: /* 0 101 011 100 */	cp1600_cmpr(3,4);	break;
		case 0x15d: /* 0 101 011 101 */	cp1600_cmpr(3,5);	break;
		case 0x15e: /* 0 101 011 110 */	cp1600_cmpr(3,6);	break;
		case 0x15f: /* 0 101 011 111 */	cp1600_cmpr(3,7);	break;

		case 0x160: /* 0 101 100 000 */	cp1600_cmpr(4,0);	break;
		case 0x161: /* 0 101 100 001 */	cp1600_cmpr(4,1);	break;
		case 0x162: /* 0 101 100 010 */	cp1600_cmpr(4,2);	break;
		case 0x163: /* 0 101 100 011 */	cp1600_cmpr(4,3);	break;
		case 0x164: /* 0 101 100 100 */	cp1600_cmpr(4,4);	break;
		case 0x165: /* 0 101 100 101 */	cp1600_cmpr(4,5);	break;
		case 0x166: /* 0 101 100 110 */	cp1600_cmpr(4,6);	break;
		case 0x167: /* 0 101 100 111 */	cp1600_cmpr(4,7);	break;

		case 0x168: /* 0 101 101 000 */	cp1600_cmpr(5,0);	break;
		case 0x169: /* 0 101 101 001 */	cp1600_cmpr(5,1);	break;
		case 0x16a: /* 0 101 101 010 */	cp1600_cmpr(5,2);	break;
		case 0x16b: /* 0 101 101 011 */	cp1600_cmpr(5,3);	break;
		case 0x16c: /* 0 101 101 100 */	cp1600_cmpr(5,4);	break;
		case 0x16d: /* 0 101 101 101 */	cp1600_cmpr(5,5);	break;
		case 0x16e: /* 0 101 101 110 */	cp1600_cmpr(5,6);	break;
		case 0x16f: /* 0 101 101 111 */	cp1600_cmpr(5,7);	break;

		case 0x170: /* 0 101 110 000 */	cp1600_cmpr(6,0);	break;
		case 0x171: /* 0 101 110 001 */	cp1600_cmpr(6,1);	break;
		case 0x172: /* 0 101 110 010 */	cp1600_cmpr(6,2);	break;
		case 0x173: /* 0 101 110 011 */	cp1600_cmpr(6,3);	break;
		case 0x174: /* 0 101 110 100 */	cp1600_cmpr(6,4);	break;
		case 0x175: /* 0 101 110 101 */	cp1600_cmpr(6,5);	break;
		case 0x176: /* 0 101 110 110 */	cp1600_cmpr(6,6);	break;
		case 0x177: /* 0 101 110 111 */	cp1600_cmpr(6,7);	break;

		case 0x178: /* 0 101 111 000 */	cp1600_cmpr(7,0);	break;
		case 0x179: /* 0 101 111 001 */	cp1600_cmpr(7,1);	break;
		case 0x17a: /* 0 101 111 010 */	cp1600_cmpr(7,2);	break;
		case 0x17b: /* 0 101 111 011 */	cp1600_cmpr(7,3);	break;
		case 0x17c: /* 0 101 111 100 */	cp1600_cmpr(7,4);	break;
		case 0x17d: /* 0 101 111 101 */	cp1600_cmpr(7,5);	break;
		case 0x17e: /* 0 101 111 110 */	cp1600_cmpr(7,6);	break;
		case 0x17f: /* 0 101 111 111 */	cp1600_cmpr(7,7);	break;

		case 0x180: /* 0 110 000 000 */	cp1600_andr(0,0);	break;
		case 0x181: /* 0 110 000 001 */	cp1600_andr(0,1);	break;
		case 0x182: /* 0 110 000 010 */	cp1600_andr(0,2);	break;
		case 0x183: /* 0 110 000 011 */	cp1600_andr(0,3);	break;
		case 0x184: /* 0 110 000 100 */	cp1600_andr(0,4);	break;
		case 0x185: /* 0 110 000 101 */	cp1600_andr(0,5);	break;
		case 0x186: /* 0 110 000 110 */	cp1600_andr(0,6);	break;
		case 0x187: /* 0 110 000 111 */	cp1600_andr(0,7);	break;

		case 0x188: /* 0 110 001 000 */	cp1600_andr(1,0);	break;
		case 0x189: /* 0 110 001 001 */	cp1600_andr(1,1);	break;
		case 0x18a: /* 0 110 001 010 */	cp1600_andr(1,2);	break;
		case 0x18b: /* 0 110 001 011 */	cp1600_andr(1,3);	break;
		case 0x18c: /* 0 110 001 100 */	cp1600_andr(1,4);	break;
		case 0x18d: /* 0 110 001 101 */	cp1600_andr(1,5);	break;
		case 0x18e: /* 0 110 001 110 */	cp1600_andr(1,6);	break;
		case 0x18f: /* 0 110 001 111 */	cp1600_andr(1,7);	break;

		case 0x190: /* 0 110 010 000 */	cp1600_andr(2,0);	break;
		case 0x191: /* 0 110 010 001 */	cp1600_andr(2,1);	break;
		case 0x192: /* 0 110 010 010 */	cp1600_andr(2,2);	break;
		case 0x193: /* 0 110 010 011 */	cp1600_andr(2,3);	break;
		case 0x194: /* 0 110 010 100 */	cp1600_andr(2,4);	break;
		case 0x195: /* 0 110 010 101 */	cp1600_andr(2,5);	break;
		case 0x196: /* 0 110 010 110 */	cp1600_andr(2,6);	break;
		case 0x197: /* 0 110 010 111 */	cp1600_andr(2,7);	break;

		case 0x198: /* 0 110 011 000 */	cp1600_andr(3,0);	break;
		case 0x199: /* 0 110 011 001 */	cp1600_andr(3,1);	break;
		case 0x19a: /* 0 110 011 010 */	cp1600_andr(3,2);	break;
		case 0x19b: /* 0 110 011 011 */	cp1600_andr(3,3);	break;
		case 0x19c: /* 0 110 011 100 */	cp1600_andr(3,4);	break;
		case 0x19d: /* 0 110 011 101 */	cp1600_andr(3,5);	break;
		case 0x19e: /* 0 110 011 110 */	cp1600_andr(3,6);	break;
		case 0x19f: /* 0 110 011 111 */	cp1600_andr(3,7);	break;

		case 0x1a0: /* 0 110 100 000 */	cp1600_andr(4,0);	break;
		case 0x1a1: /* 0 110 100 001 */	cp1600_andr(4,1);	break;
		case 0x1a2: /* 0 110 100 010 */	cp1600_andr(4,2);	break;
		case 0x1a3: /* 0 110 100 011 */	cp1600_andr(4,3);	break;
		case 0x1a4: /* 0 110 100 100 */	cp1600_andr(4,4);	break;
		case 0x1a5: /* 0 110 100 101 */	cp1600_andr(4,5);	break;
		case 0x1a6: /* 0 110 100 110 */	cp1600_andr(4,6);	break;
		case 0x1a7: /* 0 110 100 111 */	cp1600_andr(4,7);	break;

		case 0x1a8: /* 0 110 101 000 */	cp1600_andr(5,0);	break;
		case 0x1a9: /* 0 110 101 001 */	cp1600_andr(5,1);	break;
		case 0x1aa: /* 0 110 101 010 */	cp1600_andr(5,2);	break;
		case 0x1ab: /* 0 110 101 011 */	cp1600_andr(5,3);	break;
		case 0x1ac: /* 0 110 101 100 */	cp1600_andr(5,4);	break;
		case 0x1ad: /* 0 110 101 101 */	cp1600_andr(5,5);	break;
		case 0x1ae: /* 0 110 101 110 */	cp1600_andr(5,6);	break;
		case 0x1af: /* 0 110 101 111 */	cp1600_andr(5,7);	break;

		case 0x1b0: /* 0 110 110 000 */	cp1600_andr(6,0);	break;
		case 0x1b1: /* 0 110 110 001 */	cp1600_andr(6,1);	break;
		case 0x1b2: /* 0 110 110 010 */	cp1600_andr(6,2);	break;
		case 0x1b3: /* 0 110 110 011 */	cp1600_andr(6,3);	break;
		case 0x1b4: /* 0 110 110 100 */	cp1600_andr(6,4);	break;
		case 0x1b5: /* 0 110 110 101 */	cp1600_andr(6,5);	break;
		case 0x1b6: /* 0 110 110 110 */	cp1600_andr(6,6);	break;
		case 0x1b7: /* 0 110 110 111 */	cp1600_andr(6,7);	break;

		case 0x1b8: /* 0 110 111 000 */	cp1600_andr(7,0);	break;
		case 0x1b9: /* 0 110 111 001 */	cp1600_andr(7,1);	break;
		case 0x1ba: /* 0 110 111 010 */	cp1600_andr(7,2);	break;
		case 0x1bb: /* 0 110 111 011 */	cp1600_andr(7,3);	break;
		case 0x1bc: /* 0 110 111 100 */	cp1600_andr(7,4);	break;
		case 0x1bd: /* 0 110 111 101 */	cp1600_andr(7,5);	break;
		case 0x1be: /* 0 110 111 110 */	cp1600_andr(7,6);	break;
		case 0x1bf: /* 0 110 111 111 */	cp1600_andr(7,7);	break;

		case 0x1c0: /* 0 111 000 000 */	cp1600_clrr(0);		break;
		case 0x1c1: /* 0 111 000 001 */	cp1600_xorr(0,1);	break;
		case 0x1c2: /* 0 111 000 010 */	cp1600_xorr(0,2);	break;
		case 0x1c3: /* 0 111 000 011 */	cp1600_xorr(0,3);	break;
		case 0x1c4: /* 0 111 000 100 */	cp1600_xorr(0,4);	break;
		case 0x1c5: /* 0 111 000 101 */	cp1600_xorr(0,5);	break;
		case 0x1c6: /* 0 111 000 110 */	cp1600_xorr(0,6);	break;
		case 0x1c7: /* 0 111 000 111 */	cp1600_xorr(0,7);	break;

		case 0x1c8: /* 0 111 001 000 */	cp1600_xorr(1,0);	break;
		case 0x1c9: /* 0 111 001 001 */	cp1600_clrr(1);		break;
		case 0x1ca: /* 0 111 001 010 */	cp1600_xorr(1,2);	break;
		case 0x1cb: /* 0 111 001 011 */	cp1600_xorr(1,3);	break;
		case 0x1cc: /* 0 111 001 100 */	cp1600_xorr(1,4);	break;
		case 0x1cd: /* 0 111 001 101 */	cp1600_xorr(1,5);	break;
		case 0x1ce: /* 0 111 001 110 */	cp1600_xorr(1,6);	break;
		case 0x1cf: /* 0 111 001 111 */	cp1600_xorr(1,7);	break;

		case 0x1d0: /* 0 111 010 000 */	cp1600_xorr(2,0);	break;
		case 0x1d1: /* 0 111 010 001 */	cp1600_xorr(2,1);	break;
		case 0x1d2: /* 0 111 010 010 */	cp1600_clrr(2);		break;
		case 0x1d3: /* 0 111 010 011 */	cp1600_xorr(2,3);	break;
		case 0x1d4: /* 0 111 010 100 */	cp1600_xorr(2,4);	break;
		case 0x1d5: /* 0 111 010 101 */	cp1600_xorr(2,5);	break;
		case 0x1d6: /* 0 111 010 110 */	cp1600_xorr(2,6);	break;
		case 0x1d7: /* 0 111 010 111 */	cp1600_xorr(2,7);	break;

		case 0x1d8: /* 0 111 011 000 */	cp1600_xorr(3,0);	break;
		case 0x1d9: /* 0 111 011 001 */	cp1600_xorr(3,1);	break;
		case 0x1da: /* 0 111 011 010 */	cp1600_xorr(3,2);	break;
		case 0x1db: /* 0 111 011 011 */	cp1600_clrr(3);		break;
		case 0x1dc: /* 0 111 011 100 */	cp1600_xorr(3,4);	break;
		case 0x1dd: /* 0 111 011 101 */	cp1600_xorr(3,5);	break;
		case 0x1de: /* 0 111 011 110 */	cp1600_xorr(3,6);	break;
		case 0x1df: /* 0 111 011 111 */	cp1600_xorr(3,7);	break;

		case 0x1e0: /* 0 111 100 000 */	cp1600_xorr(4,0);	break;
		case 0x1e1: /* 0 111 100 001 */	cp1600_xorr(4,1);	break;
		case 0x1e2: /* 0 111 100 010 */	cp1600_xorr(4,2);	break;
		case 0x1e3: /* 0 111 100 011 */	cp1600_xorr(4,3);	break;
		case 0x1e4: /* 0 111 100 100 */	cp1600_clrr(4);		break;
		case 0x1e5: /* 0 111 100 101 */	cp1600_xorr(4,5);	break;
		case 0x1e6: /* 0 111 100 110 */	cp1600_xorr(4,6);	break;
		case 0x1e7: /* 0 111 100 111 */	cp1600_xorr(4,7);	break;

		case 0x1e8: /* 0 111 101 000 */	cp1600_xorr(5,0);	break;
		case 0x1e9: /* 0 111 101 001 */	cp1600_xorr(5,1);	break;
		case 0x1ea: /* 0 111 101 010 */	cp1600_xorr(5,2);	break;
		case 0x1eb: /* 0 111 101 011 */	cp1600_xorr(5,3);	break;
		case 0x1ec: /* 0 111 101 100 */	cp1600_xorr(5,4);	break;
		case 0x1ed: /* 0 111 101 101 */	cp1600_clrr(5);		break;
		case 0x1ee: /* 0 111 101 110 */	cp1600_xorr(5,6);	break;
		case 0x1ef: /* 0 111 101 111 */	cp1600_xorr(5,7);	break;

		case 0x1f0: /* 0 111 110 000 */	cp1600_xorr(6,0);	break;
		case 0x1f1: /* 0 111 110 001 */	cp1600_xorr(6,1);	break;
		case 0x1f2: /* 0 111 110 010 */	cp1600_xorr(6,2);	break;
		case 0x1f3: /* 0 111 110 011 */	cp1600_xorr(6,3);	break;
		case 0x1f4: /* 0 111 110 100 */	cp1600_xorr(6,4);	break;
		case 0x1f5: /* 0 111 110 101 */	cp1600_xorr(6,5);	break;
		case 0x1f6: /* 0 111 110 110 */	cp1600_clrr(6);		break;
		case 0x1f7: /* 0 111 110 111 */	cp1600_xorr(6,7);	break;

		case 0x1f8: /* 0 111 111 000 */	cp1600_xorr(7,0);	break;
		case 0x1f9: /* 0 111 111 001 */	cp1600_xorr(7,1);	break;
		case 0x1fa: /* 0 111 111 010 */	cp1600_xorr(7,2);	break;
		case 0x1fb: /* 0 111 111 011 */	cp1600_xorr(7,3);	break;
		case 0x1fc: /* 0 111 111 100 */	cp1600_xorr(7,4);	break;
		case 0x1fd: /* 0 111 111 101 */	cp1600_xorr(7,5);	break;
		case 0x1fe: /* 0 111 111 110 */	cp1600_xorr(7,6);	break;
		case 0x1ff: /* 0 110 111 111 */	cp1600_clrr(7);		break;

		case 0x200: /* 1 000 000 000 */
		case 0x201:
		case 0x202:
		case 0x203:
		case 0x204:
		case 0x205:
		case 0x206:
		case 0x207:

		case 0x208:
		case 0x209:
		case 0x20a:
		case 0x20b:
		case 0x20c:
		case 0x20d:
		case 0x20e:
		case 0x20f:

		case 0x210:
		case 0x211:
		case 0x212:
		case 0x213:
		case 0x214:
		case 0x215:
		case 0x216:
		case 0x217:

		case 0x218:
		case 0x219:
		case 0x21a:
		case 0x21b:
		case 0x21c:
		case 0x21d:
		case 0x21e:
		case 0x21f:

		case 0x220:
		case 0x221:
		case 0x222:
		case 0x223:
		case 0x224:
		case 0x225:
		case 0x226:
		case 0x227:

		case 0x228:
		case 0x229:
		case 0x22a:
		case 0x22b:
		case 0x22c:
		case 0x22d:
		case 0x22e:
		case 0x22f:

		case 0x230:
		case 0x231:
		case 0x232:
		case 0x233:
		case 0x234:
		case 0x235:
		case 0x236:
		case 0x237:

		case 0x238:
		case 0x239:
		case 0x23a:
		case 0x23b:
		case 0x23c:
		case 0x23d:
		case 0x23e:
		case 0x23f:	/* 1 000 111 111 */ cp1600_branch();		break;

		case 0x240: /* 1 001 000 000 */ cp1600_mvo(0);			break;
		case 0x241: /* 1 001 000 001 */ cp1600_mvo(1);			break;
		case 0x242: /* 1 001 000 010 */ cp1600_mvo(2);			break;
		case 0x243: /* 1 001 000 011 */ cp1600_mvo(3);			break;
		case 0x244: /* 1 001 000 100 */ cp1600_mvo(4);			break;
		case 0x245: /* 1 001 000 101 */ cp1600_mvo(5);			break;
		case 0x246: /* 1 001 000 110 */ cp1600_mvo(6);			break;
		case 0x247: /* 1 001 000 111 */ cp1600_mvo(7);			break;

		case 0x248: /* 1 001 001 000 */ cp1600_mvoat(0,1);		break;
		case 0x249: /* 1 001 001 001 */ cp1600_mvoat(1,1);		break;
		case 0x24a: /* 1 001 001 010 */ cp1600_mvoat(2,1);		break;
		case 0x24b: /* 1 001 001 011 */ cp1600_mvoat(3,1);		break;
		case 0x24c: /* 1 001 001 100 */ cp1600_mvoat(4,1);		break;
		case 0x24d: /* 1 001 001 101 */ cp1600_mvoat(5,1);		break;
		case 0x24e: /* 1 001 001 110 */ cp1600_mvoat(6,1);		break;
		case 0x24f: /* 1 001 001 111 */ cp1600_mvoat(7,1);		break;

		case 0x250: /* 1 001 010 000 */ cp1600_mvoat(0,2);		break;
		case 0x251: /* 1 001 010 001 */ cp1600_mvoat(1,2);		break;
		case 0x252: /* 1 001 010 010 */ cp1600_mvoat(2,2);		break;
		case 0x253: /* 1 001 010 011 */ cp1600_mvoat(3,2);		break;
		case 0x254: /* 1 001 010 100 */ cp1600_mvoat(4,2);		break;
		case 0x255: /* 1 001 010 101 */ cp1600_mvoat(5,2);		break;
		case 0x256: /* 1 001 010 110 */ cp1600_mvoat(6,2);		break;
		case 0x257: /* 1 001 010 111 */ cp1600_mvoat(7,2);		break;

		case 0x258: /* 1 001 011 000 */ cp1600_mvoat(0,3);		break;
		case 0x259: /* 1 001 011 001 */ cp1600_mvoat(1,3);		break;
		case 0x25a: /* 1 001 011 010 */ cp1600_mvoat(2,3);		break;
		case 0x25b: /* 1 001 011 011 */ cp1600_mvoat(3,3);		break;
		case 0x25c: /* 1 001 011 100 */ cp1600_mvoat(4,3);		break;
		case 0x25d: /* 1 001 011 101 */ cp1600_mvoat(5,3);		break;
		case 0x25e: /* 1 001 011 110 */ cp1600_mvoat(6,3);		break;
		case 0x25f: /* 1 001 011 111 */ cp1600_mvoat(7,3);		break;

		case 0x260: /* 1 001 100 000 */ cp1600_mvoat(0,4);		break;
		case 0x261: /* 1 001 100 001 */ cp1600_mvoat(1,4);		break;
		case 0x262: /* 1 001 100 010 */ cp1600_mvoat(2,4);		break;
		case 0x263: /* 1 001 100 011 */ cp1600_mvoat(3,4);		break;
		case 0x264: /* 1 001 100 100 */ cp1600_mvoat(4,4);		break; /* ??? */
		case 0x265: /* 1 001 100 101 */ cp1600_mvoat(5,4);		break;
		case 0x266: /* 1 001 100 110 */ cp1600_mvoat(6,4);		break;
		case 0x267: /* 1 001 100 111 */ cp1600_mvoat(7,4);		break;

		case 0x268: /* 1 001 101 000 */ cp1600_mvoat(0,5);		break;
		case 0x269: /* 1 001 101 001 */ cp1600_mvoat(1,5);		break;
		case 0x26a: /* 1 001 101 010 */ cp1600_mvoat(2,5);		break;
		case 0x26b: /* 1 001 101 011 */ cp1600_mvoat(3,5);		break;
		case 0x26c: /* 1 001 101 100 */ cp1600_mvoat(4,5);		break;
		case 0x26d: /* 1 001 101 101 */ cp1600_mvoat(5,5);		break; /* ??? */
		case 0x26e: /* 1 001 101 110 */ cp1600_mvoat(6,5);		break;
		case 0x26f: /* 1 001 101 111 */ cp1600_mvoat(7,5);		break;

		case 0x270: /* 1 001 110 000 */ cp1600_pshr(0);			break;
		case 0x271: /* 1 001 110 001 */ cp1600_pshr(1);			break;
		case 0x272: /* 1 001 110 010 */ cp1600_pshr(2);			break;
		case 0x273: /* 1 001 110 011 */ cp1600_pshr(3);			break;
		case 0x274: /* 1 001 110 100 */ cp1600_pshr(4);			break;
		case 0x275: /* 1 001 110 101 */ cp1600_pshr(5);			break;
		case 0x276: /* 1 001 110 110 */ cp1600_pshr(6);			break; /* ??? */
		case 0x277: /* 1 001 110 111 */ cp1600_pshr(7);			break;

		case 0x278: /* 1 001 111 000 */ cp1600_mvoi(0);			break;
		case 0x279: /* 1 001 111 001 */ cp1600_mvoi(1);			break;
		case 0x27a: /* 1 001 111 010 */ cp1600_mvoi(2);			break;
		case 0x27b: /* 1 001 111 011 */ cp1600_mvoi(3);			break;
		case 0x27c: /* 1 001 111 100 */ cp1600_mvoi(4);			break;
		case 0x27d: /* 1 001 111 101 */ cp1600_mvoi(5);			break;
		case 0x27e: /* 1 001 111 110 */ cp1600_mvoi(6);			break;
		case 0x27f: /* 1 001 111 111 */ cp1600_mvoi(7);			break; /* ??? */

		case 0x280: /* 1 010 000 000 */ cp1600_mvi(0);			break;
		case 0x281: /* 1 010 000 001 */ cp1600_mvi(1);			break;
		case 0x282: /* 1 010 000 010 */ cp1600_mvi(2);			break;
		case 0x283: /* 1 010 000 011 */ cp1600_mvi(3);			break;
		case 0x284: /* 1 010 000 100 */ cp1600_mvi(4);			break;
		case 0x285: /* 1 010 000 101 */ cp1600_mvi(5);			break;
		case 0x286: /* 1 010 000 110 */ cp1600_mvi(6);			break;
		case 0x287: /* 1 010 000 111 */ cp1600_mvi(7);			break;

		case 0x288: /* 1 010 001 000 */ cp1600_mviat(1,0);		break;
		case 0x289: /* 1 010 001 001 */ cp1600_mviat(1,1);		break;
		case 0x28a: /* 1 010 001 010 */ cp1600_mviat(1,2);		break;
		case 0x28b: /* 1 010 001 011 */ cp1600_mviat(1,3);		break;
		case 0x28c: /* 1 010 001 100 */ cp1600_mviat(1,4);		break;
		case 0x28d: /* 1 010 001 101 */ cp1600_mviat(1,5);		break;
		case 0x28e: /* 1 010 001 110 */ cp1600_mviat(1,6);		break;
		case 0x28f: /* 1 010 001 111 */ cp1600_mviat(1,7);		break;

		case 0x290: /* 1 010 010 000 */ cp1600_mviat(2,0);		break;
		case 0x291: /* 1 010 010 001 */ cp1600_mviat(2,1);		break;
		case 0x292: /* 1 010 010 010 */ cp1600_mviat(2,2);		break;
		case 0x293: /* 1 010 010 011 */ cp1600_mviat(2,3);		break;
		case 0x294: /* 1 010 010 100 */ cp1600_mviat(2,4);		break;
		case 0x295: /* 1 010 010 101 */ cp1600_mviat(2,5);		break;
		case 0x296: /* 1 010 010 110 */ cp1600_mviat(2,6);		break;
		case 0x297: /* 1 010 010 111 */ cp1600_mviat(2,7);		break;

		case 0x298: /* 1 010 011 000 */ cp1600_mviat(3,0);		break;
		case 0x299: /* 1 010 011 001 */ cp1600_mviat(3,1);		break;
		case 0x29a: /* 1 010 011 010 */ cp1600_mviat(3,2);		break;
		case 0x29b: /* 1 010 011 011 */ cp1600_mviat(3,3);		break;
		case 0x29c: /* 1 010 011 100 */ cp1600_mviat(3,4);		break;
		case 0x29d: /* 1 010 011 101 */ cp1600_mviat(3,5);		break;
		case 0x29e: /* 1 010 011 110 */ cp1600_mviat(3,6);		break;
		case 0x29f: /* 1 010 011 111 */ cp1600_mviat(3,7);		break;

		case 0x2a0: /* 1 010 100 000 */ cp1600_mviat(4,0);		break;
		case 0x2a1: /* 1 010 100 001 */ cp1600_mviat(4,1);		break;
		case 0x2a2: /* 1 010 100 010 */ cp1600_mviat(4,2);		break;
		case 0x2a3: /* 1 010 100 011 */ cp1600_mviat(4,3);		break;
		case 0x2a4: /* 1 010 100 100 */ cp1600_mviat(4,4);		break;
		case 0x2a5: /* 1 010 100 101 */ cp1600_mviat(4,5);		break;
		case 0x2a6: /* 1 010 100 110 */ cp1600_mviat(4,6);		break;
		case 0x2a7: /* 1 010 100 111 */ cp1600_mviat(4,7);		break;

		case 0x2a8: /* 1 010 101 000 */ cp1600_mviat(5,0);		break;
		case 0x2a9: /* 1 010 101 001 */ cp1600_mviat(5,1);		break;
		case 0x2aa: /* 1 010 101 010 */ cp1600_mviat(5,2);		break;
		case 0x2ab: /* 1 010 101 011 */ cp1600_mviat(5,3);		break;
		case 0x2ac: /* 1 010 101 100 */ cp1600_mviat(5,4);		break;
		case 0x2ad: /* 1 010 101 101 */ cp1600_mviat(5,5);		break;
		case 0x2ae: /* 1 010 101 110 */ cp1600_mviat(5,6);		break;
		case 0x2af: /* 1 010 101 111 */ cp1600_mviat(5,7);		break;

		case 0x2b0: /* 1 010 110 000 */ cp1600_pulr(0);			break;
		case 0x2b1: /* 1 010 110 001 */ cp1600_pulr(1);			break;
		case 0x2b2: /* 1 010 110 010 */ cp1600_pulr(2);			break;
		case 0x2b3: /* 1 010 110 011 */ cp1600_pulr(3);			break;
		case 0x2b4: /* 1 010 110 100 */ cp1600_pulr(4);			break;
		case 0x2b5: /* 1 010 110 101 */ cp1600_pulr(5);			break;
		case 0x2b6: /* 1 010 110 110 */ cp1600_pulr(6);			break;
		case 0x2b7: /* 1 010 110 111 */ cp1600_pulr(7);			break;

		case 0x2b8: /* 1 010 111 000 */ cp1600_mvii(0);			break;
		case 0x2b9: /* 1 010 111 001 */ cp1600_mvii(1);			break;
		case 0x2ba: /* 1 010 111 010 */ cp1600_mvii(2);			break;
		case 0x2bb: /* 1 010 111 011 */ cp1600_mvii(3);			break;
		case 0x2bc: /* 1 010 111 100 */ cp1600_mvii(4);			break;
		case 0x2bd: /* 1 010 111 101 */ cp1600_mvii(5);			break;
		case 0x2be: /* 1 010 111 110 */ cp1600_mvii(6);			break;
		case 0x2bf: /* 1 010 111 111 */ cp1600_mvii(7);			break;

		case 0x2c0: /* 1 011 000 000 */ cp1600_addat(0,0);		break;
		case 0x2c1: /* 1 011 000 001 */ cp1600_addat(0,1);		break;
		case 0x2c2: /* 1 011 000 010 */ cp1600_addat(0,2);		break;
		case 0x2c3: /* 1 011 000 011 */ cp1600_addat(0,3);		break;
		case 0x2c4: /* 1 011 000 100 */ cp1600_addat(0,4);		break;
		case 0x2c5: /* 1 011 000 101 */ cp1600_addat(0,5);		break;
		case 0x2c6: /* 1 011 000 110 */ cp1600_addat(0,6);		break;
		case 0x2c7: /* 1 011 000 111 */ cp1600_addat(0,7);		break;

		case 0x2c8: /* 1 011 001 000 */ cp1600_addat(1,0);		break;
		case 0x2c9: /* 1 011 001 001 */ cp1600_addat(1,1);		break;
		case 0x2ca: /* 1 011 001 010 */ cp1600_addat(1,2);		break;
		case 0x2cb: /* 1 011 001 011 */ cp1600_addat(1,3);		break;
		case 0x2cc: /* 1 011 001 100 */ cp1600_addat(1,4);		break;
		case 0x2cd: /* 1 011 001 101 */ cp1600_addat(1,5);		break;
		case 0x2ce: /* 1 011 001 110 */ cp1600_addat(1,6);		break;
		case 0x2cf: /* 1 011 001 111 */ cp1600_addat(1,7);		break;

		case 0x2d0: /* 1 011 010 000 */ cp1600_addat(2,0);		break;
		case 0x2d1: /* 1 011 010 001 */ cp1600_addat(2,1);		break;
		case 0x2d2: /* 1 011 010 010 */ cp1600_addat(2,2);		break;
		case 0x2d3: /* 1 011 010 011 */ cp1600_addat(2,3);		break;
		case 0x2d4: /* 1 011 010 100 */ cp1600_addat(2,4);		break;
		case 0x2d5: /* 1 011 010 101 */ cp1600_addat(2,5);		break;
		case 0x2d6: /* 1 011 010 110 */ cp1600_addat(2,6);		break;
		case 0x2d7: /* 1 011 010 111 */ cp1600_addat(2,7);		break;

		case 0x2d8: /* 1 011 011 000 */ cp1600_addat(3,0);		break;
		case 0x2d9: /* 1 011 011 001 */ cp1600_addat(3,1);		break;
		case 0x2da: /* 1 011 011 010 */ cp1600_addat(3,2);		break;
		case 0x2db: /* 1 011 011 011 */ cp1600_addat(3,3);		break;
		case 0x2dc: /* 1 011 011 100 */ cp1600_addat(3,4);		break;
		case 0x2dd: /* 1 011 011 101 */ cp1600_addat(3,5);		break;
		case 0x2de: /* 1 011 011 110 */ cp1600_addat(3,6);		break;
		case 0x2df: /* 1 011 011 111 */ cp1600_addat(3,7);		break;

		case 0x2e0: /* 1 011 100 000 */ cp1600_addat(4,0);		break;
		case 0x2e1: /* 1 011 100 001 */ cp1600_addat(4,1);		break;
		case 0x2e2: /* 1 011 100 010 */ cp1600_addat(4,2);		break;
		case 0x2e3: /* 1 011 100 011 */ cp1600_addat(4,3);		break;
		case 0x2e4: /* 1 011 100 100 */ cp1600_addat(4,4);		break;
		case 0x2e5: /* 1 011 100 101 */ cp1600_addat(4,5);		break;
		case 0x2e6: /* 1 011 100 110 */ cp1600_addat(4,6);		break;
		case 0x2e7: /* 1 011 100 111 */ cp1600_addat(4,7);		break;

		case 0x2e8: /* 1 011 101 000 */ cp1600_addat(5,0);		break;
		case 0x2e9: /* 1 011 101 001 */ cp1600_addat(5,1);		break;
		case 0x2ea: /* 1 011 101 010 */ cp1600_addat(5,2);		break;
		case 0x2eb: /* 1 011 101 011 */ cp1600_addat(5,3);		break;
		case 0x2ec: /* 1 011 101 100 */ cp1600_addat(5,4);		break;
		case 0x2ed: /* 1 011 101 101 */ cp1600_addat(5,5);		break;
		case 0x2ee: /* 1 011 101 110 */ cp1600_addat(5,6);		break;
		case 0x2ef: /* 1 011 101 111 */ cp1600_addat(5,7);		break;

		case 0x2f0: /* 1 011 110 000 */ cp1600_addat(6,0);		break;
		case 0x2f1: /* 1 011 110 001 */ cp1600_addat(6,1);		break;
		case 0x2f2: /* 1 011 110 010 */ cp1600_addat(6,2);		break;
		case 0x2f3: /* 1 011 110 011 */ cp1600_addat(6,3);		break;
		case 0x2f4: /* 1 011 110 100 */ cp1600_addat(6,4);		break;
		case 0x2f5: /* 1 011 110 101 */ cp1600_addat(6,5);		break;
		case 0x2f6: /* 1 011 110 110 */ cp1600_addat(6,6);		break;
		case 0x2f7: /* 1 011 110 111 */ cp1600_addat(6,7);		break;

		case 0x2f8: /* 1 011 111 000 */ cp1600_addi(0);			break;
		case 0x2f9: /* 1 011 111 001 */ cp1600_addi(1);			break;
		case 0x2fa: /* 1 011 111 010 */ cp1600_addi(2);			break;
		case 0x2fb: /* 1 011 111 011 */ cp1600_addi(3);			break;
		case 0x2fc: /* 1 011 111 100 */ cp1600_addi(4);			break;
		case 0x2fd: /* 1 011 111 101 */ cp1600_addi(5);			break;
		case 0x2fe: /* 1 011 111 110 */ cp1600_addi(6);			break;
		case 0x2ff: /* 1 011 111 111 */ cp1600_addi(7);			break;

		case 0x300: /* 1 100 000 000 */ cp1600_subat(0,0);		break;
		case 0x301: /* 1 100 000 001 */ cp1600_subat(0,1);		break;
		case 0x302: /* 1 100 000 010 */ cp1600_subat(0,2);		break;
		case 0x303: /* 1 100 000 011 */ cp1600_subat(0,3);		break;
		case 0x304: /* 1 100 000 100 */ cp1600_subat(0,4);		break;
		case 0x305: /* 1 100 000 101 */ cp1600_subat(0,5);		break;
		case 0x306: /* 1 100 000 110 */ cp1600_subat(0,6);		break;
		case 0x307: /* 1 100 000 111 */ cp1600_subat(0,7);		break;

		case 0x308: /* 1 100 001 000 */ cp1600_subat(1,0);		break;
		case 0x309: /* 1 100 001 001 */ cp1600_subat(1,1);		break;
		case 0x30a: /* 1 100 001 010 */ cp1600_subat(1,2);		break;
		case 0x30b: /* 1 100 001 011 */ cp1600_subat(1,3);		break;
		case 0x30c: /* 1 100 001 100 */ cp1600_subat(1,4);		break;
		case 0x30d: /* 1 100 001 101 */ cp1600_subat(1,5);		break;
		case 0x30e: /* 1 100 001 110 */ cp1600_subat(1,6);		break;
		case 0x30f: /* 1 100 001 111 */ cp1600_subat(1,7);		break;

		case 0x310: /* 1 100 010 000 */ cp1600_subat(2,0);		break;
		case 0x311: /* 1 100 010 001 */ cp1600_subat(2,1);		break;
		case 0x312: /* 1 100 010 010 */ cp1600_subat(2,2);		break;
		case 0x313: /* 1 100 010 011 */ cp1600_subat(2,3);		break;
		case 0x314: /* 1 100 010 100 */ cp1600_subat(2,4);		break;
		case 0x315: /* 1 100 010 101 */ cp1600_subat(2,5);		break;
		case 0x316: /* 1 100 010 110 */ cp1600_subat(2,6);		break;
		case 0x317: /* 1 100 010 111 */ cp1600_subat(2,7);		break;

		case 0x318: /* 1 100 011 000 */ cp1600_subat(3,0);		break;
		case 0x319: /* 1 100 011 001 */ cp1600_subat(3,1);		break;
		case 0x31a: /* 1 100 011 010 */ cp1600_subat(3,2);		break;
		case 0x31b: /* 1 100 011 011 */ cp1600_subat(3,3);		break;
		case 0x31c: /* 1 100 011 100 */ cp1600_subat(3,4);		break;
		case 0x31d: /* 1 100 011 101 */ cp1600_subat(3,5);		break;
		case 0x31e: /* 1 100 011 110 */ cp1600_subat(3,6);		break;
		case 0x31f: /* 1 100 011 111 */ cp1600_subat(3,7);		break;

		case 0x320: /* 1 100 100 000 */ cp1600_subat(4,0);		break;
		case 0x321: /* 1 100 100 001 */ cp1600_subat(4,1);		break;
		case 0x322: /* 1 100 100 010 */ cp1600_subat(4,2);		break;
		case 0x323: /* 1 100 100 011 */ cp1600_subat(4,3);		break;
		case 0x324: /* 1 100 100 100 */ cp1600_subat(4,4);		break;
		case 0x325: /* 1 100 100 101 */ cp1600_subat(4,5);		break;
		case 0x326: /* 1 100 100 110 */ cp1600_subat(4,6);		break;
		case 0x327: /* 1 100 100 111 */ cp1600_subat(4,7);		break;

		case 0x328: /* 1 100 101 000 */ cp1600_subat(5,0);		break;
		case 0x329: /* 1 100 101 001 */ cp1600_subat(5,1);		break;
		case 0x32a: /* 1 100 101 010 */ cp1600_subat(5,2);		break;
		case 0x32b: /* 1 100 101 011 */ cp1600_subat(5,3);		break;
		case 0x32c: /* 1 100 101 100 */ cp1600_subat(5,4);		break;
		case 0x32d: /* 1 100 101 101 */ cp1600_subat(5,5);		break;
		case 0x32e: /* 1 100 101 110 */ cp1600_subat(5,6);		break;
		case 0x32f: /* 1 100 101 111 */ cp1600_subat(5,7);		break;

		case 0x330: /* 1 100 110 000 */ cp1600_subat(6,0);		break;
		case 0x331: /* 1 100 110 001 */ cp1600_subat(6,1);		break;
		case 0x332: /* 1 100 110 010 */ cp1600_subat(6,2);		break;
		case 0x333: /* 1 100 110 011 */ cp1600_subat(6,3);		break;
		case 0x334: /* 1 100 110 100 */ cp1600_subat(6,4);		break;
		case 0x335: /* 1 100 110 101 */ cp1600_subat(6,5);		break;
		case 0x336: /* 1 100 110 110 */ cp1600_subat(6,6);		break;
		case 0x337: /* 1 100 110 111 */ cp1600_subat(6,7);		break;

		case 0x338: /* 1 100 111 000 */ cp1600_subi(0);			break;
		case 0x339: /* 1 100 111 001 */ cp1600_subi(1);			break;
		case 0x33a: /* 1 100 111 010 */ cp1600_subi(2);			break;
		case 0x33b: /* 1 100 111 011 */ cp1600_subi(3);			break;
		case 0x33c: /* 1 100 111 100 */ cp1600_subi(4);			break;
		case 0x33d: /* 1 100 111 101 */ cp1600_subi(5);			break;
		case 0x33e: /* 1 100 111 110 */ cp1600_subi(6);			break;
		case 0x33f: /* 1 100 111 111 */ cp1600_subi(7);			break;

		case 0x340: /* 1 101 000 000 */ cp1600_cmpat(0,0);		break;
		case 0x341: /* 1 101 000 001 */ cp1600_cmpat(0,1);		break;
		case 0x342: /* 1 101 000 010 */ cp1600_cmpat(0,2);		break;
		case 0x343: /* 1 101 000 011 */ cp1600_cmpat(0,3);		break;
		case 0x344: /* 1 101 000 100 */ cp1600_cmpat(0,4);		break;
		case 0x345: /* 1 101 000 101 */ cp1600_cmpat(0,5);		break;
		case 0x346: /* 1 101 000 110 */ cp1600_cmpat(0,6);		break;
		case 0x347: /* 1 101 000 111 */ cp1600_cmpat(0,7);		break;

		case 0x348: /* 1 101 001 000 */ cp1600_cmpat(1,0);		break;
		case 0x349: /* 1 101 001 001 */ cp1600_cmpat(1,1);		break;
		case 0x34a: /* 1 101 001 010 */ cp1600_cmpat(1,2);		break;
		case 0x34b: /* 1 101 001 011 */ cp1600_cmpat(1,3);		break;
		case 0x34c: /* 1 101 001 100 */ cp1600_cmpat(1,4);		break;
		case 0x34d: /* 1 101 001 101 */ cp1600_cmpat(1,5);		break;
		case 0x34e: /* 1 101 001 110 */ cp1600_cmpat(1,6);		break;
		case 0x34f: /* 1 101 001 111 */ cp1600_cmpat(1,7);		break;

		case 0x350: /* 1 101 010 000 */ cp1600_cmpat(2,0);		break;
		case 0x351: /* 1 101 010 001 */ cp1600_cmpat(2,1);		break;
		case 0x352: /* 1 101 010 010 */ cp1600_cmpat(2,2);		break;
		case 0x353: /* 1 101 010 011 */ cp1600_cmpat(2,3);		break;
		case 0x354: /* 1 101 010 100 */ cp1600_cmpat(2,4);		break;
		case 0x355: /* 1 101 010 101 */ cp1600_cmpat(2,5);		break;
		case 0x356: /* 1 101 010 110 */ cp1600_cmpat(2,6);		break;
		case 0x357: /* 1 101 010 111 */ cp1600_cmpat(2,7);		break;

		case 0x358: /* 1 101 011 000 */ cp1600_cmpat(3,0);		break;
		case 0x359: /* 1 101 011 001 */ cp1600_cmpat(3,1);		break;
		case 0x35a: /* 1 101 011 010 */ cp1600_cmpat(3,2);		break;
		case 0x35b: /* 1 101 011 011 */ cp1600_cmpat(3,3);		break;
		case 0x35c: /* 1 101 011 100 */ cp1600_cmpat(3,4);		break;
		case 0x35d: /* 1 101 011 101 */ cp1600_cmpat(3,5);		break;
		case 0x35e: /* 1 101 011 110 */ cp1600_cmpat(3,6);		break;
		case 0x35f: /* 1 101 011 111 */ cp1600_cmpat(3,7);		break;

		case 0x360: /* 1 101 100 000 */ cp1600_cmpat(4,0);		break;
		case 0x361: /* 1 101 100 001 */ cp1600_cmpat(4,1);		break;
		case 0x362: /* 1 101 100 010 */ cp1600_cmpat(4,2);		break;
		case 0x363: /* 1 101 100 011 */ cp1600_cmpat(4,3);		break;
		case 0x364: /* 1 101 100 100 */ cp1600_cmpat(4,4);		break;
		case 0x365: /* 1 101 100 101 */ cp1600_cmpat(4,5);		break;
		case 0x366: /* 1 101 100 110 */ cp1600_cmpat(4,6);		break;
		case 0x367: /* 1 101 100 111 */ cp1600_cmpat(4,7);		break;

		case 0x368: /* 1 101 101 000 */ cp1600_cmpat(5,0);		break;
		case 0x369: /* 1 101 101 001 */ cp1600_cmpat(5,1);		break;
		case 0x36a: /* 1 101 101 010 */ cp1600_cmpat(5,2);		break;
		case 0x36b: /* 1 101 101 011 */ cp1600_cmpat(5,3);		break;
		case 0x36c: /* 1 101 101 100 */ cp1600_cmpat(5,4);		break;
		case 0x36d: /* 1 101 101 101 */ cp1600_cmpat(5,5);		break;
		case 0x36e: /* 1 101 101 110 */ cp1600_cmpat(5,6);		break;
		case 0x36f: /* 1 101 101 111 */ cp1600_cmpat(5,7);		break;

		case 0x370: /* 1 101 110 000 */ cp1600_cmpat(6,0);		break;
		case 0x371: /* 1 101 110 001 */ cp1600_cmpat(6,1);		break;
		case 0x372: /* 1 101 110 010 */ cp1600_cmpat(6,2);		break;
		case 0x373: /* 1 101 110 011 */ cp1600_cmpat(6,3);		break;
		case 0x374: /* 1 101 110 100 */ cp1600_cmpat(6,4);		break;
		case 0x375: /* 1 101 110 101 */ cp1600_cmpat(6,5);		break;
		case 0x376: /* 1 101 110 110 */ cp1600_cmpat(6,6);		break;
		case 0x377: /* 1 101 110 111 */ cp1600_cmpat(6,7);		break;

		case 0x378: /* 1 101 111 000 */ cp1600_cmpi(0);			break;
		case 0x379: /* 1 101 111 001 */ cp1600_cmpi(1);			break;
		case 0x37a: /* 1 101 111 010 */ cp1600_cmpi(2);			break;
		case 0x37b: /* 1 101 111 011 */ cp1600_cmpi(3);			break;
		case 0x37c: /* 1 101 111 100 */ cp1600_cmpi(4);			break;
		case 0x37d: /* 1 101 111 101 */ cp1600_cmpi(5);			break;
		case 0x37e: /* 1 101 111 110 */ cp1600_cmpi(6);			break;
		case 0x37f: /* 1 101 111 111 */ cp1600_cmpi(7);			break;

		case 0x380: /* 1 110 000 000 */ cp1600_andat(0,0);		break;
		case 0x381: /* 1 110 000 001 */ cp1600_andat(0,1);		break;
		case 0x382: /* 1 110 000 010 */ cp1600_andat(0,2);		break;
		case 0x383: /* 1 110 000 011 */ cp1600_andat(0,3);		break;
		case 0x384: /* 1 110 000 100 */ cp1600_andat(0,4);		break;
		case 0x385: /* 1 110 000 101 */ cp1600_andat(0,5);		break;
		case 0x386: /* 1 110 000 110 */ cp1600_andat(0,6);		break;
		case 0x387: /* 1 110 000 111 */ cp1600_andat(0,7);		break;

		case 0x388: /* 1 110 001 000 */ cp1600_andat(1,0);		break;
		case 0x389: /* 1 110 001 001 */ cp1600_andat(1,1);		break;
		case 0x38a: /* 1 110 001 010 */ cp1600_andat(1,2);		break;
		case 0x38b: /* 1 110 001 011 */ cp1600_andat(1,3);		break;
		case 0x38c: /* 1 110 001 100 */ cp1600_andat(1,4);		break;
		case 0x38d: /* 1 110 001 101 */ cp1600_andat(1,5);		break;
		case 0x38e: /* 1 110 001 110 */ cp1600_andat(1,6);		break;
		case 0x38f: /* 1 110 001 111 */ cp1600_andat(1,7);		break;

		case 0x390: /* 1 110 010 000 */ cp1600_andat(2,0);		break;
		case 0x391: /* 1 110 010 001 */ cp1600_andat(2,1);		break;
		case 0x392: /* 1 110 010 010 */ cp1600_andat(2,2);		break;
		case 0x393: /* 1 110 010 011 */ cp1600_andat(2,3);		break;
		case 0x394: /* 1 110 010 100 */ cp1600_andat(2,4);		break;
		case 0x395: /* 1 110 010 101 */ cp1600_andat(2,5);		break;
		case 0x396: /* 1 110 010 110 */ cp1600_andat(2,6);		break;
		case 0x397: /* 1 110 010 111 */ cp1600_andat(2,7);		break;

		case 0x398: /* 1 110 011 000 */ cp1600_andat(3,0);		break;
		case 0x399: /* 1 110 011 001 */ cp1600_andat(3,1);		break;
		case 0x39a: /* 1 110 011 010 */ cp1600_andat(3,2);		break;
		case 0x39b: /* 1 110 011 011 */ cp1600_andat(3,3);		break;
		case 0x39c: /* 1 110 011 100 */ cp1600_andat(3,4);		break;
		case 0x39d: /* 1 110 011 101 */ cp1600_andat(3,5);		break;
		case 0x39e: /* 1 110 011 110 */ cp1600_andat(3,6);		break;
		case 0x39f: /* 1 110 011 111 */ cp1600_andat(3,7);		break;

		case 0x3a0: /* 1 110 100 000 */ cp1600_andat(4,0);		break;
		case 0x3a1: /* 1 110 100 001 */ cp1600_andat(4,1);		break;
		case 0x3a2: /* 1 110 100 010 */ cp1600_andat(4,2);		break;
		case 0x3a3: /* 1 110 100 011 */ cp1600_andat(4,3);		break;
		case 0x3a4: /* 1 110 100 100 */ cp1600_andat(4,4);		break;
		case 0x3a5: /* 1 110 100 101 */ cp1600_andat(4,5);		break;
		case 0x3a6: /* 1 110 100 110 */ cp1600_andat(4,6);		break;
		case 0x3a7: /* 1 110 100 111 */ cp1600_andat(4,7);		break;

		case 0x3a8: /* 1 110 101 000 */ cp1600_andat(5,0);		break;
		case 0x3a9: /* 1 110 101 001 */ cp1600_andat(5,1);		break;
		case 0x3aa: /* 1 110 101 010 */ cp1600_andat(5,2);		break;
		case 0x3ab: /* 1 110 101 011 */ cp1600_andat(5,3);		break;
		case 0x3ac: /* 1 110 101 100 */ cp1600_andat(5,4);		break;
		case 0x3ad: /* 1 110 101 101 */ cp1600_andat(5,5);		break;
		case 0x3ae: /* 1 110 101 110 */ cp1600_andat(5,6);		break;
		case 0x3af: /* 1 110 101 111 */ cp1600_andat(5,7);		break;

		case 0x3b0: /* 1 110 110 000 */ cp1600_andat(6,0);		break;
		case 0x3b1: /* 1 110 110 001 */ cp1600_andat(6,1);		break;
		case 0x3b2: /* 1 110 110 010 */ cp1600_andat(6,2);		break;
		case 0x3b3: /* 1 110 110 011 */ cp1600_andat(6,3);		break;
		case 0x3b4: /* 1 110 110 100 */ cp1600_andat(6,4);		break;
		case 0x3b5: /* 1 110 110 101 */ cp1600_andat(6,5);		break;
		case 0x3b6: /* 1 110 110 110 */ cp1600_andat(6,6);		break;
		case 0x3b7: /* 1 110 110 111 */ cp1600_andat(6,7);		break;

		case 0x3b8: /* 1 110 111 000 */ cp1600_andi(0);			break;
		case 0x3b9: /* 1 110 111 001 */ cp1600_andi(1);			break;
		case 0x3ba: /* 1 110 111 010 */ cp1600_andi(2);			break;
		case 0x3bb: /* 1 110 111 011 */ cp1600_andi(3);			break;
		case 0x3bc: /* 1 110 111 100 */ cp1600_andi(4);			break;
		case 0x3bd: /* 1 110 111 101 */ cp1600_andi(5);			break;
		case 0x3be: /* 1 110 111 110 */ cp1600_andi(6);			break;
		case 0x3bf: /* 1 110 111 111 */ cp1600_andi(7);			break;

		case 0x3c0: /* 1 111 000 000 */ cp1600_xorat(0,0);		break;
		case 0x3c1: /* 1 111 000 001 */ cp1600_xorat(0,1);		break;
		case 0x3c2: /* 1 111 000 010 */ cp1600_xorat(0,2);		break;
		case 0x3c3: /* 1 111 000 011 */ cp1600_xorat(0,3);		break;
		case 0x3c4: /* 1 111 000 100 */ cp1600_xorat(0,4);		break;
		case 0x3c5: /* 1 111 000 101 */ cp1600_xorat(0,5);		break;
		case 0x3c6: /* 1 111 000 110 */ cp1600_xorat(0,6);		break;
		case 0x3c7: /* 1 111 000 111 */ cp1600_xorat(0,7);		break;

		case 0x3c8: /* 1 111 001 000 */ cp1600_xorat(1,0);		break;
		case 0x3c9: /* 1 111 001 001 */ cp1600_xorat(1,1);		break;
		case 0x3ca: /* 1 111 001 010 */ cp1600_xorat(1,2);		break;
		case 0x3cb: /* 1 111 001 011 */ cp1600_xorat(1,3);		break;
		case 0x3cc: /* 1 111 001 100 */ cp1600_xorat(1,4);		break;
		case 0x3cd: /* 1 111 001 101 */ cp1600_xorat(1,5);		break;
		case 0x3ce: /* 1 111 001 110 */ cp1600_xorat(1,6);		break;
		case 0x3cf: /* 1 111 001 111 */ cp1600_xorat(1,7);		break;

		case 0x3d0: /* 1 111 010 000 */ cp1600_xorat(2,0);		break;
		case 0x3d1: /* 1 111 010 001 */ cp1600_xorat(2,1);		break;
		case 0x3d2: /* 1 111 010 010 */ cp1600_xorat(2,2);		break;
		case 0x3d3: /* 1 111 010 011 */ cp1600_xorat(2,3);		break;
		case 0x3d4: /* 1 111 010 100 */ cp1600_xorat(2,4);		break;
		case 0x3d5: /* 1 111 010 101 */ cp1600_xorat(2,5);		break;
		case 0x3d6: /* 1 111 010 110 */ cp1600_xorat(2,6);		break;
		case 0x3d7: /* 1 111 010 111 */ cp1600_xorat(2,7);		break;

		case 0x3d8: /* 1 111 011 000 */ cp1600_xorat(3,0);		break;
		case 0x3d9: /* 1 111 011 001 */ cp1600_xorat(3,1);		break;
		case 0x3da: /* 1 111 011 010 */ cp1600_xorat(3,2);		break;
		case 0x3db: /* 1 111 011 011 */ cp1600_xorat(3,3);		break;
		case 0x3dc: /* 1 111 011 100 */ cp1600_xorat(3,4);		break;
		case 0x3dd: /* 1 111 011 101 */ cp1600_xorat(3,5);		break;
		case 0x3de: /* 1 111 011 110 */ cp1600_xorat(3,6);		break;
		case 0x3df: /* 1 111 011 111 */ cp1600_xorat(3,7);		break;

		case 0x3e0: /* 1 111 100 000 */ cp1600_xorat(4,0);		break;
		case 0x3e1: /* 1 111 100 001 */ cp1600_xorat(4,1);		break;
		case 0x3e2: /* 1 111 100 010 */ cp1600_xorat(4,2);		break;
		case 0x3e3: /* 1 111 100 011 */ cp1600_xorat(4,3);		break;
		case 0x3e4: /* 1 111 100 100 */ cp1600_xorat(4,4);		break;
		case 0x3e5: /* 1 111 100 101 */ cp1600_xorat(4,5);		break;
		case 0x3e6: /* 1 111 100 110 */ cp1600_xorat(4,6);		break;
		case 0x3e7: /* 1 111 100 111 */ cp1600_xorat(4,7);		break;

		case 0x3e8: /* 1 111 101 000 */ cp1600_xorat(5,0);		break;
		case 0x3e9: /* 1 111 101 001 */ cp1600_xorat(5,1);		break;
		case 0x3ea: /* 1 111 101 010 */ cp1600_xorat(5,2);		break;
		case 0x3eb: /* 1 111 101 011 */ cp1600_xorat(5,3);		break;
		case 0x3ec: /* 1 111 101 100 */ cp1600_xorat(5,4);		break;
		case 0x3ed: /* 1 111 101 101 */ cp1600_xorat(5,5);		break;
		case 0x3ee: /* 1 111 101 110 */ cp1600_xorat(5,6);		break;
		case 0x3ef: /* 1 111 101 111 */ cp1600_xorat(5,7);		break;

		case 0x3f0: /* 1 111 110 000 */ cp1600_xorat(6,0);		break;
		case 0x3f1: /* 1 111 110 001 */ cp1600_xorat(6,1);		break;
		case 0x3f2: /* 1 111 110 010 */ cp1600_xorat(6,2);		break;
		case 0x3f3: /* 1 111 110 011 */ cp1600_xorat(6,3);		break;
		case 0x3f4: /* 1 111 110 100 */ cp1600_xorat(6,4);		break;
		case 0x3f5: /* 1 111 110 101 */ cp1600_xorat(6,5);		break;
		case 0x3f6: /* 1 111 110 110 */ cp1600_xorat(6,6);		break;
		case 0x3f7: /* 1 111 110 111 */ cp1600_xorat(6,7);		break;

		case 0x3f8: /* 1 111 111 000 */ cp1600_xori(0);			break;
		case 0x3f9: /* 1 111 111 001 */ cp1600_xori(1);			break;
		case 0x3fa: /* 1 111 111 010 */ cp1600_xori(2);			break;
		case 0x3fb: /* 1 111 111 011 */ cp1600_xori(3);			break;
		case 0x3fc: /* 1 111 111 100 */ cp1600_xori(4);			break;
		case 0x3fd: /* 1 111 111 101 */ cp1600_xori(5);			break;
		case 0x3fe: /* 1 111 111 110 */ cp1600_xori(6);			break;
		case 0x3ff: /* 1 111 111 111 */ cp1600_xori(7);			break;
        }
	} while( cp1600_icount > 0 );

	return cycles - cp1600_icount;
}

/* Get registers, return context size */
unsigned cp1600_get_context(void *dst)
{
	if( dst )
		memcpy(dst, &cp1600, sizeof(CP1600));
	return sizeof(CP1600);
}

/* Set registers */
void cp1600_set_context(void *src)
{
	if( src )
		memcpy(&cp1600, src, sizeof(CP1600));
}

/* Get program counter */
unsigned cp1600_get_pc(void)
{
	return cp1600.r[7];
}

/* Set program counter */
void cp1600_set_pc(unsigned val)
{
	cp1600.r[7] = val;
}

/* Get stack pointer */
unsigned cp1600_get_sp(void)
{
	return 0;
}

/* Set stack pointer */
void cp1600_set_sp(unsigned val)
{
	/* nothing */
}


unsigned cp1600_get_reg(int regnum)
{
	switch( regnum )
	{
	case CP1600_R0: return cp1600.r[0];
	case CP1600_R1: return cp1600.r[1];
	case CP1600_R2: return cp1600.r[2];
	case CP1600_R3: return cp1600.r[3];
	case CP1600_R4: return cp1600.r[4];
	case CP1600_R5: return cp1600.r[5];
	case CP1600_R6: return cp1600.r[6];
	case CP1600_R7: return cp1600.r[7];
	}
	return 0;
}

void cp1600_set_reg (int regnum, unsigned val)
{
	switch( regnum )
	{
	case CP1600_R0: cp1600.r[0] = val; break;
	case CP1600_R1: cp1600.r[1] = val; break;
	case CP1600_R2: cp1600.r[2] = val; break;
	case CP1600_R3: cp1600.r[3] = val; break;
	case CP1600_R4: cp1600.r[4] = val; break;
	case CP1600_R5: cp1600.r[5] = val; break;
	case CP1600_R6: cp1600.r[6] = val; break;
	case CP1600_R7: cp1600.r[7] = val; break;
    }
}

void cp1600_set_nmi_line(int state)
{
	/* not applicable */
}

void cp1600_set_irq_line(int irqline, int state)
{
	switch( irqline )
	{
	}
}

void cp1600_set_irq_callback(int (*callback)(int irqline))
{
	cp1600.irq_callback = callback;
}

void cp1600_state_save(void *file)
{
}

void cp1600_state_load(void *file)
{
}

const char *cp1600_info(void *context, int regnum)
{
	static char buffer[8][15+1];
	static int which = 0;
	CP1600 *r = context;

	which = ++which % 8;
	buffer[which][0] = '\0';
	if( !context )
		r = &cp1600;

    switch( regnum )
	{
		case CPU_INFO_REG+CP1600_R0:sprintf(buffer[which], "R0:%04X", r->r[0]); break;
		case CPU_INFO_REG+CP1600_R1:sprintf(buffer[which], "R1:%04X", r->r[1]); break;
		case CPU_INFO_REG+CP1600_R2:sprintf(buffer[which], "R2:%04X", r->r[2]); break;
		case CPU_INFO_REG+CP1600_R3:sprintf(buffer[which], "R3:%04X", r->r[3]); break;
		case CPU_INFO_REG+CP1600_R4:sprintf(buffer[which], "R4:%04X", r->r[4]); break;
		case CPU_INFO_REG+CP1600_R5:sprintf(buffer[which], "R5:%04X", r->r[5]); break;
		case CPU_INFO_REG+CP1600_R6:sprintf(buffer[which], "R6:%04X", r->r[6]); break;
		case CPU_INFO_REG+CP1600_R7:sprintf(buffer[which], "R7:%04X", r->r[7]); break;
        case CPU_INFO_FLAGS:
			sprintf(buffer[which], "%c%c%c%c",
				r->flags & 0x80 ? 'S':'.',
				r->flags & 0x40 ? 'Z':'.',
				r->flags & 0x10 ? 'V':'.',
				r->flags & 0x10 ? 'C':'.');
			break;
		case CPU_INFO_NAME: return "CP1600";
		case CPU_INFO_FAMILY: return "????";
		case CPU_INFO_VERSION: return "1.0";
		case CPU_INFO_FILE: return __FILE__;
		case CPU_INFO_CREDITS: return "Copyright (c) 2000 Frank Palazzolo, all rights reserved.";
		case CPU_INFO_REG_LAYOUT: return (const char*)cp1600_reg_layout;
		case CPU_INFO_WIN_LAYOUT: return (const char*)cp1600_win_layout;
	}
    return buffer[which];
}

unsigned cp1600_dasm(char *buffer, unsigned pc)
{
#ifdef MAME_DEBUG
/*	return DasmCP1600( buffer, pc ); */
	sprintf( buffer, "$%02X", cpu_readop(pc) );
    return 1;
#else
	sprintf( buffer, "$%02X", cpu_readop(pc) );
	return 1;
#endif
}

