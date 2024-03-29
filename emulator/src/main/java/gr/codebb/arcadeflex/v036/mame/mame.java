/*
 This file is part of Arcadeflex.

 Arcadeflex is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Arcadeflex is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Arcadeflex.  If not, see <http://www.gnu.org/licenses/>.
 */
package gr.codebb.arcadeflex.v036.mame;

/**
 *
 * @author george
 */
import gr.codebb.arcadeflex.common.SubArrays.UShortArray;
import gr.codebb.arcadeflex.v036.platform.MainStream;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static common.libc.cstdlib.rand;
import static gr.codebb.arcadeflex.v036.mame.mameH.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.platform.fileio.*;
import static gr.codebb.arcadeflex.v036.mame.driver.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.usrintrf.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static arcadeflex.v036.mame.input.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.mame.sndintrf.sound_start;
import static arcadeflex.v036.mame.sndintrf.sound_stop;
import static arcadeflex.v036.mame.sndintrf.sound_update;
import static gr.codebb.arcadeflex.v036.mame.spriteC.*;

public class mame {

    static RunningMachine machine = new RunningMachine();
    public static RunningMachine Machine = machine;
    static GameDriver gamedrv;
    private static MachineDriver drv;
    /* Variables to hold the status of various game options */
    public static GameOptions options = new GameOptions();
    public static FILE errorlog;
    public static FILE record;
    /* for -record */

    public static FILE playback;
    /* for -playback */

    public static int mame_debug;
    /* !0 when -debug option is specified */

    public static int bailing;
    /* set to 1 if the startup is aborted to prevent multiple error messages */

    public static int settingsloaded;
    public static int bitmap_dirty;

    /* set by osd_clearbitmap() */
    public static int run_game(int game) {
        int err;

        /* copy some settings into easier-to-handle variables */
        errorlog = options.errorlog;
        record = options.record;
        playback = options.playback;
        mame_debug = options.mame_debug;

        Machine.gamedrv = gamedrv = drivers[game];
        System.out.println("Machine.gamedrv: " + Machine.gamedrv.drv.init_machine);
        System.out.println("drivers[game]: " + drivers[game].drv.init_machine);
        Machine.drv = drv = gamedrv.drv;
        /*TODO TOTAL HACK TO BE REMOVED*/ Machine.visible_area = Machine.drv.visible_area;
        /* copy configuration */
        if (options.color_depth == 16
                || (options.color_depth != 8 && (Machine.gamedrv.flags & GAME_REQUIRES_16BIT) != 0)) {
            Machine.color_depth = 16;
        } else {
            Machine.color_depth = 8;
        }
        Machine.sample_rate = options.samplerate;

        /* get orientation right */
        Machine.orientation = gamedrv.flags & ORIENTATION_MASK;
        Machine.ui_orientation = ROT0;
        if (options.norotate != 0) {
            Machine.orientation = ROT0;
        }
        if (options.ror != 0) {
            /* if only one of the components is inverted, switch them */
            if ((Machine.orientation & ROT180) == ORIENTATION_FLIP_X
                    || (Machine.orientation & ROT180) == ORIENTATION_FLIP_Y) {
                Machine.orientation ^= ROT180;
            }

            Machine.orientation ^= ROT90;

            /* if only one of the components is inverted, switch them */
            if ((Machine.ui_orientation & ROT180) == ORIENTATION_FLIP_X
                    || (Machine.ui_orientation & ROT180) == ORIENTATION_FLIP_Y) {
                Machine.ui_orientation ^= ROT180;
            }

            Machine.ui_orientation ^= ROT90;
        }
        if (options.rol != 0) {
            /* if only one of the components is inverted, switch them */
            if ((Machine.orientation & ROT180) == ORIENTATION_FLIP_X
                    || (Machine.orientation & ROT180) == ORIENTATION_FLIP_Y) {
                Machine.orientation ^= ROT180;
            }

            Machine.orientation ^= ROT270;

            /* if only one of the components is inverted, switch them */
            if ((Machine.ui_orientation & ROT180) == ORIENTATION_FLIP_X
                    || (Machine.ui_orientation & ROT180) == ORIENTATION_FLIP_Y) {
                Machine.ui_orientation ^= ROT180;
            }

            Machine.ui_orientation ^= ROT270;
        }
        if (options.flipx != 0) {
            Machine.orientation ^= ORIENTATION_FLIP_X;
            Machine.ui_orientation ^= ORIENTATION_FLIP_X;
        }
        if (options.flipy != 0) {
            Machine.orientation ^= ORIENTATION_FLIP_Y;
            Machine.ui_orientation ^= ORIENTATION_FLIP_Y;
        }

        set_pixel_functions();

        /* Do the work*/
        err = 1;
        bailing = 0;

        if (osd_init() == 0) {
            if (init_machine() == 0) {
                if (run_machine() == 0) {
                    err = 0;
                } else if (bailing == 0) {
                    bailing = 1;
                    printf("Unable to start machine emulation\n");
                }

                shutdown_machine();
            } else if (bailing == 0) {
                bailing = 1;
                printf("Unable to initialize machine emulation\n");
            }

            osd_exit();
        } else if (bailing == 0) {
            bailing = 1;
            printf("Unable to initialize system\n");
        }

        return err;
    }

    /**
     * *************************************************************************
     *
     * Initialize the emulated machine (load the roms, initialize the various
     * subsystems...). Returns 0 if successful.
     *
     **************************************************************************
     */
    public static int init_machine() {
        int i;

        System.out.println("mame init_machine");

        if (code_init() != 0) {
            return out();
        }

        for (i = 0; i < MAX_MEMORY_REGIONS; i++) {
            Machine.memory_region[i] = null;
            Machine.memory_region_length[i] = 0;
            Machine.memory_region_type[i] = 0;
        }

        if (gamedrv.input_ports != null) {
            Machine.input_ports = input_port_allocate(gamedrv.input_ports);
            if (Machine.input_ports == null) {
                return out_code();
            }
            Machine.input_ports_default = input_port_allocate(gamedrv.input_ports);
            if (Machine.input_ports_default == null) {
                input_port_free(Machine.input_ports);
                Machine.input_ports = null;
                return out_code();
            }
        }

        if (readroms() != 0) {
            return out_free();
        }


        /* Mish:  Multi-session safety - set spriteram size to zero before memory map is set up */
        spriteram_size[0] = 0;
        spriteram_2_size[0] = 0;

        /* first of all initialize the memory handlers, which could be used by the */
 /* other initialization routines */
        System.out.println(gamedrv.driver_init);
        cpu_init();
        System.out.println(gamedrv.driver_init);

        /* load input ports settings (keys, dip switches, and so on) */
        settingsloaded = load_input_port_settings();

        if (memory_init() == 0) {
            return out_free();
        }

        if (gamedrv.driver_init != null) {
            gamedrv.driver_init.handler();
        }

        System.out.println("FIN mame init_machine " + gamedrv.driver_init);

        return 0;
    }

    static int out_free() {
        /*TODO*/ //    out_free:
/*TODO*/ //            input_port_free(Machine.input_ports);
/*TODO*/ //            Machine.input_ports = 0;
/*TODO*/ //            input_port_free(Machine.input_ports_default);
/*TODO*/ //            Machine.input_ports_default = 0;
        return out_code();
    }

    static int out_code() {

        code_close();
        return out();
    }

    static int out() {
        return 1;
    }

    public static void shutdown_machine() {
        int i;

        /* ASG 971007 free memory element map */
 /*TODO*/ //            memory_shutdown();

        /* free the memory allocated for ROM and RAM */
        for (i = 0; i < MAX_MEMORY_REGIONS; i++) {
            Machine.memory_region[i] = null;
            Machine.memory_region_length[i] = 0;
            Machine.memory_region_type[i] = 0;
        }

        /* free the memory allocated for input ports definition */
 /*TODO*/ //            input_port_free(Machine.input_ports);
/*TODO*/ //            Machine.input_ports = 0;
/*TODO*/ //            input_port_free(Machine.input_ports_default);
/*TODO*/ //            Machine.input_ports_default = 0;
        code_close();
    }

    public static void vh_close() {
        int i;


        /*TODO*/ //           for (i = 0;i < MAX_GFX_ELEMENTS;i++)
        /*TODO*/ //           {
        /*TODO*/ //                   freegfx(Machine.gfx[i]);
        /*TODO*/ //                   Machine.gfx[i] = 0;
        /*TODO*/ //           }
        /*TODO*/ //           freegfx(Machine.uifont);
        /*TODO*/ //           Machine.uifont = 0;
        /*TODO*/ //           osd_close_display();
        /*TODO*/ //           palette_stop();

        /*TODO*/ //           if (drv.video_attributes & VIDEO_BUFFERS_SPRITERAM) {
        /*TODO*/ //                   if (buffered_spriteram) free(buffered_spriteram);
        /*TODO*/ //                   if (buffered_spriteram_2) free(buffered_spriteram_2);
        /*TODO*/ //                   buffered_spriteram=NULL;
        /*TODO*/ //                   buffered_spriteram_2=NULL;
        /*TODO*/ //           }
    }

    public static int vh_open() {
        int i;

        for (i = 0; i < MAX_GFX_ELEMENTS; i++) {
            Machine.gfx[i] = null;
        }
        Machine.uifont = null;

        if (palette_start() != 0) {
            vh_close();
            return 1;
        }


        /* convert the gfx ROMs into character sets. This is done BEFORE calling the driver's */
 /* convert_color_prom() routine (in palette_init()) because it might need to check the */
 /* Machine.gfx[] data */
        if (drv.gfxdecodeinfo != null) {
            for (i = 0; i < drv.gfxdecodeinfo.length && i < MAX_GFX_ELEMENTS && drv.gfxdecodeinfo[i].memory_region != -1; i++) {
                int reglen = 8 * memory_region_length(drv.gfxdecodeinfo[i].memory_region);
                GfxLayout glcopy;
                int j;

                glcopy = drv.gfxdecodeinfo[i].gfxlayout;//memcpy(&glcopy,drv.gfxdecodeinfo[i].gfxlayout,sizeof(glcopy));

                if ((IS_FRAC(glcopy.total)) != 0) {
                    glcopy.total = reglen / glcopy.charincrement * FRAC_NUM(glcopy.total) / FRAC_DEN(glcopy.total);
                }
                for (j = 0; j < glcopy.planeoffset.length && j < MAX_GFX_PLANES; j++) {
                    if ((IS_FRAC(glcopy.planeoffset[j])) != 0) {
                        glcopy.planeoffset[j] = FRAC_OFFSET(glcopy.planeoffset[j])
                                + reglen * FRAC_NUM(glcopy.planeoffset[j]) / FRAC_DEN(glcopy.planeoffset[j]);
                    }
                }
                for (j = 0; j < MAX_GFX_SIZE; j++) {
                    if (j < glcopy.xoffset.length && (IS_FRAC(glcopy.xoffset[j]) != 0)) {
                        glcopy.xoffset[j] = FRAC_OFFSET(glcopy.xoffset[j])
                                + reglen * FRAC_NUM(glcopy.xoffset[j]) / FRAC_DEN(glcopy.xoffset[j]);
                    }
                    if (j < glcopy.yoffset.length && (IS_FRAC(glcopy.yoffset[j]) != 0)) {
                        glcopy.yoffset[j] = FRAC_OFFSET(glcopy.yoffset[j])
                                + reglen * FRAC_NUM(glcopy.yoffset[j]) / FRAC_DEN(glcopy.yoffset[j]);
                    }
                }
                if ((Machine.gfx[i] = decodegfx(new UBytePtr(memory_region(drv.gfxdecodeinfo[i].memory_region), drv.gfxdecodeinfo[i].start), glcopy)) == null) {
                    vh_close();
                    return 1;
                }
                if (Machine.remapped_colortable != null) {
                    Machine.gfx[i].colortable = new UShortArray(Machine.remapped_colortable, drv.gfxdecodeinfo[i].color_codes_start);
                }

                Machine.gfx[i].total_colors = drv.gfxdecodeinfo[i].total_color_codes;
            }
        }


        /* create the display bitmap, and allocate the palette */
        if ((Machine.scrbitmap = osd_create_display(drv.screen_width, drv.screen_height, Machine.color_depth, drv.video_attributes)) == null) {
            vh_close();
            return 1;
        }

        /* create spriteram buffers if necessary */
        if ((drv.video_attributes & VIDEO_BUFFERS_SPRITERAM) != 0) {
            if (spriteram_size[0] != 0) {
                buffered_spriteram = new UBytePtr(spriteram_size[0]);
                if (buffered_spriteram == null) {
                    vh_close();
                    return 1;
                }
                if (spriteram_2_size[0] != 0) {
                    buffered_spriteram_2 = new UBytePtr(spriteram_2_size[0]);
                }
                if (spriteram_2_size[0] != 0 && buffered_spriteram_2 == null) {
                    vh_close();
                    return 1;
                }
            } else {
                if (errorlog != null) {
                    fprintf(errorlog, "vh_open():  Video buffers spriteram but spriteram_size is 0\n");
                }
                buffered_spriteram = null;
                buffered_spriteram_2 = null;
            }
        }

        /* build our private user interface font */
 /* This must be done AFTER osd_create_display() so the function knows the */
 /* resolution we are running at and can pick a different font depending on it. */
 /* It must be done BEFORE palette_init() because that will also initialize */
 /* (through osd_allocate_colors()) the uifont colortable. */
        if ((Machine.uifont = builduifont()) == null) {
            vh_close();
            return 1;
        }

        /* initialize the palette - must be done after osd_create_display() */
        if (palette_init() != 0) {
            vh_close();
            return 1;
        }

        return 0;
    }
    /**
     * *************************************************************************
     *
     * This function takes care of refreshing the screen, processing user input,
     * and throttling the emulation speed to obtain the required frames per
     * second.
     *
     **************************************************************************
     */
    public static int need_to_clear_bitmap;

    /* set by the user interface */
    public static int updatescreen() {
        /* update sound */
        //if(MainStream.inst==null){ //disable sound in MainStream case for now..
        sound_update();
        //}

        if (osd_skip_this_frame() == 0) {
            if (need_to_clear_bitmap != 0) {
                osd_clearbitmap(Machine.scrbitmap);
                need_to_clear_bitmap = 0;
            }
            drv.vh_update.handler(Machine.scrbitmap, bitmap_dirty);/* update screen */
            bitmap_dirty = 0;
        }

        /* the user interface must be called between vh_update() and osd_update_video_and_audio(), */
 /* to allow it to overlay things on the game display. We must call it even */
 /* if the frame is skipped, to keep a consistent timing. */
        if (handle_user_interface() != 0) /* quit if the user asked to */ {
            return 1;
        }

        osd_update_video_and_audio();

        if (drv.vh_eof_callback != null) {
            drv.vh_eof_callback.handler();
        }

        return 0;
    }

    /**
     * *************************************************************************
     *
     * Run the emulation. Start the various subsystems and the CPU emulation.
     * Returns non zero in case of error.
     *
     **************************************************************************
     */
    public static int run_machine() {
        int res = 1;

        if (vh_open() == 0) {
            tilemap_init();
            sprite_init();
            /*TODO*///		gfxobj_init();
            if (drv.vh_start == null || drv.vh_start.handler() == 0) /* start the video hardware */ {
                if (sound_start() == 0) /* start the audio hardware */ {
                    int region;

                    /* free memory regions allocated with REGIONFLAG_DISPOSE (typically gfx roms) */
                    for (region = 0; region < MAX_MEMORY_REGIONS; region++) {
                        if ((Machine.memory_region_type[region] & REGIONFLAG_DISPOSE) != 0) {
                            int i;

                            /* invalidate contents to avoid subtle bugs */
                            for (i = 0; i < memory_region_length(region); i++) {
                                memory_region(region).write(i, rand());
                            }

                            Machine.memory_region[region] = null;
                        }
                    }

                    if (settingsloaded == 0) {
                        /* if there is no saved config, it must be first time we run this game, */
 /* so show the disclaimer. */
                        if (MainStream.inst == null) {
                            if (showcopyright() != 0) {
                                return userquit_goto();
                            }
                        }

                    }

                    if (showgamewarnings() == 0) /* show info about incorrect behaviour (wrong colors etc.) */ {
                        init_user_interface();
                        /*TODO*///
                        /*TODO*///					/* disable cheat if no roms */
                        /*TODO*///					if (!gamedrv->rom) options.cheat = 0;
                        /*TODO*///
                        /*TODO*///					if (options.cheat) InitCheat();
                        /*TODO*///
                        if (drv.nvram_handler != null) {
                            Object f;

                            f = osd_fopen(Machine.gamedrv.name, null, OSD_FILETYPE_NVRAM, 0);
                            drv.nvram_handler.handler(f, 0);
                            if (f != null) {
                                osd_fclose(f);
                            }
                        }

                        cpu_run();
                        /* run the emulation! */

                        if (drv.nvram_handler != null) {
                            Object f;

                            if ((f = osd_fopen(Machine.gamedrv.name, null, OSD_FILETYPE_NVRAM, 1)) != null) {
                                drv.nvram_handler.handler(f, 1);
                                osd_fclose(f);
                            }
                        }

                        /*TODO*/// 					if (options.cheat) StopCheat();
                        /* save input ports settings */
 /*TODO*/// 					save_input_port_settings();
                    }

                    //userquit:
                    /* the following MUST be done after hiscore_save() otherwise */
 /* some 68000 games will not work */
                    sound_stop();
                    if (drv.vh_stop != null) {
                        drv.vh_stop.handler();
                    }
                    res = 0;
                } else if (bailing == 0) {
                    bailing = 1;
                    printf("Unable to start audio emulation\n");
                }
            } else if (bailing == 0) {
                bailing = 1;
                printf("Unable to start video emulation\n");
            }

            /*TODO*///		gfxobj_close();
            /*TODO*///		sprite_close();
            /*TODO*///		tilemap_close();
            vh_close();
        } else if (bailing == 0) {
            bailing = 1;
            printf("Unable to initialize display\n");
        }

        return res;
    }

    public static int userquit_goto() //java doesn't support proper goto so do it here (shadow)
    {
        int res;
        /* the following MUST be done after hiscore_save() otherwise */
 /* some 68000 games will not work */
        sound_stop();
        if (drv.vh_stop != null) {
            drv.vh_stop.handler();
        }
        res = 0;
        /*TODO*///		gfxobj_close();
        /*TODO*///		sprite_close();
        /*TODO*///		tilemap_close();
        vh_close();
        return res;
    }

    public static int mame_highscore_enabled() {
        /* disable high score when record/playback is on */
        if (record != null || playback != null) {
            return 0;
        }

        /* disable high score when cheats are used */
 /*TODO*/ //            if (he_did_cheat != 0) return 0;
        return 1;
    }

    /* Scale the vector games to a given resolution */
    public static void scale_vectorgames(int gfx_width, int gfx_height, int width, int height) {
        double x_scale, y_scale, scale;

        if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
            x_scale = (double) gfx_width / (double) (height);
            y_scale = (double) gfx_height / (double) (width);
        } else {
            x_scale = (double) gfx_width / (double) (width);
            y_scale = (double) gfx_height / (double) (height);
        }
        if (x_scale < y_scale) {
            scale = x_scale;
        } else {
            scale = y_scale;
        }
        width = (int) ((double) width * scale);
        height = (int) ((double) height * scale);

        /* Padding to an dword value */
        width -= width % 4;
        height -= height % 4;
    }
}
