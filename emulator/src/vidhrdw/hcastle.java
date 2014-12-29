/***************************************************************************

	Haunted Castle video emulation

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package vidhrdw;

import static arcadeflex.libc.*;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static vidhrdw.generic.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.mame.*;
import static arcadeflex.ptrlib.*;
import static arcadeflex.libc_old.*;
import static arcadeflex.video.*;
import static vidhrdw.konamiic.*;
import static mame.palette.*;
import static mame.paletteH.*;


public class hcastle
{
	
	static osd_bitmap pf1_bitmap;
	static osd_bitmap pf2_bitmap;
	static char[] dirty_pf1;
        static char[] dirty_pf2;
	public static UBytePtr hcastle_pf1_videoram=new UBytePtr();
        public static UBytePtr hcastle_pf2_videoram=new UBytePtr();
	static int gfx_bank;
	
	
	
	public static VhConvertColorPromPtr hcastle_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(UByte []palette, char []colortable, UBytePtr color_prom) 
	{
		int i,chip,pal,clut;
                int c_ptr=0;
		for (chip = 0;chip < 2;chip++)
		{
			for (pal = 0;pal < 8;pal++)
			{
				clut = (pal & 1) + 2 * chip;
				for (i = 0;i < 256;i++)
				{
					if ((pal & 1) == 0)	/* sprites */
					{
						if (color_prom.read(256 * clut + i) == 0)
							colortable[c_ptr++] = 0;
						else
							colortable[c_ptr++] = (char)(16 * pal + color_prom.read(256 * clut + i));
					}
					else
						colortable[c_ptr++] = (char)(16 * pal + color_prom.read(256 * clut + i));
				}
			}
		}
	} };
	
	
	
	/*****************************************************************************/
	
	public static VhStartPtr hcastle_vh_start = new VhStartPtr() { public int handler() 
	{
	 	if ((pf1_bitmap = osd_create_bitmap(64*8,32*8)) == null)
			return 1;
		if ((pf2_bitmap = osd_create_bitmap(64*8,32*8)) == null)
			return 1;
	
		dirty_pf1=new char[0x1000];
		dirty_pf2=new char[0x1000];
		memset(dirty_pf1,1,0x1000);
		memset(dirty_pf2,1,0x1000);
	
		return 0;
	} };
	
	public static VhStopPtr hcastle_vh_stop = new VhStopPtr() { public void handler() 
	{
		osd_free_bitmap(pf1_bitmap);
		osd_free_bitmap(pf2_bitmap);
		dirty_pf1=null;
		dirty_pf2=null;
	} };
	
	
	
	public static WriteHandlerPtr hcastle_pf1_video_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		hcastle_pf1_videoram.write(offset,data);
		dirty_pf1[offset]=1;
	} };
	
	public static WriteHandlerPtr hcastle_pf2_video_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		hcastle_pf2_videoram.write(offset,data);
		dirty_pf2[offset]=1;
	} };
	
	public static WriteHandlerPtr hcastle_gfxbank_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		gfx_bank = data;
	} };
	
	public static ReadHandlerPtr hcastle_gfxbank_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return gfx_bank;
	} };
	
	public static WriteHandlerPtr hcastle_pf1_control_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (offset==3)
		{
			if ((data&0x8)==0)
				buffer_spriteram(new UBytePtr(spriteram,0x800),0x800);
			else
				buffer_spriteram(spriteram,0x800);
		}
		K007121_ctrl_0_w.handler(offset,data);
	} };
	
	public static WriteHandlerPtr hcastle_pf2_control_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (offset==3)
		{
			if ((data&0x8)==0)
				buffer_spriteram_2(new UBytePtr(spriteram_2,0x800),0x800);
			else
				buffer_spriteram_2(spriteram_2,0x800);
		}
		K007121_ctrl_1_w.handler(offset,data);
	} };
	
	/*****************************************************************************/
	
	static void draw_sprites( osd_bitmap bitmap, UBytePtr sbank, int bank )
	{
		int bank_base = (bank == 0) ? 0x4000 * (gfx_bank & 1) : 0;
		K007121_sprites_draw(bank,bitmap,new UBytePtr(sbank),(K007121_ctrlram[bank][6]&0x30)*2,0,bank_base);
	}
	
	/*****************************************************************************/
	static int old_pf1,old_pf2;
	public static VhUpdatePtr hcastle_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs,tile,color,mx,my,bank,scrollx,scrolly;
		int pf2_bankbase,pf1_bankbase,attr;
		int bit0,bit1,bit2,bit3;
		
	
	
		palette_init_used_colors();
		memset(palette_used_colors,PALETTE_COLOR_USED,128);
		palette_used_colors.write(0*16,PALETTE_COLOR_TRANSPARENT);
		palette_used_colors.write(1*16,PALETTE_COLOR_TRANSPARENT);
		palette_used_colors.write(2*16,PALETTE_COLOR_TRANSPARENT);
		palette_used_colors.write(3*16,PALETTE_COLOR_TRANSPARENT);
	
		pf1_bankbase = 0x0000;
		pf2_bankbase = 0x4000 * ((gfx_bank & 2) >> 1);
	
		if ((K007121_ctrlram[0][3] & 0x01)!=0) pf1_bankbase += 0x2000;
		if ((K007121_ctrlram[1][3] & 0x01)!=0) pf2_bankbase += 0x2000;
	
		if (palette_recalc()!=null || pf1_bankbase!=old_pf1 || pf2_bankbase!=old_pf2)
		{
			memset(dirty_pf1,1,0x1000);
			memset(dirty_pf2,1,0x1000);
		}
		old_pf1=pf1_bankbase;
		old_pf2=pf2_bankbase;
	
		/* Draw foreground */
		bit0 = (K007121_ctrlram[0][0x05] >> 0) & 0x03;
		bit1 = (K007121_ctrlram[0][0x05] >> 2) & 0x03;
		bit2 = (K007121_ctrlram[0][0x05] >> 4) & 0x03;
		bit3 = (K007121_ctrlram[0][0x05] >> 6) & 0x03;
		for (my = 0;my < 32;my++)
		{
			for (mx = 0;mx < 64;mx++)
			{
				if (mx >= 32)
					offs = 0x800 + my*32 + (mx-32);
				else
					offs = my*32 + mx;
	
				if (dirty_pf1[offs]==0 && dirty_pf1[offs+0x400]==0) continue;
				dirty_pf1[offs]=dirty_pf1[offs+0x400]=0;
	
				tile = hcastle_pf1_videoram.read(offs+0x400);
				attr = hcastle_pf1_videoram.read(offs);
				color = attr & 0x7;
				bank = ((attr & 0x80) >> 7) |
						((attr >> (bit0+2)) & 0x02) |
						((attr >> (bit1+1)) & 0x04) |
						((attr >> (bit2  )) & 0x08) |
						((attr >> (bit3-1)) & 0x10);
	
				drawgfx(pf1_bitmap,Machine.gfx[0],
						tile+bank*0x100+pf1_bankbase,
						((K007121_ctrlram[0][6]&0x30)*2+16)+color,
						0,0,
						8*mx,8*my,
						null,TRANSPARENCY_NONE,0);
			}
		}
	
		/* Draw background */
		bit0 = (K007121_ctrlram[1][0x05] >> 0) & 0x03;
		bit1 = (K007121_ctrlram[1][0x05] >> 2) & 0x03;
		bit2 = (K007121_ctrlram[1][0x05] >> 4) & 0x03;
		bit3 = (K007121_ctrlram[1][0x05] >> 6) & 0x03;
		for (my = 0;my < 32;my++)
		{
			for (mx = 0;mx < 64;mx++)
			{
				if (mx >= 32)
					offs = 0x800 + my*32 + (mx-32);
				else
					offs = my*32 + mx;
	
				if (dirty_pf2[offs]==0 && dirty_pf2[offs+0x400]==0) continue;
				dirty_pf2[offs]=dirty_pf2[offs+0x400]=0;
	
				tile = hcastle_pf2_videoram.read(offs+0x400);
				attr = hcastle_pf2_videoram.read(offs);
				color = attr & 0x7;
				bank = ((attr & 0x80) >> 7) |
						((attr >> (bit0+2)) & 0x02) |
						((attr >> (bit1+1)) & 0x04) |
						((attr >> (bit2  )) & 0x08) |
						((attr >> (bit3-1)) & 0x10);
	
				drawgfx(pf2_bitmap,Machine.gfx[1],
						tile+bank*0x100+pf2_bankbase,
						((K007121_ctrlram[1][6]&0x30)*2+16)+color,
						0,0,
						8*mx,8*my,
						null,TRANSPARENCY_NONE,0);
			}
		}
	
	
	//	/* Sprite priority */
	//	if (K007121_ctrlram[0][3]&0x20)
		if ((gfx_bank & 0x04) == 0)
		{
			scrolly = -K007121_ctrlram[1][2];
			scrollx = -((K007121_ctrlram[1][1]<<8)+K007121_ctrlram[1][0]);
			copyscrollbitmap(bitmap,pf2_bitmap,1,new int[]{scrollx},1,new int[]{scrolly},Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	
			draw_sprites( bitmap, buffered_spriteram, 0 );
			draw_sprites( bitmap, buffered_spriteram_2, 1 );
	
			scrolly = -K007121_ctrlram[0][2];
			scrollx = -((K007121_ctrlram[0][1]<<8)+K007121_ctrlram[0][0]);
			copyscrollbitmap(bitmap,pf1_bitmap,1,new int[] {scrollx},1,new int[]{scrolly},Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
		}
		else
		{
			scrolly = -K007121_ctrlram[1][2];
			scrollx = -((K007121_ctrlram[1][1]<<8)+K007121_ctrlram[1][0]);
			copyscrollbitmap(bitmap,pf2_bitmap,1,new int[] {scrollx},1,new int[]{scrolly},Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	
			scrolly = -K007121_ctrlram[0][2];
			scrollx = -((K007121_ctrlram[0][1]<<8)+K007121_ctrlram[0][0]);
			copyscrollbitmap(bitmap,pf1_bitmap,1,new int[] {scrollx},1,new int[]{scrolly},Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
	
			draw_sprites( bitmap, buffered_spriteram, 0 );
			draw_sprites( bitmap, buffered_spriteram_2, 1 );
		}
	} };
}
