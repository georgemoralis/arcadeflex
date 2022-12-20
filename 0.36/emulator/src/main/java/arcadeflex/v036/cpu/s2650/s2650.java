/*
 * ported to 0.36 opcode fixes up to 0.37b7
 */
package arcadeflex.v036.cpu.s2650;

//cpu imports
import static arcadeflex.v036.cpu.s2650.s2650H.*;
import static arcadeflex.v036.cpu.s2650.s2650cpuH.*;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.memory.*;

public class s2650 extends cpu_interface {

    static int[] s2650_ICount = new int[1];

    public s2650() {
        cpu_num = CPU_S2650;
        num_irqs = 2;
        default_vector = 0;
        overclock = 1.0;
        no_int = S2650_INT_NONE;
        irq_int = -1;
        nmi_int = -1;
        address_shift = 0;
        address_bits = 15;
        endianess = CPU_IS_LE;
        align_unit = 1;
        max_inst_len = 3;
        abits1 = ABITS1_16;
        abits2 = ABITS2_16;
        abitsmin = ABITS_MIN_16;
        icount = s2650_ICount;
        icount[0] = 0;
    }

    public static class s2650_Regs {

        char ppc;/* previous program counter (page + iar) */
        char page;/* 8K page select register (A14..A13) */
        char iar;/* instruction address register (A12..A0) */
        char ea;/* effective address (A14..A0) */
        char u8_psl;/* processor status lower */
        char u8_psu;/* processor status upper */
        char u8_r;/* absolute addressing dst/src register */
        char[] u8_reg = new char[7];/* 7 general purpose registers */
        char u8_halt;/* 1 if cpu is halted */
        char u8_ir;/* instruction register */
        char[] ras = new char[8];/* 8 return address stack entries */
        char u8_irq_state;
        public IrqCallbackHandlerPtr irq_callback;
    }

    static s2650_Regs S = new s2650_Regs();

    /* condition code changes for a byte */
    static int ccc[] = {
        0x00, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40,
        0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40,
        0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40,
        0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40,
        0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40,
        0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40,
        0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40,
        0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40,
        0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80,
        0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80,
        0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80,
        0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80,
        0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80,
        0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80,
        0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80,
        0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80,
        0x04, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44,
        0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44,
        0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44,
        0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44,
        0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44,
        0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44,
        0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44,
        0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44,
        0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84,
        0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84,
        0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84,
        0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84,
        0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84,
        0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84,
        0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84,
        0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84, 0x84,};

    public static void CHECK_IRQ_LINE() {
        if (S.u8_irq_state != CLEAR_LINE) {
            if ((S.u8_psu & II) == 0) {
                int vector;
                if (S.u8_halt != 0) {
                    S.u8_halt = 0;
                    S.iar = (char) ((S.iar + 1) & PMSK);
                }
                vector = (S.irq_callback).handler(0) & 0xff;
                /* build effective address within first 8K page */
                S.ea = (char) (S2650_relative[vector] & PMSK);
                if ((vector & 0x80) != 0) /* indirect bit set ? */ {
                    int addr = S.ea;
                    s2650_ICount[0] -= 2;
                    /* build indirect 32K address */
                    S.ea = (char) (RDMEM(addr) << 8);
                    if ((++addr & PMSK) == 0) {
                        addr -= PLEN;
                    }
                    S.ea = (char) ((S.ea + RDMEM(addr)) & AMSK);
                }
                //LOG(("S2650 interrupt to $%04x\n", S.ea));
                S.u8_psu = (char) (((S.u8_psu & ~SP) | ((S.u8_psu + 1) & SP) | II) & 0xFF);
                S.ras[S.u8_psu & SP] = (char) (S.page + S.iar);
                S.page = (char) (S.ea & PAGE);
                S.iar = (char) (S.ea & PMSK);
            }
        }
    }

    /**
     * *************************************************************
     *
     * set condition code (zero,plus,minus) from result
     * *************************************************************
     */
    public static void SET_CC(int result) {
        S.u8_psl = (char) (((S.u8_psl & ~CC) | ccc[result]) & 0xFF);
    }

    /**
     * *************************************************************
     *
     * set condition code (zero,plus,minus) and overflow
     * *************************************************************
     */
    public static void SET_CC_OVF(int result, int value) {
        S.u8_psl = (char) (((S.u8_psl & ~(OVF + CC))
                | ccc[result + (((result ^ value) << 1) & 256)]) & 0xFF);
    }

    /**
     * *************************************************************
     * ROP read next opcode
     * *************************************************************
     */
    public static char ROP() {
        char result = (char) ((cpu_readop(S.page + S.iar)) & 0xFF);
        S.iar = (char) ((S.iar + 1) & PMSK);
        return result;
    }

    /**
     * *************************************************************
     * ARG read next opcode argument
     * *************************************************************
     */
    public static char ARG() {
        char result = (char) ((cpu_readop_arg(S.page + S.iar)) & 0xFF);
        S.iar = (char) ((S.iar + 1) & PMSK);
        return result;
    }

    /**
     * *************************************************************
     * RDMEM read memory byte from addr
     * *************************************************************
     */
    public static char RDMEM(int addr) {
        return (char) ((cpu_readmem16(addr) & 0xFF));
    }

    /**
     * *************************************************************
     * handy table to build PC relative offsets from HR (holding register)
     * *************************************************************
     */
    static int S2650_relative[]
            = {
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
                16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
                32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
                48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63,
                -64, -63, -62, -61, -60, -59, -58, -57, -56, -55, -54, -53, -52, -51, -50, -49,
                -48, -47, -46, -45, -44, -43, -42, -41, -40, -39, -38, -37, -36, -35, -34, -33,
                -32, -31, -30, -29, -28, -27, -26, -25, -24, -23, -22, -21, -20, -19, -18, -17,
                -16, -15, -14, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1,
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
                16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
                32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
                48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63,
                -64, -63, -62, -61, -60, -59, -58, -57, -56, -55, -54, -53, -52, -51, -50, -49,
                -48, -47, -46, -45, -44, -43, -42, -41, -40, -39, -38, -37, -36, -35, -34, -33,
                -32, -31, -30, -29, -28, -27, -26, -25, -24, -23, -22, -21, -20, -19, -18, -17,
                -16, -15, -14, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1,};

    /**
     * *************************************************************
     * _REL_EA build effective address with relative addressing
     * *************************************************************
     */
    public static void REL_EA(char page) {
        char/*UINT8*/ hr = ARG();
        /* get 'holding register' */
 /* build effective address within current 8K page */
        S.ea = (char) (page + ((S.iar + S2650_relative[hr]) & PMSK));
        if ((hr & 0x80) != 0) {
            /* indirect bit set ? */
            int addr = S.ea;
            s2650_ICount[0] -= 2;
            /* build indirect 32K address */
            S.ea = (char) (RDMEM(addr) << 8);
            if ((++addr & PMSK) == 0) {
                addr -= PLEN;
                /* page wrap */
            }
            S.ea = (char) ((S.ea + RDMEM(addr)) & AMSK);
        }
    }

    /**
     * *************************************************************
     * _REL_ZERO build effective address with zero relative addressing
     * *************************************************************
     */
    public static void REL_ZERO(char page) {
        char hr = ARG();
        /* get 'holding register' */
 /* build effective address from 0 */
        S.ea = (char) (S2650_relative[hr] & PMSK);
        if ((hr & 0x80) != 0) {
            /* indirect bit set ? */
            int addr = S.ea;
            s2650_ICount[0] -= 2;
            /* build indirect 32K address */
            S.ea = (char) (RDMEM(addr) << 8);
            if ((++addr & PMSK) == 0) {
                addr -= PLEN;
                /* page wrap */
            }
            S.ea = (char) ((S.ea + RDMEM(addr)) & AMSK);
        }
    }

    /**
     * *************************************************************
     * _ABS_EA build effective address with absolute addressing
     * *************************************************************
     */
    public static void ABS_EA() {
        char/*UINT8*/ hr, dr;
        hr = ARG();
        /* get 'holding register' */
        dr = ARG();
        /* get 'data bus register' */
 /* build effective address within current 8K page */
        S.ea = (char) (S.page + (((hr << 8) + dr) & PMSK));
        /* indirect addressing ? */
        if ((hr & 0x80) != 0) {
            int addr = S.ea;
            s2650_ICount[0] -= 2;
            /* build indirect 32K address */
 /* build indirect 32K address */
            S.ea = (char) (RDMEM(addr) << 8);
            if ((++addr & PMSK) == 0) {
                addr -= PLEN;
                /* page wrap */
            }
            S.ea = (char) ((S.ea + RDMEM(addr)) & AMSK);
        }
        /* check indexed addressing modes */
        switch (hr & 0x60) {
            case 0x00:
                /* not indexed */
                break;
            case 0x20:
                /* auto increment indexed */
                S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] + 1) & 0xFF);
                S.ea = (char) ((S.ea & PAGE) + ((S.ea + S.u8_reg[S.u8_r]) & PMSK));
                S.u8_r = 0;
                /* absolute addressing reg is R0 */
                break;
            case 0x40:
                /* auto decrement indexed */
                S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] - 1) & 0xFF);
                S.ea = (char) ((S.ea & PAGE) + ((S.ea + S.u8_reg[S.u8_r]) & PMSK));
                S.u8_r = 0;
                /* absolute addressing reg is R0 */
                break;
            case 0x60:
                /* indexed */
                S.ea = (char) ((S.ea & PAGE) + ((S.ea + S.u8_reg[S.u8_r]) & PMSK));
                S.u8_r = 0;
                /* absolute addressing reg is R0 */
                break;
        }
    }

    /**
     * *************************************************************
     * _BRA_EA build effective address with absolute addressing (branch)
     * *************************************************************
     */
    public static void BRA_EA() {
        char/*UINT8*/ hr, dr;
        hr = ARG();
        /* get 'holding register' */
        dr = ARG();
        /* get 'data bus register' */
 /* build address in 32K address space */
        S.ea = (char) (((hr << 8) + dr) & AMSK);
        /* indirect addressing ? */
        if ((hr & 0x80) != 0) {
            int addr = S.ea;
            s2650_ICount[0] -= 2;
            /* build indirect 32K address */
            S.ea = (char) (RDMEM(addr) << 8);
            if ((++addr & PMSK) == 0) {
                addr -= PLEN;
                /* page wrap */
            }
            S.ea = (char) ((S.ea + RDMEM(addr)) & AMSK);
        }
    }

    /**
     * *************************************************************
     * SWAP_REGS Swap registers r1-r3 with r4-r6 (the second set) This is done
     * everytime the RS bit in PSL changes
     * *************************************************************
     */
    public static void SWAP_REGS() {
        char/*UINT8*/ tmp;
        tmp = S.u8_reg[1];
        S.u8_reg[1] = S.u8_reg[4];
        S.u8_reg[4] = tmp;
        tmp = S.u8_reg[2];
        S.u8_reg[2] = S.u8_reg[5];
        S.u8_reg[5] = tmp;
        tmp = S.u8_reg[3];
        S.u8_reg[3] = S.u8_reg[6];
        S.u8_reg[6] = tmp;
    }

    /**
     * *************************************************************
     * M_BRR Branch relative if cond is true
     * *************************************************************
     */
    public static void M_BRR(boolean cond) {
        if (cond) {
            REL_EA(S.page);
            S.page = (char) (S.ea & PAGE);
            S.iar = (char) (S.ea & PMSK);
            change_pc(S.ea);
        } else {
            S.iar = (char) ((S.iar + 1) & PMSK);
        }
    }

    /**
     * *************************************************************
     * M_ZBRR Branch relative to page zero
     * *************************************************************
     */
    public static void M_ZBRR() {
        REL_ZERO((char) 0);
        S.page = (char) (S.ea & PAGE);
        S.iar = (char) (S.ea & PMSK);
        change_pc(S.ea);
    }

    /**
     * *************************************************************
     * M_BRA Branch absolute if cond is true
     * *************************************************************
     */
    public static void M_BRA(boolean cond) {
        if (cond) {
            BRA_EA();
            S.page = (char) (S.ea & PAGE);
            S.iar = (char) (S.ea & PMSK);
            change_pc(S.ea);
        } else {
            S.iar = (char) ((S.iar + 2) & PMSK);
        }
    }

    /**
     * *************************************************************
     * M_BXA Branch indexed absolute (EA + R3)
     * *************************************************************
     */
    public static void M_BXA() {
        BRA_EA();
        S.ea = (char) ((S.ea + S.u8_reg[3]) & AMSK);
        S.page = (char) (S.ea & PAGE);
        S.iar = (char) (S.ea & PMSK);
        change_pc(S.ea);
    }

    /**
     * *************************************************************
     * M_BSR Branch to subroutine relative if cond is true
     * *************************************************************
     */
    public static void M_BSR(boolean cond) {
        if (cond) {
            REL_EA(S.page);
            S.u8_psu = (char) (((S.u8_psu & ~SP) | ((S.u8_psu + 1) & SP)) & 0xFF);
            S.ras[S.u8_psu & SP] = (char) (S.page + S.iar);
            S.page = (char) (S.ea & PAGE);
            S.iar = (char) (S.ea & PMSK);
            change_pc(S.ea);
        } else {
            S.iar = (char) ((S.iar + 1) & PMSK);
        }
    }

    /**
     * *************************************************************
     * M_ZBSR Branch to subroutine relative to page zero
     * *************************************************************
     */
    public static void M_ZBSR() {
        REL_ZERO((char) 0);
        S.u8_psu = (char) (((S.u8_psu & ~SP) | ((S.u8_psu + 1) & SP)) & 0xFF);
        S.ras[S.u8_psu & SP] = (char) (S.page + S.iar);
        S.page = (char) (S.ea & PAGE);
        S.iar = (char) (S.ea & PMSK);
        change_pc(S.ea);
    }

    /**
     * *************************************************************
     * M_BSA Branch to subroutine absolute
     * *************************************************************
     */
    public static void M_BSA(boolean cond) {
        if (cond) {
            BRA_EA();
            S.u8_psu = (char) (((S.u8_psu & ~SP) | ((S.u8_psu + 1) & SP)) & 0xFF);
            S.ras[S.u8_psu & SP] = (char) (S.page + S.iar);
            S.page = (char) (S.ea & PAGE);
            S.iar = (char) (S.ea & PMSK);
            change_pc(S.ea);
        } else {
            S.iar = (char) ((S.iar + 2) & PMSK);
        }
    }

    /**
     * *************************************************************
     * M_BSXA Branch to subroutine indexed absolute (EA + R3)
     * *************************************************************
     */
    public static void M_BSXA() {
        BRA_EA();
        S.ea = (char) ((S.ea + S.u8_reg[3]) & AMSK);
        S.u8_psu = (char) (((S.u8_psu & ~SP) | ((S.u8_psu + 1) & SP)) & 0xFF);
        S.ras[S.u8_psu & SP] = (char) (S.page + S.iar);
        S.page = (char) (S.ea & PAGE);
        S.iar = (char) (S.ea & PMSK);
        change_pc(S.ea);
    }

    /**
     * *************************************************************
     * M_RET Return from subroutine if cond is true
     * *************************************************************
     */
    public static void M_RET(boolean cond) {
        if (cond) {
            S.ea = S.ras[S.u8_psu & SP];
            S.u8_psu = (char) (((S.u8_psu & ~SP) | ((S.u8_psu - 1) & SP)) & 0xFF);
            S.page = (char) (S.ea & PAGE);
            S.iar = (char) (S.ea & PMSK);
            change_pc(S.ea);
        }
    }

    /**
     * *************************************************************
     * M_RETE Return from subroutine if cond is true and enable interrupts;
     * afterwards check IRQ line state and eventually take next interrupt
     * *************************************************************
     */
    public static void M_RETE(boolean cond) {
        if (cond) {
            S.ea = S.ras[S.u8_psu & SP];
            S.u8_psu = (char) (((S.u8_psu & ~SP) | ((S.u8_psu - 1) & SP)) & 0xFF);
            S.page = (char) (S.ea & PAGE);
            S.iar = (char) (S.ea & PMSK);
            change_pc(S.ea);
            S.u8_psu = (char) ((S.u8_psu & ~II) & 0xFF);
            CHECK_IRQ_LINE();
        }
    }

    /*TODO*////***************************************************************
/*TODO*/// * M_ADD
/*TODO*/// * Add source to destination
/*TODO*/// * Add with carry if WC flag of PSL is set
/*TODO*/// ***************************************************************/
/*TODO*///#define M_ADD(dest,source)										
/*TODO*///{																
/*TODO*///	char before = (char)((dest)&0xFF);										
/*TODO*///	/* add source; carry only if WC is set */					
/*TODO*///	dest = (char)((dest + source + ((S.u8_psl >>> 3) & S.u8_psl & C))&0xFF);			
/*TODO*///	S.u8_psl = (char)((S.u8_psl & ~(C | OVF | IDC))&0xFF);									
/*TODO*///	if( dest < before ) S.u8_psl = (char)((S.u8_psl | C)&0xFF); 							
/*TODO*///	if( (dest & 15) < (before & 15) ) S.u8_psl =(char)((S.u8_psl | IDC)&0xFF); 			
/*TODO*///	SET_CC_OVF(dest,before);									
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * M_SUB
/*TODO*/// * Subtract source from destination
/*TODO*/// * Subtract with borrow if WC flag of PSL is set
/*TODO*/// ***************************************************************/
/*TODO*///#define M_SUB(dest,source)										
/*TODO*///{																
/*TODO*///	char before = (char)((dest)&0xFF);;										
/*TODO*///	/* subtract source; borrow only if WC is set */ 			
/*TODO*///	dest = (char)((dest - source - ((S.u8_psl >>> 3) & (S.u8_psl ^ C) & C))&0xFF);	
/*TODO*///	S.u8_psl = (char)((S.u8_psl & ~(C | OVF | IDC))&0xFF);	
/*TODO*///	if( dest <= before ) S.u8_psl = (char)((S.u8_psl | C)&0xFF);							
/*TODO*///	if( (dest & 15) < (before & 15) ) S.u8_psl = (char)((S.u8_psl | IDC)&0xFF); 			
/*TODO*///	SET_CC_OVF(dest,before);									
/*TODO*///}
    /**
     * *************************************************************
     * M_SPSU Store processor status upper (PSU) to register R0
     * *************************************************************
     */
    public static void M_SPSU() {
        S.u8_reg[0] = (char) ((S.u8_psu & ~PSU34) & 0xFF);
        SET_CC(S.u8_reg[0]);
    }

    /**
     * *************************************************************
     * M_SPSL Store processor status lower (PSL) to register R0
     * *************************************************************
     */
    public static void M_SPSL() {
        S.u8_reg[0] = (char) (S.u8_psl & 0xFF);
        SET_CC(S.u8_reg[0]);
    }

    /**
     * *************************************************************
     * M_CPSU Clear processor status upper (PSU), selective
     * *************************************************************
     */
    public static void M_CPSU() {
        char/*UINT8*/ cpsu = ARG();
        S.u8_psu = (char) ((S.u8_psu & ~cpsu) & 0xFF);
        CHECK_IRQ_LINE();
    }

    /**
     * *************************************************************
     * M_CPSL Clear processor status lower (PSL), selective
     * *************************************************************
     */
    public static void M_CPSL() {
        char/*UINT8*/ cpsl = ARG();
        /* select other register set now ? */
        if ((cpsl & RS) != 0 && (S.u8_psl & RS) != 0) {
            SWAP_REGS();
        }
        S.u8_psl = (char) ((S.u8_psl & ~cpsl) & 0xFF);
        CHECK_IRQ_LINE();
    }

    /**
     * *************************************************************
     * M_PPSU Preset processor status upper (PSU), selective Unused bits 3 and 4
     * can't be set
     * *************************************************************
     */
    public static void M_PPSU() {
        char/*UINT8*/ ppsu = (char) ((ARG() & ~PSU34) & 0xFF);
        S.u8_psu = (char) ((S.u8_psu | ppsu) & 0xFF);
    }

    /**
     * *************************************************************
     * M_PPSL Preset processor status lower (PSL), selective
     * *************************************************************
     */
    public static void M_PPSL() {
        char/*UINT8*/ ppsl = ARG();
        /* select 2nd register set now ? */
        if ((ppsl & RS) != 0 && (S.u8_psl & RS) == 0) {
            SWAP_REGS();
        }
        S.u8_psl = (char) ((S.u8_psl | ppsl) & 0xFF);
    }

    /**
     * *************************************************************
     * M_TPSU Test processor status upper (PSU)
     * *************************************************************
     */
    public static void M_TPSU() {
        char/*UINT8*/ tpsu = ARG();
        S.u8_psl = (char) ((S.u8_psl & ~CC) & 0xFF);
        if ((S.u8_psu & tpsu) != tpsu) {
            S.u8_psl = (char) ((S.u8_psl | 0x80) & 0xFF);
        }
    }

    /**
     * *************************************************************
     * M_TPSL Test processor status lower (PSL)
     * *************************************************************
     */
    public static void M_TPSL() {
        char/*UINT8*/ tpsl = ARG();
        if ((S.u8_psl & tpsl) != tpsl) {
            S.u8_psl = (char) (((S.u8_psl & ~CC) | 0x80) & 0xFF);
        } else {
            S.u8_psl = (char) ((S.u8_psl & ~CC) & 0xFF);
        }
    }

    /**
     * *************************************************************
     * M_TMI Test under mask immediate
     * *************************************************************
     */
    public static void M_TMI(int value) {
        char/*UINT8*/ tmi = ARG();
        S.u8_psl = (char) ((S.u8_psl & ~CC) & 0xFF);
        if ((value & tmi) != tmi) {
            S.u8_psl = (char) ((S.u8_psl | 0x80) & 0xFF);
        }
    }

    public static void s2650_set_flag(int state) {
        if (state != 0) {
            S.u8_psu = (char) ((S.u8_psu | FO) & 0xFF);
        } else {
            S.u8_psu = (char) ((S.u8_psu & ~FO) & 0xFF);
        }
    }

    public static int s2650_get_flag() {
        return (S.u8_psu & FO) != 0 ? 1 : 0;
    }

    public static void s2650_set_sense(int state) {
        if (state != 0) {
            S.u8_psu = (char) ((S.u8_psu | SI) & 0xFF);
        } else {
            S.u8_psu = (char) ((S.u8_psu & ~SI) & 0xFF);
        }
    }

    public static int s2650_get_sense() {
        return (S.u8_psu & SI) != 0 ? 1 : 0;
    }

    static int S2650_Cycles[] = {
        2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4,
        2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3,
        2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4,
        2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
        2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4,
        2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
        2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4,
        2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3,
        2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4,
        2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3,
        2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4,
        2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3,
        2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4,
        2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
        2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4,
        2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3
    };

    @Override
    public void reset(Object param) {
        //memset(&S, 0, sizeof(S));
        S.u8_psl = COM | WC;
        S.u8_psu = SI;
    }

    @Override
    public void exit() {
        /* nothing to do */
    }

    @Override
    public int execute(int cycles) {
        s2650_ICount[0] = cycles;
        do {
            S.ppc = (char) (S.page + S.iar);

            S.u8_ir = ROP();
            s2650_ICount[0] -= S2650_Cycles[S.u8_ir];
            S.u8_r = (char) ((S.u8_ir & 3) & 0xFF);
            /* register / value */
            switch (S.u8_ir) {
                case 0x00:
                /* LODZ,0 */
                case 0x01:
                /* LODZ,1 */
                case 0x02:
                /* LODZ,2 */
                case 0x03: /* LODZ,3 */ {
                    //M_LOD( R0, S.reg[S.r] );
                    S.u8_reg[0] = (char) ((S.u8_reg[S.u8_r]) & 0xFF);
                    SET_CC(S.u8_reg[0]);
                }
                break;

                case 0x04:
                /* LODI,0 v */
                case 0x05:
                /* LODI,1 v */
                case 0x06:
                /* LODI,2 v */
                case 0x07: /* LODI,3 v */ {
                    //M_LOD( S.reg[S.r], ARG() );
                    S.u8_reg[S.u8_r] = ARG();
                    SET_CC(S.u8_reg[S.u8_r]);
                }
                break;

                case 0x08:
                /* LODR,0 (*)a */
                case 0x09:
                /* LODR,1 (*)a */
                case 0x0a:
                /* LODR,2 (*)a */
                case 0x0b: /* LODR,3 (*)a */ {
                    REL_EA(S.page);
                    //M_LOD( S.reg[S.r], RDMEM(S.ea) );
                    S.u8_reg[S.u8_r] = RDMEM(S.ea);
                    SET_CC(S.u8_reg[S.u8_r]);
                }
                break;

                case 0x0c:
                /* LODA,0 (*)a(,X) */
                case 0x0d:
                /* LODA,1 (*)a(,X) */
                case 0x0e:
                /* LODA,2 (*)a(,X) */
                case 0x0f: /* LODA,3 (*)a(,X) */ {
                    ABS_EA();
                    //M_LOD( S.reg[S.r], RDMEM(S.ea) );
                    S.u8_reg[S.u8_r] = RDMEM(S.ea);
                    SET_CC(S.u8_reg[S.u8_r]);
                }
                break;

                case 0x10:
                /* illegal */
                case 0x11:
                    /* illegal */
                    break;
                case 0x12:
                    /* SPSU */
                    M_SPSU();
                    break;
                case 0x13:
                    /* SPSL */
                    M_SPSL();
                    break;

                case 0x14:
                /* RETC,0	(zero)	*/
                case 0x15:
                /* RETC,1	(plus)	*/
                case 0x16:
                    /* RETC,2	(minus) */
                    M_RET((S.u8_psl >>> 6) == S.u8_r);
                    break;
                case 0x17:
                    /* RETC,3	(always) */
                    M_RET(true);
                    break;

                case 0x18:
                /* BCTR,0  (*)a */
                case 0x19:
                /* BCTR,1  (*)a */
                case 0x1a:
                    /* BCTR,2  (*)a */
                    M_BRR((S.u8_psl >>> 6) == S.u8_r);
                    break;
                case 0x1b:
                    /* BCTR,3  (*)a */
                    M_BRR(true);
                    break;

                case 0x1c:
                /* BCTA,0  (*)a */
                case 0x1d:
                /* BCTA,1  (*)a */
                case 0x1e:
                    /* BCTA,2  (*)a */
                    M_BRA((S.u8_psl >>> 6) == S.u8_r);
                    break;
                case 0x1f:
                    /* BCTA,3  (*)a */
                    M_BRA(true);
                    break;

                case 0x20:
                /* EORZ,0 */
                case 0x21:
                /* EORZ,1 */
                case 0x22:
                /* EORZ,2 */
                case 0x23: /* EORZ,3 */ {
                    //M_EOR( R0, S.reg[S.r] );
                    S.u8_reg[0] = (char) ((S.u8_reg[0] ^ S.u8_reg[S.u8_r]) & 0xFF);
                    SET_CC(S.u8_reg[0]);
                }
                break;

                case 0x24:
                /* EORI,0 v */
                case 0x25:
                /* EORI,1 v */
                case 0x26:
                /* EORI,2 v */
                case 0x27: /* EORI,3 v */ {
                    //M_EOR( S.reg[S.r], ARG() );
                    S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] ^ ARG()) & 0xFF);
                    SET_CC(S.u8_reg[S.u8_r]);
                }
                break;

                case 0x28:
                /* EORR,0 (*)a */
                case 0x29:
                /* EORR,1 (*)a */
                case 0x2a:
                /* EORR,2 (*)a */
                case 0x2b: /* EORR,3 (*)a */ {
                    REL_EA(S.page);
                    //M_EOR( S.reg[S.r], RDMEM(S.ea) );
                    S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] ^ RDMEM(S.ea)) & 0xFF);
                    SET_CC(S.u8_reg[S.u8_r]);
                }
                break;

                case 0x2c:
                /* EORA,0 (*)a(,X) */
                case 0x2d:
                /* EORA,1 (*)a(,X) */
                case 0x2e:
                /* EORA,2 (*)a(,X) */
                case 0x2f: /* EORA,3 (*)a(,X) */ {
                    ABS_EA();
                    //M_EOR( S.reg[S.r], RDMEM(S.ea) );
                    S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] ^ RDMEM(S.ea)) & 0xFF);
                    SET_CC(S.u8_reg[S.u8_r]);
                }
                break;

                case 0x30:
                /* REDC,0 */
                case 0x31:
                /* REDC,1 */
                case 0x32:
                /* REDC,2 */
                case 0x33:
                    /* REDC,3 */
                    S.u8_reg[S.u8_r] = (char) ((cpu_readport(S2650_CTRL_PORT)) & 0xFF);
                    SET_CC(S.u8_reg[S.u8_r]);
                    break;

                case 0x34:
                /* RETE,0 */
                case 0x35:
                /* RETE,1 */
                case 0x36:
                    /* RETE,2 */
                    M_RETE((S.u8_psl >>> 6) == S.u8_r);
                    break;
                case 0x37:
                    /* RETE,3 */
                    M_RETE(true);
                    break;

                case 0x38:
                /* BSTR,0 (*)a */
                case 0x39:
                /* BSTR,1 (*)a */
                case 0x3a:
                    /* BSTR,2 (*)a */
                    M_BSR((S.u8_psl >>> 6) == S.u8_r);
                    break;
                case 0x3b:
                    /* BSTR,R3 (*)a */
                    M_BSR(true);
                    break;

                case 0x3c:
                /* BSTA,0 (*)a */
                case 0x3d:
                /* BSTA,1 (*)a */
                case 0x3e:
                    /* BSTA,2 (*)a */
                    M_BSA((S.u8_psl >> 6) == S.u8_r);
                    break;
                case 0x3f:
                    /* BSTA,3 (*)a */
                    M_BSA(true);
                    break;

                case 0x40: /* HALT */ {
                    S.iar = (char) ((S.iar - 1) & PMSK);
                    S.u8_halt = 1;
                    if (s2650_ICount[0] > 0) {
                        s2650_ICount[0] = 0;
                    }
                }
                break;
                case 0x41:
                /* ANDZ,1 */
                case 0x42:
                /* ANDZ,2 */
                case 0x43: /* ANDZ,3 */ {
                    //M_AND( R0, S.reg[S.r] );
                    S.u8_reg[0] = (char) ((S.u8_reg[0] & S.u8_reg[S.u8_r]) & 0xFF);
                    SET_CC(S.u8_reg[0]);
                }
                break;

                case 0x44:
                /* ANDI,0 v */
                case 0x45:
                /* ANDI,1 v */
                case 0x46:
                /* ANDI,2 v */
                case 0x47: /* ANDI,3 v */ {
                    //M_AND( S.reg[S.r], ARG() );
                    S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] & ARG()) & 0xFF);
                    SET_CC(S.u8_reg[S.u8_r]);
                }
                break;

                case 0x48:
                /* ANDR,0 (*)a */
                case 0x49:
                /* ANDR,1 (*)a */
                case 0x4a:
                /* ANDR,2 (*)a */
                case 0x4b: /* ANDR,3 (*)a */ {
                    REL_EA(S.page);
                    //M_AND( S.reg[S.r], RDMEM(S.ea) );
                    S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] & RDMEM(S.ea)) & 0xFF);
                    SET_CC(S.u8_reg[S.u8_r]);
                }
                break;

                case 0x4c:
                /* ANDA,0 (*)a(,X) */
                case 0x4d:
                /* ANDA,1 (*)a(,X) */
                case 0x4e:
                /* ANDA,2 (*)a(,X) */
                case 0x4f: /* ANDA,3 (*)a(,X) */ {
                    ABS_EA();
                    //M_AND( S.reg[S.r], RDMEM(S.ea) );
                    S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] & RDMEM(S.ea)) & 0xFF);
                    SET_CC(S.u8_reg[S.u8_r]);
                }
                break;

                case 0x50:
                /* RRR,0 */
                case 0x51:
                /* RRR,1 */
                case 0x52:
                /* RRR,2 */
                case 0x53: /* RRR,3 */ {
                    //M_RRR( S.reg[S.r] );
                    char before = (char) ((S.u8_reg[S.u8_r]) & 0xFF);
                    if ((S.u8_psl & WC) != 0) {
                        char c = (char) ((S.u8_psl & C) & 0xFF);
                        S.u8_psl = (char) ((S.u8_psl & ~(C + IDC)) & 0xFF);
                        S.u8_reg[S.u8_r] = (char) (((before >>> 1) | (c << 7)) & 0xFF);
                        S.u8_psl = (char) ((S.u8_psl | (before & C) + (S.u8_reg[S.u8_r] & IDC)) & 0xFF);
                    } else {
                        S.u8_reg[S.u8_r] = (char) (((before >>> 1) | (before << 7)) & 0xFF);
                    }
                    SET_CC_OVF(S.u8_reg[S.u8_r], before);
                }
                break;

                case 0x54:
                /* REDE,0 v */
                case 0x55:
                /* REDE,1 v */
                case 0x56:
                /* REDE,2 v */
                case 0x57:
                    /* REDE,3 v */
                    S.u8_reg[S.u8_r] = (char) ((cpu_readport(ARG())) & 0xFF);
                    SET_CC(S.u8_reg[S.u8_r]);
                    break;

                case 0x58:
                /* BRNR,0 (*)a */
                case 0x59:
                /* BRNR,1 (*)a */
                case 0x5a:
                /* BRNR,2 (*)a */
                case 0x5b:
                    /* BRNR,3 (*)a */
                    M_BRR(S.u8_reg[S.u8_r] != 0);
                    break;

                case 0x5c:
                /* BRNA,0 (*)a */
                case 0x5d:
                /* BRNA,1 (*)a */
                case 0x5e:
                /* BRNA,2 (*)a */
                case 0x5f:
                    /* BRNA,3 (*)a */
                    M_BRA(S.u8_reg[S.u8_r] != 0);
                    break;

                case 0x60:
                /* IORZ,0 */
                case 0x61:
                /* IORZ,1 */
                case 0x62:
                /* IORZ,2 */
                case 0x63: /* IORZ,3 */ {
                    //M_IOR( R0, S.reg[S.r] );
                    S.u8_reg[0] = (char) ((S.u8_reg[0] | S.u8_reg[S.u8_r]) & 0xFF);
                    SET_CC(S.u8_reg[0]);
                }
                break;

                case 0x64:
                /* IORI,0 v */
                case 0x65:
                /* IORI,1 v */
                case 0x66:
                /* IORI,2 v */
                case 0x67: /* IORI,3 v */ {
                    //M_IOR( S.reg[S.r], ARG() );
                    S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] | ARG()) & 0xFF);
                    SET_CC(S.u8_reg[S.u8_r]);
                }
                break;

                case 0x68:
                /* IORR,0 (*)a */
                case 0x69:
                /* IORR,1 (*)a */
                case 0x6a:
                /* IORR,2 (*)a */
                case 0x6b: /* IORR,3 (*)a */ {
                    REL_EA(S.page);
                    //M_IOR( S.reg[S. r],RDMEM(S.ea) );
                    S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] | RDMEM(S.ea)) & 0xFF);
                    SET_CC(S.u8_reg[S.u8_r]);
                }
                break;

                case 0x6c:
                /* IORA,0 (*)a(,X) */
                case 0x6d:
                /* IORA,1 (*)a(,X) */
                case 0x6e:
                /* IORA,2 (*)a(,X) */
                case 0x6f: /* IORA,3 (*)a(,X) */ {
                    ABS_EA();
                    //M_IOR( S.reg[S.r], RDMEM(S.ea) );
                    S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] | RDMEM(S.ea)) & 0xFF);
                    SET_CC(S.u8_reg[S.u8_r]);
                }
                break;

                case 0x70:
                /* REDD,0 */
                case 0x71:
                /* REDD,1 */
                case 0x72:
                /* REDD,2 */
                case 0x73: /* REDD,3 */ {
                    S.u8_reg[S.u8_r] = (char) ((cpu_readport(S2650_DATA_PORT)) & 0xFF);
                    SET_CC(S.u8_reg[S.u8_r]);
                }
                break;

                case 0x74:
                    /* CPSU */
                    M_CPSU();
                    break;
                case 0x75:
                    /* CPSL */
                    M_CPSL();
                    break;
                case 0x76:
                    /* PPSU */
                    M_PPSU();
                    break;
                case 0x77:
                    /* PPSL */
                    M_PPSL();
                    break;

                case 0x78:
                /* BSNR,0 (*)a */
                case 0x79:
                /* BSNR,1 (*)a */
                case 0x7a:
                /* BSNR,2 (*)a */
                case 0x7b:
                    /* BSNR,3 (*)a */
                    M_BSR(S.u8_reg[S.u8_r] != 0);
                    break;

                case 0x7c:
                /* BSNA,0 (*)a */
                case 0x7d:
                /* BSNA,1 (*)a */
                case 0x7e:
                /* BSNA,2 (*)a */
                case 0x7f:
                    /* BSNA,3 (*)a */
                    M_BSA(S.u8_reg[S.u8_r] != 0);
                    break;

                case 0x80:
                /* ADDZ,0 */
                case 0x81:
                /* ADDZ,1 */
                case 0x82:
                /* ADDZ,2 */
                case 0x83: /* ADDZ,3 */ {
                    //M_ADD(R0, S.reg[S.r]);
                    char before = (char) ((S.u8_reg[0]) & 0xFF);
                    /* add source; carry only if WC is set */
                    S.u8_reg[0] = (char) ((S.u8_reg[0] + S.u8_reg[S.u8_r] + ((S.u8_psl >>> 3) & S.u8_psl & C)) & 0xFF);
                    S.u8_psl = (char) ((S.u8_psl & ~(C | OVF | IDC)) & 0xFF);
                    if (S.u8_reg[0] < before) {
                        S.u8_psl = (char) ((S.u8_psl | C) & 0xFF);
                    }
                    if ((S.u8_reg[0] & 15) < (before & 15)) {
                        S.u8_psl = (char) ((S.u8_psl | IDC) & 0xFF);
                    }
                    SET_CC_OVF(S.u8_reg[0], before);
                }
                break;

                case 0x84:
                /* ADDI,0 v */
                case 0x85:
                /* ADDI,1 v */
                case 0x86:
                /* ADDI,2 v */
                case 0x87: /* ADDI,3 v */ {
                    //M_ADD( S.reg[S.r], ARG() );
                    char before = (char) ((S.u8_reg[S.u8_r]) & 0xFF);
                    /* add source; carry only if WC is set */
                    S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] + ARG() + ((S.u8_psl >>> 3) & S.u8_psl & C)) & 0xFF);
                    S.u8_psl = (char) ((S.u8_psl & ~(C | OVF | IDC)) & 0xFF);
                    if (S.u8_reg[S.u8_r] < before) {
                        S.u8_psl = (char) ((S.u8_psl | C) & 0xFF);
                    }
                    if ((S.u8_reg[S.u8_r] & 15) < (before & 15)) {
                        S.u8_psl = (char) ((S.u8_psl | IDC) & 0xFF);
                    }
                    SET_CC_OVF(S.u8_reg[S.u8_r], before);
                }
                break;

                case 0x88:
                /* ADDR,0 (*)a */
                case 0x89:
                /* ADDR,1 (*)a */
                case 0x8a:
                /* ADDR,2 (*)a */
                case 0x8b: /* ADDR,3 (*)a */ {
                    REL_EA(S.page);
                    //M_ADD(S.reg[S.r], RDMEM(S.ea));
                    char before = (char) ((S.u8_reg[S.u8_r]) & 0xFF);
                    /* add source; carry only if WC is set */
                    S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] + RDMEM(S.ea) + ((S.u8_psl >>> 3) & S.u8_psl & C)) & 0xFF);
                    S.u8_psl = (char) ((S.u8_psl & ~(C | OVF | IDC)) & 0xFF);
                    if (S.u8_reg[S.u8_r] < before) {
                        S.u8_psl = (char) ((S.u8_psl | C) & 0xFF);
                    }
                    if ((S.u8_reg[S.u8_r] & 15) < (before & 15)) {
                        S.u8_psl = (char) ((S.u8_psl | IDC) & 0xFF);
                    }
                    SET_CC_OVF(S.u8_reg[S.u8_r], before);
                }
                break;

                case 0x8c:
                /* ADDA,0 (*)a(,X) */
                case 0x8d:
                /* ADDA,1 (*)a(,X) */
                case 0x8e:
                /* ADDA,2 (*)a(,X) */
                case 0x8f: /* ADDA,3 (*)a(,X) */ {
                    ABS_EA();
                    //M_ADD( S.reg[S.r], RDMEM(S.ea) );
                    char before = (char) ((S.u8_reg[S.u8_r]) & 0xFF);
                    /* add source; carry only if WC is set */
                    S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] + RDMEM(S.ea) + ((S.u8_psl >>> 3) & S.u8_psl & C)) & 0xFF);
                    S.u8_psl = (char) ((S.u8_psl & ~(C | OVF | IDC)) & 0xFF);
                    if (S.u8_reg[S.u8_r] < before) {
                        S.u8_psl = (char) ((S.u8_psl | C) & 0xFF);
                    }
                    if ((S.u8_reg[S.u8_r] & 15) < (before & 15)) {
                        S.u8_psl = (char) ((S.u8_psl | IDC) & 0xFF);
                    }
                    SET_CC_OVF(S.u8_reg[S.u8_r], before);
                }
                break;
                case 0x90:
                /* illegal */
                case 0x91:
                    /* illegal */
                    break;
                case 0x92:
                    /* LPSU */
                    S.u8_psu = (char) ((S.u8_reg[0] & ~PSU34) & 0xFF);
                    break;
                case 0x93:
                    /* LPSL */
 /* change register set ? */
                    if (((S.u8_psl ^ S.u8_reg[0]) & RS) != 0) {
                        SWAP_REGS();
                    }
                    S.u8_psl = (char) (S.u8_reg[0] & 0xFF);
                    break;

                case 0x94:
                /* DAR,0 */
                case 0x95:
                /* DAR,1 */
                case 0x96:
                /* DAR,2 */
                case 0x97: /* DAR,3 */ {
                    //M_DAR( S.reg[S.r] );
                    if ((S.u8_psl & IDC) != 0) {
                        if ((S.u8_psl & C) == 0) {
                            S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] - 0x60) & 0xFF);
                        }
                    } else {
                        if ((S.u8_psl & C) != 0) {
                            S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] - 0x06) & 0xFF);
                        } else {
                            S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] - 0x66) & 0xFF);
                        }
                    }
                }
                break;

                case 0x98:
                /* BCFR,0 (*)a */
                case 0x99:
                /* BCFR,1 (*)a */
                case 0x9a:
                    /* BCFR,2 (*)a */
                    M_BRR((S.u8_psl >>> 6) != S.u8_r);
                    break;
                case 0x9b:
                    /* ZBRR    (*)a */
                    M_ZBRR();
                    break;

                case 0x9c:
                /* BCFA,0 (*)a */
                case 0x9d:
                /* BCFA,1 (*)a */
                case 0x9e:
                    /* BCFA,2 (*)a */
                    M_BRA((S.u8_psl >>> 6) != S.u8_r);
                    break;
                case 0x9f:
                    /* BXA	   (*)a */
                    M_BXA();
                    break;

                case 0xa0:
                /* SUBZ,0 */
                case 0xa1:
                /* SUBZ,1 */
                case 0xa2:
                /* SUBZ,2 */
                case 0xa3: /* SUBZ,3 */ {
                    //M_SUB( R0, S.reg[S.r] );
                    char before = (char) ((S.u8_reg[0]) & 0xFF);
                    /* subtract source; borrow only if WC is set */
                    S.u8_reg[0] = (char) ((S.u8_reg[0] - S.u8_reg[S.u8_r] - ((S.u8_psl >>> 3) & (S.u8_psl ^ C) & C)) & 0xFF);
                    S.u8_psl = (char) ((S.u8_psl & ~(C | OVF | IDC)) & 0xFF);
                    if (S.u8_reg[0] <= before) {
                        S.u8_psl = (char) ((S.u8_psl | C) & 0xFF);
                    }
                    if ((S.u8_reg[0] & 15) < (before & 15)) {
                        S.u8_psl = (char) ((S.u8_psl | IDC) & 0xFF);
                    }
                    SET_CC_OVF(S.u8_reg[0], before);
                }
                break;

                case 0xa4:
                /* SUBI,0 v */
                case 0xa5:
                /* SUBI,1 v */
                case 0xa6:
                /* SUBI,2 v */
                case 0xa7: /* SUBI,3 v */ {
                    //M_SUB( S.reg[S.r], ARG() );
                    char before = (char) ((S.u8_reg[S.u8_r]) & 0xFF);
                    /* subtract source; borrow only if WC is set */
                    S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] - ARG() - ((S.u8_psl >>> 3) & (S.u8_psl ^ C) & C)) & 0xFF);
                    S.u8_psl = (char) ((S.u8_psl & ~(C | OVF | IDC)) & 0xFF);
                    if (S.u8_reg[S.u8_r] <= before) {
                        S.u8_psl = (char) ((S.u8_psl | C) & 0xFF);
                    }
                    if ((S.u8_reg[S.u8_r] & 15) < (before & 15)) {
                        S.u8_psl = (char) ((S.u8_psl | IDC) & 0xFF);
                    }
                    SET_CC_OVF(S.u8_reg[S.u8_r], before);
                }
                break;

                case 0xa8:
                /* SUBR,0 (*)a */
                case 0xa9:
                /* SUBR,1 (*)a */
                case 0xaa:
                /* SUBR,2 (*)a */
                case 0xab: /* SUBR,3 (*)a */ {
                    REL_EA(S.page);

                    //M_SUB(S.reg[S.r], RDMEM(S.ea));
                    char before = (char) ((S.u8_reg[S.u8_r]) & 0xFF);
                    /* subtract source; borrow only if WC is set */
                    S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] - RDMEM(S.ea) - ((S.u8_psl >>> 3) & (S.u8_psl ^ C) & C)) & 0xFF);
                    S.u8_psl = (char) ((S.u8_psl & ~(C | OVF | IDC)) & 0xFF);
                    if (S.u8_reg[S.u8_r] <= before) {
                        S.u8_psl = (char) ((S.u8_psl | C) & 0xFF);
                    }
                    if ((S.u8_reg[S.u8_r] & 15) < (before & 15)) {
                        S.u8_psl = (char) ((S.u8_psl | IDC) & 0xFF);
                    }
                    SET_CC_OVF(S.u8_reg[S.u8_r], before);
                }
                break;

                case 0xac:
                /* SUBA,0 (*)a(,X) */
                case 0xad:
                /* SUBA,1 (*)a(,X) */
                case 0xae:
                /* SUBA,2 (*)a(,X) */
                case 0xaf: /* SUBA,3 (*)a(,X) */ {
                    ABS_EA();
                    //M_SUB( S.reg[S.r], RDMEM(S.ea) );
                    char before = (char) ((S.u8_reg[S.u8_r]) & 0xFF);
                    /* subtract source; borrow only if WC is set */
                    S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] - RDMEM(S.ea) - ((S.u8_psl >>> 3) & (S.u8_psl ^ C) & C)) & 0xFF);
                    S.u8_psl = (char) ((S.u8_psl & ~(C | OVF | IDC)) & 0xFF);
                    if (S.u8_reg[S.u8_r] <= before) {
                        S.u8_psl = (char) ((S.u8_psl | C) & 0xFF);
                    }
                    if ((S.u8_reg[S.u8_r] & 15) < (before & 15)) {
                        S.u8_psl = (char) ((S.u8_psl | IDC) & 0xFF);
                    }
                    SET_CC_OVF(S.u8_reg[S.u8_r], before);
                }
                break;

                case 0xb0:
                /* WRTC,0 */
                case 0xb1:
                /* WRTC,1 */
                case 0xb2:
                /* WRTC,2 */
                case 0xb3:
                    /* WRTC,3 */
                    cpu_writeport(S2650_CTRL_PORT, S.u8_reg[S.u8_r] & 0xFF);
                    break;

                case 0xb4:
                    /* TPSU */
                    M_TPSU();
                    break;
                case 0xb5:
                    /* TPSL */
                    M_TPSL();
                    break;
                case 0xb6:
                /* illegal */
                case 0xb7:
                    /* illegal */
                    break;

                case 0xb8:
                /* BSFR,0 (*)a */
                case 0xb9:
                /* BSFR,1 (*)a */
                case 0xba:
                    /* BSFR,2 (*)a */
                    M_BSR((S.u8_psl >>> 6) != S.u8_r);
                    break;
                case 0xbb:
                    /* ZBSR    (*)a */
                    M_ZBSR();
                    break;

                case 0xbc:
                /* BSFA,0 (*)a */
                case 0xbd:
                /* BSFA,1 (*)a */
                case 0xbe:
                    /* BSFA,2 (*)a */
                    M_BSA((S.u8_psl >>> 6) != S.u8_r);
                    break;
                case 0xbf:
                    /* BSXA    (*)a */
                    M_BSXA();
                    break;

                case 0xc0:
                    /* NOP */
                    break;
                case 0xc1:
                /* STRZ,1 */
                case 0xc2:
                /* STRZ,2 */
                case 0xc3: /* STRZ,3 */ {
                    //M_LOD( S.reg[S.r], R0 );
                    S.u8_reg[S.u8_r] = (char) (S.u8_reg[0] & 0xFF);
                    SET_CC(S.u8_reg[S.u8_r]);
                }
                break;

                case 0xc4:
                /* illegal */
                case 0xc5:
                /* illegal */
                case 0xc6:
                /* illegal */
                case 0xc7:
                    /* illegal */
                    break;

                case 0xc8:
                /* STRR,0 (*)a */
                case 0xc9:
                /* STRR,1 (*)a */
                case 0xca:
                /* STRR,2 (*)a */
                case 0xcb: /* STRR,3 (*)a */ {
                    REL_EA(S.page);
                    //M_STR( S.ea, S.reg[S.r] );
                    cpu_writemem16(S.ea & 0xFFFF, S.u8_reg[S.u8_r] & 0xFF);
                }
                break;

                case 0xcc:
                /* STRA,0 (*)a(,X) */
                case 0xcd:
                /* STRA,1 (*)a(,X) */
                case 0xce:
                /* STRA,2 (*)a(,X) */
                case 0xcf: /* STRA,3 (*)a(,X) */ {
                    ABS_EA();
                    //M_STR( S.ea, S.reg[S.r] );
                    cpu_writemem16(S.ea & 0xFFFF, S.u8_reg[S.u8_r] & 0xFF);
                }
                break;

                case 0xd0:
                /* RRL,0 */
                case 0xd1:
                /* RRL,1 */
                case 0xd2:
                /* RRL,2 */
                case 0xd3: /* RRL,3 */ {
                    //M_RRL( S.reg[S.r] );
                    char before = (char) (S.u8_reg[S.u8_r] & 0xFF);
                    if ((S.u8_psl & WC) != 0) {
                        char c = (char) ((S.u8_psl & C) & 0xFF);
                        S.u8_psl = (char) ((S.u8_psl & ~(C + IDC)) & 0xFF);
                        S.u8_reg[S.u8_r] = (char) (((before << 1) | c) & 0xFF);
                        S.u8_psl = (char) ((S.u8_psl | (before >>> 7) + (S.u8_reg[S.u8_r] & IDC)) & 0xFF);
                    } else {
                        S.u8_reg[S.u8_r] = (char) (((before << 1) | (before >>> 7)) & 0xFF);
                    }
                    SET_CC_OVF(S.u8_reg[S.u8_r], before);
                }
                break;

                case 0xd4:
                /* WRTE,0 v */
                case 0xd5:
                /* WRTE,1 v */
                case 0xd6:
                /* WRTE,2 v */
                case 0xd7:
                    /* WRTE,3 v */
                    cpu_writeport(ARG() & 0xFFFF, S.u8_reg[S.u8_r] & 0xFF);
                    break;

                case 0xd8:
                /* BIRR,0 (*)a */
                case 0xd9:
                /* BIRR,1 (*)a */
                case 0xda:
                /* BIRR,2 (*)a */
                case 0xdb: /* BIRR,3 (*)a */ {
                    //M_BRR( ++S.reg[S.r] );
                    S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] + 1) & 0xFF);
                    M_BRR(S.u8_reg[S.u8_r] != 0);
                }
                break;

                case 0xdc:
                /* BIRA,0 (*)a */
                case 0xdd:
                /* BIRA,1 (*)a */
                case 0xde:
                /* BIRA,2 (*)a */
                case 0xdf: /* BIRA,3 (*)a */ {
                    //M_BRA( ++S.reg[S.r] );
                    S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] + 1) & 0xFF);
                    M_BRA(S.u8_reg[S.u8_r] != 0);
                }
                break;

                case 0xe0:
                /* COMZ,0 */
                case 0xe1:
                /* COMZ,1 */
                case 0xe2:
                /* COMZ,2 */
                case 0xe3: /* COMZ,3 */ {
                    //M_COM(R0, S.reg[S.r]);
                    int d;
                    S.u8_psl = (char) ((S.u8_psl & ~CC) & 0xFF);
                    if ((S.u8_psl & COM) != 0) {
                        d = /*(UINT8)*/ S.u8_reg[0] - /*(UINT8)*/ S.u8_reg[S.u8_r];
                    } else {
                        d = (byte) S.u8_reg[0] - (byte) S.u8_reg[S.u8_r];
                    }
                    if (d < 0) {
                        S.u8_psl = (char) ((S.u8_psl | 0x80) & 0xFF);
                    } else if (d > 0) {
                        S.u8_psl = (char) ((S.u8_psl | 0x40) & 0xFF);
                    }
                }
                break;

                case 0xe4:
                /* COMI,0 v */
                case 0xe5:
                /* COMI,1 v */
                case 0xe6:
                /* COMI,2 v */
                case 0xe7: /* COMI,3 v */ {
                    //M_COM(S.reg[S.r], ARG());
                    int d;
                    S.u8_psl = (char) ((S.u8_psl & ~CC) & 0xFF);
                    if ((S.u8_psl & COM) != 0) {
                        d = /*(UINT8)*/ S.u8_reg[S.u8_r] - /*(UINT8)*/ ARG();
                    } else {
                        d = (byte) S.u8_reg[S.u8_r] - (byte) ARG();
                    }
                    if (d < 0) {
                        S.u8_psl = (char) ((S.u8_psl | 0x80) & 0xFF);
                    } else if (d > 0) {
                        S.u8_psl = (char) ((S.u8_psl | 0x40) & 0xFF);
                    }
                }
                break;

                case 0xe8:
                /* COMR,0 (*)a */
                case 0xe9:
                /* COMR,1 (*)a */
                case 0xea:
                /* COMR,2 (*)a */
                case 0xeb: /* COMR,3 (*)a */ {
                    REL_EA(S.page);
                    //M_COM( S.reg[S.r], RDMEM(S.ea) );
                    int d;
                    S.u8_psl = (char) ((S.u8_psl & ~CC) & 0xFF);
                    if ((S.u8_psl & COM) != 0) {
                        d = /*(UINT8)*/ S.u8_reg[S.u8_r] - /*(UINT8)*/ RDMEM(S.ea);
                    } else {
                        d = (byte) S.u8_reg[S.u8_r] - (byte) RDMEM(S.ea);
                    }
                    if (d < 0) {
                        S.u8_psl = (char) ((S.u8_psl | 0x80) & 0xFF);
                    } else if (d > 0) {
                        S.u8_psl = (char) ((S.u8_psl | 0x40) & 0xFF);
                    }
                }
                break;

                case 0xec:
                /* COMA,0 (*)a(,X) */
                case 0xed:
                /* COMA,1 (*)a(,X) */
                case 0xee:
                /* COMA,2 (*)a(,X) */
                case 0xef: /* COMA,3 (*)a(,X) */ {
                    ABS_EA();
                    //M_COM( S.reg[S.r], RDMEM(S.ea) );
                    int d;
                    S.u8_psl = (char) ((S.u8_psl & ~CC) & 0xFF);
                    if ((S.u8_psl & COM) != 0) {
                        d = /*(UINT8)*/ S.u8_reg[S.u8_r] - /*(UINT8)*/ RDMEM(S.ea);
                    } else {
                        d = (byte) S.u8_reg[S.u8_r] - (byte) RDMEM(S.ea);
                    }
                    if (d < 0) {
                        S.u8_psl = (char) ((S.u8_psl | 0x80) & 0xFF);
                    } else if (d > 0) {
                        S.u8_psl = (char) ((S.u8_psl | 0x40) & 0xFF);
                    }
                }
                break;
                case 0xf0:
                /* WRTD,0 */
                case 0xf1:
                /* WRTD,1 */
                case 0xf2:
                /* WRTD,2 */
                case 0xf3:
                    /* WRTD,3 */
                    cpu_writeport(S2650_DATA_PORT, S.u8_reg[S.u8_r] & 0xFF);
                    break;

                case 0xf4:
                /* TMI,0  v */
                case 0xf5:
                /* TMI,1  v */
                case 0xf6:
                /* TMI,2  v */
                case 0xf7:
                    /* TMI,3  v */
                    M_TMI(S.u8_reg[S.u8_r] & 0xFF);
                    break;

                case 0xf8:
                /* BDRR,0 (*)a */
                case 0xf9:
                /* BDRR,1 (*)a */
                case 0xfa:
                /* BDRR,2 (*)a */
                case 0xfb: /* BDRR,3 (*)a */ {
                    //M_BRR( --S.reg[S.r] );
                    S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] - 1) & 0xFF);
                    M_BRR(S.u8_reg[S.u8_r] != 0);
                }
                break;

                case 0xfc:
                /* BDRA,0 (*)a */
                case 0xfd:
                /* BDRA,1 (*)a */
                case 0xfe:
                /* BDRA,2 (*)a */
                case 0xff: /* BDRA,3 (*)a */ {
                    //M_BRA( --S.reg[S.r] );
                    S.u8_reg[S.u8_r] = (char) ((S.u8_reg[S.u8_r] - 1) & 0xFF);
                    M_BRA(S.u8_reg[S.u8_r] != 0);
                }
                break;
            }

        } while (s2650_ICount[0] > 0);

        return cycles - s2650_ICount[0];
    }

    @Override
    public Object get_context() {
        s2650_Regs regs = new s2650_Regs();

        regs.ppc = S.ppc;
        regs.page = S.page;
        regs.iar = S.iar;
        regs.ea = S.ea;
        regs.u8_psl = S.u8_psl;
        regs.u8_psu = S.u8_psu;
        regs.u8_r = S.u8_r;
        System.arraycopy(S.u8_reg, 0, regs.u8_reg, 0, 7);//char[] u8_reg = new char[7];
        regs.u8_halt = S.u8_halt;
        regs.u8_ir = S.u8_ir;
        System.arraycopy(S.ras, 0, regs.ras, 0, 8);//char[] ras = new char[8];
        regs.u8_irq_state = S.u8_irq_state;
        regs.irq_callback = S.irq_callback;
        return regs;
    }

    @Override
    public void set_context(Object reg) {
        s2650_Regs regs = (s2650_Regs) reg;
        S.ppc = regs.ppc;
        S.page = regs.page;
        S.iar = regs.iar;
        S.ea = regs.ea;
        S.u8_psl = regs.u8_psl;
        S.u8_psu = regs.u8_psu;
        S.u8_r = regs.u8_r;
        System.arraycopy(regs.u8_reg, 0, S.u8_reg, 0, 7);
        S.u8_halt = regs.u8_halt;
        S.u8_ir = regs.u8_ir;
        System.arraycopy(regs.ras, 0, S.ras, 0, 8);
        S.u8_irq_state = regs.u8_irq_state;
        S.irq_callback = regs.irq_callback;

        S.page = (char) (S.page & PAGE);
        S.iar = (char) (S.iar & PMSK);
        change_pc(S.page + S.iar);
    }

    @Override
    public int get_pc() {
        return S.page + S.iar;
    }

    public static int s2650_get_pc() {//same as previous needed for dkong driver
        return S.page + S.iar;
    }

    @Override
    public void set_pc(int val) {
        /*TODO*///	S.page = val & PAGE;
/*TODO*///	S.iar = val & PMSK;
/*TODO*///	change_pc(S.page + S.iar);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int get_sp() {
        /*TODO*///	return S.psu & SP;
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_sp(int val) {
        /*TODO*///        S.psu = (S.psu & ~SP) | (val & SP);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int get_reg(int regnum) {
        /*TODO*///	switch( regnum )
/*TODO*///	{
/*TODO*///		case S2650_PC: return S.page + S.iar;
/*TODO*///		case S2650_PS: return (S.psu << 8) | S.psl;
/*TODO*///		case S2650_R0: return S.reg[0];
/*TODO*///		case S2650_R1: return S.reg[1];
/*TODO*///		case S2650_R2: return S.reg[2];
/*TODO*///		case S2650_R3: return S.reg[3];
/*TODO*///		case S2650_R1A: return S.reg[4];
/*TODO*///		case S2650_R2A: return S.reg[5];
/*TODO*///		case S2650_R3A: return S.reg[6];
/*TODO*///		case S2650_HALT: return S.halt;
/*TODO*///		case S2650_IRQ_STATE: return S.irq_state;
/*TODO*///		case S2650_SI: return s2650_get_sense(); break;
/*TODO*///		case S2650_FO: return s2650_get_flag(); break;
/*TODO*///		case REG_PREVIOUSPC: return S.ppc; break;
/*TODO*///		default:
/*TODO*///			if( regnum <= REG_SP_CONTENTS )
/*TODO*///			{
/*TODO*///				unsigned offset = (REG_SP_CONTENTS - regnum);
/*TODO*///				if( offset < 8 )
/*TODO*///					return S.ras[offset];
/*TODO*///			}
/*TODO*///	}
/*TODO*///	return 0;
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_reg(int regnum, int val) {
        /*TODO*///	switch( regnum )
/*TODO*///	{
/*TODO*///		case S2650_PC: S.page = val & PAGE; S.iar = val & PMSK; break;
/*TODO*///		case S2650_PS: S.psl = val & 0xff; S.psu = val >> 8; break;
/*TODO*///		case S2650_R0: S.reg[0] = val; break;
/*TODO*///		case S2650_R1: S.reg[1] = val; break;
/*TODO*///		case S2650_R2: S.reg[2] = val; break;
/*TODO*///		case S2650_R3: S.reg[3] = val; break;
/*TODO*///		case S2650_R1A: S.reg[4] = val; break;
/*TODO*///		case S2650_R2A: S.reg[5] = val; break;
/*TODO*///		case S2650_R3A: S.reg[6] = val; break;
/*TODO*///		case S2650_HALT: S.halt = val; break;
/*TODO*///		case S2650_IRQ_STATE: s2650_set_irq_line(0, val); break;
/*TODO*///		case S2650_SI: s2650_set_sense(val); break;
/*TODO*///		case S2650_FO: s2650_set_flag(val); break;
/*TODO*///		default:
/*TODO*///			if( regnum <= REG_SP_CONTENTS )
/*TODO*///			{
/*TODO*///				unsigned offset = (REG_SP_CONTENTS - regnum);
/*TODO*///				if( offset < 8 )
/*TODO*///					S.ras[offset] = val;
/*TODO*///			}
/*TODO*///    }
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_nmi_line(int linestate) {
        /* no NMI line */
    }

    @Override
    public void set_irq_line(int irqline, int state) {
        if (irqline == 1) {
            if (state == CLEAR_LINE) {
                s2650_set_sense(0);
            } else {
                s2650_set_sense(1);
            }
            return;
        }
        S.u8_irq_state = (char) (state & 0xFF);
        CHECK_IRQ_LINE();
    }

    @Override
    public void set_irq_callback(IrqCallbackHandlerPtr callback) {
        S.irq_callback = callback;
    }

    @Override
    public void internal_interrupt(int type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cpu_state_save(Object file) {
        /*TODO*///	int cpu = cpu_getactivecpu();
/*TODO*///	state_save_UINT16(file,"s2650",cpu,"PAGE",&S.page,1);
/*TODO*///	state_save_UINT16(file,"s2650",cpu,"IAR",&S.iar,1);
/*TODO*///	state_save_UINT8(file,"s2650",cpu,"PSL",&S.psl,1);
/*TODO*///	state_save_UINT8(file,"s2650",cpu,"PSU",&S.psu,1);
/*TODO*///	state_save_UINT8(file,"s2650",cpu,"REG",S.reg,7);
/*TODO*///	state_save_UINT8(file,"s2650",cpu,"HALT",&S.halt,1);
/*TODO*///	state_save_UINT16(file,"s2650",cpu,"RAS",S.ras,8);
/*TODO*///	state_save_UINT8(file,"s2650",cpu,"IRQ_STATE",&S.irq_state,1);
    }

    @Override
    public void cpu_state_load(Object file) {
        /*TODO*///	int cpu = cpu_getactivecpu();
/*TODO*///	state_load_UINT16(file,"s2650",cpu,"PAGE",&S.page,1);
/*TODO*///	state_load_UINT16(file,"s2650",cpu,"IAR",&S.iar,1);
/*TODO*///	state_load_UINT8(file,"s2650",cpu,"PSL",&S.psl,1);
/*TODO*///	state_load_UINT8(file,"s2650",cpu,"PSU",&S.psu,1);
/*TODO*///	state_load_UINT8(file,"s2650",cpu,"REG",S.reg,7);
/*TODO*///	state_load_UINT8(file,"s2650",cpu,"HALT",&S.halt,1);
/*TODO*///	state_load_UINT16(file,"s2650",cpu,"RAS",S.ras,8);
/*TODO*///	state_load_UINT8(file,"s2650",cpu,"IRQ_STATE",&S.irq_state,1);
    }

    @Override
    public String cpu_info(Object context, int regnum) {
        /*TODO*///	static char buffer[16][47+1];
/*TODO*///	static int which = 0;
/*TODO*///	s2650_Regs *r = context;
/*TODO*///
/*TODO*///	which = ++which % 16;
/*TODO*///	buffer[which][0] = '\0';
/*TODO*///
/*TODO*///    if( !context )
/*TODO*///		r = &S;
/*TODO*///
        switch (regnum) {
            /*TODO*///		case CPU_INFO_FLAGS:
/*TODO*///		case CPU_INFO_REG+S2650_PC: sprintf(buffer[which], "PC:%04X", r->page + r->iar); break;
/*TODO*///		case CPU_INFO_REG+S2650_PS: sprintf(buffer[which], "PS:%02X%02X", r->psu, r->psl); break;
/*TODO*///		case CPU_INFO_REG+S2650_R0: sprintf(buffer[which], "R0:%02X", r->reg[0]); break;
/*TODO*///		case CPU_INFO_REG+S2650_R1: sprintf(buffer[which], "R1:%02X", r->reg[1]); break;
/*TODO*///		case CPU_INFO_REG+S2650_R2: sprintf(buffer[which], "R2:%02X", r->reg[2]); break;
/*TODO*///		case CPU_INFO_REG+S2650_R3: sprintf(buffer[which], "R3:%02X", r->reg[3]); break;
/*TODO*///		case CPU_INFO_REG+S2650_R1A: sprintf(buffer[which], "R1'%02X", r->reg[4]); break;
/*TODO*///		case CPU_INFO_REG+S2650_R2A: sprintf(buffer[which], "R2'%02X", r->reg[5]); break;
/*TODO*///		case CPU_INFO_REG+S2650_R3A: sprintf(buffer[which], "R3'%02X", r->reg[6]); break;
/*TODO*///		case CPU_INFO_REG+S2650_HALT: sprintf(buffer[which], "HALT:%X", r->halt); break;
/*TODO*///		case CPU_INFO_REG+S2650_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r->irq_state); break;
/*TODO*///		case CPU_INFO_REG+S2650_SI: sprintf(buffer[which], "SI:%X", (r->psu & SI) ? 1 : 0); break;
/*TODO*///		case CPU_INFO_REG+S2650_FO: sprintf(buffer[which], "FO:%X", (r->psu & FO) ? 1 : 0); break;
/*TODO*///			sprintf(buffer[which], "%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c",
/*TODO*///				r->psu & 0x80 ? 'S':'.',
/*TODO*///				r->psu & 0x40 ? 'O':'.',
/*TODO*///				r->psu & 0x20 ? 'I':'.',
/*TODO*///				r->psu & 0x10 ? '?':'.',
/*TODO*///				r->psu & 0x08 ? '?':'.',
/*TODO*///				r->psu & 0x04 ? 's':'.',
/*TODO*///				r->psu & 0x02 ? 's':'.',
/*TODO*///				r->psu & 0x01 ? 's':'.',
/*TODO*///                r->psl & 0x80 ? 'M':'.',
/*TODO*///				r->psl & 0x40 ? 'P':'.',
/*TODO*///				r->psl & 0x20 ? 'H':'.',
/*TODO*///				r->psl & 0x10 ? 'R':'.',
/*TODO*///				r->psl & 0x08 ? 'W':'.',
/*TODO*///				r->psl & 0x04 ? 'V':'.',
/*TODO*///				r->psl & 0x02 ? '2':'.',
/*TODO*///				r->psl & 0x01 ? 'C':'.');
/*TODO*///			break;
            case CPU_INFO_NAME:
                return "S2650";
            case CPU_INFO_FAMILY:
                return "Signetics 2650";
            case CPU_INFO_VERSION:
                return "1.1";
            case CPU_INFO_FILE:
                return "s2650.java";
            case CPU_INFO_CREDITS:
                return "Written by Juergen Buchmueller for use with MAME";
            /*TODO*///		case CPU_INFO_REG_LAYOUT: return (const char *)s2650_reg_layout;
/*TODO*///		case CPU_INFO_WIN_LAYOUT: return (const char *)s2650_win_layout;
        }
        /*TODO*///	return buffer[which];
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int memory_read(int offset) {
        return cpu_readmem16(offset);
    }

    @Override
    public void memory_write(int offset, int data) {
        cpu_writemem16(offset, data);
    }

    /**
     * Arcadeflex's specific
     */
    @Override
    public void set_op_base(int pc) {
        cpu_setOPbase16.handler(pc);
    }

    @Override
    public Object init_context() {
        Object reg = new s2650_Regs();
        return reg;
    }

}
