/*****************************************************************************

	Irem M92 video hardware, Bryan McPhail, mish@tendril.co.uk

	Brief Overview:

		3 scrolling playfields, 512 by 512.
		Each playfield can enable rowscroll, change shape (to 1024 by 512),
		be enabled/disabled and change position in VRAM.

		Tiles can have several priority values:
			0 = standard
			1 = Top 8 pens appear over sprites (split tilemap)
			2 = Whole tile appears over sprites
			3 = ?  Seems to be the whole tile is over sprites (as 2).

		Sprites have 2 priority values:
			0 = standard
			1 = Sprite appears over all tiles, including high priority pf1

		Raster interrupts can be triggered at any line of the screen redraw,
		typically used in games like R-Type Leo to multiplex the top playfield.

*****************************************************************************

	Master Control registers:

		Word 0:	Playfield 1 control
			Bit  0x40:  1 = Rowscroll enable, 0 = disable
			Bit  0x10:	0 = Playfield enable, 1 = disable
			Bit  0x04:  0 = 512 x 512 playfield, 1 = 1024 x 512 playfield
			Bits 0x03:  Playfield location in VRAM (0, 0x4000, 0x8000, 0xc000)
		Word 1: Playfield 2 control (as above)
		Word 2: Playfield 3 control (as above)
		Word 3: Raster IRQ position.

	The raster IRQ position is offset by 128+8 from the first visible line,
	suggesting there are 8 lines before the first visible one.

*****************************************************************************/

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
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.drivers.m92.*;
import static common.libc.cstring.*;

public class m92
{
	
	static tilemap pf3_wide_layer,pf3_layer,pf2_layer,pf1_wide_layer,pf1_layer,pf1_hlayer;
	static int[] pf1_control=new int[8];
        static int[] pf2_control=new int[8];
        static int[] pf3_control=new int[8];
        static int[] pf4_control=new int[8];
	static int pf1_vram_ptr,pf2_vram_ptr,pf3_vram_ptr;
	static int pf1_enable,pf2_enable,pf3_enable;
	static int pf1_rowscroll,pf2_rowscroll,pf3_rowscroll;
	static int pf1_shape,pf2_shape,pf3_shape;
	static int m92_sprite_list;
	
	public static int m92_raster_irq_position,m92_spritechip,m92_raster_machine,m92_raster_enable;
	public static UBytePtr m92_vram_data=new UBytePtr();
        public static UBytePtr m92_spritecontrol=new UBytePtr();
	public static int m92_game_kludge;
	
	/* This is a kludgey speedup that I consider to be pretty ugly..  But -
	it gets a massive speed increase (~40fps . ~65fps).  It works by avoiding
	the need to dirty the top playfield twice a frame */
	public static int RYPELEO_SPEEDUP() { return m92_game_kludge==1?1:0;}
	
	/*****************************************************************************/
	
	public static WriteHandlerPtr m92_spritecontrol_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		m92_spritecontrol.write(offset,data);
	
		/* The games using this sprite chip can autoclear spriteram */
		if (offset==8 && m92_spritechip==1) {
	//		if (errorlog != 0) fprintf(errorlog,"%04x: cleared spriteram\n",cpu_get_pc());
			buffer_spriteram_w.handler(0,0);
			memset(spriteram,0,0x800);
			m92_sprite_interrupt();
		}
	} };
	
	public static WriteHandlerPtr m92_spritebuffer_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	//	if (errorlog != 0) fprintf(errorlog,"%04x: buffered spriteram %d %d\n",cpu_get_pc(),offset,data);
		if (m92_spritechip==0 && offset==0) {
			buffer_spriteram_w.handler(0,0);
			m92_sprite_interrupt();
		}
	//	if (m92_spritechip==1)	memset(spriteram,0,0x800);
	} };
	
	/*****************************************************************************/
	
	public static WriteHandlerPtr get_pf1_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int offs,tile,color,pri;
		offs=(col*4) + (row*256);
		offs+=pf1_vram_ptr;
	
		tile=m92_vram_data.read(offs)+(m92_vram_data.read(offs+1)<<8);
		color=m92_vram_data.read(offs+2);
		SET_TILE_INFO(0,tile,color&0x3f);
	
		if ((m92_vram_data.read(offs+3)&1)!=0) pri = 2;
		else if ((color & 0x80) != 0) pri = 1;
		else pri = 0;
	
		tile_info.flags = (char)(TILE_FLIPYX((m92_vram_data.read(offs+3) & 0x6)>>1) | TILE_SPLIT(pri));
	} };
	
	public static WriteHandlerPtr get_pf1_htile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int offs,tile,color,pri;
		offs=(col*4) + (row*256);
		offs+=0xc000;
	
		tile=m92_vram_data.read(offs)+(m92_vram_data.read(offs+1)<<8);
		color=m92_vram_data.read(offs+2);
		SET_TILE_INFO(0,tile,color&0x3f);
	
		if ((m92_vram_data.read(offs+3)&1)!=0) pri = 2;
		else if ((color & 0x80) != 0) pri = 1;
		else pri = 0;
	
		tile_info.flags = (char)(TILE_FLIPYX((m92_vram_data.read(offs+3) & 0x6)>>1) | TILE_SPLIT(pri));
	} };
	
	public static WriteHandlerPtr get_pf1_ltile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int offs,tile,color,pri;
		offs=(col*4) + (row*256);
		if (pf1_vram_ptr==0x4000) offs+=0x4000;
		else if (pf1_vram_ptr==0x8000) offs+=0x8000;
	
		tile=m92_vram_data.read(offs)+(m92_vram_data.read(offs+1)<<8);
		color=m92_vram_data.read(offs+2);
		SET_TILE_INFO(0,tile,color&0x3f);
	
		if ((m92_vram_data.read(offs+3)&1)!=0) pri = 2;
		else if ((color & 0x80) != 0) pri = 1;
		else pri = 0;
	
		tile_info.flags = (char)(TILE_FLIPYX((m92_vram_data.read(offs+3) & 0x6)>>1) | TILE_SPLIT(pri));
	} };
	
	public static WriteHandlerPtr get_pf2_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int offs,tile,color,pri;
		offs=(col*4) + (row*256);
		offs+=pf2_vram_ptr;
	
		tile=m92_vram_data.read(offs)+(m92_vram_data.read(offs+1)<<8);
		color=m92_vram_data.read(offs+2);
		SET_TILE_INFO(0,tile,color&0x3f);
	
		if ((m92_vram_data.read(offs+3)&1)!=0) pri = 2;
		else if ((color & 0x80) != 0) pri = 1;
		else pri = 0;
	
		tile_info.flags = (char)(TILE_FLIPYX((m92_vram_data.read(offs+3) & 0x6)>>1) | TILE_SPLIT(pri));
	} };
	
	public static WriteHandlerPtr get_pf3_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int offs,tile,color,pri;
		offs=(col*4) + (row*256);
		offs+=pf3_vram_ptr;
		tile=m92_vram_data.read(offs)+(m92_vram_data.read(offs+1)<<8);
		color=m92_vram_data.read(offs+2);
	
		if ((color & 0x80) != 0) pri = 1;
		else pri = 0;
	
		SET_TILE_INFO(0,tile,color&0x3f);
		tile_info.flags = (char)(TILE_FLIPYX((m92_vram_data.read(offs+3) & 0x6)>>1) | TILE_SPLIT(pri));
	} };
	
	public static WriteHandlerPtr get_pf1_wide_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int offs,tile,color,pri;
		offs=(col*4) + (row*512);
		offs+=pf1_vram_ptr;
	
		tile=m92_vram_data.read(offs)+(m92_vram_data.read(offs+1)<<8);
		color=m92_vram_data.read(offs+2);
		SET_TILE_INFO(0,tile,color&0x3f);
	
		if ((m92_vram_data.read(offs+3)&1)!=0) pri = 2;
		else if ((color & 0x80) != 0) pri = 1;
		else pri = 0;
	
		tile_info.flags = (char)(TILE_FLIPYX((m92_vram_data.read(offs+3) & 0x6)>>1) | TILE_SPLIT(pri));
	} };
	
	public static WriteHandlerPtr get_pf3_wide_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int offs,tile,color,pri;
		offs=(col*4) + (row*512);
		offs+=pf3_vram_ptr;
	
		tile=m92_vram_data.read(offs)+(m92_vram_data.read(offs+1)<<8);
		color=m92_vram_data.read(offs+2);
	
		if ((color & 0x80) != 0) pri = 1;
		else pri = 0;
	
		SET_TILE_INFO(0,tile,color&0x3f);
		tile_info.flags = (char)(TILE_FLIPYX((m92_vram_data.read(offs+3) & 0x6)>>1) | TILE_SPLIT(pri));
	} };
	
	/*****************************************************************************/
	
	public static ReadHandlerPtr m92_vram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return m92_vram_data.read(offset);
	} };
	
	public static WriteHandlerPtr m92_vram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int a,wide;
	
		m92_vram_data.write(offset,data);
	
		/* Work out what area to dirty, potentially more than 1 */
		a=offset&0xc000;
		wide=offset&0x7fff;
		offset&=0x3fff;
	
		if (RYPELEO_SPEEDUP() != 0) {
			if (a==0xc000) {
				tilemap_mark_tile_dirty( pf1_hlayer,(offset/4)%64,(offset/4)/64 );
				return;
			}
			tilemap_mark_tile_dirty( pf1_layer,(offset/4)%64,(offset/4)/64 );
		}
		else
		if (a==pf1_vram_ptr || (a==pf1_vram_ptr+0x4000)) {
			tilemap_mark_tile_dirty( pf1_layer,(offset/4)%64,(offset/4)/64 );
			tilemap_mark_tile_dirty( pf1_wide_layer,(wide/4)%128,(wide/4)/128 );
		}
	
		if (a==pf2_vram_ptr)
			tilemap_mark_tile_dirty( pf2_layer,(offset/4)%64,(offset/4)/64 );
	
		if (a==pf3_vram_ptr || (a==pf3_vram_ptr+0x4000)) {
			tilemap_mark_tile_dirty( pf3_layer,(offset/4)%64,(offset/4)/64 );
			tilemap_mark_tile_dirty( pf3_wide_layer,(wide/4)%128,(wide/4)/128 );
		}
	} };
	
	/*****************************************************************************/
	
	public static WriteHandlerPtr m92_pf1_control_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		pf1_control[offset]=data;
	} };
	
	public static WriteHandlerPtr m92_pf2_control_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		pf2_control[offset]=data;
	} };
	
	public static WriteHandlerPtr m92_pf3_control_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		pf3_control[offset]=data;
	} };
	static int last_pf1_ptr,last_pf2_ptr,last_pf3_ptr;
	public static WriteHandlerPtr m92_master_control_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		
	
		pf4_control[offset]=data;
	
		switch (offset) {
			case 0: /* Playfield 1 (top layer) */
				if ((pf4_control[0]&0x10)==0x10) pf1_enable=0; else pf1_enable=1;
				if ((pf4_control[0]&0x40)==0x40) pf1_rowscroll=1; else pf1_rowscroll=0;
				pf1_vram_ptr=(pf4_control[0]&3)*0x4000;
				pf1_shape=(pf4_control[0]&4)>>2;
	
				if (RYPELEO_SPEEDUP() != 0) tilemap_set_enable(pf1_hlayer,pf1_enable);
				if (pf1_shape != 0) {
					tilemap_set_enable(pf1_layer,0);
					tilemap_set_enable(pf1_wide_layer,pf1_enable);
				}
				else {
					tilemap_set_enable(pf1_layer,pf1_enable);
					tilemap_set_enable(pf1_wide_layer,0);
				}
				/* Have to dirty tilemaps if vram pointer has changed */
				if (m92_game_kludge!=1 && last_pf1_ptr!=pf1_vram_ptr) {
					tilemap_mark_all_tiles_dirty(pf1_layer);
					tilemap_mark_all_tiles_dirty(pf1_wide_layer);
				}
				last_pf1_ptr=pf1_vram_ptr;
				break;
			case 2: /* Playfield 2 (middle layer) */
				if ((pf4_control[2]&0x10)==0x10) pf2_enable=0; else pf2_enable=1;
				if ((pf4_control[2]&0x40)==0x40) pf2_rowscroll=1; else pf2_rowscroll=0;
				tilemap_set_enable(pf2_layer,pf2_enable);
				pf2_vram_ptr=(pf4_control[2]&3)*0x4000;
				pf2_shape=(pf4_control[2]&4)>>2;
				if (last_pf2_ptr!=pf2_vram_ptr)
					tilemap_mark_all_tiles_dirty(pf2_layer);
				last_pf2_ptr=pf2_vram_ptr;
				break;
			case 4: /* Playfield 3 (bottom layer) */
				if ((pf4_control[4]&0x10)==0x10) pf3_enable=0; else pf3_enable=1;
				if ((pf4_control[4]&0x40)==0x40) pf3_rowscroll=1; else pf3_rowscroll=0;
				pf3_shape=(pf4_control[4]&4)>>2;
	
				if (pf3_shape != 0) {
					tilemap_set_enable(pf3_layer,0);
					tilemap_set_enable(pf3_wide_layer,pf3_enable);
				}
				else {
					tilemap_set_enable(pf3_layer,pf3_enable);
					tilemap_set_enable(pf3_wide_layer,0);
				}
				pf3_vram_ptr=(pf4_control[4]&3)*0x4000;
				if (last_pf3_ptr!=pf3_vram_ptr) {
					tilemap_mark_all_tiles_dirty(pf3_layer);
					tilemap_mark_all_tiles_dirty(pf3_wide_layer);
				}
				last_pf3_ptr=pf3_vram_ptr;
				break;
			case 6:
			case 7:
				m92_raster_irq_position=((pf4_control[7]<<8) | pf4_control[6])-128;
				/*if (!m92_raster_machine && m92_raster_enable && m92_raster_irq_position>128)
					usrintf_showmessage("WARNING!  RASTER IRQ ON NON-RASTER MACHINE DRIVER!");*/
				break;
		}
	} };
	
	/*****************************************************************************/
	
	public static VhStartHandlerPtr m92_vh_start = new VhStartHandlerPtr() { public int handler() 
	{
		if (RYPELEO_SPEEDUP() != 0) {
			pf1_hlayer = tilemap_create(
				get_pf1_htile_info,
				TILEMAP_TRANSPARENT | TILEMAP_SPLIT,
				8,8,
				64,64
			);
			pf1_layer = tilemap_create(
					get_pf1_ltile_info,
					TILEMAP_TRANSPARENT | TILEMAP_SPLIT,
					8,8,
					64,64
				);
			tilemap_set_scroll_cols(pf1_hlayer,1);
			pf1_hlayer.transmask[0] = 0xffff;
			pf1_hlayer.transmask[1] = 0x00ff;
			pf1_hlayer.transmask[2] = 0x0001;
			pf1_hlayer.transparent_pen = 0;
		}
		else
			pf1_layer = tilemap_create(
				get_pf1_tile_info,
				TILEMAP_TRANSPARENT | TILEMAP_SPLIT,
				8,8,
				64,64
			);
	
		pf2_layer = tilemap_create(
			get_pf2_tile_info,
			TILEMAP_TRANSPARENT | TILEMAP_SPLIT,
			8,8,
			64,64
		);
	
		pf3_layer = tilemap_create(
			get_pf3_tile_info,
			TILEMAP_SPLIT,
			8,8,
			64,64
		);
	
		pf1_wide_layer = tilemap_create(
			get_pf1_wide_tile_info,
			TILEMAP_TRANSPARENT | TILEMAP_SPLIT,
			8,8,
			128,64
		);
	
		pf3_wide_layer = tilemap_create(
			get_pf3_wide_tile_info,
			TILEMAP_SPLIT,
			8,8,
			128,64
		);
	
		pf1_layer.transparent_pen = 0;
		pf2_layer.transparent_pen = 0;
		pf1_wide_layer.transparent_pen = 0;
		/* split type 0 - totally transparent in front half */
		pf1_layer.transmask[0] = 0xffff;
		pf2_layer.transmask[0] = 0xffff;
		pf3_layer.transmask[0] = 0xffff;
		pf1_wide_layer.transmask[0] = 0xffff;
		pf3_wide_layer.transmask[0] = 0xffff;
		/* split type 1 - pens 0-7 transparent in front half */
		pf1_layer.transmask[1] = 0x00ff;
		pf2_layer.transmask[1] = 0x00ff;
		pf3_layer.transmask[1] = 0x00ff;
		pf1_wide_layer.transmask[1] = 0x00ff;
		pf3_wide_layer.transmask[1] = 0x00ff;
		/* split type 2 - pen 0 transparent in front half */
		pf1_layer.transmask[2] = 0x0001;
		pf2_layer.transmask[2] = 0x0001;
		pf3_layer.transmask[2] = 0x0001;
		pf1_wide_layer.transmask[2] = 0x0001;
		pf3_wide_layer.transmask[2] = 0x0001;
	
		tilemap_set_scroll_cols(pf1_layer,1);
		tilemap_set_scroll_cols(pf2_layer,1);
		tilemap_set_scroll_cols(pf3_layer,1);
		tilemap_set_scroll_cols(pf1_wide_layer,1);
		tilemap_set_scroll_cols(pf3_wide_layer,1);
	
		pf1_vram_ptr=pf2_vram_ptr=pf3_vram_ptr=0;
		pf1_enable=pf2_enable=pf3_enable=0;
		pf1_rowscroll=pf2_rowscroll=pf3_rowscroll=0;
		pf1_shape=pf2_shape=pf3_shape=0;
	
		m92_sprite_list=0;
		memset(spriteram,0,0x800);
		memset(buffered_spriteram,0,0x800);
	
		return 0;
	} };
	
	public static VhStopHandlerPtr m92_vh_stop = new VhStopHandlerPtr() { public void handler() 
	{
		/* Nothing */
	} };
	
	/*****************************************************************************/
	
	static void mark_sprite_colours()
	{
		int offs,color,i,j,pal_base;
                        int[] colmask=new int[64];
	    int[] pen_usage; /* Save some struct derefs */
		int sprite_mask;
	
		sprite_mask=(Machine.drv.gfxdecodeinfo[1].gfxlayout.total)-1;
	
		pal_base = Machine.drv.gfxdecodeinfo[1].color_codes_start;
		pen_usage=Machine.gfx[1].pen_usage;
		for (color = 0;color < 64;color++) colmask[color] = 0;
	
		if (m92_spritechip==0)
			m92_sprite_list=(((0x100 - m92_spritecontrol.read(0))&0xff)*8)-8;
		for (offs = m92_sprite_list;offs >= 0;offs -= 8)
		{
			int sprite,x_multi,y_multi,x,y,s_ptr;
	
			/* Save colours by skipping offscreen sprites */
			y=(buffered_spriteram.read(offs+0) | (buffered_spriteram.read(offs+1)<<8))&0x1ff;
			x=(buffered_spriteram.read(offs+6) | (buffered_spriteram.read(offs+7)<<8))&0x1ff;
			if (x==0 || y==0) continue;
	
		    sprite=buffered_spriteram.read(offs+2) | (buffered_spriteram.read(offs+3)<<8);
			color=buffered_spriteram.read(offs+4)&0x3f;
	
			y_multi=(buffered_spriteram.read(offs+1)>>1)&0x3;
			x_multi=(buffered_spriteram.read(offs+1)>>3)&0x3;
	
			y_multi=1 << y_multi; /* 1, 2, 4 or 8 */
			x_multi=1 << x_multi; /* 1, 2, 4 or 8 */
	
			for (j=0; j<x_multi; j++)
			{
				s_ptr=8 * j;
				for (i=0; i<y_multi; i++)
				{
					colmask[color] |= pen_usage[(sprite + s_ptr)&sprite_mask];
					s_ptr++;
				}
			}
		}
	
		for (color = 0;color < 64;color++)
		{
			for (i = 1;i < 16;i++)
			{
				if ((colmask[color] & (1 << i))!=0)
					palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
			}
		}
	}
	
	static void m92_drawsprites(osd_bitmap bitmap, rectangle clip, int pri)
	{
		int offs;
	
		/* The sprite list is now drawn *in reverse* from the control length */
		for (offs = m92_sprite_list;offs >= 0;offs -= 8) {
			int x,y,sprite,colour,fx,fy,x_multi,y_multi,i,j,s_ptr;
	
			if (((buffered_spriteram.read(offs+4)&0x80)==0x80) && pri==0) continue;
			if (((buffered_spriteram.read(offs+4)&0x80)==0x00) && pri==1) continue;
	
			y=(buffered_spriteram.read(offs+0)| (buffered_spriteram.read(offs+1)<<8))&0x1ff;
			x=(buffered_spriteram.read(offs+6) | (buffered_spriteram.read(offs+7)<<8))&0x1ff;
			if (x==0 || y==0) continue; /* offscreen */
	
			x = x - 16;
			y = 512 - 16 - y;
	
		    sprite=(buffered_spriteram.read(offs+2) | (buffered_spriteram.read(offs+3)<<8));
			colour=buffered_spriteram.read(offs+4)&0x3f;
	
			fx=buffered_spriteram.read(offs+5)&1;
			fy=buffered_spriteram.read(offs+5)&2;
			y_multi=(buffered_spriteram.read(offs+1)>>1)&0x3;
			x_multi=(buffered_spriteram.read(offs+1)>>3)&0x3;
	
			y_multi=1 << y_multi; /* 1, 2, 4 or 8 */
			x_multi=1 << x_multi; /* 1, 2, 4 or 8 */
	
			if (fx!=0 && x_multi>1) x+=16;
			for (j=0; j<x_multi; j++)
			{
				s_ptr=8 * j;
				if (fy==0) s_ptr+=y_multi-1;
	
				for (i=0; i<y_multi; i++)
				{
					drawgfx(bitmap,Machine.gfx[1],
							sprite + s_ptr,
							colour,
							fx,fy,
							x,y-i*16,
							clip,TRANSPARENCY_PEN,0);
					if (fy != 0) s_ptr++; else s_ptr--;
				}
				if (fx != 0) x-=16; else x+=16;
			}
		}
	}
	
	/*****************************************************************************/
	
	public static VhUpdateHandlerPtr m92_vh_screenrefresh = new VhUpdateHandlerPtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		/* Screen refresh is handled by raster interrupt routine, here
			we just check the keyboard */
		/*if (keyboard_pressed_memory(KEYCODE_F1)) {
			m92_raster_enable ^= 1;
			if (m92_raster_enable != 0)
				usrintf_showmessage("Raster IRQ enabled");
			else
				usrintf_showmessage("Raster IRQ disabled");
		}*/
	} };
	
	static void m92_update_scroll_positions()
	{
		int i;
	
		/*	Playfield 3 rowscroll data is 0xdfc00 - 0xdffff
			Playfield 2 rowscroll data is 0xdf800 - 0xdfbff
			Playfield 1 rowscroll data is 0xdf400 - 0xdf7ff
	
			It appears to be hardwired to those locations.
	
			In addition, each playfield is staggered 2 pixels horizontally from the
			previous one.  This is most obvious in Hook & Blademaster.
	
		*/
	
		if (pf1_rowscroll != 0) {
			tilemap_set_scroll_rows(pf1_layer,512);
			tilemap_set_scroll_rows(pf1_wide_layer,512);
			for (i=0; i<1024; i+=2)
				tilemap_set_scrollx( pf1_layer,i/2, (m92_vram_data.read(0xf400+i)+(m92_vram_data.read(0xf401+i)<<8)));
			for (i=0; i<1024; i+=2)
				tilemap_set_scrollx( pf1_wide_layer,i/2, (m92_vram_data.read(0xf400+i)+(m92_vram_data.read(0xf401+i)<<8))+256);
		} else {
			tilemap_set_scroll_rows(pf1_layer,1);
			tilemap_set_scroll_rows(pf1_wide_layer,1);
			tilemap_set_scrollx( pf1_layer,0, (pf1_control[5]<<8)+pf1_control[4] );
			tilemap_set_scrollx( pf1_wide_layer,0, (pf1_control[5]<<8)+pf1_control[4]+256 );
		}
	
		if (pf2_rowscroll != 0) {
			tilemap_set_scroll_rows(pf2_layer,512);
			for (i=0; i<1024; i+=2)
				tilemap_set_scrollx( pf2_layer,i/2, (m92_vram_data.read(0xf800+i)+(m92_vram_data.read(0xf801+i)<<8))-2);
		} else {
			tilemap_set_scroll_rows(pf2_layer,1);
			tilemap_set_scrollx( pf2_layer,0, (pf2_control[5]<<8)+pf2_control[4]-2 );
		}
	
		if (pf3_rowscroll != 0) {
			tilemap_set_scroll_rows(pf3_layer,512);
			for (i=0; i<1024; i+=2)
				tilemap_set_scrollx( pf3_layer,i/2, (m92_vram_data.read(0xfc00+i)+(m92_vram_data.read(0xfc01+i)<<8))-4);
			tilemap_set_scroll_rows(pf3_wide_layer,512);
			for (i=0; i<1024; i+=2)
				tilemap_set_scrollx( pf3_wide_layer,i/2, (m92_vram_data.read(0xfc00+i)+(m92_vram_data.read(0xfc01+i)<<8))-4+256);
		} else {
			tilemap_set_scroll_rows(pf3_layer,1);
			tilemap_set_scrollx( pf3_layer,0, (pf3_control[5]<<8)+pf3_control[4]-4 );
			tilemap_set_scroll_rows(pf3_wide_layer,1);
			tilemap_set_scrollx( pf3_wide_layer,0, (pf3_control[5]<<8)+pf3_control[4]-4+256 );
		}
	
		tilemap_set_scrolly( pf1_layer,0, (pf1_control[1]<<8)+pf1_control[0] );
		tilemap_set_scrolly( pf2_layer,0, (pf2_control[1]<<8)+pf2_control[0] );
		tilemap_set_scrolly( pf3_layer,0, (pf3_control[1]<<8)+pf3_control[0] );
		tilemap_set_scrolly( pf1_wide_layer,0, (pf1_control[1]<<8)+pf1_control[0] );
		tilemap_set_scrolly( pf3_wide_layer,0, (pf3_control[1]<<8)+pf3_control[0] );
	
		if (m92_spritechip==1)
			m92_sprite_list=0x800-8;
	
		if (RYPELEO_SPEEDUP() != 0) {
			tilemap_set_scroll_rows(pf1_hlayer,1);
			tilemap_set_scrollx( pf1_hlayer,0, (pf1_control[5]<<8)+pf1_control[4] );
			tilemap_set_scrolly( pf1_hlayer,0, (pf1_control[1]<<8)+pf1_control[0] );
			pf1_hlayer.scrolled=1;
		}
	
		pf1_wide_layer.scrolled=1;
		pf3_wide_layer.scrolled=1;
		pf3_layer.scrolled=1;
		pf2_layer.scrolled=1;
		pf1_layer.scrolled=1;
	}
	
	/*****************************************************************************/
	
	public static void m92_screenrefresh(osd_bitmap bitmap,rectangle clip)
	{
		if (pf3_shape != 0) /* Updating all tilemaps causes palette overflow */
			tilemap_update(pf3_wide_layer);
		else
			tilemap_update(pf3_layer);
		tilemap_update(pf2_layer);
		if (pf1_shape != 0)
			tilemap_update(pf1_wide_layer);
		else
			tilemap_update(pf1_layer);
	
		if (RYPELEO_SPEEDUP() != 0) tilemap_update(pf1_hlayer);
	
		/* This should be done once a frame only... but we can almost get away with it */
		palette_init_used_colors();
		mark_sprite_colours();
		if (palette_recalc()!=null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		if (pf3_enable != 0) {
			tilemap_draw(bitmap,pf3_wide_layer,TILEMAP_BACK);
			tilemap_draw(bitmap,pf3_layer,TILEMAP_BACK);
		}
		else
			fillbitmap(bitmap,palette_transparent_pen,clip);
	
		tilemap_draw(bitmap,pf2_layer,TILEMAP_BACK);
		tilemap_draw(bitmap,pf1_wide_layer,TILEMAP_BACK);
		if (RYPELEO_SPEEDUP()!=0 && pf1_vram_ptr==0xc000)
			tilemap_draw(bitmap,pf1_hlayer,TILEMAP_BACK);
		else
			tilemap_draw(bitmap,pf1_layer,TILEMAP_BACK);
	
		m92_drawsprites(bitmap,clip,0);
	
		tilemap_draw(bitmap,pf3_wide_layer,TILEMAP_FRONT);
		tilemap_draw(bitmap,pf3_layer,TILEMAP_FRONT);
		tilemap_draw(bitmap,pf2_layer,TILEMAP_FRONT);
		tilemap_draw(bitmap,pf1_wide_layer,TILEMAP_FRONT);
		if (RYPELEO_SPEEDUP()!=0 && pf1_vram_ptr==0xc000)
			tilemap_draw(bitmap,pf1_hlayer,TILEMAP_FRONT);
		else
			tilemap_draw(bitmap,pf1_layer,TILEMAP_FRONT);
	
		m92_drawsprites(bitmap,clip,1); /* These sprites are over all playfields */
	}
	
	public static void m92_vh_raster_partial_refresh(osd_bitmap bitmap,int start_line,int end_line)
	{
		rectangle clip=new rectangle();
	
		clip.min_x = Machine.drv.visible_area.min_x;
		clip.max_x = Machine.drv.visible_area.max_x;
		clip.min_y = start_line+128;
		clip.max_y = end_line+128;
		if (clip.min_y < Machine.drv.visible_area.min_y)
			clip.min_y = Machine.drv.visible_area.min_y;
		if (clip.max_y > Machine.drv.visible_area.max_y)
			clip.max_y = Machine.drv.visible_area.max_y;
	
		if (clip.max_y > clip.min_y)
		{
			m92_update_scroll_positions();
			if (RYPELEO_SPEEDUP() != 0) tilemap_set_clip(pf1_hlayer,clip);
			tilemap_set_clip(pf1_layer,clip);
			tilemap_set_clip(pf2_layer,clip);
			tilemap_set_clip(pf3_layer,clip);
			tilemap_set_clip(pf1_wide_layer,clip);
			tilemap_set_clip(pf3_wide_layer,clip);
			m92_screenrefresh(bitmap,clip);
		}
	}
}
