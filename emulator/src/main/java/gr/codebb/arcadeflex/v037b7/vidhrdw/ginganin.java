/**************************************************************************

							Ginga NinkyouDen
						    (C) 1987 Jaleco

				    driver by Luca Elia (eliavit@unina.it)


Note:	if MAME_DEBUG is defined, pressing Z with:

		Q		shows background
		W		shows foreground
		E		shows frontmost (text) layer
		A		shows sprites

		Keys can be used togheter!


[Screen]
 	Visible Size:		256H x 240V
	Dynamic Colors:		256 x 4
	Color Space:		16R x 16G x 16B

[Scrolling layers]
	Format (all layers):	Offset:		0x400    0x000
							Bit:		fedc---- --------	Color
										----ba98 76543210	Code

	[Background]
		Size:				8192 x 512	(static: stored in ROM)
		Scrolling:			X,Y			(registers: $60006.w, $60004.w)
		Tiles Size:			16 x 16
		Tiles Number:		$400
		Colors:				$300-$3ff

	[Foreground]
		Size:				4096 x 512
		Scrolling:			X,Y			(registers: $60002.w, $60000.w)
		Tiles Size:			16 x 16
		Tiles Number:		$400
		Colors:				$200-$2ff

	[Frontmost]
		Size:				256 x 256
		Scrolling:			-
		Tiles Size:			8 x 8
		Tiles Number:		$200
		Colors:				$000-$0ff


[Sprites]
	On Screen:			256
	In ROM:				$a00
	Colors:				$100-$1ff
	Format:				See Below


**************************************************************************/
/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v036.platform.video.osd_clearbitmap;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_w;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v036.platform.video.osd_clearbitmap;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_w;
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v036.platform.video.osd_clearbitmap;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_w;
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v036.platform.video.osd_clearbitmap;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_w;
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v036.platform.video.osd_clearbitmap;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_w;
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v036.platform.video.osd_clearbitmap;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_w;
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v036.platform.video.osd_clearbitmap;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_w;
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v036.platform.video.osd_clearbitmap;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_w;
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v036.platform.video.osd_clearbitmap;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_w;
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v036.platform.video.osd_clearbitmap;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_w;
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v036.platform.video.osd_clearbitmap;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_w;
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;


public class ginganin
{
	
	/* Variables only used here */
	static tilemap bg_tilemap, fg_tilemap, tx_tilemap;
	static int layers_ctrl, flipscreen;
	
	/* Variables that driver has access to */
	public static UBytePtr ginganin_fgram=new UBytePtr(), ginganin_txtram=new UBytePtr(), ginganin_vregs=new UBytePtr();
	
	/* Variables defined in drivers */
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	
	/* Background - Resides in ROM */
	
	static int BG_GFX = (0);
	static int BG_NX  = (16*32);
	static int BG_NY  = (16*2);
	
	public static WriteHandlerPtr get_bg_tile_info = new WriteHandlerPtr() { 
            public void handler(int col, int row) 
            {
		int tile_index = row + col * BG_NY;
                int code = new UBytePtr(memory_region(REGION_GFX5)).read(tile_index*2 + 0) * 256 + new UBytePtr(memory_region(REGION_GFX5)).read(tile_index*2 + 1);
                SET_TILE_INFO(BG_GFX, code, code >> 12);
            } 
        };
	
	
	
	
	
	/* Foreground - Resides in RAM */
	
	static int FG_GFX = (1);
	static int FG_NX  = (16*16);
	static int FG_NY  = (16*2);
	
	public static WriteHandlerPtr get_fg_tile_info  = new WriteHandlerPtr() { 
            public void handler(int col, int row) 
            {
		int tile_index = row + col * FG_NY;
                int code = ginganin_fgram.READ_WORD(tile_index*2);
                SET_TILE_INFO(FG_GFX, code, code >> 12);
            } 
        };
	
	public static WriteHandlerPtr ginganin_fgram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int old_data, new_data;
	
		old_data  = ginganin_fgram.READ_WORD(offset);
		COMBINE_WORD_MEM(ginganin_fgram,offset,data);
		new_data  = ginganin_fgram.READ_WORD(offset);
	
		if (old_data != new_data)
			tilemap_mark_tile_dirty(fg_tilemap,offset/2,0);
	} };
	
	
	
	
	/* Frontmost (text) Layer - Resides in RAM */
	
	static int TXT_GFX = (2);
	static int TXT_NX  = (32);
	static int TXT_NY  = (32);
	
	public static WriteHandlerPtr get_txt_tile_info = new WriteHandlerPtr() {public void handler(int row, int col)
	{
		int tile_index = row * TXT_NX + col;
                int code = ginganin_txtram.READ_WORD(tile_index*2);
                SET_TILE_INFO(TXT_GFX, code, code >> 12);
	} };
	
	public static WriteHandlerPtr ginganin_txtram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	int old_data, new_data;
	
		old_data  = ginganin_txtram.READ_WORD(offset);
		COMBINE_WORD_MEM(ginganin_txtram,offset,data);
		new_data  = ginganin_txtram.READ_WORD(offset);
	
		if (old_data != new_data)
			tilemap_mark_tile_dirty(tx_tilemap,offset/2,0);
	} };
	
	
	
	
	
	public static VhStartPtr ginganin_vh_start = new VhStartPtr() { public int handler() 
	{
		bg_tilemap = tilemap_create(get_bg_tile_info,
								TILEMAP_OPAQUE,
								16,16,
								BG_NX,BG_NY );

	fg_tilemap = tilemap_create(get_fg_tile_info,
								TILEMAP_TRANSPARENT,
								16,16,
								FG_NX,FG_NY );

	tx_tilemap = tilemap_create(get_txt_tile_info,
								TILEMAP_TRANSPARENT,
								8,8,
								TXT_NX,TXT_NY );

	if (fg_tilemap!=null && bg_tilemap!=null && tx_tilemap!=null)
	{
		tilemap_set_scroll_rows(bg_tilemap,1);
		tilemap_set_scroll_cols(bg_tilemap,1);

		tilemap_set_scroll_rows(fg_tilemap,1);
		tilemap_set_scroll_cols(fg_tilemap,1);
		fg_tilemap.transparent_pen = 15;

		tilemap_set_scroll_rows(tx_tilemap,0);
		tilemap_set_scroll_cols(tx_tilemap,0);
		tx_tilemap.transparent_pen = 15;

		return 0;
	}
	else return 1;
	} };
	
	
	
	
	
	public static WriteHandlerPtr ginganin_vregs_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int new_data;
	
		COMBINE_WORD_MEM(ginganin_vregs,offset,data);
		new_data  = ginganin_vregs.READ_WORD(offset);
	
		switch (offset)
	{
		case 0x0 : { tilemap_set_scrolly(fg_tilemap, 0, new_data); } break;
		case 0x2 : { tilemap_set_scrollx(fg_tilemap, 0, new_data); } break;
		case 0x4 : { tilemap_set_scrolly(bg_tilemap, 0, new_data); } break;
		case 0x6 : { tilemap_set_scrollx(bg_tilemap, 0, new_data); } break;
		case 0x8 : { layers_ctrl = new_data; } break;
//		case 0xa : break;
		case 0xc : { flipscreen = (new_data & 1)!=0?0:1;	tilemap_set_flip(ALL_TILEMAPS,flipscreen!=0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0); } break;
		case 0xe : { soundlatch_w.handler(0,new_data);
					 cpu_cause_interrupt(1,M6809_INT_NMI);
				   } break;

		default  : {logerror("CPU #0 PC %06X : Warning, videoreg %04X <- %04X\n",cpu_get_pc(),offset,data);}
	}
	} };
	
	
	
	
	
	
	/* --------------------------[ Sprites Format ]----------------------------
	
	Offset:			Values:			Format:
	
	0000.w			y position		fedc ba9- ---- ----		unused
									---- ---8 ---- ----		subtract 256
									---- ---- 7654 3210		position
	
	0002.w			x position		See above
	
	0004.w			code			f--- ---- ---- ----		y flip
									-e-- ---- ---- ----		x flip
									--dc ---- ---- ----		unused?
									---- ba98 7654 3210		code
	
	0006.w			colour			fedc ---- ---- ----		colour code
									---- ba98 7654 3210		unused?
	
	------------------------------------------------------------------------ */
	
	static void draw_sprites(osd_bitmap bitmap)
	{
	int offs;

	for ( offs = 0 ; offs < spriteram_size[0] ; offs += 8 )
	{
		int	y		=	spriteram.READ_WORD(offs + 0);
		int	x		=	spriteram.READ_WORD(offs + 2);
		int	code	=	spriteram.READ_WORD(offs + 4);
		int	attr	=	spriteram.READ_WORD(offs + 6);
		int	flipx	=	code & 0x4000;
		int	flipy	=	code & 0x8000;

		x = (x & 0xFF) - (x & 0x100);
		y = (y & 0xFF) - (y & 0x100);

		if (flipscreen!=0)
		{
			x = 240 - x;		y = 240 - y;
			flipx = flipx!=0?0:1;		flipy = flipy!=0?0:1;
		}

		drawgfx(bitmap,Machine.gfx[3],
				code & 0x3fff,
				attr >> 12,
				flipx, flipy,
				x,y,
				Machine.drv.visible_area,TRANSPARENCY_PEN,15);

            }
	}
	
        public static VhUpdatePtr ginganin_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
            int i, offs;
            int layers_ctrl1;

            layers_ctrl1 = layers_ctrl;
	
            tilemap_update(ALL_TILEMAPS);

            palette_init_used_colors();
	
	
            /* Palette stuff: visible sprites */
	
            int color;
            int[] colmask=new int[16];

            int xmin = Machine.drv.visible_area.min_x - 16 - 1;
            int xmax = Machine.drv.visible_area.max_x;
            int ymin = Machine.drv.visible_area.min_y - 16 - 1;
            int ymax = Machine.drv.visible_area.max_y;

            int nmax				=	Machine.gfx[3].total_elements;
            int[] pen_usage	=	Machine.gfx[3].pen_usage;
            int color_codes_start	=	Machine.drv.gfxdecodeinfo[3].color_codes_start;

            for (color = 0 ; color < 16 ; color++) colmask[color] = 0;

            for (offs = 0 ; offs < spriteram_size[0] ; offs += 8)
            {
                int x,y,code;

		y	=	spriteram.READ_WORD(offs + 0);
		y	=	(y & 0xff) - (y & 0x100);
		if ((y < ymin) || (y > ymax))	continue;

		x	=	spriteram.READ_WORD(offs + 2);
		x	=	(x & 0xff) - (x & 0x100);
		if ((x < xmin) || (x > xmax))	continue;

		code	=	(spriteram.READ_WORD(offs + 4) & 0x3fff)% nmax;
		color	=	spriteram.READ_WORD(offs + 6) >> 12;

		colmask[color] |= pen_usage[code];
            }

            for (color = 0; color < 16; color++)
            {
                    if (colmask[color]!=0)
                    {
                            for (i = 0; i < 16; i++)
                                    if ((colmask[color] & (1 << i)) != 0)
                                            palette_used_colors.write(16 * color + i + color_codes_start, PALETTE_COLOR_USED);
                    }
            }



            if (palette_recalc()!=null)	tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);

            tilemap_render(ALL_TILEMAPS);

            if ((layers_ctrl1 & 1)!=0)	tilemap_draw(bitmap, bg_tilemap,  0);
            else					osd_clearbitmap(Machine.scrbitmap);

            if ((layers_ctrl1 & 2)!=0)	tilemap_draw(bitmap, fg_tilemap,  0);
            if ((layers_ctrl1 & 8)!=0)	draw_sprites(bitmap);
            if ((layers_ctrl1 & 4)!=0)	tilemap_draw(bitmap, tx_tilemap, 0);


        }
       };
}