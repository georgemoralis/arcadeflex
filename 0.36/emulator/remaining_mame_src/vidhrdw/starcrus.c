
/* Ramtek - Star Cruiser */

#include "driver.h"
#include "vidhrdw/generic.h"

static struct osd_bitmap *ship1_vid;
static struct osd_bitmap *ship2_vid;
static struct osd_bitmap *proj1_vid;
static struct osd_bitmap *proj2_vid;

static int s1_x = 0;
static int s1_y = 0;
static int s2_x = 0;
static int s2_y = 0;
static int p1_x = 0;
static int p1_y = 0;
static int p2_x = 0;
static int p2_y = 0;

static int p1_sprite = 0;
static int p2_sprite = 0;
static int s1_sprite = 0;
static int s2_sprite = 0;

static int engine1_on = 0;
static int engine2_on = 0;
static int explode1_on = 0;
static int explode2_on = 0;
static int launch1_on = 0;
static int launch2_on = 0;

/* The collision detection techniques use in this driver
   are well explained in the comments in the sprint2 driver */

static int collision_reg = 0x00;

/* I hate to have sound in vidhrdw, but the sprite and
   audio bits are in the same bytes, and there are so few
   samples... */

int starcrus_engine_sound_playing = 0;
int starcrus_explode_sound_playing = 0;
int starcrus_launch1_sound_playing = 0;
int starcrus_launch2_sound_playing = 0;

void starcrus_s1_x_w(int offset, int data) { s1_x = data^0xff; }
void starcrus_s1_y_w(int offset, int data) { s1_y = data^0xff; }
void starcrus_s2_x_w(int offset, int data) { s2_x = data^0xff; }
void starcrus_s2_y_w(int offset, int data) { s2_y = data^0xff; }
void starcrus_p1_x_w(int offset, int data) { p1_x = data^0xff; }
void starcrus_p1_y_w(int offset, int data) { p1_y = data^0xff; }
void starcrus_p2_x_w(int offset, int data) { p2_x = data^0xff; }
void starcrus_p2_y_w(int offset, int data) { p2_y = data^0xff; }

int starcrus_vh_start(void)
{
	if ((ship1_vid = osd_create_bitmap(16,16)) == 0)
	{
		return 1;
	}

	if ((ship2_vid = osd_create_bitmap(16,16)) == 0)
	{
		osd_free_bitmap(ship1_vid);
		return 1;
	}

	if ((proj1_vid = osd_create_bitmap(16,16)) == 0)
	{
		osd_free_bitmap(ship1_vid);
		osd_free_bitmap(ship2_vid);
		return 1;
	}

	if ((proj2_vid = osd_create_bitmap(16,16)) == 0)
	{
		osd_free_bitmap(ship1_vid);
		osd_free_bitmap(ship2_vid);
		osd_free_bitmap(proj1_vid);
		return 1;
	}

	return 0;
}

void starcrus_vh_stop(void)
{
	osd_free_bitmap(ship1_vid);
	osd_free_bitmap(ship2_vid);
	osd_free_bitmap(proj1_vid);
	osd_free_bitmap(proj2_vid);
}

void starcrus_ship_parm_1_w(int offset, int data)
{
    s1_sprite = data&0x1f;
    engine1_on = ((data&0x20)>>5)^0x01;

    if (engine1_on || engine2_on)
    {
		if (starcrus_engine_sound_playing == 0)
		{
        	starcrus_engine_sound_playing = 1;
        	sample_start(0,0,1);	/* engine sample */

		}
    }
    else
    {
		if (starcrus_engine_sound_playing == 1)
		{
        	starcrus_engine_sound_playing = 0;
			sample_stop(0);
		}
	}
}

void starcrus_ship_parm_2_w(int offset, int data)
{
    s2_sprite = data&0x1f;
    osd_led_w(2, ((data&0x80)>>7)^0x01); 		/* game over lamp */
    coin_counter_w(0, ((data&0x40)>>6)^0x01); 	/* coin counter */
    engine2_on = ((data&0x20)>>5)^0x01;

    if (engine1_on || engine2_on)
    {
		if (starcrus_engine_sound_playing == 0)
		{
        	starcrus_engine_sound_playing = 1;
        	sample_start(0,0,1);	/* engine sample */
		}
    }
    else
    {
		if (starcrus_engine_sound_playing == 1)
		{
        	starcrus_engine_sound_playing = 0;
			sample_stop(0);
		}
	}

}

void starcrus_proj_parm_1_w(int offset, int data)
{
    p1_sprite = data&0x0f;
    launch1_on = ((data&0x20)>>5)^0x01;
    explode1_on = ((data&0x10)>>4)^0x01;

    if (explode1_on || explode2_on)
    {
		if (starcrus_explode_sound_playing == 0)
		{
			starcrus_explode_sound_playing = 1;
			sample_start(1,1,1);	/* explosion initial sample */
		}
	}
	else
    {
		if (starcrus_explode_sound_playing == 1)
		{
			starcrus_explode_sound_playing = 0;
			sample_start(1,2,0);	/* explosion ending sample */
		}
	}

	if (launch1_on)
	{
		if (starcrus_launch1_sound_playing == 0)
		{
			starcrus_launch1_sound_playing = 1;
			sample_start(2,3,0);	/* launch sample */
		}
	}
	else
	{
		starcrus_launch1_sound_playing = 0;
	}
}

void starcrus_proj_parm_2_w(int offset, int data)
{
    p2_sprite = data&0x0f;
    launch2_on = ((data&0x20)>>5)^0x01;
    explode2_on = ((data&0x10)>>4)^0x01;

    if (explode1_on || explode2_on)
    {
		if (starcrus_explode_sound_playing == 0)
		{
			starcrus_explode_sound_playing = 1;
			sample_start(1,1,1);	/* explosion initial sample */
		}
	}
	else
    {
		if (starcrus_explode_sound_playing == 1)
		{
			starcrus_explode_sound_playing = 0;
			sample_start(1,2,0);	/* explosion ending sample */
		}
	}

	if (launch2_on)
	{
		if (starcrus_launch2_sound_playing == 0)
		{
			starcrus_launch2_sound_playing = 1;
			sample_start(3,3,0);	/* launch sample */
		}
	}
	else
	{
		starcrus_launch2_sound_playing = 0;
	}
}

int starcrus_collision_check_s1s2(void)
{
	int org_x, org_y;
	int sx, sy;
	struct rectangle clip;

    clip.min_x=0;
    clip.max_x=15;
    clip.min_y=0;
    clip.max_y=15;

    fillbitmap(ship1_vid,Machine->pens[0],&clip);
    fillbitmap(ship2_vid,Machine->pens[0],&clip);

	/* origin is with respect to ship1 */

	org_x = s1_x;
	org_y = s1_y;

	/* Draw ship 1 */
    drawgfx(ship1_vid,
            Machine->gfx[8+((s1_sprite&0x04)>>2)],
            (s1_sprite&0x03)^0x03,
            0,
            (s1_sprite&0x08)>>3,(s1_sprite&0x10)>>4,
            s1_x-org_x,s1_y-org_y,
            &clip,
            TRANSPARENCY_NONE,
            0);

	/* Draw ship 2 */
    drawgfx(ship2_vid,
            Machine->gfx[10+((s2_sprite&0x04)>>2)],
            (s2_sprite&0x03)^0x03,
            0,
            (s2_sprite&0x08)>>3,(s2_sprite&0x10)>>4,
            s2_x-org_x,s2_y-org_y,
            &clip,
            TRANSPARENCY_NONE,
            0);

    /* Now check for collisions */
    for (sy=0;sy<16;sy++)
    {
        for (sx=0;sx<16;sx++)
        {
        	if (read_pixel(ship1_vid, sx, sy)==Machine->pens[1])
           	{
        		/* Condition 1 - ship 1 = ship 2 */
				if (read_pixel(ship2_vid, sx, sy)==Machine->pens[1])
                	return 1;
			}
        }
    }

    return 0;
}

int starcrus_collision_check_p1p2(void)
{
	int org_x, org_y;
	int sx, sy;
	struct rectangle clip;

	/* if both are scores, return */
	if ( ((p1_sprite & 0x08) == 0) &&
         ((p2_sprite & 0x08) == 0) )
	{
		return 0;
	}

    clip.min_x=0;
    clip.max_x=15;
    clip.min_y=0;
    clip.max_y=15;

    fillbitmap(proj1_vid,Machine->pens[0],&clip);
    fillbitmap(proj2_vid,Machine->pens[0],&clip);

	/* origin is with respect to proj1 */

	org_x = p1_x;
	org_y = p1_y;

	if (p1_sprite & 0x08)	/* if p1 is a projectile */
	{
		/* Draw score/projectile 1 */
		drawgfx(proj1_vid,
				Machine->gfx[(p1_sprite&0x0c)>>2],
				(p1_sprite&0x03)^0x03,
				0,
				0,0,
				p1_x-org_x,p1_y-org_y,
				&clip,
				TRANSPARENCY_NONE,
				0);
	}

	if (p2_sprite & 0x08)	/* if p2 is a projectile */
	{
		/* Draw score/projectile 2 */
		drawgfx(proj2_vid,
				Machine->gfx[4+((p2_sprite&0x0c)>>2)],
				(p2_sprite&0x03)^0x03,
				0,
				0,0,
				p2_x-org_x,p2_y-org_y,
				&clip,
				TRANSPARENCY_NONE,
				0);
	}

    /* Now check for collisions */
    for (sy=0;sy<16;sy++)
    {
        for (sx=0;sx<16;sx++)
        {
        	if (read_pixel(proj1_vid, sx, sy)==Machine->pens[1])
           	{
        		/* Condition 1 - proj 1 = proj 2 */
				if (read_pixel(proj2_vid, sx, sy)==Machine->pens[1])
                	return 1;
			}
        }
    }

    return 0;
}

int starcrus_collision_check_s1p1p2(void)
{
	int org_x, org_y;
	int sx, sy;
	struct rectangle clip;

	/* if both are scores, return */
	if ( ((p1_sprite & 0x08) == 0) &&
         ((p2_sprite & 0x08) == 0) )
	{
		return 0;
	}

    clip.min_x=0;
    clip.max_x=15;
    clip.min_y=0;
    clip.max_y=15;

    fillbitmap(ship1_vid,Machine->pens[0],&clip);
    fillbitmap(proj1_vid,Machine->pens[0],&clip);
    fillbitmap(proj2_vid,Machine->pens[0],&clip);

	/* origin is with respect to ship1 */

	org_x = s1_x;
	org_y = s1_y;

	/* Draw ship 1 */
    drawgfx(ship1_vid,
            Machine->gfx[8+((s1_sprite&0x04)>>2)],
            (s1_sprite&0x03)^0x03,
            0,
            (s1_sprite&0x08)>>3,(s1_sprite&0x10)>>4,
            s1_x-org_x,s1_y-org_y,
            &clip,
            TRANSPARENCY_NONE,
            0);

	if (p1_sprite & 0x08)	/* if p1 is a projectile */
	{
		/* Draw projectile 1 */
		drawgfx(proj1_vid,
				Machine->gfx[(p1_sprite&0x0c)>>2],
				(p1_sprite&0x03)^0x03,
				0,
				0,0,
				p1_x-org_x,p1_y-org_y,
				&clip,
				TRANSPARENCY_NONE,
				0);
	}

	if (p2_sprite & 0x08)	/* if p2 is a projectile */
	{
		/* Draw projectile 2 */
		drawgfx(proj2_vid,
				Machine->gfx[4+((p2_sprite&0x0c)>>2)],
				(p2_sprite&0x03)^0x03,
				0,
				0,0,
				p2_x-org_x,p2_y-org_y,
				&clip,
				TRANSPARENCY_NONE,
				0);
	}

    /* Now check for collisions */
    for (sy=0;sy<16;sy++)
    {
        for (sx=0;sx<16;sx++)
        {
        	if (read_pixel(ship1_vid, sx, sy)==Machine->pens[1])
           	{
        		/* Condition 1 - ship 1 = proj 1 */
				if (read_pixel(proj1_vid, sx, sy)==Machine->pens[1])
                	return 1;
        		/* Condition 2 - ship 1 = proj 2 */
        		if (read_pixel(proj2_vid, sx, sy)==Machine->pens[1])
                	return 1;
            }
        }
    }

    return 0;
}

int starcrus_collision_check_s2p1p2(void)
{
	int org_x, org_y;
	int sx, sy;
	struct rectangle clip;

	/* if both are scores, return */
	if ( ((p1_sprite & 0x08) == 0) &&
         ((p2_sprite & 0x08) == 0) )
	{
		return 0;
	}

    clip.min_x=0;
    clip.max_x=15;
    clip.min_y=0;
    clip.max_y=15;

    fillbitmap(ship2_vid,Machine->pens[0],&clip);
    fillbitmap(proj1_vid,Machine->pens[0],&clip);
    fillbitmap(proj2_vid,Machine->pens[0],&clip);

	/* origin is with respect to ship2 */

	org_x = s2_x;
	org_y = s2_y;

	/* Draw ship 2 */
    drawgfx(ship2_vid,
            Machine->gfx[10+((s2_sprite&0x04)>>2)],
            (s2_sprite&0x03)^0x03,
            0,
            (s2_sprite&0x08)>>3,(s2_sprite&0x10)>>4,
            s2_x-org_x,s2_y-org_y,
            &clip,
            TRANSPARENCY_NONE,
            0);

	if (p1_sprite & 0x08)	/* if p1 is a projectile */
	{
		/* Draw projectile 1 */
		drawgfx(proj1_vid,
				Machine->gfx[(p1_sprite&0x0c)>>2],
				(p1_sprite&0x03)^0x03,
				0,
				0,0,
				p1_x-org_x,p1_y-org_y,
				&clip,
				TRANSPARENCY_NONE,
				0);
	}

	if (p2_sprite & 0x08)	/* if p2 is a projectile */
	{
		/* Draw projectile 2 */
		drawgfx(proj2_vid,
				Machine->gfx[4+((p2_sprite&0x0c)>>2)],
				(p2_sprite&0x03)^0x03,
				0,
				0,0,
				p2_x-org_x,p2_y-org_y,
				&clip,
				TRANSPARENCY_NONE,
				0);
	}

    /* Now check for collisions */
    for (sy=0;sy<16;sy++)
    {
        for (sx=0;sx<16;sx++)
        {
        	if (read_pixel(ship2_vid, sx, sy)==Machine->pens[1])
           	{
        		/* Condition 1 - ship 2 = proj 1 */
				if (read_pixel(proj1_vid, sx, sy)==Machine->pens[1])
                	return 1;
        		/* Condition 2 - ship 2 = proj 2 */
        		if (read_pixel(proj2_vid, sx, sy)==Machine->pens[1])
                	return 1;
            }
        }
    }

    return 0;
}

void starcrus_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
    fillbitmap(bitmap,Machine->pens[0],&Machine->drv->visible_area);

	/* Draw ship 1 */
    drawgfx(bitmap,
            Machine->gfx[8+((s1_sprite&0x04)>>2)],
            (s1_sprite&0x03)^0x03,
            0,
            (s1_sprite&0x08)>>3,(s1_sprite&0x10)>>4,
            s1_x,s1_y,
            &Machine->drv->visible_area,
            TRANSPARENCY_PEN,
            0);

	/* Draw ship 2 */
    drawgfx(bitmap,
            Machine->gfx[10+((s2_sprite&0x04)>>2)],
            (s2_sprite&0x03)^0x03,
            0,
            (s2_sprite&0x08)>>3,(s2_sprite&0x10)>>4,
            s2_x,s2_y,
            &Machine->drv->visible_area,
            TRANSPARENCY_PEN,
            0);

	/* Draw score/projectile 1 */
	drawgfx(bitmap,
            Machine->gfx[(p1_sprite&0x0c)>>2],
            (p1_sprite&0x03)^0x03,
            0,
            0,0,
            p1_x,p1_y,
            &Machine->drv->visible_area,
            TRANSPARENCY_PEN,
            0);

	/* Draw score/projectile 2 */
	drawgfx(bitmap,
            Machine->gfx[4+((p2_sprite&0x0c)>>2)],
            (p2_sprite&0x03)^0x03,
            0,
            0,0,
            p2_x,p2_y,
            &Machine->drv->visible_area,
            TRANSPARENCY_PEN,
            0);

    /* Collision detection */

    collision_reg = 0x00;

	/* Check for collisions between ship1 and ship2 */
    if (starcrus_collision_check_s1s2())
    {
        collision_reg |= 0x08;
    }
	/* Check for collisions between ship1 and projectiles */
    if (starcrus_collision_check_s1p1p2())
    {
        collision_reg |= 0x02;
    }
    /* Check for collisions between ship1 and projectiles */
    if (starcrus_collision_check_s2p1p2())
    {
        collision_reg |= 0x01;
    }
    /* Check for collisions between ship1 and projectiles */
    /* Note: I don't think this is used by the game */
    if (starcrus_collision_check_p1p2())
    {
        collision_reg |= 0x04;
    }

}

int starcrus_coll_det_r(int offset)
{
    return collision_reg ^ 0xff;
}
