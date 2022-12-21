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

public class arabian
{
	
	
	
	static struct osd_bitmap *tmpbitmap2;
	static unsigned char inverse_palette[256]; /* JB 970727 */
	
	
	public static VhConvertColorPromPtr arabian_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(UByte []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
		/* this should be very close */
		for (i = 0;i < Machine.drv.total_colors/2;i++)
		{
			int on;
	
	
			on = (i & 0x08) ? 0x80 : 0xff;
	
			*(palette++) = (i & 0x04) ? 0xff : 0;
			*(palette++) = (i & 0x02) ? on : 0;
			*(palette++) = (i & 0x01) ? on : 0;
		}
	
	
		/* this is handmade to match the screen shot */
	
		*(palette++) = 0x00;
		*(palette++) = 0x00;	  // 0000
		*(palette++) = 0x00;
	
		*(palette++) = 0x00;
		*(palette++) = 0xff;  // 0001
		*(palette++) = 0x00;
	
		*(palette++) = 0x00;
		*(palette++) = 0xff;  // 0010
		*(palette++) = 0x00;
	
		*(palette++) = 0x00;
		*(palette++) = 0xff;  // 0011
		*(palette++) = 0x00;
	
		*(palette++) = 0xff;
		*(palette++) = 0x00;  // 0100
		*(palette++) = 0x00;
	
		*(palette++) = 0xff;
		*(palette++) = 0xff;  // 0101
		*(palette++) = 0x00;
	
		*(palette++) = 0xff;
		*(palette++) = 0xff;  // 0110
		*(palette++) = 0x00;
	
		*(palette++) = 0xff;
		*(palette++) = 0xff;  // 0111
		*(palette++) = 0x00;
	
	
	
		*(palette++) = 0x00;
		*(palette++) = 0x00;  // 1000
		*(palette++) = 0x00;
	
		*(palette++) = 0xff;
		*(palette++) = 0xff;  // 1001
		*(palette++) = 0x00;
	
		*(palette++) = 0xff;
		*(palette++) = 0x80;  // 1010
		*(palette++) = 0x00;
	
		*(palette++) = 0x00;
		*(palette++) = 0xff;  // 1011
		*(palette++) = 0x00;
	
		*(palette++) = 0xff;
		*(palette++) = 0x00;  // 1100
		*(palette++) = 0x00;
	
		*(palette++) = 0xff;
		*(palette++) = 0xff;  // 1101
		*(palette++) = 0x00;
	
		*(palette++) = 0xff;
		*(palette++) = 0x80;  // 1110
		*(palette++) = 0x00;
	
		*(palette++) = 0xff;
		*(palette++) = 0xff;  // 1111
		*(palette++) = 0x00;
	} };
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	
	public static VhStartPtr arabian_vh_start = new VhStartPtr() { public int handler() 
	{
		int p1,p2,p3,p4,v1,v2,offs;
		int i;	/* JB 970727 */
	
		if ((tmpbitmap = osd_create_bitmap(Machine.drv.screen_width,Machine.drv.screen_height)) == 0)
			return 1;
	
		if ((tmpbitmap2 = osd_create_bitmap(Machine.drv.screen_width,Machine.drv.screen_height)) == 0)
		{
			osd_free_bitmap(tmpbitmap2);
			return 1;
		}
	
		/* JB 970727 */
		for (i = 0;i < Machine.drv.total_colors;i++)
			inverse_palette[ Machine.pens[i] ] = i;
	
	/*transform graphics data into more usable format*/
	/*which is coded like this:
	
	  byte adr+0x4000  byte adr
	  DCBA DCBA        DCBA DCBA
	
	D-bits of pixel 4
	C-bits of pixel 3
	B-bits of pixel 2
	A-bits of pixel 1
	
	after conversion :
	
	  byte adr+0x4000  byte adr
	  DDDD CCCC        BBBB AAAA
	*/
	
	  for (offs=0; offs<0x4000; offs++)
	  {
	     v1 = memory_region(REGION_GFX1)[offs];
	     v2 = memory_region(REGION_GFX1)[offs+0x4000];
	
	     p1 = (v1 & 0x01) | ( (v1 & 0x10) >> 3) | ( (v2 & 0x01) << 2 ) | ( (v2 & 0x10) >> 1);
	     v1 = v1 >> 1;
	     v2 = v2 >> 1;
	     p2 = (v1 & 0x01) | ( (v1 & 0x10) >> 3) | ( (v2 & 0x01) << 2 ) | ( (v2 & 0x10) >> 1);
	     v1 = v1 >> 1;
	     v2 = v2 >> 1;
	     p3 = (v1 & 0x01) | ( (v1 & 0x10) >> 3) | ( (v2 & 0x01) << 2 ) | ( (v2 & 0x10) >> 1);
	     v1 = v1 >> 1;
	     v2 = v2 >> 1;
	     p4 = (v1 & 0x01) | ( (v1 & 0x10) >> 3) | ( (v2 & 0x01) << 2 ) | ( (v2 & 0x10) >> 1);
	
	     memory_region(REGION_GFX1)[offs] = p1 | (p2<<4);
	     memory_region(REGION_GFX1)[offs+0x4000] = p3 | (p4<<4);
	
	  }
		return 0;
	} };
	
	/***************************************************************************
	
	  Stop the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStopPtr arabian_vh_stop = new VhStopPtr() { public void handler() 
	{
		osd_free_bitmap(tmpbitmap2);
		osd_free_bitmap(tmpbitmap);
	} };
	
	
	
	
	INLINE void blit_byte(UINT8 x, UINT8 y, int val, int val2, UINT8 plane)
	{
		int p1,p2,p3,p4;
	
		INT8 dx=1,dy=0;
	
	
		p4 =  val        & 0x0f;
		p3 = (val  >> 4) & 0x0f;
		p2 =  val2       & 0x0f;
		p1 = (val2 >> 4) & 0x0f;
	
	
		if (Machine.orientation & ORIENTATION_SWAP_XY)
		{
			int t;
	
			t = x; x = y; y = t;
			t = dx; dx = dy; dy = t;
		}
		if (Machine.orientation & ORIENTATION_FLIP_X)
		{
			x = x ^ 0xff;
			dx = -dx;
		}
		if (Machine.orientation & ORIENTATION_FLIP_Y)
		{
			y = y ^ 0xff;
			dy = -dy;
		}
	
	
		if ((plane & 0x01) != 0)
		{
			if (p4 != 8)  tmpbitmap .line[y     ][x     ] = Machine.pens[p4];
			if (p3 != 8)  tmpbitmap .line[y+  dy][x+  dx] = Machine.pens[p3];
			if (p2 != 8)  tmpbitmap .line[y+2*dy][x+2*dx] = Machine.pens[p2];
			if (p1 != 8)  tmpbitmap .line[y+3*dy][x+3*dx] = Machine.pens[p1];
		}
	
		if ((plane & 0x04) != 0)
		{
			if (p4 != 8)  tmpbitmap2.line[y     ][x     ] = Machine.pens[16+p4];
			if (p3 != 8)  tmpbitmap2.line[y+  dy][x+  dx] = Machine.pens[16+p3];
			if (p2 != 8)  tmpbitmap2.line[y+2*dy][x+2*dx] = Machine.pens[16+p2];
			if (p1 != 8)  tmpbitmap2.line[y+3*dy][x+3*dx] = Machine.pens[16+p1];
		}
	
		if (dx >= 0 && dy >= 0)
			osd_mark_dirty(x,y,x+3*dx,y+3*dy,0);
		else if (dx >= 0)
			osd_mark_dirty(x,y+3*dy,x+3*dx,y,0);
		else if (dy >= 0)
			osd_mark_dirty(x+3*dx,y,x,y+3*dy,0);
		else
			osd_mark_dirty(x+3*dx,y+3*dy,x,y,0);
	}
	
	
	void arabian_blit_area(UINT8 plane, UINT16 src, UINT8 x, UINT8 y, UINT8 sx, UINT8 sy)
	{
		int i,j;
	
		for (i = 0; i <= sx; i++, x += 4)
		{
			for (j = 0; j <= sy; j++)
			{
				blit_byte(x, y+j, memory_region(REGION_GFX1)[src], memory_region(REGION_GFX1)[src+0x4000], plane);
				src++;
			}
		}
	}
	
	
	public static WriteHandlerPtr arabian_blitter_w = new WriteHandlerPtr() { public void handler(int offset, int val)
	{
		spriteram[offset]=val;
	
		if ((offset & 0x07) == 6)
		{
			UINT8 plane,x,y,sx,sy;
			UINT16 src;
	
			plane = spriteram[offset-6];
			src   = spriteram[offset-5] | (spriteram[offset-4] << 8);
			x     = spriteram[offset-2] << 2;
			y     = spriteram[offset-3];
			sx    = spriteram[offset-0];
			sy    = spriteram[offset-1];
	
			arabian_blit_area(plane,src,x,y,sx,sy);
		}
	} };
	
	
	
	public static WriteHandlerPtr arabian_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int val)
	{
		int plane1,plane2,plane3,plane4;
		unsigned char *bm;
	
		UINT8 x,y;
		INT8 dx=1, dy=0;
	
	
		plane1 = spriteram[0] & 0x01;
		plane2 = spriteram[0] & 0x02;
		plane3 = spriteram[0] & 0x04;
		plane4 = spriteram[0] & 0x08;
	
		x = (offset >> 8) << 2;
		y = offset & 0xff;
	
	
		if (Machine.orientation & ORIENTATION_SWAP_XY)
		{
			int t;
	
			t = x; x = y; y = t;
			t = dx; dx = dy; dy = t;
		}
		if (Machine.orientation & ORIENTATION_FLIP_X)
		{
			x = x ^ 0xff;
			dx = -dx;
		}
		if (Machine.orientation & ORIENTATION_FLIP_Y)
		{
			y = y ^ 0xff;
			dy = -dy;
		}
	
	
		/* JB 970727 */
		tmpbitmap .line[y     ][x     ] = inverse_palette[ tmpbitmap .line[y     ][x     ] ];
		tmpbitmap .line[y+  dy][x+  dx] = inverse_palette[ tmpbitmap .line[y+  dy][x+  dx] ];
		tmpbitmap .line[y+2*dy][x+2*dx] = inverse_palette[ tmpbitmap .line[y+2*dy][x+2*dx] ];
		tmpbitmap .line[y+3*dy][x+3*dx] = inverse_palette[ tmpbitmap .line[y+3*dy][x+3*dx] ];
		tmpbitmap2.line[y     ][x     ] = inverse_palette[ tmpbitmap2.line[y     ][x     ] ];
		tmpbitmap2.line[y+  dy][x+  dx] = inverse_palette[ tmpbitmap2.line[y+  dy][x+  dx] ];
		tmpbitmap2.line[y+2*dy][x+2*dx] = inverse_palette[ tmpbitmap2.line[y+2*dy][x+2*dx] ];
		tmpbitmap2.line[y+3*dy][x+3*dx] = inverse_palette[ tmpbitmap2.line[y+3*dy][x+3*dx] ];
	
		if (plane1 != 0)
		{
			bm = &tmpbitmap.line[y     ][x     ];
			*bm &= 0xf3;
			if ((val & 0x10) != 0) *bm |= 8;
			if ((val & 0x01) != 0) *bm |= 4;
	
			bm = &tmpbitmap.line[y+  dy][x+  dx];
			*bm &= 0xf3;
			if ((val & 0x20) != 0) *bm |= 8;
			if ((val & 0x02) != 0) *bm |= 4;
	
			bm = &tmpbitmap.line[y+2*dy][x+2*dx];
			*bm &= 0xf3;
			if ((val & 0x40) != 0) *bm |= 8;
			if ((val & 0x04) != 0) *bm |= 4;
	
			bm = &tmpbitmap.line[y+3*dy][x+3*dx];
			*bm &= 0xf3;
			if ((val & 0x80) != 0) *bm |= 8;
			if ((val & 0x08) != 0) *bm |= 4;
		}
	
		if (plane2 != 0)
		{
			bm = &tmpbitmap.line[y     ][x     ];
			*bm &= 0xfc;
			if ((val & 0x10) != 0) *bm |= 2;
			if ((val & 0x01) != 0) *bm |= 1;
	
			bm = &tmpbitmap.line[y+  dy][x+  dx];
			*bm &= 0xfc;
			if ((val & 0x20) != 0) *bm |= 2;
			if ((val & 0x02) != 0) *bm |= 1;
	
			bm = &tmpbitmap.line[y+2*dy][x+2*dx];
			*bm &= 0xfc;
			if ((val & 0x40) != 0) *bm |= 2;
			if ((val & 0x04) != 0) *bm |= 1;
	
			bm = &tmpbitmap.line[y+3*dy][x+3*dx];
			*bm &= 0xfc;
			if ((val & 0x80) != 0) *bm |= 2;
			if ((val & 0x08) != 0) *bm |= 1;
		}
	
		if (plane3 != 0)
		{
			bm = &tmpbitmap2.line[y     ][x     ];
			*bm &= 0xf3;
			if ((val & 0x10) != 0) *bm |= (16+8);
			if ((val & 0x01) != 0) *bm |= (16+4);
	
			bm = &tmpbitmap2.line[y+  dy][x+  dx];
			*bm &= 0xf3;
			if ((val & 0x20) != 0) *bm |= (16+8);
			if ((val & 0x02) != 0) *bm |= (16+4);
	
			bm = &tmpbitmap2.line[y+2*dy][x+2*dx];
			*bm &= 0xf3;
			if ((val & 0x40) != 0) *bm |= (16+8);
			if ((val & 0x04) != 0) *bm |= (16+4);
	
			bm = &tmpbitmap2.line[y+3*dy][x+3*dx];
			*bm &= 0xf3;
			if ((val & 0x80) != 0) *bm |= (16+8);
			if ((val & 0x08) != 0) *bm |= (16+4);
		}
	
		if (plane4 != 0)
		{
			bm = &tmpbitmap2.line[y     ][x     ];
			*bm &= 0xfc;
			if ((val & 0x10) != 0) *bm |= (16+2);
			if ((val & 0x01) != 0) *bm |= (16+1);
	
			bm = &tmpbitmap2.line[y+  dy][x+  dx];
			*bm &= 0xfc;
			if ((val & 0x20) != 0) *bm |= (16+2);
			if ((val & 0x02) != 0) *bm |= (16+1);
	
			bm = &tmpbitmap2.line[y+2*dy][x+2*dx];
			*bm &= 0xfc;
			if ((val & 0x40) != 0) *bm |= (16+2);
			if ((val & 0x04) != 0) *bm |= (16+1);
	
			bm = &tmpbitmap2.line[y+3*dy][x+3*dx];
			*bm &= 0xfc;
			if ((val & 0x80) != 0) *bm |= (16+2);
			if ((val & 0x08) != 0) *bm |= (16+1);
		}
	
		/* JB 970727 */
		tmpbitmap .line[y     ][x     ] = Machine.pens[ tmpbitmap .line[y     ][x     ] ];
		tmpbitmap .line[y+  dy][x+  dx] = Machine.pens[ tmpbitmap .line[y+  dy][x+  dx] ];
		tmpbitmap .line[y+2*dy][x+2*dx] = Machine.pens[ tmpbitmap .line[y+2*dy][x+2*dx] ];
		tmpbitmap .line[y+3*dy][x+3*dx] = Machine.pens[ tmpbitmap .line[y+3*dy][x+3*dx] ];
		tmpbitmap2.line[y     ][x     ] = Machine.pens[ tmpbitmap2.line[y     ][x     ] ];
		tmpbitmap2.line[y+  dy][x+  dx] = Machine.pens[ tmpbitmap2.line[y+  dy][x+  dx] ];
		tmpbitmap2.line[y+2*dy][x+2*dx] = Machine.pens[ tmpbitmap2.line[y+2*dy][x+2*dx] ];
		tmpbitmap2.line[y+3*dy][x+3*dx] = Machine.pens[ tmpbitmap2.line[y+3*dy][x+3*dx] ];
	
		if (dx >= 0 && dy >= 0)
			osd_mark_dirty(x,y,x+3*dx,y+3*dy,0);
		else if (dx >= 0)
			osd_mark_dirty(x,y+3*dy,x+3*dx,y,0);
		else if (dy >= 0)
			osd_mark_dirty(x+3*dx,y,x,y+3*dy,0);
		else
			osd_mark_dirty(x+3*dx,y+3*dy,x,y,0);
	} };
	
	
	public static VhUpdatePtr arabian_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		copybitmap(bitmap,tmpbitmap2,0,0,0,0,&Machine.drv.visible_area,TRANSPARENCY_NONE, 0);
	 	copybitmap(bitmap,tmpbitmap ,0,0,0,0,&Machine.drv.visible_area,TRANSPARENCY_COLOR,0);
	} };
}
