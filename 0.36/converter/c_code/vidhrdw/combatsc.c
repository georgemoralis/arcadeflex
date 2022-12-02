/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

#include "driver.h"
#include "vidhrdw/konamiic.h"
#include "vidhrdw/generic.h"

static struct tilemap *tilemap[2];
static struct tilemap *textlayer;
static unsigned char *private_spriteram[2];
static int priority;

unsigned char *combasc_io_ram;
static int combasc_vreg;
unsigned char* banked_area;

static int combasc_bank_select; /* 0x00..0x1f */
static int combasc_video_circuit; /* 0 or 1 */
static unsigned char *combasc_page[2];
static unsigned char combasc_scrollram0[0x40];
static unsigned char combasc_scrollram1[0x40];
static unsigned char *combasc_scrollram;


void combasc_convert_color_prom(unsigned char *palette, unsigned short *colortable, const unsigned char *color_prom )
{
	int i,pal,clut = 0;
	for( pal=0; pal<8; pal++ )
	{
		switch( pal )
		{
			case 0: /* other sprites */
			case 2: /* other sprites(alt) */
			clut = 1;	/* 0 is wrong for Firing Range III targets */
			break;

			case 4: /* player sprites */
			case 6: /* player sprites(alt) */
			clut = 2;
			break;

			case 1: /* background */
			case 3: /* background(alt) */
			clut = 1;
			break;

			case 5: /* foreground tiles */
			case 7: /* foreground tiles(alt) */
			clut = 3;
			break;
		}

		for( i=0; i<256; i++ )
		{
			if ((pal & 1) == 0)	/* sprites */
			{
				if (color_prom[256 * clut + i] == 0)
					*(colortable++) = 0;
				else
					*(colortable++) = 16 * pal + color_prom[256 * clut + i];
			}
			else	/* chars */
				*(colortable++) = 16 * pal + color_prom[256 * clut + i];
		}
	}
}

void combascb_convert_color_prom(unsigned char *palette, unsigned short *colortable, const unsigned char *color_prom )
{
	int i,pal;
	for( pal=0; pal<8; pal++ )
	{
		for( i=0; i<256; i++ )
		{
			if ((pal & 1) == 0)	/* sprites */
				*(colortable++) = 16 * pal + (color_prom[i] ^ 0x0f);
			else	/* chars */
				*(colortable++) = 16 * pal + (i & 0x0f);	/* no lookup? */
		}
	}
}

/***************************************************************************

	Callbacks for the TileMap code

***************************************************************************/

static void get_tile_info0( int col, int row )
{
	int tile_index = row*32 + col;
	unsigned char attributes = combasc_page[0][tile_index];
	int bank = 4*((combasc_vreg & 0x0f) - 1);
	int number,color;

	if (bank < 0) bank = 0;
	if ((attributes & 0xb0) == 0) bank = 0;	/* text bank */

	if (attributes & 0x80) bank += 1;
	if (attributes & 0x10) bank += 2;
	if (attributes & 0x20) bank += 4;

	color = ((K007121_ctrlram[0][6]&0x10)*2+16) + (attributes & 0x0f);

	number = combasc_page[0][tile_index + 0x400] + 256*bank;

	SET_TILE_INFO(0,number,color)
	tile_info.priority = (attributes & 0x40) >> 6;
}

static void get_tile_info1( int col, int row )
{
	int tile_index = row*32 + col;
	unsigned char attributes = combasc_page[1][tile_index];
	int bank = 4*((combasc_vreg >> 4) - 1);
	int number, color;

	if (bank < 0) bank = 0;
	if ((attributes & 0xb0) == 0) bank = 0;	/* text bank */

	if (attributes & 0x80) bank += 1;
	if (attributes & 0x10) bank += 2;
	if (attributes & 0x20) bank += 4;

	color = ((K007121_ctrlram[1][6]&0x10)*2+16+4*16) + (attributes & 0x0f);

	number = combasc_page[1][tile_index + 0x400] + 256*bank;

	SET_TILE_INFO(1,number,color)
	tile_info.priority = (attributes & 0x40) >> 6;
}

static void get_text_info( int col, int row )
{
	int tile_index = row*32 + col + 0x800;
	unsigned char attributes = combasc_page[0][tile_index];
	int number = combasc_page[0][tile_index + 0x400];
	int color = 16 + (attributes & 0x0f);

	SET_TILE_INFO(0,number,color)

	/* the following hack is needed because the TileMap system doesn't support TRANSPARENCY_COLOR */
	tile_info.flags = 0;
	if ((attributes & 0x0f) == 0x01 || (attributes & 0x0f) == 0x0e)
		tile_info.flags = TILE_IGNORE_TRANSPARENCY;
}

static void get_tile_info0_bootleg( int col, int row )
{
	int tile_index = row*32 + col;
	unsigned char attributes = combasc_page[0][tile_index];
	int bank = 4*((combasc_vreg & 0x0f) - 1);
	int number, pal, color;

	if (bank < 0) bank = 0;
	if ((attributes & 0xb0) == 0) bank = 0;	/* text bank */

	if (attributes & 0x80) bank += 1;
	if (attributes & 0x10) bank += 2;
	if (attributes & 0x20) bank += 4;

	pal = (bank == 0 || bank >= 0x1c || (attributes & 0x40)) ? 1 : 3;
	color = pal*16;// + (attributes & 0x0f);
	number = combasc_page[0][tile_index + 0x400] + 256*bank;

	SET_TILE_INFO(0,number,color)
}

static void get_tile_info1_bootleg( int col, int row )
{
	int tile_index = row*32 + col;
	unsigned char attributes = combasc_page[1][tile_index];
	int bank = 4*((combasc_vreg >> 4) - 1);
	int number, pal, color;

	if (bank < 0) bank = 0;
	if ((attributes & 0xb0) == 0) bank = 0;	/* text bank */

	if (attributes & 0x80) bank += 1;
	if (attributes & 0x10) bank += 2;
	if (attributes & 0x20) bank += 4;

	pal = (bank == 0 || bank >= 0x1c || (attributes & 0x40)) ? 5 : 7;
	color = pal*16;// + (attributes & 0x0f);
	number = combasc_page[1][tile_index + 0x400] + 256*bank;

	SET_TILE_INFO(1,number,color)
}

static void get_text_info_bootleg( int col, int row )
{
	int tile_index = row*32 + col + 0x800;
//	unsigned char attributes = combasc_page[0][tile_index];
	int number = combasc_page[0][tile_index + 0x400];
	int color = 16;// + (attributes & 0x0f);

	SET_TILE_INFO(1, number, color)
}

/***************************************************************************

	Start the video hardware emulation.

***************************************************************************/

int combasc_vh_start(void)
{
	combasc_vreg = -1;

	tilemap[0] = tilemap_create(get_tile_info0,TILEMAP_TRANSPARENT,8,8,32,32);
	tilemap[1] = tilemap_create(get_tile_info1,TILEMAP_TRANSPARENT,8,8,32,32);
	textlayer =  tilemap_create(get_text_info, TILEMAP_TRANSPARENT,8,8,32,32);

	private_spriteram[0] = malloc(0x800);
	private_spriteram[1] = malloc(0x800);
	memset(private_spriteram[0],0,0x800);
	memset(private_spriteram[1],0,0x800);

	if (tilemap[0] && tilemap[1] && textlayer)
	{
		tilemap[0]->transparent_pen = 0;
		tilemap[1]->transparent_pen = 0;
		textlayer->transparent_pen = 0;

		tilemap_set_scroll_rows(textlayer,32);

		return 0;
	}

	return 1;
}

int combascb_vh_start(void)
{
	combasc_vreg = -1;

	tilemap[0] =  tilemap_create(get_tile_info0_bootleg,TILEMAP_TRANSPARENT,8,8,32,32);
	tilemap[1] =  tilemap_create(get_tile_info1_bootleg,TILEMAP_TRANSPARENT,8,8,32,32);
	textlayer = tilemap_create(get_text_info_bootleg, TILEMAP_TRANSPARENT,8,8,32,32);

	private_spriteram[0] = malloc(0x800);
	private_spriteram[1] = malloc(0x800);
	memset(private_spriteram[0],0,0x800);
	memset(private_spriteram[1],0,0x800);

	if (tilemap[0] && tilemap[1] && textlayer)
	{
		tilemap[0]->transparent_pen = 0;
		tilemap[1]->transparent_pen = 0;
		textlayer->transparent_pen = 0;

		tilemap_set_scroll_rows(tilemap[0],32);
		tilemap_set_scroll_rows(tilemap[1],32);

		return 0;
	}

	return 1;
}

void combasc_vh_stop(void)
{
	free(private_spriteram[0]);
	free(private_spriteram[1]);
}

/***************************************************************************

	Memory handlers

***************************************************************************/

int combasc_video_r( int offset )
{
	return videoram[offset];
}

void combasc_video_w( int offset, int data )
{
	if( videoram[offset]!=data )
	{
		videoram[offset] = data;
		if( offset<0x800 )
		{
			offset = offset&0x3ff;
			if (combasc_video_circuit)
				tilemap_mark_tile_dirty( tilemap[1], offset%32, offset/32 );
			else
				tilemap_mark_tile_dirty( tilemap[0], offset%32, offset/32 );
		}
		else if( offset<0x1000 && combasc_video_circuit==0 )
		{
			offset = offset&0x3ff;
			tilemap_mark_tile_dirty( textlayer, offset%32, offset/32 );
		}
	}
}


void combasc_vreg_w( int offset, int data )
{
	if (data != combasc_vreg)
	{
		tilemap_mark_all_tiles_dirty( textlayer );
		if ((data & 0x0f) != (combasc_vreg & 0x0f))
			tilemap_mark_all_tiles_dirty( tilemap[0] );
		if ((data >> 4) != (combasc_vreg >> 4))
			tilemap_mark_all_tiles_dirty( tilemap[1] );
		combasc_vreg = data;
	}
}

void combascb_sh_irqtrigger_w(int offset, int data)
{
	soundlatch_w(offset,data);
	cpu_cause_interrupt(1,0xff);
}

int combasc_io_r( int offset )
{
	if ((offset <= 0x403) && (offset >= 0x400))
	{
		switch (offset)
		{
			case 0x400:	return input_port_0_r(0); break;
			case 0x401:	return input_port_1_r(0); break;
			case 0x402:	return input_port_2_r(0); break;
			case 0x403:	return input_port_3_r(0); break;
		}
	}
	return banked_area[offset];
}

void combasc_io_w( int offset, int data )
{
	switch (offset)
	{
		case 0x400: priority = data & 0x20; break;
		case 0x800: combascb_sh_irqtrigger_w(0, data); break;
		case 0xc00:	combasc_vreg_w(0, data); break;
		default:
			combasc_io_ram[offset] = data;
	}
}

void combasc_bankselect_w(int offset,int data)
{
	unsigned char *page = memory_region(REGION_CPU1) + 0x10000;

	if (data & 0x40)
	{
		combasc_video_circuit = 1;
		videoram = combasc_page[1];
		combasc_scrollram = combasc_scrollram1;
	}
	else
	{
		combasc_video_circuit = 0;
		videoram = combasc_page[0];
		combasc_scrollram = combasc_scrollram0;
	}

	priority = data & 0x20;

	if (data & 0x10)
	{
		cpu_setbank(1,page + 0x4000 * ((data & 0x0e) >> 1));
	}
	else
	{
		cpu_setbank(1,page + 0x20000 + 0x4000 * (data & 1));
	}
}

void combascb_bankselect_w(int offset,int data)
{
	if (data & 0x40)
	{
		combasc_video_circuit = 1;
		videoram = combasc_page[1];
	}
	else
	{
		combasc_video_circuit = 0;
		videoram = combasc_page[0];
	}

	data = data & 0x1f;
	if( data != combasc_bank_select )
	{
		unsigned char *page = memory_region(REGION_CPU1) + 0x10000;
		combasc_bank_select = data;

		if (data & 0x10)
		{
			cpu_setbank(1,page + 0x4000 * ((data & 0x0e) >> 1));
		}
		else
		{
			cpu_setbank(1,page + 0x20000 + 0x4000 * (data & 1));
		}

		if (data == 0x1f)
		{
cpu_setbank(1,page + 0x20000 + 0x4000 * (data & 1));
			cpu_setbankhandler_r (1, combasc_io_r);/* IO RAM & Video Registers */
			cpu_setbankhandler_w (1, combasc_io_w);
		}
		else
		{
			cpu_setbankhandler_r (1, MRA_BANK1);	/* banked ROM */
			cpu_setbankhandler_w (1, MWA_ROM);
		}
	}
}

void combasc_init_machine( void )
{
	unsigned char *MEM = memory_region(REGION_CPU1) + 0x38000;


	combasc_io_ram  = MEM + 0x0000;
	combasc_page[0] = MEM + 0x4000;
	combasc_page[1] = MEM + 0x6000;

	memset( combasc_io_ram,  0x00, 0x4000 );
	memset( combasc_page[0], 0x00, 0x2000 );
	memset( combasc_page[1], 0x00, 0x2000 );

	combasc_bank_select = -1;
	combasc_bankselect_w( 0,0 );
}

void combasc_pf_control_w( int offset, int data )
{
	K007121_ctrl_w(combasc_video_circuit,offset,data);

	if (offset == 7)
		tilemap_set_flip(tilemap[combasc_video_circuit],(data & 0x08) ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);

	if (offset == 3)
	{
		if (data & 0x08)
			memcpy(private_spriteram[combasc_video_circuit],combasc_page[combasc_video_circuit]+0x1000,0x800);
		else
			memcpy(private_spriteram[combasc_video_circuit],combasc_page[combasc_video_circuit]+0x1800,0x800);
	}
}

int combasc_scrollram_r( int offset )
{
	return combasc_scrollram[offset];
}

void combasc_scrollram_w( int offset, int data )
{
	combasc_scrollram[offset] = data;
}



/***************************************************************************

	Display Refresh

***************************************************************************/

static void draw_sprites(struct osd_bitmap *bitmap, const unsigned char *source, int circuit)
{
	int base_color = (circuit*4)*16+(K007121_ctrlram[circuit][6]&0x10)*2;

	K007121_sprites_draw(circuit,bitmap,source,base_color,0,0);
}


void combasc_vh_screenrefresh( struct osd_bitmap *bitmap, int fullrefresh )
{
	int i;


	if (K007121_ctrlram[0][0x01] & 0x02)
	{
		tilemap_set_scroll_rows(tilemap[0],32);
		for (i = 0;i < 32;i++)
		{
			tilemap_set_scrollx(tilemap[0],i,combasc_scrollram0[i]);
		}
	}
	else
	{
		tilemap_set_scroll_rows(tilemap[0],1);
		tilemap_set_scrollx(tilemap[0],0,K007121_ctrlram[0][0x00] | ((K007121_ctrlram[0][0x01] & 0x01) << 8));
	}

	if (K007121_ctrlram[1][0x01] & 0x02)
	{
		tilemap_set_scroll_rows(tilemap[1],32);
		for (i = 0;i < 32;i++)
		{
			tilemap_set_scrollx(tilemap[1],i,combasc_scrollram1[i]);
		}
	}
	else
	{
		tilemap_set_scroll_rows(tilemap[1],1);
		tilemap_set_scrollx( tilemap[1],0,K007121_ctrlram[1][0x00] | ((K007121_ctrlram[1][0x01] & 0x01) << 8));
	}

	tilemap_set_scrolly(tilemap[0],0,K007121_ctrlram[0][0x02]);
	tilemap_set_scrolly(tilemap[1],0,K007121_ctrlram[1][0x02]);

	tilemap_update(ALL_TILEMAPS);
	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	tilemap_render(ALL_TILEMAPS);

	if (priority == 0)
	{
/*
 TODO: there's a priority bug in the Mission level which isn't easy to fix.
 It is an orthogonality issue: while tilemap[1],1 has priority over sprites 1,
 and sprites 1 have priority over tilemap[0],1, tilemap[1] does NOT have
 priority over tilemap[0]. As it is now, the damage indicator passes behind the
 wall when you go through the first door, while it should of course be in front.
 */
		tilemap_draw(bitmap,tilemap[1],TILEMAP_IGNORE_TRANSPARENCY|0);
		tilemap_draw(bitmap,tilemap[0],0);
		draw_sprites(bitmap,private_spriteram[0],0);
		tilemap_draw(bitmap,tilemap[0],1);
		draw_sprites(bitmap,private_spriteram[1],1);
		tilemap_draw(bitmap,tilemap[1],1);
	}
	else
	{
		tilemap_draw(bitmap,tilemap[0],TILEMAP_IGNORE_TRANSPARENCY|0);
		draw_sprites(bitmap,private_spriteram[0],0);
		tilemap_draw(bitmap,tilemap[0],1);
		tilemap_draw(bitmap,tilemap[1],0);
		draw_sprites(bitmap,private_spriteram[1],1);
		tilemap_draw(bitmap,tilemap[1],1);
	}

	if (K007121_ctrlram[0][0x01] & 0x08)
	{
		for (i = 0;i < 32;i++)
		{
			tilemap_set_scrollx(textlayer,i,combasc_scrollram0[0x20+i] ? 0 : TILE_LINE_DISABLED);
			tilemap_draw(bitmap,textlayer,0);
		}
	}

	/* chop the extreme columns if necessary */
	if (K007121_ctrlram[0][0x03] & 0x40)
	{
		struct rectangle clip;

		clip = Machine->drv->visible_area;
		clip.max_x = clip.min_x + 7;
		fillbitmap(bitmap,Machine->pens[0],&clip);

		clip = Machine->drv->visible_area;
		clip.min_x = clip.max_x - 7;
		fillbitmap(bitmap,Machine->pens[0],&clip);
	}
}








/***************************************************************************

	bootleg Combat School sprites. Each sprite has 5 bytes:

byte #0:	sprite number
byte #1:	y position
byte #2:	x position
byte #3:
	bit 0:		x position (bit 0)
	bits 1..3:	???
	bit 4:		flip x
	bit 5:		unused?
	bit 6:		sprite bank # (bit 2)
	bit 7:		???
byte #4:
	bits 0,1:	sprite bank # (bits 0 & 1)
	bits 2,3:	unused?
	bits 4..7:	sprite color

***************************************************************************/

static void bootleg_draw_sprites( struct osd_bitmap *bitmap, const unsigned char *source, int circuit )
{
	const struct GfxElement *gfx = Machine->gfx[circuit+2];
	const struct rectangle *clip = &Machine->drv->visible_area;

	unsigned char *RAM = memory_region(REGION_CPU1);
	int limit = ( circuit) ? (RAM[0xc2]*256 + RAM[0xc3]) : (RAM[0xc0]*256 + RAM[0xc1]);
	const unsigned char *finish;

	source+=0x1000;
	finish = source;
	source+=0x400;
	limit = (0x3400-limit)/8;
	if( limit>=0 ) finish = source-limit*8;
	source-=8;

	while( source>finish )
	{
		unsigned char attributes = source[3]; /* PBxF ?xxX */
		{
			int number = source[0];
			int x = source[2] - 71 + (attributes & 0x01)*256;
			int y = 242 - source[1];
			unsigned char color = source[4]; /* CCCC xxBB */

			int bank = (color & 0x03) | ((attributes & 0x40) >> 4);

			number = ((number & 0x02) << 1) | ((number & 0x04) >> 1) | (number & (~6));
			number += 256*bank;

			color = (circuit*4)*16 + (color >> 4);

			/*	hacks to select alternate palettes */
//			if(combasc_vreg == 0x40 && (attributes & 0x40)) color += 1*16;
//			if(combasc_vreg == 0x23 && (attributes & 0x02)) color += 1*16;
//			if(combasc_vreg == 0x66 ) color += 2*16;

			drawgfx( bitmap, gfx,
				number, color,
				attributes & 0x10,0, /* flip */
				x,y,
				clip, TRANSPARENCY_PEN, 15 );
		}
		source -= 8;
	}
}

void combascb_vh_screenrefresh( struct osd_bitmap *bitmap, int fullrefresh )
{
	int i;

	for( i=0; i<32; i++ )
	{
		tilemap_set_scrollx( tilemap[0],i, combasc_io_ram[0x040+i]+5 );
		tilemap_set_scrollx( tilemap[1],i, combasc_io_ram[0x060+i]+3 );
	}
	tilemap_set_scrolly( tilemap[0],0, combasc_io_ram[0x000] );
	tilemap_set_scrolly( tilemap[1],0, combasc_io_ram[0x020] );

	tilemap_update( ALL_TILEMAPS );
	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	tilemap_render( ALL_TILEMAPS );

	if (priority == 0)
	{
		tilemap_draw( bitmap,tilemap[1],TILEMAP_IGNORE_TRANSPARENCY );
		bootleg_draw_sprites( bitmap, combasc_page[0], 0 );
		tilemap_draw( bitmap,tilemap[0],0 );
		bootleg_draw_sprites( bitmap, combasc_page[1], 1 );
	}
	else
	{
		tilemap_draw( bitmap,tilemap[0],TILEMAP_IGNORE_TRANSPARENCY );
		bootleg_draw_sprites( bitmap, combasc_page[0], 0 );
		tilemap_draw( bitmap,tilemap[1],0 );
		bootleg_draw_sprites( bitmap, combasc_page[1], 1 );
	}

	tilemap_draw( bitmap,textlayer,0 );
}
