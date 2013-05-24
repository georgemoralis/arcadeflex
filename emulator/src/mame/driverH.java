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

import static mame.osdependH.*;
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
	public static abstract interface VhInitPtr { public abstract int handler(String gamename); }
	public static abstract interface VhStartPtr { public abstract int handler(); }
	public static abstract interface VhStopPtr { public abstract void handler(); }
	public static abstract interface VhUpdatePtr { public abstract void handler(osd_bitmap bitmap); }
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
     
        
    public static final int MAX_CPU= 8;	/* MAX_CPU is the maximum number of CPUs which cpuintrf.c */
					/* can run at the same time. Currently, 8 is enough. */


    public static final int MAX_SOUND= 5;	/* MAX_SOUND is the maximum number of sound subsystems */
					/* which can run at the same time. Currently, 5 is enough. */      
    public static class InputPort
    {
        
    }    
    public static class MachineDriver
    {
        
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
