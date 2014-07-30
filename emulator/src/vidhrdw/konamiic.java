package vidhrdw;

import static arcadeflex.libc.*;
import static arcadeflex.libc_old.*;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static vidhrdw.generic.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.mame.*;
import static mame.tilemapC.*;
import static mame.tilemapH.*;
import static mame.palette.*;
import static mame.cpuintrf.*;
import static mame.common.*;
import static mame.paletteH.*;

public  class konamiic
{
    public static FILE konamiicclog=fopen("konamiicc.log", "wa");  //for debug purposes
    
/*TODO*////*TODO*////*
/*TODO*////*TODO*///	This recursive function doesn't use additional memory
/*TODO*////*TODO*///	(it could be easily converted into an iterative one).
/*TODO*////*TODO*///	It's called shuffle because it mimics the shuffling of a deck of cards.
/*TODO*////*TODO*///*/
/*TODO*////*TODO*///static void shuffle(UINT16 *buf,int len)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	int i;
/*TODO*////*TODO*///	UINT16 t;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	if (len == 2) return;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	if (len % 4) exit(1);   /* must not happen */
/*TODO*////*TODO*///
/*TODO*////*TODO*///	len /= 2;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	for (i = 0;i < len/2;i++)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		t = buf[len/2 + i];
/*TODO*////*TODO*///		buf[len/2 + i] = buf[len + i];
/*TODO*////*TODO*///		buf[len + i] = t;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///
/*TODO*////*TODO*///	shuffle(buf,len);
/*TODO*////*TODO*///	shuffle(buf + len,len);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*////* helper function to join two 16-bit ROMs and form a 32-bit data stream */
/*TODO*////*TODO*///void konami_rom_deinterleave_2(int mem_region)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	shuffle((UINT16 *)memory_region(mem_region),memory_region_length(mem_region)/2);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*////* helper function to join four 16-bit ROMs and form a 64-bit data stream */
/*TODO*////*TODO*///void konami_rom_deinterleave_4(int mem_region)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	konami_rom_deinterleave_2(mem_region);
/*TODO*////*TODO*///	konami_rom_deinterleave_2(mem_region);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
        public static final int MAX_K007121=2;
        public static char[][] K007121_ctrlram=new char[MAX_K007121][];
        public static int[] K007121_flipscreen=new int[MAX_K007121];

        static 
        {
            for(int i=0; i<MAX_K007121; i++)
            {
                K007121_ctrlram[i]=new char[8];
            }
        }
        public static void K007121_ctrl_w(int chip,int offset,int data)
        {
                switch (offset)
                {
                        case 6:
        /* palette bank change */
        if ((K007121_ctrlram[chip][offset] & 0x30) != (data & 0x30))
                tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
                                break;
                        case 7:
                                K007121_flipscreen[chip] = data & 0x08;
                                break;
                }

                K007121_ctrlram[chip][offset] = (char)(data &0xff);
                //if(konamiicclog!=null) fprintf( konamiicclog, "K007121_ctrlram: chip=%d,offset=%d,data=%d\n",chip,offset,data );
        }

        public static WriteHandlerPtr K007121_ctrl_0_w = new WriteHandlerPtr() { public void handler(int offset, int data){       
            K007121_ctrl_w(0,offset,data & 0xFF);
        }};

        public static WriteHandlerPtr K007121_ctrl_1_w = new WriteHandlerPtr() { public void handler(int offset, int data){
            K007121_ctrl_w(1,offset,data & 0xFF);
        }};

    public static void K007121_sprites_draw(int chip,osd_bitmap bitmap,
                    CharPtr source,int base_color,int global_x_offset,int bank_base)
    {
            GfxElement gfx = Machine.gfx[chip];
            int flip_screen = K007121_flipscreen[chip];
            int i,num,inc,trans;
            int[] offs=new int[5];
            int is_flakatck = K007121_ctrlram[chip][0x06] & 0x04;	/* WRONG!!!! */

     /*       if(konamiicclog!=null) fprintf( konamiicclog,"%02x-%02x-%02x-%02x-%02x-%02x-%02x-%02x  %02x-%02x-%02x-%02x-%02x-%02x-%02x-%02x\n",
	(int)K007121_ctrlram[0][0x00],(int)K007121_ctrlram[0][0x01],(int)K007121_ctrlram[0][0x02],(int)K007121_ctrlram[0][0x03],(int)K007121_ctrlram[0][0x04],(int)K007121_ctrlram[0][0x05],(int)K007121_ctrlram[0][0x06],(int)K007121_ctrlram[0][0x07],
	(int)K007121_ctrlram[1][0x00],(int)K007121_ctrlram[1][0x01],(int)K007121_ctrlram[1][0x02],(int)K007121_ctrlram[1][0x03],(int)K007121_ctrlram[1][0x04],(int)K007121_ctrlram[1][0x05],(int)K007121_ctrlram[1][0x06],(int)K007121_ctrlram[1][0x07]);
           */
            if (is_flakatck!=0)
            {
                    num = 0x40;
                    inc = -0x20;
                    source.inc(0x3f*0x20);
                    offs[0] = 0x0e;
                    offs[1] = 0x0f;
                    offs[2] = 0x06;
                    offs[3] = 0x04;
                    offs[4] = 0x08;
                    /* Flak Attack doesn't use a lookup PROM, it maps the color code directly */
                    /* to a palette entry */
                    trans = TRANSPARENCY_PEN;
            }
            else	/* all others */
            {
                    num = (K007121_ctrlram[chip][0x03] & 0x40)!=0 ? 0x80 : 0x40;	/* WRONG!!! (needed by combasc)  */
                    inc = 5;
                    offs[0] = 0x00;
                    offs[1] = 0x01;
                    offs[2] = 0x02;
                    offs[3] = 0x03;
                    offs[4] = 0x04;
                    trans = TRANSPARENCY_COLOR;
            }

            for (i = 0;i < num;i++)
            {
                    int number = source.read(offs[0]);				/* sprite number */
                    int sprite_bank = source.read(offs[1]) & 0x0f;	/* sprite bank */
                    int sx = source.read(offs[3]);					/* vertical position */
                    int sy = source.read(offs[2]);					/* horizontal position */
                    int attr = source.read(offs[4]);				/* attributes */
                    int xflip = source.read(offs[4]) & 0x10;		/* flip x */
                    int yflip = source.read(offs[4]) & 0x20;		/* flip y */
                    int color = base_color + ((source.read(offs[1]) & 0xf0) >> 4);
                    int width,height;
                    int x_offset[] = {0x0,0x1,0x4,0x5};
                    int y_offset[] = {0x0,0x2,0x8,0xa};
                    int x,y, ex, ey;

                    if ((attr & 0x01)!=0) sx -= 256;
                    if (sy >= 240) sy -= 256;

                    number += ((sprite_bank & 0x3) << 8) + ((attr & 0xc0) << 4);
                    number = number << 2;
                    number += (sprite_bank >> 2) & 3;
                    if(konamiicclog!=null) fprintf( konamiicclog,"number=%d,sprite_bank=%d,sx=%d,sy=%d,attr=%d,xflip=%d,yflip=%d,color=%d\n",number,sprite_bank,sx,sy,attr,xflip,yflip,color );
                    if (is_flakatck==0 || source.read(0x00)!=0)	/* Flak Attack needs this */
                    {
                            number += bank_base;

                            switch( attr&0xe )
                            {
                                    case 0x06: width = height = 1; break;
                                    case 0x04: width = 1; height = 2; number &= (~2); break;
                                    case 0x02: width = 2; height = 1; number &= (~1); break;
                                    case 0x00: width = height = 2; number &= (~3); break;
                                    case 0x08: width = height = 4; number &= (~3); break;
                                    default: width = 1; height = 1;
    //					if (errorlog) fprintf(errorlog,"Unknown sprite size %02x\n",attr&0xe);
    //					usrintf_showmessage("Unknown sprite size %02x\n",attr&0xe);
                            }

                            for (y = 0;y < height;y++)
                            {
                                    for (x = 0;x < width;x++)
                                    {
                                            ex = xflip!=0 ? (width-1-x) : x;
                                            ey = yflip!=0 ? (height-1-y) : y;

                                            if (flip_screen!=0)
                                                    drawgfx(bitmap,gfx,
                                                            number + x_offset[ex] + y_offset[ey],
                                                            color,
                                                            NOT(xflip),NOT(yflip),
                                                            248-(sx+x*8),248-(sy+y*8),
                                                            Machine.drv.visible_area,trans,0);
                                            else
                                                    drawgfx(bitmap,gfx,
                                                            number + x_offset[ex] + y_offset[ey],
                                                            color,
                                                            xflip,yflip,
                                                            global_x_offset+sx+x*8,sy+y*8,
                                                            Machine.drv.visible_area,trans,0);
                                    }
                            }
                    }

                    source.inc(inc);
            }
    }
/*TODO*////*TODO*///void K007121_mark_sprites_colors(int chip,
/*TODO*////*TODO*///		const unsigned char *source,int base_color,int bank_base)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	int i,num,inc,offs[5];
/*TODO*////*TODO*///	int is_flakatck = K007121_ctrlram[chip][0x06] & 0x04;	/* WRONG!!!! */
/*TODO*////*TODO*///
/*TODO*////*TODO*///	unsigned short palette_map[512];
/*TODO*////*TODO*///
/*TODO*////*TODO*///	if (is_flakatck)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		num = 0x40;
/*TODO*////*TODO*///		inc = -0x20;
/*TODO*////*TODO*///		source += 0x3f*0x20;
/*TODO*////*TODO*///		offs[0] = 0x0e;
/*TODO*////*TODO*///		offs[1] = 0x0f;
/*TODO*////*TODO*///		offs[2] = 0x06;
/*TODO*////*TODO*///		offs[3] = 0x04;
/*TODO*////*TODO*///		offs[4] = 0x08;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else	/* all others */
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		num = (K007121_ctrlram[chip][0x03] & 0x40) ? 0x80 : 0x40;
/*TODO*////*TODO*///		inc = 5;
/*TODO*////*TODO*///		offs[0] = 0x00;
/*TODO*////*TODO*///		offs[1] = 0x01;
/*TODO*////*TODO*///		offs[2] = 0x02;
/*TODO*////*TODO*///		offs[3] = 0x03;
/*TODO*////*TODO*///		offs[4] = 0x04;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///
/*TODO*////*TODO*///	memset (palette_map, 0, sizeof (palette_map));
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* sprites */
/*TODO*////*TODO*///	for (i = 0;i < num;i++)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int color;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		color = base_color + ((source[offs[1]] & 0xf0) >> 4);
/*TODO*////*TODO*///		palette_map[color] |= 0xffff;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		source += inc;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* now build the final table */
/*TODO*////*TODO*///	for (i = 0; i < 512; i++)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int usage = palette_map[i], j;
/*TODO*////*TODO*///		if (usage)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			for (j = 0; j < 16; j++)
/*TODO*////*TODO*///				if (usage & (1 << j))
/*TODO*////*TODO*///					palette_used_colors[i * 16 + j] |= PALETTE_COLOR_VISIBLE;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///static unsigned char *K007342_ram,*K007342_scroll_ram;
/*TODO*////*TODO*///static int K007342_gfxnum;
/*TODO*////*TODO*///static int K007342_int_enabled;
/*TODO*////*TODO*///static int K007342_flipscreen;
/*TODO*////*TODO*///static int K007342_scrollx[2];
/*TODO*////*TODO*///static int K007342_scrolly[2];
/*TODO*////*TODO*///static unsigned char *K007342_videoram_0,*K007342_colorram_0;
/*TODO*////*TODO*///static unsigned char *K007342_videoram_1,*K007342_colorram_1;
/*TODO*////*TODO*///static int K007342_regs[8];
/*TODO*////*TODO*///static void (*K007342_callback)(int tilemap, int bank, int *code, int *color);
/*TODO*////*TODO*///static struct tilemap *K007342_tilemap[2];
/*TODO*////*TODO*///
/*TODO*////*TODO*////***************************************************************************
/*TODO*////*TODO*///
/*TODO*////*TODO*///  Callbacks for the TileMap code
/*TODO*////*TODO*///
/*TODO*////*TODO*///***************************************************************************/
/*TODO*////*TODO*///
/*TODO*////*TODO*////*
/*TODO*////*TODO*///  data format:
/*TODO*////*TODO*///  video RAM     xxxxxxxx    tile number (bits 0-7)
/*TODO*////*TODO*///  color RAM     x-------    tiles with priority over the sprites
/*TODO*////*TODO*///  color RAM     -x------    depends on external conections
/*TODO*////*TODO*///  color RAM     --x-----    flip Y
/*TODO*////*TODO*///  color RAM     ---x----    flip X
/*TODO*////*TODO*///  color RAM     ----xxxx    depends on external connections (usually color and banking)
/*TODO*////*TODO*///*/
/*TODO*////*TODO*///
/*TODO*////*TODO*///static unsigned char *colorram,*videoram1,*videoram2;
/*TODO*////*TODO*///static int layer;
/*TODO*////*TODO*///
/*TODO*////*TODO*///static void tilemap_0_preupdate(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	colorram = K007342_colorram_0;
/*TODO*////*TODO*///	videoram1 = K007342_videoram_0;
/*TODO*////*TODO*///	layer = 0;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///static void tilemap_1_preupdate(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	colorram = K007342_colorram_1;
/*TODO*////*TODO*///	videoram1 = K007342_videoram_1;
/*TODO*////*TODO*///	layer = 1;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///static void K007342_get_tile_info(int col,int row)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	int tile_index, color, code;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	if (col >= 32)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		col -= 32;
/*TODO*////*TODO*///		tile_index = 0x400 + row*32 + col;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else
/*TODO*////*TODO*///		tile_index = row*32 + col;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	color = colorram[tile_index];
/*TODO*////*TODO*///	code = videoram1[tile_index];
/*TODO*////*TODO*///
/*TODO*////*TODO*///	tile_info.flags = TILE_FLIPYX((color & 0x30) >> 4);
/*TODO*////*TODO*///	tile_info.priority = (color & 0x80) >> 7;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	(*K007342_callback)(layer, K007342_regs[1], &code, &color);
/*TODO*////*TODO*///
/*TODO*////*TODO*///	SET_TILE_INFO(K007342_gfxnum,code,color);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K007342_vh_start(int gfx_index, void (*callback)(int tilemap, int bank, int *code, int *color))
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K007342_gfxnum = gfx_index;
/*TODO*////*TODO*///	K007342_callback = callback;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	K007342_tilemap[0] = tilemap_create(K007342_get_tile_info, TILEMAP_TRANSPARENT, 8, 8, 64, 32);
/*TODO*////*TODO*///	K007342_tilemap[1] = tilemap_create(K007342_get_tile_info, TILEMAP_TRANSPARENT, 8, 8, 64, 32);
/*TODO*////*TODO*///
/*TODO*////*TODO*///	K007342_ram = malloc(0x2000);
/*TODO*////*TODO*///	K007342_scroll_ram = malloc(0x0200);
/*TODO*////*TODO*///
/*TODO*////*TODO*///	if (!K007342_ram || !K007342_scroll_ram || !K007342_tilemap[0] || !K007342_tilemap[1])
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		K007342_vh_stop();
/*TODO*////*TODO*///		return 1;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///
/*TODO*////*TODO*///	memset(K007342_ram,0,0x2000);
/*TODO*////*TODO*///
/*TODO*////*TODO*///	K007342_colorram_0 = &K007342_ram[0x0000];
/*TODO*////*TODO*///	K007342_colorram_1 = &K007342_ram[0x1000];
/*TODO*////*TODO*///	K007342_videoram_0 = &K007342_ram[0x0800];
/*TODO*////*TODO*///	K007342_videoram_1 = &K007342_ram[0x1800];
/*TODO*////*TODO*///
/*TODO*////*TODO*///	K007342_tilemap[0]->transparent_pen = 0;
/*TODO*////*TODO*///	K007342_tilemap[1]->transparent_pen = 0;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	return 0;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K007342_vh_stop(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	free(K007342_ram);
/*TODO*////*TODO*///	K007342_ram = 0;
/*TODO*////*TODO*///	free(K007342_scroll_ram);
/*TODO*////*TODO*///	K007342_scroll_ram = 0;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K007342_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K007342_ram[offset];
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K007342_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	if (offset < 0x1000){		/* layer 0 */
/*TODO*////*TODO*///		if (K007342_ram[offset] != data){
/*TODO*////*TODO*///			if (offset & 0x400)
/*TODO*////*TODO*///				tilemap_mark_tile_dirty(K007342_tilemap[0], offset%32 + 32, (offset&0x3ff)/32);
/*TODO*////*TODO*///			else
/*TODO*////*TODO*///				tilemap_mark_tile_dirty(K007342_tilemap[0], offset%32, (offset&0x3ff)/32);
/*TODO*////*TODO*///			K007342_ram[offset] = data;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else{						/* layer 1 */
/*TODO*////*TODO*///		if (K007342_ram[offset] != data){
/*TODO*////*TODO*///			if (offset & 0x400)
/*TODO*////*TODO*///				tilemap_mark_tile_dirty(K007342_tilemap[1], offset%32 + 32, (offset&0x3ff)/32);
/*TODO*////*TODO*///			else
/*TODO*////*TODO*///				tilemap_mark_tile_dirty(K007342_tilemap[1], offset%32, (offset&0x3ff)/32);
/*TODO*////*TODO*///			K007342_ram[offset] = data;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K007342_scroll_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K007342_scroll_ram[offset];
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K007342_scroll_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K007342_scroll_ram[offset] = data;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K007342_vreg_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	switch(offset)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		case 0x00:
/*TODO*////*TODO*///			/* bit 1: INT control */
/*TODO*////*TODO*///			K007342_int_enabled = data & 0x02;
/*TODO*////*TODO*///			K007342_flipscreen = data & 0x10;
/*TODO*////*TODO*///			tilemap_set_flip(K007342_tilemap[0],K007342_flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
/*TODO*////*TODO*///			tilemap_set_flip(K007342_tilemap[1],K007342_flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
/*TODO*////*TODO*///			break;
/*TODO*////*TODO*///		case 0x01:  /* used for banking in Rock'n'Rage */
/*TODO*////*TODO*///			if (data != K007342_regs[1])
/*TODO*////*TODO*///				tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
/*TODO*////*TODO*///		case 0x02:
/*TODO*////*TODO*///			K007342_scrollx[0] = (K007342_scrollx[0] & 0xff) | ((data & 0x01) << 8);
/*TODO*////*TODO*///			K007342_scrollx[1] = (K007342_scrollx[1] & 0xff) | ((data & 0x02) << 7);
/*TODO*////*TODO*///			break;
/*TODO*////*TODO*///		case 0x03:  /* scroll x (register 0) */
/*TODO*////*TODO*///			K007342_scrollx[0] = (K007342_scrollx[0] & 0x100) | data;
/*TODO*////*TODO*///			break;
/*TODO*////*TODO*///		case 0x04:  /* scroll y (register 0) */
/*TODO*////*TODO*///			K007342_scrolly[0] = data;
/*TODO*////*TODO*///			break;
/*TODO*////*TODO*///		case 0x05:  /* scroll x (register 1) */
/*TODO*////*TODO*///			K007342_scrollx[1] = (K007342_scrollx[1] & 0x100) | data;
/*TODO*////*TODO*///			break;
/*TODO*////*TODO*///		case 0x06:  /* scroll y (register 1) */
/*TODO*////*TODO*///			K007342_scrolly[1] = data;
/*TODO*////*TODO*///		case 0x07:  /* unused */
/*TODO*////*TODO*///			break;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	K007342_regs[offset] = data;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K007342_tilemap_update(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	int offs;
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* update scroll */
/*TODO*////*TODO*///	switch (K007342_regs[2] & 0x1c)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		case 0x00:
/*TODO*////*TODO*///		case 0x08:	/* unknown, blades of steel shootout between periods */
/*TODO*////*TODO*///			tilemap_set_scroll_rows(K007342_tilemap[0],1);
/*TODO*////*TODO*///			tilemap_set_scroll_cols(K007342_tilemap[0],1);
/*TODO*////*TODO*///			tilemap_set_scrollx(K007342_tilemap[0],0,K007342_scrollx[0]);
/*TODO*////*TODO*///			tilemap_set_scrolly(K007342_tilemap[0],0,K007342_scrolly[0]);
/*TODO*////*TODO*///			break;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		case 0x0c:	/* 32 columns */
/*TODO*////*TODO*///			tilemap_set_scroll_rows(K007342_tilemap[0],1);
/*TODO*////*TODO*///			tilemap_set_scroll_cols(K007342_tilemap[0],512);
/*TODO*////*TODO*///			tilemap_set_scrollx(K007342_tilemap[0],0,K007342_scrollx[0]);
/*TODO*////*TODO*///			for (offs = 0;offs < 256;offs++)
/*TODO*////*TODO*///				tilemap_set_scrolly(K007342_tilemap[0],(offs + K007342_scrollx[0]) & 0x1ff,
/*TODO*////*TODO*///						K007342_scroll_ram[2*(offs/8)] + 256 * K007342_scroll_ram[2*(offs/8)+1]);
/*TODO*////*TODO*///			break;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		case 0x14:	/* 256 rows */
/*TODO*////*TODO*///			tilemap_set_scroll_rows(K007342_tilemap[0],256);
/*TODO*////*TODO*///			tilemap_set_scroll_cols(K007342_tilemap[0],1);
/*TODO*////*TODO*///			tilemap_set_scrolly(K007342_tilemap[0],0,K007342_scrolly[0]);
/*TODO*////*TODO*///			for (offs = 0;offs < 256;offs++)
/*TODO*////*TODO*///				tilemap_set_scrollx(K007342_tilemap[0],(offs + K007342_scrolly[0]) & 0xff,
/*TODO*////*TODO*///						K007342_scroll_ram[2*offs] + 256 * K007342_scroll_ram[2*offs+1]);
/*TODO*////*TODO*///			break;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		default:
/*TODO*////*TODO*///usrintf_showmessage("unknown scroll ctrl %02x",K007342_regs[2] & 0x1c);
/*TODO*////*TODO*///			break;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///
/*TODO*////*TODO*///	tilemap_set_scrollx(K007342_tilemap[1],0,K007342_scrollx[1]);
/*TODO*////*TODO*///	tilemap_set_scrolly(K007342_tilemap[1],0,K007342_scrolly[1]);
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* update all layers */
/*TODO*////*TODO*///	tilemap_0_preupdate(); tilemap_update(K007342_tilemap[0]);
/*TODO*////*TODO*///	tilemap_1_preupdate(); tilemap_update(K007342_tilemap[1]);
/*TODO*////*TODO*///
/*TODO*////*TODO*///#if 0
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		static int current_layer = 0;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		if (keyboard_pressed_memory(KEYCODE_Z)) current_layer = !current_layer;
/*TODO*////*TODO*///		tilemap_set_enable(K007342_tilemap[current_layer], 1);
/*TODO*////*TODO*///		tilemap_set_enable(K007342_tilemap[!current_layer], 0);
/*TODO*////*TODO*///
/*TODO*////*TODO*///		usrintf_showmessage("regs:%02x %02x %02x %02x-%02x %02x %02x %02x:%02x",
/*TODO*////*TODO*///			K007342_regs[0], K007342_regs[1], K007342_regs[2], K007342_regs[3],
/*TODO*////*TODO*///			K007342_regs[4], K007342_regs[5], K007342_regs[6], K007342_regs[7],
/*TODO*////*TODO*///			current_layer);
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K007342_tilemap_set_enable(int tilemap, int enable)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	tilemap_set_enable(K007342_tilemap[tilemap], enable);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K007342_tilemap_draw(struct osd_bitmap *bitmap,int num,int flags)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	tilemap_draw(bitmap,K007342_tilemap[num],flags);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K007342_is_INT_enabled(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K007342_int_enabled;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///static struct GfxElement *K007420_gfx;
/*TODO*////*TODO*///static void (*K007420_callback)(int *code,int *color);
/*TODO*////*TODO*///static unsigned char *K007420_ram;
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K007420_vh_start(int gfxnum, void (*callback)(int *code,int *color))
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K007420_gfx = Machine->gfx[gfxnum];
/*TODO*////*TODO*///	K007420_callback = callback;
/*TODO*////*TODO*///	K007420_ram = malloc(0x200);
/*TODO*////*TODO*///	if (!K007420_ram) return 1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	memset(K007420_ram,0,0x200);
/*TODO*////*TODO*///
/*TODO*////*TODO*///	return 0;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K007420_vh_stop(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	free(K007420_ram);
/*TODO*////*TODO*///	K007420_ram = 0;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K007420_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K007420_ram[offset];
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K007420_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K007420_ram[offset] = data;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*////*
/*TODO*////*TODO*/// * Sprite Format
/*TODO*////*TODO*/// * ------------------
/*TODO*////*TODO*/// *
/*TODO*////*TODO*/// * Byte | Bit(s)   | Use
/*TODO*////*TODO*/// * -----+-76543210-+----------------
/*TODO*////*TODO*/// *   0  | xxxxxxxx | y position
/*TODO*////*TODO*/// *   1  | xxxxxxxx | sprite code (low 8 bits)
/*TODO*////*TODO*/// *   2  | xxxxxxxx | depends on external conections. Usually banking
/*TODO*////*TODO*/// *   3  | xxxxxxxx | x position (low 8 bits)
/*TODO*////*TODO*/// *   4  | x------- | x position (high bit)
/*TODO*////*TODO*/// *   4  | -xxx---- | sprite size 000=16x16 001=8x16 010=16x8 011=8x8 100=32x32
/*TODO*////*TODO*/// *   4  | ----x--- | flip y
/*TODO*////*TODO*/// *   4  | -----x-- | flip x
/*TODO*////*TODO*/// *   4  | ------xx | zoom (bits 8 & 9)
/*TODO*////*TODO*/// *   5  | xxxxxxxx | zoom (low 8 bits)  0x080 = normal, < 0x80 enlarge, > 0x80 reduce
/*TODO*////*TODO*/// *   6  | xxxxxxxx | unused
/*TODO*////*TODO*/// *   7  | xxxxxxxx | unused
/*TODO*////*TODO*/// */
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K007420_sprites_draw(struct osd_bitmap *bitmap)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///#define K007420_SPRITERAM_SIZE 0x200
/*TODO*////*TODO*///	int offs;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	for (offs = K007420_SPRITERAM_SIZE - 8; offs >= 0; offs -= 8)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int ox,oy,code,color,flipx,flipy,zoom,w,h,x,y;
/*TODO*////*TODO*///		static int xoffset[4] = { 0, 1, 4, 5 };
/*TODO*////*TODO*///		static int yoffset[4] = { 0, 2, 8, 10 };
/*TODO*////*TODO*///
/*TODO*////*TODO*///		code = K007420_ram[offs+1];
/*TODO*////*TODO*///		color = K007420_ram[offs+2];
/*TODO*////*TODO*///		ox = K007420_ram[offs+3] - ((K007420_ram[offs+4] & 0x80) << 1);
/*TODO*////*TODO*///		oy = 256 - K007420_ram[offs+0];
/*TODO*////*TODO*///		flipx = K007420_ram[offs+4] & 0x04;
/*TODO*////*TODO*///		flipy = K007420_ram[offs+4] & 0x08;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		(*K007420_callback)(&code,&color);
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* kludge for rock'n'rage */
/*TODO*////*TODO*///		if ((K007420_ram[offs+4] == 0x40) && (K007420_ram[offs+1] == 0xff) &&
/*TODO*////*TODO*///			(K007420_ram[offs+2] == 0x00) && (K007420_ram[offs+5] == 0xf0)) continue;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* 0x080 = normal scale, 0x040 = double size, 0x100 half size */
/*TODO*////*TODO*///		zoom = K007420_ram[offs+5] | ((K007420_ram[offs+4] & 0x03) << 8);
/*TODO*////*TODO*///		if (!zoom) continue;
/*TODO*////*TODO*///		zoom = 0x10000 * 128 / zoom;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		switch (K007420_ram[offs+4] & 0x70)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			case 0x30: w = h = 1; break;
/*TODO*////*TODO*///			case 0x20: w = 2; h = 1; code &= (~1); break;
/*TODO*////*TODO*///			case 0x10: w = 1; h = 2; code &= (~2); break;
/*TODO*////*TODO*///			case 0x00: w = h = 2; code &= (~3); break;
/*TODO*////*TODO*///			case 0x40: w = h = 4; code &= (~3); break;
/*TODO*////*TODO*///			default: w = 1; h = 1;
/*TODO*////*TODO*/////if (errorlog) fprintf(errorlog,"Unknown sprite size %02x\n",(K007420_ram[offs+4] & 0x70)>>4);
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///
/*TODO*////*TODO*///		if (K007342_flipscreen)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			ox = 256 - ox - ((zoom * w + (1<<12)) >> 13);
/*TODO*////*TODO*///			oy = 256 - oy - ((zoom * h + (1<<12)) >> 13);
/*TODO*////*TODO*///			flipx = !flipx;
/*TODO*////*TODO*///			flipy = !flipy;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///
/*TODO*////*TODO*///		if (zoom == 0x10000)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			int sx,sy;
/*TODO*////*TODO*///
/*TODO*////*TODO*///			for (y = 0;y < h;y++)
/*TODO*////*TODO*///			{
/*TODO*////*TODO*///				sy = oy + 8 * y;
/*TODO*////*TODO*///
/*TODO*////*TODO*///				for (x = 0;x < w;x++)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					int c = code;
/*TODO*////*TODO*///
/*TODO*////*TODO*///					sx = ox + 8 * x;
/*TODO*////*TODO*///					if (flipx) c += xoffset[(w-1-x)];
/*TODO*////*TODO*///					else c += xoffset[x];
/*TODO*////*TODO*///					if (flipy) c += yoffset[(h-1-y)];
/*TODO*////*TODO*///					else c += yoffset[y];
/*TODO*////*TODO*///
/*TODO*////*TODO*///					drawgfx(bitmap,K007420_gfx,
/*TODO*////*TODO*///						c,
/*TODO*////*TODO*///						color,
/*TODO*////*TODO*///						flipx,flipy,
/*TODO*////*TODO*///						sx,sy,
/*TODO*////*TODO*///						&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
/*TODO*////*TODO*///
/*TODO*////*TODO*///					if (K007342_regs[2] & 0x80)
/*TODO*////*TODO*///						drawgfx(bitmap,K007420_gfx,
/*TODO*////*TODO*///							c,
/*TODO*////*TODO*///							color,
/*TODO*////*TODO*///							flipx,flipy,
/*TODO*////*TODO*///							sx,sy-256,
/*TODO*////*TODO*///							&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///			}
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///		else
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			int sx,sy,zw,zh;
/*TODO*////*TODO*///			for (y = 0;y < h;y++)
/*TODO*////*TODO*///			{
/*TODO*////*TODO*///				sy = oy + ((zoom * y + (1<<12)) >> 13);
/*TODO*////*TODO*///				zh = (oy + ((zoom * (y+1) + (1<<12)) >> 13)) - sy;
/*TODO*////*TODO*///
/*TODO*////*TODO*///				for (x = 0;x < w;x++)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					int c = code;
/*TODO*////*TODO*///
/*TODO*////*TODO*///					sx = ox + ((zoom * x + (1<<12)) >> 13);
/*TODO*////*TODO*///					zw = (ox + ((zoom * (x+1) + (1<<12)) >> 13)) - sx;
/*TODO*////*TODO*///					if (flipx) c += xoffset[(w-1-x)];
/*TODO*////*TODO*///					else c += xoffset[x];
/*TODO*////*TODO*///					if (flipy) c += yoffset[(h-1-y)];
/*TODO*////*TODO*///					else c += yoffset[y];
/*TODO*////*TODO*///
/*TODO*////*TODO*///					drawgfxzoom(bitmap,K007420_gfx,
/*TODO*////*TODO*///						c,
/*TODO*////*TODO*///						color,
/*TODO*////*TODO*///						flipx,flipy,
/*TODO*////*TODO*///						sx,sy,
/*TODO*////*TODO*///						&Machine->drv->visible_area,TRANSPARENCY_PEN,0,
/*TODO*////*TODO*///						(zw << 16) / 8,(zh << 16) / 8);
/*TODO*////*TODO*///
/*TODO*////*TODO*///					if (K007342_regs[2] & 0x80)
/*TODO*////*TODO*///						drawgfxzoom(bitmap,K007420_gfx,
/*TODO*////*TODO*///							c,
/*TODO*////*TODO*///							color,
/*TODO*////*TODO*///							flipx,flipy,
/*TODO*////*TODO*///							sx,sy-256,
/*TODO*////*TODO*///							&Machine->drv->visible_area,TRANSPARENCY_PEN,0,
/*TODO*////*TODO*///							(zw << 16) / 8,(zh << 16) / 8);
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///			}
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///#if 0
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		static int current_sprite = 0;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		if (keyboard_pressed_memory(KEYCODE_Z)) current_sprite = (current_sprite+1) & ((K007420_SPRITERAM_SIZE/8)-1);
/*TODO*////*TODO*///		if (keyboard_pressed_memory(KEYCODE_X)) current_sprite = (current_sprite-1) & ((K007420_SPRITERAM_SIZE/8)-1);
/*TODO*////*TODO*///
/*TODO*////*TODO*///		usrintf_showmessage("%02x:%02x %02x %02x %02x %02x %02x %02x %02x", current_sprite,
/*TODO*////*TODO*///			K007420_ram[(current_sprite*8)+0], K007420_ram[(current_sprite*8)+1],
/*TODO*////*TODO*///			K007420_ram[(current_sprite*8)+2], K007420_ram[(current_sprite*8)+3],
/*TODO*////*TODO*///			K007420_ram[(current_sprite*8)+4], K007420_ram[(current_sprite*8)+5],
/*TODO*////*TODO*///			K007420_ram[(current_sprite*8)+6], K007420_ram[(current_sprite*8)+7]);
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///static int K052109_memory_region;
/*TODO*////*TODO*///static int K052109_gfxnum;
/*TODO*////*TODO*///static void (*K052109_callback)(int tilemap,int bank,int *code,int *color);
/*TODO*////*TODO*///static unsigned char *K052109_ram;
/*TODO*////*TODO*///static unsigned char *K052109_videoram_F,*K052109_videoram2_F,*K052109_colorram_F;
/*TODO*////*TODO*///static unsigned char *K052109_videoram_A,*K052109_videoram2_A,*K052109_colorram_A;
/*TODO*////*TODO*///static unsigned char *K052109_videoram_B,*K052109_videoram2_B,*K052109_colorram_B;
/*TODO*////*TODO*///static unsigned char K052109_charrombank[4];
/*TODO*////*TODO*///static int has_extra_video_ram;
/*TODO*////*TODO*///static int K052109_RMRD_line;
/*TODO*////*TODO*///static int K052109_tileflip_enable;
/*TODO*////*TODO*///static int K052109_irq_enabled;
/*TODO*////*TODO*///static unsigned char K052109_romsubbank,K052109_scrollctrl;
/*TODO*////*TODO*///static struct tilemap *K052109_tilemap[3];
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*////***************************************************************************
/*TODO*////*TODO*///
/*TODO*////*TODO*///  Callbacks for the TileMap code
/*TODO*////*TODO*///
/*TODO*////*TODO*///***************************************************************************/
/*TODO*////*TODO*///
/*TODO*////*TODO*////*
/*TODO*////*TODO*///  data format:
/*TODO*////*TODO*///  video RAM    xxxxxxxx  tile number (low 8 bits)
/*TODO*////*TODO*///  color RAM    xxxx----  depends on external connections (usually color and banking)
/*TODO*////*TODO*///  color RAM    ----xx--  bank select (0-3): these bits are replaced with the 2
/*TODO*////*TODO*///                         bottom bits of the bank register before being placed on
/*TODO*////*TODO*///                         the output pins. The other two bits of the bank register are
/*TODO*////*TODO*///                         placed on the CAB1 and CAB2 output pins.
/*TODO*////*TODO*///  color RAM    ------xx  depends on external connections (usually banking, flip)
/*TODO*////*TODO*///*/
/*TODO*////*TODO*///
/*TODO*////*TODO*///static void tilemap0_preupdate(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	colorram = K052109_colorram_F;
/*TODO*////*TODO*///	videoram1 = K052109_videoram_F;
/*TODO*////*TODO*///	videoram2 = K052109_videoram2_F;
/*TODO*////*TODO*///	layer = 0;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///static void tilemap1_preupdate(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	colorram = K052109_colorram_A;
/*TODO*////*TODO*///	videoram1 = K052109_videoram_A;
/*TODO*////*TODO*///	videoram2 = K052109_videoram2_A;
/*TODO*////*TODO*///	layer = 1;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///static void tilemap2_preupdate(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	colorram = K052109_colorram_B;
/*TODO*////*TODO*///	videoram1 = K052109_videoram_B;
/*TODO*////*TODO*///	videoram2 = K052109_videoram2_B;
/*TODO*////*TODO*///	layer = 2;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///static void K052109_get_tile_info(int col,int row)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	int flipy = 0;
/*TODO*////*TODO*///	int tile_index = 64*row+col;
/*TODO*////*TODO*///	int code = videoram1[tile_index] + 256 * videoram2[tile_index];
/*TODO*////*TODO*///	int color = colorram[tile_index];
/*TODO*////*TODO*///	int bank = K052109_charrombank[(color & 0x0c) >> 2];
/*TODO*////*TODO*///if (has_extra_video_ram) bank = (color & 0x0c) >> 2;	/* kludge for X-Men */
/*TODO*////*TODO*///	color = (color & 0xf3) | ((bank & 0x03) << 2);
/*TODO*////*TODO*///	bank >>= 2;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	flipy = color & 0x02;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	tile_info.flags = 0;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	(*K052109_callback)(layer,bank,&code,&color);
/*TODO*////*TODO*///
/*TODO*////*TODO*///	SET_TILE_INFO(K052109_gfxnum,code,color);
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* if the callback set flip X but it is not enabled, turn it off */
/*TODO*////*TODO*///	if (!(K052109_tileflip_enable & 1)) tile_info.flags &= ~TILE_FLIPX;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* if flip Y is enabled and the attribute but is set, turn it on */
/*TODO*////*TODO*///	if (flipy && (K052109_tileflip_enable & 2)) tile_info.flags |= TILE_FLIPY;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K052109_vh_start(int gfx_memory_region,int plane0,int plane1,int plane2,int plane3,
/*TODO*////*TODO*///		void (*callback)(int tilemap,int bank,int *code,int *color))
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	int gfx_index;
/*TODO*////*TODO*///	static struct GfxLayout charlayout =
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		8,8,
/*TODO*////*TODO*///		0,				/* filled in later */
/*TODO*////*TODO*///		4,
/*TODO*////*TODO*///		{ 0, 0, 0, 0 },	/* filled in later */
/*TODO*////*TODO*///		{ 0, 1, 2, 3, 4, 5, 6, 7 },
/*TODO*////*TODO*///		{ 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
/*TODO*////*TODO*///		32*8
/*TODO*////*TODO*///	};
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* find first empty slot to decode gfx */
/*TODO*////*TODO*///	for (gfx_index = 0; gfx_index < MAX_GFX_ELEMENTS; gfx_index++)
/*TODO*////*TODO*///		if (Machine->gfx[gfx_index] == 0)
/*TODO*////*TODO*///			break;
/*TODO*////*TODO*///	if (gfx_index == MAX_GFX_ELEMENTS)
/*TODO*////*TODO*///		return 1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* tweak the structure for the number of tiles we have */
/*TODO*////*TODO*///	charlayout.total = memory_region_length(gfx_memory_region) / 32;
/*TODO*////*TODO*///	charlayout.planeoffset[0] = plane3 * 8;
/*TODO*////*TODO*///	charlayout.planeoffset[1] = plane2 * 8;
/*TODO*////*TODO*///	charlayout.planeoffset[2] = plane1 * 8;
/*TODO*////*TODO*///	charlayout.planeoffset[3] = plane0 * 8;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* decode the graphics */
/*TODO*////*TODO*///	Machine->gfx[gfx_index] = decodegfx(memory_region(gfx_memory_region),&charlayout);
/*TODO*////*TODO*///	if (!Machine->gfx[gfx_index])
/*TODO*////*TODO*///		return 1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* set the color information */
/*TODO*////*TODO*///	Machine->gfx[gfx_index]->colortable = Machine->remapped_colortable;
/*TODO*////*TODO*///	Machine->gfx[gfx_index]->total_colors = Machine->drv->color_table_len / 16;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	K052109_memory_region = gfx_memory_region;
/*TODO*////*TODO*///	K052109_gfxnum = gfx_index;
/*TODO*////*TODO*///	K052109_callback = callback;
/*TODO*////*TODO*///	K052109_RMRD_line = CLEAR_LINE;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	has_extra_video_ram = 0;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	K052109_tilemap[0] = tilemap_create(K052109_get_tile_info,TILEMAP_TRANSPARENT,8,8,64,32);
/*TODO*////*TODO*///	K052109_tilemap[1] = tilemap_create(K052109_get_tile_info,TILEMAP_TRANSPARENT,8,8,64,32);
/*TODO*////*TODO*///	K052109_tilemap[2] = tilemap_create(K052109_get_tile_info,TILEMAP_TRANSPARENT,8,8,64,32);
/*TODO*////*TODO*///
/*TODO*////*TODO*///	K052109_ram = malloc(0x6000);
/*TODO*////*TODO*///
/*TODO*////*TODO*///	if (!K052109_ram || !K052109_tilemap[0] || !K052109_tilemap[1] || !K052109_tilemap[2])
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		K052109_vh_stop();
/*TODO*////*TODO*///		return 1;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///
/*TODO*////*TODO*///	memset(K052109_ram,0,0x6000);
/*TODO*////*TODO*///
/*TODO*////*TODO*///	K052109_colorram_F = &K052109_ram[0x0000];
/*TODO*////*TODO*///	K052109_colorram_A = &K052109_ram[0x0800];
/*TODO*////*TODO*///	K052109_colorram_B = &K052109_ram[0x1000];
/*TODO*////*TODO*///	K052109_videoram_F = &K052109_ram[0x2000];
/*TODO*////*TODO*///	K052109_videoram_A = &K052109_ram[0x2800];
/*TODO*////*TODO*///	K052109_videoram_B = &K052109_ram[0x3000];
/*TODO*////*TODO*///	K052109_videoram2_F = &K052109_ram[0x4000];
/*TODO*////*TODO*///	K052109_videoram2_A = &K052109_ram[0x4800];
/*TODO*////*TODO*///	K052109_videoram2_B = &K052109_ram[0x5000];
/*TODO*////*TODO*///
/*TODO*////*TODO*///	K052109_tilemap[0]->transparent_pen = 0;
/*TODO*////*TODO*///	K052109_tilemap[1]->transparent_pen = 0;
/*TODO*////*TODO*///	K052109_tilemap[2]->transparent_pen = 0;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	return 0;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K052109_vh_stop(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	free(K052109_ram);
/*TODO*////*TODO*///	K052109_ram = 0;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K052109_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	if (K052109_RMRD_line == CLEAR_LINE)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		if ((offset & 0x1fff) >= 0x1800)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			if (offset >= 0x180c && offset < 0x1834)
/*TODO*////*TODO*///			{	/* A y scroll */	}
/*TODO*////*TODO*///			else if (offset >= 0x1a00 && offset < 0x1c00)
/*TODO*////*TODO*///			{	/* A x scroll */	}
/*TODO*////*TODO*///			else if (offset == 0x1d00)
/*TODO*////*TODO*///			{	/* read for bitwise operations before writing */	}
/*TODO*////*TODO*///			else if (offset >= 0x380c && offset < 0x3834)
/*TODO*////*TODO*///			{	/* B y scroll */	}
/*TODO*////*TODO*///			else if (offset >= 0x3a00 && offset < 0x3c00)
/*TODO*////*TODO*///			{	/* B x scroll */	}
/*TODO*////*TODO*///			else
/*TODO*////*TODO*///if (errorlog) fprintf(errorlog,"%04x: read from unknown 052109 address %04x\n",cpu_get_pc(),offset);
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///
/*TODO*////*TODO*///		return K052109_ram[offset];
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else	/* Punk Shot and TMNT read from 0000-1fff, Aliens from 2000-3fff */
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int code = (offset & 0x1fff) >> 5;
/*TODO*////*TODO*///		int color = K052109_romsubbank;
/*TODO*////*TODO*///		int bank = K052109_charrombank[(color & 0x0c) >> 2] >> 2;   /* discard low bits (TMNT) */
/*TODO*////*TODO*///		int addr;
/*TODO*////*TODO*///
/*TODO*////*TODO*///if (has_extra_video_ram) code |= color << 8;	/* kludge for X-Men */
/*TODO*////*TODO*///else
/*TODO*////*TODO*///		(*K052109_callback)(0,bank,&code,&color);
/*TODO*////*TODO*///
/*TODO*////*TODO*///		addr = (code << 5) + (offset & 0x1f);
/*TODO*////*TODO*///		addr &= memory_region_length(K052109_memory_region)-1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///#if 0
/*TODO*////*TODO*///	usrintf_showmessage("%04x: off%04x sub%02x (bnk%x) adr%06x",cpu_get_pc(),offset,K052109_romsubbank,bank,addr);
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///
/*TODO*////*TODO*///		return memory_region(K052109_memory_region)[addr];
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K052109_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	if ((offset & 0x1fff) < 0x1800) /* tilemap RAM */
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		if (K052109_ram[offset] != data)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			if (offset >= 0x4000) has_extra_video_ram = 1;  /* kludge for X-Men */
/*TODO*////*TODO*///			K052109_ram[offset] = data;
/*TODO*////*TODO*///			tilemap_mark_tile_dirty(K052109_tilemap[(offset&0x1fff)/0x800],offset%64,(offset%0x800)/64);
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else	/* control registers */
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		K052109_ram[offset] = data;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		if (offset >= 0x180c && offset < 0x1834)
/*TODO*////*TODO*///		{	/* A y scroll */	}
/*TODO*////*TODO*///		else if (offset >= 0x1a00 && offset < 0x1c00)
/*TODO*////*TODO*///		{	/* A x scroll */	}
/*TODO*////*TODO*///		else if (offset == 0x1c80)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///if (K052109_scrollctrl != data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///#if 0
/*TODO*////*TODO*///usrintf_showmessage("scrollcontrol = %02x",data);
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///if (errorlog) fprintf(errorlog,"%04x: rowscrollcontrol = %02x\n",cpu_get_pc(),data);
/*TODO*////*TODO*///			K052109_scrollctrl = data;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///		else if (offset == 0x1d00)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///#if VERBOSE
/*TODO*////*TODO*///if (errorlog) fprintf(errorlog,"%04x: 052109 register 1d00 = %02x\n",cpu_get_pc(),data);
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///			/* bit 2 = irq enable */
/*TODO*////*TODO*///			/* the custom chip can also generate NMI and FIRQ, for use with a 6809 */
/*TODO*////*TODO*///			K052109_irq_enabled = data & 0x04;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///		else if (offset == 0x1d80)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			int dirty = 0;
/*TODO*////*TODO*///
/*TODO*////*TODO*///			if (K052109_charrombank[0] != (data & 0x0f)) dirty |= 1;
/*TODO*////*TODO*///			if (K052109_charrombank[1] != ((data >> 4) & 0x0f)) dirty |= 2;
/*TODO*////*TODO*///			if (dirty)
/*TODO*////*TODO*///			{
/*TODO*////*TODO*///				int i;
/*TODO*////*TODO*///
/*TODO*////*TODO*///				K052109_charrombank[0] = data & 0x0f;
/*TODO*////*TODO*///				K052109_charrombank[1] = (data >> 4) & 0x0f;
/*TODO*////*TODO*///
/*TODO*////*TODO*///				for (i = 0;i < 0x1800;i++)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					int bank = (K052109_ram[i]&0x0c) >> 2;
/*TODO*////*TODO*///					if ((bank == 0 && (dirty & 1)) || (bank == 1 && dirty & 2))
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						tilemap_mark_tile_dirty(K052109_tilemap[(i&0x1fff)/0x800],i%64,(i%0x800)/64);
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///			}
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///		else if (offset == 0x1e00)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///if (errorlog) fprintf(errorlog,"%04x: 052109 register 1e00 = %02x\n",cpu_get_pc(),data);
/*TODO*////*TODO*///			K052109_romsubbank = data;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///		else if (offset == 0x1e80)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///if (errorlog && (data & 0xfe)) fprintf(errorlog,"%04x: 052109 register 1e80 = %02x\n",cpu_get_pc(),data);
/*TODO*////*TODO*///			tilemap_set_flip(K052109_tilemap[0],(data & 1) ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
/*TODO*////*TODO*///			tilemap_set_flip(K052109_tilemap[1],(data & 1) ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
/*TODO*////*TODO*///			tilemap_set_flip(K052109_tilemap[2],(data & 1) ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
/*TODO*////*TODO*///			if (K052109_tileflip_enable != ((data & 0x06) >> 1))
/*TODO*////*TODO*///			{
/*TODO*////*TODO*///				K052109_tileflip_enable = ((data & 0x06) >> 1);
/*TODO*////*TODO*///
/*TODO*////*TODO*///				tilemap_mark_all_tiles_dirty(K052109_tilemap[0]);
/*TODO*////*TODO*///				tilemap_mark_all_tiles_dirty(K052109_tilemap[1]);
/*TODO*////*TODO*///				tilemap_mark_all_tiles_dirty(K052109_tilemap[2]);
/*TODO*////*TODO*///			}
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///		else if (offset == 0x1f00)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			int dirty = 0;
/*TODO*////*TODO*///
/*TODO*////*TODO*///			if (K052109_charrombank[2] != (data & 0x0f)) dirty |= 1;
/*TODO*////*TODO*///			if (K052109_charrombank[3] != ((data >> 4) & 0x0f)) dirty |= 2;
/*TODO*////*TODO*///			if (dirty)
/*TODO*////*TODO*///			{
/*TODO*////*TODO*///				int i;
/*TODO*////*TODO*///
/*TODO*////*TODO*///				K052109_charrombank[2] = data & 0x0f;
/*TODO*////*TODO*///				K052109_charrombank[3] = (data >> 4) & 0x0f;
/*TODO*////*TODO*///
/*TODO*////*TODO*///				for (i = 0;i < 0x1800;i++)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					int bank = (K052109_ram[i] & 0x0c) >> 2;
/*TODO*////*TODO*///					if ((bank == 2 && (dirty & 1)) || (bank == 3 && dirty & 2))
/*TODO*////*TODO*///						tilemap_mark_tile_dirty(K052109_tilemap[(i&0x1fff)/0x800],i%64,(i%0x800)/64);
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///			}
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///		else if (offset >= 0x380c && offset < 0x3834)
/*TODO*////*TODO*///		{	/* B y scroll */	}
/*TODO*////*TODO*///		else if (offset >= 0x3a00 && offset < 0x3c00)
/*TODO*////*TODO*///		{	/* B x scroll */	}
/*TODO*////*TODO*///		else
/*TODO*////*TODO*///if (errorlog) fprintf(errorlog,"%04x: write %02x to unknown 052109 address %04x\n",cpu_get_pc(),data,offset);
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K052109_set_RMRD_line(int state)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K052109_RMRD_line = state;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K052109_tilemap_update(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///#if 0
/*TODO*////*TODO*///{
/*TODO*////*TODO*///usrintf_showmessage("%x %x %x %x",
/*TODO*////*TODO*///	K052109_charrombank[0],
/*TODO*////*TODO*///	K052109_charrombank[1],
/*TODO*////*TODO*///	K052109_charrombank[2],
/*TODO*////*TODO*///	K052109_charrombank[3]);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///	if ((K052109_scrollctrl & 0x03) == 0x02)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int xscroll,yscroll,offs;
/*TODO*////*TODO*///		unsigned char *scrollram = &K052109_ram[0x1a00];
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///		tilemap_set_scroll_rows(K052109_tilemap[1],256);
/*TODO*////*TODO*///		tilemap_set_scroll_cols(K052109_tilemap[1],1);
/*TODO*////*TODO*///		yscroll = K052109_ram[0x180c];
/*TODO*////*TODO*///		tilemap_set_scrolly(K052109_tilemap[1],0,yscroll);
/*TODO*////*TODO*///		for (offs = 0;offs < 256;offs++)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			xscroll = scrollram[2*(offs&0xfff8)+0] + 256 * scrollram[2*(offs&0xfff8)+1];
/*TODO*////*TODO*///			xscroll -= 6;
/*TODO*////*TODO*///			tilemap_set_scrollx(K052109_tilemap[1],(offs+yscroll)&0xff,xscroll);
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else if ((K052109_scrollctrl & 0x03) == 0x03)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int xscroll,yscroll,offs;
/*TODO*////*TODO*///		unsigned char *scrollram = &K052109_ram[0x1a00];
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///		tilemap_set_scroll_rows(K052109_tilemap[1],256);
/*TODO*////*TODO*///		tilemap_set_scroll_cols(K052109_tilemap[1],1);
/*TODO*////*TODO*///		yscroll = K052109_ram[0x180c];
/*TODO*////*TODO*///		tilemap_set_scrolly(K052109_tilemap[1],0,yscroll);
/*TODO*////*TODO*///		for (offs = 0;offs < 256;offs++)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			xscroll = scrollram[2*offs+0] + 256 * scrollram[2*offs+1];
/*TODO*////*TODO*///			xscroll -= 6;
/*TODO*////*TODO*///			tilemap_set_scrollx(K052109_tilemap[1],(offs+yscroll)&0xff,xscroll);
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else if ((K052109_scrollctrl & 0x04) == 0x04)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int xscroll,yscroll,offs;
/*TODO*////*TODO*///		unsigned char *scrollram = &K052109_ram[0x1800];
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///		tilemap_set_scroll_rows(K052109_tilemap[1],1);
/*TODO*////*TODO*///		tilemap_set_scroll_cols(K052109_tilemap[1],512);
/*TODO*////*TODO*///		xscroll = K052109_ram[0x1a00] + 256 * K052109_ram[0x1a01];
/*TODO*////*TODO*///		xscroll -= 6;
/*TODO*////*TODO*///		tilemap_set_scrollx(K052109_tilemap[1],0,xscroll);
/*TODO*////*TODO*///		for (offs = 0;offs < 512;offs++)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			yscroll = scrollram[offs/8];
/*TODO*////*TODO*///			tilemap_set_scrolly(K052109_tilemap[1],(offs+xscroll)&0x1ff,yscroll);
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int xscroll,yscroll;
/*TODO*////*TODO*///		unsigned char *scrollram = &K052109_ram[0x1a00];
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///		tilemap_set_scroll_rows(K052109_tilemap[1],1);
/*TODO*////*TODO*///		tilemap_set_scroll_cols(K052109_tilemap[1],1);
/*TODO*////*TODO*///		xscroll = scrollram[0] + 256 * scrollram[1];
/*TODO*////*TODO*///		xscroll -= 6;
/*TODO*////*TODO*///		yscroll = K052109_ram[0x180c];
/*TODO*////*TODO*///		tilemap_set_scrollx(K052109_tilemap[1],0,xscroll);
/*TODO*////*TODO*///		tilemap_set_scrolly(K052109_tilemap[1],0,yscroll);
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///
/*TODO*////*TODO*///	if ((K052109_scrollctrl & 0x18) == 0x10)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int xscroll,yscroll,offs;
/*TODO*////*TODO*///		unsigned char *scrollram = &K052109_ram[0x3a00];
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///		tilemap_set_scroll_rows(K052109_tilemap[2],256);
/*TODO*////*TODO*///		tilemap_set_scroll_cols(K052109_tilemap[2],1);
/*TODO*////*TODO*///		yscroll = K052109_ram[0x380c];
/*TODO*////*TODO*///		tilemap_set_scrolly(K052109_tilemap[2],0,yscroll);
/*TODO*////*TODO*///		for (offs = 0;offs < 256;offs++)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			xscroll = scrollram[2*(offs&0xfff8)+0] + 256 * scrollram[2*(offs&0xfff8)+1];
/*TODO*////*TODO*///			xscroll -= 6;
/*TODO*////*TODO*///			tilemap_set_scrollx(K052109_tilemap[2],(offs+yscroll)&0xff,xscroll);
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else if ((K052109_scrollctrl & 0x18) == 0x18)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int xscroll,yscroll,offs;
/*TODO*////*TODO*///		unsigned char *scrollram = &K052109_ram[0x3a00];
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///		tilemap_set_scroll_rows(K052109_tilemap[2],256);
/*TODO*////*TODO*///		tilemap_set_scroll_cols(K052109_tilemap[2],1);
/*TODO*////*TODO*///		yscroll = K052109_ram[0x380c];
/*TODO*////*TODO*///		tilemap_set_scrolly(K052109_tilemap[2],0,yscroll);
/*TODO*////*TODO*///		for (offs = 0;offs < 256;offs++)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			xscroll = scrollram[2*offs+0] + 256 * scrollram[2*offs+1];
/*TODO*////*TODO*///			xscroll -= 6;
/*TODO*////*TODO*///			tilemap_set_scrollx(K052109_tilemap[2],(offs+yscroll)&0xff,xscroll);
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else if ((K052109_scrollctrl & 0x20) == 0x20)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int xscroll,yscroll,offs;
/*TODO*////*TODO*///		unsigned char *scrollram = &K052109_ram[0x3800];
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///		tilemap_set_scroll_rows(K052109_tilemap[2],1);
/*TODO*////*TODO*///		tilemap_set_scroll_cols(K052109_tilemap[2],512);
/*TODO*////*TODO*///		xscroll = K052109_ram[0x3a00] + 256 * K052109_ram[0x3a01];
/*TODO*////*TODO*///		xscroll -= 6;
/*TODO*////*TODO*///		tilemap_set_scrollx(K052109_tilemap[2],0,xscroll);
/*TODO*////*TODO*///		for (offs = 0;offs < 512;offs++)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			yscroll = scrollram[offs/8];
/*TODO*////*TODO*///			tilemap_set_scrolly(K052109_tilemap[2],(offs+xscroll)&0x1ff,yscroll);
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int xscroll,yscroll;
/*TODO*////*TODO*///		unsigned char *scrollram = &K052109_ram[0x3a00];
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///		tilemap_set_scroll_rows(K052109_tilemap[2],1);
/*TODO*////*TODO*///		tilemap_set_scroll_cols(K052109_tilemap[2],1);
/*TODO*////*TODO*///		xscroll = scrollram[0] + 256 * scrollram[1];
/*TODO*////*TODO*///		xscroll -= 6;
/*TODO*////*TODO*///		yscroll = K052109_ram[0x380c];
/*TODO*////*TODO*///		tilemap_set_scrollx(K052109_tilemap[2],0,xscroll);
/*TODO*////*TODO*///		tilemap_set_scrolly(K052109_tilemap[2],0,yscroll);
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///
/*TODO*////*TODO*///	tilemap0_preupdate(); tilemap_update(K052109_tilemap[0]);
/*TODO*////*TODO*///	tilemap1_preupdate(); tilemap_update(K052109_tilemap[1]);
/*TODO*////*TODO*///	tilemap2_preupdate(); tilemap_update(K052109_tilemap[2]);
/*TODO*////*TODO*///
/*TODO*////*TODO*///#ifdef MAME_DEBUG
/*TODO*////*TODO*///if ((K052109_scrollctrl & 0x03) == 0x01 ||
/*TODO*////*TODO*///		(K052109_scrollctrl & 0x18) == 0x08 ||
/*TODO*////*TODO*///		((K052109_scrollctrl & 0x04) && (K052109_scrollctrl & 0x03)) ||
/*TODO*////*TODO*///		((K052109_scrollctrl & 0x20) && (K052109_scrollctrl & 0x18)) ||
/*TODO*////*TODO*///		(K052109_scrollctrl & 0xc0) != 0)
/*TODO*////*TODO*///	usrintf_showmessage("scrollcontrol = %02x",K052109_scrollctrl);
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///
/*TODO*////*TODO*///#if 0
/*TODO*////*TODO*///if (keyboard_pressed(KEYCODE_F))
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	FILE *fp;
/*TODO*////*TODO*///	fp=fopen("TILE.DMP", "w+b");
/*TODO*////*TODO*///	if (fp)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		fwrite(K052109_ram, 0x6000, 1, fp);
/*TODO*////*TODO*///		usrintf_showmessage("saved");
/*TODO*////*TODO*///		fclose(fp);
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///}
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K052109_tilemap_draw(struct osd_bitmap *bitmap,int num,int flags)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	tilemap_draw(bitmap,K052109_tilemap[num],flags);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K052109_is_IRQ_enabled(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K052109_irq_enabled;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///static int K051960_memory_region;
/*TODO*////*TODO*///static struct GfxElement *K051960_gfx;
/*TODO*////*TODO*///static void (*K051960_callback)(int *code,int *color,int *priority);
/*TODO*////*TODO*///static int K051960_romoffset;
/*TODO*////*TODO*///static int K051960_spriteflip,K051960_readroms;
/*TODO*////*TODO*///static unsigned char K051960_spriterombank[3];
/*TODO*////*TODO*///static unsigned char *K051960_ram;
/*TODO*////*TODO*///static int K051960_irq_enabled, K051960_nmi_enabled;
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K051960_vh_start(int gfx_memory_region,int plane0,int plane1,int plane2,int plane3,
/*TODO*////*TODO*///		void (*callback)(int *code,int *color,int *priority))
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	int gfx_index;
/*TODO*////*TODO*///	static struct GfxLayout spritelayout =
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		16,16,
/*TODO*////*TODO*///		0,				/* filled in later */
/*TODO*////*TODO*///		4,
/*TODO*////*TODO*///		{ 0, 0, 0, 0 },	/* filled in later */
/*TODO*////*TODO*///		{ 0, 1, 2, 3, 4, 5, 6, 7,
/*TODO*////*TODO*///				8*32+0, 8*32+1, 8*32+2, 8*32+3, 8*32+4, 8*32+5, 8*32+6, 8*32+7 },
/*TODO*////*TODO*///		{ 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32,
/*TODO*////*TODO*///				16*32, 17*32, 18*32, 19*32, 20*32, 21*32, 22*32, 23*32 },
/*TODO*////*TODO*///		128*8
/*TODO*////*TODO*///	};
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* find first empty slot to decode gfx */
/*TODO*////*TODO*///	for (gfx_index = 0; gfx_index < MAX_GFX_ELEMENTS; gfx_index++)
/*TODO*////*TODO*///		if (Machine->gfx[gfx_index] == 0)
/*TODO*////*TODO*///			break;
/*TODO*////*TODO*///	if (gfx_index == MAX_GFX_ELEMENTS)
/*TODO*////*TODO*///		return 1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* tweak the structure for the number of tiles we have */
/*TODO*////*TODO*///	spritelayout.total = memory_region_length(gfx_memory_region) / 128;
/*TODO*////*TODO*///	spritelayout.planeoffset[0] = plane0 * 8;
/*TODO*////*TODO*///	spritelayout.planeoffset[1] = plane1 * 8;
/*TODO*////*TODO*///	spritelayout.planeoffset[2] = plane2 * 8;
/*TODO*////*TODO*///	spritelayout.planeoffset[3] = plane3 * 8;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* decode the graphics */
/*TODO*////*TODO*///	Machine->gfx[gfx_index] = decodegfx(memory_region(gfx_memory_region),&spritelayout);
/*TODO*////*TODO*///	if (!Machine->gfx[gfx_index])
/*TODO*////*TODO*///		return 1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* set the color information */
/*TODO*////*TODO*///	Machine->gfx[gfx_index]->colortable = Machine->remapped_colortable;
/*TODO*////*TODO*///	Machine->gfx[gfx_index]->total_colors = Machine->drv->color_table_len / 16;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	K051960_memory_region = gfx_memory_region;
/*TODO*////*TODO*///	K051960_gfx = Machine->gfx[gfx_index];
/*TODO*////*TODO*///	K051960_callback = callback;
/*TODO*////*TODO*///	K051960_ram = malloc(0x400);
/*TODO*////*TODO*///	if (!K051960_ram) return 1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	memset(K051960_ram,0,0x400);
/*TODO*////*TODO*///
/*TODO*////*TODO*///	return 0;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051960_vh_stop(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	free(K051960_ram);
/*TODO*////*TODO*///	K051960_ram = 0;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///static int K051960_fetchromdata(int byte)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	int code,color,pri,off1,addr;
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///	addr = K051960_romoffset + (K051960_spriterombank[0] << 8) +
/*TODO*////*TODO*///			((K051960_spriterombank[1] & 0x03) << 16);
/*TODO*////*TODO*///	code = (addr & 0x3ffe0) >> 5;
/*TODO*////*TODO*///	off1 = addr & 0x1f;
/*TODO*////*TODO*///	color = ((K051960_spriterombank[1] & 0xfc) >> 2) + ((K051960_spriterombank[2] & 0x03) << 6);
/*TODO*////*TODO*///	pri = 0;
/*TODO*////*TODO*///	(*K051960_callback)(&code,&color,&pri);
/*TODO*////*TODO*///
/*TODO*////*TODO*///	addr = (code << 7) | (off1 << 2) | byte;
/*TODO*////*TODO*///	addr &= memory_region_length(K051960_memory_region)-1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///#if 0
/*TODO*////*TODO*///	usrintf_showmessage("%04x: addr %06x",cpu_get_pc(),addr);
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///
/*TODO*////*TODO*///	return memory_region(K051960_memory_region)[addr];
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K051960_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	if (K051960_readroms)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		/* the 051960 remembers the last address read and uses it when reading the sprite ROMs */
/*TODO*////*TODO*///		K051960_romoffset = (offset & 0x3fc) >> 2;
/*TODO*////*TODO*///		return K051960_fetchromdata(offset & 3);	/* only 88 Games reads the ROMs from here */
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else
/*TODO*////*TODO*///		return K051960_ram[offset];
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051960_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K051960_ram[offset] = data;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K051960_word_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K051960_r(offset + 1) | (K051960_r(offset) << 8);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051960_word_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	if ((data & 0xff000000) == 0)
/*TODO*////*TODO*///		K051960_w(offset,(data >> 8) & 0xff);
/*TODO*////*TODO*///	if ((data & 0x00ff0000) == 0)
/*TODO*////*TODO*///		K051960_w(offset + 1,data & 0xff);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K051937_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	if (K051960_readroms && offset >= 4 && offset < 8)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		return K051960_fetchromdata(offset & 3);
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		if (offset == 0)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			static int counter;
/*TODO*////*TODO*///
/*TODO*////*TODO*///			/* some games need bit 0 to pulse */
/*TODO*////*TODO*///			return (counter++) & 1;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///if (errorlog) fprintf(errorlog,"%04x: read unknown 051937 address %x\n",cpu_get_pc(),offset);
/*TODO*////*TODO*///		return 0;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051937_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	if (offset == 0)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///#ifdef MAME_DEBUG
/*TODO*////*TODO*///if (data & 0xc6)
/*TODO*////*TODO*///	usrintf_showmessage("051937 reg 00 = %02x",data);
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///		/* bit 0 is IRQ enable */
/*TODO*////*TODO*///		K051960_irq_enabled = (data & 0x01);
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* bit 1: probably FIRQ enable */
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* bit 2 is NMI enable */
/*TODO*////*TODO*///		K051960_nmi_enabled = (data & 0x04);
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* bit 3 = flip screen */
/*TODO*////*TODO*///		K051960_spriteflip = data & 0x08;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* bit 4 used by Devastators and TMNT, unknown */
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* bit 5 = enable gfx ROM reading */
/*TODO*////*TODO*///		K051960_readroms = data & 0x20;
/*TODO*////*TODO*///#if VERBOSE
/*TODO*////*TODO*///if (errorlog) fprintf(errorlog,"%04x: write %02x to 051937 address %x\n",cpu_get_pc(),data,offset);
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else if (offset >= 2 && offset < 5)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		K051960_spriterombank[offset - 2] = data;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///#if 0
/*TODO*////*TODO*///	usrintf_showmessage("%04x: write %02x to 051937 address %x",cpu_get_pc(),data,offset);
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///if (errorlog) fprintf(errorlog,"%04x: write %02x to unknown 051937 address %x\n",cpu_get_pc(),data,offset);
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K051937_word_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K051937_r(offset + 1) | (K051937_r(offset) << 8);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051937_word_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	if ((data & 0xff000000) == 0)
/*TODO*////*TODO*///		K051937_w(offset,(data >> 8) & 0xff);
/*TODO*////*TODO*///	if ((data & 0x00ff0000) == 0)
/*TODO*////*TODO*///		K051937_w(offset + 1,data & 0xff);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*////*
/*TODO*////*TODO*/// * Sprite Format
/*TODO*////*TODO*/// * ------------------
/*TODO*////*TODO*/// *
/*TODO*////*TODO*/// * Byte | Bit(s)   | Use
/*TODO*////*TODO*/// * -----+-76543210-+----------------
/*TODO*////*TODO*/// *   0  | x------- | active (show this sprite)
/*TODO*////*TODO*/// *   0  | -xxxxxxx | priority order
/*TODO*////*TODO*/// *   1  | xxx----- | sprite size (see below)
/*TODO*////*TODO*/// *   1  | ---xxxxx | sprite code (high 5 bits)
/*TODO*////*TODO*/// *   2  | xxxxxxxx | sprite code (low 8 bits)
/*TODO*////*TODO*/// *   3  | xxxxxxxx | "color", but depends on external connections (see below)
/*TODO*////*TODO*/// *   4  | xxxxxx-- | zoom y (0 = normal, >0 = shrink)
/*TODO*////*TODO*/// *   4  | ------x- | flip y
/*TODO*////*TODO*/// *   4  | -------x | y position (high bit)
/*TODO*////*TODO*/// *   5  | xxxxxxxx | y position (low 8 bits)
/*TODO*////*TODO*/// *   6  | xxxxxx-- | zoom x (0 = normal, >0 = shrink)
/*TODO*////*TODO*/// *   6  | ------x- | flip x
/*TODO*////*TODO*/// *   6  | -------x | x position (high bit)
/*TODO*////*TODO*/// *   7  | xxxxxxxx | x position (low 8 bits)
/*TODO*////*TODO*/// *
/*TODO*////*TODO*/// * Example of "color" field for Punk Shot:
/*TODO*////*TODO*/// *   3  | x------- | shadow
/*TODO*////*TODO*/// *   3  | -xx----- | priority
/*TODO*////*TODO*/// *   3  | ---x---- | use second gfx ROM bank
/*TODO*////*TODO*/// *   3  | ----xxxx | color code
/*TODO*////*TODO*/// *
/*TODO*////*TODO*/// * shadow enables transparent shadows. Note that it applies to pen 0x0f ONLY.
/*TODO*////*TODO*/// * The rest of the sprite remains normal.
/*TODO*////*TODO*/// * Note that Aliens also uses the shadow bit to select the second sprite bank.
/*TODO*////*TODO*/// */
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051960_sprites_draw(struct osd_bitmap *bitmap,int min_priority,int max_priority)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///#define NUM_SPRITES 128
/*TODO*////*TODO*///	int offs,pri_code;
/*TODO*////*TODO*///	int sortedlist[NUM_SPRITES];
/*TODO*////*TODO*///
/*TODO*////*TODO*///	for (offs = 0;offs < NUM_SPRITES;offs++)
/*TODO*////*TODO*///		sortedlist[offs] = -1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* prebuild a sorted table */
/*TODO*////*TODO*///	for (offs = 0;offs < 0x400;offs += 8)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		if (K051960_ram[offs] & 0x80)
/*TODO*////*TODO*///			sortedlist[K051960_ram[offs] & 0x7f] = offs;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///
/*TODO*////*TODO*///	for (pri_code = 0;pri_code < NUM_SPRITES;pri_code++)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int ox,oy,code,color,pri,size,w,h,x,y,flipx,flipy,zoomx,zoomy;
/*TODO*////*TODO*///		/* sprites can be grouped up to 8x8. The draw order is
/*TODO*////*TODO*///			 0  1  4  5 16 17 20 21
/*TODO*////*TODO*///			 2  3  6  7 18 19 22 23
/*TODO*////*TODO*///			 8  9 12 13 24 25 28 29
/*TODO*////*TODO*///			10 11 14 15 26 27 30 31
/*TODO*////*TODO*///			32 33 36 37 48 49 52 53
/*TODO*////*TODO*///			34 35 38 39 50 51 54 55
/*TODO*////*TODO*///			40 41 44 45 56 57 60 61
/*TODO*////*TODO*///			42 43 46 47 58 59 62 63
/*TODO*////*TODO*///		*/
/*TODO*////*TODO*///		static int xoffset[8] = { 0, 1, 4, 5, 16, 17, 20, 21 };
/*TODO*////*TODO*///		static int yoffset[8] = { 0, 2, 8, 10, 32, 34, 40, 42 };
/*TODO*////*TODO*///		static int width[8] =  { 1, 2, 1, 2, 4, 2, 4, 8 };
/*TODO*////*TODO*///		static int height[8] = { 1, 1, 2, 2, 2, 4, 4, 8 };
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///		offs = sortedlist[pri_code];
/*TODO*////*TODO*///		if (offs == -1) continue;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		code = K051960_ram[offs+2] + ((K051960_ram[offs+1] & 0x1f) << 8);
/*TODO*////*TODO*///		color = K051960_ram[offs+3] & 0xff;
/*TODO*////*TODO*///		pri = 0;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		(*K051960_callback)(&code,&color,&pri);
/*TODO*////*TODO*///
/*TODO*////*TODO*///		if (pri < min_priority || pri > max_priority) continue;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		size = (K051960_ram[offs+1] & 0xe0) >> 5;
/*TODO*////*TODO*///		w = width[size];
/*TODO*////*TODO*///		h = height[size];
/*TODO*////*TODO*///
/*TODO*////*TODO*///		if (w >= 2) code &= ~0x01;
/*TODO*////*TODO*///		if (h >= 2) code &= ~0x02;
/*TODO*////*TODO*///		if (w >= 4) code &= ~0x04;
/*TODO*////*TODO*///		if (h >= 4) code &= ~0x08;
/*TODO*////*TODO*///		if (w >= 8) code &= ~0x10;
/*TODO*////*TODO*///		if (h >= 8) code &= ~0x20;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		ox = (256 * K051960_ram[offs+6] + K051960_ram[offs+7]) & 0x01ff;
/*TODO*////*TODO*///		oy = 256 - ((256 * K051960_ram[offs+4] + K051960_ram[offs+5]) & 0x01ff);
/*TODO*////*TODO*///		flipx = K051960_ram[offs+6] & 0x02;
/*TODO*////*TODO*///		flipy = K051960_ram[offs+4] & 0x02;
/*TODO*////*TODO*///		zoomx = (K051960_ram[offs+6] & 0xfc) >> 2;
/*TODO*////*TODO*///		zoomy = (K051960_ram[offs+4] & 0xfc) >> 2;
/*TODO*////*TODO*///		zoomx = 0x10000 / 128 * (128 - zoomx);
/*TODO*////*TODO*///		zoomy = 0x10000 / 128 * (128 - zoomy);
/*TODO*////*TODO*///
/*TODO*////*TODO*///		if (K051960_spriteflip)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			ox = 512 - (zoomx * w >> 12) - ox;
/*TODO*////*TODO*///			oy = 256 - (zoomy * h >> 12) - oy;
/*TODO*////*TODO*///			flipx = !flipx;
/*TODO*////*TODO*///			flipy = !flipy;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///
/*TODO*////*TODO*///		if (zoomx == 0x10000 && zoomy == 0x10000)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			int sx,sy;
/*TODO*////*TODO*///
/*TODO*////*TODO*///			for (y = 0;y < h;y++)
/*TODO*////*TODO*///			{
/*TODO*////*TODO*///				sy = oy + 16 * y;
/*TODO*////*TODO*///
/*TODO*////*TODO*///				for (x = 0;x < w;x++)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					int c = code;
/*TODO*////*TODO*///
/*TODO*////*TODO*///					sx = ox + 16 * x;
/*TODO*////*TODO*///					if (flipx) c += xoffset[(w-1-x)];
/*TODO*////*TODO*///					else c += xoffset[x];
/*TODO*////*TODO*///					if (flipy) c += yoffset[(h-1-y)];
/*TODO*////*TODO*///					else c += yoffset[y];
/*TODO*////*TODO*///
/*TODO*////*TODO*///					/* hack to simulate shadow */
/*TODO*////*TODO*///					if (K051960_ram[offs+3] & 0x80)
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						int o = K051960_gfx->colortable[16*color+15];
/*TODO*////*TODO*///						K051960_gfx->colortable[16*color+15] = palette_transparent_pen;
/*TODO*////*TODO*///						drawgfx(bitmap,K051960_gfx,
/*TODO*////*TODO*///								c,
/*TODO*////*TODO*///								color,
/*TODO*////*TODO*///								flipx,flipy,
/*TODO*////*TODO*///								sx & 0x1ff,sy,
/*TODO*////*TODO*///								&Machine->drv->visible_area,TRANSPARENCY_PENS,(cpu_getcurrentframe() & 1) ? 0x8001 : 0x0001);
/*TODO*////*TODO*///						K051960_gfx->colortable[16*color+15] = o;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///					else
/*TODO*////*TODO*///						drawgfx(bitmap,K051960_gfx,
/*TODO*////*TODO*///								c,
/*TODO*////*TODO*///								color,
/*TODO*////*TODO*///								flipx,flipy,
/*TODO*////*TODO*///								sx & 0x1ff,sy,
/*TODO*////*TODO*///								&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///			}
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///		else
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			int sx,sy,zw,zh;
/*TODO*////*TODO*///
/*TODO*////*TODO*///			for (y = 0;y < h;y++)
/*TODO*////*TODO*///			{
/*TODO*////*TODO*///				sy = oy + ((zoomy * y + (1<<11)) >> 12);
/*TODO*////*TODO*///				zh = (oy + ((zoomy * (y+1) + (1<<11)) >> 12)) - sy;
/*TODO*////*TODO*///
/*TODO*////*TODO*///				for (x = 0;x < w;x++)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					int c = code;
/*TODO*////*TODO*///
/*TODO*////*TODO*///					sx = ox + ((zoomx * x + (1<<11)) >> 12);
/*TODO*////*TODO*///					zw = (ox + ((zoomx * (x+1) + (1<<11)) >> 12)) - sx;
/*TODO*////*TODO*///					if (flipx) c += xoffset[(w-1-x)];
/*TODO*////*TODO*///					else c += xoffset[x];
/*TODO*////*TODO*///					if (flipy) c += yoffset[(h-1-y)];
/*TODO*////*TODO*///					else c += yoffset[y];
/*TODO*////*TODO*///
/*TODO*////*TODO*///					drawgfxzoom(bitmap,K051960_gfx,
/*TODO*////*TODO*///							c,
/*TODO*////*TODO*///							color,
/*TODO*////*TODO*///							flipx,flipy,
/*TODO*////*TODO*///							sx & 0x1ff,sy,
/*TODO*////*TODO*///							&Machine->drv->visible_area,TRANSPARENCY_PEN,0,
/*TODO*////*TODO*///							(zw << 16) / 16,(zh << 16) / 16);
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///			}
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///#if 0
/*TODO*////*TODO*///if (keyboard_pressed(KEYCODE_D))
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	FILE *fp;
/*TODO*////*TODO*///	fp=fopen("SPRITE.DMP", "w+b");
/*TODO*////*TODO*///	if (fp)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		fwrite(K051960_ram, 0x400, 1, fp);
/*TODO*////*TODO*///		usrintf_showmessage("saved");
/*TODO*////*TODO*///		fclose(fp);
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///}
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///#undef NUM_SPRITES
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051960_mark_sprites_colors(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	int offs,i;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	unsigned short palette_map[512];
/*TODO*////*TODO*///
/*TODO*////*TODO*///	memset (palette_map, 0, sizeof (palette_map));
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* sprites */
/*TODO*////*TODO*///	for (offs = 0x400-8;offs >= 0;offs -= 8)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		if (K051960_ram[offs] & 0x80)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			int code,color,pri;
/*TODO*////*TODO*///
/*TODO*////*TODO*///			code = K051960_ram[offs+2] + ((K051960_ram[offs+1] & 0x1f) << 8);
/*TODO*////*TODO*///			color = (K051960_ram[offs+3] & 0xff);
/*TODO*////*TODO*///			pri = 0;
/*TODO*////*TODO*///			(*K051960_callback)(&code,&color,&pri);
/*TODO*////*TODO*///			palette_map[color] |= 0xffff;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* now build the final table */
/*TODO*////*TODO*///	for (i = 0; i < 512; i++)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int usage = palette_map[i], j;
/*TODO*////*TODO*///		if (usage)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			for (j = 1; j < 16; j++)
/*TODO*////*TODO*///				if (usage & (1 << j))
/*TODO*////*TODO*///					palette_used_colors[i * 16 + j] |= PALETTE_COLOR_VISIBLE;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K051960_is_IRQ_enabled(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K051960_irq_enabled;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K051960_is_NMI_enabled(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K051960_nmi_enabled;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K052109_051960_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	if (K052109_RMRD_line == CLEAR_LINE)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		if (offset >= 0x3800 && offset < 0x3808)
/*TODO*////*TODO*///			return K051937_r(offset - 0x3800);
/*TODO*////*TODO*///		else if (offset < 0x3c00)
/*TODO*////*TODO*///			return K052109_r(offset);
/*TODO*////*TODO*///		else
/*TODO*////*TODO*///			return K051960_r(offset - 0x3c00);
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else return K052109_r(offset);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K052109_051960_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	if (offset >= 0x3800 && offset < 0x3808)
/*TODO*////*TODO*///		K051937_w(offset - 0x3800,data);
/*TODO*////*TODO*///	else if (offset < 0x3c00)
/*TODO*////*TODO*///		K052109_w(offset,data);
/*TODO*////*TODO*///	else
/*TODO*////*TODO*///		K051960_w(offset - 0x3c00,data);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///static int K053245_memory_region=2;
/*TODO*////*TODO*///static struct GfxElement *K053245_gfx;
/*TODO*////*TODO*///static void (*K053245_callback)(int *code,int *color,int *priority);
/*TODO*////*TODO*///static int K053244_romoffset,K053244_rombank;
/*TODO*////*TODO*///static int K053244_readroms;
/*TODO*////*TODO*///static int K053245_flipscreenX,K053245_flipscreenY;
/*TODO*////*TODO*///static int K053245_spriteoffsX,K053245_spriteoffsY;
/*TODO*////*TODO*///static unsigned char *K053245_ram;
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K053245_vh_start(int gfx_memory_region,int plane0,int plane1,int plane2,int plane3,
/*TODO*////*TODO*///		void (*callback)(int *code,int *color,int *priority))
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	int gfx_index;
/*TODO*////*TODO*///	static struct GfxLayout spritelayout =
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		16,16,
/*TODO*////*TODO*///		0,				/* filled in later */
/*TODO*////*TODO*///		4,
/*TODO*////*TODO*///		{ 0, 0, 0, 0 },	/* filled in later */
/*TODO*////*TODO*///		{ 0, 1, 2, 3, 4, 5, 6, 7,
/*TODO*////*TODO*///				8*32+0, 8*32+1, 8*32+2, 8*32+3, 8*32+4, 8*32+5, 8*32+6, 8*32+7 },
/*TODO*////*TODO*///		{ 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32,
/*TODO*////*TODO*///				16*32, 17*32, 18*32, 19*32, 20*32, 21*32, 22*32, 23*32 },
/*TODO*////*TODO*///		128*8
/*TODO*////*TODO*///	};
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* find first empty slot to decode gfx */
/*TODO*////*TODO*///	for (gfx_index = 0; gfx_index < MAX_GFX_ELEMENTS; gfx_index++)
/*TODO*////*TODO*///		if (Machine->gfx[gfx_index] == 0)
/*TODO*////*TODO*///			break;
/*TODO*////*TODO*///	if (gfx_index == MAX_GFX_ELEMENTS)
/*TODO*////*TODO*///		return 1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* tweak the structure for the number of tiles we have */
/*TODO*////*TODO*///	spritelayout.total = memory_region_length(gfx_memory_region) / 128;
/*TODO*////*TODO*///	spritelayout.planeoffset[0] = plane3 * 8;
/*TODO*////*TODO*///	spritelayout.planeoffset[1] = plane2 * 8;
/*TODO*////*TODO*///	spritelayout.planeoffset[2] = plane1 * 8;
/*TODO*////*TODO*///	spritelayout.planeoffset[3] = plane0 * 8;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* decode the graphics */
/*TODO*////*TODO*///	Machine->gfx[gfx_index] = decodegfx(memory_region(gfx_memory_region),&spritelayout);
/*TODO*////*TODO*///	if (!Machine->gfx[gfx_index])
/*TODO*////*TODO*///		return 1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* set the color information */
/*TODO*////*TODO*///	Machine->gfx[gfx_index]->colortable = Machine->remapped_colortable;
/*TODO*////*TODO*///	Machine->gfx[gfx_index]->total_colors = Machine->drv->color_table_len / 16;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	K053245_memory_region = gfx_memory_region;
/*TODO*////*TODO*///	K053245_gfx = Machine->gfx[gfx_index];
/*TODO*////*TODO*///	K053245_callback = callback;
/*TODO*////*TODO*///	K053244_rombank = 0;
/*TODO*////*TODO*///	K053245_ram = malloc(0x800);
/*TODO*////*TODO*///	if (!K053245_ram) return 1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	memset(K053245_ram,0,0x800);
/*TODO*////*TODO*///
/*TODO*////*TODO*///	return 0;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K053245_vh_stop(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	free(K053245_ram);
/*TODO*////*TODO*///	K053245_ram = 0;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K053245_word_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return READ_WORD(&K053245_ram[offset]);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K053245_word_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	COMBINE_WORD_MEM(&K053245_ram[offset],data);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K053245_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	int shift = ((offset & 1) ^ 1) << 3;
/*TODO*////*TODO*///	return (READ_WORD(&K053245_ram[offset & ~1]) >> shift) & 0xff;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K053245_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	int shift = ((offset & 1) ^ 1) << 3;
/*TODO*////*TODO*///	offset &= ~1;
/*TODO*////*TODO*///	COMBINE_WORD_MEM(&K053245_ram[offset],(0xff000000 >> shift) | ((data & 0xff) << shift));
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K053244_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	if (K053244_readroms && offset >= 0x0c && offset < 0x10)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int addr;
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///		addr = 0x200000 * K053244_rombank + 4 * (K053244_romoffset & 0x7ffff) + ((offset & 3) ^ 1);
/*TODO*////*TODO*///		addr &= memory_region_length(K053245_memory_region)-1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///#if 0
/*TODO*////*TODO*///	usrintf_showmessage("%04x: offset %02x addr %06x",cpu_get_pc(),offset&3,addr);
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///
/*TODO*////*TODO*///		return memory_region(K053245_memory_region)[addr];
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///if (errorlog) fprintf(errorlog,"%04x: read from unknown 053244 address %x\n",cpu_get_pc(),offset);
/*TODO*////*TODO*///		return 0;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K053244_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	if (offset == 0x00)
/*TODO*////*TODO*///		K053245_spriteoffsX = (K053245_spriteoffsX & 0x00ff) | (data << 8);
/*TODO*////*TODO*///	else if (offset == 0x01)
/*TODO*////*TODO*///		K053245_spriteoffsX = (K053245_spriteoffsX & 0xff00) | data;
/*TODO*////*TODO*///	else if (offset == 0x02)
/*TODO*////*TODO*///		K053245_spriteoffsY = (K053245_spriteoffsY & 0x00ff) | (data << 8);
/*TODO*////*TODO*///	else if (offset == 0x03)
/*TODO*////*TODO*///		K053245_spriteoffsY = (K053245_spriteoffsY & 0xff00) | data;
/*TODO*////*TODO*///	else if (offset == 0x05)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///#ifdef MAME_DEBUG
/*TODO*////*TODO*///if (data & 0xc8)
/*TODO*////*TODO*///	usrintf_showmessage("053244 reg 05 = %02x",data);
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///		/* bit 0/1 = flip screen */
/*TODO*////*TODO*///		K053245_flipscreenX = data & 0x01;
/*TODO*////*TODO*///		K053245_flipscreenY = data & 0x02;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* bit 2 = unknown, Parodius uses it */
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* bit 4 = enable gfx ROM reading */
/*TODO*////*TODO*///		K053244_readroms = data & 0x10;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* bit 5 = unknown, Rollergames uses it */
/*TODO*////*TODO*///#if VERBOSE
/*TODO*////*TODO*///if (errorlog) fprintf(errorlog,"%04x: write %02x to 053244 address 5\n",cpu_get_pc(),data);
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else if (offset >= 0x08 && offset < 0x0c)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		offset = 8*((offset & 0x03) ^ 0x01);
/*TODO*////*TODO*///		K053244_romoffset = (K053244_romoffset & ~(0xff << offset)) | (data << offset);
/*TODO*////*TODO*///		return;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else
/*TODO*////*TODO*///if (errorlog) fprintf(errorlog,"%04x: write %02x to unknown 053244 address %x\n",cpu_get_pc(),data,offset);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K053244_bankselect(int bank)   /* used by TMNT2 for ROM testing */
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K053244_rombank = bank;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*////*
/*TODO*////*TODO*/// * Sprite Format
/*TODO*////*TODO*/// * ------------------
/*TODO*////*TODO*/// *
/*TODO*////*TODO*/// * Word | Bit(s)           | Use
/*TODO*////*TODO*/// * -----+-fedcba9876543210-+----------------
/*TODO*////*TODO*/// *   0  | x--------------- | active (show this sprite)
/*TODO*////*TODO*/// *   0  | -x-------------- | maintain aspect ratio (when set, zoom y acts on both axis)
/*TODO*////*TODO*/// *   0  | --x------------- | flip y
/*TODO*////*TODO*/// *   0  | ---x------------ | flip x
/*TODO*////*TODO*/// *   0  | ----xxxx-------- | sprite size (see below)
/*TODO*////*TODO*/// *   0  | ---------xxxxxxx | priority order
/*TODO*////*TODO*/// *   1  | --xxxxxxxxxxxxxx | sprite code. We use an additional bit in TMNT2, but this is
/*TODO*////*TODO*/// *                           probably not accurate (protection related so we can't verify)
/*TODO*////*TODO*/// *   2  | ------xxxxxxxxxx | y position
/*TODO*////*TODO*/// *   3  | ------xxxxxxxxxx | x position
/*TODO*////*TODO*/// *   4  | xxxxxxxxxxxxxxxx | zoom y (0x40 = normal, <0x40 = enlarge, >0x40 = reduce)
/*TODO*////*TODO*/// *   5  | xxxxxxxxxxxxxxxx | zoom x (0x40 = normal, <0x40 = enlarge, >0x40 = reduce)
/*TODO*////*TODO*/// *   6  | ------x--------- | mirror y (top half is drawn as mirror image of the bottom)
/*TODO*////*TODO*/// *   6  | -------x-------- | mirror x (right half is drawn as mirror image of the left)
/*TODO*////*TODO*/// *   6  | --------x------- | shadow
/*TODO*////*TODO*/// *   6  | ---------xxxxxxx | "color", but depends on external connections
/*TODO*////*TODO*/// *   7  | ---------------- |
/*TODO*////*TODO*/// *
/*TODO*////*TODO*/// * shadow enables transparent shadows. Note that it applies to pen 0x0f ONLY.
/*TODO*////*TODO*/// * The rest of the sprite remains normal.
/*TODO*////*TODO*/// */
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K053245_sprites_draw(struct osd_bitmap *bitmap,int min_priority,int max_priority)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///#define NUM_SPRITES 128
/*TODO*////*TODO*///	int offs,pri_code;
/*TODO*////*TODO*///	int sortedlist[NUM_SPRITES];
/*TODO*////*TODO*///
/*TODO*////*TODO*///	for (offs = 0;offs < NUM_SPRITES;offs++)
/*TODO*////*TODO*///		sortedlist[offs] = -1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* prebuild a sorted table */
/*TODO*////*TODO*///	for (offs = 0;offs < 0x800;offs += 16)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		if (READ_WORD(&K053245_ram[offs]) & 0x8000)
/*TODO*////*TODO*///			sortedlist[READ_WORD(&K053245_ram[offs]) & 0x007f] = offs;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///
/*TODO*////*TODO*///	for (pri_code = 0;pri_code < NUM_SPRITES;pri_code++)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int ox,oy,color,code,size,w,h,x,y,flipx,flipy,mirrorx,mirrory,zoomx,zoomy,pri;
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///		offs = sortedlist[pri_code];
/*TODO*////*TODO*///		if (offs == -1) continue;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* the following changes the sprite draw order from
/*TODO*////*TODO*///			 0  1  4  5 16 17 20 21
/*TODO*////*TODO*///			 2  3  6  7 18 19 22 23
/*TODO*////*TODO*///			 8  9 12 13 24 25 28 29
/*TODO*////*TODO*///			10 11 14 15 26 27 30 31
/*TODO*////*TODO*///			32 33 36 37 48 49 52 53
/*TODO*////*TODO*///			34 35 38 39 50 51 54 55
/*TODO*////*TODO*///			40 41 44 45 56 57 60 61
/*TODO*////*TODO*///			42 43 46 47 58 59 62 63
/*TODO*////*TODO*///
/*TODO*////*TODO*///			to
/*TODO*////*TODO*///
/*TODO*////*TODO*///			 0  1  2  3  4  5  6  7
/*TODO*////*TODO*///			 8  9 10 11 12 13 14 15
/*TODO*////*TODO*///			16 17 18 19 20 21 22 23
/*TODO*////*TODO*///			24 25 26 27 28 29 30 31
/*TODO*////*TODO*///			32 33 34 35 36 37 38 39
/*TODO*////*TODO*///			40 41 42 43 44 45 46 47
/*TODO*////*TODO*///			48 49 50 51 52 53 54 55
/*TODO*////*TODO*///			56 57 58 59 60 61 62 63
/*TODO*////*TODO*///		*/
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* NOTE: from the schematics, it looks like the top 2 bits should be ignored */
/*TODO*////*TODO*///		/* (there are not output pins for them), and probably taken from the "color" */
/*TODO*////*TODO*///		/* field to do bank switching. However this applies only to TMNT2, with its */
/*TODO*////*TODO*///		/* protection mcu creating the sprite table, so we don't know where to fetch */
/*TODO*////*TODO*///		/* the bits from. */
/*TODO*////*TODO*///		code = READ_WORD(&K053245_ram[offs+0x02]);
/*TODO*////*TODO*///		code = ((code & 0xffe1) + ((code & 0x0010) >> 2) + ((code & 0x0008) << 1)
/*TODO*////*TODO*///				 + ((code & 0x0004) >> 1) + ((code & 0x0002) << 2));
/*TODO*////*TODO*///		color = READ_WORD(&K053245_ram[offs+0x0c]) & 0x00ff;
/*TODO*////*TODO*///		pri = 0;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		(*K053245_callback)(&code,&color,&pri);
/*TODO*////*TODO*///
/*TODO*////*TODO*///		if (pri < min_priority || pri > max_priority) continue;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		size = (READ_WORD(&K053245_ram[offs]) & 0x0f00) >> 8;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		w = 1 << (size & 0x03);
/*TODO*////*TODO*///		h = 1 << ((size >> 2) & 0x03);
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* zoom control:
/*TODO*////*TODO*///		   0x40 = normal scale
/*TODO*////*TODO*///		  <0x40 enlarge (0x20 = double size)
/*TODO*////*TODO*///		  >0x40 reduce (0x80 = half size)
/*TODO*////*TODO*///		*/
/*TODO*////*TODO*///		zoomy = READ_WORD(&K053245_ram[offs+0x08]);
/*TODO*////*TODO*///		if (zoomy > 0x2000) continue;
/*TODO*////*TODO*///		if (zoomy) zoomy = (0x400000+zoomy/2) / zoomy;
/*TODO*////*TODO*///		else zoomy = 2 * 0x400000;
/*TODO*////*TODO*///		if ((READ_WORD(&K053245_ram[offs]) & 0x4000) == 0)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			zoomx = READ_WORD(&K053245_ram[offs+0x0a]);
/*TODO*////*TODO*///			if (zoomx > 0x2000) continue;
/*TODO*////*TODO*///			if (zoomx) zoomx = (0x400000+zoomx/2) / zoomx;
/*TODO*////*TODO*/////			else zoomx = 2 * 0x400000;
/*TODO*////*TODO*///else zoomx = zoomy; /* workaround for TMNT2 */
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///		else zoomx = zoomy;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		ox = READ_WORD(&K053245_ram[offs+0x06]) + K053245_spriteoffsX;
/*TODO*////*TODO*///		oy = READ_WORD(&K053245_ram[offs+0x04]);
/*TODO*////*TODO*///
/*TODO*////*TODO*///		flipx = READ_WORD(&K053245_ram[offs]) & 0x1000;
/*TODO*////*TODO*///		flipy = READ_WORD(&K053245_ram[offs]) & 0x2000;
/*TODO*////*TODO*///		mirrorx = READ_WORD(&K053245_ram[offs+0x0c]) & 0x0100;
/*TODO*////*TODO*///		mirrory = READ_WORD(&K053245_ram[offs+0x0c]) & 0x0200;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		if (K053245_flipscreenX)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			ox = 512 - ox;
/*TODO*////*TODO*///			if (!mirrorx) flipx = !flipx;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///		if (K053245_flipscreenY)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			oy = -oy;
/*TODO*////*TODO*///			if (!mirrory) flipy = !flipy;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///
/*TODO*////*TODO*///		ox = (ox + 0x5d) & 0x3ff;
/*TODO*////*TODO*///		if (ox >= 768) ox -= 1024;
/*TODO*////*TODO*///		oy = (-(oy + K053245_spriteoffsY + 0x07)) & 0x3ff;
/*TODO*////*TODO*///		if (oy >= 640) oy -= 1024;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* the coordinates given are for the *center* of the sprite */
/*TODO*////*TODO*///		ox -= (zoomx * w) >> 13;
/*TODO*////*TODO*///		oy -= (zoomy * h) >> 13;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		for (y = 0;y < h;y++)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			int sx,sy,zw,zh;
/*TODO*////*TODO*///
/*TODO*////*TODO*///			sy = oy + ((zoomy * y + (1<<11)) >> 12);
/*TODO*////*TODO*///			zh = (oy + ((zoomy * (y+1) + (1<<11)) >> 12)) - sy;
/*TODO*////*TODO*///
/*TODO*////*TODO*///			for (x = 0;x < w;x++)
/*TODO*////*TODO*///			{
/*TODO*////*TODO*///				int c,fx,fy;
/*TODO*////*TODO*///
/*TODO*////*TODO*///				sx = ox + ((zoomx * x + (1<<11)) >> 12);
/*TODO*////*TODO*///				zw = (ox + ((zoomx * (x+1) + (1<<11)) >> 12)) - sx;
/*TODO*////*TODO*///				c = code;
/*TODO*////*TODO*///				if (mirrorx)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					if ((flipx == 0) ^ (2*x < w))
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						/* mirror left/right */
/*TODO*////*TODO*///						c += (w-x-1);
/*TODO*////*TODO*///						fx = 1;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///					else
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						c += x;
/*TODO*////*TODO*///						fx = 0;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///				else
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					if (flipx) c += w-1-x;
/*TODO*////*TODO*///					else c += x;
/*TODO*////*TODO*///					fx = flipx;
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///				if (mirrory)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					if ((flipy == 0) ^ (2*y >= h))
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						/* mirror top/bottom */
/*TODO*////*TODO*///						c += 8*(h-y-1);
/*TODO*////*TODO*///						fy = 1;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///					else
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						c += 8*y;
/*TODO*////*TODO*///						fy = 0;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///				else
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					if (flipy) c += 8*(h-1-y);
/*TODO*////*TODO*///					else c += 8*y;
/*TODO*////*TODO*///					fy = flipy;
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///
/*TODO*////*TODO*///				/* the sprite can start at any point in the 8x8 grid, but it must stay */
/*TODO*////*TODO*///				/* in a 64 entries window, wrapping around at the edges. The animation */
/*TODO*////*TODO*///				/* at the end of the saloon level in SUnset Riders breaks otherwise. */
/*TODO*////*TODO*///				c = (c & 0x3f) | (code & ~0x3f);
/*TODO*////*TODO*///
/*TODO*////*TODO*///				if (zoomx == 0x10000 && zoomy == 0x10000)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					/* hack to simulate shadow */
/*TODO*////*TODO*///					if (READ_WORD(&K053245_ram[offs+0x0c]) & 0x0080)
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						int o = K053245_gfx->colortable[16*color+15];
/*TODO*////*TODO*///						K053245_gfx->colortable[16*color+15] = palette_transparent_pen;
/*TODO*////*TODO*///						drawgfx(bitmap,K053245_gfx,
/*TODO*////*TODO*///								c,
/*TODO*////*TODO*///								color,
/*TODO*////*TODO*///								fx,fy,
/*TODO*////*TODO*///								sx,sy,
/*TODO*////*TODO*///								&Machine->drv->visible_area,TRANSPARENCY_PENS,(cpu_getcurrentframe() & 1) ? 0x8001 : 0x0001);
/*TODO*////*TODO*///						K053245_gfx->colortable[16*color+15] = o;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///					else
/*TODO*////*TODO*///						drawgfx(bitmap,K053245_gfx,
/*TODO*////*TODO*///								c,
/*TODO*////*TODO*///								color,
/*TODO*////*TODO*///								fx,fy,
/*TODO*////*TODO*///								sx,sy,
/*TODO*////*TODO*///								&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///				else
/*TODO*////*TODO*///					drawgfxzoom(bitmap,K053245_gfx,
/*TODO*////*TODO*///							c,
/*TODO*////*TODO*///							color,
/*TODO*////*TODO*///							fx,fy,
/*TODO*////*TODO*///							sx,sy,
/*TODO*////*TODO*///							&Machine->drv->visible_area,TRANSPARENCY_PEN,0,
/*TODO*////*TODO*///							(zw << 16) / 16,(zh << 16) / 16);
/*TODO*////*TODO*///			}
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///#if 0
/*TODO*////*TODO*///if (keyboard_pressed(KEYCODE_D))
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	FILE *fp;
/*TODO*////*TODO*///	fp=fopen("SPRITE.DMP", "w+b");
/*TODO*////*TODO*///	if (fp)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		fwrite(K053245_ram, 0x800, 1, fp);
/*TODO*////*TODO*///		usrintf_showmessage("saved");
/*TODO*////*TODO*///		fclose(fp);
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///}
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///#undef NUM_SPRITES
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K053245_mark_sprites_colors(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	int offs,i;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	unsigned short palette_map[512];
/*TODO*////*TODO*///
/*TODO*////*TODO*///	memset (palette_map, 0, sizeof (palette_map));
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* sprites */
/*TODO*////*TODO*///	for (offs = 0x800-16;offs >= 0;offs -= 16)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		if (READ_WORD(&K053245_ram[offs]) & 0x8000)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			int code,color,pri;
/*TODO*////*TODO*///
/*TODO*////*TODO*///			code = READ_WORD(&K053245_ram[offs+0x02]);
/*TODO*////*TODO*///			code = ((code & 0xffe1) + ((code & 0x0010) >> 2) + ((code & 0x0008) << 1)
/*TODO*////*TODO*///					 + ((code & 0x0004) >> 1) + ((code & 0x0002) << 2));
/*TODO*////*TODO*///			color = READ_WORD(&K053245_ram[offs+0x0c]) & 0x00ff;
/*TODO*////*TODO*///			pri = 0;
/*TODO*////*TODO*///			(*K053245_callback)(&code,&color,&pri);
/*TODO*////*TODO*///			palette_map[color] |= 0xffff;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* now build the final table */
/*TODO*////*TODO*///	for (i = 0; i < 512; i++)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int usage = palette_map[i], j;
/*TODO*////*TODO*///		if (usage)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			for (j = 1; j < 16; j++)
/*TODO*////*TODO*///				if (usage & (1 << j))
/*TODO*////*TODO*///					palette_used_colors[i * 16 + j] |= PALETTE_COLOR_VISIBLE;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///static int K053247_memory_region;
/*TODO*////*TODO*///static struct GfxElement *K053247_gfx;
/*TODO*////*TODO*///static void (*K053247_callback)(int *code,int *color,int *priority);
/*TODO*////*TODO*///static int K053246_OBJCHA_line;
/*TODO*////*TODO*///static int K053246_romoffset;
/*TODO*////*TODO*///static int K053247_flipscreenX,K053247_flipscreenY;
/*TODO*////*TODO*///static int K053247_spriteoffsX,K053247_spriteoffsY;
/*TODO*////*TODO*///static unsigned char *K053247_ram;
/*TODO*////*TODO*///static int K053247_irq_enabled;
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K053247_vh_start(int gfx_memory_region,int plane0,int plane1,int plane2,int plane3,
/*TODO*////*TODO*///		void (*callback)(int *code,int *color,int *priority))
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	int gfx_index;
/*TODO*////*TODO*///	static struct GfxLayout spritelayout =
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		16,16,
/*TODO*////*TODO*///		0,				/* filled in later */
/*TODO*////*TODO*///		4,
/*TODO*////*TODO*///		{ 0, 0, 0, 0 },	/* filled in later */
/*TODO*////*TODO*///		{ 2*4, 3*4, 0*4, 1*4, 6*4, 7*4, 4*4, 5*4,
/*TODO*////*TODO*///				10*4, 11*4, 8*4, 9*4, 14*4, 15*4, 12*4, 13*4 },
/*TODO*////*TODO*///		{ 0*64, 1*64, 2*64, 3*64, 4*64, 5*64, 6*64, 7*64,
/*TODO*////*TODO*///				8*64, 9*64, 10*64, 11*64, 12*64, 13*64, 14*64, 15*64 },
/*TODO*////*TODO*///		128*8
/*TODO*////*TODO*///	};
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* find first empty slot to decode gfx */
/*TODO*////*TODO*///	for (gfx_index = 0; gfx_index < MAX_GFX_ELEMENTS; gfx_index++)
/*TODO*////*TODO*///		if (Machine->gfx[gfx_index] == 0)
/*TODO*////*TODO*///			break;
/*TODO*////*TODO*///	if (gfx_index == MAX_GFX_ELEMENTS)
/*TODO*////*TODO*///		return 1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* tweak the structure for the number of tiles we have */
/*TODO*////*TODO*///	spritelayout.total = memory_region_length(gfx_memory_region) / 128;
/*TODO*////*TODO*///	spritelayout.planeoffset[0] = plane0;
/*TODO*////*TODO*///	spritelayout.planeoffset[1] = plane1;
/*TODO*////*TODO*///	spritelayout.planeoffset[2] = plane2;
/*TODO*////*TODO*///	spritelayout.planeoffset[3] = plane3;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* decode the graphics */
/*TODO*////*TODO*///	Machine->gfx[gfx_index] = decodegfx(memory_region(gfx_memory_region),&spritelayout);
/*TODO*////*TODO*///	if (!Machine->gfx[gfx_index])
/*TODO*////*TODO*///		return 1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* set the color information */
/*TODO*////*TODO*///	Machine->gfx[gfx_index]->colortable = Machine->remapped_colortable;
/*TODO*////*TODO*///	Machine->gfx[gfx_index]->total_colors = Machine->drv->color_table_len / 16;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	K053247_memory_region = gfx_memory_region;
/*TODO*////*TODO*///	K053247_gfx = Machine->gfx[gfx_index];
/*TODO*////*TODO*///	K053247_callback = callback;
/*TODO*////*TODO*///	K053246_OBJCHA_line = CLEAR_LINE;
/*TODO*////*TODO*///	K053247_ram = malloc(0x1000);
/*TODO*////*TODO*///	if (!K053247_ram) return 1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	memset(K053247_ram,0,0x1000);
/*TODO*////*TODO*///
/*TODO*////*TODO*///	return 0;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K053247_vh_stop(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	free(K053247_ram);
/*TODO*////*TODO*///	K053247_ram = 0;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K053247_word_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return READ_WORD(&K053247_ram[offset]);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K053247_word_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	COMBINE_WORD_MEM(&K053247_ram[offset],data);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K053247_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	int shift = ((offset & 1) ^ 1) << 3;
/*TODO*////*TODO*///	return (READ_WORD(&K053247_ram[offset & ~1]) >> shift) & 0xff;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K053247_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	int shift = ((offset & 1) ^ 1) << 3;
/*TODO*////*TODO*///	offset &= ~1;
/*TODO*////*TODO*///	COMBINE_WORD_MEM(&K053247_ram[offset],(0xff000000 >> shift) | ((data & 0xff) << shift));
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K053246_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	if (K053246_OBJCHA_line == ASSERT_LINE)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int addr;
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///		addr = 2 * K053246_romoffset + ((offset & 1) ^ 1);
/*TODO*////*TODO*///		addr &= memory_region_length(K053247_memory_region)-1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///#if 0
/*TODO*////*TODO*///	usrintf_showmessage("%04x: offset %02x addr %06x",cpu_get_pc(),offset,addr);
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///
/*TODO*////*TODO*///		return memory_region(K053247_memory_region)[addr];
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///if (errorlog) fprintf(errorlog,"%04x: read from unknown 053244 address %x\n",cpu_get_pc(),offset);
/*TODO*////*TODO*///		return 0;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K053246_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	if (offset == 0x00)
/*TODO*////*TODO*///		K053247_spriteoffsX = (K053247_spriteoffsX & 0x00ff) | (data << 8);
/*TODO*////*TODO*///	else if (offset == 0x01)
/*TODO*////*TODO*///		K053247_spriteoffsX = (K053247_spriteoffsX & 0xff00) | data;
/*TODO*////*TODO*///	else if (offset == 0x02)
/*TODO*////*TODO*///		K053247_spriteoffsY = (K053247_spriteoffsY & 0x00ff) | (data << 8);
/*TODO*////*TODO*///	else if (offset == 0x03)
/*TODO*////*TODO*///		K053247_spriteoffsY = (K053247_spriteoffsY & 0xff00) | data;
/*TODO*////*TODO*///	else if (offset == 0x05)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///#ifdef MAME_DEBUG
/*TODO*////*TODO*///if (data & 0xc8)
/*TODO*////*TODO*///	usrintf_showmessage("053246 reg 05 = %02x",data);
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///		/* bit 0/1 = flip screen */
/*TODO*////*TODO*///		K053247_flipscreenX = data & 0x01;
/*TODO*////*TODO*///		K053247_flipscreenY = data & 0x02;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* bit 2 = unknown */
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* bit 4 = interrupt enable */
/*TODO*////*TODO*///		K053247_irq_enabled = data & 0x10;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* bit 5 = unknown */
/*TODO*////*TODO*///
/*TODO*////*TODO*///if (errorlog) fprintf(errorlog,"%04x: write %02x to 053246 address 5\n",cpu_get_pc(),data);
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else if (offset >= 0x04 && offset < 0x08)   /* only 4,6,7 - 5 is handled above */
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		offset = 8*(((offset & 0x03) ^ 0x01) - 1);
/*TODO*////*TODO*///		K053246_romoffset = (K053246_romoffset & ~(0xff << offset)) | (data << offset);
/*TODO*////*TODO*///		return;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else
/*TODO*////*TODO*///if (errorlog) fprintf(errorlog,"%04x: write %02x to unknown 053246 address %x\n",cpu_get_pc(),data,offset);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K053246_word_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K053246_r(offset + 1) | (K053246_r(offset) << 8);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K053246_word_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	if ((data & 0xff000000) == 0)
/*TODO*////*TODO*///		K053246_w(offset,(data >> 8) & 0xff);
/*TODO*////*TODO*///	if ((data & 0x00ff0000) == 0)
/*TODO*////*TODO*///		K053246_w(offset + 1,data & 0xff);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K053246_set_OBJCHA_line(int state)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K053246_OBJCHA_line = state;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*////*
/*TODO*////*TODO*/// * Sprite Format
/*TODO*////*TODO*/// * ------------------
/*TODO*////*TODO*/// *
/*TODO*////*TODO*/// * Word | Bit(s)           | Use
/*TODO*////*TODO*/// * -----+-fedcba9876543210-+----------------
/*TODO*////*TODO*/// *   0  | x--------------- | active (show this sprite)
/*TODO*////*TODO*/// *   0  | -x-------------- | maintain aspect ratio (when set, zoom y acts on both axis)
/*TODO*////*TODO*/// *   0  | --x------------- | flip y
/*TODO*////*TODO*/// *   0  | ---x------------ | flip x
/*TODO*////*TODO*/// *   0  | ----xxxx-------- | sprite size (see below)
/*TODO*////*TODO*/// *   0  | ---------xxxxxxx | priority order
/*TODO*////*TODO*/// *   1  | xxxxxxxxxxxxxxxx | sprite code
/*TODO*////*TODO*/// *   2  | ------xxxxxxxxxx | y position
/*TODO*////*TODO*/// *   3  | ------xxxxxxxxxx | x position
/*TODO*////*TODO*/// *   4  | xxxxxxxxxxxxxxxx | zoom y (0x40 = normal, <0x40 = enlarge, >0x40 = reduce)
/*TODO*////*TODO*/// *   5  | xxxxxxxxxxxxxxxx | zoom x (0x40 = normal, <0x40 = enlarge, >0x40 = reduce)
/*TODO*////*TODO*/// *   6  | x--------------- | mirror y (top half is drawn as mirror image of the bottom)
/*TODO*////*TODO*/// *   6  | -x-------------- | mirror x (right half is drawn as mirror image of the left)
/*TODO*////*TODO*/// *   6  | -----x---------- | shadow
/*TODO*////*TODO*/// *   6  | xxxxxxxxxxxxxxxx | "color", but depends on external connections
/*TODO*////*TODO*/// *   7  | ---------------- |
/*TODO*////*TODO*/// *
/*TODO*////*TODO*/// * shadow enables transparent shadows. Note that it applies to pen 0x0f ONLY.
/*TODO*////*TODO*/// * The rest of the sprite remains normal.
/*TODO*////*TODO*/// */
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K053247_sprites_draw(struct osd_bitmap *bitmap,int min_priority,int max_priority)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///#define NUM_SPRITES 256
/*TODO*////*TODO*///	int offs,pri_code;
/*TODO*////*TODO*///	int sortedlist[NUM_SPRITES];
/*TODO*////*TODO*///
/*TODO*////*TODO*///	for (offs = 0;offs < NUM_SPRITES;offs++)
/*TODO*////*TODO*///		sortedlist[offs] = -1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* prebuild a sorted table */
/*TODO*////*TODO*///	for (offs = 0;offs < 0x1000;offs += 16)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*/////		if (READ_WORD(&K053247_ram[offs]) & 0x8000)
/*TODO*////*TODO*///			sortedlist[READ_WORD(&K053247_ram[offs]) & 0x00ff] = offs;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///
/*TODO*////*TODO*///	for (pri_code = NUM_SPRITES-1;pri_code >= 0;pri_code--)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int ox,oy,color,code,size,w,h,x,y,xa,ya,flipx,flipy,mirrorx,mirrory,zoomx,zoomy,pri;
/*TODO*////*TODO*///		/* sprites can be grouped up to 8x8. The draw order is
/*TODO*////*TODO*///			 0  1  4  5 16 17 20 21
/*TODO*////*TODO*///			 2  3  6  7 18 19 22 23
/*TODO*////*TODO*///			 8  9 12 13 24 25 28 29
/*TODO*////*TODO*///			10 11 14 15 26 27 30 31
/*TODO*////*TODO*///			32 33 36 37 48 49 52 53
/*TODO*////*TODO*///			34 35 38 39 50 51 54 55
/*TODO*////*TODO*///			40 41 44 45 56 57 60 61
/*TODO*////*TODO*///			42 43 46 47 58 59 62 63
/*TODO*////*TODO*///		*/
/*TODO*////*TODO*///		static int xoffset[8] = { 0, 1, 4, 5, 16, 17, 20, 21 };
/*TODO*////*TODO*///		static int yoffset[8] = { 0, 2, 8, 10, 32, 34, 40, 42 };
/*TODO*////*TODO*///		static int offsetkludge;
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///		offs = sortedlist[pri_code];
/*TODO*////*TODO*///		if (offs == -1) continue;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		if ((READ_WORD(&K053247_ram[offs]) & 0x8000) == 0) continue;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		code = READ_WORD(&K053247_ram[offs+0x02]);
/*TODO*////*TODO*///		color = READ_WORD(&K053247_ram[offs+0x0c]);
/*TODO*////*TODO*///		pri = 0;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		(*K053247_callback)(&code,&color,&pri);
/*TODO*////*TODO*///
/*TODO*////*TODO*///		if (pri < min_priority || pri > max_priority) continue;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		size = (READ_WORD(&K053247_ram[offs]) & 0x0f00) >> 8;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		w = 1 << (size & 0x03);
/*TODO*////*TODO*///		h = 1 << ((size >> 2) & 0x03);
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* the sprite can start at any point in the 8x8 grid. We have to */
/*TODO*////*TODO*///		/* adjust the offsets to draw it correctly. Simpsons does this all the time. */
/*TODO*////*TODO*///		xa = 0;
/*TODO*////*TODO*///		ya = 0;
/*TODO*////*TODO*///		if (code & 0x01) xa += 1;
/*TODO*////*TODO*///		if (code & 0x02) ya += 1;
/*TODO*////*TODO*///		if (code & 0x04) xa += 2;
/*TODO*////*TODO*///		if (code & 0x08) ya += 2;
/*TODO*////*TODO*///		if (code & 0x10) xa += 4;
/*TODO*////*TODO*///		if (code & 0x20) ya += 4;
/*TODO*////*TODO*///		code &= ~0x3f;
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* zoom control:
/*TODO*////*TODO*///		   0x40 = normal scale
/*TODO*////*TODO*///		  <0x40 enlarge (0x20 = double size)
/*TODO*////*TODO*///		  >0x40 reduce (0x80 = half size)
/*TODO*////*TODO*///		*/
/*TODO*////*TODO*///		zoomy = READ_WORD(&K053247_ram[offs+0x08]);
/*TODO*////*TODO*///		if (zoomy > 0x2000) continue;
/*TODO*////*TODO*///		if (zoomy) zoomy = (0x400000+zoomy/2) / zoomy;
/*TODO*////*TODO*///		else zoomy = 2 * 0x400000;
/*TODO*////*TODO*///		if ((READ_WORD(&K053247_ram[offs]) & 0x4000) == 0)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			zoomx = READ_WORD(&K053247_ram[offs+0x0a]);
/*TODO*////*TODO*///			if (zoomx > 0x2000) continue;
/*TODO*////*TODO*///			if (zoomx) zoomx = (0x400000+zoomx/2) / zoomx;
/*TODO*////*TODO*///			else zoomx = 2 * 0x400000;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///		else zoomx = zoomy;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		ox = READ_WORD(&K053247_ram[offs+0x06]);
/*TODO*////*TODO*///		oy = READ_WORD(&K053247_ram[offs+0x04]);
/*TODO*////*TODO*///
/*TODO*////*TODO*////* TODO: it is not known how the global Y offset works */
/*TODO*////*TODO*///switch (K053247_spriteoffsY)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	case 0x0261:	/* simpsons */
/*TODO*////*TODO*///	case 0x0262:	/* simpsons (dreamland) */
/*TODO*////*TODO*///	case 0x0263:	/* simpsons (dreamland) */
/*TODO*////*TODO*///	case 0x0264:	/* simpsons (dreamland) */
/*TODO*////*TODO*///	case 0x0265:	/* simpsons (dreamland) */
/*TODO*////*TODO*///	case 0x006d:	/* simpsons flip (dreamland) */
/*TODO*////*TODO*///	case 0x006e:	/* simpsons flip (dreamland) */
/*TODO*////*TODO*///	case 0x006f:	/* simpsons flip (dreamland) */
/*TODO*////*TODO*///	case 0x0070:	/* simpsons flip (dreamland) */
/*TODO*////*TODO*///	case 0x0071:	/* simpsons flip */
/*TODO*////*TODO*///		offsetkludge = 0x017;
/*TODO*////*TODO*///		break;
/*TODO*////*TODO*///	case 0x02f7:	/* vendetta (level 4 boss) */
/*TODO*////*TODO*///	case 0x02f8:	/* vendetta (level 4 boss) */
/*TODO*////*TODO*///	case 0x02f9:	/* vendetta (level 4 boss) */
/*TODO*////*TODO*///	case 0x02fa:	/* vendetta */
/*TODO*////*TODO*///	case 0x02fb:	/* vendetta (fat guy jumping) */
/*TODO*////*TODO*///	case 0x02fc:	/* vendetta (fat guy jumping) */
/*TODO*////*TODO*///	case 0x02fd:	/* vendetta (fat guy jumping) */
/*TODO*////*TODO*///	case 0x02fe:	/* vendetta (fat guy jumping) */
/*TODO*////*TODO*///	case 0x02ff:	/* vendetta (fat guy jumping) */
/*TODO*////*TODO*///	case 0x03f7:	/* vendetta flip (level 4 boss) */
/*TODO*////*TODO*///	case 0x03f8:	/* vendetta flip (level 4 boss) */
/*TODO*////*TODO*///	case 0x03f9:	/* vendetta flip (level 4 boss) */
/*TODO*////*TODO*///	case 0x03fa:	/* vendetta flip */
/*TODO*////*TODO*///	case 0x03fb:	/* vendetta flip (fat guy jumping) */
/*TODO*////*TODO*///	case 0x03fc:	/* vendetta flip (fat guy jumping) */
/*TODO*////*TODO*///	case 0x03fd:	/* vendetta flip (fat guy jumping) */
/*TODO*////*TODO*///	case 0x03fe:	/* vendetta flip (fat guy jumping) */
/*TODO*////*TODO*///	case 0x03ff:	/* vendetta flip (fat guy jumping) */
/*TODO*////*TODO*///		offsetkludge = 0x006;
/*TODO*////*TODO*///		break;
/*TODO*////*TODO*///	case 0x0292:	/* xmen */
/*TODO*////*TODO*///	case 0x0072:	/* xmen flip */
/*TODO*////*TODO*///		offsetkludge = -0x002;
/*TODO*////*TODO*///		break;
/*TODO*////*TODO*///	default:
/*TODO*////*TODO*///		offsetkludge = 0;
/*TODO*////*TODO*///			usrintf_showmessage("unknown spriteoffsY %04x",K053247_spriteoffsY);
/*TODO*////*TODO*///		break;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///		flipx = READ_WORD(&K053247_ram[offs]) & 0x1000;
/*TODO*////*TODO*///		flipy = READ_WORD(&K053247_ram[offs]) & 0x2000;
/*TODO*////*TODO*///		mirrorx = READ_WORD(&K053247_ram[offs+0x0c]) & 0x4000;
/*TODO*////*TODO*///		mirrory = READ_WORD(&K053247_ram[offs+0x0c]) & 0x8000;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		if (K053247_flipscreenX)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			ox = -ox;
/*TODO*////*TODO*///			if (!mirrorx) flipx = !flipx;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///		if (K053247_flipscreenY)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			oy = -oy;
/*TODO*////*TODO*///			if (!mirrory) flipy = !flipy;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///
/*TODO*////*TODO*///		ox = (ox + 0x35 - K053247_spriteoffsX) & 0x3ff;
/*TODO*////*TODO*///		if (ox >= 768) ox -= 1024;
/*TODO*////*TODO*///		oy = (-(oy + K053247_spriteoffsY + offsetkludge)) & 0x3ff;
/*TODO*////*TODO*///		if (oy >= 640) oy -= 1024;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* the coordinates given are for the *center* of the sprite */
/*TODO*////*TODO*///		ox -= (zoomx * w) >> 13;
/*TODO*////*TODO*///		oy -= (zoomy * h) >> 13;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		for (y = 0;y < h;y++)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			int sx,sy,zw,zh;
/*TODO*////*TODO*///
/*TODO*////*TODO*///			sy = oy + ((zoomy * y + (1<<11)) >> 12);
/*TODO*////*TODO*///			zh = (oy + ((zoomy * (y+1) + (1<<11)) >> 12)) - sy;
/*TODO*////*TODO*///
/*TODO*////*TODO*///			for (x = 0;x < w;x++)
/*TODO*////*TODO*///			{
/*TODO*////*TODO*///				int c,fx,fy;
/*TODO*////*TODO*///
/*TODO*////*TODO*///				sx = ox + ((zoomx * x + (1<<11)) >> 12);
/*TODO*////*TODO*///				zw = (ox + ((zoomx * (x+1) + (1<<11)) >> 12)) - sx;
/*TODO*////*TODO*///				c = code;
/*TODO*////*TODO*///				if (mirrorx)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					if ((flipx == 0) ^ (2*x < w))
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						/* mirror left/right */
/*TODO*////*TODO*///						c += xoffset[(w-1-x+xa)&7];
/*TODO*////*TODO*///						fx = 1;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///					else
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						c += xoffset[(x+xa)&7];
/*TODO*////*TODO*///						fx = 0;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///				else
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					if (flipx) c += xoffset[(w-1-x+xa)&7];
/*TODO*////*TODO*///					else c += xoffset[(x+xa)&7];
/*TODO*////*TODO*///					fx = flipx;
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///				if (mirrory)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					if ((flipy == 0) ^ (2*y >= h))
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						/* mirror top/bottom */
/*TODO*////*TODO*///						c += yoffset[(h-1-y+ya)&7];
/*TODO*////*TODO*///						fy = 1;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///					else
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						c += yoffset[(y+ya)&7];
/*TODO*////*TODO*///						fy = 0;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///				else
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					if (flipy) c += yoffset[(h-1-y+ya)&7];
/*TODO*////*TODO*///					else c += yoffset[(y+ya)&7];
/*TODO*////*TODO*///					fy = flipy;
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///
/*TODO*////*TODO*///				if (zoomx == 0x10000 && zoomy == 0x10000)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					/* hack to simulate shadow */
/*TODO*////*TODO*///					if (READ_WORD(&K053247_ram[offs+0x0c]) & 0x0400)
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						int o = K053247_gfx->colortable[16*color+15];
/*TODO*////*TODO*///						K053247_gfx->colortable[16*color+15] = palette_transparent_pen;
/*TODO*////*TODO*///						drawgfx(bitmap,K053247_gfx,
/*TODO*////*TODO*///								c,
/*TODO*////*TODO*///								color,
/*TODO*////*TODO*///								fx,fy,
/*TODO*////*TODO*///								sx,sy,
/*TODO*////*TODO*///								&Machine->drv->visible_area,TRANSPARENCY_PENS,(cpu_getcurrentframe() & 1) ? 0x8001 : 0x0001);
/*TODO*////*TODO*///						K053247_gfx->colortable[16*color+15] = o;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///					else
/*TODO*////*TODO*///						drawgfx(bitmap,K053247_gfx,
/*TODO*////*TODO*///								c,
/*TODO*////*TODO*///								color,
/*TODO*////*TODO*///								fx,fy,
/*TODO*////*TODO*///								sx,sy,
/*TODO*////*TODO*///								&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///				else
/*TODO*////*TODO*///					drawgfxzoom(bitmap,K053247_gfx,
/*TODO*////*TODO*///							c,
/*TODO*////*TODO*///							color,
/*TODO*////*TODO*///							fx,fy,
/*TODO*////*TODO*///							sx,sy,
/*TODO*////*TODO*///							&Machine->drv->visible_area,TRANSPARENCY_PEN,0,
/*TODO*////*TODO*///							(zw << 16) / 16,(zh << 16) / 16);
/*TODO*////*TODO*///
/*TODO*////*TODO*///				if (mirrory && h == 1)  /* Simpsons shadows */
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					if (zoomx == 0x10000 && zoomy == 0x10000)
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						/* hack to simulate shadow */
/*TODO*////*TODO*///						if (READ_WORD(&K053247_ram[offs+0x0c]) & 0x0400)
/*TODO*////*TODO*///						{
/*TODO*////*TODO*///							int o = K053247_gfx->colortable[16*color+15];
/*TODO*////*TODO*///							K053247_gfx->colortable[16*color+15] = palette_transparent_pen;
/*TODO*////*TODO*///							drawgfx(bitmap,K053247_gfx,
/*TODO*////*TODO*///									c,
/*TODO*////*TODO*///									color,
/*TODO*////*TODO*///									fx,!fy,
/*TODO*////*TODO*///									sx,sy,
/*TODO*////*TODO*///									&Machine->drv->visible_area,TRANSPARENCY_PENS,(cpu_getcurrentframe() & 1) ? 0x8001 : 0x0001);
/*TODO*////*TODO*///							K053247_gfx->colortable[16*color+15] = o;
/*TODO*////*TODO*///						}
/*TODO*////*TODO*///						else
/*TODO*////*TODO*///							drawgfx(bitmap,K053247_gfx,
/*TODO*////*TODO*///									c,
/*TODO*////*TODO*///									color,
/*TODO*////*TODO*///									fx,!fy,
/*TODO*////*TODO*///									sx,sy,
/*TODO*////*TODO*///									&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///					else
/*TODO*////*TODO*///						drawgfxzoom(bitmap,K053247_gfx,
/*TODO*////*TODO*///								c,
/*TODO*////*TODO*///								color,
/*TODO*////*TODO*///								fx,!fy,
/*TODO*////*TODO*///								sx,sy,
/*TODO*////*TODO*///								&Machine->drv->visible_area,TRANSPARENCY_PEN,0,
/*TODO*////*TODO*///								(zw << 16) / 16,(zh << 16) / 16);
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///			}
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///#if 0
/*TODO*////*TODO*///if (keyboard_pressed(KEYCODE_D))
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	FILE *fp;
/*TODO*////*TODO*///	fp=fopen("SPRITE.DMP", "w+b");
/*TODO*////*TODO*///	if (fp)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		fwrite(K053247_ram, 0x1000, 1, fp);
/*TODO*////*TODO*///		usrintf_showmessage("saved");
/*TODO*////*TODO*///		fclose(fp);
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///}
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///#undef NUM_SPRITES
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K053247_mark_sprites_colors(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	int offs,i;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	unsigned short palette_map[512];
/*TODO*////*TODO*///
/*TODO*////*TODO*///	memset (palette_map, 0, sizeof (palette_map));
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* sprites */
/*TODO*////*TODO*///	for (offs = 0x1000-16;offs >= 0;offs -= 16)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		if (READ_WORD(&K053247_ram[offs]) & 0x8000)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			int code,color,pri;
/*TODO*////*TODO*///
/*TODO*////*TODO*///			code = READ_WORD(&K053247_ram[offs+0x02]);
/*TODO*////*TODO*///			color = READ_WORD(&K053247_ram[offs+0x0c]);
/*TODO*////*TODO*///			pri = 0;
/*TODO*////*TODO*///			(*K053247_callback)(&code,&color,&pri);
/*TODO*////*TODO*///			palette_map[color] |= 0xffff;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* now build the final table */
/*TODO*////*TODO*///	for (i = 0; i < 512; i++)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int usage = palette_map[i], j;
/*TODO*////*TODO*///		if (usage)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			for (j = 1; j < 16; j++)
/*TODO*////*TODO*///				if (usage & (1 << j))
/*TODO*////*TODO*///					palette_used_colors[i * 16 + j] |= PALETTE_COLOR_VISIBLE;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K053247_is_IRQ_enabled(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K053247_irq_enabled;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///#define MAX_K051316 3
/*TODO*////*TODO*///
/*TODO*////*TODO*///static int K051316_memory_region[MAX_K051316];
/*TODO*////*TODO*///static int K051316_gfxnum[MAX_K051316];
/*TODO*////*TODO*///static int K051316_wraparound[MAX_K051316];
/*TODO*////*TODO*///static int K051316_offset[MAX_K051316][2];
/*TODO*////*TODO*///static int K051316_bpp[MAX_K051316];
/*TODO*////*TODO*///static void (*K051316_callback[MAX_K051316])(int *code,int *color);
/*TODO*////*TODO*///static unsigned char *K051316_ram[MAX_K051316];
/*TODO*////*TODO*///static unsigned char K051316_ctrlram[MAX_K051316][16];
/*TODO*////*TODO*///static struct tilemap *K051316_tilemap[MAX_K051316];
/*TODO*////*TODO*///static int K051316_chip_selected;
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051316_vh_stop(int chip);
/*TODO*////*TODO*///
/*TODO*////*TODO*////***************************************************************************
/*TODO*////*TODO*///
/*TODO*////*TODO*///  Callbacks for the TileMap code
/*TODO*////*TODO*///
/*TODO*////*TODO*///***************************************************************************/
/*TODO*////*TODO*///
/*TODO*////*TODO*///static void K051316_preupdate(int chip)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K051316_chip_selected = chip;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///static void K051316_get_tile_info(int col,int row)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	int tile_index = 32*row+col;
/*TODO*////*TODO*///	int code = K051316_ram[K051316_chip_selected][tile_index];
/*TODO*////*TODO*///	int color = K051316_ram[K051316_chip_selected][tile_index + 0x400];
/*TODO*////*TODO*///
/*TODO*////*TODO*///	(*K051316_callback[K051316_chip_selected])(&code,&color);
/*TODO*////*TODO*///
/*TODO*////*TODO*///	SET_TILE_INFO(K051316_gfxnum[K051316_chip_selected],code,color);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K051316_vh_start(int chip, int gfx_memory_region,int bpp,
/*TODO*////*TODO*///		void (*callback)(int *code,int *color))
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	int gfx_index;
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* find first empty slot to decode gfx */
/*TODO*////*TODO*///	for (gfx_index = 0; gfx_index < MAX_GFX_ELEMENTS; gfx_index++)
/*TODO*////*TODO*///		if (Machine->gfx[gfx_index] == 0)
/*TODO*////*TODO*///			break;
/*TODO*////*TODO*///	if (gfx_index == MAX_GFX_ELEMENTS)
/*TODO*////*TODO*///		return 1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	if (bpp == 4)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		static struct GfxLayout charlayout =
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			16,16,
/*TODO*////*TODO*///			0,				/* filled in later */
/*TODO*////*TODO*///			4,
/*TODO*////*TODO*///			{ 0, 1, 2, 3 },
/*TODO*////*TODO*///			{ 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4,
/*TODO*////*TODO*///					8*4, 9*4, 10*4, 11*4, 12*4, 13*4, 14*4, 15*4 },
/*TODO*////*TODO*///			{ 0*64, 1*64, 2*64, 3*64, 4*64, 5*64, 6*64, 7*64,
/*TODO*////*TODO*///					8*64, 9*64, 10*64, 11*64, 12*64, 13*64, 14*64, 15*64 },
/*TODO*////*TODO*///			128*8
/*TODO*////*TODO*///		};
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* tweak the structure for the number of tiles we have */
/*TODO*////*TODO*///		charlayout.total = memory_region_length(gfx_memory_region) / 128;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* decode the graphics */
/*TODO*////*TODO*///		Machine->gfx[gfx_index] = decodegfx(memory_region(gfx_memory_region),&charlayout);
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else if (bpp == 7)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		static struct GfxLayout charlayout =
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			16,16,
/*TODO*////*TODO*///			0,				/* filled in later */
/*TODO*////*TODO*///			7,
/*TODO*////*TODO*///			{ 1, 2, 3, 4, 5, 6, 7 },
/*TODO*////*TODO*///			{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
/*TODO*////*TODO*///					8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
/*TODO*////*TODO*///			{ 0*128, 1*128, 2*128, 3*128, 4*128, 5*128, 6*128, 7*128,
/*TODO*////*TODO*///					8*128, 9*128, 10*128, 11*128, 12*128, 13*128, 14*128, 15*128 },
/*TODO*////*TODO*///			256*8
/*TODO*////*TODO*///		};
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* tweak the structure for the number of tiles we have */
/*TODO*////*TODO*///		charlayout.total = memory_region_length(gfx_memory_region) / 256;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* decode the graphics */
/*TODO*////*TODO*///		Machine->gfx[gfx_index] = decodegfx(memory_region(gfx_memory_region),&charlayout);
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///if (errorlog) fprintf(errorlog,"K051316_vh_start supports only 4 or 7 bpp\n");
/*TODO*////*TODO*///		return 1;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///
/*TODO*////*TODO*///	if (!Machine->gfx[gfx_index])
/*TODO*////*TODO*///		return 1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* set the color information */
/*TODO*////*TODO*///	Machine->gfx[gfx_index]->colortable = Machine->remapped_colortable;
/*TODO*////*TODO*///	Machine->gfx[gfx_index]->total_colors = Machine->drv->color_table_len / (1 << bpp);
/*TODO*////*TODO*///
/*TODO*////*TODO*///	K051316_memory_region[chip] = gfx_memory_region;
/*TODO*////*TODO*///	K051316_gfxnum[chip] = gfx_index;
/*TODO*////*TODO*///	K051316_bpp[chip] = bpp;
/*TODO*////*TODO*///	K051316_callback[chip] = callback;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	K051316_tilemap[chip] = tilemap_create(K051316_get_tile_info,TILEMAP_OPAQUE,16,16,32,32);
/*TODO*////*TODO*///
/*TODO*////*TODO*///	K051316_ram[chip] = malloc(0x800);
/*TODO*////*TODO*///
/*TODO*////*TODO*///	if (!K051316_ram[chip] || !K051316_tilemap[chip])
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		K051316_vh_stop(chip);
/*TODO*////*TODO*///		return 1;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///
/*TODO*////*TODO*///	tilemap_set_clip(K051316_tilemap[chip],0);
/*TODO*////*TODO*///
/*TODO*////*TODO*///	K051316_wraparound[chip] = 0;	/* default = no wraparound */
/*TODO*////*TODO*///	K051316_offset[chip][0] = K051316_offset[chip][1] = 0;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	return 0;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K051316_vh_start_0(int gfx_memory_region,int bpp,
/*TODO*////*TODO*///		void (*callback)(int *code,int *color))
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K051316_vh_start(0,gfx_memory_region,bpp,callback);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K051316_vh_start_1(int gfx_memory_region,int bpp,
/*TODO*////*TODO*///		void (*callback)(int *code,int *color))
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K051316_vh_start(1,gfx_memory_region,bpp,callback);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K051316_vh_start_2(int gfx_memory_region,int bpp,
/*TODO*////*TODO*///		void (*callback)(int *code,int *color))
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K051316_vh_start(2,gfx_memory_region,bpp,callback);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051316_vh_stop(int chip)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	free(K051316_ram[chip]);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051316_vh_stop_0(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K051316_vh_stop(0);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051316_vh_stop_1(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K051316_vh_stop(1);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051316_vh_stop_2(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K051316_vh_stop(2);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K051316_r(int chip, int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K051316_ram[chip][offset];
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K051316_0_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K051316_r(0, offset);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K051316_1_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K051316_r(1, offset);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K051316_2_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K051316_r(2, offset);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051316_w(int chip,int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	if (K051316_ram[chip][offset] != data)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		K051316_ram[chip][offset] = data;
/*TODO*////*TODO*///		tilemap_mark_tile_dirty(K051316_tilemap[chip],offset%32,(offset%0x400)/32);
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051316_0_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K051316_w(0,offset,data);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051316_1_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K051316_w(1,offset,data);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051316_2_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K051316_w(2,offset,data);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K051316_rom_r(int chip, int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	if ((K051316_ctrlram[chip][0x0e] & 0x01) == 0)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int addr;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		addr = offset + (K051316_ctrlram[chip][0x0c] << 11) + (K051316_ctrlram[chip][0x0d] << 19);
/*TODO*////*TODO*///		if (K051316_bpp[chip] <= 4) addr /= 2;
/*TODO*////*TODO*///		addr &= memory_region_length(K051316_memory_region[chip])-1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///#if 0
/*TODO*////*TODO*///	usrintf_showmessage("%04x: offset %04x addr %04x",cpu_get_pc(),offset,addr);
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///
/*TODO*////*TODO*///		return memory_region(K051316_memory_region[chip])[addr];
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///if (errorlog) fprintf(errorlog,"%04x: read 051316 ROM offset %04x but reg 0x0c bit 0 not clear\n",cpu_get_pc(),offset);
/*TODO*////*TODO*///		return 0;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K051316_rom_0_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K051316_rom_r(0,offset);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K051316_rom_1_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K051316_rom_r(1,offset);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K051316_rom_2_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K051316_rom_r(2,offset);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051316_ctrl_w(int chip,int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K051316_ctrlram[chip][offset] = data;
/*TODO*////*TODO*///if (errorlog && offset >= 0x0c) fprintf(errorlog,"%04x: write %02x to 051316 reg %x\n",cpu_get_pc(),data,offset);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051316_ctrl_0_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K051316_ctrl_w(0,offset,data);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051316_ctrl_1_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K051316_ctrl_w(1,offset,data);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051316_ctrl_2_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K051316_ctrl_w(2,offset,data);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051316_wraparound_enable(int chip, int status)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K051316_wraparound[chip] = status;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051316_set_offset(int chip, int xoffs, int yoffs)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K051316_offset[chip][0] = xoffs;
/*TODO*////*TODO*///	K051316_offset[chip][1] = yoffs;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051316_tilemap_update(int chip)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K051316_preupdate(chip);
/*TODO*////*TODO*///	tilemap_update(K051316_tilemap[chip]);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051316_tilemap_update_0(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K051316_tilemap_update(0);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051316_tilemap_update_1(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K051316_tilemap_update(1);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051316_tilemap_update_2(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K051316_tilemap_update(2);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*////* Note: rotation support doesn't handle asymmetrical visible areas. This doesn't */
/*TODO*////*TODO*////* matter because in the Konami games the visible area is always symmetrical. */
/*TODO*////*TODO*///void K051316_zoom_draw(int chip, struct osd_bitmap *bitmap)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	UINT32 startx,starty,cx,cy;
/*TODO*////*TODO*///	int incxx,incxy,incyx,incyy;
/*TODO*////*TODO*///	int x,sx,sy,ex,ey;
/*TODO*////*TODO*///	struct osd_bitmap *srcbitmap = K051316_tilemap[chip]->pixmap;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	startx = 256 * ((INT16)(256 * K051316_ctrlram[chip][0x00] + K051316_ctrlram[chip][0x01]));
/*TODO*////*TODO*///	incxx  =        (INT16)(256 * K051316_ctrlram[chip][0x02] + K051316_ctrlram[chip][0x03]);
/*TODO*////*TODO*///	incyx  =        (INT16)(256 * K051316_ctrlram[chip][0x04] + K051316_ctrlram[chip][0x05]);
/*TODO*////*TODO*///	starty = 256 * ((INT16)(256 * K051316_ctrlram[chip][0x06] + K051316_ctrlram[chip][0x07]));
/*TODO*////*TODO*///	incxy  =        (INT16)(256 * K051316_ctrlram[chip][0x08] + K051316_ctrlram[chip][0x09]);
/*TODO*////*TODO*///	incyy  =        (INT16)(256 * K051316_ctrlram[chip][0x0a] + K051316_ctrlram[chip][0x0b]);
/*TODO*////*TODO*///
/*TODO*////*TODO*///	startx += (Machine->drv->visible_area.min_y - (16 + K051316_offset[chip][1])) * incyx;
/*TODO*////*TODO*///	starty += (Machine->drv->visible_area.min_y - (16 + K051316_offset[chip][1])) * incyy;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	startx += (Machine->drv->visible_area.min_x - (89 + K051316_offset[chip][0])) * incxx;
/*TODO*////*TODO*///	starty += (Machine->drv->visible_area.min_x - (89 + K051316_offset[chip][0])) * incxy;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	sx = Machine->drv->visible_area.min_x;
/*TODO*////*TODO*///	sy = Machine->drv->visible_area.min_y;
/*TODO*////*TODO*///	ex = Machine->drv->visible_area.max_x;
/*TODO*////*TODO*///	ey = Machine->drv->visible_area.max_y;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int t;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		t = startx; startx = starty; starty = t;
/*TODO*////*TODO*///		t = sx; sx = sy; sy = t;
/*TODO*////*TODO*///		t = ex; ex = ey; ey = t;
/*TODO*////*TODO*///		t = incxx; incxx = incyy; incyy = t;
/*TODO*////*TODO*///		t = incxy; incxy = incyx; incyx = t;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///
/*TODO*////*TODO*///	if (Machine->orientation & ORIENTATION_FLIP_X)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int w = ex - sx;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		incxy = -incxy;
/*TODO*////*TODO*///		incyx = -incyx;
/*TODO*////*TODO*///		startx = 0xfffff - startx;
/*TODO*////*TODO*///		startx -= incxx * w;
/*TODO*////*TODO*///		starty -= incxy * w;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///
/*TODO*////*TODO*///	if (Machine->orientation & ORIENTATION_FLIP_Y)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int h = ey - sy;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		incxy = -incxy;
/*TODO*////*TODO*///		incyx = -incyx;
/*TODO*////*TODO*///		starty = 0xfffff - starty;
/*TODO*////*TODO*///		startx -= incyx * h;
/*TODO*////*TODO*///		starty -= incyy * h;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///
/*TODO*////*TODO*///	if (bitmap->depth == 8)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		unsigned char *dest;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		if (incxy == 0 && incyx == 0 && !K051316_wraparound[chip])
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			/* optimized loop for the not rotated case */
/*TODO*////*TODO*///
/*TODO*////*TODO*///			if (incxx == 0x800)
/*TODO*////*TODO*///			{
/*TODO*////*TODO*///				/* optimized loop for the not zoomed case */
/*TODO*////*TODO*///
/*TODO*////*TODO*///				/* startx is unsigned */
/*TODO*////*TODO*///				startx = ((INT32)startx) >> 11;
/*TODO*////*TODO*///
/*TODO*////*TODO*///				if (startx >= 512)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					sx += -startx;
/*TODO*////*TODO*///					startx = 0;
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///
/*TODO*////*TODO*///				if (sx <= ex)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					while (sy <= ey)
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						if ((starty & 0xfff00000) == 0)
/*TODO*////*TODO*///						{
/*TODO*////*TODO*///							x = sx;
/*TODO*////*TODO*///							cx = startx;
/*TODO*////*TODO*///							cy = starty >> 11;
/*TODO*////*TODO*///							dest = &bitmap->line[sy][sx];
/*TODO*////*TODO*///							while (x <= ex && cx < 512)
/*TODO*////*TODO*///							{
/*TODO*////*TODO*///								int c = srcbitmap->line[cy][cx];
/*TODO*////*TODO*///
/*TODO*////*TODO*///								if (c != palette_transparent_pen)
/*TODO*////*TODO*///									*dest = c;
/*TODO*////*TODO*///
/*TODO*////*TODO*///								cx++;
/*TODO*////*TODO*///								x++;
/*TODO*////*TODO*///								dest++;
/*TODO*////*TODO*///							}
/*TODO*////*TODO*///						}
/*TODO*////*TODO*///						starty += incyy;
/*TODO*////*TODO*///						sy++;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///			}
/*TODO*////*TODO*///			else
/*TODO*////*TODO*///			{
/*TODO*////*TODO*///				while ((startx & 0xfff00000) != 0 && sx <= ex)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					startx += incxx;
/*TODO*////*TODO*///					sx++;
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///
/*TODO*////*TODO*///				if ((startx & 0xfff00000) == 0)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					while (sy <= ey)
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						if ((starty & 0xfff00000) == 0)
/*TODO*////*TODO*///						{
/*TODO*////*TODO*///							x = sx;
/*TODO*////*TODO*///							cx = startx;
/*TODO*////*TODO*///							cy = starty >> 11;
/*TODO*////*TODO*///							dest = &bitmap->line[sy][sx];
/*TODO*////*TODO*///							while (x <= ex && (cx & 0xfff00000) == 0)
/*TODO*////*TODO*///							{
/*TODO*////*TODO*///								int c = srcbitmap->line[cy][cx >> 11];
/*TODO*////*TODO*///
/*TODO*////*TODO*///								if (c != palette_transparent_pen)
/*TODO*////*TODO*///									*dest = c;
/*TODO*////*TODO*///
/*TODO*////*TODO*///								cx += incxx;
/*TODO*////*TODO*///								x++;
/*TODO*////*TODO*///								dest++;
/*TODO*////*TODO*///							}
/*TODO*////*TODO*///						}
/*TODO*////*TODO*///						starty += incyy;
/*TODO*////*TODO*///						sy++;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///			}
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///		else
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			if (K051316_wraparound[chip])
/*TODO*////*TODO*///			{
/*TODO*////*TODO*///				/* plot with wraparound */
/*TODO*////*TODO*///				while (sy <= ey)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					x = sx;
/*TODO*////*TODO*///					cx = startx;
/*TODO*////*TODO*///					cy = starty;
/*TODO*////*TODO*///					dest = &bitmap->line[sy][sx];
/*TODO*////*TODO*///					while (x <= ex)
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						int c = srcbitmap->line[(cy >> 11) & 0x1ff][(cx >> 11) & 0x1ff];
/*TODO*////*TODO*///
/*TODO*////*TODO*///						if (c != palette_transparent_pen)
/*TODO*////*TODO*///							*dest = c;
/*TODO*////*TODO*///
/*TODO*////*TODO*///						cx += incxx;
/*TODO*////*TODO*///						cy += incxy;
/*TODO*////*TODO*///						x++;
/*TODO*////*TODO*///						dest++;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///					startx += incyx;
/*TODO*////*TODO*///					starty += incyy;
/*TODO*////*TODO*///					sy++;
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///			}
/*TODO*////*TODO*///			else
/*TODO*////*TODO*///			{
/*TODO*////*TODO*///				while (sy <= ey)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					x = sx;
/*TODO*////*TODO*///					cx = startx;
/*TODO*////*TODO*///					cy = starty;
/*TODO*////*TODO*///					dest = &bitmap->line[sy][sx];
/*TODO*////*TODO*///					while (x <= ex)
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						if ((cx & 0xfff00000) == 0 && (cy & 0xfff00000) == 0)
/*TODO*////*TODO*///						{
/*TODO*////*TODO*///							int c = srcbitmap->line[cy >> 11][cx >> 11];
/*TODO*////*TODO*///
/*TODO*////*TODO*///							if (c != palette_transparent_pen)
/*TODO*////*TODO*///								*dest = c;
/*TODO*////*TODO*///						}
/*TODO*////*TODO*///
/*TODO*////*TODO*///						cx += incxx;
/*TODO*////*TODO*///						cy += incxy;
/*TODO*////*TODO*///						x++;
/*TODO*////*TODO*///						dest++;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///					startx += incyx;
/*TODO*////*TODO*///					starty += incyy;
/*TODO*////*TODO*///					sy++;
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///			}
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///	else
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		/* 16-bit case */
/*TODO*////*TODO*///
/*TODO*////*TODO*///		unsigned short *dest;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		if (incxy == 0 && incyx == 0 && !K051316_wraparound[chip])
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			/* optimized loop for the not rotated case */
/*TODO*////*TODO*///
/*TODO*////*TODO*///			if (incxx == 0x800)
/*TODO*////*TODO*///			{
/*TODO*////*TODO*///				/* optimized loop for the not zoomed case */
/*TODO*////*TODO*///
/*TODO*////*TODO*///				/* startx is unsigned */
/*TODO*////*TODO*///				startx = ((INT32)startx) >> 11;
/*TODO*////*TODO*///
/*TODO*////*TODO*///				if (startx >= 512)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					sx += -startx;
/*TODO*////*TODO*///					startx = 0;
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///
/*TODO*////*TODO*///				if (sx <= ex)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					while (sy <= ey)
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						if ((starty & 0xfff00000) == 0)
/*TODO*////*TODO*///						{
/*TODO*////*TODO*///							x = sx;
/*TODO*////*TODO*///							cx = startx;
/*TODO*////*TODO*///							cy = starty >> 11;
/*TODO*////*TODO*///							dest = &((unsigned short *)bitmap->line[sy])[sx];
/*TODO*////*TODO*///							while (x <= ex && cx < 512)
/*TODO*////*TODO*///							{
/*TODO*////*TODO*///								int c = ((unsigned short *)srcbitmap->line[cy])[cx];
/*TODO*////*TODO*///
/*TODO*////*TODO*///								if (c != palette_transparent_pen)
/*TODO*////*TODO*///									*dest = c;
/*TODO*////*TODO*///
/*TODO*////*TODO*///								cx++;
/*TODO*////*TODO*///								x++;
/*TODO*////*TODO*///								dest++;
/*TODO*////*TODO*///							}
/*TODO*////*TODO*///						}
/*TODO*////*TODO*///						starty += incyy;
/*TODO*////*TODO*///						sy++;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///			}
/*TODO*////*TODO*///			else
/*TODO*////*TODO*///			{
/*TODO*////*TODO*///				while ((startx & 0xfff00000) != 0 && sx <= ex)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					startx += incxx;
/*TODO*////*TODO*///					sx++;
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///
/*TODO*////*TODO*///				if ((startx & 0xfff00000) == 0)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					while (sy <= ey)
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						if ((starty & 0xfff00000) == 0)
/*TODO*////*TODO*///						{
/*TODO*////*TODO*///							x = sx;
/*TODO*////*TODO*///							cx = startx;
/*TODO*////*TODO*///							cy = starty >> 11;
/*TODO*////*TODO*///							dest = &((unsigned short *)bitmap->line[sy])[sx];
/*TODO*////*TODO*///							while (x <= ex && (cx & 0xfff00000) == 0)
/*TODO*////*TODO*///							{
/*TODO*////*TODO*///								int c = ((unsigned short *)srcbitmap->line[cy])[cx >> 11];
/*TODO*////*TODO*///
/*TODO*////*TODO*///								if (c != palette_transparent_pen)
/*TODO*////*TODO*///									*dest = c;
/*TODO*////*TODO*///
/*TODO*////*TODO*///								cx += incxx;
/*TODO*////*TODO*///								x++;
/*TODO*////*TODO*///								dest++;
/*TODO*////*TODO*///							}
/*TODO*////*TODO*///						}
/*TODO*////*TODO*///						starty += incyy;
/*TODO*////*TODO*///						sy++;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///			}
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///		else
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			if (K051316_wraparound[chip])
/*TODO*////*TODO*///			{
/*TODO*////*TODO*///				/* plot with wraparound */
/*TODO*////*TODO*///				while (sy <= ey)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					x = sx;
/*TODO*////*TODO*///					cx = startx;
/*TODO*////*TODO*///					cy = starty;
/*TODO*////*TODO*///					dest = &((unsigned short *)bitmap->line[sy])[sx];
/*TODO*////*TODO*///					while (x <= ex)
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						int c = ((unsigned short *)srcbitmap->line[(cy >> 11) & 0x1ff])[(cx >> 11) & 0x1ff];
/*TODO*////*TODO*///
/*TODO*////*TODO*///						if (c != palette_transparent_pen)
/*TODO*////*TODO*///							*dest = c;
/*TODO*////*TODO*///
/*TODO*////*TODO*///						cx += incxx;
/*TODO*////*TODO*///						cy += incxy;
/*TODO*////*TODO*///						x++;
/*TODO*////*TODO*///						dest++;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///					startx += incyx;
/*TODO*////*TODO*///					starty += incyy;
/*TODO*////*TODO*///					sy++;
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///			}
/*TODO*////*TODO*///			else
/*TODO*////*TODO*///			{
/*TODO*////*TODO*///				while (sy <= ey)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					x = sx;
/*TODO*////*TODO*///					cx = startx;
/*TODO*////*TODO*///					cy = starty;
/*TODO*////*TODO*///					dest = &((unsigned short *)bitmap->line[sy])[sx];
/*TODO*////*TODO*///					while (x <= ex)
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						if ((cx & 0xfff00000) == 0 && (cy & 0xfff00000) == 0)
/*TODO*////*TODO*///						{
/*TODO*////*TODO*///							int c = ((unsigned short *)srcbitmap->line[cy >> 11])[cx >> 11];
/*TODO*////*TODO*///
/*TODO*////*TODO*///							if (c != palette_transparent_pen)
/*TODO*////*TODO*///								*dest = c;
/*TODO*////*TODO*///						}
/*TODO*////*TODO*///
/*TODO*////*TODO*///						cx += incxx;
/*TODO*////*TODO*///						cy += incxy;
/*TODO*////*TODO*///						x++;
/*TODO*////*TODO*///						dest++;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///					startx += incyx;
/*TODO*////*TODO*///					starty += incyy;
/*TODO*////*TODO*///					sy++;
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///			}
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///#if 0
/*TODO*////*TODO*///	usrintf_showmessage("%02x%02x%02x%02x %02x%02x%02x%02x %02x%02x%02x%02x %02x%02x%02x%02x",
/*TODO*////*TODO*///			K051316_ctrlram[chip][0x00],
/*TODO*////*TODO*///			K051316_ctrlram[chip][0x01],
/*TODO*////*TODO*///			K051316_ctrlram[chip][0x02],
/*TODO*////*TODO*///			K051316_ctrlram[chip][0x03],
/*TODO*////*TODO*///			K051316_ctrlram[chip][0x04],
/*TODO*////*TODO*///			K051316_ctrlram[chip][0x05],
/*TODO*////*TODO*///			K051316_ctrlram[chip][0x06],
/*TODO*////*TODO*///			K051316_ctrlram[chip][0x07],
/*TODO*////*TODO*///			K051316_ctrlram[chip][0x08],
/*TODO*////*TODO*///			K051316_ctrlram[chip][0x09],
/*TODO*////*TODO*///			K051316_ctrlram[chip][0x0a],
/*TODO*////*TODO*///			K051316_ctrlram[chip][0x0b],
/*TODO*////*TODO*///			K051316_ctrlram[chip][0x0c],	/* bank for ROM testing */
/*TODO*////*TODO*///			K051316_ctrlram[chip][0x0d],
/*TODO*////*TODO*///			K051316_ctrlram[chip][0x0e],	/* 0 = test ROMs */
/*TODO*////*TODO*///			K051316_ctrlram[chip][0x0f]);
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051316_zoom_draw_0(struct osd_bitmap *bitmap)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K051316_zoom_draw(0, bitmap);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051316_zoom_draw_1(struct osd_bitmap *bitmap)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K051316_zoom_draw(1, bitmap);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051316_zoom_draw_2(struct osd_bitmap *bitmap)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	K051316_zoom_draw(2, bitmap);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///static unsigned char K053251_ram[16];
/*TODO*////*TODO*///static int K053251_palette_index[5];
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K053251_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	data &= 0x3f;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	if (K053251_ram[offset] != data)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		K053251_ram[offset] = data;
/*TODO*////*TODO*///		if (offset == 9)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			/* palette base index */
/*TODO*////*TODO*///			K053251_palette_index[0] = 32 * ((data >> 0) & 0x03);
/*TODO*////*TODO*///			K053251_palette_index[1] = 32 * ((data >> 2) & 0x03);
/*TODO*////*TODO*///			K053251_palette_index[2] = 32 * ((data >> 4) & 0x03);
/*TODO*////*TODO*///			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///		else if (offset == 10)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			/* palette base index */
/*TODO*////*TODO*///			K053251_palette_index[3] = 16 * ((data >> 0) & 0x07);
/*TODO*////*TODO*///			K053251_palette_index[4] = 16 * ((data >> 3) & 0x07);
/*TODO*////*TODO*///			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///#if 0
/*TODO*////*TODO*///else
/*TODO*////*TODO*///{
/*TODO*////*TODO*///if (errorlog)
/*TODO*////*TODO*///fprintf(errorlog,"%04x: write %02x to K053251 register %04x\n",cpu_get_pc(),data&0xff,offset);
/*TODO*////*TODO*///usrintf_showmessage("pri = %02x%02x%02x%02x %02x%02x%02x%02x %02x%02x%02x%02x %02x%02x%02x%02x",
/*TODO*////*TODO*///	K053251_ram[0],K053251_ram[1],K053251_ram[2],K053251_ram[3],
/*TODO*////*TODO*///	K053251_ram[4],K053251_ram[5],K053251_ram[6],K053251_ram[7],
/*TODO*////*TODO*///	K053251_ram[8],K053251_ram[9],K053251_ram[10],K053251_ram[11],
/*TODO*////*TODO*///	K053251_ram[12],K053251_ram[13],K053251_ram[14],K053251_ram[15]
/*TODO*////*TODO*///	);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K053251_get_priority(int ci)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K053251_ram[ci];
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K053251_get_palette_index(int ci)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K053251_palette_index[ci];
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///static unsigned char K054000_ram[0x20];
/*TODO*////*TODO*///
/*TODO*////*TODO*///static void collision_w( int offs, int data )
/*TODO*////*TODO*///{
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K054000_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///#if VERBOSE
/*TODO*////*TODO*///if (errorlog) fprintf(errorlog,"%04x: write %02x to 054000 address %02x\n",cpu_get_pc(),data,offset);
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///
/*TODO*////*TODO*///	K054000_ram[offset] = data;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K054000_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	int Acx,Acy,Aax,Aay;
/*TODO*////*TODO*///	int Bcx,Bcy,Bax,Bay;
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///#if VERBOSE
/*TODO*////*TODO*///if (errorlog) fprintf(errorlog,"%04x: read 054000 address %02x\n",cpu_get_pc(),offset);
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///
/*TODO*////*TODO*///	if (offset != 0x18) return 0;
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///	Acx = (K054000_ram[0x01] << 16) | (K054000_ram[0x02] << 8) | K054000_ram[0x03];
/*TODO*////*TODO*///	Acy = (K054000_ram[0x09] << 16) | (K054000_ram[0x0a] << 8) | K054000_ram[0x0b];
/*TODO*////*TODO*////* TODO: this is a hack to make thndrx2 pass the startup check. It is certainly wrong. */
/*TODO*////*TODO*///if (K054000_ram[0x04] == 0xff) Acx+=3;
/*TODO*////*TODO*///if (K054000_ram[0x0c] == 0xff) Acy+=3;
/*TODO*////*TODO*///	Aax = K054000_ram[0x06] + 1;
/*TODO*////*TODO*///	Aay = K054000_ram[0x07] + 1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	Bcx = (K054000_ram[0x15] << 16) | (K054000_ram[0x16] << 8) | K054000_ram[0x17];
/*TODO*////*TODO*///	Bcy = (K054000_ram[0x11] << 16) | (K054000_ram[0x12] << 8) | K054000_ram[0x13];
/*TODO*////*TODO*///	Bax = K054000_ram[0x0e] + 1;
/*TODO*////*TODO*///	Bay = K054000_ram[0x0f] + 1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	if (Acx + Aax < Bcx - Bax)
/*TODO*////*TODO*///		return 1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	if (Bcx + Bax < Acx - Aax)
/*TODO*////*TODO*///		return 1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	if (Acy + Aay < Bcy - Bay)
/*TODO*////*TODO*///		return 1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	if (Bcy + Bay < Acy - Aay)
/*TODO*////*TODO*///		return 1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	return 0;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///static unsigned char K051733_ram[0x20];
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K051733_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///#if VERBOSE
/*TODO*////*TODO*///if (errorlog) fprintf(errorlog,"%04x: write %02x to 051733 address %02x\n",cpu_get_pc(),data,offset);
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///
/*TODO*////*TODO*///	K051733_ram[offset] = data;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K051733_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	int op1 = (K051733_ram[0x00] << 8) | K051733_ram[0x01];
/*TODO*////*TODO*///	int op2 = (K051733_ram[0x02] << 8) | K051733_ram[0x03];
/*TODO*////*TODO*///
/*TODO*////*TODO*///	int rad = (K051733_ram[0x06] << 8) | K051733_ram[0x07];
/*TODO*////*TODO*///	int yobj1c = (K051733_ram[0x08] << 8) | K051733_ram[0x09];
/*TODO*////*TODO*///	int xobj1c = (K051733_ram[0x0a] << 8) | K051733_ram[0x0b];
/*TODO*////*TODO*///	int yobj2c = (K051733_ram[0x0c] << 8) | K051733_ram[0x0d];
/*TODO*////*TODO*///	int xobj2c = (K051733_ram[0x0e] << 8) | K051733_ram[0x0f];
/*TODO*////*TODO*///
/*TODO*////*TODO*///#if VERBOSE
/*TODO*////*TODO*///if (errorlog) fprintf(errorlog,"%04x: read 051733 address %02x\n",cpu_get_pc(),offset);
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///
/*TODO*////*TODO*///	switch(offset){
/*TODO*////*TODO*///		case 0x00:
/*TODO*////*TODO*///			if (op2) return	((op1/op2) >> 8);
/*TODO*////*TODO*///			else return 0xff;
/*TODO*////*TODO*///		case 0x01:
/*TODO*////*TODO*///			if (op2) return	op1/op2;
/*TODO*////*TODO*///			else return 0xff;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* this is completely unverified */
/*TODO*////*TODO*///		case 0x02:
/*TODO*////*TODO*///			if (op2) return	((op1%op2) >> 8);
/*TODO*////*TODO*///			else return 0xff;
/*TODO*////*TODO*///		case 0x03:
/*TODO*////*TODO*///			if (op2) return	op1%op2;
/*TODO*////*TODO*///			else return 0xff;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		case 0x07:{
/*TODO*////*TODO*///			if (xobj1c + rad < xobj2c - rad)
/*TODO*////*TODO*///				return 0x80;
/*TODO*////*TODO*///
/*TODO*////*TODO*///			if (xobj2c + rad < xobj1c - rad)
/*TODO*////*TODO*///				return 0x80;
/*TODO*////*TODO*///
/*TODO*////*TODO*///			if (yobj1c + rad < yobj2c - rad)
/*TODO*////*TODO*///				return 0x80;
/*TODO*////*TODO*///
/*TODO*////*TODO*///			if (yobj2c + rad < yobj1c - rad)
/*TODO*////*TODO*///				return 0x80;
/*TODO*////*TODO*///
/*TODO*////*TODO*///			return 0;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///		default:
/*TODO*////*TODO*///			return K051733_ram[offset];
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
    }