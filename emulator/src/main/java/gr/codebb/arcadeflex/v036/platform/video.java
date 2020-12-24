package gr.codebb.arcadeflex.v036.platform;

import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.platform.ticker.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.version.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.usrintrf.*;
import static gr.codebb.arcadeflex.v036.platform.blit.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v036.mame.input.*;
import static gr.codebb.arcadeflex.v036.platform.sound.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class video {

    public static software_gfx screen; //for our screen creation

    public static class RGB {

        char r, g, b;
    }
    /*TODO*////* function to make scanline mode */
    /*TODO*///Register *make_scanline_mode(Register *inreg,int entries);
    /*TODO*///
    /*TODO*////*15.75KHz SVGA driver (req. for 15.75KHz Arcade Monitor Modes)*/
    /*TODO*///SVGA15KHZDRIVER *SVGA15KHzdriver;
    /*TODO*///
    /*TODO*///
    /*TODO*////* from blit.c, for VGA triple buffering */
    /*TODO*///extern int xpage_size;
    /*TODO*///extern int no_xpages;
    /*TODO*///void unchain_vga(Register *pReg);
    /*TODO*///
    public static int warming_up;
    /*TODO*///
    /*TODO*////* tweak values for centering tweaked modes */
    /*TODO*///int center_x;
    /*TODO*///int center_y;
    /*TODO*///
    /*TODO*///BEGIN_GFX_DRIVER_LIST
    /*TODO*///	GFX_DRIVER_VGA
    /*TODO*///	GFX_DRIVER_VESA3
    /*TODO*///	GFX_DRIVER_VESA2L
    /*TODO*///	GFX_DRIVER_VESA2B
    /*TODO*///	GFX_DRIVER_VESA1
    /*TODO*///END_GFX_DRIVER_LIST
    /*TODO*///
    /*TODO*///BEGIN_COLOR_DEPTH_LIST
    /*TODO*///	COLOR_DEPTH_8
    /*TODO*///	COLOR_DEPTH_15
    /*TODO*///	COLOR_DEPTH_16
    /*TODO*///END_COLOR_DEPTH_LIST
    /*TODO*///
    public static final int BACKGROUND = 0;
    /*TODO*///
    /*TODO*///
    /*TODO*///dirtygrid grid1;
    /*TODO*///dirtygrid grid2;
    /*TODO*///char *dirty_old=grid1;
    /*TODO*///char *dirty_new=grid2;
    /*TODO*///
    /*TODO*///void scale_vectorgames(int gfx_width,int gfx_height,int *width,int *height);
    /*TODO*///
    /*TODO*///void center_mode(Register *pReg);
    /*TODO*///
    /*TODO*////* in msdos/sound.c */
    /*TODO*///int msdos_update_audio(void);
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*////* specialized update_screen functions defined in blit.c */
    /*TODO*///
    /*TODO*////* dirty mode 1 (VIDEO_SUPPORTS_DIRTY) */
    /*TODO*///void blitscreen_dirty1_vga(void);
    /*TODO*///void blitscreen_dirty1_unchained_vga(void);
    /*TODO*///void blitscreen_dirty1_vesa_1x_1x_8bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_1x_2x_8bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_1x_2xs_8bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_2x_1x_8bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_2x_2x_8bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_2x_2xs_8bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_2x_3x_8bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_2x_3xs_8bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_3x_1x_8bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_3x_2x_8bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_3x_2xs_8bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_3x_3x_8bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_3x_3xs_8bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_4x_2x_8bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_4x_2xs_8bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_4x_3x_8bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_4x_3xs_8bpp(void);
    /*TODO*///
    /*TODO*///void blitscreen_dirty1_vesa_1x_1x_16bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_1x_2x_16bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_1x_2xs_16bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_2x_1x_16bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_2x_2x_16bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_2x_2xs_16bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_3x_1x_16bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_3x_2x_16bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_3x_2xs_16bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_4x_2x_16bpp(void);
    /*TODO*///void blitscreen_dirty1_vesa_4x_2xs_16bpp(void);
    /*TODO*///
    /*TODO*///void blitscreen_dirty1_vesa_1x_1x_16bpp_palettized(void);
    /*TODO*///void blitscreen_dirty1_vesa_1x_2x_16bpp_palettized(void);
    /*TODO*///void blitscreen_dirty1_vesa_1x_2xs_16bpp_palettized(void);
    /*TODO*///void blitscreen_dirty1_vesa_2x_1x_16bpp_palettized(void);
    /*TODO*///void blitscreen_dirty1_vesa_2x_2x_16bpp_palettized(void);
    /*TODO*///void blitscreen_dirty1_vesa_2x_2xs_16bpp_palettized(void);
    /*TODO*///void blitscreen_dirty1_vesa_3x_1x_16bpp_palettized(void);
    /*TODO*///void blitscreen_dirty1_vesa_3x_2x_16bpp_palettized(void);
    /*TODO*///void blitscreen_dirty1_vesa_3x_2xs_16bpp_palettized(void);
    /*TODO*///void blitscreen_dirty1_vesa_4x_2x_16bpp_palettized(void);
    /*TODO*///void blitscreen_dirty1_vesa_4x_2xs_16bpp_palettized(void);
    /*TODO*///
    /*TODO*///
    /*TODO*////* dirty mode 0 (no osd_mark_dirty calls) */
    /*TODO*///void blitscreen_dirty0_vga(void);
    /*TODO*///void blitscreen_dirty0_unchained_vga(void);
    /*TODO*///void blitscreen_dirty0_vesa_1x_1x_8bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_1x_2x_8bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_1x_2xs_8bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_2x_1x_8bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_2x_2x_8bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_2x_2xs_8bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_2x_3x_8bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_2x_3xs_8bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_3x_1x_8bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_3x_2x_8bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_3x_2xs_8bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_3x_3x_8bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_3x_3xs_8bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_4x_2x_8bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_4x_2xs_8bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_4x_3x_8bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_4x_3xs_8bpp(void);
    /*TODO*///
    /*TODO*///void blitscreen_dirty0_vesa_1x_1x_16bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_1x_2x_16bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_1x_2xs_16bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_2x_1x_16bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_2x_2x_16bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_2x_2xs_16bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_3x_1x_16bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_3x_2x_16bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_3x_2xs_16bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_4x_2x_16bpp(void);
    /*TODO*///void blitscreen_dirty0_vesa_4x_2xs_16bpp(void);
    /*TODO*///
    /*TODO*///void blitscreen_dirty0_vesa_1x_1x_16bpp_palettized(void);
    /*TODO*///void blitscreen_dirty0_vesa_1x_2x_16bpp_palettized(void);
    /*TODO*///void blitscreen_dirty0_vesa_1x_2xs_16bpp_palettized(void);
    /*TODO*///void blitscreen_dirty0_vesa_2x_1x_16bpp_palettized(void);
    /*TODO*///void blitscreen_dirty0_vesa_2x_2x_16bpp_palettized(void);
    /*TODO*///void blitscreen_dirty0_vesa_2x_2xs_16bpp_palettized(void);
    /*TODO*///void blitscreen_dirty0_vesa_3x_1x_16bpp_palettized(void);
    /*TODO*///void blitscreen_dirty0_vesa_3x_2x_16bpp_palettized(void);
    /*TODO*///void blitscreen_dirty0_vesa_3x_2xs_16bpp_palettized(void);
    /*TODO*///void blitscreen_dirty0_vesa_4x_2x_16bpp_palettized(void);
    /*TODO*///void blitscreen_dirty0_vesa_4x_2xs_16bpp_palettized(void);
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///static void update_screen_dummy(void);
    /*TODO*///void (*update_screen)(void) = update_screen_dummy;
    /*TODO*///
    public static final int MAX_X_MULTIPLY = 4;
    public static final int MAX_Y_MULTIPLY = 3;
    public static final int MAX_X_MULTIPLY16 = 4;
    public static final int MAX_Y_MULTIPLY16 = 2;
    /*TODO*///
    /*TODO*///static void (*updaters8[MAX_X_MULTIPLY][MAX_Y_MULTIPLY][2][2])(void) =
    /*TODO*///{			/* 1 x 1 */
    /*TODO*///	{	{	{ blitscreen_dirty0_vesa_1x_1x_8bpp, blitscreen_dirty1_vesa_1x_1x_8bpp },
    /*TODO*///			{ blitscreen_dirty0_vesa_1x_1x_8bpp, blitscreen_dirty1_vesa_1x_1x_8bpp }
    /*TODO*///		},	/* 1 x 2 */
    /*TODO*///		{	{ blitscreen_dirty0_vesa_1x_2x_8bpp,  blitscreen_dirty1_vesa_1x_2x_8bpp },
    /*TODO*///			{ blitscreen_dirty0_vesa_1x_2xs_8bpp, blitscreen_dirty1_vesa_1x_2xs_8bpp }
    /*TODO*///		},	/* 1 x 3 */
    /*TODO*///		{	{ update_screen_dummy, update_screen_dummy },
    /*TODO*///			{ update_screen_dummy, update_screen_dummy },
    /*TODO*///		}
    /*TODO*///	},		/* 2 x 1 */
    /*TODO*///	{	{	{ blitscreen_dirty0_vesa_2x_1x_8bpp, blitscreen_dirty1_vesa_2x_1x_8bpp },
    /*TODO*///			{ blitscreen_dirty0_vesa_2x_1x_8bpp, blitscreen_dirty1_vesa_2x_1x_8bpp }
    /*TODO*///		},	/* 2 x 2 */
    /*TODO*///		{	{ blitscreen_dirty0_vesa_2x_2x_8bpp,  blitscreen_dirty1_vesa_2x_2x_8bpp },
    /*TODO*///			{ blitscreen_dirty0_vesa_2x_2xs_8bpp, blitscreen_dirty1_vesa_2x_2xs_8bpp }
    /*TODO*///		},	/* 2 x 3 */
    /*TODO*///		{	{ blitscreen_dirty0_vesa_2x_3x_8bpp,  blitscreen_dirty1_vesa_2x_3x_8bpp },
    /*TODO*///			{ blitscreen_dirty0_vesa_2x_3xs_8bpp, blitscreen_dirty1_vesa_2x_3xs_8bpp }
    /*TODO*///		}
    /*TODO*///	},		/* 3 x 1 */
    /*TODO*///	{	{	{ blitscreen_dirty0_vesa_3x_1x_8bpp,  blitscreen_dirty1_vesa_3x_1x_8bpp },
    /*TODO*///			{ update_screen_dummy, update_screen_dummy }
    /*TODO*///		},	/* 3 x 2 */
    /*TODO*///		{	{ blitscreen_dirty0_vesa_3x_2x_8bpp,  blitscreen_dirty1_vesa_3x_2x_8bpp },
    /*TODO*///			{ blitscreen_dirty0_vesa_3x_2xs_8bpp, blitscreen_dirty1_vesa_3x_2xs_8bpp }
    /*TODO*///		},	/* 3 x 3 */
    /*TODO*///		{	{ blitscreen_dirty0_vesa_3x_3x_8bpp,  blitscreen_dirty1_vesa_3x_3x_8bpp },
    /*TODO*///			{ blitscreen_dirty0_vesa_3x_3xs_8bpp, blitscreen_dirty1_vesa_3x_3xs_8bpp }
    /*TODO*///		}
    /*TODO*///	},		/* 4 x 1 */
    /*TODO*///	{	{	{ update_screen_dummy, update_screen_dummy },
    /*TODO*///			{ update_screen_dummy, update_screen_dummy }
    /*TODO*///		},	/* 4 x 2 */
    /*TODO*///		{	{ blitscreen_dirty0_vesa_4x_2x_8bpp,  blitscreen_dirty1_vesa_4x_2x_8bpp },
    /*TODO*///			{ blitscreen_dirty0_vesa_4x_2xs_8bpp, blitscreen_dirty1_vesa_4x_2xs_8bpp }
    /*TODO*///		},	/* 4 x 3 */
    /*TODO*///		{	{ blitscreen_dirty0_vesa_4x_3x_8bpp,  blitscreen_dirty1_vesa_4x_3x_8bpp },
    /*TODO*///			{ blitscreen_dirty0_vesa_4x_3xs_8bpp, blitscreen_dirty1_vesa_4x_3xs_8bpp }
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///};
    /*TODO*///
    /*TODO*///static void (*updaters16[MAX_X_MULTIPLY16][MAX_Y_MULTIPLY16][2][2])(void) =
    /*TODO*///{				/* 1 x 1 */
    /*TODO*///	{	{	{ blitscreen_dirty0_vesa_1x_1x_16bpp, blitscreen_dirty1_vesa_1x_1x_16bpp },
    /*TODO*///			{ blitscreen_dirty0_vesa_1x_1x_16bpp, blitscreen_dirty1_vesa_1x_1x_16bpp }
    /*TODO*///		},	/* 1 x 2 */
    /*TODO*///		{	{ blitscreen_dirty0_vesa_1x_2x_16bpp,  blitscreen_dirty1_vesa_1x_2x_16bpp },
    /*TODO*///			{ blitscreen_dirty0_vesa_1x_2xs_16bpp, blitscreen_dirty1_vesa_1x_2xs_16bpp }
    /*TODO*///		}
    /*TODO*///	},		/* 2 x 1 */
    /*TODO*///	{	{	{ blitscreen_dirty0_vesa_2x_1x_16bpp,  blitscreen_dirty1_vesa_2x_1x_16bpp },
    /*TODO*///			{ blitscreen_dirty0_vesa_2x_1x_16bpp,  blitscreen_dirty1_vesa_2x_1x_16bpp }
    /*TODO*///		},	/* 2 x 2 */
    /*TODO*///		{	{ blitscreen_dirty0_vesa_2x_2x_16bpp,  blitscreen_dirty1_vesa_2x_2x_16bpp },
    /*TODO*///			{ blitscreen_dirty0_vesa_2x_2xs_16bpp, blitscreen_dirty1_vesa_2x_2xs_16bpp }
    /*TODO*///		}
    /*TODO*///	},		/* 3 x 1 */
    /*TODO*///	{	{	{ blitscreen_dirty0_vesa_3x_1x_16bpp, blitscreen_dirty1_vesa_3x_1x_16bpp },
    /*TODO*///			{ update_screen_dummy, update_screen_dummy }
    /*TODO*///		},	/* 3 x 2 */
    /*TODO*///		{	{ blitscreen_dirty0_vesa_3x_2x_16bpp, blitscreen_dirty1_vesa_3x_2x_16bpp },
    /*TODO*///			{ blitscreen_dirty0_vesa_3x_2xs_16bpp, blitscreen_dirty1_vesa_3x_2xs_16bpp }
    /*TODO*///		}
    /*TODO*///	},		/* 4 x 1 */
    /*TODO*///	{	{	{ update_screen_dummy, update_screen_dummy },
    /*TODO*///			{ update_screen_dummy, update_screen_dummy }
    /*TODO*///		},	/* 4 x 2 */
    /*TODO*///		{	{ blitscreen_dirty0_vesa_4x_2x_16bpp,  blitscreen_dirty1_vesa_4x_2x_16bpp },
    /*TODO*///			{ blitscreen_dirty0_vesa_4x_2xs_16bpp, blitscreen_dirty1_vesa_4x_2xs_16bpp }
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///};
    /*TODO*///
    /*TODO*///static void (*updaters16_palettized[MAX_X_MULTIPLY16][MAX_Y_MULTIPLY16][2][2])(void) =
    /*TODO*///{				/* 1 x 1 */
    /*TODO*///	{	{	{ blitscreen_dirty0_vesa_1x_1x_16bpp_palettized, blitscreen_dirty1_vesa_1x_1x_16bpp_palettized },
    /*TODO*///			{ blitscreen_dirty0_vesa_1x_1x_16bpp_palettized, blitscreen_dirty1_vesa_1x_1x_16bpp_palettized }
    /*TODO*///		},	/* 1 x 2 */
    /*TODO*///		{	{ blitscreen_dirty0_vesa_1x_2x_16bpp_palettized,  blitscreen_dirty1_vesa_1x_2x_16bpp_palettized },
    /*TODO*///			{ blitscreen_dirty0_vesa_1x_2xs_16bpp_palettized, blitscreen_dirty1_vesa_1x_2xs_16bpp_palettized }
    /*TODO*///		}
    /*TODO*///	},		/* 2 x 1 */
    /*TODO*///	{	{	{ blitscreen_dirty0_vesa_2x_1x_16bpp_palettized,  blitscreen_dirty1_vesa_2x_1x_16bpp_palettized },
    /*TODO*///			{ blitscreen_dirty0_vesa_2x_1x_16bpp_palettized,  blitscreen_dirty1_vesa_2x_1x_16bpp_palettized }
    /*TODO*///		},	/* 2 x 2 */
    /*TODO*///		{	{ blitscreen_dirty0_vesa_2x_2x_16bpp_palettized,  blitscreen_dirty1_vesa_2x_2x_16bpp_palettized },
    /*TODO*///			{ blitscreen_dirty0_vesa_2x_2xs_16bpp_palettized, blitscreen_dirty1_vesa_2x_2xs_16bpp_palettized }
    /*TODO*///		}
    /*TODO*///	},		/* 3 x 1 */
    /*TODO*///	{	{	{ blitscreen_dirty0_vesa_3x_1x_16bpp_palettized, blitscreen_dirty1_vesa_3x_1x_16bpp_palettized },
    /*TODO*///			{ update_screen_dummy, update_screen_dummy }
    /*TODO*///		},	/* 3 x 2 */
    /*TODO*///		{	{ blitscreen_dirty0_vesa_3x_2x_16bpp_palettized, blitscreen_dirty1_vesa_3x_2x_16bpp_palettized },
    /*TODO*///			{ blitscreen_dirty0_vesa_3x_2xs_16bpp_palettized, blitscreen_dirty1_vesa_3x_2xs_16bpp_palettized }
    /*TODO*///		}
    /*TODO*///	},		/* 4 x 1 */
    /*TODO*///	{	{	{ update_screen_dummy, update_screen_dummy },
    /*TODO*///			{ update_screen_dummy, update_screen_dummy }
    /*TODO*///		},	/* 4 x 2 */
    /*TODO*///		{	{ blitscreen_dirty0_vesa_4x_2x_16bpp_palettized,  blitscreen_dirty1_vesa_4x_2x_16bpp_palettized },
    /*TODO*///			{ blitscreen_dirty0_vesa_4x_2xs_16bpp_palettized, blitscreen_dirty1_vesa_4x_2xs_16bpp_palettized }
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///};
    /*TODO*///
    public static osd_bitmap scrbitmap;
    public static int modifiable_palette;
    public static int screen_colors;
    public static UBytePtr current_palette;
    public static /*unsigned int * */ int[] dirtycolor;
    public static int dirtypalette;
    public static int dirty_bright;
    public static int[] bright_lookup = new int[256];
    /*TODO*///extern unsigned int doublepixel[256];
    /*TODO*///extern unsigned int quadpixel[256]; /* for quadring pixels */
    /*TODO*///extern UINT32 *palette_16bit_lookup;
    /*TODO*///
    public static int frameskip, autoframeskip;
    public static final int FRAMESKIP_LEVELS = 12;
    /*TODO*///
    /*TODO*////* type of monitor output- */
    /*TODO*////* Standard PC, NTSC, PAL or Arcade */
    /*TODO*///int monitor_type;
    /*TODO*///
    /*TODO*///int vgafreq;
    /*TODO*///int always_synced;
    static int video_sync;
    /*TODO*///int wait_vsync;
    /*TODO*///int use_triplebuf;
    /*TODO*///int triplebuf_pos,triplebuf_page_width;
    public static int vsync_frame_rate;
    public static int skiplines;
    public static int skipcolumns;
    public static int scanlines;
    public static int stretch;
    /*TODO*///int use_mmx;
    /*TODO*///int mmxlfb;
    /*TODO*///int use_tweaked;
    public static int use_vesa;
    public static int use_dirty;
    public static float osd_gamma_correction = 1.0f;
    public static int brightness;
    public static float brightness_paused_adjust;
    /*TODO*///char *resolution;
    /*TODO*///char *mode_desc;
    /*TODO*///int gfx_mode;
    public static int gfx_width;
    public static int gfx_height;
    /*TODO*///
    /*TODO*///
    /*TODO*////*new 'half' flag (req. for 15.75KHz Arcade Monitor Modes)*/
    /*TODO*///int half_yres=0;
    /*TODO*////* indicates unchained video mode (req. for 15.75KHz Arcade Monitor Modes)*/
    /*TODO*///int unchained;
    /*TODO*////* flags for lowscanrate modes */
    /*TODO*///int scanrate15KHz;
    /*TODO*///
    public static int auto_resolution;
    public static int viswidth;
    public static int visheight;
    public static int skiplinesmax;
    public static int skipcolumnsmax;
    public static int skiplinesmin;
    public static int skipcolumnsmin;

    public static int vector_game;
    /*TODO*///
    /*TODO*///static Register *reg = 0;       /* for VGA modes */
    /*TODO*///static int reglen = 0;  /* for VGA modes */
    /*TODO*///static int videofreq;   /* for VGA modes */
    /*TODO*///
    public static int gfx_xoffset;
    public static int gfx_yoffset;
    public static int gfx_display_lines;
    public static int gfx_display_columns;
    public static int xmultiply, ymultiply;
    public static int throttle = 1;
    /* toggled by F10 */

 /*TODO*///static int gone_to_gfx_mode;
    public static int frameskip_counter;
    public static int frames_displayed;
    public static long start_time, end_time;
    /* to calculate fps average on exit */

    public static final int FRAMES_TO_SKIP = 20;
    /* skip the first few frames from the FPS calculation */
 /*TODO*///							/* to avoid counting the copyright and info screens */
    /*TODO*///
    /*TODO*///unsigned char tw224x288_h, tw224x288_v;
    /*TODO*///unsigned char tw240x256_h, tw240x256_v;
    /*TODO*///unsigned char tw256x240_h, tw256x240_v;
    /*TODO*///unsigned char tw256x256_h, tw256x256_v;
    /*TODO*///unsigned char tw256x256_hor_h, tw256x256_hor_v;
    /*TODO*///unsigned char tw288x224_h, tw288x224_v;
    /*TODO*///unsigned char tw240x320_h, tw240x320_v;
    /*TODO*///unsigned char tw320x240_h, tw320x240_v;
    /*TODO*///unsigned char tw336x240_h, tw336x240_v;
    /*TODO*///unsigned char tw384x224_h, tw384x224_v;
    /*TODO*///unsigned char tw384x240_h, tw384x240_v;
    /*TODO*///unsigned char tw384x256_h, tw384x256_v;
    /*TODO*///
    /*TODO*///
    /*TODO*///struct vga_tweak { int x, y; Register *reg; int reglen; int syncvgafreq; int unchained; int vertical_mode; };
    /*TODO*///struct vga_tweak vga_tweaked[] = {
    /*TODO*///	{ 240, 256, scr240x256, sizeof(scr240x256)/sizeof(Register),  1, 0, 1 },
    /*TODO*///	{ 256, 240, scr256x240, sizeof(scr256x240)/sizeof(Register),  0, 0, 0 },
    /*TODO*///	{ 256, 256, scr256x256, sizeof(scr256x256)/sizeof(Register),  1, 0, 1 },
    /*TODO*///	{ 256, 256, scr256x256hor, sizeof(scr256x256hor)/sizeof(Register),  0, 0, 0 },
    /*TODO*///	{ 224, 288, scr224x288, sizeof(scr224x288)/sizeof(Register),  1, 0, 1 },
    /*TODO*///	{ 288, 224, scr288x224, sizeof(scr288x224)/sizeof(Register),  0, 0, 0 },
    /*TODO*///	{ 240, 320, scr240x320, sizeof(scr240x320)/sizeof(Register),  1, 1, 1 },
    /*TODO*///	{ 320, 240, scr320x240, sizeof(scr320x240)/sizeof(Register),  0, 1, 0 },
    /*TODO*///	{ 336, 240, scr336x240, sizeof(scr336x240)/sizeof(Register),  0, 1, 0 },
    /*TODO*///	{ 384, 224, scr384x224, sizeof(scr384x224)/sizeof(Register),  1, 1, 0 },
    /*TODO*///	{ 384, 240, scr384x240, sizeof(scr384x240)/sizeof(Register),  1, 1, 0 },
    /*TODO*///	{ 384, 256, scr384x256, sizeof(scr384x256)/sizeof(Register),  1, 1, 0 },
    /*TODO*///	{ 0, 0 }
    /*TODO*///};
    /*TODO*///struct mode_adjust  {int x, y; unsigned char *hadjust; unsigned char *vadjust; int vertical_mode; };
    /*TODO*///
    /*TODO*////* horizontal and vertical total tweak values for above modes */
    /*TODO*///struct mode_adjust  pc_adjust[] = {
    /*TODO*///	{ 240, 256, &tw240x256_h, &tw240x256_v, 1 },
    /*TODO*///	{ 256, 240, &tw256x240_h, &tw256x240_v, 0 },
    /*TODO*///	{ 256, 256, &tw256x256_hor_h, &tw256x256_hor_v, 0 },
    /*TODO*///	{ 256, 256, &tw256x256_h, &tw256x256_v, 1 },
    /*TODO*///	{ 224, 288, &tw224x288_h, &tw224x288_v, 1 },
    /*TODO*///	{ 288, 224, &tw288x224_h, &tw288x224_v, 0 },
    /*TODO*///	{ 240, 320, &tw240x320_h, &tw240x320_v, 1 },
    /*TODO*///	{ 320, 240, &tw320x240_h, &tw320x240_v, 0 },
    /*TODO*///	{ 336, 240, &tw336x240_h, &tw336x240_v, 0 },
    /*TODO*///	{ 384, 224, &tw384x224_h, &tw384x224_v, 0 },
    /*TODO*///	{ 384, 240, &tw384x240_h, &tw384x240_v, 0 },
    /*TODO*///	{ 384, 256, &tw384x256_h, &tw384x256_v, 0 },
    /*TODO*///	{ 0, 0 }
    /*TODO*///};
    /*TODO*///
    /*TODO*////* Tweak values for arcade/ntsc/pal modes */
    /*TODO*///unsigned char tw224x288arc_h, tw224x288arc_v, tw288x224arc_h, tw288x224arc_v;
    /*TODO*///unsigned char tw256x240arc_h, tw256x240arc_v, tw256x256arc_h, tw256x256arc_v;
    /*TODO*///unsigned char tw320x240arc_h, tw320x240arc_v, tw320x256arc_h, tw320x256arc_v;
    /*TODO*///unsigned char tw352x240arc_h, tw352x240arc_v, tw352x256arc_h, tw352x256arc_v;
    /*TODO*///unsigned char tw368x224arc_h, tw368x224arc_v;
    /*TODO*///unsigned char tw368x240arc_h, tw368x240arc_v, tw368x256arc_h, tw368x256arc_v;
    /*TODO*///unsigned char tw512x224arc_h, tw512x224arc_v, tw512x256arc_h, tw512x256arc_v;
    /*TODO*///unsigned char tw512x448arc_h, tw512x448arc_v, tw512x512arc_h, tw512x512arc_v;
    /*TODO*///unsigned char tw640x480arc_h, tw640x480arc_v;
    /*TODO*///
    /*TODO*////* 15.75KHz Modes */
    /*TODO*///struct vga_15KHz_tweak { int x, y; Register *reg; int reglen;
    /*TODO*///			  int syncvgafreq; int vesa; int ntsc;
    /*TODO*///			  int half_yres; int matchx; };
    /*TODO*///struct vga_15KHz_tweak arcade_tweaked[] = {
    /*TODO*///	{ 224, 288, scr224x288_15KHz, sizeof(scr224x288_15KHz)/sizeof(Register), 0, 0, 0, 0, 224 },
    /*TODO*///	{ 256, 240, scr256x240_15KHz, sizeof(scr256x240_15KHz)/sizeof(Register), 0, 0, 1, 0, 256 },
    /*TODO*///	{ 256, 256, scr256x256_15KHz, sizeof(scr256x256_15KHz)/sizeof(Register), 0, 0, 0, 0, 256 },
    /*TODO*///	{ 288, 224, scr288x224_15KHz, sizeof(scr288x224_15KHz)/sizeof(Register), 0, 0, 1, 0, 288 },
    /*TODO*///	{ 320, 240, scr320x240_15KHz, sizeof(scr320x240_15KHz)/sizeof(Register), 1, 0, 1, 0, 320 },
    /*TODO*///	{ 320, 256, scr320x256_15KHz, sizeof(scr320x256_15KHz)/sizeof(Register), 1, 0, 0, 0, 320 },
    /*TODO*///	{ 352, 240, scr352x240_15KHz, sizeof(scr352x240_15KHz)/sizeof(Register), 1, 0, 1, 0, 352 },
    /*TODO*///	{ 352, 256, scr352x256_15KHz, sizeof(scr352x256_15KHz)/sizeof(Register), 1, 0, 0, 0, 352 },
    /*TODO*////* force 384 games to match to 368 modes - the standard VGA clock speeds mean we can't go as wide as 384 */
    /*TODO*///	{ 368, 224, scr368x224_15KHz, sizeof(scr368x224_15KHz)/sizeof(Register), 1, 0, 1, 0, 384 },
    /*TODO*////* all VGA modes from now on are too big for triple buffering */
    /*TODO*///	{ 368, 240, scr368x240_15KHz, sizeof(scr368x240_15KHz)/sizeof(Register), 1, 0, 1, 0, 384 },
    /*TODO*///	{ 368, 256, scr368x256_15KHz, sizeof(scr368x256_15KHz)/sizeof(Register), 1, 0, 0, 0, 384 },
    /*TODO*////* double monitor modes */
    /*TODO*///	{ 512, 224, scr512x224_15KHz, sizeof(scr512x224_15KHz)/sizeof(Register), 0, 0, 1, 0, 512 },
    /*TODO*///	{ 512, 256, scr512x256_15KHz, sizeof(scr512x256_15KHz)/sizeof(Register), 0, 0, 0, 0, 512 },
    /*TODO*////* SVGA Mode (VGA register array not used) */
    /*TODO*///	{ 640, 480, NULL            , 0                                        , 0, 1, 1, 0, 640 },
    /*TODO*////* 'half y' VGA modes, used to fake hires if 'tweaked' is on */
    /*TODO*///	{ 512, 448, scr512x224_15KHz, sizeof(scr512x224_15KHz)/sizeof(Register), 0, 0, 1, 1, 512 },
    /*TODO*///	{ 512, 512, scr512x256_15KHz, sizeof(scr512x256_15KHz)/sizeof(Register), 0, 0, 0, 1, 512 },
    /*TODO*///	{ 0, 0 }
    /*TODO*///};
    /*TODO*///
    /*TODO*////* horizontal and vertical total tweak values for above modes */
    /*TODO*///struct mode_adjust  arcade_adjust[] = {
    /*TODO*///	{ 224, 288, &tw224x288arc_h, &tw224x288arc_v, 1 },
    /*TODO*///	{ 256, 240, &tw256x240arc_h, &tw256x240arc_v, 0 },
    /*TODO*///	{ 256, 256, &tw256x256arc_h, &tw256x256arc_v, 0 },
    /*TODO*///	{ 288, 224, &tw288x224arc_h, &tw288x224arc_v, 0 },
    /*TODO*///	{ 320, 240, &tw320x240arc_h, &tw320x240arc_v, 0 },
    /*TODO*///	{ 352, 240, &tw352x240arc_h, &tw352x240arc_v, 0 },
    /*TODO*///	{ 352, 256, &tw352x256arc_h, &tw352x256arc_v, 0 },
    /*TODO*///	{ 368, 224, &tw368x224arc_h, &tw368x224arc_v, 0 },
    /*TODO*///	{ 368, 240, &tw368x240arc_h, &tw368x240arc_v, 0 },
    /*TODO*///	{ 368, 256, &tw368x256arc_h, &tw368x256arc_v, 0 },
    /*TODO*///	{ 512, 224, &tw512x224arc_h, &tw512x224arc_v, 0 },
    /*TODO*///	{ 512, 256, &tw512x256arc_h, &tw512x256arc_v, 0 },
    /*TODO*///	{ 512, 448, &tw512x224arc_h, &tw512x224arc_v, 0 },
    /*TODO*///	{ 512, 512, &tw512x256arc_h, &tw512x256arc_v, 0 },
    /*TODO*///	{ 0, 0 }
    /*TODO*///};

    /* Create a bitmap. Also calls osd_clearbitmap() to appropriately initialize */
 /* it to the background color. */
 /* VERY IMPORTANT: the function must allocate also a "safety area" 16 pixels wide all */
 /* around the bitmap. This is required because, for performance reasons, some graphic */
 /* routines don't clip at boundaries of the bitmap. */
    public static final int safety = 0; // TEMP HACK safety should be 16 (NICKBLAME FIX THIS!)

    public static osd_bitmap osd_new_bitmap(int width, int height, int depth) {

        osd_bitmap bitmap;

        if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
            int temp;

            temp = width;
            width = height;
            height = temp;
        }

        if ((bitmap = new osd_bitmap()) != null) {
            int i, rowlen, rdwidth;
            UBytePtr bm;

            if (depth != 8 && depth != 16) {
                depth = 8;
            }

            bitmap.depth = depth;
            bitmap.width = width;
            bitmap.height = height;

            rdwidth = (width + 7) & ~7;
            /* round width to a quadword */

            if (depth == 16) {
                rowlen = 2 * (rdwidth + 2 * safety);
            } else {
                rowlen = (rdwidth + 2 * safety);
            }

            if ((bm = new UBytePtr((height + 2 * safety) * rowlen)) == null) {
                bitmap = null;
                return null;
            }

            /* clear ALL bitmap, including safety area, to avoid garbage on right */
 /* side of screen is width is not a multiple of 4 */
            memset(bm, 0, (height + 2 * safety) * rowlen);
            if ((bitmap.line = new UBytePtr[((height + 2 * safety))]) == null) {
                bm = null;
                bitmap = null;
                return null;
            }

            for (i = bitmap.ptrPos; i < height + 2 * safety; i++) {
                if (depth == 16) {
                    //bitmap->line[i] = &bm[i * rowlen + 2*safety];
                    bitmap.line[i] = new UBytePtr(bm, (i * rowlen + 2 * safety));

                } else {
                    //bitmap->line[i] = &bm[i * rowlen + safety];
                    bitmap.line[i] = new UBytePtr(bm, (i * rowlen + safety));

                }
                //bitmap.line[i].base += safety;
            }
            //bitmap.line += safety; //moved above TODO check if it's correct (shadow)
            bitmap.ptrPos += safety;
            bitmap._private = bm;

            osd_clearbitmap(bitmap);
        }

        return bitmap;
    }

    /* set the bitmap to black */
    public static void osd_clearbitmap(osd_bitmap bitmap) {
        int i;

        for (i = bitmap.ptrPos; i < bitmap.height; i++) {
            if (bitmap.depth == 16) {
                memset(bitmap.line[i], 0, 2 * bitmap.width);
            } else {
                memset(bitmap.line[i], BACKGROUND, bitmap.width);
            }
        }

        if (bitmap == scrbitmap) {

            osd_mark_dirty(0, 0, bitmap.width - 1, bitmap.height - 1, 1);
            bitmap_dirty = 1;
        }
    }

    public static void osd_free_bitmap(osd_bitmap bitmap) {
        if (bitmap != null) {
            //bitmap->line -= safety;   //what's the point of this? TODO check it (shadow)
            bitmap.ptrPos -= safety;
            bitmap.line = null;
            bitmap._private = null;
            bitmap = null;
        }
    }

    /*TODO*///
    public static void osd_mark_dirty(int _x1, int _y1, int _x2, int _y2, int ui) {
        //  System.out.println("TODO osd_mark_dirty");

        /*TODO*///	if (use_dirty)
        /*TODO*///	{
        /*TODO*///		int x, y;
        /*TODO*///
        /*TODO*/////        if (errorlog) fprintf(errorlog, "mark_dirty %3d,%3d - %3d,%3d\n", _x1,_y1, _x2,_y2);
        /*TODO*///
        /*TODO*///		_x1 -= skipcolumns;
        /*TODO*///		_x2 -= skipcolumns;
        /*TODO*///		_y1 -= skiplines;
        /*TODO*///		_y2 -= skiplines;
        /*TODO*///
        /*TODO*///	if (_y1 >= gfx_display_lines || _y2 < 0 || _x1 > gfx_display_columns || _x2 < 0) return;
        /*TODO*///		if (_y1 < 0) _y1 = 0;
        /*TODO*///		if (_y2 >= gfx_display_lines) _y2 = gfx_display_lines - 1;
        /*TODO*///		if (_x1 < 0) _x1 = 0;
        /*TODO*///		if (_x2 >= gfx_display_columns) _x2 = gfx_display_columns - 1;
        /*TODO*///
        /*TODO*///		for (y = _y1; y <= _y2 + 15; y += 16)
        /*TODO*///			for (x = _x1; x <= _x2 + 15; x += 16)
        /*TODO*///				MARKDIRTY(x,y);
        /*TODO*///	}
    }

    /*TODO*///
    /*TODO*///static void init_dirty(char dirty)
    /*TODO*///{
    /*TODO*///	memset(dirty_new, dirty, MAX_GFX_WIDTH/16 * MAX_GFX_HEIGHT/16);
    /*TODO*///}
    /*TODO*///
    /*TODO*///INLINE void swap_dirty(void)
    /*TODO*///{
    /*TODO*///    char *tmp;
    /*TODO*///
    /*TODO*///	tmp = dirty_old;
    /*TODO*///	dirty_old = dirty_new;
    /*TODO*///	dirty_new = tmp;
    /*TODO*///}
    /*TODO*///
    /*TODO*////*
    /*TODO*/// * This function tries to find the best display mode.
    /*TODO*/// */
    public static void select_display_mode(int depth) {
        int width, height;

        auto_resolution = 0;
        /*TODO*///	/* assume unchained video mode  */
        /*TODO*///	unchained = 0;
        /*TODO*///	/* see if it's a low scanrate mode */
        /*TODO*///	switch (monitor_type)
        /*TODO*///	{
        /*TODO*///		case MONITOR_TYPE_NTSC:
        /*TODO*///		case MONITOR_TYPE_PAL:
        /*TODO*///		case MONITOR_TYPE_ARCADE:
        /*TODO*///			scanrate15KHz = 1;
        /*TODO*///			break;
        /*TODO*///		default:
        /*TODO*///			scanrate15KHz = 0;
        /*TODO*///	}
        /*TODO*///
        /*TODO*///	/* initialise quadring table [useful for *all* doubling modes */
        /*TODO*///	for (i = 0; i < 256; i++)
        /*TODO*///	{
        /*TODO*///		doublepixel[i] = i | (i<<8);
        /*TODO*///		quadpixel[i] = i | (i<<8) | (i << 16) | (i << 24);
        /*TODO*///	}
        /*TODO*///
        if (vector_game != 0) {
            width = Machine.drv.screen_width;
            height = Machine.drv.screen_height;
        } else {
            width = Machine.drv.visible_area.max_x - Machine.drv.visible_area.min_x + 1;
            height = Machine.drv.visible_area.max_y - Machine.drv.visible_area.min_y + 1;
        }

        if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
            int temp;

            temp = width;
            width = height;
            height = temp;
        }

        use_vesa = -1;

        /* If no VESA resolution has been given, we choose a sensible one. */
 /* 640x480, 800x600 and 1024x768 are common to all VESA drivers. */
        if (gfx_width == 0 && gfx_height == 0) {
            auto_resolution = 1;
            use_vesa = 1;

            /* vector games use 640x480 as default */
            if (vector_game != 0) {
                gfx_width = 640;
                gfx_height = 480;
            } else {
                int xm, ym;

                xm = ym = 1;

                if ((Machine.drv.video_attributes & VIDEO_PIXEL_ASPECT_RATIO_MASK)
                        == VIDEO_PIXEL_ASPECT_RATIO_1_2) {
                    if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
                        xm++;
                    } else {
                        ym++;
                    }
                }

                if (scanlines != 0 && stretch != 0) {
                    if (ym == 1) {
                        xm *= 2;
                        ym *= 2;
                    }

                    /* see if pixel doubling can be applied at 640x480 */
                    if (ym * height <= 480 && xm * width <= 640
                            && (xm > 1 || (ym + 1) * height > 768 || (xm + 1) * width > 1024)) {
                        gfx_width = 640;
                        gfx_height = 480;
                    } /* see if pixel doubling can be applied at 800x600 */ else if (ym * height <= 600 && xm * width <= 800
                            && (xm > 1 || (ym + 1) * height > 768 || (xm + 1) * width > 1024)) {
                        gfx_width = 800;
                        gfx_height = 600;
                    }
                    /* don't use 1024x768 right away. If 512x384 is available, it */
 /* will provide hardware scanlines. */

                    if (ym > 1 && xm > 1) {
                        xm /= 2;
                        ym /= 2;
                    }
                }

                if (gfx_width == 0 && gfx_height == 0) {
                    if (ym * height <= 240 && xm * width <= 320) {
                        gfx_width = 320;
                        gfx_height = 240;
                    } else if (ym * height <= 300 && xm * width <= 400) {
                        gfx_width = 400;
                        gfx_height = 300;
                    } else if (ym * height <= 384 && xm * width <= 512) {
                        gfx_width = 512;
                        gfx_height = 384;
                    } else if (ym * height <= 480 && xm * width <= 640
                            && (stretch == 0 || (ym + 1) * height > 768 || (xm + 1) * width > 1024)) {
                        gfx_width = 640;
                        gfx_height = 480;
                    } else if (ym * height <= 600 && xm * width <= 800
                            && (stretch == 0 || (ym + 1) * height > 768 || (xm + 1) * width > 1024)) {
                        gfx_width = 800;
                        gfx_height = 600;
                    } else {
                        gfx_width = 1024;
                        gfx_height = 768;
                    }
                }
            }
        }
    }

    /*TODO*///
    /*TODO*///
    /*TODO*///
    /* center image inside the display based on the visual area */
    public static void adjust_display(int xmin, int ymin, int xmax, int ymax, int depth) {
        int temp;
        int w, h;
        int act_width;

        act_width = gfx_width;

        if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
            temp = xmin;
            xmin = ymin;
            ymin = temp;
            temp = xmax;
            xmax = ymax;
            ymax = temp;
            w = Machine.drv.screen_height;
            h = Machine.drv.screen_width;
        } else {
            w = Machine.drv.screen_width;
            h = Machine.drv.screen_height;
        }

        if (vector_game == 0) {
            if ((Machine.orientation & ORIENTATION_FLIP_X) != 0) {
                temp = w - xmin - 1;
                xmin = w - xmax - 1;
                xmax = temp;
            }
            if ((Machine.orientation & ORIENTATION_FLIP_Y) != 0) {
                temp = h - ymin - 1;
                ymin = h - ymax - 1;
                ymax = temp;
            }
        }

        viswidth = xmax - xmin + 1;
        visheight = ymax - ymin + 1;

        /* setup xmultiply to handle SVGA driver's (possible) double width */
        xmultiply = act_width / gfx_width;
        ymultiply = 1;

        if (use_vesa != 0 && vector_game == 0) {
            if (stretch != 0) {
                if ((Machine.orientation & ORIENTATION_SWAP_XY) == 0
                        && (Machine.drv.video_attributes & VIDEO_DUAL_MONITOR) == 0) {
                    /* horizontal, non dual monitor games may be stretched at will */
                    while ((xmultiply + 1) * viswidth <= act_width) {
                        xmultiply++;
                    }
                    while ((ymultiply + 1) * visheight <= gfx_height) {
                        ymultiply++;
                    }
                } else {
                    int tw, th;

                    tw = act_width;
                    th = gfx_height;

                    if ((Machine.drv.video_attributes & VIDEO_PIXEL_ASPECT_RATIO_MASK)
                            == VIDEO_PIXEL_ASPECT_RATIO_1_2) {
                        if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
                            tw /= 2;
                        } else {
                            th /= 2;
                        }
                    }

                    /* Hack for 320x480 and 400x600 "vmame" video modes */
                    if ((gfx_width == 320 && gfx_height == 480)
                            || (gfx_width == 400 && gfx_height == 600)) {
                        th /= 2;
                    }

                    /* maintain aspect ratio for other games */
                    while ((xmultiply + 1) * viswidth <= tw
                            && (ymultiply + 1) * visheight <= th) {
                        xmultiply++;
                        ymultiply++;
                    }

                    if ((Machine.drv.video_attributes & VIDEO_PIXEL_ASPECT_RATIO_MASK)
                            == VIDEO_PIXEL_ASPECT_RATIO_1_2) {
                        if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
                            xmultiply *= 2;
                        } else {
                            ymultiply *= 2;
                        }
                    }

                    /* Hack for 320x480 and 400x600 "vmame" video modes */
                    if ((gfx_width == 320 && gfx_height == 480)
                            || (gfx_width == 400 && gfx_height == 600)) {
                        ymultiply *= 2;
                    }
                }
            } else {
                if ((Machine.drv.video_attributes & VIDEO_PIXEL_ASPECT_RATIO_MASK)
                        == VIDEO_PIXEL_ASPECT_RATIO_1_2) {
                    if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
                        xmultiply *= 2;
                    } else {
                        ymultiply *= 2;
                    }
                }

                /* Hack for 320x480 and 400x600 "vmame" video modes */
                if ((gfx_width == 320 && gfx_height == 480)
                        || (gfx_width == 400 && gfx_height == 600)) {
                    ymultiply *= 2;
                }
            }
        }

        if (depth == 16) {
            if (xmultiply > MAX_X_MULTIPLY16) {
                xmultiply = MAX_X_MULTIPLY16;
            }
            if (ymultiply > MAX_Y_MULTIPLY16) {
                ymultiply = MAX_Y_MULTIPLY16;
            }
        } else {
            if (xmultiply > MAX_X_MULTIPLY) {
                xmultiply = MAX_X_MULTIPLY;
            }
            if (ymultiply > MAX_Y_MULTIPLY) {
                ymultiply = MAX_Y_MULTIPLY;
            }
        }

        gfx_display_lines = visheight;
        gfx_display_columns = viswidth;

        gfx_xoffset = (act_width - viswidth * xmultiply) / 2;
        if (gfx_display_columns > act_width / xmultiply) {
            gfx_display_columns = act_width / xmultiply;
        }

        gfx_yoffset = (gfx_height - visheight * ymultiply) / 2;
        if (gfx_display_lines > gfx_height / ymultiply) {
            gfx_display_lines = gfx_height / ymultiply;
        }

        skiplinesmin = ymin;
        skiplinesmax = visheight - gfx_display_lines + ymin;
        skipcolumnsmin = xmin;
        skipcolumnsmax = viswidth - gfx_display_columns + xmin;

        /* Align on a quadword !*/
        gfx_xoffset &= ~7;

        /* the skipcolumns from mame.cfg/cmdline is relative to the visible area */
        skipcolumns = xmin + skipcolumns;
        skiplines = ymin + skiplines;

        /* Just in case the visual area doesn't fit */
        if (gfx_xoffset < 0) {
            skipcolumns -= gfx_xoffset;
            gfx_xoffset = 0;
        }
        if (gfx_yoffset < 0) {
            skiplines -= gfx_yoffset;
            gfx_yoffset = 0;
        }

        /* Failsafe against silly parameters */
        if (skiplines < skiplinesmin) {
            skiplines = skiplinesmin;
        }
        if (skipcolumns < skipcolumnsmin) {
            skipcolumns = skipcolumnsmin;
        }
        if (skiplines > skiplinesmax) {
            skiplines = skiplinesmax;
        }
        if (skipcolumns > skipcolumnsmax) {
            skipcolumns = skipcolumnsmax;
        }

        if (errorlog != null) {
            fprintf(errorlog,
                    "gfx_width = %d gfx_height = %d\n"
                    + "gfx_xoffset = %d gfx_yoffset = %d\n"
                    + "xmin %d ymin %d xmax %d ymax %d\n"
                    + "skiplines %d skipcolumns %d\n"
                    + "gfx_display_lines %d gfx_display_columns %d\n"
                    + "xmultiply %d ymultiply %d\n",
                    gfx_width, gfx_height,
                    gfx_xoffset, gfx_yoffset,
                    xmin, ymin, xmax, ymax, skiplines, skipcolumns, gfx_display_lines, gfx_display_columns, xmultiply, ymultiply);
        }

        set_ui_visarea(skipcolumns, skiplines, skipcolumns + gfx_display_columns - 1, skiplines + gfx_display_lines - 1);

        /* round to a multiple of 4 to avoid missing pixels on the right side */
        gfx_display_columns = (gfx_display_columns + 3) & ~3;
    }

    public static int game_width;
    public static int game_height;
    public static int game_attributes;

    /* Create a display screen, or window, large enough to accomodate a bitmap */
 /* of the given dimensions. Attributes are the ones defined in driver.h. */
 /* Return a osd_bitmap pointer or 0 in case of error. */
    public static osd_bitmap osd_create_display(int width, int height, int depth, int attributes) {
        if (errorlog != null) {
            fprintf(errorlog, "width %d, height %d\n", width, height);
        }

        brightness = 100;
        brightness_paused_adjust = 1.0f;
        dirty_bright = 1;

        if (frameskip < 0) {
            frameskip = 0;
        }
        if (frameskip >= FRAMESKIP_LEVELS) {
            frameskip = FRAMESKIP_LEVELS - 1;
        }

        /*TODO????*///	gone_to_gfx_mode = 0;
        /* Look if this is a vector game */
        if ((Machine.drv.video_attributes & VIDEO_TYPE_VECTOR) != 0) {
            vector_game = 1;
        } else {
            vector_game = 0;
        }

        if (use_dirty == -1) /* dirty=auto in mame.cfg? */ {
            /* Is the game using a dirty system? */
            if (((Machine.drv.video_attributes & VIDEO_SUPPORTS_DIRTY) != 0) || (vector_game != 0)) {
                use_dirty = 1;
            } else {
                use_dirty = 0;
            }
        }

        select_display_mode(depth);

        if (vector_game != 0) {
            throw new UnsupportedOperationException("Unsupported scale_vectorgames");
            /*TODO*///		scale_vectorgames(gfx_width,gfx_height,&width, &height);
        }

        game_width = width;
        game_height = height;
        game_attributes = attributes;

        if (depth == 16) {
            scrbitmap = osd_new_bitmap(width, height, 16);
        } else {
            scrbitmap = osd_new_bitmap(width, height, 8);
        }

        if (scrbitmap == null) {
            return null;
        }

        if (osd_set_display(width, height, attributes) == 0) {
            return null;
        }

        /* center display based on visible area */
        if (vector_game != 0) {
            adjust_display(0, 0, width - 1, height - 1, depth);
        } else {
            rectangle vis = Machine.drv.visible_area;
            adjust_display(vis.min_x, vis.min_y, vis.max_x, vis.max_y, depth);
        }

        return scrbitmap;

    }

    /*TODO*///
    /*TODO*////* set the actual display screen but don't allocate the screen bitmap */
    public static int osd_set_display(int width, int height, int attributes) {
        /*TODO*///	struct mode_adjust *adjust_array;
        /*TODO*///
        int i;
        /* moved 'found' to here (req. for 15.75KHz Arcade Monitor Modes) */
        int found;

        if (gfx_height == 0 || gfx_width == 0) {
            printf("Please specify height AND width (e.g. -640x480)\n");
            return 0;
        }

        if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
            int temp;

            temp = width;
            width = height;
            height = temp;
        }
        /* Mark the dirty buffers as dirty */

 /*TODO*///	if (use_dirty)
        /*TODO*///	{
        /*TODO*///		if (vector_game)
        /*TODO*///			/* vector games only use one dirty buffer */
        /*TODO*///			init_dirty (0);
        /*TODO*///		else
        /*TODO*///			init_dirty(1);
        /*TODO*///		swap_dirty();
        /*TODO*///		init_dirty(1);
        /*TODO*///	}
        /*TODO*///	if (dirtycolor)
        /*TODO*///	{
        /*TODO*///		for (i = 0;i < screen_colors;i++)
        /*TODO*///			dirtycolor[i] = 1;
        /*TODO*///		dirtypalette = 1;
        /*TODO*///	}
        /*TODO*///	/* handle special 15.75KHz modes, these now include SVGA modes */
        /*TODO*///	found = 0;
        /*TODO*///	/*move video freq set to here, as we need to set it explicitly for the 15.75KHz modes */
        /*TODO*///	videofreq = vgafreq;
        /*TODO*///
        /*TODO*///	if (use_vesa != 0)
        /*TODO*///	{
        /*TODO*///		/*removed local 'found' */
        /*TODO*///		int mode, bits, err;
        /*TODO*///
        /*TODO*///		mode = gfx_mode;
        /*TODO*///		found = 0;
        /*TODO*///		bits = scrbitmap->depth;
        /*TODO*///
        /*TODO*///		/* Try the specified vesamode, 565 and 555 for 16 bit color modes, */
        /*TODO*///		/* doubled resolution in case of noscanlines and if not succesful  */
        /*TODO*///		/* repeat for all "lower" VESA modes. NS/BW 19980102 */
        /*TODO*///
        /*TODO*///		while (!found)
        /*TODO*///		{
        /*TODO*///			set_color_depth(bits);
        /*TODO*///
        /*TODO*///			/* allocate a wide enough virtual screen if possible */
        /*TODO*///			/* we round the width (in dwords) to be an even multiple 256 - that */
        /*TODO*///			/* way, during page flipping only one byte of the video RAM */
        /*TODO*///			/* address changes, therefore preventing flickering. */
        /*TODO*///			if (bits == 8)
        /*TODO*///				triplebuf_page_width = (gfx_width + 0x3ff) & ~0x3ff;
        /*TODO*///			else
        /*TODO*///				triplebuf_page_width = (gfx_width + 0x1ff) & ~0x1ff;
        /*TODO*///
        /*TODO*///			/* don't ask for a larger screen if triplebuffer not requested - could */
        /*TODO*///			/* cause problems in some cases. */
        /*TODO*///			err = 1;
        /*TODO*///			if (use_triplebuf)
        /*TODO*///				err = set_gfx_mode(mode,gfx_width,gfx_height,3*triplebuf_page_width,0);
        /*TODO*///			if (err)
        /*TODO*///			{
        /*TODO*///				/* if we're using a SVGA 15KHz driver - tell Allegro the virtual screen width */
        /*TODO*///				if(SVGA15KHzdriver)
        /*TODO*///					err = set_gfx_mode(mode,gfx_width,gfx_height,SVGA15KHzdriver->getlogicalwidth(gfx_width),0);
        /*TODO*///				else
        /*TODO*///					err = set_gfx_mode(mode,gfx_width,gfx_height,0,0);
        /*TODO*///			}
        /*TODO*///
        /*TODO*///			if (errorlog)
        /*TODO*///			{
        /*TODO*///				fprintf (errorlog,"Trying ");
        /*TODO*///				if      (mode == GFX_VESA1)
        /*TODO*///					fprintf (errorlog, "VESA1");
        /*TODO*///				else if (mode == GFX_VESA2B)
        /*TODO*///					fprintf (errorlog, "VESA2B");
        /*TODO*///				else if (mode == GFX_VESA2L)
        /*TODO*///				    fprintf (errorlog, "VESA2L");
        /*TODO*///				else if (mode == GFX_VESA3)
        /*TODO*///					fprintf (errorlog, "VESA3");
        /*TODO*///			    fprintf (errorlog, "  %dx%d, %d bit\n",
        /*TODO*///						gfx_width, gfx_height, bits);
        /*TODO*///			}
        /*TODO*///
        /*TODO*///			if (err == 0)
        /*TODO*///			{
        /*TODO*///				found = 1;
        /*TODO*///				/* replace gfx_mode with found mode */
        /*TODO*///				gfx_mode = mode;
        /*TODO*///				continue;
        /*TODO*///			}
        /*TODO*///			else if (errorlog)
        /*TODO*///				fprintf (errorlog,"%s\n",allegro_error);
        /*TODO*///
        /*TODO*///			/* Now adjust parameters for the next loop */
        /*TODO*///
        /*TODO*///			/* try 5-5-5 in case there is no 5-6-5 16 bit color mode */
        /*TODO*///			if (scrbitmap->depth == 16)
        /*TODO*///			{
        /*TODO*///				if (bits == 16)
        /*TODO*///				{
        /*TODO*///					bits = 15;
        /*TODO*///					continue;
        /*TODO*///				}
        /*TODO*///				else
        /*TODO*///					bits = 16; /* reset to 5-6-5 */
        /*TODO*///			}
        /*TODO*///
        /*TODO*///			/* try VESA modes in VESA3-VESA2L-VESA2B-VESA1 order */
        /*TODO*///
        /*TODO*///			if (mode == GFX_VESA3)
        /*TODO*///			{
        /*TODO*///				mode = GFX_VESA2L;
        /*TODO*///				continue;
        /*TODO*///			}
        /*TODO*///			else if (mode == GFX_VESA2L)
        /*TODO*///			{
        /*TODO*///				mode = GFX_VESA2B;
        /*TODO*///				continue;
        /*TODO*///			}
        /*TODO*///			else if (mode == GFX_VESA2B)
        /*TODO*///			{
        /*TODO*///				mode = GFX_VESA1;
        /*TODO*///				continue;
        /*TODO*///			}
        /*TODO*///			else if (mode == GFX_VESA1)
        /*TODO*///				mode = gfx_mode; /* restart with the mode given in mame.cfg */
        /*TODO*///
        /*TODO*///			/* try higher resolutions */
        /*TODO*///			if (auto_resolution)
        /*TODO*///			{
        /*TODO*///				if (stretch && gfx_width <= 512)
        /*TODO*///				{
        /*TODO*///					/* low res VESA mode not available, try an high res one */
        /*TODO*///					gfx_width *= 2;
        /*TODO*///					gfx_height *= 2;
        /*TODO*///					continue;
        /*TODO*///				}
        /*TODO*///
        /*TODO*///				/* try next higher resolution */
        /*TODO*///				if (gfx_height < 300 && gfx_width < 400)
        /*TODO*///				{
        /*TODO*///					gfx_width = 400;
        /*TODO*///					gfx_height = 300;
        /*TODO*///					continue;
        /*TODO*///				}
        /*TODO*///				else if (gfx_height < 384 && gfx_width < 512)
        /*TODO*///				{
        /*TODO*///					gfx_width = 512;
        /*TODO*///					gfx_height = 384;
        /*TODO*///					continue;
        /*TODO*///				}
        /*TODO*///				else if (gfx_height < 480 && gfx_width < 640)
        /*TODO*///				{
        /*TODO*///					gfx_width = 640;
        /*TODO*///					gfx_height = 480;
        /*TODO*///					continue;
        /*TODO*///				}
        /*TODO*///				else if (gfx_height < 600 && gfx_width < 800)
        /*TODO*///				{
        /*TODO*///					gfx_width = 800;
        /*TODO*///					gfx_height = 600;
        /*TODO*///					continue;
        /*TODO*///				}
        /*TODO*///				else if (gfx_height < 768 && gfx_width < 1024)
        /*TODO*///				{
        /*TODO*///					gfx_width = 1024;
        /*TODO*///					gfx_height = 768;
        /*TODO*///					continue;
        /*TODO*///				}
        /*TODO*///			}
        /*TODO*///
        /*TODO*///			/* If there was no continue up to this point, we give up */
        /*TODO*///			break;
        /*TODO*///		}
        /*TODO*///
        /*TODO*///		if (found == 0)
        /*TODO*///		{
        /*TODO*///			printf ("\nNo %d-bit %dx%d VESA mode available.\n",
        /*TODO*///					scrbitmap->depth,gfx_width,gfx_height);
        /*TODO*///			printf ("\nPossible causes:\n"
        /*TODO*///"1) Your video card does not support VESA modes at all. Almost all\n"
        /*TODO*///"   video cards support VESA modes natively these days, so you probably\n"
        /*TODO*///"   have an older card which needs some driver loaded first.\n"
        /*TODO*///"   In case you can't find such a driver in the software that came with\n"
        /*TODO*///"   your video card, Scitech Display Doctor or (for S3 cards) S3VBE\n"
        /*TODO*///"   are good alternatives.\n"
        /*TODO*///"2) Your VESA implementation does not support this resolution. For example,\n"
        /*TODO*///"   '-320x240', '-400x300' and '-512x384' are only supported by a few\n"
        /*TODO*///"   implementations.\n"
        /*TODO*///"3) Your video card doesn't support this resolution at this color depth.\n"
        /*TODO*///"   For example, 1024x768 in 16 bit colors requires 2MB video memory.\n"
        /*TODO*///"   You can either force an 8 bit video mode ('-depth 8') or use a lower\n"
        /*TODO*///"   resolution ('-640x480', '-800x600').\n");
        /*TODO*///			return 0;
        /*TODO*///		}
        /*TODO*///		else
        /*TODO*///		{
        /*TODO*///			if (errorlog)
        /*TODO*///				fprintf (errorlog, "Found matching %s mode\n", gfx_driver->desc);
        /*TODO*///			gfx_mode = mode;
        /*TODO*///			/* disable triple buffering if the screen is not large enough */
        /*TODO*///			if (errorlog)
        /*TODO*///				fprintf (errorlog, "Virtual screen size %dx%d\n",VIRTUAL_W,VIRTUAL_H);
        /*TODO*///			if (VIRTUAL_W < 3*triplebuf_page_width)
        /*TODO*///			{
        /*TODO*///				use_triplebuf = 0;
        /*TODO*///				if (errorlog)
        /*TODO*///					fprintf (errorlog, "Triple buffer disabled\n");
        /*TODO*///			}
        /*TODO*///
        /*TODO*///			/* if triple buffering is enabled, turn off vsync */
        /*TODO*///			if (use_triplebuf)
        /*TODO*///			{
        /*TODO*///				wait_vsync = 0;
        /*TODO*///				video_sync = 0;
        /*TODO*///			}
        /*TODO*///		}
        /*TODO*///	}
        /*TODO*///	else
        /*TODO*///	{
        /*TODO*///
        /*TODO*///
        /*TODO*///		/* set the VGA clock */
        /*TODO*///		if (video_sync || always_synced || wait_vsync)
        /*TODO*///			reg[0].value = (reg[0].value & 0xf3) | (videofreq << 2);
        /*TODO*///
        /*TODO*///		/* VGA triple buffering */
        /*TODO*///		if(use_triplebuf)
        /*TODO*///		{
        /*TODO*///
        /*TODO*///			int vga_page_size = (gfx_width * gfx_height);
        /*TODO*///			/* see if it'll fit */
        /*TODO*///			if ((vga_page_size * 3) > 0x40000)
        /*TODO*///			{
        /*TODO*///				/* too big */
        /*TODO*///				if (errorlog)
        /*TODO*///					fprintf(errorlog,"tweaked mode %dx%d is too large to triple buffer\ntriple buffering disabled\n",gfx_width,gfx_height);
        /*TODO*///				use_triplebuf = 0;
        /*TODO*///			}
        /*TODO*///			else
        /*TODO*///			{
        /*TODO*///				/* it fits, so set up the 3 pages */
        /*TODO*///				no_xpages = 3;
        /*TODO*///				xpage_size = vga_page_size / 4;
        /*TODO*///				if (errorlog)
        /*TODO*///					fprintf(errorlog,"unchained VGA triple buffering page size :%d\n",xpage_size);
        /*TODO*///				/* and make sure the mode's unchained */
        /*TODO*///				unchain_vga (reg);
        /*TODO*///				/* triple buffering is enabled, turn off vsync */
        /*TODO*///				wait_vsync = 0;
        /*TODO*///				video_sync = 0;
        /*TODO*///			}
        /*TODO*///		}
        /*TODO*///		/* center the mode */
        /*TODO*///		center_mode (reg);
        /*TODO*///
        /*TODO*///		/* set the horizontal and vertical total */
        /*TODO*///		if (scanrate15KHz)
        /*TODO*///			/* 15.75KHz modes */
        /*TODO*///			adjust_array = arcade_adjust;
        /*TODO*///		else
        /*TODO*///			/* PC monitor modes */
        /*TODO*///			adjust_array = pc_adjust;
        /*TODO*///
        /*TODO*///		for (i=0; adjust_array[i].x != 0; i++)
        /*TODO*///		{
        /*TODO*///			if ((gfx_width == adjust_array[i].x) && (gfx_height == adjust_array[i].y))
        /*TODO*///			{
        /*TODO*///				/* check for 'special vertical' modes */
        /*TODO*///				if((!adjust_array[i].vertical_mode && !(Machine->orientation & ORIENTATION_SWAP_XY)) ||
        /*TODO*///					(adjust_array[i].vertical_mode && (Machine->orientation & ORIENTATION_SWAP_XY)))
        /*TODO*///				{
        /*TODO*///					reg[H_TOTAL_INDEX].value = *adjust_array[i].hadjust;
        /*TODO*///					reg[V_TOTAL_INDEX].value = *adjust_array[i].vadjust;
        /*TODO*///					break;
        /*TODO*///				}
        /*TODO*///			}
        /*TODO*///		}
        /*TODO*///
        /*TODO*///		/*if scanlines were requested - change the array values to get a scanline mode */
        /*TODO*///		if (scanlines && !scanrate15KHz)
        /*TODO*///			reg = make_scanline_mode(reg,reglen);
        /*TODO*///
        /*TODO*///		/* big hack: open a mode 13h screen using Allegro, then load the custom screen */
        /*TODO*///		/* definition over it. */
        /*TODO*///		if (set_gfx_mode(GFX_VGA,320,200,0,0) != 0)
        /*TODO*///			return 0;
        /*TODO*///
        /*TODO*///		if (errorlog)
        /*TODO*///		{
        /*TODO*///			fprintf(errorlog,"Generated Tweak Values :-\n");
        /*TODO*///			for (i=0; i<reglen; i++)
        /*TODO*///			{
        /*TODO*///				fprintf(errorlog,"{ 0x%02x, 0x%02x, 0x%02x},",reg[i].port,reg[i].index,reg[i].value);
        /*TODO*///				if (!((i+1)%3))
        /*TODO*///					fprintf(errorlog,"\n");
        /*TODO*///			}
        /*TODO*///		}
        /*TODO*///
        /*TODO*///		/* tweak the mode */
        /*TODO*///		outRegArray(reg,reglen);
        /*TODO*///
        /*TODO*///		/* check for unchained mode,  if unchained clear all pages */
        /*TODO*///		if (unchained)
        /*TODO*///		{
        /*TODO*///			unsigned long address;
        /*TODO*///			/* clear all 4 bit planes */
        /*TODO*///			outportw (0x3c4, (0x02 | (0x0f << 0x08)));
        /*TODO*///			for (address = 0xa0000; address < 0xb0000; address += 4)
        /*TODO*///				_farpokel(screen->seg, address, 0);
        /*TODO*///		}
        /*TODO*///	}
        /*TODO*///
        /*TODO*///
        /*TODO*///	gone_to_gfx_mode = 1;
        /*TODO*///
        /*TODO*///
        vsync_frame_rate = Machine.drv.frames_per_second;
        /*TODO*///
        /*TODO*///	if (video_sync)
        /*TODO*///	{
        /*TODO*///		TICKER a,b;
        /*TODO*///		float rate;
        /*TODO*///
        /*TODO*///
        /*TODO*///		/* wait some time to let everything stabilize */
        /*TODO*///		for (i = 0;i < 60;i++)
        /*TODO*///		{
        /*TODO*///			vsync();
        /*TODO*///			a = ticker();
        /*TODO*///		}
        /*TODO*///
        /*TODO*///		/* small delay for really really fast machines */
        /*TODO*///		for (i = 0;i < 100000;i++) ;
        /*TODO*///
        /*TODO*///		vsync();
        /*TODO*///		b = ticker();
        /*TODO*///
        /*TODO*///		rate = ((float)TICKS_PER_SEC)/(b-a);
        /*TODO*///
        /*TODO*///		if (errorlog)
        /*TODO*///			fprintf(errorlog,"target frame rate = %ffps, video frame rate = %3.2fHz\n",Machine->drv->frames_per_second,rate);
        /*TODO*///
        /*TODO*///		/* don't allow more than 8% difference between target and actual frame rate */
        /*TODO*///		while (rate > Machine->drv->frames_per_second * 108 / 100)
        /*TODO*///			rate /= 2;
        /*TODO*///
        /*TODO*///		if (rate < Machine->drv->frames_per_second * 92 / 100)
        /*TODO*///		{
        /*TODO*///			osd_close_display();
        /*TODO*///			if (errorlog) fprintf(errorlog,"-vsync option cannot be used with this display mode:\n"
        /*TODO*///						"video refresh frequency = %dHz, target frame rate = %ffps\n",
        /*TODO*///						(int)(TICKS_PER_SEC/(b-a)),Machine->drv->frames_per_second);
        /*TODO*///			return 0;
        /*TODO*///		}
        /*TODO*///
        /*TODO*///		if (errorlog) fprintf(errorlog,"adjusted video frame rate = %3.2fHz\n",rate);
        /*TODO*///			vsync_frame_rate = rate;
        /*TODO*///
        /*TODO*///		if (Machine->sample_rate)
        /*TODO*///		{
        /*TODO*///			Machine->sample_rate = Machine->sample_rate * Machine->drv->frames_per_second / rate;
        /*TODO*///			if (errorlog)
        /*TODO*///				fprintf(errorlog,"sample rate adjusted to match video freq: %d\n",Machine->sample_rate);
        /*TODO*///		}
        /*TODO*///	}
        /*TODO*///
        warming_up = 1;

        //kill loading window
        if (MainApplet.inst != null) {
            //kill loading window
            osdepend.dlprogress.setVisible(false);

            MainApplet.inst.addKeyListener(MainApplet.inst);
            MainApplet.inst.setSize((scanlines == 0), width, height);
            MainApplet.inst.setBackground(Color.black);
            MainApplet.inst.start();
            MainApplet.inst.run();
            MainApplet.inst.getFocus();

        } else if (MainStream.inst != null) {
            //main.MainStream.inst.addKeyListener(main.MainStream.inst);
            MainStream.inst.setSize((scanlines == 0), width, height);
            //main.MainStream.inst.setBackground(Color.black);
            //MainStream.inst.start();
            //MainStream.inst.run();
            //main.MainStream.inst.getFocus();
            if (MainStream.debug) {
                screen = new software_gfx(settings.version + " (based on mame v" + build_version + ")");
                screen.pack();
                //screen.setSize((scanlines==1),gfx_width,gfx_height);//this???
                //screen.setSize((scanlines==1),width,height);//this???
                screen.setSize((scanlines == 0), width, height);
                screen.setBackground(Color.black);
                /*part of the old arcadeflex emulator probably need refactoring */
                Dimension localDimension = Toolkit.getDefaultToolkit().getScreenSize();
                screen.setLocation((int) ((localDimension.getWidth() - screen.getWidth()) / 2.0D), (int) ((localDimension.getHeight() - screen.getHeight()) / 2.0D));
                screen.setVisible(true);

                screen.addWindowListener(new WindowAdapter() {

                    public void windowClosing(WindowEvent evt) {
                        screen.readkey = KeyEvent.VK_ESCAPE;
                        screen.key[KeyEvent.VK_ESCAPE] = true;
                        osd_refresh();
                        if (screen != null) {
                            screen.key[KeyEvent.VK_ESCAPE] = false;
                        }
                    }
                });

                screen.addKeyListener(screen);
                screen.setFocusTraversalKeysEnabled(false);
            }
        } else {
            /*part of the old arcadeflex emulator probably need refactoring */
            Dimension localDimension = Toolkit.getDefaultToolkit().getScreenSize();
            //kill loading window
            osdepend.dlprogress.setVisible(false);

            screen = new software_gfx(settings.version + " (based on mame v" + build_version + ")");
            screen.pack();
            //screen.setSize((scanlines==1),gfx_width,gfx_height);//this???
            //screen.setSize((scanlines==1),width,height);//this???
            screen.setSize((scanlines == 0), width, height);
            screen.setBackground(Color.black);
            screen.start();
            screen.run();
            screen.setLocation((int) ((localDimension.getWidth() - screen.getWidth()) / 2.0D), (int) ((localDimension.getHeight() - screen.getHeight()) / 2.0D));
            screen.setVisible(true);
            screen.setResizable((scanlines == 1));

            screen.addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent evt) {
                    screen.readkey = KeyEvent.VK_ESCAPE;
                    screen.key[KeyEvent.VK_ESCAPE] = true;
                    osd_refresh();
                    if (screen != null) {
                        screen.key[KeyEvent.VK_ESCAPE] = false;
                    }
                }
            });

            screen.addComponentListener(new ComponentAdapter() {

                public void componentResized(ComponentEvent evt) {
                    screen.resizeVideo();
                }
            });

            screen.addKeyListener(screen);
            screen.setFocusTraversalKeysEnabled(false);
            screen.requestFocus();

        }
        return 1;
    }

    public static void osd_refresh() {
        /*function from old arcadeflex */

        if (screen != null) {
            screen.blit();
        }
        if (MainApplet.inst != null) {
            MainApplet.inst.blit();
        }
        try {
            Thread.sleep(100L);
        } catch (InterruptedException localInterruptedException) {
        }
    }

    public static void osd_close_display() {
        /*TODO*///	if (gone_to_gfx_mode != 0)
        /*TODO*///	{
        /*TODO*///		/* tidy up if 15.75KHz SVGA mode used */
        /*TODO*///		if (scanrate15KHz && use_vesa == 1)
        /*TODO*///		{
        /*TODO*///			/* check we've got a valid driver before calling it */
        /*TODO*///			if (SVGA15KHzdriver != NULL)
        /*TODO*///				SVGA15KHzdriver->resetSVGA15KHzmode();
        /*TODO*///		}
        /*TODO*///
        /*TODO*///		set_gfx_mode (GFX_TEXT,0,0,0,0);
        /*TODO*///
        if (frames_displayed > FRAMES_TO_SKIP) {
            printf("Average FPS: %f\n", (double) TICKS_PER_SEC / (end_time - start_time) * (frames_displayed - FRAMES_TO_SKIP));
        }
        /*TODO*///	}
        /*TODO*///
        /*TODO*///	free(dirtycolor);
        /*TODO*///	dirtycolor = 0;
        /*TODO*///	free(current_palette);
        /*TODO*///	current_palette = 0;
        /*TODO*///	free(palette_16bit_lookup);
        /*TODO*///	palette_16bit_lookup = 0;
        /*TODO*///	if (scrbitmap)
        /*TODO*///	{
        /*TODO*///		osd_free_bitmap(scrbitmap);
        /*TODO*///		scrbitmap = NULL;
        /*TODO*///	}
    }

    public static int osd_allocate_colors(int totalcolors, UByte[] palette, char[] pens, int modifiable) {
        int i;

        modifiable_palette = modifiable;
        screen_colors = totalcolors;
        if (scrbitmap.depth != 8) {
            screen_colors += 2;
        } else {
            screen_colors = 256;
        }

        dirtycolor = new int[screen_colors];
        current_palette = new UBytePtr(3 * screen_colors);
        palette_16bit_lookup = new char[screen_colors];
        if (dirtycolor == null || current_palette == null || palette_16bit_lookup == null) {
            return 1;
        }

        for (i = 0; i < screen_colors; i++) {
            dirtycolor[i] = 1;
        }
        dirtypalette = 1;
        for (i = 0; i < screen_colors; i++) {
            current_palette.write(3 * i + 0, 0);
            current_palette.write(3 * i + 1, 0);
            current_palette.write(3 * i + 2, 0);
        }

        if (scrbitmap.depth != 8 && modifiable == 0) {
            throw new UnsupportedOperationException("osd_allocatecolors");
            /*TODO*///		int r,g,b;
            /*TODO*///
            /*TODO*///
            /*TODO*///		for (i = 0;i < totalcolors;i++)
            /*TODO*///		{
            /*TODO*///			r = 255 * brightness * pow(palette[3*i+0] / 255.0, 1 / osd_gamma_correction) / 100;
            /*TODO*///			g = 255 * brightness * pow(palette[3*i+1] / 255.0, 1 / osd_gamma_correction) / 100;
            /*TODO*///			b = 255 * brightness * pow(palette[3*i+2] / 255.0, 1 / osd_gamma_correction) / 100;
            /*TODO*///			*pens++ = makecol(r,g,b);
            /*TODO*///		}
            /*TODO*///
            /*TODO*///		Machine->uifont->colortable[0] = makecol(0x00,0x00,0x00);
            /*TODO*///		Machine->uifont->colortable[1] = makecol(0xff,0xff,0xff);
            /*TODO*///		Machine->uifont->colortable[2] = makecol(0xff,0xff,0xff);
            /*TODO*///		Machine->uifont->colortable[3] = makecol(0x00,0x00,0x00);
        } else {
            if (scrbitmap.depth == 8 && totalcolors >= 255) {
                int bestblack, bestwhite;
                int bestblackscore, bestwhitescore;

                bestblack = bestwhite = 0;
                bestblackscore = 3 * 255 * 255;
                bestwhitescore = 0;
                for (i = 0; i < totalcolors; i++) {
                    int r, g, b, score;

                    r = palette[3 * i + 0].read();
                    g = palette[3 * i + 1].read();
                    b = palette[3 * i + 2].read();
                    score = r * r + g * g + b * b;

                    if (score < bestblackscore) {
                        bestblack = i;
                        bestblackscore = score;
                    }
                    if (score > bestwhitescore) {
                        bestwhite = i;
                        bestwhitescore = score;
                    }
                }

                for (i = 0; i < totalcolors; i++) {
                    pens[i] = (char) i;
                }

                /* map black to pen 0, otherwise the screen border will not be black */
                pens[bestblack] = 0;
                pens[0] = (char) bestblack;

                Machine.uifont.colortable.write(0, pens[bestblack]);
                Machine.uifont.colortable.write(1, pens[bestwhite]);
                Machine.uifont.colortable.write(2, pens[bestwhite]);
                Machine.uifont.colortable.write(3, pens[bestblack]);
            } else {
                /* reserve color 1 for the user interface text */
                current_palette.write(3 * 1 + 0, 0xff);
                current_palette.write(3 * 1 + 1, 0xff);
                current_palette.write(3 * 1 + 2, 0xff);
                Machine.uifont.colortable.write(0, 0);
                Machine.uifont.colortable.write(1, 1);
                Machine.uifont.colortable.write(2, 1);
                Machine.uifont.colortable.write(3, 0);

                /* fill the palette starting from the end, so we mess up badly written */
 /* drivers which don't go through Machine->pens[] */
                for (i = 0; i < totalcolors; i++) {
                    pens[i] = (char) ((screen_colors - 1) - i);
                }
            }

            for (i = 0; i < totalcolors; i++) {
                current_palette.write(3 * pens[i] + 0, palette[3 * i].read());
                current_palette.write(3 * pens[i] + 1, palette[3 * i + 1].read());
                current_palette.write(3 * pens[i] + 2, palette[3 * i + 2].read());
            }
        }

        /*TODO*///
        /*TODO*///	if (use_vesa == 0)
        /*TODO*///	{
        /*TODO*///		if (use_dirty) /* supports dirty ? */
        /*TODO*///		{
        /*TODO*///			if (unchained)
        /*TODO*///			{
        /*TODO*///				update_screen = blitscreen_dirty1_unchained_vga;
        /*TODO*///				if (errorlog) fprintf (errorlog, "blitscreen_dirty1_unchained_vga\n");
        /*TODO*///			}
        /*TODO*///			else
        /*TODO*///			{
        /*TODO*///				update_screen = blitscreen_dirty1_vga;
        /*TODO*///				if (errorlog) fprintf (errorlog, "blitscreen_dirty1_vga\n");
        /*TODO*///			}
        /*TODO*///		}
        /*TODO*///		else
        /*TODO*///		{
        /*TODO*///			/* check for unchained modes */
        /*TODO*///			if (unchained)
        /*TODO*///			{
        /*TODO*///				update_screen = blitscreen_dirty0_unchained_vga;
        /*TODO*///				if (errorlog) fprintf (errorlog, "blitscreen_dirty0_unchained_vga\n");
        /*TODO*///			}
        /*TODO*///			else
        /*TODO*///			{
        /*TODO*///				update_screen = blitscreen_dirty0_vga;
        /*TODO*///				if (errorlog) fprintf (errorlog, "blitscreen_dirty0_vga\n");
        /*TODO*///			}
        /*TODO*///		}
        /*TODO*///	}
        /*TODO*///	else
        /*TODO*///	{
        /*TODO*///		if (use_mmx == -1) /* mmx=auto: can new mmx blitters be applied? */
        /*TODO*///		{
        /*TODO*///			/* impossible cases follow */
        /*TODO*///			if (!cpu_mmx)
        /*TODO*///				mmxlfb = 0;
        /*TODO*///			else if ((gfx_mode != GFX_VESA2L) && (gfx_mode != GFX_VESA3))
        /*TODO*///				mmxlfb = 0;
        /*TODO*///			/* not yet implemented cases follow */
        /*TODO*///			else if ((xmultiply > 2) || (ymultiply > 2))
        /*TODO*///				mmxlfb = 0;
        /*TODO*///			else
        /*TODO*///				mmxlfb = 1;
        /*TODO*///		}
        /*TODO*///		else /* use forced mmx= setting from mame.cfg at own risk!!! */
        /*TODO*///			mmxlfb = use_mmx;
        /*TODO*///
        /*TODO*///		if (scrbitmap->depth == 16)
        /*TODO*///		{
        /*TODO*///			if (modifiable_palette)
        /*TODO*///				update_screen = updaters16_palettized[xmultiply-1][ymultiply-1][scanlines?1:0][use_dirty?1:0];
        /*TODO*///			else
        /*TODO*///				update_screen = updaters16[xmultiply-1][ymultiply-1][scanlines?1:0][use_dirty?1:0];
        /*TODO*///		}
        /*TODO*///		else
        /*TODO*///		{
        /*TODO*///			update_screen = updaters8[xmultiply-1][ymultiply-1][scanlines?1:0][use_dirty?1:0];
        /*TODO*///		}
        /*TODO*///	}
        /*TODO*///
        /*temphack?? */ back_buffer = new char[Machine.scrbitmap.line[scrbitmap.ptrPos].memory.length];
        return 0;
    }

    /*TODO*///
    /*TODO*///
    public static void osd_modify_pen(int pen, int red, int green, int blue) {
        if (modifiable_palette == 0) {
            if (errorlog != null) {
                fprintf(errorlog, "error: osd_modify_pen() called with modifiable_palette == 0\n");
            }
            return;
        }

        if (current_palette.read(3 * pen + 0) != red
                || current_palette.read(3 * pen + 1) != green
                || current_palette.read(3 * pen + 2) != blue) {
            current_palette.write(3 * pen + 0, red);
            current_palette.write(3 * pen + 1, green);
            current_palette.write(3 * pen + 2, blue);

            dirtycolor[pen] = 1;
            dirtypalette = 1;
        }
    }

    public static void osd_get_pen(int pen, char[] red, char[] green, char[] blue) {
        if (scrbitmap.depth != 8 && modifiable_palette == 0) {
            throw new UnsupportedOperationException("Unimplemented");
            /*TODO*///		*red =   getr(pen);
            /*TODO*///		*green = getg(pen);
            /*TODO*///		*blue =  getb(pen);
        } else {
            red[0] = (char) (current_palette.read(3 * pen + 0) & 0xFF);
            green[0] = (char) (current_palette.read(3 * pen + 1) & 0xFF);
            blue[0] = (char) (current_palette.read(3 * pen + 2) & 0xFF);
        }
    }

    /*TODO*///
    /*TODO*///
    /*TODO*///void update_screen_dummy(void)
    /*TODO*///{
    /*TODO*///	if (errorlog)
    /*TODO*///		fprintf(errorlog, "msdos/video.c: undefined update_screen() function for %d x %d!\n",xmultiply,ymultiply);
    /*TODO*///}
    /*TODO*///
    public static void pan_display() {
        int pan_changed = 0;

        /* horizontal panning */
        if (input_ui_pressed_repeat(IPT_UI_PAN_LEFT, 1) != 0) {
            if (skipcolumns < skipcolumnsmax) {
                skipcolumns++;
                osd_mark_dirty(0, 0, scrbitmap.width - 1, scrbitmap.height - 1, 1);
                pan_changed = 1;
            }
        }
        if (input_ui_pressed_repeat(IPT_UI_PAN_RIGHT, 1) != 0) {
            if (skipcolumns > skipcolumnsmin) {
                skipcolumns--;
                osd_mark_dirty(0, 0, scrbitmap.width - 1, scrbitmap.height - 1, 1);
                pan_changed = 1;
            }
        }
        if (input_ui_pressed_repeat(IPT_UI_PAN_DOWN, 1) != 0) {
            if (skiplines < skiplinesmax) {
                skiplines++;
                osd_mark_dirty(0, 0, scrbitmap.width - 1, scrbitmap.height - 1, 1);
                pan_changed = 1;
            }
        }
        if (input_ui_pressed_repeat(IPT_UI_PAN_UP, 1) != 0) {
            if (skiplines > skiplinesmin) {
                skiplines--;
                osd_mark_dirty(0, 0, scrbitmap.width - 1, scrbitmap.height - 1, 1);
                pan_changed = 1;
            }
        }
        if (pan_changed != 0) {
            /*TODO*///  		if (use_dirty) init_dirty(1);

            set_ui_visarea(skipcolumns, skiplines, skipcolumns + gfx_display_columns - 1, skiplines + gfx_display_lines - 1);
        }
    }
    static int[][] skiptable
            = {
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                new int[]{0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1},
                new int[]{0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1},
                new int[]{0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1},
                new int[]{0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 1},
                new int[]{0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1},
                new int[]{0, 1, 0, 1, 1, 0, 1, 0, 1, 1, 0, 1},
                new int[]{0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1},
                new int[]{0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1},
                new int[]{0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1},
                new int[]{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
            };

    public static int osd_skip_this_frame() {
        return skiptable[frameskip][frameskip_counter];
    }

    static int[][] waittable
            = {
                new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                new int[]{2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
                new int[]{2, 1, 1, 1, 1, 0, 2, 1, 1, 1, 1, 0},
                new int[]{2, 1, 1, 0, 2, 1, 1, 0, 2, 1, 1, 0},
                new int[]{2, 1, 0, 2, 1, 0, 2, 1, 0, 2, 1, 0},
                new int[]{2, 0, 2, 1, 0, 2, 0, 2, 1, 0, 2, 0},
                new int[]{2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0},
                new int[]{2, 0, 2, 0, 0, 3, 0, 2, 0, 0, 3, 0},
                new int[]{3, 0, 0, 3, 0, 0, 3, 0, 0, 3, 0, 0},
                new int[]{4, 0, 0, 0, 4, 0, 0, 0, 4, 0, 0, 0},
                new int[]{6, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0},
                new int[]{12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
            };

    static int showfps, showfpstemp;

    static long prev_measure, this_frame_base, prev;
    static int speed = 100;
    static int vups, vfcount;

    static long last1, last2;
    static int frameskipadjust;

    static long ticksPerFrame, ticksSinceLastFrame; //not in mame

    /* Update the display. */
    public static void osd_update_video_and_audio2() {
        long curr;
        int need_to_clear_bitmap = 0;
        int already_synced;

        if (warming_up != 0) {
            /* first time through, initialize timer */
            prev_measure = (ticker() - (long) (FRAMESKIP_LEVELS * TICKS_PER_SEC / Machine.drv.frames_per_second));
            warming_up = 0;
            //use different settings depending on fps (shadow)
            if (Machine.drv.frames_per_second == 30) {
                ticksPerFrame = (long) (TICKS_PER_SEC / Machine.drv.frames_per_second * 0.95);
            }
            if (Machine.drv.frames_per_second == 60) {
                ticksPerFrame = (long) (TICKS_PER_SEC / Machine.drv.frames_per_second * 0.90);
            }
        }

        if (frameskip_counter == 0) {
            this_frame_base = (prev_measure + (long) (FRAMESKIP_LEVELS * TICKS_PER_SEC / Machine.drv.frames_per_second));
        }

        if (throttle != 0) {
            /* if too much time has passed since last sound update, disable throttling */
 /* temporarily - we wouldn't be able to keep synch anyway. */
            curr = ticker();
            if ((curr - last1) > (2 * TICKS_PER_SEC / Machine.drv.frames_per_second)) {
                throttle = 1;
            }
            last1 = curr;

            already_synced = 1;//update_audio();

            throttle = 1;
        } else {
            already_synced = 1;//update_audio();
        }
        if (osd_skip_this_frame() == 0) {
            if (showfpstemp != 0) {
                showfpstemp--;
                if (showfps == 0 && showfpstemp == 0) {
                    need_to_clear_bitmap = 1;
                }
            }

            if (input_ui_pressed(IPT_UI_SHOW_FPS) != 0) {
                if (showfpstemp != 0) {
                    showfpstemp = 0;
                    need_to_clear_bitmap = 1;
                } else {
                    showfps ^= 1;
                    if (showfps == 0) {
                        need_to_clear_bitmap = 1;
                    }
                }
            }

            /* now wait until it's time to update the screen */
            //while ((ticker() - ticksSinceLastFrame) < ticksPerFrame)System.Threading.Thread.Sleep(0);
            if (throttle != 0) {
                if (video_sync != 0) {
                    do {
                        //vsync();
                        curr = ticker();
                    } while ((long) (TICKS_PER_SEC / (curr - last2)) > (long) (Machine.drv.frames_per_second * 11L / 10L));

                    last2 = curr;
                } else {
                    long target;
                    /* wait for video sync but use normal throttling */
                    //                        if (wait_vsync != 0)
                    //vsync();
                    while ((ticker() - ticksSinceLastFrame) < ticksPerFrame/**
                             * 0.95
                             */
                            );

                    curr = ticker();

                    if (already_synced == 0) {
                        /* wait only if the audio update hasn't synced us already */

                        target = this_frame_base + (long) (frameskip_counter * TICKS_PER_SEC / Machine.drv.frames_per_second);

                        if (curr - target < 0) {
                            do {
                                curr = ticker();
                            } while (curr - target < 0);
                        }
                    }
                }
            } else {
                curr = ticker();
            }

            /* for the FPS average calculation */
            if (++frames_displayed == FRAMES_TO_SKIP) {
                start_time = curr;
            } else {
                end_time = curr;
            }

            if (frameskip_counter == 0) {
                long divdr = (Machine.drv.frames_per_second * (curr - prev_measure) / (100L * FRAMESKIP_LEVELS));
                speed = (int) ((TICKS_PER_SEC + divdr / 2L) / divdr);
                prev_measure = curr;
            }

            prev = curr;

            vfcount += waittable[frameskip][frameskip_counter];
            if (vfcount >= Machine.drv.frames_per_second) {
                vfcount = 0;
                //    vups = AvgDvg.vector_updates;
                //     AvgDvg.vector_updates = 0;
//                    throw new UnsupportedOperationException("Not supported yet.");
            }

            if (showfps != 0 || showfpstemp != 0) {
                int divdr = 100 * FRAMESKIP_LEVELS;
                int fps = (Machine.drv.frames_per_second * (FRAMESKIP_LEVELS - frameskip) * speed + (divdr / 2)) / divdr;
                String buf = sprintf("%s%2d%4d%%%4d/%d fps", autoframeskip != 0 ? "auto" : "fskp", frameskip, speed, fps, (int) (Machine.drv.frames_per_second + 0.5));
                ui_text(buf, Machine.uiwidth - buf.length() * Machine.uifontwidth, 0);
                if (vector_game != 0) {
                    throw new UnsupportedOperationException("Not supported yet.");
                    //buf += sprintf(" %d vector updates", vups);
                    //ui_text(buf, Machine.uiwidth - (buf.Length) * Machine.uifontwidth, Machine.uifontheight);
                }
            }

            if (scrbitmap.depth == 8) {
                if (dirty_bright != 0) {
                    dirty_bright = 0;
                    for (int i = 0; i < 256; i++) {
                        float rate = (float) (brightness * brightness_paused_adjust * Math.pow(i / 255.0, 1 / osd_gamma_correction) / 100);
                        /*bright_lookup[i] = 63 * rate + 0.5;*/ bright_lookup[i] = (int) (255 * rate + 0.5);

                    }
                }
                if (dirtypalette != 0) {
                    dirtypalette = 0;
                    for (int i = 0; i < screen_colors; i++) {
                        if (dirtycolor[i] != 0) {
                            RGB adjusted_palette = new RGB();

                            dirtycolor[i] = 0;

                            adjusted_palette.r = current_palette.read(3 * i + 0);
                            adjusted_palette.g = current_palette.read(3 * i + 1);
                            adjusted_palette.b = current_palette.read(3 * i + 2);
                            if (i != Machine.uifont.colortable.read(1)) /* don't adjust the user interface text */ {
                                adjusted_palette.r = (char) bright_lookup[adjusted_palette.r];
                                adjusted_palette.g = (char) bright_lookup[adjusted_palette.g];
                                adjusted_palette.b = (char) bright_lookup[adjusted_palette.b];
                            } else {

                                /*TODO*///							adjusted_palette.r >>= 2;
                                /*TODO*///							adjusted_palette.g >>= 2;
                                /*TODO*///							adjusted_palette.b >>= 2;
                            }
                            set_color(i, adjusted_palette);
                        }
                    }
                }
            } else {
                throw new UnsupportedOperationException("Not supported yet.");

                /*TODO*///			if (dirty_bright)
                /*TODO*///			{
                /*TODO*///				dirty_bright = 0;
                /*TODO*///				for (i = 0;i < 256;i++)
                /*TODO*///				{
                /*TODO*///					float rate = brightness * brightness_paused_adjust * pow(i / 255.0, 1 / osd_gamma_correction) / 100;
                /*TODO*///					bright_lookup[i] = 255 * rate + 0.5;
                /*TODO*///				}
                /*TODO*///			}
                /*TODO*///			if (dirtypalette)
                /*TODO*///			{
                /*TODO*///				if (use_dirty) init_dirty(1);	/* have to redraw the whole screen */
                /*TODO*///
                /*TODO*///				dirtypalette = 0;
                /*TODO*///				for (i = 0;i < screen_colors;i++)
                /*TODO*///				{
                /*TODO*///					if (dirtycolor[i])
                /*TODO*///					{
                /*TODO*///						int r,g,b;
                /*TODO*///
                /*TODO*///						dirtycolor[i] = 0;
                /*TODO*///
                /*TODO*///						r = current_palette[3*i+0];
                /*TODO*///						g = current_palette[3*i+1];
                /*TODO*///						b = current_palette[3*i+2];
                /*TODO*///						if (i != Machine->uifont->colortable[1])	/* don't adjust the user interface text */
                /*TODO*///						{
                /*TODO*///							r = bright_lookup[r];
                /*TODO*///							g = bright_lookup[g];
                /*TODO*///							b = bright_lookup[b];
                /*TODO*///						}
                /*TODO*///						palette_16bit_lookup[i] = makecol(r,g,b) * 0x10001;
                /*TODO*///					}
                /*TODO*///				}
                /*TODO*///			}
            }

            /* copy the bitmap to screen memory */
            //doupdate_screen();
            blitscreen_dirty1_vga();

            if (need_to_clear_bitmap != 0) {
                osd_clearbitmap(scrbitmap);
            }

            if (use_dirty != 0) {
                /*TODO*///                   if (!vector_game)
                /*TODO*///                       swap_dirty();
                /*TODO*///                   init_dirty(0);
            }

            if (need_to_clear_bitmap != 0) {
                osd_clearbitmap(scrbitmap);
            }

            if (throttle == 0 && autoframeskip == 0 && frameskip_counter == 0) {
                /* adjust speed to video refresh rate if vsync is on */
                int adjspeed = (int) (speed * Machine.drv.frames_per_second / vsync_frame_rate);

                if (adjspeed >= 100) {
                    frameskipadjust++;
                    if (frameskipadjust >= 3) {
                        frameskipadjust = 0;
                        if (frameskip > 0) {
                            frameskip--;
                        }
                    }
                } else {
                    if (adjspeed < 80) {
                        frameskipadjust -= (90 - adjspeed) / 5;
                    } else /* don't push frameskip too far if we are close to 100% speed */ if (frameskip < 8) {
                        frameskipadjust--;
                    }

                    while (frameskipadjust <= -2) {
                        frameskipadjust += 2;
                        if (frameskip < FRAMESKIP_LEVELS - 1) {
                            frameskip++;
                        }
                    }
                }
            }
        }

        /* Check for PGUP, PGDN and pan screen */
        pan_display();

        if (input_ui_pressed(IPT_UI_FRAMESKIP_INC) != 0) {
            if (autoframeskip != 0) {
                autoframeskip = 0;
                frameskip = 0;
            } else if (frameskip == FRAMESKIP_LEVELS - 1) {
                frameskip = 0;
                autoframeskip = 1;
            } else {
                frameskip++;
            }

            if (showfps == 0) {
                showfpstemp = (int) (2 * Machine.drv.frames_per_second);
            }

            /* reset the frame counter every time the frameskip key is pressed, so */
 /* we'll measure the average FPS on a consistent status. */
            frames_displayed = 0;
        }

        if (input_ui_pressed(IPT_UI_FRAMESKIP_DEC) != 0) {
            if (autoframeskip != 0) {
                autoframeskip = 0;
                frameskip = FRAMESKIP_LEVELS - 1;
            } else if (frameskip == 0) {
                autoframeskip = 1;
            } else {
                frameskip--;
            }

            if (showfps == 0) {
                showfpstemp = (int) (2 * Machine.drv.frames_per_second);
            }

            /* reset the frame counter every time the frameskip key is pressed, so */
 /* we'll measure the average FPS on a consistent status. */
            frames_displayed = 0;
        }
        if (input_ui_pressed(IPT_UI_THROTTLE) != 0) {
            throttle ^= 1;

            /* reset the frame counter every time the throttle key is pressed, so */
 /* we'll measure the average FPS on a consistent status. */
            frames_displayed = 0;
        }

        frameskip_counter = (frameskip_counter + 1) % FRAMESKIP_LEVELS;
        ticksSinceLastFrame = ticker();
    }

    //TEMP HACK used old arcadeflex's sync. should be rewriten to new format
    static final int MEMORY = 10;
    static long[] prev1 = new long[10];
    static int clock_counter;
    static int framecount = 0;

    public static void osd_update_video_and_audio() {
        if (++framecount > frameskip) {
            framecount = 0;

            if (input_ui_pressed(IPT_UI_SHOW_FPS) != 0) {
                if (showfpstemp != 0) {
                    showfpstemp = 0;
                    need_to_clear_bitmap = 1;
                } else {
                    showfps ^= 1;
                    if (showfps == 0) {
                        need_to_clear_bitmap = 1;
                    }
                }
            }

            long curr;
            /* now wait until it's time to trigger the interrupt */
            do {

                curr = uclock();
            } while ((throttle != 0) && (curr - prev1[clock_counter] < (frameskip + 1) * 1000000000 / Machine.drv.frames_per_second));
            //while (throttle != 0 && video_sync == 0 && (curr - prev[i]) < (frameskip+1) * UCLOCKS_PER_SEC/drv.frames_per_second);
            if (showfps != 0 || showfpstemp != 0) {
                int fps;
                String buf;
                int divdr;

                divdr = 100 * FRAMESKIP_LEVELS;
                fps = (Machine.drv.frames_per_second * (FRAMESKIP_LEVELS - frameskip) * speed + (divdr / 2)) / divdr;
                buf = sprintf("%s%2d%4d%%%4d/%d fps", autoframeskip != 0 ? "auto" : "fskp", frameskip, speed, fps, (int) (Machine.drv.frames_per_second + 0.5));
                ui_text(buf, Machine.uiwidth - buf.length() * Machine.uifontwidth, 0);
                if (vector_game != 0) {
                    sprintf(buf, " %d vector updates", vups);
                    ui_text(buf, Machine.uiwidth - buf.length() * Machine.uifontwidth, Machine.uifontheight);
                }
            }
            if (scrbitmap.depth == 8) {
                if (dirty_bright != 0) {
                    dirty_bright = 0;
                    for (int i = 0; i < 256; i++) {
                        float rate = (float) (brightness * brightness_paused_adjust * Math.pow(i / 255.0, 1 / osd_gamma_correction) / 100);
                        /*bright_lookup[i] = 63 * rate + 0.5;*/ bright_lookup[i] = (int) (255 * rate + 0.5);

                    }
                }
                if (dirtypalette != 0) {
                    dirtypalette = 0;
                    for (int i = 0; i < screen_colors; i++) {
                        if (dirtycolor[i] != 0) {
                            RGB adjusted_palette = new RGB();

                            dirtycolor[i] = 0;

                            adjusted_palette.r = current_palette.read(3 * i + 0);
                            adjusted_palette.g = current_palette.read(3 * i + 1);
                            adjusted_palette.b = current_palette.read(3 * i + 2);
                            if (i != Machine.uifont.colortable.read(1)) /* don't adjust the user interface text */ {
                                adjusted_palette.r = (char) bright_lookup[adjusted_palette.r];
                                adjusted_palette.g = (char) bright_lookup[adjusted_palette.g];
                                adjusted_palette.b = (char) bright_lookup[adjusted_palette.b];
                            } else {

                                /*TODO*///							adjusted_palette.r >>= 2;
                                /*TODO*///							adjusted_palette.g >>= 2;
                                /*TODO*///							adjusted_palette.b >>= 2;
                            }
                            set_color(i, adjusted_palette);
                        }
                    }
                }
            }
            /*TODO*///		else
            /*TODO*///		{
            /*TODO*///			if (dirty_bright)
            /*TODO*///			{
            /*TODO*///				dirty_bright = 0;
            /*TODO*///				for (i = 0;i < 256;i++)
            /*TODO*///				{
            /*TODO*///					float rate = brightness * brightness_paused_adjust * pow(i / 255.0, 1 / osd_gamma_correction) / 100;
            /*TODO*///					bright_lookup[i] = 255 * rate + 0.5;
            /*TODO*///				}
            /*TODO*///			}
            /*TODO*///			if (dirtypalette)
            /*TODO*///			{
            /*TODO*///				if (use_dirty) init_dirty(1);	/* have to redraw the whole screen */
            /*TODO*///
            /*TODO*///				dirtypalette = 0;
            /*TODO*///				for (i = 0;i < screen_colors;i++)
            /*TODO*///				{
            /*TODO*///					if (dirtycolor[i])
            /*TODO*///					{
            /*TODO*///						int r,g,b;
            /*TODO*///
            /*TODO*///						dirtycolor[i] = 0;
            /*TODO*///
            /*TODO*///						r = current_palette[3*i+0];
            /*TODO*///						g = current_palette[3*i+1];
            /*TODO*///						b = current_palette[3*i+2];
            /*TODO*///						if (i != Machine->uifont->colortable[1])	/* don't adjust the user interface text */
            /*TODO*///						{
            /*TODO*///							r = bright_lookup[r];
            /*TODO*///							g = bright_lookup[g];
            /*TODO*///							b = bright_lookup[b];
            /*TODO*///						}
            /*TODO*///						palette_16bit_lookup[i] = makecol(r,g,b) * 0x10001;
            /*TODO*///					}
            /*TODO*///				}
            /*TODO*///			}
            /*TODO*///		}
            blitscreen_dirty1_vga();
            update_audio();
            if (need_to_clear_bitmap != 0) {
                osd_clearbitmap(scrbitmap);
            }
            clock_counter = (clock_counter + 1) % MEMORY;
            if ((curr - prev1[clock_counter]) != 0) {
                long divdr = Machine.drv.frames_per_second * (curr - prev1[clock_counter]) / (100L * MEMORY);

                speed = (int) ((UCLOCKS_PER_SEC * (frameskip + 1) + divdr / 2L) / divdr);
            }

            prev1[clock_counter] = curr;
        }
    }

    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///void osd_set_gamma(float _gamma)
    /*TODO*///{
    /*TODO*///	int i;
    /*TODO*///
    /*TODO*///	osd_gamma_correction = _gamma;
    /*TODO*///
    /*TODO*///	for (i = 0;i < screen_colors;i++)
    /*TODO*///		dirtycolor[i] = 1;
    /*TODO*///	dirtypalette = 1;
    /*TODO*///	dirty_bright = 1;
    /*TODO*///}
    /*TODO*///
    /*TODO*///float osd_get_gamma(void)
    /*TODO*///{
    /*TODO*///	return osd_gamma_correction;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* brightess = percentage 0-100% */
    /*TODO*///void osd_set_brightness(int _brightness)
    /*TODO*///{
    /*TODO*///	int i;
    /*TODO*///
    /*TODO*///	brightness = _brightness;
    /*TODO*///
    /*TODO*///	for (i = 0;i < screen_colors;i++)
    /*TODO*///		dirtycolor[i] = 1;
    /*TODO*///	dirtypalette = 1;
    /*TODO*///	dirty_bright = 1;
    /*TODO*///}
    /*TODO*///
    /*TODO*///int osd_get_brightness(void)
    /*TODO*///{
    /*TODO*///	return brightness;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///void osd_save_snapshot(void)
    /*TODO*///{
    /*TODO*///	save_screen_snapshot();
    /*TODO*///}
    public static void osd_pause(int paused) {
        int i;

        if (paused != 0) {
            brightness_paused_adjust = (float) 0.65;
        } else {
            brightness_paused_adjust = (float) 1.0;
        }

        for (i = 0; i < screen_colors; i++) {
            dirtycolor[i] = 1;
        }
        dirtypalette = 1;
        dirty_bright = 1;
    }
    /*TODO*///
    /*TODO*///Register *make_scanline_mode(Register *inreg,int entries)
    /*TODO*///{
    /*TODO*///	static Register outreg[32];
    /*TODO*///	int maxscan,maxscanout;
    /*TODO*///	int overflow,overflowout;
    /*TODO*///	int ytotalin,ytotalout;
    /*TODO*///	int ydispin,ydispout;
    /*TODO*///	int vrsin,vrsout,vreout,vblksout,vblkeout;
    /*TODO*////* first - check's it not already a 'non doubled' line mode */
    /*TODO*///	maxscan = inreg[MAXIMUM_SCANLINE_INDEX].value;
    /*TODO*///	if ((maxscan & 1) == 0)
    /*TODO*///	/* it is, so just return the array as is */
    /*TODO*///  		return inreg;
    /*TODO*////* copy across our standard display array */
    /*TODO*///	memcpy (&outreg, inreg, entries * sizeof(Register));
    /*TODO*////* keep hold of the overflow register - as we'll need to refer to it a lot */
    /*TODO*///	overflow = inreg[OVERFLOW_INDEX].value;
    /*TODO*////* set a large line compare value  - as we won't be doing any split window scrolling etc.*/
    /*TODO*///	maxscanout = 0x40;
    /*TODO*////* half all the y values */
    /*TODO*////* total */
    /*TODO*///	ytotalin = inreg[V_TOTAL_INDEX].value;
    /*TODO*///	ytotalin |= ((overflow & 1)<<0x08) | ((overflow & 0x20)<<0x04);
    /*TODO*///    ytotalout = ytotalin >> 1;
    /*TODO*////* display enable end */
    /*TODO*///	ydispin = inreg[13].value | ((overflow & 0x02)<< 0x07) | ((overflow & 0x040) << 0x03);
    /*TODO*///	ydispin ++;
    /*TODO*///	ydispout = ydispin >> 1;
    /*TODO*///	ydispout --;
    /*TODO*///	overflowout = ((ydispout & 0x100) >> 0x07) | ((ydispout && 0x200) >> 0x03);
    /*TODO*///	outreg[V_END_INDEX].value = (ydispout & 0xff);
    /*TODO*////* avoid top over scan */
    /*TODO*///	if ((ytotalin - ydispin) < 40 && !center_y)
    /*TODO*///	{
    /*TODO*///  		vrsout = ydispout;
    /*TODO*///		/* give ourselves a scanline cushion */
    /*TODO*///		ytotalout += 2;
    /*TODO*///	}
    /*TODO*///  	else
    /*TODO*///	{
    /*TODO*////* vertical retrace start */
    /*TODO*///		vrsin = inreg[V_RETRACE_START_INDEX].value | ((overflow & 0x04)<<0x06) | ((overflow & 0x80)<<0x02);
    /*TODO*///		vrsout = vrsin >> 1;
    /*TODO*///	}
    /*TODO*////* check it's legal */
    /*TODO*///	if (vrsout < ydispout)
    /*TODO*///		vrsout = ydispout;
    /*TODO*////*update our output overflow */
    /*TODO*///	overflowout |= (((vrsout & 0x100) >> 0x06) | ((vrsout & 0x200) >> 0x02));
    /*TODO*///	outreg[V_RETRACE_START_INDEX].value = (vrsout & 0xff);
    /*TODO*////* vertical retrace end */
    /*TODO*///	vreout = vrsout + 2;
    /*TODO*////* make sure the retrace fits into our adjusted display size */
    /*TODO*///	if (vreout > (ytotalout - 9))
    /*TODO*///		ytotalout = vreout + 9;
    /*TODO*////* write out the vertical retrace end */
    /*TODO*///	outreg[V_RETRACE_END_INDEX].value &= ~0x0f;
    /*TODO*///	outreg[V_RETRACE_END_INDEX].value |= (vreout & 0x0f);
    /*TODO*////* vertical blanking start */
    /*TODO*///	vblksout = ydispout + 1;
    /*TODO*////* check it's legal */
    /*TODO*///	if(vblksout > vreout)
    /*TODO*///		vblksout = vreout;
    /*TODO*////* save the overflow value */
    /*TODO*///	overflowout |= ((vblksout & 0x100) >> 0x05);
    /*TODO*///	maxscanout |= ((vblksout & 0x200) >> 0x04);
    /*TODO*////* write the v blank value out */
    /*TODO*///	outreg[V_BLANKING_START_INDEX].value = (vblksout & 0xff);
    /*TODO*////* vertical blanking end */
    /*TODO*///	vblkeout = vreout + 1;
    /*TODO*////* make sure the blanking fits into our adjusted display size */
    /*TODO*///	if (vblkeout > (ytotalout - 9))
    /*TODO*///		ytotalout = vblkeout + 9;
    /*TODO*////* write out the vertical blanking total */
    /*TODO*///	outreg[V_BLANKING_END_INDEX].value = (vblkeout & 0xff);
    /*TODO*////* update our output overflow */
    /*TODO*///	overflowout |= ((ytotalout & 0x100) >> 0x08) | ((ytotalout & 0x200) >> 0x04);
    /*TODO*////* write out the new vertical total */
    /*TODO*///	outreg[V_TOTAL_INDEX].value = (ytotalout & 0xff);
    /*TODO*///
    /*TODO*////* write out our over flows */
    /*TODO*///	outreg[OVERFLOW_INDEX].value = overflowout;
    /*TODO*////* finally the max scan line */
    /*TODO*///	outreg[MAXIMUM_SCANLINE_INDEX].value = maxscanout;
    /*TODO*////* and we're done */
    /*TODO*///	return outreg;
    /*TODO*///
    /*TODO*///}
    /*TODO*///
    /*TODO*///void center_mode(Register *pReg)
    /*TODO*///{
    /*TODO*///	int center;
    /*TODO*///	int hrt_start, hrt_end, hrt, hblnk_start, hblnk_end;
    /*TODO*///	int vrt_start, vrt_end, vert_total, vert_display, vblnk_start, vrt, vblnk_end;
    /*TODO*////* check for empty array */
    /*TODO*///	if (!pReg)
    /*TODO*///		return;
    /*TODO*////* vertical retrace width */
    /*TODO*///	vrt = 2;
    /*TODO*////* check the clock speed, to work out the retrace width */
    /*TODO*///	if (pReg[CLOCK_INDEX].value == 0xe7)
    /*TODO*///		hrt = 11;
    /*TODO*///	else
    /*TODO*///		hrt = 10;
    /*TODO*////* our center x tweak value */
    /*TODO*///	center = center_x;
    /*TODO*////* check for double width scanline rather than half clock (15.75kHz modes) */
    /*TODO*///	if( pReg[H_TOTAL_INDEX].value > 0x96)
    /*TODO*///	{
    /*TODO*///		center<<=1;
    /*TODO*///		hrt<<=1;
    /*TODO*///	}
    /*TODO*////* set the hz retrace */
    /*TODO*///	hrt_start = pReg[H_RETRACE_START_INDEX].value;
    /*TODO*///	hrt_start += center;
    /*TODO*////* make sure it's legal */
    /*TODO*///	if (hrt_start <= pReg[H_DISPLAY_INDEX].value)
    /*TODO*///		hrt_start = pReg[H_DISPLAY_INDEX].value + 1;
    /*TODO*///	pReg[H_RETRACE_START_INDEX].value = hrt_start;
    /*TODO*////* set hz retrace end */
    /*TODO*///	hrt_end = hrt_start + hrt;
    /*TODO*////* make sure it's legal */
    /*TODO*///	if( hrt_end > pReg[H_TOTAL_INDEX].value)
    /*TODO*///		hrt_end = pReg[H_TOTAL_INDEX].value;
    /*TODO*///
    /*TODO*////* set the hz blanking */
    /*TODO*///	hblnk_start = pReg[H_DISPLAY_INDEX].value + 1;
    /*TODO*////* make sure it's legal */
    /*TODO*///	if (hblnk_start > hrt_start)
    /*TODO*///		hblnk_start = pReg[H_RETRACE_START_INDEX].value;
    /*TODO*///
    /*TODO*///	pReg[H_BLANKING_START_INDEX].value = hblnk_start;
    /*TODO*////* the horizontal blanking end */
    /*TODO*///	hblnk_end = hrt_end + 2;
    /*TODO*////* make sure it's legal */
    /*TODO*///	if( hblnk_end > pReg[H_TOTAL_INDEX].value)
    /*TODO*///		hblnk_end = pReg[H_TOTAL_INDEX].value;
    /*TODO*////* write horizontal blanking - include 7th test bit (always 1) */
    /*TODO*///	pReg[H_BLANKING_END_INDEX].value = (hblnk_end & 0x1f) | 0x80;
    /*TODO*////* include the 5th bit of the horizontal blanking in the horizontal retrace reg. */
    /*TODO*///	hrt_end = ((hrt_end & 0x1f) | ((hblnk_end & 0x20) << 2));
    /*TODO*///	pReg[H_RETRACE_END_INDEX].value = hrt_end;
    /*TODO*///
    /*TODO*///
    /*TODO*////* get the vt retrace */
    /*TODO*///	vrt_start = pReg[V_RETRACE_START_INDEX].value | ((pReg[OVERFLOW_INDEX].value & 0x04) << 6) |
    /*TODO*///				((pReg[OVERFLOW_INDEX].value & 0x80) << 2);
    /*TODO*///
    /*TODO*////* set the new retrace start */
    /*TODO*///	vrt_start += center_y;
    /*TODO*////* check it's legal, get the display line count */
    /*TODO*///	vert_display = (pReg[V_END_INDEX].value | ((pReg[OVERFLOW_INDEX].value & 0x02) << 7) |
    /*TODO*///				((pReg[OVERFLOW_INDEX].value & 0x40) << 3)) + 1;
    /*TODO*///
    /*TODO*///	if (vrt_start < vert_display)
    /*TODO*///		vrt_start = vert_display;
    /*TODO*///
    /*TODO*////* and get the vertical line count */
    /*TODO*///	vert_total = pReg[V_TOTAL_INDEX].value | ((pReg[OVERFLOW_INDEX].value & 0x01) << 8) |
    /*TODO*///				((pReg[OVERFLOW_INDEX].value & 0x20) << 4);
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///	pReg[V_RETRACE_START_INDEX].value = (vrt_start & 0xff);
    /*TODO*///	pReg[OVERFLOW_INDEX].value &= ~0x84;
    /*TODO*///	pReg[OVERFLOW_INDEX].value |= ((vrt_start & 0x100) >> 6);
    /*TODO*///	pReg[OVERFLOW_INDEX].value |= ((vrt_start & 0x200) >> 2);
    /*TODO*///	vrt_end = vrt_start + vrt;
    /*TODO*///
    /*TODO*///
    /*TODO*///	if (vrt_end > vert_total)
    /*TODO*///		vrt_end = vert_total;
    /*TODO*///
    /*TODO*////* write retrace end, include CRT protection and IRQ2 bits */
    /*TODO*///	pReg[V_RETRACE_END_INDEX].value = (vrt_end  & 0x0f) | 0x80 | 0x20;
    /*TODO*///
    /*TODO*////* get the start of vt blanking */
    /*TODO*///	vblnk_start = vert_display + 1;
    /*TODO*////* check it's legal */
    /*TODO*///	if (vblnk_start > vrt_start)
    /*TODO*///		vblnk_start = vrt_start;
    /*TODO*////* and the end */
    /*TODO*///	vblnk_end = vrt_end + 2;
    /*TODO*////* check it's legal */
    /*TODO*///	if (vblnk_end > vert_total)
    /*TODO*///		vblnk_end = vert_total;
    /*TODO*////* set vblank start */
    /*TODO*///	pReg[V_BLANKING_START_INDEX].value = (vblnk_start & 0xff);
    /*TODO*////* write out any overflows */
    /*TODO*///	pReg[OVERFLOW_INDEX].value &= ~0x08;
    /*TODO*///	pReg[OVERFLOW_INDEX].value |= ((vblnk_start & 0x100) >> 5);
    /*TODO*///	pReg[MAXIMUM_SCANLINE_INDEX].value &= ~0x20;
    /*TODO*///	pReg[MAXIMUM_SCANLINE_INDEX].value |= ((vblnk_start &0x200) >> 4);
    /*TODO*////* set the vblank end */
    /*TODO*///	pReg[V_BLANKING_END_INDEX].value = (vblnk_end & 0xff);
    /*TODO*///}
}
