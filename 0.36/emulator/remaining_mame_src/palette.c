/******************************************************************************

  palette.c

  Palette handling functions.

  There are several levels of abstraction in the way MAME handles the palette,
  and several display modes which can be used by the drivers.

  Palette
  -------
  Note: in the following text, "color" refers to a color in the emulated
  game's virtual palette. For example, a game might be able to display 1024
  colors at the same time. If the game uses RAM to change the available
  colors, the term "palette" refers to the colors available at any given time,
  not to the whole range of colors which can be produced by the hardware. The
  latter is referred to as "color space".
  The term "pen" refers to one of the maximum MAX_PENS colors that can be
  used to generate the display. PC users might want to think of them as the
  colors available in VGA, but be warned: the mapping of MAME pens to the VGA
  registers is not 1:1, so MAME's pen 10 will not necessarily be mapped to
  VGA's color #10 (actually this is never the case). This is done to ensure
  portability, since on some systems it is not possible to do a 1:1 mapping.

  So, to summarize, the three layers of palette abstraction are:

  P1) The game virtual palette (the "colors")
  P2) MAME's MAX_PENS colors palette (the "pens")
  P3) The OS specific hardware color registers (the "OS specific pens")

  The array Machine->pens is a lookup table which maps game colors to OS
  specific pens (P1 to P3). When you are working on bitmaps at the pixel level,
  *always* use Machine->pens to map the color numbers. *Never* use constants.
  For example if you want to make pixel (x,y) of color 3, do:
  bitmap->line[y][x] = Machine->pens[3];


  Lookup table
  ------------
  Palettes are only half of the story. To map the gfx data to colors, the
  graphics routines use a lookup table. For example if we have 4bpp tiles,
  which can have 256 different color codes, the lookup table for them will have
  256 * 2^4 = 4096 elements. For games using a palette RAM, the lookup table is
  usually a 1:1 map. For games using PROMs, the lookup table is often larger
  than the palette itself so for example the game can display 256 colors out
  of a palette of 16.

  The palette and the lookup table are initialized to default values by the
  main core, but can be initialized by the driver using the function
  MachineDriver->vh_init_palette(). For games using palette RAM, that
  function is usually not needed, and the lookup table can be set up by
  properly initializing the color_codes_start and total_color_codes fields in
  the GfxDecodeInfo array.
  When vh_init_palette() initializes the lookup table, it maps gfx codes
  to game colors (P1 above). The lookup table will be converted by the core to
  map to OS specific pens (P3 above), and stored in Machine->remapped_colortable.


  Display modes
  -------------
  The available display modes can be summarized in four categories:
  1) Static palette. Use this for games which use PROMs for color generation.
     The palette is initialized by vh_init_palette(), and never changed
     again.
  2) Dynamic palette. Use this for games which use RAM for color generation and
     have no more than MAX_PENS colors in the palette. The palette can be
     dynamically modified by the driver using the function
     palette_change_color(). MachineDriver->video_attributes must contain the
     flag VIDEO_MODIFIES_PALETTE.
  3) Dynamic shrinked palette. Use this for games which use RAM for color
     generation and have more than MAX_PENS colors in the palette. The palette
	 can be dynamically modified by the driver using the function
     palette_change_color(). MachineDriver->video_attributes must contain the
     flag VIDEO_MODIFIES_PALETTE.
     The difference with case 2) above is that the driver must do some
     additional work to allow for palette reduction without loss of quality.
     The function palette_recalc() must be called every frame before doing any
     rendering. The palette_used_colors array can be changed to precisely
     indicate to the function which of the game colors are used, so it can pick
     only the needed colors, and make the palette fit into MAX_PENS colors.
	 Colors can also be marked as "transparent".
     The return code of palette_recalc() tells the driver whether the lookup
     table has changed, and therefore whether a screen refresh is needed. Note
     that this only applies to colors which were used in the previous frame:
     that's why palette_recalc() must be called before ANY rendering takes
     place.
  4) 16-bit color. This should only be used for games which use more than
     MAX_PENS colors at a time. It is slower than the other modes, so it should
	 be avoided whenever possible. Transparency support is limited.
     MachineDriver->video_attributes must contain VIDEO_MODIFIES_PALETTE, and
	 GameDriver->flags GAME_REQUIRES_16BIT.

  The dynamic shrinking of the palette works this way: as colors are requested,
  they are associated to a pen. When a color is no longer needed, the pen is
  freed and can be used for another color. When the code runs out of free pens,
  it compacts the palette, putting together colors with the same RGB
  components, then starts again to allocate pens for each new color. The bottom
  line of all this is that the pen assignment will automatically adapt to the
  game needs, and colors which change often will be assigned an exclusive pen,
  which can be modified using the video cards hardware registers without need
  for a screen refresh.
  The important difference between cases 3) and 4) is that in 3), color cycling
  (which many games use) is essentially free, while in 4) every palette change
  requires a screen refresh. The color quality in 3) is also better than in 4)
  if the game uses more than 5 bits per color component. For testing purposes,
  you can switch between the two modes by just adding/removing the
  GAME_REQUIRES_16BIT flag (but be warned about the limited transparency
  support in 16-bit mode).

******************************************************************************/

#include "driver.h"


#define VERBOSE 0


static unsigned char *game_palette;	/* RGB palette as set by the driver. */
static unsigned char *new_palette;	/* changes to the palette are stored here before */
							/* being moved to game_palette by palette_recalc() */
static unsigned char *palette_dirty;
/* arrays which keep track of colors actually used, to help in the palette shrinking. */
unsigned char *palette_used_colors;
static unsigned char *old_used_colors;
static int *pen_visiblecount,*pen_cachedcount;
static unsigned char *just_remapped;	/* colors which have been remapped in this frame, */
										/* returned by palette_recalc() */

static int use_16bit;
#define NO_16BIT			0
#define STATIC_16BIT		1
#define PALETTIZED_16BIT	2

static int total_shrinked_pens;
unsigned short *shrinked_pens;
static unsigned char *shrinked_palette;
static unsigned short *palette_map;	/* map indexes from game_palette to shrinked_palette */
static unsigned short pen_usage_count[DYNAMIC_MAX_PENS];

unsigned short palette_transparent_pen;
int palette_transparent_color;


#define BLACK_PEN		0
#define TRANSPARENT_PEN	1
#define RESERVED_PENS	2

#define PALETTE_COLOR_NEEDS_REMAP 0x80

/* helper macro for 16-bit mode */
#define rgbpenindex(r,g,b) ((Machine->scrbitmap->depth==16) ? ((((r)>>3)<<10)+(((g)>>3)<<5)+((b)>>3)) : ((((r)>>5)<<5)+(((g)>>5)<<2)+((b)>>6)))


unsigned short *palette_shadow_table;
unsigned short *palette_highlight_table;



int palette_start(void)
{
	int i,num;


	game_palette = malloc(3 * Machine->drv->total_colors * sizeof(unsigned char));
	palette_map = malloc(Machine->drv->total_colors * sizeof(unsigned short));
	if (Machine->drv->color_table_len)
	{
		Machine->game_colortable = malloc(Machine->drv->color_table_len * sizeof(unsigned short));
		Machine->remapped_colortable = malloc(Machine->drv->color_table_len * sizeof(unsigned short));
	}
	else Machine->game_colortable = Machine->remapped_colortable = 0;

	if (Machine->color_depth == 16 || (Machine->gamedrv->flags & GAME_REQUIRES_16BIT))
	{
		if (Machine->color_depth == 8 || Machine->drv->total_colors > 65532)
			use_16bit = STATIC_16BIT;
		else
			use_16bit = PALETTIZED_16BIT;
	}
	else
		use_16bit = NO_16BIT;

	switch (use_16bit)
	{
		case NO_16BIT:
			if (Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE)
				total_shrinked_pens = DYNAMIC_MAX_PENS;
			else
				total_shrinked_pens = STATIC_MAX_PENS;
			break;
		case STATIC_16BIT:
			total_shrinked_pens = 32768;
			break;
		case PALETTIZED_16BIT:
			total_shrinked_pens = Machine->drv->total_colors + RESERVED_PENS;
			break;
	}

	shrinked_pens = malloc(total_shrinked_pens * sizeof(short));
	shrinked_palette = malloc(3 * total_shrinked_pens * sizeof(unsigned char));

	Machine->pens = malloc(Machine->drv->total_colors * sizeof(short));

	if ((Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE))
	{
		/* if the palette changes dynamically, */
		/* we'll need the usage arrays to help in shrinking. */
		palette_used_colors = malloc((1+1+1+3+1) * Machine->drv->total_colors * sizeof(unsigned char));
		pen_visiblecount = malloc(2 * Machine->drv->total_colors * sizeof(int));

		if (palette_used_colors == 0 || pen_visiblecount == 0)
		{
			palette_stop();
			return 1;
		}

		old_used_colors = palette_used_colors + Machine->drv->total_colors * sizeof(unsigned char);
		just_remapped = old_used_colors + Machine->drv->total_colors * sizeof(unsigned char);
		new_palette = just_remapped + Machine->drv->total_colors * sizeof(unsigned char);
		palette_dirty = new_palette + 3*Machine->drv->total_colors * sizeof(unsigned char);
		memset(palette_used_colors,PALETTE_COLOR_USED,Machine->drv->total_colors * sizeof(unsigned char));
		memset(old_used_colors,PALETTE_COLOR_UNUSED,Machine->drv->total_colors * sizeof(unsigned char));
		memset(palette_dirty,0,Machine->drv->total_colors * sizeof(unsigned char));
		pen_cachedcount = pen_visiblecount + Machine->drv->total_colors;
		memset(pen_visiblecount,0,Machine->drv->total_colors * sizeof(int));
		memset(pen_cachedcount,0,Machine->drv->total_colors * sizeof(int));
	}
	else palette_used_colors = old_used_colors = just_remapped = new_palette = palette_dirty = 0;

	if (Machine->color_depth == 8) num = 256;
	else num = 65536;
	palette_shadow_table = malloc(2 * num * sizeof(unsigned short));
	if (palette_shadow_table == 0)
	{
		palette_stop();
		return 1;
	}
	palette_highlight_table = palette_shadow_table + num;
	for (i = 0;i < num;i++)
		palette_shadow_table[i] = palette_highlight_table[i] = i;

	if ((Machine->drv->color_table_len && (Machine->game_colortable == 0 || Machine->remapped_colortable == 0))
			|| game_palette == 0 ||	palette_map == 0
			|| shrinked_pens == 0 || shrinked_palette == 0 || Machine->pens == 0)
	{
		palette_stop();
		return 1;
	}

	return 0;
}

void palette_stop(void)
{
	free(palette_used_colors);
	palette_used_colors = old_used_colors = just_remapped = new_palette = palette_dirty = 0;
	free(pen_visiblecount);
	pen_visiblecount = 0;
	free(game_palette);
	game_palette = 0;
	free(palette_map);
	palette_map = 0;
	free(Machine->game_colortable);
	Machine->game_colortable = 0;
	free(Machine->remapped_colortable);
	Machine->remapped_colortable = 0;
	free(shrinked_pens);
	shrinked_pens = 0;
	free(shrinked_palette);
	shrinked_palette = 0;
	free(Machine->pens);
	Machine->pens = 0;
	free(palette_shadow_table);
	palette_shadow_table = 0;
}



int palette_init(void)
{
	int i;


	/* We initialize the palette and colortable to some default values so that */
	/* drivers which dynamically change the palette don't need a vh_init_palette() */
	/* function (provided the default color table fits their needs). */

	for (i = 0;i < Machine->drv->total_colors;i++)
	{
		game_palette[3*i + 0] = ((i & 1) >> 0) * 0xff;
		game_palette[3*i + 1] = ((i & 2) >> 1) * 0xff;
		game_palette[3*i + 2] = ((i & 4) >> 2) * 0xff;
	}

	/* Preload the colortable with a default setting, following the same */
	/* order of the palette. The driver can overwrite this in */
	/* vh_init_palette() */
	for (i = 0;i < Machine->drv->color_table_len;i++)
		Machine->game_colortable[i] = i % Machine->drv->total_colors;

	/* by default we use -1 to identify the transparent color, the driver */
	/* can modify this. */
	palette_transparent_color = -1;

	/* now the driver can modify the default values if it wants to. */
	if (Machine->drv->vh_init_palette)
		(*Machine->drv->vh_init_palette)(game_palette,Machine->game_colortable,memory_region(REGION_PROMS));


	switch (use_16bit)
	{
		case NO_16BIT:
		{
			/* initialize shrinked palette to all black */
			for (i = 0;i < total_shrinked_pens;i++)
			{
				shrinked_palette[3*i + 0] =
				shrinked_palette[3*i + 1] =
				shrinked_palette[3*i + 2] = 0;
			}

			if (Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE)
			{
				/* initialize pen usage counters */
				for (i = 0;i < DYNAMIC_MAX_PENS;i++)
					pen_usage_count[i] = 0;

				/* allocate two fixed pens at the beginning: */
				/* transparent black */
				pen_usage_count[TRANSPARENT_PEN] = 1;	/* so the pen will not be reused */

				/* non transparent black */
				pen_usage_count[BLACK_PEN] = 1;

				/* create some defaults associations of game colors to shrinked pens. */
				/* They will be dynamically modified at run time. */
				for (i = 0;i < Machine->drv->total_colors;i++)
					palette_map[i] = (i & 7) + 8;

				if (osd_allocate_colors(total_shrinked_pens,shrinked_palette,shrinked_pens,1))
					return 1;
			}
			else
			{
				int j,used;


if (errorlog) fprintf(errorlog,"shrinking %d colors palette...\n",Machine->drv->total_colors);

				/* shrink palette to fit */
				used = 0;

				for (i = 0;i < Machine->drv->total_colors;i++)
				{
					for (j = 0;j < used;j++)
					{
						if (	shrinked_palette[3*j + 0] == game_palette[3*i + 0] &&
								shrinked_palette[3*j + 1] == game_palette[3*i + 1] &&
								shrinked_palette[3*j + 2] == game_palette[3*i + 2])
							break;
					}

					palette_map[i] = j;

					if (j == used)
					{
						used++;
						if (used > total_shrinked_pens)
						{
							used = total_shrinked_pens;
							palette_map[i] = total_shrinked_pens-1;
							usrintf_showmessage("cannot shrink static palette");
if (errorlog) fprintf(errorlog,"error: ran out of free pens to shrink the palette.\n");
						}
						else
						{
							shrinked_palette[3*j + 0] = game_palette[3*i + 0];
							shrinked_palette[3*j + 1] = game_palette[3*i + 1];
							shrinked_palette[3*j + 2] = game_palette[3*i + 2];
						}
					}
				}

if (errorlog) fprintf(errorlog,"shrinked palette uses %d colors\n",used);

				if (osd_allocate_colors(used,shrinked_palette,shrinked_pens,0))
					return 1;
			}


			for (i = 0;i < Machine->drv->total_colors;i++)
				Machine->pens[i] = shrinked_pens[palette_map[i]];

			palette_transparent_pen = shrinked_pens[TRANSPARENT_PEN];	/* for dynamic palette games */
		}
		break;

		case STATIC_16BIT:
		{
			unsigned char *p = shrinked_palette;
			int r,g,b;

			if (Machine->scrbitmap->depth == 16)
			{
				for (r = 0;r < 32;r++)
				{
					for (g = 0;g < 32;g++)
					{
						for (b = 0;b < 32;b++)
						{
							*p++ = (r << 3) | (r >> 2);
							*p++ = (g << 3) | (g >> 2);
							*p++ = (b << 3) | (b >> 2);
						}
					}
				}

				if (osd_allocate_colors(32768,shrinked_palette,shrinked_pens,0))
					return 1;
			}
			else
			{
				for (r = 0;r < 8;r++)
				{
					for (g = 0;g < 8;g++)
					{
						for (b = 0;b < 4;b++)
						{
							*p++ = (r << 5) | (r << 2) | (r >> 1);
							*p++ = (g << 5) | (g << 2) | (g >> 1);
							*p++ = (b << 6) | (b << 4) | (b << 2) | b;
						}
					}
				}

				if (osd_allocate_colors(256,shrinked_palette,shrinked_pens,0))
					return 1;
			}

			for (i = 0;i < Machine->drv->total_colors;i++)
			{
				r = game_palette[3*i + 0];
				g = game_palette[3*i + 1];
				b = game_palette[3*i + 2];

				Machine->pens[i] = shrinked_pens[rgbpenindex(r,g,b)];
			}

			palette_transparent_pen = shrinked_pens[0];	/* we are forced to use black for the transparent pen */
		}
		break;

		case PALETTIZED_16BIT:
		{
			for (i = 0;i < RESERVED_PENS;i++)
			{
				shrinked_palette[3*i + 0] =
				shrinked_palette[3*i + 1] =
				shrinked_palette[3*i + 2] = 0;
			}

			for (i = 0;i < Machine->drv->total_colors;i++)
			{
				shrinked_palette[3*(i+RESERVED_PENS) + 0] = game_palette[3*i + 0];
				shrinked_palette[3*(i+RESERVED_PENS) + 1] = game_palette[3*i + 1];
				shrinked_palette[3*(i+RESERVED_PENS) + 2] = game_palette[3*i + 2];
			}

			if (osd_allocate_colors(total_shrinked_pens,shrinked_palette,shrinked_pens,(Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE)))
				return 1;

			for (i = 0;i < Machine->drv->total_colors;i++)
				Machine->pens[i] = shrinked_pens[i + RESERVED_PENS];

			palette_transparent_pen = shrinked_pens[TRANSPARENT_PEN];	/* for dynamic palette games */
		}
		break;
	}

	for (i = 0;i < Machine->drv->color_table_len;i++)
	{
		int color = Machine->game_colortable[i];

		/* check for invalid colors set by Machine->drv->vh_init_palette */
		if (color < Machine->drv->total_colors)
			Machine->remapped_colortable[i] = Machine->pens[color];
		else
			usrintf_showmessage("colortable[%d] (=%d) out of range (total_colors = %d)",
					i,color,Machine->drv->total_colors);
	}

	return 0;
}



INLINE void palette_change_color_16_static(int color,unsigned char red,unsigned char green,unsigned char blue)
{
	if (color == palette_transparent_color)
	{
		int i;


		palette_transparent_pen = shrinked_pens[rgbpenindex(red,green,blue)];

		if (color == -1) return;	/* by default, palette_transparent_color is -1 */

		for (i = 0;i < Machine->drv->total_colors;i++)
		{
			if ((old_used_colors[i] & (PALETTE_COLOR_VISIBLE | PALETTE_COLOR_TRANSPARENT_FLAG))
					== (PALETTE_COLOR_VISIBLE | PALETTE_COLOR_TRANSPARENT_FLAG))
				old_used_colors[i] |= PALETTE_COLOR_NEEDS_REMAP;
		}
	}

	if (	game_palette[3*color + 0] == red &&
			game_palette[3*color + 1] == green &&
			game_palette[3*color + 2] == blue)
		return;

	game_palette[3*color + 0] = red;
	game_palette[3*color + 1] = green;
	game_palette[3*color + 2] = blue;

	if (old_used_colors[color] & PALETTE_COLOR_VISIBLE)
		/* we'll have to reassign the color in palette_recalc() */
		old_used_colors[color] |= PALETTE_COLOR_NEEDS_REMAP;
}

INLINE void palette_change_color_16_palettized(int color,unsigned char red,unsigned char green,unsigned char blue)
{
	if (color == palette_transparent_color)
	{
		osd_modify_pen(palette_transparent_pen,red,green,blue);

		if (color == -1) return;	/* by default, palette_transparent_color is -1 */
	}

	if (	game_palette[3*color + 0] == red &&
			game_palette[3*color + 1] == green &&
			game_palette[3*color + 2] == blue)
		return;

	/* Machine->pens[color] might have been remapped to transparent_pen, so I */
	/* use shrinked_pens[] directly */
	osd_modify_pen(shrinked_pens[color + RESERVED_PENS],red,green,blue);
	game_palette[3*color + 0] = red;
	game_palette[3*color + 1] = green;
	game_palette[3*color + 2] = blue;
}

INLINE void palette_change_color_8(int color,unsigned char red,unsigned char green,unsigned char blue)
{
	int pen;

	if (color == palette_transparent_color)
	{
		osd_modify_pen(palette_transparent_pen,red,green,blue);

		if (color == -1) return;	/* by default, palette_transparent_color is -1 */
	}

	if (	game_palette[3*color + 0] == red &&
			game_palette[3*color + 1] == green &&
			game_palette[3*color + 2] == blue)
	{
		palette_dirty[color] = 0;
		return;
	}

	pen = palette_map[color];

	/* if the color was used, mark it as dirty, we'll change it in palette_recalc() */
	if (old_used_colors[color] & PALETTE_COLOR_VISIBLE)
	{
		new_palette[3*color + 0] = red;
		new_palette[3*color + 1] = green;
		new_palette[3*color + 2] = blue;
		palette_dirty[color] = 1;
	}
	/* otherwise, just update the array */
	else
	{
		game_palette[3*color + 0] = red;
		game_palette[3*color + 1] = green;
		game_palette[3*color + 2] = blue;
	}
}

void palette_change_color(int color,unsigned char red,unsigned char green,unsigned char blue)
{
	if ((Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE) == 0)
	{
if (errorlog) fprintf(errorlog,"Error: palette_change_color() called, but VIDEO_MODIFIES_PALETTE not set.\n");
		return;
	}

	if (color >= Machine->drv->total_colors)
	{
if (errorlog) fprintf(errorlog,"error: palette_change_color() called with color %d, but only %d allocated.\n",color,Machine->drv->total_colors);
		return;
	}

	switch (use_16bit)
	{
		case NO_16BIT:
			palette_change_color_8(color,red,green,blue);
			break;
		case STATIC_16BIT:
			palette_change_color_16_static(color,red,green,blue);
			break;
		case PALETTIZED_16BIT:
			palette_change_color_16_palettized(color,red,green,blue);
			break;
	}
}




void palette_increase_usage_count(int table_offset,unsigned int usage_mask,int color_flags)
{
	/* if we are not dynamically reducing the palette, return immediately. */
	if (palette_used_colors == 0) return;

	while (usage_mask)
	{
		if (usage_mask & 1)
		{
			if (color_flags & PALETTE_COLOR_VISIBLE)
				pen_visiblecount[Machine->game_colortable[table_offset]]++;
			if (color_flags & PALETTE_COLOR_CACHED)
				pen_cachedcount[Machine->game_colortable[table_offset]]++;
		}
		table_offset++;
		usage_mask >>= 1;
	}
}

void palette_decrease_usage_count(int table_offset,unsigned int usage_mask,int color_flags)
{
	/* if we are not dynamically reducing the palette, return immediately. */
	if (palette_used_colors == 0) return;

	while (usage_mask)
	{
		if (usage_mask & 1)
		{
			if (color_flags & PALETTE_COLOR_VISIBLE)
				pen_visiblecount[Machine->game_colortable[table_offset]]--;
			if (color_flags & PALETTE_COLOR_CACHED)
				pen_cachedcount[Machine->game_colortable[table_offset]]--;
		}
		table_offset++;
		usage_mask >>= 1;
	}
}

void palette_increase_usage_countx(int table_offset,int num_pens,const unsigned char *pen_data,int color_flags)
{
	char flag[256];
	memset(flag,0,256);

	while (num_pens--)
	{
		int pen = pen_data[num_pens];
		if (flag[pen] == 0)
		{
			if (color_flags & PALETTE_COLOR_VISIBLE)
				pen_visiblecount[Machine->game_colortable[table_offset+pen]]++;
			if (color_flags & PALETTE_COLOR_CACHED)
				pen_cachedcount[Machine->game_colortable[table_offset+pen]]++;
			flag[pen] = 1;
		}
	}
}

void palette_decrease_usage_countx(int table_offset, int num_pens, const unsigned char *pen_data,int color_flags)
{
	char flag[256];
	memset(flag,0,256);

	while (num_pens--)
	{
		int pen = pen_data[num_pens];
		if (flag[pen] == 0)
		{
			if (color_flags & PALETTE_COLOR_VISIBLE)
				pen_visiblecount[Machine->game_colortable[table_offset+pen]]--;
			if (color_flags & PALETTE_COLOR_CACHED)
				pen_cachedcount[Machine->game_colortable[table_offset+pen]]--;
			flag[pen] = 1;
		}
	}
}

void palette_init_used_colors(void)
{
	int pen;


	/* if we are not dynamically reducing the palette, return immediately. */
	if (palette_used_colors == 0) return;

	memset(palette_used_colors,PALETTE_COLOR_UNUSED,Machine->drv->total_colors * sizeof(unsigned char));

	for (pen = 0;pen < Machine->drv->total_colors;pen++)
	{
		if (pen_visiblecount[pen]) palette_used_colors[pen] |= PALETTE_COLOR_VISIBLE;
		if (pen_cachedcount[pen]) palette_used_colors[pen] |= PALETTE_COLOR_CACHED;
	}
}




static unsigned char rgb6_to_pen[64][64][64];

static void build_rgb_to_pen(void)
{
	int i,rr,gg,bb;

	memset(rgb6_to_pen,DYNAMIC_MAX_PENS,sizeof(rgb6_to_pen));
	rgb6_to_pen[0][0][0] = BLACK_PEN;

	for (i = 0;i < DYNAMIC_MAX_PENS;i++)
	{
		if (pen_usage_count[i] > 0)
		{
			rr = shrinked_palette[3*i + 0] >> 2;
			gg = shrinked_palette[3*i + 1] >> 2;
			bb = shrinked_palette[3*i + 2] >> 2;

			if (rgb6_to_pen[rr][gg][bb] == DYNAMIC_MAX_PENS)
			{
				int j,max;

				rgb6_to_pen[rr][gg][bb] = i;
				max = pen_usage_count[i];

				/* to reduce flickering during remaps, find the pen used by most colors */
				for (j = i+1;j < DYNAMIC_MAX_PENS;j++)
				{
					if (pen_usage_count[j] > max &&
							rr == (shrinked_palette[3*j + 0] >> 2) &&
							gg == (shrinked_palette[3*j + 1] >> 2) &&
							bb == (shrinked_palette[3*j + 2] >> 2))
					{
						rgb6_to_pen[rr][gg][bb] = j;
						max = pen_usage_count[j];
					}
				}
			}
		}
	}
}

static int compress_palette(void)
{
	int i,j,saved,r,g,b;


	build_rgb_to_pen();

	saved = 0;

	for (i = 0;i < Machine->drv->total_colors;i++)
	{
		/* merge pens of the same color */
		if ((old_used_colors[i] & PALETTE_COLOR_VISIBLE) &&
				!(old_used_colors[i] & (PALETTE_COLOR_NEEDS_REMAP|PALETTE_COLOR_TRANSPARENT_FLAG)))
		{
			r = game_palette[3*i + 0] >> 2;
			g = game_palette[3*i + 1] >> 2;
			b = game_palette[3*i + 2] >> 2;

			j = rgb6_to_pen[r][g][b];

			if (palette_map[i] != j)
			{
				just_remapped[i] = 1;

				pen_usage_count[palette_map[i]]--;
				if (pen_usage_count[palette_map[i]] == 0)
					saved++;
				palette_map[i] = j;
				pen_usage_count[palette_map[i]]++;
				Machine->pens[i] = shrinked_pens[palette_map[i]];
			}
		}
	}

#if VERBOSE
if (errorlog)
{
	int subcount[8];


	for (i = 0;i < 8;i++)
		subcount[i] = 0;

	for (i = 0;i < Machine->drv->total_colors;i++)
		subcount[palette_used_colors[i]]++;

	fprintf(errorlog,"Ran out of pens! %d colors used (%d unused, %d visible %d cached %d visible+cached, %d transparent)\n",
			subcount[PALETTE_COLOR_VISIBLE]+subcount[PALETTE_COLOR_CACHED]+subcount[PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED]+subcount[PALETTE_COLOR_TRANSPARENT],
			subcount[PALETTE_COLOR_UNUSED],
			subcount[PALETTE_COLOR_VISIBLE],
			subcount[PALETTE_COLOR_CACHED],
			subcount[PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED],
			subcount[PALETTE_COLOR_TRANSPARENT]);
	fprintf(errorlog,"Compressed the palette, saving %d pens\n",saved);
}
#endif

	return saved;
}


static const unsigned char *palette_recalc_16_static(void)
{
	int i,color;
	int did_remap = 0;
	int need_refresh = 0;


	memset(just_remapped,0,Machine->drv->total_colors * sizeof(unsigned char));

	for (color = 0;color < Machine->drv->total_colors;color++)
	{
		/* the comparison between palette_used_colors and old_used_colors also includes */
		/* PALETTE_COLOR_NEEDS_REMAP which might have been set by palette_change_color() */
		if ((palette_used_colors[color] & PALETTE_COLOR_VISIBLE) &&
				palette_used_colors[color] != old_used_colors[color])
		{
			int r,g,b;


			did_remap = 1;
			if (old_used_colors[color] & palette_used_colors[color] & PALETTE_COLOR_CACHED)
			{
				/* the color was and still is cached, we'll have to redraw everything */
				need_refresh = 1;
				just_remapped[color] = 1;
			}

			if (palette_used_colors[color] & PALETTE_COLOR_TRANSPARENT_FLAG)
				Machine->pens[color] = palette_transparent_pen;
			else
			{
				r = game_palette[3*color + 0];
				g = game_palette[3*color + 1];
				b = game_palette[3*color + 2];

				Machine->pens[color] = shrinked_pens[rgbpenindex(r,g,b)];
			}
		}

		old_used_colors[color] = palette_used_colors[color];
	}


	if (did_remap)
	{
		/* rebuild the color lookup table */
		for (i = 0;i < Machine->drv->color_table_len;i++)
			Machine->remapped_colortable[i] = Machine->pens[Machine->game_colortable[i]];
	}

	if (need_refresh) return just_remapped;
	else return 0;
}

static const unsigned char *palette_recalc_16_palettized(void)
{
	int i,color;
	int did_remap = 0;
	int need_refresh = 0;


	memset(just_remapped,0,Machine->drv->total_colors * sizeof(unsigned char));

	for (color = 0;color < Machine->drv->total_colors;color++)
	{
		if ((palette_used_colors[color] & PALETTE_COLOR_TRANSPARENT_FLAG) !=
				(old_used_colors[color] & PALETTE_COLOR_TRANSPARENT_FLAG))
		{
			did_remap = 1;
			if (old_used_colors[color] & palette_used_colors[color] & PALETTE_COLOR_CACHED)
			{
				/* the color was and still is cached, we'll have to redraw everything */
				need_refresh = 1;
				just_remapped[color] = 1;
			}

			if (palette_used_colors[color] & PALETTE_COLOR_TRANSPARENT_FLAG)
				Machine->pens[color] = palette_transparent_pen;
			else
				Machine->pens[color] = shrinked_pens[color + RESERVED_PENS];
		}

		old_used_colors[color] = palette_used_colors[color];
	}


	if (did_remap)
	{
		/* rebuild the color lookup table */
		for (i = 0;i < Machine->drv->color_table_len;i++)
			Machine->remapped_colortable[i] = Machine->pens[Machine->game_colortable[i]];
	}

	if (need_refresh) return just_remapped;
	else return 0;
}

static const unsigned char *palette_recalc_8(void)
{
	int i,color;
	int did_remap = 0;
	int need_refresh = 0;
	int first_free_pen;
	int ran_out = 0;
	int reuse_pens = 0;
	int need,avail;


	memset(just_remapped,0,Machine->drv->total_colors * sizeof(unsigned char));


	/* first of all, apply the changes to the palette which were */
	/* requested since last update */
	for (color = 0;color < Machine->drv->total_colors;color++)
	{
		if (palette_dirty[color])
		{
			int r,g,b,pen;


			pen = palette_map[color];
			r = new_palette[3*color + 0];
			g = new_palette[3*color + 1];
			b = new_palette[3*color + 2];

			/* if the color maps to an exclusive pen, just change it */
			if (pen_usage_count[pen] == 1)
			{
				palette_dirty[color] = 0;
				game_palette[3*color + 0] = r;
				game_palette[3*color + 1] = g;
				game_palette[3*color + 2] = b;

				shrinked_palette[3*pen + 0] = r;
				shrinked_palette[3*pen + 1] = g;
				shrinked_palette[3*pen + 2] = b;
				osd_modify_pen(Machine->pens[color],r,g,b);
			}
			else
			{
				if (pen < RESERVED_PENS)
				{
					/* the color uses a reserved pen, the only thing we can do is remap it */
					for (i = color;i < Machine->drv->total_colors;i++)
					{
						if (palette_dirty[i] != 0 && palette_map[i] == pen)
						{
							palette_dirty[i] = 0;
							game_palette[3*i + 0] = new_palette[3*i + 0];
							game_palette[3*i + 1] = new_palette[3*i + 1];
							game_palette[3*i + 2] = new_palette[3*i + 2];
							old_used_colors[i] |= PALETTE_COLOR_NEEDS_REMAP;
						}
					}
				}
				else
				{
					/* the pen is shared with other colors, let's see if all of them */
					/* have been changed to the same value */
					for (i = 0;i < Machine->drv->total_colors;i++)
					{
						if ((old_used_colors[i] & PALETTE_COLOR_VISIBLE) &&
								palette_map[i] == pen)
						{
							if (palette_dirty[i] == 0 ||
									new_palette[3*i + 0] != r ||
									new_palette[3*i + 1] != g ||
									new_palette[3*i + 2] != b)
								break;
						}
					}

					if (i == Machine->drv->total_colors)
					{
						/* all colors sharing this pen still are the same, so we */
						/* just change the palette. */
						shrinked_palette[3*pen + 0] = r;
						shrinked_palette[3*pen + 1] = g;
						shrinked_palette[3*pen + 2] = b;
						osd_modify_pen(Machine->pens[color],r,g,b);

						for (i = color;i < Machine->drv->total_colors;i++)
						{
							if (palette_dirty[i] != 0 && palette_map[i] == pen)
							{
								palette_dirty[i] = 0;
								game_palette[3*i + 0] = r;
								game_palette[3*i + 1] = g;
								game_palette[3*i + 2] = b;
							}
						}
					}
					else
					{
						/* the colors sharing this pen now are different, we'll */
						/* have to remap them. */
						for (i = color;i < Machine->drv->total_colors;i++)
						{
							if (palette_dirty[i] != 0 && palette_map[i] == pen)
							{
								palette_dirty[i] = 0;
								game_palette[3*i + 0] = new_palette[3*i + 0];
								game_palette[3*i + 1] = new_palette[3*i + 1];
								game_palette[3*i + 2] = new_palette[3*i + 2];
								old_used_colors[i] |= PALETTE_COLOR_NEEDS_REMAP;
							}
						}
					}
				}
			}
		}
	}


	need = 0;
	for (i = 0;i < Machine->drv->total_colors;i++)
	{
		if ((palette_used_colors[i] & PALETTE_COLOR_VISIBLE) && palette_used_colors[i] != old_used_colors[i])
			need++;
	}
	if (need > 0)
	{
		avail = 0;
		for (i = 0;i < DYNAMIC_MAX_PENS;i++)
		{
			if (pen_usage_count[i] == 0)
				avail++;
		}

		if (need > avail)
		{
#if VERBOSE
if (errorlog) fprintf(errorlog,"Need %d new pens; %d available. I'll reuse some pens.\n",need,avail);
#endif
			reuse_pens = 1;
			build_rgb_to_pen();
		}
	}

	first_free_pen = RESERVED_PENS;
	for (color = 0;color < Machine->drv->total_colors;color++)
	{
		/* the comparison between palette_used_colors and old_used_colors also includes */
		/* PALETTE_COLOR_NEEDS_REMAP which might have been set previously */
		if ((palette_used_colors[color] & PALETTE_COLOR_VISIBLE) &&
				palette_used_colors[color] != old_used_colors[color])
		{
			int r,g,b;


			if (old_used_colors[color] & PALETTE_COLOR_VISIBLE)
			{
				pen_usage_count[palette_map[color]]--;
				old_used_colors[color] &= ~PALETTE_COLOR_VISIBLE;
			}

			r = game_palette[3*color + 0];
			g = game_palette[3*color + 1];
			b = game_palette[3*color + 2];

			if (palette_used_colors[color] & PALETTE_COLOR_TRANSPARENT_FLAG)
			{
				if (palette_map[color] != TRANSPARENT_PEN)
				{
					/* use the fixed transparent black for this */
					did_remap = 1;
					if (old_used_colors[color] & palette_used_colors[color] & PALETTE_COLOR_CACHED)
					{
						/* the color was and still is cached, we'll have to redraw everything */
						need_refresh = 1;
						just_remapped[color] = 1;
					}

					palette_map[color] = TRANSPARENT_PEN;
				}
				pen_usage_count[palette_map[color]]++;
				Machine->pens[color] = shrinked_pens[palette_map[color]];
				old_used_colors[color] = palette_used_colors[color];
			}
			else
			{
				if (reuse_pens)
				{
					i = rgb6_to_pen[r >> 2][g >> 2][b >> 2];
					if (i != DYNAMIC_MAX_PENS)
					{
						if (palette_map[color] != i)
						{
							did_remap = 1;
							if (old_used_colors[color] & palette_used_colors[color] & PALETTE_COLOR_CACHED)
							{
								/* the color was and still is cached, we'll have to redraw everything */
								need_refresh = 1;
								just_remapped[color] = 1;
							}

							palette_map[color] = i;
						}
						pen_usage_count[palette_map[color]]++;
						Machine->pens[color] = shrinked_pens[palette_map[color]];
						old_used_colors[color] = palette_used_colors[color];
					}
				}

				/* if we still haven't found a pen, choose a new one */
				if (old_used_colors[color] != palette_used_colors[color])
				{
					/* if possible, reuse the last associated pen */
					if (pen_usage_count[palette_map[color]] == 0)
					{
						pen_usage_count[palette_map[color]]++;
					}
					else	/* allocate a new pen */
					{
retry:
						while (first_free_pen < DYNAMIC_MAX_PENS && pen_usage_count[first_free_pen] > 0)
							first_free_pen++;

						if (first_free_pen < DYNAMIC_MAX_PENS)
						{
							did_remap = 1;
							if (old_used_colors[color] & palette_used_colors[color] & PALETTE_COLOR_CACHED)
							{
								/* the color was and still is cached, we'll have to redraw everything */
								need_refresh = 1;
								just_remapped[color] = 1;
							}

							palette_map[color] = first_free_pen;
							pen_usage_count[palette_map[color]]++;
							Machine->pens[color] = shrinked_pens[palette_map[color]];
						}
						else
						{
							/* Ran out of pens! Let's see what we can do. */

							if (ran_out == 0)
							{
								ran_out++;

								/* from now on, try to reuse already allocated pens */
								reuse_pens = 1;
								if (compress_palette() > 0)
								{
									did_remap = 1;
									need_refresh = 1;	/* we'll have to redraw everything */

									first_free_pen = RESERVED_PENS;
									goto retry;
								}
							}

							ran_out++;

							/* we failed, but go on with the loop, there might */
							/* be some transparent pens to remap */

							continue;
						}
					}

					{
						int rr,gg,bb;

						i = palette_map[color];
						rr = shrinked_palette[3*i + 0] >> 2;
						gg = shrinked_palette[3*i + 1] >> 2;
						bb = shrinked_palette[3*i + 2] >> 2;
						if (rgb6_to_pen[rr][gg][bb] == i)
							rgb6_to_pen[rr][gg][bb] = DYNAMIC_MAX_PENS;

						shrinked_palette[3*i + 0] = r;
						shrinked_palette[3*i + 1] = g;
						shrinked_palette[3*i + 2] = b;
						osd_modify_pen(Machine->pens[color],r,g,b);

						r >>= 2;
						g >>= 2;
						b >>= 2;
						if (rgb6_to_pen[r][g][b] == DYNAMIC_MAX_PENS)
							rgb6_to_pen[r][g][b] = i;
					}

					old_used_colors[color] = palette_used_colors[color];
				}
			}
		}
	}

	if (ran_out > 1)
	{
#ifdef MAME_DEBUG
		char buf[80];

		sprintf(buf,"Error: Palette overflow -%d",ran_out-1);
		usrintf_showmessage(buf);
#endif
if (errorlog) fprintf(errorlog,"Error: no way to shrink the palette to 256 colors, left out %d colors.\n",ran_out-1);
#if 0
fprintf(errorlog,"color list:\n");
for (color = 0;color < Machine->drv->total_colors;color++)
{
	int r,g,b;
	r = game_palette[3*color + 0];
	g = game_palette[3*color + 1];
	b = game_palette[3*color + 2];
	if (palette_used_colors[color] & PALETTE_COLOR_VISIBLE)
		fprintf(errorlog,"%02x %02x %02x\n",r,g,b);
}
#endif
	}

	/* Reclaim unused pens; we do this AFTER allocating the new ones, to avoid */
	/* using the same pen for two different colors in two consecutive frames, */
	/* which might cause flicker. */
	for (color = 0;color < Machine->drv->total_colors;color++)
	{
		if (!(palette_used_colors[color] & PALETTE_COLOR_VISIBLE))
		{
			if (old_used_colors[color] & PALETTE_COLOR_VISIBLE)
				pen_usage_count[palette_map[color]]--;
			old_used_colors[color] = palette_used_colors[color];
		}
	}

#ifdef PEDANTIC
	/* invalidate unused pens to make bugs in color allocation evident. */
	for (i = 0;i < DYNAMIC_MAX_PENS;i++)
	{
		if (pen_usage_count[i] == 0)
		{
			int r,g,b;
			r = rand() & 0xff;
			g = rand() & 0xff;
			b = rand() & 0xff;
			shrinked_palette[3*i + 0] = r;
			shrinked_palette[3*i + 1] = g;
			shrinked_palette[3*i + 2] = b;
			osd_modify_pen(shrinked_pens[i],r,g,b);
		}
	}
#endif

	if (did_remap)
	{
		/* rebuild the color lookup table */
		for (i = 0;i < Machine->drv->color_table_len;i++)
			Machine->remapped_colortable[i] = Machine->pens[Machine->game_colortable[i]];
	}

	if (need_refresh)
	{
		if (errorlog)
		{
			int used;

			used = 0;
			for (i = 0;i < DYNAMIC_MAX_PENS;i++)
			{
				if (pen_usage_count[i] > 0)
					used++;
			}

#if VERBOSE
			fprintf(errorlog,"Did a palette remap, need a full screen redraw (%d pens used).\n",used);
#endif
		}
		return just_remapped;
	}
	else return 0;
}


const unsigned char *palette_recalc(void)
{
	/* if we are not dynamically reducing the palette, return immediately. */
	if (palette_used_colors == 0)
		return 0;
	switch (use_16bit)
	{
		case NO_16BIT:
		default:
			return palette_recalc_8();
		case STATIC_16BIT:
			return palette_recalc_16_static();
		case PALETTIZED_16BIT:
			return palette_recalc_16_palettized();
	}
}



/******************************************************************************

 Commonly used palette RAM handling functions

******************************************************************************/

unsigned char *paletteram,*paletteram_2;

int paletteram_r(int offset)
{
	return paletteram[offset];
}

int paletteram_2_r(int offset)
{
	return paletteram_2[offset];
}

int paletteram_word_r(int offset)
{
	return READ_WORD(&paletteram[offset]);
}

int paletteram_2_word_r(int offset)
{
	return READ_WORD(&paletteram_2[offset]);
}

void paletteram_RRRGGGBB_w(int offset,int data)
{
	int r,g,b;
	int bit0,bit1,bit2;


	paletteram[offset] = data;

	/* red component */
	bit0 = (data >> 5) & 0x01;
	bit1 = (data >> 6) & 0x01;
	bit2 = (data >> 7) & 0x01;
	r = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	/* green component */
	bit0 = (data >> 2) & 0x01;
	bit1 = (data >> 3) & 0x01;
	bit2 = (data >> 4) & 0x01;
	g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	/* blue component */
	bit0 = 0;
	bit1 = (data >> 0) & 0x01;
	bit2 = (data >> 1) & 0x01;
	b = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;

	palette_change_color(offset,r,g,b);
}


void paletteram_BBGGGRRR_w(int offset,int data)
{
	int r,g,b;
	int bit0,bit1,bit2;


	paletteram[offset] = data;

	/* red component */
	bit0 = (data >> 0) & 0x01;
	bit1 = (data >> 1) & 0x01;
	bit2 = (data >> 2) & 0x01;
	r = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	/* green component */
	bit0 = (data >> 3) & 0x01;
	bit1 = (data >> 4) & 0x01;
	bit2 = (data >> 5) & 0x01;
	g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	/* blue component */
	bit0 = 0;
	bit1 = (data >> 6) & 0x01;
	bit2 = (data >> 7) & 0x01;
	b = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;

	palette_change_color(offset,r,g,b);
}


void paletteram_IIBBGGRR_w(int offset,int data)
{
	int r,g,b,i;


	paletteram[offset] = data;

	i = (data >> 6) & 0x03;
	/* red component */
	r = (data << 2) & 0x0c;
	if (r) r |= i;
	r *= 0x11;
	/* green component */
	g = (data >> 0) & 0x0c;
	if (g) g |= i;
	g *= 0x11;
	/* blue component */
	b = (data >> 2) & 0x0c;
	if (b) b |= i;
	b *= 0x11;

	palette_change_color(offset,r,g,b);
}


void paletteram_BBGGRRII_w(int offset,int data)
{
	int r,g,b,i;


	paletteram[offset] = data;

	i = (data >> 0) & 0x03;
	/* red component */
	r = (((data >> 0) & 0x0c) | i) * 0x11;
	/* green component */
	g = (((data >> 2) & 0x0c) | i) * 0x11;
	/* blue component */
	b = (((data >> 4) & 0x0c) | i) * 0x11;

	palette_change_color(offset,r,g,b);
}


INLINE void changecolor_xxxxBBBBGGGGRRRR(int color,int data)
{
	int r,g,b;


	r = (data >> 0) & 0x0f;
	g = (data >> 4) & 0x0f;
	b = (data >> 8) & 0x0f;

	r = (r << 4) | r;
	g = (g << 4) | g;
	b = (b << 4) | b;

	palette_change_color(color,r,g,b);
}

void paletteram_xxxxBBBBGGGGRRRR_w(int offset,int data)
{
	paletteram[offset] = data;
	changecolor_xxxxBBBBGGGGRRRR(offset / 2,paletteram[offset & ~1] | (paletteram[offset | 1] << 8));
}

void paletteram_xxxxBBBBGGGGRRRR_swap_w(int offset,int data)
{
	paletteram[offset] = data;
	changecolor_xxxxBBBBGGGGRRRR(offset / 2,paletteram[offset | 1] | (paletteram[offset & ~1] << 8));
}

void paletteram_xxxxBBBBGGGGRRRR_split1_w(int offset,int data)
{
	paletteram[offset] = data;
	changecolor_xxxxBBBBGGGGRRRR(offset,paletteram[offset] | (paletteram_2[offset] << 8));
}

void paletteram_xxxxBBBBGGGGRRRR_split2_w(int offset,int data)
{
	paletteram_2[offset] = data;
	changecolor_xxxxBBBBGGGGRRRR(offset,paletteram[offset] | (paletteram_2[offset] << 8));
}

void paletteram_xxxxBBBBGGGGRRRR_word_w(int offset,int data)
{
	int oldword = READ_WORD(&paletteram[offset]);
	int newword = COMBINE_WORD(oldword,data);


	WRITE_WORD(&paletteram[offset],newword);
	changecolor_xxxxBBBBGGGGRRRR(offset / 2,newword);
}


INLINE void changecolor_xxxxBBBBRRRRGGGG(int color,int data)
{
	int r,g,b;


	r = (data >> 4) & 0x0f;
	g = (data >> 0) & 0x0f;
	b = (data >> 8) & 0x0f;

	r = (r << 4) | r;
	g = (g << 4) | g;
	b = (b << 4) | b;

	palette_change_color(color,r,g,b);
}

void paletteram_xxxxBBBBRRRRGGGG_w(int offset,int data)
{
	paletteram[offset] = data;
	changecolor_xxxxBBBBRRRRGGGG(offset / 2,paletteram[offset & ~1] | (paletteram[offset | 1] << 8));
}

void paletteram_xxxxBBBBRRRRGGGG_swap_w(int offset,int data)
{
	paletteram[offset] = data;
	changecolor_xxxxBBBBRRRRGGGG(offset / 2,paletteram[offset | 1] | (paletteram[offset & ~1] << 8));
}

void paletteram_xxxxBBBBRRRRGGGG_split1_w(int offset,int data)
{
	paletteram[offset] = data;
	changecolor_xxxxBBBBRRRRGGGG(offset,paletteram[offset] | (paletteram_2[offset] << 8));
}

void paletteram_xxxxBBBBRRRRGGGG_split2_w(int offset,int data)
{
	paletteram_2[offset] = data;
	changecolor_xxxxBBBBRRRRGGGG(offset,paletteram[offset] | (paletteram_2[offset] << 8));
}


INLINE void changecolor_xxxxRRRRBBBBGGGG(int color,int data)
{
	int r,g,b;


	r = (data >> 8) & 0x0f;
	g = (data >> 0) & 0x0f;
	b = (data >> 4) & 0x0f;

	r = (r << 4) | r;
	g = (g << 4) | g;
	b = (b << 4) | b;

	palette_change_color(color,r,g,b);
}

void paletteram_xxxxRRRRBBBBGGGG_split1_w(int offset,int data)
{
	paletteram[offset] = data;
	changecolor_xxxxRRRRBBBBGGGG(offset,paletteram[offset] | (paletteram_2[offset] << 8));
}

void paletteram_xxxxRRRRBBBBGGGG_split2_w(int offset,int data)
{
	paletteram_2[offset] = data;
	changecolor_xxxxRRRRBBBBGGGG(offset,paletteram[offset] | (paletteram_2[offset] << 8));
}


INLINE void changecolor_xxxxRRRRGGGGBBBB(int color,int data)
{
	int r,g,b;


	r = (data >> 8) & 0x0f;
	g = (data >> 4) & 0x0f;
	b = (data >> 0) & 0x0f;

	r = (r << 4) | r;
	g = (g << 4) | g;
	b = (b << 4) | b;

	palette_change_color(color,r,g,b);
}

void paletteram_xxxxRRRRGGGGBBBB_w(int offset,int data)
{
	paletteram[offset] = data;
	changecolor_xxxxRRRRGGGGBBBB(offset / 2,paletteram[offset & ~1] | (paletteram[offset | 1] << 8));
}

void paletteram_xxxxRRRRGGGGBBBB_word_w(int offset,int data)
{
	int oldword = READ_WORD(&paletteram[offset]);
	int newword = COMBINE_WORD(oldword,data);


	WRITE_WORD(&paletteram[offset],newword);
	changecolor_xxxxRRRRGGGGBBBB(offset / 2,newword);
}


INLINE void changecolor_RRRRGGGGBBBBxxxx(int color,int data)
{
	int r,g,b;


	r = (data >> 12) & 0x0f;
	g = (data >>  8) & 0x0f;
	b = (data >>  4) & 0x0f;

	r = (r << 4) | r;
	g = (g << 4) | g;
	b = (b << 4) | b;

	palette_change_color(color,r,g,b);
}

void paletteram_RRRRGGGGBBBBxxxx_swap_w(int offset,int data)
{
	paletteram[offset] = data;
	changecolor_RRRRGGGGBBBBxxxx(offset / 2,paletteram[offset | 1] | (paletteram[offset & ~1] << 8));
}

void paletteram_RRRRGGGGBBBBxxxx_split1_w(int offset,int data)
{
	paletteram[offset] = data;
	changecolor_RRRRGGGGBBBBxxxx(offset,paletteram[offset] | (paletteram_2[offset] << 8));
}

void paletteram_RRRRGGGGBBBBxxxx_split2_w(int offset,int data)
{
	paletteram_2[offset] = data;
	changecolor_RRRRGGGGBBBBxxxx(offset,paletteram[offset] | (paletteram_2[offset] << 8));
}

void paletteram_RRRRGGGGBBBBxxxx_word_w(int offset,int data)
{
	int oldword = READ_WORD(&paletteram[offset]);
	int newword = COMBINE_WORD(oldword,data);


	WRITE_WORD(&paletteram[offset],newword);
	changecolor_RRRRGGGGBBBBxxxx(offset / 2,newword);
}


INLINE void changecolor_BBBBGGGGRRRRxxxx(int color,int data)
{
	int r,g,b;


	r = (data >>  4) & 0x0f;
	g = (data >>  8) & 0x0f;
	b = (data >> 12) & 0x0f;

	r = (r << 4) | r;
	g = (g << 4) | g;
	b = (b << 4) | b;

	palette_change_color(color,r,g,b);
}

void paletteram_BBBBGGGGRRRRxxxx_swap_w(int offset,int data)
{
	paletteram[offset] = data;
	changecolor_BBBBGGGGRRRRxxxx(offset / 2,paletteram[offset | 1] | (paletteram[offset & ~1] << 8));
}

void paletteram_BBBBGGGGRRRRxxxx_split1_w(int offset,int data)
{
	paletteram[offset] = data;
	changecolor_BBBBGGGGRRRRxxxx(offset,paletteram[offset] | (paletteram_2[offset] << 8));
}

void paletteram_BBBBGGGGRRRRxxxx_split2_w(int offset,int data)
{
	paletteram_2[offset] = data;
	changecolor_BBBBGGGGRRRRxxxx(offset,paletteram[offset] | (paletteram_2[offset] << 8));
}

void paletteram_BBBBGGGGRRRRxxxx_word_w(int offset,int data)
{
	int oldword = READ_WORD(&paletteram[offset]);
	int newword = COMBINE_WORD(oldword,data);


	WRITE_WORD(&paletteram[offset],newword);
	changecolor_BBBBGGGGRRRRxxxx(offset / 2,newword);
}


INLINE void changecolor_xBBBBBGGGGGRRRRR(int color,int data)
{
	int r,g,b;


	r = (data >>  0) & 0x1f;
	g = (data >>  5) & 0x1f;
	b = (data >> 10) & 0x1f;

	r = (r << 3) | (r >> 2);
	g = (g << 3) | (g >> 2);
	b = (b << 3) | (b >> 2);

	palette_change_color(color,r,g,b);
}

void paletteram_xBBBBBGGGGGRRRRR_w(int offset,int data)
{
	paletteram[offset] = data;
	changecolor_xBBBBBGGGGGRRRRR(offset / 2,paletteram[offset & ~1] | (paletteram[offset | 1] << 8));
}

void paletteram_xBBBBBGGGGGRRRRR_swap_w(int offset,int data)
{
	paletteram[offset] = data;
	changecolor_xBBBBBGGGGGRRRRR(offset / 2,paletteram[offset | 1] | (paletteram[offset & ~1] << 8));
}

void paletteram_xBBBBBGGGGGRRRRR_word_w(int offset,int data)
{
	int oldword = READ_WORD(&paletteram[offset]);
	int newword = COMBINE_WORD(oldword,data);


	WRITE_WORD(&paletteram[offset],newword);
	changecolor_xBBBBBGGGGGRRRRR(offset / 2,newword);
}


INLINE void changecolor_xRRRRRGGGGGBBBBB(int color,int data)
{
	int r,g,b;


	r = (data >> 10) & 0x1f;
	g = (data >>  5) & 0x1f;
	b = (data >>  0) & 0x1f;

	r = (r << 3) | (r >> 2);
	g = (g << 3) | (g >> 2);
	b = (b << 3) | (b >> 2);

	palette_change_color(color,r,g,b);
}

void paletteram_xRRRRRGGGGGBBBBB_w(int offset,int data)
{
	paletteram[offset] = data;
	changecolor_xRRRRRGGGGGBBBBB(offset / 2,paletteram[offset & ~1] | (paletteram[offset | 1] << 8));
}

void paletteram_xRRRRRGGGGGBBBBB_word_w(int offset,int data)
{
	int oldword = READ_WORD(&paletteram[offset]);
	int newword = COMBINE_WORD(oldword,data);


	WRITE_WORD(&paletteram[offset],newword);
	changecolor_xRRRRRGGGGGBBBBB(offset / 2,newword);
}


INLINE void changecolor_IIIIRRRRGGGGBBBB(int color,int data)
{
	int i,r,g,b;


	static const int ztable[16] =
		{ 0x0, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf, 0x10, 0x11 };

	i = ztable[(data >> 12) & 15];
	r = ((data >> 8) & 15) * i;
	g = ((data >> 4) & 15) * i;
	b = ((data >> 0) & 15) * i;

	palette_change_color(color,r,g,b);
}

void paletteram_IIIIRRRRGGGGBBBB_word_w(int offset,int data)
{
	int oldword = READ_WORD(&paletteram[offset]);
	int newword = COMBINE_WORD(oldword,data);


	WRITE_WORD(&paletteram[offset],newword);
	changecolor_IIIIRRRRGGGGBBBB(offset / 2,newword);
}


INLINE void changecolor_RRRRGGGGBBBBIIII(int color,int data)
{
	int i,r,g,b;


	static const int ztable[16] =
		{ 0x0, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf, 0x10, 0x11 };

	i = ztable[(data >> 0) & 15];
	r = ((data >> 12) & 15) * i;
	g = ((data >>  8) & 15) * i;
	b = ((data >>  4) & 15) * i;

	palette_change_color(color,r,g,b);
}

void paletteram_RRRRGGGGBBBBIIII_word_w(int offset,int data)
{
	int oldword = READ_WORD(&paletteram[offset]);
	int newword = COMBINE_WORD(oldword,data);


	WRITE_WORD(&paletteram[offset],newword);
	changecolor_RRRRGGGGBBBBIIII(offset / 2,newword);
}
