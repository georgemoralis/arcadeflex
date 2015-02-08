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
}
