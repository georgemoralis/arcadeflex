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
	
/*TODO*///#define m68k_read_memory_8(address)          cpu_readmem24(address)
/*TODO*///#define m68k_read_memory_16(address)         cpu_readmem24_word(address)
/*TODO*///#define m68k_read_memory_32(address)         cpu_readmem24_dword(address)

/*TODO*///#define m68k_read_immediate_8(address)       (cpu_readop16((address)-1)&0xff)
/*TODO*///#define m68k_read_immediate_16(address)      cpu_readop16(address)
/*TODO*///#define m68k_read_immediate_32(address)      ((cpu_readop16(address)<<16) | cpu_readop16((address)+2))

/*TODO*///#define m68k_read_instruction(address)       cpu_readop16(address)

/*TODO*///#define m68k_write_memory_8(address, value)  cpu_writemem24(address, value)
/*TODO*///#define m68k_write_memory_16(address, value) cpu_writemem24_word(address, value)
/*TODO*///#define m68k_write_memory_32(address, value) cpu_writemem24_dword(address, value)
}