/***************************************************************************

  vidhrdw/generic.c

  Some general purpose functions used by many video drivers.

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package vidhrdw;

import static arcadeflex.libc.*;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static vidhrdw.generic.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.mame.*;

public class generic
{
	
	
	
	unsigned char *videoram;
	int videoram_size;
	unsigned char *colorram;
	unsigned char *spriteram;	/* not used in this module... */
	unsigned char *spriteram_2;	/* ... */
	unsigned char *spriteram_3;	/* ... */
	unsigned char *buffered_spriteram;	/* not used in this module... */
	unsigned char *buffered_spriteram_2;	/* ... */
	int spriteram_size;	/* ... here just for convenience */
	int spriteram_2_size;	/* ... here just for convenience */
	int spriteram_3_size;	/* ... here just for convenience */
	unsigned char *flip_screen;	/* ... */
	unsigned char *flip_screen_x;	/* ... */
	unsigned char *flip_screen_y;	/* ... */
	unsigned char *dirtybuffer;
	struct osd_bitmap *tmpbitmap;
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	public static VhStartPtr generic_vh_start = new VhStartPtr() { public int handler() 
	{
		dirtybuffer = 0;
		tmpbitmap = 0;
	
		if (videoram_size == 0)
		{
	if (errorlog != 0) fprintf(errorlog,"Error: generic_vh_start() called but videoram_size not initialized\n");
			return 1;
		}
	
		if ((dirtybuffer = malloc(videoram_size)) == 0)
			return 1;
		memset(dirtybuffer,1,videoram_size);
	
		if ((tmpbitmap = osd_new_bitmap(Machine.drv.screen_width,Machine.drv.screen_height,Machine.scrbitmap.depth)) == 0)
		{
			free(dirtybuffer);
			return 1;
		}
	
		return 0;
	} };
	
	
	public static VhStartPtr generic_bitmapped_vh_start = new VhStartPtr() { public int handler() 
	{
		if ((tmpbitmap = osd_new_bitmap(Machine.drv.screen_width,Machine.drv.screen_height,Machine.scrbitmap.depth)) == 0)
		{
			return 1;
		}
	
		return 0;
	} };
	
	
	/***************************************************************************
	
	  Stop the video hardware emulation.
	
	***************************************************************************/
	public static VhStopPtr generic_vh_stop = new VhStopPtr() { public void handler() 
	{
		free(dirtybuffer);
		osd_free_bitmap(tmpbitmap);
	
		dirtybuffer = 0;
		tmpbitmap = 0;
	} };
	
	public static VhStopPtr generic_bitmapped_vh_stop = new VhStopPtr() { public void handler() 
	{
		osd_free_bitmap(tmpbitmap);
	
		tmpbitmap = 0;
	} };
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  To be used by bitmapped games not using sprites.
	
	***************************************************************************/
	public static VhUpdatePtr generic_bitmapped_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		if (full_refresh != 0)
			copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	} };
	
	
	public static ReadHandlerPtr videoram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return videoram[offset];
	} };
	
	public static ReadHandlerPtr colorram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return colorram[offset];
	} };
	
	public static WriteHandlerPtr videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (videoram[offset] != data)
		{
			dirtybuffer[offset] = 1;
	
			videoram[offset] = data;
		}
	} };
	
	public static WriteHandlerPtr colorram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (colorram[offset] != data)
		{
			dirtybuffer[offset] = 1;
	
			colorram[offset] = data;
		}
	} };
	
	
	
	public static ReadHandlerPtr spriteram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return spriteram[offset];
	} };
	
	public static WriteHandlerPtr spriteram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		spriteram[offset] = data;
	} };
	
	public static ReadHandlerPtr spriteram_2_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return spriteram_2[offset];
	} };
	
	public static WriteHandlerPtr spriteram_2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		spriteram_2[offset] = data;
	} };
	
	/* Mish:  171099
	
		'Buffered spriteram' is where the graphics hardware draws the sprites
	from private ram that the main CPU cannot access.  The main CPU typically
	prepares sprites for the next frame in it's own sprite ram as the graphics
	hardware renders sprites for the current frame from private ram.  Main CPU
	sprite ram is usually copied across to private ram by setting some flag
	in the VBL interrupt routine.
	
		The reason for this is to avoid sprite flicker or lag - if a game
	is unable to prepare sprite ram within a frame (for example, lots of sprites
	on screen) then it doesn't trigger the buffering hardware - instead the
	graphics hardware will use the sprites from the last frame. An example is
	Dark Seal - the buffer flag is only written to if the CPU is idle at the time
	of the VBL interrupt.  If the buffering is not emulated the sprites flicker
	at busy scenes.
	
		Some games seem to use buffering because of hardware constraints -
	Capcom games (Cps1, Last Duel, etc) render spriteram _1 frame ahead_ and
	buffer this spriteram at the end of a frame, so the _next_ frame must be drawn
	from the buffer.  Presumably the graphics hardware and the main cpu cannot
	share the same spriteram for whatever reason.
	
		Sprite buffering & Mame:
	
		To use sprite buffering in a driver use VIDEO_BUFFERS_SPRITERAM in the
	machine driver.  This will automatically create an area for buffered spriteram
	equal to the size of normal spriteram.
	
		Spriteram size _must_ be declared in the memory map:
	
		{ 0x120000, 0x1207ff, MWA_BANK2, &spriteram, &spriteram_size },
	
		Then the video driver must draw the sprites from the buffered_spriteram
	pointer.  The function buffer_spriteram_w() is used to simulate hardware
	which buffers the spriteram from a memory location write.  The function
	buffer_spriteram(unsigned char *ptr, int length) can be used where
	more control is needed over what is buffered.
	
		For examples see darkseal.c, contra.c, lastduel.c, bionicc.c etc.
	
	*/
	
	public static WriteHandlerPtr buffer_spriteram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		memcpy(buffered_spriteram,spriteram,spriteram_size);
	} };
	
	public static WriteHandlerPtr buffer_spriteram_2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		memcpy(buffered_spriteram_2,spriteram_2,spriteram_2_size);
	} };
	
	void buffer_spriteram(unsigned char *ptr,int length)
	{
		memcpy(buffered_spriteram,ptr,length);
	}
	
	void buffer_spriteram_2(unsigned char *ptr,int length)
	{
		memcpy(buffered_spriteram_2,ptr,length);
	}
}
