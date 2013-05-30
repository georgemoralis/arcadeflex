
package mame;

import static arcadeflex.libc.*;
import static mame.osdependH.*;

public class drawgfxH {
/*TODO*///#define MAX_GFX_PLANES 8
/*TODO*///#define MAX_GFX_SIZE 64
/*TODO*///
/*TODO*///#define RGN_FRAC(num,den) (0x80000000 | (((num) & 0x0f) << 27) | (((den) & 0x0f) << 23))
/*TODO*///#define IS_FRAC(offset) ((offset) & 0x80000000)
/*TODO*///#define FRAC_NUM(offset) (((offset) >> 27) & 0x0f)
/*TODO*///#define FRAC_DEN(offset) (((offset) >> 23) & 0x0f)
/*TODO*///#define FRAC_OFFSET(offset) ((offset) & 0x007fffff)
/*TODO*///
/*TODO*///
    public static class GfxLayout
    {
	public GfxLayout(int w, int h, int t, int p, int po[], int x[], int y[], int ci) { width = w; height = h; total = t; planes = p; planeoffset = po; xoffset = x; yoffset = y; charincrement = ci; };
	/*UNINT16*/public int width,height;	/* width and height of chars/sprites */
	/*UNINT32*/public int total;	/* total numer of chars/sprites in the rom */
	/*UNINT16*/public int planes;	/* number of bitplanes */
	/*UNINT32*/public int planeoffset[];	/* start of every bitplane */
	/*UNINT32*/public int xoffset[];	/* coordinates of the bit corresponding to the pixel */
	/*UNINT32*/public int yoffset[];	/* of the given coordinates */
	/*UNINT16*/public int charincrement;	/* distance between two consecutive characters/sprites */
    };
    public static class GfxElement
    {
        public GfxElement() {};
        //TODO rest of constructors?
	public int width,height;

	public /*unsigned */int total_elements;	/* total number of characters/sprites */
        public int color_granularity;	/* number of colors for each color code */
				/* (for example, 4 for 2 bitplanes gfx) */
        public CharPtr colortable;	/* map color codes to screen pens */								/* if this is 0, the function does a verbatim copy */
	public int total_colors;
/*TODO*///	unsigned int *pen_usage;	/* an array of total_elements ints. */
/*TODO*///								/* It is a table of the pens each character uses */
/*TODO*///								/* (bit 0 = pen 0, and so on). This is used by */
/*TODO*///								/* drawgfgx() to do optimizations like skipping */
/*TODO*///								/* drawing of a totally transparent characters */
/*TODO*///	unsigned char *gfxdata;	/* pixel data */
/*TODO*///	int line_modulo;	/* amount to add to get to the next line (usually = width) */
/*TODO*///	int char_modulo;	/* = line_modulo * height */
    };
    public static class GfxDecodeInfo
    {
	public GfxDecodeInfo(int mr, int s, GfxLayout g, int ccs, int tcc) { memory_region = mr; start = s; gfxlayout = g; color_codes_start = ccs; total_color_codes = tcc; };
	public GfxDecodeInfo(int s, GfxLayout g, int ccs, int tcc) { start = s; gfxlayout = g; color_codes_start = ccs; total_color_codes = tcc; };
	public GfxDecodeInfo(int s) { this(s, s, null, 0, 0); }

        public int memory_region;	/* memory region where the data resides (usually 1) */
						/* -1 marks the end of the array */
	public int start;	/* beginning of data data to decode (offset in RAM[]) */
	public GfxLayout gfxlayout;
	public int color_codes_start;	/* offset in the color lookup table where color codes start */
	public int total_color_codes;	/* total number of color codes */
    };
    public static class rectangle
    {
        public rectangle() {};
	public rectangle(int minx, int maxx, int miny, int maxy) { min_x = minx; max_x = maxx; min_y = miny; max_y = maxy; };
	public int min_x,max_x;
	public int min_y,max_y;
    };

    public static final int TRANSPARENCY_NONE      = 0;
    public static final int TRANSPARENCY_PEN       = 1;
    public static final int TRANSPARENCY_PENS      = 4;
    public static final int TRANSPARENCY_COLOR     = 2;
    public static final int TRANSPARENCY_THROUGH   = 3;
    public static final int TRANSPARENCY_PEN_TABLE = 5;
/*TODO*///
/*TODO*////* drawing mode case TRANSPARENCY_PEN_TABLE */
/*TODO*///extern UINT8 gfx_drawmode_table[256];
/*TODO*///#define DRAWMODE_NONE		0
/*TODO*///#define DRAWMODE_SOURCE		1
/*TODO*///#define DRAWMODE_SHADOW		2
/*TODO*///#define DRAWMODE_HIGHLIGHT	3
/*TODO*///
/*TODO*///
/*TODO*///typedef void (*plot_pixel_proc)(struct osd_bitmap *bitmap,int x,int y,int pen);
/*TODO*///typedef int  (*read_pixel_proc)(struct osd_bitmap *bitmap,int x,int y);
/*TODO*///
    public static abstract interface plot_pixel_procPtr { public abstract void handler(osd_bitmap bitmap,int x,int y,int pen); }
    public static abstract interface read_pixel_procPtr { public abstract int handler(osd_bitmap bitmap,int x,int y); }

 /*TODO*////* pointers to pixel functions.  They're set based on orientation, depthness and weather
/*TODO*///   dirty rectangle handling is enabled */
/*TODO*///extern plot_pixel_proc plot_pixel;
/*TODO*///extern read_pixel_proc read_pixel;
/*TODO*///
/*TODO*///
/*TODO*///void decodechar(struct GfxElement *gfx,int num,const unsigned char *src,const struct GfxLayout *gl);
/*TODO*///struct GfxElement *decodegfx(const unsigned char *src,const struct GfxLayout *gl);
}
