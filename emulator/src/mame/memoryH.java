
package mame;

import arcadeflex.libc_old.CharPtr;
import mame.driverH.ReadHandlerPtr;
import mame.driverH.WriteHandlerPtr;
import arcadeflex.libc.*;

/**
 *
 *  temp file with old memory definitions to do some workarounds
 * 
 */
public class memoryH {
    

     public static class MemoryReadAddress
     {
        public MemoryReadAddress(int s, int e, int h, CharPtr b, int[] size){ this.start = s; this.end = e; this.handler = h; this.base = b; this.size = size; }
        public MemoryReadAddress(int s, int e, ReadHandlerPtr rhp, CharPtr b, int[] size) { this.start = s; this.end = e; this.handler = 1; this._handler = rhp; this.base = b; this.size = size; }
        public MemoryReadAddress(int s, int e, int h, CharPtr b) { start = s; end = e; handler = h; base = b; };
        public MemoryReadAddress(int s, int e, int h) { this(s, e, h, null); };
        public MemoryReadAddress(int s, int e, ReadHandlerPtr rhp, CharPtr b) { start = s; end = e; handler = 1; _handler = rhp; base = b; };
        public MemoryReadAddress(int s, int e, ReadHandlerPtr rhp) { this(s, e, rhp, null); };
        public MemoryReadAddress(int s) { this(s, -1, null); };
        public int start,end;
        public int handler;
        public ReadHandlerPtr _handler;	/* see special values below */
        public CharPtr base;
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
        public MemoryWriteAddress(int s, int e, int h, CharPtr b, int[] size){this.start = s; this.end = e; this.handler = h; this.base = b; this.size = size; }
        public MemoryWriteAddress(int s, int e, WriteHandlerPtr whp, CharPtr b, int[] size) { this.start = s; this.end = e; this.handler = 1; this._handler = whp; this.base = b; this.size = size; }
        public MemoryWriteAddress(int s, int e, int h, CharPtr b) { start = s; end = e; handler = h; base = b; };
        public MemoryWriteAddress(int s, int e, int h) { this(s, e, h, null); };
        public MemoryWriteAddress(int s, int e, WriteHandlerPtr whp, CharPtr b) { start = s; end = e; handler = 1; _handler = whp; base = b; };
        public MemoryWriteAddress(int s, int e, WriteHandlerPtr whp) { this(s, e, whp, null); };
        public MemoryWriteAddress(int s) { this(s, -1, null); };
        public int start,end;
        public int handler;
        public WriteHandlerPtr _handler;	/* see special values below */
        public CharPtr base;
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
        public IOReadPort(int s) { this(s, -1, null); };
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
            public IOWritePort(int s) { this(s, -1, null); };
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
public static final int MAX_EXT_MEMORY = 64;        
public static class ExtMemory{
            int start, end,region;
            UBytePtr data;
        }
public static abstract interface opbase_handlerPtr { public abstract int handler(int address); }
}
