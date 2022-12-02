/*********************************************************************

  artwork.c

  Generic backdrop/overlay functions.

  Created by Mike Balfour - 10/01/1998

  Added some overlay and backdrop functions
  for vector games. Mathis Rosenhauer - 10/09/1998

  MAB - 09 MAR 1999 - made some changes to artwork_create
  MLR - 29 MAR 1999 - added circles to artwork_create

*********************************************************************/

#include "driver.h"
#include "png.h"
#include "artwork.h"

#ifndef MIN
#define MIN(x,y) (x)<(y)?(x):(y)
#endif
#ifndef MAX
#define MAX(x,y) (x)>(y)?(x):(y)
#endif


/* Local variables */
static unsigned char isblack[256];

/*
 * finds closest color and returns the index (for 256 color)
 */

static unsigned char find_pen(unsigned char r,unsigned char g,unsigned char b)
{
	int i,bi,ii;
	long x,y,z,bc;
	ii = 32;
	bi = 256;
	bc = 0x01000000;

	do
	{
		for( i=0; i<256; i++ )
		{
			unsigned char r1,g1,b1;

			osd_get_pen(Machine->pens[i],&r1,&g1,&b1);
			if((x=(long)(abs(r1-r)+1)) > ii) continue;
			if((y=(long)(abs(g1-g)+1)) > ii) continue;
			if((z=(long)(abs(b1-b)+1)) > ii) continue;
			x = x*y*z;
			if (x < bc)
			{
				bc = x;
				bi = i;
			}
		}
		ii<<=1;
	} while (bi==256);

	return(bi);
}

void backdrop_refresh_tables (struct artwork *a)
{
	int i,j, k, total_colors;
	unsigned char rgb1[3], rgb2[3], c[3];
	unsigned short *pens = Machine->pens;

	/* Calculate brightness of all colors */

	total_colors = Machine->drv->total_colors;

	for (i = 0; i < Machine->drv->total_colors; i++)
	{
		osd_get_pen (pens[i], &rgb1[0], &rgb1[1], &rgb1[2]);
		a->brightness[pens[i]]=(222*rgb1[0]+707*rgb1[1]+71*rgb1[2])/1000;
	}

	/* Calculate mixed colors */

	for( i=0; i < total_colors ;i++ )                /* color1 */
	{
		osd_get_pen(pens[i],&rgb1[0],&rgb1[1],&rgb1[2]);
		for( j=0; j < total_colors ;j++ )               /* color2 */
		{
			osd_get_pen(pens[j],&rgb2[0],&rgb2[1],&rgb2[2]);

			for (k=0; k<3; k++)
			{
				int tmp;
				tmp = rgb1[k]/4 + rgb2[k];
				if (tmp > 255)
					c[k] = 255;
				else
					c[k] = tmp;
			}
			a->pTable[i * total_colors + j] = find_pen(c[0],c[1],c[2]);
		}
	}
}

/*********************************************************************
  backdrop_refresh

  This remaps the "original" palette indexes to the abstract OS indexes
  used by MAME.  This needs to be called every time palette_recalc
  returns a non-zero value, since the mappings will have changed.
 *********************************************************************/

void backdrop_refresh(struct artwork *a)
{
	int i,j;
	int height,width;
	struct osd_bitmap *back = NULL;
	struct osd_bitmap *orig = NULL;
	int offset;

	offset = a->start_pen;
	back = a->artwork;
	orig = a->orig_artwork;
	height = a->artwork->height;
	width = a->artwork->width;

	if (back->depth == 8)
	{
		for ( j=0; j<height; j++)
			for (i=0; i<width; i++)
				back->line[j][i] = Machine->pens[orig->line[j][i]+offset];
	}
	else
	{
		for ( j=0; j<height; j++)
			for (i=0; i<width; i++)
				((unsigned short *)back->line[j])[i] = Machine->pens[((unsigned short *)orig->line[j])[i]+offset];
	}
}

/*********************************************************************
  backdrop_set_palette

  This sets the palette colors used by the backdrop to the new colors
  passed in as palette.  The values should be stored as one byte of red,
  one byte of blue, one byte of green.  This could hopefully be used
  for special effects, like lightening and darkening the backdrop.
 *********************************************************************/
void backdrop_set_palette(struct artwork *a, unsigned char *palette)
{
	int i;

	/* Load colors into the palette */
	if ((Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE))
	{
		for (i = 0; i < a->num_pens_used; i++)
			palette_change_color(i + a->start_pen, palette[i*3], palette[i*3+1], palette[i*3+2]);

		palette_recalc();
		backdrop_refresh(a);
	}
}

/*********************************************************************
  artwork_free

  Don't forget to clean up when you're done with the backdrop!!!
 *********************************************************************/

void artwork_free(struct artwork *a)
{
	if (a)
	{
		if (a->artwork)
			osd_free_bitmap(a->artwork);
		if (a->orig_artwork)
			osd_free_bitmap(a->orig_artwork);
		if (a->vector_bitmap)
			osd_free_bitmap(a->vector_bitmap);
		if (a->orig_palette)
			free (a->orig_palette);
		if (a->transparency)
			free (a->transparency);
		if (a->brightness)
			free (a->brightness);
		if (a->pTable)
			free (a->pTable);
		free(a);
	}
}

/*********************************************************************
  backdrop_black_recalc

  If you use any of the experimental backdrop draw* blitters below,
  call this once per frame.  It will catch palette changes and mark
  every black as transparent.  If it returns a non-zero value, redraw
  the whole background.
 *********************************************************************/

int backdrop_black_recalc(void)
{
	unsigned char r,g,b;
	int i;
	int redraw = 0;

	/* Determine what colors can be overwritten */
	for (i=0; i<256; i++)
	{
		osd_get_pen(i,&r,&g,&b);

		if ((r==0) && (g==0) && (b==0))
		{
			if (isblack[i] != 1)
				redraw = 1;
			isblack[i] = 1;
		}
		else
		{
			if (isblack[i] != 0)
				redraw = 1;
			isblack[i] = 0;
		}
	}
	return redraw;
}

/*********************************************************************
  draw_backdrop

  This is an experimental backdrop blitter.  How to use:
  1)  Draw the dirty background video game graphic with no transparency.
  2)  Call draw_backdrop with a clipping rectangle containing the location
      of the dirty_graphic.

  draw_backdrop will fill in everything that's colored black with the
  backdrop.
 *********************************************************************/

void draw_backdrop(struct osd_bitmap *dest,const struct osd_bitmap *src,int sx,int sy,
					const struct rectangle *clip)
{
	int ox,oy,ex,ey,y,start,dy;
	/*  int col;
		int *sd4;
		int trans4,col4;*/
	struct rectangle myclip;

	if (!src) return;
	if (!dest) return;

    /* Rotate and swap as necessary... */
	if (Machine->orientation & ORIENTATION_SWAP_XY)
	{
		int temp;

		temp = sx;
		sx = sy;
		sy = temp;

		if (clip)
		{
			/* clip and myclip might be the same, so we need a temporary storage */
			temp = clip->min_x;
			myclip.min_x = clip->min_y;
			myclip.min_y = temp;
			temp = clip->max_x;
			myclip.max_x = clip->max_y;
			myclip.max_y = temp;
			clip = &myclip;
		}
	}
	if (Machine->orientation & ORIENTATION_FLIP_X)
	{
		sx = dest->width - src->width - sx;
		if (clip)
		{
			int temp;


			/* clip and myclip might be the same, so we need a temporary storage */
			temp = clip->min_x;
			myclip.min_x = dest->width-1 - clip->max_x;
			myclip.max_x = dest->width-1 - temp;
			myclip.min_y = clip->min_y;
			myclip.max_y = clip->max_y;
			clip = &myclip;
		}
	}
	if (Machine->orientation & ORIENTATION_FLIP_Y)
	{
		sy = dest->height - src->height - sy;
		if (clip)
		{
			int temp;


			myclip.min_x = clip->min_x;
			myclip.max_x = clip->max_x;
			/* clip and myclip might be the same, so we need a temporary storage */
			temp = clip->min_y;
			myclip.min_y = dest->height-1 - clip->max_y;
			myclip.max_y = dest->height-1 - temp;
			clip = &myclip;
		}
	}


	/* check bounds */
	ox = sx;
	oy = sy;
	ex = sx + src->width-1;
	if (sx < 0) sx = 0;
	if (clip && sx < clip->min_x) sx = clip->min_x;
	if (ex >= dest->width) ex = dest->width-1;
	if (clip && ex > clip->max_x) ex = clip->max_x;
	if (sx > ex) return;
	ey = sy + src->height-1;
	if (sy < 0) sy = 0;
	if (clip && sy < clip->min_y) sy = clip->min_y;
	if (ey >= dest->height) ey = dest->height-1;
	if (clip && ey > clip->max_y) ey = clip->max_y;
	if (sy > ey) return;

    /* VERY IMPORTANT to mark this rectangle as dirty! :) - MAB */
	osd_mark_dirty (sx,sy,ex,ey,0);

	start = sy-oy;
	dy = 1;

	if (dest->depth == 8)
	{
		for (y = sy;y <= ey;y++)
		{
			const unsigned char *sd;
			unsigned char *bm,*bme;

			bm = dest->line[y];
			bme = bm + ex;
			sd = src->line[start] + (sx-ox);
			for( bm = bm+sx ; bm <= bme ; bm++ )
			{
				if (isblack[*bm])
					*bm = *sd;
				sd++;
			}
			start+=dy;
		}
	}
	else
	{
		for (y = sy;y <= ey;y++)
		{
			const unsigned short *sd;
			unsigned short *bm,*bme;

			bm = (unsigned short *)dest->line[y];
			bme = bm + ex;
			sd = ((unsigned short *)src->line[start]) + (sx-ox);
			for( bm = bm+sx ; bm <= bme ; bm++ )
			{
				if (isblack[*bm])
					*bm = *sd;
				sd++;
			}
			start+=dy;
		}
	}
}


/*********************************************************************
  drawgfx_backdrop

  This is an experimental backdrop blitter.  How to use:

  Every time you want to draw a background tile, instead of calling
  drawgfx, call this and pass in the backdrop bitmap.  Wherever the
  tile is black, the backdrop will be drawn.
 *********************************************************************/
void drawgfx_backdrop(struct osd_bitmap *dest,const struct GfxElement *gfx,
		unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
		const struct rectangle *clip,const struct osd_bitmap *back)
{
	int ox,oy,ex,ey,y,start,dy;
	const unsigned char *sd;
	/*int col;
	  int *sd4;
	  int trans4,col4;*/
	struct rectangle myclip;

	if (!gfx) return;

	code %= gfx->total_elements;
	color %= gfx->total_colors;

	if (Machine->orientation & ORIENTATION_SWAP_XY)
	{
		int temp;

		temp = sx;
		sx = sy;
		sy = temp;

		temp = flipx;
		flipx = flipy;
		flipy = temp;

		if (clip)
		{
			/* clip and myclip might be the same, so we need a temporary storage */
			temp = clip->min_x;
			myclip.min_x = clip->min_y;
			myclip.min_y = temp;
			temp = clip->max_x;
			myclip.max_x = clip->max_y;
			myclip.max_y = temp;
			clip = &myclip;
		}
	}
	if (Machine->orientation & ORIENTATION_FLIP_X)
	{
		sx = dest->width - gfx->width - sx;
		if (clip)
		{
			int temp;


			/* clip and myclip might be the same, so we need a temporary storage */
			temp = clip->min_x;
			myclip.min_x = dest->width-1 - clip->max_x;
			myclip.max_x = dest->width-1 - temp;
			myclip.min_y = clip->min_y;
			myclip.max_y = clip->max_y;
			clip = &myclip;
		}
	}
	if (Machine->orientation & ORIENTATION_FLIP_Y)
	{
		sy = dest->height - gfx->height - sy;
		if (clip)
		{
			int temp;


			myclip.min_x = clip->min_x;
			myclip.max_x = clip->max_x;
			/* clip and myclip might be the same, so we need a temporary storage */
			temp = clip->min_y;
			myclip.min_y = dest->height-1 - clip->max_y;
			myclip.max_y = dest->height-1 - temp;
			clip = &myclip;
		}
	}


	/* check bounds */
	ox = sx;
	oy = sy;
	ex = sx + gfx->width-1;
	if (sx < 0) sx = 0;
	if (clip && sx < clip->min_x) sx = clip->min_x;
	if (ex >= dest->width) ex = dest->width-1;
	if (clip && ex > clip->max_x) ex = clip->max_x;
	if (sx > ex) return;
	ey = sy + gfx->height-1;
	if (sy < 0) sy = 0;
	if (clip && sy < clip->min_y) sy = clip->min_y;
	if (ey >= dest->height) ey = dest->height-1;
	if (clip && ey > clip->max_y) ey = clip->max_y;
	if (sy > ey) return;

	osd_mark_dirty (sx,sy,ex,ey,0);	/* ASG 971011 */

	/* start = code * gfx->height; */
	if (flipy)	/* Y flop */
	{
		start = code * gfx->height + gfx->height-1 - (sy-oy);
		dy = -1;
	}
	else		/* normal */
	{
		start = code * gfx->height + (sy-oy);
		dy = 1;
	}


	if (dest->depth == 8)
	{
		unsigned char *bm,*bme;
		const unsigned char *sb;

		if (gfx->colortable)	/* remap colors */
		{
			const unsigned short *paldata;	/* ASG 980209 */

			paldata = &gfx->colortable[gfx->color_granularity * color];

			if (flipx)	/* X flip */
			{
				for (y = sy;y <= ey;y++)
				{
					bm  = dest->line[y];
					bme = bm + ex;
					sd = gfx->gfxdata + start * gfx->line_modulo + gfx->width-1 - (sx-ox);
    	            sb = back->line[y] + sx;
					for( bm += sx ; bm <= bme ; bm++ )
					{
						if (isblack[paldata[*sd]])
    	                    *bm = *sb;
    	                else
							*bm = paldata[*sd];
						sd--;
    	                sb++;
					}
					start+=dy;
				}
			}
			else		/* normal */
			{
				for (y = sy;y <= ey;y++)
				{
					bm  = dest->line[y];
					bme = bm + ex;
					sd = gfx->gfxdata + start * gfx->line_modulo + (sx-ox);
    	            sb = back->line[y] + sx;
					for( bm += sx ; bm <= bme ; bm++ )
					{
						if (isblack[paldata[*sd]])
    	                    *bm = *sb;
    	                else
							*bm = paldata[*sd];
						sd++;
    	                sb++;
					}
					start+=dy;
				}
			}
		}
		else
		{
			if (flipx)	/* X flip */
			{
				for (y = sy;y <= ey;y++)
				{
					bm = dest->line[y];
					bme = bm + ex;
					sd = gfx->gfxdata + start * gfx->line_modulo + gfx->width-1 - (sx-ox);
    	            sb = back->line[y] + sx;
					for( bm = bm+sx ; bm <= bme ; bm++ )
					{
						if (isblack[*sd])
    	                    *bm = *sb;
    	                else
							*bm = *sd;
						sd--;
    	                sb++;
					}
					start+=dy;
				}
			}
			else		/* normal */
			{
				for (y = sy;y <= ey;y++)
				{
					bm = dest->line[y];
					bme = bm + ex;
					sd = gfx->gfxdata + start * gfx->line_modulo + (sx-ox);
    	            sb = back->line[y] + sx;
					for( bm = bm+sx ; bm <= bme ; bm++ )
					{
						if (isblack[*sb])
    	                    *bm = *sb;
    	                else
							*bm = *sd;
						sd++;
    	                sb++;
					}
					start+=dy;
				}
			}
		}
	}
	else
	{
		unsigned short *bm,*bme;
		const unsigned short *sb;

		if (gfx->colortable)	/* remap colors */
		{
			const unsigned short *paldata;	/* ASG 980209 */

			paldata = &gfx->colortable[gfx->color_granularity * color];

			if (flipx)	/* X flip */
			{
				for (y = sy;y <= ey;y++)
				{
					bm  = (unsigned short *)dest->line[y];
					bme = bm + ex;
					sd = gfx->gfxdata + start * gfx->line_modulo + gfx->width-1 - (sx-ox);
    	            sb = ((unsigned short *)back->line[y]) + sx;
					for( bm += sx ; bm <= bme ; bm++ )
					{
						if (isblack[paldata[*sd]])
    	                    *bm = *sb;
    	                else
							*bm = paldata[*sd];
						sd--;
    	                sb++;
					}
					start+=dy;
				}
			}
			else		/* normal */
			{
				for (y = sy;y <= ey;y++)
				{
					bm  = (unsigned short *)dest->line[y];
					bme = bm + ex;
					sd = gfx->gfxdata + start * gfx->line_modulo + (sx-ox);
    	            sb = ((unsigned short *)back->line[y]) + sx;
					for( bm += sx ; bm <= bme ; bm++ )
					{
						if (isblack[paldata[*sd]])
    	                    *bm = *sb;
    	                else
							*bm = paldata[*sd];
						sd++;
    	                sb++;
					}
					start+=dy;
				}
			}
		}
		else
		{
			if (flipx)	/* X flip */
			{
				for (y = sy;y <= ey;y++)
				{
					bm = (unsigned short *)dest->line[y];
					bme = bm + ex;
					sd = gfx->gfxdata + start * gfx->line_modulo + gfx->width-1 - (sx-ox);
    	            sb = ((unsigned short *)back->line[y]) + sx;
					for( bm = bm+sx ; bm <= bme ; bm++ )
					{
						if (isblack[*sd])
    	                    *bm = *sb;
    	                else
							*bm = *sd;
						sd--;
    	                sb++;
					}
					start+=dy;
				}
			}
			else		/* normal */
			{
				for (y = sy;y <= ey;y++)
				{
					bm = (unsigned short *)dest->line[y];
					bme = bm + ex;
					sd = gfx->gfxdata + start * gfx->line_modulo + (sx-ox);
    	            sb = ((unsigned short *)back->line[y]) + sx;
					for( bm = bm+sx ; bm <= bme ; bm++ )
					{
						if (isblack[*sb])
    	                    *bm = *sb;
    	                else
							*bm = *sd;
						sd++;
    	                sb++;
					}
					start+=dy;
				}
			}
		}
	}
}

/*********************************************************************
  overlay_draw

  This is an experimental backdrop blitter.  How to use:
  1)  Refresh all of your bitmap.
		- This is usually done with copybitmap(bitmap,tmpbitmap,...);
  2)  Call overlay_draw with the bitmap and the overlay.

  Not so tough, is it? :)

  Note: we don't have to worry about marking dirty rectangles here,
  because we should only have color changes in redrawn sections, which
  should already be marked as dirty by the original blitter.

  TODO: support translucency and multiple intensities if we need to.

 *********************************************************************/

void overlay_draw(struct osd_bitmap *dest,const struct artwork *overlay)
{
	int i,j;
	int height,width;
	struct osd_bitmap *o = NULL;
	int black;

	o = overlay->artwork;
	height = overlay->artwork->height;
	width = overlay->artwork->width;
	black = Machine->pens[0];

	if (dest->depth == 8)
	{
		unsigned char *dst, *ovr;

		for ( j=0; j<height; j++)
		{
			dst = dest->line[j];
			ovr = o->line[j];
			for (i=0; i<width; i++)
			{
				if (*dst!=black)
					*dst = *ovr;
				dst++;
				ovr++;
			}
		}
	}
	else
	{
		unsigned short *dst, *ovr;

		for ( j=0; j<height; j++)
		{
			dst = (unsigned short *)dest->line[j];
			ovr = (unsigned short *)o->line[j];
			for (i=0; i<width; i++)
			{
				if (*dst!=black)
					*dst = *ovr;
				dst++;
				ovr++;
			}
		}
	}
}


/*********************************************************************
  RGBtoHSV and HSVtoRGB

  This is overkill for now but maybe they come in handy later
  (Stolen from Foley's book)
 *********************************************************************/
static void RGBtoHSV( float r, float g, float b, float *h, float *s, float *v )
{
	float min, max, delta;

	min = MIN( r, MIN( g, b ));
	max = MAX( r, MAX( g, b ));
	*v = max;

	delta = max - min;

	if( delta > 0  )
		*s = delta / max;
	else {
		*s = 0;
		*h = 0;
		return;
	}

	if( r == max )
		*h = ( g - b ) / delta;
	else if( g == max )
		*h = 2 + ( b - r ) / delta;
	else
		*h = 4 + ( r - g ) / delta;

	*h *= 60;
	if( *h < 0 )
		*h += 360;
}

static void HSVtoRGB( float *r, float *g, float *b, float h, float s, float v )
{
	int i;
	float f, p, q, t;

	if( s == 0 ) {
		*r = *g = *b = v;
		return;
	}

	h /= 60;
	i = h;
	f = h - i;
	p = v * ( 1 - s );
	q = v * ( 1 - s * f );
	t = v * ( 1 - s * ( 1 - f ) );

	switch( i ) {
	case 0: *r = v; *g = t; *b = p; break;
	case 1: *r = q; *g = v; *b = p; break;
	case 2: *r = p; *g = v; *b = t; break;
	case 3: *r = p; *g = q; *b = v; break;
	case 4: *r = t; *g = p; *b = v; break;
	default: *r = v; *g = p; *b = q; break;
	}

}

/*********************************************************************
  transparency_hist

  Calculates a histogram of all transparent pixels in the overlay.
  The function returns a array of ints with the number of shades
  for each transparent color based on the color histogram.
 *********************************************************************/
static unsigned int *transparency_hist (struct artwork *a, int num_shades)
{
	int i, j;
	unsigned int *hist;
	int num_pix=0, min_shades;
	unsigned char pen;

	if ((hist = (unsigned int *)malloc(a->num_pens_trans*sizeof(unsigned int)))==NULL)
	{
		if (errorlog)
			fprintf(errorlog,"Not enough memory!\n");
		return NULL;
	}
	memset (hist, 0, a->num_pens_trans*sizeof(int));

	if (a->orig_artwork->depth == 8)
	{
		for ( j=0; j<a->orig_artwork->height; j++)
			for (i=0; i<a->orig_artwork->width; i++)
			{
				pen = a->orig_artwork->line[j][i];
				if (pen < a->num_pens_trans)
				{
					hist[pen]++;
					num_pix++;
				}
			}
	}
	else
	{
		for ( j=0; j<a->orig_artwork->height; j++)
			for (i=0; i<a->orig_artwork->width; i++)
			{
				pen = ((unsigned short *)a->orig_artwork->line[j])[i];
				if (pen < a->num_pens_trans)
				{
					hist[pen]++;
					num_pix++;
				}
			}
	}

	/* we try to get at least 3 shades per transparent color */
	min_shades = ((num_shades-a->num_pens_used-3*a->num_pens_trans) < 0) ? 0 : 3;

	if (errorlog && (min_shades==0))
		fprintf(errorlog,"Too many colors in overlay. Vector colors may be wrong.\n");

	num_pix /= num_shades-(a->num_pens_used-a->num_pens_trans)
		-min_shades*a->num_pens_trans;

	for (i=0; i<a->num_pens_trans; i++)
		hist[i] = hist[i]/num_pix + min_shades;

	return hist;
}

/*********************************************************************
  overlay_set_palette

  Generates a palette for vector games with an overlay.

  The 'glowing' effect is simulated by alpha blending the transparent
  colors with a black (the screen) background. Then different shades
  of each transparent color are calculated by alpha blending this
  color with different levels of brightness (values in HSV) of the
  transparent color from v=0 to v=1. This doesn't work very well with
  blue. The number of shades is proportional to the number of pixels of
  that color. A look up table is also generated to map beam
  brightness and overlay colors to pens. If you have a beam brightness
  of 128 under a transparent pixel of pen 7 then
     Table (7,128)
  returns the pen of the resulting color. The table is usually
  converted to OS colors later.
 *********************************************************************/
int overlay_set_palette (struct artwork *a, unsigned char *palette, int num_shades)
{
	unsigned int i,j, shades=0, step;
	unsigned int *hist;
	float h, s, v, r, g, b;

	/* adjust palette start */

	palette += 3*a->start_pen;

	if((hist = transparency_hist (a, num_shades))==NULL)
		return 0;

	/* Copy all artwork colors to the palette */
	memcpy (palette, a->orig_palette, 3*a->num_pens_used);

	/* Fill the palette with shades of the transparent colors */
	for (i=0; i<a->num_pens_trans; i++)
	{
		RGBtoHSV( a->orig_palette[i*3]/255.0,
			  a->orig_palette[i*3+1]/255.0,
			  a->orig_palette[i*3+2]/255.0, &h, &s, &v );

		/* blend transparent entries with black background */
		/* we don't need the original palette entry any more */
		HSVtoRGB ( &r, &g, &b, h, s, v*a->transparency[i]/255.0);
		palette [i*3+0] = r * 255.0;
		palette [i*3+1] = g * 255.0;
		palette [i*3+2] = b * 255.0;

		if (hist[i]>1)
		{
			for (j=0; j<hist[i]-1; j++)
			{
				/* we start from 1 because the 0 level is already in the palette */
				HSVtoRGB ( &r, &g, &b, h, s, v*a->transparency[i]/255.0 +
					   ((1-(v*a->transparency[i]/255.0))*(j+1))/(hist[i]-1));
				palette [(a->num_pens_used + shades + j)*3+0] = r * 255.0;
				palette [(a->num_pens_used + shades + j)*3+1] = g * 255.0;
				palette [(a->num_pens_used + shades + j)*3+2] = b * 255.0;
			}

			/* create alpha LUT for quick alpha blending */
			for (j=0; j<256; j++)
			{
				step = hist[i]*j/256.0;
				if (step == 0)
				/* no beam, just overlay over black screen */
					a->pTable[i*256+j] = i + a->start_pen;
				else
					a->pTable[i*256+j] = a->num_pens_used +
						shades + step-1 + a->start_pen;
			}

			shades += hist[i]-1;
		}
	}

	return 1;
}

/*********************************************************************
  overlay_remap

  This remaps the "original" palette indexes to the abstract OS indexes
  used by MAME. This has to be called during startup after the
  OS colors have been initialized (vh_start).
 *********************************************************************/
void overlay_remap(struct artwork *a)
{
	int i,j;
	unsigned char r,g,b;

	int offset = a->start_pen;
	int height = a->artwork->height;
	int width = a->artwork->width;
	struct osd_bitmap *overlay = a->artwork;
	struct osd_bitmap *orig = a->orig_artwork;

	if (overlay->depth == 8)
	{
		for ( j=0; j<height; j++)
			for (i=0; i<width; i++)
				overlay->line[j][i] = Machine->pens[orig->line[j][i]+offset];
	}
	else
	{
		for ( j=0; j<height; j++)
			for (i=0; i<width; i++)
				((unsigned short *)overlay->line[j])[i] = Machine->pens[((unsigned short *)orig->line[j])[i]+offset];
	}

	/* Calculate brightness of all colors */

	for (i = 0; i < Machine->drv->total_colors; i++)
	{
		osd_get_pen (Machine->pens[i], &r, &g, &b);
		a->brightness[Machine->pens[i]]=(222*r+707*g+71*b)/1000;
	}

	/* Erase vector bitmap same way as in vector.c */
	if (a->vector_bitmap)
		fillbitmap(a->vector_bitmap,Machine->pens[0],0);
}

/*********************************************************************
  allocate_artwork_mem

  Allocates memory for all the bitmaps.
 *********************************************************************/
static struct artwork *allocate_artwork_mem (int width, int height)
{
	struct artwork *a;
	int temp;

	if (Machine->orientation & ORIENTATION_SWAP_XY)
	{
		temp = height;
		height = width;
		width = temp;
	}

	a = (struct artwork *)malloc(sizeof(struct artwork));
	if (a == 0)
	{
		if (errorlog) fprintf(errorlog,"Not enough memory for artwork!\n");
		return NULL;
	}

	a->transparency = NULL;
	a->orig_palette = NULL;
	a->pTable = NULL;
	a->brightness = NULL;
	a->vector_bitmap = NULL;

	if ((a->orig_artwork = osd_create_bitmap(width, height)) == 0)
	{
		if (errorlog) fprintf(errorlog,"Not enough memory for artwork!\n");
		artwork_free(a);
		return NULL;
	}
	fillbitmap(a->orig_artwork,0,0);

	/* Create a second bitmap for public use */
	if ((a->artwork = osd_create_bitmap(width,height)) == 0)
	{
		if (errorlog) fprintf(errorlog,"Not enough memory for artwork!\n");
		artwork_free(a);
		return NULL;
	}

	if ((a->pTable = (unsigned char*)malloc(256*256))==0)
	{
		if (errorlog)
			fprintf(errorlog,"Not enough memory.\n");
		artwork_free(a);
		return NULL;
	}

	if ((a->brightness = (unsigned char*)malloc(256*256))==0)
	{
		if (errorlog)
			fprintf(errorlog,"Not enough memory.\n");
		artwork_free(a);
		return NULL;
	}
	memset (a->brightness, 0, 256*256);

	/* Create bitmap for the vector screen */
	if (Machine->drv->video_attributes & VIDEO_TYPE_VECTOR)
	{
		if ((a->vector_bitmap = osd_create_bitmap(width,height)) == 0)
		{
			if (errorlog) fprintf(errorlog,"Not enough memory for artwork!\n");
			artwork_free(a);
			return NULL;
		}
		fillbitmap(a->vector_bitmap,0,0);
	}

	return a;
}

/*********************************************************************

  Reads a PNG for a artwork struct and checks if it has the right
  format.

 *********************************************************************/
static int artwork_read_bitmap(const char *file_name, struct osd_bitmap **bitmap, struct png_info *p)
{
	UINT8 *tmp;
	UINT32 orientation;
	UINT32 x, y;
	void *fp;

	if (!(fp = osd_fopen(Machine->gamedrv->name, file_name, OSD_FILETYPE_ARTWORK, 0)))
	{
		if (errorlog)
			fprintf(errorlog,"Unable to open PNG %s\n", file_name);
		return 0;
	}

	if (!png_read_file(fp, p))
	{
		osd_fclose (fp);
		return 0;
	}
	osd_fclose (fp);

	if (p->bit_depth > 8)
	{
		if (errorlog)
			fprintf(errorlog,"Unsupported bit depth %i (8 bit max.)\n", p->bit_depth);
		return 0;
	}

	if (p->color_type != 3)
	{
		if (errorlog)
			fprintf(errorlog,"Unsupported color type %i (has to be 3)\n", p->color_type);
		return 0;
	}
	if (p->interlace_method != 0)
	{
		if (errorlog)
			fprintf(errorlog,"Interlace unsupported\n");
		return 0;
	}

	/* Convert to 8 bit */
	png_expand_buffer_8bit (p);

	png_delete_unused_colors (p);

	if ((*bitmap=osd_create_bitmap(p->width,p->height)) == 0)
	{
		if (errorlog)
			fprintf(errorlog,"Unable to allocate memory for artwork\n");
		return 0;
	}

	orientation = Machine->orientation;

	tmp = p->image;
	for (y=0; y<p->height; y++)
		for (x=0; x<p->width; x++)
		{
			plot_pixel(*bitmap, x, y, *tmp++);
		}

	free (p->image);
	return 1;
}

/*********************************************************************
  artwork_load(_size)

  This is what loads your backdrop in from disk.
  start_pen = the first pen available for the backdrop to use
  max_pens = the number of pens the backdrop can use
  So, for example, suppose you want to load "dotron.png", have it
  start with color 192, and only use 48 pens.  You would call
  backdrop = backdrop_load("dotron.png",192,48);
 *********************************************************************/

struct artwork *artwork_load_size(const char *filename, int start_pen, int max_pens, int width, int height)
{
	struct osd_bitmap *picture = NULL;
	struct artwork *a = NULL;
	struct png_info p;

	/* If the user turned artwork off, bail */
	if (!options.use_artwork) return NULL;

	if ((a = allocate_artwork_mem(width, height))==NULL)
		return NULL;

	a->start_pen = start_pen;

	if (!artwork_read_bitmap(filename, &picture, &p))
	{
		artwork_free(a);
		return NULL;
	}

	a->num_pens_used = p.num_palette;
	a->num_pens_trans = p.num_trans;
	a->orig_palette = p.palette;
	a->transparency = p.trans;

	/* Make sure we don't have too many colors */
	if (a->num_pens_used > max_pens)
	{
		if (errorlog) fprintf(errorlog,"Too many colors in overlay.\n");
		if (errorlog) fprintf(errorlog,"Colors found: %d  Max Allowed: %d\n",
				      a->num_pens_used,max_pens);
		artwork_free(a);
		osd_free_bitmap(picture);
		return NULL;
	}

	/* Scale the original picture to be the same size as the visible area */
	if (Machine->orientation & ORIENTATION_SWAP_XY)
		copybitmapzoom(a->orig_artwork,picture,0,0,0,0,
			       0, TRANSPARENCY_NONE, 0,
			       (a->orig_artwork->height<<16)/picture->height,
			       (a->orig_artwork->width<<16)/picture->width);
	else
		copybitmapzoom(a->orig_artwork,picture,0,0,0,0,
			       0, TRANSPARENCY_NONE, 0,
			       (a->orig_artwork->width<<16)/picture->width,
			       (a->orig_artwork->height<<16)/picture->height);

	/* If the game uses dynamic colors, we assume that it's safe
	   to init the palette and remap the colors now */
	if (Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE)
		backdrop_set_palette(a,a->orig_palette);

	/* We don't need the original any more */
	osd_free_bitmap(picture);

	return a;
}

struct artwork *artwork_load(const char *filename, int start_pen, int max_pens)
{
	return artwork_load_size (filename, start_pen, max_pens, Machine->scrbitmap->width, Machine->scrbitmap->height);
}

/*********************************************************************
  create_circle

  Creates a circle with radius r in the color of pen. A new bitmap
  is allocated for the circle. The background is set to pen 255.

*********************************************************************/
static struct osd_bitmap *create_circle (int r, int pen)
{
	struct osd_bitmap *circle;

	int x = 0, twox = 0;
	int y = r;
	int twoy = r+r;
	int p = 1 - r;
	int i;

	if ((circle = osd_create_bitmap(twoy, twoy)) == 0)
	{
		if (errorlog) fprintf(errorlog,"Not enough memory for artwork!\n");
		return NULL;
	}

	/* background */
	fillbitmap (circle, 255, 0);

	while (x < y)
	{
		x++;
		twox +=2;
		if (p < 0)
			p += twox + 1;
		else
		{
			y--;
			twoy -= 2;
			p += twox - twoy + 1;
		}

		for (i = 0; i < twox; i++)
		{
			plot_pixel(circle, r-x+i, r-y  , pen);
			plot_pixel(circle, r-x+i, r+y-1, pen);
		}

		for (i = 0; i < twoy; i++)
		{
			plot_pixel(circle, r-y+i, r-x  , pen);
			plot_pixel(circle, r-y+i, r+x-1, pen);
		}
	}
	return circle;
}

/*********************************************************************
  artwork_elements scale

  scales an array of artwork elements to width and height. The first
  element (which has to be a box) is used as reference. This is useful
  for atwork with circles.

*********************************************************************/

void artwork_elements_scale(struct artwork_element *ae, int width, int height)
{
	int scale_w, scale_h;

	if (Machine->orientation & ORIENTATION_SWAP_XY)
	{
		scale_w = (height << 16)/(ae->box.max_x + 1);
		scale_h = (width << 16)/(ae->box.max_y + 1);
	}
	else
	{
		scale_w = (width << 16)/(ae->box.max_x + 1);
		scale_h = (height << 16)/(ae->box.max_y + 1);
	}
	while (ae->box.min_x >= 0)
	{
		ae->box.min_x = (ae->box.min_x * scale_w) >> 16;
		ae->box.max_x = (ae->box.max_x * scale_w) >> 16;
		ae->box.min_y = (ae->box.min_y * scale_h) >> 16;
		if (ae->box.max_y >= 0)
			ae->box.max_y = (ae->box.max_y * scale_h) >> 16;
		ae++;
	}
}

/*********************************************************************
  artwork_create

  This works similar to artwork_load but generates artwork from
  an array of artwork_element. This is useful for very simple artwork
  like the overlay in the Space invaders series of games.  The overlay
  is defined to be the same size as the screen.
  The end of the array is marked by an entry with negative coordinates.
  Boxes and circles are supported. Circles are marked max_y == -1,
  min_x == x coord. of center, min_y == y coord. of center, max_x == radius.
  If there are transparent and opaque overlay elements, the opaque ones
  have to be at the end of the list to stay compatible with the PNG
  artwork.
 *********************************************************************/
struct artwork *artwork_create(const struct artwork_element *ae, int start_pen, int max_pens)
{
	struct artwork *a;
	struct osd_bitmap *circle;
	int pen;

	if ((a = allocate_artwork_mem(Machine->scrbitmap->width, Machine->scrbitmap->height))==NULL)
		return NULL;

	a->start_pen = start_pen;

	if ((a->orig_palette = (unsigned char *)malloc(256*3)) == NULL)
	{
		if (errorlog) fprintf(errorlog,"Not enough memory for overlay!\n");
		artwork_free(a);
		return NULL;
	}

	if ((a->transparency = (unsigned char *)malloc(256)) == NULL)
	{
		if (errorlog) fprintf(errorlog,"Not enough memory for overlay!\n");
		artwork_free(a);
		return NULL;
	}

	a->num_pens_used = 0;
	a->num_pens_trans = 0;

	while (ae->box.min_x >= 0)
	{
		/* look if the color is already in the palette */
		pen =0;
		while ((pen < a->num_pens_used) &&
		       ((ae->red != a->orig_palette[3*pen]) ||
				(ae->green != a->orig_palette[3*pen+1]) ||
				(ae->blue != a->orig_palette[3*pen+2]) ||
				((ae->alpha < 255) && (ae->alpha != a->transparency[pen]))))
			pen++;

		if (pen == a->num_pens_used)
		{
			a->orig_palette[3*pen]=ae->red;
			a->orig_palette[3*pen+1]=ae->green;
			a->orig_palette[3*pen+2]=ae->blue;
			a->num_pens_used++;
			if (ae->alpha < 255)
			{
				a->transparency[pen]=ae->alpha;
				a->num_pens_trans++;
			}
		}

		if (ae->box.max_y == -1) /* circle */
		{
			int r = ae->box.max_x;

			if ((circle = create_circle (r, pen)) != NULL)
			{
				copybitmap(a->orig_artwork,circle,0, 0,
						   ae->box.min_x - r,
						   ae->box.min_y - r,
						   0,TRANSPARENCY_PEN,255);
				osd_free_bitmap(circle);
			}
		}
		else
			fillbitmap (a->orig_artwork, pen, &ae->box);
		ae++;
	}

	/* Make sure we don't have too many colors */
	if (a->num_pens_used > max_pens)
	{
		if (errorlog) fprintf(errorlog,"Too many colors in overlay.\n");
		if (errorlog) fprintf(errorlog,"Colors found: %d  Max Allowed: %d\n",
				      a->num_pens_used,max_pens);
		artwork_free(a);
		return NULL;
	}

	/* If the game uses dynamic colors, we assume that it's safe
	   to init the palette and remap the colors now */
	if (Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE)
		backdrop_set_palette(a,a->orig_palette);

	return a;
}
