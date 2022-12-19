/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

#include "driver.h"
#include "tilemap.h"
#include "vidhrdw/generic.h"

unsigned char *splash_vregs;
unsigned char *splash_videoram;
unsigned char *splash_spriteram;
unsigned char *splash_pixelram;

static struct tilemap *screen[2];
static struct osd_bitmap *screen2;


/***************************************************************************

	Callbacks for the TileMap code

***************************************************************************/

/*
	Tile format
	-----------

	Screen 0: (64*32, 8x8 tiles)

	Byte | Bit(s)			 | Description
	-----+-FEDCBA98-76543210-+--------------------------
	  0  | -------- xxxxxxxx | sprite code (low 8 bits)
	  0  | ----xxxx -------- | sprite code (high 4 bits)
	  0  | xxxx---- -------- | color

	Screen 1: (32*32, 16x16 tiles)

	Byte | Bit(s)			 | Description
	-----+-FEDCBA98-76543210-+--------------------------
	  0  | -------- -------x | flip y
	  0  | -------- ------x- | flip x
	  0  | -------- xxxxxx-- | sprite code (low 6 bits)
	  0  | ----xxxx -------- | sprite code (high 4 bits)
	  0  | xxxx---- -------- | color
*/

static void get_tile_info_splash_screen0(int tile_index)
{
	int data = READ_WORD(&splash_videoram[2*tile_index]);
	int attr = data >> 8;
	int code = data & 0xff;

	SET_TILE_INFO(0, code + ((0x20 + (attr & 0x0f)) << 8), (attr & 0xf0) >> 4);
}

static void get_tile_info_splash_screen1(int tile_index)
{
	int data = READ_WORD(&splash_videoram[0x1000 + 2*tile_index]);
	int attr = data >> 8;
	int code = data & 0xff;

	tile_info.flags = TILE_FLIPXY(code & 0x03);

	SET_TILE_INFO(1, (code >> 2) + ((0x30 + (attr & 0x0f)) << 6), (attr & 0xf0) >> 4);
}

/***************************************************************************

	Memory Handlers

***************************************************************************/

READ_HANDLER( splash_vram_r )
{
	return READ_WORD(&splash_videoram[offset]);
}

WRITE_HANDLER( splash_vram_w )
{
	COMBINE_WORD_MEM(&splash_videoram[offset],data);
	tilemap_mark_tile_dirty(screen[offset >> 12],(offset & 0x0fff) >> 1);
}

READ_HANDLER( splash_pixelram_r )
{
	return READ_WORD(&splash_pixelram[offset]);
}

WRITE_HANDLER( splash_pixelram_w )
{
	int sx,sy,color;

	COMBINE_WORD_MEM(&splash_pixelram[offset],data);

	sx = (offset >> 1) & 0x1ff;
	sy = (offset >> 10);

	color = READ_WORD(&splash_pixelram[offset]);

	plot_pixel(screen2, sx-9, sy, Machine->pens[0x300 + (color & 0xff)]);
}


/***************************************************************************

	Start the video hardware emulation.

***************************************************************************/

int splash_vh_start(void)
{
	screen[0] = tilemap_create(get_tile_info_splash_screen0,tilemap_scan_rows,TILEMAP_TRANSPARENT, 8, 8,64,32);
	screen[1] = tilemap_create(get_tile_info_splash_screen1,tilemap_scan_rows,TILEMAP_TRANSPARENT,16,16,32,32);
	screen2 = bitmap_alloc (512, 256);

	if (!screen[0] || !screen[1] || !screen[2])
		return 1;

	screen[0]->transparent_pen = 0;
	screen[1]->transparent_pen = 0;

	tilemap_set_scrollx(screen[0], 0, 4);

	return 0;
}

/***************************************************************************

	Sprites

***************************************************************************/

/*
	Sprite Format
	-------------

	Byte | Bit(s)   | Description
	-----+-76543210-+--------------------------
	  0  | xxxxxxxx | sprite number (low 8 bits)
	  1  | xxxxxxxx | y position
	  2  | xxxxxxxx | x position (low 8 bits)
	  3  | ----xxxx | sprite number (high 4 bits)
	  3  | --xx---- | unknown
	  3  | -x------ | flip x
	  3  | x------- | flip y
	  4  | ----xxxx | sprite color
	  4  | -xxx---- | unknown
	  4  | x------- | x position (high bit)
*/

static void splash_draw_sprites(struct osd_bitmap *bitmap)
{
	int i;
	const struct GfxElement *gfx = Machine->gfx[1];

	for (i = 0; i < 0x800; i += 8){
		int sx = READ_WORD(&splash_spriteram[i+4]) & 0xff;
		int sy = (240 - (READ_WORD(&splash_spriteram[i+2]) & 0xff)) & 0xff;
		int attr = READ_WORD(&splash_spriteram[i+6]) & 0xff;
		int attr2 = READ_WORD(&splash_spriteram[i+0x800]) >> 8;
		int number = (READ_WORD(&splash_spriteram[i]) & 0xff) + (attr & 0xf)*256;

		if (attr2 & 0x80) sx += 256;

		drawgfx(bitmap,gfx,number,
			0x10 + (attr2 & 0x0f),attr & 0x40,attr & 0x80,
			sx-8,sy,
			&Machine->visible_area,TRANSPARENCY_PEN,0);
	}
}

/***************************************************************************

	Display Refresh

***************************************************************************/

void splash_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	/* set scroll registers */
	tilemap_set_scrolly(screen[0], 0, READ_WORD(&splash_vregs[0]));
	tilemap_set_scrolly(screen[1], 0, READ_WORD(&splash_vregs[2]));

	tilemap_update(ALL_TILEMAPS);

	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);

	tilemap_render(ALL_TILEMAPS);

	copybitmap(bitmap,screen2,0,0,0,0,&Machine->visible_area,TRANSPARENCY_NONE,0);

	tilemap_draw(bitmap,screen[1],0);
	splash_draw_sprites(bitmap);
	tilemap_draw(bitmap,screen[0],0);
}
