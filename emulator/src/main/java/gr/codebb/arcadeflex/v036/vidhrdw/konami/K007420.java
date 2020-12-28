package gr.codebb.arcadeflex.v036.vidhrdw.konami;

/*
 used in battlnts driver. Seems to be fully functional
 */
import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.common.libc.expressions.NOT;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konami.K007342.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;

public class K007420 {

    //K007420_callback interface

    public static abstract interface K007420_callbackProcPtr {

        public abstract void handler(int[] code, int[] color);
    }
    static GfxElement K007420_gfx;
    static K007420_callbackProcPtr K007420_callback;
    static UBytePtr K007420_ram;

    public static int K007420_vh_start(int gfxnum, K007420_callbackProcPtr callback) {
        K007420_gfx = Machine.gfx[gfxnum];
        K007420_callback = callback;
        K007420_ram = new UBytePtr(0x200);
        if (K007420_ram == null) {
            return 1;
        }

        memset(K007420_ram, 0, 0x200);

        return 0;
    }

    public static void K007420_vh_stop() {
        K007420_ram = null;
    }

    public static ReadHandlerPtr K007420_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return K007420_ram.read(offset);
        }
    };

    public static WriteHandlerPtr K007420_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            K007420_ram.write(offset, data);
        }
    };

    /*
     * Sprite Format
     * ------------------
     *
     * Byte | Bit(s)   | Use
     * -----+-76543210-+----------------
     *   0  | xxxxxxxx | y position
     *   1  | xxxxxxxx | sprite code (low 8 bits)
     *   2  | xxxxxxxx | depends on external conections. Usually banking
     *   3  | xxxxxxxx | x position (low 8 bits)
     *   4  | x------- | x position (high bit)
     *   4  | -xxx---- | sprite size 000=16x16 001=8x16 010=16x8 011=8x8 100=32x32
     *   4  | ----x--- | flip y
     *   4  | -----x-- | flip x
     *   4  | ------xx | zoom (bits 8 & 9)
     *   5  | xxxxxxxx | zoom (low 8 bits)  0x080 = normal, < 0x80 enlarge, > 0x80 reduce
     *   6  | xxxxxxxx | unused
     *   7  | xxxxxxxx | unused
     */
    public static void K007420_sprites_draw(osd_bitmap bitmap) {
        int K007420_SPRITERAM_SIZE = 0x200;
        int offs;

        for (offs = K007420_SPRITERAM_SIZE - 8; offs >= 0; offs -= 8) {
            int ox, oy, flipx, flipy, zoom, w, h, x, y;
            int[] code = new int[1];
            int[] color = new int[1];
            int xoffset[] = {0, 1, 4, 5};
            int yoffset[] = {0, 2, 8, 10};

            code[0] = K007420_ram.read(offs + 1);
            color[0] = K007420_ram.read(offs + 2);
            ox = K007420_ram.read(offs + 3) - ((K007420_ram.read(offs + 4) & 0x80) << 1);
            oy = 256 - K007420_ram.read(offs + 0);
            flipx = K007420_ram.read(offs + 4) & 0x04;
            flipy = K007420_ram.read(offs + 4) & 0x08;

            K007420_callback.handler(code, color);

            /* kludge for rock'n'rage */
            if ((K007420_ram.read(offs + 4) == 0x40) && (K007420_ram.read(offs + 1) == 0xff)
                    && (K007420_ram.read(offs + 2) == 0x00) && (K007420_ram.read(offs + 5) == 0xf0)) {
                continue;
            }

            /* 0x080 = normal scale, 0x040 = double size, 0x100 half size */
            zoom = K007420_ram.read(offs + 5) | ((K007420_ram.read(offs + 4) & 0x03) << 8);
            if (zoom == 0) {
                continue;
            }
            zoom = 0x10000 * 128 / zoom;

            switch (K007420_ram.read(offs + 4) & 0x70) {
                case 0x30:
                    w = h = 1;
                    break;
                case 0x20:
                    w = 2;
                    h = 1;
                    code[0] &= (~1);
                    break;
                case 0x10:
                    w = 1;
                    h = 2;
                    code[0] &= (~2);
                    break;
                case 0x00:
                    w = h = 2;
                    code[0] &= (~3);
                    break;
                case 0x40:
                    w = h = 4;
                    code[0] &= (~3);
                    break;
                default:
                    w = 1;
                    h = 1;
                //if (errorlog) fprintf(errorlog,"Unknown sprite size %02x\n",(K007420_ram[offs+4] & 0x70)>>4);
            }

            if (K007342_flipscreen != 0) {
                ox = 256 - ox - ((zoom * w + (1 << 12)) >> 13);
                oy = 256 - oy - ((zoom * h + (1 << 12)) >> 13);
                flipx = NOT(flipx);
                flipy = NOT(flipy);
            }

            if (zoom == 0x10000) {
                int sx, sy;

                for (y = 0; y < h; y++) {
                    sy = oy + 8 * y;

                    for (x = 0; x < w; x++) {
                        int c = code[0];

                        sx = ox + 8 * x;
                        if (flipx != 0) {
                            c += xoffset[(w - 1 - x)];
                        } else {
                            c += xoffset[x];
                        }
                        if (flipy != 0) {
                            c += yoffset[(h - 1 - y)];
                        } else {
                            c += yoffset[y];
                        }

                        drawgfx(bitmap, K007420_gfx,
                                c,
                                color[0],
                                flipx, flipy,
                                sx, sy,
                                Machine.drv.visible_area, TRANSPARENCY_PEN, 0);

                        if ((K007342_regs[2] & 0x80) != 0) {
                            drawgfx(bitmap, K007420_gfx,
                                    c,
                                    color[0],
                                    flipx, flipy,
                                    sx, sy - 256,
                                    Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                        }
                    }
                }
            } else {
                int sx, sy, zw, zh;
                for (y = 0; y < h; y++) {
                    sy = oy + ((zoom * y + (1 << 12)) >> 13);
                    zh = (oy + ((zoom * (y + 1) + (1 << 12)) >> 13)) - sy;

                    for (x = 0; x < w; x++) {
                        int c = code[0];

                        sx = ox + ((zoom * x + (1 << 12)) >> 13);
                        zw = (ox + ((zoom * (x + 1) + (1 << 12)) >> 13)) - sx;
                        if (flipx != 0) {
                            c += xoffset[(w - 1 - x)];
                        } else {
                            c += xoffset[x];
                        }
                        if (flipy != 0) {
                            c += yoffset[(h - 1 - y)];
                        } else {
                            c += yoffset[y];
                        }

                        drawgfxzoom(bitmap, K007420_gfx,
                                c,
                                color[0],
                                flipx, flipy,
                                sx, sy,
                                Machine.drv.visible_area, TRANSPARENCY_PEN, 0,
                                (zw << 16) / 8, (zh << 16) / 8);

                        if ((K007342_regs[2] & 0x80) != 0) {
                            drawgfxzoom(bitmap, K007420_gfx,
                                    c,
                                    color[0],
                                    flipx, flipy,
                                    sx, sy - 256,
                                    Machine.drv.visible_area, TRANSPARENCY_PEN, 0,
                                    (zw << 16) / 8, (zh << 16) / 8);
                        }
                    }
                }
            }
        }
    }
}
