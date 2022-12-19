/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class genericH
{
	
	#ifdef __cplusplus
	extern "C" {
	#endif
	
	extern UBytePtr videoram;
	extern size_t videoram_size;
	extern UBytePtr colorram;
	extern UBytePtr spriteram;
	extern size_t spriteram_size;
	extern UBytePtr spriteram_2;
	extern size_t spriteram_2_size;
	extern UBytePtr spriteram_3;
	extern size_t spriteram_3_size;
	extern UBytePtr buffered_spriteram;
	extern UBytePtr buffered_spriteram_2;
	extern UBytePtr dirtybuffer;
	extern struct osd_bitmap *tmpbitmap;
	
	void buffer_spriteram(UBytePtr ptr,int length);
	void buffer_spriteram_2(UBytePtr ptr,int length);
	
	#ifdef __cplusplus
	}
	#endif
}
