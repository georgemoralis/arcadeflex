/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class atarifb
{
	
	/* local */
	size_t atarifb_alphap1_vram_size;
	size_t atarifb_alphap2_vram_size;
	UBytePtr atarifb_alphap1_vram;
	UBytePtr atarifb_alphap2_vram;
	UBytePtr atarifb_scroll_register;
	UBytePtr alphap1_dirtybuffer;
	UBytePtr alphap2_dirtybuffer;
	
	extern int atarifb_game;
	
	
	struct rectangle bigfield_area = {  4*8, 34*8-1, 0*8, 32*8-1 };
	struct rectangle left_area =     {  0*8,  3*8-1, 0*8, 32*8-1 };
	struct rectangle right_area =    { 34*8, 38*8-1, 0*8, 32*8-1 };
	
	/***************************************************************************
	***************************************************************************/
	public static WriteHandlerPtr atarifb_alphap1_vram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (atarifb_alphap1_vram[offset] != data)
		{
			atarifb_alphap1_vram[offset] = data;
	
			alphap1_dirtybuffer[offset] = 1;
		}
	} };
	
	public static WriteHandlerPtr atarifb_alphap2_vram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (atarifb_alphap2_vram[offset] != data)
		{
			atarifb_alphap2_vram[offset] = data;
	
			alphap2_dirtybuffer[offset] = 1;
		}
	} };
	
	/***************************************************************************
	***************************************************************************/
	public static WriteHandlerPtr atarifb_scroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (data - 8 != *atarifb_scroll_register)
		{
			*atarifb_scroll_register = data - 8;
			memset(dirtybuffer,1,videoram_size[0]);
		}
	} };
	
	/***************************************************************************
	***************************************************************************/
	
	public static VhStartPtr atarifb_vh_start = new VhStartPtr() { public int handler() 
	{
		if (generic_vh_start()!=0)
			return 1;
	
		alphap1_dirtybuffer = malloc (atarifb_alphap1_vram_size);
		alphap2_dirtybuffer = malloc (atarifb_alphap2_vram_size);
		if ((!alphap1_dirtybuffer) || (!alphap2_dirtybuffer))
		{
			generic_vh_stop();
			return 1;
		}
	
		memset(alphap1_dirtybuffer, 1, atarifb_alphap1_vram_size);
		memset(alphap2_dirtybuffer, 1, atarifb_alphap2_vram_size);
		memset(dirtybuffer, 1, videoram_size[0]);
	
		return 0;
	} };
	
	/***************************************************************************
	***************************************************************************/
	
	public static VhStopPtr atarifb_vh_stop = new VhStopPtr() { public void handler() 
	{
		generic_vh_stop();
		free (alphap1_dirtybuffer);
		free (alphap2_dirtybuffer);
	} };
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr atarifb_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs,obj;
		int sprite_bank;
	
		if (full_refresh != 0)
		{
			memset(alphap1_dirtybuffer, 1, atarifb_alphap1_vram_size);
			memset(alphap2_dirtybuffer, 1, atarifb_alphap2_vram_size);
			memset(dirtybuffer,1,videoram_size[0]);
		}
	
		/* Soccer uses a different graphics set for sprites */
		if (atarifb_game == 4)
			sprite_bank = 2;
		else
			sprite_bank = 1;
	
		/* for every character in the Player 1 Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = atarifb_alphap1_vram_size - 1;offs >= 0;offs--)
		{
			if (alphap1_dirtybuffer[offs])
			{
				int charcode;
				int flipbit;
				int disable;
				int sx,sy;
	
				alphap1_dirtybuffer[offs] = 0;
	
				sx = 8 * (offs / 32) + 35*8;
				sy = 8 * (offs % 32) + 8;
	
				charcode = atarifb_alphap1_vram[offs] & 0x3f;
				flipbit = (atarifb_alphap1_vram[offs] & 0x40) >> 6;
				disable = (atarifb_alphap1_vram[offs] & 0x80) >> 7;
	
				if (!disable)
				{
					drawgfx(bitmap,Machine.gfx[0],
						charcode, 0,
						flipbit,flipbit,sx,sy,
						&right_area,TRANSPARENCY_NONE,0);
				}
			}
		}
	
		/* for every character in the Player 2 Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = atarifb_alphap2_vram_size - 1;offs >= 0;offs--)
		{
			if (alphap2_dirtybuffer[offs])
			{
				int charcode;
				int flipbit;
				int disable;
				int sx,sy;
	
				alphap2_dirtybuffer[offs] = 0;
	
				sx = 8 * (offs / 32);
				sy = 8 * (offs % 32) + 8;
	
				charcode = atarifb_alphap2_vram[offs] & 0x3f;
				flipbit = (atarifb_alphap2_vram[offs] & 0x40) >> 6;
				disable = (atarifb_alphap2_vram[offs] & 0x80) >> 7;
	
				if (!disable)
				{
					drawgfx(bitmap,Machine.gfx[0],
						charcode, 0,
						flipbit,flipbit,sx,sy,
						&left_area,TRANSPARENCY_NONE,0);
				}
			}
		}
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs])
			{
				int charcode;
				int flipx,flipy;
				int sx,sy;
	
				dirtybuffer[offs]=0;
	
				charcode = videoram.read(offs)& 0x3f;
				flipx = (videoram.read(offs)& 0x40) >> 6;
				flipy = (videoram.read(offs)& 0x80) >> 7;
	
				sx = (8 * (offs % 32) - *atarifb_scroll_register);
				sy = 8 * (offs / 32) + 8;
	
				/* Soccer hack */
				if (atarifb_game == 4)
				{
					sy += 8;
				}
	
				/* Baseball hack */
				if (atarifb_game == 0x03) sx -= 8;
	
				if (sx < 0) sx += 256;
				if (sx > 255) sx -= 256;
	
				drawgfx(tmpbitmap,Machine.gfx[1],
						charcode, 0,
						flipx,flipy,sx,sy,
						0,TRANSPARENCY_NONE,0);
			}
		}
	
		/* copy the character mapped graphics */
		copybitmap(bitmap,tmpbitmap,0,0,8*3,0,&bigfield_area,TRANSPARENCY_NONE,0);
	
		/* Draw our motion objects */
		for (obj=0;obj<16;obj++)
		{
			int charcode;
			int flipx,flipy;
			int sx,sy;
			int shade = 0;
	
			sy = 255 - spriteram.read(obj*2 + 1);
			if (sy == 255) continue;
	
			charcode = spriteram.read(obj*2)& 0x3f;
			flipx = (spriteram.read(obj*2)& 0x40);
			flipy = (spriteram.read(obj*2)& 0x80);
			sx = spriteram.read(obj*2 + 0x20)+ 8*3;
	
			/* Note on Atari Soccer: */
			/* There are 3 sets of 2 bits each, where the 2 bits represent */
			/* black, dk grey, grey and white. I think the 3 sets determine the */
			/* color of each bit in the sprite, but I haven't implemented it that way. */
			if (atarifb_game == 4)
			{
				shade = ((spriteram.read(obj*2+1 + 0x20)) & 0x07);
	
				drawgfx(bitmap,Machine.gfx[sprite_bank+1],
					charcode, shade,
					flipx,flipy,sx,sy,
					&bigfield_area,TRANSPARENCY_PEN,0);
	
				shade = ((spriteram.read(obj*2+1 + 0x20)) & 0x08) >> 3;
			}
	
			drawgfx(bitmap,Machine.gfx[sprite_bank],
					charcode, shade,
					flipx,flipy,sx,sy,
					&bigfield_area,TRANSPARENCY_PEN,0);
	
			/* If this isn't soccer, handle the multiplexed sprites */
			if (atarifb_game != 4)
			{
				/* The down markers are multiplexed by altering the y location during */
				/* mid-screen. We'll fake it by essentially doing the same thing here. */
				if ((charcode == 0x11) && (sy == 0x07))
				{
					sy = 0xf1; /* When multiplexed, it's 0x10...why? */
					drawgfx(bitmap,Machine.gfx[sprite_bank],
						charcode, 0,
						flipx,flipy,sx,sy,
						&bigfield_area,TRANSPARENCY_PEN,0);
				}
			}
		}
	
	/* If this isn't Soccer, print the plays at the top of the screen */
	if (atarifb_game != 4)
	{
		int x;
		char buf1[25], buf2[25];
	extern int atarifb_lamp1, atarifb_lamp2;
	
		switch (atarifb_game)
		{
			case 0x01: /* 2-player football */
				switch (atarifb_lamp1)
				{
					case 0x00:
						sprintf (buf1, "                    ");
						break;
					case 0x01:
						sprintf (buf1, "SWEEP               ");
						break;
					case 0x02:
						sprintf (buf1, "KEEPER              ");
						break;
					case 0x04:
						sprintf (buf1, "BOMB                ");
						break;
					case 0x08:
						sprintf (buf1, "DOWN & OUT          ");
						break;
				}
				switch (atarifb_lamp2)
				{
					case 0x00:
						sprintf (buf2, "                    ");
						break;
					case 0x01:
						sprintf (buf2, "SWEEP               ");
						break;
					case 0x02:
						sprintf (buf2, "KEEPER              ");
						break;
					case 0x04:
						sprintf (buf2, "BOMB                ");
						break;
					case 0x08:
						sprintf (buf2, "DOWN & OUT          ");
						break;
				}
				break;
			case 0x02: /* 4-player football */
				switch (atarifb_lamp1 & 0x1f)
				{
					case 0x01:
						sprintf (buf1, "SLANT OUT           ");
						break;
					case 0x02:
						sprintf (buf1, "SLANT IN            ");
						break;
					case 0x04:
						sprintf (buf1, "BOMB                ");
						break;
					case 0x08:
						sprintf (buf1, "DOWN & OUT          ");
						break;
					case 0x10:
						sprintf (buf1, "KICK                ");
						break;
					default:
						sprintf (buf1, "                    ");
						break;
				}
				switch (atarifb_lamp2 & 0x1f)
				{
					case 0x01:
						sprintf (buf2, "SLANT OUT           ");
						break;
					case 0x02:
						sprintf (buf2, "SLANT IN            ");
						break;
					case 0x04:
						sprintf (buf2, "BOMB                ");
						break;
					case 0x08:
						sprintf (buf2, "DOWN & OUT          ");
						break;
					case 0x10:
						sprintf (buf2, "KICK                ");
						break;
					default:
						sprintf (buf2, "                    ");
						break;
				}
				break;
			case 0x03: /* 2-player baseball */
				switch (atarifb_lamp1 & 0x0f)
				{
					case 0x01:
						sprintf (buf1, "RT SWING/FASTBALL   ");
						break;
					case 0x02:
						sprintf (buf1, "LT SWING/CHANGE-UP  ");
						break;
					case 0x04:
						sprintf (buf1, "RT BUNT/CURVE BALL  ");
						break;
					case 0x08:
						sprintf (buf1, "LT BUNT/KNUCKLE BALL");
						break;
					default:
						sprintf (buf1, "                    ");
						break;
				}
				switch (atarifb_lamp2 & 0x0f)
				{
					case 0x01:
						sprintf (buf2, "RT SWING/FASTBALL   ");
						break;
					case 0x02:
						sprintf (buf2, "LT SWING/CHANGE-UP  ");
						break;
					case 0x04:
						sprintf (buf2, "RT BUNT/CURVE BALL  ");
						break;
					case 0x08:
						sprintf (buf2, "LT BUNT/KNUCKLE BALL");
						break;
					default:
						sprintf (buf2, "                    ");
						break;
				}
				break;
			default:
				sprintf (buf1, "                    ");
				sprintf (buf2, "                    ");
				break;
		}
		for (x = 0;x < 20;x++)
				drawgfx(bitmap,Machine.uifont,buf1[x],UI_COLOR_NORMAL,0,0,6*x + 24*8,0,0,TRANSPARENCY_NONE,0);
	
		for (x = 0;x < 20;x++)
				drawgfx(bitmap,Machine.uifont,buf2[x],UI_COLOR_NORMAL,0,0,6*x,0,0,TRANSPARENCY_NONE,0);
	}
	} };
}
