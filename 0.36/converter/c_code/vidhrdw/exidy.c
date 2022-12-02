/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"

unsigned char *exidy_characterram;
unsigned char *exidy_color_latch;
unsigned char *exidy_sprite_no;
unsigned char *exidy_sprite_enable;
unsigned char *exidy_sprite1_xpos;
unsigned char *exidy_sprite1_ypos;
unsigned char *exidy_sprite2_xpos;
unsigned char *exidy_sprite2_ypos;

int exidy_collision;
int exidy_collision_counter;

static struct osd_bitmap *motion_object_1_vid;
static struct osd_bitmap *motion_object_2_vid;

static unsigned char exidy_dirtycharacter[256];

#define COLOR_BLUE 0
#define COLOR_GREEN 1
#define COLOR_RED 2

/***************************************************************************
exidy_vh_start
***************************************************************************/

int exidy_vh_start(void)
{
    exidy_collision_counter = 0;

    if (generic_vh_start()!=0)
        return 1;

    if ((motion_object_1_vid = osd_create_bitmap(16,16)) == 0)
    {
        generic_vh_stop();
        return 1;
    }

    if ((motion_object_2_vid = osd_create_bitmap(16,16)) == 0)
    {
        osd_free_bitmap(motion_object_1_vid);
        generic_vh_stop();
        return 1;
    }

    return 0;
}

/***************************************************************************
exidy_vh_stop
***************************************************************************/

void exidy_vh_stop(void)
{
    osd_free_bitmap(motion_object_1_vid);
    osd_free_bitmap(motion_object_2_vid);
    generic_vh_stop();
}

/***************************************************************************
exidy_characterram_w
***************************************************************************/

void exidy_characterram_w(int offset,int data)
{
	if (exidy_characterram[offset] != data)
	{
		exidy_dirtycharacter[offset / 8 % 256] = 1;

		exidy_characterram[offset] = data;
	}
}

/***************************************************************************
exidy_color_w
***************************************************************************/

void exidy_color_w(int offset,int data)
{
	int i;

	exidy_color_latch[offset]=data;

	for (i=0;i<8;i++)
	{
		int r,g,b;

		r=((exidy_color_latch[COLOR_RED]>>i) & 0x01) * 0xFF;
		g=((exidy_color_latch[COLOR_GREEN]>>i) & 0x01) * 0xFF;
		b=((exidy_color_latch[COLOR_BLUE]>>i) & 0x01) * 0xFF;

		palette_change_color(i,r,g,b);
	}
}

/***************************************************************************
exidy_check_collision

It might seem strange to put the collision-checking routine in vidhrdw.
However, Exidy hardware checks for two types of collisions based on the
video signals.  If the Motion Object 1 and Motion Object 2 signals are
on at the same time, an M1M2 collision bit gets set.  If the Motion Object 1
and Background Character signals are on at the same time, an M1CHAR
collision bit gets set.  So effectively, there's a pixel-by-pixel collision
check comparing Motion Object 1 (the player) to the background and to the
other Motion Object (typically a bad guy).

Now, where things get a little weird is in how these bits get checked.
These bits can cause IRQs, depending on whether or not certain traces on
the board are cut.  Targ, Spectar, and Pepper II don't even check these
collision bits.  Venture generates an IRQ for each collision but only
checks the VBLANK bit.  MouseTrap should generate an IRQ for each
collision, but the game works just by setting the two collision bits
once per frame.

We can't just check the color of the main bitmap at a given location, because one
of our video signals might have overdrawn another one.  So here's what we do:
1)  Redraw the background, Motion Object 1, and Motion Object 2 into separate
bitmaps, but clip to where Motion Object 2 is located.
2)  Scan through the bitmaps and look for bits that are on at the same time.
3)  Save the number of scanlines causing collisions for Venture.
***************************************************************************/

void exidy_check_collision(struct osd_bitmap *bitmap)
{
    int sx,sy,org_x,org_y;
    struct rectangle clip;
    unsigned char enable_set=0;
    int collision;

    /* reset collision flags */
    exidy_collision &= 0xEB;

    clip.min_x=0;
    clip.max_x=15;
    clip.min_y=0;
    clip.max_y=15;

    org_x = 236-*exidy_sprite1_xpos-4;
    org_y = 244-*exidy_sprite1_ypos-4;
    if ((*exidy_sprite_enable&0x20)==0x20)
        enable_set=1;

    fillbitmap(motion_object_1_vid,Machine->pens[0],&clip);
    fillbitmap(motion_object_2_vid,Machine->pens[0],&clip);

    /* Draw Motion Object 1 */
    drawgfx(motion_object_1_vid,Machine->gfx[1],
	    (*exidy_sprite_no & 0x0F)+16*enable_set,0,
	    0,0,0,0,&clip,TRANSPARENCY_NONE,0);

    /* Draw Motion Object 2 clipped to Motion Object 1's location */
    if (!(*exidy_sprite_enable&0x40))
    {
        sx = (236-*exidy_sprite2_xpos-4)-org_x;
        sy = (244-*exidy_sprite2_ypos-4)-org_y;

        drawgfx(motion_object_2_vid,Machine->gfx[1],
	        ((*exidy_sprite_no>>4) & 0x0F)+32,1,
	        0,0,sx,sy,&clip,TRANSPARENCY_NONE,0);
    }

    /* Now check for Collision */
    for (sy=0;sy<16;sy++)
    {
	    for (sx=0;sx<16;sx++)
	    {
    		if (read_pixel(motion_object_1_vid, sx, sy) != Machine->pens[0])
    		{
                collision=0;

                /* Check for background collision (M1CHAR) */
    		    if (read_pixel(bitmap, org_x+sx, org_y+sy) != Machine->pens[0])
                {
    			    exidy_collision|=0x04;
                    collision=1;
                }

                /* Check for Motion Object collision (M1M2) */
                if (read_pixel(motion_object_2_vid, sx, sy) != Machine->pens[0])
                {
                    exidy_collision|=0x10;
                    collision=1;
                }

                exidy_collision_counter+=collision;
            }
    	}
    }
}



/***************************************************************************

  Draw the game screen in the given osd_bitmap.
  Do NOT call osd_update_display() from this function, it will be called by
  the main emulation engine.

***************************************************************************/
void exidy_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs,i;


	if (palette_recalc())
		memset(dirtybuffer,1,videoram_size);

	/* for every character in the Video RAM, check if it has been modified */
	/* since last time and update it accordingly. */
	for (offs = videoram_size - 1;offs >= 0;offs--)
	{
		int charcode;


		charcode = videoram[offs];

		if (dirtybuffer[offs] || exidy_dirtycharacter[charcode])
		{
			int sx,sy,color;


		/* decode modified characters */
			if (exidy_dirtycharacter[charcode] == 1)
			{
				decodechar(Machine->gfx[0],charcode,exidy_characterram,
						Machine->drv->gfxdecodeinfo[0].gfxlayout);
				exidy_dirtycharacter[charcode] = 2;
			}


			dirtybuffer[offs] = 0;

			sx = 8 * (offs % 32);
			sy = 8 * (offs / 32);
			color = (charcode & 0xC0) >> 6;
			drawgfx(tmpbitmap,Machine->gfx[0],
					charcode,color,
					0,0,sx,sy,
					&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
		}
	}


	for (i = 0;i < 256;i++)
	{
		if (exidy_dirtycharacter[i] == 2)
			exidy_dirtycharacter[i] = 0;
	}

	/* copy the character mapped graphics */
	copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);

    exidy_check_collision(bitmap);

	/* Draw the sprites */
	{
		int sx,sy;
        unsigned char enable_set = 0;

		/* Draw Motion Object 2 first. */
		if (!(*exidy_sprite_enable&0x40))
		{
			sx = 236-*exidy_sprite2_xpos-4;
			sy = 244-*exidy_sprite2_ypos-4;

			drawgfx(bitmap,Machine->gfx[1],
				((*exidy_sprite_no>>4) & 0x0F)+32,1,
				0,0,
				sx,sy,
				&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
		}

		/* Now draw Motion Object 1. */
		if (!(*exidy_sprite_enable&0x80) || *exidy_sprite_enable&0x10)
		{
			sx = 236-*exidy_sprite1_xpos-4;
			sy = 244-*exidy_sprite1_ypos-4;

            if (sy < 0) sy = 0;

            if ((*exidy_sprite_enable&0x20)==0x20)
                enable_set=1;

			drawgfx(bitmap,Machine->gfx[1],
				(*exidy_sprite_no & 0x0F)+16*enable_set,0,
				0,0,
				sx,sy,
				&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
		}

	}
}

