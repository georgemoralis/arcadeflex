/**
 * ported to 0.58
 * ported to 0.37b5
 */
package gr.codebb.arcadeflex.v058.sound;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static common.libc.cstring.*;
import static common.libc.expressions.*;
import static common.libc.cstdlib.*;
import static gr.codebb.arcadeflex.v036.mame.common.memory_region;
import static gr.codebb.arcadeflex.v036.mame.common.memory_region_length;
import static gr.codebb.arcadeflex.v036.mame.common.readsamples;
import gr.codebb.arcadeflex.v036.mame.driverH.WriteHandlerPtr;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import gr.codebb.arcadeflex.v036.mame.sndintrf.snd_interface;
import gr.codebb.arcadeflex.v036.mame.sndintrfH.MachineSound;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.SOUND_VLM5030;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v036.sound.mixer.mixer_allocate_channel;
import gr.codebb.arcadeflex.v036.sound.streams.StreamInitPtr;
import static gr.codebb.arcadeflex.v036.sound.streams.stream_init;
import static gr.codebb.arcadeflex.v036.sound.streams.stream_update;
import static gr.codebb.arcadeflex.v058.sound.vlm5030H.*;


public class vlm5030 extends snd_interface {

    public vlm5030() {
        this.name = "VLM5030";
        this.sound_num = SOUND_VLM5030;
    }

    @Override
    public int chips_num(MachineSound msound) {
        return 0;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((VLM5030interface) msound.sound_interface).baseclock;
    }

    /* interpolator per frame   */
    public static final int FR_SIZE = 4;
    /* samples per interpolator */
    public static final int IP_SIZE_SLOWER = (240 / FR_SIZE);
    public static final int IP_SIZE_SLOW = (200 / FR_SIZE);
    public static final int IP_SIZE_NORMAL = (160 / FR_SIZE);
    public static final int IP_SIZE_FAST = (120 / FR_SIZE);
    public static final int IP_SIZE_FASTER = (80 / FR_SIZE);

    static VLM5030interface intf;

    static int channel;
    static int schannel;

    /* need to save state */
    static UBytePtr VLM5030_rom;
    static int VLM5030_address_mask;
    static char VLM5030_address;
    static /*UINT8*/ int u8_pin_BSY;
    static /*UINT8*/ int u8_pin_ST;
    static /*UINT8*/ int u8_pin_VCU;
    static /*UINT8*/ int u8_pin_RST;
    static /*UINT8*/ int u8_latch_data;
    static char vcu_addr_h;
    static /*UINT8*/ int u8_VLM5030_parameter;
    static /*UINT8*/ int u8_VLM5030_phase;

    /* state of option paramter */
    static int VLM5030_frame_size;
    static int pitch_offset;
    static /*UINT8*/ int u8_interp_step;

    static /*UINT8*/ int u8_interp_count;
    /* number of interp periods    */
    static /*UINT8*/ int u8_sample_count;
    /* sample number within interp */
    static /*UINT8*/ int u8_pitch_count;

    /* these contain data describing the current and previous voice frames */
    static char old_energy;
    static /*UINT8*/ int u8_old_pitch;
    static short[] old_k = new short[10];
    static char target_energy;
    static /*UINT8*/ int u8_target_pitch;
    static short[] target_k = new short[10];

    static char new_energy;
    static /*UINT8*/ int u8_new_pitch;
    static short[] new_k = new short[10];

    /* these are all used to contain the current state of the sound generation */
    static /*unsigned*/ int u32_current_energy;
    static /*unsigned*/ int u32_current_pitch;
    static int[] current_k = new int[10];

    static int[] x = new int[10];

    /* phase value */
    public static final int PH_RESET = 0;
    public static final int PH_IDLE = 1;
    public static final int PH_SETUP = 2;
    public static final int PH_WAIT = 3;
    public static final int PH_RUN = 4;
    public static final int PH_STOP = 5;
    public static final int PH_END = 5;

    static int VLM5030_speed_table[]
            = {
                IP_SIZE_NORMAL,
                IP_SIZE_FAST,
                IP_SIZE_FASTER,
                IP_SIZE_FASTER,
                IP_SIZE_NORMAL,
                IP_SIZE_SLOWER,
                IP_SIZE_SLOW,
                IP_SIZE_SLOW
            };

    static String VLM_NAME = "VLM5030";

    /* ROM Tables */

 /* This is the energy lookup table */

 /* sampled from real chip */
    static char energytable[]
            = {
                0, 2, 4, 6, 10, 12, 14, 18, /*  0-7  */
                22, 26, 30, 34, 38, 44, 48, 54, /*  8-15 */
                62, 68, 76, 84, 94, 102, 114, 124, /* 16-23 */
                136, 150, 164, 178, 196, 214, 232, 254 /* 24-31 */};

    /* This is the pitch lookup table */
    static /* unsigned*/ char pitchtable[]
            = {
                1, /* 0     : random mode */
                22, /* 1     : start=22    */
                23, 24, 25, 26, 27, 28, 29, 30, /*  2- 9 : 1step       */
                32, 34, 36, 38, 40, 42, 44, 46, /* 10-17 : 2step       */
                50, 54, 58, 62, 66, 70, 74, 78, /* 18-25 : 4step       */
                86, 94, 102, 110, 118, 126 /* 26-31 : 8step       */};

    static short K1_table[] = {
        -24898, -25672, -26446, -27091, -27736, -28252, -28768, -29155,
        -29542, -29929, -30316, -30574, -30832, -30961, -31219, -31348,
        -31606, -31735, -31864, -31864, -31993, -32122, -32122, -32251,
        -32251, -32380, -32380, -32380, -32509, -32509, -32509, -32509,
        24898, 23995, 22963, 21931, 20770, 19480, 18061, 16642,
        15093, 13416, 11610, 9804, 7998, 6063, 3999, 1935,
        0, -1935, -3999, -6063, -7998, -9804, -11610, -13416,
        -15093, -16642, -18061, -19480, -20770, -21931, -22963, -23995
    };
    static short K2_table[] = {
        0, -3096, -6321, -9417, -12513, -15351, -18061, -20770,
        -23092, -25285, -27220, -28897, -30187, -31348, -32122, -32638,
        0, 32638, 32122, 31348, 30187, 28897, 27220, 25285,
        23092, 20770, 18061, 15351, 12513, 9417, 6321, 3096
    };
    static short K3_table[] = {
        0, -3999, -8127, -12255, -16384, -20383, -24511, -28639,
        32638, 28639, 24511, 20383, 16254, 12255, 8127, 3999
    };
    static short K5_table[] = {
        0, -8127, -16384, -24511, 32638, 24511, 16254, 8127
    };

    /* check sample file */
    static int check_samplefile(int num) {
        if (Machine.samples == null) {
            return 0;
        }
        if (Machine.samples.total <= num) {
            return 0;
        }
        if (Machine.samples.sample[num] == null) {
            return 0;
        }
        /* sample file is found */
        return 1;
    }

    static int get_bits(int sbit, int bits) {
        int offset = VLM5030_address + (sbit >> 3);
        int data;

        data = VLM5030_rom.read(offset & VLM5030_address_mask)
                + (((int) VLM5030_rom.read((offset + 1) & VLM5030_address_mask)) * 256);
        data >>= (sbit & 7);
        data &= (0xff >> (8 - bits));

        return data;
    }

    /* get next frame */
    static int parse_frame() {
        int cmd;
        int i;

        /* remember previous frame */
        old_energy = new_energy;
        u8_old_pitch = u8_new_pitch & 0xFF;
        for (i = 0; i <= 9; i++) {
            old_k[i] = new_k[i];
        }

        /* command byte check */
        cmd = VLM5030_rom.read(VLM5030_address & VLM5030_address_mask);
        if ((cmd & 0x01) != 0) {
            /* extend frame */
            new_energy = 0;
            u8_new_pitch = 0;
            for (i = 0; i <= 9; i++) {
                new_k[i] = 0;
            }
            VLM5030_address++;
            if ((cmd & 0x02) != 0) {
                /* end of speech */

 /* logerror("VLM5030 %04X end \n",VLM5030_address ); */
                return 0;
            } else {
                /* silent frame */
                int nums = ((cmd >> 2) + 1) * 2;
                /* logerror("VLM5030 %04X silent %d frame\n",VLM5030_address,nums ); */
                return nums * FR_SIZE;
            }
        }
        /* pitch */
        u8_new_pitch = (pitchtable[get_bits(1, 5)] + pitch_offset) & 0xff;
        /* energy */
        new_energy = energytable[get_bits(6, 5)];

        /* 10 K's */
        new_k[9] = K5_table[get_bits(11, 3)];
        new_k[8] = K5_table[get_bits(14, 3)];
        new_k[7] = K5_table[get_bits(17, 3)];
        new_k[6] = K5_table[get_bits(20, 3)];
        new_k[5] = K5_table[get_bits(23, 3)];
        new_k[4] = K5_table[get_bits(26, 3)];
        new_k[3] = K3_table[get_bits(29, 4)];
        new_k[2] = K3_table[get_bits(33, 4)];
        new_k[1] = K2_table[get_bits(37, 5)];
        new_k[0] = K1_table[get_bits(42, 6)];

        VLM5030_address += 6;
        logerror("VLM5030 %04X voice \n", VLM5030_address);
        return FR_SIZE;
    }
    /* decode and buffering data */
    public static StreamInitPtr vlm5030_update_callback = new StreamInitPtr() {
        public void handler(int chip, ShortPtr buffer, int length) {
            int buf_count = 0;
            int interp_effect;
            int i;
            int[] u = new int[11];

            /* running */
            if (u8_VLM5030_phase == PH_RUN || u8_VLM5030_phase == PH_STOP) {
                /* playing speech */
                while (length > 0) {
                    int current_val;

                    /* check new interpolator or  new frame */
                    if (u8_sample_count == 0) {
                        if (u8_VLM5030_phase == PH_STOP) {
                            u8_VLM5030_phase = PH_END;
                            u8_sample_count = 1;
                            //gotophase_stop;
                            phase_stop(buffer, length, buf_count);
                            return;
                            /* continue to end phase */
                        }
                        u8_sample_count = VLM5030_frame_size;
                        /* interpolator changes */
                        if (u8_interp_count == 0) {
                            /* change to new frame */
                            u8_interp_count = parse_frame() & 0xFF;
                            /* with change phase */
                            if (u8_interp_count == 0) {
                                /* end mark found */
                                u8_interp_count = FR_SIZE;
                                u8_sample_count = VLM5030_frame_size;
                                /* end -> stop time */
                                u8_VLM5030_phase = PH_STOP;
                            }
                            /* Set old target as new start of frame */
                            u32_current_energy = old_energy;
                            u32_current_pitch = u8_old_pitch;
                            for (i = 0; i <= 9; i++) {
                                current_k[i] = old_k[i];
                            }
                            /* is this a zero energy frame? */
                            if (u32_current_energy == 0) {
                                /*printf("processing frame: zero energy\n");*/
                                target_energy = 0;
                                u8_target_pitch = u32_current_pitch & 0xFF;
                                for (i = 0; i <= 9; i++) {
                                    target_k[i] = (short) current_k[i];
                                }
                            } else {
                                /*printf("processing frame: Normal\n");*/
 /*printf("*** Energy = %d\n",current_energy);*/
 /*printf("proc: %d %d\n",last_fbuf_head,fbuf_head);*/
                                target_energy = new_energy;
                                u8_target_pitch = u8_new_pitch & 0xFF;
                                for (i = 0; i <= 9; i++) {
                                    target_k[i] = new_k[i];
                                }
                            }
                        }
                        /* next interpolator */
 /* Update values based on step values 25% , 50% , 75% , 100% */
                        u8_interp_count = (u8_interp_count - u8_interp_step) & 0xFF;//u8_interp_count -= u8_interp_step;
                        /* 3,2,1,0 -> 1,2,3,4 */
                        interp_effect = FR_SIZE - (u8_interp_count % FR_SIZE);
                        u32_current_energy = old_energy + (target_energy - old_energy) * interp_effect / FR_SIZE;
                        if (u8_old_pitch > 1) {
                            u32_current_pitch = u8_old_pitch + (u8_target_pitch - u8_old_pitch) * interp_effect / FR_SIZE;
                        }
                        for (i = 0; i <= 9; i++) {
                            current_k[i] = old_k[i] + (target_k[i] - old_k[i]) * interp_effect / FR_SIZE;
                        }
                    }
                    /* calcrate digital filter */
                    if (old_energy == 0) {
                        /* generate silent samples here */
                        current_val = 0x00;
                    } else if (u8_old_pitch <= 1) {
                        /* generate unvoiced samples here */
                        current_val = (rand() & 1) != 0 ? u32_current_energy : -u32_current_energy;
                    } else {
                        /* generate voiced samples here */
                        current_val = (u8_pitch_count == 0) ? u32_current_energy : 0;
                    }

                    /* Lattice filter here */
                    u[10] = current_val;
                    for (i = 9; i >= 0; i--) {
                        u[i] = u[i + 1] - ((current_k[i] * x[i]) / 32768);
                    }
                    for (i = 9; i >= 1; i--) {
                        x[i] = x[i - 1] + ((current_k[i - 1] * u[i - 1]) / 32768);
                    }
                    x[0] = u[0];

                    /* clipping, buffering */
                    if (u[0] > 511) {
                        buffer.write(buf_count, (short) (511 << 6));
                    } else if (u[0] < -511) {
                        buffer.write(buf_count, (short) (-511 << 6));
                    } else {
                        buffer.write(buf_count, (short) (u[0] << 6));
                    }
                    buf_count++;

                    /* sample count */
                    u8_sample_count = (u8_sample_count - 1) & 0xFF;//u8_sample_count--;
                    /* pitch */
                    u8_pitch_count = (u8_pitch_count + 1) & 0xFF; //u8_pitch_count++;
                    if (u8_pitch_count >= u32_current_pitch) {
                        u8_pitch_count = 0;
                    }
                    /* size */
                    length--;
                }
                /*		return;*/
            }
            /* stop phase */
            phase_stop(buffer, length, buf_count);
        }
    };

    public static void phase_stop(ShortPtr buffer, int length, int buf_count) {
        switch (u8_VLM5030_phase) {
            case PH_SETUP:
                if (u8_sample_count <= length) {
                    u8_sample_count = 0;
                    /* logerror("VLM5030 BSY=H\n" ); */
 /* pin_BSY = 1; */
                    u8_VLM5030_phase = PH_WAIT;
                } else {
                    u8_sample_count = (u8_sample_count - length) & 0xFF; //u8_sample_count -= length;
                }
                break;
            case PH_END:
                if (u8_sample_count <= length) {
                    u8_sample_count = 0;
                    /* logerror("VLM5030 BSY=L\n" ); */
                    u8_pin_BSY = 0;
                    u8_VLM5030_phase = PH_IDLE;
                } else {
                    u8_sample_count = (u8_sample_count - length) & 0xFF;//u8_sample_count -= length;
                }
        }
        /* silent buffering */
        while (length > 0) {
            buffer.write(buf_count++, (short) 0x00);
            length--;
        }
    }

    /* realtime update */
    static void VLM5030_update() {
        stream_update(channel, 0);
    }

    /* setup parameteroption when RST=H */
    static void VLM5030_setup_parameter(int param) {
        /* latch parameter value */
        u8_VLM5030_parameter = param & 0xFF;

        /* bit 0,1 : 4800bps / 9600bps , interporator step */
        if ((param & 2) != 0) /* bit 1 = 1 , 9600bps */ {
            u8_interp_step = 4;
            /* 9600bps : no interporator */
        } else if ((param & 1) != 0) /* bit1 = 0 & bit0 = 1 , 4800bps */ {
            u8_interp_step = 2;
            /* 4800bps : 2 interporator */
        } else /* bit1 = bit0 = 0 : 2400bps */ {
            u8_interp_step = 1;
            /* 2400bps : 4 interporator */
        }

        /* bit 3,4,5 : speed (frame size) */
        VLM5030_frame_size = VLM5030_speed_table[(param >> 3) & 7];

        /* bit 6,7 : low / high pitch */
        if ((param & 0x80) != 0) /* bit7=1 , high pitch */ {
            pitch_offset = -8;
        } else if ((param & 0x40) != 0) /* bit6=1 , low pitch */ {
            pitch_offset = 8;
        } else {
            pitch_offset = 0;
        }
    }

    static void VLM5030_reset() {
        u8_VLM5030_phase = PH_RESET;
        VLM5030_address = 0;
        vcu_addr_h = 0;
        u8_pin_BSY = 0;

        old_energy = 0;
        u8_old_pitch = 0;
        new_energy = 0;
        u8_new_pitch = 0;
        u32_current_energy = 0;
        u32_current_pitch = 0;
        target_energy = 0;
        u8_target_pitch = 0;
        memset(old_k, 0, sizeof(old_k));
        memset(new_k, 0, sizeof(new_k));
        memset(current_k, 0, sizeof(current_k));
        memset(target_k, 0, sizeof(target_k));
        u8_interp_count = u8_sample_count = u8_pitch_count = 0;
        memset(x, 0, sizeof(x));
        /* reset parameters */
        VLM5030_setup_parameter(0x00);
    }

    /* set speech rom address */
    public static void VLM5030_set_rom(UBytePtr speech_rom) {
        VLM5030_rom = new UBytePtr(speech_rom);
    }

    /* get BSY pin level */
    public static int VLM5030_BSY() {
        VLM5030_update();
        return u8_pin_BSY;
    }
    /* latch contoll data */
    public static WriteHandlerPtr VLM5030_data_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            u8_latch_data = data & 0xFF;
        }
    };

    /* set RST pin level : reset / set table address A8-A15 */
    public static void VLM5030_RST(int pin) {
        if (u8_pin_RST != 0) {
            if (pin == 0) {
                /* H -> L : latch parameters */
                u8_pin_RST = 0;
                VLM5030_setup_parameter(u8_latch_data);
            }
        } else {
            if (pin != 0) {
                /* L -> H : reset chip */
                u8_pin_RST = 1;
                if (u8_pin_BSY != 0) {
                    VLM5030_reset();
                }
            }
        }
    }

    /* set VCU pin level : ?? unknown */
    public static void VLM5030_VCU(int pin) {
        /* direct mode / indirect mode */
        u8_pin_VCU = pin;
        return;
    }

    /* set ST pin level  : set table address A0-A7 / start speech */
    public static void VLM5030_ST(int pin) {
        int table;

        if (u8_pin_ST != pin) {
            /* pin level is change */
            if (pin == 0) {
                /* H -> L */
                u8_pin_ST = 0;

                if (u8_pin_VCU != 0) {
                    /* direct access mode & address High */
                    vcu_addr_h = (char) (((int) u8_latch_data << 8) + 0x01);
                } else {
                    /* start speech */
                    if (Machine.sample_rate == 0) {
                        u8_pin_BSY = 0;
                        return;
                    }
                    /* check access mode */
                    if (vcu_addr_h != 0) {
                        /* direct access mode */
                        VLM5030_address = (char) ((vcu_addr_h & 0xff00) + u8_latch_data);
                        vcu_addr_h = 0;
                    } else {
                        /* indirect accedd mode */
                        table = (u8_latch_data & 0xfe) + (((int) u8_latch_data & 1) << 8);
                        VLM5030_address = (char) ((((int) VLM5030_rom.read(table & VLM5030_address_mask)) << 8)
                                | VLM5030_rom.read((table + 1) & VLM5030_address_mask));
                    }
                    VLM5030_update();
                    /* logerror("VLM5030 %02X start adr=%04X\n",table/2,VLM5030_address ); */
 /* reset process status */
                    u8_sample_count = VLM5030_frame_size;
                    u8_interp_count = FR_SIZE;
                    /* clear filter */
 /* start after 3 sampling cycle */
                    u8_VLM5030_phase = PH_RUN;
                }
            } else {
                /* L -> H */
                u8_pin_ST = 1;
                /* setup speech , BSY on after 30ms? */
                u8_VLM5030_phase = PH_SETUP;
                u8_sample_count = 1;
                /* wait time for busy on */
                u8_pin_BSY = 1;
                /* */
            }
        }
    }

    @Override
    public int start(MachineSound msound) {
        int emulation_rate;

        intf = (VLM5030interface) msound.sound_interface;
        Machine.samples = readsamples(intf.samplenames, Machine.gamedrv.name);

        emulation_rate = intf.baseclock / 440;

        /* reset input pins */
        u8_pin_RST = u8_pin_ST = u8_pin_VCU = 0;
        u8_latch_data = 0;

        VLM5030_reset();
        u8_VLM5030_phase = PH_IDLE;

        VLM5030_rom = memory_region(intf.memory_region);
        /* memory size */
        if (intf.memory_size == 0) {
            VLM5030_address_mask = memory_region_length(intf.memory_region) - 1;
        } else {
            VLM5030_address_mask = intf.memory_size - 1;
        }

        channel = stream_init(VLM_NAME, intf.volume, emulation_rate, 0, vlm5030_update_callback);
        if (channel == -1) {
            return 1;
        }

        schannel = mixer_allocate_channel(intf.volume);

        return 0;
    }

    @Override
    public void stop() {
        //no functionality expected
    }

    @Override
    public void update() {
        VLM5030_update();
    }

    @Override
    public void reset() {
        //no functionality expected
    }

}
