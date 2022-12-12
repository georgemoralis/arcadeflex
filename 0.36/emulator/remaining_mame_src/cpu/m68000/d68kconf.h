#ifndef D68KCONF__HEADER
#define D68KCONF__HEADER

/* ======================================================================== */
/* ============================= CONFIGURATION ============================ */
/* ======================================================================== */

/* Set to 1 if you are building MAME */
#define M68000_BUILDING_MAME 1



/* ======================================================================== */
/* ============================== MAME STUFF ============================== */
/* ======================================================================== */

#if M68000_BUILDING_MAME

#include "memory.h"
#define m68k_read_memory_8(address)  cpu_readmem24(address)
#define m68k_read_memory_16(address) cpu_readmem24_word(address)
#define m68k_read_memory_32(address) cpu_readmem24_dword(address)


#define Dasm68000(buff, pc) m68000_disassemble(buff, pc)


#endif /* M68000_BUILDING_MAME */



/* ======================================================================== */
/* ============================== END OF FILE ============================= */
/* ======================================================================== */

#endif /* D68KCONF__HEADER */
