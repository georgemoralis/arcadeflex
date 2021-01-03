/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.machine;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.*;
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.platform.osdepend.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.avgdvg.*;
        
public class asteroid
{
	
	public static InterruptPtr asteroid_interrupt = new InterruptPtr() { public int handler() 
	{
		/* Turn off interrupts if self-test is enabled */
		if ((readinputport(0) & 0x80) != 0)
			return ignore_interrupt.handler();
		else
			return nmi_interrupt.handler();
	} };
	
	public static InterruptPtr llander_interrupt = new InterruptPtr() { public int handler() 
	{
		/* Turn off interrupts if self-test is enabled */
		if ((readinputport(0) & 0x02) != 0)
			return nmi_interrupt.handler();
		else
			return ignore_interrupt.handler();
	} };
	
	public static ReadHandlerPtr asteroid_IN0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	
		int res;
		int bitmask;
	
		res=readinputport(0);
	
		bitmask = (1 << offset);
	
		if ((cpu_gettotalcycles() & 0x100) != 0)
			res |= 0x02;
		if (avgdvg_done()==0)
			res |= 0x04;
	
		if ((res & bitmask) != 0)
			res = 0x80;
		else
			res = ~0x80;
	
		return res;
	} };
	
	public static ReadHandlerPtr asteroib_IN0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int res;
	
		res=readinputport(0);
	
	//	if (cpu_gettotalcycles() & 0x100)
	//		res |= 0x02;
		if (avgdvg_done()==0)
			res |= 0x80;
	
		return res;
	} };
	
	/*
	 * These 7 memory locations are used to read the player's controls.
	 * Typically, only the high bit is used. This is handled by one input port.
	 */
	
	public static ReadHandlerPtr asteroid_IN1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int res;
		int bitmask;
	
		res=readinputport(1);
		bitmask = (1 << offset);
	
		if ((res & bitmask) != 0)
			res = 0x80;
		else
		 	res = ~0x80;
		return (res);
	} };
	
	public static ReadHandlerPtr asteroid_DSW1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int res;
		int res1;
	
		res1 = readinputport(2);
	
		res = 0xfc | ((res1 >> (2 * (3 - (offset & 0x3)))) & 0x3);
		return res;
	} };
	
	static int asteroid_bank = 0;
	public static WriteHandlerPtr asteroid_bank_switch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int asteroid_newbank;
		UBytePtr RAM = memory_region(REGION_CPU1);
	
	
		asteroid_newbank = (data >> 2) & 1;
		if (asteroid_bank != asteroid_newbank) {
			/* Perform bankswitching on page 2 and page 3 */
			int temp;
			int i;
	
			asteroid_bank = asteroid_newbank;
			for (i = 0; i < 0x100; i++) {
				temp = RAM.read(0x200 + i);
				RAM.write(0x200 + i, RAM.read(0x300 + i));
				RAM.write(0x300 + i, temp);
			}
		}
/*TODO*///		set_led_status (0, ~data & 0x02);
/*TODO*///		set_led_status (1, ~data & 0x01);
	} };
        
        static int astdelux_bank = 0;
	
	public static WriteHandlerPtr astdelux_bank_switch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int astdelux_newbank;
		UBytePtr RAM = memory_region(REGION_CPU1);
	
	
		astdelux_newbank = (data >> 7) & 1;
		if (astdelux_bank != astdelux_newbank) {
			/* Perform bankswitching on page 2 and page 3 */
			int temp;
			int i;
	
			astdelux_bank = astdelux_newbank;
			for (i = 0; i < 0x100; i++) {
				temp = RAM.read(0x200 + i);
				RAM.write(0x200 + i, RAM.read(0x300 + i));
				RAM.write(0x300 + i, temp);
			}
		}
	} };
	
	public static WriteHandlerPtr astdelux_led_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
/*TODO*///		set_led_status(offset,~data & 0x01);
	} };
	
	public static InitMachinePtr asteroid_init_machine = new InitMachinePtr() { public void handler() 
	{
		asteroid_bank_switch_w.handler(0,0);
	} };
	
	/*
	 * This is Lunar Lander's Inputport 0.
	 */
	public static ReadHandlerPtr llander_IN0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int res;
	
		res = readinputport(0);
	
		if (avgdvg_done() != 0)
			res |= 0x01;
		if ((cpu_gettotalcycles() & 0x100) != 0)
			res |= 0x40;
	
		return res;
	} };
}
