/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.vidhrdw;

import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.cpu_getcurrentframe;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;

public class cninja
{
	
	public static UBytePtr cninja_pf1_data=new UBytePtr();
        public static UBytePtr cninja_pf2_data=new UBytePtr();
	public static UBytePtr cninja_pf3_data=new UBytePtr();
        public static UBytePtr cninja_pf4_data=new UBytePtr();
	public static UBytePtr cninja_pf1_rowscroll=new UBytePtr();
        public static UBytePtr cninja_pf2_rowscroll=new UBytePtr();
	public static UBytePtr cninja_pf3_rowscroll=new UBytePtr();
        public static UBytePtr cninja_pf4_rowscroll=new UBytePtr();
	
	static tilemap cninja_pf1_tilemap,cninja_pf2_tilemap,cninja_pf3_tilemap,cninja_pf4_tilemap;
	public static UBytePtr gfx_base;
	static int gfx_bank;
	
	public static UBytePtr cninja_control_0=new UBytePtr(16);
	public static UBytePtr cninja_control_1=new UBytePtr(16);
	
	static int cninja_pf2_bank,cninja_pf3_bank;
	static int bootleg,spritemask,color_base,flipscreen;
	
	public static UBytePtr cninja_spriteram;
	
	/******************************************************************************/
	
	public static WriteHandlerPtr cninja_update_sprites = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		memcpy(cninja_spriteram,spriteram,0x800);
	} };
	
	static void update_24bitcol(int offset)
	{
		int r,g,b;
	
		if ((offset%4)!=0) offset-=2;
	
		b = (paletteram.READ_WORD(offset) >> 0) & 0xff;
		g = (paletteram.READ_WORD(offset+2) >> 8) & 0xff;
		r = (paletteram.READ_WORD(offset+2) >> 0) & 0xff;
	
		palette_change_color(offset / 4,r,g,b);
	}
	
	public static WriteHandlerPtr cninja_palette_24bit_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(paletteram,offset,data);
		update_24bitcol(offset);
	} };
	
	/******************************************************************************/
	
	static void mark_sprites_colors()
	{
		int offs,color,i,pal_base;
		int[] colmask=new int[16];
                int[] pen_usage; /* Save some struct derefs */
	
		/* Sprites */
		pal_base = Machine.drv.gfxdecodeinfo[4].color_codes_start;
		pen_usage=Machine.gfx[4].pen_usage;
		for (color = 0;color < 16;color++) colmask[color] = 0;
	
		for (offs = 0;offs < 0x800;offs += 8)
		{
			int x,y,sprite,multi;
	
			sprite = cninja_spriteram.READ_WORD(offs+2) & spritemask;
			if (sprite==0) continue;
	
			x = cninja_spriteram.READ_WORD(offs+4);
			y = cninja_spriteram.READ_WORD(offs);
	
			color = (x >> 9) &0xf;
			multi = (1 << ((y & 0x0600) >> 9)) - 1;
			sprite &= ~multi;
	
			/* Save palette by missing offscreen sprites */
			x = x & 0x01ff;
			y = y & 0x01ff;
			if (x >= 256) x -= 512;
			x = 240 - x;
			if (x>256) continue;
	
			while (multi >= 0)
			{
				colmask[color] |= pen_usage[sprite + multi];
				multi--;
			}
		}
	
		for (color = 0;color < 16;color++)
		{
			for (i = 1;i < 16;i++)
			{
				if ((colmask[color] & (1 << i))!=0)
					palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
			}
		}
	}
	
	static void cninja_drawsprites(osd_bitmap bitmap, int pri)
	{
		int offs;
	
		for (offs = 0;offs < 0x800;offs += 8)
		{
			int x,y,sprite,colour,multi,fx,fy,inc,flash,mult;
			sprite = cninja_spriteram.READ_WORD(offs+2) & spritemask;
			if (sprite==0) continue;
	
			x = cninja_spriteram.READ_WORD(offs+4);
	
			/* Sprite/playfield priority */
			if ((x&0x4000)!=0 && pri==1) continue;
			if ((x&0x4000)==0 && pri==0) continue;
	
			y = cninja_spriteram.READ_WORD(offs);
			flash=y&0x1000;
			if (flash!=0 && (cpu_getcurrentframe() & 1)!=0) continue;
			colour = (x >> 9) &0xf;
	
			fx = y & 0x2000;
			fy = y & 0x4000;
			multi = (1 << ((y & 0x0600) >> 9)) - 1;	/* 1x, 2x, 4x, 8x height */
	
			x = x & 0x01ff;
			y = y & 0x01ff;
			if (x >= 256) x -= 512;
			if (y >= 256) y -= 512;
			x = 240 - x;
			y = 240 - y;
	
			if (x>256) continue; /* Speedup */
	
			sprite &= ~multi;
			if (fy != 0)
				inc = -1;
			else
			{
				sprite += multi;
				inc = 1;
			}
	
			if (flipscreen != 0) {
				y=240-y;
				x=240-x;
				if (fx != 0) fx=0; else fx=1;
				if (fy != 0) fy=0; else fy=1;
				mult=16;
			}
			else mult=-16;
	
			while (multi >= 0)
			{
				drawgfx(bitmap,Machine.gfx[4],
						sprite - multi * inc,
						colour,
						fx,fy,
						x,y + mult * multi,
						Machine.drv.visible_area,TRANSPARENCY_PEN,0);
	
				multi--;
			}
		}
	}
	
	/* Function for all 16x16 1024x512 layers */
	public static WriteHandlerPtr get_back_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int offs,tile,color;
	
		if (col>31 && row>15) offs=0xc00 + (col-32)*2 + (row-16) *64; /* Bottom right */
		else if (col>31) offs=0x800 + (col-32)*2 + row *64; /* Top right */
		else if (row>15) offs=0x400 + col*2 + (row-16) *64; /* Bottom left */
		else offs=col*2 + row *64; /* Top left */
	
		tile=gfx_base.READ_WORD(offs);
		color=tile >> 12;
		tile=tile&0xfff;
	
		SET_TILE_INFO(gfx_bank,tile,color+color_base);
	} };
	
	/* 8x8 top layer */
	public static WriteHandlerPtr get_fore_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int offs=(col*2) + (row*128);
		int tile=cninja_pf1_data.READ_WORD(offs);
		int color=tile >> 12;
	
		tile=tile&0xfff;
	
		SET_TILE_INFO(0,tile,color);
	} };
	
	/******************************************************************************/
	public static VhUpdatePtr cninja_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
		int pf23_control,pf1_control;
	
		/* Update flipscreen */
		flipscreen = cninja_control_1.READ_WORD(0)&0x80;
		tilemap_set_flip(ALL_TILEMAPS,flipscreen!=0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
	
		/* Handle gfx rom switching */
		pf23_control=cninja_control_0.READ_WORD(0xe);
		if ((pf23_control&0xff)==0x00)
			cninja_pf3_bank=2;
		else
			cninja_pf3_bank=1;
	
		if ((pf23_control&0xff00)==0x00)
			cninja_pf2_bank=2;
		else
			cninja_pf2_bank=1;
	
		/* Setup scrolling */
		pf23_control=cninja_control_0.READ_WORD(0xc);
		pf1_control=cninja_control_1.READ_WORD(0xc);
	
		/* Background - Rowscroll enable */
		if ((pf23_control & 0x4000) != 0) {
			int scrollx=cninja_control_0.READ_WORD(6),rows;
			tilemap_set_scroll_cols(cninja_pf2_tilemap,1);
			tilemap_set_scrolly( cninja_pf2_tilemap,0, cninja_control_0.READ_WORD(8) );
	
			/* Several different rowscroll styles! */
			switch ((cninja_control_0.READ_WORD(0xa)>>11)&7) {
				case 0: rows=512; break;/* Every line of 512 height bitmap */
				case 1: rows=256; break;
				case 2: rows=128; break;
				case 3: rows=64; break;
				case 4: rows=32; break;
				case 5: rows=16; break;
				case 6: rows=8; break;
				case 7: rows=4; break;
				default: rows=1; break;
			}
	
			tilemap_set_scroll_rows(cninja_pf2_tilemap,rows);
			for (offs = 0;offs < rows;offs++)
				tilemap_set_scrollx( cninja_pf2_tilemap,offs, scrollx + cninja_pf2_rowscroll.READ_WORD(2*offs) );
		}
		else {
			tilemap_set_scroll_rows(cninja_pf2_tilemap,1);
			tilemap_set_scroll_cols(cninja_pf2_tilemap,1);
			tilemap_set_scrollx( cninja_pf2_tilemap,0, cninja_control_0.READ_WORD(6) );
			tilemap_set_scrolly( cninja_pf2_tilemap,0, cninja_control_0.READ_WORD(8) );
		}
	
		/* Playfield 3 */
		if ((pf23_control & 0x40) != 0) { /* Rowscroll */
			int scrollx=cninja_control_0.READ_WORD(2),rows;
			tilemap_set_scroll_cols(cninja_pf3_tilemap,1);
			tilemap_set_scrolly( cninja_pf3_tilemap,0, cninja_control_0.READ_WORD(4) );
	
			/* Several different rowscroll styles! */
			switch ((cninja_control_0.READ_WORD(0xa)>>3)&7) {
				case 0: rows=512; break;/* Every line of 512 height bitmap */
				case 1: rows=256; break;
				case 2: rows=128; break;
				case 3: rows=64; break;
				case 4: rows=32; break;
				case 5: rows=16; break;
				case 6: rows=8; break;
				case 7: rows=4; break;
				default: rows=1; break;
			}
	
			tilemap_set_scroll_rows(cninja_pf3_tilemap,rows);
			for (offs = 0;offs < rows;offs++)
				tilemap_set_scrollx( cninja_pf3_tilemap,offs, scrollx + cninja_pf3_rowscroll.READ_WORD(2*offs) );
		}
		else if ((pf23_control & 0x20) != 0) { /* Colscroll */
			int scrolly=cninja_control_0.READ_WORD(4);
			tilemap_set_scroll_rows(cninja_pf3_tilemap,1);
			tilemap_set_scroll_cols(cninja_pf3_tilemap,64);
			tilemap_set_scrollx( cninja_pf3_tilemap,0, cninja_control_0.READ_WORD(2) );
	
			/* Used in lava level & Level 1 */
			for (offs=0 ; offs < 32;offs++)
				tilemap_set_scrolly( cninja_pf3_tilemap,offs+32, scrolly + cninja_pf3_rowscroll.READ_WORD((2*offs)+0x400) );
		}
		else {
			tilemap_set_scroll_rows(cninja_pf3_tilemap,1);
			tilemap_set_scroll_cols(cninja_pf3_tilemap,1);
			tilemap_set_scrollx( cninja_pf3_tilemap,0, cninja_control_0.READ_WORD(2) );
			tilemap_set_scrolly( cninja_pf3_tilemap,0, cninja_control_0.READ_WORD(4) );
		}
	
		/* Top foreground */
		if ((pf1_control & 0x4000) != 0) {
			int scrollx=cninja_control_1.READ_WORD(6),rows;
			tilemap_set_scroll_cols(cninja_pf4_tilemap,1);
			tilemap_set_scrolly( cninja_pf4_tilemap,0, cninja_control_1.READ_WORD(8) );
	
			/* Several different rowscroll styles! */
			switch ((cninja_control_1.READ_WORD(0xa)>>11)&7) {
				case 0: rows=512; break;/* Every line of 512 height bitmap */
				case 1: rows=256; break;
				case 2: rows=128; break;
				case 3: rows=64; break;
				case 4: rows=32; break;
				case 5: rows=16; break;
				case 6: rows=8; break;
				case 7: rows=4; break;
				default: rows=1; break;
			}
	
			tilemap_set_scroll_rows(cninja_pf4_tilemap,rows);
			for (offs = 0;offs < rows;offs++)
				tilemap_set_scrollx( cninja_pf4_tilemap,offs, scrollx + cninja_pf4_rowscroll.READ_WORD(2*offs) );
		}
		else if ((pf1_control & 0x2000) != 0) { /* Colscroll */
			int scrolly=cninja_control_1.READ_WORD(8);
			tilemap_set_scroll_rows(cninja_pf4_tilemap,1);
			tilemap_set_scroll_cols(cninja_pf4_tilemap,64);
			tilemap_set_scrollx( cninja_pf4_tilemap,0, cninja_control_0.READ_WORD(2) );
	
			/* Used in first lava level */
			for (offs=0 ; offs < 64;offs++)
				tilemap_set_scrolly( cninja_pf4_tilemap,offs, scrolly + cninja_pf4_rowscroll.READ_WORD((2*offs)+0x400) );
		}
		else {
			tilemap_set_scroll_rows(cninja_pf4_tilemap,1);
			tilemap_set_scroll_cols(cninja_pf4_tilemap,1);
			tilemap_set_scrollx( cninja_pf4_tilemap,0, cninja_control_1.READ_WORD(6) );
			tilemap_set_scrolly( cninja_pf4_tilemap,0, cninja_control_1.READ_WORD(8) );
		}
	
		/* Playfield 1 - 8 * 8 Text */
		if ((pf1_control & 0x40) != 0) { /* Rowscroll */
			int scrollx=cninja_control_1.READ_WORD(2),rows;
			tilemap_set_scroll_cols(cninja_pf1_tilemap,1);
			tilemap_set_scrolly( cninja_pf1_tilemap,0, cninja_control_1.READ_WORD(4) );
	
			/* Several different rowscroll styles! */
			switch ((cninja_control_1.READ_WORD(0xa)>>3)&7) {
				case 0: rows=256; break;
				case 1: rows=128; break;
				case 2: rows=64; break;
				case 3: rows=32; break;
				case 4: rows=16; break;
				case 5: rows=8; break;
				case 6: rows=4; break;
				case 7: rows=2; break;
				default: rows=1; break;
			}
	
			tilemap_set_scroll_rows(cninja_pf1_tilemap,rows);
			for (offs = 0;offs < rows;offs++)
				tilemap_set_scrollx( cninja_pf1_tilemap,offs, scrollx + cninja_pf1_rowscroll.READ_WORD(2*offs) );
		}
		else {
			tilemap_set_scroll_rows(cninja_pf1_tilemap,1);
			tilemap_set_scroll_cols(cninja_pf1_tilemap,1);
			tilemap_set_scrollx( cninja_pf1_tilemap,0, cninja_control_1.READ_WORD(2) );
			tilemap_set_scrolly( cninja_pf1_tilemap,0, cninja_control_1.READ_WORD(4) );
		}
	
		/* Update playfields */
		gfx_bank=cninja_pf2_bank;
		gfx_base=cninja_pf2_data;
		color_base=48;
		tilemap_update(cninja_pf2_tilemap);
	
		gfx_bank=cninja_pf3_bank;
		gfx_base=cninja_pf3_data;
		color_base=0;
		tilemap_update(cninja_pf3_tilemap);
	
		gfx_bank=3;
		gfx_base=cninja_pf4_data;
		color_base=0;
		tilemap_update(cninja_pf4_tilemap);
		tilemap_update(cninja_pf1_tilemap);
	
		palette_init_used_colors();
		mark_sprites_colors();
		if (palette_recalc()!=null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		/* Draw playfields */
		tilemap_render(ALL_TILEMAPS);
		tilemap_draw(bitmap,cninja_pf2_tilemap,0);
		tilemap_draw(bitmap,cninja_pf3_tilemap,0);
		tilemap_draw(bitmap,cninja_pf4_tilemap,TILEMAP_BACK);
		cninja_drawsprites(bitmap,0);
		tilemap_draw(bitmap,cninja_pf4_tilemap,TILEMAP_FRONT);
		cninja_drawsprites(bitmap,1);
		tilemap_draw(bitmap,cninja_pf1_tilemap,0);
	}};
	
	/******************************************************************************/
	
	public static WriteHandlerPtr cninja_pf1_data_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int oldword = cninja_pf1_data.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword,data);
	
		if (oldword != newword)
		{
			cninja_pf1_data.WRITE_WORD(offset,newword);
			tilemap_mark_tile_dirty(cninja_pf1_tilemap,(offset%128)/2,offset/128);
		}
	} };
	
	public static WriteHandlerPtr cninja_pf2_data_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int oldword = cninja_pf2_data.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword,data);
		int dx=0,dy=0;
	
		if (oldword != newword)
		{
			cninja_pf2_data.WRITE_WORD(offset,newword);
	
			if (offset>0xbff) {offset-=0xc00;dx=32; dy=16;}
			else if (offset>0x7ff) {offset-=0x800;dx=32; dy=0;}
			else if (offset>0x3ff) {offset-=0x400;dx=0; dy=16;}
			dx+=(offset%64)/2; dy+=(offset/64);
			tilemap_mark_tile_dirty(cninja_pf2_tilemap,dx,dy);
		}
	} };
	
	public static WriteHandlerPtr cninja_pf3_data_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int oldword = cninja_pf3_data.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword,data);
		int dx=0,dy=0;
	
		if (oldword != newword)
		{
			cninja_pf3_data.WRITE_WORD(offset,newword);
	
			if (offset>0xbff) {offset-=0xc00;dx=32; dy=16;}
			else if (offset>0x7ff) {offset-=0x800;dx=32; dy=0;}
			else if (offset>0x3ff) {offset-=0x400;dx=0; dy=16;}
			dx+=(offset%64)/2; dy+=(offset/64);
			tilemap_mark_tile_dirty(cninja_pf3_tilemap,dx,dy);
		}
	} };
	
	public static WriteHandlerPtr cninja_pf4_data_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int oldword = cninja_pf4_data.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword,data);
		int dx=0,dy=0;
	
		if (oldword != newword)
		{
			cninja_pf4_data.WRITE_WORD(offset,newword);
	
			if (offset>0xbff) {offset-=0xc00;dx=32; dy=16;}
			else if (offset>0x7ff) {offset-=0x800;dx=32; dy=0;}
			else if (offset>0x3ff) {offset-=0x400;dx=0; dy=16;}
			dx+=(offset%64)/2; dy+=(offset/64);
			tilemap_mark_tile_dirty(cninja_pf4_tilemap,dx,dy);
		}
	} };
	
	public static WriteHandlerPtr cninja_control_0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (bootleg!=0 && offset==6) {
			COMBINE_WORD_MEM(cninja_control_0,offset,data+0xa);
			return;
		}
		COMBINE_WORD_MEM(cninja_control_0,offset,data);
	} };
	
	public static WriteHandlerPtr cninja_control_1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (bootleg != 0) {
			switch (offset) {
				case 2:
					COMBINE_WORD_MEM(cninja_control_1,offset,data-2);
					return;
				case 6:
					COMBINE_WORD_MEM(cninja_control_1,offset,data+0xa);
					return;
			}
		}
		COMBINE_WORD_MEM(cninja_control_1,offset,data);
	} };
	
	public static ReadHandlerPtr cninja_pf1_data_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return cninja_pf1_data.READ_WORD(offset);
	} };
	
	public static WriteHandlerPtr cninja_pf1_rowscroll_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(cninja_pf1_rowscroll,offset,data);
	} };
	
	public static WriteHandlerPtr cninja_pf2_rowscroll_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(cninja_pf2_rowscroll,offset,data);
	} };
	
	public static WriteHandlerPtr cninja_pf3_rowscroll_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(cninja_pf3_rowscroll,offset,data);
	} };
	
	public static ReadHandlerPtr cninja_pf3_rowscroll_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return cninja_pf3_rowscroll.READ_WORD(offset);
	} };
	
	public static WriteHandlerPtr cninja_pf4_rowscroll_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(cninja_pf4_rowscroll,offset,data);
	} };
	
	/******************************************************************************/
	
	public static VhStopPtr cninja_vh_stop = new VhStopPtr() { public void handler() 
	{
		cninja_spriteram=null;
	} };
	
	public static VhStartPtr cninja_vh_start = new VhStartPtr() { public int handler() 
	{
		/* The bootleg has some broken scroll registers... */
		if (strcmp(Machine.gamedrv.name,"stoneage")==0)
			bootleg=1;
		else
			bootleg=0;
	
		if (strcmp(Machine.gamedrv.name,"edrandy")==0
			|| strcmp(Machine.gamedrv.name,"edrandyj")==0)
			spritemask=0xffff;
		else
			spritemask=0x3fff;
	
		cninja_pf2_bank=1;
		cninja_pf3_bank=2;
	
		cninja_pf2_tilemap = tilemap_create(
			get_back_tile_info,
			0,
			16,16,
			64,32 /* 1024 by 512 */
		);
	
		cninja_pf3_tilemap = tilemap_create(
			get_back_tile_info,
			TILEMAP_TRANSPARENT,
			16,16,
			64,32
		);
	
		cninja_pf4_tilemap = tilemap_create(
			get_back_tile_info,
			TILEMAP_TRANSPARENT | TILEMAP_SPLIT,
			16,16,
			64,32
		);
	
		cninja_pf1_tilemap = tilemap_create(
			get_fore_tile_info,
			TILEMAP_TRANSPARENT,
			8,8,
			64,32
		);
	
		cninja_pf1_tilemap.transparent_pen = 0;
		cninja_pf3_tilemap.transparent_pen = 0;
		cninja_pf4_tilemap.transparent_pen = 0;
		cninja_pf4_tilemap.transmask[0] = 0x00ff;
		cninja_pf4_tilemap.transmask[1] = 0xff00;
	
		cninja_spriteram = new UBytePtr(0x800);
	
		return 0;
	} };
	
	/******************************************************************************/
}
