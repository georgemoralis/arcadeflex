/***************************************************************************

  video hardware for Tecmo games

***************************************************************************/

#include "driver.h"


unsigned char *tecmo_txvideoram,*tecmo_fgvideoram,*tecmo_bgvideoram;
extern unsigned char *spriteram;
extern size_t spriteram_size;

int tecmo_video_type = 0;
/*
   video_type is used to distinguish Rygar, Silkworm and Gemini Wing.
   This is needed because there is a difference in the tile and sprite indexing.
*/

static struct tilemap *tx_tilemap,*fg_tilemap,*bg_tilemap;


/***************************************************************************

  Callbacks for the TileMap code

***************************************************************************/

static void get_bg_tile_info(int tile_index)
{
	unsigned char attr = tecmo_bgvideoram[tile_index+0x200];
	SET_TILE_INFO(3,tecmo_bgvideoram[tile_index] + ((attr & 0x07) << 8),attr >> 4)
}

static void get_fg_tile_info(int tile_index)
{
	unsigned char attr = tecmo_fgvideoram[tile_index+0x200];
	SET_TILE_INFO(2,tecmo_fgvideoram[tile_index] + ((attr & 0x07) << 8),attr >> 4)
}

static void gemini_get_bg_tile_info(int tile_index)
{
	unsigned char attr = tecmo_bgvideoram[tile_index+0x200];
	SET_TILE_INFO(3,tecmo_bgvideoram[tile_index] + ((attr & 0x70) << 4),attr & 0x0f)
}

static void gemini_get_fg_tile_info(int tile_index)
{
	unsigned char attr = tecmo_fgvideoram[tile_index+0x200];
	SET_TILE_INFO(2,tecmo_fgvideoram[tile_index] + ((attr & 0x70) << 4),attr & 0x0f)
}

static void get_tx_tile_info(int tile_index)
{
	unsigned char attr = tecmo_txvideoram[tile_index+0x400];
	SET_TILE_INFO(0,tecmo_txvideoram[tile_index] + ((attr & 0x03) << 8),attr >> 4)
}



/***************************************************************************

  Start the video hardware emulation.

***************************************************************************/

int tecmo_vh_start(void)
{
	if (tecmo_video_type == 2)	/* gemini */
	{
		bg_tilemap = tilemap_create(gemini_get_bg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,16,16,32,16);
		fg_tilemap = tilemap_create(gemini_get_fg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,16,16,32,16);
	}
	else	/* rygar, silkworm */
	{
		bg_tilemap = tilemap_create(get_bg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,16,16,32,16);
		fg_tilemap = tilemap_create(get_fg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,16,16,32,16);
	}
	tx_tilemap = tilemap_create(get_tx_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT, 8, 8,32,32);

	if (!bg_tilemap || !fg_tilemap || !tx_tilemap)
		return 1;

	bg_tilemap->transparent_pen = 0;
	fg_tilemap->transparent_pen = 0;
	tx_tilemap->transparent_pen = 0;
	/* 0x100 is the background color */
	palette_transparent_color = 0x100;

	tilemap_set_scrolldx(bg_tilemap,-48,256+48);
	tilemap_set_scrolldx(fg_tilemap,-48,256+48);

	return 0;
}



/***************************************************************************

  Memory handlers

***************************************************************************/

WRITE_HANDLER( tecmo_txvideoram_w )
{
	if (tecmo_txvideoram[offset] != data)
	{
		tecmo_txvideoram[offset] = data;
		tilemap_mark_tile_dirty(tx_tilemap,offset & 0x3ff);
	}
}

WRITE_HANDLER( tecmo_fgvideoram_w )
{
	if (tecmo_fgvideoram[offset] != data)
	{
		tecmo_fgvideoram[offset] = data;
		tilemap_mark_tile_dirty(fg_tilemap,offset & 0x1ff);
	}
}

WRITE_HANDLER( tecmo_bgvideoram_w )
{
	if (tecmo_bgvideoram[offset] != data)
	{
		tecmo_bgvideoram[offset] = data;
		tilemap_mark_tile_dirty(bg_tilemap,offset & 0x1ff);
	}
}

WRITE_HANDLER( tecmo_fgscroll_w )
{
	static UINT8 scroll[3];

	scroll[offset] = data;

	tilemap_set_scrollx(fg_tilemap,0,scroll[0] + 256 * scroll[1]);
	tilemap_set_scrolly(fg_tilemap,0,scroll[2]);
}

WRITE_HANDLER( tecmo_bgscroll_w )
{
	static UINT8 scroll[3];

	scroll[offset] = data;

	tilemap_set_scrollx(bg_tilemap,0,scroll[0] + 256 * scroll[1]);
	tilemap_set_scrolly(bg_tilemap,0,scroll[2]);
}

WRITE_HANDLER( tecmo_flipscreen_w )
{
	flip_screen_w(0,data & 1);
}



/***************************************************************************

  Display refresh

***************************************************************************/

static void draw_sprites(struct osd_bitmap *bitmap)
{
	int offs;
	const UINT8 layout[8][8] =
	{
		{0,1,4,5,16,17,20,21},
		{2,3,6,7,18,19,22,23},
		{8,9,12,13,24,25,28,29},
		{10,11,14,15,26,27,30,31},
		{32,33,36,37,48,49,52,53},
		{34,35,38,39,50,51,54,55},
		{40,41,44,45,56,57,60,61},
		{42,43,46,47,58,59,62,63}
	};

	for (offs = spriteram_size-8;offs >= 0;offs -= 8)
	{
		int flags = spriteram[offs+3];
		int priority = flags>>6;
		int bank = spriteram[offs+0];
		if (bank & 4)
		{ /* visible */
			int which = spriteram[offs+1];
			int code,xpos,ypos,flipx,flipy,priority_mask,x,y;
			int size = spriteram[offs + 2] & 3;

			if (tecmo_video_type != 0)	/* gemini, silkworm */
			  code = which + ((bank & 0xf8) << 5);
			else						/* rygar */
			  code = which + ((bank & 0xf0) << 4);

			code &= ~((1 << (size*2)) - 1);
			size = 1 << size;

			xpos = spriteram[offs + 5] - ((flags & 0x10) << 4);
			ypos = spriteram[offs + 4] - ((flags & 0x20) << 3);
			flipx = bank & 1;
			flipy = bank & 2;

			if (flip_screen)
			{
				xpos = 256 - (8 * size) - xpos;
				ypos = 256 - (8 * size) - ypos;
				flipx = !flipx;
				flipy = !flipy;
			}

			/* bg: 1; fg:2; text: 4 */
			switch (priority)
			{
				default:
				case 0x0: priority_mask = 0; break;
				case 0x1: priority_mask = 0xf0; break; /* obscured by text layer */
				case 0x2: priority_mask = 0xf0|0xcc; break;	/* obscured by foreground */
				case 0x3: priority_mask = 0xf0|0xcc|0xaa; break; /* obscured by bg and fg */
			}

			for (y = 0;y < size;y++)
			{
				for (x = 0;x < size;x++)
				{
					int sx = xpos + 8*(flipx?(size-1-x):x);
					int sy = ypos + 8*(flipy?(size-1-y):y);
					pdrawgfx(bitmap,Machine->gfx[1],
							code + layout[y][x],
							flags & 0xf,
							flipx,flipy,
							sx,sy,
							&Machine->visible_area,TRANSPARENCY_PEN,0,
							priority_mask);
				}
			}
		}
	}
}


static void mark_sprite_colors(void)
{
	int i;
	unsigned short palette_map[16];
	int pal_base;

	memset(palette_map,0,sizeof(palette_map));

	for (i = 0;i < spriteram_size;i += 8)
	{
		int color;

		color = spriteram[i+3] & 0x0f;
		palette_map[color] |= 0xffff;
	}

	/* now build the final table */
	pal_base = Machine->drv->gfxdecodeinfo[1].color_codes_start;
	for (i = 0;i < 16;i++)
	{
		int usage = palette_map[i],j;
		if (usage)
		{
			for (j = 1; j < 16; j++)
				if (usage & (1 << j))
					palette_used_colors[pal_base + i * 16 + j] |= PALETTE_COLOR_VISIBLE;
		}
	}
}


void tecmo_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	tilemap_update(ALL_TILEMAPS);

	palette_init_used_colors();
	mark_sprite_colors();
	palette_used_colors[0x100] = PALETTE_COLOR_USED;

	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);

	tilemap_render(ALL_TILEMAPS);

	fillbitmap(priority_bitmap,0,NULL);
	fillbitmap(bitmap,Machine->pens[0x100],&Machine->visible_area);
	tilemap_draw(bitmap,bg_tilemap,1<<16);
	tilemap_draw(bitmap,fg_tilemap,2<<16);
	tilemap_draw(bitmap,tx_tilemap,4<<16);

	draw_sprites(bitmap);
}
