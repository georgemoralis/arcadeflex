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
import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;


public class yiear
{
	
	
	static int flipscreen;
	static int nmi_enable;
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Yie Ar Kung-Fu has one 32x8 palette PROM, connected to the RGB output this
	  way:
	
	  bit 7 -- 220 ohm resistor  -- BLUE
	        -- 470 ohm resistor  -- BLUE
	        -- 220 ohm resistor  -- GREEN
	        -- 470 ohm resistor  -- GREEN
	        -- 1  kohm resistor  -- GREEN
	        -- 220 ohm resistor  -- RED
	        -- 470 ohm resistor  -- RED
	  bit 0 -- 1  kohm resistor  -- RED
	
	***************************************************************************/
	public static VhConvertColorPromPtr yiear_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
                int p_inc = 0;
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2;
	
			/* red component */
			bit0 = (color_prom.read() >> 0) & 0x01;
			bit1 = (color_prom.read() >> 1) & 0x01;
			bit2 = (color_prom.read() >> 2) & 0x01;
			palette[p_inc++]=(char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
	
			/* green component */
			bit0 = (color_prom.read() >> 3) & 0x01;
			bit1 = (color_prom.read() >> 4) & 0x01;
			bit2 = (color_prom.read() >> 5) & 0x01;
			palette[p_inc++]=(char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
	
			/* blue component */
			bit0 = 0;
			bit1 = (color_prom.read() >> 6) & 0x01;
			bit2 = (color_prom.read() >> 7) & 0x01;
			palette[p_inc++]=(char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
	
			color_prom.inc();
		}
	} };
	
	
	public static WriteHandlerPtr yiear_control_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* bit 0 flips screen */
		if (flipscreen != (data & 1))
		{
			flipscreen = data & 1;
			memset(dirtybuffer,1,videoram_size[0]);
		}
	
		/* bit 1 is NMI enable */
		nmi_enable = data & 0x02;
	
		/* bit 2 is IRQ enable */
		interrupt_enable_w.handler(0, data & 0x04);
	
		/* bits 3 and 4 are coin counters */
		coin_counter_w.handler(0, (data >> 3) & 0x01);
		coin_counter_w.handler(1, (data >> 4) & 0x01);
	} };
	
	public static InterruptPtr yiear_nmi_interrupt = new InterruptPtr() {
        public int handler() {

		/* can't use nmi_interrupt() because interrupt_enable_w() effects it */
		return nmi_enable!=0 ? M6809_INT_NMI : ignore_interrupt.handler();
	}};
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr yiear_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size[0] - 2;offs >= 0;offs -= 2)
		{
			if (dirtybuffer[offs]!=0 || dirtybuffer[offs + 1]!=0)
			{
				int sx,sy,flipx,flipy;
	
	
				dirtybuffer[offs] = dirtybuffer[offs + 1] = 0;
	
				sx = (offs/2) % 32;
				sy = (offs/2) / 32;
				flipx = videoram.read(offs) & 0x80;
				flipy = videoram.read(offs) & 0x40;
				if (flipscreen != 0)
				{
					sx = 31 - sx;
					sy = 31 - sy;
					flipx = NOT(flipx);
					flipy = NOT(flipy);
				}
	
				drawgfx(tmpbitmap,Machine.gfx[0],
					videoram.read(offs + 1) | ((videoram.read(offs) & 0x10) << 4),
					0,
					flipx,flipy,
					8*sx,8*sy,
					null,TRANSPARENCY_NONE,0);
			}
		}
	
	
		/* copy the temporary bitmap to the screen */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	
	
		/* draw sprites */
		for (offs = spriteram_size[0] - 2;offs >= 0;offs -= 2)
		{
			int sx,sy,flipx,flipy;
	
	
			sy    =  240 - spriteram.read(offs + 1);
			sx    =  spriteram_2.read(offs);
			flipx = ~spriteram.read(offs) & 0x40;
			flipy =  spriteram.read(offs) & 0x80;
	
			if (flipscreen != 0)
			{
				sy = 240 - sy;
				flipy = NOT(flipy);
			}
	
			if (offs < 0x26)
			{
				sy++;	/* fix title screen & garbage at the bottom of the screen */
			}
	
			drawgfx(bitmap,Machine.gfx[1],
				spriteram_2.read(offs + 1) + 256 * (spriteram.read(offs) & 1),
				0,
				flipx,flipy,
				sx,sy,
				Machine.drv.visible_area,TRANSPARENCY_PEN,0);
		}
	} };
}
