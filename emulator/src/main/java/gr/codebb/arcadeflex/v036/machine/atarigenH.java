package gr.codebb.arcadeflex.v036.machine;

import gr.codebb.arcadeflex.common.PtrLib.ShortPtr;
import gr.codebb.arcadeflex.common.PtrLib.UShortPtr;
import gr.codebb.arcadeflex.common.SubArrays.UShortArray;
import static gr.codebb.arcadeflex.v036.machine.atarigen.atarigen_overrender_colortable;
import gr.codebb.arcadeflex.v036.mame.osdependH.osd_bitmap;
import gr.codebb.arcadeflex.v037b7.mame.drawgfxH.rectangle;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import gr.codebb.arcadeflex.v037b7.mame.drawgfxH.GfxElement;

public class atarigenH {

    public static final int ATARI_CLOCK_14MHz = 14318180;
    public static final int ATARI_CLOCK_20MHz = 20000000;
    public static final int ATARI_CLOCK_32MHz = 32000000;
    public static final int ATARI_CLOCK_50MHz = 50000000;
    /*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*--------------------------------------------------------------------------
/*TODO*///
/*TODO*///	Atari generic interrupt model (required)
/*TODO*///
/*TODO*///		atarigen_scanline_int_state - state of the scanline interrupt line
/*TODO*///		atarigen_sound_int_state - state of the sound interrupt line
/*TODO*///		atarigen_video_int_state - state of the video interrupt line
/*TODO*///
/*TODO*///		atarigen_int_callback - called when the interrupt state changes
/*TODO*///
/*TODO*///		atarigen_interrupt_reset - resets & initializes the interrupt state
/*TODO*///		atarigen_update_interrupts - forces the interrupts to be reevaluted
/*TODO*///
/*TODO*///		atarigen_scanline_int_set - scanline interrupt initialization
/*TODO*///		atarigen_sound_int_gen - scanline interrupt generator
/*TODO*///		atarigen_scanline_int_ack_w - scanline interrupt acknowledgement
/*TODO*///
/*TODO*///		atarigen_sound_int_gen - sound interrupt generator
/*TODO*///		atarigen_sound_int_ack_w - sound interrupt acknowledgement
/*TODO*///
/*TODO*///		atarigen_video_int_gen - video interrupt generator
/*TODO*///		atarigen_video_int_ack_w - video interrupt acknowledgement
/*TODO*///
/*TODO*///--------------------------------------------------------------------------*/
/*TODO*///extern int atarigen_scanline_int_state;
/*TODO*///extern int atarigen_sound_int_state;
/*TODO*///extern int atarigen_video_int_state;

    public static abstract interface atarigen_void_callbackPtr {

        public abstract void handler();
    }

    /*TODO*///
/*TODO*///void atarigen_interrupt_reset(atarigen_int_callback update_int);
/*TODO*///void atarigen_update_interrupts(void);
/*TODO*///
/*TODO*///void atarigen_scanline_int_set(int scanline);
/*TODO*///int atarigen_scanline_int_gen(void);
/*TODO*///void atarigen_scanline_int_ack_w(int offset, int data);
/*TODO*///
/*TODO*///int atarigen_sound_int_gen(void);
/*TODO*///void atarigen_sound_int_ack_w(int offset, int data);
/*TODO*///
/*TODO*///int atarigen_video_int_gen(void);
/*TODO*///void atarigen_video_int_ack_w(int offset, int data);
/*TODO*///
/*TODO*///
/*TODO*////*--------------------------------------------------------------------------
/*TODO*///
/*TODO*///	EEPROM I/O (optional)
/*TODO*///
/*TODO*///		atarigen_eeprom_default - pointer to compressed default data
/*TODO*///		atarigen_eeprom - pointer to base of EEPROM memory
/*TODO*///		atarigen_eeprom_size - size of EEPROM memory
/*TODO*///
/*TODO*///		atarigen_eeprom_reset - resets the EEPROM system
/*TODO*///
/*TODO*///		atarigen_eeprom_enable_w - write handler to enable EEPROM access
/*TODO*///		atarigen_eeprom_w - write handler for EEPROM data
/*TODO*///		atarigen_eeprom_r - read handler for EEPROM data (low byte)
/*TODO*///		atarigen_eeprom_upper_r - read handler for EEPROM data (high byte)
/*TODO*///
/*TODO*///		atarigen_nvram_handler - load/save EEPROM data
/*TODO*///
/*TODO*///--------------------------------------------------------------------------*/
/*TODO*///extern const UINT16 *atarigen_eeprom_default;
/*TODO*///extern UINT8 *atarigen_eeprom;
/*TODO*///extern int atarigen_eeprom_size;
/*TODO*///
/*TODO*///void atarigen_eeprom_reset(void);
/*TODO*///
/*TODO*///void atarigen_eeprom_enable_w(int offset, int data);
/*TODO*///void atarigen_eeprom_w(int offset, int data);
/*TODO*///int atarigen_eeprom_r(int offset);
/*TODO*///int atarigen_eeprom_upper_r(int offset);
/*TODO*///
/*TODO*///void atarigen_nvram_handler(void *file,int read_or_write);
/*TODO*///void atarigen_hisave(void);
/*TODO*///
/*TODO*///
/*TODO*////*--------------------------------------------------------------------------
/*TODO*///
/*TODO*///	Slapstic I/O (optional)
/*TODO*///
/*TODO*///		atarigen_slapstic_init - select and initialize the slapstic handlers
/*TODO*///		atarigen_slapstic_reset - resets the slapstic state
/*TODO*///
/*TODO*///		atarigen_slapstic_w - write handler for slapstic data
/*TODO*///		atarigen_slapstic_r - read handler for slapstic data
/*TODO*///
/*TODO*///		slapstic_init - low-level init routine
/*TODO*///		slapstic_reset - low-level reset routine
/*TODO*///		slapstic_bank - low-level routine to return the current bank
/*TODO*///		slapstic_tweak - low-level tweak routine
/*TODO*///
/*TODO*///--------------------------------------------------------------------------*/
/*TODO*///void atarigen_slapstic_init(int cpunum, int base, int chipnum);
/*TODO*///void atarigen_slapstic_reset(void);
/*TODO*///
/*TODO*///void atarigen_slapstic_w(int offset, int data);
/*TODO*///int atarigen_slapstic_r(int offset);
/*TODO*///
/*TODO*///void slapstic_init(int chip);
/*TODO*///void slapstic_reset(void);
/*TODO*///int slapstic_bank(void);
/*TODO*///int slapstic_tweak(int offset);
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***********************************************************************************************/
/*TODO*////***********************************************************************************************/
/*TODO*////***********************************************************************************************/
/*TODO*////***********************************************************************************************/
/*TODO*////***********************************************************************************************/
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*--------------------------------------------------------------------------
/*TODO*///
/*TODO*///	Sound I/O
/*TODO*///
/*TODO*///		atarigen_sound_io_reset - reset the sound I/O system
/*TODO*///
/*TODO*///		atarigen_6502_irq_gen - standard 6502 IRQ interrupt generator
/*TODO*///		atarigen_6502_irq_ack_r - standard 6502 IRQ interrupt acknowledgement
/*TODO*///		atarigen_6502_irq_ack_w - standard 6502 IRQ interrupt acknowledgement
/*TODO*///
/*TODO*///		atarigen_ym2151_irq_gen - YM2151 sound IRQ generator
/*TODO*///
/*TODO*///		atarigen_sound_w - Main CPU -> sound CPU data write (low byte)
/*TODO*///		atarigen_sound_r - Sound CPU -> main CPU data read (low byte)
/*TODO*///		atarigen_sound_upper_w - Main CPU -> sound CPU data write (high byte)
/*TODO*///		atarigen_sound_upper_r - Sound CPU -> main CPU data read (high byte)
/*TODO*///
/*TODO*///		atarigen_sound_reset_w - 6502 CPU reset
/*TODO*///		atarigen_6502_sound_w - Sound CPU -> main CPU data write
/*TODO*///		atarigen_6502_sound_r - Main CPU -> sound CPU data read
/*TODO*///
/*TODO*///--------------------------------------------------------------------------*/
/*TODO*///extern int atarigen_cpu_to_sound_ready;
/*TODO*///extern int atarigen_sound_to_cpu_ready;
/*TODO*///
/*TODO*///void atarigen_sound_io_reset(int cpu_num);
/*TODO*///
/*TODO*///int atarigen_6502_irq_gen(void);
/*TODO*///int atarigen_6502_irq_ack_r(int offset);
/*TODO*///void atarigen_6502_irq_ack_w(int offset, int data);
/*TODO*///
/*TODO*///void atarigen_ym2151_irq_gen(int irq);
/*TODO*///
/*TODO*///void atarigen_sound_w(int offset, int data);
/*TODO*///int atarigen_sound_r(int offset);
/*TODO*///void atarigen_sound_upper_w(int offset, int data);
/*TODO*///int atarigen_sound_upper_r(int offset);
/*TODO*///
/*TODO*///void atarigen_sound_reset(void);
/*TODO*///void atarigen_sound_reset_w(int offset, int data);
/*TODO*///void atarigen_6502_sound_w(int offset, int data);
/*TODO*///int atarigen_6502_sound_r(int offset);
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*--------------------------------------------------------------------------
/*TODO*///
/*TODO*///	Misc sound helpers
/*TODO*///
/*TODO*///		atarigen_init_6502_speedup - installs 6502 speedup cheat handler
/*TODO*///		atarigen_set_ym2151_vol - set the volume of the 2151 chip
/*TODO*///		atarigen_set_ym2413_vol - set the volume of the 2413 chip
/*TODO*///		atarigen_set_pokey_vol - set the volume of the POKEY chip(s)
/*TODO*///		atarigen_set_tms5220_vol - set the volume of the 5220 chip
/*TODO*///		atarigen_set_oki6295_vol - set the volume of the OKI6295
/*TODO*///
/*TODO*///--------------------------------------------------------------------------*/
/*TODO*///void atarigen_init_6502_speedup(int cpunum, int compare_pc1, int compare_pc2);
/*TODO*///void atarigen_set_ym2151_vol(int volume);
/*TODO*///void atarigen_set_ym2413_vol(int volume);
/*TODO*///void atarigen_set_pokey_vol(int volume);
/*TODO*///void atarigen_set_tms5220_vol(int volume);
/*TODO*///void atarigen_set_oki6295_vol(int volume);
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***********************************************************************************************/
/*TODO*////***********************************************************************************************/
/*TODO*////***********************************************************************************************/
/*TODO*////***********************************************************************************************/
/*TODO*////***********************************************************************************************/
/*TODO*///
/*TODO*///
/*TODO*////* general video globals */
/*TODO*///extern UINT8 *atarigen_playfieldram;
/*TODO*///extern UINT8 *atarigen_playfield2ram;
/*TODO*///extern UINT8 *atarigen_playfieldram_color;
/*TODO*///extern UINT8 *atarigen_playfield2ram_color;
/*TODO*///extern UINT8 *atarigen_spriteram;
/*TODO*///extern UINT8 *atarigen_alpharam;
/*TODO*///extern UINT8 *atarigen_vscroll;
/*TODO*///extern UINT8 *atarigen_hscroll;
/*TODO*///
/*TODO*///extern int atarigen_playfieldram_size;
/*TODO*///extern int atarigen_playfield2ram_size;
/*TODO*///extern int atarigen_spriteram_size;
/*TODO*///extern int atarigen_alpharam_size;
/*TODO*///
/*TODO*///
/*TODO*////*--------------------------------------------------------------------------
/*TODO*///
/*TODO*///	Video scanline timing
/*TODO*///
/*TODO*///		atarigen_scanline_callback - called every n scanlines
/*TODO*///
/*TODO*///		atarigen_scanline_timer_reset - call to reset the system
/*TODO*///
/*TODO*///--------------------------------------------------------------------------*/
    public static abstract interface atarigen_scanline_callbackPtr {

        public abstract void handler(int scanline);
    }
    /*TODO*///
/*TODO*///void atarigen_scanline_timer_reset(atarigen_scanline_callback update_graphics, int frequency);
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*--------------------------------------------------------------------------
/*TODO*///
/*TODO*///	Video Controller I/O: used in Shuuz, Thunderjaws, Relief Pitcher, Off the Wall
/*TODO*///
/*TODO*///		atarigen_video_control_data - pointer to base of control memory
/*TODO*///		atarigen_video_control_state - current state of the video controller
/*TODO*///
/*TODO*///		atarigen_video_control_reset - initializes the video controller
/*TODO*///
/*TODO*///		atarigen_video_control_w - write handler for the video controller
/*TODO*///		atarigen_video_control_r - read handler for the video controller

    /*--------------------------------------------------------------------------*/
    public static class atarigen_video_control_state_desc {

        public int latch1;								/* latch #1 value (-1 means disabled) */

        public int latch2;								/* latch #2 value (-1 means disabled) */

        public int rowscroll_enable;					/* true if row-scrolling is enabled */

        public int palette_bank;						/* which palette bank is enabled */

        public int pf1_xscroll;						/* playfield 1 xscroll */

        public int pf1_yscroll;						/* playfield 1 yscroll */

        public int pf2_xscroll;						/* playfield 2 xscroll */

        public int pf2_yscroll;						/* playfield 2 yscroll */

        public int sprite_xscroll;						/* sprite xscroll */

        public int sprite_yscroll;						/* sprite xscroll */

    };
    /*TODO*///
/*TODO*///extern UINT8 *atarigen_video_control_data;
/*TODO*///extern struct atarigen_video_control_state_desc atarigen_video_control_state;
/*TODO*///
/*TODO*///void atarigen_video_control_reset(void);
/*TODO*///void atarigen_video_control_update(const UINT8 *data);
/*TODO*///
/*TODO*///void atarigen_video_control_w(int offset, int data);
/*TODO*///int atarigen_video_control_r(int offset);
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*--------------------------------------------------------------------------
/*TODO*///
/*TODO*///	Motion object rendering
/*TODO*///
/*TODO*///		atarigen_mo_desc - description of the M.O. layout
/*TODO*///
/*TODO*///		atarigen_mo_callback - called back for each M.O. during processing
/*TODO*///
/*TODO*///		atarigen_mo_init - initializes and configures the M.O. list walker
/*TODO*///		atarigen_mo_free - frees all memory allocated by atarigen_mo_init
/*TODO*///		atarigen_mo_reset - reset for a new frame (use only if not using interrupt system)
/*TODO*///		atarigen_mo_update - updates the M.O. list for the given scanline
/*TODO*///		atarigen_mo_process - processes the current list
/*TODO*///
/*TODO*///--------------------------------------------------------------------------*/
    public static final int ATARIGEN_MAX_MAXCOUNT = 1024;	/* no more than 1024 MO's ever */


    public static class atarigen_mo_desc {

        public atarigen_mo_desc(){
            this.maxcount = 0;
            this.entryskip = 0;
            this.wordskip = 0;
            this.ignoreword = 0;
            this.linkword = 0;
            this.linkshift = 0;
            this.linkmask = 0;
            this.reverse = 0;
            this.entrywords = 0;
        }
        
        public atarigen_mo_desc(int maxcount, int entryskip, int wordskip, int ignoreword, int linkword, int linkshift, int linkmask, int reverse, int entrywords) {
            this.maxcount = maxcount;
            this.entryskip = entryskip;
            this.wordskip = wordskip;
            this.ignoreword = ignoreword;
            this.linkword = linkword;
            this.linkshift = linkshift;
            this.linkmask = linkmask;
            this.reverse = reverse;
            this.entrywords = entrywords;
        }

        public atarigen_mo_desc(int maxcount, int entryskip, int wordskip, int ignoreword, int linkword, int linkshift, int linkmask, int reverse) {
            this.maxcount = maxcount;
            this.entryskip = entryskip;
            this.wordskip = wordskip;
            this.ignoreword = ignoreword;
            this.linkword = linkword;
            this.linkshift = linkshift;
            this.linkmask = linkmask;
            this.reverse = reverse;
            this.entrywords = 0;
        }
        public int maxcount;                           /* maximum number of MO's */

        public int entryskip;                          /* number of bytes per MO entry */

        public int wordskip;                           /* number of bytes between MO words */

        public int ignoreword;                         /* ignore an entry if this word == 0xffff */

        public int linkword, linkshift, linkmask;		/* link = (data[linkword >> linkshift) & linkmask */

        public int reverse;                            /* render in reverse link order */

        public int entrywords;							/* number of words/entry (0 defaults to 4) */

    };

    /*TODO*///typedef void (*atarigen_mo_callback)(const UINT16 *data, const struct rectangle *clip, void *param);
    public static abstract interface atarigen_mo_callback {

        public abstract void handler(ShortPtr data, rectangle clip, Object param);
    }
/*TODO*///
/*TODO*///int atarigen_mo_init(const struct atarigen_mo_desc *source_desc);
/*TODO*///void atarigen_mo_free(void);
/*TODO*///void atarigen_mo_reset(void);
/*TODO*///void atarigen_mo_update(const UINT8 *base, int start, int scanline);
/*TODO*///void atarigen_mo_update_slip_512(const UINT8 *base, int scroll, int scanline, const UINT8 *slips);
/*TODO*///void atarigen_mo_process(atarigen_mo_callback callback, void *param);
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*--------------------------------------------------------------------------
/*TODO*///
/*TODO*///	RLE Motion object rendering/decoding
/*TODO*///
/*TODO*///		atarigen_rle_descriptor - describes a single object
/*TODO*///
/*TODO*///		atarigen_rle_count - total number of objects found
/*TODO*///		atarigen_rle_info - array of descriptors for objects we found
/*TODO*///
/*TODO*///		atarigen_rle_init - prescans the RLE objects
/*TODO*///		atarigen_rle_free - frees all memory allocated by atarigen_rle_init
/*TODO*///		atarigen_rle_render - render an RLE-compressed motion object
/*TODO*///
/*TODO*///--------------------------------------------------------------------------*/
/*TODO*///struct atarigen_rle_descriptor
/*TODO*///{
/*TODO*///	int width;
/*TODO*///	int height;
/*TODO*///	INT16 xoffs;
/*TODO*///	INT16 yoffs;
/*TODO*///	int bpp;
/*TODO*///	UINT32 pen_usage;
/*TODO*///	UINT32 pen_usage_hi;
/*TODO*///	const UINT16 *table;
/*TODO*///	const UINT16 *data;
/*TODO*///};
/*TODO*///
/*TODO*///extern int atarigen_rle_count;
/*TODO*///extern struct atarigen_rle_descriptor *atarigen_rle_info;
/*TODO*///
/*TODO*///int atarigen_rle_init(int region, int colorbase);
/*TODO*///void atarigen_rle_free(void);
/*TODO*///void atarigen_rle_render(struct osd_bitmap *bitmap, struct atarigen_rle_descriptor *info, int color, int hflip, int vflip,
/*TODO*///	int x, int y, int xscale, int yscale, const struct rectangle *clip);
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*--------------------------------------------------------------------------
/*TODO*///
/*TODO*///	Playfield rendering
/*TODO*///
/*TODO*///		atarigen_pf_state - data block describing the playfield
/*TODO*///
/*TODO*///		atarigen_pf_callback - called back for each chunk during processing
/*TODO*///
/*TODO*///		atarigen_pf_init - initializes and configures the playfield params
/*TODO*///		atarigen_pf_free - frees all memory allocated by atarigen_pf_init
/*TODO*///		atarigen_pf_reset - reset for a new frame (use only if not using interrupt system)
/*TODO*///		atarigen_pf_update - updates the playfield params for the given scanline
/*TODO*///		atarigen_pf_process - processes the current list of parameters
/*TODO*///
/*TODO*///		atarigen_pf2_init - same as above but for a second playfield
/*TODO*///		atarigen_pf2_free - same as above but for a second playfield
/*TODO*///		atarigen_pf2_reset - same as above but for a second playfield
/*TODO*///		atarigen_pf2_update - same as above but for a second playfield
/*TODO*///		atarigen_pf2_process - same as above but for a second playfield
/*TODO*///
/*TODO*///--------------------------------------------------------------------------*/
/*TODO*///extern struct osd_bitmap *atarigen_pf_bitmap;
/*TODO*///extern UINT8 *atarigen_pf_dirty;
/*TODO*///extern UINT8 *atarigen_pf_visit;
/*TODO*///
/*TODO*///extern struct osd_bitmap *atarigen_pf2_bitmap;
/*TODO*///extern UINT8 *atarigen_pf2_dirty;
/*TODO*///extern UINT8 *atarigen_pf2_visit;
/*TODO*///
/*TODO*///extern struct osd_bitmap *atarigen_pf_overrender_bitmap;
/*TODO*///extern UINT16 atarigen_overrender_colortable[32];
    public static class atarigen_pf_desc {

        public atarigen_pf_desc(int tilewidth, int tileheight, int xtiles, int ytiles, int noscroll) {
            this.tilewidth = tilewidth;
            this.tileheight = tileheight;
            this.xtiles = xtiles;
            this.ytiles = ytiles;
            this.noscroll = noscroll;
        }
        
        public atarigen_pf_desc(int tilewidth, int tileheight, int xtiles, int ytiles) {
            this.tilewidth = tilewidth;
            this.tileheight = tileheight;
            this.xtiles = xtiles;
            this.ytiles = ytiles;            
        }
        
        public int tilewidth, tileheight;              /* width/height of each tile */

        public int xtiles, ytiles;						/* number of tiles in each direction */

        public int noscroll;							/* non-scrolling? */

    };

    public static class atarigen_pf_state {

        public atarigen_pf_state()
        {
            param=new int[2];
        }
        public int hscroll;							/* current horizontal starting offset */

        public int vscroll;							/* current vertical starting offset */

        public int[] param;							/* up to 2 other parameters that will cause a boundary break */

    };
    public static abstract interface atarigen_pf_callbackPtr {

        public abstract void handler(rectangle tiles, rectangle clip, atarigen_pf_state state, Object param);
    }

/*TODO*////*--------------------------------------------------------------------------
/*TODO*///
/*TODO*///	Misc Video stuff
/*TODO*///
/*TODO*///		atarigen_get_hblank - returns the current HBLANK state
/*TODO*///		atarigen_halt_until_hblank_0_w - write handler for a HBLANK halt
/*TODO*///		atarigen_666_paletteram_w - 6-6-6 special RGB paletteram handler
/*TODO*///		atarigen_expanded_666_paletteram_w - byte version of above
/*TODO*///
/*TODO*///--------------------------------------------------------------------------*/
/*TODO*///int atarigen_get_hblank(void);
/*TODO*///void atarigen_halt_until_hblank_0_w(int offset, int data);
/*TODO*///void atarigen_666_paletteram_w(int offset, int data);
/*TODO*///void atarigen_expanded_666_paletteram_w(int offset, int data);
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*--------------------------------------------------------------------------
/*TODO*///
/*TODO*///	General stuff
/*TODO*///
/*TODO*///		atarigen_show_slapstic_message - display warning about slapstic
/*TODO*///		atarigen_show_sound_message - display warning about coins
/*TODO*///		atarigen_update_messages - update messages
/*TODO*///
/*TODO*///--------------------------------------------------------------------------*/
/*TODO*///void atarigen_show_slapstic_message(void);
/*TODO*///void atarigen_show_sound_message(void);
/*TODO*///void atarigen_update_messages(void);



    /*--------------------------------------------------------------------------

            Motion object drawing macros

                    atarigen_mo_compute_clip - computes the M.O. clip rect
                    atarigen_mo_compute_clip_8x8 - computes the M.O. clip rect
                    atarigen_mo_compute_clip_16x16 - computes the M.O. clip rect

                    atarigen_mo_draw - draws a generically-sized M.O.
                    atarigen_mo_draw_strip - draws a generically-sized M.O. strip
                    atarigen_mo_draw_8x8 - draws an 8x8 M.O.
                    atarigen_mo_draw_8x8_strip - draws an 8x8 M.O. strip (hsize == 1)
                    atarigen_mo_draw_16x16 - draws a 16x16 M.O.
                    atarigen_mo_draw_16x16_strip - draws a 16x16 M.O. strip (hsize == 1)

    --------------------------------------------------------------------------*/
    public static void atarigen_mo_compute_clip(rectangle dest, int xpos, int ypos, int hsize, int vsize, rectangle clip, int tile_width, int tile_height) 
    {																								
            /* determine the bounding box */															
            dest.min_x = xpos;																			
            dest.min_y = ypos;																			
            dest.max_x = xpos + hsize * tile_width - 1;													
            dest.max_y = ypos + vsize * tile_height - 1;												

            /* clip to the display */																	
            if (dest.min_x < clip.min_x)																
                    dest.min_x = clip.min_x;																
            else if (dest.min_x > clip.max_x)															
                    dest.min_x = clip.max_x;																
            if (dest.max_x < clip.min_x)																
                    dest.max_x = clip.min_x;																
            else if (dest.max_x > clip.max_x)															
                    dest.max_x = clip.max_x;																
            if (dest.min_y < clip.min_y)																
                    dest.min_y = clip.min_y;																
            else if (dest.min_y > clip.max_y)															
                    dest.min_y = clip.max_y;																
            if (dest.max_y < clip.min_y)																
                    dest.max_y = clip.min_y;																
            else if (dest.max_y > clip.max_y)															
                    dest.max_y = clip.max_y;																
    }

    public static void atarigen_mo_compute_clip_8x8(rectangle dest, int xpos, int ypos, int hsize, int vsize, rectangle clip)
    {
	atarigen_mo_compute_clip(dest, xpos, ypos, hsize, vsize, clip, 8, 8);
    }

/*TODO*///#define atarigen_mo_compute_clip_16x8(dest, xpos, ypos, hsize, vsize, clip) \
/*TODO*///	atarigen_mo_compute_clip(dest, xpos, ypos, hsize, vsize, clip, 16, 8)
/*TODO*///
/*TODO*///#define atarigen_mo_compute_clip_16x16(dest, xpos, ypos, hsize, vsize, clip) \
/*TODO*///	atarigen_mo_compute_clip(dest, xpos, ypos, hsize, vsize, clip, 16, 16)
/*TODO*///
/*TODO*///
    public static void atarigen_mo_draw(osd_bitmap bitmap, GfxElement gfx, int code, int color, int hflip, int vflip, int x, int y, int hsize, int vsize, rectangle clip, int trans, int trans_pen, int tile_width, int tile_height)
    {																										
            int tilex, tiley, screenx, screendx, screendy;														
            int startx = x;																						
            int screeny = y;																					
            int tile = code;																					
                                                                                                                                                                                                                    
            /* adjust for h flip */																				
            if (hflip != 0){
                    startx += (hsize - 1) * tile_width;
                    screendx = -tile_width;
            }
            else																								
                    screendx = tile_width;																			
                                                                                                                                                                                                                    
            /* adjust for v flip */																				
            if (vflip != 0){
                    screeny += (vsize - 1) * tile_height;
                    screendy = -tile_height;
            }
            else																								
                    screendy = tile_height;																			
                                                                                                                                                                                                                    
            /* loop over the height */																			
            for (tiley = 0; tiley < vsize; tiley++, screeny += screendy)										
            {																									
                    /* clip the Y coordinate */																		
                    if (screeny <= clip.min_y - tile_height)														
                    {																								
                            tile += hsize;																				
                            continue;																					
                    }																								
                    else if (screeny > clip.max_y)																	
                            break;																						
                                                                                                                                                                                                                    
                    /* loop over the width */																		
                    for (tilex = 0, screenx = startx; tilex < hsize; tilex++, screenx += screendx, tile++)			
                    {																								
                            /* clip the X coordinate */																	
                            if (screenx <= clip.min_x - tile_width || screenx > clip.max_x)							
                                    continue;																				
                                                                                                                                                                                                                    
                            /* draw the sprite */																		
                            drawgfx(bitmap, gfx, tile, color, hflip, vflip, screenx, screeny, clip, trans, trans_pen);	
                    }																								
            }																									
    }

    public static void atarigen_mo_draw_transparent(osd_bitmap bitmap, GfxElement gfx, int code, int hflip, int vflip, int x, int y, int hsize, int vsize, rectangle clip, int trans, int trans_pen, int tile_width, int tile_height)
    {
            UShortArray temp = new UShortArray(gfx.colortable);
            gfx.colortable = new UShortArray(atarigen_overrender_colortable);
            atarigen_mo_draw(bitmap, gfx, code, 0, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen, tile_width, tile_height);
            gfx.colortable = temp;
    }

/*TODO*///#define atarigen_mo_draw_strip(bitmap, gfx, code, color, hflip, vflip, x, y, vsize, clip, trans, trans_pen, tile_width, tile_height) \
/*TODO*///{																										\
/*TODO*///	int tiley, screendy;																				\
/*TODO*///	int screenx = x;																					\
/*TODO*///	int screeny = y;																					\
/*TODO*///	int tile = code;																					\
/*TODO*///																										\
/*TODO*///	/* clip the X coordinate */																			\
/*TODO*///	if (screenx > clip->min_x - tile_width && screenx <= clip->max_x)									\
/*TODO*///	{																									\
/*TODO*///		/* adjust for v flip */																			\
/*TODO*///		if (vflip)																						\
/*TODO*///			screeny += (vsize - 1) * tile_height, screendy = -tile_height;								\
/*TODO*///		else																							\
/*TODO*///			screendy = tile_height;																		\
/*TODO*///																										\
/*TODO*///		/* loop over the height */																		\
/*TODO*///		for (tiley = 0; tiley < vsize; tiley++, screeny += screendy, tile++)							\
/*TODO*///		{																								\
/*TODO*///			/* clip the Y coordinate */																	\
/*TODO*///			if (screeny <= clip->min_y - tile_height)													\
/*TODO*///				continue;																				\
/*TODO*///			else if (screeny > clip->max_y)																\
/*TODO*///				break;																					\
/*TODO*///																										\
/*TODO*///			/* draw the sprite */																		\
/*TODO*///			drawgfx(bitmap, gfx, tile, color, hflip, vflip, screenx, screeny, clip, trans, trans_pen);	\
/*TODO*///		}																								\
/*TODO*///	}																									\
/*TODO*///}
/*TODO*///
/*TODO*///#define atarigen_mo_draw_transparent_strip(bitmap, gfx, code, hflip, vflip, x, y, vsize, clip, trans, trans_pen, tile_width, tile_height) \
/*TODO*///{																										\
/*TODO*///	UINT16 *temp = gfx->colortable;																\
/*TODO*///	gfx->colortable = atarigen_overrender_colortable;													\
/*TODO*///	atarigen_mo_draw_strip(bitmap, gfx, code, 0, hflip, vflip, x, y, vsize, clip, trans, trans_pen, tile_width, tile_height);\
/*TODO*///	gfx->colortable = temp;																				\
/*TODO*///}


    public static void atarigen_mo_draw_8x8(osd_bitmap bitmap, GfxElement gfx, int code, int color, int hflip, int vflip, int x, int y, int hsize, int vsize, rectangle clip, int trans, int trans_pen)
    {
            atarigen_mo_draw(bitmap, gfx, code, color, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen, 8, 8);
                    
    }

/*TODO*///#define atarigen_mo_draw_16x8(bitmap, gfx, code, color, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen) \
/*TODO*///	atarigen_mo_draw(bitmap, gfx, code, color, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen, 16, 8)
/*TODO*///
/*TODO*///#define atarigen_mo_draw_16x16(bitmap, gfx, code, color, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen) \
/*TODO*///	atarigen_mo_draw(bitmap, gfx, code, color, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen, 16, 16)


    public static void atarigen_mo_draw_transparent_8x8(osd_bitmap bitmap, GfxElement gfx, int code, int hflip, int vflip, int x, int y, int hsize, int vsize, rectangle clip, int trans, int trans_pen)
    {
	atarigen_mo_draw_transparent(bitmap, gfx, code, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen, 8, 8);
    }
/*TODO*///#define atarigen_mo_draw_transparent_16x8(bitmap, gfx, code, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen) \
/*TODO*///	atarigen_mo_draw_transparent(bitmap, gfx, code, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen, 16, 8)
/*TODO*///
/*TODO*///#define atarigen_mo_draw_transparent_16x16(bitmap, gfx, code, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen) \
/*TODO*///	atarigen_mo_draw_transparent(bitmap, gfx, code, hflip, vflip, x, y, hsize, vsize, clip, trans, trans_pen, 16, 16)
/*TODO*///
/*TODO*///
/*TODO*///#define atarigen_mo_draw_8x8_strip(bitmap, gfx, code, color, hflip, vflip, x, y, vsize, clip, trans, trans_pen) \
/*TODO*///	atarigen_mo_draw_strip(bitmap, gfx, code, color, hflip, vflip, x, y, vsize, clip, trans, trans_pen, 8, 8)
/*TODO*///
/*TODO*///#define atarigen_mo_draw_16x8_strip(bitmap, gfx, code, color, hflip, vflip, x, y, vsize, clip, trans, trans_pen) \
/*TODO*///	atarigen_mo_draw_strip(bitmap, gfx, code, color, hflip, vflip, x, y, vsize, clip, trans, trans_pen, 16, 8)
/*TODO*///
/*TODO*///#define atarigen_mo_draw_16x16_strip(bitmap, gfx, code, color, hflip, vflip, x, y, vsize, clip, trans, trans_pen) \
/*TODO*///	atarigen_mo_draw_strip(bitmap, gfx, code, color, hflip, vflip, x, y, vsize, clip, trans, trans_pen, 16, 16)
/*TODO*///
/*TODO*///
/*TODO*///#define atarigen_mo_draw_transparent_8x8_strip(bitmap, gfx, code, hflip, vflip, x, y, vsize, clip, trans, trans_pen) \
/*TODO*///	atarigen_mo_draw_transparent_strip(bitmap, gfx, code, hflip, vflip, x, y, vsize, clip, trans, trans_pen, 8, 8)
/*TODO*///
/*TODO*///#define atarigen_mo_draw_transparent_16x8_strip(bitmap, gfx, code, hflip, vflip, x, y, vsize, clip, trans, trans_pen) \
/*TODO*///	atarigen_mo_draw_transparent_strip(bitmap, gfx, code, hflip, vflip, x, y, vsize, clip, trans, trans_pen, 16, 8)
/*TODO*///
/*TODO*///#define atarigen_mo_draw_transparent_16x16_strip(bitmap, gfx, code, hflip, vflip, x, y, vsize, clip, trans, trans_pen) \
/*TODO*///	atarigen_mo_draw_transparent_strip(bitmap, gfx, code, hflip, vflip, x, y, vsize, clip, trans, trans_pen, 16, 16)
/*TODO*///
/*TODO*///#endif
/*TODO*///    
}
