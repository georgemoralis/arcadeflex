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
import gr.codebb.arcadeflex.common.SubArrays.UShortArray;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.ataripfH.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.mameH.MAX_GFX_ELEMENTS;
import gr.codebb.arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import gr.codebb.arcadeflex.v037b7.mame.timer;
import static gr.codebb.arcadeflex.v037b7.mame.timer.*;
import static gr.codebb.arcadeflex.v037b7.mame.timerH.*;

public class ataripf
{
	
	
	
	/*##########################################################################
		TYPES & STRUCTURES
	##########################################################################*/
	
	/* internal state structure containing values that can change scanline-by-scanline */
              
	public static class ataripf_state
	{
		int					scanline;			/* scanline where we are valid */
		int					xscroll;			/* xscroll value */
		int					yscroll;			/* yscroll value */
		int					bankbits;			/* bank bits */

	};
	

	/* internal variant of the gfxelement that contains extra data */
	public static class ataripf_gfxelement
	{
		GfxElement		element;
		int						initialized;
		ataripf_usage	usage;
		int						usage_words;
		int						colorshift;
	};
	
	
	/* internal structure containing the state of a playfield */
	public static class ataripf_data
	{
		int					initialized;		/* true if we're initialized */
		int					timerallocated;		/* true if we've allocated the timer */
		int					gfxchanged;			/* true if the gfx info has changed */

		int					colshift;			/* bits to shift X coordinate when looking up in VRAM */
		int                                     rowshift;			/* bits to shift Y coordinate when looking up in VRAM */
		int					colmask;			/* mask to use when wrapping X coordinate in VRAM */
		int 				rowmask;			/* mask to use when wrapping Y coordinate in VRAM */
		int					vrammask;			/* combined mask when accessing VRAM with raw addresses */
		int					vramsize;			/* total size of VRAM, in entries */

		int					tilexshift;			/* bits to shift X coordinate when drawing */
		int					tileyshift;			/* bits to shift Y coordinate when drawing */
		int					tilewidth;			/* width of a single tile */
		int					tileheight;			/* height of a single tile */
		int					bitmapwidth;		/* width of the full playfield bitmap */
		int					bitmapheight;		/* height of the full playfield bitmap */
		int					bitmapxmask;		/* x coordinate mask for the playfield bitmap */
		int					bitmapymask;		/* y coordinate mask for the playfield bitmap */

		int					palettebase;		/* base palette entry */
		int					maxcolors;			/* maximum number of colors */
		int					shadowxor;			/* color XOR for shadow effect (if any) */
		int				transpens;			/* transparent pen */
		int					transpen;			/* transparent pen */

		int					lookupmask;			/* mask for the lookup table */
	
		int					latchval;			/* value for latching */
		int					latchdata;			/* shifted value for latching */
		int					latchmask;			/* mask for latching */
	
		osd_bitmap              bitmap;				/* backing bitmap */
		char[]			vram;				/* pointer to VRAM */
		char[]                                  dirtymap;	/* dirty bitmap */
		UBytePtr				visitmap;	/* visiting bitmap */
		int[]			lookup;				/* pointer to lookup table */

		ataripf_state curstate;			/* current state */
		ataripf_state[]   statelist;		/* list of changed states */
                int _ataripf_state=0;
		int		stateindex;		/* index of the next state */

		rectangle	process_clip=new rectangle();		/* (during processing) the clip rectangle */
		rectangle	process_tiles=new rectangle();		/* (during processing) the tiles rectangle */
		Object		process_param;		/* (during processing) the callback parameter */
/*TODO*///	
		ataripf_gfxelement[] gfxelement=new ataripf_gfxelement[MAX_GFX_ELEMENTS]; /* graphics element copies */
		int 				max_usage_words;	/* maximum words of usage */
	};
	
	
/*TODO*///	/* callback function for the internal playfield processing mechanism */
/*TODO*///	typedef void (*pf_callback)(struct ataripf_data *pf, const struct ataripf_state *state);
            public static abstract interface pf_callback { public abstract void handler(ataripf_data pf, ataripf_state state); }
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
	
	public static /*data16_t*/ UBytePtr ataripf_0_base;
        public static /*data16_t*/ UBytePtr ataripf_0_upper;

/*TODO*///	data16_t *ataripf_1_base;
/*TODO*///	
/*TODO*///	data32_t *ataripf_0_base32;
	
	
	
	/*##########################################################################
		STATIC VARIABLES
	##########################################################################*/
	
	static ataripf_data[] ataripf = new ataripf_data[ATARIPF_MAX];
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
	
	
	
	/*##########################################################################
		INLINE FUNCTIONS
	##########################################################################*/
	
	/*---------------------------------------------------------------
		compute_log: Computes the number of bits necessary to
		hold a given value. The input must be an even power of
		two.
	---------------------------------------------------------------*/
	
	static int compute_log(int value)
	{
		int log = 0;
	
		if (value == 0)
			return -1;
		while ((value & 1)==0){
			log++;
                        value >>= 1;
                }
		if (value != 1)
			return -1;
		return log;
	}
	
	
	/*---------------------------------------------------------------
		round_to_powerof2: Rounds a number up to the nearest
		power of 2. Even powers of 2 are rounded up to the
		next greatest power (e.g., 4 returns 8).
	---------------------------------------------------------------*/
	
	static int round_to_powerof2(int value)
	{
		int log = 0;
	
		if (value == 0)
			return 1;
		while ((value >>= 1) != 0)
			log++;
		return 1 << (log + 1);
	}
	
	
	/*---------------------------------------------------------------
		collapse_bits: Moving right-to-left, for each 1 bit in
		the mask, copy the corresponding bit from the input
		value into the result, packing the bits along the way.
	---------------------------------------------------------------*/
	
	static int collapse_bits(int value, int mask)
	{
		int testmask, ormask;
		int result = 0;
	
		for (testmask = ormask = 1; testmask != 0; testmask <<= 1)
			if ((mask & testmask) != 0)
			{
				if ((value & testmask) != 0)
					result |= ormask;
				ormask <<= 1;
			}
		return result;
	}
	
	
	/*---------------------------------------------------------------
		pf_update_state: Internal routine that updates the
		state list of the playfield with the current parameters.
	---------------------------------------------------------------*/
	
	public static void pf_update_state(ataripf_data pf, int scanline)
	{
		//ataripf_state state = pf.statelist[pf.stateindex];
                int _idx = pf.stateindex;
	
		/* ignore anything after the bottom of the visible screen */
		if (scanline > Machine.visible_area.max_y)
			return;
	
		/* ignore anything earlier than the last scanline we entered */
		if (pf.statelist[_idx-1].scanline > scanline)
		{
			logerror("pf_update_state: Attempted state update on prior scanline (%d vs. %d)\n", scanline, pf.statelist[_idx-1]);
			return;
		}
	
		/* if this is the same scanline as last time, overwrite it */
		else if (pf.statelist[_idx-1].scanline == scanline)
		{
			logerror("pf_update_state: scanlines equal, overwriting\n");
			//state--;
                        _idx--;
		}
	
		/* otherwise, move forward one entry */
		else
		{
			logerror("pf_update_state: new entry\n");
			pf.stateindex++;
		}
	
		/* fill in the data */
		//*state = pf.curstate;
                pf.statelist[_idx] = pf.curstate;
		//state.scanline = scanline;
                pf.statelist[_idx].scanline = scanline;
	}
	
	
	
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
	
	
	/*---------------------------------------------------------------
		ataripf_init: Configures the playfield using the input
		description. Allocates all memory necessary and generates
		the attribute lookup table. If custom_lookup is provided,
		it is used in place of the generated attribute table.
	---------------------------------------------------------------*/
	
	public static int ataripf_init(int map, ataripf_desc desc)
	{
		int lookupcount = round_to_powerof2(desc.tilemask | desc.colormask | desc.hflipmask | desc.vflipmask | desc.prioritymask) >> ATARIPF_LOOKUP_DATABITS;
		GfxElement gfx = Machine.gfx[desc.gfxindex];
		//ataripf_data pf = ataripf[map];
                
                if (ataripf[map]==null) {
                    ataripf[map] = new ataripf_data();
                    
                    for (int _i=0 ; _i<MAX_GFX_ELEMENTS ; _i++)
                        ataripf[map].gfxelement[_i]=new ataripf_gfxelement();
                }
                
		int i;
	
/*TODO*///		/* sanity checks */
/*TODO*///		VERIFYRETFREE(map >= 0 && map < ATARIPF_MAX, "ataripf_init: map out of range", 0)
/*TODO*///		VERIFYRETFREE(compute_log(desc.cols) != -1, "ataripf_init: cols must be power of 2", 0)
/*TODO*///		VERIFYRETFREE(compute_log(desc.rows) != -1, "ataripf_init: rows must be power of 2", 0)
/*TODO*///		VERIFYRETFREE(compute_log(desc.xmult) != -1, "ataripf_init: xmult must be power of 2", 0)
/*TODO*///		VERIFYRETFREE(compute_log(desc.ymult) != -1, "ataripf_init: ymult must be power of 2", 0)
/*TODO*///		VERIFYRETFREE((desc.tilemask & ATARIPF_LOOKUP_DATAMASK) == ATARIPF_LOOKUP_DATAMASK, "ataripf_init: low bits of tilemask must be 0xff", 0)
	
		/* copy in the basic data */
		ataripf[map].initialized  = 0;
		ataripf[map].timerallocated = 0;
		ataripf[map].gfxchanged   = 0;
	
		ataripf[map].colshift     = compute_log(desc.xmult);
		ataripf[map].rowshift     = compute_log(desc.ymult);
		ataripf[map].colmask      = desc.cols - 1;
		ataripf[map].rowmask      = desc.rows - 1;
		ataripf[map].vrammask     = (ataripf[map].colmask << ataripf[map].colshift) | (ataripf[map].rowmask << ataripf[map].rowshift);
		ataripf[map].vramsize     = round_to_powerof2(ataripf[map].vrammask);
	
		ataripf[map].tilexshift   = compute_log(gfx.width);
		ataripf[map].tileyshift   = compute_log(gfx.height);
		ataripf[map].tilewidth    = gfx.width;
		ataripf[map].tileheight   = gfx.height;
		ataripf[map].bitmapwidth  = desc.cols * gfx.width;
		ataripf[map].bitmapheight = desc.rows * gfx.height;
		ataripf[map].bitmapxmask  = ataripf[map].bitmapwidth - 1;
		ataripf[map].bitmapymask  = ataripf[map].bitmapheight - 1;
	
		ataripf[map].palettebase  = desc.palettebase;
		ataripf[map].maxcolors    = desc.maxcolors / ATARIPF_BASE_GRANULARITY;
		ataripf[map].shadowxor    = desc.shadowxor;
		ataripf[map].transpens    = desc.transpens;
		ataripf[map].transpen     = desc.transpens!=0 ? compute_log(desc.transpens) : -1;
	
		ataripf[map].lookupmask   = lookupcount - 1;
	
		ataripf[map].latchval     = 0;
		ataripf[map].latchdata    = -1;
		ataripf[map].latchmask    = desc.latchmask;
	
		/* allocate the backing bitmap */
		ataripf[map].bitmap = bitmap_alloc(ataripf[map].bitmapwidth, ataripf[map].bitmapheight);
/*TODO*///		VERIFYRETFREE(pf.bitmap, "ataripf_init: out of memory for bitmap", 0)
	
		/* allocate the vram */
		ataripf[map].vram = new char[ataripf[map].vramsize];
/*TODO*///		VERIFYRETFREE(pf.vram, "ataripf_init: out of memory for vram", 0)
	
		/* clear it to zero */
		memset(ataripf[map].vram, 0, ataripf[map].vramsize);
	
		/* allocate the dirty map */
		ataripf[map].dirtymap = new char[ataripf[map].vramsize];
/*TODO*///		VERIFYRETFREE(pf.dirtymap, "ataripf_init: out of memory for dirtymap", 0)
	
		/* mark everything dirty */
		memset(ataripf[map].dirtymap, -1, ataripf[map].vramsize);
	
		/* allocate the visitation map */
		ataripf[map].visitmap = new UBytePtr(ataripf[map].vramsize);
/*TODO*///		VERIFYRETFREE(pf.visitmap, "ataripf_init: out of memory for visitmap", 0)
	
		/* mark everything non-visited */
		memset(ataripf[map].visitmap, 0, ataripf[map].vramsize);
	
		/* allocate the attribute lookup */
		ataripf[map].lookup = new int[lookupcount];
/*TODO*///		VERIFYRETFREE(pf.lookup, "ataripf_init: out of memory for lookup", 0)
	
		/* fill in the attribute lookup */
		for (i = 0; i < lookupcount; i++)
		{
			int value    = (i << ATARIPF_LOOKUP_DATABITS);
			int tile     = collapse_bits(value, desc.tilemask);
			int color    = collapse_bits(value, desc.colormask);
			int hflip    = collapse_bits(value, desc.hflipmask);
			int vflip    = collapse_bits(value, desc.vflipmask);
			int priority = collapse_bits(value, desc.prioritymask);
	
			ataripf[map].lookup[i] = ATARIPF_LOOKUP_ENTRY(desc.gfxindex, tile, color, hflip, vflip, priority);
		}
	
		/* compute the extended usage map */
		pf_init_gfx(ataripf[map], desc.gfxindex);
/*TODO*///		VERIFYRETFREE(pf.gfxelement[desc.gfxindex].initialized, "ataripf_init: out of memory for extra usage map", 0)
	
		/* allocate the state list */
		ataripf[map].statelist = new ataripf_state[ataripf[map].bitmapheight];
/*TODO*///		VERIFYRETFREE(pf.statelist, "ataripf_init: out of memory for extra state list", 0)
	
		/* reset the state list */
/*TODO*///		memset(&pf.curstate, 0, sizeof(pf.curstate));
                ataripf[map].curstate = new ataripf_state();
                
		ataripf[map].statelist[0] = ataripf[map].curstate;
		ataripf[map].stateindex = 1;
	
		ataripf[map].initialized = 1;
	
		logerror("ataripf_init:\n");
		logerror("  width=%d (shift=%d),  height=%d (shift=%d)\n", gfx.width, ataripf[map].tilexshift, gfx.height, ataripf[map].tileyshift);
		logerror("  cols=%d  (mask=%X),   rows=%d   (mask=%X)\n", desc.cols, ataripf[map].colmask, desc.rows, ataripf[map].rowmask);
		logerror("  xmult=%d (shift=%d),  ymult=%d  (shift=%d)\n", desc.xmult, ataripf[map].colshift, desc.ymult, ataripf[map].rowshift);
		logerror("  VRAM mask=%X,  dirtymap size=%d\n", ataripf[map].vrammask, ataripf[map].vramsize);
		logerror("  bitmap size=%dx%d\n", ataripf[map].bitmapwidth, ataripf[map].bitmapheight);
	
		return 1;
	}
	
	
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
	
	
	/*---------------------------------------------------------------
		ataripf_invalidate: Marks all tiles in the playfield
		dirty. This must be called when the palette changes.
	---------------------------------------------------------------*/
	
	public static void ataripf_invalidate(int map)
	{
		ataripf_data pf = ataripf[map];
		if (pf.initialized != 0)
			memset(pf.dirtymap, -1, pf.vramsize);
	}
	
	
	/*---------------------------------------------------------------
		ataripf_render: Render the playfield, updating any dirty
		blocks, and copy it to the destination bitmap.
	---------------------------------------------------------------*/
	
	public static void ataripf_render(int map, osd_bitmap bitmap)
	{
		ataripf_data pf = ataripf[map];
	
		if (pf.initialized != 0)
		{
			/* render via the standard render callback */
			pf_process(pf, pf_render_callback, bitmap, null);
	
			/* set a timer to call the eof function just before scanline 0 */
			if (pf.timerallocated==0)
			{
				timer_set(cpu_getscanlinetime(0), map, pf_eof_callback);
				pf.timerallocated = 1;
			}
		}
	}
	
	
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
	
	
	/*---------------------------------------------------------------
		ataripf_mark_palette: Mark palette entries used in the
		current playfield.
	---------------------------------------------------------------*/
	
	public static void ataripf_mark_palette(int map)
	{
		ataripf_data pf = ataripf[map];
	
		if (pf.initialized != 0)
		{
			UBytePtr used_colors = new UBytePtr(palette_used_colors, pf.palettebase);
			ataripf_usage[] marked_colors = new ataripf_usage[256];
			int i, j, k;
	
			/* reset the marked colors */
			//memset(marked_colors, 0, pf.maxcolors);
                        for (int _i=0 ; _i<pf.maxcolors ; _i++)
                            marked_colors[_i] = new ataripf_usage();
	
			/* mark the colors used */
			if (pf.max_usage_words <= 1)
				pf_process(pf, pf_usage_callback_1, marked_colors, null);
			else if (pf.max_usage_words == 2)
				pf_process(pf, pf_usage_callback_2, marked_colors, null);
			else
				logerror("ataripf_mark_palette: unsupported max_usage_words = %d\n", pf.max_usage_words);
	
			/* loop over colors */
			for (i = 0; i < pf.maxcolors; i++)
			{
				for (j = 0; j < pf.max_usage_words; j++)
				{
					int usage = marked_colors[i].read().bits[j];
	
					/* if this entry was marked, loop over bits */
					for (k = 0; usage!=0; k++, usage >>= 1)
						if ((usage & 1) != 0)
							used_colors.write(j * 32 + k, (pf.transpens & (1 << k))!=0 ? PALETTE_COLOR_TRANSPARENT : PALETTE_COLOR_USED);
				}
	
				/* advance by the color granularity of the gfx */
				used_colors.inc( ATARIPF_BASE_GRANULARITY );
			}
	
			/* reset the visitation map now that we're done */
			memset(pf.visitmap, 0, pf.vramsize);
		}
	}
	
	
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
	
	
	/*---------------------------------------------------------------
		ataripf_set_xscroll: Set the horizontal scroll value for
		a playfield.
	---------------------------------------------------------------*/
	
	public static void ataripf_set_xscroll(int map, int xscroll, int scanline)
	{
		//struct ataripf_data pf = ataripf[map];
		if (ataripf[map].initialized!=0 && ataripf[map].curstate.xscroll != xscroll)
		{
			ataripf[map].curstate.xscroll = xscroll;
			pf_update_state(ataripf[map], scanline);
		}
	}
	
	
	/*---------------------------------------------------------------
		ataripf_set_yscroll: Set the vertical scroll value for
		a playfield.
	---------------------------------------------------------------*/
	
	public static void ataripf_set_yscroll(int map, int yscroll, int scanline)
	{
		//struct ataripf_data *pf = &ataripf[map];
		if (ataripf[map].initialized!=0 && ataripf[map].curstate.yscroll != yscroll)
		{
			ataripf[map].curstate.yscroll = yscroll;
			pf_update_state(ataripf[map], scanline);
		}
	}
	
	
	/*---------------------------------------------------------------
		ataripf_set_latch: Set the upper word latch value and mask
		a playfield.
	---------------------------------------------------------------*/
	
	static void ataripf_set_latch(int map, int latch)
	{
/*TODO*///		struct ataripf_data *pf = &ataripf[map];
		int mask;
	
		if (ataripf[map].initialized != 0)
		{
			/* -1 means disable the latching */
			if (latch == -1)
				ataripf[map].latchdata = -1;
			else
				ataripf[map].latchdata = latch & ataripf[map].latchmask;
	
			/* compute the shifted value */
			ataripf[map].latchval = latch & ataripf[map].latchmask;
			mask = ataripf[map].latchmask;
			if (mask != 0)
				for ( ; (mask & 1)==0; mask >>= 1)
					ataripf[map].latchval >>= 1;
		}
	}
	
	
	/*---------------------------------------------------------------
		ataripf_set_latch_lo: Set the latch for any playfield with
		a latchmask in the low byte.
	---------------------------------------------------------------*/
	
	public static void ataripf_set_latch_lo(int latch)
	{
		int i;
	
		for (i = 0; i < ATARIPF_MAX; i++){
                        if (ataripf[i] == null)
                            ataripf[i] = new ataripf_data();
                        
			if ((ataripf[i].latchmask & 0x00ff) != 0)
				ataripf_set_latch(i, latch);
                }
	}
	
	
	/*---------------------------------------------------------------
		ataripf_set_latch_hi: Set the latch for any playfield with
		a latchmask in the high byte.
	---------------------------------------------------------------*/
	
	public static void ataripf_set_latch_hi(int latch)
	{
		int i;
	
		for (i = 0; i < ATARIPF_MAX; i++)
			if ((ataripf[i].latchmask & 0xff00) != 0)
				ataripf_set_latch(i, latch);
	}
	
	
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
	
	
	/*---------------------------------------------------------------
		ataripf_0_upper_msb_w: Simple write handler for the upper
		word of split two-word playfields, where the MSB contains
		the significant data.
	---------------------------------------------------------------*/
	//WRITE16_HANDLER
        public static WriteHandlerPtr ataripf_0_upper_msb_w = new WriteHandlerPtr() {
            @Override
            public void handler(int offset, int data) {
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
            }
        };

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
	
	
	/*---------------------------------------------------------------
		pf_process: Internal routine that loops over chunks of
		the playfield with common parameters and processes them
		via a callback.
	---------------------------------------------------------------*/
	
	static void pf_process(ataripf_data pf, pf_callback callback, Object param, rectangle clip)
	{
		ataripf_state[] state = pf.statelist;
		rectangle finalclip;
		int i;
	
		if (clip != null)
			finalclip = new rectangle(clip);
		else
			finalclip = new rectangle(Machine.visible_area);
	
		/* if the gfx has changed, make sure we have extended usage maps for everyone */
		if (pf.gfxchanged != 0)
		{
			pf.gfxchanged = 0;
			for (i = 0; i < pf.lookupmask + 1; i++)
			{
				int gfxindex = ATARIPF_LOOKUP_GFX(pf.lookup[i]);
				if (pf.gfxelement[gfxindex].initialized == 0)
				{
					pf_init_gfx(pf, gfxindex);
					if (pf.gfxelement[gfxindex].initialized==0)
					{
						logerror("ataripf_init: out of memory for extra usage map\n");
						System.exit(1);
					}
				}
			}
		}
	
		/* preinitialization */
		pf.process_clip.min_x = finalclip.min_x;
		pf.process_clip.max_x = finalclip.max_x;
	
		/* mark the n+1'th entry with a large scanline */
                if (pf.statelist[pf.stateindex] == null)
                    pf.statelist[pf.stateindex] = new ataripf_state();
                
		pf.statelist[pf.stateindex].scanline = 100000;
		pf.process_param = param;
                
                int _state=0;
	
		/* loop over all entries */
		for (i = 0; i < pf.stateindex; i++, _state++)
		{
			/* determine the clip rect */
			pf.process_clip.min_y = state[0].scanline;
			pf.process_clip.max_y = state[1].scanline - 1;
	
			/* skip if we're clipped out */
			if (pf.process_clip.min_y > finalclip.max_y || pf.process_clip.max_y < finalclip.min_y)
				continue;
	
			/* clip the clipper */
			if (pf.process_clip.min_y < finalclip.min_y)
				pf.process_clip.min_y = finalclip.min_y;
			if (pf.process_clip.max_y > finalclip.max_y)
				pf.process_clip.max_y = finalclip.max_y;
	
			/* determine the tile rect */
			pf.process_tiles.min_x = ((state[_state].xscroll + pf.process_clip.min_x) >> pf.tilexshift) & pf.colmask;
			pf.process_tiles.max_x = ((state[_state].xscroll + pf.process_clip.max_x + pf.tilewidth) >> pf.tilexshift) & pf.colmask;
			pf.process_tiles.min_y = ((state[_state].yscroll + pf.process_clip.min_y) >> pf.tileyshift) & pf.rowmask;
			pf.process_tiles.max_y = ((state[_state].yscroll + pf.process_clip.max_y + pf.tileheight) >> pf.tileyshift) & pf.rowmask;
	
			/* call the callback */
			(callback).handler(pf, state[_state]);
		}
	}
	
	
	/*---------------------------------------------------------------
		pf_usage_callback_1: Internal processing callback that
		marks pens used if the maximum word count is 1.
	---------------------------------------------------------------*/
	
	static pf_callback pf_usage_callback_1 = new pf_callback() {
            @Override
            public void handler(ataripf_data pf, ataripf_state state) {
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
            }
        };
        

	
	/*---------------------------------------------------------------
		pf_usage_callback_2: Internal processing callback that
		marks pens used if the maximum word count is 2.
	---------------------------------------------------------------*/
	
	static pf_callback pf_usage_callback_2 = new pf_callback() {
            @Override
            public void handler(ataripf_data pf, ataripf_state state) {
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
            }
        };
	
	
	/*---------------------------------------------------------------
		pf_render_callback: Internal processing callback that
		renders to the backing bitmap and then copies the result
		to the destination.
	---------------------------------------------------------------*/
	
	static pf_callback pf_render_callback = new pf_callback() {
            @Override
            public void handler(ataripf_data pf, ataripf_state state) {
                osd_bitmap bitmap = (osd_bitmap) pf.process_param;
		int x, y, bankbits = state.bankbits;
	
		/* standard loop over tiles */
		for (y = pf.process_tiles.min_y; y != pf.process_tiles.max_y; y = (y + 1) & pf.rowmask)
			for (x = pf.process_tiles.min_x; x != pf.process_tiles.max_x; x = (x + 1) & pf.colmask)
			{
				int offs = (y << pf.rowshift) + (x << pf.colshift);
				int data = pf.vram[offs] | bankbits;
	
				/* update only if dirty */
				if (pf.dirtymap[offs] != data)
				{
					int lookup = pf.lookup[(data >> ATARIPF_LOOKUP_DATABITS) & pf.lookupmask];
					ataripf_gfxelement gfx = pf.gfxelement[ATARIPF_LOOKUP_GFX(lookup)];
					int code = ATARIPF_LOOKUP_CODE(lookup, data);
					int color = ATARIPF_LOOKUP_COLOR(lookup);
					int hflip = ATARIPF_LOOKUP_HFLIP(lookup);
					int vflip = ATARIPF_LOOKUP_VFLIP(lookup);
	
					/* draw and reset the dirty value */
					drawgfx(pf.bitmap, gfx.element, code, color << gfx.colorshift, hflip, vflip,
							x << pf.tilexshift, y << pf.tileyshift,
							null, TRANSPARENCY_NONE, 0);
					pf.dirtymap[offs] = (char) data;
				}
	
				/* track the tiles we've visited */
				pf.visitmap.write(offs, 1);
			}
	
		/* then blast the result */
		x = -state.xscroll;
		y = -state.yscroll;
		if (pf.transpens==0)
			copyscrollbitmap(bitmap, pf.bitmap, 1, new int[]{x}, 1, new int[]{y}, pf.process_clip, TRANSPARENCY_NONE, 0);
		else
			copyscrollbitmap(bitmap, pf.bitmap, 1, new int[]{x}, 1, new int[]{y}, pf.process_clip, TRANSPARENCY_PEN, palette_transparent_pen);
            }
        };
        
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
	
	
	/*---------------------------------------------------------------
		pf_eof_callback: This callback is called on scanline 0 to
		reset the playfields.
	---------------------------------------------------------------*/
	
	static timer_callback pf_eof_callback = new timer.timer_callback() {
            @Override
            public void handler(int map) {
                ataripf_data pf = ataripf[map];
	
		/* copy the current state to entry 0 and reset the index */
		pf.statelist[0] = pf.curstate;
		pf.statelist[0].scanline = 0;
		pf.stateindex = 1;
	
		/* go off again same time next frame */
		timer_set(cpu_getscanlinetime(0), map, pf_eof_callback);
            }
        };
	
	
	/*---------------------------------------------------------------
		pf_init_gfx: Initializes our own internal graphics
		representation.
	---------------------------------------------------------------*/
	
	static void pf_init_gfx(ataripf_data pf, int gfxindex)
	{
		ataripf_gfxelement gfx = pf.gfxelement[gfxindex];
		ataripf_usage usage;
		int i;
	
		/* make a copy of the original GfxElement structure */
		gfx.element = Machine.gfx[gfxindex];
	
		/* adjust the granularity */
		gfx.colorshift = compute_log(gfx.element.color_granularity / ATARIPF_BASE_GRANULARITY);
		gfx.element.color_granularity = ATARIPF_BASE_GRANULARITY;
		gfx.element.total_colors = pf.maxcolors;
		gfx.element.colortable = new UShortArray(Machine.remapped_colortable, pf.palettebase);
	
		/* allocate the extended usage map */
		usage = new ataripf_usage(gfx.element.total_elements);
		if (usage==null)
			return;
	
		/* set the pointer and clear the word count */
		gfx.usage = usage;
		gfx.usage_words = 0;
	
		/* fill in the extended usage map */
/*TODO*///		memset(usage, 0, gfx.element.total_elements);
		for (i = 0; i < gfx.element.total_elements; i++, usage.inc())
		{
			UBytePtr src = new UBytePtr(gfx.element.gfxdata, gfx.element.char_modulo * i);
			int x, y, words;
	
			/* loop over all pixels, marking pens */
			for (y = 0; y < gfx.element.height; y++)
			{
				/* if the graphics are 4bpp packed, do it one way */
/*TODO*///				if (gfx.element.flags & GFX_PACKED)
/*TODO*///					for (x = 0; x < gfx.element.width / 2; x++)
/*TODO*///					{
/*TODO*///						usage.bits[0] |= 1 << (src[x] & 15);
/*TODO*///						usage.bits[0] |= 1 << (src[x] >> 4);
/*TODO*///					}
/*TODO*///	
/*TODO*///				/* otherwise, do it the original way */
/*TODO*///				else
					for (x = 0; x < gfx.element.width; x++){
						ataripf_usage_block _temp = usage.read();
                                                _temp.bits[src.read(x) >> 5] |= 1 << (src.read(x) & 31);
                                                usage.write(_temp);
                                        }
	
				src.inc( gfx.element.line_modulo );
			}
	
			/* count how many words maximum we needed to combine */
			for (words = ATARIPF_USAGE_WORDS; words > 0; words--)
				if (usage.read().bits[words - 1] != 0)
					break;
			if (words > gfx.usage_words)
				gfx.usage_words = words;
		}
	
		/* if we're the biggest so far, track it */
		if (gfx.usage_words > pf.max_usage_words)
			pf.max_usage_words = gfx.usage_words;
		gfx.initialized = 1;
	
		logerror("Finished build external usage map for gfx[%d]: words = %d\n", gfxindex, gfx.usage_words);
		logerror("Color shift = %d (granularity=%d)\n", gfx.colorshift, gfx.element.color_granularity);
		logerror("Current maximum = %d\n", pf.max_usage_words);
	}
}
