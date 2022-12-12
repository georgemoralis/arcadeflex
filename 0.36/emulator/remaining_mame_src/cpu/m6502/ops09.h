/*****************************************************************************
 *
 *	 ops09.h
 *
 *	 Copyright (c) 2000 Peter Trauner, all rights reserved.
 *   documentation by michael steil mist@c64.org
 *   available at ftp://ftp.funet.fi/pub/cbm/c65
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
 *****************************************************************************/

#define m6502 m6509
#define m6502_ICount m6509_ICount

#define ZPWH	m6509.zp.w.h

#define EAWH	m6509.ea.w.h

#define PBWH	m6509.pc_bank.w.h
#define PB		m6509.pc_bank.d

#define IBWH	m6509.ind_bank.w.h
#define IB		m6509.ind_bank.d

/***************************************************************
 *  RDOP    read an opcode
 ***************************************************************/
#undef RDOP
#define RDOP() cpu_readop((PCW++)|PB)

/***************************************************************
 *  RDOPARG read an opcode argument
 ***************************************************************/
#undef RDOPARG
#define RDOPARG() cpu_readop_arg((PCW++)|PB)

/***************************************************************
 *  RDMEM   read memory
 ***************************************************************/
#undef RDMEM
#define RDMEM(addr) cpu_readmem20(addr)

/***************************************************************
 *  WRMEM   write memory
 ***************************************************************/
#undef WRMEM
#define WRMEM(addr,data) cpu_writemem20(addr,data)

/***************************************************************
 * push a register onto the stack
 ***************************************************************/
#undef PUSH
#define PUSH(Rg) WRMEM(SPD|PB, Rg); S--

/***************************************************************
 * pull a register from the stack
 ***************************************************************/
#undef PULL
#define PULL(Rg) S++; Rg = RDMEM(SPD|PB)


/***************************************************************
 *  EA = zero page address
 ***************************************************************/
#undef EA_ZPG
#define EA_ZPG													\
	ZPL = RDOPARG();											\
	ZPWH = PBWH;												\
    EAD = ZPD

/***************************************************************
 *  EA = zero page address + X
 ***************************************************************/
#undef EA_ZPX
#define EA_ZPX													\
	ZPL = RDOPARG() + X;										\
	ZPWH = PBWH;												\
    EAD = ZPD

/***************************************************************
 *  EA = zero page address + Y
 ***************************************************************/
#undef EA_ZPY
#define EA_ZPY													\
	ZPL = RDOPARG() + Y;										\
	ZPWH = PBWH;												\
    EAD = ZPD

/***************************************************************
 *  EA = absolute address
 ***************************************************************/
#undef EA_ABS
#define EA_ABS													\
	EAL = RDOPARG();											\
	EAH = RDOPARG();											\
    EAWH = PBWH

/***************************************************************
 *	EA = zero page indirect (65c02 pre indexed w/o X)
 ***************************************************************/
#undef EA_ZPI
#define EA_ZPI													\
	ZPL = RDOPARG();											\
	ZPWH=PBWH;													\
	EAL = RDMEM(ZPD);											\
	ZPL++;														\
	EAH = RDMEM(ZPD);											\
    EAWH = PBWH

/***************************************************************
 *  EA = zero page + X indirect (pre indexed)
 ***************************************************************/
#undef EA_IDX
#define EA_IDX													\
	ZPL = RDOPARG() + X;										\
	ZPWH=PBWH;													\
	EAL = RDMEM(ZPD);											\
	ZPL++;														\
	EAH = RDMEM(ZPD);											\
    EAWH = PBWH

/***************************************************************
 *  EA = zero page indirect + Y (post indexed)
 *	subtract 1 cycle if page boundary is crossed
 ***************************************************************/
#undef EA_IDY
#define EA_IDY													\
	ZPL = RDOPARG();											\
	ZPWH=PBWH;													\
	EAL = RDMEM(ZPD);											\
	ZPL++;														\
	EAH = RDMEM(ZPD);											\
	EAWH = PBWH;												\
    if (EAL + Y > 0xff)                                         \
		m6509_ICount--; 										\
	EAW += Y


/***************************************************************
 *  EA = zero page indirect + Y (post indexed)
 *	subtract 1 cycle if page boundary is crossed
 ***************************************************************/
#define EA_IDY_6509 											\
	ZPL = RDOPARG();											\
	ZPWH=PBWH;													\
	EAL = RDMEM(ZPD);											\
	ZPL++;														\
	EAH = RDMEM(ZPD);											\
	EAWH = IBWH;												\
    if (EAL + Y > 0xff)                                         \
		m6509_ICount--; 										\
	EAW += Y

/***************************************************************
 *	EA = indirect (only used by JMP)
 ***************************************************************/
#undef EA_IND
#define EA_IND													\
	EA_ABS; 													\
	tmp = RDMEM(EAD);											\
	EAL++;	/* booby trap: stay in same page! ;-) */			\
	EAH = RDMEM(EAD);											\
	EAL = tmp;
//    EAWH = PBWH

/***************************************************************
 *	EA = indirect plus x (only used by 65c02 JMP)
 ***************************************************************/
#undef EA_IAX
#define EA_IAX                                                  \
	EA_IND; 													\
	if (EAL + X > 0xff) /* assumption; probably wrong ? */		\
		m6509_ICount--; 										\
    EAW += X

#define RD_IDY_6509	EA_IDY_6509; tmp = RDMEM(EAD)
#define WR_IDY_6509	EA_IDY_6509; WRMEM(EAD, tmp)

/***************************************************************
 *	BRA  branch relative
 *	extra cycle if page boundary is crossed
 ***************************************************************/
#undef BRA
#define BRA(cond)                                               \
	if (cond)													\
	{															\
		tmp = RDOPARG();										\
		EAW = PCW + (signed char)tmp;							\
		m6509_ICount -= (PCH == EAH) ? 3 : 4;					\
		PCD = EAD|PB;											\
		change_pc20(PCD);										\
	}															\
	else														\
	{															\
		PCW++;													\
		m6509_ICount -= 2;										\
	}

/* 6502 ********************************************************
 *	BRK Break
 *	increment PC, push PC hi, PC lo, flags (with B bit set),
 *	set I flag, reset D flag and jump via IRQ vector
 ***************************************************************/
#undef BRK
#define BRK 													\
	PCW++;														\
	PUSH(PCH);													\
	PUSH(PCL);													\
	PUSH(P | F_B);												\
	P = (P | F_I) & ~F_D;										\
	PCL = RDMEM(M6509_IRQ_VEC); 								\
	PCH = RDMEM(M6509_IRQ_VEC+1);								\
	change_pc20(PCD)


/* 6502 ********************************************************
 *	ILL Illegal opcode
 ***************************************************************/
#undef ILL
#define ILL 													\
	if (errorlog)												\
		fprintf(errorlog, "M6509 illegal opcode %05x: %02x\n",  \
			((PCW-1)&0xffff)|PB, cpu_readop((PCW-1)&0xffff)|PB)

/* 6502 ********************************************************
 *	JMP Jump to address
 *	set PC to the effective address
 ***************************************************************/
#undef JMP
#define JMP 													\
	if( EAD == PPC && !m6509.pending_irq && !m6509.after_cli )	\
		if( m6509_ICount > 0 ) m6509_ICount = 0;				\
	PCD = EAD;													\
	change_pc20(PCD)

/* 6502 ********************************************************
 *	JSR Jump to subroutine
 *	decrement PC (sic!) push PC hi, push PC lo and set
 *	PC to the effective address
 ***************************************************************/
#undef JSR
#define JSR 													\
	EAL = RDOPARG();											\
	PUSH(PCH);													\
	PUSH(PCL);													\
	EAH = RDOPARG();											\
	EAWH = PBWH;												\
	PCD = EAD;													\
	change_pc20(PCD)

/* 6502 ********************************************************
 * RTI	Return from interrupt
 * pull flags, pull PC lo, pull PC hi and increment PC
 *	PCW++;
 ***************************************************************/
#undef RTI
#define RTI 													\
	PULL(P);													\
	PULL(PCL);													\
    PULL(PCH);                                                  \
	P |= F_T;													\
	if( (m6509.irq_state != CLEAR_LINE) && !(P & F_I) ) 		\
	{															\
		LOG((errorlog, "M6509#%d RTI sets after_cli\n",cpu_getactivecpu())); \
		m6509.after_cli = 1;									\
	}															\
    change_pc20(PCD)

/* 6502 ********************************************************
 *	RTS Return from subroutine
 *	pull PC lo, PC hi and increment PC
 ***************************************************************/
#undef RTS
#define RTS 													\
	PULL(PCL);													\
	PULL(PCH);													\
	PCW++;														\
	change_pc20(PCD)

/* 6510 ********************************************************
 * SAH	store accumulator and index X and high + 1
 * result = accumulator and index X and memory [PC+1] + 1
 ***************************************************************/
#undef SAH
#define SAH 													\
	tmp = A & X;												\
	tmp &= (cpu_readop_arg(((PCW + 1) & 0xffff)|PB) + 1)

/* 6510 ********************************************************
 * SSH	store stack high
 * logical and accumulator with index X, transfer result to S
 * logical and result with memory [PC+1] + 1
 ***************************************************************/
#undef SSH
#define SSH 													\
	tmp = S = A & X;											\
	tmp &= (UINT8)(cpu_readop_arg(((PCW + 1) & 0xffff)|PB) + 1)

/* 6510 ********************************************************
 * SXH	store index X high
 * logical and index X with memory[PC+1] and store the result
 ***************************************************************/
#undef SXH
#define SXH 													\
	tmp = X & (UINT8)(cpu_readop_arg(((PCW + 1) & 0xffff)|PB)

/* 6510 ********************************************************
 * SYH	store index Y and (high + 1)
 * logical and index Y with memory[PC+1] + 1 and store the result
 ***************************************************************/
#undef SYH
#define SYH 													\
	tmp = Y & (UINT8)(cpu_readop_arg(((PCW + 1) & 0xffff)|PB) + 1)



