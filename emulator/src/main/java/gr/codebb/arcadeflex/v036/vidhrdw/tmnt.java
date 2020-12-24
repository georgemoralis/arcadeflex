package gr.codebb.arcadeflex.v036.vidhrdw;

/**
 *
 * @author shadow
 */
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;

public class tmnt {

    static int[] layer_colorbase = new int[3];
    static int sprite_colorbase, bg_colorbase;
    static int priorityflag;

    /**
     * *************************************************************************
     *
     * Callbacks for the K052109
     *
     **************************************************************************
     */

    /* Missing in Action */
    public static K052109_callbackProcPtr mia_tile_callback = new K052109_callbackProcPtr() {
        public void handler(int layer, int bank, int[] code, int[] color) {

            tile_info.flags = (color[0] & 0x04) != 0 ? (char) TILE_FLIPX : (char) 0;
            if (layer == 0) {
                code[0] |= ((color[0] & 0x01) << 8);
                color[0] = layer_colorbase[layer] + ((color[0] & 0x80) >> 5) + ((color[0] & 0x10) >> 1);
            } else {
                code[0] |= ((color[0] & 0x01) << 8) | ((color[0] & 0x18) << 6) | (bank << 11);
                color[0] = layer_colorbase[layer] + ((color[0] & 0xe0) >> 5);
            }
        }
    };
    public static K052109_callbackProcPtr tmnt_tile_callback = new K052109_callbackProcPtr() {
        public void handler(int layer, int bank, int[] code, int[] color) {
            code[0] |= ((color[0] & 0x03) << 8) | ((color[0] & 0x10) << 6) | ((color[0] & 0x0c) << 9)
                    | (bank << 13);
            color[0] = layer_colorbase[layer] + ((color[0] & 0xe0) >> 5);
        }
    };

    static int detatwin_rombank;
    public static K052109_callbackProcPtr detatwin_tile_callback = new K052109_callbackProcPtr() {
        public void handler(int layer, int bank, int[] code, int[] color) {

            /* (color & 0x02) is flip y handled internally by the 052109 */
            code[0] |= ((color[0] & 0x01) << 8) | ((color[0] & 0x10) << 5) | ((color[0] & 0x0c) << 8)
                    | (bank << 12) | detatwin_rombank << 14;
            color[0] = layer_colorbase[layer] + ((color[0] & 0xe0) >> 5);
        }
    };

    /**
     * *************************************************************************
     *
     * Callbacks for the K051960
     *
     **************************************************************************
     */
    public static K051960_callbackProcPtr mia_sprite_callback = new K051960_callbackProcPtr() {
        public void handler(int[] code, int[] color, int[] priority) {
            color[0] = sprite_colorbase + (color[0] & 0x0f);
        }
    };
    public static K051960_callbackProcPtr tmnt_sprite_callback = new K051960_callbackProcPtr() {
        public void handler(int[] code, int[] color, int[] priority) {
            code[0] |= (color[0] & 0x10) << 9;
            color[0] = sprite_colorbase + (color[0] & 0x0f);
        }
    };
    public static K051960_callbackProcPtr punkshot_sprite_callback = new K051960_callbackProcPtr() {
        public void handler(int[] code, int[] color, int[] priority) {
            code[0] |= (color[0] & 0x10) << 9;
            priority[0] = 0x20 | ((color[0] & 0x60) >> 2);
            color[0] = sprite_colorbase + (color[0] & 0x0f);
        }
    };
    public static K051960_callbackProcPtr thndrx2_sprite_callback = new K051960_callbackProcPtr() {
        public void handler(int[] code, int[] color, int[] priority) {
            priority[0] = 0x20 | ((color[0] & 0x60) >> 2);
            color[0] = sprite_colorbase + (color[0] & 0x0f);
        }
    };

    /**
     * *************************************************************************
     *
     * Callbacks for the K053245
     *
     **************************************************************************
     */
    public static K053245_callbackProcPtr lgtnfght_sprite_callback = new K053245_callbackProcPtr() {
        public void handler(int[] code, int[] color, int[] priority) {
            priority[0] = 0x20 | ((color[0] & 0x60) >> 2);
            color[0] = sprite_colorbase + (color[0] & 0x1f);
        }
    };
    public static K053245_callbackProcPtr detatwin_sprite_callback = new K053245_callbackProcPtr() {
        public void handler(int[] code, int[] color, int[] priority) {
            /*#if 0
             if (keyboard_pressed(KEYCODE_Q) && (*color & 0x20)) *color = rand();
             if (keyboard_pressed(KEYCODE_W) && (*color & 0x40)) *color = rand();
             if (keyboard_pressed(KEYCODE_E) && (*color & 0x80)) *color = rand();
             #endif*/
            priority[0] = 0x20 | ((color[0] & 0x60) >> 2);
            color[0] = sprite_colorbase + (color[0] & 0x1f);
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartPtr mia_vh_start = new VhStartPtr() {
        public int handler() {
            layer_colorbase[0] = 0;
            layer_colorbase[1] = 32;
            layer_colorbase[2] = 40;
            sprite_colorbase = 16;
            if (K052109_vh_start(REGION_GFX1, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, mia_tile_callback) != 0) {
                return 1;
            }
            if (K051960_vh_start(REGION_GFX2, 3, 2, 1, 0/*REVERSE_PLANE_ORDER*/, mia_sprite_callback) != 0) {
                K052109_vh_stop();
                return 1;
            }
            return 0;
        }
    };
    public static VhStartPtr tmnt_vh_start = new VhStartPtr() {
        public int handler() {
            layer_colorbase[0] = 0;
            layer_colorbase[1] = 32;
            layer_colorbase[2] = 40;
            sprite_colorbase = 16;
            if (K052109_vh_start(REGION_GFX1, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, tmnt_tile_callback) != 0) {
                return 1;
            }
            if (K051960_vh_start(REGION_GFX2, 3, 2, 1, 0/*REVERSE_PLANE_ORDER*/, tmnt_sprite_callback) != 0) {
                K052109_vh_stop();
                return 1;
            }
            return 0;
        }
    };
    public static VhStartPtr punkshot_vh_start = new VhStartPtr() {
        public int handler() {
            if (K052109_vh_start(REGION_GFX1, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, tmnt_tile_callback) != 0) {
                return 1;
            }
            if (K051960_vh_start(REGION_GFX2, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, punkshot_sprite_callback) != 0) {
                K052109_vh_stop();
                return 1;
            }
            return 0;
        }
    };
    public static VhStartPtr lgtnfght_vh_start = new VhStartPtr() {
        public int handler() /* also tmnt2, ssriders */ {
            if (K052109_vh_start(REGION_GFX1, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, tmnt_tile_callback) != 0) {
                return 1;
            }
            if (K053245_vh_start(REGION_GFX2, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, lgtnfght_sprite_callback) != 0) {
                K052109_vh_stop();
                return 1;
            }
            return 0;
        }
    };
    public static VhStartPtr detatwin_vh_start = new VhStartPtr() {
        public int handler() {
            if (K052109_vh_start(REGION_GFX1, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, detatwin_tile_callback) != 0) {
                return 1;
            }
            if (K053245_vh_start(REGION_GFX2, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, detatwin_sprite_callback) != 0) {
                K052109_vh_stop();
                return 1;
            }
            return 0;
        }
    };

    public static VhStartPtr glfgreat_vh_start = new VhStartPtr() {
        public int handler() {
            if (K052109_vh_start(REGION_GFX1, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, tmnt_tile_callback) != 0) {
                return 1;
            }
            if (K053245_vh_start(REGION_GFX2, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, lgtnfght_sprite_callback) != 0) {
                K052109_vh_stop();
                return 1;
            }
            return 0;
        }
    };
    public static VhStartPtr thndrx2_vh_start = new VhStartPtr() {
        public int handler() {
            if (K052109_vh_start(REGION_GFX1, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, tmnt_tile_callback) != 0) {
                return 1;
            }
            if (K051960_vh_start(REGION_GFX2, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, thndrx2_sprite_callback) != 0) {
                K052109_vh_stop();
                return 1;
            }
            return 0;
        }
    };
    public static VhStopPtr punkshot_vh_stop = new VhStopPtr() {
        public void handler() {
            K052109_vh_stop();
            K051960_vh_stop();
        }
    };
    public static VhStopPtr lgtnfght_vh_stop = new VhStopPtr() {
        public void handler() {
            K052109_vh_stop();
            K053245_vh_stop();
        }
    };
    public static VhStopPtr detatwin_vh_stop = new VhStopPtr() {
        public void handler() {
            K052109_vh_stop();
            K053245_vh_stop();
        }
    };

    public static VhStopPtr glfgreat_vh_stop = new VhStopPtr() {
        public void handler() {
            K052109_vh_stop();
            K053245_vh_stop();
        }
    };

    public static VhStopPtr thndrx2_vh_stop = new VhStopPtr() {
        public void handler() {
            K052109_vh_stop();
            K051960_vh_stop();
        }
    };
    /**
     * *************************************************************************
     *
     * Memory handlers
     *
     **************************************************************************
     */

    public static WriteHandlerPtr tmnt_paletteram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int oldword = paletteram.READ_WORD(offset);
            int newword = COMBINE_WORD(oldword, data);
            paletteram.WRITE_WORD(offset, newword);

            offset /= 4;
            {
                int palette = ((paletteram.READ_WORD(offset * 4) & 0x00ff) << 8)
                        + (paletteram.READ_WORD(offset * 4 + 2) & 0x00ff);
                int r = palette & 31;
                int g = (palette >> 5) & 31;
                int b = (palette >> 10) & 31;

                r = (r << 3) + (r >> 2);
                g = (g << 3) + (g >> 2);
                b = (b << 3) + (b >> 2);

                palette_change_color(offset, r, g, b);
            }
        }
    };

    static int last_tmnt;
    public static WriteHandlerPtr tmnt_0a0000_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 0x00ff0000) == 0) {

                /* bit 0/1 = coin counters */
                coin_counter_w.handler(0, data & 0x01);
                coin_counter_w.handler(1, data & 0x02);	/* 2 players version */

                /* bit 3 high then low triggers irq on sound CPU */
                if (last_tmnt == 0x08 && (data & 0x08) == 0) {
                    cpu_cause_interrupt(1, 0xff);
                }

                last_tmnt = data & 0x08;

                /* bit 5 = irq enable */
                interrupt_enable_w.handler(0, data & 0x20);

                /* bit 7 = enable char ROM reading through the video RAM */
                K052109_set_RMRD_line((data & 0x80) != 0 ? ASSERT_LINE : CLEAR_LINE);

                /* other bits unused */
            }
        }
    };
    static int last_p;
    public static WriteHandlerPtr punkshot_0a0020_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 0x00ff0000) == 0) {
                /* bit 0 = coin counter */
                coin_counter_w.handler(0, data & 0x01);

                /* bit 2 = trigger irq on sound CPU */
                if (last_p == 0x04 && (data & 0x04) == 0) {
                    cpu_cause_interrupt(1, 0xff);
                }

                last_p = data & 0x04;

                /* bit 3 = enable char ROM reading through the video RAM */
                K052109_set_RMRD_line((data & 0x08) != 0 ? ASSERT_LINE : CLEAR_LINE);
            }
        }
    };
    static int last_lgt;
    public static WriteHandlerPtr lgtnfght_0a0018_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 0x00ff0000) == 0) {

                /* bit 0,1 = coin counter */
                coin_counter_w.handler(0, data & 0x01);
                coin_counter_w.handler(1, data & 0x02);

                /* bit 2 = trigger irq on sound CPU */
                if (last_lgt == 0x00 && (data & 0x04) == 0x04) {
                    cpu_cause_interrupt(1, 0xff);
                }

                last_lgt = data & 0x04;

                /* bit 3 = enable char ROM reading through the video RAM */
                K052109_set_RMRD_line((data & 0x08) != 0 ? ASSERT_LINE : CLEAR_LINE);
            }
        }
    };
    public static WriteHandlerPtr detatwin_700300_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 0x00ff0000) == 0) {
                /* bit 0,1 = coin counter */
                coin_counter_w.handler(0, data & 0x01);
                coin_counter_w.handler(1, data & 0x02);

                /* bit 3 = enable char ROM reading through the video RAM */
                K052109_set_RMRD_line((data & 0x08) != 0 ? ASSERT_LINE : CLEAR_LINE);

                /* bit 7 = select char ROM bank */
                if (detatwin_rombank != ((data & 0x80) >> 7)) {
                    detatwin_rombank = (data & 0x80) >> 7;
                    tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
                }

                /* other bits unknown */
            }
        }
    };

    public static WriteHandlerPtr glfgreat_122000_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 0x00ff0000) == 0) {
                /* bit 0,1 = coin counter */
                coin_counter_w.handler(0, data & 0x01);
                coin_counter_w.handler(1, data & 0x02);

                /* bit 4 = enable char ROM reading through the video RAM */
                K052109_set_RMRD_line((data & 0x10) != 0 ? ASSERT_LINE : CLEAR_LINE);

                /* other bits unknown */
            }
        }
    };

    public static WriteHandlerPtr ssriders_1c0300_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 0x00ff0000) == 0) {
                /* bit 0,1 = coin counter */
                coin_counter_w.handler(0, data & 0x01);
                coin_counter_w.handler(1, data & 0x02);

                /* bit 3 = enable char ROM reading through the video RAM */
                K052109_set_RMRD_line((data & 0x08) != 0 ? ASSERT_LINE : CLEAR_LINE);

                /* other bits unknown (bits 4-6 used in TMNT2) */
            }
        }
    };
    public static WriteHandlerPtr tmnt_priority_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 0x00ff0000) == 0) {
                /* bit 2/3 = priority; other bits unused */
                /* bit2 = PRI bit3 = PRI2
                 sprite/playfield priority is controlled by these two bits, by bit 3
                 of the background tile color code, and by the SHADOW sprite
                 attribute bit.
                 Priorities are encoded in a PROM (G19 for TMNT). However, in TMNT,
                 the PROM only takes into account the PRI and SHADOW bits.
                 PRI  Priority
                 0   bg fg spr text
                 1   bg spr fg text
                 The SHADOW bit, when set, torns a sprite into a shadow which makes
                 color below it darker (this is done by turning off three resistors
                 in parallel with the RGB output).
	
                 Note: the background color (color used when all of the four layers
                 are 0) is taken from the *foreground* palette, not the background
                 one as would be more intuitive.
                 */
                priorityflag = (data & 0x0c) >> 2;
            }
        }
    };

    /**
     * *************************************************************************
     *
     * Display refresh
     *
     **************************************************************************
     */
    public static VhUpdatePtr mia_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            K052109_tilemap_update();

            palette_init_used_colors();
            K051960_mark_sprites_colors();
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            K052109_tilemap_draw(bitmap, 2, TILEMAP_IGNORE_TRANSPARENCY);
            if ((priorityflag & 1) == 1) {
                K051960_sprites_draw(bitmap, 0, 0);
            }
            K052109_tilemap_draw(bitmap, 1, 0);
            if ((priorityflag & 1) == 0) {
                K051960_sprites_draw(bitmap, 0, 0);
            }
            K052109_tilemap_draw(bitmap, 0, 0);
        }
    };
    public static VhUpdatePtr tmnt_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            K052109_tilemap_update();

            palette_init_used_colors();
            K051960_mark_sprites_colors();
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            K052109_tilemap_draw(bitmap, 2, TILEMAP_IGNORE_TRANSPARENCY);
            if ((priorityflag & 1) == 1) {
                K051960_sprites_draw(bitmap, 0, 0);
            }
            K052109_tilemap_draw(bitmap, 1, 0);
            if ((priorityflag & 1) == 0) {
                K051960_sprites_draw(bitmap, 0, 0);
            }
            K052109_tilemap_draw(bitmap, 0, 0);
        }
    };
    public static VhUpdatePtr punkshot_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int[] pri = new int[3];
            int[] layer = new int[3];

            bg_colorbase = K053251_get_palette_index(K053251_CI0);
            sprite_colorbase = K053251_get_palette_index(K053251_CI1);
            layer_colorbase[0] = K053251_get_palette_index(K053251_CI2);
            layer_colorbase[1] = K053251_get_palette_index(K053251_CI4);
            layer_colorbase[2] = K053251_get_palette_index(K053251_CI3);

            K052109_tilemap_update();

            palette_init_used_colors();
            K051960_mark_sprites_colors();
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            layer[0] = 0;
            pri[0] = K053251_get_priority(K053251_CI2);
            layer[1] = 1;
            pri[1] = K053251_get_priority(K053251_CI4);
            layer[2] = 2;
            pri[2] = K053251_get_priority(K053251_CI3);

            if (pri[0] < pri[1]) {
                int t;
                t = pri[0];
                pri[0] = pri[1];
                pri[1] = t;
                t = layer[0];
                layer[0] = layer[1];
                layer[1] = t;
            }
            if (pri[0] < pri[2]) {
                int t;
                t = pri[0];
                pri[0] = pri[2];
                pri[2] = t;
                t = layer[0];
                layer[0] = layer[2];
                layer[2] = t;
            }
            if (pri[1] < pri[2]) {
                int t;
                t = pri[1];
                pri[1] = pri[2];
                pri[2] = t;
                t = layer[1];
                layer[1] = layer[2];
                layer[2] = t;
            }

            K052109_tilemap_draw(bitmap, layer[0], TILEMAP_IGNORE_TRANSPARENCY);
            K051960_sprites_draw(bitmap, pri[1] + 1, pri[0]);
            K052109_tilemap_draw(bitmap, layer[1], 0);
            K051960_sprites_draw(bitmap, pri[2] + 1, pri[1]);
            K052109_tilemap_draw(bitmap, layer[2], 0);
            K051960_sprites_draw(bitmap, 0, pri[2]);
        }
    };
    public static VhUpdatePtr lgtnfght_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int[] pri = new int[3];
            int[] layer = new int[3];

            bg_colorbase = K053251_get_palette_index(K053251_CI0);
            sprite_colorbase = K053251_get_palette_index(K053251_CI1);
            layer_colorbase[0] = K053251_get_palette_index(K053251_CI2);
            layer_colorbase[1] = K053251_get_palette_index(K053251_CI4);
            layer_colorbase[2] = K053251_get_palette_index(K053251_CI3);

            K052109_tilemap_update();

            palette_init_used_colors();
            K053245_mark_sprites_colors();
            palette_used_colors.write(16 * bg_colorbase, palette_used_colors.read(16 * bg_colorbase) | PALETTE_COLOR_VISIBLE);
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            layer[0] = 0;
            pri[0] = K053251_get_priority(K053251_CI2);
            layer[1] = 1;
            pri[1] = K053251_get_priority(K053251_CI4);
            layer[2] = 2;
            pri[2] = K053251_get_priority(K053251_CI3);

            //sortlayers(layer,pri);
            if (pri[0] < pri[1]) {
                int t;
                t = pri[0];
                pri[0] = pri[1];
                pri[1] = t;
                t = layer[0];
                layer[0] = layer[1];
                layer[1] = t;
            }
            if (pri[0] < pri[2]) {
                int t;
                t = pri[0];
                pri[0] = pri[2];
                pri[2] = t;
                t = layer[0];
                layer[0] = layer[2];
                layer[2] = t;
            }
            if (pri[1] < pri[2]) {
                int t;
                t = pri[1];
                pri[1] = pri[2];
                pri[2] = t;
                t = layer[1];
                layer[1] = layer[2];
                layer[2] = t;
            }

            fillbitmap(bitmap, Machine.pens[16 * bg_colorbase], Machine.drv.visible_area);
            K053245_sprites_draw(bitmap, pri[0] + 1, 0x3f);
            K052109_tilemap_draw(bitmap, layer[0], 0);
            K053245_sprites_draw(bitmap, pri[1] + 1, pri[0]);
            K052109_tilemap_draw(bitmap, layer[1], 0);
            K053245_sprites_draw(bitmap, pri[2] + 1, pri[1]);
            K052109_tilemap_draw(bitmap, layer[2], 0);
            K053245_sprites_draw(bitmap, 0, pri[2]);
        }
    };
    public static VhUpdatePtr glfgreat_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int[] pri = new int[3];
            int[] layer = new int[3];

            bg_colorbase = K053251_get_palette_index(K053251_CI0);
            sprite_colorbase = K053251_get_palette_index(K053251_CI1);
            layer_colorbase[0] = K053251_get_palette_index(K053251_CI2);
            layer_colorbase[1] = K053251_get_palette_index(K053251_CI3) + 8;	/* weird... */

            layer_colorbase[2] = K053251_get_palette_index(K053251_CI4);

            K052109_tilemap_update();

            palette_init_used_colors();
            K053245_mark_sprites_colors();
            palette_used_colors.write(16 * bg_colorbase, palette_used_colors.read(16 * bg_colorbase) | PALETTE_COLOR_VISIBLE);
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            layer[0] = 0;
            pri[0] = K053251_get_priority(K053251_CI2);
            layer[1] = 1;
            pri[1] = K053251_get_priority(K053251_CI3);
            layer[2] = 2;
            pri[2] = K053251_get_priority(K053251_CI4);

            //sortlayers(layer,pri);
            if (pri[0] < pri[1]) {
                int t;
                t = pri[0];
                pri[0] = pri[1];
                pri[1] = t;
                t = layer[0];
                layer[0] = layer[1];
                layer[1] = t;
            }
            if (pri[0] < pri[2]) {
                int t;
                t = pri[0];
                pri[0] = pri[2];
                pri[2] = t;
                t = layer[0];
                layer[0] = layer[2];
                layer[2] = t;
            }
            if (pri[1] < pri[2]) {
                int t;
                t = pri[1];
                pri[1] = pri[2];
                pri[2] = t;
                t = layer[1];
                layer[1] = layer[2];
                layer[2] = t;
            }

            fillbitmap(bitmap, Machine.pens[16 * bg_colorbase], Machine.drv.visible_area);
            K053245_sprites_draw(bitmap, pri[0] + 1, 0x3f);
            K052109_tilemap_draw(bitmap, layer[0], 0);
            K053245_sprites_draw(bitmap, pri[1] + 1, pri[0]);
            K052109_tilemap_draw(bitmap, layer[1], 0);
            K053245_sprites_draw(bitmap, pri[2] + 1, pri[1]);
            K052109_tilemap_draw(bitmap, layer[2], 0);
            K053245_sprites_draw(bitmap, 0, pri[2]);
        }
    };
    public static VhUpdatePtr ssriders_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int i;

            for (i = 0; i < 128; i++) {
                if ((K053245_word_r.handler(16 * i) & 0x8000) != 0 && (K053245_word_r.handler(16 * i + 2) & 0x8000) == 0) {
                    K053245_word_w.handler(16 * i, 0xff000000 | i);	/* workaround for protection */

                }
            }

            lgtnfght_vh_screenrefresh.handler(bitmap, full_refresh);
        }
    };
    public static VhUpdatePtr thndrx2_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int[] pri = new int[3];
            int[] layer = new int[3];

            bg_colorbase = K053251_get_palette_index(K053251_CI0);
            sprite_colorbase = K053251_get_palette_index(K053251_CI1);
            layer_colorbase[0] = K053251_get_palette_index(K053251_CI2);
            layer_colorbase[1] = K053251_get_palette_index(K053251_CI4);
            layer_colorbase[2] = K053251_get_palette_index(K053251_CI3);

            K052109_tilemap_update();

            palette_init_used_colors();
            K051960_mark_sprites_colors();
            palette_used_colors.write(16 * bg_colorbase, palette_used_colors.read(16 * bg_colorbase) | PALETTE_COLOR_VISIBLE);
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            layer[0] = 0;
            pri[0] = K053251_get_priority(K053251_CI2);
            layer[1] = 1;
            pri[1] = K053251_get_priority(K053251_CI4);
            layer[2] = 2;
            pri[2] = K053251_get_priority(K053251_CI3);

            //sortlayers(layer,pri);
            if (pri[0] < pri[1]) {
                int t;
                t = pri[0];
                pri[0] = pri[1];
                pri[1] = t;
                t = layer[0];
                layer[0] = layer[1];
                layer[1] = t;
            }
            if (pri[0] < pri[2]) {
                int t;
                t = pri[0];
                pri[0] = pri[2];
                pri[2] = t;
                t = layer[0];
                layer[0] = layer[2];
                layer[2] = t;
            }
            if (pri[1] < pri[2]) {
                int t;
                t = pri[1];
                pri[1] = pri[2];
                pri[2] = t;
                t = layer[1];
                layer[1] = layer[2];
                layer[2] = t;
            }

            fillbitmap(bitmap, Machine.pens[16 * bg_colorbase], Machine.drv.visible_area);
            K051960_sprites_draw(bitmap, pri[0] + 1, 0x3f);
            K052109_tilemap_draw(bitmap, layer[0], 0);
            K051960_sprites_draw(bitmap, pri[1] + 1, pri[0]);
            K052109_tilemap_draw(bitmap, layer[1], 0);
            K051960_sprites_draw(bitmap, pri[2] + 1, pri[1]);
            K052109_tilemap_draw(bitmap, layer[2], 0);
            K051960_sprites_draw(bitmap, 0, pri[2]);
        }
    };
}
