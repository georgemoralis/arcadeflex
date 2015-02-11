/***************************************************************************

  The Main Event, (c) 1988 Konami
  Devastators, (c) 1988 Konami

Emulation by Bryan McPhail, mish@tendril.co.uk

Notes:
* Schematics show a palette/work RAM bank selector, but this doesn't seem
  to be used?
* Sprite priorities are not correct (ropes appear behind fighters).
  Sprite/sprite priorities have to be orthogonal to sprite/tile priorities
  for this to work correctly.
  This is fixed with a kludge in mainevt_sprite_callback().

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers.WIP;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.commonH.*;
import static mame.inputport.*;
import static arcadeflex.ptrlib.*;
import static mame.drawgfxH.*;
import static vidhrdw.generic.*;
import static mame.cpuintrf.*;
import static mame.cpuintrfH.*;
import static mame.inputportH.*;
import static mame.mame.*;
import static arcadeflex.libc_old.*;
import static arcadeflex.libc.*;
import static mame.sndintrf.soundlatch_r;
import static mame.sndintrf.soundlatch_w;
import static cpu.m6809.m6809H.*;
import static cpu.z80.z80H.*;
import static mame.common.*;
import static mame.commonH.*;
import static mame.palette.*;
import static mame.memory.*;
import mame.sndintrfH.MachineSound;
import static mame.sndintrfH.SOUND_K007232;
import static mame.sndintrfH.SOUND_UPD7759;
import static mame.sndintrfH.SOUND_YM2151;
import static vidhrdw.mainevt.*;
import static vidhrdw.konamiic.*;
import static sound.k007232.*;
import static sound.k007232H.*;
import static sound._2151intf.*;
import static sound._2151intfH.*;
import static sound.mixerH.*;
import static sound.upd7759.*;
import static sound.upd7759H.*;

public class mainevt
{
		
	public static InterruptPtr mainevt_interrupt = new InterruptPtr() { public int handler() 
	{
		if (K052109_is_IRQ_enabled()!=0) return M6809_INT_IRQ;
		else return ignore_interrupt.handler();
	} };
	
	
	static int nmi_enable;
	
	public static WriteHandlerPtr dv_nmienable_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		nmi_enable = data;
	} };
	
	public static InterruptPtr dv_interrupt = new InterruptPtr() { public int handler() 
	{
		if (nmi_enable!=0) return M6809_INT_NMI;
		else return ignore_interrupt.handler();
	} };
	
	
	public static ReadHandlerPtr zero_ret = new ReadHandlerPtr() { public int handler(int offset)
	{
		return 0xff;
	} };
	
	
	public static WriteHandlerPtr mainevt_bankswitch_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
		int bankaddress;
	
		/* bit 0-1 ROM bank select */
		bankaddress = 0x10000 + (data & 0x03) * 0x2000;
		cpu_setbank(1,new UBytePtr(RAM,bankaddress));
	
		/* TODO: bit 5 = select work RAM or palette? */
	//	palette_selected = data & 0x20;
	
		/* bit 6 = enable char ROM reading through the video RAM */
		K052109_set_RMRD_line((data & 0x40)!=0 ? ASSERT_LINE : CLEAR_LINE);
	
		/* bit 7 = NINITSET (unknown) */
	
		/* other bits unused */
	} };
	
	public static WriteHandlerPtr mainevt_coin_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		coin_counter_w.handler(0,data & 0x10);
		coin_counter_w.handler(1,data & 0x20);
		//osd_led_w(0,data >> 0);
		//osd_led_w(1,data >> 1);
		//osd_led_w(2,data >> 2);
		//osd_led_w(3,data >> 3);
	} };
	
	public static WriteHandlerPtr mainevt_sh_irqtrigger_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		cpu_cause_interrupt(1,0xff);
	} };
	
	public static WriteHandlerPtr mainevt_sh_irqcontrol_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	 	/* I think bit 1 resets the UPD7795C sound chip */
	 	if ((data & 0x02) == 0)
	 	{
	 		UPD7759_reset_w.handler(0,(data & 0x02) >> 1);
	 	}
		else if ((data & 0x01)!=0)
	 	{
			UPD7759_start_w.handler(0,0);
	 	}
	
		interrupt_enable_w.handler(0,data & 4);
	} };
	
	public static WriteHandlerPtr devstor_sh_irqcontrol_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
            interrupt_enable_w.handler(0,data & 4);
	} };
	
	public static WriteHandlerPtr mainevt_sh_bankswitch_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		UBytePtr src;
                UBytePtr dest;
		UBytePtr RAM = memory_region(REGION_SOUND1);
		int bank_A,bank_B;
	
	//if (errorlog) fprintf(errorlog,"CPU #1 PC: %04x bank switch = %02x\n",cpu_get_pc(),data);
	
		/* bits 0-3 select the 007232 banks */
		bank_A=0x20000 * (data&0x3);
		bank_B=0x20000 * ((data>>2)&0x3);
                K007232_bankswitch(0,new UBytePtr(RAM,bank_A),new UBytePtr(RAM,bank_B));
	
		/* bits 4-5 select the UPD7759 bank */
		src = new UBytePtr(memory_region(REGION_SOUND2),0x20000);  //TODO recheck
		dest = memory_region(REGION_SOUND2);                       //TODO recheck
		//memcpy(dest,&src[((data >> 4) & 0x03) * 0x20000],0x20000);
                for(int i=0; i<0x20000; i++)
                {
                    dest.write(i, src.read(i+(((data >> 4) & 0x03) * 0x20000)));  //TODO recheck
                }
	} };
	
	public static WriteHandlerPtr dv_sh_bankswitch_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_SOUND1);
		int bank_A,bank_B;
	
	//if (errorlog) fprintf(errorlog,"CPU #1 PC: %04x bank switch = %02x\n",cpu_get_pc(),data);
	
		/* bits 0-3 select the 007232 banks */
		bank_A=0x20000 * (data&0x3);
		bank_B=0x20000 * ((data>>2)&0x3);
		K007232_bankswitch(0,new UBytePtr(RAM,bank_A),new UBytePtr(RAM,bank_B));
	} };
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x1f94, 0x1f94, input_port_0_r ), /* Coins */
		new MemoryReadAddress( 0x1f95, 0x1f95, input_port_1_r ), /* Player 1 */
		new MemoryReadAddress( 0x1f96, 0x1f96, input_port_2_r ), /* Player 2 */
		new MemoryReadAddress( 0x1f97, 0x1f97, input_port_5_r ), /* Dip 1 */
		new MemoryReadAddress( 0x1f98, 0x1f98, input_port_7_r ), /* Dip 3 */
		new MemoryReadAddress( 0x1f99, 0x1f99, input_port_3_r ), /* Player 3 */
		new MemoryReadAddress( 0x1f9a, 0x1f9a, input_port_4_r ), /* Player 4 */
		new MemoryReadAddress( 0x1f9b, 0x1f9b, input_port_6_r ), /* Dip 2 */
	
		new MemoryReadAddress( 0x0000, 0x3fff, K052109_051960_r ),
		new MemoryReadAddress( 0x4000, 0x5fff, MRA_RAM ),
		new MemoryReadAddress( 0x6000, 0x7fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x1f80, 0x1f80, mainevt_bankswitch_w ),
		new MemoryWriteAddress( 0x1f84, 0x1f84, soundlatch_w ),				/* probably */
		new MemoryWriteAddress( 0x1f88, 0x1f88, mainevt_sh_irqtrigger_w ),	/* probably */
		new MemoryWriteAddress( 0x1f8c, 0x1f8d, MWA_NOP ),	/* ??? */
		new MemoryWriteAddress( 0x1f90, 0x1f90, mainevt_coin_w ),	/* coin counters + lamps */
	
		new MemoryWriteAddress( 0x0000, 0x3fff, K052109_051960_w ),
		new MemoryWriteAddress( 0x4000, 0x5dff, MWA_RAM ),
		new MemoryWriteAddress( 0x5e00, 0x5fff, paletteram_xBBBBBGGGGGRRRRR_swap_w, paletteram ),
	 	new MemoryWriteAddress( 0x6000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	static MemoryReadAddress dv_readmem[] =
	{
		new MemoryReadAddress( 0x1f94, 0x1f94, input_port_0_r ), /* Coins */
		new MemoryReadAddress( 0x1f95, 0x1f95, input_port_1_r ), /* Player 1 */
		new MemoryReadAddress( 0x1f96, 0x1f96, input_port_2_r ), /* Player 2 */
		new MemoryReadAddress( 0x1f97, 0x1f97, input_port_5_r ), /* Dip 1 */
		new MemoryReadAddress( 0x1f98, 0x1f98, input_port_7_r ), /* Dip 3 */
		new MemoryReadAddress( 0x1f9b, 0x1f9b, input_port_6_r ), /* Dip 2 */
		new MemoryReadAddress( 0x1fa0, 0x1fbf, K051733_r ),
	
		new MemoryReadAddress( 0x0000, 0x3fff, K052109_051960_r ),
		new MemoryReadAddress( 0x4000, 0x5fff, MRA_RAM ),
		new MemoryReadAddress( 0x6000, 0x7fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress dv_writemem[] =
	{
		new MemoryWriteAddress( 0x1f80, 0x1f80, mainevt_bankswitch_w ),
		new MemoryWriteAddress( 0x1f84, 0x1f84, soundlatch_w ),				/* probably */
		new MemoryWriteAddress( 0x1f88, 0x1f88, mainevt_sh_irqtrigger_w ),	/* probably */
		new MemoryWriteAddress( 0x1f90, 0x1f90, mainevt_coin_w ),	/* coin counters + lamps */
		new MemoryWriteAddress( 0x1fb2, 0x1fb2, dv_nmienable_w ),
		new MemoryWriteAddress( 0x1fa0, 0x1fbf, K051733_w ),
	
		new MemoryWriteAddress( 0x0000, 0x3fff, K052109_051960_w ),
		new MemoryWriteAddress( 0x4000, 0x5dff, MWA_RAM ),
		new MemoryWriteAddress( 0x5e00, 0x5fff, paletteram_xBBBBBGGGGGRRRRR_swap_w, paletteram ),
		new MemoryWriteAddress( 0x6000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x83ff, MRA_RAM ),
		new MemoryReadAddress( 0xa000, 0xa000, soundlatch_r ),
		new MemoryReadAddress( 0xb000, 0xb00d, K007232_read_port_0_r ),
		new MemoryReadAddress( 0xd000, 0xd000, UPD7759_busy_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0x83ff, MWA_RAM ),
		new MemoryWriteAddress( 0xb000, 0xb00d, K007232_write_port_0_w ),
		new MemoryWriteAddress( 0x9000, 0x9000, UPD7759_message_w ),
		new MemoryWriteAddress( 0xe000, 0xe000, mainevt_sh_irqcontrol_w ),
		new MemoryWriteAddress( 0xf000, 0xf000, mainevt_sh_bankswitch_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress dv_sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x83ff, MRA_RAM ),
		new MemoryReadAddress( 0xa000, 0xa000, soundlatch_r ),
		new MemoryReadAddress( 0xb000, 0xb00d, K007232_read_port_0_r ),
		new MemoryReadAddress( 0xc001, 0xc001, YM2151_status_port_0_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress dv_sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0x83ff, MWA_RAM ),
		new MemoryWriteAddress( 0xb000, 0xb00d, K007232_write_port_0_w ),
		new MemoryWriteAddress( 0xc000, 0xc000, YM2151_register_port_0_w ),
		new MemoryWriteAddress( 0xc001, 0xc001, YM2151_data_port_0_w ),
		new MemoryWriteAddress( 0xe000, 0xe000, devstor_sh_irqcontrol_w ),
		new MemoryWriteAddress( 0xf000, 0xf000, dv_sh_bankswitch_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	
	/*****************************************************************************/
	
	static InputPortPtr input_ports_mainevt = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN4 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_SERVICE2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_SERVICE3 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_SERVICE4 );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER4 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER4 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER4 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_5C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
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
	
	 	PORT_START(); 
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x18, 0x10, "Bonus Energy" );
		PORT_DIPSETTING(    0x00, "60" );
		PORT_DIPSETTING(    0x08, "70" );
		PORT_DIPSETTING(    0x10, "80" );
		PORT_DIPSETTING(    0x18, "90" );
		PORT_DIPNAME( 0x60, 0x40, DEF_STR( "Difficulty") );
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
	
	static InputPortPtr input_ports_ringohja = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN4 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_5C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_5C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
	
	 	PORT_START(); 
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x18, 0x10, "Bonus Energy" );
		PORT_DIPSETTING(    0x00, "60" );
		PORT_DIPSETTING(    0x08, "70" );
		PORT_DIPSETTING(    0x10, "80" );
		PORT_DIPSETTING(    0x18, "90" );
		PORT_DIPNAME( 0x60, 0x40, DEF_STR( "Difficulty") );
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
	
	static InputPortPtr input_ports_devstors = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START(); 
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 
		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNUSED );
	
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
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x18, "150 and every 200" );
		PORT_DIPSETTING(    0x10, "150 and every 250" );
		PORT_DIPSETTING(    0x08, "150" );
		PORT_DIPSETTING(    0x00, "200" );
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
	
	/*****************************************************************************/

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
	
	static UPD7759_interface upd7759_interface = new UPD7759_interface
	(
		1,		/* number of chips */
		UPD7759_STANDARD_CLOCK,
		new int[]{ 50 }, /* volume */
		new int[]{ REGION_SOUND2 },		/* memory region */
		UPD7759_STANDALONE_MODE,		/* chip mode */
		new irqcallbackPtr[]{null}
        );
	
	static YM2151interface ym2151_interface = new YM2151interface
        (
		1,			/* 1 chip */
                3579545,	/* 3.579545 MHz */
		new int[]{ YM3012_VOL(30,MIXER_PAN_CENTER,30,MIXER_PAN_CENTER) },
		new WriteYmHandlerPtr[]{ null },
		new WriteHandlerPtr[]{ null }
        );
	
	static MachineDriver machine_driver_mainevt = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
	 		new MachineCPU(
				CPU_HD6309,
				3000000,	/* ?? */
				readmem,writemem,null,null,
				mainevt_interrupt,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				3579545,	/* 3.579545 MHz */
				sound_readmem,sound_writemem,null,null,
				nmi_interrupt,8	/* ??? */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		64*8, 32*8, new rectangle( 14*8, (64-14)*8-1, 2*8, 30*8-1 ),
		null,	/* gfx decoded by konamiic.c */
		256, 256,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		mainevt_vh_start,
		mainevt_vh_stop,
		mainevt_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_K007232,
				k007232_interface
			),
			new MachineSound(
				SOUND_UPD7759,
				upd7759_interface
			)
		}
	);
	
	static MachineDriver machine_driver_devstors = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
	 		new MachineCPU(
				CPU_HD6309,
				3000000,	/* ?? */
				dv_readmem,dv_writemem,null,null,
				dv_interrupt,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				3579545,	/* 3.579545 MHz */
				dv_sound_readmem,dv_sound_writemem,null,null,
				interrupt,4
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		64*8, 32*8, new rectangle( 13*8, (64-13)*8-1, 2*8, 30*8-1 ),
		null,	/* gfx decoded by konamiic.c */
		256, 256,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		dv_vh_start,
		mainevt_vh_stop,
		dv_vh_screenrefresh,
	
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
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_mainevt = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );
		ROM_LOAD( "799c02.k11",   0x10000, 0x08000, 0xe2e7dbd5 );
		ROM_CONTINUE(             0x08000, 0x08000 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "799c01.f7",    0x00000, 0x08000, 0x447c4c5c );
	
		ROM_REGION( 0x20000, REGION_GFX1 );/* graphics (addressable by the main CPU) */
		ROM_LOAD_GFX_EVEN( "799c06.f22",   0x00000, 0x08000, 0xf839cb58 );
		ROM_LOAD_GFX_ODD ( "799c07.h22",   0x00000, 0x08000, 0x176df538 );
		ROM_LOAD_GFX_EVEN( "799c08.j22",   0x10000, 0x08000, 0xd01e0078 );
		ROM_LOAD_GFX_ODD ( "799c09.k22",   0x10000, 0x08000, 0x9baec75e );
	
		ROM_REGION( 0x100000, REGION_GFX2 );/* graphics (addressable by the main CPU) */
		ROM_LOAD( "799b04.h4",    0x00000, 0x80000, 0x323e0c2b );
		ROM_LOAD( "799b05.k4",    0x80000, 0x80000, 0x571c5831 );
	
		ROM_REGION( 0x0100, REGION_PROMS );
		ROM_LOAD( "63s141n.bin",  0x0000, 0x0100, 0x61f6c8d1 );/* priority encoder (not used) */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* 512k for 007232 samples */
		ROM_LOAD( "799b03.d4",    0x00000, 0x80000, 0xf1cfd342 );
	
		ROM_REGION( 0xa0000, REGION_SOUND2 );/* 128+512k for the UPD7759C samples */
		/* 00000-1ffff space where the following ROM is bank switched */
		ROM_LOAD( "799b06.c22",   0x20000, 0x80000, 0x2c8c47d7 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_mainevt2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );
		ROM_LOAD( "02",           0x10000, 0x08000, 0xc143596b );
		ROM_CONTINUE(             0x08000, 0x08000 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "799c01.f7",    0x00000, 0x08000, 0x447c4c5c );
	
		ROM_REGION( 0x20000, REGION_GFX1 );/* graphics (addressable by the main CPU) */
		ROM_LOAD_GFX_EVEN( "799c06.f22",   0x00000, 0x08000, 0xf839cb58 );
		ROM_LOAD_GFX_ODD ( "799c07.h22",   0x00000, 0x08000, 0x176df538 );
		ROM_LOAD_GFX_EVEN( "799c08.j22",   0x10000, 0x08000, 0xd01e0078 );
		ROM_LOAD_GFX_ODD ( "799c09.k22",   0x10000, 0x08000, 0x9baec75e );
	
		ROM_REGION( 0x100000, REGION_GFX2 );/* graphics (addressable by the main CPU) */
		ROM_LOAD( "799b04.h4",    0x00000, 0x80000, 0x323e0c2b );
		ROM_LOAD( "799b05.k4",    0x80000, 0x80000, 0x571c5831 );
	
		ROM_REGION( 0x0100, REGION_PROMS );
		ROM_LOAD( "63s141n.bin",  0x0000, 0x0100, 0x61f6c8d1 );/* priority encoder (not used) */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* 512k for 007232 samples */
		ROM_LOAD( "799b03.d4",    0x00000, 0x80000, 0xf1cfd342 );
	
		ROM_REGION( 0xa0000, REGION_SOUND2 );/* 128+512k for the UPD7759C samples */
		/* 00000-1ffff space where the following ROM is bank switched */
		ROM_LOAD( "799b06.c22",   0x20000, 0x80000, 0x2c8c47d7 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ringohja = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );
		ROM_LOAD( "799n02.k11",   0x10000, 0x08000, 0xf9305dd0 );
		ROM_CONTINUE(             0x08000, 0x08000 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "799c01.f7",    0x00000, 0x08000, 0x447c4c5c );
	
		ROM_REGION( 0x20000, REGION_GFX1 );/* graphics (addressable by the main CPU) */
		ROM_LOAD_GFX_EVEN( "799c06.f22",   0x00000, 0x08000, 0xf839cb58 );
		ROM_LOAD_GFX_ODD ( "799c07.h22",   0x00000, 0x08000, 0x176df538 );
		ROM_LOAD_GFX_EVEN( "799c08.j22",   0x10000, 0x08000, 0xd01e0078 );
		ROM_LOAD_GFX_ODD ( "799c09.k22",   0x10000, 0x08000, 0x9baec75e );
	
		ROM_REGION( 0x100000, REGION_GFX2 );/* graphics (addressable by the main CPU) */
		ROM_LOAD( "799b04.h4",    0x00000, 0x80000, 0x323e0c2b );
		ROM_LOAD( "799b05.k4",    0x80000, 0x80000, 0x571c5831 );
	
		ROM_REGION( 0x0100, REGION_PROMS );
		ROM_LOAD( "63s141n.bin",  0x0000, 0x0100, 0x61f6c8d1 );/* priority encoder (not used) */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* 512k for 007232 samples */
		ROM_LOAD( "799b03.d4",    0x00000, 0x80000, 0xf1cfd342 );
	
		ROM_REGION( 0xa0000, REGION_SOUND2 );/* 128+512k for the UPD7759C samples */
		/* 00000-1ffff space where the following ROM is bank switched */
		ROM_LOAD( "799b06.c22",   0x20000, 0x80000, 0x2c8c47d7 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_devstors = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );
		ROM_LOAD( "890-z02.k11",  0x10000, 0x08000, 0xebeb306f );
		ROM_CONTINUE(             0x08000, 0x08000 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "dev-k01.rom",  0x00000, 0x08000, 0xd44b3eb0 );
	
		ROM_REGION( 0x40000, REGION_GFX1 );/* graphics (addressable by the main CPU) */
		ROM_LOAD_GFX_EVEN( "dev-f06.rom",  0x00000, 0x10000, 0x26592155 );
		ROM_LOAD_GFX_ODD ( "dev-f07.rom",  0x00000, 0x10000, 0x6c74fa2e );
		ROM_LOAD_GFX_EVEN( "dev-f08.rom",  0x20000, 0x10000, 0x29e12e80 );
		ROM_LOAD_GFX_ODD ( "dev-f09.rom",  0x20000, 0x10000, 0x67ca40d5 );
	
		ROM_REGION( 0x100000, REGION_GFX2 );/* graphics (addressable by the main CPU) */
		ROM_LOAD( "dev-f04.rom",  0x00000, 0x80000, 0xf16cd1fa );
		ROM_LOAD( "dev-f05.rom",  0x80000, 0x80000, 0xda37db05 );
	
		ROM_REGION( 0x0100, REGION_PROMS );
		ROM_LOAD( "devaprom.bin", 0x0000, 0x0100, 0xd3620106 );/* priority encoder (not used) */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* 512k for 007232 samples */
	 	ROM_LOAD( "dev-f03.rom",  0x00000, 0x80000, 0x19065031 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_devstor2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );
		ROM_LOAD( "dev-x02.rom",  0x10000, 0x08000, 0xe58ebb35 );
		ROM_CONTINUE(             0x08000, 0x08000 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "dev-k01.rom",  0x00000, 0x08000, 0xd44b3eb0 );
	
		ROM_REGION( 0x40000, REGION_GFX1 );/* graphics (addressable by the main CPU) */
		ROM_LOAD_GFX_EVEN( "dev-f06.rom",  0x00000, 0x10000, 0x26592155 );
		ROM_LOAD_GFX_ODD ( "dev-f07.rom",  0x00000, 0x10000, 0x6c74fa2e );
		ROM_LOAD_GFX_EVEN( "dev-f08.rom",  0x20000, 0x10000, 0x29e12e80 );
		ROM_LOAD_GFX_ODD ( "dev-f09.rom",  0x20000, 0x10000, 0x67ca40d5 );
	
		ROM_REGION( 0x100000, REGION_GFX2 );/* graphics (addressable by the main CPU) */
		ROM_LOAD( "dev-f04.rom",  0x00000, 0x80000, 0xf16cd1fa );
		ROM_LOAD( "dev-f05.rom",  0x80000, 0x80000, 0xda37db05 );
	
		ROM_REGION( 0x0100, REGION_PROMS );
		ROM_LOAD( "devaprom.bin", 0x0000, 0x0100, 0xd3620106 );/* priority encoder (not used) */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* 512k for 007232 samples */
	 	ROM_LOAD( "dev-f03.rom",  0x00000, 0x80000, 0x19065031 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_devstor3 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );
		ROM_LOAD( "890k02.k11",   0x10000, 0x08000, 0x52f4ccdd );
		ROM_CONTINUE(             0x08000, 0x08000 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "dev-k01.rom",  0x00000, 0x08000, 0xd44b3eb0 );
	
		ROM_REGION( 0x40000, REGION_GFX1 );/* graphics (addressable by the main CPU) */
		ROM_LOAD_GFX_EVEN( "dev-f06.rom",  0x00000, 0x10000, 0x26592155 );
		ROM_LOAD_GFX_ODD ( "dev-f07.rom",  0x00000, 0x10000, 0x6c74fa2e );
		ROM_LOAD_GFX_EVEN( "dev-f08.rom",  0x20000, 0x10000, 0x29e12e80 );
		ROM_LOAD_GFX_ODD ( "dev-f09.rom",  0x20000, 0x10000, 0x67ca40d5 );
	
		ROM_REGION( 0x100000, REGION_GFX2 );/* graphics (addressable by the main CPU) */
		ROM_LOAD( "dev-f04.rom",  0x00000, 0x80000, 0xf16cd1fa );
		ROM_LOAD( "dev-f05.rom",  0x80000, 0x80000, 0xda37db05 );
	
		ROM_REGION( 0x0100, REGION_PROMS );
		ROM_LOAD( "devaprom.bin", 0x0000, 0x0100, 0xd3620106 );/* priority encoder (not used) */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* 512k for 007232 samples */
	 	ROM_LOAD( "dev-f03.rom",  0x00000, 0x80000, 0x19065031 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_garuka = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );
		ROM_LOAD( "890w02.bin",   0x10000, 0x08000, 0xb2f6f538 );
		ROM_CONTINUE(             0x08000, 0x08000 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "dev-k01.rom",  0x00000, 0x08000, 0xd44b3eb0 );
	
		ROM_REGION( 0x40000, REGION_GFX1 );/* graphics (addressable by the main CPU) */
		ROM_LOAD_GFX_EVEN( "dev-f06.rom",  0x00000, 0x10000, 0x26592155 );
		ROM_LOAD_GFX_ODD ( "dev-f07.rom",  0x00000, 0x10000, 0x6c74fa2e );
		ROM_LOAD_GFX_EVEN( "dev-f08.rom",  0x20000, 0x10000, 0x29e12e80 );
		ROM_LOAD_GFX_ODD ( "dev-f09.rom",  0x20000, 0x10000, 0x67ca40d5 );
	
		ROM_REGION( 0x100000, REGION_GFX2 );/* graphics (addressable by the main CPU) */
		ROM_LOAD( "dev-f04.rom",  0x00000, 0x80000, 0xf16cd1fa );
		ROM_LOAD( "dev-f05.rom",  0x80000, 0x80000, 0xda37db05 );
	
		ROM_REGION( 0x0100, REGION_PROMS );
		ROM_LOAD( "devaprom.bin", 0x0000, 0x0100, 0xd3620106 );/* priority encoder (not used) */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* 512k for 007232 samples */
	 	ROM_LOAD( "dev-f03.rom",  0x00000, 0x80000, 0x19065031 );
	ROM_END(); }}; 
	
	
	
	public static InitDriverPtr init_mainevt = new InitDriverPtr() { public void handler() 
	{
		konami_rom_deinterleave_2(REGION_GFX1);
		konami_rom_deinterleave_2(REGION_GFX2);
	} };
	
	
	
	public static GameDriver driver_mainevt	   = new GameDriver("1988"	,"mainevt"	,"mainevt.java"	,rom_mainevt,null	,machine_driver_mainevt	,input_ports_mainevt	,init_mainevt	,ROT0	,	"Konami", "The Main Event (version Y)" );
	public static GameDriver driver_mainevt2	   = new GameDriver("1988"	,"mainevt2"	,"mainevt.java"	,rom_mainevt2,driver_mainevt	,machine_driver_mainevt	,input_ports_mainevt	,init_mainevt	,ROT0	,	"Konami", "The Main Event (version F)" );
	public static GameDriver driver_ringohja	   = new GameDriver("1988"	,"ringohja"	,"mainevt.java"	,rom_ringohja,driver_mainevt	,machine_driver_mainevt	,input_ports_ringohja	,init_mainevt	,ROT0	,	"Konami", "Ring no Ohja (Japan)" );
	public static GameDriver driver_devstors	   = new GameDriver("1988"	,"devstors"	,"mainevt.java"	,rom_devstors,null	,machine_driver_devstors	,input_ports_devstors	,init_mainevt	,ROT90	,	"Konami", "Devastators (version Z)" );
	public static GameDriver driver_devstor2	   = new GameDriver("1988"	,"devstor2"	,"mainevt.java"	,rom_devstor2,driver_devstors	,machine_driver_devstors	,input_ports_devstors	,init_mainevt	,ROT90	,	"Konami", "Devastators (version X)" );
	public static GameDriver driver_devstor3	   = new GameDriver("1988"	,"devstor3"	,"mainevt.java"	,rom_devstor3,driver_devstors	,machine_driver_devstors	,input_ports_devstors	,init_mainevt	,ROT90	,	"Konami", "Devastators (version V)" );
	public static GameDriver driver_garuka	   = new GameDriver("1988"	,"garuka"	,"mainevt.java"	,rom_garuka,driver_devstors	,machine_driver_devstors	,input_ports_devstors	,init_mainevt	,ROT90	,	"Konami", "Garuka (Japan)" );
}
