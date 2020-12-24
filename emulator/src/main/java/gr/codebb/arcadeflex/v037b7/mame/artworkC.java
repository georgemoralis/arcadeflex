/**
 * ported to 0.37b7
 */
package gr.codebb.arcadeflex.v037b7.mame;

import gr.codebb.arcadeflex.v036.mame.osdependH.osd_bitmap;
import gr.codebb.arcadeflex.v037b7.mame.artworkH.*;


public class artworkC {

    /* the backdrop instance */
    public static artwork_info artwork_backdrop = null;

    /* the overlay instance */
    public static artwork_info artwork_overlay = null;

    public static osd_bitmap artwork_real_scrbitmap = null;

    /*TODO*///static void brightness_update (struct artwork_info *a)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	UINT8 rgb[3];
/*TODO*///	UINT16 *pens = Machine->pens;
/*TODO*///
/*TODO*///	/* Calculate brightness of all colors */
/*TODO*///	if (Machine->scrbitmap->depth == 8)
/*TODO*///		i = MIN(256, Machine->drv->total_colors);
/*TODO*///	else
/*TODO*///		i = MIN(32768, Machine->drv->total_colors);
/*TODO*///
/*TODO*///	while (--i >= 0)
/*TODO*///	{
/*TODO*///		osd_get_pen (pens[i], &rgb[0], &rgb[1], &rgb[2]);
/*TODO*///		a->brightness[pens[i]]=(222*rgb[0]+707*rgb[1]+71*rgb[2])/1000;
/*TODO*///	}
/*TODO*///}
/*TODO*///
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
/*TODO*///static UINT8 *create_15bit_palette ( void )
/*TODO*///{
/*TODO*///	int r, g, b;
/*TODO*///	UINT8 *palette, *tmp;
/*TODO*///
/*TODO*///	if ((palette = malloc (3 * 32768)) == 0)
/*TODO*///		return 0;
/*TODO*///
/*TODO*///	tmp = palette;
/*TODO*///	for (r = 0; r < 32; r++)
/*TODO*///		for (g = 0; g < 32; g++)
/*TODO*///			for (b = 0; b < 32; b++)
/*TODO*///			{
/*TODO*///				*tmp++ = (r << 3) | (r >> 2);
/*TODO*///				*tmp++ = (g << 3) | (g >> 2);
/*TODO*///				*tmp++ = (b << 3) | (b >> 2);
/*TODO*///			}
/*TODO*///	return palette;
/*TODO*///}
/*TODO*///
/*TODO*///static int get_new_pen (struct artwork_info *a, int r, int g, int b, int alpha)
/*TODO*///{
/*TODO*///	int pen;
/*TODO*///
/*TODO*///	/* look if the color is already in the palette */
/*TODO*///	if (Machine->scrbitmap->depth == 8)
/*TODO*///	{
/*TODO*///		pen =0;
/*TODO*///		while ((pen < a->num_pens_used) &&
/*TODO*///			   ((r != a->orig_palette[3*pen]) ||
/*TODO*///				(g != a->orig_palette[3*pen+1]) ||
/*TODO*///				(b != a->orig_palette[3*pen+2]) ||
/*TODO*///				((alpha < 255) && (alpha != a->transparency[pen]))))
/*TODO*///			pen++;
/*TODO*///
/*TODO*///		if (pen == a->num_pens_used)
/*TODO*///		{
/*TODO*///			a->orig_palette[3*pen]=r;
/*TODO*///			a->orig_palette[3*pen+1]=g;
/*TODO*///			a->orig_palette[3*pen+2]=b;
/*TODO*///			a->num_pens_used++;
/*TODO*///			if (alpha < 255)
/*TODO*///			{
/*TODO*///				a->transparency[pen]=alpha;
/*TODO*///				a->num_pens_trans++;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///		pen = ((r & 0xf8) << 7) | ((g & 0xf8) << 2) | (b >> 3);
/*TODO*///
/*TODO*///	return pen;
/*TODO*///}
/*TODO*///
/*TODO*///static void merge_cmy(struct artwork_info *a, struct osd_bitmap *source, struct osd_bitmap *source_alpha,int sx, int sy)
/*TODO*///{
/*TODO*///	int c1, c2, m1, m2, y1, y2, pen1, pen2, max, alpha;
/*TODO*///	int x, y, w, h;
/*TODO*///	struct osd_bitmap *dest, *dest_alpha;
/*TODO*///
/*TODO*///	dest = a->orig_artwork;
/*TODO*///	dest_alpha = a->alpha;
/*TODO*///
/*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///	{
/*TODO*///		w = source->height;
/*TODO*///		h = source->width;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		h = source->height;
/*TODO*///		w = source->width;
/*TODO*///	}
/*TODO*///
/*TODO*///	for (y = 0; y < h; y++)
/*TODO*///		for (x = 0; x < w; x++)
/*TODO*///		{
/*TODO*///			pen1 = read_pixel(dest, sx + x, sy + y);
/*TODO*///
/*TODO*///			c1 = 0xff - a->orig_palette[3*pen1];
/*TODO*///			m1 = 0xff - a->orig_palette[3*pen1+1];
/*TODO*///			y1 = 0xff - a->orig_palette[3*pen1+2];
/*TODO*///
/*TODO*///			pen2 = read_pixel(source, x, y);
/*TODO*///			c2 = 0xff - a->orig_palette[3*pen2] + c1;
/*TODO*///			m2 = 0xff - a->orig_palette[3*pen2+1] + m1;
/*TODO*///			y2 = 0xff - a->orig_palette[3*pen2+2] + y1;
/*TODO*///
/*TODO*///			max = MAX(c2, MAX(m2, y2));
/*TODO*///			if (max > 0xff)
/*TODO*///			{
/*TODO*///				c2 = (c2 * 0xf8) / max;
/*TODO*///				m2 = (m2 * 0xf8) / max;
/*TODO*///				y2 = (y2 * 0xf8) / max;
/*TODO*///			}
/*TODO*///
/*TODO*///			alpha = MIN (0xff, read_pixel(source_alpha, x, y)
/*TODO*///						 + read_pixel(dest_alpha, sx + x, sy + y));
/*TODO*///			plot_pixel(dest, sx + x, sy + y, get_new_pen(a, 0xff - c2, 0xff - m2, 0xff - y2, alpha));
/*TODO*///			plot_pixel(dest_alpha, sx + x, sy + y, alpha);
/*TODO*///		}
/*TODO*///}
/*TODO*///
/*TODO*////*********************************************************************
/*TODO*///  allocate_artwork_mem
/*TODO*///
/*TODO*///  Allocates memory for all the bitmaps.
/*TODO*/// *********************************************************************/
/*TODO*///static void allocate_artwork_mem (int width, int height, struct artwork_info **a)
/*TODO*///{
/*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///	{
/*TODO*///		int temp;
/*TODO*///
/*TODO*///		temp = height;
/*TODO*///		height = width;
/*TODO*///		width = temp;
/*TODO*///	}
/*TODO*///
/*TODO*///	*a = (struct artwork_info *)malloc(sizeof(struct artwork_info));
/*TODO*///	if (*a == 0)
/*TODO*///	{
/*TODO*///		logerror("Not enough memory for artwork!\n");
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	(*a)->transparency = NULL;
/*TODO*///	(*a)->orig_palette = NULL;
/*TODO*///	(*a)->pTable = NULL;
/*TODO*///	(*a)->brightness = NULL;
/*TODO*///
/*TODO*///	if (((*a)->orig_artwork = bitmap_alloc(width, height)) == 0)
/*TODO*///	{
/*TODO*///		logerror("Not enough memory for artwork!\n");
/*TODO*///		artwork_free(a);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	fillbitmap((*a)->orig_artwork,0,0);
/*TODO*///
/*TODO*///	if (((*a)->alpha = bitmap_alloc(width, height)) == 0)
/*TODO*///	{
/*TODO*///		logerror("Not enough memory for artwork!\n");
/*TODO*///		artwork_free(a);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	fillbitmap((*a)->alpha,0,0);
/*TODO*///
/*TODO*///	if (((*a)->artwork = bitmap_alloc(width,height)) == 0)
/*TODO*///	{
/*TODO*///		logerror("Not enough memory for artwork!\n");
/*TODO*///		artwork_free(a);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (((*a)->artwork1 = bitmap_alloc(width,height)) == 0)
/*TODO*///	{
/*TODO*///		logerror("Not enough memory for artwork!\n");
/*TODO*///		artwork_free(a);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (((*a)->pTable = (UINT8*)malloc(256*256))==0)
/*TODO*///	{
/*TODO*///		logerror("Not enough memory.\n");
/*TODO*///		artwork_free(a);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (((*a)->brightness = (UINT8*)malloc(256*256))==0)
/*TODO*///	{
/*TODO*///		logerror("Not enough memory.\n");
/*TODO*///		artwork_free(a);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	memset ((*a)->brightness, 0, 256*256);
/*TODO*///
/*TODO*///	if (((*a)->rgb = (UINT64*)malloc(width*height*sizeof(UINT64)))==0)
/*TODO*///	{
/*TODO*///		logerror("Not enough memory.\n");
/*TODO*///		artwork_free(a);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////*********************************************************************
/*TODO*///  create_disk
/*TODO*///
/*TODO*///  Creates a disk with radius r in the color of pen. A new bitmap
/*TODO*///  is allocated for the disk.
/*TODO*///
/*TODO*///*********************************************************************/
/*TODO*///static struct osd_bitmap *create_disk (int r, int fg, int bg)
/*TODO*///{
/*TODO*///	struct osd_bitmap *disk;
/*TODO*///
/*TODO*///	int x = 0, twox = 0;
/*TODO*///	int y = r;
/*TODO*///	int twoy = r+r;
/*TODO*///	int p = 1 - r;
/*TODO*///	int i;
/*TODO*///
/*TODO*///	if ((disk = bitmap_alloc(twoy, twoy)) == 0)
/*TODO*///	{
/*TODO*///		logerror("Not enough memory for artwork!\n");
/*TODO*///		return NULL;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* background */
/*TODO*///	fillbitmap (disk, bg, 0);
/*TODO*///
/*TODO*///	while (x < y)
/*TODO*///	{
/*TODO*///		x++;
/*TODO*///		twox +=2;
/*TODO*///		if (p < 0)
/*TODO*///			p += twox + 1;
/*TODO*///		else
/*TODO*///		{
/*TODO*///			y--;
/*TODO*///			twoy -= 2;
/*TODO*///			p += twox - twoy + 1;
/*TODO*///		}
/*TODO*///
/*TODO*///		for (i = 0; i < twox; i++)
/*TODO*///		{
/*TODO*///			plot_pixel(disk, r-x+i, r-y  , fg);
/*TODO*///			plot_pixel(disk, r-x+i, r+y-1, fg);
/*TODO*///		}
/*TODO*///
/*TODO*///		for (i = 0; i < twoy; i++)
/*TODO*///		{
/*TODO*///			plot_pixel(disk, r-y+i, r-x  , fg);
/*TODO*///			plot_pixel(disk, r-y+i, r+x-1, fg);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return disk;
/*TODO*///}
/*TODO*///
/*TODO*////*********************************************************************
/*TODO*///  transparency_hist
/*TODO*///
/*TODO*///  Calculates a histogram of all transparent pixels in the overlay.
/*TODO*///  The function returns a array of ints with the number of shades
/*TODO*///  for each transparent color based on the color histogram.
/*TODO*/// *********************************************************************/
/*TODO*///static unsigned int *transparency_hist (struct artwork_info *a, int num_shades)
/*TODO*///{
/*TODO*///	int i, j;
/*TODO*///	unsigned int *hist;
/*TODO*///	int num_pix=0, min_shades;
/*TODO*///	UINT8 pen;
/*TODO*///
/*TODO*///	if ((hist = (unsigned int *)malloc(a->num_pens_trans*sizeof(unsigned int)))==NULL)
/*TODO*///	{
/*TODO*///		logerror("Not enough memory!\n");
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
/*TODO*///				pen = ((UINT16 *)a->orig_artwork->line[j])[i];
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
/*TODO*///	if (min_shades==0)
/*TODO*///		logerror("Too many colors in overlay. Vector colors may be wrong.\n");
/*TODO*///
/*TODO*///	num_pix /= num_shades-(a->num_pens_used-a->num_pens_trans)
/*TODO*///		-min_shades*a->num_pens_trans;
/*TODO*///
/*TODO*///	if (num_pix)
/*TODO*///		for (i=0; i < a->num_pens_trans; i++)
/*TODO*///			hist[i] = hist[i]/num_pix + min_shades;
/*TODO*///
/*TODO*///	return hist;
/*TODO*///}
/*TODO*///
/*TODO*////*********************************************************************
/*TODO*///  load_palette
/*TODO*///
/*TODO*///  This sets the palette colors used by the backdrop to the new colors
/*TODO*///  passed in as palette.  The values should be stored as one byte of red,
/*TODO*///  one byte of blue, one byte of green.  This could hopefully be used
/*TODO*///  for special effects, like lightening and darkening the backdrop.
/*TODO*/// *********************************************************************/
/*TODO*///static void load_palette(struct artwork_info *a, UINT8 *palette)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///	/* Load colors into the palette */
/*TODO*///	if ((Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE))
/*TODO*///	{
/*TODO*///		for (i = 0; i < a->num_pens_used; i++)
/*TODO*///			palette_change_color(i + a->start_pen, palette[i*3], palette[i*3+1], palette[i*3+2]);
/*TODO*///
/*TODO*///		palette_recalc();
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////*********************************************************************
/*TODO*///
/*TODO*///  Reads a PNG for a artwork struct and checks if it has the right
/*TODO*///  format.
/*TODO*///
/*TODO*/// *********************************************************************/
/*TODO*///static int decode_png(const char *file_name, struct osd_bitmap **bitmap, struct osd_bitmap **alpha, struct png_info *p)
/*TODO*///{
/*TODO*///	UINT8 *tmp;
/*TODO*///	int x, y, pen;
/*TODO*///	void *fp;
/*TODO*///	int file_name_len;
/*TODO*///	char file_name2[256];
/*TODO*///
/*TODO*///	/* check for .png */
/*TODO*///	strcpy(file_name2, file_name);
/*TODO*///	file_name_len = strlen(file_name2);
/*TODO*///	if ((file_name_len < 4) || stricmp(&file_name2[file_name_len - 4], ".png"))
/*TODO*///	{
/*TODO*///		strcat(file_name2, ".png");
/*TODO*///	}
/*TODO*///
/*TODO*///	if (!(fp = osd_fopen(Machine->gamedrv->name, file_name2, OSD_FILETYPE_ARTWORK, 0)))
/*TODO*///	{
/*TODO*///		logerror("Unable to open PNG %s\n", file_name);
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
/*TODO*///		logerror("Unsupported bit depth %i (8 bit max.)\n", p->bit_depth);
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (p->interlace_method != 0)
/*TODO*///	{
/*TODO*///		logerror("Interlace unsupported\n");
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (Machine->scrbitmap->depth == 8 && p->color_type != 3)
/*TODO*///	{
/*TODO*///		logerror("Use 8bit artwork for 8bpp modes. Artwork disabled.\n");
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	switch (p->color_type)
/*TODO*///	{
/*TODO*///	case 3:
/*TODO*///		/* Convert to 8 bit */
/*TODO*///		png_expand_buffer_8bit (p);
/*TODO*///
/*TODO*///		png_delete_unused_colors (p);
/*TODO*///
/*TODO*///		if ((*bitmap = bitmap_alloc(p->width,p->height)) == 0)
/*TODO*///		{
/*TODO*///			logerror("Unable to allocate memory for artwork\n");
/*TODO*///			return 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		tmp = p->image;
/*TODO*///		if ((*bitmap)->depth == 8)
/*TODO*///		{
/*TODO*///			for (y=0; y<p->height; y++)
/*TODO*///				for (x=0; x<p->width; x++)
/*TODO*///				{
/*TODO*///					plot_pixel(*bitmap, x, y, *tmp++);
/*TODO*///				}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			/* convert to 15bit */
/*TODO*///			if (p->num_trans > 0)
/*TODO*///				if ((*alpha = bitmap_alloc(p->width,p->height)) == 0)
/*TODO*///				{
/*TODO*///					logerror("Unable to allocate memory for artwork\n");
/*TODO*///					return 0;
/*TODO*///				}
/*TODO*///
/*TODO*///			for (y=0; y<p->height; y++)
/*TODO*///				for (x=0; x<p->width; x++)
/*TODO*///				{
/*TODO*///					pen = ((p->palette[*tmp * 3] & 0xf8) << 7) | ((p->palette[*tmp * 3 + 1] & 0xf8) << 2) | (p->palette[*tmp * 3 + 2] >> 3);
/*TODO*///					plot_pixel(*bitmap, x, y, pen);
/*TODO*///
/*TODO*///					if (p->num_trans > 0)
/*TODO*///					{
/*TODO*///						if (*tmp < p->num_trans)
/*TODO*///							plot_pixel(*alpha, x, y, p->trans[*tmp]);
/*TODO*///						else
/*TODO*///							plot_pixel(*alpha, x, y, 255);
/*TODO*///					}
/*TODO*///					tmp++;
/*TODO*///				}
/*TODO*///
/*TODO*///			free (p->palette);
/*TODO*///
/*TODO*///			/* create 15 bit palette */
/*TODO*///			if ((p->palette = create_15bit_palette()) == 0)
/*TODO*///			{
/*TODO*///				logerror("Unable to allocate memory for artwork\n");
/*TODO*///				return 0;
/*TODO*///			}
/*TODO*///			p->num_palette = 32768;
/*TODO*///		}
/*TODO*///		break;
/*TODO*///
/*TODO*///	case 6:
/*TODO*///		if ((*alpha = bitmap_alloc(p->width,p->height)) == 0)
/*TODO*///		{
/*TODO*///			logerror("Unable to allocate memory for artwork\n");
/*TODO*///			return 0;
/*TODO*///		}
/*TODO*///
/*TODO*///	case 2:
/*TODO*///		if ((*bitmap = bitmap_alloc(p->width,p->height)) == 0)
/*TODO*///		{
/*TODO*///			logerror("Unable to allocate memory for artwork\n");
/*TODO*///			return 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		/* create 15 bit palette */
/*TODO*///		if ((p->palette = create_15bit_palette()) == 0)
/*TODO*///		{
/*TODO*///			logerror("Unable to allocate memory for artwork\n");
/*TODO*///			return 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		p->num_palette = 32768;
/*TODO*///		p->trans = NULL;
/*TODO*///		p->num_trans = 0;
/*TODO*///
/*TODO*///		/* reduce true color to 15 bit */
/*TODO*///		tmp = p->image;
/*TODO*///		for (y=0; y<p->height; y++)
/*TODO*///			for (x=0; x<p->width; x++)
/*TODO*///			{
/*TODO*///				pen = ((tmp[0] & 0xf8) << 7) | ((tmp[1] & 0xf8) << 2) | (tmp[2] >> 3);
/*TODO*///				plot_pixel(*bitmap, x, y, pen);
/*TODO*///
/*TODO*///				if (p->color_type == 6)
/*TODO*///				{
/*TODO*///					plot_pixel(*alpha, x, y, tmp[3]);
/*TODO*///					tmp += 4;
/*TODO*///				}
/*TODO*///				else
/*TODO*///					tmp += 3;
/*TODO*///			}
/*TODO*///
/*TODO*///		break;
/*TODO*///
/*TODO*///	default:
/*TODO*///		logerror("Unsupported color type %i \n", p->color_type);
/*TODO*///		return 0;
/*TODO*///		break;
/*TODO*///	}
/*TODO*///	free (p->image);
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*////*********************************************************************
/*TODO*///  load_png
/*TODO*///
/*TODO*///  This is what loads your backdrop in from disk.
/*TODO*///  start_pen = the first pen available for the backdrop to use
/*TODO*///  max_pens = the number of pens the backdrop can use
/*TODO*///  So, for example, suppose you want to load "dotron.png", have it
/*TODO*///  start with color 192, and only use 48 pens.  You would call
/*TODO*///  backdrop = backdrop_load("dotron.png",192,48);
/*TODO*/// *********************************************************************/
/*TODO*///
/*TODO*///static void load_png(const char *filename, unsigned int start_pen, unsigned int max_pens,
/*TODO*///					 int width, int height, struct artwork_info **a)
/*TODO*///{
/*TODO*///	struct osd_bitmap *picture = 0, *alpha = 0;
/*TODO*///	struct png_info p;
/*TODO*///	int scalex, scaley;
/*TODO*///
/*TODO*///	/* If the user turned artwork off, bail */
/*TODO*///	if (!options.use_artwork) return;
/*TODO*///
/*TODO*///	if (!decode_png(filename, &picture, &alpha, &p))
/*TODO*///		return;
/*TODO*///
/*TODO*///	allocate_artwork_mem(width, height, a);
/*TODO*///
/*TODO*///	if (*a==NULL)
/*TODO*///		return;
/*TODO*///
/*TODO*///	(*a)->start_pen = start_pen;
/*TODO*///
/*TODO*///	(*a)->num_pens_used = p.num_palette;
/*TODO*///	(*a)->num_pens_trans = p.num_trans;
/*TODO*///	(*a)->orig_palette = p.palette;
/*TODO*///	(*a)->transparency = p.trans;
/*TODO*///
/*TODO*///	/* Make sure we don't have too many colors */
/*TODO*///	if ((*a)->num_pens_used > max_pens)
/*TODO*///	{
/*TODO*///		logerror("Too many colors in artwork.\n");
/*TODO*///		logerror("Colors found: %d  Max Allowed: %d\n",
/*TODO*///				 (*a)->num_pens_used,max_pens);
/*TODO*///		artwork_free(a);
/*TODO*///		bitmap_free(picture);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* Scale the original picture to be the same size as the visible area */
/*TODO*///	scalex = 0x10000 * picture->width  / (*a)->orig_artwork->width;
/*TODO*///	scaley = 0x10000 * picture->height / (*a)->orig_artwork->height;
/*TODO*///
/*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///	{
/*TODO*///		int tmp;
/*TODO*///		tmp = scalex;
/*TODO*///		scalex = scaley;
/*TODO*///		scaley = tmp;
/*TODO*///	}
/*TODO*///
/*TODO*///	copyrozbitmap((*a)->orig_artwork, picture, 0, 0, scalex, 0, 0, scaley, 0, 0, TRANSPARENCY_NONE, 0, 0);
/*TODO*///	/* We don't need the original any more */
/*TODO*///	bitmap_free(picture);
/*TODO*///
/*TODO*///	if (alpha)
/*TODO*///	{
/*TODO*///		copyrozbitmap((*a)->alpha, alpha, 0, 0, scalex, 0, 0, scaley, 0, 0, TRANSPARENCY_NONE, 0, 0);
/*TODO*///		bitmap_free(alpha);
/*TODO*///	}
/*TODO*///
/*TODO*///	/* If the game uses dynamic colors, we assume that it's safe
/*TODO*///	   to init the palette and remap the colors now */
/*TODO*///	if (Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE)
/*TODO*///	{
/*TODO*///		load_palette(*a,(*a)->orig_palette);
/*TODO*///		backdrop_refresh(*a);
/*TODO*///	}
/*TODO*///
/*TODO*///}
/*TODO*///
/*TODO*///static void load_png_fit(const char *filename, unsigned int start_pen, unsigned int max_pens, struct artwork_info **a)
/*TODO*///{
/*TODO*///	load_png(filename, start_pen, max_pens, Machine->scrbitmap->width, Machine->scrbitmap->height, a);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////*********************************************************************
/*TODO*///  backdrop_refresh
/*TODO*///
/*TODO*///  This remaps the "original" palette indexes to the abstract OS indexes
/*TODO*///  used by MAME.  This needs to be called every time palette_recalc
/*TODO*///  returns a non-zero value, since the mappings will have changed.
/*TODO*/// *********************************************************************/
/*TODO*///
/*TODO*///void backdrop_refresh(struct artwork_info *a)
/*TODO*///{
/*TODO*///	int i, j, height,width, offset;
/*TODO*///	struct osd_bitmap *back, *orig;
/*TODO*///
/*TODO*///	offset = a->start_pen;
/*TODO*///	back = a->artwork;
/*TODO*///	orig = a->orig_artwork;
/*TODO*///	height = a->artwork->height;
/*TODO*///	width = a->artwork->width;
/*TODO*///
/*TODO*///	if (back->depth == 8)
/*TODO*///	{
/*TODO*///		for ( j = 0; j < height; j++)
/*TODO*///			for (i = 0; i < width; i++)
/*TODO*///				back->line[j][i] = Machine->pens[orig->line[j][i] + offset];
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		for ( j = 0; j < height; j++)
/*TODO*///			for (i = 0; i < width; i++)
/*TODO*///				((UINT16 *)back->line[j])[i] = Machine->pens[((UINT16 *)orig->line[j])[i] + offset];
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void backdrop_remap(void)
/*TODO*///{
/*TODO*///	backdrop_refresh(artwork_backdrop);
/*TODO*///	brightness_update (artwork_backdrop);
/*TODO*///}
/*TODO*///
/*TODO*////*********************************************************************
/*TODO*///  overlay_remap
/*TODO*///
/*TODO*///  This remaps the "original" palette indexes to the abstract OS indexes
/*TODO*///  used by MAME. The alpha channel is also taken into account.
/*TODO*/// *********************************************************************/
/*TODO*///static void overlay_remap(void)
/*TODO*///{
/*TODO*///	int i,j;
/*TODO*///	UINT8 r,g,b;
/*TODO*///	float h, s, v, rf, gf, bf;
/*TODO*///	int offset, height, width;
/*TODO*///	struct osd_bitmap *overlay, *overlay1, *orig;
/*TODO*///
/*TODO*///	offset = artwork_overlay->start_pen;
/*TODO*///	height = artwork_overlay->artwork->height;
/*TODO*///	width = artwork_overlay->artwork->width;
/*TODO*///	overlay = artwork_overlay->artwork;
/*TODO*///	overlay1 = artwork_overlay->artwork1;
/*TODO*///	orig = artwork_overlay->orig_artwork;
/*TODO*///
/*TODO*///	if (overlay->depth == 8)
/*TODO*///	{
/*TODO*///		for ( j=0; j<height; j++)
/*TODO*///			for (i=0; i<width; i++)
/*TODO*///				overlay->line[j][i] = Machine->pens[orig->line[j][i]+offset];
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		if (artwork_overlay->alpha)
/*TODO*///		{
/*TODO*///			for ( j=0; j<height; j++)
/*TODO*///				for (i=0; i<width; i++)
/*TODO*///				{
/*TODO*///					UINT64 v1,v2;
/*TODO*///					UINT16 alpha = ((UINT16 *)artwork_overlay->alpha->line[j])[i];
/*TODO*///
/*TODO*///					osd_get_pen (Machine->pens[((UINT16 *)orig->line[j])[i]+offset], &r, &g, &b);
/*TODO*///					v1 = MAX(r, MAX(g, b));
/*TODO*///					v2 = (v1 * alpha) / 255;
/*TODO*///					artwork_overlay->rgb[j*width+i] = (v1 << 32) | (v2 << 24) | ((UINT64)r << 16) |
/*TODO*///													  ((UINT64)g << 8) | (UINT64)b;
/*TODO*///
/*TODO*///					RGBtoHSV( r/255.0, g/255.0, b/255.0, &h, &s, &v );
/*TODO*///
/*TODO*///					HSVtoRGB( &rf, &gf, &bf, h, s, v * alpha/255.0);
/*TODO*///					r = rf*255; g = gf*255; b = bf*255;
/*TODO*///					((UINT16 *)overlay->line[j])[i] = Machine->pens[(((r & 0xf8) << 7) | ((g & 0xf8) << 2) | (b >> 3)) + artwork_overlay->start_pen];
/*TODO*///
/*TODO*///					HSVtoRGB( &rf, &gf, &bf, h, s, 1);
/*TODO*///					r = rf*255; g = gf*255; b = bf*255;
/*TODO*///					((UINT16 *)overlay1->line[j])[i] = Machine->pens[(((r & 0xf8) << 7) | ((g & 0xf8) << 2) | (b >> 3)) + artwork_overlay->start_pen];
/*TODO*///				}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			for ( j=0; j<height; j++)
/*TODO*///				for (i=0; i<width; i++)
/*TODO*///					((UINT16 *)overlay->line[j])[i] = Machine->pens[((UINT16 *)orig->line[j])[i]+offset];
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* Calculate brightness of all colors */
/*TODO*///	brightness_update(artwork_overlay);
/*TODO*///}
/*TODO*///
    public static void artwork_remap() {
        /*TODO*///	if (artwork_backdrop) backdrop_remap();
/*TODO*///	if (artwork_overlay) overlay_remap();
    }

    /*TODO*///
/*TODO*////*********************************************************************
/*TODO*///  overlay_draw
/*TODO*///
/*TODO*///  Supports different levels of intensity on the screen and different
/*TODO*///  levels of transparancy of the overlay (only in 16 bpp modes).
/*TODO*/// *********************************************************************/
/*TODO*///
    public static void overlay_draw(osd_bitmap dest, osd_bitmap source) {
        /*TODO*///	int i,j;
/*TODO*///	int height,width;
/*TODO*///
/*TODO*///	/* the colors could have changed so update the brightness table */
/*TODO*///	if (Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE)
/*TODO*///		brightness_update(artwork_overlay);
/*TODO*///
/*TODO*///	height = artwork_overlay->artwork->height;
/*TODO*///	width = artwork_overlay->artwork->width;
/*TODO*///
/*TODO*///	if (dest->depth == 8)
/*TODO*///	{
/*TODO*///		if (Machine->drv->video_attributes & VIDEO_TYPE_VECTOR)
/*TODO*///		{
/*TODO*///			UINT8 *dst, *ovr, *src;
/*TODO*///			UINT8 *bright = artwork_overlay->brightness;
/*TODO*///			UINT8 *tab = artwork_overlay->pTable;
/*TODO*///			int bp;
/*TODO*///
/*TODO*///			copybitmap(dest, artwork_overlay->artwork ,0,0,0,0,NULL,TRANSPARENCY_NONE,0);
/*TODO*///			for ( j = 0; j < height; j++)
/*TODO*///			{
/*TODO*///				dst = dest->line[j];
/*TODO*///				src = source->line[j];
/*TODO*///				ovr = artwork_overlay->orig_artwork->line[j];
/*TODO*///				for (i = 0; i < width; i++)
/*TODO*///				{
/*TODO*///					bp = bright[*src++];
/*TODO*///					if (bp > 0)
/*TODO*///						dst[i] = Machine->pens[tab[(ovr[i] << 8) + bp]];
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else /* !VECTOR */
/*TODO*///		{
/*TODO*///			UINT8 *dst, *ovr, *src;
/*TODO*///			int black = Machine->pens[0];
/*TODO*///
/*TODO*///			for ( j = 0; j < height; j++)
/*TODO*///			{
/*TODO*///				dst = dest->line[j];
/*TODO*///				src = source->line[j];
/*TODO*///				ovr = artwork_overlay->artwork->line[j];
/*TODO*///				for (i = width; i > 0; i--)
/*TODO*///				{
/*TODO*///					if (*src!=black)
/*TODO*///						*dst = *ovr;
/*TODO*///					else
/*TODO*///						*dst = black;
/*TODO*///					dst++;
/*TODO*///					src++;
/*TODO*///					ovr++;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else /* 16 bit */
/*TODO*///	{
/*TODO*///		if (artwork_overlay->start_pen == 2)
/*TODO*///		{
/*TODO*///			/* fast version */
/*TODO*///			UINT16 *dst, *bg, *fg, *src;
/*TODO*///			int black = Machine->pens[0];
/*TODO*///
/*TODO*///			height = artwork_overlay->artwork->height;
/*TODO*///			width = artwork_overlay->artwork->width;
/*TODO*///
/*TODO*///			for ( j = 0; j < height; j++)
/*TODO*///			{
/*TODO*///				dst = (UINT16 *)dest->line[j];
/*TODO*///				src = (UINT16 *)source->line[j];
/*TODO*///				bg = (UINT16 *)artwork_overlay->artwork->line[j];
/*TODO*///				fg = (UINT16 *)artwork_overlay->artwork1->line[j];
/*TODO*///				for (i = width; i > 0; i--)
/*TODO*///				{
/*TODO*///					if (*src!=black)
/*TODO*///						*dst = *fg;
/*TODO*///					else
/*TODO*///						*dst = *bg;
/*TODO*///					dst++;
/*TODO*///					src++;
/*TODO*///					fg++;
/*TODO*///					bg++;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else /* slow version */
/*TODO*///		{
/*TODO*///			UINT16 *src, *dst;
/*TODO*///			UINT64 *rgb = artwork_overlay->rgb;
/*TODO*///			UINT8 *bright = artwork_overlay->brightness;
/*TODO*///			unsigned short *pens = &Machine->pens[artwork_overlay->start_pen];
/*TODO*///
/*TODO*///			copybitmap(dest, artwork_overlay->artwork ,0,0,0,0,NULL,TRANSPARENCY_NONE,0);
/*TODO*///
/*TODO*///			for ( j = 0; j < height; j++)
/*TODO*///			{
/*TODO*///				dst = (UINT16 *)dest->line[j];
/*TODO*///				src = (UINT16 *)source->line[j];
/*TODO*///				for (i = width; i > 0; i--)
/*TODO*///				{
/*TODO*///					int bp = bright[*src++];
/*TODO*///					if (bp > 0)
/*TODO*///					{
/*TODO*///						if (*rgb & 0x00ffffff)
/*TODO*///						{
/*TODO*///							int v = *rgb >> 32;
/*TODO*///							int vn =(*rgb >> 24) & 0xff;
/*TODO*///							UINT8 r = *rgb >> 16;
/*TODO*///							UINT8 g = *rgb >> 8;
/*TODO*///							UINT8 b = *rgb;
/*TODO*///
/*TODO*///							vn += ((255 - vn) * bp) / 255;
/*TODO*///							r = (r * vn) / v;
/*TODO*///							g = (g * vn) / v;
/*TODO*///							b = (b * vn) / v;
/*TODO*///							*dst = pens[(((r & 0xf8) << 7) | ((g & 0xf8) << 2) | (b >> 3))];
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							int vn =(*rgb >> 24) & 0xff;
/*TODO*///
/*TODO*///							vn += ((255 - vn) * bp) / 255;
/*TODO*///							*dst = pens[(((vn & 0xf8) << 7) | ((vn & 0xf8) << 2) | (vn >> 3))];
/*TODO*///						}
/*TODO*///					}
/*TODO*///					dst++;
/*TODO*///					rgb++;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
    }

    /*TODO*///
/*TODO*////*********************************************************************
/*TODO*///  backdrop_draw
/*TODO*///
/*TODO*///  Very simple, no translucency.
/*TODO*/// *********************************************************************/
/*TODO*///
/*TODO*///static void backdrop_draw(struct osd_bitmap *dest, struct osd_bitmap *source)
/*TODO*///{
/*TODO*///	int i, j;
/*TODO*///	UINT8 *brightness = artwork_backdrop->brightness;
/*TODO*///
/*TODO*///	/* the colors could have changed so update the brightness table */
/*TODO*///	if (Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE)
/*TODO*///		brightness_update(artwork_backdrop);
/*TODO*///
/*TODO*///	copybitmap(dest, artwork_backdrop->artwork ,0,0,0,0,NULL,TRANSPARENCY_NONE,0);
/*TODO*///
/*TODO*///	if (dest->depth == 8)
/*TODO*///	{
/*TODO*///		UINT8 *dst, *bdr, *src;
/*TODO*///
/*TODO*///		for ( j = 0; j < source->height; j++)
/*TODO*///		{
/*TODO*///			dst = dest->line[j];
/*TODO*///			src = source->line[j];
/*TODO*///			bdr = artwork_backdrop->artwork->line[j];
/*TODO*///			for (i = 0; i < source->width; i++)
/*TODO*///			{
/*TODO*///				if (brightness[*src] > brightness[*bdr])
/*TODO*/////				if (brightness[*src] > 10)
/*TODO*///					*dst = *src;
/*TODO*///				dst++;
/*TODO*///				src++;
/*TODO*///				bdr++;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		UINT16 *dst, *bdr, *src;
/*TODO*///
/*TODO*///		for ( j = 0; j < source->height; j++)
/*TODO*///		{
/*TODO*///			dst = (UINT16 *)dest->line[j];
/*TODO*///			src = (UINT16 *)source->line[j];
/*TODO*///			bdr = (UINT16 *)artwork_backdrop->artwork->line[j];
/*TODO*///			for (i = 0; i < source->width; i++)
/*TODO*///			{
/*TODO*///				if (brightness[*src] > brightness[*bdr])
/*TODO*///					*dst = *src;
/*TODO*///				dst++;
/*TODO*///				src++;
/*TODO*///				bdr++;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
    public static void artwork_draw(osd_bitmap dest, osd_bitmap source, int _bitmap_dirty) {
        /*TODO*///	if (_bitmap_dirty)
/*TODO*///	{
/*TODO*///		artwork_remap();
/*TODO*///		osd_mark_dirty (0, 0, dest->width-1, dest->height-1, 0);
/*TODO*///	}
/*TODO*///
/*TODO*///	if (artwork_backdrop) backdrop_draw(dest, source);
/*TODO*///	if (artwork_overlay) overlay_draw(dest, source);
    }

    /*TODO*///
/*TODO*////*********************************************************************
/*TODO*///  artwork_free
/*TODO*///
/*TODO*///  Don't forget to clean up when you're done with the backdrop!!!
/*TODO*/// *********************************************************************/
/*TODO*///
/*TODO*///void artwork_free(struct artwork_info **a)
/*TODO*///{
/*TODO*///	if (*a)
/*TODO*///	{
/*TODO*///		if ((*a)->artwork)
/*TODO*///			bitmap_free((*a)->artwork);
/*TODO*///		if ((*a)->artwork1)
/*TODO*///			bitmap_free((*a)->artwork1);
/*TODO*///		if ((*a)->alpha)
/*TODO*///			bitmap_free((*a)->alpha);
/*TODO*///		if ((*a)->orig_artwork)
/*TODO*///			bitmap_free((*a)->orig_artwork);
/*TODO*///		if ((*a)->orig_palette)
/*TODO*///			free ((*a)->orig_palette);
/*TODO*///		if ((*a)->transparency)
/*TODO*///			free ((*a)->transparency);
/*TODO*///		if ((*a)->brightness)
/*TODO*///			free ((*a)->brightness);
/*TODO*///		if ((*a)->rgb)
/*TODO*///			free ((*a)->rgb);
/*TODO*///		if ((*a)->pTable)
/*TODO*///			free ((*a)->pTable);
/*TODO*///		free(*a);
/*TODO*///
/*TODO*///		*a = NULL;
/*TODO*///	}
/*TODO*///}
/*TODO*///
    public static void artwork_kill() {
        /*TODO*///	if (artwork_backdrop || artwork_overlay)
/*TODO*///		bitmap_free(artwork_real_scrbitmap);
/*TODO*///
/*TODO*///	if (artwork_backdrop) artwork_free(&artwork_backdrop);
/*TODO*///	if (artwork_overlay) artwork_free(&artwork_overlay);
    }
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
/*TODO*///int overlay_set_palette (UINT8 *palette, int num_shades)
/*TODO*///{
/*TODO*///	unsigned int i,j, shades = 0, step;
/*TODO*///	unsigned int *hist;
/*TODO*///	float h, s, v, r, g, b;
/*TODO*///
/*TODO*///	/* adjust palette start */
/*TODO*///
/*TODO*///	palette += 3 * artwork_overlay->start_pen;
/*TODO*///
/*TODO*///	if (Machine->scrbitmap->depth == 8)
/*TODO*///	{
/*TODO*///		if((hist = transparency_hist (artwork_overlay, num_shades))==NULL)
/*TODO*///			return 0;
/*TODO*///
/*TODO*///		/* Copy all artwork colors to the palette */
/*TODO*///		memcpy (palette, artwork_overlay->orig_palette, 3 * artwork_overlay->num_pens_used);
/*TODO*///
/*TODO*///		/* Fill the palette with shades of the transparent colors */
/*TODO*///		for (i = 0; i < artwork_overlay->num_pens_trans; i++)
/*TODO*///		{
/*TODO*///			RGBtoHSV( artwork_overlay->orig_palette[i * 3]/255.0,
/*TODO*///					  artwork_overlay->orig_palette[i * 3 + 1] / 255.0,
/*TODO*///					  artwork_overlay->orig_palette[i * 3 + 2] / 255.0, &h, &s, &v );
/*TODO*///
/*TODO*///			/* blend transparent entries with black background */
/*TODO*///			/* we don't need the original palette entry any more */
/*TODO*///			HSVtoRGB ( &r, &g, &b, h, s, v*artwork_overlay->transparency[i]/255.0);
/*TODO*///			palette [i * 3] = r * 255.0;
/*TODO*///			palette [i * 3 + 1] = g * 255.0;
/*TODO*///			palette [i * 3 + 2] = b * 255.0;
/*TODO*///			if (hist[i] > 1)
/*TODO*///			{
/*TODO*///				for (j = 0; j < hist[i] - 1; j++)
/*TODO*///				{
/*TODO*///					/* we start from 1 because the 0 level is already in the palette */
/*TODO*///					HSVtoRGB ( &r, &g, &b, h, s, v * artwork_overlay->transparency[i]/255.0 +
/*TODO*///							   ((1.0-(v*artwork_overlay->transparency[i]/255.0))*(j+1))/(hist[i]-1));
/*TODO*///					palette [(artwork_overlay->num_pens_used + shades + j) * 3] = r * 255.0;
/*TODO*///					palette [(artwork_overlay->num_pens_used + shades + j) * 3 + 1] = g * 255.0;
/*TODO*///					palette [(artwork_overlay->num_pens_used + shades + j) * 3 + 2] = b * 255.0;
/*TODO*///				}
/*TODO*///
/*TODO*///				/* create alpha LUT for quick alpha blending */
/*TODO*///				for (j = 0; j < 256; j++)
/*TODO*///				{
/*TODO*///					step = hist[i] * j / 256.0;
/*TODO*///					if (step == 0)
/*TODO*///						/* no beam, just overlay over black screen */
/*TODO*///						artwork_overlay->pTable[i * 256 + j] = i + artwork_overlay->start_pen;
/*TODO*///					else
/*TODO*///						artwork_overlay->pTable[i * 256 + j] = artwork_overlay->num_pens_used +
/*TODO*///															   shades + step - 1 + artwork_overlay->start_pen;
/*TODO*///				}
/*TODO*///				shades += hist[i] - 1;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///		memcpy (palette, artwork_overlay->orig_palette, 3 * artwork_overlay->num_pens_used);
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*///void overlay_load(const char *filename, unsigned int start_pen, unsigned int max_pens)
/*TODO*///{
/*TODO*///	int width, height;
/*TODO*///
/*TODO*///	/* replace the real display with a fake one, this way drivers can access Machine->scrbitmap
/*TODO*///	   the same way as before */
/*TODO*///
/*TODO*///	width = Machine->scrbitmap->width;
/*TODO*///	height = Machine->scrbitmap->height;
/*TODO*///
/*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///	{
/*TODO*///		int temp;
/*TODO*///
/*TODO*///		temp = height;
/*TODO*///		height = width;
/*TODO*///		width = temp;
/*TODO*///	}
/*TODO*///
/*TODO*///	load_png_fit(filename, start_pen, max_pens, &artwork_overlay);
/*TODO*///
/*TODO*///	if (artwork_overlay)
/*TODO*///	{
/*TODO*///		if ((artwork_real_scrbitmap = bitmap_alloc(width, height)) == 0)
/*TODO*///		{
/*TODO*///			artwork_kill();
/*TODO*///			logerror("Not enough memory for artwork!\n");
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void backdrop_load(const char *filename, unsigned int start_pen, unsigned int max_pens)
/*TODO*///{
/*TODO*///	int width, height;
/*TODO*///
/*TODO*///	/* replace the real display with a fake one, this way drivers can access Machine->scrbitmap
/*TODO*///	   the same way as before */
/*TODO*///
/*TODO*///	load_png_fit(filename, start_pen, max_pens, &artwork_backdrop);
/*TODO*///
/*TODO*///	if (artwork_backdrop)
/*TODO*///	{
/*TODO*///		width = artwork_backdrop->artwork->width;
/*TODO*///		height = artwork_backdrop->artwork->height;
/*TODO*///
/*TODO*///		if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///		{
/*TODO*///			int temp;
/*TODO*///
/*TODO*///			temp = height;
/*TODO*///			height = width;
/*TODO*///			width = temp;
/*TODO*///		}
/*TODO*///
/*TODO*///		if ((artwork_real_scrbitmap = bitmap_alloc(width, height)) == 0)
/*TODO*///		{
/*TODO*///			artwork_kill();
/*TODO*///			logerror("Not enough memory for artwork!\n");
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void artwork_load(struct artwork_info **a, const char *filename, unsigned int start_pen, unsigned int max_pens)
/*TODO*///{
/*TODO*///	load_png_fit(filename, start_pen, max_pens, a);
/*TODO*///}
/*TODO*///
/*TODO*///void artwork_load_size(struct artwork_info **a, const char *filename, unsigned int start_pen, unsigned int max_pens,
/*TODO*///					   int width, int height)
/*TODO*///{
/*TODO*///	load_png(filename, start_pen, max_pens, width, height, a);
/*TODO*///}
/*TODO*///
/*TODO*////*********************************************************************
/*TODO*///  artwork_elements scale
/*TODO*///
/*TODO*///  scales an array of artwork elements to width and height. The first
/*TODO*///  element (which has to be a box) is used as reference. This is useful
/*TODO*///  for atwork with disks.
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
/*TODO*///
/*TODO*////*********************************************************************
/*TODO*///  overlay_create
/*TODO*///
/*TODO*///  This works similar to artwork_load but generates artwork from
/*TODO*///  an array of artwork_element. This is useful for very simple artwork
/*TODO*///  like the overlay in the Space invaders series of games.  The overlay
/*TODO*///  is defined to be the same size as the screen.
/*TODO*///  The end of the array is marked by an entry with negative coordinates.
/*TODO*///  Boxes and disks are supported. Disks are marked max_y == -1,
/*TODO*///  min_x == x coord. of center, min_y == y coord. of center, max_x == radius.
/*TODO*///  If there are transparent and opaque overlay elements, the opaque ones
/*TODO*///  have to be at the end of the list to stay compatible with the PNG
/*TODO*///  artwork.
/*TODO*/// *********************************************************************/
/*TODO*///void overlay_create(const struct artwork_element *ae, unsigned int start_pen, unsigned int max_pens)
/*TODO*///{
/*TODO*///	struct osd_bitmap *disk, *disk_alpha, *box, *box_alpha;
/*TODO*///	int pen, transparent_pen = -1, disk_type, white_pen;
/*TODO*///	int width, height;
/*TODO*///
/*TODO*///	allocate_artwork_mem(Machine->scrbitmap->width, Machine->scrbitmap->height, &artwork_overlay);
/*TODO*///
/*TODO*///	if (artwork_overlay==NULL)
/*TODO*///		return;
/*TODO*///
/*TODO*///	/* replace the real display with a fake one, this way drivers can access Machine->scrbitmap
/*TODO*///	   the same way as before */
/*TODO*///
/*TODO*///	width = Machine->scrbitmap->width;
/*TODO*///	height = Machine->scrbitmap->height;
/*TODO*///
/*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///	{
/*TODO*///		int temp;
/*TODO*///
/*TODO*///		temp = height;
/*TODO*///		height = width;
/*TODO*///		width = temp;
/*TODO*///	}
/*TODO*///
/*TODO*///	if ((artwork_real_scrbitmap = bitmap_alloc(width, height)) == 0)
/*TODO*///	{
/*TODO*///		artwork_kill();
/*TODO*///		logerror("Not enough memory for artwork!\n");
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	artwork_overlay->start_pen = start_pen;
/*TODO*///
/*TODO*///	if (Machine->scrbitmap->depth == 8)
/*TODO*///	{
/*TODO*///		if ((artwork_overlay->orig_palette = (UINT8 *)malloc(256*3)) == NULL)
/*TODO*///		{
/*TODO*///			logerror("Not enough memory for overlay!\n");
/*TODO*///			artwork_kill();
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		if ((artwork_overlay->transparency = (UINT8 *)malloc(256)) == NULL)
/*TODO*///		{
/*TODO*///			logerror("Not enough memory for overlay!\n");
/*TODO*///			artwork_kill();
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		transparent_pen = 255;
/*TODO*///		/* init with transparent white */
/*TODO*///		memset (artwork_overlay->orig_palette, 255, 3);
/*TODO*///		artwork_overlay->transparency[0]=0;
/*TODO*///		artwork_overlay->num_pens_used = 1;
/*TODO*///		artwork_overlay->num_pens_trans = 1;
/*TODO*///		white_pen = 0;
/*TODO*///		fillbitmap (artwork_overlay->orig_artwork, 0, 0);
/*TODO*///		fillbitmap (artwork_overlay->alpha, 0, 0);
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		if ((artwork_overlay->orig_palette = create_15bit_palette()) == 0)
/*TODO*///		{
/*TODO*///			logerror("Unable to allocate memory for artwork\n");
/*TODO*///			artwork_kill();
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		artwork_overlay->num_pens_used = 32768;
/*TODO*///		transparent_pen = 0xffff;
/*TODO*///		white_pen = 0x7fff;
/*TODO*///		fillbitmap (artwork_overlay->orig_artwork, white_pen, 0);
/*TODO*///		fillbitmap (artwork_overlay->alpha, 0, 0);
/*TODO*///	}
/*TODO*///
/*TODO*///	while (ae->box.min_x >= 0)
/*TODO*///	{
/*TODO*///		int alpha = ae->alpha;
/*TODO*///
/*TODO*///		if (alpha == OVERLAY_DEFAULT_OPACITY)
/*TODO*///		{
/*TODO*///			alpha = 0x18;
/*TODO*///		}
/*TODO*///
/*TODO*///		pen = get_new_pen (artwork_overlay, ae->red, ae->green, ae->blue, alpha);
/*TODO*///		if (ae->box.max_y < 0) /* disk */
/*TODO*///		{
/*TODO*///			int r = ae->box.max_x;
/*TODO*///			disk_type = ae->box.max_y;
/*TODO*///
/*TODO*///			switch (disk_type)
/*TODO*///			{
/*TODO*///			case -1: /* disk overlay */
/*TODO*///				if ((disk = create_disk (r, pen, white_pen)) == NULL)
/*TODO*///				{
/*TODO*///					artwork_kill();
/*TODO*///					return;
/*TODO*///				}
/*TODO*///				if ((disk_alpha = create_disk (r, alpha, 0)) == NULL)
/*TODO*///				{
/*TODO*///					artwork_kill();
/*TODO*///					return;
/*TODO*///				}
/*TODO*///				merge_cmy (artwork_overlay, disk, disk_alpha, ae->box.min_x - r, ae->box.min_y - r);
/*TODO*///				bitmap_free(disk_alpha);
/*TODO*///				bitmap_free(disk);
/*TODO*///				break;
/*TODO*///
/*TODO*///			case -2: /* punched disk */
/*TODO*///				if ((disk = create_disk (r, pen, transparent_pen)) == NULL)
/*TODO*///				{
/*TODO*///					artwork_kill();
/*TODO*///					return;
/*TODO*///				}
/*TODO*///				copybitmap(artwork_overlay->orig_artwork,disk,0, 0,
/*TODO*///						   ae->box.min_x - r,
/*TODO*///						   ae->box.min_y - r,
/*TODO*///						   0,TRANSPARENCY_PEN, transparent_pen);
/*TODO*///				/* alpha */
/*TODO*///				if ((disk_alpha = create_disk (r, alpha, transparent_pen)) == NULL)
/*TODO*///				{
/*TODO*///					artwork_kill();
/*TODO*///					return;
/*TODO*///				}
/*TODO*///				copybitmap(artwork_overlay->alpha,disk_alpha,0, 0,
/*TODO*///						   ae->box.min_x - r,
/*TODO*///						   ae->box.min_y - r,
/*TODO*///						   0,TRANSPARENCY_PEN, transparent_pen);
/*TODO*///				bitmap_free(disk_alpha);
/*TODO*///				bitmap_free(disk);
/*TODO*///				break;
/*TODO*///
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if ((box = bitmap_alloc(ae->box.max_x - ae->box.min_x + 1,
/*TODO*///										 ae->box.max_y - ae->box.min_y + 1)) == 0)
/*TODO*///			{
/*TODO*///				logerror("Not enough memory for artwork!\n");
/*TODO*///				artwork_kill();
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			if ((box_alpha = bitmap_alloc(ae->box.max_x - ae->box.min_x + 1,
/*TODO*///										 ae->box.max_y - ae->box.min_y + 1)) == 0)
/*TODO*///			{
/*TODO*///				logerror("Not enough memory for artwork!\n");
/*TODO*///				artwork_kill();
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			fillbitmap (box, pen, 0);
/*TODO*///			fillbitmap (box_alpha, alpha, 0);
/*TODO*///			merge_cmy (artwork_overlay, box, box_alpha, ae->box.min_x, ae->box.min_y);
/*TODO*///			bitmap_free(box);
/*TODO*///			bitmap_free(box_alpha);
/*TODO*///		}
/*TODO*///		ae++;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* Make sure we don't have too many colors */
/*TODO*///	if (artwork_overlay->num_pens_used > max_pens)
/*TODO*///	{
/*TODO*///		logerror("Too many colors in overlay.\n");
/*TODO*///		logerror("Colors found: %d  Max Allowed: %d\n",
/*TODO*///				      artwork_overlay->num_pens_used,max_pens);
/*TODO*///		artwork_kill();
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE)
/*TODO*///	{
/*TODO*///		load_palette(artwork_overlay,artwork_overlay->orig_palette);
/*TODO*///		backdrop_refresh(artwork_overlay);
/*TODO*///	}
/*TODO*///
/*TODO*///	if (Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE)
/*TODO*///		overlay_remap();
/*TODO*///}
/*TODO*///
/*TODO*///int artwork_get_size_info(const char *file_name, struct artwork_size_info *a)
/*TODO*///{
/*TODO*///	void *fp;
/*TODO*///	struct png_info p;
/*TODO*///	int file_name_len;
/*TODO*///	char file_name2[256];
/*TODO*///
/*TODO*///	/* If the user turned artwork off, bail */
/*TODO*///	if (!options.use_artwork) return 0;
/*TODO*///
/*TODO*///	/* check for .png */
/*TODO*///	strcpy(file_name2, file_name);
/*TODO*///	file_name_len = strlen(file_name2);
/*TODO*///	if ((file_name_len < 4) || stricmp(&file_name2[file_name_len - 4], ".png"))
/*TODO*///	{
/*TODO*///		strcat(file_name2, ".png");
/*TODO*///	}
/*TODO*///
/*TODO*///	if (!(fp = osd_fopen(Machine->gamedrv->name, file_name2, OSD_FILETYPE_ARTWORK, 0)))
/*TODO*///	{
/*TODO*///		logerror("Unable to open PNG %s\n", file_name);
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (!png_read_info(fp, &p))
/*TODO*///	{
/*TODO*///		osd_fclose (fp);
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	osd_fclose (fp);
/*TODO*///
/*TODO*///	a->width = p.width;
/*TODO*///	a->height = p.height;
/*TODO*///	a->screen = p.screen;
/*TODO*///
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*///    
}
