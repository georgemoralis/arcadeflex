/* 8080bw.c *********************************
 updated: 1997-04-09 08:46 TT
 updated  20-3-1998 LT Added colour changes on base explosion
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
 * Port 4:
 * bit 0=UFO  (repeats)       0.raw
 * bit 1=Shot                 1.raw
 * bit 2=Base hit             2.raw
 * bit 3=Invader hit          3.raw
 *
 * Port 5:
 * bit 0=Fleet movement 1     4.raw
 * bit 1=Fleet movement 2     5.raw
 * bit 2=Fleet movement 3     6.raw
 * bit 3=Fleet movement 4     7.raw
 * bit 4=UFO 2                8.raw
 */

#include "driver.h"
#include "cpu/i8039/i8039.h"
#include "machine/74123.h"

void invaders_flipscreen_w(int data);
void invaders_screen_red_w(int data);


/*
   Note: For invad2ct, the Player 1 sounds are the same as for the
         original and deluxe versions.  Player 2 sounds are all
         different, and are triggered by writes to port 1 and port 7.

*/

void invad2ct_sh_port1_w(int offset, int data)
{
	static unsigned char Sound = 0;

	if (data & 0x01 && ~Sound & 0x01)
		sample_start (7, 10, 1);		/* Saucer Sound - Player 2 */

	if (~data & 0x01 && Sound & 0x01)
		sample_stop (7);

	if (data & 0x02 && ~Sound & 0x02)
		sample_start (8, 11, 0);		/* Missle Sound - Player 2 */

	if (data & 0x04 && ~Sound & 0x04)
		sample_start (9, 12, 0);		/* Explosion - Player 2 */

	if (data & 0x08 && ~Sound & 0x08)
		sample_start (10, 13, 0);		/* Invader Hit - Player 2 */

	if (data & 0x10 && ~Sound & 0x10)
		sample_start (4, 9, 0);		    /* Bonus Missle Base - Player 2 */

	Sound = data;
}

void invaders_sh_port3_w(int offset, int data)
{
	static unsigned char Sound = 0;

	if (data & 0x01 && ~Sound & 0x01)
		sample_start (0, 0, 1);

	if (~data & 0x01 && Sound & 0x01)
		sample_stop (0);

	if (data & 0x02 && ~Sound & 0x02)
		sample_start (1, 1, 0);

	if (data & 0x04 && ~Sound & 0x04)
		sample_start (2, 2, 0);

	if (~data & 0x04 && Sound & 0x04)
		sample_stop (2);

	if (data & 0x08 && ~Sound & 0x08)
		sample_start (3, 3, 0);

	if (data & 0x10 && ~Sound & 0x10)
		sample_start (4, 9, 0);

	invaders_screen_red_w(data & 0x04);

	Sound = data;
}

void invaders_sh_port5_w(int offset, int data)
{
	static unsigned char Sound = 0;

	if (data & 0x01 && ~Sound & 0x01)
		sample_start (5, 4, 0);			/* Fleet 1 */

	if (data & 0x02 && ~Sound & 0x02)
		sample_start (5, 5, 0);			/* Fleet 2 */

	if (data & 0x04 && ~Sound & 0x04)
		sample_start (5, 6, 0);			/* Fleet 3 */

	if (data & 0x08 && ~Sound & 0x08)
		sample_start (5, 7, 0);			/* Fleet 4 */

	if (data & 0x10 && ~Sound & 0x10)
		sample_start (6, 8, 0);			/* Saucer Hit */

	invaders_flipscreen_w(data & 0x20);

	Sound = data;
}

void invad2ct_sh_port7_w(int offset, int data)
{
	static unsigned char Sound = 0;

	if (data & 0x01 && ~Sound & 0x01)
		sample_start (11, 14, 0);		/* Fleet 1 - Player 2 */

	if (data & 0x02 && ~Sound & 0x02)
		sample_start (11, 15, 0);		/* Fleet 2 - Player 2 */

	if (data & 0x04 && ~Sound & 0x04)
		sample_start (11, 16, 0);		/* Fleet 3 - Player 2 */

	if (data & 0x08 && ~Sound & 0x08)
		sample_start (11, 17, 0);		/* Fleet 4 - Player 2 */

	if (data & 0x10 && ~Sound & 0x10)
		sample_start (12, 18, 0);		/* Saucer Hit - Player 2 */

	invaders_flipscreen_w(data & 0x20);

	Sound = data;
}


/* HC 4/14/98 NOTE: *I* THINK there are sounds missing...
i dont know for sure... but that is my guess....... */

void boothill_sh_port3_w(int offset, int data)
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
}

/* HC 4/14/98 */
void boothill_sh_port5_w(int offset, int data)
{
	switch (data)
	{
		case 0x3b:
			sample_start (2, 1, 0);
			break;
	}
}

/*******************************************************/
/*                                                     */
/* Taito "Balloon Bomber"                              */
/*                                                     */
/*******************************************************/

/* This only does the colour swap for the explosion */
/* We do not have correct samples so sound not done */

void ballbomb_sh_port3_w(int offset, int data)
{
	invaders_screen_red_w(data & 0x04);
}


/*******************************************************/
/*                                                     */
/* Exidy "Bandido"                              	   */
/*                                                     */
/*******************************************************/

static void bandido_74123_0_output_changed_cb(void)
{
	SN76477_vco_w    (0,  TTL74123_output_r(0));
	SN76477_mixer_a_w(0, !TTL74123_output_r(0));

	SN76477_enable_w(0, TTL74123_output_comp_r(0) && TTL74123_output_comp_r(1));
}

static void bandido_74123_1_output_changed_cb(void)
{
	SN76477_set_vco_voltage(0, !TTL74123_output_comp_r(1) ? 5.0 : 0.0);

	SN76477_enable_w(0, TTL74123_output_comp_r(0) && TTL74123_output_comp_r(1));
}

static struct TTL74123_interface bandido_74123_0_intf =
{
	RES_K(33),
	CAP_U(33),
	bandido_74123_0_output_changed_cb
};

static struct TTL74123_interface bandido_74123_1_intf =
{
	RES_K(33),
	CAP_U(33),
	bandido_74123_1_output_changed_cb
};


void init_machine_bandido(void)
{
	TTL74123_config(0, &bandido_74123_0_intf);
	TTL74123_config(1, &bandido_74123_1_intf);

	/* set up the fixed connections */
	TTL74123_reset_comp_w  (0, 1);
	TTL74123_trigger_comp_w(0, 0);

	TTL74123_trigger_comp_w(1, 0);

	SN76477_envelope_1_w(0, 1);
	SN76477_envelope_2_w(0, 0);
	SN76477_noise_clock_w(0, 0);
	SN76477_mixer_b_w(0, 0);
	SN76477_mixer_c_w(0, 0);
}


static int bandido_t0,bandido_t1,bandido_p1,bandido_p2;


void bandido_sh_port4_w(int offset, int data)
{
	bandido_t0 = data & 1;

	bandido_p1 = (bandido_p1 & 0x4f) |
				 ((data & 0x02) << 3) |		/* P1.4 */
				 ((data & 0x08) << 2) |		/* P1.5 */
				 ((data & 0x20) << 2);		/* P1.7 */

	cpu_set_irq_line(1, I8035_EXT_INT, ((bandido_p1 & 0x70) == 0x70) ? ASSERT_LINE : CLEAR_LINE);


	TTL74123_trigger_w   (0, data & 0x04);

	TTL74123_trigger_w   (1, data & 0x10);
	TTL74123_reset_comp_w(1, data & 0x04);
}

void bandido_sh_port5_w(int offset, int data)
{
	bandido_t1 = (data >> 5) & 1;

	bandido_p1 = (bandido_p1 & 0xb0) |
				 ((data & 0x01) << 3) |		/* P1.3 */
				 ((data & 0x02) << 1) |		/* P1.2 */
				 ((data & 0x04) >> 1) |		/* P1.1 */
				 ((data & 0x08) >> 3) |		/* P1.0 */
				 ((data & 0x10) << 2);		/* P1.6 */

	cpu_set_irq_line(1, I8035_EXT_INT, ((bandido_p1 & 0x70) == 0x70) ? ASSERT_LINE : CLEAR_LINE);
}

int  bandido_sh_gett0(int offset)
{
	return bandido_t0;
}

int  bandido_sh_gett1(int offset)
{
	return bandido_t1;
}

int  bandido_sh_getp1(int offset)
{
	return bandido_p1;
}

int  bandido_sh_getp2(int offset)
{
	return bandido_p2;
}

void bandido_sh_putp2(int offset, int data)
{
	bandido_p2 = data;

	DAC_data_w(0, bandido_p2 & 0x80 ? 0xff : 0x00);
}
