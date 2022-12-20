package gr.codebb.arcadeflex.v036.mame;

import static arcadeflex.v036.mame.memoryH.ABITS2_20;
import static arcadeflex.v036.mame.memoryH.ABITS2_24;
import static arcadeflex.v036.mame.memoryH.ABITS_MIN_20;
import static arcadeflex.v036.mame.memoryH.ABITS_MIN_24;
import static arcadeflex.v036.mame.memoryH.change_pc_generic;
import static arcadeflex.v036.mame.memory.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.memory.cpu_setOPbase20;
import static gr.codebb.arcadeflex.v036.mame.memory.cpu_setOPbase24;

/**
 *
 * temp file with old memory definitions to do some workarounds
 *
 */
public class memoryH {

    /* ----- 16-bit memory accessing ----- */

 /*public static final int READ_WORD(a)          (*(UINT16 *)(a))
public static final int WRITE_WORD(a,d)       (*(UINT16 *)(a) = (d))
public static final int COMBINE_WORD(w,d)     (((w) & ((d) >> 16)) | ((d) & 0xffff))
public static final int COMBINE_WORD_MEM(a,d) (WRITE_WORD((a), (READ_WORD(a) & ((d) >> 16)) | (d)))*/
    public static void COMBINE_WORD_MEM(UBytePtr a, int offset, int d) {
        a.WRITE_WORD(offset, (a.READ_WORD(offset) & ((d) >> 16) | d));

    }

    public static int COMBINE_WORD(int w, int d) {
        return (((w) & ((d) >> 16)) | ((d) & 0xffff));
    }

    /* ----- opcode reading ----- */
//#define cpu_readop16(A)		READ_WORD(&OP_ROM[A])
    public static int cpu_readop16(int A) {
        return (OP_ROM.READ_WORD(A));
    }

//#define cpu_readop_arg16(A)	READ_WORD(&OP_RAM[A])
    public static void change_pc24(int pc) {
        change_pc_generic(pc, ABITS2_24, ABITS_MIN_24, 0, cpu_setOPbase24);
    }
    public static void change_pc20(int pc){
        change_pc_generic(pc, ABITS2_20, ABITS_MIN_20, 0, cpu_setOPbase20);
    }
}
