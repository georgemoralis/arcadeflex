/***************************************************************************

  machine.c

  Written by Kenneth Lin (kenneth_lin@ai.vancouver.bc.ca)

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package machine;

public class jackal
{
	
	extern unsigned char jackal_interrupt_enable;
	
	UBytePtr jackal_rambank = 0;
	UBytePtr jackal_spritebank = 0;
	
	
	public static InitMachinePtr jackal_init_machine = new InitMachinePtr() { public void handler() 
	{
		cpu_setbank(1,&((memory_region(REGION_CPU1))[0x4000]));
	 	jackal_rambank = &((memory_region(REGION_CPU1))[0]);
		jackal_spritebank = &((memory_region(REGION_CPU1))[0]);
	} };
	
	
	
	public static ReadHandlerPtr jackal_zram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return jackal_rambank[0x0020+offset];
	} };
	
	
	public static ReadHandlerPtr jackal_commonram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return jackal_rambank[0x0060+offset];
	} };
	
	
	public static ReadHandlerPtr jackal_commonram1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return (memory_region(REGION_CPU1))[0x0060+offset];
	} };
	
	
	public static ReadHandlerPtr jackal_voram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return jackal_rambank[0x2000+offset];
	} };
	
	
	public static ReadHandlerPtr jackal_spriteram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return jackal_spritebank[0x3000+offset];
	} };
	
	
	public static WriteHandlerPtr jackal_rambank_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		jackal_rambank = &((memory_region(REGION_CPU1))[((data & 0x10) << 12)]);
		jackal_spritebank = &((memory_region(REGION_CPU1))[((data & 0x08) << 13)]);
		cpu_setbank(1,&((memory_region(REGION_CPU1))[((data & 0x20) << 11) + 0x4000]));
	} };
	
	
	public static WriteHandlerPtr jackal_zram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		jackal_rambank[0x0020+offset] = data;
	} };
	
	
	public static WriteHandlerPtr jackal_commonram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		jackal_rambank[0x0060+offset] = data;
	} };
	
	
	public static WriteHandlerPtr jackal_commonram1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		(memory_region(REGION_CPU1))[0x0060+offset] = data;
		(memory_region(REGION_CPU2))[0x6060+offset] = data;
	} };
	
	
	public static WriteHandlerPtr jackal_voram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((offset & 0xF800) == 0)
		{
			dirtybuffer[offset & 0x3FF] = 1;
		}
		jackal_rambank[0x2000+offset] = data;
	} };
	
	
	public static WriteHandlerPtr jackal_spriteram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		jackal_spritebank[0x3000+offset] = data;
	} };
}
