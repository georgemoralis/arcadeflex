/*
 * Aztarac vector generator emulation
 *
 * Jul 25 1999 by Mathis Rosenhauer
 *
 */

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.vidhrdw.vector.*;

public class aztarac
{
	
	public static int VEC_SHIFT = 16;
	
	public static UBytePtr aztarac_vectorram=new UBytePtr();
	
	static int xcenter, ycenter;
	
	public static void read_vectorram (int addr, int[] x, int[] y, int[] c)
	{
	    addr <<= 1;
	    c[0] = aztarac_vectorram.READ_WORD (addr) & 0xffff;
	    x[0] = aztarac_vectorram.READ_WORD (addr + 0x1000) & 0x03ff;
	    y[0] = aztarac_vectorram.READ_WORD (addr + 0x2000) & 0x03ff;
	    if ((x[0] & 0x200)!=0) x[0] |= 0xfffffc00;
	    if ((y[0] & 0x200)!=0) y[0] |= 0xfffffc00;
	}
	
	public static void aztarac_vector (int x, int y, int color, int intensity)
	{
	    if (translucency != 0) intensity *= 0.8;
	    vector_add_point (xcenter + (x << VEC_SHIFT), ycenter - (y << VEC_SHIFT), color, intensity);
	}
	
	public static WriteHandlerPtr aztarac_ubr_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	    int intensity, color;
            int[] x=new int[1], y=new int[1], c=new int[1], xoffset=new int[1], yoffset=new int[1];
            int ndefs=0;
	    int defaddr, objaddr=0;
	
	    if ((data & 1) != 0)
	    {
	        vector_clear_list();
	
	        while (true)
	        {
	            read_vectorram (objaddr, xoffset, yoffset, c);
	            objaddr++;
	
	            if ((c[0] & 0x4000) != 0)
	                break;
	
	            if ((c[0] & 0x2000) == 0)
	            {
	                defaddr = (c[0] >> 1) & 0x7ff;
	                aztarac_vector (xoffset[0], yoffset[0], 0, 0);
                        int[] _nd=new int[1];
	
	                read_vectorram (defaddr, x, _nd, c);
                        ndefs=_nd[0];
                        
	                ndefs++;
	
	                if (c[0] != 0)
	                {
	                    /* latch color only once */
	                    intensity = c[0] >> 8;
	                    color = c[0] & 0x3f;
	                    while (ndefs != 0)
	                    {
                                ndefs--;
	                        defaddr++;
	                        read_vectorram (defaddr, x, y, c);
	                        if ((c[0] & 0xff00) == 0)
	                            aztarac_vector (x[0] + xoffset[0], y[0] + yoffset[0], 0, 0);
	                        else
	                            aztarac_vector (x[0] + xoffset[0], y[0] + yoffset[0], color, intensity);
	                    }
	                }
	                else
	                {
	                    /* latch color for every definition */
	                    while (ndefs!=0)
	                    {
                                ndefs--;
	                        defaddr++;
	                        read_vectorram (defaddr, x, y, c);
	                        aztarac_vector (x[0] + xoffset[0], y[0] + yoffset[0], c[0] & 0x3f, c[0] >> 8);
	                    }
	                }
	            }
	        }
	    }
	} };
	
	public static InterruptPtr aztarac_vg_interrupt = new InterruptPtr() { public int handler() 
	{
	    return 4;
	} };
	
	public static VhConvertColorPromPtr aztarac_init_colors = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
	    int r, g, b, i;
            int _palette=0;
	
	    for (i = 4; i > 0; i--)
	        for (r = 0; r < 4; r++)
	            for (g = 0; g < 4; g++)
	                for (b = 0; b < 4; b++)
	                {
	                    palette[_palette++] = (char) ((255 * r * i)/ 12);
	                    palette[_palette++] = (char) ((255 * g * i)/ 12);
	                    palette[_palette++] = (char) ((255 * b * i)/ 12);
	                }
	} };
	
	public static VhStartPtr aztarac_vh_start = new VhStartPtr() { public int handler() 
	{
	    int xmin, xmax, ymin, ymax;
	
	
		xmin = Machine.visible_area.min_x;
		ymin = Machine.visible_area.min_y;
		xmax = Machine.visible_area.max_x;
		ymax = Machine.visible_area.max_y;
	
		xcenter=((xmax + xmin) / 2) << VEC_SHIFT;
		ycenter=((ymax + ymin) / 2) << VEC_SHIFT;
	
		vector_set_shift (VEC_SHIFT);
		return vector_vh_start.handler();
	} };
}
