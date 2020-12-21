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
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;
import static gr.codebb.arcadeflex.v036.mame.palette.*;


public class qix
{
	public static UBytePtr qix_palettebank=new UBytePtr();
	public static UBytePtr qix_videoaddress=new UBytePtr();
		
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Qix doesn't have colors PROMs, it uses RAM. The meaning of the bits are
	  bit 7 -- Red
	        -- Red
	        -- Green
	        -- Green
	        -- Blue
	        -- Blue
	        -- Intensity
	  bit 0 -- Intensity
	
	***************************************************************************/
	public static WriteHandlerPtr update_pen = new WriteHandlerPtr() { public void handler(int pen, int val)
	{
		/* this conversion table should be about right. It gives a reasonable */
		/* gray scale in the test screen, and the red, green and blue squares */
		/* in the same screen are barely visible, as the manual requires. */
		char table[] =
		{
			0x00,	/* value = 0, intensity = 0 */
			0x12,	/* value = 0, intensity = 1 */
			0x24,	/* value = 0, intensity = 2 */
			0x49,	/* value = 0, intensity = 3 */
			0x12,	/* value = 1, intensity = 0 */
			0x24,	/* value = 1, intensity = 1 */
			0x49,	/* value = 1, intensity = 2 */
			0x92,	/* value = 1, intensity = 3 */
			0x5b,	/* value = 2, intensity = 0 */
			0x6d,	/* value = 2, intensity = 1 */
			0x92,	/* value = 2, intensity = 2 */
			0xdb,	/* value = 2, intensity = 3 */
			0x7f,	/* value = 3, intensity = 0 */
			0x91,	/* value = 3, intensity = 1 */
			0xb6,	/* value = 3, intensity = 2 */
			0xff	/* value = 3, intensity = 3 */
		};
	
		int bits,intensity,red,green,blue;
	
		intensity = (val >> 0) & 0x03;
		bits = (val >> 6) & 0x03;
		red = table[(bits << 2) | intensity];
		bits = (val >> 4) & 0x03;
		green = table[(bits << 2) | intensity];
		bits = (val >> 2) & 0x03;
		blue = table[(bits << 2) | intensity];
	
		palette_change_color(pen,red,green,blue);
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	public static VhStartPtr qix_vh_start = new VhStartPtr() { public int handler() 
	{
		//if ((videoram = malloc(256*256)) == 0)
		//	return 1;
                videoram = new UBytePtr(256*256);
	
		return 0;
	} };
	
	
	
	/***************************************************************************
	
	  Stop the video hardware emulation.
	
	***************************************************************************/
	public static VhStopPtr qix_vh_stop = new VhStopPtr() { public void handler() 
	{
		videoram = null;
	
	} };
	
	
	
	/* The screen is 256x256 with eight bit pixels (64K).  The screen is divided
	into two halves each half mapped by the video CPU at $0000-$7FFF.  The
	high order bit of the address latch at $9402 specifies which half of the
	screen is being accessed.
	
	The address latch works as follows.  When the video CPU accesses $9400,
	the screen address is computed by using the values at $9402 (high byte)
	and $9403 (low byte) to get a value between $0000-$FFFF.  The value at
	that location is either returned or written. */
	
	public static ReadHandlerPtr qix_videoram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		offset += (qix_videoaddress.read(0) & 0x80) * 0x100;
		return videoram.read(offset);
	} };
	
	public static WriteHandlerPtr qix_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int x, y;
	
		offset += (qix_videoaddress.read(0) & 0x80) * 0x100;
	
		x = offset & 0xff;
		y = offset >> 8;
	
		plot_pixel.handler(Machine.scrbitmap, x, y, Machine.pens[data]);
	
		videoram.write(offset,data);
	} };
	
	
	
	public static ReadHandlerPtr qix_addresslatch_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		offset = qix_videoaddress.read(0) * 0x100 + qix_videoaddress.read(1);
		return videoram.read(offset);
	} };
	
	
	
	public static WriteHandlerPtr qix_addresslatch_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int x, y;
	
		offset = qix_videoaddress.read(0) * 0x100 + qix_videoaddress.read(1);
	
		x = offset & 0xff;
		y = offset >> 8;
	
		plot_pixel.handler(Machine.scrbitmap, x, y, Machine.pens[data]);
	
		videoram.write(offset,data);
	} };
	
	
	
	/* The color RAM works as follows.  The color RAM contains palette values for
	four pages (0-3).  When a write to $8800 on the video CPU occurs, the color
	RAM page is taken from the lowest 2 bits of the value.  This selects one of
	the color RAM pages as follows:
	
	     colorRAMAddr = 0x9000 + ((data & 0x03) * 0x100);
	
	Qix uses a palette of 64 colors (2 each RGB) and four intensities (RRGGBBII).
	*/
	public static WriteHandlerPtr qix_paletteram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		paletteram.write(offset,data);
	
		if ((qix_palettebank.read() & 0x03) == (offset / 256))
			update_pen.handler(offset % 256, data);
	} };
	
	
	
	public static WriteHandlerPtr qix_palettebank_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if ((qix_palettebank.read() & 0x03) != (data & 0x03))
		{
			UBytePtr pram = new UBytePtr(paletteram,256 * (data & 0x03));
			int i;
	
			for (i = 0;i < 256;i++)
				update_pen.handler(i, pram.readinc());
		}
	
		qix_palettebank.write(data);
	
	} };
	
	
	public static VhUpdatePtr qix_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		/* recalc the palette if necessary */
		if (palette_recalc ()!=null || full_refresh!=0)
		{
			int offs;
	
			for (offs = 0; offs < 256*256; offs++)
			{
				int x = offs & 0xff;
				int y = offs >> 8;
	
				plot_pixel.handler(bitmap, x, y, Machine.pens[videoram.read(offs)]);
			}
		}
	} };
}
