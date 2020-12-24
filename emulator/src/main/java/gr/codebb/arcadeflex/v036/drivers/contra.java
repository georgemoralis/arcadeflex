
/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 */ 
package gr.codebb.arcadeflex.v036.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_r;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_w;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.palette.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.contra.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class contra
{

		
	
        public static WriteHandlerPtr contra_bankswitch_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
            int bankaddress;
            UBytePtr RAM = memory_region(REGION_CPU1);


            bankaddress = 0x10000 + (data & 0x0f) * 0x2000;
            if (bankaddress < 0x28000)	/* for safety */
                    cpu_setbank(1,new UBytePtr(RAM,bankaddress));
        }};
	
	public static WriteHandlerPtr contra_sh_irqtrigger_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		cpu_cause_interrupt(1,M6809_INT_IRQ);
        }};
	
	public static WriteHandlerPtr contra_coin_counter_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if ((data & 0x01)!=0) coin_counter_w.handler(0,data & 0x01);
		if ((data & 0x02)!=0) coin_counter_w.handler(1,(data & 0x02) >> 1);
        }};
	
	public static WriteHandlerPtr cpu_sound_command_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		soundlatch_w.handler(offset,data);
	} };
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0010, 0x0010, input_port_0_r ),		/* IN0 */
		new MemoryReadAddress( 0x0011, 0x0011, input_port_1_r ),		/* IN1 */
		new MemoryReadAddress( 0x0012, 0x0012, input_port_2_r ),		/* IN2 */
	
		new MemoryReadAddress( 0x0014, 0x0014, input_port_3_r ),		/* DIPSW1 */
		new MemoryReadAddress( 0x0015, 0x0015, input_port_4_r ),		/* DIPSW2 */
		new MemoryReadAddress( 0x0016, 0x0016, input_port_5_r ),		/* DIPSW3 */
	
		new MemoryReadAddress( 0x0c00, 0x0cff, MRA_RAM ),
		new MemoryReadAddress( 0x1000, 0x5fff, MRA_RAM ),
		new MemoryReadAddress( 0x6000, 0x7fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x0007, contra_K007121_ctrl_0_w ),
		new MemoryWriteAddress( 0x0018, 0x0018, contra_coin_counter_w ),
		new MemoryWriteAddress( 0x001a, 0x001a, contra_sh_irqtrigger_w ),
		new MemoryWriteAddress( 0x001c, 0x001c, cpu_sound_command_w ),
		new MemoryWriteAddress( 0x001e, 0x001e, MWA_NOP ),	/* ? */
		new MemoryWriteAddress( 0x0060, 0x0067, contra_K007121_ctrl_1_w ),
		new MemoryWriteAddress( 0x0c00, 0x0cff, paletteram_xBBBBBGGGGGRRRRR_w, paletteram ),
		new MemoryWriteAddress( 0x1000, 0x1fff, MWA_RAM ),
		new MemoryWriteAddress( 0x2000, 0x23ff, contra_fg_cram_w, contra_fg_cram ),
		new MemoryWriteAddress( 0x2400, 0x27ff, contra_fg_vram_w, contra_fg_vram ),
		new MemoryWriteAddress( 0x2800, 0x2bff, contra_text_cram_w, contra_text_cram ),
		new MemoryWriteAddress( 0x2c00, 0x2fff, contra_text_vram_w, contra_text_vram ),
		new MemoryWriteAddress( 0x3000, 0x37ff, MWA_RAM, spriteram ),/* 2nd bank is at 0x5000 */
		new MemoryWriteAddress( 0x3800, 0x3fff, MWA_RAM ), // second sprite buffer
		new MemoryWriteAddress( 0x4000, 0x43ff, contra_bg_cram_w, contra_bg_cram ),
		new MemoryWriteAddress( 0x4400, 0x47ff, contra_bg_vram_w, contra_bg_vram ),
		new MemoryWriteAddress( 0x4800, 0x5fff, MWA_RAM ),
		new MemoryWriteAddress( 0x6000, 0x6fff, MWA_ROM ),
	 	new MemoryWriteAddress( 0x7000, 0x7000, contra_bankswitch_w ),
		new MemoryWriteAddress( 0x7001, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress readmem_sound[] =
	{
		new MemoryReadAddress( 0x0000, 0x0000, soundlatch_r ),
/*TODO*///		new MemoryReadAddress( 0x2001, 0x2001, YM2151_status_port_0_r ),
		new MemoryReadAddress( 0x6000, 0x67ff, MRA_RAM ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress writemem_sound[] =
	{
/*TODO*///		new MemoryWriteAddress( 0x2000, 0x2000, YM2151_register_port_0_w ),
/*TODO*///		new MemoryWriteAddress( 0x2001, 0x2001, YM2151_data_port_0_w ),
		new MemoryWriteAddress( 0x4000, 0x4000, MWA_NOP ), /* read triggers irq reset and latch read (in the hardware only). */
		new MemoryWriteAddress( 0x6000, 0x67ff, MWA_RAM ),
		new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )
	};
	
	
	
	static InputPortPtr input_ports_contra = new InputPortPtr(){ public void handler() { 
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
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
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
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );//marvins.c
		/* 0x00 is invalid */
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x03, 0x02, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "2" );
		PORT_DIPSETTING(    0x02, "3" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPSETTING(    0x00, "7" );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x18, "30000 70000" );
		PORT_DIPSETTING(    0x10, "40000 80000" );
		PORT_DIPSETTING(    0x08, "40000" );
		PORT_DIPSETTING(    0x00, "50000" );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x60, "Easy" );
		PORT_DIPSETTING(    0x40, "Normal" );
		PORT_DIPSETTING(    0x20, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW2 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Upright Controls" );
		PORT_DIPSETTING(    0x02, "Single" );
		PORT_DIPSETTING(    0x00, "Dual" );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, "Sound" );
		PORT_DIPSETTING(    0x00, "Mono" );
		PORT_DIPSETTING(    0x08, "Stereo" );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout gfx_layout = new GfxLayout
	(
		8,8,
		0x4000,
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 0, 4, 8, 12, 16, 20, 24, 28 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, gfx_layout,       0, 8*16 ),
		new GfxDecodeInfo( REGION_GFX2, 0, gfx_layout, 8*16*16, 8*16 ),
		new GfxDecodeInfo( -1 )
	};
	
	
	
/*	static struct YM2151interface ym2151_interface =
	{
.*		1,			/* 1 chip */
/*		3582071,	/* seems to be the standard */
/*		{ YM3012_VOL(60,MIXER_PAN_RIGHT,60,MIXER_PAN_LEFT) },
		{ 0 }
	};*/
	
	
	
	static MachineDriver machine_driver_contra = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
	 			CPU_M6809,
				1500000,
				readmem,writemem,null,null,
				interrupt,1
			),
			new MachineCPU(
	 			CPU_M6809 | CPU_AUDIO_CPU,
				2000000,
				readmem_sound,writemem_sound,null,null,
				ignore_interrupt,0	/* IRQs are caused by the main CPU */
			),
		},
		60,DEFAULT_REAL_60HZ_VBLANK_DURATION,
		10,	/* 10 CPU slices per frame - enough for the sound CPU to read all commands */
		null,
	
		/* video hardware */
	
		37*8, 32*8, new rectangle( 0*8, 35*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		128, 2*8*16*16,
		contra_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		contra_vh_start,
		contra_vh_stop,
		contra_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,//SOUND_SUPPORTS_STEREO,0,0,0,
                null
		/*{
			{
				SOUND_YM2151,
				&ym2151_interface
			}
		}*/
                
	);
	
	
	

	
	static RomLoadPtr rom_contra = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x28000, REGION_CPU1 );/* 64k for code + 96k for banked ROMs */
		ROM_LOAD( "633e03.18a",   0x20000, 0x08000, 0x7fc0d8cf );
		ROM_CONTINUE(			  0x08000, 0x08000 );
		ROM_LOAD( "633e02.17a",   0x10000, 0x10000, 0xb2f7bd9a );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for SOUND code */
		ROM_LOAD( "633e01.12a",   0x08000, 0x08000, 0xd1549255 );
	
		ROM_REGION( 0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD_GFX_EVEN( "633e04.7d",    0x00000, 0x40000, 0x14ddc542 );
		ROM_LOAD_GFX_ODD ( "633e05.7f",    0x00000, 0x40000, 0x42185044 );
	
		ROM_REGION( 0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD_GFX_EVEN( "633e06.16d",   0x00000, 0x40000, 0x9cf6faae );
		ROM_LOAD_GFX_ODD ( "633e07.16f",   0x00000, 0x40000, 0xf2d06638 );
	
		ROM_REGION( 0x0400, REGION_PROMS );
		ROM_LOAD( "633e08.10g",   0x0000, 0x0100, 0x9f0949fa );/* 007121 #0 sprite lookup table */
		ROM_LOAD( "633e09.12g",   0x0100, 0x0100, 0x14ca5e19 );/* 007121 #0 char lookup table */
		ROM_LOAD( "633f10.18g",   0x0200, 0x0100, 0x2b244d84 );/* 007121 #1 sprite lookup table */
		ROM_LOAD( "633f11.20g",   0x0300, 0x0100, 0x14ca5e19 );/* 007121 #1 char lookup table */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_contrab = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x28000, REGION_CPU1 );/* 64k for code + 96k for banked ROMs */
		ROM_LOAD( "contra.20",    0x20000, 0x08000, 0xd045e1da );
		ROM_CONTINUE(             0x08000, 0x08000 );
		ROM_LOAD( "633e02.17a",   0x10000, 0x10000, 0xb2f7bd9a );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for SOUND code */
		ROM_LOAD( "633e01.12a",   0x08000, 0x08000, 0xd1549255 );
	
		ROM_REGION( 0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD_GFX_EVEN( "633e04.7d",    0x00000, 0x40000, 0x14ddc542 );
		ROM_LOAD_GFX_ODD ( "633e05.7f",    0x00000, 0x40000, 0x42185044 );
	
		ROM_REGION( 0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD_GFX_EVEN( "633e06.16d",   0x00000, 0x40000, 0x9cf6faae );
		ROM_LOAD_GFX_ODD ( "633e07.16f",   0x00000, 0x40000, 0xf2d06638 );
	
		ROM_REGION( 0x0400, REGION_PROMS );
		ROM_LOAD( "633e08.10g",   0x0000, 0x0100, 0x9f0949fa );/* 007121 #0 sprite lookup table */
		ROM_LOAD( "633e09.12g",   0x0100, 0x0100, 0x14ca5e19 );/* 007121 #0 char lookup table */
		ROM_LOAD( "633f10.18g",   0x0200, 0x0100, 0x2b244d84 );/* 007121 #1 sprite lookup table */
		ROM_LOAD( "633f11.20g",   0x0300, 0x0100, 0x14ca5e19 );/* 007121 #1 char lookup table */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_contraj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x28000, REGION_CPU1 );/* 64k for code + 96k for banked ROMs */
		ROM_LOAD( "633n03.18a",   0x20000, 0x08000, 0xfedab568 );
		ROM_CONTINUE(             0x08000, 0x08000 );
		ROM_LOAD( "633k02.17a",   0x10000, 0x10000, 0x5d5f7438 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for SOUND code */
		ROM_LOAD( "633e01.12a",   0x08000, 0x08000, 0xd1549255 );
	
		ROM_REGION( 0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD_GFX_EVEN( "633e04.7d",    0x00000, 0x40000, 0x14ddc542 );
		ROM_LOAD_GFX_ODD ( "633e05.7f",    0x00000, 0x40000, 0x42185044 );
	
		ROM_REGION( 0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD_GFX_EVEN( "633e06.16d",   0x00000, 0x40000, 0x9cf6faae );
		ROM_LOAD_GFX_ODD ( "633e07.16f",   0x00000, 0x40000, 0xf2d06638 );
	
		ROM_REGION( 0x0400, REGION_PROMS );
		ROM_LOAD( "633e08.10g",   0x0000, 0x0100, 0x9f0949fa );/* 007121 #0 sprite lookup table */
		ROM_LOAD( "633e09.12g",   0x0100, 0x0100, 0x14ca5e19 );/* 007121 #0 char lookup table */
		ROM_LOAD( "633f10.18g",   0x0200, 0x0100, 0x2b244d84 );/* 007121 #1 sprite lookup table */
		ROM_LOAD( "633f11.20g",   0x0300, 0x0100, 0x14ca5e19 );/* 007121 #1 char lookup table */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_contrajb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x28000, REGION_CPU1 );/* 64k for code + 96k for banked ROMs */
		ROM_LOAD( "g-2.rom",      0x20000, 0x08000, 0xbdb9196d );
		ROM_CONTINUE(             0x08000, 0x08000 );
		ROM_LOAD( "633k02.17a",   0x10000, 0x10000, 0x5d5f7438 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for SOUND code */
		ROM_LOAD( "633e01.12a",   0x08000, 0x08000, 0xd1549255 );
	
		ROM_REGION( 0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD_GFX_EVEN( "633e04.7d",    0x00000, 0x40000, 0x14ddc542 );
		ROM_LOAD_GFX_ODD ( "633e05.7f",    0x00000, 0x40000, 0x42185044 );
	
		ROM_REGION( 0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD_GFX_EVEN( "633e06.16d",   0x00000, 0x40000, 0x9cf6faae );
		ROM_LOAD_GFX_ODD ( "633e07.16f",   0x00000, 0x40000, 0xf2d06638 );
	
		ROM_REGION( 0x0400, REGION_PROMS );
		ROM_LOAD( "633e08.10g",   0x0000, 0x0100, 0x9f0949fa );/* 007121 #0 sprite lookup table */
		ROM_LOAD( "633e09.12g",   0x0100, 0x0100, 0x14ca5e19 );/* 007121 #0 char lookup table */
		ROM_LOAD( "633f10.18g",   0x0200, 0x0100, 0x2b244d84 );/* 007121 #1 sprite lookup table */
		ROM_LOAD( "633f11.20g",   0x0300, 0x0100, 0x14ca5e19 );/* 007121 #1 char lookup table */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_gryzor = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x28000, REGION_CPU1 );/* 64k for code + 96k for banked ROMs */
		ROM_LOAD( "g2",           0x20000, 0x08000, 0x92ca77bd );
		ROM_CONTINUE(             0x08000, 0x08000 );
		ROM_LOAD( "g3",           0x10000, 0x10000, 0xbbd9e95e );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for SOUND code */
		ROM_LOAD( "633e01.12a",   0x08000, 0x08000, 0xd1549255 );
	
		ROM_REGION( 0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD_GFX_EVEN( "633e04.7d",    0x00000, 0x40000, 0x14ddc542 );
		ROM_LOAD_GFX_ODD ( "633e05.7f",    0x00000, 0x40000, 0x42185044 );
	
		ROM_REGION( 0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD_GFX_EVEN( "633e06.16d",   0x00000, 0x40000, 0x9cf6faae );
		ROM_LOAD_GFX_ODD ( "633e07.16f",   0x00000, 0x40000, 0xf2d06638 );
	
		ROM_REGION( 0x0400, REGION_PROMS );
		ROM_LOAD( "633e08.10g",   0x0000, 0x0100, 0x9f0949fa );/* 007121 #0 sprite lookup table */
		ROM_LOAD( "633e09.12g",   0x0100, 0x0100, 0x14ca5e19 );/* 007121 #0 char lookup table */
		ROM_LOAD( "633f10.18g",   0x0200, 0x0100, 0x2b244d84 );/* 007121 #1 sprite lookup table */
		ROM_LOAD( "633f11.20g",   0x0300, 0x0100, 0x14ca5e19 );/* 007121 #1 char lookup table */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_contra	   = new GameDriver("1987"	,"contra"	,"contra.java"	,rom_contra,null	,machine_driver_contra	,input_ports_contra	,null	,ROT90	,	"Konami", "Contra (US)" );
	public static GameDriver driver_contrab	   = new GameDriver("1987"	,"contrab"	,"contra.java"	,rom_contrab,driver_contra	,machine_driver_contra	,input_ports_contra	,null	,ROT90	,	"bootleg", "Contra (US bootleg)" );
	public static GameDriver driver_contraj	   = new GameDriver("1987"	,"contraj"	,"contra.java"	,rom_contraj,driver_contra	,machine_driver_contra	,input_ports_contra	,null	,ROT90	,	"Konami", "Contra (Japan)" );
	public static GameDriver driver_contrajb	   = new GameDriver("1987"	,"contrajb"	,"contra.java"	,rom_contrajb,driver_contra	,machine_driver_contra	,input_ports_contra	,null	,ROT90	,	"bootleg", "Contra (Japan bootleg)" );
	public static GameDriver driver_gryzor	   = new GameDriver("1987"	,"gryzor"	,"contra.java"	,rom_gryzor,driver_contra	,machine_driver_contra	,input_ports_contra	,null	,ROT90	,	"Konami", "Gryzor" );
}
