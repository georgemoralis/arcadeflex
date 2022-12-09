/***************************************************************************

  vidhrdw/llander.c

  Functions to emulate the blinking control panel in lunar lander.
  Added 11/6/98, by Chris Kirmse (ckirmse@ricochet.net)

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.vidhrdw.avgdvg.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.avgdvgH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.vector.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.vectorH.*;

public class llander
{
	
	public static final int NUM_LIGHTS = 5;
	
/*TODO*///	static struct artwork_info *llander_panel;
/*TODO*///	static struct artwork_info *llander_lit_panel;
	
	static rectangle light_areas[] =
	{
		new rectangle(  0, 205, 0, 127 ),
		new rectangle(206, 343, 0, 127 ),
		new rectangle(344, 481, 0, 127 ),
		new rectangle(482, 616, 0, 127 ),
		new rectangle(617, 799, 0, 127 )
	};
	
	/* current status of each light */
	static int[] lights=new int[NUM_LIGHTS];
	/* whether or not each light needs to be redrawn*/
	static int[] lights_changed=new int[NUM_LIGHTS];
	/***************************************************************************
	
	  Lunar Lander video routines
	
	***************************************************************************/
	
	public static VhConvertColorPromHandlerPtr llander_init_colors = new VhConvertColorPromHandlerPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int width, height, i, nextcol;
	
		avg_init_palette_white.handler(palette,colortable,color_prom);
	
/*TODO*///		llander_lit_panel = null;
		width = Machine.scrbitmap.width;
		height = (int) (0.16 * width);
	
		nextcol = 24;
	
/*TODO*///		artwork_load_size(&llander_panel, "llander.png", nextcol, Machine.drv.total_colors-nextcol, width, height);
/*TODO*///		if (llander_panel != NULL)
/*TODO*///		{
/*TODO*///			if (Machine.scrbitmap.depth == 8)
/*TODO*///				nextcol += llander_panel.num_pens_used;
/*TODO*///	
/*TODO*///			artwork_load_size(&llander_lit_panel, "llander1.png", nextcol, Machine.drv.total_colors-nextcol, width, height);
/*TODO*///			if (llander_lit_panel == NULL)
/*TODO*///			{
/*TODO*///				artwork_free (&llander_panel);
/*TODO*///				return ;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///			return;
	
		for (i = 0; i < 16; i++)
			palette[3*(i+8)]=palette[3*(i+8)+1]=palette[3*(i+8)+2]= (char) ((255*i)/15);
	
/*TODO*///		memcpy (palette+3*llander_panel.start_pen, llander_panel.orig_palette,
/*TODO*///				3*llander_panel.num_pens_used);
/*TODO*///		memcpy (palette+3*llander_lit_panel.start_pen, llander_lit_panel.orig_palette,
/*TODO*///				3*llander_lit_panel.num_pens_used);
	} };
	
	public static VhStartHandlerPtr llander_start = new VhStartHandlerPtr() {
            @Override
            public int handler() {
                int i;
	
		if (dvg_start.handler() != 0)
			return 1;
	
/*TODO*///		if (llander_panel == null)
/*TODO*///			return 0;
	
		for (i=0;i<NUM_LIGHTS;i++)
		{
			lights[i] = 0;
			lights_changed[i] = 1;
		}
/*TODO*///		if (llander_panel != 0) backdrop_refresh(llander_panel);
/*TODO*///		if (llander_lit_panel != 0) backdrop_refresh(llander_lit_panel);
		return 0;
            }
        };
	
	public static VhStopHandlerPtr llander_stop = new VhStopHandlerPtr() {
            @Override
            public void handler() {
		dvg_stop.handler();
	
/*TODO*///		if (llander_panel != NULL)
/*TODO*///			artwork_free(&llander_panel);
	
/*TODO*///		if (llander_lit_panel != NULL)
/*TODO*///			artwork_free(&llander_lit_panel);
	
            }
        };
	
	public static VhUpdateHandlerPtr llander_screenrefresh = new VhUpdateHandlerPtr() {
            @Override
            public void handler(osd_bitmap bitmap, int full_refresh) {
		int i, pwidth, pheight;
		float scale=1f;
		osd_bitmap vector_bitmap;
		rectangle rect=new rectangle();
	
/*TODO*///		if (llander_panel == NULL)
/*TODO*///		{
/*TODO*///			vector_vh_screenrefresh(bitmap,full_refresh);
/*TODO*///			return;
/*TODO*///		}
	
/*TODO*///		pwidth = llander_panel.artwork.width;
/*TODO*///		pheight = llander_panel.artwork.height;
	
/*TODO*///		vector_bitmap.width = bitmap.width;
/*TODO*///		vector_bitmap.height = bitmap.height - pheight;
/*TODO*///		vector_bitmap._private = bitmap._private;
/*TODO*///		vector_bitmap.line = bitmap.line;
	
/*TODO*///		vector_vh_screenrefresh.handler(vector_bitmap,full_refresh);
	
		if (full_refresh != 0)
		{
/*TODO*///			rect.min_x = 0;
/*TODO*///			rect.max_x = pwidth-1;
/*TODO*///			rect.min_y = bitmap.height - pheight;
/*TODO*///			rect.max_y = bitmap.height - 1;
	
/*TODO*///			copybitmap(bitmap,llander_panel.artwork,0,0,
/*TODO*///					   0,bitmap.height - pheight,&rect,TRANSPARENCY_NONE,0);
/*TODO*///			osd_mark_dirty (rect.min_x,rect.min_y,rect.max_x,rect.max_y,0);
		}
	
/*TODO*///		scale = pwidth/800.0;
	
		for (i=0;i<NUM_LIGHTS;i++)
		{
			if (lights_changed[i]!=0 || full_refresh!=0)
			{
/*TODO*///				rect.min_x = (int) (scale * light_areas[i].min_x);
/*TODO*///				rect.max_x = (int) (scale * light_areas[i].max_x);
/*TODO*///				rect.min_y = (int) (bitmap.height - pheight + scale * light_areas[i].min_y);
/*TODO*///				rect.max_y = (int) (bitmap.height - pheight + scale * light_areas[i].max_y);
	
/*TODO*///				if (lights[i]!=0)
/*TODO*///					copybitmap(bitmap,llander_lit_panel.artwork,0,0,
/*TODO*///							   0,bitmap.height - pheight,&rect,TRANSPARENCY_NONE,0);
/*TODO*///				else
/*TODO*///					copybitmap(bitmap,llander_panel.artwork,0,0,
/*TODO*///							   0,bitmap.height - pheight,&rect,TRANSPARENCY_NONE,0);
	
/*TODO*///				osd_mark_dirty (rect.min_x,rect.min_y,rect.max_x,rect.max_y,0);
	
				lights_changed[i] = 0;
			}
		}
            }
        };
	
	/* Lunar lander LED port seems to be mapped thus:
	
	   NNxxxxxx - Apparently unused
	   xxNxxxxx - Unknown gives 4 high pulses of variable duration when coin put in ?
	   xxxNxxxx - Start    Lamp ON/OFF == 0/1
	   xxxxNxxx - Training Lamp ON/OFF == 1/0
	   xxxxxNxx - Cadet    Lamp ON/OFF
	   xxxxxxNx - Prime    Lamp ON/OFF
	   xxxxxxxN - Command  Lamp ON/OFF
	
	   Selection lamps seem to all be driver 50/50 on/off during attract mode ?
	
	*/
	
	public static WriteHandlerPtr llander_led_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/*      logerror("LANDER LED: %02x\n",data); */
	
	    int i;
	
	    for (i=0;i<5;i++)
	    {
			int new_light = (data & (1 << (4-i))) != 0 ? 1:0;
			if (lights[i] != new_light)
			{
				lights[i] = new_light;
				lights_changed[i] = 1;
			}
	    }
	
	
	
	} };
	
}
