
package mame;
import static arcadeflex.libc.*;
import static mame.osdependH.*;
import static mame.drawgfxH.*;
import static mame.mame.*;
import static mame.driverH.*;

public class drawgfx {

/*TODO*///#define BL0 0
/*TODO*///#define BL1 1
/*TODO*///#define BL2 2
/*TODO*///#define BL3 3
/*TODO*///#define WL0 0
/*TODO*///#define WL1 1
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///UINT8 gfx_drawmode_table[256];
    public static plot_pixel_procPtr plot_pixel;
    public static read_pixel_procPtr read_pixel;
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///#define read_dword(address) *(int *)address
/*TODO*///#define write_dword(address,data) *(int *)address=data
/*TODO*///
/*TODO*///
    private static int readbit(CharPtr src, int bitnum) {
         return (src.read(bitnum / 8) >> (7 - bitnum % 8)) & 1;
    }
/*TODO*///
/*TODO*///
    public static void decodechar(GfxElement gfx, int num, CharPtr src, GfxLayout gl) 
    {
	int plane,x,y;
	char dp[];
	int offs;

	offs = num * gl.charincrement;
        
        throw new UnsupportedOperationException("Unsupported decodechar");
/*TODO*///	dp = gfx->gfxdata + num * gfx->char_modulo;
/*TODO*///	for (y = 0;y < gfx->height;y++)
/*TODO*///	{
/*TODO*///		int yoffs;
/*TODO*///
/*TODO*///		yoffs = y;
/*TODO*///		for (x = 0;x < gfx->width;x++)
/*TODO*///		{
/*TODO*///			int xoffs;
/*TODO*///
/*TODO*///			xoffs = x;
/*TODO*///
/*TODO*///			dp[x] = 0;
/*TODO*///			if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///			{
/*TODO*///				for (plane = 0;plane < gl->planes;plane++)
/*TODO*///				{
/*TODO*///					if (readbit(src,offs + gl->planeoffset[plane] + gl->yoffset[xoffs] + gl->xoffset[yoffs]))
/*TODO*///						dp[x] |= (1 << (gl->planes-1-plane));
/*TODO*///				}
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				for (plane = 0;plane < gl->planes;plane++)
/*TODO*///				{
/*TODO*///					if (readbit(src,offs + gl->planeoffset[plane] + gl->yoffset[yoffs] + gl->xoffset[xoffs]))
/*TODO*///						dp[x] |= (1 << (gl->planes-1-plane));
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///		dp += gfx->line_modulo;
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	if (gfx->pen_usage)
/*TODO*///	{
/*TODO*///		/* fill the pen_usage array with info on the used pens */
/*TODO*///		gfx->pen_usage[num] = 0;
/*TODO*///
/*TODO*///		dp = gfx->gfxdata + num * gfx->char_modulo;
/*TODO*///		for (y = 0;y < gfx->height;y++)
/*TODO*///		{
/*TODO*///			for (x = 0;x < gfx->width;x++)
/*TODO*///			{
/*TODO*///				gfx->pen_usage[num] |= 1 << dp[x];
/*TODO*///			}
/*TODO*///			dp += gfx->line_modulo;
/*TODO*///		}
/*TODO*///	}
    }
/*TODO*///
/*TODO*///
/*TODO*///struct GfxElement *decodegfx(const unsigned char *src,const struct GfxLayout *gl)
/*TODO*///{
/*TODO*///	int c;
/*TODO*///	struct GfxElement *gfx;
/*TODO*///
/*TODO*///
/*TODO*///	if ((gfx = malloc(sizeof(struct GfxElement))) == 0)
/*TODO*///		return 0;
/*TODO*///	memset(gfx,0,sizeof(struct GfxElement));
/*TODO*///
/*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///	{
/*TODO*///		gfx->width = gl->height;
/*TODO*///		gfx->height = gl->width;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		gfx->width = gl->width;
/*TODO*///		gfx->height = gl->height;
/*TODO*///	}
/*TODO*///
/*TODO*///	gfx->line_modulo = gfx->width;
/*TODO*///	gfx->char_modulo = gfx->line_modulo * gfx->height;
/*TODO*///	if ((gfx->gfxdata = malloc(gl->total * gfx->char_modulo * sizeof(unsigned char))) == 0)
/*TODO*///	{
/*TODO*///		free(gfx);
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	gfx->total_elements = gl->total;
/*TODO*///	gfx->color_granularity = 1 << gl->planes;
/*TODO*///
/*TODO*///	gfx->pen_usage = 0; /* need to make sure this is NULL if the next test fails) */
/*TODO*///	if (gfx->color_granularity <= 32)	/* can't handle more than 32 pens */
/*TODO*///		gfx->pen_usage = malloc(gfx->total_elements * sizeof(int));
/*TODO*///		/* no need to check for failure, the code can work without pen_usage */
/*TODO*///
/*TODO*///	for (c = 0;c < gl->total;c++)
/*TODO*///		decodechar(gfx,c,src,gl);
/*TODO*///
/*TODO*///	return gfx;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void freegfx(struct GfxElement *gfx)
/*TODO*///{
/*TODO*///	if (gfx)
/*TODO*///	{
/*TODO*///		free(gfx->pen_usage);
/*TODO*///		free(gfx->gfxdata);
/*TODO*///		free(gfx);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///INLINE void blockmove_transpen_noremap8(
/*TODO*///		const UINT8 *srcdata,int srcwidth,int srcheight,int srcmodulo,
/*TODO*///		UINT8 *dstdata,int dstmodulo,
/*TODO*///		int transpen)
/*TODO*///{
/*TODO*///	UINT8 *end;
/*TODO*///	int trans4;
/*TODO*///	UINT32 *sd4;
/*TODO*///
/*TODO*///	srcmodulo -= srcwidth;
/*TODO*///	dstmodulo -= srcwidth;
/*TODO*///
/*TODO*///	trans4 = transpen * 0x01010101;
/*TODO*///
/*TODO*///	while (srcheight)
/*TODO*///	{
/*TODO*///		end = dstdata + srcwidth;
/*TODO*///		while (((long)srcdata & 3) && dstdata < end)	/* longword align */
/*TODO*///		{
/*TODO*///			int col;
/*TODO*///
/*TODO*///			col = *(srcdata++);
/*TODO*///			if (col != transpen) *dstdata = col;
/*TODO*///			dstdata++;
/*TODO*///		}
/*TODO*///		sd4 = (UINT32 *)srcdata;
/*TODO*///		while (dstdata <= end - 4)
/*TODO*///		{
/*TODO*///			UINT32 col4;
/*TODO*///
/*TODO*///			if ((col4 = *(sd4++)) != trans4)
/*TODO*///			{
/*TODO*///				UINT32 xod4;
/*TODO*///
/*TODO*///				xod4 = col4 ^ trans4;
/*TODO*///				if( (xod4&0x000000ff) && (xod4&0x0000ff00) &&
/*TODO*///					(xod4&0x00ff0000) && (xod4&0xff000000) )
/*TODO*///				{
/*TODO*///					write_dword((UINT32 *)dstdata,col4);
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if (xod4 & 0xff000000) dstdata[BL3] = col4 >> 24;
/*TODO*///					if (xod4 & 0x00ff0000) dstdata[BL2] = col4 >> 16;
/*TODO*///					if (xod4 & 0x0000ff00) dstdata[BL1] = col4 >>  8;
/*TODO*///					if (xod4 & 0x000000ff) dstdata[BL0] = col4;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			dstdata += 4;
/*TODO*///		}
/*TODO*///		srcdata = (unsigned char *)sd4;
/*TODO*///		while (dstdata < end)
/*TODO*///		{
/*TODO*///			int col;
/*TODO*///
/*TODO*///			col = *(srcdata++);
/*TODO*///			if (col != transpen) *dstdata = col;
/*TODO*///			dstdata++;
/*TODO*///		}
/*TODO*///
/*TODO*///		srcdata += srcmodulo;
/*TODO*///		dstdata += dstmodulo;
/*TODO*///		srcheight--;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///INLINE void blockmove_transpen_noremap_flipx8(
/*TODO*///		const UINT8 *srcdata,int srcwidth,int srcheight,int srcmodulo,
/*TODO*///		UINT8 *dstdata,int dstmodulo,
/*TODO*///		int transpen)
/*TODO*///{
/*TODO*///	UINT8 *end;
/*TODO*///	int trans4;
/*TODO*///	UINT32 *sd4;
/*TODO*///
/*TODO*///	srcmodulo += srcwidth;
/*TODO*///	dstmodulo -= srcwidth;
/*TODO*///	//srcdata += srcwidth-1;
/*TODO*///	srcdata -= 3;
/*TODO*///
/*TODO*///	trans4 = transpen * 0x01010101;
/*TODO*///
/*TODO*///	while (srcheight)
/*TODO*///	{
/*TODO*///		end = dstdata + srcwidth;
/*TODO*///		while (((long)srcdata & 3) && dstdata < end)	/* longword align */
/*TODO*///		{
/*TODO*///			int col;
/*TODO*///
/*TODO*///			col = srcdata[3];
/*TODO*///			srcdata--;
/*TODO*///			if (col != transpen) *dstdata = col;
/*TODO*///			dstdata++;
/*TODO*///		}
/*TODO*///		sd4 = (UINT32 *)srcdata;
/*TODO*///		while (dstdata <= end - 4)
/*TODO*///		{
/*TODO*///			UINT32 col4;
/*TODO*///
/*TODO*///			if ((col4 = *(sd4--)) != trans4)
/*TODO*///			{
/*TODO*///				UINT32 xod4;
/*TODO*///
/*TODO*///				xod4 = col4 ^ trans4;
/*TODO*///				if (xod4 & 0x000000ff) dstdata[BL3] = col4;
/*TODO*///				if (xod4 & 0x0000ff00) dstdata[BL2] = col4 >>  8;
/*TODO*///				if (xod4 & 0x00ff0000) dstdata[BL1] = col4 >> 16;
/*TODO*///				if (xod4 & 0xff000000) dstdata[BL0] = col4 >> 24;
/*TODO*///			}
/*TODO*///			dstdata += 4;
/*TODO*///		}
/*TODO*///		srcdata = (unsigned char *)sd4;
/*TODO*///		while (dstdata < end)
/*TODO*///		{
/*TODO*///			int col;
/*TODO*///
/*TODO*///			col = srcdata[3];
/*TODO*///			srcdata--;
/*TODO*///			if (col != transpen) *dstdata = col;
/*TODO*///			dstdata++;
/*TODO*///		}
/*TODO*///
/*TODO*///		srcdata += srcmodulo;
/*TODO*///		dstdata += dstmodulo;
/*TODO*///		srcheight--;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void blockmove_transpen_noremap16(
/*TODO*///		const UINT16 *srcdata,int srcwidth,int srcheight,int srcmodulo,
/*TODO*///		UINT16 *dstdata,int dstmodulo,
/*TODO*///		int transpen)
/*TODO*///{
/*TODO*///	UINT16 *end;
/*TODO*///
/*TODO*///	srcmodulo -= srcwidth;
/*TODO*///	dstmodulo -= srcwidth;
/*TODO*///
/*TODO*///	while (srcheight)
/*TODO*///	{
/*TODO*///		end = dstdata + srcwidth;
/*TODO*///		while (dstdata < end)
/*TODO*///		{
/*TODO*///			int col;
/*TODO*///
/*TODO*///			col = *(srcdata++);
/*TODO*///			if (col != transpen) *dstdata = col;
/*TODO*///			dstdata++;
/*TODO*///		}
/*TODO*///
/*TODO*///		srcdata += srcmodulo;
/*TODO*///		dstdata += dstmodulo;
/*TODO*///		srcheight--;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///INLINE void blockmove_transpen_noremap_flipx16(
/*TODO*///		const UINT16 *srcdata,int srcwidth,int srcheight,int srcmodulo,
/*TODO*///		UINT16 *dstdata,int dstmodulo,
/*TODO*///		int transpen)
/*TODO*///{
/*TODO*///	UINT16 *end;
/*TODO*///
/*TODO*///	srcmodulo += srcwidth;
/*TODO*///	dstmodulo -= srcwidth;
/*TODO*///	//srcdata += srcwidth-1;
/*TODO*///
/*TODO*///	while (srcheight)
/*TODO*///	{
/*TODO*///		end = dstdata + srcwidth;
/*TODO*///		while (dstdata < end)
/*TODO*///		{
/*TODO*///			int col;
/*TODO*///
/*TODO*///			col = *(srcdata--);
/*TODO*///			if (col != transpen) *dstdata = col;
/*TODO*///			dstdata++;
/*TODO*///		}
/*TODO*///
/*TODO*///		srcdata += srcmodulo;
/*TODO*///		dstdata += dstmodulo;
/*TODO*///		srcheight--;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///#define DATA_TYPE UINT8
/*TODO*///#define DECLARE(function,args,body) INLINE void function##8 args body
/*TODO*///#define BLOCKMOVE(function,flipx,args) \
/*TODO*///	if (flipx) blockmove_##function##_flipx##8 args ; \
/*TODO*///	else blockmove_##function##8 args
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DATA_TYPE
/*TODO*///#undef DECLARE
/*TODO*///#undef BLOCKMOVE
/*TODO*///
/*TODO*///#define DATA_TYPE UINT16
/*TODO*///#define DECLARE(function,args,body) INLINE void function##16 args body
/*TODO*///#define BLOCKMOVE(function,flipx,args) \
/*TODO*///	if (flipx) blockmove_##function##_flipx##16 args ; \
/*TODO*///	else blockmove_##function##16 args
/*TODO*///#include "drawgfx.c"
/*TODO*///#undef DATA_TYPE
/*TODO*///#undef DECLARE
/*TODO*///#undef BLOCKMOVE
/*TODO*///
/*TODO*///
    public static void drawgfx(osd_bitmap dest, GfxElement gfx,
            int code, int color, int flipx, int flipy, int sx, int sy,
            rectangle clip, int transparency, int transparent_color)
    {
        throw new UnsupportedOperationException("Unsupported drawgfx!! Here you go nickblame :D");
/*TODO*///	struct rectangle myclip;
/*TODO*///
/*TODO*///	if (!gfx)
/*TODO*///	{
/*TODO*///		usrintf_showmessage("drawgfx() gfx == 0");
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	if (!gfx->colortable)
/*TODO*///	{
/*TODO*///		usrintf_showmessage("drawgfx() gfx->colortable == 0");
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	code %= gfx->total_elements;
/*TODO*///	color %= gfx->total_colors;
/*TODO*///
/*TODO*///	if (gfx->pen_usage && (transparency == TRANSPARENCY_PEN || transparency == TRANSPARENCY_PENS))
/*TODO*///	{
/*TODO*///		int transmask = 0;
/*TODO*///
/*TODO*///		if (transparency == TRANSPARENCY_PEN)
/*TODO*///		{
/*TODO*///			transmask = 1 << transparent_color;
/*TODO*///		}
/*TODO*///		else if (transparency == TRANSPARENCY_PENS)
/*TODO*///		{
/*TODO*///			transmask = transparent_color;
/*TODO*///		}
/*TODO*///
/*TODO*///		if ((gfx->pen_usage[code] & ~transmask) == 0)
/*TODO*///			/* character is totally transparent, no need to draw */
/*TODO*///			return;
/*TODO*///		else if ((gfx->pen_usage[code] & transmask) == 0 && transparency != TRANSPARENCY_THROUGH && transparency != TRANSPARENCY_PEN_TABLE )
/*TODO*///			/* character is totally opaque, can disable transparency */
/*TODO*///			transparency = TRANSPARENCY_NONE;
/*TODO*///	}
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
/*TODO*///
/*TODO*///		flipx = !flipx;
/*TODO*///
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
/*TODO*///
/*TODO*///		flipy = !flipy;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (dest->depth != 16)
/*TODO*///		drawgfx_core8(dest,gfx,code,color,flipx,flipy,sx,sy,clip,transparency,transparent_color);
/*TODO*///	else
/*TODO*///		drawgfx_core16(dest,gfx,code,color,flipx,flipy,sx,sy,clip,transparency,transparent_color);
    }
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Use drawgfx() to copy a bitmap onto another at the given position.
/*TODO*///  This function will very likely change in the future.
/*TODO*///
/*TODO*///***************************************************************************/
    public static void copybitmap(osd_bitmap dest, osd_bitmap src, int flipx, int flipy, int sx, int sy,
            rectangle clip, int transparency, int transparent_color)
    {
         throw new UnsupportedOperationException("Unsupported copybitmap Here you go nickblame :D");
/*TODO*///	struct rectangle myclip;
/*TODO*///
/*TODO*///
/*TODO*///	/* if necessary, remap the transparent color */
/*TODO*///	if (transparency == TRANSPARENCY_COLOR)
/*TODO*///		transparent_color = Machine->pens[transparent_color];
/*TODO*///
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
/*TODO*///	if (dest->depth != 16)
/*TODO*///		copybitmap_core8(dest,src,flipx,flipy,sx,sy,clip,transparency,transparent_color);
/*TODO*///	else
/*TODO*///		copybitmap_core16(dest,src,flipx,flipy,sx,sy,clip,transparency,transparent_color);
    }
/*TODO*///
/*TODO*///
/*TODO*///void copybitmapzoom(struct osd_bitmap *dest_bmp,struct osd_bitmap *source_bmp,int flipx,int flipy,int sx,int sy,
/*TODO*///		const struct rectangle *clip,int transparency,int transparent_color,int scalex,int scaley)
/*TODO*///{
/*TODO*///	struct rectangle myclip;
/*TODO*///
/*TODO*///
/*TODO*///	/*
/*TODO*///	scalex and scaley are 16.16 fixed point numbers
/*TODO*///	1<<15 : shrink to 50%
/*TODO*///	1<<16 : uniform scale
/*TODO*///	1<<17 : double to 200%
/*TODO*///	*/
/*TODO*///
/*TODO*///	/* if necessary, remap the transparent color */
/*TODO*///	if (transparency == TRANSPARENCY_COLOR)
/*TODO*///		transparent_color = Machine->pens[transparent_color];
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
/*TODO*///		temp = scalex;
/*TODO*///		scalex = scaley;
/*TODO*///		scaley = temp;
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
/*TODO*///		sx = dest_bmp->width - ((source_bmp->width * scalex + 0x7fff) >> 16) - sx;
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			int temp;
/*TODO*///
/*TODO*///
/*TODO*///			/* clip and myclip might be the same, so we need a temporary storage */
/*TODO*///			temp = clip->min_x;
/*TODO*///			myclip.min_x = dest_bmp->width-1 - clip->max_x;
/*TODO*///			myclip.max_x = dest_bmp->width-1 - temp;
/*TODO*///			myclip.min_y = clip->min_y;
/*TODO*///			myclip.max_y = clip->max_y;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	if (Machine->orientation & ORIENTATION_FLIP_Y)
/*TODO*///	{
/*TODO*///		sy = dest_bmp->height - ((source_bmp->height * scaley + 0x7fff) >> 16) - sy;
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			int temp;
/*TODO*///
/*TODO*///
/*TODO*///			myclip.min_x = clip->min_x;
/*TODO*///			myclip.max_x = clip->max_x;
/*TODO*///			/* clip and myclip might be the same, so we need a temporary storage */
/*TODO*///			temp = clip->min_y;
/*TODO*///			myclip.min_y = dest_bmp->height-1 - clip->max_y;
/*TODO*///			myclip.max_y = dest_bmp->height-1 - temp;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	/* ASG 980209 -- added 16-bit version */
/*TODO*///	if (dest_bmp->depth != 16)
/*TODO*///	{
/*TODO*///		int sprite_screen_height = (scaley*source_bmp->height+0x8000)>>16;
/*TODO*///		int sprite_screen_width = (scalex*source_bmp->width+0x8000)>>16;
/*TODO*///
/*TODO*///		/* compute sprite increment per screen pixel */
/*TODO*///		int dx = (source_bmp->width<<16)/sprite_screen_width;
/*TODO*///		int dy = (source_bmp->height<<16)/sprite_screen_height;
/*TODO*///
/*TODO*///		int ex = sx+sprite_screen_width;
/*TODO*///		int ey = sy+sprite_screen_height;
/*TODO*///
/*TODO*///		int x_index_base;
/*TODO*///		int y_index;
/*TODO*///
/*TODO*///		if( flipx )
/*TODO*///		{
/*TODO*///			x_index_base = (sprite_screen_width-1)*dx;
/*TODO*///			dx = -dx;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			x_index_base = 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		if( flipy )
/*TODO*///		{
/*TODO*///			y_index = (sprite_screen_height-1)*dy;
/*TODO*///			dy = -dy;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			y_index = 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		if( clip )
/*TODO*///		{
/*TODO*///			if( sx < clip->min_x)
/*TODO*///			{ /* clip left */
/*TODO*///				int pixels = clip->min_x-sx;
/*TODO*///				sx += pixels;
/*TODO*///				x_index_base += pixels*dx;
/*TODO*///			}
/*TODO*///			if( sy < clip->min_y )
/*TODO*///			{ /* clip top */
/*TODO*///				int pixels = clip->min_y-sy;
/*TODO*///				sy += pixels;
/*TODO*///				y_index += pixels*dy;
/*TODO*///			}
/*TODO*///			/* NS 980211 - fixed incorrect clipping */
/*TODO*///			if( ex > clip->max_x+1 )
/*TODO*///			{ /* clip right */
/*TODO*///				int pixels = ex-clip->max_x-1;
/*TODO*///				ex -= pixels;
/*TODO*///			}
/*TODO*///			if( ey > clip->max_y+1 )
/*TODO*///			{ /* clip bottom */
/*TODO*///				int pixels = ey-clip->max_y-1;
/*TODO*///				ey -= pixels;
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		if( ex>sx )
/*TODO*///		{ /* skip if inner loop doesn't draw anything */
/*TODO*///			int y;
/*TODO*///
/*TODO*///			switch (transparency)
/*TODO*///			{
/*TODO*///				case TRANSPARENCY_NONE:
/*TODO*///					for( y=sy; y<ey; y++ )
/*TODO*///					{
/*TODO*///						unsigned char *source = source_bmp->line[(y_index>>16)];
/*TODO*///						unsigned char *dest = dest_bmp->line[y];
/*TODO*///
/*TODO*///						int x, x_index = x_index_base;
/*TODO*///						for( x=sx; x<ex; x++ )
/*TODO*///						{
/*TODO*///							dest[x] = source[x_index>>16];
/*TODO*///							x_index += dx;
/*TODO*///						}
/*TODO*///
/*TODO*///						y_index += dy;
/*TODO*///					}
/*TODO*///					break;
/*TODO*///
/*TODO*///				case TRANSPARENCY_PEN:
/*TODO*///				case TRANSPARENCY_COLOR:
/*TODO*///					for( y=sy; y<ey; y++ )
/*TODO*///					{
/*TODO*///						unsigned char *source = source_bmp->line[(y_index>>16)];
/*TODO*///						unsigned char *dest = dest_bmp->line[y];
/*TODO*///
/*TODO*///						int x, x_index = x_index_base;
/*TODO*///						for( x=sx; x<ex; x++ )
/*TODO*///						{
/*TODO*///							int c = source[x_index>>16];
/*TODO*///							if( c != transparent_color ) dest[x] = c;
/*TODO*///							x_index += dx;
/*TODO*///						}
/*TODO*///
/*TODO*///						y_index += dy;
/*TODO*///					}
/*TODO*///					break;
/*TODO*///
/*TODO*///				case TRANSPARENCY_THROUGH:
/*TODO*///usrintf_showmessage("copybitmapzoom() TRANSPARENCY_THROUGH");
/*TODO*///					break;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* ASG 980209 -- new 16-bit part */
/*TODO*///	else
/*TODO*///	{
/*TODO*///		int sprite_screen_height = (scaley*source_bmp->height+0x8000)>>16;
/*TODO*///		int sprite_screen_width = (scalex*source_bmp->width+0x8000)>>16;
/*TODO*///
/*TODO*///		/* compute sprite increment per screen pixel */
/*TODO*///		int dx = (source_bmp->width<<16)/sprite_screen_width;
/*TODO*///		int dy = (source_bmp->height<<16)/sprite_screen_height;
/*TODO*///
/*TODO*///		int ex = sx+sprite_screen_width;
/*TODO*///		int ey = sy+sprite_screen_height;
/*TODO*///
/*TODO*///		int x_index_base;
/*TODO*///		int y_index;
/*TODO*///
/*TODO*///		if( flipx )
/*TODO*///		{
/*TODO*///			x_index_base = (sprite_screen_width-1)*dx;
/*TODO*///			dx = -dx;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			x_index_base = 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		if( flipy )
/*TODO*///		{
/*TODO*///			y_index = (sprite_screen_height-1)*dy;
/*TODO*///			dy = -dy;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			y_index = 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		if( clip )
/*TODO*///		{
/*TODO*///			if( sx < clip->min_x)
/*TODO*///			{ /* clip left */
/*TODO*///				int pixels = clip->min_x-sx;
/*TODO*///				sx += pixels;
/*TODO*///				x_index_base += pixels*dx;
/*TODO*///			}
/*TODO*///			if( sy < clip->min_y )
/*TODO*///			{ /* clip top */
/*TODO*///				int pixels = clip->min_y-sy;
/*TODO*///				sy += pixels;
/*TODO*///				y_index += pixels*dy;
/*TODO*///			}
/*TODO*///			/* NS 980211 - fixed incorrect clipping */
/*TODO*///			if( ex > clip->max_x+1 )
/*TODO*///			{ /* clip right */
/*TODO*///				int pixels = ex-clip->max_x-1;
/*TODO*///				ex -= pixels;
/*TODO*///			}
/*TODO*///			if( ey > clip->max_y+1 )
/*TODO*///			{ /* clip bottom */
/*TODO*///				int pixels = ey-clip->max_y-1;
/*TODO*///				ey -= pixels;
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		if( ex>sx )
/*TODO*///		{ /* skip if inner loop doesn't draw anything */
/*TODO*///			int y;
/*TODO*///
/*TODO*///			switch (transparency)
/*TODO*///			{
/*TODO*///				case TRANSPARENCY_NONE:
/*TODO*///					for( y=sy; y<ey; y++ )
/*TODO*///					{
/*TODO*///						unsigned short *source = (unsigned short *)source_bmp->line[(y_index>>16)];
/*TODO*///						unsigned short *dest = (unsigned short *)dest_bmp->line[y];
/*TODO*///
/*TODO*///						int x, x_index = x_index_base;
/*TODO*///						for( x=sx; x<ex; x++ )
/*TODO*///						{
/*TODO*///							dest[x] = source[x_index>>16];
/*TODO*///							x_index += dx;
/*TODO*///						}
/*TODO*///
/*TODO*///						y_index += dy;
/*TODO*///					}
/*TODO*///					break;
/*TODO*///
/*TODO*///				case TRANSPARENCY_PEN:
/*TODO*///				case TRANSPARENCY_COLOR:
/*TODO*///					for( y=sy; y<ey; y++ )
/*TODO*///					{
/*TODO*///						unsigned short *source = (unsigned short *)source_bmp->line[(y_index>>16)];
/*TODO*///						unsigned short *dest = (unsigned short *)dest_bmp->line[y];
/*TODO*///
/*TODO*///						int x, x_index = x_index_base;
/*TODO*///						for( x=sx; x<ex; x++ )
/*TODO*///						{
/*TODO*///							int c = source[x_index>>16];
/*TODO*///							if( c != transparent_color ) dest[x] = c;
/*TODO*///							x_index += dx;
/*TODO*///						}
/*TODO*///
/*TODO*///						y_index += dy;
/*TODO*///					}
/*TODO*///					break;
/*TODO*///
/*TODO*///				case TRANSPARENCY_THROUGH:
/*TODO*///usrintf_showmessage("copybitmapzoom() TRANSPARENCY_THROUGH");
/*TODO*///					break;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Copy a bitmap onto another with scroll and wraparound.
/*TODO*///  This function supports multiple independently scrolling rows/columns.
/*TODO*///  "rows" is the number of indepentently scrolling rows. "rowscroll" is an
/*TODO*///  array of integers telling how much to scroll each row. Same thing for
/*TODO*///  "cols" and "colscroll".
/*TODO*///  If the bitmap cannot scroll in one direction, set rows or columns to 0.
/*TODO*///  If the bitmap scrolls as a whole, set rows and/or cols to 1.
/*TODO*///  Bidirectional scrolling is, of course, supported only if the bitmap
/*TODO*///  scrolls as a whole in at least one direction.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///void copyscrollbitmap(struct osd_bitmap *dest,struct osd_bitmap *src,
/*TODO*///		int rows,const int *rowscroll,int cols,const int *colscroll,
/*TODO*///		const struct rectangle *clip,int transparency,int transparent_color)
/*TODO*///{
/*TODO*///	int srcwidth,srcheight,destwidth,destheight;
/*TODO*///
/*TODO*///
/*TODO*///	if (rows == 0 && cols == 0)
/*TODO*///	{
/*TODO*///		copybitmap(dest,src,0,0,0,0,clip,transparency,transparent_color);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///	{
/*TODO*///		srcwidth = src->height;
/*TODO*///		srcheight = src->width;
/*TODO*///		destwidth = dest->height;
/*TODO*///		destheight = dest->width;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		srcwidth = src->width;
/*TODO*///		srcheight = src->height;
/*TODO*///		destwidth = dest->width;
/*TODO*///		destheight = dest->height;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (rows == 0)
/*TODO*///	{
/*TODO*///		/* scrolling columns */
/*TODO*///		int col,colwidth;
/*TODO*///		struct rectangle myclip;
/*TODO*///
/*TODO*///
/*TODO*///		colwidth = srcwidth / cols;
/*TODO*///
/*TODO*///		myclip.min_y = clip->min_y;
/*TODO*///		myclip.max_y = clip->max_y;
/*TODO*///
/*TODO*///		col = 0;
/*TODO*///		while (col < cols)
/*TODO*///		{
/*TODO*///			int cons,scroll;
/*TODO*///
/*TODO*///
/*TODO*///			/* count consecutive columns scrolled by the same amount */
/*TODO*///			scroll = colscroll[col];
/*TODO*///			cons = 1;
/*TODO*///			while (col + cons < cols &&	colscroll[col + cons] == scroll)
/*TODO*///				cons++;
/*TODO*///
/*TODO*///			if (scroll < 0) scroll = srcheight - (-scroll) % srcheight;
/*TODO*///			else scroll %= srcheight;
/*TODO*///
/*TODO*///			myclip.min_x = col * colwidth;
/*TODO*///			if (myclip.min_x < clip->min_x) myclip.min_x = clip->min_x;
/*TODO*///			myclip.max_x = (col + cons) * colwidth - 1;
/*TODO*///			if (myclip.max_x > clip->max_x) myclip.max_x = clip->max_x;
/*TODO*///
/*TODO*///			copybitmap(dest,src,0,0,0,scroll,&myclip,transparency,transparent_color);
/*TODO*///			copybitmap(dest,src,0,0,0,scroll - srcheight,&myclip,transparency,transparent_color);
/*TODO*///
/*TODO*///			col += cons;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else if (cols == 0)
/*TODO*///	{
/*TODO*///		/* scrolling rows */
/*TODO*///		int row,rowheight;
/*TODO*///		struct rectangle myclip;
/*TODO*///
/*TODO*///
/*TODO*///		rowheight = srcheight / rows;
/*TODO*///
/*TODO*///		myclip.min_x = clip->min_x;
/*TODO*///		myclip.max_x = clip->max_x;
/*TODO*///
/*TODO*///		row = 0;
/*TODO*///		while (row < rows)
/*TODO*///		{
/*TODO*///			int cons,scroll;
/*TODO*///
/*TODO*///
/*TODO*///			/* count consecutive rows scrolled by the same amount */
/*TODO*///			scroll = rowscroll[row];
/*TODO*///			cons = 1;
/*TODO*///			while (row + cons < rows &&	rowscroll[row + cons] == scroll)
/*TODO*///				cons++;
/*TODO*///
/*TODO*///			if (scroll < 0) scroll = srcwidth - (-scroll) % srcwidth;
/*TODO*///			else scroll %= srcwidth;
/*TODO*///
/*TODO*///			myclip.min_y = row * rowheight;
/*TODO*///			if (myclip.min_y < clip->min_y) myclip.min_y = clip->min_y;
/*TODO*///			myclip.max_y = (row + cons) * rowheight - 1;
/*TODO*///			if (myclip.max_y > clip->max_y) myclip.max_y = clip->max_y;
/*TODO*///
/*TODO*///			copybitmap(dest,src,0,0,scroll,0,&myclip,transparency,transparent_color);
/*TODO*///			copybitmap(dest,src,0,0,scroll - srcwidth,0,&myclip,transparency,transparent_color);
/*TODO*///
/*TODO*///			row += cons;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else if (rows == 1 && cols == 1)
/*TODO*///	{
/*TODO*///		/* XY scrolling playfield */
/*TODO*///		int scrollx,scrolly,sx,sy;
/*TODO*///
/*TODO*///
/*TODO*///		if (rowscroll[0] < 0) scrollx = srcwidth - (-rowscroll[0]) % srcwidth;
/*TODO*///		else scrollx = rowscroll[0] % srcwidth;
/*TODO*///
/*TODO*///		if (colscroll[0] < 0) scrolly = srcheight - (-colscroll[0]) % srcheight;
/*TODO*///		else scrolly = colscroll[0] % srcheight;
/*TODO*///
/*TODO*///		for (sx = scrollx - srcwidth;sx < destwidth;sx += srcwidth)
/*TODO*///			for (sy = scrolly - srcheight;sy < destheight;sy += srcheight)
/*TODO*///				copybitmap(dest,src,0,0,sx,sy,clip,transparency,transparent_color);
/*TODO*///	}
/*TODO*///	else if (rows == 1)
/*TODO*///	{
/*TODO*///		/* scrolling columns + horizontal scroll */
/*TODO*///		int col,colwidth;
/*TODO*///		int scrollx;
/*TODO*///		struct rectangle myclip;
/*TODO*///
/*TODO*///
/*TODO*///		if (rowscroll[0] < 0) scrollx = srcwidth - (-rowscroll[0]) % srcwidth;
/*TODO*///		else scrollx = rowscroll[0] % srcwidth;
/*TODO*///
/*TODO*///		colwidth = srcwidth / cols;
/*TODO*///
/*TODO*///		myclip.min_y = clip->min_y;
/*TODO*///		myclip.max_y = clip->max_y;
/*TODO*///
/*TODO*///		col = 0;
/*TODO*///		while (col < cols)
/*TODO*///		{
/*TODO*///			int cons,scroll;
/*TODO*///
/*TODO*///
/*TODO*///			/* count consecutive columns scrolled by the same amount */
/*TODO*///			scroll = colscroll[col];
/*TODO*///			cons = 1;
/*TODO*///			while (col + cons < cols &&	colscroll[col + cons] == scroll)
/*TODO*///				cons++;
/*TODO*///
/*TODO*///			if (scroll < 0) scroll = srcheight - (-scroll) % srcheight;
/*TODO*///			else scroll %= srcheight;
/*TODO*///
/*TODO*///			myclip.min_x = col * colwidth + scrollx;
/*TODO*///			if (myclip.min_x < clip->min_x) myclip.min_x = clip->min_x;
/*TODO*///			myclip.max_x = (col + cons) * colwidth - 1 + scrollx;
/*TODO*///			if (myclip.max_x > clip->max_x) myclip.max_x = clip->max_x;
/*TODO*///
/*TODO*///			copybitmap(dest,src,0,0,scrollx,scroll,&myclip,transparency,transparent_color);
/*TODO*///			copybitmap(dest,src,0,0,scrollx,scroll - srcheight,&myclip,transparency,transparent_color);
/*TODO*///
/*TODO*///			myclip.min_x = col * colwidth + scrollx - srcwidth;
/*TODO*///			if (myclip.min_x < clip->min_x) myclip.min_x = clip->min_x;
/*TODO*///			myclip.max_x = (col + cons) * colwidth - 1 + scrollx - srcwidth;
/*TODO*///			if (myclip.max_x > clip->max_x) myclip.max_x = clip->max_x;
/*TODO*///
/*TODO*///			copybitmap(dest,src,0,0,scrollx - srcwidth,scroll,&myclip,transparency,transparent_color);
/*TODO*///			copybitmap(dest,src,0,0,scrollx - srcwidth,scroll - srcheight,&myclip,transparency,transparent_color);
/*TODO*///
/*TODO*///			col += cons;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else if (cols == 1)
/*TODO*///	{
/*TODO*///		/* scrolling rows + vertical scroll */
/*TODO*///		int row,rowheight;
/*TODO*///		int scrolly;
/*TODO*///		struct rectangle myclip;
/*TODO*///
/*TODO*///
/*TODO*///		if (colscroll[0] < 0) scrolly = srcheight - (-colscroll[0]) % srcheight;
/*TODO*///		else scrolly = colscroll[0] % srcheight;
/*TODO*///
/*TODO*///		rowheight = srcheight / rows;
/*TODO*///
/*TODO*///		myclip.min_x = clip->min_x;
/*TODO*///		myclip.max_x = clip->max_x;
/*TODO*///
/*TODO*///		row = 0;
/*TODO*///		while (row < rows)
/*TODO*///		{
/*TODO*///			int cons,scroll;
/*TODO*///
/*TODO*///
/*TODO*///			/* count consecutive rows scrolled by the same amount */
/*TODO*///			scroll = rowscroll[row];
/*TODO*///			cons = 1;
/*TODO*///			while (row + cons < rows &&	rowscroll[row + cons] == scroll)
/*TODO*///				cons++;
/*TODO*///
/*TODO*///			if (scroll < 0) scroll = srcwidth - (-scroll) % srcwidth;
/*TODO*///			else scroll %= srcwidth;
/*TODO*///
/*TODO*///			myclip.min_y = row * rowheight + scrolly;
/*TODO*///			if (myclip.min_y < clip->min_y) myclip.min_y = clip->min_y;
/*TODO*///			myclip.max_y = (row + cons) * rowheight - 1 + scrolly;
/*TODO*///			if (myclip.max_y > clip->max_y) myclip.max_y = clip->max_y;
/*TODO*///
/*TODO*///			copybitmap(dest,src,0,0,scroll,scrolly,&myclip,transparency,transparent_color);
/*TODO*///			copybitmap(dest,src,0,0,scroll - srcwidth,scrolly,&myclip,transparency,transparent_color);
/*TODO*///
/*TODO*///			myclip.min_y = row * rowheight + scrolly - srcheight;
/*TODO*///			if (myclip.min_y < clip->min_y) myclip.min_y = clip->min_y;
/*TODO*///			myclip.max_y = (row + cons) * rowheight - 1 + scrolly - srcheight;
/*TODO*///			if (myclip.max_y > clip->max_y) myclip.max_y = clip->max_y;
/*TODO*///
/*TODO*///			copybitmap(dest,src,0,0,scroll,scrolly - srcheight,&myclip,transparency,transparent_color);
/*TODO*///			copybitmap(dest,src,0,0,scroll - srcwidth,scrolly - srcheight,&myclip,transparency,transparent_color);
/*TODO*///
/*TODO*///			row += cons;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////* fill a bitmap using the specified pen */
/*TODO*///void fillbitmap(struct osd_bitmap *dest,int pen,const struct rectangle *clip)
/*TODO*///{
/*TODO*///	int sx,sy,ex,ey,y;
/*TODO*///	struct rectangle myclip;
/*TODO*///
/*TODO*///
/*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///	{
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			myclip.min_x = clip->min_y;
/*TODO*///			myclip.max_x = clip->max_y;
/*TODO*///			myclip.min_y = clip->min_x;
/*TODO*///			myclip.max_y = clip->max_x;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	if (Machine->orientation & ORIENTATION_FLIP_X)
/*TODO*///	{
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			int temp;
/*TODO*///
/*TODO*///
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
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			int temp;
/*TODO*///
/*TODO*///
/*TODO*///			myclip.min_x = clip->min_x;
/*TODO*///			myclip.max_x = clip->max_x;
/*TODO*///			temp = clip->min_y;
/*TODO*///			myclip.min_y = dest->height-1 - clip->max_y;
/*TODO*///			myclip.max_y = dest->height-1 - temp;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	sx = 0;
/*TODO*///	ex = dest->width - 1;
/*TODO*///	sy = 0;
/*TODO*///	ey = dest->height - 1;
/*TODO*///
/*TODO*///	if (clip && sx < clip->min_x) sx = clip->min_x;
/*TODO*///	if (clip && ex > clip->max_x) ex = clip->max_x;
/*TODO*///	if (sx > ex) return;
/*TODO*///	if (clip && sy < clip->min_y) sy = clip->min_y;
/*TODO*///	if (clip && ey > clip->max_y) ey = clip->max_y;
/*TODO*///	if (sy > ey) return;
/*TODO*///
/*TODO*///	osd_mark_dirty (sx,sy,ex,ey,0);	/* ASG 971011 */
/*TODO*///
/*TODO*///	/* ASG 980211 */
/*TODO*///	if (dest->depth == 16)
/*TODO*///	{
/*TODO*///		if ((pen >> 8) == (pen & 0xff))
/*TODO*///		{
/*TODO*///			for (y = sy;y <= ey;y++)
/*TODO*///				memset(&dest->line[y][sx*2],pen&0xff,(ex-sx+1)*2);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			unsigned short *sp = (unsigned short *)dest->line[sy];
/*TODO*///			int x;
/*TODO*///
/*TODO*///			for (x = sx;x <= ex;x++)
/*TODO*///				sp[x] = pen;
/*TODO*///			sp+=sx;
/*TODO*///			for (y = sy+1;y <= ey;y++)
/*TODO*///				memcpy(&dest->line[y][sx*2],sp,(ex-sx+1)*2);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		for (y = sy;y <= ey;y++)
/*TODO*///			memset(&dest->line[y][sx],pen,ex-sx+1);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void drawgfxzoom( struct osd_bitmap *dest_bmp,const struct GfxElement *gfx,
/*TODO*///		unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
/*TODO*///		const struct rectangle *clip,int transparency,int transparent_color,int scalex, int scaley)
/*TODO*///{
/*TODO*///	struct rectangle myclip;
/*TODO*///
/*TODO*///
/*TODO*///	/* only support TRANSPARENCY_PEN and TRANSPARENCY_COLOR */
/*TODO*///	if (transparency != TRANSPARENCY_PEN && transparency != TRANSPARENCY_COLOR)
/*TODO*///		return;
/*TODO*///
/*TODO*///	if (transparency == TRANSPARENCY_COLOR)
/*TODO*///		transparent_color = Machine->pens[transparent_color];
/*TODO*///
/*TODO*///
/*TODO*///	/*
/*TODO*///	scalex and scaley are 16.16 fixed point numbers
/*TODO*///	1<<15 : shrink to 50%
/*TODO*///	1<<16 : uniform scale
/*TODO*///	1<<17 : double to 200%
/*TODO*///	*/
/*TODO*///
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
/*TODO*///		temp = scalex;
/*TODO*///		scalex = scaley;
/*TODO*///		scaley = temp;
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
/*TODO*///		sx = dest_bmp->width - ((gfx->width * scalex + 0x7fff) >> 16) - sx;
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			int temp;
/*TODO*///
/*TODO*///
/*TODO*///			/* clip and myclip might be the same, so we need a temporary storage */
/*TODO*///			temp = clip->min_x;
/*TODO*///			myclip.min_x = dest_bmp->width-1 - clip->max_x;
/*TODO*///			myclip.max_x = dest_bmp->width-1 - temp;
/*TODO*///			myclip.min_y = clip->min_y;
/*TODO*///			myclip.max_y = clip->max_y;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///
/*TODO*///		flipx = !flipx;
/*TODO*///
/*TODO*///	}
/*TODO*///	if (Machine->orientation & ORIENTATION_FLIP_Y)
/*TODO*///	{
/*TODO*///		sy = dest_bmp->height - ((gfx->height * scaley + 0x7fff) >> 16) - sy;
/*TODO*///		if (clip)
/*TODO*///		{
/*TODO*///			int temp;
/*TODO*///
/*TODO*///
/*TODO*///			myclip.min_x = clip->min_x;
/*TODO*///			myclip.max_x = clip->max_x;
/*TODO*///			/* clip and myclip might be the same, so we need a temporary storage */
/*TODO*///			temp = clip->min_y;
/*TODO*///			myclip.min_y = dest_bmp->height-1 - clip->max_y;
/*TODO*///			myclip.max_y = dest_bmp->height-1 - temp;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///
/*TODO*///		flipy = !flipy;
/*TODO*///
/*TODO*///	}
/*TODO*///
/*TODO*///	/* KW 991012 -- Added code to force clip to bitmap boundary */
/*TODO*///	if(clip)
/*TODO*///	{
/*TODO*///		myclip.min_x = clip->min_x;
/*TODO*///		myclip.max_x = clip->max_x;
/*TODO*///		myclip.min_y = clip->min_y;
/*TODO*///		myclip.max_y = clip->max_y;
/*TODO*///
/*TODO*///		if (myclip.min_x < 0) myclip.min_x = 0;
/*TODO*///		if (myclip.max_x >= dest_bmp->width) myclip.max_x = dest_bmp->width-1;
/*TODO*///		if (myclip.min_y < 0) myclip.min_y = 0;
/*TODO*///		if (myclip.max_y >= dest_bmp->height) myclip.max_y = dest_bmp->height-1;
/*TODO*///
/*TODO*///		clip=&myclip;
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	/* ASG 980209 -- added 16-bit version */
/*TODO*///	if (dest_bmp->depth != 16)
/*TODO*///	{
/*TODO*///		if( gfx && gfx->colortable )
/*TODO*///		{
/*TODO*///			const unsigned short *pal = &gfx->colortable[gfx->color_granularity * (color % gfx->total_colors)]; /* ASG 980209 */
/*TODO*///			int source_base = (code % gfx->total_elements) * gfx->height;
/*TODO*///
/*TODO*///			int sprite_screen_height = (scaley*gfx->height+0x8000)>>16;
/*TODO*///			int sprite_screen_width = (scalex*gfx->width+0x8000)>>16;
/*TODO*///
/*TODO*///			/* compute sprite increment per screen pixel */
/*TODO*///			int dx = (gfx->width<<16)/sprite_screen_width;
/*TODO*///			int dy = (gfx->height<<16)/sprite_screen_height;
/*TODO*///
/*TODO*///			int ex = sx+sprite_screen_width;
/*TODO*///			int ey = sy+sprite_screen_height;
/*TODO*///
/*TODO*///			int x_index_base;
/*TODO*///			int y_index;
/*TODO*///
/*TODO*///			if( flipx )
/*TODO*///			{
/*TODO*///				x_index_base = (sprite_screen_width-1)*dx;
/*TODO*///				dx = -dx;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				x_index_base = 0;
/*TODO*///			}
/*TODO*///
/*TODO*///			if( flipy )
/*TODO*///			{
/*TODO*///				y_index = (sprite_screen_height-1)*dy;
/*TODO*///				dy = -dy;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				y_index = 0;
/*TODO*///			}
/*TODO*///
/*TODO*///			if( clip )
/*TODO*///			{
/*TODO*///				if( sx < clip->min_x)
/*TODO*///				{ /* clip left */
/*TODO*///					int pixels = clip->min_x-sx;
/*TODO*///					sx += pixels;
/*TODO*///					x_index_base += pixels*dx;
/*TODO*///				}
/*TODO*///				if( sy < clip->min_y )
/*TODO*///				{ /* clip top */
/*TODO*///					int pixels = clip->min_y-sy;
/*TODO*///					sy += pixels;
/*TODO*///					y_index += pixels*dy;
/*TODO*///				}
/*TODO*///				/* NS 980211 - fixed incorrect clipping */
/*TODO*///				if( ex > clip->max_x+1 )
/*TODO*///				{ /* clip right */
/*TODO*///					int pixels = ex-clip->max_x-1;
/*TODO*///					ex -= pixels;
/*TODO*///				}
/*TODO*///				if( ey > clip->max_y+1 )
/*TODO*///				{ /* clip bottom */
/*TODO*///					int pixels = ey-clip->max_y-1;
/*TODO*///					ey -= pixels;
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			if( ex>sx )
/*TODO*///			{ /* skip if inner loop doesn't draw anything */
/*TODO*///				int y;
/*TODO*///
/*TODO*///				/* case 1: TRANSPARENCY_PEN */
/*TODO*///				if (transparency == TRANSPARENCY_PEN)
/*TODO*///				{
/*TODO*///					for( y=sy; y<ey; y++ )
/*TODO*///					{
/*TODO*///						unsigned char *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///						unsigned char *dest = dest_bmp->line[y];
/*TODO*///
/*TODO*///						int x, x_index = x_index_base;
/*TODO*///						for( x=sx; x<ex; x++ )
/*TODO*///						{
/*TODO*///							int c = source[x_index>>16];
/*TODO*///							if( c != transparent_color ) dest[x] = pal[c];
/*TODO*///							x_index += dx;
/*TODO*///						}
/*TODO*///
/*TODO*///						y_index += dy;
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				/* case 2: TRANSPARENCY_COLOR */
/*TODO*///				else if (transparency == TRANSPARENCY_COLOR)
/*TODO*///				{
/*TODO*///					for( y=sy; y<ey; y++ )
/*TODO*///					{
/*TODO*///						unsigned char *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///						unsigned char *dest = dest_bmp->line[y];
/*TODO*///
/*TODO*///						int x, x_index = x_index_base;
/*TODO*///						for( x=sx; x<ex; x++ )
/*TODO*///						{
/*TODO*///							int c = pal[source[x_index>>16]];
/*TODO*///							if( c != transparent_color ) dest[x] = c;
/*TODO*///							x_index += dx;
/*TODO*///						}
/*TODO*///
/*TODO*///						y_index += dy;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* ASG 980209 -- new 16-bit part */
/*TODO*///	else
/*TODO*///	{
/*TODO*///		if( gfx && gfx->colortable )
/*TODO*///		{
/*TODO*///			const unsigned short *pal = &gfx->colortable[gfx->color_granularity * (color % gfx->total_colors)]; /* ASG 980209 */
/*TODO*///			int source_base = (code % gfx->total_elements) * gfx->height;
/*TODO*///
/*TODO*///			int sprite_screen_height = (scaley*gfx->height+0x8000)>>16;
/*TODO*///			int sprite_screen_width = (scalex*gfx->width+0x8000)>>16;
/*TODO*///
/*TODO*///			/* compute sprite increment per screen pixel */
/*TODO*///			int dx = (gfx->width<<16)/sprite_screen_width;
/*TODO*///			int dy = (gfx->height<<16)/sprite_screen_height;
/*TODO*///
/*TODO*///			int ex = sx+sprite_screen_width;
/*TODO*///			int ey = sy+sprite_screen_height;
/*TODO*///
/*TODO*///			int x_index_base;
/*TODO*///			int y_index;
/*TODO*///
/*TODO*///			if( flipx )
/*TODO*///			{
/*TODO*///				x_index_base = (sprite_screen_width-1)*dx;
/*TODO*///				dx = -dx;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				x_index_base = 0;
/*TODO*///			}
/*TODO*///
/*TODO*///			if( flipy )
/*TODO*///			{
/*TODO*///				y_index = (sprite_screen_height-1)*dy;
/*TODO*///				dy = -dy;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				y_index = 0;
/*TODO*///			}
/*TODO*///
/*TODO*///			if( clip )
/*TODO*///			{
/*TODO*///				if( sx < clip->min_x)
/*TODO*///				{ /* clip left */
/*TODO*///					int pixels = clip->min_x-sx;
/*TODO*///					sx += pixels;
/*TODO*///					x_index_base += pixels*dx;
/*TODO*///				}
/*TODO*///				if( sy < clip->min_y )
/*TODO*///				{ /* clip top */
/*TODO*///					int pixels = clip->min_y-sy;
/*TODO*///					sy += pixels;
/*TODO*///					y_index += pixels*dy;
/*TODO*///				}
/*TODO*///				/* NS 980211 - fixed incorrect clipping */
/*TODO*///				if( ex > clip->max_x+1 )
/*TODO*///				{ /* clip right */
/*TODO*///					int pixels = ex-clip->max_x-1;
/*TODO*///					ex -= pixels;
/*TODO*///				}
/*TODO*///				if( ey > clip->max_y+1 )
/*TODO*///				{ /* clip bottom */
/*TODO*///					int pixels = ey-clip->max_y-1;
/*TODO*///					ey -= pixels;
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			if( ex>sx )
/*TODO*///			{ /* skip if inner loop doesn't draw anything */
/*TODO*///				int y;
/*TODO*///
/*TODO*///				/* case 1: TRANSPARENCY_PEN */
/*TODO*///				if (transparency == TRANSPARENCY_PEN)
/*TODO*///				{
/*TODO*///					for( y=sy; y<ey; y++ )
/*TODO*///					{
/*TODO*///						unsigned char *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///						unsigned short *dest = (unsigned short *)dest_bmp->line[y];
/*TODO*///
/*TODO*///						int x, x_index = x_index_base;
/*TODO*///						for( x=sx; x<ex; x++ )
/*TODO*///						{
/*TODO*///							int c = source[x_index>>16];
/*TODO*///							if( c != transparent_color ) dest[x] = pal[c];
/*TODO*///							x_index += dx;
/*TODO*///						}
/*TODO*///
/*TODO*///						y_index += dy;
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				/* case 2: TRANSPARENCY_COLOR */
/*TODO*///				else if (transparency == TRANSPARENCY_COLOR)
/*TODO*///				{
/*TODO*///					for( y=sy; y<ey; y++ )
/*TODO*///					{
/*TODO*///						unsigned char *source = gfx->gfxdata + (source_base+(y_index>>16)) * gfx->line_modulo;
/*TODO*///						unsigned short *dest = (unsigned short *)dest_bmp->line[y];
/*TODO*///
/*TODO*///						int x, x_index = x_index_base;
/*TODO*///						for( x=sx; x<ex; x++ )
/*TODO*///						{
/*TODO*///							int c = pal[source[x_index>>16]];
/*TODO*///							if( c != transparent_color ) dest[x] = c;
/*TODO*///							x_index += dx;
/*TODO*///						}
/*TODO*///
/*TODO*///						y_index += dy;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void plot_pixel2(struct osd_bitmap *bitmap1,struct osd_bitmap *bitmap2,int x,int y,int pen)
/*TODO*///{
/*TODO*///	plot_pixel(bitmap1, x, y, pen);
/*TODO*///	plot_pixel(bitmap2, x, y, pen);
/*TODO*///}
/*TODO*///
/*TODO*///static void pp_8_nd(struct osd_bitmap *b,int x,int y,int p)  { b->line[y][x] = p; }
        public static plot_pixel_procPtr pp_8_nd = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_8_nd");
    }};
/*TODO*///static void pp_8_nd_fx(struct osd_bitmap *b,int x,int y,int p)  { b->line[y][b->width-1-x] = p; }
       public static plot_pixel_procPtr pp_8_nd_fx = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_8_nd_fx");
    }};
/*TODO*///static void pp_8_nd_fy(struct osd_bitmap *b,int x,int y,int p)  { b->line[b->height-1-y][x] = p; }
              public static plot_pixel_procPtr pp_8_nd_fy = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_8_nd_fy");
    }};
/*TODO*///static void pp_8_nd_fxy(struct osd_bitmap *b,int x,int y,int p)  { b->line[b->height-1-y][b->width-1-x] = p; }
                 public static plot_pixel_procPtr pp_8_nd_fxy = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_8_nd_fxy");
    }};           
/*TODO*///static void pp_8_nd_s(struct osd_bitmap *b,int x,int y,int p)  { b->line[x][y] = p; }
                  public static plot_pixel_procPtr pp_8_nd_s = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_8_nd_s");
    }};  
/*TODO*///static void pp_8_nd_fx_s(struct osd_bitmap *b,int x,int y,int p)  { b->line[x][b->width-1-y] = p; }
                   public static plot_pixel_procPtr pp_8_nd_fx_s = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_8_nd_fx_s");
    }};                  
/*TODO*///static void pp_8_nd_fy_s(struct osd_bitmap *b,int x,int y,int p)  { b->line[b->height-1-x][y] = p; }
                    public static plot_pixel_procPtr pp_8_nd_fy_s = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_8_nd_fy_s");
    }};                   
/*TODO*///static void pp_8_nd_fxy_s(struct osd_bitmap *b,int x,int y,int p)  { b->line[b->height-1-x][b->width-1-y] = p; }
    public static plot_pixel_procPtr pp_8_nd_fxy_s = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_8_nd_fxy_s");
    }};  
/*TODO*///static void pp_8_d(struct osd_bitmap *b,int x,int y,int p)  { b->line[y][x] = p; osd_mark_dirty (x,y,x,y,0); }
        public static plot_pixel_procPtr pp_8_d = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_8_d");
    }};
/*TODO*///static void pp_8_d_fx(struct osd_bitmap *b,int x,int y,int p)  { int newx = b->width-1-x;  b->line[y][newx] = p; osd_mark_dirty (newx,y,newx,y,0); }
          public static plot_pixel_procPtr pp_8_d_fx = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_8_d_fx");
    }};
/*TODO*///static void pp_8_d_fy(struct osd_bitmap *b,int x,int y,int p)  { int newy = b->height-1-y; b->line[newy][x] = p; osd_mark_dirty (x,newy,x,newy,0); }
           public static plot_pixel_procPtr pp_8_d_fy = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_8_d_fy");
    }};        
/*TODO*///static void pp_8_d_fxy(struct osd_bitmap *b,int x,int y,int p)  { int newx = b->width-1-x; int newy = b->height-1-y; b->line[newy][newx] = p; osd_mark_dirty (newx,newy,newx,newy,0); }
            public static plot_pixel_procPtr pp_8_d_fxy = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_8_d_fxy");
    }};           
/*TODO*///static void pp_8_d_s(struct osd_bitmap *b,int x,int y,int p)  { b->line[x][y] = p; osd_mark_dirty (y,x,y,x,0); }
             public static plot_pixel_procPtr pp_8_d_s = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_8_d_s");
    }};           
/*TODO*///static void pp_8_d_fx_s(struct osd_bitmap *b,int x,int y,int p)  { int newy = b->width-1-y; b->line[x][newy] = p; osd_mark_dirty (newy,x,newy,x,0); }
              public static plot_pixel_procPtr pp_8_d_fx_s = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_8_d_fx_s");
    }};              
/*TODO*///static void pp_8_d_fy_s(struct osd_bitmap *b,int x,int y,int p)  { int newx = b->height-1-x; b->line[newx][y] = p; osd_mark_dirty (y,newx,y,newx,0); }
                public static plot_pixel_procPtr pp_8_d_fy_s = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_8_d_fy_s");
    }};              
/*TODO*///static void pp_8_d_fxy_s(struct osd_bitmap *b,int x,int y,int p)  { int newx = b->height-1-x; int newy = b->width-1-y; b->line[newx][newy] = p; osd_mark_dirty (newy,newx,newy,newx,0); }
                 public static plot_pixel_procPtr pp_8_d_fxy_s = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_8_d_fxy_s");
    }};                
/*TODO*///
/*TODO*///static void pp_16_nd(struct osd_bitmap *b,int x,int y,int p)  { ((unsigned short *)b->line[y])[x] = p; }
       public static plot_pixel_procPtr pp_16_nd = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_16_nd");
    }};               
/*TODO*///static void pp_16_nd_fx(struct osd_bitmap *b,int x,int y,int p)  { ((unsigned short *)b->line[y])[b->width-1-x] = p; }
       public static plot_pixel_procPtr pp_16_nd_fx = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_16_nd_fx");
    }};                   
/*TODO*///static void pp_16_nd_fy(struct osd_bitmap *b,int x,int y,int p)  { ((unsigned short *)b->line[b->height-1-y])[x] = p; }
      public static plot_pixel_procPtr pp_16_nd_fy = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_16_nd_fy");
    }};                      
/*TODO*///static void pp_16_nd_fxy(struct osd_bitmap *b,int x,int y,int p)  { ((unsigned short *)b->line[b->height-1-y])[b->width-1-x] = p; }
       public static plot_pixel_procPtr pp_16_nd_fxy = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_16_nd_fxy");
    }};      
/*TODO*///static void pp_16_nd_s(struct osd_bitmap *b,int x,int y,int p)  { ((unsigned short *)b->line[x])[y] = p; }
        public static plot_pixel_procPtr pp_16_nd_s = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_16_nd_s");
    }};       
/*TODO*///static void pp_16_nd_fx_s(struct osd_bitmap *b,int x,int y,int p)  { ((unsigned short *)b->line[x])[b->width-1-y] = p; }
        public static plot_pixel_procPtr pp_16_nd_fx_s = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_16_nd_fx_s");
    }};         
/*TODO*///static void pp_16_nd_fy_s(struct osd_bitmap *b,int x,int y,int p)  { ((unsigned short *)b->line[b->height-1-x])[y] = p; }
    public static plot_pixel_procPtr pp_16_nd_fy_s = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_16_nd_fy_s");
    }};      
/*TODO*///static void pp_16_nd_fxy_s(struct osd_bitmap *b,int x,int y,int p)  { ((unsigned short *)b->line[b->height-1-x])[b->width-1-y] = p; }
    public static plot_pixel_procPtr pp_16_nd_fxy_s = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_16_nd_fxy_s");
    }};  
/*TODO*///static void pp_16_d(struct osd_bitmap *b,int x,int y,int p)  { ((unsigned short *)b->line[y])[x] = p; osd_mark_dirty (x,y,x,y,0); }
     public static plot_pixel_procPtr pp_16_d = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_16_d");
    }};    
/*TODO*///static void pp_16_d_fx(struct osd_bitmap *b,int x,int y,int p)  { int newx = b->width-1-x;  ((unsigned short *)b->line[y])[newx] = p; osd_mark_dirty (newx,y,newx,y,0); }
       public static plot_pixel_procPtr pp_16_d_fx = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_16_d_fx");
    }};    
/*TODO*///static void pp_16_d_fy(struct osd_bitmap *b,int x,int y,int p)  { int newy = b->height-1-y; ((unsigned short *)b->line[newy])[x] = p; osd_mark_dirty (x,newy,x,newy,0); }
       public static plot_pixel_procPtr pp_16_d_fy = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_16_d_fy");
    }};    
/*TODO*///static void pp_16_d_fxy(struct osd_bitmap *b,int x,int y,int p)  { int newx = b->width-1-x; int newy = b->height-1-y; ((unsigned short *)b->line[newy])[newx] = p; osd_mark_dirty (newx,newy,newx,newy,0); }
        public static plot_pixel_procPtr pp_16_d_fxy = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_16_d_fxy");
    }};       
/*TODO*///static void pp_16_d_s(struct osd_bitmap *b,int x,int y,int p)  { ((unsigned short *)b->line[x])[y] = p; osd_mark_dirty (y,x,y,x,0); }
        public static plot_pixel_procPtr pp_16_d_s = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_16_d_s");
    }};       
/*TODO*///static void pp_16_d_fx_s(struct osd_bitmap *b,int x,int y,int p)  { int newy = b->width-1-y; ((unsigned short *)b->line[x])[newy] = p; osd_mark_dirty (newy,x,newy,x,0); }
         public static plot_pixel_procPtr pp_16_d_fx_s = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_16_d_fx_s");
    }};      
/*TODO*///static void pp_16_d_fy_s(struct osd_bitmap *b,int x,int y,int p)  { int newx = b->height-1-x; ((unsigned short *)b->line[newx])[y] = p; osd_mark_dirty (y,newx,y,newx,0); }
        public static plot_pixel_procPtr pp_16_d_fy_s = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_16_d_fy_s");
    }};       
/*TODO*///static void pp_16_d_fxy_s(struct osd_bitmap *b,int x,int y,int p)  { int newx = b->height-1-x; int newy = b->width-1-y; ((unsigned short *)b->line[newx])[newy] = p; osd_mark_dirty (newy,newx,newy,newx,0); }
        public static plot_pixel_procPtr pp_16_d_fxy_s = new plot_pixel_procPtr() { public void handler(osd_bitmap bitmap,int x,int y,int pen) {
        throw new UnsupportedOperationException("Unsupported pp_16_d_fxy_s");
    }};       

/*TODO*///static int rp_8(struct osd_bitmap *b,int x,int y)  { return b->line[y][x]; }
    public static read_pixel_procPtr rp_8 = new read_pixel_procPtr() {	public int handler(osd_bitmap bitmap,int x,int y) {
        throw new UnsupportedOperationException("Unsupported rp_8");
    }};
/*TODO*///static int rp_8_fx(struct osd_bitmap *b,int x,int y)  { return b->line[y][b->width-1-x]; }
    public static read_pixel_procPtr rp_8_fx = new read_pixel_procPtr() {	public int handler(osd_bitmap bitmap,int x,int y) {
        throw new UnsupportedOperationException("Unsupported rp_8_fx");
    }};
/*TODO*///static int rp_8_fy(struct osd_bitmap *b,int x,int y)  { return b->line[b->height-1-y][x]; }
        public static read_pixel_procPtr rp_8_fy = new read_pixel_procPtr() {	public int handler(osd_bitmap bitmap,int x,int y) {
        throw new UnsupportedOperationException("Unsupported rp_8_fy");
    }};
/*TODO*///static int rp_8_fxy(struct osd_bitmap *b,int x,int y)  { return b->line[b->height-1-y][b->width-1-x]; }
         public static read_pixel_procPtr rp_8_fxy = new read_pixel_procPtr() {	public int handler(osd_bitmap bitmap,int x,int y) {
         System.out.println("TRALALAA");
             throw new UnsupportedOperationException("Unsupported rp_8_fy");
    }};
/*TODO*///static int rp_8_s(struct osd_bitmap *b,int x,int y)  { return b->line[x][y]; }
           public static read_pixel_procPtr rp_8_s = new read_pixel_procPtr() {	public int handler(osd_bitmap bitmap,int x,int y) {
        throw new UnsupportedOperationException("Unsupported rp_8_s");
    }};       
/*TODO*///static int rp_8_fx_s(struct osd_bitmap *b,int x,int y)  { return b->line[x][b->width-1-y]; }
           public static read_pixel_procPtr rp_8_fx_s = new read_pixel_procPtr() {	public int handler(osd_bitmap bitmap,int x,int y) {
        throw new UnsupportedOperationException("Unsupported rp_8_fx_s");
    }};
/*TODO*///static int rp_8_fy_s(struct osd_bitmap *b,int x,int y)  { return b->line[b->height-1-x][y]; }
            public static read_pixel_procPtr rp_8_fy_s = new read_pixel_procPtr() {	public int handler(osd_bitmap bitmap,int x,int y) {
        throw new UnsupportedOperationException("Unsupported rp_8_fy_s");
    }};
/*TODO*///static int rp_8_fxy_s(struct osd_bitmap *b,int x,int y)  { return b->line[b->height-1-x][b->width-1-y]; }
     public static read_pixel_procPtr rp_8_fxy_s = new read_pixel_procPtr() {	public int handler(osd_bitmap bitmap,int x,int y) {
        throw new UnsupportedOperationException("Unsupported rp_8_fxy_s");
    }};
/*TODO*///static int rp_16(struct osd_bitmap *b,int x,int y)  { return ((unsigned short *)b->line[y])[x]; }
      public static read_pixel_procPtr rp_16 = new read_pixel_procPtr() {	public int handler(osd_bitmap bitmap,int x,int y) {
        throw new UnsupportedOperationException("Unsupported rp_16");
    }};    
/*TODO*///static int rp_16_fx(struct osd_bitmap *b,int x,int y)  { return ((unsigned short *)b->line[y])[b->width-1-x]; }
      public static read_pixel_procPtr rp_16_fx = new read_pixel_procPtr() {	public int handler(osd_bitmap bitmap,int x,int y) {
        throw new UnsupportedOperationException("Unsupported rp_16_fx");
    }};
/*TODO*///static int rp_16_fy(struct osd_bitmap *b,int x,int y)  { return ((unsigned short *)b->line[b->height-1-y])[x]; }
      public static read_pixel_procPtr rp_16_fy = new read_pixel_procPtr() {	public int handler(osd_bitmap bitmap,int x,int y) {
        throw new UnsupportedOperationException("Unsupported rp_16_fy");
    }};
/*TODO*///static int rp_16_fxy(struct osd_bitmap *b,int x,int y)  { return ((unsigned short *)b->line[b->height-1-y])[b->width-1-x]; }
      public static read_pixel_procPtr rp_16_fxy = new read_pixel_procPtr() {	public int handler(osd_bitmap bitmap,int x,int y) {
        throw new UnsupportedOperationException("Unsupported rp_16_fxy");
    }};
/*TODO*///static int rp_16_s(struct osd_bitmap *b,int x,int y)  { return ((unsigned short *)b->line[x])[y]; }
       public static read_pixel_procPtr rp_16_s = new read_pixel_procPtr() {	public int handler(osd_bitmap bitmap,int x,int y) {
        throw new UnsupportedOperationException("Unsupported rp_16_s");
    }};
/*TODO*///static int rp_16_fx_s(struct osd_bitmap *b,int x,int y)  { return ((unsigned short *)b->line[x])[b->width-1-y]; }
       public static read_pixel_procPtr rp_16_fx_s = new read_pixel_procPtr() {	public int handler(osd_bitmap bitmap,int x,int y) {
        throw new UnsupportedOperationException("Unsupported rp_16_fx_s");
    }};
/*TODO*///static int rp_16_fy_s(struct osd_bitmap *b,int x,int y)  { return ((unsigned short *)b->line[b->height-1-x])[y]; }
       public static read_pixel_procPtr rp_16_fy_s = new read_pixel_procPtr() {	public int handler(osd_bitmap bitmap,int x,int y) {
        throw new UnsupportedOperationException("Unsupported rp_16_fy_s");
    }};
/*TODO*///static int rp_16_fxy_s(struct osd_bitmap *b,int x,int y)  { return ((unsigned short *)b->line[b->height-1-x])[b->width-1-y]; }
      public static read_pixel_procPtr rp_16_fxy_s = new read_pixel_procPtr() {	public int handler(osd_bitmap bitmap,int x,int y) {
        throw new UnsupportedOperationException("Unsupported rp_16_fxy_s");
    }};


    public static plot_pixel_procPtr pps_8_nd[] =
                    { pp_8_nd, 	 pp_8_nd_fx,   pp_8_nd_fy, 	 pp_8_nd_fxy,
                      pp_8_nd_s, pp_8_nd_fx_s, pp_8_nd_fy_s, pp_8_nd_fxy_s };

    public static plot_pixel_procPtr pps_8_d[] =
                    { pp_8_d, 	pp_8_d_fx,   pp_8_d_fy,	  pp_8_d_fxy,
                      pp_8_d_s, pp_8_d_fx_s, pp_8_d_fy_s, pp_8_d_fxy_s };

    public static plot_pixel_procPtr pps_16_nd[] =
                    { pp_16_nd,   pp_16_nd_fx,   pp_16_nd_fy, 	pp_16_nd_fxy,
                      pp_16_nd_s, pp_16_nd_fx_s, pp_16_nd_fy_s, pp_16_nd_fxy_s };

    public static plot_pixel_procPtr pps_16_d[] =
                    { pp_16_d,   pp_16_d_fx,   pp_16_d_fy, 	 pp_16_d_fxy,
                      pp_16_d_s, pp_16_d_fx_s, pp_16_d_fy_s, pp_16_d_fxy_s };


    public static read_pixel_procPtr rps_8[] =
                    { rp_8,rp_8_fx,rp_8_fy,rp_8_fxy,
                      rp_8_s, rp_8_fx_s, rp_8_fy_s, rp_8_fxy_s
                    };

    public static read_pixel_procPtr rps_16[] =
                    { rp_16,   rp_16_fx,   rp_16_fy,   rp_16_fxy,
                      rp_16_s, rp_16_fx_s, rp_16_fy_s, rp_16_fxy_s };


    public static void set_pixel_functions()
    {
	if (Machine.color_depth == 8)
	{         
		read_pixel = rps_8[Machine.orientation];
		if ((Machine.drv.video_attributes & VIDEO_SUPPORTS_DIRTY)!=0)
			plot_pixel = pps_8_d[Machine.orientation];
		else
			plot_pixel = pps_8_nd[Machine.orientation];
	}
	else
	{
		read_pixel = rps_16[Machine.orientation];

		if ((Machine.drv.video_attributes & VIDEO_SUPPORTS_DIRTY)!=0)
			plot_pixel = pps_16_d[Machine.orientation];
		else
			plot_pixel = pps_16_nd[Machine.orientation];
	}
    }

}
