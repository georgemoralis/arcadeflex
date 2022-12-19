/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class galpanic
{
	
	
	
	UBytePtr galpanic_bgvideoram,*galpanic_fgvideoram;
	size_t galpanic_fgvideoram_size;
	
	
	
	public static VhConvertColorPromPtr galpanic_init_palette = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
		palette += 3*1024;	/* first 1024 colors are dynamic */
	
		/* initialize 555 RGB lookup */
		for (i = 0;i < 32768;i++)
		{
			int r,g,b;
	
			r = (i >>  5) & 0x1f;
			g = (i >> 10) & 0x1f;
			b = (i >>  0) & 0x1f;
	
			(*palette++) = (r << 3) | (r >> 2);
			(*palette++) = (g << 3) | (g >> 2);
			(*palette++) = (b << 3) | (b >> 2);
		}
	} };
	
	
	
	public static ReadHandlerPtr galpanic_bgvideoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return READ_WORD(&galpanic_bgvideoram[offset]);
	} };
	
	public static WriteHandlerPtr galpanic_bgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int sx,sy,color;
	
	
		COMBINE_WORD_MEM(&galpanic_bgvideoram[offset],data);
	
		sy = (offset/2) / 256;
		sx = (offset/2) % 256;
	
		color = READ_WORD(&galpanic_bgvideoram[offset]);
	
		plot_pixel(tmpbitmap, sx, sy, Machine.pens[1024 + (color >> 1)]);
	} };
	
	public static ReadHandlerPtr galpanic_fgvideoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return READ_WORD(&galpanic_fgvideoram[offset]);
	} };
	
	public static WriteHandlerPtr galpanic_fgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(&galpanic_fgvideoram[offset],data);
	} };
	
	public static ReadHandlerPtr galpanic_paletteram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return READ_WORD(&paletteram.read(offset));
	} };
	
	public static WriteHandlerPtr galpanic_paletteram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int r,g,b;
		int oldword = READ_WORD(&paletteram.read(offset));
		int newword = COMBINE_WORD(oldword,data);
	
	
		WRITE_WORD(&paletteram.read(offset),newword);
	
		r = (newword >>  6) & 0x1f;
		g = (newword >> 11) & 0x1f;
		b = (newword >>  1) & 0x1f;
		/* bit 0 seems to be a transparency flag for the front bitmap */
	
		r = (r << 3) | (r >> 2);
		g = (g << 3) | (g >> 2);
		b = (b << 3) | (b >> 2);
	
		palette_change_color(offset / 2,r,g,b);
	} };
	
	public static ReadHandlerPtr galpanic_spriteram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return READ_WORD(&spriteram.read(offset));
	} };
	
	public static WriteHandlerPtr galpanic_spriteram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(&spriteram.read(offset),data);
	} };
	
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	
	static void galpanic_draw_sprites(struct osd_bitmap *bitmap)
	{
		int offs;
		int sx,sy;
	
		sx = sy = 0;
		for (offs = 0;offs < spriteram_size;offs += 0x10)
		{
			int x,y,code,color,flipx,flipy,attr1,attr2;
	
			attr1 = READ_WORD(&spriteram.read(offs + 6));
			x = READ_WORD(&spriteram.read(offs + 8)) - ((attr1 & 0x01) << 8);
			y = READ_WORD(&spriteram.read(offs + 10)) + ((attr1 & 0x02) << 7);
			if ((attr1 & 0x04) != 0)	/* multi sprite */
			{
				sx += x;
				sy += y;
			}
			else
			{
				sx = x;
				sy = y;
			}
	
			color = (attr1 & 0xf0) >> 4;
	
			/* bit 0 [offs + 0] is used but I don't know what for */
	
			attr2 = READ_WORD(&spriteram.read(offs + 14));
			code = READ_WORD(&spriteram.read(offs + 12)) + ((attr2 & 0x1f) << 8);
			flipx = attr2 & 0x80;
			flipy = attr2 & 0x40;
	
			drawgfx(bitmap,Machine.gfx[0],
					code,
					color,
					flipx,flipy,
					sx,sy - 16,
					&Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	}
	
	static void comad_draw_sprites(struct osd_bitmap *bitmap)
	{
		int offs;
	
		for (offs = 0;offs < spriteram_size;offs += 8)
		{
			int sx,sy,code,color,flipx,flipy;
	
			sx = READ_WORD(&spriteram.read(offs + 4)) >> 6;
			sy = READ_WORD(&spriteram.read(offs + 6)) >> 6;
			code = READ_WORD(&spriteram.read(offs + 2));
			color = (READ_WORD(&spriteram.read(offs)) & 0x003c) >> 2;
			flipx = READ_WORD(&spriteram.read(offs)) & 0x0002;
			flipy = READ_WORD(&spriteram.read(offs)) & 0x0001;
	
			drawgfx(bitmap,Machine.gfx[0],
					code,
					color,
					flipx,flipy,
					sx,sy,
					&Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	}
	
	static void draw_fgbitmap(struct osd_bitmap *bitmap)
	{
		int offs;
	
		for (offs = 0;offs < galpanic_fgvideoram_size;offs+=2)
		{
			int sx,sy,color;
	
			sx = (offs/2) % 256;
			sy = (offs/2) / 256;
			color = READ_WORD(&galpanic_fgvideoram[offs]);
			if (color != 0)
			{
				plot_pixel(bitmap, sx, sy, Machine.pens[color]);
			}
		}
	}
	
	public static VhUpdatePtr galpanic_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		palette_recalc();
	
		/* copy the temporary bitmap to the screen */
		/* it's raw RGB, so it doesn't have to be recalculated even if palette_recalc() */
		/* returns true */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
	
		draw_fgbitmap(bitmap);
	
		galpanic_draw_sprites(bitmap);
	} };
	
	public static VhUpdatePtr comad_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		palette_recalc();
	
		/* copy the temporary bitmap to the screen */
		/* it's raw RGB, so it doesn't have to be recalculated even if palette_recalc() */
		/* returns true */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
	
		draw_fgbitmap(bitmap);
	
		comad_draw_sprites(bitmap);
	} };
}
