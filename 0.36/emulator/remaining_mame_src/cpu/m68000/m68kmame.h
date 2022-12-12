#ifndef M68KMAME__HEADER
#define M68KMAME__HEADER

/* ======================================================================== */
/* ============================== MISC STUFF ============================== */
/* ======================================================================== */

#undef M68K_INT_ACK
#undef M68K_BKPT_ACK
#undef M68K_TRACE
#undef M68K_HALT
#undef M68K_OUTPUT_RESET
#undef M68K_PC_CHANGED
#undef M68K_SET_FC
#undef M68K_INSTR_HOOK
#undef M68K_DEFAULT_CPU_MODE
#undef M68K_USE_PREFETCH
#define M68K_INT_ACK            1
#define M68K_BKPT_ACK           0
#define M68K_TRACE              0
#define M68K_HALT               0
#define M68K_OUTPUT_RESET       0
#define M68K_PC_CHANGED         0 /* 1 disabled for speed */
#define M68K_SET_FC             0 /* MAME needs a way to handle function codes */
#define M68K_INSTR_HOOK         0 /* 1 disabled for speed */
#define M68K_USE_PREFETCH       1
#define M68K_DEFAULT_CPU_MODE   M68K_CPU_MODE_68000


#include "cpuintrf.h"
#include "memory.h"
#include "mamedbg.h"
#define m68k_read_memory_8(address)          cpu_readmem24(address)
#define m68k_read_memory_16(address)         cpu_readmem24_word(address)
#define m68k_read_memory_32(address)         cpu_readmem24_dword(address)

#define m68k_read_immediate_8(address)       (cpu_readop16((address)-1)&0xff)
#define m68k_read_immediate_16(address)      cpu_readop16(address)
#define m68k_read_immediate_32(address)      ((cpu_readop16(address)<<16) | cpu_readop16((address)+2))

#define m68k_read_instruction(address)       cpu_readop16(address)

#define m68k_write_memory_8(address, value)  cpu_writemem24(address, value)
#define m68k_write_memory_16(address, value) cpu_writemem24_word(address, value)
#define m68k_write_memory_32(address, value) cpu_writemem24_dword(address, value)


/* Logging stuff.  You may want to turn on logging of emulated instructions. */
/*
#include <stdio.h>
#include "driver.h"
extern  FILE* errorlog;
#define M68K_LOG errorlog
#define M68K_LOG_EMULATED_INSTRUCTIONS 1
*/


extern int m68k_clks_left;


int m68k_disassemble(char* str_buff, int pc);



/* ======================================================================== */
/* ============================== END OF FILE ============================= */
/* ======================================================================== */

#endif /* M68KMAME__HEADER */
