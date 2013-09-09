/*
 * ported to v0.36
 * using automatic conversion tool v0.08 + manual fixes
 *
 *
 *
 */ 
package vidhrdw;

import static arcadeflex.libc.*;
import static arcadeflex.libc_old.*;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static vidhrdw.generic.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.mame.*;
import static arcadeflex.video.*;
import static mame.commonH.*;
import static mame.common.*;

public class snk
{
	
	public static int snk_bg_tilemap_baseaddr, gwar_sprite_placement;
	
	public static final int MAX_VRAM_SIZE= (64*64*2);
	
	//static int k = 0; /*for debugging use */
	
	static int shadows_visible = 0; /* toggles rapidly to fake translucency in ikari warriors */
	
	public static void print(osd_bitmap bitmap, int num, int row ){
		char[] digit = { '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	
		drawgfx( bitmap,Machine.uifont,
			digit[(num>>4)&0xf],
			0,
			0,0, /* no flip */
			24,row*8+8,
			null,TRANSPARENCY_NONE,0);
		drawgfx( bitmap,Machine.uifont,
			digit[num&0xf],
			0,
			0,0, /* no flip */
			32,row*8+8,
			null,TRANSPARENCY_NONE,0);
	}
	
	public static final int GFX_CHARS			=0;
	public static final int GFX_TILES			=1;
	public static final int GFX_SPRITES			=2;
	public static final int GFX_BIGSPRITES			=3;
	
	public static VhConvertColorPromPtr snk_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(UByte []palette, char []colortable, UBytePtr color_prom) {
		int i;
		int num_colors = 1024;
                int p_inc=0;
		for( i=0; i<num_colors; i++ ){
			int bit0,bit1,bit2,bit3;
	
			colortable[i] = (char)i;
	
			bit0 = (color_prom.read(0) >> 0) & 0x01;
			bit1 = (color_prom.read(0) >> 1) & 0x01;
			bit2 = (color_prom.read(0) >> 2) & 0x01;
			bit3 = (color_prom.read(0) >> 3) & 0x01;
			palette[p_inc++].set((char)(0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));
	
			bit0 = (color_prom.read(num_colors) >> 0) & 0x01;
			bit1 = (color_prom.read(num_colors) >> 1) & 0x01;
			bit2 = (color_prom.read(num_colors) >> 2) & 0x01;
			bit3 = (color_prom.read(num_colors) >> 3) & 0x01;
			palette[p_inc++].set((char)(0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));
	
			bit0 = (color_prom.read(2*num_colors) >> 0) & 0x01;
			bit1 = (color_prom.read(2*num_colors) >> 1) & 0x01;
			bit2 = (color_prom.read(2*num_colors) >> 2) & 0x01;
			bit3 = (color_prom.read(2*num_colors) >> 3) & 0x01;
			palette[p_inc++].set((char)(0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));
	
			color_prom.inc();
		}
	} };
	
/*TODO*///void ikari_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom){
/*TODO*///	int i;
/*TODO*///	snk_vh_convert_color_prom( palette, colortable, color_prom);
/*TODO*///
/*TODO*///	palette += 6*3;
/*TODO*///	/*
/*TODO*///		pen#6 is used for translucent shadows;
/*TODO*///		we'll just make it dark grey for now
/*TODO*///	*/
/*TODO*///	for( i=0; i<256; i+=8 ){
/*TODO*///		palette[i*3+0] = palette[i*3+1] = palette[i*3+2] = 14;
/*TODO*///	}
/*TODO*///}
	
	public static VhStartPtr snk_vh_start = new VhStartPtr() { public int handler() 
	{
		dirtybuffer = new char[MAX_VRAM_SIZE];
		if (dirtybuffer != null){
			tmpbitmap = osd_new_bitmap( 512, 512, Machine.scrbitmap.depth );
			if (tmpbitmap != null){
				memset( dirtybuffer, 0xff, MAX_VRAM_SIZE  );
				shadows_visible = 1;
				return 0;
			}
			dirtybuffer=null;
		}
		return 1;
	}};
	public static VhStopPtr snk_vh_stop = new VhStopPtr() {public void handler() 
        {
                
		osd_free_bitmap( tmpbitmap );
		tmpbitmap = null;
		dirtybuffer = null;
	}};
	
	/**************************************************************************************/
	
	static void tnk3_draw_background(osd_bitmap bitmap, int scrollx, int scrolly )
        {
		rectangle clip = Machine.drv.visible_area;
		GfxElement gfx = Machine.gfx[GFX_TILES];
		int offs;
		for( offs=0; offs<64*64*2; offs+=2 ){
			int tile_number = videoram.read(offs);
			/*unsigned*/ char attributes = (char)(videoram.read(offs+1) & 0xFF);
	
			if( tile_number!=dirtybuffer[offs] || attributes != dirtybuffer[offs+1] ){
				int sy = ((offs/2)%64)*8;
				int sx = ((offs/2)/64)*8;
				int color = (attributes&0xf)^0x8;
	
				dirtybuffer[offs] = (char)tile_number;
				dirtybuffer[offs+1] = attributes;
	
				tile_number += 256*((attributes>>4)&0x3);
	
				drawgfx( tmpbitmap,gfx,
					tile_number,
					color,
					0,0, /* no flip */
					sx,sy,
					null,TRANSPARENCY_NONE,0);
			}
		}
		{
			copyscrollbitmap(bitmap,tmpbitmap,
				1,new int[]{scrollx},1,new int[]{scrolly},
				clip,
				TRANSPARENCY_NONE,0);
		}
	}
	
	static void tnk3_draw_text(osd_bitmap bitmap, int bank, UBytePtr source ){
		GfxElement gfx = Machine.gfx[GFX_CHARS];
		int offs;
	
		bank*=256;
	
		for( offs = 0;offs <0x400; offs++ ){
			drawgfx( bitmap, gfx,
				source.read(offs)+bank,
				source.read(offs)>>5,
				0,0, /* no flip */
				16+(offs/32)*8,(offs%32)*8+8,
				null,
				TRANSPARENCY_PEN,15 );
		}
	}
	
	static void tnk3_draw_status(osd_bitmap bitmap, int bank, UBytePtr source ){
		rectangle clip = Machine.drv.visible_area;
		GfxElement gfx = Machine.gfx[GFX_CHARS];
		int offs;
	
		bank *= 256;
	
		for( offs = 0; offs<64; offs++ ){
			int tile_number = source.read(offs+30*32);
			int sy = (offs % 32)*8+8;
			int sx = (offs / 32)*8;
	
			drawgfx(bitmap,gfx,
				tile_number+bank,
				tile_number>>5,
				0,0, /* no flip */
				sx,sy,
				clip,TRANSPARENCY_NONE,0);
	
			tile_number = source.read(offs);
			sx += 34*8;
	
			drawgfx(bitmap,gfx,
				tile_number+bank,
				tile_number>>5,
				0,0, /* no flip */
				sx,sy,
				clip,TRANSPARENCY_NONE,0);
		}
	}
	
	public static void tnk3_draw_sprites(osd_bitmap bitmap, int xscroll, int yscroll ){
		int n = 50;
		CharPtr source = new CharPtr(spriteram,0);
		CharPtr finish = new CharPtr(source,n*4);
		rectangle clip = Machine.drv.visible_area;

	
		while( source.base<finish.base){
			int attributes = source.read(3); /* YBBX.CCCC */
			int tile_number = source.read(1);
			int sy = source.read(0) + ((attributes&0x10)!=0?256:0) - yscroll;
			int sx = source.read(2) + ((attributes&0x80)!=0?256:0) - xscroll;
			int color = attributes&0xf;
	
			if ((attributes & 0x40) != 0) tile_number += 256;
			if ((attributes & 0x20) != 0) tile_number += 512;
	
			drawgfx(bitmap,Machine.gfx[GFX_SPRITES],
				tile_number,
				color,
				0,0,
				(256-sx)&0x1ff,sy&0x1ff,
				clip,TRANSPARENCY_PEN,7);
	
			source.base+=4;
		}
	}
	public static VhUpdatePtr tnk3_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		UBytePtr ram = memory_region(REGION_CPU1);
		int attributes = ram.read(0xc800);
		/*
			X-------
			-X------	character bank (for text layer)
			--X-----
			---X----	scrolly MSB (background)
			----X---	scrolly MSB (sprites)
			-----X--
			------X-	scrollx MSB (background)
			-------X	scrollx MSB (sprites)
		*/
	
		/* to be moved to memmap */
		spriteram = new CharPtr(ram,0xd000);
		videoram = new CharPtr(ram,0xd800);
	
		{
			int scrolly =  -8+ram.read(0xcb00)+((attributes&0x10)!=0?256:0);
			int scrollx = -16+ram.read(0xcc00)+((attributes&0x02)!=0?256:0);
			tnk3_draw_background( bitmap, -scrollx, -scrolly );
		}
	
		{
			int scrolly =  8+ram.read(0xc900) + ((attributes&0x08)!=0?256:0);
			int scrollx = 30+ram.read(0xca00) + ((attributes&0x01)!=0?256:0);
			tnk3_draw_sprites( bitmap, scrollx, scrolly );
		}
	
		{
			int bank = (attributes&0x40)!=0?1:0;
			tnk3_draw_text( bitmap, bank, new UBytePtr(ram,0xf800) );
			tnk3_draw_status( bitmap, bank, new UBytePtr(ram,0xfc00) );
		}
	}};
	
/*TODO*////**************************************************************************************/
/*TODO*///
/*TODO*///static void ikari_draw_background( struct osd_bitmap *bitmap, int xscroll, int yscroll ){
/*TODO*///	const struct GfxElement *gfx = Machine->gfx[GFX_TILES];
/*TODO*///	const unsigned char *source = &memory_region(REGION_CPU1)[snk_bg_tilemap_baseaddr];
/*TODO*///
/*TODO*///	int offs;
/*TODO*///	for( offs=0; offs<32*32*2; offs+=2 ){
/*TODO*///		int tile_number = source[offs];
/*TODO*///		unsigned char attributes = source[offs+1];
/*TODO*///
/*TODO*///		if( tile_number!=dirtybuffer[offs] ||
/*TODO*///			attributes != dirtybuffer[offs+1] ){
/*TODO*///
/*TODO*///			int sy = ((offs/2)%32)*16;
/*TODO*///			int sx = ((offs/2)/32)*16;
/*TODO*///
/*TODO*///			dirtybuffer[offs] = tile_number;
/*TODO*///			dirtybuffer[offs+1] = attributes;
/*TODO*///
/*TODO*///			tile_number+=256*(attributes&0x3);
/*TODO*///
/*TODO*///			drawgfx(tmpbitmap,gfx,
/*TODO*///				tile_number,
/*TODO*///				(attributes>>4), /* color */
/*TODO*///				0,0, /* no flip */
/*TODO*///				sx,sy,
/*TODO*///				0,TRANSPARENCY_NONE,0);
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	{
/*TODO*///		struct rectangle clip = Machine->drv->visible_area;
/*TODO*///		clip.min_x += 16;
/*TODO*///		clip.max_x -= 16;
/*TODO*///		copyscrollbitmap(bitmap,tmpbitmap,
/*TODO*///			1,&xscroll,1,&yscroll,
/*TODO*///			&clip,
/*TODO*///			TRANSPARENCY_NONE,0);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void ikari_draw_text( struct osd_bitmap *bitmap ){
/*TODO*///	const struct rectangle *clip = &Machine->drv->visible_area;
/*TODO*///	const struct GfxElement *gfx = Machine->gfx[GFX_CHARS];
/*TODO*///	const unsigned char *source = &memory_region(REGION_CPU1)[0xf800];
/*TODO*///
/*TODO*///	int offs;
/*TODO*///	for( offs = 0;offs <0x400; offs++ ){
/*TODO*///		int tile_number = source[offs];
/*TODO*///		int sy = (offs % 32)*8+8;
/*TODO*///		int sx = (offs / 32)*8+16;
/*TODO*///
/*TODO*///		drawgfx(bitmap,gfx,
/*TODO*///			tile_number,
/*TODO*///			8, /* color - vreg needs mapped! */
/*TODO*///			0,0, /* no flip */
/*TODO*///			sx,sy,
/*TODO*///			clip,TRANSPARENCY_PEN,15);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void ikari_draw_status( struct osd_bitmap *bitmap ){
/*TODO*///	/*	this is drawn above and below the main display */
/*TODO*///
/*TODO*///	const struct rectangle *clip = &Machine->drv->visible_area;
/*TODO*///	const struct GfxElement *gfx = Machine->gfx[GFX_CHARS];
/*TODO*///	const unsigned char *source = &memory_region(REGION_CPU1)[0xfc00];
/*TODO*///
/*TODO*///	int offs;
/*TODO*///	for( offs = 0; offs<64; offs++ ){
/*TODO*///		int tile_number = source[offs+30*32];
/*TODO*///		int sy = 20+(offs % 32)*8 - 16;
/*TODO*///		int sx = (offs / 32)*8;
/*TODO*///
/*TODO*///		drawgfx(bitmap,gfx,
/*TODO*///			tile_number,
/*TODO*///			8, /* color */
/*TODO*///			0,0, /* no flip */
/*TODO*///			sx,sy,
/*TODO*///			clip,TRANSPARENCY_NONE,0);
/*TODO*///
/*TODO*///		tile_number = source[offs];
/*TODO*///		sx += 34*8;
/*TODO*///
/*TODO*///		drawgfx(bitmap,gfx,
/*TODO*///			tile_number,
/*TODO*///			8, /* color */
/*TODO*///			0,0, /* no flip */
/*TODO*///			sx,sy,
/*TODO*///			clip,TRANSPARENCY_NONE,0);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void ikari_draw_sprites_16x16( struct osd_bitmap *bitmap, int start, int quantity, int xscroll, int yscroll )
/*TODO*///{
/*TODO*///	int transp_mode  = shadows_visible ? TRANSPARENCY_PEN : TRANSPARENCY_PENS;
/*TODO*///	int transp_param = shadows_visible ? 7 : ((1<<7) | (1<<6));
/*TODO*///
/*TODO*///	int which;
/*TODO*///	const unsigned char *source = &memory_region(REGION_CPU1)[0xe800];
/*TODO*///
/*TODO*///	struct rectangle clip = Machine->drv->visible_area;
/*TODO*///	clip.min_x += 16;
/*TODO*///	clip.max_x -= 16;
/*TODO*///
/*TODO*///	for( which = start*4; which < (start+quantity)*4; which+=4 )
/*TODO*///	{
/*TODO*///		int attributes = source[which+3]; /* YBBX.CCCC */
/*TODO*///		int tile_number = source[which+1] + ((attributes&0x60)<<3);
/*TODO*///		int sy = - yscroll + source[which]  +((attributes&0x10)?256:0);
/*TODO*///		int sx =   xscroll - source[which+2]+((attributes&0x80)?0:256);
/*TODO*///
/*TODO*///		drawgfx(bitmap,Machine->gfx[GFX_SPRITES],
/*TODO*///			tile_number,
/*TODO*///			attributes&0xf, /* color */
/*TODO*///			0,0, /* flip */
/*TODO*///			-16+(sx & 0x1ff), -16+(sy & 0x1ff),
/*TODO*///			&clip,transp_mode,transp_param);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void ikari_draw_sprites_32x32( struct osd_bitmap *bitmap, int start, int quantity, int xscroll, int yscroll )
/*TODO*///{
/*TODO*///	int transp_mode  = shadows_visible ? TRANSPARENCY_PEN : TRANSPARENCY_PENS;
/*TODO*///	int transp_param = shadows_visible ? 7 : ((1<<7) | (1<<6));
/*TODO*///
/*TODO*///	int which;
/*TODO*///	const unsigned char *source = &memory_region(REGION_CPU1)[0xe000];
/*TODO*///
/*TODO*///	struct rectangle clip = Machine->drv->visible_area;
/*TODO*///	clip.min_x += 16;
/*TODO*///	clip.max_x -= 16;
/*TODO*///
/*TODO*///	for( which = start*4; which < (start+quantity)*4; which+=4 )
/*TODO*///	{
/*TODO*///		int attributes = source[which+3];
/*TODO*///		int tile_number = source[which+1];
/*TODO*///		int sy = - yscroll + source[which] + ((attributes&0x10)?256:0);
/*TODO*///		int sx = xscroll - source[which+2] + ((attributes&0x80)?0:256);
/*TODO*///		if( attributes&0x40 ) tile_number += 256;
/*TODO*///
/*TODO*///		drawgfx( bitmap,Machine->gfx[GFX_BIGSPRITES],
/*TODO*///			tile_number,
/*TODO*///			attributes&0xf, /* color */
/*TODO*///			0,0, /* flip */
/*TODO*///			-16+(sx & 0x1ff), -16+(sy & 0x1ff),
/*TODO*///			&clip,transp_mode,transp_param );
/*TODO*///
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void ikari_vh_screenrefresh( struct osd_bitmap *bitmap, int full_refresh){
/*TODO*///	const unsigned char *ram = memory_region(REGION_CPU1);
/*TODO*///
/*TODO*///	shadows_visible = !shadows_visible;
/*TODO*///
/*TODO*///	{
/*TODO*///		int attributes = ram[0xc900];
/*TODO*///		int scrolly =  8-ram[0xc800] - ((attributes&0x01) ? 256:0);
/*TODO*///		int scrollx = 13-ram[0xc880] - ((attributes&0x02) ? 256:0);
/*TODO*///		ikari_draw_background( bitmap, scrollx, scrolly );
/*TODO*///	}
/*TODO*///	{
/*TODO*///		int attributes = ram[0xcd00];
/*TODO*///
/*TODO*///		int sp16_scrolly = -7 + ram[0xca00] + ((attributes&0x04) ? 256:0);
/*TODO*///		int sp16_scrollx = 44 + ram[0xca80] + ((attributes&0x10) ? 256:0);
/*TODO*///
/*TODO*///		int sp32_scrolly =  9 + ram[0xcb00] + ((attributes&0x08) ? 256:0);
/*TODO*///		int sp32_scrollx = 28 + ram[0xcb80] + ((attributes&0x20) ? 256:0);
/*TODO*///
/*TODO*///		ikari_draw_sprites_16x16( bitmap,  0, 25, sp16_scrollx, sp16_scrolly );
/*TODO*///		ikari_draw_sprites_32x32( bitmap,  0, 25, sp32_scrollx, sp32_scrolly );
/*TODO*///		ikari_draw_sprites_16x16( bitmap, 25, 25, sp16_scrollx, sp16_scrolly );
/*TODO*///	}
/*TODO*///	ikari_draw_text( bitmap );
/*TODO*///	ikari_draw_status( bitmap );
/*TODO*///}
/*TODO*///
/*TODO*////**************************************************************/
/*TODO*///
/*TODO*///static void tdfever_draw_background( struct osd_bitmap *bitmap,
/*TODO*///		int xscroll, int yscroll )
/*TODO*///{
/*TODO*///	const struct GfxElement *gfx = Machine->gfx[GFX_TILES];
/*TODO*///	const unsigned char *source = &memory_region(REGION_CPU1)[0xd000]; //d000
/*TODO*///
/*TODO*///	int offs;
/*TODO*///	for( offs=0; offs<32*32*2; offs+=2 ){
/*TODO*///		int tile_number = source[offs];
/*TODO*///		unsigned char attributes = source[offs+1];
/*TODO*///
/*TODO*///		if( tile_number!=dirtybuffer[offs] ||
/*TODO*///			attributes != dirtybuffer[offs+1] ){
/*TODO*///
/*TODO*///			int sy = ((offs/2)%32)*16;
/*TODO*///			int sx = ((offs/2)/32)*16;
/*TODO*///
/*TODO*///			int color = (attributes>>4); /* color */
/*TODO*///
/*TODO*///			dirtybuffer[offs] = tile_number;
/*TODO*///			dirtybuffer[offs+1] = attributes;
/*TODO*///
/*TODO*///			tile_number+=256*(attributes&0xf);
/*TODO*///
/*TODO*///			drawgfx(tmpbitmap,gfx,
/*TODO*///				tile_number,
/*TODO*///				color,
/*TODO*///				0,0, /* no flip */
/*TODO*///				sx,sy,
/*TODO*///				0,TRANSPARENCY_NONE,0);
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	{
/*TODO*///		struct rectangle clip = Machine->drv->visible_area;
/*TODO*///		copyscrollbitmap(bitmap,tmpbitmap,
/*TODO*///			1,&xscroll,1,&yscroll,
/*TODO*///			&clip,
/*TODO*///			TRANSPARENCY_NONE,0);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void tdfever_draw_sprites( struct osd_bitmap *bitmap, int xscroll, int yscroll ){
/*TODO*///	int transp_mode  = shadows_visible ? TRANSPARENCY_PEN : TRANSPARENCY_PENS;
/*TODO*///	int transp_param = shadows_visible ? 15 : ((1<<15) | (1<<14));
/*TODO*///
/*TODO*///	const struct GfxElement *gfx = Machine->gfx[GFX_SPRITES];
/*TODO*///	const unsigned char *source = &memory_region(REGION_CPU1)[0xe000];
/*TODO*///
/*TODO*///	int which;
/*TODO*///
/*TODO*///	struct rectangle clip = Machine->drv->visible_area;
/*TODO*///
/*TODO*///	for( which = 0; which < 32*4; which+=4 ){
/*TODO*///		int attributes = source[which+3];
/*TODO*///		int tile_number = source[which+1] + 8*(attributes&0x60);
/*TODO*///
/*TODO*///		int sy = - yscroll + source[which];
/*TODO*///		int sx = xscroll - source[which+2];
/*TODO*///		if( attributes&0x10 ) sy += 256;
/*TODO*///		if( attributes&0x80 ) sx -= 256;
/*TODO*///
/*TODO*///		sx &= 0x1ff; if( sx>512-32 ) sx-=512;
/*TODO*///		sy &= 0x1ff; if( sy>512-32 ) sy-=512;
/*TODO*///
/*TODO*///		drawgfx(bitmap,gfx,
/*TODO*///			tile_number,
/*TODO*///			(attributes&0xf), /* color */
/*TODO*///			0,0, /* no flip */
/*TODO*///			sx,sy,
/*TODO*///			&clip,transp_mode,transp_param);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void tdfever_draw_text( struct osd_bitmap *bitmap, int attributes, int dx, int dy, int base ){
/*TODO*///	int bank = attributes>>4;
/*TODO*///	int color = attributes&0xf;
/*TODO*///
/*TODO*///	const struct rectangle *clip = &Machine->drv->visible_area;
/*TODO*///	const struct GfxElement *gfx = Machine->gfx[GFX_CHARS];
/*TODO*///
/*TODO*///	const unsigned char *source = &memory_region(REGION_CPU1)[base];
/*TODO*///
/*TODO*///	int offs;
/*TODO*///
/*TODO*///	int bank_offset = bank*256;
/*TODO*///
/*TODO*///	for( offs = 0;offs <0x800; offs++ ){
/*TODO*///		int tile_number = source[offs];
/*TODO*///		int sy = dx+(offs % 32)*8;
/*TODO*///		int sx = dy+(offs / 32)*8;
/*TODO*///
/*TODO*///		if( source[offs] != 0x20 ){
/*TODO*///			drawgfx(bitmap,gfx,
/*TODO*///				tile_number + bank_offset,
/*TODO*///				color,
/*TODO*///				0,0, /* no flip */
/*TODO*///				sx,sy,
/*TODO*///				clip,TRANSPARENCY_PEN,15);
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void tdfever_vh_screenrefresh( struct osd_bitmap *bitmap, int fullrefresh ){
/*TODO*///	const unsigned char *ram = memory_region(REGION_CPU1);
/*TODO*///	shadows_visible = !shadows_visible;
/*TODO*///
/*TODO*///	{
/*TODO*///		unsigned char bg_attributes = ram[0xc880];
/*TODO*///		int bg_scroll_y = -30 - ram[0xc800] - ((bg_attributes&0x01)?256:0);
/*TODO*///		int bg_scroll_x = 141 - ram[0xc840] - ((bg_attributes&0x02)?256:0);
/*TODO*///		tdfever_draw_background( bitmap, bg_scroll_x, bg_scroll_y );
/*TODO*///	}
/*TODO*///
/*TODO*///	{
/*TODO*///		unsigned char sprite_attributes = ram[0xc900];
/*TODO*///		int sprite_scroll_y =   65 + ram[0xc980] + ((sprite_attributes&0x80)?256:0);
/*TODO*///		int sprite_scroll_x = -135 + ram[0xc9c0] + ((sprite_attributes&0x40)?256:0);
/*TODO*///		tdfever_draw_sprites( bitmap, sprite_scroll_x, sprite_scroll_y );
/*TODO*///	}
/*TODO*///
/*TODO*///	{
/*TODO*///		unsigned char text_attributes = ram[0xc8c0];
/*TODO*///		tdfever_draw_text( bitmap, text_attributes, 0,0, 0xf800 );
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void ftsoccer_vh_screenrefresh( struct osd_bitmap *bitmap, int fullrefresh ){
/*TODO*///	const unsigned char *ram = memory_region(REGION_CPU1);
/*TODO*///	shadows_visible = !shadows_visible;
/*TODO*///	{
/*TODO*///		unsigned char bg_attributes = ram[0xc880];
/*TODO*///		int bg_scroll_y = - ram[0xc800] - ((bg_attributes&0x01)?256:0);
/*TODO*///		int bg_scroll_x = 16 - ram[0xc840] - ((bg_attributes&0x02)?256:0);
/*TODO*///		tdfever_draw_background( bitmap, bg_scroll_x, bg_scroll_y );
/*TODO*///	}
/*TODO*///
/*TODO*///	{
/*TODO*///		unsigned char sprite_attributes = ram[0xc900];
/*TODO*///		int sprite_scroll_y =  31 + ram[0xc980] + ((sprite_attributes&0x80)?256:0);
/*TODO*///		int sprite_scroll_x = -40 + ram[0xc9c0] + ((sprite_attributes&0x40)?256:0);
/*TODO*///		tdfever_draw_sprites( bitmap, sprite_scroll_x, sprite_scroll_y );
/*TODO*///	}
/*TODO*///
/*TODO*///	{
/*TODO*///		unsigned char text_attributes = ram[0xc8c0];
/*TODO*///		tdfever_draw_text( bitmap, text_attributes, 0,0, 0xf800 );
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void gwar_draw_sprites_16x16( struct osd_bitmap *bitmap, int xscroll, int yscroll ){
/*TODO*///	const struct GfxElement *gfx = Machine->gfx[GFX_SPRITES];
/*TODO*///	const unsigned char *source = &memory_region(REGION_CPU1)[0xe800];
/*TODO*///
/*TODO*///	const struct rectangle *clip = &Machine->drv->visible_area;
/*TODO*///
/*TODO*///	int which;
/*TODO*///	for( which=0; which<(64)*4; which+=4 )
/*TODO*///	{
/*TODO*///		int attributes = source[which+3]; /* YBBX.BCCC */
/*TODO*///		int tile_number = source[which+1];
/*TODO*///		int sy = -xscroll + source[which];
/*TODO*///		int sx =  yscroll - source[which+2];
/*TODO*///		if( attributes&0x10 ) sy += 256;
/*TODO*///		if( attributes&0x80 ) sx -= 256;
/*TODO*///
/*TODO*///		if( attributes&0x08 ) tile_number += 256;
/*TODO*///		if( attributes&0x20 ) tile_number += 512;
/*TODO*///		if( attributes&0x40 ) tile_number += 1024;
/*TODO*///
/*TODO*///		sy &= 0x1ff; if( sy>512-16 ) sy-=512;
/*TODO*///		sx = (-sx)&0x1ff; if( sx>512-16 ) sx-=512;
/*TODO*///
/*TODO*///		drawgfx(bitmap,gfx,
/*TODO*///			tile_number,
/*TODO*///			(attributes&7), /* color */
/*TODO*///			0,0, /* flip */
/*TODO*///			sx,sy,
/*TODO*///			clip,TRANSPARENCY_PEN,15 );
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void gwar_draw_sprites_32x32( struct osd_bitmap *bitmap, int xscroll, int yscroll ){
/*TODO*///	const struct GfxElement *gfx = Machine->gfx[GFX_BIGSPRITES];
/*TODO*///	const unsigned char *source = &memory_region(REGION_CPU1)[0xe000];
/*TODO*///
/*TODO*///	const struct rectangle *clip = &Machine->drv->visible_area;
/*TODO*///
/*TODO*///	int which;
/*TODO*///	for( which=0; which<(32)*4; which+=4 )
/*TODO*///	{
/*TODO*///		int attributes = source[which+3];
/*TODO*///		int tile_number = source[which+1] + 8*(attributes&0x60);
/*TODO*///
/*TODO*///		int sy = - xscroll + source[which];
/*TODO*///		int sx = yscroll - source[which+2];
/*TODO*///		if( attributes&0x10 ) sy += 256;
/*TODO*///		if( attributes&0x80 ) sx -= 256;
/*TODO*///
/*TODO*///		sy = (sy&0x1ff);
/*TODO*///		sx = ((-sx)&0x1ff);
/*TODO*///		if( sy>512-32 ) sy-=512;
/*TODO*///		if( sx>512-32 ) sx-=512;
/*TODO*///
/*TODO*///		drawgfx(bitmap,gfx,
/*TODO*///			tile_number,
/*TODO*///			(attributes&0xf), /* color */
/*TODO*///			0,0, /* no flip */
/*TODO*///			sx,sy,
/*TODO*///			clip,TRANSPARENCY_PEN,15);
/*TODO*///	}
/*TODO*///}
/*TODO*///
     public static VhUpdatePtr gwar_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
     {     

/*TODO*///	const unsigned char *ram = memory_region(REGION_CPU1);
/*TODO*///	unsigned char bg_attributes, sp_attributes;
/*TODO*///
/*TODO*///	{
/*TODO*///		int bg_scroll_y, bg_scroll_x;
/*TODO*///
/*TODO*///		if( gwar_sprite_placement==2 ) { /* Gwar alternate */
/*TODO*///			bg_attributes = ram[0xf880];
/*TODO*///			sp_attributes = ram[0xfa80];
/*TODO*///			bg_scroll_y = - ram[0xf800] - ((bg_attributes&0x01)?256:0);
/*TODO*///			bg_scroll_x  = 16 - ram[0xf840] - ((bg_attributes&0x02)?256:0);
/*TODO*///		} else {
/*TODO*///			bg_attributes = ram[0xc880];
/*TODO*///			sp_attributes = ram[0xcac0];
/*TODO*///			bg_scroll_y = - ram[0xc800] - ((bg_attributes&0x01)?256:0);
/*TODO*///			bg_scroll_x  = 16 - ram[0xc840] - ((bg_attributes&0x02)?256:0);
/*TODO*///		}
/*TODO*///		tdfever_draw_background( bitmap, bg_scroll_x, bg_scroll_y );
/*TODO*///	}
/*TODO*///
/*TODO*///	{
/*TODO*///		int sp16_y = ram[0xc900]+15;
/*TODO*///		int sp16_x = ram[0xc940]+8;
/*TODO*///		int sp32_y = ram[0xc980]+31;
/*TODO*///		int sp32_x = ram[0xc9c0]+8;
/*TODO*///
/*TODO*///		if( gwar_sprite_placement ) /* gwar */
/*TODO*///		{
/*TODO*///			if( bg_attributes&0x10 ) sp16_y += 256;
/*TODO*///			if( bg_attributes&0x40 ) sp16_x += 256;
/*TODO*///			if( bg_attributes&0x20 ) sp32_y += 256;
/*TODO*///			if( bg_attributes&0x80 ) sp32_x += 256;
/*TODO*///		}
/*TODO*///		else{ /* psychos, bermudet, chopper1... */
/*TODO*///			unsigned char spp_attributes = ram[0xca80];
/*TODO*///			if( spp_attributes&0x04 ) sp16_y += 256;
/*TODO*///			if( spp_attributes&0x08 ) sp32_y += 256;
/*TODO*///			if( spp_attributes&0x10 ) sp16_x += 256;
/*TODO*///			if( spp_attributes&0x20 ) sp32_x += 256;
/*TODO*///		}
/*TODO*///		if (sp_attributes & 0x20)
/*TODO*///		{
/*TODO*///			gwar_draw_sprites_16x16( bitmap, sp16_y, sp16_x );
/*TODO*///			gwar_draw_sprites_32x32( bitmap, sp32_y, sp32_x );
/*TODO*///		}
/*TODO*///		else {
/*TODO*///			gwar_draw_sprites_32x32( bitmap, sp32_y, sp32_x );
/*TODO*///			gwar_draw_sprites_16x16( bitmap, sp16_y, sp16_x );
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	{
/*TODO*///		if( gwar_sprite_placement==2) { /* Gwar alternate */
/*TODO*///			unsigned char text_attributes = ram[0xf8c0];
/*TODO*///			tdfever_draw_text( bitmap, text_attributes,0,0, 0xc800 );
/*TODO*///		}
/*TODO*///		else {
/*TODO*///			unsigned char text_attributes = ram[0xc8c0];
/*TODO*///			tdfever_draw_text( bitmap, text_attributes,0,0, 0xf800 );
/*TODO*///		}
/*TODO*///	}
     }};
/*TODO*///
/*TODO*///
}