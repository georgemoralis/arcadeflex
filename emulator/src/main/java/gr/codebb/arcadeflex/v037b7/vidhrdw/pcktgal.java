/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.vidhrdw;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static common.libc.cstring.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.vidhrdw.generic.*;

public class pcktgal {

    static int flipscreen;
    static int last_flip;
    public static WriteHandlerPtr pcktgal_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            flipscreen = (data & 0x80) != 0 ? 1 : 0;
            if (last_flip != flipscreen) {
                memset(dirtybuffer, 1, 0x800);
            }
            last_flip = flipscreen;
        }
    };

    public static VhUpdateHandlerPtr pcktgal_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* Draw character tiles */
            for (offs = videoram_size[0] - 2; offs >= 0; offs -= 2) {
                if (dirtybuffer[offs] != 0 || dirtybuffer[offs + 1] != 0) {
                    int sx, sy, fx = 0, fy = 0;

                    dirtybuffer[offs] = dirtybuffer[offs + 1] = 0;

                    sx = (offs / 2) % 32;
                    sy = (offs / 2) / 32;
                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                        fx = 1;
                        fy = 1;
                    }

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs + 1) + ((videoram.read(offs) & 0x0f) << 8),
                            videoram.read(offs) >> 4,
                            fx, fy,
                            8 * sx, 8 * sy,
                            Machine.visible_area, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the character mapped graphics */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_NONE, 0);

            /* Sprites */
            for (offs = 0; offs < spriteram_size[0]; offs += 4) {
                if (spriteram.read(offs) != 0xf8) {
                    int sx, sy, flipx, flipy;

                    sx = 240 - spriteram.read(offs + 2);
                    sy = 240 - spriteram.read(offs);

                    flipx = spriteram.read(offs + 1) & 0x04;
                    flipy = spriteram.read(offs + 1) & 0x02;
                    if (flipscreen != 0) {
                        sx = 240 - sx;
                        sy = 240 - sy;
                        if (flipx != 0) {
                            flipx = 0;
                        } else {
                            flipx = 1;
                        }
                        if (flipy != 0) {
                            flipy = 0;
                        } else {
                            flipy = 1;
                        }
                    }

                    drawgfx(bitmap, Machine.gfx[1],
                            spriteram.read(offs + 3) + ((spriteram.read(offs + 1) & 1) << 8),
                            (spriteram.read(offs + 1) & 0x70) >> 4,
                            flipx, flipy,
                            sx, sy,
                            Machine.visible_area, TRANSPARENCY_PEN, 0);
                }
            }
        }
    };
}
