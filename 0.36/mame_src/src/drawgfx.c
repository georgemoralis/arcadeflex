#ifndef DECLARE

#include "driver.h"


/* LBO */
#ifdef LSB_FIRST
#define BL0 0
#define BL1 1
#define BL2 2
#define BL3 3
#define WL0 0
#define WL1 1
#else
#define BL0 3
#define BL1 2
#define BL2 1
#define BL3 0
#define WL0 1
#define WL1 0
#endif


UINT8 gfx_drawmode_table[256];
plot_pixel_proc plot_pixel;
read_pixel_proc read_pixel;


#ifdef ALIGN_INTS /* GSL 980108 read/write nonaligned dword routine for ARM processor etc */

INLINE int read_dword(void *address)
{
	if ((long)address & 3)
	{
#ifdef LSB_FIRST  /* little endian version */
  		return ( *((unsigned char *)address) +
				(*((unsigned char *)address+1) << 8)  +
				(*((unsigned char *)address+2) << 16) +
				(*((unsigned char *)address+3) << 24) );
#else             /* big endian version */
  		return ( *((unsigned char *)address+3) +
				(*((unsigned char *)address+2) << 8)  +
				(*((unsigned char *)address+1) << 16) +
				(*((unsigned char *)address)   << 24) );
#endif
	}
	else
		return *(int *)address;
}


INLINE void write_dword(void *address, int data)
{
  	if ((long)address & 3)
	{
#ifdef LSB_FIRST
    		*((unsigned char *)address) =    data;
    		*((unsigned char *)address+1) = (data >> 8);
    		*((unsigned char *)address+2) = (data >> 16);
    		*((unsigned char *)address+3) = (data >> 24);
#else
    		*((unsigned char *)address+3) =  data;
    		*((unsigned char *)address+2) = (data >> 8);
    		*((unsigned char *)address+1) = (data >> 16);
    		*((unsigned char *)address)   = (data >> 24);
#endif
		return;
  	}
  	else
		*(int *)address = data;
}
#else
#define read_dword(address) *(int *)address
#define write_dword(address,data) *(int *)address=data
#endif



INLINE int readbit(const unsigned char *src,int bitnum)
{
	return (src[bitnum / 8] >> (7 - bitnum % 8)) & 1;
}


void decodechar(struct GfxElement *gfx,int num,const unsigned char *src,const struct GfxLayout *gl)
{
	int plane,x,y;
	unsigned char *dp;
	int offs;


	offs = num * gl->charincrement;
	dp = gfx->gfxdata + num * gfx->char_modulo;
	for (y = 0;y < gfx->height;y++)
	{
		int yoffs;

		yoffs = y;
#ifdef PREROTATE_GFX
		if (Machine->orientation & ORIENTATION_FLIP_Y)
			yoffs = gfx->height-1 - yoffs;
#endif

		for (x = 0;x < gfx->width;x++)
		{
			int xoffs;

			xoffs = x;
#ifdef PREROTATE_GFX
			if (Machine->orientation & ORIENTATION_FLIP_X)
				xoffs = gfx->width-1 - xoffs;
#endif

			dp[x] = 0;
			if (Machine->orientation & ORIENTATION_SWAP_XY)
			{
				for (plane = 0;plane < gl->planes;plane++)
				{
					if (readbit(src,offs + gl->planeoffset[plane] + gl->yoffset[xoffs] + gl->xoffset[yoffs]))
						dp[x] |= (1 << (gl->planes-1-plane));
				}
			}
			else
			{
				for (plane = 0;plane < gl->planes;plane++)
				{
					if (readbit(src,offs + gl->planeoffset[plane] + gl->yoffset[yoffs] + gl->xoffset[xoffs]))
						dp[x] |= (1 << (gl->planes-1-plane));
				}
			}
		}
		dp += gfx->line_modulo;
	}


	if (gfx->pen_usage)
	{
		/* fill the pen_usage array with info on the used pens */
		gfx->pen_usage[num] = 0;

		dp = gfx->gfxdata + num * gfx->char_modulo;
		for (y = 0;y < gfx->height;y++)
		{
			for (x = 0;x < gfx->width;x++)
			{
				gfx->pen_usage[num] |= 1 << dp[x];
			}
			dp += gfx->line_modulo;
		}
	}
}


struct GfxElement *decodegfx(const unsigned char *src,const struct GfxLayout *gl)
{
	int c;
	struct GfxElement *gfx;


	if ((gfx = malloc(sizeof(struct GfxElement))) == 0)
		return 0;
	memset(gfx,0,sizeof(struct GfxElement));

	if (Machine->orientation & ORIENTATION_SWAP_XY)
	{
		gfx->width = gl->height;
		gfx->height = gl->width;
	}
	else
	{
		gfx->width = gl->width;
		gfx->height = gl->height;
	}

	gfx->line_modulo = gfx->width;
	gfx->char_modulo = gfx->line_modulo * gfx->height;
	if ((gfx->gfxdata = malloc(gl->total * gfx->char_modulo * sizeof(unsigned char))) == 0)
	{
		free(gfx);
		return 0;
	}

	gfx->total_elements = gl->total;
	gfx->color_granularity = 1 << gl->planes;

	gfx->pen_usage = 0; /* need to make sure this is NULL if the next test fails) */
	if (gfx->color_granularity <= 32)	/* can't handle more than 32 pens */
		gfx->pen_usage = malloc(gfx->total_elements * sizeof(int));
		/* no need to check for failure, the code can work without pen_usage */

	for (c = 0;c < gl->total;c++)
		decodechar(gfx,c,src,gl);

	return gfx;
}


void freegfx(struct GfxElement *gfx)
{
	if (gfx)
	{
		free(gfx->pen_usage);
		free(gfx->gfxdata);
		free(gfx);
	}
}




INLINE void blockmove_transpen_noremap8(
		const UINT8 *srcdata,int srcwidth,int srcheight,int srcmodulo,
		UINT8 *dstdata,int dstmodulo,
		int transpen)
{
	UINT8 *end;
	int trans4;
	UINT32 *sd4;

	srcmodulo -= srcwidth;
	dstmodulo -= srcwidth;

	trans4 = transpen * 0x01010101;

	while (srcheight)
	{
		end = dstdata + srcwidth;
		while (((long)srcdata & 3) && dstdata < end)	/* longword align */
		{
			int col;

			col = *(srcdata++);
			if (col != transpen) *dstdata = col;
			dstdata++;
		}
		sd4 = (UINT32 *)srcdata;
		while (dstdata <= end - 4)
		{
			UINT32 col4;

			if ((col4 = *(sd4++)) != trans4)
			{
				UINT32 xod4;

				xod4 = col4 ^ trans4;
				if( (xod4&0x000000ff) && (xod4&0x0000ff00) &&
					(xod4&0x00ff0000) && (xod4&0xff000000) )
				{
					write_dword((UINT32 *)dstdata,col4);
				}
				else
				{
					if (xod4 & 0xff000000) dstdata[BL3] = col4 >> 24;
					if (xod4 & 0x00ff0000) dstdata[BL2] = col4 >> 16;
					if (xod4 & 0x0000ff00) dstdata[BL1] = col4 >>  8;
					if (xod4 & 0x000000ff) dstdata[BL0] = col4;
				}
			}
			dstdata += 4;
		}
		srcdata = (unsigned char *)sd4;
		while (dstdata < end)
		{
			int col;

			col = *(srcdata++);
			if (col != transpen) *dstdata = col;
			dstdata++;
		}

		srcdata += srcmodulo;
		dstdata += dstmodulo;
		srcheight--;
	}
}

INLINE void blockmove_transpen_noremap_flipx8(
		const UINT8 *srcdata,int srcwidth,int srcheight,int srcmodulo,
		UINT8 *dstdata,int dstmodulo,
		int transpen)
{
	UINT8 *end;
	int trans4;
	UINT32 *sd4;

	srcmodulo += srcwidth;
	dstmodulo -= srcwidth;
	//srcdata += srcwidth-1;
	srcdata -= 3;

	trans4 = transpen * 0x01010101;

	while (srcheight)
	{
		end = dstdata + srcwidth;
		while (((long)srcdata & 3) && dstdata < end)	/* longword align */
		{
			int col;

			col = srcdata[3];
			srcdata--;
			if (col != transpen) *dstdata = col;
			dstdata++;
		}
		sd4 = (UINT32 *)srcdata;
		while (dstdata <= end - 4)
		{
			UINT32 col4;

			if ((col4 = *(sd4--)) != trans4)
			{
				UINT32 xod4;

				xod4 = col4 ^ trans4;
				if (xod4 & 0x000000ff) dstdata[BL3] = col4;
				if (xod4 & 0x0000ff00) dstdata[BL2] = col4 >>  8;
				if (xod4 & 0x00ff0000) dstdata[BL1] = col4 >> 16;
				if (xod4 & 0xff000000) dstdata[BL0] = col4 >> 24;
			}
			dstdata += 4;
		}
		srcdata = (unsigned char *)sd4;
		while (dstdata < end)
		{
			int col;

			col = srcdata[3];
			srcdata--;
			if (col != transpen) *dstdata = col;
			dstdata++;
		}

		srcdata += srcmodulo;
		dstdata += dstmodulo;
		srcheight--;
	}
}


INLINE void blockmove_transpen_noremap16(
		const UINT16 *srcdata,int srcwidth,int srcheight,int srcmodulo,
		UINT16 *dstdata,int dstmodulo,
		int transpen)
{
	UINT16 *end;

	srcmodulo -= srcwidth;
	dstmodulo -= srcwidth;

	while (srcheight)
	{
		end = dstdata + srcwidth;
		while (dstdata < end)
		{
			int col;

			col = *(srcdata++);
			if (col != transpen) *dstdata = col;
			dstdata++;
		}

		srcdata += srcmodulo;
		dstdata += dstmodulo;
		srcheight--;
	}
}

INLINE void blockmove_transpen_noremap_flipx16(
		const UINT16 *srcdata,int srcwidth,int srcheight,int srcmodulo,
		UINT16 *dstdata,int dstmodulo,
		int transpen)
{
	UINT16 *end;

	srcmodulo += srcwidth;
	dstmodulo -= srcwidth;
	//srcdata += srcwidth-1;

	while (srcheight)
	{
		end = dstdata + srcwidth;
		while (dstdata < end)
		{
			int col;

			col = *(srcdata--);
			if (col != transpen) *dstdata = col;
			dstdata++;
		}

		srcdata += srcmodulo;
		dstdata += dstmodulo;
		srcheight--;
	}
}


#define DATA_TYPE UINT8
#define DECLARE(function,args,body) INLINE void function##8 args body
#define BLOCKMOVE(function,flipx,args) \
	if (flipx) blockmove_##function##_flipx##8 args ; \
	else blockmove_##function##8 args
#include "drawgfx.c"
#undef DATA_TYPE
#undef DECLARE
#undef BLOCKMOVE

#define DATA_TYPE UINT16
#define DECLARE(function,args,body) INLINE void function##16 args body
#define BLOCKMOVE(function,flipx,args) \
	if (flipx) blockmove_##function##_flipx##16 args ; \
	else blockmove_##function##16 args
#include "drawgfx.c"
#undef DATA_TYPE
#undef DECLARE
#undef BLOCKMOVE


/***************************************************************************

  Draw graphic elements in the specified bitmap.

  transparency == TRANSPARENCY_NONE - no transparency.
  transparency == TRANSPARENCY_PEN - bits whose _original_ value is == transparent_color
                                     are transparent. This is the most common kind of
									 transparency.
  transparency == TRANSPARENCY_PENS - as above, but transparent_color is a mask of
  									 transparent pens.
  transparency == TRANSPARENCY_COLOR - bits whose _remapped_ palette index (taken from
                                     Machine->game_colortable) is == transparent_color
  transparency == TRANSPARENCY_THROUGH - if the _destination_ pixel is == transparent_color,
                                     the source pixel is drawn over it. This is used by
									 e.g. Jr. Pac Man to draw the sprites when the background
									 has priority over them.

  transparency == TRANSPARENCY_PEN_TABLE - the transparency condition is same as TRANSPARENCY_PEN
					A special drawing is done according to gfx_drawmode_table[source pixel].
					DRAWMODE_NONE      transparent
					DRAWMODE_SOURCE    normal, draw source pixel.
					DRAWMODE_SHADOW    destination is changed through palette_shadow_table[]
					DRAWMODE_HIGHLIGHT destination is changed through palette_highlight_table[]

***************************************************************************/

void drawgfx(struct osd_bitmap *dest,const struct GfxElement *gfx,
		unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
		const struct rectangle *clip,int transparency,int transparent_color)
{
	struct rectangle myclip;

	if (!gfx)
	{
		usrintf_showmessage("drawgfx() gfx == 0");
		return;
	}
	if (!gfx->colortable)
	{
		usrintf_showmessage("drawgfx() gfx->colortable == 0");
		return;
	}

	code %= gfx->total_elements;
	color %= gfx->total_colors;

	if (gfx->pen_usage && (transparency == TRANSPARENCY_PEN || transparency == TRANSPARENCY_PENS))
	{
		int transmask = 0;

		if (transparency == TRANSPARENCY_PEN)
		{
			transmask = 1 << transparent_color;
		}
		else if (transparency == TRANSPARENCY_PENS)
		{
			transmask = transparent_color;
		}

		if ((gfx->pen_usage[code] & ~transmask) == 0)
			/* character is totally transparent, no need to draw */
			return;
		else if ((gfx->pen_usage[code] & transmask) == 0 && transparency != TRANSPARENCY_THROUGH && transparency != TRANSPARENCY_PEN_TABLE )
			/* character is totally opaque, can disable transparency */
			transparency = TRANSPARENCY_NONE;
	}

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
#ifndef PREROTATE_GFX
		flipx = !flipx;
#endif
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
#ifndef PREROTATE_GFX
		flipy = !flipy;
#endif
	}

	if (dest->depth != 16)
		drawgfx_core8(dest,gfx,code,color,flipx,flipy,sx,sy,clip,transparency,transparent_color);
	else
		drawgfx_core16(dest,gfx,code,color,flipx,flipy,sx,sy,clip,transparency,transparent_color);
}


/***************************************************************************

  Use drawgfx() to copy a bitmap onto another at the given position.
  This function will very likely change in the future.

***************************************************************************/
void copybitmap(struct osd_bitmap *dest,struct osd_bitmap *src,int flipx,int flipy,int sx,int sy,
		const struct rectangle *clip,int transparency,int transparent_color)
{
	struct rectangle myclip;


	/* if necessary, remap the transparent color */
	if (transparency == TRANSPARENCY_COLOR)
		transparent_color = Machine->pens[transparent_color];


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

	if (dest->depth != 16)
		copybitmap_core8(dest,src,flipx,flipy,sx,sy,clip,transparency,transparent_color);
	else
		copybitmap_core16(dest,src,flipx,flipy,sx,sy,clip,transparency,transparent_color);
}


void copybitmapzoom(struct osd_bitmap *dest_bmp,struct osd_bitmap *source_bmp,int flipx,int flipy,int sx,int sy,
		const struct rectangle *clip,int transparency,int transparent_color,int scalex,int scaley)
{
	struct rectangle myclip;


	/*
	scalex and scaley are 16.16 fixed point numbers
	1<<15 : shrink to 50%
	1<<16 : uniform scale
	1<<17 : double to 200%
	*/

	/* if necessary, remap the transparent color */
	if (transparency == TRANSPARENCY_COLOR)
		transparent_color = Machine->pens[transparent_color];

	if (Machine->orientation & ORIENTATION_SWAP_XY)
	{
		int temp;

		temp = sx;
		sx = sy;
		sy = temp;

		temp = flipx;
		flipx = flipy;
		flipy = temp;

		temp = scalex;
		scalex = scaley;
		scaley = temp;

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
		sx = dest_bmp->width - ((source_bmp->width * scalex + 0x7fff) >> 16) - sx;
		if (clip)
		{
			int temp;


			/* clip and myclip might be the same, so we need a temporary storage */
			temp = clip->min_x;
			myclip.min_x = dest_bmp->width-1 - clip->max_x;
			myclip.max_x = dest_bmp->width-1 - temp;
			myclip.min_y = clip->min_y;
			myclip.max_y = clip->max_y;
			clip = &myclip;
		}
	}
	if (Machine->orientation & ORIENTATION_FLIP_Y)
	{
		sy = dest_bmp->height - ((source_bmp->height * scaley + 0x7fff) >> 16) - sy;
		if (clip)
		{
			int temp;


			myclip.min_x = clip->min_x;
			myclip.max_x = clip->max_x;
			/* clip and myclip might be the same, so we need a temporary storage */
			temp = clip->min_y;
			myclip.min_y = dest_bmp->height-1 - clip->max_y;
			myclip.max_y = dest_bmp->height-1 - temp;
			clip = &myclip;
		}
	}


	/* ASG 980209 -- added 16-bit version */
	if (dest_bmp->depth != 16)
	{
		int sprite_screen_height = (scaley*source_bmp->height+0x8000)>>16;
		int sprite_screen_width = (scalex*source_bmp->width+0x8000)>>16;

		/* compute sprite increment per screen pixel */
		int dx = (source_bmp->width<<16)/sprite_screen_width;
		int dy = (source_bmp->height<<16)/sprite_screen_height;

		int ex = sx+sprite_screen_width;
		int ey = sy+sprite_screen_height;

		int x_index_base;
		int y_index;

		if( flipx )
		{
			x_index_base = (sprite_screen_width-1)*dx;
			dx = -dx;
		}
		else
		{
			x_index_base = 0;
		}

		if( flipy )
		{
			y_index = (sprite_screen_height-1)*dy;
			dy = -dy;
		}
		else
		{
			y_index = 0;
		}

		if( clip )
		{
			if( sx < clip->min_x)
			{ /* clip left */
				int pixels = clip->min_x-sx;
				sx += pixels;
				x_index_base += pixels*dx;
			}
			if( sy < clip->min_y )
			{ /* clip top */
				int pixels = clip->min_y-sy;
				sy += pixels;
				y_index += pixels*dy;
			}
			/* NS 980211 - fixed incorrect clipping */
			if( ex > clip->max_x+1 )
			{ /* clip right */
				int pixels = ex-clip->max_x-1;
				ex -= pixels;
			}
			if( ey > clip->max_y+1 )
			{ /* clip bottom */
				int pixels = ey-clip->max_y-1;
				ey -= pixels;
			}
		}

		if( ex>sx )
		{ /* skip if inner loop doesn't draw anything */
			int y;

			switch (transparency)
			{
				case TRANSPARENCY_NONE:
					for( y=sy; y<ey; y++ )
					{
						unsigned char *source = source_bmp->line[(y_index>>16)];
						unsigned char *dest = dest_bmp->line[y];

						int x, x_index = x_index_base;
						for( x=sx; x<ex; x++ )
						{
							dest[x] = source[x_index>>16];
							x_index += dx;
						}

						y_index += dy;
					}
					break;

				case TRANSPARENCY_PEN:
				case TRANSPARENCY_COLOR:
					for( y=sy; y<ey; y++ )
					{
						unsigned char *source = source_bmp->line[(y_index>>16)];
						unsigned char *dest = dest_bmp->line[y];

						int x, x_index = x_index_base;
						for( x=sx; x<ex; x++ )
						{
							int c = source[x_index>>16];
							if( c != transparent_color ) dest[x] = c;
							x_index += dx;
						}

						y_index += dy;
					}
					break;

				case TRANSPARENCY_THROUGH:
usrintf_showmessage("copybitmapzoom() TRANSPARENCY_THROUGH");
					break;
			}
		}
	}

	/* ASG 980209 -- new 16-bit part */
	else
	{
		int sprite_screen_height = (scaley*source_bmp->height+0x8000)>>16;
		int sprite_screen_width = (scalex*source_bmp->width+0x8000)>>16;

		/* compute sprite increment per screen pixel */
		int dx = (source_bmp->width<<16)/sprite_screen_width;
		int dy = (source_bmp->height<<16)/sprite_screen_height;

		int ex = sx+sprite_screen_width;
		int ey = sy+sprite_screen_height;

		int x_index_base;
		int y_index;

		if( flipx )
		{
			x_index_base = (sprite_screen_width-1)*dx;
			dx = -dx;
		}
		else
		{
			x_index_base = 0;
		}

		if( flipy )
		{
			y_index = (sprite_screen_height-1)*dy;
			dy = -dy;
		}
		else
		{
			y_index = 0;
		}

		if( clip )
		{
			if( sx < clip->min_x)
			{ /* clip left */
				int pixels = clip->min_x-sx;
				sx += pixels;
				x_index_base += pixels*dx;
			}
			if( sy < clip->min_y )
			{ /* clip top */
				int pixels = clip->min_y-sy;
				sy += pixels;
				y_index += pixels*dy;
			}
			/* NS 980211 - fixed incorrect clipping */
			if( ex > clip->max_x+1 )
			{ /* clip right */
				int pixels = ex-clip->max_x-1;
				ex -= pixels;
			}
			if( ey > clip->max_y+1 )
			{ /* clip bottom */
				int pixels = ey-clip->max_y-1;
				ey -= pixels;
			}
		}

		if( ex>sx )
		{ /* skip if inner loop doesn't draw anything */
			int y;

			switch (transparency)
			{
				case TRANSPARENCY_NONE:
					for( y=sy; y<ey; y++ )
					{
						unsigned short *source = (unsigned short *)source_bmp->line[(y_index>>16)];
						unsigned short *dest = (unsigned short *)dest_bmp->line[y];

						int x, x_index = x_index_base;
						for( x=sx; x<ex; x++ )
						{
							dest[x] = source[x_index>>16];
							x_index += dx;
						}

						y_index += dy;
					}
					break;

				case TRANSPARENCY_PEN:
				case TRANSPARENCY_COLOR:
					for( y=sy; y<ey; y++ )
					{
						unsigned short *source = (unsigned short *)source_bmp->line[(y_index>>16)];
						unsigned short *dest = (unsigned short *)dest_bmp->line[y];

						int x, x_index = x_index_base;
						for( x=sx; x<ex; x++ )
						{
							int c = source[x_index>>16];
							if( c != transparent_color ) dest[x] = c;
							x_index += dx;
						}

						y_index += dy;
					}
					break;

				case TRANSPARENCY_THROUGH:
usrintf_showmessage("copybitmapzoom() TRANSPARENCY_THROUGH");
					break;
			}
		}
	}
}


/***************************************************************************

  Copy a bitmap onto another with scroll and wraparound.
  This function supports multiple independently scrolling rows/columns.
  "rows" is the number of indepentently scrolling rows. "rowscroll" is an
  array of integers telling how much to scroll each row. Same thing for
  "cols" and "colscroll".
  If the bitmap cannot scroll in one direction, set rows or columns to 0.
  If the bitmap scrolls as a whole, set rows and/or cols to 1.
  Bidirectional scrolling is, of course, supported only if the bitmap
  scrolls as a whole in at least one direction.

***************************************************************************/
void copyscrollbitmap(struct osd_bitmap *dest,struct osd_bitmap *src,
		int rows,const int *rowscroll,int cols,const int *colscroll,
		const struct rectangle *clip,int transparency,int transparent_color)
{
	int srcwidth,srcheight,destwidth,destheight;


	if (rows == 0 && cols == 0)
	{
		copybitmap(dest,src,0,0,0,0,clip,transparency,transparent_color);
		return;
	}

	if (Machine->orientation & ORIENTATION_SWAP_XY)
	{
		srcwidth = src->height;
		srcheight = src->width;
		destwidth = dest->height;
		destheight = dest->width;
	}
	else
	{
		srcwidth = src->width;
		srcheight = src->height;
		destwidth = dest->width;
		destheight = dest->height;
	}

	if (rows == 0)
	{
		/* scrolling columns */
		int col,colwidth;
		struct rectangle myclip;


		colwidth = srcwidth / cols;

		myclip.min_y = clip->min_y;
		myclip.max_y = clip->max_y;

		col = 0;
		while (col < cols)
		{
			int cons,scroll;


			/* count consecutive columns scrolled by the same amount */
			scroll = colscroll[col];
			cons = 1;
			while (col + cons < cols &&	colscroll[col + cons] == scroll)
				cons++;

			if (scroll < 0) scroll = srcheight - (-scroll) % srcheight;
			else scroll %= srcheight;

			myclip.min_x = col * colwidth;
			if (myclip.min_x < clip->min_x) myclip.min_x = clip->min_x;
			myclip.max_x = (col + cons) * colwidth - 1;
			if (myclip.max_x > clip->max_x) myclip.max_x = clip->max_x;

			copybitmap(dest,src,0,0,0,scroll,&myclip,transparency,transparent_color);
			copybitmap(dest,src,0,0,0,scroll - srcheight,&myclip,transparency,transparent_color);

			col += cons;
		}
	}
	else if (cols == 0)
	{
		/* scrolling rows */
		int row,rowheight;
		struct rectangle myclip;


		rowheight = srcheight / rows;

		myclip.min_x = clip->min_x;
		myclip.max_x = clip->max_x;

		row = 0;
		while (row < rows)
		{
			int cons,scroll;


			/* count consecutive rows scrolled by the same amount */
			scroll = rowscroll[row];
			cons = 1;
			while (row + cons < rows &&	rowscroll[row + cons] == scroll)
				cons++;

			if (scroll < 0) scroll = srcwidth - (-scroll) % srcwidth;
			else scroll %= srcwidth;

			myclip.min_y = row * rowheight;
			if (myclip.min_y < clip->min_y) myclip.min_y = clip->min_y;
			myclip.max_y = (row + cons) * rowheight - 1;
			if (myclip.max_y > clip->max_y) myclip.max_y = clip->max_y;

			copybitmap(dest,src,0,0,scroll,0,&myclip,transparency,transparent_color);
			copybitmap(dest,src,0,0,scroll - srcwidth,0,&myclip,transparency,transparent_color);

			row += cons;
		}
	}
	else if (rows == 1 && cols == 1)
	{
		/* XY scrolling playfield */
		int scrollx,scrolly,sx,sy;


		if (rowscroll[0] < 0) scrollx = srcwidth - (-rowscroll[0]) % srcwidth;
		else scrollx = rowscroll[0] % srcwidth;

		if (colscroll[0] < 0) scrolly = srcheight - (-colscroll[0]) % srcheight;
		else scrolly = colscroll[0] % srcheight;

		for (sx = scrollx - srcwidth;sx < destwidth;sx += srcwidth)
			for (sy = scrolly - srcheight;sy < destheight;sy += srcheight)
				copybitmap(dest,src,0,0,sx,sy,clip,transparency,transparent_color);
	}
	else if (rows == 1)
	{
		/* scrolling columns + horizontal scroll */
		int col,colwidth;
		int scrollx;
		struct rectangle myclip;


		if (rowscroll[0] < 0) scrollx = srcwidth - (-rowscroll[0]) % srcwidth;
		else scrollx = rowscroll[0] % srcwidth;

		colwidth = srcwidth / cols;

		myclip.min_y = clip->min_y;
		myclip.max_y = clip->max_y;

		col = 0;
		while (col < cols)
		{
			int cons,scroll;


			/* count consecutive columns scrolled by the same amount */
			scroll = colscroll[col];
			cons = 1;
			while (col + cons < cols &&	colscroll[col + cons] == scroll)
				cons++;

			if (scroll < 0) scroll = srcheight - (-scroll) % srcheight;
			else scroll %= srcheight;

			myclip.min_x = col * colwidth + scrollx;
			if (myclip.min_x < clip->min_x) myclip.min_x = clip->min_x;
			myclip.max_x = (col + cons) * colwidth - 1 + scrollx;
			if (myclip.max_x > clip->max_x) myclip.max_x = clip->max_x;

			copybitmap(dest,src,0,0,scrollx,scroll,&myclip,transparency,transparent_color);
			copybitmap(dest,src,0,0,scrollx,scroll - srcheight,&myclip,transparency,transparent_color);

			myclip.min_x = col * colwidth + scrollx - srcwidth;
			if (myclip.min_x < clip->min_x) myclip.min_x = clip->min_x;
			myclip.max_x = (col + cons) * colwidth - 1 + scrollx - srcwidth;
			if (myclip.max_x > clip->max_x) myclip.max_x = clip->max_x;

			copybitmap(dest,src,0,0,scrollx - srcwidth,scroll,&myclip,transparency,transparent_color);
			copybitmap(dest,src,0,0,scrollx - srcwidth,scroll - srcheight,&myclip,transparency,transparent_color);

			col += cons;
		}
	}
	else if (cols == 1)
	{
		/* scrolling rows + vertical scroll */
		int row,rowheight;
		int scrolly;
		struct rectangle myclip;


		if (colscroll[0] < 0) scrolly = srcheight - (-colscroll[0]) % srcheight;
		else scrolly = colscroll[0] % srcheight;

		rowheight = srcheight / rows;

		myclip.min_x = clip->min_x;
		myclip.max_x = clip->max_x;

		row = 0;
		while (row < rows)
		{
			int cons,scroll;


			/* count consecutive rows scrolled by the same amount */
			scroll = rowscroll[row];
			cons = 1;
			while (row + cons < rows &&	rowscroll[row + cons] == scroll)
				cons++;

			if (scroll < 0) scroll = srcwidth - (-scroll) % srcwidth;
			else scroll %= srcwidth;

			myclip.min_y = row * rowheight + scrolly;
			if (myclip.min_y < clip->min_y) myclip.min_y = clip->min_y;
			myclip.max_y = (row + cons) * rowheight - 1 + scrolly;
			if (myclip.max_y > clip->max_y) myclip.max_y = clip->max_y;

			copybitmap(dest,src,0,0,scroll,scrolly,&myclip,transparency,transparent_color);
			copybitmap(dest,src,0,0,scroll - srcwidth,scrolly,&myclip,transparency,transparent_color);

			myclip.min_y = row * rowheight + scrolly - srcheight;
			if (myclip.min_y < clip->min_y) myclip.min_y = clip->min_y;
			myclip.max_y = (row + cons) * rowheight - 1 + scrolly - srcheight;
			if (myclip.max_y > clip->max_y) myclip.max_y = clip->max_y;

			copybitmap(dest,src,0,0,scroll,scrolly - srcheight,&myclip,transparency,transparent_color);
			copybitmap(dest,src,0,0,scroll - srcwidth,scrolly - srcheight,&myclip,transparency,transparent_color);

			row += cons;
		}
	}
}


/* fill a bitmap using the specified pen */
void fillbitmap(struct osd_bitmap *dest,int pen,const struct rectangle *clip)
{
	int sx,sy,ex,ey,y;
	struct rectangle myclip;


	if (Machine->orientation & ORIENTATION_SWAP_XY)
	{
		if (clip)
		{
			myclip.min_x = clip->min_y;
			myclip.max_x = clip->max_y;
			myclip.min_y = clip->min_x;
			myclip.max_y = clip->max_x;
			clip = &myclip;
		}
	}
	if (Machine->orientation & ORIENTATION_FLIP_X)
	{
		if (clip)
		{
			int temp;


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
		if (clip)
		{
			int temp;


			myclip.min_x = clip->min_x;
			myclip.max_x = clip->max_x;
			temp = clip->min_y;
			myclip.min_y = dest->height-1 - clip->max_y;
			myclip.max_y = dest->height-1 - temp;
			clip = &myclip;
		}
	}


	sx = 0;
	ex = dest->width - 1;
	sy = 0;
	ey = dest->height - 1;

	if (clip && sx < clip->min_x) sx = clip->min_x;
	if (clip && ex > clip->max_x) ex = clip->max_x;
	if (sx > ex) return;
	if (clip && sy < clip->min_y) sy = clip->min_y;
	if (clip && ey > clip->max_y) ey = clip->max_y;
	if (sy > ey) return;

	osd_mark_dirty (sx,sy,ex,ey,0);	/* ASG 971011 */

	/* ASG 980211 */
	if (dest->depth == 16)
	{
		if ((pen >> 8) == (pen & 0xff))
		{
			for (y = sy;y <= ey;y++)
				memset(&dest->line[y][sx*2],pen&0xff,(ex-sx+1)*2);
		}
		else
		{
			unsigned short *sp = (unsigned short *)dest->line[sy];
			int x;

			for (x = sx;x <= ex;x++)
				sp[x] = pen;
			sp+=sx;
			for (y = sy+1;y <= ey;y++)
				memcpy(&dest->line[y][sx*2],sp,(ex-sx+1)*2);
		}
	}
	else
	{
		for (y = sy;y <= ey;y++)
			memset(&dest->line[y][sx],pen,ex-sx+1);
	}
}


void drawgfxzoom( struct osd_bitmap *dest_bmp,const struct GfxElement *gfx,
		unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
		const struct rectangle *clip,int transparency,int transparent_color,int scalex, int scaley)
{
	struct rectangle myclip;


	/* only support TRANSPARENCY_PEN and TRANSPARENCY_COLOR */
	if (transparency != TRANSPARENCY_PEN && transparency != TRANSPARENCY_COLOR)
		return;

	if (transparency == TRANSPARENCY_COLOR)
		transparent_color = Machine->pens[transparent_color];


	/*
	scalex and scaley are 16.16 fixed point numbers
	1<<15 : shrink to 50%
	1<<16 : uniform scale
	1<<17 : double to 200%
	*/


	if (Machine->orientation & ORIENTATION_SWAP_XY)
	{
		int temp;

		temp = sx;
		sx = sy;
		sy = temp;

		temp = flipx;
		flipx = flipy;
		flipy = temp;

		temp = scalex;
		scalex = scaley;
		scaley = temp;

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
		sx = dest_bmp->width - ((gfx->width * scalex + 0x7fff) >> 16) - sx;
		if (clip)
		{
			int temp;


			/* clip and myclip might be the same, so we need a temporary storage */
			temp = clip->min_x;
			myclip.min_x = dest_bmp->width-1 - clip->max_x;
			myclip.max_x = dest_bmp->width-1 - temp;
			myclip.min_y = clip->min_y;
			myclip.max_y = clip->max_y;
			clip = &myclip;
		}
#ifndef PREROTATE_GFX
		flipx = !flipx;
#endif
	}
	if (Machine->orientation & ORIENTATION_FLIP_Y)
	{
		sy = dest_bmp->height - ((gfx->height * scaley + 0x7fff) >> 16) - sy;
		if (clip)
		{
			int temp;


			myclip.min_x = clip->min_x;
			myclip.max_x = clip->max_x;
			/* clip and myclip might be the same, so we need a temporary storage */
			temp = clip->min_y;
			myclip.min_y = dest_bmp->height-1 - clip->max_y;
			myclip.max_y = dest_bmp->height-1 - temp;
			clip = &myclip;
		}
#ifndef PREROTATE_GFX
		flipy = !flipy;
#endif
	}

	/* KW 991012 -- Added code to force clip to bitmap boundary */
	if(clip)
	{
		myclip.min_x = clip->min_x;
		myclip.max_x = clip->max_x;
		myclip.min_y = clip->min_y;
		myclip.max_y = clip->max_y;

		if (myclip.min_x < 0) myclip.min_x = 0;
		if (myclip.max_x >= dest_bmp->width) myclip.max_x = dest_bmp->width-1;
		if (myclip.min_y < 0) myclip.min_y = 0;
		if (myclip.max_y >= dest_bmp->height) myclip.max_y = dest_bmp->height-1;

		clip=&myclip;
	}


	/* ASG 980209 -- added 16-bit version */
	if (dest_bmp->depth != 16)
	{
		if( gfx && gfx->colortable )
		{
			const unsigned short *pal = &gfx->colortable[gfx->color_granularity * (color % gfx->total_colors)]; /* ASG 980209 */
			int source_base = (code % gfx->total_elements) * gfx->height;

			int sprite_screen_height = (scaley*gfx->height+0x8000)>>16;
			int sprite_screen_width = (scalex*gfx->width+0x8000)>>16;

			/* compute sprite increment per screen pixel */
			int dx = (gfx->width<<16)/sprite_screen_width;
			int dy = (gfx->height<<16)/sprite_screen_height;

			int ex = sx+sprite_screen_width;
			int ey = sy+sprite_screen_height;

			int x_index_base;
			int y_index;

			if( flipx )
			{
				x_index_base = (sprite_screen_width-1)*dx;
				dx = -dx;
			}
			else
			{
				x_index_base = 0;
			}

			if( flipy )
			{
				y_index = (sprite_screen_height-1)*dy;
				dy = -dy;
			}
			else
			{
				y_index = 0;
			}

			if( clip )
			{
				if( sx < clip->min_x)
				{ /* clip left */
					int pixels = clip->min_x-sx;
					sx += pixels;
					x_index_base += pixels*dx;
				}
				if( sy < clip->min_y )
				{ /* clip top */
					int pixels = clip->min_y-sy;
					sy += pixels;
					y_index += pixels*dy;
				}
				/* NS 980211 - fixed incorrect clipping */
				if( ex > clip->max_x+1 )
				{ /* clip right */
					int pixels = ex-clip->max_x-1;
					ex -= pixels;
				}
				if( ey > clip->max_y+1 )
				{ /* clip bottom */
					int pixels = ey-clip->max_y-1;
					ey -= pixels;
				}
			}

			if( ex>sx )
			{ /* skip if inner loop doesn't draw anything */
				int y;

				/* case 1: TRANSPARENCY_PEN */
				if (transparency == TRANSPARENCY_PEN)
				{
					for( y=sy; y<ey; y++ )
					{
						unsigned char *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
						unsigned char *dest = dest_bmp->line[y];

						int x, x_index = x_index_base;
						for( x=sx; x<ex; x++ )
						{
							int c = source[x_index>>16];
							if( c != transparent_color ) dest[x] = pal[c];
							x_index += dx;
						}

						y_index += dy;
					}
				}

				/* case 2: TRANSPARENCY_COLOR */
				else if (transparency == TRANSPARENCY_COLOR)
				{
					for( y=sy; y<ey; y++ )
					{
						unsigned char *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
						unsigned char *dest = dest_bmp->line[y];

						int x, x_index = x_index_base;
						for( x=sx; x<ex; x++ )
						{
							int c = pal[source[x_index>>16]];
							if( c != transparent_color ) dest[x] = c;
							x_index += dx;
						}

						y_index += dy;
					}
				}
			}

		}
	}

	/* ASG 980209 -- new 16-bit part */
	else
	{
		if( gfx && gfx->colortable )
		{
			const unsigned short *pal = &gfx->colortable[gfx->color_granularity * (color % gfx->total_colors)]; /* ASG 980209 */
			int source_base = (code % gfx->total_elements) * gfx->height;

			int sprite_screen_height = (scaley*gfx->height+0x8000)>>16;
			int sprite_screen_width = (scalex*gfx->width+0x8000)>>16;

			/* compute sprite increment per screen pixel */
			int dx = (gfx->width<<16)/sprite_screen_width;
			int dy = (gfx->height<<16)/sprite_screen_height;

			int ex = sx+sprite_screen_width;
			int ey = sy+sprite_screen_height;

			int x_index_base;
			int y_index;

			if( flipx )
			{
				x_index_base = (sprite_screen_width-1)*dx;
				dx = -dx;
			}
			else
			{
				x_index_base = 0;
			}

			if( flipy )
			{
				y_index = (sprite_screen_height-1)*dy;
				dy = -dy;
			}
			else
			{
				y_index = 0;
			}

			if( clip )
			{
				if( sx < clip->min_x)
				{ /* clip left */
					int pixels = clip->min_x-sx;
					sx += pixels;
					x_index_base += pixels*dx;
				}
				if( sy < clip->min_y )
				{ /* clip top */
					int pixels = clip->min_y-sy;
					sy += pixels;
					y_index += pixels*dy;
				}
				/* NS 980211 - fixed incorrect clipping */
				if( ex > clip->max_x+1 )
				{ /* clip right */
					int pixels = ex-clip->max_x-1;
					ex -= pixels;
				}
				if( ey > clip->max_y+1 )
				{ /* clip bottom */
					int pixels = ey-clip->max_y-1;
					ey -= pixels;
				}
			}

			if( ex>sx )
			{ /* skip if inner loop doesn't draw anything */
				int y;

				/* case 1: TRANSPARENCY_PEN */
				if (transparency == TRANSPARENCY_PEN)
				{
					for( y=sy; y<ey; y++ )
					{
						unsigned char *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
						unsigned short *dest = (unsigned short *)dest_bmp->line[y];

						int x, x_index = x_index_base;
						for( x=sx; x<ex; x++ )
						{
							int c = source[x_index>>16];
							if( c != transparent_color ) dest[x] = pal[c];
							x_index += dx;
						}

						y_index += dy;
					}
				}

				/* case 2: TRANSPARENCY_COLOR */
				else if (transparency == TRANSPARENCY_COLOR)
				{
					for( y=sy; y<ey; y++ )
					{
						unsigned char *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
						unsigned short *dest = (unsigned short *)dest_bmp->line[y];

						int x, x_index = x_index_base;
						for( x=sx; x<ex; x++ )
						{
							int c = pal[source[x_index>>16]];
							if( c != transparent_color ) dest[x] = c;
							x_index += dx;
						}

						y_index += dy;
					}
				}
			}
		}
	}
}


void plot_pixel2(struct osd_bitmap *bitmap1,struct osd_bitmap *bitmap2,int x,int y,int pen)
{
	plot_pixel(bitmap1, x, y, pen);
	plot_pixel(bitmap2, x, y, pen);
}

static void pp_8_nd(struct osd_bitmap *b,int x,int y,int p)  { b->line[y][x] = p; }
static void pp_8_nd_fx(struct osd_bitmap *b,int x,int y,int p)  { b->line[y][b->width-1-x] = p; }
static void pp_8_nd_fy(struct osd_bitmap *b,int x,int y,int p)  { b->line[b->height-1-y][x] = p; }
static void pp_8_nd_fxy(struct osd_bitmap *b,int x,int y,int p)  { b->line[b->height-1-y][b->width-1-x] = p; }
static void pp_8_nd_s(struct osd_bitmap *b,int x,int y,int p)  { b->line[x][y] = p; }
static void pp_8_nd_fx_s(struct osd_bitmap *b,int x,int y,int p)  { b->line[x][b->width-1-y] = p; }
static void pp_8_nd_fy_s(struct osd_bitmap *b,int x,int y,int p)  { b->line[b->height-1-x][y] = p; }
static void pp_8_nd_fxy_s(struct osd_bitmap *b,int x,int y,int p)  { b->line[b->height-1-x][b->width-1-y] = p; }

static void pp_8_d(struct osd_bitmap *b,int x,int y,int p)  { b->line[y][x] = p; osd_mark_dirty (x,y,x,y,0); }
static void pp_8_d_fx(struct osd_bitmap *b,int x,int y,int p)  { int newx = b->width-1-x;  b->line[y][newx] = p; osd_mark_dirty (newx,y,newx,y,0); }
static void pp_8_d_fy(struct osd_bitmap *b,int x,int y,int p)  { int newy = b->height-1-y; b->line[newy][x] = p; osd_mark_dirty (x,newy,x,newy,0); }
static void pp_8_d_fxy(struct osd_bitmap *b,int x,int y,int p)  { int newx = b->width-1-x; int newy = b->height-1-y; b->line[newy][newx] = p; osd_mark_dirty (newx,newy,newx,newy,0); }
static void pp_8_d_s(struct osd_bitmap *b,int x,int y,int p)  { b->line[x][y] = p; osd_mark_dirty (y,x,y,x,0); }
static void pp_8_d_fx_s(struct osd_bitmap *b,int x,int y,int p)  { int newy = b->width-1-y; b->line[x][newy] = p; osd_mark_dirty (newy,x,newy,x,0); }
static void pp_8_d_fy_s(struct osd_bitmap *b,int x,int y,int p)  { int newx = b->height-1-x; b->line[newx][y] = p; osd_mark_dirty (y,newx,y,newx,0); }
static void pp_8_d_fxy_s(struct osd_bitmap *b,int x,int y,int p)  { int newx = b->height-1-x; int newy = b->width-1-y; b->line[newx][newy] = p; osd_mark_dirty (newy,newx,newy,newx,0); }

static void pp_16_nd(struct osd_bitmap *b,int x,int y,int p)  { ((unsigned short *)b->line[y])[x] = p; }
static void pp_16_nd_fx(struct osd_bitmap *b,int x,int y,int p)  { ((unsigned short *)b->line[y])[b->width-1-x] = p; }
static void pp_16_nd_fy(struct osd_bitmap *b,int x,int y,int p)  { ((unsigned short *)b->line[b->height-1-y])[x] = p; }
static void pp_16_nd_fxy(struct osd_bitmap *b,int x,int y,int p)  { ((unsigned short *)b->line[b->height-1-y])[b->width-1-x] = p; }
static void pp_16_nd_s(struct osd_bitmap *b,int x,int y,int p)  { ((unsigned short *)b->line[x])[y] = p; }
static void pp_16_nd_fx_s(struct osd_bitmap *b,int x,int y,int p)  { ((unsigned short *)b->line[x])[b->width-1-y] = p; }
static void pp_16_nd_fy_s(struct osd_bitmap *b,int x,int y,int p)  { ((unsigned short *)b->line[b->height-1-x])[y] = p; }
static void pp_16_nd_fxy_s(struct osd_bitmap *b,int x,int y,int p)  { ((unsigned short *)b->line[b->height-1-x])[b->width-1-y] = p; }

static void pp_16_d(struct osd_bitmap *b,int x,int y,int p)  { ((unsigned short *)b->line[y])[x] = p; osd_mark_dirty (x,y,x,y,0); }
static void pp_16_d_fx(struct osd_bitmap *b,int x,int y,int p)  { int newx = b->width-1-x;  ((unsigned short *)b->line[y])[newx] = p; osd_mark_dirty (newx,y,newx,y,0); }
static void pp_16_d_fy(struct osd_bitmap *b,int x,int y,int p)  { int newy = b->height-1-y; ((unsigned short *)b->line[newy])[x] = p; osd_mark_dirty (x,newy,x,newy,0); }
static void pp_16_d_fxy(struct osd_bitmap *b,int x,int y,int p)  { int newx = b->width-1-x; int newy = b->height-1-y; ((unsigned short *)b->line[newy])[newx] = p; osd_mark_dirty (newx,newy,newx,newy,0); }
static void pp_16_d_s(struct osd_bitmap *b,int x,int y,int p)  { ((unsigned short *)b->line[x])[y] = p; osd_mark_dirty (y,x,y,x,0); }
static void pp_16_d_fx_s(struct osd_bitmap *b,int x,int y,int p)  { int newy = b->width-1-y; ((unsigned short *)b->line[x])[newy] = p; osd_mark_dirty (newy,x,newy,x,0); }
static void pp_16_d_fy_s(struct osd_bitmap *b,int x,int y,int p)  { int newx = b->height-1-x; ((unsigned short *)b->line[newx])[y] = p; osd_mark_dirty (y,newx,y,newx,0); }
static void pp_16_d_fxy_s(struct osd_bitmap *b,int x,int y,int p)  { int newx = b->height-1-x; int newy = b->width-1-y; ((unsigned short *)b->line[newx])[newy] = p; osd_mark_dirty (newy,newx,newy,newx,0); }


static int rp_8(struct osd_bitmap *b,int x,int y)  { return b->line[y][x]; }
static int rp_8_fx(struct osd_bitmap *b,int x,int y)  { return b->line[y][b->width-1-x]; }
static int rp_8_fy(struct osd_bitmap *b,int x,int y)  { return b->line[b->height-1-y][x]; }
static int rp_8_fxy(struct osd_bitmap *b,int x,int y)  { return b->line[b->height-1-y][b->width-1-x]; }
static int rp_8_s(struct osd_bitmap *b,int x,int y)  { return b->line[x][y]; }
static int rp_8_fx_s(struct osd_bitmap *b,int x,int y)  { return b->line[x][b->width-1-y]; }
static int rp_8_fy_s(struct osd_bitmap *b,int x,int y)  { return b->line[b->height-1-x][y]; }
static int rp_8_fxy_s(struct osd_bitmap *b,int x,int y)  { return b->line[b->height-1-x][b->width-1-y]; }

static int rp_16(struct osd_bitmap *b,int x,int y)  { return ((unsigned short *)b->line[y])[x]; }
static int rp_16_fx(struct osd_bitmap *b,int x,int y)  { return ((unsigned short *)b->line[y])[b->width-1-x]; }
static int rp_16_fy(struct osd_bitmap *b,int x,int y)  { return ((unsigned short *)b->line[b->height-1-y])[x]; }
static int rp_16_fxy(struct osd_bitmap *b,int x,int y)  { return ((unsigned short *)b->line[b->height-1-y])[b->width-1-x]; }
static int rp_16_s(struct osd_bitmap *b,int x,int y)  { return ((unsigned short *)b->line[x])[y]; }
static int rp_16_fx_s(struct osd_bitmap *b,int x,int y)  { return ((unsigned short *)b->line[x])[b->width-1-y]; }
static int rp_16_fy_s(struct osd_bitmap *b,int x,int y)  { return ((unsigned short *)b->line[b->height-1-x])[y]; }
static int rp_16_fxy_s(struct osd_bitmap *b,int x,int y)  { return ((unsigned short *)b->line[b->height-1-x])[b->width-1-y]; }


static plot_pixel_proc pps_8_nd[] =
		{ pp_8_nd, 	 pp_8_nd_fx,   pp_8_nd_fy, 	 pp_8_nd_fxy,
		  pp_8_nd_s, pp_8_nd_fx_s, pp_8_nd_fy_s, pp_8_nd_fxy_s };

static plot_pixel_proc pps_8_d[] =
		{ pp_8_d, 	pp_8_d_fx,   pp_8_d_fy,	  pp_8_d_fxy,
		  pp_8_d_s, pp_8_d_fx_s, pp_8_d_fy_s, pp_8_d_fxy_s };

static plot_pixel_proc pps_16_nd[] =
		{ pp_16_nd,   pp_16_nd_fx,   pp_16_nd_fy, 	pp_16_nd_fxy,
		  pp_16_nd_s, pp_16_nd_fx_s, pp_16_nd_fy_s, pp_16_nd_fxy_s };

static plot_pixel_proc pps_16_d[] =
		{ pp_16_d,   pp_16_d_fx,   pp_16_d_fy, 	 pp_16_d_fxy,
		  pp_16_d_s, pp_16_d_fx_s, pp_16_d_fy_s, pp_16_d_fxy_s };


static read_pixel_proc rps_8[] =
		{ rp_8,	  rp_8_fx,   rp_8_fy,	rp_8_fxy,
		  rp_8_s, rp_8_fx_s, rp_8_fy_s, rp_8_fxy_s };

static read_pixel_proc rps_16[] =
		{ rp_16,   rp_16_fx,   rp_16_fy,   rp_16_fxy,
		  rp_16_s, rp_16_fx_s, rp_16_fy_s, rp_16_fxy_s };


void set_pixel_functions(void)
{
	if (Machine->color_depth == 8)
	{
		read_pixel = rps_8[Machine->orientation];

		if (Machine->drv->video_attributes & VIDEO_SUPPORTS_DIRTY)
			plot_pixel = pps_8_d[Machine->orientation];
		else
			plot_pixel = pps_8_nd[Machine->orientation];
	}
	else
	{
		read_pixel = rps_16[Machine->orientation];

		if (Machine->drv->video_attributes & VIDEO_SUPPORTS_DIRTY)
			plot_pixel = pps_16_d[Machine->orientation];
		else
			plot_pixel = pps_16_nd[Machine->orientation];
	}
}

#else /* DECLARE */

/* -------------------- included inline section --------------------- */

/* don't put this file in the makefile, it is #included by common.c to */
/* generate 8-bit and 16-bit versions                                  */

DECLARE(blockmove_opaque,(
		const UINT8 *srcdata,int srcwidth,int srcheight,int srcmodulo,
		DATA_TYPE *dstdata,int dstmodulo,
		const unsigned short *paldata),
{
	DATA_TYPE *end;

	srcmodulo -= srcwidth;
	dstmodulo -= srcwidth;

	while (srcheight)
	{
		end = dstdata + srcwidth;
		while (dstdata <= end - 8)
		{
			dstdata[0] = paldata[srcdata[0]];
			dstdata[1] = paldata[srcdata[1]];
			dstdata[2] = paldata[srcdata[2]];
			dstdata[3] = paldata[srcdata[3]];
			dstdata[4] = paldata[srcdata[4]];
			dstdata[5] = paldata[srcdata[5]];
			dstdata[6] = paldata[srcdata[6]];
			dstdata[7] = paldata[srcdata[7]];
			dstdata += 8;
			srcdata += 8;
		}
		while (dstdata < end)
			*(dstdata++) = paldata[*(srcdata++)];

		srcdata += srcmodulo;
		dstdata += dstmodulo;
		srcheight--;
	}
})

DECLARE(blockmove_opaque_flipx,(
		const UINT8 *srcdata,int srcwidth,int srcheight,int srcmodulo,
		DATA_TYPE *dstdata,int dstmodulo,
		const unsigned short *paldata),
{
	DATA_TYPE *end;

	srcmodulo += srcwidth;
	dstmodulo -= srcwidth;
	//srcdata += srcwidth-1;

	while (srcheight)
	{
		end = dstdata + srcwidth;
		while (dstdata <= end - 8)
		{
			srcdata -= 8;
			dstdata[0] = paldata[srcdata[8]];
			dstdata[1] = paldata[srcdata[7]];
			dstdata[2] = paldata[srcdata[6]];
			dstdata[3] = paldata[srcdata[5]];
			dstdata[4] = paldata[srcdata[4]];
			dstdata[5] = paldata[srcdata[3]];
			dstdata[6] = paldata[srcdata[2]];
			dstdata[7] = paldata[srcdata[1]];
			dstdata += 8;
		}
		while (dstdata < end)
			*(dstdata++) = paldata[*(srcdata--)];

		srcdata += srcmodulo;
		dstdata += dstmodulo;
		srcheight--;
	}
})


DECLARE(blockmove_transpen,(
		const UINT8 *srcdata,int srcwidth,int srcheight,int srcmodulo,
		DATA_TYPE *dstdata,int dstmodulo,
		const unsigned short *paldata,int transpen),
{
	DATA_TYPE *end;
	int trans4;
	UINT32 *sd4;

	srcmodulo -= srcwidth;
	dstmodulo -= srcwidth;

	trans4 = transpen * 0x01010101;

	while (srcheight)
	{
		end = dstdata + srcwidth;
		while (((long)srcdata & 3) && dstdata < end)	/* longword align */
		{
			int col;

			col = *(srcdata++);
			if (col != transpen) *dstdata = paldata[col];
			dstdata++;
		}
		sd4 = (UINT32 *)srcdata;
		while (dstdata <= end - 4)
		{
			UINT32 col4;

			if ((col4 = *(sd4++)) != trans4)
			{
				UINT32 xod4;

				xod4 = col4 ^ trans4;
				if (xod4 & 0x000000ff) dstdata[BL0] = paldata[(col4) & 0xff];
				if (xod4 & 0x0000ff00) dstdata[BL1] = paldata[(col4 >>  8) & 0xff];
				if (xod4 & 0x00ff0000) dstdata[BL2] = paldata[(col4 >> 16) & 0xff];
				if (xod4 & 0xff000000) dstdata[BL3] = paldata[col4 >> 24];
			}
			dstdata += 4;
		}
		srcdata = (unsigned char *)sd4;
		while (dstdata < end)
		{
			int col;

			col = *(srcdata++);
			if (col != transpen) *dstdata = paldata[col];
			dstdata++;
		}

		srcdata += srcmodulo;
		dstdata += dstmodulo;
		srcheight--;
	}
})

DECLARE(blockmove_transpen_flipx,(
		const UINT8 *srcdata,int srcwidth,int srcheight,int srcmodulo,
		DATA_TYPE *dstdata,int dstmodulo,
		const unsigned short *paldata,int transpen),
{
	DATA_TYPE *end;
	int trans4;
	UINT32 *sd4;

	srcmodulo += srcwidth;
	dstmodulo -= srcwidth;
	//srcdata += srcwidth-1;
	srcdata -= 3;

	trans4 = transpen * 0x01010101;

	while (srcheight)
	{
		end = dstdata + srcwidth;
		while (((long)srcdata & 3) && dstdata < end)	/* longword align */
		{
			int col;

			col = srcdata[3];
			srcdata--;
			if (col != transpen) *dstdata = paldata[col];
			dstdata++;
		}
		sd4 = (UINT32 *)srcdata;
		while (dstdata <= end - 4)
		{
			UINT32 col4;

			if ((col4 = *(sd4--)) != trans4)
			{
				UINT32 xod4;

				xod4 = col4 ^ trans4;
				if (xod4 & 0xff000000) dstdata[BL0] = paldata[col4 >> 24];
				if (xod4 & 0x00ff0000) dstdata[BL1] = paldata[(col4 >> 16) & 0xff];
				if (xod4 & 0x0000ff00) dstdata[BL2] = paldata[(col4 >>  8) & 0xff];
				if (xod4 & 0x000000ff) dstdata[BL3] = paldata[col4 & 0xff];
			}
			dstdata += 4;
		}
		srcdata = (unsigned char *)sd4;
		while (dstdata < end)
		{
			int col;

			col = srcdata[3];
			srcdata--;
			if (col != transpen) *dstdata = paldata[col];
			dstdata++;
		}

		srcdata += srcmodulo;
		dstdata += dstmodulo;
		srcheight--;
	}
})


#define PEN_IS_OPAQUE ((1<<col)&transmask) == 0

DECLARE(blockmove_transmask,(
		const UINT8 *srcdata,int srcwidth,int srcheight,int srcmodulo,
		DATA_TYPE *dstdata,int dstmodulo,
		const unsigned short *paldata,int transmask),
{
	DATA_TYPE *end;
	UINT32 *sd4;

	srcmodulo -= srcwidth;
	dstmodulo -= srcwidth;

	while (srcheight)
	{
		end = dstdata + srcwidth;
		while (((long)srcdata & 3) && dstdata < end)	/* longword align */
		{
			int col;

			col = *(srcdata++);
			if (PEN_IS_OPAQUE) *dstdata = paldata[col];
			dstdata++;
		}
		sd4 = (UINT32 *)srcdata;
		while (dstdata <= end - 4)
		{
			int col;
			UINT32 col4;

			col4 = *(sd4++);
			col = (col4 >>  0) & 0xff;
			if (PEN_IS_OPAQUE) dstdata[BL0] = paldata[col];
			col = (col4 >>  8) & 0xff;
			if (PEN_IS_OPAQUE) dstdata[BL1] = paldata[col];
			col = (col4 >> 16) & 0xff;
			if (PEN_IS_OPAQUE) dstdata[BL2] = paldata[col];
			col = (col4 >> 24) & 0xff;
			if (PEN_IS_OPAQUE) dstdata[BL3] = paldata[col];
			dstdata += 4;
		}
		srcdata = (unsigned char *)sd4;
		while (dstdata < end)
		{
			int col;

			col = *(srcdata++);
			if (PEN_IS_OPAQUE) *dstdata = paldata[col];
			dstdata++;
		}

		srcdata += srcmodulo;
		dstdata += dstmodulo;
		srcheight--;
	}
})

DECLARE(blockmove_transmask_flipx,(
		const UINT8 *srcdata,int srcwidth,int srcheight,int srcmodulo,
		DATA_TYPE *dstdata,int dstmodulo,
		const unsigned short *paldata,int transmask),
{
	DATA_TYPE *end;
	UINT32 *sd4;

	srcmodulo += srcwidth;
	dstmodulo -= srcwidth;
	//srcdata += srcwidth-1;
	srcdata -= 3;

	while (srcheight)
	{
		end = dstdata + srcwidth;
		while (((long)srcdata & 3) && dstdata < end)	/* longword align */
		{
			int col;

			col = srcdata[3];
			srcdata--;
			if (PEN_IS_OPAQUE) *dstdata = paldata[col];
			dstdata++;
		}
		sd4 = (UINT32 *)srcdata;
		while (dstdata <= end - 4)
		{
			int col;
			UINT32 col4;

			col4 = *(sd4--);
			col = (col4 >> 24) & 0xff;
			if (PEN_IS_OPAQUE) dstdata[BL0] = paldata[col];
			col = (col4 >> 16) & 0xff;
			if (PEN_IS_OPAQUE) dstdata[BL1] = paldata[col];
			col = (col4 >>  8) & 0xff;
			if (PEN_IS_OPAQUE) dstdata[BL2] = paldata[col];
			col = (col4 >>  0) & 0xff;
			if (PEN_IS_OPAQUE) dstdata[BL3] = paldata[col];
			dstdata += 4;
		}
		srcdata = (unsigned char *)sd4;
		while (dstdata < end)
		{
			int col;

			col = srcdata[3];
			srcdata--;
			if (PEN_IS_OPAQUE) *dstdata = paldata[col];
			dstdata++;
		}

		srcdata += srcmodulo;
		dstdata += dstmodulo;
		srcheight--;
	}
})


DECLARE(blockmove_transcolor,(
		const UINT8 *srcdata,int srcwidth,int srcheight,int srcmodulo,
		DATA_TYPE *dstdata,int dstmodulo,
		const unsigned short *paldata,int transcolor),
{
	DATA_TYPE *end;
	const unsigned short *lookupdata = Machine->game_colortable + (paldata - Machine->remapped_colortable);

	srcmodulo -= srcwidth;
	dstmodulo -= srcwidth;

	while (srcheight)
	{
		end = dstdata + srcwidth;
		while (dstdata < end)
		{
			if (lookupdata[*srcdata] != transcolor) *dstdata = paldata[*srcdata];
			srcdata++;
			dstdata++;
		}

		srcdata += srcmodulo;
		dstdata += dstmodulo;
		srcheight--;
	}
})

DECLARE(blockmove_transcolor_flipx,(
		const UINT8 *srcdata,int srcwidth,int srcheight,int srcmodulo,
		DATA_TYPE *dstdata,int dstmodulo,
		const unsigned short *paldata,int transcolor),
{
	DATA_TYPE *end;
	const unsigned short *lookupdata = Machine->game_colortable + (paldata - Machine->remapped_colortable);

	srcmodulo += srcwidth;
	dstmodulo -= srcwidth;
	//srcdata += srcwidth-1;

	while (srcheight)
	{
		end = dstdata + srcwidth;
		while (dstdata < end)
		{
			if (lookupdata[*srcdata] != transcolor) *dstdata = paldata[*srcdata];
			srcdata--;
			dstdata++;
		}

		srcdata += srcmodulo;
		dstdata += dstmodulo;
		srcheight--;
	}
})


DECLARE(blockmove_transthrough,(
		const UINT8 *srcdata,int srcwidth,int srcheight,int srcmodulo,
		DATA_TYPE *dstdata,int dstmodulo,
		const unsigned short *paldata,int transcolor),
{
	DATA_TYPE *end;

	srcmodulo -= srcwidth;
	dstmodulo -= srcwidth;

	while (srcheight)
	{
		end = dstdata + srcwidth;
		while (dstdata < end)
		{
			if (*dstdata == transcolor) *dstdata = paldata[*srcdata];
			srcdata++;
			dstdata++;
		}

		srcdata += srcmodulo;
		dstdata += dstmodulo;
		srcheight--;
	}
})

DECLARE(blockmove_transthrough_flipx,(
		const UINT8 *srcdata,int srcwidth,int srcheight,int srcmodulo,
		DATA_TYPE *dstdata,int dstmodulo,
		const unsigned short *paldata,int transcolor),
{
	DATA_TYPE *end;

	srcmodulo += srcwidth;
	dstmodulo -= srcwidth;
	//srcdata += srcwidth-1;

	while (srcheight)
	{
		end = dstdata + srcwidth;
		while (dstdata < end)
		{
			if (*dstdata == transcolor) *dstdata = paldata[*srcdata];
			srcdata--;
			dstdata++;
		}

		srcdata += srcmodulo;
		dstdata += dstmodulo;
		srcheight--;
	}
})

DECLARE(blockmove_pen_table,(
		const UINT8 *srcdata,int srcwidth,int srcheight,int srcmodulo,
		DATA_TYPE *dstdata,int dstmodulo,
		const unsigned short *paldata,int transcolor),
{
	DATA_TYPE *end;

	srcmodulo -= srcwidth;
	dstmodulo -= srcwidth;

	while (srcheight)
	{
		end = dstdata + srcwidth;
		while (dstdata < end)
		{
			int col;

			col = *(srcdata++);
			if (col != transcolor)
			{
				switch(gfx_drawmode_table[col])
				{
				case DRAWMODE_SOURCE:
					*dstdata = paldata[col];
					break;
				case DRAWMODE_SHADOW:
					*dstdata = palette_shadow_table[*dstdata];
					break;
				case DRAWMODE_HIGHLIGHT:
					*dstdata = palette_highlight_table[*dstdata];
					break;
				}
			}
			dstdata++;
		}

		srcdata += srcmodulo;
		dstdata += dstmodulo;
		srcheight--;
	}
})

DECLARE(blockmove_pen_table_flipx,(
		const UINT8 *srcdata,int srcwidth,int srcheight,int srcmodulo,
		DATA_TYPE *dstdata,int dstmodulo,
		const unsigned short *paldata,int transcolor),
{
	DATA_TYPE *end;

	srcmodulo += srcwidth;
	dstmodulo -= srcwidth;
	//srcdata += srcwidth-1;

	while (srcheight)
	{
		end = dstdata + srcwidth;
		while (dstdata < end)
		{
			int col;

			col = *(srcdata--);
			if (col != transcolor)
			{
				switch(gfx_drawmode_table[col])
				{
				case DRAWMODE_SOURCE:
					*dstdata = paldata[col];
					break;
				case DRAWMODE_SHADOW:
					*dstdata = palette_shadow_table[*dstdata];
					break;
				case DRAWMODE_HIGHLIGHT:
					*dstdata = palette_highlight_table[*dstdata];
					break;
				}
			}
			dstdata++;
		}

		srcdata += srcmodulo;
		dstdata += dstmodulo;
		srcheight--;
	}
})

DECLARE(blockmove_opaque_noremap,(
		const DATA_TYPE *srcdata,int srcwidth,int srcheight,int srcmodulo,
		DATA_TYPE *dstdata,int dstmodulo),
{
	while (srcheight)
	{
		memcpy(dstdata,srcdata,srcwidth * sizeof(DATA_TYPE));
		srcdata += srcmodulo;
		dstdata += dstmodulo;
		srcheight--;
	}
})

DECLARE(blockmove_opaque_noremap_flipx,(
		const DATA_TYPE *srcdata,int srcwidth,int srcheight,int srcmodulo,
		DATA_TYPE *dstdata,int dstmodulo),
{
	DATA_TYPE *end;

	srcmodulo += srcwidth;
	dstmodulo -= srcwidth;
	//srcdata += srcwidth-1;

	while (srcheight)
	{
		end = dstdata + srcwidth;
		while (dstdata <= end - 8)
		{
			srcdata -= 8;
			dstdata[0] = srcdata[8];
			dstdata[1] = srcdata[7];
			dstdata[2] = srcdata[6];
			dstdata[3] = srcdata[5];
			dstdata[4] = srcdata[4];
			dstdata[5] = srcdata[3];
			dstdata[6] = srcdata[2];
			dstdata[7] = srcdata[1];
			dstdata += 8;
		}
		while (dstdata < end)
			*(dstdata++) = *(srcdata--);

		srcdata += srcmodulo;
		dstdata += dstmodulo;
		srcheight--;
	}
})


DECLARE(blockmove_transthrough_noremap,(
		const DATA_TYPE *srcdata,int srcwidth,int srcheight,int srcmodulo,
		DATA_TYPE *dstdata,int dstmodulo,
		int transcolor),
{
	DATA_TYPE *end;

	srcmodulo -= srcwidth;
	dstmodulo -= srcwidth;

	while (srcheight)
	{
		end = dstdata + srcwidth;
		while (dstdata < end)
		{
			if (*dstdata == transcolor) *dstdata = *srcdata;
			srcdata++;
			dstdata++;
		}

		srcdata += srcmodulo;
		dstdata += dstmodulo;
		srcheight--;
	}
})

DECLARE(blockmove_transthrough_noremap_flipx,(
		const DATA_TYPE *srcdata,int srcwidth,int srcheight,int srcmodulo,
		DATA_TYPE *dstdata,int dstmodulo,
		int transcolor),
{
	DATA_TYPE *end;

	srcmodulo += srcwidth;
	dstmodulo -= srcwidth;
	//srcdata += srcwidth-1;

	while (srcheight)
	{
		end = dstdata + srcwidth;
		while (dstdata < end)
		{
			if (*dstdata == transcolor) *dstdata = *srcdata;
			srcdata--;
			dstdata++;
		}

		srcdata += srcmodulo;
		dstdata += dstmodulo;
		srcheight--;
	}
})

DECLARE(drawgfx_core,(
		struct osd_bitmap *dest,const struct GfxElement *gfx,
		unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
		const struct rectangle *clip,int transparency,int transparent_color),
{
	int ox;
	int oy;
	int ex;
	int ey;


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

	{
		UINT8 *sd = gfx->gfxdata + code * gfx->char_modulo;		/* source data */
		int sw = ex-sx+1;										/* source width */
		int sh = ey-sy+1;										/* source height */
		int sm = gfx->line_modulo;								/* source modulo */
		DATA_TYPE *dd = ((DATA_TYPE *)dest->line[sy]) + sx;		/* dest data */
		int dm = ((DATA_TYPE *)dest->line[1])-((DATA_TYPE *)dest->line[0]);	/* dest modulo */
		const unsigned short *paldata = &gfx->colortable[gfx->color_granularity * color];

		if (flipx)
		{
			//if ((sx-ox) == 0) sd += gfx->width - sw;
			sd += gfx->width -1 -(sx-ox);
		}
		else
			sd += (sx-ox);

		if (flipy)
		{
			//if ((sy-oy) == 0) sd += sm * (gfx->height - sh);
			//dd += dm * (sh - 1);
			//dm = -dm;
			sd += sm * (gfx->height -1 -(sy-oy));
			sm = -sm;
		}
		else
			sd += sm * (sy-oy);

		switch (transparency)
		{
			case TRANSPARENCY_NONE:
				BLOCKMOVE(opaque,flipx,(sd,sw,sh,sm,dd,dm,paldata));
				break;

			case TRANSPARENCY_PEN:
				BLOCKMOVE(transpen,flipx,(sd,sw,sh,sm,dd,dm,paldata,transparent_color));
				break;

			case TRANSPARENCY_PENS:
				BLOCKMOVE(transmask,flipx,(sd,sw,sh,sm,dd,dm,paldata,transparent_color));
				break;

			case TRANSPARENCY_COLOR:
				BLOCKMOVE(transcolor,flipx,(sd,sw,sh,sm,dd,dm,paldata,transparent_color));
				break;

			case TRANSPARENCY_THROUGH:
				BLOCKMOVE(transthrough,flipx,(sd,sw,sh,sm,dd,dm,paldata,transparent_color));
				break;

			case TRANSPARENCY_PEN_TABLE:
				BLOCKMOVE(pen_table,flipx,(sd,sw,sh,sm,dd,dm,paldata,transparent_color));
				break;
		}
	}
})

DECLARE(copybitmap_core,(
		struct osd_bitmap *dest,struct osd_bitmap *src,
		int flipx,int flipy,int sx,int sy,
		const struct rectangle *clip,int transparency,int transparent_color),
{
	int ox;
	int oy;
	int ex;
	int ey;


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

	{
		DATA_TYPE *sd = ((DATA_TYPE *)src->line[0]);							/* source data */
		int sw = ex-sx+1;														/* source width */
		int sh = ey-sy+1;														/* source height */
		int sm = ((DATA_TYPE *)src->line[1])-((DATA_TYPE *)src->line[0]);		/* source modulo */
		DATA_TYPE *dd = ((DATA_TYPE *)dest->line[sy]) + sx;						/* dest data */
		int dm = ((DATA_TYPE *)dest->line[1])-((DATA_TYPE *)dest->line[0]);		/* dest modulo */

		if (flipx)
		{
			//if ((sx-ox) == 0) sd += gfx->width - sw;
			sd += src->width -1 -(sx-ox);
		}
		else
			sd += (sx-ox);

		if (flipy)
		{
			//if ((sy-oy) == 0) sd += sm * (gfx->height - sh);
			//dd += dm * (sh - 1);
			//dm = -dm;
			sd += sm * (src->height -1 -(sy-oy));
			sm = -sm;
		}
		else
			sd += sm * (sy-oy);

		switch (transparency)
		{
			case TRANSPARENCY_NONE:
				BLOCKMOVE(opaque_noremap,flipx,(sd,sw,sh,sm,dd,dm));
				break;

			case TRANSPARENCY_PEN:
			case TRANSPARENCY_COLOR:
				BLOCKMOVE(transpen_noremap,flipx,(sd,sw,sh,sm,dd,dm,transparent_color));
				break;

			case TRANSPARENCY_THROUGH:
				BLOCKMOVE(transthrough_noremap,flipx,(sd,sw,sh,sm,dd,dm,transparent_color));
				break;
		}
	}
})

#endif /* DECLARE */
