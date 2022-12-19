/*****************************************************************************
 *
 *	 disasm.c
 *	 portable cosmac cdp1802 emulator interface
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

#include "cdp1802.h"

typedef enum { 
	Ill,
	Imm,
	Imp,
	Imp2, // lownibble contains register number
	Imp3, // bit 0,1,2 1..7 contains n0,n1,n2 level
	Low, // only low byte of address specified
	Abs
} Adr;

static const struct { char *mnemonic; Adr adr; } table[]={
	{ "IDL",	Imp }, { "LDN",	Imp2},{ 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },
	{ 0 }, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },

	{ "INC",	Imp2},{ 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },
	{ 0 }, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },

	{ "DEC",	Imp2},{ 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },
	{ 0 }, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },


	{ "BR",		Low	}, { "BQ",		Low }, { "BZ",		Low	}, { "BDF",		Low },
	{ "B1",		Low	}, { "B2",		Low }, { "B3",		Low	}, { "B4",		Low },
	{ "SKP",	Low	}, { "BNQ",		Low }, { "BNZ",		Low	}, { "BNF",		Low },
	{ "BN1",	Low	}, { "BN2",		Low }, { "BN3",		Low	}, { "BN4",		Low },

	{ "LDA",	Imp2},{ 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },
	{ 0 }, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },

	{ "STR",	Imp2},{ 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },
	{ 0 }, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },

	{ "IRX",	Imp } ,{ "OUT",	Imp3},{ 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 },
	{ 0 } ,{ "IMP",	Imp3},{ 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 },

	{ "RET",	Imp	}, { "DIS",		Imp }, { "LDXA",	Imp	}, { "STXD",	Imp },
	{ "ADC",	Imp	}, { "SDB",		Imp }, { "SHRC",	Imp	}, { "SMB",		Imp },
	{ "SAV",	Imp	}, { "MARK",	Imp }, { "REQ",		Imp	}, { "SEQ",		Imp },
	{ "ADCI",	Imm	}, { "SDBI",	Imm }, { "SHLC",	Imp	}, { "SMBI",	Imm },


	{ "GLO",	Imp2},{ 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },
	{ 0 }, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },

	{ "GHI",	Imp2},{ 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },
	{ 0 }, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },

	{ "PLO",	Imp2},{ 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },
	{ 0 }, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },

	{ "PHI",	Imp2},{ 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },
	{ 0 }, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },

	{ "LBR",	Abs	}, { "LBQ",		Abs }, { "LBZ",		Abs	}, { "LBDF",	Abs },
	{ "NOP",	Imp	}, { "LSNQ",	Imp }, { "LSNZ",	Imp	}, { "LSNF",	Imp },
	{ "LSKP",	Abs	}, { "LBNQ",	Abs }, { "LBNZ",	Abs	}, { "LBNF",	Abs },
	{ "LSIE",	Imp	}, { "LSQ",		Imp }, { "LSZ",		Imp	}, { "LSDF",	Imp },

	{ "SEP",	Imp2},{ 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },
	{ 0 }, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },

	{ "SEX",	Imp2},{ 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },
	{ 0 }, { 0 }, { 0 }, { 0 },  { 0 }, { 0 }, { 0 }, { 0 },

	{ "LDX",	Imp	}, { "OR",		Imp }, { "AND",		Imp	}, { "XOR",		Imp },
	{ "ADD",	Imp	}, { "SD",		Imp }, { "SHR",		Imp	}, { "SM",		Imp },
	{ "LDI",	Imm	}, { "ORI",		Imm }, { "ANI",		Imm	}, { "XRI",		Imm },
	{ "ADI",	Imm	}, { "SDI",		Imm }, { "SHL",		Imp	}, { "SMI",		Imm },

};

unsigned cdp1802_dasm(char *dst, unsigned oldpc)
{
	int pc;
	int oper;
	UINT16 absolut;
	oldpc&=0xffff;
	pc=oldpc;
	oper=cpu_readmem16(pc++);

	switch(oper&0xf0) {
	case 0:
		if (oper==0) {
			sprintf(dst,"%-5s",table[oper].mnemonic); 
		} else {
			sprintf(dst,"%-5sR%.1x",table[(oper&0xf0)|1].mnemonic, oper&0x0f); 
		}
		break;
	case 0x10:
	case 0x20:
	case 0x40:
	case 0x50:
	case 0x80:
	case 0x90:
	case 0xa0:
	case 0xb0:
	case 0xd0:
	case 0xe0:
		sprintf(dst,"%-5sR%.1x",table[oper&0xf0].mnemonic, oper&0x0f); 
		break;
	default: 		
		switch(oper&0xf8) {
		case 0x60: 
			if (oper==0x60) {
				sprintf(dst,"%-5s",table[oper].mnemonic); 
			} else {
				sprintf(dst,"%-5s%d",table[(oper&0xf8)|1].mnemonic, oper&0x7); 
			}
			break;
		case 0x68: 
			if (oper==0x68) {
				sprintf(dst,"%-5s%.2x","ill",oper); 
			} else {
				sprintf(dst,"%-5s%d",table[(oper&0xf8)|1].mnemonic, oper&0x7); 
			}
			break;
		default:
			switch (table[oper].adr) {
			case Imp:
				sprintf(dst,"%-5s",table[oper].mnemonic); 
				break;
			case Imm:
				sprintf(dst,"%-5s#%.2x",table[oper].mnemonic,cpu_readmem16(pc++)); 
				break;
			case Low:
				absolut=cpu_readmem16(pc++);
				absolut|=pc&0xff00;
				sprintf(dst,"%-5s%.4x",table[oper].mnemonic,absolut); 
				break;
			case Abs:
				absolut=cpu_readmem16(pc++)<<8;
				absolut|=cpu_readmem16(pc++);
				sprintf(dst,"%-5s%.4x",table[oper].mnemonic,absolut); 
				break;
			default:
				sprintf(dst,"%-5s%.2x","ill",oper); 
				break;
			}
			break;
		}
		break;
	}
	return pc-oldpc;
}
#endif	/* MAME_DEBUG */


