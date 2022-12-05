/*
 * ported to 0.36
 *
 */
package arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.common.libc.cstring.memset;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.*;

public class wiz {

    static rectangle spritevisiblearea = new rectangle(
            2 * 8, 32 * 8 - 1,
            2 * 8, 30 * 8 - 1
    );

    static rectangle spritevisibleareaflipx = new rectangle(
            0 * 8, 30 * 8 - 1,
            2 * 8, 30 * 8 - 1
    );

    public static UBytePtr wiz_videoram2 = new UBytePtr();
    public static UBytePtr wiz_colorram2 = new UBytePtr();
    public static UBytePtr wiz_attributesram = new UBytePtr();
    public static UBytePtr wiz_attributesram2 = new UBytePtr();

    static int flipx, flipy;

    public static UBytePtr wiz_sprite_bank = new UBytePtr();
    static char[] char_bank = new char[2];
    static char[] palbank = new char[2];
    static int palette_bank;

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Stinger has three 256x4 palette PROMs (one per gun). The palette PROMs
     * are connected to the RGB output this way:
     *
     * bit 3 -- 100 ohm resistor -- RED/GREEN/BLUE -- 220 ohm resistor --
     * RED/GREEN/BLUE -- 470 ohm resistor -- RED/GREEN/BLUE bit 0 -- 1 kohm
     * resistor -- RED/GREEN/BLUE
     *
     **************************************************************************
     */
    public static VhConvertColorPromPtr wiz_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2, bit3;

                bit0 = (color_prom.read(0) >> 0) & 0x01;
                bit1 = (color_prom.read(0) >> 1) & 0x01;
                bit2 = (color_prom.read(0) >> 2) & 0x01;
                bit3 = (color_prom.read(0) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x42 * bit2 + 0x90 * bit3));
                bit0 = (color_prom.read(Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x42 * bit2 + 0x90 * bit3));
                bit0 = (color_prom.read(2 * Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(2 * Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(2 * Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(2 * Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x42 * bit2 + 0x90 * bit3));

                color_prom.inc();
            }
        }
    };

    public static WriteHandlerPtr wiz_attributes_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((offset & 1) != 0 && wiz_attributesram.read(offset) != data) {
                int i;

                for (i = offset / 2; i < videoram_size[0]; i += 32) {
                    dirtybuffer[i] = 1;
                }
            }

            wiz_attributesram.write(offset, data);
        }
    };

    public static WriteHandlerPtr wiz_palettebank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (palbank[offset] != (data & 1)) {
                palbank[offset] = (char) ((data & 1) & 0xFF);
                palette_bank = palbank[0] + 2 * palbank[1];

                memset(dirtybuffer, 1, videoram_size[0]);
            }
        }
    };

    public static WriteHandlerPtr wiz_char_bank_select_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (char_bank[offset] != (data & 1)) {
                char_bank[offset] = (char) ((data & 1) & 0xFF);
                memset(dirtybuffer, 1, videoram_size[0]);
            }
        }
    };

    public static WriteHandlerPtr wiz_flipx_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (flipx != data) {
                flipx = data;

                memset(dirtybuffer, 1, videoram_size[0]);
            }
        }
    };

    public static WriteHandlerPtr wiz_flipy_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (flipy != data) {
                flipy = data;

                memset(dirtybuffer, 1, videoram_size[0]);
            }
        }
    };

    static void draw_background(osd_bitmap bitmap, int bank, int colortype) {
        int i, offs;

        /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
        for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
            if (dirtybuffer[offs] != 0) {
                int sx, sy, col;

                dirtybuffer[offs] = 0;

                sx = offs % 32;
                sy = offs / 32;

                if (colortype != 0) {
                    col = (wiz_attributesram.read(2 * sx + 1) & 0x07);
                } else {
                    col = (colorram.read(offs) & 0x07);
                }

                if (flipx != 0) {
                    sx = 31 - sx;
                }
                if (flipy != 0) {
                    sy = 31 - sy;
                }

                drawgfx(tmpbitmap, Machine.gfx[bank],
                        videoram.read(offs),
                        (wiz_attributesram.read(2 * (offs % 32) + 1) & 0x07) + 8 * palette_bank,
                        flipx, flipy,
                        8 * sx, 8 * sy,
                        null, TRANSPARENCY_NONE, 0);
            }
        }

        /* copy the temporary bitmap to the screen */
        {
            int[] scroll = new int[32];

            if (flipx != 0) {
                for (i = 0; i < 32; i++) {
                    scroll[31 - i] = -wiz_attributesram.read(2 * i);
                    if (flipy != 0) {
                        scroll[31 - i] = -scroll[31 - i];
                    }
                }
            } else {
                for (i = 0; i < 32; i++) {
                    scroll[i] = -wiz_attributesram.read(2 * i);
                    if (flipy != 0) {
                        scroll[i] = -scroll[i];
                    }
                }
            }

            copyscrollbitmap(bitmap, tmpbitmap, 0, null, 32, scroll, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

        }
    }

    static void draw_foreground(osd_bitmap bitmap, int colortype) {
        int offs;

        /* draw the frontmost playfield. They are characters, but draw them as sprites. */
        for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
            int scroll, sx, sy, col;

            sx = offs % 32;
            sy = offs / 32;

            if (colortype != 0) {
                col = (wiz_attributesram2.read(2 * sx + 1) & 0x07);
            } else {
                col = (wiz_colorram2.read(offs) & 0x07);
            }

            scroll = (8 * sy + 256 - wiz_attributesram2.read(2 * sx)) % 256;
            if (flipy != 0) {
                scroll = (248 - scroll) % 256;
            }
            if (flipx != 0) {
                sx = 31 - sx;
            }

            drawgfx(bitmap, Machine.gfx[char_bank[1]],
                    wiz_videoram2.read(offs),
                    col + 8 * palette_bank,
                    flipx, flipy,
                    8 * sx, scroll,
                    Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
        }
    }

    static void draw_sprites(osd_bitmap bitmap, UBytePtr sprite_ram,
            int bank, rectangle visible_area) {
        int offs;

        for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
            int sx, sy;

            sx = sprite_ram.read(offs + 3);
            sy = sprite_ram.read(offs);

            if (sx == 0 || sy == 0) {
                continue;
            }

            if (flipx != 0) {
                sx = 240 - sx;
            }
            if (flipy == 0) {
                sy = 240 - sy;
            }

            drawgfx(bitmap, Machine.gfx[bank],
                    sprite_ram.read(offs + 1),
                    (sprite_ram.read(offs + 2) & 0x07) + 8 * palette_bank,
                    flipx, flipy,
                    sx, sy,
                    visible_area, TRANSPARENCY_PEN, 0);
        }
    }

    /**
     * *************************************************************************
     *
     * Draw the game screen in the given osd_bitmap. Do NOT call
     * osd_update_display() from this function, it will be called by the main
     * emulation engine.
     *
     **************************************************************************
     */
    public static VhUpdatePtr wiz_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int bank;
            rectangle visible_area;

            draw_background(bitmap, 2 + ((char_bank[0] << 1) | char_bank[1]), 0);
            draw_foreground(bitmap, 0);

            visible_area = flipx != 0 ? spritevisibleareaflipx : spritevisiblearea;

            /* I seriously doubt that the real hardware works this way */
            if ((spriteram.read(1) & 0x80) != 0 || spriteram.read(3) == 0 || spriteram.read(0) == 0) {
                bank = 7 + wiz_sprite_bank.read();
            } else {
                bank = 8;	// Dragon boss
            }

            draw_sprites(bitmap, spriteram_2, 6, visible_area);
            draw_sprites(bitmap, spriteram, bank, visible_area);
        }
    };

    public static VhUpdatePtr stinger_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            draw_background(bitmap, 2 + char_bank[0], 1);
            draw_foreground(bitmap, 1);
            draw_sprites(bitmap, spriteram_2, 4, Machine.drv.visible_area);
            draw_sprites(bitmap, spriteram, 5, Machine.drv.visible_area);
        }
    };
}
