/***************************************************************************

Super Contra / Thunder Cross

driver by Bryan McPhail, Manuel Abadia

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package gr.codebb.arcadeflex.v036.drivers;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_r;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_w;
import static gr.codebb.arcadeflex.v036.cpu.konami.konamiH.*;
import static gr.codebb.arcadeflex.v037b7.cpu.z80.z80H.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.memory.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.thunderx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;
import static gr.codebb.arcadeflex.v036.sound._2151intf.*;
import static gr.codebb.arcadeflex.v036.sound._2151intfH.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static gr.codebb.arcadeflex.v036.sound.k007232.*;
import static gr.codebb.arcadeflex.v036.sound.k007232H.*;

public class thunderx
{
	
	
	/***************************************************************************/
	
	public static InterruptPtr scontra_interrupt = new InterruptPtr() { public int handler() 
	{
		if (K052109_is_IRQ_enabled()!=0)
			return KONAMI_INT_IRQ;
		else
			return ignore_interrupt.handler();
	} };
	
	public static InterruptPtr thunderx_interrupt = new InterruptPtr() { public int handler() 
	{
		if (K052109_is_IRQ_enabled()!=0)
		{
			if (cpu_getiloops() == 0) return KONAMI_INT_IRQ;
			else if ((cpu_getiloops() & 1)!=0) return KONAMI_INT_FIRQ;	/* ??? */
		}
		return ignore_interrupt.handler();
	} };
	
	
	static int palette_selected;
	static int bank;
	static UBytePtr ram=new UBytePtr();
        static UBytePtr unknownram=new UBytePtr();
	
	public static ReadHandlerPtr scontra_bankedram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (palette_selected!=0)
			return paletteram_r.handler(offset);
		else
			return ram.read(offset);
	} };
	
	public static WriteHandlerPtr scontra_bankedram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (palette_selected!=0)
			paletteram_xBBBBBGGGGGRRRRR_swap_w.handler(offset,data);
		else
			ram.write(offset,data);
	} };
	
	public static ReadHandlerPtr thunderx_bankedram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if ((bank & 0x01) == 0)
		{
			if ((bank & 0x10)!=0)
				return unknownram.read(offset);
			else
				return paletteram_r.handler(offset);
		}
		else
			return ram.read(offset);
	} };
	
	public static WriteHandlerPtr thunderx_bankedram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if ((bank & 0x01) == 0)
		{
			if ((bank & 0x10)!=0)
				unknownram.write(offset,data);
			else
				paletteram_xBBBBBGGGGGRRRRR_swap_w.handler(offset,data);
		}
		else
			ram.write(offset,data);
	} };
	
	static void calculate_collisions() //not tested (shadow)
        {
/*		UBytePtr ptr1 = new UBytePtr(unknownram,0x10);
                UBytePtr ptr2;
		int i, j;
	
		/* each sprite is defined as: flags height width xpos ypos */
/*		for( i = 0; i < 127; i++, ptr1.base += 5 ) {
			int w,h;
			int	x,y;
	
			if ( ( ptr1.read(0) & 0x80 ) == 0x00 )
				continue;
	
			ptr2 = new UBytePtr(ptr1, 5);
			w = 4; /* ? */
/*			h = 4; /* ? */
/*			x = ptr1.read(3);
			y = ptr1.read(4);
	
			for( j = i+1; j < 128; j++, ptr2.base += 5 ) {
				int x1,y1;
	
				if ( ( ptr2.read(0) & 0x80 ) == 0x00 )
					continue;
	
				x1 = ptr2.read(3);
				y1 = ptr2.read(4);
	
				x1 -= x;
	
				if ( x1 < 0 )
					x1 = -x1;
	
				if ( x1 > w )
					continue;
	
				y1 -= y;
	
				if ( y1 < 0 )
					y1 = -y1;
	
				if ( y1 > h )
					continue;
	
	/*
	00 - 02 - our ships
	02 - 40 - our bullets
	42 - 16 - enemy bullets
	58 - 60 - enemy ships
	118 - ? - ?
	*/
/*				if ( i > 117 )
					continue;
	
				if ( i < 42 ) { /* our ship & bullets */
/*					if ( j < 42 ) /* our ship & bullets */
/*						continue;
/*				} else { /* enemy ships & bullets */
/*					if ( j > 41 ) /* enemy ships & bullets */
/*						continue;
				}
	
				/* bullets dont collide eachother */
/*				if ( i > 1 && i < 42 )
					if ( j > 41 && j < 58 )
						continue;
	
				/* collision */
/*				if (( ptr1.read(0) & 0x20 )!=0)
					ptr1.write(0,ptr1.read(0) | 0x10);
	
				if (( ptr2.read(0) & 0x20 )!=0)
					ptr2.write(0,ptr2.read(0) | 0x10);
			}
		}*/
	}
	
	public static WriteHandlerPtr thunderx_1f98_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	//if (errorlog) fprintf(errorlog,"%04x: write %02x to 1f98\n",cpu_get_pc(),data);
		/* bit 0 = enable char ROM reading through the video RAM */
		K052109_set_RMRD_line((data & 0x01)!=0 ? ASSERT_LINE : CLEAR_LINE);
	
		/* bit 1 unknown - used by Thunder Cross during test of RAM C8 (5800-5fff) */
		if (( data & 2 )!=0)
			calculate_collisions();
	} };
	
	public static WriteHandlerPtr scontra_bankswitch_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
		int offs;
	
	//if (errorlog) fprintf(errorlog,"%04x: bank switch %02x\n",cpu_get_pc(),data);
	
		/* bits 0-3 ROM bank */
		offs = 0x10000 + (data & 0x0f)*0x2000;
		cpu_setbank( 1, new UBytePtr(RAM,offs) );
	
		/* bit 4 select work RAM or palette RAM at 5800-5fff */
		palette_selected = ~data & 0x10;
	
		/* bits 5/6 coin counters */
		coin_counter_w.handler(0,data & 0x20);
		coin_counter_w.handler(1,data & 0x40);
	
		/* bit 7 controls layer priority */
		scontra_priority = data & 0x80;
	} };
	
	public static WriteHandlerPtr thunderx_videobank_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	//if (errorlog) fprintf(errorlog,"%04x: select video ram bank %02x\n",cpu_get_pc(),data);
		/* 0x01 = work RAM at 4000-5fff */
		/* 0x00 = palette at 5800-5fff */
		/* 0x10 = unknown RAM at 5800-5fff */
		bank = data;
	
		/* bits 1/2 coin counters */
		coin_counter_w.handler(0,data & 0x02);
		coin_counter_w.handler(1,data & 0x04);
	
		/* bit 3 controls layer priority (seems to be always 1) */
		scontra_priority = data & 0x08;
	} };
	
	public static WriteHandlerPtr thunderx_sh_irqtrigger_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		cpu_cause_interrupt(1,0xff);
	} };
	
	public static WriteHandlerPtr scontra_snd_bankswitch_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_SOUND1);
		/* b3-b2: bank for chanel B */
		/* b1-b0: bank for chanel A */
	
		int bank_A = 0x20000*(data & 0x03);
		int bank_B = 0x20000*((data >> 2) & 0x03);
	
		K007232_bankswitch(0,new UBytePtr(RAM,bank_A),new UBytePtr(RAM,bank_B));
	} };

        public static InitMachinePtr scontra_init_machine = new InitMachinePtr() { public void handler() 
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		paletteram = new UBytePtr(RAM,0x30000);
	} };
	
	public static InitMachinePtr thunderx_init_machine = new InitMachinePtr() { public void handler() 
	{
/*TODO*///		unsigned char *RAM = memory_region(REGION_CPU1);
	
/*TODO*///		konami_cpu_setlines_callback = thunderx_banking;
/*TODO*///		cpu_setbank( 1, &RAM[0x10000] ); /* init the default bank */
	
/*TODO*///		paletteram = &RAM[0x28000];
/*TODO*///		unknownram = &RAM[0x28800];
	} };
	/***************************************************************************/
	
	static MemoryReadAddress scontra_readmem[] =
	{
		new MemoryReadAddress( 0x1f90, 0x1f90, input_port_0_r ), /* coin */
		new MemoryReadAddress( 0x1f91, 0x1f91, input_port_1_r ), /* p1 */
		new MemoryReadAddress( 0x1f92, 0x1f92, input_port_2_r ), /* p2 */
		new MemoryReadAddress( 0x1f93, 0x1f93, input_port_5_r ), /* Dip 3 */
		new MemoryReadAddress( 0x1f94, 0x1f94, input_port_3_r ), /* Dip 1 */
		new MemoryReadAddress( 0x1f95, 0x1f95, input_port_4_r ), /* Dip 2 */
	
		new MemoryReadAddress( 0x0000, 0x3fff, K052109_051960_r ),
		new MemoryReadAddress( 0x4000, 0x57ff, MRA_RAM ),
		new MemoryReadAddress( 0x5800, 0x5fff, scontra_bankedram_r ),			/* palette + work RAM */
		new MemoryReadAddress( 0x6000, 0x7fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress thunderx_readmem[] =
	{
		new MemoryReadAddress( 0x1f90, 0x1f90, input_port_0_r ), /* coin */
		new MemoryReadAddress( 0x1f91, 0x1f91, input_port_1_r ), /* p1 */
		new MemoryReadAddress( 0x1f92, 0x1f92, input_port_2_r ), /* p2 */
		new MemoryReadAddress( 0x1f93, 0x1f93, input_port_5_r ), /* Dip 3 */
		new MemoryReadAddress( 0x1f94, 0x1f94, input_port_3_r ), /* Dip 1 */
		new MemoryReadAddress( 0x1f95, 0x1f95, input_port_4_r ), /* Dip 2 */
	
		new MemoryReadAddress( 0x0000, 0x3fff, K052109_051960_r ),
		new MemoryReadAddress( 0x4000, 0x57ff, MRA_RAM ),
		new MemoryReadAddress( 0x5800, 0x5fff, thunderx_bankedram_r ),			/* palette + work RAM + unknown RAM */
		new MemoryReadAddress( 0x6000, 0x7fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress scontra_writemem[] =
	{
		new MemoryWriteAddress( 0x1f80, 0x1f80, scontra_bankswitch_w ),	/* bankswitch control + coin counters */
		new MemoryWriteAddress( 0x1f84, 0x1f84, soundlatch_w ),
		new MemoryWriteAddress( 0x1f88, 0x1f88, thunderx_sh_irqtrigger_w ),		/* cause interrupt on audio CPU */
		new MemoryWriteAddress( 0x1f8c, 0x1f8c, watchdog_reset_w ),
		new MemoryWriteAddress( 0x1f98, 0x1f98, thunderx_1f98_w ),
	
		new MemoryWriteAddress( 0x0000, 0x3fff, K052109_051960_w ),		/* video RAM + sprite RAM */
		new MemoryWriteAddress( 0x4000, 0x57ff, MWA_RAM ),
		new MemoryWriteAddress( 0x5800, 0x5fff, scontra_bankedram_w, ram ),			/* palette + work RAM */
		new MemoryWriteAddress( 0x6000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryWriteAddress thunderx_writemem[] =
	{
		new MemoryWriteAddress( 0x1f80, 0x1f80, thunderx_videobank_w ),
		new MemoryWriteAddress( 0x1f84, 0x1f84, soundlatch_w ),
		new MemoryWriteAddress( 0x1f88, 0x1f88, thunderx_sh_irqtrigger_w ),		/* cause interrupt on audio CPU */
		new MemoryWriteAddress( 0x1f8c, 0x1f8c, watchdog_reset_w ),
		new MemoryWriteAddress( 0x1f98, 0x1f98, thunderx_1f98_w ),
	
		new MemoryWriteAddress( 0x0000, 0x3fff, K052109_051960_w ),
		new MemoryWriteAddress( 0x4000, 0x57ff, MWA_RAM ),
		new MemoryWriteAddress( 0x5800, 0x5fff, thunderx_bankedram_w, ram ),			/* palette + work RAM + unknown RAM */
		new MemoryWriteAddress( 0x6000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress scontra_readmem_sound[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),				/* ROM */
		new MemoryReadAddress( 0x8000, 0x87ff, MRA_RAM ),				/* RAM */
		new MemoryReadAddress( 0xa000, 0xa000, soundlatch_r ),			/* soundlatch_r */
		new MemoryReadAddress( 0xb000, 0xb00d, K007232_read_port_0_r ),	/* 007232 registers */
		new MemoryReadAddress( 0xc001, 0xc001, YM2151_status_port_0_r ),	/* YM2151 */
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress scontra_writemem_sound[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),					/* ROM */
		new MemoryWriteAddress( 0x8000, 0x87ff, MWA_RAM ),					/* RAM */
		new MemoryWriteAddress( 0xb000, 0xb00d, K007232_write_port_0_w ),		/* 007232 registers */
		new MemoryWriteAddress( 0xc000, 0xc000, YM2151_register_port_0_w ),	/* YM2151 */
		new MemoryWriteAddress( 0xc001, 0xc001, YM2151_data_port_0_w ),		/* YM2151 */
		new MemoryWriteAddress( 0xf000, 0xf000, scontra_snd_bankswitch_w ),	/* 007232 bank select */
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress thunderx_readmem_sound[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x87ff, MRA_RAM ),
		new MemoryReadAddress( 0xa000, 0xa000, soundlatch_r ),
		new MemoryReadAddress( 0xc001, 0xc001, YM2151_status_port_0_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress thunderx_writemem_sound[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0x87ff, MWA_RAM ),
		new MemoryWriteAddress( 0xc000, 0xc000, YM2151_register_port_0_w ),
		new MemoryWriteAddress( 0xc001, 0xc001, YM2151_data_port_0_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	/***************************************************************************
	
		Input Ports
	
	***************************************************************************/
	
	static InputPortPtr input_ports_scontra = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* COINSW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* PLAYER 1 INPUTS */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* PLAYER 2 INPUTS */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* DSW #1 */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
	//	PORT_DIPSETTING(    0x00, "Invalid" );
	
		PORT_START(); 	/* DSW #2 */
		PORT_DIPNAME( 0x03, 0x02, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x03, "2" );
		PORT_DIPSETTING(	0x02, "3" );
		PORT_DIPSETTING(	0x01, "5" );
		PORT_DIPSETTING(	0x00, "7" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unknown") );	/* test mode calls it cabinet type, */
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );		/* but this is a 2 players game */
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x18, 0x10, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(	0x18, "30000 200000" );
		PORT_DIPSETTING(	0x10, "50000 300000" );
		PORT_DIPSETTING(	0x08, "30000" );
		PORT_DIPSETTING(	0x00, "50000" );
		PORT_DIPNAME( 0x60, 0x40, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x60, "Easy" );
		PORT_DIPSETTING(	0x40, "Normal" );
		PORT_DIPSETTING(	0x20, "Difficult" );
		PORT_DIPSETTING(	0x00, "Very Difficult" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW #3 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(	0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, "Continue Limit" );
		PORT_DIPSETTING(    0x08, "3" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_thunderx = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* PLAYER 1 INPUTS */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* PLAYER 2 INPUTS */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
	//	PORT_DIPSETTING(    0x00, "Invalid" );
	
	 	PORT_START(); 
		PORT_DIPNAME( 0x03, 0x02, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "2" );
		PORT_DIPSETTING(    0x02, "3" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPSETTING(    0x00, "7" );
		PORT_DIPNAME( 0x04, 0x00, "Award Bonus Life" );
		PORT_DIPSETTING(    0x04, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x18, "30000 200000" );
		PORT_DIPSETTING(    0x10, "50000 300000" );
		PORT_DIPSETTING(    0x08, "30000" );
		PORT_DIPSETTING(    0x00, "50000" );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x60, "Easy" );
		PORT_DIPSETTING(    0x40, "Normal" );
		PORT_DIPSETTING(    0x20, "Difficult" );
		PORT_DIPSETTING(    0x00, "Very Difficult" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	
	
	/***************************************************************************
	
		Machine Driver
	
	***************************************************************************/
	
	static YM2151interface ym2151_interface = new YM2151interface
	(
		1,			/* 1 chip */
		3579545,	/* 3.579545 MHz */
		new int[]{ YM3012_VOL(100,MIXER_PAN_LEFT,100,MIXER_PAN_RIGHT) },
		new WriteYmHandlerPtr[]{ null }
        );
	public static portwritehandlerPtr volume_callback = new portwritehandlerPtr() { public void handler(int v)
        {
		K007232_set_volume(0,0,(v >> 4) * 0x11,0);
		K007232_set_volume(0,1,0,(v & 0x0f) * 0x11);
        }};
	static K007232_interface k007232_interface = new K007232_interface
	(
		1,		/* number of chips */
		new int[]{ REGION_SOUND1 },	/* memory regions */
		new int[]{ K007232_VOL(20,MIXER_PAN_CENTER,20,MIXER_PAN_CENTER) },	/* volume */
		new portwritehandlerPtr[]{ volume_callback }	/* external port callback */
        );
	
	static MachineDriver machine_driver_scontra = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_KONAMI,	/* 052001 */
				3000000,	/* ? */
				scontra_readmem,scontra_writemem,null,null,
				scontra_interrupt,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				3579545,		/* ? */
				scontra_readmem_sound,scontra_writemem_sound,null,null,
				ignore_interrupt,0	/* interrupts are triggered by the main CPU */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		scontra_init_machine,
	
		/* video hardware */
		64*8, 32*8, new rectangle( 14*8, (64-14)*8-1, 2*8, 30*8-1 ),
		null, /* gfx decoded by konamiic.c */
		1024, 1024,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		scontra_vh_start,
		scontra_vh_stop,
		scontra_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			),
			new MachineSound(
				SOUND_K007232,
				k007232_interface
			)
		}
	);
	
	static MachineDriver machine_driver_thunderx = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_KONAMI,
				3000000,		/* ? */
				thunderx_readmem,thunderx_writemem,null,null,
				thunderx_interrupt,16	/* ???? */
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				3579545,		/* ? */
				thunderx_readmem_sound,thunderx_writemem_sound,null,null,
				ignore_interrupt,0	/* interrupts are triggered by the main CPU */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		thunderx_init_machine,
	
		/* video hardware */
		64*8, 32*8, new rectangle( 14*8, (64-14)*8-1, 2*8, 30*8-1 ),
		null, /* gfx decoded by konamiic.c */
		1024, 1024,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		scontra_vh_start,
		scontra_vh_stop,
		scontra_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			)
		}
	);
	
	
	/***************************************************************************
	
	  Game ROMs
	
	***************************************************************************/
	
	static RomLoadPtr rom_scontra = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x30800, REGION_CPU1 );/* ROMs + banked RAM */
		ROM_LOAD( "e02.k11",     0x10000, 0x08000, 0xa61c0ead );/* banked ROM */
		ROM_CONTINUE(            0x08000, 0x08000 );			/* fixed ROM */
		ROM_LOAD( "e03.k13",     0x20000, 0x10000, 0x00b02622 );/* banked ROM */
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the SOUND CPU */
		ROM_LOAD( "775-c01.bin", 0x00000, 0x08000, 0x0ced785a );
	
		ROM_REGION( 0x100000, REGION_GFX1 );/* tiles */
		ROM_LOAD_GFX_EVEN( "775-a07a.bin", 0x00000, 0x20000, 0xe716bdf3 );/* tiles */
		ROM_LOAD_GFX_ODD(  "775-a07e.bin", 0x00000, 0x20000, 0x0986e3a5 );
		ROM_LOAD_GFX_EVEN( "775-f07c.bin", 0x40000, 0x10000, 0xb0b30915 );
		ROM_LOAD_GFX_ODD(  "775-f07g.bin", 0x40000, 0x10000, 0xfbed827d );
		ROM_LOAD_GFX_EVEN( "775-f07d.bin", 0x60000, 0x10000, 0xf184be8e );
		ROM_LOAD_GFX_ODD(  "775-f07h.bin", 0x60000, 0x10000, 0x7b56c348 );
		ROM_LOAD_GFX_EVEN( "775-a08a.bin", 0x80000, 0x20000, 0x3ddd11a4 );
		ROM_LOAD_GFX_ODD(  "775-a08e.bin", 0x80000, 0x20000, 0x1007d963 );
		ROM_LOAD_GFX_EVEN( "775-f08c.bin", 0xc0000, 0x10000, 0x53abdaec );
		ROM_LOAD_GFX_ODD(  "775-f08g.bin", 0xc0000, 0x10000, 0x3df85a6e );
		ROM_LOAD_GFX_EVEN( "775-f08d.bin", 0xe0000, 0x10000, 0x102dcace );
		ROM_LOAD_GFX_ODD(  "775-f08h.bin", 0xe0000, 0x10000, 0xad9d7016 );
	
		ROM_REGION( 0x100000, REGION_GFX2 );/* sprites */
		ROM_LOAD_GFX_EVEN( "775-a05a.bin", 0x00000, 0x10000, 0xa0767045 );/* sprites */
		ROM_LOAD_GFX_ODD(  "775-a05e.bin", 0x00000, 0x10000, 0x2f656f08 );
		ROM_LOAD_GFX_EVEN( "775-a05b.bin", 0x20000, 0x10000, 0xab8ad4fd );
		ROM_LOAD_GFX_ODD(  "775-a05f.bin", 0x20000, 0x10000, 0x1c0eb1b6 );
		ROM_LOAD_GFX_EVEN( "775-f05c.bin", 0x40000, 0x10000, 0x5647761e );
		ROM_LOAD_GFX_ODD(  "775-f05g.bin", 0x40000, 0x10000, 0xa1692cca );
		ROM_LOAD_GFX_EVEN( "775-f05d.bin", 0x60000, 0x10000, 0xad676a6f );
		ROM_LOAD_GFX_ODD(  "775-f05h.bin", 0x60000, 0x10000, 0x3f925bcf );
		ROM_LOAD_GFX_EVEN( "775-a06a.bin", 0x80000, 0x10000, 0x77a34ad0 );
		ROM_LOAD_GFX_ODD(  "775-a06e.bin", 0x80000, 0x10000, 0x8a910c94 );
		ROM_LOAD_GFX_EVEN( "775-a06b.bin", 0xa0000, 0x10000, 0x563fb565 );
		ROM_LOAD_GFX_ODD(  "775-a06f.bin", 0xa0000, 0x10000, 0xe14995c0 );
		ROM_LOAD_GFX_EVEN( "775-f06c.bin", 0xc0000, 0x10000, 0x5ee6f3c1 );
		ROM_LOAD_GFX_ODD(  "775-f06g.bin", 0xc0000, 0x10000, 0x2645274d );
		ROM_LOAD_GFX_EVEN( "775-f06d.bin", 0xe0000, 0x10000, 0xc8b764fa );
		ROM_LOAD_GFX_ODD(  "775-f06h.bin", 0xe0000, 0x10000, 0xd6595f59 );
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* k007232 data */
		ROM_LOAD( "775-a04a.bin", 0x00000, 0x10000, 0x7efb2e0f );
		ROM_LOAD( "775-a04b.bin", 0x10000, 0x10000, 0xf41a2b33 );
		ROM_LOAD( "775-a04c.bin", 0x20000, 0x10000, 0xe4e58f14 );
		ROM_LOAD( "775-a04d.bin", 0x30000, 0x10000, 0xd46736f6 );
		ROM_LOAD( "775-f04e.bin", 0x40000, 0x10000, 0xfbf7e363 );
		ROM_LOAD( "775-f04f.bin", 0x50000, 0x10000, 0xb031ef2d );
		ROM_LOAD( "775-f04g.bin", 0x60000, 0x10000, 0xee107bbb );
		ROM_LOAD( "775-f04h.bin", 0x70000, 0x10000, 0xfb0fab46 );
	
		ROM_REGION( 0x0100, REGION_PROMS );
		ROM_LOAD( "775a09.b19",   0x0000, 0x0100, 0x46d1e0df );/* priority encoder (not used) */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_scontraj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x30800, REGION_CPU1 );/* ROMs + banked RAM */
		ROM_LOAD( "775-f02.bin", 0x10000, 0x08000, 0x8d5933a7 );/* banked ROM */
		ROM_CONTINUE(            0x08000, 0x08000 );			/* fixed ROM */
		ROM_LOAD( "775-f03.bin", 0x20000, 0x10000, 0x1ef63d80 );/* banked ROM */
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the SOUND CPU */
		ROM_LOAD( "775-c01.bin", 0x00000, 0x08000, 0x0ced785a );
	
		ROM_REGION( 0x100000, REGION_GFX1 );/* tiles */
		ROM_LOAD_GFX_EVEN( "775-a07a.bin", 0x00000, 0x20000, 0xe716bdf3 );/* tiles */
		ROM_LOAD_GFX_ODD(  "775-a07e.bin", 0x00000, 0x20000, 0x0986e3a5 );
		ROM_LOAD_GFX_EVEN( "775-f07c.bin", 0x40000, 0x10000, 0xb0b30915 );
		ROM_LOAD_GFX_ODD(  "775-f07g.bin", 0x40000, 0x10000, 0xfbed827d );
		ROM_LOAD_GFX_EVEN( "775-f07d.bin", 0x60000, 0x10000, 0xf184be8e );
		ROM_LOAD_GFX_ODD(  "775-f07h.bin", 0x60000, 0x10000, 0x7b56c348 );
		ROM_LOAD_GFX_EVEN( "775-a08a.bin", 0x80000, 0x20000, 0x3ddd11a4 );
		ROM_LOAD_GFX_ODD(  "775-a08e.bin", 0x80000, 0x20000, 0x1007d963 );
		ROM_LOAD_GFX_EVEN( "775-f08c.bin", 0xc0000, 0x10000, 0x53abdaec );
		ROM_LOAD_GFX_ODD(  "775-f08g.bin", 0xc0000, 0x10000, 0x3df85a6e );
		ROM_LOAD_GFX_EVEN( "775-f08d.bin", 0xe0000, 0x10000, 0x102dcace );
		ROM_LOAD_GFX_ODD(  "775-f08h.bin", 0xe0000, 0x10000, 0xad9d7016 );
	
		ROM_REGION( 0x100000, REGION_GFX2 );/* sprites */
		ROM_LOAD_GFX_EVEN( "775-a05a.bin", 0x00000, 0x10000, 0xa0767045 );/* sprites */
		ROM_LOAD_GFX_ODD(  "775-a05e.bin", 0x00000, 0x10000, 0x2f656f08 );
		ROM_LOAD_GFX_EVEN( "775-a05b.bin", 0x20000, 0x10000, 0xab8ad4fd );
		ROM_LOAD_GFX_ODD(  "775-a05f.bin", 0x20000, 0x10000, 0x1c0eb1b6 );
		ROM_LOAD_GFX_EVEN( "775-f05c.bin", 0x40000, 0x10000, 0x5647761e );
		ROM_LOAD_GFX_ODD(  "775-f05g.bin", 0x40000, 0x10000, 0xa1692cca );
		ROM_LOAD_GFX_EVEN( "775-f05d.bin", 0x60000, 0x10000, 0xad676a6f );
		ROM_LOAD_GFX_ODD(  "775-f05h.bin", 0x60000, 0x10000, 0x3f925bcf );
		ROM_LOAD_GFX_EVEN( "775-a06a.bin", 0x80000, 0x10000, 0x77a34ad0 );
		ROM_LOAD_GFX_ODD(  "775-a06e.bin", 0x80000, 0x10000, 0x8a910c94 );
		ROM_LOAD_GFX_EVEN( "775-a06b.bin", 0xa0000, 0x10000, 0x563fb565 );
		ROM_LOAD_GFX_ODD(  "775-a06f.bin", 0xa0000, 0x10000, 0xe14995c0 );
		ROM_LOAD_GFX_EVEN( "775-f06c.bin", 0xc0000, 0x10000, 0x5ee6f3c1 );
		ROM_LOAD_GFX_ODD(  "775-f06g.bin", 0xc0000, 0x10000, 0x2645274d );
		ROM_LOAD_GFX_EVEN( "775-f06d.bin", 0xe0000, 0x10000, 0xc8b764fa );
		ROM_LOAD_GFX_ODD(  "775-f06h.bin", 0xe0000, 0x10000, 0xd6595f59 );
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* k007232 data */
		ROM_LOAD( "775-a04a.bin", 0x00000, 0x10000, 0x7efb2e0f );
		ROM_LOAD( "775-a04b.bin", 0x10000, 0x10000, 0xf41a2b33 );
		ROM_LOAD( "775-a04c.bin", 0x20000, 0x10000, 0xe4e58f14 );
		ROM_LOAD( "775-a04d.bin", 0x30000, 0x10000, 0xd46736f6 );
		ROM_LOAD( "775-f04e.bin", 0x40000, 0x10000, 0xfbf7e363 );
		ROM_LOAD( "775-f04f.bin", 0x50000, 0x10000, 0xb031ef2d );
		ROM_LOAD( "775-f04g.bin", 0x60000, 0x10000, 0xee107bbb );
		ROM_LOAD( "775-f04h.bin", 0x70000, 0x10000, 0xfb0fab46 );
	
		ROM_REGION( 0x0100, REGION_PROMS );
		ROM_LOAD( "775a09.b19",   0x0000, 0x0100, 0x46d1e0df );/* priority encoder (not used) */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_thunderx = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x29000, REGION_CPU1 );/* ROMs + banked RAM */
		ROM_LOAD( "873k03.k15", 0x10000, 0x10000, 0x276817ad );
		ROM_LOAD( "873k02.k13", 0x20000, 0x08000, 0x80cc1c45 );
		ROM_CONTINUE(           0x08000, 0x08000 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "873h01.f8",    0x0000, 0x8000, 0x990b7a7c );
	
		ROM_REGION( 0x80000, REGION_GFX1 );/* temporary space for graphics (disposed after conversion) */
		ROM_LOAD_GFX_EVEN( "873c06a.f6",   0x00000, 0x10000, 0x0e340b67 );/* Chars */
		ROM_LOAD_GFX_ODD ( "873c06c.f5",   0x00000, 0x10000, 0xef0e72cd );
		ROM_LOAD_GFX_EVEN( "873c06b.e6",   0x20000, 0x10000, 0x97ad202e );
		ROM_LOAD_GFX_ODD ( "873c06d.e5",   0x20000, 0x10000, 0x8393d42e );
		ROM_LOAD_GFX_EVEN( "873c07a.f4",   0x40000, 0x10000, 0xa8aab84f );
		ROM_LOAD_GFX_ODD ( "873c07c.f3",   0x40000, 0x10000, 0x2521009a );
		ROM_LOAD_GFX_EVEN( "873c07b.e4",   0x60000, 0x10000, 0x12a2b8ba );
		ROM_LOAD_GFX_ODD ( "873c07d.e3",   0x60000, 0x10000, 0xfae9f965 );
	
		ROM_REGION( 0x80000, REGION_GFX2 );
		ROM_LOAD_GFX_EVEN( "873c04a.f11",  0x00000, 0x10000, 0xf7740bf3 );/* Sprites */
		ROM_LOAD_GFX_ODD ( "873c04c.f10",  0x00000, 0x10000, 0x5dacbd2b );
		ROM_LOAD_GFX_EVEN( "873c04b.e11",  0x20000, 0x10000, 0x9ac581da );
		ROM_LOAD_GFX_ODD ( "873c04d.e10",  0x20000, 0x10000, 0x44a4668c );
		ROM_LOAD_GFX_EVEN( "873c05a.f9",   0x40000, 0x10000, 0xd73e107d );
		ROM_LOAD_GFX_ODD ( "873c05c.f8",   0x40000, 0x10000, 0x59903200 );
		ROM_LOAD_GFX_EVEN( "873c05b.e9",   0x60000, 0x10000, 0x81059b99 );
		ROM_LOAD_GFX_ODD ( "873c05d.e8",   0x60000, 0x10000, 0x7fa3d7df );
	
		ROM_REGION( 0x0100, REGION_PROMS );
		ROM_LOAD( "873a08.f20",   0x0000, 0x0100, 0xe2d09a1b );/* priority encoder (not used) */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_thnderxj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x29000, REGION_CPU1 );/* ROMs + banked RAM */
		ROM_LOAD( "873-n03.k15", 0x10000, 0x10000, 0xa01e2e3e );
		ROM_LOAD( "873-n02.k13", 0x20000, 0x08000, 0x55afa2cc );
		ROM_CONTINUE(            0x08000, 0x08000 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "873-f01.f8",   0x0000, 0x8000, 0xea35ffa3 );
	
		ROM_REGION( 0x80000, REGION_GFX1 );/* temporary space for graphics (disposed after conversion) */
		ROM_LOAD_GFX_EVEN( "873c06a.f6",   0x00000, 0x10000, 0x0e340b67 );/* Chars */
		ROM_LOAD_GFX_ODD ( "873c06c.f5",   0x00000, 0x10000, 0xef0e72cd );
		ROM_LOAD_GFX_EVEN( "873c06b.e6",   0x20000, 0x10000, 0x97ad202e );
		ROM_LOAD_GFX_ODD ( "873c06d.e5",   0x20000, 0x10000, 0x8393d42e );
		ROM_LOAD_GFX_EVEN( "873c07a.f4",   0x40000, 0x10000, 0xa8aab84f );
		ROM_LOAD_GFX_ODD ( "873c07c.f3",   0x40000, 0x10000, 0x2521009a );
		ROM_LOAD_GFX_EVEN( "873c07b.e4",   0x60000, 0x10000, 0x12a2b8ba );
		ROM_LOAD_GFX_ODD ( "873c07d.e3",   0x60000, 0x10000, 0xfae9f965 );
	
		ROM_REGION( 0x80000, REGION_GFX2 );
		ROM_LOAD_GFX_EVEN( "873c04a.f11",  0x00000, 0x10000, 0xf7740bf3 );/* Sprites */
		ROM_LOAD_GFX_ODD ( "873c04c.f10",  0x00000, 0x10000, 0x5dacbd2b );
		ROM_LOAD_GFX_EVEN( "873c04b.e11",  0x20000, 0x10000, 0x9ac581da );
		ROM_LOAD_GFX_ODD ( "873c04d.e10",  0x20000, 0x10000, 0x44a4668c );
		ROM_LOAD_GFX_EVEN( "873c05a.f9",   0x40000, 0x10000, 0xd73e107d );
		ROM_LOAD_GFX_ODD ( "873c05c.f8",   0x40000, 0x10000, 0x59903200 );
		ROM_LOAD_GFX_EVEN( "873c05b.e9",   0x60000, 0x10000, 0x81059b99 );
		ROM_LOAD_GFX_ODD ( "873c05d.e8",   0x60000, 0x10000, 0x7fa3d7df );
	
		ROM_REGION( 0x0100, REGION_PROMS );
		ROM_LOAD( "873a08.f20",   0x0000, 0x0100, 0xe2d09a1b );/* priority encoder (not used) */
	ROM_END(); }}; 
	
	/***************************************************************************/
	
/*TODO*///	static void thunderx_banking( int lines )
/*TODO*///	{
/*TODO*///		unsigned char *RAM = memory_region(REGION_CPU1);
/*TODO*///		int offs;
	
	//	if ( errorlog )
	//		fprintf( errorlog, "thunderx %04x: bank select %02x\n", cpu_get_pc(), lines );
	
/*TODO*///		offs = 0x10000 + (((lines & 0x0f) ^ 0x08) * 0x2000);
/*TODO*///		if (offs >= 0x28000) offs -= 0x20000;
/*TODO*///		cpu_setbank( 1, &RAM[offs] );
/*TODO*///	}
	

	
	public static InitDriverPtr init_scontra = new InitDriverPtr() { public void handler() 
	{
		konami_rom_deinterleave_2(REGION_GFX1);
		konami_rom_deinterleave_2(REGION_GFX2);
	} };
	
	
	
	public static GameDriver driver_scontra	   = new GameDriver("1988"	,"scontra"	,"thunderx.java"	,rom_scontra,null	,machine_driver_scontra	,input_ports_scontra	,init_scontra	,ROT90	,	"Konami", "Super Contra" );
	public static GameDriver driver_scontraj	   = new GameDriver("1988"	,"scontraj"	,"thunderx.java"	,rom_scontraj,driver_scontra	,machine_driver_scontra	,input_ports_scontra	,init_scontra	,ROT90	,	"Konami", "Super Contra (Japan)" );
/*TODO*///	GAMEX(1988, thunderx, null,        thunderx, thunderx, scontra, ROT0, "Konami", "Thunder Cross", GAME_NOT_WORKING )
/*TODO*///	GAMEX(1988, thnderxj, thunderx, thunderx, thunderx, scontra, ROT0, "Konami", "Thunder Cross (Japan)", GAME_NOT_WORKING )
}
