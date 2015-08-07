package vidhrdw;

/**
 *
 * @author shadow
 */
import static arcadeflex.libc.*;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static vidhrdw.generic.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.mame.*;
import static arcadeflex.ptrlib.*;
import static mame.commonH.*;
import static vidhrdw.konamiic.*;
import static vidhrdw.konami.K053247.*;
import static mame.memory.*;
import static mame.palette.*;
import static mame.paletteH.*;
import static mame.tilemapC.*;
import static mame.tilemapH.*;
import static mame.common.*;
import static mame.cpuintrfH.*;
import static mame.cpuintrf.*;
import static mame.memoryH.*;

public class tmnt {

    static int[] layer_colorbase = new int[3];
    static int sprite_colorbase, bg_colorbase;
    static int priorityflag;
    /*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Callbacks for the K052109
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*////* Missing in Action */
/*TODO*///
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
    /*TODO*///
/*TODO*///static int detatwin_rombank;
/*TODO*///
/*TODO*///static void detatwin_tile_callback(int layer,int bank,int *code,int *color)
/*TODO*///{
/*TODO*///	/* (color & 0x02) is flip y handled internally by the 052109 */
/*TODO*///	*code |= ((*color & 0x01) << 8) | ((*color & 0x10) << 5) | ((*color & 0x0c) << 8)
/*TODO*///			| (bank << 12) | detatwin_rombank << 14;
/*TODO*///	*color = layer_colorbase[layer] + ((*color & 0xe0) >> 5);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Callbacks for the K051960
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
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
    /*TODO*///
/*TODO*///static void punkshot_sprite_callback(int *code,int *color,int *priority)
/*TODO*///{
/*TODO*///	*code |= (*color & 0x10) << 9;
/*TODO*///	*priority = 0x20 | ((*color & 0x60) >> 2);
/*TODO*///	*color = sprite_colorbase + (*color & 0x0f);
/*TODO*///}
/*TODO*///
/*TODO*///static void thndrx2_sprite_callback(int *code,int *color,int *priority)
/*TODO*///{
/*TODO*///	*priority = 0x20 | ((*color & 0x60) >> 2);
/*TODO*///	*color = sprite_colorbase + (*color & 0x0f);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Callbacks for the K053245
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
    public static K053245_callbackProcPtr lgtnfght_sprite_callback = new K053245_callbackProcPtr() {
        public void handler(int[] code, int[] color, int[] priority) {
            priority[0] = 0x20 | ((color[0] & 0x60) >> 2);
            color[0] = sprite_colorbase + (color[0] & 0x1f);
        }
    };
    /*TODO*///
/*TODO*///static void detatwin_sprite_callback(int *code,int *color,int *priority)
/*TODO*///{
/*TODO*///#if 0
/*TODO*///if (keyboard_pressed(KEYCODE_Q) && (*color & 0x20)) *color = rand();
/*TODO*///if (keyboard_pressed(KEYCODE_W) && (*color & 0x40)) *color = rand();
/*TODO*///if (keyboard_pressed(KEYCODE_E) && (*color & 0x80)) *color = rand();
/*TODO*///#endif
/*TODO*///	*priority = 0x20 | ((*color & 0x60) >> 2);
/*TODO*///	*color = sprite_colorbase + (*color & 0x1f);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Start the video hardware emulation.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
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
    /*TODO*///
/*TODO*///int tmnt_vh_start(void)
/*TODO*///{
/*TODO*///	layer_colorbase[0] = 0;
/*TODO*///	layer_colorbase[1] = 32;
/*TODO*///	layer_colorbase[2] = 40;
/*TODO*///	sprite_colorbase = 16;
/*TODO*///	if (K052109_vh_start(REGION_GFX1,NORMAL_PLANE_ORDER,tmnt_tile_callback))
/*TODO*///		return 1;
/*TODO*///	if (K051960_vh_start(REGION_GFX2,REVERSE_PLANE_ORDER,tmnt_sprite_callback))
/*TODO*///	{
/*TODO*///		K052109_vh_stop();
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///int punkshot_vh_start(void)
/*TODO*///{
/*TODO*///	if (K052109_vh_start(REGION_GFX1,NORMAL_PLANE_ORDER,tmnt_tile_callback))
/*TODO*///		return 1;
/*TODO*///	if (K051960_vh_start(REGION_GFX2,NORMAL_PLANE_ORDER,punkshot_sprite_callback))
/*TODO*///	{
/*TODO*///		K052109_vh_stop();
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
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
    /*TODO*///
/*TODO*///int detatwin_vh_start(void)
/*TODO*///{
/*TODO*///	if (K052109_vh_start(REGION_GFX1,NORMAL_PLANE_ORDER,detatwin_tile_callback))
/*TODO*///		return 1;
/*TODO*///	if (K053245_vh_start(REGION_GFX2,NORMAL_PLANE_ORDER,detatwin_sprite_callback))
/*TODO*///	{
/*TODO*///		K052109_vh_stop();
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///int glfgreat_vh_start(void)
/*TODO*///{
/*TODO*///	if (K052109_vh_start(REGION_GFX1,NORMAL_PLANE_ORDER,tmnt_tile_callback))
/*TODO*///		return 1;
/*TODO*///	if (K053245_vh_start(REGION_GFX2,NORMAL_PLANE_ORDER,lgtnfght_sprite_callback))
/*TODO*///	{
/*TODO*///		K052109_vh_stop();
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///int thndrx2_vh_start(void)
/*TODO*///{
/*TODO*///	if (K052109_vh_start(REGION_GFX1,NORMAL_PLANE_ORDER,tmnt_tile_callback))
/*TODO*///		return 1;
/*TODO*///	if (K051960_vh_start(REGION_GFX2,NORMAL_PLANE_ORDER,thndrx2_sprite_callback))
/*TODO*///	{
/*TODO*///		K052109_vh_stop();
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
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
    /*TODO*///
/*TODO*///void detatwin_vh_stop(void)
/*TODO*///{
/*TODO*///	K052109_vh_stop();
/*TODO*///	K053245_vh_stop();
/*TODO*///}
/*TODO*///
/*TODO*///void glfgreat_vh_stop(void)
/*TODO*///{
/*TODO*///	K052109_vh_stop();
/*TODO*///	K053245_vh_stop();
/*TODO*///}
/*TODO*///
/*TODO*///void thndrx2_vh_stop(void)
/*TODO*///{
/*TODO*///	K052109_vh_stop();
/*TODO*///	K051960_vh_stop();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Memory handlers
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
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
    /*TODO*///
/*TODO*///void punkshot_0a0020_w(int offset,int data)
/*TODO*///{
/*TODO*///	if ((data & 0x00ff0000) == 0)
/*TODO*///	{
/*TODO*///		static int last;
/*TODO*///
/*TODO*///
/*TODO*///		/* bit 0 = coin counter */
/*TODO*///		coin_counter_w(0,data & 0x01);
/*TODO*///
/*TODO*///		/* bit 2 = trigger irq on sound CPU */
/*TODO*///		if (last == 0x04 && (data & 0x04) == 0)
/*TODO*///			cpu_cause_interrupt(1,0xff);
/*TODO*///
/*TODO*///		last = data & 0x04;
/*TODO*///
/*TODO*///		/* bit 3 = enable char ROM reading through the video RAM */
/*TODO*///		K052109_set_RMRD_line((data & 0x08) ? ASSERT_LINE : CLEAR_LINE);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void lgtnfght_0a0018_w(int offset,int data)
/*TODO*///{
/*TODO*///	if ((data & 0x00ff0000) == 0)
/*TODO*///	{
/*TODO*///		static int last;
/*TODO*///
/*TODO*///
/*TODO*///		/* bit 0,1 = coin counter */
/*TODO*///		coin_counter_w(0,data & 0x01);
/*TODO*///		coin_counter_w(1,data & 0x02);
/*TODO*///
/*TODO*///		/* bit 2 = trigger irq on sound CPU */
/*TODO*///		if (last == 0x00 && (data & 0x04) == 0x04)
/*TODO*///			cpu_cause_interrupt(1,0xff);
/*TODO*///
/*TODO*///		last = data & 0x04;
/*TODO*///
/*TODO*///		/* bit 3 = enable char ROM reading through the video RAM */
/*TODO*///		K052109_set_RMRD_line((data & 0x08) ? ASSERT_LINE : CLEAR_LINE);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void detatwin_700300_w(int offset,int data)
/*TODO*///{
/*TODO*///	if ((data & 0x00ff0000) == 0)
/*TODO*///	{
/*TODO*///		/* bit 0,1 = coin counter */
/*TODO*///		coin_counter_w(0,data & 0x01);
/*TODO*///		coin_counter_w(1,data & 0x02);
/*TODO*///
/*TODO*///		/* bit 3 = enable char ROM reading through the video RAM */
/*TODO*///		K052109_set_RMRD_line((data & 0x08) ? ASSERT_LINE : CLEAR_LINE);
/*TODO*///
/*TODO*///		/* bit 7 = select char ROM bank */
/*TODO*///		if (detatwin_rombank != ((data & 0x80) >> 7))
/*TODO*///		{
/*TODO*///			detatwin_rombank = (data & 0x80) >> 7;
/*TODO*///			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
/*TODO*///		}
/*TODO*///
/*TODO*///		/* other bits unknown */
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void glfgreat_122000_w(int offset,int data)
/*TODO*///{
/*TODO*///	if ((data & 0x00ff0000) == 0)
/*TODO*///	{
/*TODO*///		/* bit 0,1 = coin counter */
/*TODO*///		coin_counter_w(0,data & 0x01);
/*TODO*///		coin_counter_w(1,data & 0x02);
/*TODO*///
/*TODO*///		/* bit 4 = enable char ROM reading through the video RAM */
/*TODO*///		K052109_set_RMRD_line((data & 0x10) ? ASSERT_LINE : CLEAR_LINE);
/*TODO*///
/*TODO*///		/* other bits unknown */
/*TODO*///	}
/*TODO*///}
/*TODO*///
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
    /*TODO*///
/*TODO*///
/*TODO*///
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
    /*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Display refresh
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*////* useful function to sort the three tile layers by priority order */
/*TODO*///static void sortlayers(int *layer,int *pri)
/*TODO*///{
/*TODO*///#define SWAP(a,b) \
/*TODO*///	if (pri[a] < pri[b]) \
/*TODO*///	{ \
/*TODO*///		int t; \
/*TODO*///		t = pri[a]; pri[a] = pri[b]; pri[b] = t; \
/*TODO*///		t = layer[a]; layer[a] = layer[b]; layer[b] = t; \
/*TODO*///	}
/*TODO*///
/*TODO*///	SWAP(0,1)
/*TODO*///	SWAP(0,2)
/*TODO*///	SWAP(1,2)
/*TODO*///}
/*TODO*///
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
    /*TODO*///
/*TODO*///void punkshot_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
/*TODO*///{
/*TODO*///	int pri[3],layer[3];
/*TODO*///
/*TODO*///
/*TODO*///	bg_colorbase       = K053251_get_palette_index(K053251_CI0);
/*TODO*///	sprite_colorbase   = K053251_get_palette_index(K053251_CI1);
/*TODO*///	layer_colorbase[0] = K053251_get_palette_index(K053251_CI2);
/*TODO*///	layer_colorbase[1] = K053251_get_palette_index(K053251_CI4);
/*TODO*///	layer_colorbase[2] = K053251_get_palette_index(K053251_CI3);
/*TODO*///
/*TODO*///	K052109_tilemap_update();
/*TODO*///
/*TODO*///	palette_init_used_colors();
/*TODO*///	K051960_mark_sprites_colors();
/*TODO*///	if (palette_recalc())
/*TODO*///		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
/*TODO*///
/*TODO*///	tilemap_render(ALL_TILEMAPS);
/*TODO*///
/*TODO*///	layer[0] = 0;
/*TODO*///	pri[0] = K053251_get_priority(K053251_CI2);
/*TODO*///	layer[1] = 1;
/*TODO*///	pri[1] = K053251_get_priority(K053251_CI4);
/*TODO*///	layer[2] = 2;
/*TODO*///	pri[2] = K053251_get_priority(K053251_CI3);
/*TODO*///
/*TODO*///	sortlayers(layer,pri);
/*TODO*///
/*TODO*///	K052109_tilemap_draw(bitmap,layer[0],TILEMAP_IGNORE_TRANSPARENCY);
/*TODO*///	K051960_sprites_draw(bitmap,pri[1]+1,pri[0]);
/*TODO*///	K052109_tilemap_draw(bitmap,layer[1],0);
/*TODO*///	K051960_sprites_draw(bitmap,pri[2]+1,pri[1]);
/*TODO*///	K052109_tilemap_draw(bitmap,layer[2],0);
/*TODO*///	K051960_sprites_draw(bitmap,0,pri[2]);
/*TODO*///}
/*TODO*///
/*TODO*///
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
    /*TODO*///
/*TODO*///void glfgreat_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
/*TODO*///{
/*TODO*///	int pri[3],layer[3];
/*TODO*///
/*TODO*///
/*TODO*///	bg_colorbase       = K053251_get_palette_index(K053251_CI0);
/*TODO*///	sprite_colorbase   = K053251_get_palette_index(K053251_CI1);
/*TODO*///	layer_colorbase[0] = K053251_get_palette_index(K053251_CI2);
/*TODO*///	layer_colorbase[1] = K053251_get_palette_index(K053251_CI3) + 8;	/* weird... */
/*TODO*///	layer_colorbase[2] = K053251_get_palette_index(K053251_CI4);
/*TODO*///
/*TODO*///	K052109_tilemap_update();
/*TODO*///
/*TODO*///	palette_init_used_colors();
/*TODO*///	K053245_mark_sprites_colors();
/*TODO*///	palette_used_colors[16 * bg_colorbase] |= PALETTE_COLOR_VISIBLE;
/*TODO*///	if (palette_recalc())
/*TODO*///		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
/*TODO*///
/*TODO*///	tilemap_render(ALL_TILEMAPS);
/*TODO*///
/*TODO*///	layer[0] = 0;
/*TODO*///	pri[0] = K053251_get_priority(K053251_CI2);
/*TODO*///	layer[1] = 1;
/*TODO*///	pri[1] = K053251_get_priority(K053251_CI3);
/*TODO*///	layer[2] = 2;
/*TODO*///	pri[2] = K053251_get_priority(K053251_CI4);
/*TODO*///
/*TODO*///	sortlayers(layer,pri);
/*TODO*///
/*TODO*///	fillbitmap(bitmap,Machine->pens[16 * bg_colorbase],&Machine->drv->visible_area);
/*TODO*///	K053245_sprites_draw(bitmap,pri[0]+1,0x3f);
/*TODO*///	K052109_tilemap_draw(bitmap,layer[0],0);
/*TODO*///	K053245_sprites_draw(bitmap,pri[1]+1,pri[0]);
/*TODO*///	K052109_tilemap_draw(bitmap,layer[1],0);
/*TODO*///	K053245_sprites_draw(bitmap,pri[2]+1,pri[1]);
/*TODO*///	K052109_tilemap_draw(bitmap,layer[2],0);
/*TODO*///	K053245_sprites_draw(bitmap,0,pri[2]);
/*TODO*///}
/*TODO*///
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
    /*TODO*///
/*TODO*///
/*TODO*///void thndrx2_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
/*TODO*///{
/*TODO*///	int pri[3],layer[3];
/*TODO*///
/*TODO*///
/*TODO*///	bg_colorbase       = K053251_get_palette_index(K053251_CI0);
/*TODO*///	sprite_colorbase   = K053251_get_palette_index(K053251_CI1);
/*TODO*///	layer_colorbase[0] = K053251_get_palette_index(K053251_CI2);
/*TODO*///	layer_colorbase[1] = K053251_get_palette_index(K053251_CI4);
/*TODO*///	layer_colorbase[2] = K053251_get_palette_index(K053251_CI3);
/*TODO*///
/*TODO*///	K052109_tilemap_update();
/*TODO*///
/*TODO*///	palette_init_used_colors();
/*TODO*///	K051960_mark_sprites_colors();
/*TODO*///	palette_used_colors[16 * bg_colorbase] |= PALETTE_COLOR_VISIBLE;
/*TODO*///	if (palette_recalc())
/*TODO*///		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
/*TODO*///
/*TODO*///	tilemap_render(ALL_TILEMAPS);
/*TODO*///
/*TODO*///	layer[0] = 0;
/*TODO*///	pri[0] = K053251_get_priority(K053251_CI2);
/*TODO*///	layer[1] = 1;
/*TODO*///	pri[1] = K053251_get_priority(K053251_CI4);
/*TODO*///	layer[2] = 2;
/*TODO*///	pri[2] = K053251_get_priority(K053251_CI3);
/*TODO*///
/*TODO*///	sortlayers(layer,pri);
/*TODO*///
/*TODO*///	fillbitmap(bitmap,Machine->pens[16 * bg_colorbase],&Machine->drv->visible_area);
/*TODO*///	K051960_sprites_draw(bitmap,pri[0]+1,0x3f);
/*TODO*///	K052109_tilemap_draw(bitmap,layer[0],0);
/*TODO*///	K051960_sprites_draw(bitmap,pri[1]+1,pri[0]);
/*TODO*///	K052109_tilemap_draw(bitmap,layer[1],0);
/*TODO*///	K051960_sprites_draw(bitmap,pri[2]+1,pri[1]);
/*TODO*///	K052109_tilemap_draw(bitmap,layer[2],0);
/*TODO*///	K051960_sprites_draw(bitmap,0,pri[2]);
/*TODO*///}
/*TODO*///    
}
