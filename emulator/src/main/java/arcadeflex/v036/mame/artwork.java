/*
 * ported to 0.36
 */
package arcadeflex.v036.mame;

//mame imports
import static arcadeflex.v036.mame.artworkH.*;
import static arcadeflex.v036.mame.osdependH.*;
//common imports
import static common.libc.cstring.*;
//TODO
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.ORIENTATION_SWAP_XY;
import static gr.codebb.arcadeflex.v036.mame.driverH.VIDEO_MODIFIES_PALETTE;
import static gr.codebb.arcadeflex.v036.mame.driverH.VIDEO_TYPE_VECTOR;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.mame.errorlog;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.platform.video.osd_free_bitmap;
import static gr.codebb.arcadeflex.v036.platform.video.osd_get_pen;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.TRANSPARENCY_PEN;
import static gr.codebb.arcadeflex.v037b7.mame.palette.palette_change_color;
import static gr.codebb.arcadeflex.v037b7.mame.palette.palette_recalc;

public class artwork {

    /*TODO*///#ifndef MIN
/*TODO*///#define MIN(x,y) (x)<(y)?(x):(y)
/*TODO*///#endif
/*TODO*///#ifndef MAX
/*TODO*///#define MAX(x,y) (x)>(y)?(x):(y)
/*TODO*///#endif
/*TODO*///
/*TODO*///
/*TODO*////* Local variables */
/*TODO*///static unsigned char isblack[256];
/*TODO*///
/*TODO*////*
/*TODO*/// * finds closest color and returns the index (for 256 color)
/*TODO*/// */
/*TODO*///
/*TODO*///static unsigned char find_pen(unsigned char r,unsigned char g,unsigned char b)
/*TODO*///{
/*TODO*///	int i,bi,ii;
/*TODO*///	long x,y,z,bc;
/*TODO*///	ii = 32;
/*TODO*///	bi = 256;
/*TODO*///	bc = 0x01000000;
/*TODO*///
/*TODO*///	do
/*TODO*///	{
/*TODO*///		for( i=0; i<256; i++ )
/*TODO*///		{
/*TODO*///			unsigned char r1,g1,b1;
/*TODO*///
/*TODO*///			osd_get_pen(Machine->pens[i],&r1,&g1,&b1);
/*TODO*///			if((x=(long)(abs(r1-r)+1)) > ii) continue;
/*TODO*///			if((y=(long)(abs(g1-g)+1)) > ii) continue;
/*TODO*///			if((z=(long)(abs(b1-b)+1)) > ii) continue;
/*TODO*///			x = x*y*z;
/*TODO*///			if (x < bc)
/*TODO*///			{
/*TODO*///				bc = x;
/*TODO*///				bi = i;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		ii<<=1;
/*TODO*///	} while (bi==256);
/*TODO*///
/*TODO*///	return(bi);
/*TODO*///}
/*TODO*///
/*TODO*///void backdrop_refresh_tables (struct artwork *a)
/*TODO*///{
/*TODO*///	int i,j, k, total_colors;
/*TODO*///	unsigned char rgb1[3], rgb2[3], c[3];
/*TODO*///	unsigned short *pens = Machine->pens;
/*TODO*///
/*TODO*///	/* Calculate brightness of all colors */
/*TODO*///
/*TODO*///	total_colors = Machine->drv->total_colors;
/*TODO*///
/*TODO*///	for (i = 0; i < Machine->drv->total_colors; i++)
/*TODO*///	{
/*TODO*///		osd_get_pen (pens[i], &rgb1[0], &rgb1[1], &rgb1[2]);
/*TODO*///		a->brightness[pens[i]]=(222*rgb1[0]+707*rgb1[1]+71*rgb1[2])/1000;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* Calculate mixed colors */
/*TODO*///
/*TODO*///	for( i=0; i < total_colors ;i++ )                /* color1 */
/*TODO*///	{
/*TODO*///		osd_get_pen(pens[i],&rgb1[0],&rgb1[1],&rgb1[2]);
/*TODO*///		for( j=0; j < total_colors ;j++ )               /* color2 */
/*TODO*///		{
/*TODO*///			osd_get_pen(pens[j],&rgb2[0],&rgb2[1],&rgb2[2]);
/*TODO*///
/*TODO*///			for (k=0; k<3; k++)
/*TODO*///			{
/*TODO*///				int tmp;
/*TODO*///				tmp = rgb1[k]/4 + rgb2[k];
/*TODO*///				if (tmp > 255)
/*TODO*///					c[k] = 255;
/*TODO*///				else
/*TODO*///					c[k] = tmp;
/*TODO*///			}
/*TODO*///			a->pTable[i * total_colors + j] = find_pen(c[0],c[1],c[2]);
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
    /**
     * *******************************************************************
     * backdrop_refresh
     *
     * This remaps the "original" palette indexes to the abstract OS indexes
     * used by MAME. This needs to be called every time palette_recalc returns a
     * non-zero value, since the mappings will have changed.
     * *******************************************************************
     */
    static void backdrop_refresh(struct_artwork a) {
        int i, j;
        int height, width;
        osd_bitmap back = null;
        osd_bitmap orig = null;
        int offset;

        offset = a.start_pen;
        back = a.artwork;
        orig = a.orig_artwork;
        height = a.artwork.height;
        width = a.artwork.width;

        if (back.depth == 8) {
            for (j = 0; j < height; j++) {
                for (i = 0; i < width; i++) {
                    back.line[j].write(i, Machine.pens[orig.line[j].read(i) + offset]);
                }
            }
        } else {
            throw new UnsupportedOperationException("Unsupported");
            /*TODO*///		for ( j=0; j<height; j++)
/*TODO*///			for (i=0; i<width; i++)
/*TODO*///				((unsigned short *)back->line[j])[i] = Machine->pens[((unsigned short *)orig->line[j])[i]+offset];
        }
    }

    /**
     * *******************************************************************
     * backdrop_set_palette
     *
     * This sets the palette colors used by the backdrop to the new colors
     * passed in as palette. The values should be stored as one byte of red, one
     * byte of blue, one byte of green. This could hopefully be used for special
     * effects, like lightening and darkening the backdrop.
     * *******************************************************************
     */
    public static void backdrop_set_palette(struct_artwork a, /*unsigned*/ char[] palette) {
        int i;

        /* Load colors into the palette */
        if ((Machine.drv.video_attributes & VIDEO_MODIFIES_PALETTE) != 0) {
            for (i = 0; i < a.num_pens_used; i++) {
                palette_change_color(i + a.start_pen, palette[i * 3], palette[i * 3 + 1], palette[i * 3 + 2]);
            }

            palette_recalc();
            backdrop_refresh(a);
        }
    }

    /**
     * *******************************************************************
     * artwork_free
     *
     * Don't forget to clean up when you're done with the backdrop!!!
     * *******************************************************************
     */
    public static void artwork_free(struct_artwork a) {
        /*TODO*///	if (a)
/*TODO*///	{
/*TODO*///		if (a->artwork)
/*TODO*///			osd_free_bitmap(a->artwork);
/*TODO*///		if (a->orig_artwork)
/*TODO*///			osd_free_bitmap(a->orig_artwork);
/*TODO*///		if (a->vector_bitmap)
/*TODO*///			osd_free_bitmap(a->vector_bitmap);
/*TODO*///		if (a->orig_palette)
/*TODO*///			free (a->orig_palette);
/*TODO*///		if (a->transparency)
/*TODO*///			free (a->transparency);
/*TODO*///		if (a->brightness)
/*TODO*///			free (a->brightness);
/*TODO*///		if (a->pTable)
/*TODO*///			free (a->pTable);
/*TODO*///		free(a);
/*TODO*///	}
    }

    /*TODO*///
/*TODO*////*********************************************************************
/*TODO*///  backdrop_black_recalc
/*TODO*///
/*TODO*///  If you use any of the experimental backdrop draw* blitters below,
/*TODO*///  call this once per frame.  It will catch palette changes and mark
/*TODO*///  every black as transparent.  If it returns a non-zero value, redraw
/*TODO*///  the whole background.
/*TODO*/// *********************************************************************/
/*TODO*///
/*TODO*///int backdrop_black_recalc(void)
/*TODO*///{
/*TODO*///	unsigned char r,g,b;
/*TODO*///	int i;
/*TODO*///	int redraw = 0;
/*TODO*///
/*TODO*///	/* Determine what colors can be overwritten */
/*TODO*///	for (i=0; i<256; i++)
/*TODO*///	{
/*TODO*///		osd_get_pen(i,&r,&g,&b);
/*TODO*///
/*TODO*///		if ((r==0) && (g==0) && (b==0))
/*TODO*///		{
/*TODO*///			if (isblack[i] != 1)
/*TODO*///				redraw = 1;
/*TODO*///			isblack[i] = 1;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if (isblack[i] != 0)
/*TODO*///				redraw = 1;
/*TODO*///			isblack[i] = 0;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return redraw;
/*TODO*///}
/*TODO*///
/*TODO*////*********************************************************************
/*TODO*///  draw_backdrop
/*TODO*///
/*TODO*///  This is an experimental backdrop blitter.  How to use:
/*TODO*///  1)  Draw the dirty background video game graphic with no transparency.
/*TODO*///  2)  Call draw_backdrop with a clipping rectangle containing the location
/*TODO*///      of the dirty_graphic.
/*TODO*///
/*TODO*///  draw_backdrop will fill in everything that's colored black with the
/*TODO*///  backdrop.
/*TODO*/// *********************************************************************/
/*TODO*///
/*TODO*///void draw_backdrop(struct osd_bitmap *dest,const struct osd_bitmap *src,int sx,int sy,
/*TODO*///					const struct rectangle *clip)
/*TODO*///{
/*TODO*///	int ox,oy,ex,ey,y,start,dy;
/*TODO*///	/*  int col;
/*TODO*///		int *sd4;
/*TODO*///		int trans4,col4;*/
/*TODO*///	struct rectangle myclip;
/*TODO*///
/*TODO*///	if (!src) return;
/*TODO*///	if (!dest) return;
/*TODO*///
/*TODO*///    /* Rotate and swap as necessary... */
/*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///	{
/*TODO*///		int temp;
/*TODO*///
/*TODO*///		temp = sx;
/*TODO*///		sx = sy;
/*TODO*///		sy = temp;
/*TODO*///
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			/* clip and myclip might be the same, so we need a temporary storage */
/*TODO*///			temp = clip->min_x;
/*TODO*///			myclip.min_x = clip->min_y;
/*TODO*///			myclip.min_y = temp;
/*TODO*///			temp = clip->max_x;
/*TODO*///			myclip.max_x = clip->max_y;
/*TODO*///			myclip.max_y = temp;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	if (Machine->orientation & ORIENTATION_FLIP_X)
/*TODO*///	{
/*TODO*///		sx = dest->width - src->width - sx;
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			int temp;
/*TODO*///
/*TODO*///
/*TODO*///			/* clip and myclip might be the same, so we need a temporary storage */
/*TODO*///			temp = clip->min_x;
/*TODO*///			myclip.min_x = dest->width-1 - clip->max_x;
/*TODO*///			myclip.max_x = dest->width-1 - temp;
/*TODO*///			myclip.min_y = clip->min_y;
/*TODO*///			myclip.max_y = clip->max_y;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	if (Machine->orientation & ORIENTATION_FLIP_Y)
/*TODO*///	{
/*TODO*///		sy = dest->height - src->height - sy;
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			int temp;
/*TODO*///
/*TODO*///
/*TODO*///			myclip.min_x = clip->min_x;
/*TODO*///			myclip.max_x = clip->max_x;
/*TODO*///			/* clip and myclip might be the same, so we need a temporary storage */
/*TODO*///			temp = clip->min_y;
/*TODO*///			myclip.min_y = dest->height-1 - clip->max_y;
/*TODO*///			myclip.max_y = dest->height-1 - temp;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	/* check bounds */
/*TODO*///	ox = sx;
/*TODO*///	oy = sy;
/*TODO*///	ex = sx + src->width-1;
/*TODO*///	if (sx < 0) sx = 0;
/*TODO*///	if (clip && sx < clip->min_x) sx = clip->min_x;
/*TODO*///	if (ex >= dest->width) ex = dest->width-1;
/*TODO*///	if (clip && ex > clip->max_x) ex = clip->max_x;
/*TODO*///	if (sx > ex) return;
/*TODO*///	ey = sy + src->height-1;
/*TODO*///	if (sy < 0) sy = 0;
/*TODO*///	if (clip && sy < clip->min_y) sy = clip->min_y;
/*TODO*///	if (ey >= dest->height) ey = dest->height-1;
/*TODO*///	if (clip && ey > clip->max_y) ey = clip->max_y;
/*TODO*///	if (sy > ey) return;
/*TODO*///
/*TODO*///    /* VERY IMPORTANT to mark this rectangle as dirty! :) - MAB */
/*TODO*///	osd_mark_dirty (sx,sy,ex,ey,0);
/*TODO*///
/*TODO*///	start = sy-oy;
/*TODO*///	dy = 1;
/*TODO*///
/*TODO*///	if (dest->depth == 8)
/*TODO*///	{
/*TODO*///		for (y = sy;y <= ey;y++)
/*TODO*///		{
/*TODO*///			const unsigned char *sd;
/*TODO*///			unsigned char *bm,*bme;
/*TODO*///
/*TODO*///			bm = dest->line[y];
/*TODO*///			bme = bm + ex;
/*TODO*///			sd = src->line[start] + (sx-ox);
/*TODO*///			for( bm = bm+sx ; bm <= bme ; bm++ )
/*TODO*///			{
/*TODO*///				if (isblack[*bm])
/*TODO*///					*bm = *sd;
/*TODO*///				sd++;
/*TODO*///			}
/*TODO*///			start+=dy;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		for (y = sy;y <= ey;y++)
/*TODO*///		{
/*TODO*///			const unsigned short *sd;
/*TODO*///			unsigned short *bm,*bme;
/*TODO*///
/*TODO*///			bm = (unsigned short *)dest->line[y];
/*TODO*///			bme = bm + ex;
/*TODO*///			sd = ((unsigned short *)src->line[start]) + (sx-ox);
/*TODO*///			for( bm = bm+sx ; bm <= bme ; bm++ )
/*TODO*///			{
/*TODO*///				if (isblack[*bm])
/*TODO*///					*bm = *sd;
/*TODO*///				sd++;
/*TODO*///			}
/*TODO*///			start+=dy;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////*********************************************************************
/*TODO*///  drawgfx_backdrop
/*TODO*///
/*TODO*///  This is an experimental backdrop blitter.  How to use:
/*TODO*///
/*TODO*///  Every time you want to draw a background tile, instead of calling
/*TODO*///  drawgfx, call this and pass in the backdrop bitmap.  Wherever the
/*TODO*///  tile is black, the backdrop will be drawn.
/*TODO*/// *********************************************************************/
/*TODO*///void drawgfx_backdrop(struct osd_bitmap *dest,const struct GfxElement *gfx,
/*TODO*///		unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
/*TODO*///		const struct rectangle *clip,const struct osd_bitmap *back)
/*TODO*///{
/*TODO*///	int ox,oy,ex,ey,y,start,dy;
/*TODO*///	const unsigned char *sd;
/*TODO*///	/*int col;
/*TODO*///	  int *sd4;
/*TODO*///	  int trans4,col4;*/
/*TODO*///	struct rectangle myclip;
/*TODO*///
/*TODO*///	if (!gfx) return;
/*TODO*///
/*TODO*///	code %= gfx->total_elements;
/*TODO*///	color %= gfx->total_colors;
/*TODO*///
/*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///	{
/*TODO*///		int temp;
/*TODO*///
/*TODO*///		temp = sx;
/*TODO*///		sx = sy;
/*TODO*///		sy = temp;
/*TODO*///
/*TODO*///		temp = flipx;
/*TODO*///		flipx = flipy;
/*TODO*///		flipy = temp;
/*TODO*///
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			/* clip and myclip might be the same, so we need a temporary storage */
/*TODO*///			temp = clip->min_x;
/*TODO*///			myclip.min_x = clip->min_y;
/*TODO*///			myclip.min_y = temp;
/*TODO*///			temp = clip->max_x;
/*TODO*///			myclip.max_x = clip->max_y;
/*TODO*///			myclip.max_y = temp;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	if (Machine->orientation & ORIENTATION_FLIP_X)
/*TODO*///	{
/*TODO*///		sx = dest->width - gfx->width - sx;
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			int temp;
/*TODO*///
/*TODO*///
/*TODO*///			/* clip and myclip might be the same, so we need a temporary storage */
/*TODO*///			temp = clip->min_x;
/*TODO*///			myclip.min_x = dest->width-1 - clip->max_x;
/*TODO*///			myclip.max_x = dest->width-1 - temp;
/*TODO*///			myclip.min_y = clip->min_y;
/*TODO*///			myclip.max_y = clip->max_y;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	if (Machine->orientation & ORIENTATION_FLIP_Y)
/*TODO*///	{
/*TODO*///		sy = dest->height - gfx->height - sy;
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			int temp;
/*TODO*///
/*TODO*///
/*TODO*///			myclip.min_x = clip->min_x;
/*TODO*///			myclip.max_x = clip->max_x;
/*TODO*///			/* clip and myclip might be the same, so we need a temporary storage */
/*TODO*///			temp = clip->min_y;
/*TODO*///			myclip.min_y = dest->height-1 - clip->max_y;
/*TODO*///			myclip.max_y = dest->height-1 - temp;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	/* check bounds */
/*TODO*///	ox = sx;
/*TODO*///	oy = sy;
/*TODO*///	ex = sx + gfx->width-1;
/*TODO*///	if (sx < 0) sx = 0;
/*TODO*///	if (clip && sx < clip->min_x) sx = clip->min_x;
/*TODO*///	if (ex >= dest->width) ex = dest->width-1;
/*TODO*///	if (clip && ex > clip->max_x) ex = clip->max_x;
/*TODO*///	if (sx > ex) return;
/*TODO*///	ey = sy + gfx->height-1;
/*TODO*///	if (sy < 0) sy = 0;
/*TODO*///	if (clip && sy < clip->min_y) sy = clip->min_y;
/*TODO*///	if (ey >= dest->height) ey = dest->height-1;
/*TODO*///	if (clip && ey > clip->max_y) ey = clip->max_y;
/*TODO*///	if (sy > ey) return;
/*TODO*///
/*TODO*///	osd_mark_dirty (sx,sy,ex,ey,0);	/* ASG 971011 */
/*TODO*///
/*TODO*///	/* start = code * gfx->height; */
/*TODO*///	if (flipy)	/* Y flop */
/*TODO*///	{
/*TODO*///		start = code * gfx->height + gfx->height-1 - (sy-oy);
/*TODO*///		dy = -1;
/*TODO*///	}
/*TODO*///	else		/* normal */
/*TODO*///	{
/*TODO*///		start = code * gfx->height + (sy-oy);
/*TODO*///		dy = 1;
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	if (dest->depth == 8)
/*TODO*///	{
/*TODO*///		unsigned char *bm,*bme;
/*TODO*///		const unsigned char *sb;
/*TODO*///
/*TODO*///		if (gfx->colortable)	/* remap colors */
/*TODO*///		{
/*TODO*///			const unsigned short *paldata;	/* ASG 980209 */
/*TODO*///
/*TODO*///			paldata = &gfx->colortable[gfx->color_granularity * color];
/*TODO*///
/*TODO*///			if (flipx)	/* X flip */
/*TODO*///			{
/*TODO*///				for (y = sy;y <= ey;y++)
/*TODO*///				{
/*TODO*///					bm  = dest->line[y];
/*TODO*///					bme = bm + ex;
/*TODO*///					sd = gfx->gfxdata + start * gfx->line_modulo + gfx->width-1 - (sx-ox);
/*TODO*///    	            sb = back->line[y] + sx;
/*TODO*///					for( bm += sx ; bm <= bme ; bm++ )
/*TODO*///					{
/*TODO*///						if (isblack[paldata[*sd]])
/*TODO*///    	                    *bm = *sb;
/*TODO*///    	                else
/*TODO*///							*bm = paldata[*sd];
/*TODO*///						sd--;
/*TODO*///    	                sb++;
/*TODO*///					}
/*TODO*///					start+=dy;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			else		/* normal */
/*TODO*///			{
/*TODO*///				for (y = sy;y <= ey;y++)
/*TODO*///				{
/*TODO*///					bm  = dest->line[y];
/*TODO*///					bme = bm + ex;
/*TODO*///					sd = gfx->gfxdata + start * gfx->line_modulo + (sx-ox);
/*TODO*///    	            sb = back->line[y] + sx;
/*TODO*///					for( bm += sx ; bm <= bme ; bm++ )
/*TODO*///					{
/*TODO*///						if (isblack[paldata[*sd]])
/*TODO*///    	                    *bm = *sb;
/*TODO*///    	                else
/*TODO*///							*bm = paldata[*sd];
/*TODO*///						sd++;
/*TODO*///    	                sb++;
/*TODO*///					}
/*TODO*///					start+=dy;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if (flipx)	/* X flip */
/*TODO*///			{
/*TODO*///				for (y = sy;y <= ey;y++)
/*TODO*///				{
/*TODO*///					bm = dest->line[y];
/*TODO*///					bme = bm + ex;
/*TODO*///					sd = gfx->gfxdata + start * gfx->line_modulo + gfx->width-1 - (sx-ox);
/*TODO*///    	            sb = back->line[y] + sx;
/*TODO*///					for( bm = bm+sx ; bm <= bme ; bm++ )
/*TODO*///					{
/*TODO*///						if (isblack[*sd])
/*TODO*///    	                    *bm = *sb;
/*TODO*///    	                else
/*TODO*///							*bm = *sd;
/*TODO*///						sd--;
/*TODO*///    	                sb++;
/*TODO*///					}
/*TODO*///					start+=dy;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			else		/* normal */
/*TODO*///			{
/*TODO*///				for (y = sy;y <= ey;y++)
/*TODO*///				{
/*TODO*///					bm = dest->line[y];
/*TODO*///					bme = bm + ex;
/*TODO*///					sd = gfx->gfxdata + start * gfx->line_modulo + (sx-ox);
/*TODO*///    	            sb = back->line[y] + sx;
/*TODO*///					for( bm = bm+sx ; bm <= bme ; bm++ )
/*TODO*///					{
/*TODO*///						if (isblack[*sb])
/*TODO*///    	                    *bm = *sb;
/*TODO*///    	                else
/*TODO*///							*bm = *sd;
/*TODO*///						sd++;
/*TODO*///    	                sb++;
/*TODO*///					}
/*TODO*///					start+=dy;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		unsigned short *bm,*bme;
/*TODO*///		const unsigned short *sb;
/*TODO*///
/*TODO*///		if (gfx->colortable)	/* remap colors */
/*TODO*///		{
/*TODO*///			const unsigned short *paldata;	/* ASG 980209 */
/*TODO*///
/*TODO*///			paldata = &gfx->colortable[gfx->color_granularity * color];
/*TODO*///
/*TODO*///			if (flipx)	/* X flip */
/*TODO*///			{
/*TODO*///				for (y = sy;y <= ey;y++)
/*TODO*///				{
/*TODO*///					bm  = (unsigned short *)dest->line[y];
/*TODO*///					bme = bm + ex;
/*TODO*///					sd = gfx->gfxdata + start * gfx->line_modulo + gfx->width-1 - (sx-ox);
/*TODO*///    	            sb = ((unsigned short *)back->line[y]) + sx;
/*TODO*///					for( bm += sx ; bm <= bme ; bm++ )
/*TODO*///					{
/*TODO*///						if (isblack[paldata[*sd]])
/*TODO*///    	                    *bm = *sb;
/*TODO*///    	                else
/*TODO*///							*bm = paldata[*sd];
/*TODO*///						sd--;
/*TODO*///    	                sb++;
/*TODO*///					}
/*TODO*///					start+=dy;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			else		/* normal */
/*TODO*///			{
/*TODO*///				for (y = sy;y <= ey;y++)
/*TODO*///				{
/*TODO*///					bm  = (unsigned short *)dest->line[y];
/*TODO*///					bme = bm + ex;
/*TODO*///					sd = gfx->gfxdata + start * gfx->line_modulo + (sx-ox);
/*TODO*///    	            sb = ((unsigned short *)back->line[y]) + sx;
/*TODO*///					for( bm += sx ; bm <= bme ; bm++ )
/*TODO*///					{
/*TODO*///						if (isblack[paldata[*sd]])
/*TODO*///    	                    *bm = *sb;
/*TODO*///    	                else
/*TODO*///							*bm = paldata[*sd];
/*TODO*///						sd++;
/*TODO*///    	                sb++;
/*TODO*///					}
/*TODO*///					start+=dy;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if (flipx)	/* X flip */
/*TODO*///			{
/*TODO*///				for (y = sy;y <= ey;y++)
/*TODO*///				{
/*TODO*///					bm = (unsigned short *)dest->line[y];
/*TODO*///					bme = bm + ex;
/*TODO*///					sd = gfx->gfxdata + start * gfx->line_modulo + gfx->width-1 - (sx-ox);
/*TODO*///    	            sb = ((unsigned short *)back->line[y]) + sx;
/*TODO*///					for( bm = bm+sx ; bm <= bme ; bm++ )
/*TODO*///					{
/*TODO*///						if (isblack[*sd])
/*TODO*///    	                    *bm = *sb;
/*TODO*///    	                else
/*TODO*///							*bm = *sd;
/*TODO*///						sd--;
/*TODO*///    	                sb++;
/*TODO*///					}
/*TODO*///					start+=dy;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			else		/* normal */
/*TODO*///			{
/*TODO*///				for (y = sy;y <= ey;y++)
/*TODO*///				{
/*TODO*///					bm = (unsigned short *)dest->line[y];
/*TODO*///					bme = bm + ex;
/*TODO*///					sd = gfx->gfxdata + start * gfx->line_modulo + (sx-ox);
/*TODO*///    	            sb = ((unsigned short *)back->line[y]) + sx;
/*TODO*///					for( bm = bm+sx ; bm <= bme ; bm++ )
/*TODO*///					{
/*TODO*///						if (isblack[*sb])
/*TODO*///    	                    *bm = *sb;
/*TODO*///    	                else
/*TODO*///							*bm = *sd;
/*TODO*///						sd++;
/*TODO*///    	                sb++;
/*TODO*///					}
/*TODO*///					start+=dy;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
    /**
     * *******************************************************************
     * overlay_draw
     *
     * This is an experimental backdrop blitter. How to use: 1) Refresh all of
     * your bitmap. - This is usually done with
     * copybitmap(bitmap,tmpbitmap,...); 2) Call overlay_draw with the bitmap
     * and the overlay.
     *
     * Not so tough, is it? :)
     *
     * Note: we don't have to worry about marking dirty rectangles here, because
     * we should only have color changes in redrawn sections, which should
     * already be marked as dirty by the original blitter.
     *
     * TODO: support translucency and multiple intensities if we need to.
     *
     ********************************************************************
     */
    public static void overlay_draw(osd_bitmap dest, struct_artwork overlay) {
        int i, j;
        int height, width;
        osd_bitmap o = null;
        int black;

        o = overlay.artwork;
        height = overlay.artwork.height;
        width = overlay.artwork.width;
        black = Machine.pens[0];

        if (dest.depth == 8) {
            UBytePtr dst, ovr;

            for (j = 0; j < height; j++) {
                dst = new UBytePtr(dest.line[j]);
                ovr = new UBytePtr(o.line[j]);
                for (i = 0; i < width; i++) {
                    if (dst.read() != black) {
                        dst.write(ovr.read());
                    }
                    dst.inc();
                    ovr.inc();
                }
            }
        } else {
            throw new UnsupportedOperationException("Unsupported");
            /*TODO*///		unsigned short *dst, *ovr;
/*TODO*///
/*TODO*///		for ( j=0; j<height; j++)
/*TODO*///		{
/*TODO*///			dst = (unsigned short *)dest->line[j];
/*TODO*///			ovr = (unsigned short *)o->line[j];
/*TODO*///			for (i=0; i<width; i++)
/*TODO*///			{
/*TODO*///				if (*dst!=black)
/*TODO*///					*dst = *ovr;
/*TODO*///				dst++;
/*TODO*///				ovr++;
/*TODO*///			}
/*TODO*///		}
        }
    }

    /*TODO*///
/*TODO*///
/*TODO*////*********************************************************************
/*TODO*///  RGBtoHSV and HSVtoRGB
/*TODO*///
/*TODO*///  This is overkill for now but maybe they come in handy later
/*TODO*///  (Stolen from Foley's book)
/*TODO*/// *********************************************************************/
/*TODO*///static void RGBtoHSV( float r, float g, float b, float *h, float *s, float *v )
/*TODO*///{
/*TODO*///	float min, max, delta;
/*TODO*///
/*TODO*///	min = MIN( r, MIN( g, b ));
/*TODO*///	max = MAX( r, MAX( g, b ));
/*TODO*///	*v = max;
/*TODO*///
/*TODO*///	delta = max - min;
/*TODO*///
/*TODO*///	if( delta > 0  )
/*TODO*///		*s = delta / max;
/*TODO*///	else {
/*TODO*///		*s = 0;
/*TODO*///		*h = 0;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	if( r == max )
/*TODO*///		*h = ( g - b ) / delta;
/*TODO*///	else if( g == max )
/*TODO*///		*h = 2 + ( b - r ) / delta;
/*TODO*///	else
/*TODO*///		*h = 4 + ( r - g ) / delta;
/*TODO*///
/*TODO*///	*h *= 60;
/*TODO*///	if( *h < 0 )
/*TODO*///		*h += 360;
/*TODO*///}
/*TODO*///
/*TODO*///static void HSVtoRGB( float *r, float *g, float *b, float h, float s, float v )
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	float f, p, q, t;
/*TODO*///
/*TODO*///	if( s == 0 ) {
/*TODO*///		*r = *g = *b = v;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	h /= 60;
/*TODO*///	i = h;
/*TODO*///	f = h - i;
/*TODO*///	p = v * ( 1 - s );
/*TODO*///	q = v * ( 1 - s * f );
/*TODO*///	t = v * ( 1 - s * ( 1 - f ) );
/*TODO*///
/*TODO*///	switch( i ) {
/*TODO*///	case 0: *r = v; *g = t; *b = p; break;
/*TODO*///	case 1: *r = q; *g = v; *b = p; break;
/*TODO*///	case 2: *r = p; *g = v; *b = t; break;
/*TODO*///	case 3: *r = p; *g = q; *b = v; break;
/*TODO*///	case 4: *r = t; *g = p; *b = v; break;
/*TODO*///	default: *r = v; *g = p; *b = q; break;
/*TODO*///	}
/*TODO*///
/*TODO*///}
/*TODO*///
/*TODO*////*********************************************************************
/*TODO*///  transparency_hist
/*TODO*///
/*TODO*///  Calculates a histogram of all transparent pixels in the overlay.
/*TODO*///  The function returns a array of ints with the number of shades
/*TODO*///  for each transparent color based on the color histogram.
/*TODO*/// *********************************************************************/
/*TODO*///static unsigned int *transparency_hist (struct artwork *a, int num_shades)
/*TODO*///{
/*TODO*///	int i, j;
/*TODO*///	unsigned int *hist;
/*TODO*///	int num_pix=0, min_shades;
/*TODO*///	unsigned char pen;
/*TODO*///
/*TODO*///	if ((hist = (unsigned int *)malloc(a->num_pens_trans*sizeof(unsigned int)))==NULL)
/*TODO*///	{
/*TODO*///		if (errorlog)
/*TODO*///			fprintf(errorlog,"Not enough memory!\n");
/*TODO*///		return NULL;
/*TODO*///	}
/*TODO*///	memset (hist, 0, a->num_pens_trans*sizeof(int));
/*TODO*///
/*TODO*///	if (a->orig_artwork->depth == 8)
/*TODO*///	{
/*TODO*///		for ( j=0; j<a->orig_artwork->height; j++)
/*TODO*///			for (i=0; i<a->orig_artwork->width; i++)
/*TODO*///			{
/*TODO*///				pen = a->orig_artwork->line[j][i];
/*TODO*///				if (pen < a->num_pens_trans)
/*TODO*///				{
/*TODO*///					hist[pen]++;
/*TODO*///					num_pix++;
/*TODO*///				}
/*TODO*///			}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		for ( j=0; j<a->orig_artwork->height; j++)
/*TODO*///			for (i=0; i<a->orig_artwork->width; i++)
/*TODO*///			{
/*TODO*///				pen = ((unsigned short *)a->orig_artwork->line[j])[i];
/*TODO*///				if (pen < a->num_pens_trans)
/*TODO*///				{
/*TODO*///					hist[pen]++;
/*TODO*///					num_pix++;
/*TODO*///				}
/*TODO*///			}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* we try to get at least 3 shades per transparent color */
/*TODO*///	min_shades = ((num_shades-a->num_pens_used-3*a->num_pens_trans) < 0) ? 0 : 3;
/*TODO*///
/*TODO*///	if (errorlog && (min_shades==0))
/*TODO*///		fprintf(errorlog,"Too many colors in overlay. Vector colors may be wrong.\n");
/*TODO*///
/*TODO*///	num_pix /= num_shades-(a->num_pens_used-a->num_pens_trans)
/*TODO*///		-min_shades*a->num_pens_trans;
/*TODO*///
/*TODO*///	for (i=0; i<a->num_pens_trans; i++)
/*TODO*///		hist[i] = hist[i]/num_pix + min_shades;
/*TODO*///
/*TODO*///	return hist;
/*TODO*///}
/*TODO*///
/*TODO*////*********************************************************************
/*TODO*///  overlay_set_palette
/*TODO*///
/*TODO*///  Generates a palette for vector games with an overlay.
/*TODO*///
/*TODO*///  The 'glowing' effect is simulated by alpha blending the transparent
/*TODO*///  colors with a black (the screen) background. Then different shades
/*TODO*///  of each transparent color are calculated by alpha blending this
/*TODO*///  color with different levels of brightness (values in HSV) of the
/*TODO*///  transparent color from v=0 to v=1. This doesn't work very well with
/*TODO*///  blue. The number of shades is proportional to the number of pixels of
/*TODO*///  that color. A look up table is also generated to map beam
/*TODO*///  brightness and overlay colors to pens. If you have a beam brightness
/*TODO*///  of 128 under a transparent pixel of pen 7 then
/*TODO*///     Table (7,128)
/*TODO*///  returns the pen of the resulting color. The table is usually
/*TODO*///  converted to OS colors later.
/*TODO*/// *********************************************************************/
/*TODO*///int overlay_set_palette (struct artwork *a, unsigned char *palette, int num_shades)
/*TODO*///{
/*TODO*///	unsigned int i,j, shades=0, step;
/*TODO*///	unsigned int *hist;
/*TODO*///	float h, s, v, r, g, b;
/*TODO*///
/*TODO*///	/* adjust palette start */
/*TODO*///
/*TODO*///	palette += 3*a->start_pen;
/*TODO*///
/*TODO*///	if((hist = transparency_hist (a, num_shades))==NULL)
/*TODO*///		return 0;
/*TODO*///
/*TODO*///	/* Copy all artwork colors to the palette */
/*TODO*///	memcpy (palette, a->orig_palette, 3*a->num_pens_used);
/*TODO*///
/*TODO*///	/* Fill the palette with shades of the transparent colors */
/*TODO*///	for (i=0; i<a->num_pens_trans; i++)
/*TODO*///	{
/*TODO*///		RGBtoHSV( a->orig_palette[i*3]/255.0,
/*TODO*///			  a->orig_palette[i*3+1]/255.0,
/*TODO*///			  a->orig_palette[i*3+2]/255.0, &h, &s, &v );
/*TODO*///
/*TODO*///		/* blend transparent entries with black background */
/*TODO*///		/* we don't need the original palette entry any more */
/*TODO*///		HSVtoRGB ( &r, &g, &b, h, s, v*a->transparency[i]/255.0);
/*TODO*///		palette [i*3+0] = r * 255.0;
/*TODO*///		palette [i*3+1] = g * 255.0;
/*TODO*///		palette [i*3+2] = b * 255.0;
/*TODO*///
/*TODO*///		if (hist[i]>1)
/*TODO*///		{
/*TODO*///			for (j=0; j<hist[i]-1; j++)
/*TODO*///			{
/*TODO*///				/* we start from 1 because the 0 level is already in the palette */
/*TODO*///				HSVtoRGB ( &r, &g, &b, h, s, v*a->transparency[i]/255.0 +
/*TODO*///					   ((1-(v*a->transparency[i]/255.0))*(j+1))/(hist[i]-1));
/*TODO*///				palette [(a->num_pens_used + shades + j)*3+0] = r * 255.0;
/*TODO*///				palette [(a->num_pens_used + shades + j)*3+1] = g * 255.0;
/*TODO*///				palette [(a->num_pens_used + shades + j)*3+2] = b * 255.0;
/*TODO*///			}
/*TODO*///
/*TODO*///			/* create alpha LUT for quick alpha blending */
/*TODO*///			for (j=0; j<256; j++)
/*TODO*///			{
/*TODO*///				step = hist[i]*j/256.0;
/*TODO*///				if (step == 0)
/*TODO*///				/* no beam, just overlay over black screen */
/*TODO*///					a->pTable[i*256+j] = i + a->start_pen;
/*TODO*///				else
/*TODO*///					a->pTable[i*256+j] = a->num_pens_used +
/*TODO*///						shades + step-1 + a->start_pen;
/*TODO*///			}
/*TODO*///
/*TODO*///			shades += hist[i]-1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	return 1;
/*TODO*///}
    /**
     * *******************************************************************
     * overlay_remap
     *
     * This remaps the "original" palette indexes to the abstract OS indexes
     * used by MAME. This has to be called during startup after the OS colors
     * have been initialized (vh_start).
     * *******************************************************************
     */
    public static void overlay_remap(struct_artwork a) {
        int i, j;
        char[] r = new char[1];
        char[] g = new char[1];
        char[] b = new char[1];

        int offset = a.start_pen;
        int height = a.artwork.height;
        int width = a.artwork.width;
        osd_bitmap overlay = a.artwork;
        osd_bitmap orig = a.orig_artwork;

        if (overlay.depth == 8) {
            for (j = 0; j < height; j++) {
                for (i = 0; i < width; i++) {
                    overlay.line[j].write(i, Machine.pens[orig.line[j].read(i) + offset]);
                }
            }
        } else {
            /*TODO*///		for ( j=0; j<height; j++)
/*TODO*///			for (i=0; i<width; i++)
/*TODO*///				((unsigned short *)overlay->line[j])[i] = Machine->pens[((unsigned short *)orig->line[j])[i]+offset];
        }

        /* Calculate brightness of all colors */
        for (i = 0; i < Machine.drv.total_colors; i++) {
            osd_get_pen(Machine.pens[i], r, g, b);
            a.brightness[Machine.pens[i]] = (char) ((222 * r[0] + 707 * g[0] + 71 * b[0]) / 1000);
        }

        /* Erase vector bitmap same way as in vector.c */
        if (a.vector_bitmap != null) {
            fillbitmap(a.vector_bitmap, Machine.pens[0], null);
        }
    }

    /**
     * *******************************************************************
     * allocate_artwork_mem
     *
     * Allocates memory for all the bitmaps.
     * *******************************************************************
     */
    static struct_artwork allocate_artwork_mem(int width, int height) {
        struct_artwork a;
        int temp;

        if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
            temp = height;
            height = width;
            width = temp;
        }

        a = new struct_artwork();

        a.transparency = null;
        a.orig_palette = null;
        a.pTable = null;
        a.brightness = null;
        a.vector_bitmap = null;

        if ((a.orig_artwork = osd_create_bitmap(width, height)) == null) {
            if (errorlog != null) {
                fprintf(errorlog, "Not enough memory for artwork!\n");
            }
            artwork_free(a);
            return null;
        }
        fillbitmap(a.orig_artwork, 0, null);

        /* Create a second bitmap for public use */
        if ((a.artwork = osd_create_bitmap(width, height)) == null) {
            if (errorlog != null) {
                fprintf(errorlog, "Not enough memory for artwork!\n");
            }
            artwork_free(a);
            return null;
        }

        a.pTable = new char[256 * 256];
        a.brightness = new char[256 * 256];

        memset(a.brightness, 0, 256 * 256);

        /* Create bitmap for the vector screen */
        if ((Machine.drv.video_attributes & VIDEO_TYPE_VECTOR) != 0) {
            if ((a.vector_bitmap = osd_create_bitmap(width, height)) == null) {
                if (errorlog != null) {
                    fprintf(errorlog, "Not enough memory for artwork!\n");
                }
                artwork_free(a);
                return null;
            }
            fillbitmap(a.vector_bitmap, 0, null);
        }

        return a;
    }

    /*TODO*////*********************************************************************
/*TODO*///
/*TODO*///  Reads a PNG for a artwork struct and checks if it has the right
/*TODO*///  format.
/*TODO*///
/*TODO*/// *********************************************************************/
/*TODO*///static int artwork_read_bitmap(const char *file_name, struct osd_bitmap **bitmap, struct png_info *p)
/*TODO*///{
/*TODO*///	UINT8 *tmp;
/*TODO*///	UINT32 orientation;
/*TODO*///	UINT32 x, y;
/*TODO*///	void *fp;
/*TODO*///
/*TODO*///	if (!(fp = osd_fopen(Machine->gamedrv->name, file_name, OSD_FILETYPE_ARTWORK, 0)))
/*TODO*///	{
/*TODO*///		if (errorlog)
/*TODO*///			fprintf(errorlog,"Unable to open PNG %s\n", file_name);
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (!png_read_file(fp, p))
/*TODO*///	{
/*TODO*///		osd_fclose (fp);
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	osd_fclose (fp);
/*TODO*///
/*TODO*///	if (p->bit_depth > 8)
/*TODO*///	{
/*TODO*///		if (errorlog)
/*TODO*///			fprintf(errorlog,"Unsupported bit depth %i (8 bit max.)\n", p->bit_depth);
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (p->color_type != 3)
/*TODO*///	{
/*TODO*///		if (errorlog)
/*TODO*///			fprintf(errorlog,"Unsupported color type %i (has to be 3)\n", p->color_type);
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	if (p->interlace_method != 0)
/*TODO*///	{
/*TODO*///		if (errorlog)
/*TODO*///			fprintf(errorlog,"Interlace unsupported\n");
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* Convert to 8 bit */
/*TODO*///	png_expand_buffer_8bit (p);
/*TODO*///
/*TODO*///	png_delete_unused_colors (p);
/*TODO*///
/*TODO*///	if ((*bitmap=osd_create_bitmap(p->width,p->height)) == 0)
/*TODO*///	{
/*TODO*///		if (errorlog)
/*TODO*///			fprintf(errorlog,"Unable to allocate memory for artwork\n");
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	orientation = Machine->orientation;
/*TODO*///
/*TODO*///	tmp = p->image;
/*TODO*///	for (y=0; y<p->height; y++)
/*TODO*///		for (x=0; x<p->width; x++)
/*TODO*///		{
/*TODO*///			plot_pixel(*bitmap, x, y, *tmp++);
/*TODO*///		}
/*TODO*///
/*TODO*///	free (p->image);
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*////*********************************************************************
/*TODO*///  artwork_load(_size)
/*TODO*///
/*TODO*///  This is what loads your backdrop in from disk.
/*TODO*///  start_pen = the first pen available for the backdrop to use
/*TODO*///  max_pens = the number of pens the backdrop can use
/*TODO*///  So, for example, suppose you want to load "dotron.png", have it
/*TODO*///  start with color 192, and only use 48 pens.  You would call
/*TODO*///  backdrop = backdrop_load("dotron.png",192,48);
/*TODO*/// *********************************************************************/
/*TODO*///
/*TODO*///struct artwork *artwork_load_size(const char *filename, int start_pen, int max_pens, int width, int height)
/*TODO*///{
/*TODO*///	struct osd_bitmap *picture = NULL;
/*TODO*///	struct artwork *a = NULL;
/*TODO*///	struct png_info p;
/*TODO*///
/*TODO*///	/* If the user turned artwork off, bail */
/*TODO*///	if (!options.use_artwork) return NULL;
/*TODO*///
/*TODO*///	if ((a = allocate_artwork_mem(width, height))==NULL)
/*TODO*///		return NULL;
/*TODO*///
/*TODO*///	a->start_pen = start_pen;
/*TODO*///
/*TODO*///	if (!artwork_read_bitmap(filename, &picture, &p))
/*TODO*///	{
/*TODO*///		artwork_free(a);
/*TODO*///		return NULL;
/*TODO*///	}
/*TODO*///
/*TODO*///	a->num_pens_used = p.num_palette;
/*TODO*///	a->num_pens_trans = p.num_trans;
/*TODO*///	a->orig_palette = p.palette;
/*TODO*///	a->transparency = p.trans;
/*TODO*///
/*TODO*///	/* Make sure we don't have too many colors */
/*TODO*///	if (a->num_pens_used > max_pens)
/*TODO*///	{
/*TODO*///		if (errorlog) fprintf(errorlog,"Too many colors in overlay.\n");
/*TODO*///		if (errorlog) fprintf(errorlog,"Colors found: %d  Max Allowed: %d\n",
/*TODO*///				      a->num_pens_used,max_pens);
/*TODO*///		artwork_free(a);
/*TODO*///		osd_free_bitmap(picture);
/*TODO*///		return NULL;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* Scale the original picture to be the same size as the visible area */
/*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///		copybitmapzoom(a->orig_artwork,picture,0,0,0,0,
/*TODO*///			       0, TRANSPARENCY_NONE, 0,
/*TODO*///			       (a->orig_artwork->height<<16)/picture->height,
/*TODO*///			       (a->orig_artwork->width<<16)/picture->width);
/*TODO*///	else
/*TODO*///		copybitmapzoom(a->orig_artwork,picture,0,0,0,0,
/*TODO*///			       0, TRANSPARENCY_NONE, 0,
/*TODO*///			       (a->orig_artwork->width<<16)/picture->width,
/*TODO*///			       (a->orig_artwork->height<<16)/picture->height);
/*TODO*///
/*TODO*///	/* If the game uses dynamic colors, we assume that it's safe
/*TODO*///	   to init the palette and remap the colors now */
/*TODO*///	if (Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE)
/*TODO*///		backdrop_set_palette(a,a->orig_palette);
/*TODO*///
/*TODO*///	/* We don't need the original any more */
/*TODO*///	osd_free_bitmap(picture);
/*TODO*///
/*TODO*///	return a;
/*TODO*///}
/*TODO*///
/*TODO*///struct artwork *artwork_load(const char *filename, int start_pen, int max_pens)
/*TODO*///{
/*TODO*///	return artwork_load_size (filename, start_pen, max_pens, Machine->scrbitmap->width, Machine->scrbitmap->height);
/*TODO*///}
    /**
     * *******************************************************************
     * create_circle
     *
     * Creates a circle with radius r in the color of pen. A new bitmap is
     * allocated for the circle. The background is set to pen 255.
     *
     ********************************************************************
     */
    static osd_bitmap create_circle(int r, int pen) {
        osd_bitmap circle;

        int x = 0, twox = 0;
        int y = r;
        int twoy = r + r;
        int p = 1 - r;
        int i;

        if ((circle = osd_create_bitmap(twoy, twoy)) == null) {
            if (errorlog != null) {
                fprintf(errorlog, "Not enough memory for artwork!\n");
            }
            return null;
        }

        /* background */
        fillbitmap(circle, 255, null);

        while (x < y) {
            x++;
            twox += 2;
            if (p < 0) {
                p += twox + 1;
            } else {
                y--;
                twoy -= 2;
                p += twox - twoy + 1;
            }

            for (i = 0; i < twox; i++) {
                plot_pixel.handler(circle, r - x + i, r - y, pen);
                plot_pixel.handler(circle, r - x + i, r + y - 1, pen);
            }

            for (i = 0; i < twoy; i++) {
                plot_pixel.handler(circle, r - y + i, r - x, pen);
                plot_pixel.handler(circle, r - y + i, r + x - 1, pen);
            }
        }
        return circle;
    }

    /*TODO*////*********************************************************************
/*TODO*///  artwork_elements scale
/*TODO*///
/*TODO*///  scales an array of artwork elements to width and height. The first
/*TODO*///  element (which has to be a box) is used as reference. This is useful
/*TODO*///  for atwork with circles.
/*TODO*///
/*TODO*///*********************************************************************/
/*TODO*///
/*TODO*///void artwork_elements_scale(struct artwork_element *ae, int width, int height)
/*TODO*///{
/*TODO*///	int scale_w, scale_h;
/*TODO*///
/*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///	{
/*TODO*///		scale_w = (height << 16)/(ae->box.max_x + 1);
/*TODO*///		scale_h = (width << 16)/(ae->box.max_y + 1);
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		scale_w = (width << 16)/(ae->box.max_x + 1);
/*TODO*///		scale_h = (height << 16)/(ae->box.max_y + 1);
/*TODO*///	}
/*TODO*///	while (ae->box.min_x >= 0)
/*TODO*///	{
/*TODO*///		ae->box.min_x = (ae->box.min_x * scale_w) >> 16;
/*TODO*///		ae->box.max_x = (ae->box.max_x * scale_w) >> 16;
/*TODO*///		ae->box.min_y = (ae->box.min_y * scale_h) >> 16;
/*TODO*///		if (ae->box.max_y >= 0)
/*TODO*///			ae->box.max_y = (ae->box.max_y * scale_h) >> 16;
/*TODO*///		ae++;
/*TODO*///	}
/*TODO*///}
    /**
     * *******************************************************************
     * artwork_create
     *
     * This works similar to artwork_load but generates artwork from an array of
     * artwork_element. This is useful for very simple artwork like the overlay
     * in the Space invaders series of games. The overlay is defined to be the
     * same size as the screen. The end of the array is marked by an entry with
     * negative coordinates. Boxes and circles are supported. Circles are marked
     * max_y == -1, min_x == x coord. of center, min_y == y coord. of center,
     * max_x == radius. If there are transparent and opaque overlay elements,
     * the opaque ones have to be at the end of the list to stay compatible with
     * the PNG artwork.
     * *******************************************************************
     */
    public static struct_artwork artwork_create(artwork_element[] ae, int start_pen, int max_pens) {
        struct_artwork a;
        osd_bitmap circle;
        int pen;

        if ((a = allocate_artwork_mem(Machine.scrbitmap.width, Machine.scrbitmap.height)) == null) {
            return null;
        }

        a.start_pen = start_pen;

        a.orig_palette = new char[256 * 3];
        a.transparency = new char[256];
        a.num_pens_used = 0;
        a.num_pens_trans = 0;
        int aei = 0;
        while (aei < ae.length && ae[aei].box.min_x >= 0) {
            /* look if the color is already in the palette */
            pen = 0;
            while ((pen < a.num_pens_used)
                    && ((ae[aei].red != a.orig_palette[3 * pen])
                    || (ae[aei].green != a.orig_palette[3 * pen + 1])
                    || (ae[aei].blue != a.orig_palette[3 * pen + 2])
                    || ((ae[aei].alpha < 255) && (ae[aei].alpha != a.transparency[pen])))) {
                pen++;
            }

            if (pen == a.num_pens_used) {
                a.orig_palette[3 * pen] = ae[aei].red;
                a.orig_palette[3 * pen + 1] = ae[aei].green;
                a.orig_palette[3 * pen + 2] = ae[aei].blue;
                a.num_pens_used++;
                if (ae[aei].alpha < 255) {
                    a.transparency[pen] = ae[aei].alpha;
                    a.num_pens_trans++;
                }
            }

            if (ae[aei].box.max_y == -1) /* circle */ {
                int r = ae[aei].box.max_x;

                if ((circle = create_circle(r, pen)) != null) {
                    copybitmap(a.orig_artwork, circle, 0, 0,
                            ae[aei].box.min_x - r,
                            ae[aei].box.min_y - r,
                            null, TRANSPARENCY_PEN, 255);
                    osd_free_bitmap(circle);
                }
            } else {
                fillbitmap(a.orig_artwork, pen, ae[aei].box);
            }
            aei++;
        }

        /* Make sure we don't have too many colors */
        if (a.num_pens_used > max_pens) {
            if (errorlog != null) {
                fprintf(errorlog, "Too many colors in overlay.\n");
            }
            if (errorlog != null) {
                fprintf(errorlog, "Colors found: %d  Max Allowed: %d\n",
                        a.num_pens_used, max_pens);
            }
            artwork_free(a);
            return null;
        }

        /* If the game uses dynamic colors, we assume that it's safe
	   to init the palette and remap the colors now */
        if ((Machine.drv.video_attributes & VIDEO_MODIFIES_PALETTE) != 0) {
            backdrop_set_palette(a, a.orig_palette);
        }

        return a;
    }
}
