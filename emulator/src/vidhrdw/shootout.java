/*
	Video Hardware for Shoot Out
	prom GB09.K6 may be related to background tile-sprite priority
*/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package vidhrdw;

import static arcadeflex.libc.*;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static vidhrdw.generic.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.mame.*;
import static arcadeflex.ptrlib.*;

public class shootout
{
	
	public static final int NUM_SPRITES =128;
	
	public static UBytePtr shootout_textram=new UBytePtr();
/*TODO*///	static struct sprite_list *sprite_list;
	
	
	public static VhStartPtr shootout_vh_start = new VhStartPtr() { public int handler() {
		if( generic_vh_start.handler()==0 ){
/*TODO*///			sprite_list = sprite_list_create( NUM_SPRITES, SPRITE_LIST_BACK_TO_FRONT );
/*TODO*///			if (sprite_list != 0){
/*TODO*///				int i;
/*TODO*///				sprite_list.sprite_type = SPRITE_TYPE_STACK;
/*TODO*///	
/*TODO*///				for( i=0; i<NUM_SPRITES; i++ ){
/*TODO*///					struct sprite *sprite = &sprite_list.sprite[i];
/*TODO*///					sprite.pal_data = Machine.gfx[1].colortable;
/*TODO*///					sprite.tile_width = 16;
/*TODO*///					sprite.tile_height = 16;
/*TODO*///					sprite.total_width = 16;
/*TODO*///					sprite.line_offset = 16;
/*TODO*///				}
/*TODO*///				sprite_list.max_priority = 1;
/*TODO*///	
				return 0;
/*TODO*///			}
/*TODO*///			generic_vh_stop.handler();
		}
		return 1; /* error */
	} };
	
/*TODO*///	static void get_sprite_info( void ){
/*TODO*///		const struct GfxElement *gfx = Machine.gfx[1];
/*TODO*///		const UINT8 *source = spriteram;
/*TODO*///		struct sprite *sprite = sprite_list.sprite;
/*TODO*///		int count = NUM_SPRITES;
	
/*TODO*///		int attributes, flags, number;
	
/*TODO*///		while( count-- ){
/*TODO*///			flags = 0;
/*TODO*///			attributes = source[1];
			/*
			    76543210
				xxx			bank
				   x		vertical size
				    x		priority
				     x		horizontal flip
				      x		flicker
				       x	enable
			*/
/*TODO*///			if ((attributes & 0x01) != 0){ /* enabled */
/*TODO*///				flags |= SPRITE_VISIBLE;
/*TODO*///				sprite.priority = (attributes&0x08)?1:0;
/*TODO*///				sprite.x = (240 - source[2])&0xff;
/*TODO*///				sprite.y = (240 - source[0])&0xff;
	
/*TODO*///				number = source[3] + ((attributes&0xe0)<<3);
/*TODO*///				if ((attributes & 0x04) != 0) flags |= SPRITE_FLIPX;
/*TODO*///				if ((attributes & 0x02) != 0) flags |= SPRITE_FLICKER; /* ? */
/*TODO*///	
/*TODO*///				if ((attributes & 0x10) != 0){ /* double height */
/*TODO*///					number = number&(~1);
/*TODO*///					sprite.y -= 16;
/*TODO*///					sprite.total_height = 32;
/*TODO*///				}
/*TODO*///				else {
/*TODO*///					sprite.total_height = 16;
/*TODO*///				}
/*TODO*///				sprite.pen_data = gfx.gfxdata + number * gfx.char_modulo;
/*TODO*///			}
/*TODO*///			sprite.flags = flags;
/*TODO*///			sprite++;
/*TODO*///			source += 4;
/*TODO*///		}
/*TODO*///	}
	
/*TODO*///	static void get_sprite_info2( void ){
/*TODO*///		const struct GfxElement *gfx = Machine.gfx[1];
/*TODO*///		const UINT8 *source = spriteram;
/*TODO*///		struct sprite *sprite = sprite_list.sprite;
/*TODO*///		int count = NUM_SPRITES;
/*TODO*///	
/*TODO*///		int attributes, flags, number;
/*TODO*///	
/*TODO*///		while( count-- ){
/*TODO*///			flags = 0;
/*TODO*///			attributes = source[1];
/*TODO*///			if ((attributes & 0x01) != 0){ /* enabled */
/*TODO*///				flags |= SPRITE_VISIBLE;
/*TODO*///				sprite.priority = (attributes&0x08)?1:0;
/*TODO*///				sprite.x = (240 - source[2])&0xff;
/*TODO*///				sprite.y = (240 - source[0])&0xff;
/*TODO*///	
/*TODO*///				number = source[3] + ((attributes&0xc0)<<2);
/*TODO*///				if ((attributes & 0x04) != 0) flags |= SPRITE_FLIPX;
/*TODO*///				if ((attributes & 0x02) != 0) flags |= SPRITE_FLICKER; /* ? */
/*TODO*///	
/*TODO*///				if ((attributes & 0x10) != 0){ /* double height */
/*TODO*///					number = number&(~1);
/*TODO*///					sprite.y -= 16;
/*TODO*///					sprite.total_height = 32;
/*TODO*///				}
/*TODO*///				else {
/*TODO*///					sprite.total_height = 16;
/*TODO*///				}
/*TODO*///				sprite.pen_data = gfx.gfxdata + number * gfx.char_modulo;
/*TODO*///			}
/*TODO*///			sprite.flags = flags;
/*TODO*///			sprite++;
/*TODO*///			source += 4;
/*TODO*///		}
/*TODO*///	}
	
	static void draw_background(osd_bitmap bitmap ){
		rectangle clip = Machine.drv.visible_area;
		int offs;
		for( offs=0; offs<videoram_size[0]; offs++ ){
			if( dirtybuffer[offs]!=0 ){
				int sx = (offs%32)*8;
				int sy = (offs/32)*8;
				int attributes = colorram.read(offs); /* CCCC -TTT */
				int tile_number = videoram.read(offs) + 256*(attributes&7);
				int color = attributes>>4;
	
				drawgfx(tmpbitmap,Machine.gfx[2],
						tile_number&0x7ff,
						color,
						0,0,
						sx,sy,
						clip,TRANSPARENCY_NONE,0);
	
				dirtybuffer[offs] = 0;
			}
		}
		copybitmap(bitmap,tmpbitmap,0,0,0,0,Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	}
	
	static void draw_foreground( osd_bitmap bitmap ){
		rectangle clip = Machine.drv.visible_area;
		GfxElement gfx = Machine.gfx[0];
		int sx,sy;
	
		UBytePtr source = new UBytePtr(shootout_textram);
	
		for( sy=0; sy<256; sy+=8 ){
			for( sx=0; sx<256; sx+=8 ){
				int attributes = source.read(videoram_size[0]);//*(source+videoram_size); /* CCCC --TT */
				int tile_number = 256*(attributes&0x3) + source.readinc();
				int color = attributes>>4;
				drawgfx(bitmap,gfx,
					tile_number, /* 0..1024 */
					color,
					0,0,
					sx,sy,
					clip,TRANSPARENCY_PEN,0);
			}
		}
	}
	
	public static VhUpdatePtr shootout_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) {
/*TODO*///		get_sprite_info();
/*TODO*///		sprite_update();
		draw_background( bitmap );
/*TODO*///		sprite_draw( sprite_list, 1);
		draw_foreground( bitmap );
/*TODO*///		sprite_draw( sprite_list, 0);
	} };
	
	public static VhUpdatePtr shootouj_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) {
/*TODO*///		get_sprite_info2();
/*TODO*///		sprite_update();
		draw_background( bitmap );
/*TODO*///		sprite_draw( sprite_list, 1);
		draw_foreground( bitmap );
/*TODO*///		sprite_draw( sprite_list, 0);
	} };
}
