/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

changes:
Aug 16, 1999 - Rotation (-ror and -rol) implementation fixed by Chad Hendrickson

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"


unsigned char *kangaroo_video_control;
unsigned char *kangaroo_bank_select;
unsigned char *kangaroo_blitter;

static int screen_flipped;
static struct osd_bitmap *tmpbitmap2;


/***************************************************************************

  Convert the color PROMs into a more useable format.

  Kangaroo doesn't have color PROMs, the playfield data is directly converted
  into colors: 1 bit per gun, therefore only 8 possible colors, but there is
  also a global mask register which controls intensities of the three guns,
  separately for foreground and background. The fourth bit in the video RAM
  data disables this mask, making the color display at full intensity
  regardless of the mask value.
  Actually the mask doesn't directly control intensity. The guns are rapidly
  turned on and off at a subpixel rate, relying on the monitor to blend the
  colors into a more or less uniform half intensity color.

  We use three groups of 8 pens: the first is fixed and contains the 8
  possible colors; the other two are dynamically modified when the mask
  register is written to, one is for the background, the other for sprites.

***************************************************************************/
void kangaroo_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
{
    int i;


    for (i = 0;i < Machine->drv->total_colors;i++)
    {
        *(palette++) = ((i & 4) >> 2) * 0xff;
        *(palette++) = ((i & 2) >> 1) * 0xff;
        *(palette++) = ((i & 1) >> 0) * 0xff;
    }
}



/***************************************************************************

  Start the video hardware emulation.

***************************************************************************/
int kangaroo_vh_start(void)
{
    if ((tmpbitmap = osd_create_bitmap(Machine->drv->screen_width,Machine->drv->screen_height)) == 0)
        return 1;

    if ((tmpbitmap2 = osd_create_bitmap(Machine->drv->screen_width,Machine->drv->screen_height)) == 0)
    {
        osd_free_bitmap(tmpbitmap);
        return 1;
    }

    if ((videoram = malloc(Machine->drv->screen_width*Machine->drv->screen_height)) == 0)
    {
        osd_free_bitmap(tmpbitmap);
        osd_free_bitmap(tmpbitmap2);
    }

    return 0;
}



/***************************************************************************

  Stop the video hardware emulation.

***************************************************************************/
void kangaroo_vh_stop(void)
{
    osd_free_bitmap(tmpbitmap2);
    osd_free_bitmap(tmpbitmap);
    free(videoram);
}


void kangaroo_video_control_w(int offset,int data)
{
    /* A & B bitmap control latch (A=playfield B=motion)
          bit 5 FLIP A
          bit 4 FLIP B
          bit 3 EN A
          bit 2 EN B
          bit 1 PRI A
          bit 0 PRI B */

    if ((*kangaroo_video_control & 0x30) != (data & 0x30))
    {
        screen_flipped = 1;
    }

    *kangaroo_video_control = data;
}


void kangaroo_bank_select_w(int offset,int data)
{
    unsigned char *RAM = memory_region(REGION_CPU1);


    /* this is a VERY crude way to handle the banked ROMs - but it's */
    /* correct enough to pass the self test */
    if (data & 0x05)
        cpu_setbank(1,&RAM[0x10000])
    else
        cpu_setbank(1,&RAM[0x12000]);

    *kangaroo_bank_select = data;
}



void kangaroo_color_mask_w(int offset,int data)
{
    int i;


    /* color mask for A plane */
    for (i = 0;i < 8;i++)
    {
        int r,g,b;


        r = ((i & 4) >> 2) * ((data & 0x20) ? 0xff : 0x7f);
        g = ((i & 2) >> 1) * ((data & 0x10) ? 0xff : 0x7f);
        b = ((i & 1) >> 0) * ((data & 0x08) ? 0xff : 0x7f);

        palette_change_color(8+i,r,g,b);
    }

    /* color mask for B plane */
    for (i = 0;i < 8;i++)
    {
        int r,g,b;


        r = ((i & 4) >> 2) * ((data & 0x04) ? 0xff : 0x7f);
        g = ((i & 2) >> 1) * ((data & 0x02) ? 0xff : 0x7f);
        b = ((i & 1) >> 0) * ((data & 0x01) ? 0xff : 0x7f);

        palette_change_color(16+i,r,g,b);
    }
}



void kangaroo_blitter_w(int offset,int data)
{
    kangaroo_blitter[offset] = data;

    if (offset == 5)    /* trigger DMA */
    {
        int src,dest;
        int x,y,xb,yb,old_bank_select,new_bank_select;

        src = kangaroo_blitter[0] + 256 * kangaroo_blitter[1];
        dest = kangaroo_blitter[2] + 256 * kangaroo_blitter[3];

        xb = kangaroo_blitter[5];
        yb = kangaroo_blitter[4];
        /* kangaroo_blitter[6] (vertical start address in bitmap) and */
        /* kangaroo_blitter[7] (horizontal start address in bitmap) seem */
        /* to be always 0 */

        old_bank_select = new_bank_select = *kangaroo_bank_select;

        if (new_bank_select & 0x0c)  new_bank_select |= 0x0c;
        if (new_bank_select & 0x03)  new_bank_select |= 0x03;
        kangaroo_bank_select_w(0, new_bank_select & 0x05);

        for (x = 0;x <= xb;x++)
        {
            for (y = 0;y <= yb;y++)
            {
                cpu_writemem16(dest++, cpu_readmem16(src++));
            }

            dest = dest - (yb + 1) + 256;
        }

        src = kangaroo_blitter[0] + 256 * kangaroo_blitter[1];
        dest = kangaroo_blitter[2] + 256 * kangaroo_blitter[3];

        kangaroo_bank_select_w(0, new_bank_select & 0x0a);

        for (x = 0;x <= xb;x++)
        {
            for (y = 0;y <= yb;y++)
            {
                cpu_writemem16(dest++, cpu_readmem16(src++));
            }

            dest = dest - (yb + 1) + 256;
        }

        kangaroo_bank_select_w(0, old_bank_select);
    }
}



INLINE void kangaroo_plot_pixel(struct osd_bitmap *bitmap, int x, int y, int col, int color_base, int flip)
{
    if (flip)
    {
        x = bitmap->width - 1 - x;
        y = bitmap->height - 1 - y;
    }

    plot_pixel(bitmap, x, y, Machine->pens[((col & 0x08) ? 0 : color_base) + (col & 0x07)]);
}

INLINE void kangaroo_redraw_4pixels(int x, int y)
{
    int offs, flipA, flipB;


    offs = y * 256 + x;

    flipA = *kangaroo_video_control & 0x20;
    flipB = *kangaroo_video_control & 0x10;

    kangaroo_plot_pixel(tmpbitmap , x  , y, videoram[offs  ] & 0x0f, 8,  flipA);
    kangaroo_plot_pixel(tmpbitmap , x+1, y, videoram[offs+1] & 0x0f, 8,  flipA);
    kangaroo_plot_pixel(tmpbitmap , x+2, y, videoram[offs+2] & 0x0f, 8,  flipA);
    kangaroo_plot_pixel(tmpbitmap , x+3, y, videoram[offs+3] & 0x0f, 8,  flipA);
    kangaroo_plot_pixel(tmpbitmap2, x  , y, videoram[offs  ] >> 4,   16, flipB);
    kangaroo_plot_pixel(tmpbitmap2, x+1, y, videoram[offs+1] >> 4,   16, flipB);
    kangaroo_plot_pixel(tmpbitmap2, x+2, y, videoram[offs+2] >> 4,   16, flipB);
    kangaroo_plot_pixel(tmpbitmap2, x+3, y, videoram[offs+3] >> 4,   16, flipB);
}

void kangaroo_videoram_w(int offset,int data)
{
    int a_Z_R,a_G_B,b_Z_R,b_G_B;
    int sx, sy, offs;

    a_Z_R = *kangaroo_bank_select & 0x01;
    a_G_B = *kangaroo_bank_select & 0x02;
    b_Z_R = *kangaroo_bank_select & 0x04;
    b_G_B = *kangaroo_bank_select & 0x08;


    sx = (offset / 256) * 4;
    sy = offset % 256;
    offs = sy * 256 + sx;

    if (a_G_B)
    {
        videoram[offs  ] = (videoram[offs  ] & 0xfc) | ((data & 0x10) >> 3) | ((data & 0x01) >> 0);
        videoram[offs+1] = (videoram[offs+1] & 0xfc) | ((data & 0x20) >> 4) | ((data & 0x02) >> 1);
        videoram[offs+2] = (videoram[offs+2] & 0xfc) | ((data & 0x40) >> 5) | ((data & 0x04) >> 2);
        videoram[offs+3] = (videoram[offs+3] & 0xfc) | ((data & 0x80) >> 6) | ((data & 0x08) >> 3);
    }

    if (a_Z_R)
    {
        videoram[offs  ] = (videoram[offs  ] & 0xf3) | ((data & 0x10) >> 1) | ((data & 0x01) << 2);
        videoram[offs+1] = (videoram[offs+1] & 0xf3) | ((data & 0x20) >> 2) | ((data & 0x02) << 1);
        videoram[offs+2] = (videoram[offs+2] & 0xf3) | ((data & 0x40) >> 3) | ((data & 0x04) >> 0);
        videoram[offs+3] = (videoram[offs+3] & 0xf3) | ((data & 0x80) >> 4) | ((data & 0x08) >> 1);
    }

    if (b_G_B)
    {
        videoram[offs  ] = (videoram[offs  ] & 0xcf) | ((data & 0x10) << 1) | ((data & 0x01) << 4);
        videoram[offs+1] = (videoram[offs+1] & 0xcf) | ((data & 0x20) >> 0) | ((data & 0x02) << 3);
        videoram[offs+2] = (videoram[offs+2] & 0xcf) | ((data & 0x40) >> 1) | ((data & 0x04) << 2);
        videoram[offs+3] = (videoram[offs+3] & 0xcf) | ((data & 0x80) >> 2) | ((data & 0x08) << 1);
    }

    if (b_Z_R)
    {
        videoram[offs  ] = (videoram[offs  ] & 0x3f) | ((data & 0x10) << 3) | ((data & 0x01) << 6);
        videoram[offs+1] = (videoram[offs+1] & 0x3f) | ((data & 0x20) << 2) | ((data & 0x02) << 5);
        videoram[offs+2] = (videoram[offs+2] & 0x3f) | ((data & 0x40) << 1) | ((data & 0x04) << 4);
        videoram[offs+3] = (videoram[offs+3] & 0x3f) | ((data & 0x80) << 0) | ((data & 0x08) << 3);
    }

    kangaroo_redraw_4pixels(sx, sy);
}



/***************************************************************************

  Draw the game screen in the given osd_bitmap.
  Do NOT call osd_update_display() from this function, it will be called by
  the main emulation engine.

***************************************************************************/
void kangaroo_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
    if (palette_recalc() || screen_flipped)
    {
        int x, y;

        /* redraw bitmap */
        for (x = 0; x < 256; x+=4)
        {
            for (y = 0; y < 256; y++)
            {
                kangaroo_redraw_4pixels(x, y);
            }
        }

        screen_flipped = 0;
    }

    if (*kangaroo_bank_select & 0x01)
    {
        /* Plane B is primary */
        copybitmap(bitmap,tmpbitmap2,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
        copybitmap(bitmap,tmpbitmap ,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_COLOR,8);
    }
    else
    {
        /* Plane A is primary */
        copybitmap(bitmap,tmpbitmap, 0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
        copybitmap(bitmap,tmpbitmap2,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_COLOR,16);
    }
}
