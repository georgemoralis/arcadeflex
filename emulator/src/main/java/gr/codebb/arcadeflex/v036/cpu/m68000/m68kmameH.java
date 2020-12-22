package gr.codebb.arcadeflex.v036.cpu.m68000;

import static gr.codebb.arcadeflex.v036.mame.memory.*;
import static gr.codebb.arcadeflex.v036.cpu.m68000.m68kH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.cpu_readop16;

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

    public static int m68k_read_memory_8(int address) {
        return cpu_readmem24(address);
    }

    public static int m68k_read_memory_16(int address) {
        return cpu_readmem24_word(address);
    }

    public static int m68k_read_memory_32(int address) {
        return cpu_readmem24_dword(address);
    }

    public static int m68k_read_immediate_8(int address) {
        return (cpu_readop16((address) - 1) & 0xff);
    }

    public static int m68k_read_immediate_16(int address) {
        return cpu_readop16(address);
    }

    public static int m68k_read_immediate_32(int address) {
        return ((cpu_readop16(address) << 16) | cpu_readop16((address) + 2));
    }

    public static int m68k_read_instruction(int address) {
        return cpu_readop16(address);
    }

    public static void m68k_write_memory_8(int address, int value) {
        cpu_writemem24(address, value);
    }

    public static void m68k_write_memory_16(int address, int value) {
        cpu_writemem24_word(address, value);
    }

    public static void m68k_write_memory_32(int address, int value) {
        cpu_writemem24_dword(address, value);
    }
}
