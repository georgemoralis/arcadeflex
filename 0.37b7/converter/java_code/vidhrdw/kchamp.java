/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class kchamp
{
	
	/* prototypes */
	void kchamp_vs_drawsprites( struct osd_bitmap *bitmap );
	void kchamp_1p_drawsprites( struct osd_bitmap *bitmap );
	
	
	
	typedef void (*kchamp_drawspritesproc)( struct osd_bitmap * );
	
	static kchamp_drawspritesproc kchamp_drawsprites;
	
	
	/***************************************************************************
	  Video hardware start.
	***************************************************************************/
	
	public static VhStartPtr kchampvs_vh_start = new VhStartPtr() { public int handler()  {
	
		kchamp_drawsprites = kchamp_vs_drawsprites;
	
		return generic_vh_start();
	} };
	
	public static VhStartPtr kchamp1p_vh_start = new VhStartPtr() { public int handler()  {
	
		kchamp_drawsprites = kchamp_1p_drawsprites;
	
		return generic_vh_start();
	} };
	
	/***************************************************************************
	  Convert color prom.
	***************************************************************************/
	public static VhConvertColorPromPtr kchamp_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
	        int i, red, green, blue;
	        #define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
	        #define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
	                red = color_prom.read(i);
	                green = color_prom.read(Machine->drv->total_colors+i);
	                blue = color_prom.read(2*Machine->drv->total_colors+i);
	
	
	                *(palette++) = red*0x11;
	                *(palette++) = green*0x11;
	                *(palette++) = blue*0x11;
	
	
	                *(colortable++) = i;
		}
	
	} };
	
	void kchamp_vs_drawsprites( struct osd_bitmap *bitmap ) {
	
		int offs;
		        /*
	                Sprites
	                -------
	                Offset          Encoding
	                  0             YYYYYYYY
	                  1             TTTTTTTT
	                  2             FGGTCCCC
	                  3             XXXXXXXX
	        */
	
	        for (offs = 0 ;offs < 0x100;offs+=4)
		{
	                int numtile = spriteram.read(offs+1)+ ( ( spriteram.read(offs+2)& 0x10 ) << 4 );
	                int flipx = ( spriteram.read(offs+2)& 0x80 );
	                int sx, sy;
	                int gfx = 1 + ( ( spriteram.read(offs+2)& 0x60 ) >> 5 );
	                int color = ( spriteram.read(offs+2)& 0x0f );
	
	                sx = spriteram.read(offs+3);
	                sy = 240 - spriteram.read(offs);
	
	                drawgfx(bitmap,Machine.gfx[gfx],
	                                numtile,
	                                color,
	                                0, flipx,
	                                sx,sy,
	                                &Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	}
	
	void kchamp_1p_drawsprites( struct osd_bitmap *bitmap ) {
	
		int offs;
		        /*
	                Sprites
	                -------
	                Offset          Encoding
	                  0             YYYYYYYY
	                  1             TTTTTTTT
	                  2             FGGTCCCC
	                  3             XXXXXXXX
	        */
	
	        for (offs = 0 ;offs < 0x100;offs+=4)
		{
	                int numtile = spriteram.read(offs+1)+ ( ( spriteram.read(offs+2)& 0x10 ) << 4 );
	                int flipx = ( spriteram.read(offs+2)& 0x80 );
	                int sx, sy;
	                int gfx = 1 + ( ( spriteram.read(offs+2)& 0x60 ) >> 5 );
	                int color = ( spriteram.read(offs+2)& 0x0f );
	
	                sx = spriteram.read(offs+3)- 8;
	                sy = 247 - spriteram.read(offs);
	
	                drawgfx(bitmap,Machine.gfx[gfx],
	                                numtile,
	                                color,
	                                0, flipx,
	                                sx,sy,
	                                &Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	}
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	
	public static VhUpdatePtr kchamp_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
	        int offs;
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
	        for ( offs = videoram_size[0] - 1; offs >= 0; offs-- ) {
	                if ( dirtybuffer[offs] ) {
				int sx,sy,code;
	
				dirtybuffer[offs] = 0;
	
	                        sx = (offs % 32);
				sy = (offs / 32);
	
	                        code = videoram.read(offs)+ ( ( colorram.read(offs)& 7 ) << 8 );
	
	                        drawgfx(tmpbitmap,Machine.gfx[0],
	                                        code,
	                                        ( colorram.read(offs)>> 3 ) & 0x1f,
	                                        0, /* flip x */
	                                        0, /* flip y */
						sx*8,sy*8,
						&Machine.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	
		/* copy the character mapped graphics */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
	
		(*kchamp_drawsprites)( bitmap);
	} };
}
