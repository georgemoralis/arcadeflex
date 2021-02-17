/***************************************************************************

  Mad Motor video emulation - Bryan McPhail, mish@tendril.co.uk

  Notes:  Playfield 3 can change size between 512x1024 and 2048x256

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.drawgfx;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import gr.codebb.arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.cpu_getcurrentframe;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.*;

public class madmotor
{
	
	public static UBytePtr madmotor_pf1_rowscroll=new UBytePtr();
	public static UBytePtr madmotor_pf1_data=new UBytePtr(),madmotor_pf2_data=new UBytePtr(),madmotor_pf3_data=new UBytePtr();
	
	static UBytePtr madmotor_pf1_control = new UBytePtr(32);
	static UBytePtr madmotor_pf2_control = new UBytePtr(32);
	static UBytePtr madmotor_pf3_control = new UBytePtr(32);
	
	static int flipscreen;
	static tilemap madmotor_pf1_tilemap,madmotor_pf2_tilemap,madmotor_pf3_tilemap,madmotor_pf3a_tilemap;
	
	
	
	
	/* 512 by 512 playfield, 8 by 8 tiles */
/*TODO*///	static UINT32 pf1_scan(UINT32 col,UINT32 row,UINT32 num_cols,UINT32 num_rows)
/*TODO*///	{
/*TODO*///		/* logical (col,row) . memory offset */
/*TODO*///		return (col & 0x1f) + ((row & 0x1f) << 5) + ((row & 0x20) << 5) + ((col & 0x20) << 6);
/*TODO*///	}
	
	public static WriteHandlerPtr get_pf1_tile_info = new WriteHandlerPtr() { public void handler(int col, int row) 
	{
		int offs,tile,color;

                if (col>31 && row>31) offs=0x1800 + (col-32)*2 + (row-32) *64; /* Bottom right */
                else if (col>31) offs=0x1000 + (col-32)*2 + row *64; /* Top right */
                else if (row>31) offs=0x800 + col*2 + (row-32) *64; /* Bottom left */
                else offs=col*2 + row *64; /* Top left */

                tile=madmotor_pf1_data.READ_WORD(offs);
                color=tile >> 12;
                tile=tile&0xfff;

                SET_TILE_INFO(0,tile,color);
	} };
	
	/* 512 by 512 playfield, 16 by 16 tiles */
/*TODO*///	static UINT32 pf2_scan(UINT32 col,UINT32 row,UINT32 num_cols,UINT32 num_rows)
/*TODO*///	{
/*TODO*///		/* logical (col,row) . memory offset */
/*TODO*///		return (col & 0x0f) + ((row & 0x0f) << 4) + ((row & 0x10) << 4) + ((col & 0x10) << 5);
/*TODO*///	}
	
	public static WriteHandlerPtr get_pf2_tile_info = new WriteHandlerPtr() { public void handler(int col, int row) 
	{
		int offs,tile,color;

                if (col>15 && row>15) offs=0x600 + (col-16)*2 + (row-16) * 32; /* Bottom right */
                else if (col>15) offs=0x400 + (col-16)*2 + row *32; /* Top right */
                else if (row>15) offs=0x200 + col*2 + (row-16) *32; /* Bottom left */
                else offs=col*2 + row *32; /* Top left */

                tile=madmotor_pf2_data.READ_WORD(offs);
                color=tile >> 12;
                tile=tile&0xfff;

                SET_TILE_INFO(1,tile,color);
	} };
	
	/* 512 by 1024 playfield, 16 by 16 tiles */
/*TODO*///	static UINT32 pf3_scan(UINT32 col,UINT32 row,UINT32 num_cols,UINT32 num_rows)
/*TODO*///	{
/*TODO*///		/* logical (col,row) . memory offset */
/*TODO*///		return (col & 0x0f) + ((row & 0x0f) << 4) + ((row & 0x30) << 4) + ((col & 0x10) << 6);
/*TODO*///	}
	
	public static WriteHandlerPtr get_pf3_tile_info = new WriteHandlerPtr() { public void handler(int col, int row) 
	{
		int offs,tile,color;

                if (col>15 && row>47) offs=0xe00 + (col-16)*2 + (row-48) *32;
                else if (col>15 && row>31) offs=0xc00 + (col-16)*2 + (row-32) *32;
                else if (col>15 && row>15) offs=0xa00 + (col-16)*2 + (row-16) *32;
                else if (col>15) offs=0x800 + (col-16)*2 + row *32;
                else if (row>47) offs=0x600 + col*2 + (row-48) *32;
                else if (row>31) offs=0x400 + col*2 + (row-32) *32;
                else if (row>15) offs=0x200 + col*2 + (row-16) *32;
                else offs=col*2 + row *32; /* Top left */

                tile=madmotor_pf3_data.READ_WORD(offs);
                color=tile >> 12;
                tile=tile&0xfff;

                SET_TILE_INFO(2,tile,color);
	} };
	
	/* 2048 by 256 playfield, 16 by 16 tiles */
/*TODO*///	static UINT32 pf3a_scan(UINT32 col,UINT32 row,UINT32 num_cols,UINT32 num_rows)
/*TODO*///	{
/*TODO*///		/* logical (col,row) . memory offset */
/*TODO*///		return (col & 0x0f) + ((row & 0x0f) << 4) + ((col & 0x70) << 4);
/*TODO*///	}
	
	public static WriteHandlerPtr get_pf3a_tile_info = new WriteHandlerPtr() { public void handler(int col, int row) 
	{
		int offs,tile,color;

                if (col>111) offs=0xe00 + (col-112)*2 + row *32;
                else if (col>95) offs=0xc00 + (col-96)*2 + row *32;
                else if (col>79) offs=0xa00 + (col-80)*2 + row *32;
                else if (col>63) offs=0x800 + (col-64)*2 + row *32;
                else if (col>47) offs=0x600 + (col-48)*2 + row *32;
                else if (col>31) offs=0x400 + (col-32)*2 + row *32;
                else if (col>15) offs=0x200 + (col-16)*2 + row *32;
                else offs=col*2 + row *32; /* Top left */

                tile=madmotor_pf3_data.READ_WORD(offs);
                color=tile >> 12;
                tile=tile&0xfff;

                SET_TILE_INFO(2,tile,color);
	} };
	
	/******************************************************************************/
	
	public static VhStartPtr madmotor_vh_start = new VhStartPtr() { public int handler() 
	{
		madmotor_pf1_tilemap = tilemap_create(
                        get_pf1_tile_info,
                        TILEMAP_TRANSPARENT,
                        8,8,
                        64,64
                );

                madmotor_pf2_tilemap = tilemap_create(
                        get_pf2_tile_info,
                        TILEMAP_TRANSPARENT,
                        16,16,
                        32,32
                );

                madmotor_pf3_tilemap = tilemap_create(
                        get_pf3_tile_info,
                        0,
                        16,16,
                        32,64
                );

                madmotor_pf3a_tilemap = tilemap_create(
                        get_pf3a_tile_info,
                        0,
                        16,16,
                        128,16
                );

                madmotor_pf1_tilemap.transparent_pen = 0;
                madmotor_pf2_tilemap.transparent_pen = 0;
                tilemap_set_scroll_rows(madmotor_pf1_tilemap,512);
                tilemap_set_scroll_cols(madmotor_pf1_tilemap,1);
                tilemap_set_scroll_rows(madmotor_pf2_tilemap,1);
                tilemap_set_scroll_cols(madmotor_pf2_tilemap,1);
                tilemap_set_scroll_rows(madmotor_pf3_tilemap,1);
                tilemap_set_scroll_cols(madmotor_pf3_tilemap,1);
                tilemap_set_scroll_rows(madmotor_pf3a_tilemap,1);
                tilemap_set_scroll_cols(madmotor_pf3a_tilemap,1);

                return 0;
	} };
	
	/******************************************************************************/
	
	public static ReadHandlerPtr madmotor_pf1_data_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return madmotor_pf1_data.READ_WORD(offset);
	} };
	
	public static ReadHandlerPtr madmotor_pf2_data_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return madmotor_pf2_data.READ_WORD(offset);
	} };
	
	public static ReadHandlerPtr madmotor_pf3_data_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return madmotor_pf3_data.READ_WORD(offset);
	} };
	
	public static WriteHandlerPtr madmotor_pf1_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int dx=0,dy=0;

                COMBINE_WORD_MEM(madmotor_pf1_data,offset,data);
                if (offset>0x17ff) {offset-=0x1800;dx=32; dy=32;}
                else if (offset>0xfff) {offset-=0x1000;dx=32; dy=0;}
                else if (offset>0x7ff) {offset-=0x800;dx=0; dy=32;}
                dx+=(offset%64)/2; dy+=(offset/64);
                tilemap_mark_tile_dirty(madmotor_pf1_tilemap,dx,dy);
	} };
	
	public static WriteHandlerPtr madmotor_pf2_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int dx=0,dy=0;

                COMBINE_WORD_MEM(madmotor_pf2_data,offset,data);
                if (offset>0x5ff) {offset-=0x600;dx=16; dy=16;}
                else if (offset>0x3ff) {offset-=0x400;dx=16; dy=0;}
                else if (offset>0x1ff) {offset-=0x200;dx=0; dy=16;}
                dx+=(offset%32)/2; dy+=(offset/32);
                tilemap_mark_tile_dirty(madmotor_pf2_tilemap,dx,dy);
	} };
	
	public static WriteHandlerPtr madmotor_pf3_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int dx=0,dy=0;

                COMBINE_WORD_MEM(madmotor_pf3_data,offset,data);

                /* Mark the dirty position on the 512 x 1024 version */
                if (offset>0xdff) {offset-=0xe00;dx=16; dy=48;}
                else if (offset>0xbff) {offset-=0xc00;dx=16; dy=32;}
                else if (offset>0x9ff) {offset-=0xa00;dx=16; dy=16;}
                else if (offset>0x7ff) {offset-=0x800;dx=16; dy=0; }
                else if (offset>0x5ff) {offset-=0x600;dx=0;  dy=48;}
                else if (offset>0x3ff) {offset-=0x400;dx=0;  dy=32;}
                else if (offset>0x1ff) {offset-=0x200;dx=0;  dy=16;}
                dx+=(offset%32)/2; dy+=(offset/32);
                tilemap_mark_tile_dirty(madmotor_pf3_tilemap,dx,dy);

                /* Mark the dirty position on the 2048 x 256 version */
                dx=0; dy=0;
                if (offset>0xdff) {offset-=0xe00;dx=112; }
                else if (offset>0xbff) {offset-=0xc00;dx=96; }
                else if (offset>0x9ff) {offset-=0xa00;dx=80; }
                else if (offset>0x7ff) {offset-=0x800;dx=64; }
                else if (offset>0x5ff) {offset-=0x600;dx=48; }
                else if (offset>0x3ff) {offset-=0x400;dx=32; }
                else if (offset>0x1ff) {offset-=0x200;dx=16; }
                dx+=(offset%32)/2; dy+=(offset/32);
                tilemap_mark_tile_dirty(madmotor_pf3_tilemap,dx,dy);
	} };
	
	public static WriteHandlerPtr madmotor_pf1_control_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(madmotor_pf1_control,offset,data);
	} };
	
	public static WriteHandlerPtr madmotor_pf2_control_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(madmotor_pf2_control,offset,data);
	} };
	
	public static WriteHandlerPtr madmotor_pf3_control_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(madmotor_pf3_control,offset,data);
	} };
	
	public static ReadHandlerPtr madmotor_pf1_rowscroll_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return madmotor_pf1_rowscroll.READ_WORD(offset);
	} };
	
	public static WriteHandlerPtr madmotor_pf1_rowscroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(madmotor_pf1_rowscroll,offset,data);
	} };
	
	/******************************************************************************/
	
	static void madmotor_mark_sprite_colours()
	{
		int offs,color,i,pal_base;
		int[] colmask=new int[16];
	
		palette_init_used_colors();
	
		/* Sprites */
		pal_base = Machine.drv.gfxdecodeinfo[3].color_codes_start;
		for (color = 0;color < 16;color++) colmask[color] = 0;
		for (offs = 0;offs < 0x800;offs += 8)
		{
			int x,y,sprite,multi;
	
			y = spriteram.READ_WORD(offs);
			if ((y&0x8000) == 0) continue;
	
			x = spriteram.READ_WORD(offs+4);
			color = (x & 0xf000) >> 12;
	
			multi = (1 << ((y & 0x1800) >> 11)) - 1;	/* 1x, 2x, 4x, 8x height */
												/* multi = 0   1   3   7 */
	
			x = x & 0x01ff;
			if (x >= 256) x -= 512;
			x = 240 - x;
			if (x>256) continue; /* Speedup + save colours */
	
			sprite = spriteram.READ_WORD (offs+2) & 0x1fff;
			sprite &= ~multi;
	
			while (multi >= 0)
			{
				colmask[color] |= Machine.gfx[3].pen_usage[sprite + multi];
	
				multi--;
			}
		}
	
		for (color = 0;color < 16;color++)
		{
			for (i = 1;i < 16;i++)
			{
				if ((colmask[color] & (1 << i)) != 0)
					palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
			}
		}
	}
	
	
	static void dec0_drawsprites(osd_bitmap bitmap,int pri_mask,int pri_val)
	{
		int offs;

                for (offs = 0;offs < 0x800;offs += 8)
                {
                        int x,y,sprite,colour,multi,fx,fy,inc,flash,mult;

                        y = spriteram.READ_WORD(offs);
                        if ((y&0x8000) == 0) continue;

                        x = spriteram.READ_WORD(offs+4);
                        colour = x >> 12;
                        if ((colour & pri_mask) != pri_val) continue;

                        flash=x&0x800;
                        if (flash!=0 && (cpu_getcurrentframe() & 1)!=0) continue;

                        fx = y & 0x2000;
                        fy = y & 0x4000;
                        multi = (1 << ((y & 0x1800) >> 11)) - 1;	/* 1x, 2x, 4x, 8x height */
                                                                                                /* multi = 0   1   3   7 */

                        sprite = spriteram.READ_WORD (offs+2) & 0x1fff;

                        x = x & 0x01ff;
                        y = y & 0x01ff;
                        if (x >= 256) x -= 512;
                        if (y >= 256) y -= 512;
                        x = 240 - x;
                        y = 240 - y;

                        if (x>256) continue; /* Speedup */

                        sprite &= ~multi;
                        if (fy!=0)
                                inc = -1;
                        else
                        {
                                sprite += multi;
                                inc = 1;
                        }

                        if (flipscreen!=0) {
                                y=240-y;
                                x=240-x;
                                if (fx!=0) fx=0; else fx=1;
                                if (fy!=0) fy=0; else fy=1;
                                mult=16;
                        }
                        else mult=-16;

                        while (multi >= 0)
                        {
                                drawgfx(bitmap,Machine.gfx[3],
                                                sprite - multi * inc,
                                                colour,
                                                fx,fy,
                                                x,y + mult * multi,
                                                Machine.drv.visible_area,TRANSPARENCY_PEN,0);

                                multi--;
                        }
                }
	}
	
	/******************************************************************************/
	
	public static VhUpdatePtr madmotor_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
		/* Update flipscreen */
		if ((madmotor_pf1_control.READ_WORD(0)&0x80) != 0)
			flipscreen=1;
		else
			flipscreen=0;
		tilemap_set_flip(ALL_TILEMAPS,flipscreen!=0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
	
		/* Setup scroll registers */
		for (offs = 0;offs < 512;offs++)
			tilemap_set_scrollx( madmotor_pf1_tilemap,offs, madmotor_pf1_control.READ_WORD(0x10) + madmotor_pf1_rowscroll.READ_WORD(0x400+2*offs) );
		tilemap_set_scrolly( madmotor_pf1_tilemap,0, madmotor_pf1_control.READ_WORD(0x12) );
		tilemap_set_scrollx( madmotor_pf2_tilemap,0, madmotor_pf2_control.READ_WORD(0x10) );
		tilemap_set_scrolly( madmotor_pf2_tilemap,0, madmotor_pf2_control.READ_WORD(0x12) );
		tilemap_set_scrollx( madmotor_pf3_tilemap,0, madmotor_pf3_control.READ_WORD(0x10) );
		tilemap_set_scrolly( madmotor_pf3_tilemap,0, madmotor_pf3_control.READ_WORD(0x12) );
		tilemap_set_scrollx( madmotor_pf3a_tilemap,0, madmotor_pf3_control.READ_WORD(0x10) );
		tilemap_set_scrolly( madmotor_pf3a_tilemap,0, madmotor_pf3_control.READ_WORD(0x12) );
	
		tilemap_update(madmotor_pf1_tilemap);
		tilemap_update(madmotor_pf2_tilemap);
		tilemap_update(madmotor_pf3_tilemap);
		tilemap_update(madmotor_pf3a_tilemap);
	
		madmotor_mark_sprite_colours();
		if (palette_recalc() != null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		/* Draw playfields & sprites */
		tilemap_render(ALL_TILEMAPS);
		if (madmotor_pf3_control.READ_WORD(0x6)==2)
			tilemap_draw(bitmap,madmotor_pf3_tilemap,0);
		else
			tilemap_draw(bitmap,madmotor_pf3a_tilemap,0);
		tilemap_draw(bitmap,madmotor_pf2_tilemap,0);
		dec0_drawsprites(bitmap,0x00,0x00);
		tilemap_draw(bitmap,madmotor_pf1_tilemap,0);
	} };
}
