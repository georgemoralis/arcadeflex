/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.v036.cpu.m68000.m68000H.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;

public class toaplan1 {

    public static final int VIDEORAM1_SIZE = 0x1000;
    /* size in bytes - sprite ram */
    public static final int VIDEORAM2_SIZE = 0x100;
    /* size in bytes - sprite size ram */
    public static final int VIDEORAM3_SIZE = 0x4000;
    /* size in bytes - tile ram */

    public static UBytePtr toaplan1_videoram1 = new UBytePtr();
    public static UBytePtr toaplan1_videoram2 = new UBytePtr();
    public static UBytePtr toaplan1_videoram3 = new UBytePtr();
    public static UBytePtr toaplan1_buffered_videoram1 = new UBytePtr();
    public static UBytePtr toaplan1_buffered_videoram2 = new UBytePtr();

    public static UBytePtr toaplan1_colorram1 = new UBytePtr();
    public static UBytePtr toaplan1_colorram2 = new UBytePtr();

    public static int[] colorram1_size = new int[1];
    public static int[] colorram2_size = new int[1];

    /*unsigned*/
    static int[] scrollregs = new int[8];
    /*unsigned*/
    static int vblank;
    /*unsigned*/
    static int num_tiles;

    /*unsigned*/
    static int video_ofs;
    /*unsigned*/
    static int video_ofs3;

    static int toaplan1_flipscreen;
    static int tiles_offsetx;
    static int tiles_offsety;
    static int[] layers_offset = new int[4];

    /*TODO*///	
/*TODO*///	typedef struct
/*TODO*///		{
/*TODO*///		UINT16 tile_num;
/*TODO*///		UINT16 color;
/*TODO*///		char priority;
/*TODO*///		int xpos;
/*TODO*///		int ypos;
/*TODO*///		} tile_struct;
/*TODO*///	
/*TODO*///	tile_struct *bg_list[4];
/*TODO*///	
/*TODO*///	tile_struct *tile_list[32];
/*TODO*///	tile_struct *temp_list;
/*TODO*///	static int max_list_size[32];
/*TODO*///	static int tile_count[32];
/*TODO*///	
    static osd_bitmap tmpbitmap1;
    static osd_bitmap tmpbitmap2;
    static osd_bitmap tmpbitmap3;

    public static VhStartPtr rallybik_vh_start = new VhStartPtr() {
        public int handler() {
            int i;

            toaplan1_videoram3 = new UBytePtr(VIDEORAM3_SIZE * 4);//if ((toaplan1_videoram3 = calloc(VIDEORAM3_SIZE * 4, 1)) == 0) /* 4 layers */

            if (errorlog != null) {
                fprintf(errorlog, "colorram_size: %08x\n", colorram1_size[0] + colorram2_size[0]);
            }
            paletteram = new UBytePtr(colorram1_size[0] + colorram2_size[0]);//if ((paletteram = calloc(colorram1_size + colorram2_size, 1)) == 0)

            /*TODO*///	
/*TODO*///		for (i=0; i<4; i++)
/*TODO*///		{
/*TODO*///			if ((bg_list[i]=(tile_struct *)malloc( 33 * 44 * sizeof(tile_struct))) == 0)
/*TODO*///			{
/*TODO*///				free(paletteram);
/*TODO*///				free(toaplan1_videoram3);
/*TODO*///				return 1;
/*TODO*///			}
/*TODO*///			memset(bg_list[i], 0, 33 * 44 * sizeof(tile_struct));
/*TODO*///		}
/*TODO*///	
/*TODO*///		for (i=0; i<16; i++)
/*TODO*///		{
/*TODO*///			max_list_size[i] = 8192;
/*TODO*///			if ((tile_list[i]=(tile_struct *)malloc(max_list_size[i]*sizeof(tile_struct))) == 0)
/*TODO*///			{
/*TODO*///				for (i=3; i>=0; i--)
/*TODO*///					free(bg_list[i]);
/*TODO*///				free(paletteram);
/*TODO*///				free(toaplan1_videoram3);
/*TODO*///				return 1;
/*TODO*///			}
/*TODO*///			memset(tile_list[i],0,max_list_size[i]*sizeof(tile_struct));
/*TODO*///		}
/*TODO*///	
/*TODO*///		max_list_size[16] = 65536;
/*TODO*///		if ((tile_list[16]=(tile_struct *)malloc(max_list_size[16]*sizeof(tile_struct))) == 0)
/*TODO*///		{
/*TODO*///			for (i=15; i>=0; i--)
/*TODO*///				free(tile_list[i]);
/*TODO*///			for (i=3; i>=0; i--)
/*TODO*///				free(bg_list[i]);
/*TODO*///			free(paletteram);
/*TODO*///			free(toaplan1_videoram3);
/*TODO*///			return 1;
/*TODO*///		}
/*TODO*///		memset(tile_list[16],0,max_list_size[16]*sizeof(tile_struct));
/*TODO*///	
            num_tiles = (Machine.drv.screen_width / 8 + 1) * (Machine.drv.screen_height / 8);

            video_ofs = video_ofs3 = 0;

            return 0;
        }
    };

    public static VhStopPtr rallybik_vh_stop = new VhStopPtr() {
        public void handler() {
            int i;
            /*TODO*///	
/*TODO*///		for (i=16; i>=0; i--)
/*TODO*///		{
/*TODO*///			free(tile_list[i]);
/*TODO*///			if (errorlog != 0) fprintf (errorlog, "max_list_size[%d]=%08x\n",i,max_list_size[i]);
/*TODO*///		}
/*TODO*///	
/*TODO*///		for (i=3; i>=0; i--)
/*TODO*///			free(bg_list[i]);
/*TODO*///	
            paletteram = null;
            toaplan1_videoram3 = null;
        }
    };

    public static VhStartPtr toaplan1_vh_start = new VhStartPtr() {
        public int handler() {
            tmpbitmap1 = osd_new_bitmap(
                    Machine.drv.screen_width,
                    Machine.drv.screen_height,
                    Machine.scrbitmap.depth);

            tmpbitmap2 = osd_new_bitmap(
                    Machine.drv.screen_width,
                    Machine.drv.screen_height,
                    Machine.scrbitmap.depth);

            tmpbitmap3 = osd_new_bitmap(
                    Machine.drv.screen_width,
                    Machine.drv.screen_height,
                    Machine.scrbitmap.depth);

            toaplan1_videoram1 = new UBytePtr(VIDEORAM1_SIZE); //if ((toaplan1_videoram1 = calloc(VIDEORAM1_SIZE, 1)) == 0)

            toaplan1_buffered_videoram1 = new UBytePtr(VIDEORAM1_SIZE); //if ((toaplan1_buffered_videoram1 = calloc(VIDEORAM1_SIZE, 1)) == 0)

            toaplan1_videoram2 = new UBytePtr(VIDEORAM2_SIZE);//if ((toaplan1_videoram2 = calloc(VIDEORAM2_SIZE, 1)) == 0)

            toaplan1_buffered_videoram2 = new UBytePtr(VIDEORAM2_SIZE);//if ((toaplan1_buffered_videoram2 = calloc(VIDEORAM2_SIZE, 1)) == 0)

            /* Also include all allocated stuff in Rally Bike startup */
            return rallybik_vh_start.handler();
        }
    };

    public static VhStopPtr toaplan1_vh_stop = new VhStopPtr() {
        public void handler() {
            rallybik_vh_stop.handler();

            toaplan1_buffered_videoram2 = null;
            toaplan1_videoram2 = null;
            toaplan1_buffered_videoram1 = null;
            toaplan1_videoram1 = null;
            osd_free_bitmap(tmpbitmap3);
            osd_free_bitmap(tmpbitmap2);
            osd_free_bitmap(tmpbitmap1);
        }
    };

    public static ReadHandlerPtr toaplan1_vblank_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return vblank ^= 1;
        }
    };

    public static WriteHandlerPtr toaplan1_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            toaplan1_flipscreen = data;
            /* 8000 flip, 0000 dont */
        }
    };

    public static ReadHandlerPtr video_ofs_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return video_ofs;
        }
    };

    public static WriteHandlerPtr video_ofs_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            video_ofs = data;
        }
    };

    /* tile palette */
    public static ReadHandlerPtr toaplan1_colorram1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return toaplan1_colorram1.READ_WORD(offset);
        }
    };

    public static WriteHandlerPtr toaplan1_colorram1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            toaplan1_colorram1.WRITE_WORD(offset, data);
            paletteram_xBBBBBGGGGGRRRRR_word_w.handler(offset, data);
        }
    };

    /* sprite palette */
    public static ReadHandlerPtr toaplan1_colorram2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return toaplan1_colorram2.READ_WORD(offset);
        }
    };

    public static WriteHandlerPtr toaplan1_colorram2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            toaplan1_colorram2.WRITE_WORD(offset, data);
            paletteram_xBBBBBGGGGGRRRRR_word_w.handler(offset + colorram1_size[0], data);
        }
    };

    public static ReadHandlerPtr toaplan1_videoram1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return toaplan1_videoram1.READ_WORD(2 * (video_ofs & (VIDEORAM1_SIZE - 1)));
        }
    };

    public static WriteHandlerPtr toaplan1_videoram1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int oldword = toaplan1_videoram1.READ_WORD(2 * video_ofs & (VIDEORAM1_SIZE - 1));
            int newword = COMBINE_WORD(oldword, data);

            toaplan1_videoram1.WRITE_WORD(2 * video_ofs & (VIDEORAM1_SIZE - 1), newword);
            video_ofs++;
        }
    };

    public static ReadHandlerPtr toaplan1_videoram2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return toaplan1_videoram2.READ_WORD(2 * video_ofs & (VIDEORAM2_SIZE - 1));
        }
    };

    public static WriteHandlerPtr toaplan1_videoram2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int oldword = toaplan1_videoram2.READ_WORD(2 * video_ofs & (VIDEORAM2_SIZE - 1));
            int newword = COMBINE_WORD(oldword, data);

            toaplan1_videoram2.WRITE_WORD(2 * video_ofs & (VIDEORAM2_SIZE - 1), newword);
            video_ofs++;
        }
    };

    public static ReadHandlerPtr video_ofs3_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return video_ofs3;
        }
    };

    public static WriteHandlerPtr video_ofs3_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            video_ofs3 = data;
        }
    };

    public static ReadHandlerPtr rallybik_videoram3_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int rb_tmp_vid;

            rb_tmp_vid = toaplan1_videoram3.READ_WORD((video_ofs3 & (VIDEORAM3_SIZE - 1)) * 4 + offset);

            if (offset == 0) {
                rb_tmp_vid |= ((rb_tmp_vid & 0xf000) >> 4);
                rb_tmp_vid |= ((rb_tmp_vid & 0x0030) << 2);
            }
            return rb_tmp_vid;
        }
    };

    public static ReadHandlerPtr toaplan1_videoram3_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return toaplan1_videoram3.READ_WORD((video_ofs3 & (VIDEORAM3_SIZE - 1)) * 4 + offset);
        }
    };

    public static WriteHandlerPtr toaplan1_videoram3_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int oldword = toaplan1_videoram3.READ_WORD((video_ofs3 & (VIDEORAM3_SIZE - 1)) * 4 + offset);
            int newword = COMBINE_WORD(oldword, data);

            toaplan1_videoram3.WRITE_WORD((video_ofs3 & (VIDEORAM3_SIZE - 1)) * 4 + offset, newword);
            if (offset == 2) {
                video_ofs3++;
            }
        }
    };

    public static ReadHandlerPtr scrollregs_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return scrollregs[offset >> 1];
        }
    };

    public static WriteHandlerPtr scrollregs_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            scrollregs[offset >> 1] = data;
        }
    };

    public static WriteHandlerPtr offsetregs_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset == 0) {
                tiles_offsetx = data;
            } else {
                tiles_offsety = data;
            }
        }
    };

    public static WriteHandlerPtr layers_offset_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            switch (offset) {
                /*		case 0:
				layers_offset[0] = (data&0xff) - 0xdb ;
				break ;
			case 2:
				layers_offset[1] = (data&0xff) - 0x14 ;
				break ;
			case 4:
				layers_offset[2] = (data&0xff) - 0x85 ;
				break ;
			case 6:
				layers_offset[3] = (data&0xff) - 0x07 ;
				break ;
                 */
                case 0:
                    layers_offset[0] = data;
                    break;
                case 2:
                    layers_offset[1] = data;
                    break;
                case 4:
                    layers_offset[2] = data;
                    break;
                case 6:
                    layers_offset[3] = data;
                    break;
            }

            if (errorlog != null) {
                fprintf(errorlog, "layers_offset[0]:%08x\n", layers_offset[0]);
            }
            if (errorlog != null) {
                fprintf(errorlog, "layers_offset[1]:%08x\n", layers_offset[1]);
            }
            if (errorlog != null) {
                fprintf(errorlog, "layers_offset[2]:%08x\n", layers_offset[2]);
            }
            if (errorlog != null) {
                fprintf(errorlog, "layers_offset[3]:%08x\n", layers_offset[3]);
            }

        }
    };

    /*TODO*///	/***************************************************************************
/*TODO*///	
/*TODO*///	  Draw the game screen in the given osd_bitmap.
/*TODO*///	
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	
/*TODO*///	static void toaplan1_update_palette (void)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///		int priority;
/*TODO*///		int color;
/*TODO*///		unsigned short palette_map[64*2];
/*TODO*///	
/*TODO*///		memset (palette_map, 0, sizeof (palette_map));
/*TODO*///	
/*TODO*///		/* extract color info from priority layers in order */
/*TODO*///		for (priority = 0; priority < 17; priority++ )
/*TODO*///		{
/*TODO*///			tile_struct *tinfo;
/*TODO*///	
/*TODO*///			tinfo = (tile_struct *)&(tile_list[priority][0]);
/*TODO*///			/* draw only tiles in list */
/*TODO*///			for ( i = 0; i < tile_count[priority]; i++ )
/*TODO*///			{
/*TODO*///				int bank;
/*TODO*///	
/*TODO*///				bank  = (tinfo.color >> 7) & 1;
/*TODO*///				color = (tinfo.color & 0x3f);
/*TODO*///				palette_map[color + bank*64] |= Machine.gfx[bank].pen_usage[tinfo.tile_num & (Machine.gfx[bank].total_elements-1)];
/*TODO*///	
/*TODO*///				tinfo++;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* Tell MAME about the color usage */
/*TODO*///		for (i = 0; i < 64*2; i++)
/*TODO*///		{
/*TODO*///			int usage = palette_map[i];
/*TODO*///			int j;
/*TODO*///	
/*TODO*///			if (usage != 0)
/*TODO*///			{
/*TODO*///				palette_used_colors[i * 16 + 0] = PALETTE_COLOR_TRANSPARENT;
/*TODO*///				for (j = 0; j < 16; j++)
/*TODO*///				{
/*TODO*///					if (usage & (1 << j))
/*TODO*///						palette_used_colors[i * 16 + j] = PALETTE_COLOR_USED;
/*TODO*///					else
/*TODO*///						palette_used_colors[i * 16 + j] = PALETTE_COLOR_UNUSED;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			else
/*TODO*///				memset(&palette_used_colors[i * 16],PALETTE_COLOR_UNUSED,16);
/*TODO*///		}
/*TODO*///	
/*TODO*///		palette_recalc ();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	static int 	layer_scrollx[4];
/*TODO*///	static int 	layer_scrolly[4];
/*TODO*///	
/*TODO*///	static public static WriteHandlerPtr toaplan1_find_tiles = new WriteHandlerPtr() { public void handler(int xoffs, int yoffs)
/*TODO*///	{
/*TODO*///		int priority;
/*TODO*///		int layer;
/*TODO*///		tile_struct *tinfo;
/*TODO*///		unsigned char *t_info;
/*TODO*///	
/*TODO*///		if (toaplan1_flipscreen != 0){
/*TODO*///			layer_scrollx[0] = ((scrollregs[0]) >> 7) + (523 - xoffs);
/*TODO*///			layer_scrollx[1] = ((scrollregs[2]) >> 7) + (525 - xoffs);
/*TODO*///			layer_scrollx[2] = ((scrollregs[4]) >> 7) + (527 - xoffs);
/*TODO*///			layer_scrollx[3] = ((scrollregs[6]) >> 7) + (529 - xoffs);
/*TODO*///	
/*TODO*///			layer_scrolly[0] = ((scrollregs[1]) >> 7) +	(256 - yoffs);
/*TODO*///			layer_scrolly[1] = ((scrollregs[3]) >> 7) + (256 - yoffs);
/*TODO*///			layer_scrolly[2] = ((scrollregs[5]) >> 7) + (256 - yoffs);
/*TODO*///			layer_scrolly[3] = ((scrollregs[7]) >> 7) + (256 - yoffs);
/*TODO*///		}else{
/*TODO*///			layer_scrollx[0] = ((scrollregs[0]) >> 7) +(495 - xoffs + 6);
/*TODO*///			layer_scrollx[1] = ((scrollregs[2]) >> 7) +(495 - xoffs + 4);
/*TODO*///			layer_scrollx[2] = ((scrollregs[4]) >> 7) +(495 - xoffs + 2);
/*TODO*///			layer_scrollx[3] = ((scrollregs[6]) >> 7) +(495 - xoffs);
/*TODO*///	
/*TODO*///			layer_scrolly[0] = ((scrollregs[1]) >> 7) +	(0x101 - yoffs);
/*TODO*///			layer_scrolly[1] = ((scrollregs[3]) >> 7) + (0x101 - yoffs);
/*TODO*///			layer_scrolly[2] = ((scrollregs[5]) >> 7) + (0x101 - yoffs);
/*TODO*///			layer_scrolly[3] = ((scrollregs[7]) >> 7) + (0x101 - yoffs);
/*TODO*///		}
/*TODO*///	
/*TODO*///		for ( layer = 3 ; layer >= 0 ; layer-- )
/*TODO*///		{
/*TODO*///			int scrolly,scrollx,offsetx,offsety;
/*TODO*///			int sx,sy,tattr;
/*TODO*///			int i;
/*TODO*///	
/*TODO*///			t_info = (toaplan1_videoram3+layer * VIDEORAM3_SIZE);
/*TODO*///			scrollx = layer_scrollx[layer];
/*TODO*///			offsetx = scrollx / 8 ;
/*TODO*///			scrolly = layer_scrolly[layer];
/*TODO*///			offsety = scrolly / 8 ;
/*TODO*///	
/*TODO*///			for ( sy = 0 ; sy < 32 ; sy++ )
/*TODO*///			{
/*TODO*///				for ( sx = 0 ; sx <= 40 ; sx++ )
/*TODO*///				{
/*TODO*///					i = ((sy+offsety)&0x3f)*256 + ((sx+offsetx)&0x3f)*4 ;
/*TODO*///					tattr = READ_WORD(&t_info[i]);
/*TODO*///					priority = (tattr >> 12);
/*TODO*///	
/*TODO*///					tinfo = (tile_struct *)&(bg_list[layer][sy*41+sx]) ;
/*TODO*///					tinfo.tile_num = READ_WORD(&t_info[i+2]) ;
/*TODO*///					tinfo.priority = priority ;
/*TODO*///					tinfo.color = tattr & 0x3f ;
/*TODO*///					tinfo.xpos = (sx*8)-(scrollx&0x7) ;
/*TODO*///					tinfo.ypos = (sy*8)-(scrolly&0x7) ;
/*TODO*///	
/*TODO*///					if ( (priority) || (layer == 0) )	/* if priority 0 draw layer 0 only */
/*TODO*///					{
/*TODO*///	
/*TODO*///						tinfo = (tile_struct *)&(tile_list[priority][tile_count[priority]]) ;
/*TODO*///						tinfo.tile_num = READ_WORD(&t_info[i+2]) ;
/*TODO*///						if ( (tinfo.tile_num & 0x8000) == 0 )
/*TODO*///						{
/*TODO*///							tinfo.priority = priority ;
/*TODO*///							tinfo.color = tattr & 0x3f ;
/*TODO*///							tinfo.color |= layer<<8;
/*TODO*///							tinfo.xpos = (sx*8)-(scrollx&0x7) ;
/*TODO*///							tinfo.ypos = (sy*8)-(scrolly&0x7) ;
/*TODO*///							tile_count[priority]++ ;
/*TODO*///							if(tile_count[priority]==max_list_size[priority])
/*TODO*///							{
/*TODO*///								/*reallocate tile_list[priority] to larger size */
/*TODO*///								temp_list=(tile_struct *)malloc(sizeof(tile_struct)*(max_list_size[priority]+512)) ;
/*TODO*///								memcpy(temp_list,tile_list[priority],sizeof(tile_struct)*max_list_size[priority]);
/*TODO*///								max_list_size[priority]+=512;
/*TODO*///								free(tile_list[priority]);
/*TODO*///								tile_list[priority] = temp_list ;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///		for ( layer = 3 ; layer >= 0 ; layer-- )
/*TODO*///		{
/*TODO*///			layer_scrollx[layer] &= 0x7;
/*TODO*///			layer_scrolly[layer] &= 0x7;
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static void rallybik_find_tiles(void)
/*TODO*///	{
/*TODO*///		int priority;
/*TODO*///		int layer;
/*TODO*///		tile_struct *tinfo;
/*TODO*///		unsigned char *t_info;
/*TODO*///	
/*TODO*///		for ( priority = 0 ; priority < 16 ; priority++ )
/*TODO*///		{
/*TODO*///			tile_count[priority]=0;
/*TODO*///		}
/*TODO*///	
/*TODO*///		for ( layer = 3 ; layer >= 0 ; layer-- )
/*TODO*///		{
/*TODO*///			int scrolly,scrollx,offsetx,offsety;
/*TODO*///			int sx,sy,tattr;
/*TODO*///			int i;
/*TODO*///	
/*TODO*///			t_info = (toaplan1_videoram3+layer * VIDEORAM3_SIZE);
/*TODO*///			scrollx = scrollregs[layer*2];
/*TODO*///			scrolly = scrollregs[(layer*2)+1];
/*TODO*///	
/*TODO*///			scrollx >>= 7 ;
/*TODO*///			scrollx += 43 ;
/*TODO*///			if ( layer == 0 ) scrollx += 2 ;
/*TODO*///			if ( layer == 2 ) scrollx -= 2 ;
/*TODO*///			if ( layer == 3 ) scrollx -= 4 ;
/*TODO*///			offsetx = scrollx / 8 ;
/*TODO*///	
/*TODO*///			scrolly >>= 7 ;
/*TODO*///			scrolly += 21 ;
/*TODO*///			offsety = scrolly / 8 ;
/*TODO*///	
/*TODO*///			for ( sy = 0 ; sy < 32 ; sy++ )
/*TODO*///			{
/*TODO*///				for ( sx = 0 ; sx <= 40 ; sx++ )
/*TODO*///				{
/*TODO*///					i = ((sy+offsety)&0x3f)*256 + ((sx+offsetx)&0x3f)*4 ;
/*TODO*///					tattr = READ_WORD(&t_info[i]);
/*TODO*///					priority = tattr >> 12 ;
/*TODO*///					if ( (priority) || (layer == 0) )	/* if priority 0 draw layer 0 only */
/*TODO*///					{
/*TODO*///						tinfo = (tile_struct *)&(tile_list[priority][tile_count[priority]]) ;
/*TODO*///						tinfo.tile_num = READ_WORD(&t_info[i+2]) ;
/*TODO*///	
/*TODO*///						if ( !((priority) && (tinfo.tile_num & 0x8000)) )
/*TODO*///						{
/*TODO*///							tinfo.tile_num &= 0x3fff ;
/*TODO*///							tinfo.color = tattr & 0x3f ;
/*TODO*///							tinfo.xpos = (sx*8)-(scrollx&0x7) ;
/*TODO*///							tinfo.ypos = (sy*8)-(scrolly&0x7) ;
/*TODO*///							tile_count[priority]++ ;
/*TODO*///							if(tile_count[priority]==max_list_size[priority])
/*TODO*///							{
/*TODO*///								/*reallocate tile_list[priority] to larger size */
/*TODO*///								temp_list=(tile_struct *)malloc(sizeof(tile_struct)*(max_list_size[priority]+512)) ;
/*TODO*///								memcpy(temp_list,tile_list[priority],sizeof(tile_struct)*max_list_size[priority]);
/*TODO*///								max_list_size[priority]+=512;
/*TODO*///								free(tile_list[priority]);
/*TODO*///								tile_list[priority] = temp_list ;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned long toaplan_sp_ram_dump = 0;
/*TODO*///	
/*TODO*///	static void toaplan1_find_sprites (void)
/*TODO*///	{
/*TODO*///		int priority;
/*TODO*///		int sprite;
/*TODO*///		unsigned char *s_info,*s_size;
/*TODO*///	
/*TODO*///	
/*TODO*///		for ( priority = 0 ; priority < 17 ; priority++ )
/*TODO*///		{
/*TODO*///			tile_count[priority]=0;
/*TODO*///		}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///		s_size = (toaplan1_buffered_videoram2);		/* sprite block size */
/*TODO*///		s_info = (toaplan1_buffered_videoram1);		/* start of sprite ram */
/*TODO*///	
/*TODO*///		for ( sprite = 0 ; sprite < 256 ; sprite++ )
/*TODO*///		{
/*TODO*///			int tattr,tchar;
/*TODO*///	
/*TODO*///			tchar = READ_WORD (&s_info[0]) & 0xffff;
/*TODO*///			tattr = READ_WORD (&s_info[2]);
/*TODO*///	
/*TODO*///			if ( (tattr & 0xf000) && ((tchar & 0x8000) == 0) )
/*TODO*///			{
/*TODO*///				int sx,sy,dx,dy,s_sizex,s_sizey;
/*TODO*///				int sprite_size_ptr;
/*TODO*///	
/*TODO*///				sx=READ_WORD(&s_info[4]);
/*TODO*///				sx >>= 7 ;
/*TODO*///				if ( sx > 416 ) sx -= 512 ;
/*TODO*///	
/*TODO*///				sy=READ_WORD(&s_info[6]);
/*TODO*///				sy >>= 7 ;
/*TODO*///				if ( sy > 416 ) sy -= 512 ;
/*TODO*///	
/*TODO*///				priority = (tattr >> 12);
/*TODO*///	
/*TODO*///				sprite_size_ptr = (tattr>>6)&0x3f ;
/*TODO*///				s_sizey = (READ_WORD(&s_size[2*sprite_size_ptr])>>4)&0xf ;
/*TODO*///				s_sizex = (READ_WORD(&s_size[2*sprite_size_ptr]))&0xf ;
/*TODO*///	
/*TODO*///				for ( dy = s_sizey ; dy > 0 ; dy-- )
/*TODO*///				for ( dx = s_sizex; dx > 0 ; dx-- )
/*TODO*///				{
/*TODO*///					tile_struct *tinfo;
/*TODO*///	
/*TODO*///					tinfo = (tile_struct *)&(tile_list[16][tile_count[16]]) ;
/*TODO*///					tinfo.priority = priority;
/*TODO*///					tinfo.tile_num = tchar;
/*TODO*///					tinfo.color = 0x80 | (tattr & 0x3f) ;
/*TODO*///					tinfo.xpos = sx-dx*8+s_sizex*8 ;
/*TODO*///					tinfo.ypos = sy-dy*8+s_sizey*8 ;
/*TODO*///					tile_count[16]++ ;
/*TODO*///					if(tile_count[16]==max_list_size[16])
/*TODO*///					{
/*TODO*///						/*reallocate tile_list[priority] to larger size */
/*TODO*///						temp_list=(tile_struct *)malloc(sizeof(tile_struct)*(max_list_size[16]+512)) ;
/*TODO*///						memcpy(temp_list,tile_list[16],sizeof(tile_struct)*max_list_size[16]);
/*TODO*///						max_list_size[16]+=512;
/*TODO*///						free(tile_list[16]);
/*TODO*///						tile_list[16] = temp_list ;
/*TODO*///					}
/*TODO*///					tchar++;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			s_info += 8 ;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void rallybik_find_sprites (void)
/*TODO*///	{
/*TODO*///		int offs;
/*TODO*///		int tattr;
/*TODO*///		int sx,sy,tchar;
/*TODO*///		int priority;
/*TODO*///		tile_struct *tinfo;
/*TODO*///	
/*TODO*///		for (offs = 0;offs < spriteram_size;offs += 8)
/*TODO*///		{
/*TODO*///			tattr = READ_WORD(&buffered_spriteram[offs+2]);
/*TODO*///			if (tattr != 0)	/* no need to render hidden sprites */
/*TODO*///			{
/*TODO*///				sx=READ_WORD(&buffered_spriteram[offs+4]);
/*TODO*///				sx >>= 7 ;
/*TODO*///				sx &= 0x1ff ;
/*TODO*///				if ( sx > 416 ) sx -= 512 ;
/*TODO*///	
/*TODO*///				sy=READ_WORD(&buffered_spriteram[offs+6]);
/*TODO*///				sy >>= 7 ;
/*TODO*///				sy &= 0x1ff ;
/*TODO*///				if ( sy > 416 ) sy -= 512 ;
/*TODO*///	
/*TODO*///				priority = (tattr>>8) & 0xc ;
/*TODO*///				tchar = READ_WORD(&buffered_spriteram[offs+0]);
/*TODO*///				tinfo = (tile_struct *)&(tile_list[priority][tile_count[priority]]) ;
/*TODO*///				tinfo.tile_num = tchar & 0x7ff ;
/*TODO*///				tinfo.color = 0x80 | (tattr&0x3f) ;
/*TODO*///				tinfo.color |= (tattr & 0x0100) ;
/*TODO*///				tinfo.color |= (tattr & 0x0200) ;
/*TODO*///				if (tinfo.color & 0x0100) sx -= 15;
/*TODO*///	
/*TODO*///				tinfo.xpos = sx-31 ;
/*TODO*///				tinfo.ypos = sy-16 ;
/*TODO*///				tile_count[priority]++ ;
/*TODO*///				if(tile_count[priority]==max_list_size[priority])
/*TODO*///				{
/*TODO*///					/*reallocate tile_list[priority] to larger size */
/*TODO*///					temp_list=(tile_struct *)malloc(sizeof(tile_struct)*(max_list_size[priority]+512)) ;
/*TODO*///					memcpy(temp_list,tile_list[priority],sizeof(tile_struct)*max_list_size[priority]);
/*TODO*///					max_list_size[priority]+=512;
/*TODO*///					free(tile_list[priority]);
/*TODO*///					tile_list[priority] = temp_list ;
/*TODO*///				}
/*TODO*///			}  // if tattr
/*TODO*///		} // for sprite
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	void toaplan1_fillbgmask(struct osd_bitmap *dest_bmp, struct osd_bitmap *source_bmp,
/*TODO*///	 const struct rectangle *clip,int transparent_color)
/*TODO*///	{
/*TODO*///		struct rectangle myclip;
/*TODO*///		int sx=0;
/*TODO*///		int sy=0;
/*TODO*///	
/*TODO*///		if (Machine.orientation & ORIENTATION_SWAP_XY)
/*TODO*///		{
/*TODO*///			int temp;
/*TODO*///	
/*TODO*///			/* clip and myclip might be the same, so we need a temporary storage */
/*TODO*///			temp = clip.min_x;
/*TODO*///			myclip.min_x = clip.min_y;
/*TODO*///			myclip.min_y = temp;
/*TODO*///			temp = clip.max_x;
/*TODO*///			myclip.max_x = clip.max_y;
/*TODO*///			myclip.max_y = temp;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///		if (Machine.orientation & ORIENTATION_FLIP_X)
/*TODO*///		{
/*TODO*///			int temp;
/*TODO*///	
/*TODO*///			sx = -sx;
/*TODO*///	
/*TODO*///			/* clip and myclip might be the same, so we need a temporary storage */
/*TODO*///			temp = clip.min_x;
/*TODO*///			myclip.min_x = dest_bmp.width-1 - clip.max_x;
/*TODO*///			myclip.max_x = dest_bmp.width-1 - temp;
/*TODO*///			myclip.min_y = clip.min_y;
/*TODO*///			myclip.max_y = clip.max_y;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///		if (Machine.orientation & ORIENTATION_FLIP_Y)
/*TODO*///		{
/*TODO*///			int temp;
/*TODO*///	
/*TODO*///			sy = -sy;
/*TODO*///	
/*TODO*///			myclip.min_x = clip.min_x;
/*TODO*///			myclip.max_x = clip.max_x;
/*TODO*///			/* clip and myclip might be the same, so we need a temporary storage */
/*TODO*///			temp = clip.min_y;
/*TODO*///			myclip.min_y = dest_bmp.height-1 - clip.max_y;
/*TODO*///			myclip.max_y = dest_bmp.height-1 - temp;
/*TODO*///			clip = &myclip;
/*TODO*///		}
/*TODO*///	
/*TODO*///		if (dest_bmp.depth != 16)
/*TODO*///		{
/*TODO*///			int ex = sx+source_bmp.width;
/*TODO*///			int ey = sy+source_bmp.height;
/*TODO*///	
/*TODO*///			if( sx < clip.min_x)
/*TODO*///			{ /* clip left */
/*TODO*///				sx = clip.min_x;
/*TODO*///			}
/*TODO*///			if( sy < clip.min_y )
/*TODO*///			{ /* clip top */
/*TODO*///				sy = clip.min_y;
/*TODO*///			}
/*TODO*///			if( ex > clip.max_x+1 )
/*TODO*///			{ /* clip right */
/*TODO*///				ex = clip.max_x + 1;
/*TODO*///			}
/*TODO*///			if( ey > clip.max_y+1 )
/*TODO*///			{ /* clip bottom */
/*TODO*///				ey = clip.max_y + 1;
/*TODO*///			}
/*TODO*///	
/*TODO*///			if( ex>sx )
/*TODO*///			{ /* skip if inner loop doesn't draw anything */
/*TODO*///				int y;
/*TODO*///				for( y=sy; y<ey; y++ )
/*TODO*///				{
/*TODO*///					unsigned char *dest = dest_bmp.line[y];
/*TODO*///					unsigned char *source = source_bmp.line[y];
/*TODO*///					int x;
/*TODO*///	
/*TODO*///					for( x=sx; x<ex; x++ )
/*TODO*///					{
/*TODO*///						int c = source[x];
/*TODO*///						if( c != transparent_color )
/*TODO*///							dest[x] = transparent_color;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		else
/*TODO*///		{
/*TODO*///			int ex = sx+source_bmp.width;
/*TODO*///			int ey = sy+source_bmp.height;
/*TODO*///	
/*TODO*///			if( sx < clip.min_x)
/*TODO*///			{ /* clip left */
/*TODO*///				sx = clip.min_x;
/*TODO*///			}
/*TODO*///			if( sy < clip.min_y )
/*TODO*///			{ /* clip top */
/*TODO*///				sy = clip.min_y;
/*TODO*///			}
/*TODO*///			if( ex > clip.max_x+1 )
/*TODO*///			{ /* clip right */
/*TODO*///				ex = clip.max_x + 1;
/*TODO*///			}
/*TODO*///			if( ey > clip.max_y+1 )
/*TODO*///			{ /* clip bottom */
/*TODO*///				ey = clip.max_y + 1;
/*TODO*///			}
/*TODO*///	
/*TODO*///			if( ex>sx )
/*TODO*///			{ /* skip if inner loop doesn't draw anything */
/*TODO*///				int y;
/*TODO*///	
/*TODO*///				for( y=sy; y<ey; y++ )
/*TODO*///				{
/*TODO*///					unsigned short *dest = (unsigned short *)dest_bmp.line[y];
/*TODO*///					unsigned char *source = source_bmp.line[y];
/*TODO*///					int x;
/*TODO*///	
/*TODO*///					for( x=sx; x<ex; x++ )
/*TODO*///					{
/*TODO*///						int c = source[x];
/*TODO*///						if( c != transparent_color )
/*TODO*///							dest[x] = transparent_color;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void toaplan1_render (struct osd_bitmap *bitmap)
/*TODO*///	{
/*TODO*///		int i,j;
/*TODO*///		int priority,pen;
/*TODO*///		int	flip;
/*TODO*///		tile_struct *tinfo;
/*TODO*///		tile_struct *tinfo2;
/*TODO*///		struct rectangle sp_rect;
/*TODO*///	
/*TODO*///		fillbitmap (bitmap, palette_transparent_pen, &Machine.drv.visible_area);
/*TODO*///	
/*TODO*///	
/*TODO*///	//	if (toaplan1_flipscreen != 0)
/*TODO*///	//		flip = 1;
/*TODO*///	//	else
/*TODO*///			flip = 0;
/*TODO*///	//
/*TODO*///		priority = 0;
/*TODO*///		while ( priority < 16 )			/* draw priority layers in order */
/*TODO*///		{
/*TODO*///			int	layer;
/*TODO*///	
/*TODO*///			tinfo = (tile_struct *)&(tile_list[priority][0]) ;
/*TODO*///			layer = (tinfo.color >> 8);
/*TODO*///			if ( (layer < 3) && (priority < 2))
/*TODO*///				pen = TRANSPARENCY_NONE ;
/*TODO*///			else
/*TODO*///				pen = TRANSPARENCY_PEN ;
/*TODO*///	
/*TODO*///			for ( i = 0 ; i < tile_count[priority] ; i++ ) /* draw only tiles in list */
/*TODO*///			{
/*TODO*///				int	xpos,ypos;
/*TODO*///	
/*TODO*///				/* hack to fix blue blobs in Zero Wing attract mode */
/*TODO*///	//			if ((pen == TRANSPARENCY_NONE) && ((tinfo.color&0x3f)==0))
/*TODO*///	//				pen = TRANSPARENCY_PEN ;
/*TODO*///	
/*TODO*///	//			if (flip != 0){
/*TODO*///	//				xpos = tinfo.xpos;
/*TODO*///	//				ypos = tinfo.ypos;
/*TODO*///	//			}
/*TODO*///	//			else{
/*TODO*///					xpos = tinfo.xpos;
/*TODO*///					ypos = tinfo.ypos;
/*TODO*///	//			}
/*TODO*///	
/*TODO*///				drawgfx(bitmap,Machine.gfx[0],
/*TODO*///					tinfo.tile_num,
/*TODO*///					(tinfo.color&0x3f),
/*TODO*///					flip,flip,							/* flipx,flipy */
/*TODO*///					tinfo.xpos,tinfo.ypos,
/*TODO*///					&Machine.drv.visible_area,pen,0);
/*TODO*///				tinfo++ ;
/*TODO*///			}
/*TODO*///			priority++;
/*TODO*///		}
/*TODO*///	
/*TODO*///		tinfo2 = (tile_struct *)&(tile_list[16][0]) ;
/*TODO*///		for ( i = 0; i < tile_count[16]; i++ )	/* draw sprite No. in order */
/*TODO*///		{
/*TODO*///			int	flipx,flipy;
/*TODO*///	
/*TODO*///			sp_rect.min_x = tinfo2.xpos;
/*TODO*///			sp_rect.min_y = tinfo2.ypos;
/*TODO*///			sp_rect.max_x = tinfo2.xpos + 7;
/*TODO*///			sp_rect.max_y = tinfo2.ypos + 7;
/*TODO*///	
/*TODO*///			fillbitmap (tmpbitmap2, palette_transparent_pen, &sp_rect);
/*TODO*///	
/*TODO*///			flipx = (tinfo2.color & 0x0100);
/*TODO*///			flipy = (tinfo2.color & 0x0200);
/*TODO*///	//		if (toaplan1_flipscreen != 0){
/*TODO*///	//			flipx = !flipx;
/*TODO*///	//			flipy = !flipy;
/*TODO*///	//		}
/*TODO*///			drawgfx(tmpbitmap2,Machine.gfx[1],
/*TODO*///				tinfo2.tile_num,
/*TODO*///				(tinfo2.color&0x3f), 			/* bit 7 not for colour */
/*TODO*///				flipx,flipy,					/* flipx,flipy */
/*TODO*///				tinfo2.xpos,tinfo2.ypos,
/*TODO*///				&Machine.drv.visible_area,TRANSPARENCY_PEN,0);
/*TODO*///	
/*TODO*///			priority = tinfo2.priority;
/*TODO*///			{
/*TODO*///			int ix0,ix1,ix2,ix3;
/*TODO*///			int dirty;
/*TODO*///	
/*TODO*///			dirty = 0;
/*TODO*///			fillbitmap (tmpbitmap3, palette_transparent_pen, &sp_rect);
/*TODO*///			for ( j = 0 ; j < 4 ; j++ )
/*TODO*///			{
/*TODO*///				int x,y;
/*TODO*///	
/*TODO*///				y = tinfo2.ypos+layer_scrolly[j];
/*TODO*///				x = tinfo2.xpos+layer_scrollx[j];
/*TODO*///				ix0 = ( y   /8) * 41 +  x   /8;
/*TODO*///				ix1 = ( y   /8) * 41 + (x+7)/8;
/*TODO*///				ix2 = ((y+7)/8) * 41 +  x   /8;
/*TODO*///				ix3 = ((y+7)/8) * 41 + (x+7)/8;
/*TODO*///	
/*TODO*///				if(	(ix0 >= 0) && (ix0 < 32*41) ){
/*TODO*///					tinfo = (tile_struct *)&(bg_list[j][ix0]) ;
/*TODO*///					if( (tinfo.priority >= tinfo2.priority) ){
/*TODO*///						drawgfx(tmpbitmap3,Machine.gfx[0],
/*TODO*///	//					drawgfx(tmpbitmap2,Machine.gfx[0],
/*TODO*///							tinfo.tile_num,
/*TODO*///							(tinfo.color&0x3f),
/*TODO*///							flip,flip,
/*TODO*///							tinfo.xpos,tinfo.ypos,
/*TODO*///							&sp_rect,TRANSPARENCY_PEN,0);
/*TODO*///						dirty=1;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				if(	(ix1 >= 0) && (ix1 < 32*41) ){
/*TODO*///					tinfo = (tile_struct *)&(bg_list[j][ix1]) ;
/*TODO*///	//				tinfo++;
/*TODO*///					if( (ix0 != ix1)
/*TODO*///					 && (tinfo.priority >= tinfo2.priority) ){
/*TODO*///						drawgfx(tmpbitmap3,Machine.gfx[0],
/*TODO*///	//					drawgfx(tmpbitmap2,Machine.gfx[0],
/*TODO*///							tinfo.tile_num,
/*TODO*///							(tinfo.color&0x3f),
/*TODO*///							flip,flip,
/*TODO*///							tinfo.xpos,tinfo.ypos,
/*TODO*///							&sp_rect,TRANSPARENCY_PEN,0);
/*TODO*///						dirty=1;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				if(	(ix2 >= 0) && (ix2 < 32*41) ){
/*TODO*///					tinfo = (tile_struct *)&(bg_list[j][ix2]) ;
/*TODO*///	//				tinfo += 40;
/*TODO*///					if( (ix0 != ix2)
/*TODO*///					 && (tinfo.priority >= tinfo2.priority) ){
/*TODO*///						drawgfx(tmpbitmap3,Machine.gfx[0],
/*TODO*///	//					drawgfx(tmpbitmap2,Machine.gfx[0],
/*TODO*///							tinfo.tile_num,
/*TODO*///							(tinfo.color&0x3f),
/*TODO*///							flip,flip,
/*TODO*///							tinfo.xpos,tinfo.ypos,
/*TODO*///							&sp_rect,TRANSPARENCY_PEN,0);
/*TODO*///						dirty=1;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				if(	(ix3 >= 0) && (ix3 < 32*41) ){
/*TODO*///					tinfo = (tile_struct *)&(bg_list[j][ix3]) ;
/*TODO*///	//				tinfo++;
/*TODO*///					if( (ix0 != ix3) && (ix1 != ix3) && (ix2 != ix3)
/*TODO*///					 && (tinfo.priority >= tinfo2.priority) ){
/*TODO*///						drawgfx(tmpbitmap3,Machine.gfx[0],
/*TODO*///	//					drawgfx(tmpbitmap2,Machine.gfx[0],
/*TODO*///							tinfo.tile_num,
/*TODO*///							(tinfo.color&0x3f),
/*TODO*///							flip,flip,
/*TODO*///							tinfo.xpos,tinfo.ypos,
/*TODO*///							&sp_rect,TRANSPARENCY_PEN,0);
/*TODO*///						dirty=1;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///			if(	dirty != 0 )
/*TODO*///			{
/*TODO*///				toaplan1_fillbgmask(
/*TODO*///					tmpbitmap2,				// dist
/*TODO*///					tmpbitmap3,				// mask
/*TODO*///					&sp_rect,
/*TODO*///					palette_transparent_pen
/*TODO*///				);
/*TODO*///			}
/*TODO*///			copybitmap(bitmap, tmpbitmap2, 0, 0, 0, 0, &sp_rect, TRANSPARENCY_PEN, palette_transparent_pen);
/*TODO*///			tinfo2++;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///	
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static void rallybik_render (struct osd_bitmap *bitmap)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///		int priority,pen;
/*TODO*///		tile_struct *tinfo;
/*TODO*///	
/*TODO*///		fillbitmap (bitmap, palette_transparent_pen, &Machine.drv.visible_area);
/*TODO*///	
/*TODO*///		for ( priority = 0 ; priority < 16 ; priority++ )	/* draw priority layers in order */
/*TODO*///		{
/*TODO*///			tinfo = (tile_struct *)&(tile_list[priority][0]) ;
/*TODO*///			/* hack to fix black blobs in Demon's World sky */
/*TODO*///			if ( priority == 1 )
/*TODO*///				pen = TRANSPARENCY_NONE ;
/*TODO*///			else
/*TODO*///				pen = TRANSPARENCY_PEN ;
/*TODO*///			for ( i = 0 ; i < tile_count[priority] ; i++ ) /* draw only tiles in list */
/*TODO*///			{
/*TODO*///				/* hack to fix blue blobs in Zero Wing attract mode */
/*TODO*///				if ((pen == TRANSPARENCY_NONE) && ((tinfo.color&0x3f)==0))
/*TODO*///					pen = TRANSPARENCY_PEN ;
/*TODO*///	
/*TODO*///				drawgfx(bitmap,Machine.gfx[(tinfo.color>>7)&1],	/* bit 7 set for sprites */
/*TODO*///					tinfo.tile_num,
/*TODO*///					(tinfo.color&0x3f), 			/* bit 7 not for colour */
/*TODO*///					(tinfo.color & 0x0100),(tinfo.color & 0x0200),	/* flipx,flipy */
/*TODO*///					tinfo.xpos,tinfo.ypos,
/*TODO*///					&Machine.drv.visible_area,pen,0);
/*TODO*///				tinfo++ ;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
    public static VhUpdatePtr toaplan1_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            /*TODO*///		/* discover what data will be drawn */
/*TODO*///		toaplan1_find_sprites();
/*TODO*///		toaplan1_find_tiles(tiles_offsetx,tiles_offsety);
/*TODO*///	
/*TODO*///		toaplan1_update_palette();
/*TODO*///		toaplan1_render(bitmap);
        }
    };

    public static VhUpdatePtr rallybik_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            /*TODO*///		/* discover what data will be drawn */
/*TODO*///		rallybik_find_tiles();
/*TODO*///		rallybik_find_sprites();
/*TODO*///	
/*TODO*///		toaplan1_update_palette();
/*TODO*///		rallybik_render(bitmap);
        }
    };

    /**
     * **************************************************************************
     * Spriteram is always 1 frame ahead, suggesting spriteram buffering. There
     * are no CPU output registers that control this so we assume it happens
     * automatically every frame, at the end of vblank
     * **************************************************************************
     */
    public static VhEofCallbackPtr toaplan1_eof_callback = new VhEofCallbackPtr() {
        public void handler() {
            memcpy(toaplan1_buffered_videoram1, toaplan1_videoram1, VIDEORAM1_SIZE);
            memcpy(toaplan1_buffered_videoram2, toaplan1_videoram2, VIDEORAM2_SIZE);
        }
    };

    public static VhEofCallbackPtr rallybik_eof_callback = new VhEofCallbackPtr() {
        public void handler() {
            buffer_spriteram_w.handler(0, 0);

        }
    };

    public static VhEofCallbackPtr samesame_eof_callback = new VhEofCallbackPtr() {
        public void handler() {
            memcpy(toaplan1_buffered_videoram1, toaplan1_videoram1, VIDEORAM1_SIZE);
            memcpy(toaplan1_buffered_videoram2, toaplan1_videoram2, VIDEORAM2_SIZE);
            cpu_set_irq_line(0, MC68000_IRQ_2, HOLD_LINE);
            /* Frame done */
        }
    };
}
