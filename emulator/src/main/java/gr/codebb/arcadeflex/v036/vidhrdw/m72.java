/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.vidhrdw;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static common.libc.cstring.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static arcadeflex.v036.sndhrdw.m72.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;


public class m72
{
	
	
	
	public static UBytePtr m72_videoram1=new UBytePtr();
        public static UBytePtr m72_videoram2=new UBytePtr();
        public static UBytePtr majtitle_rowscrollram=new UBytePtr();
	public static UBytePtr m72_spriteram;
	static int rastersplit;
	static int splitline;
	static tilemap fg_tilemap,bg_tilemap;
	static int xadjust;
	static int[] scrollx1=new int[256];
        static int[] scrolly1=new int[256];
        static int[] scrollx2=new int[256];
        static int[] scrolly2=new int[256];
	
	static int irq1,irq2;
	
        public static InitMachinePtr m72_init_machine = new InitMachinePtr() { public void handler() 
	{
		int i;
	
		irq1 = 0x20;
		irq2 = 0x22;
	
		/* R-Type II doesn't clear the scroll registers on reset, so we have to do it ourselves */
		for (i = 0;i < 256;i++)
			scrollx1[i] = scrolly1[i] = 0;
	
		m72_init_sound.handler();
	}};
	public static InitMachinePtr xmultipl_init_machine = new InitMachinePtr() { public void handler() 
	{
		irq1 = 0x08;
		irq2 = 0x0a;
		m72_init_sound.handler();
	}};
	public static InterruptPtr m72_interrupt = new InterruptPtr() {
            public int handler() {

		int line = 255 - cpu_getiloops();
	
		if (line == 255)	/* vblank */
		{
			rastersplit = 0;
			interrupt_vector_w.handler(0,irq1);
			return interrupt.handler();
		}
		else
		{
			if (line != splitline - 128)
				return ignore_interrupt.handler();
	
			rastersplit = line + 1;
	
			/* this is used to do a raster effect and show the score display at
			   the bottom of the screen or other things. The line where the
			   interrupt happens is programmable (and the interrupt can be triggered
			   multiple times, be changing the interrupt line register in the
			   interrupt handler).
			 */
			interrupt_vector_w.handler(0,irq2);
			return interrupt.handler();
		}
	}};
	
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static WriteHandlerPtr m72_get_bg_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int tile_index = 4*(64*row+col);
		/*unsigned*/ char attr = m72_videoram2.read(tile_index+1);
		SET_TILE_INFO(2,m72_videoram2.read(tile_index) + ((attr & 0x3f) << 8),m72_videoram2.read(tile_index+2) & 0x0f);
		tile_info.flags = (char)(TILE_FLIPYX((attr & 0xc0) >> 6));
	} };
	
	public static WriteHandlerPtr m72_get_fg_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int tile_index = 4*(64*row+col);
		/*unsigned*/ char attr = m72_videoram1.read(tile_index+1);
		SET_TILE_INFO(1,m72_videoram1.read(tile_index) + ((attr & 0x3f) << 8),m72_videoram1.read(tile_index+2) & 0x0f);
	/* bchopper: (videoram[tile_index+2] & 0x10) is used, priority? */
		tile_info.flags = (char)TILE_FLIPYX((attr & 0xc0) >> 6);
	
		tile_info.priority = (char)((m72_videoram1.read(tile_index+2) & 0x80) >> 7);
	} };
	
	public static WriteHandlerPtr dbreed_get_bg_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int tile_index = 4*(64*row+col);
		/*unsigned*/ char attr = m72_videoram2.read(tile_index+1);
		SET_TILE_INFO(2,m72_videoram2.read(tile_index) + ((attr & 0x3f) << 8),m72_videoram2.read(tile_index+2) & 0x0f);
		tile_info.flags = (char)(TILE_FLIPYX((attr & 0xc0) >> 6));
	
		/* this seems to apply only to Dragon Breed, it breaks R-Type and Gallop */
		tile_info.priority = (char)((m72_videoram2.read(tile_index+2) & 0x80) >> 7);
	} };
	
	public static WriteHandlerPtr rtype2_get_bg_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int tile_index = 4*(64*row+col);
		/*unsigned*/ char attr = m72_videoram2.read(tile_index+2);
		SET_TILE_INFO(1,m72_videoram2.read(tile_index) + (m72_videoram2.read(tile_index+1) << 8),attr & 0x0f);
		tile_info.flags = (char)(TILE_FLIPYX((attr & 0x60) >> 5));
	} };
	
	public static WriteHandlerPtr rtype2_get_fg_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int tile_index = 4*(64*row+col);
		/*unsigned*/ char attr = m72_videoram1.read(tile_index+2);
		SET_TILE_INFO(1,m72_videoram1.read(tile_index) + (m72_videoram1.read(tile_index+1) << 8),attr & 0x0f);
		tile_info.flags = (char)TILE_FLIPYX((attr & 0x60) >> 5);
	
		tile_info.priority = (char)(m72_videoram1.read(tile_index+3) & 0x01);
	
	/* TODO: this is used on the continue screen by rtype2. Maybe it selects split tilemap */
	/* like in M92 (top 8 pens appear over sprites), however if it is only used in that */
	/* place there's no need to support it, it's just a black screen... */
		tile_info.priority |= (m72_videoram1.read(tile_index+2) & 0x80) >> 7;
	
	/* (videoram[tile_index+2] & 0x10) is used by majtitle on the green, but it's not clear for what */
	/* (videoram[tile_index+3] & 0xfe) are used as well */
	} };
	
	public static WriteHandlerPtr majtitle_get_bg_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int tile_index = 4*(256*row+col);
		/*unsigned*/ char attr = m72_videoram2.read(tile_index+2);
		SET_TILE_INFO(1,m72_videoram2.read(tile_index) + (m72_videoram2.read(tile_index+1) << 8),attr & 0x0f);
		tile_info.flags = (char)(TILE_FLIPYX((attr & 0x60) >> 5));
	/* (videoram[tile_index+2] & 0x10) is used, but it's not clear for what (priority?) */
	} };
	
	static void hharry_get_tile_info(int gfxnum,UBytePtr videoram,int col,int row)
	{
		int tile_index = 4*(64*row+col);
		/*unsigned*/ char attr = videoram.read(tile_index+1);
		SET_TILE_INFO(gfxnum,videoram.read(tile_index) + ((attr & 0x3f) << 8),videoram.read(tile_index+2) & 0x0f);
		tile_info.flags = (char)(TILE_FLIPYX((attr & 0xc0) >> 6));
	/* (videoram[tile_index+2] & 0x10) is used, but it's not clear for what (priority?) */
	}
	
	public static WriteHandlerPtr hharry_get_bg_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		hharry_get_tile_info(1,m72_videoram2,col,row);
	} };
	
	public static WriteHandlerPtr hharry_get_fg_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		hharry_get_tile_info(1,m72_videoram1,col,row);
	} };
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr m72_vh_start = new VhStartPtr() { public int handler() 
	{
		int i;
	
	
		bg_tilemap = tilemap_create(m72_get_bg_tile_info,TILEMAP_OPAQUE,     8,8,64,64);
		fg_tilemap = tilemap_create(m72_get_fg_tile_info,TILEMAP_TRANSPARENT,8,8,64,64);
	
		m72_spriteram = new UBytePtr(spriteram_size[0]);
	
		if (fg_tilemap==null || bg_tilemap==null || m72_spriteram==null)
			return 1;
	
		fg_tilemap.transparent_pen = 0;
	
		memset(m72_spriteram,0,spriteram_size[0]);
	
		xadjust = 0;
	
		/* improves bad gfx in nspirit (but this is not a complete fix, maybe there's a */
		/* layer enalbe register */
		for (i = 0;i < Machine.drv.total_colors;i++)
			palette_change_color(i,0,0,0);
	
		return 0;
	} };
	
	public static VhStartPtr dbreed_vh_start = new VhStartPtr() { public int handler() 
	{
		bg_tilemap = tilemap_create(dbreed_get_bg_tile_info,TILEMAP_OPAQUE,     8,8,64,64);
		fg_tilemap = tilemap_create(m72_get_fg_tile_info,TILEMAP_TRANSPARENT,8,8,64,64);
	
		m72_spriteram = new UBytePtr(spriteram_size[0]);;
	
		if (fg_tilemap==null || bg_tilemap==null || m72_spriteram==null)
			return 1;
	
		fg_tilemap.transparent_pen = 0;
	
		memset(m72_spriteram,0,spriteram_size[0]);
	
		xadjust = 0;
	
		return 0;
	} };
	
	public static VhStartPtr rtype2_vh_start = new VhStartPtr() { public int handler() 
	{
		bg_tilemap = tilemap_create(rtype2_get_bg_tile_info,TILEMAP_OPAQUE,     8,8,64,64);
		fg_tilemap = tilemap_create(rtype2_get_fg_tile_info,TILEMAP_TRANSPARENT,8,8,64,64);
	
		m72_spriteram = new UBytePtr(spriteram_size[0]);
	
		if (fg_tilemap==null || bg_tilemap==null || m72_spriteram==null)
			return 1;
	
		fg_tilemap.transparent_pen = 0;
	
		memset(m72_spriteram,0,spriteram_size[0]);
	
		xadjust = -4;
	
		return 0;
	} };
	
	/* Major Title has a larger background RAM, and rowscroll */
	public static VhStartPtr majtitle_vh_start = new VhStartPtr() { public int handler() 
	{
	// tilemap can be 256x64, but seems to be used at 128x64 (scroll wraparound) */
	//	bg_tilemap = tilemap_create(majtitle_get_bg_tile_info,TILEMAP_OPAQUE,     8,8,256,64);
		bg_tilemap = tilemap_create(majtitle_get_bg_tile_info,TILEMAP_OPAQUE,     8,8,128,64);
		fg_tilemap = tilemap_create(rtype2_get_fg_tile_info  ,TILEMAP_TRANSPARENT,8,8,64,64);
	
		m72_spriteram = new UBytePtr(spriteram_size[0]);
	
		if (fg_tilemap==null || bg_tilemap==null || m72_spriteram==null)
			return 1;
	
		fg_tilemap.transparent_pen = 0;
	
		memset(m72_spriteram,0,spriteram_size[0]);
	
		xadjust = -4;
	
		return 0;
	} };
	
	public static VhStartPtr hharry_vh_start = new VhStartPtr() { public int handler() 
	{
		bg_tilemap = tilemap_create(hharry_get_bg_tile_info,TILEMAP_OPAQUE,     8,8,64,64);
		fg_tilemap = tilemap_create(hharry_get_fg_tile_info,TILEMAP_TRANSPARENT,8,8,64,64);
	
		m72_spriteram = new UBytePtr(spriteram_size[0]);
	
		if (fg_tilemap==null || bg_tilemap==null || m72_spriteram==null)
			return 1;
	
		fg_tilemap.transparent_pen = 0;
	
		memset(m72_spriteram,0,spriteram_size[0]);
	
		xadjust = -4;
	
		return 0;
	} };
	
	public static VhStopPtr m72_vh_stop = new VhStopPtr() { public void handler() 
	{
		m72_spriteram = null;
	} };
	
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static ReadHandlerPtr m72_palette1_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return paletteram.read(offset);
	} };
	
	public static ReadHandlerPtr m72_palette2_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return paletteram_2.read(offset);
	} };
	
	public static void changecolor(int color,int r,int g,int b)
	{
		r = (r << 3) | (r >> 2);
		g = (g << 3) | (g >> 2);
		b = (b << 3) | (b >> 2);
	
		palette_change_color(color,r,g,b);
	}
	
	public static WriteHandlerPtr m72_palette1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		paletteram.write(offset,data);
		if ((offset & 1) != 0) return;
		offset &= 0x3ff;
		changecolor(offset / 2,
				paletteram.read(offset + 0x000),
				paletteram.read(offset + 0x400),
				paletteram.read(offset + 0x800));
	} };
	
	public static WriteHandlerPtr m72_palette2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		paletteram_2.write(offset,data);
		if ((offset & 1) != 0) return;
		offset &= 0x3ff;
		changecolor(offset / 2 + 512,
				paletteram_2.read(offset + 0x000),
				paletteram_2.read(offset + 0x400),
				paletteram_2.read(offset + 0x800));
	} };
	
	public static ReadHandlerPtr m72_videoram1_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return m72_videoram1.read(offset);
	} };
	
	public static ReadHandlerPtr m72_videoram2_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return m72_videoram2.read(offset);
	} };
	
	public static WriteHandlerPtr m72_videoram1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (m72_videoram1.read(offset) != data)
		{
			m72_videoram1.write(offset,data);
			tilemap_mark_tile_dirty(fg_tilemap,(offset/4)%64,(offset/4)/64);
		}
	} };
	
	public static WriteHandlerPtr m72_videoram2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (m72_videoram2.read(offset) != data)
		{
			m72_videoram2.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap,(offset/4)%64,(offset/4)/64);
		}
	} };
	
	public static WriteHandlerPtr majtitle_videoram2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (m72_videoram2.read(offset) != data)
		{
			m72_videoram2.write(offset,data);
	//		tilemap_mark_tile_dirty(bg_tilemap,(offset/4)%256,(offset/4)/256);
	// tilemap can be 256x64, but seems to be used at 128x64 (scroll wraparound) */
	if ((offset/4)%256 < 128)
			tilemap_mark_tile_dirty(bg_tilemap,(offset/4)%256,(offset/4)/256);
		}
	} };
	
	public static WriteHandlerPtr m72_irq_line_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		offset *= 8;
		splitline = (splitline & (0xff00 >> offset)) | (data << offset);
	} };
	
	public static WriteHandlerPtr m72_scrollx1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int i;
	
		offset *= 8;
		scrollx1[rastersplit] = (scrollx1[rastersplit] & (0xff00 >> offset)) | (data << offset);
	
		for (i = rastersplit+1;i < 256;i++)
			scrollx1[i] = scrollx1[rastersplit];
	} };
	
	public static WriteHandlerPtr m72_scrollx2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int i;
	
		offset *= 8;
		scrollx2[rastersplit] = (scrollx2[rastersplit] & (0xff00 >> offset)) | (data << offset);
	
		for (i = rastersplit+1;i < 256;i++)
			scrollx2[i] = scrollx2[rastersplit];
	} };
	
	public static WriteHandlerPtr m72_scrolly1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int i;
	
		offset *= 8;
		scrolly1[rastersplit] = (scrolly1[rastersplit] & (0xff00 >> offset)) | (data << offset);
	
		for (i = rastersplit+1;i < 256;i++)
			scrolly1[i] = scrolly1[rastersplit];
	} };
	
	public static WriteHandlerPtr m72_scrolly2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int i;
	
		offset *= 8;
		scrolly2[rastersplit] = (scrolly2[rastersplit] & (0xff00 >> offset)) | (data << offset);
	
		for (i = rastersplit+1;i < 256;i++)
			scrolly2[i] = scrolly2[rastersplit];
	} };
	
	public static WriteHandlerPtr m72_spritectrl_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	//if (errorlog != 0) fprintf(errorlog,"%04x: write %02x to sprite ctrl+%d\n",cpu_get_pc(),data,offset);
		/* TODO: this is ok for R-Type, but might be wrong for others */
		if (offset == 1)
		{
			memcpy(m72_spriteram,spriteram,spriteram_size[0]);
			if ((data & 0x40) != 0) memset(spriteram,0,spriteram_size[0]);
			/* bit 7 is used by bchopper, nspirit, imgfight, loht, gallop - meaning unknown */
			/* rtype2 uses bits 4,5,6 and 7 - of course it could be a different chip */
		}
	} };
	
	public static WriteHandlerPtr hharry_spritectrl_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	//if (errorlog != 0) fprintf(errorlog,"%04x: write %02x to sprite ctrl+%d\n",cpu_get_pc(),data,offset);
		if (offset == 0)
		{
			memcpy(m72_spriteram,spriteram,spriteram_size[0]);
			memset(spriteram,0,spriteram_size[0]);
		}
	} };
	
	public static WriteHandlerPtr hharryu_spritectrl_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	//if (errorlog != 0) fprintf(errorlog,"%04x: write %02x to sprite ctrl+%d\n",cpu_get_pc(),data,offset);
		if (offset == 1)
		{
			memcpy(m72_spriteram,spriteram,spriteram_size[0]);
			if ((data & 0x80) != 0) memset(spriteram,0,spriteram_size[0]);
			/* hharryu uses bits 2,3,4,5,6 and 7 - of course it could be a different chip */
			/* majtitle uses bits 2,3,5,6 and 7 - of course it could be a different chip */
		}
	} };
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	static void draw_sprites(osd_bitmap bitmap)
	{
		int offs;
	
		for (offs = 0;offs < spriteram_size[0];offs += 8)
		{
			int code,color,sx,sy,flipx,flipy,w,h,x,y;
	
	
			code = m72_spriteram.read(offs+2) | (m72_spriteram.read(offs+3) << 8);
			color = m72_spriteram.read(offs+4) & 0x0f;
			sx = -256+(m72_spriteram.read(offs+6) | ((m72_spriteram.read(offs+7) & 0x03) << 8));
			sy = 512-(m72_spriteram.read(offs+0) | ((m72_spriteram.read(offs+1) & 0x01) << 8));
			flipx = m72_spriteram.read(offs+5) & 0x08;
			flipy = m72_spriteram.read(offs+5) & 0x04;
	
			w = 1 << ((m72_spriteram.read(offs+5) & 0xc0) >> 6);
			h = 1 << ((m72_spriteram.read(offs+5) & 0x30) >> 4);
			sy -= 16 * h;
	
			for (x = 0;x < w;x++)
			{
				for (y = 0;y < h;y++)
				{
					int c = code;
	
					if (flipx != 0) c += 8*(w-1-x);
					else c += 8*x;
					if (flipy != 0) c += h-1-y;
					else c += y;
	
					drawgfx(bitmap,Machine.gfx[0],
							c,
							color,
							flipx,flipy,
							sx + 16*x,sy + 16*y,
							Machine.drv.visible_area,TRANSPARENCY_PEN,0);
				}
			}
		}
	}
	
	static void majtitle_draw_sprites(osd_bitmap bitmap)
	{
		int offs;
	
		for (offs = 0;offs < spriteram_size[0];offs += 8)
		{
			int code,color,sx,sy,flipx,flipy,w,h,x,y;
	
	
			code = spriteram_2.read(offs+2) | (spriteram_2.read(offs+3) << 8);
			color = spriteram_2.read(offs+4) & 0x0f;
			sx = -256+(spriteram_2.read(offs+6) | ((spriteram_2.read(offs+7) & 0x03) << 8));
			sy = 512-(spriteram_2.read(offs+0) | ((spriteram_2.read(offs+1) & 0x01) << 8));
			flipx = spriteram_2.read(offs+5) & 0x08;
			flipy = spriteram_2.read(offs+5) & 0x04;
	
			w = 1;// << ((spriteram_2[offs+5] & 0xc0) >> 6);
			h = 1 << ((spriteram_2.read(offs+5) & 0x30) >> 4);
			sy -= 16 * h;
	
			for (x = 0;x < w;x++)
			{
				for (y = 0;y < h;y++)
				{
					int c = code;
	
					if (flipx != 0) c += 8*(w-1-x);
					else c += 8*x;
					if (flipy != 0) c += h-1-y;
					else c += y;
	
					drawgfx(bitmap,Machine.gfx[2],
							c,
							color,
							flipx,flipy,
							sx + 16*x,sy + 16*y,
							Machine.drv.visible_area,TRANSPARENCY_PEN,0);
				}
			}
		}
	}
	
	static void mark_sprite_colors(UBytePtr ram)
	{
		int offs,color,i;
		int[] colmask=new int[32];
		int pal_base;
	
	
		pal_base = Machine.drv.gfxdecodeinfo[0].color_codes_start;
	
		for (color = 0;color < 32;color++) colmask[color] = 0;
	
		for (offs = 0;offs < spriteram_size[0];offs += 8)
		{
			color = ram.read(offs+4) & 0x0f;
			colmask[color] |= 0xffff;
		}
	
		for (color = 0;color < 32;color++)
		{
			for (i = 1;i < 16;i++)
			{
				if ((colmask[color] & (1 << i))!=0)
					palette_used_colors.write(pal_base + 16 * color + i,palette_used_colors.read(pal_base + 16 * color + i) | PALETTE_COLOR_VISIBLE);
			}
		}
	}
	
	static void draw_layer(osd_bitmap bitmap,
			tilemap tilemap,int[] scrollx,int[] scrolly,int priority)
	{
		int start,i;
		/* use clip regions to split the screen */
		rectangle clip=new rectangle();
	
		clip.min_x = Machine.drv.visible_area.min_x;
		clip.max_x = Machine.drv.visible_area.max_x;
		start = Machine.drv.visible_area.min_y - 128;
		do
		{
			i = start;
			while (scrollx[i+1] == scrollx[start] && scrolly[i+1] == scrolly[start]
					&& i < Machine.drv.visible_area.max_y - 128)
                        {
				i++;
                                if(i==255) break;//check the boundries of table (shadow)
                        }
                        //System.out.println(i);
			clip.min_y = start + 128;
			clip.max_y = i + 128;
			tilemap_set_clip(tilemap,clip);
			tilemap_set_scrollx(tilemap,0,scrollx[start] + xadjust);
			tilemap_set_scrolly(tilemap,0,scrolly[start]);
			tilemap_draw(bitmap,tilemap,priority);
	
			start = i+1;
		} while (start < Machine.drv.visible_area.max_y - 128);
	}
	
	static void draw_bg(osd_bitmap bitmap,int priority)
	{
		draw_layer(bitmap,bg_tilemap,scrollx2,scrolly2,priority);
	}
	
	static void draw_fg(osd_bitmap bitmap,int priority)
	{
		draw_layer(bitmap,fg_tilemap,scrollx1,scrolly1,priority);
	}
	
	
	public static VhUpdatePtr m72_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_set_clip(fg_tilemap,null);
		tilemap_set_clip(bg_tilemap,null);
	
		tilemap_update(bg_tilemap);
		tilemap_update(fg_tilemap);
	
		palette_init_used_colors();
		mark_sprite_colors(m72_spriteram);
		if (palette_recalc()!=null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		draw_bg(bitmap,0);
		draw_fg(bitmap,0);
		draw_sprites(bitmap);
		draw_fg(bitmap,1);
	} };
	
	public static VhUpdatePtr dbreed_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_set_clip(fg_tilemap,null);
		tilemap_set_clip(bg_tilemap,null);
	
		tilemap_update(bg_tilemap);
		tilemap_update(fg_tilemap);
	
		palette_init_used_colors();
		mark_sprite_colors(m72_spriteram);
		if (palette_recalc()!=null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		draw_bg(bitmap,0);
		draw_fg(bitmap,0);
		draw_sprites(bitmap);
		draw_bg(bitmap,1);
		draw_fg(bitmap,1);
	} };
	
	public static VhUpdatePtr majtitle_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i;
	
	
		tilemap_set_clip(fg_tilemap,null);
	
		/* TODO: find how rowscroll is disabled */
		if (1 != 0)
		{
			tilemap_set_scroll_rows(bg_tilemap,512);
			for (i = 0;i < 512;i++)
				tilemap_set_scrollx(bg_tilemap,(i+scrolly2[0])&0x1ff,
						256 + majtitle_rowscrollram.read(2*i) + (majtitle_rowscrollram.read(2*i+1) << 8) + xadjust);
		}
		else
		{
			tilemap_set_scroll_rows(bg_tilemap,1);
			tilemap_set_scrollx(bg_tilemap,0,256 + scrollx2[0] + xadjust);
		}
		tilemap_set_scrolly(bg_tilemap,0,scrolly2[0]);
		tilemap_update(bg_tilemap);
		tilemap_update(fg_tilemap);
	
		palette_init_used_colors();
		mark_sprite_colors(m72_spriteram);
		mark_sprite_colors(spriteram_2);
		if (palette_recalc()!=null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		tilemap_draw(bitmap,bg_tilemap,0);
	
		draw_fg(bitmap,0);
		majtitle_draw_sprites(bitmap);
		draw_sprites(bitmap);
		draw_fg(bitmap,1);
	} };
}
