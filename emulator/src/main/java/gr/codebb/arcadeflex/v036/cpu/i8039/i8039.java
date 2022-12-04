/*
This file is part of Arcadeflex.

Arcadeflex is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Arcadeflex is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Arcadeflex.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * ported to 0.37b7
 */
package gr.codebb.arcadeflex.v036.cpu.i8039;

import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.CPU_I8039;
import static gr.codebb.arcadeflex.v036.cpu.i8039.i8039H.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.cpu_setOPbase16;
import static gr.codebb.arcadeflex.common.libc.cstring.*;

public class i8039 extends cpu_interface {

    public static int[] i8039_ICount = new int[1];

    public i8039() {
        cpu_num = CPU_I8039;
        num_irqs = 1;
        default_vector = 0;
        overclock = 1.0;
        no_int = I8039_IGNORE_INT;
        irq_int = I8039_EXT_INT;
        nmi_int = -1;
        address_bits = 16;
        address_shift = 0;
        endianess = CPU_IS_LE;
        align_unit = 1;
        max_inst_len = 2;
        abits1 = ABITS1_16;
        abits2 = ABITS2_16;
        abitsmin = ABITS_MIN_16;
        icount = i8039_ICount;
    }

    public static char M_RDMEM(int A) {
        return I8039_RDMEM(A);
    }

    public static char M_RDOP(int A) {
        return I8039_RDOP(A);
    }

    public static char M_RDOP_ARG(int A) {
        return I8039_RDOP_ARG(A);
    }

    public static char M_IN(int A) {
        return I8039_In(A);
    }

    public static void M_OUT(int A, int V) {
        I8039_Out(A, V);
    }

    public static char port_r(int A) {
        return I8039_In(I8039_p0 + A);
    }

    public static void port_w(int A, int V) {
        I8039_Out(I8039_p0 + A, V);
    }

    public static char test_r(int A) {
        return I8039_In(I8039_t0 + A);
    }

    public static void test_w(int A, int V) {
        I8039_Out(I8039_t0 + A, V);
    }

    public static char bus_r() {
        return I8039_In(I8039_bus);
    }

    public static void bus_w(int V) {
        I8039_Out(I8039_bus, V);
    }
    public static final int C_FLAG = 0x80;
    public static final int A_FLAG = 0x40;
    public static final int F_FLAG = 0x20;
    public static final int B_FLAG = 0x10;


    public static class PAIR {
        //L = low 8 bits
        //H = high 8 bits
        //D = whole 16 bits

        public int H, L, D;

        public void SetH(int val) {
            H = val;
            D = (H << 8) | L;
        }

        public void SetL(int val) {
            L = val;
            D = (H << 8) | L;
        }

        public void SetD(int val) {
            D = val;
            H = D >> 8 & 0xFF;
            L = D & 0xFF;
        }

        public void AddH(int val) {
            H = (H + val) & 0xFF;
            D = (H << 8) | L;
        }

        public void AddL(int val) {
            L = (L + val) & 0xFF;
            D = (H << 8) | L;
        }

        public void AddD(int val) {
            D = (D + val) & 0xFFFF;
            H = D >> 8 & 0xFF;
            L = D & 0xFF;
        }
    };

    public static class I8039_Regs {

        public PAIR PREPC = new PAIR();
        /* previous program counter */
        public PAIR PC = new PAIR();
        /* program counter */
        public char u8_A, u8_SP, u8_PSW;
        public char[] u8_RAM = new char[128];
        public char u8_bus, u8_f1;
        /* Bus data, and flag1 */

        public int pending_irq, irq_executing, masterClock, regPtr;
        public char u8_t_flag, u8_timer, u8_timerON, u8_countON, u8_xirq_en, u8_tirq_en;
        public char A11, A11ff;
        public int irq_state;
        public irqcallbacksPtr irq_callback;
    }

    public static I8039_Regs R = new I8039_Regs();
    public static char u8_Old_T1;

    /* The opcode table now is a combination of cycle counts and function pointers */
    public abstract interface opcode {

        public abstract void handler();
    }

    static class s_opcode {

        public /*uint*/ int cycles;
        public opcode function;

        public s_opcode(int cycles, opcode function) {
            this.cycles = cycles;
            this.function = function;
        }
    }

    public static int POSITIVE_EDGE_T1(int T1) {
        return (((T1 - u8_Old_T1) > 0) ? 1 : 0);
    }

    public static int NEGATIVE_EDGE_T1(int T1) {
        return (((u8_Old_T1 - T1) > 0) ? 1 : 0);
    }

    public static char M_Cy() {
        return (char) ((((R.u8_PSW & C_FLAG) >>> 7)) & 0xFF);
    }

    public static boolean M_Cn() {
        return (M_Cy() == 0);
    }

    public static boolean M_Ay() {
        return ((R.u8_PSW & A_FLAG)) != 0;
    }

    public static boolean M_An() {
        return (!M_Ay());
    }

    public static boolean M_F0y() {
        return ((R.u8_PSW & F_FLAG)) != 0;
    }

    public static boolean M_F0n() {
        return (!M_F0y());
    }

    public static boolean M_By() {
        return ((R.u8_PSW & B_FLAG)) != 0;
    }

    public static boolean M_Bn() {
        return (!M_By());
    }

    public static void CLR(/*UINT8*/int flag) {
        R.u8_PSW = (char) ((R.u8_PSW & ~flag) & 0xFF);
    }

    public static void SET(/*UINT8*/int flag) {
        R.u8_PSW = (char) ((R.u8_PSW | flag) & 0xFF);
    }

    /* Get next opcode argument and increment program counter */
    public static char M_RDMEM_OPCODE() {
        char retval;
        retval = M_RDOP_ARG(R.PC.D);
        R.PC.AddD(1);
        return (char) (retval & 0xFF);
    }

    public static void push(/*UINT8*/int d) {
        R.u8_RAM[8 + R.u8_SP++] = (char) (d & 0xFF);
        R.u8_SP = (char) ((R.u8_SP & 0x0f) & 0xFF);
        R.u8_PSW = (char) ((R.u8_PSW & 0xf8) & 0xFF);
        R.u8_PSW = (char) ((R.u8_PSW | (R.u8_SP >>> 1)) & 0xFF);
    }

    public static char /*UINT8*/ pull() {
        R.u8_SP = (char) (((R.u8_SP + 15) & 0x0f) & 0xFF);
        /*  if (--R.SP < 0) R.SP = 15;  */
        R.u8_PSW = (char) ((R.u8_PSW & 0xf8) & 0xFF);
        R.u8_PSW = (char) ((R.u8_PSW | (R.u8_SP >>> 1)) & 0xFF);
        /* regPTR = ((M_By) ? 24 : 0);  regPTR should not change */
        return (char) ((R.u8_RAM[8 + R.u8_SP]) & 0xFF);
    }

    static opcode daa_a = new opcode() {
        public void handler() {
            if ((R.u8_A & 0x0f) > 0x09 || (R.u8_PSW & A_FLAG) != 0) {
                R.u8_A = (char) ((R.u8_A + 0x06) & 0xFF);
            }
            if ((R.u8_A & 0xf0) > 0x90 || (R.u8_PSW & C_FLAG) != 0) {
                R.u8_A = (char) ((R.u8_A + 0x60) & 0xFF);
                SET(C_FLAG);
            } else {
                CLR(C_FLAG);
            }
        }
    };

    public static void M_ADD(/*UINT8*/int dat) {
        char temp;

        CLR(C_FLAG | A_FLAG);
        if ((R.u8_A & 0xf) + (dat & 0xf) > 0xf) {
            SET(A_FLAG);
        }
        temp = (char) (R.u8_A + dat);
        if (temp > 0xff) {
            SET(C_FLAG);
        }
        R.u8_A = (char) (temp & 0xff);
    }

    public static void M_ADDC(/*UINT8*/int dat) {
        char temp;

        CLR(A_FLAG);
        if ((R.u8_A & 0xf) + (dat & 0xf) + M_Cy() > 0xf) {
            SET(A_FLAG);
        }
        temp = (char) (R.u8_A + dat + M_Cy());
        CLR(C_FLAG);
        if (temp > 0xff) {
            SET(C_FLAG);
        }
        R.u8_A = (char) (temp & 0xff);
    }

    public static void M_CALL(int addr) {
        push(R.PC.L);
        push((R.PC.H & 0x0f) | (R.u8_PSW & 0xf0));
        R.PC.SetD(addr & 0xFFFF);

    }

    public static void M_XCHD(/*UINT8*/int addr) {
        char/*UINT8*/ dat = (char) ((R.u8_A & 0x0f) & 0xFF);
        R.u8_A = (char) ((R.u8_A & 0xf0) & 0xFF);
        R.u8_A = (char) ((R.u8_A | R.u8_RAM[addr] & 0x0f) & 0xFF);
        R.u8_RAM[addr] = (char) ((R.u8_RAM[addr] & 0xf0) & 0xFF);
        R.u8_RAM[addr] = (char) ((R.u8_RAM[addr] | dat) & 0xFF);
    }

    public static void M_ILLEGAL() {
        //logerror("I8039:  PC = %04x,  Illegal opcode = %02x\n", R.PC.D - 1, M_RDMEM(R.PC.D - 1));
    }

    public static void M_UNDEFINED() {
        //logerror("I8039:  PC = %04x,  Unimplemented opcode = %02x\n", R.PC.D - 1, M_RDMEM(R.PC.D - 1));
    }

    static opcode illegal = new opcode() {
        public void handler() {
            M_ILLEGAL();
        }
    };

    static opcode add_a_n = new opcode() {
        public void handler() {
            M_ADD(M_RDMEM_OPCODE());
        }
    };
    static opcode add_a_r0 = new opcode() {
        public void handler() {
            M_ADD(R.u8_RAM[R.regPtr]);
        }
    };
    static opcode add_a_r1 = new opcode() {
        public void handler() {
            M_ADD(R.u8_RAM[R.regPtr + 1]);
        }
    };
    static opcode add_a_r2 = new opcode() {
        public void handler() {
            M_ADD(R.u8_RAM[R.regPtr + 2]);
        }
    };
    static opcode add_a_r3 = new opcode() {
        public void handler() {
            M_ADD(R.u8_RAM[R.regPtr + 3]);
        }
    };
    static opcode add_a_r4 = new opcode() {
        public void handler() {
            M_ADD(R.u8_RAM[R.regPtr + 4]);
        }
    };
    static opcode add_a_r5 = new opcode() {
        public void handler() {
            M_ADD(R.u8_RAM[R.regPtr + 5]);
        }
    };
    static opcode add_a_r6 = new opcode() {
        public void handler() {
            M_ADD(R.u8_RAM[R.regPtr + 6]);
        }
    };
    static opcode add_a_r7 = new opcode() {
        public void handler() {
            M_ADD(R.u8_RAM[R.regPtr + 7]);
        }
    };
    static opcode add_a_xr0 = new opcode() {
        public void handler() {
            M_ADD(R.u8_RAM[R.u8_RAM[R.regPtr] & 0x7f]);
        }
    };
    static opcode add_a_xr1 = new opcode() {
        public void handler() {
            M_ADD(R.u8_RAM[R.u8_RAM[R.regPtr + 1] & 0x7f]);
        }
    };
    static opcode adc_a_n = new opcode() {
        public void handler() {
            M_ADDC(M_RDMEM_OPCODE());
        }
    };
    static opcode adc_a_r0 = new opcode() {
        public void handler() {
            M_ADDC(R.u8_RAM[R.regPtr]);
        }
    };
    static opcode adc_a_r1 = new opcode() {
        public void handler() {
            M_ADDC(R.u8_RAM[R.regPtr + 1]);
        }
    };
    static opcode adc_a_r2 = new opcode() {
        public void handler() {
            M_ADDC(R.u8_RAM[R.regPtr + 2]);
        }
    };
    static opcode adc_a_r3 = new opcode() {
        public void handler() {
            M_ADDC(R.u8_RAM[R.regPtr + 3]);
        }
    };
    static opcode adc_a_r4 = new opcode() {
        public void handler() {
            M_ADDC(R.u8_RAM[R.regPtr + 4]);
        }
    };
    static opcode adc_a_r5 = new opcode() {
        public void handler() {
            M_ADDC(R.u8_RAM[R.regPtr + 5]);
        }
    };
    static opcode adc_a_r6 = new opcode() {
        public void handler() {
            M_ADDC(R.u8_RAM[R.regPtr + 6]);
        }
    };
    static opcode adc_a_r7 = new opcode() {
        public void handler() {
            M_ADDC(R.u8_RAM[R.regPtr + 7]);
        }
    };
    static opcode adc_a_xr0 = new opcode() {
        public void handler() {
            M_ADDC(R.u8_RAM[R.u8_RAM[R.regPtr] & 0x7f]);
        }
    };
    static opcode adc_a_xr1 = new opcode() {
        public void handler() {
            M_ADDC(R.u8_RAM[R.u8_RAM[R.regPtr + 1] & 0x7f]);
        }
    };
    static opcode anl_a_n = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A & M_RDMEM_OPCODE()) & 0xFF);
        }
    };
    static opcode anl_a_r0 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A & R.u8_RAM[R.regPtr]) & 0xFF);
        }
    };
    static opcode anl_a_r1 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A & R.u8_RAM[R.regPtr + 1]) & 0xFF);
        }
    };
    static opcode anl_a_r2 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A & R.u8_RAM[R.regPtr + 2]) & 0xFF);
        }
    };
    static opcode anl_a_r3 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A & R.u8_RAM[R.regPtr + 3]) & 0xFF);
        }
    };
    static opcode anl_a_r4 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A & R.u8_RAM[R.regPtr + 4]) & 0xFF);
        }
    };
    static opcode anl_a_r5 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A & R.u8_RAM[R.regPtr + 5]) & 0xFF);
        }
    };
    static opcode anl_a_r6 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A & R.u8_RAM[R.regPtr + 6]) & 0xFF);
        }
    };
    static opcode anl_a_r7 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A & R.u8_RAM[R.regPtr + 7]) & 0xFF);
        }
    };
    static opcode anl_a_xr0 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A & R.u8_RAM[R.u8_RAM[R.regPtr] & 0x7f]) & 0xFF);
        }
    };
    static opcode anl_a_xr1 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A & R.u8_RAM[R.u8_RAM[R.regPtr + 1] & 0x7f]) & 0xFF);
        }
    };
    static opcode anl_bus_n = new opcode() {
        public void handler() {
            bus_w(bus_r() & M_RDMEM_OPCODE());
        }
    };
    static opcode anl_p1_n = new opcode() {
        public void handler() {
            port_w(1, port_r(1) & M_RDMEM_OPCODE());
        }
    };
    static opcode anl_p2_n = new opcode() {
        public void handler() {
            port_w(2, port_r(2) & M_RDMEM_OPCODE());
        }
    };
    static opcode anld_p4_a = new opcode() {
        public void handler() {
            port_w(4, port_r(4) & M_RDMEM_OPCODE());
        }
    };
    static opcode anld_p5_a = new opcode() {
        public void handler() {
            port_w(5, port_r(5) & M_RDMEM_OPCODE());
        }
    };
    static opcode anld_p6_a = new opcode() {
        public void handler() {
            port_w(6, port_r(6) & M_RDMEM_OPCODE());
        }
    };
    static opcode anld_p7_a = new opcode() {
        public void handler() {
            port_w(7, port_r(7) & M_RDMEM_OPCODE());
        }
    };
    static opcode call = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            M_CALL(i | R.A11);
        }
    };
    static opcode call_1 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            M_CALL(i | 0x100 | R.A11);
        }
    };
    static opcode call_2 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            M_CALL(i | 0x200 | R.A11);
        }
    };
    static opcode call_3 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            M_CALL(i | 0x300 | R.A11);
        }
    };
    static opcode call_4 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            M_CALL(i | 0x400 | R.A11);
        }
    };
    static opcode call_5 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            M_CALL(i | 0x500 | R.A11);
        }
    };
    static opcode call_6 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            M_CALL(i | 0x600 | R.A11);
        }
    };
    static opcode call_7 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            M_CALL(i | 0x700 | R.A11);
        }
    };
    static opcode clr_a = new opcode() {
        public void handler() {
            R.u8_A = 0;
        }
    };
    static opcode clr_c = new opcode() {
        public void handler() {
            CLR(C_FLAG);
        }
    };
    static opcode clr_f0 = new opcode() {
        public void handler() {
            CLR(F_FLAG);
        }
    };
    static opcode clr_f1 = new opcode() {
        public void handler() {
            R.u8_f1 = 0;
        }
    };
    static opcode cpl_a = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A ^ 0xff) & 0xFF);
        }
    };
    static opcode cpl_c = new opcode() {
        public void handler() {
            R.u8_PSW = (char) ((R.u8_PSW ^ C_FLAG) & 0xFF);
        }
    };
    static opcode cpl_f0 = new opcode() {
        public void handler() {
            R.u8_PSW = (char) ((R.u8_PSW ^ F_FLAG) & 0xFF);
        }
    };
    static opcode cpl_f1 = new opcode() {
        public void handler() {
            R.u8_f1 = (char) ((R.u8_f1 ^ 1) & 0xFF);
        }
    };
    static opcode dec_a = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A - 1) & 0xFF);
        }
    };
    static opcode dec_r0 = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr] = (char) ((R.u8_RAM[R.regPtr] - 1) & 0xFF);
        }
    };
    static opcode dec_r1 = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 1] = (char) ((R.u8_RAM[R.regPtr + 1] - 1) & 0xFF);
        }
    };
    static opcode dec_r2 = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 2] = (char) ((R.u8_RAM[R.regPtr + 2] - 1) & 0xFF);
        }
    };
    static opcode dec_r3 = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 3] = (char) ((R.u8_RAM[R.regPtr + 3] - 1) & 0xFF);
        }
    };
    static opcode dec_r4 = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 4] = (char) ((R.u8_RAM[R.regPtr + 4] - 1) & 0xFF);
        }
    };
    static opcode dec_r5 = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 5] = (char) ((R.u8_RAM[R.regPtr + 5] - 1) & 0xFF);
        }
    };
    static opcode dec_r6 = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 6] = (char) ((R.u8_RAM[R.regPtr + 6] - 1) & 0xFF);
        }
    };
    static opcode dec_r7 = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 7] = (char) ((R.u8_RAM[R.regPtr + 7] - 1) & 0xFF);
        }
    };
    static opcode dis_i = new opcode() {
        public void handler() {
            R.u8_xirq_en = 0;
        }
    };
    static opcode dis_tcnti = new opcode() {
        public void handler() {
            R.u8_tirq_en = 0;
        }
    };

    static opcode djnz_r0 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            R.u8_RAM[R.regPtr] = (char) ((R.u8_RAM[R.regPtr] - 1) & 0xFF);
            if (R.u8_RAM[R.regPtr] != 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode djnz_r1 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            R.u8_RAM[R.regPtr + 1] = (char) ((R.u8_RAM[R.regPtr + 1] - 1) & 0xFF);
            if (R.u8_RAM[R.regPtr + 1] != 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode djnz_r2 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            R.u8_RAM[R.regPtr + 2] = (char) ((R.u8_RAM[R.regPtr + 2] - 1) & 0xFF);
            if (R.u8_RAM[R.regPtr + 2] != 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode djnz_r3 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            R.u8_RAM[R.regPtr + 3] = (char) ((R.u8_RAM[R.regPtr + 3] - 1) & 0xFF);
            if (R.u8_RAM[R.regPtr + 3] != 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode djnz_r4 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            R.u8_RAM[R.regPtr + 4] = (char) ((R.u8_RAM[R.regPtr + 4] - 1) & 0xFF);
            if (R.u8_RAM[R.regPtr + 4] != 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode djnz_r5 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            R.u8_RAM[R.regPtr + 5] = (char) ((R.u8_RAM[R.regPtr + 5] - 1) & 0xFF);
            if (R.u8_RAM[R.regPtr + 5] != 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode djnz_r6 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            R.u8_RAM[R.regPtr + 6] = (char) ((R.u8_RAM[R.regPtr + 6] - 1) & 0xFF);
            if (R.u8_RAM[R.regPtr + 6] != 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode djnz_r7 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            R.u8_RAM[R.regPtr + 7] = (char) ((R.u8_RAM[R.regPtr + 7] - 1) & 0xFF);
            if (R.u8_RAM[R.regPtr + 7] != 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode en_i = new opcode() {
        public void handler() {
            R.u8_xirq_en = 1;
            if (R.irq_state != CLEAR_LINE) {
                R.pending_irq |= I8039_EXT_INT;
            }
        }
    };
    static opcode en_tcnti = new opcode() {
        public void handler() {
            R.u8_tirq_en = 1;
        }
    };
    static opcode ento_clk = new opcode() {
        public void handler() {
            M_UNDEFINED();
        }
    };
    static opcode in_a_p1 = new opcode() {
        public void handler() {
            R.u8_A = port_r(1);
        }
    };
    static opcode in_a_p2 = new opcode() {
        public void handler() {
            R.u8_A = port_r(2);
        }
    };
    static opcode ins_a_bus = new opcode() {
        public void handler() {
            R.u8_A = bus_r();
        }
    };
    static opcode inc_a = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A + 1) & 0xFF);
        }
    };
    static opcode inc_r0 = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr] = (char) ((R.u8_RAM[R.regPtr] + 1) & 0xFF);
        }
    };
    static opcode inc_r1 = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 1] = (char) ((R.u8_RAM[R.regPtr + 1] + 1) & 0xFF);
        }
    };
    static opcode inc_r2 = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 2] = (char) ((R.u8_RAM[R.regPtr + 2] + 1) & 0xFF);
        }
    };
    static opcode inc_r3 = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 3] = (char) ((R.u8_RAM[R.regPtr + 3] + 1) & 0xFF);
        }
    };
    static opcode inc_r4 = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 4] = (char) ((R.u8_RAM[R.regPtr + 4] + 1) & 0xFF);
        }
    };
    static opcode inc_r5 = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 5] = (char) ((R.u8_RAM[R.regPtr + 5] + 1) & 0xFF);
        }
    };
    static opcode inc_r6 = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 6] = (char) ((R.u8_RAM[R.regPtr + 6] + 1) & 0xFF);
        }
    };
    static opcode inc_r7 = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 7] = (char) ((R.u8_RAM[R.regPtr + 7] + 1) & 0xFF);
        }
    };
    static opcode inc_xr0 = new opcode() {
        public void handler() {
            R.u8_RAM[R.u8_RAM[R.regPtr] & 0x7f] = (char) ((R.u8_RAM[R.u8_RAM[R.regPtr] & 0x7f] + 1) & 0xFF);
        }
    };
    static opcode inc_xr1 = new opcode() {
        public void handler() {
            R.u8_RAM[R.u8_RAM[R.regPtr + 1] & 0x7f] = (char) ((R.u8_RAM[R.u8_RAM[R.regPtr + 1] & 0x7f] + 1) & 0xFF);
        }
    };

    static opcode jmp = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDOP(R.PC.D);
            char oldpc, newpc;

            oldpc = (char) (R.PC.D - 1);
            R.PC.SetD(i | R.A11);
            newpc = (char) R.PC.D;
            if (newpc == oldpc) {
                if (i8039_ICount[0] > 0) {
                    i8039_ICount[0] = 0;
                }
            } /* speed up busy loop */ else if (newpc == oldpc - 1 && M_RDOP(newpc) == 0x00) /* NOP - Gyruss */ {
                if (i8039_ICount[0] > 0) {
                    i8039_ICount[0] = 0;
                }
            }
        }
    };
    static opcode jmp_1 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDOP(R.PC.D);
            R.PC.SetD(i | 0x100 | R.A11);
        }
    };
    static opcode jmp_2 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDOP(R.PC.D);
            R.PC.SetD(i | 0x200 | R.A11);
        }
    };
    static opcode jmp_3 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDOP(R.PC.D);
            R.PC.SetD(i | 0x300 | R.A11);
        }
    };
    static opcode jmp_4 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDOP(R.PC.D);
            R.PC.SetD(i | 0x400 | R.A11);
        }
    };
    static opcode jmp_5 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDOP(R.PC.D);
            R.PC.SetD(i | 0x500 | R.A11);
        }
    };
    static opcode jmp_6 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDOP(R.PC.D);
            R.PC.SetD(i | 0x600 | R.A11);
        }
    };
    static opcode jmp_7 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDOP(R.PC.D);
            R.PC.SetD(i | 0x700 | R.A11);
        }
    };
    static opcode jmpp_xa = new opcode() {
        public void handler() {
            char addr = (char) ((R.PC.D & 0xf00) | R.u8_A);
            R.PC.SetD((R.PC.D & 0xf00) | M_RDMEM(addr));
        }
    };
    static opcode jb_0 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            if ((R.u8_A & 0x01) != 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode jb_1 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            if ((R.u8_A & 0x02) != 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode jb_2 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            if ((R.u8_A & 0x04) != 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode jb_3 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            if ((R.u8_A & 0x08) != 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode jb_4 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            if ((R.u8_A & 0x10) != 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode jb_5 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            if ((R.u8_A & 0x20) != 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode jb_6 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            if ((R.u8_A & 0x40) != 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode jb_7 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            if ((R.u8_A & 0x80) != 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode jf0 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            if (M_F0y()) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode jf_1 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            if (R.u8_f1 != 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode jnc = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            if (M_Cn()) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode jc = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            if (M_Cy() != 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode jni = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            if (R.irq_state != CLEAR_LINE) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode jnt_0 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            if (test_r(0) == 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode jt_0 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            if (test_r(0) != 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode jnt_1 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            if (test_r(1) == 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode jt_1 = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            if (test_r(1) != 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode jnz = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            if (R.u8_A != 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode jz = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            if (R.u8_A == 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
            }
        }
    };
    static opcode jtf = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_RDMEM_OPCODE();
            if (R.u8_t_flag != 0) {
                R.PC.SetD((R.PC.D & 0xf00) | i);
                R.u8_t_flag = 0;
            }
        }
    };
    static opcode mov_a_n = new opcode() {
        public void handler() {
            R.u8_A = M_RDMEM_OPCODE();
        }
    };
    static opcode mov_a_r0 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_RAM[R.regPtr]) & 0xFF);
        }
    };
    static opcode mov_a_r1 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_RAM[R.regPtr + 1]) & 0xFF);
        }
    };
    static opcode mov_a_r2 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_RAM[R.regPtr + 2]) & 0xFF);
        }
    };
    static opcode mov_a_r3 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_RAM[R.regPtr + 3]) & 0xFF);
        }
    };
    static opcode mov_a_r4 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_RAM[R.regPtr + 4]) & 0xFF);
        }
    };
    static opcode mov_a_r5 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_RAM[R.regPtr + 5]) & 0xFF);
        }
    };
    static opcode mov_a_r6 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_RAM[R.regPtr + 6]) & 0xFF);
        }
    };
    static opcode mov_a_r7 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_RAM[R.regPtr + 7]) & 0xFF);
        }
    };
    static opcode mov_a_psw = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_PSW) & 0xFF);
        }
    };
    static opcode mov_a_xr0 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_RAM[R.u8_RAM[R.regPtr] & 0x7f]) & 0xFF);
        }
    };
    static opcode mov_a_xr1 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_RAM[R.u8_RAM[R.regPtr + 1] & 0x7f]) & 0xFF);
        }
    };
    static opcode mov_r0_a = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr] = (char) ((R.u8_A) & 0xFF);
        }
    };
    static opcode mov_r1_a = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 1] = (char) ((R.u8_A) & 0xFF);
        }
    };
    static opcode mov_r2_a = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 2] = (char) ((R.u8_A) & 0xFF);
        }
    };
    static opcode mov_r3_a = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 3] = (char) ((R.u8_A) & 0xFF);
        }
    };
    static opcode mov_r4_a = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 4] = (char) ((R.u8_A) & 0xFF);
        }
    };
    static opcode mov_r5_a = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 5] = (char) ((R.u8_A) & 0xFF);
        }
    };
    static opcode mov_r6_a = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 6] = (char) ((R.u8_A) & 0xFF);
        }
    };
    static opcode mov_r7_a = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 7] = (char) ((R.u8_A) & 0xFF);
        }
    };
    static opcode mov_psw_a = new opcode() {
        public void handler() {
            R.u8_PSW = R.u8_A;
            R.regPtr = ((M_By()) ? 24 : 0);
            R.u8_SP = (char) (((R.u8_PSW & 7) << 1) & 0xFF);
        }
    };
    static opcode mov_r0_n = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr] = M_RDMEM_OPCODE();
        }
    };
    static opcode mov_r1_n = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 1] = M_RDMEM_OPCODE();
        }
    };
    static opcode mov_r2_n = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 2] = M_RDMEM_OPCODE();
        }
    };
    static opcode mov_r3_n = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 3] = M_RDMEM_OPCODE();
        }
    };
    static opcode mov_r4_n = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 4] = M_RDMEM_OPCODE();
        }
    };
    static opcode mov_r5_n = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 5] = M_RDMEM_OPCODE();
        }
    };
    static opcode mov_r6_n = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 6] = M_RDMEM_OPCODE();
        }
    };
    static opcode mov_r7_n = new opcode() {
        public void handler() {
            R.u8_RAM[R.regPtr + 7] = M_RDMEM_OPCODE();
        }
    };
    static opcode mov_a_t = new opcode() {
        public void handler() {
            R.u8_A = R.u8_timer;
        }
    };
    static opcode mov_t_a = new opcode() {
        public void handler() {
            R.u8_timer = R.u8_A;
        }
    };
    static opcode mov_xr0_a = new opcode() {
        public void handler() {
            R.u8_RAM[R.u8_RAM[R.regPtr] & 0x7f] = R.u8_A;
        }
    };
    static opcode mov_xr1_a = new opcode() {
        public void handler() {
            R.u8_RAM[R.u8_RAM[R.regPtr + 1] & 0x7f] = R.u8_A;
        }
    };
    static opcode mov_xr0_n = new opcode() {
        public void handler() {
            R.u8_RAM[R.u8_RAM[R.regPtr] & 0x7f] = M_RDMEM_OPCODE();
        }
    };
    static opcode mov_xr1_n = new opcode() {
        public void handler() {
            R.u8_RAM[R.u8_RAM[R.regPtr + 1] & 0x7f] = M_RDMEM_OPCODE();
        }
    };
    static opcode movd_a_p4 = new opcode() {
        public void handler() {
            R.u8_A = port_r(4);
        }
    };
    static opcode movd_a_p5 = new opcode() {
        public void handler() {
            R.u8_A = port_r(5);
        }
    };
    static opcode movd_a_p6 = new opcode() {
        public void handler() {
            R.u8_A = port_r(6);
        }
    };
    static opcode movd_a_p7 = new opcode() {
        public void handler() {
            R.u8_A = port_r(7);
        }
    };
    static opcode movd_p4_a = new opcode() {
        public void handler() {
            port_w(4, R.u8_A);
        }
    };
    static opcode movd_p5_a = new opcode() {
        public void handler() {
            port_w(5, R.u8_A);
        }
    };
    static opcode movd_p6_a = new opcode() {
        public void handler() {
            port_w(6, R.u8_A);
        }
    };
    static opcode movd_p7_a = new opcode() {
        public void handler() {
            port_w(7, R.u8_A);
        }
    };
    static opcode movp_a_xa = new opcode() {
        public void handler() {
            R.u8_A = M_RDMEM((R.PC.D & 0x0f00) | R.u8_A);
        }
    };
    static opcode movp3_a_xa = new opcode() {
        public void handler() {
            R.u8_A = M_RDMEM(0x300 | R.u8_A);
        }
    };
    static opcode movx_a_xr0 = new opcode() {
        public void handler() {
            R.u8_A = M_IN(R.u8_RAM[R.regPtr]);
        }
    };
    static opcode movx_a_xr1 = new opcode() {
        public void handler() {
            R.u8_A = M_IN(R.u8_RAM[R.regPtr + 1]);
        }
    };
    static opcode movx_xr0_a = new opcode() {
        public void handler() {
            M_OUT(R.u8_RAM[R.regPtr], R.u8_A);
        }
    };
    static opcode movx_xr1_a = new opcode() {
        public void handler() {
            M_OUT(R.u8_RAM[R.regPtr + 1], R.u8_A);
        }
    };
    static opcode nop = new opcode() {
        public void handler() {
        }
    };
    static opcode orl_a_n = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A | M_RDMEM_OPCODE()) & 0xFF);
        }
    };
    static opcode orl_a_r0 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A | R.u8_RAM[R.regPtr]) & 0xFF);
        }
    };
    static opcode orl_a_r1 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A | R.u8_RAM[R.regPtr + 1]) & 0xFF);
        }
    };
    static opcode orl_a_r2 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A | R.u8_RAM[R.regPtr + 2]) & 0xFF);
        }
    };
    static opcode orl_a_r3 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A | R.u8_RAM[R.regPtr + 3]) & 0xFF);
        }
    };
    static opcode orl_a_r4 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A | R.u8_RAM[R.regPtr + 4]) & 0xFF);
        }
    };
    static opcode orl_a_r5 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A | R.u8_RAM[R.regPtr + 5]) & 0xFF);
        }
    };
    static opcode orl_a_r6 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A | R.u8_RAM[R.regPtr + 6]) & 0xFF);
        }
    };
    static opcode orl_a_r7 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A | R.u8_RAM[R.regPtr + 7]) & 0xFF);
        }
    };
    static opcode orl_a_xr0 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A | R.u8_RAM[R.u8_RAM[R.regPtr] & 0x7f]) & 0xFF);
        }
    };
    static opcode orl_a_xr1 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A | R.u8_RAM[R.u8_RAM[R.regPtr + 1] & 0x7f]) & 0xFF);
        }
    };
    static opcode orl_bus_n = new opcode() {
        public void handler() {
            bus_w(bus_r() | M_RDMEM_OPCODE());
        }
    };
    static opcode orl_p1_n = new opcode() {
        public void handler() {
            port_w(1, port_r(1) | M_RDMEM_OPCODE());
        }
    };
    static opcode orl_p2_n = new opcode() {
        public void handler() {
            port_w(2, port_r(2) | M_RDMEM_OPCODE());
        }
    };
    static opcode orld_p4_a = new opcode() {
        public void handler() {
            port_w(4, port_r(4) | R.u8_A);
        }
    };
    static opcode orld_p5_a = new opcode() {
        public void handler() {
            port_w(5, port_r(5) | R.u8_A);
        }
    };
    static opcode orld_p6_a = new opcode() {
        public void handler() {
            port_w(6, port_r(6) | R.u8_A);
        }
    };
    static opcode orld_p7_a = new opcode() {
        public void handler() {
            port_w(7, port_r(7) | R.u8_A);
        }
    };
    static opcode outl_bus_a = new opcode() {
        public void handler() {
            bus_w(R.u8_A);
        }
    };
    static opcode outl_p1_a = new opcode() {
        public void handler() {
            port_w(1, R.u8_A);
        }
    };
    static opcode outl_p2_a = new opcode() {
        public void handler() {
            port_w(2, R.u8_A);
        }
    };
    static opcode ret = new opcode() {
        public void handler() {
            R.PC.SetD(((pull() & 0x0f) << 8));
            R.PC.SetD(R.PC.D | pull());
        }
    };
    static opcode retr = new opcode() {
        public void handler() {
            char/*UINT8*/ i = pull();
            R.PC.SetD(((i & 0x0f) << 8) | pull());
            R.irq_executing = I8039_IGNORE_INT;
//	R.A11 = R.A11ff;	/* NS990113 */
            R.u8_PSW = (char) (((R.u8_PSW & 0x0f) | (i & 0xf0)) & 0xFF);
            /* Stack is already changed by pull */
            R.regPtr = ((M_By()) ? 24 : 0);
        }
    };
    static opcode rl_a = new opcode() {
        public void handler() {
            char/*UINT8*/ i = (char) ((R.u8_A & 0x80) & 0xFF);
            R.u8_A = (char) ((R.u8_A << 1) & 0xFF);
            if (i != 0) {
                R.u8_A = (char) ((R.u8_A | 0x01) & 0xFF);
            } else {
                R.u8_A = (char) ((R.u8_A & 0xfe) & 0xFF);
            }
        }
    };
    /* NS990113 */
    static opcode rlc_a = new opcode() {
        public void handler() {
            char/*UINT8*/ i = M_Cy();
            if ((R.u8_A & 0x80) != 0) {
                SET(C_FLAG);
            } else {
                CLR(C_FLAG);
            }
            R.u8_A = (char) ((R.u8_A << 1) & 0xFF);
            if (i != 0) {
                R.u8_A = (char) ((R.u8_A | 0x01) & 0xFF);
            } else {
                R.u8_A = (char) ((R.u8_A & 0xfe) & 0xFF);
            }
        }
    };
    static opcode rr_a = new opcode() {
        public void handler() {
            char i = (char) ((R.u8_A & 1) & 0xFF);
            R.u8_A = (char) ((R.u8_A >>> 1) & 0xFF);
            if (i != 0) {
                R.u8_A = (char) ((R.u8_A | 0x80) & 0xFF);
            } else {
                R.u8_A = (char) ((R.u8_A & 0x7f) & 0xFF);
            }
        }
    };
    /* NS990113 */
    static opcode rrc_a = new opcode() {
        public void handler() {
            char i = M_Cy();
            if ((R.u8_A & 1) != 0) {
                SET(C_FLAG);
            } else {
                CLR(C_FLAG);
            }
            R.u8_A = (char) ((R.u8_A >>> 1) & 0xFF);
            if (i != 0) {
                R.u8_A = (char) ((R.u8_A | 0x80) & 0xFF);
            } else {
                R.u8_A = (char) ((R.u8_A & 0x7f) & 0xFF);
            }
        }
    };
    static opcode sel_mb0 = new opcode() {
        public void handler() {
            R.A11 = 0;
            R.A11ff = 0;
        }
    };
    static opcode sel_mb1 = new opcode() {
        public void handler() {
            R.A11ff = 0x800;
            if (R.irq_executing == I8039_IGNORE_INT) {
                R.A11 = 0x800;
            }
        }
    };
    static opcode sel_rb0 = new opcode() {
        public void handler() {
            CLR(B_FLAG);
            R.regPtr = 0;
        }
    };
    static opcode sel_rb1 = new opcode() {
        public void handler() {
            SET(B_FLAG);
            R.regPtr = 24;
        }
    };
    static opcode stop_tcnt = new opcode() {
        public void handler() {
            R.u8_timerON = R.u8_countON = 0;
        }
    };
    static opcode strt_cnt = new opcode() {
        public void handler() {
            R.u8_countON = 1;
            u8_Old_T1 = test_r(1);
        }
    };
    /* NS990113 */
    static opcode strt_t = new opcode() {
        public void handler() {
            R.u8_timerON = 1;
            R.masterClock = 0;
        }
    };
    /* NS990113 */
    static opcode swap_a = new opcode() {
        public void handler() {
            char/*UINT8*/ i = (char) ((R.u8_A >>> 4) & 0xFF);
            R.u8_A = (char) ((R.u8_A << 4) & 0xFF);
            R.u8_A = (char) ((R.u8_A | i) & 0xFF);
        }
    };
    static opcode xch_a_r0 = new opcode() {
        public void handler() {
            /*UINT8*/
            char i = (char) (R.u8_A & 0xFF);
            R.u8_A = (char) ((R.u8_RAM[R.regPtr]) & 0xFF);
            R.u8_RAM[R.regPtr] = i;
        }
    };
    static opcode xch_a_r1 = new opcode() {
        public void handler() {
            /*UINT8*/
            char i = (char) (R.u8_A & 0xFF);
            R.u8_A = (char) ((R.u8_RAM[R.regPtr + 1]) & 0xFF);
            R.u8_RAM[R.regPtr + 1] = i;
        }
    };
    static opcode xch_a_r2 = new opcode() {
        public void handler() {
            /*UINT8*/
            char i = (char) (R.u8_A & 0xFF);
            R.u8_A = (char) ((R.u8_RAM[R.regPtr + 2]) & 0xFF);
            R.u8_RAM[R.regPtr + 2] = i;
        }
    };
    static opcode xch_a_r3 = new opcode() {
        public void handler() {
            /*UINT8*/
            char i = (char) (R.u8_A & 0xFF);
            R.u8_A = (char) ((R.u8_RAM[R.regPtr + 3]) & 0xFF);
            R.u8_RAM[R.regPtr + 3] = i;
        }
    };
    static opcode xch_a_r4 = new opcode() {
        public void handler() {
            /*UINT8*/
            char i = (char) (R.u8_A & 0xFF);
            R.u8_A = (char) ((R.u8_RAM[R.regPtr + 4]) & 0xFF);
            R.u8_RAM[R.regPtr + 4] = i;
        }
    };
    static opcode xch_a_r5 = new opcode() {
        public void handler() {
            /*UINT8*/
            char i = (char) (R.u8_A & 0xFF);
            R.u8_A = (char) ((R.u8_RAM[R.regPtr + 5]) & 0xFF);
            R.u8_RAM[R.regPtr + 5] = i;
        }
    };
    static opcode xch_a_r6 = new opcode() {
        public void handler() {
            /*UINT8*/
            char i = (char) (R.u8_A & 0xFF);
            R.u8_A = (char) ((R.u8_RAM[R.regPtr + 6]) & 0xFF);
            R.u8_RAM[R.regPtr + 6] = i;
        }
    };
    static opcode xch_a_r7 = new opcode() {
        public void handler() {
            /*UINT8*/
            char i = (char) (R.u8_A & 0xFF);
            R.u8_A = (char) ((R.u8_RAM[R.regPtr + 7]) & 0xFF);
            R.u8_RAM[R.regPtr + 7] = i;
        }
    };
    static opcode xch_a_xr0 = new opcode() {
        public void handler() {
            /*UINT8*/
            char i = (char) (R.u8_A & 0xFF);
            R.u8_A = (char) ((R.u8_RAM[R.u8_RAM[R.regPtr] & 0x7f]) & 0xFF);
            R.u8_RAM[R.u8_RAM[R.regPtr] & 0x7f] = i;
        }
    };
    static opcode xch_a_xr1 = new opcode() {
        public void handler() {
            /*UINT8*/
            char i = (char) (R.u8_A & 0xFF);
            R.u8_A = (char) ((R.u8_RAM[R.u8_RAM[R.regPtr + 1] & 0x7f]) & 0xFF);
            R.u8_RAM[R.u8_RAM[R.regPtr + 1] & 0x7f] = i;
        }
    };
    static opcode xchd_a_xr0 = new opcode() {
        public void handler() {
            M_XCHD(R.u8_RAM[R.regPtr] & 0x7f);
        }
    };
    static opcode xchd_a_xr1 = new opcode() {
        public void handler() {
            M_XCHD(R.u8_RAM[R.regPtr + 1] & 0x7f);
        }
    };
    static opcode xrl_a_n = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A ^ M_RDMEM_OPCODE()) & 0xFF);
        }
    };
    static opcode xrl_a_r0 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A ^ R.u8_RAM[R.regPtr]) & 0xFF);
        }
    };
    static opcode xrl_a_r1 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A ^ R.u8_RAM[R.regPtr + 1]) & 0xFF);
        }
    };
    static opcode xrl_a_r2 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A ^ R.u8_RAM[R.regPtr + 2]) & 0xFF);
        }
    };
    static opcode xrl_a_r3 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A ^ R.u8_RAM[R.regPtr + 3]) & 0xFF);
        }
    };
    static opcode xrl_a_r4 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A ^ R.u8_RAM[R.regPtr + 4]) & 0xFF);
        }
    };
    static opcode xrl_a_r5 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A ^ R.u8_RAM[R.regPtr + 5]) & 0xFF);
        }
    };
    static opcode xrl_a_r6 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A ^ R.u8_RAM[R.regPtr + 6]) & 0xFF);
        }
    };
    static opcode xrl_a_r7 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A ^ R.u8_RAM[R.regPtr + 7]) & 0xFF);
        }
    };
    static opcode xrl_a_xr0 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A ^ R.u8_RAM[R.u8_RAM[R.regPtr] & 0x7f]) & 0xFF);
        }
    };
    static opcode xrl_a_xr1 = new opcode() {
        public void handler() {
            R.u8_A = (char) ((R.u8_A ^ R.u8_RAM[R.u8_RAM[R.regPtr + 1] & 0x7f]) & 0xFF);
        }
    };

    static s_opcode opcode_main[]
            = {
                new s_opcode(1, nop), new s_opcode(0, illegal), new s_opcode(2, outl_bus_a), new s_opcode(2, add_a_n), new s_opcode(2, jmp), new s_opcode(1, en_i), new s_opcode(0, illegal), new s_opcode(1, dec_a),
                new s_opcode(2, ins_a_bus), new s_opcode(2, in_a_p1), new s_opcode(2, in_a_p2), new s_opcode(0, illegal), new s_opcode(2, movd_a_p4), new s_opcode(2, movd_a_p5), new s_opcode(2, movd_a_p6), new s_opcode(2, movd_a_p7),
                new s_opcode(1, inc_xr0), new s_opcode(1, inc_xr1), new s_opcode(2, jb_0), new s_opcode(2, adc_a_n), new s_opcode(2, call), new s_opcode(1, dis_i), new s_opcode(2, jtf), new s_opcode(1, inc_a),
                new s_opcode(1, inc_r0), new s_opcode(1, inc_r1), new s_opcode(1, inc_r2), new s_opcode(1, inc_r3), new s_opcode(1, inc_r4), new s_opcode(1, inc_r5), new s_opcode(1, inc_r6), new s_opcode(1, inc_r7),
                new s_opcode(1, xch_a_xr0), new s_opcode(1, xch_a_xr1), new s_opcode(0, illegal), new s_opcode(2, mov_a_n), new s_opcode(2, jmp_1), new s_opcode(1, en_tcnti), new s_opcode(2, jnt_0), new s_opcode(1, clr_a),
                new s_opcode(1, xch_a_r0), new s_opcode(1, xch_a_r1), new s_opcode(1, xch_a_r2), new s_opcode(1, xch_a_r3), new s_opcode(1, xch_a_r4), new s_opcode(1, xch_a_r5), new s_opcode(1, xch_a_r6), new s_opcode(1, xch_a_r7),
                new s_opcode(1, xchd_a_xr0), new s_opcode(1, xchd_a_xr1), new s_opcode(2, jb_1), new s_opcode(0, illegal), new s_opcode(2, call_1), new s_opcode(1, dis_tcnti), new s_opcode(2, jt_0), new s_opcode(1, cpl_a),
                new s_opcode(0, illegal), new s_opcode(2, outl_p1_a), new s_opcode(2, outl_p2_a), new s_opcode(0, illegal), new s_opcode(2, movd_p4_a), new s_opcode(2, movd_p5_a), new s_opcode(2, movd_p6_a), new s_opcode(2, movd_p7_a),
                new s_opcode(1, orl_a_xr0), new s_opcode(1, orl_a_xr1), new s_opcode(1, mov_a_t), new s_opcode(2, orl_a_n), new s_opcode(2, jmp_2), new s_opcode(1, strt_cnt), new s_opcode(2, jnt_1), new s_opcode(1, swap_a),
                new s_opcode(1, orl_a_r0), new s_opcode(1, orl_a_r1), new s_opcode(1, orl_a_r2), new s_opcode(1, orl_a_r3), new s_opcode(1, orl_a_r4), new s_opcode(1, orl_a_r5), new s_opcode(1, orl_a_r6), new s_opcode(1, orl_a_r7),
                new s_opcode(1, anl_a_xr0), new s_opcode(1, anl_a_xr1), new s_opcode(2, jb_2), new s_opcode(2, anl_a_n), new s_opcode(2, call_2), new s_opcode(1, strt_t), new s_opcode(2, jt_1), new s_opcode(1, daa_a),
                new s_opcode(1, anl_a_r0), new s_opcode(1, anl_a_r1), new s_opcode(1, anl_a_r2), new s_opcode(1, anl_a_r3), new s_opcode(1, anl_a_r4), new s_opcode(1, anl_a_r5), new s_opcode(1, anl_a_r6), new s_opcode(1, anl_a_r7),
                new s_opcode(1, add_a_xr0), new s_opcode(1, add_a_xr1), new s_opcode(1, mov_t_a), new s_opcode(0, illegal), new s_opcode(2, jmp_3), new s_opcode(1, stop_tcnt), new s_opcode(0, illegal), new s_opcode(1, rrc_a),
                new s_opcode(1, add_a_r0), new s_opcode(1, add_a_r1), new s_opcode(1, add_a_r2), new s_opcode(1, add_a_r3), new s_opcode(1, add_a_r4), new s_opcode(1, add_a_r5), new s_opcode(1, add_a_r6), new s_opcode(1, add_a_r7),
                new s_opcode(1, adc_a_xr0), new s_opcode(1, adc_a_xr1), new s_opcode(2, jb_3), new s_opcode(0, illegal), new s_opcode(2, call_3), new s_opcode(1, ento_clk), new s_opcode(2, jf_1), new s_opcode(1, rr_a),
                new s_opcode(1, adc_a_r0), new s_opcode(1, adc_a_r1), new s_opcode(1, adc_a_r2), new s_opcode(1, adc_a_r3), new s_opcode(1, adc_a_r4), new s_opcode(1, adc_a_r5), new s_opcode(1, adc_a_r6), new s_opcode(1, adc_a_r7),
                new s_opcode(2, movx_a_xr0), new s_opcode(2, movx_a_xr1), new s_opcode(0, illegal), new s_opcode(2, ret), new s_opcode(2, jmp_4), new s_opcode(1, clr_f0), new s_opcode(2, jni), new s_opcode(0, illegal),
                new s_opcode(2, orl_bus_n), new s_opcode(2, orl_p1_n), new s_opcode(2, orl_p2_n), new s_opcode(0, illegal), new s_opcode(2, orld_p4_a), new s_opcode(2, orld_p5_a), new s_opcode(2, orld_p6_a), new s_opcode(2, orld_p7_a),
                new s_opcode(2, movx_xr0_a), new s_opcode(2, movx_xr1_a), new s_opcode(2, jb_4), new s_opcode(2, retr), new s_opcode(2, call_4), new s_opcode(1, cpl_f0), new s_opcode(2, jnz), new s_opcode(1, clr_c),
                new s_opcode(2, anl_bus_n), new s_opcode(2, anl_p1_n), new s_opcode(2, anl_p2_n), new s_opcode(0, illegal), new s_opcode(2, anld_p4_a), new s_opcode(2, anld_p5_a), new s_opcode(2, anld_p6_a), new s_opcode(2, anld_p7_a),
                new s_opcode(1, mov_xr0_a), new s_opcode(1, mov_xr1_a), new s_opcode(0, illegal), new s_opcode(2, movp_a_xa), new s_opcode(2, jmp_5), new s_opcode(1, clr_f1), new s_opcode(0, illegal), new s_opcode(1, cpl_c),
                new s_opcode(1, mov_r0_a), new s_opcode(1, mov_r1_a), new s_opcode(1, mov_r2_a), new s_opcode(1, mov_r3_a), new s_opcode(1, mov_r4_a), new s_opcode(1, mov_r5_a), new s_opcode(1, mov_r6_a), new s_opcode(1, mov_r7_a),
                new s_opcode(2, mov_xr0_n), new s_opcode(2, mov_xr1_n), new s_opcode(2, jb_5), new s_opcode(2, jmpp_xa), new s_opcode(2, call_5), new s_opcode(1, cpl_f1), new s_opcode(2, jf0), new s_opcode(0, illegal),
                new s_opcode(2, mov_r0_n), new s_opcode(2, mov_r1_n), new s_opcode(2, mov_r2_n), new s_opcode(2, mov_r3_n), new s_opcode(2, mov_r4_n), new s_opcode(2, mov_r5_n), new s_opcode(2, mov_r6_n), new s_opcode(2, mov_r7_n),
                new s_opcode(0, illegal), new s_opcode(0, illegal), new s_opcode(0, illegal), new s_opcode(0, illegal), new s_opcode(2, jmp_6), new s_opcode(1, sel_rb0), new s_opcode(2, jz), new s_opcode(1, mov_a_psw),
                new s_opcode(1, dec_r0), new s_opcode(1, dec_r1), new s_opcode(1, dec_r2), new s_opcode(1, dec_r3), new s_opcode(1, dec_r4), new s_opcode(1, dec_r5), new s_opcode(1, dec_r6), new s_opcode(1, dec_r7),
                new s_opcode(1, xrl_a_xr0), new s_opcode(1, xrl_a_xr1), new s_opcode(2, jb_6), new s_opcode(2, xrl_a_n), new s_opcode(2, call_6), new s_opcode(1, sel_rb1), new s_opcode(0, illegal), new s_opcode(1, mov_psw_a),
                new s_opcode(1, xrl_a_r0), new s_opcode(1, xrl_a_r1), new s_opcode(1, xrl_a_r2), new s_opcode(1, xrl_a_r3), new s_opcode(1, xrl_a_r4), new s_opcode(1, xrl_a_r5), new s_opcode(1, xrl_a_r6), new s_opcode(1, xrl_a_r7),
                new s_opcode(0, illegal), new s_opcode(0, illegal), new s_opcode(0, illegal), new s_opcode(2, movp3_a_xa), new s_opcode(2, jmp_7), new s_opcode(1, sel_mb0), new s_opcode(2, jnc), new s_opcode(1, rl_a),
                new s_opcode(2, djnz_r0), new s_opcode(2, djnz_r1), new s_opcode(2, djnz_r2), new s_opcode(2, djnz_r3), new s_opcode(2, djnz_r4), new s_opcode(2, djnz_r5), new s_opcode(2, djnz_r6), new s_opcode(2, djnz_r7),
                new s_opcode(1, mov_a_xr0), new s_opcode(1, mov_a_xr1), new s_opcode(2, jb_7), new s_opcode(0, illegal), new s_opcode(2, call_7), new s_opcode(1, sel_mb1), new s_opcode(2, jc), new s_opcode(1, rlc_a),
                new s_opcode(1, mov_a_r0), new s_opcode(1, mov_a_r1), new s_opcode(1, mov_a_r2), new s_opcode(1, mov_a_r3), new s_opcode(1, mov_a_r4), new s_opcode(1, mov_a_r5), new s_opcode(1, mov_a_r6), new s_opcode(1, mov_a_r7)};

    /**
     * **************************************************************************
     * Issue an interrupt if necessary
     * **************************************************************************
     */
    static int Timer_IRQ() {
        if (R.u8_tirq_en != 0 && R.irq_executing == 0) {
            //logerror("I8039:  TIMER INTERRUPT\n");
            R.irq_executing = I8039_TIMER_INT;
            push(R.PC.L);
            push((R.PC.H & 0x0f) | (R.u8_PSW & 0xf0));
            R.PC.SetD(0x07);

            R.A11ff = R.A11;
            R.A11 = 0;
            return 2;
            /* 2 clock cycles used */
        }
        return 0;
    }

    static int Ext_IRQ() {
        if (R.u8_xirq_en != 0) {
            //logerror("I8039:  EXT INTERRUPT\n");
            R.irq_executing = I8039_EXT_INT;
            push(R.PC.L);
            push((R.PC.H & 0x0f) | (R.u8_PSW & 0xf0));
            R.PC.SetD(0x03);
            R.A11ff = R.A11;
            R.A11 = 0;
            return 2;
            /* 2 clock cycles used */
        }
        return 0;
    }

    /**
     * **************************************************************************
     * Reset registers to their initial values
     * **************************************************************************
     */
    @Override
    public void reset(Object param) {
        R.PC.SetD(0);
        R.u8_SP = 0;
        R.u8_A = 0;
        R.u8_PSW = 0x08;
        /* Start with Carry SET, Bit 4 is always SET */
        memset(R.u8_RAM, 0x0, 128);
        R.u8_bus = 0;
        R.irq_executing = I8039_IGNORE_INT;
        R.pending_irq = I8039_IGNORE_INT;

        R.A11ff = R.A11 = 0;
        R.u8_timerON = R.u8_countON = 0;
        R.u8_tirq_en = R.u8_xirq_en = 0;
        R.u8_xirq_en = 0;
        /* NS990113 */
        R.u8_timerON = 1;
        /* Mario Bros. doesn't work without this */
        R.masterClock = 0;
    }

    /**
     * **************************************************************************
     * Shut down CPU emulation
     * **************************************************************************
     */
    @Override
    public void exit() {
        /* nothing to do ? */
    }

    /**
     * **************************************************************************
     * Execute cycles CPU cycles. Return number of cycles really executed
     * **************************************************************************
     */
    @Override
    public int execute(int cycles) {
        char opcode, T1;
        int count;

        i8039_ICount[0] = cycles;

        do {
            switch (R.pending_irq) {
                case I8039_COUNT_INT:
                case I8039_TIMER_INT:
                    count = Timer_IRQ();
                    i8039_ICount[0] -= count;
                    if (R.u8_timerON != 0) /* NS990113 */ {
                        R.masterClock += count;
                    }
                    R.u8_t_flag = 1;
                    break;
                case I8039_EXT_INT:
                    if (R.irq_callback != null) {
                        (R.irq_callback).handler(0);
                    }
                    count = Ext_IRQ();
                    i8039_ICount[0] -= count;
                    if (R.u8_timerON != 0) /* NS990113 */ {
                        R.masterClock += count;
                    }
                    break;
            }
            R.pending_irq = I8039_IGNORE_INT;

            R.PREPC.SetD(R.PC.D);

            opcode = M_RDOP(R.PC.D);

            /*      logerror("I8039:  PC = %04x,  opcode = %02x\n", R.PC.w.l, opcode); */
            R.PC.AddD(1);
            i8039_ICount[0] -= opcode_main[opcode].cycles;
            opcode_main[opcode].function.handler();

            if (R.u8_countON != 0) /* NS990113 */ {
                T1 = test_r(1);
                if (POSITIVE_EDGE_T1(T1) != 0) {
                    /* Handle COUNTER IRQs */
                    R.u8_timer = (char) ((R.u8_timer + 1) & 0xFF);
                    if (R.u8_timer == 0) {
                        R.pending_irq = I8039_COUNT_INT;
                    }

                    u8_Old_T1 = T1;
                }
            }

            if (R.u8_timerON != 0) {
                /* Handle TIMER IRQs */
                R.masterClock += opcode_main[opcode].cycles;
                if (R.masterClock >= 32) {
                    /* NS990113 */
                    R.masterClock -= 32;
                    R.u8_timer = (char) ((R.u8_timer + 1) & 0xFF);
                    if (R.u8_timer == 0) {
                        R.pending_irq = I8039_TIMER_INT;
                    }
                }
            }
        } while (i8039_ICount[0] > 0);

        return cycles - i8039_ICount[0];
    }

    /**
     * **************************************************************************
     * Get all registers in given buffer
     * **************************************************************************
     */
    @Override
    public Object get_context() {
        throw new UnsupportedOperationException("Not supported yet.");
        /*TODO*///	if( dst )
/*TODO*///		*(I8039_Regs*)dst = R;
/*TODO*///	return sizeof(I8039_Regs);
    }

    /**
     * **************************************************************************
     * Set all registers to given values
     * **************************************************************************
     */
    @Override
    public void set_context(Object reg) {
        throw new UnsupportedOperationException("Not supported yet.");
        /*TODO*///	if( src )
/*TODO*///	{
/*TODO*///		R = *(I8039_Regs*)src;
/*TODO*///		regPTR = ((M_By) ? 24 : 0);
/*TODO*///		R.SP = (R.PSW << 1) & 0x0f;
/*TODO*///		#ifdef MESS
/*TODO*///			change_pc(R.PC.w.l);
/*TODO*///		#endif
/*TODO*///	}
    }

    /**
     * **************************************************************************
     * Return program counter
     * **************************************************************************
     */
    @Override
    public int get_pc() {
        return R.PC.D;
    }

    /**
     * **************************************************************************
     * Return program counter
     * **************************************************************************
     */
    @Override
    public void set_pc(int val) {
        throw new UnsupportedOperationException("Not supported yet.");
        /*TODO*///	R.PC.w.l = val;
    }

    /**
     * **************************************************************************
     * Return stack pointer
     * **************************************************************************
     */
    @Override
    public int get_sp() {
        throw new UnsupportedOperationException("Not supported yet.");
        /*TODO*///	return R.SP;
    }

    /**
     * **************************************************************************
     * Set stack pointer
     * **************************************************************************
     */
    @Override
    public void set_sp(int val) {
        throw new UnsupportedOperationException("Not supported yet.");
        /*TODO*///	R.SP = val;
    }

    /**
     * *************************************************************************
     */
    /* Get a specific register                                                  */
    /**
     * *************************************************************************
     */
    @Override
    public int get_reg(int regnum) {
        throw new UnsupportedOperationException("Not supported yet.");
        /*TODO*///	switch( regnum )
/*TODO*///	{
/*TODO*///		case I8039_PC: return R.PC.w.l;
/*TODO*///		case I8039_SP: return R.SP;
/*TODO*///		case I8039_PSW: return R.PSW;
/*TODO*///        case I8039_A: return R.A;
/*TODO*///		case I8039_IRQ_STATE: return R.irq_state;
/*TODO*///		case I8039_R0: return R0;
/*TODO*///		case I8039_R1: return R1;
/*TODO*///		case I8039_R2: return R2;
/*TODO*///		case I8039_R3: return R3;
/*TODO*///		case I8039_R4: return R4;
/*TODO*///		case I8039_R5: return R5;
/*TODO*///		case I8039_R6: return R6;
/*TODO*///		case I8039_R7: return R7;
/*TODO*///		case REG_PREVIOUSPC: return R.PREPC.w.l;
/*TODO*///		default:
/*TODO*///			if( regnum <= REG_SP_CONTENTS )
/*TODO*///			{
/*TODO*///				unsigned offset = 8 + 2 * ((R.SP + REG_SP_CONTENTS - regnum) & 7);
/*TODO*///				return R.RAM[offset] + 256 * R.RAM[offset+1];
/*TODO*///			}
/*TODO*///	}
/*TODO*///	return 0;
    }

    /**
     * *************************************************************************
     */
    /* Set a specific register                                                  */
    /**
     * *************************************************************************
     */
    @Override
    public void set_reg(int regnum, /*unsigned*/ int val) {
        throw new UnsupportedOperationException("Not supported yet.");
        /*TODO*///	switch( regnum )
/*TODO*///	{
/*TODO*///		case I8039_PC: R.PC.w.l = val; break;
/*TODO*///		case I8039_SP: R.SP = val; break;
/*TODO*///		case I8039_PSW: R.PSW = val; break;
/*TODO*///		case I8039_A: R.A = val; break;
/*TODO*///		case I8039_IRQ_STATE: i8039_set_irq_line( 0, val ); break;
/*TODO*///		case I8039_R0: R0 = val; break;
/*TODO*///		case I8039_R1: R1 = val; break;
/*TODO*///		case I8039_R2: R2 = val; break;
/*TODO*///		case I8039_R3: R3 = val; break;
/*TODO*///		case I8039_R4: R4 = val; break;
/*TODO*///		case I8039_R5: R5 = val; break;
/*TODO*///		case I8039_R6: R6 = val; break;
/*TODO*///		case I8039_R7: R7 = val; break;
/*TODO*///		default:
/*TODO*///			if( regnum <= REG_SP_CONTENTS )
/*TODO*///			{
/*TODO*///				unsigned offset = 8 + 2 * ((R.SP + REG_SP_CONTENTS - regnum) & 7);
/*TODO*///				R.RAM[offset] = val & 0xff;
/*TODO*///				R.RAM[offset+1] = val >> 8;
/*TODO*///            }
/*TODO*///	}
    }

    /**
     * *************************************************************************
     */
    /* Set NMI line state														*/
    /**
     * *************************************************************************
     */
    @Override
    public void set_nmi_line(int linestate) {
        /* I8039 does not have a NMI line */
    }

    /**
     * *************************************************************************
     */
    /* Set IRQ line state														*/
    /**
     * *************************************************************************
     */
    @Override
    public void set_irq_line(int irqline, int state) {
        R.irq_state = state;
        if (state == CLEAR_LINE) {
            R.pending_irq &= ~I8039_EXT_INT;
        } else {
            R.pending_irq |= I8039_EXT_INT;
        }
    }

    /**
     * *************************************************************************
     */
    /* Set IRQ callback (interrupt acknowledge) 								*/
    /**
     * *************************************************************************
     */
    @Override
    public void set_irq_callback(irqcallbacksPtr callback) {
        R.irq_callback = callback;
    }

    @Override
    public String cpu_info(Object context, int regnum) {
        /*TODO*///	static char buffer[8][47+1];
/*TODO*///	static int which = 0;
/*TODO*///    I8039_Regs *r = context;
/*TODO*///
/*TODO*///	which = ++which % 8;
/*TODO*///	buffer[which][0] = '\0';
/*TODO*///	if( !context )
/*TODO*///		r = &R;
/*TODO*///
        switch (regnum) {
            /*TODO*///		case CPU_INFO_REG+I8039_PC: sprintf(buffer[which], "PC:%04X", r->PC.w.l); break;
/*TODO*///		case CPU_INFO_REG+I8039_SP: sprintf(buffer[which], "SP:%02X", r->SP); break;
/*TODO*///		case CPU_INFO_REG+I8039_PSW: sprintf(buffer[which], "PSW:%02X", r->PSW); break;
/*TODO*///        case CPU_INFO_REG+I8039_A: sprintf(buffer[which], "A:%02X", r->A); break;
/*TODO*///		case CPU_INFO_REG+I8039_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r->irq_state); break;
/*TODO*///		case CPU_INFO_REG+I8039_R0: sprintf(buffer[which], "R0:%02X", r->RAM[r->regPtr+0]); break;
/*TODO*///		case CPU_INFO_REG+I8039_R1: sprintf(buffer[which], "R1:%02X", r->RAM[r->regPtr+1]); break;
/*TODO*///		case CPU_INFO_REG+I8039_R2: sprintf(buffer[which], "R2:%02X", r->RAM[r->regPtr+2]); break;
/*TODO*///		case CPU_INFO_REG+I8039_R3: sprintf(buffer[which], "R3:%02X", r->RAM[r->regPtr+3]); break;
/*TODO*///		case CPU_INFO_REG+I8039_R4: sprintf(buffer[which], "R4:%02X", r->RAM[r->regPtr+4]); break;
/*TODO*///		case CPU_INFO_REG+I8039_R5: sprintf(buffer[which], "R5:%02X", r->RAM[r->regPtr+5]); break;
/*TODO*///		case CPU_INFO_REG+I8039_R6: sprintf(buffer[which], "R6:%02X", r->RAM[r->regPtr+6]); break;
/*TODO*///		case CPU_INFO_REG+I8039_R7: sprintf(buffer[which], "R7:%02X", r->RAM[r->regPtr+7]); break;
/*TODO*///		case CPU_INFO_FLAGS:
/*TODO*///			sprintf(buffer[which], "%c%c%c%c%c%c%c%c",
/*TODO*///				r->PSW & 0x80 ? 'C':'.',
/*TODO*///				r->PSW & 0x40 ? 'A':'.',
/*TODO*///				r->PSW & 0x20 ? 'F':'.',
/*TODO*///				r->PSW & 0x10 ? 'B':'.',
/*TODO*///				r->PSW & 0x08 ? '?':'.',
/*TODO*///				r->PSW & 0x04 ? '4':'.',
/*TODO*///				r->PSW & 0x02 ? '2':'.',
/*TODO*///				r->PSW & 0x01 ? '1':'.');
/*TODO*///			break;
            case CPU_INFO_NAME:
                return "I8039";
            case CPU_INFO_FAMILY:
                return "Intel 8039";
            case CPU_INFO_VERSION:
                return "1.1";
            case CPU_INFO_FILE:
                return "i8038.java";
            case CPU_INFO_CREDITS:
                return "Copyright (C) 1997 by Mirko Buffoni\nBased on the original work (C) 1997 by Dan Boris";
            /*TODO*///		case CPU_INFO_REG_LAYOUT: return (const char*)i8039_reg_layout;
/*TODO*///		case CPU_INFO_WIN_LAYOUT: return (const char*)i8039_win_layout;
        }

        /*TODO*///    return buffer[which];
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * no implementation in current cpu
     *
     */

    @Override
    public int memory_read(int offset) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void memory_write(int offset, int data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void internal_interrupt(int type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cpu_state_save(Object file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cpu_state_load(Object file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        Object reg = new I8039_Regs();
        return reg;
    }
}
