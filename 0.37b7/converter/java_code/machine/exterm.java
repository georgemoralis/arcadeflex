/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package machine;

public class exterm
{
	
	extern UBytePtr exterm_code_rom;
	UBytePtr exterm_master_speedup, *exterm_slave_speedup;
	
	static int aimpos1, aimpos2;
	
	
	public static WriteHandlerPtr exterm_host_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		tms34010_host_w(1, offset / TOBYTE(0x00100000), data);
	} };
	
	
	public static ReadHandlerPtr exterm_host_data_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return tms34010_host_r(1, TMS34010_HOST_DATA);
	} };
	
	
	public static ReadHandlerPtr exterm_coderom_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    return READ_WORD(&exterm_code_rom[offset]);
	} };
	
	
	public static ReadHandlerPtr exterm_input_port_0_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int hi = input_port_1_r.handler(offset);
		if (!(hi & 2)) aimpos1++;
		if (!(hi & 1)) aimpos1--;
		aimpos1 &= 0x3f;
	
		return ((hi & 0x80) << 8) | (aimpos1 << 8) | input_port_0_r.handler(offset);
	} };
	
	public static ReadHandlerPtr exterm_input_port_2_3_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int hi = input_port_3_r.handler(offset);
		if (!(hi & 2)) aimpos2++;
		if (!(hi & 1)) aimpos2--;
		aimpos2 &= 0x3f;
	
		return (aimpos2 << 8) | input_port_2_r.handler(offset);
	} };
	
	public static WriteHandlerPtr exterm_output_port_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* All the outputs are activated on the rising edge */
	
		static int last = 0;
	
		/* Bit 0-1= Resets analog controls */
		if ((data & 0x0001) && !(last & 0x0001))
		{
			aimpos1 = 0;
		}
	
		if ((data & 0x0002) && !(last & 0x0002))
		{
			aimpos2 = 0;
		}
	
		/* Bit 13 = Resets the slave CPU */
		if ((data & 0x2000) && !(last & 0x2000))
		{
			cpu_set_reset_line(1,PULSE_LINE);
		}
	
		/* Bits 14-15 = Coin counters */
		coin_counter_w.handler(0, data & 0x8000);
		coin_counter_w.handler(1, data & 0x4000);
	
		last = data;
	} };
	
	
	public static ReadHandlerPtr exterm_master_speedup_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int value = READ_WORD(&exterm_master_speedup[offset]);
	
		/* Suspend cpu if it's waiting for an interrupt */
		if (cpu_get_pc() == 0xfff4d9b0 && !value)
		{
			cpu_spinuntil_int();
		}
	
		return value;
	} };
	
	public static WriteHandlerPtr exterm_slave_speedup_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* Suspend cpu if it's waiting for an interrupt */
		if (cpu_get_pc() == 0xfffff050)
		{
			cpu_spinuntil_int();
		}
	
		WRITE_WORD(&exterm_slave_speedup[offset], data);
	} };
	
	public static ReadHandlerPtr exterm_sound_dac_speedup_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UBytePtr RAM = memory_region(REGION_CPU3);
		int value = RAM[0x0007];
	
		/* Suspend cpu if it's waiting for an interrupt */
		if (cpu_get_pc() == 0x8e79 && !value)
		{
			cpu_spinuntil_int();
		}
	
		return value;
	} };
	
	public static ReadHandlerPtr exterm_sound_ym2151_speedup_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* Doing this won't flash the LED, but we're not emulating that anyhow, so
		   it doesn't matter */
	
		UBytePtr RAM = memory_region(REGION_CPU4);
		int value = RAM[0x02b6];
	
		/* Suspend cpu if it's waiting for an interrupt */
		if (  cpu_get_pc() == 0x8179 &&
			!(value & 0x80) &&
			  RAM[0x00bc] == RAM[0x00bb] &&
			  RAM[0x0092] == 0x00 &&
			  RAM[0x0093] == 0x00 &&
			!(RAM[0x0004] & 0x80))
		{
			cpu_spinuntil_int();
		}
	
		return value;
	} };
	
}
