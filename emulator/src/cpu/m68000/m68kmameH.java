package cpu.m68000;

import static mame.cpuintrfH.*;
import static mame.cpuintrfH.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.memory.*;
import static cpu.m68000.m68000H.*;
import static cpu.m68000.m68kH.*;
import static cpu.m68000.m68kopsH.*;
import static cpu.m68000.m68kops.*;
import static cpu.m68000.m68kcpu.*;

public class m68kmameH {
    public static int M68K_INT_ACK = 1;
    public static int M68K_BKPT_ACK = 0;
    public static int M68K_TRACE = 0;
    public static int M68K_HALT = 0;
    public static int M68K_OUTPUT_RESET = 0;
    public static int M68K_PC_CHANGED = 0;
    public static int M68K_SET_FC = 0;
    public static int M68K_INSTR_HOOK = 0;
    public static int M68K_USE_PREFETCH = 1;
    public static int M68K_DEFAULT_CPU_MODE = M68K_CPU_MODE_68000;
}