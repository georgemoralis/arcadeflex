/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package gr.codebb.arcadeflex.v036.drivers;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.platform.input.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.btime.*;
import static gr.codebb.arcadeflex.v036.machine.btime.*;
import static gr.codebb.arcadeflex.v036.sound.samplesH.*;
import static gr.codebb.arcadeflex.v036.mame.memory.*;
import static gr.codebb.arcadeflex.v036.sound.ay8910.*;
import static gr.codebb.arcadeflex.v036.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.cpu.m6502.m6502H.*;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;

public class btime
{

	public static ReadHandlerPtr swap_bits_5_6 = new ReadHandlerPtr() { public int handler(int data)
	{
		return (data & 0x9f) | ((data & 0x20) << 1) | ((data & 0x40) >> 1);
	} };
	
	public static WriteHandlerPtr sound_command_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		soundlatch_w.handler(offset,data);
		cpu_cause_interrupt(1,M6502_INT_IRQ);
	} };
        
	static void btime_decrypt()
	{
		int A,A1;
		UBytePtr rom = memory_region(REGION_CPU1);
		int diff = memory_region_length(REGION_CPU1) / 2;
	
	
		/* the encryption is a simple bit rotation: 76543210 . 65342710, but */
		/* with a catch: it is only applied if the previous instruction did a */
		/* memory write. Also, only opcodes at addresses with this bit pattern: */
		/* xxxx xxx1 xxxx x1xx are encrypted. */
	
		/* get the address of the next opcode */
		A = cpu_get_pc();
	
		/* however if the previous instruction was JSR (which caused a write to */
		/* the stack), fetch the address of the next instruction. */
		A1 = cpu_getpreviouspc();
		if (rom.read(A1 + diff) == 0x20)	/* JSR $xxxx */
			A = cpu_readop_arg(A1+1) + 256 * cpu_readop_arg(A1+2);
	
		/* If the address of the next instruction is xxxx xxx1 xxxx x1xx, decode it. */
		if ((A & 0x0104) == 0x0104)
		{
			/* 76543210 . 65342710 bit rotation */
			rom.write(A + diff, (rom.read(A) & 0x13) | ((rom.read(A) & 0x80) >> 5) | ((rom.read(A) & 0x64) << 1)
				   | ((rom.read(A) & 0x08) << 2));
		}
	}
	
	public static WriteHandlerPtr lnc_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		UBytePtr rom = memory_region(REGION_CPU1);
		int diff = memory_region_length(REGION_CPU1) / 2;
	
		if      (offset <= 0x3bff)                       ;
		else if (offset >= 0x3c00 && offset <= 0x3fff) { lnc_videoram_w.handler(offset - 0x3c00,data); return; }
		else if (offset >= 0x7c00 && offset <= 0x7fff) { lnc_mirrorvideoram_w.handler(offset - 0x7c00,data); return; }
		else if (offset == 0x8000)                     { return; }  /* MWA_NOP */
		else if (offset == 0x8001)                     { lnc_video_control_w.handler(0,data); return; }
		else if (offset == 0x8003)                       ;
		else if (offset == 0x9000)                     { return; }  /* MWA_NOP */
		else if (offset == 0x9002)                     { sound_command_w.handler(0,data); return; }
		else if (offset >= 0xb000 && offset <= 0xb1ff)   ;
		else if (errorlog!=null) fprintf(errorlog,"CPU #%d PC %04x: warning - write %02x to unmapped memory address %04x\n",cpu_getactivecpu(),cpu_get_pc(),data,offset);
	
		rom.write(offset,data);
	
		/* Swap bits 5 & 6 for opcodes */
		rom.write(offset+diff,swap_bits_5_6.handler(data));
	} };
	
	public static WriteHandlerPtr mmonkey_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		UBytePtr rom = memory_region(REGION_CPU1);
		int diff = memory_region_length(REGION_CPU1) / 2;
	
		if      (offset <= 0x3bff)                       ;
		else if (offset >= 0x3c00 && offset <= 0x3fff) { lnc_videoram_w.handler(offset - 0x3c00,data); return; }
		else if (offset >= 0x7c00 && offset <= 0x7fff) { lnc_mirrorvideoram_w.handler(offset - 0x7c00,data); return; }
		else if (offset == 0x8001)                     { lnc_video_control_w.handler(0,data); return; }
		else if (offset == 0x8003)                       ;
		else if (offset == 0x9000)                     { return; }  /* MWA_NOP */
		else if (offset == 0x9002)                     { sound_command_w.handler(0,data); return; }
		else if (offset >= 0xb000 && offset <= 0xbfff) { mmonkey_protection_w.handler(offset - 0xb000, data); return; }
		else if (errorlog!=null) fprintf(errorlog,"CPU #%d PC %04x: warning - write %02x to unmapped memory address %04x\n",cpu_getactivecpu(),cpu_get_pc(),data,offset);
	
		rom.write(offset,data);
	
		/* Swap bits 5 & 6 for opcodes */
		rom.write(offset+diff,swap_bits_5_6.handler(data));
	} };
	
	public static WriteHandlerPtr btime_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		if      (offset <= 0x07ff)                     RAM.write(offset,data);
		else if (offset >= 0x0c00 && offset <= 0x0c0f) btime_paletteram_w.handler(offset - 0x0c00,data);
		else if (offset >= 0x1000 && offset <= 0x13ff) videoram_w.handler(offset - 0x1000,data);
		else if (offset >= 0x1400 && offset <= 0x17ff) colorram_w.handler(offset - 0x1400,data);
		else if (offset >= 0x1800 && offset <= 0x1bff) btime_mirrorvideoram_w.handler(offset - 0x1800,data);
		else if (offset >= 0x1c00 && offset <= 0x1fff) btime_mirrorcolorram_w.handler(offset - 0x1c00,data);
		else if (offset == 0x4002)                     btime_video_control_w.handler(0,data);
		else if (offset == 0x4003)                     sound_command_w.handler(0,data);
		else if (offset == 0x4004)                     bnj_scroll1_w.handler(0,data);
		else if (errorlog!=null) fprintf(errorlog,"CPU #%d PC %04x: warning - write %02x to unmapped memory address %04x\n",cpu_getactivecpu(),cpu_get_pc(),data,offset);
	
		btime_decrypt();
	} };
	
	public static WriteHandlerPtr zoar_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		if      (offset <= 0x07ff) 					   RAM.write(offset,data);
		else if (offset >= 0x8000 && offset <= 0x83ff) videoram_w.handler(offset - 0x8000,data);
		else if (offset >= 0x8400 && offset <= 0x87ff) colorram_w.handler(offset - 0x8400,data);
		else if (offset >= 0x8800 && offset <= 0x8bff) btime_mirrorvideoram_w.handler(offset - 0x8800,data);
		else if (offset >= 0x8c00 && offset <= 0x8fff) btime_mirrorcolorram_w.handler(offset - 0x8c00,data);
		else if (offset == 0x9000)					   zoar_video_control_w.handler(0, data);
		else if (offset >= 0x9800 && offset <= 0x9803) RAM.write(offset,data);
		else if (offset == 0x9804)                     bnj_scroll2_w.handler(0,data);
		else if (offset == 0x9805)                     bnj_scroll1_w.handler(0,data);
		else if (offset == 0x9806)                     sound_command_w.handler(0,data);
		else if (errorlog!=null) fprintf(errorlog,"CPU #%d PC %04x: warning - write %02x to unmapped memory address %04x\n",cpu_getactivecpu(),cpu_get_pc(),data,offset);
	
		btime_decrypt();
	
	} };
	
	public static WriteHandlerPtr disco_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		if      (offset <= 0x04ff)                     RAM.write(offset,data);
		else if (offset >= 0x2000 && offset <= 0x7fff) deco_charram_w.handler(offset - 0x2000,data);
		else if (offset >= 0x8000 && offset <= 0x83ff) videoram_w.handler(offset - 0x8000,data);
		else if (offset >= 0x8400 && offset <= 0x87ff) colorram_w.handler(offset - 0x8400,data);
		else if (offset >= 0x8800 && offset <= 0x881f) RAM.write(offset,data);
		else if (offset == 0x9a00)                     sound_command_w.handler(0,data);
		else if (offset == 0x9c00)                     disco_video_control_w.handler(0,data);
		else if (errorlog!=null) fprintf(errorlog,"CPU #%d PC %04x: warning - write %02x to unmapped memory address %04x\n",cpu_getactivecpu(),cpu_get_pc(),data,offset);
	
		btime_decrypt();
	} };
	
	
	static MemoryReadAddress btime_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x07ff, MRA_RAM ),
		new MemoryReadAddress( 0x1000, 0x17ff, MRA_RAM ),
		new MemoryReadAddress( 0x1800, 0x1bff, btime_mirrorvideoram_r ),
		new MemoryReadAddress( 0x1c00, 0x1fff, btime_mirrorcolorram_r ),
		new MemoryReadAddress( 0x4000, 0x4000, input_port_0_r ),     /* IN0 */
		new MemoryReadAddress( 0x4001, 0x4001, input_port_1_r ),     /* IN1 */
		new MemoryReadAddress( 0x4002, 0x4002, input_port_2_r ),     /* coin */
		new MemoryReadAddress( 0x4003, 0x4003, input_port_3_r ),     /* DSW1 */
		new MemoryReadAddress( 0x4004, 0x4004, input_port_4_r ),     /* DSW2 */
		new MemoryReadAddress( 0xb000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress btime_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xffff, btime_w ),	    /* override the following entries to */
											/* support ROM decryption */
		new MemoryWriteAddress( 0x0000, 0x07ff, MWA_RAM ),
		new MemoryWriteAddress( 0x0c00, 0x0c0f, btime_paletteram_w, paletteram ),
		new MemoryWriteAddress( 0x1000, 0x13ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x1400, 0x17ff, colorram_w, colorram ),
		new MemoryWriteAddress( 0x1800, 0x1bff, btime_mirrorvideoram_w ),
		new MemoryWriteAddress( 0x1c00, 0x1fff, btime_mirrorcolorram_w ),
		new MemoryWriteAddress( 0x4000, 0x4000, MWA_NOP ),
		new MemoryWriteAddress( 0x4002, 0x4002, btime_video_control_w ),
		new MemoryWriteAddress( 0x4003, 0x4003, sound_command_w ),
		new MemoryWriteAddress( 0x4004, 0x4004, bnj_scroll1_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress cookrace_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x03ff, MRA_RAM ),
		new MemoryReadAddress( 0x0500, 0x3fff, MRA_ROM ),
		new MemoryReadAddress( 0xc000, 0xc7ff, MRA_RAM ),
		new MemoryReadAddress( 0xc800, 0xcbff, btime_mirrorvideoram_r ),
		new MemoryReadAddress( 0xcc00, 0xcfff, btime_mirrorcolorram_r ),
		new MemoryReadAddress( 0xd000, 0xd0ff, MRA_RAM ),	/* background */
		new MemoryReadAddress( 0xd100, 0xd3ff, MRA_RAM ),	/* ? */
		new MemoryReadAddress( 0xd400, 0xd7ff, MRA_RAM ),	/* background? */
		new MemoryReadAddress( 0xe000, 0xe000, input_port_3_r ),     /* DSW1 */
		new MemoryReadAddress( 0xe300, 0xe300, input_port_3_r ),     /* mirror address used on high score name enter */
												/* screen */
		new MemoryReadAddress( 0xe001, 0xe001, input_port_4_r ),     /* DSW2 */
		new MemoryReadAddress( 0xe002, 0xe002, input_port_0_r ),     /* IN0 */
		new MemoryReadAddress( 0xe003, 0xe003, input_port_1_r ),     /* IN1 */
		new MemoryReadAddress( 0xe004, 0xe004, input_port_2_r ),     /* coin */
		new MemoryReadAddress( 0xfff9, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress cookrace_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x03ff, MWA_RAM ),
		new MemoryWriteAddress( 0x0500, 0x3fff, MWA_ROM ),
		new MemoryWriteAddress( 0xc000, 0xc3ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0xc400, 0xc7ff, colorram_w, colorram ),
		new MemoryWriteAddress( 0xc800, 0xcbff, btime_mirrorvideoram_w ),
		new MemoryWriteAddress( 0xcc00, 0xcfff, btime_mirrorcolorram_w ),
		new MemoryWriteAddress( 0xd000, 0xd0ff, MWA_RAM ),	/* background? */
		new MemoryWriteAddress( 0xd100, 0xd3ff, MWA_RAM ),	/* ? */
		new MemoryWriteAddress( 0xd400, 0xd7ff, MWA_RAM, bnj_backgroundram, bnj_backgroundram_size ),
		new MemoryWriteAddress( 0xe000, 0xe000, bnj_video_control_w ),
		new MemoryWriteAddress( 0xe001, 0xe001, sound_command_w ),
	/*#if 0
		new MemoryWriteAddress( 0x4004, 0x4004, bnj_scroll1_w ),
	#endif*/
		new MemoryWriteAddress( 0xfff9, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress zoar_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x07ff, MRA_RAM ),
		new MemoryReadAddress( 0x9800, 0x9800, input_port_3_r ),     /* DSW 1 */
		new MemoryReadAddress( 0x9801, 0x9801, input_port_4_r ),     /* DSW 2 */
		new MemoryReadAddress( 0x9802, 0x9802, input_port_0_r ),     /* IN 0 */
		new MemoryReadAddress( 0x9803, 0x9803, input_port_1_r ),     /* IN 1 */
		new MemoryReadAddress( 0x9804, 0x9804, input_port_2_r ),     /* coin */
		new MemoryReadAddress( 0xd000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress zoar_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xffff, zoar_w ),	    /* override the following entries to */
										/* support ROM decryption */
		new MemoryWriteAddress( 0x0000, 0x07ff, MWA_RAM ),
		new MemoryWriteAddress( 0x8000, 0x83ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x8400, 0x87ff, colorram_w, colorram ),
		new MemoryWriteAddress( 0x8800, 0x8bff, btime_mirrorvideoram_w ),
		new MemoryWriteAddress( 0x8c00, 0x8fff, btime_mirrorcolorram_w ),
		new MemoryWriteAddress( 0x9000, 0x9000, zoar_video_control_w ),
		new MemoryWriteAddress( 0x9800, 0x9803, MWA_RAM, zoar_scrollram ),
		new MemoryWriteAddress( 0x9805, 0x9805, bnj_scroll2_w ),
		new MemoryWriteAddress( 0x9805, 0x9805, bnj_scroll1_w ),
		new MemoryWriteAddress( 0x9806, 0x9806, sound_command_w ),
	  /*new MemoryWriteAddress( 0x9807, 0x9807, MWA_RAM ), */ /* Marked as ACK on schematics (Board 2 Pg 5) */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress lnc_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x3fff, MRA_RAM ),
		new MemoryReadAddress( 0x7c00, 0x7fff, btime_mirrorvideoram_r ),
		new MemoryReadAddress( 0x8000, 0x8000, input_port_3_r ),     /* DSW1 */
		new MemoryReadAddress( 0x8001, 0x8001, input_port_4_r ),     /* DSW2 */
		new MemoryReadAddress( 0x9000, 0x9000, input_port_0_r ),     /* IN0 */
		new MemoryReadAddress( 0x9001, 0x9001, input_port_1_r ),     /* IN1 */
		new MemoryReadAddress( 0x9002, 0x9002, input_port_2_r ),     /* coin */
		new MemoryReadAddress( 0xb000, 0xb1ff, MRA_RAM ),
		new MemoryReadAddress( 0xc000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress lnc_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xffff, lnc_w ),      /* override the following entries to */
										/* support ROM decryption */
		new MemoryWriteAddress( 0x0000, 0x3bff, MWA_RAM ),
		new MemoryWriteAddress( 0x3c00, 0x3fff, lnc_videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x7800, 0x7bff, colorram_w, colorram ),  /* this is just here to initialize the pointer */
		new MemoryWriteAddress( 0x7c00, 0x7fff, lnc_mirrorvideoram_w ),
		new MemoryWriteAddress( 0x8000, 0x8000, MWA_NOP ),            /* ??? */
		new MemoryWriteAddress( 0x8001, 0x8001, lnc_video_control_w ),
		new MemoryWriteAddress( 0x8003, 0x8003, MWA_RAM, lnc_charbank ),
		new MemoryWriteAddress( 0x9000, 0x9000, MWA_NOP ),            /* IRQ ACK ??? */
		new MemoryWriteAddress( 0x9002, 0x9002, sound_command_w ),
		new MemoryWriteAddress( 0xb000, 0xb1ff, MWA_RAM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	static MemoryReadAddress mmonkey_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x3fff, MRA_RAM ),
		new MemoryReadAddress( 0x7c00, 0x7fff, btime_mirrorvideoram_r ),
		new MemoryReadAddress( 0x8000, 0x8000, input_port_3_r ),     /* DSW1 */
		new MemoryReadAddress( 0x8001, 0x8001, input_port_4_r ),     /* DSW2 */
		new MemoryReadAddress( 0x9000, 0x9000, input_port_0_r ),     /* IN0 */
		new MemoryReadAddress( 0x9001, 0x9001, input_port_1_r ),     /* IN1 */
		new MemoryReadAddress( 0x9002, 0x9002, input_port_2_r ),     /* coin */
		new MemoryReadAddress( 0xb000, 0xbfff, mmonkey_protection_r ),
		new MemoryReadAddress( 0xc000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress mmonkey_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xffff, mmonkey_w ),  /* override the following entries to */
										/* support ROM decryption */
		new MemoryWriteAddress( 0x0000, 0x3bff, MWA_RAM ),
		new MemoryWriteAddress( 0x3c00, 0x3fff, lnc_videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x7800, 0x7bff, colorram_w, colorram ),  /* this is just here to initialize the pointer */
		new MemoryWriteAddress( 0x7c00, 0x7fff, lnc_mirrorvideoram_w ),
		new MemoryWriteAddress( 0x8001, 0x8001, lnc_video_control_w ),
		new MemoryWriteAddress( 0x8003, 0x8003, MWA_RAM, lnc_charbank ),
		new MemoryWriteAddress( 0x9000, 0x9000, MWA_NOP ),            /* IRQ ACK ??? */
		new MemoryWriteAddress( 0x9002, 0x9002, sound_command_w ),
		new MemoryWriteAddress( 0xb000, 0xbfff, mmonkey_protection_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	static MemoryReadAddress bnj_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x07ff, MRA_RAM ),
		new MemoryReadAddress( 0x1000, 0x1000, input_port_3_r ),     /* DSW1 */
		new MemoryReadAddress( 0x1001, 0x1001, input_port_4_r ),     /* DSW2 */
		new MemoryReadAddress( 0x1002, 0x1002, input_port_0_r ),     /* IN0 */
		new MemoryReadAddress( 0x1003, 0x1003, input_port_1_r ),     /* IN1 */
		new MemoryReadAddress( 0x1004, 0x1004, input_port_2_r ),     /* coin */
		new MemoryReadAddress( 0x4000, 0x47ff, MRA_RAM ),
		new MemoryReadAddress( 0x4800, 0x4bff, btime_mirrorvideoram_r ),
		new MemoryReadAddress( 0x4c00, 0x4fff, btime_mirrorcolorram_r ),
		new MemoryReadAddress( 0xa000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress bnj_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x07ff, MWA_RAM ),
		new MemoryWriteAddress( 0x1001, 0x1001, bnj_video_control_w ),
		new MemoryWriteAddress( 0x1002, 0x1002, sound_command_w ),
		new MemoryWriteAddress( 0x4000, 0x43ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x4400, 0x47ff, colorram_w, colorram ),
		new MemoryWriteAddress( 0x4800, 0x4bff, btime_mirrorvideoram_w ),
		new MemoryWriteAddress( 0x4c00, 0x4fff, btime_mirrorcolorram_w ),
		new MemoryWriteAddress( 0x5000, 0x51ff, bnj_background_w, bnj_backgroundram, bnj_backgroundram_size ),
		new MemoryWriteAddress( 0x5400, 0x5400, bnj_scroll1_w ),
		new MemoryWriteAddress( 0x5800, 0x5800, bnj_scroll2_w ),
		new MemoryWriteAddress( 0x5c00, 0x5c0f, btime_paletteram_w, paletteram ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress disco_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x04ff, MRA_RAM ),
		new MemoryReadAddress( 0x2000, 0x881f, MRA_RAM ),
		new MemoryReadAddress( 0x9000, 0x9000, input_port_2_r ),     /* coin */
		new MemoryReadAddress( 0x9200, 0x9200, input_port_0_r ),     /* IN0 */
		new MemoryReadAddress( 0x9400, 0x9400, input_port_1_r ),     /* IN1 */
		new MemoryReadAddress( 0x9800, 0x9800, input_port_3_r ),     /* DSW1 */
		new MemoryReadAddress( 0x9a00, 0x9a00, input_port_4_r ),     /* DSW2 */
		new MemoryReadAddress( 0x9c00, 0x9c00, input_port_5_r ),     /* VBLANK */
		new MemoryReadAddress( 0xa000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress disco_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xffff, disco_w ),    /* override the following entries to */
										/* support ROM decryption */
		new MemoryWriteAddress( 0x2000, 0x7fff, deco_charram_w, deco_charram ),
		new MemoryWriteAddress( 0x8000, 0x83ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x8400, 0x87ff, colorram_w, colorram ),
		new MemoryWriteAddress( 0x8800, 0x881f, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x9a00, 0x9a00, sound_command_w ),
		new MemoryWriteAddress( 0x9c00, 0x9c00, disco_video_control_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x03ff, MRA_RAM ),
		new MemoryReadAddress( 0x0200, 0x0fff, MRA_ROM ),	/* Cook Race */
		new MemoryReadAddress( 0xa000, 0xafff, soundlatch_r ),
		new MemoryReadAddress( 0xf000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x03ff, MWA_RAM ),
		new MemoryWriteAddress( 0x0200, 0x0fff, MWA_ROM ),	/* Cook Race */
		new MemoryWriteAddress( 0x2000, 0x2fff, AY8910_write_port_0_w ),
		new MemoryWriteAddress( 0x4000, 0x4fff, AY8910_control_port_0_w ),
		new MemoryWriteAddress( 0x6000, 0x6fff, AY8910_write_port_1_w ),
		new MemoryWriteAddress( 0x8000, 0x8fff, AY8910_control_port_1_w ),
		new MemoryWriteAddress( 0xc000, 0xcfff, interrupt_enable_w ),
		new MemoryWriteAddress( 0xf000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	static MemoryReadAddress disco_sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x03ff, MRA_RAM ),
		new MemoryReadAddress( 0x8000, 0x8fff, soundlatch_r ),
		new MemoryReadAddress( 0xf000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress disco_sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x03ff, MWA_RAM ),
		new MemoryWriteAddress( 0x4000, 0x4fff, AY8910_write_port_0_w ),
		new MemoryWriteAddress( 0x5000, 0x5fff, AY8910_control_port_0_w ),
		new MemoryWriteAddress( 0x6000, 0x6fff, AY8910_write_port_1_w ),
		new MemoryWriteAddress( 0x7000, 0x7fff, AY8910_control_port_1_w ),
		new MemoryWriteAddress( 0x8000, 0x8fff, MWA_NOP ),  /* ACK ? */
		new MemoryWriteAddress( 0xf000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	/***************************************************************************
	
	These games don't have VBlank interrupts.
	Interrupts are still used by the game, coin insertion generates an IRQ.
	
	***************************************************************************/
	static int bcoin;
        static int btime_interrupt(InterruptPtr generated_interrupt, int active_high)
	{
		
		int port;
	
		port = readinputport(2) & 0xc0;
		if (active_high!=0) port ^= 0xc0;
	
		if (port != 0xc0)    /* Coin */
		{
			if (bcoin == 0)
			{
				bcoin = 1;
				return generated_interrupt.handler();
			}
		}
		else bcoin = 0;
	
		return ignore_interrupt.handler();
	}
	
	public static InterruptPtr btime_irq_interrupt = new InterruptPtr() { public int handler() 
	{
		return btime_interrupt(interrupt, 1);
	} };
	
	public static InterruptPtr zoar_irq_interrupt = new InterruptPtr() { public int handler() 
	{
		return btime_interrupt(interrupt, 0);
	} };
	
	public static InterruptPtr btime_nmi_interrupt = new InterruptPtr() { public int handler() 
	{
		return btime_interrupt(nmi_interrupt, 0);
	} };
	

	
	
	static InputPortPtr input_ports_btime = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_TILT );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH,IPT_COIN1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH,IPT_COIN2 );
	
		PORT_START();       /* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_3C") );
		PORT_SERVICE( 0x10, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x20, 0x20, "Cross Hatch Pattern" );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Cocktail") );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK  );
	
		PORT_START();       /* DSW2 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x01, "3" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x06, "10000" );
		PORT_DIPSETTING(    0x04, "15000" );
		PORT_DIPSETTING(    0x02, "20000"  );
		PORT_DIPSETTING(    0x00, "30000"  );
		PORT_DIPNAME( 0x08, 0x08, "Enemies" );
		PORT_DIPSETTING(    0x08, "4" );
		PORT_DIPSETTING(    0x00, "6" );
		PORT_DIPNAME( 0x10, 0x10, "End of Level Pepper" );
		PORT_DIPSETTING(    0x10, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_cookrace = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH,IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH,IPT_JOYSTICK_LEFT  | IPF_4WAY );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH,IPT_JOYSTICK_UP    | IPF_4WAY );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH,IPT_JOYSTICK_DOWN  | IPF_4WAY );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH,IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH,IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH,IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH,IPT_UNUSED );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH,IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH,IPT_JOYSTICK_LEFT  | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH,IPT_JOYSTICK_UP    | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH,IPT_JOYSTICK_DOWN  | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH,IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH,IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH,IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH,IPT_UNUSED );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH,IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH,IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH,IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH,IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH,IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH,IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START();       /* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Cocktail") );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK  );
	
		PORT_START();       /* DSW2 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x01, "3" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x06, "20000" );
		PORT_DIPSETTING(    0x04, "30000" );
		PORT_DIPSETTING(    0x02, "40000"  );
		PORT_DIPSETTING(    0x00, "50000"  );
		PORT_DIPNAME( 0x08, 0x08, "Enemies" );
		PORT_DIPSETTING(    0x08, "4" );
		PORT_DIPSETTING(    0x00, "6" );
		PORT_DIPNAME( 0x10, 0x10, "End of Level Pepper" );
		PORT_DIPSETTING(    0x10, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0xc0, 0x80, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0xc0, "Easy" );
		PORT_DIPSETTING(    0x80, "Normal" );
		PORT_DIPSETTING(    0x40, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_zoar = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0xc0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0xc0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN2 );
	
		PORT_START();       /* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );    /* almost certainly unused */
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		/* Service mode doesn't work because of missing ROMs */
		PORT_SERVICE( 0x20, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Cocktail") );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_VBLANK  );
	
		PORT_START();       /* DSW2 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x01, "3" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x06, "5000" );
		PORT_DIPSETTING(    0x04, "10000" );
		PORT_DIPSETTING(    0x02, "15000"  );
		PORT_DIPSETTING(    0x00, "20000"  );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x08, "Easy" );
		PORT_DIPSETTING(    0x00, "Hard" );
		PORT_DIPNAME( 0x10, 0x00, "Weapon Select" );
		PORT_DIPSETTING(    0x00, "Manual" );
		PORT_DIPSETTING(    0x10, "Auto" );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );    /* These 3 switches     */
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );        /* have to do with      */
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );         /* coinage.             */
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );    /* See code at $d234.   */
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );        /* Feel free to figure  */
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );         /* them out.            */
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_lnc = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0xe0, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0xe0, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN2 );
	
		PORT_START();       /* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x30, 0x30, "Test Mode" );
		PORT_DIPSETTING(    0x30, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, "RAM Test Only" );
		PORT_DIPSETTING(    0x20, "Watchdog Test Only" );
		PORT_DIPSETTING(    0x10, "All Tests" );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Cocktail") );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK  );
	
		PORT_START();       /* DSW2 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x01, "3" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x06, "15000" );
		PORT_DIPSETTING(    0x04, "20000" );
		PORT_DIPSETTING(    0x02, "30000" );
		PORT_DIPSETTING(    0x00, "None" );
		PORT_DIPNAME( 0x08, 0x08, "Game Speed" );
		PORT_DIPSETTING(    0x08, "Slow" );
		PORT_DIPSETTING(    0x00, "Hard" );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_wtennis = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0xe0, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0xe0, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN2 );
	
		PORT_START();       /* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Cocktail") );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK  );
	
		PORT_START();       /* DSW2 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x01, "2" );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x06, "10000" );
		PORT_DIPSETTING(    0x04, "20000" );
		PORT_DIPSETTING(    0x02, "30000" );
		PORT_DIPSETTING(    0x00, "None" );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );   /* definately used */
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );    /* These 3 switches     */
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );        /* have to do with      */
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );         /* coinage.             */
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_mmonkey = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0xe0, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0xe0, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN2 );
	
		PORT_START();       /* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Free_Play") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unused") );   /* almost certainly unused */
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Cocktail") );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK  );
	
		PORT_START();       /* DSW2 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x01, "3" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x06, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x02, "Every 15000" );
		PORT_DIPSETTING(    0x04, "Every 30000" );
		PORT_DIPSETTING(    0x00, "20000" );
		PORT_DIPSETTING(    0x06, "None" );
		PORT_DIPNAME( 0x18, 0x08, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x18, "Easy" );
		PORT_DIPSETTING(    0x08, "Medium" );
		PORT_DIPSETTING(    0x10, "Hard" );
		PORT_BITX( 0,       0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Level Skip Mode", IP_KEY_NONE, IP_JOY_NONE );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unused") );   /* almost certainly unused */
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unused") );   /* almost certainly unused */
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_bnj = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN2 );
	
		PORT_START();       /* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_3C") );
		PORT_SERVICE( 0x10, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Cocktail") );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK  );
	
		PORT_START();       /* DSW2 */
		PORT_BIT( 0xe0, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x01, "3" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x06, "Every 30000" );
		PORT_DIPSETTING(    0x04, "Every 70000" );
		PORT_DIPSETTING(    0x02, "20000 Only"  );
		PORT_DIPSETTING(    0x00, "30000 Only"  );
		PORT_DIPNAME( 0x08, 0x00, "Allow Continue" );
		PORT_DIPSETTING(    0x08, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x10, "Easy" );
		PORT_DIPSETTING(    0x00, "Hard" );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_disco = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_4WAY );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_4WAY );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_4WAY );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x60, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_START1 );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x60, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_START2 );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x1f, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH,IPT_COIN1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH,IPT_COIN2 );
	
		PORT_START();       /* DSW1 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x03, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x0c, 0x00, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START();       /* DSW2 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPNAME( 0x06, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "10000" );
		PORT_DIPSETTING(    0x02, "20000" );
		PORT_DIPSETTING(    0x04, "30000" );
		PORT_DIPSETTING(    0x06, "None" );
		PORT_DIPNAME( 0x08, 0x00, "Music Weapons" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPSETTING(    0x08, "8" );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START();       /* VBLANK */
		PORT_BIT( 0x7f, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK );
	INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,    /* 8*8 characters */
		1024,   /* 1024 characters */
		3,      /* 3 bits per pixel */
		new int[] { 2*1024*8*8, 1024*8*8, 0 },    /* the bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8     /* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,  /* 16*16 sprites */
		256,    /* 256 sprites */
		3,      /* 3 bits per pixel */
		new int[] { 2*256*16*16, 256*16*16, 0 },  /* the bitplanes are separated */
		new int[] { 16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7,
		  0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
		  8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		32*8    /* every sprite takes 32 consecutive bytes */
	);
	
	static GfxLayout zoar_spritelayout = new GfxLayout
	(
		16,16,  /* 16*16 sprites */
		128,    /* 256 sprites */
		3,      /* 3 bits per pixel */
		new int[] { 2*128*16*16, 128*16*16, 0 },  /* the bitplanes are separated */
		new int[] { 16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7,
		  0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
		  8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		32*8    /* every sprite takes 32 consecutive bytes */
	);
	
	static GfxLayout btime_tilelayout = new GfxLayout
	(
		16,16,  /* 16*16 characters */
		64,    /* 64 characters */
		3,      /* 3 bits per pixel */
		new int[] {  2*64*16*16, 64*16*16, 0 },    /* the bitplanes are separated */
		new int[] { 16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7,
		  0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
		  8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		32*8    /* every tile takes 32 consecutive bytes */
	);
	
	static GfxLayout cookrace_tilelayout = new GfxLayout
	(
		8,8,  /* 8*8 characters */
		256,    /* 256 characters */
		3,      /* 3 bits per pixel */
		new int[] {  2*256*8*8, 256*8*8, 0 },    /* the bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8    /* every tile takes 8 consecutive bytes */
	);
	
	static GfxLayout bnj_tilelayout = new GfxLayout
	(
		16,16,  /* 16*16 characters */
		64, /* 64 characters */
		3,  /* 3 bits per pixel */
		new int[] { 2*64*16*16+4, 0, 4 },
		new int[] { 3*16*8+0, 3*16*8+1, 3*16*8+2, 3*16*8+3, 2*16*8+0, 2*16*8+1, 2*16*8+2, 2*16*8+3,
		  16*8+0, 16*8+1, 16*8+2, 16*8+3, 0, 1, 2, 3 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
		  8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		64*8    /* every tile takes 64 consecutive bytes */
	);
	
	static GfxDecodeInfo btime_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,          0, 1 ), /* char set #1 */
		new GfxDecodeInfo( REGION_GFX1, 0, spritelayout,        0, 1 ), /* sprites */
		new GfxDecodeInfo( REGION_GFX2, 0, btime_tilelayout,    8, 1 ), /* background tiles */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo cookrace_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,          0, 1 ), /* char set #1 */
		new GfxDecodeInfo( REGION_GFX1, 0, spritelayout,        0, 1 ), /* sprites */
		new GfxDecodeInfo( REGION_GFX2, 0, cookrace_tilelayout, 8, 1 ), /* background tiles */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo lnc_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,          0, 1 ),     /* char set #1 */
		new GfxDecodeInfo( REGION_GFX1, 0, spritelayout,        0, 1 ),     /* sprites */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo bnj_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,          0, 1 ), /* char set #1 */
		new GfxDecodeInfo( REGION_GFX1, 0, spritelayout,        0, 1 ), /* sprites */
		new GfxDecodeInfo( REGION_GFX2, 0, bnj_tilelayout,      8, 1 ), /* background tiles */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo zoar_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,          0, 8 ), /* char set #1 */
		new GfxDecodeInfo( REGION_GFX4, 0, zoar_spritelayout,   0, 8 ), /* sprites */
		new GfxDecodeInfo( REGION_GFX2, 0, btime_tilelayout,    0, 8 ), /* background tiles */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo disco_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( 0, 0x2000, charlayout,          0, 4 ), /* char set #1 */
		new GfxDecodeInfo( 0, 0x2000, spritelayout,        0, 4 ), /* sprites */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
        static AY8910interface ay8910_interface = new AY8910interface
	(
		2,	/* 2 chips */
		1500000,	/* 1.5 MHz ? (hand tuned) */
		new int[]{ 23, 23 },
		new ReadHandlerPtr[]{ null,null },
		new ReadHandlerPtr[]{ null,null },
		new WriteHandlerPtr[]{ null,null },
		new WriteHandlerPtr[]{ null,null }
	);
	
	/********************************************************************
	*
	*  Machine Driver macro
	*  ====================
	*
	*  Abusing the pre-processor.
	*
	********************************************************************/
        static MachineDriver machine_driver_btime = new MachineDriver
	(                                                                   
		/* basic machine hardware */                                	
		new MachineCPU[] {		                                                        
			new MachineCPU(	  	                                                    
				CPU_M6502,                                  			
				1500000,													
				btime_readmem,btime_writemem,null,null, 			
				btime_irq_interrupt,1                                  			
			),		                                                    
			new MachineCPU(		                                                    
				CPU_M6502 | CPU_AUDIO_CPU,                  			
				500000, /* 500 kHz */                       			
				sound_readmem,sound_writemem,null,null, 
				nmi_interrupt,16   /* IRQs are triggered by the main CPU */ 
			)                                                   		
		},                                                          	
		57, 3072,        /* frames per second, vblank duration */   	
		1,      /* 1 CPU slice per frame - interleaving is forced when a sound command is written */ 
		null,		                               	
																		
		/* video hardware */                                        	
		32*8, 32*8, new rectangle( 1*8, 31*8-1, 1*8, 31*8-1 ),                   	
		btime_gfxdecodeinfo,                                                        	
		16,16,                                                	
		btime_vh_convert_color_prom,                           	
																		
		VIDEO_TYPE_RASTER|VIDEO_MODIFIES_PALETTE,                   	
		null,                                                          	
		btime_vh_start,                                        	
		generic_vh_stop,                                         	
		btime_vh_screenrefresh,                                   	
																		
		/* sound hardware */                                        	
		0,0,0,0,                                                    	
		new MachineSound[] {                                                           	
			new MachineSound(                                                   		
				SOUND_AY8910,                               			
				ay8910_interface                           			
			)                                                   		
		}                                                           	
	);
	static MachineDriver machine_driver_cookrace = new MachineDriver
	(                                                                   
		/* basic machine hardware */                                	
		new MachineCPU[] {		                                                        
			new MachineCPU(	  	                                                    
				CPU_M6502,                                  			
				1500000,													
				cookrace_readmem,cookrace_writemem,null,null, 			
				btime_nmi_interrupt,1                                  			
			),		                                                    
			new MachineCPU(		                                                    
				CPU_M6502 | CPU_AUDIO_CPU,                  			
				500000, /* 500 kHz */                       			
				sound_readmem,sound_writemem,null,null, 
				nmi_interrupt,16   /* IRQs are triggered by the main CPU */ 
			)                                                   		
		},                                                          	
		57, 3072,        /* frames per second, vblank duration */   	
		1,      /* 1 CPU slice per frame - interleaving is forced when a sound command is written */ 
		null,		                               	
																		
		/* video hardware */                                        	
		32*8, 32*8, new rectangle( 1*8, 31*8-1, 1*8, 31*8-1 ),                   	
		cookrace_gfxdecodeinfo,                                                        	
		16,16,                                                	
		btime_vh_convert_color_prom,                           	
																		
		VIDEO_TYPE_RASTER|VIDEO_MODIFIES_PALETTE,                   	
		null,                                                          	
		btime_vh_start,                                        	
		generic_vh_stop,                                         	
		cookrace_vh_screenrefresh,                                   	
																		
		/* sound hardware */                                        	
		0,0,0,0,                                                    	
		new MachineSound[] {                                                           	
			new MachineSound(                                                   		
				SOUND_AY8910,                               			
				ay8910_interface                           			
			)                                                   		
		}                                                           	
	);
	static MachineDriver machine_driver_lnc = new MachineDriver
	(                                                                   
		/* basic machine hardware */                                	
		new MachineCPU[] {		                                                        
			new MachineCPU(	  	                                                    
				CPU_M6502,                                  			
				1500000,													
				lnc_readmem,lnc_writemem,null,null, 			
				btime_nmi_interrupt,1                                  			
			),		                                                    
			new MachineCPU(		                                                    
				CPU_M6502 | CPU_AUDIO_CPU,                  			
				500000, /* 500 kHz */                       			
				sound_readmem,sound_writemem,null,null, 
				lnc_sound_interrupt,16   /* IRQs are triggered by the main CPU */ 
			)                                                   		
		},                                                          	
		57, 3072,        /* frames per second, vblank duration */   	
		1,      /* 1 CPU slice per frame - interleaving is forced when a sound command is written */ 
		lnc_init_machine,		                               	
																		
		/* video hardware */                                        	
		32*8, 32*8, new rectangle( 1*8, 31*8-1, 1*8, 31*8-1 ),                   	
		lnc_gfxdecodeinfo,                                                        	
		8,8,                                                	
		lnc_vh_convert_color_prom,                           	
																		
		VIDEO_TYPE_RASTER|VIDEO_MODIFIES_PALETTE,                   	
		null,                                                          	
		btime_vh_start,                                        	
		generic_vh_stop,                                         	
		lnc_vh_screenrefresh,                                   	
																		
		/* sound hardware */                                        	
		0,0,0,0,                                                    	
		new MachineSound[] {                                                           	
			new MachineSound(                                                   		
				SOUND_AY8910,                               			
				ay8910_interface                           			
			)                                                   		
		}                                                           	
	);                        
	static MachineDriver machine_driver_wtennis = new MachineDriver
	(                                                                   
		/* basic machine hardware */                                	
		new MachineCPU[] {		                                                        
			new MachineCPU(	  	                                                    
				CPU_M6502,                                  			
				1500000,													
				lnc_readmem,lnc_writemem,null,null, 			
				btime_nmi_interrupt,1                                  			
			),		                                                    
			new MachineCPU(		                                                    
				CPU_M6502 | CPU_AUDIO_CPU,                  			
				500000, /* 500 kHz */                       			
				sound_readmem,sound_writemem,null,null, 
				nmi_interrupt,16   /* IRQs are triggered by the main CPU */ 
			)                                                   		
		},                                                          	
		57, 3072,        /* frames per second, vblank duration */   	
		1,      /* 1 CPU slice per frame - interleaving is forced when a sound command is written */ 
		lnc_init_machine,		                               	
																		
		/* video hardware */                                        	
		32*8, 32*8, new rectangle( 1*8, 31*8-1, 1*8, 31*8-1 ),                   	
		lnc_gfxdecodeinfo,                                                        	
		8,8,                                                	
		lnc_vh_convert_color_prom,                           	
																		
		VIDEO_TYPE_RASTER|VIDEO_MODIFIES_PALETTE,                   	
		null,                                                          	
		btime_vh_start,                                        	
		generic_vh_stop,                                         	
		eggs_vh_screenrefresh,                                   	
																		
		/* sound hardware */                                        	
		0,0,0,0,                                                    	
		new MachineSound[] {                                                           	
			new MachineSound(                                                   		
				SOUND_AY8910,                               			
				ay8910_interface                           			
			)                                                   		
		}                                                           	
	);	
	static MachineDriver machine_driver_mmonkey = new MachineDriver
	(                                                                   
		/* basic machine hardware */                                	
		new MachineCPU[] {		                                                        
			new MachineCPU(	  	                                                    
				CPU_M6502,                                  			
				1500000,													
				mmonkey_readmem,mmonkey_writemem,null,null, 			
				btime_nmi_interrupt,1                                  			
			),		                                                    
			new MachineCPU(		                                                    
				CPU_M6502 | CPU_AUDIO_CPU,                  			
				500000, /* 500 kHz */                       			
				sound_readmem,sound_writemem,null,null, 
				nmi_interrupt,16   /* IRQs are triggered by the main CPU */ 
			)                                                   		
		},                                                          	
		57, 3072,        /* frames per second, vblank duration */   	
		1,      /* 1 CPU slice per frame - interleaving is forced when a sound command is written */ 
		lnc_init_machine,		                               	
																		
		/* video hardware */                                        	
		32*8, 32*8, new rectangle( 1*8, 31*8-1, 1*8, 31*8-1 ),                   	
		lnc_gfxdecodeinfo,                                                        	
		8,8,                                                	
		lnc_vh_convert_color_prom,                           	
																		
		VIDEO_TYPE_RASTER|VIDEO_MODIFIES_PALETTE,                   	
		null,                                                          	
		btime_vh_start,                                        	
		generic_vh_stop,                                         	
		eggs_vh_screenrefresh,                                   	
																		
		/* sound hardware */                                        	
		0,0,0,0,                                                    	
		new MachineSound[] {                                                           	
			new MachineSound(                                                   		
				SOUND_AY8910,                               			
				ay8910_interface                           			
			)                                                   		
		}                                                           	
	);
	static MachineDriver machine_driver_bnj = new MachineDriver
	(                                                                   
		/* basic machine hardware */                                	
		new MachineCPU[] {		                                                        
			new MachineCPU(	  	                                                    
				CPU_M6502,                                  			
				750000,													
				bnj_readmem,bnj_writemem,null,null, 			
				btime_nmi_interrupt,1                                  			
			),		                                                    
			new MachineCPU(		                                                    
				CPU_M6502 | CPU_AUDIO_CPU,                  			
				500000, /* 500 kHz */                       			
				sound_readmem,sound_writemem,null,null, 
				nmi_interrupt,16   /* IRQs are triggered by the main CPU */ 
			)                                                   		
		},                                                          	
		57, 3072,        /* frames per second, vblank duration */   	
		1,      /* 1 CPU slice per frame - interleaving is forced when a sound command is written */ 
		null,		                               	
																		
		/* video hardware */                                        	
		32*8, 32*8, new rectangle( 1*8, 31*8-1, 1*8, 31*8-1 ),                   	
		bnj_gfxdecodeinfo,                                                        	
		16,16,                                                	
		null,                           	
																		
		VIDEO_TYPE_RASTER|VIDEO_MODIFIES_PALETTE,                   	
		null,                                                          	
		bnj_vh_start,                                        	
		bnj_vh_stop,                                         	
		bnj_vh_screenrefresh,                                   	
																		
		/* sound hardware */                                        	
		0,0,0,0,                                                    	
		new MachineSound[] {                                                           	
			new MachineSound(                                                   		
				SOUND_AY8910,                               			
				ay8910_interface                           			
			)                                                   		
		}                                                           	
	);
	static MachineDriver machine_driver_zoar = new MachineDriver
	(                                                                   
		/* basic machine hardware */                                	
		new MachineCPU[] {		                                                        
			new MachineCPU(	  	                                                    
				CPU_M6502,                                  			
				1500000,													
				zoar_readmem,zoar_writemem,null,null, 			
				zoar_irq_interrupt,1                                  			
			),		                                                    
			new MachineCPU(		                                                    
				CPU_M6502 | CPU_AUDIO_CPU,                  			
				500000, /* 500 kHz */                       			
				sound_readmem,sound_writemem,null,null, 
				nmi_interrupt,16   /* IRQs are triggered by the main CPU */ 
			)                                                   		
		},                                                          	
		57, 3072,        /* frames per second, vblank duration */   	
		1,      /* 1 CPU slice per frame - interleaving is forced when a sound command is written */ 
		null,		                               	
																		
		/* video hardware */                                        	
		32*8, 32*8, new rectangle( 1*8, 31*8-1, 1*8, 31*8-1 ),                   	
		zoar_gfxdecodeinfo,                                                        	
		64,64,                                                	
		btime_vh_convert_color_prom,                           	
																		
		VIDEO_TYPE_RASTER|VIDEO_MODIFIES_PALETTE,                   	
		null,                                                          	
		btime_vh_start,                                        	
		generic_vh_stop,                                         	
		zoar_vh_screenrefresh,                                   	
																		
		/* sound hardware */                                        	
		0,0,0,0,                                                    	
		new MachineSound[] {                                                           	
			new MachineSound(                                                   		
				SOUND_AY8910,                               			
				ay8910_interface                           			
			)                                                   		
		}                                                           	
	);       
	static MachineDriver machine_driver_disco = new MachineDriver
	(                                                                   
		/* basic machine hardware */                                	
		new MachineCPU[] {		                                                        
			new MachineCPU(	  	                                                    
				CPU_M6502,                                  			
				750000,													
				disco_readmem,disco_writemem,null,null, 			
				btime_irq_interrupt,1                                  			
			),		                                                    
			new MachineCPU(		                                                    
				CPU_M6502 | CPU_AUDIO_CPU,                  			
				500000, /* 500 kHz */                       			
				disco_sound_readmem,disco_sound_writemem,null,null, 
				nmi_interrupt,16   /* IRQs are triggered by the main CPU */ 
			)                                                   		
		},                                                          	
		57, 3072,        /* frames per second, vblank duration */   	
		1,      /* 1 CPU slice per frame - interleaving is forced when a sound command is written */ 
		null,		                               	
																		
		/* video hardware */                                        	
		32*8, 32*8, new rectangle( 1*8, 31*8-1, 1*8, 31*8-1 ),                   	
		disco_gfxdecodeinfo,                                                        	
		32,32,                                                	
		 btime_vh_convert_color_prom,                           	
																		
		VIDEO_TYPE_RASTER|VIDEO_MODIFIES_PALETTE,                   	
		null,                                                          	
		btime_vh_start,                                        	
		generic_vh_stop,                                         	
		disco_vh_screenrefresh,                                   	
																		
		/* sound hardware */                                        	
		0,0,0,0,                                                    	
		new MachineSound[] {                                                           	
			new MachineSound(                                                   		
				SOUND_AY8910,                               			
				ay8910_interface                           			
			)                                                   		
		}                                                           	
	);	 

	
	/***************************************************************************
	
		Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_btime = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );/* 64k for code + 64k for decrypted opcodes */
		ROM_LOAD( "aa04.9b",      0xc000, 0x1000, 0x368a25b5 );
		ROM_LOAD( "aa06.13b",     0xd000, 0x1000, 0xb4ba400d );
		ROM_LOAD( "aa05.10b",     0xe000, 0x1000, 0x8005bffa );
		ROM_LOAD( "aa07.15b",     0xf000, 0x1000, 0x086440ad );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the audio CPU */
		ROM_LOAD( "ab14.12h",     0xf000, 0x1000, 0xf55e5211 );
	
		ROM_REGION( 0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "aa12.7k",      0x0000, 0x1000, 0xc4617243 );   /* charset #1 */
		ROM_LOAD( "ab13.9k",      0x1000, 0x1000, 0xac01042f );
		ROM_LOAD( "ab10.10k",     0x2000, 0x1000, 0x854a872a );
		ROM_LOAD( "ab11.12k",     0x3000, 0x1000, 0xd4848014 );
		ROM_LOAD( "aa8.13k",      0x4000, 0x1000, 0x8650c788 );
		ROM_LOAD( "ab9.15k",      0x5000, 0x1000, 0x8dec15e6 );
	
		ROM_REGION( 0x1800, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ab00.1b",      0x0000, 0x0800, 0xc7a14485 );   /* charset #2 */
		ROM_LOAD( "ab01.3b",      0x0800, 0x0800, 0x25b49078 );
		ROM_LOAD( "ab02.4b",      0x1000, 0x0800, 0xb8ef56c3 );
	
		ROM_REGION( 0x0800, REGION_GFX3 );/* background tilemaps */
		ROM_LOAD( "ab03.6b",      0x0000, 0x0800, 0xd26bc1f3 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_btime2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );/* 64k for code + 64k for decrypted opcodes */
		ROM_LOAD( "aa04.9b2",     0xc000, 0x1000, 0xa041e25b );
		ROM_LOAD( "aa06.13b",     0xd000, 0x1000, 0xb4ba400d );
		ROM_LOAD( "aa05.10b",     0xe000, 0x1000, 0x8005bffa );
		ROM_LOAD( "aa07.15b",     0xf000, 0x1000, 0x086440ad );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the audio CPU */
		ROM_LOAD( "ab14.12h",     0xf000, 0x1000, 0xf55e5211 );
	
		ROM_REGION( 0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "aa12.7k",      0x0000, 0x1000, 0xc4617243 );   /* charset #1 */
		ROM_LOAD( "ab13.9k",      0x1000, 0x1000, 0xac01042f );
		ROM_LOAD( "ab10.10k",     0x2000, 0x1000, 0x854a872a );
		ROM_LOAD( "ab11.12k",     0x3000, 0x1000, 0xd4848014 );
		ROM_LOAD( "aa8.13k",      0x4000, 0x1000, 0x8650c788 );
		ROM_LOAD( "ab9.15k",      0x5000, 0x1000, 0x8dec15e6 );
	
		ROM_REGION( 0x1800, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ab00.1b",      0x0000, 0x0800, 0xc7a14485 );   /* charset #2 */
		ROM_LOAD( "ab01.3b",      0x0800, 0x0800, 0x25b49078 );
		ROM_LOAD( "ab02.4b",      0x1000, 0x0800, 0xb8ef56c3 );
	
		ROM_REGION( 0x0800, REGION_GFX3 );/* background tilemaps */
		ROM_LOAD( "ab03.6b",      0x0000, 0x0800, 0xd26bc1f3 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_btimem = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );/* 64k for code + 64k for decrypted opcodes */
		ROM_LOAD( "ab05a1.12b",   0xb000, 0x1000, 0x0a98b230 );
		ROM_LOAD( "ab04.9b",      0xc000, 0x1000, 0x797e5f75 );
		ROM_LOAD( "ab06.13b",     0xd000, 0x1000, 0xc77f3f64 );
		ROM_LOAD( "ab05.10b",     0xe000, 0x1000, 0xb0d3640f );
		ROM_LOAD( "ab07.15b",     0xf000, 0x1000, 0xa142f862 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the audio CPU */
		ROM_LOAD( "ab14.12h",     0xf000, 0x1000, 0xf55e5211 );
	
		ROM_REGION( 0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ab12.7k",      0x0000, 0x1000, 0x6c79f79f );   /* charset #1 */
		ROM_LOAD( "ab13.9k",      0x1000, 0x1000, 0xac01042f );
		ROM_LOAD( "ab10.10k",     0x2000, 0x1000, 0x854a872a );
		ROM_LOAD( "ab11.12k",     0x3000, 0x1000, 0xd4848014 );
		ROM_LOAD( "ab8.13k",      0x4000, 0x1000, 0x70b35bbe );
		ROM_LOAD( "ab9.15k",      0x5000, 0x1000, 0x8dec15e6 );
	
		ROM_REGION( 0x1800, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ab00.1b",      0x0000, 0x0800, 0xc7a14485 );   /* charset #2 */
		ROM_LOAD( "ab01.3b",      0x0800, 0x0800, 0x25b49078 );
		ROM_LOAD( "ab02.4b",      0x1000, 0x0800, 0xb8ef56c3 );
	
		ROM_REGION( 0x0800, REGION_GFX3 );/* background tilemaps */
		ROM_LOAD( "ab03.6b",      0x0000, 0x0800, 0xd26bc1f3 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_cookrace = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );/* 64k for code + 64k for decrypted opcodes */
		/* code is in the range 0500-3fff, encrypted */
		ROM_LOAD( "1f.1",         0x0000, 0x2000, 0x68759d32 );
		ROM_LOAD( "2f.2",         0x2000, 0x2000, 0xbe7d72d1 );
		ROM_LOAD( "2k",           0xffe0, 0x0020, 0xe2553b3d );/* reset/interrupt vectors */
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the audio CPU */
		ROM_LOAD( "6f.6",         0x0000, 0x1000, 0x6b8e0272 );/* starts at 0000, not f000; 0000-01ff is RAM */
		ROM_RELOAD(               0xf000, 0x1000 );    /* for the reset/interrupt vectors */
	
		ROM_REGION( 0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "m8.7",         0x0000, 0x2000, 0xa1a0d5a6 ); /* charset #1 */
		ROM_LOAD( "m7.8",         0x2000, 0x2000, 0x1104f497 );
		ROM_LOAD( "m6.9",         0x4000, 0x2000, 0xd0d94477 );
	
		ROM_REGION( 0x1800, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "2f.3",         0x0000, 0x0800, 0x28609a75 ); /* garbage?? */
		ROM_CONTINUE(             0x0000, 0x0800 );             /* charset #2 */
		ROM_LOAD( "4f.4",         0x0800, 0x0800, 0x7742e771 ); /* garbage?? */
		ROM_CONTINUE(             0x0800, 0x0800 );
		ROM_LOAD( "5f.5",         0x1000, 0x0800, 0x611c686f ); /* garbage?? */
		ROM_CONTINUE(             0x1000, 0x0800 );
	
		ROM_REGION( 0x0040, REGION_PROMS );
		ROM_LOAD( "f9.clr",       0x0000, 0x0020, 0xc2348c1d );/* palette */
		ROM_LOAD( "b7",           0x0020, 0x0020, 0xe4268fa6 );/* unknown */
	ROM_END(); }}; 
	
	/* There is a flyer with a screen shot for Lock'n'Chase at:
	   http://www.gamearchive.com/flyers/video/taito/locknchase_f.jpg  */
	
	static RomLoadPtr rom_lnc = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );/* 64k for code + 64k for decrypted opcodes */
		ROM_LOAD( "s3-3d",        0xc000, 0x1000, 0x1ab4f2c2 );
		ROM_LOAD( "s2-3c",        0xd000, 0x1000, 0x5e46b789 );
		ROM_LOAD( "s1-3b",        0xe000, 0x1000, 0x1308a32e );
		ROM_LOAD( "s0-3a",        0xf000, 0x1000, 0xbeb4b1fc );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the audio CPU */
		ROM_LOAD( "sa-1h",        0xf000, 0x1000, 0x379387ec );
	
		ROM_REGION( 0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "s4-11l",       0x0000, 0x1000, 0xa2162a9e );
		ROM_LOAD( "s5-11m",       0x1000, 0x1000, 0x12f1c2db );
		ROM_LOAD( "s6-13l",       0x2000, 0x1000, 0xd21e2a57 );
		ROM_LOAD( "s7-13m",       0x3000, 0x1000, 0xc4f247cd );
		ROM_LOAD( "s8-15l",       0x4000, 0x1000, 0x672a92d0 );
		ROM_LOAD( "s9-15m",       0x5000, 0x1000, 0x87c8ee9a );
	
		ROM_REGION( 0x0040, REGION_PROMS );
		ROM_LOAD( "sc-5m",        0x0000, 0x0020, 0x2a976ebe );/* palette */
		ROM_LOAD( "sb-4c",        0x0020, 0x0020, 0xa29b4204 );/* RAS/CAS logic - not used */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_wtennis = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );/* 64k for code + 64k for decrypted opcodes */
		ROM_LOAD( "tx",           0xc000, 0x0800, 0xfd343474 );
		ROM_LOAD( "t4",           0xd000, 0x1000, 0xe465d82c );
		ROM_LOAD( "t3",           0xe000, 0x1000, 0x8f090eab );
		ROM_LOAD( "t2",           0xf000, 0x1000, 0xd2f9dd30 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the audio CPU */
		ROM_LOAD( "t1",           0x0000, 0x1000, 0x40737ea7 );/* starts at 0000, not f000; 0000-01ff is RAM */
		ROM_RELOAD(               0xf000, 0x1000 );    /* for the reset/interrupt vectors */
	
		ROM_REGION( 0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "t7",           0x0000, 0x1000, 0xaa935169 );
		ROM_LOAD( "t10",          0x1000, 0x1000, 0x746be927 );
		ROM_LOAD( "t5",           0x2000, 0x1000, 0xea1efa5d );
		ROM_LOAD( "t8",           0x3000, 0x1000, 0x542ace7b );
		ROM_LOAD( "t6",           0x4000, 0x1000, 0x4fb8565d );
		ROM_LOAD( "t9",           0x5000, 0x1000, 0x4893286d );
	
		ROM_REGION( 0x0040, REGION_PROMS );
		ROM_LOAD( "mb7051.m5",    0x0000, 0x0020, 0xf051cb28 );/* palette */
		ROM_LOAD( "sb-4c",        0x0020, 0x0020, 0xa29b4204 );/* RAS/CAS logic - not used */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_mmonkey = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );/* 64k for code + 64k for decrypted opcodes */
		ROM_LOAD( "mmonkey.e4",   0xc000, 0x1000, 0x8d31bf6a );
		ROM_LOAD( "mmonkey.d4",   0xd000, 0x1000, 0xe54f584a );
		ROM_LOAD( "mmonkey.b4",   0xe000, 0x1000, 0x399a161e );
		ROM_LOAD( "mmonkey.a4",   0xf000, 0x1000, 0xf7d3d1e3 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the audio CPU */
		ROM_LOAD( "mmonkey.h1",   0xf000, 0x1000, 0x5bcb2e81 );
	
		ROM_REGION( 0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "mmonkey.l11",  0x0000, 0x1000, 0xb6aa8566 );
		ROM_LOAD( "mmonkey.m11",  0x1000, 0x1000, 0x6cc4d0c4 );
		ROM_LOAD( "mmonkey.l13",  0x2000, 0x1000, 0x2a343b7e );
		ROM_LOAD( "mmonkey.m13",  0x3000, 0x1000, 0x0230b50d );
		ROM_LOAD( "mmonkey.l14",  0x4000, 0x1000, 0x922bb3e1 );
		ROM_LOAD( "mmonkey.m14",  0x5000, 0x1000, 0xf943e28c );
	
		ROM_REGION( 0x0040, REGION_PROMS );
		ROM_LOAD( "mmi6331.m5",   0x0000, 0x0020, 0x55e28b32 );/* palette */
		ROM_LOAD( "sb-4c",        0x0020, 0x0020, 0xa29b4204 );/* RAS/CAS logic - not used */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_brubber = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );/* 64k for code + 64k for decrypted opcodes */
		/* a000-bfff space for the service ROM */
		ROM_LOAD( "brubber.12c",  0xc000, 0x2000, 0xb5279c70 );
		ROM_LOAD( "brubber.12d",  0xe000, 0x2000, 0xb2ce51f5 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the audio CPU */
		ROM_LOAD( "bnj6c.bin",    0xf000, 0x1000, 0x8c02f662 );
	
		ROM_REGION( 0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "bnj4e.bin",    0x0000, 0x2000, 0xb864d082 );
		ROM_LOAD( "bnj4f.bin",    0x2000, 0x2000, 0x6c31d77a );
		ROM_LOAD( "bnj4h.bin",    0x4000, 0x2000, 0x5824e6fb );
	
		ROM_REGION( 0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "bnj10e.bin",   0x0000, 0x1000, 0xf4e9eb49 );
		ROM_LOAD( "bnj10f.bin",   0x1000, 0x1000, 0xa9ffacb4 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_bnj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );/* 64k for code + 64k for decrypted opcodes */
		ROM_LOAD( "bnj12b.bin",   0xa000, 0x2000, 0xba3e3801 );
		ROM_LOAD( "bnj12c.bin",   0xc000, 0x2000, 0xfb3a2cdd );
		ROM_LOAD( "bnj12d.bin",   0xe000, 0x2000, 0xb88bc99e );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the audio CPU */
		ROM_LOAD( "bnj6c.bin",    0xf000, 0x1000, 0x8c02f662 );
	
		ROM_REGION( 0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "bnj4e.bin",    0x0000, 0x2000, 0xb864d082 );
		ROM_LOAD( "bnj4f.bin",    0x2000, 0x2000, 0x6c31d77a );
		ROM_LOAD( "bnj4h.bin",    0x4000, 0x2000, 0x5824e6fb );
	
		ROM_REGION( 0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "bnj10e.bin",   0x0000, 0x1000, 0xf4e9eb49 );
		ROM_LOAD( "bnj10f.bin",   0x1000, 0x1000, 0xa9ffacb4 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_caractn = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );/* 64k for code + 64k for decrypted opcodes */
		/* a000-bfff space for the service ROM */
		ROM_LOAD( "brubber.12c",  0xc000, 0x2000, 0xb5279c70 );
		ROM_LOAD( "caractn.a6",   0xe000, 0x2000, 0x1d6957c4 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the audio CPU */
		ROM_LOAD( "bnj6c.bin",    0xf000, 0x1000, 0x8c02f662 );
	
		ROM_REGION( 0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "caractn.a0",   0x0000, 0x2000, 0xbf3ea732 );
		ROM_LOAD( "caractn.a1",   0x2000, 0x2000, 0x9789f639 );
		ROM_LOAD( "caractn.a2",   0x4000, 0x2000, 0x51dcc111 );
	
		ROM_REGION( 0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "bnj10e.bin",   0x0000, 0x1000, 0xf4e9eb49 );
		ROM_LOAD( "bnj10f.bin",   0x1000, 0x1000, 0xa9ffacb4 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_zoar = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );/* 64k for code + 64k for decrypted opcodes */
		ROM_LOAD( "zoar15",       0xd000, 0x1000, 0x1f0cfdb7 );
		ROM_LOAD( "zoar16",       0xe000, 0x1000, 0x7685999c );
		ROM_LOAD( "zoar17",       0xf000, 0x1000, 0x619ea867 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );     /* 64k for the audio CPU */
		ROM_LOAD( "zoar09",       0xf000, 0x1000, 0x18d96ff1 );
	
		ROM_REGION( 0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "zoar00",       0x0000, 0x1000, 0xfd2dcb64 );
		ROM_LOAD( "zoar01",       0x1000, 0x1000, 0x74d3ca48 );
		ROM_LOAD( "zoar03",       0x2000, 0x1000, 0x77b7df14 );
		ROM_LOAD( "zoar04",       0x3000, 0x1000, 0x9be786de );
		ROM_LOAD( "zoar06",       0x4000, 0x1000, 0x07638c71 );
		ROM_LOAD( "zoar07",       0x5000, 0x1000, 0xf4710f25 );
	
		ROM_REGION( 0x1800, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "zoar10",       0x0000, 0x0800, 0xaa8bcab8 );
		ROM_LOAD( "zoar11",       0x0800, 0x0800, 0xdcdad357 );
		ROM_LOAD( "zoar12",       0x1000, 0x0800, 0xed317e40 );
	
		ROM_REGION( 0x1000, REGION_GFX3 );/* background tilemaps */
		ROM_LOAD( "zoar13",       0x0000, 0x1000, 0x8fefa960 );
	
		ROM_REGION( 0x3000, REGION_GFX4 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "zoar02",       0x0000, 0x1000, 0xd8c3c122 );
		ROM_LOAD( "zoar05",       0x1000, 0x1000, 0x05dc6b09 );
		ROM_LOAD( "zoar08",       0x2000, 0x1000, 0x9a148551 );
	
		ROM_REGION( 0x0040, REGION_PROMS );
		ROM_LOAD( "z20-1l",       0x0000, 0x0020, 0xa63f0a07 );
		ROM_LOAD( "z21-1l",       0x0020, 0x0020, 0x5e1e5788 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_disco = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );/* 64k for code + 64k for decrypted opcodes */
		ROM_LOAD( "disco.w5",     0xa000, 0x1000, 0xb2c87b78 );
		ROM_LOAD( "disco.w4",     0xb000, 0x1000, 0xad7040ee );
		ROM_LOAD( "disco.w3",     0xc000, 0x1000, 0x12fb4f08 );
		ROM_LOAD( "disco.w2",     0xd000, 0x1000, 0x73f6fb2f );
		ROM_LOAD( "disco.w1",     0xe000, 0x1000, 0xee7b536b );
		ROM_LOAD( "disco.w0",     0xf000, 0x1000, 0x7c26e76b );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the audio CPU */
		ROM_LOAD( "disco.w6",     0xf000, 0x1000, 0xd81e781e );
	
		/* no gfx1 */
	
		ROM_REGION( 0x0020, REGION_PROMS );
		ROM_LOAD( "disco.clr",    0x0000, 0x0020, 0xa393f913 );
	ROM_END(); }}; 
	
	
	public static ReadHandlerPtr wtennis_reset_hack_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		/* Otherwise the game goes into test mode and there is no way out that I
		   can see.  I'm not sure how it can work, it probably somehow has to do
		   with the tape system */
	
		RAM.write(0xfc30,0);
	
		return RAM.read(0xc15f);
	} };
	
	public static InitDriverPtr init_btime = new InitDriverPtr() { public void handler() 
	{
		UBytePtr rom = memory_region(REGION_CPU1);
		int diff = memory_region_length(REGION_CPU1) / 2;
	
		memory_set_opcode_base(0,new UBytePtr(rom,diff));
	
		/* For now, just copy the RAM array over to ROM. Decryption will happen */
		/* at run time, since the CPU applies the decryption only if the previous */
		/* instruction did a memory write. */
		//memcpy(rom+diff,rom,0x10000);
                for(int i=0; i<0x10000; i++)
                {
                    rom.write(diff+i, rom.read(i));
                }
	} };
	
	public static InitDriverPtr init_zoar = new InitDriverPtr() { public void handler() 
	{
		UBytePtr rom = memory_region(REGION_CPU1);
	
	
		/* At location 0xD50A is what looks like an undocumented opcode. I tried
		   implementing it given what opcode 0x23 should do, but it still didn't
		   work in demo mode. So this could be another protection or a bad ROM read.
		   I'm NOPing it out for now. */
		memset(rom,0xd50a,0xea,8);
	
                init_btime.handler();
	} };
	
	public static InitDriverPtr init_lnc = new InitDriverPtr() { public void handler() 
	{
		int A;
		UBytePtr rom = memory_region(REGION_CPU1);
		int diff = memory_region_length(REGION_CPU1) / 2;
	
		memory_set_opcode_base(0,new UBytePtr(rom,diff));
	
		/* Swap bits 5 & 6 for opcodes */
		for (A = 0;A < 0x10000;A++)
			rom.write(A+diff,swap_bits_5_6.handler(rom.read(A)));
	} };
	
	public static InitDriverPtr init_wtennis = new InitDriverPtr() { public void handler() 
	{
		install_mem_read_handler(0, 0xc15f, 0xc15f, wtennis_reset_hack_r);
		init_lnc.handler();
	} };
	
	
	
	public static GameDriver driver_btime	   = new GameDriver("1982"	,"btime"	,"btime.java"	,rom_btime,null	,machine_driver_btime	,input_ports_btime	,init_btime	,ROT270	,	"Data East Corporation", "Burger Time (Data East set 1)" );
	public static GameDriver driver_btime2	   = new GameDriver("1982"	,"btime2"	,"btime.java"	,rom_btime2,driver_btime	,machine_driver_btime	,input_ports_btime	,init_btime	,ROT270	,	"Data East Corporation", "Burger Time (Data East set 2)" );
	public static GameDriver driver_btimem	   = new GameDriver("1982"	,"btimem"	,"btime.java"	,rom_btimem,driver_btime	,machine_driver_btime	,input_ports_btime	,init_btime	,ROT270	,	"Data East (Bally Midway license)", "Burger Time (Midway)" );
	public static GameDriver driver_cookrace	   = new GameDriver("1982"	,"cookrace"	,"btime.java"	,rom_cookrace,driver_btime	,machine_driver_cookrace	,input_ports_cookrace	,init_lnc	,ROT270	,	"bootleg", "Cook Race" );
	public static GameDriver driver_lnc	   = new GameDriver("1981"	,"lnc"	,"btime.java"	,rom_lnc,null	,machine_driver_lnc	,input_ports_lnc	,init_lnc	,ROT270	,	"Data East Corporation", "Lock'n'Chase" );
	public static GameDriver driver_wtennis	   = new GameDriver("1982", "wtennis","btime.java"	,rom_wtennis,  null,       machine_driver_wtennis,  input_ports_wtennis,  init_wtennis, ROT270, "bootleg", "World Tennis", GAME_IMPERFECT_COLORS );
	public static GameDriver driver_mmonkey	   = new GameDriver("1982"	,"mmonkey"	,"btime.java"	,rom_mmonkey,null	,machine_driver_mmonkey	,input_ports_mmonkey	,init_lnc	,ROT270	,	"Technos + Roller Tron", "Minky Monkey" );
	public static GameDriver driver_brubber	   = new GameDriver("1982"	,"brubber"	,"btime.java"	,rom_brubber,null	,machine_driver_bnj	,input_ports_bnj	,init_lnc	,ROT270	,	"Data East", "Burnin' Rubber" );
	public static GameDriver driver_bnj	   = new GameDriver("1982"	,"bnj"	,"btime.java"	,rom_bnj,driver_brubber	,machine_driver_bnj	,input_ports_bnj	,init_lnc	,ROT270	,	"Data East USA (Bally Midway license)", "Bump 'n' Jump" );
	public static GameDriver driver_caractn	   = new GameDriver("1983"	,"caractn"	,"btime.java"	,rom_caractn,driver_brubber	,machine_driver_bnj	,input_ports_bnj	,init_lnc	,ROT270	,	"bootleg", "Car Action" );
	public static GameDriver driver_zoar	   = new GameDriver("1982"	,"zoar"	,"btime.java"	,rom_zoar,null	,machine_driver_zoar	,input_ports_zoar	,init_zoar	,ROT270	,	"Data East USA", "Zoar" );
	public static GameDriver driver_disco	   = new GameDriver("1982"	,"disco"	,"btime.java"	,rom_disco,null	,machine_driver_disco	,input_ports_disco	,init_btime	,ROT270	,	"Data East", "Disco No.1" );
	
	
	
	public static ReadHandlerPtr pip = new ReadHandlerPtr() { public int handler(int offset)
	{
		return rand();
	} };
	
	static MemoryReadAddress decocass_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x01ff, MRA_RAM ),
		new MemoryReadAddress( 0xe300, 0xe300, input_port_3_r ),     /* DSW1 */
		new MemoryReadAddress( 0xe500, 0xe502, pip ),	/* read data from tape */
	/*#if 0
		new MemoryReadAddress( 0x0000, 0x03ff, MRA_RAM ),
		new MemoryReadAddress( 0x0500, 0x3fff, MRA_ROM ),
		new MemoryReadAddress( 0xc000, 0xc7ff, MRA_RAM ),
		new MemoryReadAddress( 0xc800, 0xcbff, btime_mirrorvideoram_r ),
		new MemoryReadAddress( 0xcc00, 0xcfff, btime_mirrorcolorram_r ),
		new MemoryReadAddress( 0xd000, 0xd0ff, MRA_RAM ),	/* background */
	/*	new MemoryReadAddress( 0xd100, 0xd3ff, MRA_RAM ),	/* ? */
	/*	new MemoryReadAddress( 0xd400, 0xd7ff, MRA_RAM ),	/* background? */
	/*	new MemoryReadAddress( 0xe000, 0xe000, input_port_3_r ),     /* DSW1 */
	/*	new MemoryReadAddress( 0xe002, 0xe002, input_port_0_r ),     /* IN0 */
	/*	new MemoryReadAddress( 0xe003, 0xe003, input_port_1_r ),     /* IN1 */
	/*	new MemoryReadAddress( 0xe004, 0xe004, input_port_2_r ),     /* coin */
	/*#endif*/
		new MemoryReadAddress( 0xf000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress decocass_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x01ff, MWA_RAM ),
		new MemoryWriteAddress( 0x6000, 0xbfff, deco_charram_w, deco_charram ),
		new MemoryWriteAddress( 0xc000, 0xc3ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0xc400, 0xc7ff, colorram_w, colorram ),
		new MemoryWriteAddress( 0xc800, 0xcbff, btime_mirrorvideoram_w ),
		new MemoryWriteAddress( 0xcc00, 0xcfff, btime_mirrorcolorram_w ),
		new MemoryWriteAddress( 0xe000, 0xe01f, btime_paletteram_w, paletteram ),	/* The "bios" doesn't write to e000 */
										/* but the "loading" background should be blue, not black */
	/*#if 0
		new MemoryWriteAddress( 0x0500, 0x3fff, MWA_ROM ),
		new MemoryWriteAddress( 0xd000, 0xd0ff, MWA_RAM, bnj_backgroundram, bnj_backgroundram_size ),
		new MemoryWriteAddress( 0xd000, 0xd0ff, MWA_RAM ),	/* background? */
	/*	new MemoryWriteAddress( 0xd100, 0xd3ff, MWA_RAM ),	/* ? */
	/*	new MemoryWriteAddress( 0xd400, 0xd7ff, MWA_RAM, bnj_backgroundram, bnj_backgroundram_size ),
		new MemoryWriteAddress( 0xe000, 0xe000, bnj_video_control_w ),
		new MemoryWriteAddress( 0xe001, 0xe001, sound_command_w ),
		new MemoryWriteAddress( 0x4004, 0x4004, bnj_scroll1_w ),
	#endif*/
		new MemoryWriteAddress( 0xf000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress decocass_sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x01ff, MRA_RAM ),
	/*#if 0
		new MemoryReadAddress( 0xa000, 0xafff, soundlatch_r ),
	#endif*/
		new MemoryReadAddress( 0xf800, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress decocass_sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x01ff, MWA_RAM ),
		new MemoryWriteAddress( 0x2000, 0x2fff, AY8910_write_port_0_w ),
		new MemoryWriteAddress( 0x4000, 0x4fff, AY8910_control_port_0_w ),
		new MemoryWriteAddress( 0x6000, 0x6fff, AY8910_write_port_1_w ),
		new MemoryWriteAddress( 0x8000, 0x8fff, AY8910_control_port_1_w ),
	/*#if 0
		new MemoryWriteAddress( 0xc000, 0xcfff, interrupt_enable_w ),
	#endif*/
		new MemoryWriteAddress( 0xf800, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static InputPortPtr input_ports_decocass = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH,IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH,IPT_JOYSTICK_LEFT | IPF_4WAY );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH,IPT_JOYSTICK_UP | IPF_4WAY );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH,IPT_JOYSTICK_DOWN | IPF_4WAY );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH,IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH,IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH,IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH,IPT_UNUSED );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH,IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH,IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH,IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH,IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH,IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH,IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH,IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH,IPT_UNUSED );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH,IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH,IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH,IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH,IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH,IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH,IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START();       /* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );    /* used by the "bios" */
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );    /* used by the "bios" */
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Cocktail") );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK  );
	
		PORT_START();       /* DSW2 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x01, "3" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x06, "20000" );
		PORT_DIPSETTING(    0x04, "30000" );
		PORT_DIPSETTING(    0x02, "40000"  );
		PORT_DIPSETTING(    0x00, "50000"  );
		PORT_DIPNAME( 0x08, 0x08, "Enemies" );
		PORT_DIPSETTING(    0x08, "4" );
		PORT_DIPSETTING(    0x00, "6" );
		PORT_DIPNAME( 0x10, 0x10, "End of Level Pepper" );
		PORT_DIPSETTING(    0x10, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static GfxDecodeInfo decocass_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( 0, 0x6000, charlayout,          0, 4 ), /* char set #1 */
		new GfxDecodeInfo( 0, 0x6000, spritelayout,        0, 4 ), /* sprites */
	//	new GfxDecodeInfo( 0, 0x6000, cookrace_tilelayout, 8, 1 ), /* background tiles */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static MachineDriver machine_driver_decocass = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				1500000,	/* ? I guess it should be 750000 like in bnj */
				decocass_readmem,decocass_writemem,null,null,
				nmi_interrupt,1
			),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				500000, /* 500 kHz */
				decocass_sound_readmem,decocass_sound_writemem,null,null,
                                ignore_interrupt,0//			nmi_interrupt,16   /* IRQs are triggered by the main CPU */
			)
		},
		57, 3072,        /* frames per second, vblank duration */
		1,      /* 1 CPU slice per frame - interleaving is forced when a sound command is written */ 
		null,//GAMENAME##_init_machine,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 1*8, 31*8-1, 1*8, 31*8-1 ),
		decocass_gfxdecodeinfo,
		32,32,//COLOR,COLOR,
		null,//GAMENAME##_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER|VIDEO_MODIFIES_PALETTE,
		null,
		btime_vh_start,
		generic_vh_stop,
		decocass_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				ay8910_interface
			)
		}
	);
	
	static RomLoadPtr rom_decocass = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );/* 64k for code + 64k for decrypted opcodes */
		ROM_LOAD( "rms8.cpu",     0xf000, 0x1000, 0x23d929b7 );
	/* the following two are just about the same stuff as the one above */
	//	ROM_LOAD( "dsp3.p0b",     0xf000, 0x0800, 0xb67a91d9 );
	//	ROM_LOAD( "dsp3.p1b",     0xf800, 0x0800, 0x3bfff5f3 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the audio CPU */
		ROM_LOAD( "rms8.snd",     0xf800, 0x0800, 0xb66b2c2a );
	ROM_END(); }}; 
	public static GameDriver driver_decocass	   = new GameDriver("????"	,"decocass"	,"btime.java"	,rom_decocass,null	,machine_driver_decocass	,input_ports_decocass	,init_lnc	, ROT270, "?????", "decocass" );
}
