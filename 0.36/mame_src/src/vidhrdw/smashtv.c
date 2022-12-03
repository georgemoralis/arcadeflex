/*************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

**************************************************************************/
#include "driver.h"
#include "cpu/tms34010/tms34010.h"

void wms_stateload(void);
void wms_statesave(void);
void wms_update_partial(int scanline);

UINT16 *wms_videoram;
INT32 wms_videoram_size = 0x80000;

/* Variables in machine/smashtv.c */
extern UINT8 *wms_cmos_ram;
extern UINT8 wms_autoerase_enable;
extern UINT8 wms_autoerase_reset;
extern UINT32 wms_dma_pal_word;

static int last_update_scanline;
static UINT8 update_status;

static UINT32 current_offset;
static int current_rowbytes;

void wms_vram_w(int offset, int data)
{
	UINT32 tempw,tempwb;
	UINT16 tempwordhi;
	UINT16 tempwordlo;
	UINT16 datalo;
	UINT16 datahi;
	UINT32 mask;

	/* first vram data */
	tempwordhi = wms_videoram[offset+1];
	tempwordlo = wms_videoram[offset];
	tempw = (wms_videoram[offset]&0x00ff) + ((wms_videoram[offset+1]&0x00ff)<<8);
	tempwb = COMBINE_WORD(tempw,data);
	datalo = tempwb&0x00ff;
	datahi = (tempwb&0xff00)>>8;
	wms_videoram[offset] = (tempwordlo&0xff00)|datalo;
	wms_videoram[offset+1] = (tempwordhi&0xff00)|datahi;

	/* now palette data */
	tempwordhi = wms_videoram[offset+1];
	tempwordlo = wms_videoram[offset];
	mask = (~(((UINT32) data)>>16))|0xffff0000;
	data = ((data&0xffff0000) | wms_dma_pal_word) & mask;
	tempw = (((UINT16) (wms_videoram[offset]&0xff00))>>8) + (wms_videoram[offset+1]&0xff00);
	tempwb = COMBINE_WORD(tempw,data);
	datalo = tempwb&0x00ff;
	datahi = (tempwb&0xff00)>>8;
	wms_videoram[offset] = (tempwordlo&0x00ff)|(datalo<<8);
	wms_videoram[offset+1] = (tempwordhi&0x00ff)|(datahi<<8);
}

void wms_objpalram_w(int offset, int data)
{
	UINT32 tempw,tempwb;
	UINT16 tempwordhi;
	UINT16 tempwordlo;
	UINT16 datalo;
	UINT16 datahi;

	tempwordhi = wms_videoram[offset+1];
	tempwordlo = wms_videoram[offset];
	tempw = ((wms_videoram[offset]&0xff00)>>8) + (wms_videoram[offset+1]&0xff00);
	tempwb = COMBINE_WORD(tempw,data);
	datalo = tempwb&0x00ff;
	datahi = (tempwb&0xff00)>>8;
	wms_videoram[offset] = (tempwordlo&0x00ff)|(datalo<<8);
	wms_videoram[offset+1] = (tempwordhi&0x00ff)|(datahi<<8);
}

int wms_vram_r(int offset)
{
	return (wms_videoram[offset]&0x00ff) | (wms_videoram[offset+1]<<8);
}

int wms_objpalram_r(int offset)
{
	return (wms_videoram[offset]>>8) | (wms_videoram[offset+1]&0xff00);
}

int wms_vh_start(void)
{
	if ((wms_cmos_ram = malloc(0x8000)) == 0)
	{
		if (errorlog) fprintf(errorlog, "smashtv.c: Couldn't Alloc CMOS RAM\n");
		return 1;
	}
	if ((paletteram = malloc(0x4000)) == 0)
	{
		if (errorlog) fprintf(errorlog, "smashtv.c: Couldn't Alloc color RAM\n");
		free(wms_cmos_ram);
		return 1;
	}
	if ((wms_videoram = malloc(wms_videoram_size)) == 0)
	{
		if (errorlog) fprintf(errorlog, "smashtv.c: Couldn't Alloc video RAM\n");
		free(wms_cmos_ram);
		free(paletteram);
		return 1;
	}
	memset(wms_cmos_ram,0,0x8000);
	update_status = 1;
	return 0;
}

int wms_t_vh_start(void)
{
	if ((wms_cmos_ram = malloc(0x10000)) == 0)
	{
		if (errorlog) fprintf(errorlog, "smashtv.c: Couldn't Alloc CMOS RAM\n");
		return 1;
	}
	if ((paletteram = malloc(0x20000)) == 0)
	{
		if (errorlog) fprintf(errorlog, "smashtv.c: Couldn't Alloc color RAM\n");
		free(wms_cmos_ram);
		return 1;
	}
	if ((wms_videoram = malloc(0x100000)) == 0)
	{
		if (errorlog) fprintf(errorlog, "smashtv.c: Couldn't Alloc video RAM\n");
		free(wms_cmos_ram);
		free(paletteram);
		return 1;
	}
	memset(wms_cmos_ram,0,0x8000);
	update_status = 1;
	return 0;
}

void wms_vh_stop (void)
{
	free(wms_cmos_ram);
	free(wms_videoram);
	free(paletteram);
}

void wms_update_partial(int scanline)
{
	struct osd_bitmap *bitmap = Machine->scrbitmap;
	UINT16 *pens = Machine->pens;
	UINT32 offset;
	int v, h, width;

	/* don't draw if we're already past the bottom of the screen */
	if (last_update_scanline >= Machine->drv->visible_area.max_y)
		return;

	/* don't start before the top of the screen */
	if (last_update_scanline < Machine->drv->visible_area.min_y)
		last_update_scanline = Machine->drv->visible_area.min_y;

	/* bail if there's nothing to do */
	if (last_update_scanline > scanline)
		return;

	/* determine the base of the videoram */
	offset = (~TMS34010_get_DPYSTRT(0) & 0x1ff0) << 5;
	offset += 512 * (last_update_scanline - Machine->drv->visible_area.min_y);
	offset &= 0x3ffff;

	/* determine how many pixels to copy */
	width = Machine->drv->visible_area.max_x;

	/* 16-bit refresh case */
	if (bitmap->depth == 16)
	{
		/* loop over rows */
		for (v = last_update_scanline; v <= scanline; v++)
		{
			UINT16 *src = &wms_videoram[offset];

			/* handle the refresh */
			if (update_status)
			{
				UINT16 *dst = (UINT16 *)&bitmap->line[v][0];
				UINT32 diff = dst - src;

				/* copy one row */
				for (h = 0; h < width; h++, src++)
					*(src + diff) = pens[*src];
			}

			/* handle the autoerase */
			if (wms_autoerase_enable)
				memcpy(&wms_videoram[offset], &wms_videoram[510 * 512], width * sizeof(UINT16));

			/* point to the next row */
			offset = (offset + 512) & 0x3ffff;
		}
	}

	/* 8-bit refresh case */
	else
	{
		/* loop over rows */
		for (v = last_update_scanline; v <= scanline; v++)
		{
			UINT16 *src = &wms_videoram[offset];

			/* handle the refresh */
			if (update_status)
			{
				UINT8 *dst = &bitmap->line[v][0];

				for (h = 0; h < width; h++)
					*dst++ = pens[*src++];
			}

			/* handle the autoerase */
			if (wms_autoerase_enable)
				memcpy(&wms_videoram[offset], &wms_videoram[510 * 512], width * sizeof(UINT16));

			/* point to the next row */
			offset = (offset + 512) & 0x3ffff;
		}
	}

	/* remember where we left off */
	last_update_scanline = scanline + 1;
}

void wms_display_addr_changed(UINT32 offs, int rowbytes, int scanline)
{
	wms_update_partial(scanline);
	current_offset = offs;
	current_rowbytes = rowbytes;
}

void wms_display_interrupt(int scanline)
{
	wms_update_partial(scanline);
}

void wms_vh_eof(void)
{
	/* update status == 2: we just updated this frame, don't do any work */
	if (update_status == 2)
		update_status = 1;

	/* update status == 1: we've been updating this frame; finish it off */
	else
	{
		wms_update_partial(Machine->drv->visible_area.max_y);
		last_update_scanline = 0;
		if (update_status)
			update_status--;
	}

	/* turn off the autoerase (NARC needs this) */
	if (wms_autoerase_reset)
		wms_autoerase_enable = 0;
}

void wms_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	if (palette_recalc() || full_refresh)  last_update_scanline = 0;

	wms_update_partial(Machine->drv->visible_area.max_y);
	last_update_scanline = 0;
	update_status = 2;

	//if (keyboard_pressed(KEYCODE_Q)) wms_statesave();
	//if (keyboard_pressed(KEYCODE_W)) wms_stateload();

	if (keyboard_pressed(KEYCODE_E)&&errorlog) fprintf(errorlog, "log spot\n");
	//if (keyboard_pressed(KEYCODE_R)&&errorlog) fprintf(errorlog, "adpcm: okay\n");
}
