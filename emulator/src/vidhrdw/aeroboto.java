/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 *
 *
 *
 */ 
package vidhrdw;

import static arcadeflex.libc.*;
import static arcadeflex.libc_old.*;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static vidhrdw.generic.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.mame.*;

public class aeroboto
{
	public static CharPtr aeroboto_videoram=new CharPtr();
	public static CharPtr aeroboto_fgscroll=new CharPtr();
        public static CharPtr aeroboto_bgscroll=new CharPtr();
	
	public static int aeroboto_charbank;
	
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr aeroboto_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
	
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			int sx,sy;
	
	
			sx = offs % 32;
			sy = offs / 32;
	
			drawgfx(bitmap,Machine.gfx[0],
					videoram.read(offs) + 256 * aeroboto_charbank,
					0,
					0,0,
					8*sx - aeroboto_bgscroll.read(sy),8*sy,
					Machine.drv.visible_area,TRANSPARENCY_NONE,0);
			drawgfx(bitmap,Machine.gfx[0],
					videoram.read(offs) + 256 * aeroboto_charbank,
					0,
					0,0,
					8*sx - aeroboto_bgscroll.read(sy) + 256,8*sy,
					Machine.drv.visible_area,TRANSPARENCY_NONE,0);
		}
	
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			int sx,sy;
	
			sx = offs % 32;
			sy = offs / 32;
	
			drawgfx(bitmap,Machine.gfx[0],
					aeroboto_videoram.read(offs) + 256 * aeroboto_charbank,
					0,
					0,0,
					8*sx - aeroboto_fgscroll.read(sy),8*sy,
					Machine.drv.visible_area,TRANSPARENCY_PEN,0);
			drawgfx(bitmap,Machine.gfx[0],
					aeroboto_videoram.read(offs) + 256 * aeroboto_charbank,
					0,
					0,0,
					8*sx - aeroboto_fgscroll.read(sy) + 256,8*sy,
					Machine.drv.visible_area,TRANSPARENCY_PEN,0);
		}
	
		for (offs = spriteram_size[0]-4;offs >= 0;offs -= 4)
		{
			int sx,sy;
	
	
			sx = spriteram.read(offs + 3);
			sy = 239 - spriteram.read(offs);
	
			drawgfx(bitmap,Machine.gfx[2],
					spriteram.read(offs + 1),
					spriteram.read(offs + 2) & 0x0f,
					0,0,
					sx,sy,
					Machine.drv.visible_area,TRANSPARENCY_PEN,0);
		}
	} };
}
