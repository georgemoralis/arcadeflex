/**********************************************************************

	Motorola 6821 PIA interface and emulation

	This function emulates all the functionality of up to 8 M6821
	peripheral interface adapters.

**********************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package machine;

import static mame.driverH.*;
import static mame.common.*;
import static mame.commonH.*;
import static mame.cpuintrf.*;
import static mame.cpuintrfH.*;
import static mame.inputport.*;
import static platform.ptrlib.*;
import static mame.memory.*;
import static mame.memoryH.*;
import static machine._6812piaH.*;

public class _6821pia
{
	
	
	/* define this to "errorlog" to get logging; define it to 0 to get no logging */
	//#define pialog 0
	
	
	/******************* internal PIA data structure *******************/
	
	public static class pia6821
	{
		pia6821_interface intf;
		/*UINT8*/char addr;
	
		/*UINT8*/char in_a;
		/*UINT8*/char in_ca1;
		/*UINT8*/char in_ca2;
		/*UINT8*/char out_a;
		/*UINT8*/char out_ca2;
		/*UINT8*/char ddr_a;
		/*UINT8*/char ctl_a;
		/*UINT8*/char irq_a1;
		/*UINT8*/char irq_a2;
		/*UINT8*/char irq_a_state;
	
		/*UINT8*/char in_b;
		/*UINT8*/char in_cb1;
		/*UINT8*/char in_cb2;
		/*UINT8*/char out_b;
		/*UINT8*/char out_cb2;
		/*UINT8*/char ddr_b;
		/*UINT8*/char ctl_b;
		/*UINT8*/char irq_b1;
		/*UINT8*/char irq_b2;
		/*UINT8*/char irq_b_state;
	};
	
	
	/******************* convenince macros and defines *******************/
	
	public static final int PIA_IRQ1				=0x80;
	public static final int PIA_IRQ2				=0x40;
	
	static boolean IRQ1_ENABLED(int c) { return (c & 0x01) != 0; }
        static boolean IRQ1_DISABLED(int c) { return (c & 0x01) == 0; }
	static boolean C1_LOW_TO_HIGH(int c) { return (c & 0x02) != 0; }
        static boolean C1_HIGH_TO_LOW(int c) { return (c & 0x02) == 0; }
	static boolean OUTPUT_SELECTED(int c) { return (c & 0x04) != 0; }
	static boolean DDR_SELECTED(int c){ return ((c & 0x04))==0; }
	static boolean IRQ2_ENABLED(int c) { return (c & 0x08) != 0; }
        static boolean IRQ2_DISABLED(int c) { return (c & 0x08) == 0; }
	static boolean STROBE_E_RESET(int c) { return (c & 0x08) != 0; }
	static boolean STROBE_C1_RESET(int c) { return (c & 0x08) == 0; }
	static boolean SET_C2(int c) { return (c & 0x08) != 0; }
	static boolean RESET_C2(int c)	{ return ((c & 0x08))==0; }
	static boolean C2_LOW_TO_HIGH(int c) { return (c & 0x10) != 0; }
        static boolean C2_HIGH_TO_LOW(int c) { return (c & 0x10) == 0; }
	static boolean C2_SET_MODE(int c) { return (c & 0x10)!=0;}
	static boolean C2_STROBE_MODE(int c) { return ((c & 0x10) == 0); }
	static boolean C2_OUTPUT(int c) { return (c & 0x20) != 0; }
        static boolean C2_INPUT(int c) { return ((c & 0x20) == 0); }
	
	
	
	
	/******************* static variables *******************/
	
	static pia6821 pia[]=new pia6821[MAX_PIA];
	
	static int swizzle_address[] = { 0, 2, 1, 3 };
	
	
	
	/******************* un-configuration *******************/
	
	public static void pia_unconfig()
	{
		for (int i = 0; i < MAX_PIA; i++) pia[i] = new pia6821();
	}
	
	
	/******************* configuration *******************/
	
	public static void pia_config(int which, int addressing,pia6821_interface intf)
	{
		if (which >= MAX_PIA) return;
		pia[which].intf = intf;
		pia[which].addr = (char)(addressing&0xFF);
	}
	
	
	/******************* reset *******************/
	
	public static void pia_reset()
	{
		int i;
	
		/* zap each structure, preserving the interface and swizzle */
                for (i = 0; i < MAX_PIA; i++)
                {
                        pia6821_interface intf = pia[i].intf;
                        char addr = pia[i].addr;

                        pia[i] = new pia6821();

                        pia[i].intf = intf;
                        pia[i].addr = addr;
                }
	}
	
	
	/******************* wire-OR for all interrupt handlers *******************/
	
	public static void update_shared_irq_handler(irqfuncPtr irq_func)
	{
		int i;
	
		/* search all PIAs for this same IRQ function */
		for (i = 0; i < MAX_PIA; i++)
			if (pia[i].intf!=null)
			{
				/* check IRQ A */
				if (pia[i].intf.irq_a_func == irq_func && pia[i].irq_a_state!=0)
				{
					irq_func.handler(1);
					return;
				}
	
				/* check IRQ B */
				if (pia[i].intf.irq_b_func == irq_func && pia[i].irq_b_state!=0)
				{
					irq_func.handler(1);
					return;
				}
			}
	
		/* if we found nothing, the state is off */
		irq_func.handler(0);
	}
	
	
	/******************* external interrupt check *******************/
	
	public static void update_6821_interrupts(pia6821 p)
	{
		int new_state;
	
		/* start with IRQ A */
		new_state = 0;
		if ((p.irq_a1!=0 && IRQ1_ENABLED(p.ctl_a)) || (p.irq_a2!=0 && IRQ2_ENABLED(p.ctl_a))) new_state = 1;
		if (new_state != p.irq_a_state)
		{
			p.irq_a_state = (char)(new_state&0xFF);
			if (p.intf.irq_a_func!=null) update_shared_irq_handler(p.intf.irq_a_func);
		}
	
		/* then do IRQ B */
		new_state = 0;
		if ((p.irq_b1!=0 && IRQ1_ENABLED(p.ctl_b)) || (p.irq_b2!=0 && IRQ2_ENABLED(p.ctl_b))) new_state = 1;
		if (new_state != p.irq_b_state)
		{
			p.irq_b_state = (char)(new_state&0xFF);
			if (p.intf.irq_b_func!=null) update_shared_irq_handler(p.intf.irq_b_func);
		}
	}
	
	
	/******************* CPU interface for PIA read *******************/
	
	public static int pia_read(int which, int offset)
	{
		pia6821 p = pia[which];
		int val = 0;
	
		/* adjust offset for 16-bit and ordering */
		if ((p.addr & PIA_16BIT)!=0) offset /= 2;
		offset &= 3;
		if ((p.addr & PIA_ALTERNATE_ORDERING)!=0) offset = swizzle_address[offset];
	
		switch (offset)
		{
			/******************* port A output/DDR read *******************/
			case PIA_DDRA:
	
				/* read output register */
				if (OUTPUT_SELECTED(p.ctl_a))
				{
					/* update the input */
					if (p.intf.in_a_func!=null) p.in_a = (char)(p.intf.in_a_func.handler(0)&0xFF);
	
					/* combine input and output values */
					val = (p.out_a & p.ddr_a) + (p.in_a & ~p.ddr_a);
	
					/* IRQ flags implicitly cleared by a read */
					p.irq_a1 = p.irq_a2 = 0;
					update_6821_interrupts(p);
	
					/* CA2 is configured as output and in read strobe mode */
					if (C2_OUTPUT(p.ctl_a) && C2_STROBE_MODE(p.ctl_a))
					{
						/* this will cause a transition low; call the output function if we're currently high */
						if (p.out_ca2!=0)
							if (p.intf.out_ca2_func!=null) p.intf.out_ca2_func.handler(0, 0);
						p.out_ca2 = 0;
	
						/* if the CA2 strobe is cleared by the E, reset it right away */
						if (STROBE_E_RESET(p.ctl_a))
						{
							if (p.intf.out_ca2_func!=null) p.intf.out_ca2_func.handler(0, 1);
							p.out_ca2 = 1;
						}
					}
	
					//if (pialog != 0) fprintf(pialog, "PIA%d read port A = %02X\n", which, val);
				}
	
				/* read DDR register */
				else
				{
					val = p.ddr_a;
					//if (pialog != 0) fprintf(pialog, "PIA%d read DDR A = %02X\n", which, val);
				}
				break;
	
			/******************* port B output/DDR read *******************/
			case PIA_DDRB:
	
				/* read output register */
				if (OUTPUT_SELECTED(p.ctl_b))
				{
					/* update the input */
					if (p.intf.in_b_func!=null) p.in_b = (char)(p.intf.in_b_func.handler(0)&0xFF);
	
					/* combine input and output values */
					val = (p.out_b & p.ddr_b) + (p.in_b & ~p.ddr_b);
	
					/* IRQ flags implicitly cleared by a read */
					p.irq_b1 = p.irq_b2 = 0;
					update_6821_interrupts(p);
	
					//if (pialog != 0) fprintf(pialog, "PIA%d read port B = %02X\n", which, val);
				}
	
				/* read DDR register */
				else
				{
					val = p.ddr_b;
					//if (pialog != 0) fprintf(pialog, "PIA%d read DDR B = %02X\n", which, val);
				}
				break;
	
			/******************* port A control read *******************/
			case PIA_CTLA:
	
				/* Update CA1 & CA2 if callback exists, these in turn may update IRQ's */
				if (p.intf.in_ca1_func!=null) pia_set_input_ca1.handler(which, p.intf.in_ca1_func.handler(0));
				if (p.intf.in_ca2_func!=null) pia_set_input_ca2.handler(which, p.intf.in_ca2_func.handler(0));
	
				/* read control register */
				val = p.ctl_a;
	
				/* set the IRQ flags if we have pending IRQs */
				if (p.irq_a1!=0) val |= PIA_IRQ1;
				if (p.irq_a2!=0 && C2_INPUT(p.ctl_a)) val |= PIA_IRQ2;
	
				//if (pialog != 0) fprintf(pialog, "PIA%d read control A = %02X\n", which, val);
				break;
	
			/******************* port B control read *******************/
			case PIA_CTLB:
	
				/* Update CB1 & CB2 if callback exists, these in turn may update IRQ's */
				if (p.intf.in_cb1_func!=null) pia_set_input_cb1.handler(which, p.intf.in_cb1_func.handler(0));
				if (p.intf.in_cb2_func!=null) pia_set_input_cb2.handler(which, p.intf.in_cb2_func.handler(0));
	
				/* read control register */
				val = p.ctl_b;
	
				/* set the IRQ flags if we have pending IRQs */
				if (p.irq_b1!=0) val |= PIA_IRQ1;
				if (p.irq_b2!=0 && C2_INPUT(p.ctl_b)) val |= PIA_IRQ2;
	
				//if (pialog != 0) fprintf(pialog, "PIA%d read control B = %02X\n", which, val);
				break;
		}
	
		/* adjust final output value for 16-bit */
		if ((p.addr & PIA_16BIT)!=0)
		{
			if ((p.addr & PIA_AUTOSENSE)!=0)
				val = (val << 8) | val;
			else if ((p.addr & PIA_UPPER)!=0)
				val <<= 8;
		}
	
		return val;
	}
	
	
	/******************* CPU interface for PIA write *******************/
	
	public static void pia_write(int which, int offset, int data)
	{
		pia6821 p = pia[which];
	
		/* adjust offset for 16-bit and ordering */
		if ((p.addr & PIA_16BIT)!=0) offset /= 2;
		offset &= 3;
		if ((p.addr & PIA_ALTERNATE_ORDERING)!=0) offset = swizzle_address[offset];
	
		/* adjust data for 16-bit */
		if ((p.addr & PIA_16BIT)!=0)
		{
			if ((p.addr & PIA_AUTOSENSE)!=0)
			{
				if ((data & 0x00ff0000)==0)
					data &= 0xff;
				else
					data = (data >> 8) & 0xff;
			}
			else if ((p.addr & PIA_UPPER)!=0)
			{
				if ((data & 0xff000000) != 0)
					return;
				data = (data >> 8) & 0xff;
			}
			else
			{
				if ((data & 0x00ff0000) != 0)
					return;
				data &= 0xff;
			}
		}
	
		switch (offset)
		{
			/******************* port A output/DDR write *******************/
			case PIA_DDRA:
	
				/* write output register */
				if (OUTPUT_SELECTED(p.ctl_a))
				{
					//if (pialog != 0) fprintf(pialog, "PIA%d port A write = %02X\n", which, data);
	
					/* update the output value */
					p.out_a = (char)(data&0xFF);/* & p.ddr_a; */	/* NS990130 - don't mask now, DDR could change later */
	
					/* send it to the output function */
					if (p.intf.out_a_func!=null && p.ddr_a!=0) p.intf.out_a_func.handler(0, p.out_a & p.ddr_a);	/* NS990130 */
				}
	
				/* write DDR register */
				else
				{
					//if (pialog != 0) fprintf(pialog, "PIA%d DDR A write = %02X\n", which, data);
	
					if (p.ddr_a != data)
					{
						/* NS990130 - if DDR changed, call the callback again */
						p.ddr_a = (char)(data&0xFF);
	
						/* send it to the output function */
						if (p.intf.out_a_func!=null && p.ddr_a!=0) p.intf.out_a_func.handler(0, p.out_a & p.ddr_a);
					}
				}
				break;
	
			/******************* port B output/DDR write *******************/
			case PIA_DDRB:
	
				/* write output register */
				if (OUTPUT_SELECTED(p.ctl_b))
				{
					//if (pialog != 0) fprintf(pialog, "PIA%d port B write = %02X\n", which, data);
	
					/* update the output value */
					p.out_b = (char)(data&0xFF);/* & p.ddr_b */	/* NS990130 - don't mask now, DDR could change later */
	
					/* send it to the output function */
					if (p.intf.out_b_func!=null && p.ddr_b!=0) p.intf.out_b_func.handler(0, p.out_b & p.ddr_b);	/* NS990130 */
	
					/* CB2 is configured as output and in write strobe mode */
					if (C2_OUTPUT(p.ctl_b) && C2_STROBE_MODE(p.ctl_b))
					{
						/* this will cause a transition low; call the output function if we're currently high */
						if (p.out_cb2!=0)
							if (p.intf.out_cb2_func!=null) p.intf.out_cb2_func.handler(0, 0);
						p.out_cb2 = 0;
	
						/* if the CB2 strobe is cleared by the E, reset it right away */
						if (STROBE_E_RESET(p.ctl_b))
						{
							//if (p.intf.out_cb2_func) p.intf.out_cb2_func(0, 1);
							p.out_cb2 = 1;
						}
					}
				}
	
				/* write DDR register */
				else
				{
					//if (pialog != 0) fprintf(pialog, "PIA%d DDR B write = %02X\n", which, data);
	
					if (p.ddr_b != data)
					{
						/* NS990130 - if DDR changed, call the callback again */
						p.ddr_b = (char)(data&0xFF);
	
						/* send it to the output function */
						if (p.intf.out_b_func!=null && p.ddr_b!=0) p.intf.out_b_func.handler(0, p.out_b & p.ddr_b);
					}
				}
				break;
	
			/******************* port A control write *******************/
			case PIA_CTLA:
	
				/* Bit 7 and 6 read only - PD 16/01/00 */
	
				data &= 0x3f;
	
	
				//if (pialog != 0) fprintf(pialog, "PIA%d control A write = %02X\n", which, data);
	
				/* CA2 is configured as output and in set/reset mode */
				/* 10/22/98 - MAB/FMP - any C2_OUTPUT should affect CA2 */
	//			if (C2_OUTPUT(data) && C2_SET_MODE(data))
				if (C2_OUTPUT(data))
				{
					/* determine the new value */
					int temp = SET_C2(data) ? 1 : 0;
	
					/* if this creates a transition, call the CA2 output function */
					if ((p.out_ca2 ^ temp)!=0)
						if (p.intf.out_ca2_func!=null) p.intf.out_ca2_func.handler(0, temp);
	
					/* set the new value */
					p.out_ca2 = (char)(temp&0xFF);
				}
	
				/* update the control register */
				p.ctl_a = (char)(data&0xFF);
	
				/* update externals */
				update_6821_interrupts(p);
				break;
	
			/******************* port B control write *******************/
			case PIA_CTLB:
	
				/* Bit 7 and 6 read only - PD 16/01/00 */
	
				data &= 0x3f;
	
				//if (pialog != 0) fprintf(pialog, "PIA%d control B write = %02X\n", which, data);
	
				/* CB2 is configured as output and in set/reset mode */
				/* 10/22/98 - MAB/FMP - any C2_OUTPUT should affect CB2 */
	//			if (C2_OUTPUT(data) && C2_SET_MODE(data))
				if (C2_OUTPUT(data))
				{
					/* determine the new value */
					int temp = SET_C2(data) ? 1 : 0;
	
					/* if this creates a transition, call the CA2 output function */
					if ((p.out_cb2 ^ temp)!=0)
						if (p.intf.out_cb2_func!=null) p.intf.out_cb2_func.handler(0, temp);
	
					/* set the new value */
					p.out_cb2 = (char)(temp&0xFF);
				}
	
				/* update the control register */
				p.ctl_b = (char)(data&0xFF);
	
				/* update externals */
				update_6821_interrupts(p);
				break;
		}
	}
	
	
	/******************* interface setting PIA port A input *******************/
	
	public static WriteHandlerPtr pia_set_input_a = new WriteHandlerPtr() { public void handler(int which, int data)
	{
		pia6821 p = pia[which];
	
		/* set the input, what could be easier? */
		p.in_a = (char)(data&0xFF);
	} };
	
	
	
	/******************* interface setting PIA port CA1 input *******************/
	
	public static WriteHandlerPtr pia_set_input_ca1 = new WriteHandlerPtr() { public void handler(int which, int data)
	{
		pia6821 p = pia[which];
	
		/* limit the data to 0 or 1 */
		data = data!=0 ? 1 : 0;
	
		/* the new state has caused a transition */
		if ((p.in_ca1 ^ data)!=0)
		{
			/* handle the active transition */
			if ((data!=0 && C1_LOW_TO_HIGH(p.ctl_a)) || (data==0 && C1_HIGH_TO_LOW(p.ctl_a)))
			{
				/* mark the IRQ */
				p.irq_a1 = 1;
	
				/* update externals */
				update_6821_interrupts(p);
	
				/* CA2 is configured as output and in read strobe mode and cleared by a CA1 transition */
				if (C2_OUTPUT(p.ctl_a) && C2_STROBE_MODE(p.ctl_a) && STROBE_C1_RESET(p.ctl_a))
				{
					/* call the CA2 output function */
					if (p.out_ca2==0)
						if (p.intf.out_ca2_func!=null) p.intf.out_ca2_func.handler(0, 1);
	
					/* clear CA2 */
					p.out_ca2 = 1;
				}
			}
		}
	
		/* set the new value for CA1 */
		p.in_ca1 = (char)(data&0xFF);
	} };
	
	
	
	/******************* interface setting PIA port CA2 input *******************/
	
	public static WriteHandlerPtr pia_set_input_ca2 = new WriteHandlerPtr() { public void handler(int which, int data)
	{
		pia6821 p = pia[which];
	
		/* limit the data to 0 or 1 */
		data = data!=0 ? 1 : 0;
	
		/* CA2 is in input mode */
		if (C2_INPUT(p.ctl_a))
		{
			/* the new state has caused a transition */
			if ((p.in_ca2 ^ data)!=0)
			{
				/* handle the active transition */
				if ((data!=0 && C2_LOW_TO_HIGH(p.ctl_a)) || (data==0 && C2_HIGH_TO_LOW(p.ctl_a)))
				{
					/* mark the IRQ */
					p.irq_a2 = 1;
	
					/* update externals */
					update_6821_interrupts(p);
				}
			}
		}
	
		/* set the new value for CA2 */
		p.in_ca2 = (char)(data&0xFF);
	} };
	
	
	
	/******************* interface setting PIA port B input *******************/
	
	public static WriteHandlerPtr pia_set_input_b = new WriteHandlerPtr() { public void handler(int which, int data)
	{
		pia6821 p = pia[which];
	
		/* set the input, what could be easier? */
		p.in_b = (char)(data&0xFF);
	} };
	
	
	
	/******************* interface setting PIA port CB1 input *******************/
	
	public static WriteHandlerPtr pia_set_input_cb1 = new WriteHandlerPtr() { public void handler(int which, int data)
	{
		pia6821 p = pia[which];
	
		/* limit the data to 0 or 1 */
		data = data!=0 ? 1 : 0;
	
		/* the new state has caused a transition */
		if ((p.in_cb1 ^ data)!=0)
		{
			/* handle the active transition */
			if ((data!=0 && C1_LOW_TO_HIGH(p.ctl_b)) || (data==0 && C1_HIGH_TO_LOW(p.ctl_b)))
			{
				/* mark the IRQ */
				p.irq_b1 = 1;
	
				/* update externals */
				update_6821_interrupts(p);
	
				/* CB2 is configured as output and in write strobe mode and cleared by a CA1 transition */
				if (C2_OUTPUT(p.ctl_b) && C2_STROBE_MODE(p.ctl_b) && STROBE_C1_RESET(p.ctl_b))
				{
					/* the IRQ1 flag must have also been cleared */
					if (p.irq_b1==0)
					{
						/* call the CB2 output function */
						if (p.out_cb2==0)
							if (p.intf.out_cb2_func!=null) p.intf.out_cb2_func.handler(0, 1);
	
						/* clear CB2 */
						p.out_cb2 = 1;
					}
				}
			}
		}
	
		/* set the new value for CB1 */
		p.in_cb1 = (char)(data&0xFF);
	} };
	
	
	
	/******************* interface setting PIA port CB2 input *******************/
	
	public static WriteHandlerPtr pia_set_input_cb2 = new WriteHandlerPtr() { public void handler(int which, int data)
	{
		pia6821 p = pia[which];
	
		/* limit the data to 0 or 1 */
		data = data!=0 ? 1 : 0;
	
		/* CB2 is in input mode */
		if (C2_INPUT(p.ctl_b))
		{
			/* the new state has caused a transition */
			if ((p.in_cb2 ^ data)!=0)
			{
				/* handle the active transition */
				if ((data!=0 && C2_LOW_TO_HIGH(p.ctl_b)) || (data==0 && C2_HIGH_TO_LOW(p.ctl_b)))
				{
					/* mark the IRQ */
					p.irq_b2 = 1;
	
					/* update externals */
					update_6821_interrupts(p);
				}
			}
		}
	
		/* set the new value for CA2 */
		p.in_cb2 = (char)(data&0xFF);
	} };
	
	
	
	/******************* Standard 8-bit CPU interfaces, D0-D7 *******************/
	
	public static ReadHandlerPtr pia_0_r = new ReadHandlerPtr() { public int handler(int offset){ return pia_read(0, offset); } };
	public static ReadHandlerPtr pia_1_r = new ReadHandlerPtr() { public int handler(int offset){ return pia_read(1, offset); } };
	public static ReadHandlerPtr pia_2_r = new ReadHandlerPtr() { public int handler(int offset){ return pia_read(2, offset); } };
	public static ReadHandlerPtr pia_3_r = new ReadHandlerPtr() { public int handler(int offset){ return pia_read(3, offset); } };
	public static ReadHandlerPtr pia_4_r = new ReadHandlerPtr() { public int handler(int offset){ return pia_read(4, offset); } };
	public static ReadHandlerPtr pia_5_r = new ReadHandlerPtr() { public int handler(int offset){ return pia_read(5, offset); } };
	public static ReadHandlerPtr pia_6_r = new ReadHandlerPtr() { public int handler(int offset){ return pia_read(6, offset); } };
	public static ReadHandlerPtr pia_7_r = new ReadHandlerPtr() { public int handler(int offset){ return pia_read(7, offset); } };
	
	public static WriteHandlerPtr pia_0_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_write(0, offset, data); } };
	public static WriteHandlerPtr pia_1_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_write(1, offset, data); } };
	public static WriteHandlerPtr pia_2_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_write(2, offset, data); } };
	public static WriteHandlerPtr pia_3_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_write(3, offset, data); } };
	public static WriteHandlerPtr pia_4_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_write(4, offset, data); } };
	public static WriteHandlerPtr pia_5_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_write(5, offset, data); } };
	public static WriteHandlerPtr pia_6_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_write(6, offset, data); } };
	public static WriteHandlerPtr pia_7_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_write(7, offset, data); } };
	
	/******************* 8-bit A/B port interfaces *******************/
	
	public static WriteHandlerPtr pia_0_porta_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_a.handler(0, data); } };
	public static WriteHandlerPtr pia_1_porta_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_a.handler(1, data); } };
	public static WriteHandlerPtr pia_2_porta_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_a.handler(2, data); } };
	public static WriteHandlerPtr pia_3_porta_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_a.handler(3, data); } };
	public static WriteHandlerPtr pia_4_porta_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_a.handler(4, data); } };
	public static WriteHandlerPtr pia_5_porta_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_a.handler(5, data); } };
	public static WriteHandlerPtr pia_6_porta_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_a.handler(6, data); } };
	public static WriteHandlerPtr pia_7_porta_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_a.handler(7, data); } };
	
	public static WriteHandlerPtr pia_0_portb_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_b.handler(0, data); } };
	public static WriteHandlerPtr pia_1_portb_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_b.handler(1, data); } };
	public static WriteHandlerPtr pia_2_portb_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_b.handler(2, data); } };
	public static WriteHandlerPtr pia_3_portb_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_b.handler(3, data); } };
	public static WriteHandlerPtr pia_4_portb_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_b.handler(4, data); } };
	public static WriteHandlerPtr pia_5_portb_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_b.handler(5, data); } };
	public static WriteHandlerPtr pia_6_portb_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_b.handler(6, data); } };
	public static WriteHandlerPtr pia_7_portb_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_b.handler(7, data); } };
	
	public static ReadHandlerPtr pia_0_porta_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[0].in_a; } };
	public static ReadHandlerPtr pia_1_porta_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[1].in_a; } };
	public static ReadHandlerPtr pia_2_porta_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[2].in_a; } };
	public static ReadHandlerPtr pia_3_porta_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[3].in_a; } };
	public static ReadHandlerPtr pia_4_porta_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[4].in_a; } };
	public static ReadHandlerPtr pia_5_porta_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[5].in_a; } };
	public static ReadHandlerPtr pia_6_porta_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[6].in_a; } };
	public static ReadHandlerPtr pia_7_porta_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[7].in_a; } };
	
	public static ReadHandlerPtr pia_0_portb_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[0].in_b; } };
	public static ReadHandlerPtr pia_1_portb_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[1].in_b; } };
	public static ReadHandlerPtr pia_2_portb_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[2].in_b; } };
	public static ReadHandlerPtr pia_3_portb_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[3].in_b; } };
	public static ReadHandlerPtr pia_4_portb_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[4].in_b; } };
	public static ReadHandlerPtr pia_5_portb_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[5].in_b; } };
	public static ReadHandlerPtr pia_6_portb_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[6].in_b; } };
	public static ReadHandlerPtr pia_7_portb_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[7].in_b; } };
	
	/******************* 1-bit CA1/CA2/CB1/CB2 port interfaces *******************/
	
	public static WriteHandlerPtr pia_0_ca1_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_ca1.handler(0, data); } };
	public static WriteHandlerPtr pia_1_ca1_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_ca1.handler(1, data); } };
	public static WriteHandlerPtr pia_2_ca1_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_ca1.handler(2, data); } };
	public static WriteHandlerPtr pia_3_ca1_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_ca1.handler(3, data); } };
	public static WriteHandlerPtr pia_4_ca1_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_ca1.handler(4, data); } };
	public static WriteHandlerPtr pia_5_ca1_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_ca1.handler(5, data); } };
	public static WriteHandlerPtr pia_6_ca1_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_ca1.handler(6, data); } };
	public static WriteHandlerPtr pia_7_ca1_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_ca1.handler(7, data); } };
	public static WriteHandlerPtr pia_0_ca2_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_ca2.handler(0, data); } };
	public static WriteHandlerPtr pia_1_ca2_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_ca2.handler(1, data); } };
	public static WriteHandlerPtr pia_2_ca2_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_ca2.handler(2, data); } };
	public static WriteHandlerPtr pia_3_ca2_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_ca2.handler(3, data); } };
	public static WriteHandlerPtr pia_4_ca2_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_ca2.handler(4, data); } };
	public static WriteHandlerPtr pia_5_ca2_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_ca2.handler(5, data); } };
	public static WriteHandlerPtr pia_6_ca2_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_ca2.handler(6, data); } };
	public static WriteHandlerPtr pia_7_ca2_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_ca2.handler(7, data); } };
	
	public static WriteHandlerPtr pia_0_cb1_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_cb1.handler(0, data); } };
	public static WriteHandlerPtr pia_1_cb1_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_cb1.handler(1, data); } };
	public static WriteHandlerPtr pia_2_cb1_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_cb1.handler(2, data); } };
	public static WriteHandlerPtr pia_3_cb1_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_cb1.handler(3, data); } };
	public static WriteHandlerPtr pia_4_cb1_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_cb1.handler(4, data); } };
	public static WriteHandlerPtr pia_5_cb1_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_cb1.handler(5, data); } };
	public static WriteHandlerPtr pia_6_cb1_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_cb1.handler(6, data); } };
	public static WriteHandlerPtr pia_7_cb1_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_cb1.handler(7, data); } };
	public static WriteHandlerPtr pia_0_cb2_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_cb2.handler(0, data); } };
	public static WriteHandlerPtr pia_1_cb2_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_cb2.handler(1, data); } };
	public static WriteHandlerPtr pia_2_cb2_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_cb2.handler(2, data); } };
	public static WriteHandlerPtr pia_3_cb2_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_cb2.handler(3, data); } };
	public static WriteHandlerPtr pia_4_cb2_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_cb2.handler(4, data); } };
	public static WriteHandlerPtr pia_5_cb2_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_cb2.handler(5, data); } };
	public static WriteHandlerPtr pia_6_cb2_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_cb2.handler(6, data); } };
	public static WriteHandlerPtr pia_7_cb2_w = new WriteHandlerPtr() { public void handler(int offset, int data){ pia_set_input_cb2.handler(7, data); } };
	
	public static ReadHandlerPtr pia_0_ca1_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[0].in_ca1; } };
	public static ReadHandlerPtr pia_1_ca1_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[1].in_ca1; } };
	public static ReadHandlerPtr pia_2_ca1_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[2].in_ca1; } };
	public static ReadHandlerPtr pia_3_ca1_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[3].in_ca1; } };
	public static ReadHandlerPtr pia_4_ca1_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[4].in_ca1; } };
	public static ReadHandlerPtr pia_5_ca1_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[5].in_ca1; } };
	public static ReadHandlerPtr pia_6_ca1_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[6].in_ca1; } };
	public static ReadHandlerPtr pia_7_ca1_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[7].in_ca1; } };
	public static ReadHandlerPtr pia_0_ca2_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[0].in_ca2; } };
	public static ReadHandlerPtr pia_1_ca2_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[1].in_ca2; } };
	public static ReadHandlerPtr pia_2_ca2_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[2].in_ca2; } };
	public static ReadHandlerPtr pia_3_ca2_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[3].in_ca2; } };
	public static ReadHandlerPtr pia_4_ca2_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[4].in_ca2; } };
	public static ReadHandlerPtr pia_5_ca2_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[5].in_ca2; } };
	public static ReadHandlerPtr pia_6_ca2_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[6].in_ca2; } };
	public static ReadHandlerPtr pia_7_ca2_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[7].in_ca2; } };
	
	public static ReadHandlerPtr pia_0_cb1_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[0].in_cb1; } };
	public static ReadHandlerPtr pia_1_cb1_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[1].in_cb1; } };
	public static ReadHandlerPtr pia_2_cb1_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[2].in_cb1; } };
	public static ReadHandlerPtr pia_3_cb1_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[3].in_cb1; } };
	public static ReadHandlerPtr pia_4_cb1_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[4].in_cb1; } };
	public static ReadHandlerPtr pia_5_cb1_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[5].in_cb1; } };
	public static ReadHandlerPtr pia_6_cb1_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[6].in_cb1; } };
	public static ReadHandlerPtr pia_7_cb1_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[7].in_cb1; } };
	public static ReadHandlerPtr pia_0_cb2_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[0].in_cb2; } };
	public static ReadHandlerPtr pia_1_cb2_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[1].in_cb2; } };
	public static ReadHandlerPtr pia_2_cb2_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[2].in_cb2; } };
	public static ReadHandlerPtr pia_3_cb2_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[3].in_cb2; } };
	public static ReadHandlerPtr pia_4_cb2_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[4].in_cb2; } };
	public static ReadHandlerPtr pia_5_cb2_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[5].in_cb2; } };
	public static ReadHandlerPtr pia_6_cb2_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[6].in_cb2; } };
	public static ReadHandlerPtr pia_7_cb2_r = new ReadHandlerPtr() { public int handler(int offset){ return pia[7].in_cb2; } };
}
