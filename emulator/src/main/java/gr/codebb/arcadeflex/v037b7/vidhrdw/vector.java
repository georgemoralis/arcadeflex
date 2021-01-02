/******************************************************************************
 *
 * vector.c
 *
 *
 * Copyright 1997,1998 by the M.A.M.E. Project
 *
 *        anti-alias code by Andrew Caldwell
 *        (still more to add)
 *
 * 980611 use translucent vectors. Thanks to Peter Hirschberg
 *        and Neil Bradley for the inspiration. BW
 * 980307 added cleverer dirty handling. BW, ASG
 *        fixed antialias table .ac
 * 980221 rewrote anti-alias line draw routine
 *        added inline assembly multiply fuction for 8086 based machines
 *        beam diameter added to draw routine
 *        beam diameter is accurate in anti-alias line draw (Tcosin)
 *        flicker added .ac
 * 980203 moved LBO's routines for drawing into a buffer of vertices
 *        from avgdvg.c to this location. Scaling is now initialized
 *        by calling vector_init(...). BW
 * 980202 moved out of msdos.c ASG
 * 980124 added anti-alias line draw routine
 *        modified avgdvg.c and sega.c to support new line draw routine
 *        added two new tables Tinten and Tmerge (for 256 color support)
 *        added find_color routine to build above tables .ac
 *
 **************************************************************************** */

/* GLmame and FXmame provide their own vector implementations */
/*TODO*///#if !(defined xgl) && !(defined xfx) && !(defined svgafx)

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import gr.codebb.arcadeflex.common.PtrLib.UShortPtr;
import static gr.codebb.arcadeflex.common.libc.cstdlib.rand;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.mame.options;
import gr.codebb.arcadeflex.v036.mame.osdependH;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.vectorH.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.platform.video.osd_get_pen;
import static gr.codebb.arcadeflex.v036.platform.video.DIRTY_H;
import static gr.codebb.arcadeflex.v036.platform.video.dirty_new;

public class vector
{

	public static final int VCLEAN  = 0;
	public static final int VDIRTY  = 1;
	public static final int VCLIP   = 2;

	public static UBytePtr vectorram = new UBytePtr();
	public static int[] vectorram_size = new int[1];

	static int antialias;                            /* flag for anti-aliasing */
	static int beam;                                 /* size of vector beam    */
	static int flicker;                              /* beam flicker value     */
	public static int translucency;

	static int beam_diameter_is_one;		  /* flag that beam is one pixel wide */

	static int vector_scale_x;                /* scaling to screen */
	static int vector_scale_y;                /* scaling to screen */

	static float gamma_correction = 1.2f;
	static float intensity_correction = 1.5f;
	
	/* The vectices are buffered here */
	public static class point
	{
		public int x; int y;
		public int col;
		public int intensity;
		public int arg1; int arg2; /* start/end in pixel array or clipping info */
		public int status;         /* for dirty and clipping handling */
	};
	
	static point[] new_list;
	static point[] old_list;
	static int new_index;
	static int old_index;

	/* coordinates of pixels are stored here for faster removal */
	static int[] pixel;
	static int p_index=0;

	static int[] pTcosin;            /* adjust line width */
	static int[] pTinten;            /* intensity         */
	static int[] pTmerge;            /* mergeing pixels   */
	static int[] invpens;            /* maps OS colors to pens */

	static char[] pens;
	static int total_colors;

/*TODO*///	#define Tcosin(x)   pTcosin[(x)]          /* adjust line width */
/*TODO*///	#define Tinten(x,y) pTinten[(x)*total_colors+(y)]  /* intensity         */
/*TODO*///	#define Tmerge(x,y) pTmerge[(x)*total_colors+(y)]  /* mergeing pixels   */

	public static final int ANTIALIAS_GUNBIT  = 6;             /* 6 bits per gun in vga (1-8 valid) */
	public static final int ANTIALIAS_GUNNUM  = (1<<ANTIALIAS_GUNBIT);

	static int[] Tgamma = new int[256];         /* quick gamma anti-alias table  */
	static int[] Tgammar = new int[256];        /* same as above, reversed order */

	static osd_bitmap vecbitmap;
	static int vecwidth, vecheight;
	static int vecshift;
	static int xmin, ymin, xmax, ymax; /* clipping area */

	static int vector_runs;	/* vector runs per refresh */

	static plot_pixel_procPtr vector_pp;
	static read_pixel_procPtr vector_rp;

	/*
	 * multiply and divide routines for drawing lines
	 * can be be replaced by an assembly routine in osinline.h
	 */
/*TODO*///	#ifndef vec_mult
	static int vec_mult(int parm1, int parm2)
	{
		int temp,result;
	
		temp     = Math.abs(parm1);
		result   = (temp&0x0000ffff) * (parm2&0x0000ffff);
		result >>= 16;
		result  += (temp&0x0000ffff) * (parm2>>16       );
		result  += (temp>>16       ) * (parm2&0x0000ffff);
		result >>= 16;
		result  += (temp>>16       ) * (parm2>>16       );
	
		if( parm1 < 0 )
			return(-result);
		else
			return( result);
	}
/*TODO*///	#endif
	
	/* can be be replaced by an assembly routine in osinline.h */
/*TODO*///	#ifndef vec_div
	static int vec_div(int parm1, int parm2)
	{
		if( (parm2>>12) != 0 )
		{
			parm1 = (parm1<<4) / (parm2>>12);
			if( parm1 > 0x00010000 )
				return( 0x00010000 );
			if( parm1 < -0x00010000 )
				return( -0x00010000 );
			return( parm1 );
		}
		return( 0x00010000 );
	}
/*TODO*///	#endif
/*TODO*///	
	public static plot_pixel_procPtr vector_pp_8  = new plot_pixel_procPtr() { public void handler(osd_bitmap b,int x,int y,int p)   { new UBytePtr(b.line[y]).write(x, p); } };
	public static plot_pixel_procPtr vector_pp_16  = new plot_pixel_procPtr() { public void handler(osd_bitmap b,int x,int y,int p)   { new UShortPtr(b.line[y]).write(x, (char) p); } };
	static read_pixel_procPtr vector_rp_8 = new read_pixel_procPtr() {
            @Override
            public int handler(osd_bitmap b, int x, int y) {
                return new UBytePtr(b.line[y]).read(x);
            }
        };
        
	static read_pixel_procPtr vector_rp_16 = new read_pixel_procPtr() {
            @Override
            public int handler(osd_bitmap b, int x, int y) {
                return (new UShortPtr(b.line[y]).read(x));
            }
        };
        
	/*
	 * finds closest color and returns the index (for 256 color)
	 */
	
	static int find_pen( char r, char g, char b)
	{
		int i,bi,ii;
		long x,y,z,bc;
		ii = 32;
		bi = 256;
		bc = 0x01000000;
	
		do
		{
			for( i = 0; i < total_colors; i++ )
			{
				char[] r1=new char[1],g1=new char[1],b1=new char[1];
	
				osd_get_pen(pens[i],r1,g1,b1);
                                //System.out.println("Red: "+r1[0]);
				if((x=(long)(Math.abs(r1[0]-r)+1)) > ii) continue;
				if((y=(long)(Math.abs(g1[0]-g)+1)) > ii) continue;
				if((z=(long)(Math.abs(b1[0]-b)+1)) > ii) continue;
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
	
	/* MLR 990316 new gamma handling added */
	static void vector_set_gamma(float _gamma)
	{
		int i, h;
	
		gamma_correction = _gamma;
	
		for (i = 0; i < 256; i++)
		{
			h = (int) (255.0*Math.pow(i/255.0, 1.0/gamma_correction));
			if( h > 255) h = 255;
			Tgamma[i] = h;
                        Tgammar[255-i] = h;
		}
	}
	
/*TODO*///	float vector_get_gamma(void)
/*TODO*///	{
/*TODO*///		return gamma_correction;
/*TODO*///	}
/*TODO*///	
/*TODO*///	void vector_set_intensity(float _intensity)
/*TODO*///	{
/*TODO*///		intensity_correction = _intensity;
/*TODO*///	}
/*TODO*///	
/*TODO*///	float vector_get_intensity(void)
/*TODO*///	{
/*TODO*///		return intensity_correction;
/*TODO*///	}
	
	/*
	 * Initializes vector game video emulation
	 */
	
	public static VhStartPtr vector_vh_start = new VhStartPtr() { public int handler() 
	{
		int h,i,j,k;
                int[] c = new int[3];
	
		/* Grab the settings for this session */
		antialias = options.antialias;
		translucency = options.translucency;
		flicker = options.flicker;
		beam = options.beam;
	
		pens = Machine.pens;
		total_colors = Math.min(256, Machine.drv.total_colors);
	
		if (Machine.color_depth == 8)
		{
			vector_pp = vector_pp_8;
			vector_rp = vector_rp_8;
		}
		else
		{
			vector_pp = vector_pp_16;
			vector_rp = vector_rp_16;
		}
	
		if (beam == 0x00010000)
			beam_diameter_is_one = 1;
		else
			beam_diameter_is_one = 0;
	
		p_index = 0;
	
		new_index = 0;
		old_index = 0;
		vector_runs = 0;
	
		/* allocate memory for tables */
		pTcosin = new int[ (2048+1) ];   /* yes! 2049 is correct */
		pTinten = new int[ total_colors * 256 ];
		pTmerge = new int[ total_colors * total_colors ];
		invpens = new int[65536 ];
		pixel = new int[ MAX_PIXELS ];
		old_list = new point[ MAX_POINTS ];
		new_list = new point[ MAX_POINTS ];
	
		/* did we get the requested memory? */
		if (!(pTcosin!=null && pTinten!=null && pTmerge!=null && invpens!=null && pixel!=null && old_list!=null && new_list!=null))
		{
			/* vector_vh_stop should better be called by the main engine */
			/* if vector_vh_start fails */
			vector_vh_stop.handler();
			return 1;
		}
	
		/* build cosine table for fixing line width in antialias */
		for (i=0; i<=2048; i++)
		{
			pTcosin[i] = (int)((double)(1.0/Math.cos(Math.atan((double)(i)/2048.0)))*0x10000000 + 0.5);
		}
	
		invpens = new int[ 65536 ];
		for( i = 0; i < total_colors ;i++ )
			invpens[Machine.pens[i]] = i;
	
		/* build anti-alias table */
		h = 256 / ANTIALIAS_GUNNUM;           /* to generate table faster */
		for (i = 0; i < 256; i += h )               /* intensity */
		{
			for (j = 0; j < total_colors; j++)               /* color */
			{
				char[] r1=new char[1],g1=new char[1],b1=new char[1];
                                int pen;
                                int n;
				osd_get_pen(pens[j],r1,g1,b1);
				pen = find_pen( (char)((r1[0]*(i+1))>>8), (char)((g1[0]*(i+1))>>8), (char)((b1[0]*(i+1))>>8) );
				for (n = 0; n < h; n++ )
				{
					pTinten[(i + n)*total_colors+(j)] = pen;
				}
			}
		}
	
		/* build merge color table */
		for( i = 0; i < total_colors ;i++ )                /* color1 */
		{
			char[] rgb1=new char[3],rgb2=new char[3];
                        int _mPen = pens[i];
                        char[] _r = new char[]{rgb1[0]};
                        char[] _g = new char[]{rgb1[1]};
                        char[] _b = new char[]{rgb1[2]};
	
			osd_get_pen(_mPen,_r,_g,_b);
			for( j = 0; j <= i ;j++ )               /* color2 */
			{
				osd_get_pen(pens[j],new char[]{rgb2[0]},new char[]{rgb2[1]},new char[]{rgb2[2]});
	
				for (k = 0; k < 3; k++)
				if (translucency != 0) /* add gun values */
				{
					int tmp;
					tmp = rgb1[k] + rgb2[k];
					if (tmp > 255)
						c[k] = 255;
					else
						c[k] = tmp;
				}
				else /* choose highest gun value */
				{
					if (rgb1[k] > rgb2[k])
						c[k] = rgb1[k];
					else
						c[k] = rgb2[k];
				}
				pTmerge[i*total_colors+(j)] = find_pen((char)c[0],(char)c[1],(char)c[2]);
                                pTmerge[i*total_colors+(j)] = find_pen((char)c[0],(char)c[1],(char)c[2]);
			}
		}
	
		/* build gamma correction table */
		vector_set_gamma (gamma_correction);
	
		return 0;
	} };
	
	
	/*
	 * Setup scaling. Currently the Sega games are stuck at VECSHIFT 15
	 * and the the AVG games at VECSHIFT 16
	 */
	static void vector_set_shift (int shift)
	{
		vecshift = shift;
	}
	
	/*
	 * Clear the old bitmap. Delete pixel for pixel, this is faster than memset.
	 */
	static void vector_clear_pixels ()
	{
		char bg=pens[0];
		int i;
		int coords;
	
	
		for (i=p_index-1; i>=0; i--)
		{
			coords = pixel[i];
			vector_pp.handler(vecbitmap, coords >> 16, coords & 0x0000ffff, bg);
		}
	
		p_index=0;
	
	}
	
	/*
	 * Stop the vector video hardware emulation. Free memory.
	 */
	public static VhStopPtr vector_vh_stop = new VhStopPtr() { public void handler() 
	{
		if (pTcosin != null)
			pTcosin = null;
		if (pTinten != null)
			pTinten = null;
		if (pTmerge != null)
			pTmerge = null;
		if (pixel != null)
			pixel = null;
		if (old_list != null)
			old_list = null;
		if (new_list != null)
			new_list = null;
	} };
        
        public static void osd_mark_vector_dirty(int x, int y){ 
            dirty_new[(y)/16 * DIRTY_H + (x)/16] = 1;
        }
	
	/*
	 * draws an anti-aliased pixel (blends pixel with background)
	 */
	static void vector_draw_aa_pixel (int x, int y, int col, int dirty)
	{
		if (x < xmin || x >= xmax)
			return;
		if (y < ymin || y >= ymax)
			return;
	
		vector_pp.handler(vecbitmap, x, y, pens[pTmerge[(invpens[vector_rp.handler(vecbitmap, x, y)])*total_colors+(col)]]);
	
		if (p_index<MAX_PIXELS)
		{
			pixel[p_index] = y | (x << 16);
			p_index++;
		}
	
		/* Mark this pixel as dirty */
		if (dirty != 0)
			osd_mark_vector_dirty (x, y);
	}
	
	
	/*
	 * draws a line
	 *
	 * input:   x2  16.16 fixed point
	 *          y2  16.16 fixed point
	 *         col  0-255 indexed color (8 bit)
	 *   intensity  0-255 intensity
	 *       dirty  bool  mark the pixels as dirty while plotting them
	 *
	 * written by Andrew Caldwell
	 */
	static int x1,yy1;
        
	static void vector_draw_to (int x2, int y2, int col, int intensity, int dirty)
	{
		char a1;
		int orientation;
		int dx,dy,sx,sy,cx,cy,width;
		
		int xx,yy;
	
/*TODO*///	#if 0
/*TODO*///		logerror("line:%d,%d nach %d,%d color %d\n",x1,yy1,x2,y2,col);
/*TODO*///	#endif
	
		/* [1] scale coordinates to display */
	
		x2 = vec_mult(x2<<4,vector_scale_x);
		y2 = vec_mult(y2<<4,vector_scale_y);
	
		/* [2] fix display orientation */
	
		orientation = Machine.orientation;
		if ((orientation & ORIENTATION_SWAP_XY) != 0)
		{
			int temp;
			temp = x2;
			x2 = y2;
			y2 = temp;
		}
		if ((orientation & ORIENTATION_FLIP_X) != 0)
			x2 = ((vecwidth-1)<<16)-x2;
		if ((orientation & ORIENTATION_FLIP_Y) != 0)
			y2 = ((vecheight-1)<<16)-y2;
	
		/* [3] adjust cords if needed */
	
		if (antialias != 0)
		{
			if (beam_diameter_is_one != 0)
			{
				x2 = (x2+0x8000)&0xffff0000;
				y2 = (y2+0x8000)&0xffff0000;
			}
		}
		else /* noantialiasing */
		{
			x2 >>= 16;
			y2 >>= 16;
		}
	
		/* [4] handle color and intensity */
                boolean end_draw=false;
		if (intensity == 0) end_draw=true;
                
                if (!end_draw){
	
                    col = pTinten[(intensity)*total_colors+(col)];

                    /* [5] draw line */

                    if (antialias != 0)
                    {
                            /* draw an anti-aliased line */
                            dx=Math.abs(x1-x2);
                            dy=Math.abs(yy1-y2);
                            if (dx>=dy)
                            {
                                    sx = ((x1 <= x2) ? 1:-1);
                                    sy = vec_div(y2-yy1,dx);
                                    if (sy<0)
                                            dy--;
                                    x1 >>= 16;
                                    xx = x2>>16;
                                    width = vec_mult(beam<<4,pTcosin[Math.abs(sy)>>5]);
                                    if (beam_diameter_is_one==0)
                                            yy1-= width>>1; /* start back half the diameter */
                                    for (;;)
                                    {
                                            dx = width;    /* init diameter of beam */
                                            dy = yy1>>16;
                                            vector_draw_aa_pixel(x1,dy++,pTinten[(Tgammar[0xff&(yy1>>8)])*total_colors+(col)], dirty);
                                            dx -= 0x10000-(0xffff & yy1); /* take off amount plotted */
                                            a1 = (char) Tgamma[(dx>>8)&0xff];   /* calc remainder pixel */
                                            dx >>= 16;                   /* adjust to pixel (solid) count */
                                            while (dx-- != 0)                 /* plot rest of pixels */
                                                    vector_draw_aa_pixel(x1,dy++,col, dirty);
                                            vector_draw_aa_pixel(x1,dy,pTinten[(a1)*total_colors+(col)], dirty); //
                                            if (x1 == xx) break;
                                            x1+=sx;
                                            yy1+=sy;
                                    }
                            }
                            else
                            {
                                    sy = ((yy1 <= y2) ? 1:-1);
                                    sx = vec_div(x2-x1,dy);
                                    if (sx<0)
                                            dx--;
                                    yy1 >>= 16;
                                    yy = y2>>16;
                                    width = vec_mult(beam<<4,pTcosin[Math.abs(sx)>>5]);
                                    if( beam_diameter_is_one == 0 )
                                            x1-= width>>1; /* start back half the width */
                                    for (;;)
                                    {
                                            dy = width;    /* calc diameter of beam */
                                            dx = x1>>16;
                                            vector_draw_aa_pixel(dx++,yy1,pTinten[(Tgammar[0xff&(x1>>8)])*total_colors+(col)], dirty);
                                            dy -= 0x10000-(0xffff & x1); /* take off amount plotted */
                                            a1 = (char) Tgamma[(dy>>8)&0xff];   /* remainder pixel */
                                            dy >>= 16;                   /* adjust to pixel (solid) count */
                                            while (dy-- != 0)                 /* plot rest of pixels */
                                                    vector_draw_aa_pixel(dx++,yy1,col, dirty);
                                            vector_draw_aa_pixel(dx,yy1,pTinten[(a1)*total_colors+(col)], dirty);//
                                            if (yy1 == yy) break;
                                            yy1+=sy;
                                            x1+=sx;
                                    }
                            }
                    }
                    else /* use good old Bresenham for non-antialiasing 980317 BW */
                    {
                            dx = Math.abs(x1-x2);
                            dy = Math.abs(yy1-y2);
                            sx = (x1 <= x2) ? 1: -1;
                            sy = (yy1 <= y2) ? 1: -1;
                            cx = dx/2;
                            cy = dy/2;

                            if (dx>=dy)
                            {
                                    for (;;)
                                    {
                                            vector_draw_aa_pixel (x1, yy1, col, dirty);
                                            if (x1 == x2) break;
                                            x1 += sx;
                                            cx -= dy;
                                            if (cx < 0)
                                            {
                                                    yy1 += sy;
                                                    cx += dx;
                                            }
                                    }
                            }
                            else
                            {
                                    for (;;)
                                    {
                                            vector_draw_aa_pixel (x1, yy1, col, dirty);
                                            if (yy1 == y2) break;
                                            yy1 += sy;
                                            cy -= dx;
                                            if (cy < 0)
                                            {
                                                    x1 += sx;
                                                    cy += dy;
                                            }
                                    }
                            }
                    }
                }
/*TODO*///	end_draw:
	
		x1=x2;
		yy1=y2;
	}
	
	
	/*
	 * Adds a line end point to the vertices list. The vector processor emulation
	 * needs to call this.
	 */
	public static void vector_add_point (int x, int y, int color, int intensity)
	{
		point _new = new point();
	
		intensity *= intensity_correction;
		if (intensity > 0xff)
			intensity = 0xff;
	
		if (flicker!=0 && (intensity > 0))
		{
			intensity += (intensity * (0x80-(rand()&0xff)) * flicker)>>16;
			if (intensity < 0)
				intensity = 0;
			if (intensity > 0xff)
				intensity = 0xff;
		}
		_new = new_list[new_index];
                if (_new==null)
                    _new=new point();
		_new.x = x;
		_new.y = y;
		_new.col = color;
		_new.intensity = intensity;
		_new.status = VDIRTY; /* mark identical lines as clean later */
                
                new_list[new_index] = _new;
	
		new_index++;
		if (new_index >= MAX_POINTS)
		{
			new_index--;
			logerror("*** Warning! Vector list overflow!\n");
		}
                
                
	}
	
	/*
	 * Add new clipping info to the list
	 */
	public static void vector_add_clip (int x1, int yy1, int x2, int y2)
	{
		point _new;
	
		_new = new_list[new_index];
		_new.x = x1;
		_new.y = yy1;
		_new.arg1 = x2;
                _new.arg2 = y2;
		_new.status = VCLIP;
                
                new_list[new_index] = _new;
	
		new_index++;
		if (new_index >= MAX_POINTS)
		{
			new_index--;
			logerror("*** Warning! Vector list overflow!\n");
		}
	}
	
	
	/*
	 * Set the clipping area
	 */
	static void vector_set_clip (int x1, int yy1, int x2, int y2)
	{
		int orientation;
		int tmp;
	
		/* failsafe */
		if ((x1 >= x2) || (yy1 >= y2))
		{
			logerror("Error in clipping parameters.\n");
			xmin = 0;
			ymin = 0;
			xmax = vecwidth;
			ymax = vecheight;
			return;
		}
	
		/* scale coordinates to display */
		x1 = vec_mult(x1<<4,vector_scale_x);
		yy1 = vec_mult(yy1<<4,vector_scale_y);
		x2 = vec_mult(x2<<4,vector_scale_x);
		y2 = vec_mult(y2<<4,vector_scale_y);
	
		/* fix orientation */
		orientation = Machine.orientation;
		/* swapping x/y coordinates will still have the minima in x1,yy1 */
		if ((orientation & ORIENTATION_SWAP_XY) != 0)
		{
			tmp = x1; x1 = yy1; yy1 = tmp;
			tmp = x2; x2 = y2; y2 = tmp;
		}
		/* don't forget to swap x1,x2, since x2 becomes the minimum */
		if ((orientation & ORIENTATION_FLIP_X) != 0)
		{
			x1 = ((vecwidth-1)<<16)-x1;
			x2 = ((vecwidth-1)<<16)-x2;
			tmp = x1; x1 = x2; x2 = tmp;
		}
		/* don't forget to swap yy1,y2, since y2 becomes the minimum */
		if ((orientation & ORIENTATION_FLIP_Y) != 0)
		{
			yy1 = ((vecheight-1)<<16)-yy1;
			y2 = ((vecheight-1)<<16)-y2;
			tmp = yy1; yy1 = y2; y2 = tmp;
		}
	
		xmin = x1 >> 16;
		ymin = yy1 >> 16;
		xmax = x2 >> 16;
		ymax = y2 >> 16;
	
		/* Make it foolproof by trapping rounding errors */
		if (xmin < 0) xmin = 0;
		if (ymin < 0) ymin = 0;
		if (xmax > vecwidth) xmax = vecwidth;
		if (ymax > vecheight) ymax = vecheight;
	}
	
	
	/*
	 * The vector CPU creates a new display list. We save the old display list,
	 * but only once per refresh.
	 */
	public static void vector_clear_list ()
	{
		point[] tmp;
	
		if (vector_runs == 0)
		{
			old_index = new_index;
			tmp = old_list; old_list = new_list; new_list = tmp;
		}
	
		new_index = 0;
		vector_runs++;
	}
	
	
	/*
	 * By comparing with the last drawn list, we can prevent that identical
	 * vectors are marked dirty which appeared at the same list index in the
	 * previous frame. BW 19980307
	 */
	static void clever_mark_dirty ()
	{
		int i, j, min_index, last_match = 0;
		int[] coords=new int[128*1024];
                int coordsPos=0;
		point[] _new, old;
                int _newPos=0, oldPos=0;
		point newclip, oldclip;
		int clips_match = 1;
	
		if (old_index < new_index)
			min_index = old_index;
		else
			min_index = new_index;
	
		/* Reset the active clips to invalid values */
		newclip=new point();
		oldclip=new point();
	
		/* Mark vectors which are not the same in both lists as dirty */
		_new = new_list;
		old = old_list;
	
		for (i = min_index; i > 0; i--, oldPos++, _newPos++)
		{
			/* If this is a clip, we need to determine if the clip regions still match */
			if (old[oldPos].status == VCLIP || _new[_newPos].status == VCLIP)
			{
				if (old[oldPos].status == VCLIP)
					oldclip = old[oldPos];
				if (_new[_newPos].status == VCLIP)
					newclip = _new[_newPos];
				clips_match = (newclip.x == oldclip.x) && (newclip.y == oldclip.y) && (newclip.arg1 == oldclip.arg1) && (newclip.arg2 == oldclip.arg2)?1:0;
				if (clips_match==0)
					last_match = 0;
	
				/* fall through to erase the old line if this is not a clip */
				if (old[oldPos].status == VCLIP)
					continue;
			}
	
			/* If the clips match and the vectors match, update */
			else if (clips_match!=0 && (_new[_newPos].x == old[oldPos].x) && (_new[_newPos].y == old[oldPos].y) &&
				(_new[_newPos].col == old[oldPos].col) && (_new[_newPos].intensity == old[oldPos].intensity))
			{
				if (last_match != 0)
				{
					_new[_newPos].status = VCLEAN;
					continue;
				}
				last_match = 1;
			}
	
			/* If nothing matches, remember it */
			else
				last_match = 0;
	
			/* mark the pixels of the old vector dirty */
			coords[coordsPos] = pixel[old[oldPos].arg1];
			for (j = (old[oldPos].arg2 - old[oldPos].arg1); j > 0; j--)
			{
				osd_mark_vector_dirty (coords[coordsPos] >> 16, coords[coordsPos] & 0x0000ffff);
				coordsPos++;
			}
		}
	
		/* all old vector with index greater new_index are dirty */
		/* old = &old_list[min_index] here! */
		for (i = (old_index-min_index); i > 0; i--, oldPos++)
		{
			/* skip old clips */
			if (old[oldPos].status == VCLIP)
				continue;
	
			/* mark the pixels of the old vector dirty */
			coords[coordsPos] = pixel[old[oldPos].arg1];
			for (j = (old[oldPos].arg2 - old[oldPos].arg1); j > 0; j--)
			{
				osd_mark_vector_dirty (coords[coordsPos] >> 16, coords[coordsPos] & 0x0000ffff);
				coordsPos++;
			}
		}
                
                new_list=_new;
		old_list=old;
	}
	
	public static VhUpdatePtr vector_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i;
		int temp_x, temp_y;
		point[] _new;
	
		/* copy parameters */
		vecbitmap = bitmap;
		vecwidth  = bitmap.width;
		vecheight = bitmap.height;
	
		/* setup scaling */
		temp_x = (1<<(44-vecshift)) / (Machine.visible_area.max_x - Machine.visible_area.min_x);
		temp_y = (1<<(44-vecshift)) / (Machine.visible_area.max_y - Machine.visible_area.min_y);
	
		if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0)
		{
			vector_scale_x = temp_x * vecheight;
			vector_scale_y = temp_y * vecwidth;
		}
		else
		{
			vector_scale_x = temp_x * vecwidth;
			vector_scale_y = temp_y * vecheight;
		}
		/* reset clipping area */
		xmin = 0; xmax = vecwidth; ymin = 0; ymax = vecheight;
	
		/* next call to vector_clear_list() is allowed to swap the lists */
		vector_runs = 0;
	
		/* mark pixels which are not idential in newlist and oldlist dirty */
		/* the old pixels which get removed are marked dirty immediately,  */
		/* new pixels are recognized by setting new.dirty                 */
		clever_mark_dirty();
	
		/* clear ALL pixels in the hidden map */
		vector_clear_pixels();
	
		/* Draw ALL lines into the hidden map. Mark only those lines with */
		/* new.dirty = 1 as dirty. Remember the pixel start/end indices  */
		_new = new_list;
                int _newPos=0;
                if (_new[_newPos]==null)
                    _new[_newPos]=new point();
		for (i = 0; i < new_index; i++)
		{
			if (_new[_newPos].status == VCLIP)
				vector_set_clip (_new[_newPos].x, _new[_newPos].y, _new[_newPos].arg1, _new[_newPos].arg2);
			else
			{
				_new[_newPos].arg1 = p_index;
				vector_draw_to (_new[_newPos].x, _new[_newPos].y, _new[_newPos].col, Tgamma[_new[_newPos].intensity], _new[_newPos].status);
	
				_new[_newPos].arg2 = p_index;
			}
			_newPos++;
		}
                
                new_list=_new;
	} };
	
/*TODO*///	#endif /* if !(defined xgl) && !(defined xfx) && !(defined svgafx) */
}
