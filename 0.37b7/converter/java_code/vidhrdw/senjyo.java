/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class senjyo
{
	
	/* in machine/segacrpt.c */
	
	
	extern UBytePtr spriteram;
	extern size_t spriteram_size;
	
	UBytePtr senjyo_fgscroll;
	UBytePtr senjyo_bgstripes;
	UBytePtr senjyo_scrollx1,*senjyo_scrolly1;
	UBytePtr senjyo_scrollx2,*senjyo_scrolly2;
	UBytePtr senjyo_scrollx3,*senjyo_scrolly3;
	UBytePtr senjyo_fgvideoram,*senjyo_fgcolorram;
	UBytePtr senjyo_bg1videoram,*senjyo_bg2videoram,*senjyo_bg3videoram;
	UBytePtr senjyo_radarram;
	
	static struct tilemap *fg_tilemap,*bg1_tilemap,*bg2_tilemap,*bg3_tilemap;
	
	static int senjyo, scrollhack;
	
	static struct osd_bitmap *bgbitmap;
	
	
	public static InitDriverPtr init_starforc = new InitDriverPtr() { public void handler() 
	{
		senjyo = 0;
		scrollhack = 1;
	} };
	public static InitDriverPtr init_starfore = new InitDriverPtr() { public void handler() 
	{
		/* encrypted CPU */
		suprloco_decode();
	
		senjyo = 0;
		scrollhack = 0;
	} };
	public static InitDriverPtr init_senjyo = new InitDriverPtr() { public void handler() 
	{
		senjyo = 1;
		scrollhack = 0;
	} };
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoPtr get_fg_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		unsigned char attr = senjyo_fgcolorram[tile_index];
		SET_TILE_INFO(0,senjyo_fgvideoram[tile_index] + ((attr & 0x10) << 4),attr & 0x07)
		tile_info.flags = (attr & 0x80) ? TILE_FLIPY : 0;
		if (senjyo && (tile_index & 0x1f) >= 32-8)
			tile_info.flags |= TILE_IGNORE_TRANSPARENCY;
	} };
	
	public static GetTileInfoPtr senjyo_bg1_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		unsigned char code = senjyo_bg1videoram[tile_index];
		SET_TILE_INFO(1,code,(code & 0x70) >> 4)
	} };
	
	public static GetTileInfoPtr starforc_bg1_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		/* Star Force has more tiles in bg1, so to get a uniform color code spread */
		/* they wired bit 7 of the tile code in place of bit 4 to get the color code */
		static int colormap[8] = { 0,2,4,6,1,3,5,7 };
		unsigned char code = senjyo_bg1videoram[tile_index];
		SET_TILE_INFO(1,code,colormap[(code & 0xe0) >> 5])
	} };
	
	public static GetTileInfoPtr get_bg2_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		unsigned char code = senjyo_bg2videoram[tile_index];
		SET_TILE_INFO(2,code,(code & 0xe0) >> 5)
	} };
	
	public static GetTileInfoPtr get_bg3_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		unsigned char code = senjyo_bg3videoram[tile_index];
		SET_TILE_INFO(3,code,(code & 0xe0) >> 5)
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStopPtr senjyo_vh_stop = new VhStopPtr() { public void handler() 
	{
		bitmap_free(bgbitmap);
		bgbitmap = 0;
	} };
	
	public static VhStartPtr senjyo_vh_start = new VhStartPtr() { public int handler() 
	{
		bgbitmap = bitmap_alloc(256,256);
		if (!bgbitmap)
			return 1;
	
		fg_tilemap = tilemap_create(get_fg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,32,32);
		if (senjyo != 0)
		{
			bg1_tilemap = tilemap_create(senjyo_bg1_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,16,16,16,32);
			bg2_tilemap = tilemap_create(get_bg2_tile_info,   tilemap_scan_rows,TILEMAP_TRANSPARENT,16,16,16,48);	/* only 16x32 used by Star Force */
			bg3_tilemap = tilemap_create(get_bg3_tile_info,   tilemap_scan_rows,TILEMAP_TRANSPARENT,16,16,16,56);	/* only 16x32 used by Star Force */
		}
		else
		{
			bg1_tilemap = tilemap_create(starforc_bg1_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,16,16,16,32);
			bg2_tilemap = tilemap_create(get_bg2_tile_info,     tilemap_scan_rows,TILEMAP_TRANSPARENT,16,16,16,32);	/* only 16x32 used by Star Force */
			bg3_tilemap = tilemap_create(get_bg3_tile_info,     tilemap_scan_rows,TILEMAP_TRANSPARENT,16,16,16,32);	/* only 16x32 used by Star Force */
		}
	
	
		if (!fg_tilemap || !bg1_tilemap || !bg2_tilemap || !bg3_tilemap)
		{
			senjyo_vh_stop();
	
			return 1;
		}
	
		fg_tilemap.transparent_pen = 0;
		bg1_tilemap.transparent_pen = 0;
		bg2_tilemap.transparent_pen = 0;
		bg3_tilemap.transparent_pen = 0;
		tilemap_set_scroll_cols(fg_tilemap,32);
	
		schedule_full_refresh();
	
		return 0;
	} };
	
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr senjyo_fgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (senjyo_fgvideoram[offset] != data)
		{
			senjyo_fgvideoram[offset] = data;
			tilemap_mark_tile_dirty(fg_tilemap,offset);
		}
	} };
	public static WriteHandlerPtr senjyo_fgcolorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (senjyo_fgcolorram[offset] != data)
		{
			senjyo_fgcolorram[offset] = data;
			tilemap_mark_tile_dirty(fg_tilemap,offset);
		}
	} };
	public static WriteHandlerPtr senjyo_bg1videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (senjyo_bg1videoram[offset] != data)
		{
			senjyo_bg1videoram[offset] = data;
			tilemap_mark_tile_dirty(bg1_tilemap,offset);
		}
	} };
	public static WriteHandlerPtr senjyo_bg2videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (senjyo_bg2videoram[offset] != data)
		{
			senjyo_bg2videoram[offset] = data;
			tilemap_mark_tile_dirty(bg2_tilemap,offset);
		}
	} };
	public static WriteHandlerPtr senjyo_bg3videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (senjyo_bg3videoram[offset] != data)
		{
			senjyo_bg3videoram[offset] = data;
			tilemap_mark_tile_dirty(bg3_tilemap,offset);
		}
	} };
	
	public static WriteHandlerPtr senjyo_bgstripes_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static data_t bgstripes;
	
		senjyo_bgstripes[offset] = data;
		set_vh_global_attribute(&bgstripes, data);
	} };
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	static void draw_bgbitmap(struct osd_bitmap *bitmap, int full_refresh)
	{
		int x,y,pen,strwid,count;
	
	
		if (*senjyo_bgstripes == 0xff)	/* off */
		{
			fillbitmap(bitmap,Machine.pens[0],0);
			return;
		}
	
		if (full_refresh != 0)
		{
			pen = 0;
			count = 0;
			strwid = *senjyo_bgstripes;
			if (strwid == 0) strwid = 0x100;
			if (flip_screen != 0) strwid ^= 0xff;
	
			for (x = 0;x < 256;x++)
			{
				if (flip_screen != 0)
				{
					for (y = 0;y < 256;y++)
					{
						plot_pixel(bgbitmap, 255 - x, y, Machine.pens[384 + pen]);
					}
				}
				else
				{
					for (y = 0;y < 256;y++)
					{
						plot_pixel(bgbitmap, x, y, Machine.pens[384 + pen]);
					}
				}
	
				count += 0x10;
				if (count >= strwid)
				{
					pen = (pen + 1) & 0x0f;
					count -= strwid;
				}
			}
		}
	
		copybitmap(bitmap,bgbitmap,0,0,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
	}
	
	static void draw_radar(struct osd_bitmap *bitmap)
	{
		int offs,x;
	
		for (offs = 0;offs < 0x400;offs++)
		{
			if (senjyo_radarram[offs])
			{
				for (x = 0;x < 8;x++)
				{
					if (senjyo_radarram[offs] & (1 << x))
					{
						int sx, sy;
	
						sx = (8 * (offs % 8) + x) + 256-64;
						sy = ((offs & 0x1ff) / 8) + 96;
	
						if (flip_screen != 0)
						{
							sx = 255 - sx;
							sy = 255 - sy;
						}
	
						plot_pixel(bitmap,
								   sx, sy,
								   Machine.pens[offs < 0x200 ? 400 : 401]);
					}
				}
			}
		}
	}
	
	static void draw_sprites(struct osd_bitmap *bitmap,int priority)
	{
		const struct rectangle *clip = &Machine.visible_area;
		int offs;
	
	
		for (offs = spriteram_size - 4;offs >= 0;offs -= 4)
		{
			int big,sx,sy,flipx,flipy;
	
			if (((spriteram.read(offs+1)& 0x30) >> 4) == priority)
			{
				if (senjyo != 0)	/* Senjyo */
					big = (spriteram.read(offs)& 0x80);
				else	/* Star Force */
					big = ((spriteram.read(offs)& 0xc0) == 0xc0);
				sx = spriteram.read(offs+3);
				if (big != 0)
					sy = 224-spriteram.read(offs+2);
				else
					sy = 240-spriteram.read(offs+2);
				flipx = spriteram.read(offs+1)& 0x40;
				flipy = spriteram.read(offs+1)& 0x80;
	
				if (flip_screen != 0)
				{
					flipx = !flipx;
					flipy = !flipy;
	
					if (big != 0)
					{
						sx = 224 - sx;
						sy = 226 - sy;
					}
					else
					{
						sx = 240 - sx;
						sy = 242 - sy;
					}
				}
	
	
				drawgfx(bitmap,Machine.gfx[big ? 5 : 4],
						spriteram.read(offs),
						spriteram.read(offs + 1)& 0x07,
						flipx,flipy,
						sx,sy,
						clip,TRANSPARENCY_PEN,0);
			}
		}
	}
	
	public static VhUpdatePtr senjyo_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i;
	
	
		/* two colors for the radar dots (verified on the real board) */
		palette_change_color(400,0xff,0x00,0x00);	/* red for enemies */
		palette_change_color(401,0xff,0xff,0x00);	/* yellow for player */
	
		{
			int scrollx,scrolly;
	
			for (i = 0;i < 32;i++)
				tilemap_set_scrolly(fg_tilemap,i,senjyo_fgscroll[i]);
	
			scrollx = senjyo_scrollx1[0];
			scrolly = senjyo_scrolly1[0] + 256 * senjyo_scrolly1[1];
			if (flip_screen != 0)
				scrollx = -scrollx;
			tilemap_set_scrollx(bg1_tilemap,0,scrollx);
			tilemap_set_scrolly(bg1_tilemap,0,scrolly);
	
			scrollx = senjyo_scrollx2[0];
			scrolly = senjyo_scrolly2[0] + 256 * senjyo_scrolly2[1];
			if (scrollhack != 0)	/* Star Force, but NOT the encrypted version */
			{
				scrollx = senjyo_scrollx1[0];
				scrolly = senjyo_scrolly1[0] + 256 * senjyo_scrolly1[1];
			}
			if (flip_screen != 0)
				scrollx = -scrollx;
			tilemap_set_scrollx(bg2_tilemap,0,scrollx);
			tilemap_set_scrolly(bg2_tilemap,0,scrolly);
	
			scrollx = senjyo_scrollx3[0];
			scrolly = senjyo_scrolly3[0] + 256 * senjyo_scrolly3[1];
			if (flip_screen != 0)
				scrollx = -scrollx;
			tilemap_set_scrollx(bg3_tilemap,0,scrollx);
			tilemap_set_scrolly(bg3_tilemap,0,scrolly);
		}
	
		tilemap_update(ALL_TILEMAPS);
	
		palette_init_used_colors();
	//	mark_sprite_colors();
		for (i = 320; i < 384; i++)
		{
			if (i % 8 != 0)
				palette_used_colors[i] = PALETTE_COLOR_USED;
		}
		for (i = 384; i < 400; i++)
		{
			palette_used_colors[i] = PALETTE_COLOR_USED;
		}
		palette_used_colors[400] = PALETTE_COLOR_USED;
		palette_used_colors[401] = PALETTE_COLOR_USED;
	
		full_refresh |= (palette_recalc() != 0);
		if (full_refresh != 0)
		{
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
		}
	
		tilemap_render(ALL_TILEMAPS);
	
		draw_bgbitmap(bitmap, full_refresh);
		draw_sprites(bitmap,0);
		tilemap_draw(bitmap,bg3_tilemap,0);
		draw_sprites(bitmap,1);
		tilemap_draw(bitmap,bg2_tilemap,0);
		draw_sprites(bitmap,2);
		tilemap_draw(bitmap,bg1_tilemap,0);
		draw_sprites(bitmap,3);
		tilemap_draw(bitmap,fg_tilemap,0);
		draw_radar(bitmap);
	
	#if 0
	{
		char baf[80];
	
		sprintf(baf,"%02x %02x %02x %02x %02x %02x %02x %02x",
			senjyo_scrolly3[0x00],
			senjyo_scrolly3[0x01],
			senjyo_scrolly3[0x02],
			senjyo_scrolly3[0x03],
			senjyo_scrolly3[0x04],
			senjyo_scrolly3[0x05],
			senjyo_scrolly3[0x06],
			senjyo_scrolly3[0x07]);
		ui_text(baf,0,0);
		sprintf(baf,"%02x %02x %02x %02x %02x %02x %02x %02x",
			senjyo_scrolly3[0x08],
			senjyo_scrolly3[0x09],
			senjyo_scrolly3[0x0a],
			senjyo_scrolly3[0x0b],
			senjyo_scrolly3[0x0c],
			senjyo_scrolly3[0x0d],
			senjyo_scrolly3[0x0e],
			senjyo_scrolly3[0x0f]);
		ui_text(baf,0,10);
		sprintf(baf,"%02x %02x %02x %02x %02x %02x %02x %02x",
			senjyo_scrolly3[0x10],
			senjyo_scrolly3[0x11],
			senjyo_scrolly3[0x12],
			senjyo_scrolly3[0x13],
			senjyo_scrolly3[0x14],
			senjyo_scrolly3[0x15],
			senjyo_scrolly3[0x16],
			senjyo_scrolly3[0x17]);
		ui_text(baf,0,20);
		sprintf(baf,"%02x %02x %02x %02x %02x %02x %02x %02x",
			senjyo_scrolly3[0x18],
			senjyo_scrolly3[0x19],
			senjyo_scrolly3[0x1a],
			senjyo_scrolly3[0x1b],
			senjyo_scrolly3[0x1c],
			senjyo_scrolly3[0x1d],
			senjyo_scrolly3[0x1e],
			senjyo_scrolly3[0x1f]);
		ui_text(baf,0,30);
	}
	#endif
	} };
}
