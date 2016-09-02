package arcadeflex;

import static arcadeflex.video.*;

public class blit {

    public static int[] palette = new int[256];
    public static char[] back_buffer;

    public static void set_color(int index, RGB entry) {
        int rgb = entry.r << 16 | entry.g << 8 | entry.b;
        palette[index] = rgb;
    }

    static void blitscreen_dirty1_vga() {
        int w, h;

        /* while (true)
            {*/
        for (int i = 0; i < scrbitmap.height; i++) {
            System.arraycopy(scrbitmap.line[i].memory, scrbitmap.line[i].offset, back_buffer, i * scrbitmap.width, scrbitmap.width);
        }

        {
            int sbi = scrbitmap.line[skiplines].offset + skipcolumns;
            sbi = 0;
            //w = drv.screen_width;// gfx_display_columns;
            //h = drv.screen_height;// gfx_display_lines;
            //w =  gfx_display_columns;
            //h =  gfx_display_lines;

            w = scrbitmap.width;
            h = scrbitmap.height;
            if (MainApplet.inst != null) {
                for (int y = 0; y < h; y++) {

                    for (int x = 0; x < w; x++) {
                        //blit_buffer[x + (y * w)] = palette[scrbitmap.line[skiplines].buffer[sbi + x + (y * w)]];
                        MainApplet.inst._pixels[x + (y * w)] = palette[back_buffer[sbi + x + (y * w)]];
                    }
                }
                MainApplet.inst.blit();

            } else if (MainStream.inst != null) {
                for (int y = 0; y < h; y++) {

                    for (int x = 0; x < w; x++) {
                        //blit_buffer[x + (y * w)] = palette[scrbitmap.line[skiplines].buffer[sbi + x + (y * w)]];
                        MainStream.inst._pixels[x + (y * w)] = palette[back_buffer[sbi + x + (y * w)]];
                    }
                }
                MainStream.inst.blit();

            } else {
                for (int y = 0; y < h; y++) {

                    for (int x = 0; x < w; x++) {
                        //blit_buffer[x + (y * w)] = palette[scrbitmap.line[skiplines].buffer[sbi + x + (y * w)]];
                        screen._pixels[x + (y * w)] = palette[back_buffer[sbi + x + (y * w)]];
                    }
                }
                screen.blit();
            }

            //}
        }
    }

    /*TODO*////* from video.c (required for 15.75KHz Arcade Monitor Modes) */
    /*TODO*///extern int half_yres;
    /*TODO*///extern int unchained;
    /*TODO*///
    /*TODO*///extern char *dirty_old;
    /*TODO*///extern char *dirty_new;
    /*TODO*///
    /*TODO*///extern struct osd_bitmap *scrbitmap;
    /*TODO*///extern int gfx_xoffset;
    /*TODO*///extern int gfx_yoffset;
    /*TODO*///extern int gfx_display_lines;
    /*TODO*///extern int gfx_display_columns;
    /*TODO*///extern int gfx_width;
    /*TODO*///extern int gfx_height;
    /*TODO*///extern int skiplines;
    /*TODO*///extern int skipcolumns;
    /*TODO*///extern int use_triplebuf;
    /*TODO*///extern int triplebuf_pos,triplebuf_page_width;
    /*TODO*///extern int mmxlfb;
    /*TODO*///
    /*TODO*///unsigned int doublepixel[256];
    /*TODO*///unsigned int quadpixel[256]; /* for quadring pixels */
    /*TODO*///
    public static /*UINT32* */ char[] palette_16bit_lookup;
    /*TODO*///
    /*TODO*///
    /*TODO*////* current 'page' for unchained modes */
    /*TODO*///static int xpage = -1;
    /*TODO*////*default page sizes for non-triple buffering */
    /*TODO*///int xpage_size = 0x8000;
    /*TODO*///int no_xpages = 2;
    /*TODO*///
    /*TODO*////* this function lifted from Allegro */
    /*TODO*///static int vesa_scroll_async(int x, int y)
    /*TODO*///{
    /*TODO*///   int ret;
    /*TODO*///	#define BYTES_PER_PIXEL(bpp)     (((int)(bpp) + 7) / 8)	/* in Allegro */
    /*TODO*///	extern __dpmi_regs _dpmi_reg;	/* in Allegro... I think */
    /*TODO*///
    /*TODO*/////   vesa_xscroll = x;
    /*TODO*/////   vesa_yscroll = y;
    /*TODO*///
    /*TODO*///#if 0
    /*TODO*///   int seg;
    /*TODO*///   long a;
    /*TODO*///	extern void (*_pm_vesa_scroller)(void);	/* in Allegro */
    /*TODO*///	extern int _mmio_segment;	/* in Allegro */
    /*TODO*///   if (_pm_vesa_scroller) {            /* use protected mode interface? */
    /*TODO*///      seg = _mmio_segment ? _mmio_segment : _my_ds();
    /*TODO*///
    /*TODO*///      a = ((x * BYTES_PER_PIXEL(screen->vtable->color_depth)) +
    /*TODO*///	   (y * ((unsigned long)screen->line[1] - (unsigned long)screen->line[0]))) / 4;
    /*TODO*///
    /*TODO*///      asm (
    /*TODO*///	 "  pushw %%es ; "
    /*TODO*///	 "  movw %w1, %%es ; "         /* set the IO segment */
    /*TODO*///	 "  call *%0 ; "               /* call the VESA function */
    /*TODO*///	 "  popw %%es "
    /*TODO*///
    /*TODO*///      :                                /* no outputs */
    /*TODO*///
    /*TODO*///      : "S" (_pm_vesa_scroller),       /* function pointer in esi */
    /*TODO*///	"a" (seg),                     /* IO segment in eax */
    /*TODO*///	"b" (0x00),                    /* mode in ebx */
    /*TODO*///	"c" (a & 0xFFFF),              /* low word of address in ecx */
    /*TODO*///	"d" (a >> 16)                  /* high word of address in edx */
    /*TODO*///
    /*TODO*/////      : "memory", "%edi", "%cc"        /* clobbers edi and flags */
    /*TODO*///		: "memory", "%ebp", "%edi", "%cc" /* clobbers ebp, edi and flags (at least) */
    /*TODO*///      );
    /*TODO*///
    /*TODO*///      ret = 0;
    /*TODO*///   }
    /*TODO*///   else
    /*TODO*///#endif
    /*TODO*///   {                              /* use a real mode interrupt call */
    /*TODO*///      _dpmi_reg.x.ax = 0x4F07;
    /*TODO*///      _dpmi_reg.x.bx = 0;
    /*TODO*///      _dpmi_reg.x.cx = x;
    /*TODO*///      _dpmi_reg.x.dx = y;
    /*TODO*///
    /*TODO*///      __dpmi_int(0x10, &_dpmi_reg);
    /*TODO*///      ret = _dpmi_reg.h.ah;
    /*TODO*///
    /*TODO*/////      _vsync_in();
    /*TODO*///   }
    /*TODO*///
    /*TODO*///   return (ret ? -1 : 0);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///void blitscreen_dirty1_vga(void)
    /*TODO*///{
    /*TODO*///	int width4, x, y;
    /*TODO*///	unsigned long *lb, address;
    /*TODO*///
    /*TODO*///	width4 = (scrbitmap->line[1] - scrbitmap->line[0]) / 4;
    /*TODO*///	address = 0xa0000 + gfx_xoffset + gfx_yoffset * gfx_width;
    /*TODO*///	lb = (unsigned long *)(scrbitmap->line[skiplines] + skipcolumns);
    /*TODO*///
    /*TODO*///	for (y = 0; y < gfx_display_lines; y += 16)
    /*TODO*///	{
    /*TODO*///		for (x = 0; x < gfx_display_columns; )
    /*TODO*///		{
    /*TODO*///			int w = 16;
    /*TODO*///			if (ISDIRTY(x,y))
    /*TODO*///			{
    /*TODO*///				unsigned long *lb0 = lb + x / 4;
    /*TODO*///				unsigned long address0 = address + x;
    /*TODO*///				int h;
    /*TODO*///				while (x + w < gfx_display_columns && ISDIRTY(x+w,y))
    /*TODO*///                    w += 16;
    /*TODO*///				if (x + w > gfx_display_columns)
    /*TODO*///                    w = gfx_display_columns - x;
    /*TODO*///				for (h = 0; h < 16 && y + h < gfx_display_lines; h++)
    /*TODO*///				{
    /*TODO*///					_dosmemputl(lb0, w/4, address0);
    /*TODO*///					lb0 += width4;
    /*TODO*///					address0 += gfx_width;
    /*TODO*///				}
    /*TODO*///			}
    /*TODO*///			x += w;
    /*TODO*///        }
    /*TODO*///		lb += 16 * width4;
    /*TODO*///		address += 16 * gfx_width;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///void blitscreen_dirty0_vga(void)
    /*TODO*///{
    /*TODO*///	int width4,y,columns4;
    /*TODO*///	unsigned long *lb, address;
    /*TODO*///
    /*TODO*///	width4 = (scrbitmap->line[1] - scrbitmap->line[0]) / 4;
    /*TODO*///	columns4 = gfx_display_columns/4;
    /*TODO*///	address = 0xa0000 + gfx_xoffset + gfx_yoffset * gfx_width;
    /*TODO*///	lb = (unsigned long *)(scrbitmap->line[skiplines] + skipcolumns);
    /*TODO*///	for (y = 0; y < gfx_display_lines; y++)
    /*TODO*///	{
    /*TODO*///		_dosmemputl(lb,columns4,address);
    /*TODO*///		lb+=width4;
    /*TODO*///		address+=gfx_width;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*////* for flipping between the unchained VGA pages */
    /*TODO*///INLINE void unchained_flip(void)
    /*TODO*///{
    /*TODO*///	int	flip_value1,flip_value2;
    /*TODO*///	int	temp;
    /*TODO*///
    /*TODO*////* memory address of non-visible page */
    /*TODO*///	temp = xpage_size * xpage;
    /*TODO*///
    /*TODO*///	flip_value1 = ((temp & 0xff00) | 0x0c);
    /*TODO*///	flip_value2 = (((temp << 8) & 0xff00) | 0x0d);
    /*TODO*///
    /*TODO*////* flip the page var */
    /*TODO*///	xpage ++;
    /*TODO*///	if (xpage == no_xpages)
    /*TODO*///		xpage = 0;
    /*TODO*////* need to change the offset address during the active display interval */
    /*TODO*////* as the value is read during the vertical retrace */
    /*TODO*///	__asm__ __volatile__ (
    /*TODO*///
    /*TODO*///	"movw   $0x3da,%%dx \n"
    /*TODO*///	"cli \n"
    /*TODO*///	".align 4                \n"
    /*TODO*////* check for active display interval */
    /*TODO*///	"0:\n"
    /*TODO*///	"inb    %%dx,%%al \n"
    /*TODO*///	"testb  $1,%%al \n"
    /*TODO*///	"jz  	0b \n"
    /*TODO*////* change the offset address */
    /*TODO*///	"movw   $0x3d4,%%dx \n"
    /*TODO*///	"movw   %%cx,%%ax \n"
    /*TODO*///	"outw   %%ax,%%dx \n"
    /*TODO*///	"movw   %%bx,%%ax \n"
    /*TODO*///	"outw   %%ax,%%dx \n"
    /*TODO*///	"sti \n"
    /*TODO*///
    /*TODO*///
    /*TODO*////* outputs  (none)*/
    /*TODO*///	:"=c" (flip_value1),
    /*TODO*///	"=b" (flip_value2)
    /*TODO*////* inputs -
    /*TODO*/// ecx = flip_value1 , ebx = flip_value2 */
    /*TODO*///	:"0" (flip_value1),
    /*TODO*///	"1" (flip_value2)
    /*TODO*////* registers modified */
    /*TODO*///	:"ax", "dx", "cc", "memory"
    /*TODO*///	);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*////* unchained dirty modes */
    /*TODO*///void blitscreen_dirty1_unchained_vga(void)
    /*TODO*///{
    /*TODO*///	int x, y, i, outval, dirty_page, triple_page, write_triple;
    /*TODO*///	int plane, planeval, iloop, page;
    /*TODO*///	unsigned long *lb, address, triple_address;
    /*TODO*///	unsigned char *lbsave;
    /*TODO*///	unsigned long asave, triple_save;
    /*TODO*///	static int width4, word_blit, dirty_height;
    /*TODO*///	static int source_width, source_line_width, dest_width, dest_line_width;
    /*TODO*///
    /*TODO*///	/* calculate our statics on the first call */
    /*TODO*///	if (xpage == -1)
    /*TODO*///	{
    /*TODO*///		width4 = ((scrbitmap->line[1] - scrbitmap->line[0]) >> 2);
    /*TODO*///		source_width = width4 << half_yres;
    /*TODO*///		dest_width = gfx_width >> 2;
    /*TODO*///		dest_line_width = (gfx_width << 2) >> half_yres;
    /*TODO*///		source_line_width = width4 << 4;
    /*TODO*///		xpage = 1;
    /*TODO*///		dirty_height = 16 >> half_yres;
    /*TODO*///		/* check if we have to do word rather than double word updates */
    /*TODO*///		word_blit = ((gfx_display_columns >> 2) & 3);
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* setup or selector */
    /*TODO*///	_farsetsel(screen->seg);
    /*TODO*///
    /*TODO*///	dirty_page = xpage;
    /*TODO*///	/* non visible page, if we're triple buffering */
    /*TODO*///	triple_page = xpage + 1;
    /*TODO*///	if (triple_page == no_xpages)
    /*TODO*///		triple_page = 0;
    /*TODO*///
    /*TODO*///	/* need to update all 'pages', but only update each page while it isn't visible */
    /*TODO*///	for (page = 0; page < 2; page ++)
    /*TODO*///	{
    /*TODO*///		planeval=0x0100;
    /*TODO*///		write_triple = (!page | (no_xpages == 3));
    /*TODO*///
    /*TODO*///		/* go through each bit plane */
    /*TODO*///		for (plane = 0; plane < 4 ;plane ++)
    /*TODO*///		{
    /*TODO*///			address = 0xa0000 + (xpage_size * dirty_page)+(gfx_xoffset >> 2) + (((gfx_yoffset >> half_yres) * gfx_width) >> 2);
    /*TODO*///			triple_address = 0xa0000 + (xpage_size * triple_page)+(gfx_xoffset >> 2) + (((gfx_yoffset >> half_yres) * gfx_width) >> 2);
    /*TODO*///			lb = (unsigned long *)(scrbitmap->line[skiplines] + skipcolumns + plane);
    /*TODO*///			/*set the bit plane */
    /*TODO*///			outportw(0x3c4, planeval|0x02);
    /*TODO*///			for (y = 0; y < gfx_display_lines ; y += 16)
    /*TODO*///			{
    /*TODO*///				for (x = 0; x < gfx_display_columns; )
    /*TODO*///				{
    /*TODO*///					int w = 16;
    /*TODO*///					if (ISDIRTY(x,y))
    /*TODO*///					{
    /*TODO*///						unsigned long *lb0 = lb + (x >> 2);
    /*TODO*///						unsigned long address0 = address + (x >> 2);
    /*TODO*///						unsigned long address1 = triple_address + (x >> 2);
    /*TODO*///						int h;
    /*TODO*///						while (x + w < gfx_display_columns && ISDIRTY(x+w,y))
    /*TODO*///							w += 16;
    /*TODO*///						if (x + w > gfx_display_columns)
    /*TODO*///							w = gfx_display_columns - x;
    /*TODO*///						if(word_blit)
    /*TODO*///						{
    /*TODO*///							iloop = w >> 3;
    /*TODO*///							for (h = 0; h < dirty_height && (y + (h << half_yres)) < gfx_display_lines; h++)
    /*TODO*///							{
    /*TODO*///								asave = address0;
    /*TODO*///								triple_save = address1;
    /*TODO*///								lbsave = (unsigned char *)lb0;
    /*TODO*///								for(i = 0; i < iloop; i++)
    /*TODO*///								{
    /*TODO*///									outval = *lbsave | (lbsave[4] << 8);
    /*TODO*///									_farnspokew(asave, outval);
    /*TODO*///									/* write 2 pages on first pass if triple buffering */
    /*TODO*///									if (write_triple)
    /*TODO*///									{
    /*TODO*///										_farnspokew(triple_save, outval);
    /*TODO*///										triple_save += 2;
    /*TODO*///									}
    /*TODO*///									lbsave += 8;
    /*TODO*///									asave += 2;
    /*TODO*///								}
    /*TODO*///								if (w&4)
    /*TODO*///								{
    /*TODO*///									_farnspokeb(asave, *lbsave);
    /*TODO*///		   							if (write_triple)
    /*TODO*///										_farnspokeb(triple_save, *lbsave);
    /*TODO*///								}
    /*TODO*///
    /*TODO*///
    /*TODO*///								lb0 += source_width;
    /*TODO*///								address0 += dest_width;
    /*TODO*///								address1 += dest_width;
    /*TODO*///							}
    /*TODO*///						}
    /*TODO*///						else
    /*TODO*///						{
    /*TODO*///							iloop = w >> 4;
    /*TODO*///							for (h = 0; h < dirty_height && (y + (h << half_yres)) < gfx_display_lines; h++)
    /*TODO*///							{
    /*TODO*///								asave = address0;
    /*TODO*///								triple_save = address1;
    /*TODO*///								lbsave = (unsigned char *)lb0;
    /*TODO*///								for(i = 0; i < iloop; i++)
    /*TODO*///								{
    /*TODO*///									outval = *lbsave | (lbsave[4] << 8) | (lbsave[8] << 16) | (lbsave[12] << 24);
    /*TODO*///									_farnspokel(asave, outval);
    /*TODO*///									/* write 2 pages on first pass if triple buffering */
    /*TODO*///									if (page == 0 && no_xpages == 3)
    /*TODO*///									{
    /*TODO*///										_farnspokel(triple_save, outval);
    /*TODO*///										triple_save += 4;
    /*TODO*///									}
    /*TODO*///									lbsave += 16;
    /*TODO*///									asave += 4;
    /*TODO*///								}
    /*TODO*///								lb0 += source_width;
    /*TODO*///								address0 += dest_width;
    /*TODO*///								address1 += dest_width;
    /*TODO*///							}
    /*TODO*///						}
    /*TODO*///					}
    /*TODO*///					x += w;
    /*TODO*///				}
    /*TODO*///				lb += source_line_width;
    /*TODO*///				address += dest_line_width;
    /*TODO*///				triple_address += dest_line_width;
    /*TODO*///			}
    /*TODO*///			/* move onto the next bit plane */
    /*TODO*///			planeval <<= 1;
    /*TODO*///		}
    /*TODO*///		/* 'bank switch' our unchained output on the first loop */
    /*TODO*///		if(!page)
    /*TODO*///			unchained_flip();
    /*TODO*///		/* move onto next 'page' */
    /*TODO*///		dirty_page += (no_xpages - 1);
    /*TODO*///		if (dirty_page >= no_xpages)
    /*TODO*///			dirty_page -= no_xpages;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///void init_unchained_blit(void)
    /*TODO*///{
    /*TODO*///    xpage=-1;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* Macros for non dirty unchained blits */
    /*TODO*///#define UNCHAIN_BLIT_START \
    /*TODO*///	int dummy1,dummy2,dummy3; \
    /*TODO*///	__asm__ __volatile__ ( \
    /*TODO*////* save es and set it to our video selector */ \
    /*TODO*///	"pushw  %%es \n" \
    /*TODO*///	"movw   %%dx,%%es \n" \
    /*TODO*///	"movw   $0x102,%%ax \n" \
    /*TODO*///	"cld \n" \
    /*TODO*///	".align 4 \n" \
    /*TODO*////* --bit plane loop-- */ \
    /*TODO*///	"0:\n" \
    /*TODO*////* save everything */ \
    /*TODO*///	"pushl  %%ebx \n" \
    /*TODO*///	"pushl  %%edi \n" \
    /*TODO*///	"pushl  %%eax \n" \
    /*TODO*///	"pushl  %%ecx \n" \
    /*TODO*////* set the bit plane */ \
    /*TODO*///	"movw   $0x3c4,%%dx \n" \
    /*TODO*///	"outw   %%ax,%%dx \n" \
    /*TODO*////* edx now free, so use it for the memwidth */ \
    /*TODO*///	"movl   %4,%%edx \n" \
    /*TODO*////* --height loop-- */ \
    /*TODO*///	"1:\n" \
    /*TODO*////* save counter , source + dest address */ \
    /*TODO*///	"pushl 	%%ecx \n" \
    /*TODO*///	"pushl	%%ebx \n" \
    /*TODO*///	"pushl	%%edi \n" \
    /*TODO*////* --width loop-- */ \
    /*TODO*///	"movl   %3,%%ecx \n" \
    /*TODO*///	"2:\n" \
    /*TODO*////* get the 4 bytes */ \
    /*TODO*////* bswap should be faster than shift, */ \
    /*TODO*////* movl should be faster then movb */ \
    /*TODO*///	"movl   %%ds:12(%%ebx),%%eax \n" \
    /*TODO*///	"movb   %%ds:8(%%ebx),%%ah \n" \
    /*TODO*///	"bswap  %%eax \n" \
    /*TODO*///	"movb   %%ds:(%%ebx),%%al \n" \
    /*TODO*///	"movb   %%ds:4(%%ebx),%%ah \n" \
    /*TODO*////* write the thing to video */ \
    /*TODO*///	"stosl \n" \
    /*TODO*////* move onto next source address */ \
    /*TODO*///	"addl   $16,%%ebx \n" \
    /*TODO*///	"loop	2b \n"
    /*TODO*///
    /*TODO*///#define UNCHAIN_BLIT_END \
    /*TODO*////* --end of width loop-- */ \
    /*TODO*////* get counter, source + dest address back */ \
    /*TODO*///	"popl	%%edi \n" \
    /*TODO*///	"popl	%%ebx \n" \
    /*TODO*///	"popl   %%ecx \n" \
    /*TODO*////* move to the next line */ \
    /*TODO*///	"addl   %%edx,%%ebx \n" \
    /*TODO*///	"addl   %%esi,%%edi \n" \
    /*TODO*///	"loop   1b \n" \
    /*TODO*////* --end of height loop-- */ \
    /*TODO*////* get everything back */ \
    /*TODO*///	"popl   %%ecx \n" \
    /*TODO*///	"popl   %%eax \n"  \
    /*TODO*///	"popl   %%edi \n" \
    /*TODO*///	"popl   %%ebx \n" \
    /*TODO*////* move onto next bit plane */ \
    /*TODO*///	"incl   %%ebx \n" \
    /*TODO*///	"shlb   $1,%%ah \n" \
    /*TODO*////* check if we've done all 4 or not */ \
    /*TODO*///	"testb  $0x10,%%ah \n" \
    /*TODO*///	"jz		0b \n" \
    /*TODO*////* --end of bit plane loop-- */ \
    /*TODO*////* restore es */ \
    /*TODO*///	"popw   %%es \n" \
    /*TODO*////* outputs (aka clobbered input) */ \
    /*TODO*///	:"=S" (dummy1), \
    /*TODO*///	"=b" (dummy2), \
    /*TODO*///	"=d" (dummy3) \
    /*TODO*////* inputs */ \
    /*TODO*////* %0=width, %1=memwidth, */ \
    /*TODO*////* esi = scrwidth */ \
    /*TODO*////* ebx = src, ecx = height */ \
    /*TODO*////* edx = seg, edi = address */ \
    /*TODO*///	:"g" (width), \
    /*TODO*///	"g" (memwidth), \
    /*TODO*///	"c" (height), \
    /*TODO*///	"D" (address), \
    /*TODO*///	"0" (scrwidth), \
    /*TODO*///	"1" (src), \
    /*TODO*///	"2" (seg) \
    /*TODO*////* registers modified */ \
    /*TODO*///	:"ax", "cc", "memory" \
    /*TODO*///	);
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*////*asm routine for unchained blit - writes 4 bytes at a time */
    /*TODO*///INLINE void unchain_dword_blit(unsigned long *src,short seg,unsigned long address,int width,int height,int memwidth,int scrwidth)
    /*TODO*///{
    /*TODO*////* straight forward double word blit */
    /*TODO*///	UNCHAIN_BLIT_START
    /*TODO*///	UNCHAIN_BLIT_END
    /*TODO*///}
    /*TODO*///
    /*TODO*////*asm routine for unchained blit - writes 4 bytes at a time, then 2 'odd' bytes at the end of each row */
    /*TODO*///INLINE void unchain_word_blit(unsigned long *src,short seg,unsigned long address,int width,int height,int memwidth,int scrwidth)
    /*TODO*///{
    /*TODO*///	UNCHAIN_BLIT_START
    /*TODO*////* get the extra 2 bytes at end of the row */
    /*TODO*///	"movl   %%ds:(%%ebx),%%eax \n"
    /*TODO*///	"movb   %%ds:4(%%ebx),%%ah \n"
    /*TODO*////* write the thing to video */
    /*TODO*///	"stosw \n"
    /*TODO*///	UNCHAIN_BLIT_END
    /*TODO*///}
    /*TODO*///
    /*TODO*////*asm routine for unchained blit - writes 4 bytes at a time, then 1 'odd' byte at the end of each row */
    /*TODO*///INLINE void unchain_byte_blit(unsigned long *src,short seg,unsigned long address,int width,int height,int memwidth,int scrwidth)
    /*TODO*///{
    /*TODO*///	UNCHAIN_BLIT_START
    /*TODO*////* get the extra byte at end of the row */
    /*TODO*///	"movb   %%ds:(%%ebx),%%eax \n"
    /*TODO*////* write the thing to video */
    /*TODO*///	"stosb \n"
    /*TODO*///	UNCHAIN_BLIT_END
    /*TODO*///}
    /*TODO*///
    /*TODO*////* unchained 'non-dirty' modes */
    /*TODO*///void blitscreen_dirty0_unchained_vga(void)
    /*TODO*///{
    /*TODO*///	unsigned long *lb, address;
    /*TODO*///	static int width4,columns4,column_chained,memwidth,scrwidth,disp_height,blit_type;
    /*TODO*///
    /*TODO*///   /* only calculate our statics the first time around */
    /*TODO*///	if(xpage==-1)
    /*TODO*///	{
    /*TODO*///		width4 = (scrbitmap->line[1] - scrbitmap->line[0]) >> 2;
    /*TODO*///		columns4 = gfx_display_columns >> 2;
    /*TODO*///		disp_height = gfx_display_lines >> half_yres;
    /*TODO*///
    /*TODO*///		xpage = 1;
    /*TODO*///		memwidth = (scrbitmap->line[1] - scrbitmap->line[0]) << half_yres;
    /*TODO*///		scrwidth = gfx_width >> 2;
    /*TODO*///
    /*TODO*///		/* check for 'not divisible by 8' */
    /*TODO*///		if((columns4 & 1))
    /*TODO*///			blit_type = 2;    /* byte blit */
    /*TODO*///		else
    /*TODO*///		{
    /*TODO*///			if((columns4 & 2))
    /*TODO*///				blit_type = 1; /* word blit */
    /*TODO*///			else
    /*TODO*///				blit_type = 0; /* double word blit */
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		column_chained = columns4 >> 2;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* get the start of the screen bitmap */
    /*TODO*///	lb = (unsigned long *)(scrbitmap->line[skiplines] + skipcolumns);
    /*TODO*///	/* and the start address in video memory */
    /*TODO*///	address = 0xa0000 + (xpage_size * xpage)+(gfx_xoffset >> 2) + (((gfx_yoffset >> half_yres) * gfx_width) >> 2);
    /*TODO*///	/* call the appropriate blit routine */
    /*TODO*///	switch (blit_type)
    /*TODO*///	{
    /*TODO*///		case 0: /* double word blit */
    /*TODO*///			unchain_dword_blit (lb, screen->seg, address, column_chained, disp_height, memwidth, scrwidth);
    /*TODO*///			break;
    /*TODO*///		case 1: /* word blit */
    /*TODO*///			unchain_word_blit (lb, screen->seg, address, column_chained, disp_height, memwidth, scrwidth);
    /*TODO*///			break;
    /*TODO*///		case 2: /* byte blit */
    /*TODO*///			unchain_byte_blit (lb, screen->seg, address, column_chained, disp_height, memwidth, scrwidth);
    /*TODO*///			break;
    /*TODO*///	}
    /*TODO*///	/* 'bank switch' our unchained output */
    /*TODO*///	unchained_flip();
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*////* setup register array to be unchained */
    /*TODO*///void unchain_vga(Register *pReg)
    /*TODO*///{
    /*TODO*////* setup registers for an unchained mode */
    /*TODO*///	pReg[UNDERLINE_LOC_INDEX].value = 0x00;
    /*TODO*///	pReg[MODE_CONTROL_INDEX].value = 0xe3;
    /*TODO*///	pReg[MEMORY_MODE_INDEX].value = 0x06;
    /*TODO*////* flag the fact it's unchained */
    /*TODO*///	unchained = 1;
    /*TODO*///}
    /*TODO*///
    /*TODO*///INLINE void copyline_1x_8bpp(unsigned char *src,short seg,unsigned long address,int width4)
    /*TODO*///{
    /*TODO*///	short src_seg;
    /*TODO*///
    /*TODO*///	src_seg = _my_ds();
    /*TODO*///
    /*TODO*///	_movedatal(src_seg,(unsigned long)src,seg,address,width4);
    /*TODO*///}
    /*TODO*///
    /*TODO*///INLINE void copyline_1x_16bpp(unsigned char *src,short seg,unsigned long address,int width2)
    /*TODO*///{
    /*TODO*///	short src_seg;
    /*TODO*///
    /*TODO*///	src_seg = _my_ds();
    /*TODO*///
    /*TODO*///	_movedatal(src_seg,(unsigned long)src,seg,address,width2);
    /*TODO*///}
    /*TODO*///
    /*TODO*///#if 1 /* use the C approach instead */
    /*TODO*///INLINE void copyline_2x_8bpp(unsigned char *src,short seg,unsigned long address,int width4)
    /*TODO*///{
    /*TODO*///	__asm__ __volatile__ (
    /*TODO*///	"pushw %%es              \n"
    /*TODO*///	"movw %%dx, %%es         \n"
    /*TODO*///	"cld                     \n"
    /*TODO*///	".align 4                \n"
    /*TODO*///	"0:                      \n"
    /*TODO*///	"lodsl                   \n"
    /*TODO*///	"movl %%eax, %%ebx       \n"
    /*TODO*///	"bswap %%eax             \n"
    /*TODO*///	"xchgw %%ax,%%bx         \n"
    /*TODO*///	"roll $8, %%eax          \n"
    /*TODO*///	"stosl                   \n"
    /*TODO*///	"movl %%ebx, %%eax       \n"
    /*TODO*///	"rorl $8, %%eax          \n"
    /*TODO*///	"stosl                   \n"
    /*TODO*///	"loop 0b                 \n"
    /*TODO*///	"popw %%ax               \n"
    /*TODO*///	"movw %%ax, %%es         \n"
    /*TODO*///	:
    /*TODO*///	"=c" (width4),
    /*TODO*///	"=d" (seg),
    /*TODO*///	"=S" (src),
    /*TODO*///	"=D" (address)
    /*TODO*///	:
    /*TODO*///	"0" (width4),
    /*TODO*///	"1" (seg),
    /*TODO*///	"2" (src),
    /*TODO*///	"3" (address):
    /*TODO*///	"ax", "bx", "cc", "memory");
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///INLINE void copyline_2x_8bpp(unsigned char *src,short seg,unsigned long address,int width4)
    /*TODO*///{
    /*TODO*///	int i;
    /*TODO*///
    /*TODO*///	/* set up selector */
    /*TODO*///	_farsetsel(seg);
    /*TODO*///
    /*TODO*///	for (i = width; i > 0; i--)
    /*TODO*///	{
    /*TODO*///		_farnspokel (address, doublepixel[*src] | (doublepixel[*(src+1)] << 16));
    /*TODO*///		_farnspokel (address+4, doublepixel[*(src+2)] | (doublepixel[*(src+3)] << 16));
    /*TODO*///		address+=8;
    /*TODO*///		src+=4;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///#endif
    /*TODO*///
    /*TODO*///INLINE void copyline_2x_16bpp(unsigned char *src,short seg,unsigned long address,int width2)
    /*TODO*///{
    /*TODO*///	__asm__ __volatile__ (
    /*TODO*///	"pushw %%es              \n"
    /*TODO*///	"movw %%dx, %%es         \n"
    /*TODO*///	"cld                     \n"
    /*TODO*///	".align 4                \n"
    /*TODO*///	"0:                      \n"
    /*TODO*///	"lodsl                   \n"
    /*TODO*///	"movl %%eax, %%ebx       \n"
    /*TODO*///	"roll $16, %%eax         \n"
    /*TODO*///	"xchgw %%ax,%%bx         \n"
    /*TODO*///	"stosl                   \n"
    /*TODO*///	"movl %%ebx, %%eax       \n"
    /*TODO*///	"stosl                   \n"
    /*TODO*///	"loop 0b                 \n"
    /*TODO*///	"popw %%ax               \n"
    /*TODO*///	"movw %%ax, %%es         \n"
    /*TODO*///	:
    /*TODO*///	"=c" (width2),
    /*TODO*///	"=d" (seg),
    /*TODO*///	"=S" (src),
    /*TODO*///	"=D" (address)
    /*TODO*///	:
    /*TODO*///	"0" (width2),
    /*TODO*///	"1" (seg),
    /*TODO*///	"2" (src),
    /*TODO*///	"3" (address):
    /*TODO*///	"ax", "bx", "cc", "memory");
    /*TODO*///}
    /*TODO*///
    /*TODO*///INLINE void copyline_3x_16bpp(unsigned char *src,short seg,unsigned long address,int width2)
    /*TODO*///{
    /*TODO*///	__asm__ __volatile__ (
    /*TODO*///	"pushw %%es              \n"
    /*TODO*///	"movw %%dx, %%es         \n"
    /*TODO*///	"cld                     \n"
    /*TODO*///	".align 4                \n"
    /*TODO*///	"0:                      \n"
    /*TODO*///	"lodsl                   \n"
    /*TODO*///	"movl %%eax, %%ebx       \n"
    /*TODO*///	"roll $16, %%eax         \n"
    /*TODO*///	"xchgw %%ax,%%bx         \n"
    /*TODO*///	"stosl                   \n"
    /*TODO*///	"stosw                   \n"
    /*TODO*///	"movl %%ebx, %%eax       \n"
    /*TODO*///	"stosl                   \n"
    /*TODO*///	"stosw                   \n"
    /*TODO*///	"loop 0b                 \n"
    /*TODO*///	"popw %%ax               \n"
    /*TODO*///	"movw %%ax, %%es         \n"
    /*TODO*///	:
    /*TODO*///	"=c" (width2),
    /*TODO*///	"=d" (seg),
    /*TODO*///	"=S" (src),
    /*TODO*///	"=D" (address)
    /*TODO*///	:
    /*TODO*///	"0" (width2),
    /*TODO*///	"1" (seg),
    /*TODO*///	"2" (src),
    /*TODO*///	"3" (address):
    /*TODO*///	"ax", "bx", "cc", "memory");
    /*TODO*///}
    /*TODO*///
    /*TODO*///INLINE void copyline_4x_16bpp(unsigned char *src,short seg,unsigned long address,int width2)
    /*TODO*///{
    /*TODO*///	__asm__ __volatile__ (
    /*TODO*///	"pushw %%es              \n"
    /*TODO*///	"movw %%dx, %%es         \n"
    /*TODO*///	"cld                     \n"
    /*TODO*///	".align 4                \n"
    /*TODO*///	"0:                      \n"
    /*TODO*///	"lodsl                   \n"
    /*TODO*///	"movl %%eax, %%ebx       \n"
    /*TODO*///	"roll $16, %%eax         \n"
    /*TODO*///	"xchgw %%ax,%%bx         \n"
    /*TODO*///	"stosl                   \n"
    /*TODO*///	"stosl                   \n"
    /*TODO*///	"movl %%ebx, %%eax       \n"
    /*TODO*///	"stosl                   \n"
    /*TODO*///	"stosl 					 \n"
    /*TODO*///
    /*TODO*///
    /*TODO*///	"loop 0b                 \n"
    /*TODO*///	"popw %%ax               \n"
    /*TODO*///	"movw %%ax, %%es         \n"
    /*TODO*///	:
    /*TODO*///	"=c" (width2),
    /*TODO*///	"=d" (seg),
    /*TODO*///	"=S" (src),
    /*TODO*///	"=D" (address)
    /*TODO*///	:
    /*TODO*///	"0" (width2),
    /*TODO*///	"1" (seg),
    /*TODO*///	"2" (src),
    /*TODO*///	"3" (address):
    /*TODO*///	"ax", "bx", "cc", "memory");
    /*TODO*///}
    /*TODO*///
    /*TODO*///INLINE void copyline_3x_8bpp(unsigned char *src,short seg,unsigned long address,int width4)
    /*TODO*///{
    /*TODO*///	int i;
    /*TODO*///
    /*TODO*///	/* set up selector */
    /*TODO*///	_farsetsel(seg);
    /*TODO*///
    /*TODO*///	for (i = width4; i > 0; i--)
    /*TODO*///	{
    /*TODO*///		_farnspokel (address, (quadpixel[*src] & 0x00ffffff) | (quadpixel[*(src+1)] & 0xff000000));
    /*TODO*///		_farnspokel (address+4, (quadpixel[*(src+1)] & 0x0000ffff) | (quadpixel[*(src+2)] & 0xffff0000));
    /*TODO*///		_farnspokel (address+8, (quadpixel[*(src+2)] & 0x000000ff) | (quadpixel[*(src+3)] & 0xffffff00));
    /*TODO*///		address+=3*4;
    /*TODO*///		src+=4;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///INLINE void copyline_4x_8bpp(unsigned char *src,short seg,unsigned long address,int width4)
    /*TODO*///{
    /*TODO*///	int i;
    /*TODO*///
    /*TODO*///	/* set up selector */
    /*TODO*///	_farsetsel(seg);
    /*TODO*///
    /*TODO*///	for (i = width4; i > 0; i--)
    /*TODO*///	{
    /*TODO*///		_farnspokel (address, quadpixel[*src]);
    /*TODO*///		_farnspokel (address+4, quadpixel[*(src+1)]);
    /*TODO*///		_farnspokel (address+8, quadpixel[*(src+2)]);
    /*TODO*///		_farnspokel (address+12, quadpixel[*(src+3)]);
    /*TODO*///		address+=16;
    /*TODO*///		src+=4;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///INLINE void copyline_1x_16bpp_palettized(unsigned char *src,short seg,unsigned long address,int width2)
    /*TODO*///{
    /*TODO*///	int i;
    /*TODO*///	unsigned short *s=(unsigned short *)src;
    /*TODO*///
    /*TODO*///	/* set up selector */
    /*TODO*///	_farsetsel(seg);
    /*TODO*///
    /*TODO*///	for (i = width2; i > 0; i--)
    /*TODO*///	{
    /*TODO*///		UINT32 d1,d2;
    /*TODO*///
    /*TODO*///		d1 = palette_16bit_lookup[*(s++)];
    /*TODO*///		d2 = palette_16bit_lookup[*(s++)];
    /*TODO*///		_farnspokel(address,(d1 & 0x0000ffff) | (d2 & 0xffff0000));
    /*TODO*///		address+=4;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///INLINE void copyline_2x_16bpp_palettized(unsigned char *src,short seg,unsigned long address,int width2)
    /*TODO*///{
    /*TODO*///	int i;
    /*TODO*///	unsigned short *s=(unsigned short *)src;
    /*TODO*///
    /*TODO*///	/* set up selector */
    /*TODO*///	_farsetsel(seg);
    /*TODO*///
    /*TODO*///	for (i = 2*width2; i > 0; i--)
    /*TODO*///	{
    /*TODO*///		_farnspokel(address,palette_16bit_lookup[*(s++)]);
    /*TODO*///		address+=4;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///INLINE void copyline_3x_16bpp_palettized(unsigned char *src,short seg,unsigned long address,int width2)
    /*TODO*///{
    /*TODO*///	int i;
    /*TODO*///	unsigned short *s=(unsigned short *)src;
    /*TODO*///
    /*TODO*///	/* set up selector */
    /*TODO*///	_farsetsel(seg);
    /*TODO*///
    /*TODO*///	for (i = width2; i > 0; i--)
    /*TODO*///	{
    /*TODO*///		UINT32 d1,d2;
    /*TODO*///
    /*TODO*///		d1 = palette_16bit_lookup[*(s++)];
    /*TODO*///		d2 = palette_16bit_lookup[*(s++)];
    /*TODO*///		_farnspokel(address,d1);
    /*TODO*///		_farnspokel(address+4,(d1 & 0x0000ffff) | (d2 & 0xffff0000));
    /*TODO*///		_farnspokel(address+8,d2);
    /*TODO*///		address+=3*4;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///INLINE void copyline_4x_16bpp_palettized(unsigned char *src,short seg,unsigned long address,int width2)
    /*TODO*///{
    /*TODO*///	int i;
    /*TODO*///	unsigned short *s=(unsigned short *)src;
    /*TODO*///
    /*TODO*///	/* set up selector */
    /*TODO*///	_farsetsel(seg);
    /*TODO*///
    /*TODO*///	for (i = 2*width2; i > 0; i--)
    /*TODO*///	{
    /*TODO*///		UINT32 d;
    /*TODO*///
    /*TODO*///		d = palette_16bit_lookup[*(s++)];
    /*TODO*///		_farnspokel(address,d);
    /*TODO*///		_farnspokel(address+4,d);
    /*TODO*///		address+=8;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///#define DIRTY1(MX,MY,SL,BPP,PALETTIZED) \
    /*TODO*///	short dest_seg; \
    /*TODO*///	int x,y,vesa_line,line_offs,xoffs; \
    /*TODO*///	unsigned char *lb; \
    /*TODO*///	unsigned long address; \
    /*TODO*///	dest_seg = screen->seg; \
    /*TODO*///	vesa_line = gfx_yoffset; \
    /*TODO*///	line_offs = scrbitmap->line[1] - scrbitmap->line[0]; \
    /*TODO*///	xoffs = (BPP/8)*gfx_xoffset; \
    /*TODO*///	lb = scrbitmap->line[skiplines] + (BPP/8)*skipcolumns; \
    /*TODO*///	for (y = 0;y < gfx_display_lines;y += 16) \
    /*TODO*///	{ \
    /*TODO*///		for (x = 0;x < gfx_display_columns; /* */) \
    /*TODO*///		{ \
    /*TODO*///			int w = 16; \
    /*TODO*///			if (ISDIRTY(x,y)) \
    /*TODO*///            { \
    /*TODO*///				unsigned char *src = lb + (BPP/8)*x; \
    /*TODO*///                int vesa_line0 = vesa_line, h; \
    /*TODO*///				while (x + w < gfx_display_columns && ISDIRTY(x+w,y)) \
    /*TODO*///                    w += 16; \
    /*TODO*///				if (x + w > gfx_display_columns) \
    /*TODO*///					w = gfx_display_columns - x; \
    /*TODO*///				for (h = 0; h < 16 && y + h < gfx_display_lines; h++) \
    /*TODO*///                { \
    /*TODO*///					address = bmp_write_line(screen,vesa_line0) + xoffs + MX*(BPP/8)*x; \
    /*TODO*///					copyline_##MX##x_##BPP##bpp##PALETTIZED(src,dest_seg,address,w/(4/(BPP/8))); \
    /*TODO*///				    if ((MY > 2) || ((MY == 2) && (SL == 0))) { \
    /*TODO*///						address = bmp_write_line(screen,vesa_line0+1) + xoffs + MX*(BPP/8)*x; \
    /*TODO*///						copyline_##MX##x_##BPP##bpp##PALETTIZED(src,dest_seg,address,w/(4/(BPP/8))); \
    /*TODO*///					} \
    /*TODO*///					if ((MY > 3) || ((MY == 3) && (SL == 0))) { \
    /*TODO*///						address = bmp_write_line(screen,vesa_line0+2) + xoffs + MX*(BPP/8)*x; \
    /*TODO*///						copyline_##MX##x_##BPP##bpp##PALETTIZED(src,dest_seg,address,w/(4/(BPP/8))); \
    /*TODO*///					} \
    /*TODO*///					vesa_line0 += MY; \
    /*TODO*///					src += line_offs; \
    /*TODO*///				} \
    /*TODO*///			} \
    /*TODO*///			x += w; \
    /*TODO*///		} \
    /*TODO*///		vesa_line += MY*16; \
    /*TODO*///		lb += 16 * line_offs; \
    /*TODO*///	}
    /*TODO*///
    /*TODO*///#define DIRTY0(MX,MY,SL,BPP,PALETTIZED) \
    /*TODO*///	short dest_seg; \
    /*TODO*///	int y,vesa_line,line_offs,xoffs,width; \
    /*TODO*///	unsigned char *src; \
    /*TODO*///	unsigned long address, address_offset; \
    /*TODO*///	dest_seg = screen->seg; \
    /*TODO*///	vesa_line = gfx_yoffset; \
    /*TODO*///	line_offs = (scrbitmap->line[1] - scrbitmap->line[0]); \
    /*TODO*///	xoffs = (BPP/8)*(gfx_xoffset + triplebuf_pos); \
    /*TODO*///	width = gfx_display_columns/(4/(BPP/8)); \
    /*TODO*///	src = scrbitmap->line[skiplines] + (BPP/8)*skipcolumns;	\
    /*TODO*///	if (mmxlfb) { \
    /*TODO*///		address = bmp_write_line(screen, vesa_line); \
    /*TODO*///		_farsetsel (screen->seg); \
    /*TODO*///		address_offset = bmp_write_line(screen, vesa_line+1) - address; \
    /*TODO*///		address += xoffs; \
    /*TODO*///		{ \
    /*TODO*///		extern void asmblit_##MX##x_##MY##y_##SL##sl_##BPP##bpp##PALETTIZED \
    /*TODO*///			(int, int, unsigned char *, int, unsigned long, unsigned long); \
    /*TODO*///		asmblit_##MX##x_##MY##y_##SL##sl_##BPP##bpp##PALETTIZED \
    /*TODO*///			(width, gfx_display_lines, src, line_offs, address, address_offset); \
    /*TODO*///		} \
    /*TODO*///	} \
    /*TODO*///	else { \
    /*TODO*///		for (y = 0;y < gfx_display_lines;y++) \
    /*TODO*///		{ \
    /*TODO*///			address = bmp_write_line(screen,vesa_line) + xoffs; \
    /*TODO*///			copyline_##MX##x_##BPP##bpp##PALETTIZED(src,dest_seg,address,width); \
    /*TODO*///		    if ((MY > 2) || ((MY == 2) && (SL == 0))) { \
    /*TODO*///				address = bmp_write_line(screen,vesa_line+1) + xoffs; \
    /*TODO*///				copyline_##MX##x_##BPP##bpp##PALETTIZED(src,dest_seg,address,width); \
    /*TODO*///			} \
    /*TODO*///			if ((MY > 3) || ((MY == 3) && (SL == 0))) { \
    /*TODO*///				address = bmp_write_line(screen,vesa_line+2) + xoffs; \
    /*TODO*///				copyline_##MX##x_##BPP##bpp##PALETTIZED(src,dest_seg,address,width); \
    /*TODO*///			} \
    /*TODO*///			vesa_line += MY; \
    /*TODO*///			src += line_offs; \
    /*TODO*///		} \
    /*TODO*///	} \
    /*TODO*///	if (use_triplebuf) \
    /*TODO*///	{ \
    /*TODO*///		vesa_scroll_async(triplebuf_pos,0); \
    /*TODO*///		triplebuf_pos = (triplebuf_pos + triplebuf_page_width) % (3*triplebuf_page_width); \
    /*TODO*///	}
    /*TODO*///
    /*TODO*///
    /*TODO*///void blitscreen_dirty1_vesa_1x_1x_8bpp(void)   { DIRTY1(1,1,0,8,)  }
    /*TODO*///void blitscreen_dirty1_vesa_1x_2x_8bpp(void)   { DIRTY1(1,2,0,8,)  }
    /*TODO*///void blitscreen_dirty1_vesa_1x_2xs_8bpp(void)  { DIRTY1(1,2,1,8,)  }
    /*TODO*///void blitscreen_dirty1_vesa_2x_1x_8bpp(void)   { DIRTY1(2,1,0,8,)  }
    /*TODO*///void blitscreen_dirty1_vesa_3x_1x_8bpp(void)   { DIRTY1(3,1,0,8,)  }
    /*TODO*///void blitscreen_dirty1_vesa_2x_2x_8bpp(void)   { DIRTY1(2,2,0,8,)  }
    /*TODO*///void blitscreen_dirty1_vesa_2x_2xs_8bpp(void)  { DIRTY1(2,2,1,8,)  }
    /*TODO*///void blitscreen_dirty1_vesa_2x_3x_8bpp(void)   { DIRTY1(2,3,0,8,)  }
    /*TODO*///void blitscreen_dirty1_vesa_2x_3xs_8bpp(void)  { DIRTY1(2,3,1,8,)  }
    /*TODO*///void blitscreen_dirty1_vesa_3x_2x_8bpp(void)   { DIRTY1(3,2,0,8,)  }
    /*TODO*///void blitscreen_dirty1_vesa_3x_2xs_8bpp(void)  { DIRTY1(3,2,1,8,)  }
    /*TODO*///void blitscreen_dirty1_vesa_3x_3x_8bpp(void)   { DIRTY1(3,3,0,8,)  }
    /*TODO*///void blitscreen_dirty1_vesa_3x_3xs_8bpp(void)  { DIRTY1(3,3,1,8,)  }
    /*TODO*///void blitscreen_dirty1_vesa_4x_2x_8bpp(void)   { DIRTY1(4,2,0,8,)  }
    /*TODO*///void blitscreen_dirty1_vesa_4x_2xs_8bpp(void)  { DIRTY1(4,2,1,8,)  }
    /*TODO*///void blitscreen_dirty1_vesa_4x_3x_8bpp(void)   { DIRTY1(4,3,0,8,)  }
    /*TODO*///void blitscreen_dirty1_vesa_4x_3xs_8bpp(void)  { DIRTY1(4,3,1,8,)  }
    /*TODO*///
    /*TODO*///void blitscreen_dirty1_vesa_1x_1x_16bpp(void)  { DIRTY1(1,1,0,16,) }
    /*TODO*///void blitscreen_dirty1_vesa_1x_2x_16bpp(void)  { DIRTY1(1,2,0,16,) }
    /*TODO*///void blitscreen_dirty1_vesa_1x_2xs_16bpp(void) { DIRTY1(1,2,1,16,) }
    /*TODO*///void blitscreen_dirty1_vesa_2x_1x_16bpp(void)  { DIRTY1(2,1,0,16,) }
    /*TODO*///void blitscreen_dirty1_vesa_2x_2x_16bpp(void)  { DIRTY1(2,2,0,16,) }
    /*TODO*///void blitscreen_dirty1_vesa_2x_2xs_16bpp(void) { DIRTY1(2,2,1,16,) }
    /*TODO*///void blitscreen_dirty1_vesa_3x_1x_16bpp(void)  { DIRTY1(3,1,0,16,) }
    /*TODO*///void blitscreen_dirty1_vesa_3x_2x_16bpp(void)  { DIRTY1(3,2,0,16,) }
    /*TODO*///void blitscreen_dirty1_vesa_3x_2xs_16bpp(void) { DIRTY1(3,2,1,16,) }
    /*TODO*///void blitscreen_dirty1_vesa_4x_2x_16bpp(void)  { DIRTY1(4,2,0,16,) }
    /*TODO*///void blitscreen_dirty1_vesa_4x_2xs_16bpp(void) { DIRTY1(4,2,1,16,) }
    /*TODO*///
    /*TODO*///void blitscreen_dirty1_vesa_1x_1x_16bpp_palettized(void)  { DIRTY1(1,1,0,16,_palettized) }
    /*TODO*///void blitscreen_dirty1_vesa_1x_2x_16bpp_palettized(void)  { DIRTY1(1,2,0,16,_palettized) }
    /*TODO*///void blitscreen_dirty1_vesa_1x_2xs_16bpp_palettized(void) { DIRTY1(1,2,1,16,_palettized) }
    /*TODO*///void blitscreen_dirty1_vesa_2x_1x_16bpp_palettized(void)  { DIRTY1(2,1,0,16,_palettized) }
    /*TODO*///void blitscreen_dirty1_vesa_2x_2x_16bpp_palettized(void)  { DIRTY1(2,2,0,16,_palettized) }
    /*TODO*///void blitscreen_dirty1_vesa_2x_2xs_16bpp_palettized(void) { DIRTY1(2,2,1,16,_palettized) }
    /*TODO*///void blitscreen_dirty1_vesa_3x_1x_16bpp_palettized(void)  { DIRTY1(3,1,0,16,_palettized) }
    /*TODO*///void blitscreen_dirty1_vesa_3x_2x_16bpp_palettized(void)  { DIRTY1(3,2,0,16,_palettized)  }
    /*TODO*///void blitscreen_dirty1_vesa_3x_2xs_16bpp_palettized(void) { DIRTY1(3,2,1,16,_palettized) }
    /*TODO*///void blitscreen_dirty1_vesa_4x_2x_16bpp_palettized(void)  { DIRTY1(4,2,0,16,_palettized) }
    /*TODO*///void blitscreen_dirty1_vesa_4x_2xs_16bpp_palettized(void) { DIRTY1(4,2,1,16,_palettized) }
    /*TODO*///
    /*TODO*///void blitscreen_dirty0_vesa_1x_1x_8bpp(void)   { DIRTY0(1,1,0,8,)  }
    /*TODO*///void blitscreen_dirty0_vesa_1x_2x_8bpp(void)   { DIRTY0(1,2,0,8,)  }
    /*TODO*///void blitscreen_dirty0_vesa_1x_2xs_8bpp(void)  { DIRTY0(1,2,1,8,)  }
    /*TODO*///void blitscreen_dirty0_vesa_2x_1x_8bpp(void)   { DIRTY0(2,1,0,8,)  }
    /*TODO*///void blitscreen_dirty0_vesa_3x_1x_8bpp(void)   { DIRTY0(3,1,0,8,)  }
    /*TODO*///void blitscreen_dirty0_vesa_2x_2x_8bpp(void)   { DIRTY0(2,2,0,8,)  }
    /*TODO*///void blitscreen_dirty0_vesa_2x_2xs_8bpp(void)  { DIRTY0(2,2,1,8,)  }
    /*TODO*///void blitscreen_dirty0_vesa_2x_3x_8bpp(void)   { DIRTY0(2,3,0,8,)  }
    /*TODO*///void blitscreen_dirty0_vesa_2x_3xs_8bpp(void)  { DIRTY0(2,3,1,8,)  }
    /*TODO*///void blitscreen_dirty0_vesa_3x_2x_8bpp(void)   { DIRTY0(3,2,0,8,)  }
    /*TODO*///void blitscreen_dirty0_vesa_3x_2xs_8bpp(void)  { DIRTY0(3,2,1,8,)  }
    /*TODO*///void blitscreen_dirty0_vesa_3x_3x_8bpp(void)   { DIRTY0(3,3,0,8,)  }
    /*TODO*///void blitscreen_dirty0_vesa_3x_3xs_8bpp(void)  { DIRTY0(3,3,1,8,)  }
    /*TODO*///void blitscreen_dirty0_vesa_4x_2x_8bpp(void)   { DIRTY0(4,2,0,8,)  }
    /*TODO*///void blitscreen_dirty0_vesa_4x_2xs_8bpp(void)  { DIRTY0(4,2,1,8,)  }
    /*TODO*///void blitscreen_dirty0_vesa_4x_3x_8bpp(void)   { DIRTY0(4,3,0,8,)  }
    /*TODO*///void blitscreen_dirty0_vesa_4x_3xs_8bpp(void)  { DIRTY0(4,3,1,8,)  }
    /*TODO*///
    /*TODO*///void blitscreen_dirty0_vesa_1x_1x_16bpp(void)  { DIRTY0(1,1,0,16,) }
    /*TODO*///void blitscreen_dirty0_vesa_1x_2x_16bpp(void)  { DIRTY0(1,2,0,16,) }
    /*TODO*///void blitscreen_dirty0_vesa_1x_2xs_16bpp(void) { DIRTY0(1,2,1,16,) }
    /*TODO*///void blitscreen_dirty0_vesa_2x_1x_16bpp(void)  { DIRTY0(2,1,0,16,) }
    /*TODO*///void blitscreen_dirty0_vesa_2x_2x_16bpp(void)  { DIRTY0(2,2,0,16,) }
    /*TODO*///void blitscreen_dirty0_vesa_2x_2xs_16bpp(void) { DIRTY0(2,2,1,16,) }
    /*TODO*///void blitscreen_dirty0_vesa_3x_1x_16bpp(void)  { DIRTY0(3,1,0,16,) }
    /*TODO*///void blitscreen_dirty0_vesa_3x_2x_16bpp(void)  { DIRTY0(3,2,0,16,) }
    /*TODO*///void blitscreen_dirty0_vesa_3x_2xs_16bpp(void) { DIRTY0(3,2,1,16,) }
    /*TODO*///void blitscreen_dirty0_vesa_4x_2x_16bpp(void)  { DIRTY0(4,2,0,16,) }
    /*TODO*///void blitscreen_dirty0_vesa_4x_2xs_16bpp(void) { DIRTY0(4,2,1,16,) }
    /*TODO*///
    /*TODO*///void blitscreen_dirty0_vesa_1x_1x_16bpp_palettized(void)  { DIRTY0(1,1,0,16,_palettized) }
    /*TODO*///void blitscreen_dirty0_vesa_1x_2x_16bpp_palettized(void)  { DIRTY0(1,2,0,16,_palettized) }
    /*TODO*///void blitscreen_dirty0_vesa_1x_2xs_16bpp_palettized(void) { DIRTY0(1,2,1,16,_palettized) }
    /*TODO*///void blitscreen_dirty0_vesa_2x_1x_16bpp_palettized(void)  { DIRTY0(2,1,0,16,_palettized) }
    /*TODO*///void blitscreen_dirty0_vesa_2x_2x_16bpp_palettized(void)  { DIRTY0(2,2,0,16,_palettized) }
    /*TODO*///void blitscreen_dirty0_vesa_2x_2xs_16bpp_palettized(void) { DIRTY0(2,2,1,16,_palettized) }
    /*TODO*///void blitscreen_dirty0_vesa_3x_1x_16bpp_palettized(void)  { DIRTY0(3,1,0,16,_palettized) }
    /*TODO*///void blitscreen_dirty0_vesa_3x_2x_16bpp_palettized(void)  { DIRTY0(3,2,0,16,_palettized) }
    /*TODO*///void blitscreen_dirty0_vesa_3x_2xs_16bpp_palettized(void) { DIRTY0(3,2,1,16,_palettized) }
    /*TODO*///void blitscreen_dirty0_vesa_4x_2x_16bpp_palettized(void)  { DIRTY0(4,2,0,16,_palettized) }
    /*TODO*///void blitscreen_dirty0_vesa_4x_2xs_16bpp_palettized(void) { DIRTY0(4,2,1,16,_palettized) }
    /*TODO*///    
}
