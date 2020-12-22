
package gr.codebb.arcadeflex.v036.mame;

import static gr.codebb.arcadeflex.v037b7.mame.memory.*;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;

/**
 *
 *  temp file with old memory definitions to do some workarounds
 * 
 */
public class memoryH {

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

//#define cpu_readop16(A)		READ_WORD(&OP_ROM[A])
public static int cpu_readop16(int A)
{
    return OP_ROM.READ_WORD(A);
}

//#define cpu_readop_arg16(A)	READ_WORD(&OP_RAM[A])

}
