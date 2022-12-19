/***************************************************************************
  vidhrdw/mole.c
  Functions to emulate the video hardware of Mole Attack!.
  Mole Attack's Video hardware is essentially two banks of 512 characters.
  The program uses a single byte to indicate which character goes in each location,
  and uses a control location (0x8400) to select the character sets
***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class mole
{
	
	static int tile_bank;
	static UINT16 *tile_data;
	#define NUM_ROWS 25
	#define NUM_COLS 40
	#define NUM_TILES (NUM_ROWS*NUM_COLS)
	
	public static VhConvertColorPromPtr moleattack_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) {
		int i;
		for( i=0; i<8; i++ ){
			colortable[i] = i;
			*palette++ = (i&1)?0xff:0x00;
			*palette++ = (i&4)?0xff:0x00;
			*palette++ = (i&2)?0xff:0x00;
		}
	} };
	
	public static VhStartPtr moleattack_vh_start = new VhStartPtr() { public int handler() {
		tile_data = (UINT16 *)malloc( NUM_TILES*sizeof(UINT16) );
		if (tile_data != 0){
			dirtybuffer = malloc( NUM_TILES );
			if (dirtybuffer != 0){
				memset( dirtybuffer, 1, NUM_TILES );
				return 0;
			}
			free( tile_data );
		}
		return 1; /* error */
	} };
	
	public static VhStopPtr moleattack_vh_stop = new VhStopPtr() { public void handler() {
		free( dirtybuffer );
		free( tile_data );
	} };
	
	public static WriteHandlerPtr moleattack_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if( offset<NUM_TILES ){
			if( tile_data[offset]!=data ){
				dirtybuffer[offset] = 1;
				tile_data[offset] = data | (tile_bank<<8);
			}
		}
		else if( offset==0x3ff ){ /* hack!  erase screen */
			memset( dirtybuffer, 1, NUM_TILES );
			memset( tile_data, 0, NUM_TILES*sizeof(UINT16) );
		}
	} };
	
	public static WriteHandlerPtr moleattack_tilesetselector_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		tile_bank = data;
	} };
	
	public static VhUpdatePtr moleattack_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) {
		int offs;
	
		if( full_refresh || palette_recalc() ){
			memset( dirtybuffer, 1, NUM_TILES );
		}
	
		for( offs=0; offs<NUM_TILES; offs++ ){
			if( dirtybuffer[offs] ){
				UINT16 code = tile_data[offs];
				drawgfx( bitmap, Machine.gfx[(code&0x200)?1:0],
					code&0x1ff,
					0, /* color */
					0,0, /* no flip */
					(offs%NUM_COLS)*8, /* xpos */
					(offs/NUM_COLS)*8, /* ypos */
					0, /* no clip */
					TRANSPARENCY_NONE,0 );
	
				dirtybuffer[offs] = 0;
			}
		}
	} };
}
