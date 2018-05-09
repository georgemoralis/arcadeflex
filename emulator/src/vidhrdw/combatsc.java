/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 *
 *
 *
 */ 
package vidhrdw;
import static arcadeflex.ptrlib.*;
import static arcadeflex.libc.*;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static vidhrdw.generic.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.mame.*;
import static arcadeflex.libc_old.*;
import static mame.tilemapC.*;
import static mame.tilemapH.*;
import static mame.memoryH.*;
import static vidhrdw.konamiic.*;
import static mame.sndintrf.*;
import static mame.cpuintrf.*;
import static mame.inputport.*;
import static mame.commonH.*;
import static mame.palette.*;
import static mame.common.*;
import static mame.memory.*;




public class combatsc
{
	
	static tilemap[] tilemap=new tilemap[2];
	static tilemap textlayer;
        static UBytePtr[] private_spriteram=new UBytePtr[2];
	static int priority;
	
	public static UBytePtr combasc_io_ram=new UBytePtr();
	static int combasc_vreg;
	public static UBytePtr banked_area=new UBytePtr();
	
	static int combasc_bank_select; /* 0x00..0x1f */
	static int combasc_video_circuit; /* 0 or 1 */
	static UBytePtr[] combasc_page=new UBytePtr[2];
	//static unsigned char combasc_scrollram0[0x40];
        static UBytePtr combasc_scrollram0=new UBytePtr(0x40);
	//static unsigned char combasc_scrollram1[0x40];
        static UBytePtr combasc_scrollram1=new UBytePtr(0x40);
	static UBytePtr combasc_scrollram=new UBytePtr();
	

	public static VhConvertColorPromPtr combasc_convert_color_prom = new VhConvertColorPromPtr() { public void handler(UByte []palette, char []colortable, UBytePtr color_prom) 
	{
		int i,pal,clut = 0;
                int ptr=0;
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
					if (color_prom.read(256 * clut + i) == 0)
                                        {
						//*(colortable++) = 0;
                                                colortable[ptr++]=0;
                                        }
					else
                                        {
						colortable[ptr++] = (char)(16 * pal + color_prom.read(256 * clut + i));
                                        }
				}
				else	/* chars */
					colortable[ptr++] = (char)(16 * pal + color_prom.read(256 * clut + i));
			}
		}
	}};
	public static VhConvertColorPromPtr combascb_convert_color_prom = new VhConvertColorPromPtr() { public void handler(UByte []palette, char []colortable, UBytePtr color_prom) 
	{
		int i,pal;
                int ptr=0;
		for( pal=0; pal<8; pal++ )
		{
			for( i=0; i<256; i++ )
			{
				if ((pal & 1) == 0)	/* sprites */
					colortable[ptr++] = (char)(16 * pal + (color_prom.read(i) ^ 0x0f));
				else	/* chars */
					colortable[ptr++] = (char)(16 * pal + (i & 0x0f));	/* no lookup? */
			}
		}
	}};
	
	/***************************************************************************
	
		Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static WriteHandlerPtr get_tile_info0 = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int tile_index = row*32 + col;
		char attributes = combasc_page[0].read(tile_index);
		int bank = 4*((combasc_vreg & 0x0f) - 1);
		int number,color;
	
		if (bank < 0) bank = 0;
		if ((attributes & 0xb0) == 0) bank = 0;	/* text bank */
	
		if ((attributes & 0x80) != 0) bank += 1;
		if ((attributes & 0x10) != 0) bank += 2;
		if ((attributes & 0x20) != 0) bank += 4;
	
		color = ((K007121_ctrlram[0][6]&0x10)*2+16) + (attributes & 0x0f);
	
		number = combasc_page[0].read(tile_index + 0x400) + 256*bank;
	
		SET_TILE_INFO(0,number,color);
		tile_info.priority = (char)((attributes & 0x40) >> 6);
	} };
	
	public static WriteHandlerPtr get_tile_info1 = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int tile_index = row*32 + col;
		char attributes = combasc_page[1].read(tile_index);
		int bank = 4*((combasc_vreg >> 4) - 1);
		int number, color;
	
		if (bank < 0) bank = 0;
		if ((attributes & 0xb0) == 0) bank = 0;	/* text bank */
	
		if ((attributes & 0x80) != 0) bank += 1;
		if ((attributes & 0x10) != 0) bank += 2;
		if ((attributes & 0x20) != 0) bank += 4;
	
		color = ((K007121_ctrlram[1][6]&0x10)*2+16+4*16) + (attributes & 0x0f);
	
		number = combasc_page[1].read(tile_index + 0x400) + 256*bank;
	
		SET_TILE_INFO(1,number,color);
		tile_info.priority = (char)((attributes & 0x40) >> 6);
	} };
	
	public static WriteHandlerPtr get_text_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int tile_index = row*32 + col + 0x800;
		char attributes = combasc_page[0].read(tile_index);
		int number = combasc_page[0].read(tile_index + 0x400);
		int color = 16 + (attributes & 0x0f);
	
		SET_TILE_INFO(0,number,color);
	
		/* the following hack is needed because the TileMap system doesn't support TRANSPARENCY_COLOR */
		tile_info.flags = 0;
		if ((attributes & 0x0f) == 0x01 || (attributes & 0x0f) == 0x0e)
			tile_info.flags = TILE_IGNORE_TRANSPARENCY;
	} };
	
	public static WriteHandlerPtr get_tile_info0_bootleg = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int tile_index = row*32 + col;
		char attributes = combasc_page[0].read(tile_index);
		int bank = 4*((combasc_vreg & 0x0f) - 1);
		int number, pal, color;
	
		if (bank < 0) bank = 0;
		if ((attributes & 0xb0) == 0) bank = 0;	/* text bank */
	
		if ((attributes & 0x80) != 0) bank += 1;
		if ((attributes & 0x10) != 0) bank += 2;
		if ((attributes & 0x20) != 0) bank += 4;
	
		pal = (bank == 0 || bank >= 0x1c || (attributes & 0x40)!=0) ? 1 : 3;
		color = pal*16;// + (attributes & 0x0f);
		number = combasc_page[0].read(tile_index + 0x400) + 256*bank;
	
		SET_TILE_INFO(0,number,color);
	} };
	
	public static WriteHandlerPtr get_tile_info1_bootleg = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int tile_index = row*32 + col;
		char attributes = combasc_page[1].read(tile_index);
		int bank = 4*((combasc_vreg >> 4) - 1);
		int number, pal, color;
	
		if (bank < 0) bank = 0;
		if ((attributes & 0xb0) == 0) bank = 0;	/* text bank */
	
		if ((attributes & 0x80) != 0) bank += 1;
		if ((attributes & 0x10) != 0) bank += 2;
		if ((attributes & 0x20) != 0) bank += 4;
	
		pal = (bank == 0 || bank >= 0x1c || (attributes & 0x40)!=0) ? 5 : 7;
		color = pal*16;// + (attributes & 0x0f);
		number = combasc_page[1].read(tile_index + 0x400) + 256*bank;
	
		SET_TILE_INFO(1,number,color);
	} };
	
	public static WriteHandlerPtr get_text_info_bootleg = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int tile_index = row*32 + col + 0x800;
	//	unsigned char attributes = combasc_page[0][tile_index];
		int number = combasc_page[0].read(tile_index + 0x400);
		int color = 16;// + (attributes & 0x0f);
	
		SET_TILE_INFO(1, number, color);
	} };
	
	/***************************************************************************
	
		Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr combasc_vh_start = new VhStartPtr() { public int handler() 
	{
		combasc_vreg = -1;
	
		tilemap[0] = tilemap_create(get_tile_info0,TILEMAP_TRANSPARENT,8,8,32,32);
		tilemap[1] = tilemap_create(get_tile_info1,TILEMAP_TRANSPARENT,8,8,32,32);
		textlayer =  tilemap_create(get_text_info, TILEMAP_TRANSPARENT,8,8,32,32);
	
		private_spriteram[0] = new UBytePtr(0x800);
		private_spriteram[1] = new UBytePtr(0x800);
                for(int i=0; i<0x800; i++)
                {
                    private_spriteram[0].write(i,0);
                    private_spriteram[1].write(i,0);
                }
		//memset(private_spriteram[0],0,0x800);
		//memset(private_spriteram[1],0,0x800);
	
		if (tilemap[0]!=null && tilemap[1]!=null && textlayer!=null)
		{
			tilemap[0].transparent_pen = 0;
			tilemap[1].transparent_pen = 0;
			textlayer.transparent_pen = 0;
	
			tilemap_set_scroll_rows(textlayer,32);
	
			return 0;
		}
	
		return 1;
	} };
	
	public static VhStartPtr combascb_vh_start = new VhStartPtr() { public int handler() 
	{
		combasc_vreg = -1;
	
		tilemap[0] =  tilemap_create(get_tile_info0_bootleg,TILEMAP_TRANSPARENT,8,8,32,32);
		tilemap[1] =  tilemap_create(get_tile_info1_bootleg,TILEMAP_TRANSPARENT,8,8,32,32);
		textlayer = tilemap_create(get_text_info_bootleg, TILEMAP_TRANSPARENT,8,8,32,32);
	
		private_spriteram[0] = new UBytePtr(0x800);
		private_spriteram[1] = new UBytePtr(0x800);
                for(int i=0; i<0x800; i++)
                {
                    private_spriteram[0].write(i,0);
                    private_spriteram[1].write(i,0);
                }
		//memset(private_spriteram[0],0,0x800);
		//memset(private_spriteram[1],0,0x800);
	
		if (tilemap[0]!=null && tilemap[1]!=null && textlayer!=null)
		{
			tilemap[0].transparent_pen = 0;
			tilemap[1].transparent_pen = 0;
			textlayer.transparent_pen = 0;
	
			tilemap_set_scroll_rows(tilemap[0],32);
			tilemap_set_scroll_rows(tilemap[1],32);
	
			return 0;
		}
	
		return 1;
	} };
	
	public static VhStopPtr combasc_vh_stop = new VhStopPtr() { public void handler() 
	{
            private_spriteram[0]=null;
            private_spriteram[1]=null;
	} };
	
	/***************************************************************************
	
		Memory handlers
	
	***************************************************************************/
	
	public static ReadHandlerPtr combasc_video_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return videoram.read(offset);
	} };
	
	public static WriteHandlerPtr combasc_video_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if( videoram.read(offset)!=data )
		{
			videoram.write(offset,data);
			if( offset<0x800 )
			{
				offset = offset&0x3ff;
				if (combasc_video_circuit != 0)
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
	} };
	
	
	public static WriteHandlerPtr combasc_vreg_w = new WriteHandlerPtr() { public void handler(int offset, int data)
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
	} };
	
	public static WriteHandlerPtr combascb_sh_irqtrigger_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		soundlatch_w.handler(offset,data);
		cpu_cause_interrupt(1,0xff);
	} };
	
	public static ReadHandlerPtr combasc_io_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if ((offset <= 0x403) && (offset >= 0x400))
		{
			switch (offset)
			{
				case 0x400:	return input_port_0_r.handler(0); //break;
				case 0x401:	return input_port_1_r.handler(0); //break;
				case 0x402:	return input_port_2_r.handler(0); //break;
				case 0x403:	return input_port_3_r.handler(0); //break;
			}
		}
		return banked_area.read(offset);
	} };
	
	public static WriteHandlerPtr combasc_io_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		switch (offset)
		{
			case 0x400: priority = data & 0x20; break;
			case 0x800: combascb_sh_irqtrigger_w.handler(0, data); break;
			case 0xc00:	combasc_vreg_w.handler(0, data); break;
			default:
				combasc_io_ram.write(offset,data);
		}
	} };
	
	public static WriteHandlerPtr combasc_bankselect_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		UBytePtr page = new UBytePtr(memory_region(REGION_CPU1),0x10000);
	
		if ((data & 0x40) != 0)
		{
			combasc_video_circuit = 1;
			videoram = new UBytePtr(combasc_page[1]);
			combasc_scrollram = new UBytePtr(combasc_scrollram1);
		}
		else
		{
			combasc_video_circuit = 0;
			videoram = new UBytePtr(combasc_page[0]);
			combasc_scrollram = new UBytePtr(combasc_scrollram0);
		}
	
		priority = data & 0x20;
	
		if ((data & 0x10) != 0)
		{
			cpu_setbank(1,new UBytePtr(page,0x4000 * ((data & 0x0e) >> 1)));
		}
		else
		{
			cpu_setbank(1,new UBytePtr(page,0x20000 + 0x4000 * (data & 1)));
		}
	} };
	
	public static WriteHandlerPtr combascb_bankselect_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if ((data & 0x40) != 0)
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
			UBytePtr page = new UBytePtr(memory_region(REGION_CPU1),0x10000);
			combasc_bank_select = data;
	
			if ((data & 0x10) != 0)
			{
				cpu_setbank(1,new UBytePtr(page,0x4000 * ((data & 0x0e) >> 1)));
			}
			else
			{
				cpu_setbank(1,new UBytePtr(page,0x20000 + 0x4000 * (data & 1)));
			}
	
			if (data == 0x1f)
			{
                                cpu_setbank(1,new UBytePtr(page,0x20000 + 0x4000 * (data & 1)));

				cpu_setbankhandler_r (1, combasc_io_r);/* IO RAM & Video Registers */
				cpu_setbankhandler_w (1, combasc_io_w);
			}
			else
			{
				cpu_setbankhandler_r (1, mrh_bank1);	/* banked ROM */
				cpu_setbankhandler_w (1, mwh_rom);
			}
		}
	} };
	public static InitMachinePtr combasc_init_machine = new InitMachinePtr() { public void handler()
       {
		UBytePtr MEM = new UBytePtr(memory_region(REGION_CPU1), 0x38000);
	
	
		combasc_io_ram  = new UBytePtr(MEM,0x0000);
		combasc_page[0] = new UBytePtr(MEM,0x4000);
		combasc_page[1] = new UBytePtr(MEM,0x6000);
	
                for(int i=0; i<0x4000; i++)
                {
                    combasc_io_ram.write(i,0x00);
                }
                for(int i=0; i<0x2000; i++)
                {
                    combasc_page[0].write(i,0x00);
                }
                for(int i=0; i<0x2000; i++)
                {
                    combasc_page[1].write(i,0x00);
                }
		//memset( combasc_io_ram,  0x00, 0x4000 );
		//memset( combasc_page[0], 0x00, 0x2000 );
		//memset( combasc_page[1], 0x00, 0x2000 );
	
		combasc_bank_select = -1;
		combasc_bankselect_w.handler(0,0 );
	}};
	
	public static WriteHandlerPtr combasc_pf_control_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		K007121_ctrl_w(combasc_video_circuit,offset,data);
	
		if (offset == 7)
			tilemap_set_flip(tilemap[combasc_video_circuit],(data & 0x08)!=0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
	
		if (offset == 3)
		{
			if ((data & 0x08) != 0)
                        {
                            //System.arraycopy(combasc_page[combasc_video_circuit].memory, combasc_page[combasc_video_circuit].offset+0x1000, private_spriteram[combasc_video_circuit].memory, 0, 0x800);
				memcpy(private_spriteram[combasc_video_circuit],0,combasc_page[combasc_video_circuit],0x1000,0x800);
                        }
                        else
                        {
                            //System.arraycopy(combasc_page[combasc_video_circuit].memory, combasc_page[combasc_video_circuit].offset+0x1800, private_spriteram[combasc_video_circuit].memory,0, 0x800);
				memcpy(private_spriteram[combasc_video_circuit],0,combasc_page[combasc_video_circuit],0x1800,0x800);
		
                        }
                }
	} };
	
	public static ReadHandlerPtr combasc_scrollram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return combasc_scrollram.read(offset);
	} };
	
	public static WriteHandlerPtr combasc_scrollram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		combasc_scrollram.write(offset,data);
	} };
	
	
	
	/***************************************************************************
	
		Display Refresh
	
	***************************************************************************/
	
	static void draw_sprites(osd_bitmap bitmap, UBytePtr source, int circuit)
	{
		int base_color = (circuit*4)*16+(K007121_ctrlram[circuit][6]&0x10)*2;       
		K007121_sprites_draw(circuit,bitmap,new UBytePtr(source),base_color,0,0);
	}
	
	public static VhUpdatePtr combasc_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i;
	
	
		if ((K007121_ctrlram[0][0x01] & 0x02)!=0)
		{
			tilemap_set_scroll_rows(tilemap[0],32);
			for (i = 0;i < 32;i++)
			{
				tilemap_set_scrollx(tilemap[0],i,combasc_scrollram0.read(i));
			}
		}
		else
		{
			tilemap_set_scroll_rows(tilemap[0],1);
			tilemap_set_scrollx(tilemap[0],0,K007121_ctrlram[0][0x00] | ((K007121_ctrlram[0][0x01] & 0x01) << 8));
		}
	
		if ((K007121_ctrlram[1][0x01] & 0x02)!=0)
		{
			tilemap_set_scroll_rows(tilemap[1],32);
			for (i = 0;i < 32;i++)
			{
				tilemap_set_scrollx(tilemap[1],i,combasc_scrollram1.read(i));
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
		if (palette_recalc()!=null)
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
	
		if ((K007121_ctrlram[0][0x01] & 0x08)!=0)
		{
			for (i = 0;i < 32;i++)
			{
				tilemap_set_scrollx(textlayer,i,combasc_scrollram0.read(0x20+i)!=0 ? 0 : TILE_LINE_DISABLED);
				tilemap_draw(bitmap,textlayer,0);
			}
		}
	
		/* chop the extreme columns if necessary */
		if ((K007121_ctrlram[0][0x03] & 0x40)!=0)
		{
			rectangle clip;
	
			clip = new rectangle(Machine.drv.visible_area);
			clip.max_x = clip.min_x + 7;
			fillbitmap(bitmap,Machine.pens[0],clip);
	
			clip = new rectangle(Machine.drv.visible_area);
			clip.min_x = clip.max_x - 7;
			fillbitmap(bitmap,Machine.pens[0],clip);
		}
	}};
	
	
	
	
	
	
	
	
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
	
	static void bootleg_draw_sprites( osd_bitmap bitmap, UBytePtr source, int circuit )
	{
		GfxElement gfx = Machine.gfx[circuit+2];
		rectangle clip = Machine.drv.visible_area;
	
		UBytePtr RAM = memory_region(REGION_CPU1);
		int limit = ( circuit)!=0 ? (RAM.read(0xc2)*256 + RAM.read(0xc3)) : (RAM.read(0xc0)*256 + RAM.read(0xc1));
		int finish;//const unsigned char *finish;
	
		source.inc(0x1000);
		finish = source.offset;
		source.inc(0x400);
		limit = (0x3400-limit)/8;
		if( limit>=0 ) finish = source.offset-limit*8;
		source.dec(8);
	
		while( source.offset>finish )
		{
			/*unsigned*/ char attributes = source.read(3); /* PBxF ?xxX */
			{
				int number = source.read(0);
				int x = source.read(2) - 71 + (attributes & 0x01)*256;
				int y = 242 - source.read(1);
				/*unsigned*/ char color = source.read(4); /* CCCC xxBB */
	
				int bank = (color & 0x03) | ((attributes & 0x40) >> 4);
	
				number = ((number & 0x02) << 1) | ((number & 0x04) >> 1) | (number & (~6));
				number += 256*bank;
	
				color = (char)((circuit*4)*16 + (color >> 4));
	
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
			source.dec(8);
		}
	}
	public static VhUpdatePtr combascb_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i;
	
		for( i=0; i<32; i++ )
		{
			tilemap_set_scrollx( tilemap[0],i, combasc_io_ram.read(0x040+i)+5 );
			tilemap_set_scrollx( tilemap[1],i, combasc_io_ram.read(0x060+i)+3 );
		}
		tilemap_set_scrolly( tilemap[0],0, combasc_io_ram.read(0x000) );
		tilemap_set_scrolly( tilemap[1],0, combasc_io_ram.read(0x020) );
	
		tilemap_update( ALL_TILEMAPS );
		if (palette_recalc()!=null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
		tilemap_render( ALL_TILEMAPS );
	
		if (priority == 0)
		{
			tilemap_draw( bitmap,tilemap[1],TILEMAP_IGNORE_TRANSPARENCY );
			bootleg_draw_sprites( bitmap, new UBytePtr(combasc_page[0]), 0 );
			tilemap_draw( bitmap,tilemap[0],0 );
			bootleg_draw_sprites( bitmap, new UBytePtr(combasc_page[1]), 1 );
		}
		else
		{
			tilemap_draw( bitmap,tilemap[0],TILEMAP_IGNORE_TRANSPARENCY );
			bootleg_draw_sprites( bitmap, new UBytePtr(combasc_page[0]), 0 );
			tilemap_draw( bitmap,tilemap[1],0 );
			bootleg_draw_sprites( bitmap, new UBytePtr(combasc_page[1]), 1 );
		}
	
		tilemap_draw( bitmap,textlayer,0 );
	}};
}
