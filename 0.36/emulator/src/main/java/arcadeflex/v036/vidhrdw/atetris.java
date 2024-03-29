/**
 * ported to v0.36
 */
/**
 * Changelog
 * =========
 * 15/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.mame.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//common imports
import static common.libc.cstring.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;

public class atetris {

    // Uncomment this if you want to see all slapstic accesses
    //#define LOG_SLAPSTICK
    static int BANK0 = 0x10000;
    static int BANK1 = 0x4000;

    static int slapstic_primed = 0;
    static int slapstic_bank = BANK0;
    static int slapstic_nextbank = -1;
    static int slapstic_75xxcnt = 0;
    static int slapstic_last60xx = 0;
    static int slapstic_last75xx = 0;

    // I'm not sure if the information here is sufficient to figure how to the
    // Slapstic chip really works in this game, because BANK1 seem to be only
    // used rarely.
    //
    // But it seems like that reading 6090 twice in a row can select either bank.
    // The main difference between the 2 cases is that when BANK1 is selected,
    // there are 2 LD A,75XXh instructions between the 6090 reads, while when
    // BANK0 gets selected, there are 3.
    public static ReadHandlerPtr atetris_slapstic_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (slapstic_nextbank != -1) {
                slapstic_bank = slapstic_nextbank;
                slapstic_nextbank = -1;
            }

            if ((offset & 0xff00) == 0x2000
                    || (offset & 0xff00) == 0x3500) {
                if (offset == 0x2000) {
                    // Reset
                    slapstic_75xxcnt = 0;
                    slapstic_last60xx = 0;
                    slapstic_primed = 1;
                } else if (offset >= 0x3500) {
                    slapstic_75xxcnt++;
                    slapstic_last75xx = (offset & 0xff);
                } else {
                    if (slapstic_primed != 0) {
                        switch (offset & 0xff) {
                            case 0x80: {
                                slapstic_nextbank = BANK0;
                                /*#ifdef LOG_SLAPSTICK
	                    if (errorlog) fprintf(errorlog,"Selecting Bank 0 at %04X\n", cpu_get_pc());
	#endif*/
                            }
                            break;

                            case 0x90:
                                if ((slapstic_75xxcnt == 0)
                                        || (slapstic_75xxcnt == 2 && slapstic_last60xx == 0x90)) {
                                    slapstic_nextbank = BANK1;
                                    /*#ifdef LOG_SLAPSTICK
	                    if (errorlog) fprintf(errorlog,"Selecting Bank 1 at %04X\n", cpu_get_pc());
	#endif*/
                                } else {
                                    slapstic_nextbank = BANK0;
                                    /*#ifdef LOG_SLAPSTICK
	                   if (errorlog) fprintf(errorlog,"Selecting Bank 0 at %04X\n", cpu_get_pc());
	#endif*/
                                }
                                break;

                            case 0xa0:
                                if (slapstic_last60xx == 0xb0) {
                                    slapstic_nextbank = BANK1;
                                    /*#ifdef LOG_SLAPSTICK
	                    if (errorlog) fprintf(errorlog,"Selecting Bank 1 at %04X\n", cpu_get_pc());
	#endif*/
                                } else {
                                    slapstic_nextbank = BANK0;
                                    /*#ifdef LOG_SLAPSTICK
	                    if (errorlog) fprintf(errorlog,"Selecting Bank 0 at %04X\n", cpu_get_pc());
	#endif*/
                                }
                                break;

                            case 0xb0:
                                if (slapstic_75xxcnt == 6 && slapstic_last60xx == 0xb0
                                        && slapstic_last75xx == 0x53) {
                                    slapstic_nextbank = BANK1;
                                    /*#ifdef LOG_SLAPSTICK
	                    if (errorlog) fprintf(errorlog,"Selecting Bank 1 at %04X\n", cpu_get_pc());
	#endif*/
                                } else {
                                    slapstic_nextbank = BANK0;
                                    /*#ifdef LOG_SLAPSTICK
	                    if (errorlog) fprintf(errorlog,"Selecting Bank 0 at %04X\n", cpu_get_pc());
	#endif*/
                                }
                                break;

                            default:
                                slapstic_primed = 0;
                                break;
                        }
                    }

                    slapstic_last60xx = (offset & 0xff);
                    slapstic_75xxcnt = 0;
                }
            } else {
                slapstic_primed = 0;
            }

            return memory_region(REGION_CPU1).read(slapstic_bank + offset);
        }
    };

    /**
     * *************************************************************************
     *
     * Draw the game screen in the given osd_bitmap. Do NOT call
     * osd_update_display() from this function, it will be called by the main
     * emulation engine.
     *
     **************************************************************************
     */
    public static VhUpdateHandlerPtr atetris_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* recalc the palette if necessary */
            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            /* for every character in the backround RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = 0; offs < videoram_size[0]; offs += 2) {
                int charcode, sx, sy, color;

                if (dirtybuffer[offs] == 0 && dirtybuffer[offs + 1] == 0) {
                    continue;
                }

                dirtybuffer[offs] = dirtybuffer[offs + 1] = 0;

                sy = 8 * (offs / 128);
                sx = 4 * (offs % 128);

                if (sx >= 42 * 8) {
                    continue;
                }

                charcode = videoram.read(offs) | ((videoram.read(offs + 1) & 0x07) << 8);

                color = ((videoram.read(offs + 1) & 0xf0) >> 4);

                drawgfx(tmpbitmap, Machine.gfx[0],
                        charcode,
                        color,
                        0, 0,
                        sx, sy,
                        Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
        }
    };
}
