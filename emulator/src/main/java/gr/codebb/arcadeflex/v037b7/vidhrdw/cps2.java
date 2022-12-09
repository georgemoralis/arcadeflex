/*

  Tatty little tile viewer for CPS2 games

*/



/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v037b7.drivers.cps1.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static gr.codebb.arcadeflex.v036.vidhrdw.cps1draw.*;
import static common.libc.cstring.*;
import static arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.cps1.*;
import static gr.codebb.arcadeflex.v037b7.drivers.cps2.cps2_qsound_sharedram_w;


public class cps2
{
	
	
	static int cps2_start;
	static int cps2_debug;
	static int cps2_width;
	
	static int cps2_gfx_start()
	{
		int dwval;
	    int size=memory_region_length(REGION_GFX1);
	    UBytePtr data = new UBytePtr(memory_region(REGION_GFX1));
		int i,j,nchar,penusage,gfxsize;
	
	    gfxsize=size/4;
	
		/* Set up maximum values */
	    cps1_max_char  =(gfxsize/2)/8;
	    cps1_max_tile16=(gfxsize/4)/8;
	    cps1_max_tile32=(gfxsize/16)/8;
	
		cps1_gfx=new int[gfxsize];
		if (cps1_gfx==null)
		{
			return -1;
		}
	
		cps1_char_pen_usage=new int[cps1_max_char];
		if (cps1_char_pen_usage==null)
		{
			return -1;
		}
		//memset(cps1_char_pen_usage, 0, cps1_max_char*sizeof(int));
	
		cps1_tile16_pen_usage=new int[cps1_max_tile16];
		if (cps1_tile16_pen_usage==null)
			return -1;
		//memset(cps1_tile16_pen_usage, 0, cps1_max_tile16*sizeof(int));
	
		cps1_tile32_pen_usage=new int[cps1_max_tile32];
		if (cps1_tile32_pen_usage==null)
		{
			return -1;
		}
		//memset(cps1_tile32_pen_usage, 0, cps1_max_tile32*sizeof(int));
	
		{
	        for (i=0; i<gfxsize/4; i++)
			{
				nchar=i/8;  /* 8x8 char number */
	            dwval=0;
	            for (j=0; j<8; j++)
	            {
					int n,mask;
					n=0;
					mask=0x80>>j;
					if ((data.read(size/4)&mask)!=0)	   n|=1;
					if ((data.read(size/4+1)&mask)!=0)	 n|=2;
					if ((data.read(size/2+size/4)&mask)!=0)    n|=4;
					if ((data.read(size/2+size/4+1)&mask)!=0)  n|=8;
					dwval|=n<<(28-j*4);
					penusage=1<<n;
	                penusage=0xffff;
					cps1_char_pen_usage[nchar]|=penusage;
					cps1_tile16_pen_usage[nchar/2]|=penusage;
					cps1_tile32_pen_usage[nchar/8]|=penusage;
			   }
			   cps1_gfx[2*i]=dwval;
			   dwval=0;
			   for (j=0; j<8; j++)
			   {
					int n,mask;
					n=0;
					mask=0x80>>j;
					if ((data.read()&mask)!=0)	  n|=1;
					if ((data.read(1)&mask)!=0)	n|=2;
					if ((data.read(size/2)&mask)!=0)   n|=4;
					if ((data.read(size/2+1)&mask)!=0) n|=8;
					dwval|=n<<(28-j*4);
					penusage=1<<n;
					cps1_char_pen_usage[nchar]|=penusage;
					cps1_tile16_pen_usage[nchar/2]|=penusage;
					cps1_tile32_pen_usage[nchar/8]|=penusage;
			   }
			   cps1_gfx[2*i+1]=dwval;
	           data.inc(4);
			}
	
	        data = new UBytePtr(memory_region(REGION_GFX1), 2);
	        for (i=0; i<gfxsize/4; i++)
			{
			   nchar=i/8+(gfxsize/4)/8;  /* 8x8 char number */
			   dwval=0;
			   for (j=0; j<8; j++)
			   {
					int n,mask;
					n=0;
					mask=0x80>>j;
					if ((data.read(size/4)&mask)!=0)	   n|=1;
					if ((data.read(size/4+1)&mask)!=0)	 n|=2;
					if ((data.read(size/2+size/4)&mask)!=0)    n|=4;
					if ((data.read(size/2+size/4+1)&mask)!=0)  n|=8;
					dwval|=n<<(28-j*4);
					penusage=1<<n;
					cps1_char_pen_usage[nchar]|=penusage;
					cps1_tile16_pen_usage[nchar/2]|=penusage;
					cps1_tile32_pen_usage[nchar/8]|=penusage;
			   }
	           cps1_gfx[2*(i+gfxsize/4)]=dwval;
			   dwval=0;
			   for (j=0; j<8; j++)
			   {
					int n,mask;
					n=0;
					mask=0x80>>j;
					if ((data.read()&mask)!=0)	  n|=1;
					if ((data.read(1)&mask)!=0)	n|=2;
					if ((data.read(size/2)&mask)!=0)   n|=4;
					if ((data.read(size/2+1)&mask)!=0) n|=8;
					dwval|=n<<(28-j*4);
					penusage=1<<n;
					cps1_char_pen_usage[nchar]|=penusage;
					cps1_tile16_pen_usage[nchar/2]|=penusage;
					cps1_tile32_pen_usage[nchar/8]|=penusage;
			   }
	           cps1_gfx[2*(i+gfxsize/4)+1]=dwval;
	           data.inc(4);
			}
	
		}
	
	    return 0;
	}
	
	
	public static VhStartPtr cps2_vh_start = new VhStartPtr() { public int handler() 
	{
	    if (cps1_vh_start.handler() != 0)
	    {
	        return -1;
	    }
	    cps1_gfx_stop();
	    cps2_gfx_start();
	
		cps2_start=0;
		cps2_debug=1;	/* Scroll 1 display */
		cps2_width=48;	/* 48 characters wide */
	
		return 0;
	} };
	
	public static VhStopPtr cps2_vh_stop = new VhStopPtr() { public void handler() 
	{
	    cps1_vh_stop.handler();
	} };
	
/*TODO*///	
/*TODO*///	void cps1_debug_tiles_f(struct osd_bitmap *bitmap, int layer, int width)
/*TODO*///	{
/*TODO*///	    int maxy=width/2;
/*TODO*///	    int x,y;
/*TODO*///		int n=cps2_start;
/*TODO*///	
/*TODO*///		/* Blank screen */
/*TODO*///	    fillbitmap(bitmap, palette_transparent_pen, NULL);
/*TODO*///	
/*TODO*///	    for (y=0; y<maxy; y++)
/*TODO*///	    {
/*TODO*///	        for (x=0;x<width;x++)
/*TODO*///	        {
/*TODO*///	            switch (layer)
/*TODO*///	            {
/*TODO*///	                case 1:
/*TODO*///	                    cps1_draw_scroll1(bitmap, n, 0, 0, 0, 32+x*8, 32+y*8, 0xffff);
/*TODO*///	                    break;
/*TODO*///	                case 2:
/*TODO*///	                    cps1_draw_tile16(bitmap, Machine.gfx[2], n, 0, 0, 0, 32+x*16, 32+y*16, 0xffff);
/*TODO*///	                    break;
/*TODO*///	                case 3:
/*TODO*///	                    cps1_draw_tile32(bitmap, Machine.gfx[3], n, 0, 0, 0, 32+x*32, 32+y*32, 0xffff);
/*TODO*///	                    break;
/*TODO*///	            }
/*TODO*///	            n++;
/*TODO*///	        }
/*TODO*///	     }
/*TODO*///	
/*TODO*///	     if (keyboard_pressed(KEYCODE_PGDN))
/*TODO*///	     {
/*TODO*///	        cps2_start+=width*maxy;
/*TODO*///	     }
/*TODO*///	     if (keyboard_pressed(KEYCODE_PGUP))
/*TODO*///	     {
/*TODO*///	        cps2_start-=width*maxy;
/*TODO*///	     }
/*TODO*///	     if (cps2_start < 0)
/*TODO*///	     {
/*TODO*///			cps2_start=0;
/*TODO*///	     }
/*TODO*///	}
	
	
	static void cps1_debug_tiles(osd_bitmap bitmap)
	{
	
/*TODO*///	    if (keyboard_pressed(KEYCODE_1))
/*TODO*///	    {
/*TODO*///	       cps2_debug=1;
/*TODO*///	       cps2_start=0;
/*TODO*///	       cps2_width=48;
/*TODO*///	    }
/*TODO*///	    if (keyboard_pressed(KEYCODE_2))
/*TODO*///	    {
/*TODO*///	       cps2_debug=2;
/*TODO*///	       cps2_start=0;
/*TODO*///	       cps2_width=24;
/*TODO*///	    }
/*TODO*///	    if (keyboard_pressed(KEYCODE_3))
/*TODO*///	    {
/*TODO*///	       cps2_debug=3;
/*TODO*///	       cps2_start=0;
/*TODO*///	       cps2_width=12;
/*TODO*///	    }
/*TODO*///	
/*TODO*///	
/*TODO*///	    if (cps2_debug != 0)
/*TODO*///	    {
/*TODO*///	        cps1_debug_tiles_f(bitmap, cps2_debug, cps2_width);
/*TODO*///	    }
	}
	
        static int qcode;
        
	public static VhUpdatePtr cps2_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
	    
	    int stop=0;
	    int oldq=qcode;
	    int i,offset;
	
	    if (cps1_palette != null)
	    {
	        for (i=0; i<cps1_palette_size; i+=2)
	        {
	            int color=0x0fff+((i&0x0f)<<(8+4));
	            cps1_palette.WRITE_WORD(i, color);
	        }
	    }
	
		/* Get video memory base registers */
		cps1_get_video_base();
	
	    cps1_build_palette();
	   	for (i = offset = 0; i < cps1_palette_entries; i++)
		{
	        int j;
	        for (j = 0; j < 15; j++)
	        {
	           palette_used_colors.write(offset++, PALETTE_COLOR_USED);
	        }
	        palette_used_colors.write(offset++, PALETTE_COLOR_TRANSPARENT);
		}
	
	    palette_recalc ();
	
	    cps1_debug_tiles(bitmap);
	    if (keyboard_pressed_memory(KEYCODE_UP) != 0)
	        qcode++;
	
	    if (keyboard_pressed_memory(KEYCODE_DOWN) != 0)
	        qcode--;
	
	    qcode &= 0xffff;
	
	    if (keyboard_pressed_memory(KEYCODE_ENTER) != 0)
	        stop=0xff;
	
	
	    if (qcode != oldq)
	    {
	        int mode=0;
	        cps2_qsound_sharedram_w.handler(0x1ffa, 0x0088);
	        cps2_qsound_sharedram_w.handler(0x1ffe, 0xffff);
	
	        cps2_qsound_sharedram_w.handler(0x00, 0x0000);
	        cps2_qsound_sharedram_w.handler(0x02, qcode);
	        cps2_qsound_sharedram_w.handler(0x06, 0x0000);
	        cps2_qsound_sharedram_w.handler(0x08, 0x0000);
	        cps2_qsound_sharedram_w.handler(0x0c, mode);
	        cps2_qsound_sharedram_w.handler(0x0e, 0x0010);
	        cps2_qsound_sharedram_w.handler(0x10, 0x0000);
	        cps2_qsound_sharedram_w.handler(0x12, 0x0000);
	        cps2_qsound_sharedram_w.handler(0x14, 0x0000);
	        cps2_qsound_sharedram_w.handler(0x16, 0x0000);
	        cps2_qsound_sharedram_w.handler(0x18, 0x0000);
	        cps2_qsound_sharedram_w.handler(0x1e, 0x0000);
	    }
/*TODO*///	    {
/*TODO*///	    struct DisplayText dt[3];
/*TODO*///	    char *instructions="PRESS: PGUP/PGDN=CODE  1=8x8  2=16x16  3=32x32  UP/DN=QCODE";
/*TODO*///	    char text1[256];
/*TODO*///	    sprintf(text1, "GFX CODE=%06x  :  QSOUND CODE=%04x", cps2_start, qcode );
/*TODO*///	    dt[0].text = text1;
/*TODO*///	    dt[0].color = UI_COLOR_INVERSE;
/*TODO*///	    dt[0].x = (Machine.uiwidth - Machine.uifontwidth * strlen(text1)) / 2;
/*TODO*///	    dt[0].y = 8*23;
/*TODO*///	    dt[1].text = instructions;
/*TODO*///	    dt[1].color = UI_COLOR_NORMAL;
/*TODO*///	    dt[1].x = (Machine.uiwidth - Machine.uifontwidth * strlen(instructions)) / 2;
/*TODO*///	    dt[1].y = dt[0].y+2*Machine.uifontheight;
/*TODO*///	
/*TODO*///	    dt[2].text = 0; /* terminate array */
/*TODO*///		displaytext(Machine.scrbitmap,dt,0,0);
/*TODO*///	    }
	} };
	
	
	
	
	
}
