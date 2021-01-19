package gr.codebb.arcadeflex.v037b16.vidhrdw;

/*##########################################################################

	atarimo.h
	
	Common motion object management functions for Atari raster games.

##########################################################################*/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 

public class atarimoH
{
/*TODO*///	
/*TODO*///	
/*TODO*///	/*##########################################################################
/*TODO*///		CONSTANTS
/*TODO*///	##########################################################################*/
/*TODO*///	
/*TODO*///	/* maximum number of motion object processors */
/*TODO*///	#define ATARIMO_MAX			2
/*TODO*///	
/*TODO*///	/* maximum objects per bank */
/*TODO*///	#define ATARIMO_MAXPERBANK	1024
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*##########################################################################
/*TODO*///		TYPES & STRUCTURES
/*TODO*///	##########################################################################*/
/*TODO*///	
/*TODO*///	/* callback for special processing */
/*TODO*///	typedef void (*atarimo_special_cb)(struct osd_bitmap *bitmap, struct rectangle *clip, int code, int color, int xpos, int ypos);
/*TODO*///	
/*TODO*///	/* description for a four-word mask */
/*TODO*///	struct atarimo_entry
/*TODO*///	{
/*TODO*///		data16_t			data[4];
/*TODO*///	};
/*TODO*///	
/*TODO*///	/* description of the motion objects */
/*TODO*///	struct atarimo_desc
/*TODO*///	{
/*TODO*///		UINT8				gfxindex;			/* index to which gfx system */
/*TODO*///		UINT8				banks;				/* number of motion object banks */
/*TODO*///		UINT8				linked;				/* are the entries linked? */
/*TODO*///		UINT8				split;				/* are the entries split? */
/*TODO*///		UINT8				reverse;			/* render in reverse order? */
/*TODO*///		UINT8				swapxy;				/* render in swapped X/Y order? */
/*TODO*///		UINT8				nextneighbor;		/* does the neighbor bit affect the next object? */
/*TODO*///		UINT16				slipheight;			/* pixels per SLIP entry (0 for no-slip) */
/*TODO*///		UINT16				updatescans;		/* number of scanlines between MO updates */
/*TODO*///		
/*TODO*///		UINT16				palettebase;		/* base palette entry */
/*TODO*///		UINT16				maxcolors;			/* maximum number of colors */
/*TODO*///		UINT8				transpen;			/* transparent pen index */
/*TODO*///	
/*TODO*///		struct atarimo_entry linkmask;			/* mask for the link */
/*TODO*///		struct atarimo_entry gfxmask;			/* mask for the graphics bank */
/*TODO*///		struct atarimo_entry codemask;			/* mask for the code index */
/*TODO*///		struct atarimo_entry codehighmask;		/* mask for the upper code index */
/*TODO*///		struct atarimo_entry colormask;			/* mask for the color */
/*TODO*///		struct atarimo_entry xposmask;			/* mask for the X position */
/*TODO*///		struct atarimo_entry yposmask;			/* mask for the Y position */
/*TODO*///		struct atarimo_entry widthmask;			/* mask for the width, in tiles*/
/*TODO*///		struct atarimo_entry heightmask;		/* mask for the height, in tiles */
/*TODO*///		struct atarimo_entry hflipmask;			/* mask for the horizontal flip */
/*TODO*///		struct atarimo_entry vflipmask;			/* mask for the vertical flip */
/*TODO*///		struct atarimo_entry prioritymask;		/* mask for the priority */
/*TODO*///		struct atarimo_entry neighbormask;		/* mask for the neighbor */
/*TODO*///		struct atarimo_entry absolutemask;		/* mask for absolute coordinates */
/*TODO*///	
/*TODO*///		struct atarimo_entry ignoremask;		/* mask for the ignore value */
/*TODO*///		data16_t			ignorevalue;		/* resulting value to indicate "ignore" */
/*TODO*///		atarimo_special_cb	ignorecb;			/* callback routine for ignored entries */
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*##########################################################################
/*TODO*///		FUNCTION PROTOTYPES
/*TODO*///	##########################################################################*/
/*TODO*///	
/*TODO*///	/* setup/shutdown */
/*TODO*///	int atarimo_init(int map, const struct atarimo_desc *desc);
/*TODO*///	UINT16 *atarimo_get_code_lookup(int map, int *size);
/*TODO*///	UINT8 *atarimo_get_color_lookup(int map, int *size);
/*TODO*///	UINT8 *atarimo_get_gfx_lookup(int map, int *size);
/*TODO*///	
/*TODO*///	/* core processing */
/*TODO*///	void atarimo_mark_palette(int map);
/*TODO*///	void atarimo_render(int map, struct osd_bitmap *bitmap, ataripf_overrender_cb callback1, ataripf_overrender_cb callback2);
/*TODO*///	void atarimo_force_update(int map, int scanline);
/*TODO*///	
/*TODO*///	/* atrribute setters */
/*TODO*///	void atarimo_set_bank(int map, int bank, int scanline);
/*TODO*///	void atarimo_set_palettebase(int map, int base, int scanline);
/*TODO*///	void atarimo_set_xscroll(int map, int xscroll, int scanline);
/*TODO*///	void atarimo_set_yscroll(int map, int yscroll, int scanline);
/*TODO*///	
/*TODO*///	/* atrribute getters */
/*TODO*///	int atarimo_get_bank(int map);
/*TODO*///	int atarimo_get_palettebase(int map);
/*TODO*///	int atarimo_get_xscroll(int map);
/*TODO*///	int atarimo_get_yscroll(int map);
/*TODO*///	
/*TODO*///	/* write handlers */
/*TODO*///	WRITE16_HANDLER( atarimo_0_spriteram_w );
/*TODO*///	WRITE16_HANDLER( atarimo_0_spriteram_expanded_w );
/*TODO*///	WRITE16_HANDLER( atarimo_0_slipram_w );
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( atarimo_1_spriteram_w );
/*TODO*///	WRITE16_HANDLER( atarimo_1_slipram_w );
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*##########################################################################
/*TODO*///		GLOBAL VARIABLES
/*TODO*///	##########################################################################*/
/*TODO*///	
/*TODO*///	extern data16_t *atarimo_0_spriteram;
/*TODO*///	extern data16_t *atarimo_0_slipram;
/*TODO*///	
/*TODO*///	extern data16_t *atarimo_1_spriteram;
/*TODO*///	extern data16_t *atarimo_1_slipram;
/*TODO*///	
/*TODO*///	
/*TODO*///	#endif
}
