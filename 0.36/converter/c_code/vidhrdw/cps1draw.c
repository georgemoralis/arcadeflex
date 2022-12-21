/* don't put this file in the makefile, it is #included by cps1.c to */
/* generate 8-bit and 16-bit versions                                */

{
	int i, j;
	UINT32 dwval;
	UINT32 *src;
	const unsigned short *paldata;
	UINT32 n;
	DATATYPE *bm;

	if ( code > max || (tpens & pusage[code])==0)
	{
		/* Do not draw blank object */
		return;
	}

	if (Machine->orientation & ORIENTATION_SWAP_XY)
	{
		int temp;
		temp=sx;
		sx=sy;
		sy=dest->height-temp-size;
		temp=flipx;
		flipx=flipy;
		flipy=!temp;
	}

	if (cps1_flip_screen)
	{
		/* Handle flipped screen */
		flipx=!flipx;
		flipy=!flipy;
		sx=dest->width-sx-size;
		sy=dest->height-sy-size;
	}

	if (sx<0 || sx > dest->width-size || sy<0 || sy>dest->height-size )
	{
		/* Don't draw clipped tiles (for sprites) */
		return;
	}

	paldata=&gfx->colortable[gfx->color_granularity * color];
	src = cps1_gfx+code*delta;

	if (Machine->orientation & ORIENTATION_SWAP_XY)
	{
		int bmdelta;

		bmdelta = (dest->line[1] - dest->line[0]);
		if (flipy)
		{
			bmdelta = -bmdelta;
			sy += size-1;
		}
		if (flipx) sx+=size-1;
		for (i=0; i<size; i++)
		{
			int ny=sy;
			for (j=0; j<size/8; j++)
			{
				dwval=*src;
				n=(dwval>>28)&0x0f;
				bm = (DATATYPE *)dest->line[ny]+sx;
				IF_NOT_TRANSPARENT(n) bm[0]=paldata[n];
				n=(dwval>>24)&0x0f;
				bm = (DATATYPE *)(((unsigned char *)bm) + bmdelta);
				IF_NOT_TRANSPARENT(n) bm[0]=paldata[n];
				n=(dwval>>20)&0x0f;
				bm = (DATATYPE *)(((unsigned char *)bm) + bmdelta);
				IF_NOT_TRANSPARENT(n) bm[0]=paldata[n];
				n=(dwval>>16)&0x0f;
				bm = (DATATYPE *)(((unsigned char *)bm) + bmdelta);
				IF_NOT_TRANSPARENT(n) bm[0]=paldata[n];
				n=(dwval>>12)&0x0f;
				bm = (DATATYPE *)(((unsigned char *)bm) + bmdelta);
				IF_NOT_TRANSPARENT(n) bm[0]=paldata[n];
				n=(dwval>>8)&0x0f;
				bm = (DATATYPE *)(((unsigned char *)bm) + bmdelta);
				IF_NOT_TRANSPARENT(n) bm[0]=paldata[n];
				n=(dwval>>4)&0x0f;
				bm = (DATATYPE *)(((unsigned char *)bm) + bmdelta);
				IF_NOT_TRANSPARENT(n) bm[0]=paldata[n];
				n=dwval&0x0f;
				bm = (DATATYPE *)(((unsigned char *)bm) + bmdelta);
				IF_NOT_TRANSPARENT(n) bm[0]=paldata[n];
				if (flipy) ny-=8;
				else ny+=8;
				src++;
			}
			if (flipx) sx--;
			else sx++;
			src+=srcdelta;
		}
	}
	else
	{
		if (flipy) sy+=size-1;
		if (flipx)
		{
			sx+=size;
			for (i=0; i<size; i++)
			{
				if (flipy) bm=(DATATYPE *)dest->line[sy-i]+sx;
				else bm=(DATATYPE *)dest->line[sy+i]+sx;
				for (j=0; j<size/8; j++)
				{
					dwval=*src;
					n=(dwval>>28)&0x0f;
					IF_NOT_TRANSPARENT(n) bm[-1]=paldata[n];
					n=(dwval>>24)&0x0f;
					IF_NOT_TRANSPARENT(n) bm[-2]=paldata[n];
					n=(dwval>>20)&0x0f;
					IF_NOT_TRANSPARENT(n) bm[-3]=paldata[n];
					n=(dwval>>16)&0x0f;
					IF_NOT_TRANSPARENT(n) bm[-4]=paldata[n];
					n=(dwval>>12)&0x0f;
					IF_NOT_TRANSPARENT(n) bm[-5]=paldata[n];
					n=(dwval>>8)&0x0f;
					IF_NOT_TRANSPARENT(n) bm[-6]=paldata[n];
					n=(dwval>>4)&0x0f;
					IF_NOT_TRANSPARENT(n) bm[-7]=paldata[n];
					n=dwval&0x0f;
					IF_NOT_TRANSPARENT(n) bm[-8]=paldata[n];
					bm-=8;
					src++;
				}
				src+=srcdelta;
			}
		}
		else
		{
			for (i=0; i<size; i++)
			{
				if (flipy) bm=(DATATYPE *)dest->line[sy-i]+sx;
				else bm=(DATATYPE *)dest->line[sy+i]+sx;
				for (j=0; j<size/8; j++)
				{
					dwval=*src;
					n=(dwval>>28)&0x0f;
					IF_NOT_TRANSPARENT(n) bm[0]=paldata[n];
					n=(dwval>>24)&0x0f;
					IF_NOT_TRANSPARENT(n) bm[1]=paldata[n];
					n=(dwval>>20)&0x0f;
					IF_NOT_TRANSPARENT(n) bm[2]=paldata[n];
					n=(dwval>>16)&0x0f;
					IF_NOT_TRANSPARENT(n) bm[3]=paldata[n];
					n=(dwval>>12)&0x0f;
					IF_NOT_TRANSPARENT(n) bm[4]=paldata[n];
					n=(dwval>>8)&0x0f;
					IF_NOT_TRANSPARENT(n) bm[5]=paldata[n];
					n=(dwval>>4)&0x0f;
					IF_NOT_TRANSPARENT(n) bm[6]=paldata[n];
					n=dwval&0x0f;
					IF_NOT_TRANSPARENT(n) bm[7]=paldata[n];
					bm+=8;
					src++;
				}
				src+=srcdelta;
			}
		}
	}
}
