
package mame;

import arcadeflex.libc.CharPtr;
import mame.driverH.ReadHandlerPtr;
import mame.driverH.WriteHandlerPtr;

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
        /* RAM[] and ROM[] are usually the same, but they aren't if the CPU opcodes are */
        /* encrypted. In such a case, opcodes are fetched from ROM[], and arguments from */
        /* RAM[]. If the program dynamically creates code in RAM and executes it, it */
        /* won't work unless writes to RAM affects both RAM[] and ROM[]. */
         public static final int MWA_RAMROM = -3;

    /***************************************************************************

    IN and OUT ports are handled like memory accesses, the hook template is the
    same so you can interchange them. Of course there is no 'base' pointer for
    IO ports.

    ***************************************************************************/
    public static class IOReadPort
    {
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
        
}
