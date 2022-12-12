/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"

#include "osdepend.h"

#define VRAM_HIGH 0x08000

//#define NOISY_VIDEO
/* Filter by video port */
//#define NOISE_FILTER (offset==0x0b)
/* Filter by video address (Filters out all slave COMMS) */
#define NOISE_FILTER (leland_video_addr[num] < 0x7800)

//#define NOISE_FILTER (leland_video_addr[num] >= 0x7800)


/* DAC */
//#define NOISE_FILTER ((leland_video_addr[num] & 0x7f) >= 0x50)

/* No filter */
//#define NOISE_FILTER


//#define VIDEO_FIDDLE


#define VIDEO_WIDTH  0x28
#define VIDEO_HEIGHT 0x1e

static int leland_video_addr[2];
static int leland_video_plane_count[2];
static int leland_vram_low_data[2];

/* Callback bank handler */
extern void (*leland_update_master_bank)(void);

/* Video RAM (internal to gfx chip ???) */
unsigned char *leland_video_ram;
const int leland_video_ram_size=0x10000;
static struct osd_bitmap *screen_bitmap;

/* Scroll background registers */
static int leland_bk_xlow;
static int leland_bk_xhigh;
static int leland_bk_ylow;
static int leland_bk_yhigh;

/* Data latches */
//static int leland_vram_high_data;


/* Background graphics */
static struct osd_bitmap *background_bitmap;

void leland_draw_bkchar(int x, int y, int ch)
{
    unsigned char *bm;
    unsigned char *sd;
    int ny;
    sd= Machine->gfx[0]->gfxdata + ch * Machine->gfx[0]->char_modulo;
    for (ny = 0;ny < 8;ny++)
    {
        bm=background_bitmap->line[8*y+ny] + 8*x;
        bm[0]=sd[0];
        bm[1]=sd[1];
        bm[2]=sd[2];
        bm[3]=sd[3];
        bm[4]=sd[4];
        bm[5]=sd[5];
        bm[6]=sd[6];
        bm[7]=sd[7];
        sd+=8;
    }
}

/* Graphics control register (driven by AY8910 port A) */
static int leland_gfx_control;
int leland_sound_port_r(int offset)
{
    return leland_gfx_control;
}

extern void leland_sound_port_w(int offset, int data)
{
    leland_gfx_control=data;
    if (leland_update_master_bank)
    {
        /* Update master bank */
        leland_update_master_bank();
    }
}

/* Scroll ports */
void leland_bk_xlow_w(int offset, int data)
{
        leland_bk_xlow=data;
}

void leland_bk_xhigh_w(int offset, int data)
{
        leland_bk_xhigh=data;
}

void leland_bk_ylow_w(int offset, int data)
{
        leland_bk_ylow=data;
}

void leland_bk_yhigh_w(int offset, int data)
{
        leland_bk_yhigh=data;
}

void leland_gfx_port_w(int offset, int data)
{
    switch (offset)
    {
        case 0: /* 0x?a */
                AY8910_control_port_0_w(0, data);
                break;
        case 1: /* 0x?b */
                AY8910_write_port_0_w(0, data);
                break;
        case 2: /* 0x?c */
                leland_bk_xlow_w(0, data);
                break;
        case 3: /* 0x?d */
                leland_bk_xhigh_w(0,data);
                break;
        case 4: /* 0x?e */
                leland_bk_ylow_w(0,data);
                break;
        case 5: /* 0x?f */
                leland_bk_yhigh_w(0, data);
                break;
        default:
            if (errorlog)
            {
                fprintf(errorlog, "CPU #%d %04x Warning: Unknown video port %02x write (%02x)\n",
                        cpu_getactivecpu(),cpu_get_pc(), offset,
                        data);
            }
    }
}


/* Word video address */

void leland_video_addr_w(int offset, int data, int num)
{
	if (!offset)
    {
        leland_video_addr[num]=(leland_video_addr[num]&0xff00)|data;
    }
	else
	{
        leland_video_addr[num]=(leland_video_addr[num]&0x00ff)|(data<<8);
	}

    /* Mask out irrelevant high bit */
    leland_video_addr[num]&=0x7fff;

    /* Reset the video plane count */
    leland_video_plane_count[num]=0;
}


int leland_vram_low_r(int num)
{
    int addr=leland_video_addr[num];
    return leland_video_ram[addr];
}

int leland_vram_high_r(int num)
{
    int addr=leland_video_addr[num];
    return leland_video_ram[addr+VRAM_HIGH];
}

void leland_vram_low_w(int data, int num)
{
    int addr=leland_video_addr[num];
    leland_video_ram[addr]=data;
    leland_vram_low_data[num]=data;  /* Set data latch */
}

void leland_vram_high_w(int data, int num)
{
    int addr=leland_video_addr[num];
    leland_video_ram[addr+VRAM_HIGH]=data;
}

void leland_vram_all_w(int data, int num)
{
    int addr=leland_video_addr[num];
    leland_video_ram[addr+0x00000]=data;
    leland_video_ram[addr+0x00001]=data;
    leland_video_ram[addr+VRAM_HIGH]=data;
    leland_video_ram[addr+VRAM_HIGH+1]=data;
}

void leland_vram_fill_w(int data, int num)
{
    int i;
    for (i=leland_video_addr[num]&0x7f; i<0x80; i++)
    {
        leland_vram_high_w(data, num);
        leland_vram_low_w(data, num);
        leland_video_addr[num]++;
    }
}

void leland_vram_planar_w(int data, int num)
{
    int addr=leland_video_addr[num];
    addr+=(leland_video_plane_count[num]&0x01)*VRAM_HIGH;
    addr+=(leland_video_plane_count[num])>>1;
    leland_video_ram[addr]=data;
    leland_video_plane_count[num]++;
/*
    if (errorlog)
    {
        fprintf(errorlog, "WRITE [%04x]=%02x\n", addr, data);
    }
 */

}

void leland_vram_planar_masked_w(int data, int num)
{
    int addr=leland_video_addr[num];
    addr+=(leland_video_plane_count[num]&0x01)*VRAM_HIGH;
    addr+=(leland_video_plane_count[num])>>1;

#if 0
    /*
    Used to draw sprites with transparency. Some self test
    text does not draw correctly if this is enabled.
    */
    if (data&0xf0)
    {
        leland_video_ram[addr] &= 0x0f;
    }
    if (data&0x0f)
    {
        leland_video_ram[addr] &= 0xf0;
    }
    leland_video_ram[addr]|=data;
#else
    leland_video_ram[addr]=data;
#endif

    leland_video_plane_count[num]++;
}


int leland_vram_planar_r(int num)
{
    int ret;
    int addr=leland_video_addr[num];
    addr+=(leland_video_plane_count[num]&0x01)*VRAM_HIGH;
    addr+=(leland_video_plane_count[num])>>1;
    ret= leland_video_ram[addr];
    leland_video_plane_count[num]++;

/*    if (errorlog)
    {
        fprintf(errorlog, "READ [%04x] (%02x)\n", addr, ret);
    }
 */
    return ret;
}

int leland_vram_plane_r(int num)
{
    int ret;
    int addr=leland_video_addr[num];
    addr+=(leland_video_plane_count[num]&0x01)*VRAM_HIGH;
    addr+=(leland_video_plane_count[num])>>1;
    ret= leland_video_ram[addr];
    return ret;
}


int leland_vram_port_r(int offset, int num)
{
    int ret;

    switch (offset)
    {
        case 0x03:
            ret=leland_vram_plane_r(num);
            leland_video_plane_count[num]=0;
            break;

        case 0x05:
            ret=leland_vram_high_r(num);
            break;

        case 0x06:
            ret=leland_vram_low_r(num);
            break;

        case 0x0b:
            ret=leland_vram_planar_r(num);
            break;

        case 0x0d:
            ret=leland_vram_high_r(num);
            leland_video_addr[num]++;
            break;

        case 0x0e:
            ret=leland_vram_low_r(num);
            leland_video_addr[num]++;
            break;

        default:
            if (errorlog)
            {
                fprintf(errorlog, "CPU #%d %04x Warning: Unknown video port %02x read (address=%04x)\n",
                        cpu_getactivecpu(),cpu_get_pc(), offset,
                        leland_video_addr[num]);
            }
            return 0;
    }

#ifdef NOISY_VIDEO
    if (errorlog && NOISE_FILTER)
    {
        fprintf(errorlog, "CPU #%d %04x video port %02x read (address=%04x value=%02x)\n",
                   cpu_getactivecpu(),cpu_get_pc(), offset,
                   leland_video_addr[num], ret);
    }
#endif

    return ret;
}

void leland_vram_port_w(int offset, int data, int num)
{
    switch (offset)
    {
        case 0x01:
            leland_vram_fill_w(data, num);
            break;

        case 0x05:
            leland_vram_high_w(data, num);
            break;

        case 0x06:
            leland_vram_low_w(data, num);
            break;

        case 0x09:
            leland_vram_high_w(data, num);
            leland_vram_low_w(leland_vram_low_data[num], num);
            leland_video_addr[num]++;
            break;

        case 0x0a:
            leland_vram_low_w(data, num);
            leland_vram_high_w(data, num);
            leland_video_addr[num]++;
            break;

        case 0x0b:
            leland_vram_planar_w(data,num);
            break;

        case 0x0e: /* Low write with latch */
            leland_vram_low_w(data, num);
            leland_video_addr[num]++;
            break;

        case 0x0d: /* High write with latch */
            leland_vram_high_w(data, num);
            leland_video_addr[num]++;
            break;

        case 0x15:
            /* Used by Alley master to draw the pins knocked over display */
            leland_vram_high_w(data, num);
            break;

        case 0x16:
            /* Used by Alley master to draw the pins knocked over display */
            leland_vram_low_w(data, num);
            break;

        case 0x1b:
            leland_vram_planar_masked_w(data, num);
            break;

        default:
            if (errorlog)
            {
                fprintf(errorlog, "CPU #%d %04x Warning: Unknown video port %02x write (address=%04x value=%02x)\n",
                        cpu_getactivecpu(),cpu_get_pc(), offset,
                        leland_video_addr[num], data);
            }
            return;
    }


#ifdef NOISY_VIDEO
    if (errorlog && NOISE_FILTER)
    {
        fprintf(errorlog, "CPU #%d %04x video port %02x write (address=%04x value=%02x)\n",
                   cpu_getactivecpu(),cpu_get_pc(), offset,
                   leland_video_addr[num], data);
    }
#endif

}


void leland_mvram_port_w(int offset, int data)
{
    leland_vram_port_w(offset, data, 0);
}

void leland_svram_port_w(int offset, int data)
{
    leland_vram_port_w(offset, data, 1);
}

int  leland_mvram_port_r(int offset)
{
    return leland_vram_port_r(offset, 0);
}

int  leland_svram_port_r(int offset)
{
    return leland_vram_port_r(offset, 1);
}

void leland_master_video_addr_w(int offset, int data)
{
    leland_video_addr_w(offset, data, 0);
}

void leland_slave_video_addr_w(int offset, int data)
{
    leland_video_addr_w(offset, data, 1);
}

/***************************************************************************

  Start the video hardware emulation.

***************************************************************************/

int leland_vh_start(void)
{
    screen_bitmap = osd_create_bitmap(VIDEO_WIDTH*8, VIDEO_HEIGHT*8);
	if (!screen_bitmap)
	{
		return 1;
	}
    fillbitmap(screen_bitmap,palette_transparent_pen,NULL);

    leland_video_ram=malloc(leland_video_ram_size);
    if (!leland_video_ram)
	{
		return 1;
	}
    memset(leland_video_ram, 0, leland_video_ram_size);

	return 0;
}



/***************************************************************************

  Stop the video hardware emulation.

***************************************************************************/
void leland_vh_stop(void)
{
	if (screen_bitmap)
	{
		osd_free_bitmap(screen_bitmap);
	}
    if (leland_video_ram)
	{
        free(leland_video_ram);
	}
}


/***************************************************************************

    Merge background and foreground bitmaps.

***************************************************************************/

void leland_draw_bitmap(
    struct osd_bitmap *bitmap,
    struct osd_bitmap *bkbitmap,
    int scrollx,
    int scrolly)
{
    int x,y, offs, data, j, value;
    /* Render the bitmap. */
    for (y=0; y<VIDEO_HEIGHT*8; y++)
    {
        for (x=0; x<VIDEO_WIDTH; x++)
        {
            int r=0;
            int bitmapvalue, bkvalue;
            offs=y*0x80+x*2;

            data =((unsigned long)leland_video_ram[0x00000+offs])<<(3*8);
            data|=((unsigned long)leland_video_ram[VRAM_HIGH+offs])<<(2*8);
            data|=((unsigned long)leland_video_ram[0x00001+offs])<<(1*8);
            data|=((unsigned long)leland_video_ram[VRAM_HIGH+1+offs]);

            for (j=32-4; j>=0; j-=4)
            {
                value=(data>>j)&0x0f;
                if (bkbitmap)
                {
                    /*
                    Merge the two bitmaps. The palette index is obtained
                    by combining the foreground and background graphics.
                    TODO:
                    - Support scrolling
                    */
                    if (Machine->orientation & ORIENTATION_SWAP_XY)
                    {
                        int line=VIDEO_WIDTH*8-1-(x*8+r);
                        bkvalue=bkbitmap->line[line+scrollx][y+scrolly];
                        bitmapvalue=Machine->pens[value*0x40+bkvalue];
                        screen_bitmap->line[line][y]=bitmapvalue;
                    }
                    else
                    {
                        bkvalue=bkbitmap->line[y+scrolly][x*8+r+scrollx];
                        bitmapvalue=Machine->pens[value*0x40+bkvalue];
                        screen_bitmap->line[y][x*8+r]=bitmapvalue;
                    }
                }
                else
                {
                    /* Legacy code */
                    if (!value)
                    {
                        bitmapvalue=palette_transparent_pen;
                    }
                    else
                    {
                        bitmapvalue=Machine->pens[value*0x40];
                    }
                    if (Machine->orientation & ORIENTATION_SWAP_XY)
                    {
                        int line=VIDEO_WIDTH*8-1-(x*8+r);
                        screen_bitmap->line[line][y]=bitmapvalue;
                    }
                    else
                    {
                        screen_bitmap->line[y][x*8+r]=bitmapvalue;
                    }
                }
                r++;
            }
        }
    }
    copybitmap(bitmap,screen_bitmap,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_PEN,palette_transparent_pen);

#ifdef MAME_DEBUG
    if (keyboard_pressed(KEYCODE_F))
	{
		FILE *fp=fopen("VIDEOR.DMP", "w+b");
		if (fp)
		{
            fwrite(leland_video_ram, leland_video_ram_size, 1, fp);
			fclose(fp);
		}
	}
#endif
}


/***************************************************************************

    Shared video refresh funciton

***************************************************************************/

void leland_screenrefresh(struct osd_bitmap *bitmap,int full_refresh, int bkcharbank)
{
#define Z 1
	int x,y, offs, colour;
    int scrollx, scrolly, xfine, yfine;
    int bkprombank;
    int ypos;
    int sx;
    unsigned char *BKGND = memory_region(REGION_USER1);

    /* PROM high bank */
    bkprombank=((leland_gfx_control>>3)&0x01)*0x2000;

    scrollx=leland_bk_xlow+256*leland_bk_xhigh;
    xfine=scrollx&0x07;
    sx=scrollx/8;
    scrolly=leland_bk_ylow+(256*leland_bk_yhigh);
    yfine=scrolly&0x07;
    ypos=scrolly/8;

    /* Build the palette */
	palette_recalc();

    /*
    Render the background graphics
    TODO: rewrite this completely using the proper video combining technique.
    */
    for (y=0; y<VIDEO_HEIGHT+1; y++)
    {
                for (x=0; x<VIDEO_WIDTH+1; x++)
                {
                        int code;
                        offs =(ypos&0x1f)*0x100;
                        offs+=((ypos>>5) & 0x07)*0x4000;
                        offs+=bkprombank;
                        offs+=((sx+x)&0xff);
                        offs&=0x1ffff;
                        code=BKGND[offs];

                        code+=(((ypos>>6)&0x03)*0x100);
                        code+=bkcharbank;

                        colour=(code&0xe0)>>5;
                        drawgfx(bitmap,Machine->gfx[0],
                                        code,
                                        colour,
                                        0,0,
                                        8*x-xfine,8*y-yfine,
                                        &Machine->drv->visible_area,
                                        TRANSPARENCY_NONE ,0);
                }
                ypos++;
    }

    /* Merge the two bitmaps together */
    leland_draw_bitmap(bitmap, 0, 0, 0);
}

void leland_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
    int bkcharbank=((leland_gfx_control>>4)&0x01)*0x0400;
    leland_screenrefresh(bitmap, full_refresh, bkcharbank);
}

void pigout_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
    /*
    Some games have more background graphics and employ an
    extra bank bit. These are the games with 0x8000 sized character
    sets.
    */
    int bkcharbank=((leland_gfx_control>>4)&0x03)*0x0400;
    leland_screenrefresh(bitmap, full_refresh, bkcharbank);
}

/***************************************************************************

    SOUND

    2 Onboard DACs driven from the video RAM

***************************************************************************/

static INT16 *buffer1,*buffer2;
static int channel;
const int buffer_len = 256;

int leland_sh_start(const struct MachineSound *msound)
{
	int vol[2];

	/* allocate the buffers */
	buffer1 = malloc(sizeof(INT16)*buffer_len);
	buffer2 = malloc(sizeof(INT16)*buffer_len);
	if (buffer1 == 0 || buffer2 == 0)
	{
		free(buffer1);
		free(buffer2);
		return 1;
	}
	memset(buffer1,0,sizeof(INT16)*buffer_len);
	memset(buffer2,0,sizeof(INT16)*buffer_len);

	/* request a sound channel */
	vol[0] = vol[1] = 25;
	channel = mixer_allocate_channels(2,vol);
	mixer_set_name(channel+0,"streamed DAC #0");
	mixer_set_name(channel+1,"streamed DAC #1");
	return 0;
}

void leland_sh_stop (void)
{
	free(buffer1);
	free(buffer2);
	buffer1 = buffer2 = 0;
}

void leland_sh_update(void)
{
    /* 8MHZ 8 bit DAC sound stored in program ROMS */
    int dacpos;
    int dac1on, dac2on;
	INT16 *buf;

    if (Machine->sample_rate == 0) return;

    dac1on=(~leland_gfx_control)&0x01;
    dac2on=(~leland_gfx_control)&0x02;

    if (dac1on)
    {
		buf = buffer1;

        for (dacpos = 0x50;dacpos < 0x8000;dacpos += 0x80)
			*(buf++) = leland_video_ram[dacpos]<<7;
	}
	else memset(buffer1,0,sizeof(INT16)*buffer_len);

    if (dac2on)
    {
		buf = buffer2;

        for (dacpos = 0x50;dacpos < 0x8000;dacpos += 0x80)
			*(buf++) = leland_video_ram[dacpos+VRAM_HIGH]<<7;
	}
	else memset(buffer2,0,sizeof(INT16)*buffer_len);

	mixer_play_streamed_sample_16(channel+0,buffer1,sizeof(INT16)*buffer_len,buffer_len * Machine->drv->frames_per_second);
	mixer_play_streamed_sample_16(channel+1,buffer2,sizeof(INT16)*buffer_len,buffer_len * Machine->drv->frames_per_second);
}

/***************************************************************************

  Ataxx

  Different from other Leland games.
  1) 12 bit palette instead of 8 bit palette.
  2) RAM based background (called quadrant RAM in the self test)
  3) Background characters are 6 bits per pixel with no funny banking.

***************************************************************************/

unsigned char * ataxx_tram;         /* Temporay RAM (TRAM) */
int ataxx_tram_size;
const int ataxx_qram_size=0x8000;   /* 2 banks of 0x4000 */
unsigned char * ataxx_qram1;        /* Background RAM # 1 - chars */
unsigned char * ataxx_qram2;        /* Background RAM # 2 - attrib */
unsigned char * ataxx_qram1dirty;    /* Dirty characters */
unsigned char * ataxx_qram2dirty;    /* Dirty characters */

int ataxx_vram_port_r(int offset, int num)
{
    int ret;
    switch (offset)
    {
        case 0x06:
            ret=leland_vram_plane_r(num);
            break;

        case 0x07:
            ret=leland_vram_planar_r(num);
            break;

        case 0x0a:
            ret=leland_vram_high_r(num);
            break;

        case 0x0b:
            ret=leland_vram_high_r(num);
            leland_video_addr[num]++;
            break;

        case 0x0c:
            ret=leland_vram_low_r(num);
            break;

        default:
            if (errorlog)
            {
                fprintf(errorlog, "CPU #%d %04x Warning: Unknown video port %02x read (address=%04x)\n",
                        cpu_getactivecpu(),cpu_get_pc(), offset,
                        leland_video_addr[num]);
            }
            return 0;
    }

#ifdef NOISY_VIDEO
    if (errorlog && offset != 0x07)
    {
        fprintf(errorlog, "CPU #%d %04x video port %02x read (address=%04x value=%02x)\n",
                   cpu_getactivecpu(),cpu_get_pc(), offset,
                   leland_video_addr[num], ret);
    }
#endif

    return ret;
}

void ataxx_vram_port_w(int offset, int data, int num)
{
    switch (offset)
    {
        case 0x03:
            leland_vram_high_w(data, num);
            leland_vram_low_w(leland_vram_low_data[num], num);
            leland_video_addr[num]++;
            break;

        case 0x05:
            leland_vram_low_w(data, num);
            leland_vram_high_w(data, num);
            leland_video_addr[num]++;
            break;

        case 0x07:
            leland_vram_planar_w(data, num);
            break;

        case 0x0a:
            leland_vram_high_w(data, num);
            break;

        case 0x0b: /* High write with latch */
            leland_vram_high_w(data, num);
            leland_video_addr[num]++;
            break;

        case 0x0c:
            leland_vram_low_w(data, num);
            break;

        case 0x17:
            leland_vram_planar_masked_w(data, num);
            break;

        case 0x1b:
            // Not sure about this one
            leland_vram_planar_w(0, num);
            leland_vram_planar_w(data, num);
            break;

        default:
            if (errorlog)
            {
                fprintf(errorlog, "CPU #%d %04x Warning: Unknown video port %02x write (address=%04x value=%02x)\n",
                        cpu_getactivecpu(),cpu_get_pc(), offset,
                        leland_video_addr[num], data);
            }
            return;
    }

#ifdef NOISY_VIDEO
    if (errorlog && offset != 0x07)
    {
        fprintf(errorlog, "CPU #%d %04x video port %02x write (address=%04x value=%02x)\n",
                   cpu_getactivecpu(),cpu_get_pc(), offset,
                   leland_video_addr[num], data);
    }
#endif
}

void ataxx_mvram_port_w(int offset, int data)
{
    ataxx_vram_port_w(offset, data, 0);
}

void ataxx_svram_port_w(int offset, int data)
{
    ataxx_vram_port_w(offset, data, 1);
}

int  ataxx_mvram_port_r(int offset)
{
    return ataxx_vram_port_r(offset, 0);
}

int  ataxx_svram_port_r(int offset)
{
    return ataxx_vram_port_r(offset, 1);
}

int ataxx_vh_start(void)
{
    if (leland_vh_start())
    {
        return 1;
    }

    /*
    This background bitmap technique needs to be moved to
    the generic leland driver (when the bitmap renderer supports
    scrolling)
    */
    background_bitmap = osd_create_bitmap(0x800+0x100, 0x400+0x100);
    if (!background_bitmap)
	{
		return 1;
	}
    fillbitmap(background_bitmap,Machine->pens[0], NULL);

    ataxx_qram1=malloc(ataxx_qram_size);
    if (!ataxx_qram1)
    {
        return 1;
    }
    memset(ataxx_qram1, 0, ataxx_qram_size);

    ataxx_qram2=malloc(ataxx_qram_size);
    if (!ataxx_qram2)
    {
        return 1;
    }
    memset(ataxx_qram2, 0, ataxx_qram_size);

    ataxx_qram1dirty=malloc(ataxx_qram_size);
    if (!ataxx_qram1dirty)
    {
        return 1;
    }
    memset(ataxx_qram1dirty, 0xff, ataxx_qram_size);

    ataxx_qram2dirty=malloc(ataxx_qram_size);
    if (!ataxx_qram2dirty)
    {
        return 1;
    }
    memset(ataxx_qram2dirty, 0xff, ataxx_qram_size);

	return 0;
}

void ataxx_vh_stop(void)
{
    leland_vh_stop();

    if (background_bitmap)
    {
        osd_free_bitmap(background_bitmap);
    }
    if (ataxx_qram1)
	{
        free(ataxx_qram1);
	}
    if (ataxx_qram2)
	{
        free(ataxx_qram2);
	}
    if (ataxx_qram1dirty)
	{
        free(ataxx_qram1dirty);
	}
    if (ataxx_qram2dirty)
	{
        free(ataxx_qram2dirty);
	}

}

void ataxx_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
    int x, y, offs, scrollx, scrolly;

    /* Build the palette */
	palette_recalc();

    /* Scroll position */
    scrollx=(leland_bk_xlow+256*leland_bk_xhigh) & 0x7ff;
    scrolly=(leland_bk_ylow+256*leland_bk_yhigh) & 0x3ff;

    /* Build quadrants ( backgrounds ) */
    for (y=0; y<0x80; y++)
    {
        for (x=0; x<0x100; x++)
        {
            int code, attr;
            offs=(y*0x0100)+x;
            offs&=0x7fff;
            code=ataxx_qram1[offs];
            attr=ataxx_qram2[offs];
            if (
                code != ataxx_qram1dirty[offs] ||
                attr != ataxx_qram2dirty[offs]
               )
            {
                /*
                There are no palette issues with the dirty handling
                of the background RAM
                */
                ataxx_qram1dirty[offs]=code;
                ataxx_qram2dirty[offs]=attr;
                code+=(attr&0x3f)*256;
                leland_draw_bkchar(x, y, code);
            }
        }
    }
    /* Merge the foreground and background layers */
    leland_draw_bitmap(bitmap, background_bitmap, scrollx, scrolly);

#if 0
    if (keyboard_pressed(KEYCODE_B))
    {
        FILE *fp;
        fp=fopen("TRAM.DMP", "w+b");
        if (fp)
        {
            fwrite(ataxx_tram, ataxx_tram_size, 1, fp);
            fclose(fp);
        }
        fp=fopen("QRAM1.DMP", "w+b");
        if (fp)
        {
            fwrite(ataxx_qram1, ataxx_qram_size, 1, fp);
            fclose(fp);
        }
        fp=fopen("QRAM2.DMP", "w+b");
        if (fp)
        {
            fwrite(ataxx_qram2, ataxx_qram_size, 1, fp);
            fclose(fp);
        }
    }
#endif
}


