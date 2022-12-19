/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class _8080bw
{
	
	static int use_tmpbitmap;
	static data_t screen_red;
	static int screen_red_enabled;		/* 1 for games that can turn the screen red */
	static data_t color_map_select;
	static int background_color;
	
	static int artwork_type;
	static const void *init_artwork;
	
	static mem_write_handler videoram_w_p;
	static void (*vh_screenrefresh_p)(struct osd_bitmap *bitmap,int full_refresh);
	static void (*plot_pixel_p)(int x, int y, int col);
	
	
	static static static static static 
	static void plot_pixel_8080 (int x, int y, int col);
	static void plot_pixel_8080_tmpbitmap (int x, int y, int col);
	
	/* smoothed pure colors, overlays are not so contrasted */
	
	#define RED				0xff,0x20,0x20,OVERLAY_DEFAULT_OPACITY
	#define GREEN 			0x20,0xff,0x20,OVERLAY_DEFAULT_OPACITY
	#define YELLOW			0xff,0xff,0x20,OVERLAY_DEFAULT_OPACITY
	#define CYAN			0x20,0xff,0xff,OVERLAY_DEFAULT_OPACITY
	
	#define	END  {{ -1, -1, -1, -1}, 0,0,0,0}
	
	
	static const struct artwork_element invaders_overlay[]=
	{
		{{  16,  71,   0, 255}, GREEN },
		{{   0,  15,  16, 133}, GREEN },
		{{ 192, 223,   0, 255}, RED   },
		END
	};
	
	//static const struct artwork_element invdpt2m_overlay[]=
	//{
	//	{{  16,  71,   0, 255}, GREEN  },
	//	{{   0,  15,  16, 133}, GREEN  },
	//	{{  72, 191,   0, 255}, YELLOW },
	//	{{ 192, 223,   0, 255}, RED    },
	//	END
	//};
	
	static const struct artwork_element invrvnge_overlay[]=
	{
		{{   0,  71,   0, 255}, GREEN },
		{{ 192, 223,   0, 255}, RED   },
		END
	};
	
	static const struct artwork_element invad2ct_overlay[]=
	{
		{{	 0,  47,   0, 255}, YELLOW },
		{{	25,  70,   0, 255}, GREEN  },
		{{	48, 139,   0, 255}, CYAN   },
		{{ 117, 185,   0, 255}, GREEN  },
		{{ 163, 231,   0, 255}, YELLOW },
		{{ 209, 255,   0, 255}, RED    },
		END
	};
	
	enum { NO_ARTWORK = 0, SIMPLE_OVERLAY, FILE_OVERLAY, SIMPLE_BACKDROP, FILE_BACKDROP };
	
	public static InitDriverPtr init_8080bw = new InitDriverPtr() { public void handler() 
	{
		videoram_w_p = bw_videoram_w;
		vh_screenrefresh_p = vh_screenrefresh;
		use_tmpbitmap = 0;
		screen_red = 0;
		screen_red_enabled = 0;
		artwork_type = NO_ARTWORK;
		color_map_select = 0;
		flip_screen_w(0,0);
	} };
	
	public static InitDriverPtr init_invaders = new InitDriverPtr() { public void handler() 
	{
		init_8080bw();
		init_artwork = invaders_overlay;
		artwork_type = SIMPLE_OVERLAY;
	} };
	
	public static InitDriverPtr init_invaddlx = new InitDriverPtr() { public void handler() 
	{
		init_8080bw();
		//init_overlay = invdpt2m_overlay;
		//overlay_type = 1;
	} };
	
	public static InitDriverPtr init_invrvnge = new InitDriverPtr() { public void handler() 
	{
		init_8080bw();
		init_artwork = invrvnge_overlay;
		artwork_type = SIMPLE_OVERLAY;
	} };
	
	public static InitDriverPtr init_invad2ct = new InitDriverPtr() { public void handler() 
	{
		init_8080bw();
		init_artwork = invad2ct_overlay;
		artwork_type = SIMPLE_OVERLAY;
	} };
	
	public static InitDriverPtr init_schaser = new InitDriverPtr() { public void handler() 
	{
		init_8080bw();
		videoram_w_p = schaser_videoram_w;
		background_color = 2;	/* blue */
	} };
	
	public static InitDriverPtr init_rollingc = new InitDriverPtr() { public void handler() 
	{
		init_8080bw();
		videoram_w_p = schaser_videoram_w;
		background_color = 0;	/* black */
	} };
	
	public static InitDriverPtr init_helifire = new InitDriverPtr() { public void handler() 
	{
		init_8080bw();
		videoram_w_p = helifire_videoram_w;
	} };
	
	public static InitDriverPtr init_polaris = new InitDriverPtr() { public void handler() 
	{
		init_8080bw();
		videoram_w_p = polaris_videoram_w;
	} };
	
	public static InitDriverPtr init_lupin3 = new InitDriverPtr() { public void handler() 
	{
		init_8080bw();
		videoram_w_p = lupin3_videoram_w;
		background_color = 0;	/* black */
	} };
	
	public static InitDriverPtr init_invadpt2 = new InitDriverPtr() { public void handler() 
	{
		init_8080bw();
		videoram_w_p = invadpt2_videoram_w;
		screen_red_enabled = 1;
	} };
	
	public static InitDriverPtr init_seawolf = new InitDriverPtr() { public void handler() 
	{
		init_8080bw();
		vh_screenrefresh_p = seawolf_vh_screenrefresh;
		use_tmpbitmap = 1;
	} };
	
	public static InitDriverPtr init_blueshrk = new InitDriverPtr() { public void handler() 
	{
		init_8080bw();
		vh_screenrefresh_p = blueshrk_vh_screenrefresh;
		use_tmpbitmap = 1;
	} };
	
	public static InitDriverPtr init_desertgu = new InitDriverPtr() { public void handler() 
	{
		init_8080bw();
		vh_screenrefresh_p = desertgu_vh_screenrefresh;
		use_tmpbitmap = 1;
	} };
	
	public static InitDriverPtr init_astinvad = new InitDriverPtr() { public void handler() 
	{
		init_8080bw();
		videoram_w_p = astinvad_videoram_w;
		screen_red_enabled = 1;
	} };
	
	public static InitDriverPtr init_spaceint = new InitDriverPtr() { public void handler() 
	{
		init_8080bw();
		videoram_w_p = spaceint_videoram_w;
	} };
	
	public static InitDriverPtr init_spcenctr = new InitDriverPtr() { public void handler() 
	{
		extern struct GameDriver driver_spcenctr;
	
		init_8080bw();
		init_artwork = driver_spcenctr.name;
		artwork_type = FILE_OVERLAY;
	} };
	
	public static InitDriverPtr init_phantom2 = new InitDriverPtr() { public void handler() 
	{
		init_8080bw();
		vh_screenrefresh_p = phantom2_vh_screenrefresh;
		use_tmpbitmap = 1;
	} };
	
	public static InitDriverPtr init_boothill = new InitDriverPtr() { public void handler() 
	{
	//	extern struct GameDriver driver_boothill;
	
		init_8080bw();
	//	init_artwork = driver_boothill.name;
	//	artwork_type = FILE_BACKDROP;
	} };
	
	public static VhStartPtr invaders_vh_start = new VhStartPtr() { public int handler() 
	{
		/* create overlay if one of was specified in init_X */
		if (artwork_type != NO_ARTWORK)
		{
			int start_pen;
			int max_pens;
	
	
			start_pen = 2;
			max_pens = Machine.drv.total_colors-start_pen;
	
			switch (artwork_type)
			{
			case SIMPLE_OVERLAY:
				overlay_create((const struct artwork_element *)init_artwork, start_pen, max_pens);
				break;
			case FILE_OVERLAY:
				overlay_load((const char *)init_artwork, start_pen, max_pens);
				break;
			case SIMPLE_BACKDROP:
				break;
			case FILE_BACKDROP:
				backdrop_load((const char *)init_artwork, start_pen, max_pens);
				break;
			default:
				logerror("Unknown artwork type.\n");
				break;
			}
		}
	
		if (use_tmpbitmap && (generic_bitmapped_vh_start() != 0))
			return 1;
	
		if (use_tmpbitmap != 0)
		{
			plot_pixel_p = plot_pixel_8080_tmpbitmap;
		}
		else
		{
			plot_pixel_p = plot_pixel_8080;
		}
	
		/* make sure that the screen matches the videoram, this fixes invad2ct */
		//schedule_full_refresh();
	
		return 0;
	} };
	
	
	public static VhStopPtr invaders_vh_stop = new VhStopPtr() { public void handler() 
	{
		if (use_tmpbitmap != 0)  generic_bitmapped_vh_stop();
	} };
	
	
	void invaders_flip_screen_w(int data)
	{
		set_vh_global_attribute(&color_map_select, data);
	
		if (input_port_3_r(0) & 0x01)
		{
			flip_screen_w(0, data);
		}
	}
	
	
	void invaders_screen_red_w(int data)
	{
		if (screen_red_enabled != 0)
		{
			set_vh_global_attribute(&screen_red, data);
		}
	}
	
	
	static void plot_pixel_8080 (int x, int y, int col)
	{
		if (flip_screen != 0)
		{
			x = 255-x;
			y = 223-y;
		}
	
		plot_pixel(Machine.scrbitmap,x,y,Machine.pens[col]);
	}
	
	static void plot_pixel_8080_tmpbitmap (int x, int y, int col)
	{
		if (flip_screen != 0)
		{
			x = 255-x;
			y = 223-y;
		}
	
		plot_pixel(tmpbitmap,x,y,Machine.pens[col]);
	}
	
	INLINE void plot_byte(int x, int y, int data, int fore_color, int back_color)
	{
		int i;
	
		for (i = 0; i < 8; i++)
		{
			plot_pixel_p (x, y, (data & 0x01) ? fore_color : back_color);
	
			x++;
			data >>= 1;
		}
	}
	
	
	public static WriteHandlerPtr invaders_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		videoram_w_p(offset, data);
	} };
	
	
	public static WriteHandlerPtr bw_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int x,y;
	
		videoram.write(offset,data);

		y = offset / 32;
		x = 8 * (offset % 32);
	
		plot_byte(x, y, data, 1, 0);
	} };
	
	public static WriteHandlerPtr schaser_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int x,y,col;
	
		videoram.write(offset,data);

		y = offset / 32;
		x = 8 * (offset % 32);
	
		col = colorram.read(offset & 0x1f1f)& 0x07;
	
		plot_byte(x, y, data, col, background_color);
	} };
	
	public static WriteHandlerPtr lupin3_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int x,y,col;
	
		videoram.write(offset,data);

		y = offset / 32;
		x = 8 * (offset % 32);
	
		col = ~colorram.read(offset & 0x1f1f)& 0x07;
	
		plot_byte(x, y, data, col, background_color);
	} };
	
	public static WriteHandlerPtr polaris_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int x,y,back_color,foreground_color;
	
		videoram.write(offset,data);

		y = offset / 32;
		x = 8 * (offset % 32);
	
		/* for the background color, bit 0 if the map PROM is connected to blue gun.
		   red is 0 */
	
		back_color = (memory_region(REGION_PROMS)[(((y+32)/8)*32) + (x/8)] & 1) ? 6 : 4;
		foreground_color = ~colorram.read(offset & 0x1f1f)& 0x07;
	
		plot_byte(x, y, data, foreground_color, back_color);
	} };
	
	public static WriteHandlerPtr helifire_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int x,y,back_color,foreground_color;
	
		videoram.write(offset,data);

		y = offset / 32;
		x = 8 * (offset % 32);
	
		back_color = 0;
		foreground_color = colorram.read(offset)& 0x07;
	
		if (x < 0x78)
		{
			back_color = 4;	/* blue */
		}
	
		plot_byte(x, y, data, foreground_color, back_color);
	} };
	
	
	public static WriteHandlerPtr schaser_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int i;
	
	
		offset &= 0x1f1f;
	
		colorram.write(offset,data);

		/* redraw region with (possibly) changed color */
		for (i = 0; i < 8; i++, offset += 0x20)
		{
			videoram_w_p(offset, videoram.read(offset));
		}
	} };
	
	public static ReadHandlerPtr schaser_colorram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return colorram.read(offset & 0x1f1f);
	} };
	
	
	public static WriteHandlerPtr helifire_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		colorram.write(offset,data);

		/* redraw region with (possibly) changed color */
		videoram_w_p(offset, videoram.read(offset));
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
		if (palette_recalc() || full_refresh)
		{
			int offs;
	
			for (offs = 0;offs < videoram_size[0];offs++)
				videoram_w_p(offs, videoram.read(offs));
		}
	
	
		if (use_tmpbitmap != 0)
			copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
	} };
	
	
	static void draw_sight(int x_center, int y_center)
	{
		int x,y;
	
	
	    if (x_center<2)   x_center=2;
	    if (x_center>253) x_center=253;
	
	    if (y_center<2)   y_center=2;
	    if (y_center>253) y_center=253;
	
		for(y = y_center-10; y < y_center+11; y++)
			if((y >= 0) && (y < 256))
				plot_pixel_8080(x_center,y,1);
	
		for(x = x_center-20; x < x_center+21; x++)
			if((x >= 0) && (x < 256))
				plot_pixel_8080(x,y_center,1);
	}
	
	
	static public static VhUpdatePtr seawolf_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		/* update the bitmap (and erase old cross) */
		vh_screenrefresh(bitmap, full_refresh);
	
	    draw_sight(((input_port_0_r(0) & 0x1f) * 8) + 4, 31);
	} };
	
	static public static VhUpdatePtr blueshrk_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		/* update the bitmap (and erase old cross) */
		vh_screenrefresh(bitmap, full_refresh);
	
	    draw_sight(((input_port_0_r(0) & 0x7f) * 2) - 12, 31);
	} };
	
	static public static VhUpdatePtr desertgu_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		/* update the bitmap (and erase old cross) */
		vh_screenrefresh(bitmap, full_refresh);
	
		draw_sight(((input_port_0_r(0) & 0x7f) * 2) - 30,
				   ((input_port_2_r(0) & 0x7f) * 2) - 30);
	} };
	
	static public static VhUpdatePtr phantom2_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		UBytePtr clouds;
		int x, y;
	
	
		/* update the bitmap */
		vh_screenrefresh(bitmap, full_refresh);
	
	
		/* draw the clouds */
		clouds = memory_region(REGION_PROMS);
	
		for (y = 0; y < 128; y++)
		{
			UBytePtr offs = &memory_region(REGION_PROMS)[y * 0x10];
	
			for (x = 0; x < 128; x++)
			{
				if (offs[x >> 3] & (1 << (x & 0x07)))
				{
					plot_pixel_8080(x*2,   y*2,   1);
					plot_pixel_8080(x*2+1, y*2,   1);
					plot_pixel_8080(x*2,   y*2+1, 1);
					plot_pixel_8080(x*2+1, y*2+1, 1);
				}
			}
		}
	} };
	
	
	public static VhConvertColorPromPtr invadpt2_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
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
	
	public static VhConvertColorPromPtr helifire_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			*(palette++) = 0xff * ((i >> 0) & 1);
			*(palette++) = 0xff * ((i >> 1) & 1);
			*(palette++) = 0xff * ((i >> 2) & 1);
		}
	} };
	
	
	public static WriteHandlerPtr invadpt2_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int x,y,col;
	
		videoram.write(offset,data);

		y = offset / 32;
		x = 8 * (offset % 32);
	
		/* 32 x 32 colormap */
		if (!screen_red)
			col = memory_region(REGION_PROMS)[(color_map_select ? 0x400 : 0 ) + (((y+32)/8)*32) + (x/8)] & 7;
		else
			col = 1;	/* red */
	
		plot_byte(x, y, data, col, 0);
	} };
	
	public static WriteHandlerPtr astinvad_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int x,y,col;
	
		videoram.write(offset,data);

		y = offset / 32;
		x = 8 * (offset % 32);
	
		if (!screen_red)
		{
			if (flip_screen != 0)
				col = memory_region(REGION_PROMS)[((y+32)/8)*32+(x/8)] >> 4;
			else
				col = memory_region(REGION_PROMS)[(31-y/8)*32+(31-x/8)] & 0x0f;
		}
		else
			col = 1; /* red */
	
		plot_byte(x, y, data, col, 0);
	} };
	
	public static WriteHandlerPtr spaceint_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int i,x,y,col;
	
		videoram.write(offset,data);

		y = 8 * (offset / 256);
		x = offset % 256;
	
		/* this is wrong */
		col = memory_region(REGION_PROMS)[(y/16)+16*((x+16)/32)];
	
		for (i = 0; i < 8; i++)
		{
			plot_pixel_p(x, y, (data & 0x01) ? col : 0);
	
			y++;
			data >>= 1;
		}
	} };
}
