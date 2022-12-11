/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package gr.codebb.arcadeflex.v036.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.tnzs.*;
import static gr.codebb.arcadeflex.v036.machine.tnzs.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import arcadeflex.v036.mame.sndintrfH.MachineSound;
import static arcadeflex.v036.mame.sndintrfH.SOUND_YM2203;
import static arcadeflex.v036.sound._2203intf.*;
import static arcadeflex.v036.sound._2203intfH.*;

public class tnzs
{
	
	
	
	
	
	/* max samples */
/*	#define	MAX_SAMPLES	0x2f
	
	int kageki_init_samples(const struct MachineSound *msound)
	{
		struct GameSamples *samples;
		UBytePtr scan, *src, *dest;
		int start, size;
		int i, n;
	
		size = sizeof(struct GameSamples) + MAX_SAMPLES * sizeof(struct GameSamples *);
	
		if ((Machine.samples = malloc(size)) == NULL) return 1;
	
		samples = Machine.samples;
		samples.total = MAX_SAMPLES;
	
		for (i = 0; i < samples.total; i++)
		{
			src = memory_region(REGION_SOUND1) + 0x0090;
			start = (src[(i * 2) + 1] * 256) + src[(i * 2)];
			scan = &src[start];
			size = 0;
	
			// check sample length
			while (1)
			{
				if (*scan++ == 0x00)
				{
					break;
				} else {
					size++;
				}
			}
			if ((samples.sample[i] = malloc(sizeof(struct GameSample) + size * sizeof(unsigned char))) == NULL) return 1;
	
			if (start < 0x100) start = size = 0;
	
			samples.sample[i].smpfreq = 7000;	/* 7 KHz??? */
/*			samples.sample[i].resolution = 8;	/* 8 bit */
/*			samples.sample[i].length = size;
	
			// signed 8-bit sample to unsigned 8-bit sample convert
			dest = (UBytePtr )samples.sample[i].data;
			scan = &src[start];
			for (n = 0; n < size; n++)
			{
				*dest++ = ((*scan++) ^ 0x80);
			}
		//	if (errorlog) fprintf(errorlog, "samples num:%02X ofs:%04X lng:%04X\n", i, start, size);
		}
	
		return 0;
	} };*/
	
	
	static int kageki_csport_sel = 0;
	public static ReadHandlerPtr kageki_csport_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		int	dsw, dsw1, dsw2;
	
		dsw1 = readinputport(0); 		// DSW1
		dsw2 = readinputport(1); 		// DSW2
	
		switch (kageki_csport_sel)
		{
			case	0x00:			// DSW2 5,1 / DSW1 5,1
				dsw = (((dsw2 & 0x10) >> 1) | ((dsw2 & 0x01) << 2) | ((dsw1 & 0x10) >> 3) | ((dsw1 & 0x01) >> 0));
				break;
			case	0x01:			// DSW2 7,3 / DSW1 7,3
				dsw = (((dsw2 & 0x40) >> 3) | ((dsw2 & 0x04) >> 0) | ((dsw1 & 0x40) >> 5) | ((dsw1 & 0x04) >> 2));
				break;
			case	0x02:			// DSW2 6,2 / DSW1 6,2
				dsw = (((dsw2 & 0x20) >> 2) | ((dsw2 & 0x02) << 1) | ((dsw1 & 0x20) >> 4) | ((dsw1 & 0x02) >> 1));
				break;
			case	0x03:			// DSW2 8,4 / DSW1 8,4
				dsw = (((dsw2 & 0x80) >> 4) | ((dsw2 & 0x08) >> 1) | ((dsw1 & 0x80) >> 6) | ((dsw1 & 0x08) >> 3));
				break;
			default:
				dsw = 0x00;
			//	if (errorlog) fprintf(errorlog, "kageki_csport_sel error !! (0x%08X)\n", kageki_csport_sel);
		}
	
		return (dsw & 0xff);
	} };
	
	public static WriteHandlerPtr kageki_csport_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		char[] mess=new char[80];
	
		if (data > 0x3f)
		{
			// read dipsw port
			kageki_csport_sel = (data & 0x03);
		} else {
/*TODO			if (data > MAX_SAMPLES)
			{
				// stop samples
				sample_stop(0);
				sprintf(mess, "VOICE:%02X STOP", data);
			} else {
				// play samples
				sample_start(0, data, 0);
				sprintf(mess, "VOICE:%02X PLAY", data);
			}*/
		}
	} };
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0xbfff, MRA_BANK1 ), /* ROM + RAM */
		new MemoryReadAddress( 0xc000, 0xdfff, MRA_RAM ),
		new MemoryReadAddress( 0xe000, 0xefff, tnzs_workram_r ),	/* WORK RAM (shared by the 2 z80's */
		new MemoryReadAddress( 0xf000, 0xf1ff, MRA_RAM ),	/* VDC RAM */
		new MemoryReadAddress( 0xf600, 0xf600, MRA_NOP ),	/* ? */
		new MemoryReadAddress( 0xf800, 0xfbff, MRA_RAM ),	/* not in extrmatn and arkanoi2 (PROMs instead) */
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0xbfff, MWA_BANK1 ),	/* ROM + RAM */
		new MemoryWriteAddress( 0xc000, 0xdfff, MWA_RAM, tnzs_objram ),
		new MemoryWriteAddress( 0xe000, 0xefff, tnzs_workram_w, tnzs_workram ),
		new MemoryWriteAddress( 0xf000, 0xf1ff, MWA_RAM, tnzs_vdcram ),
		new MemoryWriteAddress( 0xf200, 0xf3ff, MWA_RAM, tnzs_scrollram ), /* scrolling info */
		new MemoryWriteAddress( 0xf400, 0xf400, MWA_NOP ),	/* ? */
		new MemoryWriteAddress( 0xf600, 0xf600, tnzs_bankswitch_w ),
		new MemoryWriteAddress( 0xf800, 0xfbff, paletteram_xRRRRRGGGGGBBBBB_w, paletteram ),	/* not in extrmatn and arkanoi2 (PROMs instead) */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress sub_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x9fff, MRA_BANK2 ),
		new MemoryReadAddress( 0xb000, 0xb000, YM2203_status_port_0_r ),
		new MemoryReadAddress( 0xb001, 0xb001, YM2203_read_port_0_r ),
		new MemoryReadAddress( 0xc000, 0xc001, tnzs_mcu_r ),	/* plain input ports in insectx (memory handler */
										/* changed in insectx_init() ) */
		new MemoryReadAddress( 0xd000, 0xdfff, MRA_RAM ),
		new MemoryReadAddress( 0xe000, 0xefff, tnzs_workram_sub_r ),
		new MemoryReadAddress( 0xf000, 0xf003, arkanoi2_sh_f000_r ),	/* paddles in arkanoid2/plumppop; the ports are */
							/* read but not used by the other games, and are not read at */
							/* all by insectx. */
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sub_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x9fff, MWA_ROM ),
		new MemoryWriteAddress( 0xa000, 0xa000, tnzs_bankswitch1_w ),
		new MemoryWriteAddress( 0xb000, 0xb000, YM2203_control_port_0_w ),
		new MemoryWriteAddress( 0xb001, 0xb001, YM2203_write_port_0_w ),
		new MemoryWriteAddress( 0xc000, 0xc001, tnzs_mcu_w ),	/* not present in insectx */
		new MemoryWriteAddress( 0xd000, 0xdfff, MWA_RAM ),
		new MemoryWriteAddress( 0xe000, 0xefff, tnzs_workram_sub_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress kageki_sub_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x9fff, MRA_BANK2 ),
		new MemoryReadAddress( 0xb000, 0xb000, YM2203_status_port_0_r ),
		new MemoryReadAddress( 0xb001, 0xb001, YM2203_read_port_0_r ),
		new MemoryReadAddress( 0xc000, 0xc000, input_port_2_r ),
		new MemoryReadAddress( 0xc001, 0xc001, input_port_3_r ),
		new MemoryReadAddress( 0xc002, 0xc002, input_port_4_r ),
		new MemoryReadAddress( 0xd000, 0xdfff, MRA_RAM ),
		new MemoryReadAddress( 0xe000, 0xefff, tnzs_workram_sub_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress kageki_sub_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x9fff, MWA_ROM ),
		new MemoryWriteAddress( 0xa000, 0xa000, tnzs_bankswitch1_w ),
		new MemoryWriteAddress( 0xb000, 0xb000, YM2203_control_port_0_w ),
		new MemoryWriteAddress( 0xb001, 0xb001, YM2203_write_port_0_w ),
		new MemoryWriteAddress( 0xd000, 0xdfff, MWA_RAM ),
		new MemoryWriteAddress( 0xe000, 0xefff, tnzs_workram_sub_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	/* the bootleg board is different, it has a third CPU (and of course no mcu) */
	
	public static WriteHandlerPtr tnzsb_sound_command_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		soundlatch_w.handler(offset,data);
		cpu_cause_interrupt(2,0xff);
	} };
	
	static MemoryReadAddress tnzsb_readmem1[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x9fff, MRA_BANK2 ),
		new MemoryReadAddress( 0xb002, 0xb002, input_port_0_r ),
		new MemoryReadAddress( 0xb003, 0xb003, input_port_1_r ),
		new MemoryReadAddress( 0xc000, 0xc000, input_port_2_r ),
		new MemoryReadAddress( 0xc001, 0xc001, input_port_3_r ),
		new MemoryReadAddress( 0xc002, 0xc002, input_port_4_r ),
		new MemoryReadAddress( 0xd000, 0xdfff, MRA_RAM ),
		new MemoryReadAddress( 0xe000, 0xefff, tnzs_workram_sub_r ),
		new MemoryReadAddress( 0xf000, 0xf003, MRA_RAM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress tnzsb_writemem1[] =
	{
		new MemoryWriteAddress( 0x0000, 0x9fff, MWA_ROM ),
		new MemoryWriteAddress( 0xa000, 0xa000, tnzs_bankswitch1_w ),
		new MemoryWriteAddress( 0xb004, 0xb004, tnzsb_sound_command_w ),
		new MemoryWriteAddress( 0xd000, 0xdfff, MWA_RAM ),
		new MemoryWriteAddress( 0xe000, 0xefff, tnzs_workram_sub_w ),
		new MemoryWriteAddress( 0xf000, 0xf3ff, paletteram_xRRRRRGGGGGBBBBB_w, paletteram ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress tnzsb_readmem2[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0xc000, 0xdfff, MRA_RAM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress tnzsb_writemem2[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0xc000, 0xdfff, MWA_RAM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static IOReadPort tnzsb_readport[] =
	{
		new IOReadPort( 0x00, 0x00, YM2203_status_port_0_r  ),
		new IOReadPort( 0x02, 0x02, soundlatch_r  ),
		new IOReadPort( -1 )	/* end of table */
	};
	
	static IOWritePort tnzsb_writeport[] =
	{
		new IOWritePort( 0x00, 0x00, YM2203_control_port_0_w  ),
		new IOWritePort( 0x01, 0x01, YM2203_write_port_0_w  ),
		new IOWritePort( -1 )	/* end of table */
	};
	
	
	
	static InputPortHandlerPtr input_ports_extrmatn = new InputPortHandlerPtr(){ public void handler() { 
		PORT_START(); 		/* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x40, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_2C") );
	
		PORT_START(); 		/* DSW B */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 		/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START(); 		/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_TILT );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortHandlerPtr input_ports_arkanoi2 = new InputPortHandlerPtr(){ public void handler() { 
		PORT_START(); 		/* DSW1 - IN2 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
	
		PORT_START(); 		/* DSW2 - IN3 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );
		PORT_DIPSETTING(    0x03, "Normal" );
		PORT_DIPSETTING(    0x01, "Hard" );
		PORT_DIPSETTING(    0x00, "Very Hard" );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "50k 150k" );
		PORT_DIPSETTING(    0x0c, "100k 200k" );
		PORT_DIPSETTING(    0x04, "50k Only" );
		PORT_DIPSETTING(    0x08, "100k Only" );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x20, "2" );
		PORT_DIPSETTING(    0x30, "3" );
		PORT_DIPSETTING(    0x10, "4" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, "Allow Continue" );
		PORT_DIPSETTING(    0x80, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
	
		PORT_START(); 		/* IN1 - read at c000 (sound cpu) */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 		/* empty */
	
		PORT_START(); 		/* empty */
	
		PORT_START(); 		/* spinner 1 - read at f000/1 */
		PORT_ANALOG( 0x0fff, 0x0000, IPT_DIAL, 70, 15, 0, 0 );
		PORT_BIT   ( 0x1000, IP_ACTIVE_LOW,  IPT_COIN2 );
		PORT_BIT   ( 0x2000, IP_ACTIVE_HIGH, IPT_COIN3 );
		PORT_BIT   ( 0x4000, IP_ACTIVE_LOW,  IPT_COIN1 );
		PORT_BIT   ( 0x8000, IP_ACTIVE_LOW,  IPT_TILT );/* arbitrarily assigned, handled by the mcu */
	
		PORT_START(); 		/* spinner 2 - read at f002/3 */
		PORT_ANALOG( 0x0fff, 0x0000, IPT_DIAL | IPF_PLAYER2, 70, 15, 0, 0 );
		PORT_BIT   ( 0xf000, IP_ACTIVE_LOW,  IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortHandlerPtr input_ports_ark2us = new InputPortHandlerPtr(){ public void handler() { 
		PORT_START(); 		/* DSW1 - IN2 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x40, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_2C") );
	
		PORT_START(); 		/* DSW2 - IN3 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );
		PORT_DIPSETTING(    0x03, "Normal" );
		PORT_DIPSETTING(    0x01, "Hard" );
		PORT_DIPSETTING(    0x00, "Very Hard" );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "50k 150k" );
		PORT_DIPSETTING(    0x0c, "100k 200k" );
		PORT_DIPSETTING(    0x04, "50k Only" );
		PORT_DIPSETTING(    0x08, "100k Only" );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x20, "2" );
		PORT_DIPSETTING(    0x30, "3" );
		PORT_DIPSETTING(    0x10, "4" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, "Allow Continue" );
		PORT_DIPSETTING(    0x80, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
	
		PORT_START(); 		/* IN1 - read at c000 (sound cpu) */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 		/* empty */
	
		PORT_START(); 		/* empty */
	
		PORT_START(); 		/* spinner 1 - read at f000/1 */
		PORT_ANALOG( 0x0fff, 0x0000, IPT_DIAL, 70, 15, 0, 0 );
		PORT_BIT   ( 0x1000, IP_ACTIVE_LOW,  IPT_COIN2 );
		PORT_BIT   ( 0x2000, IP_ACTIVE_HIGH, IPT_COIN3 );
		PORT_BIT   ( 0x4000, IP_ACTIVE_LOW,  IPT_COIN1 );
		PORT_BIT   ( 0x8000, IP_ACTIVE_LOW,  IPT_TILT );/* arbitrarily assigned, handled by the mcu */
	
		PORT_START(); 		/* spinner 2 - read at f002/3 */
		PORT_ANALOG( 0x0fff, 0x0000, IPT_DIAL | IPF_PLAYER2, 70, 15, 0, 0 );
		PORT_BIT   ( 0xf000, IP_ACTIVE_LOW,  IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortHandlerPtr input_ports_plumppop = new InputPortHandlerPtr(){ public void handler() { 
		PORT_START(); 		/* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x40, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_2C") );
	
		PORT_START(); 		/* DSW B */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x20, "2" );
		PORT_DIPSETTING(    0x30, "3" );
		PORT_DIPSETTING(    0x10, "4" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_CHEAT );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 		/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_CHEAT | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START(); 		/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_TILT );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 		/* spinner 1 - read at f000/1 */
		PORT_ANALOG( 0xffff, 0x0000, IPT_DIAL, 70, 15, 0, 0 );
	
		PORT_START(); 		/* spinner 2 - read at f002/3 */
		PORT_ANALOG( 0xffff, 0x0000, IPT_DIAL | IPF_PLAYER2, 70, 15, 0, 0 );
	INPUT_PORTS_END(); }}; 
	
	static InputPortHandlerPtr input_ports_drtoppel = new InputPortHandlerPtr(){ public void handler() { 
		PORT_START(); 		/* DSW A */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x40, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_2C") );
	
		PORT_START(); 		/* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );
		PORT_DIPSETTING(    0x03, "Medium" );
		PORT_DIPSETTING(    0x01, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x0c, "30000" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Unknown") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x20, "2" );
		PORT_DIPSETTING(    0x30, "3" );
		PORT_DIPSETTING(    0x10, "4" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 		/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START(); 		/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_TILT );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortHandlerPtr input_ports_chukatai = new InputPortHandlerPtr(){ public void handler() { 
		PORT_START(); 		/* DSW A */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x40, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_2C") );
	
		PORT_START(); 		/* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );
		PORT_DIPSETTING(    0x03, "Medium" );
		PORT_DIPSETTING(    0x01, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	/* Bonus life awards are to be verified
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "50000 150000" );
		PORT_DIPSETTING(    0x0c, "70000 200000" );
		PORT_DIPSETTING(    0x04, "100000 250000" );
		PORT_DIPSETTING(    0x08, "200000 300000" );
	*/
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "1" );
		PORT_DIPSETTING(    0x10, "2" );
		PORT_DIPSETTING(    0x30, "3" );
		PORT_DIPSETTING(    0x20, "4" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 		/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START(); 		/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_TILT );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortHandlerPtr input_ports_tnzs = new InputPortHandlerPtr(){ public void handler() { 
		PORT_START(); 		/* DSW A */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x40, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_2C") );
	
		PORT_START(); 		/* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );
		PORT_DIPSETTING(    0x03, "Medium" );
		PORT_DIPSETTING(    0x01, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "50000 150000" );
		PORT_DIPSETTING(    0x0c, "70000 200000" );
		PORT_DIPSETTING(    0x04, "100000 250000" );
		PORT_DIPSETTING(    0x08, "200000 300000" );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x20, "2" );
		PORT_DIPSETTING(    0x30, "3" );
		PORT_DIPSETTING(    0x00, "4" );
		PORT_DIPSETTING(    0x10, "5" );
		PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 		/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START(); 		/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_TILT );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortHandlerPtr input_ports_tnzsb = new InputPortHandlerPtr(){ public void handler() { 
		PORT_START(); 		/* DSW A */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
	
		PORT_START(); 		/* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );
		PORT_DIPSETTING(    0x03, "Medium" );
		PORT_DIPSETTING(    0x01, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "50000 150000" );
		PORT_DIPSETTING(    0x0c, "70000 200000" );
		PORT_DIPSETTING(    0x04, "100000 250000" );
		PORT_DIPSETTING(    0x08, "200000 300000" );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x20, "2" );
		PORT_DIPSETTING(    0x30, "3" );
		PORT_DIPSETTING(    0x00, "4" );
		PORT_DIPSETTING(    0x10, "5" );
		PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 		/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START(); 		/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_TILT );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortHandlerPtr input_ports_tnzs2 = new InputPortHandlerPtr(){ public void handler() { 
		PORT_START(); 		/* DSW A */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
	
		PORT_START(); 		/* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );
		PORT_DIPSETTING(    0x03, "Medium" );
		PORT_DIPSETTING(    0x01, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "10000 100000" );
		PORT_DIPSETTING(    0x0c, "10000 150000" );
		PORT_DIPSETTING(    0x08, "10000 200000" );
		PORT_DIPSETTING(    0x04, "10000 300000" );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x20, "2" );
		PORT_DIPSETTING(    0x30, "3" );
		PORT_DIPSETTING(    0x00, "4" );
		PORT_DIPSETTING(    0x10, "5" );
		PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 		/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START(); 		/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_TILT );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortHandlerPtr input_ports_insectx = new InputPortHandlerPtr(){ public void handler() { 
		PORT_START(); 		/* DSW A */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
	
		PORT_START(); 		/* DSW B */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "1" );
		PORT_DIPSETTING(    0x10, "2" );
		PORT_DIPSETTING(    0x30, "3" );
		PORT_DIPSETTING(    0x20, "4" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 		/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START(); 		/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_TILT );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortHandlerPtr input_ports_kageki = new InputPortHandlerPtr(){ public void handler() { 
		PORT_START(); 		/* DSW A */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x40, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_2C") );
	
		PORT_START(); 		/* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );
		PORT_DIPSETTING(    0x03, "Medium" );
		PORT_DIPSETTING(    0x01, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "Allow Continue" );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Yes") );
	
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 		/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START(); 		/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_TILT );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout arkanoi2_charlayout = new GfxLayout
	(
		16,16,
		4096,
		4,
		new int[] { 3*4096*32*8, 2*4096*32*8, 1*4096*32*8, 0*4096*32*8 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,
				8*8+0,8*8+1,8*8+2,8*8+3,8*8+4,8*8+5,8*8+6,8*8+7},
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				16*8, 17*8, 18*8, 19*8, 20*8, 21*8, 22*8, 23*8 },
		32*8
	);
	
	static GfxLayout tnzs_charlayout = new GfxLayout
	(
		16,16,
		8192,
		4,
		new int[] { 3*8192*32*8, 2*8192*32*8, 1*8192*32*8, 0*8192*32*8 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,
				8*8+0, 8*8+1, 8*8+2, 8*8+3, 8*8+4, 8*8+5, 8*8+6, 8*8+7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				16*8, 17*8, 18*8, 19*8, 20*8, 21*8, 22*8, 23*8 },
		32*8
	);
	
	static GfxLayout insectx_charlayout = new GfxLayout
	(
		16,16,
		8192,
		4,
		new int[] { 8, 0, 8192*64*8+8, 8192*64*8 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,
				8*16+0, 8*16+1, 8*16+2, 8*16+3, 8*16+4, 8*16+5, 8*16+6, 8*16+7 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
			16*16, 17*16, 18*16, 19*16, 20*16, 21*16, 22*16, 23*16 },
		64*8
	);
	
	static GfxDecodeInfo arkanoi2_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, arkanoi2_charlayout, 0, 32 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo tnzs_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, tnzs_charlayout, 0, 32 ),
		new GfxDecodeInfo( -1 )	/* end of array */
	};
	
	static GfxDecodeInfo insectx_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, insectx_charlayout, 0, 32 ),
		new GfxDecodeInfo( -1 )	/* end of array */
	};
	
	
	
/*TODO*///	static struct YM2203interface ym2203_interface =
/*TODO*///	{
/*TODO*///		1,			/* 1 chip */
/*TODO*///		3000000,	/* 3 MHz ??? */
/*TODO*///		{ YM2203_VOL(30,30) },
/*TODO*///		{ input_port_0_r },		/* DSW1 connected to port A */
/*TODO*///		{ input_port_1_r },		/* DSW2 connected to port B */
/*TODO*///		{ 0 },
/*TODO*///		{ 0 }
/*TODO*///	};
        static YM2203interface ym2203_interface = new YM2203interface
	(
		1,			/* 1 chip */
		3000000,	/* 3 MHz ??? */
		new int[]{ YM2203_VOL(30,30) },
		new ReadHandlerPtr[]{ input_port_0_r },
		new ReadHandlerPtr[]{ input_port_1_r },
		new WriteHandlerPtr[]{ null },
		new WriteHandlerPtr[]{ null }
        );
	
	
	/* handler called by the 2203 emulator when the internal timers cause an IRQ */
	public static WriteYmHandlerPtr irqhandler = new WriteYmHandlerPtr() { public void handler(int irq)
	{
            cpu_set_nmi_line(2,irq!=0 ? ASSERT_LINE : CLEAR_LINE);
            
        }};
	static YM2203interface ym2203b_interface = new YM2203interface
	(
		1,			/* 1 chip */
		3000000,	/* 3 MHz ??? */
		new int[]{ YM2203_VOL(100,100) },
		new ReadHandlerPtr[]{ null },
		new ReadHandlerPtr[]{ null },
		new WriteHandlerPtr[]{ null },
		new WriteHandlerPtr[]{ null },
		new WriteYmHandlerPtr[]{ irqhandler }
        );
	
        static YM2203interface kageki_ym2203_interface = new YM2203interface
	(
		1,			/* 1 chip */
		3000000,				/* 12000000/4 ??? */
		new int[]{ YM2203_VOL(35, 15) },
		new ReadHandlerPtr[]{ kageki_csport_r },
		new ReadHandlerPtr[]{ null },
		new WriteHandlerPtr[]{ null },
		new WriteHandlerPtr[]{ kageki_csport_w}
        );

	
/*TODO*///	static struct Samplesinterface samples_interface =
/*TODO*///	{
/*TODO*///		1,					/* 1 channel */
/*TODO*///		100					/* volume */
/*TODO*///	};
	
/*TODO*///	static struct CustomSound_interface custom_interface =
/*TODO*///	{
/*TODO*///		kageki_init_samples,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	};
	
	
	static MachineDriver machine_driver_arkanoi2 = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				8000000,	/* ?? Hz (only crystal is 12MHz) */
							/* 8MHz is wrong, but extrmatn doesn't work properly at 6MHz */
				readmem,writemem,null,null,
				tnzs_interrupt,1
			),
			new MachineCPU(
				CPU_Z80,
				6000000,	/* ?? Hz */
				sub_readmem,sub_writemem,null,null,
				interrupt,1
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,		/* video frequency (Hz), duration */
		100,							/* cpu slices */
		tnzs_init_machine,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		arkanoi2_gfxdecodeinfo,
		512, 512,
		arkanoi2_vh_convert_color_prom,		/* convert color p-roms */
	
		VIDEO_TYPE_RASTER,
		null,
		tnzs_vh_start,
		tnzs_vh_stop,
		arkanoi2_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			)
		}
	);
	
	static MachineDriver machine_driver_drtoppel = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				12000000/2,		/* 6.0 MHz ??? - Main board Crystal is 12Mhz */
				readmem,writemem,null,null,
				tnzs_interrupt,1
			),
			new MachineCPU(
				CPU_Z80,
				12000000/2,		/* 6.0 MHz ??? - Main board Crystal is 12Mhz */
				sub_readmem,sub_writemem,null,null,
				interrupt,1
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,		/* video frequency (Hz), duration */
		100,							/* cpu slices */
		tnzs_init_machine,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		tnzs_gfxdecodeinfo,
		512, 512,
		arkanoi2_vh_convert_color_prom,		/* convert color bproms */
	
		VIDEO_TYPE_RASTER,
		null,
		tnzs_vh_start,
		tnzs_vh_stop,
		arkanoi2_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			)
		}
	);
	
	static MachineDriver machine_driver_tnzs = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				12000000/2,		/* 6.0 MHz ??? - Main board Crystal is 12Mhz */
				readmem,writemem,null,null,
				tnzs_interrupt,1
			),
			new MachineCPU(
				CPU_Z80,
				12000000/2,		/* 6.0 MHz ??? - Main board Crystal is 12Mhz */
				sub_readmem,sub_writemem,null,null,
				interrupt,1
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		200,	/* 100 CPU slices per frame - an high value to ensure proper */
				/* synchronization of the CPUs */
		tnzs_init_machine,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		tnzs_gfxdecodeinfo,
		512, 512,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		tnzs_vh_start,
		tnzs_vh_stop,
		tnzs_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			)
		}
	);
	
	static MachineDriver machine_driver_tnzsb = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				6000000,		/* 6 Mhz(?) */
				readmem,writemem,null,null,
				tnzs_interrupt,1
			),
			new MachineCPU(
				CPU_Z80,
				6000000,		/* 6 Mhz(?) */
				tnzsb_readmem1,tnzsb_writemem1,null,null,
				interrupt,1
			),
			new MachineCPU(
				CPU_Z80,
				4000000,		/* 4 Mhz??? */
				tnzsb_readmem2,tnzsb_writemem2,tnzsb_readport,tnzsb_writeport,
				ignore_interrupt,1
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		200,	/* 100 CPU slices per frame - an high value to ensure proper */
				/* synchronization of the CPUs */
		tnzs_init_machine,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		tnzs_gfxdecodeinfo,
		512, 512,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		tnzs_vh_start,
		tnzs_vh_stop,
		tnzs_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203b_interface
			)
		}
	);
	
	static MachineDriver machine_driver_insectx = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				6000000,	/* 6 Mhz(?) */
				readmem,writemem,null,null,
				tnzs_interrupt,1
			),
			new MachineCPU(
				CPU_Z80,
				6000000,	/* 6 Mhz(?) */
				sub_readmem,sub_writemem,null,null,
				interrupt,1
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		200,	/* 100 CPU slices per frame - an high value to ensure proper */
				/* synchronization of the CPUs */
		tnzs_init_machine,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		insectx_gfxdecodeinfo,
		512, 512,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		tnzs_vh_start,
		tnzs_vh_stop,
		tnzs_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			)
		}
	);
	
	static MachineDriver machine_driver_kageki = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				6000000,		/* 12000000/2 ??? */
				readmem, writemem, null, null,
				tnzs_interrupt, 1
			),
			new MachineCPU(
				CPU_Z80,
				4000000,		/* 12000000/3 ??? */
				kageki_sub_readmem, kageki_sub_writemem, null, null,
				interrupt, 1
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		200,	/* 200 CPU slices per frame - an high value to ensure proper */
			/* synchronization of the CPUs */
		tnzs_init_machine,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		tnzs_gfxdecodeinfo,
		512, 512,
		null,
	
		VIDEO_TYPE_RASTER|VIDEO_MODIFIES_PALETTE,
		null,
		tnzs_vh_start,
		tnzs_vh_stop,
		tnzs_vh_screenrefresh,
	
		/* sound hardware */
		0, 0, 0, 0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				kageki_ym2203_interface
			)/*,
			new MachineSound(
				SOUND_SAMPLES,
				samples_interface
			),
			new MachineSound(
				SOUND_CUSTOM,
				custom_interface
			)*/
		}
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadHandlerPtr rom_extrmatn = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x30000, REGION_CPU1 );			/* Region 0 - main cpu */
		ROM_LOAD( "b06-20.bin", 0x00000, 0x08000, 0x04e3fc1f );
		ROM_CONTINUE(           0x18000, 0x08000 );			/* banked at 8000-bfff */
		ROM_LOAD( "b06-21.bin", 0x20000, 0x10000, 0x1614d6a2 );/* banked at 8000-bfff */
	
		ROM_REGION( 0x18000, REGION_CPU2 );			/* Region 2 - sound cpu */
		ROM_LOAD( "b06-06.bin", 0x00000, 0x08000, 0x744f2c84 );
		ROM_CONTINUE(           0x10000, 0x08000 );/* banked at 8000-9fff */
	
		ROM_REGION( 0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "b06-01.bin", 0x00000, 0x20000, 0xd2afbf7e );
		ROM_LOAD( "b06-02.bin", 0x20000, 0x20000, 0xe0c2757a );
		ROM_LOAD( "b06-03.bin", 0x40000, 0x20000, 0xee80ab9d );
		ROM_LOAD( "b06-04.bin", 0x60000, 0x20000, 0x3697ace4 );
	
		ROM_REGION( 0x0400, REGION_PROMS );
		ROM_LOAD( "b06-09.bin", 0x00000, 0x200, 0xf388b361 );/* hi bytes */
		ROM_LOAD( "b06-08.bin", 0x00200, 0x200, 0x10c9aac3 );/* lo bytes */
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_arkanoi2 = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x30000, REGION_CPU1 );			/* Region 0 - main cpu */
		ROM_LOAD( "a2-05.rom",  0x00000, 0x08000, 0x136edf9d );
		ROM_CONTINUE(           0x18000, 0x08000 );		/* banked at 8000-bfff */
		/* 20000-2ffff empty */
	
		ROM_REGION( 0x18000, REGION_CPU2 );			/* Region 2 - sound cpu */
		ROM_LOAD( "a2-13.rom",  0x00000, 0x08000, 0xe8035ef1 );
		ROM_CONTINUE(           0x10000, 0x08000 );		/* banked at 8000-9fff */
	
		ROM_REGION( 0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "a2-m01.bin", 0x00000, 0x20000, 0x2ccc86b4 );
		ROM_LOAD( "a2-m02.bin", 0x20000, 0x20000, 0x056a985f );
		ROM_LOAD( "a2-m03.bin", 0x40000, 0x20000, 0x274a795f );
		ROM_LOAD( "a2-m04.bin", 0x60000, 0x20000, 0x9754f703 );
	
		ROM_REGION( 0x0400, REGION_PROMS );
		ROM_LOAD( "b08-08.bin", 0x00000, 0x200, 0xa4f7ebd9 );/* hi bytes */
		ROM_LOAD( "b08-07.bin", 0x00200, 0x200, 0xea34d9f7 );/* lo bytes */
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_ark2us = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x30000, REGION_CPU1 );			/* Region 0 - main cpu */
		ROM_LOAD( "b08-11.bin", 0x00000, 0x08000, 0x99555231 );
		ROM_CONTINUE(           0x18000, 0x08000 );		/* banked at 8000-bfff */
		/* 20000-2ffff empty */
	
		ROM_REGION( 0x18000, REGION_CPU2 );			/* Region 2 - sound cpu */
		ROM_LOAD( "b08-12.bin", 0x00000, 0x08000, 0xdc84e27d );
		ROM_CONTINUE(           0x10000, 0x08000 );		/* banked at 8000-9fff */
	
		ROM_REGION( 0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "a2-m01.bin", 0x00000, 0x20000, 0x2ccc86b4 );
		ROM_LOAD( "a2-m02.bin", 0x20000, 0x20000, 0x056a985f );
		ROM_LOAD( "a2-m03.bin", 0x40000, 0x20000, 0x274a795f );
		ROM_LOAD( "a2-m04.bin", 0x60000, 0x20000, 0x9754f703 );
	
		ROM_REGION( 0x0400, REGION_PROMS );
		ROM_LOAD( "b08-08.bin", 0x00000, 0x200, 0xa4f7ebd9 );/* hi bytes */
		ROM_LOAD( "b08-07.bin", 0x00200, 0x200, 0xea34d9f7 );/* lo bytes */
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_ark2jp = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x30000, REGION_CPU1 );			/* Region 0 - main cpu */
		ROM_LOAD( "a2-05.rom",  0x00000, 0x08000, 0x136edf9d );
		ROM_CONTINUE(           0x18000, 0x08000 );		/* banked at 8000-bfff */
		/* 20000-2ffff empty */
	
		ROM_REGION( 0x18000, REGION_CPU2 );			/* Region 2 - sound cpu */
		ROM_LOAD( "b08-06",     0x00000, 0x08000, 0xadfcd40c );
		ROM_CONTINUE(           0x10000, 0x08000 );		/* banked at 8000-9fff */
	
		ROM_REGION( 0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "a2-m01.bin", 0x00000, 0x20000, 0x2ccc86b4 );
		ROM_LOAD( "a2-m02.bin", 0x20000, 0x20000, 0x056a985f );
		ROM_LOAD( "a2-m03.bin", 0x40000, 0x20000, 0x274a795f );
		ROM_LOAD( "a2-m04.bin", 0x60000, 0x20000, 0x9754f703 );
	
		ROM_REGION( 0x0400, REGION_PROMS );
		ROM_LOAD( "b08-08.bin", 0x00000, 0x200, 0xa4f7ebd9 );/* hi bytes */
		ROM_LOAD( "b08-07.bin", 0x00200, 0x200, 0xea34d9f7 );/* lo bytes */
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_plumppop = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x30000, REGION_CPU1 );/* 64k + bankswitch areas for the first CPU */
		ROM_LOAD( "a98-09.bin", 0x00000, 0x08000, 0x107f9e06 );
		ROM_CONTINUE(           0x18000, 0x08000 );			/* banked at 8000-bfff */
		ROM_LOAD( "a98-10.bin", 0x20000, 0x10000, 0xdf6e6af2 );/* banked at 8000-bfff */
	
		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the second CPU */
		ROM_LOAD( "a98-11.bin", 0x00000, 0x08000, 0xbc56775c );
		ROM_CONTINUE(           0x10000, 0x08000 );	/* banked at 8000-9fff */
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "a98-01.bin", 0x00000, 0x10000, 0xf3033dca );
		ROM_RELOAD(             0x10000, 0x10000 );
		ROM_LOAD( "a98-02.bin", 0x20000, 0x10000, 0xf2d17b0c );
		ROM_RELOAD(             0x30000, 0x10000 );
		ROM_LOAD( "a98-03.bin", 0x40000, 0x10000, 0x1a519b0a );
		ROM_RELOAD(             0x40000, 0x10000 );
		ROM_LOAD( "a98-04.bin", 0x60000, 0x10000, 0xb64501a1 );
		ROM_RELOAD(             0x70000, 0x10000 );
		ROM_LOAD( "a98-05.bin", 0x80000, 0x10000, 0x45c36963 );
		ROM_RELOAD(             0x90000, 0x10000 );
		ROM_LOAD( "a98-06.bin", 0xa0000, 0x10000, 0xe075341b );
		ROM_RELOAD(             0xb0000, 0x10000 );
		ROM_LOAD( "a98-07.bin", 0xc0000, 0x10000, 0x8e16cd81 );
		ROM_RELOAD(             0xd0000, 0x10000 );
		ROM_LOAD( "a98-08.bin", 0xe0000, 0x10000, 0xbfa7609a );
		ROM_RELOAD(             0xf0000, 0x10000 );
	
		ROM_REGION( 0x0400, REGION_PROMS );	/* color proms */
		ROM_LOAD( "a98-13.bpr", 0x0000, 0x200, 0x7cde2da5 );/* hi bytes */
		ROM_LOAD( "a98-12.bpr", 0x0200, 0x200, 0x90dc9da7 );/* lo bytes */
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_drtoppel = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x30000, REGION_CPU1 );/* 64k + bankswitch areas for the first CPU */
		ROM_LOAD( "b19-09.bin", 0x00000, 0x08000, 0x3e654f82 );
		ROM_CONTINUE(           0x18000, 0x08000 );			/* banked at 8000-bfff */
		ROM_LOAD( "b19-10.bin", 0x20000, 0x10000, 0x7e72fd25 );/* banked at 8000-bfff */
	
		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the second CPU */
		ROM_LOAD( "b19-11.bin", 0x00000, 0x08000, 0x524dc249 );
		ROM_CONTINUE(           0x10000, 0x08000 );	/* banked at 8000-9fff */
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "b19-01.bin", 0x00000, 0x20000, 0xa7e8a0c1 );
		ROM_LOAD( "b19-02.bin", 0x20000, 0x20000, 0x790ae654 );
		ROM_LOAD( "b19-03.bin", 0x40000, 0x20000, 0x495c4c5a );
		ROM_LOAD( "b19-04.bin", 0x60000, 0x20000, 0x647007a0 );
		ROM_LOAD( "b19-05.bin", 0x80000, 0x20000, 0x49f2b1a5 );
		ROM_LOAD( "b19-06.bin", 0xa0000, 0x20000, 0x2d39f1d0 );
		ROM_LOAD( "b19-07.bin", 0xc0000, 0x20000, 0x8bb06f41 );
		ROM_LOAD( "b19-08.bin", 0xe0000, 0x20000, 0x3584b491 );
	
		ROM_REGION( 0x0400, REGION_PROMS );	/* color proms */
		ROM_LOAD( "b19-13.bin", 0x0000, 0x200, 0x6a547980 );/* hi bytes */
		ROM_LOAD( "b19-12.bin", 0x0200, 0x200, 0x5754e9d8 );/* lo bytes */
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_chukatai = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x30000, REGION_CPU1 );/* 64k + bankswitch areas for the first CPU */
		ROM_LOAD( "b44.10", 0x00000, 0x08000, 0x8c69e008 );
		ROM_CONTINUE(       0x18000, 0x08000 );			/* banked at 8000-bfff */
		ROM_LOAD( "b44.11", 0x20000, 0x10000, 0x32484094 ); /* banked at 8000-bfff */
	
		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the second CPU */
		ROM_LOAD( "b44.12", 0x00000, 0x08000, 0x0600ace6 );
		ROM_CONTINUE(       0x10000, 0x08000 );/* banked at 8000-9fff */
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "b44-01.a13", 0x00000, 0x20000, 0xaae7b3d5 );
		ROM_LOAD( "b44-02.a12", 0x20000, 0x20000, 0x7f0b9568 );
		ROM_LOAD( "b44-03.a10", 0x40000, 0x20000, 0x5a54a3b9 );
		ROM_LOAD( "b44-04.a08", 0x60000, 0x20000, 0x3c5f544b );
		ROM_LOAD( "b44-05.a07", 0x80000, 0x20000, 0xd1b7e314 );
		ROM_LOAD( "b44-06.a05", 0xa0000, 0x20000, 0x269978a8 );
		ROM_LOAD( "b44-07.a04", 0xc0000, 0x20000, 0x3e0e737e );
		ROM_LOAD( "b44-08.a02", 0xe0000, 0x20000, 0x6cb1e8fc );
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_tnzs = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x30000, REGION_CPU1 );/* 64k + bankswitch areas for the first CPU */
		ROM_LOAD( "nzsb5310.bin", 0x00000, 0x08000, 0xa73745c6 );
		ROM_CONTINUE(             0x18000, 0x18000 );	/* banked at 8000-bfff */
	
		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the second CPU */
		ROM_LOAD( "nzsb5311.bin", 0x00000, 0x08000, 0x9784d443 );
		ROM_CONTINUE(             0x10000, 0x08000 );	/* banked at 8000-9fff */
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		/* ROMs taken from another set (the ones from this set were read incorrectly) */
		ROM_LOAD( "nzsb5316.bin", 0x00000, 0x20000, 0xc3519c2a );
		ROM_LOAD( "nzsb5317.bin", 0x20000, 0x20000, 0x2bf199e8 );
		ROM_LOAD( "nzsb5318.bin", 0x40000, 0x20000, 0x92f35ed9 );
		ROM_LOAD( "nzsb5319.bin", 0x60000, 0x20000, 0xedbb9581 );
		ROM_LOAD( "nzsb5322.bin", 0x80000, 0x20000, 0x59d2aef6 );
		ROM_LOAD( "nzsb5323.bin", 0xa0000, 0x20000, 0x74acfb9b );
		ROM_LOAD( "nzsb5320.bin", 0xc0000, 0x20000, 0x095d0dc0 );
		ROM_LOAD( "nzsb5321.bin", 0xe0000, 0x20000, 0x9800c54d );
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_tnzsb = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x30000, REGION_CPU1 );/* 64k + bankswitch areas for the first CPU */
		ROM_LOAD( "nzsb5324.bin", 0x00000, 0x08000, 0xd66824c6 );
		ROM_CONTINUE(             0x18000, 0x18000 );	/* banked at 8000-bfff */
	
		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the second CPU */
		ROM_LOAD( "nzsb5325.bin", 0x00000, 0x08000, 0xd6ac4e71 );
		ROM_CONTINUE(             0x10000, 0x08000 );	/* banked at 8000-9fff */
	
		ROM_REGION( 0x10000, REGION_CPU3 );/* 64k for the third CPU */
		ROM_LOAD( "nzsb5326.bin", 0x00000, 0x10000, 0xcfd5649c );
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		/* ROMs taken from another set (the ones from this set were read incorrectly) */
		ROM_LOAD( "nzsb5316.bin", 0x00000, 0x20000, 0xc3519c2a );
		ROM_LOAD( "nzsb5317.bin", 0x20000, 0x20000, 0x2bf199e8 );
		ROM_LOAD( "nzsb5318.bin", 0x40000, 0x20000, 0x92f35ed9 );
		ROM_LOAD( "nzsb5319.bin", 0x60000, 0x20000, 0xedbb9581 );
		ROM_LOAD( "nzsb5322.bin", 0x80000, 0x20000, 0x59d2aef6 );
		ROM_LOAD( "nzsb5323.bin", 0xa0000, 0x20000, 0x74acfb9b );
		ROM_LOAD( "nzsb5320.bin", 0xc0000, 0x20000, 0x095d0dc0 );
		ROM_LOAD( "nzsb5321.bin", 0xe0000, 0x20000, 0x9800c54d );
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_tnzs2 = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x30000, REGION_CPU1 );/* 64k + bankswitch areas for the first CPU */
		ROM_LOAD( "ns_c-11.rom",  0x00000, 0x08000, 0x3c1dae7b );
		ROM_CONTINUE(             0x18000, 0x18000 );	/* banked at 8000-bfff */
	
		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the second CPU */
		ROM_LOAD( "ns_e-3.rom",   0x00000, 0x08000, 0xc7662e96 );
		ROM_CONTINUE(             0x10000, 0x08000 );
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ns_a13.rom",   0x00000, 0x20000, 0x7e0bd5bb );
		ROM_LOAD( "ns_a12.rom",   0x20000, 0x20000, 0x95880726 );
		ROM_LOAD( "ns_a10.rom",   0x40000, 0x20000, 0x2bc4c053 );
		ROM_LOAD( "ns_a08.rom",   0x60000, 0x20000, 0x8ff8d88c );
		ROM_LOAD( "ns_a07.rom",   0x80000, 0x20000, 0x291bcaca );
		ROM_LOAD( "ns_a05.rom",   0xa0000, 0x20000, 0x6e762e20 );
		ROM_LOAD( "ns_a04.rom",   0xc0000, 0x20000, 0xe1fd1b9d );
		ROM_LOAD( "ns_a02.rom",   0xe0000, 0x20000, 0x2ab06bda );
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_insectx = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x30000, REGION_CPU1 );/* 64k + bankswitch areas for the first CPU */
		ROM_LOAD( "insector.u32", 0x00000, 0x08000, 0x18eef387 );
		ROM_CONTINUE(             0x18000, 0x18000 );	/* banked at 8000-bfff */
	
		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the second CPU */
		ROM_LOAD( "insector.u38", 0x00000, 0x08000, 0x324b28c9 );
		ROM_CONTINUE(             0x10000, 0x08000 );	/* banked at 8000-9fff */
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "insector.r15", 0x00000, 0x80000, 0xd00294b1 );
		ROM_LOAD( "insector.r16", 0x80000, 0x80000, 0xdb5a7434 );
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_kageki = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x30000, REGION_CPU1 );
		ROM_LOAD( "b35-16.11c",  0x00000, 0x08000, 0xa4e6fd58 );/* US ver */
		ROM_CONTINUE(            0x18000, 0x08000 );
		ROM_LOAD( "b35-10.9c",   0x20000, 0x10000, 0xb150457d );
	
		ROM_REGION( 0x18000, REGION_CPU2 );
		ROM_LOAD( "b35-17.43e",  0x00000, 0x08000, 0xfdd9c246 );/* US ver */
		ROM_CONTINUE(            0x10000, 0x08000 );
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "b35-01.13a",  0x00000, 0x20000, 0x01d83a69 );
		ROM_LOAD( "b35-02.12a",  0x20000, 0x20000, 0xd8af47ac );
		ROM_LOAD( "b35-03.10a",  0x40000, 0x20000, 0x3cb68797 );
		ROM_LOAD( "b35-04.8a",   0x60000, 0x20000, 0x71c03f91 );
		ROM_LOAD( "b35-05.7a",   0x80000, 0x20000, 0xa4e20c08 );
		ROM_LOAD( "b35-06.5a",   0xa0000, 0x20000, 0x3f8ab658 );
		ROM_LOAD( "b35-07.4a",   0xc0000, 0x20000, 0x1b4af049 );
		ROM_LOAD( "b35-08.2a",   0xe0000, 0x20000, 0xdeb2268c );
	
		ROM_REGION( 0x10000, REGION_SOUND1 );/* samples */
		ROM_LOAD( "b35-15.98g",  0x00000, 0x10000, 0xe6212a0f );/* US ver */
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_kagekij = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x30000, REGION_CPU1 );
		ROM_LOAD( "b35-09j.11c", 0x00000, 0x08000, 0x829637d5 );/* JP ver */
		ROM_CONTINUE(            0x18000, 0x08000 );
		ROM_LOAD( "b35-10.9c",   0x20000, 0x10000, 0xb150457d );
	
		ROM_REGION( 0x18000, REGION_CPU2 );
		ROM_LOAD( "b35-11j.43e", 0x00000, 0x08000, 0x64d093fc );/* JP ver */
		ROM_CONTINUE(            0x10000, 0x08000 );
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "b35-01.13a",  0x00000, 0x20000, 0x01d83a69 );
		ROM_LOAD( "b35-02.12a",  0x20000, 0x20000, 0xd8af47ac );
		ROM_LOAD( "b35-03.10a",  0x40000, 0x20000, 0x3cb68797 );
		ROM_LOAD( "b35-04.8a",   0x60000, 0x20000, 0x71c03f91 );
		ROM_LOAD( "b35-05.7a",   0x80000, 0x20000, 0xa4e20c08 );
		ROM_LOAD( "b35-06.5a",   0xa0000, 0x20000, 0x3f8ab658 );
		ROM_LOAD( "b35-07.4a",   0xc0000, 0x20000, 0x1b4af049 );
		ROM_LOAD( "b35-08.2a",   0xe0000, 0x20000, 0xdeb2268c );
	
		ROM_REGION( 0x10000, REGION_SOUND1 );/* samples */
		ROM_LOAD( "b35-12j.98g", 0x00000, 0x10000, 0x184409f1 );/* JP ver */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_extrmatn	   = new GameDriver("1987"	,"extrmatn"	,"tnzs.java"	,rom_extrmatn,null	,machine_driver_arkanoi2	,input_ports_extrmatn	,init_extrmatn	,ROT270	,	"[Taito] World Games", "Extermination (US)" );
	public static GameDriver driver_arkanoi2	   = new GameDriver("1987"	,"arkanoi2"	,"tnzs.java"	,rom_arkanoi2,null	,machine_driver_arkanoi2	,input_ports_arkanoi2	,init_arkanoi2	,ROT270	,	"Taito Corporation Japan", "Arkanoid - Revenge of DOH (World)" );
	public static GameDriver driver_ark2us	   = new GameDriver("1987"	,"ark2us"	,"tnzs.java"	,rom_ark2us,driver_arkanoi2	,machine_driver_arkanoi2	,input_ports_ark2us	,init_arkanoi2	,ROT270	,	"Taito America Corporation (Romstar license)", "Arkanoid - Revenge of DOH (US)" );
	public static GameDriver driver_ark2jp	   = new GameDriver("1987"	,"ark2jp"	,"tnzs.java"	,rom_ark2jp,driver_arkanoi2	,machine_driver_arkanoi2	,input_ports_ark2us	,init_arkanoi2	,ROT270	,	"Taito Corporation", "Arkanoid - Revenge of DOH (Japan)" );
	public static GameDriver driver_plumppop	   = new GameDriver("1987"	,"plumppop"	,"tnzs.java"	,rom_plumppop,null	,machine_driver_drtoppel	,input_ports_plumppop	,init_drtoppel	,ROT0	,	"Taito Corporation", "Plump Pop (Japan)" );
	public static GameDriver driver_drtoppel	   = new GameDriver("1987"	,"drtoppel"	,"tnzs.java"	,rom_drtoppel,null	,machine_driver_drtoppel	,input_ports_drtoppel	,init_drtoppel	,ROT90	,	"Taito Corporation", "Dr. Toppel's Tankentai (Japan)" );
	public static GameDriver driver_chukatai	   = new GameDriver("1988"	,"chukatai"	,"tnzs.java"	,rom_chukatai,null	,machine_driver_tnzs	,input_ports_chukatai	,init_chukatai	,ROT0	,	"Taito Corporation", "Chuka Taisen (Japan)" );
	public static GameDriver driver_tnzs	   = new GameDriver("1988"	,"tnzs"	,"tnzs.java"	,rom_tnzs,null	,machine_driver_tnzs	,input_ports_tnzs	,init_tnzs	,ROT0	,	"Taito Corporation", "The NewZealand Story (Japan)" );
	public static GameDriver driver_tnzsb	   = new GameDriver("1988"	,"tnzsb"	,"tnzs.java"	,rom_tnzsb,driver_tnzs	,machine_driver_tnzsb	,input_ports_tnzsb	,init_tnzs	,ROT0	,	"bootleg", "The NewZealand Story (World, bootleg)" );
	public static GameDriver driver_tnzs2	   = new GameDriver("1988"	,"tnzs2"	,"tnzs.java"	,rom_tnzs2,driver_tnzs	,machine_driver_tnzs	,input_ports_tnzs2	,init_tnzs	,ROT0	,	"Taito Corporation Japan", "The NewZealand Story 2 (World)" );
	public static GameDriver driver_insectx	   = new GameDriver("1989"	,"insectx"	,"tnzs.java"	,rom_insectx,null	,machine_driver_insectx	,input_ports_insectx	,init_insectx	,ROT0	,	"Taito Corporation Japan", "Insector X (World)" );
	public static GameDriver driver_kageki	   = new GameDriver("1988"	,"kageki"	,"tnzs.java"	,rom_kageki,null	,machine_driver_kageki	,input_ports_kageki	,init_kageki	,ROT90	,	"Taito America Corporation (Romstar license)", "Kageki (US)" );
	public static GameDriver driver_kagekij	   = new GameDriver("1988"	,"kagekij"	,"tnzs.java"	,rom_kagekij,driver_kageki	,machine_driver_kageki	,input_ports_kageki	,init_kageki	,ROT90	,	"Taito Corporation", "Kageki (Japan)" );
}

