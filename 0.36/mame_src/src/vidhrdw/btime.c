/***************************************************************************

    vidhrdw.c

    Functions to emulate the video hardware of the machine.

This file is also used by scregg.c

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"


unsigned char *lnc_charbank;
unsigned char *bnj_backgroundram;
unsigned char *zoar_scrollram;
unsigned char *deco_charram;
int bnj_backgroundram_size;

static int sprite_dirty[256];
static int char_dirty[1024];

static int flipscreen = 0;
static int btime_palette = 0;
static unsigned char bnj_scroll1 = 0;
static unsigned char bnj_scroll2 = 0;
static unsigned char *dirtybuffer2 = 0;
static struct osd_bitmap *background_bitmap;
static int lnc_sound_interrupt_enabled = 0;

/***************************************************************************

    Burger Time doesn't have a color PROM. It uses RAM to dynamically
    create the palette.
    The palette RAM is connected to the RGB output this way:

    bit 7 -- 15 kohm resistor  -- BLUE (inverted)
          -- 33 kohm resistor  -- BLUE (inverted)
          -- 15 kohm resistor  -- GREEN (inverted)
          -- 33 kohm resistor  -- GREEN (inverted)
          -- 47 kohm resistor  -- GREEN (inverted)
          -- 15 kohm resistor  -- RED (inverted)
          -- 33 kohm resistor  -- RED (inverted)
    bit 0 -- 47 kohm resistor  -- RED (inverted)

***************************************************************************/
void btime_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
{
    int i;


    /* Burger Time doesn't have a color PROM, but Hamburge has. */
    /* This function is also used by Eggs. */
    if (color_prom == 0) return;

    for (i = 0;i < Machine->drv->total_colors;i++)
    {
        int bit0,bit1,bit2;

        /* red component */
        bit0 = (*color_prom >> 0) & 0x01;
        bit1 = (*color_prom >> 1) & 0x01;
        bit2 = (*color_prom >> 2) & 0x01;
        *(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
        /* green component */
        bit0 = (*color_prom >> 3) & 0x01;
        bit1 = (*color_prom >> 4) & 0x01;
        bit2 = (*color_prom >> 5) & 0x01;
        *(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
        /* blue component */
        bit0 = 0;
        bit1 = (*color_prom >> 6) & 0x01;
        bit2 = (*color_prom >> 7) & 0x01;
        *(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;

        color_prom++;
    }
}

/***************************************************************************

    Convert the color PROMs into a more useable format.

    The PROM is connected to the RGB output this way:

    bit 7 -- 47 kohm resistor  -- RED
          -- 33 kohm resistor  -- RED
          -- 15 kohm resistor  -- RED
          -- 47 kohm resistor  -- GREEN
          -- 33 kohm resistor  -- GREEN
          -- 15 kohm resistor  -- GREEN
          -- 33 kohm resistor  -- BLUE
    bit 0 -- 15 kohm resistor  -- BLUE

***************************************************************************/
void lnc_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
{
    int i;


    for (i = 0;i < Machine->drv->total_colors;i++)
    {
        int bit0,bit1,bit2;

        /* red component */
        bit0 = (*color_prom >> 7) & 0x01;
        bit1 = (*color_prom >> 6) & 0x01;
        bit2 = (*color_prom >> 5) & 0x01;
        *(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
        /* green component */
        bit0 = (*color_prom >> 4) & 0x01;
        bit1 = (*color_prom >> 3) & 0x01;
        bit2 = (*color_prom >> 2) & 0x01;
        *(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
        /* blue component */
        bit0 = 0;
        bit1 = (*color_prom >> 1) & 0x01;
        bit2 = (*color_prom >> 0) & 0x01;
        *(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;

        color_prom++;
    }
}


void lnc_init_machine(void)
{
    *lnc_charbank = 1;
}


/***************************************************************************

Start the video hardware emulation.

***************************************************************************/
int bnj_vh_start (void)
{
    if (generic_vh_start() != 0)
        return 1;

    if ((dirtybuffer2 = malloc(bnj_backgroundram_size)) == 0)
    {
        generic_vh_stop();
        return 1;
    }
    memset(dirtybuffer2,1,bnj_backgroundram_size);

    /* the background area is twice as wide as the screen */
    if ((background_bitmap = osd_create_bitmap(2*Machine->drv->screen_width,Machine->drv->screen_height)) == 0)
    {
        free(dirtybuffer2);
        generic_vh_stop();
        return 1;
    }

    bnj_scroll1 = 0;
    bnj_scroll2 = 0;

    return 0;
}

int btime_vh_start (void)
{
    bnj_scroll1 = 0;
    bnj_scroll2 = 0;

    return generic_vh_start();
}

/***************************************************************************

Stop the video hardware emulation.

***************************************************************************/
void bnj_vh_stop (void)
{
    osd_free_bitmap(background_bitmap);
    free(dirtybuffer2);
    generic_vh_stop();
}


void btime_paletteram_w(int offset,int data)
{
    /* RGB output is inverted */
    paletteram_BBGGGRRR_w(offset,~data);
}

void lnc_videoram_w(int offset, int data)
{
    if (videoram[offset] != data || colorram[offset] != *lnc_charbank)
    {
        videoram[offset] = data;
        colorram[offset] = *lnc_charbank;

        dirtybuffer[offset] = 1;
    }
}

int btime_mirrorvideoram_r(int offset)
{
    int x,y;

    /* swap x and y coordinates */
    x = offset / 32;
    y = offset % 32;
    offset = 32 * y + x;

    return videoram_r(offset);
}

int btime_mirrorcolorram_r(int offset)
{
    int x,y;

    /* swap x and y coordinates */
    x = offset / 32;
    y = offset % 32;
    offset = 32 * y + x;

    return colorram_r(offset);
}

void btime_mirrorvideoram_w(int offset,int data)
{
    int x,y;

    /* swap x and y coordinates */
    x = offset / 32;
    y = offset % 32;
    offset = 32 * y + x;

    videoram_w(offset,data);
}

void lnc_mirrorvideoram_w(int offset,int data)
{
    int x,y;

    /* swap x and y coordinates */
    x = offset / 32;
    y = offset % 32;
    offset = 32 * y + x;

    lnc_videoram_w(offset,data);
}

void btime_mirrorcolorram_w(int offset,int data)
{
    int x,y;

    /* swap x and y coordinates */
    x = offset / 32;
    y = offset % 32;
    offset = 32 * y + x;

    colorram_w(offset,data);
}

void deco_charram_w(int offset,int data)
{
    if (deco_charram[offset] == data)  return;

    deco_charram[offset] = data;

    offset &= 0x1fff;

    /* dirty sprite */
    sprite_dirty[offset >> 5] = 1;

    /* diry char */
    char_dirty  [offset >> 3] = 1;
}

void bnj_background_w(int offset, int data)
{
    if (bnj_backgroundram[offset] != data)
    {
        dirtybuffer2[offset] = 1;

        bnj_backgroundram[offset] = data;
    }
}

void bnj_scroll1_w(int offset, int data)
{
    // Dirty screen if background is being turned off
    if (bnj_scroll1 && !data)
    {
        memset(dirtybuffer,1,videoram_size);
    }

    bnj_scroll1 = data;
}

void bnj_scroll2_w(int offset, int data)
{
    bnj_scroll2 = data;
}

void zoar_video_control_w(int offset,int data)
{
    // Zoar video control
    //
    // Bit 0-2 = Unknown (always 0). Marked as MCOL on schematics
    // Bit 3-4 = Palette
    // Bit 7   = Flip Screen

    static int video_control = -1;

    if (video_control != data)
    {
        memset(dirtybuffer,1,videoram_size);

        video_control = data;

        flipscreen =  data & 0x80;
        btime_palette    = (data & 0x30) >> 3;
    }
}

void btime_video_control_w(int offset,int data)
{
    // Btime video control
    //
    // Bit 0   = Flip screen
    // Bit 1-7 = Unknown

    static int video_control = -1;

    if (video_control != data)
    {
        memset(dirtybuffer,1,videoram_size);

        // This is used by Bump'n'Jump
        if (dirtybuffer2)
        {
            memset(dirtybuffer2,1,bnj_backgroundram_size);
        }

        video_control = data;

        flipscreen = data & 0x01;
    }
}

void bnj_video_control_w(int offset,int data)
{
    /* Bnj/Lnc works a little differently than the btime/eggs (apparently). */
    /* According to the information at: */
    /* http://www.davesclassics.com/arcade/Switch_Settings/BumpNJump.sw */
    /* SW8 is used for cocktail video selection (as opposed to controls), */
    /* but bit 7 of the input port is used for vblank input. */
    /* My guess is that this switch open circuits some connection to */
    /* the monitor hardware. */
    /* For now we just check 0x40 in DSW1, and ignore the write if we */
    /* are in upright controls mode. */

    if (input_port_3_r(0) & 0x40) /* cocktail mode */
        btime_video_control_w(offset, data);
}

void lnc_video_control_w(int offset,int data)
{
    // I have a feeling that this only works by coincidence. I couldn't
    // figure out how NMI's are disabled by the sound processor
    lnc_sound_interrupt_enabled = data & 0x08;

    bnj_video_control_w(offset, data & 0x01);
}

void disco_video_control_w(int offset,int data)
{
    static int video_control = -1;

    if (video_control != data)
    {
        memset(dirtybuffer,1,videoram_size);

        video_control = data;

        btime_palette = (data >> 2) & 0x03;

        if (!(input_port_3_r(0) & 0x40)) /* cocktail mode */
        {
            flipscreen = data & 0x01;
        }
    }
}


int lnc_sound_interrupt(void)
{
    if (lnc_sound_interrupt_enabled)
        return nmi_interrupt();
    else
        return ignore_interrupt();
}


/***************************************************************************

Draw the game screen in the given osd_bitmap.
Do NOT call osd_update_display() from this function, it will be called by
the main emulation engine.

***************************************************************************/
static void drawchars(struct osd_bitmap *bitmap, int transparency, int color, int priority)
{
    int offs;

    /* for every character in the Video RAM, check if it has been modified */
    /* since last time and update it accordingly. If the background is on, */
    /* draw characters as sprites */

    for (offs = videoram_size - 1;offs >= 0;offs--)
    {
        int sx,sy,code;

        if (!dirtybuffer[offs] && (bitmap == tmpbitmap)) continue;

        dirtybuffer[offs] = 0;

        code = videoram[offs] + 256 * (colorram[offs] & 3);

        /* check priority */
        if ((priority != -1) && (priority != ((code >> 7) & 0x01)))  continue;

        sx = 31 - (offs / 32);
        sy = offs % 32;

        if (flipscreen)
        {
            sx = 31 - sx;
            sy = 31 - sy;
        }

        drawgfx(bitmap,Machine->gfx[0],
                code,
                color,
                flipscreen,flipscreen,
                8*sx,8*sy,
                &Machine->drv->visible_area,transparency,0);
    }
}

static void drawsprites(struct osd_bitmap *bitmap, int color,
                        int sprite_y_adjust, int sprite_y_adjust_flipscreen,
                        unsigned char *sprite_ram, int interleave)
{
    int i,offs;

    /* Draw the sprites */
    for (i = 0, offs = 0;i < 8; i++, offs += 4*interleave)
    {
        int sx,sy,flipx,flipy;

        if (!(sprite_ram[offs + 0] & 0x01)) continue;

        sx = 240 - sprite_ram[offs + 3*interleave];
        sy = 240 - sprite_ram[offs + 2*interleave];

        flipx = sprite_ram[offs + 0] & 0x04;
        flipy = sprite_ram[offs + 0] & 0x02;

        if (flipscreen)
        {
            sx = 240 - sx;
            sy = 240 - sy + sprite_y_adjust_flipscreen;

            flipx = !flipx;
            flipy = !flipy;
        }

        sy -= sprite_y_adjust;

        drawgfx(bitmap,Machine->gfx[1],
                sprite_ram[offs + interleave],
                color,
                flipx,flipy,
                sx,sy,
                &Machine->drv->visible_area,TRANSPARENCY_PEN,0);

        sy += (flipscreen ? -256 : 256);

        // Wrap around
        drawgfx(bitmap,Machine->gfx[1],
                sprite_ram[offs + interleave],
                color,
                flipx,flipy,
                sx,sy,
                &Machine->drv->visible_area,TRANSPARENCY_PEN,0);
    }
}


static void drawbackground(struct osd_bitmap *bitmap, unsigned char* tilemap)
{
    int i, offs;

    int scroll = -(bnj_scroll2 | ((bnj_scroll1 & 0x03) << 8));

    // One extra iteration for wrap around
    for (i = 0; i < 5; i++, scroll += 256)
    {
        int tileoffset = tilemap[i & 3] * 0x100;

        // Skip if this title is completely off the screen
        if (scroll > 256)  break;
        if (scroll < -256) continue;

        for (offs = 0; offs < 0x100; offs++)
        {
            int sx,sy;

            sx = 240 - (16 * (offs / 16) + scroll);
            sy = 16 * (offs % 16);

            if (flipscreen)
            {
                sx = 240 - sx;
                sy = 240 - sy;
            }

            drawgfx(bitmap, Machine->gfx[2],
                    memory_region(REGION_GFX3)[tileoffset + offs],
                    btime_palette,
                    flipscreen,flipscreen,
                    sx,sy,
                    0,TRANSPARENCY_NONE,0);
        }
    }
}


static void decode_modified(unsigned char *sprite_ram, int interleave)
{
    int i,offs;


    /* decode dirty characters */
    for (offs = videoram_size - 1;offs >= 0;offs--)
    {
        int code;

        code = videoram[offs] + 256 * (colorram[offs] & 3);

        switch (char_dirty[code])
        {
        case 1:
            decodechar(Machine->gfx[0],code,deco_charram,Machine->drv->gfxdecodeinfo[0].gfxlayout);
            char_dirty[code] = 2;
            /* fall through */
        case 2:
            dirtybuffer[offs] = 1;
            break;
        default:
            break;
        }
    }

    for (i = 0; i < sizeof(char_dirty); i++)
    {
        if (char_dirty[i] == 2)  char_dirty[i] = 0;
    }

    /* decode dirty sprites */
    for (i = 0, offs = 0;i < 8; i++, offs += 4*interleave)
    {
        int code;

        code  = sprite_ram[offs + interleave];
        if (sprite_dirty[code])
        {
            sprite_dirty[code] = 0;

            decodechar(Machine->gfx[1],code,deco_charram,Machine->drv->gfxdecodeinfo[1].gfxlayout);
        }
    }
}


void btime_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
    if (palette_recalc())
        memset(dirtybuffer,1,videoram_size);

    if (bnj_scroll1 & 0x10)
    {
        int i, start;

        // Generate tile map
        static unsigned char btime_tilemap[4];

        if (flipscreen)
            start = 0;
        else
            start = 1;

        for (i = 0; i < 4; i++)
        {
            btime_tilemap[i] = start | (bnj_scroll1 & 0x04);
            start = (++start & 0x03);
        }

        drawbackground(bitmap, btime_tilemap);

        drawchars(bitmap, TRANSPARENCY_PEN, 0, -1);
    }
    else
    {
        drawchars(tmpbitmap, TRANSPARENCY_NONE, 0, -1);

        /* copy the temporary bitmap to the screen */
        copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
    }

    drawsprites(bitmap, 0, 1, 0, videoram, 0x20);
}


void eggs_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
    if (palette_recalc())
        memset(dirtybuffer,1,videoram_size);

    drawchars(tmpbitmap, TRANSPARENCY_NONE, 0, -1);

    /* copy the temporary bitmap to the screen */
    copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);

    drawsprites(bitmap, 0, 0, 0, videoram, 0x20);
}


void lnc_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
    if (palette_recalc())
        memset(dirtybuffer,1,videoram_size);

    drawchars(tmpbitmap, TRANSPARENCY_NONE, 0, -1);

    /* copy the temporary bitmap to the screen */
    copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);

    drawsprites(bitmap, 0, 1, 2, videoram, 0x20);
}


void zoar_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
    if (palette_recalc())
        memset(dirtybuffer,1,videoram_size);

    if (bnj_scroll1 & 0x04)
    {
        drawbackground(bitmap, zoar_scrollram);

        drawchars(bitmap, TRANSPARENCY_PEN, btime_palette + 1, -1);
    }
    else
    {
        drawchars(tmpbitmap, TRANSPARENCY_NONE, btime_palette + 1, -1);

        /* copy the temporary bitmap to the screen */
        copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
    }

    /* The order is important for correct priorities */
    drawsprites(bitmap, btime_palette + 1, 1, 2, videoram + 0x1f, 0x20);
    drawsprites(bitmap, btime_palette + 1, 1, 2, videoram,        0x20);
}


void bnj_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
    if (palette_recalc())
    {
        memset(dirtybuffer,1,videoram_size);
        memset(dirtybuffer2,1,bnj_backgroundram_size);
    }

    /*
     *  For each character in the background RAM, check if it has been
     *  modified since last time and update it accordingly.
     */
    if (bnj_scroll1)
    {
        int scroll, offs;

        for (offs = bnj_backgroundram_size-1; offs >=0; offs--)
        {
            int sx,sy;

            if (!dirtybuffer2[offs]) continue;

            dirtybuffer2[offs] = 0;

            sx = 16 * ((offs < 0x100) ? ((offs % 0x80) / 8) : ((offs % 0x80) / 8) + 16);
            sy = 16 * (((offs % 0x100) < 0x80) ? offs % 8 : (offs % 8) + 8);
            sx = 496 - sx;

            if (flipscreen)
            {
                sx = 496 - sx;
                sy = 240 - sy;
            }

            drawgfx(background_bitmap, Machine->gfx[2],
                    (bnj_backgroundram[offs] >> 4) + ((offs & 0x80) >> 3) + 32,
                    0,
                    flipscreen, flipscreen,
                    sx, sy,
                    0, TRANSPARENCY_NONE, 0);
        }

        /* copy the background bitmap to the screen */
        scroll = (bnj_scroll1 & 0x02) * 128 + 511 - bnj_scroll2;
        if (!flipscreen)
            scroll = 767-scroll;
        copyscrollbitmap (bitmap, background_bitmap, 1, &scroll, 0, 0, &Machine->drv->visible_area,TRANSPARENCY_NONE, 0);

        /* copy the low priority characters followed by the sprites
           then the high priority characters */
        drawchars(bitmap, TRANSPARENCY_PEN, 0, 1);
        drawsprites(bitmap, 0, 0, 0, videoram, 0x20);
        drawchars(bitmap, TRANSPARENCY_PEN, 0, 0);
    }
    else
    {
        drawchars(tmpbitmap, TRANSPARENCY_NONE, 0, -1);

        /* copy the temporary bitmap to the screen */
        copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);

        drawsprites(bitmap, 0, 0, 0, videoram, 0x20);
    }
}


void cookrace_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
    int offs;


    if (palette_recalc())
        memset(dirtybuffer,1,videoram_size);

    /*
     *  For each character in the background RAM, check if it has been
     *  modified since last time and update it accordingly.
     */
    for (offs = bnj_backgroundram_size-1; offs >=0; offs--)
    {
        int sx,sy;

        sx = 31 - (offs / 32);
        sy = offs % 32;

        if (flipscreen)
        {
            sx = 31 - sx;
            sy = 31 - sy;
        }

        drawgfx(bitmap, Machine->gfx[2],
                bnj_backgroundram[offs],
                0,
                flipscreen, flipscreen,
                8*sx,8*sy,
                0, TRANSPARENCY_NONE, 0);
    }

    drawchars(bitmap, TRANSPARENCY_PEN, 0, -1);

    drawsprites(bitmap, 0, 1, 0, videoram, 0x20);
}


void disco_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
    if (palette_recalc())
        memset(dirtybuffer,1,videoram_size);

    decode_modified(spriteram, 1);

    drawchars(tmpbitmap, TRANSPARENCY_NONE, btime_palette, -1);

    /* copy the temporary bitmap to the screen */
    copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);

    drawsprites(bitmap, btime_palette, 0, 0, spriteram, 1);
}







void decocass_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
    if (palette_recalc())
        memset(dirtybuffer,1,videoram_size);

    decode_modified(videoram, 0x20);

    drawchars(tmpbitmap, TRANSPARENCY_NONE, 0, -1);

    /* copy the temporary bitmap to the screen */
    copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);

    drawsprites(bitmap, 0, 0, 0, videoram, 0x20);
}
