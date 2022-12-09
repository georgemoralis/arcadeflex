/***************************************************************************

  machine\qix.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.machine;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import arcadeflex.v036.generic.funcPtr.TimerCallbackHandlerPtr;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.inptport.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.timer.*;
import static arcadeflex.v036.mame.timerH.*;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;
import static arcadeflex.v036.sound.dac.*;
import static gr.codebb.arcadeflex.v036.machine._6821pia.*;
import static gr.codebb.arcadeflex.v036.machine._6812piaH.*;



public class qix
{
	static int suspended;
	
	static int sdungeon_coinctrl;
	
	
	public static UBytePtr qix_sharedram=new UBytePtr();
	
	
	public static ReadHandlerPtr qix_sharedram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return qix_sharedram.read(offset);
	} };
	
	
	public static WriteHandlerPtr qix_sharedram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		qix_sharedram.write(offset,data);
	} };
	
	
	public static WriteHandlerPtr zoo_bankswitch_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU2);
	
	
		if ((data & 0x04) != 0) cpu_setbank (1, new UBytePtr(RAM,0x10000));
		else cpu_setbank (1, new UBytePtr(RAM,0xa000));
	} };
	
	
	public static WriteHandlerPtr qix_video_firq_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* generate firq for video cpu */
		cpu_cause_interrupt(1,M6809_INT_FIRQ);
	} };
	
	
	
	public static WriteHandlerPtr qix_data_firq_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* generate firq for data cpu */
		cpu_cause_interrupt(0,M6809_INT_FIRQ);
	} };
	
	
	
	/* Return the current video scan line */
	public static ReadHandlerPtr qix_scanline_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* The +80&0xff thing is a hack to avoid flicker in Electric Yo-Yo */
		return (cpu_scalebyfcount(256) + 80) & 0xff;
	} };
	
	public static InitMachineHandlerPtr withmcu_init_machine = new InitMachineHandlerPtr() { public void handler() 
	{
		suspended = 0;
	
		pia_unconfig();
		pia_config(0, PIA_STANDARD_ORDERING | PIA_8BIT, qixmcu_pia_0_intf);
		pia_config(1, PIA_STANDARD_ORDERING | PIA_8BIT, qix_pia_1_intf);
		pia_config(2, PIA_STANDARD_ORDERING | PIA_8BIT, qixmcu_pia_2_intf);
		pia_config(3, PIA_STANDARD_ORDERING | PIA_8BIT, qix_pia_3_intf);
		pia_config(4, PIA_STANDARD_ORDERING | PIA_8BIT, qix_pia_4_intf);
		pia_config(5, PIA_STANDARD_ORDERING | PIA_8BIT, qix_pia_5_intf);
		pia_reset();
	
		sdungeon_coinctrl = 0x00;
	} };
	
	public static InitMachineHandlerPtr qix_init_machine = new InitMachineHandlerPtr() { public void handler() 
	{
		suspended = 0;
	
		pia_unconfig();
		pia_config(0, PIA_STANDARD_ORDERING | PIA_8BIT, qix_pia_0_intf);
		pia_config(1, PIA_STANDARD_ORDERING | PIA_8BIT, qix_pia_1_intf);
		pia_config(2, PIA_STANDARD_ORDERING | PIA_8BIT, qix_pia_2_intf);
		pia_config(3, PIA_STANDARD_ORDERING | PIA_8BIT, qix_pia_3_intf);
		pia_config(4, PIA_STANDARD_ORDERING | PIA_8BIT, qix_pia_4_intf);
		pia_config(5, PIA_STANDARD_ORDERING | PIA_8BIT, qix_pia_5_intf);
		pia_reset();
	
		sdungeon_coinctrl = 0x00;
	} };
	
	public static InitMachineHandlerPtr zoo_init_machine = new InitMachineHandlerPtr() { public void handler() 
	{
		withmcu_init_machine.handler();
	} };
	
	
	/***************************************************************************
	
		6821 PIA handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr qix_dac_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		DAC_data_w.handler(0, data);
	} };
	
	public static ReadHandlerPtr qix_sound_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* if we've suspended the main CPU for this, trigger it and give up some of our timeslice */
		if (suspended != 0)
		{
			timer_trigger (500);
			cpu_yielduntil_time (TIME_IN_USEC (100));
			suspended = 0;
		}
		return pia_4_porta_r.handler(offset);
	} };
	public static irqfuncPtr qix_pia_dint = new irqfuncPtr() { public void handler(int state)
	{
		/* not used by Qix, but others might use it; depends on a jumper on the PCB */
	}};
	public static irqfuncPtr qix_pia_sint = new irqfuncPtr() { public void handler(int state)
	{
		/* generate a sound interrupt */
	/*	cpu_set_irq_line (2, M6809_IRQ_LINE, state ? ASSERT_LINE : CLEAR_LINE);*/
	
		if (state != 0)
		{
			/* ideally we should use the cpu_set_irq_line call above, but it breaks */
			/* sound in Qix */
			cpu_cause_interrupt (2, M6809_INT_IRQ);
	
			/* wait for the sound CPU to read the command */
			cpu_yielduntil_trigger (500);
			suspended = 1;
	
			/* but add a watchdog so that we're not hosed if interrupts are disabled */
			cpu_triggertime (TIME_IN_USEC (100), 500);
		}
	}};
	
	/***************************************************************************
	
	        68705 Communication
	
	***************************************************************************/
	
	static /*unsigned*/ char portA_in,portA_out,ddrA;
	static /*unsigned*/ char portB_in,portB_out,ddrB;
	static /*unsigned*/ char portC_in,portC_out,ddrC;
	
	public static ReadHandlerPtr sdungeon_68705_portA_r = new ReadHandlerPtr() { public int handler(int offset)
	{
	//if (errorlog != 0) fprintf(errorlog,"PC: %x MCU PORTA R = %x\n",cpu_get_pc(),portA_in);
		return (portA_out & ddrA) | (portA_in & ~ddrA);
	} };
	
	public static WriteHandlerPtr sdungeon_68705_portA_w = new WriteHandlerPtr() { public void handler(int offest, int data)
	{
	//if (errorlog != 0) fprintf(errorlog,"PC: %x SD COINTOMAIN W: %x\n",cpu_get_pc(),data);
		portA_out = (char)(data&0xFF);
	} };
	
	public static WriteHandlerPtr sdungeon_68705_ddrA_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		ddrA = (char)(data&0xFF);
	} };
	
	
	public static ReadHandlerPtr sdungeon_68705_portB_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		portB_in = (char)(input_port_1_r.handler(0) & 0x0F);
		portB_in = (char)(portB_in | ((input_port_1_r.handler(0) & 0x80) >> 3));
	//if (errorlog != 0) fprintf(errorlog,"PC: %x MCU PORTB R = %x\n",cpu_get_pc(),portB_in);
	
		return (portB_out & ddrB) | (portB_in & ~ddrB);
	} };
	
	public static WriteHandlerPtr sdungeon_68705_portB_w = new WriteHandlerPtr() { public void handler(int offest, int data)
	{
	//if (errorlog != 0) fprintf(errorlog,"PC: %x port B write %x\n",cpu_get_pc(),data);
		portB_out = (char)(data&0xFF);
	} };
	
	public static WriteHandlerPtr sdungeon_68705_ddrB_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		ddrB = (char)(data&0xFF);
	} };
	
	
	public static ReadHandlerPtr sdungeon_68705_portC_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		portC_in = (char)((~sdungeon_coinctrl & 0x08) | ((input_port_1_r.handler(0) & 0x70) >> 4));
	//if (errorlog != 0) fprintf(errorlog,"PC: %x MCU PORTC R = %x\n",cpu_get_pc(),portC_in);
	
		return (portC_out & ddrC) | (portC_in & ~ddrC);
	} };
	
	public static WriteHandlerPtr sdungeon_68705_portC_w = new WriteHandlerPtr() { public void handler(int offest, int data)
	{
	//if (errorlog != 0) fprintf(errorlog,"PC: %x port C write %x\n",cpu_get_pc(),data);
		portC_out = (char)(data&0xFF);
	} };
	
	public static WriteHandlerPtr sdungeon_68705_ddrC_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		ddrC = (char)(data&0xFF);
	} };
	
	
	
	public static ReadHandlerPtr sdungeon_coin_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return portA_out;
	} };
	
	public static WriteHandlerPtr sdungeon_coin_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	//if (errorlog != 0) fprintf(errorlog,"PC: %x COIN COMMAND W: %x\n",cpu_get_pc(),data);
		/* this is a callback called by pia_0_w(), so I don't need to synchronize */
		/* the CPUs - they have already been synchronized by sdungeon_pia_0_w() */
		portA_in = (char)(data&0xFF);
	} };
	
	public static WriteHandlerPtr sdungeon_coinctrl_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	//if (errorlog != 0) fprintf(errorlog,"PC: %x COIN CTRL W: %x\n",cpu_get_pc(),data);
		if ((data & 0x04) != 0)
		{
			cpu_set_irq_line(3,M6809_IRQ_LINE,ASSERT_LINE);
			/* spin for a while to let the 68705 write the result */
			cpu_spinuntil_time(TIME_IN_USEC(50));
		}
		else
			cpu_set_irq_line(3,M6809_IRQ_LINE,CLEAR_LINE);
	
		/* this is a callback called by pia_0_w(), so I don't need to synchronize */
		/* the CPUs - they have already been synchronized by sdungeon_pia_0_w() */
		sdungeon_coinctrl = data;
	} };
	
	public static TimerCallbackHandlerPtr pia_0_w_callback = new TimerCallbackHandlerPtr(){ public void handler(int param)
        {

		pia_0_w.handler(param >> 8,param & 0xff);
	}};
	
	public static WriteHandlerPtr sdungeon_pia_0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	//if (errorlog != 0) fprintf(errorlog,"%04x: PIA 1 write offset %02x data %02x\n",cpu_get_pc(),offset,data);
	
		/* Hack: Kram and Zoo Keeper for some reason (protection?) leave the port A */
		/* DDR set to 0xff, so they cannot read the player 1 controls. Here I force */
		/* the DDR to 0, so the controls work correctly. */
		if (offset == 0) data = 0;
	
		/* make all the CPUs synchronize, and only AFTER that write the command to the PIA */
		/* otherwise the 68705 will miss commands */
		timer_set(TIME_NOW,data | (offset << 8),pia_0_w_callback);
	} };
        	/***************************************************************************
	
		Qix has 6 PIAs on board:
	
		From the ROM I/O schematic:
	
		PIA 1 = U11:
			port A = external input (input_port_0)
			port B = external input (input_port_1) (coin)
		PIA 2 = U20:
			port A = external input (???)
			port B = external input (???)
		PIA 3 = U30:
			port A = external input (???)
			port B = external input (???)
	
	
		From the data/sound processor schematic:
	
		PIA 4 = U20:
			port A = data CPU to sound CPU communication
			port B = some kind of sound control
			CA1 = interrupt signal from sound CPU
			CA2 = interrupt signal to sound CPU
		PIA 5 = U8:
			port A = sound CPU to data CPU communication
			port B = DAC value (port B)
			CA1 = interrupt signal from data CPU
			CA2 = interrupt signal to data CPU
		PIA 6 = U7: (never actually used)
			port A = unused
			port B = sound CPU to TMS5220 communication
			CA1 = interrupt signal from TMS5220
			CA2 = write signal to TMS5220
			CB1 = ready signal from TMS5220
			CB2 = read signal to TMS5220
	
	***************************************************************************/
	
	static pia6821_interface qix_pia_0_intf = new pia6821_interface
	(
		/*inputs : A/B,CA/B1,CA/B2 */ input_port_0_r, input_port_1_r, null, null, null, null,
		/*outputs: A/B,CA/B2       */ null, null, null, null,
		/*irqs   : A/B             */ null, null
        );
	
	static pia6821_interface qixmcu_pia_0_intf = new pia6821_interface
	(
		/*inputs : A/B,CA/B1,CA/B2 */ input_port_0_r, sdungeon_coin_r, null, null, null, null,
		/*outputs: A/B,CA/B2       */ null, sdungeon_coin_w, null, null,
		/*irqs   : A/B             */ null, null
        );
	
	static  pia6821_interface qix_pia_1_intf = new pia6821_interface
	(
		/*inputs : A/B,CA/B1,CA/B2 */ input_port_2_r, input_port_3_r, null, null, null, null,
		/*outputs: A/B,CA/B2       */ null, null, null, null,
		/*irqs   : A/B             */ null, null
        );
	
	static  pia6821_interface qix_pia_2_intf = new pia6821_interface
	(
		/*inputs : A/B,CA/B1,CA/B2 */ input_port_4_r, null, null, null, null, null,
		/*outputs: A/B,CA/B2       */ null, null, null, null,
		/*irqs   : A/B             */ null, null
        );
	
	static  pia6821_interface qixmcu_pia_2_intf = new pia6821_interface
	(
		/*inputs : A/B,CA/B1,CA/B2 */ input_port_4_r, null, null, null, null, null,
		/*outputs: A/B,CA/B2       */ null, sdungeon_coinctrl_w, null, null,
		/*irqs   : A/B             */ null, null
        );
	
	static  pia6821_interface qix_pia_3_intf = new pia6821_interface
	(
		/*inputs : A/B,CA/B1,CA/B2 */ null, null, null, null, null, null,
		/*outputs: A/B,CA/B2       */ pia_4_porta_w, null, pia_4_ca1_w, null,
		/*irqs   : A/B             */ /*qix_pia_dint*/null, /*qix_pia_dint*/null
        );
	
	static  pia6821_interface qix_pia_4_intf = new pia6821_interface
	(
		/*inputs : A/B,CA/B1,CA/B2 */ qix_sound_r, null, null, null, null, null,
		/*outputs: A/B,CA/B2       */ pia_3_porta_w, qix_dac_w, pia_3_ca1_w, null,
		/*irqs   : A/B             */ qix_pia_sint, qix_pia_sint
        );
	
	static  pia6821_interface qix_pia_5_intf = new pia6821_interface
	(
		/*inputs : A/B,CA/B1,CA/B2 */ null, null, null, null, null, null,
		/*outputs: A/B,CA/B2       */ pia_3_porta_w, qix_dac_w, pia_3_ca1_w, null,
		/*irqs   : A/B             */ qix_pia_sint, qix_pia_sint
        );
}
