#ifndef M68K__HEADER
#define M68K__HEADER

/* ======================================================================== */
/* ========================= LICENSING & COPYRIGHT ======================== */
/* ======================================================================== */
/*
 *                                  MUSASHI
 *                                Version 2.1
 *
 * A portable Motorola M680x0 processor emulation engine.
 * Copyright 1999 Karl Stenerud.  All rights reserved.
 *
 * This code may be freely used for non-commercial purpooses as long as this
 * copyright notice remains unaltered in the source code and any binary files
 * containing this code in compiled form.
 *
 * Any commercial ventures wishing to use this code must contact the author
 * (Karl Stenerud) to negotiate commercial licensing terms.
 *
 * The latest version of this code can be obtained at:
 * (home page pending)
 */


/* ======================================================================== */
/* ============================= INSTRUCTIONS ============================= */
/* ======================================================================== */
/* 1. edit m68kconf.h and modify according to your needs.
 * 2. Implement in your host program the functions defined in
 *    "FUNCTIONS CALLED BY THE CPU" located later in this file.
 * 3. You must call m68k_pulse_reset() first to initialize the emulation.
 * 4. If you don't call m68k_set_cpu_mode(), it will default to 68000 behavior.
 *
 * Requirements:
 * - All operations in this core are done using a default size which must be
 *   at least 32 bits wide.
 * - Because of signed/unsigned operations required for CPU emulation, this
 *   core will only work on a 2's complement machine.  I'm not about to add
 *   2's complement emulation =).  If you don't know what 2's complement is,
 *   you most likely have it.
 *
 * Notes:
 *
 * You must at least implement the m68k_read_memory_xx() and
 * m68k_write_memory_xx() functions in order to use the emulation core.
 * Unless you plan to implement more direct access to memory for immediate
 * reads and instruction fetches, you can just #define the
 * m68k_read_immediate_xx() and m68k_read_instruction() functions to
 * the m68k_read_memory_xx() functions.
 *
 * In order to use the emulation, you will need to call m68k_input_reset()
 * and m68k_execute().  All the other functions are just to let you poke
 * about with the internals of the CPU.
 */


/* ======================================================================== */
/* ============================ GENERAL DEFINES =========================== */
/* ======================================================================== */

/* There are 7 levels of interrupt to the 68000.  Level 7 cannot me masked */
#define M68K_IRQ_1    1
#define M68K_IRQ_2    2
#define M68K_IRQ_3    3
#define M68K_IRQ_4    4
#define M68K_IRQ_5    5
#define M68K_IRQ_6    6
#define M68K_IRQ_7    7


/* Special interrupt acknowledge values.
 * Use these as special returns from the interrupt acknowledge callback
 * (specified later in this header).
 */

/* Causes an interrupt autovector (0x18 + interrupt level) to be taken.
 * This happens in a real 68000 if VPA or AVEC is asserted during an interrupt
 * acknowledge cycle instead of DTACK.
 */
#define M68K_INT_ACK_AUTOVECTOR    -1

/* Causes the spurious interrupt vector (0x18) to be taken
 * This happens in a real 68000 if BERR is asserted during the interrupt
 * acknowledge cycle (i.e. no devices responded to the acknowledge).
 */
#define M68K_INT_ACK_SPURIOUS      -2


/* CPU types for use in m68k_set_cpu_type() */
#define M68K_CPU_MODE_68000 1
#define M68K_CPU_MODE_68010 2
#define M68K_CPU_MODE_68EC020 4
#define M68K_CPU_MODE_68020 8


/* ======================================================================== */
/* ============================== STRUCTURES ============================== */
/* ======================================================================== */

typedef struct                 /* CPU Context */
{
   unsigned int  mode;         /* CPU Operation Mode (68000, 68010, or 68020) */
   unsigned int  sr;           /* Status Register */
   unsigned int  ppc;		   /* Previous program counter */
   unsigned int  pc;           /* Program Counter */
   unsigned int  d[8];         /* Data Registers */
   unsigned int  a[8];         /* Address Registers */
   unsigned int  usp;          /* User Stack Pointer */
   unsigned int  isp;          /* Interrupt Stack Pointer */
   unsigned int  msp;          /* Master Stack Pointer */
   unsigned int  vbr;          /* Vector Base Register.  Used in 68010+ */
   unsigned int  sfc;          /* Source Function Code.  Used in 68010+ */
   unsigned int  dfc;          /* Destination Function Code.  Used in 68010+ */
   unsigned int  stopped;      /* Stopped state: only interrupt can restart */
   unsigned int  halted;       /* Halted state: only reset can restart */
   unsigned int  int_state;	   /* Current interrupt line states -- ASG: changed from ints_pending */
   unsigned int  int_cycles;   /* Extra cycles taken due to interrupts -- ASG: added */
   unsigned int  pref_addr;    /* Last prefetch address */
   unsigned int  pref_data;    /* Data in the prefetch queue */
   int  (*int_ack_callback)(int int_level); /* Interrupt Acknowledge */
   void (*bkpt_ack_callback)(int data);     /* Breakpoint Acknowledge */
   void (*reset_instr_callback)(void);      /* Called when a RESET instruction is encountered */
   void (*pc_changed_callback)(int new_pc); /* Called when the PC changes by a large amount */
   void (*set_fc_callback)(int new_fc);     /* Called when the CPU function code changes */
   void (*instr_hook_callback)(void);       /* Called every instruction cycle prior to execution */
} m68k_cpu_context;

/* ======================================================================== */
/* ====================== FUNCTIONS CALLED BY THE CPU ===================== */
/* ======================================================================== */

/* You will have to implement these functions */

/* read/write functions called by the CPU to access memory.
 * while values used are 32 bits, only the appropriate number
 * of bits are relevant (i.e. in write_memory_8, only the lower 8 bits
 * of value should be written to memory).
 * address will be a 24-bit value.
 */

/* Read from anywhere */
int  m68k_read_memory_8(int address);
int  m68k_read_memory_16(int address);
int  m68k_read_memory_32(int address);

/* Read data immediately following the PC */
int  m68k_read_immediate_8(int address);
int  m68k_read_immediate_16(int address);
int  m68k_read_immediate_32(int address);

/* Read an instruction (16-bit word immeditately after PC) */
int  m68k_read_instruction(int address);

/* Write to anywhere */
void m68k_write_memory_8(int address, int value);
void m68k_write_memory_16(int address, int value);
void m68k_write_memory_32(int address, int value);



/* ======================================================================== */
/* ============================== CALLBACKS =============================== */
/* ======================================================================== */

/* These functions allow you to set callbacks to the host when specific events
 * occur.  Note that you must enable the corresponding value in m68kconf.h
 * in order for these to do anything useful.
 * Note: I have defined default callbacks which are used if you have enabled
 * the corresponding #define in m68kconf.h but either haven't assigned a
 * callback or have assigned a callback of NULL.
 */

/* Set the callback for an interrupt acknowledge.
 * You must enable M68K_INT_ACK in m68kconf.h.
 * The CPU will call the callback with the interrupt level being acknowledged.
 * The host program must return either a vector from 0x02-0xff, or one of the
 * special interrupt acknowledge values specified earlier in this header.
 * If this is not implemented, the CPU will always assume an autovectored
 * interrupt.
 * Default behavior: return M68K_INT_ACK_AUTOVECTOR.
 */
void m68k_set_int_ack_callback(int  (*callback)(int int_level));


/* Set the callback for a breakpoint acknowledge (68010+).
 * You must enable M68K_BKPT_ACK in m68kconf.h.
 * The CPU will call the callback with whatever was in the data field of the
 * BKPT instruction for 68020+, or 0 for 68010.
 * Default behavior: do nothing.
 */
void m68k_set_bkpt_ack_callback(void (*callback)(int data));


/* Set the callback for the RESET instruction.
 * You must enable M68K_OUTPUT_RESET in m68kconf.h.
 * The CPU calls this callback every time it encounters a RESET instruction.
 * Default behavior: do nothing.
 */
void m68k_set_reset_instr_callback(void  (*callback)(void));


/* Set the callback for informing of a large PC change.
 * You must enable M68K_PC_CHANGED in m68kconf.h.
 * The CPU calls this callback with the new PC value every time the PC changes
 * by a large value (currently set for changes by longwords).
 * Default behavior: do nothing.
 */
void m68k_set_pc_changed_callback(void  (*callback)(int new_pc));


/* Set the callback for CPU function code changes.
 * You must enable M68K_SET_FC in m68kconf.h.
 * The CPU calls this callback with the function code before every memory
 * access to set the CPU's function code according to what kind of memory
 * access it is (supervisor/user, program/data and such).
 * Default behavior: do nothing.
 */
void m68k_set_fc_callback(void  (*callback)(int new_fc));


/* Set a callback for the instruction cycle of the CPU.
 * You must enable M68K_INSTR_HOOK in m68kconf.h.
 * The CPU calls this callback just before fetching the opcode in the
 * instruction cycle.
 * Default behavior: do nothing.
 */
void m68k_set_instr_hook_callback(void  (*callback)(void));



/* ======================================================================== */
/* ====================== FUNCTIONS TO ACCESS THE CPU ===================== */
/* ======================================================================== */

/* Use this function to set the CPU type ypu want to emulate.
 * Currently supported types are: M68K_CPU_MODE_68000, M68K_CPU_MODE_68010,
 * and M68K_CPU_MODE_68020.
 */
void m68k_set_cpu_mode(int cpu_mode);

/* Reset the CPU as if you asserted RESET */
/* You *MUST* call this at least once to initialize the emulation */
void m68k_pulse_reset(void *param);

/* execute num_clks worth of instructions.  returns number of clks used */
int m68k_execute(int num_clks);

/* The following 2 functions simulate an interrupt controller attached to the
 * CPU since the 68000 needs an interrupt controller attached to work
 * properly.  Valid interrupt lines are 1, 2, 3, 4, 5, 6, and 7 (7 is a non-
 * maskable interrupt)
 */
void m68k_assert_irq(int int_line);
void m68k_clear_irq(int int_line);

/* Halt the CPU as if you asserted the HALT pin */
void m68k_pulse_halt(void);

/* look at the internals of the CPU */
int m68k_peek_dr(int reg_num);
int m68k_peek_ar(int reg_num);
unsigned int m68k_peek_pc(void);
unsigned int m68k_peek_ppc(void);
int m68k_peek_sr(void);
int m68k_peek_ir(void);
int m68k_peek_t1_flag(void);
int m68k_peek_t0_flag(void);
int m68k_peek_s_flag(void);
int m68k_peek_m_flag(void);
int m68k_peek_int_mask(void);
int m68k_peek_x_flag(void);
int m68k_peek_n_flag(void);
int m68k_peek_z_flag(void);
int m68k_peek_v_flag(void);
int m68k_peek_c_flag(void);
int m68k_peek_usp(void);
int m68k_peek_isp(void);
int m68k_peek_msp(void);

/* poke values into the internals of the CPU */
void m68k_poke_dr(int reg_num, int value);
void m68k_poke_ar(int reg_num, int value);
void m68k_poke_pc(unsigned int value);
void m68k_poke_sr(int value);
void m68k_poke_ir(int value);
void m68k_poke_t1_flag(int value);
void m68k_poke_t0_flag(int value);
void m68k_poke_s_flag(int value);
void m68k_poke_m_flag(int value);
void m68k_poke_int_mask(int value);
void m68k_poke_x_flag(int value);
void m68k_poke_n_flag(int value);
void m68k_poke_z_flag(int value);
void m68k_poke_v_flag(int value);
void m68k_poke_c_flag(int value);
void m68k_poke_usp(int value);
void m68k_poke_isp(int value);
void m68k_poke_msp(int value);

/* context switching to allow multiple CPUs */
unsigned m68k_get_context(void* dst);
void m68k_set_context(void* dst);

/* check if an instruction is valid for the specified CPU mode */
int m68k_is_valid_instruction(int instruction, int cpu_mode);



/* ======================================================================== */
/* ============================= CONFIGURATION ============================ */
/* ======================================================================== */

/* Import the configuration for this build */
#include "m68kconf.h"



/* ======================================================================== */
/* ============================== END OF FILE ============================= */
/* ======================================================================== */

#endif /* M68K__HEADER */
