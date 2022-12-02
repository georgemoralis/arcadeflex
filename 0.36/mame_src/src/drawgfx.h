/*********************************************************************

  drawgfx.h

  Generic graphic functions.

*********************************************************************/

#ifndef DRAWGFX_H
#define DRAWGFX_H

#define MAX_GFX_PLANES 8
#define MAX_GFX_SIZE 64

#define RGN_FRAC(num,den) (0x80000000 | (((num) & 0x0f) << 27) | (((den) & 0x0f) << 23))
#define IS_FRAC(offset) ((offset) & 0x80000000)
#define FRAC_NUM(offset) (((offset) >> 27) & 0x0f)
#define FRAC_DEN(offset) (((offset) >> 23) & 0x0f)
#define FRAC_OFFSET(offset) ((offset) & 0x007fffff)


struct GfxLayout
{
	UINT16 width,height; /* width and height (in pixels) of chars/sprites */
	UINT32 total; /* total numer of chars/sprites in the rom */
	UINT16 planes; /* number of bitplanes */
	UINT32 planeoffset[MAX_GFX_PLANES]; /* start of every bitplane (in bits) */
	UINT32 xoffset[MAX_GFX_SIZE]; /* position of the bit corresponding to the pixel */
	UINT32 yoffset[MAX_GFX_SIZE]; /* of the given coordinates */
	UINT16 charincrement; /* distance between two consecutive characters/sprites (in bits) */
};

struct GfxElement
{
	int width,height;

	unsigned int total_elements;	/* total number of characters/sprites */
	int color_granularity;	/* number of colors for each color code */
							/* (for example, 4 for 2 bitplanes gfx) */
	unsigned short *colortable;	/* map color codes to screen pens */
								/* if this is 0, the function does a verbatim copy */
	int total_colors;
	unsigned int *pen_usage;	/* an array of total_elements ints. */
								/* It is a table of the pens each character uses */
								/* (bit 0 = pen 0, and so on). This is used by */
								/* drawgfgx() to do optimizations like skipping */
								/* drawing of a totally transparent characters */
	unsigned char *gfxdata;	/* pixel data */
	int line_modulo;	/* amount to add to get to the next line (usually = width) */
	int char_modulo;	/* = line_modulo * height */
};

struct GfxDecodeInfo
{
	int memory_region;	/* memory region where the data resides (usually 1) */
						/* -1 marks the end of the array */
	int start;	/* beginning of data to decode */
	struct GfxLayout *gfxlayout;
	int color_codes_start;	/* offset in the color lookup table where color codes start */
	int total_color_codes;	/* total number of color codes */
};


struct rectangle
{
	int min_x,max_x;
	int min_y,max_y;
};



#define TRANSPARENCY_NONE 0
#define TRANSPARENCY_PEN 1
#define TRANSPARENCY_PENS 4
#define TRANSPARENCY_COLOR 2
#define TRANSPARENCY_THROUGH 3
#define TRANSPARENCY_PEN_TABLE 5

/* drawing mode case TRANSPARENCY_PEN_TABLE */
extern UINT8 gfx_drawmode_table[256];
#define DRAWMODE_NONE		0
#define DRAWMODE_SOURCE		1
#define DRAWMODE_SHADOW		2
#define DRAWMODE_HIGHLIGHT	3


typedef void (*plot_pixel_proc)(struct osd_bitmap *bitmap,int x,int y,int pen);
typedef int  (*read_pixel_proc)(struct osd_bitmap *bitmap,int x,int y);

/* pointers to pixel functions.  They're set based on orientation, depthness and weather
   dirty rectangle handling is enabled */
extern plot_pixel_proc plot_pixel;
extern read_pixel_proc read_pixel;


void decodechar(struct GfxElement *gfx,int num,const unsigned char *src,const struct GfxLayout *gl);
struct GfxElement *decodegfx(const unsigned char *src,const struct GfxLayout *gl);
void set_pixel_functions(void);
void freegfx(struct GfxElement *gfx);
void drawgfx(struct osd_bitmap *dest,const struct GfxElement *gfx,
		unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
		const struct rectangle *clip,int transparency,int transparent_color);
void copybitmap(struct osd_bitmap *dest,struct osd_bitmap *src,int flipx,int flipy,int sx,int sy,
		const struct rectangle *clip,int transparency,int transparent_color);
void copybitmapzoom(struct osd_bitmap *dest,struct osd_bitmap *src,int flipx,int flipy,int sx,int sy,
		const struct rectangle *clip,int transparency,int transparent_color,int scalex,int scaley);
void copyscrollbitmap(struct osd_bitmap *dest,struct osd_bitmap *src,
		int rows,const int *rowscroll,int cols,const int *colscroll,
		const struct rectangle *clip,int transparency,int transparent_color);
void fillbitmap(struct osd_bitmap *dest,int pen,const struct rectangle *clip);
void plot_pixel2(struct osd_bitmap *bitmap1,struct osd_bitmap *bitmap2,int x,int y,int pen);
void drawgfxzoom( struct osd_bitmap *dest_bmp,const struct GfxElement *gfx,
		unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
		const struct rectangle *clip,int transparency,int transparent_color,int scalex, int scaley );

#endif
