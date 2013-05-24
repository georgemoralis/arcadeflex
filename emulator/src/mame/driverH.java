/*
This file is part of Arcadeflex.

Arcadeflex is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Arcadeflex is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Arcadeflex.  If not, see <http://www.gnu.org/licenses/>.
 */
package mame;

import static arcadeflex.libc.CopyArray;
import static mame.osdependH.*;
import static mame.memoryH.*;
/**
 *
 * @author george
 */
public class driverH 
{
        //JAVA HELPERS
    	public static abstract interface ReadHandlerPtr { public abstract int handler(int offset); }
	public static abstract interface WriteHandlerPtr { public abstract void handler(int offset, int data); }
	public static abstract interface InitMachinePtr { public abstract void handler(); }
        public static abstract interface InitDriverPtr { public abstract void handler(); }
	public static abstract interface InterruptPtr { public abstract int handler(); }
	public static abstract interface VhConvertColorPromPtr { public abstract void handler(char []palette, char []colortable, char []color_prom); }
	public static abstract interface VhEofCallbackPtr { public abstract int handler(); }
	public static abstract interface VhStartPtr { public abstract int handler(); }
	public static abstract interface VhStopPtr { public abstract void handler(); }
	public static abstract interface VhUpdatePtr { public abstract void handler(osd_bitmap bitmap,int full_refresh); }
	public static abstract interface ShInitPtr { public abstract int handler(String gamename); }
	public static abstract interface ShStartPtr { public abstract int handler(); }
	public static abstract interface ShStopPtr { public abstract void handler(); }
	public static abstract interface ShUpdatePtr { public abstract void handler(); }
	public static abstract interface DecodePtr {  public abstract void handler();}
	public static abstract interface HiscoreLoadPtr { public abstract int handler(); }
	public static abstract interface HiscoreSavePtr { public abstract void handler(); }
        public static abstract interface ConversionPtr{ public abstract int handler(int data);}
        public static abstract interface RomLoadPtr { public abstract void handler();}
        public static abstract interface InputPortPtr { public abstract void handler();}
     
        public static class MachineCPU
        {
		public MachineCPU(int ct, int cc,MemoryReadAddress []mr, MemoryWriteAddress []mw, IOReadPort []pr, IOWritePort []pw, InterruptPtr vb, int vbf,InterruptPtr ti, int tif)
		{
			cpu_type = ct; 
                        cpu_clock =cc; 
			memory_read = mr; 
                        memory_write = mw; 
                        port_read = pr; 
                        port_write = pw; 
                        vblank_interrupt = vb; 
                        vblank_interrupts_per_frame = vbf;
                        timed_interrupt=ti;
                        timed_interrupts_per_second=tif;
		};
		public MachineCPU(int ct, int cc,MemoryReadAddress []mr, MemoryWriteAddress []mw, IOReadPort []pr, IOWritePort []pw, InterruptPtr vb, int vbf)
		{
			cpu_type = ct; 
                        cpu_clock =cc; 
			memory_read = mr; 
                        memory_write = mw; 
                        port_read = pr; 
                        port_write = pw; 
                        vblank_interrupt = vb; 
                        vblank_interrupts_per_frame = vbf;    
		};
		public MachineCPU()
		{ this(0, 0,null, null, null, null, null, 0,null, 0); }

		public static MachineCPU[] create(int n)
		{ 
                    MachineCPU []a = new MachineCPU[n]; 
                    for(int k = 0; k < n; k++) 
                        a[k] = new MachineCPU(); 
                    return a; 
                }           
                public int cpu_type;	/* see #defines below. */
                public int cpu_clock;	/* in Hertz */
                public MemoryReadAddress []memory_read;
		public MemoryWriteAddress []memory_write;
		public IOReadPort []port_read;
		public IOWritePort []port_write;
                public InterruptPtr vblank_interrupt;
                int vblank_interrupts_per_frame;    /* usually 1 */
                /* use this for interrupts which are not tied to vblank 	*/
                /* usually frequency in Hz, but if you need 				*/
                /* greater precision you can give the period in nanoseconds */
                public InterruptPtr timed_interrupt;
                int timed_interrupts_per_second;
                /* pointer to a parameter to pass to the CPU cores reset function */
 /*TODO*///               void *reset_param;
                
      };
    public static final int CPU_DUMMY   = 0;
    public static final int CPU_Z80     = 1;
    public static final int CPU_Z80GB   = 2;
    public static final int CPU_8080    = 3;
    public static final int CPU_8085A   = 4;
    public static final int CPU_M6502   = 5;
    public static final int CPU_M65C02  = 6;
    public static final int CPU_M65SC02 = 7;
    public static final int CPU_M65CE02 = 8;
    public static final int CPU_M6509   = 9;
    public static final int CPU_M6510   =10;
    public static final int CPU_N2A03   =11;
    public static final int CPU_H6280   =12;
    public static final int CPU_I86     =13;
    public static final int CPU_V20     =14;
    public static final int CPU_V30     =15;
    public static final int CPU_V33     =16;
    public static final int CPU_I8035   =17;	/* same as CPU_I8039 */
    public static final int CPU_I8039   =18;
    public static final int CPU_I8048   =19;	/* same as CPU_I8039 */
    public static final int CPU_N7751   =20;	/* same as CPU_I8039 */
    public static final int CPU_M6800   =21;	/* same as CPU_M6802/CPU_M6808 */
    public static final int CPU_M6801   =22;	/* same as CPU_M6803 */
    public static final int CPU_M6802   =23;	/* same as CPU_M6800/CPU_M6808 */
    public static final int CPU_M6803   =24;	/* same as CPU_M6801 */
    public static final int CPU_M6808   =25;	/* same as CPU_M6800/CPU_M6802 */
    public static final int CPU_HD63701 =26;	/* 6808 with some additional opcodes */
    public static final int CPU_NSC8105 =27;	/* same(?) as CPU_M6802(?) with scrambled opcodes. There is at least one new opcode. */
    public static final int CPU_M6805   =28;
    public static final int CPU_M68705  =29;	/* same as CPU_M6805 */
    public static final int CPU_HD63705 =30;	/* M6805 family but larger address space, different stack size */
    public static final int CPU_HD6309  =31;	/* same as CPU_M6809 (actually it's not 100% compatible) */
    public static final int CPU_M6809   =32;
    public static final int CPU_KONAMI  =33;
    public static final int CPU_M68000  =34;
    public static final int CPU_M68010  =35;
    public static final int CPU_M68EC020=36;
    public static final int CPU_M68020  =37;
    public static final int CPU_T11     =38;
    public static final int CPU_S2650   =39;
    public static final int CPU_TMS34010=40;
    public static final int CPU_TMS9900 =41;
    public static final int CPU_TMS9940 =42;
    public static final int CPU_TMS9980 =43;
    public static final int CPU_TMS9985 =44;
    public static final int CPU_TMS9989 =45;
    public static final int CPU_TMS9995 =46;
    public static final int CPU_TMS99105A=47;
    public static final int CPU_TMS99110A=48;
    public static final int CPU_Z8000   =49;
    public static final int CPU_TMS320C10=50;
    public static final int CPU_CCPU    =51;
    public static final int CPU_PDP1    =52;
    public static final int CPU_ADSP2100=53;
    public static final int CPU_COUNT   =54;
    
    /* set this if the CPU is used as a slave for audio. It will not be emulated if */
    /* sound is disabled, therefore speeding up a lot the emulation. */
    public static final int CPU_AUDIO_CPU= 0x8000;

    /* the Z80 can be wired to use 16 bit addressing for I/O ports */
    public static final int CPU_16BIT_PORT= 0x4000;

    public static final int CPU_FLAGS_MASK= 0xff00;

    public static final int MAX_CPU= 8;	/* MAX_CPU is the maximum number of CPUs which cpuintrf.c */
					/* can run at the same time. Currently, 8 is enough. */

    public static final int MAX_SOUND= 5;	/* MAX_SOUND is the maximum number of sound subsystems */
					/* which can run at the same time. Currently, 5 is enough. */      
    

    public static class InputPort
    {
        
    }    
    public static class MachineDriver
    {
        	/*public MachineDriver(MachineCPU []mcp, int fps,int slices, InitMachinePtr im, int sw, int sh, rectangle va, GfxDecodeInfo []gdi, int tc, int ctl, VhConvertColorPromPtr vccp,int vattr, VhInitPtr vi, VhStartPtr vsta, VhStopPtr vsto, VhUpdatePtr vup, char []sa, ShInitPtr si, ShStartPtr ssta, ShStopPtr ssto, ShUpdatePtr sup)
		{
			CopyArray(cpu, mcp); 
                        frames_per_second = fps;
                        cpu_slices_per_frame=slices;
                        init_machine = im;
			screen_width = sw; screen_height = sh; visible_area = va; gfxdecodeinfo = gdi; total_colors = tc; color_table_len = ctl; vh_convert_color_prom = vccp;
			video_attributes=vattr; vh_init = vi; vh_start = vsta; vh_stop = vsto; vh_update = vup;
			samples = sa; sh_init = si; sh_start = ssta; sh_stop = ssto; sh_update = sup;
		}*/
                public MachineDriver() {} //null implementation
        /*partial implementation */
               
                public MachineDriver(MachineCPU []mcp,int fps,int vblank)
                {
                    CopyArray(cpu,mcp);
                    frames_per_second = fps;
                    vblank_duration = vblank;
                }
		/* basic machine hardware */
		public MachineCPU cpu[] = MachineCPU.create(MAX_CPU);
		public int frames_per_second;
                public int vblank_duration;	/* in microseconds - see description below */
                public int cpu_slices_per_frame;	/* for multicpu games. 1 is the minimum, meaning */
								/* that each CPU runs for the whole video frame */
								/* before giving control to the others. The higher */
								/* this setting, the more closely CPUs are interleaved */
								/* and therefore the more accurate the emulation is. */
								/* However, an higher setting also means slower */
								/* performance. */
		public InitMachinePtr init_machine;

		/* video hardware */
		public int screen_width, screen_height;
/*TODO*///			public rectangle visible_area;
/*TODO*///			public GfxDecodeInfo []gfxdecodeinfo;
		public int total_colors;	/* palette is 3*total_colors bytes long */
		public int color_table_len;	/* length in bytes of the color lookup table */
		public VhConvertColorPromPtr vh_init_palette;
                public int video_attributes;

                	
                public VhEofCallbackPtr vh_eof_callback;	/* called every frame after osd_update_video_and_audio() */
									/* This is useful when there are operations that need */
									/* to be performed every frame regardless of frameskip, */
									/* e.g. sprite buffering or collision detection. */
		public VhStartPtr vh_start;
		public VhStopPtr vh_stop;
		public VhUpdatePtr vh_update;

                /* sound hardware */
                public int sound_attributes;
                public int obsolete1;
                public int obsolete2;
                public int obsolete3;
/*TODO*///	struct MachineSound sound[MAX_SOUND];

	/*
	   use this to manage nvram/eeprom/cmos/etc.
	   It is called before the emulation starts and after it ends. Note that it is
	   NOT called when the game is reset, since it is not needed.
	   file == 0, read_or_write == 0 -> first time the game is run, initialize nvram
	   file != 0, read_or_write == 0 -> load nvram from disk
	   file == 0, read_or_write != 0 -> not allowed
	   file != 0, read_or_write != 0 -> save nvram to disk
	 */
/*TODO*///		void (*nvram_handler)(void *file,int read_or_write);
    }    
    

    public static class GameDriver
    {
        //this is used instead of GAME macro
        public GameDriver(String year,String name,String source,RomLoadPtr romload,GameDriver parent,MachineDriver drv,InputPortPtr input,InitDriverPtr init,int monitor,String manufacture,String fullname)
        {
            this.year=year;
            this.source_file=source;
            this.clone_of=parent;
            this.name=name;
            this.description=fullname;
            this.manufacturer=manufacture;
            this.drv=drv;
            //inputports
            this.driver_init=init;
            //rom
            this.flags=monitor;
        }
        public String source_file;	
	public GameDriver clone_of; /* if this is a clone, point to */
				    /* the main version of the game */
	public String name;
	public String description;
	public String year;
	public String manufacturer;
        public MachineDriver drv;
/*TODO*/ //	const struct InputPortTiny *input_ports;
        public InitDriverPtr driver_init;	/* optional function to be called during initialization */
						/* This is called ONCE, unlike Machine->init_machine */
						/* which is called every time the game is reset. */

/*TODO*/ //	const struct RomModule *rom;

        public int flags;	/* orientation and other flags; see defines below */

    } 
    public static final int ORIENTATION_MASK      =   0x0007;
    public static final int ORIENTATION_FLIP_X	  =   0x0001;	/* mirror everything in the X direction */
    public static final int ORIENTATION_FLIP_Y	  =   0x0002;	/* mirror everything in the Y direction */
    public static final int ORIENTATION_SWAP_XY	  =   0x0004;	/* mirror along the top-left/bottom-right diagonal */

    public static final int GAME_NOT_WORKING	  =   0x0008;
    public static final int GAME_WRONG_COLORS	  =   0x0010;	/* colors are totally wrong */
    public static final int GAME_IMPERFECT_COLORS =   0x0020;	/* colors are not 100% accurate, but close */
    public static final int GAME_NO_SOUND	  =   0x0040;	/* sound is missing */
    public static final int GAME_IMPERFECT_SOUND  =   0x0080;	/* sound is known to be wrong */
    public static final int GAME_REQUIRES_16BIT	  =   0x0100;	/* cannot fit in 256 colors */
    public static final int GAME_NO_COCKTAIL	  =   0x0200;	/* screen flip support is missing */
    public static final int NOT_A_DRIVER	  =   0x4000;	/* set by the fake "root" driver_ and by "containers" */
										/* e.g. driver_neogeo. */

    /* monitor parameters to be used with the GAME() macro */
    public static final int ROT0	  = 0x0000;
    public static final int ROT90	  = (ORIENTATION_SWAP_XY|ORIENTATION_FLIP_X);	/* rotate clockwise 90 degrees */
    public static final int ROT180        = (ORIENTATION_FLIP_X|ORIENTATION_FLIP_Y);		/* rotate 180 degrees */
    public static final int ROT270	  = (ORIENTATION_SWAP_XY|ORIENTATION_FLIP_Y);	/* rotate counter-clockwise 90 degrees */
    public static final int ROT0_16BIT    =	(ROT0|GAME_REQUIRES_16BIT);
    public static final int ROT90_16BIT   =	(ROT90|GAME_REQUIRES_16BIT);
    public static final int ROT180_16BIT  =	(ROT180|GAME_REQUIRES_16BIT);
    public static final int ROT270_16BIT  =	(ROT270|GAME_REQUIRES_16BIT);
}
