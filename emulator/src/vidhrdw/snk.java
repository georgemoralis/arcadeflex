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
import static arcadeflex.ptrlib.*;
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
	public static VhConvertColorPromPtr ikari_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(UByte []palette, char []colortable, UBytePtr color_prom) {

            int i;
            snk_vh_convert_color_prom.handler(palette, colortable, color_prom);

            //palette += 6*3;//move pallete 18 places???
            int p_inc=0;
            p_inc+=6*3;
            /*
                    pen#6 is used for translucent shadows;
                    we'll just make it dark grey for now
            */
            for( i=0; i<256; i+=8 ){
                    palette[p_inc+i*3+0].set((char)(14)); 
                    palette[p_inc+i*3+1].set((char)(14)); 
                    palette[p_inc+i*3+2].set((char)(14));
            }
        }};
	
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
		UBytePtr source = new UBytePtr(spriteram,0);
		UBytePtr finish = new UBytePtr(source,n*4);
		rectangle clip = Machine.drv.visible_area;

	
		while( source.offset<finish.offset){
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
	
			source.offset+=4;
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
		spriteram = new UBytePtr(ram,0xd000);
		videoram = new UBytePtr(ram,0xd800);
	
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
	
    /**************************************************************************************/

    public static void ikari_draw_background(osd_bitmap bitmap, int xscroll, int yscroll ){
            GfxElement gfx = Machine.gfx[GFX_TILES];
            UBytePtr source = new UBytePtr(memory_region(REGION_CPU1),snk_bg_tilemap_baseaddr);

            int offs;
            for( offs=0; offs<32*32*2; offs+=2 ){
                    int tile_number = source.read(offs);
                   char attributes = source.read(offs+1);

                    if( tile_number!=dirtybuffer[offs] ||
                            attributes != dirtybuffer[offs+1] ){

                            int sy = ((offs/2)%32)*16;
                            int sx = ((offs/2)/32)*16;

                            dirtybuffer[offs] = (char)tile_number;
                            dirtybuffer[offs+1] = attributes;

                            tile_number+=256*(attributes&0x3);

                            drawgfx(tmpbitmap,gfx,
                                    tile_number,
                                    (attributes>>4), /* color */
                                    0,0, /* no flip */
                                    sx,sy,
                                    null,TRANSPARENCY_NONE,0);
                    }
            }

            {
                    rectangle clip = Machine.drv.visible_area;
                    clip.min_x += 16;
                    clip.max_x -= 16;
                    copyscrollbitmap(bitmap,tmpbitmap,
                            1,new int[]{xscroll},1,new int[]{yscroll},
                            clip,
                            TRANSPARENCY_NONE,0);
            }
    }

    static void ikari_draw_text(osd_bitmap bitmap ){
            rectangle clip = Machine.drv.visible_area;
           GfxElement gfx = Machine.gfx[GFX_CHARS];
            UBytePtr source = new UBytePtr(memory_region(REGION_CPU1),0xf800);

            int offs;
            for( offs = 0;offs <0x400; offs++ ){
                    int tile_number = source.read(offs);
                    int sy = (offs % 32)*8+8;
                    int sx = (offs / 32)*8+16;

                    drawgfx(bitmap,gfx,
                            tile_number,
                            8, /* color - vreg needs mapped! */
                            0,0, /* no flip */
                            sx,sy,
                            clip,TRANSPARENCY_PEN,15);
            }
    }

    static void ikari_draw_status(osd_bitmap bitmap ){
            /*	this is drawn above and below the main display */

            rectangle clip = Machine.drv.visible_area;
            GfxElement gfx = Machine.gfx[GFX_CHARS];
            UBytePtr source = new UBytePtr(memory_region(REGION_CPU1),0xfc00);

            int offs;
            for( offs = 0; offs<64; offs++ ){
                    int tile_number = source.read(offs+30*32);
                    int sy = 20+(offs % 32)*8 - 16;
                    int sx = (offs / 32)*8;

                    drawgfx(bitmap,gfx,
                            tile_number,
                            8, /* color */
                            0,0, /* no flip */
                            sx,sy,
                            clip,TRANSPARENCY_NONE,0);

                    tile_number = source.read(offs);
                    sx += 34*8;

                    drawgfx(bitmap,gfx,
                            tile_number,
                            8, /* color */
                            0,0, /* no flip */
                            sx,sy,
                            clip,TRANSPARENCY_NONE,0);
            }
    }

    static void ikari_draw_sprites_16x16(osd_bitmap bitmap, int start, int quantity, int xscroll, int yscroll )
    {
            int transp_mode  = shadows_visible!=0 ? TRANSPARENCY_PEN : TRANSPARENCY_PENS;
            int transp_param = shadows_visible!=0 ? 7 : ((1<<7) | (1<<6));

            int which;
            UBytePtr source = new UBytePtr(memory_region(REGION_CPU1),0xe800);

            rectangle clip = Machine.drv.visible_area;
            clip.min_x += 16;
            clip.max_x -= 16;

            for( which = start*4; which < (start+quantity)*4; which+=4 )
            {
                    int attributes = source.read(which+3); /* YBBX.CCCC */
                    int tile_number = source.read(which+1) + ((attributes&0x60)<<3);
                    int sy = - yscroll + source.read(which)  +((attributes&0x10)!=0?256:0);
                    int sx =   xscroll - source.read(which+2)+((attributes&0x80)!=0?0:256);

                    drawgfx(bitmap,Machine.gfx[GFX_SPRITES],
                            tile_number,
                            attributes&0xf, /* color */
                            0,0, /* flip */
                            -16+(sx & 0x1ff), -16+(sy & 0x1ff),
                            clip,transp_mode,transp_param);
            }
    }

    static void ikari_draw_sprites_32x32( osd_bitmap bitmap, int start, int quantity, int xscroll, int yscroll )
    {
            int transp_mode  = shadows_visible!=0 ? TRANSPARENCY_PEN : TRANSPARENCY_PENS;
            int transp_param = shadows_visible!=0 ? 7 : ((1<<7) | (1<<6));

            int which;
            UBytePtr source = new UBytePtr(memory_region(REGION_CPU1),0xe000);

            rectangle clip = Machine.drv.visible_area;
            clip.min_x += 16;
            clip.max_x -= 16;

            for( which = start*4; which < (start+quantity)*4; which+=4 )
            {
                    int attributes = source.read(which+3);
                    int tile_number = source.read(which+1);
                    int sy = - yscroll + source.read(which) + ((attributes&0x10)!=0?256:0);
                    int sx = xscroll - source.read(which+2) + ((attributes&0x80)!=0?0:256);
                    if(( attributes&0x40 )!=0) tile_number += 256;

                    drawgfx( bitmap,Machine.gfx[GFX_BIGSPRITES],
                            tile_number,
                            attributes&0xf, /* color */
                            0,0, /* flip */
                            -16+(sx & 0x1ff), -16+(sy & 0x1ff),
                            clip,transp_mode,transp_param );

            }
    }
    public static VhUpdatePtr ikari_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
    {

	UBytePtr ram = memory_region(REGION_CPU1);

	shadows_visible = NOT(shadows_visible);

	{
		int attributes = ram.read(0xc900);
		int scrolly =  8-ram.read(0xc800) - ((attributes&0x01)!=0 ? 256:0);
		int scrollx = 13-ram.read(0xc880) - ((attributes&0x02)!=0 ? 256:0);
		ikari_draw_background( bitmap, scrollx, scrolly );
	}
	{
		int attributes = ram.read(0xcd00);

		int sp16_scrolly = -7 + ram.read(0xca00) + ((attributes&0x04)!=0 ? 256:0);
		int sp16_scrollx = 44 + ram.read(0xca80) + ((attributes&0x10)!=0 ? 256:0);

		int sp32_scrolly =  9 + ram.read(0xcb00) + ((attributes&0x08)!=0 ? 256:0);
		int sp32_scrollx = 28 + ram.read(0xcb80) + ((attributes&0x20)!=0 ? 256:0);

		ikari_draw_sprites_16x16( bitmap,  0, 25, sp16_scrollx, sp16_scrolly );
		ikari_draw_sprites_32x32( bitmap,  0, 25, sp32_scrollx, sp32_scrolly );
		ikari_draw_sprites_16x16( bitmap, 25, 25, sp16_scrollx, sp16_scrolly );
	}
	ikari_draw_text( bitmap );
	ikari_draw_status( bitmap );
    }};

/**************************************************************/

    static void tdfever_draw_background(osd_bitmap bitmap,
                    int xscroll, int yscroll )
    {
            GfxElement gfx = Machine.gfx[GFX_TILES];
            //const unsigned char *source = &memory_region(REGION_CPU1)[0xd000]; //d000
            UBytePtr source = new UBytePtr(memory_region(REGION_CPU1),0xd000);
            int offs;
            for( offs=0; offs<32*32*2; offs+=2 ){
                    int tile_number = source.read(offs);
                    /*unsigned*/ char attributes = source.read(offs+1);

                    if( tile_number!=dirtybuffer[offs] ||
                            attributes != dirtybuffer[offs+1] ){

                            int sy = ((offs/2)%32)*16;
                            int sx = ((offs/2)/32)*16;

                            int color = (attributes>>4); /* color */

                            dirtybuffer[offs] = (char)tile_number;
                            dirtybuffer[offs+1] = attributes;

                            tile_number+=256*(attributes&0xf);

                            drawgfx(tmpbitmap,gfx,
                                    tile_number,
                                    color,
                                    0,0, /* no flip */
                                    sx,sy,
                                    null,TRANSPARENCY_NONE,0);
                    }
            }

            {
                    rectangle clip = Machine.drv.visible_area;
                    copyscrollbitmap(bitmap,tmpbitmap,
                            1,new int[]{xscroll},1,new int[] {yscroll},
                            clip,
                            TRANSPARENCY_NONE,0);
            }
    }

    static void tdfever_draw_sprites(osd_bitmap bitmap, int xscroll, int yscroll ){
            int transp_mode  = shadows_visible!=0 ? TRANSPARENCY_PEN : TRANSPARENCY_PENS;
            int transp_param = shadows_visible!=0 ? 15 : ((1<<15) | (1<<14));

            GfxElement gfx = Machine.gfx[GFX_SPRITES];
            UBytePtr source = new UBytePtr(memory_region(REGION_CPU1),0xe000);

            int which;

            rectangle clip = Machine.drv.visible_area;

            for( which = 0; which < 32*4; which+=4 ){
                    int attributes = source.read(which+3);
                    int tile_number = source.read(which+1) + 8*(attributes&0x60);

                    int sy = - yscroll + source.read(which);
                    int sx = xscroll - source.read(which+2);
                    if(( attributes&0x10 )!=0) sy += 256;
                    if(( attributes&0x80 )!=0) sx -= 256;

                    sx &= 0x1ff; if( sx>512-32 ) sx-=512;
                    sy &= 0x1ff; if( sy>512-32 ) sy-=512;

                    drawgfx(bitmap,gfx,
                            tile_number,
                            (attributes&0xf), /* color */
                            0,0, /* no flip */
                            sx,sy,
                            clip,transp_mode,transp_param);
            }
    }

    static void tdfever_draw_text(osd_bitmap bitmap, int attributes, int dx, int dy, int base ){
            int bank = attributes>>4;
            int color = attributes&0xf;

            rectangle clip = Machine.drv.visible_area;
            GfxElement gfx = Machine.gfx[GFX_CHARS];

            //const unsigned char *source = &memory_region(REGION_CPU1)[base];
            UBytePtr source = new UBytePtr(memory_region(REGION_CPU1),base);
            int offs;

            int bank_offset = bank*256;

            for( offs = 0;offs <0x800; offs++ ){
                    int tile_number = source.read(offs);
                    int sy = dx+(offs % 32)*8;
                    int sx = dy+(offs / 32)*8;

                    if( source.read(offs) != 0x20 ){
                            drawgfx(bitmap,gfx,
                                    tile_number + bank_offset,
                                    color,
                                    0,0, /* no flip */
                                    sx,sy,
                                    clip,TRANSPARENCY_PEN,15);
                    }
            }
    }
    public static VhUpdatePtr tdfever_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
     {
	UBytePtr ram = memory_region(REGION_CPU1);
	shadows_visible = NOT(shadows_visible);

	{
		char bg_attributes = ram.read(0xc880);
		int bg_scroll_y = -30 - ram.read(0xc800) - ((bg_attributes&0x01)!=0?256:0);
		int bg_scroll_x = 141 - ram.read(0xc840) - ((bg_attributes&0x02)!=0?256:0);
		tdfever_draw_background( bitmap, bg_scroll_x, bg_scroll_y );
	}

	{
		char sprite_attributes = ram.read(0xc900);
		int sprite_scroll_y =   65 + ram.read(0xc980) + ((sprite_attributes&0x80)!=0?256:0);
		int sprite_scroll_x = -135 + ram.read(0xc9c0) + ((sprite_attributes&0x40)!=0?256:0);
		tdfever_draw_sprites( bitmap, sprite_scroll_x, sprite_scroll_y );
	}

	{
		char text_attributes = ram.read(0xc8c0);
		tdfever_draw_text( bitmap, text_attributes, 0,0, 0xf800 );
	}
    }};

    public static VhUpdatePtr ftsoccer_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
    {
	UBytePtr ram = memory_region(REGION_CPU1);
	shadows_visible = NOT(shadows_visible);
	{
		char bg_attributes = ram.read(0xc880);
		int bg_scroll_y = - ram.read(0xc800) - ((bg_attributes&0x01)!=0?256:0);
		int bg_scroll_x = 16 - ram.read(0xc840) - ((bg_attributes&0x02)!=0?256:0);
		tdfever_draw_background( bitmap, bg_scroll_x, bg_scroll_y );
	}

	{
		char sprite_attributes = ram.read(0xc900);
		int sprite_scroll_y =  31 + ram.read(0xc980) + ((sprite_attributes&0x80)!=0?256:0);
		int sprite_scroll_x = -40 + ram.read(0xc9c0) + ((sprite_attributes&0x40)!=0?256:0);
		tdfever_draw_sprites( bitmap, sprite_scroll_x, sprite_scroll_y );
	}

	{
		char text_attributes = ram.read(0xc8c0);
		tdfever_draw_text( bitmap, text_attributes, 0,0, 0xf800 );
	}
    }};

    static void gwar_draw_sprites_16x16(osd_bitmap bitmap, int xscroll, int yscroll ){
            GfxElement gfx = Machine.gfx[GFX_SPRITES];
            //const unsigned char *source = &memory_region(REGION_CPU1)[0xe800];
            UBytePtr source = new UBytePtr(memory_region(REGION_CPU1),0xe800);
            
            rectangle clip = Machine.drv.visible_area;

            int which;
            for( which=0; which<(64)*4; which+=4 )
            {
                    int attributes = source.read(which+3); /* YBBX.BCCC */
                    int tile_number = source.read(which+1);
                    int sy = -xscroll + source.read(which);
                    int sx =  yscroll - source.read(which+2);
                    if(( attributes&0x10 )!=0) sy += 256;
                    if(( attributes&0x80 )!=0) sx -= 256;

                    if(( attributes&0x08 )!=0) tile_number += 256;
                    if(( attributes&0x20 )!=0) tile_number += 512;
                    if(( attributes&0x40 )!=0) tile_number += 1024;

                    sy &= 0x1ff; if( sy>512-16 ) sy-=512;
                    sx = (-sx)&0x1ff; if( sx>512-16 ) sx-=512;

                    drawgfx(bitmap,gfx,
                            tile_number,
                            (attributes&7), /* color */
                            0,0, /* flip */
                            sx,sy,
                            clip,TRANSPARENCY_PEN,15 );
            }
    }

    public static void gwar_draw_sprites_32x32(osd_bitmap bitmap, int xscroll, int yscroll ){
            GfxElement gfx = Machine.gfx[GFX_BIGSPRITES];
            //const unsigned char *source = &memory_region(REGION_CPU1)[0xe000];
            UBytePtr source = new UBytePtr(memory_region(REGION_CPU1),0xe000);
            
            rectangle clip = Machine.drv.visible_area;

            int which;
            for( which=0; which<(32)*4; which+=4 )
            {
                    int attributes = source.read(which+3);
                    int tile_number = source.read(which+1) + 8*(attributes&0x60);

                    int sy = - xscroll + source.read(which);
                    int sx = yscroll - source.read(which+2);
                    if(( attributes&0x10 )!=0) sy += 256;
                    if(( attributes&0x80 )!=0) sx -= 256;

                    sy = (sy&0x1ff);
                    sx = ((-sx)&0x1ff);
                    if( sy>512-32 ) sy-=512;
                    if( sx>512-32 ) sx-=512;

                    drawgfx(bitmap,gfx,
                            tile_number,
                            (attributes&0xf), /* color */
                            0,0, /* no flip */
                            sx,sy,
                            clip,TRANSPARENCY_PEN,15);
            }
    }

     public static VhUpdatePtr gwar_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
     {     

	UBytePtr ram = memory_region(REGION_CPU1);
	/*unsigned*/ char bg_attributes, sp_attributes;

	{
		int bg_scroll_y, bg_scroll_x;

		if( gwar_sprite_placement==2 ) { /* Gwar alternate */
			bg_attributes = ram.read(0xf880);
			sp_attributes = ram.read(0xfa80);
			bg_scroll_y = - ram.read(0xf800) - ((bg_attributes&0x01)!=0?256:0);
			bg_scroll_x  = 16 - ram.read(0xf840) - ((bg_attributes&0x02)!=0?256:0);
		} else {
			bg_attributes = ram.read(0xc880);
			sp_attributes = ram.read(0xcac0);
			bg_scroll_y = - ram.read(0xc800) - ((bg_attributes&0x01)!=0?256:0);
			bg_scroll_x  = 16 - ram.read(0xc840) - ((bg_attributes&0x02)!=0?256:0);
		}
		tdfever_draw_background( bitmap, bg_scroll_x, bg_scroll_y );
	}

	{
		int sp16_y = ram.read(0xc900)+15;
		int sp16_x = ram.read(0xc940)+8;
		int sp32_y = ram.read(0xc980)+31;
		int sp32_x = ram.read(0xc9c0)+8;

		if( gwar_sprite_placement!=0 ) /* gwar */
		{
			if(( bg_attributes&0x10 )!=0) sp16_y += 256;
			if(( bg_attributes&0x40 )!=0) sp16_x += 256;
			if(( bg_attributes&0x20 )!=0) sp32_y += 256;
			if(( bg_attributes&0x80 )!=0) sp32_x += 256;
		}
		else{ /* psychos, bermudet, chopper1... */
			/*unsigned*/ char spp_attributes = ram.read(0xca80);
			if(( spp_attributes&0x04 )!=0) sp16_y += 256;
			if(( spp_attributes&0x08 )!=0) sp32_y += 256;
			if(( spp_attributes&0x10 )!=0) sp16_x += 256;
			if(( spp_attributes&0x20 )!=0) sp32_x += 256;
		}
		if ((sp_attributes & 0x20)!=0)
		{
			gwar_draw_sprites_16x16( bitmap, sp16_y, sp16_x );
			gwar_draw_sprites_32x32( bitmap, sp32_y, sp32_x );
		}
		else {
			gwar_draw_sprites_32x32( bitmap, sp32_y, sp32_x );
			gwar_draw_sprites_16x16( bitmap, sp16_y, sp16_x );
		}
	}

	{
		if( gwar_sprite_placement==2) { /* Gwar alternate */
			/*unsigned*/ char text_attributes = ram.read(0xf8c0);
			tdfever_draw_text( bitmap, text_attributes,0,0, 0xc800 );
		}
		else {
			/*unsigned*/ char text_attributes = ram.read(0xc8c0);
			tdfever_draw_text( bitmap, text_attributes,0,0, 0xf800 );
		}
	}
     }};


}