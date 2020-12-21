package gr.codebb.arcadeflex.v036.machine;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;

public class arkanoid {
    public static int arkanoid_paddle_select;
    static int z80write,fromz80,m68705write,toz80;
    
    static char portA_in,portA_out,ddrA;
    static char portC_out,ddrC;
 
	public static InitMachinePtr arkanoid_init_machine = new InitMachinePtr() { public void handler() 
	{
		portA_in = 0;
                portA_out = 0;
                z80write = m68705write = 0;
	} };
    /*TODO*///
    /*TODO*///int arkanoid_Z80_mcu_r (int value)
    /*TODO*///{
    /*TODO*///	/* return the last value the 68705 wrote, and mark that we've read it */
    /*TODO*///	m68705write = 0;
    /*TODO*///	return toz80;
    /*TODO*///}
    /*TODO*///
    /*TODO*///void arkanoid_Z80_mcu_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	/* a write from the Z80 has occurred, mark it and remember the value */
    /*TODO*///	z80write = 1;
    /*TODO*///	fromz80 = data;
    /*TODO*///
    /*TODO*///	/* give up a little bit of time to let the 68705 detect the write */
    /*TODO*///	cpu_spinuntil_trigger(700);
    /*TODO*///}
    /*TODO*///
    /*TODO*///int arkanoid_68705_portA_r(int offset)
    /*TODO*///{
    /*TODO*///	return (portA_out & ddrA) | (portA_in & ~ddrA);
    /*TODO*///}
    /*TODO*///
    /*TODO*///void arkanoid_68705_portA_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	portA_out = data;
    /*TODO*///}
    /*TODO*///
    /*TODO*///void arkanoid_68705_ddrA_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	ddrA = data;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///int arkanoid_68705_portC_r(int offset)
    /*TODO*///{
    /*TODO*///	int res=0;
    /*TODO*///
    /*TODO*///	/* bit 0 is high on a write strobe; clear it once we've detected it */
    /*TODO*///	if (z80write) res |= 0x01;
    /*TODO*///
    /*TODO*///	/* bit 1 is high if the previous write has been read */
    /*TODO*///	if (!m68705write) res |= 0x02;
    /*TODO*///
    /*TODO*///	return (portC_out & ddrC) | (res & ~ddrC);
    /*TODO*///}
    /*TODO*///
    /*TODO*///void arkanoid_68705_portC_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	if ((ddrC & 0x04) && (~data & 0x04) && (portC_out & 0x04))
    /*TODO*///	{
    /*TODO*///		/* mark that the command has been seen */
    /*TODO*///		cpu_trigger(700);
    /*TODO*///
    /*TODO*///		/* return the last value the Z80 wrote */
    /*TODO*///		z80write = 0;
    /*TODO*///		portA_in = fromz80;
    /*TODO*///	}
    /*TODO*///	if ((ddrC & 0x08) && (~data & 0x08) && (portC_out & 0x08))
    /*TODO*///	{
    /*TODO*///		/* a write from the 68705 to the Z80; remember its value */
    /*TODO*///		m68705write = 1;
    /*TODO*///		toz80 = portA_out;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	portC_out = data;
    /*TODO*///}
    /*TODO*///
    /*TODO*///void arkanoid_68705_ddrC_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	ddrC = data;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///int arkanoid_68705_input_0_r(int offset)
    /*TODO*///{
    /*TODO*///	int res = input_port_0_r(offset) & 0x3f;
    /*TODO*///
    /*TODO*///	/* bit 0x40 comes from the sticky bit */
    /*TODO*///	if (!z80write) res |= 0x40;
    /*TODO*///
    /*TODO*///	/* bit 0x80 comes from a write latch */
    /*TODO*///	if (!m68705write) res |= 0x80;
    /*TODO*///
    /*TODO*///	return res;
    /*TODO*///}
    /*TODO*///
	public static ReadHandlerPtr arkanoid_input_2_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (arkanoid_paddle_select != 0)
		{
			return input_port_3_r.handler(offset);
		}
		else
		{
			return input_port_2_r.handler(offset);
		}
	} };   
}
