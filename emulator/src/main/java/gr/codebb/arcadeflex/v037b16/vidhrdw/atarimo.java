package gr.codebb.arcadeflex.v037b16.vidhrdw;

/*##########################################################################

	atarimo.c

	Common motion object management functions for Atari raster games.

##########################################################################*/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 


public class atarimo
{
	
	
/*TODO*///	/*##########################################################################
/*TODO*///		TYPES & STRUCTURES
/*TODO*///	##########################################################################*/
/*TODO*///	
/*TODO*///	/* internal structure containing a word index, shift and mask */
/*TODO*///	struct atarimo_mask
/*TODO*///	{
/*TODO*///		int					word;				/* word index */
/*TODO*///		int					shift;				/* shift amount */
/*TODO*///		int					mask;				/* final mask */
/*TODO*///	};
/*TODO*///	
/*TODO*///	/* internal cache entry */
/*TODO*///	struct atarimo_cache
/*TODO*///	{
/*TODO*///		UINT16				scanline;			/* effective scanline */
/*TODO*///		struct atarimo_entry entry;				/* entry data */
/*TODO*///	};
/*TODO*///	
/*TODO*///	/* internal structure containing the state of the motion objects */
/*TODO*///	struct atarimo_data
/*TODO*///	{
/*TODO*///		int					timerallocated;		/* true if we've allocated the timer */
/*TODO*///		int					gfxchanged;			/* true if the gfx info has changed */
/*TODO*///		struct GfxElement	gfxelement[MAX_GFX_ELEMENTS]; /* local copy of graphics elements */
/*TODO*///	
/*TODO*///		int					linked;				/* are the entries linked? */
/*TODO*///		int					split;				/* are entries split or together? */
/*TODO*///		int					reverse;			/* render in reverse order? */
/*TODO*///		int					swapxy;				/* render in swapped X/Y order? */
/*TODO*///		UINT8				nextneighbor;		/* does the neighbor bit affect the next object? */
/*TODO*///		int					slipshift;			/* log2(pixels_per_SLIP) */
/*TODO*///		int					updatescans;		/* number of scanlines per update */
/*TODO*///	
/*TODO*///		int					entrycount;			/* number of entries per bank */
/*TODO*///		int					entrybits;			/* number of bits needed to represent entrycount */
/*TODO*///		int					bankcount;			/* number of banks */
/*TODO*///	
/*TODO*///		int					tilexshift;			/* bits to shift X coordinate when drawing */
/*TODO*///		int					tileyshift;			/* bits to shift Y coordinate when drawing */
/*TODO*///		int					bitmapwidth;		/* width of the full playfield bitmap */
/*TODO*///		int					bitmapheight;		/* height of the full playfield bitmap */
/*TODO*///		int					bitmapxmask;		/* x coordinate mask for the playfield bitmap */
/*TODO*///		int					bitmapymask;		/* y coordinate mask for the playfield bitmap */
/*TODO*///	
/*TODO*///		int					spriterammask;		/* combined mask when accessing sprite RAM with raw addresses */
/*TODO*///		int					spriteramsize;		/* total size of sprite RAM, in entries */
/*TODO*///		int					sliprammask;		/* combined mask when accessing SLIP RAM with raw addresses */
/*TODO*///		int					slipramsize;		/* total size of SLIP RAM, in entries */
/*TODO*///	
/*TODO*///		int					palettebase;		/* base palette entry */
/*TODO*///		int					maxcolors;			/* maximum number of colors */
/*TODO*///		int					transpen;			/* transparent pen index */
/*TODO*///	
/*TODO*///		int					bank;				/* current bank number */
/*TODO*///		int					xscroll;			/* current x scroll offset */
/*TODO*///		int					yscroll;			/* current y scroll offset */
/*TODO*///	
/*TODO*///		struct atarimo_mask	linkmask;			/* mask for the link */
/*TODO*///		struct atarimo_mask gfxmask;			/* mask for the graphics bank */
/*TODO*///		struct atarimo_mask	codemask;			/* mask for the code index */
/*TODO*///		struct atarimo_mask codehighmask;		/* mask for the upper code index */
/*TODO*///		struct atarimo_mask	colormask;			/* mask for the color */
/*TODO*///		struct atarimo_mask	xposmask;			/* mask for the X position */
/*TODO*///		struct atarimo_mask	yposmask;			/* mask for the Y position */
/*TODO*///		struct atarimo_mask	widthmask;			/* mask for the width, in tiles*/
/*TODO*///		struct atarimo_mask	heightmask;			/* mask for the height, in tiles */
/*TODO*///		struct atarimo_mask	hflipmask;			/* mask for the horizontal flip */
/*TODO*///		struct atarimo_mask	vflipmask;			/* mask for the vertical flip */
/*TODO*///		struct atarimo_mask	prioritymask;		/* mask for the priority */
/*TODO*///		struct atarimo_mask	neighbormask;		/* mask for the neighbor */
/*TODO*///		struct atarimo_mask absolutemask;		/* mask for absolute coordinates */
/*TODO*///	
/*TODO*///		struct atarimo_mask ignoremask;			/* mask for the ignore value */
/*TODO*///		int					ignorevalue;		/* resulting value to indicate "ignore" */
/*TODO*///		atarimo_special_cb	ignorecb;			/* callback routine for ignored entries */
/*TODO*///		int					codehighshift;		/* shift count for the upper code */
/*TODO*///	
/*TODO*///		struct atarimo_entry *spriteram;		/* pointer to sprite RAM */
/*TODO*///		data16_t **			slipram;			/* pointer to the SLIP RAM pointer */
/*TODO*///		UINT16 *			codelookup;			/* lookup table for codes */
/*TODO*///		UINT8 *				colorlookup;		/* lookup table for colors */
/*TODO*///		UINT8 *				gfxlookup;			/* lookup table for graphics */
/*TODO*///	
/*TODO*///		struct atarimo_cache *cache;			/* pointer to the cache data */
/*TODO*///		struct atarimo_cache *endcache;			/* end of the cache */
/*TODO*///		struct atarimo_cache *curcache;			/* current cache entry */
/*TODO*///		struct atarimo_cache *prevcache;		/* previous cache entry */
/*TODO*///	
/*TODO*///		ataripf_overrender_cb overrender0;		/* overrender callback for PF 0 */
/*TODO*///		ataripf_overrender_cb overrender1;		/* overrender callback for PF 1 */
/*TODO*///		struct rectangle	process_clip;		/* (during processing) the clip rectangle */
/*TODO*///		void *				process_param;		/* (during processing) the callback parameter */
/*TODO*///		int					last_xpos;			/* (during processing) the previous X position */
/*TODO*///		int					next_xpos;			/* (during processing) the next X position */
/*TODO*///		int					process_xscroll;	/* (during processing) the X scroll position */
/*TODO*///		int					process_yscroll;	/* (during processing) the Y scroll position */
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	/* callback function for the internal playfield processing mechanism */
/*TODO*///	typedef void (*mo_callback)(struct atarimo_data *pf, const struct atarimo_entry *entry);
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*##########################################################################
/*TODO*///		MACROS
/*TODO*///	##########################################################################*/
/*TODO*///	
/*TODO*///	/* verification macro for void functions */
/*TODO*///	#define VERIFY(cond, msg) if (!(cond)) { logerror(msg); return; }
/*TODO*///	
/*TODO*///	/* verification macro for non-void functions */
/*TODO*///	#define VERIFYRETFREE(cond, msg, ret) if (!(cond)) { logerror(msg); atarimo_free(); return (ret); }
/*TODO*///	
/*TODO*///	
/*TODO*///	/* data extraction */
/*TODO*///	#define EXTRACT_DATA(_input, _mask) (((_input).data[(_mask).word] >> (_mask).shift) & (_mask).mask)
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*##########################################################################
/*TODO*///		GLOBAL VARIABLES
/*TODO*///	##########################################################################*/
/*TODO*///	
/*TODO*///	data16_t *atarimo_0_spriteram;
/*TODO*///	data16_t *atarimo_0_slipram;
/*TODO*///	
/*TODO*///	data16_t *atarimo_1_spriteram;
/*TODO*///	data16_t *atarimo_1_slipram;
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*##########################################################################
/*TODO*///		STATIC VARIABLES
/*TODO*///	##########################################################################*/
/*TODO*///	
/*TODO*///	static struct atarimo_data atarimo[ATARIMO_MAX];
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*##########################################################################
/*TODO*///		STATIC FUNCTION DECLARATIONS
/*TODO*///	##########################################################################*/
/*TODO*///	
/*TODO*///	static void mo_process(struct atarimo_data *mo, mo_callback callback, void *param, const struct rectangle *clip);
/*TODO*///	static void mo_update(struct atarimo_data *mo, int scanline);
/*TODO*///	static void mo_usage_callback(struct atarimo_data *mo, const struct atarimo_entry *entry);
/*TODO*///	static void mo_render_callback(struct atarimo_data *mo, const struct atarimo_entry *entry);
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*##########################################################################
/*TODO*///		INLINE FUNCTIONS
/*TODO*///	##########################################################################*/
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		compute_log: Computes the number of bits necessary to
/*TODO*///		hold a given value. The input must be an even power of
/*TODO*///		two.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	INLINE int compute_log(int value)
/*TODO*///	{
/*TODO*///		int log = 0;
/*TODO*///	
/*TODO*///		if (value == 0)
/*TODO*///			return -1;
/*TODO*///		while (!(value & 1))
/*TODO*///			log++, value >>= 1;
/*TODO*///		if (value != 1)
/*TODO*///			return -1;
/*TODO*///		return log;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		round_to_powerof2: Rounds a number up to the nearest
/*TODO*///		power of 2. Even powers of 2 are rounded up to the
/*TODO*///		next greatest power (e.g., 4 returns 8).
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	INLINE int round_to_powerof2(int value)
/*TODO*///	{
/*TODO*///		int log = 0;
/*TODO*///	
/*TODO*///		if (value == 0)
/*TODO*///			return 1;
/*TODO*///		while ((value >>= 1) != 0)
/*TODO*///			log++;
/*TODO*///		return 1 << (log + 1);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		convert_mask: Converts a 4-word mask into a word index,
/*TODO*///		shift, and adjusted mask. Returns 0 if invalid.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	INLINE int convert_mask(const struct atarimo_entry *input, struct atarimo_mask *result)
/*TODO*///	{
/*TODO*///		int i, temp;
/*TODO*///	
/*TODO*///		/* determine the word and make sure it's only 1 */
/*TODO*///		result.word = -1;
/*TODO*///		for (i = 0; i < 4; i++)
/*TODO*///			if (input.data[i])
/*TODO*///			{
/*TODO*///				if (result.word == -1)
/*TODO*///					result.word = i;
/*TODO*///				else
/*TODO*///					return 0;
/*TODO*///			}
/*TODO*///	
/*TODO*///		/* if all-zero, it's valid */
/*TODO*///		if (result.word == -1)
/*TODO*///		{
/*TODO*///			result.word = result.shift = result.mask = 0;
/*TODO*///			return 1;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* determine the shift and final mask */
/*TODO*///		result.shift = 0;
/*TODO*///		temp = input.data[result.word];
/*TODO*///		while (!(temp & 1))
/*TODO*///		{
/*TODO*///			result.shift++;
/*TODO*///			temp >>= 1;
/*TODO*///		}
/*TODO*///		result.mask = temp;
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*##########################################################################
/*TODO*///		GLOBAL FUNCTIONS
/*TODO*///	##########################################################################*/
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarimo_init: Configures the motion objects using the input
/*TODO*///		description. Allocates all memory necessary and generates
/*TODO*///		the attribute lookup table.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	int atarimo_init(int map, const struct atarimo_desc *desc)
/*TODO*///	{
/*TODO*///		struct GfxElement *gfx = Machine.gfx[desc.gfxindex];
/*TODO*///		struct atarimo_data *mo = &atarimo[map];
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		VERIFYRETFREE(map >= 0 && map < ATARIMO_MAX, "atarimo_init: map out of range", 0)
/*TODO*///	
/*TODO*///		/* determine the masks first */
/*TODO*///		convert_mask(&desc.linkmask,     &mo.linkmask);
/*TODO*///		convert_mask(&desc.gfxmask,      &mo.gfxmask);
/*TODO*///		convert_mask(&desc.codemask,     &mo.codemask);
/*TODO*///		convert_mask(&desc.codehighmask, &mo.codehighmask);
/*TODO*///		convert_mask(&desc.colormask,    &mo.colormask);
/*TODO*///		convert_mask(&desc.xposmask,     &mo.xposmask);
/*TODO*///		convert_mask(&desc.yposmask,     &mo.yposmask);
/*TODO*///		convert_mask(&desc.widthmask,    &mo.widthmask);
/*TODO*///		convert_mask(&desc.heightmask,   &mo.heightmask);
/*TODO*///		convert_mask(&desc.hflipmask,    &mo.hflipmask);
/*TODO*///		convert_mask(&desc.vflipmask,    &mo.vflipmask);
/*TODO*///		convert_mask(&desc.prioritymask, &mo.prioritymask);
/*TODO*///		convert_mask(&desc.neighbormask, &mo.neighbormask);
/*TODO*///		convert_mask(&desc.absolutemask, &mo.absolutemask);
/*TODO*///	
/*TODO*///		/* copy in the basic data */
/*TODO*///		mo.timerallocated = 0;
/*TODO*///		mo.gfxchanged    = 0;
/*TODO*///	
/*TODO*///		mo.linked        = desc.linked;
/*TODO*///		mo.split         = desc.split;
/*TODO*///		mo.reverse       = desc.reverse;
/*TODO*///		mo.swapxy        = desc.swapxy;
/*TODO*///		mo.nextneighbor  = desc.nextneighbor;
/*TODO*///		mo.slipshift     = desc.slipheight ? compute_log(desc.slipheight) : 0;
/*TODO*///		mo.updatescans   = desc.updatescans;
/*TODO*///	
/*TODO*///		mo.entrycount    = round_to_powerof2(mo.linkmask.mask);
/*TODO*///		mo.entrybits     = compute_log(mo.entrycount);
/*TODO*///		mo.bankcount     = desc.banks;
/*TODO*///	
/*TODO*///		mo.tilexshift    = compute_log(gfx.width);
/*TODO*///		mo.tileyshift    = compute_log(gfx.height);
/*TODO*///		mo.bitmapwidth   = round_to_powerof2(mo.xposmask.mask);
/*TODO*///		mo.bitmapheight  = round_to_powerof2(mo.yposmask.mask);
/*TODO*///		mo.bitmapxmask   = mo.bitmapwidth - 1;
/*TODO*///		mo.bitmapymask   = mo.bitmapheight - 1;
/*TODO*///	
/*TODO*///		mo.spriteramsize = mo.bankcount * mo.entrycount;
/*TODO*///		mo.spriterammask = mo.spriteramsize - 1;
/*TODO*///		mo.slipramsize   = mo.bitmapheight >> mo.tileyshift;
/*TODO*///		mo.sliprammask   = mo.slipramsize - 1;
/*TODO*///	
/*TODO*///		mo.palettebase   = desc.palettebase;
/*TODO*///		mo.maxcolors     = desc.maxcolors / gfx.color_granularity;
/*TODO*///		mo.transpen      = desc.transpen;
/*TODO*///	
/*TODO*///		mo.bank          = 0;
/*TODO*///		mo.xscroll       = 0;
/*TODO*///		mo.yscroll       = 0;
/*TODO*///	
/*TODO*///		convert_mask(&desc.ignoremask, &mo.ignoremask);
/*TODO*///		mo.ignorevalue   = desc.ignorevalue;
/*TODO*///		mo.ignorecb      = desc.ignorecb;
/*TODO*///		mo.codehighshift = compute_log(round_to_powerof2(mo.codemask.mask));
/*TODO*///	
/*TODO*///		mo.slipram       = (map == 0) ? &atarimo_0_slipram : &atarimo_1_slipram;
/*TODO*///	
/*TODO*///		/* allocate the priority bitmap */
/*TODO*///		priority_bitmap = bitmap_alloc_depth(Machine.drv.screen_width, Machine.drv.screen_height, 8);
/*TODO*///		VERIFYRETFREE(priority_bitmap, "atarimo_init: out of memory for priority bitmap", 0)
/*TODO*///	
/*TODO*///		/* allocate the spriteram */
/*TODO*///		mo.spriteram = malloc(sizeof(mo.spriteram[0]) * mo.spriteramsize);
/*TODO*///		VERIFYRETFREE(mo.spriteram, "atarimo_init: out of memory for spriteram", 0)
/*TODO*///	
/*TODO*///		/* clear it to zero */
/*TODO*///		memset(mo.spriteram, 0, sizeof(mo.spriteram[0]) * mo.spriteramsize);
/*TODO*///	
/*TODO*///		/* allocate the code lookup */
/*TODO*///		mo.codelookup = malloc(sizeof(mo.codelookup[0]) * round_to_powerof2(mo.codemask.mask));
/*TODO*///		VERIFYRETFREE(mo.codelookup, "atarimo_init: out of memory for code lookup", 0)
/*TODO*///	
/*TODO*///		/* initialize it 1:1 */
/*TODO*///		for (i = 0; i < round_to_powerof2(mo.codemask.mask); i++)
/*TODO*///			mo.codelookup[i] = i;
/*TODO*///	
/*TODO*///		/* allocate the color lookup */
/*TODO*///		mo.colorlookup = malloc(sizeof(mo.colorlookup[0]) * round_to_powerof2(mo.colormask.mask));
/*TODO*///		VERIFYRETFREE(mo.colorlookup, "atarimo_init: out of memory for color lookup", 0)
/*TODO*///	
/*TODO*///		/* initialize it 1:1 */
/*TODO*///		for (i = 0; i < round_to_powerof2(mo.colormask.mask); i++)
/*TODO*///			mo.colorlookup[i] = i;
/*TODO*///	
/*TODO*///		/* allocate the gfx lookup */
/*TODO*///		mo.gfxlookup = malloc(sizeof(mo.gfxlookup[0]) * round_to_powerof2(mo.gfxmask.mask));
/*TODO*///		VERIFYRETFREE(mo.gfxlookup, "atarimo_init: out of memory for gfx lookup", 0)
/*TODO*///	
/*TODO*///		/* initialize it with the gfxindex we were passed in */
/*TODO*///		for (i = 0; i < round_to_powerof2(mo.gfxmask.mask); i++)
/*TODO*///			mo.gfxlookup[i] = desc.gfxindex;
/*TODO*///	
/*TODO*///		/* allocate the cache */
/*TODO*///		mo.cache = malloc(mo.entrycount * Machine.drv.screen_height * sizeof(mo.cache[0]));
/*TODO*///		VERIFYRETFREE(mo.cache, "atarimo_init: out of memory for cache", 0)
/*TODO*///		mo.endcache = mo.cache + mo.entrycount * Machine.drv.screen_height;
/*TODO*///	
/*TODO*///		/* initialize the end/last pointers */
/*TODO*///		mo.curcache = mo.cache;
/*TODO*///		mo.prevcache = NULL;
/*TODO*///	
/*TODO*///		/* initialize the gfx elements */
/*TODO*///		mo.gfxelement[desc.gfxindex] = *Machine.gfx[desc.gfxindex];
/*TODO*///		mo.gfxelement[desc.gfxindex].colortable = &Machine.remapped_colortable[mo.palettebase];
/*TODO*///	
/*TODO*///		logerror("atarimo_init:\n");
/*TODO*///		logerror("  width=%d (shift=%d),  height=%d (shift=%d)\n", gfx.width, mo.tilexshift, gfx.height, mo.tileyshift);
/*TODO*///		logerror("  spriteram mask=%X, size=%d\n", mo.spriterammask, mo.spriteramsize);
/*TODO*///		logerror("  slipram mask=%X, size=%d\n", mo.sliprammask, mo.slipramsize);
/*TODO*///		logerror("  bitmap size=%dx%d\n", mo.bitmapwidth, mo.bitmapheight);
/*TODO*///	
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarimo_free: Frees any memory allocated for motion objects.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void atarimo_free(void)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		/* free the motion object data */
/*TODO*///		for (i = 0; i < ATARIMO_MAX; i++)
/*TODO*///		{
/*TODO*///			struct atarimo_data *mo = &atarimo[i];
/*TODO*///	
/*TODO*///			/* free the priority bitmap */
/*TODO*///			if (priority_bitmap != 0)
/*TODO*///				free(priority_bitmap);
/*TODO*///			priority_bitmap = NULL;
/*TODO*///	
/*TODO*///			/* free the spriteram */
/*TODO*///			if (mo.spriteram)
/*TODO*///				free(mo.spriteram);
/*TODO*///			mo.spriteram = NULL;
/*TODO*///	
/*TODO*///			/* free the codelookup */
/*TODO*///			if (mo.codelookup)
/*TODO*///				free(mo.codelookup);
/*TODO*///			mo.codelookup = NULL;
/*TODO*///	
/*TODO*///			/* free the codelookup */
/*TODO*///			if (mo.codelookup)
/*TODO*///				free(mo.codelookup);
/*TODO*///			mo.codelookup = NULL;
/*TODO*///	
/*TODO*///			/* free the colorlookup */
/*TODO*///			if (mo.colorlookup)
/*TODO*///				free(mo.colorlookup);
/*TODO*///			mo.colorlookup = NULL;
/*TODO*///	
/*TODO*///			/* free the gfxlookup */
/*TODO*///			if (mo.gfxlookup)
/*TODO*///				free(mo.gfxlookup);
/*TODO*///			mo.gfxlookup = NULL;
/*TODO*///	
/*TODO*///			/* free the cache */
/*TODO*///			if (mo.cache)
/*TODO*///				free(mo.cache);
/*TODO*///			mo.cache = NULL;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarimo_get_code_lookup: Returns a pointer to the code
/*TODO*///		lookup table.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	UINT16 *atarimo_get_code_lookup(int map, int *size)
/*TODO*///	{
/*TODO*///		struct atarimo_data *mo = &atarimo[map];
/*TODO*///	
/*TODO*///		if (size != 0)
/*TODO*///			*size = round_to_powerof2(mo.codemask.mask);
/*TODO*///		return mo.codelookup;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarimo_get_code_lookup: Returns a pointer to the code
/*TODO*///		lookup table.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	UINT8 *atarimo_get_color_lookup(int map, int *size)
/*TODO*///	{
/*TODO*///		struct atarimo_data *mo = &atarimo[map];
/*TODO*///	
/*TODO*///		if (size != 0)
/*TODO*///			*size = round_to_powerof2(mo.colormask.mask);
/*TODO*///		return mo.colorlookup;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarimo_get_code_lookup: Returns a pointer to the code
/*TODO*///		lookup table.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	UINT8 *atarimo_get_gfx_lookup(int map, int *size)
/*TODO*///	{
/*TODO*///		struct atarimo_data *mo = &atarimo[map];
/*TODO*///	
/*TODO*///		mo.gfxchanged = 1;
/*TODO*///		if (size != 0)
/*TODO*///			*size = round_to_powerof2(mo.gfxmask.mask);
/*TODO*///		return mo.gfxlookup;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarimo_render: Render the motion objects to the
/*TODO*///		destination bitmap.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void atarimo_render(int map, struct osd_bitmap *bitmap, ataripf_overrender_cb callback1, ataripf_overrender_cb callback2)
/*TODO*///	{
/*TODO*///		struct atarimo_data *mo = &atarimo[map];
/*TODO*///	
/*TODO*///		/* render via the standard render callback */
/*TODO*///		mo.overrender0 = callback1;
/*TODO*///		mo.overrender1 = callback2;
/*TODO*///		mo_process(mo, mo_render_callback, bitmap, NULL);
/*TODO*///	
/*TODO*///		/* set a timer to call the eof function on scanline 0 */
/*TODO*///		if (!mo.timerallocated)
/*TODO*///		{
/*TODO*///			timer_set(cpu_getscanlinetime(0), 0 | (map << 16), mo_scanline_callback);
/*TODO*///			mo.timerallocated = 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarimo_mark_palette: Mark palette entries used in the
/*TODO*///		current set of motion objects.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void atarimo_mark_palette(int map)
/*TODO*///	{
/*TODO*///		struct atarimo_data *mo = &atarimo[map];
/*TODO*///		UINT8 *used_colors = &palette_used_colors[mo.palettebase];
/*TODO*///		UINT32 marked_colors[256];
/*TODO*///		int i, j;
/*TODO*///	
/*TODO*///		/* reset the marked colors */
/*TODO*///		memset(marked_colors, 0, mo.maxcolors * sizeof(UINT32));
/*TODO*///	
/*TODO*///		/* mark the colors used */
/*TODO*///		mo_process(mo, mo_usage_callback, marked_colors, NULL);
/*TODO*///	
/*TODO*///		/* loop over colors */
/*TODO*///		for (i = 0; i < mo.maxcolors; i++)
/*TODO*///		{
/*TODO*///			int usage = marked_colors[i];
/*TODO*///	
/*TODO*///			/* if this entry was marked, loop over bits */
/*TODO*///			if (usage != 0)
/*TODO*///			{
/*TODO*///				for (j = 0; j < 32; j++, usage >>= 1)
/*TODO*///					if ((usage & 1) != 0)
/*TODO*///						used_colors[j] = PALETTE_COLOR_USED;
/*TODO*///				used_colors[mo.transpen] = PALETTE_COLOR_TRANSPARENT;
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* advance by the color granularity of the gfx */
/*TODO*///			used_colors += mo.gfxelement[mo.gfxlookup[0]].color_granularity;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarimo_force_update: Force an update for the given
/*TODO*///		scanline.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void atarimo_force_update(int map, int scanline)
/*TODO*///	{
/*TODO*///		mo_update(&atarimo[map], scanline);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarimo_set_bank: Set the banking value for
/*TODO*///		the motion objects.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void atarimo_set_bank(int map, int bank, int scanline)
/*TODO*///	{
/*TODO*///		struct atarimo_data *mo = &atarimo[map];
/*TODO*///	
/*TODO*///		if (mo.bank != bank)
/*TODO*///		{
/*TODO*///			mo.bank = bank;
/*TODO*///			mo_update(mo, scanline);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarimo_set_palettebase: Set the palette base for
/*TODO*///		the motion objects.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void atarimo_set_palettebase(int map, int base, int scanline)
/*TODO*///	{
/*TODO*///		struct atarimo_data *mo = &atarimo[map];
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		mo.palettebase = base;
/*TODO*///		for (i = 0; i < MAX_GFX_ELEMENTS; i++)
/*TODO*///			mo.gfxelement[i].colortable = &Machine.remapped_colortable[base];
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarimo_set_xscroll: Set the horizontal scroll value for
/*TODO*///		the motion objects.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void atarimo_set_xscroll(int map, int xscroll, int scanline)
/*TODO*///	{
/*TODO*///		struct atarimo_data *mo = &atarimo[map];
/*TODO*///	
/*TODO*///		if (mo.xscroll != xscroll)
/*TODO*///		{
/*TODO*///			mo.xscroll = xscroll;
/*TODO*///			mo_update(mo, scanline);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarimo_set_yscroll: Set the vertical scroll value for
/*TODO*///		the motion objects.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void atarimo_set_yscroll(int map, int yscroll, int scanline)
/*TODO*///	{
/*TODO*///		struct atarimo_data *mo = &atarimo[map];
/*TODO*///	
/*TODO*///		if (mo.yscroll != yscroll)
/*TODO*///		{
/*TODO*///			mo.yscroll = yscroll;
/*TODO*///			mo_update(mo, scanline);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarimo_get_bank: Returns the banking value
/*TODO*///		for the motion objects.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	int atarimo_get_bank(int map)
/*TODO*///	{
/*TODO*///		return atarimo[map].bank;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarimo_get_palettebase: Returns the palette base
/*TODO*///		for the motion objects.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	int atarimo_get_palettebase(int map)
/*TODO*///	{
/*TODO*///		return atarimo[map].palettebase;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarimo_get_xscroll: Returns the horizontal scroll value
/*TODO*///		for the motion objects.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	int atarimo_get_xscroll(int map)
/*TODO*///	{
/*TODO*///		return atarimo[map].xscroll;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarimo_get_yscroll: Returns the vertical scroll value for
/*TODO*///		the motion objects.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	int atarimo_get_yscroll(int map)
/*TODO*///	{
/*TODO*///		return atarimo[map].yscroll;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarimo_0_spriteram_w: Write handler for the spriteram.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( atarimo_0_spriteram_w )
/*TODO*///	{
/*TODO*///		int entry, idx, bank;
/*TODO*///	
/*TODO*///		COMBINE_DATA(&atarimo_0_spriteram[offset]);
/*TODO*///		if (atarimo[0].split)
/*TODO*///		{
/*TODO*///			entry = offset & atarimo[0].linkmask.mask;
/*TODO*///			idx = (offset >> atarimo[0].entrybits) & 3;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			entry = (offset >> 2) & atarimo[0].linkmask.mask;
/*TODO*///			idx = offset & 3;
/*TODO*///		}
/*TODO*///		bank = offset >> (2 + atarimo[0].entrybits);
/*TODO*///		COMBINE_DATA(&atarimo[0].spriteram.read((bank << atarimo[0).entrybits) + entry].data[idx]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarimo_1_spriteram_w: Write handler for the spriteram.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( atarimo_1_spriteram_w )
/*TODO*///	{
/*TODO*///		int entry, idx, bank;
/*TODO*///	
/*TODO*///		COMBINE_DATA(&atarimo_1_spriteram[offset]);
/*TODO*///		if (atarimo[1].split)
/*TODO*///		{
/*TODO*///			entry = offset & atarimo[1].linkmask.mask;
/*TODO*///			idx = (offset >> atarimo[1].entrybits) & 3;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			entry = (offset >> 2) & atarimo[1].linkmask.mask;
/*TODO*///			idx = offset & 3;
/*TODO*///		}
/*TODO*///		bank = offset >> (2 + atarimo[1].entrybits);
/*TODO*///		COMBINE_DATA(&atarimo[1].spriteram.read((bank << atarimo[1).entrybits) + entry].data[idx]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarimo_0_spriteram_expanded_w: Write handler for the
/*TODO*///		expanded form of spriteram.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( atarimo_0_spriteram_expanded_w )
/*TODO*///	{
/*TODO*///		int entry, idx, bank;
/*TODO*///	
/*TODO*///		COMBINE_DATA(&atarimo_0_spriteram[offset]);
/*TODO*///		if (!(offset & 1))
/*TODO*///		{
/*TODO*///			offset >>= 1;
/*TODO*///			if (atarimo[0].split)
/*TODO*///			{
/*TODO*///				entry = offset & atarimo[0].linkmask.mask;
/*TODO*///				idx = (offset >> atarimo[0].entrybits) & 3;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				entry = (offset >> 2) & atarimo[0].linkmask.mask;
/*TODO*///				idx = offset & 3;
/*TODO*///			}
/*TODO*///			bank = offset >> (2 + atarimo[0].entrybits);
/*TODO*///			COMBINE_DATA(&atarimo[0].spriteram.read((bank << atarimo[0).entrybits) + entry].data[idx]);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarimo_0_slipram_w: Write handler for the slipram.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( atarimo_0_slipram_w )
/*TODO*///	{
/*TODO*///		COMBINE_DATA(&atarimo_0_slipram[offset]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarimo_1_slipram_w: Write handler for the slipram.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( atarimo_1_slipram_w )
/*TODO*///	{
/*TODO*///		COMBINE_DATA(&atarimo_1_slipram[offset]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		mo_process: Internal routine that loops over chunks of
/*TODO*///		the playfield with common parameters and processes them
/*TODO*///		via a callback.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void mo_process(struct atarimo_data *mo, mo_callback callback, void *param, const struct rectangle *clip)
/*TODO*///	{
/*TODO*///		struct rectangle finalclip;
/*TODO*///		struct atarimo_cache *base = mo.cache;
/*TODO*///	
/*TODO*///		if (clip != 0)
/*TODO*///			finalclip = *clip;
/*TODO*///		else
/*TODO*///			finalclip = Machine.visible_area;
/*TODO*///	
/*TODO*///		/* if the graphics info has changed, recompute */
/*TODO*///		if (mo.gfxchanged)
/*TODO*///		{
/*TODO*///			int i;
/*TODO*///	
/*TODO*///			mo.gfxchanged = 0;
/*TODO*///			for (i = 0; i < round_to_powerof2(mo.gfxmask.mask); i++)
/*TODO*///			{
/*TODO*///				int idx = mo.gfxlookup[i];
/*TODO*///				mo.gfxelement[idx] = *Machine.gfx[idx];
/*TODO*///				mo.gfxelement[idx].colortable = &Machine.remapped_colortable[mo.palettebase];
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* create a clipping rectangle so that only partial sections are updated at a time */
/*TODO*///		mo.process_clip.min_x = finalclip.min_x;
/*TODO*///		mo.process_clip.max_x = finalclip.max_x;
/*TODO*///		mo.process_param = param;
/*TODO*///		mo.next_xpos = 123456;
/*TODO*///	
/*TODO*///		/* loop over the list until the end */
/*TODO*///		while (base < mo.curcache)
/*TODO*///		{
/*TODO*///			struct atarimo_cache *current, *first, *last;
/*TODO*///			int step;
/*TODO*///	
/*TODO*///			/* set the upper clip bound and a maximum lower bound */
/*TODO*///			mo.process_clip.min_y = base.scanline;
/*TODO*///			mo.process_clip.max_y = 100000;
/*TODO*///	
/*TODO*///			/* import the X and Y scroll values */
/*TODO*///			mo.process_xscroll = base.entry.data[0];
/*TODO*///			mo.process_yscroll = base.entry.data[1];
/*TODO*///			base++;
/*TODO*///	
/*TODO*///			/* look for an entry whose scanline start is different from ours; that's our bottom */
/*TODO*///			for (current = base; current < mo.curcache; current++)
/*TODO*///				if (current.scanline != mo.process_clip.min_y)
/*TODO*///				{
/*TODO*///					mo.process_clip.max_y = current.scanline;
/*TODO*///					break;
/*TODO*///				}
/*TODO*///	
/*TODO*///			/* clip the clipper */
/*TODO*///			if (mo.process_clip.min_y < finalclip.min_y)
/*TODO*///				mo.process_clip.min_y = finalclip.min_y;
/*TODO*///			if (mo.process_clip.max_y > finalclip.max_y)
/*TODO*///				mo.process_clip.max_y = finalclip.max_y;
/*TODO*///	
/*TODO*///			/* set the start and end points */
/*TODO*///			if (mo.reverse)
/*TODO*///			{
/*TODO*///				first = current - 1;
/*TODO*///				last = base - 1;
/*TODO*///				step = -1;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				first = base;
/*TODO*///				last = current;
/*TODO*///				step = 1;
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* update the base */
/*TODO*///			base = current;
/*TODO*///	
/*TODO*///			/* render the mos */
/*TODO*///			for (current = first; current != last; current += step)
/*TODO*///				(*callback)(mo, &current.entry);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		mo_update: Parses the current motion object list, caching
/*TODO*///		all entries.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void mo_update(struct atarimo_data *mo, int scanline)
/*TODO*///	{
/*TODO*///		struct atarimo_cache *current = mo.curcache;
/*TODO*///		struct atarimo_cache *previous = mo.prevcache;
/*TODO*///		struct atarimo_cache *new_previous = current;
/*TODO*///		UINT8 spritevisit[ATARIMO_MAXPERBANK];
/*TODO*///		int match = 0, link;
/*TODO*///	
/*TODO*///		/* skip if the scanline is past the bottom of the screen */
/*TODO*///		if (scanline > Machine.visible_area.max_y)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* if we don't use SLIPs, just recapture from 0 */
/*TODO*///		if (!mo.slipshift)
/*TODO*///			link = 0;
/*TODO*///	
/*TODO*///		/* otherwise, grab the SLIP */
/*TODO*///		else
/*TODO*///		{
/*TODO*///			int slipentry = ((scanline + mo.yscroll) & mo.bitmapymask) >> mo.slipshift;
/*TODO*///			link = ((*mo.slipram)[slipentry] >> mo.linkmask.shift) & mo.linkmask.mask;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* if the last list entries were on the same scanline, overwrite them */
/*TODO*///		if (previous != 0)
/*TODO*///		{
/*TODO*///			if (previous.scanline == scanline)
/*TODO*///				current = new_previous = previous;
/*TODO*///			else
/*TODO*///				match = 1;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* set up the first entry with scroll and banking information */
/*TODO*///		current.scanline = scanline;
/*TODO*///		current.entry.data[0] = mo.xscroll;
/*TODO*///		current.entry.data[1] = mo.yscroll;
/*TODO*///	
/*TODO*///		/* look for a match with the previous entry */
/*TODO*///		if (match != 0)
/*TODO*///		{
/*TODO*///			if (previous.entry.data[0] != current.entry.data[0] ||
/*TODO*///				previous.entry.data[1] != current.entry.data[1])
/*TODO*///				match = 0;
/*TODO*///			previous++;
/*TODO*///		}
/*TODO*///		current++;
/*TODO*///	
/*TODO*///		/* visit all the sprites and copy their data into the display list */
/*TODO*///		memset(spritevisit, 0, mo.entrycount);
/*TODO*///		while (!spritevisit[link])
/*TODO*///		{
/*TODO*///			struct atarimo_entry *modata = &mo.spriteram[link + (mo.bank << mo.entrybits)];
/*TODO*///	
/*TODO*///			/* bounds checking */
/*TODO*///			if (current >= mo.endcache)
/*TODO*///			{
/*TODO*///				logerror("Motion object list exceeded maximum\n");
/*TODO*///				break;
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* start with the scanline */
/*TODO*///			current.scanline = scanline;
/*TODO*///			current.entry = *modata;
/*TODO*///	
/*TODO*///			/* update our match status */
/*TODO*///			if (match != 0)
/*TODO*///			{
/*TODO*///				if (previous.entry.data[0] != current.entry.data[0] ||
/*TODO*///					previous.entry.data[1] != current.entry.data[1] ||
/*TODO*///					previous.entry.data[2] != current.entry.data[2] ||
/*TODO*///					previous.entry.data[3] != current.entry.data[3])
/*TODO*///					match = 0;
/*TODO*///				previous++;
/*TODO*///			}
/*TODO*///			current++;
/*TODO*///	
/*TODO*///			/* link to the next object */
/*TODO*///			spritevisit[link] = 1;
/*TODO*///			if (mo.linked)
/*TODO*///				link = EXTRACT_DATA(modata, mo.linkmask);
/*TODO*///			else
/*TODO*///				link = (link + 1) & mo.linkmask.mask;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* if we didn't match the last set of entries, update the counters */
/*TODO*///		if (!match)
/*TODO*///		{
/*TODO*///			mo.prevcache = new_previous;
/*TODO*///			mo.curcache = current;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		mo_usage_callback: Internal processing callback that
/*TODO*///		marks pens used.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void mo_usage_callback(struct atarimo_data *mo, const struct atarimo_entry *entry)
/*TODO*///	{
/*TODO*///		int gfxindex = mo.gfxlookup[EXTRACT_DATA(entry, mo.gfxmask)];
/*TODO*///		const unsigned int *usage = mo.gfxelement[gfxindex].pen_usage;
/*TODO*///		UINT32 *colormap = mo.process_param;
/*TODO*///		int code = mo.codelookup[EXTRACT_DATA(entry, mo.codemask)] | (EXTRACT_DATA(entry, mo.codehighmask) << mo.codehighshift);
/*TODO*///		int width = EXTRACT_DATA(entry, mo.widthmask) + 1;
/*TODO*///		int height = EXTRACT_DATA(entry, mo.heightmask) + 1;
/*TODO*///		int color = mo.colorlookup[EXTRACT_DATA(entry, mo.colormask)];
/*TODO*///		int tiles = width * height;
/*TODO*///		UINT32 temp = 0;
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		/* is this one to ignore? */
/*TODO*///		if (mo.ignoremask.mask != 0 && EXTRACT_DATA(entry, mo.ignoremask) == mo.ignorevalue)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		for (i = 0; i < tiles; i++)
/*TODO*///			temp |= usage[code++];
/*TODO*///		colormap[color] |= temp;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		mo_render_callback: Internal processing callback that
/*TODO*///		renders to the backing bitmap and then copies the result
/*TODO*///		to the destination.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void mo_render_callback(struct atarimo_data *mo, const struct atarimo_entry *entry)
/*TODO*///	{
/*TODO*///		int gfxindex = mo.gfxlookup[EXTRACT_DATA(entry, mo.gfxmask)];
/*TODO*///		const struct GfxElement *gfx = &mo.gfxelement[gfxindex];
/*TODO*///		const unsigned int *usage = gfx.pen_usage;
/*TODO*///		struct osd_bitmap *bitmap = mo.process_param;
/*TODO*///		struct ataripf_overrender_data overrender_data;
/*TODO*///		UINT32 total_usage = 0;
/*TODO*///		int x, y, sx, sy;
/*TODO*///	
/*TODO*///		/* extract data from the various words */
/*TODO*///		int code = mo.codelookup[EXTRACT_DATA(entry, mo.codemask)] | (EXTRACT_DATA(entry, mo.codehighmask) << mo.codehighshift);
/*TODO*///		int color = mo.colorlookup[EXTRACT_DATA(entry, mo.colormask)];
/*TODO*///		int xpos = EXTRACT_DATA(entry, mo.xposmask);
/*TODO*///		int ypos = -EXTRACT_DATA(entry, mo.yposmask);
/*TODO*///		int hflip = EXTRACT_DATA(entry, mo.hflipmask);
/*TODO*///		int vflip = EXTRACT_DATA(entry, mo.vflipmask);
/*TODO*///		int width = EXTRACT_DATA(entry, mo.widthmask) + 1;
/*TODO*///		int height = EXTRACT_DATA(entry, mo.heightmask) + 1;
/*TODO*///		int xadv, yadv;
/*TODO*///	
/*TODO*///		/* is this one to ignore? */
/*TODO*///		if (mo.ignoremask.mask != 0 && EXTRACT_DATA(entry, mo.ignoremask) == mo.ignorevalue)
/*TODO*///		{
/*TODO*///			if (mo.ignorecb)
/*TODO*///				(*mo.ignorecb)(bitmap, &mo.process_clip, code, color, xpos, ypos);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* add in the scroll positions if we're not in absolute coordinates */
/*TODO*///		if (!EXTRACT_DATA(entry, mo.absolutemask))
/*TODO*///		{
/*TODO*///			xpos -= mo.process_xscroll;
/*TODO*///			ypos -= mo.process_yscroll;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* adjust for height */
/*TODO*///		ypos -= height << mo.tileyshift;
/*TODO*///	
/*TODO*///		/* handle previous hold bits */
/*TODO*///		if (mo.next_xpos != 123456)
/*TODO*///			xpos = mo.next_xpos;
/*TODO*///		mo.next_xpos = 123456;
/*TODO*///	
/*TODO*///		/* check for the hold bit */
/*TODO*///		if (EXTRACT_DATA(entry, mo.neighbormask))
/*TODO*///		{
/*TODO*///			if (!mo.nextneighbor)
/*TODO*///				xpos = mo.last_xpos + gfx.width;
/*TODO*///			else
/*TODO*///				mo.next_xpos = xpos + gfx.width;
/*TODO*///		}
/*TODO*///		mo.last_xpos = xpos;
/*TODO*///	
/*TODO*///		/* adjust the final coordinates */
/*TODO*///		xpos &= mo.bitmapxmask;
/*TODO*///		ypos &= mo.bitmapymask;
/*TODO*///		if (xpos > Machine.visible_area.max_x) xpos -= mo.bitmapwidth;
/*TODO*///		if (ypos > Machine.visible_area.max_y) ypos -= mo.bitmapheight;
/*TODO*///	
/*TODO*///		/* compute the overrendering clip rect */
/*TODO*///		overrender_data.clip.min_x = xpos;
/*TODO*///		overrender_data.clip.min_y = ypos;
/*TODO*///		overrender_data.clip.max_x = xpos + width * gfx.width - 1;
/*TODO*///		overrender_data.clip.max_y = ypos + height * gfx.height - 1;
/*TODO*///	
/*TODO*///		/* adjust for h flip */
/*TODO*///		xadv = gfx.width;
/*TODO*///		if (hflip != 0)
/*TODO*///		{
/*TODO*///			xpos += (width - 1) << mo.tilexshift;
/*TODO*///			xadv = -xadv;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* adjust for v flip */
/*TODO*///		yadv = gfx.height;
/*TODO*///		if (vflip != 0)
/*TODO*///		{
/*TODO*///			ypos += (height - 1) << mo.tileyshift;
/*TODO*///			yadv = -yadv;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* standard order is: loop over Y first, then X */
/*TODO*///		if (!mo.swapxy)
/*TODO*///		{
/*TODO*///			/* loop over the height */
/*TODO*///			for (y = 0, sy = ypos; y < height; y++, sy += yadv)
/*TODO*///			{
/*TODO*///				/* clip the Y coordinate */
/*TODO*///				if (sy <= mo.process_clip.min_y - gfx.height)
/*TODO*///				{
/*TODO*///					code += width;
/*TODO*///					continue;
/*TODO*///				}
/*TODO*///				else if (sy > mo.process_clip.max_y)
/*TODO*///					break;
/*TODO*///	
/*TODO*///				/* loop over the width */
/*TODO*///				for (x = 0, sx = xpos; x < width; x++, sx += xadv, code++)
/*TODO*///				{
/*TODO*///					/* clip the X coordinate */
/*TODO*///					if (sx <= -mo.process_clip.min_x - gfx.width || sx > mo.process_clip.max_x)
/*TODO*///						continue;
/*TODO*///	
/*TODO*///					/* draw the sprite */
/*TODO*///					drawgfx(bitmap, gfx, code, color, hflip, vflip, sx, sy, &mo.process_clip, TRANSPARENCY_PEN, mo.transpen);
/*TODO*///	
/*TODO*///					/* also draw the raw version to the priority bitmap */
/*TODO*///					if (mo.overrender0 || mo.overrender1)
/*TODO*///						drawgfx(priority_bitmap, gfx, code, 0, hflip, vflip, sx, sy, &mo.process_clip, TRANSPARENCY_NONE_RAW, mo.transpen);
/*TODO*///	
/*TODO*///					/* track the total usage */
/*TODO*///					total_usage |= usage[code];
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* alternative order is swapped */
/*TODO*///		else
/*TODO*///		{
/*TODO*///			/* loop over the width */
/*TODO*///			for (x = 0, sx = xpos; x < width; x++, sx += xadv)
/*TODO*///			{
/*TODO*///				/* clip the X coordinate */
/*TODO*///				if (sx <= mo.process_clip.min_x - gfx.width)
/*TODO*///				{
/*TODO*///					code += height;
/*TODO*///					continue;
/*TODO*///				}
/*TODO*///				else if (sx > mo.process_clip.max_x)
/*TODO*///					break;
/*TODO*///	
/*TODO*///				/* loop over the height */
/*TODO*///				for (y = 0, sy = ypos; y < height; y++, sy += yadv, code++)
/*TODO*///				{
/*TODO*///					/* clip the X coordinate */
/*TODO*///					if (sy <= -mo.process_clip.min_y - gfx.height || sy > mo.process_clip.max_y)
/*TODO*///						continue;
/*TODO*///	
/*TODO*///					/* draw the sprite */
/*TODO*///					drawgfx(bitmap, gfx, code, color, hflip, vflip, sx, sy, &mo.process_clip, TRANSPARENCY_PEN, mo.transpen);
/*TODO*///	
/*TODO*///					/* also draw the raw version to the priority bitmap */
/*TODO*///					if (mo.overrender0 || mo.overrender1)
/*TODO*///						drawgfx(priority_bitmap, gfx, code, 0, hflip, vflip, sx, sy, &mo.process_clip, TRANSPARENCY_NONE_RAW, mo.transpen);
/*TODO*///	
/*TODO*///					/* track the total usage */
/*TODO*///					total_usage |= usage[code];
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* handle overrendering */
/*TODO*///		if (mo.overrender0 || mo.overrender1)
/*TODO*///		{
/*TODO*///			/* clip to the display */
/*TODO*///			if (overrender_data.clip.min_x < mo.process_clip.min_x)
/*TODO*///				overrender_data.clip.min_x = mo.process_clip.min_x;
/*TODO*///			else if (overrender_data.clip.min_x > mo.process_clip.max_x)
/*TODO*///				overrender_data.clip.min_x = mo.process_clip.max_x;
/*TODO*///			if (overrender_data.clip.max_x < mo.process_clip.min_x)
/*TODO*///				overrender_data.clip.max_x = mo.process_clip.min_x;
/*TODO*///			else if (overrender_data.clip.max_x > mo.process_clip.max_x)
/*TODO*///				overrender_data.clip.max_x = mo.process_clip.max_x;
/*TODO*///			if (overrender_data.clip.min_y < mo.process_clip.min_y)
/*TODO*///				overrender_data.clip.min_y = mo.process_clip.min_y;
/*TODO*///			else if (overrender_data.clip.min_y > mo.process_clip.max_y)
/*TODO*///				overrender_data.clip.min_y = mo.process_clip.max_y;
/*TODO*///			if (overrender_data.clip.max_y < mo.process_clip.min_y)
/*TODO*///				overrender_data.clip.max_y = mo.process_clip.min_y;
/*TODO*///			else if (overrender_data.clip.max_y > mo.process_clip.max_y)
/*TODO*///				overrender_data.clip.max_y = mo.process_clip.max_y;
/*TODO*///	
/*TODO*///			/* overrender the playfield */
/*TODO*///			overrender_data.bitmap = bitmap;
/*TODO*///			overrender_data.mousage = total_usage;
/*TODO*///			overrender_data.mocolor = color;
/*TODO*///			overrender_data.mopriority = EXTRACT_DATA(entry, mo.prioritymask);
/*TODO*///			if (mo.overrender0)
/*TODO*///				ataripf_overrender(0, mo.overrender0, &overrender_data);
/*TODO*///			if (mo.overrender1)
/*TODO*///				ataripf_overrender(1, mo.overrender1, &overrender_data);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		mo_scanline_callback: This callback is called on SLIP
/*TODO*///		boundaries to update the current set of motion objects.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	public static timer_callback mo_scanline_callback = new timer_callback() { public void handler(int param) 
/*TODO*///	{
/*TODO*///		struct atarimo_data *mo = &atarimo[param >> 16];
/*TODO*///		int scanline = param & 0xffff;
/*TODO*///		int nextscanline = scanline + mo.updatescans;
/*TODO*///	
/*TODO*///		/* if this is scanline 0, reset things */
/*TODO*///		/* also, adjust where we will next break */
/*TODO*///		if (scanline == 0)
/*TODO*///		{
/*TODO*///			mo.curcache = mo.cache;
/*TODO*///			mo.prevcache = NULL;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* do the update */
/*TODO*///		mo_update(mo, scanline);
/*TODO*///	
/*TODO*///		/* don't bother updating in the VBLANK area, just start back at 0 */
/*TODO*///		if (nextscanline > Machine.visible_area.max_y)
/*TODO*///			nextscanline = 0;
/*TODO*///		timer_set(cpu_getscanlinetime(nextscanline), nextscanline | (param & ~0xffff), mo_scanline_callback);
/*TODO*///	} };
}
