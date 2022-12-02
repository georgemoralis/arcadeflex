/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package vidhrdw;

import static arcadeflex.libc.*;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static vidhrdw.generic.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.mame.*;

public class _8080bw
{
	
	static int use_tmpbitmap;
	static int flipscreen;
	static int screen_red;
	static int screen_red_enabled;		/* 1 for games that can turn the screen red */
	static int redraw_screen;
	static int color_map_select;
	
	static const struct artwork_element *init_overlay;
	static struct artwork *overlay;
	
	static void (*videoram_w_p)(int offset,int data);
	static void (*vh_screenrefresh_p)(struct osd_bitmap *bitmap,int full_refresh);
	static void (*plot_pixel_p)(int x, int y, int col);
	
	static public static WriteHandlerPtr bw_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	static public static WriteHandlerPtr schaser_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	static public static WriteHandlerPtr rollingc_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	static public static WriteHandlerPtr invadpt2_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	static public static WriteHandlerPtr astinvad_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	static public static WriteHandlerPtr spaceint_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	
	static public static VhUpdatePtr vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) ;
	static public static VhUpdatePtr seawolf_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) ;
	static public static VhUpdatePtr blueshrk_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) ;
	static public static VhUpdatePtr desertgu_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) ;
	
	static void plot_pixel_8080 (int x, int y, int col);
	static void plot_pixel_8080_tmpbitmap (int x, int y, int col);
	
	/* smoothed pure colors, overlays are not so contrasted */
	
	#define BLACK			0x00,0x00,0x00
	#define RED				0xff,0x20,0x20
	#define GREEN 			0x20,0xff,0x20
	#define YELLOW			0xff,0xff,0x20
	#define WHITE			0xff,0xff,0xff
	#define CYAN			0x20,0xff,0xff
	#define PURPLE			0xff,0x20,0xff
	
	#define ORANGE			0xff,0x90,0x20
	#define YELLOW_GREEN	0x90,0xff,0x20
	#define GREEN_CYAN		0x20,0xff,0x90
	
	#define	END  {{  -1,  -1,  -1,  -1}, 0,0,0,0} };
	
	
	static const struct artwork_element invaders_overlay[]=
	{
		{{	 0, 255,   0, 255}, WHITE,  0xff},
		{{  16,  71,   0, 255}, GREEN,  0xff},
		{{   0,  15,  16, 133}, GREEN,  0xff},
		{{ 192, 223,   0, 255}, RED,    0xff},
		END
	};
	
	static const struct artwork_element invdpt2m_overlay[]=
	{
		{{	 0, 255,   0, 255}, WHITE,  0xff},
		{{  16,  71,   0, 255}, GREEN,  0xff},
		{{   0,  15,  16, 133}, GREEN,  0xff},
		{{  72, 191,   0, 255}, YELLOW, 0xff},
		{{ 192, 223,   0, 255}, RED,    0xff},
		END
	};
	
	static const struct artwork_element invrvnge_overlay[]=
	{
		{{	 0, 255,   0, 255}, WHITE,  0xff},
		{{   0,  71,   0, 255}, GREEN,  0xff},
		{{ 192, 223,   0, 255}, RED,    0xff},
		END
	};
	
	static const struct artwork_element invad2ct_overlay[]=
	{
		{{	 0,  24,   0, 255}, YELLOW,       0xff},
		{{	25,  47,   0, 255}, YELLOW_GREEN, 0xff},
		{{	48,  70,   0, 255}, GREEN_CYAN,   0xff},
		{{	71, 116,   0, 255}, CYAN,         0xff},
		{{ 117, 139,   0, 255}, GREEN_CYAN,   0xff},
		{{ 140, 162,   0, 255}, GREEN,        0xff},
		{{ 163, 185,   0, 255}, YELLOW_GREEN, 0xff},
		{{ 186, 208,   0, 255}, YELLOW,       0xff},
		{{ 209, 231,   0, 255}, ORANGE,       0xff},
		{{ 232, 255,   0, 255}, RED,          0xff},
		END
	};
	
	
	void init_8080bw(void)
	{
		videoram_w_p = bw_videoram_w;
		vh_screenrefresh_p = vh_screenrefresh;
		use_tmpbitmap = 0;
		screen_red_enabled = 0;
		init_overlay = 0;
		color_map_select = 0;
		flipscreen = 0;
	}
	
	void init_invaders(void)
	{
		init_8080bw();
		init_overlay = invaders_overlay;
	}
	
	void init_invdpt2m(void)
	{
		init_8080bw();
		init_overlay = invdpt2m_overlay;
	}
	
	void init_invrvnge(void)
	{
		init_8080bw();
		init_overlay = invrvnge_overlay;
	}
	
	void init_invad2ct(void)
	{
		init_8080bw();
		init_overlay = invad2ct_overlay;
	}
	
	void init_schaser(void)
	{
		init_8080bw();
		videoram_w_p = schaser_videoram_w;
	}
	
	void init_rollingc(void)
	{
		init_8080bw();
		videoram_w_p = rollingc_videoram_w;
	}
	
	void init_invadpt2(void)
	{
		init_8080bw();
		videoram_w_p = invadpt2_videoram_w;
		screen_red_enabled = 1;
	}
	
	void init_seawolf(void)
	{
		init_8080bw();
		vh_screenrefresh_p = seawolf_vh_screenrefresh;
		use_tmpbitmap = 1;
	}
	
	void init_blueshrk(void)
	{
		init_8080bw();
		vh_screenrefresh_p = blueshrk_vh_screenrefresh;
		use_tmpbitmap = 1;
	}
	
	void init_desertgu(void)
	{
		init_8080bw();
		vh_screenrefresh_p = desertgu_vh_screenrefresh;
		use_tmpbitmap = 1;
	}
	
	void init_astinvad(void)
	{
		init_8080bw();
		videoram_w_p = astinvad_videoram_w;
		screen_red_enabled = 1;
	}
	
	void init_spaceint(void)
	{
		init_8080bw();
		videoram_w_p = spaceint_videoram_w;
	}
	
	
	public static VhStartPtr invaders_vh_start = new VhStartPtr() { public int handler() 
	{
		/* create overlay if one of was specified in init_X */
		if (init_overlay != 0)
		{
			if ((overlay = artwork_create(init_overlay, 2, Machine.drv.total_colors-2)) == 0)
				return 1;
	
			use_tmpbitmap = 1;
		}
	
		if (use_tmpbitmap && (generic_bitmapped_vh_start() != 0))
			return 1;
	
		plot_pixel_p = use_tmpbitmap ? plot_pixel_8080_tmpbitmap : plot_pixel_8080;
	
		return 0;
	} };
	
	
	public static VhStopPtr invaders_vh_stop = new VhStopPtr() { public void handler() 
	{
		if (overlay != 0)
		{
			artwork_free(overlay);
			overlay = 0;
		}
	
		if (use_tmpbitmap != 0)  generic_bitmapped_vh_stop();
	} };
	
	
	void invaders_flipscreen_w(int data)
	{
		if (data != color_map_select)
		{
			color_map_select = data;
			redraw_screen = 1;
		}
	
		if (input_port_3_r(0) & 0x01)
		{
			if (data != flipscreen)
			{
				flipscreen = data;
				redraw_screen = 1;
			}
		}
	}
	
	
	void invaders_screen_red_w(int data)
	{
		if (screen_red_enabled && (data != screen_red))
		{
			screen_red = data;
			redraw_screen = 1;
		}
	}
	
	
	static void plot_pixel_8080 (int x, int y, int col)
	{
		if (flipscreen != 0)
		{
			x = 255-x;
			y = 223-y;
		}
	
		plot_pixel(Machine.scrbitmap,x,y,Machine.pens[col]);
	}
	
	static void plot_pixel_8080_tmpbitmap (int x, int y, int col)
	{
		if (flipscreen != 0)
		{
			x = 255-x;
			y = 223-y;
		}
	
		plot_pixel2(Machine.scrbitmap,tmpbitmap,x,y,Machine.pens[col]);
	}
	
	
	public static WriteHandlerPtr invaders_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		videoram_w_p(offset, data);
	} };
	
	
	static public static WriteHandlerPtr bw_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int i,x,y;
	
		videoram[offset] = data;
	
		y = offset / 32;
		x = 8 * (offset % 32);
	
		for (i = 0; i < 8; i++)
		{
			plot_pixel_p (x, y, data & 0x01);
	
			x ++;
			data >>= 1;
		}
	} };
	
	
	/* thr only difference between these is the background color */
	
	static public static WriteHandlerPtr schaser_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int i,x,y,fore_color,back_color;
	
		videoram[offset] = data;
	
		y = offset / 32;
		x = 8 * (offset % 32);
	
		back_color = 2;	/* blue */
		fore_color = colorram[offset & 0x1f1f] & 0x07;
	
		for (i = 0; i < 8; i++)
		{
			if ((data & 0x01) != 0)
				plot_pixel_p (x, y, fore_color);
			else
				plot_pixel_p (x, y, back_color);
	
			x ++;
			data >>= 1;
		}
	} };
	
	static public static WriteHandlerPtr rollingc_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int i,x,y,fore_color,back_color;
	
		videoram[offset] = data;
	
		y = offset / 32;
		x = 8 * (offset % 32);
	
		back_color = 0;	/* black */
		fore_color = colorram[offset & 0x1f1f] & 0x07;
	
		for (i = 0; i < 8; i++)
		{
			if ((data & 0x01) != 0)
				plot_pixel_p (x, y, fore_color);
			else
				plot_pixel_p (x, y, back_color);
	
			x ++;
			data >>= 1;
		}
	} };
	
	public static WriteHandlerPtr schaser_colorram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int i;
	
	
		offset &= 0x1f1f;
	
		colorram[offset] = data;
	
		/* redraw region with (possibly) changed color */
		for (i = 0; i < 8; i++, offset += 0x20)
		{
			videoram_w_p(offset, videoram[offset]);
		}
	} };
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr invaders_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		vh_screenrefresh_p(bitmap, full_refresh);
	} };
	
	
	static public static VhUpdatePtr vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		if (palette_recalc() || redraw_screen || (full_refresh && !use_tmpbitmap))
		{
			int offs;
	
			for (offs = 0;offs < videoram_size;offs++)
				videoram_w_p(offs, videoram[offs]);
	
			redraw_screen = 0;
	
			if (overlay != 0)
				overlay_remap(overlay);
		}
	
	
		if (full_refresh && use_tmpbitmap)
			/* copy the character mapped graphics */
			copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	
		if (overlay != 0)
			overlay_draw(bitmap,overlay);
	} };
	
	
	static void draw_sight(struct osd_bitmap *bitmap, int x_center, int y_center)
	{
		int x,y;
	
	
	    if (x_center<2)   x_center=2;
	    if (x_center>253) x_center=253;
	
	    if (y_center<2)   y_center=2;
	    if (y_center>253) y_center=253;
	
		for(y = y_center-10; y < y_center+11; y++)
			if((y >= 0) && (y < 256))
				plot_pixel(bitmap,x_center,y,Machine.pens[1]);
	
		for(x = x_center-20; x < x_center+21; x++)
			if((x >= 0) && (x < 256))
				plot_pixel(bitmap,x,y_center,Machine.pens[1]);
	}
	
	
	static public static VhUpdatePtr seawolf_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		/* update the bitmap (and erase old cross) */
	
		vh_screenrefresh(bitmap, 1);
	
	    draw_sight(bitmap, ((input_port_0_r(0) & 0x1f) * 8) + 4, 31);
	} };
	
	static public static VhUpdatePtr blueshrk_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		/* update the bitmap (and erase old cross) */
	
		vh_screenrefresh(bitmap, 1);
	
	    draw_sight(bitmap, ((input_port_1_r(0) & 0x7f) * 2) - 12, 31);
	} };
	
	static public static VhUpdatePtr desertgu_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		/* update the bitmap (and erase old cross) */
	
		vh_screenrefresh(bitmap, 1);
	
		draw_sight(bitmap,
				   ((input_port_1_r(0) & 0x7f) * 2) - 30,
				   ((input_port_2_r(0) & 0x7f) * 2) - 30);
	} };
	
	
	public static VhConvertColorPromPtr invadpt2_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(UByte []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			/* this bit arrangment is a little unusual but are confirmed by screen shots */
	
			*(palette++) = 0xff * ((i >> 0) & 1);
			*(palette++) = 0xff * ((i >> 2) & 1);
			*(palette++) = 0xff * ((i >> 1) & 1);
		}
	} };
	
	static public static WriteHandlerPtr invadpt2_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int i,x,y;
		int col;
	
		videoram[offset] = data;
	
		y = offset / 32;
		x = 8 * (offset % 32);
	
		/* 32 x 32 colormap */
		if (!screen_red)
			col = memory_region(REGION_PROMS)[(color_map_select ? 0x400 : 0 ) + (((y+32)/8)*32) + (x/8)] & 7;
		else
			col = 1;	/* red */
	
		for (i = 0; i < 8; i++)
		{
			plot_pixel_p(x, y, (data & 0x01) ? col : 0);
	
			x ++;
			data >>= 1;
		}
	} };
	
	
	static public static WriteHandlerPtr astinvad_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int i,x,y;
		int col;
	
		videoram[offset] = data;
	
		y = offset / 32;
		x = 8 * (offset % 32);
	
		if (!screen_red)
		{
			if (flipscreen != 0)
				col = memory_region(REGION_PROMS)[((y+32)/8)*32+(x/8)] >> 4;
			else
				col = memory_region(REGION_PROMS)[(31-y/8)*32+(31-x/8)] & 0x0f;
		}
		else
			col = 1; /* red */
	
		for (i = 0; i < 8; i++)
		{
			plot_pixel_p(x, y, (data & 0x01) ? col : 0);
	
			x++;
			data >>= 1;
		}
	} };
	
	static public static WriteHandlerPtr spaceint_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int i;
		UINT8 x,y;
	
		videoram[offset] = data;
	
		y = 8 * (offset / 256);
		x = offset % 256;
	
		for (i = 0; i < 8; i++)
		{
			int col = 0;
	
			if ((data & 0x01) != 0)
			{
				/* this is wrong */
				col = memory_region(REGION_PROMS)[(y/16)+16*((x+16)/32)];
			}
	
			plot_pixel_p(x, y, col);
	
			y++;
			data >>= 1;
		}
	} };
}
