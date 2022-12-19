/***************************************************************************
  vidhrdw.c

  Functions to emulate the video hardware of these machines.
  Video is 30x40 tiles. (200x320 for Twin Cobra/Flying shark)
  Video is 40x30 tiles. (320x200 for Wardner)

  Video has 3 scrolling tile layers (Background, Foreground and Text) and
  Sprites that have 4 (5?) priorities. Lowest priority is "Off".
  Wardner has an unusual sprite priority in the shop scenes, whereby a
  middle level priority Sprite appears over a high priority Sprite ?

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class twincobr
{
	
	
	static UBytePtr twincobr_bgvideoram;
	static UBytePtr twincobr_fgvideoram;
	
	int wardner_sprite_hack = 0;	/* Required for weird sprite priority in wardner  */
									/* when hero is in shop. Hero should cover shop owner */
	
	extern int toaplan_main_cpu;	/* Main CPU type.  0 = 68000, 1 = Z80 */
	
	#define READ_WORD_Z80(x) (*(UBytePtr )(x) + (*(UBytePtr )(x+1) << 8))
	#define WRITE_WORD_Z80(a, d) (*(UBytePtr )(a) = d & 0xff, (*(UBytePtr )(a+1) = (d>>8) & 0xff))
	
	static size_t twincobr_bgvideoram_size,twincobr_fgvideoram_size;
	static int txscrollx = 0;
	static int txscrolly = 0;
	static int fgscrollx = 0;
	static int fgscrolly = 0;
	static int bgscrollx = 0;
	static int bgscrolly = 0;
	int twincobr_fg_rom_bank = 0;
	int twincobr_bg_ram_bank = 0;
	int twincobr_display_on = 1;
	int twincobr_flip_screen = 0;
	int twincobr_flip_x_base = 0x37;	/* value to 0 the X scroll offsets (non-flip) */
	int twincobr_flip_y_base = 0x1e;	/* value to 0 the Y scroll offsets (non-flip) */
	
	static int txoffs = 0;
	static int bgoffs = 0;
	static int fgoffs = 0;
	static int scroll_x = 0;
	static int scroll_y = 0;
	
	static int vidbaseaddr = 0;
	static int scroll_realign_x = 0;
	
	/************************* Wardner variables *******************************/
	
	static int tx_offset_lsb = 0;
	static int tx_offset_msb = 0;
	static int bg_offset_lsb = 0;
	static int bg_offset_msb = 0;
	static int fg_offset_lsb = 0;
	static int fg_offset_msb = 0;
	static int tx_scrollx_lsb = 0;
	static int tx_scrollx_msb = 0;
	static int tx_scrolly_lsb = 0;
	static int tx_scrolly_msb = 0;
	static int bg_scrollx_lsb = 0;
	static int bg_scrollx_msb = 0;
	static int bg_scrolly_lsb = 0;
	static int bg_scrolly_msb = 0;
	static int fg_scrollx_lsb = 0;
	static int fg_scrollx_msb = 0;
	static int fg_scrolly_lsb = 0;
	static int fg_scrolly_msb = 0;
	
	
	
	public static VhStartPtr twincobr_vh_start = new VhStartPtr() { public int handler() 
	{
		/* the video RAM is accessed via ports, it's not memory mapped */
		videoram_size[0] = 0x1000;
		twincobr_bgvideoram_size = 0x4000;	/* banked two times 0x2000 */
		twincobr_fgvideoram_size = 0x2000;
	
		if ((videoram = malloc(videoram_size[0])) == 0)
			return 1;
		memset(videoram,0,videoram_size[0]);
	
		if ((twincobr_fgvideoram = malloc(twincobr_fgvideoram_size)) == 0)
		{
			free(videoram);
			return 1;
		}
		memset(twincobr_fgvideoram,0,twincobr_fgvideoram_size);
	
		if ((twincobr_bgvideoram = malloc(twincobr_bgvideoram_size)) == 0)
		{
			free(twincobr_fgvideoram);
			free(videoram);
			return 1;
		}
		memset(twincobr_bgvideoram,0,twincobr_bgvideoram_size);
	
		if ((dirtybuffer = malloc(twincobr_bgvideoram_size)) == 0)
		{
			free(twincobr_bgvideoram);
			free(twincobr_fgvideoram);
			free(videoram);
			return 1;
		}
		memset(dirtybuffer,1,twincobr_bgvideoram_size);
	
		if ((tmpbitmap = bitmap_alloc(Machine.drv.screen_width,2*Machine.drv.screen_height)) == 0)
		{
			free(dirtybuffer);
			free(twincobr_bgvideoram);
			free(twincobr_fgvideoram);
			free(videoram);
			return 1;
		}
	
		return 0;
	} };
	
	public static VhStopPtr twincobr_vh_stop = new VhStopPtr() { public void handler() 
	{
		bitmap_free(tmpbitmap);
		free(dirtybuffer);
		free(twincobr_bgvideoram);
		free(twincobr_fgvideoram);
		free(videoram);
	} };
	
	
	public static ReadHandlerPtr twincobr_crtc_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return crtc6845_register_r(offset);
	} };
	
	public static WriteHandlerPtr twincobr_crtc_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (offset == 0) crtc6845_address_w(offset, data);
		if (offset == 2) crtc6845_register_w(offset, data);
	} };
	
	int twincobr_txoffs_r(void)
	{
		return txoffs / 2;
	}
	public static WriteHandlerPtr twincobr_txoffs_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		txoffs = (2 * data) % videoram_size[0];
	} };
	public static ReadHandlerPtr twincobr_txram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return READ_WORD(&videoram.read(txoffs));
	} };
	public static WriteHandlerPtr twincobr_txram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WRITE_WORD(&videoram.read(txoffs),data);
	} };
	
	public static WriteHandlerPtr twincobr_bgoffs_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		bgoffs = (2 * data) % (twincobr_bgvideoram_size >> 1);
	} };
	public static ReadHandlerPtr twincobr_bgram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return READ_WORD(&twincobr_bgvideoram[bgoffs+twincobr_bg_ram_bank]);
	} };
	public static WriteHandlerPtr twincobr_bgram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WRITE_WORD(&twincobr_bgvideoram[bgoffs+twincobr_bg_ram_bank],data);
		dirtybuffer[bgoffs / 2] = 1;
	} };
	
	public static WriteHandlerPtr twincobr_fgoffs_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		fgoffs = (2 * data) % twincobr_fgvideoram_size;
	} };
	public static ReadHandlerPtr twincobr_fgram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return READ_WORD(&twincobr_fgvideoram[fgoffs]);
	} };
	public static WriteHandlerPtr twincobr_fgram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WRITE_WORD(&twincobr_fgvideoram[fgoffs],data);
	} };
	
	
	public static WriteHandlerPtr twincobr_txscroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (offset == 0) txscrollx = data;
		else txscrolly = data;
	} };
	
	public static WriteHandlerPtr twincobr_bgscroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (offset == 0) bgscrollx = data;
		else bgscrolly = data;
	} };
	
	public static WriteHandlerPtr twincobr_fgscroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (offset == 0) fgscrollx = data;
		else fgscrolly = data;
	} };
	
	public static WriteHandlerPtr twincobr_exscroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)	/* Extra unused video layer */
	{
		if (offset == 0) logerror("PC - write %04x to extra video layer Y scroll register\n",data);
		else logerror("PC - write %04x to extra video layer scroll X register\n",data);
	} };
	
	/******************** Wardner interface to this hardware ********************/
	public static WriteHandlerPtr wardner_txlayer_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (offset == 0) tx_offset_lsb = data;
		if (offset == 1) tx_offset_msb = (data << 8);
		twincobr_txoffs_w(0,tx_offset_msb | tx_offset_lsb);
	} };
	public static WriteHandlerPtr wardner_bglayer_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (offset == 0) bg_offset_lsb = data;
		if (offset == 1) bg_offset_msb = (data<<8);
		twincobr_bgoffs_w(0,bg_offset_msb | bg_offset_lsb);
	} };
	public static WriteHandlerPtr wardner_fglayer_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (offset == 0) fg_offset_lsb = data;
		if (offset == 1) fg_offset_msb = (data<<8);
		twincobr_fgoffs_w(0,fg_offset_msb | fg_offset_lsb);
	} };
	
	public static WriteHandlerPtr wardner_txscroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((offset & 2) != 0) {
			if (offset == 2) tx_scrollx_lsb = data;
			if (offset == 3) tx_scrollx_msb = (data<<8);
			twincobr_txscroll_w(2,tx_scrollx_msb | tx_scrollx_lsb);
		}
		else
		{
			if (offset == 0) tx_scrolly_lsb = data;
			if (offset == 1) tx_scrolly_msb = (data<<8);
			twincobr_txscroll_w(0,tx_scrolly_msb | tx_scrolly_lsb);
		}
	} };
	public static WriteHandlerPtr wardner_bgscroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((offset & 2) != 0) {
			if (offset == 2) bg_scrollx_lsb = data;
			if (offset == 3) bg_scrollx_msb = (data<<8);
			twincobr_bgscroll_w(2,bg_scrollx_msb | bg_scrollx_lsb);
		}
		else
		{
			if (offset == 0) bg_scrolly_lsb = data;
			if (offset == 1) bg_scrolly_msb = (data<<8);
			twincobr_bgscroll_w(0,bg_scrolly_msb | bg_scrolly_lsb);
		}
	} };
	public static WriteHandlerPtr wardner_fgscroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((offset & 2) != 0) {
			if (offset == 2) fg_scrollx_lsb = data;
			if (offset == 3) fg_scrollx_msb = (data<<8);
			twincobr_fgscroll_w(2,fg_scrollx_msb | fg_scrollx_lsb);
		}
		else
		{
			if (offset == 0) fg_scrolly_lsb = data;
			if (offset == 1) fg_scrolly_msb = (data<<8);
			twincobr_fgscroll_w(0,fg_scrolly_msb | fg_scrolly_lsb);
		}
	} };
	
	public static ReadHandlerPtr wardner_videoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int memdata = 0;
		switch (offset) {
			case 0: memdata =  twincobr_txram_r(0) & 0x00ff; break;
			case 1: memdata = (twincobr_txram_r(0) & 0xff00) >> 8; break;
			case 2: memdata =  twincobr_bgram_r(0) & 0x00ff; break;
			case 3: memdata = (twincobr_bgram_r(0) & 0xff00) >> 8; break;
			case 4: memdata =  twincobr_fgram_r(0) & 0x00ff; break;
			case 5: memdata = (twincobr_fgram_r(0) & 0xff00) >> 8; break;
		}
		return memdata;
	} };
	
	public static WriteHandlerPtr wardner_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int memdata = 0;
		switch (offset) {
			case 0: memdata = twincobr_txram_r(0) & 0xff00;
					memdata |= data;
					twincobr_txram_w(0,memdata); break;
			case 1: memdata = twincobr_txram_r(0) & 0x00ff;
					memdata |= (data << 8);
					twincobr_txram_w(0,memdata); break;
			case 2: memdata = twincobr_bgram_r(0) & 0xff00;
					memdata |= data;
					twincobr_bgram_w(0,memdata); break;
			case 3: memdata = twincobr_bgram_r(0) & 0x00ff;
					memdata |= (data << 8);
					twincobr_bgram_w(0,memdata); break;
			case 4: memdata = twincobr_fgram_r(0) & 0xff00;
					memdata |= data;
					twincobr_fgram_w(0,memdata); break;
			case 5: memdata = twincobr_fgram_r(0) & 0x00ff;
					memdata |= (data << 8);
					twincobr_fgram_w(0,memdata); break;
		}
	} };
	
	static void twincobr_draw_sprites (struct osd_bitmap *bitmap, int priority)
	{
		int offs;
	
		if (toaplan_main_cpu == 0) /* 68k */
		{
			for (offs = 0;offs < spriteram_size;offs += 8)
			{
				int attribute,sx,sy,flipx,flipy;
				int sprite, color;
	
				attribute = READ_WORD(&buffered_spriteram.read(offs + 2));
				if ((attribute & 0x0c00) == priority) {	/* low priority */
					sy = READ_WORD(&buffered_spriteram.read(offs + 6)) >> 7;
					if (sy != 0x0100) {		/* sx = 0x01a0 or 0x0040*/
						sprite = READ_WORD(&buffered_spriteram.read(offs)) & 0x7ff;
						color  = attribute & 0x3f;
						sx = READ_WORD(&buffered_spriteram.read(offs + 4)) >> 7;
						flipx = attribute & 0x100;
						if (flipx != 0) sx -= 14;		/* should really be 15 */
						flipy = attribute & 0x200;
						drawgfx(bitmap,Machine.gfx[3],
							sprite,
							color,
							flipx,flipy,
							sx-32,sy-16,
							&Machine.visible_area,TRANSPARENCY_PEN,0);
					}
				}
			}
		}
		else /* Z80 */
		{
			for (offs = 0;offs < spriteram_size;offs += 8)
			{
				int attribute,sx,sy,flipx,flipy;
				int sprite, color;
	
				attribute = READ_WORD_Z80(&buffered_spriteram.read(offs + 2));
				if ((attribute & 0x0c00) == priority) {	/* low priority */
					sy = READ_WORD_Z80(&buffered_spriteram.read(offs + 6)) >> 7;
					if (sy != 0x0100) {		/* sx = 0x01a0 or 0x0040*/
						sprite = READ_WORD_Z80(&buffered_spriteram.read(offs)) & 0x7ff;
						color  = attribute & 0x3f;
						sx = READ_WORD_Z80(&buffered_spriteram.read(offs + 4)) >> 7;
						flipx = attribute & 0x100;
						if (flipx != 0) sx -= 14;		/* should really be 15 */
						flipy = attribute & 0x200;
						drawgfx(bitmap,Machine.gfx[3],
							sprite,
							color,
							flipx,flipy,
							sx-32,sy-16,
							&Machine.visible_area,TRANSPARENCY_PEN,0);
					}
				}
			}
		}
	}
	
	
	
	public static VhUpdatePtr twincobr_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
	  static int offs,code,tile,i,pal_base,sprite,color;
	  static int colmask[64];
	
	
	  if (twincobr_display_on != 0) {
		memset(palette_used_colors,PALETTE_COLOR_UNUSED,Machine.drv.total_colors * sizeof(unsigned char));
		{
		pal_base = Machine.drv.gfxdecodeinfo[2].color_codes_start;
	
		for (color = 0;color < 16;color++) colmask[color] = 0;
	
		for (offs = (twincobr_bgvideoram_size >> 1) - 2;offs >= 0;offs -= 2)
		{
			code  = READ_WORD(&twincobr_bgvideoram[(offs+twincobr_bg_ram_bank)]);
			tile  = (code & 0x0fff);
			color = (code & 0xf000) >> 12;
			colmask[color] |= Machine.gfx[2].pen_usage[tile];
		}
	
		for (color = 0;color < 16;color++)
		{
			for (i = 0;i < 16;i++)
			{
				if (colmask[color] & (1 << i))
					palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
			}
		}
	
	
		pal_base = Machine.drv.gfxdecodeinfo[1].color_codes_start;
	
		for (color = 0;color < 16;color++) colmask[color] = 0;
	
		scroll_x = (twincobr_flip_x_base + fgscrollx) & 0x01ff;
		scroll_y = (twincobr_flip_y_base + fgscrolly) & 0x01ff;
		vidbaseaddr = ((scroll_y>>3)*64) + (scroll_x>>3);
		scroll_realign_x = scroll_x >> 3;
		for (offs = (31*41)-1; offs >= 0; offs-- )
		{
			unsigned char sx,sy;
			unsigned short int vidramaddr = 0;
	
			sx = offs % 41;
			sy = offs / 41;
			vidramaddr = ((vidbaseaddr + (sy*64) + sx) * 2);
	
			if ((scroll_realign_x + sx) > 63) vidramaddr -= 128;
	
			code  = READ_WORD(&twincobr_fgvideoram[(vidramaddr & 0x1fff)]);
			tile  = (code & 0x0fff) | twincobr_fg_rom_bank;
			color = (code & 0xf000) >> 12;
			colmask[color] |= Machine.gfx[1].pen_usage[tile];
		}
	
		for (color = 0;color < 16;color++)
		{
			if (colmask[color] & (1 << 0))
				palette_used_colors[pal_base + 16 * color] = PALETTE_COLOR_TRANSPARENT;
			for (i = 1;i < 16;i++)
			{
				if (colmask[color] & (1 << i))
					palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
			}
		}
	
	
		pal_base = Machine.drv.gfxdecodeinfo[3].color_codes_start;
	
		for (color = 0;color < 64;color++) colmask[color] = 0;
	
		if (toaplan_main_cpu == 0) /* 68k */
		{
			for (offs = 0;offs < spriteram_size[0];offs += 8)
			{
				int sy;
				sy = READ_WORD(&buffered_spriteram.read(offs + 6));
				if (sy != 0x8000) {					/* Is sprite is turned off ? */
					sprite = READ_WORD(&buffered_spriteram.read(offs)) & 0x7ff;
					color = READ_WORD(&buffered_spriteram.read(offs + 2)) & 0x3f;
					colmask[color] |= Machine.gfx[3].pen_usage[sprite];
				}
			}
		}
		else /* Z80 */
		{
			for (offs = 0;offs < spriteram_size[0];offs += 8)
			{
				int sy;
				sy = READ_WORD_Z80(&buffered_spriteram.read(offs + 6));
				if (sy != 0x8000) {					/* Is sprite is turned off ? */
					sprite = READ_WORD_Z80(&buffered_spriteram.read(offs)) & 0x7ff;
					color = READ_WORD_Z80(&buffered_spriteram.read(offs + 2)) & 0x3f;
					colmask[color] |= Machine.gfx[3].pen_usage[sprite];
				}
			}
		}
	
		for (color = 0;color < 64;color++)
		{
			if (colmask[color] & (1 << 0))
				palette_used_colors[pal_base + 16 * color] = PALETTE_COLOR_TRANSPARENT;
			for (i = 1;i < 16;i++)
			{
				if (colmask[color] & (1 << i))
					palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
			}
		}
	
	
		pal_base = Machine.drv.gfxdecodeinfo[0].color_codes_start;
	
		for (color = 0;color < 32;color++) colmask[color] = 0;
	
	
		scroll_x = (twincobr_flip_x_base + txscrollx) & 0x01ff;
		scroll_y = (twincobr_flip_y_base + txscrolly) & 0x00ff;
		vidbaseaddr = ((scroll_y>>3)*64) + (scroll_x>>3);
		scroll_realign_x = scroll_x>>3;
		for (offs = (31*41)-1; offs >= 0; offs-- )
		{
			unsigned char sx,sy;
			unsigned short int vidramaddr = 0;
	
			sx = offs % 41;
			sy = offs / 41;
	
			vidramaddr = (vidbaseaddr + (sy*64) + sx) * 2;
			if ((scroll_realign_x + sx) > 63) vidramaddr -= 128;
			code = READ_WORD(&videoram.read((vidramaddr & 0x0fff)));
			tile  = (code & 0x07ff);
			color = (code & 0xf800) >> 11;
			colmask[color] |= Machine.gfx[0].pen_usage[tile];
		}
	
	
		for (color = 0;color < 32;color++)
		{
			if (colmask[color] & (1 << 0))
				palette_used_colors[pal_base + 8 * color] = PALETTE_COLOR_TRANSPARENT;
			for (i = 1;i < 8;i++)
			{
				if (colmask[color] & (1 << i))
					palette_used_colors[pal_base + 8 * color + i] = PALETTE_COLOR_USED;
			}
		}
	
	
		if (palette_recalc())
		{
			memset(dirtybuffer,1,twincobr_bgvideoram_size >> 1);
		}
		}
	
	
		/* draw the background */
		for (offs = (twincobr_bgvideoram_size >> 1) - 2;offs >= 0;offs -= 2)
		{
			if (dirtybuffer[offs / 2])
			{
				int sx,sy;
	
				dirtybuffer[offs / 2] = 0;
	
				sx = (offs/2) % 64;
				sy = (offs/2) / 64;
	
				code = READ_WORD(&twincobr_bgvideoram[offs+twincobr_bg_ram_bank]);
				tile  = (code & 0x0fff);
				color = (code & 0xf000) >> 12;
				if (twincobr_flip_screen != 0) { sx=63-sx; sy=63-sy; }
				drawgfx(tmpbitmap,Machine.gfx[2],
					tile,
					color,
					twincobr_flip_screen,twincobr_flip_screen,
					8*sx,8*sy,
					0,TRANSPARENCY_NONE,0);
			}
		}
	
		/* copy the background graphics */
		{
			if (twincobr_flip_screen != 0) {
				scroll_x = (twincobr_flip_x_base + bgscrollx + 0x141) & 0x1ff;
				scroll_y = (twincobr_flip_y_base + bgscrolly + 0xf1) & 0x1ff;
			}
			else {
				scroll_x = (0x1c9 - bgscrollx) & 0x1ff;
				scroll_y = (- 0x1e - bgscrolly) & 0x1ff;
			}
			copyscrollbitmap(bitmap,tmpbitmap,1,&scroll_x,1,&scroll_y,&Machine.visible_area,TRANSPARENCY_NONE,0);
		}
	
	
		/* draw the sprites in low priority (Twin Cobra tanks under roofs) */
		twincobr_draw_sprites (bitmap, 0x0400);
	
		/* draw the foreground */
		scroll_x = (twincobr_flip_x_base + fgscrollx) & 0x01ff;
		scroll_y = (twincobr_flip_y_base + fgscrolly) & 0x01ff;
		vidbaseaddr = ((scroll_y>>3)*64) + (scroll_x>>3);
		scroll_realign_x = scroll_x >> 3;		/* realign video ram pointer */
		for (offs = (31*41)-1; offs >= 0; offs-- )
		{
			int xpos,ypos;
			unsigned char sx,sy;
			unsigned short int vidramaddr = 0;
	
			sx = offs % 41;
			sy = offs / 41;
	
			vidramaddr = ((vidbaseaddr + (sy*64) + sx) * 2);
			if ((scroll_realign_x + sx) > 63) vidramaddr -= 128;
	
			code  = READ_WORD(&twincobr_fgvideoram[(vidramaddr & 0x1fff)]);
			tile  = (code & 0x0fff) | twincobr_fg_rom_bank;
			color = (code & 0xf000) >> 12;
			if (twincobr_flip_screen != 0) { sx=40-sx; sy=30-sy; xpos=(sx*8) - (7-(scroll_x&7)); ypos=(sy*8) - (7-(scroll_y&7)); }
			else { xpos=(sx*8) - (scroll_x&7); ypos=(sy*8) - (scroll_y&7); }
			drawgfx(bitmap,Machine.gfx[1],
				tile,
				color,
				twincobr_flip_screen,twincobr_flip_screen,
				xpos,ypos,
				&Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	
	/*********  Begin ugly sprite hack for Wardner when hero is in shop *********/
		if ((wardner_sprite_hack) && (fgscrollx != bgscrollx)) {	/* Wardner ? */
			if ((fgscrollx==0x1c9) || (twincobr_flip_screen && (fgscrollx==0x17a))) {	/* in the shop ? */
				int wardner_hack = READ_WORD_Z80(&buffered_spriteram.read(0x0b04));
			/* sprite position 0x6300 to 0x8700 -- hero on shop keeper (normal) */
			/* sprite position 0x3900 to 0x5e00 -- hero on shop keeper (flip) */
				if ((wardner_hack > 0x3900) && (wardner_hack < 0x8700)) {	/* hero at shop keeper ? */
						wardner_hack = READ_WORD_Z80(&buffered_spriteram.read(0x0b02));
						wardner_hack |= 0x0400;			/* make hero top priority */
						WRITE_WORD_Z80(&buffered_spriteram.read(0x0b02),wardner_hack);
						wardner_hack = READ_WORD_Z80(&buffered_spriteram.read(0x0b0a));
						wardner_hack |= 0x0400;
						WRITE_WORD_Z80(&buffered_spriteram.read(0x0b0a),wardner_hack);
						wardner_hack = READ_WORD_Z80(&buffered_spriteram.read(0x0b12));
						wardner_hack |= 0x0400;
						WRITE_WORD_Z80(&buffered_spriteram.read(0x0b12),wardner_hack);
						wardner_hack = READ_WORD_Z80(&buffered_spriteram.read(0x0b1a));
						wardner_hack |= 0x0400;
						WRITE_WORD_Z80(&buffered_spriteram.read(0x0b1a),wardner_hack);
				}
			}
		}
	/**********  End ugly sprite hack for Wardner when hero is in shop **********/
	
		/* draw the sprites in normal priority */
		twincobr_draw_sprites (bitmap, 0x0800);
	
		/* draw the top layer */
		scroll_x = (twincobr_flip_x_base + txscrollx) & 0x01ff;
		scroll_y = (twincobr_flip_y_base + txscrolly) & 0x00ff;
		vidbaseaddr = ((scroll_y>>3)*64) + (scroll_x>>3);
		scroll_realign_x = scroll_x >> 3;
		for (offs = (31*41)-1; offs >= 0; offs-- )
		{
			int xpos,ypos;
			unsigned char sx,sy;
			unsigned short int vidramaddr = 0;
	
			sx = offs % 41;
			sy = offs / 41;
	
			vidramaddr = (vidbaseaddr + (sy*64) + sx) * 2;
			if ((scroll_realign_x + sx) > 63) vidramaddr -=128;
	
			code  = READ_WORD(&videoram.read((vidramaddr & 0x0fff)));
			tile  = (code & 0x07ff);
			color = (code & 0xf800) >> 11;
			if (twincobr_flip_screen != 0) { sx=40-sx; sy=30-sy; xpos=(sx*8) - (7-(scroll_x&7)); ypos=(sy*8) - (7-(scroll_y&7)); }
			else { xpos=(sx*8) - (scroll_x&7); ypos=(sy*8) - (scroll_y&7); }
			drawgfx(bitmap,Machine.gfx[0],
				tile,
				color,
				twincobr_flip_screen,twincobr_flip_screen,
				xpos,ypos,
				&Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	
		/* draw the sprites in high priority */
		twincobr_draw_sprites (bitmap, 0x0c00);
	
	  }
	} };
	
	public static VhEofCallbackPtr twincobr_eof_callback = new VhEofCallbackPtr() { public void handler() 
	{
		/*  Spriteram is always 1 frame ahead, suggesting spriteram buffering.
			There are no CPU output registers that control this so we
			assume it happens automatically every frame, at the end of vblank */
		buffer_spriteram_w(0,0);
	} };
	
}
