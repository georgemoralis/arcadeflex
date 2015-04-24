package vidhrdw;

import static arcadeflex.libc.*;
import static arcadeflex.libc_old.*;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.mame.*;
import static mame.tilemapC.*;
import static mame.tilemapH.*;
import static mame.palette.*;
import static mame.cpuintrf.*;
import static mame.common.*;
import static mame.paletteH.*;
import static mame.mameH.*;
import static mame.cpuintrfH.*;
import static mame.memoryH.*;
import static arcadeflex.ptrlib.*;

public  class konamiic
{
    public static FILE konamiicclog=fopen("konamiicc.log", "wa");  //for debug purposes
    
    /**
    *   Macros from konamiic.h and specific arcadeflex ones
    */
        public static final int K053251_CI0=0;
        public static final int K053251_CI1=1;
        public static final int K053251_CI2=2;
        public static final int K053251_CI3=3;
        public static final int K053251_CI4=4;
        //K052109_callback interface
        public static abstract interface K052109_callbackProcPtr { public abstract void handler(int layer,int bank,int[] code,int[] color); }
        //K051960_callback interface
        public static abstract interface K051960_callbackProcPtr { public abstract void handler(int[] code,int[] color,int[] priority); }
        //K053245 callback interface
        public static abstract interface K053245_callbackProcPtr { public abstract void handler(int[] code,int[] color,int[] priority); }
        //(*K051316_callback[MAX_K051316])(int *code,int *color);
        public static abstract interface K051316_callbackProcPtr { public abstract void handler(int[] code,int[] color); }
    /**
     *  
     * end of macros 
     */
    
        /*
                This recursive function doesn't use additional memory
                (it could be easily converted into an iterative one).
                It's called shuffle because it mimics the shuffling of a deck of cards.
        */
        static void shuffle(ShortPtr buf,int len)
        {
                int i;
                char t;

                if (len == 2) return;

                if ((len % 4)!=0) 
                {
                    throw new UnsupportedOperationException("Error in shuffle konamicc");
                }   /* must not happen */

                len /= 2;

                for (i = 0;i < len/2;i++)
                {
                        t = buf.read(len/2 + i);
                        buf.write(len/2 + i,buf.read(len + i));
                        buf.write(len + i,t);
                }

                shuffle(buf,len);
                shuffle(new ShortPtr(buf,len*2),len);//len*2 ??
        }


        /* helper function to join two 16-bit ROMs and form a 32-bit data stream */
        public static void konami_rom_deinterleave_2(int mem_region)
        {
        	shuffle(new ShortPtr(memory_region(mem_region).memory,memory_region(mem_region).offset),memory_region_length(mem_region)/2);
        }

        /* helper function to join four 16-bit ROMs and form a 64-bit data stream */
        public static void konami_rom_deinterleave_4(int mem_region)
        {
                konami_rom_deinterleave_2(mem_region);
                konami_rom_deinterleave_2(mem_region);
        }


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
                    UBytePtr source,int base_color,int global_x_offset,int bank_base)
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
    public static void K007121_mark_sprites_colors(int chip,UBytePtr source,int base_color,int bank_base)
    {
            int i,num,inc;
            int[] offs=new int[5];
            int is_flakatck = K007121_ctrlram[chip][0x06] & 0x04;	/* WRONG!!!! */

            char[] palette_map=new char[512];

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
            }
            else	/* all others */
            {
                    num = (K007121_ctrlram[chip][0x03] & 0x40)!=0 ? 0x80 : 0x40;
                    inc = 5;
                    offs[0] = 0x00;
                    offs[1] = 0x01;
                    offs[2] = 0x02;
                    offs[3] = 0x03;
                    offs[4] = 0x04;
            }

            memset (palette_map, 0, sizeof (palette_map));

            /* sprites */
            for (i = 0;i < num;i++)
            {
                    int color;

                    color = base_color + ((source.read(offs[1]) & 0xf0) >> 4);
                    palette_map[color] |= 0xffff;

                    source.inc(inc);
            }

            /* now build the final table */
            for (i = 0; i < 512; i++)
            {
                    int usage = palette_map[i], j;
                    if (usage!=0)
                    {
                            for (j = 0; j < 16; j++)
                                    if ((usage & (1 << j))!=0)
                                            palette_used_colors.write(i * 16 + j,palette_used_colors.read(i * 16 + j) | PALETTE_COLOR_VISIBLE);
                    }
            }
    }


        /***************************************************************************

          Callbacks for the TileMap code

        ***************************************************************************/

        /*
          data format:
          video RAM     xxxxxxxx    tile number (bits 0-7)
          color RAM     x-------    tiles with priority over the sprites
          color RAM     -x------    depends on external conections
          color RAM     --x-----    flip Y
          color RAM     ---x----    flip X
          color RAM     ----xxxx    depends on external connections (usually color and banking)
        */

        public static UBytePtr colorram,videoram1,videoram2;
        public static int layer;

        

        static int K052109_memory_region;
        static int K052109_gfxnum;
        static K052109_callbackProcPtr K052109_callback;
        static UBytePtr K052109_ram;
        static UBytePtr K052109_videoram_F,K052109_videoram2_F,K052109_colorram_F;
        static UBytePtr K052109_videoram_A,K052109_videoram2_A,K052109_colorram_A;
        static UBytePtr K052109_videoram_B,K052109_videoram2_B,K052109_colorram_B;
        static /*unsigned*/ char[] K052109_charrombank=new char[4];
        static int has_extra_video_ram;
        static int K052109_RMRD_line;
        static int K052109_tileflip_enable;
        static int K052109_irq_enabled;
        static /*unsigned*/ char K052109_romsubbank,K052109_scrollctrl;
        static tilemap[] K052109_tilemap=new tilemap[3];
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

        static void tilemap0_preupdate()
        {
                colorram = K052109_colorram_F;
                videoram1 = K052109_videoram_F;
                videoram2 = K052109_videoram2_F;
                layer = 0;
        }

        static void tilemap1_preupdate()
        {
                colorram = K052109_colorram_A;
                videoram1 = K052109_videoram_A;
                videoram2 = K052109_videoram2_A;
                layer = 1;
        }

        static void tilemap2_preupdate()
        {
                colorram = K052109_colorram_B;
                videoram1 = K052109_videoram_B;
                videoram2 = K052109_videoram2_B;
                layer = 2;
        }

	public static WriteHandlerPtr K052109_get_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
            int flipy = 0;
            int tile_index = 64*row+col;
            int[] code = new int[1];
            int[] color = new int[1];
            code[0] = videoram1.read(tile_index) + 256 * videoram2.read(tile_index);
            color[0] = colorram.read(tile_index);
            int bank = K052109_charrombank[(color[0] & 0x0c) >> 2] & 0xFF;
            if (has_extra_video_ram!=0) bank = (color[0] & 0x0c) >> 2;	/* kludge for X-Men */
            color[0] = (color[0] & 0xf3) | ((bank & 0x03) << 2);
            bank >>= 2;

            flipy = color[0] & 0x02;

            tile_info.flags = 0;

            K052109_callback.handler(layer,bank,code,color);///(*K052109_callback)(layer,bank,&code,&color);

            SET_TILE_INFO(K052109_gfxnum,code[0],color[0]);

            /* if the callback set flip X but it is not enabled, turn it off */
            if ((K052109_tileflip_enable & 1)==0) tile_info.flags &= ~TILE_FLIPX;

            /* if flip Y is enabled and the attribute but is set, turn it on */
            if (flipy!=0 && (K052109_tileflip_enable & 2)!=0) tile_info.flags |= TILE_FLIPY;
        }};

    public static int K052109_vh_start(int gfx_memory_region,int plane0,int plane1,int plane2,int plane3,K052109_callbackProcPtr callback)
    {
	int gfx_index;
	GfxLayout charlayout = new GfxLayout
	(
		8,8,
		0,				/* filled in later */
		4,
		new int[]{ 0, 0, 0, 0 },	/* filled in later */
		new int[]{ 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[]{ 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8
        );


	/* find first empty slot to decode gfx */
	for (gfx_index = 0; gfx_index < MAX_GFX_ELEMENTS; gfx_index++)
		if (Machine.gfx[gfx_index] == null)
			break;
	if (gfx_index == MAX_GFX_ELEMENTS)
		return 1;

	/* tweak the structure for the number of tiles we have */
	charlayout.total = memory_region_length(gfx_memory_region) / 32;
	charlayout.planeoffset[0] = plane3 * 8;
	charlayout.planeoffset[1] = plane2 * 8;
	charlayout.planeoffset[2] = plane1 * 8;
	charlayout.planeoffset[3] = plane0 * 8;

	/* decode the graphics */
	Machine.gfx[gfx_index] = decodegfx(memory_region(gfx_memory_region),charlayout);
	if (Machine.gfx[gfx_index]==null)
		return 1;

	/* set the color information */
	Machine.gfx[gfx_index].colortable = new CharPtr(Machine.remapped_colortable);
	Machine.gfx[gfx_index].total_colors = Machine.drv.color_table_len / 16;

	K052109_memory_region = gfx_memory_region;
	K052109_gfxnum = gfx_index;
	K052109_callback = callback;
	K052109_RMRD_line = CLEAR_LINE;

	has_extra_video_ram = 0;

	K052109_tilemap[0] = tilemap_create(K052109_get_tile_info,TILEMAP_TRANSPARENT,8,8,64,32);
	K052109_tilemap[1] = tilemap_create(K052109_get_tile_info,TILEMAP_TRANSPARENT,8,8,64,32);
	K052109_tilemap[2] = tilemap_create(K052109_get_tile_info,TILEMAP_TRANSPARENT,8,8,64,32);

	K052109_ram = new UBytePtr(0x6000);

	if (K052109_ram==null || K052109_tilemap[0]==null || K052109_tilemap[1]==null || K052109_tilemap[2]==null)
	{
		K052109_vh_stop();
		return 1;
	}

	for (int i = 0; i < 0x6000; i++) K052109_ram.write(i,0);//memset(K052109_ram,0,0x6000);

	K052109_colorram_F = new UBytePtr(K052109_ram,0x0000);
	K052109_colorram_A = new UBytePtr(K052109_ram,0x0800);
	K052109_colorram_B = new UBytePtr(K052109_ram,0x1000);
	K052109_videoram_F = new UBytePtr(K052109_ram,0x2000);
	K052109_videoram_A = new UBytePtr(K052109_ram,0x2800);
	K052109_videoram_B = new UBytePtr(K052109_ram,0x3000);
	K052109_videoram2_F = new UBytePtr(K052109_ram,0x4000);
	K052109_videoram2_A = new UBytePtr(K052109_ram,0x4800);
	K052109_videoram2_B = new UBytePtr(K052109_ram,0x5000);

	K052109_tilemap[0].transparent_pen = 0;
	K052109_tilemap[1].transparent_pen = 0;
	K052109_tilemap[2].transparent_pen = 0;

        	return 0;
    }
    public static void K052109_vh_stop()
    {
	K052109_ram = null;
    }
    public static ReadHandlerPtr K052109_r = new ReadHandlerPtr() { public int handler(int offset)
    {
                if (K052109_RMRD_line == CLEAR_LINE)
                {
                        if ((offset & 0x1fff) >= 0x1800)
                        {
                                if (offset >= 0x180c && offset < 0x1834)
                                {	/* A y scroll */	}
                                else if (offset >= 0x1a00 && offset < 0x1c00)
                                {	/* A x scroll */	}
                                else if (offset == 0x1d00)
                                {	/* read for bitwise operations before writing */	}
                                else if (offset >= 0x380c && offset < 0x3834)
                                {	/* B y scroll */	}
                                else if (offset >= 0x3a00 && offset < 0x3c00)
                                {	/* B x scroll */	}
                                else
                                    if (errorlog!=null) fprintf(errorlog,"%04x: read from unknown 052109 address %04x\n",cpu_get_pc(),offset);
                        }

                        return K052109_ram.read(offset);
                }
                else	/* Punk Shot and TMNT read from 0000-1fff, Aliens from 2000-3fff */
                {
                        int[] code = new int[1];
                        int[] color = new int[1];
                        code[0] = (offset & 0x1fff) >> 5;
                        color[0] = K052109_romsubbank;
                        int bank = (K052109_charrombank[(color[0] & 0x0c) >> 2] >> 2)&0xFF;   /* discard low bits (TMNT) */
                        int addr;

                        if (has_extra_video_ram!=0) code[0] |= color[0] << 8;	/* kludge for X-Men */
                        else
                          K052109_callback.handler(0, bank, code, color);//(*K052109_callback)(0,bank,&code,&color);

                        addr = (code[0] << 5) + (offset & 0x1f);
                        addr &= memory_region_length(K052109_memory_region)-1;


                        return memory_region(K052109_memory_region).read(addr);
                }
        }};
    	public static WriteHandlerPtr K052109_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
            	if ((offset & 0x1fff) < 0x1800) /* tilemap RAM */
	{
		if (K052109_ram.read(offset) != data)
		{
			if (offset >= 0x4000) has_extra_video_ram = 1;  /* kludge for X-Men */
			K052109_ram.write(offset,data);
			tilemap_mark_tile_dirty(K052109_tilemap[(offset&0x1fff)/0x800],offset%64,(offset%0x800)/64);
		}
	}
	else	/* control registers */
	{
		K052109_ram.write(offset,data);

		if (offset >= 0x180c && offset < 0x1834)
		{	/* A y scroll */	}
		else if (offset >= 0x1a00 && offset < 0x1c00)
		{	/* A x scroll */	}
		else if (offset == 0x1c80)
		{
                    if (K052109_scrollctrl != data)
                    {
                            if (errorlog!=null) fprintf(errorlog,"%04x: rowscrollcontrol = %02x\n",cpu_get_pc(),data);
                                 K052109_scrollctrl = (char)(data&0xFF);
                    }
                }
                else if (offset == 0x1d00)
                {
                    //#if VERBOSE
                    //if (errorlog) fprintf(errorlog,"%04x: 052109 register 1d00 = %02x\n",cpu_get_pc(),data);
                    //#endif
                                    /* bit 2 = irq enable */
                                    /* the custom chip can also generate NMI and FIRQ, for use with a 6809 */
                                    K052109_irq_enabled = data & 0x04;
                }
                            else if (offset == 0x1d80)
                            {
                                    int dirty = 0;

                                    if (K052109_charrombank[0] != (data & 0x0f)) dirty |= 1;
                                    if (K052109_charrombank[1] != ((data >> 4) & 0x0f)) dirty |= 2;
                                    if (dirty!=0)
                                    {
                                            int i;

                                            K052109_charrombank[0] = (char)((data & 0x0f)&0xFF);
                                            K052109_charrombank[1] = (char)(((data >> 4) & 0x0f)&0xFF);

                                            for (i = 0;i < 0x1800;i++)
                                            {
                                                    int bank = (K052109_ram.read(i)&0x0c) >> 2;
                                                    if ((bank == 0 && (dirty & 1) != 0) || (bank == 1 && (dirty & 2) != 0))
                                                    {
                                                            tilemap_mark_tile_dirty(K052109_tilemap[(i&0x1fff)/0x800],i%64,(i%0x800)/64);
                                                    }
                                            }
                                    }
                            }
                            else if (offset == 0x1e00)
                            {
                                if (errorlog!=null) fprintf(errorlog,"%04x: 052109 register 1e00 = %02x\n",cpu_get_pc(),data);
                                    K052109_romsubbank = (char)(data&0xFF);
                            }
                            else if (offset == 0x1e80)
                            {
                                if (errorlog!=null && (data & 0xfe)!=0) fprintf(errorlog,"%04x: 052109 register 1e80 = %02x\n",cpu_get_pc(),data);
                                    tilemap_set_flip(K052109_tilemap[0],(data & 1)!=0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
                                    tilemap_set_flip(K052109_tilemap[1],(data & 1)!=0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
                                    tilemap_set_flip(K052109_tilemap[2],(data & 1)!=0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
                                    if (K052109_tileflip_enable != ((data & 0x06) >> 1))
                                    {
                                            K052109_tileflip_enable = ((data & 0x06) >> 1);

                                            tilemap_mark_all_tiles_dirty(K052109_tilemap[0]);
                                            tilemap_mark_all_tiles_dirty(K052109_tilemap[1]);
                                            tilemap_mark_all_tiles_dirty(K052109_tilemap[2]);
                                    }
                            }
                            else if (offset == 0x1f00)
                            {
                                    int dirty = 0;

                                    if (K052109_charrombank[2] != (data & 0x0f)) dirty |= 1;
                                    if (K052109_charrombank[3] != ((data >> 4) & 0x0f)) dirty |= 2;
                                    if (dirty!=0)
                                    {
                                            int i;

                                            K052109_charrombank[2] = (char)((data & 0x0f)&0xFF);
                                            K052109_charrombank[3] = (char)(((data >> 4) & 0x0f)&0xFF);

                                            for (i = 0;i < 0x1800;i++)
                                            {
                                                    int bank = (K052109_ram.read(i) & 0x0c) >> 2;
                                                    if ((bank == 2 && (dirty & 1) != 0) || (bank == 3 && (dirty & 2) != 0))
                                                            tilemap_mark_tile_dirty(K052109_tilemap[(i&0x1fff)/0x800],i%64,(i%0x800)/64);
                                            }
                                    }
                            }
                            else if (offset >= 0x380c && offset < 0x3834)
                            {	/* B y scroll */	}
                            else if (offset >= 0x3a00 && offset < 0x3c00)
                            {	/* B x scroll */	}
                            else
                               if (errorlog!=null) fprintf(errorlog,"%04x: write %02x to unknown 052109 address %04x\n",cpu_get_pc(),data,offset);
	}
        }};
        public static void K052109_set_RMRD_line(int state)
        {
                K052109_RMRD_line = state;
        }

        public static void K052109_tilemap_update()
        {
         
                if ((K052109_scrollctrl & 0x03) == 0x02)
                {
                        int xscroll,yscroll,offs;
                        UBytePtr scrollram = new UBytePtr(K052109_ram,0x1a00);


                        tilemap_set_scroll_rows(K052109_tilemap[1],256);
                        tilemap_set_scroll_cols(K052109_tilemap[1],1);
                        yscroll = K052109_ram.read(0x180c);
                        tilemap_set_scrolly(K052109_tilemap[1],0,yscroll);
                        for (offs = 0;offs < 256;offs++)
                        {
                                xscroll = scrollram.read(2*(offs&0xfff8)+0) + 256 * scrollram.read(2*(offs&0xfff8)+1);
                                xscroll -= 6;
                                tilemap_set_scrollx(K052109_tilemap[1],(offs+yscroll)&0xff,xscroll);
                        }
                }
                else if ((K052109_scrollctrl & 0x03) == 0x03)
                {
                        int xscroll,yscroll,offs;
                        UBytePtr scrollram = new UBytePtr(K052109_ram,0x1a00);


                        tilemap_set_scroll_rows(K052109_tilemap[1],256);
                        tilemap_set_scroll_cols(K052109_tilemap[1],1);
                        yscroll = K052109_ram.read(0x180c);
                        tilemap_set_scrolly(K052109_tilemap[1],0,yscroll);
                        for (offs = 0;offs < 256;offs++)
                        {
                                xscroll = scrollram.read(2*offs+0) + 256 * scrollram.read(2*offs+1);
                                xscroll -= 6;
                                tilemap_set_scrollx(K052109_tilemap[1],(offs+yscroll)&0xff,xscroll);
                        }
                }
                else if ((K052109_scrollctrl & 0x04) == 0x04)
                {
                        int xscroll,yscroll,offs;
                        UBytePtr scrollram = new UBytePtr(K052109_ram,0x1800);


                        tilemap_set_scroll_rows(K052109_tilemap[1],1);
                        tilemap_set_scroll_cols(K052109_tilemap[1],512);
                        xscroll = K052109_ram.read(0x1a00) + 256 * K052109_ram.read(0x1a01);
                        xscroll -= 6;
                        tilemap_set_scrollx(K052109_tilemap[1],0,xscroll);
                        for (offs = 0;offs < 512;offs++)
                        {
                                yscroll = scrollram.read(offs/8);
                                tilemap_set_scrolly(K052109_tilemap[1],(offs+xscroll)&0x1ff,yscroll);
                        }
                }
                else
                {
                        int xscroll,yscroll;
                        UBytePtr scrollram = new UBytePtr(K052109_ram,0x1a00);


                        tilemap_set_scroll_rows(K052109_tilemap[1],1);
                        tilemap_set_scroll_cols(K052109_tilemap[1],1);
                        xscroll = scrollram.read(0) + 256 * scrollram.read(1);
                        xscroll -= 6;
                        yscroll = K052109_ram.read(0x180c);
                        tilemap_set_scrollx(K052109_tilemap[1],0,xscroll);
                        tilemap_set_scrolly(K052109_tilemap[1],0,yscroll);
                }

                if ((K052109_scrollctrl & 0x18) == 0x10)
                {
                        int xscroll,yscroll,offs;
                        UBytePtr scrollram = new UBytePtr(K052109_ram,0x3a00);


                        tilemap_set_scroll_rows(K052109_tilemap[2],256);
                        tilemap_set_scroll_cols(K052109_tilemap[2],1);
                        yscroll = K052109_ram.read(0x380c);
                        tilemap_set_scrolly(K052109_tilemap[2],0,yscroll);
                        for (offs = 0;offs < 256;offs++)
                        {
                                xscroll = scrollram.read(2*(offs&0xfff8)+0) + 256 * scrollram.read(2*(offs&0xfff8)+1);
                                xscroll -= 6;
                                tilemap_set_scrollx(K052109_tilemap[2],(offs+yscroll)&0xff,xscroll);
                        }
                }
                else if ((K052109_scrollctrl & 0x18) == 0x18)
                {
                        int xscroll,yscroll,offs;
                        UBytePtr scrollram = new UBytePtr(K052109_ram,0x3a00);


                        tilemap_set_scroll_rows(K052109_tilemap[2],256);
                        tilemap_set_scroll_cols(K052109_tilemap[2],1);
                        yscroll = K052109_ram.read(0x380c);
                        tilemap_set_scrolly(K052109_tilemap[2],0,yscroll);
                        for (offs = 0;offs < 256;offs++)
                        {
                                xscroll = scrollram.read(2*offs+0) + 256 * scrollram.read(2*offs+1);
                                xscroll -= 6;
                                tilemap_set_scrollx(K052109_tilemap[2],(offs+yscroll)&0xff,xscroll);
                        }
                }
                else if ((K052109_scrollctrl & 0x20) == 0x20)
                {
                        int xscroll,yscroll,offs;
                        UBytePtr scrollram = new UBytePtr(K052109_ram,0x3800);


                        tilemap_set_scroll_rows(K052109_tilemap[2],1);
                        tilemap_set_scroll_cols(K052109_tilemap[2],512);
                        xscroll = K052109_ram.read(0x3a00) + 256 * K052109_ram.read(0x3a01);
                        xscroll -= 6;
                        tilemap_set_scrollx(K052109_tilemap[2],0,xscroll);
                        for (offs = 0;offs < 512;offs++)
                        {
                                yscroll = scrollram.read(offs/8);
                                tilemap_set_scrolly(K052109_tilemap[2],(offs+xscroll)&0x1ff,yscroll);
                        }
                }
                else
                {
                        int xscroll,yscroll;
                        UBytePtr scrollram = new UBytePtr(K052109_ram,0x3a00);


                        tilemap_set_scroll_rows(K052109_tilemap[2],1);
                        tilemap_set_scroll_cols(K052109_tilemap[2],1);
                        xscroll = scrollram.read(0) + 256 * scrollram.read(1);
                        xscroll -= 6;
                        yscroll = K052109_ram.read(0x380c);
                        tilemap_set_scrollx(K052109_tilemap[2],0,xscroll);
                        tilemap_set_scrolly(K052109_tilemap[2],0,yscroll);
                }

                tilemap0_preupdate(); tilemap_update(K052109_tilemap[0]);
                tilemap1_preupdate(); tilemap_update(K052109_tilemap[1]);
                tilemap2_preupdate(); tilemap_update(K052109_tilemap[2]);

                /*#ifdef MAME_DEBUG
                if ((K052109_scrollctrl & 0x03) == 0x01 ||
                                (K052109_scrollctrl & 0x18) == 0x08 ||
                                ((K052109_scrollctrl & 0x04) && (K052109_scrollctrl & 0x03)) ||
                                ((K052109_scrollctrl & 0x20) && (K052109_scrollctrl & 0x18)) ||
                                (K052109_scrollctrl & 0xc0) != 0)
                        usrintf_showmessage("scrollcontrol = %02x",K052109_scrollctrl);
                #endif*/

        }

        public static void K052109_tilemap_draw(osd_bitmap bitmap,int num,int flags)
        {
             tilemap_draw(bitmap,K052109_tilemap[num],flags);
        }
        public static int K052109_is_IRQ_enabled()
        {
                return K052109_irq_enabled;
        }

        static int K051960_memory_region;
        static GfxElement K051960_gfx;
        static K051960_callbackProcPtr K051960_callback;//static void (*K051960_callback)(int *code,int *color,int *priority);
        static int K051960_romoffset;
        static int K051960_spriteflip,K051960_readroms;
        static /*unsigned*/ char[] K051960_spriterombank=new char[3];
        static UBytePtr K051960_ram;
        static int K051960_irq_enabled, K051960_nmi_enabled;


        public static int K051960_vh_start(int gfx_memory_region,int plane0,int plane1,int plane2,int plane3,K051960_callbackProcPtr callback)
        {
            int gfx_index;
            GfxLayout spritelayout = new GfxLayout
            (
                    16,16,
                    0,				/* filled in later */
                    4,
                    new int[]{ 0, 0, 0, 0 },	/* filled in later */
                    new int[]{ 0, 1, 2, 3, 4, 5, 6, 7,
                                    8*32+0, 8*32+1, 8*32+2, 8*32+3, 8*32+4, 8*32+5, 8*32+6, 8*32+7 },
                    new int[]{ 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32,
                                    16*32, 17*32, 18*32, 19*32, 20*32, 21*32, 22*32, 23*32 },
                    128*8
            );


            /* find first empty slot to decode gfx */
            for (gfx_index = 0; gfx_index < MAX_GFX_ELEMENTS; gfx_index++)
                    if (Machine.gfx[gfx_index] == null)
                            break;
            if (gfx_index == MAX_GFX_ELEMENTS)
                    return 1;

            /* tweak the structure for the number of tiles we have */
            spritelayout.total = memory_region_length(gfx_memory_region) / 128;
            spritelayout.planeoffset[0] = plane0 * 8;
            spritelayout.planeoffset[1] = plane1 * 8;
            spritelayout.planeoffset[2] = plane2 * 8;
            spritelayout.planeoffset[3] = plane3 * 8;

            /* decode the graphics */
            Machine.gfx[gfx_index] = decodegfx(memory_region(gfx_memory_region),spritelayout);
            if (Machine.gfx[gfx_index]==null)
                    return 1;

            /* set the color information */
            Machine.gfx[gfx_index].colortable = new CharPtr(Machine.remapped_colortable);
            Machine.gfx[gfx_index].total_colors = Machine.drv.color_table_len / 16;

            K051960_memory_region = gfx_memory_region;
            K051960_gfx = Machine.gfx[gfx_index];
            K051960_callback = callback;
            K051960_ram = new UBytePtr(0x400);
            if (K051960_ram==null) return 1;

            for (int i = 0; i < 0x400; i++) K051960_ram.write(i,0);//memset(K051960_ram,0,0x400);

            return 0;
        }
        public static void K051960_vh_stop()
        {
            K051960_ram = null;
        }


        public static int K051960_fetchromdata(int _byte)
        {
                int[] code=new int[1];
                int[] color=new int[1];
                int[] pri=new int[1];
                int off1,addr;


                addr = K051960_romoffset + (K051960_spriterombank[0] << 8) +
                                ((K051960_spriterombank[1] & 0x03) << 16);
                code[0] = (addr & 0x3ffe0) >> 5;
                off1 = addr & 0x1f;
                color[0] = ((K051960_spriterombank[1] & 0xfc) >> 2) + ((K051960_spriterombank[2] & 0x03) << 6);
                pri[0] = 0;
                K051960_callback.handler(code, color, pri);//(*K051960_callback)(&code,&color,&pri);

                addr = (code[0] << 7) | (off1 << 2) | _byte;
                addr &= memory_region_length(K051960_memory_region)-1;


                return memory_region(K051960_memory_region).read(addr);
        }
	public static ReadHandlerPtr K051960_r = new ReadHandlerPtr() { public int handler(int offset)
	{
            if (K051960_readroms!=0)
            {
                    /* the 051960 remembers the last address read and uses it when reading the sprite ROMs */
                    K051960_romoffset = (offset & 0x3fc) >> 2;
                    return K051960_fetchromdata(offset & 3);	/* only 88 Games reads the ROMs from here */
            }
            else
                    return K051960_ram.read(offset);
        }};
	public static WriteHandlerPtr K051960_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
            	K051960_ram.write(offset,data);
        }};
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
        static int K051937_counter;
	public static ReadHandlerPtr K051937_r = new ReadHandlerPtr() { public int handler(int offset)
	{
            if (K051960_readroms!=0 && offset >= 4 && offset < 8)
            {
                    return K051960_fetchromdata(offset & 3);
            }
            else
            {
                    if (offset == 0)
                    {


                            /* some games need bit 0 to pulse */
                            return (K051937_counter++) & 1;
                    }
                    if (errorlog!=null) fprintf(errorlog,"%04x: read unknown 051937 address %x\n",cpu_get_pc(),offset);
                    return 0;
            }
        }};
	public static WriteHandlerPtr K051937_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
            if (offset == 0)
            {
		/* bit 0 is IRQ enable */
		K051960_irq_enabled = (data & 0x01);

		/* bit 1: probably FIRQ enable */

		/* bit 2 is NMI enable */
		K051960_nmi_enabled = (data & 0x04);

		/* bit 3 = flip screen */
		K051960_spriteflip = data & 0x08;

		/* bit 4 used by Devastators and TMNT, unknown */

		/* bit 5 = enable gfx ROM reading */
		K051960_readroms = data & 0x20;
//#if VERBOSE
//if (errorlog) fprintf(errorlog,"%04x: write %02x to 051937 address %x\n",cpu_get_pc(),data,offset);
//#endif
	}
	else if (offset >= 2 && offset < 5)
	{
		K051960_spriterombank[offset - 2] = (char)(data&0xFF);
	}
	else
	{
            if (errorlog!=null) fprintf(errorlog,"%04x: write %02x to unknown 051937 address %x\n",cpu_get_pc(),data,offset);
	}
        }};

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
        public static void K051960_sprites_draw(osd_bitmap bitmap,int min_priority,int max_priority)
        {
            int NUM_SPRITES= 128;
            int offs,pri_code;
            int[] sortedlist=new int[NUM_SPRITES];

            for (offs = 0;offs < NUM_SPRITES;offs++)
                    sortedlist[offs] = -1;

            /* prebuild a sorted table */
            for (offs = 0;offs < 0x400;offs += 8)
            {
                    if ((K051960_ram.read(offs) & 0x80)!=0)
                            sortedlist[K051960_ram.read(offs) & 0x7f] = offs;
            }

            for (pri_code = 0;pri_code < NUM_SPRITES;pri_code++)
            {
                    int ox,oy,size,w,h,x,y,flipx,flipy,zoomx,zoomy;
                    int[] code =new int[1];
                    int[] color=new int[1];
                    int[] pri = new int[1];
                    /* sprites can be grouped up to 8x8. The draw order is
                             0  1  4  5 16 17 20 21
                             2  3  6  7 18 19 22 23
                             8  9 12 13 24 25 28 29
                            10 11 14 15 26 27 30 31
                            32 33 36 37 48 49 52 53
                            34 35 38 39 50 51 54 55
                            40 41 44 45 56 57 60 61
                            42 43 46 47 58 59 62 63
                    */
                    int xoffset[] = { 0, 1, 4, 5, 16, 17, 20, 21 };
                    int yoffset[] = { 0, 2, 8, 10, 32, 34, 40, 42 };
                    int width[] =  { 1, 2, 1, 2, 4, 2, 4, 8 };
                    int height[] = { 1, 1, 2, 2, 2, 4, 4, 8 };


                    offs = sortedlist[pri_code];
                    if (offs == -1) continue;

                    code[0] = K051960_ram.read(offs+2) + ((K051960_ram.read(offs+1) & 0x1f) << 8);
                    color[0] = K051960_ram.read(offs+3) & 0xff;
                    pri[0] = 0;

                    K051960_callback.handler(code, color, pri);//(*K051960_callback)(&code,&color,&pri);

                    if (pri[0] < min_priority || pri[0] > max_priority) continue;

                    size = (K051960_ram.read(offs+1) & 0xe0) >> 5;
                    w = width[size];
                    h = height[size];

                    if (w >= 2) code[0] &= ~0x01;
                    if (h >= 2) code[0] &= ~0x02;
                    if (w >= 4) code[0] &= ~0x04;
                    if (h >= 4) code[0] &= ~0x08;
                    if (w >= 8) code[0] &= ~0x10;
                    if (h >= 8) code[0] &= ~0x20;

                    ox = (256 * K051960_ram.read(offs+6) + K051960_ram.read(offs+7)) & 0x01ff;
                    oy = 256 - ((256 * K051960_ram.read(offs+4) + K051960_ram.read(offs+5)) & 0x01ff);
                    flipx = K051960_ram.read(offs+6) & 0x02;
                    flipy = K051960_ram.read(offs+4) & 0x02;
                    zoomx = (K051960_ram.read(offs+6) & 0xfc) >> 2;
                    zoomy = (K051960_ram.read(offs+4) & 0xfc) >> 2;
                    zoomx = 0x10000 / 128 * (128 - zoomx);
                    zoomy = 0x10000 / 128 * (128 - zoomy);

                    if (K051960_spriteflip!=0)
                    {
                            ox = 512 - (zoomx * w >> 12) - ox;
                            oy = 256 - (zoomy * h >> 12) - oy;
                            flipx = NOT(flipx);
                            flipy = NOT(flipy);
                    }

                    if (zoomx == 0x10000 && zoomy == 0x10000)
                    {
                            int sx,sy;

                            for (y = 0;y < h;y++)
                            {
                                    sy = oy + 16 * y;

                                    for (x = 0;x < w;x++)
                                    {
                                            int c = code[0];

                                            sx = ox + 16 * x;
                                            if (flipx!=0) c += xoffset[(w-1-x)];
                                            else c += xoffset[x];
                                            if (flipy!=0) c += yoffset[(h-1-y)];
                                            else c += yoffset[y];

                                            /* hack to simulate shadow */
                                            if ((K051960_ram.read(offs+3) & 0x80)!=0)
                                            {
                                                    int o = K051960_gfx.colortable.read(16*color[0]+15);
                                                    K051960_gfx.colortable.write(16*color[0]+15,palette_transparent_pen);
                                                    drawgfx(bitmap,K051960_gfx,
                                                                    c,
                                                                    color[0],
                                                                    flipx,flipy,
                                                                    sx & 0x1ff,sy,
                                                                    Machine.drv.visible_area,TRANSPARENCY_PENS,(cpu_getcurrentframe() & 1)!=0 ? 0x8001 : 0x0001);
                                                    K051960_gfx.colortable.write(16*color[0]+15,o);
                                            }
                                            else
                                                    drawgfx(bitmap,K051960_gfx,
                                                                    c,
                                                                    color[0],
                                                                    flipx,flipy,
                                                                    sx & 0x1ff,sy,
                                                                    Machine.drv.visible_area,TRANSPARENCY_PEN,0);
                                    }
                            }
                    }
                    else
                    {
                            int sx,sy,zw,zh;

                            for (y = 0;y < h;y++)
                            {
                                    sy = oy + ((zoomy * y + (1<<11)) >> 12);
                                    zh = (oy + ((zoomy * (y+1) + (1<<11)) >> 12)) - sy;

                                    for (x = 0;x < w;x++)
                                    {
                                            int c = code[0];

                                            sx = ox + ((zoomx * x + (1<<11)) >> 12);
                                            zw = (ox + ((zoomx * (x+1) + (1<<11)) >> 12)) - sx;
                                            if (flipx!=0) c += xoffset[(w-1-x)];
                                            else c += xoffset[x];
                                            if (flipy!=0) c += yoffset[(h-1-y)];
                                            else c += yoffset[y];

                                            drawgfxzoom(bitmap,K051960_gfx,
                                                            c,
                                                            color[0],
                                                            flipx,flipy,
                                                            sx & 0x1ff,sy,
                                                            Machine.drv.visible_area,TRANSPARENCY_PEN,0,
                                                            (zw << 16) / 16,(zh << 16) / 16);
                                    }
                            }
                    }
            }
        }

        public static void K051960_mark_sprites_colors()
        {
                int offs,i;

                /*unsigned short*/ char[] palette_map=new char[512];

                memset (palette_map, 0, sizeof (palette_map));

                /* sprites */
                for (offs = 0x400-8;offs >= 0;offs -= 8)
                {
                        if ((K051960_ram.read(offs) & 0x80)!=0)
                        {
                                //int code,color,pri;
                                int[] code = new int[1];
                                int[] color= new int[1];
                                int[] pri=new int[1];

                                code[0] = K051960_ram.read(offs+2) + ((K051960_ram.read(offs+1) & 0x1f) << 8);
                                color[0] = (K051960_ram.read(offs+3) & 0xff);
                                pri[0] = 0;
                                K051960_callback.handler(code, color, pri);//(*K051960_callback)(&code,&color,&pri);
                                palette_map[color[0]] |= 0xffff;
                        }
                }

                /* now build the final table */
                for (i = 0; i < 512; i++)
                {
                        int usage = palette_map[i], j;
                        if (usage!=0)
                        {
                                for (j = 1; j < 16; j++)
                                        if ((usage & (1 << j))!=0)
                                                palette_used_colors.write(i * 16 + j,palette_used_colors.read(i * 16 + j) | PALETTE_COLOR_VISIBLE);
                        }
                }
        }

        public static int K051960_is_IRQ_enabled()
        {
                return K051960_irq_enabled;
        }
/*TODO*////*TODO*///
/*TODO*////*TODO*///int K051960_is_NMI_enabled(void)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K051960_nmi_enabled;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///
   	public static ReadHandlerPtr K052109_051960_r = new ReadHandlerPtr() { public int handler(int offset)
	{
            	if (K052109_RMRD_line == CLEAR_LINE)
		{
			if (offset >= 0x3800 && offset < 0x3808)
				return K051937_r.handler(offset - 0x3800);
			else if (offset < 0x3c00)
				return K052109_r.handler(offset);
			else
				return K051960_r.handler(offset - 0x3c00);
		}
		else return K052109_r.handler(offset);
	} };
        
        public static WriteHandlerPtr K052109_051960_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (offset >= 0x3800 && offset < 0x3808)
			K051937_w.handler(offset - 0x3800,data);
		else if (offset < 0x3c00)
			K052109_w.handler(offset,data);
		else
			K051960_w.handler(offset - 0x3c00,data);
	} };





    static int K053245_memory_region=2;
    static GfxElement K053245_gfx;
    static K053245_callbackProcPtr K053245_callback;//static void (*K053245_callback)(int *code,int *color,int *priority);
    static int K053244_romoffset,K053244_rombank;
    static int K053244_readroms;
    static int K053245_flipscreenX,K053245_flipscreenY;
    static int K053245_spriteoffsX,K053245_spriteoffsY;
    static UBytePtr K053245_ram;

    public static int K053245_vh_start(int gfx_memory_region,int plane0,int plane1,int plane2,int plane3,
                    K053245_callbackProcPtr callback)
    {
            int gfx_index;
            GfxLayout spritelayout = new GfxLayout
            (
                    16,16,
                    0,				/* filled in later */
                    4,
                    new int[]{ 0, 0, 0, 0 },	/* filled in later */
                    new int[]{ 0, 1, 2, 3, 4, 5, 6, 7,
                                    8*32+0, 8*32+1, 8*32+2, 8*32+3, 8*32+4, 8*32+5, 8*32+6, 8*32+7 },
                    new int[]{ 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32,
                                    16*32, 17*32, 18*32, 19*32, 20*32, 21*32, 22*32, 23*32 },
                    128*8
            );

            /* find first empty slot to decode gfx */
            for (gfx_index = 0; gfx_index < MAX_GFX_ELEMENTS; gfx_index++)
                    if (Machine.gfx[gfx_index] == null)
                            break;
            if (gfx_index == MAX_GFX_ELEMENTS)
                    return 1;

            /* tweak the structure for the number of tiles we have */
            spritelayout.total = memory_region_length(gfx_memory_region) / 128;
            spritelayout.planeoffset[0] = plane3 * 8;
            spritelayout.planeoffset[1] = plane2 * 8;
            spritelayout.planeoffset[2] = plane1 * 8;
            spritelayout.planeoffset[3] = plane0 * 8;

            /* decode the graphics */
            Machine.gfx[gfx_index] = decodegfx(memory_region(gfx_memory_region),spritelayout);
            if (Machine.gfx[gfx_index]==null)
                    return 1;

            /* set the color information */
            Machine.gfx[gfx_index].colortable = new CharPtr(Machine.remapped_colortable);
            Machine.gfx[gfx_index].total_colors = Machine.drv.color_table_len / 16;

            K053245_memory_region = gfx_memory_region;
            K053245_gfx = Machine.gfx[gfx_index];
            K053245_callback = callback;
            K053244_rombank = 0;
            K053245_ram = new UBytePtr(0x800);
            if (K053245_ram==null) return 1;

            for (int i = 0; i < 0x800; i++) K053245_ram.write(i,0);//memset(K053245_ram,0,0x800);

            return 0;
    }

    public static void K053245_vh_stop()
    {
            K053245_ram = null;
    }
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
        public static ReadHandlerPtr K053245_r = new ReadHandlerPtr() { public int handler(int offset)
	{
       
            int shift = ((offset & 1) ^ 1) << 3;
            int d=(K053245_ram.READ_WORD(offset & ~1)>>>shift)&0xff;
            if(konamiicclog!=null) fprintf( konamiicclog,"read %d\n",d);
            System.out.println(d);
            return d;
                    
            //return (READ_WORD(&K053245_ram[offset & ~1]) >> shift) & 0xff;
        }};

        public static WriteHandlerPtr K053245_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
        	int shift = ((offset & 1) ^ 1) << 3;
        	offset &= ~1;
                //K053245_ram.COMBINE_WORD_MEM(offset,(char)((0xff000000 >> shift) | ((data & 0xff) << shift)));
        	COMBINE_WORD_MEM(K053245_ram,offset,(0xff000000 >> shift) | ((data & 0xff) << shift));
                if(konamiicclog!=null) fprintf( konamiicclog,"write shift=%d offset=%d write=%d\n",shift,offset,(int)K053245_ram.read(offset));

        }};

        public static ReadHandlerPtr K053244_r = new ReadHandlerPtr() { public int handler(int offset)
	{
                if (K053244_readroms!=0 && offset >= 0x0c && offset < 0x10)
                {
                        int addr;


                        addr = 0x200000 * K053244_rombank + 4 * (K053244_romoffset & 0x7ffff) + ((offset & 3) ^ 1);
                        addr &= memory_region_length(K053245_memory_region)-1;

        /*#if 0
                usrintf_showmessage("%04x: offset %02x addr %06x",cpu_get_pc(),offset&3,addr);
        #endif*/

                        return memory_region(K053245_memory_region).read(addr);
                }
                else
                {
                        if (errorlog!=null) fprintf(errorlog,"%04x: read from unknown 053244 address %x\n",cpu_get_pc(),offset);
                        return 0;
                }
        }};

        public static WriteHandlerPtr K053244_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
                if (offset == 0x00)
                        K053245_spriteoffsX = (K053245_spriteoffsX & 0x00ff) | (data << 8);
                else if (offset == 0x01)
                        K053245_spriteoffsX = (K053245_spriteoffsX & 0xff00) | data;
                else if (offset == 0x02)
                        K053245_spriteoffsY = (K053245_spriteoffsY & 0x00ff) | (data << 8);
                else if (offset == 0x03)
                        K053245_spriteoffsY = (K053245_spriteoffsY & 0xff00) | data;
                else if (offset == 0x05)
                {
        /*#ifdef MAME_DEBUG
        if (data & 0xc8)
                usrintf_showmessage("053244 reg 05 = %02x",data);
        #endif*/
                        /* bit 0/1 = flip screen */
                        K053245_flipscreenX = data & 0x01;
                        K053245_flipscreenY = data & 0x02;

                        /* bit 2 = unknown, Parodius uses it */

                        /* bit 4 = enable gfx ROM reading */
                        K053244_readroms = data & 0x10;

                        /* bit 5 = unknown, Rollergames uses it */
        /*#if VERBOSE
        if (errorlog) fprintf(errorlog,"%04x: write %02x to 053244 address 5\n",cpu_get_pc(),data);
        #endif*/
                }
                else if (offset >= 0x08 && offset < 0x0c)
                {
                        offset = 8*((offset & 0x03) ^ 0x01);
                        K053244_romoffset = (K053244_romoffset & ~(0xff << offset)) | (data << offset);
                        return;
                }
                else
        if (errorlog!=null) fprintf(errorlog,"%04x: write %02x to unknown 053244 address %x\n",cpu_get_pc(),data,offset);
        }};

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
public static void K053245_sprites_draw(osd_bitmap bitmap,int min_priority,int max_priority)
{
   int NUM_SPRITES= 128;
	int offs,pri_code;
	int[] sortedlist=new int[NUM_SPRITES];

	for (offs = 0;offs < NUM_SPRITES;offs++)
		sortedlist[offs] = -1;

	/* prebuild a sorted table */
	for (offs = 0;offs < 0x800;offs += 16)
	{
		if ((K053245_ram.READ_WORD(offs) & 0x8000)!=0)
			sortedlist[K053245_ram.READ_WORD(offs) & 0x007f] = offs;
	}

	for (pri_code = 0;pri_code < NUM_SPRITES;pri_code++)
	{
		int ox,oy,size,w,h,x,y,flipx,flipy,mirrorx,mirrory,zoomx,zoomy;
                int[] color = new int[1];
                int[] code = new int[1];
                int[] pri = new int[1];

		offs = sortedlist[pri_code];
		if (offs == -1) continue;

		/* the following changes the sprite draw order from
			 0  1  4  5 16 17 20 21
			 2  3  6  7 18 19 22 23
			 8  9 12 13 24 25 28 29
			10 11 14 15 26 27 30 31
			32 33 36 37 48 49 52 53
			34 35 38 39 50 51 54 55
			40 41 44 45 56 57 60 61
			42 43 46 47 58 59 62 63

			to

			 0  1  2  3  4  5  6  7
			 8  9 10 11 12 13 14 15
			16 17 18 19 20 21 22 23
			24 25 26 27 28 29 30 31
			32 33 34 35 36 37 38 39
			40 41 42 43 44 45 46 47
			48 49 50 51 52 53 54 55
			56 57 58 59 60 61 62 63
		*/

		/* NOTE: from the schematics, it looks like the top 2 bits should be ignored */
		/* (there are not output pins for them), and probably taken from the "color" */
		/* field to do bank switching. However this applies only to TMNT2, with its */
		/* protection mcu creating the sprite table, so we don't know where to fetch */
		/* the bits from. */
		code[0] = K053245_ram.READ_WORD(offs+0x02);
		code[0] = ((code[0] & 0xffe1) + ((code[0] & 0x0010) >> 2) + ((code[0] & 0x0008) << 1)
				 + ((code[0] & 0x0004) >> 1) + ((code[0] & 0x0002) << 2));
		color[0] = K053245_ram.READ_WORD(offs+0x0c) & 0x00ff;
		pri[0] = 0;

		(K053245_callback).handler(code,color,pri);

		if (pri[0] < min_priority || pri[0] > max_priority) continue;

		size = (K053245_ram.READ_WORD(offs) & 0x0f00) >> 8;

		w = 1 << (size & 0x03);
		h = 1 << ((size >> 2) & 0x03);

		/* zoom control:
		   0x40 = normal scale
		  <0x40 enlarge (0x20 = double size)
		  >0x40 reduce (0x80 = half size)
		*/
		zoomy = K053245_ram.READ_WORD(offs+0x08);
		if (zoomy > 0x2000) continue;
		if (zoomy!=0) zoomy = (0x400000+zoomy/2) / zoomy;
		else zoomy = 2 * 0x400000;
		if ((K053245_ram.READ_WORD(offs) & 0x4000) == 0)
		{
			zoomx = K053245_ram.READ_WORD(offs+0x0a);
			if (zoomx > 0x2000) continue;
			if (zoomx!=0) zoomx = (0x400000+zoomx/2) / zoomx;
//			else zoomx = 2 * 0x400000;
else zoomx = zoomy; /* workaround for TMNT2 */
		}
		else zoomx = zoomy;

		ox = K053245_ram.READ_WORD(offs+0x06) + K053245_spriteoffsX;
		oy = K053245_ram.READ_WORD(offs+0x04);

		flipx = K053245_ram.READ_WORD(offs) & 0x1000;
		flipy = K053245_ram.READ_WORD(offs) & 0x2000;
		mirrorx = K053245_ram.READ_WORD(offs+0x0c) & 0x0100;
		mirrory = K053245_ram.READ_WORD(offs+0x0c) & 0x0200;

		if (K053245_flipscreenX!=0)
		{
			ox = 512 - ox;
			if (mirrorx==0) flipx = NOT(flipx);
		}
		if (K053245_flipscreenY!=0)
		{
			oy = -oy;
			if (mirrory==0) flipy = NOT(flipy);
		}

		ox = (ox + 0x5d) & 0x3ff;
		if (ox >= 768) ox -= 1024;
		oy = (-(oy + K053245_spriteoffsY + 0x07)) & 0x3ff;
		if (oy >= 640) oy -= 1024;

		/* the coordinates given are for the *center* of the sprite */
		ox -= (zoomx * w) >> 13;
		oy -= (zoomy * h) >> 13;

		for (y = 0;y < h;y++)
		{
			int sx,sy,zw,zh;

			sy = oy + ((zoomy * y + (1<<11)) >> 12);
			zh = (oy + ((zoomy * (y+1) + (1<<11)) >> 12)) - sy;

			for (x = 0;x < w;x++)
			{
				int c,fx,fy;

				sx = ox + ((zoomx * x + (1<<11)) >> 12);
				zw = (ox + ((zoomx * (x+1) + (1<<11)) >> 12)) - sx;
				c = code[0];
				if (mirrorx!=0)
				{
					if ((flipx == 0) ^ (2*x < w))
					{
						/* mirror left/right */
						c += (w-x-1);
						fx = 1;
					}
					else
					{
						c += x;
						fx = 0;
					}
				}
				else
				{
					if (flipx!=0) c += w-1-x;
					else c += x;
					fx = flipx;
				}
				if (mirrory!=0)
				{
					if ((flipy == 0) ^ (2*y >= h))
					{
						/* mirror top/bottom */
						c += 8*(h-y-1);
						fy = 1;
					}
					else
					{
						c += 8*y;
						fy = 0;
					}
				}
				else
				{
					if (flipy!=0) c += 8*(h-1-y);
					else c += 8*y;
					fy = flipy;
				}

				/* the sprite can start at any point in the 8x8 grid, but it must stay */
				/* in a 64 entries window, wrapping around at the edges. The animation */
				/* at the end of the saloon level in SUnset Riders breaks otherwise. */
				c = (c & 0x3f) | (code[0] & ~0x3f);

				if (zoomx == 0x10000 && zoomy == 0x10000)
				{
					/* hack to simulate shadow */
					if ((K053245_ram.READ_WORD(offs+0x0c) & 0x0080)!=0)
					{
						int o = K053245_gfx.colortable.read(16*color[0]+15);
						K053245_gfx.colortable.write(16*color[0]+15,palette_transparent_pen);
						drawgfx(bitmap,K053245_gfx,c,color[0],
								fx,fy,
								sx,sy,
								Machine.drv.visible_area,TRANSPARENCY_PENS,(cpu_getcurrentframe() & 1)!=0 ? 0x8001 : 0x0001);
						K053245_gfx.colortable.write(16*color[0]+15,o);
					}
					else
                                        {
						drawgfx(bitmap,K053245_gfx,c,color[0],
								fx,fy,
								sx,sy,
								Machine.drv.visible_area,TRANSPARENCY_PEN,0);
                                        }
				}
				else
                                {
					drawgfxzoom(bitmap,K053245_gfx,c,color[0],
							fx,fy,
							sx,sy,
							Machine.drv.visible_area,TRANSPARENCY_PEN,0,
							(zw << 16) / 16,(zh << 16) / 16);
                                }
			}
		}
	}
}

public static void K053245_mark_sprites_colors()
{
	int offs,i;

    char[] palette_map=new char[512];

                memset (palette_map, 0, sizeof (palette_map));

	/* sprites */
	for (offs = 0x800-16;offs >= 0;offs -= 16)
	{
		if ((K053245_ram.READ_WORD(offs) & 0x8000)!=0)
		{
			int[] code=new int[1];
                        int[] color=new int[1];
                        int[] pri=new int[1];

			code[0] = K053245_ram.READ_WORD(offs+0x02);
			code[0] = ((code[0] & 0xffe1) + ((code[0] & 0x0010) >> 2) + ((code[0] & 0x0008) << 1)
					 + ((code[0] & 0x0004) >> 1) + ((code[0] & 0x0002) << 2));
			color[0] = K053245_ram.READ_WORD(offs+0x0c) & 0x00ff;
			pri[0] = 0;
			(K053245_callback).handler(code,color,pri);
			palette_map[color[0]] |= 0xffff;
		}
	}

	/* now build the final table */
	for (i = 0; i < 512; i++)
	{
		int usage = palette_map[i], j;
		if (usage!=0)
		{
			for (j = 1; j < 16; j++)
				if((usage & (1 << j))!=0)
					palette_used_colors.write(i * 16 + j,palette_used_colors.read(i * 16 + j) | PALETTE_COLOR_VISIBLE);
		}
	}
}

        //public static int MAX_K051316= 3;

        public static int[] K051316_memory_region=new int[3];
        static int[] K051316_gfxnum=new int[3];
        static int[] K051316_wraparound=new int[3];
        static int[][] K051316_offset=new int[3][];
        static int[] K051316_bpp=new int[3];
        static K051316_callbackProcPtr[] K051316_callback=new K051316_callbackProcPtr[3];//static void (*K051316_callback[MAX_K051316])(int *code,int *color);
        static UBytePtr[] K051316_ram=new UBytePtr[3];
        static char[][] K051316_ctrlram=new char[3][];
        static tilemap[] K051316_tilemap=new tilemap[3];
        static int K051316_chip_selected;
        
        static
        {
             for (int i = 0; i < 3; i++)
                K051316_offset[i] = new int[2];
            for (int i = 0; i < 3; i++)
                K051316_ctrlram[i] = new char[16];

        }

        /***************************************************************************

          Callbacks for the TileMap code

        ***************************************************************************/

        public static void K051316_preupdate(int chip)
        {
                K051316_chip_selected = chip;
        }

        public static WriteHandlerPtr K051316_get_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
                int tile_index = 32*row+col;
                int[] code = new int[1];
                int[] color= new int[1];
                code[0] = K051316_ram[K051316_chip_selected].read(tile_index);
                color[0] = K051316_ram[K051316_chip_selected].read(tile_index + 0x400);

                K051316_callback[K051316_chip_selected].handler(code, color);//(*K051316_callback[K051316_chip_selected])(&code,&color);

                SET_TILE_INFO(K051316_gfxnum[K051316_chip_selected],code[0],color[0]);
        }};


        public static int K051316_vh_start(int chip, int gfx_memory_region,int bpp,K051316_callbackProcPtr callback)
        {
                int gfx_index;


                /* find first empty slot to decode gfx */
                for (gfx_index = 0; gfx_index < MAX_GFX_ELEMENTS; gfx_index++)
                        if (Machine.gfx[gfx_index] == null)
                                break;
                if (gfx_index == MAX_GFX_ELEMENTS)
                        return 1;

                if (bpp == 4)
                {
                        GfxLayout charlayout = new GfxLayout
                        (
                                16,16,
                                0,				/* filled in later */
                                4,
                                new int[]{ 0, 1, 2, 3 },
                                new int[]{ 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4,
                                                8*4, 9*4, 10*4, 11*4, 12*4, 13*4, 14*4, 15*4 },
                                new int[]{ 0*64, 1*64, 2*64, 3*64, 4*64, 5*64, 6*64, 7*64,
                                                8*64, 9*64, 10*64, 11*64, 12*64, 13*64, 14*64, 15*64 },
                                128*8
                        );


                        /* tweak the structure for the number of tiles we have */
                        charlayout.total = memory_region_length(gfx_memory_region) / 128;

                        /* decode the graphics */
                        Machine.gfx[gfx_index] = decodegfx(memory_region(gfx_memory_region),charlayout);
                }
                else if (bpp == 7)
                {
                        GfxLayout charlayout = new GfxLayout
                        (
                                16,16,
                                0,				/* filled in later */
                                7,
                                new int[]{ 1, 2, 3, 4, 5, 6, 7 },
                                new int[]{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
                                                8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
                                new int[]{ 0*128, 1*128, 2*128, 3*128, 4*128, 5*128, 6*128, 7*128,
                                                8*128, 9*128, 10*128, 11*128, 12*128, 13*128, 14*128, 15*128 },
                                256*8
                        );


                        /* tweak the structure for the number of tiles we have */
                        charlayout.total = memory_region_length(gfx_memory_region) / 256;

                        /* decode the graphics */
                        Machine.gfx[gfx_index] = decodegfx(memory_region(gfx_memory_region),charlayout);
                }
                else
                {
                        if (errorlog!=null) fprintf(errorlog,"K051316_vh_start supports only 4 or 7 bpp\n");
                        return 1;
                }

                if (Machine.gfx[gfx_index]==null)
                        return 1;

                /* set the color information */
                Machine.gfx[gfx_index].colortable = new CharPtr(Machine.remapped_colortable);
                Machine.gfx[gfx_index].total_colors = Machine.drv.color_table_len / (1 << bpp);

                K051316_memory_region[chip] = gfx_memory_region;
                K051316_gfxnum[chip] = gfx_index;
                K051316_bpp[chip] = bpp;
                K051316_callback[chip] = callback;

                K051316_tilemap[chip] = tilemap_create(K051316_get_tile_info,TILEMAP_OPAQUE,16,16,32,32);

                K051316_ram[chip] = new UBytePtr(0x800);

                if (K051316_ram[chip]==null || K051316_tilemap[chip]==null)
                {
                        K051316_vh_stop(chip);
                        return 1;
                }

                tilemap_set_clip(K051316_tilemap[chip],null);

                K051316_wraparound[chip] = 0;	/* default = no wraparound */
                K051316_offset[chip][0] = K051316_offset[chip][1] = 0;

                return 0;
        }

        public static int K051316_vh_start_0(int gfx_memory_region,int bpp,
                        K051316_callbackProcPtr callback)
        {
                return K051316_vh_start(0,gfx_memory_region,bpp,callback);
        }

        public static int K051316_vh_start_1(int gfx_memory_region,int bpp,
                        K051316_callbackProcPtr callback)
        {
                return K051316_vh_start(1,gfx_memory_region,bpp,callback);
        }

        public static int K051316_vh_start_2(int gfx_memory_region,int bpp,
                        K051316_callbackProcPtr callback)
        {
                return K051316_vh_start(2,gfx_memory_region,bpp,callback);
        }


        public static void K051316_vh_stop(int chip)
        {
                K051316_ram[chip]=null;
        }

        public static void K051316_vh_stop_0()
        {
                K051316_vh_stop(0);
        }

        public static void K051316_vh_stop_1()
        {
                K051316_vh_stop(1);
        }

        public static void K051316_vh_stop_2()
        {
                K051316_vh_stop(2);
        }

	public static int K051316_r(int chip, int offset)
	{
		return K051316_ram[chip].read(offset);
	}
	
	public static ReadHandlerPtr K051316_0_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K051316_r(0, offset);
	} };
	
	public static ReadHandlerPtr K051316_1_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K051316_r(1, offset);
	} };
	
	public static ReadHandlerPtr K051316_2_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K051316_r(2, offset);
	} };
	public static void K051316_w(int chip,int offset,int data)
	{
		if (K051316_ram[chip].read(offset) != data)
		{
			K051316_ram[chip].write(offset,data);
			tilemap_mark_tile_dirty(K051316_tilemap[chip],offset%32,(offset%0x400)/32);
		}
	}
	
	public static WriteHandlerPtr K051316_0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		K051316_w(0,offset,data);
	} };
	
	public static WriteHandlerPtr K051316_1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		K051316_w(1,offset,data);
	} };
	
	public static WriteHandlerPtr K051316_2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		K051316_w(2,offset,data);
	} };

        public static int K051316_rom_r(int chip, int offset)
        {
                if ((K051316_ctrlram[chip][0x0e] & 0x01) == 0)
                {
                        int addr;

                        addr = offset + (K051316_ctrlram[chip][0x0c] << 11) + (K051316_ctrlram[chip][0x0d] << 19);
                        if (K051316_bpp[chip] <= 4) addr /= 2;
                        addr &= memory_region_length(K051316_memory_region[chip])-1;

       /* #if 0
                usrintf_showmessage("%04x: offset %04x addr %04x",cpu_get_pc(),offset,addr);
        #endif*/

                        return memory_region(K051316_memory_region[chip]).read(addr);
                }
                else
                {
                    if (errorlog!=null) fprintf(errorlog,"%04x: read 051316 ROM offset %04x but reg 0x0c bit 0 not clear\n",cpu_get_pc(),offset);
                        return 0;
                }
        }

	public static ReadHandlerPtr K051316_rom_0_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K051316_rom_r(0,offset);
	} };
	
	public static ReadHandlerPtr K051316_rom_1_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K051316_rom_r(1,offset);
	} };
	
	public static ReadHandlerPtr K051316_rom_2_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K051316_rom_r(2,offset);
	} };



        public static void K051316_ctrl_w(int chip,int offset,int data)
        {
                K051316_ctrlram[chip][offset] = (char)(data &0xFF);
                if (errorlog!=null && offset >= 0x0c) fprintf(errorlog,"%04x: write %02x to 051316 reg %x\n",cpu_get_pc(),data,offset);
        }

	public static WriteHandlerPtr K051316_ctrl_0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		K051316_ctrl_w(0,offset,data);
	} };
	
	public static WriteHandlerPtr K051316_ctrl_1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		K051316_ctrl_w(1,offset,data);
	} };
	
	public static WriteHandlerPtr K051316_ctrl_2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		K051316_ctrl_w(2,offset,data);
	} };

        public static void K051316_wraparound_enable(int chip, int status)
        {
                K051316_wraparound[chip] = status;
        }

        public static void K051316_set_offset(int chip, int xoffs, int yoffs)
        {
                K051316_offset[chip][0] = xoffs;
                K051316_offset[chip][1] = yoffs;
        }

        public static void K051316_tilemap_update(int chip)
        {
                K051316_preupdate(chip);
                tilemap_update(K051316_tilemap[chip]);
        }

        public static void K051316_tilemap_update_0()
        {
                K051316_tilemap_update(0);
        }

        public static void K051316_tilemap_update_1()
        {
                K051316_tilemap_update(1);
        }

        public static void K051316_tilemap_update_2()
        {
                K051316_tilemap_update(2);
        }


/* Note: rotation support doesn't handle asymmetrical visible areas. This doesn't */
/* matter because in the Konami games the visible area is always symmetrical. */
public static void K051316_zoom_draw(int chip,osd_bitmap bitmap)
{
	/*UINT32*/int startx,starty,cx,cy;
	int incxx,incxy,incyx,incyy;
	int x,sx,sy,ex,ey;
	osd_bitmap srcbitmap = K051316_tilemap[chip].pixmap;

	startx = 256 * ((char)(256 * K051316_ctrlram[chip][0x00] + K051316_ctrlram[chip][0x01]));
	incxx  =        (char)(256 * K051316_ctrlram[chip][0x02] + K051316_ctrlram[chip][0x03]);
	incyx  =        (char)(256 * K051316_ctrlram[chip][0x04] + K051316_ctrlram[chip][0x05]);
	starty = 256 * ((char)(256 * K051316_ctrlram[chip][0x06] + K051316_ctrlram[chip][0x07]));
	incxy  =        (char)(256 * K051316_ctrlram[chip][0x08] + K051316_ctrlram[chip][0x09]);
	incyy  =        (char)(256 * K051316_ctrlram[chip][0x0a] + K051316_ctrlram[chip][0x0b]);

	startx += (Machine.drv.visible_area.min_y - (16 + K051316_offset[chip][1])) * incyx;
	starty += (Machine.drv.visible_area.min_y - (16 + K051316_offset[chip][1])) * incyy;

	startx += (Machine.drv.visible_area.min_x - (89 + K051316_offset[chip][0])) * incxx;
	starty += (Machine.drv.visible_area.min_x - (89 + K051316_offset[chip][0])) * incxy;

	sx = Machine.drv.visible_area.min_x;
	sy = Machine.drv.visible_area.min_y;
	ex = Machine.drv.visible_area.max_x;
	ey = Machine.drv.visible_area.max_y;

	if ((Machine.orientation & ORIENTATION_SWAP_XY)!=0)
	{
		int t;

		t = startx; startx = starty; starty = t;
		t = sx; sx = sy; sy = t;
		t = ex; ex = ey; ey = t;
		t = incxx; incxx = incyy; incyy = t;
		t = incxy; incxy = incyx; incyx = t;
	}

	if ((Machine.orientation & ORIENTATION_FLIP_X)!=0)
	{
		int w = ex - sx;

		incxy = -incxy;
		incyx = -incyx;
		startx = 0xfffff - startx;
		startx -= incxx * w;
		starty -= incxy * w;
	}

	if ((Machine.orientation & ORIENTATION_FLIP_Y)!=0)
	{
		int h = ey - sy;

		incxy = -incxy;
		incyx = -incyx;
		starty = 0xfffff - starty;
		startx -= incyx * h;
		starty -= incyy * h;
	}

	if (bitmap.depth == 8)
	{
		UBytePtr dest;

		if (incxy == 0 && incyx == 0 && K051316_wraparound[chip]==0)
		{
			/* optimized loop for the not rotated case */

			if (incxx == 0x800)
			{
				/* optimized loop for the not zoomed case */

				/* startx is unsigned */
				startx = ((int)startx) >> 11;

				if (startx >= 512)
				{
					sx += -startx;
					startx = 0;
				}

				if (sx <= ex)
				{
					while (sy <= ey)
					{
						if ((starty & 0xfff00000) == 0)
						{
							x = sx;
							cx = startx;
							cy = starty >> 11;
							dest = new UBytePtr(bitmap.line[sy],sx);
							while (x <= ex && cx < 512)
							{
								int c = srcbitmap.line[cy].read(cx);

								if (c != palette_transparent_pen)
									dest.write(0, c);//*dest = c; ????

								cx++;
								x++;
								dest.offset++;
							}
						}
						starty += incyy;
						sy++;
					}
				}
			}
			else
			{
				while ((startx & 0xfff00000) != 0 && sx <= ex)
				{
					startx += incxx;
					sx++;
				}

				if ((startx & 0xfff00000) == 0)
				{
					while (sy <= ey)
					{
						if ((starty & 0xfff00000) == 0)
						{
							x = sx;
							cx = startx;
							cy = starty >> 11;
							dest = new UBytePtr(bitmap.line[sy],sx);
							while (x <= ex && (cx & 0xfff00000) == 0)
							{
								int c = srcbitmap.line[cy].read(cx >> 11);

								if (c != palette_transparent_pen)
									dest.write(0, c);//*dest = c;

								cx += incxx;
								x++;
								dest.offset++;
							}
						}
						starty += incyy;
						sy++;
					}
				}
			}
		}
		else
		{
			if (K051316_wraparound[chip]!=0)
			{
				/* plot with wraparound */
				while (sy <= ey)
				{
					x = sx;
					cx = startx;
					cy = starty;
					dest = new UBytePtr(bitmap.line[sy],sx);
					while (x <= ex)
					{
						int c = srcbitmap.line[(cy >> 11) & 0x1ff].read((cx >> 11) & 0x1ff);

						if (c != palette_transparent_pen)
							dest.write(0, c);//*dest = c;

						cx += incxx;
						cy += incxy;
						x++;
						dest.offset++;
					}
					startx += incyx;
					starty += incyy;
					sy++;
				}
			}
			else
			{
				while (sy <= ey)
				{
					x = sx;
					cx = startx;
					cy = starty;
					dest = new UBytePtr(bitmap.line[sy],sx);
					while (x <= ex)
					{
						if ((cx & 0xfff00000) == 0 && (cy & 0xfff00000) == 0)
						{
							int c = srcbitmap.line[cy >> 11].read(cx >> 11);

							if (c != palette_transparent_pen)
								dest.write(0,c);//*dest = c;
						}

						cx += incxx;
						cy += incxy;
						x++;
						dest.offset++;
					}
					startx += incyx;
					starty += incyy;
					sy++;
				}
			}
		}
	}
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
}

        public static void K051316_zoom_draw_0(osd_bitmap bitmap)
        {
                K051316_zoom_draw(0, bitmap);
        }

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


        static /*unsigned*/ char[] K053251_ram=new char[16];
        static int[] K053251_palette_index=new int[5];

        public static WriteHandlerPtr K053251_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
                data &= 0x3f;

                if (K053251_ram[offset] != data)
                {
                        K053251_ram[offset] = (char)(data&0xFF);
                        if (offset == 9)
                        {
                                /* palette base index */
                                K053251_palette_index[0] = 32 * ((data >> 0) & 0x03);
                                K053251_palette_index[1] = 32 * ((data >> 2) & 0x03);
                                K053251_palette_index[2] = 32 * ((data >> 4) & 0x03);
                                tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
                        }
                        else if (offset == 10)
                        {
                                /* palette base index */
                                K053251_palette_index[3] = 16 * ((data >> 0) & 0x07);
                                K053251_palette_index[4] = 16 * ((data >> 3) & 0x07);
                                tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
                        }
        /*#if 0
        else
        {
        if (errorlog)
        fprintf(errorlog,"%04x: write %02x to K053251 register %04x\n",cpu_get_pc(),data&0xff,offset);
        usrintf_showmessage("pri = %02x%02x%02x%02x %02x%02x%02x%02x %02x%02x%02x%02x %02x%02x%02x%02x",
                K053251_ram[0],K053251_ram[1],K053251_ram[2],K053251_ram[3],
                K053251_ram[4],K053251_ram[5],K053251_ram[6],K053251_ram[7],
                K053251_ram[8],K053251_ram[9],K053251_ram[10],K053251_ram[11],
                K053251_ram[12],K053251_ram[13],K053251_ram[14],K053251_ram[15]
                );
        }
        #endif*/
                }
        }};

        static int K053251_get_priority(int ci)
        {
                return K053251_ram[ci];
        }

        static int K053251_get_palette_index(int ci)
        {
                return K053251_palette_index[ci];
        }


        static /*unsigned*/ char[] K051733_ram=new char[0x20];

        public static WriteHandlerPtr K051733_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{

            if (errorlog != null) fprintf(errorlog,"%04x: write %02x to 051733 address %02x\n",cpu_get_pc(),data,offset);	
		K051733_ram[offset] = (char)(data&0xFF);
	} };
        public static ReadHandlerPtr K051733_r = new ReadHandlerPtr() { public int handler(int offset)
	{
            int op1 = (K051733_ram[0x00] << 8) | K051733_ram[0x01];
            int op2 = (K051733_ram[0x02] << 8) | K051733_ram[0x03];

            int rad = (K051733_ram[0x06] << 8) | K051733_ram[0x07];
            int yobj1c = (K051733_ram[0x08] << 8) | K051733_ram[0x09];
            int xobj1c = (K051733_ram[0x0a] << 8) | K051733_ram[0x0b];
            int yobj2c = (K051733_ram[0x0c] << 8) | K051733_ram[0x0d];
            int xobj2c = (K051733_ram[0x0e] << 8) | K051733_ram[0x0f];

        //#if VERBOSE
        //if (errorlog) fprintf(errorlog,"%04x: read 051733 address %02x\n",cpu_get_pc(),offset);
        //#endif

            switch(offset){
                    case 0x00:
                            if (op2!=0) return	((op1/op2) >> 8);
                            else return 0xff;
                    case 0x01:
                            if (op2!=0) return	op1/op2;
                            else return 0xff;

                    /* this is completely unverified */
                    case 0x02:
                            if (op2!=0) return	((op1%op2) >> 8);
                            else return 0xff;
                    case 0x03:
                            if (op2!=0) return	op1%op2;
                            else return 0xff;

                    case 0x07:{
                            if (xobj1c + rad < xobj2c - rad)
                                    return 0x80;

                            if (xobj2c + rad < xobj1c - rad)
                                    return 0x80;

                            if (yobj1c + rad < yobj2c - rad)
                                    return 0x80;

                            if (yobj2c + rad < yobj1c - rad)
                                    return 0x80;

                            return 0;
                    }
                    default:
                            return K051733_ram[offset];
            }
        }};

    }