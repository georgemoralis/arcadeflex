/*****************************************************************************
 *
 *	 sc61860.c
 *	 portable sharp 61860 emulator interface
 *   (sharp pocket computers)
 *
 *	 Copyright (c) 2000 Peter Trauner, all rights reserved.
 *
 *	 - This source code is released as freeware for non-commercial purposes.
 *	 - You are free to use and redistribute this code in modified or
 *	   unmodified form, provided you list me in the credits.
 *	 - If you modify this source code, you must add a notice to each modified
 *	   source file that it has been changed.  If you're a nice person, you
 *	   will clearly mark each change too.  :)
 *	 - If you wish to use this for commercial purposes, please contact me at
 *	   peter.trauner@jk.uni-linz.ac.at
 *	 - The author of this copywritten work reserves the right to change the
 *	   terms of its usage and license at any time, including retroactively
 *	 - This entire notice must remain in the source code.
 *
 *****************************************************************************/

#include <stdio.h>
#ifdef MAME_DEBUG
#include "driver.h"
#include "mamedbg.h"

#include "sc61860.h"
#include "sc.h"

/* explanations for the sharp mnemonics
   d data: external memory
   m memory: internal memory (address in p register)
   p register (internal memory adress)
   q register (internal memory adress), internally used in several opcodes!?
   r stack pointer (internal memory)
   c carry flag
   z zero flag

   the following are special internal memory registers
   a akkumulator: (2)
   b b register: (3)
   i,j,k,l,v,w counter
   x external memory address
   y external memory address
   ia input/output (92)
   ib input/output (93)
   f0 output (94)
   c output(95)

   li load immediate
   ld load accu
   st store accu
   or
   an and
   inc
   dec
   ad add
   sb sub
   cp compare
   jr jump relativ
   jp jump absolut
   mv move
   ex exchange
*/


typedef enum { 
	Ill,
	Imp, 
	Imm, ImmW,
	RelP, RelM, 
	Abs, 
	Ptc,
	Etc, 
	Cal, 
	Lp 
} Adr;

static const struct { char *mnemonic; Adr adr; } table[]={
	{ "LII",	Imm	}, { "LIJ",		Imm }, { "LIA",		Imm	}, { "LIB",		Imm },
	{ "IX",		Imp	}, { "DX",		Imp }, { "IY",		Imp	}, { "DY",		Imp },
	{ "MVW",	Imp	}, { "EXW",		Imp }, { "MVB",		Imp	}, { "EXB",		Imp },
	{ "ADN",	Imp	}, { "SBN",		Imp }, { "ADW",		Imp	}, { "SBW",		Imp },

	{ "LIDP",	ImmW}, { "LIDL",	Imm }, { "LIP",		Imm	}, { "LIQ",		Imm },
	{ "ADB",	Imp	}, { "SBB",		Imp }, { "LIDP",	ImmW}, { "LIDL",	Imm },
	{ "MVWD",	Imp	}, { "EXWD",	Imp }, { "MVBD",	Imp	}, { "EXBD",	Imp },
	{ "SRW",	Imp	}, { "SLW",		Imp }, { "FILM",	Imp	}, { "FILD",	Imp },

	{ "LDP",	Imp	}, { "LPQ",		Imp }, { "LPR",		Imp	}, { 0,			Ill },
	{ "IXL",	Imp	}, { "DXL",		Imp }, { "IYS",		Imp	}, { "DYS",		Imp },
	{ "JRNZP",	RelP}, { "JRNZM",	RelM}, { "JRNCP",	RelP}, { "JRNCM",	RelM},
	{ "JRP",	RelP}, { "JRM",		RelM}, { 0,			Ill	}, { "LOOP",	RelM},

	{ "STP",	Imp	}, { "STQ",		Imp }, { "STR",		Imp	}, { 0,			Ill },
	{ "PUSH",	Imp	}, { "DATA",	Imp }, { 0,			Ill	}, { "RTN",		Imp },
	{ "JRZP",	RelP}, { "JRZM",	RelM}, { "JRCP",	RelP}, { "JRCM",	RelM},
	{ 0,		Ill	}, { 0,			Ill }, { 0,			Ill	}, { 0,			Ill },

	{ "INCI",	Imp	}, { "DECI",	Imp }, { "INCA",	Imp	}, { "DECA",	Imp },
	{ "ADM",	Imp	}, { "SBM",		Imp }, { "ANMA",	Imp	}, { "ORMA",	Imp },
	{ "INCK",	Imp	}, { "DECK",	Imp }, { "INCV",	Imp	}, { "DECV",	Imp },
	{ "INA",	Imp	}, { "NOPW",	Imp }, { "WAIT",	Imm	}, { 0,			Ill },

	{ "INCP",	Imp	}, { "DECP",	Imp }, { "STD",		Imp	}, { "MVDM",	Imp },
	{ 0,		Ill	}, { "MVMD",	Imp }, { 0,			Ill	}, { "LDD",		Imp },
	{ "SWP",	Imp	}, { "LDM",		Imp }, { "SL",		Imp	}, { "POP",		Imp },
	{ 0,		Ill	}, { "OUTA",	Imp }, { 0,			Ill	}, { "OUTF",	Imp },

	{ "ANIM",	Imm	}, { "ORIM",	Imm }, { "TSIM",	Imm	}, { "CPIM",	Imm },
	{ "ANIA",	Imm	}, { "ORIA",	Imm }, { "TSIA",	Imm	}, { "CPIA",	Imm },
	{ 0,		Ill	}, { "ETC",		Etc }, { 0,			Ill	}, { "TEST",	Imm },
	{ 0,		Ill	}, { 0,			Ill }, { 0,			Ill	}, { 0,			Ill },

	{ "ADIM",	Imm	}, { "SBIM",	Imm }, { 0,			Ill	}, { 0,			Ill },
	{ "ADIA",	Imm	}, { "SBIA",	Imm }, { 0,			Ill	}, { 0,			Ill },
	{ "CALL",	Abs	}, { "JP",		Abs }, { "PTC",		Ptc	}, { 0,			Ill },
	{ "JPNZ",	Abs	}, { "JPNC",	Abs }, { "JPZ",		Abs	}, { "JPC",		Abs },


	{ "LP",	Lp	}, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },
	{ 0 }, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },

	{ 0 }, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },
	{ 0 }, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },

	{ 0 }, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },
	{ 0 }, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },

	{ 0 }, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },
	{ 0 }, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },

	{ "INCJ",	Imp	}, { "DECJ",	Imp }, { "INCB",	Imp	}, { "DECB",	Imp },
	{ "ACDM",	Imp	}, { "SBCM",	Imp }, { 0,			Ill	}, { "CPMA",	Imp },
	{ "INCL",	Imp	}, { "DECL",	Imp }, { "INCW",	Imp	}, { "DECW",	Imp },
	{ "INB",	Imp	}, { 0,			Ill }, { "NOPT",	Imp	}, { 0,			Ill },

	{ "SC",		Imp	}, { "RC",		Imp }, { "SR",		Imp	}, { 0,			Ill },
	{ "ANID",	Imm	}, { "ORID",	Imm }, { "TSID",	Imm	}, { 0,			Ill },
	{ "LEAVE",	Imp	}, { 0,			Ill }, { "EXAB",	Imp	}, { "EXAM",	Imp },
	{ 0,		Ill	}, { "OUTB",	Imp }, { 0,			Ill	}, { "OUTC",	Imp },

	{ "CAL", Imp }, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },
	{ 0 }, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },

	{ 0 }, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },
	{ 0 }, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },
};

unsigned sc61860_dasm(char *dst, unsigned oldpc)
{
	int pc=oldpc;
	int oper=PEEK_OP(pc++);
	int t;
	UINT16 adr;

	switch(oper&0xc0) {
	case 0x80: 
		sprintf(dst,"%-6s%.2x",table[oper&0x80].mnemonic, oper&0x3f);
		break;
	default:
		switch(oper&0xe0) {
		case 0xe0: 
			sprintf(dst,"%-6s%.4x",table[oper&0xe0].mnemonic,
					PEEK_OP(pc++)|((oper&0x1f)<<8)); 
			break;
		default:
			switch (table[oper].adr) {
			case Ill: sprintf(dst,"?%.2x",oper);break;
			case Imp: sprintf(dst,"%s",table[oper].mnemonic); break;
			case Imm: sprintf(dst,"%-6s%.2x",table[oper].mnemonic, PEEK_OP(pc++)); break;
			case ImmW: 
				adr=(PEEK_OP(pc)<<8)|PEEK_OP(pc+1);pc+=2;
				sprintf(dst,"%-6s%.4x",table[oper].mnemonic, adr); 
				break;
			case Abs: 
				adr=(PEEK_OP(pc)<<8)|PEEK_OP(pc+1);pc+=2;
				sprintf(dst,"%-6s%.4x",table[oper].mnemonic, adr); 
				break;
			case RelM: 
				adr=pc-PEEK_OP(pc++);
				sprintf(dst,"%-6s%.4x",table[oper].mnemonic, adr&0xffff); 
				break;
			case RelP:
				adr=pc+PEEK_OP(pc++);
				sprintf(dst,"%-6s%.4x",table[oper].mnemonic, adr&0xffff); 
				break;
			case Ptc:
				t=PEEK_OP(pc++);
				adr=(PEEK_OP(pc)<<8)|PEEK_OP(pc+1);pc+=2;
				sprintf(dst,"%-6s%.2x,%.4x",table[oper].mnemonic,t, adr);
				break;
			case Etc:
				sprintf(dst,"%-6s",table[oper].mnemonic);
				/*H imm, abs */
				/* abs */
				break;
			case Cal: case Lp: break;
			}
			break;
		}
		break;
	}
	return pc-oldpc;
}
#endif	/* MAME_DEBUG */


