/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.vidhrdw;

import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;

public class gaelco
{
	
	public static UBytePtr splash_vregs=new UBytePtr();
	public static UBytePtr splash_videoram=new UBytePtr();
	public static UBytePtr splash_spriteram=new UBytePtr();
	public static UBytePtr splash_pixelram=new UBytePtr();
	
	static  tilemap screen0, screen1;
	static osd_bitmap screen2;
	
	/***************************************************************************
	
		Palette
	
	***************************************************************************/
	
	public static WriteHandlerPtr paletteram_xRRRRxGGGGxBBBBx_word_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int r,g,b;
		int oldword = paletteram.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword,data);
	
		paletteram.WRITE_WORD(offset,newword);
	
		r = (newword >> 11) & 0x0f;
		g = (newword >>  6) & 0x0f;
		b = (newword >>  1) & 0x0f;
	
		r = (r << 4) | r;
		g = (g << 4) | g;
		b = (b << 4) | b;
	
		palette_change_color(offset >> 1, r, g, b);
	} };
	
	/***************************************************************************
	
		Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static WriteHandlerPtr get_tile_info_screen0 = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int tile_index = 2*(64*row + col);
		char data = (char)splash_videoram.READ_WORD(tile_index);
		char attr = (char)((data >> 8)&0xFF);
		char code = (char)(data & 0xff);
	
		SET_TILE_INFO(0, code + ((0x20 + (attr & 0x0f)) << 8), (attr & 0xf0) >> 4);
	} };
	
	public static WriteHandlerPtr get_tile_info_screen1 = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int tile_index = 2*(32*row + col);
		char data = (char)splash_videoram.READ_WORD(tile_index+0x1000);
		char attr = (char)((data >> 8)&0xFF);
		char code = (char)(data & 0xff);
	
		tile_info.flags = (char)TILE_FLIPXY(code & 0x03);
	
		SET_TILE_INFO(1, (code >> 2) + ((0x30 + (attr & 0x0f)) << 6), (attr & 0xf0) >> 4);
	} };
	
	/***************************************************************************
	
		Memory Handlers
	
	***************************************************************************/
	
	public static ReadHandlerPtr splash_vram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return splash_videoram.READ_WORD(offset);
	} };
	
	public static WriteHandlerPtr splash_vram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(splash_videoram,offset,data);
		if (offset < 0x1000){	/* Screen 0 */
			tilemap_mark_tile_dirty(screen0,(offset/2)%64,(offset/2)/64);
		}
		else{	/* Screen 1 */
			offset -= 0x1000;
			tilemap_mark_tile_dirty(screen1,(offset/2)%32,(offset/2)/32);
		}
	} };
	
	public static ReadHandlerPtr splash_pixelram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return splash_pixelram.READ_WORD(offset);
	} };
	
	public static WriteHandlerPtr splash_pixelram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int sx,sy,color;
	
		COMBINE_WORD_MEM(splash_pixelram,offset,data);
	
		sx = (offset >> 1) & 0x1ff;	//(offset/2) % 512;
		sy = (offset >> 10);		//(offset/2) / 512;
	
		color = splash_pixelram.READ_WORD(offset);
	
		plot_pixel.handler(screen2, sx-9, sy, Machine.pens[0x300 + (color & 0xff)]);
	} };
	
	/***************************************************************************
	
		Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr splash_vh_start = new VhStartPtr() { public int handler() 
	{
		screen0 = tilemap_create(get_tile_info_screen0, TILEMAP_TRANSPARENT, 8, 8, 64, 32);
		screen1 = tilemap_create(get_tile_info_screen1, TILEMAP_TRANSPARENT, 16, 16, 32, 32);
		screen2 = osd_create_bitmap (512, 256);
	
		if (screen0!=null && screen1!=null && screen2!=null){
			screen0.transparent_pen = 0;
			screen1.transparent_pen = 0;
	
			tilemap_set_scrollx(screen0, 0, 4);
	
			return 0;
		}
		return 1;
	} };
	
	
	/***************************************************************************
	
		Display Refresh
	
	***************************************************************************/
	
	/*
	 * Sprite Format
	 * -------------
	 *
	 * Byte | Bit(s)   | Description
	 * -----+-76543210-+--------------------------
	 *   0  | xxxxxxxx | sprite code (low 8 bits)
	 *   2  | xxxxxxxx | y position
	 *   4  | xxxxxxxx | x position (low 8 bits)
	 *   6  | x------- | flip y
	 *   6  | -x------ | flip x
	 *   6  | --xx---- | unknown
	 *   6  | ----xxxx | sprite code (high 4 bits)
	 * 801	| x------- | x position (high bit)
	 * 801	| -xxx---- | unknown
	 * 801	| ----xxxx | color
	*/
	
	static void draw_sprites(osd_bitmap bitmap)
	{
		int i;
		GfxElement gfx = Machine.gfx[1];
	
		for (i = 0; i < 0x800; i += 8){
			int sx = splash_spriteram.READ_WORD(i+4) & 0xff;						/* x position */
			int sy = 256 - (splash_spriteram.READ_WORD(i+2) & 0xff);				/* y position */
			int attr = splash_spriteram.READ_WORD(i+6) & 0xff;					/* attributes */
			int attr2 = splash_spriteram.READ_WORD(i+0x800) >> 8;				/* attributes 2 */
			int number = (splash_spriteram.READ_WORD(i) & 0xff) + (attr & 0xf)*256;/* sprite number */
	
			if ((attr2 & 0x80) != 0) sx += 256;
	
			drawgfx(bitmap,gfx,
				number,
				0x10 + (attr2 & 0x0f),
				attr & 0x40, attr & 0x80,
				sx-8,sy-16,
				Machine.drv.visible_area,
				TRANSPARENCY_PEN,0);
		}
	}
	
	public static VhUpdatePtr splash_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		/* set scroll registers */
		tilemap_set_scrolly(screen0, 0, splash_vregs.READ_WORD(0));
		tilemap_set_scrolly(screen1, 0, splash_vregs.READ_WORD(2));
	
		tilemap_update(ALL_TILEMAPS);
	
		if (palette_recalc()!=null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		copybitmap(bitmap,screen2,0,0,0,0,Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	
		tilemap_draw(bitmap,screen1,0);
		draw_sprites(bitmap);
		tilemap_draw(bitmap,screen0,0);
	} };
}
