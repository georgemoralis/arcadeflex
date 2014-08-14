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
package machine;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.commonH.*;
import static mame.inputport.*;
import static mame.drawgfxH.*;
import static vidhrdw.generic.*;
import static mame.sndintrfH.*;
import static mame.cpuintrf.*;
import static mame.common.*;
import static arcadeflex.input.*;
import static mame.inputportH.*;
import static mame.inputH.*;
import static arcadeflex.libc.*;
import static arcadeflex.libc_old.*;
import static mame.memory.*;
import static mame.mame.*;
import static cpu.m6809.m6809H.*;

public class ddrible
{
	
	public static CharPtr ddrible_sharedram=new CharPtr();
	public static CharPtr ddrible_snd_sharedram=new CharPtr();
	public static int int_enable_0, int_enable_1;
	
        public static InitMachinePtr ddrible_init_machine = new InitMachinePtr() { public void handler() 
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
	public static InterruptPtr ddrible_interrupt_0 = new InterruptPtr() { public int handler()
        {
		if (int_enable_0 != 0)
			return M6809_INT_FIRQ;
		return ignore_interrupt.handler();
	}};
	public static InterruptPtr ddrible_interrupt_1 = new InterruptPtr() { public int handler()
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
