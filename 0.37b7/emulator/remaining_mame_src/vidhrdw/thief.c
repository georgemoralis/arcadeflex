/*	video hardware for Pacific Novelty games:
**	Thief/Nato Defense
*/

#include "driver.h"
#include "vidhrdw/generic.h"

struct osd_bitmap *thief_page0;
struct osd_bitmap *thief_page1;

static UINT8 thief_read_mask, thief_write_mask;
static UINT8 thief_video_control;

static struct {
	UINT8 *context_ram;
	UINT8 bank;
	UINT8 *image_ram;
	UINT8 param[0x9];
} thief_coprocessor;

enum {
	IMAGE_ADDR_LO,		//0xe000
	IMAGE_ADDR_HI,		//0xe001
	SCREEN_XPOS,		//0xe002
	SCREEN_YPOS,		//0xe003
	BLIT_WIDTH,			//0xe004
	BLIT_HEIGHT,		//0xe005
	GFX_PORT,			//0xe006
	BARL_PORT,			//0xe007
	BLIT_ATTRIBUTES		//0xe008
};

/***************************************************************************/

READ_HANDLER( thief_context_ram_r ){
	return thief_coprocessor.context_ram[0x40*thief_coprocessor.bank+offset];
}

WRITE_HANDLER( thief_context_ram_w ){
	thief_coprocessor.context_ram[0x40*thief_coprocessor.bank+offset] = data;
}

WRITE_HANDLER( thief_context_bank_w ){
	thief_coprocessor.bank = data&0xf;
}

/***************************************************************************/

WRITE_HANDLER( thief_video_control_w ){
	if( (data^thief_video_control)&1 ){
		/* screen flipped */
		memset( dirtybuffer, 0x00, 0x2000*2 );
	}

	thief_video_control = data;
/*
	bit 0: screen flip
	bit 1: working page
	bit 2: visible page
	bit 3: mirrors bit 1
	bit 4: mirrors bit 2
*/
}

WRITE_HANDLER( thief_vtcsel_w ){
	/* TMS9927 VTAC registers */
}

WRITE_HANDLER( thief_color_map_w ){
/*
	--xx----	blue
	----xx--	green
	------xx	red
*/
	const UINT8 intensity[4] = {0x00,0x55,0xAA,0xFF};
	int r = intensity[(data & 0x03) >> 0];
    int g = intensity[(data & 0x0C) >> 2];
    int b = intensity[(data & 0x30) >> 4];
	palette_change_color( offset,r,g,b );
}

/***************************************************************************/

WRITE_HANDLER( thief_color_plane_w ){
/*
	--xx----	selects bitplane to read from (0..3)
	----xxxx	selects bitplane(s) to write to (0x0 = none, 0xf = all)
*/
	thief_write_mask = data&0xf;
	thief_read_mask = (data>>4)&3;
}

READ_HANDLER( thief_videoram_r ){
	unsigned char *source = &videoram[offset];
	if( thief_video_control&0x02 ) source+=0x2000*4; /* foreground/background */
	return source[thief_read_mask*0x2000];
}

WRITE_HANDLER( thief_videoram_w ){
	UINT8 *dest = &videoram[offset];
	if( thief_video_control&0x02 ){
		dest+=0x2000*4; /* foreground/background */
		dirtybuffer[offset+0x2000] = 1;
	}
	else {
		dirtybuffer[offset] = 1;
	}
	if( thief_write_mask&0x1 ) dest[0x2000*0] = data;
	if( thief_write_mask&0x2 ) dest[0x2000*1] = data;
	if( thief_write_mask&0x4 ) dest[0x2000*2] = data;
	if( thief_write_mask&0x8 ) dest[0x2000*3] = data;
}

/***************************************************************************/

void thief_vh_stop( void ){
	free( videoram );
	free( dirtybuffer );
	osd_free_bitmap( thief_page1 );
	osd_free_bitmap( thief_page0 );
	free( thief_coprocessor.context_ram );
	free( thief_coprocessor.image_ram );
}

int thief_vh_start( void ){
	UINT8 *dest = memory_region( REGION_CPU1 );
	const UINT8 *source = memory_region( REGION_CPU2 );
	memcpy( &dest[0xe010], &source[0x290], 0x20 );

	memset( &thief_coprocessor, 0x00, sizeof(thief_coprocessor) );

	thief_page0	= bitmap_alloc( 256,256 );
	thief_page1	= bitmap_alloc( 256,256 );
	videoram = calloc( 0x2000*4*2,1 );
	dirtybuffer = malloc( 0x2000*2 );

	thief_coprocessor.image_ram = malloc( 0x2000 );
	thief_coprocessor.context_ram = malloc( 0x400 );

	if( thief_page0 && thief_page1 &&
		videoram && dirtybuffer &&
		thief_coprocessor.image_ram &&
		thief_coprocessor.context_ram )
	{
		memset( dirtybuffer, 1, 0x2000*2 );
		return 0;
	}
	thief_vh_stop();
	return 1;
}

void thief_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh){
	unsigned int offs;
	int flipscreen = thief_video_control&1;
	const UINT16 *pal_data = Machine->pens;
	UINT8 *dirty = dirtybuffer;
	const UINT8 *source = videoram;
	struct osd_bitmap *page;

	if( thief_video_control&4 ){ /* visible page */
		dirty += 0x2000;
		source += 0x2000*4;
		page = thief_page1;
	}
	else {
		page = thief_page0;
	}

	palette_recalc();

	for( offs=0; offs<0x2000; offs++ ){
		if( dirty[offs] ){
			int ypos = offs/32;
			int xpos = (offs%32)*8;
			int plane0 = source[0x2000*0+offs];
			int plane1 = source[0x2000*1+offs];
			int plane2 = source[0x2000*2+offs];
			int plane3 = source[0x2000*3+offs];
			int bit;
			if( flipscreen ){
				for( bit=0; bit<8; bit++ ){
					plot_pixel( page, 0xff - (xpos+bit), 0xff - ypos,
						pal_data[
							(((plane0<<bit)&0x80)>>7) |
							(((plane1<<bit)&0x80)>>6) |
							(((plane2<<bit)&0x80)>>5) |
							(((plane3<<bit)&0x80)>>4)
						]
					);
				}
			}
			else {
				for( bit=0; bit<8; bit++ ){
					plot_pixel( page, xpos+bit, ypos,
						pal_data[
							(((plane0<<bit)&0x80)>>7) |
							(((plane1<<bit)&0x80)>>6) |
							(((plane2<<bit)&0x80)>>5) |
							(((plane3<<bit)&0x80)>>4)
						]
					);
				}
			}
			dirty[offs] = 0;
		}
	}
	copybitmap(bitmap,page,0,0,0,0,&Machine->visible_area,TRANSPARENCY_NONE,0);
}

/***************************************************************************/

static UINT16 fetch_image_addr( void ){
	int addr = thief_coprocessor.param[IMAGE_ADDR_LO]+256*thief_coprocessor.param[IMAGE_ADDR_HI];
	/* auto-increment */
	thief_coprocessor.param[IMAGE_ADDR_LO]++;
	if( thief_coprocessor.param[IMAGE_ADDR_LO]==0x00 ){
		thief_coprocessor.param[IMAGE_ADDR_HI]++;
	}
	return addr;
}

WRITE_HANDLER( thief_blit_w ){
	int i, offs, xoffset, dy;
	UINT8 *gfx_rom = memory_region( REGION_GFX1 );
	UINT8 x = thief_coprocessor.param[SCREEN_XPOS];
	UINT8 y = thief_coprocessor.param[SCREEN_YPOS];
	UINT8 width = thief_coprocessor.param[BLIT_WIDTH];
	UINT8 height = thief_coprocessor.param[BLIT_HEIGHT];
	UINT8 attributes = thief_coprocessor.param[BLIT_ATTRIBUTES];

	UINT8 old_data;
	int xor_blit = data;
		/* making the xor behavior selectable fixes score display,
		but causes minor glitches on the playfield */

	x -= width*8;
	xoffset = x&7;

	if( attributes&0x10 ){
		y += 7-height;
		dy = 1;
	}
	else {
		dy = -1;
	}
	height++;
	while( height-- ){
		for( i=0; i<=width; i++ ){
			int addr = fetch_image_addr();
			if( addr<0x2000 ){
				data = thief_coprocessor.image_ram[addr];
			}
			else {
				addr -= 0x2000;
				if( addr<0x2000*3 ) data = gfx_rom[addr];
			}
			offs = (y*32+x/8+i)&0x1fff;
			old_data = thief_videoram_r( offs );
			if( xor_blit ){
				thief_videoram_w( offs, old_data^(data>>xoffset) );
			}
			else {
				thief_videoram_w( offs,
					(old_data&(0xff00>>xoffset)) | (data>>xoffset)
				);
			}
			offs = (offs+1)&0x1fff;
			old_data = thief_videoram_r( offs );
			if( xor_blit ){
				thief_videoram_w( offs, old_data^((data<<(8-xoffset))&0xff) );
			}
			else {
				thief_videoram_w( offs,
					(old_data&(0xff>>xoffset)) | ((data<<(8-xoffset))&0xff)
				);
			}
		}
		y+=dy;
	}
}

READ_HANDLER( thief_coprocessor_r ){
	switch( offset ){
 	case SCREEN_XPOS: /* xpos */
	case SCREEN_YPOS: /* ypos */
		{
	 	/* XLAT: given (x,y) coordinate, return byte address in videoram */
			int addr = thief_coprocessor.param[SCREEN_XPOS]+256*thief_coprocessor.param[SCREEN_YPOS];
			int result = 0xc000 | (addr>>3);
			return (offset==0x03)?(result>>8):(result&0xff);
		}
		break;

	case GFX_PORT:
		{
			int addr = fetch_image_addr();
			if( addr<0x2000 ){
				return thief_coprocessor.image_ram[addr];
			}
			else {
				UINT8 *gfx_rom = memory_region( REGION_GFX1 );
				addr -= 0x2000;
				if( addr<0x6000 ) return gfx_rom[addr];
			}
		}
		break;

	case BARL_PORT:
		{
			/* return bitmask for addressed pixel */
			int dx = thief_coprocessor.param[SCREEN_XPOS]&0x7;
			if( thief_coprocessor.param[BLIT_ATTRIBUTES]&0x01 ){
				return 0x01<<dx; // flipx
			}
			else {
				return 0x80>>dx; // no flip
			}
		}
		break;
	}

	return thief_coprocessor.param[offset];
}

WRITE_HANDLER( thief_coprocessor_w ){
	switch( offset ){
	case GFX_PORT:
		{
			int addr = fetch_image_addr();
			if( addr<0x2000 ){
				thief_coprocessor.image_ram[addr] = data;
			}
		}
		break;

	default:
		thief_coprocessor.param[offset] = data;
		break;
	}
}
