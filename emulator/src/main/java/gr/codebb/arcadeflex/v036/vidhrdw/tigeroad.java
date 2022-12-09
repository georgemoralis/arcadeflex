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
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static common.libc.expressions.NOT;
import static arcadeflex.v036.mame.commonH.REGION_GFX4;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;

public class tigeroad
{
	
	static UBytePtr tigeroad_scrollram=new UBytePtr(4);
	static int flipscreen,bgcharbank;
	
	
	
	public static WriteHandlerPtr tigeroad_videoctrl_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if ((data & 0xff000000) == 0)
		{
			data = (data >> 8) & 0xff;
	
			/* bit 1 flips screen */
			flipscreen = data & 0x02;
	
			/* bit 2 selects bg char bank */
			bgcharbank = (data & 0x04) >> 2;
	
			/* bits 4-5 are unknown, but used */
	
			/* bits 6-7 are coin counters */
			coin_counter_w.handler(0,data & 0x40);
			coin_counter_w.handler(1,data & 0x80);
		}
	} };
	
	public static WriteHandlerPtr tigeroad_scroll_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(tigeroad_scrollram,offset,data);
	} };
	
	
	static void render_background(osd_bitmap bitmap, int priority )
	{
		int scrollx = 	tigeroad_scrollram.READ_WORD(0) & 0xfff; /* 0..4096 */
		int scrolly =	tigeroad_scrollram.READ_WORD(2) & 0xfff; /* 0..4096 */
	
		UBytePtr p = memory_region(REGION_GFX4);
	
		int alignx = scrollx%32;
		int aligny = scrolly%32;
	
		int row = scrolly/32;	/* 0..127 */
		int sy = 224+aligny;
	
		int transp0,transp1;
	
		if (priority != 0){ /* foreground */
			transp0 = 0xFFFF;	/* draw nothing (all pens transparent) */
			transp1 = 0x01FF;	/* high priority half of tile */
		}
		else { /* background */
			transp0 = 0;		/* NO_TRANSPARENCY */
			transp1 = 0xFE00;	/* low priority half of tile */
		}
	
		while( sy>-32 ){
			int col = scrollx/32;	/* 0..127 */
			int sx = -alignx;
	
			while( sx<256 ){
				int offset = 2*(col%8) + 16*(row%8) + 128*(col/8) + 2048*(row/8);
	
				int code = p.read(offset);
				int attr = p.read(offset+1);
	
				int flipx = attr & 0x20;
				int flipy = 0;
				int color = attr & 0x0f;
	
				if (flipscreen != 0)
					drawgfx(bitmap,Machine.gfx[1],
							code + ((attr & 0xc0) << 2) + (bgcharbank << 10),
							color,
							NOT(flipx),NOT(flipy),
							224-sx,224-sy,
							Machine.drv.visible_area,
							TRANSPARENCY_PENS,(attr & 0x10)!=0 ? transp1 : transp0);
				else
					drawgfx(bitmap,Machine.gfx[1],
							code + ((attr & 0xc0) << 2) + (bgcharbank << 10),
							color,
							flipx,flipy,
							sx,sy,
							Machine.drv.visible_area,
							TRANSPARENCY_PENS,(attr & 0x10)!=0 ? transp1 : transp0);
	
				sx+=32;
				col++;
				if( col>=128 ) col-=128;
			}
			sy-=32;
			row++;
			if( row>=128 ) row-=128;
		}
	}
	
	static void render_sprites(osd_bitmap bitmap )
	{
		UBytePtr source = new UBytePtr(spriteram,spriteram_size[0] - 8);
		UBytePtr finish = new UBytePtr(spriteram);
	
		while( source.offset>=finish.offset )
		{
			int tile_number = source.READ_WORD(0);
			if( tile_number!=0xFFF ){
				int attributes = source.READ_WORD(2);
				int sy = source.READ_WORD(4) & 0x1ff;
				int sx = source.READ_WORD(6) & 0x1ff;
	
				int flipx = attributes&2;
				int flipy = attributes&1;
				int color = (attributes>>2)&0xf;
	
				if( sx>0x100 ) sx -= 0x200;
				if( sy>0x100 ) sy -= 0x200;
	
				if (flipscreen != 0)
				{
					sx = 240 - sx;
					sy = 240 - sy;
					flipx = NOT(flipx);
					flipy = NOT(flipy);
				}
	
				drawgfx(bitmap,Machine.gfx[2],
					tile_number,
					color,
					flipx,flipy,
					sx,240-sy,
					Machine.drv.visible_area,
					TRANSPARENCY_PEN,15);
			}
			source.offset-=8;
		}
	}
	
	static void render_text( osd_bitmap bitmap )
	{
		int offs;
	
	
		for (offs = 0;offs < videoram_size[0];offs += 2)
		{
			int sx,sy;
			int data = videoram.READ_WORD(offs);
			int attr = data >> 8;
			int code = data & 0xff;
			int color = attr & 0x0f;
			int flipy = attr & 0x10;
	
			sx = (offs / 2) % 32;
			sy = (offs / 2) / 32;
	
			if (flipscreen != 0)
			{
				sx = 31 - sx;
				sy = 31 - sy;
				flipy = NOT(flipy);
			}
	
			drawgfx(bitmap,Machine.gfx[0],
					code + ((attr & 0xc0) << 2) + ((attr & 0x20) << 5),
					color,
					flipscreen,flipy,
					8*sx,8*sy,
					Machine.drv.visible_area, TRANSPARENCY_PEN,3);
		}
	}
	
	
	
	public static VhUpdateHandlerPtr tigeroad_vh_screenrefresh = new VhUpdateHandlerPtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		palette_recalc();
		/* no need to check the return code since we redraw everything each frame */
	
		render_background( bitmap,0 );
		render_sprites( bitmap );
		render_background( bitmap,1 );
		render_text( bitmap );
	} };
}
