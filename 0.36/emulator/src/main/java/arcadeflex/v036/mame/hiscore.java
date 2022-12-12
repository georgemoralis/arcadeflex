/**
 * ported to v0.36
 *
 */
package arcadeflex.v036.mame;

import arcadeflex.v036.generic.hiscoreFileParser;
import static arcadeflex.v036.mame.driverH.CPU_FLAGS_MASK;
import static arcadeflex.v036.mame.osdependH.OSD_FILETYPE_HIGHSCORE;
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.mame.errorlog;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.platform.fileio.osd_fclose;
import static gr.codebb.arcadeflex.v036.platform.fileio.osd_fopen;
import static gr.codebb.arcadeflex.v036.platform.fileio.osd_fread;
import static gr.codebb.arcadeflex.v036.platform.fileio.osd_fwrite;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.cpu_getactivecpu;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.cpuintf;
import static gr.codebb.arcadeflex.v037b7.mame.memory.memorycontextswap;

public class hiscore {

    public static class mem_range {

        public int/*UINT32*/ cpu, addr, num_bytes, start_value, end_value;
        public mem_range next;
    }

    public static class _state {

        public int hiscores_have_been_loaded;
        public mem_range mem_range = null;
    }
    public static _state state = new _state();

    /**
     * **************************************************************************
     */
    public static void computer_writemem_byte(int cpu, int addr, int value) {
        int oldcpu = cpu_getactivecpu();
        memorycontextswap(cpu);
        //MEMORY_WRITE(cpu, addr, value);
        cpuintf[Machine.drv.cpu[cpu].cpu_type & ~CPU_FLAGS_MASK].memory_write(addr, value);
        if (oldcpu != cpu) {
            memorycontextswap(oldcpu);
        }
    }

    public static int computer_readmem_byte(int cpu, int addr) {
        int oldcpu = cpu_getactivecpu(), result;
        memorycontextswap(cpu);
        //result = MEMORY_READ(cpu, addr);
        result = cpuintf[Machine.drv.cpu[cpu].cpu_type & ~CPU_FLAGS_MASK].memory_read(addr);
        if (oldcpu != cpu) {
            memorycontextswap(oldcpu);
        }
        return result;
    }

    /**
     * **************************************************************************
     */
    static void copy_to_memory(int cpu, int addr, UBytePtr source, int num_bytes) {
        int i;
        for (i = 0; i < num_bytes; i++) {
            computer_writemem_byte(cpu, addr + i, source.read(i));
        }
    }

    static void copy_from_memory(int cpu, int addr, UBytePtr dest, int num_bytes) {
        int i;
        for (i = 0; i < num_bytes; i++) {
            dest.write(i, computer_readmem_byte(cpu, addr + i));
        }
    }

    /**
     * **************************************************************************
     */

    /* safe_to_load checks the start and end values of each memory range */
    public static int safe_to_load() {
        mem_range mem_range = state.mem_range;
        while (mem_range != null) {
            if (computer_readmem_byte(mem_range.cpu, mem_range.addr)
                    != mem_range.start_value) {
                return 0;
            }
            if (computer_readmem_byte(mem_range.cpu, (mem_range.addr + mem_range.num_bytes - 1))
                    != mem_range.end_value) {
                return 0;
            }
            mem_range = mem_range.next;
        }
        return 1;
    }

    /* hs_free disposes of the mem_range linked list */
    static void hs_free() {
        mem_range mem_range = state.mem_range;
        while (mem_range != null) {
            mem_range next = mem_range.next;
            mem_range = null;
            mem_range = next;
        }
        state.mem_range = null;
    }

    static void hs_load() {
        Object f = osd_fopen(Machine.gamedrv.name, null, OSD_FILETYPE_HIGHSCORE, 0);
        state.hiscores_have_been_loaded = 1;
        if (errorlog != null) {
            fprintf(errorlog, "hs_load\n");
        }
        if (f != null) {
            mem_range mem_range = state.mem_range;
            if (errorlog != null) {
                fprintf(errorlog, "loading...\n");
            }
            while (mem_range != null) {
                UBytePtr data = new UBytePtr(mem_range.num_bytes);
                if (data != null) {
                    /*	this buffer will almost certainly be small
					enough to be dynamically allocated, but let's
					avoid memory trashing just in case
                     */
                    osd_fread(f, data, mem_range.num_bytes);
                    copy_to_memory(mem_range.cpu, mem_range.addr, data, mem_range.num_bytes);
                    data = null;
                }
                mem_range = mem_range.next;
            }
            osd_fclose(f);
        }
    }

    static void hs_save() {
        Object f = osd_fopen(Machine.gamedrv.name, null, OSD_FILETYPE_HIGHSCORE, 1);
        if (errorlog != null) {
            fprintf(errorlog, "hs_save\n");
        }
        if (f != null) {
            mem_range mem_range = state.mem_range;
            if (errorlog != null) {
                fprintf(errorlog, "saving...\n");
            }
            while (mem_range != null) {
                UBytePtr data = new UBytePtr(mem_range.num_bytes);
                if (data != null) {
                    /*	this buffer will almost certainly be small
					enough to be dynamically allocated, but let's
					avoid memory trashing just in case
                     */
                    copy_from_memory(mem_range.cpu, mem_range.addr, data, mem_range.num_bytes);
                    osd_fwrite(f, data, mem_range.num_bytes);
                }
                mem_range = mem_range.next;
            }
            osd_fclose(f);
        }
    }

    /**
     * **************************************************************************
     */
    /* public API */
 /* call hs_open once after loading a game */
    public static void hs_open(String name) {
        state.mem_range = null;
        if (errorlog != null) {
            fprintf(errorlog, "hs_open: '%s'\n", name);
        }
        if (hiscoreFileParser.loadHiscoreFile("hiscore.dat") != 0) {
            hiscoreFileParser.read(name);
        }
    }

    /* call hs_init when emulation starts, and when the game is reset */
    public static void hs_init() {
        mem_range mem_range = state.mem_range;
        state.hiscores_have_been_loaded = 0;

        while (mem_range != null) {
            computer_writemem_byte(
                    mem_range.cpu,
                    mem_range.addr,
                    ~mem_range.start_value
            );

            computer_writemem_byte(
                    mem_range.cpu,
                    mem_range.addr + mem_range.num_bytes - 1,
                    ~mem_range.end_value
            );
            mem_range = mem_range.next;
        }
    }

    /* call hs_update periodically (i.e. once per frame) */
    public static void hs_update() {
        if (state.mem_range != null) {
            if (state.hiscores_have_been_loaded == 0) {
                if (safe_to_load() != 0) {
                    hs_load();
                }
            }
        }
    }

    /* call hs_close when done playing game */
    public static void hs_close() {
        if (state.hiscores_have_been_loaded != 0) {
            hs_save();
        }
        hs_free();
    }

}
