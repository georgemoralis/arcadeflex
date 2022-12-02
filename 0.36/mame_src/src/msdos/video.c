#include "mamalleg.h"
#include "driver.h"
#include <pc.h>
#include <conio.h>
#include <sys/farptr.h>
#include <go32.h>
#include "TwkUser.c"
#include <math.h>
#include "vgafreq.h"
#include "vidhrdw/vector.h"
#include "dirty.h"
/*extra functions for 15.75KHz modes */
#include "gen15khz.h"
#include "ticker.h"


/* function to make scanline mode */
Register *make_scanline_mode(Register *inreg,int entries);

/*15.75KHz SVGA driver (req. for 15.75KHz Arcade Monitor Modes)*/
SVGA15KHZDRIVER *SVGA15KHzdriver;


/* from blit.c, for VGA triple buffering */
extern int xpage_size;
extern int no_xpages;
void unchain_vga(Register *pReg);

static int warming_up;

/* tweak values for centering tweaked modes */
int center_x;
int center_y;

BEGIN_GFX_DRIVER_LIST
	GFX_DRIVER_VGA
	GFX_DRIVER_VESA3
	GFX_DRIVER_VESA2L
	GFX_DRIVER_VESA2B
	GFX_DRIVER_VESA1
END_GFX_DRIVER_LIST

BEGIN_COLOR_DEPTH_LIST
	COLOR_DEPTH_8
	COLOR_DEPTH_15
	COLOR_DEPTH_16
END_COLOR_DEPTH_LIST

#define BACKGROUND 0


dirtygrid grid1;
dirtygrid grid2;
char *dirty_old=grid1;
char *dirty_new=grid2;

void scale_vectorgames(int gfx_width,int gfx_height,int *width,int *height);

void center_mode(Register *pReg);

/* in msdos/sound.c */
int msdos_update_audio(void);



/* specialized update_screen functions defined in blit.c */

/* dirty mode 1 (VIDEO_SUPPORTS_DIRTY) */
void blitscreen_dirty1_vga(void);
void blitscreen_dirty1_unchained_vga(void);
void blitscreen_dirty1_vesa_1x_1x_8bpp(void);
void blitscreen_dirty1_vesa_1x_2x_8bpp(void);
void blitscreen_dirty1_vesa_1x_2xs_8bpp(void);
void blitscreen_dirty1_vesa_2x_1x_8bpp(void);
void blitscreen_dirty1_vesa_2x_2x_8bpp(void);
void blitscreen_dirty1_vesa_2x_2xs_8bpp(void);
void blitscreen_dirty1_vesa_2x_3x_8bpp(void);
void blitscreen_dirty1_vesa_2x_3xs_8bpp(void);
void blitscreen_dirty1_vesa_3x_1x_8bpp(void);
void blitscreen_dirty1_vesa_3x_2x_8bpp(void);
void blitscreen_dirty1_vesa_3x_2xs_8bpp(void);
void blitscreen_dirty1_vesa_3x_3x_8bpp(void);
void blitscreen_dirty1_vesa_3x_3xs_8bpp(void);
void blitscreen_dirty1_vesa_4x_2x_8bpp(void);
void blitscreen_dirty1_vesa_4x_2xs_8bpp(void);
void blitscreen_dirty1_vesa_4x_3x_8bpp(void);
void blitscreen_dirty1_vesa_4x_3xs_8bpp(void);

void blitscreen_dirty1_vesa_1x_1x_16bpp(void);
void blitscreen_dirty1_vesa_1x_2x_16bpp(void);
void blitscreen_dirty1_vesa_1x_2xs_16bpp(void);
void blitscreen_dirty1_vesa_2x_1x_16bpp(void);
void blitscreen_dirty1_vesa_2x_2x_16bpp(void);
void blitscreen_dirty1_vesa_2x_2xs_16bpp(void);
void blitscreen_dirty1_vesa_3x_1x_16bpp(void);
void blitscreen_dirty1_vesa_3x_2x_16bpp(void);
void blitscreen_dirty1_vesa_3x_2xs_16bpp(void);
void blitscreen_dirty1_vesa_4x_2x_16bpp(void);
void blitscreen_dirty1_vesa_4x_2xs_16bpp(void);

void blitscreen_dirty1_vesa_1x_1x_16bpp_palettized(void);
void blitscreen_dirty1_vesa_1x_2x_16bpp_palettized(void);
void blitscreen_dirty1_vesa_1x_2xs_16bpp_palettized(void);
void blitscreen_dirty1_vesa_2x_1x_16bpp_palettized(void);
void blitscreen_dirty1_vesa_2x_2x_16bpp_palettized(void);
void blitscreen_dirty1_vesa_2x_2xs_16bpp_palettized(void);
void blitscreen_dirty1_vesa_3x_1x_16bpp_palettized(void);
void blitscreen_dirty1_vesa_3x_2x_16bpp_palettized(void);
void blitscreen_dirty1_vesa_3x_2xs_16bpp_palettized(void);
void blitscreen_dirty1_vesa_4x_2x_16bpp_palettized(void);
void blitscreen_dirty1_vesa_4x_2xs_16bpp_palettized(void);


/* dirty mode 0 (no osd_mark_dirty calls) */
void blitscreen_dirty0_vga(void);
void blitscreen_dirty0_unchained_vga(void);
void blitscreen_dirty0_vesa_1x_1x_8bpp(void);
void blitscreen_dirty0_vesa_1x_2x_8bpp(void);
void blitscreen_dirty0_vesa_1x_2xs_8bpp(void);
void blitscreen_dirty0_vesa_2x_1x_8bpp(void);
void blitscreen_dirty0_vesa_2x_2x_8bpp(void);
void blitscreen_dirty0_vesa_2x_2xs_8bpp(void);
void blitscreen_dirty0_vesa_2x_3x_8bpp(void);
void blitscreen_dirty0_vesa_2x_3xs_8bpp(void);
void blitscreen_dirty0_vesa_3x_1x_8bpp(void);
void blitscreen_dirty0_vesa_3x_2x_8bpp(void);
void blitscreen_dirty0_vesa_3x_2xs_8bpp(void);
void blitscreen_dirty0_vesa_3x_3x_8bpp(void);
void blitscreen_dirty0_vesa_3x_3xs_8bpp(void);
void blitscreen_dirty0_vesa_4x_2x_8bpp(void);
void blitscreen_dirty0_vesa_4x_2xs_8bpp(void);
void blitscreen_dirty0_vesa_4x_3x_8bpp(void);
void blitscreen_dirty0_vesa_4x_3xs_8bpp(void);

void blitscreen_dirty0_vesa_1x_1x_16bpp(void);
void blitscreen_dirty0_vesa_1x_2x_16bpp(void);
void blitscreen_dirty0_vesa_1x_2xs_16bpp(void);
void blitscreen_dirty0_vesa_2x_1x_16bpp(void);
void blitscreen_dirty0_vesa_2x_2x_16bpp(void);
void blitscreen_dirty0_vesa_2x_2xs_16bpp(void);
void blitscreen_dirty0_vesa_3x_1x_16bpp(void);
void blitscreen_dirty0_vesa_3x_2x_16bpp(void);
void blitscreen_dirty0_vesa_3x_2xs_16bpp(void);
void blitscreen_dirty0_vesa_4x_2x_16bpp(void);
void blitscreen_dirty0_vesa_4x_2xs_16bpp(void);

void blitscreen_dirty0_vesa_1x_1x_16bpp_palettized(void);
void blitscreen_dirty0_vesa_1x_2x_16bpp_palettized(void);
void blitscreen_dirty0_vesa_1x_2xs_16bpp_palettized(void);
void blitscreen_dirty0_vesa_2x_1x_16bpp_palettized(void);
void blitscreen_dirty0_vesa_2x_2x_16bpp_palettized(void);
void blitscreen_dirty0_vesa_2x_2xs_16bpp_palettized(void);
void blitscreen_dirty0_vesa_3x_1x_16bpp_palettized(void);
void blitscreen_dirty0_vesa_3x_2x_16bpp_palettized(void);
void blitscreen_dirty0_vesa_3x_2xs_16bpp_palettized(void);
void blitscreen_dirty0_vesa_4x_2x_16bpp_palettized(void);
void blitscreen_dirty0_vesa_4x_2xs_16bpp_palettized(void);



static void update_screen_dummy(void);
void (*update_screen)(void) = update_screen_dummy;

#define MAX_X_MULTIPLY 4
#define MAX_Y_MULTIPLY 3
#define MAX_X_MULTIPLY16 4
#define MAX_Y_MULTIPLY16 2

static void (*updaters8[MAX_X_MULTIPLY][MAX_Y_MULTIPLY][2][2])(void) =
{			/* 1 x 1 */
	{	{	{ blitscreen_dirty0_vesa_1x_1x_8bpp, blitscreen_dirty1_vesa_1x_1x_8bpp },
			{ blitscreen_dirty0_vesa_1x_1x_8bpp, blitscreen_dirty1_vesa_1x_1x_8bpp }
		},	/* 1 x 2 */
		{	{ blitscreen_dirty0_vesa_1x_2x_8bpp,  blitscreen_dirty1_vesa_1x_2x_8bpp },
			{ blitscreen_dirty0_vesa_1x_2xs_8bpp, blitscreen_dirty1_vesa_1x_2xs_8bpp }
		},	/* 1 x 3 */
		{	{ update_screen_dummy, update_screen_dummy },
			{ update_screen_dummy, update_screen_dummy },
		}
	},		/* 2 x 1 */
	{	{	{ blitscreen_dirty0_vesa_2x_1x_8bpp, blitscreen_dirty1_vesa_2x_1x_8bpp },
			{ blitscreen_dirty0_vesa_2x_1x_8bpp, blitscreen_dirty1_vesa_2x_1x_8bpp }
		},	/* 2 x 2 */
		{	{ blitscreen_dirty0_vesa_2x_2x_8bpp,  blitscreen_dirty1_vesa_2x_2x_8bpp },
			{ blitscreen_dirty0_vesa_2x_2xs_8bpp, blitscreen_dirty1_vesa_2x_2xs_8bpp }
		},	/* 2 x 3 */
		{	{ blitscreen_dirty0_vesa_2x_3x_8bpp,  blitscreen_dirty1_vesa_2x_3x_8bpp },
			{ blitscreen_dirty0_vesa_2x_3xs_8bpp, blitscreen_dirty1_vesa_2x_3xs_8bpp }
		}
	},		/* 3 x 1 */
	{	{	{ blitscreen_dirty0_vesa_3x_1x_8bpp,  blitscreen_dirty1_vesa_3x_1x_8bpp },
			{ update_screen_dummy, update_screen_dummy }
		},	/* 3 x 2 */
		{	{ blitscreen_dirty0_vesa_3x_2x_8bpp,  blitscreen_dirty1_vesa_3x_2x_8bpp },
			{ blitscreen_dirty0_vesa_3x_2xs_8bpp, blitscreen_dirty1_vesa_3x_2xs_8bpp }
		},	/* 3 x 3 */
		{	{ blitscreen_dirty0_vesa_3x_3x_8bpp,  blitscreen_dirty1_vesa_3x_3x_8bpp },
			{ blitscreen_dirty0_vesa_3x_3xs_8bpp, blitscreen_dirty1_vesa_3x_3xs_8bpp }
		}
	},		/* 4 x 1 */
	{	{	{ update_screen_dummy, update_screen_dummy },
			{ update_screen_dummy, update_screen_dummy }
		},	/* 4 x 2 */
		{	{ blitscreen_dirty0_vesa_4x_2x_8bpp,  blitscreen_dirty1_vesa_4x_2x_8bpp },
			{ blitscreen_dirty0_vesa_4x_2xs_8bpp, blitscreen_dirty1_vesa_4x_2xs_8bpp }
		},	/* 4 x 3 */
		{	{ blitscreen_dirty0_vesa_4x_3x_8bpp,  blitscreen_dirty1_vesa_4x_3x_8bpp },
			{ blitscreen_dirty0_vesa_4x_3xs_8bpp, blitscreen_dirty1_vesa_4x_3xs_8bpp }
		}
	}
};

static void (*updaters16[MAX_X_MULTIPLY16][MAX_Y_MULTIPLY16][2][2])(void) =
{				/* 1 x 1 */
	{	{	{ blitscreen_dirty0_vesa_1x_1x_16bpp, blitscreen_dirty1_vesa_1x_1x_16bpp },
			{ blitscreen_dirty0_vesa_1x_1x_16bpp, blitscreen_dirty1_vesa_1x_1x_16bpp }
		},	/* 1 x 2 */
		{	{ blitscreen_dirty0_vesa_1x_2x_16bpp,  blitscreen_dirty1_vesa_1x_2x_16bpp },
			{ blitscreen_dirty0_vesa_1x_2xs_16bpp, blitscreen_dirty1_vesa_1x_2xs_16bpp }
		}
	},		/* 2 x 1 */
	{	{	{ blitscreen_dirty0_vesa_2x_1x_16bpp,  blitscreen_dirty1_vesa_2x_1x_16bpp },
			{ blitscreen_dirty0_vesa_2x_1x_16bpp,  blitscreen_dirty1_vesa_2x_1x_16bpp }
		},	/* 2 x 2 */
		{	{ blitscreen_dirty0_vesa_2x_2x_16bpp,  blitscreen_dirty1_vesa_2x_2x_16bpp },
			{ blitscreen_dirty0_vesa_2x_2xs_16bpp, blitscreen_dirty1_vesa_2x_2xs_16bpp }
		}
	},		/* 3 x 1 */
	{	{	{ blitscreen_dirty0_vesa_3x_1x_16bpp, blitscreen_dirty1_vesa_3x_1x_16bpp },
			{ update_screen_dummy, update_screen_dummy }
		},	/* 3 x 2 */
		{	{ blitscreen_dirty0_vesa_3x_2x_16bpp, blitscreen_dirty1_vesa_3x_2x_16bpp },
			{ blitscreen_dirty0_vesa_3x_2xs_16bpp, blitscreen_dirty1_vesa_3x_2xs_16bpp }
		}
	},		/* 4 x 1 */
	{	{	{ update_screen_dummy, update_screen_dummy },
			{ update_screen_dummy, update_screen_dummy }
		},	/* 4 x 2 */
		{	{ blitscreen_dirty0_vesa_4x_2x_16bpp,  blitscreen_dirty1_vesa_4x_2x_16bpp },
			{ blitscreen_dirty0_vesa_4x_2xs_16bpp, blitscreen_dirty1_vesa_4x_2xs_16bpp }
		}
	}

};

static void (*updaters16_palettized[MAX_X_MULTIPLY16][MAX_Y_MULTIPLY16][2][2])(void) =
{				/* 1 x 1 */
	{	{	{ blitscreen_dirty0_vesa_1x_1x_16bpp_palettized, blitscreen_dirty1_vesa_1x_1x_16bpp_palettized },
			{ blitscreen_dirty0_vesa_1x_1x_16bpp_palettized, blitscreen_dirty1_vesa_1x_1x_16bpp_palettized }
		},	/* 1 x 2 */
		{	{ blitscreen_dirty0_vesa_1x_2x_16bpp_palettized,  blitscreen_dirty1_vesa_1x_2x_16bpp_palettized },
			{ blitscreen_dirty0_vesa_1x_2xs_16bpp_palettized, blitscreen_dirty1_vesa_1x_2xs_16bpp_palettized }
		}
	},		/* 2 x 1 */
	{	{	{ blitscreen_dirty0_vesa_2x_1x_16bpp_palettized,  blitscreen_dirty1_vesa_2x_1x_16bpp_palettized },
			{ blitscreen_dirty0_vesa_2x_1x_16bpp_palettized,  blitscreen_dirty1_vesa_2x_1x_16bpp_palettized }
		},	/* 2 x 2 */
		{	{ blitscreen_dirty0_vesa_2x_2x_16bpp_palettized,  blitscreen_dirty1_vesa_2x_2x_16bpp_palettized },
			{ blitscreen_dirty0_vesa_2x_2xs_16bpp_palettized, blitscreen_dirty1_vesa_2x_2xs_16bpp_palettized }
		}
	},		/* 3 x 1 */
	{	{	{ blitscreen_dirty0_vesa_3x_1x_16bpp_palettized, blitscreen_dirty1_vesa_3x_1x_16bpp_palettized },
			{ update_screen_dummy, update_screen_dummy }
		},	/* 3 x 2 */
		{	{ blitscreen_dirty0_vesa_3x_2x_16bpp_palettized, blitscreen_dirty1_vesa_3x_2x_16bpp_palettized },
			{ blitscreen_dirty0_vesa_3x_2xs_16bpp_palettized, blitscreen_dirty1_vesa_3x_2xs_16bpp_palettized }
		}
	},		/* 4 x 1 */
	{	{	{ update_screen_dummy, update_screen_dummy },
			{ update_screen_dummy, update_screen_dummy }
		},	/* 4 x 2 */
		{	{ blitscreen_dirty0_vesa_4x_2x_16bpp_palettized,  blitscreen_dirty1_vesa_4x_2x_16bpp_palettized },
			{ blitscreen_dirty0_vesa_4x_2xs_16bpp_palettized, blitscreen_dirty1_vesa_4x_2xs_16bpp_palettized }
		}
	}

};

struct osd_bitmap *scrbitmap;
static int modifiable_palette;
static int screen_colors;
static unsigned char *current_palette;
static unsigned int *dirtycolor;
static int dirtypalette;
static int dirty_bright;
static int bright_lookup[256];
extern unsigned int doublepixel[256];
extern unsigned int quadpixel[256]; /* for quadring pixels */
extern UINT32 *palette_16bit_lookup;

int frameskip,autoframeskip;
#define FRAMESKIP_LEVELS 12

/* type of monitor output- */
/* Standard PC, NTSC, PAL or Arcade */
int monitor_type;

int vgafreq;
int always_synced;
int video_sync;
int wait_vsync;
int use_triplebuf;
int triplebuf_pos,triplebuf_page_width;
int vsync_frame_rate;
int skiplines;
int skipcolumns;
int scanlines;
int stretch;
int use_mmx;
int mmxlfb;
int use_tweaked;
int use_vesa;
int use_dirty;
float osd_gamma_correction = 1.0;
int brightness;
float brightness_paused_adjust;
char *resolution;
char *mode_desc;
int gfx_mode;
int gfx_width;
int gfx_height;


/*new 'half' flag (req. for 15.75KHz Arcade Monitor Modes)*/
int half_yres=0;
/* indicates unchained video mode (req. for 15.75KHz Arcade Monitor Modes)*/
int unchained;
/* flags for lowscanrate modes */
int scanrate15KHz;

static int auto_resolution;
static int viswidth;
static int visheight;
static int skiplinesmax;
static int skipcolumnsmax;
static int skiplinesmin;
static int skipcolumnsmin;

static int vector_game;

static Register *reg = 0;       /* for VGA modes */
static int reglen = 0;  /* for VGA modes */
static int videofreq;   /* for VGA modes */

int gfx_xoffset;
int gfx_yoffset;
int gfx_display_lines;
int gfx_display_columns;
static int xmultiply,ymultiply;
int throttle = 1;       /* toggled by F10 */

static int gone_to_gfx_mode;
static int frameskip_counter;
static int frames_displayed;
static TICKER start_time,end_time;    /* to calculate fps average on exit */
#define FRAMES_TO_SKIP 20       /* skip the first few frames from the FPS calculation */
							/* to avoid counting the copyright and info screens */

unsigned char tw224x288_h, tw224x288_v;
unsigned char tw240x256_h, tw240x256_v;
unsigned char tw256x240_h, tw256x240_v;
unsigned char tw256x256_h, tw256x256_v;
unsigned char tw256x256_hor_h, tw256x256_hor_v;
unsigned char tw288x224_h, tw288x224_v;
unsigned char tw240x320_h, tw240x320_v;
unsigned char tw320x240_h, tw320x240_v;
unsigned char tw336x240_h, tw336x240_v;
unsigned char tw384x224_h, tw384x224_v;
unsigned char tw384x240_h, tw384x240_v;
unsigned char tw384x256_h, tw384x256_v;


struct vga_tweak { int x, y; Register *reg; int reglen; int syncvgafreq; int unchained; int vertical_mode; };
struct vga_tweak vga_tweaked[] = {
	{ 240, 256, scr240x256, sizeof(scr240x256)/sizeof(Register),  1, 0, 1 },
	{ 256, 240, scr256x240, sizeof(scr256x240)/sizeof(Register),  0, 0, 0 },
	{ 256, 256, scr256x256, sizeof(scr256x256)/sizeof(Register),  1, 0, 1 },
	{ 256, 256, scr256x256hor, sizeof(scr256x256hor)/sizeof(Register),  0, 0, 0 },
	{ 224, 288, scr224x288, sizeof(scr224x288)/sizeof(Register),  1, 0, 1 },
	{ 288, 224, scr288x224, sizeof(scr288x224)/sizeof(Register),  0, 0, 0 },
	{ 240, 320, scr240x320, sizeof(scr240x320)/sizeof(Register),  1, 1, 1 },
	{ 320, 240, scr320x240, sizeof(scr320x240)/sizeof(Register),  0, 1, 0 },
	{ 336, 240, scr336x240, sizeof(scr336x240)/sizeof(Register),  0, 1, 0 },
	{ 384, 224, scr384x224, sizeof(scr384x224)/sizeof(Register),  1, 1, 0 },
	{ 384, 240, scr384x240, sizeof(scr384x240)/sizeof(Register),  1, 1, 0 },
	{ 384, 256, scr384x256, sizeof(scr384x256)/sizeof(Register),  1, 1, 0 },
	{ 0, 0 }
};
struct mode_adjust  {int x, y; unsigned char *hadjust; unsigned char *vadjust; int vertical_mode; };

/* horizontal and vertical total tweak values for above modes */
struct mode_adjust  pc_adjust[] = {
	{ 240, 256, &tw240x256_h, &tw240x256_v, 1 },
	{ 256, 240, &tw256x240_h, &tw256x240_v, 0 },
	{ 256, 256, &tw256x256_hor_h, &tw256x256_hor_v, 0 },
	{ 256, 256, &tw256x256_h, &tw256x256_v, 1 },
	{ 224, 288, &tw224x288_h, &tw224x288_v, 1 },
	{ 288, 224, &tw288x224_h, &tw288x224_v, 0 },
	{ 240, 320, &tw240x320_h, &tw240x320_v, 1 },
	{ 320, 240, &tw320x240_h, &tw320x240_v, 0 },
	{ 336, 240, &tw336x240_h, &tw336x240_v, 0 },
	{ 384, 224, &tw384x224_h, &tw384x224_v, 0 },
	{ 384, 240, &tw384x240_h, &tw384x240_v, 0 },
	{ 384, 256, &tw384x256_h, &tw384x256_v, 0 },
	{ 0, 0 }
};

/* Tweak values for arcade/ntsc/pal modes */
unsigned char tw224x288arc_h, tw224x288arc_v, tw288x224arc_h, tw288x224arc_v;
unsigned char tw256x240arc_h, tw256x240arc_v, tw256x256arc_h, tw256x256arc_v;
unsigned char tw320x240arc_h, tw320x240arc_v, tw320x256arc_h, tw320x256arc_v;
unsigned char tw352x240arc_h, tw352x240arc_v, tw352x256arc_h, tw352x256arc_v;
unsigned char tw368x224arc_h, tw368x224arc_v;
unsigned char tw368x240arc_h, tw368x240arc_v, tw368x256arc_h, tw368x256arc_v;
unsigned char tw512x224arc_h, tw512x224arc_v, tw512x256arc_h, tw512x256arc_v;
unsigned char tw512x448arc_h, tw512x448arc_v, tw512x512arc_h, tw512x512arc_v;
unsigned char tw640x480arc_h, tw640x480arc_v;

/* 15.75KHz Modes */
struct vga_15KHz_tweak { int x, y; Register *reg; int reglen;
			  int syncvgafreq; int vesa; int ntsc;
			  int half_yres; int matchx; };
struct vga_15KHz_tweak arcade_tweaked[] = {
	{ 224, 288, scr224x288_15KHz, sizeof(scr224x288_15KHz)/sizeof(Register), 0, 0, 0, 0, 224 },
	{ 256, 240, scr256x240_15KHz, sizeof(scr256x240_15KHz)/sizeof(Register), 0, 0, 1, 0, 256 },
	{ 256, 256, scr256x256_15KHz, sizeof(scr256x256_15KHz)/sizeof(Register), 0, 0, 0, 0, 256 },
	{ 288, 224, scr288x224_15KHz, sizeof(scr288x224_15KHz)/sizeof(Register), 0, 0, 1, 0, 288 },
	{ 320, 240, scr320x240_15KHz, sizeof(scr320x240_15KHz)/sizeof(Register), 1, 0, 1, 0, 320 },
	{ 320, 256, scr320x256_15KHz, sizeof(scr320x256_15KHz)/sizeof(Register), 1, 0, 0, 0, 320 },
	{ 352, 240, scr352x240_15KHz, sizeof(scr352x240_15KHz)/sizeof(Register), 1, 0, 1, 0, 352 },
	{ 352, 256, scr352x256_15KHz, sizeof(scr352x256_15KHz)/sizeof(Register), 1, 0, 0, 0, 352 },
/* force 384 games to match to 368 modes - the standard VGA clock speeds mean we can't go as wide as 384 */
	{ 368, 224, scr368x224_15KHz, sizeof(scr368x224_15KHz)/sizeof(Register), 1, 0, 1, 0, 384 },
/* all VGA modes from now on are too big for triple buffering */
	{ 368, 240, scr368x240_15KHz, sizeof(scr368x240_15KHz)/sizeof(Register), 1, 0, 1, 0, 384 },
	{ 368, 256, scr368x256_15KHz, sizeof(scr368x256_15KHz)/sizeof(Register), 1, 0, 0, 0, 384 },
/* double monitor modes */
	{ 512, 224, scr512x224_15KHz, sizeof(scr512x224_15KHz)/sizeof(Register), 0, 0, 1, 0, 512 },
	{ 512, 256, scr512x256_15KHz, sizeof(scr512x256_15KHz)/sizeof(Register), 0, 0, 0, 0, 512 },
/* SVGA Mode (VGA register array not used) */
	{ 640, 480, NULL            , 0                                        , 0, 1, 1, 0, 640 },
/* 'half y' VGA modes, used to fake hires if 'tweaked' is on */
	{ 512, 448, scr512x224_15KHz, sizeof(scr512x224_15KHz)/sizeof(Register), 0, 0, 1, 1, 512 },
	{ 512, 512, scr512x256_15KHz, sizeof(scr512x256_15KHz)/sizeof(Register), 0, 0, 0, 1, 512 },
	{ 0, 0 }
};

/* horizontal and vertical total tweak values for above modes */
struct mode_adjust  arcade_adjust[] = {
	{ 224, 288, &tw224x288arc_h, &tw224x288arc_v, 1 },
	{ 256, 240, &tw256x240arc_h, &tw256x240arc_v, 0 },
	{ 256, 256, &tw256x256arc_h, &tw256x256arc_v, 0 },
	{ 288, 224, &tw288x224arc_h, &tw288x224arc_v, 0 },
	{ 320, 240, &tw320x240arc_h, &tw320x240arc_v, 0 },
	{ 352, 240, &tw352x240arc_h, &tw352x240arc_v, 0 },
	{ 352, 256, &tw352x256arc_h, &tw352x256arc_v, 0 },
	{ 368, 224, &tw368x224arc_h, &tw368x224arc_v, 0 },
	{ 368, 240, &tw368x240arc_h, &tw368x240arc_v, 0 },
	{ 368, 256, &tw368x256arc_h, &tw368x256arc_v, 0 },
	{ 512, 224, &tw512x224arc_h, &tw512x224arc_v, 0 },
	{ 512, 256, &tw512x256arc_h, &tw512x256arc_v, 0 },
	{ 512, 448, &tw512x224arc_h, &tw512x224arc_v, 0 },
	{ 512, 512, &tw512x256arc_h, &tw512x256arc_v, 0 },
	{ 0, 0 }
};

/* Create a bitmap. Also calls osd_clearbitmap() to appropriately initialize */
/* it to the background color. */
/* VERY IMPORTANT: the function must allocate also a "safety area" 16 pixels wide all */
/* around the bitmap. This is required because, for performance reasons, some graphic */
/* routines don't clip at boundaries of the bitmap. */

const int safety = 16;

struct osd_bitmap *osd_new_bitmap(int width,int height,int depth)       /* ASG 980209 */
{
	struct osd_bitmap *bitmap;


	if (Machine->orientation & ORIENTATION_SWAP_XY)
	{
		int temp;

		temp = width;
		width = height;
		height = temp;
	}

	if ((bitmap = malloc(sizeof(struct osd_bitmap))) != 0)
	{
		int i,rowlen,rdwidth;
		unsigned char *bm;


		if (depth != 8 && depth != 16) depth = 8;

		bitmap->depth = depth;
		bitmap->width = width;
		bitmap->height = height;

		rdwidth = (width + 7) & ~7;     /* round width to a quadword */
		if (depth == 16)
			rowlen = 2 * (rdwidth + 2 * safety) * sizeof(unsigned char);
		else
			rowlen =     (rdwidth + 2 * safety) * sizeof(unsigned char);

		if ((bm = malloc((height + 2 * safety) * rowlen)) == 0)
		{
			free(bitmap);
			return 0;
		}

		/* clear ALL bitmap, including safety area, to avoid garbage on right */
		/* side of screen is width is not a multiple of 4 */
		memset(bm,0,(height + 2 * safety) * rowlen);

		if ((bitmap->line = malloc((height + 2 * safety) * sizeof(unsigned char *))) == 0)
		{
			free(bm);
			free(bitmap);
			return 0;
		}

		for (i = 0;i < height + 2 * safety;i++)
		{
			if (depth == 16)
				bitmap->line[i] = &bm[i * rowlen + 2*safety];
			else
				bitmap->line[i] = &bm[i * rowlen + safety];
		}
		bitmap->line += safety;

		bitmap->_private = bm;

		osd_clearbitmap(bitmap);
	}

	return bitmap;
}



/* set the bitmap to black */
void osd_clearbitmap(struct osd_bitmap *bitmap)
{
	int i;


	for (i = 0;i < bitmap->height;i++)
	{
		if (bitmap->depth == 16)
			memset(bitmap->line[i],0,2*bitmap->width);
		else
			memset(bitmap->line[i],BACKGROUND,bitmap->width);
	}


	if (bitmap == scrbitmap)
	{
		extern int bitmap_dirty;        /* in mame.c */

		osd_mark_dirty (0,0,bitmap->width-1,bitmap->height-1,1);
		bitmap_dirty = 1;
	}
}



void osd_free_bitmap(struct osd_bitmap *bitmap)
{
	if (bitmap)
	{
		bitmap->line -= safety;
		free(bitmap->line);
		free(bitmap->_private);
		free(bitmap);
	}
}


void osd_mark_dirty(int _x1, int _y1, int _x2, int _y2, int ui)
{
	if (use_dirty)
	{
		int x, y;

//        if (errorlog) fprintf(errorlog, "mark_dirty %3d,%3d - %3d,%3d\n", _x1,_y1, _x2,_y2);

		_x1 -= skipcolumns;
		_x2 -= skipcolumns;
		_y1 -= skiplines;
		_y2 -= skiplines;

	if (_y1 >= gfx_display_lines || _y2 < 0 || _x1 > gfx_display_columns || _x2 < 0) return;
		if (_y1 < 0) _y1 = 0;
		if (_y2 >= gfx_display_lines) _y2 = gfx_display_lines - 1;
		if (_x1 < 0) _x1 = 0;
		if (_x2 >= gfx_display_columns) _x2 = gfx_display_columns - 1;

		for (y = _y1; y <= _y2 + 15; y += 16)
			for (x = _x1; x <= _x2 + 15; x += 16)
				MARKDIRTY(x,y);
	}
}

static void init_dirty(char dirty)
{
	memset(dirty_new, dirty, MAX_GFX_WIDTH/16 * MAX_GFX_HEIGHT/16);
}

INLINE void swap_dirty(void)
{
    char *tmp;

	tmp = dirty_old;
	dirty_old = dirty_new;
	dirty_new = tmp;
}

/*
 * This function tries to find the best display mode.
 */
static void select_display_mode(int depth)
{
	int width,height, i;

	auto_resolution = 0;
	/* assume unchained video mode  */
	unchained = 0;
	/* see if it's a low scanrate mode */
	switch (monitor_type)
	{
		case MONITOR_TYPE_NTSC:
		case MONITOR_TYPE_PAL:
		case MONITOR_TYPE_ARCADE:
			scanrate15KHz = 1;
			break;
		default:
			scanrate15KHz = 0;
	}

	/* initialise quadring table [useful for *all* doubling modes */
	for (i = 0; i < 256; i++)
	{
		doublepixel[i] = i | (i<<8);
		quadpixel[i] = i | (i<<8) | (i << 16) | (i << 24);
	}

	if (vector_game)
	{
		width = Machine->drv->screen_width;
		height = Machine->drv->screen_height;
	}
	else
	{
		width = Machine->drv->visible_area.max_x - Machine->drv->visible_area.min_x + 1;
		height = Machine->drv->visible_area.max_y - Machine->drv->visible_area.min_y + 1;
	}

	if (Machine->orientation & ORIENTATION_SWAP_XY)
	{
		int temp;

		temp = width;
		width = height;
		height = temp;
	}

	use_vesa = -1;

	/* 16 bit color is supported only by VESA modes */
	if (depth == 16)
	{
		if (errorlog)
			fprintf (errorlog, "Game needs 16-bit colors. Using VESA\n");
		use_tweaked = 0;
		/* only one 15.75KHz VESA mode, so force that */
		if (scanrate15KHz == 1)
		{
			gfx_width = 640;
			gfx_height = 480;
		}
	}


  /* Check for special 15.75KHz mode (req. for 15.75KHz Arcade Modes) */
	if (scanrate15KHz == 1)
	{
		if (errorlog)
		{
			switch (monitor_type)
			{
				case MONITOR_TYPE_NTSC:
					fprintf (errorlog, "Using special NTSC video mode.\n");
					break;
				case MONITOR_TYPE_PAL:
					fprintf (errorlog, "Using special PAL video mode.\n");
					break;
				case MONITOR_TYPE_ARCADE:
					fprintf (errorlog, "Using special arcade monitor mode.\n");
					break;
			}
		}
		scanlines = 0;
		/* if no width/height specified, pick one from our tweaked list */
		if (!gfx_width && !gfx_height)
		{
			for (i=0; arcade_tweaked[i].x != 0; i++)
			{
				/* find height/width fit */
				/* only allow VESA modes if vesa explicitly selected */
				/* only allow PAL / NTSC modes if explicitly selected */
				/* arcade modes cover 50-60Hz) */
				if ((use_tweaked == 0 ||!arcade_tweaked[i].vesa) &&
					(monitor_type == MONITOR_TYPE_ARCADE || /* handles all 15.75KHz modes */
					(arcade_tweaked[i].ntsc && monitor_type == MONITOR_TYPE_NTSC) ||  /* NTSC only */
					(!arcade_tweaked[i].ntsc && monitor_type == MONITOR_TYPE_PAL)) &&  /* PAL ONLY */
					width  <= arcade_tweaked[i].matchx &&
					height <= arcade_tweaked[i].y)

				{
					gfx_width  = arcade_tweaked[i].x;
					gfx_height = arcade_tweaked[i].y;
					break;
				}
			}
			/* if it's a vector, and there's isn't an SVGA support we want to avoid the half modes */
			/* - so force default res. */
			if (vector_game && (use_vesa == 0 || monitor_type == MONITOR_TYPE_PAL))
				gfx_width = 0;

			/* we didn't find a tweaked 15.75KHz mode to fit */
			if (gfx_width == 0)
			{
				/* pick a default resolution for the monitor type */
				/* something with the right refresh rate + an aspect ratio which can handle vectors */
				switch (monitor_type)
				{
					case MONITOR_TYPE_NTSC:
					case MONITOR_TYPE_ARCADE:
						gfx_width = 320; gfx_height = 240;
						break;
					case MONITOR_TYPE_PAL:
						gfx_width = 320; gfx_height = 256;
						break;
				}

				use_vesa = 0;
			}
			else
				use_vesa = arcade_tweaked[i].vesa;
		}

	}


	/* If using tweaked modes, check if there exists one to fit
	   the screen in, otherwise use VESA */
	if (use_tweaked && !gfx_width && !gfx_height)
	{
		for (i=0; vga_tweaked[i].x != 0; i++)
		{
			if (width <= vga_tweaked[i].x &&
				height <= vga_tweaked[i].y)
			{
				/*check for 57Hz modes which would fit into a 60Hz mode*/
				if (gfx_width <= 256 && gfx_height <= 256 &&
					video_sync && Machine->drv->frames_per_second == 57)
				{
					gfx_width = 256;
					gfx_height = 256;
					use_vesa = 0;
					break;
				}

				/* check for correct horizontal/vertical modes */
				if((!vga_tweaked[i].vertical_mode && !(Machine->orientation & ORIENTATION_SWAP_XY)) ||
					(vga_tweaked[i].vertical_mode && (Machine->orientation & ORIENTATION_SWAP_XY)))
				{
					gfx_width  = vga_tweaked[i].x;
					gfx_height = vga_tweaked[i].y;
					use_vesa = 0;
					/* leave the loop on match */

if (gfx_width == 320 && gfx_height == 240 && scanlines == 0)
{
	use_vesa = 1;
	gfx_width = 0;
	gfx_height = 0;
}
					break;
				}
			}
		}
		/* If we didn't find a tweaked VGA mode, use VESA */
		if (gfx_width == 0)
		{
			if (errorlog)
				fprintf (errorlog, "Did not find a tweaked VGA mode. Using VESA.\n");
			use_vesa = 1;
		}
	}


	/* If no VESA resolution has been given, we choose a sensible one. */
	/* 640x480, 800x600 and 1024x768 are common to all VESA drivers. */
	if (!gfx_width && !gfx_height)
	{
		auto_resolution = 1;
		use_vesa = 1;

		/* vector games use 640x480 as default */
		if (vector_game)
		{
			gfx_width = 640;
			gfx_height = 480;
		}
		else
		{
			int xm,ym;

			xm = ym = 1;

			if ((Machine->drv->video_attributes & VIDEO_PIXEL_ASPECT_RATIO_MASK)
					== VIDEO_PIXEL_ASPECT_RATIO_1_2)
			{
				if (Machine->orientation & ORIENTATION_SWAP_XY)
					xm++;
				else ym++;
			}

			if (scanlines && stretch)
			{
				if (ym == 1)
				{
					xm *= 2;
					ym *= 2;
				}

				/* see if pixel doubling can be applied at 640x480 */
				if (ym*height <= 480 && xm*width <= 640 &&
						(xm > 1 || (ym+1)*height > 768 || (xm+1)*width > 1024))
				{
					gfx_width = 640;
					gfx_height = 480;
				}
				/* see if pixel doubling can be applied at 800x600 */
				else if (ym*height <= 600 && xm*width <= 800 &&
						(xm > 1 || (ym+1)*height > 768 || (xm+1)*width > 1024))
				{
					gfx_width = 800;
					gfx_height = 600;
				}
				/* don't use 1024x768 right away. If 512x384 is available, it */
				/* will provide hardware scanlines. */

				if (ym > 1 && xm > 1)
				{
					xm /= 2;
					ym /= 2;
				}
			}

			if (!gfx_width && !gfx_height)
			{
				if (ym*height <= 240 && xm*width <= 320)
				{
					gfx_width = 320;
					gfx_height = 240;
				}
				else if (ym*height <= 300 && xm*width <= 400)
				{
					gfx_width = 400;
					gfx_height = 300;
				}
				else if (ym*height <= 384 && xm*width <= 512)
				{
					gfx_width = 512;
					gfx_height = 384;
				}
				else if (ym*height <= 480 && xm*width <= 640 &&
						(!stretch || (ym+1)*height > 768 || (xm+1)*width > 1024))
				{
					gfx_width = 640;
					gfx_height = 480;
				}
				else if (ym*height <= 600 && xm*width <= 800 &&
						(!stretch || (ym+1)*height > 768 || (xm+1)*width > 1024))
				{
					gfx_width = 800;
					gfx_height = 600;
				}
				else
				{
					gfx_width = 1024;
					gfx_height = 768;
				}
			}
		}
	}
}



/* center image inside the display based on the visual area */
static void adjust_display(int xmin, int ymin, int xmax, int ymax, int depth)
{
	int temp;
	int w,h;
	int act_width;

/* if it's a SVGA arcade monitor mode, get the memory width of the mode */
/* this could be double the width of the actual mode set */
	if (scanrate15KHz && SVGA15KHzdriver && use_vesa == 1)
		act_width = SVGA15KHzdriver->getlogicalwidth (gfx_width);
	else
		act_width = gfx_width;


	if (Machine->orientation & ORIENTATION_SWAP_XY)
	{
		temp = xmin; xmin = ymin; ymin = temp;
		temp = xmax; xmax = ymax; ymax = temp;
		w = Machine->drv->screen_height;
		h = Machine->drv->screen_width;
	}
	else
	{
		w = Machine->drv->screen_width;
		h = Machine->drv->screen_height;
	}

	if (!vector_game)
	{
		if (Machine->orientation & ORIENTATION_FLIP_X)
		{
			temp = w - xmin - 1;
			xmin = w - xmax - 1;
			xmax = temp;
		}
		if (Machine->orientation & ORIENTATION_FLIP_Y)
		{
			temp = h - ymin - 1;
			ymin = h - ymax - 1;
			ymax = temp;
		}
	}

	viswidth  = xmax - xmin + 1;
	visheight = ymax - ymin + 1;


	/* setup xmultiply to handle SVGA driver's (possible) double width */
	xmultiply = act_width / gfx_width;
	ymultiply = 1;

	if (use_vesa && !vector_game)
	{
		if (stretch)
		{
			if (!(Machine->orientation & ORIENTATION_SWAP_XY) &&
					!(Machine->drv->video_attributes & VIDEO_DUAL_MONITOR))
			{
				/* horizontal, non dual monitor games may be stretched at will */
				while ((xmultiply+1) * viswidth <= act_width)
					xmultiply++;
				while ((ymultiply+1) * visheight <= gfx_height)
					ymultiply++;
			}
			else
			{
				int tw,th;

				tw = act_width;
				th = gfx_height;

				if ((Machine->drv->video_attributes & VIDEO_PIXEL_ASPECT_RATIO_MASK)
						== VIDEO_PIXEL_ASPECT_RATIO_1_2)
				{
					if (Machine->orientation & ORIENTATION_SWAP_XY)
						tw /= 2;
					else th /= 2;
				}

				/* Hack for 320x480 and 400x600 "vmame" video modes */
				if ((gfx_width == 320 && gfx_height == 480) ||
						(gfx_width == 400 && gfx_height == 600))
					th /= 2;

				/* maintain aspect ratio for other games */
				while ((xmultiply+1) * viswidth <= tw &&
						(ymultiply+1) * visheight <= th)
				{
					xmultiply++;
					ymultiply++;
				}

				if ((Machine->drv->video_attributes & VIDEO_PIXEL_ASPECT_RATIO_MASK)
						== VIDEO_PIXEL_ASPECT_RATIO_1_2)
				{
					if (Machine->orientation & ORIENTATION_SWAP_XY)
						xmultiply *= 2;
					else ymultiply *= 2;
				}

				/* Hack for 320x480 and 400x600 "vmame" video modes */
				if ((gfx_width == 320 && gfx_height == 480) ||
						(gfx_width == 400 && gfx_height == 600))
					ymultiply *= 2;
			}
		}
		else
		{
			if ((Machine->drv->video_attributes & VIDEO_PIXEL_ASPECT_RATIO_MASK)
					== VIDEO_PIXEL_ASPECT_RATIO_1_2)
			{
				if (Machine->orientation & ORIENTATION_SWAP_XY)
					xmultiply *= 2;
				else ymultiply *= 2;
			}

			/* Hack for 320x480 and 400x600 "vmame" video modes */
			if ((gfx_width == 320 && gfx_height == 480) ||
					(gfx_width == 400 && gfx_height == 600))
				ymultiply *= 2;
		}
	}

	if (depth == 16)
	{
		if (xmultiply > MAX_X_MULTIPLY16) xmultiply = MAX_X_MULTIPLY16;
		if (ymultiply > MAX_Y_MULTIPLY16) ymultiply = MAX_Y_MULTIPLY16;
	}
	else
	{
		if (xmultiply > MAX_X_MULTIPLY) xmultiply = MAX_X_MULTIPLY;
		if (ymultiply > MAX_Y_MULTIPLY) ymultiply = MAX_Y_MULTIPLY;
	}

	gfx_display_lines = visheight;
	gfx_display_columns = viswidth;

	gfx_xoffset = (act_width - viswidth * xmultiply) / 2;
	if (gfx_display_columns > act_width / xmultiply)
		gfx_display_columns = act_width / xmultiply;

	gfx_yoffset = (gfx_height - visheight * ymultiply) / 2;
		if (gfx_display_lines > gfx_height / ymultiply)
			gfx_display_lines = gfx_height / ymultiply;


	skiplinesmin = ymin;
	skiplinesmax = visheight - gfx_display_lines + ymin;
	skipcolumnsmin = xmin;
	skipcolumnsmax = viswidth - gfx_display_columns + xmin;

	/* Align on a quadword !*/
	gfx_xoffset &= ~7;

	/* the skipcolumns from mame.cfg/cmdline is relative to the visible area */
	skipcolumns = xmin + skipcolumns;
	skiplines   = ymin + skiplines;

	/* Just in case the visual area doesn't fit */
	if (gfx_xoffset < 0)
	{
		skipcolumns -= gfx_xoffset;
		gfx_xoffset = 0;
	}
	if (gfx_yoffset < 0)
	{
		skiplines   -= gfx_yoffset;
		gfx_yoffset = 0;
	}

	/* Failsafe against silly parameters */
	if (skiplines < skiplinesmin)
		skiplines = skiplinesmin;
	if (skipcolumns < skipcolumnsmin)
		skipcolumns = skipcolumnsmin;
	if (skiplines > skiplinesmax)
		skiplines = skiplinesmax;
	if (skipcolumns > skipcolumnsmax)
		skipcolumns = skipcolumnsmax;

	if (errorlog)
		fprintf(errorlog,
				"gfx_width = %d gfx_height = %d\n"
				"gfx_xoffset = %d gfx_yoffset = %d\n"
				"xmin %d ymin %d xmax %d ymax %d\n"
				"skiplines %d skipcolumns %d\n"
				"gfx_display_lines %d gfx_display_columns %d\n"
				"xmultiply %d ymultiply %d\n",
				gfx_width,gfx_height,
				gfx_xoffset,gfx_yoffset,
				xmin, ymin, xmax, ymax, skiplines, skipcolumns,gfx_display_lines,gfx_display_columns,xmultiply,ymultiply);

	set_ui_visarea (skipcolumns, skiplines, skipcolumns+gfx_display_columns-1, skiplines+gfx_display_lines-1);

	/* round to a multiple of 4 to avoid missing pixels on the right side */
	gfx_display_columns  = (gfx_display_columns + 3) & ~3;
}



int game_width;
int game_height;
int game_attributes;

/* Create a display screen, or window, large enough to accomodate a bitmap */
/* of the given dimensions. Attributes are the ones defined in driver.h. */
/* Return a osd_bitmap pointer or 0 in case of error. */
struct osd_bitmap *osd_create_display(int width,int height,int depth,int attributes)
{
	if (errorlog)
		fprintf (errorlog, "width %d, height %d\n", width,height);

	brightness = 100;
	brightness_paused_adjust = 1.0;
	dirty_bright = 1;

	if (frameskip < 0) frameskip = 0;
	if (frameskip >= FRAMESKIP_LEVELS) frameskip = FRAMESKIP_LEVELS-1;



	gone_to_gfx_mode = 0;

	/* Look if this is a vector game */
	if (Machine->drv->video_attributes & VIDEO_TYPE_VECTOR)
		vector_game = 1;
	else
		vector_game = 0;


	if (use_dirty == -1)	/* dirty=auto in mame.cfg? */
	{
		/* Is the game using a dirty system? */
		if ((Machine->drv->video_attributes & VIDEO_SUPPORTS_DIRTY) || vector_game)
			use_dirty = 1;
		else
			use_dirty = 0;
	}

	select_display_mode(depth);

	if (vector_game)
	{
		scale_vectorgames(gfx_width,gfx_height,&width, &height);
	}

	game_width = width;
	game_height = height;
	game_attributes = attributes;

	if (depth == 16)
		scrbitmap = osd_new_bitmap(width,height,16);
	else
		scrbitmap = osd_new_bitmap(width,height,8);

	if (!scrbitmap) return 0;

/* find a VESA driver for 15KHz modes just in case we need it later on */
	if (scanrate15KHz)
		getSVGA15KHzdriver (&SVGA15KHzdriver);
	else
		SVGA15KHzdriver = 0;


	if (!osd_set_display(width, height, attributes))
		return 0;

	/* center display based on visible area */
	if (vector_game)
		adjust_display(0, 0, width-1, height-1, depth);
	else
	{
		struct rectangle vis = Machine->drv->visible_area;
		adjust_display(vis.min_x, vis.min_y, vis.max_x, vis.max_y, depth);
	}

   /*Check for SVGA 15.75KHz mode (req. for 15.75KHz Arcade Monitor Modes)
     need to do this here, as the double params will be set up correctly */
	if (use_vesa == 1 && scanrate15KHz)
	{
		int dbl;
		dbl = (ymultiply >= 2);
		/* check that we found a driver */
		if (!SVGA15KHzdriver)
		{
			printf ("\nUnable to find 15.75KHz SVGA driver for %dx%d\n", gfx_width, gfx_height);
			return 0;
		}
		if(errorlog)
			fprintf (errorlog, "Using %s 15.75KHz SVGA driver\n", SVGA15KHzdriver->name);
		/*and try to set the mode */
		if (!SVGA15KHzdriver->setSVGA15KHzmode (dbl, gfx_width, gfx_height))
		{
			printf ("\nUnable to set SVGA 15.75KHz mode %dx%d (driver: %s)\n", gfx_width, gfx_height, SVGA15KHzdriver->name);
			return 0;
		}
		/* if we're doubling, we might as well have scanlines */
		/* the 15.75KHz driver is going to drop every other line anyway -
			so we can avoid drawing them and save some time */
		if(dbl)
			scanlines=1;
	}

    return scrbitmap;
}

/* set the actual display screen but don't allocate the screen bitmap */
int osd_set_display(int width,int height, int attributes)
{
	struct mode_adjust *adjust_array;

	int     i;
	/* moved 'found' to here (req. for 15.75KHz Arcade Monitor Modes) */
	int     found;

	if (!gfx_height || !gfx_width)
	{
		printf("Please specify height AND width (e.g. -640x480)\n");
		return 0;
	}


	if (Machine->orientation & ORIENTATION_SWAP_XY)
	{
		int temp;

		temp = width;
		width = height;
		height = temp;
	}
	/* Mark the dirty buffers as dirty */

	if (use_dirty)
	{
		if (vector_game)
			/* vector games only use one dirty buffer */
			init_dirty (0);
		else
			init_dirty(1);
		swap_dirty();
		init_dirty(1);
	}
	if (dirtycolor)
	{
		for (i = 0;i < screen_colors;i++)
			dirtycolor[i] = 1;
		dirtypalette = 1;
	}
	/* handle special 15.75KHz modes, these now include SVGA modes */
	found = 0;
	/*move video freq set to here, as we need to set it explicitly for the 15.75KHz modes */
	videofreq = vgafreq;

	if (scanrate15KHz == 1)
	{
		/* pick the mode from our 15.75KHz tweaked modes */
		for (i=0; ((arcade_tweaked[i].x != 0) && !found); i++)
		{
			if (gfx_width  == arcade_tweaked[i].x &&
				gfx_height == arcade_tweaked[i].y)
			{
				/* check for SVGA mode with no vesa flag */
				if (arcade_tweaked[i].vesa&& use_vesa == 0)
				{
					printf ("\n %dx%d SVGA 15.75KHz mode only available if tweaked flag is set to 0\n", gfx_width, gfx_height);
					return 0;
				}
				/* check for a NTSC or PAL mode with no arcade flag */
				if (monitor_type != MONITOR_TYPE_ARCADE)
				{
					if (arcade_tweaked[i].ntsc && monitor_type != MONITOR_TYPE_NTSC)
					{
						printf("\n %dx%d 15.75KHz mode only available if -monitor set to 'arcade' or 'ntsc' \n", gfx_width, gfx_height);
						return 0;
					}
					if (!arcade_tweaked[i].ntsc && monitor_type != MONITOR_TYPE_PAL)
					{
						printf("\n %dx%d 15.75KHz mode only available if -monitor set to 'arcade' or 'pal' \n", gfx_width, gfx_height);
						return 0;
					}

				}

				reg = arcade_tweaked[i].reg;
				reglen = arcade_tweaked[i].reglen;
				use_vesa = arcade_tweaked[i].vesa;
				half_yres = arcade_tweaked[i].half_yres;
				/* all 15.75KHz VGA modes are unchained */
				unchained = !use_vesa;

				if (errorlog)
					fprintf (errorlog, "15.75KHz mode (%dx%d) vesa:%d half:%d unchained:%d\n",
										gfx_width, gfx_height, use_vesa, half_yres, unchained);
				/* always use the freq from the structure */
				videofreq = arcade_tweaked[i].syncvgafreq;
				found = 1;
			}
		}
		/* explicitly asked for an 15.75KHz mode which doesn't exist , so inform and exit */
		if (!found)
		{
			printf ("\nNo %dx%d 15.75KHz mode available.\n", gfx_width, gfx_height);
			return 0;
		}
	}

	if (use_vesa != 1 && use_tweaked == 1)
	{

		/* setup tweaked modes */
		/* handle 57Hz games which fit into 60Hz mode */
		if (!found && gfx_width <= 256 && gfx_height <= 256 &&
				video_sync && Machine->drv->frames_per_second == 57)
		{
			found = 1;
			if (!(Machine->orientation & ORIENTATION_SWAP_XY))
			{
				reg = scr256x256hor;
				reglen = sizeof(scr256x256hor)/sizeof(Register);
				videofreq = 0;
			}
			else
			{
				reg = scr256x256;
				reglen = sizeof(scr256x256)/sizeof(Register);
				videofreq = 1;
			}
		}

		/* find the matching tweaked mode */
		for (i=0; ((vga_tweaked[i].x != 0) && !found); i++)
		{
			if (gfx_width  == vga_tweaked[i].x &&
				gfx_height == vga_tweaked[i].y)
			{
				/* check for correct horizontal/vertical modes */

				if((!vga_tweaked[i].vertical_mode && !(Machine->orientation & ORIENTATION_SWAP_XY)) ||
					(vga_tweaked[i].vertical_mode && (Machine->orientation & ORIENTATION_SWAP_XY)))
				{
					reg = vga_tweaked[i].reg;
					reglen = vga_tweaked[i].reglen;
					if (videofreq == -1)
						videofreq = vga_tweaked[i].syncvgafreq;
					found = 1;
					unchained = vga_tweaked[i].unchained;
					if(unchained)
					{
						/* for unchained modes, turn off dirty updates */
						/* as any speed gain is lost in the complex multi-page update needed */
						/* plus - non-dirty updates remove unchained 'shearing' */
						use_dirty = 0;
					}
				}
			}
		}


		/* can't find a VGA mode, use VESA */
		if (found == 0)
		{
			use_vesa = 1;
		}
		else
		{
			use_vesa = 0;
			if (videofreq < 0) videofreq = 0;
			else if (videofreq > 3) videofreq = 3;
		}
	}

	if (use_vesa != 0)
	{
		/*removed local 'found' */
		int mode, bits, err;

		mode = gfx_mode;
		found = 0;
		bits = scrbitmap->depth;

		/* Try the specified vesamode, 565 and 555 for 16 bit color modes, */
		/* doubled resolution in case of noscanlines and if not succesful  */
		/* repeat for all "lower" VESA modes. NS/BW 19980102 */

		while (!found)
		{
			set_color_depth(bits);

			/* allocate a wide enough virtual screen if possible */
			/* we round the width (in dwords) to be an even multiple 256 - that */
			/* way, during page flipping only one byte of the video RAM */
			/* address changes, therefore preventing flickering. */
			if (bits == 8)
				triplebuf_page_width = (gfx_width + 0x3ff) & ~0x3ff;
			else
				triplebuf_page_width = (gfx_width + 0x1ff) & ~0x1ff;

			/* don't ask for a larger screen if triplebuffer not requested - could */
			/* cause problems in some cases. */
			err = 1;
			if (use_triplebuf)
				err = set_gfx_mode(mode,gfx_width,gfx_height,3*triplebuf_page_width,0);
			if (err)
			{
				/* if we're using a SVGA 15KHz driver - tell Allegro the virtual screen width */
				if(SVGA15KHzdriver)
					err = set_gfx_mode(mode,gfx_width,gfx_height,SVGA15KHzdriver->getlogicalwidth(gfx_width),0);
				else
					err = set_gfx_mode(mode,gfx_width,gfx_height,0,0);
			}

			if (errorlog)
			{
				fprintf (errorlog,"Trying ");
				if      (mode == GFX_VESA1)
					fprintf (errorlog, "VESA1");
				else if (mode == GFX_VESA2B)
					fprintf (errorlog, "VESA2B");
				else if (mode == GFX_VESA2L)
				    fprintf (errorlog, "VESA2L");
				else if (mode == GFX_VESA3)
					fprintf (errorlog, "VESA3");
			    fprintf (errorlog, "  %dx%d, %d bit\n",
						gfx_width, gfx_height, bits);
			}

			if (err == 0)
			{
				found = 1;
				/* replace gfx_mode with found mode */
				gfx_mode = mode;
				continue;
			}
			else if (errorlog)
				fprintf (errorlog,"%s\n",allegro_error);

			/* Now adjust parameters for the next loop */

			/* try 5-5-5 in case there is no 5-6-5 16 bit color mode */
			if (scrbitmap->depth == 16)
			{
				if (bits == 16)
				{
					bits = 15;
					continue;
				}
				else
					bits = 16; /* reset to 5-6-5 */
			}

			/* try VESA modes in VESA3-VESA2L-VESA2B-VESA1 order */

			if (mode == GFX_VESA3)
			{
				mode = GFX_VESA2L;
				continue;
			}
			else if (mode == GFX_VESA2L)
			{
				mode = GFX_VESA2B;
				continue;
			}
			else if (mode == GFX_VESA2B)
			{
				mode = GFX_VESA1;
				continue;
			}
			else if (mode == GFX_VESA1)
				mode = gfx_mode; /* restart with the mode given in mame.cfg */

			/* try higher resolutions */
			if (auto_resolution)
			{
				if (stretch && gfx_width <= 512)
				{
					/* low res VESA mode not available, try an high res one */
					gfx_width *= 2;
					gfx_height *= 2;
					continue;
				}

				/* try next higher resolution */
				if (gfx_height < 300 && gfx_width < 400)
				{
					gfx_width = 400;
					gfx_height = 300;
					continue;
				}
				else if (gfx_height < 384 && gfx_width < 512)
				{
					gfx_width = 512;
					gfx_height = 384;
					continue;
				}
				else if (gfx_height < 480 && gfx_width < 640)
				{
					gfx_width = 640;
					gfx_height = 480;
					continue;
				}
				else if (gfx_height < 600 && gfx_width < 800)
				{
					gfx_width = 800;
					gfx_height = 600;
					continue;
				}
				else if (gfx_height < 768 && gfx_width < 1024)
				{
					gfx_width = 1024;
					gfx_height = 768;
					continue;
				}
			}

			/* If there was no continue up to this point, we give up */
			break;
		}

		if (found == 0)
		{
			printf ("\nNo %d-bit %dx%d VESA mode available.\n",
					scrbitmap->depth,gfx_width,gfx_height);
			printf ("\nPossible causes:\n"
"1) Your video card does not support VESA modes at all. Almost all\n"
"   video cards support VESA modes natively these days, so you probably\n"
"   have an older card which needs some driver loaded first.\n"
"   In case you can't find such a driver in the software that came with\n"
"   your video card, Scitech Display Doctor or (for S3 cards) S3VBE\n"
"   are good alternatives.\n"
"2) Your VESA implementation does not support this resolution. For example,\n"
"   '-320x240', '-400x300' and '-512x384' are only supported by a few\n"
"   implementations.\n"
"3) Your video card doesn't support this resolution at this color depth.\n"
"   For example, 1024x768 in 16 bit colors requires 2MB video memory.\n"
"   You can either force an 8 bit video mode ('-depth 8') or use a lower\n"
"   resolution ('-640x480', '-800x600').\n");
			return 0;
		}
		else
		{
			if (errorlog)
				fprintf (errorlog, "Found matching %s mode\n", gfx_driver->desc);
			gfx_mode = mode;
			/* disable triple buffering if the screen is not large enough */
			if (errorlog)
				fprintf (errorlog, "Virtual screen size %dx%d\n",VIRTUAL_W,VIRTUAL_H);
			if (VIRTUAL_W < 3*triplebuf_page_width)
			{
				use_triplebuf = 0;
				if (errorlog)
					fprintf (errorlog, "Triple buffer disabled\n");
			}

			/* if triple buffering is enabled, turn off vsync */
			if (use_triplebuf)
			{
				wait_vsync = 0;
				video_sync = 0;
			}
		}
	}
	else
	{


		/* set the VGA clock */
		if (video_sync || always_synced || wait_vsync)
			reg[0].value = (reg[0].value & 0xf3) | (videofreq << 2);

		/* VGA triple buffering */
		if(use_triplebuf)
		{

			int vga_page_size = (gfx_width * gfx_height);
			/* see if it'll fit */
			if ((vga_page_size * 3) > 0x40000)
			{
				/* too big */
				if (errorlog)
					fprintf(errorlog,"tweaked mode %dx%d is too large to triple buffer\ntriple buffering disabled\n",gfx_width,gfx_height);
				use_triplebuf = 0;
			}
			else
			{
				/* it fits, so set up the 3 pages */
				no_xpages = 3;
				xpage_size = vga_page_size / 4;
				if (errorlog)
					fprintf(errorlog,"unchained VGA triple buffering page size :%d\n",xpage_size);
				/* and make sure the mode's unchained */
				unchain_vga (reg);
				/* triple buffering is enabled, turn off vsync */
				wait_vsync = 0;
				video_sync = 0;
			}
		}
		/* center the mode */
		center_mode (reg);

		/* set the horizontal and vertical total */
		if (scanrate15KHz)
			/* 15.75KHz modes */
			adjust_array = arcade_adjust;
		else
			/* PC monitor modes */
			adjust_array = pc_adjust;

		for (i=0; adjust_array[i].x != 0; i++)
		{
			if ((gfx_width == adjust_array[i].x) && (gfx_height == adjust_array[i].y))
			{
				/* check for 'special vertical' modes */
				if((!adjust_array[i].vertical_mode && !(Machine->orientation & ORIENTATION_SWAP_XY)) ||
					(adjust_array[i].vertical_mode && (Machine->orientation & ORIENTATION_SWAP_XY)))
				{
					reg[H_TOTAL_INDEX].value = *adjust_array[i].hadjust;
					reg[V_TOTAL_INDEX].value = *adjust_array[i].vadjust;
					break;
				}
			}
		}

		/*if scanlines were requested - change the array values to get a scanline mode */
		if (scanlines && !scanrate15KHz)
			reg = make_scanline_mode(reg,reglen);

		/* big hack: open a mode 13h screen using Allegro, then load the custom screen */
		/* definition over it. */
		if (set_gfx_mode(GFX_VGA,320,200,0,0) != 0)
			return 0;

		if (errorlog)
		{
			fprintf(errorlog,"Generated Tweak Values :-\n");
			for (i=0; i<reglen; i++)
			{
				fprintf(errorlog,"{ 0x%02x, 0x%02x, 0x%02x},",reg[i].port,reg[i].index,reg[i].value);
				if (!((i+1)%3))
					fprintf(errorlog,"\n");
			}
		}

		/* tweak the mode */
		outRegArray(reg,reglen);

		/* check for unchained mode,  if unchained clear all pages */
		if (unchained)
		{
			unsigned long address;
			/* clear all 4 bit planes */
			outportw (0x3c4, (0x02 | (0x0f << 0x08)));
			for (address = 0xa0000; address < 0xb0000; address += 4)
				_farpokel(screen->seg, address, 0);
		}
	}


	gone_to_gfx_mode = 1;


	vsync_frame_rate = Machine->drv->frames_per_second;

	if (video_sync)
	{
		TICKER a,b;
		float rate;


		/* wait some time to let everything stabilize */
		for (i = 0;i < 60;i++)
		{
			vsync();
			a = ticker();
		}

		/* small delay for really really fast machines */
		for (i = 0;i < 100000;i++) ;

		vsync();
		b = ticker();

		rate = ((float)TICKS_PER_SEC)/(b-a);

		if (errorlog)
			fprintf(errorlog,"target frame rate = %ffps, video frame rate = %3.2fHz\n",Machine->drv->frames_per_second,rate);

		/* don't allow more than 8% difference between target and actual frame rate */
		while (rate > Machine->drv->frames_per_second * 108 / 100)
			rate /= 2;

		if (rate < Machine->drv->frames_per_second * 92 / 100)
		{
			osd_close_display();
			if (errorlog) fprintf(errorlog,"-vsync option cannot be used with this display mode:\n"
						"video refresh frequency = %dHz, target frame rate = %ffps\n",
						(int)(TICKS_PER_SEC/(b-a)),Machine->drv->frames_per_second);
			return 0;
		}

		if (errorlog) fprintf(errorlog,"adjusted video frame rate = %3.2fHz\n",rate);
			vsync_frame_rate = rate;

		if (Machine->sample_rate)
		{
			Machine->sample_rate = Machine->sample_rate * Machine->drv->frames_per_second / rate;
			if (errorlog)
				fprintf(errorlog,"sample rate adjusted to match video freq: %d\n",Machine->sample_rate);
		}
	}

	warming_up = 1;

	return 1;
}



/* shut up the display */
void osd_close_display(void)
{
	if (gone_to_gfx_mode != 0)
	{
		/* tidy up if 15.75KHz SVGA mode used */
		if (scanrate15KHz && use_vesa == 1)
		{
			/* check we've got a valid driver before calling it */
			if (SVGA15KHzdriver != NULL)
				SVGA15KHzdriver->resetSVGA15KHzmode();
		}

		set_gfx_mode (GFX_TEXT,0,0,0,0);

		if (frames_displayed > FRAMES_TO_SKIP)
			printf("Average FPS: %f\n",(double)TICKS_PER_SEC/(end_time-start_time)*(frames_displayed-FRAMES_TO_SKIP));
	}

	free(dirtycolor);
	dirtycolor = 0;
	free(current_palette);
	current_palette = 0;
	free(palette_16bit_lookup);
	palette_16bit_lookup = 0;
	if (scrbitmap)
	{
		osd_free_bitmap(scrbitmap);
		scrbitmap = NULL;
	}
}



int osd_allocate_colors(unsigned int totalcolors,const unsigned char *palette,unsigned short *pens,int modifiable)
{
	int i;

	modifiable_palette = modifiable;
	screen_colors = totalcolors;
	if (scrbitmap->depth != 8)
		screen_colors += 2;
	else screen_colors = 256;

	dirtycolor = malloc(screen_colors * sizeof(int));
	current_palette = malloc(3 * screen_colors * sizeof(unsigned char));
	palette_16bit_lookup = malloc(screen_colors * sizeof(palette_16bit_lookup[0]));
	if (dirtycolor == 0 || current_palette == 0 || palette_16bit_lookup == 0)
		return 1;

	for (i = 0;i < screen_colors;i++)
		dirtycolor[i] = 1;
	dirtypalette = 1;
	for (i = 0;i < screen_colors;i++)
		current_palette[3*i+0] = current_palette[3*i+1] = current_palette[3*i+2] = 0;

	if (scrbitmap->depth != 8 && modifiable == 0)
	{
		int r,g,b;


		for (i = 0;i < totalcolors;i++)
		{
			r = 255 * brightness * pow(palette[3*i+0] / 255.0, 1 / osd_gamma_correction) / 100;
			g = 255 * brightness * pow(palette[3*i+1] / 255.0, 1 / osd_gamma_correction) / 100;
			b = 255 * brightness * pow(palette[3*i+2] / 255.0, 1 / osd_gamma_correction) / 100;
			*pens++ = makecol(r,g,b);
		}

		Machine->uifont->colortable[0] = makecol(0x00,0x00,0x00);
		Machine->uifont->colortable[1] = makecol(0xff,0xff,0xff);
		Machine->uifont->colortable[2] = makecol(0xff,0xff,0xff);
		Machine->uifont->colortable[3] = makecol(0x00,0x00,0x00);
	}
	else
	{
		if (scrbitmap->depth == 8 && totalcolors >= 255)
		{
			int bestblack,bestwhite;
			int bestblackscore,bestwhitescore;


			bestblack = bestwhite = 0;
			bestblackscore = 3*255*255;
			bestwhitescore = 0;
			for (i = 0;i < totalcolors;i++)
			{
				int r,g,b,score;

				r = palette[3*i+0];
				g = palette[3*i+1];
				b = palette[3*i+2];
				score = r*r + g*g + b*b;

				if (score < bestblackscore)
				{
					bestblack = i;
					bestblackscore = score;
				}
				if (score > bestwhitescore)
				{
					bestwhite = i;
					bestwhitescore = score;
				}
			}

			for (i = 0;i < totalcolors;i++)
				pens[i] = i;

			/* map black to pen 0, otherwise the screen border will not be black */
			pens[bestblack] = 0;
			pens[0] = bestblack;

			Machine->uifont->colortable[0] = pens[bestblack];
			Machine->uifont->colortable[1] = pens[bestwhite];
			Machine->uifont->colortable[2] = pens[bestwhite];
			Machine->uifont->colortable[3] = pens[bestblack];
		}
		else
		{
			/* reserve color 1 for the user interface text */
			current_palette[3*1+0] = current_palette[3*1+1] = current_palette[3*1+2] = 0xff;
			Machine->uifont->colortable[0] = 0;
			Machine->uifont->colortable[1] = 1;
			Machine->uifont->colortable[2] = 1;
			Machine->uifont->colortable[3] = 0;

			/* fill the palette starting from the end, so we mess up badly written */
			/* drivers which don't go through Machine->pens[] */
			for (i = 0;i < totalcolors;i++)
				pens[i] = (screen_colors-1)-i;
		}

		for (i = 0;i < totalcolors;i++)
		{
			current_palette[3*pens[i]+0] = palette[3*i];
			current_palette[3*pens[i]+1] = palette[3*i+1];
			current_palette[3*pens[i]+2] = palette[3*i+2];
		}
	}

	if (use_vesa == 0)
	{
		if (use_dirty) /* supports dirty ? */
		{
			if (unchained)
			{
				update_screen = blitscreen_dirty1_unchained_vga;
				if (errorlog) fprintf (errorlog, "blitscreen_dirty1_unchained_vga\n");
			}
			else
			{
				update_screen = blitscreen_dirty1_vga;
				if (errorlog) fprintf (errorlog, "blitscreen_dirty1_vga\n");
			}
		}
		else
		{
			/* check for unchained modes */
			if (unchained)
			{
				update_screen = blitscreen_dirty0_unchained_vga;
				if (errorlog) fprintf (errorlog, "blitscreen_dirty0_unchained_vga\n");
			}
			else
			{
				update_screen = blitscreen_dirty0_vga;
				if (errorlog) fprintf (errorlog, "blitscreen_dirty0_vga\n");
			}
		}
	}
	else
	{
		if (use_mmx == -1) /* mmx=auto: can new mmx blitters be applied? */
		{
			/* impossible cases follow */
			if (!cpu_mmx)
				mmxlfb = 0;
			else if ((gfx_mode != GFX_VESA2L) && (gfx_mode != GFX_VESA3))
				mmxlfb = 0;
			/* not yet implemented cases follow */
			else if ((xmultiply > 2) || (ymultiply > 2))
				mmxlfb = 0;
			else
				mmxlfb = 1;
		}
		else /* use forced mmx= setting from mame.cfg at own risk!!! */
			mmxlfb = use_mmx;

		if (scrbitmap->depth == 16)
		{
			if (modifiable_palette)
				update_screen = updaters16_palettized[xmultiply-1][ymultiply-1][scanlines?1:0][use_dirty?1:0];
			else
				update_screen = updaters16[xmultiply-1][ymultiply-1][scanlines?1:0][use_dirty?1:0];
		}
		else
		{
			update_screen = updaters8[xmultiply-1][ymultiply-1][scanlines?1:0][use_dirty?1:0];
		}
	}

	return 0;
}


void osd_modify_pen(int pen,unsigned char red, unsigned char green, unsigned char blue)
{
	if (modifiable_palette == 0)
	{
		if (errorlog) fprintf(errorlog,"error: osd_modify_pen() called with modifiable_palette == 0\n");
		return;
	}


	if (	current_palette[3*pen+0] != red ||
			current_palette[3*pen+1] != green ||
			current_palette[3*pen+2] != blue)
	{
		current_palette[3*pen+0] = red;
		current_palette[3*pen+1] = green;
		current_palette[3*pen+2] = blue;

		dirtycolor[pen] = 1;
		dirtypalette = 1;
	}
}



void osd_get_pen(int pen,unsigned char *red, unsigned char *green, unsigned char *blue)
{
	if (scrbitmap->depth != 8 && modifiable_palette == 0)
	{
		*red =   getr(pen);
		*green = getg(pen);
		*blue =  getb(pen);
	}
	else
	{
		*red =   current_palette[3*pen+0];
		*green = current_palette[3*pen+1];
		*blue =  current_palette[3*pen+2];
	}
}



void update_screen_dummy(void)
{
	if (errorlog)
		fprintf(errorlog, "msdos/video.c: undefined update_screen() function for %d x %d!\n",xmultiply,ymultiply);
}

INLINE void pan_display(void)
{
	int pan_changed = 0;

	/* horizontal panning */
	if (input_ui_pressed_repeat(IPT_UI_PAN_LEFT,1))
		if (skipcolumns < skipcolumnsmax)
		{
			skipcolumns++;
			osd_mark_dirty (0,0,scrbitmap->width-1,scrbitmap->height-1,1);
			pan_changed = 1;
		}
	if (input_ui_pressed_repeat(IPT_UI_PAN_RIGHT,1))
		if (skipcolumns > skipcolumnsmin)
		{
			skipcolumns--;
			osd_mark_dirty (0,0,scrbitmap->width-1,scrbitmap->height-1,1);
			pan_changed = 1;
		}
	if (input_ui_pressed_repeat(IPT_UI_PAN_DOWN,1))
		if (skiplines < skiplinesmax)
		{
			skiplines++;
			osd_mark_dirty (0,0,scrbitmap->width-1,scrbitmap->height-1,1);
			pan_changed = 1;
		}
	if (input_ui_pressed_repeat(IPT_UI_PAN_UP,1))
		if (skiplines > skiplinesmin)
		{
			skiplines--;
			osd_mark_dirty (0,0,scrbitmap->width-1,scrbitmap->height-1,1);
			pan_changed = 1;
		}

	if (pan_changed)
	{
		if (use_dirty) init_dirty(1);

		set_ui_visarea (skipcolumns, skiplines, skipcolumns+gfx_display_columns-1, skiplines+gfx_display_lines-1);
	}
}



int osd_skip_this_frame(void)
{
	static const int skiptable[FRAMESKIP_LEVELS][FRAMESKIP_LEVELS] =
	{
		{ 0,0,0,0,0,0,0,0,0,0,0,0 },
		{ 0,0,0,0,0,0,0,0,0,0,0,1 },
		{ 0,0,0,0,0,1,0,0,0,0,0,1 },
		{ 0,0,0,1,0,0,0,1,0,0,0,1 },
		{ 0,0,1,0,0,1,0,0,1,0,0,1 },
		{ 0,1,0,0,1,0,1,0,0,1,0,1 },
		{ 0,1,0,1,0,1,0,1,0,1,0,1 },
		{ 0,1,0,1,1,0,1,0,1,1,0,1 },
		{ 0,1,1,0,1,1,0,1,1,0,1,1 },
		{ 0,1,1,1,0,1,1,1,0,1,1,1 },
		{ 0,1,1,1,1,1,0,1,1,1,1,1 },
		{ 0,1,1,1,1,1,1,1,1,1,1,1 }
	};

	return skiptable[frameskip][frameskip_counter];
}

/* Update the display. */
void osd_update_video_and_audio(void)
{
	static const int waittable[FRAMESKIP_LEVELS][FRAMESKIP_LEVELS] =
	{
		{ 1,1,1,1,1,1,1,1,1,1,1,1 },
		{ 2,1,1,1,1,1,1,1,1,1,1,0 },
		{ 2,1,1,1,1,0,2,1,1,1,1,0 },
		{ 2,1,1,0,2,1,1,0,2,1,1,0 },
		{ 2,1,0,2,1,0,2,1,0,2,1,0 },
		{ 2,0,2,1,0,2,0,2,1,0,2,0 },
		{ 2,0,2,0,2,0,2,0,2,0,2,0 },
		{ 2,0,2,0,0,3,0,2,0,0,3,0 },
		{ 3,0,0,3,0,0,3,0,0,3,0,0 },
		{ 4,0,0,0,4,0,0,0,4,0,0,0 },
		{ 6,0,0,0,0,0,6,0,0,0,0,0 },
		{12,0,0,0,0,0,0,0,0,0,0,0 }
	};
	int i;
	static int showfps,showfpstemp;
	TICKER curr;
	static TICKER prev_measure,this_frame_base,prev;
	static int speed = 100;
	static int vups,vfcount;
	int need_to_clear_bitmap = 0;
	int already_synced;


	if (warming_up)
	{
		/* first time through, initialize timer */
		prev_measure = ticker() - FRAMESKIP_LEVELS * TICKS_PER_SEC/Machine->drv->frames_per_second;
		warming_up = 0;
	}

	if (frameskip_counter == 0)
		this_frame_base = prev_measure + FRAMESKIP_LEVELS * TICKS_PER_SEC/Machine->drv->frames_per_second;

	if (throttle)
	{
		static TICKER last;

		/* if too much time has passed since last sound update, disable throttling */
		/* temporarily - we wouldn't be able to keep synch anyway. */
		curr = ticker();
		if ((curr - last) > 2*TICKS_PER_SEC / Machine->drv->frames_per_second)
			throttle = 0;
		last = curr;

		already_synced = msdos_update_audio();

		throttle = 1;
	}
	else
		already_synced = msdos_update_audio();


	if (osd_skip_this_frame() == 0)
	{
		if (showfpstemp)
		{
			showfpstemp--;
			if (showfps == 0 && showfpstemp == 0)
			{
				need_to_clear_bitmap = 1;
			}
		}


		if (input_ui_pressed(IPT_UI_SHOW_FPS))
		{
			if (showfpstemp)
			{
				showfpstemp = 0;
				need_to_clear_bitmap = 1;
			}
			else
			{
				showfps ^= 1;
				if (showfps == 0)
				{
					need_to_clear_bitmap = 1;
				}
			}
		}


		/* now wait until it's time to update the screen */
		if (throttle)
		{
			profiler_mark(PROFILER_IDLE);
			if (video_sync)
			{
				static TICKER last;


				do
				{
					vsync();
					curr = ticker();
				} while (TICKS_PER_SEC / (curr - last) > Machine->drv->frames_per_second * 11 /10);

				last = curr;
			}
			else
			{
				TICKER target;


				/* wait for video sync but use normal throttling */
				if (wait_vsync)
					vsync();

				curr = ticker();

				if (already_synced == 0)
				{
				/* wait only if the audio update hasn't synced us already */

					target = this_frame_base +
							frameskip_counter * TICKS_PER_SEC/Machine->drv->frames_per_second;

					if (curr - target < 0)
					{
						do
						{
							curr = ticker();
						} while (curr - target < 0);
					}
				}

			}
			profiler_mark(PROFILER_END);
		}
		else curr = ticker();


		/* for the FPS average calculation */
		if (++frames_displayed == FRAMES_TO_SKIP)
			start_time = curr;
		else
			end_time = curr;


		if (frameskip_counter == 0)
		{
			int divdr;


			divdr = Machine->drv->frames_per_second * (curr - prev_measure) / (100 * FRAMESKIP_LEVELS);
			speed = (TICKS_PER_SEC + divdr/2) / divdr;

			prev_measure = curr;
		}

		prev = curr;

		vfcount += waittable[frameskip][frameskip_counter];
		if (vfcount >= Machine->drv->frames_per_second)
		{
			extern int vector_updates; /* avgdvg_go()'s per Mame frame, should be 1 */


			vfcount = 0;
			vups = vector_updates;
			vector_updates = 0;
		}

		if (showfps || showfpstemp)
		{
			int fps;
			char buf[30];
			int divdr;


			divdr = 100 * FRAMESKIP_LEVELS;
			fps = (Machine->drv->frames_per_second * (FRAMESKIP_LEVELS - frameskip) * speed + (divdr / 2)) / divdr;
			sprintf(buf,"%s%2d%4d%%%4d/%d fps",autoframeskip?"auto":"fskp",frameskip,speed,fps,(int)(Machine->drv->frames_per_second+0.5));
			ui_text(buf,Machine->uiwidth-strlen(buf)*Machine->uifontwidth,0);
			if (vector_game)
			{
				sprintf(buf," %d vector updates",vups);
				ui_text(buf,Machine->uiwidth-strlen(buf)*Machine->uifontwidth,Machine->uifontheight);
			}
		}

		if (scrbitmap->depth == 8)
		{
			if (dirty_bright)
			{
				dirty_bright = 0;
				for (i = 0;i < 256;i++)
				{
					float rate = brightness * brightness_paused_adjust * pow(i / 255.0, 1 / osd_gamma_correction) / 100;
					bright_lookup[i] = 63 * rate + 0.5;
				}
			}
			if (dirtypalette)
			{
				dirtypalette = 0;
				for (i = 0;i < screen_colors;i++)
				{
					if (dirtycolor[i])
					{
						RGB adjusted_palette;

						dirtycolor[i] = 0;

						adjusted_palette.r = current_palette[3*i+0];
						adjusted_palette.g = current_palette[3*i+1];
						adjusted_palette.b = current_palette[3*i+2];
						if (i != Machine->uifont->colortable[1])	/* don't adjust the user interface text */
						{
							adjusted_palette.r = bright_lookup[adjusted_palette.r];
							adjusted_palette.g = bright_lookup[adjusted_palette.g];
							adjusted_palette.b = bright_lookup[adjusted_palette.b];
						}
						else
						{
							adjusted_palette.r >>= 2;
							adjusted_palette.g >>= 2;
							adjusted_palette.b >>= 2;
						}
						set_color(i,&adjusted_palette);
					}
				}
			}
		}
		else
		{
			if (dirty_bright)
			{
				dirty_bright = 0;
				for (i = 0;i < 256;i++)
				{
					float rate = brightness * brightness_paused_adjust * pow(i / 255.0, 1 / osd_gamma_correction) / 100;
					bright_lookup[i] = 255 * rate + 0.5;
				}
			}
			if (dirtypalette)
			{
				if (use_dirty) init_dirty(1);	/* have to redraw the whole screen */

				dirtypalette = 0;
				for (i = 0;i < screen_colors;i++)
				{
					if (dirtycolor[i])
					{
						int r,g,b;

						dirtycolor[i] = 0;

						r = current_palette[3*i+0];
						g = current_palette[3*i+1];
						b = current_palette[3*i+2];
						if (i != Machine->uifont->colortable[1])	/* don't adjust the user interface text */
						{
							r = bright_lookup[r];
							g = bright_lookup[g];
							b = bright_lookup[b];
						}
						palette_16bit_lookup[i] = makecol(r,g,b) * 0x10001;
					}
				}
			}
		}

		/* copy the bitmap to screen memory */
		profiler_mark(PROFILER_BLIT);
		update_screen();
		profiler_mark(PROFILER_END);

		/* see if we need to give the card enough time to draw both odd/even fields of the interlaced display
			(req. for 15.75KHz Arcade Monitor Modes */
		interlace_sync();


		if (need_to_clear_bitmap)
			osd_clearbitmap(scrbitmap);

		if (use_dirty)
		{
			if (!vector_game)
				swap_dirty();
			init_dirty(0);
		}

		if (need_to_clear_bitmap)
			osd_clearbitmap(scrbitmap);


		if (throttle && autoframeskip && frameskip_counter == 0)
		{
			static int frameskipadjust;
			int adjspeed;

			/* adjust speed to video refresh rate if vsync is on */
			adjspeed = speed * Machine->drv->frames_per_second / vsync_frame_rate;

			if (adjspeed >= 100)
			{
				frameskipadjust++;
				if (frameskipadjust >= 3)
				{
					frameskipadjust = 0;
					if (frameskip > 0) frameskip--;
				}
			}
			else
			{
				if (adjspeed < 80)
					frameskipadjust -= (90 - adjspeed) / 5;
				else
				{
					/* don't push frameskip too far if we are close to 100% speed */
					if (frameskip < 8)
						frameskipadjust--;
				}

				while (frameskipadjust <= -2)
				{
					frameskipadjust += 2;
					if (frameskip < FRAMESKIP_LEVELS-1) frameskip++;
				}
			}
		}
	}

	/* Check for PGUP, PGDN and pan screen */
	pan_display();

	if (input_ui_pressed(IPT_UI_FRAMESKIP_INC))
	{
		if (autoframeskip)
		{
			autoframeskip = 0;
			frameskip = 0;
		}
		else
		{
			if (frameskip == FRAMESKIP_LEVELS-1)
			{
				frameskip = 0;
				autoframeskip = 1;
			}
			else
				frameskip++;
		}

		if (showfps == 0)
			showfpstemp = 2*Machine->drv->frames_per_second;

		/* reset the frame counter every time the frameskip key is pressed, so */
		/* we'll measure the average FPS on a consistent status. */
		frames_displayed = 0;
	}

	if (input_ui_pressed(IPT_UI_FRAMESKIP_DEC))
	{
		if (autoframeskip)
		{
			autoframeskip = 0;
			frameskip = FRAMESKIP_LEVELS-1;
		}
		else
		{
			if (frameskip == 0)
				autoframeskip = 1;
			else
				frameskip--;
		}

		if (showfps == 0)
			showfpstemp = 2*Machine->drv->frames_per_second;

		/* reset the frame counter every time the frameskip key is pressed, so */
		/* we'll measure the average FPS on a consistent status. */
		frames_displayed = 0;
	}

	if (input_ui_pressed(IPT_UI_THROTTLE))
	{
		throttle ^= 1;

		/* reset the frame counter every time the throttle key is pressed, so */
		/* we'll measure the average FPS on a consistent status. */
		frames_displayed = 0;
	}


	frameskip_counter = (frameskip_counter + 1) % FRAMESKIP_LEVELS;
}



void osd_set_gamma(float _gamma)
{
	int i;

	osd_gamma_correction = _gamma;

	for (i = 0;i < screen_colors;i++)
		dirtycolor[i] = 1;
	dirtypalette = 1;
	dirty_bright = 1;
}

float osd_get_gamma(void)
{
	return osd_gamma_correction;
}

/* brightess = percentage 0-100% */
void osd_set_brightness(int _brightness)
{
	int i;

	brightness = _brightness;

	for (i = 0;i < screen_colors;i++)
		dirtycolor[i] = 1;
	dirtypalette = 1;
	dirty_bright = 1;
}

int osd_get_brightness(void)
{
	return brightness;
}


void osd_save_snapshot(void)
{
	save_screen_snapshot();
}

void osd_pause(int paused)
{
	int i;

	if (paused) brightness_paused_adjust = 0.65;
	else brightness_paused_adjust = 1.0;

	for (i = 0;i < screen_colors;i++)
		dirtycolor[i] = 1;
	dirtypalette = 1;
	dirty_bright = 1;
}

Register *make_scanline_mode(Register *inreg,int entries)
{
	static Register outreg[32];
	int maxscan,maxscanout;
	int overflow,overflowout;
	int ytotalin,ytotalout;
	int ydispin,ydispout;
	int vrsin,vrsout,vreout,vblksout,vblkeout;
/* first - check's it not already a 'non doubled' line mode */
	maxscan = inreg[MAXIMUM_SCANLINE_INDEX].value;
	if ((maxscan & 1) == 0)
	/* it is, so just return the array as is */
  		return inreg;
/* copy across our standard display array */
	memcpy (&outreg, inreg, entries * sizeof(Register));
/* keep hold of the overflow register - as we'll need to refer to it a lot */
	overflow = inreg[OVERFLOW_INDEX].value;
/* set a large line compare value  - as we won't be doing any split window scrolling etc.*/
	maxscanout = 0x40;
/* half all the y values */
/* total */
	ytotalin = inreg[V_TOTAL_INDEX].value;
	ytotalin |= ((overflow & 1)<<0x08) | ((overflow & 0x20)<<0x04);
    ytotalout = ytotalin >> 1;
/* display enable end */
	ydispin = inreg[13].value | ((overflow & 0x02)<< 0x07) | ((overflow & 0x040) << 0x03);
	ydispin ++;
	ydispout = ydispin >> 1;
	ydispout --;
	overflowout = ((ydispout & 0x100) >> 0x07) | ((ydispout && 0x200) >> 0x03);
	outreg[V_END_INDEX].value = (ydispout & 0xff);
/* avoid top over scan */
	if ((ytotalin - ydispin) < 40 && !center_y)
	{
  		vrsout = ydispout;
		/* give ourselves a scanline cushion */
		ytotalout += 2;
	}
  	else
	{
/* vertical retrace start */
		vrsin = inreg[V_RETRACE_START_INDEX].value | ((overflow & 0x04)<<0x06) | ((overflow & 0x80)<<0x02);
		vrsout = vrsin >> 1;
	}
/* check it's legal */
	if (vrsout < ydispout)
		vrsout = ydispout;
/*update our output overflow */
	overflowout |= (((vrsout & 0x100) >> 0x06) | ((vrsout & 0x200) >> 0x02));
	outreg[V_RETRACE_START_INDEX].value = (vrsout & 0xff);
/* vertical retrace end */
	vreout = vrsout + 2;
/* make sure the retrace fits into our adjusted display size */
	if (vreout > (ytotalout - 9))
		ytotalout = vreout + 9;
/* write out the vertical retrace end */
	outreg[V_RETRACE_END_INDEX].value &= ~0x0f;
	outreg[V_RETRACE_END_INDEX].value |= (vreout & 0x0f);
/* vertical blanking start */
	vblksout = ydispout + 1;
/* check it's legal */
	if(vblksout > vreout)
		vblksout = vreout;
/* save the overflow value */
	overflowout |= ((vblksout & 0x100) >> 0x05);
	maxscanout |= ((vblksout & 0x200) >> 0x04);
/* write the v blank value out */
	outreg[V_BLANKING_START_INDEX].value = (vblksout & 0xff);
/* vertical blanking end */
	vblkeout = vreout + 1;
/* make sure the blanking fits into our adjusted display size */
	if (vblkeout > (ytotalout - 9))
		ytotalout = vblkeout + 9;
/* write out the vertical blanking total */
	outreg[V_BLANKING_END_INDEX].value = (vblkeout & 0xff);
/* update our output overflow */
	overflowout |= ((ytotalout & 0x100) >> 0x08) | ((ytotalout & 0x200) >> 0x04);
/* write out the new vertical total */
	outreg[V_TOTAL_INDEX].value = (ytotalout & 0xff);

/* write out our over flows */
	outreg[OVERFLOW_INDEX].value = overflowout;
/* finally the max scan line */
	outreg[MAXIMUM_SCANLINE_INDEX].value = maxscanout;
/* and we're done */
	return outreg;

}

void center_mode(Register *pReg)
{
	int center;
	int hrt_start, hrt_end, hrt, hblnk_start, hblnk_end;
	int vrt_start, vrt_end, vert_total, vert_display, vblnk_start, vrt, vblnk_end;
/* check for empty array */
	if (!pReg)
		return;
/* vertical retrace width */
	vrt = 2;
/* check the clock speed, to work out the retrace width */
	if (pReg[CLOCK_INDEX].value == 0xe7)
		hrt = 11;
	else
		hrt = 10;
/* our center x tweak value */
	center = center_x;
/* check for double width scanline rather than half clock (15.75kHz modes) */
	if( pReg[H_TOTAL_INDEX].value > 0x96)
	{
		center<<=1;
		hrt<<=1;
	}
/* set the hz retrace */
	hrt_start = pReg[H_RETRACE_START_INDEX].value;
	hrt_start += center;
/* make sure it's legal */
	if (hrt_start <= pReg[H_DISPLAY_INDEX].value)
		hrt_start = pReg[H_DISPLAY_INDEX].value + 1;
	pReg[H_RETRACE_START_INDEX].value = hrt_start;
/* set hz retrace end */
	hrt_end = hrt_start + hrt;
/* make sure it's legal */
	if( hrt_end > pReg[H_TOTAL_INDEX].value)
		hrt_end = pReg[H_TOTAL_INDEX].value;

/* set the hz blanking */
	hblnk_start = pReg[H_DISPLAY_INDEX].value + 1;
/* make sure it's legal */
	if (hblnk_start > hrt_start)
		hblnk_start = pReg[H_RETRACE_START_INDEX].value;

	pReg[H_BLANKING_START_INDEX].value = hblnk_start;
/* the horizontal blanking end */
	hblnk_end = hrt_end + 2;
/* make sure it's legal */
	if( hblnk_end > pReg[H_TOTAL_INDEX].value)
		hblnk_end = pReg[H_TOTAL_INDEX].value;
/* write horizontal blanking - include 7th test bit (always 1) */
	pReg[H_BLANKING_END_INDEX].value = (hblnk_end & 0x1f) | 0x80;
/* include the 5th bit of the horizontal blanking in the horizontal retrace reg. */
	hrt_end = ((hrt_end & 0x1f) | ((hblnk_end & 0x20) << 2));
	pReg[H_RETRACE_END_INDEX].value = hrt_end;


/* get the vt retrace */
	vrt_start = pReg[V_RETRACE_START_INDEX].value | ((pReg[OVERFLOW_INDEX].value & 0x04) << 6) |
				((pReg[OVERFLOW_INDEX].value & 0x80) << 2);

/* set the new retrace start */
	vrt_start += center_y;
/* check it's legal, get the display line count */
	vert_display = (pReg[V_END_INDEX].value | ((pReg[OVERFLOW_INDEX].value & 0x02) << 7) |
				((pReg[OVERFLOW_INDEX].value & 0x40) << 3)) + 1;

	if (vrt_start < vert_display)
		vrt_start = vert_display;

/* and get the vertical line count */
	vert_total = pReg[V_TOTAL_INDEX].value | ((pReg[OVERFLOW_INDEX].value & 0x01) << 8) |
				((pReg[OVERFLOW_INDEX].value & 0x20) << 4);



	pReg[V_RETRACE_START_INDEX].value = (vrt_start & 0xff);
	pReg[OVERFLOW_INDEX].value &= ~0x84;
	pReg[OVERFLOW_INDEX].value |= ((vrt_start & 0x100) >> 6);
	pReg[OVERFLOW_INDEX].value |= ((vrt_start & 0x200) >> 2);
	vrt_end = vrt_start + vrt;


	if (vrt_end > vert_total)
		vrt_end = vert_total;

/* write retrace end, include CRT protection and IRQ2 bits */
	pReg[V_RETRACE_END_INDEX].value = (vrt_end  & 0x0f) | 0x80 | 0x20;

/* get the start of vt blanking */
	vblnk_start = vert_display + 1;
/* check it's legal */
	if (vblnk_start > vrt_start)
		vblnk_start = vrt_start;
/* and the end */
	vblnk_end = vrt_end + 2;
/* check it's legal */
	if (vblnk_end > vert_total)
		vblnk_end = vert_total;
/* set vblank start */
	pReg[V_BLANKING_START_INDEX].value = (vblnk_start & 0xff);
/* write out any overflows */
	pReg[OVERFLOW_INDEX].value &= ~0x08;
	pReg[OVERFLOW_INDEX].value |= ((vblnk_start & 0x100) >> 5);
	pReg[MAXIMUM_SCANLINE_INDEX].value &= ~0x20;
	pReg[MAXIMUM_SCANLINE_INDEX].value |= ((vblnk_start &0x200) >> 4);
/* set the vblank end */
	pReg[V_BLANKING_END_INDEX].value = (vblnk_end & 0xff);
}

