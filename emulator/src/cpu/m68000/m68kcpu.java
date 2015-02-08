package cpu.m68000;

import static mame.cpuintrfH.*;
import static mame.cpuintrfH.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.memory.*;
import static cpu.m68000.m68000H.*;
import static cpu.m68000.m68kopsH.*;
import static cpu.m68000.m68kops.*;

public class m68kcpu {
    public static int m68k_emulation_initialized = 0;                /* flag if emulation has been initialized */
    public static opcode[] m68k_instruction_jump_table = new opcode[0x10000]; /* opcode handler jump table */

    public static int[] m68k_clks_left = new int[1];                            /* Number of clocks remaining */
    
    /*TODO*///uint         m68k_tracing = 0;   
    
    public static void m68k_pulse_reset(Object param) {
        /*TOD0*///   CPU_HALTED = 0;
/*TOD0*///   CPU_STOPPED = 0;
/*TOD0*///   CPU_INT_STATE = 0;	/* ASG: changed from CPU_INTS_PENDING */
/*TOD0*///   CPU_T1 = CPU_T0 = 0;
/*TOD0*///   m68ki_clear_trace();
/*TOD0*///   CPU_S = 1;
/*TOD0*///   CPU_M = 0;
/*TOD0*///   CPU_INT_MASK = 7;
/*TOD0*///   CPU_VBR = 0;
/*TOD0*///   CPU_A[7] = m68ki_read_32(0);
/*TOD0*///   m68ki_set_pc(m68ki_read_32(4));
/*TOD0*///#if M68K_USE_PREFETCH
/*TOD0*///   CPU_PREF_ADDR = MASK_OUT_BELOW_2(CPU_PC);
/*TOD0*///   CPU_PREF_DATA = m68k_read_immediate_32(ADDRESS_68K(CPU_PREF_ADDR));
/*TOD0*///#endif /* M68K_USE_PREFETCH */
/*TOD0*///   m68k_clks_left = 0;

        /*TOD0*///   if (CPU_MODE == 0) CPU_MODE = M68K_DEFAULT_CPU_MODE;	/* KW 990319 */
   /* The first call to this function initializes the opcode handler jump table */
        if (m68k_emulation_initialized != 0) {
            return;
        } else {
                m68ki_build_opcode_table();
 /*TOD0*///      m68k_set_int_ack_callback(NULL);
 /*TOD0*///      m68k_set_bkpt_ack_callback(NULL);
 /*TOD0*///      m68k_set_reset_instr_callback(NULL);
 /*TOD0*///      m68k_set_pc_changed_callback(NULL);
 /*TOD0*///      m68k_set_fc_callback(NULL);
 /*TOD0*///      m68k_set_instr_hook_callback(NULL);

            m68k_emulation_initialized = 1;
        }
    }
}
