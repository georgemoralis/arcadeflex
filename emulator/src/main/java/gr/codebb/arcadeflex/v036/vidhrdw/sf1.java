/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.vidhrdw;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;


public class sf1
{
	
	public static UBytePtr sf1_objectram=new UBytePtr();
	
	public static int sf1_active = 0;
	
	static tilemap bgb_tilemap, bgm_tilemap, char_tilemap;
        
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static WriteHandlerPtr get_bgb_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int addr = (col*32 + row*2 + 128) & 0xffff;
		UBytePtr base = new UBytePtr(memory_region(REGION_GFX5), addr);
		int attr = base.read(65536);
		int color = base.read(0);
		int code = (base.read(65537)<<8) | base.read(1);
		SET_TILE_INFO (0, code, color);
		tile_info.flags = (char)TILE_FLIPYX(attr & 3);
	} };
	
	public static WriteHandlerPtr get_bgm_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int addr = (col*32 + row*2 + 128) & 0xffff;
		UBytePtr base = new UBytePtr(memory_region(REGION_GFX5), addr + 65536*2);
		int attr = base.read(65536);
		int color = base.read(0);
		int code = (base.read(65537)<<8) | base.read(1);
		SET_TILE_INFO (1, code, color);
		tile_info.flags = (char)TILE_FLIPYX(attr & 3);
	} };
	
	public static WriteHandlerPtr get_char_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int addr = 16 + col*2 + row*128;
		int code = videoram.READ_WORD(addr);
		SET_TILE_INFO (3, code & 0x3ff, code>>12);
		tile_info.flags = (char)TILE_FLIPYX((code & 0xc00)>>10);
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartHandlerPtr sf1_vh_start = new VhStartHandlerPtr() { public int handler() 
	{
		int i;
	
		bgb_tilemap = tilemap_create (get_bgb_tile_info,
									  TILEMAP_OPAQUE,
									  16, 16,
									  2048, 16);
		bgm_tilemap = tilemap_create (get_bgm_tile_info,
									  TILEMAP_TRANSPARENT,
									  16, 16,
									  2048, 16);
		char_tilemap = tilemap_create (get_char_tile_info,
									   TILEMAP_TRANSPARENT,
									   8, 8,
									   48, 32);
	
		if(bgb_tilemap==null || bgm_tilemap==null || char_tilemap==null)
			return 1;
	
		bgm_tilemap.transparent_pen = 15;
		char_tilemap.transparent_pen = 3;
	
		for(i = 832; i<1024; i++)
			palette_used_colors.write(i, PALETTE_COLOR_UNUSED);
	
		return 0;
	} };
	
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr sf1_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int old = videoram.READ_WORD(offset);
		int _new = COMBINE_WORD(old, data);
		if(old!=_new) {
			int x;
			videoram.WRITE_WORD(offset, _new);
			offset /= 2;
			x = offset%64;
			if(x>=8 && x<56)
				tilemap_mark_tile_dirty (char_tilemap, x-8, offset/64);
		}
	} };
	
	public static WriteHandlerPtr sf1_deltaxb_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		tilemap_set_scrollx(bgb_tilemap, 0, data);
	} };
	
	public static WriteHandlerPtr sf1_deltaxm_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		tilemap_set_scrollx(bgm_tilemap, 0, data);
	} };
	
	public static void sf1_active_w(int data)
	{
		sf1_active = data;
		tilemap_set_enable(bgb_tilemap, data & 0x20);
		tilemap_set_enable(bgm_tilemap, data & 0x40);
		tilemap_set_enable(char_tilemap, data & 0x08);
	}
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	static int delta[] = {0x00, 0x18, 0x18, 0x00};
	public static ReadHandlerPtr sf1_invert = new ReadHandlerPtr() { public int handler(int nb)
	{
		
		return nb^delta[(nb>>3)&3];
	} };
	
	
	static void mark_sprites_palette()
	{
		//UBytePtr umap = &palette_used_colors[Machine.drv.gfxdecodeinfo[2].color_codes_start];
                UBytePtr umap = new UBytePtr(palette_used_colors,Machine.drv.gfxdecodeinfo[2].color_codes_start);
		/*unsigned*/ int cmap = 0;
		UBytePtr pt = new UBytePtr(sf1_objectram , 0x2000-0x40);
		int i, j;
	
		while(pt.offset>=sf1_objectram.offset)
		{
			int at = pt.READ_WORD(2);
			int y = pt.READ_WORD(4);
			int x = pt.READ_WORD(6);
	
			if(x>32 && x<415 && y>0 && y<256)
				cmap |= (1<<(at & 0x0f));
	
			pt.offset -= 0x40;
		}
	
		for(i=0;i<16;i++)
		{
			if((cmap & (1<<i))!=0)
			{
				for(j=0;j<15;j++)
					umap.writeinc(PALETTE_COLOR_USED);
				umap.writeinc(PALETTE_COLOR_TRANSPARENT);
			}
			else
			{
				for(j=0;j<16;j++)
					umap.writeinc(PALETTE_COLOR_UNUSED);
			}
		}
	}
	
	static void draw_sprites(osd_bitmap bitmap)
	{
		UBytePtr pt = new UBytePtr(sf1_objectram, 0x2000-0x40);
	
		while(pt.offset>=sf1_objectram.offset) {
			int c = pt.READ_WORD(0);
			int at = pt.READ_WORD(2);
			int y = pt.READ_WORD(4);
			int x = pt.READ_WORD(6);
	
			if(x>32 && x<415 && y>0 && y<256) {
				x -= 64;
	
				if((at&0x400)==0) {
					drawgfx(bitmap,
							Machine.gfx[2],
							sf1_invert.handler(c),
							at & 0xf,
							at & 0x100, at & 0x200,
							x, y,
							Machine.drv.visible_area, TRANSPARENCY_PEN, 15);
				} else {
					int c1, c2, c3, c4;
					switch(at & 0x300) {
					case 0x000:
					default:
						c1 = c;
						c2 = c+1;
						c3 = c+16;
						c4 = c+17;
						break;
					case 0x100:
						c1 = c+1;
						c2 = c;
						c3 = c+17;
						c4 = c+16;
						break;
					case 0x200:
						c1 = c+16;
						c2 = c+17;
						c3 = c;
						c4 = c+1;
						break;
					case 0x300:
						c1 = c+17;
						c2 = c+16;
						c3 = c+1;
						c4 = c;
						break;
					}
					drawgfx(bitmap,
							Machine.gfx[2],
							sf1_invert.handler(c1),
							at & 0xf,
							at & 0x100, at & 0x200,
							x, y,
							Machine.drv.visible_area, TRANSPARENCY_PEN, 15);
					drawgfx(bitmap,
							Machine.gfx[2],
							sf1_invert.handler(c2),
							at & 0xf,
							at & 0x100, at & 0x200,
							x+16, y,
							Machine.drv.visible_area, TRANSPARENCY_PEN, 15);
					drawgfx(bitmap,
							Machine.gfx[2],
							sf1_invert.handler(c3),
							at & 0xf,
							at & 0x100, at & 0x200,
							x, y+16,
							Machine.drv.visible_area, TRANSPARENCY_PEN, 15);
					drawgfx(bitmap,
							Machine.gfx[2],
							sf1_invert.handler(c4),
							at & 0xf,
							at & 0x100, at & 0x200,
							x+16, y+16,
							Machine.drv.visible_area, TRANSPARENCY_PEN, 15);
				}
			}
			pt.offset -= 0x40;
		}
	}
	
	public static VhUpdateHandlerPtr sf1_vh_screenrefresh = new VhUpdateHandlerPtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_update(ALL_TILEMAPS);
	
		palette_init_used_colors();
	
		if ((sf1_active & 0x80) != 0)
			mark_sprites_palette();
	
		if (palette_recalc()!=null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		tilemap_draw(bitmap, bgb_tilemap, 0);
	
		if((sf1_active & 0x20)==0)
			fillbitmap(bitmap,palette_transparent_pen,Machine.drv.visible_area);
	
		tilemap_draw(bitmap, bgm_tilemap, 0);
	
		if ((sf1_active & 0x80) != 0)
			draw_sprites(bitmap);
	
		tilemap_draw(bitmap, char_tilemap, 0);
	}};
}
