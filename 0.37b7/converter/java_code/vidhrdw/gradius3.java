/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class gradius3
{
	
	
	#define TOTAL_CHARS 0x1000
	#define TOTAL_SPRITES 0x4000
	
	UBytePtr gradius3_gfxram;
	int gradius3_priority;
	static int layer_colorbase[3],sprite_colorbase;
	static int dirtygfx;
	static UBytePtr dirtychar;
	
	
	
	/***************************************************************************
	
	  Callbacks for the K052109
	
	***************************************************************************/
	
	public static K052109_callbackProcPtr gradius3_tile_callback = new K052109_callbackProcPtr() { public void handler(int layer,int bank,int[] code,int[] color) 
	{
		/* (color & 0x02) is flip y handled internally by the 052109 */
		*code |= ((*color & 0x01) << 8) | ((*color & 0x1c) << 7);
		*color = layer_colorbase[layer] + ((*color & 0xe0) >> 5);
	} };
	
	
	
	/***************************************************************************
	
	  Callbacks for the K051960
	
	***************************************************************************/
	
	public static K051960_callbackProcPtr gradius3_sprite_callback = new K051960_callbackProcPtr() { public void handler(int[] code,int[] color,int[] priority) 
	{
		#define L0 0xaa
		#define L1 0xcc
		#define L2 0xf0
		static int primask[2][4] =
		{
			{ L0|L2, L0, L0|L2, L0|L1|L2 },
			{ L1|L2, L2, 0,     L0|L1|L2 }
		};
		int pri = ((*color & 0x60) >> 5);
		if (gradius3_priority == 0) *priority_mask = primask[0][pri];
		else *priority_mask = primask[1][pri];
	
		*code |= (*color & 0x01) << 13;
		*color = sprite_colorbase + ((*color & 0x1e) >> 1);
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr gradius3_vh_start = new VhStartPtr() { public int handler() 
	{
		int i;
		static GfxLayout spritelayout = new GfxLayout
		(
			8,8,
			TOTAL_SPRITES,
			4,
			new int[] { 0, 1, 2, 3 },
			new int[] { 2*4, 3*4, 0*4, 1*4, 6*4, 7*4, 4*4, 5*4,
					32*8+2*4, 32*8+3*4, 32*8+0*4, 32*8+1*4, 32*8+6*4, 32*8+7*4, 32*8+4*4, 32*8+5*4 },
			new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32,
					64*8+0*32, 64*8+1*32, 64*8+2*32, 64*8+3*32, 64*8+4*32, 64*8+5*32, 64*8+6*32, 64*8+7*32 },
			128*8
		);
	
	
		layer_colorbase[0] = 0;
		layer_colorbase[1] = 32;
		layer_colorbase[2] = 48;
		sprite_colorbase = 16;
		if (K052109_vh_start(REGION_GFX1,NORMAL_PLANE_ORDER,gradius3_tile_callback))
			return 1;
		if (K051960_vh_start(REGION_GFX2,REVERSE_PLANE_ORDER,gradius3_sprite_callback))
		{
			K052109_vh_stop();
			return 1;
		}
	
		/* re-decode the sprites because the ROMs are connected to the custom IC differently
		   from how they are connected to the CPU. */
		for (i = 0;i < TOTAL_SPRITES;i++)
			decodechar(Machine.gfx[1],i,memory_region(REGION_GFX2),&spritelayout);
	
		if (!(dirtychar = malloc(TOTAL_CHARS)))
		{
			K052109_vh_stop();
			K051960_vh_stop();
			return 1;
		}
	
		memset(dirtychar,1,TOTAL_CHARS);
	
		return 0;
	}
	
	public static VhStopPtr gradius3_vh_stop = new VhStopPtr() { public void handler() 
	{
		K052109_vh_stop();
		K051960_vh_stop();
		free(dirtychar);
		dirtychar = 0;
	} };
	
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static ReadHandlerPtr gradius3_gfxrom_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UBytePtr gfxdata = memory_region(REGION_GFX2);
	
		return (gfxdata[offset+1] << 8) | gfxdata[offset];
	} };
	
	public static ReadHandlerPtr gradius3_gfxram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return READ_WORD(&gradius3_gfxram[offset]);
	} };
	
	public static WriteHandlerPtr gradius3_gfxram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = READ_WORD(&gradius3_gfxram[offset]);
		int newword = COMBINE_WORD(oldword,data);
	
		if (oldword != newword)
		{
			dirtygfx = 1;
			dirtychar[offset / 32] = 1;
			WRITE_WORD(&gradius3_gfxram[offset],newword);
		}
	} };
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	public static VhUpdatePtr gradius3_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		static GfxLayout charlayout = new GfxLayout
		(
			8,8,
			TOTAL_CHARS,
			4,
			new int[] { 0, 1, 2, 3 },
	#ifdef LSB_FIRST
			new int[] { 2*4, 3*4, 0*4, 1*4, 6*4, 7*4, 4*4, 5*4 },
	#else
			new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4 },
	#endif
			new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
			32*8
		);
	
		/* TODO: this kludge enforces the char banks. For some reason, they don't work otherwise. */
		K052109_w(0x1d80,0x10);
		K052109_w(0x1f00,0x32);
	
		if (dirtygfx != 0)
		{
			int i;
	
			dirtygfx = 0;
	
			for (i = 0;i < TOTAL_CHARS;i++)
			{
				if (dirtychar[i])
				{
					dirtychar[i] = 0;
					decodechar(Machine.gfx[0],i,gradius3_gfxram,&charlayout);
				}
			}
	
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	
		K052109_tilemap_update();
	
		palette_init_used_colors();
		K051960_mark_sprites_colors();
		if (palette_recalc())
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		fillbitmap(priority_bitmap,0,NULL);
		if (gradius3_priority == 0)
		{
			K052109_tilemap_draw(bitmap,1,TILEMAP_IGNORE_TRANSPARENCY|(2<<16));
			K052109_tilemap_draw(bitmap,2,4<<16);
			K052109_tilemap_draw(bitmap,0,1<<16);
		}
		else
		{
			K052109_tilemap_draw(bitmap,0,TILEMAP_IGNORE_TRANSPARENCY|(1<<16));
			K052109_tilemap_draw(bitmap,1,2<<16);
			K052109_tilemap_draw(bitmap,2,4<<16);
		}
	
		K051960_sprites_draw(bitmap,-1,-1);
	}
}
