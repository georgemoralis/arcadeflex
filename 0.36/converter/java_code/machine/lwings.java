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

public class lwings
{
	
	
	int lwings_bank_register=0xff;
	
	public static WriteHandlerPtr lwings_bankswitch_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		unsigned char *RAM = memory_region(REGION_CPU1);
		int bank = (data>>1)&0x3;
		cpu_setbank(1,&RAM[0x10000 + bank*0x4000]);
	
		lwings_bank_register=data;
	} };
	
	public static InterruptPtr lwings_interrupt = new InterruptPtr() { public int handler() {
		return 0x00d7; /* RST 10h */
	} };
	
	int avengers_interrupt( void ){ /* hack */
		static int n;
		if (keyboard_pressed(KEYCODE_S)){ /* test code */
			while (keyboard_pressed(KEYCODE_S))
			{}
			n++;
			n&=0x0f;
			ADPCM_trigger(0, n);
		}
	
		if ((lwings_bank_register & 0x08) != 0){ /* NMI enable */
			static int s;
			s=!s;
			if (s != 0){
				return interrupt();
				//cpu_cause_interrupt(0, 0xd7);
			}
			else {
				return Z80_NMI_INT;
			}
		}
	
		return Z80_IGNORE_INT;
	}
	
}
