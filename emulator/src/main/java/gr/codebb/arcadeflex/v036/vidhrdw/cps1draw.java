/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.codebb.arcadeflex.v036.vidhrdw;

import gr.codebb.arcadeflex.common.SubArrays.UShortArray;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.drivers.cps1.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.cps1.*;

public class cps1draw {

    public static void cps1_draw_gfx(osd_bitmap dest, GfxElement gfx, int code, int color, int flipx, int flipy, int sx, int sy, int tpens, int[] pusage, int size, int max, int delta, int srcdelta) {
        	int i, j;
	int dwval;
	int[] src;
        int src_ptr;
	UShortArray paldata;
	int n;
	UBytePtr bm;

	if ( code > max || (tpens & pusage[code])==0)
	{
		/* Do not draw blank object */
		return;
	}

	if ((Machine.orientation & ORIENTATION_SWAP_XY)!=0)
	{
		int temp;
		temp=sx;
		sx=sy;
		sy=dest.height-temp-size;
		temp=flipx;
		flipx=flipy;
		flipy=NOT(temp);
	}

	if (cps1_flip_screen != 0)
	{
		/* Handle flipped screen */
		flipx=NOT(flipx);
		flipy=NOT(flipy);
		sx=dest.width-sx-size;
		sy=dest.height-sy-size;
	}

	if (sx<0 || sx > dest.width-size || sy<0 || sy>dest.height-size )
	{
		/* Don't draw clipped tiles (for sprites) */
		return;
	}

	paldata=new UShortArray(gfx.colortable,gfx.color_granularity * color);
	//src = cps1_gfx+code*delta;
        src_ptr=code*delta;

	if ((Machine.orientation & ORIENTATION_SWAP_XY)!=0)
	{
		int bmdelta;
		bmdelta = (dest.line[1].read() - dest.line[0].read());
		if (flipy != 0)
		{
			bmdelta = -bmdelta;
			sy += size-1;
		}
		if (flipx != 0) sx+=size-1;
		for (i=0; i<size; i++)
		{
			int ny=sy;
			for (j=0; j<size/8; j++)
			{
				dwval=cps1_gfx[src_ptr];
				n=(dwval>>28)&0x0f;
				bm = new UBytePtr(dest.line[ny],sx);
				if ((tpens & (0x01 << n))!=0) bm.write(0,paldata.read(n));
				n=(dwval>>24)&0x0f;
				bm = new UBytePtr(bm, bmdelta);
				if ((tpens & (0x01 << n))!=0) bm.write(0,paldata.read(n));
				n=(dwval>>20)&0x0f;
				bm = new UBytePtr(bm, bmdelta);
				if ((tpens & (0x01 << n))!=0) bm.write(0,paldata.read(n));
				n=(dwval>>16)&0x0f;
				bm = new UBytePtr(bm, bmdelta);
				if ((tpens & (0x01 << n))!=0) bm.write(0,paldata.read(n));
				n=(dwval>>12)&0x0f;
				bm = new UBytePtr(bm, bmdelta);
				if ((tpens & (0x01 << n))!=0) bm.write(0,paldata.read(n));
				n=(dwval>>8)&0x0f;
				bm = new UBytePtr(bm, bmdelta);
				if ((tpens & (0x01 << n))!=0) bm.write(0,paldata.read(n));
				n=(dwval>>4)&0x0f;
				bm = new UBytePtr(bm, bmdelta);
				if ((tpens & (0x01 << n))!=0) bm.write(0,paldata.read(n));
				n=dwval&0x0f;
				bm = new UBytePtr(bm, bmdelta);
				if ((tpens & (0x01 << n))!=0) bm.write(0,paldata.read(n));
				if (flipy != 0) ny-=8;
				else ny+=8;
				src_ptr++;
			}
			if (flipx != 0) sx--;
			else sx++;
			src_ptr+=srcdelta;
		}
	}
	else
	{
		if (flipy != 0) sy+=size-1;
		if (flipx != 0)
		{
                    sx+=size;
			for (i=0; i<size; i++)
			{
				if (flipy != 0) bm=new UBytePtr(dest.line[sy-i],sx);
				else bm=new UBytePtr(dest.line[sy+i],sx);
				for (j=0; j<size/8; j++)
				{
					dwval=cps1_gfx[src_ptr];
					n=(dwval>>28)&0x0f;
					if ((tpens & (0x01 << n))!=0) bm.write(-1,paldata.read(n));
					n=(dwval>>24)&0x0f;
					if ((tpens & (0x01 << n))!=0) bm.write(-2,paldata.read(n));
					n=(dwval>>20)&0x0f;
					if ((tpens & (0x01 << n))!=0) bm.write(-3,paldata.read(n));
					n=(dwval>>16)&0x0f;
					if ((tpens & (0x01 << n))!=0) bm.write(-4,paldata.read(n));
					n=(dwval>>12)&0x0f;
					if ((tpens & (0x01 << n))!=0) bm.write(-5,paldata.read(n));
					n=(dwval>>8)&0x0f;
					if ((tpens & (0x01 << n))!=0) bm.write(-6,paldata.read(n));
					n=(dwval>>4)&0x0f;
					if ((tpens & (0x01 << n))!=0) bm.write(-7,paldata.read(n));
					n=dwval&0x0f;
					if ((tpens & (0x01 << n))!=0) bm.write(-8,paldata.read(n));
					bm.offset-=8;
					src_ptr++;
				}
				src_ptr+=srcdelta;
			}
		}
		else
		{
                    for (i=0; i<size; i++)
			{
				if (flipy != 0) bm=new UBytePtr(dest.line[sy-i],sx);
				else bm=new UBytePtr(dest.line[sy+i],sx);
				for (j=0; j<size/8; j++)
				{
					dwval=cps1_gfx[src_ptr];
					n=(dwval>>28)&0x0f;
					if ((tpens & (0x01 << n))!=0) bm.write(0,paldata.read(n));
					n=(dwval>>24)&0x0f;
					if ((tpens & (0x01 << n))!=0) bm.write(1,paldata.read(n));
					n=(dwval>>20)&0x0f;
					if ((tpens & (0x01 << n))!=0) bm.write(2,paldata.read(n));
					n=(dwval>>16)&0x0f;
					if ((tpens & (0x01 << n))!=0) bm.write(3,paldata.read(n));
					n=(dwval>>12)&0x0f;
					if ((tpens & (0x01 << n))!=0) bm.write(4,paldata.read(n));
					n=(dwval>>8)&0x0f;
					if ((tpens & (0x01 << n))!=0) bm.write(5,paldata.read(n));
					n=(dwval>>4)&0x0f;
					if ((tpens & (0x01 << n))!=0) bm.write(6,paldata.read(n));
					n=dwval&0x0f;
					if ((tpens & (0x01 << n))!=0) bm.write(7,paldata.read(n));
					bm.offset+=8;
					src_ptr++;
				}
				src_ptr+=srcdelta;
			}
		}
	}
    }
        public static void cps1_draw_gfx_opaque(osd_bitmap dest, GfxElement gfx, int code, int color, int flipx, int flipy, int sx, int sy, int tpens, int[] pusage, int size, int max, int delta, int srcdelta) {
        	int i, j;
	int dwval;
	int[] src;
        int src_ptr;
	UShortArray paldata;
	int n;
	UBytePtr bm;

	if ( code > max || (tpens & pusage[code])==0)
	{
		/* Do not draw blank object */
		return;
	}

	if ((Machine.orientation & ORIENTATION_SWAP_XY)!=0)
	{
		int temp;
		temp=sx;
		sx=sy;
		sy=dest.height-temp-size;
		temp=flipx;
		flipx=flipy;
		flipy=NOT(temp);
	}

	if (cps1_flip_screen != 0)
	{
		/* Handle flipped screen */
		flipx=NOT(flipx);
		flipy=NOT(flipy);
		sx=dest.width-sx-size;
		sy=dest.height-sy-size;
	}

	if (sx<0 || sx > dest.width-size || sy<0 || sy>dest.height-size )
	{
		/* Don't draw clipped tiles (for sprites) */
		return;
	}

	paldata=new UShortArray(gfx.colortable,gfx.color_granularity * color);
	//src = cps1_gfx+code*delta;
        src_ptr=code*delta;

	if ((Machine.orientation & ORIENTATION_SWAP_XY)!=0)
	{
		int bmdelta;
		bmdelta = (dest.line[1].read() - dest.line[0].read());
		if (flipy != 0)
		{
			bmdelta = -bmdelta;
			sy += size-1;
		}
		if (flipx != 0) sx+=size-1;
		for (i=0; i<size; i++)
		{
			int ny=sy;
			for (j=0; j<size/8; j++)
			{
				dwval=cps1_gfx[src_ptr];
				n=(dwval>>28)&0x0f;
				bm = new UBytePtr(dest.line[ny],sx);
				 bm.write(0,paldata.read(n));
				n=(dwval>>24)&0x0f;
				bm = new UBytePtr(bm, bmdelta);
				 bm.write(0,paldata.read(n));
				n=(dwval>>20)&0x0f;
				bm = new UBytePtr(bm, bmdelta);
				 bm.write(0,paldata.read(n));
				n=(dwval>>16)&0x0f;
				bm = new UBytePtr(bm, bmdelta);
				 bm.write(0,paldata.read(n));
				n=(dwval>>12)&0x0f;
				bm = new UBytePtr(bm, bmdelta);
				 bm.write(0,paldata.read(n));
				n=(dwval>>8)&0x0f;
				bm = new UBytePtr(bm, bmdelta);
				 bm.write(0,paldata.read(n));
				n=(dwval>>4)&0x0f;
				bm = new UBytePtr(bm, bmdelta);
				 bm.write(0,paldata.read(n));
				n=dwval&0x0f;
				bm = new UBytePtr(bm, bmdelta);
				 bm.write(0,paldata.read(n));
				if (flipy != 0) ny-=8;
				else ny+=8;
				src_ptr++;
			}
			if (flipx != 0) sx--;
			else sx++;
			src_ptr+=srcdelta;
		}
	}
	else
	{
		if (flipy != 0) sy+=size-1;
		if (flipx != 0)
		{
			sx+=size;
			for (i=0; i<size; i++)
			{
				if (flipy != 0) bm=new UBytePtr(dest.line[sy-i],sx);
				else bm=new UBytePtr(dest.line[sy+i],sx);
				for (j=0; j<size/8; j++)
				{
					dwval=cps1_gfx[src_ptr];
					n=(dwval>>28)&0x0f;
					 bm.write(-1,paldata.read(n));
					n=(dwval>>24)&0x0f;
					 bm.write(-2,paldata.read(n));
					n=(dwval>>20)&0x0f;
					 bm.write(-3,paldata.read(n));
					n=(dwval>>16)&0x0f;
					 bm.write(-4,paldata.read(n));
					n=(dwval>>12)&0x0f;
					 bm.write(-5,paldata.read(n));
					n=(dwval>>8)&0x0f;
					 bm.write(-6,paldata.read(n));
					n=(dwval>>4)&0x0f;
					 bm.write(-7,paldata.read(n));
					n=dwval&0x0f;
					 bm.write(-8,paldata.read(n));
					bm.offset-=8;
					src_ptr++;
				}
				src_ptr+=srcdelta;
			}
		}
		else
		{
                    for (i=0; i<size; i++)
			{
				if (flipy != 0) bm=new UBytePtr(dest.line[sy-i],sx);
				else bm=new UBytePtr(dest.line[sy+i],sx);
				for (j=0; j<size/8; j++)
				{
					dwval=cps1_gfx[src_ptr];
					n=(dwval>>28)&0x0f;
					 bm.write(0,paldata.read(n));
					n=(dwval>>24)&0x0f;
					 bm.write(1,paldata.read(n));
					n=(dwval>>20)&0x0f;
					 bm.write(2,paldata.read(n));
					n=(dwval>>16)&0x0f;
					 bm.write(3,paldata.read(n));
					n=(dwval>>12)&0x0f;
					 bm.write(4,paldata.read(n));
					n=(dwval>>8)&0x0f;
					 bm.write(5,paldata.read(n));
					n=(dwval>>4)&0x0f;
					 bm.write(6,paldata.read(n));
					n=dwval&0x0f;
					 bm.write(7,paldata.read(n));
					bm.offset+=8;
					src_ptr++;
				}
				src_ptr+=srcdelta;
			}
		}
	}
    }
}
