/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.machine;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.vidhrdw.generic.videoram_w;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.segar.*;

public class segar {

    public static abstract interface sega_decryptPtr {

        public abstract void handler(int pc,/*unsinged*/ int[] lo);
    }

    public static sega_decryptPtr sega_decrypt;

    public static UBytePtr segar_mem = new UBytePtr();

    public static WriteHandlerPtr segar_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int pc, op, page, off;
            /*unsigned*/
            int[] bad = new int[1];

            off = offset;

            pc = cpu_getpreviouspc();
            if (pc != -1) {
                op = segar_mem.read(pc) & 0xFF;

                if (op == 0x32) {
                    bad[0] = offset & 0x00FF;
                    page = offset & 0xFF00;
                    (sega_decrypt).handler(pc, bad);
                    off = page | bad[0];
                }
            }

            /* MWA_ROM */
            if ((off >= 0x0000) && (off <= 0xC7FF)) {
                ;
            } /* MWA_RAM */ else if ((off >= 0xC800) && (off <= 0xCFFF)) {
                segar_mem.write(off, data);
            } else if ((off >= 0xE000) && (off <= 0xE3FF)) {
                videoram_w.handler(off - 0xE000, data);
            } /* MWA_RAM */ else if ((off >= 0xE400) && (off <= 0xE7FF)) {
                segar_mem.write(off, data);
            } else if ((off >= 0xE800) && (off <= 0xEFFF)) {
                segar_characterram_w.handler(off - 0xE800, data);
            } else if ((off >= 0xF000) && (off <= 0xF03F)) {
                segar_colortable_w.handler(off - 0xF000, data);
            } else if ((off >= 0xF040) && (off <= 0xF07F)) {
                segar_bcolortable_w.handler(off - 0xF040, data);
            } /* MWA_RAM */ else if ((off >= 0xF080) && (off <= 0xF7FF)) {
                segar_mem.write(off, data);
            } else if ((off >= 0xF800) && (off <= 0xFFFF)) {
                segar_characterram2_w.handler(off - 0xF800, data);
            } else {
                logerror("unmapped write at %04X:%02X\n", off, data);
            }
        }
    };

    /**
     * *************************************************************************
     */
    /* MB 971025 - Emulate Sega G80 security chip 315-0062                      */
    /**
     * *************************************************************************
     */
    public static sega_decryptPtr sega_decrypt62 = new sega_decryptPtr() {
        public void handler(int pc,/*unsinged*/ int[] lo) {
            /*unsigned*/
            int i = 0;
            /*unsigned*/
            int b = lo[0];

            switch (pc & 0x03) {
                case 0x00:
                    /* D */
                    i = b & 0x23;
                    i += ((b & 0xC0) >> 4);
                    i += ((b & 0x10) << 2);
                    i += ((b & 0x08) << 1);
                    i += (((~b) & 0x04) << 5);
                    i &= 0xFF;
                    break;
                case 0x01:
                    /* C */
                    i = b & 0x03;
                    i += ((b & 0x80) >> 4);
                    i += (((~b) & 0x40) >> 1);
                    i += ((b & 0x20) >> 1);
                    i += ((b & 0x10) >> 2);
                    i += ((b & 0x08) << 3);
                    i += ((b & 0x04) << 5);
                    i &= 0xFF;
                    break;
                case 0x02:
                    /* B */
                    i = b & 0x03;
                    i += ((b & 0x80) >> 1);
                    i += ((b & 0x60) >> 3);
                    i += ((~b) & 0x10);
                    i += ((b & 0x08) << 2);
                    i += ((b & 0x04) << 5);
                    i &= 0xFF;
                    break;
                case 0x03:
                    /* A */
                    i = b;
                    break;
            }

            lo[0] = i;
        }
    };

    /**
     * *************************************************************************
     */
    /* MB 971025 - Emulate Sega G80 security chip 315-0063                      */
    /**
     * *************************************************************************
     */
    public static sega_decryptPtr sega_decrypt63 = new sega_decryptPtr() {
        public void handler(int pc,/*unsinged*/ int[] lo) {
            /*unsigned*/
            int i = 0;
            /*unsigned*/
            int b = lo[0];

            switch (pc & 0x09) {
                case 0x00:
                    /* D */
                    i = b & 0x23;
                    i += ((b & 0xC0) >> 4);
                    i += ((b & 0x10) << 2);
                    i += ((b & 0x08) << 1);
                    i += (((~b) & 0x04) << 5);
                    i &= 0xFF;
                    break;
                case 0x01:
                    /* C */
                    i = b & 0x03;
                    i += ((b & 0x80) >> 4);
                    i += (((~b) & 0x40) >> 1);
                    i += ((b & 0x20) >> 1);
                    i += ((b & 0x10) >> 2);
                    i += ((b & 0x08) << 3);
                    i += ((b & 0x04) << 5);
                    i &= 0xFF;
                    break;
                case 0x08:
                    /* B */
                    i = b & 0x03;
                    i += ((b & 0x80) >> 1);
                    i += ((b & 0x60) >> 3);
                    i += ((~b) & 0x10);
                    i += ((b & 0x08) << 2);
                    i += ((b & 0x04) << 5);
                    i &= 0xFF;
                    break;
                case 0x09:
                    /* A */
                    i = b;
                    break;
            }

            lo[0] = i;
        }
    };

    /**
     * *************************************************************************
     */
    /* MB 971025 - Emulate Sega G80 security chip 315-0064                      */
    /**
     * *************************************************************************
     */
    public static sega_decryptPtr sega_decrypt64 = new sega_decryptPtr() {
        public void handler(int pc,/*unsinged*/ int[] lo) {
            /*unsigned*/
            int i = 0;
            /*unsigned*/
            int b = lo[0];

            switch (pc & 0x03) {
                case 0x00:
                    /* A */
                    i = b;
                    break;
                case 0x01:
                    /* B */
                    i = b & 0x03;
                    i += ((b & 0x80) >> 1);
                    i += ((b & 0x60) >> 3);
                    i += ((~b) & 0x10);
                    i += ((b & 0x08) << 2);
                    i += ((b & 0x04) << 5);
                    i &= 0xFF;
                    break;
                case 0x02:
                    /* C */
                    i = b & 0x03;
                    i += ((b & 0x80) >> 4);
                    i += (((~b) & 0x40) >> 1);
                    i += ((b & 0x20) >> 1);
                    i += ((b & 0x10) >> 2);
                    i += ((b & 0x08) << 3);
                    i += ((b & 0x04) << 5);
                    i &= 0xFF;
                    break;
                case 0x03:
                    /* D */
                    i = b & 0x23;
                    i += ((b & 0xC0) >> 4);
                    i += ((b & 0x10) << 2);
                    i += ((b & 0x08) << 1);
                    i += (((~b) & 0x04) << 5);
                    i &= 0xFF;
                    break;
            }

            lo[0] = i;
        }
    };

    /**
     * *************************************************************************
     */
    /* MB 971025 - Emulate Sega G80 security chip 315-0070                      */
    /**
     * *************************************************************************
     */
    public static sega_decryptPtr sega_decrypt70 = new sega_decryptPtr() {
        public void handler(int pc,/*unsinged*/ int[] lo) {
            /*unsigned*/
            int i = 0;
            /*unsigned*/
            int b = lo[0];

            switch (pc & 0x09) {
                case 0x00:
                    /* B */
                    i = b & 0x03;
                    i += ((b & 0x80) >> 1);
                    i += ((b & 0x60) >> 3);
                    i += ((~b) & 0x10);
                    i += ((b & 0x08) << 2);
                    i += ((b & 0x04) << 5);
                    i &= 0xFF;
                    break;
                case 0x01:
                    /* A */
                    i = b;
                    break;
                case 0x08:
                    /* D */
                    i = b & 0x23;
                    i += ((b & 0xC0) >> 4);
                    i += ((b & 0x10) << 2);
                    i += ((b & 0x08) << 1);
                    i += (((~b) & 0x04) << 5);
                    i &= 0xFF;
                    break;
                case 0x09:
                    /* C */
                    i = b & 0x03;
                    i += ((b & 0x80) >> 4);
                    i += (((~b) & 0x40) >> 1);
                    i += ((b & 0x20) >> 1);
                    i += ((b & 0x10) >> 2);
                    i += ((b & 0x08) << 3);
                    i += ((b & 0x04) << 5);
                    i &= 0xFF;
                    break;
            }

            lo[0] = i;
        }
    };

    /**
     * *************************************************************************
     */
    /* MB 971025 - Emulate Sega G80 security chip 315-0076                      */
    /**
     * *************************************************************************
     */
    public static sega_decryptPtr sega_decrypt76 = new sega_decryptPtr() {
        public void handler(int pc,/*unsinged*/ int[] lo) {
            /*unsigned*/
            int i = 0;
            /*unsigned*/
            int b = lo[0];

            switch (pc & 0x09) {
                case 0x00:
                    /* A */
                    i = b;
                    break;
                case 0x01:
                    /* B */
                    i = b & 0x03;
                    i += ((b & 0x80) >> 1);
                    i += ((b & 0x60) >> 3);
                    i += ((~b) & 0x10);
                    i += ((b & 0x08) << 2);
                    i += ((b & 0x04) << 5);
                    i &= 0xFF;
                    break;
                case 0x08:
                    /* C */
                    i = b & 0x03;
                    i += ((b & 0x80) >> 4);
                    i += (((~b) & 0x40) >> 1);
                    i += ((b & 0x20) >> 1);
                    i += ((b & 0x10) >> 2);
                    i += ((b & 0x08) << 3);
                    i += ((b & 0x04) << 5);
                    i &= 0xFF;
                    break;
                case 0x09:
                    /* D */
                    i = b & 0x23;
                    i += ((b & 0xC0) >> 4);
                    i += ((b & 0x10) << 2);
                    i += ((b & 0x08) << 1);
                    i += (((~b) & 0x04) << 5);
                    i &= 0xFF;
                    break;
            }

            lo[0] = i;
        }
    };

    /**
     * *************************************************************************
     */
    /* MB 971025 - Emulate Sega G80 security chip 315-0082                      */
    /**
     * *************************************************************************
     */
    public static sega_decryptPtr sega_decrypt82 = new sega_decryptPtr() {
        public void handler(int pc,/*unsinged*/ int[] lo) {
            /*unsigned*/
            int i = 0;
            /*unsigned*/
            int b = lo[0];

            switch (pc & 0x11) {
                case 0x00:
                    /* A */
                    i = b;
                    break;
                case 0x01:
                    /* B */
                    i = b & 0x03;
                    i += ((b & 0x80) >> 1);
                    i += ((b & 0x60) >> 3);
                    i += ((~b) & 0x10);
                    i += ((b & 0x08) << 2);
                    i += ((b & 0x04) << 5);
                    i &= 0xFF;
                    break;
                case 0x10:
                    /* C */
                    i = b & 0x03;
                    i += ((b & 0x80) >> 4);
                    i += (((~b) & 0x40) >> 1);
                    i += ((b & 0x20) >> 1);
                    i += ((b & 0x10) >> 2);
                    i += ((b & 0x08) << 3);
                    i += ((b & 0x04) << 5);
                    i &= 0xFF;
                    break;
                case 0x11:
                    /* D */
                    i = b & 0x23;
                    i += ((b & 0xC0) >> 4);
                    i += ((b & 0x10) << 2);
                    i += ((b & 0x08) << 1);
                    i += (((~b) & 0x04) << 5);
                    i &= 0xFF;
                    break;
            }

            lo[0] = i;
        }
    };

    /**
     * *************************************************************************
     */
    /* MB 971031 - Emulate no Sega G80 security chip                            */
    /**
     * *************************************************************************
     */
    public static sega_decryptPtr sega_decrypt0 = new sega_decryptPtr() {
        public void handler(int pc,/*unsinged*/ int[] lo) {
            return;
        }
    };

    /**
     * *************************************************************************
     */
    /* MB 971025 - Set the security chip to be used                             */
    /**
     * *************************************************************************
     */
    public static void sega_security(int chip) {
        switch (chip) {
            case 62:
                sega_decrypt = sega_decrypt62;
                break;
            case 63:
                sega_decrypt = sega_decrypt63;
                break;
            case 64:
                sega_decrypt = sega_decrypt64;
                break;
            case 70:
                sega_decrypt = sega_decrypt70;
                break;
            case 76:
                sega_decrypt = sega_decrypt76;
                break;
            case 82:
                sega_decrypt = sega_decrypt82;
                break;
            default:
                sega_decrypt = sega_decrypt0;
                break;
        }
    }

}
