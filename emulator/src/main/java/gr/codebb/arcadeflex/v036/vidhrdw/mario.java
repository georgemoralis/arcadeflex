//copied with TODOS from original src

package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;

public class mario {

    public static int gfx_bank;
    public static int palette_bank;
    public static CharPtr mario_scrolly = new CharPtr();

/*TODO*///void mario_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	#define TOTAL_COLORS(gfxn) (Machine->gfx[gfxn]->total_colors * Machine->gfx[gfxn]->color_granularity)
/*TODO*///	#define COLOR(gfxn,offs) (colortable[Machine->drv->gfxdecodeinfo[gfxn].color_codes_start + offs])
/*TODO*///
/*TODO*///
/*TODO*///	for (i = 0;i < Machine->drv->total_colors;i++)
/*TODO*///	{
/*TODO*///		int bit0,bit1,bit2;
/*TODO*///
/*TODO*///
/*TODO*///		/* red component */
/*TODO*///		bit0 = (*color_prom >> 5) & 1;
/*TODO*///		bit1 = (*color_prom >> 6) & 1;
/*TODO*///		bit2 = (*color_prom >> 7) & 1;
/*TODO*///		*(palette++) = 255 - (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
/*TODO*///		/* green component */
/*TODO*///		bit0 = (*color_prom >> 2) & 1;
/*TODO*///		bit1 = (*color_prom >> 3) & 1;
/*TODO*///		bit2 = (*color_prom >> 4) & 1;
/*TODO*///		*(palette++) = 255 - (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
/*TODO*///		/* blue component */
/*TODO*///		bit0 = (*color_prom >> 0) & 1;
/*TODO*///		bit1 = (*color_prom >> 1) & 1;
/*TODO*///		*(palette++) = 255 - (0x55 * bit0 + 0xaa * bit1);
/*TODO*///
/*TODO*///		color_prom++;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* characters use the same palette as sprites, however characters */
/*TODO*///	/* use only colors 64-127 and 192-255. */
/*TODO*///	for (i = 0;i < 8;i++)
/*TODO*///	{
/*TODO*///		COLOR(0,4*i) = 8*i + 64;
/*TODO*///		COLOR(0,4*i+1) = 8*i+1 + 64;
/*TODO*///		COLOR(0,4*i+2) = 8*i+2 + 64;
/*TODO*///		COLOR(0,4*i+3) = 8*i+3 + 64;
/*TODO*///	}
/*TODO*///	for (i = 0;i < 8;i++)
/*TODO*///	{
/*TODO*///		COLOR(0,4*i+8*4) = 8*i + 192;
/*TODO*///		COLOR(0,4*i+8*4+1) = 8*i+1 + 192;
/*TODO*///		COLOR(0,4*i+8*4+2) = 8*i+2 + 192;
/*TODO*///		COLOR(0,4*i+8*4+3) = 8*i+3 + 192;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* sprites */
/*TODO*///	for (i = 0;i < TOTAL_COLORS(1);i++)
/*TODO*///		COLOR(1,i) = i;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
    public static WriteHandlerPtr mario_gfxbank_w = new WriteHandlerPtr() {	public void handler(int offset, int data)
    {   
        if (gfx_bank != (data & 1))
        {
             memset(dirtybuffer,1,videoram_size[0]);
             gfx_bank = data & 1;
        }
    }};
    public static WriteHandlerPtr mario_palettebank_w = new WriteHandlerPtr() {	public void handler(int offset, int data)
    { 

        if (palette_bank != (data & 1))
        {
             memset(dirtybuffer,1,videoram_size[0]);
             palette_bank = data & 1;
        }
    }};

    /***************************************************************************

      Draw the game screen in the given osd_bitmap.
      Do NOT call osd_update_display() from this function, it will be called by
      the main emulation engine.

    /***************************************************************************/
    public static VhUpdatePtr mario_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh)
    {
/*TODO*///	int offs;
/*TODO*///
/*TODO*///
/*TODO*///	/* for every character in the Video RAM, check if it has been modified */
/*TODO*///	/* since last time and update it accordingly. */
/*TODO*///	for (offs = videoram_size - 1;offs >= 0;offs--)
/*TODO*///	{
/*TODO*///		if (dirtybuffer[offs])
/*TODO*///		{
/*TODO*///			int sx,sy;
/*TODO*///
/*TODO*///
/*TODO*///			dirtybuffer[offs] = 0;
/*TODO*///
/*TODO*///			sx = offs % 32;
/*TODO*///			sy = offs / 32;
/*TODO*///
/*TODO*///			drawgfx(tmpbitmap,Machine->gfx[0],
/*TODO*///					videoram[offs] + 256 * gfx_bank,
/*TODO*///					(videoram[offs] >> 5) + 8 * palette_bank,
/*TODO*///					0,0,
/*TODO*///					8*sx,8*sy,
/*TODO*///					0,TRANSPARENCY_NONE,0);
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	/* copy the temporary bitmap to the screen */
/*TODO*///	{
/*TODO*///		int scrolly;
/*TODO*///
/*TODO*///		/* I'm not positive the scroll direction is right */
/*TODO*///		scrolly = -*mario_scrolly - 17;
/*TODO*///		copyscrollbitmap(bitmap,tmpbitmap,0,0,1,&scrolly,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
/*TODO*///	}
/*TODO*///
/*TODO*///	/* Draw the sprites. */
/*TODO*///	for (offs = 0;offs < spriteram_size;offs += 4)
/*TODO*///	{
/*TODO*///		if (spriteram[offs])
/*TODO*///		{
/*TODO*///			drawgfx(bitmap,Machine->gfx[1],
/*TODO*///					spriteram[offs + 2],
/*TODO*///					(spriteram[offs + 1] & 0x0f) + 16 * palette_bank,
/*TODO*///					spriteram[offs + 1] & 0x80,spriteram[offs + 1] & 0x40,
/*TODO*///					spriteram[offs + 3] - 8,240 - spriteram[offs] + 8,
/*TODO*///					&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
/*TODO*///		}
/*TODO*///	}
    }};   
}
