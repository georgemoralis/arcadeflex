/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
/**
 * Changelog
 * =========
 * 30/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.osdependH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;

public class pcktgal {

    public static VhUpdateHandlerPtr pcktgal_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* Draw character tiles */
            for (offs = videoram_size[0] - 2; offs >= 0; offs -= 2) {
                if (dirtybuffer[offs] != 0 || dirtybuffer[offs + 1] != 0) {
                    int sx, sy;

                    dirtybuffer[offs] = dirtybuffer[offs + 1] = 0;

                    sx = (offs / 2) % 32;
                    sy = (offs / 2) / 32;

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs + 1) + ((videoram.read(offs) & 0x0f) << 8),
                            videoram.read(offs) >> 4,
                            0, 0,
                            8 * sx, 8 * sy,
                            Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the character mapped graphics */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            /* Sprites */
            for (offs = 0; offs < spriteram_size[0]; offs += 4) {
                if (spriteram.read(offs) != 0xf8) {
                    int sx, sy, flipx, flipy;

                    sx = 240 - spriteram.read(offs + 2);
                    sy = 240 - spriteram.read(offs);

                    flipx = spriteram.read(offs + 1) & 0x04;
                    flipy = spriteram.read(offs + 1) & 0x02;

                    drawgfx(bitmap, Machine.gfx[1],
                            spriteram.read(offs + 3) + ((spriteram.read(offs + 1) & 1) << 8),
                            (spriteram.read(offs + 1) & 0x70) >> 4,
                            flipx, flipy,
                            sx, sy,
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                }
            }
        }
    };
}
