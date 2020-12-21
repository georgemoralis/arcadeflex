/*
 * ported to v0.36
 * using automatic conversion tool v0.07
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.sndhrdw;

public class _8080bw
{
/*TODO*///	
/*TODO*///	void invaders_flipscreen_w(int data);
/*TODO*///	void invaders_screen_red_w(int data);
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///	   Note: For invad2ct, the Player 1 sounds are the same as for the
/*TODO*///	         original and deluxe versions.  Player 2 sounds are all
/*TODO*///	         different, and are triggered by writes to port 1 and port 7.
/*TODO*///	
/*TODO*///	*/
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr invad2ct_sh_port1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		static unsigned char Sound = 0;
/*TODO*///	
/*TODO*///		if (data & 0x01 && ~Sound & 0x01)
/*TODO*///			sample_start (7, 10, 1);		/* Saucer Sound - Player 2 */
/*TODO*///	
/*TODO*///		if (~data & 0x01 && Sound & 0x01)
/*TODO*///			sample_stop (7);
/*TODO*///	
/*TODO*///		if (data & 0x02 && ~Sound & 0x02)
/*TODO*///			sample_start (8, 11, 0);		/* Missle Sound - Player 2 */
/*TODO*///	
/*TODO*///		if (data & 0x04 && ~Sound & 0x04)
/*TODO*///			sample_start (9, 12, 0);		/* Explosion - Player 2 */
/*TODO*///	
/*TODO*///		if (data & 0x08 && ~Sound & 0x08)
/*TODO*///			sample_start (10, 13, 0);		/* Invader Hit - Player 2 */
/*TODO*///	
/*TODO*///		if (data & 0x10 && ~Sound & 0x10)
/*TODO*///			sample_start (4, 9, 0);		    /* Bonus Missle Base - Player 2 */
/*TODO*///	
/*TODO*///		Sound = data;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr invaders_sh_port3_w = new WriteHandlerPtr() { public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		static unsigned char Sound = 0;
/*TODO*///	
/*TODO*///		if (data & 0x01 && ~Sound & 0x01)
/*TODO*///			sample_start (0, 0, 1);
/*TODO*///	
/*TODO*///		if (~data & 0x01 && Sound & 0x01)
/*TODO*///			sample_stop (0);
/*TODO*///	
/*TODO*///		if (data & 0x02 && ~Sound & 0x02)
/*TODO*///			sample_start (1, 1, 0);
/*TODO*///	
/*TODO*///		if (data & 0x04 && ~Sound & 0x04)
/*TODO*///			sample_start (2, 2, 0);
/*TODO*///	
/*TODO*///		if (~data & 0x04 && Sound & 0x04)
/*TODO*///			sample_stop (2);
/*TODO*///	
/*TODO*///		if (data & 0x08 && ~Sound & 0x08)
/*TODO*///			sample_start (3, 3, 0);
/*TODO*///	
/*TODO*///		if (data & 0x10 && ~Sound & 0x10)
/*TODO*///			sample_start (4, 9, 0);
/*TODO*///	
/*TODO*///		invaders_screen_red_w(data & 0x04);
/*TODO*///	
/*TODO*///		Sound = data;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr invaders_sh_port5_w = new WriteHandlerPtr() { public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		static unsigned char Sound = 0;
/*TODO*///	
/*TODO*///		if (data & 0x01 && ~Sound & 0x01)
/*TODO*///			sample_start (5, 4, 0);			/* Fleet 1 */
/*TODO*///	
/*TODO*///		if (data & 0x02 && ~Sound & 0x02)
/*TODO*///			sample_start (5, 5, 0);			/* Fleet 2 */
/*TODO*///	
/*TODO*///		if (data & 0x04 && ~Sound & 0x04)
/*TODO*///			sample_start (5, 6, 0);			/* Fleet 3 */
/*TODO*///	
/*TODO*///		if (data & 0x08 && ~Sound & 0x08)
/*TODO*///			sample_start (5, 7, 0);			/* Fleet 4 */
/*TODO*///	
/*TODO*///		if (data & 0x10 && ~Sound & 0x10)
/*TODO*///			sample_start (6, 8, 0);			/* Saucer Hit */
/*TODO*///	
/*TODO*///		invaders_flipscreen_w(data & 0x20);
/*TODO*///	
/*TODO*///		Sound = data;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr invad2ct_sh_port7_w = new WriteHandlerPtr() { public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		static unsigned char Sound = 0;
/*TODO*///	
/*TODO*///		if (data & 0x01 && ~Sound & 0x01)
/*TODO*///			sample_start (11, 14, 0);		/* Fleet 1 - Player 2 */
/*TODO*///	
/*TODO*///		if (data & 0x02 && ~Sound & 0x02)
/*TODO*///			sample_start (11, 15, 0);		/* Fleet 2 - Player 2 */
/*TODO*///	
/*TODO*///		if (data & 0x04 && ~Sound & 0x04)
/*TODO*///			sample_start (11, 16, 0);		/* Fleet 3 - Player 2 */
/*TODO*///	
/*TODO*///		if (data & 0x08 && ~Sound & 0x08)
/*TODO*///			sample_start (11, 17, 0);		/* Fleet 4 - Player 2 */
/*TODO*///	
/*TODO*///		if (data & 0x10 && ~Sound & 0x10)
/*TODO*///			sample_start (12, 18, 0);		/* Saucer Hit - Player 2 */
/*TODO*///	
/*TODO*///		invaders_flipscreen_w(data & 0x20);
/*TODO*///	
/*TODO*///		Sound = data;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	/* HC 4/14/98 NOTE: *I* THINK there are sounds missing...
/*TODO*///	i dont know for sure... but that is my guess....... */
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr boothill_sh_port3_w = new WriteHandlerPtr() { public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		switch (data)
/*TODO*///		{
/*TODO*///			case 0x0c:
/*TODO*///				sample_start (0, 0, 0);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x18:
/*TODO*///			case 0x28:
/*TODO*///				sample_start (1, 2, 0);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x48:
/*TODO*///			case 0x88:
/*TODO*///				sample_start (2, 3, 0);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/* HC 4/14/98 */
/*TODO*///	public static WriteHandlerPtr boothill_sh_port5_w = new WriteHandlerPtr() { public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		switch (data)
/*TODO*///		{
/*TODO*///			case 0x3b:
/*TODO*///				sample_start (2, 1, 0);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************/
/*TODO*///	/*                                                     */
/*TODO*///	/* Taito "Balloon Bomber"                              */
/*TODO*///	/*                                                     */
/*TODO*///	/*******************************************************/
/*TODO*///	
/*TODO*///	/* This only does the colour swap for the explosion */
/*TODO*///	/* We do not have correct samples so sound not done */
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr ballbomb_sh_port3_w = new WriteHandlerPtr() { public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		invaders_screen_red_w(data & 0x04);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	/*******************************************************/
/*TODO*///	/*                                                     */
/*TODO*///	/* Exidy "Bandido"                              	   */
/*TODO*///	/*                                                     */
/*TODO*///	/*******************************************************/
/*TODO*///	
/*TODO*///	static void bandido_74123_0_output_changed_cb(void)
/*TODO*///	{
/*TODO*///		SN76477_vco_w    (0,  TTL74123_output_r(0));
/*TODO*///		SN76477_mixer_a_w(0, !TTL74123_output_r(0));
/*TODO*///	
/*TODO*///		SN76477_enable_w(0, TTL74123_output_comp_r(0) && TTL74123_output_comp_r(1));
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void bandido_74123_1_output_changed_cb(void)
/*TODO*///	{
/*TODO*///		SN76477_set_vco_voltage(0, !TTL74123_output_comp_r(1) ? 5.0 : 0.0);
/*TODO*///	
/*TODO*///		SN76477_enable_w(0, TTL74123_output_comp_r(0) && TTL74123_output_comp_r(1));
/*TODO*///	}
/*TODO*///	
/*TODO*///	static struct TTL74123_interface bandido_74123_0_intf =
/*TODO*///	{
/*TODO*///		RES_K(33),
/*TODO*///		CAP_U(33),
/*TODO*///		bandido_74123_0_output_changed_cb
/*TODO*///	};
/*TODO*///	
/*TODO*///	static struct TTL74123_interface bandido_74123_1_intf =
/*TODO*///	{
/*TODO*///		RES_K(33),
/*TODO*///		CAP_U(33),
/*TODO*///		bandido_74123_1_output_changed_cb
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	void init_machine_bandido(void)
/*TODO*///	{
/*TODO*///		TTL74123_config(0, &bandido_74123_0_intf);
/*TODO*///		TTL74123_config(1, &bandido_74123_1_intf);
/*TODO*///	
/*TODO*///		/* set up the fixed connections */
/*TODO*///		TTL74123_reset_comp_w  (0, 1);
/*TODO*///		TTL74123_trigger_comp_w(0, 0);
/*TODO*///	
/*TODO*///		TTL74123_trigger_comp_w(1, 0);
/*TODO*///	
/*TODO*///		SN76477_envelope_1_w(0, 1);
/*TODO*///		SN76477_envelope_2_w(0, 0);
/*TODO*///		SN76477_noise_clock_w(0, 0);
/*TODO*///		SN76477_mixer_b_w(0, 0);
/*TODO*///		SN76477_mixer_c_w(0, 0);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static int bandido_t0,bandido_t1,bandido_p1,bandido_p2;
/*TODO*///	
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr bandido_sh_port4_w = new WriteHandlerPtr() { public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		bandido_t0 = data & 1;
/*TODO*///	
/*TODO*///		bandido_p1 = (bandido_p1 & 0x4f) |
/*TODO*///					 ((data & 0x02) << 3) |		/* P1.4 */
/*TODO*///					 ((data & 0x08) << 2) |		/* P1.5 */
/*TODO*///					 ((data & 0x20) << 2);		/* P1.7 */
/*TODO*///	
/*TODO*///		cpu_set_irq_line(1, I8035_EXT_INT, ((bandido_p1 & 0x70) == 0x70) ? ASSERT_LINE : CLEAR_LINE);
/*TODO*///	
/*TODO*///	
/*TODO*///		TTL74123_trigger_w   (0, data & 0x04);
/*TODO*///	
/*TODO*///		TTL74123_trigger_w   (1, data & 0x10);
/*TODO*///		TTL74123_reset_comp_w(1, data & 0x04);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr bandido_sh_port5_w = new WriteHandlerPtr() { public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		bandido_t1 = (data >> 5) & 1;
/*TODO*///	
/*TODO*///		bandido_p1 = (bandido_p1 & 0xb0) |
/*TODO*///					 ((data & 0x01) << 3) |		/* P1.3 */
/*TODO*///					 ((data & 0x02) << 1) |		/* P1.2 */
/*TODO*///					 ((data & 0x04) >> 1) |		/* P1.1 */
/*TODO*///					 ((data & 0x08) >> 3) |		/* P1.0 */
/*TODO*///					 ((data & 0x10) << 2);		/* P1.6 */
/*TODO*///	
/*TODO*///		cpu_set_irq_line(1, I8035_EXT_INT, ((bandido_p1 & 0x70) == 0x70) ? ASSERT_LINE : CLEAR_LINE);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr bandido_sh_gett0 = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		return bandido_t0;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr bandido_sh_gett1 = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		return bandido_t1;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr bandido_sh_getp1 = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		return bandido_p1;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr bandido_sh_getp2 = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		return bandido_p2;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr bandido_sh_putp2 = new WriteHandlerPtr() { public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		bandido_p2 = data;
/*TODO*///	
/*TODO*///		DAC_data_w(0, bandido_p2 & 0x80 ? 0xff : 0x00);
/*TODO*///	} };
}
