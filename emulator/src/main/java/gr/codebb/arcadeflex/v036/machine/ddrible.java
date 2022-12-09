/***************************************************************************

  machine.c

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
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.platform.input.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.memory.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;

public class ddrible
{
	
	public static UBytePtr ddrible_sharedram=new UBytePtr();
	public static UBytePtr ddrible_snd_sharedram=new UBytePtr();
	public static int int_enable_0, int_enable_1;
	
        public static InitMachineHandlerPtr ddrible_init_machine = new InitMachineHandlerPtr() { public void handler() 
	{
		int_enable_0 = int_enable_1 = 0;
        }};
	
	public static WriteHandlerPtr ddrible_bankswitch_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
		int bankaddress;
	
		bankaddress = 0x10000 + (data & 0x0f) * 0x2000;
		cpu_setbank(1,new UBytePtr(RAM,bankaddress));
	} };
	public static InterruptHandlerPtr ddrible_interrupt_0 = new InterruptHandlerPtr() { public int handler()
        {
		if (int_enable_0 != 0)
			return M6809_INT_FIRQ;
		return ignore_interrupt.handler();
	}};
	public static InterruptHandlerPtr ddrible_interrupt_1 = new InterruptHandlerPtr() { public int handler()
        {
		if (int_enable_1 != 0)
			return M6809_INT_FIRQ;
		return ignore_interrupt.handler();
	}};
	
	public static WriteHandlerPtr int_0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if ((data & 0x02) != 0)
			int_enable_0 = 1;
		else
			int_enable_0 = 0;
	} };
	
	public static WriteHandlerPtr int_1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if ((data & 0x02) != 0)
			int_enable_1 = 1;
		else
			int_enable_1 = 0;
	} };
	
	public static ReadHandlerPtr ddrible_sharedram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return ddrible_sharedram.read(offset);
	} };
	
	public static WriteHandlerPtr ddrible_sharedram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		ddrible_sharedram.write(offset,data);
	} };
	
	public static ReadHandlerPtr ddrible_snd_sharedram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return ddrible_snd_sharedram.read(offset);
	} };
	
	public static WriteHandlerPtr ddrible_snd_sharedram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		ddrible_snd_sharedram.write(offset,data);
	} };
	
	public static WriteHandlerPtr ddrible_coin_counter_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* b4-b7: unused */
		/* b2-b3: unknown */
		/* b1: coin counter 2 */
		/* b0: coin counter 1 */
	
		coin_counter_w.handler(0,(data) & 0x01);
		coin_counter_w.handler(1,(data >> 1) & 0x01);
	} };
	
}
