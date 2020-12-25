/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.vidhrdw;
import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;


public class ddragon
{
	
	public static UBytePtr dd_videoram=new UBytePtr();
	public static int dd_scrollx_hi, dd_scrolly_hi;
	public static UBytePtr dd_scrollx_lo=new UBytePtr();
	public static UBytePtr dd_scrolly_lo=new UBytePtr();
	public static UBytePtr dd_spriteram=new UBytePtr();
	public static int dd2_video;
	
	
	public static VhStartPtr dd_vh_start = new VhStartPtr() { public int handler() 
	{
		dirtybuffer = new char[0x400];
		if (dirtybuffer != null)
		{
			memset(dirtybuffer,1, 0x400);
	
			tmpbitmap = osd_new_bitmap(
					Machine.drv.screen_width*2,
					Machine.drv.screen_height*2,
					Machine.scrbitmap.depth );
	
			if (tmpbitmap != null) return 0;
	
			dirtybuffer=null;
		}
	
		return 1;
	}};
	
	
	public static VhStopPtr dd_vh_stop = new VhStopPtr() { public void handler() 
	{
		osd_free_bitmap( tmpbitmap );
		 dirtybuffer=null;
	}};
	
	
	public static WriteHandlerPtr dd_background_w = new WriteHandlerPtr() { public void handler(int offset, int val)
	{
		if( dd_videoram.read(offset) != val ){
			dd_videoram.write(offset,val);
			dirtybuffer[offset/2] = 1;
		}
	} };
	
	
	static void dd_draw_background(osd_bitmap bitmap )
	{
		GfxElement gfx = Machine.gfx[2];
	
		int scrollx = -dd_scrollx_hi - ( dd_scrollx_lo.read(0) );
		int scrolly = -dd_scrolly_hi - ( dd_scrolly_lo.read(0) );
	
		int offset;
	
		for( offset = 0; offset<0x400; offset++ ){
			int attributes = dd_videoram.read(offset*2);
			int color = ( attributes >> 3 ) & 0x7;
			if( dirtybuffer[offset]!=0 ){
				int tile_number = dd_videoram.read(offset*2+1) + ((attributes&7)<<8);
				int xflip = attributes & 0x40;
				int yflip = attributes & 0x80;
				int sx = 16*(((offset>>8)&1)*16 + (offset&0xff)%16);
				int sy = 16*(((offset>>9)&1)*16 + (offset&0xff)/16);
	
				/* CALB ????
	                          if( sx<0 || sx>=512 || sy<0 || sy>=512 ) ExitToShell();*/
	
				drawgfx(tmpbitmap,gfx,
					tile_number,
					color,
					xflip,yflip,
					sx,sy,
					null,TRANSPARENCY_NONE,0);
	
				dirtybuffer[offset] = 0;
			}
		}
	
		copyscrollbitmap(bitmap,tmpbitmap,
				1,new int[]{scrollx},1,new int[]{scrolly},
				Machine.drv.visible_area,
				TRANSPARENCY_NONE,0);
	}
	
	/*static void DRAW_SPRITE( int order, int sx, int sy ) 
        {
            drawgfx( bitmap, gfx,
						(which+order),color,flipx,flipy,sx,sy, \
						clip,TRANSPARENCY_PEN,0);
        }*/
        
	static void dd_draw_sprites(osd_bitmap bitmap )
	{
		rectangle clip = Machine.drv.visible_area;
		GfxElement gfx = Machine.gfx[1];
	
		UBytePtr src = new UBytePtr(dd_spriteram,0x800 );
		int i;
	
		for( i = 0; i < ( 64 * 5 ); i += 5 ) {
			int attr = src.read(i+1);
			if ((attr & 0x80) != 0) { /* visible */
				int sx = 240 - src.read(i+4) + ( ( attr & 2 ) << 7 );
				int sy = 240 - src.read(i+0) + ( ( attr & 1 ) << 8 );
				int size = ( attr & 0x30 ) >> 4;
				int flipx = ( attr & 8 );
				int flipy = ( attr & 4 );
	
				int which;
				int color;
	
				if (dd2_video != 0) {
					color = ( src.read(i+2) >> 5 );
					which = src.read(i+3) + ( ( src.read(i+2) & 0x1f ) << 8 );
				} else {
					color = ( src.read(i+2) >> 4 ) & 0x07;
					which = src.read(i+3) + ( ( src.read(i+2) & 0x0f ) << 8 );
				}
	
				switch ( size ) {
					case 0: /* normal */
					//DRAW_SPRITE( 0, sx, sy );
                                        drawgfx( bitmap, gfx,(which+0),color,flipx,flipy,sx,sy,clip,TRANSPARENCY_PEN,0);
					break;
	
					case 1: /* double y */
					//DRAW_SPRITE( 0, sx, sy - 16 );
					//DRAW_SPRITE( 1, sx, sy );
                                        drawgfx( bitmap, gfx,(which+0),color,flipx,flipy,sx,sy - 16,clip,TRANSPARENCY_PEN,0);
                                        drawgfx( bitmap, gfx,(which+1),color,flipx,flipy,sx,sy,clip,TRANSPARENCY_PEN,0);                                   
					break;
	
					case 2: /* double x */
					//DRAW_SPRITE( 0, sx - 16, sy );
					//DRAW_SPRITE( 2, sx, sy );
                                        drawgfx( bitmap, gfx,(which+0),color,flipx,flipy,sx - 16,sy,clip,TRANSPARENCY_PEN,0);
                                        drawgfx( bitmap, gfx,(which+2),color,flipx,flipy,sx,sy,clip,TRANSPARENCY_PEN,0);
					break;
	
					case 3:
					//DRAW_SPRITE( 0, sx - 16, sy - 16 );
					//DRAW_SPRITE( 1, sx - 16, sy );
					//DRAW_SPRITE( 2, sx, sy - 16 );
					//DRAW_SPRITE( 3, sx, sy );
                                        drawgfx( bitmap, gfx,(which+0),color,flipx,flipy,sx - 16,sy - 16,clip,TRANSPARENCY_PEN,0);
                                        drawgfx( bitmap, gfx,(which+1),color,flipx,flipy,sx - 16,sy,clip,TRANSPARENCY_PEN,0);
                                        drawgfx( bitmap, gfx,(which+2),color,flipx,flipy,sx,sy - 16,clip,TRANSPARENCY_PEN,0);
                                        drawgfx( bitmap, gfx,(which+3),color,flipx,flipy,sx,sy,clip,TRANSPARENCY_PEN,0);
					break;
				}
			}
		}
	}

	
	static void dd_draw_foreground( osd_bitmap bitmap )
	{
		GfxElement gfx = Machine.gfx[0];
		UBytePtr source = new UBytePtr(videoram,0);
	
		int sx,sy;
	
		for( sy=0; sy<256; sy+=8 ){
			for( sx=0; sx<256; sx+=8 ){
				int attributes = source.read(0);
				int tile_number = source.read(1) + 256*( attributes & 7 );
				int color = ( attributes >> 5 ) & 0x7;
	
				if (tile_number != 0) {
					drawgfx( bitmap,gfx, tile_number,
					color,
					0,0, /* no flip */
					sx,sy,
					null, /* no need to clip */
					TRANSPARENCY_PEN,0);
				}
				source.offset+=2;
			}
		}
	}
	
	
	
	public static VhUpdatePtr dd_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		if (palette_recalc()!=null)
	 		memset(dirtybuffer,1, 0x400);
	
		dd_draw_background( bitmap );
		dd_draw_sprites( bitmap );
		dd_draw_foreground( bitmap );
	} };
}
