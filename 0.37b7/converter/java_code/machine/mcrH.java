/***************************************************************************

	mcr.c

	Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
	I/O ports)

	Tapper machine started by Chris Kirmse

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package machine;

public class mcrH
{
	
	
	extern INT16 spyhunt_scrollx, spyhunt_scrolly;
	extern double mcr68_timing_factor;
	
	
	
	/************ Generic MCR routines ***************/
	
	extern Z80_DaisyChain mcr_daisy_chain[];
	extern UINT8 mcr_cocktail_flip;
	
	
	
	
	
	
	
	/************ Generic character and sprite definition ***************/
	
	extern struct GfxLayout mcr_bg_layout;
	extern struct GfxLayout mcr_sprite_layout;
}
