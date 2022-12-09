/*
 * ported to v0.37b7
 *
 */
package gr.codebb.arcadeflex.v037b7.machine;

import arcadeflex.v036.generic.funcPtr.TimerCallbackHandlerPtr;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import gr.codebb.arcadeflex.common.SubArrays.IntSubArray;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.M6809_FIRQ_LINE;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.M6809_IRQ_LINE;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.timer.*;
import static arcadeflex.v036.mame.timerH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.irobot.irobot_poly_clear;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.irobot.run_video;

public class irobot {

    /* Note:
     * There's probably something wrong with the way the Mathbox gets started.
	 * Try compiling with IR_TIMING=1, run with logging on and take a look at
	 * the resulting logilfe.
	 * The mathbox is started in short intervals (<10 scanlines) without (!)
	 * checking its idle status.
	 * It also seems that the mathbox in this emulation would have to cope with
	 * approx. 5000 instructions per scanline [look at the number of instructions
	 * and the number of scanlines to the next mathbox start]. This seems a bit
	 * too high.
     */
    //#define IR_TIMING 				1		/* try to emulate MB and VG running time */
    //#define DISASSEMBLE_MB_ROM		0		/* generate a disassembly of the mathbox ROMs */
    //#define IR_CPU_STATE \
    //	logerror(\
    //			"pc: %4x, scanline: %d\n", cpu_getpreviouspc(), cpu_getscanline())
    public static char /*UINT8*/ irvg_clear;
    public static char /*UINT8*/ irvg_vblank;
    public static char /*UINT8*/ irvg_running;
    public static char /*UINT8*/ irmb_running;
    static Object irscanline_timer;

    static Object irvg_timer;
    static Object irmb_timer;

    static UBytePtr[] comRAM = new UBytePtr[2];
    static UBytePtr mbRAM;
    static UBytePtr mbROM;
    public static char /*UINT8*/ irobot_control_num = 0;
    public static char /*UINT8*/ irobot_statwr;
    public static char /*UINT8*/ irobot_out0;
    public static char /*UINT8*/ irobot_outx, irobot_mpage;

    static UBytePtr irobot_combase_mb;
    public static UBytePtr irobot_combase;
    public static char /*UINT8*/ irobot_bufsel;
    public static char /*UINT8*/ irobot_alphamap;

    /* mathbox and vector data is stored in big-endian format */
    static int BYTE_XOR_LE(int x) {
        return x ^ 1;
    }

    /**
     * ********************************************************************
     */
    public static ReadHandlerPtr irobot_sharedmem_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (irobot_outx == 3) {
                return mbRAM.read(BYTE_XOR_LE(offset));
            }

            if (irobot_outx == 2) {
                return irobot_combase.read(BYTE_XOR_LE(offset & 0xFFF));
            }

            if (irobot_outx == 0) {
                return mbROM.read(((irobot_mpage & 1) << 13) + BYTE_XOR_LE(offset));
            }

            if (irobot_outx == 1) {
                return mbROM.read(0x4000 + ((irobot_mpage & 3) << 13) + BYTE_XOR_LE(offset));
            }

            return 0xFF;
        }
    };

    /* Comment out the mbRAM =, comRAM2 = or comRAM1 = and it will start working */
    public static WriteHandlerPtr irobot_sharedmem_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (irobot_outx == 3) {
                mbRAM.write(BYTE_XOR_LE(offset), data);
            }

            if (irobot_outx == 2) {
                irobot_combase.write(BYTE_XOR_LE(offset & 0xFFF), data);
            }
        }
    };

    public static TimerCallbackHandlerPtr irvg_done_callback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            //logerror("vg done. ");
            //IR_CPU_STATE;
            irvg_running = 0;
        }
    };

    public static WriteHandlerPtr irobot_statwr_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            //logerror("write %2x ", data);
            //IR_CPU_STATE;

            irobot_combase = comRAM[data >> 7];
            irobot_combase_mb = comRAM[(data >> 7) ^ 1];
            irobot_bufsel = (char) ((data & 0x02) & 0xFF);
            if (((data & 0x01) == 0x01) && (irvg_clear == 0)) {
                irobot_poly_clear();
            }

            irvg_clear = (char) ((data & 0x01) & 0xFF);

            if ((data & 0x04) != 0 && (irobot_statwr & 0x04) == 0) {
                run_video();
                if (irvg_running == 0) {
                    //logerror("vg start ");
                    //IR_CPU_STATE;
                    irvg_timer = timer_set(TIME_IN_MSEC(10), 0, irvg_done_callback);
                } else {
                    //logerror("vg start [busy!] ");
                    //IR_CPU_STATE;
                    timer_reset(irvg_timer, TIME_IN_MSEC(10));
                }
                irvg_running = 1;
            }
            if ((data & 0x10) != 0 && (irobot_statwr & 0x10) == 0) {
                irmb_run();
            }
            irobot_statwr = (char) (data & 0xFF);
        }
    };

    public static WriteHandlerPtr irobot_out0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UBytePtr RAM = memory_region(REGION_CPU1);

            irobot_out0 = (char) (data & 0xFF);
            switch (data & 0x60) {
                case 0:
                    cpu_setbank(2, new UBytePtr(RAM, 0x1C000));
                    break;
                case 0x20:
                    cpu_setbank(2, new UBytePtr(RAM, 0x1C800));
                    break;
                case 0x40:
                    cpu_setbank(2, new UBytePtr(RAM, 0x1D000));
                    break;
            }
            irobot_outx = (char) (((data & 0x18) >> 3) & 0xFF);
            irobot_mpage = (char) (((data & 0x06) >> 1) & 0xFF);
            irobot_alphamap = (char) ((data & 0x80) & 0xFF);
        }
    };

    public static WriteHandlerPtr irobot_rom_banksel_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UBytePtr RAM = memory_region(REGION_CPU1);

            switch ((data & 0x0E) >> 1) {
                case 0:
                    cpu_setbank(1, new UBytePtr(RAM, 0x10000));
                    break;
                case 1:
                    cpu_setbank(1, new UBytePtr(RAM, 0x12000));
                    break;
                case 2:
                    cpu_setbank(1, new UBytePtr(RAM, 0x14000));
                    break;
                case 3:
                    cpu_setbank(1, new UBytePtr(RAM, 0x16000));
                    break;
                case 4:
                    cpu_setbank(1, new UBytePtr(RAM, 0x18000));
                    break;
                case 5:
                    cpu_setbank(1, new UBytePtr(RAM, 0x1A000));
                    break;
            }
            //set_led_status(0,data & 0x10);
            //set_led_status(1,data & 0x20);
        }
    };
    public static TimerCallbackHandlerPtr scanline_callback = new TimerCallbackHandlerPtr() {
        public void handler(int scanline) {
            if (scanline == 0) {
                irvg_vblank = 0;
            }
            if (scanline == 224) {
                irvg_vblank = 1;
            }
            //logerror("SCANLINE CALLBACK %d\n", scanline);
            /* set the IRQ line state based on the 32V line state */
            cpu_set_irq_line(0, M6809_IRQ_LINE, (scanline & 32) != 0 ? ASSERT_LINE : CLEAR_LINE);

            /* set a callback for the next 32-scanline increment */
            scanline += 32;
            if (scanline >= 256) {
                scanline = 0;
            }
            irscanline_timer = timer_set(cpu_getscanlinetime(scanline), scanline, scanline_callback);
        }
    };
    public static InitMachinePtr irobot_init_machine = new InitMachinePtr() {
        public void handler() {
            UBytePtr MB = memory_region(REGION_CPU2);

            /* initialize the memory regions */
            mbROM = new UBytePtr(MB, 0x00000);
            mbRAM = new UBytePtr(MB, 0x0c000);
            comRAM[0] = new UBytePtr(MB, 0x0e000);
            comRAM[1] = new UBytePtr(MB, 0x0f000);

            irvg_vblank = 0;
            irvg_running = 0;
            irmb_running = 0;

            /* set an initial timer to go off on scanline 0 */
            irscanline_timer = timer_set(cpu_getscanlinetime(0), 0, scanline_callback);

            irobot_rom_banksel_w.handler(0, 0);
            irobot_out0_w.handler(0, 0);
            irobot_combase = comRAM[0];
            irobot_combase_mb = comRAM[1];
            irobot_outx = 0;
        }
    };

    public static WriteHandlerPtr irobot_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            irobot_control_num = (char) ((offset & 0x03) & 0xFF);
        }
    };

    public static ReadHandlerPtr irobot_control_r = new ReadHandlerPtr() {
        public int handler(int offset) {

            if (irobot_control_num == 0) {
                return readinputport(5);
            } else if (irobot_control_num == 1) {
                return readinputport(6);
            }
            return 0;

        }
    };

    /*  we allow irmb_running and irvg_running to appear running before clearing
        them to simulate the mathbox and vector generator running in real time */
    public static ReadHandlerPtr irobot_status_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int d = 0;

            //logerror("status read. ");
            //IR_CPU_STATE;
            if (irmb_running == 0) {
                d |= 0x20;
            }
            if (irvg_running != 0) {
                d |= 0x40;
            }

            //        d = (irmb_running * 0x20) | (irvg_running * 0x40);
            if (irvg_vblank != 0) {
                d = d | 0x80;
            }
            return d;
        }
    };

    /**
     * *********************************************************************
     * I-Robot Mathbox
     * <p>
     * Based on 4 2901 chips slice processors connected to form a 16-bit ALU
     * <p>
     * Microcode roms: 6N: bits 0..3: Address of ALU A register 5P: bits 0..3:
     * Address of ALU B register 6M: bits 0..3: ALU Function bits 5..8 7N: bits
     * 0..3: ALU Function bits 1..4 8N: bits 0,1: Memory write timing bit 2:
     * Hardware multiply mode bit 3: ALU Function bit 0 6P: bits 0,1: Direct
     * addressing bits 0,1 bits 2,3: Jump address bits 0,1 8M: bits 0..3: Jump
     * address bits 6..9 9N: bits 0..3: Jump address bits 2..5 8P: bits 0..3:
     * Memory address bits 2..5 9M: bit 0: Shift control bits 1..3: Jump type 0
     * = No Jump 1 = On carry 2 = On zero 3 = On positive 4 = On negative 5 =
     * Unconditional 6 = Jump to Subroutine 7 = Return from Subroutine 7M: Bit
     * 0: Mathbox memory enable Bit 1: Latch data to address bus Bit 2: Carry in
     * select Bit 3: Carry in value (if 2,3 = 11 then mathbox is done) 9P: Bit
     * 0: Hardware divide enable Bits 1,2: Memory select Bit 3: Memory R/W 7P:
     * Bits 0,1: Direct addressing bits 6,7 Bits 2,3: Unused
     **********************************************************************
     */
    public static final int FL_MULT = 0x01;
    public static final int FL_shift = 0x02;
    public static final int FL_MBMEMDEC = 0x04;
    public static final int FL_ADDEN = 0x08;
    public static final int FL_DPSEL = 0x10;
    public static final int FL_carry = 0x20;
    public static final int FL_DIV = 0x40;
    public static final int FL_MBRW = 0x80;

    public static class irmb_ops {

        int nxtop;//const struct irmb_ops *nxtop;
        int/*UINT32*/ func;
        int/*UINT32*/ diradd;
        int/*UINT32*/ latchmask;
        IntSubArray/*UINT32*/ areg;
        IntSubArray/*UINT32*/ breg;
        char/*UINT8*/ cycles;
        char/*UINT8*/ diren;
        char/*UINT8*/ flags;
        char/*UINT8*/ ramsel;
    }

    static irmb_ops[] mbops;

    static int[] irmb_stack = new int[16];
    static /*UINT32*/ int[] irmb_regs = new int[16];
    static /*UINT32*/ int irmb_latch;

    static int /*UINT32*/ irmb_din(int curop) {
        int /*UINT32*/ d = 0;

        if ((mbops[curop].flags & FL_MBMEMDEC) == 0 && (mbops[curop].flags & FL_MBRW) != 0) {
            int/*UINT32*/ ad = mbops[curop].diradd | (irmb_latch & mbops[curop].latchmask);

            if (mbops[curop].diren != 0 || (irmb_latch & 0x6000) == 0) {
                d = mbRAM.READ_WORD((ad << 1) & 0x1fff);		/* MB RAM read */
            } else if ((irmb_latch & 0x4000) != 0) {
                d = mbROM.READ_WORD((ad << 1) + 0x4000);		/* MB ROM read, CEMATH = 1 */
            } else {
                d = mbROM.READ_WORD((ad << 1) & 0x3fff);		/* MB ROM read, CEMATH = 0 */
            }
        }
        return d;
    }

    static void irmb_dout(int curop, /*UINT32*/ int d) {
        /* Write to video com ram */
        if (mbops[curop].ramsel == 3) {
            irobot_combase_mb.WRITE_WORD((irmb_latch << 1) & 0xfff, d);
        }

        /* Write to mathox ram */
        if ((mbops[curop].flags & FL_MBMEMDEC) == 0) {
            int /*UINT32*/ ad = mbops[curop].diradd | (irmb_latch & mbops[curop].latchmask);

            if (mbops[curop].diren != 0 || (irmb_latch & 0x6000) == 0) {
                mbRAM.WRITE_WORD((ad << 1) & 0x1fff, d);		/* MB RAM write */
            }
        }
    }


    /* Convert microcode roms to a more usable form */
    public static void load_oproms() {
        UBytePtr MB = memory_region(REGION_CPU2);
        int i;

        /* allocate RAM */
        mbops = new irmb_ops[1024];
        for (int k = 0; k < 1024; k++) {
            mbops[k] = new irmb_ops();
        }
        if (mbops == null) {
            return;
        }

        for (i = 0; i < 1024; i++) {
            int nxtadd, func, ramsel, diradd, latchmask, dirmask, time;

            mbops[i].areg = new IntSubArray(irmb_regs, MB.read(0xC000 + i) & 0x0F);
            mbops[i].breg = new IntSubArray(irmb_regs, MB.read(0xC400 + i) & 0x0F);
            func = (MB.read(0xC800 + i) & 0x0F) << 5;
            func |= ((MB.read(0xCC00 + i) & 0x0F) << 1);
            func |= (MB.read(0xD000 + i) & 0x08) >> 3;
            time = MB.read(0xD000 + i) & 0x03;
            mbops[i].flags = (char) ((MB.read(0xD000 + i) & 0x04) >> 2);
            nxtadd = (MB.read(0xD400 + i) & 0x0C) >> 2;
            diradd = MB.read(0xD400 + i) & 0x03;
            nxtadd |= ((MB.read(0xD800 + i) & 0x0F) << 6);
            nxtadd |= ((MB.read(0xDC00 + i) & 0x0F) << 2);
            diradd |= (MB.read(0xE000 + i) & 0x0F) << 2;
            func |= (MB.read(0xE400 + i) & 0x0E) << 9;
            mbops[i].flags |= (MB.read(0xE400 + i) & 0x01) << 1;
            mbops[i].flags |= (MB.read(0xE800 + i) & 0x0F) << 2;
            mbops[i].flags |= ((MB.read(0xEC00 + i) & 0x01) << 6);
            mbops[i].flags |= (MB.read(0xEC00 + i) & 0x08) << 4;
            ramsel = (MB.read(0xEC00 + i) & 0x06) >> 1;
            diradd |= (MB.read(0xF000 + i) & 0x03) << 6;

            if ((mbops[i].flags & FL_shift) != 0) {
                func |= 0x200;
            }

            mbops[i].func = func;
            mbops[i].nxtop = nxtadd;//&mbops[nxtadd];

            /* determine the number of 12MHz cycles for this operation */
            if (time == 3) {
                mbops[i].cycles = 2;
            } else {
                mbops[i].cycles = (char) (3 + time);
            }

            /* precompute the hardcoded address bits and the mask to be used on the latch value */
            if (ramsel == 0) {
                dirmask = 0x00FC;
                latchmask = 0x3000;
            } else {
                dirmask = 0x0000;
                latchmask = 0x3FFC;
            }
            if ((ramsel & 2) != 0) {
                latchmask |= 0x0003;
            } else {
                dirmask |= 0x0003;
            }

            mbops[i].ramsel = (char) (ramsel & 0xFF);
            mbops[i].diradd = diradd & dirmask;
            mbops[i].latchmask = latchmask;
            mbops[i].diren = (char) ((ramsel == 0) ? 1 : 0);

        }
    }


    /* Init mathbox (only called once) */
    public static InitDriverPtr init_irobot = new InitDriverPtr() {
        public void handler() {
            int i;
            for (i = 0; i < 16; i++) {
                irmb_stack[i] = 0;//&mbops[0];
                irmb_regs[i] = 0;
            }
            irmb_latch = 0;
            load_oproms();
        }
    };

    public static TimerCallbackHandlerPtr irmb_done_callback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            //logerror("mb done. ");
            //IR_CPU_STATE;
            irmb_running = 0;
            cpu_set_irq_line(0, M6809_FIRQ_LINE, ASSERT_LINE);
        }
    };


    /* Run mathbox */
    public static void irmb_run() {
        int prevop = 0;//const irmb_ops *prevop = &mbops[0];
        int curop = 0;//const irmb_ops *curop = &mbops[0];

        int/*UINT32*/ Q = 0;
        int/*UINT32*/ Y = 0;
        int/*UINT32*/ nflag = 0;
        int/*UINT32*/ vflag = 0;
        int/*UINT32*/ cflag = 0;
        int/*UINT32*/ zresult = 1;
        int/*UINT32*/ CI = 0;
        int/*UINT32*/ SP = 0;
        int/*UINT32*/ icount = 0;

        while ((mbops[prevop].flags & (FL_DPSEL | FL_carry)) != (FL_DPSEL | FL_carry)) {
            int/*UINT32*/ result = 0;
            int/*UINT32*/ fu;
            int/*UINT32*/ tmp;

            icount += mbops[curop].cycles;

            /* Get function code */
            fu = mbops[curop].func;

            /* Modify function for MULT */
            if ((mbops[prevop].flags & FL_MULT) == 0 || (Q & 1) != 0) {
                fu = fu ^ 0x02;
            } else {
                fu = fu | 0x02;
            }

            /* Modify function for DIV */
            if ((mbops[prevop].flags & FL_DIV) != 0 || nflag != 0) {
                fu = fu ^ 0x08;
            } else {
                fu = fu | 0x08;
            }

            /* Do source and operation */
            switch (fu & 0x03f) {
                case 0x00: {
                    //ADD(*curop.areg, Q);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    result = mbops[curop].areg.read(0) + Q + CI;
                    cflag = (result >> 16) & 1;
                    vflag = (((mbops[curop].areg.read(0) & 0x7fff) + (Q & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x01: {
                    //ADD(*curop.areg, *curop.breg);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    result = mbops[curop].areg.read(0) + mbops[curop].breg.read(0) + CI;
                    cflag = (result >> 16) & 1;
                    vflag = (((mbops[curop].areg.read(0) & 0x7fff) + (mbops[curop].breg.read(0) & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x02: {
                    //ADD(0, Q);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    result = 0 + Q + CI;
                    cflag = (result >> 16) & 1;
                    vflag = (((0 & 0x7fff) + (Q & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x03: {
                    //ADD(0, *curop.breg);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    result = 0 + mbops[curop].breg.read(0) + CI;
                    cflag = (result >> 16) & 1;
                    vflag = (((0 & 0x7fff) + (mbops[curop].breg.read(0) & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x04: {
                    //ADD(0, *curop.areg);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    result = 0 + mbops[curop].areg.read(0) + CI;
                    cflag = (result >> 16) & 1;
                    vflag = (((0 & 0x7fff) + (mbops[curop].areg.read(0) & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x05: {
                    tmp = irmb_din(curop);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    //ADD(tmp, *curop.areg);
                    result = tmp + mbops[curop].areg.read(0) + CI;
                    cflag = (result >> 16) & 1;
                    vflag = (((tmp & 0x7fff) + (mbops[curop].areg.read(0) & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x06: {
                    tmp = irmb_din(curop);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    //ADD(tmp, Q);
                    result = tmp + Q + CI;
                    cflag = (result >> 16) & 1;
                    vflag = (((tmp & 0x7fff) + (Q & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x07: {
                    tmp = irmb_din(curop);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    //ADD(tmp, 0);
                    result = tmp + 0 + CI;
                    cflag = (result >> 16) & 1;
                    vflag = (((tmp & 0x7fff) + (0 & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x08: {
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    //SUBR(*curop.areg, Q);
                    result = (mbops[curop].areg.read(0) ^ 0xFFFF) + Q + CI;
                    /*S - R + CI - 1*/
                    cflag = (result >> 16) & 1;
                    vflag = (((Q & 0x7fff) + ((mbops[curop].areg.read(0) ^ 0xffff) & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x09: {
                    //SUBR(*curop.areg, *curop.breg);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    result = (mbops[curop].areg.read(0) ^ 0xFFFF) + mbops[curop].breg.read(0) + CI;
                    /*S - R + CI - 1*/
                    cflag = (result >> 16) & 1;
                    vflag = (((mbops[curop].breg.read(0) & 0x7fff) + ((mbops[curop].areg.read(0) ^ 0xffff) & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x0a: {
                    //SUBR(0, Q);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    result = (0 ^ 0xFFFF) + Q + CI;
                    /*S - R + CI - 1*/
                    cflag = (result >> 16) & 1;
                    vflag = (((Q & 0x7fff) + ((0 ^ 0xffff) & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x0b: {
                    //SUBR(0, *curop.breg);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    result = (0 ^ 0xFFFF) + mbops[curop].breg.read(0) + CI;
                    /*S - R + CI - 1*/
                    cflag = (result >> 16) & 1;
                    vflag = (((mbops[curop].breg.read(0) & 0x7fff) + ((0 ^ 0xffff) & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x0c: {
                    //SUBR(0, *curop.areg);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    result = (0 ^ 0xFFFF) + mbops[curop].areg.read(0) + CI;
                    /*S - R + CI - 1*/
                    cflag = (result >> 16) & 1;
                    vflag = (((mbops[curop].areg.read(0) & 0x7fff) + ((0 ^ 0xffff) & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x0d: {
                    tmp = irmb_din(curop);
                    //SUBR(tmp, *curop.areg);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    result = (tmp ^ 0xFFFF) + mbops[curop].areg.read(0) + CI;
                    /*S - R + CI - 1*/
                    cflag = (result >> 16) & 1;
                    vflag = (((mbops[curop].areg.read(0) & 0x7fff) + ((tmp ^ 0xffff) & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x0e: {
                    tmp = irmb_din(curop);
                    //SUBR(tmp, Q);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    result = (tmp ^ 0xFFFF) + Q + CI;
                    /*S - R + CI - 1*/
                    cflag = (result >> 16) & 1;
                    vflag = (((Q & 0x7fff) + ((tmp ^ 0xffff) & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x0f: {
                    tmp = irmb_din(curop);
                    //SUBR(tmp, 0);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    result = (tmp ^ 0xFFFF) + 0 + CI;
                    /*S - R + CI - 1*/
                    cflag = (result >> 16) & 1;
                    vflag = (((0 & 0x7fff) + ((tmp ^ 0xffff) & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x10: {
                    //SUB(*curop.areg, Q);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    result = mbops[curop].areg.read(0) + (Q ^ 0xFFFF) + CI;
                    /*R - S + CI - 1*/
                    cflag = (result >> 16) & 1;
                    vflag = (((mbops[curop].areg.read(0) & 0x7fff) + ((Q ^ 0xffff) & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x11: {
                    //SUB(*curop.areg, *curop.breg);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    result = mbops[curop].areg.read(0) + (mbops[curop].breg.read(0) ^ 0xFFFF) + CI;
                    /*R - S + CI - 1*/
                    cflag = (result >> 16) & 1;
                    vflag = (((mbops[curop].areg.read(0) & 0x7fff) + ((mbops[curop].breg.read(0) ^ 0xffff) & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x12: {
                    //SUB(0, Q);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    result = 0 + (Q ^ 0xFFFF) + CI;
                    /*R - S + CI - 1*/
                    cflag = (result >> 16) & 1;
                    vflag = (((0 & 0x7fff) + ((Q ^ 0xffff) & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x13: {
                    //SUB(0, *curop.breg);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    result = 0 + (mbops[curop].breg.read(0) ^ 0xFFFF) + CI;
                    /*R - S + CI - 1*/
                    cflag = (result >> 16) & 1;
                    vflag = (((0 & 0x7fff) + ((mbops[curop].breg.read(0) ^ 0xffff) & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x14: {
                    //SUB(0, *curop.areg);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    result = 0 + (mbops[curop].areg.read(0) ^ 0xFFFF) + CI;
                    /*R - S + CI - 1*/
                    cflag = (result >> 16) & 1;
                    vflag = (((0 & 0x7fff) + ((mbops[curop].areg.read(0) ^ 0xffff) & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x15: {
                    tmp = irmb_din(curop);
                    //SUB(tmp, *curop.areg);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    result = tmp + (mbops[curop].areg.read(0) ^ 0xFFFF) + CI;
                    /*R - S + CI - 1*/
                    cflag = (result >> 16) & 1;
                    vflag = (((tmp & 0x7fff) + ((mbops[curop].areg.read(0) ^ 0xffff) & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x16: {
                    tmp = irmb_din(curop);
                    //SUB(tmp, Q);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    result = tmp + (Q ^ 0xFFFF) + CI;
                    /*R - S + CI - 1*/
                    cflag = (result >> 16) & 1;
                    vflag = (((tmp & 0x7fff) + ((Q ^ 0xffff) & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x17: {
                    tmp = irmb_din(curop);
                    //SUB(tmp, 0);
                    CI = 0;
                    if ((mbops[curop].flags & FL_DPSEL) != 0) {
                        CI = cflag;
                    } else {
                        if ((mbops[curop].flags & FL_carry) != 0) {
                            CI = 1;
                        }
                        if ((mbops[prevop].flags & FL_DIV) == 0 && nflag == 0) {
                            CI = 1;
                        }
                    }
                    result = tmp + (0 ^ 0xFFFF) + CI;
                    /*R - S + CI - 1*/
                    cflag = (result >> 16) & 1;
                    vflag = (((tmp & 0x7fff) + ((0 ^ 0xffff) & 0x7fff) + CI) >> 15) ^ cflag;
                }
                break;
                case 0x18:
                    result = mbops[curop].areg.read(0) | Q;
                    vflag = cflag = 0;/*OR(*curop.areg, Q);				*/
                    break;
                case 0x19:
                    result = mbops[curop].areg.read(0) | mbops[curop].breg.read(0);
                    vflag = cflag = 0;/*OR(*curop.areg, *curop.breg);	*/
                    break;
                case 0x1a:
                    result = 0 | Q;
                    vflag = cflag = 0;/*OR(0, Q);							*/
                    break;
                case 0x1b:
                    result = 0 | mbops[curop].breg.read(0);
                    vflag = cflag = 0;/*OR(0, *curop.breg);					*/
                    break;
                case 0x1c:
                    result = 0 | mbops[curop].areg.read(0);
                    vflag = cflag = 0;/*OR(0, *curop.areg);					*/
                    break;
                case 0x1d:
                    result = irmb_din(curop) | mbops[curop].areg.read(0);
                    vflag = cflag = 0;/*OR(irmb_din(curop), *curop.areg);	*/
                    break;
                case 0x1e:
                    result = irmb_din(curop) | Q;
                    vflag = cflag = 0;/*OR(irmb_din(curop), Q);				*/
                    break;
                case 0x1f:
                    result = irmb_din(curop) | 0;
                    vflag = cflag = 0;/*OR(irmb_din(curop), 0);				*/
                    break;
                case 0x20:
                    result = mbops[curop].areg.read(0) & Q;
                    vflag = cflag = 0;/*AND(*curop.areg, Q);		*/
                    break;
                case 0x21:
                    result = mbops[curop].areg.read(0) & mbops[curop].breg.read(0);
                    vflag = cflag = 0;/*AND(*curop.areg, *curop.breg);	*/
                    break;
                case 0x22:
                    result = 0 & Q;
                    vflag = cflag = 0;/*AND(0, Q);						*/
                    break;
                case 0x23:
                    result = 0 & mbops[curop].breg.read(0);
                    vflag = cflag = 0;/*AND(0, *curop.breg);			*/
                    break;
                case 0x24:
                    result = 0 & mbops[curop].areg.read(0);
                    vflag = cflag = 0;/*AND(0, *curop.areg);			*/
                    break;
                case 0x25:
                    result = irmb_din(curop) & mbops[curop].areg.read(0);
                    vflag = cflag = 0;/*AND(irmb_din(curop), *curop.areg); */
                    break;
                case 0x26:
                    result = irmb_din(curop) & Q;
                    vflag = cflag = 0;/*AND(irmb_din(curop), Q);			*/
                    break;
                case 0x27:
                    result = irmb_din(curop) & 0;
                    vflag = cflag = 0;/*AND(irmb_din(curop), 0);			*/
                    break;
                case 0x28:
                    result = (mbops[curop].areg.read(0) ^ 0xFFFF) & Q;
                    vflag = cflag = 0;/*IAND(*curop.areg, Q);	*/
                    break;
                case 0x29:
                    result = (mbops[curop].areg.read(0) ^ 0xFFFF) & mbops[curop].breg.read(0);
                    vflag = cflag = 0;/*IAND(*curop.areg, *curop.breg);	*/
                    break;
                case 0x2a:
                    result = (0 ^ 0xFFFF) & Q;
                    vflag = cflag = 0;/*IAND(0, Q);						*/
                    break;
                case 0x2b:
                    result = (0 ^ 0xFFFF) & mbops[curop].breg.read(0);
                    vflag = cflag = 0;/*IAND(0, *curop.breg);			*/
                    break;
                case 0x2c:
                    result = (0 ^ 0xFFFF) & mbops[curop].areg.read(0);
                    vflag = cflag = 0;/*IAND(0, *curop.areg);			*/
                    break;
                case 0x2d:
                    result = (irmb_din(curop) ^ 0xFFFF) & mbops[curop].areg.read(0);
                    vflag = cflag = 0;/*IAND(irmb_din(curop), *curop.areg);*/
                    break;
                case 0x2e:
                    result = (irmb_din(curop) ^ 0xFFFF) & Q;
                    vflag = cflag = 0;/*IAND(irmb_din(curop), Q);			*/
                    break;
                case 0x2f:
                    result = (irmb_din(curop) ^ 0xFFFF) & 0;
                    vflag = cflag = 0;/*IAND(irmb_din(curop), 0);			*/
                    break;
                case 0x30:
                    result = mbops[curop].areg.read(0) ^ Q;
                    vflag = cflag = 0;
                    /*XOR(*curop.areg, Q);	*/
                    break;
                case 0x31:
                    result = mbops[curop].areg.read(0) ^ mbops[curop].breg.read(0);
                    vflag = cflag = 0;
                    /*XOR(*curop.areg, *curop.breg);	*/
                    break;
                case 0x32:
                    result = 0 ^ Q;
                    vflag = cflag = 0;
                    /*XOR(0, Q);	*/
                    break;
                case 0x33:
                    result = 0 ^ mbops[curop].breg.read(0);
                    vflag = cflag = 0;/* XOR(0, *curop.breg);*/
                    break;
                case 0x34:
                    result = 0 ^ mbops[curop].areg.read(0);
                    vflag = cflag = 0;
                    /*XOR(0, *curop.areg);		*/
                    break;
                case 0x35:
                    result = irmb_din(curop) ^ mbops[curop].areg.read(0);
                    vflag = cflag = 0;
                    /*XOR(irmb_din(curop), *curop.areg);*/
                    break;
                case 0x36:
                    result = irmb_din(curop) ^ Q;
                    vflag = cflag = 0;
                    /*XOR(irmb_din(curop), Q);	*/
                    break;
                case 0x37:
                    result = irmb_din(curop) ^ 0;
                    vflag = cflag = 0;
                    /*XOR(irmb_din(curop), 0);	*/
                    break;
                case 0x38: {
                    //IXOR(*curop.areg, Q);
                    result = (mbops[curop].areg.read(0) ^ Q) ^ 0xFFFF;
                    vflag = cflag = 0;
                }
                break;
                case 0x39: {
                    //IXOR(*curop.areg, *curop.breg);
                    result = (mbops[curop].areg.read(0) ^ mbops[curop].breg.read(0)) ^ 0xFFFF;
                    vflag = cflag = 0;
                }
                break;
                case 0x3a: {
                    //IXOR(0, Q);
                    result = (0 ^ Q) ^ 0xFFFF;
                    vflag = cflag = 0;
                }
                break;
                case 0x3b: {
                    //IXOR(0, *curop.breg);
                    result = (0 ^ mbops[curop].breg.read(0)) ^ 0xFFFF;
                    vflag = cflag = 0;
                }
                break;
                case 0x3c: {
                    //IXOR(0, *curop.areg);
                    result = (0 ^ mbops[curop].areg.read(0)) ^ 0xFFFF;
                    vflag = cflag = 0;
                }
                break;
                case 0x3d: {
                    //IXOR(irmb_din(curop), *curop.areg)
                    result = (irmb_din(curop) ^ mbops[curop].areg.read(0)) ^ 0xFFFF;
                    vflag = cflag = 0;
                }
                break;
                case 0x3e: {
                    //IXOR(irmb_din(curop), Q);
                    result = (irmb_din(curop) ^ Q) ^ 0xFFFF;
                    vflag = cflag = 0;
                }
                break;

                default:
                case 0x3f: {
                    // IXOR(irmb_din(curop), 0);
                    result = (irmb_din(curop) ^ 0) ^ 0xFFFF;
                    vflag = cflag = 0;
                }
                break;
            }

            /* Evaluate flags */
            zresult = result & 0xFFFF;
            nflag = zresult >> 15;

            prevop = curop;

            /* Do destination and jump */
            switch (fu >> 6) {
                case 0x00:
                case 0x08: {
                    Q = Y = zresult;
                    curop++;
                }
                break;
                case 0x01:
                case 0x09: {
                    Y = zresult;
                    curop++;
                }
                break;
                case 0x02:
                case 0x0a:
                    Y = mbops[curop].areg.read(0);
                    mbops[curop].breg.write(zresult);
                    curop++;
                    break;
                case 0x03:
                case 0x0b:
                    mbops[curop].breg.write(zresult);
                    Y = zresult;
                    curop++;
                    break;
                case 0x04:
                    mbops[curop].breg.write((zresult >> 1) | ((mbops[curop].flags & 0x20) << 10));
                    Q = (Q >> 1) | ((mbops[curop].flags & 0x20) << 10);
                    Y = zresult;
                    curop++;
                    break;
                case 0x05:
                    mbops[curop].breg.write((zresult >> 1) | ((mbops[curop].flags & 0x20) << 10));
                    Y = zresult;
                    curop++;
                    break;
                case 0x06:
                    mbops[curop].breg.write(zresult << 1);
                    Q = ((Q << 1) & 0xffff) | (nflag ^ 1);
                    Y = zresult;
                    curop++;
                    break;
                case 0x07:
                    mbops[curop].breg.write(zresult << 1);
                    Y = zresult;
                    curop++;
                    break;
                case 0x0c:
                    mbops[curop].breg.write((zresult >> 1) | ((nflag ^ vflag) << 15));
                    Q = (Q >> 1) | ((zresult & 0x01) << 15);
                    Y = zresult;
                    curop++;
                    break;
                case 0x0d:
                    mbops[curop].breg.write((zresult >> 1) | ((nflag ^ vflag) << 15));
                    Y = zresult;
                    curop++;
                    break;
                case 0x0e:
                    mbops[curop].breg.write((zresult << 1) | ((Q & 0x8000) >> 15));
                    Q = (Q << 1) & 0xffff;
                    Y = zresult;
                    curop++;
                    break;
                case 0x0f:
                    mbops[curop].breg.write((zresult << 1) | ((Q & 0x8000) >> 15));
                    Y = zresult;
                    curop++;
                    break;

                case 0x10:
                case 0x18:
                    Q = Y = zresult;
                    if (cflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x11:
                case 0x19:
                    Y = zresult;
                    if (cflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x12:
                case 0x1a:
                    Y = mbops[curop].areg.read(0);
                    mbops[curop].breg.write(zresult);
                    if (cflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x13:
                case 0x1b:
                    mbops[curop].breg.write(zresult);
                    Y = zresult;
                    if (cflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x14:
                    mbops[curop].breg.write((zresult >> 1) | ((mbops[curop].flags & 0x20) << 10));
                    Q = (Q >> 1) | ((mbops[curop].flags & 0x20) << 10);
                    Y = zresult;
                    if (cflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x15:
                    mbops[curop].breg.write((zresult >> 1) | ((mbops[curop].flags & 0x20) << 10));
                    Y = zresult;
                    if (cflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x16:
                    mbops[curop].breg.write(zresult << 1);
                    Q = ((Q << 1) & 0xffff) | (nflag ^ 1);
                    Y = zresult;
                    if (cflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x17:
                    mbops[curop].breg.write(zresult << 1);
                    Y = zresult;
                    if (cflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x1c:
                    mbops[curop].breg.write((zresult >> 1) | ((nflag ^ vflag) << 15));
                    Q = (Q >> 1) | ((zresult & 0x01) << 15);
                    Y = zresult;
                    if (cflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x1d:
                    mbops[curop].breg.write((zresult >> 1) | ((nflag ^ vflag) << 15));
                    Y = zresult;
                    if (cflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x1e:
                    mbops[curop].breg.write((zresult << 1) | ((Q & 0x8000) >> 15));
                    Q = (Q << 1) & 0xffff;
                    Y = zresult;
                    if (cflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x1f:
                    mbops[curop].breg.write((zresult << 1) | ((Q & 0x8000) >> 15));
                    Y = zresult;
                    if (cflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;

                case 0x20:
                case 0x28:
                    Q = Y = zresult;
                    if (zresult == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x21:
                case 0x29:
                    Y = zresult;
                    if (zresult == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x22:
                case 0x2a:
                    Y = mbops[curop].areg.read(0);
                    mbops[curop].breg.write(zresult);
                    if (zresult == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x23:
                case 0x2b:
                    mbops[curop].breg.write(zresult);
                    Y = zresult;
                    if (zresult == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x24:
                    mbops[curop].breg.write((zresult >> 1) | ((mbops[curop].flags & 0x20) << 10));
                    Q = (Q >> 1) | ((mbops[curop].flags & 0x20) << 10);
                    Y = zresult;
                    if (zresult == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x25:
                    mbops[curop].breg.write((zresult >> 1) | ((mbops[curop].flags & 0x20) << 10));
                    Y = zresult;
                    if (zresult == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x26:
                    mbops[curop].breg.write(zresult << 1);
                    Q = ((Q << 1) & 0xffff) | (nflag ^ 1);
                    Y = zresult;
                    if (zresult == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x27:
                    mbops[curop].breg.write(zresult << 1);
                    Y = zresult;
                    if (zresult == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x2c:
                    mbops[curop].breg.write((zresult >> 1) | ((nflag ^ vflag) << 15));
                    Q = (Q >> 1) | ((zresult & 0x01) << 15);
                    Y = zresult;
                    if (zresult == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x2d:
                    mbops[curop].breg.write((zresult >> 1) | ((nflag ^ vflag) << 15));
                    Y = zresult;
                    if (zresult == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x2e:
                    mbops[curop].breg.write((zresult << 1) | ((Q & 0x8000) >> 15));
                    Q = (Q << 1) & 0xffff;
                    Y = zresult;
                    if (zresult == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x2f:
                    mbops[curop].breg.write((zresult << 1) | ((Q & 0x8000) >> 15));
                    Y = zresult;
                    if (zresult == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;

                case 0x30:
                case 0x38:
                    Q = Y = zresult;
                    if (nflag == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x31:
                case 0x39:
                    Y = zresult;
                    if (nflag == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x32:
                case 0x3a:
                    Y = mbops[curop].areg.read(0);
                    mbops[curop].breg.write(zresult);
                    if (nflag == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x33:
                case 0x3b:
                    mbops[curop].breg.write(zresult);
                    Y = zresult;
                    if (nflag == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x34:
                    mbops[curop].breg.write((zresult >> 1) | ((mbops[curop].flags & 0x20) << 10));
                    Q = (Q >> 1) | ((mbops[curop].flags & 0x20) << 10);
                    Y = zresult;
                    if (nflag == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x35:
                    mbops[curop].breg.write((zresult >> 1) | ((mbops[curop].flags & 0x20) << 10));
                    Y = zresult;
                    if (nflag == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x36:
                    mbops[curop].breg.write(zresult << 1);
                    Q = ((Q << 1) & 0xffff) | (nflag ^ 1);
                    Y = zresult;
                    if (nflag == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x37:
                    mbops[curop].breg.write(zresult << 1);
                    Y = zresult;
                    if (nflag == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x3c:
                    mbops[curop].breg.write((zresult >> 1) | ((nflag ^ vflag) << 15));
                    Q = (Q >> 1) | ((zresult & 0x01) << 15);
                    Y = zresult;
                    if (nflag == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x3d:
                    mbops[curop].breg.write((zresult >> 1) | ((nflag ^ vflag) << 15));
                    Y = zresult;
                    if (nflag == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x3e:
                    mbops[curop].breg.write((zresult << 1) | ((Q & 0x8000) >> 15));
                    Q = (Q << 1) & 0xffff;
                    Y = zresult;
                    if (nflag == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x3f:
                    mbops[curop].breg.write((zresult << 1) | ((Q & 0x8000) >> 15));
                    Y = zresult;
                    if (nflag == 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;

                case 0x40:
                case 0x48:
                    Q = Y = zresult;
                    if (nflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x41:
                case 0x49:
                    Y = zresult;
                    if (nflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x42:
                case 0x4a:
                    Y = mbops[curop].areg.read(0);
                    mbops[curop].breg.write(zresult);
                    if (nflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x43:
                case 0x4b:
                    mbops[curop].breg.write(zresult);
                    Y = zresult;
                    if (nflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x44:
                    mbops[curop].breg.write((zresult >> 1) | ((mbops[curop].flags & 0x20) << 10));
                    Q = (Q >> 1) | ((mbops[curop].flags & 0x20) << 10);
                    Y = zresult;
                    if (nflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x45:
                    mbops[curop].breg.write((zresult >> 1) | ((mbops[curop].flags & 0x20) << 10));
                    Y = zresult;
                    if (nflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x46:
                    mbops[curop].breg.write(zresult << 1);
                    Q = ((Q << 1) & 0xffff) | (nflag ^ 1);
                    Y = zresult;
                    if (nflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x47:
                    mbops[curop].breg.write(zresult << 1);
                    Y = zresult;
                    if (nflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x4c:
                    mbops[curop].breg.write((zresult >> 1) | ((nflag ^ vflag) << 15));
                    Q = (Q >> 1) | ((zresult & 0x01) << 15);
                    Y = zresult;
                    if (nflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x4d:
                    mbops[curop].breg.write((zresult >> 1) | ((nflag ^ vflag) << 15));
                    Y = zresult;
                    if (nflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x4e:
                    mbops[curop].breg.write((zresult << 1) | ((Q & 0x8000) >> 15));
                    Q = (Q << 1) & 0xffff;
                    Y = zresult;
                    if (nflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;
                case 0x4f:
                    mbops[curop].breg.write((zresult << 1) | ((Q & 0x8000) >> 15));
                    Y = zresult;
                    if (nflag != 0) {
                        curop = mbops[curop].nxtop;
                    } else {
                        curop++;
                    }
                    break;

                case 0x50:
                case 0x58:
                    Q = Y = zresult;
                    curop = mbops[curop].nxtop;
                    break;
                case 0x51:
                case 0x59:
                    Y = zresult;
                    curop = mbops[curop].nxtop;
                    break;
                case 0x52:
                case 0x5a:
                    Y = mbops[curop].areg.read(0);
                    mbops[curop].breg.write(zresult);
                    curop = mbops[curop].nxtop;
                    break;
                case 0x53:
                case 0x5b:
                    mbops[curop].breg.write(zresult);
                    Y = zresult;
                    curop = mbops[curop].nxtop;
                    break;
                case 0x54:
                    mbops[curop].breg.write((zresult >> 1) | ((mbops[curop].flags & 0x20) << 10));
                    Q = (Q >> 1) | ((mbops[curop].flags & 0x20) << 10);
                    Y = zresult;
                    curop = mbops[curop].nxtop;
                    break;
                case 0x55:
                    mbops[curop].breg.write((zresult >> 1) | ((mbops[curop].flags & 0x20) << 10));
                    Y = zresult;
                    curop = mbops[curop].nxtop;
                    break;
                case 0x56:
                    mbops[curop].breg.write(zresult << 1);
                    Q = ((Q << 1) & 0xffff) | (nflag ^ 1);
                    Y = zresult;
                    curop = mbops[curop].nxtop;
                    break;
                case 0x57:
                    mbops[curop].breg.write(zresult << 1);
                    Y = zresult;
                    curop = mbops[curop].nxtop;
                    break;
                case 0x5c:
                    mbops[curop].breg.write((zresult >> 1) | ((nflag ^ vflag) << 15));
                    Q = (Q >> 1) | ((zresult & 0x01) << 15);
                    Y = zresult;
                    curop = mbops[curop].nxtop;
                    break;
                case 0x5d:
                    mbops[curop].breg.write((zresult >> 1) | ((nflag ^ vflag) << 15));
                    Y = zresult;
                    curop = mbops[curop].nxtop;
                    break;
                case 0x5e:
                    mbops[curop].breg.write((zresult << 1) | ((Q & 0x8000) >> 15));
                    Q = (Q << 1) & 0xffff;
                    Y = zresult;
                    curop = mbops[curop].nxtop;
                    break;
                case 0x5f:
                    mbops[curop].breg.write((zresult << 1) | ((Q & 0x8000) >> 15));
                    Y = zresult;
                    curop = mbops[curop].nxtop;
                    break;

                case 0x60:
                case 0x68:
                    Q = Y = zresult;
                    irmb_stack[SP] = curop + 1;
                    SP = (SP + 1) & 15;
                    curop = mbops[curop].nxtop;
                    break;
                case 0x61:
                case 0x69:
                    Y = zresult;
                    irmb_stack[SP] = curop + 1;
                    SP = (SP + 1) & 15;
                    curop = mbops[curop].nxtop;
                    break;
                case 0x62:
                case 0x6a:
                    Y = mbops[curop].areg.read(0);
                    mbops[curop].breg.write(zresult);
                    irmb_stack[SP] = curop + 1;
                    SP = (SP + 1) & 15;
                    curop = mbops[curop].nxtop;
                    break;
                case 0x63:
                case 0x6b:
                    mbops[curop].breg.write(zresult);
                    Y = zresult;
                    irmb_stack[SP] = curop + 1;
                    SP = (SP + 1) & 15;
                    curop = mbops[curop].nxtop;
                    break;
                case 0x64:
                    mbops[curop].breg.write((zresult >> 1) | ((mbops[curop].flags & 0x20) << 10));
                    Q = (Q >> 1) | ((mbops[curop].flags & 0x20) << 10);
                    Y = zresult;
                    irmb_stack[SP] = curop + 1;
                    SP = (SP + 1) & 15;
                    curop = mbops[curop].nxtop;
                    break;
                case 0x65:
                    mbops[curop].breg.write((zresult >> 1) | ((mbops[curop].flags & 0x20) << 10));
                    Y = zresult;
                    irmb_stack[SP] = curop + 1;
                    SP = (SP + 1) & 15;
                    curop = mbops[curop].nxtop;
                    break;
                case 0x66:
                    mbops[curop].breg.write(zresult << 1);
                    Q = ((Q << 1) & 0xffff) | (nflag ^ 1);
                    Y = zresult;
                    irmb_stack[SP] = curop + 1;
                    SP = (SP + 1) & 15;
                    curop = mbops[curop].nxtop;
                    break;
                case 0x67:
                    mbops[curop].breg.write(zresult << 1);
                    Y = zresult;
                    irmb_stack[SP] = curop + 1;
                    SP = (SP + 1) & 15;
                    curop = mbops[curop].nxtop;
                    break;
                case 0x6c:
                    mbops[curop].breg.write((zresult >> 1) | ((nflag ^ vflag) << 15));
                    Q = (Q >> 1) | ((zresult & 0x01) << 15);
                    Y = zresult;
                    irmb_stack[SP] = curop + 1;
                    SP = (SP + 1) & 15;
                    curop = mbops[curop].nxtop;
                    break;
                case 0x6d:
                    mbops[curop].breg.write((zresult >> 1) | ((nflag ^ vflag) << 15));
                    Y = zresult;
                    irmb_stack[SP] = curop + 1;
                    SP = (SP + 1) & 15;
                    curop = mbops[curop].nxtop;
                    break;
                case 0x6e:
                    mbops[curop].breg.write((zresult << 1) | ((Q & 0x8000) >> 15));
                    Q = (Q << 1) & 0xffff;
                    Y = zresult;
                    irmb_stack[SP] = curop + 1;
                    SP = (SP + 1) & 15;
                    curop = mbops[curop].nxtop;
                    break;
                case 0x6f:
                    mbops[curop].breg.write((zresult << 1) | ((Q & 0x8000) >> 15));
                    Y = zresult;
                    irmb_stack[SP] = curop + 1;
                    SP = (SP + 1) & 15;
                    curop = mbops[curop].nxtop;
                    break;

                case 0x70:
                case 0x78:
                    Q = Y = zresult;
                    SP = (SP - 1) & 15;
                    curop = irmb_stack[SP];
                    break;
                case 0x71:
                case 0x79:
                    Y = zresult;
                    SP = (SP - 1) & 15;
                    curop = irmb_stack[SP];
                    break;
                case 0x72:
                case 0x7a:
                    Y = mbops[curop].areg.read(0);
                    mbops[curop].breg.write(zresult);
                    SP = (SP - 1) & 15;
                    curop = irmb_stack[SP];
                    break;
                case 0x73:
                case 0x7b:
                    mbops[curop].breg.write(zresult);
                    Y = zresult;
                    SP = (SP - 1) & 15;
                    curop = irmb_stack[SP];
                    break;
                case 0x74:
                    mbops[curop].breg.write((zresult >> 1) | ((mbops[curop].flags & 0x20) << 10));
                    Q = (Q >> 1) | ((mbops[curop].flags & 0x20) << 10);
                    Y = zresult;
                    SP = (SP - 1) & 15;
                    curop = irmb_stack[SP];
                    break;
                case 0x75:
                    mbops[curop].breg.write((zresult >> 1) | ((mbops[curop].flags & 0x20) << 10));
                    Y = zresult;
                    SP = (SP - 1) & 15;
                    curop = irmb_stack[SP];
                    break;
                case 0x76:
                    mbops[curop].breg.write(zresult << 1);
                    Q = ((Q << 1) & 0xffff) | (nflag ^ 1);
                    Y = zresult;
                    SP = (SP - 1) & 15;
                    curop = irmb_stack[SP];
                    break;
                case 0x77:
                    mbops[curop].breg.write(zresult << 1);
                    Y = zresult;
                    SP = (SP - 1) & 15;
                    curop = irmb_stack[SP];
                    break;
                case 0x7c:
                    mbops[curop].breg.write((zresult >> 1) | ((nflag ^ vflag) << 15));
                    Q = (Q >> 1) | ((zresult & 0x01) << 15);
                    Y = zresult;
                    SP = (SP - 1) & 15;
                    curop = irmb_stack[SP];
                    break;
                case 0x7d:
                    mbops[curop].breg.write((zresult >> 1) | ((nflag ^ vflag) << 15));
                    Y = zresult;
                    SP = (SP - 1) & 15;
                    curop = irmb_stack[SP];
                    break;
                case 0x7e:
                    mbops[curop].breg.write((zresult << 1) | ((Q & 0x8000) >> 15));
                    Q = (Q << 1) & 0xffff;
                    Y = zresult;
                    SP = (SP - 1) & 15;
                    curop = irmb_stack[SP];
                    break;
                case 0x7f:
                    mbops[curop].breg.write((zresult << 1) | ((Q & 0x8000) >> 15));
                    Y = zresult;
                    SP = (SP - 1) & 15;
                    curop = irmb_stack[SP];
                    break;
            }

            /* Do write */
            if ((mbops[prevop].flags & FL_MBRW) == 0) {
                irmb_dout(prevop, Y);
            }

            /* ADDEN */
            if ((mbops[prevop].flags & FL_ADDEN) == 0) {
                if ((mbops[prevop].flags & FL_MBRW) != 0) {
                    irmb_latch = irmb_din(prevop);
                } else {
                    irmb_latch = Y;
                }
            }
        }

        //logerror("%d instructions for Mathbox \n", icount);
        if (irmb_running == 0) {
            irmb_timer = timer_set(TIME_IN_HZ(12000000) * icount, 0, irmb_done_callback);
            //logerror("mb start ");
            //IR_CPU_STATE;
        } else {
            //logerror("mb start [busy!] ");
            //IR_CPU_STATE;
            timer_reset(irmb_timer, TIME_IN_NSEC(200) * icount);
        }
        irmb_running = 1;
    }
}
