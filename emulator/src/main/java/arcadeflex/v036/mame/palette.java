/**
 * ported to v0.36
 */
package arcadeflex.v036.mame;

public class palette {
/*TODO*////******************************************************************************
/*TODO*///
/*TODO*///  palette.c
/*TODO*///
/*TODO*///  Palette handling functions.
/*TODO*///
/*TODO*///  There are several levels of abstraction in the way MAME handles the palette,
/*TODO*///  and several display modes which can be used by the drivers.
/*TODO*///
/*TODO*///  Palette
/*TODO*///  -------
/*TODO*///  Note: in the following text, "color" refers to a color in the emulated
/*TODO*///  game's virtual palette. For example, a game might be able to display 1024
/*TODO*///  colors at the same time. If the game uses RAM to change the available
/*TODO*///  colors, the term "palette" refers to the colors available at any given time,
/*TODO*///  not to the whole range of colors which can be produced by the hardware. The
/*TODO*///  latter is referred to as "color space".
/*TODO*///  The term "pen" refers to one of the maximum MAX_PENS colors that can be
/*TODO*///  used to generate the display. PC users might want to think of them as the
/*TODO*///  colors available in VGA, but be warned: the mapping of MAME pens to the VGA
/*TODO*///  registers is not 1:1, so MAME's pen 10 will not necessarily be mapped to
/*TODO*///  VGA's color #10 (actually this is never the case). This is done to ensure
/*TODO*///  portability, since on some systems it is not possible to do a 1:1 mapping.
/*TODO*///
/*TODO*///  So, to summarize, the three layers of palette abstraction are:
/*TODO*///
/*TODO*///  P1) The game virtual palette (the "colors")
/*TODO*///  P2) MAME's MAX_PENS colors palette (the "pens")
/*TODO*///  P3) The OS specific hardware color registers (the "OS specific pens")
/*TODO*///
/*TODO*///  The array Machine->pens is a lookup table which maps game colors to OS
/*TODO*///  specific pens (P1 to P3). When you are working on bitmaps at the pixel level,
/*TODO*///  *always* use Machine->pens to map the color numbers. *Never* use constants.
/*TODO*///  For example if you want to make pixel (x,y) of color 3, do:
/*TODO*///  bitmap->line[y][x] = Machine->pens[3];
/*TODO*///
/*TODO*///
/*TODO*///  Lookup table
/*TODO*///  ------------
/*TODO*///  Palettes are only half of the story. To map the gfx data to colors, the
/*TODO*///  graphics routines use a lookup table. For example if we have 4bpp tiles,
/*TODO*///  which can have 256 different color codes, the lookup table for them will have
/*TODO*///  256 * 2^4 = 4096 elements. For games using a palette RAM, the lookup table is
/*TODO*///  usually a 1:1 map. For games using PROMs, the lookup table is often larger
/*TODO*///  than the palette itself so for example the game can display 256 colors out
/*TODO*///  of a palette of 16.
/*TODO*///
/*TODO*///  The palette and the lookup table are initialized to default values by the
/*TODO*///  main core, but can be initialized by the driver using the function
/*TODO*///  MachineDriver->vh_init_palette(). For games using palette RAM, that
/*TODO*///  function is usually not needed, and the lookup table can be set up by
/*TODO*///  properly initializing the color_codes_start and total_color_codes fields in
/*TODO*///  the GfxDecodeInfo array.
/*TODO*///  When vh_init_palette() initializes the lookup table, it maps gfx codes
/*TODO*///  to game colors (P1 above). The lookup table will be converted by the core to
/*TODO*///  map to OS specific pens (P3 above), and stored in Machine->remapped_colortable.
/*TODO*///
/*TODO*///
/*TODO*///  Display modes
/*TODO*///  -------------
/*TODO*///  The available display modes can be summarized in four categories:
/*TODO*///  1) Static palette. Use this for games which use PROMs for color generation.
/*TODO*///     The palette is initialized by vh_init_palette(), and never changed
/*TODO*///     again.
/*TODO*///  2) Dynamic palette. Use this for games which use RAM for color generation and
/*TODO*///     have no more than MAX_PENS colors in the palette. The palette can be
/*TODO*///     dynamically modified by the driver using the function
/*TODO*///     palette_change_color(). MachineDriver->video_attributes must contain the
/*TODO*///     flag VIDEO_MODIFIES_PALETTE.
/*TODO*///  3) Dynamic shrinked palette. Use this for games which use RAM for color
/*TODO*///     generation and have more than MAX_PENS colors in the palette. The palette
/*TODO*///	 can be dynamically modified by the driver using the function
/*TODO*///     palette_change_color(). MachineDriver->video_attributes must contain the
/*TODO*///     flag VIDEO_MODIFIES_PALETTE.
/*TODO*///     The difference with case 2) above is that the driver must do some
/*TODO*///     additional work to allow for palette reduction without loss of quality.
/*TODO*///     The function palette_recalc() must be called every frame before doing any
/*TODO*///     rendering. The palette_used_colors array can be changed to precisely
/*TODO*///     indicate to the function which of the game colors are used, so it can pick
/*TODO*///     only the needed colors, and make the palette fit into MAX_PENS colors.
/*TODO*///	 Colors can also be marked as "transparent".
/*TODO*///     The return code of palette_recalc() tells the driver whether the lookup
/*TODO*///     table has changed, and therefore whether a screen refresh is needed. Note
/*TODO*///     that this only applies to colors which were used in the previous frame:
/*TODO*///     that's why palette_recalc() must be called before ANY rendering takes
/*TODO*///     place.
/*TODO*///  4) 16-bit color. This should only be used for games which use more than
/*TODO*///     MAX_PENS colors at a time. It is slower than the other modes, so it should
/*TODO*///	 be avoided whenever possible. Transparency support is limited.
/*TODO*///     MachineDriver->video_attributes must contain VIDEO_MODIFIES_PALETTE, and
/*TODO*///	 GameDriver->flags GAME_REQUIRES_16BIT.
/*TODO*///
/*TODO*///  The dynamic shrinking of the palette works this way: as colors are requested,
/*TODO*///  they are associated to a pen. When a color is no longer needed, the pen is
/*TODO*///  freed and can be used for another color. When the code runs out of free pens,
/*TODO*///  it compacts the palette, putting together colors with the same RGB
/*TODO*///  components, then starts again to allocate pens for each new color. The bottom
/*TODO*///  line of all this is that the pen assignment will automatically adapt to the
/*TODO*///  game needs, and colors which change often will be assigned an exclusive pen,
/*TODO*///  which can be modified using the video cards hardware registers without need
/*TODO*///  for a screen refresh.
/*TODO*///  The important difference between cases 3) and 4) is that in 3), color cycling
/*TODO*///  (which many games use) is essentially free, while in 4) every palette change
/*TODO*///  requires a screen refresh. The color quality in 3) is also better than in 4)
/*TODO*///  if the game uses more than 5 bits per color component. For testing purposes,
/*TODO*///  you can switch between the two modes by just adding/removing the
/*TODO*///  GAME_REQUIRES_16BIT flag (but be warned about the limited transparency
/*TODO*///  support in 16-bit mode).
/*TODO*///
/*TODO*///******************************************************************************/
/*TODO*///
/*TODO*///#include "driver.h"
/*TODO*///
/*TODO*///
/*TODO*///#define VERBOSE 0
/*TODO*///
/*TODO*///
/*TODO*///static unsigned char *game_palette;	/* RGB palette as set by the driver. */
/*TODO*///static unsigned char *new_palette;	/* changes to the palette are stored here before */
/*TODO*///							/* being moved to game_palette by palette_recalc() */
/*TODO*///static unsigned char *palette_dirty;
/*TODO*////* arrays which keep track of colors actually used, to help in the palette shrinking. */
/*TODO*///unsigned char *palette_used_colors;
/*TODO*///static unsigned char *old_used_colors;
/*TODO*///static int *pen_visiblecount,*pen_cachedcount;
/*TODO*///static unsigned char *just_remapped;	/* colors which have been remapped in this frame, */
/*TODO*///										/* returned by palette_recalc() */
/*TODO*///
/*TODO*///static int use_16bit;
/*TODO*///#define NO_16BIT			0
/*TODO*///#define STATIC_16BIT		1
/*TODO*///#define PALETTIZED_16BIT	2
/*TODO*///
/*TODO*///static int total_shrinked_pens;
/*TODO*///unsigned short *shrinked_pens;
/*TODO*///static unsigned char *shrinked_palette;
/*TODO*///static unsigned short *palette_map;	/* map indexes from game_palette to shrinked_palette */
/*TODO*///static unsigned short pen_usage_count[DYNAMIC_MAX_PENS];
/*TODO*///
/*TODO*///unsigned short palette_transparent_pen;
/*TODO*///int palette_transparent_color;
/*TODO*///
/*TODO*///
/*TODO*///#define BLACK_PEN		0
/*TODO*///#define TRANSPARENT_PEN	1
/*TODO*///#define RESERVED_PENS	2
/*TODO*///
/*TODO*///#define PALETTE_COLOR_NEEDS_REMAP 0x80
/*TODO*///
/*TODO*////* helper macro for 16-bit mode */
/*TODO*///#define rgbpenindex(r,g,b) ((Machine->scrbitmap->depth==16) ? ((((r)>>3)<<10)+(((g)>>3)<<5)+((b)>>3)) : ((((r)>>5)<<5)+(((g)>>5)<<2)+((b)>>6)))
/*TODO*///
/*TODO*///
/*TODO*///unsigned short *palette_shadow_table;
/*TODO*///unsigned short *palette_highlight_table;
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///int palette_start(void)
/*TODO*///{
/*TODO*///	int i,num;
/*TODO*///
/*TODO*///
/*TODO*///	game_palette = malloc(3 * Machine->drv->total_colors * sizeof(unsigned char));
/*TODO*///	palette_map = malloc(Machine->drv->total_colors * sizeof(unsigned short));
/*TODO*///	if (Machine->drv->color_table_len)
/*TODO*///	{
/*TODO*///		Machine->game_colortable = malloc(Machine->drv->color_table_len * sizeof(unsigned short));
/*TODO*///		Machine->remapped_colortable = malloc(Machine->drv->color_table_len * sizeof(unsigned short));
/*TODO*///	}
/*TODO*///	else Machine->game_colortable = Machine->remapped_colortable = 0;
/*TODO*///
/*TODO*///	if (Machine->color_depth == 16 || (Machine->gamedrv->flags & GAME_REQUIRES_16BIT))
/*TODO*///	{
/*TODO*///		if (Machine->color_depth == 8 || Machine->drv->total_colors > 65532)
/*TODO*///			use_16bit = STATIC_16BIT;
/*TODO*///		else
/*TODO*///			use_16bit = PALETTIZED_16BIT;
/*TODO*///	}
/*TODO*///	else
/*TODO*///		use_16bit = NO_16BIT;
/*TODO*///
/*TODO*///	switch (use_16bit)
/*TODO*///	{
/*TODO*///		case NO_16BIT:
/*TODO*///			if (Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE)
/*TODO*///				total_shrinked_pens = DYNAMIC_MAX_PENS;
/*TODO*///			else
/*TODO*///				total_shrinked_pens = STATIC_MAX_PENS;
/*TODO*///			break;
/*TODO*///		case STATIC_16BIT:
/*TODO*///			total_shrinked_pens = 32768;
/*TODO*///			break;
/*TODO*///		case PALETTIZED_16BIT:
/*TODO*///			total_shrinked_pens = Machine->drv->total_colors + RESERVED_PENS;
/*TODO*///			break;
/*TODO*///	}
/*TODO*///
/*TODO*///	shrinked_pens = malloc(total_shrinked_pens * sizeof(short));
/*TODO*///	shrinked_palette = malloc(3 * total_shrinked_pens * sizeof(unsigned char));
/*TODO*///
/*TODO*///	Machine->pens = malloc(Machine->drv->total_colors * sizeof(short));
/*TODO*///
/*TODO*///	if ((Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE))
/*TODO*///	{
/*TODO*///		/* if the palette changes dynamically, */
/*TODO*///		/* we'll need the usage arrays to help in shrinking. */
/*TODO*///		palette_used_colors = malloc((1+1+1+3+1) * Machine->drv->total_colors * sizeof(unsigned char));
/*TODO*///		pen_visiblecount = malloc(2 * Machine->drv->total_colors * sizeof(int));
/*TODO*///
/*TODO*///		if (palette_used_colors == 0 || pen_visiblecount == 0)
/*TODO*///		{
/*TODO*///			palette_stop();
/*TODO*///			return 1;
/*TODO*///		}
/*TODO*///
/*TODO*///		old_used_colors = palette_used_colors + Machine->drv->total_colors * sizeof(unsigned char);
/*TODO*///		just_remapped = old_used_colors + Machine->drv->total_colors * sizeof(unsigned char);
/*TODO*///		new_palette = just_remapped + Machine->drv->total_colors * sizeof(unsigned char);
/*TODO*///		palette_dirty = new_palette + 3*Machine->drv->total_colors * sizeof(unsigned char);
/*TODO*///		memset(palette_used_colors,PALETTE_COLOR_USED,Machine->drv->total_colors * sizeof(unsigned char));
/*TODO*///		memset(old_used_colors,PALETTE_COLOR_UNUSED,Machine->drv->total_colors * sizeof(unsigned char));
/*TODO*///		memset(palette_dirty,0,Machine->drv->total_colors * sizeof(unsigned char));
/*TODO*///		pen_cachedcount = pen_visiblecount + Machine->drv->total_colors;
/*TODO*///		memset(pen_visiblecount,0,Machine->drv->total_colors * sizeof(int));
/*TODO*///		memset(pen_cachedcount,0,Machine->drv->total_colors * sizeof(int));
/*TODO*///	}
/*TODO*///	else palette_used_colors = old_used_colors = just_remapped = new_palette = palette_dirty = 0;
/*TODO*///
/*TODO*///	if (Machine->color_depth == 8) num = 256;
/*TODO*///	else num = 65536;
/*TODO*///	palette_shadow_table = malloc(2 * num * sizeof(unsigned short));
/*TODO*///	if (palette_shadow_table == 0)
/*TODO*///	{
/*TODO*///		palette_stop();
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///	palette_highlight_table = palette_shadow_table + num;
/*TODO*///	for (i = 0;i < num;i++)
/*TODO*///		palette_shadow_table[i] = palette_highlight_table[i] = i;
/*TODO*///
/*TODO*///	if ((Machine->drv->color_table_len && (Machine->game_colortable == 0 || Machine->remapped_colortable == 0))
/*TODO*///			|| game_palette == 0 ||	palette_map == 0
/*TODO*///			|| shrinked_pens == 0 || shrinked_palette == 0 || Machine->pens == 0)
/*TODO*///	{
/*TODO*///		palette_stop();
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///void palette_stop(void)
/*TODO*///{
/*TODO*///	free(palette_used_colors);
/*TODO*///	palette_used_colors = old_used_colors = just_remapped = new_palette = palette_dirty = 0;
/*TODO*///	free(pen_visiblecount);
/*TODO*///	pen_visiblecount = 0;
/*TODO*///	free(game_palette);
/*TODO*///	game_palette = 0;
/*TODO*///	free(palette_map);
/*TODO*///	palette_map = 0;
/*TODO*///	free(Machine->game_colortable);
/*TODO*///	Machine->game_colortable = 0;
/*TODO*///	free(Machine->remapped_colortable);
/*TODO*///	Machine->remapped_colortable = 0;
/*TODO*///	free(shrinked_pens);
/*TODO*///	shrinked_pens = 0;
/*TODO*///	free(shrinked_palette);
/*TODO*///	shrinked_palette = 0;
/*TODO*///	free(Machine->pens);
/*TODO*///	Machine->pens = 0;
/*TODO*///	free(palette_shadow_table);
/*TODO*///	palette_shadow_table = 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///int palette_init(void)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///
/*TODO*///	/* We initialize the palette and colortable to some default values so that */
/*TODO*///	/* drivers which dynamically change the palette don't need a vh_init_palette() */
/*TODO*///	/* function (provided the default color table fits their needs). */
/*TODO*///
/*TODO*///	for (i = 0;i < Machine->drv->total_colors;i++)
/*TODO*///	{
/*TODO*///		game_palette[3*i + 0] = ((i & 1) >> 0) * 0xff;
/*TODO*///		game_palette[3*i + 1] = ((i & 2) >> 1) * 0xff;
/*TODO*///		game_palette[3*i + 2] = ((i & 4) >> 2) * 0xff;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* Preload the colortable with a default setting, following the same */
/*TODO*///	/* order of the palette. The driver can overwrite this in */
/*TODO*///	/* vh_init_palette() */
/*TODO*///	for (i = 0;i < Machine->drv->color_table_len;i++)
/*TODO*///		Machine->game_colortable[i] = i % Machine->drv->total_colors;
/*TODO*///
/*TODO*///	/* by default we use -1 to identify the transparent color, the driver */
/*TODO*///	/* can modify this. */
/*TODO*///	palette_transparent_color = -1;
/*TODO*///
/*TODO*///	/* now the driver can modify the default values if it wants to. */
/*TODO*///	if (Machine->drv->vh_init_palette)
/*TODO*///		(*Machine->drv->vh_init_palette)(game_palette,Machine->game_colortable,memory_region(REGION_PROMS));
/*TODO*///
/*TODO*///
/*TODO*///	switch (use_16bit)
/*TODO*///	{
/*TODO*///		case NO_16BIT:
/*TODO*///		{
/*TODO*///			/* initialize shrinked palette to all black */
/*TODO*///			for (i = 0;i < total_shrinked_pens;i++)
/*TODO*///			{
/*TODO*///				shrinked_palette[3*i + 0] =
/*TODO*///				shrinked_palette[3*i + 1] =
/*TODO*///				shrinked_palette[3*i + 2] = 0;
/*TODO*///			}
/*TODO*///
/*TODO*///			if (Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE)
/*TODO*///			{
/*TODO*///				/* initialize pen usage counters */
/*TODO*///				for (i = 0;i < DYNAMIC_MAX_PENS;i++)
/*TODO*///					pen_usage_count[i] = 0;
/*TODO*///
/*TODO*///				/* allocate two fixed pens at the beginning: */
/*TODO*///				/* transparent black */
/*TODO*///				pen_usage_count[TRANSPARENT_PEN] = 1;	/* so the pen will not be reused */
/*TODO*///
/*TODO*///				/* non transparent black */
/*TODO*///				pen_usage_count[BLACK_PEN] = 1;
/*TODO*///
/*TODO*///				/* create some defaults associations of game colors to shrinked pens. */
/*TODO*///				/* They will be dynamically modified at run time. */
/*TODO*///				for (i = 0;i < Machine->drv->total_colors;i++)
/*TODO*///					palette_map[i] = (i & 7) + 8;
/*TODO*///
/*TODO*///				if (osd_allocate_colors(total_shrinked_pens,shrinked_palette,shrinked_pens,1))
/*TODO*///					return 1;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				int j,used;
/*TODO*///
/*TODO*///
/*TODO*///if (errorlog) fprintf(errorlog,"shrinking %d colors palette...\n",Machine->drv->total_colors);
/*TODO*///
/*TODO*///				/* shrink palette to fit */
/*TODO*///				used = 0;
/*TODO*///
/*TODO*///				for (i = 0;i < Machine->drv->total_colors;i++)
/*TODO*///				{
/*TODO*///					for (j = 0;j < used;j++)
/*TODO*///					{
/*TODO*///						if (	shrinked_palette[3*j + 0] == game_palette[3*i + 0] &&
/*TODO*///								shrinked_palette[3*j + 1] == game_palette[3*i + 1] &&
/*TODO*///								shrinked_palette[3*j + 2] == game_palette[3*i + 2])
/*TODO*///							break;
/*TODO*///					}
/*TODO*///
/*TODO*///					palette_map[i] = j;
/*TODO*///
/*TODO*///					if (j == used)
/*TODO*///					{
/*TODO*///						used++;
/*TODO*///						if (used > total_shrinked_pens)
/*TODO*///						{
/*TODO*///							used = total_shrinked_pens;
/*TODO*///							palette_map[i] = total_shrinked_pens-1;
/*TODO*///							usrintf_showmessage("cannot shrink static palette");
/*TODO*///if (errorlog) fprintf(errorlog,"error: ran out of free pens to shrink the palette.\n");
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							shrinked_palette[3*j + 0] = game_palette[3*i + 0];
/*TODO*///							shrinked_palette[3*j + 1] = game_palette[3*i + 1];
/*TODO*///							shrinked_palette[3*j + 2] = game_palette[3*i + 2];
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///if (errorlog) fprintf(errorlog,"shrinked palette uses %d colors\n",used);
/*TODO*///
/*TODO*///				if (osd_allocate_colors(used,shrinked_palette,shrinked_pens,0))
/*TODO*///					return 1;
/*TODO*///			}
/*TODO*///
/*TODO*///
/*TODO*///			for (i = 0;i < Machine->drv->total_colors;i++)
/*TODO*///				Machine->pens[i] = shrinked_pens[palette_map[i]];
/*TODO*///
/*TODO*///			palette_transparent_pen = shrinked_pens[TRANSPARENT_PEN];	/* for dynamic palette games */
/*TODO*///		}
/*TODO*///		break;
/*TODO*///
/*TODO*///		case STATIC_16BIT:
/*TODO*///		{
/*TODO*///			unsigned char *p = shrinked_palette;
/*TODO*///			int r,g,b;
/*TODO*///
/*TODO*///			if (Machine->scrbitmap->depth == 16)
/*TODO*///			{
/*TODO*///				for (r = 0;r < 32;r++)
/*TODO*///				{
/*TODO*///					for (g = 0;g < 32;g++)
/*TODO*///					{
/*TODO*///						for (b = 0;b < 32;b++)
/*TODO*///						{
/*TODO*///							*p++ = (r << 3) | (r >> 2);
/*TODO*///							*p++ = (g << 3) | (g >> 2);
/*TODO*///							*p++ = (b << 3) | (b >> 2);
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				if (osd_allocate_colors(32768,shrinked_palette,shrinked_pens,0))
/*TODO*///					return 1;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				for (r = 0;r < 8;r++)
/*TODO*///				{
/*TODO*///					for (g = 0;g < 8;g++)
/*TODO*///					{
/*TODO*///						for (b = 0;b < 4;b++)
/*TODO*///						{
/*TODO*///							*p++ = (r << 5) | (r << 2) | (r >> 1);
/*TODO*///							*p++ = (g << 5) | (g << 2) | (g >> 1);
/*TODO*///							*p++ = (b << 6) | (b << 4) | (b << 2) | b;
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				if (osd_allocate_colors(256,shrinked_palette,shrinked_pens,0))
/*TODO*///					return 1;
/*TODO*///			}
/*TODO*///
/*TODO*///			for (i = 0;i < Machine->drv->total_colors;i++)
/*TODO*///			{
/*TODO*///				r = game_palette[3*i + 0];
/*TODO*///				g = game_palette[3*i + 1];
/*TODO*///				b = game_palette[3*i + 2];
/*TODO*///
/*TODO*///				Machine->pens[i] = shrinked_pens[rgbpenindex(r,g,b)];
/*TODO*///			}
/*TODO*///
/*TODO*///			palette_transparent_pen = shrinked_pens[0];	/* we are forced to use black for the transparent pen */
/*TODO*///		}
/*TODO*///		break;
/*TODO*///
/*TODO*///		case PALETTIZED_16BIT:
/*TODO*///		{
/*TODO*///			for (i = 0;i < RESERVED_PENS;i++)
/*TODO*///			{
/*TODO*///				shrinked_palette[3*i + 0] =
/*TODO*///				shrinked_palette[3*i + 1] =
/*TODO*///				shrinked_palette[3*i + 2] = 0;
/*TODO*///			}
/*TODO*///
/*TODO*///			for (i = 0;i < Machine->drv->total_colors;i++)
/*TODO*///			{
/*TODO*///				shrinked_palette[3*(i+RESERVED_PENS) + 0] = game_palette[3*i + 0];
/*TODO*///				shrinked_palette[3*(i+RESERVED_PENS) + 1] = game_palette[3*i + 1];
/*TODO*///				shrinked_palette[3*(i+RESERVED_PENS) + 2] = game_palette[3*i + 2];
/*TODO*///			}
/*TODO*///
/*TODO*///			if (osd_allocate_colors(total_shrinked_pens,shrinked_palette,shrinked_pens,(Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE)))
/*TODO*///				return 1;
/*TODO*///
/*TODO*///			for (i = 0;i < Machine->drv->total_colors;i++)
/*TODO*///				Machine->pens[i] = shrinked_pens[i + RESERVED_PENS];
/*TODO*///
/*TODO*///			palette_transparent_pen = shrinked_pens[TRANSPARENT_PEN];	/* for dynamic palette games */
/*TODO*///		}
/*TODO*///		break;
/*TODO*///	}
/*TODO*///
/*TODO*///	for (i = 0;i < Machine->drv->color_table_len;i++)
/*TODO*///	{
/*TODO*///		int color = Machine->game_colortable[i];
/*TODO*///
/*TODO*///		/* check for invalid colors set by Machine->drv->vh_init_palette */
/*TODO*///		if (color < Machine->drv->total_colors)
/*TODO*///			Machine->remapped_colortable[i] = Machine->pens[color];
/*TODO*///		else
/*TODO*///			usrintf_showmessage("colortable[%d] (=%d) out of range (total_colors = %d)",
/*TODO*///					i,color,Machine->drv->total_colors);
/*TODO*///	}
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///INLINE void palette_change_color_16_static(int color,unsigned char red,unsigned char green,unsigned char blue)
/*TODO*///{
/*TODO*///	if (color == palette_transparent_color)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///
/*TODO*///
/*TODO*///		palette_transparent_pen = shrinked_pens[rgbpenindex(red,green,blue)];
/*TODO*///
/*TODO*///		if (color == -1) return;	/* by default, palette_transparent_color is -1 */
/*TODO*///
/*TODO*///		for (i = 0;i < Machine->drv->total_colors;i++)
/*TODO*///		{
/*TODO*///			if ((old_used_colors[i] & (PALETTE_COLOR_VISIBLE | PALETTE_COLOR_TRANSPARENT_FLAG))
/*TODO*///					== (PALETTE_COLOR_VISIBLE | PALETTE_COLOR_TRANSPARENT_FLAG))
/*TODO*///				old_used_colors[i] |= PALETTE_COLOR_NEEDS_REMAP;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	if (	game_palette[3*color + 0] == red &&
/*TODO*///			game_palette[3*color + 1] == green &&
/*TODO*///			game_palette[3*color + 2] == blue)
/*TODO*///		return;
/*TODO*///
/*TODO*///	game_palette[3*color + 0] = red;
/*TODO*///	game_palette[3*color + 1] = green;
/*TODO*///	game_palette[3*color + 2] = blue;
/*TODO*///
/*TODO*///	if (old_used_colors[color] & PALETTE_COLOR_VISIBLE)
/*TODO*///		/* we'll have to reassign the color in palette_recalc() */
/*TODO*///		old_used_colors[color] |= PALETTE_COLOR_NEEDS_REMAP;
/*TODO*///}
/*TODO*///
/*TODO*///INLINE void palette_change_color_16_palettized(int color,unsigned char red,unsigned char green,unsigned char blue)
/*TODO*///{
/*TODO*///	if (color == palette_transparent_color)
/*TODO*///	{
/*TODO*///		osd_modify_pen(palette_transparent_pen,red,green,blue);
/*TODO*///
/*TODO*///		if (color == -1) return;	/* by default, palette_transparent_color is -1 */
/*TODO*///	}
/*TODO*///
/*TODO*///	if (	game_palette[3*color + 0] == red &&
/*TODO*///			game_palette[3*color + 1] == green &&
/*TODO*///			game_palette[3*color + 2] == blue)
/*TODO*///		return;
/*TODO*///
/*TODO*///	/* Machine->pens[color] might have been remapped to transparent_pen, so I */
/*TODO*///	/* use shrinked_pens[] directly */
/*TODO*///	osd_modify_pen(shrinked_pens[color + RESERVED_PENS],red,green,blue);
/*TODO*///	game_palette[3*color + 0] = red;
/*TODO*///	game_palette[3*color + 1] = green;
/*TODO*///	game_palette[3*color + 2] = blue;
/*TODO*///}
/*TODO*///
/*TODO*///INLINE void palette_change_color_8(int color,unsigned char red,unsigned char green,unsigned char blue)
/*TODO*///{
/*TODO*///	int pen;
/*TODO*///
/*TODO*///	if (color == palette_transparent_color)
/*TODO*///	{
/*TODO*///		osd_modify_pen(palette_transparent_pen,red,green,blue);
/*TODO*///
/*TODO*///		if (color == -1) return;	/* by default, palette_transparent_color is -1 */
/*TODO*///	}
/*TODO*///
/*TODO*///	if (	game_palette[3*color + 0] == red &&
/*TODO*///			game_palette[3*color + 1] == green &&
/*TODO*///			game_palette[3*color + 2] == blue)
/*TODO*///	{
/*TODO*///		palette_dirty[color] = 0;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	pen = palette_map[color];
/*TODO*///
/*TODO*///	/* if the color was used, mark it as dirty, we'll change it in palette_recalc() */
/*TODO*///	if (old_used_colors[color] & PALETTE_COLOR_VISIBLE)
/*TODO*///	{
/*TODO*///		new_palette[3*color + 0] = red;
/*TODO*///		new_palette[3*color + 1] = green;
/*TODO*///		new_palette[3*color + 2] = blue;
/*TODO*///		palette_dirty[color] = 1;
/*TODO*///	}
/*TODO*///	/* otherwise, just update the array */
/*TODO*///	else
/*TODO*///	{
/*TODO*///		game_palette[3*color + 0] = red;
/*TODO*///		game_palette[3*color + 1] = green;
/*TODO*///		game_palette[3*color + 2] = blue;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void palette_change_color(int color,unsigned char red,unsigned char green,unsigned char blue)
/*TODO*///{
/*TODO*///	if ((Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE) == 0)
/*TODO*///	{
/*TODO*///if (errorlog) fprintf(errorlog,"Error: palette_change_color() called, but VIDEO_MODIFIES_PALETTE not set.\n");
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (color >= Machine->drv->total_colors)
/*TODO*///	{
/*TODO*///if (errorlog) fprintf(errorlog,"error: palette_change_color() called with color %d, but only %d allocated.\n",color,Machine->drv->total_colors);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	switch (use_16bit)
/*TODO*///	{
/*TODO*///		case NO_16BIT:
/*TODO*///			palette_change_color_8(color,red,green,blue);
/*TODO*///			break;
/*TODO*///		case STATIC_16BIT:
/*TODO*///			palette_change_color_16_static(color,red,green,blue);
/*TODO*///			break;
/*TODO*///		case PALETTIZED_16BIT:
/*TODO*///			palette_change_color_16_palettized(color,red,green,blue);
/*TODO*///			break;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///void palette_increase_usage_count(int table_offset,unsigned int usage_mask,int color_flags)
/*TODO*///{
/*TODO*///	/* if we are not dynamically reducing the palette, return immediately. */
/*TODO*///	if (palette_used_colors == 0) return;
/*TODO*///
/*TODO*///	while (usage_mask)
/*TODO*///	{
/*TODO*///		if (usage_mask & 1)
/*TODO*///		{
/*TODO*///			if (color_flags & PALETTE_COLOR_VISIBLE)
/*TODO*///				pen_visiblecount[Machine->game_colortable[table_offset]]++;
/*TODO*///			if (color_flags & PALETTE_COLOR_CACHED)
/*TODO*///				pen_cachedcount[Machine->game_colortable[table_offset]]++;
/*TODO*///		}
/*TODO*///		table_offset++;
/*TODO*///		usage_mask >>= 1;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void palette_decrease_usage_count(int table_offset,unsigned int usage_mask,int color_flags)
/*TODO*///{
/*TODO*///	/* if we are not dynamically reducing the palette, return immediately. */
/*TODO*///	if (palette_used_colors == 0) return;
/*TODO*///
/*TODO*///	while (usage_mask)
/*TODO*///	{
/*TODO*///		if (usage_mask & 1)
/*TODO*///		{
/*TODO*///			if (color_flags & PALETTE_COLOR_VISIBLE)
/*TODO*///				pen_visiblecount[Machine->game_colortable[table_offset]]--;
/*TODO*///			if (color_flags & PALETTE_COLOR_CACHED)
/*TODO*///				pen_cachedcount[Machine->game_colortable[table_offset]]--;
/*TODO*///		}
/*TODO*///		table_offset++;
/*TODO*///		usage_mask >>= 1;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void palette_increase_usage_countx(int table_offset,int num_pens,const unsigned char *pen_data,int color_flags)
/*TODO*///{
/*TODO*///	char flag[256];
/*TODO*///	memset(flag,0,256);
/*TODO*///
/*TODO*///	while (num_pens--)
/*TODO*///	{
/*TODO*///		int pen = pen_data[num_pens];
/*TODO*///		if (flag[pen] == 0)
/*TODO*///		{
/*TODO*///			if (color_flags & PALETTE_COLOR_VISIBLE)
/*TODO*///				pen_visiblecount[Machine->game_colortable[table_offset+pen]]++;
/*TODO*///			if (color_flags & PALETTE_COLOR_CACHED)
/*TODO*///				pen_cachedcount[Machine->game_colortable[table_offset+pen]]++;
/*TODO*///			flag[pen] = 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void palette_decrease_usage_countx(int table_offset, int num_pens, const unsigned char *pen_data,int color_flags)
/*TODO*///{
/*TODO*///	char flag[256];
/*TODO*///	memset(flag,0,256);
/*TODO*///
/*TODO*///	while (num_pens--)
/*TODO*///	{
/*TODO*///		int pen = pen_data[num_pens];
/*TODO*///		if (flag[pen] == 0)
/*TODO*///		{
/*TODO*///			if (color_flags & PALETTE_COLOR_VISIBLE)
/*TODO*///				pen_visiblecount[Machine->game_colortable[table_offset+pen]]--;
/*TODO*///			if (color_flags & PALETTE_COLOR_CACHED)
/*TODO*///				pen_cachedcount[Machine->game_colortable[table_offset+pen]]--;
/*TODO*///			flag[pen] = 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void palette_init_used_colors(void)
/*TODO*///{
/*TODO*///	int pen;
/*TODO*///
/*TODO*///
/*TODO*///	/* if we are not dynamically reducing the palette, return immediately. */
/*TODO*///	if (palette_used_colors == 0) return;
/*TODO*///
/*TODO*///	memset(palette_used_colors,PALETTE_COLOR_UNUSED,Machine->drv->total_colors * sizeof(unsigned char));
/*TODO*///
/*TODO*///	for (pen = 0;pen < Machine->drv->total_colors;pen++)
/*TODO*///	{
/*TODO*///		if (pen_visiblecount[pen]) palette_used_colors[pen] |= PALETTE_COLOR_VISIBLE;
/*TODO*///		if (pen_cachedcount[pen]) palette_used_colors[pen] |= PALETTE_COLOR_CACHED;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///static unsigned char rgb6_to_pen[64][64][64];
/*TODO*///
/*TODO*///static void build_rgb_to_pen(void)
/*TODO*///{
/*TODO*///	int i,rr,gg,bb;
/*TODO*///
/*TODO*///	memset(rgb6_to_pen,DYNAMIC_MAX_PENS,sizeof(rgb6_to_pen));
/*TODO*///	rgb6_to_pen[0][0][0] = BLACK_PEN;
/*TODO*///
/*TODO*///	for (i = 0;i < DYNAMIC_MAX_PENS;i++)
/*TODO*///	{
/*TODO*///		if (pen_usage_count[i] > 0)
/*TODO*///		{
/*TODO*///			rr = shrinked_palette[3*i + 0] >> 2;
/*TODO*///			gg = shrinked_palette[3*i + 1] >> 2;
/*TODO*///			bb = shrinked_palette[3*i + 2] >> 2;
/*TODO*///
/*TODO*///			if (rgb6_to_pen[rr][gg][bb] == DYNAMIC_MAX_PENS)
/*TODO*///			{
/*TODO*///				int j,max;
/*TODO*///
/*TODO*///				rgb6_to_pen[rr][gg][bb] = i;
/*TODO*///				max = pen_usage_count[i];
/*TODO*///
/*TODO*///				/* to reduce flickering during remaps, find the pen used by most colors */
/*TODO*///				for (j = i+1;j < DYNAMIC_MAX_PENS;j++)
/*TODO*///				{
/*TODO*///					if (pen_usage_count[j] > max &&
/*TODO*///							rr == (shrinked_palette[3*j + 0] >> 2) &&
/*TODO*///							gg == (shrinked_palette[3*j + 1] >> 2) &&
/*TODO*///							bb == (shrinked_palette[3*j + 2] >> 2))
/*TODO*///					{
/*TODO*///						rgb6_to_pen[rr][gg][bb] = j;
/*TODO*///						max = pen_usage_count[j];
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static int compress_palette(void)
/*TODO*///{
/*TODO*///	int i,j,saved,r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	build_rgb_to_pen();
/*TODO*///
/*TODO*///	saved = 0;
/*TODO*///
/*TODO*///	for (i = 0;i < Machine->drv->total_colors;i++)
/*TODO*///	{
/*TODO*///		/* merge pens of the same color */
/*TODO*///		if ((old_used_colors[i] & PALETTE_COLOR_VISIBLE) &&
/*TODO*///				!(old_used_colors[i] & (PALETTE_COLOR_NEEDS_REMAP|PALETTE_COLOR_TRANSPARENT_FLAG)))
/*TODO*///		{
/*TODO*///			r = game_palette[3*i + 0] >> 2;
/*TODO*///			g = game_palette[3*i + 1] >> 2;
/*TODO*///			b = game_palette[3*i + 2] >> 2;
/*TODO*///
/*TODO*///			j = rgb6_to_pen[r][g][b];
/*TODO*///
/*TODO*///			if (palette_map[i] != j)
/*TODO*///			{
/*TODO*///				just_remapped[i] = 1;
/*TODO*///
/*TODO*///				pen_usage_count[palette_map[i]]--;
/*TODO*///				if (pen_usage_count[palette_map[i]] == 0)
/*TODO*///					saved++;
/*TODO*///				palette_map[i] = j;
/*TODO*///				pen_usage_count[palette_map[i]]++;
/*TODO*///				Machine->pens[i] = shrinked_pens[palette_map[i]];
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///#if VERBOSE
/*TODO*///if (errorlog)
/*TODO*///{
/*TODO*///	int subcount[8];
/*TODO*///
/*TODO*///
/*TODO*///	for (i = 0;i < 8;i++)
/*TODO*///		subcount[i] = 0;
/*TODO*///
/*TODO*///	for (i = 0;i < Machine->drv->total_colors;i++)
/*TODO*///		subcount[palette_used_colors[i]]++;
/*TODO*///
/*TODO*///	fprintf(errorlog,"Ran out of pens! %d colors used (%d unused, %d visible %d cached %d visible+cached, %d transparent)\n",
/*TODO*///			subcount[PALETTE_COLOR_VISIBLE]+subcount[PALETTE_COLOR_CACHED]+subcount[PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED]+subcount[PALETTE_COLOR_TRANSPARENT],
/*TODO*///			subcount[PALETTE_COLOR_UNUSED],
/*TODO*///			subcount[PALETTE_COLOR_VISIBLE],
/*TODO*///			subcount[PALETTE_COLOR_CACHED],
/*TODO*///			subcount[PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED],
/*TODO*///			subcount[PALETTE_COLOR_TRANSPARENT]);
/*TODO*///	fprintf(errorlog,"Compressed the palette, saving %d pens\n",saved);
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*///	return saved;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///static const unsigned char *palette_recalc_16_static(void)
/*TODO*///{
/*TODO*///	int i,color;
/*TODO*///	int did_remap = 0;
/*TODO*///	int need_refresh = 0;
/*TODO*///
/*TODO*///
/*TODO*///	memset(just_remapped,0,Machine->drv->total_colors * sizeof(unsigned char));
/*TODO*///
/*TODO*///	for (color = 0;color < Machine->drv->total_colors;color++)
/*TODO*///	{
/*TODO*///		/* the comparison between palette_used_colors and old_used_colors also includes */
/*TODO*///		/* PALETTE_COLOR_NEEDS_REMAP which might have been set by palette_change_color() */
/*TODO*///		if ((palette_used_colors[color] & PALETTE_COLOR_VISIBLE) &&
/*TODO*///				palette_used_colors[color] != old_used_colors[color])
/*TODO*///		{
/*TODO*///			int r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///			did_remap = 1;
/*TODO*///			if (old_used_colors[color] & palette_used_colors[color] & PALETTE_COLOR_CACHED)
/*TODO*///			{
/*TODO*///				/* the color was and still is cached, we'll have to redraw everything */
/*TODO*///				need_refresh = 1;
/*TODO*///				just_remapped[color] = 1;
/*TODO*///			}
/*TODO*///
/*TODO*///			if (palette_used_colors[color] & PALETTE_COLOR_TRANSPARENT_FLAG)
/*TODO*///				Machine->pens[color] = palette_transparent_pen;
/*TODO*///			else
/*TODO*///			{
/*TODO*///				r = game_palette[3*color + 0];
/*TODO*///				g = game_palette[3*color + 1];
/*TODO*///				b = game_palette[3*color + 2];
/*TODO*///
/*TODO*///				Machine->pens[color] = shrinked_pens[rgbpenindex(r,g,b)];
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		old_used_colors[color] = palette_used_colors[color];
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	if (did_remap)
/*TODO*///	{
/*TODO*///		/* rebuild the color lookup table */
/*TODO*///		for (i = 0;i < Machine->drv->color_table_len;i++)
/*TODO*///			Machine->remapped_colortable[i] = Machine->pens[Machine->game_colortable[i]];
/*TODO*///	}
/*TODO*///
/*TODO*///	if (need_refresh) return just_remapped;
/*TODO*///	else return 0;
/*TODO*///}
/*TODO*///
/*TODO*///static const unsigned char *palette_recalc_16_palettized(void)
/*TODO*///{
/*TODO*///	int i,color;
/*TODO*///	int did_remap = 0;
/*TODO*///	int need_refresh = 0;
/*TODO*///
/*TODO*///
/*TODO*///	memset(just_remapped,0,Machine->drv->total_colors * sizeof(unsigned char));
/*TODO*///
/*TODO*///	for (color = 0;color < Machine->drv->total_colors;color++)
/*TODO*///	{
/*TODO*///		if ((palette_used_colors[color] & PALETTE_COLOR_TRANSPARENT_FLAG) !=
/*TODO*///				(old_used_colors[color] & PALETTE_COLOR_TRANSPARENT_FLAG))
/*TODO*///		{
/*TODO*///			did_remap = 1;
/*TODO*///			if (old_used_colors[color] & palette_used_colors[color] & PALETTE_COLOR_CACHED)
/*TODO*///			{
/*TODO*///				/* the color was and still is cached, we'll have to redraw everything */
/*TODO*///				need_refresh = 1;
/*TODO*///				just_remapped[color] = 1;
/*TODO*///			}
/*TODO*///
/*TODO*///			if (palette_used_colors[color] & PALETTE_COLOR_TRANSPARENT_FLAG)
/*TODO*///				Machine->pens[color] = palette_transparent_pen;
/*TODO*///			else
/*TODO*///				Machine->pens[color] = shrinked_pens[color + RESERVED_PENS];
/*TODO*///		}
/*TODO*///
/*TODO*///		old_used_colors[color] = palette_used_colors[color];
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	if (did_remap)
/*TODO*///	{
/*TODO*///		/* rebuild the color lookup table */
/*TODO*///		for (i = 0;i < Machine->drv->color_table_len;i++)
/*TODO*///			Machine->remapped_colortable[i] = Machine->pens[Machine->game_colortable[i]];
/*TODO*///	}
/*TODO*///
/*TODO*///	if (need_refresh) return just_remapped;
/*TODO*///	else return 0;
/*TODO*///}
/*TODO*///
/*TODO*///static const unsigned char *palette_recalc_8(void)
/*TODO*///{
/*TODO*///	int i,color;
/*TODO*///	int did_remap = 0;
/*TODO*///	int need_refresh = 0;
/*TODO*///	int first_free_pen;
/*TODO*///	int ran_out = 0;
/*TODO*///	int reuse_pens = 0;
/*TODO*///	int need,avail;
/*TODO*///
/*TODO*///
/*TODO*///	memset(just_remapped,0,Machine->drv->total_colors * sizeof(unsigned char));
/*TODO*///
/*TODO*///
/*TODO*///	/* first of all, apply the changes to the palette which were */
/*TODO*///	/* requested since last update */
/*TODO*///	for (color = 0;color < Machine->drv->total_colors;color++)
/*TODO*///	{
/*TODO*///		if (palette_dirty[color])
/*TODO*///		{
/*TODO*///			int r,g,b,pen;
/*TODO*///
/*TODO*///
/*TODO*///			pen = palette_map[color];
/*TODO*///			r = new_palette[3*color + 0];
/*TODO*///			g = new_palette[3*color + 1];
/*TODO*///			b = new_palette[3*color + 2];
/*TODO*///
/*TODO*///			/* if the color maps to an exclusive pen, just change it */
/*TODO*///			if (pen_usage_count[pen] == 1)
/*TODO*///			{
/*TODO*///				palette_dirty[color] = 0;
/*TODO*///				game_palette[3*color + 0] = r;
/*TODO*///				game_palette[3*color + 1] = g;
/*TODO*///				game_palette[3*color + 2] = b;
/*TODO*///
/*TODO*///				shrinked_palette[3*pen + 0] = r;
/*TODO*///				shrinked_palette[3*pen + 1] = g;
/*TODO*///				shrinked_palette[3*pen + 2] = b;
/*TODO*///				osd_modify_pen(Machine->pens[color],r,g,b);
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				if (pen < RESERVED_PENS)
/*TODO*///				{
/*TODO*///					/* the color uses a reserved pen, the only thing we can do is remap it */
/*TODO*///					for (i = color;i < Machine->drv->total_colors;i++)
/*TODO*///					{
/*TODO*///						if (palette_dirty[i] != 0 && palette_map[i] == pen)
/*TODO*///						{
/*TODO*///							palette_dirty[i] = 0;
/*TODO*///							game_palette[3*i + 0] = new_palette[3*i + 0];
/*TODO*///							game_palette[3*i + 1] = new_palette[3*i + 1];
/*TODO*///							game_palette[3*i + 2] = new_palette[3*i + 2];
/*TODO*///							old_used_colors[i] |= PALETTE_COLOR_NEEDS_REMAP;
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					/* the pen is shared with other colors, let's see if all of them */
/*TODO*///					/* have been changed to the same value */
/*TODO*///					for (i = 0;i < Machine->drv->total_colors;i++)
/*TODO*///					{
/*TODO*///						if ((old_used_colors[i] & PALETTE_COLOR_VISIBLE) &&
/*TODO*///								palette_map[i] == pen)
/*TODO*///						{
/*TODO*///							if (palette_dirty[i] == 0 ||
/*TODO*///									new_palette[3*i + 0] != r ||
/*TODO*///									new_palette[3*i + 1] != g ||
/*TODO*///									new_palette[3*i + 2] != b)
/*TODO*///								break;
/*TODO*///						}
/*TODO*///					}
/*TODO*///
/*TODO*///					if (i == Machine->drv->total_colors)
/*TODO*///					{
/*TODO*///						/* all colors sharing this pen still are the same, so we */
/*TODO*///						/* just change the palette. */
/*TODO*///						shrinked_palette[3*pen + 0] = r;
/*TODO*///						shrinked_palette[3*pen + 1] = g;
/*TODO*///						shrinked_palette[3*pen + 2] = b;
/*TODO*///						osd_modify_pen(Machine->pens[color],r,g,b);
/*TODO*///
/*TODO*///						for (i = color;i < Machine->drv->total_colors;i++)
/*TODO*///						{
/*TODO*///							if (palette_dirty[i] != 0 && palette_map[i] == pen)
/*TODO*///							{
/*TODO*///								palette_dirty[i] = 0;
/*TODO*///								game_palette[3*i + 0] = r;
/*TODO*///								game_palette[3*i + 1] = g;
/*TODO*///								game_palette[3*i + 2] = b;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						/* the colors sharing this pen now are different, we'll */
/*TODO*///						/* have to remap them. */
/*TODO*///						for (i = color;i < Machine->drv->total_colors;i++)
/*TODO*///						{
/*TODO*///							if (palette_dirty[i] != 0 && palette_map[i] == pen)
/*TODO*///							{
/*TODO*///								palette_dirty[i] = 0;
/*TODO*///								game_palette[3*i + 0] = new_palette[3*i + 0];
/*TODO*///								game_palette[3*i + 1] = new_palette[3*i + 1];
/*TODO*///								game_palette[3*i + 2] = new_palette[3*i + 2];
/*TODO*///								old_used_colors[i] |= PALETTE_COLOR_NEEDS_REMAP;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	need = 0;
/*TODO*///	for (i = 0;i < Machine->drv->total_colors;i++)
/*TODO*///	{
/*TODO*///		if ((palette_used_colors[i] & PALETTE_COLOR_VISIBLE) && palette_used_colors[i] != old_used_colors[i])
/*TODO*///			need++;
/*TODO*///	}
/*TODO*///	if (need > 0)
/*TODO*///	{
/*TODO*///		avail = 0;
/*TODO*///		for (i = 0;i < DYNAMIC_MAX_PENS;i++)
/*TODO*///		{
/*TODO*///			if (pen_usage_count[i] == 0)
/*TODO*///				avail++;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (need > avail)
/*TODO*///		{
/*TODO*///#if VERBOSE
/*TODO*///if (errorlog) fprintf(errorlog,"Need %d new pens; %d available. I'll reuse some pens.\n",need,avail);
/*TODO*///#endif
/*TODO*///			reuse_pens = 1;
/*TODO*///			build_rgb_to_pen();
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	first_free_pen = RESERVED_PENS;
/*TODO*///	for (color = 0;color < Machine->drv->total_colors;color++)
/*TODO*///	{
/*TODO*///		/* the comparison between palette_used_colors and old_used_colors also includes */
/*TODO*///		/* PALETTE_COLOR_NEEDS_REMAP which might have been set previously */
/*TODO*///		if ((palette_used_colors[color] & PALETTE_COLOR_VISIBLE) &&
/*TODO*///				palette_used_colors[color] != old_used_colors[color])
/*TODO*///		{
/*TODO*///			int r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///			if (old_used_colors[color] & PALETTE_COLOR_VISIBLE)
/*TODO*///			{
/*TODO*///				pen_usage_count[palette_map[color]]--;
/*TODO*///				old_used_colors[color] &= ~PALETTE_COLOR_VISIBLE;
/*TODO*///			}
/*TODO*///
/*TODO*///			r = game_palette[3*color + 0];
/*TODO*///			g = game_palette[3*color + 1];
/*TODO*///			b = game_palette[3*color + 2];
/*TODO*///
/*TODO*///			if (palette_used_colors[color] & PALETTE_COLOR_TRANSPARENT_FLAG)
/*TODO*///			{
/*TODO*///				if (palette_map[color] != TRANSPARENT_PEN)
/*TODO*///				{
/*TODO*///					/* use the fixed transparent black for this */
/*TODO*///					did_remap = 1;
/*TODO*///					if (old_used_colors[color] & palette_used_colors[color] & PALETTE_COLOR_CACHED)
/*TODO*///					{
/*TODO*///						/* the color was and still is cached, we'll have to redraw everything */
/*TODO*///						need_refresh = 1;
/*TODO*///						just_remapped[color] = 1;
/*TODO*///					}
/*TODO*///
/*TODO*///					palette_map[color] = TRANSPARENT_PEN;
/*TODO*///				}
/*TODO*///				pen_usage_count[palette_map[color]]++;
/*TODO*///				Machine->pens[color] = shrinked_pens[palette_map[color]];
/*TODO*///				old_used_colors[color] = palette_used_colors[color];
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				if (reuse_pens)
/*TODO*///				{
/*TODO*///					i = rgb6_to_pen[r >> 2][g >> 2][b >> 2];
/*TODO*///					if (i != DYNAMIC_MAX_PENS)
/*TODO*///					{
/*TODO*///						if (palette_map[color] != i)
/*TODO*///						{
/*TODO*///							did_remap = 1;
/*TODO*///							if (old_used_colors[color] & palette_used_colors[color] & PALETTE_COLOR_CACHED)
/*TODO*///							{
/*TODO*///								/* the color was and still is cached, we'll have to redraw everything */
/*TODO*///								need_refresh = 1;
/*TODO*///								just_remapped[color] = 1;
/*TODO*///							}
/*TODO*///
/*TODO*///							palette_map[color] = i;
/*TODO*///						}
/*TODO*///						pen_usage_count[palette_map[color]]++;
/*TODO*///						Machine->pens[color] = shrinked_pens[palette_map[color]];
/*TODO*///						old_used_colors[color] = palette_used_colors[color];
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				/* if we still haven't found a pen, choose a new one */
/*TODO*///				if (old_used_colors[color] != palette_used_colors[color])
/*TODO*///				{
/*TODO*///					/* if possible, reuse the last associated pen */
/*TODO*///					if (pen_usage_count[palette_map[color]] == 0)
/*TODO*///					{
/*TODO*///						pen_usage_count[palette_map[color]]++;
/*TODO*///					}
/*TODO*///					else	/* allocate a new pen */
/*TODO*///					{
/*TODO*///retry:
/*TODO*///						while (first_free_pen < DYNAMIC_MAX_PENS && pen_usage_count[first_free_pen] > 0)
/*TODO*///							first_free_pen++;
/*TODO*///
/*TODO*///						if (first_free_pen < DYNAMIC_MAX_PENS)
/*TODO*///						{
/*TODO*///							did_remap = 1;
/*TODO*///							if (old_used_colors[color] & palette_used_colors[color] & PALETTE_COLOR_CACHED)
/*TODO*///							{
/*TODO*///								/* the color was and still is cached, we'll have to redraw everything */
/*TODO*///								need_refresh = 1;
/*TODO*///								just_remapped[color] = 1;
/*TODO*///							}
/*TODO*///
/*TODO*///							palette_map[color] = first_free_pen;
/*TODO*///							pen_usage_count[palette_map[color]]++;
/*TODO*///							Machine->pens[color] = shrinked_pens[palette_map[color]];
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							/* Ran out of pens! Let's see what we can do. */
/*TODO*///
/*TODO*///							if (ran_out == 0)
/*TODO*///							{
/*TODO*///								ran_out++;
/*TODO*///
/*TODO*///								/* from now on, try to reuse already allocated pens */
/*TODO*///								reuse_pens = 1;
/*TODO*///								if (compress_palette() > 0)
/*TODO*///								{
/*TODO*///									did_remap = 1;
/*TODO*///									need_refresh = 1;	/* we'll have to redraw everything */
/*TODO*///
/*TODO*///									first_free_pen = RESERVED_PENS;
/*TODO*///									goto retry;
/*TODO*///								}
/*TODO*///							}
/*TODO*///
/*TODO*///							ran_out++;
/*TODO*///
/*TODO*///							/* we failed, but go on with the loop, there might */
/*TODO*///							/* be some transparent pens to remap */
/*TODO*///
/*TODO*///							continue;
/*TODO*///						}
/*TODO*///					}
/*TODO*///
/*TODO*///					{
/*TODO*///						int rr,gg,bb;
/*TODO*///
/*TODO*///						i = palette_map[color];
/*TODO*///						rr = shrinked_palette[3*i + 0] >> 2;
/*TODO*///						gg = shrinked_palette[3*i + 1] >> 2;
/*TODO*///						bb = shrinked_palette[3*i + 2] >> 2;
/*TODO*///						if (rgb6_to_pen[rr][gg][bb] == i)
/*TODO*///							rgb6_to_pen[rr][gg][bb] = DYNAMIC_MAX_PENS;
/*TODO*///
/*TODO*///						shrinked_palette[3*i + 0] = r;
/*TODO*///						shrinked_palette[3*i + 1] = g;
/*TODO*///						shrinked_palette[3*i + 2] = b;
/*TODO*///						osd_modify_pen(Machine->pens[color],r,g,b);
/*TODO*///
/*TODO*///						r >>= 2;
/*TODO*///						g >>= 2;
/*TODO*///						b >>= 2;
/*TODO*///						if (rgb6_to_pen[r][g][b] == DYNAMIC_MAX_PENS)
/*TODO*///							rgb6_to_pen[r][g][b] = i;
/*TODO*///					}
/*TODO*///
/*TODO*///					old_used_colors[color] = palette_used_colors[color];
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	if (ran_out > 1)
/*TODO*///	{
/*TODO*///#ifdef MAME_DEBUG
/*TODO*///		char buf[80];
/*TODO*///
/*TODO*///		sprintf(buf,"Error: Palette overflow -%d",ran_out-1);
/*TODO*///		usrintf_showmessage(buf);
/*TODO*///#endif
/*TODO*///if (errorlog) fprintf(errorlog,"Error: no way to shrink the palette to 256 colors, left out %d colors.\n",ran_out-1);
/*TODO*///#if 0
/*TODO*///fprintf(errorlog,"color list:\n");
/*TODO*///for (color = 0;color < Machine->drv->total_colors;color++)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///	r = game_palette[3*color + 0];
/*TODO*///	g = game_palette[3*color + 1];
/*TODO*///	b = game_palette[3*color + 2];
/*TODO*///	if (palette_used_colors[color] & PALETTE_COLOR_VISIBLE)
/*TODO*///		fprintf(errorlog,"%02x %02x %02x\n",r,g,b);
/*TODO*///}
/*TODO*///#endif
/*TODO*///	}
/*TODO*///
/*TODO*///	/* Reclaim unused pens; we do this AFTER allocating the new ones, to avoid */
/*TODO*///	/* using the same pen for two different colors in two consecutive frames, */
/*TODO*///	/* which might cause flicker. */
/*TODO*///	for (color = 0;color < Machine->drv->total_colors;color++)
/*TODO*///	{
/*TODO*///		if (!(palette_used_colors[color] & PALETTE_COLOR_VISIBLE))
/*TODO*///		{
/*TODO*///			if (old_used_colors[color] & PALETTE_COLOR_VISIBLE)
/*TODO*///				pen_usage_count[palette_map[color]]--;
/*TODO*///			old_used_colors[color] = palette_used_colors[color];
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///#ifdef PEDANTIC
/*TODO*///	/* invalidate unused pens to make bugs in color allocation evident. */
/*TODO*///	for (i = 0;i < DYNAMIC_MAX_PENS;i++)
/*TODO*///	{
/*TODO*///		if (pen_usage_count[i] == 0)
/*TODO*///		{
/*TODO*///			int r,g,b;
/*TODO*///			r = rand() & 0xff;
/*TODO*///			g = rand() & 0xff;
/*TODO*///			b = rand() & 0xff;
/*TODO*///			shrinked_palette[3*i + 0] = r;
/*TODO*///			shrinked_palette[3*i + 1] = g;
/*TODO*///			shrinked_palette[3*i + 2] = b;
/*TODO*///			osd_modify_pen(shrinked_pens[i],r,g,b);
/*TODO*///		}
/*TODO*///	}
/*TODO*///#endif
/*TODO*///
/*TODO*///	if (did_remap)
/*TODO*///	{
/*TODO*///		/* rebuild the color lookup table */
/*TODO*///		for (i = 0;i < Machine->drv->color_table_len;i++)
/*TODO*///			Machine->remapped_colortable[i] = Machine->pens[Machine->game_colortable[i]];
/*TODO*///	}
/*TODO*///
/*TODO*///	if (need_refresh)
/*TODO*///	{
/*TODO*///		if (errorlog)
/*TODO*///		{
/*TODO*///			int used;
/*TODO*///
/*TODO*///			used = 0;
/*TODO*///			for (i = 0;i < DYNAMIC_MAX_PENS;i++)
/*TODO*///			{
/*TODO*///				if (pen_usage_count[i] > 0)
/*TODO*///					used++;
/*TODO*///			}
/*TODO*///
/*TODO*///#if VERBOSE
/*TODO*///			fprintf(errorlog,"Did a palette remap, need a full screen redraw (%d pens used).\n",used);
/*TODO*///#endif
/*TODO*///		}
/*TODO*///		return just_remapped;
/*TODO*///	}
/*TODO*///	else return 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///const unsigned char *palette_recalc(void)
/*TODO*///{
/*TODO*///	/* if we are not dynamically reducing the palette, return immediately. */
/*TODO*///	if (palette_used_colors == 0)
/*TODO*///		return 0;
/*TODO*///	switch (use_16bit)
/*TODO*///	{
/*TODO*///		case NO_16BIT:
/*TODO*///		default:
/*TODO*///			return palette_recalc_8();
/*TODO*///		case STATIC_16BIT:
/*TODO*///			return palette_recalc_16_static();
/*TODO*///		case PALETTIZED_16BIT:
/*TODO*///			return palette_recalc_16_palettized();
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////******************************************************************************
/*TODO*///
/*TODO*/// Commonly used palette RAM handling functions
/*TODO*///
/*TODO*///******************************************************************************/
/*TODO*///
/*TODO*///unsigned char *paletteram,*paletteram_2;
/*TODO*///
/*TODO*///int paletteram_r(int offset)
/*TODO*///{
/*TODO*///	return paletteram[offset];
/*TODO*///}
/*TODO*///
/*TODO*///int paletteram_2_r(int offset)
/*TODO*///{
/*TODO*///	return paletteram_2[offset];
/*TODO*///}
/*TODO*///
/*TODO*///int paletteram_word_r(int offset)
/*TODO*///{
/*TODO*///	return READ_WORD(&paletteram[offset]);
/*TODO*///}
/*TODO*///
/*TODO*///int paletteram_2_word_r(int offset)
/*TODO*///{
/*TODO*///	return READ_WORD(&paletteram_2[offset]);
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_RRRGGGBB_w(int offset,int data)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///	int bit0,bit1,bit2;
/*TODO*///
/*TODO*///
/*TODO*///	paletteram[offset] = data;
/*TODO*///
/*TODO*///	/* red component */
/*TODO*///	bit0 = (data >> 5) & 0x01;
/*TODO*///	bit1 = (data >> 6) & 0x01;
/*TODO*///	bit2 = (data >> 7) & 0x01;
/*TODO*///	r = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
/*TODO*///	/* green component */
/*TODO*///	bit0 = (data >> 2) & 0x01;
/*TODO*///	bit1 = (data >> 3) & 0x01;
/*TODO*///	bit2 = (data >> 4) & 0x01;
/*TODO*///	g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
/*TODO*///	/* blue component */
/*TODO*///	bit0 = 0;
/*TODO*///	bit1 = (data >> 0) & 0x01;
/*TODO*///	bit2 = (data >> 1) & 0x01;
/*TODO*///	b = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
/*TODO*///
/*TODO*///	palette_change_color(offset,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void paletteram_BBGGGRRR_w(int offset,int data)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///	int bit0,bit1,bit2;
/*TODO*///
/*TODO*///
/*TODO*///	paletteram[offset] = data;
/*TODO*///
/*TODO*///	/* red component */
/*TODO*///	bit0 = (data >> 0) & 0x01;
/*TODO*///	bit1 = (data >> 1) & 0x01;
/*TODO*///	bit2 = (data >> 2) & 0x01;
/*TODO*///	r = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
/*TODO*///	/* green component */
/*TODO*///	bit0 = (data >> 3) & 0x01;
/*TODO*///	bit1 = (data >> 4) & 0x01;
/*TODO*///	bit2 = (data >> 5) & 0x01;
/*TODO*///	g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
/*TODO*///	/* blue component */
/*TODO*///	bit0 = 0;
/*TODO*///	bit1 = (data >> 6) & 0x01;
/*TODO*///	bit2 = (data >> 7) & 0x01;
/*TODO*///	b = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
/*TODO*///
/*TODO*///	palette_change_color(offset,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void paletteram_IIBBGGRR_w(int offset,int data)
/*TODO*///{
/*TODO*///	int r,g,b,i;
/*TODO*///
/*TODO*///
/*TODO*///	paletteram[offset] = data;
/*TODO*///
/*TODO*///	i = (data >> 6) & 0x03;
/*TODO*///	/* red component */
/*TODO*///	r = (data << 2) & 0x0c;
/*TODO*///	if (r) r |= i;
/*TODO*///	r *= 0x11;
/*TODO*///	/* green component */
/*TODO*///	g = (data >> 0) & 0x0c;
/*TODO*///	if (g) g |= i;
/*TODO*///	g *= 0x11;
/*TODO*///	/* blue component */
/*TODO*///	b = (data >> 2) & 0x0c;
/*TODO*///	if (b) b |= i;
/*TODO*///	b *= 0x11;
/*TODO*///
/*TODO*///	palette_change_color(offset,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void paletteram_BBGGRRII_w(int offset,int data)
/*TODO*///{
/*TODO*///	int r,g,b,i;
/*TODO*///
/*TODO*///
/*TODO*///	paletteram[offset] = data;
/*TODO*///
/*TODO*///	i = (data >> 0) & 0x03;
/*TODO*///	/* red component */
/*TODO*///	r = (((data >> 0) & 0x0c) | i) * 0x11;
/*TODO*///	/* green component */
/*TODO*///	g = (((data >> 2) & 0x0c) | i) * 0x11;
/*TODO*///	/* blue component */
/*TODO*///	b = (((data >> 4) & 0x0c) | i) * 0x11;
/*TODO*///
/*TODO*///	palette_change_color(offset,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_xxxxBBBBGGGGRRRR(int color,int data)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	r = (data >> 0) & 0x0f;
/*TODO*///	g = (data >> 4) & 0x0f;
/*TODO*///	b = (data >> 8) & 0x0f;
/*TODO*///
/*TODO*///	r = (r << 4) | r;
/*TODO*///	g = (g << 4) | g;
/*TODO*///	b = (b << 4) | b;
/*TODO*///
/*TODO*///	palette_change_color(color,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_xxxxBBBBGGGGRRRR_w(int offset,int data)
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xxxxBBBBGGGGRRRR(offset / 2,paletteram[offset & ~1] | (paletteram[offset | 1] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_xxxxBBBBGGGGRRRR_swap_w(int offset,int data)
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xxxxBBBBGGGGRRRR(offset / 2,paletteram[offset | 1] | (paletteram[offset & ~1] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_xxxxBBBBGGGGRRRR_split1_w(int offset,int data)
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xxxxBBBBGGGGRRRR(offset,paletteram[offset] | (paletteram_2[offset] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_xxxxBBBBGGGGRRRR_split2_w(int offset,int data)
/*TODO*///{
/*TODO*///	paletteram_2[offset] = data;
/*TODO*///	changecolor_xxxxBBBBGGGGRRRR(offset,paletteram[offset] | (paletteram_2[offset] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_xxxxBBBBGGGGRRRR_word_w(int offset,int data)
/*TODO*///{
/*TODO*///	int oldword = READ_WORD(&paletteram[offset]);
/*TODO*///	int newword = COMBINE_WORD(oldword,data);
/*TODO*///
/*TODO*///
/*TODO*///	WRITE_WORD(&paletteram[offset],newword);
/*TODO*///	changecolor_xxxxBBBBGGGGRRRR(offset / 2,newword);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_xxxxBBBBRRRRGGGG(int color,int data)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	r = (data >> 4) & 0x0f;
/*TODO*///	g = (data >> 0) & 0x0f;
/*TODO*///	b = (data >> 8) & 0x0f;
/*TODO*///
/*TODO*///	r = (r << 4) | r;
/*TODO*///	g = (g << 4) | g;
/*TODO*///	b = (b << 4) | b;
/*TODO*///
/*TODO*///	palette_change_color(color,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_xxxxBBBBRRRRGGGG_w(int offset,int data)
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xxxxBBBBRRRRGGGG(offset / 2,paletteram[offset & ~1] | (paletteram[offset | 1] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_xxxxBBBBRRRRGGGG_swap_w(int offset,int data)
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xxxxBBBBRRRRGGGG(offset / 2,paletteram[offset | 1] | (paletteram[offset & ~1] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_xxxxBBBBRRRRGGGG_split1_w(int offset,int data)
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xxxxBBBBRRRRGGGG(offset,paletteram[offset] | (paletteram_2[offset] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_xxxxBBBBRRRRGGGG_split2_w(int offset,int data)
/*TODO*///{
/*TODO*///	paletteram_2[offset] = data;
/*TODO*///	changecolor_xxxxBBBBRRRRGGGG(offset,paletteram[offset] | (paletteram_2[offset] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_xxxxRRRRBBBBGGGG(int color,int data)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	r = (data >> 8) & 0x0f;
/*TODO*///	g = (data >> 0) & 0x0f;
/*TODO*///	b = (data >> 4) & 0x0f;
/*TODO*///
/*TODO*///	r = (r << 4) | r;
/*TODO*///	g = (g << 4) | g;
/*TODO*///	b = (b << 4) | b;
/*TODO*///
/*TODO*///	palette_change_color(color,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_xxxxRRRRBBBBGGGG_split1_w(int offset,int data)
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xxxxRRRRBBBBGGGG(offset,paletteram[offset] | (paletteram_2[offset] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_xxxxRRRRBBBBGGGG_split2_w(int offset,int data)
/*TODO*///{
/*TODO*///	paletteram_2[offset] = data;
/*TODO*///	changecolor_xxxxRRRRBBBBGGGG(offset,paletteram[offset] | (paletteram_2[offset] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_xxxxRRRRGGGGBBBB(int color,int data)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	r = (data >> 8) & 0x0f;
/*TODO*///	g = (data >> 4) & 0x0f;
/*TODO*///	b = (data >> 0) & 0x0f;
/*TODO*///
/*TODO*///	r = (r << 4) | r;
/*TODO*///	g = (g << 4) | g;
/*TODO*///	b = (b << 4) | b;
/*TODO*///
/*TODO*///	palette_change_color(color,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_xxxxRRRRGGGGBBBB_w(int offset,int data)
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xxxxRRRRGGGGBBBB(offset / 2,paletteram[offset & ~1] | (paletteram[offset | 1] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_xxxxRRRRGGGGBBBB_word_w(int offset,int data)
/*TODO*///{
/*TODO*///	int oldword = READ_WORD(&paletteram[offset]);
/*TODO*///	int newword = COMBINE_WORD(oldword,data);
/*TODO*///
/*TODO*///
/*TODO*///	WRITE_WORD(&paletteram[offset],newword);
/*TODO*///	changecolor_xxxxRRRRGGGGBBBB(offset / 2,newword);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_RRRRGGGGBBBBxxxx(int color,int data)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	r = (data >> 12) & 0x0f;
/*TODO*///	g = (data >>  8) & 0x0f;
/*TODO*///	b = (data >>  4) & 0x0f;
/*TODO*///
/*TODO*///	r = (r << 4) | r;
/*TODO*///	g = (g << 4) | g;
/*TODO*///	b = (b << 4) | b;
/*TODO*///
/*TODO*///	palette_change_color(color,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_RRRRGGGGBBBBxxxx_swap_w(int offset,int data)
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_RRRRGGGGBBBBxxxx(offset / 2,paletteram[offset | 1] | (paletteram[offset & ~1] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_RRRRGGGGBBBBxxxx_split1_w(int offset,int data)
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_RRRRGGGGBBBBxxxx(offset,paletteram[offset] | (paletteram_2[offset] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_RRRRGGGGBBBBxxxx_split2_w(int offset,int data)
/*TODO*///{
/*TODO*///	paletteram_2[offset] = data;
/*TODO*///	changecolor_RRRRGGGGBBBBxxxx(offset,paletteram[offset] | (paletteram_2[offset] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_RRRRGGGGBBBBxxxx_word_w(int offset,int data)
/*TODO*///{
/*TODO*///	int oldword = READ_WORD(&paletteram[offset]);
/*TODO*///	int newword = COMBINE_WORD(oldword,data);
/*TODO*///
/*TODO*///
/*TODO*///	WRITE_WORD(&paletteram[offset],newword);
/*TODO*///	changecolor_RRRRGGGGBBBBxxxx(offset / 2,newword);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_BBBBGGGGRRRRxxxx(int color,int data)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	r = (data >>  4) & 0x0f;
/*TODO*///	g = (data >>  8) & 0x0f;
/*TODO*///	b = (data >> 12) & 0x0f;
/*TODO*///
/*TODO*///	r = (r << 4) | r;
/*TODO*///	g = (g << 4) | g;
/*TODO*///	b = (b << 4) | b;
/*TODO*///
/*TODO*///	palette_change_color(color,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_BBBBGGGGRRRRxxxx_swap_w(int offset,int data)
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_BBBBGGGGRRRRxxxx(offset / 2,paletteram[offset | 1] | (paletteram[offset & ~1] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_BBBBGGGGRRRRxxxx_split1_w(int offset,int data)
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_BBBBGGGGRRRRxxxx(offset,paletteram[offset] | (paletteram_2[offset] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_BBBBGGGGRRRRxxxx_split2_w(int offset,int data)
/*TODO*///{
/*TODO*///	paletteram_2[offset] = data;
/*TODO*///	changecolor_BBBBGGGGRRRRxxxx(offset,paletteram[offset] | (paletteram_2[offset] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_BBBBGGGGRRRRxxxx_word_w(int offset,int data)
/*TODO*///{
/*TODO*///	int oldword = READ_WORD(&paletteram[offset]);
/*TODO*///	int newword = COMBINE_WORD(oldword,data);
/*TODO*///
/*TODO*///
/*TODO*///	WRITE_WORD(&paletteram[offset],newword);
/*TODO*///	changecolor_BBBBGGGGRRRRxxxx(offset / 2,newword);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_xBBBBBGGGGGRRRRR(int color,int data)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	r = (data >>  0) & 0x1f;
/*TODO*///	g = (data >>  5) & 0x1f;
/*TODO*///	b = (data >> 10) & 0x1f;
/*TODO*///
/*TODO*///	r = (r << 3) | (r >> 2);
/*TODO*///	g = (g << 3) | (g >> 2);
/*TODO*///	b = (b << 3) | (b >> 2);
/*TODO*///
/*TODO*///	palette_change_color(color,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_xBBBBBGGGGGRRRRR_w(int offset,int data)
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xBBBBBGGGGGRRRRR(offset / 2,paletteram[offset & ~1] | (paletteram[offset | 1] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_xBBBBBGGGGGRRRRR_swap_w(int offset,int data)
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xBBBBBGGGGGRRRRR(offset / 2,paletteram[offset | 1] | (paletteram[offset & ~1] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_xBBBBBGGGGGRRRRR_word_w(int offset,int data)
/*TODO*///{
/*TODO*///	int oldword = READ_WORD(&paletteram[offset]);
/*TODO*///	int newword = COMBINE_WORD(oldword,data);
/*TODO*///
/*TODO*///
/*TODO*///	WRITE_WORD(&paletteram[offset],newword);
/*TODO*///	changecolor_xBBBBBGGGGGRRRRR(offset / 2,newword);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_xRRRRRGGGGGBBBBB(int color,int data)
/*TODO*///{
/*TODO*///	int r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	r = (data >> 10) & 0x1f;
/*TODO*///	g = (data >>  5) & 0x1f;
/*TODO*///	b = (data >>  0) & 0x1f;
/*TODO*///
/*TODO*///	r = (r << 3) | (r >> 2);
/*TODO*///	g = (g << 3) | (g >> 2);
/*TODO*///	b = (b << 3) | (b >> 2);
/*TODO*///
/*TODO*///	palette_change_color(color,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_xRRRRRGGGGGBBBBB_w(int offset,int data)
/*TODO*///{
/*TODO*///	paletteram[offset] = data;
/*TODO*///	changecolor_xRRRRRGGGGGBBBBB(offset / 2,paletteram[offset & ~1] | (paletteram[offset | 1] << 8));
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_xRRRRRGGGGGBBBBB_word_w(int offset,int data)
/*TODO*///{
/*TODO*///	int oldword = READ_WORD(&paletteram[offset]);
/*TODO*///	int newword = COMBINE_WORD(oldword,data);
/*TODO*///
/*TODO*///
/*TODO*///	WRITE_WORD(&paletteram[offset],newword);
/*TODO*///	changecolor_xRRRRRGGGGGBBBBB(offset / 2,newword);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_IIIIRRRRGGGGBBBB(int color,int data)
/*TODO*///{
/*TODO*///	int i,r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	static const int ztable[16] =
/*TODO*///		{ 0x0, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf, 0x10, 0x11 };
/*TODO*///
/*TODO*///	i = ztable[(data >> 12) & 15];
/*TODO*///	r = ((data >> 8) & 15) * i;
/*TODO*///	g = ((data >> 4) & 15) * i;
/*TODO*///	b = ((data >> 0) & 15) * i;
/*TODO*///
/*TODO*///	palette_change_color(color,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_IIIIRRRRGGGGBBBB_word_w(int offset,int data)
/*TODO*///{
/*TODO*///	int oldword = READ_WORD(&paletteram[offset]);
/*TODO*///	int newword = COMBINE_WORD(oldword,data);
/*TODO*///
/*TODO*///
/*TODO*///	WRITE_WORD(&paletteram[offset],newword);
/*TODO*///	changecolor_IIIIRRRRGGGGBBBB(offset / 2,newword);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void changecolor_RRRRGGGGBBBBIIII(int color,int data)
/*TODO*///{
/*TODO*///	int i,r,g,b;
/*TODO*///
/*TODO*///
/*TODO*///	static const int ztable[16] =
/*TODO*///		{ 0x0, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf, 0x10, 0x11 };
/*TODO*///
/*TODO*///	i = ztable[(data >> 0) & 15];
/*TODO*///	r = ((data >> 12) & 15) * i;
/*TODO*///	g = ((data >>  8) & 15) * i;
/*TODO*///	b = ((data >>  4) & 15) * i;
/*TODO*///
/*TODO*///	palette_change_color(color,r,g,b);
/*TODO*///}
/*TODO*///
/*TODO*///void paletteram_RRRRGGGGBBBBIIII_word_w(int offset,int data)
/*TODO*///{
/*TODO*///	int oldword = READ_WORD(&paletteram[offset]);
/*TODO*///	int newword = COMBINE_WORD(oldword,data);
/*TODO*///
/*TODO*///
/*TODO*///	WRITE_WORD(&paletteram[offset],newword);
/*TODO*///	changecolor_RRRRGGGGBBBBIIII(offset / 2,newword);
/*TODO*///}
/*TODO*///    
}
