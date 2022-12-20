/**
 * ported to v0.36
 */
package arcadeflex.v036.mame;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.commonH.*;
//common imports
import static common.libc.cstring.*;
//TODO
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import gr.codebb.arcadeflex.v036.platform.libc_old.FILE;
import static gr.codebb.arcadeflex.v036.platform.libc_old.fclose;
import static gr.codebb.arcadeflex.v036.platform.libc_old.fopen;
import static gr.codebb.arcadeflex.v036.platform.libc_old.fprintf;
import java.util.Arrays;

public class memory {

    public static final boolean MEM_DUMP = false;

    /* Convenience macros - not in cpuintrf.h because they shouldn't be used by everyone */
    public static int ADDRESS_BITS(int index) {
        return (cpuintf[Machine.drv.cpu[index].cpu_type & ~CPU_FLAGS_MASK].address_bits);
    }

    public static int ABITS1(int index) {
        return (cpuintf[Machine.drv.cpu[index].cpu_type & ~CPU_FLAGS_MASK].abits1);
    }

    public static int ABITS2(int index) {
        return (cpuintf[Machine.drv.cpu[index].cpu_type & ~CPU_FLAGS_MASK].abits2);
    }

    public static int ABITS3(int index) {
        return (0);
    }

    public static int ABITSMIN(int index) {
        return (cpuintf[Machine.drv.cpu[index].cpu_type & ~CPU_FLAGS_MASK].abitsmin);
    }

    public static int ALIGNUNIT(int index) {
        return (cpuintf[Machine.drv.cpu[index].cpu_type & ~CPU_FLAGS_MASK].align_unit);
    }

    public static int BYTE_XOR_BE(int a) {
        return a ^ 1;
    }

    public static int BYTE_XOR_LE(int a) {
        return a;
    }

    public static UBytePtr OP_RAM = new UBytePtr();
    public static UBytePtr OP_ROM = new UBytePtr();

    /* change bases preserving opcode/data shift for encrypted games */
    public static void SET_OP_RAMROM(UBytePtr _base) {
        OP_ROM = new UBytePtr(_base, (OP_ROM.offset - OP_RAM.offset));
        OP_RAM = new UBytePtr(_base);
    }
    public static char u8_ophw;/* op-code hardware number */

    public static ExtMemory[] ext_memory = new ExtMemory[MAX_EXT_MEMORY];
    public static UBytePtr[] ramptr = new UBytePtr[MAX_CPU];
    public static UBytePtr[] romptr = new UBytePtr[MAX_CPU];

    /* element shift bits, mask bits */
    public static int[][] mhshift = new int[MAX_CPU][3];
    public static int[][] mhmask = new int[MAX_CPU][3];

    /* pointers to port structs */
 /* ASG: port speedup */
    static IOReadPort[][] readport = new IOReadPort[MAX_CPU][];
    static IOWritePort[][] writeport = new IOWritePort[MAX_CPU][];
    static int[] portmask = new int[MAX_CPU];
    static int[] readport_size = new int[MAX_CPU];
    static int[] writeport_size = new int[MAX_CPU];
    /* HJB 990210: removed 'static' for access by assembly CPU core memory handlers */
    static IOReadPort[] cur_readport;
    static IOWritePort[] cur_writeport;
    static int cur_portmask;

    /* current hardware element map */
    public static char[][] u8_cur_mr_element = new char[MAX_CPU][];
    public static char[][] u8_cur_mw_element = new char[MAX_CPU][];
    /* sub memory/port hardware element map */
 /* HJB 990210: removed 'static' for access by assembly CPU core memory handlers */
    public static char[] u8_readhardware = new char[MH_ELEMAX << MH_SBITS];
    public static char[] u8_writehardware = new char[MH_ELEMAX << MH_SBITS];

    /* memory hardware element map */
 /* value:					   */
    public static final int HT_RAM = 0;/* RAM direct		 */
    static final int HT_BANK1 = 1;/* bank memory #1	 */
    static final int HT_BANK2 = 2;/* bank memory #2	 */
    static final int HT_BANK3 = 3;/* bank memory #3	 */
    static final int HT_BANK4 = 4;/* bank memory #4	 */
    static final int HT_BANK5 = 5;/* bank memory #5	 */
    static final int HT_BANK6 = 6;/* bank memory #6	 */
    static final int HT_BANK7 = 7;/* bank memory #7	 */
    static final int HT_BANK8 = 8;/* bank memory #8	 */
    static final int HT_BANK9 = 9;/* bank memory #9	 */
    static final int HT_BANK10 = 10;/* bank memory #10	 */
    static final int HT_BANK11 = 11;/* bank memory #11	 */
    static final int HT_BANK12 = 12;/* bank memory #12	 */
    static final int HT_BANK13 = 13;/* bank memory #13	 */
    static final int HT_BANK14 = 14;/* bank memory #14	 */
    static final int HT_BANK15 = 15;/* bank memory #15	 */
    static final int HT_BANK16 = 16;/* bank memory #16	 */
    static final int HT_NON = 17;/* non mapped memory */
    static final int HT_NOP = 18;/* NOP memory		 */
    static final int HT_RAMROM = 19;/* RAM ROM memory	 */
    static final int HT_ROM = 20;/* ROM memory		 */
    static final int HT_USER = 21;
    /* user functions	 */
 /* [MH_HARDMAX]-0xff	  link to sub memory element  */
 /*						  (value-MH_HARDMAX)<<MH_SBITS -> element bank */

    public static final int HT_BANKMAX = (HT_BANK1 + MAX_BANKS - 1);

    /* memory hardware handler */
 /* HJB 990210: removed 'static' for access by assembly CPU core memory handlers */
    public static ReadHandlerPtr[] memoryreadhandler = new ReadHandlerPtr[MH_HARDMAX];
    public static int[] memoryreadoffset = new int[MH_HARDMAX];
    public static WriteHandlerPtr[] memorywritehandler = new WriteHandlerPtr[MH_HARDMAX];
    public static int[] memorywriteoffset = new int[MH_HARDMAX];

    /* bank ram base address; RAM is bank 0 */
    public static UBytePtr[] cpu_bankbase = new UBytePtr[HT_BANKMAX + 1];

    public static int[] bankreadoffset = new int[HT_BANKMAX + 1];
    public static int[] bankwriteoffset = new int[HT_BANKMAX + 1];

    ///* override OP base handler */
    public static opbase_handlerPtr[] setOPbasefunc = new opbase_handlerPtr[MAX_CPU];
    public static opbase_handlerPtr OPbasefunc;

    /* current cpu current hardware element map point */
    public static char[] u8_cur_mrhard;
    public static char[] u8_cur_mwhard;

    /* empty port handler structures */
    public static IOReadPort[] empty_readport = {new IOReadPort(-1)};
    public static IOWritePort[] empty_writeport = {new IOWritePort(-1)};

    /**
     * *************************************************************************
     *
     * Memory read handling
     *
     **************************************************************************
     */
    public static ReadHandlerPtr mrh_ram = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[0].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank1 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[1].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank2 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[2].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank3 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[3].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank4 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[4].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank5 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[5].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank6 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[6].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank7 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[7].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank8 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[8].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank9 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[9].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank10 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[10].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank11 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[11].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank12 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[12].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank13 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[13].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank14 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[14].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank15 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[15].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_bank16 = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_bankbase[16].read(offset);
        }
    };

    public static ReadHandlerPtr bank_read_handler[]
            = {
                mrh_ram, mrh_bank1, mrh_bank2, mrh_bank3, mrh_bank4, mrh_bank5, mrh_bank6, mrh_bank7,
                mrh_bank8, mrh_bank9, mrh_bank10, mrh_bank11, mrh_bank12, mrh_bank13, mrh_bank14, mrh_bank15,
                mrh_bank16
            };

    public static ReadHandlerPtr mrh_error = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (errorlog != null) {
                fprintf(errorlog, "CPU #%d PC %04x: warning - read %02x from unmapped memory address %04x\n", cpu_getactivecpu(), cpu_get_pc(), cpu_bankbase[0].read(offset), offset);
            }
            return cpu_bankbase[0].read(offset);
        }
    };
    public static ReadHandlerPtr mrh_error_sparse = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (errorlog != null) {
                fprintf(errorlog, "CPU #%d PC %08x: warning - read unmapped memory address %08x\n", cpu_getactivecpu(), cpu_get_pc(), offset);
            }
            return 0;
        }
    };

    public static ReadHandlerPtr mrh_error_sparse_bit = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (errorlog != null) {
                fprintf(errorlog, "CPU #%d PC %08x: warning - read unmapped memory bit addr %08x (byte addr %08x)\n", cpu_getactivecpu(), cpu_get_pc(), offset << 3, offset);
            }
            return 0;
        }
    };

    public static ReadHandlerPtr mrh_nop = new ReadHandlerPtr() {
        public int handler(int offset) {
            return 0;
        }
    };
    /**
     * *************************************************************************
     *
     * Memory write handling
     *
     **************************************************************************
     */
    public static WriteHandlerPtr mwh_ram = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[0].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank1 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[1].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank2 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[2].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank3 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[3].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank4 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[4].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank5 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[5].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank6 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[6].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank7 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[7].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank8 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[8].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank9 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[9].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank10 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[10].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank11 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[11].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank12 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[12].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank13 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[13].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank14 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[14].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank15 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[15].write(offset, data);
        }
    };
    public static WriteHandlerPtr mwh_bank16 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[16].write(offset, data);
        }
    };

    public static WriteHandlerPtr bank_write_handler[]
            = {
                mwh_ram, mwh_bank1, mwh_bank2, mwh_bank3, mwh_bank4, mwh_bank5, mwh_bank6, mwh_bank7,
                mwh_bank8, mwh_bank9, mwh_bank10, mwh_bank11, mwh_bank12, mwh_bank13, mwh_bank14, mwh_bank15,
                mwh_bank16
            };

    public static WriteHandlerPtr mwh_error = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (errorlog != null) {
                fprintf(errorlog, "CPU #%d PC %04x: warning - write %02x to unmapped memory address %04x\n", cpu_getactivecpu(), cpu_get_pc(), data, offset);
            }
            cpu_bankbase[0].write(offset, data);
        }
    };

    public static WriteHandlerPtr mwh_error_sparse = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (errorlog != null) {
                fprintf(errorlog, "CPU #%d PC %08x: warning - write %02x to unmapped memory address %08x\n", cpu_getactivecpu(), cpu_get_pc(), data, offset);
            }
        }
    };

    public static WriteHandlerPtr mwh_error_sparse_bit = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (errorlog != null) {
                fprintf(errorlog, "CPU #%d PC %08x: warning - write %02x to unmapped memory bit addr %08x\n", cpu_getactivecpu(), cpu_get_pc(), data, offset << 3);
            }
        }
    };

    public static WriteHandlerPtr mwh_rom = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (errorlog != null) {
                fprintf(errorlog, "CPU #%d PC %04x: warning - write %02x to ROM address %04x\n", cpu_getactivecpu(), cpu_get_pc(), data, offset);
            }
        }
    };

    public static WriteHandlerPtr mwh_ramrom = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_bankbase[0].write(offset + (OP_ROM.offset - OP_RAM.offset), data);
            cpu_bankbase[0].write(offset, data);
        }
    };

    public static WriteHandlerPtr mwh_nop = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
        }
    };

    /**
     * *************************************************************************
     *
     * Memory structure building
     *
     **************************************************************************
     */

    /* return element offset */
    static UBytePtr get_element(UBytePtr element, int ad, int elemask, UBytePtr subelement, int[] ele_max) {
        char u8_hw = element.read(ad);
        int i, ele;
        int banks = (elemask / (1 << MH_SBITS)) + 1;

        if (u8_hw >= MH_HARDMAX) {
            return new UBytePtr(subelement, (u8_hw - MH_HARDMAX) << MH_SBITS);
        }

        /* create new element block */
        if ((ele_max[0]) + banks > MH_ELEMAX) {
            if (errorlog != null) {
                fprintf(errorlog, "memory element size overflow\n");
            }
            return null;
        }
        /* get new element nunber */
        ele = ele_max[0];
        (ele_max[0]) += banks;

        if (MEM_DUMP) {
            if (errorlog != null) {
                fprintf(errorlog, "create element %2d(%2d)\n", ele, banks);
            }
        }

        /* set link mark to current element */
        element.write(ad, ele + MH_HARDMAX);
        /* get next subelement top */
        subelement = new UBytePtr(subelement, ele << MH_SBITS);
        /* initialize new block */
        for (i = 0; i < (banks << MH_SBITS); i++) {
            subelement.write(i, u8_hw);
        }
        return subelement;
    }

    static void set_element(int cpu, UBytePtr celement, int sp, int ep, char u8_type, UBytePtr subelement, int[] ele_max) {
        int i;
        int edepth = 0;
        int shift, mask;
        UBytePtr eele = celement;
        UBytePtr sele = celement;
        UBytePtr ele;
        int ss, sb, eb, ee;
        if (MEM_DUMP) {
            if (errorlog != null) {
                fprintf(errorlog, "set_element %8X-%8X = %2X\n", sp, ep, (int) u8_type);
            }
        }
        if ( /*(unsigned int)*/sp > /*(unsigned int)*/ ep) {/*TODO*///check if we have to change this to long and casting (shadow)
            return;
        }
        do {
            mask = mhmask[cpu][edepth];
            shift = mhshift[cpu][edepth];

            /* center element */
            ss = /*(unsigned int)*/ sp >>> shift;
            sb = /*(unsigned int)*/ sp != 0 ? (/*(unsigned int)*/(sp - 1) >>> shift) + 1 : 0;
            eb = (/*(unsigned int)*/(ep + 1) >>> shift) - 1;
            ee = /*(unsigned int)*/ ep >>> shift;

            if (sb <= eb) {
                if ((sb | mask) == (eb | mask)) {
                    /* same reason */
                    ele = (sele != null ? sele : eele);
                    for (i = sb; i <= eb; i++) {
                        ele.write(i & mask, u8_type);
                    }
                } else {
                    if (sele != null) {
                        for (i = sb; i <= (sb | mask); i++) {
                            sele.write(i & mask, u8_type);
                        }
                    }
                    if (eele != null) {
                        for (i = eb & (~mask); i <= eb; i++) {
                            eele.write(i & mask, u8_type);
                        }
                    }
                }
            }

            edepth++;

            if (ss == sb) {
                sele = null;
            } else {
                sele = get_element(sele, ss & mask, mhmask[cpu][edepth],
                        subelement, ele_max);
            }
            if (ee == eb) {
                eele = null;
            } else {
                eele = get_element(eele, ee & mask, mhmask[cpu][edepth],
                        subelement, ele_max);
            }

        } while (sele != null || eele != null);
    }

    /* ASG 980121 -- allocate all the external memory */
    static int memory_allocate_ext() {
        int ext = 0;//struct ExtMemory *ext = ext_memory;
        int cpu;

        /* a change for MESS */
        if (Machine.gamedrv.rom == null) {
            return 1;
        }

        /* loop over all CPUs */
        for (cpu = 0; cpu < cpu_gettotalcpu(); cpu++) {
            int region = REGION_CPU1 + cpu;
            int size = memory_region_length(region);

            /* now it's time to loop */
            while (true) {
                int lowest = 0x7fffffff, end, lastend;

                /* find the base of the lowest memory region that extends past the end */
                for (int mra = 0; Machine.drv.cpu[cpu].memory_read[mra].start != -1; mra++) {
                    if (Machine.drv.cpu[cpu].memory_read[mra].end >= size
                            && Machine.drv.cpu[cpu].memory_read[mra].start < lowest) {
                        lowest = Machine.drv.cpu[cpu].memory_read[mra].start;
                    }
                }

                for (int mwa = 0; Machine.drv.cpu[cpu].memory_write[mwa].start != -1; mwa++) {
                    if (Machine.drv.cpu[cpu].memory_write[mwa].end >= size
                            && Machine.drv.cpu[cpu].memory_write[mwa].start < lowest) {
                        lowest = Machine.drv.cpu[cpu].memory_write[mwa].start;
                    }
                }

                /* done if nothing found */
                if (lowest == 0x7fffffff) {
                    break;
                }

                /* now loop until we find the end of this contiguous block of memory */
                lastend = -1;
                end = lowest;
                while (end != lastend) {
                    lastend = end;

                    /* find the base of the lowest memory region that extends past the end */
                    for (int mra = 0; Machine.drv.cpu[cpu].memory_read[mra].start != -1; mra++) {
                        if (Machine.drv.cpu[cpu].memory_read[mra].start <= end && Machine.drv.cpu[cpu].memory_read[mra].end > end) {
                            end = Machine.drv.cpu[cpu].memory_read[mra].end;
                        }
                    }

                    for (int mwa = 0; Machine.drv.cpu[cpu].memory_write[mwa].start != -1; mwa++) {
                        if (Machine.drv.cpu[cpu].memory_write[mwa].start <= end && Machine.drv.cpu[cpu].memory_write[mwa].end > end) {
                            end = Machine.drv.cpu[cpu].memory_write[mwa].end;
                        }
                    }

                }

                /* time to allocate */
                ext_memory[ext].start = lowest;
                ext_memory[ext].end = end;
                ext_memory[ext].region = region;
                ext_memory[ext].data = new UBytePtr(end + 1 - lowest);

                /* if that fails, we're through */
                if (ext_memory[ext].data == null) {
                    return 0;
                }

                /* reset the memory */
                memset(ext_memory[ext].data, 0, end + 1 - lowest);
                size = ext_memory[ext].end + 1;
                ext++;
            }
        }

        return 1;
    }

    /*TODO*///
/*TODO*///
/*TODO*///unsigned char *findmemorychunk(int cpu, int offset, int *chunkstart, int *chunkend)
/*TODO*///{
/*TODO*///	int region = REGION_CPU1+cpu;
/*TODO*///	struct ExtMemory *ext;
/*TODO*///
/*TODO*///	/* look in external memory first */
/*TODO*///	for (ext = ext_memory; ext->data; ext++)
/*TODO*///		if (ext->region == region && ext->start <= offset && ext->end >= offset)
/*TODO*///		{
/*TODO*///			*chunkstart = ext->start;
/*TODO*///			*chunkend = ext->end;
/*TODO*///			return ext->data;
/*TODO*///		}
/*TODO*///
/*TODO*///	/* return RAM */
/*TODO*///	*chunkstart = 0;
/*TODO*///	*chunkend = memory_region_length(region) - 1;
/*TODO*///	return ramptr[cpu];
/*TODO*///}
/*TODO*///
    public static UBytePtr memory_find_base(int cpu, int offset) {

        int region = REGION_CPU1 + cpu;

        /* look in external memory first */
        for (ExtMemory ext : ext_memory) {
            if (ext.data == null) {
                break;
            }
            if (ext.region == region && ext.start <= offset && ext.end >= offset) {
                return new UBytePtr(ext.data, (offset - ext.start));
            }
        }
        return new UBytePtr(ramptr[cpu], offset);
    }

    /* make these static so they can be used in a callback by game drivers */
    static int[] rdelement_max = new int[1];
    static int[] wrelement_max = new int[1];
    static int rdhard_max = HT_USER;
    static int wrhard_max = HT_USER;

    /* return = FALSE:can't allocate element memory */
    public static int memory_init() {
        /*java code intialaze ext_memory stuff elements (shadow) */
        for (int x = 0; x < MAX_EXT_MEMORY; x++) {
            ext_memory[x] = new ExtMemory();
        }
        int i, cpu;
        /*TODO*///	MHELE hardware;
        int abits1, abits2, abits3, abitsmin;
        rdelement_max[0] = 0;
        wrelement_max[0] = 0;
        rdhard_max = HT_USER;
        wrhard_max = HT_USER;

        for (cpu = 0; cpu < MAX_CPU; cpu++) {
            u8_cur_mr_element[cpu] = null;
            u8_cur_mw_element[cpu] = null;
        }
        u8_ophw = 0xff;

        /* ASG 980121 -- allocate external memory */
        if (memory_allocate_ext() == 0) {
            return 0;
        }
        /*TODO*///
/*TODO*///	for( cpu = 0 ; cpu < cpu_gettotalcpu() ; cpu++ )
/*TODO*///	{
/*TODO*///		const struct MemoryReadAddress *_mra;
/*TODO*///		const struct MemoryWriteAddress *_mwa;
/*TODO*///
/*TODO*///		setOPbasefunc[cpu] = NULL;
/*TODO*///
/*TODO*///		ramptr[cpu] = romptr[cpu] = memory_region(REGION_CPU1+cpu);
/*TODO*///
/*TODO*///		/* initialize the memory base pointers for memory hooks */
/*TODO*///		_mra = Machine->drv->cpu[cpu].memory_read;
/*TODO*///		if (_mra)
/*TODO*///		{
/*TODO*///			while (_mra->start != -1)
/*TODO*///			{
/*TODO*/////				if (_mra->base) *_mra->base = memory_find_base (cpu, _mra->start);
/*TODO*/////				if (_mra->size) *_mra->size = _mra->end - _mra->start + 1;
/*TODO*///				_mra++;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		_mwa = Machine->drv->cpu[cpu].memory_write;
/*TODO*///		if (_mwa)
/*TODO*///		{
/*TODO*///			while (_mwa->start != -1)
/*TODO*///			{
/*TODO*///				if (_mwa->base) *_mwa->base = memory_find_base (cpu, _mwa->start);
/*TODO*///				if (_mwa->size) *_mwa->size = _mwa->end - _mwa->start + 1;
/*TODO*///				_mwa++;
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		/* initialize port structures */
/*TODO*///		readport_size[cpu] = 0;
/*TODO*///		writeport_size[cpu] = 0;
/*TODO*///		readport[cpu] = 0;
/*TODO*///		writeport[cpu] = 0;
/*TODO*///
/*TODO*///		/* install port handlers - at least an empty one */
/*TODO*///		ioread = Machine->drv->cpu[cpu].port_read;
/*TODO*///		if (ioread == 0)  ioread = empty_readport;
/*TODO*///
/*TODO*///		while (1)
/*TODO*///		{
/*TODO*///			if (install_port_read_handler_common(cpu, ioread->start, ioread->end, ioread->handler, 0) == 0)
/*TODO*///			{
/*TODO*///				memory_shutdown();
/*TODO*///				return 0;
/*TODO*///			}
/*TODO*///
/*TODO*///			if (ioread->start == -1)  break;
/*TODO*///
/*TODO*///			ioread++;
/*TODO*///		}
/*TODO*///
/*TODO*///
/*TODO*///		iowrite = Machine->drv->cpu[cpu].port_write;
/*TODO*///		if (iowrite == 0)  iowrite = empty_writeport;
/*TODO*///
/*TODO*///		while (1)
/*TODO*///		{
/*TODO*///			if (install_port_write_handler_common(cpu, iowrite->start, iowrite->end, iowrite->handler, 0) == 0)
/*TODO*///			{
/*TODO*///				memory_shutdown();
/*TODO*///				return 0;
/*TODO*///			}
/*TODO*///
/*TODO*///			if (iowrite->start == -1)  break;
/*TODO*///
/*TODO*///			iowrite++;
/*TODO*///		}
/*TODO*///
/*TODO*///		portmask[cpu] = 0xffff;
/*TODO*///#if HAS_Z80
/*TODO*///        if ((Machine->drv->cpu[cpu].cpu_type & ~CPU_FLAGS_MASK) == CPU_Z80 &&
/*TODO*///			(Machine->drv->cpu[cpu].cpu_type & CPU_16BIT_PORT) == 0)
/*TODO*///			portmask[cpu] = 0xff;
/*TODO*///#endif
/*TODO*///    }
/*TODO*///
/*TODO*///	/* initialize grobal handler */
/*TODO*///	for( i = 0 ; i < MH_HARDMAX ; i++ ){
/*TODO*///		memoryreadoffset[i] = 0;
/*TODO*///		memorywriteoffset[i] = 0;
/*TODO*///	}
/*TODO*///	/* bank memory */
/*TODO*///	for (i = 1; i <= MAX_BANKS; i++)
/*TODO*///	{
/*TODO*///		memoryreadhandler[i] = bank_read_handler[i];
/*TODO*///		memorywritehandler[i] = bank_write_handler[i];
/*TODO*///	}
/*TODO*///	/* non map memory */
/*TODO*///	memoryreadhandler[HT_NON] = mrh_error;
/*TODO*///	memorywritehandler[HT_NON] = mwh_error;
/*TODO*///	/* NOP memory */
/*TODO*///	memoryreadhandler[HT_NOP] = mrh_nop;
/*TODO*///	memorywritehandler[HT_NOP] = mwh_nop;
/*TODO*///	/* RAMROM memory */
/*TODO*///	memorywritehandler[HT_RAMROM] = mwh_ramrom;
/*TODO*///	/* ROM memory */
/*TODO*///	memorywritehandler[HT_ROM] = mwh_rom;
/*TODO*///
/*TODO*///	/* if any CPU is 21-bit or more, we change the error handlers to be more benign */
/*TODO*///	for (cpu = 0; cpu < cpu_gettotalcpu(); cpu++)
/*TODO*///		if (ADDRESS_BITS (cpu) >= 21)
/*TODO*///		{
/*TODO*///			memoryreadhandler[HT_NON] = mrh_error_sparse;
/*TODO*///			memorywritehandler[HT_NON] = mwh_error_sparse;
/*TODO*///#if HAS_TMS34010
/*TODO*///            if ((Machine->drv->cpu[cpu].cpu_type & ~CPU_FLAGS_MASK)==CPU_TMS34010)
/*TODO*///			{
/*TODO*///				memoryreadhandler[HT_NON] = mrh_error_sparse_bit;
/*TODO*///				memorywritehandler[HT_NON] = mwh_error_sparse_bit;
/*TODO*///			}
/*TODO*///#endif
/*TODO*///        }
/*TODO*///
/*TODO*///	for( cpu = 0 ; cpu < cpu_gettotalcpu() ; cpu++ )
/*TODO*///	{
/*TODO*///		/* cpu selection */
/*TODO*///		abits1 = ABITS1 (cpu);
/*TODO*///		abits2 = ABITS2 (cpu);
/*TODO*///		abits3 = ABITS3 (cpu);
/*TODO*///		abitsmin = ABITSMIN (cpu);
/*TODO*///
/*TODO*///		/* element shifter , mask set */
/*TODO*///		mhshift[cpu][0] = (abits2+abits3);
/*TODO*///		mhshift[cpu][1] = abits3;			/* 2nd */
/*TODO*///		mhshift[cpu][2] = 0;				/* 3rd (used by set_element)*/
/*TODO*///		mhmask[cpu][0]  = MHMASK(abits1);		/*1st(used by set_element)*/
/*TODO*///		mhmask[cpu][1]  = MHMASK(abits2);		/*2nd*/
/*TODO*///		mhmask[cpu][2]  = MHMASK(abits3);		/*3rd*/
/*TODO*///
/*TODO*///		/* allocate current element */
/*TODO*///		if( (cur_mr_element[cpu] = (MHELE *)malloc(sizeof(MHELE)<<abits1)) == 0 )
/*TODO*///		{
/*TODO*///			memory_shutdown();
/*TODO*///			return 0;
/*TODO*///		}
/*TODO*///		if( (cur_mw_element[cpu] = (MHELE *)malloc(sizeof(MHELE)<<abits1)) == 0 )
/*TODO*///		{
/*TODO*///			memory_shutdown();
/*TODO*///			return 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		/* initialize curent element table */
/*TODO*///		for( i = 0 ; i < (1<<abits1) ; i++ )
/*TODO*///		{
/*TODO*///			cur_mr_element[cpu][i] = HT_NON;	/* no map memory */
/*TODO*///			cur_mw_element[cpu][i] = HT_NON;	/* no map memory */
/*TODO*///		}
/*TODO*///
/*TODO*///		memoryread = Machine->drv->cpu[cpu].memory_read;
/*TODO*///		memorywrite = Machine->drv->cpu[cpu].memory_write;
/*TODO*///
/*TODO*///		/* memory read handler build */
/*TODO*///		if (memoryread)
/*TODO*///		{
/*TODO*///			mra = memoryread;
/*TODO*///			while (mra->start != -1) mra++;
/*TODO*///			mra--;
/*TODO*///
/*TODO*///			while (mra >= memoryread)
/*TODO*///			{
/*TODO*///				mem_read_handler handler = mra->handler;
/*TODO*///
/*TODO*////* work around a compiler bug */
/*TODO*///#ifdef SGI_FIX_MWA_NOP
/*TODO*///				if ((FPTR)handler == (FPTR)MRA_NOP) {
/*TODO*///					hardware = HT_NOP;
/*TODO*///				} else {
/*TODO*///#endif
/*TODO*///				switch ((FPTR)handler)
/*TODO*///				{
/*TODO*///				case (FPTR)MRA_RAM:
/*TODO*///				case (FPTR)MRA_ROM:
/*TODO*///					hardware = HT_RAM;	/* sprcial case ram read */
/*TODO*///					break;
/*TODO*///				case (FPTR)MRA_BANK1:
/*TODO*///				case (FPTR)MRA_BANK2:
/*TODO*///				case (FPTR)MRA_BANK3:
/*TODO*///				case (FPTR)MRA_BANK4:
/*TODO*///				case (FPTR)MRA_BANK5:
/*TODO*///				case (FPTR)MRA_BANK6:
/*TODO*///				case (FPTR)MRA_BANK7:
/*TODO*///				case (FPTR)MRA_BANK8:
/*TODO*///				case (FPTR)MRA_BANK9:
/*TODO*///				case (FPTR)MRA_BANK10:
/*TODO*///				case (FPTR)MRA_BANK11:
/*TODO*///				case (FPTR)MRA_BANK12:
/*TODO*///				case (FPTR)MRA_BANK13:
/*TODO*///				case (FPTR)MRA_BANK14:
/*TODO*///				case (FPTR)MRA_BANK15:
/*TODO*///				case (FPTR)MRA_BANK16:
/*TODO*///				{
/*TODO*///					hardware = (int)MRA_BANK1 - (int)handler + 1;
/*TODO*///					memoryreadoffset[hardware] = bankreadoffset[hardware] = mra->start;
/*TODO*///					cpu_bankbase[hardware] = memory_find_base(cpu, mra->start);
/*TODO*///					break;
/*TODO*///				}
/*TODO*///				case (FPTR)MRA_NOP:
/*TODO*///					hardware = HT_NOP;
/*TODO*///					break;
/*TODO*///				default:
/*TODO*///					/* create newer hardware handler */
/*TODO*///					if( rdhard_max == MH_HARDMAX )
/*TODO*///					{
/*TODO*///						if (errorlog)
/*TODO*///						 fprintf(errorlog,"read memory hardware pattern over !\n");
/*TODO*///						hardware = 0;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						/* regist hardware function */
/*TODO*///						hardware = rdhard_max++;
/*TODO*///						memoryreadhandler[hardware] = handler;
/*TODO*///						memoryreadoffset[hardware] = mra->start;
/*TODO*///					}
/*TODO*///				}
/*TODO*///#ifdef SGI_FIX_MWA_NOP
/*TODO*///				}
/*TODO*///#endif
/*TODO*///				/* hardware element table make */
/*TODO*///				set_element( cpu , cur_mr_element[cpu] ,
/*TODO*///					(((unsigned int) mra->start) >> abitsmin) ,
/*TODO*///					(((unsigned int) mra->end) >> abitsmin) ,
/*TODO*///					hardware , readhardware , &rdelement_max );
/*TODO*///
/*TODO*///				mra--;
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		/* memory write handler build */
/*TODO*///		if (memorywrite)
/*TODO*///		{
/*TODO*///			mwa = memorywrite;
/*TODO*///			while (mwa->start != -1) mwa++;
/*TODO*///			mwa--;
/*TODO*///
/*TODO*///			while (mwa >= memorywrite)
/*TODO*///			{
/*TODO*///				mem_write_handler handler = mwa->handler;
/*TODO*///#ifdef SGI_FIX_MWA_NOP
/*TODO*///				if ((FPTR)handler == (FPTR)MWA_NOP) {
/*TODO*///					hardware = HT_NOP;
/*TODO*///				} else {
/*TODO*///#endif
/*TODO*///				switch( (FPTR)handler )
/*TODO*///				{
/*TODO*///				case (FPTR)MWA_RAM:
/*TODO*///					hardware = HT_RAM;	/* sprcial case ram write */
/*TODO*///					break;
/*TODO*///				case (FPTR)MWA_BANK1:
/*TODO*///				case (FPTR)MWA_BANK2:
/*TODO*///				case (FPTR)MWA_BANK3:
/*TODO*///				case (FPTR)MWA_BANK4:
/*TODO*///				case (FPTR)MWA_BANK5:
/*TODO*///				case (FPTR)MWA_BANK6:
/*TODO*///				case (FPTR)MWA_BANK7:
/*TODO*///				case (FPTR)MWA_BANK8:
/*TODO*///				case (FPTR)MWA_BANK9:
/*TODO*///				case (FPTR)MWA_BANK10:
/*TODO*///				case (FPTR)MWA_BANK11:
/*TODO*///				case (FPTR)MWA_BANK12:
/*TODO*///				case (FPTR)MWA_BANK13:
/*TODO*///				case (FPTR)MWA_BANK14:
/*TODO*///				case (FPTR)MWA_BANK15:
/*TODO*///				case (FPTR)MWA_BANK16:
/*TODO*///				{
/*TODO*///					hardware = (int)MWA_BANK1 - (int)handler + 1;
/*TODO*///					memorywriteoffset[hardware] = bankwriteoffset[hardware] = mwa->start;
/*TODO*///					cpu_bankbase[hardware] = memory_find_base(cpu, mwa->start);
/*TODO*///					break;
/*TODO*///				}
/*TODO*///				case (FPTR)MWA_NOP:
/*TODO*///					hardware = HT_NOP;
/*TODO*///					break;
/*TODO*///				case (FPTR)MWA_RAMROM:
/*TODO*///					hardware = HT_RAMROM;
/*TODO*///					break;
/*TODO*///				case (FPTR)MWA_ROM:
/*TODO*///					hardware = HT_ROM;
/*TODO*///					break;
/*TODO*///				default:
/*TODO*///					/* create newer hardware handler */
/*TODO*///					if( wrhard_max == MH_HARDMAX ){
/*TODO*///						if (errorlog)
/*TODO*///						 fprintf(errorlog,"write memory hardware pattern over !\n");
/*TODO*///						hardware = 0;
/*TODO*///					}else{
/*TODO*///						/* regist hardware function */
/*TODO*///						hardware = wrhard_max++;
/*TODO*///						memorywritehandler[hardware] = handler;
/*TODO*///						memorywriteoffset[hardware]  = mwa->start;
/*TODO*///					}
/*TODO*///				}
/*TODO*///#ifdef SGI_FIX_MWA_NOP
/*TODO*///				}
/*TODO*///#endif
/*TODO*///				/* hardware element table make */
/*TODO*///				set_element( cpu , cur_mw_element[cpu] ,
/*TODO*///					(int) (((unsigned int) mwa->start) >> abitsmin) ,
/*TODO*///					(int) (((unsigned int) mwa->end) >> abitsmin) ,
/*TODO*///					hardware , (MHELE *)writehardware , &wrelement_max );
/*TODO*///
/*TODO*///				mwa--;
/*TODO*///			}
/*TODO*///		}
/*TODO*///    }
/*TODO*///
/*TODO*///	if (errorlog){
/*TODO*///		fprintf(errorlog,"used read  elements %d/%d , functions %d/%d\n"
/*TODO*///		    ,rdelement_max,MH_ELEMAX , rdhard_max,MH_HARDMAX );
/*TODO*///		fprintf(errorlog,"used write elements %d/%d , functions %d/%d\n"
/*TODO*///		    ,wrelement_max,MH_ELEMAX , wrhard_max,MH_HARDMAX );
/*TODO*///	}
/*TODO*///#ifdef MEM_DUMP
/*TODO*///	mem_dump();
/*TODO*///#endif
        return 1;/* ok */
    }

    public static void memory_set_opcode_base(int cpu, UBytePtr base) {
        romptr[cpu] = base;
    }

    public static void memorycontextswap(int activecpu) {
        cpu_bankbase[0] = ramptr[activecpu];

        u8_cur_mrhard = u8_cur_mr_element[activecpu];
        u8_cur_mwhard = u8_cur_mw_element[activecpu];

        /* ASG: port speedup */
        cur_readport = readport[activecpu];
        cur_writeport = writeport[activecpu];
        cur_portmask = portmask[activecpu];

        OPbasefunc = setOPbasefunc[activecpu];

        /* op code memory pointer */
        u8_ophw = HT_RAM;
        OP_RAM = cpu_bankbase[0];
        OP_ROM = romptr[activecpu];
    }

    /*TODO*///
/*TODO*///void memory_shutdown(void)
/*TODO*///{
/*TODO*///	struct ExtMemory *ext;
/*TODO*///	int cpu;
/*TODO*///
/*TODO*///	for( cpu = 0 ; cpu < MAX_CPU ; cpu++ )
/*TODO*///	{
/*TODO*///		if( cur_mr_element[cpu] != 0 )
/*TODO*///		{
/*TODO*///			free( cur_mr_element[cpu] );
/*TODO*///			cur_mr_element[cpu] = 0;
/*TODO*///		}
/*TODO*///		if( cur_mw_element[cpu] != 0 )
/*TODO*///		{
/*TODO*///			free( cur_mw_element[cpu] );
/*TODO*///			cur_mw_element[cpu] = 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (readport[cpu] != 0)
/*TODO*///		{
/*TODO*///			free(readport[cpu]);
/*TODO*///			readport[cpu] = 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (writeport[cpu] != 0)
/*TODO*///		{
/*TODO*///			free(writeport[cpu]);
/*TODO*///			writeport[cpu] = 0;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* ASG 980121 -- free all the external memory */
/*TODO*///	for (ext = ext_memory; ext->data; ext++)
/*TODO*///		free (ext->data);
/*TODO*///	memset (ext_memory, 0, sizeof (ext_memory));
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Perform a memory read. This function is called by the CPU emulation.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*////* use these constants to define which type of memory handler to build */
/*TODO*///#define TYPE_8BIT					0		/* 8-bit aligned */
/*TODO*///#define TYPE_16BIT_BE				1		/* 16-bit aligned, big-endian */
/*TODO*///#define TYPE_16BIT_LE				2		/* 16-bit aligned, little-endian */
/*TODO*///
/*TODO*///#define CAN_BE_MISALIGNED			0		/* word/dwords can be read on non-16-bit boundaries */
/*TODO*///#define ALWAYS_ALIGNED				1		/* word/dwords are always read on 16-bit boundaries */
/*TODO*///
/*TODO*////* stupid workarounds so that we can generate an address mask that works even for 32 bits */
/*TODO*///#define ADDRESS_TOPBIT(abits)		(1UL << (ABITS1_##abits + ABITS2_##abits + ABITS_MIN_##abits - 1))
/*TODO*///#define ADDRESS_MASK(abits)			(ADDRESS_TOPBIT(abits) | (ADDRESS_TOPBIT(abits) - 1))
/*TODO*///
/*TODO*///
/*TODO*////* generic byte-sized read handler */
/*TODO*///#define READBYTE(name,type,abits)														\
/*TODO*///int name(int address)																	\
/*TODO*///{																						\
/*TODO*///	MHELE hw;																			\
/*TODO*///																						\
/*TODO*///	/* first-level lookup */															\
/*TODO*///	hw = cur_mrhard[(UINT32)address >> (ABITS2_##abits + ABITS_MIN_##abits)];			\
/*TODO*///																						\
/*TODO*///	/* for compatibility with setbankhandler, 8-bit systems must call handlers */		\
/*TODO*///	/* for banked memory reads/writes */												\
/*TODO*///	if (type == TYPE_8BIT && hw == HT_RAM)												\
/*TODO*///		return cpu_bankbase[HT_RAM][address];											\
/*TODO*///	else if (type != TYPE_8BIT && hw <= HT_BANKMAX)										\
/*TODO*///	{																					\
/*TODO*///		if (type == TYPE_16BIT_BE)														\
/*TODO*///			return cpu_bankbase[hw][BYTE_XOR_BE(address) - memoryreadoffset[hw]];		\
/*TODO*///		else if (type == TYPE_16BIT_LE)													\
/*TODO*///			return cpu_bankbase[hw][BYTE_XOR_LE(address) - memoryreadoffset[hw]];		\
/*TODO*///	}																					\
/*TODO*///																						\
/*TODO*///	/* second-level lookup */															\
/*TODO*///	if (hw >= MH_HARDMAX)																\
/*TODO*///	{																					\
/*TODO*///		hw -= MH_HARDMAX;																\
/*TODO*///		hw = readhardware[(hw << MH_SBITS) + (((UINT32)address >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))];	\
/*TODO*///																						\
/*TODO*///		/* for compatibility with setbankhandler, 8-bit systems must call handlers */	\
/*TODO*///		/* for banked memory reads/writes */											\
/*TODO*///		if (type == TYPE_8BIT && hw == HT_RAM)											\
/*TODO*///			return cpu_bankbase[HT_RAM][address];										\
/*TODO*///		else if (type != TYPE_8BIT && hw <= HT_BANKMAX)									\
/*TODO*///		{																				\
/*TODO*///			if (type == TYPE_16BIT_BE)													\
/*TODO*///				return cpu_bankbase[hw][BYTE_XOR_BE(address) - memoryreadoffset[hw]];	\
/*TODO*///			else if (type == TYPE_16BIT_LE)												\
/*TODO*///				return cpu_bankbase[hw][BYTE_XOR_LE(address) - memoryreadoffset[hw]];	\
/*TODO*///		}																				\
/*TODO*///	}																					\
/*TODO*///																						\
/*TODO*///	/* fall back to handler */															\
/*TODO*///	if (type == TYPE_8BIT)																\
/*TODO*///		return (*memoryreadhandler[hw])(address - memoryreadoffset[hw]);				\
/*TODO*///	else																				\
/*TODO*///	{																					\
/*TODO*///		int shift = (address & 1) << 3;													\
/*TODO*///		int data = (*memoryreadhandler[hw])((address & ~1) - memoryreadoffset[hw]);		\
/*TODO*///		if (type == TYPE_16BIT_BE)														\
/*TODO*///			return (data >> (shift ^ 8)) & 0xff;										\
/*TODO*///		else if (type == TYPE_16BIT_LE)													\
/*TODO*///			return (data >> shift) & 0xff;												\
/*TODO*///	}																					\
/*TODO*///}
/*TODO*///
/*TODO*////* generic word-sized read handler (16-bit aligned only!) */
/*TODO*///#define READWORD(name,type,abits,align)													\
/*TODO*///int name##_word(int address)															\
/*TODO*///{																						\
/*TODO*///	MHELE hw;																			\
/*TODO*///																						\
/*TODO*///	/* only supports 16-bit memory systems */											\
/*TODO*///	if (type == TYPE_8BIT)																\
/*TODO*///		printf("Unsupported type for READWORD macro!\n");								\
/*TODO*///																						\
/*TODO*///	/* handle aligned case first */														\
/*TODO*///	if (align == ALWAYS_ALIGNED || !(address & 1))										\
/*TODO*///	{																					\
/*TODO*///		/* first-level lookup */														\
/*TODO*///		hw = cur_mrhard[(UINT32)address >> (ABITS2_##abits + ABITS_MIN_##abits)];		\
/*TODO*///		if (hw <= HT_BANKMAX)															\
/*TODO*///			return READ_WORD(&cpu_bankbase[hw][address - memoryreadoffset[hw]]);		\
/*TODO*///																						\
/*TODO*///		/* second-level lookup */														\
/*TODO*///		if (hw >= MH_HARDMAX)															\
/*TODO*///		{																				\
/*TODO*///			hw -= MH_HARDMAX;															\
/*TODO*///			hw = readhardware[(hw << MH_SBITS) + (((UINT32)address >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))];	\
/*TODO*///			if (hw <= HT_BANKMAX)														\
/*TODO*///				return READ_WORD(&cpu_bankbase[hw][address - memoryreadoffset[hw]]);	\
/*TODO*///		}																				\
/*TODO*///																						\
/*TODO*///		/* fall back to handler */														\
/*TODO*///		return (*memoryreadhandler[hw])(address - memoryreadoffset[hw]);				\
/*TODO*///	}																					\
/*TODO*///																						\
/*TODO*///	/* unaligned case */																\
/*TODO*///	else if (type == TYPE_16BIT_BE)														\
/*TODO*///	{																					\
/*TODO*///		int data = name(address) << 8;													\
/*TODO*///		return data | (name(address + 1) & 0xff);										\
/*TODO*///	}																					\
/*TODO*///	else if (type == TYPE_16BIT_LE)														\
/*TODO*///	{																					\
/*TODO*///		int data = name(address) & 0xff;												\
/*TODO*///		return data | (name(address + 1) << 8);											\
/*TODO*///	}																					\
/*TODO*///}
/*TODO*///
/*TODO*////* generic dword-sized read handler (16-bit aligned only!) */
/*TODO*///#define READLONG(name,type,abits,align)													\
/*TODO*///int name##_dword(int address)															\
/*TODO*///{																						\
/*TODO*///	UINT16 word1, word2;																\
/*TODO*///	MHELE hw1, hw2;																		\
/*TODO*///																						\
/*TODO*///	/* only supports 16-bit memory systems */											\
/*TODO*///	if (type == TYPE_8BIT)																\
/*TODO*///		printf("Unsupported type for READWORD macro!\n");								\
/*TODO*///																						\
/*TODO*///	/* handle aligned case first */														\
/*TODO*///	if (align == ALWAYS_ALIGNED || !(address & 1))										\
/*TODO*///	{																					\
/*TODO*///		int address2 = (address + 2) & ADDRESS_MASK(abits);								\
/*TODO*///																						\
/*TODO*///		/* first-level lookup */														\
/*TODO*///		hw1 = cur_mrhard[(UINT32)address >> (ABITS2_##abits + ABITS_MIN_##abits)];		\
/*TODO*///		hw2 = cur_mrhard[(UINT32)address2 >> (ABITS2_##abits + ABITS_MIN_##abits)];		\
/*TODO*///																						\
/*TODO*///		/* second-level lookup */														\
/*TODO*///		if (hw1 >= MH_HARDMAX)															\
/*TODO*///		{																				\
/*TODO*///			hw1 -= MH_HARDMAX;															\
/*TODO*///			hw1 = readhardware[(hw1 << MH_SBITS) + (((UINT32)address >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))];	\
/*TODO*///		}																				\
/*TODO*///		if (hw2 >= MH_HARDMAX)															\
/*TODO*///		{																				\
/*TODO*///			hw2 -= MH_HARDMAX;															\
/*TODO*///			hw2 = readhardware[(hw2 << MH_SBITS) + (((UINT32)address2 >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))];	\
/*TODO*///		}																				\
/*TODO*///																						\
/*TODO*///		/* process each word */															\
/*TODO*///		if (hw1 <= HT_BANKMAX)															\
/*TODO*///			word1 = READ_WORD(&cpu_bankbase[hw1][address - memoryreadoffset[hw1]]);		\
/*TODO*///		else																			\
/*TODO*///			word1 = (*memoryreadhandler[hw1])(address - memoryreadoffset[hw1]);			\
/*TODO*///		if (hw2 <= HT_BANKMAX)															\
/*TODO*///			word2 = READ_WORD(&cpu_bankbase[hw2][address2 - memoryreadoffset[hw2]]);	\
/*TODO*///		else																			\
/*TODO*///			word2 = (*memoryreadhandler[hw2])(address2 - memoryreadoffset[hw2]);		\
/*TODO*///																						\
/*TODO*///		/* fall back to handler */														\
/*TODO*///		if (type == TYPE_16BIT_BE)														\
/*TODO*///			return (word1 << 16) | (word2 & 0xffff);									\
/*TODO*///		else if (type == TYPE_16BIT_LE)													\
/*TODO*///			return (word1 & 0xffff) | (word2 << 16);									\
/*TODO*///	}																					\
/*TODO*///																						\
/*TODO*///	/* unaligned case */																\
/*TODO*///	else if (type == TYPE_16BIT_BE)														\
/*TODO*///	{																					\
/*TODO*///		int data = name(address) << 24;													\
/*TODO*///		data |= name##_word(address + 1) << 8;											\
/*TODO*///		return data | (name(address + 3) & 0xff);										\
/*TODO*///	}																					\
/*TODO*///	else if (type == TYPE_16BIT_LE)														\
/*TODO*///	{																					\
/*TODO*///		int data = name(address) & 0xff;												\
/*TODO*///		data |= name##_word(address + 1) << 8;											\
/*TODO*///		return data | (name(address + 3) << 24);										\
/*TODO*///	}																					\
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////* the handlers we need to generate */
/*TODO*///READBYTE(cpu_readmem16,    TYPE_8BIT,     16)
/*TODO*///READBYTE(cpu_readmem20,    TYPE_8BIT,     20)
/*TODO*///READBYTE(cpu_readmem21,    TYPE_8BIT,     21)
/*TODO*///
/*TODO*///READBYTE(cpu_readmem16bew, TYPE_16BIT_BE, 16BEW)
/*TODO*///READWORD(cpu_readmem16bew, TYPE_16BIT_BE, 16BEW, ALWAYS_ALIGNED)
/*TODO*///
/*TODO*///READBYTE(cpu_readmem16lew, TYPE_16BIT_LE, 16LEW)
/*TODO*///READWORD(cpu_readmem16lew, TYPE_16BIT_LE, 16LEW, ALWAYS_ALIGNED)
/*TODO*///
/*TODO*///READBYTE(cpu_readmem24,    TYPE_16BIT_BE, 24)
/*TODO*///READWORD(cpu_readmem24,    TYPE_16BIT_BE, 24,    CAN_BE_MISALIGNED)
/*TODO*///READLONG(cpu_readmem24,    TYPE_16BIT_BE, 24,    CAN_BE_MISALIGNED)
/*TODO*///
/*TODO*///READBYTE(cpu_readmem29,    TYPE_16BIT_LE, 29)
/*TODO*///READWORD(cpu_readmem29,    TYPE_16BIT_LE, 29,    CAN_BE_MISALIGNED)
/*TODO*///READLONG(cpu_readmem29,    TYPE_16BIT_LE, 29,    CAN_BE_MISALIGNED)
/*TODO*///
/*TODO*///READBYTE(cpu_readmem32,    TYPE_16BIT_BE, 32)
/*TODO*///READWORD(cpu_readmem32,    TYPE_16BIT_BE, 32,    CAN_BE_MISALIGNED)
/*TODO*///READLONG(cpu_readmem32,    TYPE_16BIT_BE, 32,    CAN_BE_MISALIGNED)
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Perform a memory write. This function is called by the CPU emulation.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*////* generic byte-sized write handler */
/*TODO*///#define WRITEBYTE(name,type,abits)														\
/*TODO*///void name(int address, int data)														\
/*TODO*///{																						\
/*TODO*///	MHELE hw;																			\
/*TODO*///																						\
/*TODO*///	/* first-level lookup */															\
/*TODO*///	hw = cur_mwhard[(UINT32)address >> (ABITS2_##abits + ABITS_MIN_##abits)];			\
/*TODO*///																						\
/*TODO*///	/* for compatibility with setbankhandler, 8-bit systems must call handlers */		\
/*TODO*///	/* for banked memory reads/writes */												\
/*TODO*///	if (type == TYPE_8BIT && hw == HT_RAM)												\
/*TODO*///	{																					\
/*TODO*///		cpu_bankbase[HT_RAM][address] = data;											\
/*TODO*///		return;																			\
/*TODO*///	}																					\
/*TODO*///	else if (type != TYPE_8BIT && hw <= HT_BANKMAX)										\
/*TODO*///	{																					\
/*TODO*///		if (type == TYPE_16BIT_BE)														\
/*TODO*///			cpu_bankbase[hw][BYTE_XOR_BE(address) - memorywriteoffset[hw]] = data;		\
/*TODO*///		else if (type == TYPE_16BIT_LE)													\
/*TODO*///			cpu_bankbase[hw][BYTE_XOR_LE(address) - memorywriteoffset[hw]] = data;		\
/*TODO*///		return;																			\
/*TODO*///	}																					\
/*TODO*///																						\
/*TODO*///	/* second-level lookup */															\
/*TODO*///	if (hw >= MH_HARDMAX)																\
/*TODO*///	{																					\
/*TODO*///		hw -= MH_HARDMAX;																\
/*TODO*///		hw = writehardware[(hw << MH_SBITS) + (((UINT32)address >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))];	\
/*TODO*///																						\
/*TODO*///		/* for compatibility with setbankhandler, 8-bit systems must call handlers */	\
/*TODO*///		/* for banked memory reads/writes */											\
/*TODO*///		if (type == TYPE_8BIT && hw == HT_RAM)											\
/*TODO*///		{																				\
/*TODO*///			cpu_bankbase[HT_RAM][address] = data;										\
/*TODO*///			return;																		\
/*TODO*///		}																				\
/*TODO*///		else if (type != TYPE_8BIT && hw <= HT_BANKMAX)									\
/*TODO*///		{																				\
/*TODO*///			if (type == TYPE_16BIT_BE)													\
/*TODO*///				cpu_bankbase[hw][BYTE_XOR_BE(address) - memorywriteoffset[hw]] = data;	\
/*TODO*///			else if (type == TYPE_16BIT_LE)												\
/*TODO*///				cpu_bankbase[hw][BYTE_XOR_LE(address) - memorywriteoffset[hw]] = data;	\
/*TODO*///			return;																		\
/*TODO*///		}																				\
/*TODO*///	}																					\
/*TODO*///																						\
/*TODO*///	/* fall back to handler */															\
/*TODO*///	if (type != TYPE_8BIT)																\
/*TODO*///	{																					\
/*TODO*///		int shift = (address & 1) << 3;													\
/*TODO*///		if (type == TYPE_16BIT_BE)														\
/*TODO*///			shift ^= 8;																	\
/*TODO*///		data = (0xff000000 >> shift) | ((data & 0xff) << shift);						\
/*TODO*///		address &= ~1;																	\
/*TODO*///	}																					\
/*TODO*///	(*memorywritehandler[hw])(address - memorywriteoffset[hw], data);					\
/*TODO*///}
/*TODO*///
/*TODO*////* generic word-sized write handler (16-bit aligned only!) */
/*TODO*///#define WRITEWORD(name,type,abits,align)												\
/*TODO*///void name##_word(int address, int data)													\
/*TODO*///{																						\
/*TODO*///	MHELE hw;																			\
/*TODO*///																						\
/*TODO*///	/* only supports 16-bit memory systems */											\
/*TODO*///	if (type == TYPE_8BIT)																\
/*TODO*///		printf("Unsupported type for WRITEWORD macro!\n");								\
/*TODO*///																						\
/*TODO*///	/* handle aligned case first */														\
/*TODO*///	if (align == ALWAYS_ALIGNED || !(address & 1))										\
/*TODO*///	{																					\
/*TODO*///		/* first-level lookup */														\
/*TODO*///		hw = cur_mwhard[(UINT32)address >> (ABITS2_##abits + ABITS_MIN_##abits)];		\
/*TODO*///		if (hw <= HT_BANKMAX)															\
/*TODO*///		{																				\
/*TODO*///			WRITE_WORD(&cpu_bankbase[hw][address - memorywriteoffset[hw]], data);		\
/*TODO*///			return;																		\
/*TODO*///		}																				\
/*TODO*///																						\
/*TODO*///		/* second-level lookup */														\
/*TODO*///		if (hw >= MH_HARDMAX)															\
/*TODO*///		{																				\
/*TODO*///			hw -= MH_HARDMAX;															\
/*TODO*///			hw = writehardware[(hw << MH_SBITS) + (((UINT32)address >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))]; \
/*TODO*///			if (hw <= HT_BANKMAX)														\
/*TODO*///			{																			\
/*TODO*///				WRITE_WORD(&cpu_bankbase[hw][address - memorywriteoffset[hw]], data);	\
/*TODO*///				return;																	\
/*TODO*///			}																			\
/*TODO*///		}																				\
/*TODO*///																						\
/*TODO*///		/* fall back to handler */														\
/*TODO*///		(*memorywritehandler[hw])(address - memorywriteoffset[hw], data & 0xffff);		\
/*TODO*///	}																					\
/*TODO*///																						\
/*TODO*///	/* unaligned case */																\
/*TODO*///	else if (type == TYPE_16BIT_BE)														\
/*TODO*///	{																					\
/*TODO*///		name(address, data >> 8);														\
/*TODO*///		name(address + 1, data & 0xff);													\
/*TODO*///	}																					\
/*TODO*///	else if (type == TYPE_16BIT_LE)														\
/*TODO*///	{																					\
/*TODO*///		name(address, data & 0xff);														\
/*TODO*///		name(address + 1, data >> 8);													\
/*TODO*///	}																					\
/*TODO*///}
/*TODO*///
/*TODO*////* generic dword-sized write handler (16-bit aligned only!) */
/*TODO*///#define WRITELONG(name,type,abits,align)												\
/*TODO*///void name##_dword(int address, int data)												\
/*TODO*///{																						\
/*TODO*///	UINT16 word1, word2;																\
/*TODO*///	MHELE hw1, hw2;																		\
/*TODO*///																						\
/*TODO*///	/* only supports 16-bit memory systems */											\
/*TODO*///	if (type == TYPE_8BIT)																\
/*TODO*///		printf("Unsupported type for WRITEWORD macro!\n");								\
/*TODO*///																						\
/*TODO*///	/* handle aligned case first */														\
/*TODO*///	if (align == ALWAYS_ALIGNED || !(address & 1))										\
/*TODO*///	{																					\
/*TODO*///		int address2 = (address + 2) & ADDRESS_MASK(abits);								\
/*TODO*///																						\
/*TODO*///		/* first-level lookup */														\
/*TODO*///		hw1 = cur_mwhard[(UINT32)address >> (ABITS2_##abits + ABITS_MIN_##abits)];		\
/*TODO*///		hw2 = cur_mwhard[(UINT32)address2 >> (ABITS2_##abits + ABITS_MIN_##abits)];		\
/*TODO*///																						\
/*TODO*///		/* second-level lookup */														\
/*TODO*///		if (hw1 >= MH_HARDMAX)															\
/*TODO*///		{																				\
/*TODO*///			hw1 -= MH_HARDMAX;															\
/*TODO*///			hw1 = writehardware[(hw1 << MH_SBITS) + (((UINT32)address >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))];	\
/*TODO*///		}																				\
/*TODO*///		if (hw2 >= MH_HARDMAX)															\
/*TODO*///		{																				\
/*TODO*///			hw2 -= MH_HARDMAX;															\
/*TODO*///			hw2 = writehardware[(hw2 << MH_SBITS) + (((UINT32)address2 >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))];	\
/*TODO*///		}																				\
/*TODO*///																						\
/*TODO*///		/* extract words */																\
/*TODO*///		if (type == TYPE_16BIT_BE)														\
/*TODO*///		{																				\
/*TODO*///			word1 = data >> 16;															\
/*TODO*///			word2 = data & 0xffff;														\
/*TODO*///		}																				\
/*TODO*///		else if (type == TYPE_16BIT_LE)													\
/*TODO*///		{																				\
/*TODO*///			word1 = data & 0xffff;														\
/*TODO*///			word2 = data >> 16;															\
/*TODO*///		}																				\
/*TODO*///																						\
/*TODO*///		/* process each word */															\
/*TODO*///		if (hw1 <= HT_BANKMAX)															\
/*TODO*///			WRITE_WORD(&cpu_bankbase[hw1][address - memorywriteoffset[hw1]], word1);	\
/*TODO*///		else																			\
/*TODO*///			(*memorywritehandler[hw1])(address - memorywriteoffset[hw1], word1);		\
/*TODO*///		if (hw2 <= HT_BANKMAX)															\
/*TODO*///			WRITE_WORD(&cpu_bankbase[hw2][address2 - memorywriteoffset[hw2]], word2);	\
/*TODO*///		else																			\
/*TODO*///			(*memorywritehandler[hw2])(address2 - memorywriteoffset[hw2], word2);		\
/*TODO*///	}																					\
/*TODO*///																						\
/*TODO*///	/* unaligned case */																\
/*TODO*///	else if (type == TYPE_16BIT_BE)														\
/*TODO*///	{																					\
/*TODO*///		name(address, data >> 24);														\
/*TODO*///		name##_word(address + 1, (data >> 8) & 0xffff);									\
/*TODO*///		name(address + 3, data & 0xff);													\
/*TODO*///	}																					\
/*TODO*///	else if (type == TYPE_16BIT_LE)														\
/*TODO*///	{																					\
/*TODO*///		name(address, data & 0xff);														\
/*TODO*///		name##_word(address + 1, (data >> 8) & 0xffff);									\
/*TODO*///		name(address + 3, data >> 24);													\
/*TODO*///	}																					\
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////* the handlers we need to generate */
/*TODO*///WRITEBYTE(cpu_writemem16,    TYPE_8BIT,     16)
/*TODO*///WRITEBYTE(cpu_writemem20,    TYPE_8BIT,     20)
/*TODO*///WRITEBYTE(cpu_writemem21,    TYPE_8BIT,     21)
/*TODO*///
/*TODO*///WRITEBYTE(cpu_writemem16bew, TYPE_16BIT_BE, 16BEW)
/*TODO*///WRITEWORD(cpu_writemem16bew, TYPE_16BIT_BE, 16BEW, ALWAYS_ALIGNED)
/*TODO*///
/*TODO*///WRITEBYTE(cpu_writemem16lew, TYPE_16BIT_LE, 16LEW)
/*TODO*///WRITEWORD(cpu_writemem16lew, TYPE_16BIT_LE, 16LEW, ALWAYS_ALIGNED)
/*TODO*///
/*TODO*///WRITEBYTE(cpu_writemem24,    TYPE_16BIT_BE, 24)
/*TODO*///WRITEWORD(cpu_writemem24,    TYPE_16BIT_BE, 24,    CAN_BE_MISALIGNED)
/*TODO*///WRITELONG(cpu_writemem24,    TYPE_16BIT_BE, 24,    CAN_BE_MISALIGNED)
/*TODO*///
/*TODO*///WRITEBYTE(cpu_writemem29,    TYPE_16BIT_LE, 29)
/*TODO*///WRITEWORD(cpu_writemem29,    TYPE_16BIT_LE, 29,    CAN_BE_MISALIGNED)
/*TODO*///WRITELONG(cpu_writemem29,    TYPE_16BIT_LE, 29,    CAN_BE_MISALIGNED)
/*TODO*///
/*TODO*///WRITEBYTE(cpu_writemem32,    TYPE_16BIT_BE, 32)
/*TODO*///WRITEWORD(cpu_writemem32,    TYPE_16BIT_BE, 32,    CAN_BE_MISALIGNED)
/*TODO*///WRITELONG(cpu_writemem32,    TYPE_16BIT_BE, 32,    CAN_BE_MISALIGNED)
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Opcode base changers. This function is called by the CPU emulation.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*////* generic opcode base changer */
/*TODO*///#define SETOPBASE(name,abits,shift)														\
/*TODO*///void name(int pc)																		\
/*TODO*///{																						\
/*TODO*///	MHELE hw;																			\
/*TODO*///																						\
/*TODO*///	pc = (UINT32)pc >> shift;															\
/*TODO*///																						\
/*TODO*///	/* allow overrides */																\
/*TODO*///	if (OPbasefunc)																		\
/*TODO*///	{																					\
/*TODO*///		pc = OPbasefunc(pc);															\
/*TODO*///		if (pc == -1)																	\
/*TODO*///			return;																		\
/*TODO*///	}																					\
/*TODO*///																						\
/*TODO*///	/* perform the lookup */															\
/*TODO*///	hw = cur_mrhard[(UINT32)pc >> (ABITS2_##abits + ABITS_MIN_##abits)];				\
/*TODO*///	if (hw >= MH_HARDMAX)																\
/*TODO*///	{																					\
/*TODO*///		hw -= MH_HARDMAX;																\
/*TODO*///		hw = readhardware[(hw << MH_SBITS) + (((UINT32)pc >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))];	\
/*TODO*///	}																					\
/*TODO*///	ophw = hw;																			\
/*TODO*///																						\
/*TODO*///	/* RAM or banked memory */															\
/*TODO*///	if (hw <= HT_BANKMAX)																\
/*TODO*///	{																					\
/*TODO*///		SET_OP_RAMROM(cpu_bankbase[hw] - memoryreadoffset[hw])							\
/*TODO*///		return;																			\
/*TODO*///	}																					\
/*TODO*///																						\
/*TODO*///	/* do not support on callback memory region */										\
/*TODO*///	if (errorlog)																		\
/*TODO*///		fprintf(errorlog, "CPU #%d PC %04x: warning - op-code execute on mapped i/o\n",	\
/*TODO*///					cpu_getactivecpu(),cpu_get_pc());									\
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////* the handlers we need to generate */
/*TODO*///SETOPBASE(cpu_setOPbase16,    16,    0)
/*TODO*///SETOPBASE(cpu_setOPbase16bew, 16BEW, 0)
/*TODO*///SETOPBASE(cpu_setOPbase16lew, 16LEW, 0)
/*TODO*///SETOPBASE(cpu_setOPbase20,    20,    0)
/*TODO*///SETOPBASE(cpu_setOPbase21,    21,    0)
/*TODO*///SETOPBASE(cpu_setOPbase24,    24,    0)
/*TODO*///SETOPBASE(cpu_setOPbase29,    29,    3)
/*TODO*///SETOPBASE(cpu_setOPbase32,    32,    0)
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Perform an I/O port read. This function is called by the CPU emulation.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///int cpu_readport(int port)
/*TODO*///{
/*TODO*///	const struct IOReadPort *iorp = cur_readport;
/*TODO*///
/*TODO*///	port &= cur_portmask;
/*TODO*///
/*TODO*///	/* search the handlers. The order is as follows: first the dynamically installed
/*TODO*///	   handlers are searched, followed by the static ones in whatever order they were
/*TODO*///	   specified in the driver */
/*TODO*///	while (iorp->start != -1)
/*TODO*///	{
/*TODO*///		if (port >= iorp->start && port <= iorp->end)
/*TODO*///		{
/*TODO*///			mem_read_handler handler = iorp->handler;
/*TODO*///
/*TODO*///
/*TODO*///			if (handler == IORP_NOP) return 0;
/*TODO*///			else return (*handler)(port - iorp->start);
/*TODO*///		}
/*TODO*///
/*TODO*///		iorp++;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (errorlog) fprintf(errorlog,"CPU #%d PC %04x: warning - read unmapped I/O port %02x\n",cpu_getactivecpu(),cpu_get_pc(),port);
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Perform an I/O port write. This function is called by the CPU emulation.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///void cpu_writeport(int port, int value)
/*TODO*///{
/*TODO*///	const struct IOWritePort *iowp = cur_writeport;
/*TODO*///
/*TODO*///	port &= cur_portmask;
/*TODO*///
/*TODO*///	/* search the handlers. The order is as follows: first the dynamically installed
/*TODO*///	   handlers are searched, followed by the static ones in whatever order they were
/*TODO*///	   specified in the driver */
/*TODO*///	while (iowp->start != -1)
/*TODO*///	{
/*TODO*///		if (port >= iowp->start && port <= iowp->end)
/*TODO*///		{
/*TODO*///			mem_write_handler handler = iowp->handler;
/*TODO*///
/*TODO*///
/*TODO*///			if (handler == IOWP_NOP) return;
/*TODO*///			else (*handler)(port - iowp->start,value);
/*TODO*///
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		iowp++;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (errorlog) fprintf(errorlog,"CPU #%d PC %04x: warning - write %02x to unmapped I/O port %02x\n",cpu_getactivecpu(),cpu_get_pc(),value,port);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////* set readmemory handler for bank memory  */
/*TODO*///void cpu_setbankhandler_r(int bank, mem_read_handler handler)
/*TODO*///{
/*TODO*///	int offset = 0;
/*TODO*///	MHELE hardware;
/*TODO*///
/*TODO*///	switch( (FPTR)handler )
/*TODO*///	{
/*TODO*///	case (FPTR)MRA_RAM:
/*TODO*///	case (FPTR)MRA_ROM:
/*TODO*///		handler = mrh_ram;
/*TODO*///		break;
/*TODO*///	case (FPTR)MRA_BANK1:
/*TODO*///	case (FPTR)MRA_BANK2:
/*TODO*///	case (FPTR)MRA_BANK3:
/*TODO*///	case (FPTR)MRA_BANK4:
/*TODO*///	case (FPTR)MRA_BANK5:
/*TODO*///	case (FPTR)MRA_BANK6:
/*TODO*///	case (FPTR)MRA_BANK7:
/*TODO*///	case (FPTR)MRA_BANK8:
/*TODO*///	case (FPTR)MRA_BANK9:
/*TODO*///	case (FPTR)MRA_BANK10:
/*TODO*///	case (FPTR)MRA_BANK11:
/*TODO*///	case (FPTR)MRA_BANK12:
/*TODO*///	case (FPTR)MRA_BANK13:
/*TODO*///	case (FPTR)MRA_BANK14:
/*TODO*///	case (FPTR)MRA_BANK15:
/*TODO*///	case (FPTR)MRA_BANK16:
/*TODO*///		hardware = (int)MWA_BANK1 - (int)handler + 1;
/*TODO*///		handler = bank_read_handler[hardware];
/*TODO*///		offset = bankreadoffset[hardware];
/*TODO*///		break;
/*TODO*///	case (FPTR)MRA_NOP:
/*TODO*///		handler = mrh_nop;
/*TODO*///		break;
/*TODO*///	default:
/*TODO*///		offset = bankreadoffset[bank];
/*TODO*///		break;
/*TODO*///	}
/*TODO*///	memoryreadoffset[bank] = offset;
/*TODO*///	memoryreadhandler[bank] = handler;
/*TODO*///}
/*TODO*///
/*TODO*////* set writememory handler for bank memory  */
/*TODO*///void cpu_setbankhandler_w(int bank, mem_write_handler handler)
/*TODO*///{
/*TODO*///	int offset = 0;
/*TODO*///	MHELE hardware;
/*TODO*///
/*TODO*///	switch( (FPTR)handler )
/*TODO*///	{
/*TODO*///	case (FPTR)MWA_RAM:
/*TODO*///		handler = mwh_ram;
/*TODO*///		break;
/*TODO*///	case (FPTR)MWA_BANK1:
/*TODO*///	case (FPTR)MWA_BANK2:
/*TODO*///	case (FPTR)MWA_BANK3:
/*TODO*///	case (FPTR)MWA_BANK4:
/*TODO*///	case (FPTR)MWA_BANK5:
/*TODO*///	case (FPTR)MWA_BANK6:
/*TODO*///	case (FPTR)MWA_BANK7:
/*TODO*///	case (FPTR)MWA_BANK8:
/*TODO*///	case (FPTR)MWA_BANK9:
/*TODO*///	case (FPTR)MWA_BANK10:
/*TODO*///	case (FPTR)MWA_BANK11:
/*TODO*///	case (FPTR)MWA_BANK12:
/*TODO*///	case (FPTR)MWA_BANK13:
/*TODO*///	case (FPTR)MWA_BANK14:
/*TODO*///	case (FPTR)MWA_BANK15:
/*TODO*///	case (FPTR)MWA_BANK16:
/*TODO*///		hardware = (int)MWA_BANK1 - (int)handler + 1;
/*TODO*///		handler = bank_write_handler[hardware];
/*TODO*///		offset = bankwriteoffset[hardware];
/*TODO*///		break;
/*TODO*///	case (FPTR)MWA_NOP:
/*TODO*///		handler = mwh_nop;
/*TODO*///		break;
/*TODO*///	case (FPTR)MWA_RAMROM:
/*TODO*///		handler = mwh_ramrom;
/*TODO*///		break;
/*TODO*///	case (FPTR)MWA_ROM:
/*TODO*///		handler = mwh_rom;
/*TODO*///		break;
/*TODO*///	default:
/*TODO*///		offset = bankwriteoffset[bank];
/*TODO*///		break;
/*TODO*///	}
/*TODO*///	memorywriteoffset[bank] = offset;
/*TODO*///	memorywritehandler[bank] = handler;
/*TODO*///}
/*TODO*///
/*TODO*////* cpu change op-code memory base */
/*TODO*///void cpu_setOPbaseoverride (int cpu,opbase_handler function)
/*TODO*///{
/*TODO*///	setOPbasefunc[cpu] = function;
/*TODO*///	if (cpu == cpu_getactivecpu())
/*TODO*///		OPbasefunc = function;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void *install_mem_read_handler(int cpu, int start, int end, mem_read_handler handler)
/*TODO*///{
/*TODO*///	MHELE hardware = 0;
/*TODO*///	int abitsmin;
/*TODO*///	int i, hw_set;
/*TODO*///	if (errorlog) fprintf(errorlog, "Install new memory read handler:\n");
/*TODO*///	if (errorlog) fprintf(errorlog, "             cpu: %d\n", cpu);
/*TODO*///	if (errorlog) fprintf(errorlog, "           start: 0x%08x\n", start);
/*TODO*///	if (errorlog) fprintf(errorlog, "             end: 0x%08x\n", end);
/*TODO*///#ifdef __LP64__
/*TODO*///	if (errorlog) fprintf(errorlog, " handler address: 0x%016lx\n", (unsigned long) handler);
/*TODO*///#else
/*TODO*///	if (errorlog) fprintf(errorlog, " handler address: 0x%08x\n", (unsigned int) handler);
/*TODO*///#endif
/*TODO*///	abitsmin = ABITSMIN (cpu);
/*TODO*///
/*TODO*///	/* see if this function is already registered */
/*TODO*///	hw_set = 0;
/*TODO*///	for ( i = 0 ; i < MH_HARDMAX ; i++)
/*TODO*///	{
/*TODO*///		/* record it if it matches */
/*TODO*///		if (( memoryreadhandler[i] == handler ) &&
/*TODO*///			(  memoryreadoffset[i] == start))
/*TODO*///		{
/*TODO*///			if (errorlog) fprintf(errorlog,"handler match - use old one\n");
/*TODO*///			hardware = i;
/*TODO*///			hw_set = 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	switch ((FPTR)handler)
/*TODO*///	{
/*TODO*///		case (FPTR)MRA_RAM:
/*TODO*///		case (FPTR)MRA_ROM:
/*TODO*///			hardware = HT_RAM;	/* sprcial case ram read */
/*TODO*///			hw_set = 1;
/*TODO*///			break;
/*TODO*///		case (FPTR)MRA_BANK1:
/*TODO*///		case (FPTR)MRA_BANK2:
/*TODO*///		case (FPTR)MRA_BANK3:
/*TODO*///		case (FPTR)MRA_BANK4:
/*TODO*///		case (FPTR)MRA_BANK5:
/*TODO*///		case (FPTR)MRA_BANK6:
/*TODO*///		case (FPTR)MRA_BANK7:
/*TODO*///		case (FPTR)MRA_BANK8:
/*TODO*///		case (FPTR)MRA_BANK9:
/*TODO*///		case (FPTR)MRA_BANK10:
/*TODO*///		case (FPTR)MRA_BANK11:
/*TODO*///		case (FPTR)MRA_BANK12:
/*TODO*///		case (FPTR)MRA_BANK13:
/*TODO*///		case (FPTR)MRA_BANK14:
/*TODO*///		case (FPTR)MRA_BANK15:
/*TODO*///		case (FPTR)MRA_BANK16:
/*TODO*///		{
/*TODO*///			hardware = (int)MRA_BANK1 - (int)handler + 1;
/*TODO*///			hw_set = 1;
/*TODO*///			break;
/*TODO*///		}
/*TODO*///		case (FPTR)MRA_NOP:
/*TODO*///			hardware = HT_NOP;
/*TODO*///			hw_set = 1;
/*TODO*///			break;
/*TODO*///	}
/*TODO*///	if (!hw_set)  /* no match */
/*TODO*///	{
/*TODO*///		/* create newer hardware handler */
/*TODO*///		if( rdhard_max == MH_HARDMAX )
/*TODO*///		{
/*TODO*///			if (errorlog) fprintf(errorlog, "read memory hardware pattern over !\n");
/*TODO*///			if (errorlog) fprintf(errorlog, "Failed to install new memory handler.\n");
/*TODO*///			return memory_find_base(cpu, start);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			/* register hardware function */
/*TODO*///			hardware = rdhard_max++;
/*TODO*///			memoryreadhandler[hardware] = handler;
/*TODO*///			memoryreadoffset[hardware] = start;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	/* set hardware element table entry */
/*TODO*///	set_element( cpu , cur_mr_element[cpu] ,
/*TODO*///		(((unsigned int) start) >> abitsmin) ,
/*TODO*///		(((unsigned int) end) >> abitsmin) ,
/*TODO*///		hardware , readhardware , &rdelement_max );
/*TODO*///	if (errorlog) fprintf(errorlog, "Done installing new memory handler.\n");
/*TODO*///	if (errorlog){
/*TODO*///		fprintf(errorlog,"used read  elements %d/%d , functions %d/%d\n"
/*TODO*///		    ,rdelement_max,MH_ELEMAX , rdhard_max,MH_HARDMAX );
/*TODO*///	}
/*TODO*///	return memory_find_base(cpu, start);
/*TODO*///}
/*TODO*///
/*TODO*///void *install_mem_write_handler(int cpu, int start, int end, mem_write_handler handler)
/*TODO*///{
/*TODO*///	MHELE hardware = 0;
/*TODO*///	int abitsmin;
/*TODO*///	int i, hw_set;
/*TODO*///	if (errorlog) fprintf(errorlog, "Install new memory write handler:\n");
/*TODO*///	if (errorlog) fprintf(errorlog, "             cpu: %d\n", cpu);
/*TODO*///	if (errorlog) fprintf(errorlog, "           start: 0x%08x\n", start);
/*TODO*///	if (errorlog) fprintf(errorlog, "             end: 0x%08x\n", end);
/*TODO*///#ifdef __LP64__
/*TODO*///	if (errorlog) fprintf(errorlog, " handler address: 0x%016lx\n", (unsigned long) handler);
/*TODO*///#else
/*TODO*///	if (errorlog) fprintf(errorlog, " handler address: 0x%08x\n", (unsigned int) handler);
/*TODO*///#endif
/*TODO*///	abitsmin = ABITSMIN (cpu);
/*TODO*///
/*TODO*///	/* see if this function is already registered */
/*TODO*///	hw_set = 0;
/*TODO*///	for ( i = 0 ; i < MH_HARDMAX ; i++)
/*TODO*///	{
/*TODO*///		/* record it if it matches */
/*TODO*///		if (( memorywritehandler[i] == handler ) &&
/*TODO*///			(  memorywriteoffset[i] == start))
/*TODO*///		{
/*TODO*///			if (errorlog) fprintf(errorlog,"handler match - use old one\n");
/*TODO*///			hardware = i;
/*TODO*///			hw_set = 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	switch( (FPTR)handler )
/*TODO*///	{
/*TODO*///		case (FPTR)MWA_RAM:
/*TODO*///			hardware = HT_RAM;	/* sprcial case ram write */
/*TODO*///			hw_set = 1;
/*TODO*///			break;
/*TODO*///		case (FPTR)MWA_BANK1:
/*TODO*///		case (FPTR)MWA_BANK2:
/*TODO*///		case (FPTR)MWA_BANK3:
/*TODO*///		case (FPTR)MWA_BANK4:
/*TODO*///		case (FPTR)MWA_BANK5:
/*TODO*///		case (FPTR)MWA_BANK6:
/*TODO*///		case (FPTR)MWA_BANK7:
/*TODO*///		case (FPTR)MWA_BANK8:
/*TODO*///		case (FPTR)MWA_BANK9:
/*TODO*///		case (FPTR)MWA_BANK10:
/*TODO*///		case (FPTR)MWA_BANK11:
/*TODO*///		case (FPTR)MWA_BANK12:
/*TODO*///		case (FPTR)MWA_BANK13:
/*TODO*///		case (FPTR)MWA_BANK14:
/*TODO*///		case (FPTR)MWA_BANK15:
/*TODO*///		case (FPTR)MWA_BANK16:
/*TODO*///		{
/*TODO*///			hardware = (int)MWA_BANK1 - (int)handler + 1;
/*TODO*///			hw_set = 1;
/*TODO*///			break;
/*TODO*///		}
/*TODO*///		case (FPTR)MWA_NOP:
/*TODO*///			hardware = HT_NOP;
/*TODO*///			hw_set = 1;
/*TODO*///			break;
/*TODO*///		case (FPTR)MWA_RAMROM:
/*TODO*///			hardware = HT_RAMROM;
/*TODO*///			hw_set = 1;
/*TODO*///			break;
/*TODO*///		case (FPTR)MWA_ROM:
/*TODO*///			hardware = HT_ROM;
/*TODO*///			hw_set = 1;
/*TODO*///			break;
/*TODO*///	}
/*TODO*///	if (!hw_set)  /* no match */
/*TODO*///	{
/*TODO*///		/* create newer hardware handler */
/*TODO*///		if( wrhard_max == MH_HARDMAX )
/*TODO*///		{
/*TODO*///			if (errorlog) fprintf(errorlog, "write memory hardware pattern over !\n");
/*TODO*///			if (errorlog) fprintf(errorlog, "Failed to install new memory handler.\n");
/*TODO*///
/*TODO*///			return memory_find_base(cpu, start);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			/* register hardware function */
/*TODO*///			hardware = wrhard_max++;
/*TODO*///			memorywritehandler[hardware] = handler;
/*TODO*///			memorywriteoffset[hardware] = start;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	/* set hardware element table entry */
/*TODO*///	set_element( cpu , cur_mw_element[cpu] ,
/*TODO*///		(((unsigned int) start) >> abitsmin) ,
/*TODO*///		(((unsigned int) end) >> abitsmin) ,
/*TODO*///		hardware , writehardware , &wrelement_max );
/*TODO*///	if (errorlog) fprintf(errorlog, "Done installing new memory handler.\n");
/*TODO*///	if (errorlog){
/*TODO*///		fprintf(errorlog,"used write elements %d/%d , functions %d/%d\n"
/*TODO*///		    ,wrelement_max,MH_ELEMAX , wrhard_max,MH_HARDMAX );
/*TODO*///	}
/*TODO*///	return memory_find_base(cpu, start);
/*TODO*///}
/*TODO*///
    public static IOReadPort[] install_port_read_handler(int cpu, int start, int end, ReadHandlerPtr _handler) {
        return install_port_read_handler_common(cpu, start, end, _handler, 1);
    }

    public static IOWritePort[] install_port_write_handler(int cpu, int start, int end, WriteHandlerPtr _handler) {
        return install_port_write_handler_common(cpu, start, end, _handler, 1);
    }

    public static IOReadPort[] install_port_read_handler_common(int cpu, int start, int end, ReadHandlerPtr _handler, int install_at_beginning) {
        int i, oldsize;

        oldsize = readport_size[cpu];
        readport_size[cpu]++;// += sizeof(struct IOReadPort);

        if (readport[cpu] == null) {
            readport[cpu] = new IOReadPort[readport_size[cpu]];
        } else {
            IOReadPort[] old_readport = readport[cpu];
            IOReadPort[] temp = Arrays.copyOf(readport[cpu], readport_size[cpu]);
            readport[cpu] = temp; //realloc(readport[cpu], readport_size[cpu]);

            /* check if we're changing the current readport and ifso update it */
            if (cur_readport == old_readport) {
                cur_readport = readport[cpu];
            }

            /* realloc leaves the old buffer intact if it fails, so free it */
            if (readport[cpu] == null) {
                old_readport = null;
            }
        }

        if (readport[cpu] == null) {
            return null;
        }

        if (install_at_beginning != 0) {
            /* can't do a single memcpy because it doesn't handle overlapping regions correctly??? */
            for (i = oldsize; i >= 1; i--)//for (i = oldsize / sizeof(struct IOReadPort); i >= 1; i--)
            {
                System.arraycopy(readport[cpu], i - 1, readport[cpu], i, 1);//memcpy(&readport[cpu][i], &readport[cpu][i - 1], sizeof(struct IOReadPort));
            }

            i = 0;
        } else {
            i = oldsize;//i = oldsize / sizeof(struct IOReadPort);
        }
        if (MEM_DUMP) {
            if (errorlog != null) {
                fprintf(errorlog, "Installing port read handler: cpu %d  slot %X  start %X  end %X\n", cpu, i, start, end);
            }
        }

        readport[cpu][i] = new IOReadPort();
        readport[cpu][i].start = start;
        readport[cpu][i].end = end;
        readport[cpu][i]._handler = _handler;

        return readport[cpu];
    }

    public static IOWritePort[] install_port_write_handler_common(int cpu, int start, int end, WriteHandlerPtr _handler, int install_at_beginning) {
        int i, oldsize;

        oldsize = writeport_size[cpu];
        writeport_size[cpu]++;// += sizeof(struct IOWritePort);

        if (writeport[cpu] == null) {
            writeport[cpu] = new IOWritePort[writeport_size[cpu]];
        } else {
            IOWritePort[] old_writeport = writeport[cpu];

            IOWritePort[] temp = Arrays.copyOf(writeport[cpu], writeport_size[cpu]);
            writeport[cpu] = temp;//realloc(writeport[cpu], writeport_size[cpu]);

            /* check if we're changing the current writeport and ifso update it */
            if (cur_writeport == old_writeport) {
                cur_writeport = writeport[cpu];
            }

            /* realloc leaves the old buffer intact if it fails, so free it */
            if (writeport[cpu] == null) {
                old_writeport = null;
            }
        }

        if (writeport[cpu] == null) {
            return null;
        }

        if (install_at_beginning != 0) {
            /* can't do a single memcpy because it doesn't handle overlapping regions correctly??? */
            for (i = oldsize; i >= 1; i--)//for (i = oldsize / sizeof(struct IOWritePort); i >= 1; i--)
            {
                System.arraycopy(writeport[cpu], i - 1, writeport[cpu], i, 1);//memcpy(&writeport[cpu][i], &writeport[cpu][i - 1], sizeof(struct IOWritePort));
            }

            i = 0;
        } else {
            i = oldsize;// / sizeof(struct IOWritePort);
        }

        if (MEM_DUMP) {
            if (errorlog != null) {
                fprintf(errorlog, "Installing port write handler: cpu %d  slot %X  start %X  end %X\n", cpu, i, start, end);
            }
        }
        writeport[cpu][i] = new IOWritePort();
        writeport[cpu][i].start = start;
        writeport[cpu][i].end = end;
        writeport[cpu][i]._handler = _handler;

        return writeport[cpu];
    }

    public static void mem_dump() {
        int cpu;
        int naddr, addr;
        char u8_nhw, u8_hw;

        FILE temp = fopen("memdump.log", "w");

        if (temp == null) {
            return;
        }

        for (cpu = 0; cpu < cpu_gettotalcpu(); cpu++) {
            fprintf(temp, "cpu %d read memory \n", cpu);
            addr = 0;
            naddr = 0;
            u8_nhw = 0xff;
            while ((addr >> mhshift[cpu][0]) <= mhmask[cpu][0]) {
                u8_hw = u8_cur_mr_element[cpu][addr >> mhshift[cpu][0]];
                if (u8_hw >= MH_HARDMAX) {
                    /* 2nd element link */
                    u8_hw = u8_readhardware[((u8_hw - MH_HARDMAX) << MH_SBITS) + ((addr >> mhshift[cpu][1]) & mhmask[cpu][1])];
                    if (u8_hw >= MH_HARDMAX) {
                        u8_hw = u8_readhardware[((u8_hw - MH_HARDMAX) << MH_SBITS) + (addr & mhmask[cpu][2])];
                    }
                }
                if (u8_nhw != u8_hw) {
                    if (addr != 0) {
                        fprintf(temp, "  %08x(%08x) - %08x = %02x\n", naddr, memoryreadoffset[u8_nhw], addr - 1, (int) u8_nhw);
                    }
                    u8_nhw = u8_hw;
                    naddr = addr;
                }
                addr++;
            }
            fprintf(temp, "  %08x(%08x) - %08x = %02x\n", naddr, memoryreadoffset[u8_nhw], addr - 1, (int) u8_nhw);

            fprintf(temp, "cpu %d write memory \n", cpu);
            naddr = 0;
            addr = 0;
            u8_nhw = 0xff;
            while ((addr >> mhshift[cpu][0]) <= mhmask[cpu][0]) {
                u8_hw = u8_cur_mw_element[cpu][addr >> mhshift[cpu][0]];
                if (u8_hw >= MH_HARDMAX) {
                    /* 2nd element link */
                    u8_hw = u8_writehardware[((u8_hw - MH_HARDMAX) << MH_SBITS) + ((addr >> mhshift[cpu][1]) & mhmask[cpu][1])];
                    if (u8_hw >= MH_HARDMAX) {
                        u8_hw = u8_writehardware[((u8_hw - MH_HARDMAX) << MH_SBITS) + (addr & mhmask[cpu][2])];
                    }
                }
                if (u8_nhw != u8_hw) {
                    if (addr != 0) {
                        fprintf(temp, "  %08x(%08x) - %08x = %02x\n", naddr, memorywriteoffset[u8_nhw], addr - 1, (int) u8_nhw);
                    }
                    u8_nhw = u8_hw;
                    naddr = addr;
                }
                addr++;
            }
            fprintf(temp, "  %08x(%08x) - %08x = %02x\n", naddr, memorywriteoffset[u8_nhw], addr - 1, (int) u8_nhw);
        }
        fclose(temp);
    }
}
