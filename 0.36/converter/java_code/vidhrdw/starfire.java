/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

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

public class starfire
{
	
	unsigned char starfire_vidctrl;
	unsigned char starfire_vidctrl1;
	unsigned char *starfire_videoram;
	unsigned char *starfire_colorram;
	unsigned char starfire_color = 0;
	
	public static WriteHandlerPtr starfire_vidctrl_w = new WriteHandlerPtr() { public void handler(int offset, int data){
	    starfire_vidctrl = data;
	} };
	
	public static WriteHandlerPtr starfire_vidctrl1_w = new WriteHandlerPtr() { public void handler(int offset, int data){
	    starfire_vidctrl1 = data;
	} };
	
	public static WriteHandlerPtr starfire_colorram_w = new WriteHandlerPtr() { public void handler(int offset, int data){
	    starfire_color = data & 0x1f;
	
	    if ((offset & 0xE0) == 0) {
		int r,g,b;
		int d1 = offset & 0x100;
	
		starfire_colorram[offset & 0xfeff] = data;
	
		if ((starfire_vidctrl1 & 0x40) != 0) {
		int reg = offset & 0x1f;
		    if ((offset & 0x200) != 0)
			reg |= 0x20;
	
		    r = (data & 0x03)*0x49;
		    if (d1 != 0)
			r |= 0x24;
	
		    b = ((data & 0x1c)*0x49) >> 3;
		    g = ((data & 0xe0)*0x49) >> 6;
	
		    palette_change_color(reg,r,g,b);
		}
	    } else
		if(!(starfire_vidctrl1 & 0x80))
		    starfire_colorram[offset] = data & 0x1f;
	} };
	
	public static ReadHandlerPtr starfire_colorram_r = new ReadHandlerPtr() { public int handler(int offset){
	    if ((offset & 0xE0) == 0)
		return starfire_colorram[offset&0xfeff];
	    else
		return starfire_colorram[offset];
	} };
	
	public static WriteHandlerPtr starfire_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data){
	
	    int i, d0, d1, m0, m1, v0, v1;
	    unsigned char c,d,d2;
	    int offset2 = (offset+0x100)&0x1fff;
	    int source;
	
	    if ((!(offset & 0xE0)) && (!(starfire_vidctrl1 & 0x20)))
	        return;
	
	    /* Handle selector 6A */
	    if ((offset & 0x2000) != 0) {
	        c = starfire_vidctrl;
		source = 1;
	    } else {
	        c = starfire_vidctrl >> 4;
		source = 0;
	    }
	
	    offset &= 0x1FFF;
	
	    /* Handle mirror bits in 5B-5E */
	    d2=0;
	    d = data & 0xFF;
	    if ((c & 0x01) != 0) {
		for (i=7; i>-1; i--) {
		    d2 = d2 | ((d & 0x80) >> i);
		    d = d << 1;
		}
	    }
	    else
		{d2 = d;}
	
	
	    /* Handle shifters 6E,6D */
	    i = 8 - ((c & 0x0E) >> 1);
	
	    d1 = d2 << i;
	    d0 = d1 >> 8;
	
	    m1 = 0xff << i;
	    m0 = m1 >> 8;
	
	    /* Clip depending on roll when falling out of the right of the screen */
	    if ((offset & 0x1f00) == 0x1f00) {
	        if ((starfire_vidctrl1 & 0x10) != 0)
	            m0 = 0;
	        else
		    m1 = 0;
	    }
	    v0 = starfire_videoram[offset];
	    v1 = starfire_videoram[offset2];
	
	    /* Handle ALU 8B,8D */
	    switch (starfire_vidctrl1 & 0x0F) {
	    case 0:
		v0 = (v0 & ~m0) | (d0 & m0);
		v1 = (v1 & ~m1) | (d1 & m1);
		break;
	    case 1:
		v0 = v0 | (d0 & m0);
		v1 = v1 | (d1 & m1);
		break;
	    case 2:
		v0 = (v0 ^ m0) | (d0 & m0);
		v1 = (v1 ^ m1) | (d1 & m1);
		break;
	    case 3:
		v0 = v0 | m0;
		v1 = v1 | m1;
		break;
	    case 4:
		v0 = v0 & (d0 | ~m0);
		v1 = v1 & (d1 | ~m1);
		break;
	    case 5:
		break;
	    case 6:
		v0 = v0 ^ (d0 & m0) ^ m0;
		v1 = v1 ^ (d1 & m1) ^ m1;
		break;
	    case 7:
		v0 = v0 | (~d0 & m0);
		v1 = v1 | (~d1 & m1);
		break;
	    case 8:
		v0 = (v0 & ~m0) | (~v0 & d0 & m0);
		v1 = (v1 & ~m1) | (~v1 & d1 & m1);
		break;
	    case 9:
		v0 = v0 ^ (d0 & m0);
		v1 = v1 ^ (d1 & m1);
		break;
	    case 10:
		v0 = v0 ^ m0;
		v1 = v1 ^ m1;
		break;
	    case 11:
		v0 = (v0 & ~m0) | (~(v0 & d0) & m0);
		v1 = (v1 & ~m1) | (~(v1 & d1) & m1);
		break;
	    case 12:
		v0 = v0 & ~m0;
		v1 = v1 & ~m1;
		break;
	    case 13:
		v0 = v0 & ~(d0 & m0);
		v1 = v1 & ~(d1 & m1);
		break;
	    case 14:
		v0 = (v0 & ~m0) | (~(v0 | d0) & m0);
		v1 = (v1 & ~m1) | (~(v1 | d1) & m1);
		break;
	    case 15:
		v0 = (v0 & ~m0) | (~d0 & m0);
		v1 = (v1 & ~m1) | (~d1 & m1);
		break;
	    }
	
	    starfire_videoram[offset] = v0;
	    starfire_videoram[offset2] = v1;
	
	    if (!source && !(starfire_vidctrl1 & 0x80)) {
		if (m0 != 0)
		    starfire_colorram[offset] = starfire_color;
		if (m1 != 0)
		    starfire_colorram[offset2] = starfire_color;
	    }
	} };
	
	public static ReadHandlerPtr starfire_videoram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
	    int i, m0, m1, d0;
	    unsigned char c;
	    int offset2 = (offset+0x100)&0x1fff;
	
	    /* Handle selector 6A */
	    if ((offset & 0x2000) != 0)
	        c = starfire_vidctrl;
	    else
	        c = starfire_vidctrl >> 4;
	
	    offset &= 0x1FFF;
	
	    /* Handle shifter mask 6E,6D */
	    i = 8 - ((c & 0x0E) >> 1);
	
	    m1 = 0xff << i;
	    m0 = m1 >> 8;
	
	    /* Clip depending on roll when falling out of the right of the screen */
	    if ((offset & 0x1f00) == 0x1f00) {
	        if ((starfire_vidctrl1 & 0x10) != 0)
	            m0 = 0;
	        else
		    m1 = 0;
	    }
	    d0 = (starfire_videoram[offset] & m0 ) | (starfire_videoram[offset2] & m1);
	
	    d0 = ((d0 >> i) | (d0 << (8-i))) & 0xff;
	
	    return d0;
	} };
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr starfire_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
	        int x,y,xx;
	        int j,col;
	        long pnt;
	        int d;
	
			palette_recalc();
	
	        pnt = 0x0000;
	        xx=0;
	
	        for (x=0; x<32; x++) {
	            for (y=0; y<256; y++) {
	                d= starfire_videoram[pnt];
	                col = starfire_colorram[pnt++];
	                for (j=0; j<8; j++) {
	                    if ((d & 0x80) != 0)
	                        tmpbitmap.line[y][xx+j] = Machine.pens[col+32];
	                    else
	                        tmpbitmap.line[y][xx+j] = Machine.pens[col];
	                    d = d << 1;
	                }
	            }
	            xx=xx+8;
		    pnt+=0x00;
	       }
	       copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	} };
	
	public static VhStartPtr starfire_vh_start = new VhStartPtr() { public int handler() 
	{
	    int i;
	
	    if ((tmpbitmap = osd_create_bitmap(Machine.drv.screen_width,Machine.drv.screen_height)) == 0)
	        return 1;
	
	    if ((starfire_videoram = malloc(0x2000)) == 0)
		{
			osd_free_bitmap(tmpbitmap);
			return 1;
		}
	    if ((starfire_colorram = malloc(0x2000)) == 0)
		{
			osd_free_bitmap(tmpbitmap);
	        free(starfire_videoram);
			return 1;
		}
	
	    for (i=0; i<0x2000; i++) {
	        starfire_videoram[i]=0;
	        starfire_colorram[i]=0;
	    }
	
	    return 0;
	} };
	
	public static VhStopPtr starfire_vh_stop = new VhStopPtr() { public void handler() 
	{
		osd_free_bitmap(tmpbitmap);
	    free(starfire_videoram);
	    free(starfire_colorram);
	} };
	
}
