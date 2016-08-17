/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package drivers;

import static arcadeflex.ptrlib.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.commonH.*;
import static mame.inputport.*;
import static mame.drawgfxH.*;
import static vidhrdw.generic.*;
import static mame.cpuintrf.*;
import static mame.cpuintrfH.*;
import static mame.inputportH.*;
import static mame.mame.*;
import static arcadeflex.libc_old.*;
import static arcadeflex.libc.*;
import static mame.sndintrf.*;
import static cpu.konami.konamiH.*;
import static cpu.konami.konami.*;
import static cpu.z80.z80H.*;
import static mame.common.*;
import static mame.commonH.*;
import static mame.inputH.KEYCODE_F2;
import static mame.palette.*;
import static mame.memory.*;
import mame.sndintrfH.MachineSound;
import static mame.sndintrfH.*;
import static sndhrdw.seibu.*;
import static sound._3812intf.*;
import static sound._3812intfH.*;
import static sound.okim6295.*;
import static sound.okim6295H.*;
import static sound.ym2413.*;
import static sound._2413intfH.*;
import static vidhrdw.system16.*;
import static sound._2151intf.*;
import static sound._2151intfH.*;
import static sound.mixerH.MIXER_PAN_LEFT;
import static sound.mixerH.MIXER_PAN_RIGHT;
import static sound.upd7759.*;
import static sound.upd7759H.*;

public class system16 {

    public static final int NumOfShadowColors = 32;
    public static final int ShadowColorsMultiplier = 2;

    public static abstract interface sys16_update_procPtr {

        public abstract void handler();
    }
    /* video driver constants (vary with game) */
    public static sys16_update_procPtr sys16_update_proc;

    /* video driver has access to these memory regions */
    public static UBytePtr sys16_tileram = new UBytePtr();
    public static UBytePtr sys16_textram = new UBytePtr();
    public static UBytePtr sys16_spriteram = new UBytePtr();

    /* other memory regions */
    static UBytePtr sys16_workingram = new UBytePtr();
    static UBytePtr sys16_extraram = new UBytePtr();
    static UBytePtr sys16_extraram2 = new UBytePtr();
    static UBytePtr sys16_extraram3 = new UBytePtr();
    static UBytePtr sys16_extraram4 = new UBytePtr();
    static UBytePtr sys16_extraram5 = new UBytePtr();

    /*TODO*///	#define MWA_PALETTERAM	sys16_paletteram_w, &paletteram
/*TODO*///	#define MRA_PALETTERAM	paletteram_word_r
/*TODO*///	
/*TODO*///	#define MRA_WORKINGRAM	MRA_BANK1
/*TODO*///	#define MWA_WORKINGRAM	MWA_BANK1,&sys16_workingram
/*TODO*///	
/*TODO*///	#define MRA_SPRITERAM	MRA_BANK2
/*TODO*///	#define MWA_SPRITERAM	MWA_BANK2,&sys16_spriteram
/*TODO*///	
/*TODO*///	#define MRA_TILERAM		sys16_tileram_r
/*TODO*///	#define MWA_TILERAM		sys16_tileram_w,&sys16_tileram
/*TODO*///	
/*TODO*///	#define MRA_TEXTRAM		sys16_textram_r
/*TODO*///	#define MWA_TEXTRAM		sys16_textram_w,&sys16_textram
/*TODO*///	
/*TODO*///	#define MRA_EXTRAM		MRA_BANK3
/*TODO*///	#define MWA_EXTRAM		MWA_BANK3,&sys16_extraram
/*TODO*///	
/*TODO*///	#define MRA_EXTRAM2		MRA_BANK4
/*TODO*///	#define MWA_EXTRAM2		MWA_BANK4,&sys16_extraram2
/*TODO*///	
/*TODO*///	#define MRA_EXTRAM3		MRA_BANK5
/*TODO*///	#define MWA_EXTRAM3		MWA_BANK5,&sys16_extraram3
/*TODO*///	
/*TODO*///	#define MRA_EXTRAM4		MRA_BANK6
/*TODO*///	#define MWA_EXTRAM4		MWA_BANK6,&sys16_extraram4
/*TODO*///	
/*TODO*///	#define MRA_EXTRAM5		MRA_BANK7
/*TODO*///	#define MWA_EXTRAM5		MWA_BANK7,&sys16_extraram5
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	#define MACHINE_DRIVER( GAMENAME,READMEM,WRITEMEM,INITMACHINE,GFXSIZE) \
/*TODO*///	static MachineDriver GAMENAME = new MachineDriver\
/*TODO*///	( \
/*TODO*///		new MachineCPU[] { \
/*TODO*///			new MachineCPU( \
/*TODO*///				CPU_M68000, \
/*TODO*///				10000000, \
/*TODO*///				READMEM,WRITEMEM,null,null, \
/*TODO*///				sys16_interrupt,1 \
/*TODO*///			), \
/*TODO*///			new MachineCPU( \
/*TODO*///				CPU_Z80 | CPU_AUDIO_CPU, \
/*TODO*///				4096000, \
/*TODO*///				sound_readmem,sound_writemem,sound_readport,sound_writeport, \
/*TODO*///				ignore_interrupt,1 \
/*TODO*///			), \
/*TODO*///		}, \
/*TODO*///		60, DEFAULT_60HZ_VBLANK_DURATION, \
/*TODO*///		1, \
/*TODO*///		INITMACHINE, \
/*TODO*///		40*8, 28*8, new rectangle( 0*8, 40*8-1, 0*8, 28*8-1 ), \
/*TODO*///		GFXSIZE, \
/*TODO*///		2048*ShadowColorsMultiplier,2048*ShadowColorsMultiplier, \
/*TODO*///		null, \
/*TODO*///		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE, \
/*TODO*///		null, \
/*TODO*///		sys16_vh_start, \
/*TODO*///		sys16_vh_stop, \
/*TODO*///		sys16_vh_screenrefresh, \
/*TODO*///		SOUND_SUPPORTS_STEREO,0,0,0, \
/*TODO*///		new MachineSound[] { \
/*TODO*///			new MachineSound( \
/*TODO*///				SOUND_YM2151, \
/*TODO*///				ym2151_interface \
/*TODO*///			) \
/*TODO*///		} \
/*TODO*///	);
/*TODO*///	
/*TODO*///	#define MACHINE_DRIVER_7759( GAMENAME,READMEM,WRITEMEM,INITMACHINE,GFXSIZE, UPD7759INTF ) \
/*TODO*///	static MachineDriver GAMENAME = new MachineDriver\
/*TODO*///	( \
/*TODO*///		new MachineCPU[] { \
/*TODO*///			new MachineCPU( \
/*TODO*///				CPU_M68000, \
/*TODO*///				10000000, \
/*TODO*///				READMEM,WRITEMEM,null,null, \
/*TODO*///				sys16_interrupt,1 \
/*TODO*///			), \
/*TODO*///			new MachineCPU( \
/*TODO*///				CPU_Z80 | CPU_AUDIO_CPU, \
/*TODO*///				4096000, \
/*TODO*///				sound_readmem_7759,sound_writemem,sound_readport,sound_writeport_7759, \
/*TODO*///				ignore_interrupt,1 \
/*TODO*///			), \
/*TODO*///		}, \
/*TODO*///		60, DEFAULT_60HZ_VBLANK_DURATION, \
/*TODO*///		1, \
/*TODO*///		INITMACHINE, \
/*TODO*///		40*8, 28*8, new rectangle( 0*8, 40*8-1, 0*8, 28*8-1 ), \
/*TODO*///		GFXSIZE, \
/*TODO*///		2048*ShadowColorsMultiplier,2048*ShadowColorsMultiplier, \
/*TODO*///		null, \
/*TODO*///		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE, \
/*TODO*///		null, \
/*TODO*///		sys16_vh_start, \
/*TODO*///		sys16_vh_stop, \
/*TODO*///		sys16_vh_screenrefresh, \
/*TODO*///		SOUND_SUPPORTS_STEREO,0,0,0, \
/*TODO*///		new MachineSound[] { \
/*TODO*///			new MachineSound( \
/*TODO*///				SOUND_YM2151, \
/*TODO*///				ym2151_interface \
/*TODO*///			), new MachineSound( \
/*TODO*///				SOUND_UPD7759, \
/*TODO*///				UPD7759INTF \
/*TODO*///			) \
/*TODO*///		} \
/*TODO*///	);
/*TODO*///	
/*TODO*///	
/*TODO*///	#define MACHINE_DRIVER_7751( GAMENAME,READMEM,WRITEMEM,INITMACHINE,GFXSIZE ) \
/*TODO*///	static MachineDriver GAMENAME = new MachineDriver\
/*TODO*///	( \
/*TODO*///		new MachineCPU[] { \
/*TODO*///			new MachineCPU( \
/*TODO*///				CPU_M68000, \
/*TODO*///				10000000, \
/*TODO*///				READMEM,WRITEMEM,null,null, \
/*TODO*///				sys16_interrupt,1 \
/*TODO*///			), \
/*TODO*///			new MachineCPU( \
/*TODO*///				CPU_Z80 | CPU_AUDIO_CPU, \
/*TODO*///				4096000, \
/*TODO*///				sound_readmem_7751,sound_writemem,sound_readport_7751,sound_writeport_7751, \
/*TODO*///				ignore_interrupt,1 \
/*TODO*///			), \
/*TODO*///			new MachineCPU( \
/*TODO*///				CPU_N7751 | CPU_AUDIO_CPU, \
/*TODO*///				6000000/15,        /* 6Mhz crystal */ \
/*TODO*///				readmem_7751,writemem_7751,readport_7751,writeport_7751, \
/*TODO*///				ignore_interrupt,1 \
/*TODO*///			) \
/*TODO*///		}, \
/*TODO*///		60, DEFAULT_60HZ_VBLANK_DURATION, \
/*TODO*///		1, \
/*TODO*///		INITMACHINE, \
/*TODO*///		40*8, 28*8, new rectangle( 0*8, 40*8-1, 0*8, 28*8-1 ), \
/*TODO*///		GFXSIZE, \
/*TODO*///		2048*ShadowColorsMultiplier,2048*ShadowColorsMultiplier, \
/*TODO*///		null, \
/*TODO*///		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE, \
/*TODO*///		null, \
/*TODO*///		sys16_vh_start, \
/*TODO*///		sys16_vh_stop, \
/*TODO*///		sys16_vh_screenrefresh, \
/*TODO*///		SOUND_SUPPORTS_STEREO,0,0,0, \
/*TODO*///		new MachineSound[] { \
/*TODO*///			new MachineSound( \
/*TODO*///				SOUND_YM2151, \
/*TODO*///				ym2151_interface \
/*TODO*///			), \
/*TODO*///			new MachineSound( \
/*TODO*///				SOUND_DAC, \
/*TODO*///				sys16_7751_dac_interface \
/*TODO*///			) \
/*TODO*///		} \
/*TODO*///	);
/*TODO*///	
/*TODO*///	
/*TODO*///	#define MACHINE_DRIVER_18( GAMENAME,READMEM,WRITEMEM,INITMACHINE,GFXSIZE) \
/*TODO*///	static MachineDriver GAMENAME = new MachineDriver\
/*TODO*///	( \
/*TODO*///		new MachineCPU[] { \
/*TODO*///			new MachineCPU( \
/*TODO*///				CPU_M68000, \
/*TODO*///				10000000, \
/*TODO*///				READMEM,WRITEMEM,null,null, \
/*TODO*///				sys16_interrupt,1 \
/*TODO*///			), \
/*TODO*///			new MachineCPU( \
/*TODO*///				CPU_Z80 | CPU_AUDIO_CPU, \
/*TODO*///				4096000*2, /* overclocked to fix sound, but wrong! */ \
/*TODO*///				sound_readmem_18,sound_writemem_18,sound_readport_18,sound_writeport_18, \
/*TODO*///				ignore_interrupt,1 \
/*TODO*///			), \
/*TODO*///		}, \
/*TODO*///		60, DEFAULT_60HZ_VBLANK_DURATION, \
/*TODO*///		1, \
/*TODO*///		INITMACHINE, \
/*TODO*///		40*8, 28*8, new rectangle( 0*8, 40*8-1, 0*8, 28*8-1 ), \
/*TODO*///		GFXSIZE, \
/*TODO*///		2048*ShadowColorsMultiplier,2048*ShadowColorsMultiplier, \
/*TODO*///		null, \
/*TODO*///		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE, \
/*TODO*///		null, \
/*TODO*///		sys18_vh_start, \
/*TODO*///		sys16_vh_stop, \
/*TODO*///		sys18_vh_screenrefresh, \
/*TODO*///		SOUND_SUPPORTS_STEREO,0,0,0, \
/*TODO*///		new MachineSound[] { \
/*TODO*///			new MachineSound( \
/*TODO*///				SOUND_YM3438, \
/*TODO*///				ym3438_interface \
/*TODO*///			), \
/*TODO*///			new MachineSound( \
/*TODO*///				SOUND_RF5C68, \
/*TODO*///				rf5c68_interface, \
/*TODO*///			) \
/*TODO*///		} \
/*TODO*///	);
/*TODO*///	
/*TODO*///	
/*TODO*///	
    public static abstract interface sys16_custom_irqPtr {

        public abstract void handler();
    }
    static sys16_custom_irqPtr sys16_custom_irq;

    public static InitMachinePtr sys16_onetime_init_machine = new InitMachinePtr() {
        public void handler() {
            sys16_bg1_trans = 0;
            sys16_rowscroll_scroll = 0;
            sys18_splittab_bg_x = null;
            sys18_splittab_bg_y = null;
            sys18_splittab_fg_x = null;
            sys18_splittab_fg_y = null;

            sys16_quartet_title_kludge = 0;

            sys16_custom_irq = null;

            sys16_MaxShadowColors = NumOfShadowColors;

        }
    };

    /*TODO*///	/***************************************************************************/
/*TODO*///	
    public static InterruptPtr sys16_interrupt = new InterruptPtr() {
        public int handler() {
            if (sys16_custom_irq != null) {
                /*TODO*///                    sys16_custom_irq();
                throw new UnsupportedOperationException("Unimplemented");
            }
            return 4;
            /* Interrupt vector 4, used by VBlank */
        }
    };

    /**
     * ************************************************************************
     */
    public static irqcallbackPtr sound_cause_nmi = new irqcallbackPtr() {

        public void handler(int chip) {
            cpu_set_nmi_line(1, PULSE_LINE);
        }
    };

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0xe800, 0xe800, soundlatch_r),
                new MemoryReadAddress(0xf800, 0xffff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0xf800, 0xffff, MWA_RAM),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort sound_readport[]
            = {
                new IOReadPort(0x01, 0x01, YM2151_status_port_0_r),
                new IOReadPort(0xc0, 0xc0, soundlatch_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort sound_writeport[]
            = {
                new IOWritePort(0x00, 0x00, YM2151_register_port_0_w),
                new IOWritePort(0x01, 0x01, YM2151_data_port_0_w),
                new IOWritePort(-1)
            };

    /*TODO*///	
/*TODO*///	
/*TODO*///	// 7751 Sound
/*TODO*///	
/*TODO*///	static MemoryReadAddress sound_readmem_7751[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0xe800, 0xe800, soundlatch_r ),
/*TODO*///		new MemoryReadAddress( 0xf800, 0xffff, MRA_RAM ),
/*TODO*///		new MemoryReadAddress( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	static IOReadPort sound_readport_7751[] =
/*TODO*///	{
/*TODO*///		new IOReadPort( 0x01, 0x01, YM2151_status_port_0_r ),
/*TODO*///	//    new IOReadPort( 0x0e, 0x0e, sys16_7751_audio_8255_r ),
/*TODO*///		new IOReadPort( 0xc0, 0xc0, soundlatch_r ),
/*TODO*///		new IOReadPort( -1 )	/* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	static IOWritePort sound_writeport_7751[] =
/*TODO*///	{
/*TODO*///		new IOWritePort( 0x00, 0x00, YM2151_register_port_0_w ),
/*TODO*///		new IOWritePort( 0x01, 0x01, YM2151_data_port_0_w ),
/*TODO*///		new IOWritePort( 0x80, 0x80, sys16_7751_audio_8255_w ),
/*TODO*///		new IOWritePort( -1 )
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryReadAddress readmem_7751[] =
/*TODO*///	{
/*TODO*///	        new MemoryReadAddress( 0x0000, 0x03ff, MRA_ROM ),
/*TODO*///	        new MemoryReadAddress( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress writemem_7751[] =
/*TODO*///	{
/*TODO*///	        new MemoryWriteAddress( 0x0000, 0x03ff, MWA_ROM ),
/*TODO*///	        new MemoryWriteAddress( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	static IOReadPort readport_7751[] =
/*TODO*///	{
/*TODO*///	        new IOReadPort( I8039_t1,  I8039_t1,  sys16_7751_sh_t1_r ),
/*TODO*///	        new IOReadPort( I8039_p2,  I8039_p2,  sys16_7751_sh_command_r ),
/*TODO*///	        new IOReadPort( I8039_bus, I8039_bus, sys16_7751_sh_rom_r ),
/*TODO*///	        new IOReadPort( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	static IOWritePort writeport_7751[] =
/*TODO*///	{
/*TODO*///	        new IOWritePort( I8039_p1, I8039_p1, sys16_7751_sh_dac_w ),
/*TODO*///	        new IOWritePort( I8039_p2, I8039_p2, sys16_7751_sh_busy_w ),
/*TODO*///	        new IOWritePort( I8039_p4, I8039_p4, sys16_7751_sh_offset_a0_a3_w ),
/*TODO*///	        new IOWritePort( I8039_p5, I8039_p5, sys16_7751_sh_offset_a4_a7_w ),
/*TODO*///	        new IOWritePort( I8039_p6, I8039_p6, sys16_7751_sh_offset_a8_a11_w ),
/*TODO*///	        new IOWritePort( I8039_p7, I8039_p7, sys16_7751_sh_rom_select_w ),
/*TODO*///	        new IOWritePort( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	static DACinterface sys16_7751_dac_interface = new DACinterface
/*TODO*///	(
/*TODO*///	        1,
/*TODO*///	        new int[] { 100 }
/*TODO*///	);
/*TODO*///	
/*TODO*///	
/*TODO*///	// 7759
/*TODO*///	
/*TODO*///	
    static MemoryReadAddress sound_readmem_7759[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0xdfff, UPD7759_0_data_r),
                new MemoryReadAddress(0xe800, 0xe800, soundlatch_r),
                new MemoryReadAddress(0xf800, 0xffff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    // some games (aurail, riotcity, eswat), seem to send different format data to the 7759
    // this function changes that data to what the 7759 expects, but it sounds quite poor.
    public static WriteHandlerPtr UPD7759_process_message_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 0xc0) == 0x40) {
                data = 0xc0;
            } else {
                data &= 0x3f;
            }

            UPD7759_message_w.handler(offset, data);
        }
    };

    static IOWritePort sound_writeport_7759[]
            = {
                new IOWritePort(0x00, 0x00, YM2151_register_port_0_w),
                new IOWritePort(0x01, 0x01, YM2151_data_port_0_w),
                new IOWritePort(0x40, 0x40, UPD7759_process_message_w),
                new IOWritePort(0x80, 0x80, UPD7759_start_w),
                new IOWritePort(-1)
            };

    static UPD7759_interface upd7759_interface = new UPD7759_interface(
            1, /* 1 chip */
            UPD7759_STANDARD_CLOCK,
            new int[]{60}, /* volumes */
            new int[]{REGION_CPU2}, /* memory region 3 contains the sample data */
            UPD7759_SLAVE_MODE,
            new irqcallbackPtr[]{sound_cause_nmi}
    );

    /*TODO*///	// SYS18 Sound
/*TODO*///	
/*TODO*///	unsigned char *sys18_SoundMemBank;
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr system18_bank_r = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		return sys18_SoundMemBank[offset];
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static MemoryReadAddress sound_readmem_18[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x0000, 0x9fff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0xa000, 0xbfff, system18_bank_r ),
/*TODO*///		/**** D/A register ****/
/*TODO*///		new MemoryReadAddress( 0xd000, 0xdfff, RF5C68ReadMem ),
/*TODO*///		new MemoryReadAddress( 0xe000, 0xffff, MRA_RAM ),
/*TODO*///		new MemoryReadAddress( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress sound_writemem_18[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),
/*TODO*///		/**** D/A register ****/
/*TODO*///		new MemoryWriteAddress( 0xc000, 0xc008, RF5C68WriteReg ),
/*TODO*///		new MemoryWriteAddress( 0xd000, 0xdfff, RF5C68WriteMem ),
/*TODO*///		new MemoryWriteAddress( 0xe000, 0xffff, MWA_RAM ),	//??
/*TODO*///		new MemoryWriteAddress( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	static struct RF5C68interface rf5c68_interface = {
/*TODO*///	  //3580000 * 2,
/*TODO*///	  3579545*2,
/*TODO*///	  100
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr sys18_soundbank_w = new WriteHandlerPtr() { public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///	// select access bank for a000~bfff
/*TODO*///		UBytePtr RAM = memory_region(REGION_CPU2);
/*TODO*///		int Bank=0;
/*TODO*///	
/*TODO*///		switch (data&0xc0)
/*TODO*///		{
/*TODO*///			case 0x00:
/*TODO*///				Bank = data<<13;
/*TODO*///				break;
/*TODO*///			case 0x40:
/*TODO*///				Bank = ((data&0x1f) + 128/8)<<13;
/*TODO*///				break;
/*TODO*///			case 0x80:
/*TODO*///				Bank = ((data&0x1f) + (256+128)/8)<<13;
/*TODO*///				break;
/*TODO*///			case 0xc0:
/*TODO*///				Bank = ((data&0x1f) + (512+128)/8)<<13;
/*TODO*///				break;
/*TODO*///		}
/*TODO*///		sys18_SoundMemBank = &RAM[Bank+0x10000];
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static IOReadPort sound_readport_18[] =
/*TODO*///	{
/*TODO*///		new IOReadPort( 0x80, 0x80, YM2612_status_port_0_A_r ),
/*TODO*///	//	new IOReadPort( 0x82, 0x82, YM2612_status_port_0_B_r ),
/*TODO*///	//	new IOReadPort( 0x90, 0x90, YM2612_status_port_1_A_r ),
/*TODO*///	//	new IOReadPort( 0x92, 0x92, YM2612_status_port_1_B_r ),
/*TODO*///		new IOReadPort( 0xc0, 0xc0, soundlatch_r ),
/*TODO*///		new IOReadPort( -1 )	/* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	static IOWritePort sound_writeport_18[] =
/*TODO*///	{
/*TODO*///		new IOWritePort( 0x80, 0x80, YM2612_control_port_0_A_w ),
/*TODO*///		new IOWritePort( 0x81, 0x81, YM2612_data_port_0_A_w ),
/*TODO*///		new IOWritePort( 0x82, 0x82, YM2612_control_port_0_B_w ),
/*TODO*///		new IOWritePort( 0x83, 0x83, YM2612_data_port_0_B_w ),
/*TODO*///		new IOWritePort( 0x90, 0x90, YM2612_control_port_1_A_w ),
/*TODO*///		new IOWritePort( 0x91, 0x91, YM2612_data_port_1_A_w ),
/*TODO*///		new IOWritePort( 0x92, 0x92, YM2612_control_port_1_B_w ),
/*TODO*///		new IOWritePort( 0x93, 0x93, YM2612_data_port_1_B_w ),
/*TODO*///		new IOWritePort( 0xa0, 0xa0, sys18_soundbank_w ),
/*TODO*///		new IOWritePort( -1 )
/*TODO*///	};
/*TODO*///	
/*TODO*///	static struct YM2612interface ym3438_interface =
/*TODO*///	{
/*TODO*///		2,	/* 2 chips */
/*TODO*///		8000000,
/*TODO*///		{ 40,40 },
/*TODO*///		{ 0 },	{ 0 },	{ 0 },	{ 0 }
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	// Sega 3D Sound
/*TODO*///	
/*TODO*///	
/*TODO*///	static YM2203interface ym2203_interface = new YM2203interface
/*TODO*///	(
/*TODO*///		1,	/* 1 chips */
/*TODO*///		4096000,	/* 3.58 MHZ ? */
/*TODO*///		new int[] { YM2203_VOL(50,50) },
/*TODO*///		new ReadHandlerPtr[] { 0 },
/*TODO*///		new ReadHandlerPtr[] { 0 },
/*TODO*///		new WriteHandlerPtr[] { 0 },
/*TODO*///		new WriteHandlerPtr[] { 0 },
/*TODO*///		new WriteYmHandlerPtr[] { 0 }
/*TODO*///	);
/*TODO*///	
/*TODO*///	static YM2203interface ym2203_interface2 = new YM2203interface
/*TODO*///	(
/*TODO*///		3,	/* 1 chips */
/*TODO*///		4096000,	/* 3.58 MHZ ? */
/*TODO*///		new int[] { YM2203_VOL(50,50),YM2203_VOL(50,50),YM2203_VOL(50,50) },
/*TODO*///		new ReadHandlerPtr[] { 0 },
/*TODO*///		new ReadHandlerPtr[] { 0 },
/*TODO*///		new WriteHandlerPtr[] { 0 },
/*TODO*///		new WriteHandlerPtr[] { 0 },
/*TODO*///		new WriteYmHandlerPtr[] { 0 }
/*TODO*///	);
/*TODO*///	
/*TODO*///	static struct SEGAPCMinterface segapcm_interface_15k = {
/*TODO*///		SEGAPCM_SAMPLE15K,
/*TODO*///		BANK_256,
/*TODO*///		REGION_SOUND1,		// memory region
/*TODO*///		50
/*TODO*///	};
/*TODO*///	
/*TODO*///	static struct SEGAPCMinterface segapcm_interface_15k_512 = {
/*TODO*///		SEGAPCM_SAMPLE15K,
/*TODO*///		BANK_512,
/*TODO*///		REGION_SOUND1,		// memory region
/*TODO*///		50
/*TODO*///	};
/*TODO*///	
/*TODO*///	static struct SEGAPCMinterface segapcm_interface_32k = {
/*TODO*///		SEGAPCM_SAMPLE32K,
/*TODO*///		BANK_256,
/*TODO*///		REGION_SOUND1,
/*TODO*///		50
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	// Super hang-on, outrun
/*TODO*///	
/*TODO*///	// hopefully this is endian safe!
/*TODO*///	static unsigned char *sound_shared_ram;
/*TODO*///	public static ReadHandlerPtr sound_shared_ram_r = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		return (sound_shared_ram[offset] << 8) + sound_shared_ram[offset+1];
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr sound_shared_ram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		int val=(sound_shared_ram[offset] << 8) + sound_shared_ram[offset+1];
/*TODO*///		val=(val & (data>>16)) | (data &0xffff);
/*TODO*///	
/*TODO*///		sound_shared_ram[offset] = val>>8;
/*TODO*///		sound_shared_ram[offset+1] = val&0xff;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr sound2_shared_ram_r = new ReadHandlerPtr() { public int handler(int offset){ return sound_shared_ram[offset]; } };
/*TODO*///	public static WriteHandlerPtr sound2_shared_ram_w = new WriteHandlerPtr() { public void handler(int offset, int data){ sound_shared_ram[offset] = data; } };
    public static WriteHandlerPtr sound_command_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (errorlog != null) {
                fprintf(errorlog, "SOUND COMMAND %04x <- %02x\n", offset, data & 0xff);
            }
            soundlatch_w.handler(0, data & 0xff);
            cpu_cause_interrupt(1, 0);
        }
    };
    /*TODO*///	
/*TODO*///	public static WriteHandlerPtr sound_command_nmi_w = new WriteHandlerPtr() { public void handler(int offset, int data){
/*TODO*///		if( errorlog ) fprintf( errorlog, "SOUND COMMAND %04x <- %02x\n", offset, data&0xff );
/*TODO*///		soundlatch_w.handler( 0,data&0xff );
/*TODO*///		cpu_set_nmi_line(1, PULSE_LINE);
/*TODO*///	} };
/*TODO*///	
    static YM2151interface ym2151_interface = new YM2151interface(
            1, /* 1 chip */
            4096000, /* 3.58 MHZ ? */
            new int[]{YM3012_VOL(40, MIXER_PAN_LEFT, 40, MIXER_PAN_RIGHT)},
            new WriteYmHandlerPtr[]{null}
    );

    /**
     * ************************************************************************
     */
    static GfxLayout charlayout1 = new GfxLayout(
            8, 8, /* 8*8 chars */
            8192, /* 8192 chars */
            3, /* 3 bits per pixel */
            new int[]{0x20000 * 8, 0x10000 * 8, 0},
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every sprite takes 8 consecutive bytes */
    );

    static GfxLayout charlayout2 = new GfxLayout(
            8, 8, /* 8*8 chars */
            16384, /* 16384 chars */
            3, /* 3 bits per pixel */
            new int[]{0x40000 * 8, 0x20000 * 8, 0},
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every sprite takes 8 consecutive bytes */
    );

    static GfxLayout charlayout4 = new GfxLayout(
            8, 8, /* 8*8 chars */
            32768, /* 32768 chars */
            3, /* 3 bits per pixel */
            new int[]{0x80000 * 8, 0x40000 * 8, 0},
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every sprite takes 8 consecutive bytes */
    );
    static GfxLayout charlayout8 = new GfxLayout(
            8, 8, /* 8*8 chars */
            4096, /* 4096 chars */
            3, /* 3 bits per pixel */
            new int[]{0x10000 * 8, 0x08000 * 8, 0},
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every sprite takes 8 consecutive bytes */
    );

    static GfxDecodeInfo gfx1[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x00000, charlayout1, 0, 256),
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo gfx2[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x00000, charlayout2, 0, 256),
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo gfx4[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x00000, charlayout4, 0, 256),
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo gfx8[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x00000, charlayout8, 0, 256),
                new GfxDecodeInfo(-1) /* end of array */};

    /**
     * ************************************************************************
     */
    static void set_refresh(int data) {
        sys16_refreshenable = data & 0x20;
        sys16_clear_screen = data & 1;
    }

    /*TODO*///	static void set_refresh_18( int data ){
/*TODO*///		sys16_refreshenable = data&0x2;
/*TODO*///	//	sys16_clear_screen  = data&4;
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void set_refresh_3d( int data ){
/*TODO*///		sys16_refreshenable = data&0x10;
/*TODO*///	}
/*TODO*///	
    static void set_tile_bank(int data) {
        sys16_tile_bank1 = data & 0xf;
        sys16_tile_bank0 = (data >> 4) & 0xf;
    }

    static void set_tile_bank18(int data) {
        sys16_tile_bank0 = data & 0xf;
        sys16_tile_bank1 = (data >> 4) & 0xf;
    }

    static void set_fg_page(int data) {
        sys16_fg_page[0] = data >> 12;
        sys16_fg_page[1] = (data >> 8) & 0xf;
        sys16_fg_page[2] = (data >> 4) & 0xf;
        sys16_fg_page[3] = data & 0xf;
    }

    static void set_bg_page(int data) {
        sys16_bg_page[0] = data >> 12;
        sys16_bg_page[1] = (data >> 8) & 0xf;
        sys16_bg_page[2] = (data >> 4) & 0xf;
        sys16_bg_page[3] = data & 0xf;
    }

    /*TODO*///	static void set_fg_page1( int data ){
/*TODO*///		sys16_fg_page[1] = data>>12;
/*TODO*///		sys16_fg_page[0] = (data>>8)&0xf;
/*TODO*///		sys16_fg_page[3] = (data>>4)&0xf;
/*TODO*///		sys16_fg_page[2] = data&0xf;
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void set_bg_page1( int data ){
/*TODO*///		sys16_bg_page[1] = data>>12;
/*TODO*///		sys16_bg_page[0] = (data>>8)&0xf;
/*TODO*///		sys16_bg_page[3] = (data>>4)&0xf;
/*TODO*///		sys16_bg_page[2] = data&0xf;
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void set_fg2_page( int data ){
/*TODO*///		sys16_fg2_page[0] = data>>12;
/*TODO*///		sys16_fg2_page[1] = (data>>8)&0xf;
/*TODO*///		sys16_fg2_page[2] = (data>>4)&0xf;
/*TODO*///		sys16_fg2_page[3] = data&0xf;
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void set_bg2_page( int data ){
/*TODO*///		sys16_bg2_page[0] = data>>12;
/*TODO*///		sys16_bg2_page[1] = (data>>8)&0xf;
/*TODO*///		sys16_bg2_page[2] = (data>>4)&0xf;
/*TODO*///		sys16_bg2_page[3] = data&0xf;
/*TODO*///	}
/*TODO*///	
    /**
     * ************************************************************************
     */
    /*	Important: you must leave extra space when listing sprite ROMs
		in a ROM module definition.  This routine unpacks each sprite nibble
		into a byte, doubling the memory consumption. */
    static void sys16_sprite_decode(int num_banks, int bank_size) {
        UBytePtr base = memory_region(REGION_GFX2);
        UBytePtr temp = new UBytePtr(bank_size);
        int i;

        if (temp == null) {
            return;
        }

        for (i = num_banks; i > 0; i--) {
            UBytePtr finish = new UBytePtr(base, 2 * bank_size * i);
            UBytePtr dest = new UBytePtr(finish, - 2 * bank_size);

            UBytePtr p1 = new UBytePtr(temp);
            UBytePtr p2 = new UBytePtr(temp, bank_size / 2);

            /*unsigned*/ char data;

            memcpy(temp, new UBytePtr(base, bank_size * (i - 1)), bank_size);
            /*
	note: both pen#0 and pen#15 are transparent.
	we replace references to pen#15 with pen#0, to simplify the sprite rendering
             */
            do {
                data = (char) (p2.readinc() & 0xFF);
                if ((data & 0x0f) == 0x0f) {
                    if ((data & 0xf0) != 0xf0 && (data & 0xf0) != 0) {
                        dest.writeinc((data >> 4) & 0xFF);
                    } else {
                        dest.writeinc(0xff);
                    }
                    dest.writeinc(0xff);
                } else if ((data & 0xf0) == 0xf0) {
                    dest.writeinc(0x00);
                    if ((data & 0x0f) == 0x0f) {
                        data &= 0xf0;
                    }
                    dest.writeinc(data & 0xf);
                } else {
                    dest.writeinc((data >> 4) & 0xFF);
                    dest.writeinc(data & 0xF);
                }

                data = (char) (p1.readinc() & 0xFF);
                if ((data & 0x0f) == 0x0f) {
                    if ((data & 0xf0) != 0xf0 && (data & 0xf0) != 0) {
                        dest.writeinc((data >> 4) & 0xFF);
                    } else {
                        dest.writeinc(0xff);
                    }
                    dest.writeinc(0xff);
                } else if ((data & 0xf0) == 0xf0) {
                    dest.writeinc(0x00);
                    if ((data & 0x0f) == 0x0f) {
                        data &= 0xf0;
                    }
                    dest.writeinc(data & 0xf);
                } else {
                    dest.writeinc((data >> 4) & 0xFF);
                    dest.writeinc(data & 0xF);
                }
            } while (dest.offset < finish.offset);
        }
        temp = null;
    }

    /*TODO*///	
/*TODO*///	static void sys16_sprite_decode2( int num_banks, int bank_size, int side_markers ){
/*TODO*///		unsigned char *base = memory_region(REGION_GFX2);
/*TODO*///		unsigned char *temp = malloc( bank_size );
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		if( !temp ) return;
/*TODO*///	
/*TODO*///		for( i = num_banks; i >0; i-- ){
/*TODO*///			unsigned char *finish	= base + 2*bank_size*i;
/*TODO*///			unsigned char *dest = finish - 2*bank_size;
/*TODO*///	
/*TODO*///			unsigned char *p1 = temp;
/*TODO*///			unsigned char *p2 = temp+bank_size/4;
/*TODO*///			unsigned char *p3 = temp+bank_size/2;
/*TODO*///			unsigned char *p4 = temp+bank_size/4*3;
/*TODO*///	
/*TODO*///			unsigned char data;
/*TODO*///	
/*TODO*///			memcpy (temp, base+bank_size*(i-1), bank_size);
/*TODO*///	
/*TODO*///	/*
/*TODO*///		note: both pen#0 and pen#15 are transparent.
/*TODO*///		we replace references to pen#15 with pen#0, to simplify the sprite rendering
/*TODO*///	*/
/*TODO*///			do {
/*TODO*///				data = *p4++;
/*TODO*///				if( side_markers )
/*TODO*///				{
/*TODO*///					if( (data&0x0f) == 0x0f )
/*TODO*///					{
/*TODO*///						if((data&0xf0) !=0xf0 && (data&0xf0) !=0)
/*TODO*///							*dest++ = data >> 4;
/*TODO*///						else
/*TODO*///							*dest++ = 0xff;
/*TODO*///						*dest++ = 0xff;
/*TODO*///					}
/*TODO*///					else if( (data&0xf0) == 0xf0 )
/*TODO*///					{
/*TODO*///						*dest++ = 0x00;
/*TODO*///						if( (data&0x0f) == 0x0f ) data &= 0xf0;
/*TODO*///						*dest++ = data &0xf;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						*dest++ = data >> 4;
/*TODO*///						*dest++ = data & 0xF;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if( (data&0xf0) == 0xf0 ) data &= 0x0f;
/*TODO*///					if( (data&0x0f) == 0x0f ) data &= 0xf0;
/*TODO*///					*dest++ = data >> 4;
/*TODO*///					*dest++ = data & 0xF;
/*TODO*///				}
/*TODO*///	
/*TODO*///				data = *p3++;
/*TODO*///				if( side_markers )
/*TODO*///				{
/*TODO*///					if( (data&0x0f) == 0x0f )
/*TODO*///					{
/*TODO*///						if((data&0xf0) !=0xf0 && (data&0xf0) !=0)
/*TODO*///							*dest++ = data >> 4;
/*TODO*///						else
/*TODO*///							*dest++ = 0xff;
/*TODO*///						*dest++ = 0xff;
/*TODO*///					}
/*TODO*///					else if( (data&0xf0) == 0xf0 )
/*TODO*///					{
/*TODO*///						*dest++ = 0x00;
/*TODO*///						if( (data&0x0f) == 0x0f ) data &= 0xf0;
/*TODO*///						*dest++ = data &0xf;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						*dest++ = data >> 4;
/*TODO*///						*dest++ = data & 0xF;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if( (data&0xf0) == 0xf0 ) data &= 0x0f;
/*TODO*///					if( (data&0x0f) == 0x0f ) data &= 0xf0;
/*TODO*///					*dest++ = data >> 4;
/*TODO*///					*dest++ = data & 0xF;
/*TODO*///				}
/*TODO*///	
/*TODO*///	
/*TODO*///				data = *p2++;
/*TODO*///				if( side_markers )
/*TODO*///				{
/*TODO*///					if( (data&0x0f) == 0x0f )
/*TODO*///					{
/*TODO*///						if((data&0xf0) !=0xf0 && (data&0xf0) !=0)
/*TODO*///							*dest++ = data >> 4;
/*TODO*///						else
/*TODO*///							*dest++ = 0xff;
/*TODO*///						*dest++ = 0xff;
/*TODO*///					}
/*TODO*///					else if( (data&0xf0) == 0xf0 )
/*TODO*///					{
/*TODO*///						*dest++ = 0x00;
/*TODO*///						if( (data&0x0f) == 0x0f ) data &= 0xf0;
/*TODO*///						*dest++ = data &0xf;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						*dest++ = data >> 4;
/*TODO*///						*dest++ = data & 0xF;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if( (data&0xf0) == 0xf0 ) data &= 0x0f;
/*TODO*///					if( (data&0x0f) == 0x0f ) data &= 0xf0;
/*TODO*///					*dest++ = data >> 4;
/*TODO*///					*dest++ = data & 0xF;
/*TODO*///				}
/*TODO*///	
/*TODO*///				data = *p1++;
/*TODO*///				if( side_markers )
/*TODO*///				{
/*TODO*///					if( (data&0x0f) == 0x0f )
/*TODO*///					{
/*TODO*///						if((data&0xf0) !=0xf0 && (data&0xf0) !=0)
/*TODO*///							*dest++ = data >> 4;
/*TODO*///						else
/*TODO*///							*dest++ = 0xff;
/*TODO*///						*dest++ = 0xff;
/*TODO*///					}
/*TODO*///					else if( (data&0xf0) == 0xf0 )
/*TODO*///					{
/*TODO*///						*dest++ = 0x00;
/*TODO*///						if( (data&0x0f) == 0x0f ) data &= 0xf0;
/*TODO*///						*dest++ = data &0xf;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						*dest++ = data >> 4;
/*TODO*///						*dest++ = data & 0xF;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if( (data&0xf0) == 0xf0 ) data &= 0x0f;
/*TODO*///					if( (data&0x0f) == 0x0f ) data &= 0xf0;
/*TODO*///					*dest++ = data >> 4;
/*TODO*///					*dest++ = data & 0xF;
/*TODO*///				}
/*TODO*///	
/*TODO*///			} while( dest<finish );
/*TODO*///		}
/*TODO*///		free( temp );
/*TODO*///	}
/*TODO*///	
/*TODO*///	int gr_bitmap_width;
/*TODO*///	
/*TODO*///	static void generate_gr_screen(int w,int bitmap_width,int skip,int start_color,int end_color,int source_size)
/*TODO*///	{
/*TODO*///		UINT8 *buf;
/*TODO*///		UINT8 *gr = memory_region(REGION_GFX3);
/*TODO*///		UINT8 *grr = NULL;
/*TODO*///	    int i,j,k;
/*TODO*///	    int center_offset=0;
/*TODO*///	
/*TODO*///	
/*TODO*///		buf=malloc(source_size);
/*TODO*///		if(buf==NULL) return;
/*TODO*///	
/*TODO*///		gr_bitmap_width = bitmap_width;
/*TODO*///	
/*TODO*///		memcpy(buf,gr,source_size);
/*TODO*///		memset(gr,0,256*bitmap_width);
/*TODO*///	
/*TODO*///		if (w!=gr_bitmap_width)
/*TODO*///		{
/*TODO*///			if (skip>0) // needs mirrored RHS
/*TODO*///				grr=gr;
/*TODO*///			else
/*TODO*///			{
/*TODO*///				center_offset=(gr_bitmap_width-w);
/*TODO*///				gr+=center_offset/2;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///	    // build gr_bitmap
/*TODO*///		for (i=0; i<256; i++)
/*TODO*///		{
/*TODO*///			UINT8 last_bit;
/*TODO*///			UINT8 color_data[4];
/*TODO*///	
/*TODO*///			color_data[0]=start_color; color_data[1]=start_color+1;
/*TODO*///			color_data[2]=start_color+2; color_data[3]=start_color+3;
/*TODO*///			last_bit=((buf[0]&0x80)==0)|(((buf[0x4000]&0x80)==0)<<1);
/*TODO*///			for (j=0; j<w/8; j++)
/*TODO*///			{
/*TODO*///				for (k=0; k<8; k++)
/*TODO*///				{
/*TODO*///					UINT8 bit=((buf[0]&0x80)==0)|(((buf[0x4000]&0x80)==0)<<1);
/*TODO*///					if (bit!=last_bit && bit==0 && i>1)
/*TODO*///					{ // color flipped to 0,advance color[0]
/*TODO*///						if (color_data[0]+end_color <= end_color)
/*TODO*///						{
/*TODO*///							color_data[0]+=end_color;
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							color_data[0]-=end_color;
/*TODO*///						}
/*TODO*///					}
/*TODO*///					*gr = color_data[bit];
/*TODO*///					last_bit=bit;
/*TODO*///					buf[0] <<= 1; buf[0x4000] <<= 1; gr++;
/*TODO*///				}
/*TODO*///				buf++;
/*TODO*///			}
/*TODO*///	
/*TODO*///			if (grr!=NULL)
/*TODO*///			{ // need mirrored RHS
/*TODO*///				UINT8 *_gr=gr-1;
/*TODO*///				_gr -= skip;
/*TODO*///				for (j=0; j<w-skip; j++)
/*TODO*///				{
/*TODO*///					*gr++ = *_gr--;
/*TODO*///				}
/*TODO*///				for (j=0; j<skip; j++) *gr++ = 0;
/*TODO*///			}
/*TODO*///			else if (center_offset!=0)
/*TODO*///			{
/*TODO*///				gr+=center_offset;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		i=1;
/*TODO*///		while ( (1<<i) < gr_bitmap_width ) i++;
/*TODO*///		gr_bitmap_width=i; // power of 2
/*TODO*///	
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	#define io_player1_r input_port_0_r
/*TODO*///	#define io_player2_r input_port_1_r
/*TODO*///	#define io_player3_r input_port_5_r
/*TODO*///	#define io_player4_r input_port_6_r
/*TODO*///	#define io_service_r input_port_2_r
/*TODO*///	
/*TODO*///	#define io_dip1_r input_port_3_r
/*TODO*///	#define io_dip2_r input_port_4_r
/*TODO*///	#define io_dip3_r input_port_5_r
/*TODO*///	
/*TODO*///	/***************************************************************************/
    static void patch_codeX(int offset, int data, int cpu) {
        int aligned_offset = offset & 0xfffffe;
        UBytePtr RAM = memory_region(REGION_CPU1 + cpu);
        int old_word = RAM.READ_WORD(aligned_offset);

        if ((offset & 1) != 0) {
            data = (old_word & 0xff00) | data;
        } else {
            data = (old_word & 0x00ff) | (data << 8);
        }

        RAM.WRITE_WORD(aligned_offset, data);
    }

    public static WriteHandlerPtr patch_code = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            patch_codeX(offset, data, 0);
        }
    };
    public static WriteHandlerPtr patch_code2 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            patch_codeX(offset, data, 2);
        }
    };

    public static WriteHandlerPtr patch_z80code = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UBytePtr RAM = memory_region(REGION_CPU2);
            RAM.write(offset, data);
        }
    };
    /*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	#define SYS16_JOY1 PORT_START();  \
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON3 );\
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON1 );\
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON2 );\
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );\
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );\
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );\
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );\
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
/*TODO*///	
/*TODO*///	#define SYS16_JOY2 PORT_START();  \
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL );\
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );\
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );\
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );\
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );\
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );\
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );\
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
/*TODO*///	
/*TODO*///	#define SYS16_JOY3 PORT_START();  \
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER3 );\
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );\
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );\
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );\
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );\
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );\
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );\
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///	
/*TODO*///	#define SYS16_JOY1_SWAPPEDBUTTONS PORT_START();  \
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON3 );\
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 );\
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON1 );\
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );\
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );\
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );\
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );\
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
/*TODO*///	
/*TODO*///	#define SYS16_JOY2_SWAPPEDBUTTONS PORT_START();  \
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL );\
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );\
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );\
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );\
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );\
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );\
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );\
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
/*TODO*///	
/*TODO*///	#define SYS16_SERVICE PORT_START();  \
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );\
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );\
/*TODO*///		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE ) \
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN3 );\
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );\
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );\
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );\
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///	#define SYS16_COINAGE PORT_START();  \
/*TODO*///		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") ); \
/*TODO*///		PORT_DIPSETTING(    0x07, DEF_STR( "4C_1C") ); \
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") ); \
/*TODO*///		PORT_DIPSETTING(    0x09, DEF_STR( "2C_1C") ); \
/*TODO*///		PORT_DIPSETTING(    0x05, "2 Coins/1 Credit 5/3 6/4");\
/*TODO*///		PORT_DIPSETTING(    0x04, "2 Coins/1 Credit 4/3");\
/*TODO*///		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") ); \
/*TODO*///		PORT_DIPSETTING(    0x01, "1 Coin/1 Credit 2/3");\
/*TODO*///		PORT_DIPSETTING(    0x02, "1 Coin/1 Credit 4/5");\
/*TODO*///		PORT_DIPSETTING(    0x03, "1 Coin/1 Credit 5/6");\
/*TODO*///		PORT_DIPSETTING(    0x06, DEF_STR( "2C_3C") ); \
/*TODO*///		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") ); \
/*TODO*///		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") ); \
/*TODO*///		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") ); \
/*TODO*///		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") ); \
/*TODO*///		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") ); \
/*TODO*///		PORT_DIPSETTING(    0x00, "Free Play (if Coin B too);or 1/1") \
/*TODO*///		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") ); \
/*TODO*///		PORT_DIPSETTING(    0x70, DEF_STR( "4C_1C") ); \
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "3C_1C") ); \
/*TODO*///		PORT_DIPSETTING(    0x90, DEF_STR( "2C_1C") ); \
/*TODO*///		PORT_DIPSETTING(    0x50, "2 Coins/1 Credit 5/3 6/4");\
/*TODO*///		PORT_DIPSETTING(    0x40, "2 Coins/1 Credit 4/3");\
/*TODO*///		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") ); \
/*TODO*///		PORT_DIPSETTING(    0x10, "1 Coin/1 Credit 2/3");\
/*TODO*///		PORT_DIPSETTING(    0x20, "1 Coin/1 Credit 4/5");\
/*TODO*///		PORT_DIPSETTING(    0x30, "1 Coin/1 Credit 5/6");\
/*TODO*///		PORT_DIPSETTING(    0x60, DEF_STR( "2C_3C") ); \
/*TODO*///		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") ); \
/*TODO*///		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") ); \
/*TODO*///		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") ); \
/*TODO*///		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") ); \
/*TODO*///		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") ); \
/*TODO*///		PORT_DIPSETTING(    0x00, "Free Play (if Coin A too);or 1/1")
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	// sys16A
/*TODO*///	static RomLoadPtr rom_alexkidd = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x040000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "epr10429.42", 0x000000, 0x10000, 0xbdf49eca )
/*TODO*///		ROM_LOAD_ODD ( "epr10427.26", 0x000000, 0x10000, 0xf6e3dd29 )
/*TODO*///		ROM_LOAD_EVEN( "epr10430.43", 0x020000, 0x10000, 0x89e3439f )
/*TODO*///		ROM_LOAD_ODD ( "epr10428.25", 0x020000, 0x10000, 0xdbed3210 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "10431.95", 0x00000, 0x08000, 0xa7962c39 );
/*TODO*///		ROM_LOAD( "10432.94", 0x08000, 0x08000, 0xdb8cd24e );
/*TODO*///		ROM_LOAD( "10433.93", 0x10000, 0x08000, 0xe163c8c2 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x050000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "10437.10", 0x000000, 0x008000, 0x522f7618 );
/*TODO*///		ROM_LOAD( "10441.11", 0x008000, 0x008000, 0x74e3a35c );
/*TODO*///		ROM_LOAD( "10438.17", 0x010000, 0x008000, 0x738a6362 );
/*TODO*///		ROM_LOAD( "10442.18", 0x018000, 0x008000, 0x86cb9c14 );
/*TODO*///		ROM_LOAD( "10439.23", 0x020000, 0x008000, 0xb391aca7 );
/*TODO*///		ROM_LOAD( "10443.24", 0x028000, 0x008000, 0x95d32635 );
/*TODO*///		ROM_LOAD( "10440.29", 0x030000, 0x008000, 0x23939508 );
/*TODO*///		ROM_LOAD( "10444.30", 0x038000, 0x008000, 0x82115823 );
/*TODO*///	//	ROM_LOAD( "10437.10", 0x040000, 0x008000, 0x522f7618 );twice?
/*TODO*///	//	ROM_LOAD( "10441.11", 0x048000, 0x008000, 0x74e3a35c );twice?
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "10434.12", 0x0000, 0x8000, 0x77141cce );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x1000, REGION_CPU3 );     /* 4k for 7751 onboard ROM */
/*TODO*///		ROM_LOAD( "7751.bin",     0x0000, 0x0400, 0x6a9534fc );/* 7751 - U34 */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_SOUND1 );/* 7751 sound data (not used yet) */
/*TODO*///		ROM_LOAD( "10435.1", 0x0000, 0x8000, 0xad89f6e3 );
/*TODO*///		ROM_LOAD( "10436.2", 0x8000, 0x8000, 0x96c76613 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_alexkida = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x040000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "10447.43", 0x000000, 0x10000, 0x29e87f71 )
/*TODO*///		ROM_LOAD_ODD ( "10445.26", 0x000000, 0x10000, 0x25ce5b6f )
/*TODO*///		ROM_LOAD_EVEN( "10448.42", 0x020000, 0x10000, 0x05baedb5 )
/*TODO*///		ROM_LOAD_ODD ( "10446.25", 0x020000, 0x10000, 0xcd61d23c )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "10431.95", 0x00000, 0x08000, 0xa7962c39 );
/*TODO*///		ROM_LOAD( "10432.94", 0x08000, 0x08000, 0xdb8cd24e );
/*TODO*///		ROM_LOAD( "10433.93", 0x10000, 0x08000, 0xe163c8c2 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x050000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "10437.10", 0x000000, 0x008000, 0x522f7618 );
/*TODO*///		ROM_LOAD( "10441.11", 0x008000, 0x008000, 0x74e3a35c );
/*TODO*///		ROM_LOAD( "10438.17", 0x010000, 0x008000, 0x738a6362 );
/*TODO*///		ROM_LOAD( "10442.18", 0x018000, 0x008000, 0x86cb9c14 );
/*TODO*///		ROM_LOAD( "10439.23", 0x020000, 0x008000, 0xb391aca7 );
/*TODO*///		ROM_LOAD( "10443.24", 0x028000, 0x008000, 0x95d32635 );
/*TODO*///		ROM_LOAD( "10440.29", 0x030000, 0x008000, 0x23939508 );
/*TODO*///		ROM_LOAD( "10444.30", 0x038000, 0x008000, 0x82115823 );
/*TODO*///	//	ROM_LOAD( "10437.10", 0x040000, 0x008000, 0x522f7618 );twice?
/*TODO*///	//	ROM_LOAD( "10441.11", 0x048000, 0x008000, 0x74e3a35c );twice?
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "10434.12", 0x0000, 0x8000, 0x77141cce );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x1000, REGION_CPU3 );     /* 4k for 7751 onboard ROM */
/*TODO*///		ROM_LOAD( "7751.bin",     0x0000, 0x0400, 0x6a9534fc );/* 7751 - U34 */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_SOUND1 );/* 7751 sound data */
/*TODO*///		ROM_LOAD( "10435.1", 0x0000, 0x8000, 0xad89f6e3 );
/*TODO*///		ROM_LOAD( "10436.2", 0x8000, 0x8000, 0x96c76613 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr alexkidd_skip = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		if (cpu_get_pc()==0x242c) {cpu_spinuntil_int(); return 0xffff;}
/*TODO*///	
/*TODO*///		return READ_WORD(&sys16_workingram[0x3108]);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static MemoryReadAddress alexkidd_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x440000, 0x440fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x840000, 0x840fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0xc40002, 0xc40005, MRA_NOP ),		//??
/*TODO*///		new MemoryReadAddress( 0xc41002, 0xc41003, input_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xc41006, 0xc41007, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0xc41000, 0xc41001, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xc42000, 0xc42001, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xc42002, 0xc42003, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xc60000, 0xc60001, MRA_NOP ),
/*TODO*///		new MemoryReadAddress( 0xfff108, 0xfff109, alexkidd_skip ),
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress alexkidd_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x440000, 0x440fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x840000, 0x840fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc40001, sound_command_nmi_w ),
/*TODO*///		new MemoryWriteAddress( 0xc40002, 0xc40005, MWA_NOP ),		//??
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void alexkidd_update_proc( void ){
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0ff8] ) & 0x01ff;
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0ffa] ) & 0x01ff;
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0f24] ) & 0x00ff;
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0f26] ) & 0x01ff;
/*TODO*///	
/*TODO*///		set_fg_page1( READ_WORD( &sys16_textram[0x0e9e] ) );
/*TODO*///		set_bg_page1( READ_WORD( &sys16_textram[0x0e9c] ) );
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr alexkidd_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = { 00,01,02,03,00,01,02,03,00,01,02,03,00,01,02,03};
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_textmode=1;
/*TODO*///		sys16_spritesystem = 2;
/*TODO*///		sys16_sprxoffset = -0xbc;
/*TODO*///		sys16_fgxoffset = sys16_bgxoffset = 7;
/*TODO*///		sys16_bg_priority_mode=1;
/*TODO*///	
/*TODO*///		sys16_update_proc = alexkidd_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_alexkidd = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_sprite_decode( 5,0x010000 );
/*TODO*///	} };
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_alexkidd = new InputPortPtr(){ public void handler() { 
/*TODO*///		SYS16_JOY1_SWAPPEDBUTTONS
/*TODO*///		SYS16_JOY2_SWAPPEDBUTTONS
/*TODO*///		SYS16_SERVICE
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x00, "Continues" );
/*TODO*///		PORT_DIPSETTING(    0x01, "Only before level 5" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Unlimited" );
/*TODO*///		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Lives") );
/*TODO*///		PORT_DIPSETTING(    0x0c, "3" );
/*TODO*///		PORT_DIPSETTING(    0x08, "4" );
/*TODO*///		PORT_DIPSETTING(    0x04, "5" );
/*TODO*///		PORT_BITX( 0,       0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "240", IP_KEY_NONE, IP_JOY_NONE );
/*TODO*///		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Bonus_Life") );
/*TODO*///		PORT_DIPSETTING(    0x20, "10000" );
/*TODO*///		PORT_DIPSETTING(    0x30, "20000" );
/*TODO*///		PORT_DIPSETTING(    0x10, "40000" );
/*TODO*///		PORT_DIPSETTING(    0x00, "None" );
/*TODO*///		PORT_DIPNAME( 0xc0, 0xc0, "Time Adjust" );
/*TODO*///		PORT_DIPSETTING(    0x80, "70" );
/*TODO*///		PORT_DIPSETTING(    0xc0, "60" );
/*TODO*///		PORT_DIPSETTING(    0x40, "50" );
/*TODO*///		PORT_DIPSETTING(    0x00, "40" );
/*TODO*///	
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_7751( machine_driver_alexkidd, \
/*TODO*///		alexkidd_readmem,alexkidd_writemem,alexkidd_init_machine,gfx8 )
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	// sys16B
/*TODO*///	static RomLoadPtr rom_aliensyn = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x030000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "11083.a4", 0x00000, 0x8000, 0xcb2ad9b3 )
/*TODO*///		ROM_LOAD_ODD ( "11080.a1", 0x00000, 0x8000, 0xfe7378d9 )
/*TODO*///		ROM_LOAD_EVEN( "11084.a5", 0x10000, 0x8000, 0x2e1ec7b1 )
/*TODO*///		ROM_LOAD_ODD ( "11081.a2", 0x10000, 0x8000, 0x1308ee63 )
/*TODO*///		ROM_LOAD_EVEN( "11085.a6", 0x20000, 0x8000, 0xcff78f39 )
/*TODO*///		ROM_LOAD_ODD ( "11082.a3", 0x20000, 0x8000, 0x9cdc2a14 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "10702.b9",  0x00000, 0x10000, 0x393bc813 );
/*TODO*///		ROM_LOAD( "10703.b10", 0x10000, 0x10000, 0x6b6dd9f5 );
/*TODO*///		ROM_LOAD( "10704.b11", 0x20000, 0x10000, 0x911e7ebc );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x080000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "10709.b1", 0x00000, 0x10000, 0xaddf0a90 );
/*TODO*///		ROM_LOAD( "10713.b5", 0x10000, 0x10000, 0xececde3a );
/*TODO*///		ROM_LOAD( "10710.b2", 0x20000, 0x10000, 0x992369eb );
/*TODO*///		ROM_LOAD( "10714.b6", 0x30000, 0x10000, 0x91bf42fb );
/*TODO*///		ROM_LOAD( "10711.b3", 0x40000, 0x10000, 0x29166ef6 );
/*TODO*///		ROM_LOAD( "10715.b7", 0x50000, 0x10000, 0xa7c57384 );
/*TODO*///		ROM_LOAD( "10712.b4", 0x60000, 0x10000, 0x876ad019 );
/*TODO*///		ROM_LOAD( "10716.b8", 0x70000, 0x10000, 0x40ba1d48 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x28000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "10723.a7", 0x0000, 0x8000, 0x99953526 );
/*TODO*///		ROM_LOAD( "10724.a8", 0x10000, 0x8000, 0xf971a817 );
/*TODO*///		ROM_LOAD( "10725.a9", 0x18000, 0x8000, 0x6a50e08f );
/*TODO*///		ROM_LOAD( "10726.a10",0x20000, 0x8000, 0xd50b7736 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	// sys16A - use a different sound chip?
/*TODO*///	static RomLoadPtr rom_aliensya = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x030000, REGION_CPU1 );/* 68000 code. I guessing the order a bit here */
/*TODO*///		ROM_LOAD_EVEN( "10808", 0x00000, 0x8000, 0xe669929f )
/*TODO*///		ROM_LOAD_ODD ( "10806", 0x00000, 0x8000, 0x9f7f8fdd )
/*TODO*///		ROM_LOAD_EVEN( "10809", 0x10000, 0x8000, 0x9a424919 )
/*TODO*///		ROM_LOAD_ODD ( "10807", 0x10000, 0x8000, 0x3d2c3530 )
/*TODO*///		ROM_LOAD_EVEN( "10701", 0x20000, 0x8000, 0x92171751 )
/*TODO*///		ROM_LOAD_ODD ( "10698", 0x20000, 0x8000, 0xc1e4fdc0 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "10739", 0x00000, 0x10000, 0xa29ec207 );
/*TODO*///		ROM_LOAD( "10740", 0x10000, 0x10000, 0x47f93015 );
/*TODO*///		ROM_LOAD( "10741", 0x20000, 0x10000, 0x4970739c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x080000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "10709.b1", 0x00000, 0x10000, 0xaddf0a90 );
/*TODO*///		ROM_LOAD( "10713.b5", 0x10000, 0x10000, 0xececde3a );
/*TODO*///		ROM_LOAD( "10710.b2", 0x20000, 0x10000, 0x992369eb );
/*TODO*///		ROM_LOAD( "10714.b6", 0x30000, 0x10000, 0x91bf42fb );
/*TODO*///		ROM_LOAD( "10711.b3", 0x40000, 0x10000, 0x29166ef6 );
/*TODO*///		ROM_LOAD( "10715.b7", 0x50000, 0x10000, 0xa7c57384 );
/*TODO*///		ROM_LOAD( "10712.b4", 0x60000, 0x10000, 0x876ad019 );
/*TODO*///		ROM_LOAD( "10716.b8", 0x70000, 0x10000, 0x40ba1d48 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x28000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "10705", 0x00000, 0x8000, 0x777b749e );
/*TODO*///		ROM_LOAD( "10706", 0x10000, 0x8000, 0xaa114acc );
/*TODO*///		ROM_LOAD( "10707", 0x18000, 0x8000, 0x800c1d82 );
/*TODO*///		ROM_LOAD( "10708", 0x20000, 0x8000, 0x5921ef52 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_aliensyj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x030000, REGION_CPU1 );/* Custom 68000 code . I guessing the order a bit here */
/*TODO*///	// custom cpu 317-0033
/*TODO*///		ROM_LOAD_EVEN( "epr10699.43", 0x00000, 0x8000, 0x3fd38d17 )
/*TODO*///		ROM_LOAD_ODD ( "epr10696.26", 0x00000, 0x8000, 0xd734f19f )
/*TODO*///		ROM_LOAD_EVEN( "epr10700.42", 0x10000, 0x8000, 0x3b04b252 )
/*TODO*///		ROM_LOAD_ODD ( "epr10697.25", 0x10000, 0x8000, 0xf2bc123d )
/*TODO*///		ROM_LOAD_EVEN( "10701", 0x20000, 0x8000, 0x92171751 )
/*TODO*///		ROM_LOAD_ODD ( "10698", 0x20000, 0x8000, 0xc1e4fdc0 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "10739", 0x00000, 0x10000, 0xa29ec207 );
/*TODO*///		ROM_LOAD( "10740", 0x10000, 0x10000, 0x47f93015 );
/*TODO*///		ROM_LOAD( "10741", 0x20000, 0x10000, 0x4970739c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x080000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "10709.b1", 0x00000, 0x10000, 0xaddf0a90 );
/*TODO*///		ROM_LOAD( "10713.b5", 0x10000, 0x10000, 0xececde3a );
/*TODO*///		ROM_LOAD( "10710.b2", 0x20000, 0x10000, 0x992369eb );
/*TODO*///		ROM_LOAD( "10714.b6", 0x30000, 0x10000, 0x91bf42fb );
/*TODO*///		ROM_LOAD( "10711.b3", 0x40000, 0x10000, 0x29166ef6 );
/*TODO*///		ROM_LOAD( "10715.b7", 0x50000, 0x10000, 0xa7c57384 );
/*TODO*///		ROM_LOAD( "10712.b4", 0x60000, 0x10000, 0x876ad019 );
/*TODO*///		ROM_LOAD( "10716.b8", 0x70000, 0x10000, 0x40ba1d48 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x28000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "10705", 0x00000, 0x8000, 0x777b749e );
/*TODO*///		ROM_LOAD( "10706", 0x10000, 0x8000, 0xaa114acc );
/*TODO*///		ROM_LOAD( "10707", 0x18000, 0x8000, 0x800c1d82 );
/*TODO*///		ROM_LOAD( "10708", 0x20000, 0x8000, 0x5921ef52 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_aliensyb = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x030000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "as_typeb.a4", 0x00000, 0x8000, 0x17bf5304 )
/*TODO*///		ROM_LOAD_ODD ( "as_typeb.a1", 0x00000, 0x8000, 0x4cd134df )
/*TODO*///		ROM_LOAD_EVEN( "as_typeb.a5", 0x10000, 0x8000, 0xc8b791b0 )
/*TODO*///		ROM_LOAD_ODD ( "as_typeb.a2", 0x10000, 0x8000, 0xbdcf4a30 )
/*TODO*///		ROM_LOAD_EVEN( "as_typeb.a6", 0x20000, 0x8000, 0x1d0790aa )
/*TODO*///		ROM_LOAD_ODD ( "as_typeb.a3", 0x20000, 0x8000, 0x1e7586b7 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "10702.b9",  0x00000, 0x10000, 0x393bc813 );
/*TODO*///		ROM_LOAD( "10703.b10", 0x10000, 0x10000, 0x6b6dd9f5 );
/*TODO*///		ROM_LOAD( "10704.b11", 0x20000, 0x10000, 0x911e7ebc );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x080000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "10709.b1", 0x00000, 0x10000, 0xaddf0a90 );
/*TODO*///		ROM_LOAD( "10713.b5", 0x10000, 0x10000, 0xececde3a );
/*TODO*///		ROM_LOAD( "10710.b2", 0x20000, 0x10000, 0x992369eb );
/*TODO*///		ROM_LOAD( "10714.b6", 0x30000, 0x10000, 0x91bf42fb );
/*TODO*///		ROM_LOAD( "10711.b3", 0x40000, 0x10000, 0x29166ef6 );
/*TODO*///		ROM_LOAD( "10715.b7", 0x50000, 0x10000, 0xa7c57384 );
/*TODO*///		ROM_LOAD( "10712.b4", 0x60000, 0x10000, 0x876ad019 );
/*TODO*///		ROM_LOAD( "10716.b8", 0x70000, 0x10000, 0x40ba1d48 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x28000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "10723.a7", 0x0000, 0x8000, 0x99953526 );
/*TODO*///		ROM_LOAD( "10724.a8", 0x10000, 0x8000, 0xf971a817 );
/*TODO*///		ROM_LOAD( "10725.a9", 0x18000, 0x8000, 0x6a50e08f );
/*TODO*///		ROM_LOAD( "10726.a10",0x20000, 0x8000, 0xd50b7736 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static MemoryReadAddress aliensyn_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x02ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x440000, 0x440fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x840000, 0x840fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0xc41002, 0xc41003, input_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xc41006, 0xc41007, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0xc41000, 0xc41001, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xc42002, 0xc42003, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xc42000, 0xc42001, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xc40000, 0xc40fff, MRA_EXTRAM ),
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress aliensyn_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x02ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x440000, 0x440fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x840000, 0x840fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xc00006, 0xc00007, sound_command_w ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc40fff, MWA_EXTRAM ),
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void aliensyn_update_proc( void ){
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0e98] );
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0e9a] );
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0e90] );
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0e92] );
/*TODO*///	
/*TODO*///		set_fg_page( READ_WORD( &sys16_textram[0x0e80] ) );
/*TODO*///		set_bg_page( READ_WORD( &sys16_textram[0x0e82] ) );
/*TODO*///	
/*TODO*///		set_refresh( READ_WORD( &sys16_extraram[0] ) ); // 0xc40001
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr aliensyn_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = { 0,0,0,0,0,0,0,6,0,0,0,4,0,2,0,0 };
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_bg_priority_mode=1;
/*TODO*///		sys16_fg_priority_mode=1;
/*TODO*///	
/*TODO*///		sys16_update_proc = aliensyn_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static void aliensyn_sprite_decode( void ){
/*TODO*///		sys16_sprite_decode( 4,0x20000 );
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_aliensyn = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_bg1_trans=1;
/*TODO*///		aliensyn_sprite_decode();
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_aliensyn = new InputPortPtr(){ public void handler() { 
/*TODO*///		SYS16_JOY1
/*TODO*///		SYS16_JOY2
/*TODO*///		SYS16_SERVICE
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Lives") );
/*TODO*///		PORT_DIPSETTING(    0x08, "2" );
/*TODO*///		PORT_DIPSETTING(    0x0c, "3" );
/*TODO*///		PORT_DIPSETTING(    0x04, "4" );
/*TODO*///		PORT_BITX( 0,       0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "127", IP_KEY_NONE, IP_JOY_NONE );
/*TODO*///		PORT_DIPNAME( 0x30, 0x30, "Timer" );
/*TODO*///		PORT_DIPSETTING(    0x00, "120" );
/*TODO*///		PORT_DIPSETTING(    0x10, "130" );
/*TODO*///		PORT_DIPSETTING(    0x20, "140" );
/*TODO*///		PORT_DIPSETTING(    0x30, "150" );
/*TODO*///		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x80, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0xc0, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x40, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static struct UPD7759_interface aliensyn_upd7759_interface =
/*TODO*///	{
/*TODO*///		1,			/* 1 chip */
/*TODO*///		480000,
/*TODO*///		{ 60 }, 	/* volumes */
/*TODO*///		{ REGION_CPU2 },			/* memory region 3 contains the sample data */
/*TODO*///	    UPD7759_SLAVE_MODE,
/*TODO*///		{ sound_cause_nmi },
/*TODO*///	};
/*TODO*///	
/*TODO*///	/****************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_7759( machine_driver_aliensyn, \
/*TODO*///		aliensyn_readmem,aliensyn_writemem,aliensyn_init_machine, gfx1, aliensyn_upd7759_interface )
/*TODO*///	
    /**
     * ************************************************************************
     */
    // sys16B
    static RomLoadPtr rom_altbeast = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x040000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("11705", 0x000000, 0x20000, 0x57dc5c7a);
            ROM_LOAD_ODD("11704", 0x000000, 0x20000, 0x33bbcf07);

            ROM_REGION(0x60000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* tiles */
            ROM_LOAD("11674", 0x00000, 0x20000, 0xa57a66d5);
            ROM_LOAD("11675", 0x20000, 0x20000, 0x2ef2f144);
            ROM_LOAD("11676", 0x40000, 0x20000, 0x0c04acac);

            ROM_REGION(0x100000 * 2, REGION_GFX2);/* sprites */
            ROM_LOAD("epr11677.b1", 0x00000, 0x10000, 0xa01425cd);
            ROM_CONTINUE(0x20000, 0x10000);
            ROM_LOAD("epr11681.b5", 0x10000, 0x10000, 0xd9e03363);
            ROM_CONTINUE(0x30000, 0x10000);
            ROM_LOAD("epr11678.b2", 0x40000, 0x10000, 0x17a9fc53);
            ROM_CONTINUE(0x60000, 0x10000);
            ROM_LOAD("epr11682.b6", 0x50000, 0x10000, 0xe3f77c5e);
            ROM_CONTINUE(0x70000, 0x10000);
            ROM_LOAD("epr11679.b3", 0x80000, 0x10000, 0x14dcc245);
            ROM_CONTINUE(0xa0000, 0x10000);
            ROM_LOAD("epr11683.b7", 0x90000, 0x10000, 0xf9a60f06);
            ROM_CONTINUE(0xb0000, 0x10000);
            ROM_LOAD("epr11680.b4", 0xc0000, 0x10000, 0xf43dcdec);
            ROM_CONTINUE(0xe0000, 0x10000);
            ROM_LOAD("epr11684.b8", 0xd0000, 0x10000, 0xb20c0edb);
            ROM_CONTINUE(0xf0000, 0x10000);

            ROM_REGION(0x50000, REGION_CPU2);/* sound CPU */
            ROM_LOAD("11671", 0x00000, 0x08000, 0x2b71343b);
            ROM_LOAD("opr11672", 0x10000, 0x20000, 0xbbd7f460);
            ROM_LOAD("opr11673", 0x30000, 0x20000, 0x400c4a36);
            ROM_END();
        }
    };

    static RomLoadPtr rom_jyuohki = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x040000, REGION_CPU1);/* Custom 68000 code. */
            // custom cpu 317-0065
            ROM_LOAD_EVEN("epr11670.a7", 0x000000, 0x20000, 0xb748eb07);
            ROM_LOAD_ODD("epr11669.a5", 0x000000, 0x20000, 0x005ecd11);

            ROM_REGION(0x60000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* tiles */
            ROM_LOAD("11674", 0x00000, 0x20000, 0xa57a66d5);
            ROM_LOAD("11675", 0x20000, 0x20000, 0x2ef2f144);
            ROM_LOAD("11676", 0x40000, 0x20000, 0x0c04acac);

            ROM_REGION(0x100000 * 2, REGION_GFX2);/* sprites */
            ROM_LOAD("epr11677.b1", 0x00000, 0x10000, 0xa01425cd);
            ROM_CONTINUE(0x20000, 0x10000);
            ROM_LOAD("epr11681.b5", 0x10000, 0x10000, 0xd9e03363);
            ROM_CONTINUE(0x30000, 0x10000);
            ROM_LOAD("epr11678.b2", 0x40000, 0x10000, 0x17a9fc53);
            ROM_CONTINUE(0x60000, 0x10000);
            ROM_LOAD("epr11682.b6", 0x50000, 0x10000, 0xe3f77c5e);
            ROM_CONTINUE(0x70000, 0x10000);
            ROM_LOAD("epr11679.b3", 0x80000, 0x10000, 0x14dcc245);
            ROM_CONTINUE(0xa0000, 0x10000);
            ROM_LOAD("epr11683.b7", 0x90000, 0x10000, 0xf9a60f06);
            ROM_CONTINUE(0xb0000, 0x10000);
            ROM_LOAD("epr11680.b4", 0xc0000, 0x10000, 0xf43dcdec);
            ROM_CONTINUE(0xe0000, 0x10000);
            ROM_LOAD("epr11684.b8", 0xd0000, 0x10000, 0xb20c0edb);
            ROM_CONTINUE(0xf0000, 0x10000);

            ROM_REGION(0x50000, REGION_CPU2);/* sound CPU */
            ROM_LOAD("11671", 0x00000, 0x08000, 0x2b71343b);
            ROM_LOAD("opr11672", 0x10000, 0x20000, 0xbbd7f460);
            ROM_LOAD("opr11673", 0x30000, 0x20000, 0x400c4a36);
            ROM_END();
        }
    };

    // sys16B
    static RomLoadPtr rom_altbeas2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x040000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("epr11740", 0x000000, 0x20000, 0xce227542);
            ROM_LOAD_ODD("epr11739", 0x000000, 0x20000, 0xe466eb65);

            ROM_REGION(0x60000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* tiles */
            ROM_LOAD("11674", 0x00000, 0x20000, 0xa57a66d5);
            ROM_LOAD("11675", 0x20000, 0x20000, 0x2ef2f144);
            ROM_LOAD("11676", 0x40000, 0x20000, 0x0c04acac);

            ROM_REGION(0x100000 * 2, REGION_GFX2);/* sprites */
            ROM_LOAD("epr11677.b1", 0x00000, 0x10000, 0xa01425cd);
            ROM_CONTINUE(0x20000, 0x10000);
            ROM_LOAD("epr11681.b5", 0x10000, 0x10000, 0xd9e03363);
            ROM_CONTINUE(0x30000, 0x10000);
            ROM_LOAD("epr11678.b2", 0x40000, 0x10000, 0x17a9fc53);
            ROM_CONTINUE(0x60000, 0x10000);
            ROM_LOAD("epr11682.b6", 0x50000, 0x10000, 0xe3f77c5e);
            ROM_CONTINUE(0x70000, 0x10000);
            ROM_LOAD("epr11679.b3", 0x80000, 0x10000, 0x14dcc245);
            ROM_CONTINUE(0xa0000, 0x10000);
            ROM_LOAD("epr11683.b7", 0x90000, 0x10000, 0xf9a60f06);
            ROM_CONTINUE(0xb0000, 0x10000);
            ROM_LOAD("epr11680.b4", 0xc0000, 0x10000, 0xf43dcdec);
            ROM_CONTINUE(0xe0000, 0x10000);
            ROM_LOAD("epr11684.b8", 0xd0000, 0x10000, 0xb20c0edb);
            ROM_CONTINUE(0xf0000, 0x10000);

            ROM_REGION(0x50000, REGION_CPU2);/* sound CPU */
            ROM_LOAD("opr11686", 0x00000, 0x08000, 0x828a45b3);// ???
            ROM_LOAD("opr11672", 0x10000, 0x20000, 0xbbd7f460);
            ROM_LOAD("opr11673", 0x30000, 0x20000, 0x400c4a36);
            ROM_END();
        }
    };

    /**
     * ************************************************************************
     */
    public static ReadHandlerPtr altbeast_skip = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x3994) {
                cpu_spinuntil_int();
                return 1 << 8;
            }

            return sys16_workingram.READ_WORD(0x301c);
        }
    };

    // ??? What is this, input test shows 4 bits to each player, but what does it do?
    public static ReadHandlerPtr altbeast_io_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return 0xff;
        }
    };

    static MemoryReadAddress altbeast_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x03ffff, MRA_ROM),
                new MemoryReadAddress(0x400000, 0x40ffff, sys16_tileram_r),
                new MemoryReadAddress(0x410000, 0x410fff, sys16_textram_r),
                new MemoryReadAddress(0x440000, 0x440fff, MRA_BANK2),
                new MemoryReadAddress(0x840000, 0x840fff, paletteram_word_r),
                new MemoryReadAddress(0xc41002, 0xc41003, input_port_0_r),
                new MemoryReadAddress(0xc41006, 0xc41007, input_port_1_r),
                new MemoryReadAddress(0xc41004, 0xc41005, altbeast_io_r),
                new MemoryReadAddress(0xc41000, 0xc41001, input_port_2_r),
                new MemoryReadAddress(0xc42002, 0xc42003, input_port_3_r),
                new MemoryReadAddress(0xc42000, 0xc42001, input_port_4_r),
                new MemoryReadAddress(0xc40000, 0xc40fff, MRA_BANK3),
                new MemoryReadAddress(0xfff01c, 0xfff01d, altbeast_skip),
                new MemoryReadAddress(0xffc000, 0xffffff, MRA_BANK1),
                new MemoryReadAddress(-1)
            };

    static MemoryWriteAddress altbeast_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x03ffff, MWA_ROM),
                new MemoryWriteAddress(0x400000, 0x40ffff, sys16_tileram_w, sys16_tileram),
                new MemoryWriteAddress(0x410000, 0x410fff, sys16_textram_w, sys16_textram),
                new MemoryWriteAddress(0x440000, 0x440fff, MWA_BANK2, sys16_spriteram),
                new MemoryWriteAddress(0x840000, 0x840fff, sys16_paletteram_w, paletteram),
                new MemoryWriteAddress(0xc40000, 0xc40fff, MWA_BANK3, sys16_extraram),
                new MemoryWriteAddress(0xfe0006, 0xfe0007, sound_command_w),
                new MemoryWriteAddress(0xffc000, 0xffffff, MWA_BANK1, sys16_workingram),
                new MemoryWriteAddress(-1)
            };

    /**
     * ************************************************************************
     */
    public static sys16_update_procPtr altbeast_update_proc = new sys16_update_procPtr() {
        public void handler() {
            sys16_fg_scrollx = sys16_textram.READ_WORD(0x0e98);
            sys16_bg_scrollx = sys16_textram.READ_WORD(0x0e9a);
            sys16_fg_scrolly = sys16_textram.READ_WORD(0x0e90);
            sys16_bg_scrolly = sys16_textram.READ_WORD(0x0e92);

            set_fg_page(sys16_textram.READ_WORD(0x0e80));
            set_bg_page(sys16_textram.READ_WORD(0x0e82));

            set_tile_bank(sys16_workingram.READ_WORD(0x3094));
            set_refresh(sys16_extraram.READ_WORD(0));
        }
    };

    public static InitMachinePtr altbeast_init_machine = new InitMachinePtr() {
        public void handler() {
            int bank[] = {0x00, 0x02, 0x04, 0x06, 0x08, 0x0A, 0x0C, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            sys16_obj_bank = bank;
            sys16_update_proc = altbeast_update_proc;
        }
    };

    public static InitMachinePtr altbeas2_init_machine = new InitMachinePtr() {
        public void handler() {
            int bank[] = {0x00, 0x00, 0x02, 0x00, 0x04, 0x00, 0x06, 0x00, 0x08, 0x00, 0x0A, 0x00, 0x0C, 0x00, 0x00, 0x00};
            sys16_obj_bank = bank;
            sys16_update_proc = altbeast_update_proc;
        }
    };

    public static InitDriverPtr init_altbeast = new InitDriverPtr() {
        public void handler() {
            sys16_onetime_init_machine.handler();
            sys16_sprite_decode(7, 0x20000);
        }
    };

    /**
     * ************************************************************************
     */
    static InputPortPtr input_ports_altbeast = new InputPortPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x07, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x09, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x05, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x04, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x02, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0x03, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x06, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x00, "Free Play (if Coin B too) or 1/1");
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x70, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x90, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x50, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x40, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x10, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x20, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0x30, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x60, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0xa0, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x00, "Free Play (if Coin A too) or 1/1");

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x01, 0x01, "Credits needed");
            PORT_DIPSETTING(0x01, "1 to start, 1 to continue");
            PORT_DIPSETTING(0x00, "2 to start, 1 to continue");
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Lives"));
            PORT_DIPSETTING(0x08, "2");
            PORT_DIPSETTING(0x0c, "3");
            PORT_DIPSETTING(0x04, "4");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "240", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x30, 0x30, "Energy Meter");
            PORT_DIPSETTING(0x20, "2");
            PORT_DIPSETTING(0x30, "3");
            PORT_DIPSETTING(0x10, "4");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0xc0, 0xc0, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x80, "Easy");
            PORT_DIPSETTING(0xc0, "Normal");
            PORT_DIPSETTING(0x40, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            INPUT_PORTS_END();
        }
    };

    /**
     * ************************************************************************
     */
    static MachineDriver machine_driver_altbeast = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000,
                        altbeast_readmem, altbeast_writemem, null, null,
                        sys16_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4096000,
                        sound_readmem_7759, sound_writemem, sound_readport, sound_writeport_7759,
                        ignore_interrupt, 1
                ),},
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1,
            altbeast_init_machine,
            40 * 8, 28 * 8, new rectangle(0 * 8, 40 * 8 - 1, 0 * 8, 28 * 8 - 1),
            gfx2,
            2048 * ShadowColorsMultiplier, 2048 * ShadowColorsMultiplier,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            sys16_vh_start,
            sys16_vh_stop,
            sys16_vh_screenrefresh,
            SOUND_SUPPORTS_STEREO, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface
                ), new MachineSound(
                        SOUND_UPD7759,
                        upd7759_interface
                )
            }
    );
    static MachineDriver machine_driver_altbeas2 = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000,
                        altbeast_readmem, altbeast_writemem, null, null,
                        sys16_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4096000,
                        sound_readmem_7759, sound_writemem, sound_readport, sound_writeport_7759,
                        ignore_interrupt, 1
                ),},
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1,
            altbeas2_init_machine,
            40 * 8, 28 * 8, new rectangle(0 * 8, 40 * 8 - 1, 0 * 8, 28 * 8 - 1),
            gfx2,
            2048 * ShadowColorsMultiplier, 2048 * ShadowColorsMultiplier,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            sys16_vh_start,
            sys16_vh_stop,
            sys16_vh_screenrefresh,
            SOUND_SUPPORTS_STEREO, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface
                ), new MachineSound(
                        SOUND_UPD7759,
                        upd7759_interface
                )
            }
    );
    /**
     * ************************************************************************
     */
    /*TODO*///	// sys18
/*TODO*///	static RomLoadPtr rom_astorm = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x080000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "epr13085.bin", 0x000000, 0x40000, 0x15f74e2d )
/*TODO*///		ROM_LOAD_ODD ( "epr13084.bin", 0x000000, 0x40000, 0x9687b38f )
/*TODO*///	
/*TODO*///		ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "epr13073.bin", 0x00000, 0x40000, 0xdf5d0a61 );
/*TODO*///		ROM_LOAD( "epr13074.bin", 0x40000, 0x40000, 0x787afab8 );
/*TODO*///		ROM_LOAD( "epr13075.bin", 0x80000, 0x40000, 0x4e01b477 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "mpr13082.bin", 0x000000, 0x40000, 0xa782b704 );
/*TODO*///		ROM_LOAD( "mpr13089.bin", 0x040000, 0x40000, 0x2a4227f0 );
/*TODO*///		ROM_LOAD( "mpr13081.bin", 0x080000, 0x40000, 0xeb510228 );
/*TODO*///		ROM_LOAD( "mpr13088.bin", 0x0c0000, 0x40000, 0x3b6b4c55 );
/*TODO*///		ROM_LOAD( "mpr13080.bin", 0x100000, 0x40000, 0xe668eefb );
/*TODO*///		ROM_LOAD( "mpr13087.bin", 0x140000, 0x40000, 0x2293427d );
/*TODO*///		ROM_LOAD( "epr13079.bin", 0x180000, 0x40000, 0xde9221ed );
/*TODO*///		ROM_LOAD( "epr13086.bin", 0x1c0000, 0x40000, 0x8c9a71c4 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr13083.bin", 0x10000, 0x20000, 0x5df3af20 );
/*TODO*///		ROM_LOAD( "epr13076.bin", 0x30000, 0x40000, 0x94e6c76e );
/*TODO*///		ROM_LOAD( "epr13077.bin", 0x70000, 0x40000, 0xe2ec0d8d );
/*TODO*///		ROM_LOAD( "epr13078.bin", 0xb0000, 0x40000, 0x15684dc5 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_astorm2p = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x080000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "epr13182.bin", 0x000000, 0x40000, 0xe31f2a1c )
/*TODO*///		ROM_LOAD_ODD ( "epr13181.bin", 0x000000, 0x40000, 0x78cd3b26 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "epr13073.bin", 0x00000, 0x40000, 0xdf5d0a61 );
/*TODO*///		ROM_LOAD( "epr13074.bin", 0x40000, 0x40000, 0x787afab8 );
/*TODO*///		ROM_LOAD( "epr13075.bin", 0x80000, 0x40000, 0x4e01b477 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "mpr13082.bin", 0x000000, 0x40000, 0xa782b704 );
/*TODO*///		ROM_LOAD( "mpr13089.bin", 0x040000, 0x40000, 0x2a4227f0 );
/*TODO*///		ROM_LOAD( "mpr13081.bin", 0x080000, 0x40000, 0xeb510228 );
/*TODO*///		ROM_LOAD( "mpr13088.bin", 0x0c0000, 0x40000, 0x3b6b4c55 );
/*TODO*///		ROM_LOAD( "mpr13080.bin", 0x100000, 0x40000, 0xe668eefb );
/*TODO*///		ROM_LOAD( "mpr13087.bin", 0x140000, 0x40000, 0x2293427d );
/*TODO*///		ROM_LOAD( "epr13079.bin", 0x180000, 0x40000, 0xde9221ed );
/*TODO*///		ROM_LOAD( "epr13086.bin", 0x1c0000, 0x40000, 0x8c9a71c4 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "ep13083a.bin", 0x10000, 0x20000, 0xe7528e06 );
/*TODO*///		ROM_LOAD( "epr13076.bin", 0x30000, 0x40000, 0x94e6c76e );
/*TODO*///		ROM_LOAD( "epr13077.bin", 0x70000, 0x40000, 0xe2ec0d8d );
/*TODO*///		ROM_LOAD( "epr13078.bin", 0xb0000, 0x40000, 0x15684dc5 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_astormbl = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x080000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "astorm.a6", 0x000000, 0x40000, 0x7682ed3e )
/*TODO*///		ROM_LOAD_ODD ( "astorm.a5", 0x000000, 0x40000, 0xefe9711e )
/*TODO*///	
/*TODO*///		ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "epr13073.bin", 0x00000, 0x40000, 0xdf5d0a61 );
/*TODO*///		ROM_LOAD( "epr13074.bin", 0x40000, 0x40000, 0x787afab8 );
/*TODO*///		ROM_LOAD( "epr13075.bin", 0x80000, 0x40000, 0x4e01b477 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "mpr13082.bin", 0x000000, 0x40000, 0xa782b704 );
/*TODO*///		ROM_LOAD( "astorm.a11",   0x040000, 0x40000, 0x7829c4f3 );
/*TODO*///		ROM_LOAD( "mpr13081.bin", 0x080000, 0x40000, 0xeb510228 );
/*TODO*///		ROM_LOAD( "mpr13088.bin", 0x0c0000, 0x40000, 0x3b6b4c55 );
/*TODO*///		ROM_LOAD( "mpr13080.bin", 0x100000, 0x40000, 0xe668eefb );
/*TODO*///		ROM_LOAD( "mpr13087.bin", 0x140000, 0x40000, 0x2293427d );
/*TODO*///		ROM_LOAD( "epr13079.bin", 0x180000, 0x40000, 0xde9221ed );
/*TODO*///		ROM_LOAD( "epr13086.bin", 0x1c0000, 0x40000, 0x8c9a71c4 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr13083.bin", 0x10000, 0x20000, 0x5df3af20 );
/*TODO*///		ROM_LOAD( "epr13076.bin", 0x30000, 0x40000, 0x94e6c76e );
/*TODO*///		ROM_LOAD( "epr13077.bin", 0x70000, 0x40000, 0xe2ec0d8d );
/*TODO*///		ROM_LOAD( "epr13078.bin", 0xb0000, 0x40000, 0x15684dc5 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr astorm_skip = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		if (cpu_get_pc()==0x3d4c) {cpu_spinuntil_int(); return 0xffff;}
/*TODO*///	
/*TODO*///		return READ_WORD(&sys16_workingram[0x2c2c]);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static MemoryReadAddress astorm_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x100000, 0x10ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x110000, 0x110fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x140000, 0x140fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0x200000, 0x200fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0xa00000, 0xa00001, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xa00002, 0xa00003, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xa01002, 0xa01003, input_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xa01004, 0xa01005, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0xa01006, 0xa01007, io_player3_r ),
/*TODO*///		new MemoryReadAddress( 0xa01000, 0xa01001, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xa00000, 0xa0ffff, MRA_EXTRAM2 ),
/*TODO*///		new MemoryReadAddress( 0xc00000, 0xc0ffff, MRA_EXTRAM ),
/*TODO*///		new MemoryReadAddress( 0xc40000, 0xc4ffff, MRA_EXTRAM3 ),
/*TODO*///		new MemoryReadAddress( 0xffec2c, 0xffec2d, astorm_skip ),
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress astorm_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x100000, 0x10ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x110000, 0x110fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x140000, 0x140fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0x200000, 0x200fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0xa00006, 0xa00007, sound_command_nmi_w ),
/*TODO*///		new MemoryWriteAddress( 0xa00000, 0xa0ffff, MWA_BANK4,sys16_extraram2 ),
/*TODO*///		new MemoryWriteAddress( 0xc00000, 0xc0ffff, MWA_EXTRAM ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc4ffff, MWA_EXTRAM3 ),
/*TODO*///		new MemoryWriteAddress( 0xfe0020, 0xfe003f, MWA_NOP ),
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void astorm_update_proc( void ){
/*TODO*///		int data;
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0e98] );
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0e9a] );
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0e90] );
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0e92] );
/*TODO*///	
/*TODO*///		data = READ_WORD( &sys16_textram[0x0e80] );
/*TODO*///		sys16_fg_page[1] = data>>12;
/*TODO*///		sys16_fg_page[3] = (data>>8)&0xf;
/*TODO*///		sys16_fg_page[0] = (data>>4)&0xf;
/*TODO*///		sys16_fg_page[2] = data&0xf;
/*TODO*///	
/*TODO*///		data = READ_WORD( &sys16_textram[0x0e82] );
/*TODO*///		sys16_bg_page[1] = data>>12;
/*TODO*///		sys16_bg_page[3] = (data>>8)&0xf;
/*TODO*///		sys16_bg_page[0] = (data>>4)&0xf;
/*TODO*///		sys16_bg_page[2] = data&0xf;
/*TODO*///	
/*TODO*///	
/*TODO*///		sys16_fg2_scrollx = READ_WORD( &sys16_textram[0x0e9c] );
/*TODO*///		sys16_bg2_scrollx = READ_WORD( &sys16_textram[0x0e9e] );
/*TODO*///		sys16_fg2_scrolly = READ_WORD( &sys16_textram[0x0e94] );
/*TODO*///		sys16_bg2_scrolly = READ_WORD( &sys16_textram[0x0e96] );
/*TODO*///	
/*TODO*///		data = READ_WORD( &sys16_textram[0x0e84] );
/*TODO*///		sys16_fg2_page[1] = data>>12;
/*TODO*///		sys16_fg2_page[3] = (data>>8)&0xf;
/*TODO*///		sys16_fg2_page[0] = (data>>4)&0xf;
/*TODO*///		sys16_fg2_page[2] = data&0xf;
/*TODO*///	
/*TODO*///		data = READ_WORD( &sys16_textram[0x0e86] );
/*TODO*///		sys16_bg2_page[1] = data>>12;
/*TODO*///		sys16_bg2_page[3] = (data>>8)&0xf;
/*TODO*///		sys16_bg2_page[0] = (data>>4)&0xf;
/*TODO*///		sys16_bg2_page[2] = data&0xf;
/*TODO*///	
/*TODO*///		if(sys16_fg2_scrollx | sys16_fg2_scrolly | READ_WORD( &sys16_textram[0x0e84] ))
/*TODO*///			sys18_fg2_active=1;
/*TODO*///		else
/*TODO*///			sys18_fg2_active=0;
/*TODO*///		if(sys16_bg2_scrollx | sys16_bg2_scrolly | READ_WORD( &sys16_textram[0x0e86] ))
/*TODO*///			sys18_bg2_active=1;
/*TODO*///		else
/*TODO*///			sys18_bg2_active=0;
/*TODO*///	
/*TODO*///		set_tile_bank18( READ_WORD( &sys16_extraram2[0xe] ) ); // 0xa0000f
/*TODO*///		set_refresh_18( READ_WORD( &sys16_extraram3[0x6600] ) ); // 0xc46601
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr astorm_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = {0x00,0x02,0x04,0x06,0x08,0x0A,0x0C,0x0E,0x10,0x12,0x14,0x16,0x18,0x1A,0x1C,0x1E};
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_fgxoffset = sys16_bgxoffset = -9;
/*TODO*///	
/*TODO*///		patch_code( 0x2D6E, 0x32 );
/*TODO*///		patch_code( 0x2D6F, 0x3c );
/*TODO*///		patch_code( 0x2D70, 0x80 );
/*TODO*///		patch_code( 0x2D71, 0x00 );
/*TODO*///		patch_code( 0x2D72, 0x33 );
/*TODO*///		patch_code( 0x2D73, 0xc1 );
/*TODO*///		patch_code( 0x2ea2, 0x30 );
/*TODO*///		patch_code( 0x2ea3, 0x38 );
/*TODO*///		patch_code( 0x2ea4, 0xec );
/*TODO*///		patch_code( 0x2ea5, 0xf6 );
/*TODO*///		patch_code( 0x2ea6, 0x30 );
/*TODO*///		patch_code( 0x2ea7, 0x80 );
/*TODO*///		patch_code( 0x2e5c, 0x30 );
/*TODO*///		patch_code( 0x2e5d, 0x38 );
/*TODO*///		patch_code( 0x2e5e, 0xec );
/*TODO*///		patch_code( 0x2e5f, 0xe2 );
/*TODO*///		patch_code( 0x2e60, 0xc0 );
/*TODO*///		patch_code( 0x2e61, 0x7c );
/*TODO*///	
/*TODO*///		patch_code( 0x4cd8, 0x02 );
/*TODO*///		patch_code( 0x4cec, 0x03 );
/*TODO*///		patch_code( 0x2dc6c, 0xe9 );
/*TODO*///		patch_code( 0x2dc64, 0x10 );
/*TODO*///		patch_code( 0x2dc65, 0x10 );
/*TODO*///		patch_code( 0x3a100, 0x10 );
/*TODO*///		patch_code( 0x3a101, 0x13 );
/*TODO*///		patch_code( 0x3a102, 0x90 );
/*TODO*///		patch_code( 0x3a103, 0x2b );
/*TODO*///		patch_code( 0x3a104, 0x00 );
/*TODO*///		patch_code( 0x3a105, 0x01 );
/*TODO*///		patch_code( 0x3a106, 0x0c );
/*TODO*///		patch_code( 0x3a107, 0x00 );
/*TODO*///		patch_code( 0x3a108, 0x00 );
/*TODO*///		patch_code( 0x3a109, 0x01 );
/*TODO*///		patch_code( 0x3a10a, 0x66 );
/*TODO*///		patch_code( 0x3a10b, 0x06 );
/*TODO*///		patch_code( 0x3a10c, 0x42 );
/*TODO*///		patch_code( 0x3a10d, 0x40 );
/*TODO*///		patch_code( 0x3a10e, 0x54 );
/*TODO*///		patch_code( 0x3a10f, 0x8b );
/*TODO*///		patch_code( 0x3a110, 0x60 );
/*TODO*///		patch_code( 0x3a111, 0x02 );
/*TODO*///		patch_code( 0x3a112, 0x30 );
/*TODO*///		patch_code( 0x3a113, 0x1b );
/*TODO*///		patch_code( 0x3a114, 0x34 );
/*TODO*///		patch_code( 0x3a115, 0xc0 );
/*TODO*///		patch_code( 0x3a116, 0x34 );
/*TODO*///		patch_code( 0x3a117, 0xdb );
/*TODO*///		patch_code( 0x3a118, 0x24 );
/*TODO*///		patch_code( 0x3a119, 0xdb );
/*TODO*///		patch_code( 0x3a11a, 0x24 );
/*TODO*///		patch_code( 0x3a11b, 0xdb );
/*TODO*///		patch_code( 0x3a11c, 0x4e );
/*TODO*///		patch_code( 0x3a11d, 0x75 );
/*TODO*///		patch_code( 0xaf8e, 0x66 );
/*TODO*///	
/*TODO*///		/* fix missing credit text */
/*TODO*///		patch_code( 0x3f9a, 0xec );
/*TODO*///		patch_code( 0x3f9b, 0x36 );
/*TODO*///	
/*TODO*///		sys16_update_proc = astorm_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_astorm = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		UBytePtr RAM= memory_region(REGION_CPU2);
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys18_splittab_fg_x=&sys16_textram[0x0f80];
/*TODO*///		sys18_splittab_bg_x=&sys16_textram[0x0fc0];
/*TODO*///	
/*TODO*///		memcpy(RAM,&RAM[0x10000],0xa000);
/*TODO*///		sys16_MaxShadowColors=0;		// doesn't seem to use transparent shadows
/*TODO*///	
/*TODO*///		sys16_sprite_decode( 4,0x080000 );
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_astorm = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();  /* player 1 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON3 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
/*TODO*///	
/*TODO*///		PORT_START();  /* player 2 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN3 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN4 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START3 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///		PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x01, "2 Credits to Start" );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x1c, 0x1c, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x04, "Easiest" );
/*TODO*///		PORT_DIPSETTING(    0x08, "Easier" );
/*TODO*///		PORT_DIPSETTING(    0x0c, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x1c, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x10, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x14, "Harder" );
/*TODO*///		PORT_DIPSETTING(    0x18, "Hardest" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Special" );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, "Coin Chutes" );
/*TODO*///		PORT_DIPSETTING(    0x20, "3" );
/*TODO*///		PORT_DIPSETTING(    0x00, "1" );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unused") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();  /* player 3 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_18( machine_driver_astorm, \
/*TODO*///		astorm_readmem,astorm_writemem,astorm_init_machine, gfx4 )
/*TODO*///	
/*TODO*///	/***************************************************************************/
    // sys16B
    static RomLoadPtr rom_atomicp = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x020000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("ap-t2.bin", 0x000000, 0x10000, 0x97421047);
            ROM_LOAD_ODD("ap-t1.bin", 0x000000, 0x10000, 0x5c65fe56);

            ROM_REGION(0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* tiles */
            ROM_LOAD("ap-t4.bin", 0x00000, 0x8000, 0x332e58f4);
            ROM_LOAD("ap-t3.bin", 0x08000, 0x8000, 0xdddc122c);
            ROM_LOAD("ap-t5.bin", 0x10000, 0x8000, 0xef5ecd6b);

            ROM_REGION(0x1, REGION_GFX2);/* sprites */

            ROM_END();
        }
    };

    /**
     * ************************************************************************
     */
    public static ReadHandlerPtr atomicp_skip = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x7fc) {
                cpu_spinuntil_int();
                return 0xffff;
            }

            return sys16_workingram.READ_WORD(0x0902);
        }
    };

    static MemoryReadAddress atomicp_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x01ffff, MRA_ROM),
                new MemoryReadAddress(0x400000, 0x40ffff, sys16_tileram_r),
                new MemoryReadAddress(0x410000, 0x410fff, sys16_textram_r),
                new MemoryReadAddress(0x440000, 0x440fff, MRA_BANK2),
                new MemoryReadAddress(0x840000, 0x840fff, paletteram_word_r),
                new MemoryReadAddress(0xc41000, 0xc41001, input_port_0_r),
                new MemoryReadAddress(0xc41002, 0xc41003, input_port_1_r),
                new MemoryReadAddress(0xc41004, 0xc41005, input_port_3_r),
                new MemoryReadAddress(0xc41006, 0xc41007, input_port_4_r),
                //	new MemoryReadAddress( 0xffc902, 0xffc903, atomicp_skip ),
                new MemoryReadAddress(0xffc000, 0xffffff, MRA_BANK1),
                new MemoryReadAddress(-1)
            };

    public static WriteHandlerPtr atomicp_sound_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset == 0) {
                YM2413_register_port_0_w.handler(0, (data >> 8) & 0xff);
            } else {
                YM2413_data_port_0_w.handler(0, (data >> 8) & 0xff);
            }
        }
    };

    static MemoryWriteAddress atomicp_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x01ffff, MWA_ROM),
                new MemoryWriteAddress(0x080000, 0x080003, atomicp_sound_w),
                new MemoryWriteAddress(0x3f0000, 0x3f0003, MWA_NOP),
                new MemoryWriteAddress(0x400000, 0x40ffff, sys16_tileram_w, sys16_tileram),
                new MemoryWriteAddress(0x410000, 0x410fff, sys16_textram_w, sys16_textram),
                new MemoryWriteAddress(0x440000, 0x44ffff, MWA_BANK2, sys16_spriteram),
                new MemoryWriteAddress(0x840000, 0x840fff, sys16_paletteram_w, paletteram),
                new MemoryWriteAddress(0xc40000, 0xc40001, MWA_BANK4, sys16_extraram2),
                new MemoryWriteAddress(0xffc000, 0xffffff, MWA_BANK1, sys16_workingram),
                new MemoryWriteAddress(-1)
            };

    //	{ 0x0a, 0x0a, YM2413_register_port_0_w },
    //	{ 0x0b, 0x0b, YM2413_data_port_0_w },
    /**
     * ************************************************************************
     */
    public static sys16_update_procPtr atomicp_update_proc = new sys16_update_procPtr() {
        public void handler() {
            sys16_fg_scrollx = sys16_textram.READ_WORD(0x0e98);
            sys16_bg_scrollx = sys16_textram.READ_WORD(0x0e9a);
            sys16_fg_scrolly = sys16_textram.READ_WORD(0x0e90);
            sys16_bg_scrolly = sys16_textram.READ_WORD(0x0e92);

            set_fg_page(sys16_textram.READ_WORD(0x0e80));
            set_bg_page(sys16_textram.READ_WORD(0x0e82));
        }
    };
    public static InitMachinePtr atomicp_init_machine = new InitMachinePtr() {
        public void handler() {
            int bank[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            sys16_obj_bank = bank;
            sys16_update_proc = atomicp_update_proc;
        }
    };

    public static InitDriverPtr init_atomicp = new InitDriverPtr() {
        public void handler() {
            sys16_onetime_init_machine.handler();
        }
    };

    /**
     * ************************************************************************
     */
    static InputPortPtr input_ports_atomicp = new InputPortPtr() {
        public void handler() {

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START(); 	// dummy

            PORT_START(); 	// dip1
            PORT_DIPNAME(0x07, 0x07, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_5C"));

            PORT_DIPNAME(0x38, 0x38, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x18, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x38, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x28, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_5C"));

            PORT_DIPNAME(0xC0, 0xC0, DEF_STR("Lives"));
            PORT_DIPSETTING(0xC0, "1");
            PORT_DIPSETTING(0x80, "2");
            PORT_DIPSETTING(0x40, "3");
            PORT_DIPSETTING(0x00, "5");

            PORT_START();   //dip2
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x04, "Instructions");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, "Continuation");
            PORT_DIPSETTING(0x20, "Continue");
            PORT_DIPSETTING(0x00, "No Continue");
            PORT_DIPNAME(0x40, 0x00, "Level Select");
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_SERVICE(0x80, IP_ACTIVE_HIGH);
            INPUT_PORTS_END();
        }
    };

    /**
     * ************************************************************************
     */
    public static InterruptPtr ap_interrupt = new InterruptPtr() {
        public int handler() {
            int intleft = cpu_getiloops();
            if (intleft != 0) {
                return 2;
            } else {
                return 4;
            }
        }
    };

    static YM2413interface ym2413_interface = new YM2413interface(
            1,
            8000000, /* ??? */
            new int[]{30}
    );

    static MachineDriver machine_driver_atomicp = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000,
                        atomicp_readmem, atomicp_writemem, null, null,
                        ap_interrupt, 2
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1,
            atomicp_init_machine,
            40 * 8, 28 * 8, new rectangle(0 * 8, 40 * 8 - 1, 0 * 8, 28 * 8 - 1),
            gfx8,
            2048 * ShadowColorsMultiplier, 2048 * ShadowColorsMultiplier,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            sys16_vh_start,
            sys16_vh_stop,
            sys16_vh_screenrefresh,
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2413,
                        ym2413_interface
                )
            }
    );

    /*TODO*///	/***************************************************************************
/*TODO*///	
/*TODO*///	   Aurail
/*TODO*///	
/*TODO*///	***************************************************************************/
/*TODO*///	// sys16B
/*TODO*///	static RomLoadPtr rom_aurail = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0xc0000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "13577", 0x000000, 0x20000, 0x6701b686 )
/*TODO*///		ROM_LOAD_ODD ( "13576", 0x000000, 0x20000, 0x1e428d94 )
/*TODO*///		/* empty 0x40000 - 0x80000 */
/*TODO*///		ROM_LOAD_EVEN( "13447", 0x080000, 0x20000, 0x70a52167 )
/*TODO*///		ROM_LOAD_ODD ( "13445", 0x080000, 0x20000, 0x28dfc3dd )
/*TODO*///	
/*TODO*///		ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "aurail.a14", 0x00000, 0x20000, 0x0fc4a7a8 );/* plane 1 */
/*TODO*///		ROM_LOAD( "aurail.b14", 0x20000, 0x20000, 0xe08135e0 );
/*TODO*///		ROM_LOAD( "aurail.a15", 0x40000, 0x20000, 0x1c49852f );/* plane 2 */
/*TODO*///		ROM_LOAD( "aurail.b15", 0x60000, 0x20000, 0xe14c6684 );
/*TODO*///		ROM_LOAD( "aurail.a16", 0x80000, 0x20000, 0x047bde5e );/* plane 3 */
/*TODO*///		ROM_LOAD( "aurail.b16", 0xa0000, 0x20000, 0x6309fec4 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "aurail.b1",  0x000000, 0x020000, 0x5fa0a9f8 );
/*TODO*///		ROM_LOAD( "aurail.b5",  0x020000, 0x020000, 0x0d1b54da );
/*TODO*///		ROM_LOAD( "aurail.b2",  0x040000, 0x020000, 0x5f6b33b1 );
/*TODO*///		ROM_LOAD( "aurail.b6",  0x060000, 0x020000, 0xbad340c3 );
/*TODO*///		ROM_LOAD( "aurail.b3",  0x080000, 0x020000, 0x4e80520b );
/*TODO*///		ROM_LOAD( "aurail.b7",  0x0a0000, 0x020000, 0x7e9165ac );
/*TODO*///		ROM_LOAD( "aurail.b4",  0x0c0000, 0x020000, 0x5733c428 );
/*TODO*///		ROM_LOAD( "aurail.b8",  0x0e0000, 0x020000, 0x66b8f9b3 );
/*TODO*///		ROM_LOAD( "aurail.a1",  0x100000, 0x020000, 0x4f370b2b );
/*TODO*///		ROM_LOAD( "aurail.b10", 0x120000, 0x020000, 0xf76014bf );
/*TODO*///		ROM_LOAD( "aurail.a2",  0x140000, 0x020000, 0x37cf9cb4 );
/*TODO*///		ROM_LOAD( "aurail.b11", 0x160000, 0x020000, 0x1061e7da );
/*TODO*///		ROM_LOAD( "aurail.a3",  0x180000, 0x020000, 0x049698ef );
/*TODO*///		ROM_LOAD( "aurail.b12", 0x1a0000, 0x020000, 0x7dbcfbf1 );
/*TODO*///		ROM_LOAD( "aurail.a4",  0x1c0000, 0x020000, 0x77a8989e );
/*TODO*///		ROM_LOAD( "aurail.b13", 0x1e0000, 0x020000, 0x551df422 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "13448",      0x0000, 0x8000, 0xb5183fb9 );
/*TODO*///		ROM_LOAD( "aurail.a12", 0x10000,0x20000, 0xd3d9aaf9 );
/*TODO*///		ROM_LOAD( "aurail.a12", 0x30000,0x20000, 0xd3d9aaf9 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_auraila = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0xc0000, REGION_CPU1 );/* 68000 code */
/*TODO*///	// custom cpu 317-0168
/*TODO*///		ROM_LOAD_EVEN( "epr13469.a7", 0x000000, 0x20000, 0xc628b69d )
/*TODO*///		ROM_LOAD_ODD ( "epr13468.a5", 0x000000, 0x20000, 0xce092218 )
/*TODO*///		/* 0x40000 - 0x80000 is empty, I will place decrypted opcodes here */
/*TODO*///		ROM_LOAD_EVEN( "13447", 0x080000, 0x20000, 0x70a52167 )
/*TODO*///		ROM_LOAD_ODD ( "13445", 0x080000, 0x20000, 0x28dfc3dd )
/*TODO*///	
/*TODO*///		ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "aurail.a14", 0x00000, 0x20000, 0x0fc4a7a8 );/* plane 1 */
/*TODO*///		ROM_LOAD( "aurail.b14", 0x20000, 0x20000, 0xe08135e0 );
/*TODO*///		ROM_LOAD( "aurail.a15", 0x40000, 0x20000, 0x1c49852f );/* plane 2 */
/*TODO*///		ROM_LOAD( "aurail.b15", 0x60000, 0x20000, 0xe14c6684 );
/*TODO*///		ROM_LOAD( "aurail.a16", 0x80000, 0x20000, 0x047bde5e );/* plane 3 */
/*TODO*///		ROM_LOAD( "aurail.b16", 0xa0000, 0x20000, 0x6309fec4 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "aurail.b1",  0x000000, 0x020000, 0x5fa0a9f8 );
/*TODO*///		ROM_LOAD( "aurail.b5",  0x020000, 0x020000, 0x0d1b54da );
/*TODO*///		ROM_LOAD( "aurail.b2",  0x040000, 0x020000, 0x5f6b33b1 );
/*TODO*///		ROM_LOAD( "aurail.b6",  0x060000, 0x020000, 0xbad340c3 );
/*TODO*///		ROM_LOAD( "aurail.b3",  0x080000, 0x020000, 0x4e80520b );
/*TODO*///		ROM_LOAD( "aurail.b7",  0x0a0000, 0x020000, 0x7e9165ac );
/*TODO*///		ROM_LOAD( "aurail.b4",  0x0c0000, 0x020000, 0x5733c428 );
/*TODO*///		ROM_LOAD( "aurail.b8",  0x0e0000, 0x020000, 0x66b8f9b3 );
/*TODO*///		ROM_LOAD( "aurail.a1",  0x100000, 0x020000, 0x4f370b2b );
/*TODO*///		ROM_LOAD( "aurail.b10", 0x120000, 0x020000, 0xf76014bf );
/*TODO*///		ROM_LOAD( "aurail.a2",  0x140000, 0x020000, 0x37cf9cb4 );
/*TODO*///		ROM_LOAD( "aurail.b11", 0x160000, 0x020000, 0x1061e7da );
/*TODO*///		ROM_LOAD( "aurail.a3",  0x180000, 0x020000, 0x049698ef );
/*TODO*///		ROM_LOAD( "aurail.b12", 0x1a0000, 0x020000, 0x7dbcfbf1 );
/*TODO*///		ROM_LOAD( "aurail.a4",  0x1c0000, 0x020000, 0x77a8989e );
/*TODO*///		ROM_LOAD( "aurail.b13", 0x1e0000, 0x020000, 0x551df422 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "13448",      0x0000, 0x8000, 0xb5183fb9 );
/*TODO*///		ROM_LOAD( "aurail.a12", 0x10000,0x20000, 0xd3d9aaf9 );
/*TODO*///		ROM_LOAD( "aurail.a12", 0x30000,0x20000, 0xd3d9aaf9 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr aurail_skip = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		if (cpu_get_pc()==0xe4e) {cpu_spinuntil_int(); return 0;}
/*TODO*///	
/*TODO*///		return READ_WORD(&sys16_workingram[0x274e]);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static MemoryReadAddress aurail_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x0bffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x3f0000, 0x3fffff, MRA_EXTRAM ),
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x440000, 0x440fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x840000, 0x840fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0xc41002, 0xc41003, input_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xc41006, 0xc41007, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0xc41000, 0xc41001, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xc42002, 0xc42003, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xc42000, 0xc42001, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xc40000, 0xc4ffff, MRA_EXTRAM2 ),
/*TODO*///		new MemoryReadAddress( 0xfc0000, 0xfc0fff, MRA_EXTRAM3 ),
/*TODO*///		new MemoryReadAddress( 0xffe74e, 0xffe74f, aurail_skip ),
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress aurail_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x0bffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x3f0000, 0x3fffff, MWA_EXTRAM ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x440000, 0x440fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x840000, 0x840fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc4ffff, MWA_BANK4,sys16_extraram2 ),
/*TODO*///		new MemoryWriteAddress( 0xfc0000, 0xfc0fff, MWA_EXTRAM3 ),
/*TODO*///		new MemoryWriteAddress( 0xfe0006, 0xfe0007, sound_command_w ),
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void aurail_update_proc (void)
/*TODO*///	{
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0e98] );
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0e9a] );
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0e90] );
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0e92] );
/*TODO*///	
/*TODO*///		set_fg_page( READ_WORD( &sys16_textram[0x0e80] ) );
/*TODO*///		set_bg_page( READ_WORD( &sys16_textram[0x0e82] ) );
/*TODO*///	
/*TODO*///		set_tile_bank( READ_WORD( &sys16_extraram3[0x0002] ) );
/*TODO*///		set_refresh( READ_WORD( &sys16_extraram2[0] ) );
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr aurail_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = {0x00,0x02,0x04,0x06,0x08,0x0A,0x0C,0x0E,0x10,0x12,0x14,0x16,0x18,0x1A,0x1C,0x1E};
/*TODO*///	
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_spritesystem = 4;
/*TODO*///		sys16_spritelist_end=0x8000;
/*TODO*///		sys16_bg_priority_mode=1;
/*TODO*///	
/*TODO*///		sys16_update_proc = aurail_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_aurail = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_sprite_decode (8,0x40000);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_auraila = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		UBytePtr rom = memory_region(REGION_CPU1);
/*TODO*///		int diff = 0x40000;	/* place decrypted opcodes in a empty hole */
/*TODO*///	
/*TODO*///		init_aurail();
/*TODO*///	
/*TODO*///		memory_set_opcode_base(0,rom+diff);
/*TODO*///	
/*TODO*///		memcpy(rom+diff,rom,0x40000);
/*TODO*///	
/*TODO*///		aurail_decode_data(rom,rom,0x10000);
/*TODO*///		aurail_decode_opcode1(rom+diff,rom+diff,0x10000);
/*TODO*///		aurail_decode_opcode2(rom+diff+0x10000,rom+diff+0x10000,0x10000);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_aurail = new InputPortPtr(){ public void handler() { 
/*TODO*///		SYS16_JOY1
/*TODO*///		SYS16_JOY2
/*TODO*///		SYS16_SERVICE
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Cabinet") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Upright") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
/*TODO*///		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Lives") );
/*TODO*///		PORT_DIPSETTING(    0x00, "2" );
/*TODO*///		PORT_DIPSETTING(    0x0c, "3" );
/*TODO*///		PORT_DIPSETTING(    0x08, "4" );
/*TODO*///		PORT_DIPSETTING(    0x04, "5" );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Bonus_Life") );
/*TODO*///		PORT_DIPSETTING(    0x10, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hard" );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x20, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hard" );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "Controller select" );
/*TODO*///		PORT_DIPSETTING(    0x40, "1 Player side" );
/*TODO*///		PORT_DIPSETTING(    0x00, "2 Players side" );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_7759( machine_driver_aurail, \
/*TODO*///		aurail_readmem,aurail_writemem,aurail_init_machine, gfx4,upd7759_interface )
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	// sys16B
/*TODO*///	static RomLoadPtr rom_bayroute = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0xc0000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "br.4a", 0x000000, 0x10000, 0x91c6424b )
/*TODO*///		ROM_LOAD_ODD ( "br.1a", 0x000000, 0x10000, 0x76954bf3 )
/*TODO*///		/* empty 0x20000-0x80000*/
/*TODO*///		ROM_LOAD_EVEN( "br.5a", 0x080000, 0x10000, 0x9d6fd183 )
/*TODO*///		ROM_LOAD_ODD ( "br.2a", 0x080000, 0x10000, 0x5ca1e3d2 )
/*TODO*///		ROM_LOAD_EVEN( "br.6a", 0x0a0000, 0x10000, 0xed97ad4c )
/*TODO*///		ROM_LOAD_ODD ( "br.3a", 0x0a0000, 0x10000, 0x0d362905 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "opr12462.a14", 0x00000, 0x10000, 0xa19943b5 );
/*TODO*///		ROM_LOAD( "opr12463.a15", 0x10000, 0x10000, 0x62f8200d );
/*TODO*///		ROM_LOAD( "opr12464.a16", 0x20000, 0x10000, 0xc8c59703 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x080000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "br_obj0o.1b", 0x00000, 0x10000, 0x098a5e82 );
/*TODO*///		ROM_LOAD( "br_obj0e.5b", 0x10000, 0x10000, 0x85238af9 );
/*TODO*///		ROM_LOAD( "br_obj1o.2b", 0x20000, 0x10000, 0xcc641da1 );
/*TODO*///		ROM_LOAD( "br_obj1e.6b", 0x30000, 0x10000, 0xd3123315 );
/*TODO*///		ROM_LOAD( "br_obj2o.3b", 0x40000, 0x10000, 0x84efac1f );
/*TODO*///		ROM_LOAD( "br_obj2e.7b", 0x50000, 0x10000, 0xb73b12cb );
/*TODO*///		ROM_LOAD( "br_obj3o.4b", 0x60000, 0x10000, 0xa2e238ac );
/*TODO*///		ROM_LOAD( "br.8b",		 0x70000, 0x10000, 0xd8de78ff );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr12459.a10", 0x00000, 0x08000, 0x3e1d29d0 );
/*TODO*///		ROM_LOAD( "mpr12460.a11", 0x10000, 0x20000, 0x0bae570d );
/*TODO*///		ROM_LOAD( "mpr12461.a12", 0x30000, 0x20000, 0xb03b8b46 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_bayrouta = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0xc0000, REGION_CPU1 );/* 68000 code */
/*TODO*///	// custom cpu 317-0116
/*TODO*///		ROM_LOAD_EVEN( "epr12517.a7", 0x000000, 0x20000, 0x436728a9 )
/*TODO*///		ROM_LOAD_ODD ( "epr12516.a5", 0x000000, 0x20000, 0x4ff0353f )
/*TODO*///		/* empty 0x40000-0x80000*/
/*TODO*///		ROM_LOAD_EVEN( "epr12458.a8", 0x080000, 0x20000, 0xe7c7476a )
/*TODO*///		ROM_LOAD_ODD ( "epr12456.a6", 0x080000, 0x20000, 0x25dc2eaf )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "opr12462.a14", 0x00000, 0x10000, 0xa19943b5 );
/*TODO*///		ROM_LOAD( "opr12463.a15", 0x10000, 0x10000, 0x62f8200d );
/*TODO*///		ROM_LOAD( "opr12464.a16", 0x20000, 0x10000, 0xc8c59703 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x080000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "mpr12465.b1", 0x00000, 0x20000, 0x11d61b45 );
/*TODO*///		ROM_LOAD( "mpr12467.b5", 0x20000, 0x20000, 0xc3b4e4c0 );
/*TODO*///		ROM_LOAD( "mpr12466.b2", 0x40000, 0x20000, 0xa57f236f );
/*TODO*///		ROM_LOAD( "mpr12468.b6", 0x60000, 0x20000, 0xd89c77de );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr12459.a10", 0x00000, 0x08000, 0x3e1d29d0 );
/*TODO*///		ROM_LOAD( "mpr12460.a11", 0x10000, 0x20000, 0x0bae570d );
/*TODO*///		ROM_LOAD( "mpr12461.a12", 0x30000, 0x20000, 0xb03b8b46 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_bayrtbl1 = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0xc0000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "b4.bin", 0x000000, 0x10000, 0xeb6646ae )
/*TODO*///		ROM_LOAD_ODD ( "b2.bin", 0x000000, 0x10000, 0xecd9cd0e )
/*TODO*///		/* empty 0x20000-0x80000*/
/*TODO*///		ROM_LOAD_EVEN( "br.5a",  0x080000, 0x10000, 0x9d6fd183 )
/*TODO*///		ROM_LOAD_ODD ( "br.2a",  0x080000, 0x10000, 0x5ca1e3d2 )
/*TODO*///		ROM_LOAD_EVEN( "b8.bin", 0x0a0000, 0x10000, 0xe7ca0331 )
/*TODO*///		ROM_LOAD_ODD ( "b6.bin", 0x0a0000, 0x10000, 0x2bc748a6 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "bs16.bin", 0x00000, 0x10000, 0xa8a5b310 );
/*TODO*///		ROM_LOAD( "bs14.bin", 0x10000, 0x10000, 0x6bc4d0a8 );
/*TODO*///		ROM_LOAD( "bs12.bin", 0x20000, 0x10000, 0xc1f967a6 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x080000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "br_obj0o.1b", 0x00000, 0x10000, 0x098a5e82 );
/*TODO*///		ROM_LOAD( "br_obj0e.5b", 0x10000, 0x10000, 0x85238af9 );
/*TODO*///		ROM_LOAD( "br_obj1o.2b", 0x20000, 0x10000, 0xcc641da1 );
/*TODO*///		ROM_LOAD( "br_obj1e.6b", 0x30000, 0x10000, 0xd3123315 );
/*TODO*///		ROM_LOAD( "br_obj2o.3b", 0x40000, 0x10000, 0x84efac1f );
/*TODO*///		ROM_LOAD( "br_obj2e.7b", 0x50000, 0x10000, 0xb73b12cb );
/*TODO*///		ROM_LOAD( "br_obj3o.4b", 0x60000, 0x10000, 0xa2e238ac );
/*TODO*///		ROM_LOAD( "bs7.bin",     0x70000, 0x10000, 0x0c91abcc );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr12459.a10", 0x00000, 0x08000, 0x3e1d29d0 );
/*TODO*///		ROM_LOAD( "mpr12460.a11", 0x10000, 0x20000, 0x0bae570d );
/*TODO*///		ROM_LOAD( "mpr12461.a12", 0x30000, 0x20000, 0xb03b8b46 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_bayrtbl2 = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0xc0000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "br_04", 0x000000, 0x10000, 0x2e33ebfc )
/*TODO*///		ROM_LOAD_ODD ( "br_06", 0x000000, 0x10000, 0x3db42313 )
/*TODO*///		/* empty 0x20000-0x80000*/
/*TODO*///		ROM_LOAD_EVEN( "br_03", 0x080000, 0x20000, 0x285d256b )
/*TODO*///		ROM_LOAD_ODD ( "br_05", 0x080000, 0x20000, 0x552e6384 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "br_15",    0x00000, 0x10000, 0x050079a9 );
/*TODO*///		ROM_LOAD( "br_16",    0x10000, 0x10000, 0xfc371928 );
/*TODO*///		ROM_LOAD( "bs12.bin", 0x20000, 0x10000, 0xc1f967a6 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x080000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "br_11",       0x00000, 0x10000, 0x65232905 );
/*TODO*///		ROM_LOAD( "br_obj0e.5b", 0x10000, 0x10000, 0x85238af9 );
/*TODO*///		ROM_LOAD( "br_obj1o.2b", 0x20000, 0x10000, 0xcc641da1 );
/*TODO*///		ROM_LOAD( "br_obj1e.6b", 0x30000, 0x10000, 0xd3123315 );
/*TODO*///		ROM_LOAD( "br_obj2o.3b", 0x40000, 0x10000, 0x84efac1f );
/*TODO*///		ROM_LOAD( "br_09",       0x50000, 0x10000, 0x05e9b840 );
/*TODO*///		ROM_LOAD( "br_14",       0x60000, 0x10000, 0x4c4a177b );
/*TODO*///		ROM_LOAD( "bs7.bin",     0x70000, 0x10000, 0x0c91abcc );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "br_01", 0x00000, 0x10000, 0xb87156ec );
/*TODO*///		ROM_LOAD( "br_02", 0x10000, 0x10000, 0xef63991b );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static MemoryReadAddress bayroute_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x0bffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x500000, 0x503fff, MRA_EXTRAM3 ),
/*TODO*///		new MemoryReadAddress( 0x600000, 0x600fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x700000, 0x70ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x710000, 0x710fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x800000, 0x800fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0x901002, 0x901003, input_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0x901006, 0x901007, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0x901000, 0x901001, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0x902002, 0x902003, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0x902000, 0x902001, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0x900000, 0x900fff, MRA_EXTRAM2 ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress bayroute_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x0bffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x500000, 0x503fff, MWA_EXTRAM3 ),
/*TODO*///		new MemoryWriteAddress( 0x600000, 0x600fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x700000, 0x70ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x710000, 0x710fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x800000, 0x800fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0x900000, 0x900fff, MWA_BANK4,sys16_extraram2 ),
/*TODO*///		new MemoryWriteAddress( 0xff0006, 0xff0007, sound_command_w ),
/*TODO*///	
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void bayroute_update_proc( void ){
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0e98] );
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0e9a] );
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0e90] );
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0e92] );
/*TODO*///	
/*TODO*///		set_fg_page( READ_WORD( &sys16_textram[0x0e80] ) );
/*TODO*///		set_bg_page( READ_WORD( &sys16_textram[0x0e82] ) );
/*TODO*///	
/*TODO*///		set_refresh( READ_WORD( &sys16_extraram2[0x0] ) );
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr bayroute_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = { 0,0,0,0,0,0,0,6,0,0,0,4,0,2,0,0 };
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_update_proc = bayroute_update_proc;
/*TODO*///		sys16_spritesystem = 4;
/*TODO*///		sys16_spritelist_end=0xc000;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_bayroute = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_sprite_decode( 4,0x20000 );
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_bayrouta = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_sprite_decode( 2,0x40000 );
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_bayrtbl1 = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///	
/*TODO*///		/* invert the graphics bits on the tiles */
/*TODO*///		for (i = 0; i < 0x30000; i++)
/*TODO*///			memory_region(REGION_GFX1)[i] ^= 0xff;
/*TODO*///	
/*TODO*///		sys16_sprite_decode( 4,0x20000 );
/*TODO*///	} };
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_bayroute = new InputPortPtr(){ public void handler() { 
/*TODO*///		SYS16_JOY1
/*TODO*///		SYS16_JOY2
/*TODO*///		SYS16_SERVICE
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 
/*TODO*///		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Lives") );
/*TODO*///		PORT_DIPSETTING(    0x04, "1" );
/*TODO*///		PORT_DIPSETTING(    0x0c, "3" );
/*TODO*///		PORT_DIPSETTING(    0x08, "5" );
/*TODO*///		PORT_BITX( 0,       0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Unlimited", IP_KEY_NONE, IP_JOY_NONE );
/*TODO*///		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Bonus_Life") );
/*TODO*///		PORT_DIPSETTING(    0x30, "10000" );
/*TODO*///		PORT_DIPSETTING(    0x20, "15000" );
/*TODO*///		PORT_DIPSETTING(    0x10, "20000" );
/*TODO*///		PORT_DIPSETTING(    0x00, "None" );
/*TODO*///		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0xc0, "A" );
/*TODO*///		PORT_DIPSETTING(    0x80, "B" );
/*TODO*///		PORT_DIPSETTING(    0x40, "C" );
/*TODO*///		PORT_DIPSETTING(    0x00, "D" );
/*TODO*///	
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_7759( machine_driver_bayroute, \
/*TODO*///		bayroute_readmem,bayroute_writemem,bayroute_init_machine, gfx1,upd7759_interface )
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///	
/*TODO*///	   Body Slam
/*TODO*///	
/*TODO*///	***************************************************************************/
/*TODO*///	// pre16
/*TODO*///	static RomLoadPtr rom_bodyslam = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "epr10066.b9", 0x000000, 0x8000, 0x6cd53290 )
/*TODO*///		ROM_LOAD_ODD ( "epr10063.b6", 0x000000, 0x8000, 0xdd849a16 )
/*TODO*///		ROM_LOAD_EVEN( "epr10067.b10",0x010000, 0x8000, 0xdb22a5ce )
/*TODO*///		ROM_LOAD_ODD ( "epr10064.b7", 0x010000, 0x8000, 0x53d6b7e0 )
/*TODO*///		ROM_LOAD_EVEN( "epr10068.b11",0x020000, 0x8000, 0x15ccc665 )
/*TODO*///		ROM_LOAD_ODD ( "epr10065.b8", 0x020000, 0x8000, 0x0e5fa314 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "epr10321.c9",  0x00000, 0x8000, 0xcd3e7cba );/* plane 1 */
/*TODO*///		ROM_LOAD( "epr10322.c10", 0x08000, 0x8000, 0xb53d3217 );/* plane 2 */
/*TODO*///		ROM_LOAD( "epr10323.c11", 0x10000, 0x8000, 0x915a3e61 );/* plane 3 */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "epr10012.c5",  0x000000, 0x08000, 0x990824e8 );
/*TODO*///		ROM_RELOAD(               0x040000, 0x08000 );
/*TODO*///		ROM_LOAD( "epr10016.b2",  0x008000, 0x08000, 0xaf5dc72f );
/*TODO*///		ROM_RELOAD(               0x048000, 0x08000 );
/*TODO*///		ROM_LOAD( "epr10013.c6",  0x010000, 0x08000, 0x9a0919c5 );
/*TODO*///		ROM_LOAD( "epr10017.b3",  0x018000, 0x08000, 0x62aafd95 );
/*TODO*///		ROM_LOAD( "epr10027.c7",  0x020000, 0x08000, 0x3f1c57c7 );
/*TODO*///		ROM_LOAD( "epr10028.b4",  0x028000, 0x08000, 0x80d4946d );
/*TODO*///		ROM_LOAD( "epr10015.c8",  0x030000, 0x08000, 0x582d3b6a );
/*TODO*///		ROM_LOAD( "epr10019.b5",  0x038000, 0x08000, 0xe020c38b );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr10026.b1", 0x00000, 0x8000, 0x123b69b8 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x1000, REGION_CPU3 );     /* 4k for 7751 onboard ROM */
/*TODO*///		ROM_LOAD( "7751.bin",     0x0000, 0x0400, 0x6a9534fc );/* 7751 - U34 */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_SOUND1 );/* 7751 sound data */
/*TODO*///		ROM_LOAD( "epr10029.c1", 0x00000, 0x8000, 0x7e4aca83 );
/*TODO*///		ROM_LOAD( "epr10030.c2", 0x08000, 0x8000, 0xdcc1df0b );
/*TODO*///		ROM_LOAD( "epr10031.c3", 0x10000, 0x8000, 0xea3c4472 );
/*TODO*///		ROM_LOAD( "epr10032.c4", 0x18000, 0x8000, 0x0aabebce );
/*TODO*///	
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_dumpmtmt = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "7704a.bin", 0x000000, 0x8000, 0x96de6c7b )
/*TODO*///		ROM_LOAD_ODD ( "7701a.bin", 0x000000, 0x8000, 0x786d1009 )
/*TODO*///		ROM_LOAD_EVEN( "7705a.bin", 0x010000, 0x8000, 0xfc584391 )
/*TODO*///		ROM_LOAD_ODD ( "7702a.bin", 0x010000, 0x8000, 0x2241a8fd )
/*TODO*///		ROM_LOAD_EVEN( "7706a.bin", 0x020000, 0x8000, 0x6bbcc9d0 )
/*TODO*///		ROM_LOAD_ODD ( "7703a.bin", 0x020000, 0x8000, 0xfcb0cd40 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "7707a.bin",  0x00000, 0x8000, 0x45318738 );/* plane 1 */
/*TODO*///		ROM_LOAD( "7708a.bin",  0x08000, 0x8000, 0x411be9a4 );/* plane 2 */
/*TODO*///		ROM_LOAD( "7709a.bin",  0x10000, 0x8000, 0x74ceb5a8 );/* plane 3 */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "7715.bin",  0x000000, 0x08000, 0xbf47e040 );
/*TODO*///		ROM_RELOAD(               0x040000, 0x08000 );
/*TODO*///		ROM_LOAD( "7719.bin",  0x008000, 0x08000, 0xfa5c5d6c );
/*TODO*///		ROM_RELOAD(               0x048000, 0x08000 );
/*TODO*///		ROM_LOAD( "epr10013.c6",  0x010000, 0x08000, 0x9a0919c5 );/* 7716 */
/*TODO*///		ROM_LOAD( "epr10017.b3",  0x018000, 0x08000, 0x62aafd95 );/* 7720 */
/*TODO*///		ROM_LOAD( "7717.bin",  0x020000, 0x08000, 0xfa64c86d );
/*TODO*///		ROM_LOAD( "7721.bin",  0x028000, 0x08000, 0x62a9143e );
/*TODO*///		ROM_LOAD( "epr10015.c8",  0x030000, 0x08000, 0x582d3b6a );/* 7718 */
/*TODO*///		ROM_LOAD( "epr10019.b5",  0x038000, 0x08000, 0xe020c38b );/* 7722 */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "7710a.bin", 0x00000, 0x8000, 0xa19b8ba8 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x1000, REGION_CPU3 );     /* 4k for 7751 onboard ROM */
/*TODO*///		ROM_LOAD( "7751.bin",     0x0000, 0x0400, 0x6a9534fc );/* 7751 - U34 */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_SOUND1 );/* 7751 sound data */
/*TODO*///		ROM_LOAD( "7711.bin", 0x00000, 0x8000, 0xefa9aabd );
/*TODO*///		ROM_LOAD( "7712.bin", 0x08000, 0x8000, 0x7bcd85cf );
/*TODO*///		ROM_LOAD( "7713.bin", 0x10000, 0x8000, 0x33f292e7 );
/*TODO*///		ROM_LOAD( "7714.bin", 0x18000, 0x8000, 0x8fd48c47 );
/*TODO*///	
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static MemoryReadAddress bodyslam_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x02ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x440000, 0x440fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x840000, 0x840fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0xc41002, 0xc41003, input_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xc41006, 0xc41007, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0xc41000, 0xc41001, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xc42000, 0xc42001, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xc42002, 0xc42003, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xc40000, 0xc400ff, MRA_EXTRAM2 ),
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress bodyslam_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x02ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x440000, 0x440fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x840000, 0x840fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc40001, sound_command_nmi_w ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc400ff, MWA_BANK4,sys16_extraram2 ),
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void bodyslam_update_proc (void)
/*TODO*///	{
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0ffa] ) & 0x01ff;
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0ff8] ) & 0x01ff;
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0f26] ) & 0x00ff;
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0f24] ) & 0x01ff;
/*TODO*///	
/*TODO*///		set_fg_page1( READ_WORD( &sys16_textram[0x0e9e] ) );
/*TODO*///		set_bg_page1( READ_WORD( &sys16_textram[0x0e9c] ) );
/*TODO*///	
/*TODO*///		set_refresh_3d( READ_WORD( &sys16_extraram2[2] ) );
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	public static InitMachinePtr bodyslam_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = {0,1,2,3,0,1,2,3,0,1,2,3,0,1,2,3};
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_textmode=1;
/*TODO*///		sys16_spritesystem = 2;
/*TODO*///		sys16_sprxoffset = -0xbc;
/*TODO*///		sys16_fgxoffset = sys16_bgxoffset = 7;
/*TODO*///		sys16_bg_priority_mode = 2;
/*TODO*///		sys16_bg_priority_value=0x0e00;
/*TODO*///	
/*TODO*///		sys16_textlayer_lo_min=0;
/*TODO*///		sys16_textlayer_lo_max=0x1f;
/*TODO*///		sys16_textlayer_hi_min=0x20;
/*TODO*///		sys16_textlayer_hi_max=0xff;
/*TODO*///	
/*TODO*///		sys16_update_proc = bodyslam_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static void bodyslam_sprite_decode (void)
/*TODO*///	{
/*TODO*///		sys16_sprite_decode (5,0x10000);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	// I have no idea if this is needed, but I cannot find any code for the countdown
/*TODO*///	// timer in the code and this seems to work ok.
/*TODO*///	static void bodyslam_irq_timer(void)
/*TODO*///	{
/*TODO*///		int flag=READ_WORD(&sys16_workingram[0x200])>>8;
/*TODO*///		int tick=READ_WORD(&sys16_workingram[0x200])&0xff;
/*TODO*///		int sec=READ_WORD(&sys16_workingram[0x202])>>8;
/*TODO*///		int min=READ_WORD(&sys16_workingram[0x202])&0xff;
/*TODO*///	
/*TODO*///		if(tick == 0 && sec == 0 && min == 0)
/*TODO*///			flag=1;
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if(tick==0)
/*TODO*///			{
/*TODO*///				tick=0x40;	// The game initialise this to 0x40
/*TODO*///				if(sec==0)
/*TODO*///				{
/*TODO*///					sec=0x59;
/*TODO*///					if(min==0)
/*TODO*///					{
/*TODO*///						flag=1;
/*TODO*///						tick=sec=min=0;
/*TODO*///					}
/*TODO*///					else
/*TODO*///						min--;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if((sec&0xf)==0)
/*TODO*///					{
/*TODO*///						sec-=0x10;
/*TODO*///						sec|=9;
/*TODO*///					}
/*TODO*///					else
/*TODO*///						sec--;
/*TODO*///	
/*TODO*///				}
/*TODO*///			}
/*TODO*///			else
/*TODO*///				tick--;
/*TODO*///		}
/*TODO*///		WRITE_WORD(&sys16_workingram[0x200],(flag<<8)+tick);
/*TODO*///		WRITE_WORD(&sys16_workingram[0x202],(sec<<8)+min);
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_bodyslam = new InitDriverPtr() { public void handler() {
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_bg1_trans=1;
/*TODO*///		sys16_custom_irq=bodyslam_irq_timer;
/*TODO*///		bodyslam_sprite_decode();
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_bodyslam = new InputPortPtr(){ public void handler() { 
/*TODO*///		SYS16_JOY1
/*TODO*///		SYS16_JOY2
/*TODO*///		SYS16_SERVICE
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_7751( machine_driver_bodyslam, \
/*TODO*///		bodyslam_readmem,bodyslam_writemem,bodyslam_init_machine, gfx8 )
/*TODO*///	
	/***************************************************************************/
	// sys16B
	static RomLoadPtr rom_dduxbl = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x0c0000, REGION_CPU1 );/* 68000 code */
		ROM_LOAD_EVEN( "dduxb03.bin", 0x000000, 0x20000, 0xe7526012 );
		ROM_LOAD_ODD ( "dduxb05.bin", 0x000000, 0x20000, 0x459d1237 );
		/* empty 0x40000 - 0x80000 */
		ROM_LOAD_EVEN( "dduxb02.bin", 0x080000, 0x20000, 0xd8ed3132 );
		ROM_LOAD_ODD ( "dduxb04.bin", 0x080000, 0x20000, 0x30c6cb92 );
	
		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
		ROM_LOAD( "dduxb14.bin", 0x00000, 0x10000, 0x664bd135 );
		ROM_LOAD( "dduxb15.bin", 0x10000, 0x10000, 0xce0d2b30 );
		ROM_LOAD( "dduxb16.bin", 0x20000, 0x10000, 0x6de95434 );
	
		ROM_REGION( 0x080000*2, REGION_GFX2 );/* sprites */
		ROM_LOAD( "dduxb10.bin", 0x000000, 0x010000, 0x0be3aee5 );
		ROM_LOAD( "dduxb06.bin", 0x010000, 0x010000, 0xb0079e99 );
		ROM_LOAD( "dduxb11.bin", 0x020000, 0x010000, 0xcfb2af18 );
		ROM_LOAD( "dduxb07.bin", 0x030000, 0x010000, 0x0217369c );
		ROM_LOAD( "dduxb12.bin", 0x040000, 0x010000, 0x28ce9b15 );
		ROM_LOAD( "dduxb08.bin", 0x050000, 0x010000, 0x8844f336 );
		ROM_LOAD( "dduxb13.bin", 0x060000, 0x010000, 0xefe57759 );
		ROM_LOAD( "dduxb09.bin", 0x070000, 0x010000, 0x6b64f665 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* sound CPU */
		ROM_LOAD( "dduxb01.bin", 0x0000, 0x8000, 0x0dbef0d7 );
	ROM_END(); }}; 
	
	/***************************************************************************/
	public static ReadHandlerPtr dduxbl_skip = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (cpu_get_pc()==0x502) {cpu_spinuntil_int(); return 0xffff;}
	
		return sys16_workingram.READ_WORD(0x36e0);
	} };
	
	static MemoryReadAddress dduxbl_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x0bffff, MRA_ROM ),
		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
		new MemoryReadAddress( 0x440000, 0x440fff, MRA_BANK2 ),
		new MemoryReadAddress( 0x840000, 0x840fff, paletteram_word_r ),
		new MemoryReadAddress( 0xc41002, 0xc41003, input_port_0_r ),
		new MemoryReadAddress( 0xc41004, 0xc41005, input_port_1_r ),
		new MemoryReadAddress( 0xc41000, 0xc41001, input_port_2_r ),
		new MemoryReadAddress( 0xc42002, 0xc42003, input_port_3_r ),
		new MemoryReadAddress( 0xc42000, 0xc42001, input_port_4_r ),
		new MemoryReadAddress( 0xfff6e0, 0xfff6e1, dduxbl_skip ),
		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
		new MemoryReadAddress(-1)
	};
	
	static MemoryWriteAddress dduxbl_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x0bffff, MWA_ROM ),
		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
		new MemoryWriteAddress( 0x440000, 0x440fff, MWA_BANK2,sys16_spriteram ),
		new MemoryWriteAddress( 0x840000, 0x840fff, sys16_paletteram_w, paletteram ),
		new MemoryWriteAddress( 0xc40006, 0xc40007, sound_command_w ),
		new MemoryWriteAddress( 0xc40000, 0xc4ffff, MWA_BANK4,sys16_extraram2 ),
		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
		new MemoryWriteAddress(-1)
	};
	/***************************************************************************/
	public static sys16_update_procPtr dduxbl_update_proc = new sys16_update_procPtr() {
            public void handler() {
		sys16_fg_scrollx = (sys16_extraram2.READ_WORD(0x6018 ) ^ 0xffff) & 0x01ff;
		sys16_bg_scrollx = (sys16_extraram2.READ_WORD(0x6008 ) ^ 0xffff) & 0x01ff;
		sys16_fg_scrolly = sys16_extraram2.READ_WORD(0x6010 ) & 0x00ff;
		sys16_bg_scrolly = sys16_extraram2.READ_WORD(0x6000 );
	
		{
			 char lu = (char)(sys16_extraram2.READ_WORD(0x6020 ) & 0xff);
			 char ru = (char)(sys16_extraram2.READ_WORD(0x6022 ) & 0xff);
			 char ld = (char)(sys16_extraram2.READ_WORD(0x6024 ) & 0xff);
			 char rd = (char)(sys16_extraram2.READ_WORD(0x6026 ) & 0xff);
	
			if (lu==4 && ld==4 && ru==5 && rd==5)
			{ // fix a bug in chicago round (un-tested in MAME)
				int vs=sys16_workingram.READ_WORD(0x36ec);
				sys16_bg_scrolly = vs & 0xff;
				sys16_fg_scrolly = vs & 0xff;
				if (vs >= 0x100)
				{
					lu=0x26; ru=0x37;
					ld=0x04; rd=0x15;
				} else {
					ld=0x26; rd=0x37;
					lu=0x04; ru=0x15;
				}
			}
			sys16_fg_page[0] = ld&0xf;
			sys16_fg_page[1] = rd&0xf;
			sys16_fg_page[2] = lu&0xf;
			sys16_fg_page[3] = ru&0xf;
	
			sys16_bg_page[0] = ld>>4;
			sys16_bg_page[1] = rd>>4;
			sys16_bg_page[2] = lu>>4;
			sys16_bg_page[3] = ru>>4;
		}
	
		set_refresh( sys16_extraram2.READ_WORD(0 ) );
	}};
	
	public static InitMachinePtr dduxbl_init_machine = new InitMachinePtr() { public void handler() {
		int bank[] = {00,00,00,00,00,00,00,0x06,00,00,00,0x04,00,0x02,00,00};
	
		sys16_obj_bank = bank;
	
		patch_code.handler( 0x1eb2e, 0x01 );
		patch_code.handler( 0x1eb2f, 0x01 );
		patch_code.handler( 0x1eb3c, 0x00 );
		patch_code.handler( 0x1eb3d, 0x00 );
		patch_code.handler( 0x23132, 0x01 );
		patch_code.handler( 0x23133, 0x01 );
		patch_code.handler( 0x23140, 0x00 );
		patch_code.handler( 0x23141, 0x00 );
		patch_code.handler( 0x24a9a, 0x01 );
		patch_code.handler( 0x24a9b, 0x01 );
		patch_code.handler( 0x24aa8, 0x00 );
		patch_code.handler( 0x24aa9, 0x00 );
	
		sys16_update_proc = dduxbl_update_proc;
		sys16_sprxoffset = -0x48;
	} };
	
	public static InitDriverPtr init_dduxbl = new InitDriverPtr() { public void handler() 
	{
		int i;
	
		sys16_onetime_init_machine.handler();
	
		/* invert the graphics bits on the tiles */
		for (i = 0; i < 0x30000; i++)
			memory_region(REGION_GFX1).write(i,memory_region(REGION_GFX1).read(i) ^ 0xff);
	
		sys16_sprite_decode( 4,0x020000 );
	} };
	/***************************************************************************/
	
	static InputPortPtr input_ports_dduxbl = new InputPortPtr(){ public void handler() { 
        PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x07, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x09, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x05, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x04, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x02, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0x03, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x06, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x00, "Free Play (if Coin B too) or 1/1");
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x70, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x90, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x50, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x40, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x10, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x20, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0x30, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x60, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0xa0, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x00, "Free Play (if Coin A too) or 1/1");
	
	PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x04, "Easy" );
		PORT_DIPSETTING(    0x06, "Normal" );
		PORT_DIPSETTING(    0x02, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x10, "2" );
		PORT_DIPSETTING(    0x18, "3" );
		PORT_DIPSETTING(    0x08, "4" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x40, "150000" );
		PORT_DIPSETTING(    0x60, "200000" );
		PORT_DIPSETTING(    0x20, "300000" );
		PORT_DIPSETTING(    0x00, "400000" );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
	INPUT_PORTS_END(); }}; 
	
	/***************************************************************************/

            static MachineDriver machine_driver_dduxbl = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000,
                        dduxbl_readmem,dduxbl_writemem, null, null,
                        sys16_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4096000,
                        sound_readmem, sound_writemem, sound_readport, sound_writeport,
                        ignore_interrupt, 1
                ),},
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1,
            dduxbl_init_machine,
            40 * 8, 28 * 8, new rectangle(0 * 8, 40 * 8 - 1, 0 * 8, 28 * 8 - 1),
            gfx1,
            2048 * ShadowColorsMultiplier, 2048 * ShadowColorsMultiplier,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            sys16_vh_start,
            sys16_vh_stop,
            sys16_vh_screenrefresh,
            SOUND_SUPPORTS_STEREO, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface
                )
            }
    );
/*TODO*///	/***************************************************************************/
/*TODO*///	// sys16B
/*TODO*///	static RomLoadPtr rom_eswat = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x080000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "12657", 0x000000, 0x40000, 0xcfb935e9 )
/*TODO*///		ROM_LOAD_ODD ( "12656", 0x000000, 0x40000, 0xbe3f9d28 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "e12624r", 0x00000, 0x40000, 0xe7b8545e );
/*TODO*///		ROM_LOAD( "e12625r", 0x40000, 0x40000, 0xb418582c );
/*TODO*///		ROM_LOAD( "e12626r", 0x80000, 0x40000, 0xba65789b );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x180000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "e12618r", 0x000000, 0x040000, 0x2d9ae975 );
/*TODO*///		ROM_LOAD( "e12621r", 0x040000, 0x040000, 0x1e6c4cf7 );
/*TODO*///		ROM_LOAD( "e12619r", 0x080000, 0x040000, 0x5f7ee6f6 );
/*TODO*///		ROM_LOAD( "e12622r", 0x0c0000, 0x040000, 0x33251fde );
/*TODO*///		ROM_LOAD( "e12620r", 0x100000, 0x040000, 0x905f9be2 );
/*TODO*///		ROM_LOAD( "e12623r", 0x140000, 0x040000, 0xa25ea1fc );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "e12617", 0x00000, 0x08000, 0x537930cb );
/*TODO*///		ROM_LOAD( "e12616r",0x10000, 0x20000, 0xf213fa4a );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_eswatbl = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x080000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "eswat_c.rom", 0x000000, 0x10000, 0x1028cc81 )
/*TODO*///		ROM_LOAD_ODD ( "eswat_f.rom", 0x000000, 0x10000, 0xf7b2d388 )
/*TODO*///		ROM_LOAD_EVEN( "eswat_b.rom", 0x020000, 0x10000, 0x87c6b1b5 )
/*TODO*///		ROM_LOAD_ODD ( "eswat_e.rom", 0x020000, 0x10000, 0x937ddf9a )
/*TODO*///		ROM_LOAD_EVEN( "eswat_a.rom", 0x040000, 0x08000, 0x2af4fc62 )
/*TODO*///		ROM_LOAD_ODD ( "eswat_d.rom", 0x040000, 0x08000, 0xb4751e19 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "ic19.bin", 0x00000, 0x40000, 0x375a5ec4 );
/*TODO*///		ROM_LOAD( "ic20.bin", 0x40000, 0x40000, 0x3b8c757e );
/*TODO*///		ROM_LOAD( "ic21.bin", 0x80000, 0x40000, 0x3efca25c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x180000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "ic9.bin",  0x000000, 0x040000, 0x0d1530bf );
/*TODO*///		ROM_LOAD( "ic12.bin", 0x040000, 0x040000, 0x18ff0799 );
/*TODO*///		ROM_LOAD( "ic10.bin", 0x080000, 0x040000, 0x32069246 );
/*TODO*///		ROM_LOAD( "ic13.bin", 0x0c0000, 0x040000, 0xa3dfe436 );
/*TODO*///		ROM_LOAD( "ic11.bin", 0x100000, 0x040000, 0xf6b096e0 );
/*TODO*///		ROM_LOAD( "ic14.bin", 0x140000, 0x040000, 0x6773fef6 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "ic8.bin", 0x0000, 0x8000, 0x7efecf23 );
/*TODO*///		ROM_LOAD( "ic6.bin", 0x10000, 0x40000, 0x254347c2 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr eswatbl_skip = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		if (cpu_get_pc()==0x65c) {cpu_spinuntil_int(); return 0xffff;}
/*TODO*///	
/*TODO*///		return READ_WORD(&sys16_workingram[0x0454]);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static MemoryReadAddress eswat_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x418fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x440000, 0x440fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x840000, 0x840fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0xc41002, 0xc41003, input_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xc41006, 0xc41007, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0xc41000, 0xc41001, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xc42002, 0xc42003, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xc42000, 0xc42001, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xffc454, 0xffc455, eswatbl_skip ),
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static int eswat_tilebank0;
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr eswat_tilebank0_w = new WriteHandlerPtr() { public void handler(int o, int d)
/*TODO*///	{
/*TODO*///		eswat_tilebank0=d;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static MemoryWriteAddress eswat_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x3e2000, 0x3e2001, eswat_tilebank0_w ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x418fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x440000, 0x440fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x840000, 0x840fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xc42006, 0xc42007, sound_command_w ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc4ffff, MWA_BANK4,sys16_extraram2 ),
/*TODO*///		new MemoryWriteAddress( 0xc80000, 0xc80001, MWA_NOP ),
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void eswat_update_proc( void ){
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x8008] ) ^ 0xffff;
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x8018] ) ^ 0xffff;
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x8000] );
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x8010] );
/*TODO*///	
/*TODO*///		set_fg_page( READ_WORD( &sys16_textram[0x8020] ) );
/*TODO*///		set_bg_page( READ_WORD( &sys16_textram[0x8028] ) );
/*TODO*///		set_refresh( READ_WORD( &sys16_extraram2[0] ) );
/*TODO*///	
/*TODO*///		sys16_tile_bank1 = (READ_WORD( &sys16_textram[0x8030] ))&0xf;
/*TODO*///		sys16_tile_bank0 = eswat_tilebank0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr eswat_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = { 0,2,8,10,16,18,24,26,4,6,12,14,20,22,28,30};
/*TODO*///	
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_sprxoffset = -0x23c;
/*TODO*///	
/*TODO*///		patch_code( 0x3897, 0x11 );
/*TODO*///	
/*TODO*///		sys16_update_proc = eswat_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_eswat = new InitDriverPtr() { public void handler() {
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_rowscroll_scroll=0x8000;
/*TODO*///		sys18_splittab_fg_x=&sys16_textram[0x0f80];
/*TODO*///	
/*TODO*///		sys16_sprite_decode( 3,0x080000 );
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_eswat = new InputPortPtr(){ public void handler() { 
/*TODO*///		SYS16_JOY1
/*TODO*///		SYS16_JOY2
/*TODO*///		SYS16_SERVICE
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x01, "2 Credits to Start" );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x04, 0x04, "Display Flip" );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Time" );
/*TODO*///		PORT_DIPSETTING(    0x08, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hard" );
/*TODO*///		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x20, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x30, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x10, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Lives") );
/*TODO*///		PORT_DIPSETTING(    0x00, "1" );
/*TODO*///		PORT_DIPSETTING(    0x40, "2" );
/*TODO*///		PORT_DIPSETTING(    0xc0, "3" );
/*TODO*///		PORT_DIPSETTING(    0x80, "4" );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_7759( machine_driver_eswat, \
/*TODO*///		eswat_readmem,eswat_writemem,eswat_init_machine, gfx4,upd7759_interface )
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	// sys16A
/*TODO*///	static RomLoadPtr rom_fantzono = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x030000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "7385.43", 0x000000, 0x8000, 0x5cb64450 )
/*TODO*///		ROM_LOAD_ODD ( "7382.26", 0x000000, 0x8000, 0x3fda7416 )
/*TODO*///		ROM_LOAD_EVEN( "7386.42", 0x010000, 0x8000, 0x15810ace )
/*TODO*///		ROM_LOAD_ODD ( "7383.25", 0x010000, 0x8000, 0xa001e10a )
/*TODO*///		ROM_LOAD_EVEN( "7387.41", 0x020000, 0x8000, 0x0acd335d )
/*TODO*///		ROM_LOAD_ODD ( "7384.24", 0x020000, 0x8000, 0xfd909341 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "7388.95", 0x00000, 0x08000, 0x8eb02f6b );
/*TODO*///		ROM_LOAD( "7389.94", 0x08000, 0x08000, 0x2f4f71b8 );
/*TODO*///		ROM_LOAD( "7390.93", 0x10000, 0x08000, 0xd90609c6 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x030000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "7392.10", 0x000000, 0x008000, 0x5bb7c8b6 );
/*TODO*///		ROM_LOAD( "7396.11", 0x008000, 0x008000, 0x74ae4b57 );
/*TODO*///		ROM_LOAD( "7393.17", 0x010000, 0x008000, 0x14fc7e82 );
/*TODO*///		ROM_LOAD( "7397.18", 0x018000, 0x008000, 0xe05a1e25 );
/*TODO*///		ROM_LOAD( "7394.23", 0x020000, 0x008000, 0x531ca13f );
/*TODO*///		ROM_LOAD( "7398.24", 0x028000, 0x008000, 0x68807b49 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "7535.12", 0x0000, 0x8000, 0x0cb2126a );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_fantzone = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x030000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "epr7385a.43", 0x000000, 0x8000, 0x4091af42 )
/*TODO*///		ROM_LOAD_ODD ( "epr7382a.26", 0x000000, 0x8000, 0x77d67bfd )
/*TODO*///		ROM_LOAD_EVEN( "epr7386a.42", 0x010000, 0x8000, 0xb0a67cd0 )
/*TODO*///		ROM_LOAD_ODD ( "epr7383a.25", 0x010000, 0x8000, 0x5f79b2a9 )
/*TODO*///		ROM_LOAD_EVEN( "7387.41", 0x020000, 0x8000, 0x0acd335d )
/*TODO*///		ROM_LOAD_ODD ( "7384.24", 0x020000, 0x8000, 0xfd909341 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "7388.95", 0x00000, 0x08000, 0x8eb02f6b );
/*TODO*///		ROM_LOAD( "7389.94", 0x08000, 0x08000, 0x2f4f71b8 );
/*TODO*///		ROM_LOAD( "7390.93", 0x10000, 0x08000, 0xd90609c6 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x030000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "7392.10", 0x000000, 0x008000, 0x5bb7c8b6 );
/*TODO*///		ROM_LOAD( "7396.11", 0x008000, 0x008000, 0x74ae4b57 );
/*TODO*///		ROM_LOAD( "7393.17", 0x010000, 0x008000, 0x14fc7e82 );
/*TODO*///		ROM_LOAD( "7397.18", 0x018000, 0x008000, 0xe05a1e25 );
/*TODO*///		ROM_LOAD( "7394.23", 0x020000, 0x008000, 0x531ca13f );
/*TODO*///		ROM_LOAD( "7398.24", 0x028000, 0x008000, 0x68807b49 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr7535a.12", 0x0000, 0x8000, 0xbc1374fa );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr fantzone_skip = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		if (cpu_get_pc()==0x91b2) {cpu_spinuntil_int(); return 0xffff;}
/*TODO*///	
/*TODO*///		return READ_WORD(&sys16_workingram[0x022a]);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static MemoryReadAddress fantzono_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x02ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x440000, 0x440fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x840000, 0x840fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0xc41002, 0xc41003, input_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xc41006, 0xc41007, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0xc41000, 0xc41001, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xc42000, 0xc42001, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xc42002, 0xc42003, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xc40000, 0xc40003, MRA_EXTRAM2 ),
/*TODO*///		new MemoryReadAddress( 0xffc22a, 0xffc22b, fantzone_skip ),
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress fantzono_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x02ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x440000, 0x440fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x840000, 0x840fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc40001, sound_command_nmi_w ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc40003, MWA_BANK4,sys16_extraram2 ),
/*TODO*///		new MemoryWriteAddress( 0xc60000, 0xc60003, MWA_NOP ),
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryReadAddress fantzone_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x02ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x440000, 0x440fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x840000, 0x840fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0xc41002, 0xc41003, input_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xc41006, 0xc41007, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0xc41000, 0xc41001, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xc42000, 0xc42001, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xc42002, 0xc42003, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xc40000, 0xc40003, MRA_EXTRAM2 ),
/*TODO*///		new MemoryReadAddress( 0xffc22a, 0xffc22b, fantzone_skip ),
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress fantzone_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x02ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x440000, 0x440fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x840000, 0x840fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc40001, sound_command_nmi_w ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc40003, MWA_BANK4,sys16_extraram2 ),
/*TODO*///		new MemoryWriteAddress( 0xc60000, 0xc60003, MWA_NOP ),
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void fantzone_update_proc( void ){
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0ff8] ) & 0x01ff;
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0ffa] ) & 0x01ff;
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0f24] ) & 0x00ff;
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0f26] ) & 0x01ff;
/*TODO*///	
/*TODO*///		set_fg_page1( READ_WORD( &sys16_textram[0x0e9e] ) );
/*TODO*///		set_bg_page1( READ_WORD( &sys16_textram[0x0e9c] ) );
/*TODO*///	
/*TODO*///		set_refresh_3d( READ_WORD( &sys16_extraram2[2] ) );	// c40003
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr fantzono_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = { 00,01,02,03,00,01,02,03,00,01,02,03,00,01,02,03};
/*TODO*///	
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_textmode=1;
/*TODO*///		sys16_spritesystem = 3;
/*TODO*///		sys16_sprxoffset = -0xbe;
/*TODO*///	//	sys16_fgxoffset = sys16_bgxoffset = 8;
/*TODO*///		sys16_fg_priority_mode=3;				// fixes end of game priority
/*TODO*///		sys16_fg_priority_value=0xd000;
/*TODO*///	
/*TODO*///		patch_code( 0x20e7, 0x16 );
/*TODO*///		patch_code( 0x30ef, 0x16 );
/*TODO*///	
/*TODO*///		// solving Fantasy Zone scrolling bug
/*TODO*///		patch_code(0x308f,0x00);
/*TODO*///	
/*TODO*///		// invincible
/*TODO*///	/*	patch_code(0x224e,0x4e);
/*TODO*///		patch_code(0x224f,0x71);
/*TODO*///		patch_code(0x2250,0x4e);
/*TODO*///		patch_code(0x2251,0x71);
/*TODO*///	
/*TODO*///		patch_code(0x2666,0x4e);
/*TODO*///		patch_code(0x2667,0x71);
/*TODO*///		patch_code(0x2668,0x4e);
/*TODO*///		patch_code(0x2669,0x71);
/*TODO*///	
/*TODO*///		patch_code(0x25c0,0x4e);
/*TODO*///		patch_code(0x25c1,0x71);
/*TODO*///		patch_code(0x25c2,0x4e);
/*TODO*///		patch_code(0x25c3,0x71);
/*TODO*///	*/
/*TODO*///	
/*TODO*///		sys16_update_proc = fantzone_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitMachinePtr fantzone_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = { 00,01,02,03,00,01,02,03,00,01,02,03,00,01,02,03};
/*TODO*///	
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_textmode=1;
/*TODO*///		sys16_spritesystem = 3;
/*TODO*///		sys16_sprxoffset = -0xbe;
/*TODO*///		sys16_fg_priority_mode=3;				// fixes end of game priority
/*TODO*///		sys16_fg_priority_value=0xd000;
/*TODO*///	
/*TODO*///		patch_code( 0x2135, 0x16 );
/*TODO*///		patch_code( 0x3649, 0x16 );
/*TODO*///	
/*TODO*///		// solving Fantasy Zone scrolling bug
/*TODO*///		patch_code(0x35e9,0x00);
/*TODO*///	
/*TODO*///		sys16_update_proc = fantzone_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_fantzone = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_sprite_decode( 3,0x010000 );
/*TODO*///	} };
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_fantzone = new InputPortPtr(){ public void handler() { 
/*TODO*///		SYS16_JOY1
/*TODO*///		SYS16_JOY2
/*TODO*///		SYS16_SERVICE
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Cabinet") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Upright") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
/*TODO*///		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Lives") );
/*TODO*///		PORT_DIPSETTING(    0x08, "2" );
/*TODO*///		PORT_DIPSETTING(    0x0c, "3" );
/*TODO*///		PORT_DIPSETTING(    0x04, "4" );
/*TODO*///		PORT_BITX( 0,       0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "240", IP_KEY_NONE, IP_JOY_NONE );
/*TODO*///		PORT_DIPNAME( 0x30, 0x30, "Extra Ship Cost" );
/*TODO*///		PORT_DIPSETTING(    0x30, "5000" );
/*TODO*///		PORT_DIPSETTING(    0x20, "10000" );
/*TODO*///		PORT_DIPSETTING(    0x10, "15000" );
/*TODO*///		PORT_DIPSETTING(    0x00, "20000" );
/*TODO*///		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x80, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0xc0, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x40, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///	
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER( machine_driver_fantzono, \
/*TODO*///		fantzono_readmem,fantzono_writemem,fantzono_init_machine, gfx8 )
/*TODO*///	MACHINE_DRIVER( machine_driver_fantzone, \
/*TODO*///		fantzone_readmem,fantzone_writemem,fantzone_init_machine, gfx8 )
/*TODO*///	
    /**
     * ************************************************************************
     */
    // sys16B
    static RomLoadPtr rom_fpoint = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x020000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("12591b.bin", 0x000000, 0x10000, 0x248b3e1b);
            ROM_LOAD_ODD("12590b.bin", 0x000000, 0x10000, 0x75256e3d);

            ROM_REGION(0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* tiles */
            ROM_LOAD("12595.bin", 0x00000, 0x10000, 0x5b18d60b);
            ROM_LOAD("12594.bin", 0x10000, 0x10000, 0x8bfc4815);
            ROM_LOAD("12593.bin", 0x20000, 0x10000, 0xcc0582d8);

            ROM_REGION(0x020000 * 2, REGION_GFX2);/* sprites */
            ROM_LOAD("12596.bin", 0x000000, 0x010000, 0x4a4041f3);
            ROM_LOAD("12597.bin", 0x010000, 0x010000, 0x6961e676);

            ROM_REGION(0x10000, REGION_CPU2);/* sound CPU */
            ROM_LOAD("12592.bin", 0x0000, 0x8000, 0x9a8c11bb);
            ROM_END();
        }
    };
    static RomLoadPtr rom_fpointbl = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x020000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("flpoint.003", 0x000000, 0x10000, 0x4d6df514);
            ROM_LOAD_ODD("flpoint.002", 0x000000, 0x10000, 0x4dff2ee8);

            ROM_REGION(0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* tiles */
            ROM_LOAD("flpoint.006", 0x00000, 0x10000, 0xc539727d);
            ROM_LOAD("flpoint.005", 0x10000, 0x10000, 0x82c0b8b0);
            ROM_LOAD("flpoint.004", 0x20000, 0x10000, 0x522426ae);

            ROM_REGION(0x020000 * 2, REGION_GFX2);/* sprites */
            ROM_LOAD("12596.bin", 0x000000, 0x010000, 0x4a4041f3);
            ROM_LOAD("12597.bin", 0x010000, 0x010000, 0x6961e676);

            ROM_REGION(0x10000, REGION_CPU2);/* sound CPU */
            ROM_LOAD("12592.bin", 0x0000, 0x8000, 0x9a8c11bb);// wrong sound rom? (this ones from the original)
            //	ROM_LOAD( "flpoint.001", 0x0000, 0x8000, 0xc5b8e0fe );// bootleg rom doesn't work!
            ROM_END();
        }
    };
    /**
     * ************************************************************************
     */

    public static ReadHandlerPtr fp_io_service_dummy_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int data = input_port_2_r.handler(0) & 0xff;
            return (data << 8) + data;
        }
    };

    static MemoryReadAddress fpoint_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x01ffff, MRA_ROM),
                new MemoryReadAddress(0x02002e, 0x020049, fp_io_service_dummy_r),
                new MemoryReadAddress(0x601002, 0x601003, input_port_0_r),
                new MemoryReadAddress(0x601004, 0x601005, input_port_1_r),
                new MemoryReadAddress(0x601000, 0x601001, input_port_2_r),
                new MemoryReadAddress(0x600000, 0x600001, input_port_3_r),
                new MemoryReadAddress(0x600002, 0x600003, input_port_4_r),
                new MemoryReadAddress(0x400000, 0x40ffff, sys16_tileram_r),
                new MemoryReadAddress(0x410000, 0x410fff, sys16_textram_r),
                new MemoryReadAddress(0x440000, 0x440fff, MRA_BANK2),
                new MemoryReadAddress(0x44302a, 0x44304d, fp_io_service_dummy_r),
                new MemoryReadAddress(0x840000, 0x840fff, paletteram_word_r),
                new MemoryReadAddress(0xfe003e, 0xfe003f, fp_io_service_dummy_r),
                new MemoryReadAddress(0xffc000, 0xffffff, MRA_BANK1),
                new MemoryReadAddress(-1)
            };

    static MemoryWriteAddress fpoint_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x01ffff, MWA_ROM),
                new MemoryWriteAddress(0x600006, 0x600007, sound_command_w),
                new MemoryWriteAddress(0x410000, 0x410fff, sys16_textram_w, sys16_textram),
                new MemoryWriteAddress(0x400000, 0x40ffff, sys16_tileram_w, sys16_tileram),
                new MemoryWriteAddress(0x440000, 0x440fff, MWA_BANK2, sys16_spriteram),
                new MemoryWriteAddress(0x840000, 0x840fff, sys16_paletteram_w, paletteram),
                new MemoryWriteAddress(0xffc000, 0xffffff, MWA_BANK1, sys16_workingram),
                new MemoryWriteAddress(-1)
            };
    /**
     * ************************************************************************
     */
    public static sys16_update_procPtr fpoint_update_proc = new sys16_update_procPtr() {
        public void handler() {
            sys16_fg_scrollx = sys16_textram.READ_WORD(0x0e98);
            sys16_bg_scrollx = sys16_textram.READ_WORD(0x0e9a);
            sys16_fg_scrolly = sys16_textram.READ_WORD(0x0e90);
            sys16_bg_scrolly = sys16_textram.READ_WORD(0x0e92);

            set_fg_page(sys16_textram.READ_WORD(0x0e80));
            set_bg_page(sys16_textram.READ_WORD(0x0e82));
        }
    };

    public static InitMachinePtr fpoint_init_machine = new InitMachinePtr() {
        public void handler() {
            int[] bank = {00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00};

            sys16_obj_bank = bank;

            patch_code.handler(0x454, 0x33);
            patch_code.handler(0x455, 0xf8);
            patch_code.handler(0x456, 0xe0);
            patch_code.handler(0x457, 0xe2);
            patch_code.handler(0x8ce8, 0x16);
            patch_code.handler(0x8ce9, 0x66);
            patch_code.handler(0x17687, 0x00);
            patch_code.handler(0x7bed, 0x04);

            patch_code.handler(0x7ea8, 0x61);
            patch_code.handler(0x7ea9, 0x00);
            patch_code.handler(0x7eaa, 0x84);
            patch_code.handler(0x7eab, 0x16);
            patch_code.handler(0x2c0, 0xe7);
            patch_code.handler(0x2c1, 0x48);
            patch_code.handler(0x2c2, 0xe7);
            patch_code.handler(0x2c3, 0x49);
            patch_code.handler(0x2c4, 0x04);
            patch_code.handler(0x2c5, 0x40);
            patch_code.handler(0x2c6, 0x00);
            patch_code.handler(0x2c7, 0x10);
            patch_code.handler(0x2c8, 0x4e);
            patch_code.handler(0x2c9, 0x75);

            sys16_update_proc = fpoint_update_proc;
        }
    };

    public static InitDriverPtr init_fpoint = new InitDriverPtr() {
        public void handler() {
            sys16_onetime_init_machine.handler();
            sys16_sprite_decode(1, 0x020000);
        }
    };

    public static InitDriverPtr init_fpointbl = new InitDriverPtr() {
        public void handler() {
            int i;

            sys16_onetime_init_machine.handler();

            /* invert the graphics bits on the tiles */
            for (i = 0; i < 0x30000; i++) {
                memory_region(REGION_GFX1).write(i, memory_region(REGION_GFX1).read(i) ^ 0xff);
            }

            sys16_sprite_decode(1, 0x020000);
        }
    };
    /**
     * ************************************************************************
     */

    static InputPortPtr input_ports_fpoint = new InputPortPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x07, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x09, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x05, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x04, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x02, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0x03, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x06, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x00, "Free Play (if Coin B too) or 1/1");
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x70, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x90, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x50, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x40, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x10, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x20, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0x30, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x60, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0xa0, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x00, "Free Play (if Coin A too) or 1/1");

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x20, "Easy");
            PORT_DIPSETTING(0x30, "Normal");
            PORT_DIPSETTING(0x10, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x40, 0x40, "Clear round allowed");/* Use button 3 */
            PORT_DIPSETTING(0x00, "1");
            PORT_DIPSETTING(0x40, "2");
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            INPUT_PORTS_END();
        }
    };

    /**
     * ************************************************************************
     */
    static MachineDriver machine_driver_fpoint = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000,
                        fpoint_readmem, fpoint_writemem, null, null,
                        sys16_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4096000,
                        sound_readmem, sound_writemem, sound_readport, sound_writeport,
                        ignore_interrupt, 1
                ),},
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1,
            fpoint_init_machine,
            40 * 8, 28 * 8, new rectangle(0 * 8, 40 * 8 - 1, 0 * 8, 28 * 8 - 1),
            gfx1,
            2048 * ShadowColorsMultiplier, 2048 * ShadowColorsMultiplier,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            sys16_vh_start,
            sys16_vh_stop,
            sys16_vh_screenrefresh,
            SOUND_SUPPORTS_STEREO, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface
                )
            }
    );
    /**
     * ************************************************************************
     */
    // sys16B
    static RomLoadPtr rom_goldnaxe = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x0c0000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("epr12523.a7", 0x00000, 0x20000, 0x8e6128d7);
            ROM_LOAD_ODD("epr12522.a5", 0x00000, 0x20000, 0xb6c35160);
            /* emtpy 0x40000 - 0x80000 */
            ROM_LOAD_EVEN("epr12521.a8", 0x80000, 0x20000, 0x5001d713);
            ROM_LOAD_ODD("epr12519.a6", 0x80000, 0x20000, 0x4438ca8e);

            ROM_REGION(0x60000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* tiles */
            ROM_LOAD("epr12385", 0x00000, 0x20000, 0xb8a4e7e0);
            ROM_LOAD("epr12386", 0x20000, 0x20000, 0x25d7d779);
            ROM_LOAD("epr12387", 0x40000, 0x20000, 0xc7fcadf3);

            ROM_REGION(0x180000 * 2, REGION_GFX2);/* sprites */
            ROM_LOAD("mpr12378.b1", 0x000000, 0x40000, 0x119e5a82);
            ROM_LOAD("mpr12379.b4", 0x040000, 0x40000, 0x1a0e8c57);
            ROM_LOAD("mpr12380.b2", 0x080000, 0x40000, 0xbb2c0853);
            ROM_LOAD("mpr12381.b5", 0x0c0000, 0x40000, 0x81ba6ecc);
            ROM_LOAD("mpr12382.b3", 0x100000, 0x40000, 0x81601c6f);
            ROM_LOAD("mpr12383.b6", 0x140000, 0x40000, 0x5dbacf7a);

            ROM_REGION(0x30000, REGION_CPU2);/* sound CPU */
            ROM_LOAD("epr12390", 0x00000, 0x08000, 0x399fc5f5);
            ROM_LOAD("mpr12384.a11", 0x10000, 0x20000, 0x6218d8e7);
            ROM_END();
        }
    };

    static RomLoadPtr rom_goldnaxj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x0c0000, REGION_CPU1);/* 68000 code */
            // Custom cpu 317-0121
            ROM_LOAD_EVEN("epr12540.a7", 0x00000, 0x20000, 0x0c7ccc6d);
            ROM_LOAD_ODD("epr12539.a5", 0x00000, 0x20000, 0x1f24f7d0);
            /* emtpy 0x40000 - 0x80000 */
            ROM_LOAD_EVEN("epr12521.a8", 0x80000, 0x20000, 0x5001d713);
            ROM_LOAD_ODD("epr12519.a6", 0x80000, 0x20000, 0x4438ca8e);

            ROM_REGION(0x60000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* tiles */
            ROM_LOAD("epr12385", 0x00000, 0x20000, 0xb8a4e7e0);
            ROM_LOAD("epr12386", 0x20000, 0x20000, 0x25d7d779);
            ROM_LOAD("epr12387", 0x40000, 0x20000, 0xc7fcadf3);

            ROM_REGION(0x180000 * 2, REGION_GFX2);/* sprites */
            ROM_LOAD("mpr12378.b1", 0x000000, 0x40000, 0x119e5a82);
            ROM_LOAD("mpr12379.b4", 0x040000, 0x40000, 0x1a0e8c57);
            ROM_LOAD("mpr12380.b2", 0x080000, 0x40000, 0xbb2c0853);
            ROM_LOAD("mpr12381.b5", 0x0c0000, 0x40000, 0x81ba6ecc);
            ROM_LOAD("mpr12382.b3", 0x100000, 0x40000, 0x81601c6f);
            ROM_LOAD("mpr12383.b6", 0x140000, 0x40000, 0x5dbacf7a);

            ROM_REGION(0x30000, REGION_CPU2);/* sound CPU */
            ROM_LOAD("epr12390", 0x00000, 0x08000, 0x399fc5f5);
            ROM_LOAD("mpr12384.a11", 0x10000, 0x20000, 0x6218d8e7);
            ROM_END();
        }
    };

    static RomLoadPtr rom_goldnabl = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x0c0000, REGION_CPU1);/* 68000 code */
            // protected code
            ROM_LOAD_EVEN("ga6.a22", 0x00000, 0x10000, 0xf95b459f);
            ROM_LOAD_ODD("ga4.a20", 0x00000, 0x10000, 0x83eabdf5);
            ROM_LOAD_EVEN("ga11.a27", 0x20000, 0x10000, 0xf4ef9349);
            ROM_LOAD_ODD("ga8.a24", 0x20000, 0x10000, 0x37a65839);
            /* emtpy 0x40000 - 0x80000 */
            ROM_LOAD_EVEN("epr12521.a8", 0x80000, 0x20000, 0x5001d713);
            ROM_LOAD_ODD("epr12519.a6", 0x80000, 0x20000, 0x4438ca8e);

            ROM_REGION(0x60000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* tiles */
            ROM_LOAD("ga33.b16", 0x00000, 0x10000, 0x84587263);
            ROM_LOAD("ga32.b15", 0x10000, 0x10000, 0x63d72388);
            ROM_LOAD("ga31.b14", 0x20000, 0x10000, 0xf8b6ae4f);
            ROM_LOAD("ga30.b13", 0x30000, 0x10000, 0xe29baf4f);
            ROM_LOAD("ga29.b12", 0x40000, 0x10000, 0x22f0667e);
            ROM_LOAD("ga28.b11", 0x50000, 0x10000, 0xafb1a7e4);

            ROM_REGION(0x180000 * 2, REGION_GFX2);/* sprites */
            ROM_LOAD("ga34.b17", 0x000000, 0x10000, 0x28ba70c8);
            ROM_LOAD("ga35.b18", 0x010000, 0x10000, 0x2ed96a26);
            ROM_LOAD("ga23.a14", 0x020000, 0x10000, 0x84dccc5b);
            ROM_LOAD("ga18.a9", 0x030000, 0x10000, 0xde346006);
            ROM_LOAD("mpr12379.b4", 0x040000, 0x40000, 0x1a0e8c57);
            ROM_LOAD("ga36.b19", 0x080000, 0x10000, 0x101d2fff);
            ROM_LOAD("ga37.b20", 0x090000, 0x10000, 0x677e64a6);
            ROM_LOAD("ga19.a10", 0x0a0000, 0x10000, 0x11794d05);
            ROM_LOAD("ga20.a11", 0x0b0000, 0x10000, 0xad1c1c90);
            ROM_LOAD("mpr12381.b5", 0x0c0000, 0x40000, 0x81ba6ecc);
            ROM_LOAD("mpr12382.b3", 0x100000, 0x40000, 0x81601c6f);
            ROM_LOAD("mpr12383.b6", 0x140000, 0x40000, 0x5dbacf7a);

            ROM_REGION(0x30000, REGION_CPU2);/* sound CPU */
            ROM_LOAD("epr12390", 0x00000, 0x08000, 0x399fc5f5);
            ROM_LOAD("mpr12384.a11", 0x10000, 0x20000, 0x6218d8e7);
            ROM_END();
        }
    };

    /**
     * ************************************************************************
     */
    public static ReadHandlerPtr goldnaxe_skip = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x3cb0) {
                cpu_spinuntil_int();
                return 0xffff;
            }

            return sys16_workingram.READ_WORD(0x2c1c);
        }
    };

    public static ReadHandlerPtr ga_io_players_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (input_port_0_r.handler(offset) << 8) | input_port_1_r.handler(offset);
        }
    };
    public static ReadHandlerPtr ga_io_service_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (input_port_2_r.handler(offset) << 8) | (sys16_workingram.READ_WORD(0x2c96) & 0x00ff);
        }
    };

    static MemoryReadAddress goldnaxe_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x0bffff, MRA_ROM),
                new MemoryReadAddress(0x100000, 0x10ffff, sys16_tileram_r),
                new MemoryReadAddress(0x110000, 0x110fff, sys16_textram_r),
                new MemoryReadAddress(0x140000, 0x140fff, paletteram_word_r),
                new MemoryReadAddress(0x1f0000, 0x1f0003, MRA_BANK3),
                new MemoryReadAddress(0x200000, 0x200fff, MRA_BANK2),
                new MemoryReadAddress(0xc41002, 0xc41003, input_port_0_r),
                new MemoryReadAddress(0xc41006, 0xc41007, input_port_1_r),
                new MemoryReadAddress(0xc41000, 0xc41001, input_port_2_r),
                new MemoryReadAddress(0xc42002, 0xc42003, input_port_3_r),
                new MemoryReadAddress(0xc42000, 0xc42001, input_port_4_r),
                new MemoryReadAddress(0xc40000, 0xc40fff, MRA_BANK4),
                new MemoryReadAddress(0xffecd0, 0xffecd1, ga_io_players_r),
                new MemoryReadAddress(0xffec96, 0xffec97, ga_io_service_r),
                new MemoryReadAddress(0xffec1c, 0xffec1d, goldnaxe_skip),
                new MemoryReadAddress(0xffc000, 0xffffff, MRA_BANK1),
                new MemoryReadAddress(-1)
            };

    public static WriteHandlerPtr ga_sound_command_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 0xff000000) == 0) {
                sound_command_w.handler(offset, data >> 8);
            }
            COMBINE_WORD_MEM(sys16_workingram, 0x2cfc, data);
        }
    };

    static MemoryWriteAddress goldnaxe_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x0bffff, MWA_ROM),
                new MemoryWriteAddress(0x100000, 0x10ffff, sys16_tileram_w, sys16_tileram),
                new MemoryWriteAddress(0x110000, 0x110fff, sys16_textram_w, sys16_textram),
                new MemoryWriteAddress(0x140000, 0x140fff, sys16_paletteram_w, paletteram),
                new MemoryWriteAddress(0x1f0000, 0x1f0003, MWA_BANK3, sys16_extraram),
                new MemoryWriteAddress(0x200000, 0x200fff, MWA_BANK2, sys16_spriteram),
                new MemoryWriteAddress(0xc40000, 0xc40fff, MWA_BANK4, sys16_extraram2),
                new MemoryWriteAddress(0xc43000, 0xc43001, MWA_NOP),
                new MemoryWriteAddress(0xffecfc, 0xffecfd, ga_sound_command_w),
                new MemoryWriteAddress(0xffc000, 0xffffff, MWA_BANK1, sys16_workingram),
                new MemoryWriteAddress(-1)
            };
    /**
     * ************************************************************************
     */
    public static sys16_update_procPtr goldnaxe_update_proc = new sys16_update_procPtr() {
        public void handler() {
            sys16_fg_scrollx = sys16_textram.READ_WORD(0x0e98);
            sys16_bg_scrollx = sys16_textram.READ_WORD(0x0e9a);
            sys16_fg_scrolly = sys16_textram.READ_WORD(0x0e90);
            sys16_bg_scrolly = sys16_textram.READ_WORD(0x0e92);

            set_fg_page(sys16_textram.READ_WORD(0x0e80));
            set_bg_page(sys16_textram.READ_WORD(0x0e82));
            set_tile_bank(sys16_workingram.READ_WORD(0x2c94));
            set_refresh(sys16_extraram2.READ_WORD(0));
        }
    };

    public static InitMachinePtr goldnaxe_init_machine = new InitMachinePtr() {
        public void handler() {
            int bank[] = {0, 2, 8, 10, 16, 18, 0, 0, 4, 6, 12, 14, 20, 22, 0, 0};

            sys16_obj_bank = bank;

            patch_code.handler(0x3CB2, 0x60);
            patch_code.handler(0x3CB3, 0x1e);

            sys16_sprxoffset = -0xb8;
            sys16_update_proc = goldnaxe_update_proc;
        }
    };

    public static InitDriverPtr init_goldnaxe = new InitDriverPtr() {
        public void handler() {
            sys16_onetime_init_machine.handler();
            sys16_sprite_decode(3, 0x80000);
        }
    };

    public static InitDriverPtr init_goldnabl = new InitDriverPtr() {
        public void handler() {
            int i;

            sys16_onetime_init_machine.handler();

            /* invert the graphics bits on the tiles */
            for (i = 0; i < 0x60000; i++) {
                memory_region(REGION_GFX1).write(i, memory_region(REGION_GFX1).read(i) ^ 0xff);
            }
            sys16_sprite_decode(3, 0x80000);
        }
    };

    /**
     * ************************************************************************
     */
    static InputPortPtr input_ports_goldnaxe = new InputPortPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x07, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x09, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x05, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x04, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x02, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0x03, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x06, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x00, "Free Play (if Coin B too) or 1/1");
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x70, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x90, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x50, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x40, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x10, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x20, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0x30, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x60, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0xa0, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x00, "Free Play (if Coin A too) or 1/1");

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x01, 0x01, "Credits needed");
            PORT_DIPSETTING(0x01, "1 to start, 1 to continue");
            PORT_DIPSETTING(0x00, "2 to start, 1 to continue");
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Lives"));
            PORT_DIPSETTING(0x08, "1");
            PORT_DIPSETTING(0x0c, "2");
            PORT_DIPSETTING(0x04, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x30, 0x30, "Energy Meter");
            PORT_DIPSETTING(0x20, "2");
            PORT_DIPSETTING(0x30, "3");
            PORT_DIPSETTING(0x10, "4");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unused"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unused"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    /**
     * ************************************************************************
     */
    static MachineDriver machine_driver_goldnaxe = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000,
                        goldnaxe_readmem, goldnaxe_writemem, null, null,
                        sys16_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4096000,
                        sound_readmem_7759, sound_writemem, sound_readport, sound_writeport_7759,
                        ignore_interrupt, 1
                ),},
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1,
            goldnaxe_init_machine,
            40 * 8, 28 * 8, new rectangle(0 * 8, 40 * 8 - 1, 0 * 8, 28 * 8 - 1),
            gfx2,
            2048 * ShadowColorsMultiplier, 2048 * ShadowColorsMultiplier,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            sys16_vh_start,
            sys16_vh_stop,
            sys16_vh_screenrefresh,
            SOUND_SUPPORTS_STEREO, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface
                ), new MachineSound(
                        SOUND_UPD7759,
                        upd7759_interface
                )
            }
    );
    /**
     * ************************************************************************
     */
    /*TODO*///	// sys16B
/*TODO*///	static RomLoadPtr rom_goldnaxa = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x0c0000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "epr12545.a2", 0x00000, 0x40000, 0xa97c4e4d )
/*TODO*///		ROM_LOAD_ODD ( "epr12544.a1", 0x00000, 0x40000, 0x5e38f668 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x60000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "epr12385", 0x00000, 0x20000, 0xb8a4e7e0 );
/*TODO*///		ROM_LOAD( "epr12386", 0x20000, 0x20000, 0x25d7d779 );
/*TODO*///		ROM_LOAD( "epr12387", 0x40000, 0x20000, 0xc7fcadf3 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x180000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "mpr12378.b1", 0x000000, 0x40000, 0x119e5a82 );
/*TODO*///		ROM_LOAD( "mpr12379.b4", 0x040000, 0x40000, 0x1a0e8c57 );
/*TODO*///		ROM_LOAD( "mpr12380.b2", 0x080000, 0x40000, 0xbb2c0853 );
/*TODO*///		ROM_LOAD( "mpr12381.b5", 0x0c0000, 0x40000, 0x81ba6ecc );
/*TODO*///		ROM_LOAD( "mpr12382.b3", 0x100000, 0x40000, 0x81601c6f );
/*TODO*///		ROM_LOAD( "mpr12383.b6", 0x140000, 0x40000, 0x5dbacf7a );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr12390",     0x00000, 0x08000, 0x399fc5f5 );
/*TODO*///		ROM_LOAD( "mpr12384.a11", 0x10000, 0x20000, 0x6218d8e7 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_goldnaxb = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x0c0000, REGION_CPU1 );/* 68000 code */
/*TODO*///	// Custom 68000 ver 317-0110
/*TODO*///		ROM_LOAD_EVEN( "epr12389.a2", 0x00000, 0x40000, 0x35d5fa77 )
/*TODO*///		ROM_LOAD_ODD ( "epr12388.a1", 0x00000, 0x40000, 0x72952a93 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x60000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "epr12385", 0x00000, 0x20000, 0xb8a4e7e0 );
/*TODO*///		ROM_LOAD( "epr12386", 0x20000, 0x20000, 0x25d7d779 );
/*TODO*///		ROM_LOAD( "epr12387", 0x40000, 0x20000, 0xc7fcadf3 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x180000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "mpr12378.b1", 0x000000, 0x40000, 0x119e5a82 );
/*TODO*///		ROM_LOAD( "mpr12379.b4", 0x040000, 0x40000, 0x1a0e8c57 );
/*TODO*///		ROM_LOAD( "mpr12380.b2", 0x080000, 0x40000, 0xbb2c0853 );
/*TODO*///		ROM_LOAD( "mpr12381.b5", 0x0c0000, 0x40000, 0x81ba6ecc );
/*TODO*///		ROM_LOAD( "mpr12382.b3", 0x100000, 0x40000, 0x81601c6f );
/*TODO*///		ROM_LOAD( "mpr12383.b6", 0x140000, 0x40000, 0x5dbacf7a );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr12390",     0x00000, 0x08000, 0x399fc5f5 );
/*TODO*///		ROM_LOAD( "mpr12384.a11", 0x10000, 0x20000, 0x6218d8e7 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_goldnaxc = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x0c0000, REGION_CPU1 );/* 68000 code */
/*TODO*///	// Custom 68000 ver 317-0122
/*TODO*///		ROM_LOAD_EVEN( "epr12543.a2", 0x00000, 0x40000, 0xb0df9ca4 )
/*TODO*///		ROM_LOAD_ODD ( "epr12542.a1", 0x00000, 0x40000, 0xb7994d3c )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x60000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "epr12385", 0x00000, 0x20000, 0xb8a4e7e0 );
/*TODO*///		ROM_LOAD( "epr12386", 0x20000, 0x20000, 0x25d7d779 );
/*TODO*///		ROM_LOAD( "epr12387", 0x40000, 0x20000, 0xc7fcadf3 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x180000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "mpr12378.b1", 0x000000, 0x40000, 0x119e5a82 );
/*TODO*///		ROM_LOAD( "mpr12379.b4", 0x040000, 0x40000, 0x1a0e8c57 );
/*TODO*///		ROM_LOAD( "mpr12380.b2", 0x080000, 0x40000, 0xbb2c0853 );
/*TODO*///		ROM_LOAD( "mpr12381.b5", 0x0c0000, 0x40000, 0x81ba6ecc );
/*TODO*///		ROM_LOAD( "mpr12382.b3", 0x100000, 0x40000, 0x81601c6f );
/*TODO*///		ROM_LOAD( "mpr12383.b6", 0x140000, 0x40000, 0x5dbacf7a );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr12390",     0x00000, 0x08000, 0x399fc5f5 );
/*TODO*///		ROM_LOAD( "mpr12384.a11", 0x10000, 0x20000, 0x6218d8e7 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr goldnaxa_skip = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		if (cpu_get_pc()==0x3ca0) {cpu_spinuntil_int(); return 0xffff;}
/*TODO*///	
/*TODO*///		return READ_WORD(&sys16_workingram[0x2c1c]);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	// This version has somekind of hardware comparitor for collision detection,
/*TODO*///	// and a hardware multiplier.
/*TODO*///	static int ga_hardware_collision_data[5];
/*TODO*///	public static WriteHandlerPtr ga_hardware_collision_w = new WriteHandlerPtr() { public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		static int bit=1;
/*TODO*///		ga_hardware_collision_data[offset/2]=data;
/*TODO*///		if(offset==4)
/*TODO*///		{
/*TODO*///			if(ga_hardware_collision_data[2] <= ga_hardware_collision_data[0] &&
/*TODO*///				ga_hardware_collision_data[2] >= ga_hardware_collision_data[1])
/*TODO*///			{
/*TODO*///				ga_hardware_collision_data[4] |=bit;
/*TODO*///			}
/*TODO*///			bit=bit<<1;
/*TODO*///		}
/*TODO*///		if(offset==8) bit=1;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr ga_hardware_collision_r = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		return ga_hardware_collision_data[4];
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static int ga_hardware_multiplier_data[4];
/*TODO*///	public static WriteHandlerPtr ga_hardware_multiplier_w = new WriteHandlerPtr() { public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		ga_hardware_multiplier_data[offset/2]=data;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr ga_hardware_multiplier_r = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		if(offset==6)
/*TODO*///			return ga_hardware_multiplier_data[0] * ga_hardware_multiplier_data[1];
/*TODO*///		else
/*TODO*///			return ga_hardware_multiplier_data[offset/2];
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static MemoryReadAddress goldnaxa_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0x100000, 0x10ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x110000, 0x110fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x140000, 0x140fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0x1e0008, 0x1e0009, ga_hardware_collision_r ),
/*TODO*///		new MemoryReadAddress( 0x1f0000, 0x1f0007, ga_hardware_multiplier_r ),
/*TODO*///		new MemoryReadAddress( 0x1f1008, 0x1f1009, ga_hardware_collision_r ),
/*TODO*///		new MemoryReadAddress( 0x1f2000, 0x1f2003, MRA_EXTRAM ),
/*TODO*///		new MemoryReadAddress( 0x200000, 0x200fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0xc41002, 0xc41003, input_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xc41006, 0xc41007, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0xc41000, 0xc41001, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xc42002, 0xc42003, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xc42000, 0xc42001, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xc40000, 0xc40fff, MRA_EXTRAM2 ),
/*TODO*///		new MemoryReadAddress( 0xffecd0, 0xffecd1, ga_io_players_r ),
/*TODO*///		new MemoryReadAddress( 0xffec96, 0xffec97, ga_io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xffec1c, 0xffec1d, goldnaxa_skip ),
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress goldnaxa_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x100000, 0x10ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x110000, 0x110fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x140000, 0x140fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0x1e0000, 0x1e0009, ga_hardware_collision_w ),
/*TODO*///		new MemoryWriteAddress( 0x1f0000, 0x1f0003, ga_hardware_multiplier_w ),
/*TODO*///		new MemoryWriteAddress( 0x1f1000, 0x1f1009, ga_hardware_collision_w ),
/*TODO*///		new MemoryWriteAddress( 0x1f2000, 0x1f2003, MWA_EXTRAM ),
/*TODO*///		new MemoryWriteAddress( 0x200000, 0x200fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc40fff, MWA_BANK4,sys16_extraram2 ),
/*TODO*///		new MemoryWriteAddress( 0xffecfc, 0xffecfd, ga_sound_command_w ),
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void goldnaxa_update_proc( void ){
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0e98] );
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0e9a] );
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0e90] );
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0e92] );
/*TODO*///	
/*TODO*///		set_fg_page( READ_WORD( &sys16_textram[0x0e80] ) );
/*TODO*///		set_bg_page( READ_WORD( &sys16_textram[0x0e82] ) );
/*TODO*///		set_tile_bank( READ_WORD( &sys16_workingram[0x2c94] ) );
/*TODO*///		set_refresh( READ_WORD( &sys16_extraram2[0] ) );
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr goldnaxa_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = { 0,2,8,10,16,18,0,0,4,6,12,14,20,22,0,0 };
/*TODO*///	
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///	
/*TODO*///		patch_code( 0x3CA2, 0x60 );
/*TODO*///		patch_code( 0x3CA3, 0x1e );
/*TODO*///	
/*TODO*///		sys16_sprxoffset = -0xb8;
/*TODO*///		sys16_update_proc = goldnaxa_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_7759( machine_driver_goldnaxa, \
/*TODO*///		goldnaxa_readmem,goldnaxa_writemem,goldnaxa_init_machine, gfx2,upd7759_interface )
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	// sys16B
/*TODO*///	static RomLoadPtr rom_hwchamp = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x040000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "rom0-e.bin", 0x000000, 0x20000, 0xe5abfed7 )
/*TODO*///		ROM_LOAD_ODD ( "rom0-o.bin", 0x000000, 0x20000, 0x25180124 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "scr01.bin", 0x00000, 0x20000, 0xfc586a86 );
/*TODO*///		ROM_LOAD( "scr11.bin", 0x20000, 0x20000, 0xaeaaa9d8 );
/*TODO*///		ROM_LOAD( "scr02.bin", 0x40000, 0x20000, 0x7715a742 );
/*TODO*///		ROM_LOAD( "scr12.bin", 0x60000, 0x20000, 0x63a82afa );
/*TODO*///		ROM_LOAD( "scr03.bin", 0x80000, 0x20000, 0xf30cd5fd );
/*TODO*///		ROM_LOAD( "scr13.bin", 0xA0000, 0x20000, 0x5b8494a8 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "obj0-o.bin", 0x000000, 0x020000, 0xfc098a13 );
/*TODO*///		ROM_LOAD( "obj0-e.bin", 0x020000, 0x020000, 0x5db934a8 );
/*TODO*///		ROM_LOAD( "obj1-o.bin", 0x040000, 0x020000, 0x1f27ee74 );
/*TODO*///		ROM_LOAD( "obj1-e.bin", 0x060000, 0x020000, 0x8a6a5cf1 );
/*TODO*///		ROM_LOAD( "obj2-o.bin", 0x080000, 0x020000, 0xc0b2ba82 );
/*TODO*///		ROM_LOAD( "obj2-e.bin", 0x0a0000, 0x020000, 0xd6c7917b );
/*TODO*///		ROM_LOAD( "obj3-o.bin", 0x0c0000, 0x020000, 0x52fa3a49 );
/*TODO*///		ROM_LOAD( "obj3-e.bin", 0x0e0000, 0x020000, 0x57e8f9d2 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "s-prog.bin", 0x0000, 0x8000, 0x96a12d9d );
/*TODO*///	
/*TODO*///		ROM_LOAD( "speech0.bin", 0x10000, 0x20000, 0x4191c03d );
/*TODO*///		ROM_LOAD( "speech1.bin", 0x30000, 0x20000, 0xa4d53f7b );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static int hwc_handles_shifts[3];
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr hwc_io_handles_w = new WriteHandlerPtr() { public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		hwc_handles_shifts[offset/2]=7;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr hwc_io_handles_r = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		static int dodge_toggle=0;
/*TODO*///		int data=0,ret;
/*TODO*///		if(offset==0)
/*TODO*///		{
/*TODO*///			// monitor
/*TODO*///			data=input_port_0_r( offset );
/*TODO*///			if(input_port_1_r( offset ) & 4)
/*TODO*///			{
/*TODO*///				if(dodge_toggle)
/*TODO*///					data=0x38;
/*TODO*///				else
/*TODO*///					data=0x60;
/*TODO*///			}
/*TODO*///			if(input_port_1_r( offset ) & 8)
/*TODO*///			{
/*TODO*///				if(dodge_toggle)
/*TODO*///					data=0xc8;
/*TODO*///				else
/*TODO*///					data=0xa0;
/*TODO*///			}
/*TODO*///			if(input_port_1_r( offset ) & 0x10)
/*TODO*///			{
/*TODO*///				if(dodge_toggle)
/*TODO*///					data=0xff;
/*TODO*///				else
/*TODO*///					data=0xe0;
/*TODO*///			}
/*TODO*///			if(input_port_1_r( offset ) & 0x20)
/*TODO*///			{
/*TODO*///				if(dodge_toggle)
/*TODO*///					data=0x0;
/*TODO*///				else
/*TODO*///					data=0x20;
/*TODO*///			}
/*TODO*///			if(hwc_handles_shifts[offset/2]==0)
/*TODO*///				dodge_toggle^=1;
/*TODO*///		}
/*TODO*///		else if(offset==2)
/*TODO*///		{
/*TODO*///			// left handle
/*TODO*///			if(input_port_1_r( offset ) & 1)
/*TODO*///				data=0xff;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			// right handle
/*TODO*///			if(input_port_1_r( offset ) & 2)
/*TODO*///				data=0xff;
/*TODO*///		}
/*TODO*///		ret=data>>hwc_handles_shifts[offset/2];
/*TODO*///		hwc_handles_shifts[offset/2]--;
/*TODO*///		return ret;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static MemoryReadAddress hwchamp_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x3f0000, 0x3fffff, MRA_EXTRAM ),
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x440000, 0x440fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x840000, 0x840fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0xc43020, 0xc43025, hwc_io_handles_r ),
/*TODO*///		new MemoryReadAddress( 0xc41000, 0xc41001, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xc42002, 0xc42003, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xc42000, 0xc42001, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xc40000, 0xc43fff, MRA_EXTRAM2 ),
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress hwchamp_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x3f0000, 0x3fffff, MWA_EXTRAM ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x440000, 0x440fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x840000, 0x840fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xc43020, 0xc43025, hwc_io_handles_w ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc43fff, MWA_BANK4,sys16_extraram2 ),
/*TODO*///		new MemoryWriteAddress( 0xfe0006, 0xfe0007, sound_command_w ),
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void hwchamp_update_proc( void ){
/*TODO*///		int leds;
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0e98] );
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0e9a] );
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0e90] );
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0e92] );
/*TODO*///	
/*TODO*///		set_fg_page( READ_WORD( &sys16_textram[0x0e80] ) );
/*TODO*///		set_bg_page( READ_WORD( &sys16_textram[0x0e82] ) );
/*TODO*///	
/*TODO*///		sys16_tile_bank0 = READ_WORD( &sys16_extraram[0x0000] )&0xf;
/*TODO*///		sys16_tile_bank1 = READ_WORD( &sys16_extraram[0x0002] )&0xf;
/*TODO*///	
/*TODO*///		set_refresh( READ_WORD( &sys16_extraram2[0] ) );
/*TODO*///	
/*TODO*///		leds=READ_WORD( &sys16_extraram2[0x3034] );
/*TODO*///		if(leds & 0x20)
/*TODO*///			osd_led_w(0,1);
/*TODO*///		else
/*TODO*///			osd_led_w(0,0);
/*TODO*///		if(leds & 0x80)
/*TODO*///			osd_led_w(1,1);
/*TODO*///		else
/*TODO*///			osd_led_w(1,0);
/*TODO*///		if(leds & 0x40)
/*TODO*///			osd_led_w(2,1);
/*TODO*///		else
/*TODO*///			osd_led_w(2,0);
/*TODO*///	
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr hwchamp_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = {0,2,4,6,8,10,12,14,16,18,20,22,24,26,28,30};
/*TODO*///	
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_spritelist_end=0xc000;
/*TODO*///	
/*TODO*///		sys16_update_proc = hwchamp_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_hwchamp = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_sprite_decode( 4,0x040000 );
/*TODO*///	} };
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_hwchamp = new InputPortPtr(){ public void handler() { 
/*TODO*///	
/*TODO*///	PORT_START(); 	/* Monitor */
/*TODO*///		PORT_ANALOG( 0xff, 0x80, IPT_PADDLE  , 70, 4, 0x0, 0xff );
/*TODO*///	
/*TODO*///	PORT_START(); 	/* Handles (Fake) */
/*TODO*///		PORT_BITX(0x01, 0, IPT_BUTTON1, IP_NAME_DEFAULT, KEYCODE_F, IP_JOY_NONE );// right hit
/*TODO*///		PORT_BITX(0x02, 0, IPT_BUTTON2, IP_NAME_DEFAULT, KEYCODE_D, IP_JOY_NONE );// left hit
/*TODO*///		PORT_BITX(0x04, 0, IPT_BUTTON3, IP_NAME_DEFAULT, KEYCODE_B, IP_JOY_NONE );// right dodge
/*TODO*///		PORT_BITX(0x08, 0, IPT_BUTTON4, IP_NAME_DEFAULT, KEYCODE_Z, IP_JOY_NONE );// left dodge
/*TODO*///		PORT_BITX(0x10, 0, IPT_BUTTON5, IP_NAME_DEFAULT, KEYCODE_V, IP_JOY_NONE );// right sway
/*TODO*///		PORT_BITX(0x20, 0, IPT_BUTTON6, IP_NAME_DEFAULT, KEYCODE_X, IP_JOY_NONE );// left swat
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		SYS16_SERVICE
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );	// Not Used
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x04, 0x00, "Start Level Select" );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Continue Mode" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x20, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x30, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x10, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0xc0, 0xc0, "Time Adjust"  );
/*TODO*///		PORT_DIPSETTING(    0x80, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0xc0, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x40, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///	
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_7759( machine_driver_hwchamp, \
/*TODO*///		hwchamp_readmem,hwchamp_writemem,hwchamp_init_machine, gfx4 ,upd7759_interface)
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	// pre16
/*TODO*///	static RomLoadPtr rom_mjleague = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x030000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "epr-7404.09b", 0x000000, 0x8000, 0xec1655b5 )
/*TODO*///		ROM_LOAD_ODD ( "epr-7401.06b", 0x000000, 0x8000, 0x2befa5e0 )
/*TODO*///		ROM_LOAD_EVEN( "epr-7405.10b", 0x010000, 0x8000, 0x7a4f4e38 )
/*TODO*///		ROM_LOAD_ODD ( "epr-7402.07b", 0x010000, 0x8000, 0xb7bef762 )
/*TODO*///		ROM_LOAD_EVEN( "epra7406.11b", 0x020000, 0x8000, 0xbb743639 )
/*TODO*///		ROM_LOAD_ODD ( "epra7403.08b", 0x020000, 0x8000, 0xd86250cf )	// Fails memory test. Bad rom?
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "epr-7051.09a", 0x00000, 0x08000, 0x10ca255a );
/*TODO*///		ROM_RELOAD(               0x08000, 0x08000 );
/*TODO*///		ROM_LOAD( "epr-7052.10a", 0x10000, 0x08000, 0x2550db0e );
/*TODO*///		ROM_RELOAD(               0x18000, 0x08000 );
/*TODO*///		ROM_LOAD( "epr-7053.11a", 0x20000, 0x08000, 0x5bfea038 );
/*TODO*///		ROM_RELOAD(               0x28000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x050000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "epr-7055.05a", 0x000000, 0x008000, 0x1fb860bd );
/*TODO*///		ROM_LOAD( "epr-7059.02b", 0x008000, 0x008000, 0x3d14091d );
/*TODO*///		ROM_LOAD( "epr-7056.06a", 0x010000, 0x008000, 0xb35dd968 );
/*TODO*///		ROM_LOAD( "epr-7060.03b", 0x018000, 0x008000, 0x61bb3757 );
/*TODO*///		ROM_LOAD( "epr-7057.07a", 0x020000, 0x008000, 0x3e5a2b6f );
/*TODO*///		ROM_LOAD( "epr-7061.04b", 0x028000, 0x008000, 0xc808dad5 );
/*TODO*///		ROM_LOAD( "epr-7058.08a", 0x030000, 0x008000, 0xb543675f );
/*TODO*///		ROM_LOAD( "epr-7062.05b", 0x038000, 0x008000, 0x9168eb47 );
/*TODO*///	//	ROM_LOAD( "epr-7055.05a", 0x040000, 0x008000, 0x1fb860bd );loaded twice??
/*TODO*///	//	ROM_LOAD( "epr-7059.02b", 0x048000, 0x008000, 0x3d14091d );loaded twice??
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "eprc7054.01b", 0x00000, 0x8000, 0x4443b744 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x1000, REGION_CPU3 );     /* 4k for 7751 onboard ROM */
/*TODO*///		ROM_LOAD( "7751.bin",     0x0000, 0x0400, 0x6a9534fc );/* 7751 - U34 */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_SOUND1 );/* 7751 sound data */
/*TODO*///		ROM_LOAD( "epr-7063.01a", 0x00000, 0x8000, 0x45d8908a );
/*TODO*///		ROM_LOAD( "epr-7065.02a", 0x08000, 0x8000, 0x8c8f8cff );
/*TODO*///		ROM_LOAD( "epr-7064.03a", 0x10000, 0x8000, 0x159f6636 );
/*TODO*///		ROM_LOAD( "epr-7066.04a", 0x18000, 0x8000, 0xf5cfa91f );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr mjl_io_player1_r = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int data=input_port_0_r( offset ) & 0x80;
/*TODO*///	
/*TODO*///		if(READ_WORD( &sys16_extraram2[2] ) & 0x4)
/*TODO*///			data|=(input_port_5_r( offset ) & 0x3f) << 1;
/*TODO*///		else
/*TODO*///			data|=(input_port_6_r( offset ) & 0x3f) << 1;
/*TODO*///	
/*TODO*///		return data;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr mjl_io_service_r = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int data=input_port_2_r( offset ) & 0x3f;
/*TODO*///	
/*TODO*///		if(READ_WORD( &sys16_extraram2[2] ) & 0x4)
/*TODO*///		{
/*TODO*///			data|=(input_port_5_r( offset ) & 0x40);
/*TODO*///			data|=(input_port_7_r( offset ) & 0x40) << 1;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			data|=(input_port_6_r( offset ) & 0x40);
/*TODO*///			data|=(input_port_8_r( offset ) & 0x40) << 1;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return data;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr mjl_io_player2_r = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int data=input_port_1_r( offset ) & 0x80;
/*TODO*///		if(READ_WORD( &sys16_extraram2[2] ) & 0x4)
/*TODO*///			data|=(input_port_7_r( offset ) & 0x3f) << 1;
/*TODO*///		else
/*TODO*///			data|=(input_port_8_r( offset ) & 0x3f) << 1;
/*TODO*///		return data;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr mjl_io_bat_r = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int data1=input_port_0_r( offset );
/*TODO*///		int data2=input_port_1_r( offset );
/*TODO*///		int ret=0;
/*TODO*///	
/*TODO*///		// Hitting has 8 values, but for easy of playing, I've only added 3
/*TODO*///	
/*TODO*///		if(data1 &1) ret=0x00;
/*TODO*///		else if(data1 &2) ret=0x03;
/*TODO*///		else if(data1 &4) ret=0x07;
/*TODO*///		else ret=0x0f;
/*TODO*///	
/*TODO*///		if(data2 &1) ret|=0x00;
/*TODO*///		else if(data2 &2) ret|=0x30;
/*TODO*///		else if(data2 &4) ret|=0x70;
/*TODO*///		else ret|=0xf0;
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static MemoryReadAddress mjleague_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x02ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x440000, 0x440fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x840000, 0x840fff, paletteram_word_r ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0xc40002, 0xc40007, MRA_EXTRAM2),
/*TODO*///		new MemoryReadAddress( 0xc41000, 0xc41001, mjl_io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xc41002, 0xc41003, mjl_io_player1_r ),
/*TODO*///		new MemoryReadAddress( 0xc41006, 0xc41007, mjl_io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0xc41004, 0xc41005, mjl_io_bat_r ),
/*TODO*///		new MemoryReadAddress( 0xc42000, 0xc42001, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xc42002, 0xc42003, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xc60000, 0xc60001, MRA_NOP ), /* What is this? Watchdog? */
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress mjleague_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x02ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x440000, 0x440fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x840000, 0x840fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc40001, sound_command_nmi_w ),
/*TODO*///		new MemoryWriteAddress( 0xc40002, 0xc40007, MWA_BANK4,sys16_extraram2),
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void mjleague_update_proc( void ){
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0ff8] ) & 0x01ff;
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0ffa] ) & 0x01ff;
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0f24] ) & 0x00ff;
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0f26] ) & 0x01ff;
/*TODO*///	
/*TODO*///		set_fg_page1( READ_WORD( &sys16_textram[0x0e8e] ) );
/*TODO*///		set_bg_page1( READ_WORD( &sys16_textram[0x0e8c] ) );
/*TODO*///	
/*TODO*///		set_refresh_3d( READ_WORD( &sys16_extraram2[0] ) );
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr mjleague_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = { 00,01,02,03,00,01,02,03,00,01,02,03,00,01,02,03};
/*TODO*///	
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_textmode=1;
/*TODO*///		sys16_spritesystem = 2;
/*TODO*///		sys16_sprxoffset = -0xbd;
/*TODO*///		sys16_fgxoffset = sys16_bgxoffset = 7;
/*TODO*///	
/*TODO*///		// remove memory test because it fails.
/*TODO*///		patch_code( 0xBD42, 0x66 );
/*TODO*///	
/*TODO*///		sys16_update_proc = mjleague_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_mjleague = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_sprite_decode( 5,0x010000 );
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_mjleague = new InputPortPtr(){ public void handler() { 
/*TODO*///	
/*TODO*///	PORT_START();  /* player 1 button fake */
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 );
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON3 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_BUTTON4 );
/*TODO*///	
/*TODO*///	PORT_START();  /* player 1 button fake */
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_BUTTON4 | IPF_PLAYER2 );
/*TODO*///	
/*TODO*///	PORT_START();   /* Service */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN3 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
/*TODO*///		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x0c, 0x00, "Starting Points" );
/*TODO*///		PORT_DIPSETTING(    0x0c, "2000" );
/*TODO*///		PORT_DIPSETTING(    0x08, "3000" );
/*TODO*///		PORT_DIPSETTING(    0x04, "5000" );
/*TODO*///		PORT_DIPSETTING(    0x00, "10000" );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, "Team Select" );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );	//??? something to do with cocktail mode?
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///	PORT_START(); 	/* IN5 */
/*TODO*///		PORT_ANALOG( 0x7f, 0x40, IPT_TRACKBALL_Y, 70, 30, 0, 127 );
/*TODO*///	
/*TODO*///	PORT_START(); 	/* IN6 */
/*TODO*///		PORT_ANALOG( 0x7f, 0x40, IPT_TRACKBALL_X /*| IPF_REVERSE*/, 50, 30, 0, 127 );
/*TODO*///	
/*TODO*///	PORT_START(); 	/* IN7 */
/*TODO*///		PORT_ANALOG( 0x7f, 0x40, IPT_TRACKBALL_Y | IPF_PLAYER2, 70, 30, 0, 127 );
/*TODO*///	
/*TODO*///	PORT_START(); 	/* IN8 */
/*TODO*///		PORT_ANALOG( 0x7f, 0x40, IPT_TRACKBALL_X | IPF_PLAYER2 | IPF_REVERSE, 50, 30, 0, 127 );
/*TODO*///	
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_7751( machine_driver_mjleague, \
/*TODO*///		mjleague_readmem,mjleague_writemem,mjleague_init_machine, gfx1)
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	// sys18
/*TODO*///	static RomLoadPtr rom_moonwalk = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x080000, REGION_CPU1 );/* 68000 code */
/*TODO*///	// custom cpu 317-0159
/*TODO*///		ROM_LOAD_EVEN( "epr13235.a6", 0x000000, 0x40000, 0x6983e129 )
/*TODO*///		ROM_LOAD_ODD ( "epr13234.a5", 0x000000, 0x40000, 0xc9fd20f2 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "mpr13216.b1", 0x00000, 0x40000, 0x862d2c03 );
/*TODO*///		ROM_LOAD( "mpr13217.b2", 0x40000, 0x40000, 0x7d1ac3ec );
/*TODO*///		ROM_LOAD( "mpr13218.b3", 0x80000, 0x40000, 0x56d3393c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "mpr13224.b11", 0x000000, 0x40000, 0xc59f107b );
/*TODO*///		ROM_LOAD( "mpr13231.a11", 0x040000, 0x40000, 0xa5e96346 );
/*TODO*///		ROM_LOAD( "mpr13223.b10", 0x080000, 0x40000, 0x364f60ff );
/*TODO*///		ROM_LOAD( "mpr13230.a10", 0x0c0000, 0x40000, 0x9550091f );
/*TODO*///		ROM_LOAD( "mpr13222.b9",  0x100000, 0x40000, 0x523df3ed );
/*TODO*///		ROM_LOAD( "mpr13229.a9",  0x140000, 0x40000, 0xf40dc45d );
/*TODO*///		ROM_LOAD( "epr13221.b8",  0x180000, 0x40000, 0x9ae7546a );
/*TODO*///		ROM_LOAD( "epr13228.a8",  0x1c0000, 0x40000, 0xde3786be );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr13225.a4", 0x10000, 0x20000, 0x56c2e82b );
/*TODO*///		ROM_LOAD( "mpr13219.b4", 0x30000, 0x40000, 0x19e2061f );
/*TODO*///		ROM_LOAD( "mpr13220.b5", 0x70000, 0x40000, 0x58d4d9ce );
/*TODO*///		ROM_LOAD( "mpr13249.b6", 0xb0000, 0x40000, 0x623edc5d );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_moonwlka = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x080000, REGION_CPU1 );/* 68000 code */
/*TODO*///	// custom cpu 317-0158
/*TODO*///		ROM_LOAD_EVEN( "epr13233", 0x000000, 0x40000, 0xf3dac671 )
/*TODO*///		ROM_LOAD_ODD ( "epr13232", 0x000000, 0x40000, 0x541d8bdf )
/*TODO*///	
/*TODO*///		ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "mpr13216.b1", 0x00000, 0x40000, 0x862d2c03 );
/*TODO*///		ROM_LOAD( "mpr13217.b2", 0x40000, 0x40000, 0x7d1ac3ec );
/*TODO*///		ROM_LOAD( "mpr13218.b3", 0x80000, 0x40000, 0x56d3393c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "mpr13224.b11", 0x000000, 0x40000, 0xc59f107b );
/*TODO*///		ROM_LOAD( "mpr13231.a11", 0x040000, 0x40000, 0xa5e96346 );
/*TODO*///		ROM_LOAD( "mpr13223.b10", 0x080000, 0x40000, 0x364f60ff );
/*TODO*///		ROM_LOAD( "mpr13230.a10", 0x0c0000, 0x40000, 0x9550091f );
/*TODO*///		ROM_LOAD( "mpr13222.b9",  0x100000, 0x40000, 0x523df3ed );
/*TODO*///		ROM_LOAD( "mpr13229.a9",  0x140000, 0x40000, 0xf40dc45d );
/*TODO*///		ROM_LOAD( "epr13221.b8",  0x180000, 0x40000, 0x9ae7546a );
/*TODO*///		ROM_LOAD( "epr13228.a8",  0x1c0000, 0x40000, 0xde3786be );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr13225.a4", 0x10000, 0x20000, 0x56c2e82b );
/*TODO*///		ROM_LOAD( "mpr13219.b4", 0x30000, 0x40000, 0x19e2061f );
/*TODO*///		ROM_LOAD( "mpr13220.b5", 0x70000, 0x40000, 0x58d4d9ce );
/*TODO*///		ROM_LOAD( "mpr13249.b6", 0xb0000, 0x40000, 0x623edc5d );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	// sys18
/*TODO*///	static RomLoadPtr rom_moonwlkb = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x080000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "moonwlkb.01", 0x000000, 0x10000, 0xf49cdb16 )
/*TODO*///		ROM_LOAD_ODD ( "moonwlkb.05", 0x000000, 0x10000, 0xc483f29f )
/*TODO*///		ROM_LOAD_EVEN( "moonwlkb.02", 0x020000, 0x10000, 0x0bde1896 )
/*TODO*///		ROM_LOAD_ODD ( "moonwlkb.06", 0x020000, 0x10000, 0x5b9fc688 )
/*TODO*///		ROM_LOAD_EVEN( "moonwlkb.03", 0x040000, 0x10000, 0x0c5fe15c )
/*TODO*///		ROM_LOAD_ODD ( "moonwlkb.07", 0x040000, 0x10000, 0x9e600704 )
/*TODO*///		ROM_LOAD_EVEN( "moonwlkb.04", 0x060000, 0x10000, 0x64692f79 )
/*TODO*///		ROM_LOAD_ODD ( "moonwlkb.08", 0x060000, 0x10000, 0x546ca530 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "mpr13216.b1", 0x00000, 0x40000, 0x862d2c03 );
/*TODO*///		ROM_LOAD( "mpr13217.b2", 0x40000, 0x40000, 0x7d1ac3ec );
/*TODO*///		ROM_LOAD( "mpr13218.b3", 0x80000, 0x40000, 0x56d3393c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "mpr13224.b11", 0x000000, 0x40000, 0xc59f107b );
/*TODO*///		ROM_LOAD( "mpr13231.a11", 0x040000, 0x40000, 0xa5e96346 );
/*TODO*///		ROM_LOAD( "mpr13223.b10", 0x080000, 0x40000, 0x364f60ff );
/*TODO*///		ROM_LOAD( "mpr13230.a10", 0x0c0000, 0x40000, 0x9550091f );
/*TODO*///		ROM_LOAD( "mpr13222.b9",  0x100000, 0x40000, 0x523df3ed );
/*TODO*///		ROM_LOAD( "mpr13229.a9",  0x140000, 0x40000, 0xf40dc45d );
/*TODO*///		ROM_LOAD( "epr13221.b8",  0x180000, 0x40000, 0x9ae7546a );
/*TODO*///		ROM_LOAD( "epr13228.a8",  0x1c0000, 0x40000, 0xde3786be );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr13225.a4", 0x10000, 0x20000, 0x56c2e82b );
/*TODO*///		ROM_LOAD( "mpr13219.b4", 0x30000, 0x40000, 0x19e2061f );
/*TODO*///		ROM_LOAD( "mpr13220.b5", 0x70000, 0x40000, 0x58d4d9ce );
/*TODO*///		ROM_LOAD( "mpr13249.b6", 0xb0000, 0x40000, 0x623edc5d );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr moonwlkb_skip = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		if (cpu_get_pc()==0x308a) {cpu_spinuntil_int(); return 0xffff;}
/*TODO*///	
/*TODO*///		return READ_WORD(&sys16_workingram[0x202c]);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static MemoryReadAddress moonwalk_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x440000, 0x440fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x840000, 0x840fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0xc00000, 0xc0ffff, MRA_EXTRAM ),
/*TODO*///		new MemoryReadAddress( 0xc40000, 0xc40001, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xc40002, 0xc40003, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xc41002, 0xc41003, input_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xc41004, 0xc41005, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0xc41006, 0xc41007, io_player3_r ),
/*TODO*///		new MemoryReadAddress( 0xc41000, 0xc41001, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xc40000, 0xc4ffff, MRA_EXTRAM3 ),
/*TODO*///		new MemoryReadAddress( 0xe40000, 0xe4ffff, MRA_EXTRAM2 ),
/*TODO*///		new MemoryReadAddress( 0xfe0000, 0xfeffff, MRA_EXTRAM4 ),
/*TODO*///		new MemoryReadAddress( 0xffe02c, 0xffe02d, moonwlkb_skip ),
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress moonwalk_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x440000, 0x440fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x840000, 0x840fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xc00000, 0xc0ffff, MWA_EXTRAM ),
/*TODO*///		new MemoryWriteAddress( 0xc40006, 0xc40007, sound_command_nmi_w ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc4ffff, MWA_EXTRAM3 ),
/*TODO*///		new MemoryWriteAddress( 0xe40000, 0xe4ffff, MWA_BANK4,sys16_extraram2 ),
/*TODO*///		new MemoryWriteAddress( 0xfe0000, 0xfeffff, MWA_EXTRAM4 ),
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void moonwalk_update_proc( void ){
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0e98] );
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0e9a] );
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0e90] );
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0e92] );
/*TODO*///	
/*TODO*///		set_fg_page( READ_WORD( &sys16_textram[0x0e80] ) );
/*TODO*///		set_bg_page( READ_WORD( &sys16_textram[0x0e82] ) );
/*TODO*///	
/*TODO*///		sys16_fg2_scrollx = READ_WORD( &sys16_textram[0x0e9c] );
/*TODO*///		sys16_bg2_scrollx = READ_WORD( &sys16_textram[0x0e9e] );
/*TODO*///		sys16_fg2_scrolly = READ_WORD( &sys16_textram[0x0e94] );
/*TODO*///		sys16_bg2_scrolly = READ_WORD( &sys16_textram[0x0e96] );
/*TODO*///	
/*TODO*///		set_fg2_page( READ_WORD( &sys16_textram[0x0e84] ) );
/*TODO*///		set_bg2_page( READ_WORD( &sys16_textram[0x0e86] ) );
/*TODO*///	
/*TODO*///		if(sys16_fg2_scrollx | sys16_fg2_scrolly | READ_WORD( &sys16_textram[0x0e84] ))
/*TODO*///			sys18_fg2_active=1;
/*TODO*///		else
/*TODO*///			sys18_fg2_active=0;
/*TODO*///		if(sys16_bg2_scrollx | sys16_bg2_scrolly | READ_WORD( &sys16_textram[0x0e86] ))
/*TODO*///			sys18_bg2_active=1;
/*TODO*///		else
/*TODO*///			sys18_bg2_active=0;
/*TODO*///	
/*TODO*///		set_tile_bank18( READ_WORD( &sys16_extraram3[0x6800] ) );
/*TODO*///		set_refresh_18( READ_WORD( &sys16_extraram3[0x6600] ) ); // 0xc46601
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr moonwalk_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = {0x00,0x02,0x04,0x06,0x08,0x0A,0x0C,0x0E,0x10,0x12,0x14,0x16,0x18,0x1A,0x1C,0x1E};
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_bg_priority_value=0x1000;
/*TODO*///		sys16_sprxoffset = -0x238;
/*TODO*///		sys16_spritelist_end=0x8000;
/*TODO*///	
/*TODO*///		patch_code( 0x70116, 0x4e);
/*TODO*///		patch_code( 0x70117, 0x71);
/*TODO*///	
/*TODO*///		patch_code( 0x314a, 0x46);
/*TODO*///		patch_code( 0x314b, 0x42);
/*TODO*///	
/*TODO*///		patch_code( 0x311b, 0x3f);
/*TODO*///	
/*TODO*///		patch_code( 0x70103, 0x00);
/*TODO*///		patch_code( 0x70109, 0x00);
/*TODO*///		patch_code( 0x07727, 0x00);
/*TODO*///		patch_code( 0x07729, 0x00);
/*TODO*///		patch_code( 0x0780d, 0x00);
/*TODO*///		patch_code( 0x0780f, 0x00);
/*TODO*///		patch_code( 0x07861, 0x00);
/*TODO*///		patch_code( 0x07863, 0x00);
/*TODO*///		patch_code( 0x07d47, 0x00);
/*TODO*///		patch_code( 0x07863, 0x00);
/*TODO*///		patch_code( 0x08533, 0x00);
/*TODO*///		patch_code( 0x08535, 0x00);
/*TODO*///		patch_code( 0x085bd, 0x00);
/*TODO*///		patch_code( 0x085bf, 0x00);
/*TODO*///		patch_code( 0x09a4b, 0x00);
/*TODO*///		patch_code( 0x09a4d, 0x00);
/*TODO*///		patch_code( 0x09b2f, 0x00);
/*TODO*///		patch_code( 0x09b31, 0x00);
/*TODO*///		patch_code( 0x0a05b, 0x00);
/*TODO*///		patch_code( 0x0a05d, 0x00);
/*TODO*///		patch_code( 0x0a23f, 0x00);
/*TODO*///		patch_code( 0x0a241, 0x00);
/*TODO*///		patch_code( 0x10159, 0x00);
/*TODO*///		patch_code( 0x1015b, 0x00);
/*TODO*///		patch_code( 0x109fb, 0x00);
/*TODO*///		patch_code( 0x109fd, 0x00);
/*TODO*///	
/*TODO*///		// * SEGA mark
/*TODO*///		patch_code( 0x70212, 0x4e);
/*TODO*///		patch_code( 0x70213, 0x71);
/*TODO*///	
/*TODO*///		sys16_update_proc = moonwalk_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_moonwalk = new InitDriverPtr() { public void handler() {
/*TODO*///		UBytePtr RAM= memory_region(REGION_CPU2);
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys18_splittab_fg_x=&sys16_textram[0x0f80];
/*TODO*///		sys18_splittab_bg_x=&sys16_textram[0x0fc0];
/*TODO*///	
/*TODO*///		memcpy(RAM,&RAM[0x10000],0xa000);
/*TODO*///	
/*TODO*///		sys16_sprite_decode( 4,0x080000 );
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_moonwalk = new InputPortPtr(){ public void handler() { 
/*TODO*///	
/*TODO*///	PORT_START();  /* player 1 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON3 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
/*TODO*///	
/*TODO*///	PORT_START();  /* player 2 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///	
/*TODO*///	PORT_START();  /* service */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
/*TODO*///		PORT_BITX(0x08, 0x08, IPT_TILT, "Test", KEYCODE_T, IP_JOY_NONE );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x01, "2 Credits to Start" );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Lives") );
/*TODO*///		PORT_DIPSETTING(    0x04, "2" );
/*TODO*///		PORT_DIPSETTING(    0x00, "3" );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Player Vitality" );
/*TODO*///		PORT_DIPSETTING(    0x08, "Low" );
/*TODO*///		PORT_DIPSETTING(    0x00, "High" );
/*TODO*///		PORT_DIPNAME( 0x10, 0x00, "Play Mode" );
/*TODO*///		PORT_DIPSETTING(    0x10, "2 Players" );
/*TODO*///		PORT_DIPSETTING(    0x00, "3 Players" );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, "Coin Mode" );
/*TODO*///		PORT_DIPSETTING(    0x20, "Common" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Individual" );
/*TODO*///		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x80, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0xc0, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x40, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///	
/*TODO*///	PORT_START();  /* player 3 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER3 );
/*TODO*///	//	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START3 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_18( machine_driver_moonwalk, \
/*TODO*///		moonwalk_readmem,moonwalk_writemem,moonwalk_init_machine, gfx4 )
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	// sys16B
/*TODO*///	static RomLoadPtr rom_passsht = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x020000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "epr11871.a4", 0x000000, 0x10000, 0x0f9ccea5 )
/*TODO*///		ROM_LOAD_ODD ( "epr11870.a1", 0x000000, 0x10000, 0xdf43ebcf )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "opr11854.b9",  0x00000, 0x10000, 0xd31c0b6c );
/*TODO*///		ROM_LOAD( "opr11855.b10", 0x10000, 0x10000, 0xb78762b4 );
/*TODO*///		ROM_LOAD( "opr11856.b11", 0x20000, 0x10000, 0xea49f666 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x60000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "opr11862.b1",  0x00000, 0x10000, 0xb6e94727 );
/*TODO*///		ROM_LOAD( "opr11865.b5",  0x10000, 0x10000, 0x17e8d5d5 );
/*TODO*///		ROM_LOAD( "opr11863.b2",  0x20000, 0x10000, 0x3e670098 );
/*TODO*///		ROM_LOAD( "opr11866.b6",  0x30000, 0x10000, 0x50eb71cc );
/*TODO*///		ROM_LOAD( "opr11864.b3",  0x40000, 0x10000, 0x05733ca8 );
/*TODO*///		ROM_LOAD( "opr11867.b7",  0x50000, 0x10000, 0x81e49697 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr11857.a7",  0x00000, 0x08000, 0x789edc06 );
/*TODO*///		ROM_LOAD( "epr11858.a8",  0x10000, 0x08000, 0x08ab0018 );
/*TODO*///		ROM_LOAD( "epr11859.a9",  0x18000, 0x08000, 0x8673e01b );
/*TODO*///		ROM_LOAD( "epr11860.a10", 0x20000, 0x08000, 0x10263746 );
/*TODO*///		ROM_LOAD( "epr11861.a11", 0x28000, 0x08000, 0x38b54a71 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_passht4b = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x020000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "pas4p.3", 0x000000, 0x10000, 0x2d8bc946 )
/*TODO*///		ROM_LOAD_ODD ( "pas4p.4", 0x000000, 0x10000, 0xe759e831 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "pas4p.11",  0x00000, 0x10000, 0xda20fbc9 );
/*TODO*///		ROM_LOAD( "pas4p.12", 0x10000, 0x10000, 0xbebb9211 );
/*TODO*///		ROM_LOAD( "pas4p.13", 0x20000, 0x10000, 0xe37506c3 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x60000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "opr11862.b1",  0x00000, 0x10000, 0xb6e94727 );
/*TODO*///		ROM_LOAD( "opr11865.b5",  0x10000, 0x10000, 0x17e8d5d5 );
/*TODO*///		ROM_LOAD( "opr11863.b2",  0x20000, 0x10000, 0x3e670098 );
/*TODO*///		ROM_LOAD( "opr11866.b6",  0x30000, 0x10000, 0x50eb71cc );
/*TODO*///		ROM_LOAD( "opr11864.b3",  0x40000, 0x10000, 0x05733ca8 );
/*TODO*///		ROM_LOAD( "opr11867.b7",  0x50000, 0x10000, 0x81e49697 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "pas4p.1",  0x00000, 0x08000, 0xe60fb017 );
/*TODO*///		ROM_LOAD( "pas4p.2",  0x10000, 0x10000, 0x092e016e );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_passshtb = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x020000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "pass3_2p.bin", 0x000000, 0x10000, 0x26bb9299 )
/*TODO*///		ROM_LOAD_ODD ( "pass4_2p.bin", 0x000000, 0x10000, 0x06ac6d5d )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "opr11854.b9",  0x00000, 0x10000, 0xd31c0b6c );
/*TODO*///		ROM_LOAD( "opr11855.b10", 0x10000, 0x10000, 0xb78762b4 );
/*TODO*///		ROM_LOAD( "opr11856.b11", 0x20000, 0x10000, 0xea49f666 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x60000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "opr11862.b1",  0x00000, 0x10000, 0xb6e94727 );
/*TODO*///		ROM_LOAD( "opr11865.b5",  0x10000, 0x10000, 0x17e8d5d5 );
/*TODO*///		ROM_LOAD( "opr11863.b2",  0x20000, 0x10000, 0x3e670098 );
/*TODO*///		ROM_LOAD( "opr11866.b6",  0x30000, 0x10000, 0x50eb71cc );
/*TODO*///		ROM_LOAD( "opr11864.b3",  0x40000, 0x10000, 0x05733ca8 );
/*TODO*///		ROM_LOAD( "opr11867.b7",  0x50000, 0x10000, 0x81e49697 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr11857.a7",  0x00000, 0x08000, 0x789edc06 );
/*TODO*///		ROM_LOAD( "epr11858.a8",  0x10000, 0x08000, 0x08ab0018 );
/*TODO*///		ROM_LOAD( "epr11859.a9",  0x18000, 0x08000, 0x8673e01b );
/*TODO*///		ROM_LOAD( "epr11860.a10", 0x20000, 0x08000, 0x10263746 );
/*TODO*///		ROM_LOAD( "epr11861.a11", 0x28000, 0x08000, 0x38b54a71 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static MemoryReadAddress passsht_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x01ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x440000, 0x440fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x840000, 0x840fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0xc41002, 0xc41003, input_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xc41004, 0xc41005, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0xc41000, 0xc41001, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xc42002, 0xc42003, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xc42000, 0xc42001, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xc40000, 0xc40fff, MRA_EXTRAM ),
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress passsht_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x01ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x440000, 0x440fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x840000, 0x840fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xc42006, 0xc42007, sound_command_w ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc40fff, MWA_EXTRAM ),
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static int passht4b_io1_val;
/*TODO*///	static int passht4b_io2_val;
/*TODO*///	static int passht4b_io3_val;
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr passht4b_service_r = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int val=io_service_r(offset);
/*TODO*///	
/*TODO*///		if(!(input_port_0_r(offset) & 0x40)) val&=0xef;
/*TODO*///		if(!(io_player2_r(offset) & 0x40)) val&=0xdf;
/*TODO*///		if(!(io_player3_r(offset) & 0x40)) val&=0xbf;
/*TODO*///		if(!(io_player4_r(offset) & 0x40)) val&=0x7f;
/*TODO*///	
/*TODO*///		passht4b_io3_val=(input_port_0_r(offset)<<4) | (io_player3_r(offset)&0xf);
/*TODO*///		passht4b_io2_val=(io_player2_r(offset)<<4) | (io_player4_r(offset)&0xf);
/*TODO*///	
/*TODO*///		passht4b_io1_val=0xff;
/*TODO*///	
/*TODO*///		// player 1 buttons
/*TODO*///		if(!(input_port_0_r(offset) & 0x10)) passht4b_io1_val &=0xfe;
/*TODO*///		if(!(input_port_0_r(offset) & 0x20)) passht4b_io1_val &=0xfd;
/*TODO*///		if(!(input_port_0_r(offset) & 0x80)) passht4b_io1_val &=0xfc;
/*TODO*///	
/*TODO*///		// player 2 buttons
/*TODO*///		if(!(io_player2_r(offset) & 0x10)) passht4b_io1_val &=0xfb;
/*TODO*///		if(!(io_player2_r(offset) & 0x20)) passht4b_io1_val &=0xf7;
/*TODO*///		if(!(io_player2_r(offset) & 0x80)) passht4b_io1_val &=0xf3;
/*TODO*///	
/*TODO*///		// player 3 buttons
/*TODO*///		if(!(io_player3_r(offset) & 0x10)) passht4b_io1_val &=0xef;
/*TODO*///		if(!(io_player3_r(offset) & 0x20)) passht4b_io1_val &=0xdf;
/*TODO*///		if(!(io_player3_r(offset) & 0x80)) passht4b_io1_val &=0xcf;
/*TODO*///	
/*TODO*///		// player 4 buttons
/*TODO*///		if(!(io_player4_r(offset) & 0x10)) passht4b_io1_val &=0xbf;
/*TODO*///		if(!(io_player4_r(offset) & 0x20)) passht4b_io1_val &=0x7f;
/*TODO*///		if(!(io_player4_r(offset) & 0x80)) passht4b_io1_val &=0x3f;
/*TODO*///	
/*TODO*///		return val;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr passht4b_io1_r = new ReadHandlerPtr() { public int handler(int offset){	return passht4b_io1_val;} };
/*TODO*///	public static ReadHandlerPtr passht4b_io2_r = new ReadHandlerPtr() { public int handler(int offset){	return passht4b_io2_val;} };
/*TODO*///	public static ReadHandlerPtr passht4b_io3_r = new ReadHandlerPtr() { public int handler(int offset){	return passht4b_io3_val;} };
/*TODO*///	
/*TODO*///	static MemoryReadAddress passht4b_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x01ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x440000, 0x440fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x840000, 0x840fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0xc41000, 0xc41001, passht4b_service_r ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0xc41002, 0xc41003, passht4b_io1_r ),
/*TODO*///		new MemoryReadAddress( 0xc41004, 0xc41005, passht4b_io2_r ),
/*TODO*///		new MemoryReadAddress( 0xc41006, 0xc41007, passht4b_io3_r ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0xc42002, 0xc42003, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xc42000, 0xc42001, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xc43000, 0xc43001, input_port_0_r ),		// test mode only
/*TODO*///		new MemoryReadAddress( 0xc43002, 0xc43003, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0xc43004, 0xc43005, io_player3_r ),
/*TODO*///		new MemoryReadAddress( 0xc43006, 0xc43007, io_player4_r ),
/*TODO*///		new MemoryReadAddress( 0xc4600a, 0xc4600b, MRA_EXTRAM ),
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress passht4b_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x01ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x440000, 0x440fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x840000, 0x840fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xc42006, 0xc42007, sound_command_w ),
/*TODO*///		new MemoryWriteAddress( 0xc4600a, 0xc4600b, MWA_EXTRAM ),
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void passsht_update_proc( void ){
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_workingram[0x34be] );
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_workingram[0x34c2] );
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_workingram[0x34bc] );
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_workingram[0x34c0] );
/*TODO*///	
/*TODO*///		set_fg_page( READ_WORD( &sys16_textram[0x0ff6] ) );
/*TODO*///		set_bg_page( READ_WORD( &sys16_textram[0x0ff4] ) );
/*TODO*///		set_refresh( READ_WORD( &sys16_extraram[0] ) );
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void passht4b_update_proc( void ){
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_workingram[0x34ce] );
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_workingram[0x34d2] );
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_workingram[0x34cc] );
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_workingram[0x34d0] );
/*TODO*///	
/*TODO*///		set_fg_page( READ_WORD( &sys16_textram[0x0ff6] ) );
/*TODO*///		set_bg_page( READ_WORD( &sys16_textram[0x0ff4] ) );
/*TODO*///		set_refresh( READ_WORD( &sys16_extraram[0] ) );
/*TODO*///	
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr passsht_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = { 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,3 };
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///	
/*TODO*///		sys16_sprxoffset = -0x48;
/*TODO*///		sys16_spritesystem = 0;
/*TODO*///	
/*TODO*///		// fix name entry
/*TODO*///		patch_code( 0x13a8,0xc0);
/*TODO*///	
/*TODO*///		sys16_update_proc = passsht_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitMachinePtr passht4b_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = { 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,3 };
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///	
/*TODO*///		sys16_sprxoffset = -0xb8;
/*TODO*///		sys16_spritesystem = 8;
/*TODO*///	
/*TODO*///		// fix name entry
/*TODO*///		patch_code( 0x138a,0xc0);
/*TODO*///	
/*TODO*///		sys16_update_proc = passht4b_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_passsht = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_sprite_decode( 3,0x20000 );
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_passht4b = new InitDriverPtr() { public void handler() {
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///	
/*TODO*///		/* invert the graphics bits on the tiles */
/*TODO*///		for (i = 0; i < 0x30000; i++)
/*TODO*///			memory_region(REGION_GFX1)[i] ^= 0xff;
/*TODO*///	
/*TODO*///		sys16_sprite_decode( 3,0x20000 );
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_passsht = new InputPortPtr(){ public void handler() { 
/*TODO*///	PORT_START();  /* joy 1 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON3 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON1 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON2 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON4 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
/*TODO*///	
/*TODO*///	PORT_START();  /* joy 2 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_COCKTAIL );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
/*TODO*///	
/*TODO*///		SYS16_SERVICE
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x0e, 0x0e, "Initial Point" );
/*TODO*///		PORT_DIPSETTING(    0x06, "2000" );
/*TODO*///		PORT_DIPSETTING(    0x0a, "3000" );
/*TODO*///		PORT_DIPSETTING(    0x0c, "4000" );
/*TODO*///		PORT_DIPSETTING(    0x0e, "5000" );
/*TODO*///		PORT_DIPSETTING(    0x08, "6000" );
/*TODO*///		PORT_DIPSETTING(    0x04, "7000" );
/*TODO*///		PORT_DIPSETTING(    0x02, "8000" );
/*TODO*///		PORT_DIPSETTING(    0x00, "9000" );
/*TODO*///		PORT_DIPNAME( 0x30, 0x30, "Point Table" );
/*TODO*///		PORT_DIPSETTING(    0x20, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x30, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x10, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x80, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0xc0, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x40, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_passht4b = new InputPortPtr(){ public void handler() { 
/*TODO*///	PORT_START();  /* joy 1 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON4 );
/*TODO*///	
/*TODO*///	PORT_START();  /* joy 2 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_COCKTAIL );
/*TODO*///	
/*TODO*///	PORT_START();  /* service */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN3 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START3 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START4 );
/*TODO*///	
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x0e, 0x0e, "Initial Point" );
/*TODO*///		PORT_DIPSETTING(    0x06, "2000" );
/*TODO*///		PORT_DIPSETTING(    0x0a, "3000" );
/*TODO*///		PORT_DIPSETTING(    0x0c, "4000" );
/*TODO*///		PORT_DIPSETTING(    0x0e, "5000" );
/*TODO*///		PORT_DIPSETTING(    0x08, "6000" );
/*TODO*///		PORT_DIPSETTING(    0x04, "7000" );
/*TODO*///		PORT_DIPSETTING(    0x02, "8000" );
/*TODO*///		PORT_DIPSETTING(    0x00, "9000" );
/*TODO*///		PORT_DIPNAME( 0x30, 0x30, "Point Table" );
/*TODO*///		PORT_DIPSETTING(    0x20, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x30, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x10, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x80, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0xc0, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x40, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///	
/*TODO*///	PORT_START();  /* joy 3 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER3 );
/*TODO*///	
/*TODO*///	PORT_START();  /* joy 4 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER4 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER4 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER4 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER4 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER4 );
/*TODO*///	
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_7759( machine_driver_passsht, \
/*TODO*///		passsht_readmem,passsht_writemem,passsht_init_machine, gfx1 ,upd7759_interface)
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_7759( machine_driver_passht4b, \
/*TODO*///		passht4b_readmem,passht4b_writemem,passht4b_init_machine, gfx1 ,upd7759_interface)
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	// pre16
/*TODO*///	static RomLoadPtr rom_quartet = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x030000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "epr7458a.9b",  0x000000, 0x8000, 0x42e7b23e )
/*TODO*///		ROM_LOAD_ODD ( "epr7455a.6b",  0x000000, 0x8000, 0x01631ab2 )
/*TODO*///		ROM_LOAD_EVEN( "epr7459a.10b", 0x010000, 0x8000, 0x6b540637 )
/*TODO*///		ROM_LOAD_ODD ( "epr7456a.7b",  0x010000, 0x8000, 0x31ca583e )
/*TODO*///		ROM_LOAD_EVEN( "epr7460.11b",  0x020000, 0x8000, 0xa444ea13 )
/*TODO*///		ROM_LOAD_ODD ( "epr7457.8b",   0x020000, 0x8000, 0x3b282c23 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "epr7461.9c",  0x00000, 0x08000, 0xf6af07f2 );
/*TODO*///		ROM_LOAD( "epr7462.10c", 0x08000, 0x08000, 0x7914af28 );
/*TODO*///		ROM_LOAD( "epr7463.11c", 0x10000, 0x08000, 0x827c5603 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x050000*2, REGION_GFX2 );/* sprites  - the same as quartet 2 */
/*TODO*///		ROM_LOAD( "epr7465.5c",  0x000000, 0x008000, 0x8a1ab7d7 );
/*TODO*///		ROM_RELOAD(              0x040000, 0x008000 );//twice? - fixes a sprite glitch
/*TODO*///		ROM_LOAD( "epr-7469.2b", 0x008000, 0x008000, 0xcb65ae4f );
/*TODO*///		ROM_RELOAD(              0x048000, 0x008000 );//twice?
/*TODO*///		ROM_LOAD( "epr7466.6c",  0x010000, 0x008000, 0xb2d3f4f3 );
/*TODO*///		ROM_LOAD( "epr-7470.3b", 0x018000, 0x008000, 0x16fc67b1 );
/*TODO*///		ROM_LOAD( "epr7467.7c",  0x020000, 0x008000, 0x0af68de2 );
/*TODO*///		ROM_LOAD( "epr-7471.4b", 0x028000, 0x008000, 0x13fad5ac );
/*TODO*///		ROM_LOAD( "epr7468.8c",  0x030000, 0x008000, 0xddfd40c0 );
/*TODO*///		ROM_LOAD( "epr-7472.5b", 0x038000, 0x008000, 0x8e2762ec );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr-7464.1b", 0x0000, 0x8000, 0x9f291306 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x1000, REGION_CPU3 );     /* 4k for 7751 onboard ROM */
/*TODO*///		ROM_LOAD( "7751.bin",     0x0000, 0x0400, 0x6a9534fc );/* 7751 - U34 */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_SOUND1 );/* 7751 sound data */
/*TODO*///		ROM_LOAD( "epr7473.1c", 0x00000, 0x8000, 0x06ec75fa );
/*TODO*///		ROM_LOAD( "epr7475.2c", 0x08000, 0x8000, 0x7abd1206 );
/*TODO*///		ROM_LOAD( "epr7474.3c", 0x10000, 0x8000, 0xdbf853b8 );
/*TODO*///		ROM_LOAD( "epr7476.4c", 0x18000, 0x8000, 0x5eba655a );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_quartetj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x030000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "epr-7458.43",  0x000000, 0x8000, 0x0096499f )
/*TODO*///		ROM_LOAD_ODD ( "epr-7455.26",  0x000000, 0x8000, 0xda934390 )
/*TODO*///		ROM_LOAD_EVEN( "epr-7459.42",  0x010000, 0x8000, 0xd130cf61 )
/*TODO*///		ROM_LOAD_ODD ( "epr-7456.25",  0x010000, 0x8000, 0x7847149f )
/*TODO*///		ROM_LOAD_EVEN( "epr7460.11b",  0x020000, 0x8000, 0xa444ea13 )
/*TODO*///		ROM_LOAD_ODD ( "epr7457.8b",   0x020000, 0x8000, 0x3b282c23 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "epr7461.9c",  0x00000, 0x08000, 0xf6af07f2 );
/*TODO*///		ROM_LOAD( "epr7462.10c", 0x08000, 0x08000, 0x7914af28 );
/*TODO*///		ROM_LOAD( "epr7463.11c", 0x10000, 0x08000, 0x827c5603 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x050000*2, REGION_GFX2 );/* sprites  - the same as quartet 2 */
/*TODO*///		ROM_LOAD( "epr7465.5c",  0x000000, 0x008000, 0x8a1ab7d7 );
/*TODO*///		ROM_RELOAD(              0x040000, 0x008000 );//twice? - fixes a sprite glitch
/*TODO*///		ROM_LOAD( "epr-7469.2b", 0x008000, 0x008000, 0xcb65ae4f );
/*TODO*///		ROM_RELOAD(              0x048000, 0x008000 );//twice?
/*TODO*///		ROM_LOAD( "epr7466.6c",  0x010000, 0x008000, 0xb2d3f4f3 );
/*TODO*///		ROM_LOAD( "epr-7470.3b", 0x018000, 0x008000, 0x16fc67b1 );
/*TODO*///		ROM_LOAD( "epr7467.7c",  0x020000, 0x008000, 0x0af68de2 );
/*TODO*///		ROM_LOAD( "epr-7471.4b", 0x028000, 0x008000, 0x13fad5ac );
/*TODO*///		ROM_LOAD( "epr7468.8c",  0x030000, 0x008000, 0xddfd40c0 );
/*TODO*///		ROM_LOAD( "epr-7472.5b", 0x038000, 0x008000, 0x8e2762ec );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr-7464.1b", 0x0000, 0x8000, 0x9f291306 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x1000, REGION_CPU3 );     /* 4k for 7751 onboard ROM */
/*TODO*///		ROM_LOAD( "7751.bin",     0x0000, 0x0400, 0x6a9534fc );/* 7751 - U34 */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_SOUND1 );/* 7751 sound data */
/*TODO*///		ROM_LOAD( "epr7473.1c", 0x00000, 0x8000, 0x06ec75fa );
/*TODO*///		ROM_LOAD( "epr7475.2c", 0x08000, 0x8000, 0x7abd1206 );
/*TODO*///		ROM_LOAD( "epr7474.3c", 0x10000, 0x8000, 0xdbf853b8 );
/*TODO*///		ROM_LOAD( "epr7476.4c", 0x18000, 0x8000, 0x5eba655a );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr quartet_skip = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		if (cpu_get_pc()==0x89b2) {cpu_spinuntil_int(); return 0xffff;}
/*TODO*///	
/*TODO*///		return READ_WORD(&sys16_workingram[0x0800]);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr io_quartet_p1_r = new ReadHandlerPtr() { public int handler(int offset){return input_port_0_r( offset );} };
/*TODO*///	public static ReadHandlerPtr io_quartet_p2_r = new ReadHandlerPtr() { public int handler(int offset){return input_port_1_r( offset );} };
/*TODO*///	public static ReadHandlerPtr io_quartet_p3_r = new ReadHandlerPtr() { public int handler(int offset){return input_port_2_r( offset );} };
/*TODO*///	public static ReadHandlerPtr io_quartet_p4_r = new ReadHandlerPtr() { public int handler(int offset){return input_port_3_r( offset );} };
/*TODO*///	public static ReadHandlerPtr io_quartet_dip1_r = new ReadHandlerPtr() { public int handler(int offset){return input_port_4_r( offset );} };
/*TODO*///	public static ReadHandlerPtr io_quartet_dip2_r = new ReadHandlerPtr() { public int handler(int offset){return input_port_5_r( offset );} };
/*TODO*///	
/*TODO*///	static MemoryReadAddress quartet_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x02ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x440000, 0x440fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x840000, 0x840fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0xc41000, 0xc41001, io_quartet_p1_r ),
/*TODO*///		new MemoryReadAddress( 0xc41002, 0xc41003, io_quartet_p2_r ),
/*TODO*///		new MemoryReadAddress( 0xc41004, 0xc41005, io_quartet_p3_r ),
/*TODO*///		new MemoryReadAddress( 0xc41006, 0xc41007, io_quartet_p4_r ),
/*TODO*///		new MemoryReadAddress( 0xc42000, 0xc42001, io_quartet_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xc42002, 0xc42003, io_quartet_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xc40000, 0xc4ffff, MRA_EXTRAM ),
/*TODO*///		new MemoryReadAddress( 0xffc800, 0xffc801, quartet_skip ),
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress quartet_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x02ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x440000, 0x440fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x840000, 0x840fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc40001, sound_command_nmi_w ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc4ffff, MWA_EXTRAM ),
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void quartet_update_proc( void ){
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_workingram[0x0d14] ) & 0x01ff;
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_workingram[0x0d18] ) & 0x01ff;
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0f24] ) & 0x00ff;
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0f26] ) & 0x01ff;
/*TODO*///	
/*TODO*///		if((READ_WORD(&sys16_extraram[4]) & 0xff) == 1)
/*TODO*///			sys16_quartet_title_kludge=1;
/*TODO*///		else
/*TODO*///			sys16_quartet_title_kludge=0;
/*TODO*///	
/*TODO*///		set_fg_page1( READ_WORD( &sys16_workingram[0x0d1c] ) );
/*TODO*///		set_bg_page1( READ_WORD( &sys16_workingram[0x0d1e] ) );
/*TODO*///	
/*TODO*///		set_refresh_3d( READ_WORD( &sys16_extraram[2] ) );
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr quartet_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = { 00,01,02,03,00,01,02,03,00,01,02,03,00,01,02,03};
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_textmode=1;
/*TODO*///		sys16_spritesystem = 2;
/*TODO*///		sys16_sprxoffset = -0xbc;
/*TODO*///		sys16_fgxoffset = sys16_bgxoffset = 7;
/*TODO*///	
/*TODO*///		sys16_update_proc = quartet_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_quartet = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_sprite_decode( 5,0x010000 );
/*TODO*///	} };
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_quartet = new InputPortPtr(){ public void handler() { 
/*TODO*///		// Player 1
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY  );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP  | IPF_8WAY  );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* player 1 coin 2 really */
/*TODO*///		// Player 2
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY  | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP  | IPF_8WAY  | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* player 2 coin 2 really */
/*TODO*///		// Player 3
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3  );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP  | IPF_8WAY  | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START3 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN3 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* player 3 coin 2 really */
/*TODO*///		// Player 4
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY  | IPF_PLAYER4 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP  | IPF_8WAY  | IPF_PLAYER4 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4);
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER4);
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4);
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START4 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4);
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN4 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* player 4 coin 2 really */
/*TODO*///	
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///		PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x06, 0x00, "Credit Power" );
/*TODO*///		PORT_DIPSETTING(    0x04, "500" );
/*TODO*///		PORT_DIPSETTING(    0x06, "1000" );
/*TODO*///		PORT_DIPSETTING(    0x02, "2000" );
/*TODO*///		PORT_DIPSETTING(    0x00, "9000" );
/*TODO*///		PORT_DIPNAME( 0x18, 0x00, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x10, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x18, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x08, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, "Coin During Game" );
/*TODO*///		PORT_DIPSETTING(    0x20, "Power" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Credit" );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Free_Play") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_7751( machine_driver_quartet, \
/*TODO*///		quartet_readmem,quartet_writemem,quartet_init_machine, gfx8 )
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	// pre16
/*TODO*///	static RomLoadPtr rom_quartet2 = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x030000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "quartet2.b9",  0x000000, 0x8000, 0x67177cd8 )
/*TODO*///		ROM_LOAD_ODD ( "quartet2.b6",  0x000000, 0x8000, 0x50f50b08 )
/*TODO*///		ROM_LOAD_EVEN( "quartet2.b10", 0x010000, 0x8000, 0x4273c3b7 )
/*TODO*///		ROM_LOAD_ODD ( "quartet2.b7",  0x010000, 0x8000, 0x0aa337bb )
/*TODO*///		ROM_LOAD_EVEN( "quartet2.b11", 0x020000, 0x8000, 0x3a6a375d )
/*TODO*///		ROM_LOAD_ODD ( "quartet2.b8",  0x020000, 0x8000, 0xd87b2ca2 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "quartet2.c9",  0x00000, 0x08000, 0x547a6058 );
/*TODO*///		ROM_LOAD( "quartet2.c10", 0x08000, 0x08000, 0x77ec901d );
/*TODO*///		ROM_LOAD( "quartet2.c11", 0x10000, 0x08000, 0x7e348cce );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x050000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "epr7465.5c",  0x000000, 0x008000, 0x8a1ab7d7 );
/*TODO*///		ROM_RELOAD(              0x040000, 0x008000 );//twice? - fixes a sprite glitch
/*TODO*///		ROM_LOAD( "epr-7469.2b", 0x008000, 0x008000, 0xcb65ae4f );
/*TODO*///		ROM_RELOAD(              0x048000, 0x008000 );//twice?
/*TODO*///		ROM_LOAD( "epr7466.6c",  0x010000, 0x008000, 0xb2d3f4f3 );
/*TODO*///		ROM_LOAD( "epr-7470.3b", 0x018000, 0x008000, 0x16fc67b1 );
/*TODO*///		ROM_LOAD( "epr7467.7c",  0x020000, 0x008000, 0x0af68de2 );
/*TODO*///		ROM_LOAD( "epr-7471.4b", 0x028000, 0x008000, 0x13fad5ac );
/*TODO*///		ROM_LOAD( "epr7468.8c",  0x030000, 0x008000, 0xddfd40c0 );
/*TODO*///		ROM_LOAD( "epr-7472.5b", 0x038000, 0x008000, 0x8e2762ec );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr-7464.1b", 0x0000, 0x8000, 0x9f291306 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x1000, REGION_CPU3 );     /* 4k for 7751 onboard ROM */
/*TODO*///		ROM_LOAD( "7751.bin",     0x0000, 0x0400, 0x6a9534fc );/* 7751 - U34 */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_SOUND1 );/* 7751 sound data */
/*TODO*///		ROM_LOAD( "epr7473.1c", 0x00000, 0x8000, 0x06ec75fa );
/*TODO*///		ROM_LOAD( "epr7475.2c", 0x08000, 0x8000, 0x7abd1206 );
/*TODO*///		ROM_LOAD( "epr7474.3c", 0x10000, 0x8000, 0xdbf853b8 );
/*TODO*///		ROM_LOAD( "epr7476.4c", 0x18000, 0x8000, 0x5eba655a );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr quartet2_skip = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		if (cpu_get_pc()==0x8f6c) {cpu_spinuntil_int(); return 0xffff;}
/*TODO*///	
/*TODO*///		return READ_WORD(&sys16_workingram[0x0800]);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static MemoryReadAddress quartet2_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x02ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x440000, 0x440fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x840000, 0x840fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0xc41002, 0xc41003, input_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xc41006, 0xc41007, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0xc41000, 0xc41001, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xc42000, 0xc42001, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xc42002, 0xc42003, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xc40000, 0xc4ffff, MRA_EXTRAM ),
/*TODO*///		new MemoryReadAddress( 0xffc800, 0xffc801, quartet2_skip ),
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress quartet2_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x02ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x440000, 0x440fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x840000, 0x840fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc40001, sound_command_nmi_w ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc4ffff, MWA_EXTRAM ),
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void quartet2_update_proc( void ){
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_workingram[0x0d14] ) & 0x01ff;
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_workingram[0x0d18] ) & 0x01ff;
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0f24] ) & 0x00ff;
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0f26] ) & 0x01ff;
/*TODO*///	
/*TODO*///		if((READ_WORD(&sys16_extraram[4]) & 0xff) == 1)
/*TODO*///			sys16_quartet_title_kludge=1;
/*TODO*///		else
/*TODO*///			sys16_quartet_title_kludge=0;
/*TODO*///	
/*TODO*///		set_fg_page1( READ_WORD( &sys16_workingram[0x0d1c] ) );
/*TODO*///		set_bg_page1( READ_WORD( &sys16_workingram[0x0d1e] ) );
/*TODO*///	
/*TODO*///		set_refresh_3d( READ_WORD( &sys16_extraram[2] ) );
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr quartet2_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = { 00,01,02,03,00,01,02,03,00,01,02,03,00,01,02,03};
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_textmode=1;
/*TODO*///		sys16_spritesystem = 2;
/*TODO*///		sys16_sprxoffset = -0xbc;
/*TODO*///		sys16_fgxoffset = sys16_bgxoffset = 7;
/*TODO*///	
/*TODO*///		sys16_update_proc = quartet2_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_quartet2 = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_sprite_decode( 5,0x010000 );
/*TODO*///	} };
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_quartet2 = new InputPortPtr(){ public void handler() { 
/*TODO*///		SYS16_JOY1_SWAPPEDBUTTONS
/*TODO*///		SYS16_JOY2_SWAPPEDBUTTONS
/*TODO*///		SYS16_SERVICE
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x06, 0x00, "Credit Power" );
/*TODO*///		PORT_DIPSETTING(    0x04, "500" );
/*TODO*///		PORT_DIPSETTING(    0x06, "1000" );
/*TODO*///		PORT_DIPSETTING(    0x02, "2000" );
/*TODO*///		PORT_DIPSETTING(    0x00, "9000" );
/*TODO*///		PORT_DIPNAME( 0x18, 0x00, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x10, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x18, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x08, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unused") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unused") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_7751( machine_driver_quartet2, \
/*TODO*///		quartet2_readmem,quartet2_writemem,quartet2_init_machine, gfx8 )
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///	
/*TODO*///	   Riot City
/*TODO*///	
/*TODO*///	***************************************************************************/
/*TODO*///	// sys16B
/*TODO*///	static RomLoadPtr rom_riotcity = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0xc0000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "epr14612.bin", 0x000000, 0x20000, 0xa1b331ec )
/*TODO*///		ROM_LOAD_ODD ( "epr14610.bin", 0x000000, 0x20000, 0xcd4f2c50 )
/*TODO*///		/* empty 0x40000 - 0x80000 */
/*TODO*///		ROM_LOAD_EVEN( "epr14613.bin", 0x080000, 0x20000, 0x0659df4c )
/*TODO*///		ROM_LOAD_ODD ( "epr14611.bin", 0x080000, 0x20000, 0xd9e6f80b )
/*TODO*///	
/*TODO*///		ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "epr14616.bin", 0x00000, 0x20000, 0x46d30368 );/* plane 1 */
/*TODO*///		ROM_LOAD( "epr14625.bin", 0x20000, 0x20000, 0xabfb80fe );
/*TODO*///		ROM_LOAD( "epr14617.bin", 0x40000, 0x20000, 0x884e40f9 );/* plane 2 */
/*TODO*///		ROM_LOAD( "epr14626.bin", 0x60000, 0x20000, 0x4ef55846 );
/*TODO*///		ROM_LOAD( "epr14618.bin", 0x80000, 0x20000, 0x00eb260e );/* plane 3 */
/*TODO*///		ROM_LOAD( "epr14627.bin", 0xa0000, 0x20000, 0x961e5f82 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x180000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "epr14619.bin",  0x000000, 0x040000, 0x6f2b5ef7 );
/*TODO*///		ROM_LOAD( "epr14622.bin",  0x040000, 0x040000, 0x7ca7e40d );
/*TODO*///		ROM_LOAD( "epr14620.bin",  0x080000, 0x040000, 0x66183333 );
/*TODO*///		ROM_LOAD( "epr14623.bin",  0x0c0000, 0x040000, 0x98630049 );
/*TODO*///		ROM_LOAD( "epr14621.bin",  0x100000, 0x040000, 0xc0f2820e );
/*TODO*///		ROM_LOAD( "epr14624.bin",  0x140000, 0x040000, 0xd1a68448 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr14614.bin", 0x00000, 0x10000, 0xc65cc69a );
/*TODO*///		ROM_LOAD( "epr14615.bin", 0x10000, 0x20000, 0x46653db1 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr riotcity_skip = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		if (cpu_get_pc()==0x3ce) {cpu_spinuntil_int(); return 0;}
/*TODO*///	
/*TODO*///		return READ_WORD(&sys16_workingram[0x2cde]);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static MemoryReadAddress riotcity_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x0bffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x3f0000, 0x3fffff, MRA_EXTRAM ),
/*TODO*///		new MemoryReadAddress( 0xf20000, 0xf20fff, MRA_EXTRAM3 ),
/*TODO*///		new MemoryReadAddress( 0xf40000, 0xf40fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0xf60000, 0xf60fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0xf81002, 0xf81003, input_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xf81006, 0xf81007, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0xf81000, 0xf81001, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xf82002, 0xf82003, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xf82000, 0xf82001, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xf80000, 0xf8ffff, MRA_EXTRAM2 ),
/*TODO*///		new MemoryReadAddress( 0xfa0000, 0xfaffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0xfb0000, 0xfb0fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0xffecde, 0xffecdf, riotcity_skip ),
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress riotcity_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x0bffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x3f0000, 0x3fffff, MWA_EXTRAM ),
/*TODO*///		new MemoryWriteAddress( 0xf00006, 0xf00007, sound_command_w ),
/*TODO*///		new MemoryWriteAddress( 0xf20000, 0xf20fff, MWA_EXTRAM3 ),
/*TODO*///		new MemoryWriteAddress( 0xf40000, 0xf40fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0xf60000, 0xf60fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xf80000, 0xf8ffff, MWA_BANK4,sys16_extraram2 ),
/*TODO*///		new MemoryWriteAddress( 0xfa0000, 0xfaffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0xfb0000, 0xfb0fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void riotcity_update_proc (void)
/*TODO*///	{
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0e98] );
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0e9a] );
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0e90] );
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0e92] );
/*TODO*///	
/*TODO*///		set_fg_page( READ_WORD( &sys16_textram[0x0e80] ) );
/*TODO*///		set_bg_page( READ_WORD( &sys16_textram[0x0e82] ) );
/*TODO*///	
/*TODO*///		sys16_tile_bank1 = READ_WORD( &sys16_extraram3[0x0002] ) & 0xf;
/*TODO*///		sys16_tile_bank0 = READ_WORD( &sys16_extraram3[0x0000] ) & 0xf;
/*TODO*///	
/*TODO*///		set_refresh( READ_WORD( &sys16_extraram2[0] ) );
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr riotcity_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = {0x00,0x02,0x08,0x0A,0x10,0x12,0x00,0x00,0x04,0x06,0x0C,0x0E,0x14,0x16,0x00,0x00};
/*TODO*///	
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_spritesystem = 4;
/*TODO*///		sys16_spritelist_end=0x8000;
/*TODO*///		sys16_bg_priority_mode=1;
/*TODO*///	
/*TODO*///		sys16_update_proc = riotcity_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_riotcity = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_sprite_decode (3,0x80000);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_riotcity = new InputPortPtr(){ public void handler() { 
/*TODO*///		SYS16_JOY1
/*TODO*///		SYS16_JOY2
/*TODO*///		SYS16_SERVICE
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x01, "2 Credits to Start" );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x0c, 0x08, DEF_STR( "Lives") );
/*TODO*///		PORT_DIPSETTING(    0x00, "1" );
/*TODO*///		PORT_DIPSETTING(    0x0c, "2" );
/*TODO*///		PORT_DIPSETTING(    0x08, "3" );
/*TODO*///		PORT_DIPSETTING(    0x04, "4" );
/*TODO*///		PORT_DIPNAME( 0x30, 0x00, DEF_STR( "Bonus_Life") );
/*TODO*///		PORT_DIPSETTING(    0x20, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x30, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x10, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x40, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hard" );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, "Attack Button to Start" );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_7759( machine_driver_riotcity, \
/*TODO*///		riotcity_readmem,riotcity_writemem,riotcity_init_machine, gfx4,upd7759_interface )
/*TODO*///	
    /**
     * ************************************************************************
     */
    // sys16B
    static RomLoadPtr rom_sdi = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x030000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("a4.rom", 0x000000, 0x8000, 0xf2c41dd6);
            ROM_LOAD_ODD("a1.rom", 0x000000, 0x8000, 0xa9f816ef);
            ROM_LOAD_EVEN("a5.rom", 0x010000, 0x8000, 0x7952e27e);
            ROM_LOAD_ODD("a2.rom", 0x010000, 0x8000, 0x369af326);
            ROM_LOAD_EVEN("a6.rom", 0x020000, 0x8000, 0x8ee2c287);
            ROM_LOAD_ODD("a3.rom", 0x020000, 0x8000, 0x193e4231);

            ROM_REGION(0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* tiles */
            ROM_LOAD("b9.rom", 0x00000, 0x10000, 0x182b6301);
            ROM_LOAD("b10.rom", 0x10000, 0x10000, 0x8f7129a2);
            ROM_LOAD("b11.rom", 0x20000, 0x10000, 0x4409411f);

            ROM_REGION(0x060000 * 2, REGION_GFX2);/* sprites */
            ROM_LOAD("b1.rom", 0x000000, 0x010000, 0x30e2c50a);
            ROM_LOAD("b5.rom", 0x010000, 0x010000, 0x794e3e8b);
            ROM_LOAD("b2.rom", 0x020000, 0x010000, 0x6a8b3fd0);
            ROM_LOAD("b6.rom", 0x030000, 0x010000, 0x602da5d5);
            ROM_LOAD("b3.rom", 0x040000, 0x010000, 0xb9de3aeb);
            ROM_LOAD("b7.rom", 0x050000, 0x010000, 0x0a73a057);

            ROM_REGION(0x10000, REGION_CPU2);/* sound CPU */
            ROM_LOAD("a7.rom", 0x0000, 0x8000, 0x793f9f7f);
            ROM_END();
        }
    };

    // sys16A
    static RomLoadPtr rom_sdioj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x030000, REGION_CPU1);/* 68000 code */
            // Custom cpu 317-0027
            ROM_LOAD_EVEN("epr10970.43", 0x000000, 0x8000, 0xb8fa4a2c);
            ROM_LOAD_ODD("epr10968.26", 0x000000, 0x8000, 0xa3f97793);
            ROM_LOAD_EVEN("epr10971.42", 0x010000, 0x8000, 0xc44a0328);
            ROM_LOAD_ODD("epr10969.25", 0x010000, 0x8000, 0x455d15bd);
            ROM_LOAD_EVEN("epr10755.41", 0x020000, 0x8000, 0x405e3969);
            ROM_LOAD_ODD("epr10752.24", 0x020000, 0x8000, 0x77453740);

            ROM_REGION(0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* tiles */
            ROM_LOAD("epr10756.95", 0x00000, 0x10000, 0x44d8a506);
            ROM_LOAD("epr10757.94", 0x10000, 0x10000, 0x497e1740);
            ROM_LOAD("epr10758.93", 0x20000, 0x10000, 0x61d61486);

            ROM_REGION(0x060000 * 2, REGION_GFX2);/* sprites */
            ROM_LOAD("b1.rom", 0x000000, 0x010000, 0x30e2c50a);
            ROM_LOAD("b5.rom", 0x010000, 0x010000, 0x794e3e8b);
            ROM_LOAD("b2.rom", 0x020000, 0x010000, 0x6a8b3fd0);
            ROM_LOAD("b6.rom", 0x030000, 0x010000, 0x602da5d5);
            ROM_LOAD("b3.rom", 0x040000, 0x010000, 0xb9de3aeb);
            ROM_LOAD("b7.rom", 0x050000, 0x010000, 0x0a73a057);

            ROM_REGION(0x10000, REGION_CPU2);/* sound CPU */
            ROM_LOAD("epr10759.12", 0x0000, 0x8000, 0xd7f9649f);
            ROM_END();
        }
    };

    /**
     * ************************************************************************
     */
    public static ReadHandlerPtr io_p1mousex_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return 0xff - input_port_5_r.handler(offset);
        }
    };
    public static ReadHandlerPtr io_p1mousey_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return input_port_6_r.handler(offset);
        }
    };

    public static ReadHandlerPtr io_p2mousex_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return input_port_7_r.handler(offset);
        }
    };
    public static ReadHandlerPtr io_p2mousey_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return input_port_8_r.handler(offset);
        }
    };

    public static ReadHandlerPtr sdi_skip = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x5326) {
                cpu_spinuntil_int();
                return 0xffff;
            }

            return sys16_workingram.READ_WORD(0x0400);
        }
    };

    static MemoryReadAddress sdi_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x02ffff, MRA_ROM),
                new MemoryReadAddress(0x400000, 0x40ffff, sys16_tileram_r),
                new MemoryReadAddress(0x410000, 0x410fff, sys16_textram_r),
                new MemoryReadAddress(0x440000, 0x440fff, MRA_BANK2),
                new MemoryReadAddress(0x840000, 0x840fff, paletteram_word_r),
                new MemoryReadAddress(0xc40000, 0xc40001, MRA_BANK3),
                new MemoryReadAddress(0xc41004, 0xc41005, input_port_0_r),
                new MemoryReadAddress(0xc41002, 0xc41003, input_port_1_r),
                new MemoryReadAddress(0xc41000, 0xc41001, input_port_2_r),
                new MemoryReadAddress(0xc42000, 0xc42001, input_port_4_r),
                new MemoryReadAddress(0xc42002, 0xc42003, input_port_3_r),
                new MemoryReadAddress(0xc42004, 0xc42005, input_port_4_r),
                new MemoryReadAddress(0xc43000, 0xc43001, io_p1mousex_r),
                new MemoryReadAddress(0xc43004, 0xc43005, io_p1mousey_r),
                new MemoryReadAddress(0xc43008, 0xc43009, io_p2mousex_r),
                new MemoryReadAddress(0xc4300c, 0xc4300d, io_p2mousey_r),
                //	new MemoryReadAddress( 0xc42000, 0xc42001, MRA_NOP ), /* What is this? */
                new MemoryReadAddress(0xc60000, 0xc60001, MRA_NOP), /* What is this? */
                new MemoryReadAddress(0xffc400, 0xffc401, sdi_skip),
                new MemoryReadAddress(0xffc000, 0xffffff, MRA_BANK1),
                new MemoryReadAddress(-1)
            };

    static MemoryWriteAddress sdi_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x02ffff, MWA_ROM),
                new MemoryWriteAddress(0x123406, 0x123407, sound_command_w),
                new MemoryWriteAddress(0x400000, 0x40ffff, sys16_tileram_w, sys16_tileram),
                new MemoryWriteAddress(0x410000, 0x410fff, sys16_textram_w, sys16_textram),
                new MemoryWriteAddress(0x440000, 0x440fff, MWA_BANK2, sys16_spriteram),
                new MemoryWriteAddress(0x840000, 0x840fff, sys16_paletteram_w, paletteram),
                new MemoryWriteAddress(0xc40000, 0xc40001, MWA_BANK3, sys16_extraram),
                new MemoryWriteAddress(0xffc000, 0xffffff, MWA_BANK1, sys16_workingram),
                new MemoryWriteAddress(-1)
            };
    /**
     * ************************************************************************
     */
    public static sys16_update_procPtr sdi_update_proc = new sys16_update_procPtr() {
        public void handler() {
            sys16_fg_scrollx = sys16_textram.READ_WORD(0x0e98);
            sys16_bg_scrollx = sys16_textram.READ_WORD(0x0e9a);
            sys16_fg_scrolly = sys16_textram.READ_WORD(0x0e90);
            sys16_bg_scrolly = sys16_textram.READ_WORD(0x0e92);

            set_fg_page(sys16_textram.READ_WORD(0x0e80));
            set_bg_page(sys16_textram.READ_WORD(0x0e82));

            set_refresh(sys16_extraram.READ_WORD(0));
        }
    };

    public static InitMachinePtr sdi_init_machine = new InitMachinePtr() {
        public void handler() {
            int bank[] = {00, 00, 00, 00, 00, 00, 00, 0x06, 00, 00, 00, 0x04, 00, 0x02, 00, 00};

            sys16_obj_bank = bank;

            // ??
            patch_code.handler(0x102f2, 0x00);
            patch_code.handler(0x102f3, 0x02);

            sys16_update_proc = sdi_update_proc;
        }
    };

    static void sdi_sprite_decode() {
        sys16_sprite_decode(3, 0x020000);
    }

    public static InitDriverPtr init_sdi = new InitDriverPtr() {
        public void handler() {
            sys16_onetime_init_machine.handler();
            sys18_splittab_bg_x = new UBytePtr(sys16_textram, 0x0fc0);
            sys16_rowscroll_scroll = 0xff00;

            sdi_sprite_decode();
        }
    };

    /**
     * ************************************************************************
     */
    static InputPortPtr input_ports_sdi = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* DSW1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT | IPF_8WAY | IPF_PLAYER2);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();
            /* Service */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);

            PORT_START();
            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x07, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x09, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x05, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x04, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x02, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0x03, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x06, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x00, "Free Play (if Coin B too) or 1/1");
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x70, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x90, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x50, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x40, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x10, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x20, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0x30, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x60, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0xa0, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x00, "Free Play (if Coin A too) or 1/1");

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unused"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Lives"));
            PORT_DIPSETTING(0x08, "2");
            PORT_DIPSETTING(0x0c, "3");
            PORT_DIPSETTING(0x04, "4");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "240?", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x20, "Easy");
            PORT_DIPSETTING(0x30, "Normal");
            PORT_DIPSETTING(0x10, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0xc0, 0xc0, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x80, "Every 50000");
            PORT_DIPSETTING(0xc0, "50000");
            PORT_DIPSETTING(0x40, "100000");
            PORT_DIPSETTING(0x00, "None");

            PORT_START();
            /* fake analog X */
            PORT_ANALOG(0xff, 0x80, IPT_TRACKBALL_X, 75, 1, 0, 255);

            PORT_START();
            /* fake analog Y */
            PORT_ANALOG(0xff, 0x80, IPT_TRACKBALL_Y, 75, 1, 0, 255);

            PORT_START();
            /* fake analog X */
            PORT_ANALOG(0xff, 0x80, IPT_TRACKBALL_X | IPF_PLAYER2, 75, 1, 0, 255);

            PORT_START();
            /* fake analog Y */
            PORT_ANALOG(0xff, 0x80, IPT_TRACKBALL_Y | IPF_PLAYER2, 75, 1, 0, 255);

            INPUT_PORTS_END();
        }
    };

    /**
     * ************************************************************************
     */
    static MachineDriver machine_driver_sdi = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000,
                        sdi_readmem, sdi_writemem, null, null,
                        sys16_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4096000,
                        sound_readmem, sound_writemem, sound_readport, sound_writeport,
                        ignore_interrupt, 1
                ),},
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1,
            sdi_init_machine,
            40 * 8, 28 * 8, new rectangle(0 * 8, 40 * 8 - 1, 0 * 8, 28 * 8 - 1),
            gfx1,
            2048 * ShadowColorsMultiplier, 2048 * ShadowColorsMultiplier,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            sys16_vh_start,
            sys16_vh_stop,
            sys16_vh_screenrefresh,
            SOUND_SUPPORTS_STEREO, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface
                )
            }
    );
    /**
     * ************************************************************************
     */
    /*TODO*///	// sys18
/*TODO*///	static RomLoadPtr rom_shdancer = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x080000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "shdancer.a6", 0x000000, 0x40000, 0x3d5b3fa9 )
/*TODO*///		ROM_LOAD_ODD ( "shdancer.a5", 0x000000, 0x40000, 0x2596004e )
/*TODO*///	
/*TODO*///		ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "sd12712.bin", 0x00000, 0x40000, 0x9bdabe3d );
/*TODO*///		ROM_LOAD( "sd12713.bin", 0x40000, 0x40000, 0x852d2b1c );
/*TODO*///		ROM_LOAD( "sd12714.bin", 0x80000, 0x40000, 0x448226ce );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "sd12719.bin",  0x000000, 0x40000, 0xd6888534 );
/*TODO*///		ROM_LOAD( "sd12726.bin",  0x040000, 0x40000, 0xff344945 );
/*TODO*///		ROM_LOAD( "sd12718.bin",  0x080000, 0x40000, 0xba2efc0c );
/*TODO*///		ROM_LOAD( "sd12725.bin",  0x0c0000, 0x40000, 0x268a0c17 );
/*TODO*///		ROM_LOAD( "sd12717.bin",  0x100000, 0x40000, 0xc81cc4f8 );
/*TODO*///		ROM_LOAD( "sd12724.bin",  0x140000, 0x40000, 0x0f4903dc );
/*TODO*///		ROM_LOAD( "sd12716.bin",  0x180000, 0x40000, 0xa870e629 );
/*TODO*///		ROM_LOAD( "sd12723.bin",  0x1c0000, 0x40000, 0xc606cf90 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x70000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "sd12720.bin", 0x10000, 0x20000, 0x7a0d8de1 );
/*TODO*///		ROM_LOAD( "sd12715.bin", 0x30000, 0x40000, 0x07051a52 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr shdancer_skip = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		if (cpu_get_pc()==0x2f76) {cpu_spinuntil_int(); return 0xffff;}
/*TODO*///	
/*TODO*///		return READ_WORD(&sys16_workingram[0x0000]);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	static MemoryReadAddress shdancer_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x440000, 0x440fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x840000, 0x840fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0xc00000, 0xc00007, MRA_EXTRAM ),
/*TODO*///		new MemoryReadAddress( 0xe4000a, 0xe4000b, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xe4000c, 0xe4000d, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xe40000, 0xe40001, input_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xe40002, 0xe40003, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0xe40008, 0xe40009, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xe40000, 0xe4001f, MRA_EXTRAM2 ),
/*TODO*///		new MemoryReadAddress( 0xe43034, 0xe43035, MRA_NOP ),
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress shdancer_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x440000, 0x440fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x840000, 0x840fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xc00000, 0xc00007, MWA_EXTRAM ),
/*TODO*///		new MemoryWriteAddress( 0xe40000, 0xe4001f, MWA_BANK4,sys16_extraram2 ),
/*TODO*///		new MemoryWriteAddress( 0xe43034, 0xe43035, MWA_NOP ),
/*TODO*///		new MemoryWriteAddress( 0xfe0006, 0xfe0007, sound_command_nmi_w ),
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void shdancer_update_proc( void ){
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0e98] );
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0e9a] );
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0e90] );
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0e92] );
/*TODO*///	
/*TODO*///		set_fg_page( READ_WORD( &sys16_textram[0x0e80] ) );
/*TODO*///		set_bg_page( READ_WORD( &sys16_textram[0x0e82] ) );
/*TODO*///	
/*TODO*///		sys16_fg2_scrollx = READ_WORD( &sys16_textram[0x0e9c] );
/*TODO*///		sys16_bg2_scrollx = READ_WORD( &sys16_textram[0x0e9e] );
/*TODO*///		sys16_fg2_scrolly = READ_WORD( &sys16_textram[0x0e94] );
/*TODO*///		sys16_bg2_scrolly = READ_WORD( &sys16_textram[0x0e96] );
/*TODO*///	
/*TODO*///		set_fg2_page( READ_WORD( &sys16_textram[0x0e84] ) );
/*TODO*///		set_bg2_page( READ_WORD( &sys16_textram[0x0e86] ) );
/*TODO*///	
/*TODO*///		sys18_bg2_active=0;
/*TODO*///		sys18_fg2_active=0;
/*TODO*///	
/*TODO*///		if(sys16_fg2_scrollx | sys16_fg2_scrolly | READ_WORD( &sys16_textram[0x0e84] ))
/*TODO*///			sys18_fg2_active=1;
/*TODO*///		if(sys16_bg2_scrollx | sys16_bg2_scrolly | READ_WORD( &sys16_textram[0x0e86] ))
/*TODO*///			sys18_bg2_active=1;
/*TODO*///	
/*TODO*///		set_tile_bank18( READ_WORD( &sys16_extraram[0] ) );
/*TODO*///		set_refresh_18( READ_WORD( &sys16_extraram2[0x1c] ) );
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr shdancer_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = {0x00,0x02,0x04,0x06,0x08,0x0A,0x0C,0x0E,0x10,0x12,0x14,0x16,0x18,0x1A,0x1C,0x1E};
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_spritelist_end=0x8000;
/*TODO*///	
/*TODO*///		sys16_update_proc = shdancer_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_shdancer = new InitDriverPtr() { public void handler() {
/*TODO*///		UBytePtr RAM= memory_region(REGION_CPU2);
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys18_splittab_fg_x=&sys16_textram[0x0f80];
/*TODO*///		sys18_splittab_bg_x=&sys16_textram[0x0fc0];
/*TODO*///		install_mem_read_handler(0, 0xffc000, 0xffc001, shdancer_skip);
/*TODO*///		sys16_MaxShadowColors=0;		// doesn't seem to use transparent shadows
/*TODO*///	
/*TODO*///		memcpy(RAM,&RAM[0x10000],0xa000);
/*TODO*///	
/*TODO*///		sys16_sprite_decode( 4,0x080000 );
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_shdancer = new InputPortPtr(){ public void handler() { 
/*TODO*///	PORT_START();  /* player 1 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON3 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
/*TODO*///	
/*TODO*///	PORT_START();  /* player 2 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		SYS16_SERVICE
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x01, "2 Credits to Start" );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Lives") );
/*TODO*///		PORT_DIPSETTING(    0x00, "2" );
/*TODO*///		PORT_DIPSETTING(    0x0c, "3" );
/*TODO*///		PORT_DIPSETTING(    0x08, "4" );
/*TODO*///		PORT_DIPSETTING(    0x04, "5" );
/*TODO*///		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x20, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x30, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x10, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0xc0, 0xc0, "Time Adjust" );
/*TODO*///		PORT_DIPSETTING(    0x00, "2.20" );
/*TODO*///		PORT_DIPSETTING(    0x40, "2.40" );
/*TODO*///		PORT_DIPSETTING(    0xc0, "3.00" );
/*TODO*///		PORT_DIPSETTING(    0x80, "3.30" );
/*TODO*///	
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_18( machine_driver_shdancer, \
/*TODO*///		shdancer_readmem,shdancer_writemem,shdancer_init_machine, gfx4 )
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_shdancbl = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x080000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "ic39", 0x000000, 0x10000, 0xadc1781c )
/*TODO*///		ROM_LOAD_ODD ( "ic53", 0x000000, 0x10000, 0x1c1ac463 )
/*TODO*///		ROM_LOAD_EVEN( "ic38", 0x020000, 0x10000, 0xcd6e155b )
/*TODO*///		ROM_LOAD_ODD ( "ic52", 0x020000, 0x10000, 0xbb3c49a4 )
/*TODO*///		ROM_LOAD_EVEN( "ic37", 0x040000, 0x10000, 0x1bd8d5c3 )
/*TODO*///		ROM_LOAD_ODD ( "ic51", 0x040000, 0x10000, 0xce2e71b4 )
/*TODO*///		ROM_LOAD_EVEN( "ic36", 0x060000, 0x10000, 0xbb861290 )
/*TODO*///		ROM_LOAD_ODD ( "ic50", 0x060000, 0x10000, 0x7f7b82b1 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "ic4",  0x00000, 0x20000, 0xf0a016fe );
/*TODO*///		ROM_LOAD( "ic18", 0x20000, 0x20000, 0xf6bee053 );
/*TODO*///		ROM_LOAD( "ic3",  0x40000, 0x20000, 0xe07e6b5d );
/*TODO*///		ROM_LOAD( "ic17", 0x60000, 0x20000, 0xf59deba1 );
/*TODO*///		ROM_LOAD( "ic2",  0x80000, 0x20000, 0x60095070 );
/*TODO*///		ROM_LOAD( "ic16", 0xa0000, 0x20000, 0x0f0d5dd3 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "ic73", 0x000000, 0x10000, 0x59e77c96 );
/*TODO*///		ROM_LOAD( "ic74", 0x010000, 0x10000, 0x90ea5407 );
/*TODO*///		ROM_LOAD( "ic75", 0x020000, 0x10000, 0x27d2fa61 );
/*TODO*///		ROM_LOAD( "ic76", 0x030000, 0x10000, 0xf36db688 );
/*TODO*///		ROM_LOAD( "ic58", 0x040000, 0x10000, 0x9cd5c8c7 );
/*TODO*///		ROM_LOAD( "ic59", 0x050000, 0x10000, 0xff40e872 );
/*TODO*///		ROM_LOAD( "ic60", 0x060000, 0x10000, 0x826d7245 );
/*TODO*///		ROM_LOAD( "ic61", 0x070000, 0x10000, 0xdcf8068b );
/*TODO*///		ROM_LOAD( "ic77", 0x080000, 0x10000, 0xf93470b7 );
/*TODO*///		ROM_LOAD( "ic78", 0x090000, 0x10000, 0x4d523ea3 );
/*TODO*///		ROM_LOAD( "ic95", 0x0a0000, 0x10000, 0x828b8294 );
/*TODO*///		ROM_LOAD( "ic94", 0x0b0000, 0x10000, 0x542b2d1e );
/*TODO*///		ROM_LOAD( "ic62", 0x0c0000, 0x10000, 0x50ca8065 );
/*TODO*///		ROM_LOAD( "ic63", 0x0d0000, 0x10000, 0xd1866aa9 );
/*TODO*///		ROM_LOAD( "ic90", 0x0e0000, 0x10000, 0x3602b758 );
/*TODO*///		ROM_LOAD( "ic89", 0x0f0000, 0x10000, 0x1ba4be93 );
/*TODO*///		ROM_LOAD( "ic79", 0x100000, 0x10000, 0xf22548ee );
/*TODO*///		ROM_LOAD( "ic80", 0x110000, 0x10000, 0x6209f7f9 );
/*TODO*///		ROM_LOAD( "ic81", 0x120000, 0x10000, 0x34692f23 );
/*TODO*///		ROM_LOAD( "ic82", 0x130000, 0x10000, 0x7ae40237 );
/*TODO*///		ROM_LOAD( "ic64", 0x140000, 0x10000, 0x7a8b7bcc );
/*TODO*///		ROM_LOAD( "ic65", 0x150000, 0x10000, 0x90ffca14 );
/*TODO*///		ROM_LOAD( "ic66", 0x160000, 0x10000, 0x5d655517 );
/*TODO*///		ROM_LOAD( "ic67", 0x170000, 0x10000, 0x0e5d0855 );
/*TODO*///		ROM_LOAD( "ic83", 0x180000, 0x10000, 0xa9040a32 );
/*TODO*///		ROM_LOAD( "ic84", 0x190000, 0x10000, 0xd6810031 );
/*TODO*///		ROM_LOAD( "ic92", 0x1a0000, 0x10000, 0xb57d5cb5 );
/*TODO*///		ROM_LOAD( "ic91", 0x1b0000, 0x10000, 0x49def6c8 );
/*TODO*///		ROM_LOAD( "ic68", 0x1c0000, 0x10000, 0x8d684e53 );
/*TODO*///		ROM_LOAD( "ic69", 0x1d0000, 0x10000, 0xc47d32e2 );
/*TODO*///		ROM_LOAD( "ic88", 0x1e0000, 0x10000, 0x9de140e1 );
/*TODO*///		ROM_LOAD( "ic87", 0x1f0000, 0x10000, 0x8172a991 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "ic45", 0x10000, 0x10000, 0x576b3a81 );
/*TODO*///		ROM_LOAD( "ic46", 0x20000, 0x10000, 0xc84e8c84 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	/*
/*TODO*///	public static ReadHandlerPtr shdancer_skip = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		if (cpu_get_pc()==0x2f76) {cpu_spinuntil_int(); return 0xffff;}
/*TODO*///	
/*TODO*///		return READ_WORD(&sys16_workingram[0x0000]);
/*TODO*///	} };
/*TODO*///	*/
/*TODO*///	
/*TODO*///	static MemoryReadAddress shdancbl_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x440000, 0x440fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x840000, 0x840fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0xc00000, 0xc00007, MRA_EXTRAM ),
/*TODO*///		new MemoryReadAddress( 0xc40000, 0xc40001, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xc40002, 0xc40003, io_dip2_r ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0xc41002, 0xc41003, input_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xc41004, 0xc41005, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0xc41000, 0xc41001, io_service_r ),
/*TODO*///	//	new MemoryReadAddress( 0xc40000, 0xc4ffff, MRA_EXTRAM3 ),
/*TODO*///		new MemoryReadAddress( 0xe40000, 0xe4001f, MRA_EXTRAM2 ),
/*TODO*///		new MemoryReadAddress( 0xe43034, 0xe43035, MRA_NOP ),
/*TODO*///	//	new MemoryReadAddress( 0xffc000, 0xffc001, shdancer_skip ),
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress shdancbl_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x440000, 0x440fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x840000, 0x840fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xc00000, 0xc00007, MWA_EXTRAM ),
/*TODO*///	//	new MemoryWriteAddress( 0xc40000, 0xc4ffff, MWA_EXTRAM3 ),
/*TODO*///		new MemoryWriteAddress( 0xe40000, 0xe4001f, MWA_BANK4,sys16_extraram2 ),
/*TODO*///		new MemoryWriteAddress( 0xe43034, 0xe43035, MWA_NOP ),
/*TODO*///		new MemoryWriteAddress( 0xfe0006, 0xfe0007, sound_command_nmi_w ),
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void shdancbl_update_proc( void ){
/*TODO*///	// this is all wrong and needs re-doing.
/*TODO*///	
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0e98] );
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0e9a] );
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0e90] );
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0e92] );
/*TODO*///	
/*TODO*///		set_fg_page( READ_WORD( &sys16_textram[0x0e80] ) );
/*TODO*///		set_bg_page( READ_WORD( &sys16_textram[0x0e82] ) );
/*TODO*///	
/*TODO*///		sys16_fg2_scrollx = READ_WORD( &sys16_textram[0x0e9c] );
/*TODO*///		sys16_bg2_scrollx = READ_WORD( &sys16_textram[0x0e9e] );
/*TODO*///		sys16_fg2_scrolly = READ_WORD( &sys16_textram[0x0e94] );
/*TODO*///		sys16_bg2_scrolly = READ_WORD( &sys16_textram[0x0e96] );
/*TODO*///	
/*TODO*///		set_fg2_page( READ_WORD( &sys16_textram[0x0e84] ) );
/*TODO*///		set_bg2_page( READ_WORD( &sys16_textram[0x0e82] ) );
/*TODO*///	
/*TODO*///		sys18_bg2_active=0;
/*TODO*///		sys18_fg2_active=0;
/*TODO*///	
/*TODO*///		if(sys16_fg2_scrollx | sys16_fg2_scrolly | READ_WORD( &sys16_textram[0x0e84] ))
/*TODO*///			sys18_fg2_active=1;
/*TODO*///		if(sys16_bg2_scrollx | sys16_bg2_scrolly | READ_WORD( &sys16_textram[0x0e86] ))
/*TODO*///			sys18_bg2_active=1;
/*TODO*///	
/*TODO*///		set_tile_bank18( READ_WORD( &sys16_extraram[0] ) );
/*TODO*///		set_refresh_18( READ_WORD( &sys16_extraram2[0x1c] ) );
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	public static InitMachinePtr shdancbl_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = {0x00,0x02,0x04,0x06,0x08,0x0A,0x0C,0x0E,0x10,0x12,0x14,0x16,0x18,0x1A,0x1C,0x1E};
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_spritelist_end=0x8000;
/*TODO*///		sys16_sprxoffset = -0xbc+0x77;
/*TODO*///	
/*TODO*///		sys16_update_proc = shdancbl_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_shdancbl = new InitDriverPtr() { public void handler() {
/*TODO*///		UBytePtr RAM= memory_region(REGION_CPU2);
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys18_splittab_fg_x=&sys16_textram[0x0f80];
/*TODO*///		sys18_splittab_bg_x=&sys16_textram[0x0fc0];
/*TODO*///		install_mem_read_handler(0, 0xffc000, 0xffc001, shdancer_skip);
/*TODO*///		sys16_MaxShadowColors=0;		// doesn't seem to use transparent shadows
/*TODO*///	
/*TODO*///		memcpy(RAM,&RAM[0x10000],0xa000);
/*TODO*///	
/*TODO*///		/* invert the graphics bits on the tiles */
/*TODO*///		for (i = 0; i < 0xc0000; i++)
/*TODO*///			memory_region(REGION_GFX1)[i] ^= 0xff;
/*TODO*///		sys16_sprite_decode( 4,0x080000 );
/*TODO*///	} };
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_18( machine_driver_shdancbl, \
/*TODO*///		shdancbl_readmem,shdancbl_writemem,shdancbl_init_machine, gfx4 )
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	// sys18
/*TODO*///	static RomLoadPtr rom_shdancrj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x080000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "sd12722b.bin", 0x000000, 0x40000, 0xc00552a2 )
/*TODO*///		ROM_LOAD_ODD ( "sd12721b.bin", 0x000000, 0x40000, 0x653d351a )
/*TODO*///	
/*TODO*///		ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "sd12712.bin",  0x00000, 0x40000, 0x9bdabe3d );
/*TODO*///		ROM_LOAD( "sd12713.bin",  0x40000, 0x40000, 0x852d2b1c );
/*TODO*///		ROM_LOAD( "sd12714.bin",  0x80000, 0x40000, 0x448226ce );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "sd12719.bin",  0x000000, 0x40000, 0xd6888534 );
/*TODO*///		ROM_LOAD( "sd12726.bin",  0x040000, 0x40000, 0xff344945 );
/*TODO*///		ROM_LOAD( "sd12718.bin",  0x080000, 0x40000, 0xba2efc0c );
/*TODO*///		ROM_LOAD( "sd12725.bin",  0x0c0000, 0x40000, 0x268a0c17 );
/*TODO*///		ROM_LOAD( "sd12717.bin",  0x100000, 0x40000, 0xc81cc4f8 );
/*TODO*///		ROM_LOAD( "sd12724.bin",  0x140000, 0x40000, 0x0f4903dc );
/*TODO*///		ROM_LOAD( "sd12716.bin",  0x180000, 0x40000, 0xa870e629 );
/*TODO*///		ROM_LOAD( "sd12723.bin",  0x1c0000, 0x40000, 0xc606cf90 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x70000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "sd12720.bin", 0x10000, 0x20000, 0x7a0d8de1 );
/*TODO*///		ROM_LOAD( "sd12715.bin", 0x30000, 0x40000, 0x07051a52 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	public static ReadHandlerPtr shdancrj_skip = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		if (cpu_get_pc()==0x2f70) {cpu_spinuntil_int(); return 0xffff;}
/*TODO*///	
/*TODO*///		return READ_WORD(&sys16_workingram[0xc000]);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitMachinePtr shdancrj_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = {0x00,0x02,0x04,0x06,0x08,0x0A,0x0C,0x0E,0x10,0x12,0x14,0x16,0x18,0x1A,0x1C,0x1E};
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_spritelist_end=0x8000;
/*TODO*///	
/*TODO*///		patch_code(0x6821, 0xdf);
/*TODO*///		sys16_update_proc = shdancer_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_shdancrj = new InitDriverPtr() { public void handler() {
/*TODO*///		UBytePtr RAM= memory_region(REGION_CPU2);
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys18_splittab_fg_x=&sys16_textram[0x0f80];
/*TODO*///		sys18_splittab_bg_x=&sys16_textram[0x0fc0];
/*TODO*///		install_mem_read_handler(0, 0xffc000, 0xffc001, shdancrj_skip);
/*TODO*///	
/*TODO*///		memcpy(RAM,&RAM[0x10000],0xa000);
/*TODO*///	
/*TODO*///		sys16_sprite_decode( 4,0x080000 );
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_18( machine_driver_shdancrj, \
/*TODO*///		shdancer_readmem,shdancer_writemem,shdancrj_init_machine, gfx4 )
/*TODO*///	
    /**
     * ************************************************************************
     */
    // sys16B
    static RomLoadPtr rom_shinobi = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x040000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("shinobi.a4", 0x000000, 0x10000, 0xb930399d);
            ROM_LOAD_ODD("shinobi.a1", 0x000000, 0x10000, 0x343f4c46);
            ROM_LOAD_EVEN("epr11283", 0x020000, 0x10000, 0x9d46e707);
            ROM_LOAD_ODD("epr11281", 0x020000, 0x10000, 0x7961d07e);

            ROM_REGION(0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* tiles */
            ROM_LOAD("shinobi.b9", 0x00000, 0x10000, 0x5f62e163);
            ROM_LOAD("shinobi.b10", 0x10000, 0x10000, 0x75f8fbc9);
            ROM_LOAD("shinobi.b11", 0x20000, 0x10000, 0x06508bb9);

            ROM_REGION(0x080000 * 2, REGION_GFX2);/* sprites */
            ROM_LOAD("epr11290.10", 0x00000, 0x10000, 0x611f413a);
            ROM_LOAD("epr11294.11", 0x10000, 0x10000, 0x5eb00fc1);
            ROM_LOAD("epr11291.17", 0x20000, 0x10000, 0x3c0797c0);
            ROM_LOAD("epr11295.18", 0x30000, 0x10000, 0x25307ef8);
            ROM_LOAD("epr11292.23", 0x40000, 0x10000, 0xc29ac34e);
            ROM_LOAD("epr11296.24", 0x50000, 0x10000, 0x04a437f8);
            ROM_LOAD("epr11293.29", 0x60000, 0x10000, 0x41f41063);
            ROM_LOAD("epr11297.30", 0x70000, 0x10000, 0xb6e1fd72);

            ROM_REGION(0x20000, REGION_CPU2);/* sound CPU */
            ROM_LOAD("shinobi.a7", 0x0000, 0x8000, 0x2457a7cf);
            ROM_LOAD("shinobi.a8", 0x10000, 0x8000, 0xc8df8460);
            ROM_LOAD("shinobi.a9", 0x18000, 0x8000, 0xe5a4cf30);

            ROM_END();
        }
    };

    static RomLoadPtr rom_shinobib = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x040000, REGION_CPU1);/* 68000 code */
            // Custom cpu 317-0049
            ROM_LOAD_EVEN("epr11282", 0x000000, 0x10000, 0x5f2e5524);
            ROM_LOAD_ODD("epr11280", 0x000000, 0x10000, 0xbdfe5c38);
            ROM_LOAD_EVEN("epr11283", 0x020000, 0x10000, 0x9d46e707);
            ROM_LOAD_ODD("epr11281", 0x020000, 0x10000, 0x7961d07e);

            ROM_REGION(0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* tiles */
            ROM_LOAD("shinobi.b9", 0x00000, 0x10000, 0x5f62e163);
            ROM_LOAD("shinobi.b10", 0x10000, 0x10000, 0x75f8fbc9);
            ROM_LOAD("shinobi.b11", 0x20000, 0x10000, 0x06508bb9);

            ROM_REGION(0x080000 * 2, REGION_GFX2);/* sprites */
            ROM_LOAD("epr11290.10", 0x00000, 0x10000, 0x611f413a);
            ROM_LOAD("epr11294.11", 0x10000, 0x10000, 0x5eb00fc1);
            ROM_LOAD("epr11291.17", 0x20000, 0x10000, 0x3c0797c0);
            ROM_LOAD("epr11295.18", 0x30000, 0x10000, 0x25307ef8);
            ROM_LOAD("epr11292.23", 0x40000, 0x10000, 0xc29ac34e);
            ROM_LOAD("epr11296.24", 0x50000, 0x10000, 0x04a437f8);
            ROM_LOAD("epr11293.29", 0x60000, 0x10000, 0x41f41063);
            ROM_LOAD("epr11297.30", 0x70000, 0x10000, 0xb6e1fd72);

            ROM_REGION(0x20000, REGION_CPU2);/* sound CPU */
            ROM_LOAD("shinobi.a7", 0x0000, 0x8000, 0x2457a7cf);
            ROM_LOAD("shinobi.a8", 0x10000, 0x8000, 0xc8df8460);
            ROM_LOAD("shinobi.a9", 0x18000, 0x8000, 0xe5a4cf30);

            ROM_END();
        }
    };

    /**
     * ************************************************************************
     */
    public static ReadHandlerPtr shinobi_skip = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x32e0) {
                cpu_spinuntil_int();
                return 1 << 8;
            }

            return sys16_workingram.READ_WORD(0x301c);
        }
    };

    static MemoryReadAddress shinobi_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x03ffff, MRA_ROM),
                new MemoryReadAddress(0x400000, 0x40ffff, sys16_tileram_r),
                new MemoryReadAddress(0x410000, 0x410fff, sys16_textram_r),
                new MemoryReadAddress(0x440000, 0x440fff, MRA_BANK2),
                new MemoryReadAddress(0x840000, 0x840fff, paletteram_word_r),
                new MemoryReadAddress(0xc41002, 0xc41003, input_port_0_r),
                new MemoryReadAddress(0xc41006, 0xc41007, input_port_1_r),
                new MemoryReadAddress(0xc41000, 0xc41001, input_port_2_r),
                new MemoryReadAddress(0xc42002, 0xc42003, input_port_3_r),
                new MemoryReadAddress(0xc42000, 0xc42001, input_port_4_r),
                new MemoryReadAddress(0xc40000, 0xc40001, MRA_BANK4),
                new MemoryReadAddress(0xc43000, 0xc43001, MRA_NOP),
                new MemoryReadAddress(0xfff01c, 0xfff01d, shinobi_skip),
                new MemoryReadAddress(0xffc000, 0xffffff, MRA_BANK1),
                new MemoryReadAddress(-1)
            };

    static MemoryWriteAddress shinobi_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x03ffff, MWA_ROM),
                new MemoryWriteAddress(0x400000, 0x40ffff, sys16_tileram_w, sys16_tileram),
                new MemoryWriteAddress(0x410000, 0x410fff, sys16_textram_w, sys16_textram),
                new MemoryWriteAddress(0x440000, 0x440fff, MWA_BANK2, sys16_spriteram),
                new MemoryWriteAddress(0x840000, 0x840fff, sys16_paletteram_w, paletteram),
                new MemoryWriteAddress(0xc40000, 0xc40001, MWA_BANK4, sys16_extraram2),
                new MemoryWriteAddress(0xc43000, 0xc43001, MWA_NOP),
                new MemoryWriteAddress(0xfe0006, 0xfe0007, sound_command_w),
                new MemoryWriteAddress(0xffc000, 0xffffff, MWA_BANK1, sys16_workingram),
                new MemoryWriteAddress(-1)
            };

    /**
     * ************************************************************************
     */
    public static sys16_update_procPtr shinobi_update_proc = new sys16_update_procPtr() {
        public void handler() {
            sys16_fg_scrollx = sys16_textram.READ_WORD(0x0e98);
            sys16_bg_scrollx = sys16_textram.READ_WORD(0x0e9a);
            sys16_fg_scrolly = sys16_textram.READ_WORD(0x0e90);
            sys16_bg_scrolly = sys16_textram.READ_WORD(0x0e92);

            set_fg_page(sys16_textram.READ_WORD(0x0e80));
            set_bg_page(sys16_textram.READ_WORD(0x0e82));

            set_refresh(sys16_extraram2.READ_WORD(0));
        }
    };

    public static InitMachinePtr shinobi_init_machine = new InitMachinePtr() {
        public void handler() {
            int bank[] = {0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 4, 0, 2, 0, 0};
            sys16_obj_bank = bank;
            sys16_dactype = 1;
            sys16_update_proc = shinobi_update_proc;
        }
    };

    public static InitDriverPtr init_shinobi = new InitDriverPtr() {
        public void handler() {
            sys16_onetime_init_machine.handler();
            sys16_sprite_decode(4, 0x20000);
        }
    };

    /**
     * ************************************************************************
     */
    static InputPortPtr input_ports_shinobi = new InputPortPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x07, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x09, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x05, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x04, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x02, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0x03, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x06, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x00, "Free Play (if Coin B too) or 1/1");
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x70, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x90, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x50, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x40, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x10, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x20, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0x30, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x60, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0xa0, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x00, "Free Play (if Coin A too) or 1/1");

            PORT_START();
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Lives"));
            PORT_DIPSETTING(0x08, "2");
            PORT_DIPSETTING(0x0c, "3");
            PORT_DIPSETTING(0x04, "5");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "240", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x20, "Easy");
            PORT_DIPSETTING(0x30, "Normal");
            PORT_DIPSETTING(0x10, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x40, 0x40, "Enemy's Bullet Speed");
            PORT_DIPSETTING(0x40, "Slow");
            PORT_DIPSETTING(0x00, "Fast");
            PORT_DIPNAME(0x80, 0x80, "Language");
            PORT_DIPSETTING(0x80, "Japanese");
            PORT_DIPSETTING(0x00, "English");

            INPUT_PORTS_END();
        }
    };

    /**
     * ************************************************************************
     */
    static MachineDriver machine_driver_shinobi = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000,
                        shinobi_readmem, shinobi_writemem, null, null,
                        sys16_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4096000,
                        sound_readmem_7759, sound_writemem, sound_readport, sound_writeport_7759,
                        ignore_interrupt, 1
                ),},
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1,
            shinobi_init_machine,
            40 * 8, 28 * 8, new rectangle(0 * 8, 40 * 8 - 1, 0 * 8, 28 * 8 - 1),
            gfx1,
            2048 * ShadowColorsMultiplier, 2048 * ShadowColorsMultiplier,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            sys16_vh_start,
            sys16_vh_stop,
            sys16_vh_screenrefresh,
            SOUND_SUPPORTS_STEREO, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface
                ), new MachineSound(
                        SOUND_UPD7759,
                        upd7759_interface
                )
            }
    );
    /**
     * ************************************************************************
     */
    // sys16A
/*TODO*///	static RomLoadPtr rom_shinobia = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x040000, REGION_CPU1 );/* 68000 code */
/*TODO*///	// custom cpu 317-0050
/*TODO*///		ROM_LOAD_EVEN( "epr11262.42", 0x000000, 0x10000, 0xd4b8df12 )
/*TODO*///		ROM_LOAD_ODD ( "epr11260.27", 0x000000, 0x10000, 0x2835c95d )
/*TODO*///		ROM_LOAD_EVEN( "epr11263.43", 0x020000, 0x10000, 0xa2a620bd )
/*TODO*///		ROM_LOAD_ODD ( "epr11261.25", 0x020000, 0x10000, 0xa3ceda52 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "epr11264.95", 0x00000, 0x10000, 0x46627e7d );
/*TODO*///		ROM_LOAD( "epr11265.94", 0x10000, 0x10000, 0x87d0f321 );
/*TODO*///		ROM_LOAD( "epr11266.93", 0x20000, 0x10000, 0xefb4af87 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x080000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "epr11290.10", 0x00000, 0x10000, 0x611f413a );
/*TODO*///		ROM_LOAD( "epr11294.11", 0x10000, 0x10000, 0x5eb00fc1 );
/*TODO*///		ROM_LOAD( "epr11291.17", 0x20000, 0x10000, 0x3c0797c0 );
/*TODO*///		ROM_LOAD( "epr11295.18", 0x30000, 0x10000, 0x25307ef8 );
/*TODO*///		ROM_LOAD( "epr11292.23", 0x40000, 0x10000, 0xc29ac34e );
/*TODO*///		ROM_LOAD( "epr11296.24", 0x50000, 0x10000, 0x04a437f8 );
/*TODO*///		ROM_LOAD( "epr11293.29", 0x60000, 0x10000, 0x41f41063 );
/*TODO*///		ROM_LOAD( "epr11297.30", 0x70000, 0x10000, 0xb6e1fd72 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr11267.12", 0x0000, 0x8000, 0xdd50b745 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x1000, REGION_CPU3 );     /* 4k for 7751 onboard ROM */
/*TODO*///		ROM_LOAD( "7751.bin",     0x0000, 0x0400, 0x6a9534fc );/* 7751 - U34 */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x08000, REGION_SOUND1 );/* 7751 sound data */
/*TODO*///		ROM_LOAD( "epr11268.1", 0x0000, 0x8000, 0x6d7966da );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_shinobl = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x040000, REGION_CPU1 );/* 68000 code */
/*TODO*///	// Star Bootleg
/*TODO*///		ROM_LOAD_EVEN( "b3",          0x000000, 0x10000, 0x38e59646 )
/*TODO*///		ROM_LOAD_ODD ( "b1",          0x000000, 0x10000, 0x8529d192 )
/*TODO*///		ROM_LOAD_EVEN( "epr11263.43", 0x020000, 0x10000, 0xa2a620bd )
/*TODO*///		ROM_LOAD_ODD ( "epr11261.25", 0x020000, 0x10000, 0xa3ceda52 )
/*TODO*///	
/*TODO*///	// Beta Bootleg
/*TODO*///	//	ROM_LOAD_EVEN( "4",           0x000000, 0x10000, 0xc178a39c )
/*TODO*///	//	ROM_LOAD_ODD ( "2",           0x000000, 0x10000, 0x5ad8ebf2 )
/*TODO*///	//	ROM_LOAD_EVEN( "epr11263.43", 0x020000, 0x10000, 0xa2a620bd )
/*TODO*///	//	ROM_LOAD_ODD ( "epr11261.25", 0x020000, 0x10000, 0xa3ceda52 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "epr11264.95", 0x00000, 0x10000, 0x46627e7d );
/*TODO*///		ROM_LOAD( "epr11265.94", 0x10000, 0x10000, 0x87d0f321 );
/*TODO*///		ROM_LOAD( "epr11266.93", 0x20000, 0x10000, 0xefb4af87 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x080000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "epr11290.10", 0x00000, 0x10000, 0x611f413a );
/*TODO*///		ROM_LOAD( "epr11294.11", 0x10000, 0x10000, 0x5eb00fc1 );
/*TODO*///		ROM_LOAD( "epr11291.17", 0x20000, 0x10000, 0x3c0797c0 );
/*TODO*///		ROM_LOAD( "epr11295.18", 0x30000, 0x10000, 0x25307ef8 );
/*TODO*///		ROM_LOAD( "epr11292.23", 0x40000, 0x10000, 0xc29ac34e );
/*TODO*///		ROM_LOAD( "epr11296.24", 0x50000, 0x10000, 0x04a437f8 );
/*TODO*///		ROM_LOAD( "epr11293.29", 0x60000, 0x10000, 0x41f41063 );
/*TODO*///	//	ROM_LOAD( "epr11297.30", 0x70000, 0x10000, 0xb6e1fd72 );
/*TODO*///		ROM_LOAD( "b17",         0x70000, 0x10000, 0x0315cf42 );// Beta bootleg uses the rom above.
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr11267.12", 0x0000, 0x8000, 0xdd50b745 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x1000, REGION_CPU3 );     /* 4k for 7751 onboard ROM */
/*TODO*///		ROM_LOAD( "7751.bin",     0x0000, 0x0400, 0x6a9534fc );/* 7751 - U34 */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x08000, REGION_SOUND1 );/* 7751 sound data */
/*TODO*///		ROM_LOAD( "epr11268.1", 0x0000, 0x8000, 0x6d7966da );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static MemoryReadAddress shinobl_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x440000, 0x440fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x840000, 0x840fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0xc41002, 0xc41003, input_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xc41006, 0xc41007, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0xc41000, 0xc41001, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xc42000, 0xc42001, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xc42002, 0xc42003, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xc40000, 0xc40fff, MRA_EXTRAM2 ),
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress shinobl_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x440000, 0x440fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x840000, 0x840fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc40001, sound_command_nmi_w ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc40fff, MWA_BANK4,sys16_extraram2 ),
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void shinobl_update_proc( void ){
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0ff8] ) & 0x01ff;
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0ffa] ) & 0x01ff;
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0f24] ) & 0x00ff;
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0f26] ) & 0x01ff;
/*TODO*///	
/*TODO*///		set_fg_page( READ_WORD( &sys16_textram[0x0e9e] ) );
/*TODO*///		set_bg_page( READ_WORD( &sys16_textram[0x0e9c] ) );
/*TODO*///	
/*TODO*///		set_refresh_3d( READ_WORD( &sys16_extraram2[2] ) );
/*TODO*///	
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr shinobl_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = {0,2,4,6,1,3,5,7,0,0,0,0,0,0,0,0};
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_textmode=1;
/*TODO*///		sys16_spritesystem = 2;
/*TODO*///		sys16_sprxoffset = -0xbc;
/*TODO*///		sys16_fgxoffset = sys16_bgxoffset = 7;
/*TODO*///		sys16_tilebank_switch=0x2000;
/*TODO*///	
/*TODO*///		sys16_dactype = 1;
/*TODO*///		sys16_update_proc = shinobl_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_7751( machine_driver_shinobl, \
/*TODO*///		shinobl_readmem,shinobl_writemem,shinobl_init_machine, gfx1)
/*TODO*///	
/*TODO*///	/***************************************************************************/

    // sys16A custom
    static RomLoadPtr rom_tetris = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x020000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("epr12201.rom", 0x000000, 0x8000, 0x338e9b51);
            ROM_LOAD_ODD("epr12200.rom", 0x000000, 0x8000, 0xfb058779);

            ROM_REGION(0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* tiles */
            ROM_LOAD("epr12202.rom", 0x00000, 0x10000, 0x2f7da741);
            ROM_LOAD("epr12203.rom", 0x10000, 0x10000, 0xa6e58ec5);
            ROM_LOAD("epr12204.rom", 0x20000, 0x10000, 0x0ae98e23);

            ROM_REGION(0x010000 * 2, REGION_GFX2);/* sprites */
            ROM_LOAD("epr12169.rom", 0x0000, 0x8000, 0xdacc6165);
            ROM_LOAD("epr12170.rom", 0x8000, 0x8000, 0x87354e42);

            ROM_REGION(0x10000, REGION_CPU2);/* sound CPU */
            ROM_LOAD("epr12205.rom", 0x0000, 0x8000, 0x6695dc99);
            ROM_END();
        }
    };

    // sys16B
    static RomLoadPtr rom_tetrisbl = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x020000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("rom2.bin", 0x000000, 0x10000, 0x4d165c38);
            ROM_LOAD_ODD("rom1.bin", 0x000000, 0x10000, 0x1e912131);

            ROM_REGION(0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* tiles */
            ROM_LOAD("scr01.rom", 0x00000, 0x10000, 0x62640221);
            ROM_LOAD("scr02.rom", 0x10000, 0x10000, 0x9abd183b);
            ROM_LOAD("scr03.rom", 0x20000, 0x10000, 0x2495fd4e);

            ROM_REGION(0x020000 * 2, REGION_GFX2);/* sprites */
            ROM_LOAD("obj0-o.rom", 0x00000, 0x10000, 0x2fb38880);
            ROM_LOAD("obj0-e.rom", 0x10000, 0x10000, 0xd6a02cba);

            ROM_REGION(0x10000, REGION_CPU2);/* sound CPU */
            ROM_LOAD("s-prog.rom", 0x0000, 0x8000, 0xbd9ba01b);
            ROM_END();
        }
    };

    // sys16B
    static RomLoadPtr rom_tetrisa = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x020000, REGION_CPU1);/* 68000 code */
            // Custom Cpu 317-0092
            ROM_LOAD_EVEN("tetris.a7", 0x000000, 0x10000, 0x9ce15ac9);
            ROM_LOAD_ODD("tetris.a5", 0x000000, 0x10000, 0x98d590ca);

            ROM_REGION(0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* tiles */
            ROM_LOAD("scr01.rom", 0x00000, 0x10000, 0x62640221);
            ROM_LOAD("scr02.rom", 0x10000, 0x10000, 0x9abd183b);
            ROM_LOAD("scr03.rom", 0x20000, 0x10000, 0x2495fd4e);

            ROM_REGION(0x020000 * 2, REGION_GFX2);/* sprites */
            ROM_LOAD("obj0-o.rom", 0x00000, 0x10000, 0x2fb38880);
            ROM_LOAD("obj0-e.rom", 0x10000, 0x10000, 0xd6a02cba);

            ROM_REGION(0x10000, REGION_CPU2);/* sound CPU */
            ROM_LOAD("s-prog.rom", 0x0000, 0x8000, 0xbd9ba01b);
            ROM_END();
        }
    };

    /**
     * ************************************************************************
     */
    static MemoryReadAddress tetris_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x01ffff, MRA_ROM),
                new MemoryReadAddress(0x400000, 0x40ffff, sys16_tileram_r),
                new MemoryReadAddress(0x410000, 0x410fff, sys16_textram_r),
                new MemoryReadAddress(0x418000, 0x41803f, MRA_BANK4),
                new MemoryReadAddress(0x440000, 0x440fff, MRA_BANK2),
                new MemoryReadAddress(0x840000, 0x840fff, paletteram_word_r),
                new MemoryReadAddress(0xc40000, 0xc40001, MRA_BANK3),
                new MemoryReadAddress(0xc41002, 0xc41003, input_port_0_r),
                new MemoryReadAddress(0xc41006, 0xc41007, input_port_1_r),
                new MemoryReadAddress(0xc41000, 0xc41001, input_port_2_r),
                new MemoryReadAddress(0xc42002, 0xc42003, input_port_3_r),
                new MemoryReadAddress(0xc42000, 0xc42001, input_port_4_r),
                new MemoryReadAddress(0xc80000, 0xc80001, MRA_NOP),
                new MemoryReadAddress(0xffc000, 0xffffff, MRA_BANK1),
                new MemoryReadAddress(-1)
            };

    static MemoryWriteAddress tetris_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x01ffff, MWA_ROM),
                new MemoryWriteAddress(0x400000, 0x40ffff, sys16_tileram_w, sys16_tileram),
                new MemoryWriteAddress(0x410000, 0x410fff, sys16_textram_w, sys16_textram),
                new MemoryWriteAddress(0x418000, 0x41803f, MWA_BANK4, sys16_extraram2),
                new MemoryWriteAddress(0x440000, 0x440fff, MWA_BANK2, sys16_spriteram),
                new MemoryWriteAddress(0x840000, 0x840fff, sys16_paletteram_w, paletteram),
                new MemoryWriteAddress(0xc40000, 0xc40001, MWA_BANK3, sys16_extraram),
                new MemoryWriteAddress(0xc42006, 0xc42007, sound_command_w),
                new MemoryWriteAddress(0xc43034, 0xc43035, MWA_NOP),
                new MemoryWriteAddress(0xc80000, 0xc80001, MWA_NOP),
                new MemoryWriteAddress(0xffc000, 0xffffff, MWA_BANK1, sys16_workingram),
                new MemoryWriteAddress(-1)
            };
    /**
     * ************************************************************************
     */
    public static sys16_update_procPtr tetris_update_proc = new sys16_update_procPtr() {
        public void handler() {
            sys16_fg_scrollx = sys16_textram.READ_WORD(0x0e98);
            sys16_bg_scrollx = sys16_textram.READ_WORD(0x0e9a);
            sys16_fg_scrolly = sys16_textram.READ_WORD(0x0e90);
            sys16_bg_scrolly = sys16_textram.READ_WORD(0x0e92);

            set_fg_page(sys16_extraram2.READ_WORD(0x38));
            set_bg_page(sys16_extraram2.READ_WORD(0x28));

            set_refresh(sys16_extraram.READ_WORD(0x0));
        }
    };

    public static InitMachinePtr tetris_init_machine = new InitMachinePtr() {
        public void handler() {

            int bank[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

            sys16_obj_bank = bank;

            patch_code.handler(0xba6, 0x4e);
            patch_code.handler(0xba7, 0x71);

            sys16_sprxoffset = -0x40;
            sys16_update_proc = tetris_update_proc;
        }
    };

    public static InitDriverPtr init_tetris = new InitDriverPtr() {
        public void handler() {
            sys16_onetime_init_machine.handler();
            sys16_sprite_decode(1, 0x10000);
        }
    };

    public static InitDriverPtr init_tetrisbl = new InitDriverPtr() {
        public void handler() {
            sys16_onetime_init_machine.handler();
            sys16_sprite_decode(1, 0x20000);
        }
    };
    /**
     * ************************************************************************
     */

    static InputPortPtr input_ports_tetris = new InputPortPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x07, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x09, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x05, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x04, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x02, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0x03, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x06, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x00, "Free Play (if Coin B too) or 1/1");
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x70, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x90, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x50, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x40, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x10, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x20, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0x30, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x60, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0xa0, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x00, "Free Play (if Coin A too) or 1/1");

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Unknown"));	// from the code it looks like some kind of difficulty
            PORT_DIPSETTING(0x0c, "A");				// level, but all 4 levels points to the same place
            PORT_DIPSETTING(0x08, "B");				// so it doesn't actually change anything!!
            PORT_DIPSETTING(0x04, "C");
            PORT_DIPSETTING(0x00, "D");
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x20, "Easy");
            PORT_DIPSETTING(0x30, "Normal");
            PORT_DIPSETTING(0x10, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    /**
     * ************************************************************************
     */
    static MachineDriver machine_driver_tetris = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000,
                        tetris_readmem, tetris_writemem, null, null,
                        sys16_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4096000,
                        sound_readmem, sound_writemem, sound_readport, sound_writeport,
                        ignore_interrupt, 1
                ),},
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1,
            tetris_init_machine,
            40 * 8, 28 * 8, new rectangle(0 * 8, 40 * 8 - 1, 0 * 8, 28 * 8 - 1),
            gfx1,
            2048 * ShadowColorsMultiplier, 2048 * ShadowColorsMultiplier,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            sys16_vh_start,
            sys16_vh_stop,
            sys16_vh_screenrefresh,
            SOUND_SUPPORTS_STEREO, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface
                )
            }
    );
    /**
     * ************************************************************************
     */
    // sys16B
/*TODO*///	static RomLoadPtr rom_timscanr = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x030000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "ts10853.bin", 0x00000, 0x8000, 0x24d7c5fb )
/*TODO*///		ROM_LOAD_ODD ( "ts10850.bin", 0x00000, 0x8000, 0xf1575732 )
/*TODO*///		ROM_LOAD_EVEN( "ts10854.bin", 0x10000, 0x8000, 0x82d0b237 )
/*TODO*///		ROM_LOAD_ODD ( "ts10851.bin", 0x10000, 0x8000, 0xf5ce271b )
/*TODO*///		ROM_LOAD_EVEN( "ts10855.bin", 0x20000, 0x8000, 0x63e95a53 )
/*TODO*///		ROM_LOAD_ODD ( "ts10852.bin", 0x20000, 0x8000, 0x7cd1382b )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "timscanr.b9",  0x00000, 0x8000, 0x07dccc37 );
/*TODO*///		ROM_LOAD( "timscanr.b10", 0x08000, 0x8000, 0x84fb9a3a );
/*TODO*///		ROM_LOAD( "timscanr.b11", 0x10000, 0x8000, 0xc8694bc0 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "ts10548.bin", 0x00000, 0x8000, 0xaa150735 );
/*TODO*///		ROM_LOAD( "ts10552.bin", 0x08000, 0x8000, 0x6fcbb9f7 );
/*TODO*///		ROM_LOAD( "ts10549.bin", 0x10000, 0x8000, 0x2f59f067 );
/*TODO*///		ROM_LOAD( "ts10553.bin", 0x18000, 0x8000, 0x8a220a9f );
/*TODO*///		ROM_LOAD( "ts10550.bin", 0x20000, 0x8000, 0xf05069ff );
/*TODO*///		ROM_LOAD( "ts10554.bin", 0x28000, 0x8000, 0xdc64f809 );
/*TODO*///		ROM_LOAD( "ts10551.bin", 0x30000, 0x8000, 0x435d811f );
/*TODO*///		ROM_LOAD( "ts10555.bin", 0x38000, 0x8000, 0x2143c471 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "ts10562.bin", 0x0000, 0x8000, 0x3f5028bf );
/*TODO*///		ROM_LOAD( "ts10563.bin", 0x10000, 0x8000, 0x9db7eddf );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr timscanr_skip = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		if (cpu_get_pc()==0x1044c) {cpu_spinuntil_int(); return 0;}
/*TODO*///	
/*TODO*///		return READ_WORD(&sys16_workingram[0x000c]);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	static MemoryReadAddress timscanr_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x02ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x440000, 0x440fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x840000, 0x840fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0xc41002, 0xc41003, input_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xc41006, 0xc41007, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0xc41000, 0xc41001, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xc42002, 0xc42003, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xc42000, 0xc42001, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xc41004, 0xc41005, io_dip3_r ),
/*TODO*///		new MemoryReadAddress( 0xffc00c, 0xffc00d, timscanr_skip ),
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress timscanr_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x02ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x440000, 0x440fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x840000, 0x840fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc40001, MWA_EXTRAM ),
/*TODO*///		new MemoryWriteAddress( 0xfe0006, 0xfe0007, sound_command_w ),
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void timscanr_update_proc( void ){
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0e98] );
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0e9a] );
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0e90] );
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0e92] );
/*TODO*///	
/*TODO*///		set_fg_page( READ_WORD( &sys16_textram[0x0e80] ) );
/*TODO*///		set_bg_page( READ_WORD( &sys16_textram[0x0e82] ) );
/*TODO*///	
/*TODO*///		set_refresh( READ_WORD( &sys16_extraram[0x0] ) );
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr timscanr_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = {00,00,00,00,00,00,00,0x03,00,00,00,0x02,00,0x01,00,00};
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_textmode=1;
/*TODO*///		sys16_update_proc = timscanr_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_timscanr = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_sprite_decode( 4,0x10000 );
/*TODO*///	} };
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_timscanr = new InputPortPtr(){ public void handler() { 
/*TODO*///		SYS16_JOY1
/*TODO*///		SYS16_JOY2
/*TODO*///		SYS16_SERVICE
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW2 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );		//??
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
/*TODO*///		PORT_DIPNAME( 0x1e, 0x14, "Bonus" );
/*TODO*///		PORT_DIPSETTING(    0x16, "Replay 1000000/2000000" );
/*TODO*///		PORT_DIPSETTING(    0x14, "Replay 1200000/2500000" );
/*TODO*///		PORT_DIPSETTING(    0x12, "Replay 1500000/3000000" );
/*TODO*///		PORT_DIPSETTING(    0x10, "Replay 2000000/4000000" );
/*TODO*///		PORT_DIPSETTING(    0x1c, "Replay 1000000" );
/*TODO*///		PORT_DIPSETTING(    0x1e, "Replay 1200000" );
/*TODO*///		PORT_DIPSETTING(    0x1a, "Replay 1500000" );
/*TODO*///		PORT_DIPSETTING(    0x18, "Replay 1800000" );
/*TODO*///		PORT_DIPSETTING(    0x0e, "ExtraBall 100000" );
/*TODO*///		PORT_DIPSETTING(    0x0c, "ExtraBall 200000" );
/*TODO*///		PORT_DIPSETTING(    0x0a, "ExtraBall 300000" );
/*TODO*///		PORT_DIPSETTING(    0x08, "ExtraBall 400000" );
/*TODO*///		PORT_DIPSETTING(    0x06, "ExtraBall 500000" );
/*TODO*///		PORT_DIPSETTING(    0x04, "ExtraBall 600000" );
/*TODO*///		PORT_DIPSETTING(    0x02, "ExtraBall 700000" );
/*TODO*///		PORT_DIPSETTING(    0x00, "None" );
/*TODO*///	
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, "Match" );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Lives") );
/*TODO*///		PORT_DIPSETTING(    0x80, "3" );
/*TODO*///		PORT_DIPSETTING(    0x00, "5" );
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW3 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );		//??
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x04, "1" );
/*TODO*///		PORT_DIPSETTING(    0x0c, "2" );
/*TODO*///		PORT_DIPSETTING(    0x08, "3" );
/*TODO*///		PORT_DIPSETTING(    0x00, "None" );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, "Allow Continue" );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Yes") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unused") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unused") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_7759( machine_driver_timscanr, \
/*TODO*///		timscanr_readmem,timscanr_writemem,timscanr_init_machine, gfx8,upd7759_interface )
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	// sys16B
/*TODO*///	static RomLoadPtr rom_toryumon = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x40000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "17689",  0x00000, 0x20000, 0x4f0dee19 )
/*TODO*///		ROM_LOAD_ODD ( "17688",  0x00000, 0x20000, 0x717d81c7 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "17700", 0x00000, 0x40000, 0x8f288b37 );
/*TODO*///		ROM_LOAD( "17701", 0x40000, 0x40000, 0x6dfb025b );
/*TODO*///		ROM_LOAD( "17702", 0x80000, 0x40000, 0xae0b7eab );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "17692", 0x00000, 0x20000, 0x543c4327 );
/*TODO*///		ROM_LOAD( "17695", 0x20000, 0x20000, 0xee60f244 );
/*TODO*///		ROM_LOAD( "17693", 0x40000, 0x20000, 0x4a350b3e );
/*TODO*///		ROM_LOAD( "17696", 0x60000, 0x20000, 0x6edb54f1 );
/*TODO*///		ROM_LOAD( "17694", 0x80000, 0x20000, 0xb296d71d );
/*TODO*///		ROM_LOAD( "17697", 0xa0000, 0x20000, 0x6ccb7b28 );
/*TODO*///		ROM_LOAD( "17698", 0xc0000, 0x20000, 0xcd4dfb82 );
/*TODO*///		ROM_LOAD( "17699", 0xe0000, 0x20000, 0x2694ecce );
/*TODO*///	
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "17691", 0x00000,  0x08000, 0x14205388 );
/*TODO*///		ROM_LOAD( "17690", 0x10000,  0x40000, 0x4f9ba4e4 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static MemoryReadAddress toryumon_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x3e2000, 0x3e2003, MRA_EXTRAM ),
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x440000, 0x440fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x840000, 0x840fff, paletteram_word_r ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0xe40000, 0xe40001, MRA_EXTRAM2 ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0xe41002, 0xe41003, input_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xe41004, 0xe41005, MRA_NOP ),
/*TODO*///		new MemoryReadAddress( 0xe41006, 0xe41007, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0xe41000, 0xe41001, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xe42002, 0xe42003, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xe42000, 0xe42001, io_dip2_r ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress toryumon_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x3e2000, 0x3e2003, MWA_EXTRAM ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x440000, 0x440fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x840000, 0x840fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xe40000, 0xe40001, MWA_BANK4,sys16_extraram2 ),
/*TODO*///		new MemoryWriteAddress( 0xfe0006, 0xfe0007, sound_command_w ),
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void toryumon_update_proc( void ){
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0e98] );
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0e9a] );
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0e90] );
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0e92] );
/*TODO*///	
/*TODO*///		set_fg_page( READ_WORD( &sys16_textram[0x0e80] ) );
/*TODO*///		set_bg_page( READ_WORD( &sys16_textram[0x0e82] ) );
/*TODO*///	
/*TODO*///		sys16_tile_bank0 = READ_WORD( &sys16_extraram[0x0000] )&0xf;
/*TODO*///		sys16_tile_bank1 = READ_WORD( &sys16_extraram[0x0002] )&0xf;
/*TODO*///	
/*TODO*///		set_refresh( READ_WORD( &sys16_extraram2[0] ) );
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr toryumon_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = {00,0x02,0x04,0x06,0x08,0x0a,0x0c,0x0e,00,00,00,00,00,00,00,00};
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///	
/*TODO*///		sys16_update_proc = toryumon_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_toryumon = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_sprite_decode( 4,0x40000 );
/*TODO*///	} };
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_toryumon = new InputPortPtr(){ public void handler() { 
/*TODO*///		SYS16_JOY1
/*TODO*///		SYS16_JOY2
/*TODO*///		SYS16_SERVICE
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unused") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unused") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unused") );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, "VS-Mode Battle" );
/*TODO*///		PORT_DIPSETTING(    0x10, "1" );
/*TODO*///		PORT_DIPSETTING(    0x00, "3" );
/*TODO*///		PORT_DIPNAME( 0xe0, 0xe0, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0xc0, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0xe0, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0xa0, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x80, "Hard+1" );
/*TODO*///		PORT_DIPSETTING(    0x60, "Hard+2" );
/*TODO*///		PORT_DIPSETTING(    0x40, "Hard+3" );
/*TODO*///		PORT_DIPSETTING(    0x20, "Hard+4" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hard+5" );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_7759( machine_driver_toryumon, \
/*TODO*///		toryumon_readmem,toryumon_writemem,toryumon_init_machine, gfx4,upd7759_interface )
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	// sys16B
/*TODO*///	static RomLoadPtr rom_tturf = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x40000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "12327.7a",  0x00000, 0x20000, 0x0376c593 )
/*TODO*///		ROM_LOAD_ODD ( "12326.5a",  0x00000, 0x20000, 0xf998862b )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "12268.14a", 0x00000, 0x10000, 0xe0dac07f );
/*TODO*///		ROM_LOAD( "12269.15a", 0x10000, 0x10000, 0x457a8790 );
/*TODO*///		ROM_LOAD( "12270.16a", 0x20000, 0x10000, 0x69fc025b );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x80000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "12279.1b", 0x00000, 0x10000, 0x7a169fb1 );
/*TODO*///		ROM_LOAD( "12283.5b", 0x10000, 0x10000, 0xae0fa085 );
/*TODO*///		ROM_LOAD( "12278.2b", 0x20000, 0x10000, 0x961d06b7 );
/*TODO*///		ROM_LOAD( "12282.6b", 0x30000, 0x10000, 0xe8671ee1 );
/*TODO*///		ROM_LOAD( "12277.3b", 0x40000, 0x10000, 0xf16b6ba2 );
/*TODO*///		ROM_LOAD( "12281.7b", 0x50000, 0x10000, 0x1ef1077f );
/*TODO*///		ROM_LOAD( "12276.4b", 0x60000, 0x10000, 0x838bd71f );
/*TODO*///		ROM_LOAD( "12280.8b", 0x70000, 0x10000, 0x639a57cb );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "12328.10a", 0x0000, 0x8000, 0x00000000 );
/*TODO*///		ROM_LOAD( "12329.11a", 0x10000, 0x10000, 0xed9a686d );	// speech
/*TODO*///		ROM_LOAD( "12330.12a", 0x20000, 0x10000, 0xfb762bca );
/*TODO*///	
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	// sys16B
/*TODO*///	static RomLoadPtr rom_tturfu = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x40000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "epr12266.bin",  0x00000, 0x10000, 0xf549def8 )
/*TODO*///		ROM_LOAD_ODD ( "epr12264.bin",  0x00000, 0x10000, 0xf7cdb289 )
/*TODO*///		ROM_LOAD_EVEN( "epr12267.bin",  0x20000, 0x10000, 0x3c3ce191 )
/*TODO*///		ROM_LOAD_ODD ( "epr12265.bin",  0x20000, 0x10000, 0x8cdadd9a )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "12268.14a", 0x00000, 0x10000, 0xe0dac07f );
/*TODO*///		ROM_LOAD( "12269.15a", 0x10000, 0x10000, 0x457a8790 );
/*TODO*///		ROM_LOAD( "12270.16a", 0x20000, 0x10000, 0x69fc025b );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x80000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "12279.1b", 0x00000, 0x10000, 0x7a169fb1 );
/*TODO*///		ROM_LOAD( "12283.5b", 0x10000, 0x10000, 0xae0fa085 );
/*TODO*///		ROM_LOAD( "12278.2b", 0x20000, 0x10000, 0x961d06b7 );
/*TODO*///		ROM_LOAD( "12282.6b", 0x30000, 0x10000, 0xe8671ee1 );
/*TODO*///		ROM_LOAD( "12277.3b", 0x40000, 0x10000, 0xf16b6ba2 );
/*TODO*///		ROM_LOAD( "12281.7b", 0x50000, 0x10000, 0x1ef1077f );
/*TODO*///		ROM_LOAD( "12276.4b", 0x60000, 0x10000, 0x838bd71f );
/*TODO*///		ROM_LOAD( "12280.8b", 0x70000, 0x10000, 0x639a57cb );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr12271.bin", 0x0000,  0x8000, 0x99671e52 );
/*TODO*///		ROM_LOAD( "epr12272.bin", 0x10000, 0x8000, 0x7cf7e69f );
/*TODO*///		ROM_LOAD( "epr12273.bin", 0x18000, 0x8000, 0x28f0bb8b );
/*TODO*///		ROM_LOAD( "epr12274.bin", 0x20000, 0x8000, 0x8207f0c4 );
/*TODO*///		ROM_LOAD( "epr12275.bin", 0x28000, 0x8000, 0x182f3c3d );
/*TODO*///	
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	public static ReadHandlerPtr tt_io_player1_r = new ReadHandlerPtr() { public int handler(int offset){ return input_port_0_r( offset ) << 8; } };
/*TODO*///	public static ReadHandlerPtr tt_io_player2_r = new ReadHandlerPtr() { public int handler(int offset){ return input_port_1_r( offset ) << 8; } };
/*TODO*///	public static ReadHandlerPtr tt_io_service_r = new ReadHandlerPtr() { public int handler(int offset){ return input_port_2_r( offset ) << 8; } };
/*TODO*///	
/*TODO*///	static MemoryReadAddress tturf_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x2001e6, 0x2001e7, tt_io_service_r ),
/*TODO*///		new MemoryReadAddress( 0x2001e8, 0x2001e9, tt_io_player1_r ),
/*TODO*///		new MemoryReadAddress( 0x2001ea, 0x2001eb, tt_io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0x200000, 0x203fff, MRA_EXTRAM ),
/*TODO*///		new MemoryReadAddress( 0x300000, 0x300fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x500000, 0x500fff, paletteram_word_r ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0x602002, 0x602003, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0x602000, 0x602001, io_dip2_r ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress tturf_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x200000, 0x203fff, MWA_EXTRAM ),
/*TODO*///		new MemoryWriteAddress( 0x300000, 0x300fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x500000, 0x500fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0x600000, 0x600005, MWA_BANK4,sys16_extraram2 ),
/*TODO*///	//	new MemoryWriteAddress( 0x600006, 0x600007, sound_command_w ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	/***************************************************************************/
/*TODO*///	static void tturf_update_proc( void ){
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0e98] );
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0e9a] );
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0e90] );
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0e92] );
/*TODO*///	
/*TODO*///		set_fg_page( READ_WORD( &sys16_textram[0x0e80] ) );
/*TODO*///		set_bg_page( READ_WORD( &sys16_textram[0x0e82] ) );
/*TODO*///	
/*TODO*///		set_refresh( READ_WORD( &sys16_extraram2[0] ) );
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr tturf_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = {00,00,0x02,00,0x04,00,0x06,00,00,00,00,00,00,00,00,00};
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_spritelist_end=0xc000;
/*TODO*///	
/*TODO*///		sys16_update_proc = tturf_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitMachinePtr tturfu_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = {00,00,00,00,00,00,00,00,00,00,00,02,00,04,06,00};
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_spritelist_end=0xc000;
/*TODO*///	
/*TODO*///		sys16_update_proc = tturf_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_tturf = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_sprite_decode( 4,0x20000 );
/*TODO*///	} };
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_tturf = new InputPortPtr(){ public void handler() { 
/*TODO*///		SYS16_JOY1
/*TODO*///		SYS16_JOY2
/*TODO*///		SYS16_SERVICE
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x03, 0x00, "Continues" );
/*TODO*///		PORT_DIPSETTING(    0x00, "None" );
/*TODO*///		PORT_DIPSETTING(    0x01, "3" );
/*TODO*///		PORT_DIPSETTING(    0x02, "Unlimited" );
/*TODO*///		PORT_DIPSETTING(    0x03, "Unlimited" );
/*TODO*///		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x08, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x0c, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x04, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0x30, 0x20, "Starting Energy" );
/*TODO*///		PORT_DIPSETTING(    0x00, "3" );
/*TODO*///		PORT_DIPSETTING(    0x10, "4" );
/*TODO*///		PORT_DIPSETTING(    0x20, "6" );
/*TODO*///		PORT_DIPSETTING(    0x30, "8" );
/*TODO*///		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x00, "Bonus Energy" );
/*TODO*///		PORT_DIPSETTING(    0x80, "1" );
/*TODO*///		PORT_DIPSETTING(    0x00, "2" );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_7759( machine_driver_tturf, \
/*TODO*///		tturf_readmem,tturf_writemem,tturf_init_machine, gfx1,upd7759_interface )
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_7759( machine_driver_tturfu, \
/*TODO*///		tturf_readmem,tturf_writemem,tturfu_init_machine, gfx1,upd7759_interface )
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	// sys16B
/*TODO*///	static RomLoadPtr rom_tturfbl = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x40000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "tt042197.rom", 0x00000, 0x10000, 0xdeee5af1 )
/*TODO*///		ROM_LOAD_ODD ( "tt06c794.rom", 0x00000, 0x10000, 0x90e6a95a )
/*TODO*///		ROM_LOAD_EVEN( "tt030be3.rom", 0x20000, 0x10000, 0x100264a2 )
/*TODO*///		ROM_LOAD_ODD ( "tt05ef8a.rom", 0x20000, 0x10000, 0xf787a948 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "tt1574b3.rom", 0x00000, 0x10000, 0xe9e630da );
/*TODO*///		ROM_LOAD( "tt16cf44.rom", 0x10000, 0x10000, 0x4c467735 );
/*TODO*///		ROM_LOAD( "tt17d59e.rom", 0x20000, 0x10000, 0x60c0f2fe );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x80000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "12279.1b", 0x00000, 0x10000, 0x7a169fb1 );
/*TODO*///		ROM_LOAD( "12283.5b", 0x10000, 0x10000, 0xae0fa085 );
/*TODO*///		ROM_LOAD( "12278.2b", 0x20000, 0x10000, 0x961d06b7 );
/*TODO*///		ROM_LOAD( "12282.6b", 0x30000, 0x10000, 0xe8671ee1 );
/*TODO*///		ROM_LOAD( "12277.3b", 0x40000, 0x10000, 0xf16b6ba2 );
/*TODO*///		ROM_LOAD( "12281.7b", 0x50000, 0x10000, 0x1ef1077f );
/*TODO*///		ROM_LOAD( "12276.4b", 0x60000, 0x10000, 0x838bd71f );
/*TODO*///		ROM_LOAD( "12280.8b", 0x70000, 0x10000, 0x639a57cb );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x28000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "tt014d68.rom", 0x00000, 0x08000, 0xd4aab1d9 );
/*TODO*///		ROM_CONTINUE(             0x10000, 0x08000 );
/*TODO*///		ROM_LOAD( "tt0246ff.rom", 0x18000, 0x10000, 0xbb4bba8f );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static MemoryReadAddress tturfbl_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x2001e6, 0x2001e7, tt_io_service_r ),
/*TODO*///		new MemoryReadAddress( 0x2001e8, 0x2001e9, tt_io_player1_r ),
/*TODO*///		new MemoryReadAddress( 0x2001ea, 0x2001eb, tt_io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0x200000, 0x203fff, MRA_EXTRAM ),
/*TODO*///		new MemoryReadAddress( 0x300000, 0x300fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x500000, 0x500fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0x600002, 0x600003, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0x600000, 0x600001, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0x601002, 0x601003, input_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0x601004, 0x601005, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0x601000, 0x601001, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0x602002, 0x602003, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0x602000, 0x602001, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xc46000, 0xc4601f, MRA_EXTRAM3 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress tturfbl_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x200000, 0x203fff, MWA_EXTRAM ),
/*TODO*///		new MemoryWriteAddress( 0x300000, 0x300fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x500000, 0x500fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0x600000, 0x600005, MWA_BANK4,sys16_extraram2 ),
/*TODO*///		new MemoryWriteAddress( 0x600006, 0x600007, sound_command_w ),
/*TODO*///		new MemoryWriteAddress( 0xc44000, 0xc44001, MWA_NOP ),
/*TODO*///		new MemoryWriteAddress( 0xc46000, 0xc4601f, MWA_EXTRAM3 ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void tturfbl_update_proc( void ){
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0e98] ) & 0x01ff;
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0e9a] ) & 0x01ff;
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0e90] );
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0e92] );
/*TODO*///	
/*TODO*///	
/*TODO*///		{
/*TODO*///			int data1,data2;
/*TODO*///	
/*TODO*///			data1 = READ_WORD( &sys16_textram[0x0e80] );
/*TODO*///			data2 = READ_WORD( &sys16_textram[0x0e82] );
/*TODO*///	
/*TODO*///			sys16_fg_page[3] = data1>>12;
/*TODO*///			sys16_bg_page[3] = (data1>>8)&0xf;
/*TODO*///			sys16_fg_page[1] = (data1>>4)&0xf;
/*TODO*///			sys16_bg_page[1] = data1&0xf;
/*TODO*///	
/*TODO*///			sys16_fg_page[2] = data2>>12;
/*TODO*///			sys16_bg_page[2] = (data2>>8)&0xf;
/*TODO*///			sys16_fg_page[0] = (data2>>4)&0xf;
/*TODO*///			sys16_bg_page[0] = data2&0xf;
/*TODO*///		}
/*TODO*///	
/*TODO*///	
/*TODO*///		set_refresh( READ_WORD( &sys16_extraram2[0] ) );
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr tturfbl_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = {00,00,00,00,00,00,00,0x06,00,00,00,0x04,00,0x02,00,00};
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_sprxoffset = -0x48;
/*TODO*///		sys16_spritelist_end=0xc000;
/*TODO*///	
/*TODO*///		sys16_update_proc = tturfbl_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_tturfbl = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///	
/*TODO*///		/* invert the graphics bits on the tiles */
/*TODO*///		for (i = 0; i < 0x30000; i++)
/*TODO*///			memory_region(REGION_GFX1)[i] ^= 0xff;
/*TODO*///	
/*TODO*///		sys16_sprite_decode( 4,0x20000 );
/*TODO*///	} };
/*TODO*///	/***************************************************************************/
/*TODO*///	// sound ??
/*TODO*///	MACHINE_DRIVER_7759( machine_driver_tturfbl, \
/*TODO*///		tturfbl_readmem,tturfbl_writemem,tturfbl_init_machine, gfx1,upd7759_interface )
/*TODO*///	
/*TODO*///	/***************************************************************************/
    // sys16B
    static RomLoadPtr rom_wb3 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("epr12259.a7", 0x000000, 0x20000, 0x54927c7e);
            ROM_LOAD_ODD("epr12258.a5", 0x000000, 0x20000, 0x01f5898c);

            ROM_REGION(0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* tiles */
            ROM_LOAD("epr12124.a14", 0x00000, 0x10000, 0xdacefb6f);
            ROM_LOAD("epr12125.a15", 0x10000, 0x10000, 0x9fc36df7);
            ROM_LOAD("epr12126.a16", 0x20000, 0x10000, 0xa693fd94);

            ROM_REGION(0x080000 * 2, REGION_GFX2);/* sprites */
            ROM_LOAD("epr12093.b4", 0x000000, 0x010000, 0x4891e7bb);
            ROM_LOAD("epr12097.b8", 0x010000, 0x010000, 0xe645902c);
            ROM_LOAD("epr12091.b2", 0x020000, 0x010000, 0x8409a243);
            ROM_LOAD("epr12095.b6", 0x030000, 0x010000, 0xe774ec2c);
            ROM_LOAD("epr12090.b1", 0x040000, 0x010000, 0xaeeecfca);
            ROM_LOAD("epr12094.b5", 0x050000, 0x010000, 0x615e4927);
            ROM_LOAD("epr12092.b3", 0x060000, 0x010000, 0x5c2f0d90);
            ROM_LOAD("epr12096.b7", 0x070000, 0x010000, 0x0cd59d6e);

            ROM_REGION(0x10000, REGION_CPU2);/* sound CPU */
            ROM_LOAD("epr12127.a10", 0x0000, 0x8000, 0x0bb901bb);
            ROM_END();
        }
    };

    static RomLoadPtr rom_wb3a = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);/* 68000 code */
            // Custom CPU 317-0089
            ROM_LOAD_EVEN("epr12137.a7", 0x000000, 0x20000, 0x6f81238e);
            ROM_LOAD_ODD("epr12136.a5", 0x000000, 0x20000, 0x4cf05003);

            ROM_REGION(0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* tiles */
            ROM_LOAD("epr12124.a14", 0x00000, 0x10000, 0xdacefb6f);
            ROM_LOAD("epr12125.a15", 0x10000, 0x10000, 0x9fc36df7);
            ROM_LOAD("epr12126.a16", 0x20000, 0x10000, 0xa693fd94);

            ROM_REGION(0x080000 * 2, REGION_GFX2);/* sprites */
            ROM_LOAD("epr12093.b4", 0x000000, 0x010000, 0x4891e7bb);
            ROM_LOAD("epr12097.b8", 0x010000, 0x010000, 0xe645902c);
            ROM_LOAD("epr12091.b2", 0x020000, 0x010000, 0x8409a243);
            ROM_LOAD("epr12095.b6", 0x030000, 0x010000, 0xe774ec2c);
            ROM_LOAD("epr12090.b1", 0x040000, 0x010000, 0xaeeecfca);
            ROM_LOAD("epr12094.b5", 0x050000, 0x010000, 0x615e4927);
            ROM_LOAD("epr12092.b3", 0x060000, 0x010000, 0x5c2f0d90);
            ROM_LOAD("epr12096.b7", 0x070000, 0x010000, 0x0cd59d6e);

            ROM_REGION(0x10000, REGION_CPU2);/* sound CPU */
            ROM_LOAD("epr12127.a10", 0x0000, 0x8000, 0x0bb901bb);
            ROM_END();
        }
    };

    /**
     * ************************************************************************
     */
    static MemoryReadAddress wb3_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x03ffff, MRA_ROM),
                new MemoryReadAddress(0x400000, 0x40ffff, sys16_tileram_r),
                new MemoryReadAddress(0x410000, 0x410fff, sys16_textram_r),
                new MemoryReadAddress(0x440000, 0x440fff, MRA_BANK2),
                new MemoryReadAddress(0x840000, 0x840fff, paletteram_word_r),
                new MemoryReadAddress(0xc41002, 0xc41003, input_port_0_r),
                new MemoryReadAddress(0xc41006, 0xc41007, input_port_1_r),
                new MemoryReadAddress(0xc41000, 0xc41001, input_port_2_r),
                new MemoryReadAddress(0xc42002, 0xc42003, input_port_3_r),
                new MemoryReadAddress(0xc42000, 0xc42001, input_port_4_r),
                new MemoryReadAddress(0xffc000, 0xffffff, MRA_BANK1),
                new MemoryReadAddress(-1)
            };

    public static WriteHandlerPtr wb3_sound_command_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 0xff000000) == 0) {
                sound_command_w.handler(offset, data >> 8);
            }
        }
    };

    static MemoryWriteAddress wb3_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x03ffff, MWA_ROM),
                new MemoryWriteAddress(0x3f0000, 0x3f0003, MWA_NOP),
                new MemoryWriteAddress(0x400000, 0x40ffff, sys16_tileram_w, sys16_tileram),
                new MemoryWriteAddress(0x410000, 0x410fff, sys16_textram_w, sys16_textram),
                new MemoryWriteAddress(0x440000, 0x440fff, MWA_BANK2, sys16_spriteram),
                new MemoryWriteAddress(0x840000, 0x840fff, sys16_paletteram_w, paletteram),
                new MemoryWriteAddress(0xc40000, 0xc40001, MWA_BANK4, sys16_extraram2),
                new MemoryWriteAddress(0xffc008, 0xffc009, wb3_sound_command_w),
                new MemoryWriteAddress(0xffc000, 0xffffff, MWA_BANK1, sys16_workingram),
                new MemoryWriteAddress(-1)
            };
    /**
     * ************************************************************************
     */
    public static sys16_update_procPtr wb3_update_proc = new sys16_update_procPtr() {
        public void handler() {
            sys16_fg_scrollx = sys16_textram.READ_WORD(0x0e98);
            sys16_bg_scrollx = sys16_textram.READ_WORD(0x0e9a);
            sys16_fg_scrolly = sys16_textram.READ_WORD(0x0e90);
            sys16_bg_scrolly = sys16_textram.READ_WORD(0x0e92);

            set_fg_page(sys16_textram.READ_WORD(0x0e80));
            set_bg_page(sys16_textram.READ_WORD(0x0e82));
            set_refresh(sys16_extraram2.READ_WORD(0));
        }
    };

    public static InitMachinePtr wb3_init_machine = new InitMachinePtr() {
        public void handler() {
            int bank[] = {4, 0, 2, 0, 6, 0, 0, 0x06, 0, 0, 0, 0x04, 0, 0x02, 0, 0};

            sys16_obj_bank = bank;

            sys16_update_proc = wb3_update_proc;
        }
    };

    public static InitDriverPtr init_wb3 = new InitDriverPtr() {
        public void handler() {
            sys16_onetime_init_machine.handler();
            sys16_sprite_decode(4, 0x20000);
        }
    };

    /**
     * ************************************************************************
     */
    static InputPortPtr input_ports_wb3 = new InputPortPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x07, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x09, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x05, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x04, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x02, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0x03, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x06, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x00, "Free Play (if Coin B too) or 1/1");
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x70, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x90, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x50, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x40, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x10, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x20, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0x30, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x60, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0xa0, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x00, "Free Play (if Coin A too) or 1/1");

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x0c, "3");
            PORT_DIPSETTING(0x08, "4");
            PORT_DIPSETTING(0x04, "5");
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Bonus_Life"));		//??
            PORT_DIPSETTING(0x10, "5000/10000/18000/30000");
            PORT_DIPSETTING(0x00, "5000/15000/30000");
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, "Allow Round Select");
            PORT_DIPSETTING(0x40, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));			// no collision though
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unused"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            INPUT_PORTS_END();
        }
    };

    /**
     * ************************************************************************
     */
    static MachineDriver machine_driver_wb3 = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000,
                        wb3_readmem, wb3_writemem, null, null,
                        sys16_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4096000,
                        sound_readmem, sound_writemem, sound_readport, sound_writeport,
                        ignore_interrupt, 1
                ),},
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1,
            wb3_init_machine,
            40 * 8, 28 * 8, new rectangle(0 * 8, 40 * 8 - 1, 0 * 8, 28 * 8 - 1),
            gfx1,
            2048 * ShadowColorsMultiplier, 2048 * ShadowColorsMultiplier,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            sys16_vh_start,
            sys16_vh_stop,
            sys16_vh_screenrefresh,
            SOUND_SUPPORTS_STEREO, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface
                )
            }
    );
    /*TODO*///	/***************************************************************************/
/*TODO*///	// sys16B
/*TODO*///	static RomLoadPtr rom_wb3bl = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x040000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "wb3_03", 0x000000, 0x10000, 0x0019ab3b )
/*TODO*///		ROM_LOAD_ODD ( "wb3_05", 0x000000, 0x10000, 0x196e17ee )
/*TODO*///		ROM_LOAD_EVEN( "wb3_02", 0x020000, 0x10000, 0xc87350cb )
/*TODO*///		ROM_LOAD_ODD ( "wb3_04", 0x020000, 0x10000, 0x565d5035 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "wb3_14", 0x00000, 0x10000, 0xd3f20bca );
/*TODO*///		ROM_LOAD( "wb3_15", 0x10000, 0x10000, 0x96ff9d52 );
/*TODO*///		ROM_LOAD( "wb3_16", 0x20000, 0x10000, 0xafaf0d31 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x080000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "epr12093.b4", 0x000000, 0x010000, 0x4891e7bb );
/*TODO*///		ROM_LOAD( "epr12097.b8", 0x010000, 0x010000, 0xe645902c );
/*TODO*///		ROM_LOAD( "epr12091.b2", 0x020000, 0x010000, 0x8409a243 );
/*TODO*///		ROM_LOAD( "epr12095.b6", 0x030000, 0x010000, 0xe774ec2c );
/*TODO*///		ROM_LOAD( "epr12090.b1", 0x040000, 0x010000, 0xaeeecfca );
/*TODO*///		ROM_LOAD( "epr12094.b5", 0x050000, 0x010000, 0x615e4927 );
/*TODO*///		ROM_LOAD( "epr12092.b3", 0x060000, 0x010000, 0x5c2f0d90 );
/*TODO*///		ROM_LOAD( "epr12096.b7", 0x070000, 0x010000, 0x0cd59d6e );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr12127.a10", 0x0000, 0x8000, 0x0bb901bb );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static MemoryReadAddress wb3bl_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x440000, 0x440fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x840000, 0x840fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0xc41002, 0xc41003, input_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xc41004, 0xc41005, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0xc41000, 0xc41001, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xc42002, 0xc42003, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xc42000, 0xc42001, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xc46000, 0xc4601f, MRA_EXTRAM3 ),
/*TODO*///		new MemoryReadAddress( 0xff0000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress wb3bl_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x3f0000, 0x3f0003, MWA_NOP ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x440000, 0x440fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x840000, 0x840fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xc42006, 0xc42007, sound_command_w ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc40001, MWA_BANK4,sys16_extraram2 ),
/*TODO*///		new MemoryWriteAddress( 0xc44000, 0xc44001, MWA_NOP ),
/*TODO*///		new MemoryWriteAddress( 0xc46000, 0xc4601f, MWA_EXTRAM3 ),
/*TODO*///		new MemoryWriteAddress( 0xff0000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void wb3bl_update_proc( void ){
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_workingram[0xc030] );
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_workingram[0xc038] );
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_workingram[0xc032] );
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_workingram[0xc03c] );
/*TODO*///	
/*TODO*///		set_fg_page( READ_WORD( &sys16_textram[0x0ff6] ) );
/*TODO*///		set_bg_page( READ_WORD( &sys16_textram[0x0ff4] ) );
/*TODO*///		set_refresh( READ_WORD( &sys16_extraram2[0] ) );
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr wb3bl_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = {4,0,2,0,6,0,0,0x06,0,0,0,0x04,0,0x02,0,0};
/*TODO*///	
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///	
/*TODO*///		patch_code( 0x17058, 0x4e );
/*TODO*///		patch_code( 0x17059, 0xb9 );
/*TODO*///		patch_code( 0x1705a, 0x00 );
/*TODO*///		patch_code( 0x1705b, 0x00 );
/*TODO*///		patch_code( 0x1705c, 0x09 );
/*TODO*///		patch_code( 0x1705d, 0xdc );
/*TODO*///		patch_code( 0x1705e, 0x4e );
/*TODO*///		patch_code( 0x1705f, 0xf9 );
/*TODO*///		patch_code( 0x17060, 0x00 );
/*TODO*///		patch_code( 0x17061, 0x01 );
/*TODO*///		patch_code( 0x17062, 0x70 );
/*TODO*///		patch_code( 0x17063, 0xe0 );
/*TODO*///		patch_code( 0x1a3a, 0x31 );
/*TODO*///		patch_code( 0x1a3b, 0x7c );
/*TODO*///		patch_code( 0x1a3c, 0x80 );
/*TODO*///		patch_code( 0x1a3d, 0x00 );
/*TODO*///		patch_code( 0x23df8, 0x14 );
/*TODO*///		patch_code( 0x23df9, 0x41 );
/*TODO*///		patch_code( 0x23dfa, 0x10 );
/*TODO*///		patch_code( 0x23dfd, 0x14 );
/*TODO*///		patch_code( 0x23dff, 0x1c );
/*TODO*///	
/*TODO*///		sys16_update_proc = wb3bl_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_wb3bl = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///	
/*TODO*///		/* invert the graphics bits on the tiles */
/*TODO*///		for (i = 0; i < 0x30000; i++)
/*TODO*///			memory_region(REGION_GFX1)[i] ^= 0xff;
/*TODO*///	
/*TODO*///		sys16_sprite_decode( 4,0x20000 );
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER( machine_driver_wb3bl, \
/*TODO*///		wb3bl_readmem,wb3bl_writemem,wb3bl_init_machine, gfx1 )
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	// sys16B
/*TODO*///	static RomLoadPtr rom_wrestwar = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0xc0000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "ww.a7", 0x00000, 0x20000, 0xeeaba126 )
/*TODO*///		ROM_LOAD_ODD ( "ww.a5", 0x00000, 0x20000, 0x6714600a )
/*TODO*///		/* empty 0x40000 - 0x80000 */
/*TODO*///		ROM_LOAD_EVEN( "ww.a8", 0x80000, 0x20000, 0xb77ba665 )
/*TODO*///		ROM_LOAD_ODD ( "ww.a6", 0x80000, 0x20000, 0xddf075cb )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x60000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "ww.a14", 0x00000, 0x20000, 0x6a821ab9 );
/*TODO*///		ROM_LOAD( "ww.a15", 0x20000, 0x20000, 0x2b1a0751 );
/*TODO*///		ROM_LOAD( "ww.a16", 0x40000, 0x20000, 0xf6e190fe );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x180000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "ww.b1",  0x000000, 0x20000, 0xffa7d368 );
/*TODO*///		ROM_LOAD( "ww.b5",  0x020000, 0x20000, 0x8d7794c1 );
/*TODO*///		ROM_LOAD( "ww.b2",  0x040000, 0x20000, 0x0ed343f2 );
/*TODO*///		ROM_LOAD( "ww.b6",  0x060000, 0x20000, 0x99458d58 );
/*TODO*///		ROM_LOAD( "ww.b3",  0x080000, 0x20000, 0x3087104d );
/*TODO*///		ROM_LOAD( "ww.b7",  0x0a0000, 0x20000, 0xabcf9bed );
/*TODO*///		ROM_LOAD( "ww.b4",  0x0c0000, 0x20000, 0x41b6068b );
/*TODO*///		ROM_LOAD( "ww.b8",  0x0e0000, 0x20000, 0x97eac164 );
/*TODO*///		ROM_LOAD( "ww.a1",  0x100000, 0x20000, 0x260311c5 );
/*TODO*///		ROM_LOAD( "ww.b10", 0x120000, 0x20000, 0x35a4b1b1 );
/*TODO*///		ROM_LOAD( "ww.a2",  0x140000, 0x10000, 0x12e38a5c );
/*TODO*///		ROM_LOAD( "ww.b11", 0x160000, 0x10000, 0xfa06fd24 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "ww.a10", 0x0000, 0x08000, 0xc3609607 );
/*TODO*///		ROM_LOAD( "ww.a11", 0x10000, 0x20000, 0xfb9a7f29 );
/*TODO*///		ROM_LOAD( "ww.a12", 0x30000, 0x20000, 0xd6617b19 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr ww_io_service_r = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		return io_service_r(offset) | (READ_WORD(&sys16_workingram[0x2082]) & 0xff00);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static MemoryReadAddress wrestwar_readmem[] =
/*TODO*///	{
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0x000000, 0x0bffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x100000, 0x10ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x110000, 0x110fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x200000, 0x200fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x300000, 0x300fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0x400000, 0x400003, MRA_EXTRAM ),
/*TODO*///		new MemoryReadAddress( 0xc40000, 0xc40001, MRA_EXTRAM2 ),
/*TODO*///		new MemoryReadAddress( 0xc41002, 0xc41003, input_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xc41006, 0xc41007, io_player2_r ),
/*TODO*///		new MemoryReadAddress( 0xc42002, 0xc42003, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xc42000, 0xc42001, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xffe082, 0xffe083, ww_io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress wrestwar_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x0bffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x100000, 0x10ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x110000, 0x110fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x200000, 0x200fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x300000, 0x300fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x400003, MWA_EXTRAM ),
/*TODO*///		new MemoryWriteAddress( 0xc40000, 0xc40001, MWA_BANK4,sys16_extraram2 ),
/*TODO*///		new MemoryWriteAddress( 0xc43034, 0xc43035, MWA_NOP ),
/*TODO*///		new MemoryWriteAddress( 0xffe08e, 0xffe08f, sound_command_w ),
/*TODO*///		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void wrestwar_update_proc( void ){
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0e98] );
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0e9a] );
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0e90] );
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0e92] );
/*TODO*///	
/*TODO*///		set_fg_page( READ_WORD( &sys16_textram[0x0e80] ) );
/*TODO*///		set_bg_page( READ_WORD( &sys16_textram[0x0e82] ) );
/*TODO*///		set_tile_bank( READ_WORD( &sys16_extraram[2] ) );
/*TODO*///		set_refresh( READ_WORD( &sys16_extraram2[0] ) );
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr wrestwar_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = {0x00,0x02,0x04,0x06,0x08,0x0A,0x0C,0x0E,0x10,0x12,0x14,0x16,0x18,0x1A,0x1C,0x1E};
/*TODO*///	
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_bg_priority_mode=2;
/*TODO*///		sys16_bg_priority_value=0x0a00;
/*TODO*///	
/*TODO*///		sys16_update_proc = wrestwar_update_proc;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_wrestwar = new InitDriverPtr() { public void handler() {
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_bg1_trans=1;
/*TODO*///		sys16_MaxShadowColors=16;
/*TODO*///		sys18_splittab_bg_y=&sys16_textram[0x0f40];
/*TODO*///		sys18_splittab_fg_y=&sys16_textram[0x0f00];
/*TODO*///		sys16_rowscroll_scroll=0x8000;
/*TODO*///	
/*TODO*///		sys16_sprite_decode( 6,0x40000 );
/*TODO*///	} };
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_wrestwar = new InputPortPtr(){ public void handler() { 
/*TODO*///		SYS16_JOY1
/*TODO*///		SYS16_JOY2
/*TODO*///		SYS16_SERVICE
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x0c, 0x0c, "Round Time" );
/*TODO*///		PORT_DIPSETTING(    0x00, "100" );
/*TODO*///		PORT_DIPSETTING(    0x0c, "110" );
/*TODO*///		PORT_DIPSETTING(    0x08, "120" );
/*TODO*///		PORT_DIPSETTING(    0x04, "130" );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, "Continuation" );
/*TODO*///		PORT_DIPSETTING(    0x20, "Continue" );
/*TODO*///		PORT_DIPSETTING(    0x00, "No Continue" );
/*TODO*///		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x80, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0xc0, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x40, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///	
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_7759( machine_driver_wrestwar, \
/*TODO*///		wrestwar_readmem,wrestwar_writemem,wrestwar_init_machine, gfx2,upd7759_interface )
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	/* hang-on's accel/brake are really both analog controls, but I've added them
/*TODO*///	as digital as well to see what works better */
/*TODO*///	#define HANGON_DIGITAL_CONTROLS
/*TODO*///	
/*TODO*///	// hangon hardware
/*TODO*///	static RomLoadPtr rom_hangon = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x020000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "6918.rom", 0x000000, 0x8000, 0x20b1c2b0 )
/*TODO*///		ROM_LOAD_ODD ( "6916.rom", 0x000000, 0x8000, 0x7d9db1bf )
/*TODO*///		ROM_LOAD_EVEN( "6917.rom", 0x010000, 0x8000, 0xfea12367 )
/*TODO*///		ROM_LOAD_ODD ( "6915.rom", 0x010000, 0x8000, 0xac883240 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "6841.rom", 0x00000, 0x08000, 0x54d295dc );
/*TODO*///		ROM_LOAD( "6842.rom", 0x08000, 0x08000, 0xf677b568 );
/*TODO*///		ROM_LOAD( "6843.rom", 0x10000, 0x08000, 0xa257f0da );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x080000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "6819.rom", 0x000000, 0x008000, 0x469dad07 );
/*TODO*///		ROM_RELOAD(           0x070000, 0x008000 );/* again? */
/*TODO*///		ROM_LOAD( "6820.rom", 0x008000, 0x008000, 0x87cbc6de );
/*TODO*///		ROM_RELOAD(           0x078000, 0x008000 );/* again? */
/*TODO*///		ROM_LOAD( "6821.rom", 0x010000, 0x008000, 0x15792969 );
/*TODO*///		ROM_LOAD( "6822.rom", 0x018000, 0x008000, 0xe9718de5 );
/*TODO*///		ROM_LOAD( "6823.rom", 0x020000, 0x008000, 0x49422691 );
/*TODO*///		ROM_LOAD( "6824.rom", 0x028000, 0x008000, 0x701deaa4 );
/*TODO*///		ROM_LOAD( "6825.rom", 0x030000, 0x008000, 0x6e23c8b4 );
/*TODO*///		ROM_LOAD( "6826.rom", 0x038000, 0x008000, 0x77d0de2c );
/*TODO*///		ROM_LOAD( "6827.rom", 0x040000, 0x008000, 0x7fa1bfb6 );
/*TODO*///		ROM_LOAD( "6828.rom", 0x048000, 0x008000, 0x8e880c93 );
/*TODO*///		ROM_LOAD( "6829.rom", 0x050000, 0x008000, 0x7ca0952d );
/*TODO*///		ROM_LOAD( "6830.rom", 0x058000, 0x008000, 0xb1a63aef );
/*TODO*///		ROM_LOAD( "6845.rom", 0x060000, 0x008000, 0xba08c9b8 );
/*TODO*///		ROM_LOAD( "6846.rom", 0x068000, 0x008000, 0xf21e57a3 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "6833.rom", 0x00000, 0x4000, 0x3b942f5f );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_SOUND1 );/* Sega PCM sound data */
/*TODO*///		ROM_LOAD( "6831.rom", 0x00000, 0x8000, 0xcfef5481 );
/*TODO*///		ROM_LOAD( "6832.rom", 0x08000, 0x8000, 0x4165aea5 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU3 );/* second 68000 CPU */
/*TODO*///		ROM_LOAD_EVEN("6920.rom", 0x0000, 0x8000, 0x1c95013e )
/*TODO*///		ROM_LOAD_ODD( "6919.rom", 0x0000, 0x8000, 0x6ca30d69 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_GFX3 );/* Road Graphics  (region size should be gr_bitmapwidth*256 )*/
/*TODO*///		ROM_LOAD( "6840.rom", 0x0000, 0x8000, 0x581230e3 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr ho_io_x_r = new ReadHandlerPtr() { public int handler(int offset){ return input_port_0_r( offset ); } };
/*TODO*///	#ifdef HANGON_DIGITAL_CONTROLS
/*TODO*///	public static ReadHandlerPtr ho_io_y_r = new ReadHandlerPtr() { public int handler(int offset){
/*TODO*///		int data = input_port_1_r( offset );
/*TODO*///	
/*TODO*///		switch(data & 3)
/*TODO*///		{
/*TODO*///			case 3:	return 0xffff;	// both
/*TODO*///			case 2:	return 0x00ff;  // brake
/*TODO*///			case 1:	return 0xff00;  // accel
/*TODO*///			case 0:	return 0x0000;  // neither
/*TODO*///		}
/*TODO*///		return 0x0000;
/*TODO*///	} };
/*TODO*///	#else
/*TODO*///	public static ReadHandlerPtr ho_io_y_r = new ReadHandlerPtr() { public int handler(int offset){ return (input_port_1_r( offset ) << 8) + input_port_5_r( offset ); } };
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr ho_io_highscoreentry_r = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int mode=READ_WORD(&sys16_extraram4[0x3000]);
/*TODO*///	
/*TODO*///		if(mode&4)
/*TODO*///		{	// brake
/*TODO*///			if(ho_io_y_r(0) & 0x00ff) return 0xffff;
/*TODO*///		}
/*TODO*///		else if(mode&8)
/*TODO*///		{
/*TODO*///			// button
/*TODO*///			if(ho_io_y_r(0) & 0xff00) return 0xffff;
/*TODO*///		}
/*TODO*///		return 0;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr hangon1_skip_r = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		if (cpu_get_pc()==0x17e6) {cpu_spinuntil_int(); return 0xffff;}
/*TODO*///	
/*TODO*///	//	return READ_WORD(&sys16_extraram[0xc400]);
/*TODO*///		return READ_WORD(&sys16_extraram[0x0400]);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	static MemoryReadAddress hangon_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x20c400, 0x20c401, hangon1_skip_r ),
/*TODO*///		new MemoryReadAddress( 0x20c000, 0x20ffff, MRA_EXTRAM ),
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x600000, 0x600fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0xa00000, 0xa00fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0xc68000, 0xc68fff, MRA_EXTRAM2 ),
/*TODO*///		new MemoryReadAddress( 0xc7e000, 0xc7ffff, MRA_EXTRAM3 ),
/*TODO*///		new MemoryReadAddress( 0xe01000, 0xe01001, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xe0100c, 0xe0100d, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xe0100a, 0xe0100b, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xe03020, 0xe03021, ho_io_highscoreentry_r ),
/*TODO*///		new MemoryReadAddress( 0xe03028, 0xe03029, ho_io_x_r ),
/*TODO*///		new MemoryReadAddress( 0xe0302a, 0xe0302b, ho_io_y_r ),
/*TODO*///		new MemoryReadAddress( 0xe00000, 0xe03fff, MRA_EXTRAM4 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress hangon_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x20c000, 0x20ffff, MWA_EXTRAM ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x600000, 0x600fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0xa00000, 0xa00fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xc68000, 0xc68fff, MWA_BANK4,sys16_extraram2 ),
/*TODO*///		new MemoryWriteAddress( 0xc7e000, 0xc7ffff, MWA_EXTRAM3 ),
/*TODO*///		new MemoryWriteAddress( 0xe00000, 0xe00001, sound_command_nmi_w ),
/*TODO*///		new MemoryWriteAddress( 0xe00000, 0xe03fff, MWA_EXTRAM4 ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr hangon2_skip_r = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		if (cpu_get_pc()==0xf66) {cpu_spinuntil_int(); return 0xffff;}
/*TODO*///	
/*TODO*///	//	return READ_WORD(&sys16_extraram2[0x3f000]);
/*TODO*///		return READ_WORD(&sys16_extraram3[0x01000]);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static MemoryReadAddress hangon_readmem2[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0xc7f000, 0xc7f001, hangon2_skip_r ),
/*TODO*///		new MemoryReadAddress( 0xc68000, 0xc68fff, MRA_EXTRAM2 ),
/*TODO*///		new MemoryReadAddress( 0xc7e000, 0xc7ffff, MRA_EXTRAM3 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress hangon_writemem2[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0xc68000, 0xc68fff, MWA_BANK4,sys16_extraram2 ),
/*TODO*///		new MemoryWriteAddress( 0xc7e000, 0xc7ffff, MWA_EXTRAM3 ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	static MemoryReadAddress hangon_sound_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0xc000, 0xc7ff, MRA_RAM ),
/*TODO*///		new MemoryReadAddress( 0xd000, 0xd000, YM2203_status_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xe000, 0xe7ff, SEGAPCMReadReg ),
/*TODO*///		new MemoryReadAddress( 0xf800, 0xffff, MRA_RAM ),
/*TODO*///		new MemoryReadAddress( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress hangon_sound_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0xc000, 0xc7ff, MWA_RAM ),
/*TODO*///		new MemoryWriteAddress( 0xd000, 0xd000, YM2203_control_port_0_w ),
/*TODO*///		new MemoryWriteAddress( 0xd001, 0xd001, YM2203_write_port_0_w ),
/*TODO*///		new MemoryWriteAddress( 0xe000, 0xe7ff, SEGAPCMWriteReg ),
/*TODO*///		new MemoryWriteAddress( 0xf800, 0xffff, MWA_RAM ),
/*TODO*///		new MemoryWriteAddress( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	static IOReadPort hangon_sound_readport[] =
/*TODO*///	{
/*TODO*///		new IOReadPort( 0x40, 0x40, soundlatch_r ),
/*TODO*///		new IOReadPort( -1 )	/* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	static IOWritePort hangon_sound_writeport[] =
/*TODO*///	{
/*TODO*///		new IOWritePort( -1 )
/*TODO*///	};
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void hangon_update_proc( void ){
/*TODO*///		int leds;
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0ff8] ) & 0x01ff;
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0ffa] ) & 0x01ff;
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0f24] ) & 0x00ff;
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0f26] ) & 0x01ff;
/*TODO*///	
/*TODO*///		set_fg_page1( READ_WORD( &sys16_textram[0x0e9e] ) );
/*TODO*///		set_bg_page1( READ_WORD( &sys16_textram[0x0e9c] ) );
/*TODO*///		set_refresh_3d( READ_WORD( &sys16_extraram4[0x2] ) );
/*TODO*///	
/*TODO*///		leds=READ_WORD( &sys16_extraram4[0x2] );
/*TODO*///		if(leds & 4)
/*TODO*///		{
/*TODO*///			osd_led_w(0,1);
/*TODO*///			osd_led_w(1,1);
/*TODO*///			osd_led_w(2,1);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			osd_led_w(0,0);
/*TODO*///			osd_led_w(1,0);
/*TODO*///			osd_led_w(2,0);
/*TODO*///		}
/*TODO*///	
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr hangon_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = { 00,01,02,03,04,05,06,00,01,02,03,04,05,06,00,06};
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_textmode=1;
/*TODO*///		sys16_spritesystem = 5;
/*TODO*///		sys16_sprxoffset = -0xc0;
/*TODO*///		sys16_fgxoffset = 8;
/*TODO*///		sys16_textlayer_lo_min=0;
/*TODO*///		sys16_textlayer_lo_max=0;
/*TODO*///		sys16_textlayer_hi_min=0;
/*TODO*///		sys16_textlayer_hi_max=0xff;
/*TODO*///	
/*TODO*///		patch_code( 0x83bd, 0x29);
/*TODO*///		patch_code( 0x8495, 0x2a);
/*TODO*///		patch_code( 0x84f9, 0x2b);
/*TODO*///	
/*TODO*///		sys16_update_proc = hangon_update_proc;
/*TODO*///	
/*TODO*///		gr_ver = &sys16_extraram2[0x0];
/*TODO*///		gr_hor = gr_ver+0x200;
/*TODO*///		gr_pal = gr_ver+0x400;
/*TODO*///		gr_flip= gr_ver+0x600;
/*TODO*///		gr_palette= 0xf80 / 2;
/*TODO*///		gr_palette_default = 0x70 /2;
/*TODO*///		gr_colorflip[0][0]=0x08 / 2;
/*TODO*///		gr_colorflip[0][1]=0x04 / 2;
/*TODO*///		gr_colorflip[0][2]=0x00 / 2;
/*TODO*///		gr_colorflip[0][3]=0x06 / 2;
/*TODO*///		gr_colorflip[1][0]=0x0a / 2;
/*TODO*///		gr_colorflip[1][1]=0x04 / 2;
/*TODO*///		gr_colorflip[1][2]=0x02 / 2;
/*TODO*///		gr_colorflip[1][3]=0x02 / 2;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_hangon = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///	
/*TODO*///		sys16_sprite_decode( 8,0x010000 );
/*TODO*///		generate_gr_screen(512,1024,8,0,4,0x8000);
/*TODO*///	} };
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_hangon = new InputPortPtr(){ public void handler() { 
/*TODO*///	PORT_START(); 	/* Steering */
/*TODO*///		PORT_ANALOG( 0xff, 0x7f, IPT_AD_STICK_X | IPF_REVERSE | IPF_CENTER , 100, 3, 0x48, 0xb7 );
/*TODO*///	
/*TODO*///	#ifdef HANGON_DIGITAL_CONTROLS
/*TODO*///	
/*TODO*///	PORT_START(); 	/* Buttons */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON2 );
/*TODO*///	
/*TODO*///	#else
/*TODO*///	
/*TODO*///	PORT_START(); 	/* Accel / Decel */
/*TODO*///		PORT_ANALOG( 0xff, 0x1, IPT_AD_STICK_Y | IPF_CENTER | IPF_REVERSE, 100, 16, 0, 0xa2 );
/*TODO*///	
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		SYS16_SERVICE
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x04, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x06, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x02, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0x18, 0x18, "Time Adj." );
/*TODO*///		PORT_DIPSETTING(    0x18, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x10, "Medium" );
/*TODO*///		PORT_DIPSETTING(    0x08, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, "Play Music" );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
/*TODO*///	
/*TODO*///	#ifndef HANGON_DIGITAL_CONTROLS
/*TODO*///	
/*TODO*///	PORT_START(); 	/* Brake */
/*TODO*///		PORT_ANALOG( 0xff, 0x1, IPT_AD_STICK_Y | IPF_PLAYER2 | IPF_CENTER | IPF_REVERSE, 100, 16, 0, 0xa2 );
/*TODO*///	
/*TODO*///	#endif
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static MachineDriver machine_driver_hangon = new MachineDriver
/*TODO*///	(
/*TODO*///		new MachineCPU[] {
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_M68000,
/*TODO*///				10000000,
/*TODO*///				hangon_readmem,hangon_writemem,null,null,
/*TODO*///				sys16_interrupt,1
/*TODO*///			),
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_Z80 | CPU_AUDIO_CPU,
/*TODO*///				4096000,
/*TODO*///				hangon_sound_readmem,hangon_sound_writemem,hangon_sound_readport,hangon_sound_writeport,
/*TODO*///	//			ignore_interrupt,1
/*TODO*///				interrupt,4
/*TODO*///			),
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_M68000,
/*TODO*///				10000000,
/*TODO*///				hangon_readmem2,hangon_writemem2,null,null,
/*TODO*///				sys16_interrupt,1
/*TODO*///			),
/*TODO*///		},
/*TODO*///		60, DEFAULT_60HZ_VBLANK_DURATION,
/*TODO*///		1,
/*TODO*///		hangon_init_machine,
/*TODO*///		40*8, 28*8, new rectangle( 0*8, 40*8-1, 0*8, 28*8-1 ),
/*TODO*///		gfx8,
/*TODO*///		2048*ShadowColorsMultiplier,2048*ShadowColorsMultiplier,
/*TODO*///		null,
/*TODO*///		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
/*TODO*///		null,
/*TODO*///		sys16_ho_vh_start,
/*TODO*///		sys16_vh_stop,
/*TODO*///		sys16_ho_vh_screenrefresh,
/*TODO*///		SOUND_SUPPORTS_STEREO,0,0,0,
/*TODO*///		new MachineSound[] {
/*TODO*///			new MachineSound(
/*TODO*///				SOUND_YM2203,
/*TODO*///				ym2203_interface
/*TODO*///			),
/*TODO*///			new MachineSound(			// wrong sound chip??
/*TODO*///				SOUND_SEGAPCM,
/*TODO*///				segapcm_interface_32k,
/*TODO*///			)
/*TODO*///		}
/*TODO*///	);
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	// space harrier / enduro racer hardware
/*TODO*///	static RomLoadPtr rom_sharrier = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x040000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "ic97.bin", 0x000000, 0x8000, 0x7c30a036 )
/*TODO*///		ROM_LOAD_ODD ( "ic84.bin", 0x000000, 0x8000, 0x16deaeb1 )
/*TODO*///		ROM_LOAD_EVEN( "ic98.bin", 0x010000, 0x8000, 0x40b1309f )
/*TODO*///		ROM_LOAD_ODD ( "ic85.bin", 0x010000, 0x8000, 0xce78045c )
/*TODO*///		ROM_LOAD_EVEN( "ic99.bin", 0x020000, 0x8000, 0xf6391091 )
/*TODO*///		ROM_LOAD_ODD ( "ic86.bin", 0x020000, 0x8000, 0x79b367d7 )
/*TODO*///		ROM_LOAD_EVEN( "ic100.bin", 0x030000, 0x8000, 0x6171e9d3 )
/*TODO*///		ROM_LOAD_ODD ( "ic87.bin", 0x030000, 0x8000, 0x70cb72ef )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "sic31.bin", 0x00000, 0x08000, 0x347fa325 );
/*TODO*///		ROM_LOAD( "sic46.bin", 0x08000, 0x08000, 0x39d98bd1 );
/*TODO*///		ROM_LOAD( "sic60.bin", 0x10000, 0x08000, 0x3da3ea6b );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "ic36.bin", 0x000000, 0x008000, 0x93e2d264 );
/*TODO*///		ROM_LOAD( "ic28.bin", 0x008000, 0x008000, 0xedbf5fc3 );
/*TODO*///		ROM_LOAD( "ic118.bin",0x010000, 0x008000, 0xe8c537d8 );
/*TODO*///		ROM_LOAD( "ic8.bin",  0x018000, 0x008000, 0x22844fa4 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "ic35.bin", 0x020000, 0x008000, 0xcd6e7500 );
/*TODO*///		ROM_LOAD( "ic27.bin", 0x028000, 0x008000, 0x41f25a9c );
/*TODO*///		ROM_LOAD( "ic17.bin", 0x030000, 0x008000, 0x5bb09a67 );
/*TODO*///		ROM_LOAD( "ic7.bin",  0x038000, 0x008000, 0xdcaa2ebf );
/*TODO*///	
/*TODO*///		ROM_LOAD( "ic34.bin", 0x040000, 0x008000, 0xd5e15e66 );
/*TODO*///		ROM_LOAD( "ic26.bin", 0x048000, 0x008000, 0xac62ae2e );
/*TODO*///		ROM_LOAD( "ic16.bin", 0x050000, 0x008000, 0x9c782295 );
/*TODO*///		ROM_LOAD( "ic6.bin",  0x058000, 0x008000, 0x3711105c );
/*TODO*///	
/*TODO*///		ROM_LOAD( "ic33.bin", 0x060000, 0x008000, 0x60d7c1bb );
/*TODO*///		ROM_LOAD( "ic25.bin", 0x068000, 0x008000, 0xf6330038 );
/*TODO*///		ROM_LOAD( "ic15.bin", 0x070000, 0x008000, 0x60737b98 );
/*TODO*///		ROM_LOAD( "ic5.bin",  0x078000, 0x008000, 0x70fb5ebb );
/*TODO*///	
/*TODO*///		ROM_LOAD( "ic32.bin", 0x080000, 0x008000, 0x6d7b5c97 );
/*TODO*///		ROM_LOAD( "ic24.bin", 0x088000, 0x008000, 0xcebf797c );
/*TODO*///		ROM_LOAD( "ic14.bin", 0x090000, 0x008000, 0x24596a8b );
/*TODO*///		ROM_LOAD( "ic4.bin",  0x098000, 0x008000, 0xb537d082 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "ic31.bin", 0x0a0000, 0x008000, 0x5e784271 );
/*TODO*///		ROM_LOAD( "ic23.bin", 0x0a8000, 0x008000, 0x510e5e10 );
/*TODO*///		ROM_LOAD( "ic13.bin", 0x0b0000, 0x008000, 0x7a2dad15 );
/*TODO*///		ROM_LOAD( "ic3.bin",  0x0b8000, 0x008000, 0xf5ba4e08 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "ic30.bin", 0x0c0000, 0x008000, 0xec42c9ef );
/*TODO*///		ROM_LOAD( "ic22.bin", 0x0c8000, 0x008000, 0x6d4a7d7a );
/*TODO*///		ROM_LOAD( "ic12.bin", 0x0d0000, 0x008000, 0x0f732717 );
/*TODO*///		ROM_LOAD( "ic2.bin",  0x0d8000, 0x008000, 0xfc3bf8f3 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "ic29.bin", 0x0e0000, 0x008000, 0xed51fdc4 );
/*TODO*///		ROM_LOAD( "ic21.bin", 0x0e8000, 0x008000, 0xdfe75f3d );
/*TODO*///		ROM_LOAD( "ic11.bin", 0x0f0000, 0x008000, 0xa2c07741 );
/*TODO*///		ROM_LOAD( "ic1.bin",  0x0f8000, 0x008000, 0xb191e22f );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "ic73.bin", 0x00000, 0x004000, 0xd6397933 );
/*TODO*///		ROM_LOAD( "ic72.bin", 0x04000, 0x004000, 0x504e76d9 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_SOUND1 );/* Sega PCM sound data */
/*TODO*///		ROM_LOAD( "snd7231.256", 0x00000, 0x008000, 0x871c6b14 );
/*TODO*///		ROM_LOAD( "snd7232.256", 0x08000, 0x008000, 0x4b59340c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU3 );/* second 68000 CPU */
/*TODO*///		ROM_LOAD_EVEN("ic54.bin", 0x0000, 0x8000, 0xd7c535b6 )
/*TODO*///		ROM_LOAD_ODD( "ic67.bin", 0x0000, 0x8000, 0xa6153af8 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_GFX3 );/* Road Graphics  (region size should be gr_bitmapwidth*256 )*/
/*TODO*///		ROM_LOAD( "pic2.bin", 0x0000, 0x8000, 0xb4740419 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr sh_io_joy_r = new ReadHandlerPtr() { public int handler(int offset){ return (input_port_5_r( offset ) << 8) + input_port_6_r( offset ); } };
/*TODO*///	
/*TODO*///	static unsigned char *shared_ram;
/*TODO*///	public static ReadHandlerPtr shared_ram_r = new ReadHandlerPtr() { public int handler(int offset){ return READ_WORD(&shared_ram[offset]); } };
/*TODO*///	public static WriteHandlerPtr shared_ram_w = new WriteHandlerPtr() { public void handler(int offset, int data){ COMBINE_WORD_MEM(&shared_ram[offset], data); } };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr sh_motor_status_r = new ReadHandlerPtr() { public int handler(int offset){ return 0x0; } };
/*TODO*///	
/*TODO*///	static MemoryReadAddress harrier_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x040000, 0x043fff, MRA_EXTRAM ),
/*TODO*///		new MemoryReadAddress( 0x100000, 0x107fff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x108000, 0x108fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x110000, 0x110fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0x124000, 0x127fff, shared_ram_r ),
/*TODO*///		new MemoryReadAddress( 0x130000, 0x130fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x140010, 0x140011, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0x140014, 0x140015, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0x140016, 0x140017, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0x140024, 0x140027, sh_motor_status_r ),
/*TODO*///		new MemoryReadAddress( 0x140000, 0x140027, MRA_EXTRAM3 ),		//io
/*TODO*///		new MemoryReadAddress( 0xc68000, 0xc68fff, MRA_EXTRAM2 ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress harrier_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x040000, 0x043fff, MWA_EXTRAM ),
/*TODO*///		new MemoryWriteAddress( 0x100000, 0x107fff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x108000, 0x108fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x110000, 0x110fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0x124000, 0x127fff, shared_ram_w, shared_ram ),
/*TODO*///		new MemoryWriteAddress( 0x130000, 0x130fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x140000, 0x140001, sound_command_nmi_w ),
/*TODO*///		new MemoryWriteAddress( 0x140000, 0x140027, MWA_EXTRAM3 ),		//io
/*TODO*///		new MemoryWriteAddress( 0xc68000, 0xc68fff, MWA_BANK4,sys16_extraram2 ),
/*TODO*///	
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryReadAddress harrier_readmem2[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0xc68000, 0xc68fff, MRA_EXTRAM2 ),
/*TODO*///		new MemoryReadAddress( 0xc7c000, 0xc7ffff, shared_ram_r ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress harrier_writemem2[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0xc68000, 0xc68fff, MWA_BANK4,sys16_extraram2 ),
/*TODO*///		new MemoryWriteAddress( 0xc7c000, 0xc7ffff, shared_ram_w, shared_ram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryReadAddress harrier_sound_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0xd000, 0xd000, YM2203_status_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xe000, 0xe0ff, SEGAPCMReadReg ),
/*TODO*///		new MemoryReadAddress( 0x8000, 0xffff, MRA_RAM ),
/*TODO*///		new MemoryReadAddress( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress harrier_sound_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0xd000, 0xd000, YM2203_control_port_0_w ),
/*TODO*///		new MemoryWriteAddress( 0xd001, 0xd001, YM2203_write_port_0_w ),
/*TODO*///		new MemoryWriteAddress( 0xe000, 0xe0ff, SEGAPCMWriteReg ),
/*TODO*///		new MemoryWriteAddress( 0x8000, 0xffff, MWA_RAM ),
/*TODO*///		new MemoryWriteAddress( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	static IOReadPort harrier_sound_readport[] =
/*TODO*///	{
/*TODO*///		new IOReadPort( 0x40, 0x40, soundlatch_r ),
/*TODO*///		new IOReadPort( -1 )	/* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	static IOWritePort harrier_sound_writeport[] =
/*TODO*///	{
/*TODO*///		new IOWritePort( -1 )
/*TODO*///	};
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void harrier_update_proc( void ){
/*TODO*///		int data;
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0ff8] ) & 0x01ff;
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0ffa] ) & 0x01ff;
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0f24] ) & 0x01ff;
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0f26] ) & 0x01ff;
/*TODO*///	
/*TODO*///		data = READ_WORD( &sys16_textram[0x0e9e] );
/*TODO*///	
/*TODO*///		sys16_fg_page[0] = data>>12;
/*TODO*///		sys16_fg_page[1] = (data>>8)&0xf;
/*TODO*///		sys16_fg_page[3] = (data>>4)&0xf;
/*TODO*///		sys16_fg_page[2] = data&0xf;
/*TODO*///	
/*TODO*///		data = READ_WORD( &sys16_textram[0x0e9c] );
/*TODO*///		sys16_bg_page[0] = data>>12;
/*TODO*///		sys16_bg_page[1] = (data>>8)&0xf;
/*TODO*///		sys16_bg_page[3] = (data>>4)&0xf;
/*TODO*///		sys16_bg_page[2] = data&0xf;
/*TODO*///	
/*TODO*///		WRITE_WORD(&sys16_extraram[0x492],sh_io_joy_r(0));
/*TODO*///	
/*TODO*///		data=READ_WORD( &sys16_extraram3[2] );
/*TODO*///		set_refresh_3d( data );
/*TODO*///	
/*TODO*///		if(data & 8)
/*TODO*///		{
/*TODO*///			osd_led_w(0,1);
/*TODO*///			osd_led_w(2,1);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			osd_led_w(0,0);
/*TODO*///			osd_led_w(2,0);
/*TODO*///		}
/*TODO*///		if(data & 4)
/*TODO*///			osd_led_w(1,1);
/*TODO*///		else
/*TODO*///			osd_led_w(1,0);
/*TODO*///	
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr harrier_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = { 00,01,02,03,04,05,06,07,00,00,00,00,00,00,00,00};
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_textmode=1;
/*TODO*///		sys16_spritesystem = 6;
/*TODO*///		sys16_sprxoffset = -0xc0;
/*TODO*///		sys16_fgxoffset = 8;
/*TODO*///		sys16_textlayer_lo_min=0;
/*TODO*///		sys16_textlayer_lo_max=0;
/*TODO*///		sys16_textlayer_hi_min=0;
/*TODO*///		sys16_textlayer_hi_max=0xff;
/*TODO*///	
/*TODO*///	
/*TODO*///	//*disable illegal rom writes
/*TODO*///		patch_code( 0x8112, 0x4a);
/*TODO*///		patch_code( 0x83d2, 0x4a);
/*TODO*///		patch_code( 0x83d6, 0x4a);
/*TODO*///		patch_code( 0x82c4, 0x4a);
/*TODO*///		patch_code( 0x82c8, 0x4a);
/*TODO*///		patch_code( 0x84d0, 0x4a);
/*TODO*///		patch_code( 0x84d4, 0x4a);
/*TODO*///		patch_code( 0x85de, 0x4a);
/*TODO*///		patch_code( 0x85e2, 0x4a);
/*TODO*///	
/*TODO*///		sys16_update_proc = harrier_update_proc;
/*TODO*///	
/*TODO*///		gr_ver = &sys16_extraram2[0x0];
/*TODO*///		gr_hor = gr_ver+0x200;
/*TODO*///		gr_pal = gr_ver+0x400;
/*TODO*///		gr_flip= gr_ver+0x600;
/*TODO*///		gr_palette= 0xf80 / 2;
/*TODO*///		gr_palette_default = 0x70 /2;
/*TODO*///		gr_colorflip[0][0]=0x00 / 2;
/*TODO*///		gr_colorflip[0][1]=0x02 / 2;
/*TODO*///		gr_colorflip[0][2]=0x04 / 2;
/*TODO*///		gr_colorflip[0][3]=0x00 / 2;
/*TODO*///		gr_colorflip[1][0]=0x00 / 2;
/*TODO*///		gr_colorflip[1][1]=0x00 / 2;
/*TODO*///		gr_colorflip[1][2]=0x06 / 2;
/*TODO*///		gr_colorflip[1][3]=0x00 / 2;
/*TODO*///	
/*TODO*///		sys16_sh_shadowpal=0;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_sharrier = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_MaxShadowColors=NumOfShadowColors / 2;	
/*TODO*///		sys16_sprite_decode2( 8,0x020000 ,1);
/*TODO*///		generate_gr_screen(512,512,0,0,4,0x8000);
/*TODO*///	} };
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_sharrier = new InputPortPtr(){ public void handler() { 
/*TODO*///		SYS16_JOY1
/*TODO*///		SYS16_JOY2
/*TODO*///	
/*TODO*///	PORT_START(); 
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN3 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
/*TODO*///		PORT_DIPSETTING(    0x01, "Moving" );
/*TODO*///		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Lives") );
/*TODO*///		PORT_DIPSETTING(    0x08, "2" );
/*TODO*///		PORT_DIPSETTING(    0x0c, "3" );
/*TODO*///		PORT_DIPSETTING(    0x04, "4" );
/*TODO*///		PORT_DIPSETTING(    0x00, "5" );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, "Add Player Score" );
/*TODO*///		PORT_DIPSETTING(    0x10, "5000000" );
/*TODO*///		PORT_DIPSETTING(    0x00, "7000000" );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, "Trial Time" );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x80, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0xc0, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x40, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///	
/*TODO*///	
/*TODO*///	PORT_START(); 	/* X */
/*TODO*///		PORT_ANALOG( 0xff, 0x7f, IPT_AD_STICK_X |  IPF_REVERSE, 100, 4, 0x20, 0xdf );
/*TODO*///	
/*TODO*///	PORT_START(); 	/* Y */
/*TODO*///		PORT_ANALOG( 0xff, 0x7f, IPT_AD_STICK_Y |  IPF_REVERSE, 100, 4, 0x60, 0x9f );
/*TODO*///	
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static MachineDriver machine_driver_sharrier = new MachineDriver
/*TODO*///	(
/*TODO*///		new MachineCPU[] {
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_M68000,
/*TODO*///				10000000,
/*TODO*///				harrier_readmem,harrier_writemem,null,null,
/*TODO*///				sys16_interrupt,1
/*TODO*///			),
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_Z80 | CPU_AUDIO_CPU,
/*TODO*///				4096000,
/*TODO*///				harrier_sound_readmem,harrier_sound_writemem,harrier_sound_readport,harrier_sound_writeport,
/*TODO*///	//			ignore_interrupt,1
/*TODO*///				interrupt,4
/*TODO*///			),
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_M68000,
/*TODO*///				10000000,
/*TODO*///				harrier_readmem2,harrier_writemem2,null,null,
/*TODO*///				sys16_interrupt,1
/*TODO*///			),
/*TODO*///		},
/*TODO*///		60, DEFAULT_60HZ_VBLANK_DURATION,
/*TODO*///		1,
/*TODO*///		harrier_init_machine,
/*TODO*///		40*8, 28*8, new rectangle( 0*8, 40*8-1, 0*8, 28*8-1 ),
/*TODO*///		gfx8,
/*TODO*///		2048*ShadowColorsMultiplier,2048*ShadowColorsMultiplier,
/*TODO*///		null,
/*TODO*///		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
/*TODO*///		null,
/*TODO*///		sys16_ho_vh_start,
/*TODO*///		sys16_vh_stop,
/*TODO*///		sys16_ho_vh_screenrefresh,
/*TODO*///		SOUND_SUPPORTS_STEREO,0,0,0,
/*TODO*///		new MachineSound[] {
/*TODO*///			new MachineSound(
/*TODO*///				SOUND_YM2203,
/*TODO*///				ym2203_interface
/*TODO*///			),
/*TODO*///			new MachineSound(
/*TODO*///				SOUND_SEGAPCM,
/*TODO*///				segapcm_interface_32k,
/*TODO*///			)
/*TODO*///		}
/*TODO*///	);
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	/* hang-on's accel/brake are really both analog controls, but I've added them
/*TODO*///	as digital as well to see what works better */
/*TODO*///	
/*TODO*///	// hangon hardware
/*TODO*///	static RomLoadPtr rom_shangon = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x040000, REGION_CPU1 );/* 68000 code - protected */
/*TODO*///		ROM_LOAD_EVEN( "ic133", 0x000000, 0x10000, 0xe52721fe )
/*TODO*///		ROM_LOAD_ODD ( "ic118", 0x000000, 0x10000, 0x5fee09f6 )
/*TODO*///		ROM_LOAD_EVEN( "ic132", 0x020000, 0x10000, 0x5d55d65f )
/*TODO*///		ROM_LOAD_ODD ( "ic117", 0x020000, 0x10000, 0xb967e8c3 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "ic54",        0x00000, 0x08000, 0x260286f9 );
/*TODO*///		ROM_LOAD( "ic55",        0x08000, 0x08000, 0xc609ee7b );
/*TODO*///		ROM_LOAD( "ic56",        0x10000, 0x08000, 0xb236a403 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x0120000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "ic8",         0x000000, 0x010000, 0xd6ac012b );
/*TODO*///		ROM_RELOAD(              0x100000, 0x010000 );// twice?
/*TODO*///		ROM_LOAD( "ic16",        0x010000, 0x010000, 0xd9d83250 );
/*TODO*///		ROM_RELOAD(              0x110000, 0x010000 );// twice?
/*TODO*///		ROM_LOAD( "ic7",         0x020000, 0x010000, 0x25ebf2c5 );
/*TODO*///		ROM_RELOAD(              0x0e0000, 0x010000 );// twice?
/*TODO*///		ROM_LOAD( "ic15",        0x030000, 0x010000, 0x6365d2e9 );
/*TODO*///		ROM_RELOAD(              0x0f0000, 0x010000 );// twice?
/*TODO*///		ROM_LOAD( "ic6",         0x040000, 0x010000, 0x8a57b8d6 );
/*TODO*///		ROM_LOAD( "ic14",        0x050000, 0x010000, 0x3aff8910 );
/*TODO*///		ROM_LOAD( "ic5",         0x060000, 0x010000, 0xaf473098 );
/*TODO*///		ROM_LOAD( "ic13",        0x070000, 0x010000, 0x80bafeef );
/*TODO*///		ROM_LOAD( "ic4",         0x080000, 0x010000, 0x03bc4878 );
/*TODO*///		ROM_LOAD( "ic12",        0x090000, 0x010000, 0x274b734e );
/*TODO*///		ROM_LOAD( "ic3",         0x0a0000, 0x010000, 0x9f0677ed );
/*TODO*///		ROM_LOAD( "ic11",        0x0b0000, 0x010000, 0x508a4701 );
/*TODO*///		ROM_LOAD( "ic2",         0x0c0000, 0x010000, 0xb176ea72 );
/*TODO*///		ROM_LOAD( "ic10",        0x0d0000, 0x010000, 0x42fcd51d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "ic88", 0x0000, 0x08000, 0x1254efa6 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "ic66", 0x10000, 0x08000, 0x06f55364 );
/*TODO*///		ROM_LOAD( "ic67", 0x18000, 0x08000, 0x731f5cf8 );
/*TODO*///		ROM_LOAD( "ic68", 0x20000, 0x08000, 0xa60dabff );
/*TODO*///		ROM_LOAD( "ic69", 0x28000, 0x08000, 0x473cc411 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_CPU3 );/* second 68000 CPU  - protected */
/*TODO*///		ROM_LOAD_EVEN( "ic76", 0x0000, 0x10000, 0x02be68db )
/*TODO*///		ROM_LOAD_ODD ( "ic58", 0x0000, 0x10000, 0xf13e8bee )
/*TODO*///		ROM_LOAD_EVEN( "ic75", 0x20000, 0x10000, 0x1627c224 )
/*TODO*///		ROM_LOAD_ODD ( "ic57", 0x20000, 0x10000, 0x8cdbcde8 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_GFX3 );/* Road Graphics  (region size should be gr_bitmapwidth*256 )*/
/*TODO*///		ROM_LOAD( "ic47", 0x0000, 0x8000, 0x7836bcc3 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_shangonb = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x030000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "s-hangon.30", 0x000000, 0x10000, 0xd95e82fc )
/*TODO*///		ROM_LOAD_ODD ( "s-hangon.32", 0x000000, 0x10000, 0x2ee4b4fb )
/*TODO*///		ROM_LOAD_EVEN( "s-hangon.29", 0x020000, 0x8000, 0x12ee8716 )
/*TODO*///		ROM_LOAD_ODD ( "s-hangon.31", 0x020000, 0x8000, 0x155e0cfd )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "ic54",        0x00000, 0x08000, 0x260286f9 );
/*TODO*///		ROM_LOAD( "ic55",        0x08000, 0x08000, 0xc609ee7b );
/*TODO*///		ROM_LOAD( "ic56",        0x10000, 0x08000, 0xb236a403 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x0120000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "ic8",         0x000000, 0x010000, 0xd6ac012b );
/*TODO*///		ROM_RELOAD(              0x100000, 0x010000 );// twice?
/*TODO*///		ROM_LOAD( "ic16",        0x010000, 0x010000, 0xd9d83250 );
/*TODO*///		ROM_RELOAD(              0x110000, 0x010000 );// twice?
/*TODO*///		ROM_LOAD( "s-hangon.20", 0x020000, 0x010000, 0xeef23b3d );
/*TODO*///		ROM_RELOAD(              0x0e0000, 0x010000 );// twice?
/*TODO*///		ROM_LOAD( "s-hangon.14", 0x030000, 0x010000, 0x0f26d131 );
/*TODO*///		ROM_RELOAD(              0x0f0000, 0x010000 );// twice?
/*TODO*///		ROM_LOAD( "ic6",         0x040000, 0x010000, 0x8a57b8d6 );
/*TODO*///		ROM_LOAD( "ic14",        0x050000, 0x010000, 0x3aff8910 );
/*TODO*///		ROM_LOAD( "ic5",         0x060000, 0x010000, 0xaf473098 );
/*TODO*///		ROM_LOAD( "ic13",        0x070000, 0x010000, 0x80bafeef );
/*TODO*///		ROM_LOAD( "ic4",         0x080000, 0x010000, 0x03bc4878 );
/*TODO*///		ROM_LOAD( "ic12",        0x090000, 0x010000, 0x274b734e );
/*TODO*///		ROM_LOAD( "ic3",         0x0a0000, 0x010000, 0x9f0677ed );
/*TODO*///		ROM_LOAD( "ic11",        0x0b0000, 0x010000, 0x508a4701 );
/*TODO*///		ROM_LOAD( "ic2",         0x0c0000, 0x010000, 0xb176ea72 );
/*TODO*///		ROM_LOAD( "ic10",        0x0d0000, 0x010000, 0x42fcd51d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "s-hangon.03", 0x0000, 0x08000, 0x83347dc0 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_SOUND1 );/* Sega PCM sound data */
/*TODO*///		ROM_LOAD( "s-hangon.02", 0x00000, 0x10000, 0xda08ca2b );
/*TODO*///		ROM_LOAD( "s-hangon.01", 0x10000, 0x10000, 0x8b10e601 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_CPU3 );/* second 68000 CPU */
/*TODO*///		ROM_LOAD_EVEN("s-hangon.09", 0x0000, 0x10000, 0x070c8059 )
/*TODO*///		ROM_LOAD_ODD( "s-hangon.05", 0x0000, 0x10000, 0x9916c54b )
/*TODO*///		ROM_LOAD_EVEN("s-hangon.08", 0x20000, 0x10000, 0x000ad595 )
/*TODO*///		ROM_LOAD_ODD( "s-hangon.04", 0x20000, 0x10000, 0x8f8f4af0 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_GFX3 );/* Road Graphics  (region size should be gr_bitmapwidth*256 )*/
/*TODO*///		ROM_LOAD( "s-hangon.26", 0x0000, 0x8000, 0x1bbe4fc8 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static unsigned char *shared_ram2;
/*TODO*///	public static ReadHandlerPtr shared_ram2_r = new ReadHandlerPtr() { public int handler(int offset){ return READ_WORD(&shared_ram2[offset]); } };
/*TODO*///	public static WriteHandlerPtr shared_ram2_w = new WriteHandlerPtr() { public void handler(int offset, int data){ COMBINE_WORD_MEM(&shared_ram2[offset], data); } };
/*TODO*///	
/*TODO*///	static MemoryReadAddress shangon_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x20c640, 0x20c647, sound_shared_ram_r ),
/*TODO*///		new MemoryReadAddress( 0x20c000, 0x20ffff, MRA_EXTRAM5 ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0x400000, 0x40ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x410000, 0x410fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x600000, 0x600fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0xa00000, 0xa00fff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0xc68000, 0xc68fff, shared_ram_r ),
/*TODO*///		new MemoryReadAddress( 0xc7c000, 0xc7ffff, shared_ram2_r ),
/*TODO*///		new MemoryReadAddress( 0xe01000, 0xe01001, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0xe0100c, 0xe0100d, io_dip2_r ),
/*TODO*///		new MemoryReadAddress( 0xe0100a, 0xe0100b, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0xe030f8, 0xe030f9, ho_io_x_r ),
/*TODO*///		new MemoryReadAddress( 0xe030fa, 0xe030fb, ho_io_y_r ),
/*TODO*///		new MemoryReadAddress( 0xe00000, 0xe03fff, MRA_EXTRAM4 ),	// io
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress shangon_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x20c640, 0x20c647, sound_shared_ram_w ),
/*TODO*///		new MemoryWriteAddress( 0x20c000, 0x20ffff, MWA_EXTRAM5 ),
/*TODO*///		new MemoryWriteAddress( 0x400000, 0x40ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x410000, 0x410fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x600000, 0x600fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0xa00000, 0xa00fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0xc68000, 0xc68fff, shared_ram_w, shared_ram ),
/*TODO*///		new MemoryWriteAddress( 0xc7c000, 0xc7ffff, shared_ram2_w, shared_ram2 ),
/*TODO*///		new MemoryWriteAddress( 0xe00000, 0xe03fff, MWA_EXTRAM4 ),	// io
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryReadAddress shangon_readmem2[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x454000, 0x45401f, MRA_EXTRAM3 ),
/*TODO*///		new MemoryReadAddress( 0x7e8000, 0x7e8fff, shared_ram_r ),
/*TODO*///		new MemoryReadAddress( 0x7fc000, 0x7ffbff, shared_ram2_r ),
/*TODO*///		new MemoryReadAddress( 0x7ffc00, 0x7fffff, MRA_EXTRAM ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress shangon_writemem2[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x454000, 0x45401f, MWA_EXTRAM3 ),
/*TODO*///		new MemoryWriteAddress( 0x7e8000, 0x7e8fff, shared_ram_w ),
/*TODO*///		new MemoryWriteAddress( 0x7fc000, 0x7ffbff, shared_ram2_w ),
/*TODO*///		new MemoryWriteAddress( 0x7ffc00, 0x7fffff, MWA_EXTRAM ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryReadAddress shangon_sound_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0xf000, 0xf7ff, SEGAPCMReadReg ),
/*TODO*///		new MemoryReadAddress( 0xf800, 0xf807, sound2_shared_ram_r ),
/*TODO*///		new MemoryReadAddress( 0xf808, 0xffff, MRA_RAM ),
/*TODO*///		new MemoryReadAddress( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress shangon_sound_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0xf000, 0xf7ff, SEGAPCMWriteReg ),
/*TODO*///		new MemoryWriteAddress( 0xf800, 0xf807, sound2_shared_ram_w,sound_shared_ram ),
/*TODO*///		new MemoryWriteAddress( 0xf808, 0xffff, MWA_RAM ),
/*TODO*///		new MemoryWriteAddress( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void shangon_update_proc( void ){
/*TODO*///		int leds;
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0ff8] ) & 0x01ff;
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0ffa] ) & 0x01ff;
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0f24] ) & 0x00ff;
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0f26] ) & 0x01ff;
/*TODO*///	
/*TODO*///		set_fg_page1( READ_WORD( &sys16_textram[0x0e9e] ) );
/*TODO*///		set_bg_page1( READ_WORD( &sys16_textram[0x0e9c] ) );
/*TODO*///	
/*TODO*///		set_refresh_3d( READ_WORD( &sys16_extraram4[2] ) );
/*TODO*///	
/*TODO*///		leds=READ_WORD( &sys16_extraram4[0x2] );
/*TODO*///	
/*TODO*///		if(leds & 4)
/*TODO*///		{
/*TODO*///			osd_led_w(0,1);
/*TODO*///			osd_led_w(1,1);
/*TODO*///			osd_led_w(2,1);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			osd_led_w(0,0);
/*TODO*///			osd_led_w(1,0);
/*TODO*///			osd_led_w(2,0);
/*TODO*///		}
/*TODO*///	
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr shangon_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_textmode=1;
/*TODO*///		sys16_spritesystem = 5;
/*TODO*///		sys16_sprxoffset = -0xc0;
/*TODO*///		sys16_fgxoffset = 8;
/*TODO*///		sys16_textlayer_lo_min=0;
/*TODO*///		sys16_textlayer_lo_max=0;
/*TODO*///		sys16_textlayer_hi_min=0;
/*TODO*///		sys16_textlayer_hi_max=0xff;
/*TODO*///	
/*TODO*///		patch_code( 0x65bd, 0xf9);
/*TODO*///		patch_code( 0x6677, 0xfa);
/*TODO*///		patch_code( 0x66d5, 0xfb);
/*TODO*///		patch_code( 0x9621, 0xfb);
/*TODO*///	
/*TODO*///		sys16_update_proc = shangon_update_proc;
/*TODO*///	
/*TODO*///		gr_ver = &shared_ram[0x0];
/*TODO*///		gr_hor = gr_ver+0x200;
/*TODO*///		gr_pal = gr_ver+0x400;
/*TODO*///		gr_flip= gr_ver+0x600;
/*TODO*///		gr_palette= 0xf80 / 2;
/*TODO*///		gr_palette_default = 0x70 /2;
/*TODO*///		gr_colorflip[0][0]=0x08 / 2;
/*TODO*///		gr_colorflip[0][1]=0x04 / 2;
/*TODO*///		gr_colorflip[0][2]=0x00 / 2;
/*TODO*///		gr_colorflip[0][3]=0x06 / 2;
/*TODO*///		gr_colorflip[1][0]=0x0a / 2;
/*TODO*///		gr_colorflip[1][1]=0x04 / 2;
/*TODO*///		gr_colorflip[1][2]=0x02 / 2;
/*TODO*///		gr_colorflip[1][3]=0x02 / 2;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_shangon = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///	
/*TODO*///		sys16_sprite_decode( 9,0x020000 );
/*TODO*///		generate_gr_screen(512,1024,0,0,4,0x8000);
/*TODO*///		//??
/*TODO*///		patch_z80code( 0x1087, 0x20);
/*TODO*///		patch_z80code( 0x1088, 0x01);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_shangonb = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///	
/*TODO*///		sys16_sprite_decode( 9,0x020000 );
/*TODO*///		generate_gr_screen(512,1024,8,0,4,0x8000);
/*TODO*///	} };
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_shangon = new InputPortPtr(){ public void handler() { 
/*TODO*///	PORT_START(); 	/* Steering */
/*TODO*///		PORT_ANALOG( 0xff, 0x7f, IPT_AD_STICK_X | IPF_REVERSE | IPF_CENTER , 100, 3, 0x42, 0xbd );
/*TODO*///	
/*TODO*///	#ifdef HANGON_DIGITAL_CONTROLS
/*TODO*///	
/*TODO*///	PORT_START(); 	/* Buttons */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON2 );
/*TODO*///	
/*TODO*///	#else
/*TODO*///	
/*TODO*///	PORT_START(); 	/* Accel / Decel */
/*TODO*///		PORT_ANALOG( 0xff, 0x1, IPT_AD_STICK_Y | IPF_CENTER | IPF_REVERSE, 100, 16, 1, 0xa2 );
/*TODO*///	
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	PORT_START(); 
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN3 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON3 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x04, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x06, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x02, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0x18, 0x18, "Time Adj." );
/*TODO*///		PORT_DIPSETTING(    0x10, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x18, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x08, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, "Play Music" );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unused") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///	
/*TODO*///	#ifndef HANGON_DIGITAL_CONTROLS
/*TODO*///	
/*TODO*///	PORT_START(); 	/* Brake */
/*TODO*///		PORT_ANALOG( 0xff, 0x1, IPT_AD_STICK_Y | IPF_PLAYER2 | IPF_CENTER | IPF_REVERSE, 100, 16, 1, 0xa2 );
/*TODO*///	
/*TODO*///	#endif
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	static MachineDriver machine_driver_shangon = new MachineDriver
/*TODO*///	(
/*TODO*///		new MachineCPU[] {
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_M68000,
/*TODO*///				10000000,
/*TODO*///				shangon_readmem,shangon_writemem,null,null,
/*TODO*///				sys16_interrupt,1
/*TODO*///			),
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_Z80 | CPU_AUDIO_CPU,
/*TODO*///				4096000,
/*TODO*///				shangon_sound_readmem,shangon_sound_writemem,sound_readport,sound_writeport,
/*TODO*///				ignore_interrupt,1
/*TODO*///			),
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_M68000,
/*TODO*///				10000000,
/*TODO*///				shangon_readmem2,shangon_writemem2,null,null,
/*TODO*///				sys16_interrupt,1
/*TODO*///			),
/*TODO*///		},
/*TODO*///		60, DEFAULT_60HZ_VBLANK_DURATION,
/*TODO*///		1,
/*TODO*///		shangon_init_machine,
/*TODO*///		40*8, 28*8, new rectangle( 0*8, 40*8-1, 0*8, 28*8-1 ),
/*TODO*///		gfx8,
/*TODO*///		2048*ShadowColorsMultiplier,2048*ShadowColorsMultiplier,
/*TODO*///		null,
/*TODO*///		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
/*TODO*///		null,
/*TODO*///		sys16_ho_vh_start,
/*TODO*///		sys16_vh_stop,
/*TODO*///		sys16_ho_vh_screenrefresh,
/*TODO*///		SOUND_SUPPORTS_STEREO,0,0,0,
/*TODO*///		new MachineSound[] {
/*TODO*///			new MachineSound(
/*TODO*///				SOUND_YM2151,
/*TODO*///				ym2151_interface
/*TODO*///			),
/*TODO*///			new MachineSound(
/*TODO*///				SOUND_SEGAPCM,
/*TODO*///				segapcm_interface_15k_512,
/*TODO*///			)
/*TODO*///		}
/*TODO*///	);
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	// Outrun hardware
/*TODO*///	static RomLoadPtr rom_outrun = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x040000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "10380a", 0x000000, 0x10000, 0x434fadbc )
/*TODO*///		ROM_LOAD_ODD ( "10382a", 0x000000, 0x10000, 0x1ddcc04e )
/*TODO*///		ROM_LOAD_EVEN( "10381a", 0x020000, 0x10000, 0xbe8c412b )
/*TODO*///		ROM_LOAD_ODD ( "10383a", 0x020000, 0x10000, 0xdcc586e7 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "10268", 0x00000, 0x08000, 0x95344b04 );
/*TODO*///		ROM_LOAD( "10232", 0x08000, 0x08000, 0x776ba1eb );
/*TODO*///		ROM_LOAD( "10267", 0x10000, 0x08000, 0xa85bb823 );
/*TODO*///		ROM_LOAD( "10231", 0x18000, 0x08000, 0x8908bcbf );
/*TODO*///		ROM_LOAD( "10266", 0x20000, 0x08000, 0x9f6f1a74 );
/*TODO*///		ROM_LOAD( "10230", 0x28000, 0x08000, 0x686f5e50 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "10371", 0x000000, 0x010000, 0x0a1c98de );
/*TODO*///		ROM_CONTINUE(      0x080000, 0x010000 );
/*TODO*///		ROM_LOAD( "10373", 0x010000, 0x010000, 0x339f8e64 );
/*TODO*///		ROM_CONTINUE(      0x090000, 0x010000 );
/*TODO*///		ROM_LOAD( "10375", 0x020000, 0x010000, 0x62a472bd );
/*TODO*///		ROM_CONTINUE(      0x0a0000, 0x010000 );
/*TODO*///		ROM_LOAD( "10377", 0x030000, 0x010000, 0xc86daecb );
/*TODO*///		ROM_CONTINUE(      0x0b0000, 0x010000 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "10372", 0x040000, 0x010000, 0x1640ad1f );
/*TODO*///		ROM_CONTINUE(      0x0c0000, 0x010000 );
/*TODO*///		ROM_LOAD( "10374", 0x050000, 0x010000, 0x22744340 );
/*TODO*///		ROM_CONTINUE(      0x0d0000, 0x010000 );
/*TODO*///		ROM_LOAD( "10376", 0x060000, 0x010000, 0x8337ace7 );
/*TODO*///		ROM_CONTINUE(      0x0e0000, 0x010000 );
/*TODO*///		ROM_LOAD( "10378", 0x070000, 0x010000, 0x544068fd );
/*TODO*///		ROM_CONTINUE(      0x0f0000, 0x010000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "10187",       0x00000, 0x008000, 0xa10abaa9 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x38000, REGION_SOUND1 );/* Sega PCM sound data */
/*TODO*///		ROM_LOAD( "10193",       0x00000, 0x008000, 0xbcd10dde );
/*TODO*///		ROM_RELOAD(              0x30000, 0x008000 );// twice??
/*TODO*///		ROM_LOAD( "10192",       0x08000, 0x008000, 0x770f1270 );
/*TODO*///		ROM_LOAD( "10191",       0x10000, 0x008000, 0x20a284ab );
/*TODO*///		ROM_LOAD( "10190",       0x18000, 0x008000, 0x7cab70e2 );
/*TODO*///		ROM_LOAD( "10189",       0x20000, 0x008000, 0x01366b54 );
/*TODO*///		ROM_LOAD( "10188",       0x28000, 0x008000, 0xbad30ad9 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_CPU3 );/* second 68000 CPU */
/*TODO*///		ROM_LOAD_EVEN("10327a", 0x00000, 0x10000, 0xe28a5baf )
/*TODO*///		ROM_LOAD_ODD( "10329a", 0x00000, 0x10000, 0xda131c81 )
/*TODO*///		ROM_LOAD_EVEN("10328a", 0x20000, 0x10000, 0xd5ec5e5d )
/*TODO*///		ROM_LOAD_ODD( "10330a", 0x20000, 0x10000, 0xba9ec82a )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x80000, REGION_GFX3 );/* Road Graphics  (region size should be gr_bitmapwidth*256 )*/
/*TODO*///		ROM_LOAD( "10185", 0x0000, 0x8000, 0x22794426 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_outruna = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x040000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "10380b", 0x000000, 0x10000, 0x1f6cadad )
/*TODO*///		ROM_LOAD_ODD ( "10382b", 0x000000, 0x10000, 0xc4c3fa1a )
/*TODO*///		ROM_LOAD_EVEN( "10381a", 0x020000, 0x10000, 0xbe8c412b )
/*TODO*///		ROM_LOAD_ODD ( "10383b", 0x020000, 0x10000, 0x10a2014a )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "10268", 0x00000, 0x08000, 0x95344b04 );
/*TODO*///		ROM_LOAD( "10232", 0x08000, 0x08000, 0x776ba1eb );
/*TODO*///		ROM_LOAD( "10267", 0x10000, 0x08000, 0xa85bb823 );
/*TODO*///		ROM_LOAD( "10231", 0x18000, 0x08000, 0x8908bcbf );
/*TODO*///		ROM_LOAD( "10266", 0x20000, 0x08000, 0x9f6f1a74 );
/*TODO*///		ROM_LOAD( "10230", 0x28000, 0x08000, 0x686f5e50 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "10371", 0x000000, 0x010000, 0x0a1c98de );
/*TODO*///		ROM_CONTINUE(      0x080000, 0x010000 );
/*TODO*///		ROM_LOAD( "10373", 0x010000, 0x010000, 0x339f8e64 );
/*TODO*///		ROM_CONTINUE(      0x090000, 0x010000 );
/*TODO*///		ROM_LOAD( "10375", 0x020000, 0x010000, 0x62a472bd );
/*TODO*///		ROM_CONTINUE(      0x0a0000, 0x010000 );
/*TODO*///		ROM_LOAD( "10377", 0x030000, 0x010000, 0xc86daecb );
/*TODO*///		ROM_CONTINUE(      0x0b0000, 0x010000 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "10372", 0x040000, 0x010000, 0x1640ad1f );
/*TODO*///		ROM_CONTINUE(      0x0c0000, 0x010000 );
/*TODO*///		ROM_LOAD( "10374", 0x050000, 0x010000, 0x22744340 );
/*TODO*///		ROM_CONTINUE(      0x0d0000, 0x010000 );
/*TODO*///		ROM_LOAD( "10376", 0x060000, 0x010000, 0x8337ace7 );
/*TODO*///		ROM_CONTINUE(      0x0e0000, 0x010000 );
/*TODO*///		ROM_LOAD( "10378", 0x070000, 0x010000, 0x544068fd );
/*TODO*///		ROM_CONTINUE(      0x0f0000, 0x010000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "10187",       0x00000, 0x008000, 0xa10abaa9 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x38000, REGION_SOUND1 );/* Sega PCM sound data */
/*TODO*///		ROM_LOAD( "10193",       0x00000, 0x008000, 0xbcd10dde );
/*TODO*///		ROM_RELOAD(              0x30000, 0x008000 );// twice??
/*TODO*///		ROM_LOAD( "10192",       0x08000, 0x008000, 0x770f1270 );
/*TODO*///		ROM_LOAD( "10191",       0x10000, 0x008000, 0x20a284ab );
/*TODO*///		ROM_LOAD( "10190",       0x18000, 0x008000, 0x7cab70e2 );
/*TODO*///		ROM_LOAD( "10189",       0x20000, 0x008000, 0x01366b54 );
/*TODO*///		ROM_LOAD( "10188",       0x28000, 0x008000, 0xbad30ad9 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_CPU3 );/* second 68000 CPU */
/*TODO*///		ROM_LOAD_EVEN("10327a", 0x00000, 0x10000, 0xe28a5baf )
/*TODO*///		ROM_LOAD_ODD( "10329a", 0x00000, 0x10000, 0xda131c81 )
/*TODO*///		ROM_LOAD_EVEN("10328a", 0x20000, 0x10000, 0xd5ec5e5d )
/*TODO*///		ROM_LOAD_ODD( "10330a", 0x20000, 0x10000, 0xba9ec82a )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x80000, REGION_GFX3 );/* Road Graphics  (region size should be gr_bitmapwidth*256 )*/
/*TODO*///		ROM_LOAD( "10185", 0x0000, 0x8000, 0x22794426 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_outrunb = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x040000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "orun_mn.rom", 0x000000, 0x10000, 0xcddceea2 )
/*TODO*///		ROM_LOAD_ODD ( "orun_ml.rom", 0x000000, 0x10000, 0x9cfc07d5 )
/*TODO*///		ROM_LOAD_EVEN( "orun_mm.rom", 0x020000, 0x10000, 0x3092d857 )
/*TODO*///		ROM_LOAD_ODD ( "orun_mk.rom", 0x020000, 0x10000, 0x30a1c496 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "10268", 0x00000, 0x08000, 0x95344b04 );
/*TODO*///		ROM_LOAD( "10232", 0x08000, 0x08000, 0x776ba1eb );
/*TODO*///		ROM_LOAD( "10267", 0x10000, 0x08000, 0xa85bb823 );
/*TODO*///		ROM_LOAD( "10231", 0x18000, 0x08000, 0x8908bcbf );
/*TODO*///		ROM_LOAD( "10266", 0x20000, 0x08000, 0x9f6f1a74 );
/*TODO*///		ROM_LOAD( "10230", 0x28000, 0x08000, 0x686f5e50 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "orun_1.rom", 0x000000, 0x010000, 0x77377e00 );
/*TODO*///		ROM_LOAD( "orun_3.rom", 0x010000, 0x010000, 0x69ecc975 );
/*TODO*///		ROM_LOAD( "orun_5.rom", 0x020000, 0x010000, 0xb6a8d0e2 );
/*TODO*///		ROM_LOAD( "orun_7.rom", 0x030000, 0x010000, 0xd632d8a2 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "orun_2.rom", 0x040000, 0x010000, 0x2c0e7277 );
/*TODO*///		ROM_LOAD( "orun_4.rom", 0x050000, 0x010000, 0x54761e57 );
/*TODO*///		ROM_LOAD( "orun_6.rom", 0x060000, 0x010000, 0xa00d0676 );
/*TODO*///		ROM_LOAD( "orun_8.rom", 0x070000, 0x010000, 0xda398368 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "orun_17.rom", 0x080000, 0x010000, 0x4f784236 );
/*TODO*///		ROM_LOAD( "orun_19.rom", 0x090000, 0x010000, 0xee4f7154 );
/*TODO*///		ROM_LOAD( "orun_21.rom", 0x0a0000, 0x010000, 0xe9880aa3 );
/*TODO*///		ROM_LOAD( "orun_23.rom", 0x0b0000, 0x010000, 0xdc286dc2 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "orun_18.rom", 0x0c0000, 0x010000, 0x8d459356 );
/*TODO*///		ROM_LOAD( "orun_20.rom", 0x0d0000, 0x010000, 0xc2825654 );
/*TODO*///		ROM_LOAD( "orun_22.rom", 0x0e0000, 0x010000, 0xef7d06fe );
/*TODO*///		ROM_LOAD( "orun_24.rom", 0x0f0000, 0x010000, 0x1222af9f );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "orun_ma.rom", 0x00000, 0x008000, 0xa3ff797a );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x38000, REGION_SOUND1 );/* Sega PCM sound data */
/*TODO*///		ROM_LOAD( "10193",       0x00000, 0x008000, 0xbcd10dde );
/*TODO*///		ROM_RELOAD(              0x30000, 0x008000 );// twice??
/*TODO*///		ROM_LOAD( "10192",       0x08000, 0x008000, 0x770f1270 );
/*TODO*///		ROM_LOAD( "10191",       0x10000, 0x008000, 0x20a284ab );
/*TODO*///		ROM_LOAD( "10190",       0x18000, 0x008000, 0x7cab70e2 );
/*TODO*///		ROM_LOAD( "10189",       0x20000, 0x008000, 0x01366b54 );
/*TODO*///		ROM_LOAD( "10188",       0x28000, 0x008000, 0xbad30ad9 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_CPU3 );/* second 68000 CPU */
/*TODO*///		ROM_LOAD_EVEN("orun_mj.rom", 0x00000, 0x10000, 0xd7f5aae0 )
/*TODO*///		ROM_LOAD_ODD( "orun_mh.rom", 0x00000, 0x10000, 0x88c2e78f )
/*TODO*///		ROM_LOAD_EVEN("10328a",      0x20000, 0x10000, 0xd5ec5e5d )
/*TODO*///		ROM_LOAD_ODD( "orun_mg.rom", 0x20000, 0x10000, 0x74c5fbec )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x80000, REGION_GFX3 );/* Road Graphics  (region size should be gr_bitmapwidth*256 )*/
/*TODO*///		ROM_LOAD( "orun_me.rom", 0x0000, 0x8000, 0x666fe754 );
/*TODO*///	
/*TODO*///	//	ROM_LOAD( "orun_mf.rom", 0x0000, 0x8000, 0xed5bda9c );//??
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr or_io_joy_r = new ReadHandlerPtr() { public int handler(int offset){ return (input_port_5_r( offset ) << 8) + input_port_6_r( offset ); } };
/*TODO*///	
/*TODO*///	#ifdef HANGON_DIGITAL_CONTROLS
/*TODO*///	public static ReadHandlerPtr or_io_brake_r = new ReadHandlerPtr() { public int handler(int offset){
/*TODO*///		int data = input_port_1_r( offset );
/*TODO*///	
/*TODO*///		switch(data & 3)
/*TODO*///		{
/*TODO*///			case 3:	return 0xff00;	// both
/*TODO*///			case 1:	return 0xff00;  // brake
/*TODO*///			case 2:	return 0x0000;  // accel
/*TODO*///			case 0:	return 0x0000;  // neither
/*TODO*///		}
/*TODO*///		return 0x0000;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr or_io_acc_steer_r = new ReadHandlerPtr() { public int handler(int offset){
/*TODO*///		int data = input_port_1_r( offset );
/*TODO*///		int ret = input_port_0_r( offset ) << 8;
/*TODO*///	
/*TODO*///		switch(data & 3)
/*TODO*///		{
/*TODO*///			case 3:	return 0x00 | ret;	// both
/*TODO*///			case 1:	return 0x00 | ret;  // brake
/*TODO*///			case 2:	return 0xff | ret;  // accel
/*TODO*///			case 0:	return 0x00 | ret ;  // neither
/*TODO*///		}
/*TODO*///		return 0x00 | ret;
/*TODO*///	} };
/*TODO*///	#else
/*TODO*///	public static ReadHandlerPtr or_io_acc_steer_r = new ReadHandlerPtr() { public int handler(int offset){ return (input_port_0_r( offset ) << 8) + input_port_1_r( offset ); } };
/*TODO*///	public static ReadHandlerPtr or_io_brake_r = new ReadHandlerPtr() { public int handler(int offset){ return input_port_5_r( offset ) << 8; } };
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	static int or_gear=0;
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr or_io_service_r = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int ret=input_port_2_r( offset );
/*TODO*///		int data=input_port_1_r( offset );
/*TODO*///		if(data & 4) or_gear=0;
/*TODO*///		else if(data & 8) or_gear=1;
/*TODO*///	
/*TODO*///		if(or_gear) ret|=0x10;
/*TODO*///		else ret&=0xef;
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr or_reset2_r = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		cpu_set_reset_line(2,PULSE_LINE);
/*TODO*///		return 0;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	static MemoryReadAddress outrun_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x060892, 0x060893, or_io_acc_steer_r ),
/*TODO*///		new MemoryReadAddress( 0x060894, 0x060895, or_io_brake_r ),
/*TODO*///		new MemoryReadAddress( 0x060900, 0x060907, sound_shared_ram_r ),		//???
/*TODO*///		new MemoryReadAddress( 0x060000, 0x067fff, MRA_EXTRAM5 ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0x100000, 0x10ffff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x110000, 0x110fff, sys16_textram_r ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0x130000, 0x130fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x120000, 0x121fff, paletteram_word_r ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0x140010, 0x140011, or_io_service_r ),
/*TODO*///		new MemoryReadAddress( 0x140014, 0x140015, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0x140016, 0x140017, io_dip2_r ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0x140000, 0x140071, MRA_EXTRAM3 ),		//io
/*TODO*///		new MemoryReadAddress( 0x200000, 0x23ffff, MRA_BANK8 ),
/*TODO*///		new MemoryReadAddress( 0x260000, 0x267fff, shared_ram_r ),
/*TODO*///		new MemoryReadAddress( 0xe00000, 0xe00001, or_reset2_r ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr outrun_sound_write_w = new WriteHandlerPtr() { public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		sound_shared_ram[0]=data&0xff;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static MemoryWriteAddress outrun_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x060900, 0x060907, sound_shared_ram_w ),		//???
/*TODO*///		new MemoryWriteAddress( 0x060000, 0x067fff, MWA_EXTRAM5 ),
/*TODO*///	
/*TODO*///		new MemoryWriteAddress( 0x100000, 0x10ffff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x110000, 0x110fff, sys16_textram_w,sys16_textram ),
/*TODO*///	
/*TODO*///		new MemoryWriteAddress( 0x130000, 0x130fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x120000, 0x121fff, sys16_paletteram_w, paletteram ),
/*TODO*///	
/*TODO*///		new MemoryWriteAddress( 0x140000, 0x140071, MWA_EXTRAM3 ),		//io
/*TODO*///		new MemoryWriteAddress( 0x200000, 0x23ffff, MWA_BANK8 ),
/*TODO*///		new MemoryWriteAddress( 0x260000, 0x267fff, shared_ram_w, shared_ram ),
/*TODO*///		new MemoryWriteAddress( 0xffff06, 0xffff07, outrun_sound_write_w ),
/*TODO*///	
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryReadAddress outrun_readmem2[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x060000, 0x067fff, shared_ram_r ),
/*TODO*///		new MemoryReadAddress( 0x080000, 0x09ffff, MRA_EXTRAM ),		// gr
/*TODO*///	
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress outrun_writemem2[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x060000, 0x067fff, shared_ram_w ),
/*TODO*///		new MemoryWriteAddress( 0x080000, 0x09ffff, MWA_EXTRAM ),		// gr
/*TODO*///	
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	// Outrun
/*TODO*///	
/*TODO*///	static MemoryReadAddress outrun_sound_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0xf000, 0xf0ff, SEGAPCMReadReg ),
/*TODO*///		new MemoryReadAddress( 0xf100, 0xf7ff, MRA_NOP ),
/*TODO*///		new MemoryReadAddress( 0xf800, 0xf807, sound2_shared_ram_r ),
/*TODO*///		new MemoryReadAddress( 0xf808, 0xffff, MRA_RAM ),
/*TODO*///		new MemoryReadAddress( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress outrun_sound_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0xf000, 0xf0ff, SEGAPCMWriteReg ),
/*TODO*///		new MemoryWriteAddress( 0xf100, 0xf7ff, MWA_NOP ),
/*TODO*///		new MemoryWriteAddress( 0xf800, 0xf807, sound2_shared_ram_w,sound_shared_ram ),
/*TODO*///		new MemoryWriteAddress( 0xf808, 0xffff, MWA_RAM ),
/*TODO*///		new MemoryWriteAddress( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static void outrun_update_proc( void ){
/*TODO*///		int data;
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0e98] );
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0e9a] );
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0e90] );
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0e92] );
/*TODO*///		set_fg_page( READ_WORD( &sys16_textram[0x0e80] ) );
/*TODO*///		set_bg_page( READ_WORD( &sys16_textram[0x0e82] ) );
/*TODO*///	
/*TODO*///		set_refresh( READ_WORD( &sys16_extraram5[0xb6e] ) );
/*TODO*///		data=READ_WORD( &sys16_extraram5[0xb6c] );
/*TODO*///	
/*TODO*///		if(data & 0x2)
/*TODO*///		{
/*TODO*///			osd_led_w(0,1);
/*TODO*///			osd_led_w(2,1);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			osd_led_w(0,0);
/*TODO*///			osd_led_w(2,0);
/*TODO*///		}
/*TODO*///	
/*TODO*///		if(data & 0x4)
/*TODO*///			osd_led_w(1,1);
/*TODO*///		else
/*TODO*///			osd_led_w(1,0);
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitMachinePtr outrun_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = { 07,00,01,04,05,02,03,06,00,00,00,00,00,00,00,00};
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_spritesystem = 7;
/*TODO*///		sys16_textlayer_lo_min=0;
/*TODO*///		sys16_textlayer_lo_max=0;
/*TODO*///		sys16_textlayer_hi_min=0;
/*TODO*///		sys16_textlayer_hi_max=0xff;
/*TODO*///		sys16_sprxoffset = -0xc0;
/*TODO*///	
/*TODO*///	// cpu 0 reset opcode resets cpu 2?
/*TODO*///		patch_code(0x7d44,0x4a);
/*TODO*///		patch_code(0x7d45,0x79);
/*TODO*///		patch_code(0x7d46,0x00);
/*TODO*///		patch_code(0x7d47,0xe0);
/*TODO*///		patch_code(0x7d48,0x00);
/*TODO*///		patch_code(0x7d49,0x00);
/*TODO*///	
/*TODO*///	// *forced sound cmd
/*TODO*///		patch_code( 0x55ed, 0x00);
/*TODO*///	
/*TODO*///	// rogue tile on music selection screen
/*TODO*///	//	patch_code( 0x38545, 0x80);
/*TODO*///	
/*TODO*///	// *freeze time
/*TODO*///	//	patch_code( 0xb6b6, 0x4e);
/*TODO*///	//	patch_code( 0xb6b7, 0x71);
/*TODO*///	
/*TODO*///		cpu_setbank(8, memory_region(REGION_CPU3));
/*TODO*///	
/*TODO*///		sys16_update_proc = outrun_update_proc;
/*TODO*///	
/*TODO*///		gr_ver = &sys16_extraram[0];
/*TODO*///		gr_hor = gr_ver+0x400;
/*TODO*///		gr_flip= gr_ver+0xc00;
/*TODO*///		gr_palette= 0xf00 / 2;
/*TODO*///		gr_palette_default = 0x800 /2;
/*TODO*///		gr_colorflip[0][0]=0x08 / 2;
/*TODO*///		gr_colorflip[0][1]=0x04 / 2;
/*TODO*///		gr_colorflip[0][2]=0x00 / 2;
/*TODO*///		gr_colorflip[0][3]=0x00 / 2;
/*TODO*///		gr_colorflip[1][0]=0x0a / 2;
/*TODO*///		gr_colorflip[1][1]=0x06 / 2;
/*TODO*///		gr_colorflip[1][2]=0x02 / 2;
/*TODO*///		gr_colorflip[1][3]=0x00 / 2;
/*TODO*///	
/*TODO*///		gr_second_road = &sys16_extraram[0x10000];
/*TODO*///	
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitMachinePtr outruna_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = { 07,00,01,04,05,02,03,06,00,00,00,00,00,00,00,00};
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_spritesystem = 7;
/*TODO*///		sys16_textlayer_lo_min=0;
/*TODO*///		sys16_textlayer_lo_max=0;
/*TODO*///		sys16_textlayer_hi_min=0;
/*TODO*///		sys16_textlayer_hi_max=0xff;
/*TODO*///	
/*TODO*///	// cpu 0 reset opcode resets cpu 2?
/*TODO*///		patch_code(0x7db8,0x4a);
/*TODO*///		patch_code(0x7db9,0x79);
/*TODO*///		patch_code(0x7dba,0x00);
/*TODO*///		patch_code(0x7dbb,0xe0);
/*TODO*///		patch_code(0x7dbc,0x00);
/*TODO*///		patch_code(0x7dbd,0x00);
/*TODO*///	
/*TODO*///	// *forced sound cmd
/*TODO*///		patch_code( 0x5661, 0x00);
/*TODO*///	
/*TODO*///	// rogue tile on music selection screen
/*TODO*///	//	patch_code( 0x38455, 0x80);
/*TODO*///	
/*TODO*///	// *freeze time
/*TODO*///	//	patch_code( 0xb6b6, 0x4e);
/*TODO*///	//	patch_code( 0xb6b7, 0x71);
/*TODO*///	
/*TODO*///		cpu_setbank(8, memory_region(REGION_CPU3));
/*TODO*///	
/*TODO*///		sys16_update_proc = outrun_update_proc;
/*TODO*///	
/*TODO*///		gr_ver = &sys16_extraram[0];
/*TODO*///		gr_hor = gr_ver+0x400;
/*TODO*///		gr_flip= gr_ver+0xc00;
/*TODO*///		gr_palette= 0xf00 / 2;
/*TODO*///		gr_palette_default = 0x800 /2;
/*TODO*///		gr_colorflip[0][0]=0x08 / 2;
/*TODO*///		gr_colorflip[0][1]=0x04 / 2;
/*TODO*///		gr_colorflip[0][2]=0x00 / 2;
/*TODO*///		gr_colorflip[0][3]=0x00 / 2;
/*TODO*///		gr_colorflip[1][0]=0x0a / 2;
/*TODO*///		gr_colorflip[1][1]=0x06 / 2;
/*TODO*///		gr_colorflip[1][2]=0x02 / 2;
/*TODO*///		gr_colorflip[1][3]=0x00 / 2;
/*TODO*///	
/*TODO*///		gr_second_road = &sys16_extraram[0x10000];
/*TODO*///	
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_outrun = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_sprite_decode2( 4,0x040000, 0 );
/*TODO*///		generate_gr_screen(512,2048,0,0,3,0x8000);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_outrunb = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		UBytePtr RAM = memory_region(REGION_CPU1);
/*TODO*///		int i;
/*TODO*///		int odd,even,word;
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///	/*
/*TODO*///	  Main Processor
/*TODO*///		Comparing the bootleg with the custom bootleg, it seems that:-
/*TODO*///	
/*TODO*///	  if even bytes &0x28 == 0x20 or 0x08 then they are xored with 0x28
/*TODO*///	  if odd bytes &0xc0 == 0x40 or 0x80 then they are xored with 0xc0
/*TODO*///	
/*TODO*///	  ie. data lines are switched.
/*TODO*///	*/
/*TODO*///	
/*TODO*///		for(i=0;i<0x40000;i+=2)
/*TODO*///		{
/*TODO*///			word=READ_WORD(&RAM[i]);
/*TODO*///			even=word>>8;
/*TODO*///			odd=word&0xff;
/*TODO*///			// even byte
/*TODO*///			if((even&0x28) == 0x20 || (even&0x28) == 0x08)
/*TODO*///				even^=0x28;
/*TODO*///			// odd byte
/*TODO*///			if((odd&0xc0) == 0x80 || (odd&0xc0) == 0x40)
/*TODO*///				odd^=0xc0;
/*TODO*///			word=(even<<8)+odd;
/*TODO*///			WRITE_WORD(&RAM[i],word);
/*TODO*///		}
/*TODO*///	
/*TODO*///	/*
/*TODO*///	  Second Processor
/*TODO*///	
/*TODO*///	  if even bytes &0xc0 == 0x40 or 0x80 then they are xored with 0xc0
/*TODO*///	  if odd bytes &0x0c == 0x04 or 0x08 then they are xored with 0x0c
/*TODO*///	*/
/*TODO*///		RAM = memory_region(REGION_CPU3);
/*TODO*///		for(i=0;i<0x40000;i+=2)
/*TODO*///		{
/*TODO*///			word=READ_WORD(&RAM[i]);
/*TODO*///			even=word>>8;
/*TODO*///			odd=word&0xff;
/*TODO*///			// even byte
/*TODO*///			if((even&0xc0) == 0x80 || (even&0xc0) == 0x40)
/*TODO*///				even^=0xc0;
/*TODO*///			// odd byte
/*TODO*///			if((odd&0x0c) == 0x08 || (odd&0x0c) == 0x04)
/*TODO*///				odd^=0x0c;
/*TODO*///			word=(even<<8)+odd;
/*TODO*///			WRITE_WORD(&RAM[i],word);
/*TODO*///		}
/*TODO*///	/*
/*TODO*///	  Road GFX
/*TODO*///	
/*TODO*///		rom orun_me.rom
/*TODO*///		if bytes &0x60 == 0x40 or 0x20 then they are xored with 0x60
/*TODO*///	
/*TODO*///		rom orun_mf.rom
/*TODO*///		if bytes &0xc0 == 0x40 or 0x80 then they are xored with 0xc0
/*TODO*///	
/*TODO*///	  I don't know why there's 2 road roms, but I'm using orun_me.rom
/*TODO*///	*/
/*TODO*///		RAM = memory_region(REGION_GFX3);
/*TODO*///		for(i=0;i<0x8000;i++)
/*TODO*///		{
/*TODO*///			if((RAM[i]&0x60) == 0x20 || (RAM[i]&0x60) == 0x40)
/*TODO*///				RAM[i]^=0x60;
/*TODO*///		}
/*TODO*///	
/*TODO*///		generate_gr_screen(512,2048,0,0,3,0x8000);
/*TODO*///	
/*TODO*///	/*
/*TODO*///	  Z80 Code
/*TODO*///		rom orun_ma.rom
/*TODO*///		if bytes &0x60 == 0x40 or 0x20 then they are xored with 0x60
/*TODO*///	
/*TODO*///	*/
/*TODO*///		RAM = memory_region(REGION_CPU2);
/*TODO*///		for(i=0;i<0x8000;i++)
/*TODO*///		{
/*TODO*///			if((RAM[i]&0x60) == 0x20 || (RAM[i]&0x60) == 0x40)
/*TODO*///				RAM[i]^=0x60;
/*TODO*///		}
/*TODO*///	
/*TODO*///		sys16_sprite_decode2( 4,0x040000,0  );
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_outrun = new InputPortPtr(){ public void handler() { 
/*TODO*///	PORT_START(); 	/* Steering */
/*TODO*///		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X | IPF_CENTER, 100, 3, 0x48, 0xb8 );
/*TODO*///	//	PORT_ANALOG( 0xff, 0x7f, IPT_PADDLE , 70, 3, 0x48, 0xb8 );
/*TODO*///	
/*TODO*///	#ifdef HANGON_DIGITAL_CONTROLS
/*TODO*///	
/*TODO*///	PORT_START(); 	/* Buttons */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_BUTTON3 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_BUTTON4 );
/*TODO*///	
/*TODO*///	#else
/*TODO*///	
/*TODO*///	PORT_START(); 	/* Accel / Decel */
/*TODO*///		PORT_ANALOG( 0xff, 0x30, IPT_AD_STICK_Y | IPF_CENTER | IPF_REVERSE, 100, 16, 0x30, 0x90 );
/*TODO*///	
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	PORT_START(); 
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///	//	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON3 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///	
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x03, 0x02, DEF_STR( "Cabinet") );
/*TODO*///		PORT_DIPSETTING(    0x02, "Up Cockpit" );
/*TODO*///		PORT_DIPSETTING(    0x01, "Mini Up" );
/*TODO*///		PORT_DIPSETTING(    0x03, "Moving" );
/*TODO*///	//	PORT_DIPSETTING(    0x00, "No Use" );
/*TODO*///		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unused") );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x30, 0x30, "Time" );
/*TODO*///		PORT_DIPSETTING(    0x20, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x30, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x10, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0xc0, 0xc0, "Enemies" );
/*TODO*///		PORT_DIPSETTING(    0x80, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0xc0, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x40, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///	
/*TODO*///	
/*TODO*///	#ifndef HANGON_DIGITAL_CONTROLS
/*TODO*///	
/*TODO*///	PORT_START(); 	/* Brake */
/*TODO*///		PORT_ANALOG( 0xff, 0x30, IPT_AD_STICK_Y | IPF_PLAYER2 | IPF_CENTER | IPF_REVERSE, 100, 16, 0x30, 0x90 );
/*TODO*///	
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	public static InterruptPtr or_interrupt = new InterruptPtr() { public int handler() {
/*TODO*///		int intleft=cpu_getiloops();
/*TODO*///		if(intleft!=0) return 2;
/*TODO*///		else return 4;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	#define MACHINE_DRIVER_OUTRUN( GAMENAME,INITMACHINE) \
/*TODO*///	static MachineDriver GAMENAME = new MachineDriver\
/*TODO*///	( \
/*TODO*///		new MachineCPU[] { \
/*TODO*///			new MachineCPU( \
/*TODO*///				CPU_M68000, \
/*TODO*///				12000000, \
/*TODO*///				outrun_readmem,outrun_writemem,null,null, \
/*TODO*///				or_interrupt,2 \
/*TODO*///			), \
/*TODO*///			new MachineCPU( \
/*TODO*///				CPU_Z80 | CPU_AUDIO_CPU, \
/*TODO*///				4096000, \
/*TODO*///				outrun_sound_readmem,outrun_sound_writemem,sound_readport,sound_writeport, \
/*TODO*///				ignore_interrupt,1 \
/*TODO*///			), \
/*TODO*///			new MachineCPU( \
/*TODO*///				CPU_M68000, \
/*TODO*///				12000000, \
/*TODO*///				outrun_readmem2,outrun_writemem2,null,null, \
/*TODO*///				sys16_interrupt,2 \
/*TODO*///			), \
/*TODO*///		}, \
/*TODO*///		60, 100 /*DEFAULT_60HZ_VBLANK_DURATION*/, \
/*TODO*///		4, /* needed to sync processors */ \
/*TODO*///		INITMACHINE, \
/*TODO*///		40*8, 28*8, new rectangle( 0*8, 40*8-1, 0*8, 28*8-1 ), \
/*TODO*///		gfx1, \
/*TODO*///		4096*ShadowColorsMultiplier,4096*ShadowColorsMultiplier, \
/*TODO*///		null, \
/*TODO*///		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_AFTER_VBLANK, \
/*TODO*///		null, \
/*TODO*///		sys16_or_vh_start, \
/*TODO*///		sys16_vh_stop, \
/*TODO*///		sys16_or_vh_screenrefresh, \
/*TODO*///		SOUND_SUPPORTS_STEREO,0,0,0, \
/*TODO*///		new MachineSound[] { \
/*TODO*///			new MachineSound( \
/*TODO*///				SOUND_YM2151, \
/*TODO*///				ym2151_interface \
/*TODO*///			), \
/*TODO*///			new MachineSound( \
/*TODO*///				SOUND_SEGAPCM, \
/*TODO*///				segapcm_interface_15k, \
/*TODO*///			) \
/*TODO*///		} \
/*TODO*///	);
/*TODO*///	
/*TODO*///	MACHINE_DRIVER_OUTRUN(machine_driver_outrun,outrun_init_machine)
/*TODO*///	MACHINE_DRIVER_OUTRUN(machine_driver_outruna,outruna_init_machine)
/*TODO*///	
/*TODO*///	/*****************************************************************************/
/*TODO*///	// Enduro Racer
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_enduror = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x040000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "7640a.rom",0x00000, 0x8000, 0x1d1dc5d4 )
/*TODO*///		ROM_LOAD_ODD ( "7636a.rom",0x00000, 0x8000, 0x84131639 )
/*TODO*///	
/*TODO*///		ROM_LOAD_EVEN( "7641.rom", 0x10000, 0x8000, 0x2503ae7c )
/*TODO*///		ROM_LOAD_ODD ( "7637.rom", 0x10000, 0x8000, 0x82a27a8c )
/*TODO*///		ROM_LOAD_EVEN( "7642.rom", 0x20000, 0x8000, 0x1c453bea )	// enduro.a06 / .a09
/*TODO*///		ROM_LOAD_ODD ( "7638.rom", 0x20000, 0x8000, 0x70544779 )	// looks like encrypted versions of
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "7644.rom", 0x00000, 0x08000, 0xe7a4ff90 );
/*TODO*///		ROM_LOAD( "7645.rom", 0x08000, 0x08000, 0x4caa0095 );
/*TODO*///		ROM_LOAD( "7646.rom", 0x10000, 0x08000, 0x7e432683 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000*2, REGION_GFX2 );/* sprites */
/*TODO*///	
/*TODO*///		ROM_LOAD( "7678.rom", 0x000000, 0x008000, 0x9fb5e656 );
/*TODO*///		ROM_LOAD( "7670.rom", 0x008000, 0x008000, 0xdbbe2f6e );
/*TODO*///		ROM_LOAD( "7662.rom", 0x010000, 0x008000, 0xcb0c13c5 );
/*TODO*///		ROM_LOAD( "7654.rom", 0x018000, 0x008000, 0x2db6520d );
/*TODO*///	
/*TODO*///		ROM_LOAD( "7677.rom", 0x020000, 0x008000, 0x7764765b );
/*TODO*///		ROM_LOAD( "7669.rom", 0x028000, 0x008000, 0xf9525faa );
/*TODO*///		ROM_LOAD( "7661.rom", 0x030000, 0x008000, 0xfe93a79b );
/*TODO*///		ROM_LOAD( "7653.rom", 0x038000, 0x008000, 0x46a52114 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "7676.rom", 0x040000, 0x008000, 0x2e42e0d4 );
/*TODO*///		ROM_LOAD( "7668.rom", 0x048000, 0x008000, 0xe115ce33 );
/*TODO*///		ROM_LOAD( "7660.rom", 0x050000, 0x008000, 0x86dfbb68 );
/*TODO*///		ROM_LOAD( "7652.rom", 0x058000, 0x008000, 0x2880cfdb );
/*TODO*///	
/*TODO*///		ROM_LOAD( "7675.rom", 0x060000, 0x008000, 0x05cd2d61 );
/*TODO*///		ROM_LOAD( "7667.rom", 0x068000, 0x008000, 0x923bde9d );
/*TODO*///		ROM_LOAD( "7659.rom", 0x070000, 0x008000, 0x629dc8ce );
/*TODO*///		ROM_LOAD( "7651.rom", 0x078000, 0x008000, 0xd7902bad );
/*TODO*///	
/*TODO*///		ROM_LOAD( "7674.rom", 0x080000, 0x008000, 0x1a129acf );
/*TODO*///		ROM_LOAD( "7666.rom", 0x088000, 0x008000, 0x23697257 );
/*TODO*///		ROM_LOAD( "7658.rom", 0x090000, 0x008000, 0x1677f24f );
/*TODO*///		ROM_LOAD( "7650.rom", 0x098000, 0x008000, 0x642635ec );
/*TODO*///	
/*TODO*///		ROM_LOAD( "7673.rom", 0x0a0000, 0x008000, 0x82602394 );
/*TODO*///		ROM_LOAD( "7665.rom", 0x0a8000, 0x008000, 0x12d77607 );
/*TODO*///		ROM_LOAD( "7657.rom", 0x0b0000, 0x008000, 0x8158839c );
/*TODO*///		ROM_LOAD( "7649.rom", 0x0b8000, 0x008000, 0x4edba14c );
/*TODO*///	
/*TODO*///		ROM_LOAD( "7672.rom", 0x0c0000, 0x008000, 0xd11452f7 );
/*TODO*///		ROM_LOAD( "7664.rom", 0x0c8000, 0x008000, 0x0df2cfad );
/*TODO*///		ROM_LOAD( "7656.rom", 0x0d0000, 0x008000, 0x6c741272 );
/*TODO*///		ROM_LOAD( "7648.rom", 0x0d8000, 0x008000, 0x983ea830 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "7671.rom", 0x0e0000, 0x008000, 0xb0c7fdc6 );
/*TODO*///		ROM_LOAD( "7663.rom", 0x0e8000, 0x008000, 0x2b0b8f08 );
/*TODO*///		ROM_LOAD( "7655.rom", 0x0f0000, 0x008000, 0x3433fe7b );
/*TODO*///		ROM_LOAD( "7647.rom", 0x0f8000, 0x008000, 0x2e7fbec0 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "7682.rom", 0x00000, 0x008000, 0xc4efbf48 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_SOUND1 );/* Sega PCM sound data */
/*TODO*///		ROM_LOAD( "7681.rom", 0x00000, 0x008000, 0xbc0c4d12 );
/*TODO*///		ROM_LOAD( "7680.rom", 0x08000, 0x008000, 0x627b3c8c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU3 );/* second 68000 CPU */
/*TODO*///		ROM_LOAD_EVEN("7634.rom", 0x0000, 0x8000, 0x3e07fd32 )
/*TODO*///		ROM_LOAD_ODD ("7635.rom", 0x0000, 0x8000, 0x22f762ab )
/*TODO*///		// alternate version??
/*TODO*///	//	ROM_LOAD_EVEN("7634a.rom", 0x0000, 0x8000, 0xaec83731 )
/*TODO*///	//	ROM_LOAD_ODD ("7635a.rom", 0x0000, 0x8000, 0xb2fce96f )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_GFX3 );/* Road Graphics  (region size should be gr_bitmapwidth*256 )*/
/*TODO*///		ROM_LOAD( "7633.rom", 0x0000, 0x8000, 0x6f146210 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_endurobl = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x040000+0x010000+0x040000, REGION_CPU1 );/* 68000 code + space for RAM + space for decrypted opcodes */
/*TODO*///		ROM_LOAD_EVEN( "7.13j", 0x030000, 0x08000, 0xf1d6b4b7 )
/*TODO*///		ROM_CONTINUE (          0x000000, 0x08000 | ROMFLAG_ALTERNATE );
/*TODO*///		ROM_LOAD_ODD ( "4.13h", 0x030000, 0x08000, 0x43bff873 )						// rom de-coded
/*TODO*///		ROM_CONTINUE (          0x000001, 0x08000 | ROMFLAG_ALTERNATE );	// data de-coded
/*TODO*///	
/*TODO*///		ROM_LOAD_EVEN( "8.14j", 0x010000, 0x08000, 0x2153154a )
/*TODO*///		ROM_LOAD_ODD ( "5.14h", 0x010000, 0x08000, 0x0a97992c )
/*TODO*///		ROM_LOAD_EVEN( "9.15j", 0x020000, 0x08000, 0xdb3bff1c )	// one byte difference from
/*TODO*///		ROM_LOAD_ODD ( "6.15h", 0x020000, 0x08000, 0x54b1885a )	// enduro.a06 / enduro.a09
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "7644.rom", 0x00000, 0x08000, 0xe7a4ff90 );
/*TODO*///		ROM_LOAD( "7645.rom", 0x08000, 0x08000, 0x4caa0095 );
/*TODO*///		ROM_LOAD( "7646.rom", 0x10000, 0x08000, 0x7e432683 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "7678.rom", 0x000000, 0x008000, 0x9fb5e656 );
/*TODO*///		ROM_LOAD( "7670.rom", 0x008000, 0x008000, 0xdbbe2f6e );
/*TODO*///		ROM_LOAD( "7662.rom", 0x010000, 0x008000, 0xcb0c13c5 );
/*TODO*///		ROM_LOAD( "7654.rom", 0x018000, 0x008000, 0x2db6520d );
/*TODO*///	
/*TODO*///		ROM_LOAD( "7677.rom", 0x020000, 0x008000, 0x7764765b );
/*TODO*///		ROM_LOAD( "7669.rom", 0x028000, 0x008000, 0xf9525faa );
/*TODO*///		ROM_LOAD( "7661.rom", 0x030000, 0x008000, 0xfe93a79b );
/*TODO*///		ROM_LOAD( "7653.rom", 0x038000, 0x008000, 0x46a52114 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "7676.rom", 0x040000, 0x008000, 0x2e42e0d4 );
/*TODO*///		ROM_LOAD( "7668.rom", 0x048000, 0x008000, 0xe115ce33 );
/*TODO*///		ROM_LOAD( "7660.rom", 0x050000, 0x008000, 0x86dfbb68 );
/*TODO*///		ROM_LOAD( "7652.rom", 0x058000, 0x008000, 0x2880cfdb );
/*TODO*///	
/*TODO*///		ROM_LOAD( "7675.rom", 0x060000, 0x008000, 0x05cd2d61 );
/*TODO*///		ROM_LOAD( "7667.rom", 0x068000, 0x008000, 0x923bde9d );
/*TODO*///		ROM_LOAD( "7659.rom", 0x070000, 0x008000, 0x629dc8ce );
/*TODO*///		ROM_LOAD( "7651.rom", 0x078000, 0x008000, 0xd7902bad );
/*TODO*///	
/*TODO*///		ROM_LOAD( "7674.rom", 0x080000, 0x008000, 0x1a129acf );
/*TODO*///		ROM_LOAD( "7666.rom", 0x088000, 0x008000, 0x23697257 );
/*TODO*///		ROM_LOAD( "7658.rom", 0x090000, 0x008000, 0x1677f24f );
/*TODO*///		ROM_LOAD( "7650.rom", 0x098000, 0x008000, 0x642635ec );
/*TODO*///	
/*TODO*///		ROM_LOAD( "7673.rom", 0x0a0000, 0x008000, 0x82602394 );
/*TODO*///		ROM_LOAD( "7665.rom", 0x0a8000, 0x008000, 0x12d77607 );
/*TODO*///		ROM_LOAD( "7657.rom", 0x0b0000, 0x008000, 0x8158839c );
/*TODO*///		ROM_LOAD( "7649.rom", 0x0b8000, 0x008000, 0x4edba14c );
/*TODO*///	
/*TODO*///		ROM_LOAD( "7672.rom", 0x0c0000, 0x008000, 0xd11452f7 );
/*TODO*///		ROM_LOAD( "7664.rom", 0x0c8000, 0x008000, 0x0df2cfad );
/*TODO*///		ROM_LOAD( "7656.rom", 0x0d0000, 0x008000, 0x6c741272 );
/*TODO*///		ROM_LOAD( "7648.rom", 0x0d8000, 0x008000, 0x983ea830 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "7671.rom", 0x0e0000, 0x008000, 0xb0c7fdc6 );
/*TODO*///		ROM_LOAD( "7663.rom", 0x0e8000, 0x008000, 0x2b0b8f08 );
/*TODO*///		ROM_LOAD( "7655.rom", 0x0f0000, 0x008000, 0x3433fe7b );
/*TODO*///		ROM_LOAD( "7647.rom", 0x0f8000, 0x008000, 0x2e7fbec0 );
/*TODO*///	
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "13.16d", 0x00000, 0x004000, 0x81c82fc9 );
/*TODO*///		ROM_LOAD( "12.16e", 0x04000, 0x004000, 0x755bfdad );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_SOUND1 );/* Sega PCM sound data */
/*TODO*///		ROM_LOAD( "7681.rom", 0x00000, 0x008000, 0xbc0c4d12 );
/*TODO*///		ROM_LOAD( "7680.rom", 0x08000, 0x008000, 0x627b3c8c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU3 );/* second 68000 CPU */
/*TODO*///		ROM_LOAD_EVEN("7634.rom", 0x0000, 0x8000, 0x3e07fd32 )
/*TODO*///		ROM_LOAD_ODD ("7635.rom", 0x0000, 0x8000, 0x22f762ab )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_GFX3 );/* Road Graphics  (region size should be gr_bitmapwidth*256 )*/
/*TODO*///		ROM_LOAD( "7633.rom", 0x0000, 0x8000, 0x6f146210 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_endurob2 = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x040000+0x010000+0x040000, REGION_CPU1 );/* 68000 code + space for RAM + space for decrypted opcodes */
/*TODO*///		ROM_LOAD_EVEN( "enduro.a07", 0x000000, 0x08000, 0x259069bc )
/*TODO*///		ROM_LOAD_ODD ( "enduro.a04", 0x000000, 0x08000, 0xf584fbd9 )
/*TODO*///		ROM_LOAD_EVEN( "enduro.a08", 0x010000, 0x08000, 0xd234918c )
/*TODO*///		ROM_LOAD_ODD ( "enduro.a05", 0x010000, 0x08000, 0xa525dd57 )
/*TODO*///		ROM_LOAD_EVEN( "enduro.a09", 0x020000, 0x08000, 0xf6391091 )
/*TODO*///		ROM_LOAD_ODD ( "enduro.a06", 0x020000, 0x08000, 0x79b367d7 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "7644.rom", 0x00000, 0x08000, 0xe7a4ff90 );
/*TODO*///		ROM_LOAD( "7645.rom", 0x08000, 0x08000, 0x4caa0095 );
/*TODO*///		ROM_LOAD( "7646.rom", 0x10000, 0x08000, 0x7e432683 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000*2, REGION_GFX2 );/* sprites */
/*TODO*///	
/*TODO*///		ROM_LOAD( "7678.rom", 0x000000, 0x008000, 0x9fb5e656 );
/*TODO*///		ROM_LOAD( "7670.rom", 0x008000, 0x008000, 0xdbbe2f6e );
/*TODO*///		ROM_LOAD( "7662.rom", 0x010000, 0x008000, 0xcb0c13c5 );
/*TODO*///		ROM_LOAD( "7654.rom", 0x018000, 0x008000, 0x2db6520d );
/*TODO*///	
/*TODO*///		ROM_LOAD( "7677.rom", 0x020000, 0x008000, 0x7764765b );
/*TODO*///		ROM_LOAD( "7669.rom", 0x028000, 0x008000, 0xf9525faa );
/*TODO*///		ROM_LOAD( "enduro.a34", 0x030000, 0x008000, 0x296454d8 );
/*TODO*///		ROM_LOAD( "7653.rom", 0x038000, 0x008000, 0x46a52114 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "7676.rom", 0x040000, 0x008000, 0x2e42e0d4 );
/*TODO*///		ROM_LOAD( "7668.rom", 0x048000, 0x008000, 0xe115ce33 );
/*TODO*///		ROM_LOAD( "enduro.a35", 0x050000, 0x008000, 0x1ebe76df );
/*TODO*///		ROM_LOAD( "7652.rom", 0x058000, 0x008000, 0x2880cfdb );
/*TODO*///	
/*TODO*///		ROM_LOAD( "enduro.a20", 0x060000, 0x008000, 0x7c280bc8 );
/*TODO*///		ROM_LOAD( "enduro.a28", 0x068000, 0x008000, 0x321f034b );
/*TODO*///		ROM_LOAD( "enduro.a36", 0x070000, 0x008000, 0x243e34e5 );
/*TODO*///		ROM_LOAD( "enduro.a44", 0x078000, 0x008000, 0x84bb12a1 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "7674.rom", 0x080000, 0x008000, 0x1a129acf );
/*TODO*///		ROM_LOAD( "7666.rom", 0x088000, 0x008000, 0x23697257 );
/*TODO*///		ROM_LOAD( "7658.rom", 0x090000, 0x008000, 0x1677f24f );
/*TODO*///		ROM_LOAD( "7650.rom", 0x098000, 0x008000, 0x642635ec );
/*TODO*///	
/*TODO*///		ROM_LOAD( "7673.rom", 0x0a0000, 0x008000, 0x82602394 );
/*TODO*///		ROM_LOAD( "7665.rom", 0x0a8000, 0x008000, 0x12d77607 );
/*TODO*///		ROM_LOAD( "7657.rom", 0x0b0000, 0x008000, 0x8158839c );
/*TODO*///		ROM_LOAD( "7649.rom", 0x0b8000, 0x008000, 0x4edba14c );
/*TODO*///	
/*TODO*///		ROM_LOAD( "7672.rom", 0x0c0000, 0x008000, 0xd11452f7 );
/*TODO*///		ROM_LOAD( "7664.rom", 0x0c8000, 0x008000, 0x0df2cfad );
/*TODO*///		ROM_LOAD( "enduro.a39", 0x0d0000, 0x008000, 0x1ff3a5e2 );
/*TODO*///		ROM_LOAD( "7648.rom", 0x0d8000, 0x008000, 0x983ea830 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "7671.rom", 0x0e0000, 0x008000, 0xb0c7fdc6 );
/*TODO*///		ROM_LOAD( "7663.rom", 0x0e8000, 0x008000, 0x2b0b8f08 );
/*TODO*///		ROM_LOAD( "7655.rom", 0x0f0000, 0x008000, 0x3433fe7b );
/*TODO*///		ROM_LOAD( "7647.rom", 0x0f8000, 0x008000, 0x2e7fbec0 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "enduro.a16", 0x00000, 0x008000, 0xd2cb6eb5 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_SOUND1 );/* Sega PCM sound data */
/*TODO*///		ROM_LOAD( "7681.rom", 0x00000, 0x008000, 0xbc0c4d12 );
/*TODO*///		ROM_LOAD( "7680.rom", 0x08000, 0x008000, 0x627b3c8c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU3 );/* second 68000 CPU */
/*TODO*///		ROM_LOAD_EVEN("7634.rom", 0x0000, 0x8000, 0x3e07fd32 )
/*TODO*///		ROM_LOAD_ODD ("7635.rom", 0x0000, 0x8000, 0x22f762ab )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_GFX3 );/* Road Graphics  (region size should be gr_bitmapwidth*256 )*/
/*TODO*///		ROM_LOAD( "7633.rom", 0x0000, 0x8000, 0x6f146210 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr er_io_analog_r = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		switch(READ_WORD(&sys16_extraram3[0x30]))
/*TODO*///		{
/*TODO*///			case 0:		// accel
/*TODO*///				if(input_port_1_r( offset ) & 1)
/*TODO*///					return 0xff;
/*TODO*///				else
/*TODO*///					return 0;
/*TODO*///			case 4:		// brake
/*TODO*///				if(input_port_1_r( offset ) & 2)
/*TODO*///					return 0xff;
/*TODO*///				else
/*TODO*///					return 0;
/*TODO*///			case 8:		// bank up down?
/*TODO*///				if(input_port_1_r( offset ) & 4)
/*TODO*///					return 0xff;
/*TODO*///				else
/*TODO*///					return 0;
/*TODO*///			case 12:	// handle
/*TODO*///				return input_port_0_r( offset );
/*TODO*///	
/*TODO*///		}
/*TODO*///		return 0;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr er_reset2_r = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		cpu_set_reset_line(2,PULSE_LINE);
/*TODO*///		return 0;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static MemoryReadAddress enduror_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x040000, 0x043fff, MRA_EXTRAM ),
/*TODO*///		new MemoryReadAddress( 0x100000, 0x107fff, sys16_tileram_r ),
/*TODO*///		new MemoryReadAddress( 0x108000, 0x108fff, sys16_textram_r ),
/*TODO*///		new MemoryReadAddress( 0x110000, 0x110fff, paletteram_word_r ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0x124000, 0x127fff, shared_ram_r ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0x130000, 0x130fff, MRA_BANK2 ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0x140010, 0x140011, io_service_r ),
/*TODO*///		new MemoryReadAddress( 0x140014, 0x140015, io_dip1_r ),
/*TODO*///		new MemoryReadAddress( 0x140016, 0x140017, io_dip2_r ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0x140030, 0x140031, er_io_analog_r ),
/*TODO*///	
/*TODO*///		new MemoryReadAddress( 0x140000, 0x1400ff, MRA_EXTRAM3 ),		//io
/*TODO*///		new MemoryReadAddress( 0xe00000, 0xe00001, er_reset2_r ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress enduror_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x040000, 0x043fff, MWA_EXTRAM ),
/*TODO*///		new MemoryWriteAddress( 0x100000, 0x107fff, sys16_tileram_w,sys16_tileram ),
/*TODO*///		new MemoryWriteAddress( 0x108000, 0x108fff, sys16_textram_w,sys16_textram ),
/*TODO*///		new MemoryWriteAddress( 0x110000, 0x110fff, sys16_paletteram_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0x124000, 0x127fff, shared_ram_w, shared_ram ),
/*TODO*///		new MemoryWriteAddress( 0x130000, 0x130fff, MWA_BANK2,sys16_spriteram ),
/*TODO*///		new MemoryWriteAddress( 0x140000, 0x140001, sound_command_nmi_w ),
/*TODO*///		new MemoryWriteAddress( 0x140000, 0x1400ff, MWA_EXTRAM3 ),		//io
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr enduro_p2_skip_r = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		if (cpu_get_pc()==0x4ba) {cpu_spinuntil_int(); return 0xffff;}
/*TODO*///	
/*TODO*///		return READ_WORD(&shared_ram[0x2000]);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static MemoryReadAddress enduror_readmem2[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0xc68000, 0xc68fff, MRA_EXTRAM2 ),
/*TODO*///		new MemoryReadAddress( 0xc7e000, 0xc7e001, enduro_p2_skip_r ),
/*TODO*///		new MemoryReadAddress( 0xc7c000, 0xc7ffff, shared_ram_r ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress enduror_writemem2[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0xc68000, 0xc68fff, MWA_BANK4,sys16_extraram2 ),
/*TODO*///		new MemoryWriteAddress( 0xc7c000, 0xc7ffff, shared_ram_w, shared_ram ),
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryReadAddress enduror_sound_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0xc000, 0xc7ff, MRA_RAM ),
/*TODO*///		new MemoryReadAddress( 0xd000, 0xd000, YM2203_status_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0xe000, 0xe7ff, SEGAPCMReadReg ),
/*TODO*///		new MemoryReadAddress( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress enduror_sound_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0xc000, 0xc7ff, MWA_RAM ),
/*TODO*///		new MemoryWriteAddress( 0xd000, 0xd000, YM2203_control_port_0_w ),
/*TODO*///		new MemoryWriteAddress( 0xd001, 0xd001, YM2203_write_port_0_w ),
/*TODO*///		new MemoryWriteAddress( 0xe000, 0xe7ff, SEGAPCMWriteReg ),
/*TODO*///		new MemoryWriteAddress( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	static IOReadPort enduror_sound_readport[] =
/*TODO*///	{
/*TODO*///		new IOReadPort( 0x40, 0x40, soundlatch_r ),
/*TODO*///		new IOReadPort( -1 )	/* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	static IOWritePort enduror_sound_writeport[] =
/*TODO*///	{
/*TODO*///		new IOWritePort( -1 )
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryReadAddress enduror_b2_sound_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
/*TODO*///	//	new MemoryReadAddress( 0xc000, 0xc7ff, MRA_RAM ),
/*TODO*///		new MemoryReadAddress( 0xf000, 0xf7ff, SEGAPCMReadReg ),
/*TODO*///		new MemoryReadAddress( 0xf800, 0xffff, MRA_RAM ),
/*TODO*///		new MemoryReadAddress( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress enduror_b2_sound_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
/*TODO*///	//	new MemoryWriteAddress( 0xc000, 0xc7ff, MWA_RAM ),
/*TODO*///		new MemoryWriteAddress( 0xf000, 0xf7ff, SEGAPCMWriteReg ),
/*TODO*///		new MemoryWriteAddress( 0xf800, 0xffff, MWA_RAM ),
/*TODO*///		new MemoryWriteAddress( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	static IOReadPort enduror_b2_sound_readport[] =
/*TODO*///	{
/*TODO*///		new IOReadPort( 0x00, 0x00, YM2203_status_port_0_r ),
/*TODO*///		new IOReadPort( 0x80, 0x80, YM2203_status_port_1_r ),
/*TODO*///		new IOReadPort( 0xc0, 0xc0, YM2203_status_port_2_r ),
/*TODO*///		new IOReadPort( 0x40, 0x40, soundlatch_r ),
/*TODO*///		new IOReadPort( -1 )	/* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	static IOWritePort enduror_b2_sound_writeport[] =
/*TODO*///	{
/*TODO*///		new IOWritePort( 0x00, 0x00, YM2203_control_port_0_w ),
/*TODO*///		new IOWritePort( 0x01, 0x01, YM2203_write_port_0_w ),
/*TODO*///		new IOWritePort( 0x80, 0x80, YM2203_control_port_1_w ),
/*TODO*///		new IOWritePort( 0x81, 0x81, YM2203_write_port_1_w ),
/*TODO*///		new IOWritePort( 0xc0, 0xc0, YM2203_control_port_2_w ),
/*TODO*///		new IOWritePort( 0xc1, 0xc1, YM2203_write_port_2_w ),
/*TODO*///		new IOWritePort( -1 )
/*TODO*///	};
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	static void enduror_update_proc( void ){
/*TODO*///		int data;
/*TODO*///		sys16_fg_scrollx = READ_WORD( &sys16_textram[0x0ff8] ) & 0x01ff;
/*TODO*///		sys16_bg_scrollx = READ_WORD( &sys16_textram[0x0ffa] ) & 0x01ff;
/*TODO*///		sys16_fg_scrolly = READ_WORD( &sys16_textram[0x0f24] ) & 0x01ff;
/*TODO*///		sys16_bg_scrolly = READ_WORD( &sys16_textram[0x0f26] ) & 0x01ff;
/*TODO*///	
/*TODO*///		data = READ_WORD( &sys16_textram[0x0e9e] );
/*TODO*///	
/*TODO*///		sys16_fg_page[0] = data>>12;
/*TODO*///		sys16_fg_page[1] = (data>>8)&0xf;
/*TODO*///		sys16_fg_page[3] = (data>>4)&0xf;
/*TODO*///		sys16_fg_page[2] = data&0xf;
/*TODO*///	
/*TODO*///		data = READ_WORD( &sys16_textram[0x0e9c] );
/*TODO*///		sys16_bg_page[0] = data>>12;
/*TODO*///		sys16_bg_page[1] = (data>>8)&0xf;
/*TODO*///		sys16_bg_page[3] = (data>>4)&0xf;
/*TODO*///		sys16_bg_page[2] = data&0xf;
/*TODO*///	
/*TODO*///		data = READ_WORD( &sys16_extraram3[2] );
/*TODO*///		set_refresh_3d( data );
/*TODO*///	
/*TODO*///		if(data & 4)
/*TODO*///		{
/*TODO*///			osd_led_w(0,1);
/*TODO*///			osd_led_w(1,1);
/*TODO*///			osd_led_w(2,1);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			osd_led_w(0,0);
/*TODO*///			osd_led_w(1,0);
/*TODO*///			osd_led_w(2,0);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	public static InitMachinePtr enduror_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = { 00,01,02,03,04,05,06,07,00,00,00,00,00,00,00,00};
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///		sys16_textmode=1;
/*TODO*///		sys16_spritesystem = 6;
/*TODO*///		sys16_sprxoffset = -0xc0;
/*TODO*///		sys16_fgxoffset = 13;
/*TODO*///	//	sys16_sprxoffset = -0xbb;
/*TODO*///	//	sys16_fgxoffset = 8;
/*TODO*///		sys16_textlayer_lo_min=0;
/*TODO*///		sys16_textlayer_lo_max=0;
/*TODO*///		sys16_textlayer_hi_min=0;
/*TODO*///		sys16_textlayer_hi_max=0xff;
/*TODO*///	
/*TODO*///		sys16_update_proc = enduror_update_proc;
/*TODO*///	
/*TODO*///		gr_ver = &sys16_extraram2[0x0];
/*TODO*///		gr_hor = gr_ver+0x200;
/*TODO*///		gr_pal = gr_ver+0x400;
/*TODO*///		gr_flip= gr_ver+0x600;
/*TODO*///		gr_palette= 0xf80 / 2;
/*TODO*///		gr_palette_default = 0x70 /2;
/*TODO*///		gr_colorflip[0][0]=0x00 / 2;
/*TODO*///		gr_colorflip[0][1]=0x02 / 2;
/*TODO*///		gr_colorflip[0][2]=0x04 / 2;
/*TODO*///		gr_colorflip[0][3]=0x00 / 2;
/*TODO*///		gr_colorflip[1][0]=0x00 / 2;
/*TODO*///		gr_colorflip[1][1]=0x00 / 2;
/*TODO*///		gr_colorflip[1][2]=0x06 / 2;
/*TODO*///		gr_colorflip[1][3]=0x00 / 2;
/*TODO*///	
/*TODO*///		sys16_sh_shadowpal=0xff;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	static void enduror_sprite_decode( void ){
/*TODO*///		unsigned char *RAM = memory_region(REGION_CPU1);
/*TODO*///		sys16_sprite_decode2( 8,0x020000 ,1);
/*TODO*///		generate_gr_screen(512,1024,8,0,4,0x8000);
/*TODO*///	
/*TODO*///	//	enduror_decode_data (RAM,RAM,0x10000);	// no decrypt info.
/*TODO*///		enduror_decode_data (RAM+0x10000,RAM+0x10000,0x10000);
/*TODO*///		enduror_decode_data2(RAM+0x20000,RAM+0x20000,0x10000);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void endurob_sprite_decode( void ){
/*TODO*///		sys16_sprite_decode2( 8,0x020000 ,1);
/*TODO*///		generate_gr_screen(512,1024,8,0,4,0x8000);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void endurora_opcode_decode( void )
/*TODO*///	{
/*TODO*///		unsigned char *rom = memory_region(REGION_CPU1);
/*TODO*///		int diff = 0x50000;	/* place decrypted opcodes in a hole after RAM */
/*TODO*///	
/*TODO*///	
/*TODO*///		memory_set_opcode_base(0,rom+diff);
/*TODO*///	
/*TODO*///		memcpy(rom+diff+0x10000,rom+0x10000,0x20000);
/*TODO*///		memcpy(rom+diff,rom+0x30000,0x10000);
/*TODO*///	
/*TODO*///		// patch code to force a reset on cpu2 when starting a new game.
/*TODO*///		// Undoubtly wrong, but something like it is needed for the game to work
/*TODO*///		WRITE_WORD(&rom[0x1866 + diff],0x4a79);
/*TODO*///		WRITE_WORD(&rom[0x1868 + diff],0x00e0);
/*TODO*///		WRITE_WORD(&rom[0x186a + diff],0x0000);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static void endurob2_opcode_decode( void )
/*TODO*///	{
/*TODO*///		unsigned char *rom = memory_region(REGION_CPU1);
/*TODO*///		int diff = 0x50000;	/* place decrypted opcodes in a hole after RAM */
/*TODO*///	
/*TODO*///	
/*TODO*///		memory_set_opcode_base(0,rom+diff);
/*TODO*///	
/*TODO*///		memcpy(rom+diff,rom,0x30000);
/*TODO*///	
/*TODO*///		endurob2_decode_data (rom,rom+diff,0x10000);
/*TODO*///		endurob2_decode_data2(rom+0x10000,rom+diff+0x10000,0x10000);
/*TODO*///	
/*TODO*///		// patch code to force a reset on cpu2 when starting a new game.
/*TODO*///		// Undoubtly wrong, but something like it is needed for the game to work
/*TODO*///		WRITE_WORD(&rom[0x1866 + diff],0x4a79);
/*TODO*///		WRITE_WORD(&rom[0x1868 + diff],0x00e0);
/*TODO*///		WRITE_WORD(&rom[0x186a + diff],0x0000);
/*TODO*///	}
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_enduror = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_MaxShadowColors=NumOfShadowColors / 2;
/*TODO*///	//	sys16_MaxShadowColors=0;
/*TODO*///	
/*TODO*///		enduror_sprite_decode();
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_endurobl = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_MaxShadowColors=NumOfShadowColors / 2;
/*TODO*///	//	sys16_MaxShadowColors=0;
/*TODO*///	
/*TODO*///		endurob_sprite_decode();
/*TODO*///		endurora_opcode_decode();
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_endurob2 = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///		sys16_MaxShadowColors=NumOfShadowColors / 2;
/*TODO*///	//	sys16_MaxShadowColors=0;
/*TODO*///	
/*TODO*///		endurob_sprite_decode();
/*TODO*///		endurob2_opcode_decode();
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_enduror = new InputPortPtr(){ public void handler() { 
/*TODO*///	PORT_START(); 	/* handle right left */
/*TODO*///		PORT_ANALOG( 0xff, 0x7f, IPT_AD_STICK_X | IPF_REVERSE | IPF_CENTER, 100, 4, 0x0, 0xff );
/*TODO*///	
/*TODO*///	PORT_START(); 	/* Fake Buttons */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON1 );// accel
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON2 );// brake
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN );// wheelie
/*TODO*///	
/*TODO*///	PORT_START(); 
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN3 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		SYS16_COINAGE
/*TODO*///	
/*TODO*///	PORT_START(); 	/* DSW1 */
/*TODO*///		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
/*TODO*///		PORT_DIPSETTING(    0x01, "Moving" );
/*TODO*///		PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x04, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x06, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x02, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0x18, 0x18, "Time Adjust" );
/*TODO*///		PORT_DIPSETTING(    0x10, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x18, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x08, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0x60, 0x60, "Time Control" );
/*TODO*///		PORT_DIPSETTING(    0x40, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x60, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x20, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///	//PORT_START(); 	/* Y */
/*TODO*///	//	PORT_ANALOG( 0xff, 0x0, IPT_AD_STICK_Y | IPF_CENTER , 100, 8, 0x0, 0xff );
/*TODO*///	
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	static MachineDriver machine_driver_enduror = new MachineDriver
/*TODO*///	(
/*TODO*///		new MachineCPU[] {
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_M68000,
/*TODO*///				10000000,
/*TODO*///				enduror_readmem,enduror_writemem,null,null,
/*TODO*///				sys16_interrupt,1
/*TODO*///			),
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_Z80 | CPU_AUDIO_CPU,
/*TODO*///				4096000,
/*TODO*///				enduror_sound_readmem,enduror_sound_writemem,enduror_sound_readport,enduror_sound_writeport,
/*TODO*///				interrupt,4
/*TODO*///			),
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_M68000,
/*TODO*///				10000000,
/*TODO*///				enduror_readmem2,enduror_writemem2,null,null,
/*TODO*///				sys16_interrupt,1
/*TODO*///			),
/*TODO*///		},
/*TODO*///		60, DEFAULT_60HZ_VBLANK_DURATION,
/*TODO*///		1,
/*TODO*///		enduror_init_machine,
/*TODO*///		40*8, 28*8, new rectangle( 0*8, 40*8-1, 0*8, 28*8-1 ),
/*TODO*///		gfx8,
/*TODO*///		2048*ShadowColorsMultiplier,2048*ShadowColorsMultiplier,
/*TODO*///		null,
/*TODO*///		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
/*TODO*///		null,
/*TODO*///		sys16_ho_vh_start,
/*TODO*///		sys16_vh_stop,
/*TODO*///		sys16_ho_vh_screenrefresh,
/*TODO*///		SOUND_SUPPORTS_STEREO,0,0,0,
/*TODO*///		new MachineSound[] {
/*TODO*///			new MachineSound(
/*TODO*///				SOUND_YM2203,
/*TODO*///				ym2203_interface
/*TODO*///			),
/*TODO*///			new MachineSound(
/*TODO*///				SOUND_SEGAPCM,
/*TODO*///				segapcm_interface_32k,
/*TODO*///			)
/*TODO*///		}
/*TODO*///	);
/*TODO*///	
/*TODO*///	static MachineDriver machine_driver_endurob2 = new MachineDriver
/*TODO*///	(
/*TODO*///		new MachineCPU[] {
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_M68000,
/*TODO*///				10000000,
/*TODO*///				enduror_readmem,enduror_writemem,null,null,
/*TODO*///				sys16_interrupt,1
/*TODO*///			),
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_Z80 | CPU_AUDIO_CPU,
/*TODO*///				4096000,
/*TODO*///				enduror_b2_sound_readmem,enduror_b2_sound_writemem,enduror_b2_sound_readport,enduror_b2_sound_writeport,
/*TODO*///				ignore_interrupt,1
/*TODO*///			),
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_M68000,
/*TODO*///				10000000,
/*TODO*///				enduror_readmem2,enduror_writemem2,null,null,
/*TODO*///				sys16_interrupt,1
/*TODO*///			),
/*TODO*///		},
/*TODO*///		60, DEFAULT_60HZ_VBLANK_DURATION,
/*TODO*///		2,
/*TODO*///		enduror_init_machine,
/*TODO*///		40*8, 28*8, new rectangle( 0*8, 40*8-1, 0*8, 28*8-1 ),
/*TODO*///		gfx8,
/*TODO*///		2048*ShadowColorsMultiplier,2048*ShadowColorsMultiplier,
/*TODO*///		null,
/*TODO*///		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
/*TODO*///		null,
/*TODO*///		sys16_ho_vh_start,
/*TODO*///		sys16_vh_stop,
/*TODO*///		sys16_ho_vh_screenrefresh,
/*TODO*///		SOUND_SUPPORTS_STEREO,0,0,0,
/*TODO*///		new MachineSound[] {
/*TODO*///			new MachineSound(
/*TODO*///				SOUND_YM2203,
/*TODO*///				ym2203_interface2
/*TODO*///			),
/*TODO*///			new MachineSound(
/*TODO*///				SOUND_SEGAPCM,
/*TODO*///				segapcm_interface_15k,
/*TODO*///			)
/*TODO*///		}
/*TODO*///	);
/*TODO*///	
/*TODO*///	/*****************************************************************************/
/*TODO*///	/* Dummy drivers for games that don't have a working clone and are protected */
/*TODO*///	/*****************************************************************************/
/*TODO*///	
/*TODO*///	static MemoryReadAddress sys16_dummy_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x0fffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0xff0000, 0xffffff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress sys16_dummy_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x0fffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0xff0000, 0xffffff, MWA_BANK1,sys16_workingram ),
/*TODO*///	
/*TODO*///		new MemoryWriteAddress(-1)
/*TODO*///	};
/*TODO*///	
/*TODO*///	public static InitMachinePtr sys16_dummy_init_machine = new InitMachinePtr() { public void handler() {
/*TODO*///		static int bank[16] = { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
/*TODO*///		sys16_obj_bank = bank;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_s16dummy = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		sys16_onetime_init_machine();
/*TODO*///	//	sys16_sprite_decode( 4,0x040000 );
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_s16dummy = new InputPortPtr(){ public void handler() { 
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	MACHINE_DRIVER( machine_driver_s16dummy, \
/*TODO*///		sys16_dummy_readmem,sys16_dummy_writemem,sys16_dummy_init_machine, gfx8)
/*TODO*///	
/*TODO*///	/*****************************************************************************/
/*TODO*///	// Ace Attacker
/*TODO*///	
/*TODO*///	// sys18
/*TODO*///	static RomLoadPtr rom_aceattac = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "11491.4a", 0x000000, 0x10000, 0x77b820f1 )
/*TODO*///		ROM_LOAD_ODD ( "11489.1a", 0x000000, 0x10000, 0xbbe623c5 )
/*TODO*///		ROM_LOAD_EVEN( "11492.5a", 0x020000, 0x10000, 0xd8bd3139 )
/*TODO*///		ROM_LOAD_ODD ( "11490.2a", 0x020000, 0x10000, 0x38cb3a41 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "11493.9b",  0x00000, 0x10000, 0x654485d9 );
/*TODO*///		ROM_LOAD( "11494.10b", 0x10000, 0x10000, 0xb67971ab );
/*TODO*///		ROM_LOAD( "11495.11b", 0x20000, 0x10000, 0xb687ab61 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x80000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "11501.1b", 0x00000, 0x10000, 0x09179ead );
/*TODO*///		ROM_LOAD( "11502.2b", 0x10000, 0x10000, 0xa3ee36b8 );
/*TODO*///		ROM_LOAD( "11503.3b", 0x20000, 0x10000, 0x344c0692 );
/*TODO*///		ROM_LOAD( "11504.4b", 0x30000, 0x10000, 0x7cae7920 );
/*TODO*///		ROM_LOAD( "11505.5b", 0x40000, 0x10000, 0xb67f1ecf );
/*TODO*///		ROM_LOAD( "11506.6b", 0x50000, 0x10000, 0xb0104def );
/*TODO*///		ROM_LOAD( "11507.7b", 0x60000, 0x10000, 0xa2af710a );
/*TODO*///		ROM_LOAD( "11508.8b", 0x70000, 0x10000, 0x5cbb833c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "11496.7a",	 0x00000, 0x08000, 0x82cb40a9 );
/*TODO*///		ROM_LOAD( "11497.8a",    0x10000, 0x08000, 0xb04f62cc );
/*TODO*///		ROM_LOAD( "11498.9a",    0x18000, 0x08000, 0x97baf52b );
/*TODO*///		ROM_LOAD( "11499.10a",   0x20000, 0x08000, 0xea332866 );
/*TODO*///		ROM_LOAD( "11500.11a",   0x28000, 0x08000, 0x2ddf1c31 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/*****************************************************************************/
/*TODO*///	/*****************************************************************************/
/*TODO*///	// After Burner
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_aburner = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD( "epr10949.bin",0x000000,0x20000, 0xd8437d92 );
/*TODO*///		ROM_LOAD( "epr10948.bin",0x000000,0x20000, 0x64284761 );
/*TODO*///		ROM_LOAD( "epr10947.bin",0x000000,0x20000, 0x08838392 );
/*TODO*///		ROM_LOAD( "epr10946.bin",0x000000,0x20000, 0xd7d485f4 );
/*TODO*///		ROM_LOAD( "epr10945.bin",0x000000,0x20000, 0xdf4d4c4f );
/*TODO*///		ROM_LOAD( "epr10944.bin",0x000000,0x20000, 0x17be8f67 );
/*TODO*///		ROM_LOAD( "epr10943.bin",0x000000,0x20000, 0xb98294dc );
/*TODO*///		ROM_LOAD( "epr10942.bin",0x000000,0x20000, 0x5ce10b8c );
/*TODO*///		ROM_LOAD( "epr10941.bin",0x000000,0x20000, 0x136ea264 );
/*TODO*///		ROM_LOAD( "epr10940.bin",0x000000,0x20000, 0x4d132c4e );
/*TODO*///		ROM_LOAD( "epr10928.bin",0x000000,0x20000, 0x7c01d40b );
/*TODO*///		ROM_LOAD( "epr10927.bin",0x000000,0x20000, 0x66d36757 );
/*TODO*///		ROM_LOAD( "epr10926.bin",0x000000,0x10000, 0xed8bd632 );
/*TODO*///		ROM_LOAD( "epr10925.bin",0x000000,0x10000, 0x4ef048cc );
/*TODO*///		ROM_LOAD( "epr10924.bin",0x000000,0x10000, 0x50c15a6d );
/*TODO*///		ROM_LOAD( "epr10923.bin",0x000000,0x10000, 0x6888eb8f );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000*2, REGION_GFX2 );/* sprites */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU3 );/* 2nd 68000 code */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_GFX3 );/* gr */
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	/*****************************************************************************/
/*TODO*///	// After Burner II
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_aburner2 = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "11107.58",  0x000000, 0x20000, 0x6d87bab7 )
/*TODO*///		ROM_LOAD_ODD ( "11108.104", 0x000000, 0x20000, 0x202a3e1d )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "11115.154", 0x00000, 0x10000, 0xe8e32921 );
/*TODO*///		ROM_LOAD( "11114.153", 0x10000, 0x10000, 0x2e97f633 );
/*TODO*///		ROM_LOAD( "11113.152", 0x20000, 0x10000, 0x36058c8c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "10932.125", 0x000000, 0x20000, 0xcc0821d6 );
/*TODO*///		ROM_LOAD( "10933.126", 0x020000, 0x20000, 0xc8efb2c3 );
/*TODO*///		ROM_LOAD( "10934.129", 0x040000, 0x20000, 0x4a51b1fa );
/*TODO*///		ROM_LOAD( "10935.130", 0x060000, 0x20000, 0xc1e23521 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "10936.133", 0x080000, 0x20000, 0xada70d64 );
/*TODO*///		ROM_LOAD( "10937.134", 0x0a0000, 0x20000, 0x00a6144f );
/*TODO*///		ROM_LOAD( "10938.102", 0x0c0000, 0x20000, 0xe7675baf );
/*TODO*///		ROM_LOAD( "10939.103", 0x0e0000, 0x20000, 0xa0d49480 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "11103.127", 0x100000, 0x20000, 0xbdd60da2 );
/*TODO*///		ROM_LOAD( "11104.131", 0x120000, 0x20000, 0x06a35fce );
/*TODO*///		ROM_LOAD( "11105.135", 0x140000, 0x20000, 0x027b0689 );
/*TODO*///		ROM_LOAD( "11106.104", 0x160000, 0x20000, 0x9e1fec09 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "11116.128", 0x180000, 0x20000, 0x49b4c1ba );
/*TODO*///		ROM_LOAD( "11117.132", 0x1a0000, 0x20000, 0x821fbb71 );
/*TODO*///		ROM_LOAD( "11118.136", 0x1c0000, 0x20000, 0x8f38540b );
/*TODO*///		ROM_LOAD( "11119.105", 0x1e0000, 0x20000, 0xd0343a8e );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "11112.17",    0x00000, 0x10000, 0xd777fc6d );
/*TODO*///		ROM_LOAD( "11102.13",    0x10000, 0x20000, 0x6c07c78d );
/*TODO*///		ROM_LOAD( "10931.11",    0x30000, 0x20000, 0x9209068f );
/*TODO*///		ROM_LOAD( "10930.12",    0x30000, 0x20000, 0x6493368b );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU3 );/* 2nd 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "11109.20", 0x000000, 0x20000, 0x85a0fe07 )
/*TODO*///		ROM_LOAD_ODD ( "11110.29", 0x000000, 0x20000, 0xf3d6797c )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_GFX3 );/* gr */
/*TODO*///		ROM_LOAD_ODD ( "10922.40", 0x000000, 0x10000, 0xb49183d4 )
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	/*****************************************************************************/
/*TODO*///	// Bloxeed
/*TODO*///	
/*TODO*///	// sys18
/*TODO*///	static RomLoadPtr rom_bloxeed = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "rom-e.rom", 0x000000, 0x20000, 0xa481581a )
/*TODO*///		ROM_LOAD_ODD ( "rom-o.rom", 0x000000, 0x20000, 0xdd1bc3bf )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "scr0.rom", 0x00000, 0x10000, 0xe024aa33 );
/*TODO*///		ROM_LOAD( "scr1.rom", 0x10000, 0x10000, 0x8041b814 );
/*TODO*///		ROM_LOAD( "scr2.rom", 0x20000, 0x10000, 0xde32285e );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "obj0-e.rom", 0x00000, 0x10000, 0x90d31a8c );
/*TODO*///		ROM_LOAD( "obj0-o.rom", 0x10000, 0x10000, 0xf0c0f49d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "sound0.rom",	 0x00000, 0x20000, 0x6f2fc63c );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	/*****************************************************************************/
/*TODO*///	// Clutch Hitter
/*TODO*///	// sys18
/*TODO*///	static RomLoadPtr rom_cltchitr = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "epr13795.6a", 0x000000, 0x40000, 0xb0b60b67 )
/*TODO*///		ROM_LOAD_ODD ( "epr13751.4a", 0x000000, 0x40000, 0xc8d80233 )
/*TODO*///		ROM_LOAD_EVEN( "epr13786.7a", 0x080000, 0x40000, 0x3095dac0 )
/*TODO*///		ROM_LOAD_ODD ( "epr13784.5a", 0x080000, 0x40000, 0x80c8180d )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x180000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "mpr13787.10a", 0x000000, 0x80000, 0xf05c68c6 );
/*TODO*///		ROM_LOAD( "mpr13788.11a", 0x080000, 0x80000, 0x0106fea6 );
/*TODO*///		ROM_LOAD( "mpr13789.12a", 0x100000, 0x80000, 0x09ba8835 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x300000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "mpr13773.1c",  0x000000, 0x80000, 0x3fc600e5 );
/*TODO*///		ROM_LOAD( "mpr13774.2c",  0x080000, 0x80000, 0x2411a824 );
/*TODO*///		ROM_LOAD( "mpr13775.3c",  0x100000, 0x80000, 0xcf527bf6 );
/*TODO*///		ROM_LOAD( "mpr13779.10c", 0x180000, 0x80000, 0xc707f416 );
/*TODO*///		ROM_LOAD( "mpr13780.11c", 0x200000, 0x80000, 0xa4c341e0 );
/*TODO*///		ROM_LOAD( "mpr13781.12c", 0x280000, 0x80000, 0xf33b13af );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x180000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr13793.7c",    0x000000, 0x80000, 0xa3d31944 );
/*TODO*///		ROM_LOAD( "epr13791.5c",	0x080000, 0x80000, 0x35c16d80 );
/*TODO*///		ROM_LOAD( "epr13792.6c",    0x100000, 0x80000, 0x808f9695 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	/*****************************************************************************/
/*TODO*///	// Cotton
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_cotton = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU1 );/* 68000 code */
/*TODO*///	// custom cpu 317-?????
/*TODO*///		ROM_LOAD_EVEN( "epr13858.a7", 0x000000, 0x20000, 0x276f42fe )
/*TODO*///		ROM_LOAD_ODD ( "epr13856.a5", 0x000000, 0x20000, 0x14e6b5e7 )
/*TODO*///		ROM_LOAD_EVEN( "epr13859.a8", 0x040000, 0x20000, 0x4703ef9d )
/*TODO*///		ROM_LOAD_ODD ( "epr13857.a6", 0x040000, 0x20000, 0xde37e527 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "scr01.rom", 0x00000, 0x20000, 0xa47354b6 );
/*TODO*///		ROM_LOAD( "scr11.rom", 0x20000, 0x20000, 0xd38424b5 );
/*TODO*///		ROM_LOAD( "scr02.rom", 0x40000, 0x20000, 0x8c990026 );
/*TODO*///		ROM_LOAD( "scr12.rom", 0x60000, 0x20000, 0x21c15b8a );
/*TODO*///		ROM_LOAD( "scr03.rom", 0x80000, 0x20000, 0xd2b175bf );
/*TODO*///		ROM_LOAD( "scr13.rom", 0xa0000, 0x20000, 0xb9d62531 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "obj0-e.rom", 0x000000, 0x20000, 0xab4b3468 );
/*TODO*///		ROM_LOAD( "obj0-o.rom", 0x020000, 0x20000, 0x7024f404 );
/*TODO*///		ROM_LOAD( "obj1-e.rom", 0x040000, 0x20000, 0x69b41ac3 );
/*TODO*///		ROM_LOAD( "obj1-o.rom", 0x060000, 0x20000, 0x6169bba4 );
/*TODO*///		ROM_LOAD( "obj2-e.rom", 0x080000, 0x20000, 0x0801cf02 );
/*TODO*///		ROM_LOAD( "obj2-o.rom", 0x0a0000, 0x20000, 0xb014f02d );
/*TODO*///		ROM_LOAD( "obj3-e.rom", 0x0c0000, 0x20000, 0xf066f315 );
/*TODO*///		ROM_LOAD( "obj3-o.rom", 0x0e0000, 0x20000, 0xe62a7cd6 );
/*TODO*///		ROM_LOAD( "obj4-e.rom", 0x100000, 0x20000, 0x1bd145f3 );
/*TODO*///		ROM_LOAD( "obj4-o.rom", 0x120000, 0x20000, 0x943aba8b );
/*TODO*///		ROM_LOAD( "obj5-e.rom", 0x140000, 0x20000, 0x4fd59bff );
/*TODO*///		ROM_LOAD( "obj5-o.rom", 0x160000, 0x20000, 0x7ea93200 );
/*TODO*///		ROM_LOAD( "obj6-e.rom", 0x180000, 0x20000, 0x6a66868d );
/*TODO*///		ROM_LOAD( "obj6-o.rom", 0x1a0000, 0x20000, 0x1c942190 );
/*TODO*///		ROM_LOAD( "obj7-e.rom", 0x1c0000, 0x20000, 0x1c5ffad8 );
/*TODO*///		ROM_LOAD( "obj7-o.rom", 0x1e0000, 0x20000, 0x856f3ee2 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "s-prog.rom",	 0x00000, 0x08000, 0x6a57b027 );
/*TODO*///		ROM_LOAD( "speech0.rom", 0x10000, 0x20000, 0x4d21153f );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_cottona = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU1 );/* 68000 code */
/*TODO*///	// custom cpu 317-0181a
/*TODO*///		ROM_LOAD_EVEN( "ep13921a.a7", 0x000000, 0x20000, 0xf047a037 )
/*TODO*///		ROM_LOAD_ODD ( "ep13919a.a5", 0x000000, 0x20000, 0x651108b1 )
/*TODO*///		ROM_LOAD_EVEN( "ep13922a.a8", 0x040000, 0x20000, 0x1ca248c5 )
/*TODO*///		ROM_LOAD_ODD ( "ep13920a.a6", 0x040000, 0x20000, 0xfa3610f9 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "scr01.rom", 0x00000, 0x20000, 0xa47354b6 );
/*TODO*///		ROM_LOAD( "scr11.rom", 0x20000, 0x20000, 0xd38424b5 );
/*TODO*///		ROM_LOAD( "scr02.rom", 0x40000, 0x20000, 0x8c990026 );
/*TODO*///		ROM_LOAD( "scr12.rom", 0x60000, 0x20000, 0x21c15b8a );
/*TODO*///		ROM_LOAD( "scr03.rom", 0x80000, 0x20000, 0xd2b175bf );
/*TODO*///		ROM_LOAD( "scr13.rom", 0xa0000, 0x20000, 0xb9d62531 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "obj0-e.rom", 0x000000, 0x20000, 0xab4b3468 );
/*TODO*///		ROM_LOAD( "obj0-o.rom", 0x020000, 0x20000, 0x7024f404 );
/*TODO*///		ROM_LOAD( "obj1-e.rom", 0x040000, 0x20000, 0x69b41ac3 );
/*TODO*///		ROM_LOAD( "obj1-o.rom", 0x060000, 0x20000, 0x6169bba4 );
/*TODO*///		ROM_LOAD( "obj2-e.rom", 0x080000, 0x20000, 0x0801cf02 );
/*TODO*///		ROM_LOAD( "obj2-o.rom", 0x0a0000, 0x20000, 0xb014f02d );
/*TODO*///		ROM_LOAD( "obj3-e.rom", 0x0c0000, 0x20000, 0xf066f315 );
/*TODO*///		ROM_LOAD( "obj3-o.rom", 0x0e0000, 0x20000, 0xe62a7cd6 );
/*TODO*///		ROM_LOAD( "obj4-e.rom", 0x100000, 0x20000, 0x1bd145f3 );
/*TODO*///		ROM_LOAD( "obj4-o.rom", 0x120000, 0x20000, 0x943aba8b );
/*TODO*///		ROM_LOAD( "obj5-e.rom", 0x140000, 0x20000, 0x4fd59bff );
/*TODO*///		ROM_LOAD( "obj5-o.rom", 0x160000, 0x20000, 0x7ea93200 );
/*TODO*///		ROM_LOAD( "obj6-e.rom", 0x180000, 0x20000, 0x6a66868d );
/*TODO*///		ROM_LOAD( "obj6-o.rom", 0x1a0000, 0x20000, 0x1c942190 );
/*TODO*///		ROM_LOAD( "obj7-e.rom", 0x1c0000, 0x20000, 0x1c5ffad8 );
/*TODO*///		ROM_LOAD( "obj7-o.rom", 0x1e0000, 0x20000, 0x856f3ee2 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "s-prog.rom",	 0x00000, 0x08000, 0x6a57b027 );
/*TODO*///		ROM_LOAD( "speech0.rom", 0x10000, 0x20000, 0x4d21153f );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	/*****************************************************************************/
/*TODO*///	// DD Crew
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_ddcrew = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "14153.6a", 0x000000, 0x40000, 0xe01fae0c )
/*TODO*///		ROM_LOAD_ODD ( "14152.4a", 0x000000, 0x40000, 0x69c7b571 )
/*TODO*///		ROM_LOAD_EVEN( "14141.7a", 0x080000, 0x40000, 0x080a494b )
/*TODO*///		ROM_LOAD_ODD ( "14139.5a", 0x080000, 0x40000, 0x06c31531 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "14127.1c", 0x00000, 0x40000, 0x2228cd88 );
/*TODO*///		ROM_LOAD( "14128.2c", 0x40000, 0x40000, 0xedba8e10 );
/*TODO*///		ROM_LOAD( "14129.3c", 0x80000, 0x40000, 0xe8ecc305 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "14134.10c", 0x000000, 0x80000, 0x4fda6a4b );
/*TODO*///		ROM_LOAD( "14142.10a", 0x080000, 0x80000, 0x3cbf1f2a );
/*TODO*///		ROM_LOAD( "14135.11c", 0x100000, 0x80000, 0xe9c74876 );
/*TODO*///		ROM_LOAD( "14143.11a", 0x180000, 0x80000, 0x59022c31 );
/*TODO*///		ROM_LOAD( "14136.12c", 0x200000, 0x80000, 0x720d9858 );
/*TODO*///		ROM_LOAD( "14144.12a", 0x280000, 0x80000, 0x7775fdd4 );
/*TODO*///		ROM_LOAD( "14137.13c", 0x300000, 0x80000, 0x846c4265 );
/*TODO*///		ROM_LOAD( "14145.13a", 0x380000, 0x80000, 0x0e76c797 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x1a0000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "14133.7c",	 0x000000, 0x20000, 0xcff96665 );
/*TODO*///		ROM_LOAD( "14130.4c",    0x020000, 0x80000, 0x948f34a1 );
/*TODO*///		ROM_LOAD( "14131.5c",    0x0a0000, 0x80000, 0xbe5a7d0b );
/*TODO*///		ROM_LOAD( "14132.6c",    0x120000, 0x80000, 0x1fae0220 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	/*****************************************************************************/
/*TODO*///	// Dunk Shot
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_dunkshot = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "10468.bin", 0x000000, 0x8000, 0xe2d5f97a )
/*TODO*///		ROM_LOAD_ODD ( "10467.bin", 0x000000, 0x8000, 0x29774114 )
/*TODO*///		ROM_LOAD_EVEN( "10470.bin", 0x010000, 0x8000, 0x8c60761f )
/*TODO*///		ROM_LOAD_ODD ( "10469.bin", 0x010000, 0x8000, 0xaa442b81 )
/*TODO*///		ROM_LOAD_EVEN( "10472.bin", 0x020000, 0x8000, 0x206027a6 )
/*TODO*///		ROM_LOAD_ODD ( "10471.bin", 0x020000, 0x8000, 0x22777314 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "10485.bin", 0x00000, 0x8000, 0xf16dda29 );
/*TODO*///		ROM_LOAD( "10486.bin", 0x08000, 0x8000, 0x311d973c );
/*TODO*///		ROM_LOAD( "10487.bin", 0x10000, 0x8000, 0xa8fb179f );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "10481.bin", 0x00000, 0x8000, 0xfeb04bc9 );
/*TODO*///		ROM_LOAD( "10477.bin", 0x08000, 0x8000, 0xf9d3b2cb );
/*TODO*///		ROM_LOAD( "10482.bin", 0x10000, 0x8000, 0x5bc07618 );
/*TODO*///		ROM_LOAD( "10478.bin", 0x18000, 0x8000, 0x5b5c5c92 );
/*TODO*///		ROM_LOAD( "10483.bin", 0x20000, 0x8000, 0x7cab4f9e );
/*TODO*///		ROM_LOAD( "10479.bin", 0x28000, 0x8000, 0xe84190a0 );
/*TODO*///		ROM_LOAD( "10484.bin", 0x30000, 0x8000, 0xbcb5fcc9 );
/*TODO*///		ROM_LOAD( "10480.bin", 0x38000, 0x8000, 0x5dffd9dd );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x28000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "10473.bin",	 0x00000, 0x08000, 0x7f1f5a27 );
/*TODO*///		ROM_LOAD( "10474.bin",   0x10000, 0x08000, 0x419a656e );
/*TODO*///		ROM_LOAD( "10475.bin",   0x18000, 0x08000, 0x17d55e85 );
/*TODO*///		ROM_LOAD( "10476.bin",   0x20000, 0x08000, 0xa6be0956 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*****************************************************************************/
/*TODO*///	// Laser Ghost
/*TODO*///	
/*TODO*///	// sys18
/*TODO*///	static RomLoadPtr rom_lghost = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "13429", 0x000000, 0x20000, 0x0e0ccf26 )
/*TODO*///		ROM_LOAD_ODD ( "13437", 0x000000, 0x20000, 0x38b4dc2f )
/*TODO*///		ROM_LOAD_EVEN( "13411", 0x040000, 0x20000, 0xc3aeae07 )
/*TODO*///		ROM_LOAD_ODD ( "13413", 0x040000, 0x20000, 0x75f43e21 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x60000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "13414", 0x00000, 0x20000, 0x82025f3b );
/*TODO*///		ROM_LOAD( "13415", 0x20000, 0x20000, 0xa76852e9 );
/*TODO*///		ROM_LOAD( "13416", 0x40000, 0x20000, 0xe88db149 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "13603", 0x00000, 0x20000, 0x2e3cc07b );
/*TODO*///		ROM_LOAD( "13604", 0x20000, 0x20000, 0x576388af );
/*TODO*///		ROM_LOAD( "13421", 0x40000, 0x20000, 0xabee8771 );
/*TODO*///		ROM_LOAD( "13424", 0x60000, 0x20000, 0x260ab077 );
/*TODO*///		ROM_LOAD( "13422", 0x80000, 0x20000, 0x36cef12c );
/*TODO*///		ROM_LOAD( "13425", 0xa0000, 0x20000, 0xe0ff8807 );
/*TODO*///		ROM_LOAD( "13423", 0xc0000, 0x20000, 0x5b8e0053 );
/*TODO*///		ROM_LOAD( "13426", 0xe0000, 0x20000, 0xc689853b );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x80000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "13417",	 0x00000, 0x20000, 0xcd7beb49 );
/*TODO*///		ROM_LOAD( "13420",   0x20000, 0x20000, 0x03199cbb );
/*TODO*///		ROM_LOAD( "13419",   0x40000, 0x20000, 0xa918ef68 );
/*TODO*///		ROM_LOAD( "13418",   0x60000, 0x20000, 0x4006c9f1 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/*****************************************************************************/
/*TODO*///	// Line of Fire
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_loffire = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "epr12850.rom", 0x000000, 0x20000, 0x14598f2a )
/*TODO*///		ROM_LOAD_ODD ( "epr12849.rom", 0x000000, 0x20000, 0x61cfd2fe )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "opr12791.rom", 0x00000, 0x10000, 0xacfa69ba );
/*TODO*///		ROM_LOAD( "opr12792.rom", 0x10000, 0x10000, 0xe506723c );
/*TODO*///		ROM_LOAD( "opr12793.rom", 0x20000, 0x10000, 0x0ce8cce3 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "epr12775.rom", 0x000000, 0x20000, 0x693056ec );
/*TODO*///		ROM_LOAD( "epr12776.rom", 0x020000, 0x20000, 0x61efbdfd );
/*TODO*///		ROM_LOAD( "epr12777.rom", 0x040000, 0x20000, 0x29d5b953 );
/*TODO*///		ROM_LOAD( "epr12778.rom", 0x060000, 0x20000, 0x2fb68e07 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "epr12779.rom", 0x080000, 0x20000, 0xae58af7c );
/*TODO*///		ROM_LOAD( "epr12780.rom", 0x0a0000, 0x20000, 0xee670c1e );
/*TODO*///		ROM_LOAD( "epr12781.rom", 0x0c0000, 0x20000, 0x538f6bc5 );
/*TODO*///		ROM_LOAD( "epr12782.rom", 0x0e0000, 0x20000, 0x5acc34f7 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "epr12783.rom", 0x100000, 0x20000, 0xc13feea9 );
/*TODO*///		ROM_LOAD( "epr12784.rom", 0x120000, 0x20000, 0x39b94c65 );
/*TODO*///		ROM_LOAD( "epr12785.rom", 0x140000, 0x20000, 0x05ed0059 );
/*TODO*///		ROM_LOAD( "epr12786.rom", 0x160000, 0x20000, 0xa4123165 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "epr12787.rom", 0x180000, 0x20000, 0x6431a3a6 );
/*TODO*///		ROM_LOAD( "epr12788.rom", 0x1a0000, 0x20000, 0x1982a0ce );
/*TODO*///		ROM_LOAD( "epr12789.rom", 0x1c0000, 0x20000, 0x97d03274 );
/*TODO*///		ROM_LOAD( "epr12790.rom", 0x1e0000, 0x20000, 0x816e76e6 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x70000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr12798.rom",	 0x00000, 0x10000, 0x0587738d );
/*TODO*///		ROM_LOAD( "epr12799.rom",    0x10000, 0x20000, 0xbc60181c );
/*TODO*///		ROM_LOAD( "epr12800.rom",    0x30000, 0x20000, 0x1158c1a3 );
/*TODO*///		ROM_LOAD( "epr12801.rom",    0x50000, 0x20000, 0x2d6567c4 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU3 );/* 2nd 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "epr12803.rom", 0x000000, 0x20000, 0xc1d9e751 )
/*TODO*///		ROM_LOAD_ODD ( "epr12802.rom", 0x000000, 0x20000, 0xd746bb39 )
/*TODO*///		ROM_LOAD_EVEN( "epr12805.rom", 0x040000, 0x20000, 0x4a7200c3 )
/*TODO*///		ROM_LOAD_ODD ( "epr12804.rom", 0x040000, 0x20000, 0xb853480e )
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	/*****************************************************************************/
/*TODO*///	// MVP
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_mvp = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "13000.rom", 0x000000, 0x40000, 0x2e0e21ec )
/*TODO*///		ROM_LOAD_ODD ( "12999.rom", 0x000000, 0x40000, 0xfd213d28 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "13011.rom", 0x00000, 0x40000, 0x1cb871fc );
/*TODO*///		ROM_LOAD( "13012.rom", 0x40000, 0x40000, 0xb75e6821 );
/*TODO*///		ROM_LOAD( "13013.rom", 0x80000, 0x40000, 0xf1944a3c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "13010.rom", 0x000000, 0x40000, 0xdf37c567 );
/*TODO*///		ROM_LOAD( "13009.rom", 0x040000, 0x40000, 0x126d2e37 );
/*TODO*///		ROM_LOAD( "13006.rom", 0x080000, 0x40000, 0x2e9afd2f );
/*TODO*///		ROM_LOAD( "13003.rom", 0x0c0000, 0x40000, 0x21424151 );
/*TODO*///		ROM_LOAD( "13007.rom", 0x100000, 0x40000, 0x55c8605b );
/*TODO*///		ROM_LOAD( "13004.rom", 0x140000, 0x40000, 0x0aa09dd3 );
/*TODO*///		ROM_LOAD( "13008.rom", 0x180000, 0x40000, 0xb3d46dfc );
/*TODO*///		ROM_LOAD( "13005.rom", 0x1c0000, 0x40000, 0xc899c810 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x50000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "13002.rom",	 0x00000, 0x08000, 0x1b6e1515 );
/*TODO*///		ROM_LOAD( "13001.rom",   0x10000, 0x40000, 0xe8cace8c );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	/*****************************************************************************/
/*TODO*///	// Thunder Blade
/*TODO*///	
/*TODO*///	// after burner hardware
/*TODO*///	static RomLoadPtr rom_thndrbld = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "thnbld.58", 0x000000, 0x20000, 0xe057dd5a )
/*TODO*///		ROM_LOAD_ODD ( "thnbld.63", 0x000000, 0x20000, 0xc6b994b8 )
/*TODO*///		ROM_LOAD_EVEN( "11306.epr", 0x040000, 0x20000, 0x4b95f2b4 )
/*TODO*///		ROM_LOAD_ODD ( "11307.epr", 0x040000, 0x20000, 0x2d6833e4 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "11316.epr", 0x00000, 0x10000, 0x84290dff );
/*TODO*///		ROM_LOAD( "11315.epr", 0x10000, 0x10000, 0x35813088 );
/*TODO*///		ROM_LOAD( "11314.epr", 0x20000, 0x10000, 0xd4f954a9 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "thnbld.105",0x000000, 0x20000, 0xb4a382f7 );
/*TODO*///		ROM_LOAD( "thnbld.101",0x020000, 0x20000, 0x525e2e1d );
/*TODO*///		ROM_LOAD( "thnbld.97", 0x040000, 0x20000, 0x5f2783be );
/*TODO*///		ROM_LOAD( "thnbld.93", 0x060000, 0x20000, 0x90775579 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "11328.epr", 0x080000, 0x20000, 0xda39e89c );
/*TODO*///		ROM_LOAD( "11329.epr", 0x0a0000, 0x20000, 0x31b20257 );
/*TODO*///		ROM_LOAD( "11330.epr", 0x0c0000, 0x20000, 0xaa7c70c5 );
/*TODO*///		ROM_LOAD( "11331.epr", 0x0e0000, 0x20000, 0x3a2c042e );
/*TODO*///	
/*TODO*///		ROM_LOAD( "11324.epr", 0x100000, 0x20000, 0x9742b552 );
/*TODO*///		ROM_LOAD( "11325.epr", 0x120000, 0x20000, 0xb9e98ae9 );
/*TODO*///		ROM_LOAD( "11326.epr", 0x140000, 0x20000, 0x29198403 );
/*TODO*///		ROM_LOAD( "11327.epr", 0x160000, 0x20000, 0xdeae90f1 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "11320.epr", 0x180000, 0x20000, 0xa95c76b8 );
/*TODO*///	//	ROM_LOAD( "11321.epr", 0x1a0000, 0x20000, 0x8e738f58 );
/*TODO*///		ROM_LOAD( "thnbld.98", 0x1a0000, 0x10000, 0xeb4b9e57 );
/*TODO*///		ROM_LOAD( "11322.epr", 0x1c0000, 0x20000, 0x10364d74 );
/*TODO*///		ROM_LOAD( "11323.epr", 0x1e0000, 0x20000, 0x27e40735 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x70000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "thnbld.17",	 0x00000, 0x10000, 0xd37b54a4 );
/*TODO*///		ROM_LOAD( "11317.epr",   0x10000, 0x20000, 0xd4e7ac1f );
/*TODO*///		ROM_LOAD( "11318.epr",   0x30000, 0x20000, 0x70d3f02c );
/*TODO*///		ROM_LOAD( "11319.epr",   0x50000, 0x20000, 0x50d9242e );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU3 );/* 2nd 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "thnbld.20", 0x000000, 0x20000, 0xed988fdb )
/*TODO*///		ROM_LOAD_ODD ( "thnbld.29", 0x000000, 0x20000, 0x12523bc1 )
/*TODO*///		ROM_LOAD_EVEN( "11310.epr", 0x040000, 0x20000, 0x5d9fa02c )
/*TODO*///		ROM_LOAD_ODD ( "11311.epr", 0x040000, 0x20000, 0x483de21b )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_GFX3 );/* ???? */
/*TODO*///		ROM_LOAD( "11313.epr",	 0x00000, 0x10000, 0x6a56c4c3 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	// Thunder Blade Japan
/*TODO*///	
/*TODO*///	// after burner hardware
/*TODO*///	static RomLoadPtr rom_thndrbdj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "11304.epr", 0x000000, 0x20000, 0xa90630ef )
/*TODO*///		ROM_LOAD_ODD ( "11305.epr", 0x000000, 0x20000, 0x9ba3ef61 )
/*TODO*///		ROM_LOAD_EVEN( "11306.epr", 0x040000, 0x20000, 0x4b95f2b4 )
/*TODO*///		ROM_LOAD_ODD ( "11307.epr", 0x040000, 0x20000, 0x2d6833e4 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "11316.epr", 0x00000, 0x10000, 0x84290dff );
/*TODO*///		ROM_LOAD( "11315.epr", 0x10000, 0x10000, 0x35813088 );
/*TODO*///		ROM_LOAD( "11314.epr", 0x20000, 0x10000, 0xd4f954a9 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "11332.epr", 0x000000, 0x20000, 0xdc089ec6 );
/*TODO*///		ROM_LOAD( "11333.epr", 0x020000, 0x20000, 0x05a2333f );
/*TODO*///		ROM_LOAD( "11334.epr", 0x040000, 0x20000, 0x348f91c7 );
/*TODO*///		ROM_LOAD( "11335.epr", 0x060000, 0x20000, 0xf19b3e86 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "11328.epr", 0x080000, 0x20000, 0xda39e89c );
/*TODO*///		ROM_LOAD( "11329.epr", 0x0a0000, 0x20000, 0x31b20257 );
/*TODO*///		ROM_LOAD( "11330.epr", 0x0c0000, 0x20000, 0xaa7c70c5 );
/*TODO*///		ROM_LOAD( "11331.epr", 0x0e0000, 0x20000, 0x3a2c042e );
/*TODO*///	
/*TODO*///		ROM_LOAD( "11324.epr", 0x100000, 0x20000, 0x9742b552 );
/*TODO*///		ROM_LOAD( "11325.epr", 0x120000, 0x20000, 0xb9e98ae9 );
/*TODO*///		ROM_LOAD( "11326.epr", 0x140000, 0x20000, 0x29198403 );
/*TODO*///		ROM_LOAD( "11327.epr", 0x160000, 0x20000, 0xdeae90f1 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "11320.epr", 0x180000, 0x20000, 0xa95c76b8 );
/*TODO*///		ROM_LOAD( "11321.epr", 0x1a0000, 0x20000, 0x8e738f58 );
/*TODO*///		ROM_LOAD( "11322.epr", 0x1c0000, 0x20000, 0x10364d74 );
/*TODO*///		ROM_LOAD( "11323.epr", 0x1e0000, 0x20000, 0x27e40735 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x70000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "11312.epr",   0x00000, 0x10000, 0x3b974ed2 );
/*TODO*///		ROM_LOAD( "11317.epr",   0x10000, 0x20000, 0xd4e7ac1f );
/*TODO*///		ROM_LOAD( "11318.epr",   0x30000, 0x20000, 0x70d3f02c );
/*TODO*///		ROM_LOAD( "11319.epr",   0x50000, 0x20000, 0x50d9242e );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU3 );/* 2nd 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "11308.epr", 0x000000, 0x20000, 0x7956c238 )
/*TODO*///		ROM_LOAD_ODD ( "11309.epr", 0x000000, 0x20000, 0xc887f620 )
/*TODO*///		ROM_LOAD_EVEN( "11310.epr", 0x040000, 0x20000, 0x5d9fa02c )
/*TODO*///		ROM_LOAD_ODD ( "11311.epr", 0x040000, 0x20000, 0x483de21b )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_GFX3 );/* ???? */
/*TODO*///		ROM_LOAD( "11313.epr",	 0x00000, 0x10000, 0x6a56c4c3 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	/*****************************************************************************/
/*TODO*///	// Turbo Outrun
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_toutrun = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU1 );/* 68000 code */
/*TODO*///	// custom cpu 317-0106
/*TODO*///		ROM_LOAD_EVEN( "epr12397.133", 0x000000, 0x10000, 0xe4b57d7d )
/*TODO*///		ROM_LOAD_ODD ( "epr12396.118", 0x000000, 0x10000, 0x5e7115cb )
/*TODO*///		ROM_LOAD_EVEN( "epr12399.132", 0x020000, 0x10000, 0x62c77b1b )
/*TODO*///		ROM_LOAD_ODD ( "epr12398.117", 0x020000, 0x10000, 0x18e34520 )
/*TODO*///		ROM_LOAD_EVEN( "epr12293.131", 0x040000, 0x10000, 0xf4321eea )
/*TODO*///		ROM_LOAD_ODD ( "epr12292.116", 0x040000, 0x10000, 0x51d98af0 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "opr12323.102", 0x00000, 0x10000, 0x4de43a6f );
/*TODO*///		ROM_LOAD( "opr12324.103", 0x10000, 0x10000, 0x24607a55 );
/*TODO*///		ROM_LOAD( "opr12325.104", 0x20000, 0x10000, 0x1405137a );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "opr12307.9",  0x00000, 0x10000, 0x437dcf09 );
/*TODO*///		ROM_LOAD( "opr12308.10", 0x10000, 0x10000, 0x0de70cc2 );
/*TODO*///		ROM_LOAD( "opr12309.11", 0x20000, 0x10000, 0xdeb8c242 );
/*TODO*///		ROM_LOAD( "opr12310.12", 0x30000, 0x10000, 0x45cf157e );
/*TODO*///	
/*TODO*///		ROM_LOAD( "opr12311.13", 0x40000, 0x10000, 0xae2bd639 );
/*TODO*///		ROM_LOAD( "opr12312.14", 0x50000, 0x10000, 0x626000e7 );
/*TODO*///		ROM_LOAD( "opr12313.15", 0x60000, 0x10000, 0x52870c37 );
/*TODO*///		ROM_LOAD( "opr12314.16", 0x70000, 0x10000, 0x40c461ea );
/*TODO*///	
/*TODO*///		ROM_LOAD( "opr12315.17", 0x80000, 0x10000, 0x3ff9a3a3 );
/*TODO*///		ROM_LOAD( "opr12316.18", 0x90000, 0x10000, 0x8a1e6dc8 );
/*TODO*///		ROM_LOAD( "opr12317.19", 0xa0000, 0x10000, 0x77e382d4 );
/*TODO*///		ROM_LOAD( "opr12318.20", 0xb0000, 0x10000, 0xd1afdea9 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "opr12320.22", 0xc0000, 0x10000, 0x7931e446 );
/*TODO*///		ROM_LOAD( "opr12321.23", 0xd0000, 0x10000, 0x830bacd4 );
/*TODO*///		ROM_LOAD( "opr12322.24", 0xe0000, 0x10000, 0x8b812492 );
/*TODO*///		ROM_LOAD( "opr12319.25", 0xf0000, 0x10000, 0xdf23baf9 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x70000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr12300.88",	0x00000, 0x10000, 0xe8ff7011 );
/*TODO*///		ROM_LOAD( "opr12301.66",    0x10000, 0x10000, 0x6e78ad15 );
/*TODO*///		ROM_LOAD( "opr12302.67",    0x20000, 0x10000, 0xe72928af );
/*TODO*///		ROM_LOAD( "opr12303.68",    0x30000, 0x10000, 0x8384205c );
/*TODO*///		ROM_LOAD( "opr12304.69",    0x40000, 0x10000, 0xe1762ac3 );
/*TODO*///		ROM_LOAD( "opr12305.70",    0x50000, 0x10000, 0xba9ce677 );
/*TODO*///		ROM_LOAD( "opr12306.71",    0x60000, 0x10000, 0xe49249fd );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU3 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "opr12295.76", 0x000000, 0x10000, 0xd43a3a84 )
/*TODO*///		ROM_LOAD_ODD ( "opr12294.58", 0x000000, 0x10000, 0x27cdcfd3 )
/*TODO*///		ROM_LOAD_EVEN( "opr12297.75", 0x020000, 0x10000, 0x1d9b5677 )
/*TODO*///		ROM_LOAD_ODD ( "opr12296.57", 0x020000, 0x10000, 0x0a513671 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_GFX3 );/* road */
/*TODO*///		ROM_LOAD_ODD ( "epr12298.11", 0x000000, 0x08000, 0xfc9bc41b )
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_toutruna = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU1 );/* 68000 code */
/*TODO*///	// custom cpu 317-0106
/*TODO*///		ROM_LOAD_EVEN( "epr12410.133", 0x000000, 0x10000, 0xaa74f3e9 )
/*TODO*///		ROM_LOAD_ODD ( "epr12409.118", 0x000000, 0x10000, 0xc11c8ef7 )
/*TODO*///		ROM_LOAD_EVEN( "epr12412.132", 0x020000, 0x10000, 0xb0534647 )
/*TODO*///		ROM_LOAD_ODD ( "epr12411.117", 0x020000, 0x10000, 0x12bb0d83 )
/*TODO*///		ROM_LOAD_EVEN( "epr12293.131", 0x040000, 0x10000, 0xf4321eea )
/*TODO*///		ROM_LOAD_ODD ( "epr12292.116", 0x040000, 0x10000, 0x51d98af0 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "opr12323.102", 0x00000, 0x10000, 0x4de43a6f );
/*TODO*///		ROM_LOAD( "opr12324.103", 0x10000, 0x10000, 0x24607a55 );
/*TODO*///		ROM_LOAD( "opr12325.104", 0x20000, 0x10000, 0x1405137a );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "opr12307.9",  0x00000, 0x10000, 0x437dcf09 );
/*TODO*///		ROM_LOAD( "opr12308.10", 0x10000, 0x10000, 0x0de70cc2 );
/*TODO*///		ROM_LOAD( "opr12309.11", 0x20000, 0x10000, 0xdeb8c242 );
/*TODO*///		ROM_LOAD( "opr12310.12", 0x30000, 0x10000, 0x45cf157e );
/*TODO*///	
/*TODO*///		ROM_LOAD( "opr12311.13", 0x40000, 0x10000, 0xae2bd639 );
/*TODO*///		ROM_LOAD( "opr12312.14", 0x50000, 0x10000, 0x626000e7 );
/*TODO*///		ROM_LOAD( "opr12313.15", 0x60000, 0x10000, 0x52870c37 );
/*TODO*///		ROM_LOAD( "opr12314.16", 0x70000, 0x10000, 0x40c461ea );
/*TODO*///	
/*TODO*///		ROM_LOAD( "opr12315.17", 0x80000, 0x10000, 0x3ff9a3a3 );
/*TODO*///		ROM_LOAD( "opr12316.18", 0x90000, 0x10000, 0x8a1e6dc8 );
/*TODO*///		ROM_LOAD( "opr12317.19", 0xa0000, 0x10000, 0x77e382d4 );
/*TODO*///		ROM_LOAD( "opr12318.20", 0xb0000, 0x10000, 0xd1afdea9 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "opr12320.22", 0xc0000, 0x10000, 0x7931e446 );
/*TODO*///		ROM_LOAD( "opr12321.23", 0xd0000, 0x10000, 0x830bacd4 );
/*TODO*///		ROM_LOAD( "opr12322.24", 0xe0000, 0x10000, 0x8b812492 );
/*TODO*///		ROM_LOAD( "opr12319.25", 0xf0000, 0x10000, 0xdf23baf9 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x70000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr12300.88",	0x00000, 0x10000, 0xe8ff7011 );
/*TODO*///		ROM_LOAD( "opr12301.66",    0x10000, 0x10000, 0x6e78ad15 );
/*TODO*///		ROM_LOAD( "opr12302.67",    0x20000, 0x10000, 0xe72928af );
/*TODO*///		ROM_LOAD( "opr12303.68",    0x30000, 0x10000, 0x8384205c );
/*TODO*///		ROM_LOAD( "opr12304.69",    0x40000, 0x10000, 0xe1762ac3 );
/*TODO*///		ROM_LOAD( "opr12305.70",    0x50000, 0x10000, 0xba9ce677 );
/*TODO*///		ROM_LOAD( "opr12306.71",    0x60000, 0x10000, 0xe49249fd );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU3 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "opr12295.76", 0x000000, 0x10000, 0xd43a3a84 )
/*TODO*///		ROM_LOAD_ODD ( "opr12294.58", 0x000000, 0x10000, 0x27cdcfd3 )
/*TODO*///		ROM_LOAD_EVEN( "opr12297.75", 0x020000, 0x10000, 0x1d9b5677 )
/*TODO*///		ROM_LOAD_ODD ( "opr12296.57", 0x020000, 0x10000, 0x0a513671 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_GFX3 );/* road */
/*TODO*///		ROM_LOAD_ODD ( "epr12298.11", 0x000000, 0x08000, 0xfc9bc41b )
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	/*****************************************************************************/
/*TODO*///	// Excite League
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_exctleag = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "epr11937.a02",0x00000,0x10000, 0x4ebda367 )
/*TODO*///		ROM_LOAD_ODD ( "epr11936.a01",0x00000,0x10000, 0x0863de60 )
/*TODO*///		ROM_LOAD_EVEN( "epr11939.a04",0x20000,0x10000, 0x117dd98f )
/*TODO*///		ROM_LOAD_ODD ( "epr11938.a03",0x20000,0x10000, 0x07c08d47 )
/*TODO*///		ROM_LOAD_EVEN( "epr11941.a06",0x40000,0x10000, 0x4df2d451 )
/*TODO*///		ROM_LOAD_ODD ( "epr11940.a05",0x40000,0x10000, 0xdec83274 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "epr11942.b09",0x00000,0x10000, 0xeb70e827 );
/*TODO*///		ROM_LOAD( "epr11943.b10",0x10000,0x10000, 0xd97c8982 );
/*TODO*///		ROM_LOAD( "epr11944.b11",0x20000,0x10000, 0xa75cae80 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x80000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "epr11950.b01",0x00000,0x10000, 0xaf497849 );
/*TODO*///		ROM_LOAD( "epr11951.b02",0x10000,0x10000, 0xc04fa974 );
/*TODO*///		ROM_LOAD( "epr11952.b03",0x20000,0x10000, 0xe64a9761 );
/*TODO*///		ROM_LOAD( "epr11953.b04",0x30000,0x10000, 0x4cae3999 );
/*TODO*///		ROM_LOAD( "epr11954.b05",0x40000,0x10000, 0x5fa2106c );
/*TODO*///		ROM_LOAD( "epr11955.b06",0x50000,0x10000, 0x86a0c368 );
/*TODO*///		ROM_LOAD( "epr11956.b07",0x60000,0x10000, 0xaff5c2fa );
/*TODO*///		ROM_LOAD( "epr11957.b08",0x70000,0x10000, 0x218f835b );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr11945.a07",0x00000,0x8000, 0xc2a83012 );
/*TODO*///		ROM_LOAD( "epr11140.a08",0x10000,0x8000, 0xb297371b );
/*TODO*///		ROM_LOAD( "epr11141.a09",0x18000,0x8000, 0x19756aa6 );
/*TODO*///		ROM_LOAD( "epr11142.a10",0x20000,0x8000, 0x25d26c66 );
/*TODO*///		ROM_LOAD( "epr11143.a11",0x28000,0x8000, 0x848b7b77 );
/*TODO*///	
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*****************************************************************************/
/*TODO*///	// Super League
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_suprleag = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU1 );/* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "epr11131.a02",0x00000,0x10000, 0x9b78c2cc )
/*TODO*///		ROM_LOAD_ODD ( "epr11130.a01",0x00000,0x10000, 0xe2451676 )
/*TODO*///		ROM_LOAD_EVEN( "epr11133.a04",0x20000,0x10000, 0xeed72f37 )
/*TODO*///		ROM_LOAD_ODD ( "epr11132.a03",0x20000,0x10000, 0xff199325 )
/*TODO*///		ROM_LOAD_EVEN( "epr11135.a06",0x40000,0x10000, 0x3735e0e1 )
/*TODO*///		ROM_LOAD_ODD ( "epr11134.a05",0x40000,0x10000, 0xccd857f5 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "epr11136.b09",0x00000,0x10000, 0xc3860ce4 );
/*TODO*///		ROM_LOAD( "epr11137.b10",0x10000,0x10000, 0x92d96187 );
/*TODO*///		ROM_LOAD( "epr11138.b11",0x20000,0x10000, 0xc01dc773 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x80000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "epr11144.b01",0x00000,0x10000, 0xb31de51c );
/*TODO*///		ROM_LOAD( "epr11145.b02",0x10000,0x10000, 0x4223d2c3 );
/*TODO*///		ROM_LOAD( "epr11146.b03",0x20000,0x10000, 0xbf0359b6 );
/*TODO*///		ROM_LOAD( "epr11147.b04",0x30000,0x10000, 0x3e592772 );
/*TODO*///		ROM_LOAD( "epr11148.b05",0x40000,0x10000, 0x126e1309 );
/*TODO*///		ROM_LOAD( "epr11149.b06",0x50000,0x10000, 0x694d3765 );
/*TODO*///		ROM_LOAD( "epr11150.b07",0x60000,0x10000, 0x9fc0aded );
/*TODO*///		ROM_LOAD( "epr11151.b08",0x70000,0x10000, 0x9de95169 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "epr11139.a07",0x00000,0x08000, 0x9cbd99da );
/*TODO*///		ROM_LOAD( "epr11140.a08",0x10000,0x08000, 0xb297371b );
/*TODO*///		ROM_LOAD( "epr11141.a09",0x18000,0x08000, 0x19756aa6 );
/*TODO*///		ROM_LOAD( "epr11142.a10",0x20000,0x08000, 0x25d26c66 );
/*TODO*///		ROM_LOAD( "epr11143.a11",0x28000,0x08000, 0x848b7b77 );
/*TODO*///	
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/*****************************************************************************/
/*TODO*///	// Action Fighter
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_afighter = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU1 );/* 68000 code */
/*TODO*///	// cpu 317-0018
/*TODO*///		ROM_LOAD_EVEN( "10348",0x00000,0x08000, 0xe51e3012 )
/*TODO*///		ROM_LOAD_ODD ( "10349",0x00000,0x08000, 0x4b434c37 )
/*TODO*///		ROM_LOAD_EVEN( "10350",0x20000,0x08000, 0xf2cd6b3f )
/*TODO*///		ROM_LOAD_ODD ( "10351",0x20000,0x08000, 0xede21d8d )
/*TODO*///		ROM_LOAD_EVEN( "10352",0x40000,0x08000, 0xf8abb143 )
/*TODO*///		ROM_LOAD_ODD ( "10353",0x40000,0x08000, 0x5a757dc9 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "10281",0x00000,0x10000, 0x30e92cda );
/*TODO*///		ROM_LOAD( "10282",0x10000,0x10000, 0xb67b8910 );
/*TODO*///		ROM_LOAD( "10283",0x20000,0x10000, 0xe7dbfd2d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "10285",0x00000,0x08000, 0x98aa3d04 );
/*TODO*///		ROM_LOAD( "10286",0x08000,0x08000, 0x8da050cf );
/*TODO*///		ROM_LOAD( "10287",0x10000,0x08000, 0x7989b74a );
/*TODO*///		ROM_LOAD( "10288",0x18000,0x08000, 0xd3ce551a );
/*TODO*///		ROM_LOAD( "10289",0x20000,0x08000, 0xc59d1b98 );
/*TODO*///		ROM_LOAD( "10290",0x28000,0x08000, 0x39354223 );
/*TODO*///		ROM_LOAD( "10291",0x30000,0x08000, 0x6e4b245c );
/*TODO*///		ROM_LOAD( "10292",0x38000,0x08000, 0xcef289a3 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "10284",0x00000,0x8000, 0x8ff09116 );
/*TODO*///	
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	/*****************************************************************************/
/*TODO*///	// Ryukyu
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_ryukyu = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x100000, REGION_CPU1 );/* 68000 code */
/*TODO*///	// cpu 317-5023
/*TODO*///		ROM_LOAD_EVEN( "13347",0x00000,0x10000, 0x398031fa )
/*TODO*///		ROM_LOAD_ODD ( "13348",0x00000,0x10000, 0x5f0e0c86 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x60000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
/*TODO*///		ROM_LOAD( "13351",0x00000,0x20000, 0xa68a4e6d );
/*TODO*///		ROM_LOAD( "13352",0x20000,0x20000, 0x5e5531e4 );
/*TODO*///		ROM_LOAD( "13353",0x40000,0x20000, 0x6d23dfd8 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000*2, REGION_GFX2 );/* sprites */
/*TODO*///		ROM_LOAD( "13354",0x00000,0x20000, 0xf07aad99 );
/*TODO*///		ROM_LOAD( "13355",0x20000,0x20000, 0x67890019 );
/*TODO*///		ROM_LOAD( "13356",0x30000,0x20000, 0x5498290b );
/*TODO*///		ROM_LOAD( "13357",0x40000,0x20000, 0xf9e7cf03 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x30000, REGION_CPU2 );/* sound CPU */
/*TODO*///		ROM_LOAD( "13349",0x00000,0x08000, 0xb83183f8 );
/*TODO*///		ROM_LOAD( "13350",0x10000,0x20000, 0x3c59a658 );
/*TODO*///	
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************/
/*TODO*///	
/*TODO*///	GAMEX(1986, alexkidd, 0,        alexkidd, alexkidd, alexkidd, ROT0,         "Sega",    "Alex Kidd (set 1)", GAME_NOT_WORKING)
/*TODO*///	public static GameDriver driver_alexkida	   = new GameDriver("1986"	,"alexkida"	,"system16.java"	,rom_alexkida,driver_alexkidd	,machine_driver_alexkidd	,input_ports_alexkidd	,init_alexkidd	,ROT0	,	"Sega",    "Alex Kidd (set 2)")
/*TODO*///	public static GameDriver driver_aliensyn	   = new GameDriver("1987"	,"aliensyn"	,"system16.java"	,rom_aliensyn,null	,machine_driver_aliensyn	,input_ports_aliensyn	,init_aliensyn	,ROT0	,	"Sega",    "Alien Syndrome (set 1)")
/*TODO*///	GAMEX(1987, aliensya, aliensyn, aliensyn, aliensyn, aliensyn, ROT0,         "Sega",    "Alien Syndrome (set 2)", GAME_NOT_WORKING)
/*TODO*///	GAMEX(1987, aliensyj, aliensyn, aliensyn, aliensyn, aliensyn, ROT0,         "Sega",    "Alien Syndrome (Japan)", GAME_NOT_WORKING)
/*TODO*///	GAMEX(1987, aliensyb, aliensyn, aliensyn, aliensyn, aliensyn, ROT0,         "Sega",    "Alien Syndrome (set 3)", GAME_NOT_WORKING)
    public static GameDriver driver_altbeast = new GameDriver("1988", "altbeast", "system16.java", rom_altbeast, null, machine_driver_altbeast, input_ports_altbeast, init_altbeast, ROT0, "Sega", "Altered Beast (Version 1)");
    /*TODO*///	GAMEX(1988, jyuohki,  altbeast, altbeast, altbeast, altbeast, ROT0,         "Sega",    "Jyuohki (Japan)",           GAME_NOT_WORKING)
/*TODO*///	GAMEX(1988, altbeas2, altbeast, altbeas2, altbeast, altbeast, ROT0,         "Sega",    "Altered Beast (Version 2)", GAME_NO_SOUND)
/*TODO*///	GAMEX(1990, astorm,   null,        astorm,   astorm,   astorm,   ROT0_16BIT,   "Sega",    "Alien Storm", GAME_NOT_WORKING)
/*TODO*///	GAMEX(1990, astorm2p, astorm,   astorm,   astorm,   astorm,   ROT0_16BIT,   "Sega",    "Alien Storm (2 Player)", GAME_NOT_WORKING)
/*TODO*///	public static GameDriver driver_astormbl	   = new GameDriver("1990"	,"astormbl"	,"system16.java"	,rom_astormbl,driver_astorm	,machine_driver_astorm	,input_ports_astorm	,init_astorm	,ROT0_16BIT	,	"bootleg", "Alien Storm (bootleg)")
    public static GameDriver driver_atomicp = new GameDriver("1990", "atomicp", "system16.java", rom_atomicp, null, machine_driver_atomicp, input_ports_atomicp, init_atomicp, ROT0, "Philko", "Atomic Point", GAME_NO_SOUND);
    /*TODO*///	public static GameDriver driver_aurail	   = new GameDriver("1990"	,"aurail"	,"system16.java"	,rom_aurail,null	,machine_driver_aurail	,input_ports_aurail	,init_aurail	,ROT0	,	"Sega / Westone", "Aurail (set 1)")
/*TODO*///	public static GameDriver driver_auraila	   = new GameDriver("1990"	,"auraila"	,"system16.java"	,rom_auraila,driver_aurail	,machine_driver_aurail	,input_ports_aurail	,init_auraila	,ROT0	,	"Sega / Westone", "Aurail (set 2)")
/*TODO*///	public static GameDriver driver_bayroute	   = new GameDriver("1989"	,"bayroute"	,"system16.java"	,rom_bayroute,null	,machine_driver_bayroute	,input_ports_bayroute	,init_bayroute	,ROT0	,	"Sunsoft / Sega", "Bay Route (set 1)")
/*TODO*///	GAMEX(1989, bayrouta, bayroute, bayroute, bayroute, bayrouta, ROT0,         "Sunsoft / Sega", "Bay Route (set 2)", GAME_NOT_WORKING)
/*TODO*///	GAMEX(1989, bayrtbl1, bayroute, bayroute, bayroute, bayrtbl1, ROT0,         "bootleg", "Bay Route (bootleg set 1)", GAME_NOT_WORKING)
/*TODO*///	GAMEX(1989, bayrtbl2, bayroute, bayroute, bayroute, bayrtbl1, ROT0,         "bootleg", "Bay Route (bootleg set 2)", GAME_NOT_WORKING)
/*TODO*///	public static GameDriver driver_bodyslam	   = new GameDriver("1986"	,"bodyslam"	,"system16.java"	,rom_bodyslam,null	,machine_driver_bodyslam	,input_ports_bodyslam	,init_bodyslam	,ROT0	,	"Sega",    "Body Slam")
/*TODO*///	public static GameDriver driver_dumpmtmt	   = new GameDriver("1986"	,"dumpmtmt"	,"system16.java"	,rom_dumpmtmt,driver_bodyslam	,machine_driver_bodyslam	,input_ports_bodyslam	,init_bodyslam	,ROT0	,	"Sega",    "Dump Matsumoto (Japan)")
    public static GameDriver driver_dduxbl	   = new GameDriver("1989"	,"dduxbl"	,"system16.java"	,rom_dduxbl,null	,machine_driver_dduxbl	,input_ports_dduxbl	,init_dduxbl	,ROT0	,	"bootleg", "Dynamite Dux (bootleg)");
/*TODO*///	GAMEX(1989, eswat,    null,        eswat,    eswat,    eswat,    ROT0,         "Sega",    "E-Swat", GAME_NOT_WORKING)
/*TODO*///	public static GameDriver driver_eswatbl	   = new GameDriver("1989"	,"eswatbl"	,"system16.java"	,rom_eswatbl,driver_eswat	,machine_driver_eswat	,input_ports_eswat	,init_eswat	,ROT0	,	"bootleg", "E-Swat (bootleg)")
/*TODO*///	public static GameDriver driver_fantzone	   = new GameDriver("1986"	,"fantzone"	,"system16.java"	,rom_fantzone,null	,machine_driver_fantzone	,input_ports_fantzone	,init_fantzone	,ROT0	,	"Sega",    "Fantasy Zone (Japan New Ver.)")
/*TODO*///	public static GameDriver driver_fantzono	   = new GameDriver("1986"	,"fantzono"	,"system16.java"	,rom_fantzono,driver_fantzone	,machine_driver_fantzono	,input_ports_fantzone	,init_fantzone	,ROT0	,	"Sega",    "Fantasy Zone (Old Ver.)")
    public static GameDriver driver_fpoint = new GameDriver("1989", "fpoint", "system16.java", rom_fpoint, null, machine_driver_fpoint, input_ports_fpoint, init_fpoint, ROT0, "Sega", "Flash Point", GAME_NOT_WORKING);
    public static GameDriver driver_fpointbl = new GameDriver("1989", "fpointbl", "system16.java", rom_fpointbl, driver_fpoint, machine_driver_fpoint, input_ports_fpoint, init_fpointbl, ROT0, "bootleg", "Flash Point (bootleg)");
    public static GameDriver driver_goldnaxe = new GameDriver("1989", "goldnaxe", "system16.java", rom_goldnaxe, null, machine_driver_goldnaxe, input_ports_goldnaxe, init_goldnaxe, ROT0, "Sega", "Golden Axe (Version 1)");
    /*TODO*///	GAMEX(1989, goldnaxj, goldnaxe, goldnaxe, goldnaxe, goldnaxe, ROT0,         "Sega",    "Golden Axe (Version 1, Japan)", GAME_NOT_WORKING)
/*TODO*///	GAMEX(1989, goldnabl, goldnaxe, goldnaxe, goldnaxe, goldnabl, ROT0,         "bootleg", "Golden Axe (bootleg)", GAME_NOT_WORKING)
/*TODO*///	public static GameDriver driver_goldnaxa	   = new GameDriver("1989"	,"goldnaxa"	,"system16.java"	,rom_goldnaxa,driver_goldnaxe	,machine_driver_goldnaxa	,input_ports_goldnaxe	,init_goldnaxe	,ROT0	,	"Sega",    "Golden Axe (Version 2)")
/*TODO*///	GAMEX(1989, goldnaxb, goldnaxe, goldnaxa, goldnaxe, goldnaxe, ROT0,         "Sega",    "Golden Axe (Version 2 317-0110)", GAME_NOT_WORKING)
/*TODO*///	GAMEX(1989, goldnaxc, goldnaxe, goldnaxa, goldnaxe, goldnaxe, ROT0,         "Sega",    "Golden Axe (Version 2 317-0122)", GAME_NOT_WORKING)
/*TODO*///	public static GameDriver driver_hwchamp	   = new GameDriver("1987"	,"hwchamp"	,"system16.java"	,rom_hwchamp,null	,machine_driver_hwchamp	,input_ports_hwchamp	,init_hwchamp	,ROT0	,	"Sega",    "Heavyweight Champ")
/*TODO*///	public static GameDriver driver_mjleague	   = new GameDriver("1985"	,"mjleague"	,"system16.java"	,rom_mjleague,null	,machine_driver_mjleague	,input_ports_mjleague	,init_mjleague	,ROT270	,	"Sega",    "Major League")
/*TODO*///	GAMEX(1990, moonwalk, null,        moonwalk, moonwalk, moonwalk, ROT0,         "Sega",    "Moon Walker (Set 1)", GAME_NOT_WORKING)
/*TODO*///	GAMEX(1990, moonwlka, moonwalk, moonwalk, moonwalk, moonwalk, ROT0,         "Sega",    "Moon Walker (Set 2)", GAME_NOT_WORKING)
/*TODO*///	public static GameDriver driver_moonwlkb	   = new GameDriver("1990"	,"moonwlkb"	,"system16.java"	,rom_moonwlkb,driver_moonwalk	,machine_driver_moonwalk	,input_ports_moonwalk	,init_moonwalk	,ROT0	,	"bootleg", "Moon Walker (bootleg)")
/*TODO*///GAMEX(????, passsht,  0,        passsht,  passsht,  passsht,  ROT270,       "Sega",    "Passing Shot (2 Players)", GAME_NOT_WORKING)
/*TODO*///GAME( ????, passshtb, passsht,  passsht,  passsht,  passsht,  ROT270,       "bootleg", "Passing Shot (2 Players) (bootleg)")
/*TODO*///GAMEX(????, passht4b, passsht,  passht4b, passht4b, passht4b, ROT270,       "bootleg", "Passing Shot (4 Players) (bootleg)", GAME_NO_SOUND)	public static GameDriver driver_quartet	   = new GameDriver("1986"	,"quartet"	,"system16.java"	,rom_quartet,null	,machine_driver_quartet	,input_ports_quartet	,init_quartet	,ROT0	,	"Sega",    "Quartet")
/*TODO*///	public static GameDriver driver_quartetj	   = new GameDriver("1986"	,"quartetj"	,"system16.java"	,rom_quartetj,driver_quartet	,machine_driver_quartet	,input_ports_quartet	,init_quartet	,ROT0	,	"Sega",    "Quartet (Japan)")
/*TODO*///	public static GameDriver driver_quartet2	   = new GameDriver("1986"	,"quartet2"	,"system16.java"	,rom_quartet2,driver_quartet	,machine_driver_quartet2	,input_ports_quartet2	,init_quartet2	,ROT0	,	"Sega",    "Quartet II")
/*TODO*///	public static GameDriver driver_riotcity	   = new GameDriver("1991"	,"riotcity"	,"system16.java"	,rom_riotcity,null	,machine_driver_riotcity	,input_ports_riotcity	,init_riotcity	,ROT0	,	"Sega / Westone", "Riot City")
	public static GameDriver driver_sdi	   = new GameDriver("1987"	,"sdi"	,"system16.java"	,rom_sdi,null	,machine_driver_sdi	,input_ports_sdi	,init_sdi	,ROT0	,	"Sega",    "SDI - Strategic Defense Initiative");
/*TODO*///	GAMEX(1987, sdioj,    sdi,      sdi,      sdi,      sdi,      ROT0,         "Sega",    "SDI - Strategic Defense Initiative (Japan)", GAME_NOT_WORKING)
/*TODO*///	public static GameDriver driver_shdancer	   = new GameDriver("1989"	,"shdancer"	,"system16.java"	,rom_shdancer,null	,machine_driver_shdancer	,input_ports_shdancer	,init_shdancer	,ROT0	,	"Sega",    "Shadow Dancer (US)")
/*TODO*///	GAMEX(1989, shdancbl, shdancer, shdancbl, shdancer, shdancbl, ROT0,         "bootleg", "Shadow Dancer (bootleg)", GAME_NOT_WORKING)
/*TODO*///	public static GameDriver driver_shdancrj	   = new GameDriver("1989"	,"shdancrj"	,"system16.java"	,rom_shdancrj,driver_shdancer	,machine_driver_shdancrj	,input_ports_shdancer	,init_shdancrj	,ROT0	,	"Sega",    "Shadow Dancer (Japan)")
    public static GameDriver driver_shinobi = new GameDriver("1987", "shinobi", "system16.java", rom_shinobi, null, machine_driver_shinobi, input_ports_shinobi, init_shinobi, ROT0, "Sega", "Shinobi (set 1)");
    /*TODO*///	GAMEX(1987, shinobib, shinobi,  shinobi,  shinobi,  shinobi,  ROT0,         "Sega",    "Shinobi (set 3)", GAME_NOT_WORKING)
/*TODO*///	GAMEX(1987, shinobia, shinobi,  shinobl,  shinobi,  shinobi,  ROT0,         "Sega",    "Shinobi (set 2)", GAME_NOT_WORKING)
/*TODO*///	public static GameDriver driver_shinobl	   = new GameDriver("1987"	,"shinobl"	,"system16.java"	,rom_shinobl,driver_shinobi	,machine_driver_shinobl	,input_ports_shinobi	,init_shinobi	,ROT0	,	"bootleg", "Shinobi (bootleg)")
    public static GameDriver driver_tetris = new GameDriver("1988", "tetris", "system16.java", rom_tetris, null, machine_driver_tetris, input_ports_tetris, init_tetris, ROT0, "Sega", "Tetris (Sega Set 1)", GAME_NOT_WORKING);
    public static GameDriver driver_tetrisbl = new GameDriver("1988", "tetrisbl", "system16.java", rom_tetrisbl, driver_tetris, machine_driver_tetris, input_ports_tetris, init_tetrisbl, ROT0, "bootleg", "Tetris (Sega bootleg)");
    /*TODO*///	GAMEX(1988, tetrisa,  tetris,   tetris,   tetris,   tetrisbl, ROT0,         "Sega",    "Tetris (Sega Set 2)", GAME_NOT_WORKING)
/*TODO*///	public static GameDriver driver_timscanr	   = new GameDriver("1987"	,"timscanr"	,"system16.java"	,rom_timscanr,null	,machine_driver_timscanr	,input_ports_timscanr	,init_timscanr	,ROT270	,	"Sega",    "Time Scanner")
/*TODO*///	GAME (1994, toryumon, null,        toryumon, toryumon, toryumon, ROT0,         "Sega",    "Toryumon")
/*TODO*///	GAMEX(1989, tturf,    null,        tturf,    tturf,    tturf,    ROT0_16BIT,   "Sega / Sunsoft", "Tough Turf (Japan)", GAME_NO_SOUND)
/*TODO*///	GAMEX(1989, tturfu,   tturf,    tturfu,   tturf,    tturf,    ROT0_16BIT,   "Sega / Sunsoft", "Tough Turf (US)", GAME_NO_SOUND)
/*TODO*///	GAMEX(1989, tturfbl,  tturf,    tturfbl,  tturf,    tturfbl,  ROT0_16BIT,   "bootleg", "Tough Turf (bootleg)", GAME_IMPERFECT_SOUND)
    public static GameDriver driver_wb3 = new GameDriver("1988", "wb3", "system16.java", rom_wb3, null, machine_driver_wb3, input_ports_wb3, init_wb3, ROT0, "Sega / Westone", "Wonder Boy III - Monster Lair (set 1)");
    /*TODO*///	GAMEX(1988, wb3a,     wb3,      wb3,      wb3,      wb3,      ROT0,         "Sega / Westone", "Wonder Boy III - Monster Lair (set 2)", GAME_NOT_WORKING)
/*TODO*///	public static GameDriver driver_wb3bl	   = new GameDriver("1988"	,"wb3bl"	,"system16.java"	,rom_wb3bl,driver_wb3	,machine_driver_wb3bl	,input_ports_wb3	,init_wb3bl	,ROT0	,	"bootleg", "Wonder Boy III - Monster Lair (bootleg)")
/*TODO*///	public static GameDriver driver_wrestwar	   = new GameDriver("1989"	,"wrestwar"	,"system16.java"	,rom_wrestwar,null	,machine_driver_wrestwar	,input_ports_wrestwar	,init_wrestwar	,ROT270_16BIT	,	"Sega",    "Wrestle War")
/*TODO*///	
/*TODO*///	public static GameDriver driver_hangon	   = new GameDriver("1985"	,"hangon"	,"system16.java"	,rom_hangon,null	,machine_driver_hangon	,input_ports_hangon	,init_hangon	,ROT0	,	"Sega",    "Hang-On")
/*TODO*///	public static GameDriver driver_sharrier	   = new GameDriver("1985"	,"sharrier"	,"system16.java"	,rom_sharrier,null	,machine_driver_sharrier	,input_ports_sharrier	,init_sharrier	,ROT0_16BIT	,	"Sega",    "Space Harrier")
/*TODO*///	GAMEX(1992, shangon,  null,        shangon,  shangon,  shangon,  ROT0,         "Sega",    "Super Hang-On", GAME_NOT_WORKING)
/*TODO*///	public static GameDriver driver_shangonb	   = new GameDriver("1992"	,"shangonb"	,"system16.java"	,rom_shangonb,driver_shangon	,machine_driver_shangon	,input_ports_shangon	,init_shangonb	,ROT0	,	"bootleg", "Super Hang-On (bootleg)")
/*TODO*///	public static GameDriver driver_outrun	   = new GameDriver("1986"	,"outrun"	,"system16.java"	,rom_outrun,null	,machine_driver_outrun	,input_ports_outrun	,init_outrun	,ROT0	,	"Sega",    "Out Run (set 1)")
/*TODO*///	public static GameDriver driver_outruna	   = new GameDriver("1986"	,"outruna"	,"system16.java"	,rom_outruna,driver_outrun	,machine_driver_outruna	,input_ports_outrun	,init_outrun	,ROT0	,	"Sega",    "Out Run (set 2)")
/*TODO*///	public static GameDriver driver_outrunb	   = new GameDriver("1986"	,"outrunb"	,"system16.java"	,rom_outrunb,driver_outrun	,machine_driver_outruna	,input_ports_outrun	,init_outrunb	,ROT0	,	"Sega",    "Out Run (set 3)")
/*TODO*///	GAMEX(1985, enduror,  null,        enduror,  enduror,  enduror,  ROT0,         "Sega",    "Enduro Racer", GAME_NOT_WORKING)
/*TODO*///	public static GameDriver driver_endurobl	   = new GameDriver("1985"	,"endurobl"	,"system16.java"	,rom_endurobl,driver_enduror	,machine_driver_enduror	,input_ports_enduror	,init_endurobl	,ROT0	,	"bootleg", "Enduro Racer (bootleg set 1)")
/*TODO*///	public static GameDriver driver_endurob2	   = new GameDriver("1985"	,"endurob2"	,"system16.java"	,rom_endurob2,driver_enduror	,machine_driver_endurob2	,input_ports_enduror	,init_endurob2	,ROT0	,	"bootleg", "Enduro Racer (bootleg set 2)")
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///GAMEX(????, aceattac, 0,        s16dummy, s16dummy, s16dummy, ROT0,         "Sega", "Ace Attacker", GAME_NOT_WORKING)
/*TODO*///GAMEX(????, aburner,  0,        s16dummy, s16dummy, s16dummy, ROT0,         "Sega", "After Burner (Japan)", GAME_NOT_WORKING)
/*TODO*///GAMEX(????, aburner2, 0,        s16dummy, s16dummy, s16dummy, ROT0,         "Sega", "After Burner II", GAME_NOT_WORKING)
/*TODO*///GAMEX(????, bloxeed,  0,        s16dummy, s16dummy, s16dummy, ROT0,         "Sega", "Bloxeed", GAME_NOT_WORKING)
/*TODO*///GAMEX(????, cltchitr, 0,        s16dummy, s16dummy, s16dummy, ROT0,         "Sega", "Clutch Hitter", GAME_NOT_WORKING)
/*TODO*///GAMEX(????, cotton,   0,        s16dummy, s16dummy, s16dummy, ROT0,         "Sega", "Cotton (Japan)", GAME_NOT_WORKING)
/*TODO*///GAMEX(????, cottona,  cotton,   s16dummy, s16dummy, s16dummy, ROT0,         "Sega", "Cotton", GAME_NOT_WORKING)
/*TODO*///GAMEX(????, ddcrew,   0,        s16dummy, s16dummy, s16dummy, ROT0,         "Sega", "DD Crew", GAME_NOT_WORKING)
/*TODO*///GAMEX(????, dunkshot, 0,        s16dummy, s16dummy, s16dummy, ROT0,         "Sega", "Dunk Shot", GAME_NOT_WORKING)
/*TODO*///GAMEX(????, lghost,   0,        s16dummy, s16dummy, s16dummy, ROT0,         "Sega", "Laser Ghost", GAME_NOT_WORKING)
/*TODO*///GAMEX(????, loffire,  0,        s16dummy, s16dummy, s16dummy, ROT0,         "Sega", "Line of Fire", GAME_NOT_WORKING)
/*TODO*///GAMEX(????, mvp,      0,        s16dummy, s16dummy, s16dummy, ROT0,         "Sega", "MVP", GAME_NOT_WORKING)
/*TODO*///GAMEX(????, thndrbld, 0,        s16dummy, s16dummy, s16dummy, ROT0,         "Sega", "Thunder Blade", GAME_NOT_WORKING)
/*TODO*///GAMEX(????, thndrbdj, thndrbld, s16dummy, s16dummy, s16dummy, ROT0,         "Sega", "Thunder Blade (Japan)", GAME_NOT_WORKING)
/*TODO*///GAMEX(????, toutrun,  0,        s16dummy, s16dummy, s16dummy, ROT0,         "Sega", "Turbo Outrun (set 1)", GAME_NOT_WORKING)
/*TODO*///GAMEX(????, toutruna, toutrun,  s16dummy, s16dummy, s16dummy, ROT0,         "Sega", "Turbo Outrun (set 2)", GAME_NOT_WORKING)
/*TODO*///GAMEX(????, exctleag, 0,        s16dummy, s16dummy, s16dummy, ROT0,         "Sega", "Excite League", GAME_NOT_WORKING)
/*TODO*///GAMEX(????, suprleag, 0,        s16dummy, s16dummy, s16dummy, ROT0,         "Sega", "Super League", GAME_NOT_WORKING)
/*TODO*///GAMEX(????, afighter, 0,        s16dummy, s16dummy, s16dummy, ROT0,         "Sega", "Action Fighter", GAME_NOT_WORKING)
/*TODO*///GAMEX(????, ryukyu  , 0,        s16dummy, s16dummy, s16dummy, ROT0,         "Sega", "Ryukyu", GAME_NOT_WORKING)
}
