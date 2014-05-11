/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.08
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
import static mame.cpuintrfH.*;
import static mame.sndintrf.*;
import static cpu.z80.z80H.*;


public class lwings
{
	
	
	static int lwings_bank_register=0xff;
	
	public static WriteHandlerPtr lwings_bankswitch_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		UBytePtr RAM = memory_region(REGION_CPU1);
		int bank = (data>>1)&0x3;
		cpu_setbank(1,new UBytePtr(RAM,0x10000 + bank*0x4000));
	
		lwings_bank_register=data;
	} };
	
	public static InterruptPtr lwings_interrupt = new InterruptPtr() { public int handler() {
		return 0x00d7; /* RST 10h */
	} };
        static int n_aveng;
        static int s_aveng;
	public static InterruptPtr avengers_interrupt = new InterruptPtr() { public int handler() {/* hack */

		
/*TODO??)		/*if (keyboard_pressed(KEYCODE_S)){ /* test code */
		/*	while (keyboard_pressed(KEYCODE_S))
			{}
			n++;
			n&=0x0f;
			ADPCM_trigger(0, n);
		}*/
	
		if ((lwings_bank_register & 0x08) != 0){ /* NMI enable */			
			s_aveng=NOT(s_aveng);
			if (s_aveng != 0){
				return interrupt.handler();
				//cpu_cause_interrupt(0, 0xd7);
			}
			else {
				return Z80_NMI_INT;
			}
		}
	
		return Z80_IGNORE_INT;
	}};
	
}
