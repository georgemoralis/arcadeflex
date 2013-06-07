
package mame;

import static arcadeflex.libc.*;
import static mame.osdependH.*;

public class drawgfxH {
    public static final int MAX_GFX_PLANES =8;
    public static final int MAX_GFX_SIZE =64;

/*TODO*///#define RGN_FRAC(num,den) (0x80000000 | (((num) & 0x0f) << 27) | (((den) & 0x0f) << 23))
    public static int IS_FRAC(int offset) { return ((offset) & 0x80000000); }
    public static int FRAC_NUM(int offset){ return (((offset) >> 27) & 0x0f); }
    public static int FRAC_DEN(int offset){ return (((offset) >> 23) & 0x0f); }
    public static int FRAC_OFFSET(int offset) { return ((offset) & 0x007fffff); }

    public static class GfxLayout
    {
        public GfxLayout(){}
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
	public int[] pen_usage;//unsigned int *pen_usage;	/* an array of total_elements ints. */
								/* It is a table of the pens each character uses */
								/* (bit 0 = pen 0, and so on). This is used by */
								/* drawgfgx() to do optimizations like skipping */
								/* drawing of a totally transparent characters */
	public char[] gfxdata; //unsigned char *gfxdata;	/* pixel data */
	int line_modulo;	/* amount to add to get to the next line (usually = width) */
	int char_modulo;	/* = line_modulo * height */
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

    /* drawing mode case TRANSPARENCY_PEN_TABLE */
    public static final int DRAWMODE_NONE	=	0;
    public static final int DRAWMODE_SOURCE	=	1;
    public static final int DRAWMODE_SHADOW	=	2;
    public static final int DRAWMODE_HIGHLIGHT	=       3;


    public static abstract interface plot_pixel_procPtr { public abstract void handler(osd_bitmap bitmap,int x,int y,int pen); }
    public static abstract interface read_pixel_procPtr { public abstract int handler(osd_bitmap bitmap,int x,int y); }


}