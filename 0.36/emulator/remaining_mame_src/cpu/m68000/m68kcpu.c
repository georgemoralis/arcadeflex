/* ======================================================================== */
/* ========================= LICENSING & COPYRIGHT ======================== */
/* ======================================================================== */

#if 0
static const char* copyright_notice =
"MUSASHI\n"
"Version 2.1 (1999-07-20)\n"
"A portable Motorola M680x0 processor emulation engine.\n"
"Copyright 1999 Karl Stenerud.  All rights reserved.\n"
"\n"
"This code may be freely used for non-commercial purpooses as long as this\n"
"copyright notice remains unaltered in the source code and any binary files\n"
"containing this code in compiled form.\n"
"\n"
"Any commercial ventures wishing to use this code must contact the author\n"
"(Karl Stenerud) to negotiate commercial licensing terms.\n"
"\n"
"The latest version of this code can be obtained at:\n"
"(homepage pending)\n"
;
#endif


/* ======================================================================== */
/* ================================= NOTES ================================ */
/* ======================================================================== */



/* ======================================================================== */
/* ================================ INCLUDES ============================== */
/* ======================================================================== */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "m68kcpu.h"
#include "m68kops.h"

/* ======================================================================== */
/* =============================== PROTOTYPES ============================= */
/* ======================================================================== */

/* Build the opcode handler table */
void m68ki_build_opcode_table(void);



/* ======================================================================== */
/* ================================= DATA ================================= */
/* ======================================================================== */

static uint  m68k_emulation_initialized = 0;                /* flag if emulation has been initialized */
void         (*m68k_instruction_jump_table[0x10000])(void); /* opcode handler jump table */
int          m68k_clks_left = 0;                            /* Number of clocks remaining */
uint         m68k_tracing = 0;

#ifdef M68K_LOG
uint  m68k_pc_offset = 2;
char* m68k_cpu_names[9] =
{
   "Invalid CPU",
   "M68000",
   "M68010",
   "Invalid CPU",
   "M68EC020"
   "Invalid CPU",
   "Invalid CPU",
   "Invalid CPU",
   "M68020"
};
#endif /* M68K_LOG */

/* Mask which bits of the SR ar eimplemented */
uint m68k_sr_implemented_bits[9] =
{
   0x0000, /* invalid */
   0xa71f, /* 68000:   T1 -- S  -- -- I2 I1 I0 -- -- -- X  N  Z  V  C  */
   0xa71f, /* 68010:   T1 -- S  -- -- I2 I1 I0 -- -- -- X  N  Z  V  C  */
   0x0000, /* invalid */
   0xf71f, /* 68EC020: T1 T0 S  M  -- I2 I1 I0 -- -- -- X  N  Z  V  C  */
   0x0000, /* invalid */
   0x0000, /* invalid */
   0x0000, /* invalid */
   0xf71f, /* 68020:   T1 T0 S  M  -- I2 I1 I0 -- -- -- X  N  Z  V  C  */
};

/* The CPU core */
m68k_cpu_core m68k_cpu = {0};

/* Pointers to speed up address register indirect with index calculation */
uint* m68k_cpu_dar[2] = {CPU_D, CPU_A};

/* Pointers to speed up movem instructions */
uint* m68k_movem_pi_table[16] =
{
   CPU_D, CPU_D+1, CPU_D+2, CPU_D+3, CPU_D+4, CPU_D+5, CPU_D+6, CPU_D+7,
   CPU_A, CPU_A+1, CPU_A+2, CPU_A+3, CPU_A+4, CPU_A+5, CPU_A+6, CPU_A+7
};
uint* m68k_movem_pd_table[16] =
{
   CPU_A+7, CPU_A+6, CPU_A+5, CPU_A+4, CPU_A+3, CPU_A+2, CPU_A+1, CPU_A,
   CPU_D+7, CPU_D+6, CPU_D+5, CPU_D+4, CPU_D+3, CPU_D+2, CPU_D+1, CPU_D,
};


/* Used when checking for pending interrupts */
uint8 m68k_int_masks[] = {0xfe, 0xfc, 0xf8, 0xf0, 0xe0, 0xc0, 0x80, 0x80};

/* Used by shift & rotate instructions */
uint8 m68k_shift_8_table[65] =
{
    0x00, 0x80, 0xc0, 0xe0, 0xf0, 0xf8, 0xfc, 0xfe, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff
};
uint16 m68k_shift_16_table[65] =
{
    0x0000, 0x8000, 0xc000, 0xe000, 0xf000, 0xf800, 0xfc00, 0xfe00, 0xff00, 0xff80, 0xffc0, 0xffe0,
    0xfff0, 0xfff8, 0xfffc, 0xfffe, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff,
    0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff,
    0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff,
    0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff,
    0xffff, 0xffff, 0xffff, 0xffff, 0xffff
};
uint m68k_shift_32_table[65] =
{
    0x00000000, 0x80000000, 0xc0000000, 0xe0000000, 0xf0000000, 0xf8000000, 0xfc000000, 0xfe000000,
    0xff000000, 0xff800000, 0xffc00000, 0xffe00000, 0xfff00000, 0xfff80000, 0xfffc0000, 0xfffe0000,
    0xffff0000, 0xffff8000, 0xffffc000, 0xffffe000, 0xfffff000, 0xfffff800, 0xfffffc00, 0xfffffe00,
    0xffffff00, 0xffffff80, 0xffffffc0, 0xffffffe0, 0xfffffff0, 0xfffffff8, 0xfffffffc, 0xfffffffe,
    0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
    0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
    0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
    0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff
};


/* Number of clock cycles to use for exception processing.
 * I used 4 for any vectors that are undocumented for processing times.
 */
uint8 m68k_exception_cycle_table[256] =
{
   40, /*  0: Reset - should never be called                                 */
   40, /*  1: Reset - should never be called                                 */
   50, /*  2: Bus Error                                (unused in emulation) */
   50, /*  3: Address Error                            (unused in emulation) */
   34, /*  4: Illegal Instruction                                            */
   38, /*  5: Divide by Zero -- ASG: changed from 42                         */
   40, /*  6: CHK -- ASG: chanaged from 44                                   */
   34, /*  7: TRAPV                                                          */
   34, /*  8: Privilege Violation                                            */
   34, /*  9: Trace                                                          */
    4, /* 10: 1010                                                           */
    4, /* 11: 1111                                                           */
    4, /* 12: RESERVED                                                       */
    4, /* 13: Coprocessor Protocol Violation           (unused in emulation) */
    4, /* 14: Format Error                             (unused in emulation) */
   44, /* 15: Uninitialized Interrupt                                        */
    4, /* 16: RESERVED                                                       */
    4, /* 17: RESERVED                                                       */
    4, /* 18: RESERVED                                                       */
    4, /* 19: RESERVED                                                       */
    4, /* 20: RESERVED                                                       */
    4, /* 21: RESERVED                                                       */
    4, /* 22: RESERVED                                                       */
    4, /* 23: RESERVED                                                       */
   44, /* 24: Spurious Interrupt                                             */
   44, /* 25: Level 1 Interrupt Autovector                                   */
   44, /* 26: Level 2 Interrupt Autovector                                   */
   44, /* 27: Level 3 Interrupt Autovector                                   */
   44, /* 28: Level 4 Interrupt Autovector                                   */
   44, /* 29: Level 5 Interrupt Autovector                                   */
   44, /* 30: Level 6 Interrupt Autovector                                   */
   44, /* 31: Level 7 Interrupt Autovector                                   */
   34, /* 32: TRAP #0 -- ASG: chanaged from 38                               */
   34, /* 33: TRAP #1                                                        */
   34, /* 34: TRAP #2                                                        */
   34, /* 35: TRAP #3                                                        */
   34, /* 36: TRAP #4                                                        */
   34, /* 37: TRAP #5                                                        */
   34, /* 38: TRAP #6                                                        */
   34, /* 39: TRAP #7                                                        */
   34, /* 40: TRAP #8                                                        */
   34, /* 41: TRAP #9                                                        */
   34, /* 42: TRAP #10                                                       */
   34, /* 43: TRAP #11                                                       */
   34, /* 44: TRAP #12                                                       */
   34, /* 45: TRAP #13                                                       */
   34, /* 46: TRAP #14                                                       */
   34, /* 47: TRAP #15                                                       */
    4, /* 48: FP Branch or Set on Unknown Condition    (unused in emulation) */
    4, /* 49: FP Inexact Result                        (unused in emulation) */
    4, /* 50: FP Divide by Zero                        (unused in emulation) */
    4, /* 51: FP Underflow                             (unused in emulation) */
    4, /* 52: FP Operand Error                         (unused in emulation) */
    4, /* 53: FP Overflow                              (unused in emulation) */
    4, /* 54: FP Signaling NAN                         (unused in emulation) */
    4, /* 55: FP Unimplemented Data Type               (unused in emulation) */
    4, /* 56: MMU Configuration Error                  (unused in emulation) */
    4, /* 57: MMU Illegal Operation Error              (unused in emulation) */
    4, /* 58: MMU Access Level Violation Error         (unused in emulation) */
    4, /* 59: RESERVED                                                       */
    4, /* 60: RESERVED                                                       */
    4, /* 61: RESERVED                                                       */
    4, /* 62: RESERVED                                                       */
    4, /* 63: RESERVED                                                       */
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /*  64- 79: User Defined */
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /*  80- 95: User Defined */
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /*  96-111: User Defined */
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /* 112-127: User Defined */
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /* 128-143: User Defined */
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /* 144-159: User Defined */
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /* 160-175: User Defined */
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /* 176-191: User Defined */
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /* 192-207: User Defined */
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /* 208-223: User Defined */
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /* 224-239: User Defined */
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /* 240-255: User Defined */
};



/* ======================================================================== */
/* =============================== CALLBACKS ============================== */
/* ======================================================================== */

/* Default callbacks used if the callback hasn't been set yet, or if the
 * callback is set to NULL
 */

/* Interrupt acknowledge */
static int default_int_ack_callback_data;
static int default_int_ack_callback(int int_level)
{
   default_int_ack_callback_data = int_level;
   return M68K_INT_ACK_AUTOVECTOR;
}

/* Breakpoint acknowledge */
static int default_bkpt_ack_callback_data;
static void default_bkpt_ack_callback(int data)
{
   default_bkpt_ack_callback_data = data;
}

/* Called when a reset instruction is executed */
static void default_reset_instr_callback(void)
{
}

/* Called when the program counter changed by a large value */
static int default_pc_changed_callback_data;
static void default_pc_changed_callback(int new_pc)
{
   default_pc_changed_callback_data = new_pc;
}

/* Called every time there's bus activity (read/write to/from memory */
static int default_set_fc_callback_data;
static void default_set_fc_callback(int new_fc)
{
   default_set_fc_callback_data = new_fc;
}

/* Called every instruction cycle prior to execution */
static void default_instr_hook_callback(void)
{
}



/* ======================================================================== */
/* ================================= API ================================== */
/* ======================================================================== */

/* Peek at the internals of the M68K */
int m68k_peek_dr(int reg_num)   { return (reg_num < 8) ? CPU_D[reg_num] : 0; }
int m68k_peek_ar(int reg_num)   { return (reg_num < 8) ? CPU_A[reg_num] : 0; }
unsigned int m68k_peek_pc(void) { return ADDRESS_68K(CPU_PC); }
unsigned int m68k_peek_ppc(void) { return ADDRESS_68K(CPU_PPC); }
int m68k_peek_sr(void)          { return m68ki_get_sr(); }
int m68k_peek_ir(void)          { return CPU_IR; }
int m68k_peek_t1_flag(void)     { return CPU_T1 != 0; }
int m68k_peek_t0_flag(void)     { return CPU_T0 != 0; }
int m68k_peek_s_flag(void)      { return CPU_S != 0; }
int m68k_peek_m_flag(void)      { return CPU_M != 0; }
int m68k_peek_int_mask(void)    { return CPU_INT_MASK; }
int m68k_peek_x_flag(void)      { return CPU_X != 0; }
int m68k_peek_n_flag(void)      { return CPU_N != 0; }
int m68k_peek_z_flag(void)      { return CPU_NOT_Z == 0; }
int m68k_peek_v_flag(void)      { return CPU_V != 0; }
int m68k_peek_c_flag(void)      { return CPU_C != 0; }
int m68k_peek_usp(void)         { return CPU_S ? CPU_USP : CPU_A[7]; }
int m68k_peek_isp(void)         { return CPU_S && !CPU_M ? CPU_A[7] : CPU_ISP; }
int m68k_peek_msp(void)         { return CPU_S && CPU_M ? CPU_A[7] : CPU_MSP; }

/* Poke data into the M68K */
void m68k_poke_dr(int reg_num, int value) { if(reg_num < 8) CPU_D[reg_num] = MASK_OUT_ABOVE_32(value); }
void m68k_poke_ar(int reg_num, int value) { if(reg_num < 8) CPU_A[reg_num] = MASK_OUT_ABOVE_32(value); }
void m68k_poke_pc(unsigned int value)     { m68ki_set_pc(ADDRESS_68K(value)); }
void m68k_poke_sr(int value)              { m68ki_set_sr(MASK_OUT_ABOVE_16(value)); }
void m68k_poke_ir(int value)              { CPU_IR = MASK_OUT_ABOVE_16(value); }
void m68k_poke_t1_flag(int value)         { CPU_T1 = (value != 0); }
void m68k_poke_t0_flag(int value)         { if(CPU_MODE & CPU_MODE_EC020_PLUS) CPU_T0 = (value != 0); }
void m68k_poke_s_flag(int value)          { m68ki_set_s_flag(value); }
void m68k_poke_m_flag(int value)          { if(CPU_MODE & CPU_MODE_EC020_PLUS) m68ki_set_m_flag(value); }
void m68k_poke_int_mask(int value)        { CPU_INT_MASK = value & 7; }
void m68k_poke_x_flag(int value)          { CPU_X = (value != 0); }
void m68k_poke_n_flag(int value)          { CPU_N = (value != 0); }
void m68k_poke_z_flag(int value)          { CPU_NOT_Z = (value == 0); }
void m68k_poke_v_flag(int value)          { CPU_V = (value != 0); }
void m68k_poke_c_flag(int value)          { CPU_C = (value != 0); }
void m68k_poke_usp(int value)
{
   if(CPU_S)
      CPU_USP = MASK_OUT_ABOVE_32(value);
   else
      CPU_A[7] = MASK_OUT_ABOVE_32(value);
}
void m68k_poke_isp(int value)
{
   if(CPU_S && !CPU_M)
      CPU_A[7] = MASK_OUT_ABOVE_32(value);
   else
      CPU_ISP = MASK_OUT_ABOVE_32(value);
}
void m68k_poke_msp(int value)
{
   if(CPU_MODE & CPU_MODE_EC020_PLUS)
   {
      if(CPU_S && CPU_M)
         CPU_A[7] = MASK_OUT_ABOVE_32(value);
      else
         CPU_MSP = MASK_OUT_ABOVE_32(value);
   }
}

/* Set the callbacks */
void m68k_set_int_ack_callback(int  (*callback)(int int_level))
{
   CPU_INT_ACK_CALLBACK = callback ? callback : default_int_ack_callback;
}

void m68k_set_bkpt_ack_callback(void  (*callback)(int data))
{
   CPU_BKPT_ACK_CALLBACK = callback ? callback : default_bkpt_ack_callback;
}

void m68k_set_reset_instr_callback(void  (*callback)(void))
{
   CPU_RESET_INSTR_CALLBACK = callback ? callback : default_reset_instr_callback;
}

void m68k_set_pc_changed_callback(void  (*callback)(int new_pc))
{
   CPU_PC_CHANGED_CALLBACK = callback ? callback : default_pc_changed_callback;
}

void m68k_set_fc_callback(void  (*callback)(int new_fc))
{
   CPU_SET_FC_CALLBACK = callback ? callback : default_set_fc_callback;
}

void m68k_set_instr_hook_callback(void  (*callback)(void))
{
   CPU_INSTR_HOOK_CALLBACK = callback ? callback : default_instr_hook_callback;
}


void m68k_set_cpu_mode(int cpu_mode)
{
   switch(cpu_mode)
   {
      case M68K_CPU_MODE_68000:
      case M68K_CPU_MODE_68010:
      case M68K_CPU_MODE_68EC020:
      case M68K_CPU_MODE_68020:
         CPU_MODE = cpu_mode;
         return;
      default:
         CPU_MODE = M68K_DEFAULT_CPU_MODE;
   }
}


/* Execute some instructions until we use up num_clks clock cycles */
/* ASG: removed per-instruction interrupt checks */
int m68k_execute(int num_clks)
{
#if M68K_HALT
   if(!CPU_HALTED)
   {
#endif /* M68K_HALT */
   /* Make sure we're not stopped */
   if(!CPU_STOPPED)
   {
      /* Set our pool of clock cycles available */
      m68k_clks_left = num_clks;

      /* ASG: update cycles */
      m68k_clks_left -= CPU_INT_CYCLES;
      CPU_INT_CYCLES = 0;

      /* Main loop.  Keep going until we run out of clock cycles */
      do
      {
         /* Set tracing accodring to T1. (T0 is done inside instruction) */
         m68ki_set_trace(); /* auto-disable (see m68kcpu.h) */

         /* Call external hook to peek at CPU */
         m68ki_instr_hook(); /* auto-disable (see m68kcpu.h) */

         /* MAME */
         CPU_PPC = CPU_PC;
         CALL_MAME_DEBUG;
	#if A68000_COREDEBUG
		Asgard68000MiniTrace(&CPU_D[0], &CPU_A[0], m68k_peek_pc(), m68k_peek_sr(), m68k_clks_left);
	#endif
         /* MAME */

         /* Read an instruction and call its handler */
         CPU_IR = m68ki_read_instruction();
         m68k_instruction_jump_table[CPU_IR]();

         /* Trace m68k_exception, if necessary */
         m68ki_exception_if_trace(); /* auto-disable (see m68kcpu.h) */
         continue;
      } while(m68k_clks_left > 0);

	  /* set previous PC to current PC for the next entry into the loop */
      CPU_PPC = CPU_PC;

      /* ASG: update cycles */
      m68k_clks_left -= CPU_INT_CYCLES;
      CPU_INT_CYCLES = 0;

	#if A68000_COREDEBUG
		Asgard68000MiniTrace(&CPU_D[0], &CPU_A[0], m68k_peek_pc(), m68k_peek_sr(), m68k_clks_left);
	#endif

      /* return how many clocks we used */
      return num_clks - m68k_clks_left;
   }
#if M68K_HALT
   }
#endif /* M68K_HALT */

   /* We get here if the CPU is stopped */
   m68k_clks_left = 0;

	#if A68000_COREDEBUG
		Asgard68000MiniTrace(&CPU_D[0], &CPU_A[0], m68k_peek_pc(), m68k_peek_sr(), m68k_clks_left);
	#endif

   return num_clks;
}


/* ASG: rewrote so that the int_line is a mask of the IPL0/IPL1/IPL2 bits */
void m68k_assert_irq(int int_line)
{
   /* OR in the bits of the interrupt */
   int old_state = CPU_INT_STATE;
   CPU_INT_STATE = 0;	/* ASG: remove me to do proper mask setting */
   CPU_INT_STATE |= int_line & 7;

   /* if it's NMI, we're edge triggered */
   if (CPU_INT_STATE == 7)
   {
      if (old_state != 7)
         m68ki_service_interrupt(1 << 7);
   }

   /* other interrupts just reflect the current state */
   else
      m68ki_check_interrupts();
}

/* ASG: rewrote so that the int_line is a mask of the IPL0/IPL1/IPL2 bits */
void m68k_clear_irq(int int_line)
{
   /* AND in the bits of the interrupt */
   CPU_INT_STATE &= ~int_line & 7;
   CPU_INT_STATE = 0;	/* ASG: remove me to do proper mask setting */

   /* check for interrupts again */
   m68ki_check_interrupts();
}


/* Reset the M68K */
void m68k_pulse_reset(void *param)
{
   CPU_HALTED = 0;
   CPU_STOPPED = 0;
   CPU_INT_STATE = 0;	/* ASG: changed from CPU_INTS_PENDING */
   CPU_T1 = CPU_T0 = 0;
   m68ki_clear_trace();
   CPU_S = 1;
   CPU_M = 0;
   CPU_INT_MASK = 7;
   CPU_VBR = 0;
   CPU_A[7] = m68ki_read_32(0);
   m68ki_set_pc(m68ki_read_32(4));
#if M68K_USE_PREFETCH
   CPU_PREF_ADDR = MASK_OUT_BELOW_2(CPU_PC);
   CPU_PREF_DATA = m68k_read_immediate_32(ADDRESS_68K(CPU_PREF_ADDR));
#endif /* M68K_USE_PREFETCH */
   m68k_clks_left = 0;

   if (CPU_MODE == 0) CPU_MODE = M68K_DEFAULT_CPU_MODE;	/* KW 990319 */
   /* The first call to this function initializes the opcode handler jump table */
   if(m68k_emulation_initialized)
      return;
   else
   {
      m68ki_build_opcode_table();
      m68k_set_int_ack_callback(NULL);
      m68k_set_bkpt_ack_callback(NULL);
      m68k_set_reset_instr_callback(NULL);
      m68k_set_pc_changed_callback(NULL);
      m68k_set_fc_callback(NULL);
      m68k_set_instr_hook_callback(NULL);

      m68k_emulation_initialized = 1;
   }
}


/* Halt the CPU */
void m68k_pulse_halt(void)
{
   CPU_HALTED = 1;
}


/* Get and set the current CPU context */
/* This is to allow for multiple CPUs */
unsigned m68k_get_context(void* dst)
{
	if( dst )
	{
		m68k_cpu_context *cpu = dst;

        cpu->mode                 = CPU_MODE;
		cpu->sr 				  = m68ki_get_sr();
		cpu->pc 				  = CPU_PC;
		memcpy(cpu->d, CPU_D, sizeof(CPU_D));
		memcpy(cpu->a, CPU_A, sizeof(CPU_A));
		cpu->usp				  = CPU_USP;
		cpu->isp				  = CPU_ISP;
		cpu->msp				  = CPU_MSP;
		cpu->vbr				  = CPU_VBR;
		cpu->sfc				  = CPU_SFC;
		cpu->dfc				  = CPU_DFC;
		cpu->stopped			  = CPU_STOPPED;
		cpu->halted 			  = CPU_HALTED;
		cpu->int_state			  = CPU_INT_STATE;	/* ASG: changed from CPU_INTS_PENDING */
		cpu->int_cycles           = CPU_INT_CYCLES;	/* ASG */
		cpu->int_ack_callback	  = CPU_INT_ACK_CALLBACK;
		cpu->bkpt_ack_callback	  = CPU_BKPT_ACK_CALLBACK;
		cpu->reset_instr_callback = CPU_RESET_INSTR_CALLBACK;
		cpu->pc_changed_callback  = CPU_PC_CHANGED_CALLBACK;
		cpu->set_fc_callback	  = CPU_SET_FC_CALLBACK;
		cpu->instr_hook_callback  = CPU_INSTR_HOOK_CALLBACK;
		cpu->pref_addr			  = CPU_PREF_ADDR;
		cpu->pref_data			  = CPU_PREF_DATA;
	}
	return sizeof(m68k_cpu_context);
}

void m68k_set_context(void* src)
{
	if( src )
	{
		m68k_cpu_context *cpu = src;

        CPU_MODE                 = cpu->mode;
		m68ki_set_sr_no_int(cpu->sr); /* This stays on top to prevent side-effects */
		m68ki_set_pc(cpu->pc);
		memcpy(CPU_D, cpu->d, sizeof(CPU_D));
		memcpy(CPU_A, cpu->a, sizeof(CPU_D));
		CPU_USP 				 = cpu->usp;
		CPU_ISP 				 = cpu->isp;
		CPU_MSP 				 = cpu->msp;
		CPU_VBR 				 = cpu->vbr;
		CPU_SFC 				 = cpu->sfc;
		CPU_DFC 				 = cpu->dfc;
		CPU_STOPPED 			 = cpu->stopped;
		CPU_HALTED				 = cpu->halted;
		CPU_INT_STATE			 = cpu->int_state;	/* ASG: changed from CPU_INTS_PENDING */
		CPU_INT_CYCLES           = cpu->int_cycles;	/* ASG */
		CPU_INT_ACK_CALLBACK	 = cpu->int_ack_callback;
		CPU_BKPT_ACK_CALLBACK	 = cpu->bkpt_ack_callback;
		CPU_RESET_INSTR_CALLBACK = cpu->reset_instr_callback;
		CPU_PC_CHANGED_CALLBACK  = cpu->pc_changed_callback;
		CPU_SET_FC_CALLBACK 	 = cpu->set_fc_callback;
		CPU_INSTR_HOOK_CALLBACK  = cpu->instr_hook_callback;
		CPU_PREF_ADDR 			 = cpu->pref_addr;
		CPU_PREF_DATA 			 = cpu->pref_data;

		/* ASG: check for interrupts */
		m68ki_check_interrupts();
	}
}


/* Check if the instruction is a valid one */
int m68k_is_valid_instruction(int instruction, int cpu_mode)
{
   if(m68k_instruction_jump_table[MASK_OUT_ABOVE_16(instruction)] == m68000_illegal)
      return 0;
   if(!(cpu_mode & CPU_MODE_010_PLUS))
   {
      if((instruction & 0xfff8) == 0x4848) /* bkpt */
         return 0;
      if((instruction & 0xffc0) == 0x42c0) /* move from ccr */
         return 0;
      if((instruction & 0xfffe) == 0x4e7a) /* movec */
         return 0;
      if((instruction & 0xff00) == 0x0e00) /* moves */
         return 0;
      if((instruction & 0xffff) == 0x4e74) /* rtd */
         return 0;
   }
   if(!(cpu_mode & CPU_MODE_EC020_PLUS))
   {
      if((instruction & 0xf0ff) == 0x60ff) /* bcc.l */
         return 0;
      if((instruction & 0xf8c0) == 0xe8c0) /* bfxxx */
         return 0;
      if((instruction & 0xffc0) == 0x06c0) /* callm */
         return 0;
      if((instruction & 0xf9c0) == 0x08c0) /* cas */
         return 0;
      if((instruction & 0xf9ff) == 0x08fc) /* cas2 */
         return 0;
      if((instruction & 0xf1c0) == 0x4100) /* chk.l */
         return 0;
      if((instruction & 0xf9c0) == 0x00c0) /* chk2, cmp2 */
         return 0;
      if((instruction & 0xff3f) == 0x0c3a) /* cmpi (pcdi) */
         return 0;
      if((instruction & 0xff3f) == 0x0c3b) /* cmpi (pcix) */
         return 0;
      if((instruction & 0xffc0) == 0x4c40) /* divl */
         return 0;
      if((instruction & 0xfff8) == 0x49c0) /* extb */
         return 0;
      if((instruction & 0xfff8) == 0x4808) /* link.l */
         return 0;
      if((instruction & 0xffc0) == 0x4c00) /* mull */
         return 0;
      if((instruction & 0xf1f0) == 0x8140) /* pack */
         return 0;
      if((instruction & 0xfff0) == 0x06c0) /* rtm */
         return 0;
      if((instruction & 0xf0f8) == 0x50f8) /* trapcc */
         return 0;
      if((instruction & 0xff38) == 0x4a08) /* tst (a) */
         return 0;
      if((instruction & 0xff3f) == 0x4a3a) /* tst (pcdi) */
         return 0;
      if((instruction & 0xff3f) == 0x4a3b) /* tst (pcix) */
         return 0;
      if((instruction & 0xff3f) == 0x4a3c) /* tst (imm) */
         return 0;
      if((instruction & 0xf1f0) == 0x8180) /* unpk */
         return 0;
   }
   return 1;
}



/* ======================================================================== */
/* ============================== END OF FILE ============================= */
/* ======================================================================== */
