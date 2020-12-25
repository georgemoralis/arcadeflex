/**
 * ported to 0.37b7
 */
package gr.codebb.arcadeflex.v037b7.mame;

import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import gr.codebb.arcadeflex.v036.platform.libc_old.FILE;
import static gr.codebb.arcadeflex.v036.platform.libc_old.fclose;
import static gr.codebb.arcadeflex.v036.platform.libc_old.fopen;
import static gr.codebb.arcadeflex.v036.platform.libc_old.fprintf;
import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.printf;
import gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.*;
import java.util.Arrays;


public class memory {

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
    public static char u8_ophw;
    /* op-code hardware number */

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
    public static final int HT_RAM = 0;
    /* RAM direct		 */
    static final int HT_BANK1 = 1;
    /* bank memory #1	 */
    static final int HT_BANK2 = 2;
    /* bank memory #2	 */
    static final int HT_BANK3 = 3;
    /* bank memory #3	 */
    static final int HT_BANK4 = 4;
    /* bank memory #4	 */
    static final int HT_BANK5 = 5;
    /* bank memory #5	 */
    static final int HT_BANK6 = 6;
    /* bank memory #6	 */
    static final int HT_BANK7 = 7;
    /* bank memory #7	 */
    static final int HT_BANK8 = 8;
    /* bank memory #8	 */
    static final int HT_BANK9 = 9;
    /* bank memory #9	 */
    static final int HT_BANK10 = 10;
    /* bank memory #10	 */
    static final int HT_BANK11 = 11;
    /* bank memory #11	 */
    static final int HT_BANK12 = 12;
    /* bank memory #12	 */
    static final int HT_BANK13 = 13;
    /* bank memory #13	 */
    static final int HT_BANK14 = 14;
    /* bank memory #14	 */
    static final int HT_BANK15 = 15;
    /* bank memory #15	 */
    static final int HT_BANK16 = 16;
    /* bank memory #16	 */
    static final int HT_NON = 17;
    /* non mapped memory */
    static final int HT_NOP = 18;
    /* NOP memory		 */
    static final int HT_RAMROM = 19;
    /* RAM ROM memory	 */
    static final int HT_ROM = 20;
    /* ROM memory		 */

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
            logerror("CPU #%d PC %04x: warning - read %02x from unmapped memory address %04x\n", cpu_getactivecpu(), cpu_get_pc(), (int) cpu_bankbase[0].read(offset), offset);
            return cpu_bankbase[0].read(offset);
        }
    };

    public static ReadHandlerPtr mrh_error_sparse = new ReadHandlerPtr() {
        public int handler(int offset) {
            logerror("CPU #%d PC %08x: warning - read unmapped memory address %08x\n", cpu_getactivecpu(), cpu_get_pc(), offset);
            return 0;
        }
    };

    public static ReadHandlerPtr mrh_error_sparse_bit = new ReadHandlerPtr() {
        public int handler(int offset) {
            logerror("CPU #%d PC %08x: warning - read unmapped memory bit addr %08x (byte addr %08x)\n", cpu_getactivecpu(), cpu_get_pc(), offset << 3, offset);
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
            logerror("CPU #%d PC %04x: warning - write %02x to unmapped memory address %04x\n", cpu_getactivecpu(), cpu_get_pc(), data, offset);
            cpu_bankbase[0].write(offset, data);
        }
    };

    public static WriteHandlerPtr mwh_error_sparse = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            logerror("CPU #%d PC %08x: warning - write %02x to unmapped memory address %08x\n", cpu_getactivecpu(), cpu_get_pc(), data, offset);
        }
    };

    public static WriteHandlerPtr mwh_error_sparse_bit = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            logerror("CPU #%d PC %08x: warning - write %02x to unmapped memory bit addr %08x\n", cpu_getactivecpu(), cpu_get_pc(), data, offset << 3);
        }
    };

    public static WriteHandlerPtr mwh_rom = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            logerror("CPU #%d PC %04x: warning - write %02x to ROM address %04x\n", cpu_getactivecpu(), cpu_get_pc(), data, offset);
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
            logerror("memory element size overflow\n");
            return null;
        }
        /* get new element nunber */
        ele = ele_max[0];
        (ele_max[0]) += banks;

        logerror("create element %2d(%2d)\n", ele, banks);

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

        logerror("set_element %8X-%8X = %2X\n", sp, ep, (int) u8_type);

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
            //const struct MemoryReadAddress *mra;
            //const struct MemoryWriteAddress *mwa;

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

        for (cpu = 0; cpu < cpu_gettotalcpu(); cpu++) {

            setOPbasefunc[cpu] = null;

            ramptr[cpu] = romptr[cpu] = memory_region(REGION_CPU1 + cpu);

            /* initialize the memory base pointers for memory hooks */
            int _mra = 0;//_mra = Machine->drv->cpu[cpu].memory_read;
            if (Machine.drv.cpu[cpu].memory_read != null && Machine.drv.cpu[cpu].memory_read[_mra] != null) {
                while (Machine.drv.cpu[cpu].memory_read[_mra].start != -1) {
                    /*				if (_mra->base) *_mra->base = memory_find_base (cpu, _mra->start); */
 /*				if (_mra->size) *_mra->size = _mra->end - _mra->start + 1; */
                    _mra++;
                }
            }
            int _mwa = 0;//_mwa = Machine->drv->cpu[cpu].memory_write;
            if (Machine.drv.cpu[cpu].memory_write != null && Machine.drv.cpu[cpu].memory_write[_mwa] != null) {
                while (Machine.drv.cpu[cpu].memory_write[_mwa].start != -1) {

                    if (Machine.drv.cpu[cpu].memory_write[_mwa].base != null) {
                        UBytePtr b = memory_find_base(cpu, Machine.drv.cpu[cpu].memory_write[_mwa].start);
                        Machine.drv.cpu[cpu].memory_write[_mwa].base.memory = b.memory;
                        Machine.drv.cpu[cpu].memory_write[_mwa].base.offset = b.offset;
                    }
                    if (Machine.drv.cpu[cpu].memory_write[_mwa].size != null) {
                        Machine.drv.cpu[cpu].memory_write[_mwa].size[0] = Machine.drv.cpu[cpu].memory_write[_mwa].end - Machine.drv.cpu[cpu].memory_write[_mwa].start + 1;
                    }
                    _mwa++;
                }
            }

            /* initialize port structures */
            readport_size[cpu] = 0;
            writeport_size[cpu] = 0;
            readport[cpu] = null;
            writeport[cpu] = null;

            /* install port handlers - at least an empty one */
            int ioread = 0;//ioread = Machine -> drv -> cpu[cpu].port_read;
            if (Machine.drv.cpu[cpu].port_read == null) {
                Machine.drv.cpu[cpu].port_read = empty_readport;
            }

            while (true) {
                if (install_port_read_handler_common(cpu, Machine.drv.cpu[cpu].port_read[ioread].start, Machine.drv.cpu[cpu].port_read[ioread].end, Machine.drv.cpu[cpu].port_read[ioread]._handler, 0) == null) {
                    memory_shutdown();
                    return 0;
                }
                if (Machine.drv.cpu[cpu].port_read[ioread].start == -1) {
                    break;
                }

                ioread++;
            }

            int iowrite = 0;//iowrite = Machine -> drv -> cpu[cpu].port_write;
            if (Machine.drv.cpu[cpu].port_write == null) {
                Machine.drv.cpu[cpu].port_write = empty_writeport;
            }

            while (true) {
                if (install_port_write_handler_common(cpu, Machine.drv.cpu[cpu].port_write[iowrite].start, Machine.drv.cpu[cpu].port_write[iowrite].end, Machine.drv.cpu[cpu].port_write[iowrite]._handler, 0) == null) {
                    memory_shutdown();
                    return 0;
                }
                if (Machine.drv.cpu[cpu].port_write[iowrite].start == -1) {
                    break;
                }

                iowrite++;
            }

            portmask[cpu] = 0xffff;
            if ((Machine.drv.cpu[cpu].cpu_type & ~CPU_FLAGS_MASK) == CPU_Z80
                    && (Machine.drv.cpu[cpu].cpu_type & CPU_16BIT_PORT) == 0) {
                portmask[cpu] = 0xff;
            }
        }

        /* initialize global handler */
        for (i = 0; i < MH_HARDMAX; i++) {
            memoryreadoffset[i] = 0;
            memorywriteoffset[i] = 0;
            memoryreadhandler[i] = null;
            memorywritehandler[i] = null;
        }
        /* bank memory */
        for (i = 1; i <= MAX_BANKS; i++) {
            memoryreadhandler[i] = bank_read_handler[i];
            memorywritehandler[i] = bank_write_handler[i];
        }
        /* non map memory */
        memoryreadhandler[HT_NON] = mrh_error;
        memorywritehandler[HT_NON] = mwh_error;
        /* NOP memory */
        memoryreadhandler[HT_NOP] = mrh_nop;
        memorywritehandler[HT_NOP] = mwh_nop;
        /* RAMROM memory */
        memorywritehandler[HT_RAMROM] = mwh_ramrom;
        /* ROM memory */
        memorywritehandler[HT_ROM] = mwh_rom;

        /* if any CPU is 21-bit or more, we change the error handlers to be more benign */
        for (cpu = 0; cpu < cpu_gettotalcpu(); cpu++) {
            if (ADDRESS_BITS(cpu) >= 21) {
                memoryreadhandler[HT_NON] = mrh_error_sparse;
                memorywritehandler[HT_NON] = mwh_error_sparse;

                if ((Machine.drv.cpu[cpu].cpu_type & ~CPU_FLAGS_MASK) == CPU_TMS34010) {
                    memoryreadhandler[HT_NON] = mrh_error_sparse_bit;
                    memorywritehandler[HT_NON] = mwh_error_sparse_bit;
                }
            }
        }

        for (cpu = 0; cpu < cpu_gettotalcpu(); cpu++) {
            /* cpu selection */
            abits1 = ABITS1(cpu);
            abits2 = ABITS2(cpu);
            abits3 = ABITS3(cpu);
            abitsmin = ABITSMIN(cpu);

            /* element shifter , mask set */
            mhshift[cpu][0] = (abits2 + abits3);
            mhshift[cpu][1] = abits3;
            /* 2nd */
            mhshift[cpu][2] = 0;
            /* 3rd (used by set_element)*/
            mhmask[cpu][0] = MHMASK(abits1);
            /*1st(used by set_element)*/
            mhmask[cpu][1] = MHMASK(abits2);
            /*2nd*/
            mhmask[cpu][2] = MHMASK(abits3);
            /*3rd*/

 /* allocate current element */
            if ((u8_cur_mr_element[cpu] = new char[1 << abits1]) == null) {
                memory_shutdown();
                return 0;
            }
            if ((u8_cur_mw_element[cpu] = new char[1 << abits1]) == null) {
                memory_shutdown();
                return 0;
            }

            /* initialize current element table */
            for (i = 0; i < (1 << abits1); i++) {
                u8_cur_mr_element[cpu][i] = HT_NON;
                /* no map memory */
                u8_cur_mw_element[cpu][i] = HT_NON;
                /* no map memory */
            }

            /* memory read handler build */
            int mra = 0;
            while (Machine.drv.cpu[cpu].memory_read[mra].start != -1) {
                mra++;
            }
            mra--;
            while (mra >= 0) { //while (mra >= memoryread)
                install_mem_read_handler(cpu, Machine.drv.cpu[cpu].memory_read[mra].start, Machine.drv.cpu[cpu].memory_read[mra].end, Machine.drv.cpu[cpu].memory_read[mra]._handler, Machine.drv.cpu[cpu].memory_read[mra].handler);
                mra--;
            }
            /* memory write handler build */
            int mwa = 0;
            while (Machine.drv.cpu[cpu].memory_write[mwa].start != -1) {
                mwa++;
            }
            mwa--;
            while (mwa >= 0) {
                install_mem_write_handler(cpu, Machine.drv.cpu[cpu].memory_write[mwa].start, Machine.drv.cpu[cpu].memory_write[mwa].end, Machine.drv.cpu[cpu].memory_write[mwa]._handler, Machine.drv.cpu[cpu].memory_write[mwa].handler);
                mwa--;
            }
        }

        logerror("used read  elements %d/%d , functions %d/%d\n",
                rdelement_max[0], MH_ELEMAX, rdhard_max, MH_HARDMAX);
        logerror("used write elements %d/%d , functions %d/%d\n",
                wrelement_max[0], MH_ELEMAX, wrhard_max, MH_HARDMAX);

        mem_dump();

        return 1;
        /* ok */
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

    public static void memory_shutdown() {
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
    }

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
/*TODO*///#define ADDRESS_MASK(abits) 		(ADDRESS_TOPBIT(abits) | (ADDRESS_TOPBIT(abits) - 1))
/*TODO*///
/*TODO*///
/*TODO*////* generic byte-sized read handler */
/*TODO*///#define READBYTE(name,type,abits)														\
/*TODO*///data_t name(offs_t address) 															\
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
/*TODO*///	else if (type != TYPE_8BIT && hw <= HT_BANKMAX) 									\
/*TODO*///	{																					\
/*TODO*///		if (type == TYPE_16BIT_BE)														\
/*TODO*///			return cpu_bankbase[hw][BYTE_XOR_BE(address) - memoryreadoffset[hw]];		\
/*TODO*///		else if (type == TYPE_16BIT_LE) 												\
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
/*TODO*///		else if (type != TYPE_8BIT && hw <= HT_BANKMAX) 								\
/*TODO*///		{																				\
/*TODO*///			if (type == TYPE_16BIT_BE)													\
/*TODO*///				return cpu_bankbase[hw][BYTE_XOR_BE(address) - memoryreadoffset[hw]];	\
/*TODO*///			else if (type == TYPE_16BIT_LE) 											\
/*TODO*///				return cpu_bankbase[hw][BYTE_XOR_LE(address) - memoryreadoffset[hw]];	\
/*TODO*///		}																				\
/*TODO*///	}																					\
/*TODO*///																						\
/*TODO*///	/* fall back to handler */															\
/*TODO*///	if (type == TYPE_8BIT)																\
/*TODO*///		return (*memoryreadhandler[hw])(address - memoryreadoffset[hw]);				\
/*TODO*///	else																				\
/*TODO*///	{																					\
/*TODO*///		int shift = (address & 1) << 3; 												\
/*TODO*///		int data = (*memoryreadhandler[hw])((address & ~1) - memoryreadoffset[hw]); 	\
/*TODO*///		if (type == TYPE_16BIT_BE)														\
/*TODO*///			return (data >> (shift ^ 8)) & 0xff;										\
/*TODO*///		else if (type == TYPE_16BIT_LE) 												\
/*TODO*///			return (data >> shift) & 0xff;												\
/*TODO*///	}																					\
/*TODO*///}
/*TODO*///
/*TODO*////* generic word-sized read handler (16-bit aligned only!) */
/*TODO*///#define READWORD(name,type,abits,align) 												\
/*TODO*///data_t name##_word(offs_t address)														\
/*TODO*///{																						\
/*TODO*///	MHELE hw;																			\
/*TODO*///																						\
/*TODO*///	/* only supports 16-bit memory systems */											\
/*TODO*///	if (type == TYPE_8BIT)																\
/*TODO*///		printf("Unsupported type for READWORD macro!\n");                               \
/*TODO*///																						\
/*TODO*///	/* handle aligned case first */ 													\
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
/*TODO*///	else if (type == TYPE_16BIT_BE) 													\
/*TODO*///	{																					\
/*TODO*///		int data = name(address) << 8;													\
/*TODO*///		return data | (name(address + 1) & 0xff);										\
/*TODO*///	}																					\
/*TODO*///	else if (type == TYPE_16BIT_LE) 													\
/*TODO*///	{																					\
/*TODO*///		int data = name(address) & 0xff;												\
/*TODO*///		return data | (name(address + 1) << 8); 										\
/*TODO*///	}																					\
/*TODO*///}
/*TODO*///
/*TODO*////* generic dword-sized read handler (16-bit aligned only!) */
/*TODO*///#define READLONG(name,type,abits,align) 												\
/*TODO*///data_t name##_dword(offs_t address) 													\
/*TODO*///{																						\
/*TODO*///	UINT16 word1, word2;																\
/*TODO*///	MHELE hw1, hw2; 																	\
/*TODO*///																						\
/*TODO*///	/* only supports 16-bit memory systems */											\
/*TODO*///	if (type == TYPE_8BIT)																\
/*TODO*///		printf("Unsupported type for READWORD macro!\n");                               \
/*TODO*///																						\
/*TODO*///	/* handle aligned case first */ 													\
/*TODO*///	if (align == ALWAYS_ALIGNED || !(address & 1))										\
/*TODO*///	{																					\
/*TODO*///		int address2 = (address + 2) & ADDRESS_MASK(abits); 							\
/*TODO*///																						\
/*TODO*///		/* first-level lookup */														\
/*TODO*///		hw1 = cur_mrhard[(UINT32)address >> (ABITS2_##abits + ABITS_MIN_##abits)];		\
/*TODO*///		hw2 = cur_mrhard[(UINT32)address2 >> (ABITS2_##abits + ABITS_MIN_##abits)]; 	\
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
/*TODO*///			hw2 = readhardware[(hw2 << MH_SBITS) + (((UINT32)address2 >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))]; \
/*TODO*///		}																				\
/*TODO*///																						\
/*TODO*///		/* process each word */ 														\
/*TODO*///		if (hw1 <= HT_BANKMAX)															\
/*TODO*///			word1 = READ_WORD(&cpu_bankbase[hw1][address - memoryreadoffset[hw1]]); 	\
/*TODO*///		else																			\
/*TODO*///			word1 = (*memoryreadhandler[hw1])(address - memoryreadoffset[hw1]); 		\
/*TODO*///		if (hw2 <= HT_BANKMAX)															\
/*TODO*///			word2 = READ_WORD(&cpu_bankbase[hw2][address2 - memoryreadoffset[hw2]]);	\
/*TODO*///		else																			\
/*TODO*///			word2 = (*memoryreadhandler[hw2])(address2 - memoryreadoffset[hw2]);		\
/*TODO*///																						\
/*TODO*///		/* fall back to handler */														\
/*TODO*///		if (type == TYPE_16BIT_BE)														\
/*TODO*///			return (word1 << 16) | (word2 & 0xffff);									\
/*TODO*///		else if (type == TYPE_16BIT_LE) 												\
/*TODO*///			return (word1 & 0xffff) | (word2 << 16);									\
/*TODO*///	}																					\
/*TODO*///																						\
/*TODO*///	/* unaligned case */																\
/*TODO*///	else if (type == TYPE_16BIT_BE) 													\
/*TODO*///	{																					\
/*TODO*///		int data = name(address) << 24; 												\
/*TODO*///		data |= name##_word(address + 1) << 8;											\
/*TODO*///		return data | (name(address + 3) & 0xff);										\
/*TODO*///	}																					\
/*TODO*///	else if (type == TYPE_16BIT_LE) 													\
/*TODO*///	{																					\
/*TODO*///		int data = name(address) & 0xff;												\
/*TODO*///		data |= name##_word(address + 1) << 8;											\
/*TODO*///		return data | (name(address + 3) << 24);										\
/*TODO*///	}																					\
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////* the handlers we need to generate */
    public static int cpu_readmem16(int address) {
        char u8_hw;

        /* first-level lookup */
        u8_hw = u8_cur_mrhard[/*(UINT32)*/address >>> (ABITS2_16 + ABITS_MIN_16)];

        /* for compatibility with setbankhandler, 8-bit systems must call handlers */
 /* for banked memory reads/writes */
        if (u8_hw == HT_RAM) {
            //return cpu_bankbase[HT_RAM][address];
            return cpu_bankbase[HT_RAM].read(address);
        }

        /* second-level lookup */
        if (u8_hw >= MH_HARDMAX) {
            u8_hw -= MH_HARDMAX;
            u8_hw = u8_readhardware[(u8_hw << MH_SBITS) + ((/*(UINT32)*/address >>> ABITS_MIN_16) & MHMASK(ABITS2_16))];

            /* for compatibility with setbankhandler, 8-bit systems must call handlers */
 /* for banked memory reads/writes */
            if (u8_hw == HT_RAM) {
                return cpu_bankbase[HT_RAM].read(address);
            }
        }

        /* fall back to handler */
        return (memoryreadhandler[u8_hw]).handler(address - memoryreadoffset[u8_hw]);
    }

    public static int cpu_readmem21(int address) {
        char u8_hw;

        /* first-level lookup */
        u8_hw = u8_cur_mrhard[/*(UINT32)*/address >>> (ABITS2_21 + ABITS_MIN_21)];

        /* for compatibility with setbankhandler, 8-bit systems must call handlers */
 /* for banked memory reads/writes */
        if (u8_hw == HT_RAM) {
            //return cpu_bankbase[HT_RAM][address];
            return cpu_bankbase[HT_RAM].read(address);
        }

        /* second-level lookup */
        if (u8_hw >= MH_HARDMAX) {
            u8_hw -= MH_HARDMAX;
            u8_hw = u8_readhardware[(u8_hw << MH_SBITS) + ((/*(UINT32)*/address >>> ABITS_MIN_21) & MHMASK(ABITS2_21))];

            /* for compatibility with setbankhandler, 8-bit systems must call handlers */
 /* for banked memory reads/writes */
            if (u8_hw == HT_RAM) {
                return cpu_bankbase[HT_RAM].read(address);
            }
        }

        /* fall back to handler */
        return (memoryreadhandler[u8_hw]).handler(address - memoryreadoffset[u8_hw]);
    }

    /*TODO*///READBYTE(cpu_readmem16,    TYPE_8BIT,	  16)
    /*TODO*///READBYTE(cpu_readmem20,    TYPE_8BIT,	  20)
    /*TODO*///READBYTE(cpu_readmem21,    TYPE_8BIT,	  21)
    /*TODO*///
    /*TODO*///READBYTE(cpu_readmem16bew, TYPE_16BIT_BE, 16BEW)
    /*TODO*///READWORD(cpu_readmem16bew, TYPE_16BIT_BE, 16BEW, ALWAYS_ALIGNED)
    /*TODO*///
    /*TODO*///READBYTE(cpu_readmem16lew, TYPE_16BIT_LE, 16LEW)
    /*TODO*///READWORD(cpu_readmem16lew, TYPE_16BIT_LE, 16LEW, ALWAYS_ALIGNED)
    /*TODO*///
    /*TODO*///READBYTE(cpu_readmem24,     TYPE_8BIT,	  24)
    /*TODO*///
    /*TODO*///READBYTE(cpu_readmem24bew, TYPE_16BIT_BE, 24BEW)
    /*TODO*///READWORD(cpu_readmem24bew, TYPE_16BIT_BE, 24BEW, CAN_BE_MISALIGNED)
    /*TODO*///READLONG(cpu_readmem24bew, TYPE_16BIT_BE, 24BEW, CAN_BE_MISALIGNED)
    /*TODO*///
    /*TODO*///READBYTE(cpu_readmem26lew, TYPE_16BIT_LE, 26LEW)
    /*TODO*///READWORD(cpu_readmem26lew, TYPE_16BIT_LE, 26LEW, ALWAYS_ALIGNED)
    /*TODO*///READLONG(cpu_readmem26lew, TYPE_16BIT_LE, 26LEW, ALWAYS_ALIGNED)
    /*TODO*///
    /*TODO*///READBYTE(cpu_readmem29,    TYPE_16BIT_LE, 29)
    /*TODO*///READWORD(cpu_readmem29,    TYPE_16BIT_LE, 29,	 CAN_BE_MISALIGNED)
    /*TODO*///READLONG(cpu_readmem29,    TYPE_16BIT_LE, 29,	 CAN_BE_MISALIGNED)
    /*TODO*///
    /*TODO*///READBYTE(cpu_readmem32,    TYPE_16BIT_BE, 32)
    /*TODO*///READWORD(cpu_readmem32,    TYPE_16BIT_BE, 32,	 CAN_BE_MISALIGNED)
    /*TODO*///READLONG(cpu_readmem32,    TYPE_16BIT_BE, 32,	 CAN_BE_MISALIGNED)
    /*TODO*///
    /*TODO*///READBYTE(cpu_readmem32lew, TYPE_16BIT_LE, 32LEW)
    /*TODO*///READWORD(cpu_readmem32lew, TYPE_16BIT_LE, 32LEW, CAN_BE_MISALIGNED)
    /*TODO*///READLONG(cpu_readmem32lew, TYPE_16BIT_LE, 32LEW, CAN_BE_MISALIGNED)
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
    /*TODO*///void name(offs_t address,data_t data)													\
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
    /*TODO*///		return; 																		\
    /*TODO*///	}																					\
    /*TODO*///	else if (type != TYPE_8BIT && hw <= HT_BANKMAX) 									\
    /*TODO*///	{																					\
    /*TODO*///		if (type == TYPE_16BIT_BE)														\
    /*TODO*///			cpu_bankbase[hw][BYTE_XOR_BE(address) - memorywriteoffset[hw]] = data;		\
    /*TODO*///		else if (type == TYPE_16BIT_LE) 												\
    /*TODO*///			cpu_bankbase[hw][BYTE_XOR_LE(address) - memorywriteoffset[hw]] = data;		\
    /*TODO*///		return; 																		\
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
    /*TODO*///			return; 																	\
    /*TODO*///		}																				\
    /*TODO*///		else if (type != TYPE_8BIT && hw <= HT_BANKMAX) 								\
    /*TODO*///		{																				\
    /*TODO*///			if (type == TYPE_16BIT_BE)													\
    /*TODO*///				cpu_bankbase[hw][BYTE_XOR_BE(address) - memorywriteoffset[hw]] = data;	\
    /*TODO*///			else if (type == TYPE_16BIT_LE) 											\
    /*TODO*///				cpu_bankbase[hw][BYTE_XOR_LE(address) - memorywriteoffset[hw]] = data;	\
    /*TODO*///			return; 																	\
    /*TODO*///		}																				\
    /*TODO*///	}																					\
    /*TODO*///																						\
    /*TODO*///	/* fall back to handler */															\
    /*TODO*///	if (type != TYPE_8BIT)																\
    /*TODO*///	{																					\
    /*TODO*///		int shift = (address & 1) << 3; 												\
    /*TODO*///		if (type == TYPE_16BIT_BE)														\
    /*TODO*///			shift ^= 8; 																\
    /*TODO*///		data = (0xff000000 >> shift) | ((data & 0xff) << shift);						\
    /*TODO*///		address &= ~1;																	\
    /*TODO*///	}																					\
    /*TODO*///	(*memorywritehandler[hw])(address - memorywriteoffset[hw], data);					\
    /*TODO*///}
    /*TODO*///
    /*TODO*////* generic word-sized write handler (16-bit aligned only!) */
    /*TODO*///#define WRITEWORD(name,type,abits,align)												\
    /*TODO*///void name##_word(offs_t address,data_t data)											\
    /*TODO*///{																						\
    /*TODO*///	MHELE hw;																			\
    /*TODO*///																						\
    /*TODO*///	/* only supports 16-bit memory systems */											\
    /*TODO*///	if (type == TYPE_8BIT)																\
    /*TODO*///		printf("Unsupported type for WRITEWORD macro!\n");                              \
    /*TODO*///																						\
    /*TODO*///	/* handle aligned case first */ 													\
    /*TODO*///	if (align == ALWAYS_ALIGNED || !(address & 1))										\
    /*TODO*///	{																					\
    /*TODO*///		/* first-level lookup */														\
    /*TODO*///		hw = cur_mwhard[(UINT32)address >> (ABITS2_##abits + ABITS_MIN_##abits)];		\
    /*TODO*///		if (hw <= HT_BANKMAX)															\
    /*TODO*///		{																				\
    /*TODO*///			WRITE_WORD(&cpu_bankbase[hw][address - memorywriteoffset[hw]], data);		\
    /*TODO*///			return; 																	\
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
    /*TODO*///				return; 																\
    /*TODO*///			}																			\
    /*TODO*///		}																				\
    /*TODO*///																						\
    /*TODO*///		/* fall back to handler */														\
    /*TODO*///		(*memorywritehandler[hw])(address - memorywriteoffset[hw], data & 0xffff);		\
    /*TODO*///	}																					\
    /*TODO*///																						\
    /*TODO*///	/* unaligned case */																\
    /*TODO*///	else if (type == TYPE_16BIT_BE) 													\
    /*TODO*///	{																					\
    /*TODO*///		name(address, data >> 8);														\
    /*TODO*///		name(address + 1, data & 0xff); 												\
    /*TODO*///	}																					\
    /*TODO*///	else if (type == TYPE_16BIT_LE) 													\
    /*TODO*///	{																					\
    /*TODO*///		name(address, data & 0xff); 													\
    /*TODO*///		name(address + 1, data >> 8);													\
    /*TODO*///	}																					\
    /*TODO*///}
    /*TODO*///
    /*TODO*////* generic dword-sized write handler (16-bit aligned only!) */
    /*TODO*///#define WRITELONG(name,type,abits,align)												\
    /*TODO*///void name##_dword(offs_t address,data_t data)											\
    /*TODO*///{																						\
    /*TODO*///	UINT16 word1, word2;																\
    /*TODO*///	MHELE hw1, hw2; 																	\
    /*TODO*///																						\
    /*TODO*///	/* only supports 16-bit memory systems */											\
    /*TODO*///	if (type == TYPE_8BIT)																\
    /*TODO*///		printf("Unsupported type for WRITEWORD macro!\n");                              \
    /*TODO*///																						\
    /*TODO*///	/* handle aligned case first */ 													\
    /*TODO*///	if (align == ALWAYS_ALIGNED || !(address & 1))										\
    /*TODO*///	{																					\
    /*TODO*///		int address2 = (address + 2) & ADDRESS_MASK(abits); 							\
    /*TODO*///																						\
    /*TODO*///		/* first-level lookup */														\
    /*TODO*///		hw1 = cur_mwhard[(UINT32)address >> (ABITS2_##abits + ABITS_MIN_##abits)];		\
    /*TODO*///		hw2 = cur_mwhard[(UINT32)address2 >> (ABITS2_##abits + ABITS_MIN_##abits)]; 	\
    /*TODO*///																						\
    /*TODO*///		/* second-level lookup */														\
    /*TODO*///		if (hw1 >= MH_HARDMAX)															\
    /*TODO*///		{																				\
    /*TODO*///			hw1 -= MH_HARDMAX;															\
    /*TODO*///			hw1 = writehardware[(hw1 << MH_SBITS) + (((UINT32)address >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))]; \
    /*TODO*///		}																				\
    /*TODO*///		if (hw2 >= MH_HARDMAX)															\
    /*TODO*///		{																				\
    /*TODO*///			hw2 -= MH_HARDMAX;															\
    /*TODO*///			hw2 = writehardware[(hw2 << MH_SBITS) + (((UINT32)address2 >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))];	\
    /*TODO*///		}																				\
    /*TODO*///																						\
    /*TODO*///		/* extract words */ 															\
    /*TODO*///		if (type == TYPE_16BIT_BE)														\
    /*TODO*///		{																				\
    /*TODO*///			word1 = data >> 16; 														\
    /*TODO*///			word2 = data & 0xffff;														\
    /*TODO*///		}																				\
    /*TODO*///		else if (type == TYPE_16BIT_LE) 												\
    /*TODO*///		{																				\
    /*TODO*///			word1 = data & 0xffff;														\
    /*TODO*///			word2 = data >> 16; 														\
    /*TODO*///		}																				\
    /*TODO*///																						\
    /*TODO*///		/* process each word */ 														\
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
    /*TODO*///	else if (type == TYPE_16BIT_BE) 													\
    /*TODO*///	{																					\
    /*TODO*///		name(address, data >> 24);														\
    /*TODO*///		name##_word(address + 1, (data >> 8) & 0xffff); 								\
    /*TODO*///		name(address + 3, data & 0xff); 												\
    /*TODO*///	}																					\
    /*TODO*///	else if (type == TYPE_16BIT_LE) 													\
    /*TODO*///	{																					\
    /*TODO*///		name(address, data & 0xff); 													\
    /*TODO*///		name##_word(address + 1, (data >> 8) & 0xffff); 								\
    /*TODO*///		name(address + 3, data >> 24);													\
    /*TODO*///	}																					\
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////* the handlers we need to generate */
    public static void cpu_writemem16(int address, int data) {
        char u8_hw;

        /* first-level lookup */
        u8_hw = u8_cur_mwhard[/*(UINT32)*/address >>> (ABITS2_16 + ABITS_MIN_16)];

        /* for compatibility with setbankhandler, 8-bit systems must call handlers */
 /* for banked memory reads/writes */
        if (u8_hw == HT_RAM) {
            cpu_bankbase[HT_RAM].write(address, data);
            return;
        }
        /* second-level lookup */
        if (u8_hw >= MH_HARDMAX) {
            u8_hw -= MH_HARDMAX;
            u8_hw = u8_writehardware[(u8_hw << MH_SBITS) + ((/*(UINT32)*/address >>> ABITS_MIN_16) & MHMASK(ABITS2_16))];

            /* for compatibility with setbankhandler, 8-bit systems must call handlers */
 /* for banked memory reads/writes */
            if (u8_hw == HT_RAM) {
                cpu_bankbase[HT_RAM].write(address, data);
                return;
            }
        }

        /* fall back to handler */
        (memorywritehandler[u8_hw]).handler(address - memorywriteoffset[u8_hw], data);
    }
    public static void cpu_writemem21(int address, int data) {
        char u8_hw;

        /* first-level lookup */
        u8_hw = u8_cur_mwhard[/*(UINT32)*/address >>> (ABITS2_21 + ABITS_MIN_21)];

        /* for compatibility with setbankhandler, 8-bit systems must call handlers */
 /* for banked memory reads/writes */
        if (u8_hw == HT_RAM) {
            cpu_bankbase[HT_RAM].write(address, data);
            return;
        }
        /* second-level lookup */
        if (u8_hw >= MH_HARDMAX) {
            u8_hw -= MH_HARDMAX;
            u8_hw = u8_writehardware[(u8_hw << MH_SBITS) + ((/*(UINT32)*/address >>> ABITS_MIN_21) & MHMASK(ABITS2_21))];

            /* for compatibility with setbankhandler, 8-bit systems must call handlers */
 /* for banked memory reads/writes */
            if (u8_hw == HT_RAM) {
                cpu_bankbase[HT_RAM].write(address, data);
                return;
            }
        }

        /* fall back to handler */
        (memorywritehandler[u8_hw]).handler(address - memorywriteoffset[u8_hw], data);
    }
    /*TODO*///WRITEBYTE(cpu_writemem16,	 TYPE_8BIT, 	16)
/*TODO*///WRITEBYTE(cpu_writemem20,	 TYPE_8BIT, 	20)
/*TODO*///WRITEBYTE(cpu_writemem21,	 TYPE_8BIT, 	21)
/*TODO*///
/*TODO*///WRITEBYTE(cpu_writemem16bew, TYPE_16BIT_BE, 16BEW)
/*TODO*///WRITEWORD(cpu_writemem16bew, TYPE_16BIT_BE, 16BEW, ALWAYS_ALIGNED)
/*TODO*///
/*TODO*///WRITEBYTE(cpu_writemem16lew, TYPE_16BIT_LE, 16LEW)
/*TODO*///WRITEWORD(cpu_writemem16lew, TYPE_16BIT_LE, 16LEW, ALWAYS_ALIGNED)
/*TODO*///
/*TODO*///WRITEBYTE(cpu_writemem24,	  TYPE_8BIT, 	24)
/*TODO*///
/*TODO*///WRITEBYTE(cpu_writemem24bew, TYPE_16BIT_BE, 24BEW)
/*TODO*///WRITEWORD(cpu_writemem24bew, TYPE_16BIT_BE, 24BEW, CAN_BE_MISALIGNED)
/*TODO*///WRITELONG(cpu_writemem24bew, TYPE_16BIT_BE, 24BEW, CAN_BE_MISALIGNED)
/*TODO*///
/*TODO*///WRITEBYTE(cpu_writemem26lew, TYPE_16BIT_LE, 26LEW)
/*TODO*///WRITEWORD(cpu_writemem26lew, TYPE_16BIT_LE, 26LEW, ALWAYS_ALIGNED)
/*TODO*///WRITELONG(cpu_writemem26lew, TYPE_16BIT_LE, 26LEW, ALWAYS_ALIGNED)
/*TODO*///
/*TODO*///WRITEBYTE(cpu_writemem29,    TYPE_16BIT_LE, 29)
/*TODO*///WRITEWORD(cpu_writemem29,	 TYPE_16BIT_LE, 29,    CAN_BE_MISALIGNED)
/*TODO*///WRITELONG(cpu_writemem29,	 TYPE_16BIT_LE, 29,    CAN_BE_MISALIGNED)
/*TODO*///
/*TODO*///WRITEBYTE(cpu_writemem32,	 TYPE_16BIT_BE, 32)
/*TODO*///WRITEWORD(cpu_writemem32,	 TYPE_16BIT_BE, 32,    CAN_BE_MISALIGNED)
/*TODO*///WRITELONG(cpu_writemem32,	 TYPE_16BIT_BE, 32,    CAN_BE_MISALIGNED)
/*TODO*///
/*TODO*///WRITEBYTE(cpu_writemem32lew, TYPE_16BIT_LE, 32LEW)
/*TODO*///WRITEWORD(cpu_writemem32lew, TYPE_16BIT_LE, 32LEW, CAN_BE_MISALIGNED)
/*TODO*///WRITELONG(cpu_writemem32lew, TYPE_16BIT_LE, 32LEW, CAN_BE_MISALIGNED)
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Opcode base changers. This function is called by the CPU emulation.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*////* generic opcode base changer */
/*TODO*///#define SETOPBASE(name,abits,shift) 													
/*TODO*///void name(int pc)																		
/*TODO*///{																						
/*TODO*///	MHELE hw;																			
/*TODO*///																						
/*TODO*///	pc = (UINT32)pc >> shift;															
/*TODO*///																						
/*TODO*///	/* allow overrides */																
/*TODO*///	if (OPbasefunc) 																	
/*TODO*///	{																					
/*TODO*///		pc = OPbasefunc(pc);															
/*TODO*///		if (pc == -1)																	
/*TODO*///			return; 																	
/*TODO*///	}																					
/*TODO*///																						
/*TODO*///	/* perform the lookup */															
/*TODO*///	hw = cur_mrhard[(UINT32)pc >> (ABITS2_##abits + ABITS_MIN_##abits)];				
/*TODO*///	if (hw >= MH_HARDMAX)																
/*TODO*///	{																					
/*TODO*///		hw -= MH_HARDMAX;																
/*TODO*///		hw = readhardware[(hw << MH_SBITS) + (((UINT32)pc >> ABITS_MIN_##abits) & MHMASK(ABITS2_##abits))]; 
/*TODO*///	}																					
/*TODO*///	ophw = hw;																			
/*TODO*///																						
/*TODO*///	/* RAM or banked memory */															
/*TODO*///	if (hw <= HT_BANKMAX)																
/*TODO*///	{																					
/*TODO*///		SET_OP_RAMROM(cpu_bankbase[hw] - memoryreadoffset[hw])							
/*TODO*///		return; 																		
/*TODO*///	}																					
/*TODO*///																						
/*TODO*///	/* do not support on callback memory region */										
/*TODO*///	logerror("CPU #%d PC %04x: warning - op-code execute on mapped i/o\n",              
/*TODO*///				cpu_getactivecpu(),cpu_get_pc());										
/*TODO*///}
/*TODO*///
/*TODO*///
    /* the handlers we need to generate */
    public static setopbase cpu_setOPbase16 = new setopbase() {
        public void handler(int pc) {
            char u8_hw;

            //not shift neccesary pc = (UINT32)pc >> shift;															
            /* allow overrides */
            if (OPbasefunc != null) {
                pc = OPbasefunc.handler(pc);
                if (pc == -1) {
                    return;
                }
            }

            /* perform the lookup */
            u8_hw = u8_cur_mrhard[/*(UINT32)*/pc >>> (ABITS2_16 + ABITS_MIN_16)];
            if (u8_hw >= MH_HARDMAX) {
                u8_hw -= MH_HARDMAX;
                u8_hw = u8_readhardware[(u8_hw << MH_SBITS) + ((/*(UINT32)*/pc >>> ABITS_MIN_16) & MHMASK(ABITS2_16))];
            }
            u8_ophw = (char) (u8_hw & 0xFF);

            /* RAM or banked memory */
            if (u8_hw <= HT_BANKMAX) {
                SET_OP_RAMROM(new UBytePtr(cpu_bankbase[u8_hw], -memoryreadoffset[u8_hw]));
                return;
            }

            /* do not support on callback memory region */
            logerror("CPU #%d PC %04x: warning - op-code execute on mapped i/o\n",
                    cpu_getactivecpu(), cpu_get_pc());
        }
    };
    public static setopbase cpu_setOPbase21 = new setopbase() {
        public void handler(int pc) {
            char u8_hw;

            //not shift neccesary pc = (UINT32)pc >> shift;															
            /* allow overrides */
            if (OPbasefunc != null) {
                pc = OPbasefunc.handler(pc);
                if (pc == -1) {
                    return;
                }
            }

            /* perform the lookup */
            u8_hw = u8_cur_mrhard[/*(UINT32)*/pc >>> (ABITS2_21 + ABITS_MIN_21)];
            if (u8_hw >= MH_HARDMAX) {
                u8_hw -= MH_HARDMAX;
                u8_hw = u8_readhardware[(u8_hw << MH_SBITS) + ((/*(UINT32)*/pc >>> ABITS_MIN_21) & MHMASK(ABITS2_21))];
            }
            u8_ophw = (char) (u8_hw & 0xFF);

            /* RAM or banked memory */
            if (u8_hw <= HT_BANKMAX) {
                SET_OP_RAMROM(new UBytePtr(cpu_bankbase[u8_hw], -memoryreadoffset[u8_hw]));
                return;
            }

            /* do not support on callback memory region */
            logerror("CPU #%d PC %04x: warning - op-code execute on mapped i/o\n",
                    cpu_getactivecpu(), cpu_get_pc());

        }
    };

    /*TODO*///SETOPBASE(cpu_setOPbase16bew, 16BEW, 0)
/*TODO*///SETOPBASE(cpu_setOPbase16lew, 16LEW, 0)
/*TODO*///SETOPBASE(cpu_setOPbase20,	  20,	 0)
/*TODO*///SETOPBASE(cpu_setOPbase21,	  21,	 0)
/*TODO*///SETOPBASE(cpu_setOPbase24,	  24,	 0)
/*TODO*///SETOPBASE(cpu_setOPbase24bew, 24BEW, 0)
/*TODO*///SETOPBASE(cpu_setOPbase26lew, 26LEW, 0)
/*TODO*///SETOPBASE(cpu_setOPbase29,	  29,	 3)
/*TODO*///SETOPBASE(cpu_setOPbase32,	  32,	 0)
/*TODO*///SETOPBASE(cpu_setOPbase32lew, 32LEW, 0)
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Perform an I/O port read. This function is called by the CPU emulation.
/*TODO*///
/*TODO*///***************************************************************************/
    public static int cpu_readport(int port) {
        int iorp = 0;

        port &= cur_portmask;

        /* search the handlers. The order is as follows: first the dynamically installed
               handlers are searched, followed by the static ones in whatever order they were
               specified in the driver */
        while (cur_readport[iorp].start != -1) {
            if (port >= cur_readport[iorp].start && port <= cur_readport[iorp].end) {
                ReadHandlerPtr handler = cur_readport[iorp]._handler;

                if (handler == null) {
                    return 0;//if (handler == IORP_NOP) return 0;
                } else {
                    return handler.handler(port - cur_readport[iorp].start);
                }
            }

            iorp++;
        }
        logerror("CPU #%d PC %04x: warning - read unmapped I/O port %02x\n", cpu_getactivecpu(), cpu_get_pc(), port);
        return 0;
    }

    /**
     * *************************************************************************
     *
     * Perform an I/O port write. This function is called by the CPU emulation.
     *
     **************************************************************************
     */
    public static void cpu_writeport(int port, int value) {
        int iowp = 0;

        port &= cur_portmask;

        /* search the handlers. The order is as follows: first the dynamically installed
               handlers are searched, followed by the static ones in whatever order they were
               specified in the driver */
        while (cur_writeport[iowp].start != -1) {
            if (port >= cur_writeport[iowp].start && port <= cur_writeport[iowp].end) {
                WriteHandlerPtr handler = cur_writeport[iowp]._handler;

                if (handler == null) {
                    return;//if (handler == IOWP_NOP) return;
                } else {
                    handler.handler(port - cur_writeport[iowp].start, value);
                }

                return;
            }

            iowp++;
        }
        logerror("CPU #%d PC %04x: warning - write %02x to unmapped I/O port %02x\n", cpu_getactivecpu(), cpu_get_pc(), value, port);
    }

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
/*TODO*////* set writememory handler for bank memory	*/
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
    public static UBytePtr install_mem_read_handler(int cpu, int start, int end, ReadHandlerPtr _handler) {
        return install_mem_read_handler(cpu, start, end, _handler, -15000);
    }

    public static UBytePtr install_mem_read_handler(int cpu, int start, int end, ReadHandlerPtr _handler, int handler) {
        char u8_hardware = 0;
        int abitsmin;
        int i, hw_set;
        logerror("Install new memory read handler:\n");
        logerror("             cpu: %d\n", cpu);
        logerror("           start: 0x%08x\n", start);
        logerror("             end: 0x%08x\n", end);

        //logerror(" handler address: 0x%08x\n", (unsigned int) handler);
        abitsmin = ABITSMIN(cpu);

        if (end < start) {
            printf("fatal: install_mem_read_handler(), start = %08x > end = %08x\n", start, end);
            throw new UnsupportedOperationException("error");
        }
        if ((start & (ALIGNUNIT(cpu) - 1)) != 0 || (end & (ALIGNUNIT(cpu) - 1)) != (ALIGNUNIT(cpu) - 1)) {
            printf("fatal: install_mem_read_handler(), start = %08x, end = %08x ALIGN = %d\n", start, end, ALIGNUNIT(cpu));
            throw new UnsupportedOperationException("error");
        }

        /* see if this function is already registered */
        hw_set = 0;
        for (i = 0; i < MH_HARDMAX; i++) {
            /* record it if it matches */
            if (memoryreadhandler[i] != null && (memoryreadhandler[i] == _handler)
                    && (memoryreadoffset[i] == start)) {
                logerror("handler match - use old one\n");
                u8_hardware = (char) (i & 0xFF);
                hw_set = 1;
            }
        }
        switch (handler) {
            case MRA_ROM:
            case MRA_RAM: {
                u8_hardware = HT_RAM;
                /* special case ram read */
                hw_set = 1;
            }
            break;
            case MRA_BANK1:
            case MRA_BANK2:
            case MRA_BANK3:
            case MRA_BANK4:
            case MRA_BANK5:
            case MRA_BANK6:
            case MRA_BANK7:
            case MRA_BANK8:
            case MRA_BANK9:
            case MRA_BANK10:
            case MRA_BANK11:
            case MRA_BANK12:
            case MRA_BANK13:
            case MRA_BANK14:
            case MRA_BANK15:
            case MRA_BANK16: {
                u8_hardware = (char) ((int) MRA_BANK1 - (int) handler + 1);
                memoryreadoffset[u8_hardware] = bankreadoffset[u8_hardware] = start;
                cpu_bankbase[u8_hardware] = memory_find_base(cpu, start);
                hw_set = 1;

            }
            break;
            case MRA_NOP: {
                u8_hardware = HT_NOP;
                hw_set = 1;

            }
            break;
        }
        if (hw_set == 0) /* no match */ {
            /* create newer hardware handler */
            if (rdhard_max == MH_HARDMAX) {
                logerror("read memory hardware pattern over !\n");
                logerror("Failed to install new memory handler.\n");
                return memory_find_base(cpu, start);
            } else {
                /* register hardware function */
                u8_hardware = (char) (rdhard_max++);
                memoryreadhandler[u8_hardware] = _handler;
                memoryreadoffset[u8_hardware] = start;
            }
        }
        /* set hardware element table entry */
        set_element(cpu, new UBytePtr(u8_cur_mr_element[cpu]),
                ((/*(unsigned int)*/start) >>> abitsmin),
                ((/*(unsigned int)*/end) >>> abitsmin),
                u8_hardware, new UBytePtr(u8_readhardware), rdelement_max);

        logerror("Done installing new memory handler.\n");
        logerror("used read  elements %d/%d , functions %d/%d\n",
                rdelement_max[0], MH_ELEMAX, rdhard_max, MH_HARDMAX);
        return memory_find_base(cpu, start);
    }

    public static UBytePtr install_mem_write_handler(int cpu, int start, int end, WriteHandlerPtr _handler) {
        return install_mem_write_handler(cpu, start, end, _handler, -15000);
    }

    public static UBytePtr install_mem_write_handler(int cpu, int start, int end, WriteHandlerPtr _handler, int handler) {
        char u8_hardware = 0;
        int abitsmin;
        int i, hw_set;
        logerror("Install new memory write handler:\n");
        logerror("             cpu: %d\n", cpu);
        logerror("           start: 0x%08x\n", start);
        logerror("             end: 0x%08x\n", end);

        //logerror(" handler address: 0x%08x\n", (unsigned int) handler);
        abitsmin = ABITSMIN(cpu);

        if (end < start) {
            printf("fatal: install_mem_write_handler(), start = %08x > end = %08x\n", start, end);
            throw new UnsupportedOperationException("error");
        }
        if ((start & (ALIGNUNIT(cpu) - 1)) != 0 || (end & (ALIGNUNIT(cpu) - 1)) != (ALIGNUNIT(cpu) - 1)) {
            printf("fatal: install_mem_write_handler(), start = %08x, end = %08x ALIGN = %d\n", start, end, ALIGNUNIT(cpu));
            throw new UnsupportedOperationException("error");
        }

        /* see if this function is already registered */
        hw_set = 0;
        for (i = 0; i < MH_HARDMAX; i++) {
            /* record it if it matches */
            if (memorywritehandler[i] != null && (memorywritehandler[i] == _handler)
                    && (memorywriteoffset[i] == start)) {
                logerror("handler match - use old one\n");
                u8_hardware = (char) (i & 0xFF);
                hw_set = 1;
            }
        }
        switch (handler) {
            case MWA_RAM: {
                u8_hardware = HT_RAM;
                /* special case ram write */
                hw_set = 1;
            }
            break;
            case MWA_BANK1:
            case MWA_BANK2:
            case MWA_BANK3:
            case MWA_BANK4:
            case MWA_BANK5:
            case MWA_BANK6:
            case MWA_BANK7:
            case MWA_BANK8:
            case MWA_BANK9:
            case MWA_BANK10:
            case MWA_BANK11:
            case MWA_BANK12:
            case MWA_BANK13:
            case MWA_BANK14:
            case MWA_BANK15:
            case MWA_BANK16: {
                u8_hardware = (char) ((int) MWA_BANK1 - (int) handler + 1);
                memorywriteoffset[u8_hardware] = bankwriteoffset[u8_hardware] = start;
                cpu_bankbase[u8_hardware] = memory_find_base(cpu, start);
                hw_set = 1;
            }
            break;
            case MWA_NOP: {
                u8_hardware = HT_NOP;
                hw_set = 1;
            }
            break;
            case MWA_RAMROM: {
                u8_hardware = HT_RAMROM;
                hw_set = 1;
            }
            break;
            case MWA_ROM: {
                u8_hardware = HT_ROM;
                hw_set = 1;
            }
            break;
        }
        if (hw_set == 0) /* no match */ {
            /* create newer hardware handler */
            if (wrhard_max == MH_HARDMAX) {
                logerror("write memory hardware pattern over !\n");
                logerror("Failed to install new memory handler.\n");

                return memory_find_base(cpu, start);
            } else {
                /* register hardware function */
                u8_hardware = (char) (wrhard_max++);
                memorywritehandler[u8_hardware] = _handler;
                memorywriteoffset[u8_hardware] = start;
            }
        }
        /* set hardware element table entry */
        set_element(cpu, new UBytePtr(u8_cur_mw_element[cpu]),
                ((/*(unsigned int)*/start) >>> abitsmin),
                ((/*(unsigned int)*/end) >>> abitsmin),
                u8_hardware, new UBytePtr(u8_writehardware), wrelement_max);

        logerror("Done installing new memory handler.\n");
        logerror("used write elements %d/%d , functions %d/%d\n",
                wrelement_max[0], MH_ELEMAX, wrhard_max, MH_HARDMAX);

        return memory_find_base(cpu, start);
    }

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
        logerror("Installing port read handler: cpu %d  slot %X  start %X  end %X\n", cpu, i, start, end);

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

        logerror("Installing port write handler: cpu %d  slot %X  start %X  end %X\n", cpu, i, start, end);

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
