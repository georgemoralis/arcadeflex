/**
 * ported to v0.36
 */
/**
 * Changelog
 * =========
 * 13/12/2022 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.mame;

//mame imports
import arcadeflex.v036.mame.commonH.*;
import arcadeflex.v036.mame.drawgfxH.*;
import arcadeflex.v036.mame.driverH.*;
import arcadeflex.v036.mame.inptportH.*;
import arcadeflex.v036.mame.osdependH.*;
//TODO
import gr.codebb.arcadeflex.common.SubArrays.UShortArray;
import gr.codebb.arcadeflex.v036.platform.libc_old.FILE;

public class mameH {

    public static final int MAX_GFX_ELEMENTS = 32;
    public static final int MAX_MEMORY_REGIONS = 32;

    public static class RunningMachine {

        public char memory_region[][] = new char[MAX_MEMORY_REGIONS][];
        public int memory_region_length[] = new int[MAX_MEMORY_REGIONS];/* some drivers might find this useful */
        public int memory_region_type[] = new int[MAX_MEMORY_REGIONS];

        public GfxElement gfx[] = new GfxElement[MAX_GFX_ELEMENTS];/* graphic sets (chars, sprites) */
        public osd_bitmap scrbitmap;/* bitmap to draw into */
        public char[] pens;/* remapped palette pen numbers. When you write */
 /* directly to a bitmap, never use absolute values, */
 /* use this array to get the pen number. For example, */
 /* if you want to use color #6 in the palette, use */
 /* pens[6] instead of just 6. */
        public char[] game_colortable;/* lookup table used to map gfx pen numbers to color numbers */
        public UShortArray remapped_colortable;/* the above, already remapped through Machine->pens */
        public GameDriver gamedrv;/* contains the definition of the game machine */
        public MachineDriver drv;/* same as gamedrv->drv */
        public int color_depth;/* video color depth: 8 or 16 */
        public int sample_rate;/* the digital audio sample rate; 0 if sound is disabled. */
 /* This is set to a default value, or a value specified by */
 /* the user; osd_init() is allowed to change it to the actual */
 /* sample rate supported by the audio card. */
        public int obsolete;	// was sample_bits;	/* 8 or 16 */
        public GameSamples samples;/* samples loaded from disk */
        public InputPort[] input_ports;/* the input ports definition from the driver */
 /* is copied here and modified (load settings from disk, */
 /* remove cheat commands, and so on) */
        public InputPort[] input_ports_default;/* original input_ports without modifications */
        public int orientation;/* see #defines in driver.h */
        public GfxElement uifont;/* font used by DisplayText() */
        public int uifontwidth, uifontheight;
        public int uixmin, uiymin;
        public int uiwidth, uiheight;
        public int ui_orientation;
    }

    /* The host platform should fill these fields with the preferences specified in the GUI */
 /* or on the commandline. */
    public static class GameOptions {

        public FILE errorlog;
        public FILE record;
        public FILE playback;
        public int mame_debug;
        public int cheat;
        public int gui_host;

        public int samplerate;
        public int use_samples;
        public int use_emulated_ym3812;

        public int color_depth;/* 8 or 16, any other value means auto */
        public int norotate;
        public int ror;
        public int rol;
        public int flipx;
        public int flipy;
        public int beam;
        public int flicker;
        public int translucency;
        public int antialias;
        public int use_artwork;

    }

}
