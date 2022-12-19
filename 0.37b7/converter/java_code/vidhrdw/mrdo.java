/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class mrdo
{
	
	
	UBytePtr mrdo_bgvideoram,*mrdo_fgvideoram;
	static struct tilemap *bg_tilemap,*fg_tilemap;
	static int flipscreen;
	
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Mr. Do! has two 32 bytes palette PROM and a 32 bytes sprite color lookup
	  table PROM.
	  The palette PROMs are connected to the RGB output this way:
	
	  U2:
	  bit 7 -- unused
	        -- unused
	        -- 100 ohm resistor  -- BLUE
	        --  75 ohm resistor  -- BLUE
	        -- 100 ohm resistor  -- GREEN
	        --  75 ohm resistor  -- GREEN
	        -- 100 ohm resistor  -- RED
	  bit 0 --  75 ohm resistor  -- RED
	
	  T2:
	  bit 7 -- unused
	        -- unused
	        -- 150 ohm resistor  -- BLUE
	        -- 120 ohm resistor  -- BLUE
	        -- 150 ohm resistor  -- GREEN
	        -- 120 ohm resistor  -- GREEN
	        -- 150 ohm resistor  -- RED
	  bit 0 -- 120 ohm resistor  -- RED
	
	***************************************************************************/
	public static VhConvertColorPromPtr mrdo_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
		for (i = 0;i < 256;i++)
		{
			int a1,a2;
			int bit0,bit1,bit2,bit3;
	
	
			a1 = ((i >> 3) & 0x1c) + (i & 0x03) + 32;
			a2 = ((i >> 0) & 0x1c) + (i & 0x03);
	
			bit0 = (color_prom.read(a1)>> 1) & 0x01;
			bit1 = (color_prom.read(a1)>> 0) & 0x01;
			bit2 = (color_prom.read(a2)>> 1) & 0x01;
			bit3 = (color_prom.read(a2)>> 0) & 0x01;
			*(palette++) = 0x2c * bit0 + 0x37 * bit1 + 0x43 * bit2 + 0x59 * bit3;
			bit0 = (color_prom.read(a1)>> 3) & 0x01;
			bit1 = (color_prom.read(a1)>> 2) & 0x01;
			bit2 = (color_prom.read(a2)>> 3) & 0x01;
			bit3 = (color_prom.read(a2)>> 2) & 0x01;
			*(palette++) = 0x2c * bit0 + 0x37 * bit1 + 0x43 * bit2 + 0x59 * bit3;
			bit0 = (color_prom.read(a1)>> 5) & 0x01;
			bit1 = (color_prom.read(a1)>> 4) & 0x01;
			bit2 = (color_prom.read(a2)>> 5) & 0x01;
			bit3 = (color_prom.read(a2)>> 4) & 0x01;
			*(palette++) = 0x2c * bit0 + 0x37 * bit1 + 0x43 * bit2 + 0x59 * bit3;
		}
	
		color_prom += 64;
	
		/* sprites */
		for (i = 0;i < TOTAL_COLORS(2);i++)
		{
			int bits;
	
			if (i < 32)
				bits = color_prom.read(i)& 0x0f;		/* low 4 bits are for sprite color n */
			else
				bits = color_prom.read(i & 0x1f)>> 4;	/* high 4 bits are for sprite color n + 8 */
	
			COLOR(2,i) = bits + ((bits & 0x0c) << 3);
		}
	} };
	
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoPtr get_bg_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		unsigned char attr = mrdo_bgvideoram[tile_index];
		SET_TILE_INFO(1,mrdo_bgvideoram[tile_index+0x400] + ((attr & 0x80) << 1),attr & 0x3f)
		tile_info.flags = TILE_SPLIT((attr & 0x40) >> 6);
	} };
	
	public static GetTileInfoPtr get_fg_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		unsigned char attr = mrdo_fgvideoram[tile_index];
		SET_TILE_INFO(0,mrdo_fgvideoram[tile_index+0x400] + ((attr & 0x80) << 1),attr & 0x3f)
		tile_info.flags = TILE_SPLIT((attr & 0x40) >> 6);
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr mrdo_vh_start = new VhStartPtr() { public int handler() 
	{
		bg_tilemap = tilemap_create(get_bg_tile_info,tilemap_scan_rows,TILEMAP_SPLIT,8,8,32,32);
		fg_tilemap = tilemap_create(get_fg_tile_info,tilemap_scan_rows,TILEMAP_SPLIT,8,8,32,32);
	
		if (!bg_tilemap || !fg_tilemap)
			return 1;
	
		bg_tilemap.transmask[0] = 0x01; /* split type 0 has pen 1 transparent in front half */
		bg_tilemap.transmask[1] = 0x00; /* split type 1 is totally opaque in front half */
		fg_tilemap.transmask[0] = 0x01; /* split type 0 has pen 1 transparent in front half */
		fg_tilemap.transmask[1] = 0x00; /* split type 1 is totally opaque in front half */
	
		return 0;
	} };
	
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr mrdo_bgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (mrdo_bgvideoram[offset] != data)
		{
			mrdo_bgvideoram[offset] = data;
			tilemap_mark_tile_dirty(bg_tilemap,offset & 0x3ff);
		}
	} };
	
	public static WriteHandlerPtr mrdo_fgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (mrdo_fgvideoram[offset] != data)
		{
			mrdo_fgvideoram[offset] = data;
			tilemap_mark_tile_dirty(fg_tilemap,offset & 0x3ff);
		}
	} };
	
	
	public static WriteHandlerPtr mrdo_scrollx_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		tilemap_set_scrollx(bg_tilemap,0,data);
	} };
	
	public static WriteHandlerPtr mrdo_scrolly_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		tilemap_set_scrolly(bg_tilemap,0,data);
	} };
	
	
	public static WriteHandlerPtr mrdo_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* bits 1-3 control the playfield priority, but they are not used by */
		/* Mr. Do! so we don't emulate them */
	
		flipscreen = data & 0x01;
		tilemap_set_flip(ALL_TILEMAPS,flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
	} };
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	static void draw_sprites(struct osd_bitmap *bitmap)
	{
		int offs;
	
	
		for (offs = spriteram_size - 4;offs >= 0;offs -= 4)
		{
			if (spriteram.read(offs + 1)!= 0)
			{
				drawgfx(bitmap,Machine.gfx[2],
						spriteram.read(offs),spriteram.read(offs + 2)& 0x0f,
						spriteram.read(offs + 2)& 0x10,spriteram.read(offs + 2)& 0x20,
						spriteram.read(offs + 3),256 - spriteram.read(offs + 1),
						&Machine.visible_area,TRANSPARENCY_PEN,0);
			}
		}
	}
	
	public static VhUpdatePtr mrdo_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_update(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		fillbitmap(bitmap,Machine.pens[0],&Machine.visible_area);
		tilemap_draw(bitmap,bg_tilemap,TILEMAP_FRONT);
		tilemap_draw(bitmap,fg_tilemap,TILEMAP_FRONT);
		draw_sprites(bitmap);
	} };
}
