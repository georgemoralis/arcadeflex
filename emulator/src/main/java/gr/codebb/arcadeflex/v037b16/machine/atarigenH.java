package gr.codebb.arcadeflex.v037b16.machine;
/***************************************************************************

  atarigen.h

  General functions for mid-to-late 80's Atari raster games.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 


public class atarigenH
{

        public static abstract interface atarigen_void_callbackPtr {
            public abstract void handler();
        }
        
	public static final int ATARI_CLOCK_14MHz	= 14318180;
/*TODO*///	#define ATARI_CLOCK_20MHz	20000000
/*TODO*///	#define ATARI_CLOCK_32MHz	32000000
/*TODO*///	#define ATARI_CLOCK_50MHz	50000000
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*--------------------------------------------------------------------------
/*TODO*///	
/*TODO*///		Atari generic interrupt model (required)
/*TODO*///	
/*TODO*///			atarigen_scanline_int_state - state of the scanline interrupt line
/*TODO*///			atarigen_sound_int_state - state of the sound interrupt line
/*TODO*///			atarigen_video_int_state - state of the video interrupt line
/*TODO*///	
/*TODO*///			atarigen_int_callback - called when the interrupt state changes
/*TODO*///	
/*TODO*///			atarigen_interrupt_reset - resets & initializes the interrupt state
/*TODO*///			atarigen_update_interrupts - forces the interrupts to be reevaluted
/*TODO*///	
/*TODO*///			atarigen_scanline_int_set - scanline interrupt initialization
/*TODO*///			atarigen_sound_int_gen - scanline interrupt generator
/*TODO*///			atarigen_scanline_int_ack_w - scanline interrupt acknowledgement
/*TODO*///	
/*TODO*///			atarigen_sound_int_gen - sound interrupt generator
/*TODO*///			atarigen_sound_int_ack_w - sound interrupt acknowledgement
/*TODO*///	
/*TODO*///			atarigen_video_int_gen - video interrupt generator
/*TODO*///			atarigen_video_int_ack_w - video interrupt acknowledgement
/*TODO*///	
/*TODO*///	--------------------------------------------------------------------------*/
/*TODO*///	extern int atarigen_scanline_int_state;
/*TODO*///	extern int atarigen_sound_int_state;
/*TODO*///	extern int atarigen_video_int_state;
/*TODO*///	
/*TODO*///	typedef void (*atarigen_int_callback)(void);
/*TODO*///	
/*TODO*///	void atarigen_interrupt_reset(atarigen_int_callback update_int);
/*TODO*///	
/*TODO*///	void atarigen_scanline_int_set(int scanline);
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*--------------------------------------------------------------------------
/*TODO*///	
/*TODO*///		EEPROM I/O (optional)
/*TODO*///	
/*TODO*///			atarigen_eeprom_default - pointer to compressed default data
/*TODO*///			atarigen_eeprom - pointer to base of EEPROM memory
/*TODO*///			atarigen_eeprom_size - size of EEPROM memory
/*TODO*///	
/*TODO*///			atarigen_eeprom_reset - resets the EEPROM system
/*TODO*///	
/*TODO*///			atarigen_eeprom_enable_w - write handler to enable EEPROM access
/*TODO*///			atarigen_eeprom_w - write handler for EEPROM data
/*TODO*///			atarigen_eeprom_r - read handler for EEPROM data (low byte)
/*TODO*///			atarigen_eeprom_upper_r - read handler for EEPROM data (high byte)
/*TODO*///	
/*TODO*///			atarigen_nvram_handler - load/save EEPROM data
/*TODO*///	
/*TODO*///	--------------------------------------------------------------------------*/
/*TODO*///	extern const UINT16 *atarigen_eeprom_default;
/*TODO*///	extern UINT8 *atarigen_eeprom;
/*TODO*///	extern size_t atarigen_eeprom_size;
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*--------------------------------------------------------------------------
/*TODO*///	
/*TODO*///		Slapstic I/O (optional)
/*TODO*///	
/*TODO*///			atarigen_slapstic_init - select and initialize the slapstic handlers
/*TODO*///			atarigen_slapstic_reset - resets the slapstic state
/*TODO*///	
/*TODO*///			atarigen_slapstic_w - write handler for slapstic data
/*TODO*///			atarigen_slapstic_r - read handler for slapstic data
/*TODO*///	
/*TODO*///			slapstic_init - low-level init routine
/*TODO*///			slapstic_reset - low-level reset routine
/*TODO*///			slapstic_bank - low-level routine to return the current bank
/*TODO*///			slapstic_tweak - low-level tweak routine
/*TODO*///	
/*TODO*///	--------------------------------------------------------------------------*/
/*TODO*///	void atarigen_slapstic_init(int cpunum, int base, int chipnum);
/*TODO*///	
/*TODO*///	
/*TODO*///	void slapstic_init(int chip);
/*TODO*///	int slapstic_tweak(offs_t offset);
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***********************************************************************************************/
/*TODO*///	/***********************************************************************************************/
/*TODO*///	/***********************************************************************************************/
/*TODO*///	/***********************************************************************************************/
/*TODO*///	/***********************************************************************************************/
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*--------------------------------------------------------------------------
/*TODO*///	
/*TODO*///		Sound I/O
/*TODO*///	
/*TODO*///			atarigen_sound_io_reset - reset the sound I/O system
/*TODO*///	
/*TODO*///			atarigen_6502_irq_gen - standard 6502 IRQ interrupt generator
/*TODO*///			atarigen_6502_irq_ack_r - standard 6502 IRQ interrupt acknowledgement
/*TODO*///			atarigen_6502_irq_ack_w - standard 6502 IRQ interrupt acknowledgement
/*TODO*///	
/*TODO*///			atarigen_ym2151_irq_gen - YM2151 sound IRQ generator
/*TODO*///	
/*TODO*///			atarigen_sound_w - Main CPU . sound CPU data write (low byte)
/*TODO*///			atarigen_sound_r - Sound CPU . main CPU data read (low byte)
/*TODO*///			atarigen_sound_upper_w - Main CPU . sound CPU data write (high byte)
/*TODO*///			atarigen_sound_upper_r - Sound CPU . main CPU data read (high byte)
/*TODO*///	
/*TODO*///			atarigen_sound_reset_w - 6502 CPU reset
/*TODO*///			atarigen_6502_sound_w - Sound CPU . main CPU data write
/*TODO*///			atarigen_6502_sound_r - Main CPU . sound CPU data read
/*TODO*///	
/*TODO*///	--------------------------------------------------------------------------*/
/*TODO*///	extern int atarigen_cpu_to_sound_ready;
/*TODO*///	extern int atarigen_sound_to_cpu_ready;
/*TODO*///	
/*TODO*///	void atarigen_sound_io_reset(int cpu_num);
/*TODO*///	
/*TODO*///	
/*TODO*///	void atarigen_ym2151_irq_gen(int irq);
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*--------------------------------------------------------------------------
/*TODO*///	
/*TODO*///		Misc sound helpers
/*TODO*///	
/*TODO*///			atarigen_init_6502_speedup - installs 6502 speedup cheat handler
/*TODO*///			atarigen_set_ym2151_vol - set the volume of the 2151 chip
/*TODO*///			atarigen_set_ym2413_vol - set the volume of the 2413 chip
/*TODO*///			atarigen_set_pokey_vol - set the volume of the POKEY chip(s)
/*TODO*///			atarigen_set_tms5220_vol - set the volume of the 5220 chip
/*TODO*///			atarigen_set_oki6295_vol - set the volume of the OKI6295
/*TODO*///	
/*TODO*///	--------------------------------------------------------------------------*/
/*TODO*///	void atarigen_init_6502_speedup(int cpunum, int compare_pc1, int compare_pc2);
/*TODO*///	void atarigen_set_ym2151_vol(int volume);
/*TODO*///	void atarigen_set_ym2413_vol(int volume);
/*TODO*///	void atarigen_set_pokey_vol(int volume);
/*TODO*///	void atarigen_set_tms5220_vol(int volume);
/*TODO*///	void atarigen_set_oki6295_vol(int volume);
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***********************************************************************************************/
/*TODO*///	/***********************************************************************************************/
/*TODO*///	/***********************************************************************************************/
/*TODO*///	/***********************************************************************************************/
/*TODO*///	/***********************************************************************************************/
/*TODO*///	
/*TODO*///	
/*TODO*///	/* general video globals */
/*TODO*///	extern UINT8 *atarigen_playfieldram;
/*TODO*///	extern UINT8 *atarigen_playfield2ram;
/*TODO*///	extern UINT8 *atarigen_playfieldram_color;
/*TODO*///	extern UINT8 *atarigen_playfield2ram_color;
/*TODO*///	extern UINT8 *atarigen_spriteram;
/*TODO*///	extern UINT8 *atarigen_alpharam;
/*TODO*///	extern UINT8 *atarigen_vscroll;
/*TODO*///	extern UINT8 *atarigen_hscroll;
/*TODO*///	
/*TODO*///	extern size_t atarigen_playfieldram_size;
/*TODO*///	extern size_t atarigen_playfield2ram_size;
/*TODO*///	extern size_t atarigen_spriteram_size;
/*TODO*///	extern size_t atarigen_alpharam_size;
/*TODO*///	
/*TODO*///	
/*TODO*///	/*--------------------------------------------------------------------------
/*TODO*///	
/*TODO*///		Video scanline timing
/*TODO*///	
/*TODO*///			atarigen_scanline_callback - called every n scanlines
/*TODO*///	
/*TODO*///			atarigen_scanline_timer_reset - call to reset the system
/*TODO*///	
/*TODO*///	--------------------------------------------------------------------------*/
/*TODO*///	typedef void (*atarigen_scanline_callback)(int scanline);
/*TODO*///	
/*TODO*///	void atarigen_scanline_timer_reset(atarigen_scanline_callback update_graphics, int frequency);
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*--------------------------------------------------------------------------
/*TODO*///	
/*TODO*///		Video Controller I/O: used in Shuuz, Thunderjaws, Relief Pitcher, Off the Wall
/*TODO*///	
/*TODO*///			atarigen_video_control_data - pointer to base of control memory
/*TODO*///			atarigen_video_control_state - current state of the video controller
/*TODO*///	
/*TODO*///			atarigen_video_control_reset - initializes the video controller
/*TODO*///	
/*TODO*///			atarigen_video_control_w - write handler for the video controller
/*TODO*///			atarigen_video_control_r - read handler for the video controller
/*TODO*///	
/*TODO*///	--------------------------------------------------------------------------*/
/*TODO*///	struct atarigen_video_control_state_desc
/*TODO*///	{
/*TODO*///		int latch1;								/* latch #1 value (-1 means disabled) */
/*TODO*///		int latch2;								/* latch #2 value (-1 means disabled) */
/*TODO*///		int rowscroll_enable;					/* true if row-scrolling is enabled */
/*TODO*///		int palette_bank;						/* which palette bank is enabled */
/*TODO*///		int pf1_xscroll;						/* playfield 1 xscroll */
/*TODO*///		int pf1_yscroll;						/* playfield 1 yscroll */
/*TODO*///		int pf2_xscroll;						/* playfield 2 xscroll */
/*TODO*///		int pf2_yscroll;						/* playfield 2 yscroll */
/*TODO*///		int sprite_xscroll;						/* sprite xscroll */
/*TODO*///		int sprite_yscroll;						/* sprite xscroll */
/*TODO*///	};
/*TODO*///	
/*TODO*///	extern UINT8 *atarigen_video_control_data;
/*TODO*///	extern struct atarigen_video_control_state_desc atarigen_video_control_state;
/*TODO*///	
/*TODO*///	void atarigen_video_control_update(const UINT8 *data);
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*--------------------------------------------------------------------------
/*TODO*///	
/*TODO*///		Motion object rendering
/*TODO*///	
/*TODO*///			atarigen_mo_desc - description of the M.O. layout
/*TODO*///	
/*TODO*///			atarigen_mo_callback - called back for each M.O. during processing
/*TODO*///	
/*TODO*///			atarigen_mo_init - initializes and configures the M.O. list walker
/*TODO*///			atarigen_mo_free - frees all memory allocated by atarigen_mo_init
/*TODO*///			atarigen_mo_reset - reset for a new frame (use only if not using interrupt system)
/*TODO*///			atarigen_mo_update - updates the M.O. list for the given scanline
/*TODO*///			atarigen_mo_process - processes the current list
/*TODO*///	
/*TODO*///	--------------------------------------------------------------------------*/
/*TODO*///	#define ATARIGEN_MAX_MAXCOUNT				1024	/* no more than 1024 MO's ever */
/*TODO*///	
/*TODO*///	struct atarigen_mo_desc
/*TODO*///	{
/*TODO*///		int maxcount;                           /* maximum number of MO's */
/*TODO*///		int entryskip;                          /* number of bytes per MO entry */
/*TODO*///		int wordskip;                           /* number of bytes between MO words */
/*TODO*///		int ignoreword;                         /* ignore an entry if this word == 0xffff */
/*TODO*///		int linkword, linkshift, linkmask;		/* link = (data[linkword >> linkshift) & linkmask */
/*TODO*///		int reverse;                            /* render in reverse link order */
/*TODO*///		int entrywords;							/* number of words/entry (0 defaults to 4) */
/*TODO*///	};
/*TODO*///	
/*TODO*///	typedef void (*atarigen_mo_callback)(const UINT16 *data, const struct rectangle *clip, void *param);
/*TODO*///	
/*TODO*///	int atarigen_mo_init(const struct atarigen_mo_desc *source_desc);
/*TODO*///	void atarigen_mo_update(const UINT8 *base, int start, int scanline);
/*TODO*///	void atarigen_mo_update_slip_512(const UINT8 *base, int scroll, int scanline, const UINT8 *slips);
/*TODO*///	void atarigen_mo_process(atarigen_mo_callback callback, void *param);
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*--------------------------------------------------------------------------
/*TODO*///	
/*TODO*///		RLE Motion object rendering/decoding
/*TODO*///	
/*TODO*///			atarigen_rle_descriptor - describes a single object
/*TODO*///	
/*TODO*///			atarigen_rle_count - total number of objects found
/*TODO*///			atarigen_rle_info - array of descriptors for objects we found
/*TODO*///	
/*TODO*///			atarigen_rle_init - prescans the RLE objects
/*TODO*///			atarigen_rle_free - frees all memory allocated by atarigen_rle_init
/*TODO*///			atarigen_rle_render - render an RLE-compressed motion object
/*TODO*///	
/*TODO*///	--------------------------------------------------------------------------*/
/*TODO*///	struct atarigen_rle_descriptor
/*TODO*///	{
/*TODO*///		int width;
/*TODO*///		int height;
/*TODO*///		INT16 xoffs;
/*TODO*///		INT16 yoffs;
/*TODO*///		int bpp;
/*TODO*///		UINT32 pen_usage;
/*TODO*///		UINT32 pen_usage_hi;
/*TODO*///		const UINT16 *table;
/*TODO*///		const UINT16 *data;
/*TODO*///	};
/*TODO*///	
/*TODO*///	extern int atarigen_rle_count;
/*TODO*///	extern struct atarigen_rle_descriptor *atarigen_rle_info;
/*TODO*///	
/*TODO*///	int atarigen_rle_init(int region, int colorbase);
/*TODO*///	void atarigen_rle_render(struct osd_bitmap *bitmap, struct atarigen_rle_descriptor *info, int color, int hflip, int vflip,
/*TODO*///		int x, int y, int xscale, int yscale, const struct rectangle *clip);
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*--------------------------------------------------------------------------
/*TODO*///	
/*TODO*///		Playfield rendering
/*TODO*///	
/*TODO*///			atarigen_pf_state - data block describing the playfield
/*TODO*///	
/*TODO*///			atarigen_pf_callback - called back for each chunk during processing
/*TODO*///	
/*TODO*///			atarigen_pf_init - initializes and configures the playfield params
/*TODO*///			atarigen_pf_free - frees all memory allocated by atarigen_pf_init
/*TODO*///			atarigen_pf_reset - reset for a new frame (use only if not using interrupt system)
/*TODO*///			atarigen_pf_update - updates the playfield params for the given scanline
/*TODO*///			atarigen_pf_process - processes the current list of parameters
/*TODO*///	
/*TODO*///			atarigen_pf2_init - same as above but for a second playfield
/*TODO*///			atarigen_pf2_free - same as above but for a second playfield
/*TODO*///			atarigen_pf2_reset - same as above but for a second playfield
/*TODO*///			atarigen_pf2_update - same as above but for a second playfield
/*TODO*///			atarigen_pf2_process - same as above but for a second playfield
/*TODO*///	
/*TODO*///	--------------------------------------------------------------------------*/
/*TODO*///	extern struct osd_bitmap *atarigen_pf_bitmap;
/*TODO*///	extern UINT8 *atarigen_pf_dirty;
/*TODO*///	extern UINT8 *atarigen_pf_visit;
/*TODO*///	
/*TODO*///	extern struct osd_bitmap *atarigen_pf2_bitmap;
/*TODO*///	extern UINT8 *atarigen_pf2_dirty;
/*TODO*///	extern UINT8 *atarigen_pf2_visit;
/*TODO*///	
/*TODO*///	extern struct osd_bitmap *atarigen_pf_overrender_bitmap;
/*TODO*///	extern UINT16 atarigen_overrender_colortable[32];
/*TODO*///	
/*TODO*///	struct atarigen_pf_desc
/*TODO*///	{
/*TODO*///		int tilewidth, tileheight;              /* width/height of each tile */
/*TODO*///		int xtiles, ytiles;						/* number of tiles in each direction */
/*TODO*///		int noscroll;							/* non-scrolling? */
/*TODO*///	};
/*TODO*///	
/*TODO*///	struct atarigen_pf_state
/*TODO*///	{
/*TODO*///		int hscroll;							/* current horizontal starting offset */
/*TODO*///		int vscroll;							/* current vertical starting offset */
/*TODO*///		int param[2];							/* up to 2 other parameters that will cause a boundary break */
/*TODO*///	};
/*TODO*///	
/*TODO*///	typedef void (*atarigen_pf_callback)(const struct rectangle *tiles, const struct rectangle *clip, const struct atarigen_pf_state *state, void *param);
/*TODO*///	
/*TODO*///	int atarigen_pf_init(const struct atarigen_pf_desc *source_desc);
/*TODO*///	void atarigen_pf_update(const struct atarigen_pf_state *state, int scanline);
/*TODO*///	void atarigen_pf_process(atarigen_pf_callback callback, void *param, const struct rectangle *clip);
/*TODO*///	
/*TODO*///	int atarigen_pf2_init(const struct atarigen_pf_desc *source_desc);
/*TODO*///	void atarigen_pf2_update(const struct atarigen_pf_state *state, int scanline);
/*TODO*///	void atarigen_pf2_process(atarigen_pf_callback callback, void *param, const struct rectangle *clip);
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*--------------------------------------------------------------------------
/*TODO*///	
/*TODO*///		Misc Video stuff
/*TODO*///	
/*TODO*///			atarigen_get_hblank - returns the current HBLANK state
/*TODO*///			atarigen_halt_until_hblank_0_w - write handler for a HBLANK halt
/*TODO*///			atarigen_666_paletteram_w - 6-6-6 special RGB paletteram handler
/*TODO*///			atarigen_expanded_666_paletteram_w - byte version of above
/*TODO*///	
/*TODO*///	--------------------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*--------------------------------------------------------------------------
/*TODO*///	
/*TODO*///		General stuff
/*TODO*///	
/*TODO*///			atarigen_show_slapstic_message - display warning about slapstic
/*TODO*///			atarigen_show_sound_message - display warning about coins
/*TODO*///			atarigen_update_messages - update messages
/*TODO*///	
/*TODO*///	--------------------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*--------------------------------------------------------------------------
/*TODO*///	
/*TODO*///		Motion object drawing macros
/*TODO*///	
/*TODO*///			atarigen_mo_compute_clip - computes the M.O. clip rect
/*TODO*///			atarigen_mo_compute_clip_8x8 - computes the M.O. clip rect
/*TODO*///			atarigen_mo_compute_clip_16x16 - computes the M.O. clip rect
/*TODO*///	
/*TODO*///			atarigen_mo_draw - draws a generically-sized M.O.
/*TODO*///			atarigen_mo_draw_strip - draws a generically-sized M.O. strip
/*TODO*///			atarigen_mo_draw_8x8 - draws an 8x8 M.O.
/*TODO*///			atarigen_mo_draw_8x8_strip - draws an 8x8 M.O. strip (hsize == 1)
/*TODO*///			atarigen_mo_draw_16x16 - draws a 16x16 M.O.
/*TODO*///			atarigen_mo_draw_16x16_strip - draws a 16x16 M.O. strip (hsize == 1)
/*TODO*///	
/*TODO*///	--------------------------------------------------------------------------*/
/*TODO*///	#define atarigen_mo_compute_clip(dest, xpos, ypos, hsize, vsize, clip, tile_width, tile_height) \
/*TODO*///	{																								\
/*TODO*///		/* determine the bounding box */															\
/*TODO*///		dest.min_x = xpos;																			\
/*TODO*///		dest.min_y = ypos;																			\
/*TODO*///		dest.max_x = xpos + hsize * tile_width - 1;													\
/*TODO*///		dest.max_y = ypos + vsize * tile_height - 1;												\
/*TODO*///																									\
/*TODO*///		/* clip to the display */																	\
/*TODO*///		if (dest.min_x < clip.min_x)																\
/*TODO*///			dest.min_x = clip.min_x;																\
/*TODO*///		else if (dest.min_x > clip.max_x)															\
/*TODO*///			dest.min_x = clip.max_x;																\
/*TODO*///		if (dest.max_x < clip.min_x)																\
/*TODO*///			dest.max_x = clip.min_x;																\
/*TODO*///		else if (dest.max_x > clip.max_x)															\
/*TODO*///			dest.max_x = clip.max_x;																\
/*TODO*///		if (dest.min_y < clip.min_y)																\
/*TODO*///			dest.min_y = clip.min_y;																\
/*TODO*///		else if (dest.min_y > clip.max_y)															\
/*TODO*///			dest.min_y = clip.max_y;																\
/*TODO*///		if (dest.max_y < clip.min_y)																\
/*TODO*///			dest.max_y = clip.min_y;																\
/*TODO*///		else if (dest.max_y > clip.max_y)															\
/*TODO*///			dest.max_y = clip.max_y;																\
/*TODO*///	}
/*TODO*///	
/*TODO*///	#define atarigen_mo_compute_clip_8x8(dest, xpos, ypos, hsize, vsize, clip) \
/*TODO*///		atarigen_mo_compute_clip(dest, xpos, ypos, hsize, vsize, clip, 8, 8)
/*TODO*///	
/*TODO*///	#define atarigen_mo_compute_clip_16x8(dest, xpos, ypos, hsize, vsize, clip) \
/*TODO*///		atarigen_mo_compute_clip(dest, xpos, ypos, hsize, vsize, clip, 16, 8)
/*TODO*///	
/*TODO*///	#define atarigen_mo_compute_clip_16x16(dest, xpos, ypos, hsize, vsize, clip) \
/*TODO*///		atarigen_mo_compute_clip(dest, xpos, ypos, hsize, vsize, clip, 16, 16)
/*TODO*///	
/*TODO*///	
/*TODO*///	#define atarigen_mo_draw(bitmap, gfx, code, color, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen, tile_width, tile_height) \
/*TODO*///	{																										\
/*TODO*///		int tilex, tiley, screenx, screendx, screendy;														\
/*TODO*///		int startx = x;																						\
/*TODO*///		int screeny = y;																					\
/*TODO*///		int tile = code;																					\
/*TODO*///																											\
/*TODO*///		/* adjust for h flip */																				\
/*TODO*///		if (hflip != 0)																							\
/*TODO*///			startx += (hsize - 1) * tile_width, screendx = -tile_width;										\
/*TODO*///		else																								\
/*TODO*///			screendx = tile_width;																			\
/*TODO*///																											\
/*TODO*///		/* adjust for v flip */																				\
/*TODO*///		if (vflip != 0)																							\
/*TODO*///			screeny += (vsize - 1) * tile_height, screendy = -tile_height;									\
/*TODO*///		else																								\
/*TODO*///			screendy = tile_height;																			\
/*TODO*///																											\
/*TODO*///		/* loop over the height */																			\
/*TODO*///		for (tiley = 0; tiley < vsize; tiley++, screeny += screendy)										\
/*TODO*///		{																									\
/*TODO*///			/* clip the Y coordinate */																		\
/*TODO*///			if (screeny <= clip.min_y - tile_height)														\
/*TODO*///			{																								\
/*TODO*///				tile += hsize;																				\
/*TODO*///				continue;																					\
/*TODO*///			}																								\
/*TODO*///			else if (screeny > clip.max_y)																	\
/*TODO*///				break;																						\
/*TODO*///																											\
/*TODO*///			/* loop over the width */																		\
/*TODO*///			for (tilex = 0, screenx = startx; tilex < hsize; tilex++, screenx += screendx, tile++)			\
/*TODO*///			{																								\
/*TODO*///				/* clip the X coordinate */																	\
/*TODO*///				if (screenx <= clip.min_x - tile_width || screenx > clip.max_x)							\
/*TODO*///					continue;																				\
/*TODO*///																											\
/*TODO*///				/* draw the sprite */																		\
/*TODO*///				drawgfx(bitmap, gfx, tile, color, hflip, vflip, screenx, screeny, clip, trans, trans_pen);	\
/*TODO*///			}																								\
/*TODO*///		}																									\
/*TODO*///	}
/*TODO*///	
/*TODO*///	#define atarigen_mo_draw_transparent(bitmap, gfx, code, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen, tile_width, tile_height) \
/*TODO*///	{																										\
/*TODO*///		UINT16 *temp = gfx.colortable;																\
/*TODO*///		gfx.colortable = atarigen_overrender_colortable;													\
/*TODO*///		atarigen_mo_draw(bitmap, gfx, code, 0, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen, tile_width, tile_height);\
/*TODO*///		gfx.colortable = temp;																				\
/*TODO*///	}
/*TODO*///	
/*TODO*///	#define atarigen_mo_draw_strip(bitmap, gfx, code, color, hflip, vflip, x, y, vsize, clip, trans, trans_pen, tile_width, tile_height) \
/*TODO*///	{																										\
/*TODO*///		int tiley, screendy;																				\
/*TODO*///		int screenx = x;																					\
/*TODO*///		int screeny = y;																					\
/*TODO*///		int tile = code;																					\
/*TODO*///																											\
/*TODO*///		/* clip the X coordinate */																			\
/*TODO*///		if (screenx > clip.min_x - tile_width && screenx <= clip.max_x)									\
/*TODO*///		{																									\
/*TODO*///			/* adjust for v flip */																			\
/*TODO*///			if (vflip != 0)																						\
/*TODO*///				screeny += (vsize - 1) * tile_height, screendy = -tile_height;								\
/*TODO*///			else																							\
/*TODO*///				screendy = tile_height;																		\
/*TODO*///																											\
/*TODO*///			/* loop over the height */																		\
/*TODO*///			for (tiley = 0; tiley < vsize; tiley++, screeny += screendy, tile++)							\
/*TODO*///			{																								\
/*TODO*///				/* clip the Y coordinate */																	\
/*TODO*///				if (screeny <= clip.min_y - tile_height)													\
/*TODO*///					continue;																				\
/*TODO*///				else if (screeny > clip.max_y)																\
/*TODO*///					break;																					\
/*TODO*///																											\
/*TODO*///				/* draw the sprite */																		\
/*TODO*///				drawgfx(bitmap, gfx, tile, color, hflip, vflip, screenx, screeny, clip, trans, trans_pen);	\
/*TODO*///			}																								\
/*TODO*///		}																									\
/*TODO*///	}
/*TODO*///	
/*TODO*///	#define atarigen_mo_draw_transparent_strip(bitmap, gfx, code, hflip, vflip, x, y, vsize, clip, trans, trans_pen, tile_width, tile_height) \
/*TODO*///	{																										\
/*TODO*///		UINT16 *temp = gfx.colortable;																\
/*TODO*///		gfx.colortable = atarigen_overrender_colortable;													\
/*TODO*///		atarigen_mo_draw_strip(bitmap, gfx, code, 0, hflip, vflip, x, y, vsize, clip, trans, trans_pen, tile_width, tile_height);\
/*TODO*///		gfx.colortable = temp;																				\
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	#define atarigen_mo_draw_8x8(bitmap, gfx, code, color, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen) \
/*TODO*///		atarigen_mo_draw(bitmap, gfx, code, color, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen, 8, 8)
/*TODO*///	
/*TODO*///	#define atarigen_mo_draw_16x8(bitmap, gfx, code, color, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen) \
/*TODO*///		atarigen_mo_draw(bitmap, gfx, code, color, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen, 16, 8)
/*TODO*///	
/*TODO*///	#define atarigen_mo_draw_16x16(bitmap, gfx, code, color, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen) \
/*TODO*///		atarigen_mo_draw(bitmap, gfx, code, color, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen, 16, 16)
/*TODO*///	
/*TODO*///	
/*TODO*///	#define atarigen_mo_draw_transparent_8x8(bitmap, gfx, code, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen) \
/*TODO*///		atarigen_mo_draw_transparent(bitmap, gfx, code, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen, 8, 8)
/*TODO*///	
/*TODO*///	#define atarigen_mo_draw_transparent_16x8(bitmap, gfx, code, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen) \
/*TODO*///		atarigen_mo_draw_transparent(bitmap, gfx, code, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen, 16, 8)
/*TODO*///	
/*TODO*///	#define atarigen_mo_draw_transparent_16x16(bitmap, gfx, code, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen) \
/*TODO*///		atarigen_mo_draw_transparent(bitmap, gfx, code, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen, 16, 16)
/*TODO*///	
/*TODO*///	
/*TODO*///	#define atarigen_mo_draw_8x8_strip(bitmap, gfx, code, color, hflip, vflip, x, y, vsize, clip, trans, trans_pen) \
/*TODO*///		atarigen_mo_draw_strip(bitmap, gfx, code, color, hflip, vflip, x, y, vsize, clip, trans, trans_pen, 8, 8)
/*TODO*///	
/*TODO*///	#define atarigen_mo_draw_16x8_strip(bitmap, gfx, code, color, hflip, vflip, x, y, vsize, clip, trans, trans_pen) \
/*TODO*///		atarigen_mo_draw_strip(bitmap, gfx, code, color, hflip, vflip, x, y, vsize, clip, trans, trans_pen, 16, 8)
/*TODO*///	
/*TODO*///	#define atarigen_mo_draw_16x16_strip(bitmap, gfx, code, color, hflip, vflip, x, y, vsize, clip, trans, trans_pen) \
/*TODO*///		atarigen_mo_draw_strip(bitmap, gfx, code, color, hflip, vflip, x, y, vsize, clip, trans, trans_pen, 16, 16)
/*TODO*///	
/*TODO*///	
/*TODO*///	#define atarigen_mo_draw_transparent_8x8_strip(bitmap, gfx, code, hflip, vflip, x, y, vsize, clip, trans, trans_pen) \
/*TODO*///		atarigen_mo_draw_transparent_strip(bitmap, gfx, code, hflip, vflip, x, y, vsize, clip, trans, trans_pen, 8, 8)
/*TODO*///	
/*TODO*///	#define atarigen_mo_draw_transparent_16x8_strip(bitmap, gfx, code, hflip, vflip, x, y, vsize, clip, trans, trans_pen) \
/*TODO*///		atarigen_mo_draw_transparent_strip(bitmap, gfx, code, hflip, vflip, x, y, vsize, clip, trans, trans_pen, 16, 8)
/*TODO*///	
/*TODO*///	#define atarigen_mo_draw_transparent_16x16_strip(bitmap, gfx, code, hflip, vflip, x, y, vsize, clip, trans, trans_pen) \
/*TODO*///		atarigen_mo_draw_transparent_strip(bitmap, gfx, code, hflip, vflip, x, y, vsize, clip, trans, trans_pen, 16, 16)
/*TODO*///	
/*TODO*///	#endif
}
