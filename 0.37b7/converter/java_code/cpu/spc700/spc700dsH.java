#ifndef HEADER__SPC700DS
#define HEADER__SPC700DS
/* ======================================================================== */
/* =============================== COPYRIGHT ============================== */
/* ======================================================================== */
/*

Sony SPC700 CPU Emulator V1.0

Copyright (c) 2000 Karl Stenerud
All rights reserved.

Permission is granted to use this source code for non-commercial purposes.
To use this code for commercial purposes, you must get permission from the
author (Karl Stenerud) at karl@higashiyama-unet.ocn.ne.jp.


*/

int spc700_disassemble(char* buff, unsigned int pc);

unsigned int spc700_read_8_disassembler(unsigned int address);

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package cpu.spc700;

public class spc700dsH
{
	#define spc700_read_8_disassembler(addr)				cpu_readmem16(addr)
	
	
	#endif /* HEADER__SPC700DS */
}
