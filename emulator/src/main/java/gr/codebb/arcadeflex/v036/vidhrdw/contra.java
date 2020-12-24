/***************************************************************************

  gryzor: vidhrdw.c

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.vidhrdw;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
public class contra
{
	
	//static int spriteram_offset;
	public static UBytePtr private_spriteram_2,private_spriteram;
	
	public static UBytePtr contra_fg_vram=new UBytePtr();
        public static UBytePtr contra_fg_cram=new UBytePtr();
	public static UBytePtr contra_text_vram=new UBytePtr();
        public static UBytePtr contra_text_cram=new UBytePtr();
	public static UBytePtr contra_bg_vram=new UBytePtr();
        public static UBytePtr contra_bg_cram=new UBytePtr();
	
	static tilemap bg_tilemap, fg_tilemap, text_tilemap;
	
	/***************************************************************************
	**
	**	Contra has palette RAM, but it also has four lookup table PROMs
	**
	**	0	sprites #0
	**	1	tiles   #0
	**	2	sprites #1
	**	3	tiles   #1
	**
	***************************************************************************/
	
	public static VhConvertColorPromPtr contra_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i,chip,pal,clut;
                int c_ptr=0;
		for (chip = 0;chip < 2;chip++)
		{
			for (pal = 0;pal < 8;pal++)
			{
				clut = (pal & 1) + 2 * chip;
                                
				for (i = 0;i < 256;i++)
				{
					if ((pal & 1) == 0)	/* sprites */
					{
						if (color_prom.read(256 * clut + i) == 0)
							colortable[c_ptr++]= 0;
						else
							colortable[c_ptr++]= (char)(16 * pal + color_prom.read(256 * clut + i));
					}
					else
						colortable[c_ptr++]= (char)(16 * pal + color_prom.read(256 * clut + i));
				}
			}
		}
	} };
	
	/***************************************************************************
	**
	**	Tilemap Manager Callbacks
	**
	***************************************************************************/
	
	public static WriteHandlerPtr get_fg_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int offs = row*32 + col;
		int attr = contra_fg_cram.read(offs);
		int bit0 = (K007121_ctrlram[0][0x05] >> 0) & 0x03;
		int bit1 = (K007121_ctrlram[0][0x05] >> 2) & 0x03;
		int bit2 = (K007121_ctrlram[0][0x05] >> 4) & 0x03;
		int bit3 = (K007121_ctrlram[0][0x05] >> 6) & 0x03;
		int bank = ((attr & 0x80) >> 7) |
				((attr >> (bit0+2)) & 0x02) |
				((attr >> (bit1+1)) & 0x04) |
				((attr >> (bit2  )) & 0x08) |
				((attr >> (bit3-1)) & 0x10) |
				((K007121_ctrlram[0][0x03] & 0x01) << 5);
		int mask = (K007121_ctrlram[0][0x04] & 0xf0) >> 4;
	
		bank = (bank & ~(mask << 1)) | ((K007121_ctrlram[0][0x04] & mask) << 1);
	
		SET_TILE_INFO(0, contra_fg_vram.read(offs)+bank*256, ((K007121_ctrlram[0][6]&0x30)*2+16)+(attr&7) );
	} };
	
	public static WriteHandlerPtr get_bg_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int offs = row*32 + col;
		int attr = contra_bg_cram.read(offs);
		int bit0 = (K007121_ctrlram[1][0x05] >> 0) & 0x03;
		int bit1 = (K007121_ctrlram[1][0x05] >> 2) & 0x03;
		int bit2 = (K007121_ctrlram[1][0x05] >> 4) & 0x03;
		int bit3 = (K007121_ctrlram[1][0x05] >> 6) & 0x03;
		int bank = ((attr & 0x80) >> 7) |
				((attr >> (bit0+2)) & 0x02) |
				((attr >> (bit1+1)) & 0x04) |
				((attr >> (bit2  )) & 0x08) |
				((attr >> (bit3-1)) & 0x10) |
				((K007121_ctrlram[1][0x03] & 0x01) << 5);
		int mask = (K007121_ctrlram[1][0x04] & 0xf0) >> 4;
	
		bank = (bank & ~(mask << 1)) | ((K007121_ctrlram[0][0x04] & mask) << 1);
	
		SET_TILE_INFO(1, contra_bg_vram.read(offs)+bank*256, ((K007121_ctrlram[1][6]&0x30)*2+16)+(attr&7) );
	} };
	
	public static WriteHandlerPtr get_text_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int offs = row*32 + col;
		int attr = contra_text_cram.read(offs);
		int bit0 = (K007121_ctrlram[0][0x05] >> 0) & 0x03;
		int bit1 = (K007121_ctrlram[0][0x05] >> 2) & 0x03;
		int bit2 = (K007121_ctrlram[0][0x05] >> 4) & 0x03;
		int bit3 = (K007121_ctrlram[0][0x05] >> 6) & 0x03;
		int bank = ((attr & 0x80) >> 7) |
				((attr >> (bit0+2)) & 0x02) |
				((attr >> (bit1+1)) & 0x04) |
				((attr >> (bit2  )) & 0x08) |
				((attr >> (bit3-1)) & 0x10);
		SET_TILE_INFO(0,contra_text_vram.read(offs)+bank*256, ((K007121_ctrlram[0][6]&0x30)*2+16)+(attr&7)) ;
	} };
	
	/***************************************************************************
	**
	**	Memory Write Handlers
	**
	***************************************************************************/
	
	public static WriteHandlerPtr contra_fg_vram_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		if (contra_fg_vram.read(offset) != data){
			tilemap_mark_tile_dirty( fg_tilemap, offset%32, offset/32 );
			contra_fg_vram.write(offset,data);
		}
	} };
	
	public static WriteHandlerPtr contra_fg_cram_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		if (contra_fg_cram.read(offset) != data){
			tilemap_mark_tile_dirty( fg_tilemap, offset%32, offset/32 );
			contra_fg_cram.write(offset,data);
		}
	} };
	
	public static WriteHandlerPtr contra_bg_vram_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		if (contra_bg_vram.read(offset) != data){
			tilemap_mark_tile_dirty( bg_tilemap, offset%32, offset/32 );
			contra_bg_vram.write(offset,data);
		}
	} };
	
	public static WriteHandlerPtr contra_bg_cram_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		if (contra_bg_cram.read(offset) != data){
			tilemap_mark_tile_dirty( bg_tilemap, offset%32, offset/32 );
			contra_bg_cram.write(offset,data);
		}
	} };
	
	public static WriteHandlerPtr contra_text_vram_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		if (contra_text_vram.read(offset) != data){
			tilemap_mark_tile_dirty( text_tilemap, offset%32, offset/32 );
			contra_text_vram.write(offset,data);
		}
	} };
	
	public static WriteHandlerPtr contra_text_cram_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		if (contra_text_cram.read(offset) != data){
			tilemap_mark_tile_dirty( text_tilemap, offset%32, offset/32 );
			contra_text_cram.write(offset,data);
		}
	} };
	
	public static WriteHandlerPtr contra_K007121_ctrl_0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (offset == 3)
		{
			if ((data&0x8)==0)
				memcpy(private_spriteram,new UBytePtr(spriteram,0x800),0x800);
			else
				memcpy(private_spriteram,spriteram,0x800);
		}
		if (offset == 6)
		{
			if (K007121_ctrlram[0][6] != data)
				tilemap_mark_all_tiles_dirty( fg_tilemap );
		}
		if (offset == 7)
			tilemap_set_flip(fg_tilemap,(data & 0x08)!=0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
	
		K007121_ctrl_0_w.handler(offset,data);
	} };
	
	public static WriteHandlerPtr contra_K007121_ctrl_1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (offset == 3)
		{
			if ((data&0x8)==0)
				memcpy(private_spriteram_2,new UBytePtr(spriteram,0x2800),0x800);
			else
				memcpy(private_spriteram_2,new UBytePtr(spriteram,0x2000),0x800);
		}
		if (offset == 6)
		{
			if (K007121_ctrlram[1][6] != data )
				tilemap_mark_all_tiles_dirty( bg_tilemap );
		}
		if (offset == 7)
			tilemap_set_flip(bg_tilemap,(data & 0x08)!=0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
	
		K007121_ctrl_1_w.handler(offset,data);
	} };
	
	/***************************************************************************
	**
	**	Video Driver Initialization
	**
	***************************************************************************/
	
	public static VhStartPtr contra_vh_start = new VhStartPtr() { public int handler() {
		bg_tilemap = tilemap_create(
			get_bg_tile_info,
			TILEMAP_OPAQUE,
			8,8,	/* tile width, tile height */
			32,32	/* number of columns, number of rows */
		);
	
		fg_tilemap = tilemap_create(
			get_fg_tile_info,
			TILEMAP_TRANSPARENT,
			8,8,	/* tile width, tile height */
			32,32	/* number of columns, number of rows */
		);
	
		text_tilemap = tilemap_create(
			get_text_tile_info,
			TILEMAP_OPAQUE,
			8,8,	/* tile width, tile height */
			32,32	/* number of columns, number of rows */
		);
	
		private_spriteram=new UBytePtr(0x800);
		private_spriteram_2=new UBytePtr(0x800);
	
		if( bg_tilemap!=null && fg_tilemap!=null && text_tilemap!=null ){
			rectangle clip = new rectangle(Machine.drv.visible_area);
			clip.min_x += 40;
			tilemap_set_clip( bg_tilemap, clip );
			tilemap_set_clip( fg_tilemap, clip );
	
			clip.max_x = 39;
			clip.min_x = 0;
			tilemap_set_clip( text_tilemap, clip );
	
			fg_tilemap.transparent_pen = 0;
	
			return 0;
		}
	
		return 1;
	} };
	
	public static VhStopPtr contra_vh_stop = new VhStopPtr() { public void handler() 
	{
		private_spriteram=null;
		private_spriteram_2=null;
	} };
	
	static void draw_sprites( osd_bitmap bitmap, int bank )
	{
		UBytePtr source;
		int base_color = (K007121_ctrlram[bank][6]&0x30)*2;
	
		if (bank==0) source=new UBytePtr(private_spriteram);
		else source=new UBytePtr(private_spriteram_2);
	
		K007121_sprites_draw(bank,bitmap,new UBytePtr(source),base_color,40,0);
	}
	
	public static VhUpdatePtr contra_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_set_scrollx( fg_tilemap,0, K007121_ctrlram[0][0x00] - 40 );
		tilemap_set_scrolly( fg_tilemap,0, K007121_ctrlram[0][0x02] );
		tilemap_set_scrollx( bg_tilemap,0, K007121_ctrlram[1][0x00] - 40 );
		tilemap_set_scrolly( bg_tilemap,0, K007121_ctrlram[1][0x02] );
	
		tilemap_update( ALL_TILEMAPS );
		if (palette_recalc()!=null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
		tilemap_render( ALL_TILEMAPS );
	
		tilemap_draw( bitmap, bg_tilemap, 0 );
		tilemap_draw( bitmap, fg_tilemap, 0 );
		draw_sprites( bitmap, 0 );
		draw_sprites( bitmap, 1 );
		tilemap_draw( bitmap, text_tilemap, 0 );
	} };
}
