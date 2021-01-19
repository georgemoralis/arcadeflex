/*##########################################################################

	ataripf.c

	Common playfield management functions for Atari raster games.

##########################################################################*/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;

public class ataripf
{
	
	
	
/*TODO*///	/*##########################################################################
/*TODO*///		TYPES & STRUCTURES
/*TODO*///	##########################################################################*/
/*TODO*///	
/*TODO*///	/* internal state structure containing values that can change scanline-by-scanline */
/*TODO*///	struct ataripf_state
/*TODO*///	{
/*TODO*///		int					scanline;			/* scanline where we are valid */
/*TODO*///		int					xscroll;			/* xscroll value */
/*TODO*///		int					yscroll;			/* yscroll value */
/*TODO*///		int					bankbits;			/* bank bits */
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	/* internal variant of the gfxelement that contains extra data */
/*TODO*///	struct ataripf_gfxelement
/*TODO*///	{
/*TODO*///		struct GfxElement		element;
/*TODO*///		int						initialized;
/*TODO*///		struct ataripf_usage *	usage;
/*TODO*///		int						usage_words;
/*TODO*///		int						colorshift;
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	/* internal structure containing the state of a playfield */
/*TODO*///	struct ataripf_data
/*TODO*///	{
/*TODO*///		int					initialized;		/* true if we're initialized */
/*TODO*///		int					timerallocated;		/* true if we've allocated the timer */
/*TODO*///		int					gfxchanged;			/* true if the gfx info has changed */
/*TODO*///	
/*TODO*///		int					colshift;			/* bits to shift X coordinate when looking up in VRAM */
/*TODO*///		int 				rowshift;			/* bits to shift Y coordinate when looking up in VRAM */
/*TODO*///		int					colmask;			/* mask to use when wrapping X coordinate in VRAM */
/*TODO*///		int 				rowmask;			/* mask to use when wrapping Y coordinate in VRAM */
/*TODO*///		int					vrammask;			/* combined mask when accessing VRAM with raw addresses */
/*TODO*///		int					vramsize;			/* total size of VRAM, in entries */
/*TODO*///	
/*TODO*///		int					tilexshift;			/* bits to shift X coordinate when drawing */
/*TODO*///		int					tileyshift;			/* bits to shift Y coordinate when drawing */
/*TODO*///		int					tilewidth;			/* width of a single tile */
/*TODO*///		int					tileheight;			/* height of a single tile */
/*TODO*///		int					bitmapwidth;		/* width of the full playfield bitmap */
/*TODO*///		int					bitmapheight;		/* height of the full playfield bitmap */
/*TODO*///		int					bitmapxmask;		/* x coordinate mask for the playfield bitmap */
/*TODO*///		int					bitmapymask;		/* y coordinate mask for the playfield bitmap */
/*TODO*///	
/*TODO*///		int					palettebase;		/* base palette entry */
/*TODO*///		int					maxcolors;			/* maximum number of colors */
/*TODO*///		int					shadowxor;			/* color XOR for shadow effect (if any) */
/*TODO*///		UINT32				transpens;			/* transparent pen */
/*TODO*///		int					transpen;			/* transparent pen */
/*TODO*///	
/*TODO*///		int					lookupmask;			/* mask for the lookup table */
/*TODO*///	
/*TODO*///		int					latchval;			/* value for latching */
/*TODO*///		int					latchdata;			/* shifted value for latching */
/*TODO*///		int					latchmask;			/* mask for latching */
/*TODO*///	
/*TODO*///		struct osd_bitmap *	bitmap;				/* backing bitmap */
/*TODO*///		UINT32 *			vram;				/* pointer to VRAM */
/*TODO*///		UINT32 *			dirtymap;			/* dirty bitmap */
/*TODO*///		UINT8 *				visitmap;			/* visiting bitmap */
/*TODO*///		UINT32 *			lookup;				/* pointer to lookup table */
/*TODO*///	
/*TODO*///		struct ataripf_state curstate;			/* current state */
/*TODO*///		struct ataripf_state *statelist;		/* list of changed states */
/*TODO*///		int					stateindex;			/* index of the next state */
/*TODO*///	
/*TODO*///		struct rectangle	process_clip;		/* (during processing) the clip rectangle */
/*TODO*///		struct rectangle	process_tiles;		/* (during processing) the tiles rectangle */
/*TODO*///		void *				process_param;		/* (during processing) the callback parameter */
/*TODO*///	
/*TODO*///		struct ataripf_gfxelement gfxelement[MAX_GFX_ELEMENTS]; /* graphics element copies */
/*TODO*///		int 				max_usage_words;	/* maximum words of usage */
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	/* callback function for the internal playfield processing mechanism */
/*TODO*///	typedef void (*pf_callback)(struct ataripf_data *pf, const struct ataripf_state *state);
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
/*TODO*///	#define VERIFYRETFREE(cond, msg, ret) if (!(cond)) { logerror(msg); ataripf_free(); return (ret); }
/*TODO*///	
/*TODO*///	
/*TODO*///	/* accessors for upper/lower halves of a 32-bit value */
/*TODO*///	#if LSB_FIRST
/*TODO*///	#define LOWER_HALF(x) ((data16_t *)&(x))[0]
/*TODO*///	#define UPPER_HALF(x) ((data16_t *)&(x))[1]
/*TODO*///	#else
/*TODO*///	#define LOWER_HALF(x) ((data16_t *)&(x))[1]
/*TODO*///	#define UPPER_HALF(x) ((data16_t *)&(x))[0]
/*TODO*///	#endif
	
	
	
	/*##########################################################################
		GLOBAL VARIABLES
	##########################################################################*/
	
	public static UBytePtr ataripf_0_base;
/*TODO*///	data16_t *ataripf_0_upper;
/*TODO*///	
/*TODO*///	data16_t *ataripf_1_base;
/*TODO*///	
/*TODO*///	data32_t *ataripf_0_base32;
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*##########################################################################
/*TODO*///		STATIC VARIABLES
/*TODO*///	##########################################################################*/
/*TODO*///	
/*TODO*///	static struct ataripf_data ataripf[ATARIPF_MAX];
/*TODO*///	
/*TODO*///	static ataripf_overrender_cb overrender_callback;
/*TODO*///	static struct ataripf_overrender_data overrender_data;
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*##########################################################################
/*TODO*///		STATIC FUNCTION DECLARATIONS
/*TODO*///	##########################################################################*/
/*TODO*///	
/*TODO*///	static void pf_process(struct ataripf_data *pf, pf_callback callback, void *param, const struct rectangle *clip);
/*TODO*///	static void pf_usage_callback_1(struct ataripf_data *pf, const struct ataripf_state *state);
/*TODO*///	static void pf_usage_callback_2(struct ataripf_data *pf, const struct ataripf_state *state);
/*TODO*///	static void pf_render_callback(struct ataripf_data *pf, const struct ataripf_state *state);
/*TODO*///	static void pf_overrender_callback(struct ataripf_data *pf, const struct ataripf_state *state);
/*TODO*///	static void pf_init_gfx(struct ataripf_data *pf, int gfxindex);
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
/*TODO*///		collapse_bits: Moving right-to-left, for each 1 bit in
/*TODO*///		the mask, copy the corresponding bit from the input
/*TODO*///		value into the result, packing the bits along the way.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	INLINE int collapse_bits(int value, int mask)
/*TODO*///	{
/*TODO*///		int testmask, ormask;
/*TODO*///		int result = 0;
/*TODO*///	
/*TODO*///		for (testmask = ormask = 1; testmask != 0; testmask <<= 1)
/*TODO*///			if ((mask & testmask) != 0)
/*TODO*///			{
/*TODO*///				if ((value & testmask) != 0)
/*TODO*///					result |= ormask;
/*TODO*///				ormask <<= 1;
/*TODO*///			}
/*TODO*///		return result;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		pf_update_state: Internal routine that updates the
/*TODO*///		state list of the playfield with the current parameters.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	INLINE void pf_update_state(struct ataripf_data *pf, int scanline)
/*TODO*///	{
/*TODO*///		struct ataripf_state *state = &pf.statelist[pf.stateindex];
/*TODO*///	
/*TODO*///		/* ignore anything after the bottom of the visible screen */
/*TODO*///		if (scanline > Machine.visible_area.max_y)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* ignore anything earlier than the last scanline we entered */
/*TODO*///		if (state[-1].scanline > scanline)
/*TODO*///		{
/*TODO*///			logerror("pf_update_state: Attempted state update on prior scanline (%d vs. %d)\n", scanline, state[-1]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* if this is the same scanline as last time, overwrite it */
/*TODO*///		else if (state[-1].scanline == scanline)
/*TODO*///		{
/*TODO*///			logerror("pf_update_state: scanlines equal, overwriting\n");
/*TODO*///			state--;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* otherwise, move forward one entry */
/*TODO*///		else
/*TODO*///		{
/*TODO*///			logerror("pf_update_state: new entry\n");
/*TODO*///			pf.stateindex++;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* fill in the data */
/*TODO*///		*state = pf.curstate;
/*TODO*///		state.scanline = scanline;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*##########################################################################
/*TODO*///		GLOBAL FUNCTIONS
/*TODO*///	##########################################################################*/
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_blend_gfx: Takes two GFXElements and blends their
/*TODO*///		data together to form one. Then frees the second.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void ataripf_blend_gfx(int gfx0, int gfx1, int mask0, int mask1)
/*TODO*///	{
/*TODO*///		struct GfxElement *gx0 = Machine.gfx[gfx0];
/*TODO*///		struct GfxElement *gx1 = Machine.gfx[gfx1];
/*TODO*///		int c, x, y;
/*TODO*///	
/*TODO*///		/* loop over elements */
/*TODO*///		for (c = 0; c < gx0.total_elements; c++)
/*TODO*///		{
/*TODO*///			UINT8 *c0base = gx0.gfxdata + gx0.char_modulo * c;
/*TODO*///			UINT8 *c1base = gx1.gfxdata + gx1.char_modulo * c;
/*TODO*///			UINT32 usage = 0;
/*TODO*///	
/*TODO*///			/* loop over height */
/*TODO*///			for (y = 0; y < gx0.height; y++)
/*TODO*///			{
/*TODO*///				UINT8 *c0 = c0base, *c1 = c1base;
/*TODO*///	
/*TODO*///				for (x = 0; x < gx0.width; x++, c0++, c1++)
/*TODO*///				{
/*TODO*///					*c0 = (*c0 & mask0) | (*c1 & mask1);
/*TODO*///					usage |= 1 << *c0;
/*TODO*///				}
/*TODO*///				c0base += gx0.line_modulo;
/*TODO*///				c1base += gx1.line_modulo;
/*TODO*///				if (gx0.pen_usage)
/*TODO*///					gx0.pen_usage[c] = usage;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* free the second graphics element */
/*TODO*///		freegfx(gx1);
/*TODO*///		Machine.gfx[gfx1] = NULL;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_init: Configures the playfield using the input
/*TODO*///		description. Allocates all memory necessary and generates
/*TODO*///		the attribute lookup table. If custom_lookup is provided,
/*TODO*///		it is used in place of the generated attribute table.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	int ataripf_init(int map, const struct ataripf_desc *desc)
/*TODO*///	{
/*TODO*///		int lookupcount = round_to_powerof2(desc.tilemask | desc.colormask | desc.hflipmask | desc.vflipmask | desc.prioritymask) >> ATARIPF_LOOKUP_DATABITS;
/*TODO*///		struct GfxElement *gfx = Machine.gfx[desc.gfxindex];
/*TODO*///		struct ataripf_data *pf = &ataripf[map];
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		/* sanity checks */
/*TODO*///		VERIFYRETFREE(map >= 0 && map < ATARIPF_MAX, "ataripf_init: map out of range", 0)
/*TODO*///		VERIFYRETFREE(compute_log(desc.cols) != -1, "ataripf_init: cols must be power of 2", 0)
/*TODO*///		VERIFYRETFREE(compute_log(desc.rows) != -1, "ataripf_init: rows must be power of 2", 0)
/*TODO*///		VERIFYRETFREE(compute_log(desc.xmult) != -1, "ataripf_init: xmult must be power of 2", 0)
/*TODO*///		VERIFYRETFREE(compute_log(desc.ymult) != -1, "ataripf_init: ymult must be power of 2", 0)
/*TODO*///		VERIFYRETFREE((desc.tilemask & ATARIPF_LOOKUP_DATAMASK) == ATARIPF_LOOKUP_DATAMASK, "ataripf_init: low bits of tilemask must be 0xff", 0)
/*TODO*///	
/*TODO*///		/* copy in the basic data */
/*TODO*///		pf.initialized  = 0;
/*TODO*///		pf.timerallocated = 0;
/*TODO*///		pf.gfxchanged   = 0;
/*TODO*///	
/*TODO*///		pf.colshift     = compute_log(desc.xmult);
/*TODO*///		pf.rowshift     = compute_log(desc.ymult);
/*TODO*///		pf.colmask      = desc.cols - 1;
/*TODO*///		pf.rowmask      = desc.rows - 1;
/*TODO*///		pf.vrammask     = (pf.colmask << pf.colshift) | (pf.rowmask << pf.rowshift);
/*TODO*///		pf.vramsize     = round_to_powerof2(pf.vrammask);
/*TODO*///	
/*TODO*///		pf.tilexshift   = compute_log(gfx.width);
/*TODO*///		pf.tileyshift   = compute_log(gfx.height);
/*TODO*///		pf.tilewidth    = gfx.width;
/*TODO*///		pf.tileheight   = gfx.height;
/*TODO*///		pf.bitmapwidth  = desc.cols * gfx.width;
/*TODO*///		pf.bitmapheight = desc.rows * gfx.height;
/*TODO*///		pf.bitmapxmask  = pf.bitmapwidth - 1;
/*TODO*///		pf.bitmapymask  = pf.bitmapheight - 1;
/*TODO*///	
/*TODO*///		pf.palettebase  = desc.palettebase;
/*TODO*///		pf.maxcolors    = desc.maxcolors / ATARIPF_BASE_GRANULARITY;
/*TODO*///		pf.shadowxor    = desc.shadowxor;
/*TODO*///		pf.transpens    = desc.transpens;
/*TODO*///		pf.transpen     = desc.transpens ? compute_log(desc.transpens) : -1;
/*TODO*///	
/*TODO*///		pf.lookupmask   = lookupcount - 1;
/*TODO*///	
/*TODO*///		pf.latchval     = 0;
/*TODO*///		pf.latchdata    = -1;
/*TODO*///		pf.latchmask    = desc.latchmask;
/*TODO*///	
/*TODO*///		/* allocate the backing bitmap */
/*TODO*///		pf.bitmap = bitmap_alloc(pf.bitmapwidth, pf.bitmapheight);
/*TODO*///		VERIFYRETFREE(pf.bitmap, "ataripf_init: out of memory for bitmap", 0)
/*TODO*///	
/*TODO*///		/* allocate the vram */
/*TODO*///		pf.vram = malloc(sizeof(pf.vram[0]) * pf.vramsize);
/*TODO*///		VERIFYRETFREE(pf.vram, "ataripf_init: out of memory for vram", 0)
/*TODO*///	
/*TODO*///		/* clear it to zero */
/*TODO*///		memset(pf.vram, 0, sizeof(pf.vram[0]) * pf.vramsize);
/*TODO*///	
/*TODO*///		/* allocate the dirty map */
/*TODO*///		pf.dirtymap = malloc(sizeof(pf.dirtymap[0]) * pf.vramsize);
/*TODO*///		VERIFYRETFREE(pf.dirtymap, "ataripf_init: out of memory for dirtymap", 0)
/*TODO*///	
/*TODO*///		/* mark everything dirty */
/*TODO*///		memset(pf.dirtymap, -1, sizeof(pf.dirtymap[0]) * pf.vramsize);
/*TODO*///	
/*TODO*///		/* allocate the visitation map */
/*TODO*///		pf.visitmap = malloc(sizeof(pf.visitmap[0]) * pf.vramsize);
/*TODO*///		VERIFYRETFREE(pf.visitmap, "ataripf_init: out of memory for visitmap", 0)
/*TODO*///	
/*TODO*///		/* mark everything non-visited */
/*TODO*///		memset(pf.visitmap, 0, sizeof(pf.visitmap[0]) * pf.vramsize);
/*TODO*///	
/*TODO*///		/* allocate the attribute lookup */
/*TODO*///		pf.lookup = malloc(lookupcount * sizeof(pf.lookup[0]));
/*TODO*///		VERIFYRETFREE(pf.lookup, "ataripf_init: out of memory for lookup", 0)
/*TODO*///	
/*TODO*///		/* fill in the attribute lookup */
/*TODO*///		for (i = 0; i < lookupcount; i++)
/*TODO*///		{
/*TODO*///			int value    = (i << ATARIPF_LOOKUP_DATABITS);
/*TODO*///			int tile     = collapse_bits(value, desc.tilemask);
/*TODO*///			int color    = collapse_bits(value, desc.colormask);
/*TODO*///			int hflip    = collapse_bits(value, desc.hflipmask);
/*TODO*///			int vflip    = collapse_bits(value, desc.vflipmask);
/*TODO*///			int priority = collapse_bits(value, desc.prioritymask);
/*TODO*///	
/*TODO*///			pf.lookup[i] = ATARIPF_LOOKUP_ENTRY(desc.gfxindex, tile, color, hflip, vflip, priority);
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* compute the extended usage map */
/*TODO*///		pf_init_gfx(pf, desc.gfxindex);
/*TODO*///		VERIFYRETFREE(pf.gfxelement[desc.gfxindex].initialized, "ataripf_init: out of memory for extra usage map", 0)
/*TODO*///	
/*TODO*///		/* allocate the state list */
/*TODO*///		pf.statelist = malloc(pf.bitmapheight * sizeof(pf.statelist[0]));
/*TODO*///		VERIFYRETFREE(pf.statelist, "ataripf_init: out of memory for extra state list", 0)
/*TODO*///	
/*TODO*///		/* reset the state list */
/*TODO*///		memset(&pf.curstate, 0, sizeof(pf.curstate));
/*TODO*///		pf.statelist[0] = pf.curstate;
/*TODO*///		pf.stateindex = 1;
/*TODO*///	
/*TODO*///		pf.initialized = 1;
/*TODO*///	
/*TODO*///		logerror("ataripf_init:\n");
/*TODO*///		logerror("  width=%d (shift=%d),  height=%d (shift=%d)\n", gfx.width, pf.tilexshift, gfx.height, pf.tileyshift);
/*TODO*///		logerror("  cols=%d  (mask=%X),   rows=%d   (mask=%X)\n", desc.cols, pf.colmask, desc.rows, pf.rowmask);
/*TODO*///		logerror("  xmult=%d (shift=%d),  ymult=%d  (shift=%d)\n", desc.xmult, pf.colshift, desc.ymult, pf.rowshift);
/*TODO*///		logerror("  VRAM mask=%X,  dirtymap size=%d\n", pf.vrammask, pf.vramsize);
/*TODO*///		logerror("  bitmap size=%dx%d\n", pf.bitmapwidth, pf.bitmapheight);
/*TODO*///	
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_free: Frees any memory allocated for any playfield.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void ataripf_free(void)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		/* free the playfield data */
/*TODO*///		for (i = 0; i < ATARIPF_MAX; i++)
/*TODO*///		{
/*TODO*///			struct ataripf_data *pf = &ataripf[i];
/*TODO*///	
/*TODO*///			/* free the backing bitmap */
/*TODO*///			if (pf.bitmap)
/*TODO*///				bitmap_free(pf.bitmap);
/*TODO*///			pf.bitmap = NULL;
/*TODO*///	
/*TODO*///			/* free the vram */
/*TODO*///			if (pf.vram)
/*TODO*///				free(pf.vram);
/*TODO*///			pf.vram = NULL;
/*TODO*///	
/*TODO*///			/* free the dirty map */
/*TODO*///			if (pf.dirtymap)
/*TODO*///				free(pf.dirtymap);
/*TODO*///			pf.dirtymap = NULL;
/*TODO*///	
/*TODO*///			/* free the visitation map */
/*TODO*///			if (pf.visitmap)
/*TODO*///				free(pf.visitmap);
/*TODO*///			pf.visitmap = NULL;
/*TODO*///	
/*TODO*///			/* free the attribute lookup */
/*TODO*///			if (pf.lookup)
/*TODO*///				free(pf.lookup);
/*TODO*///			pf.lookup = NULL;
/*TODO*///	
/*TODO*///			/* free the state list */
/*TODO*///			if (pf.statelist)
/*TODO*///				free(pf.statelist);
/*TODO*///			pf.statelist = NULL;
/*TODO*///	
/*TODO*///			/* free the extended usage maps */
/*TODO*///			for (i = 0; i < MAX_GFX_ELEMENTS; i++)
/*TODO*///				if (pf.gfxelement[i].usage)
/*TODO*///				{
/*TODO*///					free(pf.gfxelement[i].usage);
/*TODO*///					pf.gfxelement[i].usage = NULL;
/*TODO*///					pf.gfxelement[i].initialized = 0;
/*TODO*///				}
/*TODO*///	
/*TODO*///			pf.initialized = 0;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_get_lookup: Fetches the lookup table so it can
/*TODO*///		be modified.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	UINT32 *ataripf_get_lookup(int map, int *size)
/*TODO*///	{
/*TODO*///		ataripf[map].gfxchanged = 1;
/*TODO*///		if (size != 0)
/*TODO*///			*size = round_to_powerof2(ataripf[map].lookupmask);
/*TODO*///		return ataripf[map].lookup;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_invalidate: Marks all tiles in the playfield
/*TODO*///		dirty. This must be called when the palette changes.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void ataripf_invalidate(int map)
/*TODO*///	{
/*TODO*///		struct ataripf_data *pf = &ataripf[map];
/*TODO*///		if (pf.initialized)
/*TODO*///			memset(pf.dirtymap, -1, sizeof(pf.dirtymap[0]) * pf.vramsize);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_render: Render the playfield, updating any dirty
/*TODO*///		blocks, and copy it to the destination bitmap.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void ataripf_render(int map, struct osd_bitmap *bitmap)
/*TODO*///	{
/*TODO*///		struct ataripf_data *pf = &ataripf[map];
/*TODO*///	
/*TODO*///		if (pf.initialized)
/*TODO*///		{
/*TODO*///			/* render via the standard render callback */
/*TODO*///			pf_process(pf, pf_render_callback, bitmap, NULL);
/*TODO*///	
/*TODO*///			/* set a timer to call the eof function just before scanline 0 */
/*TODO*///			if (!pf.timerallocated)
/*TODO*///			{
/*TODO*///				timer_set(cpu_getscanlinetime(0), map, pf_eof_callback);
/*TODO*///				pf.timerallocated = 1;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_overrender: Overrender the playfield, calling
/*TODO*///		the callback for each tile before proceeding.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void ataripf_overrender(int map, ataripf_overrender_cb callback, struct ataripf_overrender_data *data)
/*TODO*///	{
/*TODO*///		struct ataripf_data *pf = &ataripf[map];
/*TODO*///	
/*TODO*///		if (pf.initialized)
/*TODO*///		{
/*TODO*///			/* set the globals before processing */
/*TODO*///			overrender_callback = callback;
/*TODO*///			overrender_data = *data;
/*TODO*///	
/*TODO*///			/* render via the standard render callback */
/*TODO*///			pf_process(pf, pf_overrender_callback, data.bitmap, &data.clip);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_mark_palette: Mark palette entries used in the
/*TODO*///		current playfield.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void ataripf_mark_palette(int map)
/*TODO*///	{
/*TODO*///		struct ataripf_data *pf = &ataripf[map];
/*TODO*///	
/*TODO*///		if (pf.initialized)
/*TODO*///		{
/*TODO*///			UINT8 *used_colors = &palette_used_colors[pf.palettebase];
/*TODO*///			struct ataripf_usage marked_colors[256];
/*TODO*///			int i, j, k;
/*TODO*///	
/*TODO*///			/* reset the marked colors */
/*TODO*///			memset(marked_colors, 0, pf.maxcolors * sizeof(marked_colors[0]));
/*TODO*///	
/*TODO*///			/* mark the colors used */
/*TODO*///			if (pf.max_usage_words <= 1)
/*TODO*///				pf_process(pf, pf_usage_callback_1, marked_colors, NULL);
/*TODO*///			else if (pf.max_usage_words == 2)
/*TODO*///				pf_process(pf, pf_usage_callback_2, marked_colors, NULL);
/*TODO*///			else
/*TODO*///				logerror("ataripf_mark_palette: unsupported max_usage_words = %d\n", pf.max_usage_words);
/*TODO*///	
/*TODO*///			/* loop over colors */
/*TODO*///			for (i = 0; i < pf.maxcolors; i++)
/*TODO*///			{
/*TODO*///				for (j = 0; j < pf.max_usage_words; j++)
/*TODO*///				{
/*TODO*///					UINT32 usage = marked_colors[i].bits[j];
/*TODO*///	
/*TODO*///					/* if this entry was marked, loop over bits */
/*TODO*///					for (k = 0; usage; k++, usage >>= 1)
/*TODO*///						if ((usage & 1) != 0)
/*TODO*///							used_colors[j * 32 + k] = (pf.transpens & (1 << k)) ? PALETTE_COLOR_TRANSPARENT : PALETTE_COLOR_USED;
/*TODO*///				}
/*TODO*///	
/*TODO*///				/* advance by the color granularity of the gfx */
/*TODO*///				used_colors += ATARIPF_BASE_GRANULARITY;
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* reset the visitation map now that we're done */
/*TODO*///			memset(pf.visitmap, 0, sizeof(pf.visitmap[0]) * pf.vramsize);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_set_bankbits: Set the extra banking bits for a
/*TODO*///		playfield.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void ataripf_set_bankbits(int map, int bankbits, int scanline)
/*TODO*///	{
/*TODO*///		struct ataripf_data *pf = &ataripf[map];
/*TODO*///	
/*TODO*///		if (pf.initialized && pf.curstate.bankbits != bankbits)
/*TODO*///		{
/*TODO*///			pf.curstate.bankbits = bankbits;
/*TODO*///			pf_update_state(pf, scanline);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_set_xscroll: Set the horizontal scroll value for
/*TODO*///		a playfield.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void ataripf_set_xscroll(int map, int xscroll, int scanline)
/*TODO*///	{
/*TODO*///		struct ataripf_data *pf = &ataripf[map];
/*TODO*///		if (pf.initialized && pf.curstate.xscroll != xscroll)
/*TODO*///		{
/*TODO*///			pf.curstate.xscroll = xscroll;
/*TODO*///			pf_update_state(pf, scanline);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_set_yscroll: Set the vertical scroll value for
/*TODO*///		a playfield.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void ataripf_set_yscroll(int map, int yscroll, int scanline)
/*TODO*///	{
/*TODO*///		struct ataripf_data *pf = &ataripf[map];
/*TODO*///		if (pf.initialized && pf.curstate.yscroll != yscroll)
/*TODO*///		{
/*TODO*///			pf.curstate.yscroll = yscroll;
/*TODO*///			pf_update_state(pf, scanline);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_set_latch: Set the upper word latch value and mask
/*TODO*///		a playfield.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void ataripf_set_latch(int map, int latch)
/*TODO*///	{
/*TODO*///		struct ataripf_data *pf = &ataripf[map];
/*TODO*///		int mask;
/*TODO*///	
/*TODO*///		if (pf.initialized)
/*TODO*///		{
/*TODO*///			/* -1 means disable the latching */
/*TODO*///			if (latch == -1)
/*TODO*///				pf.latchdata = -1;
/*TODO*///			else
/*TODO*///				pf.latchdata = latch & pf.latchmask;
/*TODO*///	
/*TODO*///			/* compute the shifted value */
/*TODO*///			pf.latchval = latch & pf.latchmask;
/*TODO*///			mask = pf.latchmask;
/*TODO*///			if (mask != 0)
/*TODO*///				for ( ; !(mask & 1); mask >>= 1)
/*TODO*///					pf.latchval >>= 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_set_latch_lo: Set the latch for any playfield with
/*TODO*///		a latchmask in the low byte.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void ataripf_set_latch_lo(int latch)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		for (i = 0; i < ATARIPF_MAX; i++)
/*TODO*///			if (ataripf[i].latchmask & 0x00ff)
/*TODO*///				ataripf_set_latch(i, latch);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_set_latch_hi: Set the latch for any playfield with
/*TODO*///		a latchmask in the high byte.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void ataripf_set_latch_hi(int latch)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		for (i = 0; i < ATARIPF_MAX; i++)
/*TODO*///			if (ataripf[i].latchmask & 0xff00)
/*TODO*///				ataripf_set_latch(i, latch);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_get_bankbits: Returns the extra banking bits for a
/*TODO*///		playfield.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	int ataripf_get_bankbits(int map)
/*TODO*///	{
/*TODO*///		return ataripf[map].curstate.bankbits;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_get_xscroll: Returns the horizontal scroll value
/*TODO*///		for a playfield.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	int ataripf_get_xscroll(int map)
/*TODO*///	{
/*TODO*///		return ataripf[map].curstate.xscroll;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_get_yscroll: Returns the vertical scroll value for
/*TODO*///		a playfield.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	int ataripf_get_yscroll(int map)
/*TODO*///	{
/*TODO*///		return ataripf[map].curstate.yscroll;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_get_vram: Returns a pointer to video RAM.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	UINT32 *ataripf_get_vram(int map)
/*TODO*///	{
/*TODO*///		return ataripf[map].vram;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_0_simple_w: Simple write handler for single-word
/*TODO*///		playfields.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( ataripf_0_simple_w )
/*TODO*///	{
/*TODO*///		int oldword = LOWER_HALF(ataripf[0].vram[offset]);
/*TODO*///		int newword = oldword;
/*TODO*///	
/*TODO*///		COMBINE_DATA(&newword);
/*TODO*///	
/*TODO*///		if (oldword != newword)
/*TODO*///		{
/*TODO*///			LOWER_HALF(ataripf[0].vram[offset]) = newword;
/*TODO*///			ataripf[0].dirtymap[offset] = -1;
/*TODO*///		}
/*TODO*///		COMBINE_DATA(&ataripf_0_base[offset]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_1_simple_w: Simple write handler for single-word
/*TODO*///		playfields.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( ataripf_1_simple_w )
/*TODO*///	{
/*TODO*///		int oldword = LOWER_HALF(ataripf[1].vram[offset]);
/*TODO*///		int newword = oldword;
/*TODO*///	
/*TODO*///		COMBINE_DATA(&newword);
/*TODO*///	
/*TODO*///		if (oldword != newword)
/*TODO*///		{
/*TODO*///			LOWER_HALF(ataripf[1].vram[offset]) = newword;
/*TODO*///			ataripf[1].dirtymap[offset] = -1;
/*TODO*///		}
/*TODO*///		COMBINE_DATA(&ataripf_1_base[offset]);
/*TODO*///	}
	
	
	/*---------------------------------------------------------------
		ataripf_0_latched_w: Simple write handler for single-word
		playfields that latches additional bits in the upper word.
	---------------------------------------------------------------*/
	
	public static WriteHandlerPtr ataripf_0_latched_w = new WriteHandlerPtr() {
            @Override
            public void handler(int offset, int data) {
                /*TODO*///		int oldword = LOWER_HALF(ataripf[0].vram[offset]);
/*TODO*///		int newword = oldword;
/*TODO*///	
/*TODO*///		COMBINE_DATA(&newword);
/*TODO*///	
/*TODO*///		if (oldword != newword)
/*TODO*///		{
/*TODO*///			LOWER_HALF(ataripf[0].vram[offset]) = newword;
/*TODO*///			ataripf[0].dirtymap[offset] = -1;
/*TODO*///		}
/*TODO*///		if (ataripf[0].latchdata != -1)
/*TODO*///		{
/*TODO*///			UPPER_HALF(ataripf[0].vram[offset]) = ataripf[0].latchval;
/*TODO*///			ataripf_0_upper[offset] = (ataripf_0_upper[offset] & ~ataripf[0].latchmask) | ataripf[0].latchdata;
/*TODO*///			ataripf[0].dirtymap[offset] = -1;
/*TODO*///		}
/*TODO*///		COMBINE_DATA(&ataripf_0_base[offset]);
            }
        };
	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_1_latched_w: Simple write handler for single-word
/*TODO*///		playfields that latches additional bits in the upper word.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( ataripf_1_latched_w )
/*TODO*///	{
/*TODO*///		int oldword = LOWER_HALF(ataripf[1].vram[offset]);
/*TODO*///		int newword = oldword;
/*TODO*///	
/*TODO*///		COMBINE_DATA(&newword);
/*TODO*///	
/*TODO*///		if (oldword != newword)
/*TODO*///		{
/*TODO*///			LOWER_HALF(ataripf[1].vram[offset]) = newword;
/*TODO*///			ataripf[1].dirtymap[offset] = -1;
/*TODO*///		}
/*TODO*///		if (ataripf[1].latchdata != -1)
/*TODO*///		{
/*TODO*///			UPPER_HALF(ataripf[1].vram[offset]) = ataripf[1].latchval;
/*TODO*///			ataripf_0_upper[offset] = (ataripf_0_upper[offset] & ~ataripf[1].latchmask) | ataripf[1].latchdata;
/*TODO*///			ataripf[1].dirtymap[offset] = -1;
/*TODO*///		}
/*TODO*///		COMBINE_DATA(&ataripf_1_base[offset]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_0_upper_msb_w: Simple write handler for the upper
/*TODO*///		word of split two-word playfields, where the MSB contains
/*TODO*///		the significant data.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( ataripf_0_upper_msb_w )
/*TODO*///	{
/*TODO*///		if (ACCESSING_MSB != 0)
/*TODO*///		{
/*TODO*///			int oldword = UPPER_HALF(ataripf[0].vram[offset]);
/*TODO*///			int newword = oldword << 8;
/*TODO*///	
/*TODO*///			COMBINE_DATA(&newword);
/*TODO*///			newword >>= 8;
/*TODO*///	
/*TODO*///			if (oldword != newword)
/*TODO*///			{
/*TODO*///				UPPER_HALF(ataripf[0].vram[offset]) = newword;
/*TODO*///				ataripf[0].dirtymap[offset] = -1;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		COMBINE_DATA(&ataripf_0_upper[offset]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_0_upper_lsb_w: Simple write handler for the upper
/*TODO*///		word of split two-word playfields, where the LSB contains
/*TODO*///		the significant data.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( ataripf_0_upper_lsb_w )
/*TODO*///	{
/*TODO*///		if (ACCESSING_LSB != 0)
/*TODO*///		{
/*TODO*///			int oldword = UPPER_HALF(ataripf[0].vram[offset]);
/*TODO*///			int newword = oldword;
/*TODO*///	
/*TODO*///			COMBINE_DATA(&newword);
/*TODO*///			newword &= 0xff;
/*TODO*///	
/*TODO*///			if (oldword != newword)
/*TODO*///			{
/*TODO*///				UPPER_HALF(ataripf[0].vram[offset]) = newword;
/*TODO*///				ataripf[0].dirtymap[offset] = -1;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		COMBINE_DATA(&ataripf_0_upper[offset]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_0_large_w: Simple write handler for double-word
/*TODO*///		playfields.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( ataripf_0_large_w )
/*TODO*///	{
/*TODO*///		if (!(offset & 1))
/*TODO*///		{
/*TODO*///			int offs = offset / 2;
/*TODO*///			int oldword = UPPER_HALF(ataripf[0].vram[offs]);
/*TODO*///			int newword = oldword;
/*TODO*///	
/*TODO*///			COMBINE_DATA(&newword);
/*TODO*///	
/*TODO*///			if (oldword != newword)
/*TODO*///			{
/*TODO*///				UPPER_HALF(ataripf[0].vram[offs]) = newword;
/*TODO*///				ataripf[0].dirtymap[offs] = -1;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			int offs = offset / 2;
/*TODO*///			int oldword = LOWER_HALF(ataripf[0].vram[offs]);
/*TODO*///			int newword = oldword;
/*TODO*///	
/*TODO*///			COMBINE_DATA(&newword);
/*TODO*///	
/*TODO*///			if (oldword != newword)
/*TODO*///			{
/*TODO*///				LOWER_HALF(ataripf[0].vram[offs]) = newword;
/*TODO*///				ataripf[0].dirtymap[offs] = -1;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		COMBINE_DATA(&ataripf_0_base[offset]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_0_split_w: Simple write handler for split playfields.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( ataripf_0_split_w )
/*TODO*///	{
/*TODO*///		int adjusted = (offset & 0x003f) | ((~offset & 0x1000) >> 6) | ((offset & 0x0fc0) << 1);
/*TODO*///		int oldword = LOWER_HALF(ataripf[0].vram[adjusted]);
/*TODO*///		int newword = oldword;
/*TODO*///	
/*TODO*///		COMBINE_DATA(&newword);
/*TODO*///	
/*TODO*///		if (oldword != newword)
/*TODO*///		{
/*TODO*///			LOWER_HALF(ataripf[0].vram[adjusted]) = newword;
/*TODO*///			ataripf[0].dirtymap[adjusted] = -1;
/*TODO*///		}
/*TODO*///		COMBINE_DATA(&ataripf_0_base[offset]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_01_upper_lsb_msb_w: Simple write handler for the
/*TODO*///		upper word of dual split two-word playfields, where the LSB
/*TODO*///		contains the significant data for playfield 0 and the MSB
/*TODO*///		contains the significant data for playfield 1.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( ataripf_01_upper_lsb_msb_w )
/*TODO*///	{
/*TODO*///		if (ACCESSING_LSB != 0)
/*TODO*///		{
/*TODO*///			int oldword = UPPER_HALF(ataripf[0].vram[offset]);
/*TODO*///			int newword = oldword;
/*TODO*///	
/*TODO*///			COMBINE_DATA(&newword);
/*TODO*///			newword &= 0xff;
/*TODO*///	
/*TODO*///			if (oldword != newword)
/*TODO*///			{
/*TODO*///				UPPER_HALF(ataripf[0].vram[offset]) = newword;
/*TODO*///				ataripf[0].dirtymap[offset] = -1;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		if (ACCESSING_MSB != 0)
/*TODO*///		{
/*TODO*///			int oldword = UPPER_HALF(ataripf[1].vram[offset]);
/*TODO*///			int newword = oldword << 8;
/*TODO*///	
/*TODO*///			COMBINE_DATA(&newword);
/*TODO*///			newword >>= 8;
/*TODO*///	
/*TODO*///			if (oldword != newword)
/*TODO*///			{
/*TODO*///				UPPER_HALF(ataripf[1].vram[offset]) = newword;
/*TODO*///				ataripf[1].dirtymap[offset] = -1;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		COMBINE_DATA(&ataripf_0_upper[offset]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		ataripf_0_split32_w: Simple write handler for split playfields.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE32_HANDLER( ataripf_0_split32_w )
/*TODO*///	{
/*TODO*///		if (ACCESSING_MSW32 != 0)
/*TODO*///		{
/*TODO*///			int adjusted = ((offset & 0x001f) | ((~offset & 0x0800) >> 6) | ((offset & 0x07e0) << 1)) * 2;
/*TODO*///			int oldword = LOWER_HALF(ataripf[0].vram[adjusted]);
/*TODO*///			int newword = oldword << 16;
/*TODO*///	
/*TODO*///			COMBINE_DATA(&newword);
/*TODO*///			newword >>= 16;
/*TODO*///	
/*TODO*///			if (oldword != newword)
/*TODO*///			{
/*TODO*///				LOWER_HALF(ataripf[0].vram[adjusted]) = newword;
/*TODO*///				ataripf[0].dirtymap[adjusted] = -1;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		if (ACCESSING_LSW32 != 0)
/*TODO*///		{
/*TODO*///			int adjusted = ((offset & 0x001f) | ((~offset & 0x0800) >> 6) | ((offset & 0x07e0) << 1)) * 2 + 1;
/*TODO*///			int oldword = LOWER_HALF(ataripf[0].vram[adjusted]);
/*TODO*///			int newword = oldword;
/*TODO*///	
/*TODO*///			COMBINE_DATA(&newword);
/*TODO*///			newword &= 0xffff;
/*TODO*///	
/*TODO*///			if (oldword != newword)
/*TODO*///			{
/*TODO*///				LOWER_HALF(ataripf[0].vram[adjusted]) = newword;
/*TODO*///				ataripf[0].dirtymap[adjusted] = -1;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		COMBINE_DATA(&ataripf_0_base32[offset]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		pf_process: Internal routine that loops over chunks of
/*TODO*///		the playfield with common parameters and processes them
/*TODO*///		via a callback.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void pf_process(struct ataripf_data *pf, pf_callback callback, void *param, const struct rectangle *clip)
/*TODO*///	{
/*TODO*///		struct ataripf_state *state = pf.statelist;
/*TODO*///		struct rectangle finalclip;
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		if (clip != 0)
/*TODO*///			finalclip = *clip;
/*TODO*///		else
/*TODO*///			finalclip = Machine.visible_area;
/*TODO*///	
/*TODO*///		/* if the gfx has changed, make sure we have extended usage maps for everyone */
/*TODO*///		if (pf.gfxchanged)
/*TODO*///		{
/*TODO*///			pf.gfxchanged = 0;
/*TODO*///			for (i = 0; i < pf.lookupmask + 1; i++)
/*TODO*///			{
/*TODO*///				int gfxindex = ATARIPF_LOOKUP_GFX(pf.lookup[i]);
/*TODO*///				if (!pf.gfxelement[gfxindex].initialized)
/*TODO*///				{
/*TODO*///					pf_init_gfx(pf, gfxindex);
/*TODO*///					if (!pf.gfxelement[gfxindex].initialized)
/*TODO*///					{
/*TODO*///						logerror("ataripf_init: out of memory for extra usage map\n");
/*TODO*///						exit(1);
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* preinitialization */
/*TODO*///		pf.process_clip.min_x = finalclip.min_x;
/*TODO*///		pf.process_clip.max_x = finalclip.max_x;
/*TODO*///	
/*TODO*///		/* mark the n+1'th entry with a large scanline */
/*TODO*///		pf.statelist[pf.stateindex].scanline = 100000;
/*TODO*///		pf.process_param = param;
/*TODO*///	
/*TODO*///		/* loop over all entries */
/*TODO*///		for (i = 0; i < pf.stateindex; i++, state++)
/*TODO*///		{
/*TODO*///			/* determine the clip rect */
/*TODO*///			pf.process_clip.min_y = state[0].scanline;
/*TODO*///			pf.process_clip.max_y = state[1].scanline - 1;
/*TODO*///	
/*TODO*///			/* skip if we're clipped out */
/*TODO*///			if (pf.process_clip.min_y > finalclip.max_y || pf.process_clip.max_y < finalclip.min_y)
/*TODO*///				continue;
/*TODO*///	
/*TODO*///			/* clip the clipper */
/*TODO*///			if (pf.process_clip.min_y < finalclip.min_y)
/*TODO*///				pf.process_clip.min_y = finalclip.min_y;
/*TODO*///			if (pf.process_clip.max_y > finalclip.max_y)
/*TODO*///				pf.process_clip.max_y = finalclip.max_y;
/*TODO*///	
/*TODO*///			/* determine the tile rect */
/*TODO*///			pf.process_tiles.min_x = ((state.xscroll + pf.process_clip.min_x) >> pf.tilexshift) & pf.colmask;
/*TODO*///			pf.process_tiles.max_x = ((state.xscroll + pf.process_clip.max_x + pf.tilewidth) >> pf.tilexshift) & pf.colmask;
/*TODO*///			pf.process_tiles.min_y = ((state.yscroll + pf.process_clip.min_y) >> pf.tileyshift) & pf.rowmask;
/*TODO*///			pf.process_tiles.max_y = ((state.yscroll + pf.process_clip.max_y + pf.tileheight) >> pf.tileyshift) & pf.rowmask;
/*TODO*///	
/*TODO*///			/* call the callback */
/*TODO*///			(*callback)(pf, state);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		pf_usage_callback_1: Internal processing callback that
/*TODO*///		marks pens used if the maximum word count is 1.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void pf_usage_callback_1(struct ataripf_data *pf, const struct ataripf_state *state)
/*TODO*///	{
/*TODO*///		struct ataripf_usage *colormap = pf.process_param;
/*TODO*///		int x, y, bankbits = state.bankbits;
/*TODO*///	
/*TODO*///		/* standard loop over tiles */
/*TODO*///		for (y = pf.process_tiles.min_y; y != pf.process_tiles.max_y; y = (y + 1) & pf.rowmask)
/*TODO*///			for (x = pf.process_tiles.min_x; x != pf.process_tiles.max_x; x = (x + 1) & pf.colmask)
/*TODO*///			{
/*TODO*///				int offs = (y << pf.rowshift) + (x << pf.colshift);
/*TODO*///				UINT32 data = pf.vram[offs] | bankbits;
/*TODO*///				int lookup = pf.lookup[(data >> ATARIPF_LOOKUP_DATABITS) & pf.lookupmask];
/*TODO*///				const struct ataripf_gfxelement *gfx = &pf.gfxelement[ATARIPF_LOOKUP_GFX(lookup)];
/*TODO*///				int code = ATARIPF_LOOKUP_CODE(lookup, data);
/*TODO*///				int color = ATARIPF_LOOKUP_COLOR(lookup);
/*TODO*///	
/*TODO*///				/* mark the pens for this color entry */
/*TODO*///				colormap[color << gfx.colorshift].bits[0] |= gfx.usage[code].bits[0];
/*TODO*///	
/*TODO*///				/* mark the pens for the corresponding shadow */
/*TODO*///				colormap[(color ^ pf.shadowxor) << gfx.colorshift].bits[0] |= gfx.usage[code].bits[0];
/*TODO*///	
/*TODO*///				/* also mark unvisited tiles dirty */
/*TODO*///				if (!pf.visitmap[offs])
/*TODO*///					pf.dirtymap[offs] = -1;
/*TODO*///			}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		pf_usage_callback_2: Internal processing callback that
/*TODO*///		marks pens used if the maximum word count is 2.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void pf_usage_callback_2(struct ataripf_data *pf, const struct ataripf_state *state)
/*TODO*///	{
/*TODO*///		struct ataripf_usage *colormap = pf.process_param;
/*TODO*///		int x, y, bankbits = state.bankbits;
/*TODO*///	
/*TODO*///		/* standard loop over tiles */
/*TODO*///		for (y = pf.process_tiles.min_y; y != pf.process_tiles.max_y; y = (y + 1) & pf.rowmask)
/*TODO*///			for (x = pf.process_tiles.min_x; x != pf.process_tiles.max_x; x = (x + 1) & pf.colmask)
/*TODO*///			{
/*TODO*///				int offs = (y << pf.rowshift) + (x << pf.colshift);
/*TODO*///				UINT32 data = pf.vram[offs] | bankbits;
/*TODO*///				int lookup = pf.lookup[(data >> ATARIPF_LOOKUP_DATABITS) & pf.lookupmask];
/*TODO*///				const struct ataripf_gfxelement *gfx = &pf.gfxelement[ATARIPF_LOOKUP_GFX(lookup)];
/*TODO*///				int code = ATARIPF_LOOKUP_CODE(lookup, data);
/*TODO*///				int color = ATARIPF_LOOKUP_COLOR(lookup);
/*TODO*///	
/*TODO*///				/* mark the pens for this color entry */
/*TODO*///				colormap[color << gfx.colorshift].bits[0] |= gfx.usage[code].bits[0];
/*TODO*///				colormap[color << gfx.colorshift].bits[1] |= gfx.usage[code].bits[1];
/*TODO*///	
/*TODO*///				/* mark the pens for the corresponding shadow */
/*TODO*///				colormap[(color ^ pf.shadowxor) << gfx.colorshift].bits[0] |= gfx.usage[code].bits[0];
/*TODO*///				colormap[(color ^ pf.shadowxor) << gfx.colorshift].bits[1] |= gfx.usage[code].bits[1];
/*TODO*///	
/*TODO*///				/* also mark unvisited tiles dirty */
/*TODO*///				if (!pf.visitmap[offs])
/*TODO*///					pf.dirtymap[offs] = -1;
/*TODO*///			}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		pf_render_callback: Internal processing callback that
/*TODO*///		renders to the backing bitmap and then copies the result
/*TODO*///		to the destination.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void pf_render_callback(struct ataripf_data *pf, const struct ataripf_state *state)
/*TODO*///	{
/*TODO*///		struct osd_bitmap *bitmap = pf.process_param;
/*TODO*///		int x, y, bankbits = state.bankbits;
/*TODO*///	
/*TODO*///		/* standard loop over tiles */
/*TODO*///		for (y = pf.process_tiles.min_y; y != pf.process_tiles.max_y; y = (y + 1) & pf.rowmask)
/*TODO*///			for (x = pf.process_tiles.min_x; x != pf.process_tiles.max_x; x = (x + 1) & pf.colmask)
/*TODO*///			{
/*TODO*///				int offs = (y << pf.rowshift) + (x << pf.colshift);
/*TODO*///				UINT32 data = pf.vram[offs] | bankbits;
/*TODO*///	
/*TODO*///				/* update only if dirty */
/*TODO*///				if (pf.dirtymap[offs] != data)
/*TODO*///				{
/*TODO*///					int lookup = pf.lookup[(data >> ATARIPF_LOOKUP_DATABITS) & pf.lookupmask];
/*TODO*///					const struct ataripf_gfxelement *gfx = &pf.gfxelement[ATARIPF_LOOKUP_GFX(lookup)];
/*TODO*///					int code = ATARIPF_LOOKUP_CODE(lookup, data);
/*TODO*///					int color = ATARIPF_LOOKUP_COLOR(lookup);
/*TODO*///					int hflip = ATARIPF_LOOKUP_HFLIP(lookup);
/*TODO*///					int vflip = ATARIPF_LOOKUP_VFLIP(lookup);
/*TODO*///	
/*TODO*///					/* draw and reset the dirty value */
/*TODO*///					drawgfx(pf.bitmap, &gfx.element, code, color << gfx.colorshift, hflip, vflip,
/*TODO*///							x << pf.tilexshift, y << pf.tileyshift,
/*TODO*///							0, TRANSPARENCY_NONE, 0);
/*TODO*///					pf.dirtymap[offs] = data;
/*TODO*///				}
/*TODO*///	
/*TODO*///				/* track the tiles we've visited */
/*TODO*///				pf.visitmap[offs] = 1;
/*TODO*///			}
/*TODO*///	
/*TODO*///		/* then blast the result */
/*TODO*///		x = -state.xscroll;
/*TODO*///		y = -state.yscroll;
/*TODO*///		if (!pf.transpens)
/*TODO*///			copyscrollbitmap(bitmap, pf.bitmap, 1, &x, 1, &y, &pf.process_clip, TRANSPARENCY_NONE, 0);
/*TODO*///		else
/*TODO*///			copyscrollbitmap(bitmap, pf.bitmap, 1, &x, 1, &y, &pf.process_clip, TRANSPARENCY_PEN, palette_transparent_pen);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		pf_overrender_callback: Internal processing callback that
/*TODO*///		calls an external function to determine if a tile should
/*TODO*///		be drawn again, and if so, how it should be drawn.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void pf_overrender_callback(struct ataripf_data *pf, const struct ataripf_state *state)
/*TODO*///	{
/*TODO*///		int x, y, bankbits = state.bankbits;
/*TODO*///		int first_result;
/*TODO*///	
/*TODO*///		/* make the first overrender call */
/*TODO*///		first_result = (*overrender_callback)(&overrender_data, OVERRENDER_BEGIN);
/*TODO*///		if (first_result == OVERRENDER_NONE)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* standard loop over tiles */
/*TODO*///		for (y = pf.process_tiles.min_y; y != pf.process_tiles.max_y; y = (y + 1) & pf.rowmask)
/*TODO*///		{
/*TODO*///			int sy = ((y << pf.tileyshift) - state.yscroll) & pf.bitmapymask;
/*TODO*///			if (sy > Machine.visible_area.max_y) sy -= pf.bitmapheight;
/*TODO*///	
/*TODO*///			for (x = pf.process_tiles.min_x; x != pf.process_tiles.max_x; x = (x + 1) & pf.colmask)
/*TODO*///			{
/*TODO*///				int offs = (y << pf.rowshift) + (x << pf.colshift);
/*TODO*///				UINT32 data = pf.vram[offs] | bankbits;
/*TODO*///				int lookup = pf.lookup[(data >> ATARIPF_LOOKUP_DATABITS) & pf.lookupmask];
/*TODO*///				const struct ataripf_gfxelement *gfx = &pf.gfxelement[ATARIPF_LOOKUP_GFX(lookup)];
/*TODO*///				int code = ATARIPF_LOOKUP_CODE(lookup, data);
/*TODO*///	
/*TODO*///				/* fill in the overrender data that might be needed */
/*TODO*///				overrender_data.pfusage = &gfx.usage[code];
/*TODO*///				overrender_data.pfcolor = ATARIPF_LOOKUP_COLOR(lookup);
/*TODO*///				overrender_data.pfpriority = ATARIPF_LOOKUP_PRIORITY(lookup);
/*TODO*///	
/*TODO*///				/* check with the callback to see if we should overrender */
/*TODO*///				if (first_result == OVERRENDER_ALL || (*overrender_callback)(&overrender_data, OVERRENDER_QUERY))
/*TODO*///				{
/*TODO*///					int hflip = ATARIPF_LOOKUP_HFLIP(lookup);
/*TODO*///					int vflip = ATARIPF_LOOKUP_VFLIP(lookup);
/*TODO*///					int sx = ((x << pf.tilexshift) - state.xscroll) & pf.bitmapxmask;
/*TODO*///					if (sx > Machine.visible_area.max_x) sx -= pf.bitmapwidth;
/*TODO*///	
/*TODO*///					/* use either mdrawgfx or drawgfx depending on the mask pens */
/*TODO*///					if (overrender_data.maskpens != 0)
/*TODO*///						mdrawgfx(overrender_data.bitmap, &gfx.element, code, overrender_data.pfcolor << gfx.colorshift, hflip, vflip,
/*TODO*///								sx, sy, &pf.process_clip, overrender_data.drawmode, overrender_data.drawpens,
/*TODO*///								overrender_data.maskpens);
/*TODO*///					else
/*TODO*///						drawgfx(overrender_data.bitmap, &gfx.element, code, overrender_data.pfcolor << gfx.colorshift, hflip, vflip,
/*TODO*///								sx, sy, &pf.process_clip, overrender_data.drawmode, overrender_data.drawpens);
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* make the final call */
/*TODO*///		(*overrender_callback)(&overrender_data, OVERRENDER_FINISH);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		pf_eof_callback: This callback is called on scanline 0 to
/*TODO*///		reset the playfields.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void pf_eof_callback(int map)
/*TODO*///	{
/*TODO*///		struct ataripf_data *pf = &ataripf[map];
/*TODO*///	
/*TODO*///		/* copy the current state to entry 0 and reset the index */
/*TODO*///		pf.statelist[0] = pf.curstate;
/*TODO*///		pf.statelist[0].scanline = 0;
/*TODO*///		pf.stateindex = 1;
/*TODO*///	
/*TODO*///		/* go off again same time next frame */
/*TODO*///		timer_set(cpu_getscanlinetime(0), map, pf_eof_callback);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		pf_init_gfx: Initializes our own internal graphics
/*TODO*///		representation.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void pf_init_gfx(struct ataripf_data *pf, int gfxindex)
/*TODO*///	{
/*TODO*///		struct ataripf_gfxelement *gfx = &pf.gfxelement[gfxindex];
/*TODO*///		struct ataripf_usage *usage;
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		/* make a copy of the original GfxElement structure */
/*TODO*///		gfx.element = *Machine.gfx[gfxindex];
/*TODO*///	
/*TODO*///		/* adjust the granularity */
/*TODO*///		gfx.colorshift = compute_log(gfx.element.color_granularity / ATARIPF_BASE_GRANULARITY);
/*TODO*///		gfx.element.color_granularity = ATARIPF_BASE_GRANULARITY;
/*TODO*///		gfx.element.total_colors = pf.maxcolors;
/*TODO*///		gfx.element.colortable = &Machine.remapped_colortable[pf.palettebase];
/*TODO*///	
/*TODO*///		/* allocate the extended usage map */
/*TODO*///		usage = malloc(gfx.element.total_elements * sizeof(usage[0]));
/*TODO*///		if (!usage)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* set the pointer and clear the word count */
/*TODO*///		gfx.usage = usage;
/*TODO*///		gfx.usage_words = 0;
/*TODO*///	
/*TODO*///		/* fill in the extended usage map */
/*TODO*///		memset(usage, 0, gfx.element.total_elements * sizeof(usage[0]));
/*TODO*///		for (i = 0; i < gfx.element.total_elements; i++, usage++)
/*TODO*///		{
/*TODO*///			UINT8 *src = gfx.element.gfxdata + gfx.element.char_modulo * i;
/*TODO*///			int x, y, words;
/*TODO*///	
/*TODO*///			/* loop over all pixels, marking pens */
/*TODO*///			for (y = 0; y < gfx.element.height; y++)
/*TODO*///			{
/*TODO*///				/* if the graphics are 4bpp packed, do it one way */
/*TODO*///				if (gfx.element.flags & GFX_PACKED)
/*TODO*///					for (x = 0; x < gfx.element.width / 2; x++)
/*TODO*///					{
/*TODO*///						usage.bits[0] |= 1 << (src[x] & 15);
/*TODO*///						usage.bits[0] |= 1 << (src[x] >> 4);
/*TODO*///					}
/*TODO*///	
/*TODO*///				/* otherwise, do it the original way */
/*TODO*///				else
/*TODO*///					for (x = 0; x < gfx.element.width; x++)
/*TODO*///						usage.bits[src[x] >> 5] |= 1 << (src[x] & 31);
/*TODO*///	
/*TODO*///				src += gfx.element.line_modulo;
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* count how many words maximum we needed to combine */
/*TODO*///			for (words = ATARIPF_USAGE_WORDS; words > 0; words--)
/*TODO*///				if (usage.bits[words - 1] != 0)
/*TODO*///					break;
/*TODO*///			if (words > gfx.usage_words)
/*TODO*///				gfx.usage_words = words;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* if we're the biggest so far, track it */
/*TODO*///		if (gfx.usage_words > pf.max_usage_words)
/*TODO*///			pf.max_usage_words = gfx.usage_words;
/*TODO*///		gfx.initialized = 1;
/*TODO*///	
/*TODO*///		logerror("Finished build external usage map for gfx[%d]: words = %d\n", gfxindex, gfx.usage_words);
/*TODO*///		logerror("Color shift = %d (granularity=%d)\n", gfx.colorshift, gfx.element.color_granularity);
/*TODO*///		logerror("Current maximum = %d\n", pf.max_usage_words);
/*TODO*///	}
}
