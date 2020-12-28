package gr.codebb.arcadeflex.v036.mame;

import gr.codebb.arcadeflex.common.SubArrays.UShortArray;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.spriteH.*;
import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.common.libc.expressions.NOT;

public class spriteC {

    /*TODO*///
/*TODO*///#define SWAP(X,Y) { int temp = X; X = Y; Y = temp; }
/*TODO*///
/*TODO*///
    static int orientation, screen_width, screen_height;
    static int screen_clip_left, screen_clip_top, screen_clip_right, screen_clip_bottom;
    public static UBytePtr screen_baseaddr;
    static int screen_line_offset;

    static sprite_list first_sprite_list = null;
    /* used for resource tracking */

    static int FlickeringInvisible;

    public static abstract interface do_blitPtr {

        public abstract void handler(sprite[] sprite, int sprite_ptr);
    }

    static char[] shade_table;

    static void sprite_order_setup(sprite_list sprite_list, int[] first, int[] last, int[] delta) {
        if ((sprite_list.flags & SPRITE_LIST_FRONT_TO_BACK) != 0) {
            delta[0] = -1;
            first[0] = sprite_list.num_sprites - 1;
            last[0] = 0;
        } else {
            delta[0] = 1;
            first[0] = 0;
            last[0] = sprite_list.num_sprites - 1;
        }
    }

    /**
     * *******************************************************************
     *
     * The mask buffer is a dynamically allocated resource it is recycled each
     * frame. Using this technique reduced the runttime memory requirements of
     * the Gaiden from 512k (worst case) to approx 6K.
     *
     * Sprites use offsets instead of pointers directly to the mask data, since
     * it is potentially reallocated.
     *
     ********************************************************************
     */
    static UBytePtr mask_buffer = null;
    static int mask_buffer_size = 0;
    /* actual size of allocated buffer */

    static int mask_buffer_used = 0;

    static void mask_buffer_reset() {
        mask_buffer_used = 0;
    }

    /*TODO*///static void mask_buffer_dispose( void ){
/*TODO*///	free( mask_buffer );
/*TODO*///	mask_buffer = NULL;
/*TODO*///	mask_buffer_size = 0;
/*TODO*///}
    static int mask_buffer_alloc(int size) {
        int result = mask_buffer_used;
        int req_size = mask_buffer_used + size;
        if (req_size > mask_buffer_size) {
            mask_buffer = new UBytePtr((int) req_size);//realloc( mask_buffer, req_size );
            mask_buffer_size = (int) req_size;
            if (errorlog != null) {
                fprintf(errorlog, "increased sprite mask buffer size to %d bytes.\n", mask_buffer_size);
                if (mask_buffer == null) {
                    fprintf(errorlog, "Error! insufficient memory for mask_buffer_alloc\n");
                }
            }
        }
        mask_buffer_used = (int) req_size;
        memset(mask_buffer, result, 0x00, size);
        /* clear it */

        return result;
    }

    /*TODO*///
/*TODO*///#define BLIT \
/*TODO*///if( sprite->flags&SPRITE_FLIPX ){ \
/*TODO*///	source += screenx + flipx_adjust; \
/*TODO*///	for( y=y1; y<y2; y++ ){ \
/*TODO*///		for( x=x1; x<x2; x++ ){ \
/*TODO*///			if( OPAQUE(-x) ) dest[x] = COLOR(-x); \
/*TODO*///		} \
/*TODO*///		source += source_dy; dest += blit.line_offset; \
/*TODO*///		NEXTLINE \
/*TODO*///	} \
/*TODO*///} \
/*TODO*///else { \
/*TODO*///	source -= screenx; \
/*TODO*///	for( y=y1; y<y2; y++ ){ \
/*TODO*///		for( x=x1; x<x2; x++ ){ \
/*TODO*///			if( OPAQUE(x) ) dest[x] = COLOR(x); \
/*TODO*///			\
/*TODO*///		} \
/*TODO*///		source += source_dy; dest += blit.line_offset; \
/*TODO*///		NEXTLINE \
/*TODO*///	} \
/*TODO*///}
    public static class blit {

        public static int transparent_pen;
        public static int clip_left, clip_right, clip_top, clip_bottom;
        public static UBytePtr baseaddr;
        public static int line_offset;
        public static int write_to_mask;
        public static int origin_x, origin_y;
    };

    public static do_blitPtr do_blit_unpack = new do_blitPtr() {
        public void handler(sprite[] sprite, int sprite_ptr) {
            UShortArray pal_data = sprite[sprite_ptr].pal_data;
            int transparent_pen = blit.transparent_pen;

            int screenx = sprite[sprite_ptr].x - blit.origin_x;
            int screeny = sprite[sprite_ptr].y - blit.origin_y;
            int x1 = screenx;
            int y1 = screeny;
            int x2 = x1 + sprite[sprite_ptr].total_width;
            int y2 = y1 + sprite[sprite_ptr].total_height;
            int flipx_adjust = sprite[sprite_ptr].total_width - 1;

            int source_dy;
            UBytePtr baseaddr = new UBytePtr(sprite[sprite_ptr].pen_data);
            UBytePtr source;
            UBytePtr dest;
            int x, y;

            source = new UBytePtr(baseaddr, sprite[sprite_ptr].line_offset * sprite[sprite_ptr].y_offset + sprite[sprite_ptr].x_offset);

            if (x1 < blit.clip_left) {
                x1 = blit.clip_left;
            }
            if (y1 < blit.clip_top) {
                y1 = blit.clip_top;
            }
            if (x2 > blit.clip_right) {
                x2 = blit.clip_right;
            }
            if (y2 > blit.clip_bottom) {
                y2 = blit.clip_bottom;
            }

            if (x1 < x2 && y1 < y2) {
                dest = new UBytePtr(blit.baseaddr, y1 * blit.line_offset);
                if ((sprite[sprite_ptr].flags & SPRITE_FLIPY) != 0) {
                    source_dy = -sprite[sprite_ptr].line_offset;
                    source.offset += (y2 - 1 - screeny) * sprite[sprite_ptr].line_offset;
                } else {
                    source_dy = sprite[sprite_ptr].line_offset;
                    source.offset += (y1 - screeny) * sprite[sprite_ptr].line_offset;
                }
                if (blit.write_to_mask != 0) {
                    if ((sprite[sprite_ptr].flags & SPRITE_FLIPX) != 0) {
                        source.inc((screenx + flipx_adjust));
                        for (y = y1; y < y2; y++) {
                            for (x = x1; x < x2; x++) {
                                if (source.read(-x) != transparent_pen) {
                                    dest.write(x, 0xff);
                                }
                            }
                            source.inc(source_dy);
                            dest.inc(blit.line_offset);

                        }
                    } else {
                        source.dec(screenx);
                        for (y = y1; y < y2; y++) {
                            for (x = x1; x < x2; x++) {
                                if (source.read(x) != transparent_pen) {
                                    dest.write(x, 0xff);
                                }

                            }
                            source.inc(source_dy);
                            dest.inc(blit.line_offset);

                        }
                    }
                } else if (sprite[sprite_ptr].mask_offset >= 0) {
                    /* draw a masked sprite */
                    UBytePtr mask = new UBytePtr(mask_buffer, sprite[sprite_ptr].mask_offset
                            + (y1 - sprite[sprite_ptr].y) * sprite[sprite_ptr].total_width - sprite[sprite_ptr].x);
                    if ((sprite[sprite_ptr].flags & SPRITE_FLIPX) != 0) {
                        source.inc((screenx + flipx_adjust));
                        for (y = y1; y < y2; y++) {
                            for (x = x1; x < x2; x++) {
                                if (mask.read(x) == 0 && source.read(-x) != transparent_pen) {
                                    dest.write(x, pal_data.read(source.read(-x)));
                                }
                            }
                            source.inc(source_dy);
                            dest.inc(blit.line_offset);
                            mask.offset += sprite[sprite_ptr].total_width;

                        }
                    } else {
                        source.dec(screenx);
                        for (y = y1; y < y2; y++) {
                            for (x = x1; x < x2; x++) {
                                if (mask.read(x) == 0 && source.read(x) != transparent_pen) {
                                    dest.write(x, pal_data.read(source.read(x)));
                                }

                            }
                            source.inc(source_dy);
                            dest.inc(blit.line_offset);
                            mask.offset += sprite[sprite_ptr].total_width;

                        }
                    }
                } else if ((sprite[sprite_ptr].flags & SPRITE_TRANSPARENCY_THROUGH) != 0) {
                    int color = Machine.pens[palette_transparent_color];
                    if ((sprite[sprite_ptr].flags & SPRITE_FLIPX) != 0) {
                        source.inc((screenx + flipx_adjust));
                        for (y = y1; y < y2; y++) {
                            for (x = x1; x < x2; x++) {
                                if (dest.read(x) == color && source.read(-x) != transparent_pen) {
                                    dest.write(x, pal_data.read(source.read(-x)));
                                }
                            }
                            source.inc(source_dy);
                            dest.inc(blit.line_offset);

                        }
                    } else {
                        source.dec(screenx);
                        for (y = y1; y < y2; y++) {
                            for (x = x1; x < x2; x++) {
                                if (dest.read(x) == color && source.read(x) != transparent_pen) {
                                    dest.write(x, pal_data.read(source.read(x)));
                                }

                            }
                            source.inc(source_dy);
                            dest.inc(blit.line_offset);
                        }
                    }
                } else if (pal_data != null) {
                    if ((sprite[sprite_ptr].flags & SPRITE_FLIPX) != 0) {
                        source.inc((screenx + flipx_adjust));
                        for (y = y1; y < y2; y++) {
                            for (x = x1; x < x2; x++) {
                                if (source.read(-x) != transparent_pen) {
                                    dest.write(x, pal_data.read(source.read(-x)));
                                }
                            }
                            source.inc(source_dy);
                            dest.inc(blit.line_offset);

                        }
                    } else {
                        source.dec(screenx);
                        for (y = y1; y < y2; y++) {
                            for (x = x1; x < x2; x++) {
                                if (source.read(x) != transparent_pen) {
                                    dest.write(x, pal_data.read(source.read(x)));
                                }

                            }
                            source.inc(source_dy);
                            dest.inc(blit.line_offset);

                        }
                    }
                }
            }
        }
    };
    public static do_blitPtr do_blit_stack = new do_blitPtr() {
        public void handler(sprite[] sprite, int sprite_ptr) {
            UShortArray pal_data = sprite[sprite_ptr].pal_data;
            int transparent_pen = blit.transparent_pen;
            int flipx_adjust = sprite[sprite_ptr].tile_width - 1;

            int xoffset, yoffset;
            int screenx, screeny;
            int x1, y1, x2, y2;
            int x, y;

            int source_dy;
            UBytePtr baseaddr = new UBytePtr(sprite[sprite_ptr].pen_data);
            UBytePtr source;
            UBytePtr dest;

            for (xoffset = 0; xoffset < sprite[sprite_ptr].total_width; xoffset += sprite[sprite_ptr].tile_width) {
                for (yoffset = 0; yoffset < sprite[sprite_ptr].total_height; yoffset += sprite[sprite_ptr].tile_height) {
                    source = new UBytePtr(baseaddr);
                    screenx = sprite[sprite_ptr].x - blit.origin_x;
                    screeny = sprite[sprite_ptr].y - blit.origin_y;

                    if ((sprite[sprite_ptr].flags & SPRITE_FLIPX) != 0) {
                        screenx += sprite[sprite_ptr].total_width - sprite[sprite_ptr].tile_width - xoffset;
                    } else {
                        screenx += xoffset;
                    }

                    if ((sprite[sprite_ptr].flags & SPRITE_FLIPY) != 0) {
                        screeny += sprite[sprite_ptr].total_height - sprite[sprite_ptr].tile_height - yoffset;
                    } else {
                        screeny += yoffset;
                    }

                    x1 = screenx;
                    y1 = screeny;
                    x2 = x1 + sprite[sprite_ptr].tile_width;
                    y2 = y1 + sprite[sprite_ptr].tile_height;

                    if (x1 < blit.clip_left) {
                        x1 = blit.clip_left;
                    }
                    if (y1 < blit.clip_top) {
                        y1 = blit.clip_top;
                    }
                    if (x2 > blit.clip_right) {
                        x2 = blit.clip_right;
                    }
                    if (y2 > blit.clip_bottom) {
                        y2 = blit.clip_bottom;
                    }

                    if (x1 < x2 && y1 < y2) {
                        dest = new UBytePtr(blit.baseaddr, y1 * blit.line_offset);

                        if ((sprite[sprite_ptr].flags & SPRITE_FLIPY) != 0) {
                            source_dy = -sprite[sprite_ptr].line_offset;
                            source.inc((y2 - 1 - screeny) * sprite[sprite_ptr].line_offset);
                        } else {
                            source_dy = sprite[sprite_ptr].line_offset;
                            source.inc((y1 - screeny) * sprite[sprite_ptr].line_offset);
                        }

                        if (blit.write_to_mask != 0) {
                            if ((sprite[sprite_ptr].flags & SPRITE_FLIPX) != 0) {
                                source.inc((screenx + flipx_adjust));
                                for (y = y1; y < y2; y++) {
                                    for (x = x1; x < x2; x++) {
                                        if (source.read(-x) != transparent_pen) {
                                            dest.write(x, 0xff);
                                        }
                                    }
                                    source.inc(source_dy);
                                    dest.inc(blit.line_offset);

                                }
                            } else {
                                source.dec(screenx);
                                for (y = y1; y < y2; y++) {
                                    for (x = x1; x < x2; x++) {
                                        if (source.read(x) != transparent_pen) {
                                            dest.write(x, 0xff);
                                        }

                                    }
                                    source.inc(source_dy);
                                    dest.inc(blit.line_offset);

                                }
                            }
                        } else if (sprite[sprite_ptr].mask_offset >= 0) {
                            /* draw a masked sprite */
                            UBytePtr mask = new UBytePtr(mask_buffer, sprite[sprite_ptr].mask_offset
                                    + (y1 - sprite[sprite_ptr].y) * sprite[sprite_ptr].total_width - sprite[sprite_ptr].x);
                            if ((sprite[sprite_ptr].flags & SPRITE_FLIPX) != 0) {
                                source.inc((screenx + flipx_adjust));
                                for (y = y1; y < y2; y++) {
                                    for (x = x1; x < x2; x++) {
                                        if (mask.read(x) == 0 && source.read(-x) != transparent_pen) {
                                            dest.write(x, pal_data.read(source.read(-x)));
                                        }
                                    }
                                    source.inc(source_dy);
                                    dest.inc(blit.line_offset);
                                    mask.offset += sprite[sprite_ptr].total_width;

                                }
                            } else {
                                source.dec(screenx);
                                for (y = y1; y < y2; y++) {
                                    for (x = x1; x < x2; x++) {
                                        if (mask.read(x) == 0 && source.read(x) != transparent_pen) {
                                            dest.write(x, pal_data.read(source.read(x)));
                                        }

                                    }
                                    source.inc(source_dy);
                                    dest.inc(blit.line_offset);
                                    mask.offset += sprite[sprite_ptr].total_width;

                                }
                            }
                        } else if ((sprite[sprite_ptr].flags & SPRITE_TRANSPARENCY_THROUGH) != 0) {
                            int color = Machine.pens[palette_transparent_color];
                            if ((sprite[sprite_ptr].flags & SPRITE_FLIPX) != 0) {
                                source.inc((screenx + flipx_adjust));
                                for (y = y1; y < y2; y++) {
                                    for (x = x1; x < x2; x++) {
                                        if (dest.read(x) == color && source.read(-x) != transparent_pen) {
                                            dest.write(x, pal_data.read(source.read(-x)));
                                        }
                                    }
                                    source.inc(source_dy);
                                    dest.inc(blit.line_offset);

                                }
                            } else {
                                source.dec(screenx);
                                for (y = y1; y < y2; y++) {
                                    for (x = x1; x < x2; x++) {
                                        if (dest.read(x) == color && source.read(x) != transparent_pen) {
                                            dest.write(x, pal_data.read(source.read(x)));
                                        }

                                    }
                                    source.inc(source_dy);
                                    dest.inc(blit.line_offset);
                                }
                            }
                        } else if (pal_data != null) {
                            if ((sprite[sprite_ptr].flags & SPRITE_FLIPX) != 0) {
                                source.inc((screenx + flipx_adjust));
                                for (y = y1; y < y2; y++) {
                                    for (x = x1; x < x2; x++) {
                                        if (source.read(-x) != transparent_pen) {
                                            dest.write(x, pal_data.read(source.read(-x)));
                                        }
                                    }
                                    source.inc(source_dy);
                                    dest.inc(blit.line_offset);

                                }
                            } else {
                                source.dec(screenx);
                                for (y = y1; y < y2; y++) {
                                    for (x = x1; x < x2; x++) {
                                        if (source.read(x) != transparent_pen) {
                                            dest.write(x, pal_data.read(source.read(x)));
                                        }

                                    }
                                    source.inc(source_dy);
                                    dest.inc(blit.line_offset);

                                }
                            }
                        }
                    }
                    /* not totally clipped */

                    baseaddr.inc(sprite[sprite_ptr].tile_height * sprite[sprite_ptr].line_offset);
                }
                /* next yoffset */

            }
            /* next xoffset */

        }
    };
    public static do_blitPtr do_blit_zoom = new do_blitPtr() {
        public void handler(sprite[] sprite, int sprite_ptr) {
            /*	assumes SPRITE_LIST_RAW_DATA flag is set */

            int x1, x2, y1, y2, dx, dy;
            int xcount0 = 0, ycount0 = 0;

            if ((sprite[sprite_ptr].flags & SPRITE_FLIPX) != 0) {
                x2 = sprite[sprite_ptr].x;
                x1 = x2 + sprite[sprite_ptr].total_width;
                dx = -1;
                if (x2 < blit.clip_left) {
                    x2 = blit.clip_left;
                }
                if (x1 > blit.clip_right) {
                    xcount0 = (x1 - blit.clip_right) * sprite[sprite_ptr].tile_width;
                    x1 = blit.clip_right;
                }
                if (x2 >= x1) {
                    return;
                }
                x1--;
                x2--;
            } else {
                x1 = sprite[sprite_ptr].x;
                x2 = x1 + sprite[sprite_ptr].total_width;
                dx = 1;
                if (x1 < blit.clip_left) {
                    xcount0 = (blit.clip_left - x1) * sprite[sprite_ptr].tile_width;
                    x1 = blit.clip_left;
                }
                if (x2 > blit.clip_right) {
                    x2 = blit.clip_right;
                }
                if (x1 >= x2) {
                    return;
                }
            }
            if ((sprite[sprite_ptr].flags & SPRITE_FLIPY) != 0) {
                y2 = sprite[sprite_ptr].y;
                y1 = y2 + sprite[sprite_ptr].total_height;
                dy = -1;
                if (y2 < blit.clip_top) {
                    y2 = blit.clip_top;
                }
                if (y1 > blit.clip_bottom) {
                    ycount0 = (y1 - blit.clip_bottom) * sprite[sprite_ptr].tile_height;
                    y1 = blit.clip_bottom;
                }
                if (y2 >= y1) {
                    return;
                }
                y1--;
                y2--;
            } else {
                y1 = sprite[sprite_ptr].y;
                y2 = y1 + sprite[sprite_ptr].total_height;
                dy = 1;
                if (y1 < blit.clip_top) {
                    ycount0 = (blit.clip_top - y1) * sprite[sprite_ptr].tile_height;
                    y1 = blit.clip_top;
                }
                if (y2 > blit.clip_bottom) {
                    y2 = blit.clip_bottom;
                }
                if (y1 >= y2) {
                    return;
                }
            }

            if ((sprite[sprite_ptr].flags & (SPRITE_SHADOW | SPRITE_PARTIAL_SHADOW)) == 0) {
                UBytePtr pen_data = new UBytePtr(sprite[sprite_ptr].pen_data);
                UShortArray pal_data = sprite[sprite_ptr].pal_data;
                int x, y;
                /*unsigned*/ char pen;
                int pitch = blit.line_offset * dy;
                UBytePtr dest = new UBytePtr(blit.baseaddr, blit.line_offset * y1);
                int ycount = ycount0;

                if ((orientation & ORIENTATION_SWAP_XY) != 0) {
                    /* manually rotate the sprite graphics */
                    int xcount = xcount0;
                    for (x = x1; x != x2; x += dx) {
                        UBytePtr source;
                        UBytePtr dest1;

                        ycount = ycount0;
                        while (xcount >= sprite[sprite_ptr].total_width) {
                            xcount -= sprite[sprite_ptr].total_width;
                            pen_data.inc(sprite[sprite_ptr].line_offset);
                        }
                        source = new UBytePtr(pen_data);
                        dest1 = new UBytePtr(dest, x);
                        boolean isskip = false;
                        for (y = y1; y != y2; y += dy) {
                            while (ycount >= sprite[sprite_ptr].total_height) {
                                ycount -= sprite[sprite_ptr].total_height;
                                source.offset++;
                            }
                            pen = (char)(source.read()&0xFF);
                            if (pen == 0xff) {
                                xcount += sprite[sprite_ptr].tile_width;
                                isskip = true;
                                break;
                                //goto skip1;/* marker for right side of sprite; needed for AltBeast, ESwat */
                            }
                            /*					if( pen==10 ) *dest1 = shade_table[*dest1];
					else */
                            if (pen != 0) {
                                dest1.write(pal_data.read(pen));
                            }
                            ycount += sprite[sprite_ptr].tile_height;
                            dest1.offset += pitch;
                        }
//skip1:                
                        if (!isskip) {
                            xcount += sprite[sprite_ptr].tile_width;
                        }
                    }
                } else {
                    for (y = y1; y != y2; y += dy) {
                        int xcount = xcount0;
                        UBytePtr source;
                        while (ycount >= sprite[sprite_ptr].total_height) {
                            ycount -= sprite[sprite_ptr].total_height;
                            pen_data.inc(sprite[sprite_ptr].line_offset);
                        }
                        source = new UBytePtr(pen_data);
                        boolean isskip = false;
                        for (x = x1; x != x2; x += dx) {
                            while (xcount >= sprite[sprite_ptr].total_width) {
                                xcount -= sprite[sprite_ptr].total_width;
                                source.offset++;
                            }
                            pen = (char) (source.read() & 0xFF);
                            if (pen == 0xff) {
                                //continue skip;/* marker for right side of sprite; needed for AltBeast, ESwat */
                                ycount += sprite[sprite_ptr].tile_height;
                                dest.offset += pitch;
                                isskip = true;
                                break;
                            }
                            /*					if( pen==10 ) dest[x] = shade_table[dest[x]];
					else */
                            if (pen != 0) {
                                dest.write(x, pal_data.read(pen));
                            }
                            xcount += sprite[sprite_ptr].tile_width;
                        }
//skip:
                        if (!isskip) {
                            ycount += sprite[sprite_ptr].tile_height;
                            dest.offset += pitch;
                        }
                    }
                }
            } else if ((sprite[sprite_ptr].flags & SPRITE_PARTIAL_SHADOW) != 0) {
                System.out.println("2st case");
                /*TODO*///		const unsigned char *pen_data = sprite[sprite_ptr].pen_data;
/*TODO*///		const unsigned short *pal_data = sprite[sprite_ptr].pal_data;
/*TODO*///		int x,y;
/*TODO*///		unsigned char pen;
/*TODO*///		int pitch = blit.line_offset*dy;
/*TODO*///		unsigned char *dest = blit.baseaddr + blit.line_offset*y1;
/*TODO*///		int ycount = ycount0;
/*TODO*///
/*TODO*///		if( orientation & ORIENTATION_SWAP_XY ){ /* manually rotate the sprite graphics */
/*TODO*///			int xcount = xcount0;
/*TODO*///			for( x=x1; x!=x2; x+=dx ){
/*TODO*///				const unsigned char *source;
/*TODO*///				unsigned char *dest1;
/*TODO*///
/*TODO*///				ycount = ycount0;
/*TODO*///				while( xcount>=sprite[sprite_ptr].total_width ){
/*TODO*///					xcount -= sprite[sprite_ptr].total_width;
/*TODO*///					pen_data+=sprite[sprite_ptr].line_offset;
/*TODO*///				}
/*TODO*///				source = pen_data;
/*TODO*///				dest1 = &dest[x];
/*TODO*///				for( y=y1; y!=y2; y+=dy ){
/*TODO*///					while( ycount>=sprite[sprite_ptr].total_height ){
/*TODO*///						ycount -= sprite[sprite_ptr].total_height;
/*TODO*///						source ++;
/*TODO*///					}
/*TODO*///					pen = *source;
/*TODO*///					if( pen==0xff ) goto skip6; /* marker for right side of sprite; needed for AltBeast, ESwat */
/*TODO*///					if( pen==sprite[sprite_ptr].shadow_pen ) *dest1 = shade_table[*dest1];
/*TODO*///					else if( pen ) *dest1 = pal_data[pen];
/*TODO*///					ycount+= sprite[sprite_ptr].tile_height;
/*TODO*///					dest1 += pitch;
/*TODO*///				}
/*TODO*///skip6:
/*TODO*///				xcount += sprite[sprite_ptr].tile_width;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else {
/*TODO*///			for( y=y1; y!=y2; y+=dy ){
/*TODO*///				int xcount = xcount0;
/*TODO*///				const unsigned char *source;
/*TODO*///				while( ycount>=sprite[sprite_ptr].total_height ){
/*TODO*///					ycount -= sprite[sprite_ptr].total_height;
/*TODO*///					pen_data += sprite[sprite_ptr].line_offset;
/*TODO*///				}
/*TODO*///				source = pen_data;
/*TODO*///				for( x=x1; x!=x2; x+=dx ){
/*TODO*///					while( xcount>=sprite[sprite_ptr].total_width ){
/*TODO*///						xcount -= sprite[sprite_ptr].total_width;
/*TODO*///						source++;
/*TODO*///					}
/*TODO*///					pen = *source;
/*TODO*///					if( pen==0xff ) goto skip5; /* marker for right side of sprite; needed for AltBeast, ESwat */
/*TODO*///					if( pen==sprite[sprite_ptr].shadow_pen ) dest[x] = shade_table[dest[x]];
/*TODO*///					else if( pen ) dest[x] = pal_data[pen];
/*TODO*///					xcount += sprite[sprite_ptr].tile_width;
/*TODO*///				}
/*TODO*///skip5:
/*TODO*///				ycount += sprite[sprite_ptr].tile_height;
/*TODO*///				dest += pitch;
/*TODO*///			}
/*TODO*///		}
            } else {	// Shadow Sprite
                System.out.println("3st case");
                /*TODO*///		const unsigned char *pen_data = sprite[sprite_ptr].pen_data;
/*TODO*/////		const unsigned short *pal_data = sprite[sprite_ptr].pal_data;
/*TODO*///		int x,y;
/*TODO*///		unsigned char pen;
/*TODO*///		int pitch = blit.line_offset*dy;
/*TODO*///		unsigned char *dest = blit.baseaddr + blit.line_offset*y1;
/*TODO*///		int ycount = ycount0;
/*TODO*///
/*TODO*///		if( orientation & ORIENTATION_SWAP_XY ){ /* manually rotate the sprite graphics */
/*TODO*///			int xcount = xcount0;
/*TODO*///			for( x=x1; x!=x2; x+=dx ){
/*TODO*///				const unsigned char *source;
/*TODO*///				unsigned char *dest1;
/*TODO*///
/*TODO*///				ycount = ycount0;
/*TODO*///				while( xcount>=sprite[sprite_ptr].total_width ){
/*TODO*///					xcount -= sprite[sprite_ptr].total_width;
/*TODO*///					pen_data+=sprite[sprite_ptr].line_offset;
/*TODO*///				}
/*TODO*///				source = pen_data;
/*TODO*///				dest1 = &dest[x];
/*TODO*///				for( y=y1; y!=y2; y+=dy ){
/*TODO*///					while( ycount>=sprite[sprite_ptr].total_height ){
/*TODO*///						ycount -= sprite[sprite_ptr].total_height;
/*TODO*///						source ++;
/*TODO*///					}
/*TODO*///					pen = *source;
/*TODO*///					if( pen==0xff ) goto skip4; /* marker for right side of sprite; needed for AltBeast, ESwat */
/*TODO*///					if( pen ) *dest1 = shade_table[*dest1];
/*TODO*///					ycount+= sprite[sprite_ptr].tile_height;
/*TODO*///					dest1 += pitch;
/*TODO*///				}
/*TODO*///skip4:
/*TODO*///				xcount += sprite[sprite_ptr].tile_width;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else {
/*TODO*///			for( y=y1; y!=y2; y+=dy ){
/*TODO*///				int xcount = xcount0;
/*TODO*///				const unsigned char *source;
/*TODO*///				while( ycount>=sprite[sprite_ptr].total_height ){
/*TODO*///					ycount -= sprite[sprite_ptr].total_height;
/*TODO*///					pen_data += sprite[sprite_ptr].line_offset;
/*TODO*///				}
/*TODO*///				source = pen_data;
/*TODO*///				for( x=x1; x!=x2; x+=dx ){
/*TODO*///					while( xcount>=sprite[sprite_ptr].total_width ){
/*TODO*///						xcount -= sprite[sprite_ptr].total_width;
/*TODO*///						source++;
/*TODO*///					}
/*TODO*///					pen = *source;
/*TODO*///					if( pen==0xff ) goto skip3; /* marker for right side of sprite; needed for AltBeast, ESwat */
/*TODO*///					if( pen ) dest[x] = shade_table[dest[x]];
/*TODO*///					xcount += sprite[sprite_ptr].tile_width;
/*TODO*///				}
/*TODO*///skip3:
/*TODO*///				ycount += sprite[sprite_ptr].tile_height;
/*TODO*///				dest += pitch;
/*TODO*///			}
/*TODO*///		}
            }

        }
    };

    /*TODO*///static void do_blit_zoom16( const struct sprite *sprite ){
/*TODO*///	/*	assumes SPRITE_LIST_RAW_DATA flag is set */
/*TODO*///
/*TODO*///	int x1,x2, y1,y2, dx,dy;
/*TODO*///	int xcount0 = 0, ycount0 = 0;
/*TODO*///
/*TODO*///	if( sprite->flags & SPRITE_FLIPX ){
/*TODO*///		x2 = sprite->x;
/*TODO*///		x1 = x2+sprite->total_width;
/*TODO*///		dx = -1;
/*TODO*///		if( x2<blit.clip_left ) x2 = blit.clip_left;
/*TODO*///		if( x1>blit.clip_right ){
/*TODO*///			xcount0 = (x1-blit.clip_right)*sprite->tile_width;
/*TODO*///			x1 = blit.clip_right;
/*TODO*///		}
/*TODO*///		if( x2>=x1 ) return;
/*TODO*///		x1--; x2--;
/*TODO*///	}
/*TODO*///	else {
/*TODO*///		x1 = sprite->x;
/*TODO*///		x2 = x1+sprite->total_width;
/*TODO*///		dx = 1;
/*TODO*///		if( x1<blit.clip_left ){
/*TODO*///			xcount0 = (blit.clip_left-x1)*sprite->tile_width;
/*TODO*///			x1 = blit.clip_left;
/*TODO*///		}
/*TODO*///		if( x2>blit.clip_right ) x2 = blit.clip_right;
/*TODO*///		if( x1>=x2 ) return;
/*TODO*///	}
/*TODO*///	if( sprite->flags & SPRITE_FLIPY ){
/*TODO*///		y2 = sprite->y;
/*TODO*///		y1 = y2+sprite->total_height;
/*TODO*///		dy = -1;
/*TODO*///		if( y2<blit.clip_top ) y2 = blit.clip_top;
/*TODO*///		if( y1>blit.clip_bottom ){
/*TODO*///			ycount0 = (y1-blit.clip_bottom)*sprite->tile_height;
/*TODO*///			y1 = blit.clip_bottom;
/*TODO*///		}
/*TODO*///		if( y2>=y1 ) return;
/*TODO*///		y1--; y2--;
/*TODO*///	}
/*TODO*///	else {
/*TODO*///		y1 = sprite->y;
/*TODO*///		y2 = y1+sprite->total_height;
/*TODO*///		dy = 1;
/*TODO*///		if( y1<blit.clip_top ){
/*TODO*///			ycount0 = (blit.clip_top-y1)*sprite->tile_height;
/*TODO*///			y1 = blit.clip_top;
/*TODO*///		}
/*TODO*///		if( y2>blit.clip_bottom ) y2 = blit.clip_bottom;
/*TODO*///		if( y1>=y2 ) return;
/*TODO*///	}
/*TODO*///
/*TODO*///	if(!(sprite->flags & (SPRITE_SHADOW | SPRITE_PARTIAL_SHADOW)))
/*TODO*///	{
/*TODO*///		const unsigned char *pen_data = sprite->pen_data;
/*TODO*///		const unsigned short *pal_data = sprite->pal_data;
/*TODO*///		int x,y;
/*TODO*///		unsigned char pen;
/*TODO*///		int pitch = blit.line_offset*dy/2;
/*TODO*///		UINT16 *dest = (UINT16 *)(blit.baseaddr + blit.line_offset*y1);
/*TODO*///		int ycount = ycount0;
/*TODO*///
/*TODO*///		if( orientation & ORIENTATION_SWAP_XY ){ /* manually rotate the sprite graphics */
/*TODO*///			int xcount = xcount0;
/*TODO*///			for( x=x1; x!=x2; x+=dx ){
/*TODO*///				const unsigned char *source;
/*TODO*///				UINT16 *dest1;
/*TODO*///
/*TODO*///				ycount = ycount0;
/*TODO*///				while( xcount>=sprite->total_width ){
/*TODO*///					xcount -= sprite->total_width;
/*TODO*///					pen_data+=sprite->line_offset;
/*TODO*///				}
/*TODO*///				source = pen_data;
/*TODO*///				dest1 = &dest[x];
/*TODO*///				for( y=y1; y!=y2; y+=dy ){
/*TODO*///					while( ycount>=sprite->total_height ){
/*TODO*///						ycount -= sprite->total_height;
/*TODO*///						source ++;
/*TODO*///					}
/*TODO*///					pen = *source;
/*TODO*///					if( pen==0xff ) goto skip1; /* marker for right side of sprite; needed for AltBeast, ESwat */
/*TODO*////*					if( pen==10 ) *dest1 = shade_table[*dest1];
/*TODO*///					else */if( pen ) *dest1 = pal_data[pen];
/*TODO*///					ycount+= sprite->tile_height;
/*TODO*///					dest1 += pitch;
/*TODO*///				}
/*TODO*///skip1:
/*TODO*///				xcount += sprite->tile_width;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else {
/*TODO*///			for( y=y1; y!=y2; y+=dy ){
/*TODO*///				int xcount = xcount0;
/*TODO*///				const unsigned char *source;
/*TODO*///				while( ycount>=sprite->total_height ){
/*TODO*///					ycount -= sprite->total_height;
/*TODO*///					pen_data += sprite->line_offset;
/*TODO*///				}
/*TODO*///				source = pen_data;
/*TODO*///				for( x=x1; x!=x2; x+=dx ){
/*TODO*///					while( xcount>=sprite->total_width ){
/*TODO*///						xcount -= sprite->total_width;
/*TODO*///						source++;
/*TODO*///					}
/*TODO*///					pen = *source;
/*TODO*///					if( pen==0xff ) goto skip; /* marker for right side of sprite; needed for AltBeast, ESwat */
/*TODO*////*					if( pen==10 ) dest[x] = shade_table[dest[x]];
/*TODO*///					else */if( pen ) dest[x] = pal_data[pen];
/*TODO*///					xcount += sprite->tile_width;
/*TODO*///				}
/*TODO*///skip:
/*TODO*///				ycount += sprite->tile_height;
/*TODO*///				dest += pitch;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else if(sprite->flags & SPRITE_PARTIAL_SHADOW)
/*TODO*///	{
/*TODO*///		const unsigned char *pen_data = sprite->pen_data;
/*TODO*///		const unsigned short *pal_data = sprite->pal_data;
/*TODO*///		int x,y;
/*TODO*///		unsigned char pen;
/*TODO*///		int pitch = blit.line_offset*dy/2;
/*TODO*///		UINT16 *dest = (UINT16 *)(blit.baseaddr + blit.line_offset*y1);
/*TODO*///		int ycount = ycount0;
/*TODO*///
/*TODO*///		if( orientation & ORIENTATION_SWAP_XY ){ /* manually rotate the sprite graphics */
/*TODO*///			int xcount = xcount0;
/*TODO*///			for( x=x1; x!=x2; x+=dx ){
/*TODO*///				const unsigned char *source;
/*TODO*///				UINT16 *dest1;
/*TODO*///
/*TODO*///				ycount = ycount0;
/*TODO*///				while( xcount>=sprite->total_width ){
/*TODO*///					xcount -= sprite->total_width;
/*TODO*///					pen_data+=sprite->line_offset;
/*TODO*///				}
/*TODO*///				source = pen_data;
/*TODO*///				dest1 = &dest[x];
/*TODO*///				for( y=y1; y!=y2; y+=dy ){
/*TODO*///					while( ycount>=sprite->total_height ){
/*TODO*///						ycount -= sprite->total_height;
/*TODO*///						source ++;
/*TODO*///					}
/*TODO*///					pen = *source;
/*TODO*///					if( pen==0xff ) goto skip6; /* marker for right side of sprite; needed for AltBeast, ESwat */
/*TODO*///					if( pen==sprite->shadow_pen ) *dest1 = shade_table[*dest1];
/*TODO*///					else if( pen ) *dest1 = pal_data[pen];
/*TODO*///					ycount+= sprite->tile_height;
/*TODO*///					dest1 += pitch;
/*TODO*///				}
/*TODO*///skip6:
/*TODO*///				xcount += sprite->tile_width;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else {
/*TODO*///			for( y=y1; y!=y2; y+=dy ){
/*TODO*///				int xcount = xcount0;
/*TODO*///				const unsigned char *source;
/*TODO*///				while( ycount>=sprite->total_height ){
/*TODO*///					ycount -= sprite->total_height;
/*TODO*///					pen_data += sprite->line_offset;
/*TODO*///				}
/*TODO*///				source = pen_data;
/*TODO*///				for( x=x1; x!=x2; x+=dx ){
/*TODO*///					while( xcount>=sprite->total_width ){
/*TODO*///						xcount -= sprite->total_width;
/*TODO*///						source++;
/*TODO*///					}
/*TODO*///					pen = *source;
/*TODO*///					if( pen==0xff ) goto skip5; /* marker for right side of sprite; needed for AltBeast, ESwat */
/*TODO*///					if( pen==sprite->shadow_pen ) dest[x] = shade_table[dest[x]];
/*TODO*///					else if( pen ) dest[x] = pal_data[pen];
/*TODO*///					xcount += sprite->tile_width;
/*TODO*///				}
/*TODO*///skip5:
/*TODO*///				ycount += sprite->tile_height;
/*TODO*///				dest += pitch;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{	// Shadow Sprite
/*TODO*///		const unsigned char *pen_data = sprite->pen_data;
/*TODO*/////		const unsigned short *pal_data = sprite->pal_data;
/*TODO*///		int x,y;
/*TODO*///		unsigned char pen;
/*TODO*///		int pitch = blit.line_offset*dy/2;
/*TODO*///		UINT16 *dest = (UINT16 *)(blit.baseaddr + blit.line_offset*y1);
/*TODO*///		int ycount = ycount0;
/*TODO*///
/*TODO*///		if( orientation & ORIENTATION_SWAP_XY ){ /* manually rotate the sprite graphics */
/*TODO*///			int xcount = xcount0;
/*TODO*///			for( x=x1; x!=x2; x+=dx ){
/*TODO*///				const unsigned char *source;
/*TODO*///				UINT16 *dest1;
/*TODO*///
/*TODO*///				ycount = ycount0;
/*TODO*///				while( xcount>=sprite->total_width ){
/*TODO*///					xcount -= sprite->total_width;
/*TODO*///					pen_data+=sprite->line_offset;
/*TODO*///				}
/*TODO*///				source = pen_data;
/*TODO*///				dest1 = &dest[x];
/*TODO*///				for( y=y1; y!=y2; y+=dy ){
/*TODO*///					while( ycount>=sprite->total_height ){
/*TODO*///						ycount -= sprite->total_height;
/*TODO*///						source ++;
/*TODO*///					}
/*TODO*///					pen = *source;
/*TODO*///					if( pen==0xff ) goto skip4; /* marker for right side of sprite; needed for AltBeast, ESwat */
/*TODO*///					if( pen ) *dest1 = shade_table[*dest1];
/*TODO*///					ycount+= sprite->tile_height;
/*TODO*///					dest1 += pitch;
/*TODO*///				}
/*TODO*///skip4:
/*TODO*///				xcount += sprite->tile_width;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else {
/*TODO*///			for( y=y1; y!=y2; y+=dy ){
/*TODO*///				int xcount = xcount0;
/*TODO*///				const unsigned char *source;
/*TODO*///				while( ycount>=sprite->total_height ){
/*TODO*///					ycount -= sprite->total_height;
/*TODO*///					pen_data += sprite->line_offset;
/*TODO*///				}
/*TODO*///				source = pen_data;
/*TODO*///				for( x=x1; x!=x2; x+=dx ){
/*TODO*///					while( xcount>=sprite->total_width ){
/*TODO*///						xcount -= sprite->total_width;
/*TODO*///						source++;
/*TODO*///					}
/*TODO*///					pen = *source;
/*TODO*///					if( pen==0xff ) goto skip3; /* marker for right side of sprite; needed for AltBeast, ESwat */
/*TODO*///					if( pen ) dest[x] = shade_table[dest[x]];
/*TODO*///					xcount += sprite->tile_width;
/*TODO*///				}
/*TODO*///skip3:
/*TODO*///				ycount += sprite->tile_height;
/*TODO*///				dest += pitch;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///}
    /**
     * ******************************************************************
     */
    public static void sprite_init() {
        rectangle clip = Machine.drv.visible_area;
        int left = clip.min_x;
        int top = clip.min_y;
        int right = clip.max_x + 1;
        int bottom = clip.max_y + 1;

        osd_bitmap bitmap = Machine.scrbitmap;
        screen_baseaddr = bitmap.line[0];
        screen_line_offset = bitmap.line[1].offset - bitmap.line[0].offset;

        orientation = Machine.orientation;
        screen_width = Machine.scrbitmap.width;
        screen_height = Machine.scrbitmap.height;

        if ((orientation & ORIENTATION_SWAP_XY) != 0) {
            //SWAP(left,top)
            int temp = left;
            left = top;
            top = temp;
            //SWAP(right,bottom)
            int temp2 = right;
            right = bottom;
            bottom = temp2;
        }
        if ((orientation & ORIENTATION_FLIP_X) != 0) {
            //SWAP(left,right)
            int temp = left;
            left = right;
            right = temp;
            left = screen_width - left;
            right = screen_width - right;
        }
        if ((orientation & ORIENTATION_FLIP_Y) != 0) {
            //SWAP(top,bottom)
            int temp = top;
            top = bottom;
            bottom = temp;
            top = screen_height - top;
            bottom = screen_height - bottom;
        }

        screen_clip_left = left;
        screen_clip_right = right;
        screen_clip_top = top;
        screen_clip_bottom = bottom;
    }

    /*TODO*///void sprite_close( void ){
/*TODO*///	struct sprite_list *sprite_list = first_sprite_list;
/*TODO*///	mask_buffer_dispose();
/*TODO*///
/*TODO*///	while( sprite_list ){
/*TODO*///		struct sprite_list *next = sprite_list.next;
/*TODO*///		free( sprite_list.sprite );
/*TODO*///		free( sprite_list );
/*TODO*///		sprite_list = next;
/*TODO*///	}
/*TODO*///	first_sprite_list = NULL;
/*TODO*///}
/*TODO*///
    public static sprite_list sprite_list_create(int num_sprites, int flags) {
        sprite[] sprite = new sprite[num_sprites];
        for (int i = 0; i < sprite.length; i++) {
            sprite[i] = new sprite();
        }
        sprite_list sprite_list = new sprite_list();

        sprite_list.num_sprites = num_sprites;
        sprite_list.special_pen = -1;
        sprite_list.sprite = sprite;
        sprite_list.flags = flags;

        /* resource tracking */
        sprite_list.next = first_sprite_list;
        first_sprite_list = sprite_list;

        return sprite_list;
        /* warning: no error checking! */

    }

    static void sprite_update_helper(sprite_list sprite_list) {
        sprite[] sprite_table = sprite_list.sprite;

        /* initialize constants */
        blit.transparent_pen = sprite_list.transparent_pen;
        blit.write_to_mask = 1;
        blit.clip_left = 0;
        blit.clip_top = 0;

        /* make a pass to adjust for screen orientation */
        if ((orientation & ORIENTATION_SWAP_XY) != 0) {
            throw new UnsupportedOperationException("Unimplemented");
        }
        /*TODO*///		struct sprite *sprite = sprite_table;
/*TODO*///		const struct sprite *finish = &sprite[sprite_list->num_sprites];
/*TODO*///		while( sprite<finish ){
/*TODO*///			SWAP(sprite->x, sprite->y)
/*TODO*///			SWAP(sprite->total_height,sprite->total_width)
/*TODO*///			SWAP(sprite->tile_width,sprite->tile_height)
/*TODO*///			SWAP(sprite->x_offset,sprite->y_offset)
/*TODO*///
/*TODO*///			/* we must also swap the flipx and flipy bits (if they aren't identical) */
/*TODO*///			if( sprite->flags&SPRITE_FLIPX ){
/*TODO*///				if( !(sprite->flags&SPRITE_FLIPY) ){
/*TODO*///					sprite->flags = (sprite->flags&~SPRITE_FLIPX)|SPRITE_FLIPY;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			else {
/*TODO*///				if( sprite->flags&SPRITE_FLIPY ){
/*TODO*///					sprite->flags = (sprite->flags&~SPRITE_FLIPY)|SPRITE_FLIPX;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			sprite++;
/*TODO*///		}
        if ((orientation & ORIENTATION_FLIP_X) != 0) {
            throw new UnsupportedOperationException("Unimplemented");

            /*TODO*///		struct sprite *sprite = sprite_table;
/*TODO*///		const struct sprite *finish = &sprite[sprite_list->num_sprites];
/*TODO*///#ifndef PREROTATE_GFX
/*TODO*///		int toggle_bit = SPRITE_FLIPX;
/*TODO*///#else
/*TODO*///		int toggle_bit = (sprite_list->flags & SPRITE_LIST_RAW_DATA)?SPRITE_FLIPX:0;
/*TODO*///#endif
/*TODO*///		while( sprite<finish ){
/*TODO*///			sprite->x = screen_width - (sprite->x+sprite->total_width);
/*TODO*///			sprite->flags ^= toggle_bit;
/*TODO*///
/*TODO*///			/* extra processing for packed sprites */
/*TODO*///			sprite->x_offset = sprite->tile_width - (sprite->x_offset+sprite->total_width);
/*TODO*///			sprite++;
/*TODO*///		}
        }
        if ((orientation & ORIENTATION_FLIP_Y) != 0) {
            throw new UnsupportedOperationException("Unimplemented");
            /*TODO*///		struct sprite *sprite = sprite_table;
/*TODO*///		const struct sprite *finish = &sprite[sprite_list->num_sprites];
/*TODO*///#ifndef PREROTATE_GFX
/*TODO*///		int toggle_bit = SPRITE_FLIPY;
/*TODO*///#else
/*TODO*///		int toggle_bit = (sprite_list->flags & SPRITE_LIST_RAW_DATA)?SPRITE_FLIPY:0;
/*TODO*///#endif
/*TODO*///		while( sprite<finish ){
/*TODO*///			sprite->y = screen_height - (sprite->y+sprite->total_height);
/*TODO*///			sprite->flags ^= toggle_bit;
/*TODO*///
/*TODO*///			/* extra processing for packed sprites */
/*TODO*///			sprite->y_offset = sprite->tile_height - (sprite->y_offset+sprite->total_height);
/*TODO*///			sprite++;
/*TODO*///		}
        }
        {
            /* visibility check */

            sprite[] sprite = sprite_table;
            int sprite_ptr = 0;
            //const struct sprite *finish = &sprite[sprite_list->num_sprites];
            int finish = sprite_list.num_sprites;
            while (sprite_ptr < finish) {
                if ((FlickeringInvisible != 0 && (sprite[sprite_ptr].flags & SPRITE_FLICKER) != 0)
                        || sprite[sprite_ptr].total_width <= 0 || sprite[sprite_ptr].total_height <= 0
                        || sprite[sprite_ptr].x + sprite[sprite_ptr].total_width <= 0 || sprite[sprite_ptr].x >= screen_width
                        || sprite[sprite_ptr].y + sprite[sprite_ptr].total_height <= 0 || sprite[sprite_ptr].y >= screen_height) {
                    sprite[sprite_ptr].flags &= (~SPRITE_VISIBLE);
                }
                sprite_ptr++;
            }
        }
        {
            do_blitPtr do_blit;
            int j;
            int[] i = new int[1];
            int[] dir = new int[1];
            int[] last = new int[1];

            switch (sprite_list.sprite_type) {
                case SPRITE_TYPE_ZOOM:
                    do_blit = do_blit_zoom;
                    break;

                case SPRITE_TYPE_STACK:
                    do_blit = do_blit_stack;
                    break;

                case SPRITE_TYPE_UNPACK:
                default:
                    do_blit = do_blit_unpack;
                    break;
            }

            sprite_order_setup(sprite_list, i, last, dir);

            for (;;) {
                /* process each sprite */

                sprite[] sprite = sprite_table;
                int sprite_ptr = i[0];
                sprite[sprite_ptr].mask_offset = -1;

                if ((sprite[sprite_ptr].flags & SPRITE_VISIBLE) != 0) {
                    int priority = sprite[sprite_ptr].priority;

                    if (palette_used_colors != null) {
                        int pen_usage = sprite[sprite_ptr].pen_usage;
                        int indx = sprite[sprite_ptr].pal_data.offset;//memory.length - Machine.remapped_colortable.length;
                        while (pen_usage != 0) {
                            if ((pen_usage & 1) != 0) {
                                palette_used_colors.write(indx, PALETTE_COLOR_USED);
                            }
                            pen_usage >>= 1;
                            indx++;
                        }
                    }

                    if (i[0] != last[0] && priority < sprite_list.max_priority) {
                        blit.origin_x = sprite[sprite_ptr].x;
                        blit.origin_y = sprite[sprite_ptr].y;
                        /* clip_left and clip_right are always zero */
                        blit.clip_right = sprite[sprite_ptr].total_width;
                        blit.clip_bottom = sprite[sprite_ptr].total_height;
                        /*
                         The following loop ensures that even though we are drawing all priority 3
                         sprites before drawing the priority 2 sprites, and priority 2 sprites before the
                         priority 1 sprites, that the sprite order as a whole still dictates
                         sprite-to-sprite priority when sprite pixels overlap and aren't obscured by a
                         background.  Checks are done to avoid special handling for the cases where
                         masking isn't needed.

                         max priority sprites are always drawn first, so we don't need to do anything
                         special to cause them to be obscured by other sprites
                         */
                        j = i[0] + dir[0];
                        for (;;) {
                            sprite[] front = sprite_table;//[j];
                            int spr_ptr = j;
                            if ((front[spr_ptr].flags & SPRITE_VISIBLE) != 0 && front[spr_ptr].priority > priority) {

                                if (front[spr_ptr].x < sprite[sprite_ptr].x + sprite[sprite_ptr].total_width
                                        && front[spr_ptr].y < sprite[sprite_ptr].y + sprite[sprite_ptr].total_height
                                        && front[spr_ptr].x + front[spr_ptr].total_width > sprite[sprite_ptr].x
                                        && front[spr_ptr].y + front[spr_ptr].total_height > sprite[sprite_ptr].y) {
                                    /* uncomment the following line to see which sprites are corrected */
                                    //sprite[sprite_ptr].pal_data = new CharPtr(Machine.remapped_colortable,(rand()&0xff));

                                    if (sprite[sprite_ptr].mask_offset < 0) {
                                        /* first masking? */

                                        sprite[sprite_ptr].mask_offset = mask_buffer_alloc(sprite[sprite_ptr].total_width * sprite[sprite_ptr].total_height);
                                        blit.line_offset = sprite[sprite_ptr].total_width;
                                        blit.baseaddr = new UBytePtr(mask_buffer, sprite[sprite_ptr].mask_offset);
                                    }
                                    do_blit.handler(front, spr_ptr);
                                }
                            }
                            if (j == last[0]) {
                                break;
                            }
                            j += dir[0];
                        }
                        /* next j */

                    }
                    /* priority<SPRITE_MAX_PRIORITY */

                }
                /* visible */

                if (i[0] == last[0]) {
                    break;
                }
                i[0] += dir[0];
            }
            /* next i */

        }
    }

    public static void sprite_update() {
        sprite_list sprite_list = first_sprite_list;
        mask_buffer_reset();
        FlickeringInvisible = NOT(FlickeringInvisible);
        while (sprite_list != null) {
            sprite_update_helper(sprite_list);
            sprite_list = sprite_list.next;
        }
    }

    public static void sprite_draw(sprite_list sprite_list, int priority) {
        sprite[] sprite_table = sprite_list.sprite;

        {
            /* set constants */

            blit.origin_x = 0;
            blit.origin_y = 0;

            blit.baseaddr = screen_baseaddr;
            blit.line_offset = screen_line_offset;
            blit.transparent_pen = sprite_list.transparent_pen;
            blit.write_to_mask = 0;

            blit.clip_left = screen_clip_left;
            blit.clip_top = screen_clip_top;
            blit.clip_right = screen_clip_right;
            blit.clip_bottom = screen_clip_bottom;
        }

        {
            do_blitPtr do_blit;
            int j;
            int[] i = new int[1];
            int[] dir = new int[1];
            int[] last = new int[1];

            switch (sprite_list.sprite_type) {
                case SPRITE_TYPE_ZOOM:
                    /*TODO*///			if (Machine->scrbitmap->depth == 16) /* 16 bit */
/*TODO*///			{
/*TODO*///				do_blit = do_blit_zoom16;
/*TODO*/////				return;
/*TODO*///			}
/*TODO*///			else
                    do_blit = do_blit_zoom;
                    break;

                case SPRITE_TYPE_STACK:
                    do_blit = do_blit_stack;
                    break;

                case SPRITE_TYPE_UNPACK:
                default:
                    do_blit = do_blit_unpack;
                    break;
            }

            sprite_order_setup(sprite_list, i, last, dir);
            for (;;) {
                sprite[] sprite = sprite_table;//&sprite_table[i];
                int s_ptr = i[0];
                if ((sprite[s_ptr].flags & SPRITE_VISIBLE) != 0 && (sprite[s_ptr].priority == priority)) {
                    do_blit.handler(sprite, s_ptr);
                }
                if (i[0] == last[0]) {
                    break;
                }
                i[0] += dir[0];
            }
        }
    }

    public static void sprite_set_shade_table(char[] table) {
        shade_table = table;
    }

}
