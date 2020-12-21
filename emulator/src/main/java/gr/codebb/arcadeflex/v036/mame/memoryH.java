
package gr.codebb.arcadeflex.v036.mame;

import gr.codebb.arcadeflex.v036.platform.libc_old.CharPtr;
import gr.codebb.arcadeflex.v036.mame.driverH.ReadHandlerPtr;
import gr.codebb.arcadeflex.v036.mame.driverH.WriteHandlerPtr;
import gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.mame.memory.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;

/**
 *
 *  temp file with old memory definitions to do some workarounds
 * 
 */
public class memoryH {
    

     public static class MemoryReadAddress
     {
        public MemoryReadAddress(int s, int e, int h, UBytePtr b, int[] size){ this.start = s; this.end = e; this.handler = h; this.base = b; this.size = size; }
        public MemoryReadAddress(int s, int e, ReadHandlerPtr rhp, UBytePtr b, int[] size) { this.start = s; this.end = e; this.handler = 1; this._handler = rhp; this.base = b; this.size = size; }
        public MemoryReadAddress(int s, int e, int h, UBytePtr b) { start = s; end = e; handler = h; base = b; };
        public MemoryReadAddress(int s, int e, int h) { this(s, e, h, null); };
        public MemoryReadAddress(int s, int e, ReadHandlerPtr rhp, UBytePtr b) { start = s; end = e; handler = 1; _handler = rhp; base = b; };
        public MemoryReadAddress(int s, int e, ReadHandlerPtr rhp) { this(s, e, rhp, null); };
        public MemoryReadAddress(int s) { this(s, -1, null); };
        public int start,end;
        public int handler;
        public ReadHandlerPtr _handler;	/* see special values below */
        public UBytePtr base;
        public int[] size;
    };

    public static final int MRA_NOP = 0;	/* don't care, return 0 */
    public static final int MRA_RAM = -1;	/* plain RAM location (return its contents) */
    public static final int MRA_ROM = -2;	/* plain ROM location (return its contents) */
    public static final int MRA_BANK1 =-10;	/* bank memory */
    public static final int MRA_BANK2 =-11;	/* bank memory */
    public static final int MRA_BANK3 =-12;	/* bank memory */
    public static final int MRA_BANK4 =-13;	/* bank memory */
    public static final int MRA_BANK5 =-14;	/* bank memory */
    public static final int MRA_BANK6 =-15;	/* bank memory */
    public static final int MRA_BANK7 =-16;	/* bank memory */
    public static final int MRA_BANK8 =-17;	/* bank memory */
    public static final int MRA_BANK9 =-18;	/* bank memory */
    public static final int MRA_BANK10 =-19;	/* bank memory */
    public static final int MRA_BANK11 =-20;	/* bank memory */
    public static final int MRA_BANK12 =-21;	/* bank memory */
    public static final int MRA_BANK13 =-22;	/* bank memory */
    public static final int MRA_BANK14 =-23;	/* bank memory */
    public static final int MRA_BANK15 =-24;	/* bank memory */
    public static final int MRA_BANK16 =-25;	/* bank memory */
    

    
    public static class MemoryWriteAddress
    {
        public MemoryWriteAddress(int s, int e, int h, UBytePtr b, int[] size){this.start = s; this.end = e; this.handler = h; this.base = b; this.size = size; }
        public MemoryWriteAddress(int s, int e, WriteHandlerPtr whp, UBytePtr b, int[] size) { this.start = s; this.end = e; this.handler = 1; this._handler = whp; this.base = b; this.size = size; }
        public MemoryWriteAddress(int s, int e, int h, UBytePtr b) { start = s; end = e; handler = h; base = b; };
        public MemoryWriteAddress(int s, int e, int h) { this(s, e, h, null); };
        public MemoryWriteAddress(int s, int e, WriteHandlerPtr whp, UBytePtr b) { start = s; end = e; handler = 1; _handler = whp; base = b; };
        public MemoryWriteAddress(int s, int e, WriteHandlerPtr whp) { start = s; end = e; handler = 1; _handler = whp; base = null;/*this(s, e, whp, null);*/ };
        public MemoryWriteAddress(int s) { this(s, -1, null); };
        public int start,end;
        public int handler;
        public WriteHandlerPtr _handler;	/* see special values below */
        public UBytePtr base;
        public int[] size;
    };

    public static final int MWA_NOP = 0;	/* do nothing */
    public static final int MWA_RAM = -1;	/* plain RAM location (store the value) */
    public static final int MWA_ROM = -2;	/* plain ROM location (do nothing) */
    /*
       If the CPU opcodes are encrypted, they are fetched from a different memory space.
       In such a case, if the program dynamically creates code in RAM and executes it,
       it won't work unless you use MWA_RAMROM to affect both memory spaces.
     */
    public static final int MWA_RAMROM =-3;
    public static final int MWA_BANK1 =-10;	/* bank memory */
    public static final int MWA_BANK2 =-11;	/* bank memory */
    public static final int MWA_BANK3 =-12;	/* bank memory */
    public static final int MWA_BANK4 =-13;	/* bank memory */
    public static final int MWA_BANK5 =-14;	/* bank memory */
    public static final int MWA_BANK6 =-15;	/* bank memory */
    public static final int MWA_BANK7 =-16;	/* bank memory */
    public static final int MWA_BANK8 =-17;	/* bank memory */
    public static final int MWA_BANK9 =-18;	/* bank memory */
    public static final int MWA_BANK10 =-19;	/* bank memory */
    public static final int MWA_BANK11 =-20;	/* bank memory */
    public static final int MWA_BANK12 =-21;	/* bank memory */
    public static final int MWA_BANK13 =-22;	/* bank memory */
    public static final int MWA_BANK14 =-23;	/* bank memory */
    public static final int MWA_BANK15 =-24;	/* bank memory */
    public static final int MWA_BANK16 =-25;	/* bank memory */

         
             //refactoring requiered from here and below

        public static int MHMASK(int abits) { return (0xffffffff >>> (32 - abits)); }

    
    public static final int MAX_BANKS		=16;
    
    /***************************************************************************

    IN and OUT ports are handled like memory accesses, the hook template is the
    same so you can interchange them. Of course there is no 'base' pointer for
    IO ports.

    ***************************************************************************/
    public static class IOReadPort
    {
        public IOReadPort(){}
        public IOReadPort(int s, int e, int h) { start = s; end = e; handler = h; };
        public IOReadPort(int s, int e, ReadHandlerPtr rhp) { start = s; end = e; handler = 1; _handler = rhp; };
        public IOReadPort(int s) { this(s, 0, null); };
        public int start,end;
        public int handler;
        public ReadHandlerPtr _handler;	/* see special values below */
    };

   public static final int IORP_NOP = 0;	/* don't care, return 0 */

        public static class IOWritePort
        {
            public IOWritePort(){}
            public IOWritePort(int s, int e, int h) { start = s; end = e; handler = h; };
            public IOWritePort(int s, int e, WriteHandlerPtr whp) { start = s; end = e; handler = 1; _handler = whp; };
            public IOWritePort(int s) { this(s, 0, null); };
            public int start,end;
            public int handler;
            public WriteHandlerPtr _handler;	/* see special values below */
        };

        public static final int IOWP_NOP = 0;	/* do nothing */
        
        
/* memory element block size */
public static final int MH_SBITS		=8;			/* sub element bank size */
public static final int MH_PBITS		=8;			/* port current element size */
public static final int MH_ELEMAX		=64;			/* sub elements limit */
public static final int MH_HARDMAX		=64;			/* hardware functions limit */

/* 16 bits address */
public static final int ABITS1_16		=12;
public static final int ABITS2_16		=4;
public static final int ABITS_MIN_16	=0;			/* minimum memory block is 1 byte */
/* 16 bits address (little endian word access) */
public static final int ABITS1_16LEW	=12;
public static final int ABITS2_16LEW	=3;
public static final int ABITS_MIN_16LEW	=1;			/* minimum memory block is 2 bytes */
/* 16 bits address (big endian word access) */
public static final int ABITS1_16BEW	=12;
public static final int ABITS2_16BEW	=3;
public static final int ABITS_MIN_16BEW	=1;			/* minimum memory block is 2 bytes */    
/* 20 bits address */
public static final int ABITS1_20		=12;
public static final int ABITS2_20		=8;
public static final int ABITS_MIN_20	=0;			/* minimum memory block is 1 byte */
/* 21 bits address */
public static final int ABITS1_21		=13;
public static final int ABITS2_21		=8;
public static final int ABITS_MIN_21	=0;			/* minimum memory block is 1 byte */
/* 24 bits address (word access) */
public static final int ABITS1_24		=15;
public static final int ABITS2_24		=8;
public static final int ABITS_MIN_24	=1;			/* minimum memory block is 2 bytes */
/* 29 bits address (dword access) */
public static final int ABITS1_29		=19;
public static final int ABITS2_29		=8;
public static final int ABITS_MIN_29	=2;			/* minimum memory block is 4 bytes */
/* 32 bits address (dword access) */
public static final int ABITS1_32		=23;
public static final int ABITS2_32		=8;
public static final int ABITS_MIN_32	=1;			/* minimum memory block is 2 bytes */
public static final int MAX_EXT_MEMORY = 64;        
public static class ExtMemory{
            int start, end,region;
            UBytePtr data;
        }

/* ----- 16-bit memory accessing ----- */

/*public static final int READ_WORD(a)          (*(UINT16 *)(a))
public static final int WRITE_WORD(a,d)       (*(UINT16 *)(a) = (d))
public static final int COMBINE_WORD(w,d)     (((w) & ((d) >> 16)) | ((d) & 0xffff))
public static final int COMBINE_WORD_MEM(a,d) (WRITE_WORD((a), (READ_WORD(a) & ((d) >> 16)) | (d)))*/
        public static void COMBINE_WORD_MEM(UBytePtr a, int offset,int d)
        {
            a.WRITE_WORD(offset, (a.READ_WORD(offset) & ((d) >> 16) | d));
            
        }
        public static int COMBINE_WORD(int w, int d)
        {
            return (((w) & ((d) >> 16)) | ((d) & 0xffff));
        }

/* ----- opcode reading ----- */
public static char cpu_readop(int A) 
{ 
    return OP_ROM.read(A); 
}
//#define cpu_readop16(A)		READ_WORD(&OP_ROM[A])
public static int cpu_readop16(int A)
{
    return OP_ROM.READ_WORD(A);
}
public static char cpu_readop_arg(int A) 
{ 
    return OP_RAM.read(A); 
}

//#define cpu_readop_arg16(A)	READ_WORD(&OP_RAM[A])


/* ----- bank switching for CPU cores ----- */
public static void change_pc_generic(int pc, int abits2, int abitsmin, int shift, setopbase setop)
{
    if (cur_mrhard[pc >> (abits2 + abitsmin + shift)] != ophw.read())
        setop.handler((int)pc);
}
public static void change_pc(int pc)
{
     change_pc_generic(pc, ABITS2_16, ABITS_MIN_16, 0, cpu_setOPbase16);
}
public static void change_pc16(int pc)
{
     change_pc_generic(pc, ABITS2_16, ABITS_MIN_16, 0, cpu_setOPbase16);
}
public static void change_pc20(int pc)		
{
    change_pc_generic(pc, ABITS2_20, ABITS_MIN_20, 0, cpu_setOPbase20);
}

public static void change_pc24(int pc)		
{
    change_pc_generic(pc, ABITS2_24, ABITS_MIN_24, 0, cpu_setOPbase24);
}
 public static void cpu_setbank(int bank, UBytePtr _base)
        {
            if (bank >= 1 && bank <= MAX_BANKS)
            {
                cpu_bankbase[bank] = _base;
                if (ophw.read() == bank)
                {
                    ophw.set((char)0xff);
                    cpu_setOPbase16.handler(cpu_get_pc());
                }
            }
        }

public static abstract interface opbase_handlerPtr { public abstract int handler(int address); }
public static abstract interface setopbase { public abstract void handler(int pc);}
}
