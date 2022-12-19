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

public class jedi
{
	
	static unsigned char jedi_control_num = 0;
	unsigned char jedi_soundlatch;
	unsigned char jedi_soundacklatch;
	unsigned char jedi_com_stat;
	
	public static WriteHandlerPtr jedi_rom_banksel_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
	    if ((data & 0x01) != 0) cpu_setbank (1, &RAM[0x10000]);
	    if ((data & 0x02) != 0) cpu_setbank (1, &RAM[0x14000]);
	    if ((data & 0x04) != 0) cpu_setbank (1, &RAM[0x18000]);
	} };
	
	public static WriteHandlerPtr jedi_sound_reset_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	    if ((data & 1) != 0)
			cpu_set_reset_line(1,CLEAR_LINE);
	    else
			cpu_set_reset_line(1,ASSERT_LINE);
	} };
	
	public static ReadHandlerPtr jedi_control_r  = new ReadHandlerPtr() { public int handler(int offset) {
	
	    if (jedi_control_num == 0)
	        return readinputport (2);
	    else if (jedi_control_num == 2)
	        return readinputport (3);
	    return 0;
	} };
	
	public static WriteHandlerPtr jedi_control_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
	
	    jedi_control_num = offset;
	} };
	
	
	public static WriteHandlerPtr jedi_soundlatch_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
	    jedi_soundlatch = data;
	    jedi_com_stat |= 0x80;
	} };
	
	public static WriteHandlerPtr jedi_soundacklatch_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
	    jedi_soundacklatch = data;
	    jedi_com_stat |= 0x40;
	} };
	
	public static ReadHandlerPtr jedi_soundlatch_r  = new ReadHandlerPtr() { public int handler(int offset) {
	    jedi_com_stat &= 0x7F;
	    return jedi_soundlatch;
	} };
	
	public static ReadHandlerPtr jedi_soundacklatch_r  = new ReadHandlerPtr() { public int handler(int offset) {
	    jedi_com_stat &= 0xBF;
	    return jedi_soundacklatch;
	} };
	
	public static ReadHandlerPtr jedi_soundstat_r  = new ReadHandlerPtr() { public int handler(int offset) {
	    return jedi_com_stat;
	} };
	
	public static ReadHandlerPtr jedi_mainstat_r  = new ReadHandlerPtr() { public int handler(int offset) {
	    unsigned char d;
	
	    d = (jedi_com_stat & 0xC0) >> 1;
	    d = d | (input_port_1_r.handler(0) & 0x80);
	    d = d | 0x1B;
	    return d;
	} };
	
}
