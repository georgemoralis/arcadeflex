package gr.codebb.arcadeflex.v037b16.vidhrdw;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import gr.codebb.arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.ataripf.*;

/***************************************************************************

	Atari Shuuz hardware

****************************************************************************/

/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 

import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;

public class shuuz
{
	
	
	
	/*************************************
	 *
	 *	Video system start
	 *
	 *************************************/
	
	public static VhStartPtr shuuz_vh_start = new VhStartPtr() { public int handler() 
	{
/*TODO*///		static const struct ataripf_desc pfdesc =
/*TODO*///		{
/*TODO*///			0,			/* index to which gfx system */
/*TODO*///			64,64,		/* size of the playfield in tiles (x,y) */
/*TODO*///			64,1,		/* tile_index = x * xmult + y * ymult (xmult,ymult) */
/*TODO*///	
/*TODO*///			0x100,		/* index of palette base */
/*TODO*///			0x100,		/* maximum number of colors */
/*TODO*///			0,			/* color XOR for shadow effect (if any) */
/*TODO*///			0xff00,		/* latch mask */
/*TODO*///			0,			/* transparent pen mask */
/*TODO*///	
/*TODO*///			0x03fff,	/* tile data index mask */
/*TODO*///			0xf0000,	/* tile data color mask */
/*TODO*///			0x08000,	/* tile data hflip mask */
/*TODO*///			0,			/* tile data vflip mask */
/*TODO*///			0			/* tile data priority mask */
/*TODO*///		};
/*TODO*///	
/*TODO*///		static const struct atarimo_desc modesc =
/*TODO*///		{
/*TODO*///			1,					/* index to which gfx system */
/*TODO*///			1,					/* number of motion object banks */
/*TODO*///			1,					/* are the entries linked? */
/*TODO*///			0,					/* are the entries split? */
/*TODO*///			0,					/* render in reverse order? */
/*TODO*///			0,					/* render in swapped X/Y order? */
/*TODO*///			0,					/* does the neighbor bit affect the next object? */
/*TODO*///			8,					/* pixels per SLIP entry (0 for no-slip) */
/*TODO*///			8,					/* number of scanlines between MO updates */
/*TODO*///	
/*TODO*///			0x000,				/* base palette entry */
/*TODO*///			0x100,				/* maximum number of colors */
/*TODO*///			0,					/* transparent pen index */
/*TODO*///	
/*TODO*///			{{ 0x00ff,0,0,0 }},	/* mask for the link */
/*TODO*///			{{ 0 }},			/* mask for the graphics bank */
/*TODO*///			{{ 0,0x7fff,0,0 }},	/* mask for the code index */
/*TODO*///			{{ 0 }},			/* mask for the upper code index */
/*TODO*///			{{ 0,0,0x000f,0 }},	/* mask for the color */
/*TODO*///			{{ 0,0,0xff80,0 }},	/* mask for the X position */
/*TODO*///			{{ 0,0,0,0xff80 }},	/* mask for the Y position */
/*TODO*///			{{ 0,0,0,0x0070 }},	/* mask for the width, in tiles*/
/*TODO*///			{{ 0,0,0,0x0007 }},	/* mask for the height, in tiles */
/*TODO*///			{{ 0,0x8000,0,0 }},	/* mask for the horizontal flip */
/*TODO*///			{{ 0 }},			/* mask for the vertical flip */
/*TODO*///			{{ 0 }},			/* mask for the priority */
/*TODO*///			{{ 0 }},			/* mask for the neighbor */
/*TODO*///			{{ 0 }},			/* mask for absolute coordinates */
/*TODO*///	
/*TODO*///			{{ 0 }},			/* mask for the ignore value */
/*TODO*///			0,					/* resulting value to indicate "ignore" */
/*TODO*///			0					/* callback routine for ignored entries */
/*TODO*///		};
/*TODO*///	
/*TODO*///		/* initialize the playfield */
/*TODO*///		if (!ataripf_init(0, &pfdesc))
/*TODO*///			goto cant_create_pf;
/*TODO*///	
/*TODO*///		/* initialize the motion objects */
/*TODO*///		if (!atarimo_init(0, &modesc))
/*TODO*///			goto cant_create_mo;
/*TODO*///		return 0;
/*TODO*///	
/*TODO*///	cant_create_mo:
/*TODO*///		ataripf_free();
/*TODO*///	cant_create_pf:
		return 1;
	} };
	
	
	
	/*************************************
	 *
	 *	Video system shutdown
	 *
	 *************************************/
	
	public static VhStopPtr shuuz_vh_stop = new VhStopPtr() { public void handler() 
	{
/*TODO*///		atarimo_free();
/*TODO*///		ataripf_free();
	} };
	
	
	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Overrendering
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static int overrender_callback(struct ataripf_overrender_data *data, int state)
/*TODO*///	{
/*TODO*///		/* we need to check tile-by-tile, so always return OVERRENDER_SOME */
/*TODO*///		if (state == OVERRENDER_BEGIN)
/*TODO*///		{
/*TODO*///			/* draw the standard playfield */
/*TODO*///			data.drawmode = TRANSPARENCY_NONE;
/*TODO*///			data.drawpens = 0;
/*TODO*///	
/*TODO*///			/* for colors other than 15, query each tile and draw everywhere */
/*TODO*///			if (data.mocolor != 15)
/*TODO*///			{
/*TODO*///				data.maskpens = 0;
/*TODO*///				return OVERRENDER_SOME;
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* otherwise, draw only on top of color 15 */
/*TODO*///			else
/*TODO*///			{
/*TODO*///				data.maskpens = ~0x8000;
/*TODO*///				return OVERRENDER_ALL;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* handle a query */
/*TODO*///		else if (state == OVERRENDER_QUERY)
/*TODO*///			return ((data.pfcolor & 8) && data.pfcolor >= data.mocolor) ? OVERRENDER_YES : OVERRENDER_NO;
/*TODO*///		return 0;
/*TODO*///	}
	
	
	
	/*************************************
	 *
	 *	Main refresh
	 *
	 *************************************/
	
	public static VhUpdatePtr shuuz_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		/* mark the used colors */
		palette_init_used_colors();
		ataripf_mark_palette(0);
/*TODO*///		atarimo_mark_palette(0);
/*TODO*///	
/*TODO*///		/* update the palette, and mark things dirty if we need to */
/*TODO*///		if (palette_recalc())
/*TODO*///			ataripf_invalidate(0);
/*TODO*///	
/*TODO*///		/* draw the layers */
/*TODO*///		ataripf_render(0, bitmap);
/*TODO*///		atarimo_render(0, bitmap, overrender_callback, NULL);
	} };
}
