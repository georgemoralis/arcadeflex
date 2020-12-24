/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.codebb.arcadeflex.v036.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.dec8.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;
import static gr.codebb.arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound._2203intf.*;
import static gr.codebb.arcadeflex.v036.sound._2203intfH.*;
import static gr.codebb.arcadeflex.v036.sound.MSM5205H.*;
import static gr.codebb.arcadeflex.v036.sound.MSM5205.*;
import static gr.codebb.arcadeflex.v036.sound._3526intf.*;
import static gr.codebb.arcadeflex.v036.sound._3812intfH.*;
import static gr.codebb.arcadeflex.v036.cpu.m6502.m6502H.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.memory_set_opcode_base;

public class dec8 {

    /* Only used by ghostb, gondo, garyoret, other games can control buffering */
    public static VhEofCallbackPtr dec8_eof_callback = new VhEofCallbackPtr() {
        public void handler() {
            buffer_spriteram_w.handler(0, 0);
        }
    };
    /**
     * ***************************************************************************
     */

    static UBytePtr dec8_shared_ram = new UBytePtr();
    static UBytePtr dec8_shared2_ram = new UBytePtr();

    static int nmi_enable, int_enable;
    static int i8751_return, i8751_value;
    static int msm5205next;

    /**
     * ***************************************************************************
     */
    public static ReadHandlerPtr i8751_h_r = new ReadHandlerPtr() {
        public int handler(int offset) {//if (errorlog && cpu_get_pc()!=0xecde && cpu_get_pc()!=0xecd5 && cpu_get_pc()!=0xecd8) fprintf(errorlog,"PC %06x - Read from 8751 high\n",cpu_get_pc());
            return i8751_return >> 8; /* MSB */

        }
    };

    public static ReadHandlerPtr i8751_l_r = new ReadHandlerPtr() {
        public int handler(int offset) {//if (errorlog && cpu_get_pc()!=0xecde && cpu_get_pc()!=0xecd5 && cpu_get_pc()!=0xecd8) fprintf(errorlog,"PC %06x - Read from 8751 low\n",cpu_get_pc());
            return i8751_return & 0xff; /* LSB */

        }
    };

    public static WriteHandlerPtr i8751_reset_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            i8751_return = 0;
        }
    };
    /**
     * ***************************************************************************
     */
    public static ReadHandlerPtr gondo_player_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            switch (offset) {
                case 0: /* Rotary low byte */

                    return ~((1 << (readinputport(5) * 12 / 256)) & 0xff);
                case 1: /* Joystick = bottom 4 bits, rotary = top 4 */

                    return ((~((1 << (readinputport(5) * 12 / 256)) >> 4)) & 0xf0) | (readinputport(0) & 0xf);
            }
            return 0xff;
        }
    };
    public static ReadHandlerPtr gondo_player_2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            switch (offset) {
                case 0: /* Rotary low byte */

                    return ~((1 << (readinputport(6) * 12 / 256)) & 0xff);
                case 1: /* Joystick = bottom 4 bits, rotary = top 4 */

                    return ((~((1 << (readinputport(6) * 12 / 256)) >> 4)) & 0xf0) | (readinputport(1) & 0xf);
            }
            return 0xff;
        }
    };
    public static WriteHandlerPtr ghostb_i8751_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            i8751_return = 0;

            switch (offset) {
                case 0: /* High byte */

                    i8751_value = (i8751_value & 0xff) | (data << 8);
                    break;
                case 1: /* Low byte */

                    i8751_value = (i8751_value & 0xff00) | data;
                    break;
            }

            if (i8751_value == 0x00aa) {
                i8751_return = 0x655;
            }
            if (i8751_value == 0x021a) {
                i8751_return = 0x6e5; /* Ghostbusters ID */
            }
            if (i8751_value == 0x021b) {
                i8751_return = 0x6e4; /* Meikyuu Hunter G ID */
            }
        }
    };
    static int coins_srdarwin, latch_srdarwin;
    public static WriteHandlerPtr srdarwin_i8751_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            i8751_return = 0;

            switch (offset) {
                case 0: /* High byte */

                    i8751_value = (i8751_value & 0xff) | (data << 8);
                    break;
                case 1: /* Low byte */

                    i8751_value = (i8751_value & 0xff00) | data;
                    break;
            }

            if (i8751_value == 0x0000) {
                i8751_return = 0;
                coins_srdarwin = 0;
            }
            if (i8751_value == 0x3063) {
                i8751_return = 0x9c; /* Protection */
            }
            if ((i8751_value & 0xff00) == 0x4000) {
                i8751_return = i8751_value; /* Coinage settings */
            }
            if (i8751_value == 0x5000) {
                i8751_return = ((coins_srdarwin / 10) << 4) | (coins_srdarwin % 10); /* Coin request */
            }
            if (i8751_value == 0x6000) {
                i8751_value = -1;
                coins_srdarwin--;
            } /* Coin clear */
            /* Nb:  Command 0x4000 for setting coinage options is not supported */

            if ((readinputport(4) & 1) == 1) {
                latch_srdarwin = 1;
            }
            if ((readinputport(4) & 1) != 1 && latch_srdarwin != 0) {
                coins_srdarwin++;
                latch_srdarwin = 0;
            }

            if (i8751_value == 0x8000) {
                i8751_return = 0xf580 + 0; /* Boss #1: Snake + Bee */
            }
            if (i8751_value == 0x8001) {
                i8751_return = 0xf580 + 30; /* Boss #2: 4 Corners */
            }
            if (i8751_value == 0x8002) {
                i8751_return = 0xf580 + 26; /* Boss #3: Clock */
            }
            if (i8751_value == 0x8003) {
                i8751_return = 0xf580 + 6; /* Boss #4: Pyramid */
            }
            if (i8751_value == 0x8004) {
                i8751_return = 0xf580 + 12; /* Boss #5: Grey things */
            }
            if (i8751_value == 0x8005) {
                i8751_return = 0xf580 + 20; /* Boss #6: Ground Base?! */
            }
            if (i8751_value == 0x8006) {
                i8751_return = 0xf580 + 28; /* Boss #7: Dragon */
            }
            if (i8751_value == 0x8007) {
                i8751_return = 0xf580 + 32; /* Boss #8: Teleport */
            }
            if (i8751_value == 0x8008) {
                i8751_return = 0xf580 + 38; /* Boss #9: Octopus (Pincer) */
            }
            if (i8751_value == 0x8009) {
                i8751_return = 0xf580 + 40; /* Boss #10: Bird */
            }
        }
    };
    static int coin1_gondo, coin2_gondo, latch_gondo, snd_gondo;
    public static WriteHandlerPtr gondo_i8751_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            i8751_return = 0;

            switch (offset) {
                case 0: /* High byte */

                    i8751_value = (i8751_value & 0xff) | (data << 8);
                    if (int_enable != 0) {
                        cpu_cause_interrupt(0, M6809_INT_IRQ); /* IRQ on *high* byte only */
                    }
                    break;
                case 1: /* Low byte */

                    i8751_value = (i8751_value & 0xff00) | data;
                    break;
            }

            /* Coins are controlled by the i8751 */
            if ((readinputport(4) & 3) == 3) {
                latch_gondo = 1;
            }
            if ((readinputport(4) & 1) != 1 && latch_gondo != 0) {
                coin1_gondo++;
                snd_gondo = 1;
                latch_gondo = 0;
            }
            if ((readinputport(4) & 2) != 2 && latch_gondo != 0) {
                coin2_gondo++;
                snd_gondo = 1;
                latch_gondo = 0;
            }

            /* Work out return values */
            if (i8751_value == 0x0000) {
                i8751_return = 0;
                coin1_gondo = coin2_gondo = snd_gondo = 0;
            }
            if (i8751_value == 0x038a) {
                i8751_return = 0x375; /* Makyou Senshi ID */
            }
            if (i8751_value == 0x038b) {
                i8751_return = 0x374; /* Gondomania ID */
            }
            if ((i8751_value >> 8) == 0x04) {
                i8751_return = 0x40f; /* Coinage settings (Not supported) */
            }
            if ((i8751_value >> 8) == 0x05) {
                i8751_return = 0x500 | ((coin1_gondo / 10) << 4) | (coin1_gondo % 10);
            } /* Coin 1 */

            if ((i8751_value >> 8) == 0x06 && coin1_gondo != 0 && offset == 0) {
                i8751_return = 0x600;
                coin1_gondo--;
            } /* Coin 1 clear */

            if ((i8751_value >> 8) == 0x07) {
                i8751_return = 0x700 | ((coin2_gondo / 10) << 4) | (coin2_gondo % 10);
            } /* Coin 2 */

            if ((i8751_value >> 8) == 0x08 && coin2_gondo != 0 && offset == 0) {
                i8751_return = 0x800;
                coin2_gondo--;
            } /* Coin 2 clear */
            /* Commands 0x9xx do nothing */

            if ((i8751_value >> 8) == 0x0a) {
                i8751_return = 0xa00 | snd_gondo;
                if (snd_gondo != 0) {
                    snd_gondo = 0;
                }
            }
        }
    };
    static int coin1_shackled, coin2_shackled, latch_shackled = 0;
    public static WriteHandlerPtr shackled_i8751_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            i8751_return = 0;

            switch (offset) {
                case 0: /* High byte */

                    i8751_value = (i8751_value & 0xff) | (data << 8);
                    cpu_cause_interrupt(1, M6809_INT_FIRQ); /* Signal main cpu */

                    break;
                case 1: /* Low byte */

                    i8751_value = (i8751_value & 0xff00) | data;
                    break;
            }

	//if (errorlog) fprintf(errorlog,"PC %06x - Write %02x to 8751 %d\n",cpu_get_pc(),data,offset);
            /* Coins are controlled by the i8751 */
            if (/*(readinputport(2)&3)==3*/latch_shackled == 0) {
                latch_shackled = 1;
                coin1_shackled = coin2_shackled = 0;
            }
            if ((readinputport(2) & 1) != 1 && latch_shackled != 0) {
                coin1_shackled = 1;
                latch_shackled = 0;
            }
            if ((readinputport(2) & 2) != 2 && latch_shackled != 0) {
                coin2_shackled = 1;
                latch_shackled = 0;
            }

            if (i8751_value == 0x0050) {
                i8751_return = 0; /* Breywood ID */
            }
            if (i8751_value == 0x0051) {
                i8751_return = 0; /* Shackled ID */
            }
            if (i8751_value == 0x0102) {
                i8751_return = 0; /* ?? */
            }
            if (i8751_value == 0x0101) {
                i8751_return = 0; /* ?? */
            }
            if (i8751_value == 0x8101) {
                i8751_return = ((coin2_shackled / 10) << 4) | (coin2_shackled % 10)
                        | ((((coin1_shackled / 10) << 4) | (coin1_shackled % 10)) << 8); /* Coins */
            }
        }
    };
    static int coin_lastmiss, latch_lastmiss = 0, snd_lastmiss;
    public static WriteHandlerPtr lastmiss_i8751_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            i8751_return = 0;

            switch (offset) {
                case 0: /* High byte */

                    i8751_value = (i8751_value & 0xff) | (data << 8);
                    cpu_cause_interrupt(0, M6809_INT_FIRQ); /* Signal main cpu */

                    break;
                case 1: /* Low byte */

                    i8751_value = (i8751_value & 0xff00) | data;
                    break;
            }

            /* Coins are controlled by the i8751 */
            if ((readinputport(2) & 3) == 3 && latch_lastmiss == 0) {
                latch_lastmiss = 1;
            }
            if ((readinputport(2) & 3) != 3 && latch_lastmiss == 0) {
                coin_lastmiss++;
                latch_lastmiss = 0;
                snd_lastmiss = 0x400;
                i8751_return = 0x400;
                return;
            }

            if (i8751_value == 0x007b) {
                i8751_return = 0x0184; //???
            }
            if (i8751_value == 0x0000) {
                i8751_return = 0x0184;
                coin_lastmiss = snd_lastmiss = 0;
            }//???
            if (i8751_value == 0x0401) {
                i8751_return = 0x0184; //???
            }
            if ((i8751_value >> 8) == 0x01) {
                i8751_return = 0x0184; /* Coinage setup */
            }
            if ((i8751_value >> 8) == 0x02) {
                i8751_return = snd_lastmiss | ((coin_lastmiss / 10) << 4) | (coin_lastmiss % 10);
                snd_lastmiss = 0;
            } /* Coin return */

            if ((i8751_value >> 8) == 0x03) {
                i8751_return = 0;
                coin_lastmiss--;
            } /* Coin clear */

        }
    };
    static int coin_csilver, latch_csilver = 0, snd_csilver;
    public static WriteHandlerPtr csilver_i8751_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            i8751_return = 0;

            switch (offset) {
                case 0: /* High byte */

                    i8751_value = (i8751_value & 0xff) | (data << 8);
                    cpu_cause_interrupt(0, M6809_INT_FIRQ); /* Signal main cpu */

                    break;
                case 1: /* Low byte */

                    i8751_value = (i8751_value & 0xff00) | data;
                    break;
            }

            /* Coins are controlled by the i8751 */
            if ((readinputport(2) & 3) == 3 && latch_csilver == 0) {
                latch_csilver = 1;
            }
            if ((readinputport(2) & 3) != 3 && latch_csilver != 0) {
                coin_csilver++;
                latch_csilver = 0;
                snd_csilver = 0x1200;
                i8751_return = 0x1200;
                return;
            }

            if (i8751_value == 0x054a) {
                i8751_return = ~(0x4a);
                coin_csilver = 0;
                snd_csilver = 0;
            } /* Captain Silver ID */

            if ((i8751_value >> 8) == 0x01) {
                i8751_return = 0; /* Coinage - Not Supported */
            }
            if ((i8751_value >> 8) == 0x02) {
                i8751_return = snd_csilver | coin_csilver;
                snd_csilver = 0;
            } /* Coin Return */

            if (i8751_value == 0x0003 && coin_csilver != 0) {
                i8751_return = 0;
                coin_csilver--;
            } /* Coin Clear */

        }
    };
    static int coin1_garyoret, coin2_garyoret, latch_garyoret;
    public static WriteHandlerPtr garyoret_i8751_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            i8751_return = 0;

            switch (offset) {
                case 0: /* High byte */

                    if (errorlog != null && data != 5) {
                        fprintf(errorlog, "PC %06x - Write %02x to 8751 %d\n", cpu_get_pc(), data, offset);
                    }
                    i8751_value = (i8751_value & 0xff) | (data << 8);
                    break;
                case 1: /* Low byte */

                    i8751_value = (i8751_value & 0xff00) | data;
                    break;
            }

            /* Coins are controlled by the i8751 */
            if ((readinputport(2) & 3) == 3) {
                latch_garyoret = 1;
            }
            if ((readinputport(2) & 1) != 1 && latch_garyoret != 0) {
                coin1_garyoret++;
                latch_garyoret = 0;
            }
            if ((readinputport(2) & 2) != 2 && latch_garyoret != 0) {
                coin2_garyoret++;
                latch_garyoret = 0;
            }

            /* Work out return values */
            if ((i8751_value >> 8) == 0x00) {
                i8751_return = 0;
                coin1_garyoret = coin2_garyoret = 0;
            }
            if ((i8751_value >> 8) == 0x01) {
                i8751_return = 0x59a; /* ID */
            }
            if ((i8751_value >> 8) == 0x04) {
                i8751_return = i8751_value; /* Coinage settings (Not supported) */
            }
            if ((i8751_value >> 8) == 0x05) {
                i8751_return = 0x00 | ((coin1_garyoret / 10) << 4) | (coin1_garyoret % 10);
            } /* Coin 1 */

            if ((i8751_value >> 8) == 0x06 && coin1_garyoret != 0 && offset == 0) {
                i8751_return = 0x600;
                coin1_garyoret--;
            } /* Coin 1 clear */

        }
    };

    /**
     * ***************************************************************************
     */
    public static WriteHandlerPtr dec8_bank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int bankaddress;
            UBytePtr RAM = memory_region(REGION_CPU1);

            bankaddress = 0x10000 + (data & 0x0f) * 0x4000;
            cpu_setbank(1, new UBytePtr(RAM, bankaddress));
        }
    };
    /* Used by Ghostbusters, Meikyuu Hunter G & Gondomania */
    public static WriteHandlerPtr ghostb_bank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int bankaddress;
            UBytePtr RAM = memory_region(REGION_CPU1);

            bankaddress = 0x10000 + (data >> 4) * 0x4000;
            cpu_setbank(1, new UBytePtr(RAM, bankaddress));

            if ((data & 1) != 0) {
                int_enable = 1;
            } else {
                int_enable = 0;
            }
            if ((data & 2) != 0) {
                nmi_enable = 1;
            } else {
                nmi_enable = 0;
            }
        }
    };

    public static WriteHandlerPtr csilver_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int bankaddress;
            UBytePtr RAM = memory_region(REGION_CPU1);

            /* Bottom 4 bits - bank switch */
            bankaddress = 0x10000 + (data & 0x0f) * 0x4000;
            cpu_setbank(1, new UBytePtr(RAM, bankaddress));

            /* There are unknown bits in the top half of the byte! */
            //if (errorlog) fprintf(errorlog,"PC %06x - Write %02x to %04x\n",cpu_get_pc(),data,offset+0x1802);
        }
    };
    public static WriteHandlerPtr dec8_sound_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(0, data);
            cpu_cause_interrupt(1, M6502_INT_NMI);
        }
    };

    public static WriteHandlerPtr oscar_sound_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(0, data);
            cpu_cause_interrupt(2, M6502_INT_NMI);
        }
    };
    static int toggle = 0;
    public static vclk_interruptPtr csilver_adpcm_int = new vclk_interruptPtr() {
        public void handler(int data) {

            toggle ^= 1;
            if (toggle != 0) {
                cpu_cause_interrupt(2, M6502_INT_IRQ);
            }

            MSM5205_data_w.handler(0, msm5205next >> 4);
            msm5205next <<= 4;
        }
    };
    public static ReadHandlerPtr csilver_adpcm_reset_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            MSM5205_reset_w.handler(0, 0);
            return 0;
        }
    };

    public static WriteHandlerPtr csilver_adpcm_data_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            msm5205next = data;
        }
    };

    public static WriteHandlerPtr csilver_sound_bank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UBytePtr RAM = memory_region(REGION_CPU3);

            if ((data & 8) != 0) {
                cpu_setbank(3, new UBytePtr(RAM, 0x14000));
            } else {
                cpu_setbank(3, new UBytePtr(RAM, 0x10000));
            }
        }
    };
    public static WriteHandlerPtr oscar_int_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* Deal with interrupts, coins also generate NMI to CPU 0 */
            switch (offset) {
                case 0: /* IRQ2 */

                    cpu_cause_interrupt(1, M6809_INT_IRQ);
                    return;
                case 1: /* IRC 1 */

                    return;
                case 2: /* IRQ 1 */

                    cpu_cause_interrupt(0, M6809_INT_IRQ);
                    return;
                case 3: /* IRC 2 */

                    return;
            }
        }
    };
    /* Used by Shackled, Last Mission, Captain Silver */
    public static WriteHandlerPtr shackled_int_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            switch (offset) {
                case 0: /* CPU 2 - IRQ acknowledge */

                    return;
                case 1: /* CPU 1 - IRQ acknowledge */

                    return;
                case 2: /* i8751 - FIRQ acknowledge */

                    return;
                case 3: /* IRQ 1 */

                    cpu_cause_interrupt(0, M6809_INT_IRQ);
                    return;
                case 4: /* IRQ 2 */

                    cpu_cause_interrupt(1, M6809_INT_IRQ);
                    return;
            }
        }
    };
    /**
     * ***************************************************************************
     */

    public static ReadHandlerPtr dec8_share_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return dec8_shared_ram.read(offset);
        }
    };
    public static ReadHandlerPtr dec8_share2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return dec8_shared2_ram.read(offset);
        }
    };
    public static WriteHandlerPtr dec8_share_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            dec8_shared_ram.write(offset, data);
        }
    };
    public static WriteHandlerPtr dec8_share2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            dec8_shared2_ram.write(offset, data);
        }
    };
    public static ReadHandlerPtr shackled_sprite_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return spriteram.read(offset);
        }
    };
    public static WriteHandlerPtr shackled_sprite_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            spriteram.write(offset, data);
        }
    };
    public static WriteHandlerPtr shackled_video_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            videoram.write(offset, data);
        }
    };
    public static ReadHandlerPtr shackled_video_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return videoram.read(offset);
        }
    };

    /**
     * ***************************************************************************
     */
    static MemoryReadAddress cobra_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x07ff, MRA_RAM),
                new MemoryReadAddress(0x0800, 0x17ff, dec8_video_r),
                new MemoryReadAddress(0x1800, 0x2fff, MRA_RAM),
                new MemoryReadAddress(0x3000, 0x31ff, paletteram_r),
                new MemoryReadAddress(0x3800, 0x3800, input_port_0_r), /* Player 1 */
                new MemoryReadAddress(0x3801, 0x3801, input_port_1_r), /* Player 2 */
                new MemoryReadAddress(0x3802, 0x3802, input_port_3_r), /* Dip 1 */
                new MemoryReadAddress(0x3803, 0x3803, input_port_4_r), /* Dip 2 */
                new MemoryReadAddress(0x3a00, 0x3a00, input_port_2_r), /* VBL  coins */
                new MemoryReadAddress(0x4000, 0x7fff, MRA_BANK1),
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};
    static MemoryWriteAddress cobra_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x07ff, MWA_RAM),
                new MemoryWriteAddress(0x0800, 0x17ff, dec8_video_w),
                new MemoryWriteAddress(0x1800, 0x1fff, MWA_RAM),
                new MemoryWriteAddress(0x2000, 0x27ff, MWA_RAM, videoram, videoram_size),
                new MemoryWriteAddress(0x2800, 0x2fff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x3000, 0x31ff, paletteram_xxxxBBBBGGGGRRRR_swap_w, paletteram),
                new MemoryWriteAddress(0x3200, 0x37ff, MWA_RAM), /* Unknown, seemingly unused */
                new MemoryWriteAddress(0x3800, 0x381f, dec8_bac06_0_w),
                new MemoryWriteAddress(0x3a00, 0x3a1f, dec8_bac06_1_w),
                new MemoryWriteAddress(0x3c00, 0x3c00, dec8_bank_w),
                new MemoryWriteAddress(0x3c02, 0x3c02, buffer_spriteram_w), /* DMA */
                new MemoryWriteAddress(0x3e00, 0x3e00, dec8_sound_w),
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};
    static MemoryReadAddress ghostb_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_RAM),
                new MemoryReadAddress(0x2000, 0x27ff, dec8_video_r),
                new MemoryReadAddress(0x2800, 0x2dff, MRA_RAM),
                new MemoryReadAddress(0x3000, 0x37ff, MRA_RAM),
                new MemoryReadAddress(0x3800, 0x3800, input_port_0_r), /* Player 1 */
                new MemoryReadAddress(0x3801, 0x3801, input_port_1_r), /* Player 2 */
                new MemoryReadAddress(0x3802, 0x3802, input_port_2_r), /* Player 3 */
                new MemoryReadAddress(0x3803, 0x3803, input_port_3_r), /* Start buttons + VBL */
                new MemoryReadAddress(0x3820, 0x3820, input_port_5_r), /* Dip */
                new MemoryReadAddress(0x3840, 0x3840, i8751_h_r),
                new MemoryReadAddress(0x3860, 0x3860, i8751_l_r),
                new MemoryReadAddress(0x4000, 0x7fff, MRA_BANK1),
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};
    static MemoryWriteAddress ghostb_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0fff, MWA_RAM),
                new MemoryWriteAddress(0x1000, 0x17ff, MWA_RAM),
                new MemoryWriteAddress(0x1800, 0x1fff, MWA_RAM, videoram),
                new MemoryWriteAddress(0x2000, 0x27ff, dec8_video_w),
                new MemoryWriteAddress(0x2800, 0x2bff, MWA_RAM), /* Scratch ram for rowscroll? */
                new MemoryWriteAddress(0x2c00, 0x2dff, MWA_RAM, dec8_row),
                new MemoryWriteAddress(0x2e00, 0x2fff, MWA_RAM), /* Unused */
                new MemoryWriteAddress(0x3000, 0x37ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x3800, 0x3800, dec8_sound_w),
                new MemoryWriteAddress(0x3820, 0x3827, dec8_pf2_w),
                new MemoryWriteAddress(0x3830, 0x3833, dec8_scroll2_w),
                new MemoryWriteAddress(0x3840, 0x3840, ghostb_bank_w),
                new MemoryWriteAddress(0x3860, 0x3861, ghostb_i8751_w),
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress srdarwin_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x13ff, MRA_RAM),
                new MemoryReadAddress(0x1400, 0x17ff, srdarwin_video_r),
                new MemoryReadAddress(0x2000, 0x2000, i8751_h_r),
                new MemoryReadAddress(0x2001, 0x2001, i8751_l_r),
                new MemoryReadAddress(0x3800, 0x3800, input_port_2_r), /* Dip 1 */
                new MemoryReadAddress(0x3801, 0x3801, input_port_0_r), /* Player 1 */
                new MemoryReadAddress(0x3802, 0x3802, input_port_1_r), /* Player 2 (cocktail) + VBL */
                new MemoryReadAddress(0x3803, 0x3803, input_port_3_r), /* Dip 2 */
                new MemoryReadAddress(0x4000, 0x7fff, MRA_BANK1),
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress srdarwin_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x05ff, MWA_RAM),
                new MemoryWriteAddress(0x0600, 0x07ff, MWA_RAM, spriteram),
                new MemoryWriteAddress(0x0800, 0x0fff, MWA_RAM, videoram, spriteram_size),
                new MemoryWriteAddress(0x1000, 0x13ff, MWA_RAM),
                new MemoryWriteAddress(0x1400, 0x17ff, srdarwin_video_w, srdarwin_tileram),
                new MemoryWriteAddress(0x1800, 0x1801, srdarwin_i8751_w),
                new MemoryWriteAddress(0x1802, 0x1802, i8751_reset_w), /* Maybe.. */
                new MemoryWriteAddress(0x1803, 0x1803, MWA_NOP), /* NMI ack */
                new MemoryWriteAddress(0x1804, 0x1804, buffer_spriteram_w), /* DMA */
                new MemoryWriteAddress(0x1805, 0x1806, srdarwin_control_w), /* Scroll  Bank */
                new MemoryWriteAddress(0x2000, 0x2000, dec8_sound_w), /* Sound */
                new MemoryWriteAddress(0x2001, 0x2001, dec8_flipscreen_w), /* Flipscreen */
                new MemoryWriteAddress(0x2800, 0x288f, paletteram_xxxxBBBBGGGGRRRR_split1_w, paletteram),
                new MemoryWriteAddress(0x3000, 0x308f, paletteram_xxxxBBBBGGGGRRRR_split2_w, paletteram_2),
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};
    static MemoryReadAddress gondo_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_RAM),
                new MemoryReadAddress(0x2000, 0x27ff, dec8_video_r),
                new MemoryReadAddress(0x2800, 0x2bff, paletteram_r),
                new MemoryReadAddress(0x2c00, 0x2fff, paletteram_2_r),
                new MemoryReadAddress(0x3000, 0x37ff, MRA_RAM), /* Sprites */
                new MemoryReadAddress(0x3800, 0x3800, input_port_7_r), /* Dip 1 */
                new MemoryReadAddress(0x3801, 0x3801, input_port_8_r), /* Dip 2 */
                new MemoryReadAddress(0x380a, 0x380b, gondo_player_1_r), /* Player 1 rotary */
                new MemoryReadAddress(0x380c, 0x380d, gondo_player_2_r), /* Player 2 rotary */
                new MemoryReadAddress(0x380e, 0x380e, input_port_3_r), /* VBL */
                new MemoryReadAddress(0x380f, 0x380f, input_port_2_r), /* Fire buttons */
                new MemoryReadAddress(0x3838, 0x3838, i8751_h_r),
                new MemoryReadAddress(0x3839, 0x3839, i8751_l_r),
                new MemoryReadAddress(0x4000, 0x7fff, MRA_BANK1),
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};
    static MemoryWriteAddress gondo_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x17ff, MWA_RAM),
                new MemoryWriteAddress(0x1800, 0x1fff, MWA_RAM, videoram, videoram_size),
                new MemoryWriteAddress(0x2000, 0x27ff, dec8_video_w),
                new MemoryWriteAddress(0x2800, 0x2bff, paletteram_xxxxBBBBGGGGRRRR_split1_w, paletteram),
                new MemoryWriteAddress(0x2c00, 0x2fff, paletteram_xxxxBBBBGGGGRRRR_split2_w, paletteram_2),
                new MemoryWriteAddress(0x3000, 0x37ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x3810, 0x3810, dec8_sound_w),
                new MemoryWriteAddress(0x3818, 0x382f, gondo_scroll_w),
                new MemoryWriteAddress(0x3830, 0x3830, ghostb_bank_w), /* Bank + NMI enable */
                new MemoryWriteAddress(0x383a, 0x383b, gondo_i8751_w),
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};
    static MemoryReadAddress oscar_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x0eff, dec8_share_r),
                new MemoryReadAddress(0x0f00, 0x0fff, MRA_RAM),
                new MemoryReadAddress(0x1000, 0x1fff, dec8_share2_r),
                new MemoryReadAddress(0x2000, 0x27ff, MRA_RAM),
                new MemoryReadAddress(0x2800, 0x2fff, dec8_video_r),
                new MemoryReadAddress(0x3000, 0x37ff, MRA_RAM), /* Sprites */
                new MemoryReadAddress(0x3800, 0x3bff, paletteram_r),
                new MemoryReadAddress(0x3c00, 0x3c00, input_port_0_r),
                new MemoryReadAddress(0x3c01, 0x3c01, input_port_1_r),
                new MemoryReadAddress(0x3c02, 0x3c02, input_port_2_r), /* VBL  coins */
                new MemoryReadAddress(0x3c03, 0x3c03, input_port_3_r), /* Dip 1 */
                new MemoryReadAddress(0x3c04, 0x3c04, input_port_4_r),
                new MemoryReadAddress(0x4000, 0x7fff, MRA_BANK1),
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};
    static MemoryWriteAddress oscar_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0eff, dec8_share_w, dec8_shared_ram),
                new MemoryWriteAddress(0x0f00, 0x0fff, MWA_RAM),
                new MemoryWriteAddress(0x1000, 0x1fff, dec8_share2_w, dec8_shared2_ram),
                new MemoryWriteAddress(0x2000, 0x27ff, MWA_RAM, videoram, videoram_size),
                new MemoryWriteAddress(0x2800, 0x2fff, dec8_video_w),
                new MemoryWriteAddress(0x3000, 0x37ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x3800, 0x3bff, paletteram_xxxxBBBBGGGGRRRR_swap_w, paletteram),
                new MemoryWriteAddress(0x3c10, 0x3c13, dec8_scroll2_w),
                new MemoryWriteAddress(0x3c80, 0x3c80, buffer_spriteram_w), /* DMA */
                new MemoryWriteAddress(0x3d00, 0x3d00, dec8_bank_w), /* BNKS */
                new MemoryWriteAddress(0x3d80, 0x3d80, oscar_sound_w), /* SOUN */
                new MemoryWriteAddress(0x3e00, 0x3e00, MWA_NOP), /* COINCL */
                new MemoryWriteAddress(0x3e80, 0x3e83, oscar_int_w),
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};
    static MemoryReadAddress oscar_sub_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x0eff, dec8_share_r),
                new MemoryReadAddress(0x0f00, 0x0fff, MRA_RAM),
                new MemoryReadAddress(0x1000, 0x1fff, dec8_share2_r),
                new MemoryReadAddress(0x4000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress oscar_sub_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0eff, dec8_share_w),
                new MemoryWriteAddress(0x0f00, 0x0fff, MWA_RAM),
                new MemoryWriteAddress(0x1000, 0x1fff, dec8_share2_w),
                new MemoryWriteAddress(0x3e80, 0x3e83, oscar_int_w),
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};
    static MemoryReadAddress lastmiss_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x0fff, dec8_share_r),
                new MemoryReadAddress(0x1000, 0x13ff, paletteram_r),
                new MemoryReadAddress(0x1400, 0x17ff, paletteram_2_r),
                new MemoryReadAddress(0x1800, 0x1800, input_port_0_r),
                new MemoryReadAddress(0x1801, 0x1801, input_port_1_r),
                new MemoryReadAddress(0x1802, 0x1802, input_port_2_r),
                new MemoryReadAddress(0x1803, 0x1803, input_port_3_r), /* Dip 1 */
                new MemoryReadAddress(0x1804, 0x1804, input_port_4_r), /* Dip 2 */
                new MemoryReadAddress(0x1806, 0x1806, i8751_h_r),
                new MemoryReadAddress(0x1807, 0x1807, i8751_l_r),
                new MemoryReadAddress(0x2000, 0x27ff, MRA_RAM),
                new MemoryReadAddress(0x2800, 0x2fff, MRA_RAM),
                new MemoryReadAddress(0x3000, 0x37ff, dec8_share2_r),
                new MemoryReadAddress(0x3800, 0x3fff, dec8_video_r),
                new MemoryReadAddress(0x4000, 0x7fff, MRA_BANK1),
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};
    static MemoryWriteAddress lastmiss_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0fff, dec8_share_w, dec8_shared_ram),
                new MemoryWriteAddress(0x1000, 0x13ff, paletteram_xxxxBBBBGGGGRRRR_split1_w, paletteram),
                new MemoryWriteAddress(0x1400, 0x17ff, paletteram_xxxxBBBBGGGGRRRR_split2_w, paletteram_2),
                new MemoryWriteAddress(0x1800, 0x1804, shackled_int_w),
                new MemoryWriteAddress(0x1805, 0x1805, buffer_spriteram_w), /* DMA */
                new MemoryWriteAddress(0x1807, 0x1807, MWA_NOP), /* Flipscreen */
                new MemoryWriteAddress(0x1809, 0x1809, lastmiss_scrollx_w), /* Scroll LSB */
                new MemoryWriteAddress(0x180b, 0x180b, lastmiss_scrolly_w), /* Scroll LSB */
                new MemoryWriteAddress(0x180c, 0x180c, oscar_sound_w),
                new MemoryWriteAddress(0x180d, 0x180d, lastmiss_control_w), /* Bank switch + Scroll MSB */
                new MemoryWriteAddress(0x180e, 0x180f, lastmiss_i8751_w),
                new MemoryWriteAddress(0x2000, 0x27ff, MWA_RAM, videoram, videoram_size),
                new MemoryWriteAddress(0x2800, 0x2fff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x3000, 0x37ff, dec8_share2_w, dec8_shared2_ram),
                new MemoryWriteAddress(0x3800, 0x3fff, dec8_video_w),
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};
    static MemoryReadAddress lastmiss_sub_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x0fff, dec8_share_r),
                new MemoryReadAddress(0x1000, 0x13ff, paletteram_r),
                new MemoryReadAddress(0x1400, 0x17ff, paletteram_2_r),
                new MemoryReadAddress(0x1800, 0x1800, input_port_0_r),
                new MemoryReadAddress(0x1801, 0x1801, input_port_1_r),
                new MemoryReadAddress(0x1802, 0x1802, input_port_2_r),
                new MemoryReadAddress(0x1803, 0x1803, input_port_3_r), /* Dip 1 */
                new MemoryReadAddress(0x1804, 0x1804, input_port_4_r), /* Dip 2 */
                new MemoryReadAddress(0x3000, 0x37ff, dec8_share2_r),
                new MemoryReadAddress(0x3800, 0x3fff, dec8_video_r),
                new MemoryReadAddress(0x4000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};
    static MemoryWriteAddress lastmiss_sub_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0fff, dec8_share_w),
                new MemoryWriteAddress(0x1000, 0x13ff, paletteram_xxxxBBBBGGGGRRRR_split1_w),
                new MemoryWriteAddress(0x1400, 0x17ff, paletteram_xxxxBBBBGGGGRRRR_split2_w),
                new MemoryWriteAddress(0x1800, 0x1804, shackled_int_w),
                new MemoryWriteAddress(0x1805, 0x1805, buffer_spriteram_w), /* DMA */
                new MemoryWriteAddress(0x1807, 0x1807, MWA_NOP), /* Flipscreen */
                new MemoryWriteAddress(0x180c, 0x180c, oscar_sound_w),
                new MemoryWriteAddress(0x2000, 0x27ff, shackled_video_w),
                new MemoryWriteAddress(0x2800, 0x2fff, shackled_sprite_w),
                new MemoryWriteAddress(0x3000, 0x37ff, dec8_share2_w),
                new MemoryWriteAddress(0x3800, 0x3fff, dec8_video_w),
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};
    static MemoryReadAddress shackled_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x0fff, dec8_share_r),
                new MemoryReadAddress(0x1000, 0x13ff, paletteram_r),
                new MemoryReadAddress(0x1400, 0x17ff, paletteram_2_r),
                new MemoryReadAddress(0x1800, 0x1800, input_port_0_r),
                new MemoryReadAddress(0x1801, 0x1801, input_port_1_r),
                new MemoryReadAddress(0x1802, 0x1802, input_port_2_r),
                new MemoryReadAddress(0x1803, 0x1803, input_port_3_r),
                new MemoryReadAddress(0x1804, 0x1804, input_port_4_r),
                new MemoryReadAddress(0x2000, 0x27ff, shackled_video_r),
                new MemoryReadAddress(0x2800, 0x2fff, shackled_sprite_r),
                new MemoryReadAddress(0x3000, 0x37ff, dec8_share2_r),
                new MemoryReadAddress(0x3800, 0x3fff, dec8_video_r),
                new MemoryReadAddress(0x4000, 0x7fff, MRA_BANK1),
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};
    static MemoryWriteAddress shackled_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0fff, dec8_share_w, dec8_shared_ram),
                new MemoryWriteAddress(0x1000, 0x13ff, paletteram_xxxxBBBBGGGGRRRR_split1_w, paletteram),
                new MemoryWriteAddress(0x1400, 0x17ff, paletteram_xxxxBBBBGGGGRRRR_split2_w, paletteram_2),
                new MemoryWriteAddress(0x1800, 0x1804, shackled_int_w),
                new MemoryWriteAddress(0x1805, 0x1805, buffer_spriteram_w), /* DMA */
                new MemoryWriteAddress(0x1809, 0x1809, lastmiss_scrollx_w), /* Scroll LSB */
                new MemoryWriteAddress(0x180b, 0x180b, lastmiss_scrolly_w), /* Scroll LSB */
                new MemoryWriteAddress(0x180c, 0x180c, oscar_sound_w),
                new MemoryWriteAddress(0x180d, 0x180d, lastmiss_control_w), /* Bank switch + Scroll MSB */
                new MemoryWriteAddress(0x2000, 0x27ff, shackled_video_w),
                new MemoryWriteAddress(0x2800, 0x2fff, shackled_sprite_w),
                new MemoryWriteAddress(0x3000, 0x37ff, dec8_share2_w, dec8_shared2_ram),
                new MemoryWriteAddress(0x3800, 0x3fff, dec8_video_w),
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};
    static MemoryReadAddress shackled_sub_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x0fff, dec8_share_r),
                new MemoryReadAddress(0x1000, 0x13ff, paletteram_r),
                new MemoryReadAddress(0x1400, 0x17ff, paletteram_2_r),
                new MemoryReadAddress(0x1800, 0x1800, input_port_0_r),
                new MemoryReadAddress(0x1801, 0x1801, input_port_1_r),
                new MemoryReadAddress(0x1802, 0x1802, input_port_2_r),
                new MemoryReadAddress(0x1803, 0x1803, input_port_3_r),
                new MemoryReadAddress(0x1804, 0x1804, input_port_4_r),
                new MemoryReadAddress(0x1806, 0x1806, i8751_h_r),
                new MemoryReadAddress(0x1807, 0x1807, i8751_l_r),
                new MemoryReadAddress(0x2000, 0x27ff, MRA_RAM),
                new MemoryReadAddress(0x2800, 0x2fff, MRA_RAM),
                new MemoryReadAddress(0x3000, 0x37ff, dec8_share2_r),
                new MemoryReadAddress(0x3800, 0x3fff, dec8_video_r),
                new MemoryReadAddress(0x4000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};
    static MemoryWriteAddress shackled_sub_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0fff, dec8_share_w),
                new MemoryWriteAddress(0x1000, 0x13ff, paletteram_xxxxBBBBGGGGRRRR_split1_w),
                new MemoryWriteAddress(0x1400, 0x17ff, paletteram_xxxxBBBBGGGGRRRR_split2_w),
                new MemoryWriteAddress(0x1800, 0x1804, shackled_int_w),
                new MemoryWriteAddress(0x1805, 0x1805, buffer_spriteram_w), /* DMA */
                new MemoryWriteAddress(0x1809, 0x1809, lastmiss_scrollx_w), /* Scroll LSB */
                new MemoryWriteAddress(0x180b, 0x180b, lastmiss_scrolly_w), /* Scroll LSB */
                new MemoryWriteAddress(0x180c, 0x180c, oscar_sound_w),
                new MemoryWriteAddress(0x180d, 0x180d, lastmiss_control_w), /* Bank switch + Scroll MSB */
                new MemoryWriteAddress(0x180e, 0x180f, shackled_i8751_w),
                new MemoryWriteAddress(0x2000, 0x27ff, MWA_RAM, videoram, videoram_size),
                new MemoryWriteAddress(0x2800, 0x2fff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x3000, 0x37ff, dec8_share2_w),
                new MemoryWriteAddress(0x3800, 0x3fff, dec8_video_w),
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};
    static MemoryReadAddress csilver_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x0fff, dec8_share_r),
                new MemoryReadAddress(0x1000, 0x13ff, paletteram_r),
                new MemoryReadAddress(0x1400, 0x17ff, paletteram_2_r),
                new MemoryReadAddress(0x1800, 0x1800, input_port_1_r),
                new MemoryReadAddress(0x1801, 0x1801, input_port_0_r),
                new MemoryReadAddress(0x1803, 0x1803, input_port_2_r),
                new MemoryReadAddress(0x1804, 0x1804, input_port_4_r), /* Dip 2 */
                new MemoryReadAddress(0x1805, 0x1805, input_port_3_r), /* Dip 1 */
                new MemoryReadAddress(0x1c00, 0x1c00, i8751_h_r),
                new MemoryReadAddress(0x1e00, 0x1e00, i8751_l_r),
                new MemoryReadAddress(0x2000, 0x27ff, shackled_video_r),
                new MemoryReadAddress(0x2800, 0x2fff, shackled_sprite_r),
                new MemoryReadAddress(0x3000, 0x37ff, dec8_share2_r),
                new MemoryReadAddress(0x3800, 0x3fff, dec8_video_r),
                new MemoryReadAddress(0x4000, 0x7fff, MRA_BANK1),
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};
    static MemoryWriteAddress csilver_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0fff, dec8_share_w, dec8_shared_ram),
                new MemoryWriteAddress(0x1000, 0x13ff, paletteram_xxxxBBBBGGGGRRRR_split1_w, paletteram),
                new MemoryWriteAddress(0x1400, 0x17ff, paletteram_xxxxBBBBGGGGRRRR_split2_w, paletteram_2),
                new MemoryWriteAddress(0x1800, 0x1804, shackled_int_w),
                new MemoryWriteAddress(0x1805, 0x1805, buffer_spriteram_w), /* DMA */
                new MemoryWriteAddress(0x1807, 0x1807, MWA_NOP), /* Flipscreen */
                new MemoryWriteAddress(0x1808, 0x180b, dec8_scroll2_w),
                new MemoryWriteAddress(0x180c, 0x180c, oscar_sound_w),
                new MemoryWriteAddress(0x180d, 0x180d, csilver_control_w),
                new MemoryWriteAddress(0x180e, 0x180f, csilver_i8751_w),
                new MemoryWriteAddress(0x2000, 0x27ff, shackled_video_w),
                new MemoryWriteAddress(0x2800, 0x2fff, shackled_sprite_w),
                new MemoryWriteAddress(0x3000, 0x37ff, dec8_share2_w, dec8_shared2_ram),
                new MemoryWriteAddress(0x3800, 0x3fff, dec8_video_w),
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};
    static MemoryReadAddress csilver_sub_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x0fff, dec8_share_r),
                new MemoryReadAddress(0x1000, 0x13ff, paletteram_r),
                new MemoryReadAddress(0x1400, 0x17ff, paletteram_2_r),
                //	new MemoryReadAddress( 0x1800, 0x1800, input_port_0_r ),
                //	new MemoryReadAddress( 0x1801, 0x1801, input_port_1_r ),
                new MemoryReadAddress(0x1803, 0x1803, input_port_2_r),
                new MemoryReadAddress(0x1804, 0x1804, input_port_4_r),
                new MemoryReadAddress(0x1805, 0x1805, input_port_3_r),
                new MemoryReadAddress(0x2000, 0x27ff, MRA_RAM),
                new MemoryReadAddress(0x2800, 0x2fff, MRA_RAM),
                new MemoryReadAddress(0x3000, 0x37ff, dec8_share2_r),
                new MemoryReadAddress(0x3800, 0x3fff, dec8_video_r),
                new MemoryReadAddress(0x4000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};
    static MemoryWriteAddress csilver_sub_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0fff, dec8_share_w),
                new MemoryWriteAddress(0x1000, 0x13ff, paletteram_xxxxBBBBGGGGRRRR_split1_w),
                new MemoryWriteAddress(0x1400, 0x17ff, paletteram_xxxxBBBBGGGGRRRR_split2_w),
                new MemoryWriteAddress(0x1800, 0x1804, shackled_int_w),
                new MemoryWriteAddress(0x1805, 0x1805, buffer_spriteram_w), /* DMA */
                new MemoryWriteAddress(0x180c, 0x180c, oscar_sound_w),
                new MemoryWriteAddress(0x180d, 0x180d, lastmiss_control_w), /* Bank switch + Scroll MSB */
                new MemoryWriteAddress(0x2000, 0x27ff, MWA_RAM, videoram, videoram_size),
                new MemoryWriteAddress(0x2800, 0x2fff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x3000, 0x37ff, dec8_share2_w),
                new MemoryWriteAddress(0x3800, 0x3fff, dec8_video_w),
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};
    static MemoryReadAddress garyoret_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_RAM),
                new MemoryReadAddress(0x2000, 0x27ff, dec8_video_r),
                new MemoryReadAddress(0x2800, 0x2bff, paletteram_r),
                new MemoryReadAddress(0x2c00, 0x2fff, paletteram_2_r),
                new MemoryReadAddress(0x3000, 0x37ff, MRA_RAM), /* Sprites */
                new MemoryReadAddress(0x3800, 0x3800, input_port_3_r), /* Dip 1 */
                new MemoryReadAddress(0x3801, 0x3801, input_port_4_r), /* Dip 2 */
                new MemoryReadAddress(0x3808, 0x3808, MRA_NOP), /* ? */
                new MemoryReadAddress(0x380a, 0x380a, input_port_1_r), /* Player 2 + VBL */
                new MemoryReadAddress(0x380b, 0x380b, input_port_0_r), /* Player 1 */
                new MemoryReadAddress(0x383a, 0x383a, i8751_h_r),
                new MemoryReadAddress(0x383b, 0x383b, i8751_l_r),
                new MemoryReadAddress(0x4000, 0x7fff, MRA_BANK1),
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};
    static MemoryWriteAddress garyoret_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x17ff, MWA_RAM),
                new MemoryWriteAddress(0x1800, 0x1fff, MWA_RAM, videoram, videoram_size),
                new MemoryWriteAddress(0x2000, 0x27ff, dec8_video_w),
                new MemoryWriteAddress(0x2800, 0x2bff, paletteram_xxxxBBBBGGGGRRRR_split1_w, paletteram),
                new MemoryWriteAddress(0x2c00, 0x2fff, paletteram_xxxxBBBBGGGGRRRR_split2_w, paletteram_2),
                new MemoryWriteAddress(0x3000, 0x37ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x3810, 0x3810, dec8_sound_w),
                new MemoryWriteAddress(0x3818, 0x382f, gondo_scroll_w),
                new MemoryWriteAddress(0x3830, 0x3830, ghostb_bank_w), /* Bank + NMI enable */
                new MemoryWriteAddress(0x3838, 0x3839, garyoret_i8751_w),
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};
    /**
     * ***************************************************************************
     */

    /* Used for Cobra Command, Maze Hunter, Super Real Darwin, Gondomania, etc */
    static MemoryReadAddress dec8_s_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x05ff, MRA_RAM),
                new MemoryReadAddress(0x6000, 0x6000, soundlatch_r),
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress dec8_s_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x05ff, MWA_RAM),
                new MemoryWriteAddress(0x2000, 0x2000, YM2203_control_port_0_w), /* OPN */
                new MemoryWriteAddress(0x2001, 0x2001, YM2203_write_port_0_w),
                new MemoryWriteAddress(0x4000, 0x4000, YM3812_control_port_0_w), /* OPL */
                new MemoryWriteAddress(0x4001, 0x4001, YM3812_write_port_0_w),
                new MemoryWriteAddress(0x8000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    /* Used by Last Mission, Shackled & Breywood */
    static MemoryReadAddress ym3526_s_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x05ff, MRA_RAM),
                new MemoryReadAddress(0x3000, 0x3000, soundlatch_r),
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress ym3526_s_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x05ff, MWA_RAM),
                new MemoryWriteAddress(0x0800, 0x0800, YM2203_control_port_0_w), /* OPN */
                new MemoryWriteAddress(0x0801, 0x0801, YM2203_write_port_0_w),
                new MemoryWriteAddress(0x1000, 0x1000, YM3526_control_port_0_w), /* ? */
                new MemoryWriteAddress(0x1001, 0x1001, YM3526_write_port_0_w),
                new MemoryWriteAddress(0x8000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    /* Captain Silver - same sound system as Pocket Gal */
    static MemoryReadAddress csilver_s_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x07ff, MRA_RAM),
                new MemoryReadAddress(0x3000, 0x3000, soundlatch_r),
                new MemoryReadAddress(0x3400, 0x3400, csilver_adpcm_reset_r), /* ? not sure */
                new MemoryReadAddress(0x4000, 0x7fff, MRA_BANK3),
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress csilver_s_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x07ff, MWA_RAM),
                new MemoryWriteAddress(0x0800, 0x0800, YM2203_control_port_0_w),
                new MemoryWriteAddress(0x0801, 0x0801, YM2203_write_port_0_w),
                new MemoryWriteAddress(0x1000, 0x1000, YM3812_control_port_0_w),
                new MemoryWriteAddress(0x1001, 0x1001, YM3812_write_port_0_w),
                new MemoryWriteAddress(0x1800, 0x1800, csilver_adpcm_data_w), /* ADPCM data for the MSM5205 chip */
                new MemoryWriteAddress(0x2000, 0x2000, csilver_sound_bank_w),
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};
    /**
     * ***************************************************************************
     */
    static InputPortPtr input_ports_cobracom = new InputPortPtr() {
        public void handler() {
            PORT_START();  /* Player 1 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* Player 2 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START(); 	/* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_VBLANK);

            PORT_START(); 	/* Dip switch bank 1 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x80, DEF_STR("Cocktail"));

            PORT_START(); 	/* Dip switch bank 2 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x02, "4");
            PORT_DIPSETTING(0x01, "5");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x04, "Easy");
            PORT_DIPSETTING(0x0c, "Normal");
            PORT_DIPSETTING(0x08, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x10, 0x10, "Allow Continue");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x10, DEF_STR("Yes"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_ghostb = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* Player 1 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* Player 2 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* Player 3 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START3);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_VBLANK);
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START(); 	/* Dummy input for i8751 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN4);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START(); 	/* Dip switch */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "1");
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x02, "5");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x04, "Easy");
            PORT_DIPSETTING(0x0c, "Normal");
            PORT_DIPSETTING(0x08, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x30, 0x30, "Scene Time");
            PORT_DIPSETTING(0x00, "4.00");
            PORT_DIPSETTING(0x10, "4.30");
            PORT_DIPSETTING(0x30, "5.00");
            PORT_DIPSETTING(0x20, "6.00");
            PORT_DIPNAME(0x40, 0x00, "Allow Continue");
            PORT_DIPSETTING(0x40, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));
            PORT_DIPNAME(0x80, 0x80, "Beam Energy Pickup");/* Ghostb only */

            PORT_DIPSETTING(0x00, "Up 1.5%");
            PORT_DIPSETTING(0x80, "Normal");
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_meikyuh = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* Player 1 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* Player 2 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* Player 3 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START3);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_VBLANK);
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START(); 	/* Dummy input for i8751 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN4);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START(); 	/* Dip switch */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "1");
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x02, "5");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x04, "Easy");
            PORT_DIPSETTING(0x0c, "Normal");
            PORT_DIPSETTING(0x08, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, "Allow Continue");
            PORT_DIPSETTING(0x40, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));
            PORT_DIPNAME(0x80, 0x80, "Freeze");
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_srdarwin = new InputPortPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_VBLANK);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* The bottom bits of this dip (coinage) are for the i8751 */
            PORT_BIT(0x0f, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x80, DEF_STR("Cocktail"));

            PORT_START();
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "1");
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x02, "5");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "28", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x04, "Easy");
            PORT_DIPSETTING(0x0c, "Normal");
            PORT_DIPSETTING(0x08, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, "Continues");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);/* Fake */

            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_gondo = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* Player 1 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            /* Top 4 bits are rotary controller */

            PORT_START(); 	/* Player 2 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            /* Top 4 bits are rotary controller */

            PORT_START(); 	/* Player 1 & 2 fire buttons */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_VBLANK);

            PORT_START();  /* Fake port for the i8751 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);

            PORT_START(); 	/* player 1 12-way rotary control */

            PORT_ANALOGX(0xff, 0x00, IPT_DIAL | IPF_REVERSE, 25, 10, 0, 0, KEYCODE_Z, KEYCODE_X, 0, 0);

            PORT_START(); 	/* player 2 12-way rotary control */

            PORT_ANALOGX(0xff, 0x00, IPT_DIAL | IPF_REVERSE | IPF_PLAYER2, 25, 10, 0, 0, KEYCODE_N, KEYCODE_M, 0, 0);

            PORT_START(); 	/* Dip switch bank 1 */
            /* Coinage not currently supported */

            PORT_DIPNAME(0x10, 0x10, "Unknown");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, "Unknown");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START(); 	/* Dip switch bank 2 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "1");
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x02, "5");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x04, "Easy");
            PORT_DIPSETTING(0x0c, "Normal");
            PORT_DIPSETTING(0x08, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x10, 0x10, "Allow Continue");
            PORT_DIPSETTING(0x10, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_oscar = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* Player 1 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* Player 2 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START(); 	/* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_VBLANK);

            PORT_START(); 	/* Dip switch bank 1 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_3C"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_3C"));
            PORT_DIPNAME(0x20, 0x20, "Demo Freeze Mode");
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x80, DEF_STR("Cocktail"));

            PORT_START(); 	/* Dip switch bank 2 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "1");
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x02, "5");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x04, "Easy");
            PORT_DIPSETTING(0x0c, "Normal");
            PORT_DIPSETTING(0x08, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x30, "Every 40000");
            PORT_DIPSETTING(0x20, "Every 60000");
            PORT_DIPSETTING(0x10, "Every 90000");
            PORT_DIPSETTING(0x00, "50000 only");
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, "Allow Continue");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x80, DEF_STR("Yes"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_lastmiss = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* Player 1 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* Player 2 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_VBLANK);

            PORT_START(); 	/* Dip switch bank 1 */
            /* Coinage options not supported (controlled by the i8751) */

            PORT_DIPNAME(0x10, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_BITX(0x40, 0x40, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, "Cabinet?");
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x80, DEF_STR("Cocktail"));

            PORT_START(); 	/* Dip switch bank 2 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x02, "4");
            PORT_DIPSETTING(0x01, "5");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x04, "Easy");
            PORT_DIPSETTING(0x0c, "Normal");
            PORT_DIPSETTING(0x08, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x10, 0x10, "Allow Continue?");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x10, DEF_STR("Yes"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_shackled = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* Player 1 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* Player 2 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_VBLANK);

            PORT_START(); 	/* Dip switch bank 1 */
            /* Coinage not supported */

            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));	/* game doesn't boot when this is On */

            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, "Freeze");
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START(); 	/* Dip switch bank 2 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x02, "4");
            PORT_DIPSETTING(0x01, "5");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x04, "Easy");
            PORT_DIPSETTING(0x0c, "Normal");
            PORT_DIPSETTING(0x08, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x10, 0x10, "Allow Continue");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x10, DEF_STR("Yes"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_csilver = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* Player 1 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* Player 2 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_VBLANK);

            PORT_START(); 	/* Dip switch bank 1 */
            /* Coinage not supported */

            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x80, DEF_STR("Cocktail"));

            PORT_START(); 	/* Dip switch bank 2 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "1");
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x02, "5");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x04, "Easy");
            PORT_DIPSETTING(0x0c, "Normal");
            PORT_DIPSETTING(0x08, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x10, 0x10, "Allow Continue");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x10, DEF_STR("Yes"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_garyoret = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* Player 1 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START(); 	/* Player 2 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_VBLANK);

            PORT_START();  /* Fake port for i8751 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);

            PORT_START(); 	/* Dip switch bank 1 */
            /* Coinage not supported */

            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unused"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unused"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START(); 	/* Dip switch bank 2 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unused"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x04, "Easy");
            PORT_DIPSETTING(0x0c, "Normal");
            PORT_DIPSETTING(0x08, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unused"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unused"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unused"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unused"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    /**
     * ***************************************************************************
     */
    static GfxLayout charlayout_32k = new GfxLayout(
            8, 8,
            1024,
            2,
            new int[]{0x4000 * 8, 0x0000 * 8},
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every sprite takes 8 consecutive bytes */
    );

    static GfxLayout chars_3bpp = new GfxLayout(
            8, 8,
            1024,
            3,
            new int[]{0x6000 * 8, 0x4000 * 8, 0x2000 * 8},
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every sprite takes 8 consecutive bytes */
    );

    /* SRDarwin characters - very unusual layout for Data East */
    static GfxLayout charlayout_16k = new GfxLayout(
            8, 8, /* 8*8 characters */
            1024,
            2, /* 2 bits per pixel */
            new int[]{0, 4}, /* the two bitplanes for 4 pixels are packed into one byte */
            new int[]{0x2000 * 8 + 0, 0x2000 * 8 + 1, 0x2000 * 8 + 2, 0x2000 * 8 + 3, 0, 1, 2, 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );

    static GfxLayout oscar_charlayout = new GfxLayout(
            8, 8,
            1024,
            3,
            new int[]{0x3000 * 8, 0x2000 * 8, 0x1000 * 8},
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every sprite takes 8 consecutive bytes */
    );

    /* Darwin sprites - only 3bpp */
    static GfxLayout sr_sprites = new GfxLayout(
            16, 16,
            2048,
            3,
            new int[]{0x10000 * 8, 0x20000 * 8, 0x00000 * 8},
            new int[]{16 * 8, 1 + (16 * 8), 2 + (16 * 8), 3 + (16 * 8), 4 + (16 * 8), 5 + (16 * 8), 6 + (16 * 8), 7 + (16 * 8),
                0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8, 8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            16 * 16
    );

    static GfxLayout srdarwin_tiles = new GfxLayout(
            16, 16,
            256,
            4,
            new int[]{0x8000 * 8, 0x8000 * 8 + 4, 0, 4},
            new int[]{0, 1, 2, 3, 1024 * 8 * 8 + 0, 1024 * 8 * 8 + 1, 1024 * 8 * 8 + 2, 1024 * 8 * 8 + 3,
                16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3, 16 * 8 + 1024 * 8 * 8 + 0, 16 * 8 + 1024 * 8 * 8 + 1, 16 * 8 + 1024 * 8 * 8 + 2, 16 * 8 + 1024 * 8 * 8 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            32 * 8 /* every tile takes 32 consecutive bytes */
    );

    static GfxLayout tiles = new GfxLayout(
            16, 16,
            4096,
            4,
            new int[]{0x60000 * 8, 0x40000 * 8, 0x20000 * 8, 0x00000 * 8},
            new int[]{16 * 8, 1 + (16 * 8), 2 + (16 * 8), 3 + (16 * 8), 4 + (16 * 8), 5 + (16 * 8), 6 + (16 * 8), 7 + (16 * 8),
                0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8, 8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            16 * 16
    );

    /* X flipped on Ghostbusters tiles */
    static GfxLayout tiles_r = new GfxLayout(
            16, 16,
            2048,
            4,
            new int[]{0x20000 * 8, 0x00000 * 8, 0x30000 * 8, 0x10000 * 8},
            new int[]{7, 6, 5, 4, 3, 2, 1, 0,
                7 + (16 * 8), 6 + (16 * 8), 5 + (16 * 8), 4 + (16 * 8), 3 + (16 * 8), 2 + (16 * 8), 1 + (16 * 8), 0 + (16 * 8)},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8, 8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            16 * 16
    );

    static GfxDecodeInfo cobracom_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout_32k, 0, 8),
                new GfxDecodeInfo(REGION_GFX2, 0, tiles, 64, 4),
                new GfxDecodeInfo(REGION_GFX3, 0, tiles, 192, 4),
                new GfxDecodeInfo(REGION_GFX4, 0, tiles, 128, 4),
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo ghostb_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, chars_3bpp, 0, 4),
                new GfxDecodeInfo(REGION_GFX2, 0, tiles, 256, 16),
                new GfxDecodeInfo(REGION_GFX3, 0, tiles_r, 512, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo srdarwin_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x00000, charlayout_16k, 128, 4), /* Only 1 used so far :/ */
                new GfxDecodeInfo(REGION_GFX2, 0x00000, sr_sprites, 64, 8),
                new GfxDecodeInfo(REGION_GFX3, 0x00000, srdarwin_tiles, 0, 8),
                new GfxDecodeInfo(REGION_GFX3, 0x10000, srdarwin_tiles, 0, 8),
                new GfxDecodeInfo(REGION_GFX3, 0x20000, srdarwin_tiles, 0, 8),
                new GfxDecodeInfo(REGION_GFX3, 0x30000, srdarwin_tiles, 0, 8),
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo gondo_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, chars_3bpp, 0, 16), /* Chars */
                new GfxDecodeInfo(REGION_GFX2, 0, tiles, 256, 32), /* Sprites */
                new GfxDecodeInfo(REGION_GFX3, 0, tiles, 768, 16), /* Tiles */
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo oscar_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, oscar_charlayout, 256, 8), /* Chars */
                new GfxDecodeInfo(REGION_GFX2, 0, tiles, 0, 16), /* Sprites */
                new GfxDecodeInfo(REGION_GFX3, 0, tiles, 384, 8), /* Tiles */
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo shackled_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, chars_3bpp, 0, 4),
                new GfxDecodeInfo(REGION_GFX2, 0, tiles, 256, 16),
                new GfxDecodeInfo(REGION_GFX3, 0, tiles, 768, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    /**
     * ***************************************************************************
     */
    static YM2203interface ym2203_interface = new YM2203interface(
            1, /* 1 chip */
            1500000, /* Should be accurate for all games, derived from 12MHz crystal */
            new int[]{YM2203_VOL(20, 23)},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );
	/* handler called by the 3812 emulator when the internal timers cause an IRQ */
	public static WriteYmHandlerPtr irqhandler = new WriteYmHandlerPtr() { public void handler(int linestate)
	{
		cpu_set_irq_line(1,0,linestate); /* M6502_INT_IRQ */
	} };
	
	public static WriteYmHandlerPtr oscar_irqhandler = new WriteYmHandlerPtr() { public void handler(int linestate)
	{
		cpu_set_irq_line(2,0,linestate); /* M6502_INT_IRQ */
	} };
	
	static YM3526interface ym3526_interface = new YM3526interface
	(
		1,			/* 1 chip */
		3000000,	/* 3 MHz */
		new int[] { 35 },
		new WriteYmHandlerPtr[] { irqhandler }
	);
	
	static YM3526interface oscar_ym3526_interface = new YM3526interface
	(
		1,			/* 1 chip */
		3000000,	/* 3 MHz */
		new int[] { 35 },
		new WriteYmHandlerPtr[] { oscar_irqhandler }
	);
	
	static YM3812interface ym3812_interface = new YM3812interface
	(
		1,			/* 1 chip */
		3000000,	/* 3 MHz */
		new int[] { 35 },
		new WriteYmHandlerPtr[] { irqhandler }
	);
	
	static MSM5205interface msm5205_interface = new MSM5205interface
	(
		1,					/* 1 chip             */
		384000,				/* 384KHz             */
		new vclk_interruptPtr[] { csilver_adpcm_int },/* interrupt function */
		new int[] { MSM5205_S48_4B },	/* 8KHz               */
		new int[] { 88 }
	);
    /******************************************************************************/

    static int[] latch_ghostb = new int[4];
    public static InterruptPtr ghostb_interrupt = new InterruptPtr() {
        public int handler() {

            int i8751_out = readinputport(4);

            /* Ghostbusters coins are controlled by the i8751 */
            if ((i8751_out & 0x8) == 0x8) {
                latch_ghostb[0] = 1;
            }
            if ((i8751_out & 0x4) == 0x4) {
                latch_ghostb[1] = 1;
            }
            if ((i8751_out & 0x2) == 0x2) {
                latch_ghostb[2] = 1;
            }
            if ((i8751_out & 0x1) == 0x1) {
                latch_ghostb[3] = 1;
            }

            if (((i8751_out & 0x8) != 0x8) && latch_ghostb[0] != 0) {
                latch_ghostb[0] = 0;
                cpu_cause_interrupt(0, M6809_INT_IRQ);
                i8751_return = 0x8001;
            } /* Player 1 coin */

            if (((i8751_out & 0x4) != 0x4) && latch_ghostb[1] != 0) {
                latch_ghostb[1] = 0;
                cpu_cause_interrupt(0, M6809_INT_IRQ);
                i8751_return = 0x4001;
            } /* Player 2 coin */

            if (((i8751_out & 0x2) != 0x2) && latch_ghostb[2] != 0) {
                latch_ghostb[2] = 0;
                cpu_cause_interrupt(0, M6809_INT_IRQ);
                i8751_return = 0x2001;
            } /* Player 3 coin */

            if (((i8751_out & 0x1) != 0x1) && latch_ghostb[3] != 0) {
                latch_ghostb[3] = 0;
                cpu_cause_interrupt(0, M6809_INT_IRQ);
                i8751_return = 0x1001;
            } /* Service */

            if (nmi_enable != 0) {
                return M6809_INT_NMI; /* VBL */
            }

            return 0; /* VBL */

        }
    };

    public static InterruptPtr gondo_interrupt = new InterruptPtr() {
        public int handler() {
            if (nmi_enable != 0) {
                return M6809_INT_NMI; /* VBL */
            }

            return 0; /* VBL */

        }
    };

    /* Coins generate NMI's */
    static int latch_oscar = 1;
    public static InterruptPtr oscar_interrupt = new InterruptPtr() {
        public int handler() {

            if ((readinputport(2) & 0x7) == 0x7) {
                latch_oscar = 1;
            }
            if (latch_oscar != 0 && (readinputport(2) & 0x7) != 0x7) {
                latch_oscar = 0;
                cpu_cause_interrupt(0, M6809_INT_NMI);
            }

            return 0;
        }
    };
    static MachineDriver machine_driver_cobracom = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809,
                        2000000,
                        cobra_readmem, cobra_writemem, null, null,
                        nmi_interrupt, 1
                ),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				1500000,
				dec8_s_readmem,dec8_s_writemem,null,null,
				ignore_interrupt,0	/* IRQs are caused by the YM3812 */
									/* NMIs are caused by the main CPU */
			)
            },
            58, 529, /* 58Hz, 529ms Vblank duration */
            1,
            null, /* init machine */
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 1 * 8, 31 * 8 - 1),
            cobracom_gfxdecodeinfo,
            256, 256,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK | VIDEO_BUFFERS_SPRITERAM,
            null,
            dec8_vh_start,
            dec8_vh_stop,
            dec8_vh_screenrefresh,
            0, 0, 0, 0,
            new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			),
			new MachineSound(
				SOUND_YM3812,
				ym3812_interface
			)
		}
    );

    static MachineDriver machine_driver_ghostb = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_HD6309,
                        3000000,
                        ghostb_readmem, ghostb_writemem, null, null,
                        ghostb_interrupt, 1
                ),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				1500000,
				dec8_s_readmem,dec8_s_writemem,null,null,
				ignore_interrupt,0	/* IRQs are caused by the YM3812 */
									/* NMIs are caused by the main CPU */
			)
            },
            58, 529, /* 58Hz, 529ms Vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null, /* init machine */
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 1 * 8, 31 * 8 - 1),
            ghostb_gfxdecodeinfo,
            1024, 1024,
            ghostb_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_UPDATE_BEFORE_VBLANK | VIDEO_BUFFERS_SPRITERAM,
            dec8_eof_callback,
            dec8_vh_start,
            dec8_vh_stop,
            ghostb_vh_screenrefresh,
            0, 0, 0, 0,
            new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			),
			new MachineSound(
				SOUND_YM3812,
				ym3812_interface
			)
		}
    );
    static MachineDriver machine_driver_srdarwin = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809, /* MC68A09EP */
                        2000000,
                        srdarwin_readmem, srdarwin_writemem, null, null,
                        nmi_interrupt, 1
                ),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				1500000,
				dec8_s_readmem,dec8_s_writemem,null,null,
				ignore_interrupt,0	/* IRQs are caused by the YM3812 */
									/* NMIs are caused by the main CPU */
			)
            },
            58, 529, /* 58Hz, 529ms Vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null, /* init machine */
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 1 * 8, 31 * 8 - 1),
            srdarwin_gfxdecodeinfo,
            144, 144,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK | VIDEO_BUFFERS_SPRITERAM,
            null,
            srdarwin_vh_start,
            null,
            srdarwin_vh_screenrefresh,
            0, 0, 0, 0,
            new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			),
			new MachineSound(
				SOUND_YM3812,
				ym3812_interface
			)
		}
    );
    static MachineDriver machine_driver_gondo = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_HD6309, /* HD63C09EP */
                        3000000,
                        gondo_readmem, gondo_writemem, null, null,
                        gondo_interrupt, 1
                ),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				1500000,
				dec8_s_readmem,dec8_s_writemem,null,null,
				ignore_interrupt,0	/* IRQs are caused by the YM3526 */
									/* NMIs are caused by the main CPU */
			)
            },
            58, 529, /* 58Hz, 529ms Vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null, /* init machine */
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 1 * 8, 31 * 8 - 1),
            gondo_gfxdecodeinfo,
            1024, 1024,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK | VIDEO_BUFFERS_SPRITERAM,
            dec8_eof_callback,
            dec8_vh_start,
            dec8_vh_stop,
            gondo_vh_screenrefresh,
            0, 0, 0, 0,
            new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			),
			new MachineSound(
				SOUND_YM3526,
				ym3526_interface
			)
		}
    );
    static MachineDriver machine_driver_oscar = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_HD6309,
                        2000000,
                        oscar_readmem, oscar_writemem, null, null,
                        oscar_interrupt, 1
                ),
                new MachineCPU(
                        CPU_HD6309,
                        2000000,
                        oscar_sub_readmem, oscar_sub_writemem, null, null,
                        ignore_interrupt, 0
                ),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				1500000,
				dec8_s_readmem,dec8_s_writemem,null,null,
				ignore_interrupt,0	/* IRQs are caused by the YM3526 */
									/* NMIs are caused by the main CPU */
			)
            },
            58, 529, /* 58Hz, 529ms Vblank duration */
            40, /* 40 CPU slices per frame */
            null, /* init machine */
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 1 * 8, 31 * 8 - 1),
            oscar_gfxdecodeinfo,
            512, 512,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK | VIDEO_BUFFERS_SPRITERAM,
            null,
            dec8_vh_start,
            dec8_vh_stop,
            oscar_vh_screenrefresh,
            0, 0, 0, 0,
            new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			),
			new MachineSound(
				SOUND_YM3526,
				oscar_ym3526_interface
			)
		}
    );
    static MachineDriver machine_driver_lastmiss = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809,
                        2000000,
                        lastmiss_readmem, lastmiss_writemem, null, null,
                        ignore_interrupt, 0
                ),
                new MachineCPU(
                        CPU_M6809,
                        2000000,
                        lastmiss_sub_readmem, lastmiss_sub_writemem, null, null,
                        ignore_interrupt, 0
                ),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				1500000,
				ym3526_s_readmem,ym3526_s_writemem,null,null,
				ignore_interrupt,0	/* IRQs are caused by the YM3526 */
									/* NMIs are caused by the main CPU */
			)
            },
            58, 529, /* 58Hz, 529ms Vblank duration */
            200,
            null, /* init machine */
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 1 * 8, 31 * 8 - 1),
            shackled_gfxdecodeinfo,
            1024, 1024,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK | VIDEO_BUFFERS_SPRITERAM,
            null,
            dec8_vh_start,
            dec8_vh_stop,
            lastmiss_vh_screenrefresh,
            0, 0, 0, 0,
            new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			),
			new MachineSound(
				SOUND_YM3526,
				oscar_ym3526_interface
			)
		}
    );
    static MachineDriver machine_driver_shackled = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809,
                        2000000,
                        shackled_readmem, shackled_writemem, null, null,
                        ignore_interrupt, 0
                ),
                new MachineCPU(
                        CPU_M6809,
                        2000000,
                        shackled_sub_readmem, shackled_sub_writemem, null, null,
                        ignore_interrupt, 0
                ),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				1500000,
				ym3526_s_readmem,ym3526_s_writemem,null,null,
				ignore_interrupt,0	/* IRQs are caused by the YM3526 */
									/* NMIs are caused by the main CPU */
			)
            },
            58, 529, /* 58Hz, 529ms Vblank duration */
            80,
            null, /* init machine */
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 1 * 8, 31 * 8 - 1),
            shackled_gfxdecodeinfo,
            1024, 1024,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK | VIDEO_BUFFERS_SPRITERAM,
            null,
            dec8_vh_start,
            dec8_vh_stop,
            lastmiss_vh_screenrefresh,
            0, 0, 0, 0,
            new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			),
			new MachineSound(
				SOUND_YM3526,
				oscar_ym3526_interface
			)
		}
    );
    static MachineDriver machine_driver_csilver = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809,
                        2000000,
                        csilver_readmem, csilver_writemem, null, null,
                        ignore_interrupt, 0
                ),
                new MachineCPU(
                        CPU_M6809,
                        2000000,
                        csilver_sub_readmem, csilver_sub_writemem, null, null,
                        nmi_interrupt, 1
                ),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				1500000,
				csilver_s_readmem,csilver_s_writemem,null,null,
				ignore_interrupt,0	/* IRQs are caused by the MSM5205 */
									/* NMIs are caused by the main CPU */
			)
            },
            58, 529, /* 58Hz, 529ms Vblank duration */
            60,
            null, /* init machine */
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 1 * 8, 31 * 8 - 1),
            shackled_gfxdecodeinfo,
            1024, 1024,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_AFTER_VBLANK | VIDEO_BUFFERS_SPRITERAM,
            null,
            dec8_vh_start,
            dec8_vh_stop,
            lastmiss_vh_screenrefresh,
            0, 0, 0, 0,
            new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			),
			new MachineSound(
				SOUND_YM3526,
				oscar_ym3526_interface
			),
			new MachineSound(
				SOUND_MSM5205,
				msm5205_interface
		    )
		}
    );
    static MachineDriver machine_driver_garyoret = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_HD6309, /* HD63C09EP */
                        3000000,
                        garyoret_readmem, garyoret_writemem, null, null,
                        gondo_interrupt, 1
                ),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				1500000,
				dec8_s_readmem,dec8_s_writemem,null,null,
				ignore_interrupt,0	/* IRQs are caused by the YM3526 */
									/* NMIs are caused by the main CPU */
			)
            },
            58, 529, /* 58Hz, 529ms Vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null, /* init machine */
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 1 * 8, 31 * 8 - 1),
            gondo_gfxdecodeinfo,
            1024, 1024,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK | VIDEO_BUFFERS_SPRITERAM,
            dec8_eof_callback,
            dec8_vh_start,
            dec8_vh_stop,
            garyoret_vh_screenrefresh,
            0, 0, 0, 0,
            new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			),
			new MachineSound(
				SOUND_YM3526,
				ym3526_interface
			)
		}
    );
    /**
     * ***************************************************************************
     */

    static RomLoadPtr rom_cobracom = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x30000, REGION_CPU1);
            ROM_LOAD("el11-5.bin", 0x08000, 0x08000, 0xaf0a8b05);
            ROM_LOAD("el12-4.bin", 0x10000, 0x10000, 0x7a44ef38);
            ROM_LOAD("el13.bin", 0x20000, 0x10000, 0x04505acb);

            ROM_REGION(0x10000, REGION_CPU2);/* 64K for sound CPU */

            ROM_LOAD("el10-4.bin", 0x8000, 0x8000, 0xedfad118);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("el14.bin", 0x00000, 0x08000, 0x47246177);

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("el00-4.bin", 0x00000, 0x10000, 0x122da2a8);
            ROM_LOAD("el01-4.bin", 0x20000, 0x10000, 0x27bf705b);
            ROM_LOAD("el02-4.bin", 0x40000, 0x10000, 0xc86fede6);
            ROM_LOAD("el03-4.bin", 0x60000, 0x10000, 0x1d8a855b);

            ROM_REGION(0x80000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* tiles 1 */

            ROM_LOAD("el05.bin", 0x00000, 0x10000, 0x1c4f6033);
            ROM_LOAD("el06.bin", 0x20000, 0x10000, 0xd24ba794);
            ROM_LOAD("el04.bin", 0x40000, 0x10000, 0xd80a49ce);
            ROM_LOAD("el07.bin", 0x60000, 0x10000, 0x6d771fc3);

            ROM_REGION(0x80000, REGION_GFX4 | REGIONFLAG_DISPOSE);/* tiles 2 */

            ROM_LOAD("el08.bin", 0x00000, 0x08000, 0xcb0dcf4c);
            ROM_CONTINUE(0x40000, 0x08000);
            ROM_LOAD("el09.bin", 0x20000, 0x08000, 0x1fae5be7);
            ROM_CONTINUE(0x60000, 0x08000);
            ROM_END();
        }
    };

    static RomLoadPtr rom_cobracmj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x30000, REGION_CPU1);
            ROM_LOAD("eh-11.rom", 0x08000, 0x08000, 0x868637e1);
            ROM_LOAD("eh-12.rom", 0x10000, 0x10000, 0x7c878a83);
            ROM_LOAD("el13.bin", 0x20000, 0x10000, 0x04505acb);

            ROM_REGION(0x10000, REGION_CPU2);/* 64K for sound CPU */

            ROM_LOAD("eh-10.rom", 0x8000, 0x8000, 0x62ca5e89);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("el14.bin", 0x00000, 0x08000, 0x47246177);

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("eh-00.rom", 0x00000, 0x10000, 0xd96b6797);
            ROM_LOAD("eh-01.rom", 0x20000, 0x10000, 0x3fef9c02);
            ROM_LOAD("eh-02.rom", 0x40000, 0x10000, 0xbfae6c34);
            ROM_LOAD("eh-03.rom", 0x60000, 0x10000, 0xd56790f8);

            ROM_REGION(0x80000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* tiles 1 */

            ROM_LOAD("el05.bin", 0x00000, 0x10000, 0x1c4f6033);
            ROM_LOAD("el06.bin", 0x20000, 0x10000, 0xd24ba794);
            ROM_LOAD("el04.bin", 0x40000, 0x10000, 0xd80a49ce);
            ROM_LOAD("el07.bin", 0x60000, 0x10000, 0x6d771fc3);

            ROM_REGION(0x80000, REGION_GFX4 | REGIONFLAG_DISPOSE);/* tiles 2 */

            ROM_LOAD("el08.bin", 0x00000, 0x08000, 0xcb0dcf4c);
            ROM_CONTINUE(0x40000, 0x08000);
            ROM_LOAD("el09.bin", 0x20000, 0x08000, 0x1fae5be7);
            ROM_CONTINUE(0x60000, 0x08000);
            ROM_END();
        }
    };

    static RomLoadPtr rom_ghostb = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x50000, REGION_CPU1);
            ROM_LOAD("dz-01.rom", 0x08000, 0x08000, 0x7c5bb4b1);
            ROM_LOAD("dz-02.rom", 0x10000, 0x10000, 0x8e117541);
            ROM_LOAD("dz-03.rom", 0x20000, 0x10000, 0x5606a8f4);
            ROM_LOAD("dz-04.rom", 0x30000, 0x10000, 0xd09bad99);
            ROM_LOAD("dz-05.rom", 0x40000, 0x10000, 0x0315f691);

            ROM_REGION(2 * 0x10000, REGION_CPU2);/* 64K for sound CPU + 64k for decrypted opcodes */

            ROM_LOAD("dz-06.rom", 0x8000, 0x8000, 0x798f56df);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("dz-00.rom", 0x00000, 0x08000, 0x992b4f31);

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("dz-15.rom", 0x00000, 0x10000, 0xa01a5fd9);
            ROM_LOAD("dz-16.rom", 0x10000, 0x10000, 0x5a9a344a);
            ROM_LOAD("dz-12.rom", 0x20000, 0x10000, 0x817fae99);
            ROM_LOAD("dz-14.rom", 0x30000, 0x10000, 0x0abbf76d);
            ROM_LOAD("dz-11.rom", 0x40000, 0x10000, 0xa5e19c24);
            ROM_LOAD("dz-13.rom", 0x50000, 0x10000, 0x3e7c0405);
            ROM_LOAD("dz-17.rom", 0x60000, 0x10000, 0x40361b8b);
            ROM_LOAD("dz-18.rom", 0x70000, 0x10000, 0x8d219489);

            ROM_REGION(0x40000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* tiles */

            ROM_LOAD("dz-07.rom", 0x00000, 0x10000, 0xe7455167);
            ROM_LOAD("dz-08.rom", 0x10000, 0x10000, 0x32f9ddfe);
            ROM_LOAD("dz-09.rom", 0x20000, 0x10000, 0xbb6efc02);
            ROM_LOAD("dz-10.rom", 0x30000, 0x10000, 0x6ef9963b);

            ROM_REGION(0x0800, REGION_PROMS);
            ROM_LOAD("dz19a.10d", 0x0000, 0x0400, 0x47e1f83b);
            ROM_LOAD("dz20a.11d", 0x0400, 0x0400, 0xd8fe2d99);
            ROM_END();
        }
    };

    static RomLoadPtr rom_ghostb3 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x50000, REGION_CPU1);
            ROM_LOAD("dz01-3b", 0x08000, 0x08000, 0xc8cc862a);
            ROM_LOAD("dz-02.rom", 0x10000, 0x10000, 0x8e117541);
            ROM_LOAD("dz-03.rom", 0x20000, 0x10000, 0x5606a8f4);
            ROM_LOAD("dz04-1", 0x30000, 0x10000, 0x3c3eb09f);
            ROM_LOAD("dz05", 0x40000, 0x10000, 0xb4971d33);

            ROM_REGION(2 * 0x10000, REGION_CPU2);/* 64K for sound CPU + 64k for decrypted opcodes */

            ROM_LOAD("dz-06.rom", 0x8000, 0x8000, 0x798f56df);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("dz-00.rom", 0x00000, 0x08000, 0x992b4f31);

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("dz-15.rom", 0x00000, 0x10000, 0xa01a5fd9);
            ROM_LOAD("dz-16.rom", 0x10000, 0x10000, 0x5a9a344a);
            ROM_LOAD("dz-12.rom", 0x20000, 0x10000, 0x817fae99);
            ROM_LOAD("dz-14.rom", 0x30000, 0x10000, 0x0abbf76d);
            ROM_LOAD("dz-11.rom", 0x40000, 0x10000, 0xa5e19c24);
            ROM_LOAD("dz-13.rom", 0x50000, 0x10000, 0x3e7c0405);
            ROM_LOAD("dz-17.rom", 0x60000, 0x10000, 0x40361b8b);
            ROM_LOAD("dz-18.rom", 0x70000, 0x10000, 0x8d219489);

            ROM_REGION(0x40000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* tiles */

            ROM_LOAD("dz-07.rom", 0x00000, 0x10000, 0xe7455167);
            ROM_LOAD("dz-08.rom", 0x10000, 0x10000, 0x32f9ddfe);
            ROM_LOAD("dz-09.rom", 0x20000, 0x10000, 0xbb6efc02);
            ROM_LOAD("dz-10.rom", 0x30000, 0x10000, 0x6ef9963b);

            ROM_REGION(0x0800, REGION_PROMS);
            ROM_LOAD("dz19a.10d", 0x0000, 0x0400, 0x47e1f83b);
            ROM_LOAD("dz20a.11d", 0x0400, 0x0400, 0xd8fe2d99);
            ROM_END();
        }
    };

    static RomLoadPtr rom_meikyuh = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);
            ROM_LOAD("dw-01.rom", 0x08000, 0x08000, 0x87610c39);
            ROM_LOAD("dw-02.rom", 0x10000, 0x10000, 0x40c9b0b8);
            ROM_LOAD("dz-03.rom", 0x20000, 0x10000, 0x5606a8f4);
            ROM_LOAD("dw-04.rom", 0x30000, 0x10000, 0x235c0c36);

            ROM_REGION(0x10000, REGION_CPU2);/* 64K for sound CPU */

            ROM_LOAD("dw-05.rom", 0x8000, 0x8000, 0xc28c4d82);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("dw-00.rom", 0x00000, 0x8000, 0x3d25f15c);

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("dw-14.rom", 0x00000, 0x10000, 0x9b0dbfa9);
            ROM_LOAD("dw-15.rom", 0x10000, 0x10000, 0x95683fda);
            ROM_LOAD("dw-11.rom", 0x20000, 0x10000, 0x1b1fcca7);
            ROM_LOAD("dw-13.rom", 0x30000, 0x10000, 0xe7413056);
            ROM_LOAD("dw-10.rom", 0x40000, 0x10000, 0x57667546);
            ROM_LOAD("dw-12.rom", 0x50000, 0x10000, 0x4c548db8);
            ROM_LOAD("dw-16.rom", 0x60000, 0x10000, 0xe5bcf927);
            ROM_LOAD("dw-17.rom", 0x70000, 0x10000, 0x9e10f723);

            ROM_REGION(0x40000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* tiles */

            ROM_LOAD("dw-06.rom", 0x00000, 0x10000, 0xb65e029d);
            ROM_LOAD("dw-07.rom", 0x10000, 0x10000, 0x668d995d);
            ROM_LOAD("dw-08.rom", 0x20000, 0x10000, 0xbb2cf4a0);
            ROM_LOAD("dw-09.rom", 0x30000, 0x10000, 0x6a528d13);

            ROM_REGION(0x0800, REGION_PROMS);
            ROM_LOAD("dz19a.10d", 0x0000, 0x0400, 0x00000000);/* Not the real ones! */

            ROM_LOAD("dz20a.11d", 0x0400, 0x0400, 0x00000000);/* These are from ghostbusters */

            ROM_END();
        }
    };

    static RomLoadPtr rom_srdarwin = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x28000, REGION_CPU1);
            ROM_LOAD("dy_01.rom", 0x20000, 0x08000, 0x1eeee4ff);
            ROM_CONTINUE(0x08000, 0x08000);
            ROM_LOAD("dy_00.rom", 0x10000, 0x10000, 0x2bf6b461);

            ROM_REGION(2 * 0x10000, REGION_CPU2);/* 64K for sound CPU + 64k for decrypted opcodes */

            ROM_LOAD("dy_04.rom", 0x8000, 0x8000, 0x2ae3591c);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("dy_05.rom", 0x00000, 0x4000, 0x8780e8a3);

            ROM_REGION(0x30000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("dy_07.rom", 0x00000, 0x8000, 0x97eaba60);
            ROM_LOAD("dy_06.rom", 0x08000, 0x8000, 0xc279541b);
            ROM_LOAD("dy_09.rom", 0x10000, 0x8000, 0xd30d1745);
            ROM_LOAD("dy_08.rom", 0x18000, 0x8000, 0x71d645fd);
            ROM_LOAD("dy_11.rom", 0x20000, 0x8000, 0xfd9ccc5b);
            ROM_LOAD("dy_10.rom", 0x28000, 0x8000, 0x88770ab8);

            ROM_REGION(0x40000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* tiles */

            ROM_LOAD("dy_03.rom", 0x00000, 0x4000, 0x44f2a4f9);
            ROM_CONTINUE(0x10000, 0x4000);
            ROM_CONTINUE(0x20000, 0x4000);
            ROM_CONTINUE(0x30000, 0x4000);
            ROM_LOAD("dy_02.rom", 0x08000, 0x4000, 0x522d9a9e);
            ROM_CONTINUE(0x18000, 0x4000);
            ROM_CONTINUE(0x28000, 0x4000);
            ROM_CONTINUE(0x38000, 0x4000);
            ROM_END();
        }
    };

    static RomLoadPtr rom_gondo = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);
            ROM_LOAD("dt-00.256", 0x08000, 0x08000, 0xa8cf9118);
            ROM_LOAD("dt-01.512", 0x10000, 0x10000, 0xc39bb877);
            ROM_LOAD("dt-02.512", 0x20000, 0x10000, 0xbb5e674b);
            ROM_LOAD("dt-03.512", 0x30000, 0x10000, 0x99c32b13);

            ROM_REGION(0x10000, REGION_CPU2);/* 64K for sound CPU */

            ROM_LOAD("dt-05.256", 0x8000, 0x8000, 0xec08aa29);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("dt-14.256", 0x00000, 0x08000, 0x4bef16e1);

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("dt-19.512", 0x00000, 0x10000, 0xda2abe4b);
            ROM_LOAD("dt-20.256", 0x10000, 0x08000, 0x42d01002);
            ROM_LOAD("dt-16.512", 0x20000, 0x10000, 0xe9955d8f);
            ROM_LOAD("dt-18.256", 0x30000, 0x08000, 0xc0c5df1c);
            ROM_LOAD("dt-15.512", 0x40000, 0x10000, 0xa54b2eb6);
            ROM_LOAD("dt-17.256", 0x50000, 0x08000, 0x3bbcff0d);
            ROM_LOAD("dt-21.512", 0x60000, 0x10000, 0x1c5f682d);
            ROM_LOAD("dt-22.256", 0x70000, 0x08000, 0xc1876a5f);

            ROM_REGION(0x80000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* tiles */

            ROM_LOAD("dt-08.512", 0x00000, 0x08000, 0xaec483f5);
            ROM_CONTINUE(0x10000, 0x08000);
            ROM_LOAD("dt-09.256", 0x08000, 0x08000, 0x446f0ce0);
            ROM_LOAD("dt-06.512", 0x20000, 0x08000, 0x3fe1527f);
            ROM_CONTINUE(0x30000, 0x08000);
            ROM_LOAD("dt-07.256", 0x28000, 0x08000, 0x61f9bce5);
            ROM_LOAD("dt-12.512", 0x40000, 0x08000, 0x1a72ca8d);
            ROM_CONTINUE(0x50000, 0x08000);
            ROM_LOAD("dt-13.256", 0x48000, 0x08000, 0xccb81aec);
            ROM_LOAD("dt-10.512", 0x60000, 0x08000, 0xcfcfc9ed);
            ROM_CONTINUE(0x70000, 0x08000);
            ROM_LOAD("dt-11.256", 0x68000, 0x08000, 0x53e9cf17);
            ROM_END();
        }
    };

    static RomLoadPtr rom_makyosen = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);
            ROM_LOAD("ds00", 0x08000, 0x08000, 0x33bb16fe);
            ROM_LOAD("dt-01.512", 0x10000, 0x10000, 0xc39bb877);
            ROM_LOAD("ds02", 0x20000, 0x10000, 0x925307a4);
            ROM_LOAD("ds03", 0x30000, 0x10000, 0x9c0fcbf6);

            ROM_REGION(0x10000, REGION_CPU2);/* 64K for sound CPU */

            ROM_LOAD("ds05", 0x8000, 0x8000, 0xe6e28ca9);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("ds14", 0x00000, 0x08000, 0x00cbe9c8);

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("dt-19.512", 0x00000, 0x10000, 0xda2abe4b);
            ROM_LOAD("ds20", 0x10000, 0x08000, 0x0eef7f56);
            ROM_LOAD("dt-16.512", 0x20000, 0x10000, 0xe9955d8f);
            ROM_LOAD("ds18", 0x30000, 0x08000, 0x2b2d1468);
            ROM_LOAD("dt-15.512", 0x40000, 0x10000, 0xa54b2eb6);
            ROM_LOAD("ds17", 0x50000, 0x08000, 0x75ae349a);
            ROM_LOAD("dt-21.512", 0x60000, 0x10000, 0x1c5f682d);
            ROM_LOAD("ds22", 0x70000, 0x08000, 0xc8ffb148);

            ROM_REGION(0x80000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* tiles */

            ROM_LOAD("dt-08.512", 0x00000, 0x08000, 0xaec483f5);
            ROM_CONTINUE(0x10000, 0x08000);
            ROM_LOAD("dt-09.256", 0x08000, 0x08000, 0x446f0ce0);
            ROM_LOAD("dt-06.512", 0x20000, 0x08000, 0x3fe1527f);
            ROM_CONTINUE(0x30000, 0x08000);
            ROM_LOAD("dt-07.256", 0x28000, 0x08000, 0x61f9bce5);
            ROM_LOAD("dt-12.512", 0x40000, 0x08000, 0x1a72ca8d);
            ROM_CONTINUE(0x50000, 0x08000);
            ROM_LOAD("dt-13.256", 0x48000, 0x08000, 0xccb81aec);
            ROM_LOAD("dt-10.512", 0x60000, 0x08000, 0xcfcfc9ed);
            ROM_CONTINUE(0x70000, 0x08000);
            ROM_LOAD("dt-11.256", 0x68000, 0x08000, 0x53e9cf17);
            ROM_END();
        }
    };

    static RomLoadPtr rom_oscar = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);
            ROM_LOAD("ed10", 0x08000, 0x08000, 0xf9b0d4d4);
            ROM_LOAD("ed09", 0x10000, 0x10000, 0xe2d4bba9);

            ROM_REGION(0x10000, REGION_CPU2);/* CPU 2, 1st 16k is empty */

            ROM_LOAD("ed11", 0x0000, 0x10000, 0x10e5d919);

            ROM_REGION(2 * 0x10000, REGION_CPU3);/* 64K for sound CPU + 64k for decrypted opcodes */

            ROM_LOAD("ed12", 0x8000, 0x8000, 0x432031c5);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("ed08", 0x00000, 0x04000, 0x308ac264);

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("ed04", 0x00000, 0x10000, 0x416a791b);
            ROM_LOAD("ed05", 0x20000, 0x10000, 0xfcdba431);
            ROM_LOAD("ed06", 0x40000, 0x10000, 0x7d50bebc);
            ROM_LOAD("ed07", 0x60000, 0x10000, 0x8fdf0fa5);

            ROM_REGION(0x80000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* tiles */

            ROM_LOAD("ed01", 0x00000, 0x10000, 0xd3a58e9e);
            ROM_LOAD("ed03", 0x20000, 0x10000, 0x4fc4fb0f);
            ROM_LOAD("ed00", 0x40000, 0x10000, 0xac201f2d);
            ROM_LOAD("ed02", 0x60000, 0x10000, 0x7ddc5651);
            ROM_END();
        }
    };

    static RomLoadPtr rom_oscarj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);
            ROM_LOAD("du10", 0x08000, 0x08000, 0x120040d8);
            ROM_LOAD("ed09", 0x10000, 0x10000, 0xe2d4bba9);

            ROM_REGION(0x10000, REGION_CPU2);/* CPU 2, 1st 16k is empty */

            ROM_LOAD("du11", 0x0000, 0x10000, 0xff45c440);

            ROM_REGION(2 * 0x10000, REGION_CPU3);/* 64K for sound CPU + 64k for decrypted opcodes */

            ROM_LOAD("ed12", 0x8000, 0x8000, 0x432031c5);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("ed08", 0x00000, 0x04000, 0x308ac264);

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("ed04", 0x00000, 0x10000, 0x416a791b);
            ROM_LOAD("ed05", 0x20000, 0x10000, 0xfcdba431);
            ROM_LOAD("ed06", 0x40000, 0x10000, 0x7d50bebc);
            ROM_LOAD("ed07", 0x60000, 0x10000, 0x8fdf0fa5);

            ROM_REGION(0x80000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* tiles */

            ROM_LOAD("ed01", 0x00000, 0x10000, 0xd3a58e9e);
            ROM_LOAD("ed03", 0x20000, 0x10000, 0x4fc4fb0f);
            ROM_LOAD("ed00", 0x40000, 0x10000, 0xac201f2d);
            ROM_LOAD("ed02", 0x60000, 0x10000, 0x7ddc5651);
            ROM_END();
        }
    };

    static RomLoadPtr rom_lastmiss = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);
            ROM_LOAD("dl03-6", 0x08000, 0x08000, 0x47751a5e);/* Rev 6 roms */

            ROM_LOAD("lm_dl04.rom", 0x10000, 0x10000, 0x7dea1552);

            ROM_REGION(0x10000, REGION_CPU2);/* CPU 2, 1st 16k is empty */

            ROM_LOAD("lm_dl02.rom", 0x0000, 0x10000, 0xec9b5daf);

            ROM_REGION(0x10000, REGION_CPU3);/* 64K for sound CPU */

            ROM_LOAD("lm_dl05.rom", 0x8000, 0x8000, 0x1a5df8c0);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("lm_dl01.rom", 0x00000, 0x2000, 0xf3787a5d);
            ROM_CONTINUE(0x06000, 0x2000);
            ROM_CONTINUE(0x04000, 0x2000);
            ROM_CONTINUE(0x02000, 0x2000);

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("lm_dl11.rom", 0x00000, 0x08000, 0x36579d3b);
            ROM_LOAD("lm_dl12.rom", 0x20000, 0x08000, 0x2ba6737e);
            ROM_LOAD("lm_dl13.rom", 0x40000, 0x08000, 0x39a7dc93);
            ROM_LOAD("lm_dl10.rom", 0x60000, 0x08000, 0xfe275ea8);

            ROM_REGION(0x80000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* tiles */

            ROM_LOAD("lm_dl09.rom", 0x00000, 0x10000, 0x6a5a0c5d);
            ROM_LOAD("lm_dl08.rom", 0x20000, 0x10000, 0x3b38cfce);
            ROM_LOAD("lm_dl07.rom", 0x40000, 0x10000, 0x1b60604d);
            ROM_LOAD("lm_dl06.rom", 0x60000, 0x10000, 0xc43c26a7);
            ROM_END();
        }
    };

    static RomLoadPtr rom_lastmss2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);
            ROM_LOAD("lm_dl03.rom", 0x08000, 0x08000, 0x357f5f6b);/* Rev 5 roms */

            ROM_LOAD("lm_dl04.rom", 0x10000, 0x10000, 0x7dea1552);

            ROM_REGION(0x10000, REGION_CPU2);/* CPU 2, 1st 16k is empty */

            ROM_LOAD("lm_dl02.rom", 0x0000, 0x10000, 0xec9b5daf);

            ROM_REGION(0x10000, REGION_CPU3);/* 64K for sound CPU */

            ROM_LOAD("lm_dl05.rom", 0x8000, 0x8000, 0x1a5df8c0);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("lm_dl01.rom", 0x00000, 0x2000, 0xf3787a5d);
            ROM_CONTINUE(0x06000, 0x2000);
            ROM_CONTINUE(0x04000, 0x2000);
            ROM_CONTINUE(0x02000, 0x2000);

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("lm_dl11.rom", 0x00000, 0x08000, 0x36579d3b);
            ROM_LOAD("lm_dl12.rom", 0x20000, 0x08000, 0x2ba6737e);
            ROM_LOAD("lm_dl13.rom", 0x40000, 0x08000, 0x39a7dc93);
            ROM_LOAD("lm_dl10.rom", 0x60000, 0x08000, 0xfe275ea8);

            ROM_REGION(0x80000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* tiles */

            ROM_LOAD("lm_dl09.rom", 0x00000, 0x10000, 0x6a5a0c5d);
            ROM_LOAD("lm_dl08.rom", 0x20000, 0x10000, 0x3b38cfce);
            ROM_LOAD("lm_dl07.rom", 0x40000, 0x10000, 0x1b60604d);
            ROM_LOAD("lm_dl06.rom", 0x60000, 0x10000, 0xc43c26a7);
            ROM_END();
        }
    };

    static RomLoadPtr rom_shackled = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x48000, REGION_CPU1);
            ROM_LOAD("dk-02.rom", 0x08000, 0x08000, 0x87f8fa85);
            ROM_LOAD("dk-06.rom", 0x10000, 0x10000, 0x69ad62d1);
            ROM_LOAD("dk-05.rom", 0x20000, 0x10000, 0x598dd128);
            ROM_LOAD("dk-04.rom", 0x30000, 0x10000, 0x36d305d4);
            ROM_LOAD("dk-03.rom", 0x40000, 0x08000, 0x6fd90fd1);

            ROM_REGION(0x10000, REGION_CPU2);/* CPU 2, 1st 16k is empty */

            ROM_LOAD("dk-01.rom", 0x00000, 0x10000, 0x71fe3bda);

            ROM_REGION(0x10000, REGION_CPU3);/* 64K for sound CPU */

            ROM_LOAD("dk-07.rom", 0x08000, 0x08000, 0x887e4bcc);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("dk-00.rom", 0x00000, 0x08000, 0x69b975aa);

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("dk-12.rom", 0x00000, 0x10000, 0x615c2371);
            ROM_LOAD("dk-13.rom", 0x10000, 0x10000, 0x479aa503);
            ROM_LOAD("dk-14.rom", 0x20000, 0x10000, 0xcdc24246);
            ROM_LOAD("dk-15.rom", 0x30000, 0x10000, 0x88db811b);
            ROM_LOAD("dk-16.rom", 0x40000, 0x10000, 0x061a76bd);
            ROM_LOAD("dk-17.rom", 0x50000, 0x10000, 0xa6c5d8af);
            ROM_LOAD("dk-18.rom", 0x60000, 0x10000, 0x4d466757);
            ROM_LOAD("dk-19.rom", 0x70000, 0x10000, 0x1911e83e);

            ROM_REGION(0x80000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* tiles */

            ROM_LOAD("dk-11.rom", 0x00000, 0x10000, 0x5cf5719f);
            ROM_LOAD("dk-10.rom", 0x20000, 0x10000, 0x408e6d08);
            ROM_LOAD("dk-09.rom", 0x40000, 0x10000, 0xc1557fac);
            ROM_LOAD("dk-08.rom", 0x60000, 0x10000, 0x5e54e9f5);
            ROM_END();
        }
    };

    static RomLoadPtr rom_breywood = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x48000, REGION_CPU1);
            ROM_LOAD("7.bin", 0x08000, 0x08000, 0xc19856b9);
            ROM_LOAD("3.bin", 0x10000, 0x10000, 0x2860ea02);
            ROM_LOAD("4.bin", 0x20000, 0x10000, 0x0fdd915e);
            ROM_LOAD("5.bin", 0x30000, 0x10000, 0x71036579);
            ROM_LOAD("6.bin", 0x40000, 0x08000, 0x308f4893);

            ROM_REGION(0x10000, REGION_CPU2);/* CPU 2, 1st 16k is empty */

            ROM_LOAD("8.bin", 0x0000, 0x10000, 0x3d9fb623);

            ROM_REGION(0x10000, REGION_CPU3);/* 64K for sound CPU */

            ROM_LOAD("2.bin", 0x8000, 0x8000, 0x4a471c38);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("1.bin", 0x00000, 0x08000, 0x815a891a);

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("20.bin", 0x00000, 0x10000, 0x2b7634f2);
            ROM_LOAD("19.bin", 0x10000, 0x10000, 0x4530a952);
            ROM_LOAD("18.bin", 0x20000, 0x10000, 0x87c28833);
            ROM_LOAD("17.bin", 0x30000, 0x10000, 0xbfb43a4d);
            ROM_LOAD("16.bin", 0x40000, 0x10000, 0xf9848cc4);
            ROM_LOAD("15.bin", 0x50000, 0x10000, 0xbaa3d218);
            ROM_LOAD("14.bin", 0x60000, 0x10000, 0x12afe533);
            ROM_LOAD("13.bin", 0x70000, 0x10000, 0x03373755);

            ROM_REGION(0x80000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* tiles */

            ROM_LOAD("9.bin", 0x00000, 0x10000, 0x067e2a43);
            ROM_LOAD("10.bin", 0x20000, 0x10000, 0xc19733aa);
            ROM_LOAD("11.bin", 0x40000, 0x10000, 0xe37d5dbe);
            ROM_LOAD("12.bin", 0x60000, 0x10000, 0xbeee880f);
            ROM_END();
        }
    };

    static RomLoadPtr rom_csilver = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x48000, REGION_CPU1);
            ROM_LOAD("a4", 0x08000, 0x08000, 0x02dd8cfc);
            ROM_LOAD("a2", 0x10000, 0x10000, 0x570fb50c);
            ROM_LOAD("a3", 0x20000, 0x10000, 0x58625890);

            ROM_REGION(0x10000, REGION_CPU2);/* CPU 2, 1st 16k is empty */

            ROM_LOAD("a5", 0x0000, 0x10000, 0x29432691);

            ROM_REGION(0x18000, REGION_CPU3);/* 64K for sound CPU */

            ROM_LOAD("a6", 0x10000, 0x08000, 0xeb32cf25);
            ROM_CONTINUE(0x08000, 0x08000);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("a1", 0x00000, 0x08000, 0xf01ef985);

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites (3bpp) */

            ROM_LOAD("b5", 0x00000, 0x10000, 0x80f07915);
            /* 0x10000-0x1ffff empy */
            ROM_LOAD("b4", 0x20000, 0x10000, 0xd32c02e7);
            /* 0x30000-0x3ffff empy */
            ROM_LOAD("b3", 0x40000, 0x10000, 0xac78b76b);
            /* 0x50000-0x5ffff empy */
            /* 0x60000-0x7ffff empy (no 4th plane) */

            ROM_REGION(0x80000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* tiles (3bpp) */

            ROM_LOAD("a7", 0x00000, 0x10000, 0xb6fb208c);
            ROM_LOAD("a8", 0x10000, 0x10000, 0xee3e1817);
            ROM_LOAD("a9", 0x20000, 0x10000, 0x705900fe);
            ROM_LOAD("a10", 0x30000, 0x10000, 0x3192571d);
            ROM_LOAD("b1", 0x40000, 0x10000, 0x3ef77a32);
            ROM_LOAD("b2", 0x50000, 0x10000, 0x9cf3d5b8);
            ROM_END();
        }
    };

    static RomLoadPtr rom_garyoret = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x58000, REGION_CPU1);
            ROM_LOAD("dv00", 0x08000, 0x08000, 0xcceaaf05);
            ROM_LOAD("dv01", 0x10000, 0x10000, 0xc33fc18a);
            ROM_LOAD("dv02", 0x20000, 0x10000, 0xf9e26ce7);
            ROM_LOAD("dv03", 0x30000, 0x10000, 0x55d8d699);
            ROM_LOAD("dv04", 0x40000, 0x10000, 0xed3d00ee);

            ROM_REGION(0x10000, REGION_CPU2);/* 64K for sound CPU */

            ROM_LOAD("dv05", 0x08000, 0x08000, 0xc97c347f);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("dv14", 0x00000, 0x08000, 0xfb2bc581);/* Characters */

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("dv22", 0x00000, 0x10000, 0xcef0367e);
            ROM_LOAD("dv21", 0x10000, 0x08000, 0x90042fb7);
            ROM_LOAD("dv20", 0x20000, 0x10000, 0x451a2d8c);
            ROM_LOAD("dv19", 0x30000, 0x08000, 0x14e1475b);
            ROM_LOAD("dv18", 0x40000, 0x10000, 0x7043bead);
            ROM_LOAD("dv17", 0x50000, 0x08000, 0x28f449d7);
            ROM_LOAD("dv16", 0x60000, 0x10000, 0x37e4971e);
            ROM_LOAD("dv15", 0x70000, 0x08000, 0xca41b6ac);

            ROM_REGION(0x80000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* tiles */

            ROM_LOAD("dv08", 0x00000, 0x08000, 0x89c13e15);
            ROM_CONTINUE(0x10000, 0x08000);
            ROM_LOAD("dv09", 0x08000, 0x08000, 0x6a345a23);
            ROM_CONTINUE(0x18000, 0x08000);

            ROM_LOAD("dv06", 0x20000, 0x08000, 0x1eb52a20);
            ROM_CONTINUE(0x30000, 0x08000);
            ROM_LOAD("dv07", 0x28000, 0x08000, 0xe7346ef8);
            ROM_CONTINUE(0x38000, 0x08000);

            ROM_LOAD("dv12", 0x40000, 0x08000, 0x46ba5af4);
            ROM_CONTINUE(0x50000, 0x08000);
            ROM_LOAD("dv13", 0x48000, 0x08000, 0xa7af6dfd);
            ROM_CONTINUE(0x58000, 0x08000);

            ROM_LOAD("dv10", 0x60000, 0x08000, 0x68b6d75c);
            ROM_CONTINUE(0x70000, 0x08000);
            ROM_LOAD("dv11", 0x68000, 0x08000, 0xb5948aee);
            ROM_CONTINUE(0x78000, 0x08000);
            ROM_END();
        }
    };
    /**
     * ***************************************************************************
     */
    /* Ghostbusters, Darwin, Oscar use a "Deco 222" custom 6502 for sound. */
    public static InitDriverPtr init_deco222 = new InitDriverPtr() {
        public void handler() {
    	int A,sound_cpu;
    	UBytePtr rom;
    	int diff;
    
    
    	sound_cpu = 1;
    	/* Oscar has three CPUs */
    	if (Machine.drv.cpu[2].cpu_type != 0) sound_cpu = 2;
    
    	/* bits 5 and 6 of the opcodes are swapped */
    	rom = memory_region(REGION_CPU1+sound_cpu);
    	diff = memory_region_length(REGION_CPU1+sound_cpu) / 2;
    
    	memory_set_opcode_base(sound_cpu,new UBytePtr(rom,diff));
    
    	for (A = 0;A < 0x10000;A++)
    		rom.write(A + diff,(rom.read(A) & 0x9f) | ((rom.read(A) & 0x20) << 1) | ((rom.read(A) & 0x40) >> 1));
        }
    };
    public static InitDriverPtr init_meikyuh = new InitDriverPtr() {
        public void handler() {
            /* Blank out garbage in colour prom to avoid colour overflow */
            UBytePtr RAM = memory_region(REGION_PROMS);
            memset(RAM, 0x20, 0, 0xe0);
        }
    };
    public static InitDriverPtr init_ghostb = new InitDriverPtr() {
        public void handler() {
            init_deco222.handler();
            init_meikyuh.handler();
        }
    };

    /**
     * ***************************************************************************
     */
    public static GameDriver driver_cobracom = new GameDriver("1988", "cobracom", "dec8.java", rom_cobracom, null, machine_driver_cobracom, input_ports_cobracom, null, ROT0, "Data East Corporation", "Cobra-Command (World revision 5)", GAME_NO_COCKTAIL);
    public static GameDriver driver_cobracmj = new GameDriver("1988", "cobracmj", "dec8.java", rom_cobracmj, driver_cobracom, machine_driver_cobracom, input_ports_cobracom, null, ROT0, "Data East Corporation", "Cobra-Command (Japan)", GAME_NO_COCKTAIL);
    public static GameDriver driver_ghostb = new GameDriver("1987", "ghostb", "dec8.java", rom_ghostb, null, machine_driver_ghostb, input_ports_ghostb, init_ghostb, ROT0, "Data East USA", "The Real Ghostbusters (US 2 Players)", GAME_NO_COCKTAIL);
    public static GameDriver driver_ghostb3 = new GameDriver("1987", "ghostb3", "dec8.java", rom_ghostb3, driver_ghostb, machine_driver_ghostb, input_ports_ghostb, init_ghostb, ROT0, "Data East USA", "The Real Ghostbusters (US 3 Players)", GAME_NO_COCKTAIL);
    public static GameDriver driver_meikyuh = new GameDriver("1987", "meikyuh", "dec8.java", rom_meikyuh, driver_ghostb, machine_driver_ghostb, input_ports_meikyuh, init_meikyuh, ROT0, "Data East Corporation", "Meikyuu Hunter G (Japan)", GAME_NO_COCKTAIL);
    public static GameDriver driver_srdarwin = new GameDriver("1987", "srdarwin", "dec8.java", rom_srdarwin, null, machine_driver_srdarwin, input_ports_srdarwin, init_deco222, ROT270, "Data East Corporation", "Super Real Darwin (Japan)");
    public static GameDriver driver_gondo = new GameDriver("1987", "gondo", "dec8.java", rom_gondo, null, machine_driver_gondo, input_ports_gondo, null, ROT270, "Data East USA", "Gondomania (US)", GAME_NO_COCKTAIL);
    public static GameDriver driver_makyosen = new GameDriver("1987", "makyosen", "dec8.java", rom_makyosen, driver_gondo, machine_driver_gondo, input_ports_gondo, null, ROT270, "Data East Corporation", "Makyou Senshi (Japan)", GAME_NO_COCKTAIL);
    public static GameDriver driver_oscar = new GameDriver("1988", "oscar", "dec8.java", rom_oscar, null, machine_driver_oscar, input_ports_oscar, init_deco222, ROT0, "Data East USA", "Psycho-Nics Oscar (US)", GAME_NO_COCKTAIL);
    public static GameDriver driver_oscarj = new GameDriver("1987", "oscarj", "dec8.java", rom_oscarj, driver_oscar, machine_driver_oscar, input_ports_oscar, init_deco222, ROT0, "Data East Corporation", "Psycho-Nics Oscar (Japan)", GAME_NO_COCKTAIL);
    public static GameDriver driver_lastmiss = new GameDriver("1986", "lastmiss", "dec8.java", rom_lastmiss, null, machine_driver_lastmiss, input_ports_lastmiss, null, ROT270, "Data East USA", "Last Mission (US revision 6)", GAME_NO_COCKTAIL);
    public static GameDriver driver_lastmss2 = new GameDriver("1986", "lastmss2", "dec8.java", rom_lastmss2, driver_lastmiss, machine_driver_lastmiss, input_ports_lastmiss, null, ROT270, "Data East USA", "Last Mission (US revision 5)", GAME_NO_COCKTAIL);
    public static GameDriver driver_shackled = new GameDriver("1986", "shackled", "dec8.java", rom_shackled, null, machine_driver_shackled, input_ports_shackled, null, ROT0, "Data East USA", "Shackled (US)", GAME_NO_COCKTAIL);
    public static GameDriver driver_breywood = new GameDriver("1986", "breywood", "dec8.java", rom_breywood, driver_shackled, machine_driver_shackled, input_ports_shackled, null, ROT0, "Data East Corporation", "Breywood (Japan revision 2)", GAME_NO_COCKTAIL);
    public static GameDriver driver_csilver = new GameDriver("1987", "csilver", "dec8.java", rom_csilver, null, machine_driver_csilver, input_ports_csilver, null, ROT0, "Data East Corporation", "Captain Silver (Japan)", GAME_NO_COCKTAIL);
    public static GameDriver driver_garyoret = new GameDriver("1987", "garyoret", "dec8.java", rom_garyoret, null, machine_driver_garyoret, input_ports_garyoret, null, ROT0, "Data East Corporation", "Garyo Retsuden (Japan)", GAME_NO_COCKTAIL);

}
