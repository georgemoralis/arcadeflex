
/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.machine;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.missile.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.pokey.*;
import static gr.codebb.arcadeflex.v036.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.*;


public class missile
{
	
	
	static int ctrld;
	static int h_pos, v_pos;
	

	
	/********************************************************************************************/
	public static ReadHandlerPtr missile_IN0_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (ctrld != 0)	/* trackball */
		{
			if (missile_flipscreen==0)
		  	    return ((readinputport(5) << 4) & 0xf0) | (readinputport(4) & 0x0f);
			else
		  	    return ((readinputport(7) << 4) & 0xf0) | (readinputport(6) & 0x0f);
		}
		else	/* buttons */
			return (readinputport(0));
	} };
	
	
	/********************************************************************************************/
	public static InitMachinePtr missile_init_machine = new InitMachinePtr() { public void handler() 
	{
		h_pos = v_pos = 0;
	} };
	
	
	/********************************************************************************************/
	public static WriteHandlerPtr missile_w = new WriteHandlerPtr() { public void handler(int address, int data)
	{
		int pc, opcode;
	
	
		pc = cpu_getpreviouspc();
		opcode = cpu_readop(pc);
	
		address += 0x640;
	
		/* 3 different ways to write to video ram - the third is caught by the core memory handler */
		if (opcode == 0x81)
		{
			/* 	STA ($00,X) */
			missile_video_w.handler(address, data);
			return;
		}
		if (address <= 0x3fff)
		{
			missile_video_mult_w.handler(address, data);
			return;
		}
	
		/* $4c00 - watchdog */
		if (address == 0x4c00)
		{
			watchdog_reset_w.handler(address, data);
			return;
		}
	
		/* $4800 - various IO */
		if (address == 0x4800)
		{
			if (missile_flipscreen != ((data & 0x40)==0?1:0))
				missile_flip_screen ();
			missile_flipscreen = (data & 0x40)==0?1:0;
			coin_counter_w.handler(0, data & 0x20);
			coin_counter_w.handler(1, data & 0x10);
			coin_counter_w.handler(2, data & 0x08);
			//osd_led_w (0, ~data >> 1);
			//osd_led_w (1, ~data >> 2);
			ctrld = data & 1;
			return;
		}
	
		/* $4d00 - IRQ acknowledge */
		if (address == 0x4d00)
		{
			return;
		}
	
		/* $4000 - $400f - Pokey */
		if (address >= 0x4000 && address <= 0x400f)
		{
			pokey1_w.handler(address, data);
			return;
		}
	
		/* $4b00 - $4b07 - color RAM */
		if (address >= 0x4b00 && address <= 0x4b07)
		{
			int r,g,b;
	
	
			r = 0xff * ((~data >> 3) & 1);
			g = 0xff * ((~data >> 2) & 1);
			b = 0xff * ((~data >> 1) & 1);
	
			palette_change_color(address - 0x4b00,r,g,b);
	
			return;
		}
	
		if (errorlog != null) fprintf (errorlog, "possible unmapped write, offset: %04x, data: %02x\n", address, data);
	} };
	
	
	/********************************************************************************************/
	
	public static UBytePtr missile_video2ram=new UBytePtr();
	
	public static ReadHandlerPtr missile_r = new ReadHandlerPtr() { public int handler(int address)
	{
		int pc, opcode;
	
	
		pc = cpu_getpreviouspc();
		opcode = cpu_readop(pc);
	
		address += 0x1900;
	
		if (opcode == 0xa1)
		{
			/* 	LDA ($00,X)  */
			return (missile_video_r.handler(address));
		}
	
		if (address >= 0x5000)
			return missile_video2ram.read(address - 0x5000);
	
		if (address == 0x4800)
			return (missile_IN0_r.handler(0));
		if (address == 0x4900)
			return (readinputport (1));
		if (address == 0x4a00)
			return (readinputport (2));
	
		if ((address >= 0x4000) && (address <= 0x400f))
			return (pokey1_r.handler(address & 0x0f));
	
		if (errorlog != null) fprintf (errorlog, "possible unmapped read, offset: %04x\n", address);
		return 0;
	} };
}
