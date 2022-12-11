/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package arcadeflex.v036.sndhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.sndintrf.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.sound.ay8910.*;
import static arcadeflex.v036.sound.ay8910H.*;
import static arcadeflex.v036.sound.MSM5205.*;
import static arcadeflex.v036.sound.MSM5205H.*;
import static arcadeflex.v036.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.cpu.m6800.m6803.*;

public class irem {

    public static WriteHandlerPtr irem_sound_cmd_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 0x80) == 0) {
                soundlatch_w.handler(0, data & 0x7f);
            } else {
                cpu_set_irq_line(1, 0, HOLD_LINE);
            }
        }
    };

    static int port1, port2;

    public static WriteHandlerPtr irem_port1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            port1 = data;
        }
    };

    public static WriteHandlerPtr irem_port2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* write latch */
            if (((port2 & 0x01) != 0) && ((data & 0x01) == 0)) {
                /* control or data port? */
                if ((port2 & 0x04) != 0) {
                    /* PSG 0 or 1? */
                    if ((port2 & 0x10) != 0) {
                        AY8910_control_port_1_w.handler(0, port1);
                    } else if ((port2 & 0x08) != 0) {
                        AY8910_control_port_0_w.handler(0, port1);
                    }
                } else {
                    /* PSG 0 or 1? */
                    if ((port2 & 0x10) != 0) {
                        AY8910_write_port_1_w.handler(0, port1);
                    } else if ((port2 & 0x08) != 0) {
                        AY8910_write_port_0_w.handler(0, port1);
                    }
                }
            }
            port2 = data;
        }
    };

    public static ReadHandlerPtr irem_port1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* PSG 0 or 1? */
            if ((port2 & 0x10) != 0) {
                return AY8910_read_port_1_r.handler(0);
            } else if ((port2 & 0x08) != 0) {
                return AY8910_read_port_0_r.handler(0);
            } else {
                return 0xff;
            }
        }
    };

    public static WriteHandlerPtr irem_adpcm_reset_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            MSM5205_reset_w.handler(0, data & 1);
            MSM5205_reset_w.handler(1, data & 2);
        }
    };

    public static vclk_InterruptHandlerPtr irem_adpcm_int = new vclk_InterruptHandlerPtr() {
        public void handler(int num) {
            cpu_set_nmi_line(1, PULSE_LINE);
        }
    };

    public static AY8910interface irem_ay8910_interface = new AY8910interface(
            2, /* 2 chips */
            910000, /* .91 MHz ?? */
            new int[]{20, 20},
            new ReadHandlerPtr[]{soundlatch_r, null},
            new ReadHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{irem_adpcm_reset_w, null}
    );

    public static MSM5205interface irem_msm5205_interface = new MSM5205interface(
            2, /* 2 chips            */
            384000, /* 384KHz             */
            new vclk_InterruptHandlerPtr[]{irem_adpcm_int, null},/* interrupt function */
            new int[]{MSM5205_S96_4B, MSM5205_S96_4B}, /* 4KHz  */
            new int[]{100, 100}
    );

    public static MemoryReadAddress irem_sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x001f, m6803_internal_registers_r),
                new MemoryReadAddress(0x0080, 0x00ff, MRA_RAM),
                new MemoryReadAddress(0x4000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    public static MemoryWriteAddress irem_sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x001f, m6803_internal_registers_w),
                new MemoryWriteAddress(0x0080, 0x00ff, MWA_RAM),
                new MemoryWriteAddress(0x0801, 0x0802, MSM5205_data_w),
                new MemoryWriteAddress(0x9000, 0x9000, MWA_NOP), /* IACK */
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    public static IOReadPort irem_sound_readport[]
            = {
                new IOReadPort(M6803_PORT1, M6803_PORT1, irem_port1_r),
                new IOReadPort(-1) /* end of table */};

    public static WriteHandlerPtr pip2 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            //if (errorlog != 0) fprintf(errorlog,"%04x: write %02x to port 2 ddr %02x\n",cpu_get_pc(),data,m6803_internal_registers_r(M6803_DDR2));
        }
    };

    public static IOWritePort irem_sound_writeport[]
            = {
                new IOWritePort(M6803_PORT1, M6803_PORT1, irem_port1_w),
                new IOWritePort(M6803_PORT2, M6803_PORT2, irem_port2_w),
                new IOWritePort(-1) /* end of table */};
}
