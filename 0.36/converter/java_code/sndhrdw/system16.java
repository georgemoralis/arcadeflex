// system 16 - 7751 emulation, based on monster bash code.
/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package sndhrdw;

public class system16
{
	
	static unsigned int port_8255_c03 = 0;
	static unsigned int port_8255_c47 = 0;
	static unsigned int port_7751_p27 = 0;
	static unsigned int rom_offset = 0;
	static unsigned int rom_base = 0;
	static unsigned int rom_bank = 0;
	
	static void trigger_7751_sound(int data)
	{
		/* I think this is correct for 128k sound roms,
		     it's OK for smaller roms */
		if((data&0xf) == 0xc) rom_bank=0;
		else if((data&0xf) == 0xd) rom_bank=0x4000;
		else if((data&0xf) == 0xb) rom_bank=0xc000;
		else if((data&0xf) == 0xa) rom_bank=0x8000;
	
		else if((data&0xf) == 0xf) rom_bank=0x1c000;
		else if((data&0xf) == 0xe) rom_bank=0x18000;
		else if((data&0xf) == 0x7) rom_bank=0x14000;
		else if((data&0xf) == 0x6) rom_bank=0x10000;
	
		port_8255_c03 = (data>>5);
	
		cpu_cause_interrupt(2,I8039_EXT_INT);
	}
	
	// I'm sure this must be wrong, but it seems to work for quartet music.
	public static WriteHandlerPtr sys16_7751_audio_8255_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (errorlog != 0) fprintf(errorlog,"7751: %4x %4x\n",data,data^0xff);
	
		if ((data & 0x0f) != 8)
		{
			cpu_set_reset_line(2,PULSE_LINE);
			timer_set(TIME_IN_USEC(300), data, trigger_7751_sound);
		}
	} };
	
	
	public static ReadHandlerPtr sys16_7751_audio_8255_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		// Only PC4 is hooked up
		/* 0x00 = BUSY, 0x10 = NOT BUSY */
		return (port_8255_c47 & 0x10);
	} };
	
	/* read from BUS */
	public static ReadHandlerPtr sys16_7751_sh_rom_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		unsigned char *sound_rom = memory_region(REGION_SOUND1);
	
		return sound_rom[rom_offset+rom_base];
	} };
	
	/* read from T1 */
	public static ReadHandlerPtr sys16_7751_sh_t1_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		// Labelled as "TEST", connected to ground
		return 0;
	} };
	
	/* read from P2 */
	public static ReadHandlerPtr sys16_7751_sh_command_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		// 8255's PC0-2 connects to 7751's S0-2 (P24-P26 on an 8048)
		return ((port_8255_c03 & 0x07) << 4) | port_7751_p27;
	} };
	
	/* write to P1 */
	public static WriteHandlerPtr sys16_7751_sh_dac_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		DAC_data_w(0,data);
	} };
	
	/* write to P2 */
	public static WriteHandlerPtr sys16_7751_sh_busy_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		port_8255_c03 = (data & 0x70) >> 4;
		port_8255_c47 = (data & 0x80) >> 3;
		port_7751_p27 = data & 0x80;
		rom_base = rom_bank;
	} };
	
	/* write to P4 */
	public static WriteHandlerPtr sys16_7751_sh_offset_a0_a3_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		rom_offset = (rom_offset & 0xFFF0) | (data & 0x0F);
	} };
	
	/* write to P5 */
	public static WriteHandlerPtr sys16_7751_sh_offset_a4_a7_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		rom_offset = (rom_offset & 0xFF0F) | ((data & 0x0F) << 4);
	} };
	
	/* write to P6 */
	public static WriteHandlerPtr sys16_7751_sh_offset_a8_a11_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		rom_offset = (rom_offset & 0xF0FF) | ((data & 0x0F) << 8);
	} };
	
	/* write to P7 */
	public static WriteHandlerPtr sys16_7751_sh_rom_select_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		rom_offset = (rom_offset & 0x0FFF) | ((0x4000 + ((data&0xf) << 12)) & 0x3000);
	
	} };
	
}
