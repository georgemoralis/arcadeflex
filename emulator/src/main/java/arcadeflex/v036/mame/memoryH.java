/**
 * ported to 0.36
 */
package arcadeflex.v036.mame;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.*;

public class memoryH {

    public static final int MAX_BANKS = 16;

    public static abstract interface opbase_handlerPtr {

        public abstract int handler(int address);
    }

    public static abstract interface setopbase {

        public abstract void handler(int pc);
    }

    /**
     * *************************************************************************
     *
     * Note that the memory hooks are not passed the actual memory address where
     * the operation takes place, but the offset from the beginning of the block
     * they are assigned to. This makes handling of mirror addresses easier, and
     * makes the handlers a bit more "object oriented". If you handler needs to
     * read/write the main memory area, provide a "base" pointer: it will be
     * initialized by the main engine to point to the beginning of the memory
     * block assigned to the handler. You may also provided a pointer to "size":
     * it will be set to the length of the memory area processed by the handler.
     *
     **************************************************************************
     */
    public static class MemoryReadAddress {

        public MemoryReadAddress(int start, int end, ReadHandlerPtr _handler, UBytePtr base) {
            this.start = start;
            this.end = end;
            this.handler = -15000;//random number for not matching something else
            this._handler = _handler;
            this.base = base;
        }

        public MemoryReadAddress(int start, int end, int handler, UBytePtr base) {
            this.start = start;
            this.end = end;
            this.handler = handler;
            this.base = base;
        }

        public MemoryReadAddress(int start, int end, int handler) {
            this(start, end, handler, null);
        }

        public MemoryReadAddress(int start, int end, ReadHandlerPtr _handler) {
            this(start, end, _handler, null);
        }

        public MemoryReadAddress(int start) {
            this(start, -1, null);
        }
        public int start, end;
        public int handler;
        public UBytePtr base;
        public ReadHandlerPtr _handler;
        /* see special values below */
    }

    public static final int MRA_NOP = 0;/* don't care, return 0 */
    public static final int MRA_RAM = -1;/* plain RAM location (return its contents) */
    public static final int MRA_ROM = -2;/* plain ROM location (return its contents) */
    public static final int MRA_BANK1 = -10;/* bank memory */
    public static final int MRA_BANK2 = -11;/* bank memory */
    public static final int MRA_BANK3 = -12;/* bank memory */
    public static final int MRA_BANK4 = -13;/* bank memory */
    public static final int MRA_BANK5 = -14;/* bank memory */
    public static final int MRA_BANK6 = -15;/* bank memory */
    public static final int MRA_BANK7 = -16;/* bank memory */
    public static final int MRA_BANK8 = -17;/* bank memory */
    public static final int MRA_BANK9 = -18;/* bank memory */
    public static final int MRA_BANK10 = -19;/* bank memory */
    public static final int MRA_BANK11 = -20;/* bank memory */
    public static final int MRA_BANK12 = -21;/* bank memory */
    public static final int MRA_BANK13 = -22;/* bank memory */
    public static final int MRA_BANK14 = -23;/* bank memory */
    public static final int MRA_BANK15 = -24;/* bank memory */
    public static final int MRA_BANK16 = -25;/* bank memory */

    public static class MemoryWriteAddress {

        public MemoryWriteAddress(int start, int end, int handler, UBytePtr base, int[] size) {
            this.start = start;
            this.end = end;
            this.handler = handler;
            this.base = base;
            this.size = size;
        }

        public MemoryWriteAddress(int start, int end, WriteHandlerPtr _handler, UBytePtr base, int[] size) {
            this.start = start;
            this.end = end;
            this.handler = -15000;//random number for not matching something else
            this._handler = _handler;
            this.base = base;
            this.size = size;
        }

        public MemoryWriteAddress(int start, int end, int handler, UBytePtr base) {
            this.start = start;
            this.end = end;
            this.handler = handler;
            this.base = base;
        }

        public MemoryWriteAddress(int start, int end, int handler) {
            this(start, end, handler, null);
        }

        public MemoryWriteAddress(int start, int end, WriteHandlerPtr _handler, UBytePtr base) {
            this.start = start;
            this.end = end;
            this.handler = -15000;//random number for not matching something else
            this._handler = _handler;
            this.base = base;
        }

        public MemoryWriteAddress(int start, int end, WriteHandlerPtr _handler) {
            this.start = start;
            this.end = end;
            this.handler = -15000;//random number for not matching something else
            this._handler = _handler;
            this.base = null;
        }

        public MemoryWriteAddress(int start) {
            this(start, -1, null);
        }
        public int start, end;
        public int handler;
        public WriteHandlerPtr _handler;/* see special values below */
        public UBytePtr base;/* optional (see explanation above) */
        public int[] size;/* optional (see explanation above) */
    }

    public static final int MWA_NOP = 0;/* do nothing */
    public static final int MWA_RAM = -1;/* plain RAM location (store the value) */
    public static final int MWA_ROM = -2;/* plain ROM location (do nothing) */
 /*
   If the CPU opcodes are encrypted, they are fetched from a different memory space.
   In such a case, if the program dynamically creates code in RAM and executes it,
   it won't work unless you use MWA_RAMROM to affect both memory spaces.
     */
    public static final int MWA_RAMROM = -3;
    public static final int MWA_BANK1 = -10;/* bank memory */
    public static final int MWA_BANK2 = -11;/* bank memory */
    public static final int MWA_BANK3 = -12;/* bank memory */
    public static final int MWA_BANK4 = -13;/* bank memory */
    public static final int MWA_BANK5 = -14;/* bank memory */
    public static final int MWA_BANK6 = -15;/* bank memory */
    public static final int MWA_BANK7 = -16;/* bank memory */
    public static final int MWA_BANK8 = -17;/* bank memory */
    public static final int MWA_BANK9 = -18;/* bank memory */
    public static final int MWA_BANK10 = -19;/* bank memory */
    public static final int MWA_BANK11 = -20;/* bank memory */
    public static final int MWA_BANK12 = -21;/* bank memory */
    public static final int MWA_BANK13 = -22;/* bank memory */
    public static final int MWA_BANK14 = -23;/* bank memory */
    public static final int MWA_BANK15 = -24;/* bank memory */
    public static final int MWA_BANK16 = -25;/* bank memory */

    /**
     * *************************************************************************
     *
     * IN and OUT ports are handled like memory accesses, the hook template is
     * the same so you can interchange them. Of course there is no 'base'
     * pointer for IO ports.
     *
     **************************************************************************
     */
    public static class IOReadPort {

        public IOReadPort() {
        }

        public IOReadPort(int start, int end, int handler) {
            this.start = start;
            this.end = end;
            this.handler = handler;
        }

        public IOReadPort(int start, int end, ReadHandlerPtr _handler) {
            this.start = start;
            this.end = end;
            this.handler = -15000;//random number for not matching something else
            this._handler = _handler;
        }

        public IOReadPort(int start) {
            this(start, 0, null);
        }
        public int start, end;
        public int handler;
        public ReadHandlerPtr _handler;/* see special values below */
    }
    /* don't care, return 0 */
    public static final int IORP_NOP = 0;

    public static class IOWritePort {

        public IOWritePort() {
        }

        public IOWritePort(int start, int end, int handler) {
            this.start = start;
            this.end = end;
            this.handler = handler;
        }

        public IOWritePort(int start, int end, WriteHandlerPtr _handler) {
            this.start = start;
            this.end = end;
            this.handler = -15000;//random number for not matching something else
            this._handler = _handler;
        }

        public IOWritePort(int start) {
            this(start, 0, null);
        }
        public int start, end;
        public int handler;
        public WriteHandlerPtr _handler;/* see special values below */
    }
    public static final int IOWP_NOP = 0;/* do nothing */

    /**
     * *************************************************************************
     *
     * If a memory region contains areas that are outside of the ROM region for
     * an address space, the memory system will allocate an array of structures
     * to track the external areas.
     *
     **************************************************************************
     */
    public static final int MAX_EXT_MEMORY = 64;

    public static class ExtMemory {

        public int start, end, region;
        public UBytePtr data;
    }
    /**
     * *************************************************************************
     *
     * For a given number of address bits, we need to determine how many
     * elements there are in the first and second-order lookup tables. We also
     * need to know how many low-order bits to ignore. The ABITS* values
     * represent these constants for each address space type we support.
     *
     **************************************************************************
     */

    /* memory element block size */
    public static final int MH_SBITS = 8;/* sub element bank size */
    public static final int MH_PBITS = 8;/* port current element size */
    public static final int MH_ELEMAX = 64;/* sub elements limit */
    public static final int MH_HARDMAX = 64;/* hardware functions limit */

 /* 16 bits address */
    public static final int ABITS1_16 = 12;
    public static final int ABITS2_16 = 4;
    public static final int ABITS_MIN_16 = 0;/* minimum memory block is 1 byte */
 /*TODO*////* 16 bits address (little endian word access) */
/*TODO*///#define ABITS1_16LEW	12
/*TODO*///#define ABITS2_16LEW	3
/*TODO*///#define ABITS_MIN_16LEW	1			/* minimum memory block is 2 bytes */
/*TODO*////* 16 bits address (big endian word access) */
/*TODO*///#define ABITS1_16BEW	12
/*TODO*///#define ABITS2_16BEW	3
/*TODO*///#define ABITS_MIN_16BEW	1			/* minimum memory block is 2 bytes */
/* 20 bits address */
    public static final int ABITS1_20 = 12;
    public static final int ABITS2_20 = 8;
    public static final int ABITS_MIN_20 = 0;/* minimum memory block is 1 byte */
 /* 21 bits address */
    public static final int ABITS1_21 = 13;
    public static final int ABITS2_21 = 8;
    public static final int ABITS_MIN_21 = 0;/* minimum memory block is 1 byte */
 /* 24 bits address (word access) */
    public static final int ABITS1_24 = 15;
    public static final int ABITS2_24 = 8;
    public static final int ABITS_MIN_24 = 1;/* minimum memory block is 2 bytes */
 /*TODO*////* 29 bits address (dword access) */
/*TODO*///#define ABITS1_29		19
/*TODO*///#define ABITS2_29		8
/*TODO*///#define ABITS_MIN_29	2			/* minimum memory block is 4 bytes */
/*TODO*////* 32 bits address (dword access) */
/*TODO*///#define ABITS1_32		23
/*TODO*///#define ABITS2_32		8
/*TODO*///#define ABITS_MIN_32	1			/* minimum memory block is 2 bytes */
    /* mask bits */
    public static int MHMASK(int abits) {

        return (0xffffffff >>> (32 - abits));
    }

    /*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///	Macros
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
    /* ----- 16-bit memory accessing ----- */
 /*TODO*///#define COMBINE_WORD(w,d)     (((w) & ((d) >> 16)) | ((d) & 0xffff))
/*TODO*///#define COMBINE_WORD_MEM(a,d) (WRITE_WORD((a), (READ_WORD(a) & ((d) >> 16)) | (d)))

    /* ----- opcode reading ----- */
    public static char cpu_readop(int A) {
        return OP_ROM.read(A);
    }

    /*TODO*///#define cpu_readop16(A)		READ_WORD(&OP_ROM[A])
    public static char cpu_readop_arg(int A) {
        return OP_RAM.read(A);
    }

    /*TODO*///#define cpu_readop_arg16(A)	READ_WORD(&OP_RAM[A])
/*TODO*///
    /* ----- bank switching for CPU cores ----- */
    public static void change_pc_generic(int pc, int abits2, int abitsmin, int shift, setopbase setop) {
        if (u8_cur_mrhard[pc >>> (abits2 + abitsmin + shift)] != u8_ophw) {
            setop.handler(pc);
        }
    }

    public static void change_pc(int pc) {
        change_pc_generic(pc, ABITS2_16, ABITS_MIN_16, 0, cpu_setOPbase16);
    }

    public static void change_pc16(int pc) {
        change_pc_generic(pc, ABITS2_16, ABITS_MIN_16, 0, cpu_setOPbase16);
    }

    /*TODO*///#define change_pc16bew(pc)	change_pc_generic(pc, ABITS2_16BEW, ABITS_MIN_16BEW, 0, cpu_setOPbase16bew)
/*TODO*///#define change_pc16lew(pc)	change_pc_generic(pc, ABITS2_16LEW, ABITS_MIN_16LEW, 0, cpu_setOPbase16lew)
/*TODO*///#define change_pc20(pc)		change_pc_generic(pc, ABITS2_20, ABITS_MIN_20, 0, cpu_setOPbase20)
/*TODO*///#define change_pc21(pc)		change_pc_generic(pc, ABITS2_21, ABITS_MIN_21, 0, cpu_setOPbase21)
/*TODO*///#define change_pc24(pc)		change_pc_generic(pc, ABITS2_24, ABITS_MIN_24, 0, cpu_setOPbase24)
/*TODO*///#define change_pc29(pc)		change_pc_generic(pc, ABITS2_29, ABITS_MIN_29, 3, cpu_setOPbase29)
/*TODO*///#define change_pc32(pc)		change_pc_generic(pc, ABITS2_32, ABITS_MIN_32, 0, cpu_setOPbase32)
/*TODO*///
/* ----- for use OPbaseOverride driver, request override callback to next cpu_setOPbase ----- */
    public static void catch_nextBranch() {
        u8_ophw = 0xff;
    }

    /* -----  bank switching macro ----- */
    public static void cpu_setbank(int bank, UBytePtr _base) {
        if (bank >= 1 && bank <= MAX_BANKS) {
            cpu_bankbase[bank] = _base;
            if (u8_ophw == bank) {
                u8_ophw = (char) 0xff;
                cpu_setOPbase16.handler(cpu_get_pc());
            }
        }
    }

}
