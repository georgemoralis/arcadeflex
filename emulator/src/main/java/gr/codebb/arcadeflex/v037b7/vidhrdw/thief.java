/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.platform.video.osd_free_bitmap;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.*;

public class thief {

    public static osd_bitmap thief_page0;
    public static osd_bitmap thief_page1;

    static /*UINT8*/ int u8_thief_read_mask, u8_thief_write_mask;
    static /*UINT8*/ int u8_thief_video_control;

    static class thief_coprocessorC {

        UBytePtr context_ram;
        /*UINT8*/ int u8_bank;
        UBytePtr image_ram;
        /*UINT8*/ int[] u8_param = new int[0x9];
    }

    static thief_coprocessorC thief_coprocessor;

    public static final int IMAGE_ADDR_LO = 0;        //0xe000
    public static final int IMAGE_ADDR_HI = 1;        //0xe001
    public static final int SCREEN_XPOS = 2;    //0xe002
    public static final int SCREEN_YPOS = 3;        //0xe003
    public static final int BLIT_WIDTH = 4;        //0xe004
    public static final int BLIT_HEIGHT = 5;        //0xe005
    public static final int GFX_PORT = 6;        //0xe006
    public static final int BARL_PORT = 7;            //0xe007
    public static final int BLIT_ATTRIBUTES = 8;        //0xe008

    /**
     * ************************************************************************
     */
    public static ReadHandlerPtr thief_context_ram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return thief_coprocessor.context_ram.read(0x40 * thief_coprocessor.u8_bank + offset);
        }
    };

    public static WriteHandlerPtr thief_context_ram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            thief_coprocessor.context_ram.write(0x40 * thief_coprocessor.u8_bank + offset, data);
        }
    };

    public static WriteHandlerPtr thief_context_bank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            thief_coprocessor.u8_bank = data & 0xf;
        }
    };

    /**
     * ************************************************************************
     */
    public static WriteHandlerPtr thief_video_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (((data ^ u8_thief_video_control) & 1) != 0) {
                /* screen flipped */
                memset(dirtybuffer, 0x00, 0x2000 * 2);
            }

            u8_thief_video_control = data & 0xFF;
            /*
		bit 0: screen flip
		bit 1: working page
		bit 2: visible page
		bit 3: mirrors bit 1
		bit 4: mirrors bit 2
             */
        }
    };

    public static WriteHandlerPtr thief_vtcsel_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* TMS9927 VTAC registers */
        }
    };

    public static WriteHandlerPtr thief_color_map_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /*
		--xx----	blue
		----xx--	green
		------xx	red
             */
            int intensity[] = {0x00, 0x55, 0xAA, 0xFF};
            int r = intensity[(data & 0x03) >> 0];
            int g = intensity[(data & 0x0C) >> 2];
            int b = intensity[(data & 0x30) >> 4];
            palette_change_color(offset, r, g, b);
        }
    };

    /**
     * ************************************************************************
     */
    public static WriteHandlerPtr thief_color_plane_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /*
		--xx----	selects bitplane to read from (0..3)
		----xxxx	selects bitplane(s) to write to (0x0 = none, 0xf = all)
             */
            u8_thief_write_mask = data & 0xf;
            u8_thief_read_mask = ((data >> 4) & 3) & 0xFF;
        }
    };

    public static ReadHandlerPtr thief_videoram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            UBytePtr source = new UBytePtr(videoram, offset);
            if ((u8_thief_video_control & 0x02) != 0) {
                source.inc(0x2000 * 4);
                /* foreground/background */
            }
            return source.read(u8_thief_read_mask * 0x2000);
        }
    };

    public static WriteHandlerPtr thief_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UBytePtr dest = new UBytePtr(videoram, offset);
            if ((u8_thief_video_control & 0x02) != 0) {
                dest.inc(0x2000 * 4);
                /* foreground/background */
                dirtybuffer[offset + 0x2000] = 1;
            } else {
                dirtybuffer[offset] = 1;
            }
            if ((u8_thief_write_mask & 0x1) != 0) {
                dest.write(0x2000 * 0, data);
            }
            if ((u8_thief_write_mask & 0x2) != 0) {
                dest.write(0x2000 * 1, data);
            }
            if ((u8_thief_write_mask & 0x4) != 0) {
                dest.write(0x2000 * 2, data);
            }
            if ((u8_thief_write_mask & 0x8) != 0) {
                dest.write(0x2000 * 3, data);
            }
        }
    };

    /**
     * ************************************************************************
     */
    public static VhStopPtr thief_vh_stop = new VhStopPtr() {
        public void handler() {
            videoram = null;
            dirtybuffer = null;
            osd_free_bitmap(thief_page1);
            osd_free_bitmap(thief_page0);
            thief_coprocessor.context_ram = null;
            thief_coprocessor.image_ram = null;
        }
    };

    public static VhStartPtr thief_vh_start = new VhStartPtr() {
        public int handler() {
            UBytePtr dest = memory_region(REGION_CPU1);
            UBytePtr source = memory_region(REGION_CPU2);
            if (source != null)//sharkatt doesn't have CPU2
            {
                memcpy(dest, 0xe010, source, 0x290, 0x20);
            }

            thief_coprocessor = new thief_coprocessorC();//memset( &thief_coprocessor, 0x00, sizeof(thief_coprocessor) );

            thief_page0 = bitmap_alloc(256, 256);
            thief_page1 = bitmap_alloc(256, 256);
            videoram = new UBytePtr(0x2000 * 4 * 2);//calloc( 0x2000*4*2,1 );
            dirtybuffer = new char[0x2000 * 2];

            thief_coprocessor.image_ram = new UBytePtr(0x2000);
            thief_coprocessor.context_ram = new UBytePtr(0x400);

            if (thief_page0 != null && thief_page1 != null
                    && videoram != null && dirtybuffer != null
                    && thief_coprocessor.image_ram != null
                    && thief_coprocessor.context_ram != null) {
                memset(dirtybuffer, 1, 0x2000 * 2);
                return 0;
            }
            thief_vh_stop.handler();
            return 1;
        }
    };

    public static VhUpdatePtr thief_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            /*unsigned*/
            int offs;
            int flipscreen = u8_thief_video_control & 1;
            char[] pal_data = Machine.pens;
            UBytePtr dirty = new UBytePtr(dirtybuffer);
            UBytePtr source = new UBytePtr(videoram);
            osd_bitmap page;

            if ((u8_thief_video_control & 4) != 0) {
                /* visible page */
                dirty.inc(0x2000);
                source.inc(0x2000 * 4);
                page = thief_page1;
            } else {
                page = thief_page0;
            }

            palette_recalc();

            for (offs = 0; offs < 0x2000; offs++) {
                if (dirty.read(offs) != 0) {
                    int ypos = offs / 32;
                    int xpos = (offs % 32) * 8;
                    int plane0 = source.read(0x2000 * 0 + offs);
                    int plane1 = source.read(0x2000 * 1 + offs);
                    int plane2 = source.read(0x2000 * 2 + offs);
                    int plane3 = source.read(0x2000 * 3 + offs);
                    int bit;
                    if (flipscreen != 0) {
                        for (bit = 0; bit < 8; bit++) {
                            plot_pixel.handler(page, 0xff - (xpos + bit), 0xff - ypos,
                                    pal_data[(((plane0 << bit) & 0x80) >> 7)
                                    | (((plane1 << bit) & 0x80) >> 6)
                                    | (((plane2 << bit) & 0x80) >> 5)
                                    | (((plane3 << bit) & 0x80) >> 4)]
                            );
                        }
                    } else {
                        for (bit = 0; bit < 8; bit++) {
                            plot_pixel.handler(page, xpos + bit, ypos,
                                    pal_data[(((plane0 << bit) & 0x80) >> 7)
                                    | (((plane1 << bit) & 0x80) >> 6)
                                    | (((plane2 << bit) & 0x80) >> 5)
                                    | (((plane3 << bit) & 0x80) >> 4)]
                            );
                        }
                    }
                    dirty.write(offs, 0);
                }
            }
            copybitmap(bitmap, page, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_NONE, 0);
        }
    };

    /**
     * ************************************************************************
     */
    static char fetch_image_addr() {
        int addr = thief_coprocessor.u8_param[IMAGE_ADDR_LO] + 256 * thief_coprocessor.u8_param[IMAGE_ADDR_HI];
        /* auto-increment */
        thief_coprocessor.u8_param[IMAGE_ADDR_LO]++;
        if (thief_coprocessor.u8_param[IMAGE_ADDR_LO] == 0x00) {
            thief_coprocessor.u8_param[IMAGE_ADDR_HI]++;
        }
        return (char) addr;
    }

    public static WriteHandlerPtr thief_blit_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int i, offs, xoffset, dy;
            UBytePtr gfx_rom = memory_region(REGION_GFX1);
            /*UINT8*/
            int u8_x = thief_coprocessor.u8_param[SCREEN_XPOS] & 0xff;
            /*UINT8*/
            int u8_y = thief_coprocessor.u8_param[SCREEN_YPOS] & 0xff;
            /*UINT8*/
            int u8_width = thief_coprocessor.u8_param[BLIT_WIDTH] & 0xff;
            /*UINT8*/
            int u8_height = thief_coprocessor.u8_param[BLIT_HEIGHT] & 0xff;
            /*UINT8*/
            int u8_attributes = thief_coprocessor.u8_param[BLIT_ATTRIBUTES] & 0xff;

            /*UINT8*/
            int u8_old_data;
            int xor_blit = data;
            /* making the xor behavior selectable fixes score display,
			but causes minor glitches on the playfield */

            u8_x = (u8_x - u8_width * 8) & 0xFF;//x -= width*8;
            xoffset = u8_x & 7;

            if ((u8_attributes & 0x10) != 0) {
                u8_y = (u8_y + (7 - u8_height)) & 0xFF;//y += 7-height;
                dy = 1;
            } else {
                dy = -1;
            }
            u8_height = (u8_height + 1) & 0xFF;//height++;
            while (u8_height-- != 0) {
                for (i = 0; i <= u8_width; i++) {
                    int addr = fetch_image_addr();
                    if (addr < 0x2000) {
                        data = thief_coprocessor.image_ram.read(addr);
                    } else {
                        addr -= 0x2000;
                        if (addr < 0x2000 * 3) {
                            data = gfx_rom.read(addr);
                        }
                    }
                    offs = (u8_y * 32 + u8_x / 8 + i) & 0x1fff;
                    u8_old_data = thief_videoram_r.handler(offs) & 0xFF;
                    if (xor_blit != 0) {
                        thief_videoram_w.handler(offs, u8_old_data ^ (data >> xoffset));
                    } else {
                        thief_videoram_w.handler(offs,
                                (u8_old_data & (0xff00 >> xoffset)) | (data >> xoffset)
                        );
                    }
                    offs = (offs + 1) & 0x1fff;
                    u8_old_data = thief_videoram_r.handler(offs) & 0xFF;
                    if (xor_blit != 0) {
                        thief_videoram_w.handler(offs, u8_old_data ^ ((data << (8 - xoffset)) & 0xff));
                    } else {
                        thief_videoram_w.handler(offs,
                                (u8_old_data & (0xff >> xoffset)) | ((data << (8 - xoffset)) & 0xff)
                        );
                    }
                }
                u8_y = (u8_y + dy) & 0xFF;//u8_y+=dy;
            }
        }
    };

    public static ReadHandlerPtr thief_coprocessor_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            switch (offset) {
                case SCREEN_XPOS:
                /* xpos */
                case SCREEN_YPOS: /* ypos */ {
                    /* XLAT: given (x,y) coordinate, return byte address in videoram */
                    int addr = thief_coprocessor.u8_param[SCREEN_XPOS] + 256 * thief_coprocessor.u8_param[SCREEN_YPOS];
                    int result = 0xc000 | (addr >> 3);
                    return (offset == 0x03) ? (result >> 8) : (result & 0xff);
                }
                //break;

                case GFX_PORT: {
                    int addr = fetch_image_addr();
                    if (addr < 0x2000) {
                        return thief_coprocessor.image_ram.read(addr);
                    } else {
                        UBytePtr gfx_rom = memory_region(REGION_GFX1);
                        addr -= 0x2000;
                        if (addr < 0x6000) {
                            return gfx_rom.read(addr);
                        }
                    }
                }
                break;

                case BARL_PORT: {
                    /* return bitmask for addressed pixel */
                    int dx = thief_coprocessor.u8_param[SCREEN_XPOS] & 0x7;
                    if ((thief_coprocessor.u8_param[BLIT_ATTRIBUTES] & 0x01) != 0) {
                        return 0x01 << dx; // flipx
                    } else {
                        return 0x80 >> dx; // no flip
                    }
                }
                //break;
            }

            return thief_coprocessor.u8_param[offset];
        }
    };

    public static WriteHandlerPtr thief_coprocessor_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            switch (offset) {
                case GFX_PORT: {
                    int addr = fetch_image_addr();
                    if (addr < 0x2000) {
                        thief_coprocessor.image_ram.write(addr, data);
                    }
                }
                break;

                default:
                    thief_coprocessor.u8_param[offset] = data & 0xFF;
                    break;
            }
        }
    };
}
