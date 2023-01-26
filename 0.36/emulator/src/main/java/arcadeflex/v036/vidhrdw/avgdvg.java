/*
 * ported to v0.36
 */
/**
 * Changelog
 * =========
 * 25/01/2023 - shadow - 0.36 version (some artwork support is missing)
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import arcadeflex.v036.mame.artworkH.struct_artwork;
//mame imports
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.common.*;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static arcadeflex.v036.mame.timer.*;
import static arcadeflex.v036.mame.timerH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.avgdvgH.*;
import static arcadeflex.v036.vidhrdw.vector.*;
//common imports
import static common.libc.cstdlib.*;
//TODO
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;

public class avgdvg {

    public static final int VEC_SHIFT = 16;
    /* fixed for the moment */
    public static final int BRIGHTNESS = 12;
    /* for maximum brightness, use 16! */


 /* the screen is red above this Y coordinate */
    public static final int BZONE_TOP = 0x0050;
    public static final int MHAVOC_YWINDOW = 0x0048;

    static int vectorEngine = USE_DVG;
    static int flipword = 0;
    /* little/big endian issues */
    static int busy = 0;
    /* vector engine busy? */
    static int[] colorram = new int[16];
    /* colorram entries */

 /* These hold the X/Y coordinates the vector engine uses */
    static int width;
    static int height;
    static int xcenter;
    static int ycenter;
    static int xmin;
    static int xmax;
    static int ymin;
    static int ymax;

    public static int vector_updates;
    /* avgdvg_go_w()'s per Mame frame, should be 1 */

    static int vg_step = 0;
    /* single step the vector generator */
    static int total_length;/* length of all lines drawn in a frame */

 /* Use backdrop if present MLR OCT0598 */
    static struct_artwork backdrop = null;

    public static final int MAXSTACK = 8;
    /* Tempest needs more than 4     BW 210797 */

 /* AVG commands */
    public static final int VCTR = 0;
    public static final int HALT = 1;
    public static final int SVEC = 2;
    public static final int STAT = 3;
    public static final int CNTR = 4;
    public static final int JSRL = 5;
    public static final int RTSL = 6;
    public static final int JMPL = 7;
    public static final int SCAL = 8;

    /* DVG commands */
    public static final int DVCTR = 0x01;
    public static final int DLABS = 0x0a;
    public static final int DHALT = 0x0b;
    public static final int DJSRL = 0x0c;
    public static final int DRTSL = 0x0d;
    public static final int DJMPL = 0x0e;
    public static final int DSVEC = 0x0f;

    static int twos_comp_val(int num, int bits) {
        return ((num & (1 << (bits - 1))) != 0 ? (num | ~((1 << bits) - 1)) : (num & ((1 << bits) - 1)));
    }

    /* ASG 971210 -- added banks and modified the read macros to use them */
    public static final int BANK_BITS = 13;
    public static final int BANK_SIZE = (1 << BANK_BITS);
    public static final int NUM_BANKS = (0x4000 / BANK_SIZE);

    static int VECTORRAM(int offset) {
        return (vectorbank[(offset) >> BANK_BITS].read((offset) & (BANK_SIZE - 1)));
    }

    static UBytePtr[] vectorbank = new UBytePtr[NUM_BANKS];

    static int map_addr(int n) {
        return n << 1;
    }

    static int memrdwd(int offset) {
        return (VECTORRAM(offset) | (VECTORRAM(offset + 1) << 8));
    }

    /* The AVG used by Star Wars reads the bytes in the opposite order */
    static int memrdwd_flip(int offset) {
        return (VECTORRAM(offset + 1) | (VECTORRAM(offset) << 8));
    }

    public static void vector_timer(int deltax, int deltay) {
        deltax = Math.abs(deltax);
        deltay = Math.abs(deltay);
        total_length += Math.max(deltax, deltay) >> VEC_SHIFT;
    }

    public static void dvg_vector_timer(int scale) {
        total_length += scale;
    }

    static void dvg_generate_vector_list() {
        int pc;
        int sp;
        int[] stack = new int[MAXSTACK];

        int scale;
        int statz;

        int currentx, currenty;

        int done = 0;

        int firstwd;
        int secondwd = 0;
        /* Initialize to tease the compiler */
        int opcode;

        int x, y;
        int z, temp;
        int a;

        int deltax, deltay;

        vector_clear_list();
        pc = 0;
        sp = 0;
        scale = 0;
        statz = 0;

        currentx = 0;
        currenty = 0;

        while (done == 0) {
            firstwd = memrdwd(map_addr(pc));
            opcode = firstwd >> 12;
            pc++;
            if ((opcode >= 0 /* DVCTR */) && (opcode <= DLABS)) {
                secondwd = memrdwd(map_addr(pc));
                pc++;
            }

            switch (opcode) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                    y = firstwd & 0x03ff;
                    if ((firstwd & 0x400) != 0) {
                        y = -y;
                    }
                    x = secondwd & 0x3ff;
                    if ((secondwd & 0x400) != 0) {
                        x = -x;
                    }
                    z = secondwd >> 12;
                    temp = ((scale + opcode) & 0x0f);
                    if (temp > 9) {
                        temp = -1;
                    }
                    deltax = (x << VEC_SHIFT) >> (9 - temp);
                    /* ASG 080497 */
                    deltay = (y << VEC_SHIFT) >> (9 - temp);
                    /* ASG 080497 */
                    currentx += deltax;
                    currenty -= deltay;
                    dvg_vector_timer(temp);

                    /* ASG 080497, .ac JAN2498 - V.V */
                    if (translucency != 0) {
                        z = z * BRIGHTNESS;
                    } else if (z != 0) {
                        z = (z << 4) | 0x0f;
                    }
                    vector_add_point(currentx, currenty, colorram[1], z);

                    break;

                case DLABS:
                    x = twos_comp_val(secondwd, 12);
                    y = twos_comp_val(firstwd, 12);
                    scale = (secondwd >> 12);
                    currentx = ((x - xmin) << VEC_SHIFT);
                    /* ASG 080497 */
                    currenty = ((ymax - y) << VEC_SHIFT);
                    /* ASG 080497 */
                    break;

                case DHALT:
                    done = 1;
                    break;

                case DJSRL:
                    a = firstwd & 0x0fff;
                    stack[sp] = pc;
                    if (sp == (MAXSTACK - 1)) {
                        if (errorlog != null) {
                            fprintf(errorlog, "\n*** Vector generator stack overflow! ***\n");
                        }
                        done = 1;
                        sp = 0;
                    } else {
                        sp++;
                    }
                    pc = a;
                    break;

                case DRTSL:
                    if (sp == 0) {
                        if (errorlog != null) {
                            fprintf(errorlog, "\n*** Vector generator stack underflow! ***\n");
                        }
                        done = 1;
                        sp = MAXSTACK - 1;
                    } else {
                        sp--;
                    }
                    pc = stack[sp];
                    break;

                case DJMPL:
                    a = firstwd & 0x0fff;
                    pc = a;
                    break;

                case DSVEC:
                    y = firstwd & 0x0300;
                    if ((firstwd & 0x0400) != 0) {
                        y = -y;
                    }
                    x = (firstwd & 0x03) << 8;
                    if ((firstwd & 0x04) != 0) {
                        x = -x;
                    }
                    z = (firstwd >> 4) & 0x0f;
                    temp = 2 + ((firstwd >> 2) & 0x02) + ((firstwd >> 11) & 0x01);
                    temp = ((scale + temp) & 0x0f);
                    if (temp > 9) {
                        temp = -1;
                    }

                    deltax = (x << VEC_SHIFT) >> (9 - temp);
                    /* ASG 080497 */
                    deltay = (y << VEC_SHIFT) >> (9 - temp);
                    /* ASG 080497 */
                    currentx += deltax;
                    currenty -= deltay;
                    dvg_vector_timer(temp);

                    /* ASG 080497, .ac JAN2498 */
                    if (translucency != 0) {
                        z = z * BRIGHTNESS;
                    } else if (z != 0) {
                        z = (z << 4) | 0x0f;
                    }
                    vector_add_point(currentx, currenty, colorram[1], z);
                    break;

                default:
                    if (errorlog != null) {
                        fprintf(errorlog, "Unknown DVG opcode found\n");
                    }
                    done = 1;
            }
        }
    }

    static void avg_generate_vector_list() {

        int pc;
        int sp;
        int[] stack = new int[MAXSTACK];

        int scale;
        int statz = 0;
        int sparkle = 0;
        int xflip = 0;

        int color = 0;
        int bz_col = -1;
        /* Battle Zone color selection */
        int ywindow = -1;
        /* Major Havoc Y-Window */

        int currentx, currenty;
        int done = 0;

        int firstwd, secondwd;
        int opcode;

        int x, y, z = 0, b, l, d, a;

        int deltax, deltay;

        pc = 0;
        sp = 0;
        statz = 0;
        color = 0;

        if (flipword != 0) {
            firstwd = memrdwd_flip(map_addr(pc));
            secondwd = memrdwd_flip(map_addr(pc + 1));
        } else {
            firstwd = memrdwd(map_addr(pc));
            secondwd = memrdwd(map_addr(pc + 1));
        }
        if ((firstwd == 0) && (secondwd == 0)) {
            if (errorlog != null) {
                fprintf(errorlog, "VGO with zeroed vector memory\n");
            }
            return;
        }

        /* kludge to bypass Major Havoc's empty frames. BW 980216 */
        if (vectorEngine == USE_AVG_MHAVOC && firstwd == 0xafe2) {
            return;
        }

        scale = 0;
        /* ASG 080497 */
        currentx = xcenter;
        /* ASG 080497 */ /*.ac JAN2498 */
        currenty = ycenter;
        /* ASG 080497 */ /*.ac JAN2498 */

        vector_clear_list();

        while (done == 0) {

            if (flipword != 0) {
                firstwd = memrdwd_flip(map_addr(pc));
            } else {
                firstwd = memrdwd(map_addr(pc));
            }

            opcode = firstwd >> 13;
            pc++;
            if (opcode == VCTR) {
                if (flipword != 0) {
                    secondwd = memrdwd_flip(map_addr(pc));
                } else {
                    secondwd = memrdwd(map_addr(pc));
                }
                pc++;
            }
            if ((opcode == STAT) && ((firstwd & 0x1000) != 0)) {
                opcode = SCAL;
            }
            switch (opcode) {
                case VCTR:

                    if (vectorEngine == USE_AVG_QUANTUM) {
                        x = twos_comp_val(secondwd, 12);
                        y = twos_comp_val(firstwd, 12);
                    } else {
                        /* These work for all other games. */
                        x = twos_comp_val(secondwd, 13);
                        y = twos_comp_val(firstwd, 13);
                    }
                    z = (secondwd >> 12) & ~0x01;

                    /* z is the maximum DAC output, and      */
 /* the 8 bit value from STAT does some   */
 /* fine tuning. STATs of 128 should give */
 /* highest intensity. */
                    if (vectorEngine == USE_AVG_SWARS) {
                        if (translucency != 0) {
                            z = (statz * z) / 12;
                        } else {
                            z = (statz * z) >> 3;
                        }
                        if (z > 0xff) {
                            z = 0xff;
                        }
                    } else {
                        if (z == 2) {
                            z = statz;
                        }
                        if (translucency != 0) {
                            z = z * BRIGHTNESS;
                        } else if (z != 0) {
                            z = (z << 4) | 0x1f;
                        }
                    }

                    deltax = x * scale;
                    if (xflip != 0) {
                        deltax = -deltax;
                    }

                    deltay = y * scale;
                    currentx += deltax;
                    currenty -= deltay;
                    vector_timer(deltax, deltay);

                    if (sparkle != 0) {
                        color = rand() & 0x07;
                    }

                    if ((vectorEngine == USE_AVG_BZONE) && (bz_col != 0)) {
                        if (currenty < (BZONE_TOP << 16)) {
                            color = 4;
                        } else {
                            color = 2;
                        }
                    }

                    vector_add_point(currentx, currenty, colorram[color], z);

                    break;

                case SVEC:
                    x = twos_comp_val(firstwd, 5) << 1;
                    y = twos_comp_val(firstwd >> 8, 5) << 1;
                    z = ((firstwd >> 4) & 0x0e);

                    if (vectorEngine == USE_AVG_SWARS) {
                        if (translucency != 0) {
                            z = (statz * z) / 12;
                        } else {
                            z = (statz * z) >> 3;
                        }
                        if (z > 0xff) {
                            z = 0xff;
                        }
                    } else {
                        if (z == 2) {
                            z = statz;
                        }
                        if (translucency != 0) {
                            z = z * BRIGHTNESS;
                        } else if (z != 0) {
                            z = (z << 4) | 0x1f;
                        }
                    }

                    deltax = x * scale;
                    if (xflip != 0) {
                        deltax = -deltax;
                    }

                    deltay = y * scale;
                    currentx += deltax;
                    currenty -= deltay;
                    vector_timer(deltax, deltay);

                    if (sparkle != 0) {
                        color = rand() & 0x07;
                    }

                    vector_add_point(currentx, currenty, colorram[color], z);

                    break;

                case STAT:
                    if (vectorEngine == USE_AVG_SWARS) {
                        /* color code 0-7 stored in top 3 bits of `color' */
                        color = (char) ((firstwd & 0x0700) >> 8);
                        statz = (firstwd) & 0xff;
                    } else {
                        color = (firstwd) & 0x000f;
                        statz = (firstwd >> 4) & 0x000f;
                        if (vectorEngine == USE_AVG_TEMPEST) {
                            sparkle = (firstwd & 0x0800) != 0 ? 0 : 1;
                        }
                        if (vectorEngine == USE_AVG_MHAVOC) {
                            sparkle = (firstwd & 0x0800);
                            xflip = firstwd & 0x0400;
                            /* Bank switch the vector ROM for Major Havoc */
                            vectorbank[1] = new UBytePtr(memory_region(REGION_CPU1), 0x18000 + ((firstwd & 0x300) >> 8) * 0x2000);
                        }
                        if (vectorEngine == USE_AVG_BZONE) {
                            bz_col = color;
                            if (color == 0) {
                                vector_add_clip(xmin << VEC_SHIFT, BZONE_TOP << VEC_SHIFT, xmax << VEC_SHIFT, ymax << VEC_SHIFT);
                                color = 2;
                            } else {
                                vector_add_clip(xmin << VEC_SHIFT, ymin << VEC_SHIFT, xmax << VEC_SHIFT, ymax << VEC_SHIFT);
                            }
                        }
                    }
                    if (errorlog != null) {
                        fprintf(errorlog, "STAT: statz: %d color: %d", statz, color);
                    }
                    if (xflip != 0 || sparkle != 0) {
                        if (errorlog != null) {
                            fprintf(errorlog, "xflip: %02x  sparkle: %02x\n", xflip, sparkle);
                        }
                    }
                    break;

                case SCAL:
                    b = ((firstwd >> 8) & 0x07) + 8;
                    l = (~firstwd) & 0xff;
                    scale = (l << VEC_SHIFT) >> b;
                    /* ASG 080497 */

 /* Y-Window toggle for Major Havoc BW 980318 */
                    if (vectorEngine == USE_AVG_MHAVOC) {
                        if ((firstwd & 0x0800) != 0) {
                            if (errorlog != null) {
                                fprintf(errorlog, "CLIP %d\n", firstwd & 0x0800);
                            }
                            if (ywindow == 0) {
                                ywindow = 1;
                                vector_add_clip(xmin << VEC_SHIFT, MHAVOC_YWINDOW << VEC_SHIFT, xmax << VEC_SHIFT, ymax << VEC_SHIFT);
                            } else {
                                ywindow = 0;
                                vector_add_clip(xmin << VEC_SHIFT, ymin << VEC_SHIFT, xmax << VEC_SHIFT, ymax << VEC_SHIFT);
                            }
                        }
                    }
                    break;

                case CNTR:
                    d = firstwd & 0xff;
                    currentx = xcenter;
                    /* ASG 080497 */ /*.ac JAN2498 */
                    currenty = ycenter;
                    /* ASG 080497 */ /*.ac JAN2498 */
                    vector_add_point(currentx, currenty, 0, 0);
                    break;

                case RTSL:
                    if (sp == 0) {
                        if (errorlog != null) {
                            fprintf(errorlog, "\n*** Vector generator stack underflow! ***\n");
                        }
                        done = 1;
                        sp = MAXSTACK - 1;
                    } else {
                        sp--;
                    }

                    pc = stack[sp];
                    break;

                case HALT:
                    done = 1;
                    break;

                case JMPL:
                    a = firstwd & 0x1fff;
                    /* if a = 0x0000, treat as HALT */
                    if (a == 0x0000) {
                        done = 1;
                    } else {
                        pc = a;
                    }
                    break;

                case JSRL:
                    a = firstwd & 0x1fff;
                    /* if a = 0x0000, treat as HALT */
                    if (a == 0x0000) {
                        done = 1;
                    } else {
                        stack[sp] = pc;
                        if (sp == (MAXSTACK - 1)) {
                            if (errorlog != null) {
                                fprintf(errorlog, "\n*** Vector generator stack overflow! ***\n");
                            }
                            done = 1;
                            sp = 0;
                        } else {
                            sp++;
                        }

                        pc = a;
                    }
                    break;

                default:
                    if (errorlog != null) {
                        fprintf(errorlog, "internal error\n");
                    }
            }
        }
    }

    public static int avgdvg_done() {
        if (busy != 0) {
            return 0;
        } else {
            return 1;
        }
    }

    public static TimerCallbackHandlerPtr avgdvg_clr_busy = new TimerCallbackHandlerPtr() {
        public void handler(int dummy) {

            busy = 0;
        }
    };

    public static WriteHandlerPtr avgdvg_go = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            if (busy != 0) {
                return;
            }

            vector_updates++;
            total_length = 1;
            busy = 1;

            if (vectorEngine == USE_DVG) {
                dvg_generate_vector_list();
                timer_set(TIME_IN_NSEC(4500) * total_length, 1, avgdvg_clr_busy);
            } else {
                avg_generate_vector_list();
                if (total_length > 1) {
                    timer_set(TIME_IN_NSEC(1500) * total_length, 1, avgdvg_clr_busy);
                } /* this is for Major Havoc */ else {
                    vector_updates--;
                    busy = 0;
                }
            }
        }
    };

    public static WriteHandlerPtr avgdvg_reset = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            avgdvg_clr_busy.handler(0);
        }
    };

    static int avgdvg_init(int vgType) {
        int i;

        if (vectorram_size[0] == 0) {
            if (errorlog != null) {
                fprintf(errorlog, "Error: vectorram_size not initialized\n");
            }
            return 1;
        }

        /* ASG 971210 -- initialize the pages */
        for (i = 0; i < NUM_BANKS; i++) {
            vectorbank[i] = new UBytePtr(vectorram, (i << BANK_BITS));
        }
        if (vgType == USE_AVG_MHAVOC) {
            vectorbank[1] = new UBytePtr(memory_region(REGION_CPU1), 0x18000);
        }

        vectorEngine = vgType;
        if ((vectorEngine < AVGDVG_MIN) || (vectorEngine > AVGDVG_MAX)) {
            if (errorlog != null) {
                fprintf(errorlog, "Error: unknown Atari Vector Game Type\n");
            }
            return 1;
        }

        if (vectorEngine == USE_AVG_SWARS) {
            flipword = 1;
        } else {
            flipword = 0;
        }

        vg_step = 0;

        busy = 0;

        xmin = Machine.drv.visible_area.min_x;
        ymin = Machine.drv.visible_area.min_y;
        xmax = Machine.drv.visible_area.max_x;
        ymax = Machine.drv.visible_area.max_y;
        width = xmax - xmin;
        height = ymax - ymin;

        xcenter = ((xmax + xmin) / 2) << VEC_SHIFT;
        /*.ac JAN2498 */
        ycenter = ((ymax + ymin) / 2) << VEC_SHIFT;
        /*.ac JAN2498 */

        vector_set_shift(VEC_SHIFT);

        if (vector_vh_start.handler() != 0) {
            return 1;
        }

        return 0;
    }

    /*
	 * These functions initialise the colors for all atari games.
     */
    public static final int RED = 0x04;
    public static final int GREEN = 0x02;
    public static final int BLUE = 0x01;
    public static final int WHITE = RED | GREEN | BLUE;

    static void shade_fill(char[] palette, int rgb, int start_index, int end_index, int start_inten, int end_inten) {
        int i, inten, index_range, inten_range;

        index_range = end_index - start_index;
        inten_range = end_inten - start_inten;
        for (i = start_index; i <= end_index; i++) {
            inten = start_inten + (inten_range) * (i - start_index) / (index_range);
            palette[3 * i] = (rgb & RED) != 0 ? (char) (inten & 0xFF) : 0;
            palette[3 * i + 1] = (rgb & GREEN) != 0 ? (char) (inten & 0xFF) : 0;
            palette[3 * i + 2] = (rgb & BLUE) != 0 ? (char) (inten & 0xFF) : 0;
        }
    }

    public static final int VEC_PAL_WHITE = 1;
    public static final int VEC_PAL_AQUA = 2;
    public static final int VEC_PAL_BZONE = 3;
    public static final int VEC_PAL_MULTI = 4;
    public static final int VEC_PAL_SWARS = 5;
    public static final int VEC_PAL_ASTDELUX = 6;

    /* Helper function to construct the color palette for the Atari vector
     * games. DO NOT reference this function from the Gamedriver or
     * MachineDriver. Use "avg_init_palette_XXXXX" instead. */
    public static void avg_init_palette(int paltype, char[] palette, char[] colortable, UBytePtr color_prom) {
        int i, j, k;

        int trcl1[] = {0, 0, 2, 2, 1, 1};
        int trcl2[] = {1, 2, 0, 1, 0, 2};
        int trcl3[] = {2, 1, 1, 0, 2, 0};

        /* initialize the first 8 colors with the basic colors */
 /* Only these are selected by writes to the colorram. */
        for (i = 0; i < 8; i++) {
            palette[3 * i] = (i & RED) != 0 ? (char) 0xff : 0;
            palette[3 * i + 1] = (i & GREEN) != 0 ? (char) 0xff : 0;
            palette[3 * i + 2] = (i & BLUE) != 0 ? (char) 0xff : 0;
        }

        /* initialize the colorram */
        for (i = 0; i < 16; i++) {
            colorram[i] = i & 0x07;
        }

        /* fill the rest of the 256 color entries depending on the game */
        switch (paltype) {
            /* Black and White vector colors (Asteroids,Omega Race) .ac JAN2498 */
            case VEC_PAL_WHITE:
                shade_fill(palette, RED | GREEN | BLUE, 8, 128 + 8, 0, 255);
                colorram[1] = 7;
                /* BW games use only color 1 (== white) */
                break;

            /* Monochrome Aqua colors (Asteroids Deluxe,Red Baron) .ac JAN2498 */
            case VEC_PAL_ASTDELUX:
                /*TODO*///			/* Use backdrop if present MLR OCT0598 */
/*TODO*///			if ((backdrop=artwork_load("astdelux.png", 32, Machine->drv->total_colors-32))!=NULL)
/*TODO*///			{
/*TODO*///				shade_fill (palette, GREEN|BLUE, 8, 23, 1, 254);
/*TODO*///				/* Some more anti-aliasing colors. */
/*TODO*///				shade_fill (palette, GREEN|BLUE, 24, 31, 1, 254);
/*TODO*///				for (i=0; i<8; i++)
/*TODO*///					palette[(24+i)*3]=80;
/*TODO*///				memcpy (palette+3*backdrop->start_pen, backdrop->orig_palette,
/*TODO*///					3*backdrop->num_pens_used);
/*TODO*///			}
/*TODO*///			else
                shade_fill(palette, GREEN | BLUE, 8, 128 + 8, 1, 254);
                colorram[1] = 3;
                /* for Asteroids */
                break;

            case VEC_PAL_AQUA:
                shade_fill(palette, GREEN | BLUE, 8, 128 + 8, 1, 254);
                colorram[0] = 3;
                /* for Red Baron */
                break;

            /* Monochrome Green/Red vector colors (Battlezone) .ac JAN2498 */
            case VEC_PAL_BZONE:
                shade_fill(palette, RED, 8, 23, 1, 254);
                shade_fill(palette, GREEN, 24, 31, 1, 254);
                shade_fill(palette, WHITE, 32, 47, 1, 254);
                /* Use backdrop if present MLR OCT0598 */
 /*TODO*///			if ((backdrop=artwork_load("bzone.png", 48, Machine->drv->total_colors-48))!=NULL)
/*TODO*///				memcpy (palette+3*backdrop->start_pen, backdrop->orig_palette, 3*backdrop->num_pens_used);
                break;

            /* Colored games (Major Havoc, Star Wars, Tempest) .ac JAN2498 */
            case VEC_PAL_MULTI:
            case VEC_PAL_SWARS:
                /* put in 40 shades for red, blue and magenta */
                shade_fill(palette, RED, 8, 47, 10, 250);
                shade_fill(palette, BLUE, 48, 87, 10, 250);
                shade_fill(palette, RED | BLUE, 88, 127, 10, 250);

                /* put in 20 shades for yellow and green */
                shade_fill(palette, GREEN, 128, 147, 10, 250);
                shade_fill(palette, RED | GREEN, 148, 167, 10, 250);

                /* and 14 shades for cyan and white */
                shade_fill(palette, BLUE | GREEN, 168, 181, 10, 250);
                shade_fill(palette, WHITE, 182, 194, 10, 250);

                /* Fill in unused gaps with more anti-aliasing colors. */
 /* There are 60 slots available.           .ac JAN2498 */
                i = 195;
                for (j = 0; j < 6; j++) {
                    for (k = 7; k <= 16; k++) {
                        palette[3 * i + trcl1[j]] = (char) ((((256 * k) / 16) - 1) & 0xFF);
                        palette[3 * i + trcl2[j]] = (char) ((((128 * k) / 16) - 1) & 0xFF);
                        palette[3 * i + trcl3[j]] = 0;
                        i++;
                    }
                }
                break;
            default:
                if (errorlog != null) {
                    fprintf(errorlog, "Wrong palette type in avgdvg.c");
                }
                break;
        }
    }

    /* The functions referenced from gamedriver */
    public static VhConvertColorPromHandlerPtr avg_init_palette_white = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            avg_init_palette(VEC_PAL_WHITE, palette, colortable, color_prom);
        }
    };
    public static VhConvertColorPromHandlerPtr avg_init_palette_aqua = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            avg_init_palette(VEC_PAL_AQUA, palette, colortable, color_prom);
        }
    };
    public static VhConvertColorPromHandlerPtr avg_init_palette_bzone = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            avg_init_palette(VEC_PAL_BZONE, palette, colortable, color_prom);
        }
    };
    public static VhConvertColorPromHandlerPtr avg_init_palette_multi = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            avg_init_palette(VEC_PAL_MULTI, palette, colortable, color_prom);
        }
    };
    public static VhConvertColorPromHandlerPtr avg_init_palette_swars = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            avg_init_palette(VEC_PAL_SWARS, palette, colortable, color_prom);
        }
    };
    public static VhConvertColorPromHandlerPtr avg_init_palette_astdelux = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            avg_init_palette(VEC_PAL_ASTDELUX, palette, colortable, color_prom);
        }
    };


    /* If you want to use the next two functions, please make sure that you have
     * a fake GfxLayout, otherwise you'll crash */
    public static WriteHandlerPtr colorram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            colorram[offset & 0x0f] = data & 0x0f;
        }
    };

    /*
	 * Tempest, Major Havoc and Quantum select colors via a 16 byte colorram.
	 * What's more, they have a different ordering of the rgbi bits than the other
	 * color avg games.
	 * We need translation tables.
     */
    public static WriteHandlerPtr tempest_colorram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int trans[] = {7, 7, 3, 3, 6, 6, 2, 2, 5, 5, 1, 1, 4, 4, 0, 0};
            colorram_w.handler(offset, trans[data & 0x0f]);
        }
    };

    public static WriteHandlerPtr mhavoc_colorram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int trans[] = {7, 6, 5, 4, 7, 6, 5, 4, 3, 2, 1, 0, 3, 2, 1, 0};
            if (errorlog != null) {
                fprintf(errorlog, "colorram: %02x: %02x\n", offset, data);
            }
            colorram_w.handler(offset, trans[data & 0x0f]);
        }
    };

    public static WriteHandlerPtr quantum_colorram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* Notes on colors:
	offset:				color:			color (game):
	0 - score, some text		0 - black?
	1 - nothing?			1 - blue
	2 - nothing?			2 - green
	3 - Quantum, streaks		3 - cyan
	4 - text/part 1 player		4 - red
	5 - part 2 of player		5 - purple
	6 - nothing?			6 - yellow
	7 - part 3 of player		7 - white
	8 - stars			8 - black
	9 - nothing?			9 - blue
	10 - nothing?			10 - green
	11 - some text, 1up, like 3	11 - cyan
	12 - some text, like 4
	13 - nothing?			13 - purple
	14 - nothing?
	15 - nothing?
	
	1up should be blue
	score should be red
	high score - white? yellow?
	level # - green
             */

            int trans[] = {7/*white*/, 0, 3, 1/*blue*/, 2/*green*/, 5, 6, 4/*red*/,
                7/*white*/, 0, 3, 1/*blue*/, 2/*green*/, 5, 6, 4/*red*/};

            colorram_w.handler(offset >> 1, trans[data & 0x0f]);
        }
    };

    /**
     * *************************************************************************
     * Draw the game screen in the given osd_bitmap. Do NOT call
     * osd_update_display() from this function, it will be called by the main
     * emulation engine.
     * *************************************************************************
     */
    public static VhUpdateHandlerPtr avg_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {

            /*TODO*///if (backdrop)
            /*TODO*///	vector_vh_update_backdrop(bitmap, backdrop, full_refresh);
            /*TODO*///else
            vector_vh_update.handler(bitmap, full_refresh);
        }
    };
    public static VhUpdateHandlerPtr dvg_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            /*TODO*///if (backdrop)
            /*TODO*///	vector_vh_update_backdrop(bitmap, backdrop, full_refresh);
            /*TODO*///else
            vector_vh_update.handler(bitmap, full_refresh);
        }
    };

    public static VhStartHandlerPtr dvg_start = new VhStartHandlerPtr() {
        public int handler() {
            /*TODO*///	if (backdrop)
/*TODO*///	{
/*TODO*///		backdrop_refresh(backdrop);
/*TODO*///		backdrop_refresh_tables (backdrop);
/*TODO*///	}
            return avgdvg_init(USE_DVG);
        }
    };

    public static VhStartHandlerPtr avg_start = new VhStartHandlerPtr() {
        public int handler() {

            return avgdvg_init(USE_AVG);
        }
    };
    public static VhStartHandlerPtr avg_start_starwars = new VhStartHandlerPtr() {
        public int handler() {

            return avgdvg_init(USE_AVG_SWARS);
        }
    };
    public static VhStartHandlerPtr avg_start_tempest = new VhStartHandlerPtr() {
        public int handler() {

            return avgdvg_init(USE_AVG_TEMPEST);
        }
    };
    public static VhStartHandlerPtr avg_start_mhavoc = new VhStartHandlerPtr() {
        public int handler() {

            return avgdvg_init(USE_AVG_MHAVOC);
        }
    };
    public static VhStartHandlerPtr avg_start_bzone = new VhStartHandlerPtr() {
        public int handler() {
            /*TODO*///	if (backdrop)
/*TODO*///	{
/*TODO*///		backdrop_refresh(backdrop);
/*TODO*///		backdrop_refresh_tables (backdrop);
/*TODO*///	}
            return avgdvg_init(USE_AVG_BZONE);
        }
    };
    public static VhStartHandlerPtr avg_start_quantum = new VhStartHandlerPtr() {
        public int handler() {
            return avgdvg_init(USE_AVG_QUANTUM);
        }
    };
    public static VhStartHandlerPtr avg_start_redbaron = new VhStartHandlerPtr() {
        public int handler() {
            return avgdvg_init(USE_AVG_RBARON);
        }
    };

    public static VhStopHandlerPtr avg_stop = new VhStopHandlerPtr() {
        public void handler() {
            busy = 0;
            vector_clear_list();

            vector_vh_stop.handler();
            /*TOOD*///if (backdrop) artwork_free(backdrop);
            /*TODO*///backdrop = NULL;
        }
    };
    public static VhStopHandlerPtr dvg_stop = new VhStopHandlerPtr() {
        public void handler() {
            busy = 0;
            vector_clear_list();

            vector_vh_stop.handler();
            /*TOOD*///if (backdrop) artwork_free(backdrop);
            /*TODO*///backdrop = NULL;
        }
    };

}
