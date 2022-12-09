 /*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.vidhrdw.vector.*;


public class sega {

    public static final int VEC_SHIFT = 15;
    /* do not use a higher value. Values will overflow */

    static int width, height, cent_x, cent_y, min_x, min_y, max_x, max_y;
    static long[] sinTable, cosTable;
    static int intensity;

    public static void sega_generate_vector_list() {
        int deltax, deltay;
        int currentX, currentY;

        int vectorIndex;
        int symbolIndex;

        int rotate, scale;
        int attrib;

        int angle, length;
        int color;

        int draw;

        vector_clear_list();

        symbolIndex = 0;
        /* Reset vector PC to 0 */

 /*
		 * walk the symbol list until 'last symbol' set
         */

        do {
            draw = vectorram.read(symbolIndex++);

            if ((draw & 1) != 0) /* if symbol active */ {
                currentX = vectorram.read(symbolIndex + 0) | (vectorram.read(symbolIndex + 1) << 8);
                currentY = vectorram.read(symbolIndex + 2) | (vectorram.read(symbolIndex + 3) << 8);
                vectorIndex = vectorram.read(symbolIndex + 4) | (vectorram.read(symbolIndex + 5) << 8);
                rotate = vectorram.read(symbolIndex + 6) | (vectorram.read(symbolIndex + 7) << 8);
                scale = vectorram.read(symbolIndex + 8);

                currentX = ((currentX & 0x7ff) - min_x) << VEC_SHIFT;
                currentY = (max_y - (currentY & 0x7ff)) << VEC_SHIFT;
                vector_add_point(currentX, currentY, 0, 0);
                vectorIndex &= 0xfff;

                /* walk the vector list until 'last vector' bit */
 /* is set in attributes */
                do {
                    attrib = vectorram.read(vectorIndex + 0);
                    length = vectorram.read(vectorIndex + 1);
                    angle = vectorram.read(vectorIndex + 2) | (vectorram.read(vectorIndex + 3) << 8);

                    vectorIndex += 4;

                    /* calculate deltas based on len, angle(s), and scale factor */
                    angle = (angle + rotate) & 0x3ff;
                    deltax = (int) (sinTable[angle] * scale * length);
                    deltay = (int) (cosTable[angle] * scale * length);

                    currentX += deltax >> 7;
                    currentY -= deltay >> 7;

                    color = attrib & 0x7e;
                    if (((attrib & 1) != 0) && color != 0) {
                        if (translucency != 0) {
                            intensity = 0xa0; /* leave room for translucency */
                        } else {
                            intensity = 0xff;
                        }
                    } else {
                        intensity = 0;
                    }
                    vector_add_point(currentX, currentY, color, intensity);

                } while ((attrib & 0x80) == 0);
            }

            symbolIndex += 9;
            if (symbolIndex >= vectorram_size[0]) {
                break;
            }

        } while ((draw & 0x80) == 0);
    }
    /**
     * *************************************************************************
     *
     * The Sega vector games don't have a color PROM, it uses RGB values for the
     * vector guns. This routine sets up the color tables to simulate it.
     *
     **************************************************************************
     */

    public static VhConvertColorPromHandlerPtr sega_init_colors = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i, r, g, b;

            /* Bits are. Red: 6&5 (0x60), Green: 4&3 (0x18), Blue: 2&1 (0x06) */
            for (i = 0; i < 128; i += 2) {
                palette[3 * i] = (char) ((85 * ((i >> 5) & 0x3)) & 0xFF);
                palette[3 * i + 1] = (char) ((85 * ((i >> 3) & 0x3)) & 0xFF);
                palette[3 * i + 2] = (char) ((85 * ((i >> 1) & 0x3)) & 0xFF);
                /* Set the color table */
                colortable[i] = (char) i;
            }
            /*
		 * Fill in the holes with good anti-aliasing colors.  This is a very good
		 * range of colors based on the previous palette entries.     .ac JAN2498
             */
            i = 1;
            for (r = 0; r <= 6; r++) {
                for (g = 0; g <= 6; g++) {
                    for (b = 0; b <= 6; b++) {
                        if (((r | g | b) & 0x1) == 0) {
                            continue;
                        }
                        if ((g == 5 || g == 6) && (b == 1 || b == 2 || r == 1 || r == 2)) {
                            continue;
                        }
                        if ((g == 3 || g == 4) && (b == 1 || r == 1)) {
                            continue;
                        }
                        if ((b == 6 || r == 6) && (g == 1 || g == 2)) {
                            continue;
                        }
                        if ((r == 5) && (b == 1)) {
                            continue;
                        }
                        if ((b == 5) && (r == 1)) {
                            continue;
                        }
                        palette[3 * i] = (char) (((255 * r) / 6) & 0xFF);
                        palette[3 * i + 1] = (char) (((255 * g) / 6) & 0xFF);
                        palette[3 * i + 2] = (char) (((255 * b) / 6) & 0xFF);
                        colortable[i] = (char) i;
                        if (i < 128) {
                            i += 2;
                        } else {
                            i++;
                        }
                    }
                }
            }
            /* There are still 4 colors left, just going to put some grays in. */
            for (i = 252; i <= 255; i++) {
                palette[3 * i]
                        = palette[3 * i + 1]
                        = palette[3 * i + 2] = (char) ((107 + (42 * (i - 252))) & 0xFF);
            }
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartHandlerPtr sega_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            int i;

            if (vectorram_size[0] == 0) {
                return 1;
            }
            min_x = Machine.visible_area.min_x;
            min_y = Machine.visible_area.min_y;
            max_x = Machine.visible_area.max_x;
            max_y = Machine.visible_area.max_y;
            width = max_x - min_x;
            height = max_y - min_y;
            cent_x = (max_x + min_x) / 2;
            cent_y = (max_y + min_y) / 2;

            vector_set_shift(VEC_SHIFT);

            /* allocate memory for the sine and cosine lookup tables ASG 080697 */
            sinTable = new long[0x400 * 8];
            if (sinTable == null) {
                return 1;
            }
            cosTable = new long[0x400 * 8];
            if (cosTable == null) {
                sinTable = null;
                return 1;
            }

            /* generate the sine/cosine lookup tables */
            for (i = 0; i < 0x400; i++) {
                double angle = ((2. * Math.PI) / (double) 0x400) * (double) i;
                double temp;

                temp = Math.sin(angle);
                if (temp < 0) {
                    sinTable[i] = (long) (temp * (double) (1 << VEC_SHIFT) - 0.5);
                } else {
                    sinTable[i] = (long) (temp * (double) (1 << VEC_SHIFT) + 0.5);
                }

                temp = Math.cos(angle);
                if (temp < 0) {
                    cosTable[i] = (long) (temp * (double) (1 << VEC_SHIFT) - 0.5);
                } else {
                    cosTable[i] = (long) (temp * (double) (1 << VEC_SHIFT) + 0.5);
                }

            }

            return vector_vh_start.handler();
        }
    };

    /**
     * *************************************************************************
     *
     * Stop the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStopHandlerPtr sega_vh_stop = new VhStopHandlerPtr() {
        public void handler() {
            if (sinTable != null) {
                sinTable = null;
            }
            sinTable = null;
            if (cosTable != null) {
                cosTable = null;
            }
            cosTable = null;

            vector_vh_stop.handler();
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
    public static VhUpdateHandlerPtr sega_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            sega_generate_vector_list();
            vector_vh_screenrefresh.handler(bitmap, full_refresh);
        }
    };
}
