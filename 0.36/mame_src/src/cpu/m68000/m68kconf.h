#ifndef M68KCONF__HEADER
#define M68KCONF__HEADER

#include "osd_cpu.h"
#include "mamedbg.h"

/* ======================================================================== */
/* ============================= CONFIGURATION ============================ */
/* ======================================================================== */

/* Configuration switches.  1 = ON, 0 = OFF */

/* If on, CPU will call the interrupt_acknowledge callback when it services an
 * interrupt.
 * If off, all interrupts will be autovectored.
 */
#define M68K_INT_ACK       0

/* If on, CPU will call the breakpoint acknowledge callback when it encounters
 * a breakpoint instruction and it is running in 68010+ mode.
 */
#define M68K_BKPT_ACK      0

/* If on, the CPU will monitor the trace flags and take trace exceptions
 */
#define M68K_TRACE         0

/* If on, CPU will actually halt when m68k_input_halt is called by the host.
 * I allow it to be disabled for a very slight speed increase.
 */
#define M68K_HALT          0

/* If on, CPU will call the output reset callback when it encounters a reset
 * instruction.
 */
#define M68K_OUTPUT_RESET  0

/* If on, CPU will call the pc changed callback when it changes the PC by a
 * large value.  This allows host programs to be nicer when it comes to
 * fetching immediate data and instructions on a banked memory system.
 */
#define M68K_PC_CHANGED    0

/* If on, CPU will call the set fc callback on every memory access to
 * differentiate between user/supervisor, program/data access like a real
 * 68000 would.  This should be enabled and the callback should be set if you
 * are going to emulate the m68010. (moves uses function codes to read/write
 * data from different address spaces)
 */
#define M68K_SET_FC        0

/* If on, CPU will call the instruction hook callback before every
 * instruction.
 */
#define M68K_INSTR_HOOK    0

/* If on, the CPU will simulate the 4-byte prefetch queue of a real 68000 */
#define M68K_USE_PREFETCH  0

/* Define the default mode the CPU runs in if none has been specified */
#define M68K_DEFAULT_CPU_MODE   M68K_CPU_MODE_68000


/* Uncomment this to enable logging of illegal instruction calls.
 * M68K_LOG must be #defined to a stdio file stream.  Logging will only
 * occur if the file stream is not NULL.
 * Turn on M68K_LOG_EMULATED_INSTRUCTIONS to log all 1010 and 1111 calls.
#include <stdio.h>
extern FILE* some_file_handle
#define M68K_LOG some_file_handle
#define M68K_LOG_EMULATED_INSTRUCTIONS 1
 */


/* Set to your compiler's static inline keyword.  If your compiler doesn't
 * have inline, just set this to static.
 * If you define INLINE in the makefile, it will override this value.
 */
#ifndef INLINE
#define INLINE static __inline__
#endif



/* ======================================================================== */
/* ============================== MAME STUFF ============================== */
/* ======================================================================== */

/* Uncomment this to enable mame stuff */
/*
*/
#include "m68kmame.h"



/* ======================================================================== */
/* ============================== END OF FILE ============================= */
/* ======================================================================== */

#endif /* M68KCONF__HEADER */
