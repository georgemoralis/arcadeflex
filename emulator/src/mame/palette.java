
package mame;

import static mame.mame.*;
import static mame.driverH.*;
import static mame.paletteH.*;
import static arcadeflex.libc.*;
import static arcadeflex.libc_old.*;
import static mame.mameH.*;
import static mame.commonH.*;
import static mame.common.*;
import static arcadeflex.video.*;


public class palette {
    public static UByte[] game_palette;	/* RGB palette as set by the driver. */
    public static UBytePtr new_palette;	/* changes to the palette are stored here before */
    						/* being moved to game_palette by palette_recalc() */
    public static UBytePtr palette_dirty;
    /* arrays which keep track of colors actually used, to help in the palette shrinking. */
    public static UBytePtr palette_used_colors;
    public static UBytePtr old_used_colors;
    public static IntPtr pen_visiblecount;
    public static IntPtr pen_cachedcount;//static int *pen_visiblecount,*pen_cachedcount;
    
    public static UBytePtr just_remapped;	/* colors which have been remapped in this frame, */
    						/* returned by palette_recalc() */
    
    static int use_16bit;
    public static final int NO_16BIT		= 0;
    public static final int STATIC_16BIT	= 1;
    public static final int PALETTIZED_16BIT	= 2;

    static int total_shrinked_pens;
    public static char[] shrinked_pens;
    public static UByte[] shrinked_palette;
    public static char[] palette_map;	/* map indexes from game_palette to shrinked_palette */

    static char[] pen_usage_count = new char[DYNAMIC_MAX_PENS];

    public static char palette_transparent_pen;
    public static int palette_transparent_color;
    /*TODO*///
    /*TODO*///
    public static final int BLACK_PEN		= 0;
    public static final int TRANSPARENT_PEN	= 1;
    public static final int RESERVED_PENS	= 2;
    
    public static final int PALETTE_COLOR_NEEDS_REMAP =0x80;
    /*TODO*///
    /*TODO*////* helper macro for 16-bit mode */
    /*TODO*///#define rgbpenindex(r,g,b) ((Machine->scrbitmap->depth==16) ? ((((r)>>3)<<10)+(((g)>>3)<<5)+((b)>>3)) : ((((r)>>5)<<5)+(((g)>>5)<<2)+((b)>>6)))
    /*TODO*///
    /*TODO*///
    /*TODO*///unsigned short *palette_shadow_table;
    /*TODO*///unsigned short *palette_highlight_table;
    /*TODO*///
    /*TODO*///
    /*TODO*///
    public static int palette_start()
    {
    /*TODO*///	int i,num;
    /*TODO*///
    /*TODO*///
        game_palette = new UByte[3* Machine.drv.total_colors];//malloc(3 * Machine->drv->total_colors * sizeof(unsigned char));
        for(int i=0; i<game_palette.length; i++) game_palette[i]=new UByte();
        palette_map = new char[Machine.drv.total_colors];//malloc(Machine->drv->total_colors * sizeof(unsigned short));

    	if (Machine.drv.color_table_len!=0)
    	{
    	    Machine.game_colortable =   new char[Machine.drv.color_table_len];//malloc(Machine->drv->color_table_len * sizeof(unsigned short));
   	    Machine.remapped_colortable = new char[Machine.drv.color_table_len];//malloc(Machine->drv->color_table_len * sizeof(unsigned short));
    	}
    	else
        {
           Machine.game_colortable = Machine.remapped_colortable = null;
        }
    	if (Machine.color_depth == 16 || ((Machine.gamedrv.flags & GAME_REQUIRES_16BIT)!=0))
    	{
    		if (Machine.color_depth == 8 || Machine.drv.total_colors > 65532)
    			use_16bit = STATIC_16BIT;
    		else
    			use_16bit = PALETTIZED_16BIT;
    	}
    	else
        {
    		use_16bit = NO_16BIT;
        }
    
    	switch (use_16bit)
    	{
    		case NO_16BIT:
    			if ((Machine.drv.video_attributes & VIDEO_MODIFIES_PALETTE)!=0)
    				total_shrinked_pens = DYNAMIC_MAX_PENS;
    			else
    				total_shrinked_pens = STATIC_MAX_PENS;
    			break;
    		case STATIC_16BIT:
    			total_shrinked_pens = 32768;
    			break;
    		case PALETTIZED_16BIT:
    			total_shrinked_pens = Machine.drv.total_colors + RESERVED_PENS;
    			break;
    	}
    
        shrinked_pens = new char[total_shrinked_pens];//malloc(total_shrinked_pens * sizeof(short));
        shrinked_palette = new UByte[3*total_shrinked_pens];//malloc(3 * total_shrinked_pens * sizeof(unsigned char));
        for(int i=0; i<shrinked_palette.length; i++) shrinked_palette[i] = new UByte();
    
        Machine.pens = new char[Machine.drv.total_colors];//malloc(Machine->drv->total_colors * sizeof(short));
    
    	if (((Machine.drv.video_attributes & VIDEO_MODIFIES_PALETTE)!=0))
    	{
            
            
   		/* if the palette changes dynamically, */
    		/* we'll need the usage arrays to help in shrinking. */
    		palette_used_colors = new UBytePtr((1+1+1+3+1) * Machine.drv.total_colors);//malloc((1+1+1+3+1) * Machine->drv->total_colors * sizeof(unsigned char));
    		pen_visiblecount = new IntPtr(2 * Machine.drv.total_colors);//malloc(2 * Machine->drv->total_colors * sizeof(int));
    
    		if (palette_used_colors == null || pen_visiblecount == null)
    		{
    			palette_stop();
    			return 1;
    		}
    
    		old_used_colors = new UBytePtr(palette_used_colors,Machine.drv.total_colors);//palette_used_colors + Machine->drv->total_colors * sizeof(unsigned char);
    		just_remapped =  new UBytePtr(old_used_colors,Machine.drv.total_colors);//old_used_colors + Machine->drv->total_colors * sizeof(unsigned char);
    		new_palette = new UBytePtr(just_remapped,Machine.drv.total_colors);//just_remapped + Machine->drv->total_colors * sizeof(unsigned char);
    		palette_dirty = new UBytePtr(new_palette,3*Machine.drv.total_colors);//new_palette + 3*Machine->drv->total_colors * sizeof(unsigned char);
    		//memset(palette_used_colors,PALETTE_COLOR_USED,Machine.drv.total_colors /* * sizeof(unsigned char)*/);
    		for (int mem = 0; mem < Machine.drv.total_colors; mem++) 
                    palette_used_colors.write(mem,PALETTE_COLOR_USED);
                //memset(old_used_colors,PALETTE_COLOR_UNUSED,Machine.drv.total_colors/* * sizeof(unsigned char)*/);
    		for (int mem = 0; mem < Machine.drv.total_colors; mem++) 
                    old_used_colors.write(mem,PALETTE_COLOR_UNUSED);
                //memset(palette_dirty,0,Machine.drv.total_colors /* * sizeof(unsigned char)*/);
    		for (int mem = 0; mem < Machine.drv.total_colors; mem++) 
                    palette_dirty.write(mem,0);
                pen_cachedcount = new IntPtr(pen_visiblecount,Machine.drv.total_colors);
    		//memset(pen_visiblecount,0,Machine.drv.total_colors /* * sizeof(int)*/);
    		//memset(pen_cachedcount,0,Machine.drv.total_colors /* * sizeof(int)*/);
                for (int i = 0; i < Machine.drv.total_colors; i++)
                {
   /*TODO*///                 //pen_visiblecount.write(i,0);//TODO!!!
   /*TODO*///                 //pen_cachedcount[i] = 0;
                }
       
    	}
    	else 
        {
            palette_used_colors = old_used_colors = just_remapped = new_palette = palette_dirty = null;
        }
    /*TODO*///
    /*TODO*///	if (Machine->color_depth == 8) num = 256;
    /*TODO*///	else num = 65536;
    /*TODO*///	palette_shadow_table = malloc(2 * num * sizeof(unsigned short));
    /*TODO*///	if (palette_shadow_table == 0)
    /*TODO*///	{
    /*TODO*///		palette_stop();
    /*TODO*///		return 1;
    /*TODO*///	}
    /*TODO*///	palette_highlight_table = palette_shadow_table + num;
    /*TODO*///	for (i = 0;i < num;i++)
    /*TODO*///		palette_shadow_table[i] = palette_highlight_table[i] = i;
    /*TODO*///
    /*TODO*///	if ((Machine->drv->color_table_len && (Machine->game_colortable == 0 || Machine->remapped_colortable == 0))
    /*TODO*///			|| game_palette == 0 ||	palette_map == 0
    /*TODO*///			|| shrinked_pens == 0 || shrinked_palette == 0 || Machine->pens == 0)
    /*TODO*///	{
    /*TODO*///		palette_stop();
    /*TODO*///		return 1;
    /*TODO*///	}
    /*TODO*///
    	return 0;
    }
    
    public static void palette_stop()
    {
    /*TODO*///	free(palette_used_colors);
    /*TODO*///	palette_used_colors = old_used_colors = just_remapped = new_palette = palette_dirty = 0;
    /*TODO*///	free(pen_visiblecount);
    /*TODO*///	pen_visiblecount = 0;
    /*TODO*///	free(game_palette);
    /*TODO*///	game_palette = 0;
    /*TODO*///	free(palette_map);
    /*TODO*///	palette_map = 0;
    /*TODO*///	free(Machine->game_colortable);
    /*TODO*///	Machine->game_colortable = 0;
    /*TODO*///	free(Machine->remapped_colortable);
    /*TODO*///	Machine->remapped_colortable = 0;
    /*TODO*///	free(shrinked_pens);
    /*TODO*///	shrinked_pens = 0;
    /*TODO*///	free(shrinked_palette);
    /*TODO*///	shrinked_palette = 0;
    /*TODO*///	free(Machine->pens);
    /*TODO*///	Machine->pens = 0;
    /*TODO*///	free(palette_shadow_table);
    /*TODO*///	palette_shadow_table = 0;
    }
    
    
    
    public static int palette_init()
    {
    	int i;
    
    
    	/* We initialize the palette and colortable to some default values so that */
    	/* drivers which dynamically change the palette don't need a vh_init_palette() */
    	/* function (provided the default color table fits their needs). */
    
    	for (i = 0;i < Machine.drv.total_colors;i++)
    	{
    		game_palette[3*i + 0].set((char)(((i & 1) >> 0) * 0xff));
    		game_palette[3*i + 1].set((char)(((i & 2) >> 1) * 0xff));
    		game_palette[3*i + 2].set((char)(((i & 4) >> 2) * 0xff));
    	}
    
    	/* Preload the colortable with a default setting, following the same */
    	/* order of the palette. The driver can overwrite this in */
    	/* vh_init_palette() */
    	for (i = 0;i < Machine.drv.color_table_len;i++)
    		Machine.game_colortable[i] =(char)( i % Machine.drv.total_colors);
    
    	/* by default we use -1 to identify the transparent color, the driver */
    	/* can modify this. */
    	palette_transparent_color = -1;
    
    	/* now the driver can modify the default values if it wants to. */
    	if (Machine.drv.vh_init_palette!=null)
        {
            Machine.drv.vh_init_palette.handler(game_palette,Machine.game_colortable,memory_region(REGION_PROMS));
        }
    
    
    	switch (use_16bit)
    	{
    		case NO_16BIT:
    		{
    			/* initialize shrinked palette to all black */
    			for (i = 0;i < total_shrinked_pens;i++)
    			{
    				shrinked_palette[3*i + 0].set((char)0);
    				shrinked_palette[3*i + 1].set((char)0);
    				shrinked_palette[3*i + 2].set((char)0);
    			}
    
    			if ((Machine.drv.video_attributes & VIDEO_MODIFIES_PALETTE)!=0)
    			{
                            
    				/* initialize pen usage counters */
    				for (i = 0;i < DYNAMIC_MAX_PENS;i++)
    					pen_usage_count[i] = 0;
    
    				/* allocate two fixed pens at the beginning: */
    				/* transparent black */
    				pen_usage_count[TRANSPARENT_PEN] = 1;	/* so the pen will not be reused */
    
    				/* non transparent black */
    				pen_usage_count[BLACK_PEN] = 1;
    
    				/* create some defaults associations of game colors to shrinked pens. */
    				/* They will be dynamically modified at run time. */
    				for (i = 0;i < Machine.drv.total_colors;i++)
    					palette_map[i] = (char)((i & 7) + 8);
    
    				if ((osd_allocate_colors(total_shrinked_pens,shrinked_palette,shrinked_pens,1))!=0)
    					return 1;
    			}
    			else
    			{
    				int j,used;
    
    
                                if (errorlog!=null) fprintf(errorlog,"shrinking %d colors palette...\n",Machine.drv.total_colors);
    
    				/* shrink palette to fit */
    				used = 0;
    
    				for (i = 0;i < Machine.drv.total_colors;i++)
    				{
    					for (j = 0;j < used;j++)
    					{
    						if (	shrinked_palette[3*j + 0].read() == game_palette[3*i + 0].read() &&
    								shrinked_palette[3*j + 1].read() == game_palette[3*i + 1].read() &&
    								shrinked_palette[3*j + 2].read() == game_palette[3*i + 2].read())
    							break;
    					}
    
    					palette_map[i] = (char)j;
    
    					if (j == used)
    					{
    						used++;
    						if (used > total_shrinked_pens)
    						{
                                                     throw new UnsupportedOperationException("error: ran out of free pens to shrink the palette");
    /*TODO*///							used = total_shrinked_pens;
    /*TODO*///							palette_map[i] = total_shrinked_pens-1;
    /*TODO*///							usrintf_showmessage("cannot shrink static palette");
    /*TODO*///                                          if (errorlog) fprintf(errorlog,"error: ran out of free pens to shrink the palette.\n");
    						}
    						else
    						{
    							shrinked_palette[3*j + 0].set((char)game_palette[3*i + 0].read());
    							shrinked_palette[3*j + 1].set((char)game_palette[3*i + 1].read());
    							shrinked_palette[3*j + 2].set((char)game_palette[3*i + 2].read());
    						}
    					}
    				}
    
                                if (errorlog!=null) fprintf(errorlog,"shrinked palette uses %d colors\n",used);
    
    				if (osd_allocate_colors(used,shrinked_palette,shrinked_pens,0)!=0)
   					return 1;
    			}
    
   
    			for (i = 0;i < Machine.drv.total_colors;i++)
    				Machine.pens[i] = shrinked_pens[palette_map[i]];
    
    			palette_transparent_pen = shrinked_pens[TRANSPARENT_PEN];	/* for dynamic palette games */
    		}
    		break;
    
    		case STATIC_16BIT:
    		{
                    throw new UnsupportedOperationException("palette_init() STATIC_16BIT unimplemented");
    /*TODO*///			unsigned char *p = shrinked_palette;
    /*TODO*///			int r,g,b;
    /*TODO*///
    /*TODO*///			if (Machine->scrbitmap->depth == 16)
    /*TODO*///			{
    /*TODO*///				for (r = 0;r < 32;r++)
    /*TODO*///				{
    /*TODO*///					for (g = 0;g < 32;g++)
    /*TODO*///					{
    /*TODO*///						for (b = 0;b < 32;b++)
    /*TODO*///						{
    /*TODO*///							*p++ = (r << 3) | (r >> 2);
    /*TODO*///							*p++ = (g << 3) | (g >> 2);
    /*TODO*///							*p++ = (b << 3) | (b >> 2);
    /*TODO*///						}
    /*TODO*///					}
    /*TODO*///				}
    /*TODO*///
    /*TODO*///				if (osd_allocate_colors(32768,shrinked_palette,shrinked_pens,0))
    /*TODO*///					return 1;
    /*TODO*///			}
    /*TODO*///			else
    /*TODO*///			{
    /*TODO*///				for (r = 0;r < 8;r++)
    /*TODO*///				{
    /*TODO*///					for (g = 0;g < 8;g++)
    /*TODO*///					{
    /*TODO*///						for (b = 0;b < 4;b++)
    /*TODO*///						{
    /*TODO*///							*p++ = (r << 5) | (r << 2) | (r >> 1);
    /*TODO*///							*p++ = (g << 5) | (g << 2) | (g >> 1);
    /*TODO*///							*p++ = (b << 6) | (b << 4) | (b << 2) | b;
    /*TODO*///						}
    /*TODO*///					}
    /*TODO*///				}
    /*TODO*///
    /*TODO*///				if (osd_allocate_colors(256,shrinked_palette,shrinked_pens,0))
    /*TODO*///					return 1;
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			for (i = 0;i < Machine->drv->total_colors;i++)
    /*TODO*///			{
    /*TODO*///				r = game_palette[3*i + 0];
    /*TODO*///				g = game_palette[3*i + 1];
    /*TODO*///				b = game_palette[3*i + 2];
    /*TODO*///
    /*TODO*///				Machine->pens[i] = shrinked_pens[rgbpenindex(r,g,b)];
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			palette_transparent_pen = shrinked_pens[0];	/* we are forced to use black for the transparent pen */
    		}
    /*TODO*///		break;
    
    		case PALETTIZED_16BIT:
    		{
                    throw new UnsupportedOperationException("palette_init() PALETTIZED_16BIT unimplemented");
    /*TODO*///			for (i = 0;i < RESERVED_PENS;i++)
    /*TODO*///			{
    /*TODO*///				shrinked_palette[3*i + 0] =
    /*TODO*///				shrinked_palette[3*i + 1] =
    /*TODO*///				shrinked_palette[3*i + 2] = 0;
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			for (i = 0;i < Machine->drv->total_colors;i++)
    /*TODO*///			{
    /*TODO*///				shrinked_palette[3*(i+RESERVED_PENS) + 0] = game_palette[3*i + 0];
    /*TODO*///				shrinked_palette[3*(i+RESERVED_PENS) + 1] = game_palette[3*i + 1];
    /*TODO*///				shrinked_palette[3*(i+RESERVED_PENS) + 2] = game_palette[3*i + 2];
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			if (osd_allocate_colors(total_shrinked_pens,shrinked_palette,shrinked_pens,(Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE)))
    /*TODO*///				return 1;
    /*TODO*///
    /*TODO*///			for (i = 0;i < Machine->drv->total_colors;i++)
    /*TODO*///				Machine->pens[i] = shrinked_pens[i + RESERVED_PENS];
    /*TODO*///
    /*TODO*///			palette_transparent_pen = shrinked_pens[TRANSPARENT_PEN];	/* for dynamic palette games */
    		}
    /*TODO*///		break;
    	}
    
    	for (i = 0;i < Machine.drv.color_table_len;i++)
    	{
    		int color = Machine.game_colortable[i];
    
   		/* check for invalid colors set by Machine->drv->vh_init_palette */
    		if (color < Machine.drv.total_colors)
    			Machine.remapped_colortable[i]=Machine.pens[color];
    /*TODO*///		else
    /*TODO*///			usrintf_showmessage("colortable[%d] (=%d) out of range (total_colors = %d)",
    /*TODO*///					i,color,Machine->drv->total_colors);
    	}
    /*TODO*///
    	return 0;
    }
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///INLINE void palette_change_color_16_static(int color,unsigned char red,unsigned char green,unsigned char blue)
    /*TODO*///{
    /*TODO*///	if (color == palette_transparent_color)
    /*TODO*///	{
    /*TODO*///		int i;
    /*TODO*///
    /*TODO*///
    /*TODO*///		palette_transparent_pen = shrinked_pens[rgbpenindex(red,green,blue)];
    /*TODO*///
    /*TODO*///		if (color == -1) return;	/* by default, palette_transparent_color is -1 */
    /*TODO*///
    /*TODO*///		for (i = 0;i < Machine->drv->total_colors;i++)
    /*TODO*///		{
    /*TODO*///			if ((old_used_colors[i] & (PALETTE_COLOR_VISIBLE | PALETTE_COLOR_TRANSPARENT_FLAG))
    /*TODO*///					== (PALETTE_COLOR_VISIBLE | PALETTE_COLOR_TRANSPARENT_FLAG))
    /*TODO*///				old_used_colors[i] |= PALETTE_COLOR_NEEDS_REMAP;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	if (	game_palette[3*color + 0] == red &&
    /*TODO*///			game_palette[3*color + 1] == green &&
    /*TODO*///			game_palette[3*color + 2] == blue)
    /*TODO*///		return;
    /*TODO*///
    /*TODO*///	game_palette[3*color + 0] = red;
    /*TODO*///	game_palette[3*color + 1] = green;
    /*TODO*///	game_palette[3*color + 2] = blue;
    /*TODO*///
    /*TODO*///	if (old_used_colors[color] & PALETTE_COLOR_VISIBLE)
    /*TODO*///		/* we'll have to reassign the color in palette_recalc() */
    /*TODO*///		old_used_colors[color] |= PALETTE_COLOR_NEEDS_REMAP;
    /*TODO*///}
    /*TODO*///
    /*TODO*///INLINE void palette_change_color_16_palettized(int color,unsigned char red,unsigned char green,unsigned char blue)
    /*TODO*///{
    /*TODO*///	if (color == palette_transparent_color)
    /*TODO*///	{
    /*TODO*///		osd_modify_pen(palette_transparent_pen,red,green,blue);
    /*TODO*///
    /*TODO*///		if (color == -1) return;	/* by default, palette_transparent_color is -1 */
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	if (	game_palette[3*color + 0] == red &&
    /*TODO*///			game_palette[3*color + 1] == green &&
    /*TODO*///			game_palette[3*color + 2] == blue)
    /*TODO*///		return;
    /*TODO*///
    /*TODO*///	/* Machine->pens[color] might have been remapped to transparent_pen, so I */
    /*TODO*///	/* use shrinked_pens[] directly */
    /*TODO*///	osd_modify_pen(shrinked_pens[color + RESERVED_PENS],red,green,blue);
    /*TODO*///	game_palette[3*color + 0] = red;
    /*TODO*///	game_palette[3*color + 1] = green;
    /*TODO*///	game_palette[3*color + 2] = blue;
    /*TODO*///}
    /*TODO*///
    public static void palette_change_color_8(int color,int red,int green,int blue)
    {
    	int pen;
    
    	if (color == palette_transparent_color)
    	{
            
    		osd_modify_pen(palette_transparent_pen,red,green,blue);
    
    		if (color == -1) return;	/* by default, palette_transparent_color is -1 */
    	}
    
    	if (	game_palette[3*color + 0].read() == red &&
    			game_palette[3*color + 1].read() == green &&
    			game_palette[3*color + 2].read() == blue)
    	{
    		palette_dirty.write(color,0);
    		return;
    	}
    
    	pen = palette_map[color];
    
    	/* if the color was used, mark it as dirty, we'll change it in palette_recalc() */
    	if ((old_used_colors.read(color) & PALETTE_COLOR_VISIBLE)!=0)
    	{
    		new_palette.write(3*color + 0,red);
    		new_palette.write(3*color + 1,green);
    		new_palette.write(3*color + 2,blue);
    		palette_dirty.write(color,1);
    	}
    	/* otherwise, just update the array */
    	else
    	{
    		game_palette[3*color + 0].set((char)red);
    		game_palette[3*color + 1].set((char)green);
    		game_palette[3*color + 2].set((char)blue);
    	}
    }
    
    public static void palette_change_color(int color,int red,int green,int blue)
    {

    	if ((Machine.drv.video_attributes & VIDEO_MODIFIES_PALETTE) == 0)
    	{
            if (errorlog!=null) fprintf(errorlog,"Error: palette_change_color() called, but VIDEO_MODIFIES_PALETTE not set.\n");
    		return;
    	}
    
    	if (color >= Machine.drv.total_colors)
    	{
            if (errorlog!=null) fprintf(errorlog,"error: palette_change_color() called with color %d, but only %d allocated.\n",color,Machine.drv.total_colors);
            return;
    	}
    
    	switch (use_16bit)
    	{
    		case NO_16BIT:
    			palette_change_color_8(color,red,green,blue);
    			break;
    		case STATIC_16BIT:
                             throw new UnsupportedOperationException("palette change color");
    /*TODO*///			palette_change_color_16_static(color,red,green,blue);
    /*TODO*///			break;
    		case PALETTIZED_16BIT:
                            throw new UnsupportedOperationException("palette change color");
    /*TODO*///			palette_change_color_16_palettized(color,red,green,blue);
    /*TODO*///			break;
    	}
   }
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///void palette_increase_usage_count(int table_offset,unsigned int usage_mask,int color_flags)
    /*TODO*///{
    /*TODO*///	/* if we are not dynamically reducing the palette, return immediately. */
    /*TODO*///	if (palette_used_colors == 0) return;
    /*TODO*///
    /*TODO*///	while (usage_mask)
    /*TODO*///	{
    /*TODO*///		if (usage_mask & 1)
    /*TODO*///		{
    /*TODO*///			if (color_flags & PALETTE_COLOR_VISIBLE)
    /*TODO*///				pen_visiblecount[Machine->game_colortable[table_offset]]++;
    /*TODO*///			if (color_flags & PALETTE_COLOR_CACHED)
    /*TODO*///				pen_cachedcount[Machine->game_colortable[table_offset]]++;
    /*TODO*///		}
    /*TODO*///		table_offset++;
    /*TODO*///		usage_mask >>= 1;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///void palette_decrease_usage_count(int table_offset,unsigned int usage_mask,int color_flags)
    /*TODO*///{
    /*TODO*///	/* if we are not dynamically reducing the palette, return immediately. */
    /*TODO*///	if (palette_used_colors == 0) return;
    /*TODO*///
    /*TODO*///	while (usage_mask)
    /*TODO*///	{
    /*TODO*///		if (usage_mask & 1)
    /*TODO*///		{
    /*TODO*///			if (color_flags & PALETTE_COLOR_VISIBLE)
    /*TODO*///				pen_visiblecount[Machine->game_colortable[table_offset]]--;
    /*TODO*///			if (color_flags & PALETTE_COLOR_CACHED)
    /*TODO*///				pen_cachedcount[Machine->game_colortable[table_offset]]--;
    /*TODO*///		}
    /*TODO*///		table_offset++;
    /*TODO*///		usage_mask >>= 1;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///void palette_increase_usage_countx(int table_offset,int num_pens,const unsigned char *pen_data,int color_flags)
    /*TODO*///{
    /*TODO*///	char flag[256];
    /*TODO*///	memset(flag,0,256);
    /*TODO*///
    /*TODO*///	while (num_pens--)
    /*TODO*///	{
    /*TODO*///		int pen = pen_data[num_pens];
    /*TODO*///		if (flag[pen] == 0)
    /*TODO*///		{
    /*TODO*///			if (color_flags & PALETTE_COLOR_VISIBLE)
    /*TODO*///				pen_visiblecount[Machine->game_colortable[table_offset+pen]]++;
    /*TODO*///			if (color_flags & PALETTE_COLOR_CACHED)
    /*TODO*///				pen_cachedcount[Machine->game_colortable[table_offset+pen]]++;
    /*TODO*///			flag[pen] = 1;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///void palette_decrease_usage_countx(int table_offset, int num_pens, const unsigned char *pen_data,int color_flags)
    /*TODO*///{
    /*TODO*///	char flag[256];
    /*TODO*///	memset(flag,0,256);
    /*TODO*///
    /*TODO*///	while (num_pens--)
    /*TODO*///	{
    /*TODO*///		int pen = pen_data[num_pens];
    /*TODO*///		if (flag[pen] == 0)
    /*TODO*///		{
    /*TODO*///			if (color_flags & PALETTE_COLOR_VISIBLE)
    /*TODO*///				pen_visiblecount[Machine->game_colortable[table_offset+pen]]--;
    /*TODO*///			if (color_flags & PALETTE_COLOR_CACHED)
    /*TODO*///				pen_cachedcount[Machine->game_colortable[table_offset+pen]]--;
    /*TODO*///			flag[pen] = 1;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///void palette_init_used_colors(void)
    /*TODO*///{
    /*TODO*///	int pen;
    /*TODO*///
    /*TODO*///
    /*TODO*///	/* if we are not dynamically reducing the palette, return immediately. */
    /*TODO*///	if (palette_used_colors == 0) return;
    /*TODO*///
    /*TODO*///	memset(palette_used_colors,PALETTE_COLOR_UNUSED,Machine->drv->total_colors * sizeof(unsigned char));
    /*TODO*///
    /*TODO*///	for (pen = 0;pen < Machine->drv->total_colors;pen++)
    /*TODO*///	{
    /*TODO*///		if (pen_visiblecount[pen]) palette_used_colors[pen] |= PALETTE_COLOR_VISIBLE;
    /*TODO*///		if (pen_cachedcount[pen]) palette_used_colors[pen] |= PALETTE_COLOR_CACHED;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///
    static char[][][] rgb6_to_pen=new char[64][64][64];
    
    public static void build_rgb_to_pen()
    {
    	int i,rr,gg,bb;
    
    /*TODO*///	memset(rgb6_to_pen,DYNAMIC_MAX_PENS,sizeof(rgb6_to_pen));
        for (int k = 0; k < 64; k++)
        {
                for (int j = 0; j < 64; j++)
                    for (int l = 0; l < 64; l++)
                        rgb6_to_pen[k][j][l] = DYNAMIC_MAX_PENS;
         }
            
    	rgb6_to_pen[0][0][0] = BLACK_PEN;
    
    	for (i = 0;i < DYNAMIC_MAX_PENS;i++)
    	{
    		if (pen_usage_count[i] > 0)
    		{
    			rr = shrinked_palette[3*i + 0].read() >> 2;
    			gg = shrinked_palette[3*i + 1].read() >> 2;
    			bb = shrinked_palette[3*i + 2].read() >> 2;
    
    			if (rgb6_to_pen[rr][gg][bb] == DYNAMIC_MAX_PENS)
    			{
    				int j,max;
    
    				rgb6_to_pen[rr][gg][bb] = (char)i;
    				max = pen_usage_count[i];
    
    				/* to reduce flickering during remaps, find the pen used by most colors */
    				for (j = i+1;j < DYNAMIC_MAX_PENS;j++)
    				{
    					if (pen_usage_count[j] > max &&
    							rr == (shrinked_palette[3*j + 0].read() >> 2) &&
    							gg == (shrinked_palette[3*j + 1].read() >> 2) &&
    							bb == (shrinked_palette[3*j + 2].read() >> 2))
    					{
    						rgb6_to_pen[rr][gg][bb] = (char)j;
    						max = pen_usage_count[j];
    					}
    				}
    			}
    		}
    	}
    }
    
    public static int compress_palette()
    {
    	int i,j,saved,r,g,b;
    
    
    	build_rgb_to_pen();
    
    	saved = 0;
    
    	for (i = 0;i < Machine.drv.total_colors;i++)
    	{
    		/* merge pens of the same color */
    		if (((old_used_colors.read(i) & PALETTE_COLOR_VISIBLE)!=0) &&
    				((old_used_colors.read(i) & (PALETTE_COLOR_NEEDS_REMAP|PALETTE_COLOR_TRANSPARENT_FLAG))==0))
    		{
    			r = game_palette[3*i + 0].read() >> 2;
    			g = game_palette[3*i + 1].read() >> 2;
    			b = game_palette[3*i + 2].read() >> 2;
    
    			j = rgb6_to_pen[r][g][b];
    
    			if (palette_map[i] != j)
    			{
    				just_remapped.write(i,1);
    
    				pen_usage_count[palette_map[i]]--;
    				if (pen_usage_count[palette_map[i]] == 0)
    					saved++;
    				palette_map[i] = (char)j;
    				pen_usage_count[palette_map[i]]++;
    				Machine.pens[i] = shrinked_pens[palette_map[i]];
    			}
    		}
    	}
    
    	return saved;
    }
    
    /*TODO*///
    /*TODO*///static const unsigned char *palette_recalc_16_static(void)
    /*TODO*///{
    /*TODO*///	int i,color;
    /*TODO*///	int did_remap = 0;
    /*TODO*///	int need_refresh = 0;
    /*TODO*///
    /*TODO*///
    /*TODO*///	memset(just_remapped,0,Machine->drv->total_colors * sizeof(unsigned char));
    /*TODO*///
    /*TODO*///	for (color = 0;color < Machine->drv->total_colors;color++)
    /*TODO*///	{
    /*TODO*///		/* the comparison between palette_used_colors and old_used_colors also includes */
    /*TODO*///		/* PALETTE_COLOR_NEEDS_REMAP which might have been set by palette_change_color() */
    /*TODO*///		if ((palette_used_colors[color] & PALETTE_COLOR_VISIBLE) &&
    /*TODO*///				palette_used_colors[color] != old_used_colors[color])
    /*TODO*///		{
    /*TODO*///			int r,g,b;
    /*TODO*///
    /*TODO*///
    /*TODO*///			did_remap = 1;
    /*TODO*///			if (old_used_colors[color] & palette_used_colors[color] & PALETTE_COLOR_CACHED)
    /*TODO*///			{
    /*TODO*///				/* the color was and still is cached, we'll have to redraw everything */
    /*TODO*///				need_refresh = 1;
    /*TODO*///				just_remapped[color] = 1;
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			if (palette_used_colors[color] & PALETTE_COLOR_TRANSPARENT_FLAG)
    /*TODO*///				Machine->pens[color] = palette_transparent_pen;
    /*TODO*///			else
    /*TODO*///			{
    /*TODO*///				r = game_palette[3*color + 0];
    /*TODO*///				g = game_palette[3*color + 1];
    /*TODO*///				b = game_palette[3*color + 2];
    /*TODO*///
    /*TODO*///				Machine->pens[color] = shrinked_pens[rgbpenindex(r,g,b)];
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		old_used_colors[color] = palette_used_colors[color];
    /*TODO*///	}
    /*TODO*///
    /*TODO*///
    /*TODO*///	if (did_remap)
    /*TODO*///	{
    /*TODO*///		/* rebuild the color lookup table */
    /*TODO*///		for (i = 0;i < Machine->drv->color_table_len;i++)
    /*TODO*///			Machine->remapped_colortable[i] = Machine->pens[Machine->game_colortable[i]];
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	if (need_refresh) return just_remapped;
    /*TODO*///	else return 0;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static const unsigned char *palette_recalc_16_palettized(void)
    /*TODO*///{
    /*TODO*///	int i,color;
    /*TODO*///	int did_remap = 0;
    /*TODO*///	int need_refresh = 0;
    /*TODO*///
    /*TODO*///
    /*TODO*///	memset(just_remapped,0,Machine->drv->total_colors * sizeof(unsigned char));
    /*TODO*///
    /*TODO*///	for (color = 0;color < Machine->drv->total_colors;color++)
    /*TODO*///	{
    /*TODO*///		if ((palette_used_colors[color] & PALETTE_COLOR_TRANSPARENT_FLAG) !=
    /*TODO*///				(old_used_colors[color] & PALETTE_COLOR_TRANSPARENT_FLAG))
    /*TODO*///		{
    /*TODO*///			did_remap = 1;
    /*TODO*///			if (old_used_colors[color] & palette_used_colors[color] & PALETTE_COLOR_CACHED)
    /*TODO*///			{
    /*TODO*///				/* the color was and still is cached, we'll have to redraw everything */
    /*TODO*///				need_refresh = 1;
    /*TODO*///				just_remapped[color] = 1;
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			if (palette_used_colors[color] & PALETTE_COLOR_TRANSPARENT_FLAG)
    /*TODO*///				Machine->pens[color] = palette_transparent_pen;
    /*TODO*///			else
    /*TODO*///				Machine->pens[color] = shrinked_pens[color + RESERVED_PENS];
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		old_used_colors[color] = palette_used_colors[color];
    /*TODO*///	}
    /*TODO*///
    /*TODO*///
    /*TODO*///	if (did_remap)
    /*TODO*///	{
    /*TODO*///		/* rebuild the color lookup table */
    /*TODO*///		for (i = 0;i < Machine->drv->color_table_len;i++)
    /*TODO*///			Machine->remapped_colortable[i] = Machine->pens[Machine->game_colortable[i]];
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	if (need_refresh) return just_remapped;
    /*TODO*///	else return 0;
    /*TODO*///}
    /*TODO*///
    public static UBytePtr palette_recalc_8()
    {
    	int i,color;
    	int did_remap = 0;
    	int need_refresh = 0;
    	int first_free_pen;
    	int ran_out = 0;
    	int reuse_pens = 0;
    	int need,avail;
    
    
    	//memset(just_remapped,0,Machine.drv.total_colors /* * sizeof(unsigned char)*/);
        for (int mem = 0; mem < Machine.drv.total_colors; mem++) 
                    just_remapped.write(mem,0);

    	/* first of all, apply the changes to the palette which were */
    	/* requested since last update */
    	for (color = 0;color < Machine.drv.total_colors;color++)
    	{
    		if ((palette_dirty.read(color))!=0)
    		{
    			int r,g,b,pen;
    
    
    			pen = palette_map[color];
    			r = new_palette.read(3*color + 0);
    			g = new_palette.read(3*color + 1);
    			b = new_palette.read(3*color + 2);
    
    			/* if the color maps to an exclusive pen, just change it */
    			if (pen_usage_count[pen] == 1)
    			{
    				palette_dirty.write(color,0);
    				game_palette[3*color + 0].set((char)r);
    				game_palette[3*color + 1].set((char)g);
    				game_palette[3*color + 2].set((char)b);
    
    				shrinked_palette[3*pen + 0].set((char)r);
    				shrinked_palette[3*pen + 1].set((char)g); 
    				shrinked_palette[3*pen + 2].set((char)b);
    				osd_modify_pen(Machine.pens[color],r,g,b);
                                    
    			}
    			else
    			{
                                
    				if (pen < RESERVED_PENS)
    				{
    					/* the color uses a reserved pen, the only thing we can do is remap it */
    					for (i = color;i < Machine.drv.total_colors;i++)
    					{
    						if (palette_dirty.read(i) != 0 && palette_map[i] == pen)
    						{
    							palette_dirty.write(i, 0);
    							game_palette[3*i + 0].set(new_palette.read(3*i + 0));
    							game_palette[3*i + 1].set(new_palette.read(3*i + 1));
    							game_palette[3*i + 2].set(new_palette.read(3*i + 2));
    							old_used_colors.write(i,old_used_colors.read(i) | PALETTE_COLOR_NEEDS_REMAP);
    						}
    					}
    				}
    				else
    				{
//                                    throw new UnsupportedOperationException("palette_recalc unimplemented");
                                
    					/* the pen is shared with other colors, let's see if all of them */
    					/* have been changed to the same value */
    					for (i = 0;i < Machine.drv.total_colors;i++)
    					{
    						if (((old_used_colors.read(i) & PALETTE_COLOR_VISIBLE)!=0) &&
    								palette_map[i] == pen)
    						{
    							if (palette_dirty.read(i) == 0 ||
    									new_palette.read(3*i + 0) != r ||
    									new_palette.read(3*i + 1) != g ||
    									new_palette.read(3*i + 2) != b)
    								break;
    						}
    					}
    
    					if (i == Machine.drv.total_colors)
    					{
    						/* all colors sharing this pen still are the same, so we */
    						/* just change the palette. */
    						shrinked_palette[3*pen + 0].set((char)r);
    						shrinked_palette[3*pen + 1].set((char)g);
    						shrinked_palette[3*pen + 2].set((char)b);
    						osd_modify_pen(Machine.pens[color],r,g,b);
    
    						for (i = color;i < Machine.drv.total_colors;i++)
    						{
    							if (palette_dirty.read(i) != 0 && palette_map[i] == pen)
    							{
    								palette_dirty.write(i,0);
    								game_palette[3*i + 0].set((char)r);
    								game_palette[3*i + 1].set((char)g);
    								game_palette[3*i + 2].set((char)b);
    							}
    						}
    					}
    					else
    					{
    						/* the colors sharing this pen now are different, we'll */
    						/* have to remap them. */
    						for (i = color;i < Machine.drv.total_colors;i++)
    						{
    							if (palette_dirty.read(i) != 0 && palette_map[i] == pen)
    							{
    								palette_dirty.write(i,0);
    								game_palette[3*i + 0].set(new_palette.read(3*i + 0));
    								game_palette[3*i + 1].set(new_palette.read(3*i + 1));
    								game_palette[3*i + 2].set(new_palette.read(3*i + 2));
    								old_used_colors.write(i,old_used_colors.read(i) |PALETTE_COLOR_NEEDS_REMAP);
    							}
    						}
    					}
    				}
        		}
    		}
    	}
    
    
    	need = 0;
    	for (i = 0;i < Machine.drv.total_colors;i++)
    	{
    		if (((palette_used_colors.read(i) & PALETTE_COLOR_VISIBLE)!=0) && palette_used_colors.read(i) != old_used_colors.read(i))
    			need++;
    	}
    	if (need > 0)
    	{
    		avail = 0;
    		for (i = 0;i < DYNAMIC_MAX_PENS;i++)
    		{
    			if (pen_usage_count[i] == 0)
    				avail++;
    		}
    
    		if (need > avail)
    		{
    
                    if (errorlog!=null) fprintf(errorlog,"Need %d new pens; %d available. I'll reuse some pens.\n",need,avail);

    			reuse_pens = 1;
    			build_rgb_to_pen();
    		}
    	}
    
    	first_free_pen = RESERVED_PENS;
    	for (color = 0;color < Machine.drv.total_colors;color++)
    	{
    		/* the comparison between palette_used_colors and old_used_colors also includes */
    		/* PALETTE_COLOR_NEEDS_REMAP which might have been set previously */
    		if (((palette_used_colors.read(color) & PALETTE_COLOR_VISIBLE)!=0) &&
    				palette_used_colors.read(color) != old_used_colors.read(color))
    		{             
    			int r,g,b;
    
    
    			if ((old_used_colors.read(color) & PALETTE_COLOR_VISIBLE)!=0)
    			{
    				pen_usage_count[palette_map[color]]--;
    				//old_used_colors[color] &= ~PALETTE_COLOR_VISIBLE;
                                old_used_colors.write(color,old_used_colors.read(color) & ~PALETTE_COLOR_VISIBLE);
    			}
    
    			r = game_palette[3*color + 0].read();
    			g = game_palette[3*color + 1].read();
    			b = game_palette[3*color + 2].read();
    
    			if ((palette_used_colors.read(color) & PALETTE_COLOR_TRANSPARENT_FLAG)!=0)
    			{
    				if (palette_map[color] != TRANSPARENT_PEN)
    				{
    					/* use the fixed transparent black for this */
    					did_remap = 1;
    					if ((old_used_colors.read(color) & palette_used_colors.read(color) & PALETTE_COLOR_CACHED)!=0)
    					{
    						/* the color was and still is cached, we'll have to redraw everything */
    						need_refresh = 1;
    						just_remapped.write(color,1);
    					}
    
    					palette_map[color] = TRANSPARENT_PEN;
    				}
    				pen_usage_count[palette_map[color]]++;
    				Machine.pens[color] = shrinked_pens[palette_map[color]];
    				old_used_colors.write(color,palette_used_colors.read(color));
    			}
    			else
    			{                           
    				if (reuse_pens!=0)
    				{
    					i = rgb6_to_pen[r >> 2][g >> 2][b >> 2];
    					if (i != DYNAMIC_MAX_PENS)
    					{
    						if (palette_map[color] != i)
    						{
    							did_remap = 1;
    							if ((old_used_colors.read(color) & palette_used_colors.read(color) & PALETTE_COLOR_CACHED)!=0)
    							{
    								/* the color was and still is cached, we'll have to redraw everything */
    								need_refresh = 1;
    								just_remapped.write(color,1);
    							}
    
    							palette_map[color] = (char)i;
    						}
    						pen_usage_count[palette_map[color]]++;
    						Machine.pens[color] = shrinked_pens[palette_map[color]];
    						old_used_colors.write(color,palette_used_colors.read(color));
    					}
                                    
    				}
    
    				/* if we still haven't found a pen, choose a new one */
    				if (old_used_colors.read(color) != palette_used_colors.read(color))
    				{
                                    //throw new UnsupportedOperationException("palette_recalc unimplemented");
    					/* if possible, reuse the last associated pen */
    					if (pen_usage_count[palette_map[color]] == 0)
    					{
    						pen_usage_count[palette_map[color]]++;
    					}
    					else	/* allocate a new pen */
    					{
    retry:
        for(;;){
    						while (first_free_pen < DYNAMIC_MAX_PENS && pen_usage_count[first_free_pen] > 0)
    							first_free_pen++;
    
    						if (first_free_pen < DYNAMIC_MAX_PENS)
    						{
    							did_remap = 1;
    							if ((old_used_colors.read(color) & palette_used_colors.read(color) & PALETTE_COLOR_CACHED)!=0)
    							{
    								/* the color was and still is cached, we'll have to redraw everything */
    								need_refresh = 1;
    								just_remapped.write(color,1);
    							}
    
    							palette_map[color] = (char)first_free_pen;
    							pen_usage_count[palette_map[color]]++;
    							Machine.pens[color] = shrinked_pens[palette_map[color]];
    						}
    						else
    						{
                                                    
                                                
    							/* Ran out of pens! Let's see what we can do. */
    
    							if (ran_out == 0)
    							{
    								ran_out++;
    
    								/* from now on, try to reuse already allocated pens */
    								reuse_pens = 1;
    								if (compress_palette() > 0)
    								{ 
    									did_remap = 1;
    									need_refresh = 1;	/* we'll have to redraw everything */
    
    									first_free_pen = RESERVED_PENS;
    									continue retry;
    								}
    							}
    
    							ran_out++;
    
    							/* we failed, but go on with the loop, there might */
    							/* be some transparent pens to remap */
    
    							continue;
    						}
                                break;//for goto        
                                }//for goto
    					}
    
    					{
    						int rr,gg,bb;
    
    						i = palette_map[color];
    						rr = shrinked_palette[3*i + 0].read() >> 2;
    						gg = shrinked_palette[3*i + 1].read() >> 2;
    						bb = shrinked_palette[3*i + 2].read() >> 2;
    						if (rgb6_to_pen[rr][gg][bb] == i)
    							rgb6_to_pen[rr][gg][bb] = DYNAMIC_MAX_PENS;
    
    						shrinked_palette[3*i + 0].set((char)r);
    						shrinked_palette[3*i + 1].set((char)g);
    						shrinked_palette[3*i + 2].set((char)b);
    						osd_modify_pen(Machine.pens[color],r,g,b);
    
    						r >>= 2;
    						g >>= 2;
    						b >>= 2;
    						if (rgb6_to_pen[r][g][b] == DYNAMIC_MAX_PENS)
    							rgb6_to_pen[r][g][b] = (char)i;
    					}
    
    					old_used_colors.write(color,palette_used_colors.read(color));
    				}
                                
    			}
    		}
    	}
    
    	if (ran_out > 1)
    	{
            if (errorlog!=null) fprintf(errorlog,"Error: no way to shrink the palette to 256 colors, left out %d colors.\n",ran_out-1);
    	}
    
    	/* Reclaim unused pens; we do this AFTER allocating the new ones, to avoid */
    	/* using the same pen for two different colors in two consecutive frames, */
    	/* which might cause flicker. */
    	for (color = 0;color < Machine.drv.total_colors;color++)
    	{
    		if ((palette_used_colors.read(color) & PALETTE_COLOR_VISIBLE)==0)
    		{
    			if ((old_used_colors.read(color) & PALETTE_COLOR_VISIBLE)!=0)
    				pen_usage_count[palette_map[color]]--;
    			old_used_colors.write(color,palette_used_colors.read(color));
                    
    		}
    	}   
    	if (did_remap!=0)
    	{
    		/* rebuild the color lookup table */
    		for (i = 0;i < Machine.drv.color_table_len;i++)
    			Machine.remapped_colortable[i] = Machine.pens[Machine.game_colortable[i]];
    	}
    
    	if (need_refresh!=0)
    	{
    		if (errorlog!=null)
    		{
    			int used;
    
    			used = 0;
    			for (i = 0;i < DYNAMIC_MAX_PENS;i++)
    			{
    				if (pen_usage_count[i] > 0)
    					used++;
    			} 
    			fprintf(errorlog,"Did a palette remap, need a full screen redraw (%d pens used).\n",used); 
    		}
    		return just_remapped;
    	}
    	else return null;
            
    }
    
    
    public static UBytePtr palette_recalc()
    {
    	/* if we are not dynamically reducing the palette, return immediately. */
    	if (palette_used_colors == null)
    		return null;
    	switch (use_16bit)
    	{
    		case NO_16BIT:
    		default:
    			return palette_recalc_8();
    		case STATIC_16BIT:
                    throw new UnsupportedOperationException("palette_recalc unimplemented");
    /*TODO*///			return palette_recalc_16_static();
    		case PALETTIZED_16BIT:
                    throw new UnsupportedOperationException("palette_recalc unimplemented");
    /*TODO*///			return palette_recalc_16_palettized();
    	}
    }
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*////******************************************************************************
    /*TODO*///
    /*TODO*/// Commonly used palette RAM handling functions
    /*TODO*///
    /*TODO*///******************************************************************************/
    /*TODO*///
    public static CharPtr paletteram = new CharPtr();//unsigned char *paletteram,*paletteram_2;
    public static CharPtr paletteram_2 = new CharPtr();
    
    /*TODO*///int paletteram_r(int offset)
    /*TODO*///{
    /*TODO*///	return paletteram[offset];
    /*TODO*///}
    /*TODO*///
    /*TODO*///int paletteram_2_r(int offset)
    /*TODO*///{
    /*TODO*///	return paletteram_2[offset];
    /*TODO*///}
    /*TODO*///
    /*TODO*///int paletteram_word_r(int offset)
    /*TODO*///{
    /*TODO*///	return READ_WORD(&paletteram[offset]);
    /*TODO*///}
    /*TODO*///
    /*TODO*///int paletteram_2_word_r(int offset)
    /*TODO*///{
    /*TODO*///	return READ_WORD(&paletteram_2[offset]);
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_RRRGGGBB_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	int r,g,b;
    /*TODO*///	int bit0,bit1,bit2;
    /*TODO*///
    /*TODO*///
    /*TODO*///	paletteram[offset] = data;
    /*TODO*///
    /*TODO*///	/* red component */
    /*TODO*///	bit0 = (data >> 5) & 0x01;
    /*TODO*///	bit1 = (data >> 6) & 0x01;
    /*TODO*///	bit2 = (data >> 7) & 0x01;
    /*TODO*///	r = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
    /*TODO*///	/* green component */
    /*TODO*///	bit0 = (data >> 2) & 0x01;
    /*TODO*///	bit1 = (data >> 3) & 0x01;
    /*TODO*///	bit2 = (data >> 4) & 0x01;
    /*TODO*///	g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
    /*TODO*///	/* blue component */
    /*TODO*///	bit0 = 0;
    /*TODO*///	bit1 = (data >> 0) & 0x01;
    /*TODO*///	bit2 = (data >> 1) & 0x01;
    /*TODO*///	b = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
    /*TODO*///
    /*TODO*///	palette_change_color(offset,r,g,b);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///void paletteram_BBGGGRRR_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	int r,g,b;
    /*TODO*///	int bit0,bit1,bit2;
    /*TODO*///
    /*TODO*///
    /*TODO*///	paletteram[offset] = data;
    /*TODO*///
    /*TODO*///	/* red component */
    /*TODO*///	bit0 = (data >> 0) & 0x01;
    /*TODO*///	bit1 = (data >> 1) & 0x01;
    /*TODO*///	bit2 = (data >> 2) & 0x01;
    /*TODO*///	r = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
    /*TODO*///	/* green component */
    /*TODO*///	bit0 = (data >> 3) & 0x01;
    /*TODO*///	bit1 = (data >> 4) & 0x01;
    /*TODO*///	bit2 = (data >> 5) & 0x01;
    /*TODO*///	g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
    /*TODO*///	/* blue component */
    /*TODO*///	bit0 = 0;
    /*TODO*///	bit1 = (data >> 6) & 0x01;
    /*TODO*///	bit2 = (data >> 7) & 0x01;
    /*TODO*///	b = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
    /*TODO*///
    /*TODO*///	palette_change_color(offset,r,g,b);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///void paletteram_IIBBGGRR_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	int r,g,b,i;
    /*TODO*///
    /*TODO*///
    /*TODO*///	paletteram[offset] = data;
    /*TODO*///
    /*TODO*///	i = (data >> 6) & 0x03;
    /*TODO*///	/* red component */
    /*TODO*///	r = (data << 2) & 0x0c;
    /*TODO*///	if (r) r |= i;
    /*TODO*///	r *= 0x11;
    /*TODO*///	/* green component */
    /*TODO*///	g = (data >> 0) & 0x0c;
    /*TODO*///	if (g) g |= i;
    /*TODO*///	g *= 0x11;
    /*TODO*///	/* blue component */
    /*TODO*///	b = (data >> 2) & 0x0c;
    /*TODO*///	if (b) b |= i;
    /*TODO*///	b *= 0x11;
    /*TODO*///
    /*TODO*///	palette_change_color(offset,r,g,b);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///void paletteram_BBGGRRII_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	int r,g,b,i;
    /*TODO*///
    /*TODO*///
    /*TODO*///	paletteram[offset] = data;
    /*TODO*///
    /*TODO*///	i = (data >> 0) & 0x03;
    /*TODO*///	/* red component */
    /*TODO*///	r = (((data >> 0) & 0x0c) | i) * 0x11;
    /*TODO*///	/* green component */
    /*TODO*///	g = (((data >> 2) & 0x0c) | i) * 0x11;
    /*TODO*///	/* blue component */
    /*TODO*///	b = (((data >> 4) & 0x0c) | i) * 0x11;
    /*TODO*///
    /*TODO*///	palette_change_color(offset,r,g,b);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    public static void changecolor_xxxxBBBBGGGGRRRR(int color,int data)
    {
    	int r,g,b;

    	r = (data >> 0) & 0x0f;
    	g = (data >> 4) & 0x0f;
    	b = (data >> 8) & 0x0f;
    
    	r = (r << 4) | r;
    	g = (g << 4) | g;
    	b = (b << 4) | b;
    
    	palette_change_color(color,r,g,b);
    }
    
    public static WriteHandlerPtr paletteram_xxxxBBBBGGGGRRRR_w = new WriteHandlerPtr() { public void handler(int offset, int data)
    {
        paletteram.write(offset,data);
        changecolor_xxxxBBBBGGGGRRRR(offset / 2,paletteram.read(offset & ~1) | (paletteram.read(offset | 1) << 8));
    }};
    /*TODO*///void paletteram_xxxxBBBBGGGGRRRR_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	paletteram[offset] = data;
    /*TODO*///	changecolor_xxxxBBBBGGGGRRRR(offset / 2,paletteram[offset & ~1] | (paletteram[offset | 1] << 8));
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_xxxxBBBBGGGGRRRR_swap_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	paletteram[offset] = data;
    /*TODO*///	changecolor_xxxxBBBBGGGGRRRR(offset / 2,paletteram[offset | 1] | (paletteram[offset & ~1] << 8));
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_xxxxBBBBGGGGRRRR_split1_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	paletteram[offset] = data;
    /*TODO*///	changecolor_xxxxBBBBGGGGRRRR(offset,paletteram[offset] | (paletteram_2[offset] << 8));
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_xxxxBBBBGGGGRRRR_split2_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	paletteram_2[offset] = data;
    /*TODO*///	changecolor_xxxxBBBBGGGGRRRR(offset,paletteram[offset] | (paletteram_2[offset] << 8));
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_xxxxBBBBGGGGRRRR_word_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	int oldword = READ_WORD(&paletteram[offset]);
    /*TODO*///	int newword = COMBINE_WORD(oldword,data);
    /*TODO*///
    /*TODO*///
    /*TODO*///	WRITE_WORD(&paletteram[offset],newword);
    /*TODO*///	changecolor_xxxxBBBBGGGGRRRR(offset / 2,newword);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///INLINE void changecolor_xxxxBBBBRRRRGGGG(int color,int data)
    /*TODO*///{
    /*TODO*///	int r,g,b;
    /*TODO*///
    /*TODO*///
    /*TODO*///	r = (data >> 4) & 0x0f;
    /*TODO*///	g = (data >> 0) & 0x0f;
    /*TODO*///	b = (data >> 8) & 0x0f;
    /*TODO*///
    /*TODO*///	r = (r << 4) | r;
    /*TODO*///	g = (g << 4) | g;
    /*TODO*///	b = (b << 4) | b;
    /*TODO*///
    /*TODO*///	palette_change_color(color,r,g,b);
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_xxxxBBBBRRRRGGGG_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	paletteram[offset] = data;
    /*TODO*///	changecolor_xxxxBBBBRRRRGGGG(offset / 2,paletteram[offset & ~1] | (paletteram[offset | 1] << 8));
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_xxxxBBBBRRRRGGGG_swap_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	paletteram[offset] = data;
    /*TODO*///	changecolor_xxxxBBBBRRRRGGGG(offset / 2,paletteram[offset | 1] | (paletteram[offset & ~1] << 8));
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_xxxxBBBBRRRRGGGG_split1_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	paletteram[offset] = data;
    /*TODO*///	changecolor_xxxxBBBBRRRRGGGG(offset,paletteram[offset] | (paletteram_2[offset] << 8));
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_xxxxBBBBRRRRGGGG_split2_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	paletteram_2[offset] = data;
    /*TODO*///	changecolor_xxxxBBBBRRRRGGGG(offset,paletteram[offset] | (paletteram_2[offset] << 8));
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///INLINE void changecolor_xxxxRRRRBBBBGGGG(int color,int data)
    /*TODO*///{
    /*TODO*///	int r,g,b;
    /*TODO*///
    /*TODO*///
    /*TODO*///	r = (data >> 8) & 0x0f;
    /*TODO*///	g = (data >> 0) & 0x0f;
    /*TODO*///	b = (data >> 4) & 0x0f;
    /*TODO*///
    /*TODO*///	r = (r << 4) | r;
    /*TODO*///	g = (g << 4) | g;
    /*TODO*///	b = (b << 4) | b;
    /*TODO*///
    /*TODO*///	palette_change_color(color,r,g,b);
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_xxxxRRRRBBBBGGGG_split1_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	paletteram[offset] = data;
    /*TODO*///	changecolor_xxxxRRRRBBBBGGGG(offset,paletteram[offset] | (paletteram_2[offset] << 8));
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_xxxxRRRRBBBBGGGG_split2_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	paletteram_2[offset] = data;
    /*TODO*///	changecolor_xxxxRRRRBBBBGGGG(offset,paletteram[offset] | (paletteram_2[offset] << 8));
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///INLINE void changecolor_xxxxRRRRGGGGBBBB(int color,int data)
    /*TODO*///{
    /*TODO*///	int r,g,b;
    /*TODO*///
    /*TODO*///
    /*TODO*///	r = (data >> 8) & 0x0f;
    /*TODO*///	g = (data >> 4) & 0x0f;
    /*TODO*///	b = (data >> 0) & 0x0f;
    /*TODO*///
    /*TODO*///	r = (r << 4) | r;
    /*TODO*///	g = (g << 4) | g;
    /*TODO*///	b = (b << 4) | b;
    /*TODO*///
    /*TODO*///	palette_change_color(color,r,g,b);
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_xxxxRRRRGGGGBBBB_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	paletteram[offset] = data;
    /*TODO*///	changecolor_xxxxRRRRGGGGBBBB(offset / 2,paletteram[offset & ~1] | (paletteram[offset | 1] << 8));
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_xxxxRRRRGGGGBBBB_word_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	int oldword = READ_WORD(&paletteram[offset]);
    /*TODO*///	int newword = COMBINE_WORD(oldword,data);
    /*TODO*///
    /*TODO*///
    /*TODO*///	WRITE_WORD(&paletteram[offset],newword);
    /*TODO*///	changecolor_xxxxRRRRGGGGBBBB(offset / 2,newword);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///INLINE void changecolor_RRRRGGGGBBBBxxxx(int color,int data)
    /*TODO*///{
    /*TODO*///	int r,g,b;
    /*TODO*///
    /*TODO*///
    /*TODO*///	r = (data >> 12) & 0x0f;
    /*TODO*///	g = (data >>  8) & 0x0f;
    /*TODO*///	b = (data >>  4) & 0x0f;
    /*TODO*///
    /*TODO*///	r = (r << 4) | r;
    /*TODO*///	g = (g << 4) | g;
    /*TODO*///	b = (b << 4) | b;
    /*TODO*///
    /*TODO*///	palette_change_color(color,r,g,b);
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_RRRRGGGGBBBBxxxx_swap_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	paletteram[offset] = data;
    /*TODO*///	changecolor_RRRRGGGGBBBBxxxx(offset / 2,paletteram[offset | 1] | (paletteram[offset & ~1] << 8));
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_RRRRGGGGBBBBxxxx_split1_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	paletteram[offset] = data;
    /*TODO*///	changecolor_RRRRGGGGBBBBxxxx(offset,paletteram[offset] | (paletteram_2[offset] << 8));
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_RRRRGGGGBBBBxxxx_split2_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	paletteram_2[offset] = data;
    /*TODO*///	changecolor_RRRRGGGGBBBBxxxx(offset,paletteram[offset] | (paletteram_2[offset] << 8));
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_RRRRGGGGBBBBxxxx_word_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	int oldword = READ_WORD(&paletteram[offset]);
    /*TODO*///	int newword = COMBINE_WORD(oldword,data);
    /*TODO*///
    /*TODO*///
    /*TODO*///	WRITE_WORD(&paletteram[offset],newword);
    /*TODO*///	changecolor_RRRRGGGGBBBBxxxx(offset / 2,newword);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///INLINE void changecolor_BBBBGGGGRRRRxxxx(int color,int data)
    /*TODO*///{
    /*TODO*///	int r,g,b;
    /*TODO*///
    /*TODO*///
    /*TODO*///	r = (data >>  4) & 0x0f;
    /*TODO*///	g = (data >>  8) & 0x0f;
    /*TODO*///	b = (data >> 12) & 0x0f;
    /*TODO*///
    /*TODO*///	r = (r << 4) | r;
    /*TODO*///	g = (g << 4) | g;
    /*TODO*///	b = (b << 4) | b;
    /*TODO*///
    /*TODO*///	palette_change_color(color,r,g,b);
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_BBBBGGGGRRRRxxxx_swap_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	paletteram[offset] = data;
    /*TODO*///	changecolor_BBBBGGGGRRRRxxxx(offset / 2,paletteram[offset | 1] | (paletteram[offset & ~1] << 8));
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_BBBBGGGGRRRRxxxx_split1_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	paletteram[offset] = data;
    /*TODO*///	changecolor_BBBBGGGGRRRRxxxx(offset,paletteram[offset] | (paletteram_2[offset] << 8));
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_BBBBGGGGRRRRxxxx_split2_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	paletteram_2[offset] = data;
    /*TODO*///	changecolor_BBBBGGGGRRRRxxxx(offset,paletteram[offset] | (paletteram_2[offset] << 8));
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_BBBBGGGGRRRRxxxx_word_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	int oldword = READ_WORD(&paletteram[offset]);
    /*TODO*///	int newword = COMBINE_WORD(oldword,data);
    /*TODO*///
    /*TODO*///
    /*TODO*///	WRITE_WORD(&paletteram[offset],newword);
    /*TODO*///	changecolor_BBBBGGGGRRRRxxxx(offset / 2,newword);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///INLINE void changecolor_xBBBBBGGGGGRRRRR(int color,int data)
    /*TODO*///{
    /*TODO*///	int r,g,b;
    /*TODO*///
    /*TODO*///
    /*TODO*///	r = (data >>  0) & 0x1f;
    /*TODO*///	g = (data >>  5) & 0x1f;
    /*TODO*///	b = (data >> 10) & 0x1f;
    /*TODO*///
    /*TODO*///	r = (r << 3) | (r >> 2);
    /*TODO*///	g = (g << 3) | (g >> 2);
    /*TODO*///	b = (b << 3) | (b >> 2);
    /*TODO*///
    /*TODO*///	palette_change_color(color,r,g,b);
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_xBBBBBGGGGGRRRRR_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	paletteram[offset] = data;
    /*TODO*///	changecolor_xBBBBBGGGGGRRRRR(offset / 2,paletteram[offset & ~1] | (paletteram[offset | 1] << 8));
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_xBBBBBGGGGGRRRRR_swap_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	paletteram[offset] = data;
    /*TODO*///	changecolor_xBBBBBGGGGGRRRRR(offset / 2,paletteram[offset | 1] | (paletteram[offset & ~1] << 8));
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_xBBBBBGGGGGRRRRR_word_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	int oldword = READ_WORD(&paletteram[offset]);
    /*TODO*///	int newword = COMBINE_WORD(oldword,data);
    /*TODO*///
    /*TODO*///
    /*TODO*///	WRITE_WORD(&paletteram[offset],newword);
    /*TODO*///	changecolor_xBBBBBGGGGGRRRRR(offset / 2,newword);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///INLINE void changecolor_xRRRRRGGGGGBBBBB(int color,int data)
    /*TODO*///{
    /*TODO*///	int r,g,b;
    /*TODO*///
    /*TODO*///
    /*TODO*///	r = (data >> 10) & 0x1f;
    /*TODO*///	g = (data >>  5) & 0x1f;
    /*TODO*///	b = (data >>  0) & 0x1f;
    /*TODO*///
    /*TODO*///	r = (r << 3) | (r >> 2);
    /*TODO*///	g = (g << 3) | (g >> 2);
    /*TODO*///	b = (b << 3) | (b >> 2);
    /*TODO*///
    /*TODO*///	palette_change_color(color,r,g,b);
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_xRRRRRGGGGGBBBBB_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	paletteram[offset] = data;
    /*TODO*///	changecolor_xRRRRRGGGGGBBBBB(offset / 2,paletteram[offset & ~1] | (paletteram[offset | 1] << 8));
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_xRRRRRGGGGGBBBBB_word_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	int oldword = READ_WORD(&paletteram[offset]);
    /*TODO*///	int newword = COMBINE_WORD(oldword,data);
    /*TODO*///
    /*TODO*///
    /*TODO*///	WRITE_WORD(&paletteram[offset],newword);
    /*TODO*///	changecolor_xRRRRRGGGGGBBBBB(offset / 2,newword);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///INLINE void changecolor_IIIIRRRRGGGGBBBB(int color,int data)
    /*TODO*///{
    /*TODO*///	int i,r,g,b;
    /*TODO*///
    /*TODO*///
    /*TODO*///	static const int ztable[16] =
    /*TODO*///		{ 0x0, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf, 0x10, 0x11 };
    /*TODO*///
    /*TODO*///	i = ztable[(data >> 12) & 15];
    /*TODO*///	r = ((data >> 8) & 15) * i;
    /*TODO*///	g = ((data >> 4) & 15) * i;
    /*TODO*///	b = ((data >> 0) & 15) * i;
    /*TODO*///
    /*TODO*///	palette_change_color(color,r,g,b);
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_IIIIRRRRGGGGBBBB_word_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	int oldword = READ_WORD(&paletteram[offset]);
    /*TODO*///	int newword = COMBINE_WORD(oldword,data);
    /*TODO*///
    /*TODO*///
    /*TODO*///	WRITE_WORD(&paletteram[offset],newword);
    /*TODO*///	changecolor_IIIIRRRRGGGGBBBB(offset / 2,newword);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///INLINE void changecolor_RRRRGGGGBBBBIIII(int color,int data)
    /*TODO*///{
    /*TODO*///	int i,r,g,b;
    /*TODO*///
    /*TODO*///
    /*TODO*///	static const int ztable[16] =
    /*TODO*///		{ 0x0, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf, 0x10, 0x11 };
    /*TODO*///
    /*TODO*///	i = ztable[(data >> 0) & 15];
    /*TODO*///	r = ((data >> 12) & 15) * i;
    /*TODO*///	g = ((data >>  8) & 15) * i;
    /*TODO*///	b = ((data >>  4) & 15) * i;
    /*TODO*///
    /*TODO*///	palette_change_color(color,r,g,b);
    /*TODO*///}
    /*TODO*///
    /*TODO*///void paletteram_RRRRGGGGBBBBIIII_word_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	int oldword = READ_WORD(&paletteram[offset]);
    /*TODO*///	int newword = COMBINE_WORD(oldword,data);
    /*TODO*///
    /*TODO*///
    /*TODO*///	WRITE_WORD(&paletteram[offset],newword);
    /*TODO*///	changecolor_RRRRGGGGBBBBIIII(offset / 2,newword);
    /*TODO*///}
    /*TODO*///    
}
