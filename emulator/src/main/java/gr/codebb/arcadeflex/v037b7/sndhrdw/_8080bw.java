/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.sndhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw._8080bw.*;
//TODO
import static gr.codebb.arcadeflex.v036.cpu.i8039.i8039H.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.sound.samplesH.*;
import static gr.codebb.arcadeflex.v036.sound.mixer.*;
import static gr.codebb.arcadeflex.v037b7.machine._8080bw.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.*;
import static arcadeflex.v036.sound.samples.*;
import static arcadeflex.v036.sound.dacH.*;

public class _8080bw {
/*TODO*///
/*TODO*///    public static SN76477interface invaders_sn76477_interface = new SN76477interface(
/*TODO*///            1, /* 1 chip */
/*TODO*///            new int[]{25}, /* mixing level   pin description		 */
/*TODO*///            new double[]{0 /* N/C */}, /*	4  noise_res		 */
/*TODO*///            new double[]{0 /* N/C */}, /*	5  filter_res		 */
/*TODO*///            new double[]{0 /* N/C */}, /*	6  filter_cap		 */
/*TODO*///            new double[]{0 /* N/C */}, /*	7  decay_res		 */
/*TODO*///            new double[]{0 /* N/C */}, /*	8  attack_decay_cap  */
/*TODO*///            new double[]{RES_K(100)}, /* 10  attack_res		 */
/*TODO*///            new double[]{RES_K(56)}, /* 11  amplitude_res	 */
/*TODO*///            new double[]{RES_K(10)}, /* 12  feedback_res 	 */
/*TODO*///            new double[]{0 /* N/C */}, /* 16  vco_voltage		 */
/*TODO*///            new double[]{CAP_U(0.1)}, /* 17  vco_cap			 */
/*TODO*///            new double[]{RES_K(8.2)}, /* 18  vco_res			 */
/*TODO*///            new double[]{5.0}, /* 19  pitch_voltage	 */
/*TODO*///            new double[]{RES_K(120)}, /* 20  slf_res			 */
/*TODO*///            new double[]{CAP_U(1.0)}, /* 21  slf_cap			 */
/*TODO*///            new double[]{0 /* N/C */}, /* 23  oneshot_cap		 */
/*TODO*///            new double[]{0 /* N/C */} /* 24  oneshot_res		 */
/*TODO*///    );

    static String invaders_sample_names[]
            = {
                "*invaders",
                "1.wav", /* Shot/Missle */
                "2.wav", /* Base Hit/Explosion */
                "3.wav", /* Invader Hit */
                "4.wav", /* Fleet move 1 */
                "5.wav", /* Fleet move 2 */
                "6.wav", /* Fleet move 3 */
                "7.wav", /* Fleet move 4 */
                "8.wav", /* UFO/Saucer Hit */
                "9.wav", /* Bonus Base */
                null /* end of array */};

    public static Samplesinterface invaders_samples_interface = new Samplesinterface(
            4, /* 4 channels */
            25, /* volume */
            invaders_sample_names
    );

/*TODO*///    public static SN76477interface invad2ct_sn76477_interface = new SN76477interface(
/*TODO*///            2, /* 2 chips */
/*TODO*///            new int[]{25, 25}, /* mixing level   pin description		 */
/*TODO*///            new double[]{0, 0 /* N/C */}, /*	4  noise_res		 */
/*TODO*///            new double[]{0, 0 /* N/C */}, /*	5  filter_res		 */
/*TODO*///            new double[]{0, 0 /* N/C */}, /*	6  filter_cap		 */
/*TODO*///            new double[]{0, 0 /* N/C */}, /*	7  decay_res		 */
/*TODO*///            new double[]{0, 0 /* N/C */}, /*	8  attack_decay_cap  */
/*TODO*///            new double[]{RES_K(100), RES_K(100)}, /* 10  attack_res		 */
/*TODO*///            new double[]{RES_K(56), RES_K(56)}, /* 11  amplitude_res	 */
/*TODO*///            new double[]{RES_K(10), RES_K(10)}, /* 12  feedback_res 	 */
/*TODO*///            new double[]{0, 0 /* N/C */}, /* 16  vco_voltage		 */
/*TODO*///            new double[]{CAP_U(0.1), CAP_U(0.047)}, /* 17  vco_cap			 */
/*TODO*///            new double[]{RES_K(8.2), RES_K(39)}, /* 18  vco_res			 */
/*TODO*///            new double[]{5.0, 5.0}, /* 19  pitch_voltage	 */
/*TODO*///            new double[]{RES_K(120), RES_K(120)}, /* 20  slf_res			 */
/*TODO*///            new double[]{CAP_U(1.0), CAP_U(1.0)}, /* 21  slf_cap			 */
/*TODO*///            new double[]{0, 0 /* N/C */}, /* 23  oneshot_cap		 */
/*TODO*///            new double[]{0, 0 /* N/C */} /* 24  oneshot_res		 */
/*TODO*///    );

    public static String invad2ct_sample_names[]
            = {
                "*invaders",
                "1.wav", /* Shot/Missle - Player 1 */
                "2.wav", /* Base Hit/Explosion - Player 1 */
                "3.wav", /* Invader Hit - Player 1 */
                "4.wav", /* Fleet move 1 - Player 1 */
                "5.wav", /* Fleet move 2 - Player 1 */
                "6.wav", /* Fleet move 3 - Player 1 */
                "7.wav", /* Fleet move 4 - Player 1 */
                "8.wav", /* UFO/Saucer Hit - Player 1 */
                "9.wav", /* Bonus Base - Player 1 */
                "11.wav", /* Shot/Missle - Player 2 */
                "12.wav", /* Base Hit/Explosion - Player 2 */
                "13.wav", /* Invader Hit - Player 2 */
                "14.wav", /* Fleet move 1 - Player 2 */
                "15.wav", /* Fleet move 2 - Player 2 */
                "16.wav", /* Fleet move 3 - Player 2 */
                "17.wav", /* Fleet move 4 - Player 2 */
                "18.wav", /* UFO/Saucer Hit - Player 2 */
                null /* end of array */};

    public static Samplesinterface invad2ct_samples_interface = new Samplesinterface(
            8, /* 8 channels */
            25, /* volume */
            invad2ct_sample_names
    );

    public static InitMachineHandlerPtr init_machine_invaders = new InitMachineHandlerPtr() {
        public void handler() {
            install_port_write_handler(0, 0x03, 0x03, invaders_sh_port3_w);
            install_port_write_handler(0, 0x05, 0x05, invaders_sh_port5_w);

/*TODO*///            SN76477_envelope_1_w(0, 1);
/*TODO*///            SN76477_envelope_2_w(0, 0);
/*TODO*///            SN76477_mixer_a_w(0, 0);
/*TODO*///            SN76477_mixer_b_w(0, 0);
/*TODO*///            SN76477_mixer_c_w(0, 0);
/*TODO*///            SN76477_vco_w(0, 1);
        }
    };

    public static InitMachineHandlerPtr init_machine_invad2ct = new InitMachineHandlerPtr() {
        public void handler() {
            init_machine_invaders.handler();

            install_port_write_handler(0, 0x01, 0x01, invad2ct_sh_port1_w);
            install_port_write_handler(0, 0x07, 0x07, invad2ct_sh_port7_w);

/*TODO*///            SN76477_envelope_1_w(1, 1);
/*TODO*///            SN76477_envelope_2_w(1, 0);
/*TODO*///            SN76477_mixer_a_w(1, 0);
/*TODO*///            SN76477_mixer_b_w(1, 0);
/*TODO*///            SN76477_mixer_c_w(1, 0);
/*TODO*///            SN76477_vco_w(1, 1);
        }
    };

    /*
	   Note: For invad2ct, the Player 1 sounds are the same as for the
	         original and deluxe versions.  Player 2 sounds are all
	         different, and are triggered by writes to port 1 and port 7.
	
     */
    static void invaders_sh_1_w(int board, int data, char[] u8_last) {
        int base_channel, base_sample;

        base_channel = 4 * board;
        base_sample = 9 * board;

/*TODO*///        SN76477_enable_w(board, NOT(data & 0x01));
/*TODO*///        /* Saucer Sound */

        if ((data & 0x02) != 0 && (~u8_last[0] & 0x02) != 0) {
            sample_start(base_channel + 0, base_sample + 0, 0);	/* Shot Sound */
        }

        if ((data & 0x04) != 0 && (~u8_last[0] & 0x04) != 0) {
            sample_start(base_channel + 1, base_sample + 1, 0);	/* Base Hit */
        }

        if ((~data & 0x04) != 0 && (u8_last[0] & 0x04) != 0) {
            sample_stop(base_channel + 1);
        }

        if ((data & 0x08) != 0 && (~u8_last[0] & 0x08) != 0) {
            sample_start(base_channel + 0, base_sample + 2, 0);	/* Invader Hit */
        }

        if ((data & 0x10) != 0 && (~u8_last[0] & 0x10) != 0) {
            sample_start(base_channel + 2, 8, 0);				/* Bonus Missle Base */
        }

        invaders_screen_red_w(data & 0x04);

        u8_last[0] = (char) (data & 0xFF);
    }

    static void invaders_sh_2_w(int board, int data, char[] u8_last) {
        int base_channel, base_sample;

        base_channel = 4 * board;
        base_sample = 9 * board;

        if ((data & 0x01) != 0 && (~u8_last[0] & 0x01) != 0) {
            sample_start(base_channel + 1, base_sample + 3, 0);	/* Fleet 1 */
        }

        if ((data & 0x02) != 0 && (~u8_last[0] & 0x02) != 0) {
            sample_start(base_channel + 1, base_sample + 4, 0);	/* Fleet 2 */
        }

        if ((data & 0x04) != 0 && (~u8_last[0] & 0x04) != 0) {
            sample_start(base_channel + 1, base_sample + 5, 0);	/* Fleet 3 */
        }

        if ((data & 0x08) != 0 && (~u8_last[0] & 0x08) != 0) {
            sample_start(base_channel + 1, base_sample + 6, 0);	/* Fleet 4 */
        }

        if ((data & 0x10) != 0 && (~u8_last[0] & 0x10) != 0) {
            sample_start(base_channel + 3, base_sample + 7, 0);	/* Saucer Hit */
        }

        invaders_flipscreen_w(data & 0x20);

        u8_last[0] = (char) (data & 0xFF);
    }

    static /*unsigned*/ char[] last_1 = new char[1];
    public static WriteHandlerPtr invad2ct_sh_port1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            invaders_sh_1_w(1, data, last_1);
        }
    };
    static /*unsigned*/ char[] last_3 = new char[1];
    public static WriteHandlerPtr invaders_sh_port3_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            invaders_sh_1_w(0, data, last_3);
        }
    };
    static /*unsigned*/ char[] last_5 = new char[1];
    public static WriteHandlerPtr invaders_sh_port5_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            invaders_sh_2_w(0, data, last_5);
        }
    };
    static /*unsigned*/ char[] last_7 = new char[1];
    public static WriteHandlerPtr invad2ct_sh_port7_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            invaders_sh_2_w(1, data, last_7);
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Gun Fight"                                  */
 /*                                                     */
    /**
     * ****************************************************
     */
    public static InitMachineHandlerPtr init_machine_gunfight = new InitMachineHandlerPtr() {
        public void handler() {
            install_port_read_handler(0, 0x00, 0x00, gunfight_port_0_r);
            install_port_read_handler(0, 0x01, 0x01, gunfight_port_1_r);
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Boot Hill"                                  */
 /*                                                     */
    /**
     * ****************************************************
     */
    static String boothill_sample_names[]
            = {
                "*boothill", /* in case we ever find any bootlegs hehehe */
                "addcoin.wav",
                "endgame.wav",
                "gunshot.wav",
                "killed.wav",
                null /* end of array */};

    public static Samplesinterface boothill_samples_interface = new Samplesinterface(
            9, /* 9 channels */
            25, /* volume */
            boothill_sample_names
    );

    /* HC 4/14/98 NOTE: *I* THINK there are sounds missing...
	i dont know for sure... but that is my guess....... */
    public static InitMachineHandlerPtr init_machine_boothill = new InitMachineHandlerPtr() {
        public void handler() {
           install_port_read_handler(0, 0x00, 0x00, boothill_port_0_r);
           install_port_read_handler(0, 0x01, 0x01, boothill_port_1_r);

            install_port_write_handler(0, 0x03, 0x03, boothill_sh_port3_w);
            install_port_write_handler(0, 0x05, 0x05, boothill_sh_port5_w);
        }
    };

    public static WriteHandlerPtr boothill_sh_port3_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            switch (data) {
                case 0x0c:
                    sample_start(0, 0, 0);
                    break;

                case 0x18:
                case 0x28:
                    sample_start(1, 2, 0);
                    break;

                case 0x48:
                case 0x88:
                    sample_start(2, 3, 0);
                    break;
            }
        }
    };

    /* HC 4/14/98 */
    public static WriteHandlerPtr boothill_sh_port5_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            switch (data) {
                case 0x3b:
                    sample_start(2, 1, 0);
                    break;
            }
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Taito "Balloon Bomber"                              */
 /*                                                     */
    /**
     * ****************************************************
     */
    /* This only does the color swap for the explosion */
 /* We do not have correct samples so sound not done */
    public static InitMachineHandlerPtr init_machine_ballbomb = new InitMachineHandlerPtr() {
        public void handler() {
            install_port_write_handler(0, 0x03, 0x03, ballbomb_sh_port3_w);
            install_port_write_handler(0, 0x05, 0x05, ballbomb_sh_port5_w);
        }
    };

    public static WriteHandlerPtr ballbomb_sh_port3_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            invaders_screen_red_w(data & 0x04);
        }
    };

    public static WriteHandlerPtr ballbomb_sh_port5_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
/*TODO*///            invaders_flip_screen_w(data & 0x20);
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Taito "Polaris"		                               */
 /*                                                     */
    /**
     * ****************************************************
     */
    public static InitMachineHandlerPtr init_machine_polaris = new InitMachineHandlerPtr() {
        public void handler() {
            install_port_write_handler(0, 0x06, 0x06, polaris_sh_port6_w);
        }
    };

    public static WriteHandlerPtr polaris_sh_port6_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            coin_lockout_global_w.handler(0, data & 0x04);

/*TODO*///            invaders_flip_screen_w(data & 0x20);
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
    /* Nintendo "Sheriff"                              	   */
    /*                                                     */
    /**
     * ****************************************************
     */
    public static DACinterface sheriff_dac_interface = new DACinterface(
            1,
            new int[]{50}
    );

/*TODO*///    public static SN76477interface sheriff_sn76477_interface = new SN76477interface(
/*TODO*///            1, /* 1 chip */
/*TODO*///            new int[]{50}, /* mixing level   pin description		 */
/*TODO*///            new double[]{RES_K(36)}, /*	4  noise_res		 */
/*TODO*///            new double[]{RES_K(100)}, /*	5  filter_res		 */
/*TODO*///            new double[]{CAP_U(0.001)}, /*	6  filter_cap		 */
/*TODO*///            new double[]{RES_K(620)}, /*	7  decay_res		 */
/*TODO*///            new double[]{CAP_U(1.0)}, /*	8  attack_decay_cap  */
/*TODO*///            new double[]{RES_K(20)}, /* 10  attack_res		 */
/*TODO*///            new double[]{RES_K(150)}, /* 11  amplitude_res	 */
/*TODO*///            new double[]{RES_K(47)}, /* 12  feedback_res 	 */
/*TODO*///            new double[]{0}, /* 16  vco_voltage		 */
/*TODO*///            new double[]{CAP_U(0.001)}, /* 17  vco_cap			 */
/*TODO*///            new double[]{RES_M(1.5)}, /* 18  vco_res			 */
/*TODO*///            new double[]{0.0}, /* 19  pitch_voltage	 */
/*TODO*///            new double[]{RES_M(1.5)}, /* 20  slf_res			 */
/*TODO*///            new double[]{CAP_U(0.047)}, /* 21  slf_cap			 */
/*TODO*///            new double[]{CAP_U(0.047)}, /* 23  oneshot_cap		 */
/*TODO*///            new double[]{RES_K(560)} /* 24  oneshot_res		 */
/*TODO*///    );
/*TODO*///
/*TODO*///    public static output_changed_cbPtr sheriff_74123_0_output_changed_cb = new output_changed_cbPtr() {
/*TODO*///        public void handler() {
/*TODO*///            SN76477_vco_w(0, TTL74123_output_r(0));
/*TODO*///            SN76477_mixer_b_w(0, NOT(TTL74123_output_r(0)));
/*TODO*///
/*TODO*///            SN76477_enable_w(0, (TTL74123_output_comp_r(0) != 0 && TTL74123_output_comp_r(1) != 0) ? 1 : 0);
/*TODO*///        }
/*TODO*///    };
/*TODO*///
/*TODO*///    public static output_changed_cbPtr sheriff_74123_1_output_changed_cb = new output_changed_cbPtr() {
/*TODO*///        public void handler() {
/*TODO*///        SN76477_set_vco_voltage(0, TTL74123_output_comp_r(1) == 0 ? 5.0 : 0.0);
/*TODO*///
/*TODO*///        SN76477_enable_w(0, (TTL74123_output_comp_r(0) != 0 && TTL74123_output_comp_r(1) != 0) ? 1 : 0);
/*TODO*///    }};
/*TODO*///
/*TODO*///    	static  TTL74123_interface sheriff_74123_0_intf = new TTL74123_interface
/*TODO*///	(
/*TODO*///		RES_K(33),
/*TODO*///		CAP_U(33),
/*TODO*///		sheriff_74123_0_output_changed_cb
/*TODO*///        );
/*TODO*///    	static  TTL74123_interface sheriff_74123_1_intf = new TTL74123_interface
/*TODO*///	(
/*TODO*///		RES_K(33),
/*TODO*///		CAP_U(33),
/*TODO*///		sheriff_74123_1_output_changed_cb
/*TODO*///        );
    public static InitMachineHandlerPtr init_machine_sheriff = new InitMachineHandlerPtr() {
        public void handler() {
            install_port_write_handler(0, 0x04, 0x04, sheriff_sh_port4_w);
            install_port_write_handler(0, 0x05, 0x05, sheriff_sh_port5_w);
            install_port_write_handler(0, 0x06, 0x06, sheriff_sh_port6_w);

/*TODO*///            		TTL74123_config(0, sheriff_74123_0_intf);
/*TODO*///		TTL74123_config(1, sheriff_74123_1_intf);
/*TODO*///            /* set up the fixed connections */
/*TODO*/// 		TTL74123_reset_comp_w  (0, 1);
/*TODO*///		TTL74123_trigger_comp_w(0, 0);
/*TODO*///            		TTL74123_trigger_comp_w(1, 0);
/*TODO*///            SN76477_envelope_1_w(0, 1);
/*TODO*///            SN76477_envelope_2_w(0, 0);
/*TODO*///            SN76477_noise_clock_w(0, 0);
/*TODO*///            SN76477_mixer_a_w(0, 0);
/*TODO*///            SN76477_mixer_c_w(0, 0);
        }
    };

    static int sheriff_t0, sheriff_t1, sheriff_p1, sheriff_p2;

    public static WriteHandlerPtr sheriff_sh_port4_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            sheriff_t0 = data & 1;

            sheriff_p1 = (sheriff_p1 & 0x4f)
                    | ((data & 0x02) << 3)
                    | /* P1.4 */ ((data & 0x08) << 2)
                    | /* P1.5 */ ((data & 0x20) << 2);
            /* P1.7 */

            soundlatch_w.handler(0, sheriff_p1);

            cpu_set_irq_line(1, I8035_EXT_INT, ((sheriff_p1 & 0x70) == 0x70) ? ASSERT_LINE : CLEAR_LINE);

/*TODO*///            		TTL74123_trigger_w   (0, data & 0x04);
/*TODO*///            		TTL74123_reset_comp_w(1, data & 0x04);
/*TODO*///		TTL74123_trigger_w   (1, data & 0x10);
        }
    };

    public static WriteHandlerPtr sheriff_sh_port5_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            sheriff_t1 = (data >> 5) & 1;

            sheriff_p1 = (sheriff_p1 & 0xb0)
                    | ((data & 0x01) << 3)
                    | /* P1.3 */ ((data & 0x02) << 1)
                    | /* P1.2 */ ((data & 0x04) >> 1)
                    | /* P1.1 */ ((data & 0x08) >> 3)
                    | /* P1.0 */ ((data & 0x10) << 2);
            /* P1.6 */

            soundlatch_w.handler(0, sheriff_p1);

            cpu_set_irq_line(1, I8035_EXT_INT, ((sheriff_p1 & 0x70) == 0x70) ? ASSERT_LINE : CLEAR_LINE);
        }
    };

    public static WriteHandlerPtr sheriff_sh_port6_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
/*TODO*///            flip_screen_w.handler(offset, data & 0x20);
        }
    };

    public static ReadHandlerPtr sheriff_sh_t0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return sheriff_t0;
        }
    };

    public static ReadHandlerPtr sheriff_sh_t1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return sheriff_t1;
        }
    };

    public static ReadHandlerPtr sheriff_sh_p1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return soundlatch_r.handler(0);
        }
    };

    public static ReadHandlerPtr sheriff_sh_p2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return sheriff_p2;
        }
    };

    public static WriteHandlerPtr sheriff_sh_p2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            sheriff_p2 = data;

/*TODO*///            DAC_data_w(0, (sheriff_p2 & 0x80) != 0 ? 0xff : 0x00);
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Nintendo "HeliFire"		                           */
 /*                                                     */
    /**
     * ****************************************************
     */
    public static InitMachineHandlerPtr init_machine_helifire = new InitMachineHandlerPtr() {
        public void handler() {
            install_port_write_handler(0, 0x06, 0x06, helifire_sh_port6_w);
        }
    };

    public static WriteHandlerPtr helifire_sh_port6_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
/*TODO*///            flip_screen_w.handler(offset, data & 0x20);
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Sea Wolf"                                   */
 /*                                                     */
    /**
     * ****************************************************
     */
    public static InitMachineHandlerPtr init_machine_seawolf = new InitMachineHandlerPtr() {
        public void handler() {
            install_port_read_handler(0, 0x01, 0x01, seawolf_port_1_r);
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Desert Gun"                                 */
 /*                                                     */
    /**
     * ****************************************************
     */
    public static InitMachineHandlerPtr init_machine_desertgu = new InitMachineHandlerPtr() {
        public void handler() {
            install_port_read_handler(0, 0x01, 0x01, desertgu_port_1_r);

            install_port_write_handler(0, 0x07, 0x07, desertgu_controller_select_w);
        }
    };

/*TODO*///    /**
/*TODO*///     * ****************************************************
/*TODO*///     */
/*TODO*///    /*                                                     */
/*TODO*/// /* Taito "Space Chaser" 							   */
/*TODO*/// /*                                                     */
/*TODO*///    /**
/*TODO*///     * ****************************************************
/*TODO*///     */
/*TODO*///    /*
/*TODO*///	 *  The dot sound is a square wave clocked by either the
/*TODO*///	 *  the 8V or 4V signals
/*TODO*///	 *
/*TODO*///	 *  The frequencies are (for the 8V signal):
/*TODO*///	 *
/*TODO*///	 *  19.968 MHz crystal / 2 (Qa of 74160 #10) . 9.984MHz
/*TODO*///	 *					   / 2 (7474 #14) . 4.992MHz
/*TODO*///	 *					   / 256+16 (74161 #5 and #8) . 18352.94Hz
/*TODO*///	 *					   / 8 (8V) . 2294.12 Hz
/*TODO*///	 * 					   / 2 the final freq. is 2 toggles . 1147.06Hz
/*TODO*///	 *
/*TODO*///	 *  for 4V, it's double at 2294.12Hz
/*TODO*///     */
    static int channel_dot;
/*TODO*///
/*TODO*///    public static SN76477interface schaser_sn76477_interface = new SN76477interface(
/*TODO*///            1, /* 1 chip */
/*TODO*///            new int[]{50}, /* mixing level   pin description		 */
/*TODO*///            new double[]{RES_K(47)}, /*	4  noise_res		 */
/*TODO*///            new double[]{RES_K(330)}, /*	5  filter_res		 */
/*TODO*///            new double[]{CAP_P(470)}, /*	6  filter_cap		 */
/*TODO*///            new double[]{RES_M(2.2)}, /*	7  decay_res		 */
/*TODO*///            new double[]{CAP_U(1.0)}, /*	8  attack_decay_cap  */
/*TODO*///            new double[]{RES_K(4.7)}, /* 10  attack_res		 */
/*TODO*///            new double[]{0}, /* 11  amplitude_res (variable)	 */
/*TODO*///            new double[]{RES_K(33)}, /* 12  feedback_res 	 */
/*TODO*///            new double[]{0}, /* 16  vco_voltage		 */
/*TODO*///            new double[]{CAP_U(0.1)}, /* 17  vco_cap			 */
/*TODO*///            new double[]{RES_K(39)}, /* 18  vco_res			 */
/*TODO*///            new double[]{5.0}, /* 19  pitch_voltage	 */
/*TODO*///            new double[]{RES_K(120)}, /* 20  slf_res			 */
/*TODO*///            new double[]{CAP_U(1.0)}, /* 21  slf_cap			 */
/*TODO*///            new double[]{0}, /* 23  oneshot_cap (variable) */
/*TODO*///            new double[]{RES_K(220)} /* 24  oneshot_res		 */
/*TODO*///    );
/*TODO*///
/*TODO*///    public static DACinterface schaser_dac_interface = new DACinterface(
/*TODO*///            1,
/*TODO*///            new int[]{50}
/*TODO*///    );
/*TODO*///
/*TODO*///    static short backgroundwave[]
/*TODO*///            = {
/*TODO*///                0x7fff, 0x7fff, 0x7fff, 0x7fff, 0x7fff, 0x7fff, 0x7fff, 0x7fff,
/*TODO*///                0x7fff, 0x7fff, 0x7fff, 0x7fff, 0x7fff, 0x7fff, 0x7fff, 0x7fff,
/*TODO*///                -0x8000, -0x8000, -0x8000, -0x8000, -0x8000, -0x8000, -0x8000, -0x8000,
/*TODO*///                -0x8000, -0x8000, -0x8000, -0x8000, -0x8000, -0x8000, -0x8000, -0x8000,};

    public static InitMachineHandlerPtr init_machine_schaser = new InitMachineHandlerPtr() {
        public void handler() {
            install_port_write_handler(0, 0x03, 0x03, schaser_sh_port3_w);
            install_port_write_handler(0, 0x05, 0x05, schaser_sh_port5_w);

/*TODO*///            SN76477_mixer_a_w(0, 0);
/*TODO*///            SN76477_mixer_c_w(0, 0);
/*TODO*///
/*TODO*///            SN76477_envelope_1_w(0, 1);
/*TODO*///            SN76477_envelope_2_w(0, 0);
        }
    };

    public static WriteHandlerPtr schaser_sh_port3_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int explosion;

            /* bit 0 - Dot Sound Enable (SX0)
		   bit 1 - Dot Sound Pitch (SX1)
		   bit 2 - Effect Sound A (SX2)
		   bit 3 - Effect Sound B (SX3)
		   bit 4 - Effect Sound C (SX4)
		   bit 5 - Explosion (SX5) */
            if (channel_dot != 0) {
                int freq;

                mixer_set_volume(channel_dot, (data & 0x01) != 0 ? 100 : 0);

                freq = 19968000 / 2 / 2 / (256 + 16) / ((data & 0x02) != 0 ? 8 : 4) / 2;
                mixer_set_sample_frequency(channel_dot, freq);
            }

            explosion = (data >> 5) & 0x01;
            if (explosion != 0) {
/*TODO*///                SN76477_set_amplitude_res(0, RES_K(200));
/*TODO*///                SN76477_set_oneshot_cap(0, CAP_U(0.1));
/*TODO*///                /* ???? */
            } else {
/*TODO*///                /* 68k and 200k resistors in parallel */
/*TODO*///                SN76477_set_amplitude_res(0, RES_K(1.0 / ((1.0 / 200.0) + (1.0 / 68.0))));
/*TODO*///                SN76477_set_oneshot_cap(0, CAP_U(0.1));
/*TODO*///                /* ???? */
            }
/*TODO*///            SN76477_enable_w(0, NOT(explosion));
/*TODO*///            SN76477_mixer_b_w(0, explosion);
        }
    };

    public static WriteHandlerPtr schaser_sh_port5_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bit 0 - Music (DAC) (SX6)
		   bit 1 - Sound Enable (SX7)
		   bit 2 - Coin Lockout (SX8)
		   bit 3 - Field Control A (SX9)
		   bit 4 - Field Control B (SX10)
		   bit 5 - Flip Screen */

/*TODO*///            DAC_data_w(0, (data & 0x01) != 0 ? 0xff : 0x00);

/*TODO*///            /*TODO*///		mixer_sound_enable_global_w(data & 0x02);
/*TODO*///            coin_lockout_global_w.handler(0, data & 0x04);

/*TODO*///            invaders_flip_screen_w(data & 0x20);
        }
    };

/*TODO*///    public static ShStartHandlerPtr schaser_sh_start = new ShStartHandlerPtr() {
/*TODO*///        public int handler(MachineSound msound) {
/*TODO*///            channel_dot = mixer_allocate_channel(50);
/*TODO*///            mixer_set_name(channel_dot, "Dot Sound");
/*TODO*///
/*TODO*///            mixer_set_volume(channel_dot, 0);
/*TODO*///            /*TODO*///		mixer_play_sample_16(channel_dot,new ShortPtr(backgroundwave),sizeof(backgroundwave),1000,1);
/*TODO*///
/*TODO*///            return 0;
/*TODO*///        }
/*TODO*///    };
/*TODO*///
/*TODO*///    public static ShStopHandlerPtr schaser_sh_stop = new ShStopHandlerPtr() {
/*TODO*///        public void handler() {
/*TODO*///            mixer_stop_sample(channel_dot);
/*TODO*///        }
/*TODO*///    };
/*TODO*///
/*TODO*///    public static ShUpdateHandlerPtr schaser_sh_update = new ShUpdateHandlerPtr() {
/*TODO*///        public void handler() {
/*TODO*///        }
/*TODO*///    };
/*TODO*///    public static CustomSound_interface schaser_custom_interface = new CustomSound_interface(
/*TODO*///            schaser_sh_start,
/*TODO*///            schaser_sh_stop,
/*TODO*///            schaser_sh_update
/*TODO*///    );
}
