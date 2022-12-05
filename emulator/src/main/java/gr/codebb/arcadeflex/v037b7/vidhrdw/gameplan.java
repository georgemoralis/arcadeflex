/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static arcadeflex.v036.vidhrdw.generic.*;

public class gameplan {

    static int gameplan_this_is_kaos;
    static int gameplan_this_is_megatack;
    static int clear_to_colour = 0;
    static int fix_clear_to_colour = -1;
    static String colour_names[] = {"WHITE", "CYAN", "MAGENTA", "BLUE",
        "YELLOW", "GREEN", "RED", ".BLACK"};

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartPtr gameplan_vh_start = new VhStartPtr() {
        public int handler() {
            if (strcmp(Machine.gamedrv.name, "kaos") == 0) {
                gameplan_this_is_kaos = 1;
            } else {
                gameplan_this_is_kaos = 0;
            }

            if (strcmp(Machine.gamedrv.name, "megatack") == 0) {
                gameplan_this_is_megatack = 1;
            } else {
                gameplan_this_is_megatack = 0;
            }

            return generic_bitmapped_vh_start.handler();
        }
    };

    static int port_b;
    static int new_request = 0;
    static int finished_sound = 0;
    static int cb2 = -1;

    public static ReadHandlerPtr gameplan_sound_r = new ReadHandlerPtr() {
        public int handler(int offset) {

            if (offset == 0) {

                return finished_sound;
            } else {
                return 0;
            }
        }
    };

    public static WriteHandlerPtr gameplan_sound_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset == 1) {
                if (cb2 == 0) {
                    //	enabling this causes a hang in Challenger when entering high score name
                    //			cpu_set_reset_line(1,PULSE_LINE);
                    return;
                }

                port_b = data;
                finished_sound = 0;
                new_request = 1;

                /* shortly after requesting a sound, the game board checks
			   whether the sound board has ackknowledged receipt of the
			   command - yield now to allow it to send the ACK */
                //		cpu_yield();	enabling this causes a hang in Challenger when entering high score name
            } else if (offset == 0x0c) /* PCR */ {
                if ((data & 0x80) != 0) {
                    if ((data & 0x60) == 0x60) {
                        cb2 = 1;
                    } else if ((data & 0x60) == 0x40) {
                        cb2 = 0;
                    } else {
                        cb2 = -1;
                    }
                }
            }
        }
    };

    public static ReadHandlerPtr gameplan_via5_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (offset == 0) {
                new_request = 0;
                return port_b;
            }

            if (offset == 5) {
                if (new_request == 1) {
                    return 0x40;
                } else {
                    return 0;
                }
            }

            return 1;
        }
    };

    public static WriteHandlerPtr gameplan_via5_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset == 2) {
                finished_sound = data;
            }
        }
    };
    static int x_1;
    public static ReadHandlerPtr gameplan_video_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            x_1++;
            return x_1;
        }
    };
    static int r0 = -1;
    static /*unsigned*/ char xpos, ypos, colour = 7;
    public static WriteHandlerPtr gameplan_video_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset == 0) /* write to 2000 */ {
                r0 = data;
            } else if (offset == 1) /* write to 2001 */ {
                if (r0 == 0) {
                    if (gameplan_this_is_kaos != 0) {
                        colour = (char) ((~data & 0x07) & 0xFF);
                    } else if ((data & 0x0f) != 0) {
                    }
                    if ((data & 0x20) != 0) {
                        if ((data & 0x80) != 0) {
                            ypos = (char) ((ypos - 1) & 0xFF);
                        } else {
                            ypos = (char) ((ypos + 1) & 0xFF);
                        }
                    }
                    if ((data & 0x10) != 0) {
                        if ((data & 0x40) != 0) {
                            xpos = (char) ((xpos - 1) & 0xFF);
                        } else {
                            xpos = (char) ((xpos + 1) & 0xFF);
                        }
                    }

                    plot_pixel2(Machine.scrbitmap, tmpbitmap, xpos, ypos, Machine.pens[colour]);
                } else if (r0 == 1) {
                    xpos = (char) (data & 0xFF);
                } else if (r0 == 2) {
                    ypos = (char) (data & 0xFF);
                } else if (r0 == 3) {
                    if (offset == 1 && data == 0) {
                        gameplan_clear_screen();
                    }
                }
            } else if (offset == 2) {
                if (data == 7) {
                    /* This whole 'fix_clear_to_colour' and special casing for
				 * megatack thing is ugly, and doesn't even work properly.
                     */

                    if (gameplan_this_is_megatack == 0 || fix_clear_to_colour == -1) {
                        clear_to_colour = colour;
                    }
                }
            } else if (offset == 3) {
                if (r0 == 0) {

                    colour = (char) ((data & 7) & 0xFF);
                } else if ((data & 0xf8) == 0xf8 && data != 0xff) {
                    clear_to_colour = fix_clear_to_colour = data & 0x07;
                }
            }
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
    public static void gameplan_clear_screen() {
        fillbitmap(tmpbitmap, Machine.pens[clear_to_colour], null);
        fillbitmap(Machine.scrbitmap, Machine.pens[clear_to_colour], null);

        fix_clear_to_colour = -1;
    }
}
