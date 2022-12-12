/**
 * ported to v0.36
 */
package arcadeflex.v036.mame;

public class mameH {
/*TODO*///#ifndef MACHINE_H
/*TODO*///#define MACHINE_H
/*TODO*///
/*TODO*///#include <stdio.h>
/*TODO*///#include <string.h>
/*TODO*///#include <stdlib.h>
/*TODO*///
/*TODO*///#ifdef MESS
/*TODO*///#include "mess/mess.h"
/*TODO*///#endif
/*TODO*///
/*TODO*///extern char build_version[];
/*TODO*///extern FILE *errorlog;
/*TODO*///
/*TODO*///#define MAX_GFX_ELEMENTS 32
/*TODO*///#define MAX_MEMORY_REGIONS 32
/*TODO*///
/*TODO*///struct RunningMachine
/*TODO*///{
/*TODO*///	unsigned char *memory_region[MAX_MEMORY_REGIONS];
/*TODO*///	unsigned int memory_region_length[MAX_MEMORY_REGIONS];	/* some drivers might find this useful */
/*TODO*///	int memory_region_type[MAX_MEMORY_REGIONS];
/*TODO*///	struct GfxElement *gfx[MAX_GFX_ELEMENTS];	/* graphic sets (chars, sprites) */
/*TODO*///	struct osd_bitmap *scrbitmap;	/* bitmap to draw into */
/*TODO*///	unsigned short *pens;	/* remapped palette pen numbers. When you write */
/*TODO*///							/* directly to a bitmap, never use absolute values, */
/*TODO*///							/* use this array to get the pen number. For example, */
/*TODO*///							/* if you want to use color #6 in the palette, use */
/*TODO*///							/* pens[6] instead of just 6. */
/*TODO*///	unsigned short *game_colortable;	/* lookup table used to map gfx pen numbers */
/*TODO*///										/* to color numbers */
/*TODO*///	unsigned short *remapped_colortable;	/* the above, already remapped through */
/*TODO*///											/* Machine->pens */
/*TODO*///	const struct GameDriver *gamedrv;	/* contains the definition of the game machine */
/*TODO*///	const struct MachineDriver *drv;	/* same as gamedrv->drv */
/*TODO*///	int color_depth;	/* video color depth: 8 or 16 */
/*TODO*///	int sample_rate;	/* the digital audio sample rate; 0 if sound is disabled. */
/*TODO*///						/* This is set to a default value, or a value specified by */
/*TODO*///						/* the user; osd_init() is allowed to change it to the actual */
/*TODO*///						/* sample rate supported by the audio card. */
/*TODO*///	int obsolete;	// was sample_bits;	/* 8 or 16 */
/*TODO*///	struct GameSamples *samples;	/* samples loaded from disk */
/*TODO*///	struct InputPort *input_ports;	/* the input ports definition from the driver */
/*TODO*///								/* is copied here and modified (load settings from disk, */
/*TODO*///								/* remove cheat commands, and so on) */
/*TODO*///	struct InputPort *input_ports_default; /* original input_ports without modifications */
/*TODO*///	int orientation;	/* see #defines in driver.h */
/*TODO*///	struct GfxElement *uifont;	/* font used by DisplayText() */
/*TODO*///	int uifontwidth,uifontheight;
/*TODO*///	int uixmin,uiymin;
/*TODO*///	int uiwidth,uiheight;
/*TODO*///	int ui_orientation;
/*TODO*///};
/*TODO*///
/*TODO*///#ifdef MESS
/*TODO*///#define MAX_IMAGES	32
/*TODO*////*
/*TODO*/// * This is a filename and it's associated peripheral type
/*TODO*/// * The types are defined in mess.h (IO_...)
/*TODO*/// */
/*TODO*///struct ImageFile {
/*TODO*///	const char *name;
/*TODO*///	int type;
/*TODO*///};
/*TODO*///#endif
/*TODO*///
/*TODO*////* The host platform should fill these fields with the preferences specified in the GUI */
/*TODO*////* or on the commandline. */
/*TODO*///struct GameOptions {
/*TODO*///	FILE *errorlog;
/*TODO*///	void *record;
/*TODO*///	void *playback;
/*TODO*///	int mame_debug;
/*TODO*///	int cheat;
/*TODO*///	int gui_host;
/*TODO*///
/*TODO*///	int samplerate;
/*TODO*///	int use_samples;
/*TODO*///	int use_emulated_ym3812;
/*TODO*///
/*TODO*///	int color_depth;	/* 8 or 16, any other value means auto */
/*TODO*///	int norotate;
/*TODO*///	int ror;
/*TODO*///	int rol;
/*TODO*///	int flipx;
/*TODO*///	int flipy;
/*TODO*///	int beam;
/*TODO*///	int flicker;
/*TODO*///	int translucency;
/*TODO*///	int antialias;
/*TODO*///	int use_artwork;
/*TODO*///
/*TODO*///	#ifdef MESS
/*TODO*///	struct ImageFile image_files[MAX_IMAGES];
/*TODO*///	int image_count;
/*TODO*///	#endif
/*TODO*///};
/*TODO*///
/*TODO*///extern struct GameOptions options;
/*TODO*///extern struct RunningMachine *Machine;
/*TODO*///
/*TODO*///int run_game (int game);
/*TODO*///int updatescreen(void);
/*TODO*////* osd_fopen() must use this to know if high score files can be used */
/*TODO*///int mame_highscore_enabled(void);
/*TODO*///
/*TODO*///#endif
/*TODO*///    
}
