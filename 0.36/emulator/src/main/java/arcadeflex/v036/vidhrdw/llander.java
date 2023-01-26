/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
/**
 * Changelog
 * =========
 * 26/01/2023 - shadow - 0.36 version . Missing artwork support
 */
package arcadeflex.v036.vidhrdw;

//generic improts
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.artworkH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.osdependH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.avgdvg.*;
//TODO
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;

public class llander {

    public static final int NUM_LIGHTS = 5;
    static struct_artwork llander_panel;
    static struct_artwork llander_lit_panel;
    /*TODO*///	
/*TODO*///	static struct rectangle light_areas[NUM_LIGHTS] =
/*TODO*///	{
/*TODO*///		{  0, 205, 0, 127 },
/*TODO*///		{206, 343, 0, 127 },
/*TODO*///		{344, 481, 0, 127 },
/*TODO*///		{482, 616, 0, 127 },
/*TODO*///		{617, 799, 0, 127 },
/*TODO*///	};
/*TODO*///	

    /* current status of each light */
    static int[] lights = new int[NUM_LIGHTS];
    /* whether or not each light needs to be redrawn*/
    static int[] lights_changed = new int[NUM_LIGHTS];
    /**
     * *************************************************************************
     * Lunar Lander video routines
     * *************************************************************************
     */
    public static VhConvertColorPromHandlerPtr llander_init_colors = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int width, height, i;

            avg_init_palette_white.handler(palette, colortable, color_prom);

            llander_lit_panel = null;
            width = Machine.scrbitmap.width;
            height = (int) (0.16 * width);
            /*TODO*///	
/*TODO*///		if ((llander_panel = artwork_load_size("llander.png", 24, 230, width, height))!=NULL)
/*TODO*///		{
/*TODO*///			if ((llander_lit_panel = artwork_load_size("llander1.png", 24 + llander_panel.num_pens_used, 230 - llander_panel.num_pens_used, width, height))==NULL)
/*TODO*///			{
/*TODO*///				artwork_free (llander_panel);
/*TODO*///				llander_panel = NULL;
/*TODO*///				return ;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///			return;
/*TODO*///	
/*TODO*///		for (i = 0; i < 16; i++)
/*TODO*///			palette[3*(i+8)]=palette[3*(i+8)+1]=palette[3*(i+8)+2]= (255*i)/15;
/*TODO*///	
/*TODO*///		memcpy (palette+3*llander_panel.start_pen, llander_panel.orig_palette,
/*TODO*///				3*llander_panel.num_pens_used);
/*TODO*///		memcpy (palette+3*llander_lit_panel.start_pen, llander_lit_panel.orig_palette,
/*TODO*///				3*llander_lit_panel.num_pens_used);
        }
    };
    public static VhStartHandlerPtr llander_start = new VhStartHandlerPtr() {
        public int handler() {
            int i;

            if (dvg_start.handler() != 0) {
                return 1;
            }
            /*TODO*///	
/*TODO*///		if (llander_panel == NULL)
/*TODO*///			return 0;
/*TODO*///	
/*TODO*///		for (i=0;i<NUM_LIGHTS;i++)
/*TODO*///		{
/*TODO*///			lights[i] = 0;
/*TODO*///			lights_changed[i] = 1;
/*TODO*///		}
/*TODO*///		if (llander_panel != 0) backdrop_refresh(llander_panel);
/*TODO*///		if (llander_lit_panel != 0) backdrop_refresh(llander_lit_panel);
            return 0;
        }
    };
    public static VhStopHandlerPtr llander_stop = new VhStopHandlerPtr() {
        public void handler() {
            dvg_stop.handler();
            /*TODO*///	
/*TODO*///		if (llander_panel != NULL)
/*TODO*///			artwork_free(llander_panel);
/*TODO*///		llander_panel = NULL;
/*TODO*///	
/*TODO*///		if (llander_lit_panel != NULL)
/*TODO*///			artwork_free(llander_lit_panel);
/*TODO*///		llander_lit_panel = NULL;
/*TODO*///	
        }
    };
    public static VhUpdateHandlerPtr llander_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int i, pwidth, pheight;
            float scale;
            osd_bitmap vector_bitmap;
            rectangle rect = new rectangle();

            if (llander_panel == null) {
                dvg_screenrefresh.handler(bitmap, full_refresh);
                return;
            }
            /*TODO*///	
/*TODO*///		pwidth = llander_panel.artwork.width;
/*TODO*///		pheight = llander_panel.artwork.height;
/*TODO*///	
/*TODO*///		vector_bitmap.width = bitmap.width;
/*TODO*///		vector_bitmap.height = bitmap.height - pheight;
/*TODO*///		vector_bitmap._private = bitmap._private;
/*TODO*///		vector_bitmap.line = bitmap.line;
/*TODO*///	
/*TODO*///		dvg_screenrefresh(&vector_bitmap,full_refresh);
/*TODO*///	
/*TODO*///		if (full_refresh != 0)
/*TODO*///		{
/*TODO*///			rect.min_x = 0;
/*TODO*///			rect.max_x = pwidth-1;
/*TODO*///			rect.min_y = bitmap.height - pheight;
/*TODO*///			rect.max_y = bitmap.height - 1;
/*TODO*///	
/*TODO*///			copybitmap(bitmap,llander_panel.artwork,0,0,
/*TODO*///					   0,bitmap.height - pheight,&rect,TRANSPARENCY_NONE,0);
/*TODO*///			osd_mark_dirty (rect.min_x,rect.min_y,rect.max_x,rect.max_y,0);
/*TODO*///		}
/*TODO*///	
/*TODO*///		scale = pwidth/800.0;
/*TODO*///	
/*TODO*///		for (i=0;i<NUM_LIGHTS;i++)
/*TODO*///		{
/*TODO*///			if (lights_changed[i] || full_refresh)
/*TODO*///			{
/*TODO*///				rect.min_x = scale * light_areas[i].min_x;
/*TODO*///				rect.max_x = scale * light_areas[i].max_x;
/*TODO*///				rect.min_y = bitmap.height - pheight + scale * light_areas[i].min_y;
/*TODO*///				rect.max_y = bitmap.height - pheight + scale * light_areas[i].max_y;
/*TODO*///	
/*TODO*///				if (lights[i])
/*TODO*///					copybitmap(bitmap,llander_lit_panel.artwork,0,0,
/*TODO*///							   0,bitmap.height - pheight,&rect,TRANSPARENCY_NONE,0);
/*TODO*///				else
/*TODO*///					copybitmap(bitmap,llander_panel.artwork,0,0,
/*TODO*///							   0,bitmap.height - pheight,&rect,TRANSPARENCY_NONE,0);
/*TODO*///	
/*TODO*///				osd_mark_dirty (rect.min_x,rect.min_y,rect.max_x,rect.max_y,0);
/*TODO*///	
/*TODO*///				lights_changed[i] = 0;
/*TODO*///			}
/*TODO*///		}
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
    public static WriteHandlerPtr llander_led_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /*      logerror("LANDER LED: %02x\n",data); */

            int i;

            for (i = 0; i < 5; i++) {
                int new_light = (data & (1 << (4 - i))) != 0 ? 1 : 0;
                if (lights[i] != new_light) {
                    lights[i] = new_light;
                    lights_changed[i] = 1;
                }
            }
        }
    };
}
