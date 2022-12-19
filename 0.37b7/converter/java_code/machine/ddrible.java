/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package machine;

public class ddrible
{
	
	UBytePtr ddrible_sharedram;
	UBytePtr ddrible_snd_sharedram;
	
	extern int ddrible_int_enable_0;
	extern int ddrible_int_enable_1;
	
	public static InitMachinePtr ddrible_init_machine = new InitMachinePtr() { public void handler() 
	{
		ddrible_int_enable_0 = ddrible_int_enable_1 = 0;
	} };
	
	public static WriteHandlerPtr ddrible_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
		int bankaddress;
	
		bankaddress = 0x10000 + (data & 0x0f) * 0x2000;
		cpu_setbank(1,&RAM[bankaddress]);
	} };
	
	public static InterruptPtr ddrible_interrupt_0 = new InterruptPtr() { public int handler() 
	{
		if (ddrible_int_enable_0 != 0)
			return M6809_INT_FIRQ;
		return ignore_interrupt();
	} };
	
	public static InterruptPtr ddrible_interrupt_1 = new InterruptPtr() { public int handler() 
	{
		if (ddrible_int_enable_1 != 0)
			return M6809_INT_FIRQ;
		return ignore_interrupt();
	} };
	
	public static WriteHandlerPtr int_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x02) != 0)
			ddrible_int_enable_0 = 1;
		else
			ddrible_int_enable_0 = 0;
	} };
	
	public static WriteHandlerPtr int_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x02) != 0)
			ddrible_int_enable_1 = 1;
		else
			ddrible_int_enable_1 = 0;
	} };
	
	public static ReadHandlerPtr ddrible_sharedram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return ddrible_sharedram[offset];
	} };
	
	public static WriteHandlerPtr ddrible_sharedram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		ddrible_sharedram[offset] = data;
	} };
	
	public static ReadHandlerPtr ddrible_snd_sharedram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return ddrible_snd_sharedram[offset];
	} };
	
	public static WriteHandlerPtr ddrible_snd_sharedram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		ddrible_snd_sharedram[offset] = data;
	} };
	
	public static WriteHandlerPtr ddrible_coin_counter_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* b4-b7: unused */
		/* b2-b3: unknown */
		/* b1: coin counter 2 */
		/* b0: coin counter 1 */
	
		coin_counter_w.handler(0,(data) & 0x01);
		coin_counter_w.handler(1,(data >> 1) & 0x01);
	} };
	
}
