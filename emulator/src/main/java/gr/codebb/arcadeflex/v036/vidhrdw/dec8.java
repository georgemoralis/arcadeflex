/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
public class dec8
{
	
	public static char[] pf_video;
        public static char[] pf_dirty;
	static int[] scroll1=new int[4];
        static int[] scroll2=new int[4];
        static int[] pf1_attr=new int[8];
        static int[] pf2_attr=new int[8];
	static osd_bitmap pf1_bitmap,pf2_bitmap,tf2_bitmap;
	public static UBytePtr dec8_row=new UBytePtr();
        public static UBytePtr srdarwin_tileram=new UBytePtr();
	
	static int blank_tile,shackled_priority,flipscreen;
	
	/* Tilemap.. Only used by Super Real Darwin for now */
	static tilemap background_layer;
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Real Ghostbusters has two 1024x8 palette PROM.
	  I don't know the exact values of the resistors between the RAM and the
	  RGB output. I assumed these values (the same as Commando)
	
	  bit 7 -- 220 ohm resistor  -- GREEN
	        -- 470 ohm resistor  -- GREEN
	        -- 1  kohm resistor  -- GREEN
	        -- 2.2kohm resistor  -- GREEN
	        -- 220 ohm resistor  -- RED
	        -- 470 ohm resistor  -- RED
	        -- 1  kohm resistor  -- RED
	  bit 0 -- 2.2kohm resistor  -- RED
	
	  bit 7 -- unused
	        -- unused
	        -- unused
	        -- unused
	        -- 220 ohm resistor  -- BLUE
	        -- 470 ohm resistor  -- BLUE
	        -- 1  kohm resistor  -- BLUE
	  bit 0 -- 2.2kohm resistor  -- BLUE
	
	***************************************************************************/
	public static VhConvertColorPromPtr ghostb_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
                int p_inc=0;
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,bit3;
	
			bit0 = (color_prom.read(0) >> 0) & 0x01;
			bit1 = (color_prom.read(0) >> 1) & 0x01;
			bit2 = (color_prom.read(0) >> 2) & 0x01;
			bit3 = (color_prom.read(0) >> 3) & 0x01;
			palette[p_inc++]=(char)(0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
			bit0 = (color_prom.read(0) >> 4) & 0x01;
			bit1 = (color_prom.read(0) >> 5) & 0x01;
			bit2 = (color_prom.read(0)>> 6) & 0x01;
			bit3 = (color_prom.read(0) >> 7) & 0x01;
			palette[p_inc++]=(char)(0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
			bit0 = (color_prom.read(Machine.drv.total_colors) >> 0) & 0x01;
			bit1 = (color_prom.read(Machine.drv.total_colors) >> 1) & 0x01;
			bit2 = (color_prom.read(Machine.drv.total_colors) >> 2) & 0x01;
			bit3 = (color_prom.read(Machine.drv.total_colors) >> 3) & 0x01;
			palette[p_inc++]=(char)(0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
	
			color_prom.inc();
		}
	} };
	
	public static WriteHandlerPtr dec8_pf1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		switch (offset)
		{
	  	case 0:
	    case 2:
	    case 4:
	    case 6:
	      pf1_attr[offset]=data;
	  }
	  if (errorlog != null) fprintf(errorlog,"Write %d to playfield 1 register %d\n",data,offset);
	} };
	
	public static WriteHandlerPtr dec8_pf2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		switch (offset)
		{
			case 0:
			case 2:
			case 4:
			case 6:
				pf2_attr[offset]=data;
	  }
	  if (errorlog != null) fprintf(errorlog,"Write %d to playfield 2 register %d\n",data,offset);
	} };
	
	public static WriteHandlerPtr dec8_bac06_0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		switch (offset) {
		case 0:
		case 2:
		case 4:
		case 6:
			pf1_attr[offset]=data;
			break;
	
		case 0x10: /* Scroll registers */
		case 0x11:
		case 0x12:
		case 0x13:
			scroll1[offset-0x10]=data;
			break;
		}
	} };
	
	public static WriteHandlerPtr dec8_bac06_1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		switch (offset) {
		case 0:
		case 2:
		case 4:
		case 6:
			pf2_attr[offset]=data;
			break;
	
		case 0x10: /* Scroll registers */
		case 0x11:
		case 0x12:
		case 0x13:
			scroll2[offset-0x10]=data;
			break;
		}
	} };
	
	public static WriteHandlerPtr dec8_scroll1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		scroll1[offset]=data;
	} };
	
	public static WriteHandlerPtr dec8_scroll2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		scroll2[offset]=data;
	} };
	
	public static WriteHandlerPtr srdarwin_control_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int bankaddress;
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		switch (offset) {
	    	case 0: /* Top 3 bits - bank switch, bottom 4 - scroll MSB */
				bankaddress = 0x10000 + (data >> 5) * 0x4000;
				cpu_setbank(1,new UBytePtr(RAM,bankaddress));
				scroll2[0]=data&0xf;
				return;
	
	        case 1:
	        	scroll2[1]=data;
	        	return;
	    }
	} };
	
	public static WriteHandlerPtr lastmiss_control_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int bankaddress;
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		/* Bottom 4 bits - bank switch, Bits 4 & 5 - Scroll MSBs */
		bankaddress = 0x10000 + (data & 0x0f) * 0x4000;
		cpu_setbank(1,new UBytePtr(RAM,bankaddress));
	
		scroll2[0]=(data>>5)&1;
		scroll2[2]=(data>>6)&1;
	
	if (cpu_get_pc()==0xfa51) cpu_set_reset_line(1,PULSE_LINE); /* No way this can be right... */
	if (cpu_get_pc()==0xf9d2) cpu_set_reset_line(1,PULSE_LINE); /* No way this can be right... */
	
	//if (errorlog != 0) fprintf(errorlog,"PC %06x - Write %02x to %04x\n",cpu_get_pc(),data,offset+0x1802);
	} };
	
	public static WriteHandlerPtr lastmiss_scrollx_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		scroll2[1]=data;
	} };
	
	public static WriteHandlerPtr lastmiss_scrolly_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		scroll2[3]=data;
	} };
	
	public static WriteHandlerPtr gondo_scroll_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		switch (offset) {
			case 0x0:
				scroll2[1]=data; /* X LSB */
				break;
			case 0x8:
				scroll2[3]=data; /* Y LSB */
				break;
			case 0x10:
				scroll2[0]=(data>>0)&1; /* Bit 0: X MSB */
				scroll2[2]=(data>>1)&1; /* Bit 1: Y MSB */
				/* Bit 2 is also used in Gondo & Garyoret */
				break;
		}
	} };
	
	/******************************************************************************/
	
	/* 'Karnov' sprites, used by Gondomania, Last Mission, Shackled, Ghostbusters */
	static void draw_sprites1(osd_bitmap bitmap, int priority)
	{
		int offs,x,y,sprite,sprite2,colour,extra,fx,fy;
	
		for (offs = 0;offs < 0x800;offs += 8)
		{
			y=buffered_spriteram.read(offs+1)+(buffered_spriteram.read(offs)<<8);
			if ((y&0x8000) == 0) continue;
	
	        fx=buffered_spriteram.read(offs+3);
	
			if ((fx&0x1) == 0) continue;
	
			extra=fx&0x10;
	        fy=fx&0x2;
	        fx=fx&0x4;
	
			x = buffered_spriteram.read(offs+5)+(buffered_spriteram.read(offs+4)<<8);
			colour = buffered_spriteram.read(offs+6) >> 4;
			if (priority==1 && (colour&8)!=0) continue;
			if (priority==2 && (colour&8)==0) continue;
			sprite = buffered_spriteram.read(offs+7)+(buffered_spriteram.read(offs+6)<<8);
			sprite &= 0x0fff;
	
			if (extra != 0) {y=y+16;sprite&=0xffe;}
	
			x = x & 0x01ff;
			y = y & 0x01ff;
			x=(x+16)%0x200;
			y=(y+16)%0x200;
			x=256 - x;
			y=256 - y;
	
			/* Y Flip determines order of multi-sprite */
			if ((extra!=0 && fy!=0))
                        {
				sprite2=sprite;
				sprite++;
			}
			else sprite2=sprite+1;
	
			drawgfx(bitmap,Machine.gfx[1],
					sprite,
					colour,fx,fy,x,y,
					null,TRANSPARENCY_PEN,0);
	
	    	/* 1 more sprite drawn underneath */
	    	if (extra != 0)
	    		drawgfx(bitmap,Machine.gfx[1],
					sprite2,
					colour,fx,fy,x,y+16,
					null,TRANSPARENCY_PEN,0);
		}
	}
	
	/* 'Dec0' sprites, used by Cobra Command, Oscar */
	static void draw_sprites2(osd_bitmap bitmap, int priority)
	{
		int offs;
	
		/* Sprites */
		for (offs = 0;offs < 0x800;offs += 8)
		{
			int x,y,sprite,colour,multi,fx,fy,inc,flash;
	
			y =buffered_spriteram.read(offs+1)+(buffered_spriteram.read(offs)<<8);
	 		if ((y&0x8000) == 0) continue;
			x = buffered_spriteram.read(offs+5)+(buffered_spriteram.read(offs+4)<<8);
			colour = ((x & 0xf000) >> 12);
			flash=x&0x800;
			if (flash!=0 && (cpu_getcurrentframe() & 1)!=0) continue;
	
			if (priority==1 &&  (colour&4)!=0) continue;
			if (priority==2 && (colour&4)==0) continue;
	
			fx = y & 0x2000;
			fy = y & 0x4000;
			multi = (1 << ((y & 0x1800) >> 11)) - 1;	/* 1x, 2x, 4x, 8x height */
	
												/* multi = 0   1   3   7 */
			sprite = buffered_spriteram.read(offs+3)+(buffered_spriteram.read(offs+2)<<8);
			sprite &= 0x0fff;
	
			x = x & 0x01ff;
			y = y & 0x01ff;
			if (x >= 256) x -= 512;
			if (y >= 256) y -= 512;
			x = 240 - x;
			y = 240 - y;
	
			sprite &= ~multi;
			if (fy != 0)
				inc = -1;
			else
			{
				sprite += multi;
				inc = 1;
			}
	
			while (multi >= 0)
			{
				drawgfx(bitmap,Machine.gfx[1],
						sprite - multi * inc,
						colour,
						fx,fy,
						x,y - 16 * multi,
						Machine.drv.visible_area,TRANSPARENCY_PEN,0);
				multi--;
			}
		}
	}
	
	static void srdarwin_drawsprites(osd_bitmap bitmap, int pri)
	{
		int offs;
	
		/* Sprites */
		for (offs = 0;offs < 0x200;offs += 4)
		{
			int multi,fx,sx,sy,sy2,code,color;
	
			code = buffered_spriteram.read(offs+3) + ( ( buffered_spriteram.read(offs+1) & 0xe0 ) << 3 );
			sx = (241 - buffered_spriteram.read(offs+2));
		//if (sx < -7) sx += 256;
	
			sy = buffered_spriteram.read(offs);
			color = (buffered_spriteram.read(offs+1) & 0x03) + ((buffered_spriteram.read(offs+1) & 0x08) >> 1);
	
			if (pri==0 && color!=0) continue;
			if (pri==1 && color==0) continue;
	
			fx = buffered_spriteram.read(offs+1) & 0x04;
			multi = buffered_spriteram.read(offs+1) & 0x10;
	
			if (flipscreen != 0) {
				sy=240-sy;
				sx=240-sx;
				if (fx != 0) fx=0; else fx=1;
				sy2=sy-16;
			}
			else sy2=sy+16;
	
	    	drawgfx(bitmap,Machine.gfx[1],
	        		code,
					color,
					fx,flipscreen,
					sx,sy,
					Machine.drv.visible_area,TRANSPARENCY_PEN,0);
	        if (multi != 0)
	    		drawgfx(bitmap,Machine.gfx[1],
					code+1,
					color,
					fx,flipscreen,
					sx,sy2,
					Machine.drv.visible_area,TRANSPARENCY_PEN,0);
		}
	}
	
	/* Draw character tiles, each game has different colour masks */
	static void draw_characters(osd_bitmap bitmap, int mask, int shift)
	{
		int mx,my,tile,color,offs;
	
		for (offs = 0x800 - 2;offs >= 0;offs -= 2) {
			tile=videoram.read(offs+1)+((videoram.read(offs)&0xf)<<8);
	
			if (tile==0) continue;
	
			color=(videoram.read(offs)&mask)>>shift;
			mx = (offs/2) % 32;
			my = (offs/2) / 32;
	
			drawgfx(bitmap,Machine.gfx[0],
					tile,color,0,0,8*mx,8*my,
					Machine.drv.visible_area,TRANSPARENCY_PEN,0);
		}
	}
	
	/******************************************************************************/
	public static VhUpdatePtr dec8_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int my,mx,offs,color,tile;
		int scrollx,scrolly;
	
		//memset(palette_used_colors,PALETTE_COLOR_USED,256);
                for(int i=0; i<256; i++)
                {
                    palette_used_colors.write(i,PALETTE_COLOR_USED);
                }
		palette_used_colors.write(64,PALETTE_COLOR_TRANSPARENT);
		palette_used_colors.write(192,PALETTE_COLOR_TRANSPARENT);
		palette_used_colors.write(192+16,PALETTE_COLOR_TRANSPARENT);
		palette_used_colors.write(192+32,PALETTE_COLOR_TRANSPARENT);
		palette_used_colors.write(192+48,PALETTE_COLOR_TRANSPARENT);
	
		if (palette_recalc()!=null)
	    	memset(pf_dirty,1,0x800);
	
		/* Playfield 2 - Foreground */
		mx=-1; my=0;
		for (offs = 0x800;offs < 0xc00; offs += 2) {
			mx++;
			if (mx==16) {mx=0; my++;}
			if (pf_dirty[offs/2]==0) continue; else pf_dirty[offs/2]=0;
			tile=pf_video[offs+1]+(pf_video[offs]<<8);
			color = ((tile & 0xf000) >> 12);
	        tile=tile&0xfff;
			drawgfx(pf2_bitmap,Machine.gfx[2],tile,
				color, 0,0, 16*mx,16*my,
			 	null,TRANSPARENCY_NONE,0);
		}
	
		mx=-1; my=0;
		for (offs = 0xc00;offs < 0x1000; offs += 2) {
		    mx++;
		    if (mx==16) {mx=0; my++;}
		    if (pf_dirty[offs/2]==0) continue; else pf_dirty[offs/2]=0;
		    tile=pf_video[offs+1]+(pf_video[offs]<<8);
		    color = ((tile & 0xf000) >> 12);
		    tile=tile&0xfff;
			drawgfx(pf2_bitmap,Machine.gfx[2],tile,
				color, 0,0, (16*mx)+256,16*my,
			 	null,TRANSPARENCY_NONE,0);
		}
	
	  /* Playfield 1 */
	  mx=-1; my=0;
	  for (offs = 0x000;offs < 0x400; offs += 2) {
	    mx++;
	    if (mx==16) {mx=0; my++;}
	    if (pf_dirty[offs/2]==0) continue; else pf_dirty[offs/2]=0;
	    tile=pf_video[offs+1]+(pf_video[offs]<<8);
	    color = ((tile & 0xf000) >> 12);
	    tile=tile&0xfff;
			drawgfx(pf1_bitmap,Machine.gfx[3],tile,
				color, 0,0, 16*mx,16*my,
			 	null,TRANSPARENCY_NONE,0);
		}
	
	  mx=-1; my=0;
	  for (offs = 0x400;offs < 0x800; offs += 2) {
	    mx++;
	    if (mx==16) {mx=0; my++;}
	    if (pf_dirty[offs/2]==0) continue; else pf_dirty[offs/2]=0;
	    tile=pf_video[offs+1]+(pf_video[offs]<<8);
	    color = ((tile & 0xf000) >> 12);
	    tile=tile&0xfff;
			drawgfx(pf1_bitmap,Machine.gfx[3],tile,
				color, 0,0, (16*mx)+256,16*my,
			 	null,TRANSPARENCY_NONE,0);
		}
	
		scrolly=-((scroll1[2]<<8)+scroll1[3]);
		scrollx=-((scroll1[0]<<8)+scroll1[1]);
		copyscrollbitmap(bitmap,pf1_bitmap,1,new int[]{scrollx},1,new int[]{scrolly},null,TRANSPARENCY_NONE,0);
	
		draw_sprites2(bitmap,1);
	
		scrolly=-((scroll2[2]<<8)+scroll2[3]);
		scrollx=-(((scroll2[0]&1)<<8)+scroll2[1]);
		copyscrollbitmap(bitmap,pf2_bitmap,1,new int[]{scrollx},1,new int[]{scrolly},null,TRANSPARENCY_PEN,palette_transparent_pen);
	
		draw_sprites2(bitmap,2);
	
		draw_characters(bitmap,0xe0,5);
	}};
	
	/******************************************************************************/
	public static VhUpdatePtr ghostb_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int my,mx,offs,color,tile,scrollx,scrolly;
	
		/* Playfield */
		mx=-1; my=0;
		for (offs = 0x000;offs < 0x400; offs += 2) {
			mx++;
	    	if (mx==16) {mx=0; my++;}
			if (pf_dirty[offs/2]==0) continue; else pf_dirty[offs/2]=0;
	
	    	tile=pf_video[offs+1]+(pf_video[offs]<<8);
	    	color = ((tile & 0xf000) >> 12);
	    	tile=tile&0xfff;
			drawgfx(pf2_bitmap,Machine.gfx[2],tile,
				color, 0, 0, 16*mx,16*my,
			 	null,TRANSPARENCY_NONE,0);
		}
	
		mx=-1; my=0;
		for (offs = 0x400;offs < 0x800; offs += 2) {
	    	mx++;
	    	if (mx==16) {mx=0; my++;}
	    	if (pf_dirty[offs/2]==0) continue; else pf_dirty[offs/2]=0;
	
		    tile=pf_video[offs+1]+(pf_video[offs]<<8);
		    color = ((tile & 0xf000) >> 12);
		    tile=tile&0xfff;
	       drawgfx(pf2_bitmap,Machine.gfx[2],tile,
				color, 0, 0, (16*mx)+256,16*my,
			 	null,TRANSPARENCY_NONE,0);
		}
	
	    /* Rowscroll */
	    if ((pf2_attr[0]&0x4)!=0) {
	    	int[] rscroll=new int[256];
	
			scrolly=-((scroll2[2]<<8)+scroll2[3]);
	
			for (offs = 0;offs < 512;offs+=2)
				rscroll[offs/2] = -( (dec8_row.read(offs)<<8) + dec8_row.read(offs+1) ) -((scroll2[0]<<8)+scroll2[1]);
	
			copyscrollbitmap(bitmap,pf2_bitmap,256,rscroll,1,new int[]{scrolly},Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	    } else {
			scrolly=-((scroll2[2]<<8)+scroll2[3]);
			scrollx=-((scroll2[0]<<8)+scroll2[1]);
			copyscrollbitmap(bitmap,pf2_bitmap,1,new int[]{scrollx},1,new int[]{scrolly},Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	    }
	
	
		draw_sprites1(bitmap,0);
	
		/* Top layer */
		draw_characters(bitmap,0xc,2);
	}};
	
	/******************************************************************************/
	public static VhUpdatePtr srdarwin_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int my,mx,offs,color,tile;
	
		tilemap_set_scrollx( background_layer,0, (scroll2[0]<<8)+scroll2[1] );
		tilemap_update(ALL_TILEMAPS);
	
		if (palette_recalc()!=null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		/* Bottom portion of tilemap */
		tilemap_draw(bitmap,background_layer,TILEMAP_BACK);
	
		/* Low priority sprites */
		srdarwin_drawsprites(bitmap,0);
	
		/* Top portion of tilemap */
		tilemap_draw(bitmap,background_layer,TILEMAP_FRONT);
	
		/* High priority sprites */
		srdarwin_drawsprites(bitmap,1);
	
		/* Draw character tiles */
		for (offs = 0;offs < 0x400; offs ++)
		{
			mx=offs%32;
			my=offs/32;
			if (flipscreen != 0) {mx=31-mx; my=31-my;}
	        tile=videoram.read(offs);
			if (tile==0) continue;
	        color=0;      /* Not correct! */
	       	drawgfx(bitmap,Machine.gfx[0],
					tile,color,flipscreen,flipscreen,8*mx,8*my,
					Machine.drv.visible_area,TRANSPARENCY_PEN,0);
	    }
	}};
	
	/******************************************************************************/
	public static VhUpdatePtr gondo_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int my,mx,offs,color,tile;
		int scrollx,scrolly;
	
		/* Palette stuff */
		int code,i;
		int[] colmask=new int[16];
		int pal_base;
	
		palette_init_used_colors();
	
		pal_base = Machine.drv.gfxdecodeinfo[0].color_codes_start;
		for (color = 0;color < 8;color++) colmask[color] = 0;
		for (offs = 0; offs < 0x800; offs += 2)
		{
			code = videoram.read(offs+1)+((videoram.read(offs)&0xF)<<8);
			color = (videoram.read(offs)&0x70)>>4;
			code &= 0x0fff;
			colmask[color] |= Machine.gfx[0].pen_usage[code];
		}
	
		for (color = 0;color < 8;color++)
		{
			if ((colmask[color] & (1 << 0))!=0)
				palette_used_colors.write(pal_base + 8 * color,PALETTE_COLOR_TRANSPARENT);
			for (i = 1;i < 8;i++)
			{
				if ((colmask[color] & (1 << i))!=0)
					palette_used_colors.write(pal_base + 8 * color + i,PALETTE_COLOR_USED);
			}
		}
	
		pal_base = Machine.drv.gfxdecodeinfo[2].color_codes_start;
		for (color = 0;color < 16;color++) colmask[color] = 0;
		for (offs = 0; offs < 0x800; offs += 2)
		{
			code = pf_video[offs+1]+((pf_video[offs]&0xF)<<8);
			color = pf_video[offs] >> 4;
			code &= 0x0fff;
			colmask[color] |= Machine.gfx[2].pen_usage[code];
		}
	
		for (color = 0;color < 16;color++) //maybe 8?
		{
			for (i = 0;i < 16;i++)
			{
				if ((colmask[color] & (1 << i))!=0)
					palette_used_colors.write(pal_base + 16 * color + i,PALETTE_COLOR_USED);
			}
		}
	
		//memset(palette_used_colors+256,PALETTE_COLOR_USED,256);
                for(int x=0; x<256; x++)
                {
                    palette_used_colors.write(256+x,PALETTE_COLOR_USED);
                }
		palette_used_colors.write(256,PALETTE_COLOR_TRANSPARENT);
	
		if (palette_recalc()!=null)
	    	memset(pf_dirty,1,0x800);
	
		/* Playfield 2 - Foreground */
		mx=-1; my=0;
		for (offs = 0x000;offs < 0x800; offs += 2) {
			mx++;
			if (mx==32) {mx=0; my++;}
			if (pf_dirty[offs/2]==0) continue; else pf_dirty[offs/2]=0;
			tile=pf_video[offs+1]+(pf_video[offs]<<8);
			color = ((tile & 0xf000) >> 12);
	        tile=tile&0xfff;
			drawgfx(pf2_bitmap,Machine.gfx[2],tile,
				color, 0,0, 16*mx,16*my,
			 	null,TRANSPARENCY_NONE,0);
	
			/* Render masked playfield */
			drawgfx(pf1_bitmap,Machine.gfx[1],0,
					0,0,0, 16*mx,16*my,
				 	null,TRANSPARENCY_NONE,0);
			drawgfx(pf1_bitmap,Machine.gfx[2],tile,
					color, 0,0, 16*mx,16*my,
					null,TRANSPARENCY_PENS,0xff);
		}
	
		/* Bottom tiles */
		scrolly=-((scroll2[2]<<8)+scroll2[3]);
		scrollx=-((scroll2[0]<<8)+scroll2[1]);
		copyscrollbitmap(bitmap,pf2_bitmap,1,new int[]{scrollx},1,new int[]{scrolly},null,TRANSPARENCY_NONE,0);
	
		/* Sprites underneath tiles */
		draw_sprites1(bitmap,2);
	
		/* Top tiles */
		scrolly=-((scroll2[2]<<8)+scroll2[3]);
		scrollx=-((scroll2[0]<<8)+scroll2[1]);
		copyscrollbitmap(bitmap,pf1_bitmap,1,new int[]{scrollx},1,new int[]{scrolly},null,TRANSPARENCY_PEN,palette_transparent_pen);
	
		/* Sprites with priority over all tiles */
		draw_sprites1(bitmap,1);
	
		/* Top layer */
		draw_characters(bitmap,0x70,4);
	}};
	
	/******************************************************************************/
	public static VhUpdatePtr garyoret_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int my,mx,offs,color,tile;
		int scrollx,scrolly;
	
		/* Palette stuff */
		int code,i;
		int[] colmask=new int[16];
		int pal_base;
	
		palette_init_used_colors();
	
		pal_base = Machine.drv.gfxdecodeinfo[0].color_codes_start;
		for (color = 0;color < 8;color++) colmask[color] = 0;
		for (offs = 0; offs < 0x800; offs += 2)
		{
			code = videoram.read(offs+1)+((videoram.read(offs)&0xF)<<8);
			color = (videoram.read(offs)&0x70)>>4;
			code &= 0x0fff;
			colmask[color] |= Machine.gfx[0].pen_usage[code];
		}
	
		for (color = 0;color < 8;color++)
		{
			if ((colmask[color] & (1 << 0))!=0)
				palette_used_colors.write(pal_base + 8 * color,PALETTE_COLOR_TRANSPARENT);
			for (i = 1;i < 8;i++)
			{
				if ((colmask[color] & (1 << i))!=0)
					palette_used_colors.write(pal_base + 8 * color + i,PALETTE_COLOR_USED);
			}
		}
	
		pal_base = Machine.drv.gfxdecodeinfo[2].color_codes_start;
		for (color = 0;color < 16;color++) colmask[color] = 0;
		for (offs = 0; offs < 0x800; offs += 2)
		{
			code = pf_video[offs+1]+((pf_video[offs]&0xF)<<8);
			color = pf_video[offs] >> 4;
			code &= 0x0fff;
			colmask[color] |= Machine.gfx[2].pen_usage[code];
		}
	
		for (color = 0;color < 16;color++)
		{
			for (i = 0;i < 16;i++)
			{
				if ((colmask[color] & (1 << i))!=0)
					palette_used_colors.write(pal_base + 16 * color + i,PALETTE_COLOR_USED);
			}
		}
	
		//memset(palette_used_colors+256,PALETTE_COLOR_USED,256);
                for(int x=0; x<256; x++)
                {
                    palette_used_colors.write(256+x,PALETTE_COLOR_USED);
                }
		palette_used_colors.write(256,PALETTE_COLOR_TRANSPARENT);
	
		if (palette_recalc()!=null)
	    	memset(pf_dirty,1,0x800);
	
	
		/* Playfield 2 - Foreground */
		mx=-1; my=0;
		for (offs = 0x000;offs < 0x800; offs += 2) {
			mx++;
			if (mx==32) {mx=0; my++;}
			if (pf_dirty[offs/2]==0) continue; else pf_dirty[offs/2]=0;
			tile=pf_video[offs+1]+(pf_video[offs]<<8);
			color = ((tile & 0xf000) >> 12);
	        tile=tile&0xfff;
			drawgfx(pf2_bitmap,Machine.gfx[2],tile,
				color, 0,0, 16*mx,16*my,
			 	null,TRANSPARENCY_NONE,0);
	
			/* Render masked playfield
			drawgfx(pf1_bitmap,Machine.gfx[1],0,
					0,0,0, 16*mx,16*my,
				 	0,TRANSPARENCY_NONE,0);
			drawgfx(pf1_bitmap,Machine.gfx[2],tile,
					color, 0,0, 16*mx,16*my,
					0,TRANSPARENCY_PENS,0xff);*/
		}
	
		/* Bottom tiles */
		scrolly=-((scroll2[2]<<8)+scroll2[3]);
		scrollx=-((scroll2[0]<<8)+scroll2[1]);
		copyscrollbitmap(bitmap,pf2_bitmap,1,new int[]{scrollx},1,new int[]{scrolly},null,TRANSPARENCY_NONE,0);
	
		/* Sprites underneath tiles */
	//	draw_sprites1(bitmap,2);
	
		/* Priority isn't confirmed yet - different from Gondomania anyway */
	
		/* Top tiles */
		scrolly=-((scroll2[2]<<8)+scroll2[3]);
		scrollx=-((scroll2[0]<<8)+scroll2[1]);
	//	copyscrollbitmap(bitmap,pf1_bitmap,1,&scrollx,1,&scrolly,0,TRANSPARENCY_PEN,palette_transparent_pen);
	
		/* Sprites with priority over all tiles */
		draw_sprites1(bitmap,0);
	
		/* Top layer */
		draw_characters(bitmap,0x70,4);
	}};
	
	/******************************************************************************/
	public static VhUpdatePtr oscar_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int my,mx,offs,color,tile;
		int scrollx,scrolly;
	
		/* Palette stuff */
		//memset(palette_used_colors,PALETTE_COLOR_USED,512);
                for (int i=0; i<512; i++)
                {
                    palette_used_colors.write(i,PALETTE_COLOR_USED);
                }
		palette_used_colors.write(0,PALETTE_COLOR_TRANSPARENT);
		if (palette_recalc()!=null)
	    	memset(pf_dirty,1,0x800);
	
		/* Playfield */
		mx=-1; my=0;
		for (offs = 0x000;offs < 0x400; offs += 2) {
			mx++;
			if (mx==16) {mx=0; my++;}
			if (pf_dirty[offs/2]==0) continue; else pf_dirty[offs/2]=0;
			tile=pf_video[offs+1]+(pf_video[offs]<<8);
			color = ((tile & 0xf000) >> 12);
	        tile=tile&0xfff;
	
			drawgfx(pf2_bitmap,Machine.gfx[2],tile,
				color&7, 0,0, 16*mx,16*my,
			 	null,TRANSPARENCY_NONE,0);
	
			/* Render masked playfield */
			drawgfx(pf1_bitmap,Machine.gfx[1],1,
					0,0,0, 16*mx,16*my,
				 	null,TRANSPARENCY_NONE,0);
			if (color>7)
				drawgfx(pf1_bitmap,Machine.gfx[2],tile,
						color&7, 0,0, 16*mx,16*my,
						null,TRANSPARENCY_PENS,0xff);
		}
	
		mx=-1; my=0;
		for (offs = 0x400;offs < 0x800; offs += 2) {
		    mx++;
		    if (mx==16) {mx=0; my++;}
		    if (pf_dirty[offs/2]==0) continue; else pf_dirty[offs/2]=0;
		    tile=pf_video[offs+1]+(pf_video[offs]<<8);
		    color = ((tile & 0xf000) >> 12);
		    tile=tile&0xfff;
			drawgfx(pf2_bitmap,Machine.gfx[2],tile,
				color&7, 0,0, (16*mx)+256,16*my,
			 	null,TRANSPARENCY_NONE,0);
	
			/* Render masked playfield */
			drawgfx(pf1_bitmap,Machine.gfx[1],1,
					0,0,0, (16*mx)+256,16*my,
				 	null,TRANSPARENCY_NONE,0);
			if (color>7)
				drawgfx(pf1_bitmap,Machine.gfx[2],tile,
						color&7, 0,0, (16*mx)+256,16*my,
						null,TRANSPARENCY_PENS,0xff);
		}
	
		/* Render playfield */
		scrolly=-((scroll2[2]<<8)+scroll2[3]);
		scrollx=-((scroll2[0]<<8)+scroll2[1]);
		copyscrollbitmap(bitmap,pf2_bitmap,1,new int[]{scrollx},1,new int[]{scrolly},null,TRANSPARENCY_NONE,0);
	
		/* Sprites */
		draw_sprites2(bitmap,0);
	
		/* Top pens of playfield */
		copyscrollbitmap(bitmap,pf1_bitmap,1,new int[]{scrollx},1,new int[]{scrolly},null,TRANSPARENCY_PEN,palette_transparent_pen);
	
		/* Top layer */
		draw_characters(bitmap,0xf0,6);
	}};
	public static VhUpdatePtr lastmiss_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int my,mx,offs,color,tile;
		int scrollx,scrolly,i;
		int[] offsetx=new int[4];
                int[] offsety=new int[4];
                int quarter;
	
		/* Palette stuff */
		int code;
		int[] colmask=new int[16];
		int pal_base;
	
		palette_init_used_colors();
	
		pal_base = Machine.drv.gfxdecodeinfo[0].color_codes_start;
		for (color = 0;color < 8;color++) colmask[color] = 0;
		for (offs = 0; offs < 0x800; offs += 2)
		{
			code = videoram.read(offs+1)+((videoram.read(offs)&0xF)<<8);
			color = videoram.read(offs) >> 6;
			code &= 0x03ff;
			colmask[color] |= Machine.gfx[0].pen_usage[code];
		}
	
		for (color = 0;color < 8;color++) //maybe 8?
		{
			if ((colmask[color] & (1 << 0))!=0)
				palette_used_colors.write(pal_base + 16 * color,PALETTE_COLOR_TRANSPARENT);
			for (i = 1;i < 8;i++)
			{
				if ((colmask[color] & (1 << i))!=0)
					palette_used_colors.write(pal_base + 8 * color + i,PALETTE_COLOR_USED);
			}
		}
	
		pal_base = Machine.drv.gfxdecodeinfo[2].color_codes_start;
		for (color = 0;color < 16;color++) colmask[color] = 0;
		for (offs = 0; offs < 0x800; offs += 2)
		{
			code = pf_video[offs+1]+((pf_video[offs]&0xF)<<8);
			color = pf_video[offs] >> 4;
			code &= 0x0fff;
			colmask[color] |= Machine.gfx[2].pen_usage[code];
		}
	
		for (color = 0;color < 16;color++) //maybe 8?
		{
			for (i = 0;i < 16;i++)
			{
				if ((colmask[color] & (1 << i))!=0)
					palette_used_colors.write(pal_base + 16 * color + i,PALETTE_COLOR_USED);
			}
		}
	
		//memset(palette_used_colors+256,PALETTE_COLOR_USED,256);
                for(int x=0; x<256; x++)
                {
                    palette_used_colors.write(256+x,PALETTE_COLOR_USED);
                }
		palette_used_colors.write(256,PALETTE_COLOR_TRANSPARENT);
	
		if (palette_recalc()!=null)
	    	memset(pf_dirty,1,0x800);
	
	
		offsetx[0]=0;   offsety[0]=0;
		offsetx[1]=256; offsety[1]=0;
		offsetx[2]=0;   offsety[2]=256;
		offsetx[3]=256; offsety[3]=256;
	
		/* Draw the playfield */
		for (quarter=0; quarter < 4;quarter++) {
			mx=-1; my=0;
			for (offs = 0x200*quarter;offs < 0x200*quarter + 0x200; offs += 2) {
				mx++;
				if (mx==16) {mx=0; my++;}
				if (pf_dirty[offs/2]==0) continue; else pf_dirty[offs/2]=0;
				tile=pf_video[offs+1]+(pf_video[offs]<<8);
				color = ((tile & 0xf000) >> 12);
		        tile=tile&0xfff;
	
				drawgfx(pf2_bitmap,Machine.gfx[2],tile,
					color, 0,0, 16*mx+ offsetx[quarter],16*my+ offsety[quarter],
				 	null,TRANSPARENCY_NONE,0);
				drawgfx(pf1_bitmap,Machine.gfx[1],blank_tile,
						0,0,0, 16*mx+ offsetx[quarter],16*my+ offsety[quarter],
					 	null,TRANSPARENCY_NONE,0);
				if (color>7)
					drawgfx(pf1_bitmap,Machine.gfx[2],tile,
						color, 0,0, 16*mx+ offsetx[quarter],16*my+ offsety[quarter],
					 null,TRANSPARENCY_PENS,0xf);
			}
		}
	
		/* Render playfield (bottom part) */
		scrollx=-(scroll2[0]<<8)-scroll2[1];
		scrolly=-(scroll2[2]<<8)-scroll2[3];
		copyscrollbitmap(bitmap,pf2_bitmap,1,new int[]{scrollx},1,new int[] {scrolly},null,TRANSPARENCY_NONE,0);
	
		/* Sprites */
		draw_sprites1(bitmap,0);
	
		/* Render parts of playfield with priority over sprites */
		if (shackled_priority != 0)
			copyscrollbitmap(bitmap,pf1_bitmap,1,new int[]{scrollx},1,new int[] {scrolly},null,TRANSPARENCY_PEN,palette_transparent_pen);
	
		//draw_sprites1(bitmap,2);
	
		/* Top layer */
		draw_characters(bitmap,0xf0,6);
	}};
	
	
	/******************************************************************************/
	
	public static ReadHandlerPtr dec8_video_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return pf_video[offset];
	} };
	
	public static WriteHandlerPtr dec8_video_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (pf_video[offset]!=data)
		{
			pf_video[offset]=(char)data;
			pf_dirty[offset/2] = 1;
		}
	} };
	
	public static VhStartPtr dec8_vh_start = new VhStartPtr() { public int handler() 
	{
		int i;
		int[] pen_usage=Machine.gfx[1].pen_usage;
	
		pf1_bitmap=osd_create_bitmap(512,512);
		pf2_bitmap=osd_create_bitmap(512,512);
		tf2_bitmap=osd_create_bitmap(512,512);
	
		pf_video=new char[0x1000];
		pf_dirty=new char[0x1000];
		memset(pf_dirty,1,0x800);
	
		/* Kludge: Find a blank tile */
		blank_tile=0;
		for (i=0; i<0xfff; i++)
			if((pen_usage[i] & ~ 1) == 0) {
				blank_tile=i;
				i=0x1000;
			}
	
		/* Stupid kludge - fix it later :) */
		shackled_priority=0;
		if (strcmp(Machine.gamedrv.name,"breywood")==0) shackled_priority=1;
		if (strcmp(Machine.gamedrv.name,"shackled")==0) shackled_priority=1;
	
		return 0;
	} };
	
	public static VhStopPtr dec8_vh_stop = new VhStopPtr() { public void handler() 
	{
		osd_free_bitmap(pf1_bitmap);
		osd_free_bitmap(pf2_bitmap);
		osd_free_bitmap(tf2_bitmap);
		pf_video=null;
		pf_dirty=null;
	} };
	
	/* Only use with tilemap games (SRDARWIN) for now */
        static int old;
	public static WriteHandlerPtr dec8_flipscreen_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		
	
		flipscreen=data;
		if (flipscreen!=old)
			tilemap_set_flip(ALL_TILEMAPS,flipscreen!=0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
		old=data;
	} };
	
	/******************************************************************************/
	
	public static ReadHandlerPtr srdarwin_video_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return srdarwin_tileram.read(offset);
	} };
	
	public static WriteHandlerPtr srdarwin_video_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		srdarwin_tileram.write(offset,data);
		tilemap_mark_tile_dirty( background_layer,(offset/2)%32,(offset/2)/32 );
	} };
	
	public static WriteHandlerPtr get_srdarwin_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int offs=(col*2) + (row*64);
		int tile=srdarwin_tileram.read(offs+1)+(srdarwin_tileram.read(offs)<<8);
		int color=tile >> 12;
		int bank;
	
		tile=tile&0xfff;
		bank=(tile/0x100)+2;
	
		SET_TILE_INFO(bank,tile,color);
	} };
	
	public static VhStartPtr srdarwin_vh_start = new VhStartPtr() { public int handler() 
	{
		background_layer = tilemap_create(
			get_srdarwin_tile_info,
			TILEMAP_SPLIT,
			16,16,
			32,16
		);
	
		if (background_layer != null)
		{
			background_layer.transmask[0] = 0x00ff; /* Bottom 8 pens */
			background_layer.transmask[1] = 0xff00; /* Top 8 pens */
	
			return 0;
		}
	
		return 1;
	} };
}
