/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class wc90b
{
	
	UBytePtr wc90b_shared;
	
	UBytePtr wc90b_tile_colorram, *wc90b_tile_videoram;
	UBytePtr wc90b_tile_colorram2, *wc90b_tile_videoram2;
	UBytePtr wc90b_scroll1xlo, *wc90b_scroll1xhi;
	UBytePtr wc90b_scroll2xlo, *wc90b_scroll2xhi;
	UBytePtr wc90b_scroll1ylo, *wc90b_scroll1yhi;
	UBytePtr wc90b_scroll2ylo, *wc90b_scroll2yhi;
	
	size_t wc90b_tile_videoram_size;
	size_t wc90b_tile_videoram_size2;
	
	static UBytePtr dirtybuffer1 = 0, *dirtybuffer2 = 0;
	static struct osd_bitmap *tmpbitmap1 = 0,*tmpbitmap2 = 0;
	
	public static VhStartPtr wc90b_vh_start = new VhStartPtr() { public int handler()  {
	
		if ( generic_vh_start() )
			return 1;
	
		if ( ( dirtybuffer1 = malloc( wc90b_tile_videoram_size ) ) == 0 ) {
			return 1;
		}
	
		memset( dirtybuffer1, 1, wc90b_tile_videoram_size );
	
		if ( ( tmpbitmap1 = bitmap_alloc(4*Machine.drv.screen_width,2*Machine.drv.screen_height) ) == 0 ){
			free( dirtybuffer1 );
			generic_vh_stop();
			return 1;
		}
	
		if ( ( dirtybuffer2 = malloc( wc90b_tile_videoram_size2 ) ) == 0 ) {
			bitmap_free(tmpbitmap1);
			free(dirtybuffer1);
			generic_vh_stop();
			return 1;
		}
	
		memset( dirtybuffer2, 1, wc90b_tile_videoram_size2 );
	
		if ( ( tmpbitmap2 = bitmap_alloc( 4*Machine.drv.screen_width,2*Machine.drv.screen_height ) ) == 0 ){
			bitmap_free(tmpbitmap1);
			free(dirtybuffer1);
			free(dirtybuffer2);
			generic_vh_stop();
			return 1;
		}
	
		// Free the generic bitmap and allocate one twice as wide
		free( tmpbitmap );
	
		if ( ( tmpbitmap = bitmap_alloc( 2*Machine.drv.screen_width,Machine.drv.screen_height ) ) == 0 ){
			bitmap_free(tmpbitmap1);
			bitmap_free(tmpbitmap2);
			free(dirtybuffer);
			free(dirtybuffer1);
			free(dirtybuffer2);
			generic_vh_stop();
			return 1;
		}
	
		return 0;
	} };
	
	public static VhStopPtr wc90b_vh_stop = new VhStopPtr() { public void handler()  {
		free( dirtybuffer1 );
		free( dirtybuffer2 );
		bitmap_free( tmpbitmap1 );
		bitmap_free( tmpbitmap2 );
		generic_vh_stop();
	} };
	
	public static ReadHandlerPtr wc90b_tile_videoram_r  = new ReadHandlerPtr() { public int handler(int offset) {
		return wc90b_tile_videoram[offset];
	} };
	
	public static WriteHandlerPtr wc90b_tile_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		if ( wc90b_tile_videoram[offset] != data ) {
			dirtybuffer1[offset] = 1;
			wc90b_tile_videoram[offset] = data;
		}
	} };
	
	public static ReadHandlerPtr wc90b_tile_colorram_r  = new ReadHandlerPtr() { public int handler(int offset) {
		return wc90b_tile_colorram[offset];
	} };
	
	public static WriteHandlerPtr wc90b_tile_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		if ( wc90b_tile_colorram[offset] != data ) {
			dirtybuffer1[offset] = 1;
			wc90b_tile_colorram[offset] = data;
		}
	} };
	
	public static ReadHandlerPtr wc90b_tile_videoram2_r  = new ReadHandlerPtr() { public int handler(int offset) {
		return wc90b_tile_videoram2[offset];
	} };
	
	public static WriteHandlerPtr wc90b_tile_videoram2_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		if ( wc90b_tile_videoram2[offset] != data ) {
			dirtybuffer2[offset] = 1;
			wc90b_tile_videoram2[offset] = data;
		}
	} };
	
	public static ReadHandlerPtr wc90b_tile_colorram2_r  = new ReadHandlerPtr() { public int handler(int offset) {
		return wc90b_tile_colorram2[offset];
	} };
	
	public static WriteHandlerPtr wc90b_tile_colorram2_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		if ( wc90b_tile_colorram2[offset] != data ) {
			dirtybuffer2[offset] = 1;
			wc90b_tile_colorram2[offset] = data;
		}
	} };
	
	public static ReadHandlerPtr wc90b_shared_r  = new ReadHandlerPtr() { public int handler(int offset) {
		return wc90b_shared[offset];
	} };
	
	public static WriteHandlerPtr wc90b_shared_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		wc90b_shared[offset] = data;
	} };
	
	static void wc90b_draw_sprites( struct osd_bitmap *bitmap, int priority ){
	  int offs;
	
	  /* draw all visible sprites of specified priority */
		for ( offs = spriteram_size - 8;offs >= 0;offs -= 8 ){
	
			if ( ( ~( spriteram.read(offs+3)>> 6 ) & 3 ) == priority ) {
	
				if ( spriteram.read(offs+1)> 16 ) { /* visible */
					int code = ( spriteram.read(offs+3)& 0x3f ) << 4;
					int bank = spriteram.read(offs+0);
					int flags = spriteram.read(offs+4);
	
					code += ( bank & 0xf0 ) >> 4;
					code <<= 2;
					code += ( bank & 0x0f ) >> 2;
	
					drawgfx( bitmap,Machine.gfx[ 17 ], code,
							flags >> 4, /* color */
							bank&1, /* flipx */
							bank&2, /* flipy */
							spriteram.read(offs + 2), /* sx */
							240 - spriteram.read(offs + 1), /* sy */
							&Machine.visible_area,TRANSPARENCY_PEN,15 );
				}
			}
		}
	}
	
	public static VhUpdatePtr wc90b_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs, i;
		int scrollx, scrolly;
	
	
	
		/* compute palette usage */
		{
			unsigned short palette_map[4 * 16];
			int tile, gfx, cram;
	
			memset (palette_map, 0, sizeof (palette_map));
	
			for ( offs = wc90b_tile_videoram_size2 - 1; offs >= 0; offs-- ) {
				tile = wc90b_tile_videoram2[offs];
				cram = wc90b_tile_colorram2[offs];
				gfx = 9 + ( cram & 3 ) + ( ( cram >> 1 ) & 4 );
				palette_map[3*16 + (cram >> 4)] |= Machine.gfx[gfx].pen_usage[tile];
			}
			for ( offs = wc90b_tile_videoram_size - 1; offs >= 0; offs-- ) {
				tile = wc90b_tile_videoram[offs];
				cram = wc90b_tile_colorram[offs];
				gfx = 1 + ( cram & 3 ) + ( ( cram >> 1 ) & 4 );
				palette_map[2*16 + (cram >> 4)] |= Machine.gfx[gfx].pen_usage[tile];
			}
			for ( offs = videoram_size[0] - 1; offs >= 0; offs-- ) {
				cram = colorram.read(offs);
				tile = videoram.read(offs)+ ( ( cram & 0x07 ) << 8 );
				palette_map[1*16 + (cram >> 4)] |= Machine.gfx[0].pen_usage[tile];
			}
			for ( offs = spriteram_size[0] - 8;offs >= 0;offs -= 8 ){
				if ( spriteram.read(offs+1)> 16 ) { /* visible */
					int flags = spriteram.read(offs+4);
					palette_map[0*16 + (flags >> 4)] |= 0xfffe;
				}
			}
	
			/* expand the results */
			for (i = 0; i < 1*16; i++)
			{
				int usage = palette_map[i], j;
				if (usage != 0)
				{
					palette_used_colors[i * 16 + 0] = PALETTE_COLOR_TRANSPARENT;
					for (j = 1; j < 16; j++)
						if (usage & (1 << j))
							palette_used_colors[i * 16 + j] = PALETTE_COLOR_USED;
						else
							palette_used_colors[i * 16 + j] = PALETTE_COLOR_UNUSED;
				}
				else
					memset (&palette_used_colors[i * 16 + 0], PALETTE_COLOR_UNUSED, 16);
			}
			for (i = 1*16; i < 4*16; i++)
			{
				int usage = palette_map[i], j;
				if (usage != 0)
				{
					for (j = 0; j < 15; j++)
						if (usage & (1 << j))
							palette_used_colors[i * 16 + j] = PALETTE_COLOR_USED;
						else
							palette_used_colors[i * 16 + j] = PALETTE_COLOR_UNUSED;
					palette_used_colors[i * 16 + 15] = PALETTE_COLOR_TRANSPARENT;
				}
				else
					memset (&palette_used_colors[i * 16 + 0], PALETTE_COLOR_UNUSED, 16);
			}
	
			if (palette_recalc ())
			{
				memset( dirtybuffer,  1, videoram_size[0] );
				memset( dirtybuffer1, 1, wc90b_tile_videoram_size );
				memset( dirtybuffer2, 1, wc90b_tile_videoram_size2 );
			}
		}
	
	/* commented out -- if we copyscrollbitmap below with TRANSPARENCY_NONE, we shouldn't waste our
	   time here:
		wc90b_draw_sprites( bitmap, 3 );
	*/
	
		for ( offs = wc90b_tile_videoram_size2 - 1; offs >= 0; offs-- ) {
			int sx, sy, tile, gfx;
	
			if ( dirtybuffer2[offs] ) {
	
				dirtybuffer2[offs] = 0;
	
				sx = ( offs % 64 );
				sy = ( offs / 64 );
	
				tile = wc90b_tile_videoram2[offs];
				gfx = 9 + ( wc90b_tile_colorram2[offs] & 3 ) + ( ( wc90b_tile_colorram2[offs] >> 1 ) & 4 );
	
				drawgfx(tmpbitmap2,Machine.gfx[ gfx ],
						tile,
						wc90b_tile_colorram2[offs] >> 4,
						0,0,
						sx*16,sy*16,
						0,TRANSPARENCY_NONE,0);
			}
		}
	
		scrollx = -wc90b_scroll2xlo[0] - 256 * ( wc90b_scroll2xhi[0] & 3 );
		scrolly = -wc90b_scroll2ylo[0] - 256 * ( wc90b_scroll2yhi[0] & 1 );
	
		copyscrollbitmap(bitmap,tmpbitmap2,1,&scrollx,1,&scrolly,&Machine.visible_area,TRANSPARENCY_NONE,0);
	
		wc90b_draw_sprites( bitmap, 2 );
	
		for ( offs = wc90b_tile_videoram_size - 1; offs >= 0; offs-- ) {
			int sx, sy, tile, gfx;
	
			if ( dirtybuffer1[offs] ) {
	
				dirtybuffer1[offs] = 0;
	
				sx = ( offs % 64 );
				sy = ( offs / 64 );
	
				tile = wc90b_tile_videoram[offs];
				gfx = 1 + ( wc90b_tile_colorram[offs] & 3 ) + ( ( wc90b_tile_colorram[offs] >> 1 ) & 4 );
	
				drawgfx(tmpbitmap1,Machine.gfx[ gfx ],
						tile,
						wc90b_tile_colorram[offs] >> 4,
						0,0,
						sx*16,sy*16,
						0,TRANSPARENCY_NONE,0);
			}
		}
	
		scrollx = -wc90b_scroll1xlo[0] - 256 * ( wc90b_scroll1xhi[0] & 3 );
		scrolly = -wc90b_scroll1ylo[0] - 256 * ( wc90b_scroll1yhi[0] & 1 );
	
		copyscrollbitmap(bitmap,tmpbitmap1,1,&scrollx,1,&scrolly,&Machine.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
	
		wc90b_draw_sprites( bitmap, 1 );
	
		for ( offs = videoram_size[0] - 1; offs >= 0; offs-- ) {
			if ( dirtybuffer[offs] ) {
				int sx, sy;
	
				dirtybuffer[offs] = 0;
	
				sx = (offs % 64);
				sy = (offs / 64);
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram.read(offs)+ ( ( colorram.read(offs)& 0x07 ) << 8 ),
						colorram.read(offs)>> 4,
						0,0,
						sx*8,sy*8,
						0,TRANSPARENCY_NONE,0);
			}
		}
	
		copybitmap( bitmap, tmpbitmap, 0, 0, 0, 0,&Machine.visible_area, TRANSPARENCY_PEN,palette_transparent_pen );
	
		wc90b_draw_sprites( bitmap, 0 );
	} };
}
