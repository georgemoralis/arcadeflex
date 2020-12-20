/***************************************************************************

	Nemesis (Hacked?)		GX400
	Nemesis (World?)		GX400
	Twin Bee				GX412
	Gradius					GX456
	Galactic Warriors		GX578
	Konami GT				GX561
	RF2						GX561
	Salamander				GX587
	Lifeforce (US)			GX587
	Lifeforce (Japan)		GX587

driver by Bryan McPhail

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;
import static platform.ptrlib.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.commonH.*;
import static mame.inputport.*;
import static mame.drawgfxH.*;
import static vidhrdw.generic.*;
import static mame.sndintrfH.*;
import static mame.cpuintrf.*;
import static mame.common.*;
import static platform.input.*;
import static mame.inputportH.*;
import static mame.inputH.*;
import static platform.libc.*;
import static platform.libc_old.*;
import static mame.memory.*;
import static mame.mame.*;
import static vidhrdw.ajax.*;
import static mame.sndintrf.*;
import static cpu.z80.z80H.*;
import static cpu.m6809.m6809H.*;
import static cpu.konami.konamiH.*;
import static sound.mixerH.*;
import static mame.cpuintrfH.*;
import static mame.palette.*;
import static sound.vlm5030.*;
import static sound.vlm5030H.*;
import static sound.ay8910.*;
import static sound.ay8910H.*;
import static sound._2151intf.*;
import static sound._2151intfH.*;
import static sound.k005289.*;
import static sound.k005289H.*;
import static sound.k007232.*;
import static sound.k007232H.*;
import static vidhrdw.nemesis.*;


public class nemesis
{
	
	static UBytePtr ram=new UBytePtr();
	static UBytePtr ram2=new UBytePtr();
	

	static int irq_on = 0;
	static int irq1_on = 0;
	static int irq2_on = 0;
	static int irq4_on = 0;
	
	public static InitMachinePtr nemesis_init_machine = new InitMachinePtr() {
        public void handler() {
		irq_on = 0;
		irq1_on = 0;
		irq2_on = 0;
		irq4_on = 0;
	} };
	
	
	
	public static InterruptPtr nemesis_interrupt = new InterruptPtr() { public int handler() 
	{
		if (irq_on!=0) return 1;
	
		return 0;
	} };
	public static WriteHandlerPtr salamand_soundlatch_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		soundlatch_w.handler(offset,data & 0xff);
		cpu_cause_interrupt(1,Z80_IRQ_INT);
	
	//if (errorlog) fprintf(errorlog,"z80 data write\n");
	
	//cpu_cause_interrupt(1,Z80_NMI_INT);
	}};
	public static InterruptPtr konamigt_interrupt = new InterruptPtr() { public int handler() 
	{
		if (cpu_getiloops() == 0)
		{
			if (irq_on!=0)	return 1;
		}
		else
		{
			if (irq2_on!=0) return 2;
		}
	
		return 0;
	} };
	
	public static InterruptPtr gx400_interrupt = new InterruptPtr() { public int handler() 
	{
		switch (cpu_getiloops())
		{
			case 0:
				if (irq1_on!=0) return 1;
				break;
	
			case 1:
				if (irq2_on!=0) return 2;
				break;
	
			case 2:
				if (irq4_on!=0) return 4;
				break;
		}
	
		return 0;
	} };
	
	public static WriteHandlerPtr gx400_irq1_enable_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			irq1_on = data & 0x0001;
	/*	else
	if (errorlog) fprintf(errorlog,"irq1en = %08x\n",data);*/
	} };
	
	public static WriteHandlerPtr gx400_irq2_enable_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			irq2_on = data & 0x0001;
	/*	else
	if (errorlog) fprintf(errorlog,"irq2en = %08x\n",data);*/
	} };
	
	public static WriteHandlerPtr gx400_irq4_enable_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if ((data & 0xff000000) == 0)
			irq4_on = data & 0x0100;
	/*	else
	if (errorlog) fprintf(errorlog,"irq4en = %08x\n",data);*/
	} };
	
	static UBytePtr gx400_shared_ram=new UBytePtr();
	
	
	public static ReadHandlerPtr gx400_sharedram_nosoundfix_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return 2;
	} };
	
	public static ReadHandlerPtr gx400_sharedram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return gx400_shared_ram.read(offset / 2);
	} };
	
	public static WriteHandlerPtr gx400_sharedram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		gx400_shared_ram.write(offset / 2, data);
	} };
	
	
	
	public static InterruptPtr salamand_interrupt = new InterruptPtr() { public int handler() 
	{
		if (irq_on!=0)
			return(1);
		else
			return(0);
	} };
	
	public static WriteHandlerPtr nemesis_irq_enable_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		irq_on = data & 0xff;
	} };
	
	public static WriteHandlerPtr konamigt_irq_enable_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
		{
			irq_on = data & 0xff;
		}
	} };
	public static WriteHandlerPtr konamigt_irq2_enable_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
		{
			irq2_on = data & 0xff;
		}
	} };
	
	public static ReadHandlerPtr konamigt_input_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		int data=readinputport(1);
		int data2=readinputport(6);
	
		int ret=0;
	
		if((data&0x10)!=0) ret|=0x0800;			// turbo/gear?
		if((data&0x80)!=0) ret|=0x0400;			// turbo?
		if((data&0x20)!=0) ret|=0x0300;			// brake		(0-3)
	
		if((data&0x40)!=0) ret|=0xf000;			// accel		(0-f)
	
		ret|=data2&0x7f;					// steering wheel, not exactly sure if DIAL works ok.
	
		return ret;
	} };
	public static WriteHandlerPtr nemesis_soundlatch_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		soundlatch_w.handler(offset,data & 0xff);
	
		/* the IRQ should probably be generated by 5e004, but we'll handle it here for now */
		cpu_cause_interrupt(1,0xff);
	}};
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
		new MemoryReadAddress( 0x040000, 0x04ffff, nemesis_characterram_r ),
		new MemoryReadAddress( 0x050000, 0x0503ff, MRA_BANK1 ),
		new MemoryReadAddress( 0x050400, 0x0507ff, MRA_BANK2 ),
		new MemoryReadAddress( 0x050800, 0x050bff, MRA_BANK3 ),
		new MemoryReadAddress( 0x050c00, 0x050fff, MRA_BANK4 ),
	
		new MemoryReadAddress( 0x052000, 0x053fff, nemesis_videoram1_r ),
		new MemoryReadAddress( 0x054000, 0x055fff, nemesis_videoram2_r ),
		new MemoryReadAddress( 0x056000, 0x056fff, MRA_BANK5 ),
		new MemoryReadAddress( 0x05a000, 0x05afff, paletteram_word_r ),
	
		new MemoryReadAddress( 0x05c400, 0x05c401, input_port_4_r ),	/* DSW0 */
		new MemoryReadAddress( 0x05c402, 0x05c403, input_port_5_r ),	/* DSW1 */
	
		new MemoryReadAddress( 0x05cc00, 0x05cc01, input_port_0_r ),	/* IN0 */
		new MemoryReadAddress( 0x05cc02, 0x05cc03, input_port_1_r ),	/* IN1 */
		new MemoryReadAddress( 0x05cc04, 0x05cc05, input_port_2_r ),	/* IN2 */
		new MemoryReadAddress( 0x05cc06, 0x05cc07, input_port_3_r ),	/* TEST */
	
		new MemoryReadAddress( 0x060000, 0x067fff, MRA_BANK6 ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),	/* ROM */
	
		new MemoryWriteAddress( 0x040000, 0x04ffff, nemesis_characterram_w, nemesis_characterram, nemesis_characterram_size ),
	
		new MemoryWriteAddress( 0x050000, 0x0503ff, MWA_BANK1, nemesis_xscroll1 ),
		new MemoryWriteAddress( 0x050400, 0x0507ff, MWA_BANK2, nemesis_xscroll2 ),
		new MemoryWriteAddress( 0x050800, 0x050bff, MWA_BANK3 ),
		new MemoryWriteAddress( 0x050c00, 0x050fff, MWA_BANK4, nemesis_yscroll ),
		new MemoryWriteAddress( 0x051000, 0x051fff, MWA_NOP ),		/* used, but written to with 0's */
	
		new MemoryWriteAddress( 0x052000, 0x053fff, nemesis_videoram1_w, nemesis_videoram1 ),	/* VRAM 1 */
		new MemoryWriteAddress( 0x054000, 0x055fff, nemesis_videoram2_w, nemesis_videoram2 ),	/* VRAM 2 */
		new MemoryWriteAddress( 0x056000, 0x056fff, MWA_BANK5, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x05a000, 0x05afff, nemesis_palette_w, paletteram ),
	
		new MemoryWriteAddress( 0x05c000, 0x05c001, nemesis_soundlatch_w ),
		new MemoryWriteAddress( 0x05c800, 0x05c801, watchdog_reset_w ),	/* probably */
	
		new MemoryWriteAddress( 0x05e000, 0x05e001, nemesis_irq_enable_w ),	/* Nemesis */
		new MemoryWriteAddress( 0x05e002, 0x05e003, nemesis_irq_enable_w ),	/* Konami GT */
		new MemoryWriteAddress( 0x05e004, 0x05e005, MWA_NOP),	/* bit 8 of the word probably triggers IRQ on sound board */
		new MemoryWriteAddress( 0x060000, 0x067fff, MWA_BANK6, ram ),	/* WORK RAM */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	public static WriteHandlerPtr salamand_speech_start = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	        VLM5030_ST ( 1 );
	        VLM5030_ST ( 0 );
	}};
	public static WriteHandlerPtr gx400_speech_start = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	        /* the voice data is not in a rom but in sound RAM at $8000 */
	        VLM5030_set_rom (new UBytePtr(memory_region(REGION_CPU2), 0x8000));
	        VLM5030_ST (1);
	        VLM5030_ST (0);
	}};
	
	public static ReadHandlerPtr nemesis_portA_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		int TIMER_RATE =1024;
	
		return cpu_gettotalcycles() / TIMER_RATE;
	} };
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new MemoryReadAddress( 0x4000, 0x47ff, MRA_RAM ),
		new MemoryReadAddress( 0xe001, 0xe001, soundlatch_r ),
		new MemoryReadAddress( 0xe086, 0xe086, AY8910_read_port_0_r ),
		new MemoryReadAddress( 0xe205, 0xe205, AY8910_read_port_1_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x3fff, MWA_ROM ),
		new MemoryWriteAddress( 0x4000, 0x47ff, MWA_RAM ),
		new MemoryWriteAddress( 0xa000, 0xafff, k005289_pitch_A_w ),
		new MemoryWriteAddress( 0xc000, 0xcfff, k005289_pitch_B_w ),
		new MemoryWriteAddress( 0xe003, 0xe003, k005289_keylatch_A_w ),
		new MemoryWriteAddress( 0xe004, 0xe004, k005289_keylatch_B_w ),
		new MemoryWriteAddress( 0xe005, 0xe005, AY8910_control_port_1_w ),
		new MemoryWriteAddress( 0xe006, 0xe006, AY8910_control_port_0_w ),
		new MemoryWriteAddress( 0xe106, 0xe106, AY8910_write_port_0_w ),
		new MemoryWriteAddress( 0xe405, 0xe405, AY8910_write_port_1_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress konamigt_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
		new MemoryReadAddress( 0x040000, 0x04ffff, nemesis_characterram_r ),
		new MemoryReadAddress( 0x050000, 0x0503ff, MRA_BANK1 ),
		new MemoryReadAddress( 0x050400, 0x0507ff, MRA_BANK2 ),
		new MemoryReadAddress( 0x050800, 0x050bff, MRA_BANK3 ),
		new MemoryReadAddress( 0x050c00, 0x050fff, MRA_BANK4 ),
	
		new MemoryReadAddress( 0x052000, 0x053fff, nemesis_videoram1_r ),
		new MemoryReadAddress( 0x054000, 0x055fff, nemesis_videoram2_r ),
		new MemoryReadAddress( 0x056000, 0x056fff, MRA_BANK5 ),
		new MemoryReadAddress( 0x05a000, 0x05afff, paletteram_word_r ),
	
		new MemoryReadAddress( 0x05c400, 0x05c401, input_port_4_r ),	/* DSW0 */
		new MemoryReadAddress( 0x05c402, 0x05c403, input_port_5_r ),	/* DSW1 */
	
		new MemoryReadAddress( 0x05cc00, 0x05cc01, input_port_0_r ),	/* IN0 */
		new MemoryReadAddress( 0x05cc02, 0x05cc03, input_port_1_r ),	/* IN1 */
		new MemoryReadAddress( 0x05cc04, 0x05cc05, input_port_2_r ),	/* IN2 */
		new MemoryReadAddress( 0x05cc06, 0x05cc07, input_port_3_r ),	/* TEST */
	
		new MemoryReadAddress( 0x060000, 0x067fff, MRA_BANK6 ),
		new MemoryReadAddress( 0x070000, 0x070001, konamigt_input_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress konamigt_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),	/* ROM */
	
		new MemoryWriteAddress( 0x040000, 0x04ffff, nemesis_characterram_w, nemesis_characterram, nemesis_characterram_size ),
	
		new MemoryWriteAddress( 0x050000, 0x0503ff, MWA_BANK1, nemesis_xscroll1 ),
		new MemoryWriteAddress( 0x050400, 0x0507ff, MWA_BANK2, nemesis_xscroll2 ),
		new MemoryWriteAddress( 0x050800, 0x050bff, MWA_BANK3 ),
		new MemoryWriteAddress( 0x050c00, 0x050fff, MWA_BANK4, nemesis_yscroll ),
		new MemoryWriteAddress( 0x051000, 0x051fff, MWA_NOP ),		/* used, but written to with 0's */
	
		new MemoryWriteAddress( 0x052000, 0x053fff, nemesis_videoram1_w, nemesis_videoram1 ),	/* VRAM 1 */
		new MemoryWriteAddress( 0x054000, 0x055fff, nemesis_videoram2_w, nemesis_videoram2 ),	/* VRAM 2 */
		new MemoryWriteAddress( 0x056000, 0x056fff, MWA_BANK5, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x05a000, 0x05afff, nemesis_palette_w, paletteram ),
	
		new MemoryWriteAddress( 0x05c000, 0x05c001, nemesis_soundlatch_w ),
		new MemoryWriteAddress( 0x05c800, 0x05c801, watchdog_reset_w ),	/* probably */
	
		new MemoryWriteAddress( 0x05e000, 0x05e001, konamigt_irq2_enable_w ),
		new MemoryWriteAddress( 0x05e002, 0x05e003, konamigt_irq_enable_w ),
		new MemoryWriteAddress( 0x05e004, 0x05e005, MWA_NOP),	/* bit 8 of the word probably triggers IRQ on sound board */
		new MemoryWriteAddress( 0x060000, 0x067fff, MWA_BANK6, ram ),	/* WORK RAM */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	static MemoryReadAddress gx400_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x00ffff, MRA_ROM ),
		new MemoryReadAddress( 0x010000, 0x01ffff, MRA_RAM ),
		new MemoryReadAddress( 0x020000, 0x0287ff, gx400_sharedram_r ),
		new MemoryReadAddress( 0x030000, 0x03ffff, nemesis_characterram_r ),
		new MemoryReadAddress( 0x050000, 0x0503ff, MRA_RAM ),
		new MemoryReadAddress( 0x050400, 0x0507ff, MRA_RAM ),
		new MemoryReadAddress( 0x050800, 0x050bff, MRA_RAM ),
		new MemoryReadAddress( 0x050c00, 0x050fff, MRA_RAM ),
		new MemoryReadAddress( 0x052000, 0x053fff, nemesis_videoram1_r ),
		new MemoryReadAddress( 0x054000, 0x055fff, nemesis_videoram2_r ),
		new MemoryReadAddress( 0x056000, 0x056fff, MRA_RAM ),
		new MemoryReadAddress( 0x057000, 0x057fff, MRA_RAM ),
		new MemoryReadAddress( 0x05a000, 0x05afff, paletteram_word_r ),
		new MemoryReadAddress( 0x05c402, 0x05c403, input_port_4_r ),	/* DSW0 */
		new MemoryReadAddress( 0x05c404, 0x05c405, input_port_5_r ),	/* DSW1 */
		new MemoryReadAddress( 0x05c406, 0x05c407, input_port_3_r ),	/* TEST */
		new MemoryReadAddress( 0x05cc00, 0x05cc01, input_port_0_r ),	/* IN0 */
		new MemoryReadAddress( 0x05cc02, 0x05cc03, input_port_1_r ),	/* IN1 */
		new MemoryReadAddress( 0x05cc04, 0x05cc05, input_port_2_r ),	/* IN2 */
		new MemoryReadAddress( 0x060000, 0x07ffff, MRA_RAM ),
		new MemoryReadAddress( 0x080000, 0x0cffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress gx400_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x00ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x010000, 0x01ffff, MWA_RAM , ram ),
		new MemoryWriteAddress( 0x020000, 0x0287ff, gx400_sharedram_w ),
		new MemoryWriteAddress( 0x030000, 0x03ffff, nemesis_characterram_w, nemesis_characterram, nemesis_characterram_size ),
		new MemoryWriteAddress( 0x050000, 0x0503ff, MWA_RAM, nemesis_xscroll1 ),
		new MemoryWriteAddress( 0x050400, 0x0507ff, MWA_RAM, nemesis_xscroll2 ),
		new MemoryWriteAddress( 0x050800, 0x050bff, MWA_RAM ),
		new MemoryWriteAddress( 0x050c00, 0x050fff, MWA_RAM, nemesis_yscroll ),
		new MemoryWriteAddress( 0x051000, 0x051fff, MWA_NOP ),		/* used, but written to with 0's */
		new MemoryWriteAddress( 0x052000, 0x053fff, nemesis_videoram1_w, nemesis_videoram1 ),	/* VRAM 1 */
		new MemoryWriteAddress( 0x054000, 0x055fff, nemesis_videoram2_w, nemesis_videoram2 ),	/* VRAM 2 */
		new MemoryWriteAddress( 0x056000, 0x056fff, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x057000, 0x057fff, MWA_RAM),										/* needed for twinbee */
		new MemoryWriteAddress( 0x05a000, 0x05afff, nemesis_palette_w, paletteram ),
		new MemoryWriteAddress( 0x05c000, 0x05c001, nemesis_soundlatch_w ),
		new MemoryWriteAddress( 0x05c800, 0x05c801, watchdog_reset_w ),	/* probably */
		new MemoryWriteAddress( 0x05e000, 0x05e001, gx400_irq2_enable_w ),	/* ?? */
		new MemoryWriteAddress( 0x05e002, 0x05e003, gx400_irq1_enable_w ),	/* ?? */
		new MemoryWriteAddress( 0x05e004, 0x05e005, MWA_NOP),	/* bit 8 of the word probably triggers IRQ on sound board */
		new MemoryWriteAddress( 0x05e008, 0x05e009, MWA_NOP ),	/* IRQ acknowledge??? */
		new MemoryWriteAddress( 0x05e00e, 0x05e00f, gx400_irq4_enable_w ),	/* ?? */
		new MemoryWriteAddress( 0x060000, 0x07ffff, MWA_RAM , ram2),
		new MemoryWriteAddress( 0x080000, 0x0cffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress rf2_gx400_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x00ffff, MRA_ROM ),
		new MemoryReadAddress( 0x010000, 0x01ffff, MRA_RAM ),
		new MemoryReadAddress( 0x020000, 0x0287ff, gx400_sharedram_r ),
		new MemoryReadAddress( 0x030000, 0x03ffff, nemesis_characterram_r ),
		new MemoryReadAddress( 0x050000, 0x0503ff, MRA_RAM ),
		new MemoryReadAddress( 0x050400, 0x0507ff, MRA_RAM ),
		new MemoryReadAddress( 0x050800, 0x050bff, MRA_RAM ),
		new MemoryReadAddress( 0x050c00, 0x050fff, MRA_RAM ),
		new MemoryReadAddress( 0x052000, 0x053fff, nemesis_videoram1_r ),
		new MemoryReadAddress( 0x054000, 0x055fff, nemesis_videoram2_r ),
		new MemoryReadAddress( 0x056000, 0x056fff, MRA_RAM ),
		new MemoryReadAddress( 0x05a000, 0x05afff, paletteram_word_r ),
		new MemoryReadAddress( 0x05c402, 0x05c403, input_port_4_r ),	/* DSW0 */
		new MemoryReadAddress( 0x05c404, 0x05c405, input_port_5_r ),	/* DSW1 */
		new MemoryReadAddress( 0x05c406, 0x05c407, input_port_3_r ),	/* TEST */
		new MemoryReadAddress( 0x05cc00, 0x05cc01, input_port_0_r ),	/* IN0 */
		new MemoryReadAddress( 0x05cc02, 0x05cc03, input_port_1_r ),	/* IN1 */
		new MemoryReadAddress( 0x05cc04, 0x05cc05, input_port_2_r ),	/* IN2 */
		new MemoryReadAddress( 0x060000, 0x067fff, MRA_RAM ),
		new MemoryReadAddress( 0x070000, 0x070001, konamigt_input_r ),
		new MemoryReadAddress( 0x080000, 0x0cffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress rf2_gx400_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x00ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x010000, 0x01ffff, MWA_RAM , ram2),
		new MemoryWriteAddress( 0x020000, 0x0287ff, gx400_sharedram_w ),
		new MemoryWriteAddress( 0x030000, 0x03ffff, nemesis_characterram_w, nemesis_characterram, nemesis_characterram_size ),
		new MemoryWriteAddress( 0x050000, 0x0503ff, MWA_RAM, nemesis_xscroll1 ),
		new MemoryWriteAddress( 0x050400, 0x0507ff, MWA_RAM, nemesis_xscroll2 ),
		new MemoryWriteAddress( 0x050800, 0x050bff, MWA_RAM ),
		new MemoryWriteAddress( 0x050c00, 0x050fff, MWA_RAM, nemesis_yscroll ),
		new MemoryWriteAddress( 0x051000, 0x051fff, MWA_NOP ),		/* used, but written to with 0's */
		new MemoryWriteAddress( 0x052000, 0x053fff, nemesis_videoram1_w, nemesis_videoram1 ),	/* VRAM 1 */
		new MemoryWriteAddress( 0x054000, 0x055fff, nemesis_videoram2_w, nemesis_videoram2 ),	/* VRAM 2 */
		new MemoryWriteAddress( 0x056000, 0x056fff, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x05a000, 0x05afff, nemesis_palette_w, paletteram ),
		new MemoryWriteAddress( 0x05c000, 0x05c001, nemesis_soundlatch_w ),
		new MemoryWriteAddress( 0x05c800, 0x05c801, watchdog_reset_w ),	/* probably */
		new MemoryWriteAddress( 0x05e000, 0x05e001, gx400_irq2_enable_w ),	/* ?? */
		new MemoryWriteAddress( 0x05e002, 0x05e003, gx400_irq1_enable_w ),	/* ?? */
		new MemoryWriteAddress( 0x05e004, 0x05e005, MWA_NOP), /*	bit 8 of the word probably triggers IRQ on sound board */
		new MemoryWriteAddress( 0x05e008, 0x05e009, MWA_NOP ),	/* IRQ acknowledge??? */
		new MemoryWriteAddress( 0x05e00e, 0x05e00f, gx400_irq4_enable_w ),	/* ?? */
		new MemoryWriteAddress( 0x060000, 0x067fff, MWA_RAM, ram ),	/* WORK RAM */
		new MemoryWriteAddress( 0x080000, 0x0cffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress gx400_sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x1fff, MRA_ROM ),
		new MemoryReadAddress( 0x4000, 0x87ff, MRA_RAM ),
		new MemoryReadAddress( 0xe001, 0xe001, soundlatch_r ),
		new MemoryReadAddress( 0xe086, 0xe086, AY8910_read_port_0_r ),
		new MemoryReadAddress( 0xe205, 0xe205, AY8910_read_port_1_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress gx400_sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x1fff, MWA_ROM ),
		new MemoryWriteAddress( 0x4000, 0x87ff, MWA_RAM, gx400_shared_ram ),
		new MemoryWriteAddress( 0xa000, 0xafff, k005289_pitch_A_w ),
		new MemoryWriteAddress( 0xc000, 0xcfff, k005289_pitch_B_w ),
		new MemoryWriteAddress( 0xe000, 0xe000, VLM5030_data_w ),
		new MemoryWriteAddress( 0xe003, 0xe003, k005289_keylatch_A_w ),
		new MemoryWriteAddress( 0xe004, 0xe004, k005289_keylatch_B_w ),
		new MemoryWriteAddress( 0xe005, 0xe005, AY8910_control_port_1_w ),
		new MemoryWriteAddress( 0xe006, 0xe006, AY8910_control_port_0_w ),
		new MemoryWriteAddress( 0xe030, 0xe030, gx400_speech_start ),
		new MemoryWriteAddress( 0xe106, 0xe106, AY8910_write_port_0_w ),
		new MemoryWriteAddress( 0xe405, 0xe405, AY8910_write_port_1_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	/******************************************************************************/
	
	static MemoryReadAddress salamand_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),  /* ROM BIOS */
		new MemoryReadAddress( 0x080000, 0x087fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x090000, 0x091fff, paletteram_word_r ),
		new MemoryReadAddress( 0x0c0002, 0x0c0003, input_port_3_r ),	/* DSW0 */
		new MemoryReadAddress( 0x0c2000, 0x0c2001, input_port_0_r ),	/* Coins, start buttons, test mode */
		new MemoryReadAddress( 0x0c2002, 0x0c2003, input_port_1_r ),	/* IN1 */
		new MemoryReadAddress( 0x0c2004, 0x0c2005, input_port_2_r ),	/* IN2 */
		new MemoryReadAddress( 0x0c2006, 0x0c2007, input_port_4_r ),	/* DSW1 */
		new MemoryReadAddress( 0x100000, 0x101fff, nemesis_videoram1_r ),
		new MemoryReadAddress( 0x102000, 0x103fff, nemesis_videoram2_r ),
		new MemoryReadAddress( 0x120000, 0x12ffff, nemesis_characterram_r ),
		new MemoryReadAddress( 0x180000, 0x180fff, MRA_BANK7 ),
		new MemoryReadAddress( 0x190000, 0x1903ff, gx400_xscroll1_r ),
		new MemoryReadAddress( 0x190400, 0x1907ff, gx400_xscroll2_r ),
		new MemoryReadAddress( 0x190800, 0x191fff, gx400_yscroll_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress salamand_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x080000, 0x087fff, MWA_BANK1, ram ),
		new MemoryWriteAddress( 0x090000, 0x091fff, salamander_palette_w, paletteram ),
		new MemoryWriteAddress( 0x0A0000, 0x0A0001, nemesis_irq_enable_w ),          /* irq enable */
		new MemoryWriteAddress( 0x0C0000, 0x0C0001, salamand_soundlatch_w ),
		new MemoryWriteAddress( 0x0C0004, 0x0C0005, MWA_NOP ),        /* Watchdog at $c0005 */
		new MemoryWriteAddress( 0x100000, 0x101fff, nemesis_videoram1_w, nemesis_videoram1 ),	/* VRAM 1 */
		new MemoryWriteAddress( 0x102000, 0x103fff, nemesis_videoram2_w, nemesis_videoram2 ),	/* VRAM 2 */
		new MemoryWriteAddress( 0x120000, 0x12ffff, nemesis_characterram_w, nemesis_characterram, nemesis_characterram_size ),
		new MemoryWriteAddress( 0x180000, 0x180fff, MWA_BANK7, spriteram, spriteram_size ),		/* more sprite ram ??? */
		new MemoryWriteAddress( 0x190000, 0x1903ff, gx400_xscroll1_w, nemesis_xscroll1 ),
		new MemoryWriteAddress( 0x190400, 0x1907ff, gx400_xscroll2_w, nemesis_xscroll2 ),
		new MemoryWriteAddress( 0x190800, 0x191fff, gx400_yscroll_w, nemesis_yscroll ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	static int a=1;
	public static ReadHandlerPtr wd_read = new ReadHandlerPtr() { public int handler(int offset)
	{		
		a^= 1;
		return a;
	} };
	
	static MemoryReadAddress sal_sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x87ff, MRA_RAM ),
		new MemoryReadAddress( 0xa000, 0xa000, soundlatch_r ),
		new MemoryReadAddress( 0xb000, 0xb00d, K007232_read_port_0_r ),
		new MemoryReadAddress( 0xc001, 0xc001, YM2151_status_port_0_r ),
		new MemoryReadAddress( 0xe000, 0xe000, wd_read ), /* watchdog?? */
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sal_sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0x87ff, MWA_RAM ),
		new MemoryWriteAddress( 0xb000, 0xb00d, K007232_write_port_0_w ),
		new MemoryWriteAddress( 0xc000, 0xc000, YM2151_register_port_0_w ),
		new MemoryWriteAddress( 0xc001, 0xc001, YM2151_data_port_0_w ),
		new MemoryWriteAddress( 0xd000, 0xd000, VLM5030_data_w ),
		new MemoryWriteAddress( 0xf000, 0xf000, salamand_speech_start ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	/******************************************************************************/
	
		
	
	
	static InputPortPtr input_ports_nemesis = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 	/* TEST */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Version" );
		PORT_DIPSETTING(    0x02, "Normal" );
		PORT_DIPSETTING(    0x00, "Vs" );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") ); 
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") ); 
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") ); 
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") ); 
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") ); 
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") ); 
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") ); 
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") ); 
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") ); 
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") ); 
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") ); 
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") ); 
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") ); 
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") ); 
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") ); 
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") ); 
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") ); 
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") ); 
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") ); 
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") ); 
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") ); 
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") ); 
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") ); 
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") ); 
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") ); 
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") ); 
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") ); 
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") ); 
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") ); 
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") ); 
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") ); 
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") ); 
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") ); 
		PORT_DIPSETTING(    0x00, "Disabled" );
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "3" );
		PORT_DIPSETTING(    0x02, "4" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPSETTING(    0x00, "7" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x18, "50k and every 100k" );
		PORT_DIPSETTING(    0x10, "30k" );
		PORT_DIPSETTING(    0x08, "50k" );
		PORT_DIPSETTING(    0x00, "100k" );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x60, "Easy" );
		PORT_DIPSETTING(    0x40, "Normal" );
		PORT_DIPSETTING(    0x20, "Difficult" );
		PORT_DIPSETTING(    0x00, "Very Difficult" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_nemesuk = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 	/* TEST */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Version" );
		PORT_DIPSETTING(    0x02, "Normal" );
		PORT_DIPSETTING(    0x00, "Vs" );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") ); 
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") ); 
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") ); 
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") ); 
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") ); 
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") ); 
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") ); 
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") ); 
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") ); 
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") ); 
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") ); 
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") ); 
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") ); 
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") ); 
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") ); 
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") ); 
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") ); 
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") ); 
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") ); 
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") ); 
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") ); 
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") ); 
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") ); 
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") ); 
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") ); 
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") ); 
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") ); 
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") ); 
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") ); 
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") ); 
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") ); 
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") ); 
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") ); 
		PORT_DIPSETTING(    0x00, "Disabled" );
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x03, 0x02, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "2" );
		PORT_DIPSETTING(    0x02, "3" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPSETTING(    0x00, "7" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x18, "20k and every 70k" );
		PORT_DIPSETTING(    0x10, "30k and every 80k" );
		PORT_DIPSETTING(    0x08, "20k" );
		PORT_DIPSETTING(    0x00, "30k" );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x60, "Easy" );
		PORT_DIPSETTING(    0x40, "Normal" );
		PORT_DIPSETTING(    0x20, "Difficult" );
		PORT_DIPSETTING(    0x00, "Very Difficult" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	/* This needs to be sorted */
	static InputPortPtr input_ports_konamigt = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON3 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON4 );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 	/* TEST */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") ); 
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") ); 
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") ); 
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") ); 
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") ); 
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") ); 
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") ); 
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") ); 
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") ); 
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") ); 
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") ); 
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") ); 
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") ); 
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") ); 
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") ); 
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") ); 
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") ); 
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") ); 
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") ); 
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") ); 
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") ); 
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") ); 
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") ); 
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") ); 
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") ); 
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") ); 
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") ); 
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") ); 
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") ); 
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") ); 
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") ); 
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") ); 
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") ); 
		PORT_DIPSETTING(    0x00, "Disabled" );
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x01, 0x01, "Unknown" );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Unknown" );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, "Unknown" );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, "Unknown" );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x30, "Easy" );
		PORT_DIPSETTING(    0x20, "Normal" );
		PORT_DIPSETTING(    0x10, "Difficult" );
		PORT_DIPSETTING(    0x00, "Very Difficult" );
		PORT_DIPNAME( 0x40, 0x40, "Unknown" );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* IN6 */
		PORT_ANALOG( 0xff, 0x40, IPT_DIAL, 25, 10, 0x00, 0x7f );
	INPUT_PORTS_END(); }}; 
	
	
	/* This needs to be sorted */
	static InputPortPtr input_ports_rf2 = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON3 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON4 );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 	/* TEST */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Unknown" );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") ); 
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") ); 
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") ); 
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") ); 
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") ); 
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") ); 
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") ); 
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") ); 
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") ); 
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") ); 
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") ); 
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") ); 
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") ); 
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") ); 
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") ); 
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") ); 
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") ); 
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") ); 
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") ); 
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") ); 
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") ); 
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") ); 
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") ); 
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") ); 
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") ); 
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") ); 
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") ); 
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") ); 
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") ); 
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") ); 
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") ); 
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") ); 
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") ); 
		PORT_DIPSETTING(    0x00, "Disabled" );
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x01, 0x00, "Unknown" );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, "Unknown" );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, "Unknown" );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, "Unknown" );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x30, "Easy" );
		PORT_DIPSETTING(    0x20, "Normal" );
		PORT_DIPSETTING(    0x10, "Difficult" );
		PORT_DIPSETTING(    0x00, "Very Difficult" );
		PORT_DIPNAME( 0x40, 0x00, "Unknown" );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* IN6 */
		PORT_ANALOG( 0xff, 0x40, IPT_DIAL, 25, 10, 0x00, 0x7f );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_gwarrior = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* TEST */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Version" );
		PORT_DIPSETTING(    0x02, "Normal" );
		PORT_DIPSETTING(    0x00, "Vs" );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") ); 
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") ); 
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") ); 
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") ); 
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") ); 
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") ); 
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") ); 
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") ); 
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") ); 
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") ); 
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") ); 
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") ); 
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") ); 
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") ); 
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") ); 
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") ); 
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") ); 
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") ); 
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") ); 
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") ); 
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") ); 
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") ); 
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") ); 
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") ); 
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") ); 
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") ); 
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") ); 
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") ); 
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") ); 
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") ); 
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") ); 
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") ); 
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") ); 
		PORT_DIPSETTING(    0x00, "Disabled" );
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x03, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "1" );
		PORT_DIPSETTING(    0x02, "2" );
		PORT_DIPSETTING(    0x01, "3" );
		PORT_DIPSETTING(    0x00, "7" );
		PORT_DIPNAME( 0x04, 0x00, "Unknown" );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x18, "30k 100k 200k 400k" );
		PORT_DIPSETTING(    0x10, "40k 120k 240k 480k" );
		PORT_DIPSETTING(    0x08, "50k 150k 300k 600k" );
		PORT_DIPSETTING(    0x00, "100k 200k 400k 800k" );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x60, "Easy" );
		PORT_DIPSETTING(    0x40, "Normal" );
		PORT_DIPSETTING(    0x20, "Difficult" );
		PORT_DIPSETTING(    0x00, "Very Difficult" );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_twinbee = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
	
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* TEST */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Version" );
		PORT_DIPSETTING(    0x02, "Normal" );
		PORT_DIPSETTING(    0x00, "Vs" );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") ); 
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") ); 
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") ); 
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") ); 
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") ); 
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") ); 
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") ); 
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") ); 
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") ); 
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") ); 
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") ); 
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") ); 
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") ); 
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") ); 
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") ); 
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") ); 
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") ); 
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") ); 
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") ); 
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") ); 
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") ); 
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") ); 
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") ); 
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") ); 
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") ); 
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") ); 
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") ); 
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") ); 
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") ); 
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") ); 
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") ); 
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") ); 
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") ); 
		PORT_DIPSETTING(    0x00, "Disabled" );
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x03, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "2" );
		PORT_DIPSETTING(    0x02, "3" );
		PORT_DIPSETTING(    0x01, "4" );
		PORT_DIPSETTING(    0x00, "7" );
		PORT_DIPNAME( 0x04, 0x00, "Unknown" );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x18, "30k and every 70k" );
		PORT_DIPSETTING(    0x10, "40k and every 80k" );
		PORT_DIPSETTING(    0x08, "50k and every 100k" );
		PORT_DIPSETTING(    0x00, "100k and every 100k" );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x60, "Easy" );
		PORT_DIPSETTING(    0x40, "Normal" );
		PORT_DIPSETTING(    0x20, "Difficult" );
		PORT_DIPSETTING(    0x00, "Very Difficult" );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_gradius = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* TEST */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Version" );
		PORT_DIPSETTING(    0x02, "Normal" );
		PORT_DIPSETTING(    0x00, "Vs" );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") ); 
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") ); 
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") ); 
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") ); 
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") ); 
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") ); 
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") ); 
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") ); 
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") ); 
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") ); 
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") ); 
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") ); 
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") ); 
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") ); 
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") ); 
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") ); 
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") ); 
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") ); 
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") ); 
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") ); 
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") ); 
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") ); 
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") ); 
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") ); 
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") ); 
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") ); 
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") ); 
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") ); 
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") ); 
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") ); 
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") ); 
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") ); 
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") ); 
		PORT_DIPSETTING(    0x00, "Disabled" );
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "3" );
		PORT_DIPSETTING(    0x02, "4" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPSETTING(    0x00, "7" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x18, "20k and every 70k" );
		PORT_DIPSETTING(    0x10, "30k and every 80k" );
		PORT_DIPSETTING(    0x08, "20k only" );
		PORT_DIPSETTING(    0x00, "30k only" );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x60, "Easy" );
		PORT_DIPSETTING(    0x40, "Normal" );
		PORT_DIPSETTING(    0x20, "Difficult" );
		PORT_DIPSETTING(    0x00, "Very Difficult" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_salamand = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_DIPNAME( 0x80, 0x00, "Sound Type" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Cocktail") );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, "Disabled" );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "2" );
		PORT_DIPSETTING(    0x02, "3" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPSETTING(    0x00, "7" );
		PORT_DIPNAME( 0x04, 0x04, "Coin Slot(s)" );
		PORT_DIPSETTING(    0x04, "1" );
		PORT_DIPSETTING(    0x00, "2" );
		PORT_DIPNAME( 0x18, 0x00, "Max Credit(s)" );
		PORT_DIPSETTING(    0x18, "1" );
		PORT_DIPSETTING(    0x10, "3" );
		PORT_DIPSETTING(    0x08, "5" );
		PORT_DIPSETTING(    0x00, "9" );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x60, "Easy" );
		PORT_DIPSETTING(    0x40, "Normal" );
		PORT_DIPSETTING(    0x20, "Difficult" );
		PORT_DIPSETTING(    0x00, "Very Difficult" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_lifefrcj = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_DIPNAME( 0x80, 0x00, "Sound Type" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Cocktail") );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, "Disabled" );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, "Disabled" );
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "3" );
		PORT_DIPSETTING(    0x02, "4" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPSETTING(    0x00, "6" );
		PORT_DIPNAME( 0x04, 0x04, "Coin Counter(s)" );
		PORT_DIPSETTING(    0x04, "1" );
		PORT_DIPSETTING(    0x00, "2" );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x18, "70k and every 200k" );
		PORT_DIPSETTING(    0x10, "100k and every 300k" );
		PORT_DIPSETTING(    0x08, "70k only" );
		PORT_DIPSETTING(    0x00, "100k only" );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x60, "Easy" );
		PORT_DIPSETTING(    0x40, "Normal" );
		PORT_DIPSETTING(    0x20, "Difficult" );
		PORT_DIPSETTING(    0x00, "Very Difficult" );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	/******************************************************************************/
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		2048,	/* 2048 characters */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 }, /* the two bitplanes are merged in the same nibble */
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8     /* every char takes 32 consecutive bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		512,	/* 512 sprites */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 }, /* the two bitplanes are merged in the same nibble */
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4,
				8*4, 9*4, 10*4, 11*4, 12*4, 13*4, 14*4, 15*4 },
		new int[] { 0*64, 1*64, 2*64, 3*64, 4*64, 5*64, 6*64, 7*64,
				8*64, 9*64, 10*64, 11*64, 12*64, 13*64, 14*64, 15*64 },
		128*8     /* every sprite takes 128 consecutive bytes */
	);
	
	static GfxLayout spritelayout3216 = new GfxLayout
	(
		32,16,	/* 32*16 sprites */
		256,	/* 256 sprites */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 }, /* the two bitplanes are merged in the same nibble */
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4,
				8*4, 9*4, 10*4, 11*4, 12*4, 13*4, 14*4, 15*4,
			   16*4,17*4, 18*4, 19*4, 20*4, 21*4, 22*4, 23*4,
			   24*4,25*4, 26*4, 27*4, 28*4, 29*4, 30*4, 31*4},
		new int[] { 0*128, 1*128, 2*128, 3*128, 4*128, 5*128, 6*128, 7*128,
				8*128, 9*128, 10*128, 11*128, 12*128, 13*128, 14*128, 15*128 },
		256*8     /* every sprite takes 128 consecutive bytes */
	);
	
	static GfxLayout spritelayout1632 = new GfxLayout
	(
		16,32,	/* 16*32 sprites */
		256,	/* 256 sprites */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 }, /* the two bitplanes are merged in the same nibble */
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4,
				8*4, 9*4, 10*4, 11*4, 12*4, 13*4, 14*4, 15*4},
		new int[] { 0*64,  1*64,  2*64,  3*64,  4*64,  5*64,  6*64,  7*64,
		  8*64,  9*64, 10*64, 11*64, 12*64, 13*64, 14*64, 15*64,
		 16*64, 17*64, 18*64, 19*64, 20*64, 21*64, 22*64, 23*64,
		 24*64, 25*64, 26*64, 27*64, 28*64, 29*64, 30*64, 31*64},
		256*8     /* every sprite takes 128 consecutive bytes */
	);
	
	static GfxLayout spritelayout3232 = new GfxLayout
	(
		32,32,	/* 32*32 sprites */
		128,	/* 128 sprites */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 }, /* the two bitplanes are merged in the same nibble */
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4,
				8*4, 9*4, 10*4, 11*4, 12*4, 13*4, 14*4, 15*4,
			   16*4,17*4, 18*4, 19*4, 20*4, 21*4, 22*4, 23*4,
			   24*4,25*4, 26*4, 27*4, 28*4, 29*4, 30*4, 31*4},
		new int[] { 0*128, 1*128, 2*128, 3*128, 4*128, 5*128, 6*128, 7*128,
				8*128,  9*128, 10*128, 11*128, 12*128, 13*128, 14*128, 15*128,
			   16*128, 17*128, 18*128, 19*128, 20*128, 21*128, 22*128, 23*128,
			   24*128, 25*128, 26*128, 27*128, 28*128, 29*128, 30*128, 31*128},
		512*8     /* every sprite takes 128 consecutive bytes */
	);
	
	static GfxLayout spritelayout816 = new GfxLayout
	(
		8,16,	/* 16*16 sprites */
		1024,	/* 1024 sprites */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 }, /* the two bitplanes are merged in the same nibble */
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4},
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32,
				8*32, 9*32, 10*32, 11*32, 12*32, 13*32, 14*32, 15*32 },
		64*8     /* every sprite takes 128 consecutive bytes */
	);
	
	static GfxLayout spritelayout168 = new GfxLayout
	(
		16,8,	/* 16*8 sprites */
		1024,	/* 1024 sprites */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 }, /* the two bitplanes are merged in the same nibble */
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4,
				8*4, 9*4, 10*4, 11*4, 12*4, 13*4, 14*4, 15*4},
		new int[] { 0*64, 1*64, 2*64, 3*64, 4*64, 5*64, 6*64, 7*64},
		64*8     /* every sprite takes 128 consecutive bytes */
	
	);
	
	static GfxLayout spritelayout6464 = new GfxLayout
	(
		64,64,	/* 32*32 sprites */
		32,	/* 128 sprites */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 }, /* the two bitplanes are merged in the same nibble */
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4,
				8*4, 9*4, 10*4, 11*4, 12*4, 13*4, 14*4, 15*4,
			   16*4,17*4, 18*4, 19*4, 20*4, 21*4, 22*4, 23*4,
			   24*4,25*4, 26*4, 27*4, 28*4, 29*4, 30*4, 31*4,
			   32*4,33*4, 34*4, 35*4, 36*4, 37*4, 38*4, 39*4,
			   40*4,41*4, 42*4, 43*4, 44*4, 45*4, 46*4, 47*4,
			   48*4,49*4, 50*4, 51*4, 52*4, 53*4, 54*4, 55*4,
			   56*4,57*4, 58*4, 59*4, 60*4, 61*4, 62*4, 63*4},
	
		new int[] { 0*256, 1*256, 2*256, 3*256, 4*256, 5*256, 6*256, 7*256,
				8*256,  9*256, 10*256, 11*256, 12*256, 13*256, 14*256, 15*256,
			   16*256, 17*256, 18*256, 19*256, 20*256, 21*256, 22*256, 23*256,
			   24*256, 25*256, 26*256, 27*256, 28*256, 29*256, 30*256, 31*256,
			   32*256, 33*256, 34*256, 35*256, 36*256, 37*256, 38*256, 39*256,
			   40*256, 41*256, 42*256, 43*256, 44*256, 45*256, 46*256, 47*256,
			   48*256, 49*256, 50*256, 51*256, 52*256, 53*256, 54*256, 55*256,
			   56*256, 57*256, 58*256, 59*256, 60*256, 61*256, 62*256, 63*256},
		2048*8     /* every sprite takes 128 consecutive bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
	    new GfxDecodeInfo( 0, 0x0, charlayout,   0, 0x80 ),	/* the game dynamically modifies this */
	    new GfxDecodeInfo( 0, 0x0, spritelayout, 0, 0x80 ),	/* the game dynamically modifies this */
	    new GfxDecodeInfo( 0, 0x0, spritelayout3216, 0, 0x80 ),	/* the game dynamically modifies this */
	    new GfxDecodeInfo( 0, 0x0, spritelayout816, 0, 0x80 ),	/* the game dynamically modifies this */
	    new GfxDecodeInfo( 0, 0x0, spritelayout3232, 0, 0x80 ),	/* the game dynamically modifies this */
	    new GfxDecodeInfo( 0, 0x0, spritelayout1632, 0, 0x80 ),	/* the game dynamically modifies this */
	    new GfxDecodeInfo( 0, 0x0, spritelayout168, 0, 0x80 ),	/* the game dynamically modifies this */
	    new GfxDecodeInfo( 0, 0x0, spritelayout6464, 0, 0x80 ),	/* the game dynamically modifies this */
		new GfxDecodeInfo( -1 )
	};
	
	/******************************************************************************/
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
		2,      		/* 2 chips */
		14318180/8,     /* 1.78975 Mhz */
		new int[] { 30, 30 },
		new ReadHandlerPtr[] { nemesis_portA_r, null },
		new ReadHandlerPtr[] { null, null },
		new WriteHandlerPtr[] { null, k005289_control_A_w },
		new WriteHandlerPtr[] { null, k005289_control_B_w }
	);
	
	static k005289_interface k005289_interface = new k005289_interface
	(
		3579545/2,		/* clock speed */
		22,				/* playback volume */
		REGION_SOUND1	/* prom memory region */
        );
	public static WriteYmHandlerPtr sound_irq = new WriteYmHandlerPtr() {
        public void handler(int irq) {
	/* Interrupts _are_ generated, I wonder where they go.. */
	/*cpu_cause_interrupt(1,Z80_IRQ_INT);*/
	}};
	
	static YM2151interface ym2151_interface = new YM2151interface
	(
		1,
		3579545,
		new int[]{ YM3012_VOL(45,MIXER_PAN_LEFT,45,MIXER_PAN_RIGHT) },
		new WriteYmHandlerPtr[]{ sound_irq }
       );
	
	static VLM5030interface vlm5030_interface = new VLM5030interface
	(
	    3579545,       /* master clock  */
	    70,            /* volume        */
	    REGION_SOUND1, /* memory region  */
	    0,             /* memory length */
	    0             /* VCU            */
        );
	
	static VLM5030interface gx400_vlm5030_interface = new VLM5030interface
	(
	    3579545,       /* master clock  */
	    100,           /* volume        */
	    0,             /* memory region (RAM based) */
	    0x0800,        /* memory length (not sure if correct) */
	    0             /* VCU            */
        );
	public static portwritehandlerPtr volume_callback = new portwritehandlerPtr() { public void handler(int v)
        {
		K007232_set_volume(0,0,(v >> 4) * 0x11,0);
		K007232_set_volume(0,1,0,(v & 0x0f) * 0x11);
	}};
	
	static K007232_interface k007232_interface = new K007232_interface
	(
		1,		/* number of chips */
		new int[]{ REGION_SOUND2 },	/* memory regions */
		new int[]{ K007232_VOL(15,MIXER_PAN_CENTER,15,MIXER_PAN_CENTER) },	/* volume */
		new portwritehandlerPtr[]{ volume_callback }	/* external port callback */
        );
	
	/******************************************************************************/
	
	static MachineDriver machine_driver_nemesis = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				14318180/2,	/* From schematics, should be accurate */
				readmem,writemem,null,null,
				nemesis_interrupt,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				14318180/4, /* From schematics, should be accurate */
				sound_readmem,sound_writemem,null,null,
				ignore_interrupt,0	/* interrupts are triggered by the main CPU */
			),
		},
	
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		nemesis_init_machine,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		2048, 2048,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		nemesis_vh_start,
		nemesis_vh_stop,
		nemesis_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				ay8910_interface
			),
			new MachineSound(
				SOUND_K005289,
				k005289_interface
			),
			new MachineSound(
				SOUND_VLM5030,
				gx400_vlm5030_interface
			)
		}
	);
	
	static MachineDriver machine_driver_konamigt = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				7159090,     /* ??? */
				konamigt_readmem,konamigt_writemem,null,null,
				konamigt_interrupt,2
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				14318180/4,        /* 3.579545 MHz */
				sound_readmem,sound_writemem,null,null,
				ignore_interrupt,1	/* interrupts are triggered by the main CPU */
			),
		},
	
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		nemesis_init_machine,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		2048, 2048,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		nemesis_vh_start,
		nemesis_vh_stop,
		nemesis_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				ay8910_interface
			),
			new MachineSound(
				SOUND_K005289,
				k005289_interface
			)
		}
	);
	
	static MachineDriver machine_driver_salamand = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				(int)(7159090*1.5),       /* ??? */
				salamand_readmem,salamand_writemem,null,null,
				salamand_interrupt,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				14318180/4,        /* 3.579545 MHz */
				sal_sound_readmem,sal_sound_writemem,null,null,
				ignore_interrupt,0
			),
		},
	
		60, DEFAULT_60HZ_VBLANK_DURATION,
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		nemesis_init_machine,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		2048, 2048,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
		null,
		nemesis_vh_start,
		nemesis_vh_stop,
		salamand_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,//TODO SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_VLM5030,
				vlm5030_interface
			),
			new MachineSound(
				SOUND_K007232,
				k007232_interface
			),
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			)
		}
	);
	
	static MachineDriver machine_driver_gx400 = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				(int)(7159090*1.25),     /* ??? */
				gx400_readmem,gx400_writemem,null,null,
				gx400_interrupt,3
			),
			new MachineCPU(
				CPU_Z80,// | CPU_AUDIO_CPU,
				14318180/4,        /* 3.579545 MHz */
				gx400_sound_readmem,gx400_sound_writemem,null,null,
				nmi_interrupt,1	/* interrupts are triggered by the main CPU */
			),
		},
	
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		nemesis_init_machine,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		2048, 2048,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		nemesis_vh_start,
		nemesis_vh_stop,
		nemesis_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				ay8910_interface
			),
			new MachineSound(
				SOUND_K005289,
				k005289_interface
			),
			new MachineSound(
				SOUND_VLM5030,
				gx400_vlm5030_interface
			)
		}
	);
	
	static MachineDriver machine_driver_twinbee_gx400 = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				(int)(7159090*1.25),     /* ??? */
				gx400_readmem,gx400_writemem,null,null,
				gx400_interrupt,3
			),
			new MachineCPU(
				CPU_Z80, // | CPU_AUDIO_CPU,
				14318180/4,        /* 3.579545 MHz */
				gx400_sound_readmem,gx400_sound_writemem,null,null,
				nmi_interrupt,1	/* interrupts are triggered by the main CPU */
			),
		},
	
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		nemesis_init_machine,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		2048, 2048,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		nemesis_vh_start,
		nemesis_vh_stop,
		twinbee_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				ay8910_interface
			),
			new MachineSound(
				SOUND_K005289,
				k005289_interface
			),
			new MachineSound(
				SOUND_VLM5030,
				gx400_vlm5030_interface
			)
		}
	);
	
	static MachineDriver machine_driver_rf2_gx400 = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				7159090,     /* ??? */
				rf2_gx400_readmem,rf2_gx400_writemem,null,null,
				gx400_interrupt,3
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				14318180/4,        /* 3.579545 MHz */
				gx400_sound_readmem,gx400_sound_writemem,null,null,
				nmi_interrupt,1	/* interrupts are triggered by the main CPU */
			),
		},
	
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		nemesis_init_machine,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		2048, 2048,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		nemesis_vh_start,
		nemesis_vh_stop,
		nemesis_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				ay8910_interface
			),
			new MachineSound(
				SOUND_K005289,
				k005289_interface
			),
			new MachineSound(
				SOUND_VLM5030,
				gx400_vlm5030_interface
			)
		}
	);
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_nemesis = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );   /* 4 * 64k for code and rom */
		ROM_LOAD_EVEN( "12a_01.bin",   0x00000, 0x8000, 0x35ff1aaa );
		ROM_LOAD_ODD ( "12c_05.bin",   0x00000, 0x8000, 0x23155faa );
		ROM_LOAD_EVEN( "13a_02.bin",   0x10000, 0x8000, 0xac0cf163 );
		ROM_LOAD_ODD ( "13c_06.bin",   0x10000, 0x8000, 0x023f22a9 );
		ROM_LOAD_EVEN( "14a_03.bin",   0x20000, 0x8000, 0x8cefb25f );
		ROM_LOAD_ODD ( "14c_07.bin",   0x20000, 0x8000, 0xd50b82cb );
		ROM_LOAD_EVEN( "15a_04.bin",   0x30000, 0x8000, 0x9ca75592 );
		ROM_LOAD_ODD ( "15c_08.bin",   0x30000, 0x8000, 0x03c0b7f5 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );   /* 64k for sound */
		ROM_LOAD(      "09c_snd.bin",  0x00000, 0x4000, 0x26bf9636 );
	
		ROM_REGION( 0x0200,  REGION_SOUND1 );     /* 2x 256 byte for 0005289 wavetable data */
		ROM_LOAD(      "400-a01.fse",  0x00000, 0x0100, 0x5827b1e8 );
		ROM_LOAD(      "400-a02.fse",  0x00100, 0x0100, 0x2f44f970 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_nemesuk = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );   /* 4 * 64k for code and rom */
		ROM_LOAD_EVEN( "12a_01.uk",    0x00000, 0x8000, 0xe1993f91 );
		ROM_LOAD_ODD ( "12c_05.uk",    0x00000, 0x8000, 0xc9761c78 );
		ROM_LOAD_EVEN( "13a_02.uk",    0x10000, 0x8000, 0xf6169c4b );
		ROM_LOAD_ODD ( "13c_06.uk",    0x10000, 0x8000, 0xaf58c548 );
		ROM_LOAD_EVEN( "14a_03.bin",   0x20000, 0x8000, 0x8cefb25f );
		ROM_LOAD_ODD ( "14c_07.bin",   0x20000, 0x8000, 0xd50b82cb );
		ROM_LOAD_EVEN( "15a_04.uk",    0x30000, 0x8000, 0x322423d0 );
		ROM_LOAD_ODD ( "15c_08.uk",    0x30000, 0x8000, 0xeb656266 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );   /* 64k for sound */
		ROM_LOAD(      "09c_snd.bin",  0x00000, 0x4000, 0x26bf9636 );
	
		ROM_REGION( 0x0200,  REGION_SOUND1 );     /* 2x 256 byte for 0005289 wavetable data */
		ROM_LOAD(      "400-a01.fse",  0x00000, 0x0100, 0x5827b1e8 );
		ROM_LOAD(      "400-a02.fse",  0x00100, 0x0100, 0x2f44f970 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_konamigt = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );   /* 4 * 64k for code and rom */
		ROM_LOAD_EVEN( "c01.rom",      0x00000, 0x8000, 0x56245bfd );
		ROM_LOAD_ODD ( "c05.rom",      0x00000, 0x8000, 0x8d651f44 );
		ROM_LOAD_EVEN( "c02.rom",      0x10000, 0x8000, 0x3407b7cb );
		ROM_LOAD_ODD ( "c06.rom",      0x10000, 0x8000, 0x209942d4 );
		ROM_LOAD_EVEN( "b03.rom",      0x20000, 0x8000, 0xaef7df48 );
		ROM_LOAD_ODD ( "b07.rom",      0x20000, 0x8000, 0xe9bd6250 );
		ROM_LOAD_EVEN( "b04.rom",      0x30000, 0x8000, 0x94bd4bd7 );
		ROM_LOAD_ODD ( "b08.rom",      0x30000, 0x8000, 0xb7236567 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );   /* 64k for sound */
		ROM_LOAD(       "b09.rom",      0x00000, 0x4000, 0x539d0c49 );
	
		ROM_REGION( 0x0200,  REGION_SOUND1 );     /* 2x 256 byte for 0005289 wavetable data */
		ROM_LOAD(      "400-a01.fse",  0x00000, 0x0100, 0x5827b1e8 );
		ROM_LOAD(      "400-a02.fse",  0x00100, 0x0100, 0x2f44f970 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_rf2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0xc0000, REGION_CPU1 );   /* 5 * 64k for code and rom */
		ROM_LOAD_EVEN( "400-a06.15l",  0x00000, 0x08000, 0xb99d8cff );
		ROM_LOAD_ODD ( "400-a04.10l",  0x00000, 0x08000, 0xd02c9552 );
		ROM_LOAD_EVEN( "561-a07.17l",  0x80000, 0x20000, 0xed6e7098 );
		ROM_LOAD_ODD ( "561-a05.12l",  0x80000, 0x20000, 0xdfe04425 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );   /* 64k for sound */
		ROM_LOAD(      "400-e03.5l",   0x00000, 0x02000, 0xa5a8e57d );
	
		ROM_REGION( 0x0200,  REGION_SOUND1 );     /* 2x 256 byte for 0005289 wavetable data */
		ROM_LOAD(      "400-a01.fse",  0x00000, 0x0100, 0x5827b1e8 );
		ROM_LOAD(      "400-a02.fse",  0x00100, 0x0100, 0x2f44f970 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_twinbee = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0xc0000, REGION_CPU1 );   /* 5 * 64k for code and rom */
		ROM_LOAD_EVEN( "400-a06.15l",  0x00000, 0x08000, 0xb99d8cff );
		ROM_LOAD_ODD ( "400-a04.10l",  0x00000, 0x08000, 0xd02c9552 );
		ROM_LOAD_EVEN( "412-a07.17l",  0x80000, 0x20000, 0xd93c5499 );
		ROM_LOAD_ODD ( "412-a05.12l",  0x80000, 0x20000, 0x2b357069 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );   /* 64k for sound */
		ROM_LOAD(      "400-e03.5l",   0x00000, 0x02000, 0xa5a8e57d );
	
		ROM_REGION( 0x0200,  REGION_SOUND1 );     /* 2x 256 byte for 0005289 wavetable data */
		ROM_LOAD(      "400-a01.fse",  0x00000, 0x0100, 0x5827b1e8 );
		ROM_LOAD(      "400-a02.fse",  0x00100, 0x0100, 0x2f44f970 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_gradius = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0xc0000, REGION_CPU1 );   /* 5 * 64k for code and rom */
		ROM_LOAD_EVEN( "400-a06.15l",  0x00000, 0x08000, 0xb99d8cff );
		ROM_LOAD_ODD ( "400-a04.10l",  0x00000, 0x08000, 0xd02c9552 );
		ROM_LOAD_EVEN( "456-a07.17l",  0x80000, 0x20000, 0x92df792c );
		ROM_LOAD_ODD ( "456-a05.12l",  0x80000, 0x20000, 0x5cafb263 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );   /* 64k for sound */
		ROM_LOAD(      "400-e03.5l",   0x00000, 0x2000, 0xa5a8e57d );
	
		ROM_REGION( 0x0200,  REGION_SOUND1 );     /* 2x 256 byte for 0005289 wavetable data */
		ROM_LOAD(      "400-a01.fse",  0x00000, 0x0100, 0x5827b1e8 );
		ROM_LOAD(      "400-a02.fse",  0x00100, 0x0100, 0x2f44f970 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_gwarrior = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0xc0000, REGION_CPU1 );   /* 5 * 64k for code and rom */
		ROM_LOAD_EVEN( "400-a06.15l",  0x00000, 0x08000, 0xb99d8cff );
		ROM_LOAD_ODD ( "400-a04.10l",  0x00000, 0x08000, 0xd02c9552 );
		ROM_LOAD_EVEN( "578-a07.17l",  0x80000, 0x20000, 0x0aedacb5 );
		ROM_LOAD_ODD ( "578-a05.12l",  0x80000, 0x20000, 0x76240e2e );
	
		ROM_REGION( 0x10000, REGION_CPU2 );   /* 64k for sound */
		ROM_LOAD(      "400-e03.5l",   0x00000, 0x02000, 0xa5a8e57d );
	
		ROM_REGION( 0x0200,  REGION_SOUND1 );     /* 2x 256 byte for 0005289 wavetable data */
		ROM_LOAD(      "400-a01.fse",  0x00000, 0x0100, 0x5827b1e8 );
		ROM_LOAD(      "400-a02.fse",  0x00100, 0x0100, 0x2f44f970 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_salamand = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );   /* 64k for code */
		ROM_LOAD_EVEN( "18b.bin",      0x00000, 0x10000, 0xa42297f9 );
		ROM_LOAD_ODD ( "18c.bin",      0x00000, 0x10000, 0xf9130b0a );
		ROM_LOAD_EVEN( "17b.bin",      0x40000, 0x20000, 0xe5caf6e6 );
		ROM_LOAD_ODD ( "17c.bin",      0x40000, 0x20000, 0xc2f567ea );
	
		ROM_REGION( 0x10000, REGION_CPU2 );   /* 64k for sound */
		ROM_LOAD(      "11j.bin",      0x00000, 0x08000, 0x5020972c );
	
		ROM_REGION( 0x04000, REGION_SOUND1 );   /* VLM5030 data? */
		ROM_LOAD(      "8g.bin",       0x00000, 0x04000, 0xf9ac6b82 );
	
		ROM_REGION( 0x20000, REGION_SOUND2 );   /* 007232 data */
		ROM_LOAD(      "10a.bin",      0x00000, 0x20000, 0x09fe0632 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_lifefrce = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );   /* 64k for code */
		ROM_LOAD_EVEN( "587-k02.bin",  0x00000, 0x10000, 0x4a44da18 );
		ROM_LOAD_ODD ( "587-k05.bin",  0x00000, 0x10000, 0x2f8c1cbd );
		ROM_LOAD_EVEN( "17b.bin",      0x40000, 0x20000, 0xe5caf6e6 );
		ROM_LOAD_ODD ( "17c.bin",      0x40000, 0x20000, 0xc2f567ea );
	
		ROM_REGION( 0x10000, REGION_CPU2 );   /* 64k for sound */
		ROM_LOAD(      "587-k09.bin",  0x00000, 0x08000, 0x2255fe8c );
	
		ROM_REGION( 0x04000, REGION_SOUND1 );   /* VLM5030 data? */
		ROM_LOAD(      "587-k08.bin",  0x00000, 0x04000, 0x7f0e9b41 );
	
		ROM_REGION( 0x20000, REGION_SOUND2 );   /* 007232 data */
		ROM_LOAD(      "10a.bin",      0x00000, 0x20000, 0x09fe0632 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_lifefrcj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );   /* 64k for code */
		ROM_LOAD_EVEN( "587-n02.bin",  0x00000, 0x10000, 0x235dba71 );
		ROM_LOAD_ODD ( "587-n05.bin",  0x00000, 0x10000, 0x054e569f );
		ROM_LOAD_EVEN( "587-n03.bin",  0x40000, 0x20000, 0x9041f850 );
		ROM_LOAD_ODD ( "587-n06.bin",  0x40000, 0x20000, 0xfba8b6aa );
	
		ROM_REGION( 0x10000, REGION_CPU2 );   /* 64k for sound */
		ROM_LOAD(      "587-n09.bin",  0x00000, 0x08000, 0xe8496150 );
	
		ROM_REGION( 0x04000, REGION_SOUND1 );   /* VLM5030 data? */
		ROM_LOAD(      "587-k08.bin",  0x00000, 0x04000, 0x7f0e9b41 );
	
		ROM_REGION( 0x20000, REGION_SOUND2 );   /* 007232 data */
		ROM_LOAD(      "10a.bin",      0x00000, 0x20000, 0x09fe0632 );
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_nemesis	   = new GameDriver("1985"	,"nemesis"	,"nemesis.java"	,rom_nemesis,null	,machine_driver_nemesis	,input_ports_nemesis	,null	,ROT0	,	"Konami", "Nemesis (hacked?)" );
	public static GameDriver driver_nemesuk	   = new GameDriver("1985"	,"nemesuk"	,"nemesis.java"	,rom_nemesuk,driver_nemesis	,machine_driver_nemesis	,input_ports_nemesuk	,null	,ROT0	,	"Konami", "Nemesis (World?)" );
	public static GameDriver driver_konamigt	   = new GameDriver("1985"	,"konamigt"	,"nemesis.java"	,rom_konamigt,null	,machine_driver_konamigt	,input_ports_konamigt	,null	,ROT0	,	"Konami", "Konami GT" );
	public static GameDriver driver_rf2	   = new GameDriver("1985"	,"rf2"	,"nemesis.java"	,rom_rf2,driver_konamigt	,machine_driver_rf2_gx400	,input_ports_rf2	,null	,ROT0	,	"Konami", "Konami RF2 - Red Fighter" );
	public static GameDriver driver_twinbee	   = new GameDriver("1985"	,"twinbee"	,"nemesis.java"	,rom_twinbee,null	,machine_driver_twinbee_gx400	,input_ports_twinbee	,null	,ORIENTATION_SWAP_XY	,	"Konami", "TwinBee" );
	public static GameDriver driver_gradius	   = new GameDriver("1985"	,"gradius"	,"nemesis.java"	,rom_gradius,driver_nemesis	,machine_driver_gx400	,input_ports_gradius	,null	,ROT0	,	"Konami", "Gradius" );
	public static GameDriver driver_gwarrior	   = new GameDriver("1985"	,"gwarrior"	,"nemesis.java"	,rom_gwarrior,null	,machine_driver_gx400	,input_ports_gwarrior	,null	,ROT0	,	"Konami", "Galactic Warriors" );
	public static GameDriver driver_salamand	   = new GameDriver("1986"	,"salamand"	,"nemesis.java"	,rom_salamand,null	,machine_driver_salamand	,input_ports_salamand	,null	,ROT0	,	"Konami", "Salamander" );
	public static GameDriver driver_lifefrce	   = new GameDriver("1986"	,"lifefrce"	,"nemesis.java"	,rom_lifefrce,driver_salamand	,machine_driver_salamand	,input_ports_salamand	,null	,ROT0	,	"Konami", "Lifeforce (US)" );
	public static GameDriver driver_lifefrcj	   = new GameDriver("1986"	,"lifefrcj"	,"nemesis.java"	,rom_lifefrcj,driver_salamand	,machine_driver_salamand	,input_ports_lifefrcj	,null	,ROT0	,	"Konami", "Lifeforce (Japan)" );
}
