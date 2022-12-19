/* 8080bw.c *********************************
 updated: 1997-04-09 08:46 TT
 updated  20-3-1998 LT Added color changes on base explosion
 *
 * Author      : Tormod Tjaberg
 * Created     : 1997-04-09
 * Description : Sound routines for the 'invaders' games
 *
 * Note:
 * The samples were taken from Michael Strutt's (mstrutt@pixie.co.za)
 * excellent space invader emulator and converted to signed samples so
 * they would work under SEAL. The port info was also gleaned from
 * his emulator. These sounds should also work on all the invader games.
 *
 * The sounds are generated using output port 3 and 5
 *
 * Port 3:
 * bit 0=UFO  (repeats)       emulated
 * bit 1=Shot                 1.raw
 * bit 2=Base hit             2.raw
 * bit 3=Invader hit          3.raw
 * bit 4=Bonus base           9.raw
 *
 * Port 5:
 * bit 0=Fleet movement 1     4.raw
 * bit 1=Fleet movement 2     5.raw
 * bit 2=Fleet movement 3     6.raw
 * bit 3=Fleet movement 4     7.raw
 * bit 4=UFO 2                8.raw
 */

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package sndhrdw;

public class _8080bw
{
	
	void invaders_flip_screen_w(int data);
	void invaders_screen_red_w(int data);
	
	
	
	
	
	
	
	
	
	static static static 
	
	
	static SN76477interface invaders_sn76477_interface = new SN76477interface
	(
		1,	/* 1 chip */
		new int[] { 25 },  /* mixing level   pin description		 */
		new double[] { 0	/* N/C */},		/*	4  noise_res		 */
		new double[] { 0	/* N/C */},		/*	5  filter_res		 */
		new double[] { 0	/* N/C */},		/*	6  filter_cap		 */
		new double[] { 0	/* N/C */},		/*	7  decay_res		 */
		new double[] { 0	/* N/C */},		/*	8  attack_decay_cap  */
		new double[] { RES_K(100) },		/* 10  attack_res		 */
		new double[] { RES_K(56)  },		/* 11  amplitude_res	 */
		new double[] { RES_K(10)  },		/* 12  feedback_res 	 */
		new double[] { 0	/* N/C */},		/* 16  vco_voltage		 */
		new double[] { CAP_U(0.1) },		/* 17  vco_cap			 */
		new double[] { RES_K(8.2) },		/* 18  vco_res			 */
		new double[] { 5.0		 },		/* 19  pitch_voltage	 */
		new double[] { RES_K(120) },		/* 20  slf_res			 */
		new double[] { CAP_U(1.0) },		/* 21  slf_cap			 */
		new double[] { 0	/* N/C */},		/* 23  oneshot_cap		 */
		new double[] { 0	/* N/C */}		/* 24  oneshot_res		 */
	);
	
	static const char *invaders_sample_names[] =
	{
		"*invaders",
		"1.wav",	/* Shot/Missle */
		"2.wav",	/* Base Hit/Explosion */
		"3.wav",	/* Invader Hit */
		"4.wav",	/* Fleet move 1 */
		"5.wav",	/* Fleet move 2 */
		"6.wav",	/* Fleet move 3 */
		"7.wav",	/* Fleet move 4 */
		"8.wav",	/* UFO/Saucer Hit */
		"9.wav",	/* Bonus Base */
		0       /* end of array */
	};
	
	static Samplesinterface invaders_samples_interface = new Samplesinterface
	(
		4,	/* 4 channels */
		25,	/* volume */
		invaders_sample_names
	);
	
	
	static SN76477interface invad2ct_sn76477_interface = new SN76477interface
	(
		2,	/* 2 chips */
		new int[] { 25,         25 },  /* mixing level   pin description		 */
		new double[] { 0,          0	/* N/C */  },	/*	4  noise_res		 */
		new double[] { 0,          0	/* N/C */  },	/*	5  filter_res		 */
		new double[] { 0,          0	/* N/C */  },	/*	6  filter_cap		 */
		new double[] { 0,          0	/* N/C */  },	/*	7  decay_res		 */
		new double[] { 0,          0	/* N/C */  },	/*	8  attack_decay_cap  */
		new double[] { RES_K(100), RES_K(100)   },	/* 10  attack_res		 */
		new double[] { RES_K(56),  RES_K(56)    },	/* 11  amplitude_res	 */
		new double[] { RES_K(10),  RES_K(10)    },	/* 12  feedback_res 	 */
		new double[] { 0,          0	/* N/C */  },	/* 16  vco_voltage		 */
		new double[] { CAP_U(0.1), CAP_U(0.047) },	/* 17  vco_cap			 */
		new double[] { RES_K(8.2), RES_K(39)    },	/* 18  vco_res			 */
		new double[] { 5.0,        5.0		   },	/* 19  pitch_voltage	 */
		new double[] { RES_K(120), RES_K(120)   },	/* 20  slf_res			 */
		new double[] { CAP_U(1.0), CAP_U(1.0)   },	/* 21  slf_cap			 */
		new double[] { 0,          0	/* N/C */  },	/* 23  oneshot_cap		 */
		new double[] { 0,          0	/* N/C */  }	/* 24  oneshot_res		 */
	);
	
	static const char *invad2ct_sample_names[] =
	{
		"*invaders",
		"1.wav",	/* Shot/Missle - Player 1 */
		"2.wav",	/* Base Hit/Explosion - Player 1 */
		"3.wav",	/* Invader Hit - Player 1 */
		"4.wav",	/* Fleet move 1 - Player 1 */
		"5.wav",	/* Fleet move 2 - Player 1 */
		"6.wav",	/* Fleet move 3 - Player 1 */
		"7.wav",	/* Fleet move 4 - Player 1 */
		"8.wav",	/* UFO/Saucer Hit - Player 1 */
		"9.wav",	/* Bonus Base - Player 1 */
		"11.wav",	/* Shot/Missle - Player 2 */
		"12.wav",	/* Base Hit/Explosion - Player 2 */
		"13.wav",	/* Invader Hit - Player 2 */
		"14.wav",	/* Fleet move 1 - Player 2 */
		"15.wav",	/* Fleet move 2 - Player 2 */
		"16.wav",	/* Fleet move 3 - Player 2 */
		"17.wav",	/* Fleet move 4 - Player 2 */
		"18.wav",	/* UFO/Saucer Hit - Player 2 */
		0       /* end of array */
	};
	
	static Samplesinterface invad2ct_samples_interface = new Samplesinterface
	(
		8,	/* 8 channels */
		25,	/* volume */
		invad2ct_sample_names
	);
	
	
	public static InitMachinePtr init_machine_invaders = new InitMachinePtr() { public void handler() 
	{
		install_port_write_handler(0, 0x03, 0x03, invaders_sh_port3_w);
		install_port_write_handler(0, 0x05, 0x05, invaders_sh_port5_w);
	
		SN76477_envelope_1_w(0, 1);
		SN76477_envelope_2_w(0, 0);
		SN76477_mixer_a_w(0, 0);
		SN76477_mixer_b_w(0, 0);
		SN76477_mixer_c_w(0, 0);
		SN76477_vco_w(0, 1);
	} };
	
	public static InitMachinePtr init_machine_invad2ct = new InitMachinePtr() { public void handler() 
	{
		init_machine_invaders();
	
		install_port_write_handler(0, 0x01, 0x01, invad2ct_sh_port1_w);
		install_port_write_handler(0, 0x07, 0x07, invad2ct_sh_port7_w);
	
		SN76477_envelope_1_w(1, 1);
		SN76477_envelope_2_w(1, 0);
		SN76477_mixer_a_w(1, 0);
		SN76477_mixer_b_w(1, 0);
		SN76477_mixer_c_w(1, 0);
		SN76477_vco_w(1, 1);
	} };
	
	
	/*
	   Note: For invad2ct, the Player 1 sounds are the same as for the
	         original and deluxe versions.  Player 2 sounds are all
	         different, and are triggered by writes to port 1 and port 7.
	
	*/
	
	static void invaders_sh_1_w(int board, int data, UBytePtr last)
	{
		int base_channel, base_sample;
	
		base_channel = 4 * board;
		base_sample  = 9 * board;
	
		SN76477_enable_w(board, !(data & 0x01));				/* Saucer Sound */
	
		if (data & 0x02 && ~*last & 0x02)
			sample_start (base_channel+0, base_sample+0, 0);	/* Shot Sound */
	
		if (data & 0x04 && ~*last & 0x04)
			sample_start (base_channel+1, base_sample+1, 0);	/* Base Hit */
	
		if (~data & 0x04 && *last & 0x04)
			sample_stop (base_channel+1);
	
		if (data & 0x08 && ~*last & 0x08)
			sample_start (base_channel+0, base_sample+2, 0);	/* Invader Hit */
	
		if (data & 0x10 && ~*last & 0x10)
			sample_start (base_channel+2, 8, 0);				/* Bonus Missle Base */
	
		invaders_screen_red_w(data & 0x04);
	
		*last = data;
	}
	
	static void invaders_sh_2_w(int board, int data, UBytePtr last)
	{
		int base_channel, base_sample;
	
		base_channel = 4 * board;
		base_sample  = 9 * board;
	
		if (data & 0x01 && ~*last & 0x01)
			sample_start (base_channel+1, base_sample+3, 0);	/* Fleet 1 */
	
		if (data & 0x02 && ~*last & 0x02)
			sample_start (base_channel+1, base_sample+4, 0);	/* Fleet 2 */
	
		if (data & 0x04 && ~*last & 0x04)
			sample_start (base_channel+1, base_sample+5, 0);	/* Fleet 3 */
	
		if (data & 0x08 && ~*last & 0x08)
			sample_start (base_channel+1, base_sample+6, 0);	/* Fleet 4 */
	
		if (data & 0x10 && ~*last & 0x10)
			sample_start (base_channel+3, base_sample+7, 0);	/* Saucer Hit */
	
		invaders_flip_screen_w(data & 0x20);
	
		*last = data;
	}
	
	
	public static WriteHandlerPtr invad2ct_sh_port1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static unsigned char last = 0;
	
		invaders_sh_1_w(1, data, &last);
	} };
	
	public static WriteHandlerPtr invaders_sh_port3_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static unsigned char last = 0;
	
		invaders_sh_1_w(0, data, &last);
	} };
	
	public static WriteHandlerPtr invaders_sh_port5_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static unsigned char last = 0;
	
		invaders_sh_2_w(0, data, &last);
	} };
	
	public static WriteHandlerPtr invad2ct_sh_port7_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static unsigned char last = 0;
	
		invaders_sh_2_w(1, data, &last);
	} };
	
	
	/*******************************************************/
	/*                                                     */
	/* Midway "Gun Fight"                                  */
	/*                                                     */
	/*******************************************************/
	
	public static InitMachinePtr init_machine_gunfight = new InitMachinePtr() { public void handler() 
	{
		install_port_read_handler(0, 0x00, 0x00, gunfight_port_0_r);
		install_port_read_handler(0, 0x01, 0x01, gunfight_port_1_r);
	} };
	
	
	/*******************************************************/
	/*                                                     */
	/* Midway "Boot Hill"                                  */
	/*                                                     */
	/*******************************************************/
	
	static const char *boothill_sample_names[] =
	{
		"*boothill", /* in case we ever find any bootlegs hehehe */
		"addcoin.wav",
		"endgame.wav",
		"gunshot.wav",
		"killed.wav",
		0       /* end of array */
	};
	
	static Samplesinterface boothill_samples_interface = new Samplesinterface
	(
		9,	/* 9 channels */
		25,	/* volume */
		boothill_sample_names
	);
	
	
	/* HC 4/14/98 NOTE: *I* THINK there are sounds missing...
	i dont know for sure... but that is my guess....... */
	
	public static InitMachinePtr init_machine_boothill = new InitMachinePtr() { public void handler() 
	{
		install_port_read_handler (0, 0x00, 0x00, boothill_port_0_r);
		install_port_read_handler (0, 0x01, 0x01, boothill_port_1_r);
	
		install_port_write_handler(0, 0x03, 0x03, boothill_sh_port3_w);
		install_port_write_handler(0, 0x05, 0x05, boothill_sh_port5_w);
	} };
	
	public static WriteHandlerPtr boothill_sh_port3_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch (data)
		{
			case 0x0c:
				sample_start (0, 0, 0);
				break;
	
			case 0x18:
			case 0x28:
				sample_start (1, 2, 0);
				break;
	
			case 0x48:
			case 0x88:
				sample_start (2, 3, 0);
				break;
		}
	} };
	
	/* HC 4/14/98 */
	public static WriteHandlerPtr boothill_sh_port5_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch (data)
		{
			case 0x3b:
				sample_start (2, 1, 0);
				break;
		}
	} };
	
	
	/*******************************************************/
	/*                                                     */
	/* Taito "Balloon Bomber"                              */
	/*                                                     */
	/*******************************************************/
	
	/* This only does the color swap for the explosion */
	/* We do not have correct samples so sound not done */
	
	public static InitMachinePtr init_machine_ballbomb = new InitMachinePtr() { public void handler() 
	{
		install_port_write_handler(0, 0x03, 0x03, ballbomb_sh_port3_w);
		install_port_write_handler(0, 0x05, 0x05, ballbomb_sh_port5_w);
	} };
	
	public static WriteHandlerPtr ballbomb_sh_port3_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		invaders_screen_red_w(data & 0x04);
	} };
	
	public static WriteHandlerPtr ballbomb_sh_port5_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		invaders_flip_screen_w(data & 0x20);
	} };
	
	
	/*******************************************************/
	/*                                                     */
	/* Taito "Polaris"		                               */
	/*                                                     */
	/*******************************************************/
	
	public static InitMachinePtr init_machine_polaris = new InitMachinePtr() { public void handler() 
	{
		install_port_write_handler(0, 0x06, 0x06, polaris_sh_port6_w);
	} };
	
	public static WriteHandlerPtr polaris_sh_port6_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		coin_lockout_global_w(0, data & 0x04);
	
		invaders_flip_screen_w(data & 0x20);
	} };
	
	
	/*******************************************************/
	/*                                                     */
	/* Nintendo "Sheriff"                              	   */
	/*                                                     */
	/*******************************************************/
	
	static DACinterface sheriff_dac_interface = new DACinterface
	(
		1,
		new int[] { 50 }
	);
	
	static SN76477interface sheriff_sn76477_interface = new SN76477interface
	(
		1,	/* 1 chip */
		new int[] { 50 },  /* mixing level   pin description		 */
		new double[] { RES_K( 36)   },		/*	4  noise_res		 */
		new double[] { RES_K(100)   },		/*	5  filter_res		 */
		new double[] { CAP_U(0.001) },		/*	6  filter_cap		 */
		new double[] { RES_K(620)   },		/*	7  decay_res		 */
		new double[] { CAP_U(1.0)   },		/*	8  attack_decay_cap  */
		new double[] { RES_K(20)    },		/* 10  attack_res		 */
		new double[] { RES_K(150)   },		/* 11  amplitude_res	 */
		new double[] { RES_K(47)    },		/* 12  feedback_res 	 */
		new double[] { 0            },		/* 16  vco_voltage		 */
		new double[] { CAP_U(0.001) },		/* 17  vco_cap			 */
		new double[] { RES_M(1.5)   },		/* 18  vco_res			 */
		new double[] { 0.0		   },		/* 19  pitch_voltage	 */
		new double[] { RES_M(1.5)   },		/* 20  slf_res			 */
		new double[] { CAP_U(0.047) },		/* 21  slf_cap			 */
		new double[] { CAP_U(0.047) },		/* 23  oneshot_cap		 */
		new double[] { RES_K(560)   }		/* 24  oneshot_res		 */
	);
	
	
	static void sheriff_74123_0_output_changed_cb(void)
	{
		SN76477_vco_w    (0,  TTL74123_output_r(0));
		SN76477_mixer_b_w(0, !TTL74123_output_r(0));
	
		SN76477_enable_w(0, TTL74123_output_comp_r(0) && TTL74123_output_comp_r(1));
	}
	
	static void sheriff_74123_1_output_changed_cb(void)
	{
		SN76477_set_vco_voltage(0, !TTL74123_output_comp_r(1) ? 5.0 : 0.0);
	
		SN76477_enable_w(0, TTL74123_output_comp_r(0) && TTL74123_output_comp_r(1));
	}
	
	static struct TTL74123_interface sheriff_74123_0_intf =
	{
		RES_K(33),
		CAP_U(33),
		sheriff_74123_0_output_changed_cb
	};
	
	static struct TTL74123_interface sheriff_74123_1_intf =
	{
		RES_K(33),
		CAP_U(33),
		sheriff_74123_1_output_changed_cb
	};
	
	
	public static InitMachinePtr init_machine_sheriff = new InitMachinePtr() { public void handler() 
	{
		install_port_write_handler(0, 0x04, 0x04, sheriff_sh_port4_w);
		install_port_write_handler(0, 0x05, 0x05, sheriff_sh_port5_w);
		install_port_write_handler(0, 0x06, 0x06, sheriff_sh_port6_w);
	
		TTL74123_config(0, &sheriff_74123_0_intf);
		TTL74123_config(1, &sheriff_74123_1_intf);
	
		/* set up the fixed connections */
		TTL74123_reset_comp_w  (0, 1);
		TTL74123_trigger_comp_w(0, 0);
	
		TTL74123_trigger_comp_w(1, 0);
	
		SN76477_envelope_1_w(0, 1);
		SN76477_envelope_2_w(0, 0);
		SN76477_noise_clock_w(0, 0);
		SN76477_mixer_a_w(0, 0);
		SN76477_mixer_c_w(0, 0);
	} };
	
	
	static int sheriff_t0,sheriff_t1,sheriff_p1,sheriff_p2;
	
	
	public static WriteHandlerPtr sheriff_sh_port4_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		sheriff_t0 = data & 1;
	
		sheriff_p1 = (sheriff_p1 & 0x4f) |
					 ((data & 0x02) << 3) |		/* P1.4 */
					 ((data & 0x08) << 2) |		/* P1.5 */
					 ((data & 0x20) << 2);		/* P1.7 */
	
		soundlatch_w.handler(0, sheriff_p1);
	
		cpu_set_irq_line(1, I8035_EXT_INT, ((sheriff_p1 & 0x70) == 0x70) ? ASSERT_LINE : CLEAR_LINE);
	
		TTL74123_trigger_w   (0, data & 0x04);
	
		TTL74123_reset_comp_w(1, data & 0x04);
		TTL74123_trigger_w   (1, data & 0x10);
	} };
	
	public static WriteHandlerPtr sheriff_sh_port5_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		sheriff_t1 = (data >> 5) & 1;
	
		sheriff_p1 = (sheriff_p1 & 0xb0) |
					 ((data & 0x01) << 3) |		/* P1.3 */
					 ((data & 0x02) << 1) |		/* P1.2 */
					 ((data & 0x04) >> 1) |		/* P1.1 */
					 ((data & 0x08) >> 3) |		/* P1.0 */
					 ((data & 0x10) << 2);		/* P1.6 */
	
		soundlatch_w.handler(0, sheriff_p1);
	
		cpu_set_irq_line(1, I8035_EXT_INT, ((sheriff_p1 & 0x70) == 0x70) ? ASSERT_LINE : CLEAR_LINE);
	} };
	
	public static WriteHandlerPtr sheriff_sh_port6_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		flip_screen_w(offset, data & 0x20);
	} };
	
	
	public static ReadHandlerPtr sheriff_sh_t0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return sheriff_t0;
	} };
	
	public static ReadHandlerPtr sheriff_sh_t1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return sheriff_t1;
	} };
	
	public static ReadHandlerPtr sheriff_sh_p1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return soundlatch_r(0);;
	} };
	
	public static ReadHandlerPtr sheriff_sh_p2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return sheriff_p2;
	} };
	
	public static WriteHandlerPtr sheriff_sh_p2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		sheriff_p2 = data;
	
		DAC_data_w(0, sheriff_p2 & 0x80 ? 0xff : 0x00);
	} };
	
	
	/*******************************************************/
	/*                                                     */
	/* Nintendo "HeliFire"		                           */
	/*                                                     */
	/*******************************************************/
	
	public static InitMachinePtr init_machine_helifire = new InitMachinePtr() { public void handler() 
	{
		install_port_write_handler(0, 0x06, 0x06, helifire_sh_port6_w);
	} };
	
	public static WriteHandlerPtr helifire_sh_port6_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		flip_screen_w(offset, data & 0x20);
	} };
	
	
	/*******************************************************/
	/*                                                     */
	/* Midway "Sea Wolf"                                   */
	/*                                                     */
	/*******************************************************/
	
	public static InitMachinePtr init_machine_seawolf = new InitMachinePtr() { public void handler() 
	{
		install_port_read_handler (0, 0x01, 0x01, seawolf_port_1_r);
	} };
	
	
	/*******************************************************/
	/*                                                     */
	/* Midway "Desert Gun"                                 */
	/*                                                     */
	/*******************************************************/
	
	public static InitMachinePtr init_machine_desertgu = new InitMachinePtr() { public void handler() 
	{
		install_port_read_handler (0, 0x01, 0x01, desertgu_port_1_r);
	
		install_port_write_handler(0, 0x07, 0x07, desertgu_controller_select_w);
	} };
	
	
	/*******************************************************/
	/*                                                     */
	/* Taito "Space Chaser" 							   */
	/*                                                     */
	/*******************************************************/
	
	/*
	 *  The dot sound is a square wave clocked by either the
	 *  the 8V or 4V signals
	 *
	 *  The frequencies are (for the 8V signal):
	 *
	 *  19.968 MHz crystal / 2 (Qa of 74160 #10) . 9.984MHz
	 *					   / 2 (7474 #14) . 4.992MHz
	 *					   / 256+16 (74161 #5 and #8) . 18352.94Hz
	 *					   / 8 (8V) . 2294.12 Hz
	 * 					   / 2 the final freq. is 2 toggles . 1147.06Hz
	 *
	 *  for 4V, it's double at 2294.12Hz
	 */
	
	static int channel_dot;
	
	static SN76477interface schaser_sn76477_interface = new SN76477interface
	(
		1,	/* 1 chip */
		new int[] { 50 },  /* mixing level   pin description		 */
		new double[] { RES_K( 47)   },		/*	4  noise_res		 */
		new double[] { RES_K(330)   },		/*	5  filter_res		 */
		new double[] { CAP_P(470)   },		/*	6  filter_cap		 */
		new double[] { RES_M(2.2)   },		/*	7  decay_res		 */
		new double[] { CAP_U(1.0)   },		/*	8  attack_decay_cap  */
		new double[] { RES_K(4.7)   },		/* 10  attack_res		 */
		new double[] { 0			   },		/* 11  amplitude_res (variable)	 */
		new double[] { RES_K(33)    },		/* 12  feedback_res 	 */
		new double[] { 0            },		/* 16  vco_voltage		 */
		new double[] { CAP_U(0.1)   },		/* 17  vco_cap			 */
		new double[] { RES_K(39)    },		/* 18  vco_res			 */
		new double[] { 5.0		   },		/* 19  pitch_voltage	 */
		new double[] { RES_K(120)   },		/* 20  slf_res			 */
		new double[] { CAP_U(1.0)   },		/* 21  slf_cap			 */
		new double[] { 0 		   },		/* 23  oneshot_cap (variable) */
		new double[] { RES_K(220)   }		/* 24  oneshot_res		 */
	);
	
	static DACinterface schaser_dac_interface = new DACinterface
	(
		1,
		new int[] { 50 }
	);
	
	static CustomSound_interface schaser_custom_interface = new CustomSound_interface
	(
		schaser_sh_start,
		schaser_sh_stop,
		schaser_sh_update
	);
	
	static INT16 backgroundwave[32] =
	{
	    0x7fff, 0x7fff, 0x7fff, 0x7fff, 0x7fff, 0x7fff, 0x7fff, 0x7fff,
	    0x7fff, 0x7fff, 0x7fff, 0x7fff, 0x7fff, 0x7fff, 0x7fff, 0x7fff,
	   -0x8000,-0x8000,-0x8000,-0x8000,-0x8000,-0x8000,-0x8000,-0x8000,
	   -0x8000,-0x8000,-0x8000,-0x8000,-0x8000,-0x8000,-0x8000,-0x8000,
	};
	
	public static InitMachinePtr init_machine_schaser = new InitMachinePtr() { public void handler() 
	{
		install_port_write_handler(0, 0x03, 0x03, schaser_sh_port3_w);
		install_port_write_handler(0, 0x05, 0x05, schaser_sh_port5_w);
	
		SN76477_mixer_a_w(0, 0);
		SN76477_mixer_c_w(0, 0);
	
		SN76477_envelope_1_w(0, 1);
		SN76477_envelope_2_w(0, 0);
	} };
	
	public static WriteHandlerPtr schaser_sh_port3_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int explosion;
	
		/* bit 0 - Dot Sound Enable (SX0)
		   bit 1 - Dot Sound Pitch (SX1)
		   bit 2 - Effect Sound A (SX2)
		   bit 3 - Effect Sound B (SX3)
		   bit 4 - Effect Sound C (SX4)
		   bit 5 - Explosion (SX5) */
	
		if (channel_dot != 0)
		{
			int freq;
	
			mixer_set_volume(channel_dot, (data & 0x01) ? 100 : 0);
	
			freq = 19968000 / 2 / 2 / (256+16) / ((data & 0x02) ? 8 : 4) / 2;
			mixer_set_sample_frequency(channel_dot, freq);
		}
	
		explosion = (data >> 5) & 0x01;
		if (explosion != 0)
		{
			SN76477_set_amplitude_res(0, RES_K(200));
			SN76477_set_oneshot_cap(0, CAP_U(0.1));		/* ???? */
		}
		else
		{
			/* 68k and 200k resistors in parallel */
			SN76477_set_amplitude_res(0, RES_K(1.0/((1.0/200.0)+(1.0/68.0))));
			SN76477_set_oneshot_cap(0, CAP_U(0.1));		/* ???? */
		}
		SN76477_enable_w(0, !explosion);
		SN76477_mixer_b_w(0, explosion);
	} };
	
	public static WriteHandlerPtr schaser_sh_port5_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* bit 0 - Music (DAC) (SX6)
		   bit 1 - Sound Enable (SX7)
		   bit 2 - Coin Lockout (SX8)
		   bit 3 - Field Control A (SX9)
		   bit 4 - Field Control B (SX10)
		   bit 5 - Flip Screen */
	
		DAC_data_w(0, data & 0x01 ? 0xff : 0x00);
	
		mixer_sound_enable_global_w(data & 0x02);
	
		coin_lockout_global_w(0, data & 0x04);
	
		invaders_flip_screen_w(data & 0x20);
	} };
	
	static public static ShStartPtr schaser_sh_start = new ShStartPtr() { public int handler(MachineSound msound) 
	{
		channel_dot = mixer_allocate_channel(50);
		mixer_set_name(channel_dot,"Dot Sound");
	
		mixer_set_volume(channel_dot,0);
		mixer_play_sample_16(channel_dot,backgroundwave,sizeof(backgroundwave),1000,1);
	
		return 0;
	} };
	
	static public static ShStopPtr schaser_sh_stop = new ShStopPtr() { public void handler() 
	{
		mixer_stop_sample(channel_dot);
	} };
	
	static public static ShUpdatePtr schaser_sh_update = new ShUpdatePtr() { public void handler() 
	{
	} };
}
