/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package vidhrdw;

import static arcadeflex.libc.*;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static vidhrdw.generic.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.mame.*;

public class snowbros
{
	
	
	unsigned char *snowbros_spriteram;
	
	int snowbros_spriteram_size;
	
	
	/* Put in case screen can be optimised later */
	
	public static WriteHandlerPtr snowbros_spriteram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	  	COMBINE_WORD_MEM(&snowbros_spriteram[offset], data);
	} };
	
	public static ReadHandlerPtr snowbros_spriteram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return READ_WORD(&snowbros_spriteram[offset]);
	} };
	
	public static VhUpdatePtr snowbros_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int x=0,y=0,offs;
	
	
		palette_recalc ();
		/* no need to check the return code since we redraw everything each frame */
	
	
		/*
		 * Sprite Tile Format
		 * ------------------
		 *
		 * Byte(s) | Bit(s)   | Use
		 * --------+-76543210-+----------------
		 *  0-5	| -------- | ?
		 *	6	| -------- | ?
		 *	7	| xxxx.... | Palette Bank
		 *	7	| .......x | XPos - Sign Bit
		 *	9	| xxxxxxxx | XPos
		 *	7	| ......x. | YPos - Sign Bit
		 *	B	| xxxxxxxx | YPos
		 *	7	| .....x.. | Use Relative offsets
		 *	C	| -------- | ?
		 *	D	| xxxxxxxx | Sprite Number (low 8 bits)
		 *	E	| -------- | ?
		 *	F	| ....xxxx | Sprite Number (high 4 bits)
		 *	F	| x....... | Flip Sprite Y-Axis
		 *	F	| .x...... | Flip Sprite X-Axis
		 */
	
		/* This clears & redraws the entire screen each pass */
	
	  	fillbitmap(bitmap,Machine.gfx[0].colortable[0],&Machine.drv.visible_area);
	
		for (offs = 0;offs < 0x1e00; offs += 16)
		{
			int sx = READ_WORD(&snowbros_spriteram[8+offs]) & 0xff;
			int sy = READ_WORD(&snowbros_spriteram[0x0a+offs]) & 0xff;
			int tilecolour = READ_WORD(&snowbros_spriteram[6+offs]);
	
			if ((tilecolour & 1) != 0) sx = -1 - (sx ^ 0xff);
	
			if ((tilecolour & 2) != 0) sy = -1 - (sy ^ 0xff);
	
			if ((tilecolour & 4) != 0)
			{
				x += sx;
				y += sy;
			}
			else
			{
				x = sx;
				y = sy;
			}
	
			if (x > 511) x &= 0x1ff;
			if (y > 511) y &= 0x1ff;
	
			if ((x>-16) && (y>0) && (x<256) && (y<240))
			{
				int attr = READ_WORD(&snowbros_spriteram[0x0e + offs]);
				int tile = ((attr & 0x0f) << 8) + (READ_WORD(&snowbros_spriteram[0x0c+offs]) & 0xff);
	
				drawgfx(bitmap,Machine.gfx[0],
						tile,
						(tilecolour & 0xf0) >> 4,
						attr & 0x80, attr & 0x40,
						x,y,
						&Machine.drv.visible_area,TRANSPARENCY_PEN,0);
			}
		}
	} };
}
