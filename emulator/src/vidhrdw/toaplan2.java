/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package vidhrdw;

import static arcadeflex.libc.*;
import static arcadeflex.libc_old.fprintf;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static vidhrdw.generic.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.mame.*;
import static arcadeflex.ptrlib.*;
import static drivers.toaplan2.*;
import static mame.cpuintrf.cpu_set_reset_line;
import static mame.cpuintrfH.PULSE_LINE;
import static mame.palette.*;
import static mame.paletteH.*;
import static sound._3812intf.*;

public class toaplan2
{
	public static final int TOAPLAN2_BG_VRAM_SIZE	=0x1000;	/* Background RAM size (in bytes) */
	public static final int TOAPLAN2_FG_VRAM_SIZE	=0x1000;	/* Foreground RAM size (in bytes) */
	public static final int TOAPLAN2_TOP_VRAM_SIZE	=0x1000;	/* Top Layer  RAM size (in bytes) */
	public static final int TOAPLAN2_SPRITERAM_SIZE	=0x0800;	/* Sprite	  RAM size (in bytes) */
	
	public static final int TOAPLAN2_SPRITE_FLIPX =0x1000;	/* Sprite flip flags (for screen flip) */
	public static final int TOAPLAN2_SPRITE_FLIPY =0x2000;
	
	public static final int CPU_2_NONE		=0x00;
	public static final int CPU_2_Z80		=0x5a;
	public static final int CPU_2_HD647180	=0xa5;
	public static final int CPU_2_Zx80		=0xff;
	
	
/*TODO*///	static unsigned char *bgvideoram[2];
/*TODO*///	static unsigned char *fgvideoram[2];
/*TODO*///	static unsigned char *topvideoram[2];
/*TODO*///	static unsigned char *spriteram_now[2];	 /* Sprites to draw this frame */
/*TODO*///	static unsigned char *spriteram_next[2]; /* Sprites to draw next frame */
/*TODO*///	static unsigned char *spriteram_new[2];	 /* Sprites to add to next frame */
	static int toaplan2_unk_vram;			 /* Video RAM tested but not used (for Teki Paki)*/
	
	static int[] toaplan2_scroll_reg=new int[2];
	static int[] toaplan2_voffs=new int[2];
	static int[] bg_offs=new int[2];
	static int[] fg_offs=new int[2];
	static int[] top_offs=new int[2];
	static int[] sprite_offs=new int[2];
	static int[] bg_scrollx=new int[2];
	static int[] bg_scrolly=new int[2];
	static int[] fg_scrollx=new int[2];
	static int[] fg_scrolly=new int[2];
	static int[] top_scrollx=new int[2];
	static int[] top_scrolly=new int[2];
	static int[] sprite_scrollx=new int[2];
	static int[] sprite_scrolly=new int[2];
	
	static int[] display_sp = { 1, 1 };
	
	static int[][] sprite_priority=new int[2][16];
	static int[] bg_flip = { 0, 0 };
	static int[] fg_flip = { 0, 0 };
	static int[] top_flip = { 0, 0 };
	static int[] sprite_flip = { 0, 0 };
	
	
/*TODO*///	static struct tilemap *top_tilemap[2], *fg_tilemap[2], *bg_tilemap[2];
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static WriteHandlerPtr get_top0_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
/*TODO*///		int color, tile_number, attrib, offset;
/*TODO*///		UINT16 *source = (UINT16 *)(topvideoram[0]);
	
/*TODO*///		offset = ((row*64) + (col*2)) & 0x7ff;
	
/*TODO*///		attrib = source[offset];
/*TODO*///		tile_number = source[offset+1];
/*TODO*///		color = attrib & 0x7f;
/*TODO*///		SET_TILE_INFO(0,tile_number,color)
/*TODO*///		tile_info.priority = (attrib & 0x0f00) >> 8;
	} };
	
	public static WriteHandlerPtr get_fg0_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
/*TODO*///		int color, tile_number, attrib, offset;
/*TODO*///		UINT16 *source = (UINT16 *)(fgvideoram[0]);
	
/*TODO*///		offset = ((row*64) + (col*2)) & 0x7ff;
	
/*TODO*///		attrib = source[offset];
/*TODO*///		tile_number = source[offset+1];
/*TODO*///		color = attrib & 0x7f;
/*TODO*///		SET_TILE_INFO(0,tile_number,color)
/*TODO*///		tile_info.priority = (attrib & 0x0f00) >> 8;
	} };
	
	public static WriteHandlerPtr get_bg0_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
/*TODO*///		int color, tile_number, attrib, offset;
/*TODO*///		UINT16 *source = (UINT16 *)(bgvideoram[0]);
	
/*TODO*///		offset = ((row*64) + (col*2)) & 0x7ff;
	
/*TODO*///		attrib = source[offset];
/*TODO*///		tile_number = source[offset+1];
/*TODO*///		color = attrib & 0x7f;
/*TODO*///		SET_TILE_INFO(0,tile_number,color)
/*TODO*///		tile_info.priority = (attrib & 0x0f00) >> 8;
	} };
	
	
	public static WriteHandlerPtr get_top1_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
/*TODO*///		int color, tile_number, attrib, offset;
/*TODO*///		UINT16 *source = (UINT16 *)(topvideoram[1]);
	
/*TODO*///		offset = ((row*64) + (col*2)) & 0x7ff;
	
/*TODO*///		attrib = source[offset];
/*TODO*///		tile_number = source[offset+1];
/*TODO*///		color = attrib & 0x7f;
/*TODO*///		SET_TILE_INFO(2,tile_number,color)
/*TODO*///		tile_info.priority = (attrib & 0x0f00) >> 8;
	} };
	
	public static WriteHandlerPtr get_fg1_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
/*TODO*///		int color, tile_number, attrib, offset;
/*TODO*///		UINT16 *source = (UINT16 *)(fgvideoram[1]);
/*TODO*///	
/*TODO*///		offset = ((row*64) + (col*2)) & 0x7ff;
/*TODO*///	
/*TODO*///		attrib = source[offset];
/*TODO*///		tile_number = source[offset+1];
/*TODO*///		color = attrib & 0x7f;
/*TODO*///		SET_TILE_INFO(2,tile_number,color)
/*TODO*///		tile_info.priority = (attrib & 0x0f00) >> 8;
	} };
	
	public static WriteHandlerPtr get_bg1_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
/*TODO*///		int color, tile_number, attrib, offset;
/*TODO*///		UINT16 *source = (UINT16 *)(bgvideoram[1]);
	
/*TODO*///		offset = ((row*64) + (col*2)) & 0x7ff;
	
/*TODO*///		attrib = source[offset];
/*TODO*///		tile_number = source[offset+1];
/*TODO*///		color = attrib & 0x7f;
/*TODO*///		SET_TILE_INFO(2,tile_number,color)
/*TODO*///		tile_info.priority = (attrib & 0x0f00) >> 8;
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	static void toaplan2_vh_stop(int controller)
	{
/*TODO*///		free(     bgvideoram[controller] );
/*TODO*///		free(     fgvideoram[controller] );
/*TODO*///		free(    topvideoram[controller] );
/*TODO*///		free(  spriteram_now[controller] );
/*TODO*///		free( spriteram_next[controller] );
/*TODO*///		free(  spriteram_new[controller] );
	}
	public static VhStopPtr toaplan2_0_vh_stop = new VhStopPtr() { public void handler() 
	{
		toaplan2_vh_stop(0);
	} };
	public static VhStopPtr toaplan2_1_vh_stop = new VhStopPtr() { public void handler() 
	{
		toaplan2_vh_stop(1);
		toaplan2_vh_stop(0);
	} };
	
	
	static int create_tilemaps_0()
	{
/*TODO*///		top_tilemap[0] = tilemap_create(
/*TODO*///			get_top0_tile_info,
/*TODO*///			TILEMAP_TRANSPARENT,
/*TODO*///			16,16,
/*TODO*///			32,32
/*TODO*///		);
	
/*TODO*///		fg_tilemap[0] = tilemap_create(
/*TODO*///			get_fg0_tile_info,
/*TODO*///			TILEMAP_TRANSPARENT,
/*TODO*///			16,16,
/*TODO*///			32,32
/*TODO*///		);
	
/*TODO*///		bg_tilemap[0] = tilemap_create(
/*TODO*///			get_bg0_tile_info,
/*TODO*///			TILEMAP_TRANSPARENT,
/*TODO*///			16,16,
/*TODO*///			32,32
/*TODO*///		);
	
/*TODO*///		if (top_tilemap[0] && fg_tilemap[0] && bg_tilemap[0])
/*TODO*///		{
/*TODO*///			top_tilemap[0].transparent_pen = 0;
/*TODO*///			fg_tilemap[0].transparent_pen = 0;
/*TODO*///			bg_tilemap[0].transparent_pen = 0;
			return 0;
/*TODO*///		}
		//return 1;
	}
	static int create_tilemaps_1()
	{
/*TODO*///		top_tilemap[1] = tilemap_create(
/*TODO*///			get_top1_tile_info,
/*TODO*///			TILEMAP_TRANSPARENT,
/*TODO*///			16,16,
/*TODO*///			32,32
/*TODO*///		);
	
/*TODO*///		fg_tilemap[1] = tilemap_create(
/*TODO*///			get_fg1_tile_info,
/*TODO*///			TILEMAP_TRANSPARENT,
/*TODO*///			16,16,
/*TODO*///			32,32
/*TODO*///		);
	
/*TODO*///		bg_tilemap[1] = tilemap_create(
/*TODO*///			get_bg1_tile_info,
/*TODO*///			TILEMAP_TRANSPARENT,
/*TODO*///			16,16,
/*TODO*///			32,32
/*TODO*///		);
	
/*TODO*///		if (top_tilemap[1] && fg_tilemap[1] && bg_tilemap[1])
/*TODO*///		{
/*TODO*///			top_tilemap[1].transparent_pen = 0;
/*TODO*///			fg_tilemap[1].transparent_pen = 0;
/*TODO*///			bg_tilemap[1].transparent_pen = 0;
			return 0;
/*TODO*///		}
		//return 1;
	}
	static int error_level = 0;
	public static ReadHandlerPtr toaplan2_vh_start = new ReadHandlerPtr() { public int handler(int controller)
	{	
/*TODO*///		if ((spriteram_new[controller] = malloc(TOAPLAN2_SPRITERAM_SIZE)) == 0)
/*TODO*///		{
/*TODO*///			return 1;
/*TODO*///		}
/*TODO*///		memset(spriteram_new[controller],0,TOAPLAN2_SPRITERAM_SIZE);
	
/*TODO*///		if ((spriteram_next[controller] = malloc(TOAPLAN2_SPRITERAM_SIZE)) == 0)
/*TODO*///		{
/*TODO*///			free( spriteram_new[controller] );
/*TODO*///			return 1;
/*TODO*///		}
/*TODO*///		memset(spriteram_next[controller],0,TOAPLAN2_SPRITERAM_SIZE);
/*TODO*///	
/*TODO*///		if ((spriteram_now[controller] = malloc(TOAPLAN2_SPRITERAM_SIZE)) == 0)
/*TODO*///		{
/*TODO*///			free( spriteram_next[controller] );
/*TODO*///			free(  spriteram_new[controller] );
/*TODO*///			return 1;
/*TODO*///		}
/*TODO*///		memset(spriteram_now[controller],0,TOAPLAN2_SPRITERAM_SIZE);
/*TODO*///	
/*TODO*///		if ((topvideoram[controller] = malloc(TOAPLAN2_TOP_VRAM_SIZE)) == 0)
/*TODO*///		{
/*TODO*///			free(  spriteram_now[controller] );
/*TODO*///			free( spriteram_next[controller] );
/*TODO*///			free(  spriteram_new[controller] );
/*TODO*///			return 1;
/*TODO*///		}
/*TODO*///		memset(topvideoram[controller],0,TOAPLAN2_TOP_VRAM_SIZE);
	
/*TODO*///		if ((fgvideoram[controller] = malloc(TOAPLAN2_FG_VRAM_SIZE)) == 0)
/*TODO*///		{
/*TODO*///			free(    topvideoram[controller] );
/*TODO*///			free(  spriteram_now[controller] );
/*TODO*///			free( spriteram_next[controller] );
/*TODO*///			free(  spriteram_new[controller] );
/*TODO*///			return 1;
/*TODO*///		}
/*TODO*///		memset(fgvideoram[controller],0,TOAPLAN2_FG_VRAM_SIZE);
	
/*TODO*///		if ((bgvideoram[controller] = malloc(TOAPLAN2_BG_VRAM_SIZE)) == 0)
/*TODO*///		{
/*TODO*///			free(     fgvideoram[controller] );
/*TODO*///			free(    topvideoram[controller] );
/*TODO*///			free(  spriteram_now[controller] );
/*TODO*///			free( spriteram_next[controller] );
/*TODO*///			free(  spriteram_new[controller] );
/*TODO*///			return 1;
/*TODO*///		}
/*TODO*///		memset(bgvideoram[controller],0,TOAPLAN2_BG_VRAM_SIZE);
	
		if (controller == 0)
		{
			error_level |= create_tilemaps_0();
		}
		if (controller == 1)
		{
			error_level |= create_tilemaps_1();
		}
		return error_level;
	} };
	public static VhStartPtr toaplan2_0_vh_start = new VhStartPtr() { public int handler() 
	{
		return toaplan2_vh_start.handler(0);
	} };
	public static VhStartPtr toaplan2_1_vh_start = new VhStartPtr() { public int handler() 
	{
		int error_level = 0;
		error_level |= toaplan2_vh_start.handler(0);
		error_level |= toaplan2_vh_start.handler(1);
		return error_level;
	} };
	
	
	
	/***************************************************************************
	
	  Video I/O port hardware.
	
	***************************************************************************/
	
	static void toaplan2_voffs_w(int offset, int data, int controller)
	{
		toaplan2_voffs[controller] = data;
	
		/* Layers are seperated by ranges in the offset */
		switch (data & 0xfc00)
		{
			case 0x0400:
			case 0x0000:	bg_offs[controller] = (data & 0x7ff) * 2; break;
			case 0x0c00:
			case 0x0800:	fg_offs[controller] = (data & 0x7ff) * 2; break;
			case 0x1400:
			case 0x1000:	top_offs[controller] = (data & 0x7ff) * 2; break;
			case 0x1800:	sprite_offs[controller] = (data & 0x3ff) * 2; break;
			default:		if (errorlog != null)
								fprintf(errorlog,"Hmmm, unknown video controller %01x layer being selected (%08x)\n",controller,data);
							data &= 0x1800;
							if ((data & 0x1800) == 0x0000)
								bg_offs[controller] = (data & 0x7ff) * 2;
							if ((data & 0x1800) == 0x0800)
								fg_offs[controller] = (data & 0x7ff) * 2;
							if ((data & 0x1800) == 0x1000)
								top_offs[controller] = (data & 0x7ff) * 2;
							if ((data & 0x1800) == 0x1800)
								sprite_offs[controller] = (data & 0x3ff) * 2;
							break;
		}
	}
	public static WriteHandlerPtr toaplan2_0_voffs_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		toaplan2_voffs_w(offset, data, 0);
	} };
	public static WriteHandlerPtr toaplan2_1_voffs_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		toaplan2_voffs_w(offset, data, 1);
	} };
	static int video_data = 0;
	static int toaplan2_videoram_r(int offset, int controller)
	{
		
		int videoram_offset;
	
		switch (toaplan2_voffs[controller] & 0xfc00)
		{
			case 0x0400:
			case 0x0000:
					videoram_offset = bg_offs[controller] & (TOAPLAN2_BG_VRAM_SIZE-1);
/*TODO*///					video_data = READ_WORD (&bgvideoram[controller][videoram_offset]);
					bg_offs[controller] += 2;
					if (bg_offs[controller] > TOAPLAN2_BG_VRAM_SIZE)
					{
						if (errorlog != null)
							fprintf(errorlog,"Reading %04x from out of range BG Layer address (%08x)  Video controller %01x  !!!\n",video_data,bg_offs[controller],controller);
					}
					break;
			case 0x0c00:
			case 0x0800:
					videoram_offset = fg_offs[controller] & (TOAPLAN2_FG_VRAM_SIZE-1);
/*TODO*///					video_data = READ_WORD (&fgvideoram[controller][videoram_offset]);
					fg_offs[controller] += 2;
					if (fg_offs[controller] > TOAPLAN2_FG_VRAM_SIZE)
					{
						if (errorlog != null)
							fprintf(errorlog,"Reading %04x from out of range FG Layer address (%08x)  Video controller %01x  !!!\n",video_data,fg_offs[controller],controller);
					}
					break;
			case 0x1400:
			case 0x1000:
					videoram_offset = top_offs[controller] & (TOAPLAN2_TOP_VRAM_SIZE-1);
/*TODO*///					video_data = READ_WORD (&topvideoram[controller][videoram_offset]);
					top_offs[controller] += 2;
					if (top_offs[controller] > TOAPLAN2_TOP_VRAM_SIZE)
					{
						if (errorlog != null)
							fprintf(errorlog,"Reading %04x from out of range TOP Layer address (%08x)  Video controller %01x  !!!\n",video_data,top_offs[controller],controller);
					}
					break;
			case 0x1800:
					videoram_offset = sprite_offs[controller] & (TOAPLAN2_SPRITERAM_SIZE-1);
/*TODO*///					video_data = READ_WORD (&spriteram_new[controller][videoram_offset]);
					sprite_offs[controller] += 2;
					if (sprite_offs[controller] > TOAPLAN2_SPRITERAM_SIZE)
					{
						if (errorlog != null)
							fprintf(errorlog,"Reading %04x from out of range Sprite address (%08x)  Video controller %01x  !!!\n",video_data,sprite_offs[controller],controller);
					}
					break;
			default:
					video_data = toaplan2_unk_vram;
					if (errorlog != null)
						fprintf(errorlog,"Hmmm, reading %04x from unknown video layer (%08x)  Video controller %01x  !!!\n",video_data,toaplan2_voffs[controller],controller);
					break;
		}
		return video_data;
	}
	public static ReadHandlerPtr toaplan2_0_videoram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return toaplan2_videoram_r(offset, 0);
	} };
	public static ReadHandlerPtr toaplan2_1_videoram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return toaplan2_videoram_r(offset, 1);
	} };
	
	public static void toaplan2_videoram_w(int offset, int data, int controller)
	{
		int oldword = 0;
		int videoram_offset;
		int dirty_cell;
	
		switch (toaplan2_voffs[controller] & 0xfc00)
		{
			case 0x0400:
			case 0x0000:
					videoram_offset = bg_offs[controller] & (TOAPLAN2_BG_VRAM_SIZE-1);
/*TODO*///					oldword = READ_WORD (&bgvideoram[controller][videoram_offset]);
					if (data != oldword)
					{
/*TODO*///						WRITE_WORD (&bgvideoram[controller][videoram_offset],data);
						dirty_cell = (bg_offs[controller] & (TOAPLAN2_BG_VRAM_SIZE-3))/2;
/*TODO*///						tilemap_mark_tile_dirty(bg_tilemap[controller], (dirty_cell%64)/2, dirty_cell/64);
					}
					bg_offs[controller] += 2;
					if (bg_offs[controller] > TOAPLAN2_BG_VRAM_SIZE)
					{
						if (errorlog != null)
							fprintf(errorlog,"Writing %04x to out of range BG Layer address (%08x)  Video controller %01x  !!!\n",data,bg_offs[controller],controller);
					}
					break;
			case 0x0c00:
			case 0x0800:
					videoram_offset = fg_offs[controller] & (TOAPLAN2_FG_VRAM_SIZE-1);
/*TODO*///					oldword = READ_WORD (&fgvideoram[controller][videoram_offset]);
					if (data != oldword)
					{
/*TODO*///						WRITE_WORD (&fgvideoram[controller][videoram_offset],data);
						dirty_cell = (fg_offs[controller] & (TOAPLAN2_FG_VRAM_SIZE-3))/2;
/*TODO*///					tilemap_mark_tile_dirty(fg_tilemap[controller], (dirty_cell%64)/2, dirty_cell/64);
					}
					fg_offs[controller] += 2;
					if (fg_offs[controller] > TOAPLAN2_FG_VRAM_SIZE)
					{
						if (errorlog != null)
							fprintf(errorlog,"Writing %04x to out of range FG Layer address (%08x)  Video controller %01x  !!!\n",data,fg_offs[controller],controller);
					}
					break;
			case 0x1400:
			case 0x1000:
					videoram_offset = top_offs[controller] & (TOAPLAN2_TOP_VRAM_SIZE-1);
/*TODO*///					oldword = READ_WORD (&topvideoram[controller][videoram_offset]);
					if (data != oldword)
					{
/*TODO*///						WRITE_WORD (&topvideoram[controller][videoram_offset],data);
						dirty_cell = (top_offs[controller] & (TOAPLAN2_TOP_VRAM_SIZE-3))/2;
/*TODO*///						tilemap_mark_tile_dirty(top_tilemap[controller], (dirty_cell%64)/2, dirty_cell/64);
					}
					top_offs[controller] += 2;
					if (top_offs[controller] > TOAPLAN2_TOP_VRAM_SIZE)
					{
						if (errorlog != null)
							fprintf(errorlog,"Writing %04x to out of range TOP Layer address (%08x)  Video controller %01x  !!!\n",data,top_offs[controller],controller);
					}
					break;
			case 0x1800:
					videoram_offset = sprite_offs[controller] & (TOAPLAN2_SPRITERAM_SIZE-1);
/*TODO*///					WRITE_WORD (&spriteram_new[controller][videoram_offset],data);
					sprite_offs[controller] += 2;
					if (sprite_offs[controller] > TOAPLAN2_SPRITERAM_SIZE)
					{
						if (errorlog != null)
							fprintf(errorlog,"Writing %04x to out of range Sprite address (%08x)  Video controller %01x  !!!\n",data,sprite_offs[controller],controller);
					}
					break;
			default:
					toaplan2_unk_vram = data;
					if (errorlog != null)
						fprintf(errorlog,"Hmmm, writing %04x to unknown video layer (%08x)  Video controller %01x  \n",toaplan2_unk_vram,toaplan2_voffs[controller],controller);
					break;
		}
	}
	public static WriteHandlerPtr toaplan2_0_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		toaplan2_videoram_w(offset, data, 0);
	} };
	public static WriteHandlerPtr toaplan2_1_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		toaplan2_videoram_w(offset, data, 1);
	} };
	
	
	public static void toaplan2_scroll_reg_select_w(int offset, int data, int controller)
	{
		toaplan2_scroll_reg[controller] = data;
		if ((toaplan2_scroll_reg[controller] & 0xffffff70)!=0)
		{
			if (errorlog != null) fprintf(errorlog,"Hmmm, unknown video control register selected (%08x)  Video controller %01x  \n",toaplan2_scroll_reg[controller],controller);
		}
	}
	public static WriteHandlerPtr toaplan2_0_scroll_reg_select_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		toaplan2_scroll_reg_select_w(offset, data, 0);
	} };
	public static WriteHandlerPtr toaplan2_1_scroll_reg_select_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		toaplan2_scroll_reg_select_w(offset, data, 1);
	} };
	
	
	static void toaplan2_scroll_reg_data_w(int offset, int data, int controller)
	{
		/************************************************************************/
		/***** X and Y layer flips can be set independantly, so emulate it ******/
		/************************************************************************/
	
	//	int vid_controllers = 1;
	
		switch(toaplan2_scroll_reg[controller])
		{
			case 0x00:	bg_scrollx[controller] = data - 0x1d6;			/* 1D6h */
/*TODO*///						bg_flip[controller] &= (~TILEMAP_FLIPX);
/*TODO*///						tilemap_set_flip(bg_tilemap[controller],bg_flip[controller]);
/*TODO*///						tilemap_set_scrollx(bg_tilemap[controller],0,bg_scrollx[controller]);
						break;
			case 0x01:	bg_scrolly[controller] = data - 0x1ef;			/* 1EFh */
/*TODO*///						bg_flip[controller] &= (~TILEMAP_FLIPY);
/*TODO*///						tilemap_set_flip(bg_tilemap[controller],bg_flip[controller]);
/*TODO*///						tilemap_set_scrolly(bg_tilemap[controller],0,bg_scrolly[controller]);
						break;
			case 0x02:	fg_scrollx[controller] = data - 0x1d8;			/* 1D0h */
/*TODO*///						fg_flip[controller] &= (~TILEMAP_FLIPX);
/*TODO*///						tilemap_set_flip(fg_tilemap[controller],fg_flip[controller]);
/*TODO*///						tilemap_set_scrollx(fg_tilemap[controller],0,fg_scrollx[controller]);
						break;
			case 0x03:  fg_scrolly[controller] = data - 0x1ef;			/* 1EFh */
/*TODO*///						fg_flip[controller] &= (~TILEMAP_FLIPY);
/*TODO*///						tilemap_set_flip(fg_tilemap[controller],fg_flip[controller]);
/*TODO*///						tilemap_set_scrolly(fg_tilemap[controller],0,fg_scrolly[controller]);
						break;
			case 0x04:	top_scrollx[controller] = data - 0x1da;			/* 1DAh */
/*TODO*///						top_flip[controller] &= (~TILEMAP_FLIPX);
/*TODO*///						tilemap_set_flip(top_tilemap[controller],top_flip[controller]);
/*TODO*///						tilemap_set_scrollx(top_tilemap[controller],0,top_scrollx[controller]);
						break;
			case 0x05:	top_scrolly[controller] = data - 0x1ef;			/* 1EFh */
/*TODO*///						top_flip[controller] &= (~TILEMAP_FLIPY);
/*TODO*///						tilemap_set_flip(top_tilemap[controller],top_flip[controller]);
/*TODO*///						tilemap_set_scrolly(top_tilemap[controller],0,top_scrolly[controller]);
						break;
			case 0x06:	sprite_scrollx[controller] = data - 0x1cc;		/* 1D4h */
/*TODO*///						if (sprite_scrollx[controller] & 0x80000000) sprite_scrollx[controller] |= 0xfffffe00;
/*TODO*///						else sprite_scrollx[controller] &= 0x1ff;
/*TODO*///						sprite_flip[controller] &= (~TOAPLAN2_SPRITE_FLIPX);
						break;
			case 0x07:	sprite_scrolly[controller] = data - 0x1ef;		/* 1F7h */
/*TODO*///						if (sprite_scrolly[controller] & 0x80000000) sprite_scrolly[controller] |= 0xfffffe00;
/*TODO*///						else sprite_scrolly[controller] &= 0x1ff;
/*TODO*///						sprite_flip[controller] &= (~TOAPLAN2_SPRITE_FLIPY);
						break;
			case 0x0f:	break;
			case 0x80:  bg_scrollx[controller] = data - 0x229;			/* 169h */
/*TODO*///						bg_flip[controller] |= TILEMAP_FLIPX;
/*TODO*///						tilemap_set_flip(bg_tilemap[controller],bg_flip[controller]);
/*TODO*///						tilemap_set_scrollx(bg_tilemap[controller],0,bg_scrollx[controller]);
						break;
			case 0x81:	bg_scrolly[controller] = data - 0x210;			/* 100h */
/*TODO*///						bg_flip[controller] |= TILEMAP_FLIPY;
/*TODO*///						tilemap_set_flip(bg_tilemap[controller],bg_flip[controller]);
/*TODO*///						tilemap_set_scrolly(bg_tilemap[controller],0,bg_scrolly[controller]);
						break;
			case 0x82:	fg_scrollx[controller] = data - 0x227;			/* 15Fh */
/*TODO*///						fg_flip[controller] |= TILEMAP_FLIPX;
/*TODO*///						tilemap_set_flip(fg_tilemap[controller],fg_flip[controller]);
/*TODO*///						tilemap_set_scrollx(fg_tilemap[controller],0,fg_scrollx[controller]);
						break;
			case 0x83:	fg_scrolly[controller] = data - 0x210;			/* 100h */
/*TODO*///						fg_flip[controller] |= TILEMAP_FLIPY;
/*TODO*///						tilemap_set_flip(fg_tilemap[controller],fg_flip[controller]);
/*TODO*///						tilemap_set_scrolly(fg_tilemap[controller],0,fg_scrolly[controller]);
						break;
			case 0x84:	top_scrollx[controller] = data - 0x225;			/* 165h */
/*TODO*///						top_flip[controller] |= TILEMAP_FLIPX;
/*TODO*///						tilemap_set_flip(top_tilemap[controller],top_flip[controller]);
/*TODO*///						tilemap_set_scrollx(top_tilemap[controller],0,top_scrollx[controller]);
						break;
			case 0x85:	top_scrolly[controller] = data - 0x210;			/* 100h */
/*TODO*///						top_flip[controller] |= TILEMAP_FLIPY;
/*TODO*///						tilemap_set_flip(top_tilemap[controller],top_flip[controller]);
/*TODO*///						tilemap_set_scrolly(top_tilemap[controller],0,top_scrolly[controller]);
						break;
			case 0x86:	sprite_scrollx[controller] = data - 0x17b;		/* 17Bh */
						if ((sprite_scrollx[controller] & 0x80000000)!=0) sprite_scrollx[controller] |= 0xfffffe00;
						else sprite_scrollx[controller] &= 0x1ff;
						sprite_flip[controller] |= TOAPLAN2_SPRITE_FLIPX;
						break;
			case 0x87:	sprite_scrolly[controller] = data - 0x108;		/* 108h */
						if ((sprite_scrolly[controller] & 0x80000000)!=0) sprite_scrolly[controller] |= 0xfffffe00;
						else sprite_scrolly[controller] &= 0x1ff;
						sprite_flip[controller] |= TOAPLAN2_SPRITE_FLIPY;
						break;
			case 0x8f:	break;
	
			case 0x0e:	/******* Initialise video controller register ? *******/
						if ((toaplan2_sub_cpu == CPU_2_Z80) && (data == 3))
						{
							/* HACK! When tilted, sound CPU needs to be reset. */
							cpu_set_reset_line(1,PULSE_LINE);
							YM3812_sh_reset();
						}
			default:	if (errorlog != null)
							fprintf(errorlog,"Hmmm, writing %08x to unknown video control register (%08x)  Video controller %01x  !!!\n",data ,toaplan2_scroll_reg[controller],controller);
						break;
		}
	
	
	}
	public static WriteHandlerPtr toaplan2_0_scroll_reg_data_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		toaplan2_scroll_reg_data_w(offset, data, 0);
	} };
	public static WriteHandlerPtr toaplan2_1_scroll_reg_data_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		toaplan2_scroll_reg_data_w(offset, data, 1);
	} };
	
	
	
	
	/***************************************************************************
		Sprite Handlers
	***************************************************************************/
	
	static void mark_sprite_colors(int controller)
	{
		int offs, attrib, sprite, color, i, pal_base;
		int sprite_sizex, sprite_sizey, temp_x, temp_y;
		int[] colmask=new int[64];
	
/*TODO*///		UINT16 *source = (UINT16 *)(spriteram_now[controller]);
	
		pal_base = Machine.drv.gfxdecodeinfo[ ((controller*2)+1) ].color_codes_start;
	
		for(i=0; i < 64; i++) colmask[i] = 0;
	
		for (offs = 0; offs < (TOAPLAN2_SPRITERAM_SIZE/2); offs += 4)
		{
/*TODO*///			attrib = source[offs];
/*TODO*///			sprite = source[offs + 1] | ((attrib & 3) << 16);
/*TODO*///			sprite %= Machine.gfx[ ((controller*2)+1) ].total_elements;
/*TODO*///			if ((attrib & 0x8000) != 0)
/*TODO*///			{
				/* While we're here, mark all priorities used */
/*TODO*///				sprite_priority[controller][((attrib & 0x0f00) >> 8)] = display_sp[controller];
	
/*TODO*///				color = (attrib >> 2) & 0x3f;
/*TODO*///				sprite_sizex = (source[offs + 2] & 0x0f) + 1;
/*TODO*///				sprite_sizey = (source[offs + 3] & 0x0f) + 1;
	
/*TODO*///				for (temp_y = 0; temp_y < sprite_sizey; temp_y++)
/*TODO*///				{
/*TODO*///					for (temp_x = 0; temp_x < sprite_sizex; temp_x++)
/*TODO*///					{
/*TODO*///						colmask[color] |= Machine.gfx[ ((controller*2)+1) ].pen_usage[sprite];
/*TODO*///						sprite++ ;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
		}
	
		for (color = 0;color < 64;color++)
		{
			if ((color == 0) && (colmask[0] & 1)!=0)
				palette_used_colors.write(pal_base + 16 * color, PALETTE_COLOR_TRANSPARENT);
			for (i = 1; i < 16; i++)
			{
				if ((colmask[color] & (1 << i))!=0)
					palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
			}
		}
	}
	
	
	
	static void draw_sprites( osd_bitmap bitmap, int controller, int priority_to_display )
	{
		GfxElement gfx = Machine.gfx[ ((controller*2)+1) ];
		rectangle clip = Machine.drv.visible_area;
	
/*TODO*///		UINT16 *source = (UINT16 *)(spriteram_now[controller]);
	
		int offs;
		for (offs = 0; offs < (TOAPLAN2_SPRITERAM_SIZE/2); offs += 4)
		{
			int attrib, sprite, color, priority, flipx, flipy, sx, sy;
			int sprite_sizex, sprite_sizey, temp_x, temp_y, sx_base, sy_base;
	
/*TODO*///			attrib = source[offs];
/*TODO*///			priority = (attrib & 0x0f00) >> 8;
	
/*TODO*///			if ((priority == priority_to_display) && (attrib & 0x8000))
/*TODO*///			{
/*TODO*///				sprite = ((attrib & 3) << 16) | source[offs + 1] ;	/* 18 bit */
/*TODO*///				color = (attrib >> 2) & 0x3f;
	
				/****** find out sprite size ******/
/*TODO*///				sprite_sizex = ((source[offs + 2] & 0x0f) + 1) * 8;
/*TODO*///				sprite_sizey = ((source[offs + 3] & 0x0f) + 1) * 8;
	
				/****** find position to display sprite ******/
/*TODO*///				sx_base = (source[offs + 2] >> 7) - sprite_scrollx[controller];
/*TODO*///				sy_base = (source[offs + 3] >> 7) - sprite_scrolly[controller];
	
/*TODO*///				flipx = attrib & TOAPLAN2_SPRITE_FLIPX;
/*TODO*///				flipy = attrib & TOAPLAN2_SPRITE_FLIPY;
	
/*TODO*///				if (flipx != 0)
/*TODO*///				{
/*TODO*///					sx_base -= 7;
					/****** wrap around sprite position ******/
/*TODO*///					if (sx_base >= 0x1c0) sx_base -= 0x200;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if (sx_base >= 0x180) sx_base -= 0x200;
/*TODO*///				}
	
/*TODO*///				if (flipy != 0)
/*TODO*///				{
/*TODO*///					sy_base -= 7;
/*TODO*///					if (sy_base >= 0x1c0) sy_base -= 0x200;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if (sy_base >= 0x180) sy_base -= 0x200;
/*TODO*///				}
	
				/****** flip the sprite layer in any active X or Y flip ******/
/*TODO*///				if (sprite_flip[controller])
/*TODO*///				{
/*TODO*///					if (sprite_flip[controller] & TOAPLAN2_SPRITE_FLIPX)
/*TODO*///						sx_base = 320 - sx_base;
/*TODO*///					if (sprite_flip[controller] & TOAPLAN2_SPRITE_FLIPY)
/*TODO*///						sy_base = 240 - sy_base;
/*TODO*///				}
	
				/****** cancel flip, if it and sprite layer flip are active ******/
/*TODO*///				flipx = (flipx ^ (sprite_flip[controller] & TOAPLAN2_SPRITE_FLIPX));
/*TODO*///				flipy = (flipy ^ (sprite_flip[controller] & TOAPLAN2_SPRITE_FLIPY));
	
/*TODO*///				for (temp_y = 0; temp_y < sprite_sizey; temp_y += 8)
/*TODO*///				{
/*TODO*///					if (flipy != 0) sy = sy_base - temp_y;
	/*TODO*///				else       sy = sy_base + temp_y;
/*TODO*///					for (temp_x = 0; temp_x < sprite_sizex; temp_x += 8)
/*TODO*///					{
/*TODO*///						if (flipx != 0) sx = sx_base - temp_x;
/*TODO*///						else       sx = sx_base + temp_x;
/*TODO*///	
/*TODO*///						drawgfx(bitmap,gfx,sprite,
/*TODO*///							color,
/*TODO*///							flipx,flipy,
/*TODO*///							sx,sy,
/*TODO*///							clip,TRANSPARENCY_PEN,0);
/*TODO*///	
/*TODO*///						sprite++ ;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
		}
	}
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	
	***************************************************************************/
	
	public static VhUpdatePtr toaplan2_0_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int priority;
	
		for (priority = 0; priority < 16; priority++)
			sprite_priority[0][priority] = 0;		/* Clear priorities used list */
	

	
/*TODO*///		tilemap_update(ALL_TILEMAPS);
	
		palette_init_used_colors();
		mark_sprite_colors(0);	/* Also mark priorities used */
	
/*TODO*///		if (palette_recalc()) tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
/*TODO*///		tilemap_render(ALL_TILEMAPS);
	
		fillbitmap(bitmap,palette_transparent_pen,Machine.drv.visible_area);
	
		for (priority = 0; priority < 16; priority++)
		{
/*TODO*///			tilemap_draw(bitmap,bg_tilemap[0],priority);
/*TODO*///			tilemap_draw(bitmap,fg_tilemap[0],priority);
/*TODO*///			tilemap_draw(bitmap,top_tilemap[0],priority);
/*TODO*///			if (sprite_priority[0][priority])
/*TODO*///				draw_sprites(bitmap,0,priority);
		}
	} };
	public static VhUpdatePtr toaplan2_1_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int priority;
	
		for (priority = 0; priority < 16; priority++)
		{
			sprite_priority[0][priority] = 0;		/* Clear priorities used list */
			sprite_priority[1][priority] = 0;		/* Clear priorities used list */
		}
	

	
/*TODO*///		tilemap_update(ALL_TILEMAPS);
	
		palette_init_used_colors();
		mark_sprite_colors(0);	/* Also mark priorities used */
		mark_sprite_colors(1);	/* Also mark priorities used */
	
/*TODO*///		if (palette_recalc()!=null) tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
/*TODO*///		tilemap_render(ALL_TILEMAPS);
	
		fillbitmap(bitmap,palette_transparent_pen,Machine.drv.visible_area);
	
		for (priority = 0; priority < 16; priority++)
		{
/*TODO*///			tilemap_draw(bitmap,bg_tilemap[1],priority);
/*TODO*///			tilemap_draw(bitmap,fg_tilemap[1],priority);
/*TODO*///			tilemap_draw(bitmap,top_tilemap[1],priority);
/*TODO*///			if (sprite_priority[1][priority])
/*TODO*///				draw_sprites(bitmap,1,priority);
		}
		for (priority = 0; priority < 16; priority++)
		{
/*TODO*///			tilemap_draw(bitmap,bg_tilemap[0],priority);
/*TODO*///			tilemap_draw(bitmap,fg_tilemap[0],priority);
/*TODO*///			tilemap_draw(bitmap,top_tilemap[0],priority);
/*TODO*///			if (sprite_priority[0][priority])
/*TODO*///				draw_sprites(bitmap,0,priority);
		}
	} };
	public static VhUpdatePtr batsugun_1_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int priority;
	
		for (priority = 0; priority < 16; priority++)
		{
			sprite_priority[0][priority] = 0;		/* Clear priorities used list */
			sprite_priority[1][priority] = 0;		/* Clear priorities used list */
		}

/*TODO*///		tilemap_update(ALL_TILEMAPS);
	
		palette_init_used_colors();
		mark_sprite_colors(0);	/* Also mark priorities used */
		mark_sprite_colors(1);	/* Also mark priorities used */
	
/*TODO*///		if (palette_recalc()!=null) tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
/*TODO*///		tilemap_render(ALL_TILEMAPS);
	
		fillbitmap(bitmap,palette_transparent_pen,Machine.drv.visible_area);
	
		for (priority = 0; priority < 16; priority++)
		{
/*TODO*///			tilemap_draw(bitmap,bg_tilemap[1],priority);
/*TODO*///			tilemap_draw(bitmap,bg_tilemap[0],priority);
/*TODO*///			tilemap_draw(bitmap,fg_tilemap[1],priority);
/*TODO*///			tilemap_draw(bitmap,top_tilemap[1],priority);
/*TODO*///			if (sprite_priority[1][priority])
/*TODO*///				draw_sprites(bitmap,1,priority);
		}
		for (priority = 0; priority < 16; priority++)
		{
/*TODO*///			tilemap_draw(bitmap,fg_tilemap[0],priority);
/*TODO*///			tilemap_draw(bitmap,top_tilemap[0],priority);
/*TODO*///			if (sprite_priority[0][priority])
/*TODO*///				draw_sprites(bitmap,0,priority);
		}
	} };
	
	public static VhEofCallbackPtr toaplan2_0_eof_callback = new VhEofCallbackPtr() {
        public void handler() { 
		/** Shift sprite RAM buffers  ***  Used to fix sprite lag **/
/*TODO*///		memcpy(spriteram_now[0],spriteram_next[0],TOAPLAN2_SPRITERAM_SIZE);
/*TODO*///		memcpy(spriteram_next[0],spriteram_new[0],TOAPLAN2_SPRITERAM_SIZE);
	}};
        public static VhEofCallbackPtr toaplan2_1_eof_callback = new VhEofCallbackPtr() {
        public void handler() {
		/** Shift sprite RAM buffers  ***  Used to fix sprite lag **/
/*TODO*///		memcpy(spriteram_now[0],spriteram_next[0],TOAPLAN2_SPRITERAM_SIZE);
/*TODO*///		memcpy(spriteram_next[0],spriteram_new[0],TOAPLAN2_SPRITERAM_SIZE);
/*TODO*///		memcpy(spriteram_now[1],spriteram_next[1],TOAPLAN2_SPRITERAM_SIZE);
/*TODO*///		memcpy(spriteram_next[1],spriteram_new[1],TOAPLAN2_SPRITERAM_SIZE);
	}};
	
}
