/*************************************************************************

 Driver for Williams/Midway games using the TMS34010 processor

**************************************************************************/
#include "driver.h"
#include "cpu/tms34010/tms34010.h"
#include "cpu/m6809/m6809.h"
#include "6821pia.h"
#include "sndhrdw/williams.h"


#define FAST_DMA			1


#if LSB_FIRST
	#define BYTE_XOR_LE(a)  (a)
	#define BIG_DWORD_LE(x) (x)
#else
	#define BYTE_XOR_LE(a)  ((UINT8*)((UINT32)(a) ^ 1))
	#define BIG_DWORD_LE(x) (((UINT32)(x) >> 16) + ((x) << 16))
#endif


#define CODE_ROM     cpu_bankbase[1]
#define GFX_ROM	     cpu_bankbase[8]
#define SCRATCH_RAM	 cpu_bankbase[2]

static void narc_sound_w (int offset,int data);
static void adpcm_sound_w (int offset,int data);
static void cvsd_sound_w (int offset,int data);

void wms_vram_w(int offset, int data);
void wms_objpalram_w(int offset, int data);
int wms_vram_r(int offset);
int wms_objpalram_r(int offset);
void wms_update_partial(int scanline);

static UINT8 *gfxrombackup;

extern UINT16 *wms_videoram;

       UINT8 *wms_cmos_ram;
       INT32  wms_bank2_size;
extern INT32  wms_videoram_size;
       int    wms_code_rom_size;
       int    wms_gfx_rom_size;
       UINT8  wms_rom_loaded;
static UINT32 wms_cmos_page = 0;
       INT32  wms_objpalram_select = 0;
       UINT8  wms_autoerase_enable = 0;
       UINT8  wms_autoerase_reset = 0;

static UINT32 wms_dma_rows=0;
static INT32  wms_dma_write_rows=0;
static UINT32 wms_dma_cols=0;
static UINT32 wms_dma_bank=0;
static UINT32 wms_dma_subbank=0;
static UINT32 wms_dma_x=0;
static UINT32 wms_dma_y=0;
       UINT32 wms_dma_pal=0;
       UINT32 wms_dma_pal_word=0;
static UINT32 wms_dma_dst=0;
static UINT32 wms_dma_stat=0;
static UINT32 wms_dma_fgcol=0;
static INT16  wms_dma_woffset=0;

static UINT16 wms_dma_preskip=0;
static UINT16 wms_dma_postskip=0;

static UINT32 wms_dma_temp=0;

static UINT32 wms_dma_8pos=0;  /* are we not on a byte boundary? */
static UINT32 wms_dma_preclip=0;
static UINT32 wms_dma_postclip=0;

static UINT16 wms_unk1=0;
static UINT16 wms_unk2=0;
static UINT16 wms_sysreg2=0;

static UINT32 wms_dma_14=0;
static UINT32 wms_dma_16=0;
static UINT32 wms_dma_tclip=0;
static UINT32 wms_dma_bclip=0;
static UINT32 wms_dma_1c=0;
static UINT32 wms_dma_1e=0;

static UINT32 smashtv_cmos_w_enable=1;

static UINT32 wms_protect_s=0xffffffff; /* never gets here */
static UINT32 wms_protect_d=0xffffffff;

static UINT32 term2_analog_select = 0;

static int narc_input_r (int offset)
{
	int ans = 0xffff;
	switch (offset)
	{
	case 0:
		ans = (input_port_1_r (offset) << 8) + (input_port_0_r (offset));
		break;
	case 2:
		ans = (input_port_3_r (offset) << 8) + (input_port_2_r (offset));
		break;
	case 4:
		ans = (input_port_5_r (offset) << 8) + (soundlatch3_r (0));
		break;
//	case 6:
//		ans = (input_port_7_r (offset) << 8) + (input_port_6_r (offset));
//		break;
//	case 8:
//		ans = (input_port_9_r (offset) << 8) + (input_port_8_r (offset));
//		break;
	default:
		if (errorlog) fprintf(errorlog, "CPU #0 PC %08x: warning - read from unmapped bit address %x\n", cpu_get_pc(), (offset<<3));
	}
	return ans;
}
int wms_input_r (int offset)
{
	int ans = 0xffff;
	switch (offset)
	{
	case 0:
		ans = (input_port_1_r (offset) << 8) + (input_port_0_r (offset));
		break;
	case 2:
		ans = (input_port_3_r (offset) << 8) + (input_port_2_r (offset));
		break;
	case 4:
		ans = (input_port_5_r (offset) << 8) + (input_port_4_r (offset));
		break;
	case 6:
		ans = (input_port_7_r (offset) << 8) + (input_port_6_r (offset));
		break;
	case 8:
		ans = (input_port_9_r (offset) << 8) + (input_port_8_r (offset));
		break;
	default:
		if (errorlog) fprintf(errorlog, "CPU #0 PC %08x: warning - read from unmapped bit address %x\n", cpu_get_pc(), (offset<<3));
	}
	return ans;
}
static int term2_input_r (int offset)
{
	int ans = 0xffff;
	switch (offset)
	{
	case 0:
		ans = (input_port_1_r (offset) << 8) + (input_port_0_r (offset));
		break;
	case 2:
		ans = (input_port_3_r (offset) << 8) + (input_port_2_r (offset));
		break;
	case 4:
		ans = (input_port_5_r (offset) << 8);
		switch (term2_analog_select)
		{
		case 0:  ans |= input_port_4_r  (offset);  break;
		case 1:  ans |= input_port_10_r (offset);  break;
		case 2:  ans |= input_port_11_r (offset);  break;
		case 3:  ans |= input_port_12_r (offset);  break;
		}
		break;
	case 6:
		ans = (input_port_7_r (offset) << 8) + (input_port_6_r (offset));
		break;
	case 8:
		ans = (input_port_9_r (offset) << 8) + (input_port_8_r (offset));
		break;
	default:
		if (errorlog) fprintf(errorlog, "CPU #0 PC %08x: warning - read from unmapped bit address %x\n", cpu_get_pc(), (offset<<3));
	}
	return ans;
}

static int term2_input_lo_r (int offset)
{
	int ans = 0xffff;
	switch (offset)
	{
//	case 0:
//		ans = (input_port_5_r (offset) << 8) + (input_port_4_r (offset));
//		break;
//	case 2:
//		ans = (input_port_7_r (offset) << 8) + (input_port_6_r (offset));
//		break;
//	case 4:
//		ans = (input_port_9_r (offset) << 8) + (input_port_8_r (offset));
//		break;
	default:
		if (errorlog) fprintf(errorlog, "CPU #0 PC %08x: warning - read from unmapped bit address %x\n", cpu_get_pc(), (offset<<3));
	}
	return ans;
}

static void term2_sound_w (int offset,int data)
{
	if (offset == 0)
	{
		term2_analog_select = (data >> 0x0c) & 0x03;
	}

	adpcm_sound_w(offset, data);
}

static int irq_callback(int irqline)
{
	tms34010_set_irq_line(0, CLEAR_LINE);
	return 0;
}

static void dma_callback(int is_in_34010_context)
{
	wms_dma_stat = 0; /* tell the cpu we're done */
	if (is_in_34010_context)
	{
		tms34010_set_irq_callback(irq_callback);
		tms34010_set_irq_line(0, ASSERT_LINE);
	}
	else
		cpu_cause_interrupt(0,TMS34010_INT1);
}

static void dma2_callback(int is_in_34010_context)
{
	wms_dma_stat = 0; /* tell the cpu we're done */
	if (is_in_34010_context)
	{
		tms34010_set_irq_callback(irq_callback);
		tms34010_set_irq_line(0, ASSERT_LINE);
	}
	else
		cpu_cause_interrupt(0,TMS34010_INT1);
}

int wms_dma_r(int offset)
{
	if (wms_dma_stat&0x8000)
	{
		switch (cpu_get_pc())
		{
		case 0xfff7aa20: /* narc */
		case 0xffe1c970: /* trog */
		case 0xffe1c9a0: /* trog3 */
		case 0xffe1d4a0: /* trogp */
		case 0xffe07690: /* smashtv */
		case 0xffe00450: /* hiimpact */
		case 0xffe14930: /* strkforc */
		case 0xffe02c20: /* strkforc */
		case 0xffc79890: /* mk */
		case 0xffc7a5a0: /* mk */
		case 0xffc063b0: /* term2 */
		case 0xffc00720: /* term2 */
		case 0xffc07a60: /* totcarn/totcarnp */
		case 0xff805200: /* mk2 */
		case 0xff8044e0: /* mk2 */
		case 0xff82e200: /* nbajam */
			cpu_spinuntil_int();
			wms_dma_stat=0;
		break;

		default:
//			if (errorlog) fprintf(errorlog, "CPU #0 PC %08x: read hi dma\n", cpu_get_pc());
			break;
		}
	}
	return wms_dma_stat;
}


/*****************************************************************************************
 *																						 *
 *			   They may not be optimal for other compilers. If you change anything, make *
 *             sure you test it on a Pentium II. Even a trivial change can make a huge   *
 *             difference!																 *
 *																						 *
 *****************************************************************************************/

#define DMA_DRAW_NONZERO_BYTES(data,advance)			\
	if (write_cols >= 0)								\
	{													\
		pal = wms_dma_pal;								\
		line_skip = 511 - write_cols; 					\
		wrva = &(wms_videoram[wms_dma_dst]);			\
		for (i=wms_dma_write_rows;i;i--)				\
		{												\
			j=write_cols;								\
			do											\
			{											\
				if ((write_data = *BYTE_XOR_LE(rda)))	\
				{										\
					*wrva = pal | (data);				\
				}										\
				rda##advance;							\
				wrva++;									\
			}											\
			while (j--); 								\
			rda+=dma_skip;								\
			wrva+=line_skip;							\
		}												\
	}

#define DMA_DRAW_ALL_BYTES(data)						\
	if (write_cols >= 0)								\
	{													\
		pal = wms_dma_pal;								\
		line_skip = 511 - write_cols;					\
		wrva = &(wms_videoram[wms_dma_dst]);			\
		for (i=wms_dma_write_rows;i;i--)				\
		{												\
			j=write_cols;								\
			do											\
			{											\
				*(wrva++) = pal | (data);				\
			}											\
			while (j--);								\
			rda+=dma_skip;								\
			wrva+=line_skip;							\
		}												\
	}

void wms_dma_w(int offset, int data)
{
	UINT32 i, j, pal, write_data, line_skip, dma_skip=0;
	int write_cols;
	UINT8 *rda;
	UINT16 *wrva;

	switch (offset)
	{
	case 0:
		write_cols = (wms_dma_cols+wms_dma_x<512?wms_dma_cols:512-wms_dma_x)-1;  /* Note the -1 */
		wms_dma_write_rows = (wms_dma_rows+wms_dma_y<512?wms_dma_rows:512-wms_dma_y);
		wms_dma_dst = ((wms_dma_y<<9) + (wms_dma_x)); /* byte addr */
		rda = &(GFX_ROM[wms_dma_bank+wms_dma_subbank]);
		wms_dma_stat = data;

		if (errorlog) fprintf(errorlog, "\nCPU #0 PC %08x: DMA command %04x:\n",cpu_get_pc(), data);
		if (errorlog) fprintf(errorlog, "%04x %04x x%04x y%04x  c%04x r%04x p%04x c%04x\n",wms_dma_subbank, wms_dma_bank, wms_dma_x, wms_dma_y, wms_dma_cols, wms_dma_rows, wms_dma_pal, wms_dma_fgcol );
		/*
		 * DMA registers
		 * ------------------
		 *
		 *  Register | Bit              | Use
		 * ----------+-FEDCBA9876543210-+------------
		 *     0     | x--------------- | trigger write (or clear if zero)
		 *           | ---184-1-------- | unknown
		 *           | ----------x----- | flip y
		 *           | -----------x---- | flip x
		 *           | ------------x--- | blit nonzero pixels as color
		 *           | -------------x-- | blit zero pixels as color
		 *           | --------------x- | blit nonzero pixels
		 *           | ---------------x | blit zero pixels
		 *     1     | xxxxxxxxxxxxxxxx | width offset
		 *     2     | xxxxxxxxxxxxxxxx | source address low word
		 *     3     | xxxxxxxxxxxxxxxx | source address high word
		 *     4     | xxxxxxxxxxxxxxxx | detination x
		 *     5     | xxxxxxxxxxxxxxxx | destination y
		 *     6     | xxxxxxxxxxxxxxxx | image columns
		 *     7     | xxxxxxxxxxxxxxxx | image rows
		 *     8     | xxxxxxxxxxxxxxxx | palette
		 *     9     | xxxxxxxxxxxxxxxx | color
		 */
		switch(data&0x803f)
		{
		case 0x0000: /* clear registers */
			dma_skip=0;
			wms_dma_cols=0;
			wms_dma_rows=0;
			wms_dma_pal=0;
			wms_dma_fgcol=0;
			break;
		case 0x8000: /* draw nothing */
			break;
		case 0x8002: /* draw only nonzero pixels */
			dma_skip = ((wms_dma_cols + wms_dma_woffset+3)&(~3)) - wms_dma_cols;
			DMA_DRAW_NONZERO_BYTES(write_data,++);
			break;
		case 0x8003: /* draw all pixels */
			dma_skip = ((wms_dma_cols + wms_dma_woffset+3)&(~3)) - wms_dma_cols;
			DMA_DRAW_ALL_BYTES(*BYTE_XOR_LE(rda++));
			break;
		case 0x8006: /* draw nonzero pixels, zero as color */
			dma_skip = ((wms_dma_cols + wms_dma_woffset+3)&(~3)) - wms_dma_cols;
			DMA_DRAW_ALL_BYTES((*BYTE_XOR_LE(rda++)?*BYTE_XOR_LE(rda-1):wms_dma_fgcol));
			break;
		case 0x800a: /* ????? */
		case 0x8008: /* draw nonzero pixels as color */
			dma_skip = ((wms_dma_cols + wms_dma_woffset+3)&(~3)) - wms_dma_cols;
			DMA_DRAW_NONZERO_BYTES(wms_dma_fgcol,++);
			break;
		case 0x8009: /* draw nonzero pixels as color, zero as zero */
			dma_skip = ((wms_dma_cols + wms_dma_woffset+3)&(~3)) - wms_dma_cols;
			DMA_DRAW_ALL_BYTES((*BYTE_XOR_LE(rda++)?wms_dma_fgcol:0));
			break;
		case 0x800e: /* ????? */
		case 0x800c: /* draw all pixels as color (fill) */
			DMA_DRAW_ALL_BYTES(wms_dma_fgcol);
			break;
		case 0x8010: /* draw nothing */
			break;
		case 0x8012: /* draw nonzero pixels x-flipped */
			dma_skip = ((wms_dma_woffset + 3 - wms_dma_cols)&(~3)) + wms_dma_cols;
			DMA_DRAW_NONZERO_BYTES(write_data,--);
			break;
		case 0x8013: /* draw all pixels x-flipped */
			dma_skip = ((wms_dma_woffset + 3 - wms_dma_cols)&(~3)) + wms_dma_cols;
			DMA_DRAW_ALL_BYTES(*BYTE_XOR_LE(rda--));
			break;
		case 0x801a: /* ????? */
		case 0x8018: /* draw nonzero pixels as color x-flipped */
			dma_skip = ((wms_dma_woffset + 3 - wms_dma_cols)&(~3)) + wms_dma_cols;
			DMA_DRAW_NONZERO_BYTES(wms_dma_fgcol,--);
			break;
		case 0x8022: /* draw nonzero pixels y-flipped */
			dma_skip = ((wms_dma_cols + wms_dma_woffset+3)&(~3)) - wms_dma_cols;
			DMA_DRAW_NONZERO_BYTES(write_data,++);
			break;
		case 0x8032: /* draw nonzero pixels x-flipped and y-flipped */
			dma_skip = ((wms_dma_woffset + 3 - wms_dma_cols)&(~3)) + wms_dma_cols;
			DMA_DRAW_NONZERO_BYTES(write_data,--);
			break;
		default:
			if (errorlog) fprintf(errorlog, "\nCPU #0 PC %08x: unhandled DMA command %04x:\n",cpu_get_pc(), data);
			if (errorlog) fprintf(errorlog, "%04x %04x x%04x y%04x  c%04x r%04x p%04x c%04x\n",wms_dma_subbank, wms_dma_bank, wms_dma_x, wms_dma_y, wms_dma_cols, wms_dma_rows, wms_dma_pal, wms_dma_fgcol );
			break;
		}
		/*
		 * One pixel every 41 ns (1e-9 sec)
		 */
		if (FAST_DMA)
			dma_callback(1);
		else
			timer_set(TIME_IN_NSEC(41*wms_dma_cols*wms_dma_rows), 0, dma_callback);
		break;
	case 2:
		wms_dma_woffset = data;
		break;
	case 4:
		wms_dma_subbank = data>>3;
		break;
	case 6:
		wms_dma_bank = ((data&0xfe00)?(data-0x200)*0x2000:data*0x2000);
		break;
	case 8:
		wms_dma_x = data&0x1ff;
		break;
	case 0x0a:
		wms_dma_y = data&0x1ff;
		break;
	case 0x0c:
		wms_dma_cols = data;
		dma_skip = data;
		break;
	case 0x0e:
		wms_dma_rows = data;
		break;
	case 0x10:  /* set palette */
		wms_dma_pal = (data&0xff) << 8;
		wms_dma_pal_word = data;
		break;
	case 0x12:  /* set color for 1-bit */
		wms_dma_fgcol = data&0xff;
		break;
	default:
		break;
	}
}

#define DMA2_GET_SKIPS(xdir)									\
	for (j=1;((j>=0)&&(data&0x0080));j--)						\
	{															\
		switch(wms_dma_8pos&0x07)								\
		{														\
		case 0:													\
			write_data = (*BYTE_XOR_LE(rda))&0x0f;				\
			break;												\
		case 1:													\
		case 2:													\
		case 3:													\
			write_data = (((*BYTE_XOR_LE(rda))>>(wms_dma_8pos&0x07))&0x0f); \
			break;												\
		case 4:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>4)&0x0f);		\
			break;												\
		case 5:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>5)&0x07);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<3)&0x08);		\
			break;												\
		case 6:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>6)&0x03);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<2)&0x0c);		\
			break;												\
		case 7:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>7)&0x01);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<1)&0x0e);		\
			break;												\
		}														\
		if (j)													\
		{														\
			wrva ##xdir##= wms_dma_preskip*write_data;			\
			wms_dma_temp = wms_dma_preskip*write_data;			\
		}														\
		else													\
		{														\
			write_cols_do -= ((wms_dma_postskip*write_data)+wms_dma_temp);	\
			if (write_cols_do&0x80000000) write_cols_do = 1;	\
		}														\
		wms_dma_8pos+=4;										\
	}															\

#define DMA2_DRAW_1BPP(check, writedata, advance)				\
	for (j=write_cols;j>=0;j--)									\
	{															\
		switch(wms_dma_8pos&0x07)								\
		{														\
		case 0:  /* 0x00 - 0x06 */								\
		case 1:													\
		case 2:													\
		case 3:													\
		case 4:													\
		case 5:													\
		case 6:													\
			write_data = ((*BYTE_XOR_LE(rda))>>(wms_dma_8pos&0x07))&0x01;	\
			break;												\
		case 7:  /* 0x07 */										\
			write_data = (((*BYTE_XOR_LE(rda++))>>7)&0x01);		\
			break;												\
		}														\
		if (check)												\
		{														\
			*wrva = wms_dma_pal | (writedata);					\
		}														\
		wrva##advance;											\
		wms_dma_8pos++;											\
	}															\

#define DMA2_DRAW_2BPP(check, writedata, advance)				\
	for (j=write_cols_do;j>=0;j--)								\
	{															\
		switch(wms_dma_8pos&0x07)								\
		{														\
		case 0:													\
			write_data = (*BYTE_XOR_LE(rda))&0x03;				\
			break;												\
		case 1:													\
			write_data = (((*BYTE_XOR_LE(rda))>>1)&0x03);		\
			break;												\
		case 2:													\
			write_data = (((*BYTE_XOR_LE(rda))>>2)&0x03);		\
			break;												\
		case 3:													\
			write_data = (((*BYTE_XOR_LE(rda))>>3)&0x03);		\
			break;												\
		case 4:													\
			write_data = (((*BYTE_XOR_LE(rda))>>4)&0x03);		\
			break;												\
		case 5:													\
			write_data = (((*BYTE_XOR_LE(rda))>>5)&0x03);		\
			break;												\
		case 6:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>6)&0x03);		\
			break;												\
		case 7:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>7)&0x01);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<1)&0x02);		\
			break;												\
		}														\
		if (check)												\
		{														\
			*wrva = wms_dma_pal | (writedata);					\
		}														\
		wrva##advance;											\
		wms_dma_8pos+=2;										\
	}															\

#define DMA2_DRAW_3BPP(check, writedata, advance)				\
	for (j=write_cols_do;j>=0;j--)								\
	{															\
		switch(wms_dma_8pos&0x07)								\
		{														\
		case 0:													\
			write_data = (*BYTE_XOR_LE(rda))&0x07;				\
			break;												\
		case 1:													\
			write_data = (((*BYTE_XOR_LE(rda))>>1)&0x07);		\
			break;												\
		case 2:													\
			write_data = (((*BYTE_XOR_LE(rda))>>2)&0x07);		\
			break;												\
		case 3:													\
			write_data = (((*BYTE_XOR_LE(rda))>>3)&0x07);		\
			break;												\
		case 4:													\
			write_data = (((*BYTE_XOR_LE(rda))>>4)&0x07);		\
			break;												\
		case 5:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>5)&0x07);		\
			break;												\
		case 6:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>6)&0x03);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<2)&0x04);		\
			break;												\
		case 7:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>7)&0x01);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<1)&0x06);		\
			break;												\
		}														\
		if (check)												\
		{														\
			*wrva = wms_dma_pal | (writedata);					\
		}														\
		wrva##advance;											\
		wms_dma_8pos+=3;										\
	}															\

#define DMA2_DRAW_4BPP(check, writedata, advance)				\
	for (j=write_cols_do;j>=0;j--)								\
	{															\
		switch(wms_dma_8pos&0x07)								\
		{														\
		case 0:													\
			write_data = (*BYTE_XOR_LE(rda))&0x0f;				\
			break;												\
		case 1:													\
			write_data = (((*BYTE_XOR_LE(rda))>>1)&0x0f);		\
			break;												\
		case 2:													\
			write_data = (((*BYTE_XOR_LE(rda))>>2)&0x0f);		\
			break;												\
		case 3:													\
			write_data = (((*BYTE_XOR_LE(rda))>>3)&0x0f);		\
			break;												\
		case 4:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>4)&0x0f);		\
			break;												\
		case 5:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>5)&0x07);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<3)&0x08);		\
			break;												\
		case 6:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>6)&0x03);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<2)&0x0c);		\
			break;												\
		case 7:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>7)&0x01);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<1)&0x0e);		\
			break;												\
		}														\
		if (check)												\
		{														\
			*wrva = wms_dma_pal | (writedata);					\
		}														\
		wrva##advance;											\
		wms_dma_8pos+=4;										\
	}															\

#define DMA2_DRAW_5BPP(check, writedata, advance)				\
	for (j=write_cols_do;j>=0;j--)								\
	{															\
		switch(wms_dma_8pos&0x07)								\
		{														\
		case 0:													\
			write_data = (*BYTE_XOR_LE(rda))&0x1f;				\
			break;												\
		case 1:													\
			write_data = (((*BYTE_XOR_LE(rda))>>1)&0x1f);		\
			break;												\
		case 2:													\
			write_data = (((*BYTE_XOR_LE(rda))>>2)&0x1f);		\
			break;												\
		case 3:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>3)&0x1f);		\
			break;												\
		case 4:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>4)&0x0f);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<4)&0x10);		\
			break;												\
		case 5:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>5)&0x07);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<3)&0x18);		\
			break;												\
		case 6:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>6)&0x03);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<2)&0x1c);		\
			break;												\
		case 7:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>7)&0x01);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<1)&0x1e);		\
			break;												\
		}														\
		if (check)												\
		{														\
			*wrva = wms_dma_pal | (writedata);					\
		}														\
		wrva##advance;											\
		wms_dma_8pos+=5;										\
	}															\

#define DMA2_DRAW_6BPP(check, writedata, advance)				\
	for (j=write_cols_do;j>=0;j--)								\
	{															\
		switch(wms_dma_8pos&0x07)								\
		{														\
		case 0:													\
			write_data = (*BYTE_XOR_LE(rda))&0x3f;				\
			break;												\
		case 1:													\
			write_data = (((*BYTE_XOR_LE(rda))>>1)&0x3f);		\
			break;												\
		case 2:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>2)&0x3f);		\
			break;												\
		case 3:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>3)&0x1f);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<5)&0x20);		\
			break;												\
		case 4:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>4)&0x0f);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<4)&0x30);		\
			break;												\
		case 5:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>5)&0x07);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<3)&0x38);		\
			break;												\
		case 6:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>6)&0x03);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<2)&0x3c);		\
			break;												\
		case 7:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>7)&0x01);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<1)&0x3e);		\
			break;												\
		}														\
		if (check)												\
		{														\
			*wrva = wms_dma_pal | (writedata);					\
		}														\
		wrva##advance;											\
		wms_dma_8pos+=6;										\
	}															\

#define DMA2_DRAW_7BPP(check, writedata, advance)				\
	for (j=write_cols_do;j>=0;j--)								\
	{															\
		switch(wms_dma_8pos&0x07)								\
		{														\
		case 0:													\
			write_data = (*BYTE_XOR_LE(rda))&0x7f;				\
			break;												\
		case 1:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>1)&0x7f);		\
			break;												\
		case 2:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>2)&0x3f);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<6)&0x40);		\
			break;												\
		case 3:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>3)&0x1f);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<5)&0x60);		\
			break;												\
		case 4:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>4)&0x0f);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<4)&0x70);		\
			break;												\
		case 5:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>5)&0x07);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<3)&0x78);		\
			break;												\
		case 6:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>6)&0x03);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<2)&0x7c);		\
			break;												\
		case 7:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>7)&0x01);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<1)&0x7e);		\
			break;												\
		}														\
		if (check)												\
		{														\
			*wrva = wms_dma_pal | (writedata);					\
		}														\
		wrva##advance;											\
		wms_dma_8pos+=7;										\
	}															\

#define DMA2_DRAW_8BPP(check, writedata, advance)				\
	for (j=write_cols_do;j>=0;j--)								\
	{															\
		switch(wms_dma_8pos&0x07)								\
		{														\
		case 0:													\
			write_data = (*BYTE_XOR_LE(rda++))&0xff;			\
			break;												\
		case 1:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>1)&0x7f);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<7)&0x80);		\
			break;												\
		case 2:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>2)&0x3f);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<6)&0xc0);		\
			break;												\
		case 3:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>3)&0x1f);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<5)&0xe0);		\
			break;												\
		case 4:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>4)&0x0f);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<4)&0xf0);		\
			break;												\
		case 5:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>5)&0x07);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<3)&0xf8);		\
			break;												\
		case 6:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>6)&0x03);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<2)&0xfc);		\
			break;												\
		case 7:													\
			write_data = (((*BYTE_XOR_LE(rda++))>>7)&0x01);		\
			write_data |= (((*BYTE_XOR_LE(rda))<<1)&0xfe);		\
			break;												\
		}														\
		if (check)												\
		{														\
			*wrva = wms_dma_pal | (writedata);					\
		}														\
		wrva##advance;											\
	}															\

#define DMA2_CLIP(bpp, xdir, clip)								\
	if (clip)													\
	{															\
		wms_dma_8pos&=0x07;										\
		wms_dma_8pos+=((clip)*(bpp));							\
		rda+=(wms_dma_8pos>>3);									\
		wrva##xdir##=clip;										\
	}															\

#define DMA2_DRAW(bpp, xdir, ydir, check, writedata, advance)	\
	for (i=0;i<(wms_dma_write_rows<<9);i+=512)					\
	{															\
		wrva = &(wms_videoram[(wms_dma_dst##ydir##i)&0x3ffff]);	\
		write_cols_do = write_cols;								\
		DMA2_GET_SKIPS(xdir);									\
		if ((wrva>=wrvatop)&&(wrva<=wrvabot))					\
		{														\
			DMA2_DRAW_##bpp##BPP(check, writedata, advance);	\
			DMA2_CLIP(bpp, xdir, wms_dma_postclip);				\
		}														\
		else													\
		{														\
			wms_dma_8pos&=0x07;									\
			wms_dma_8pos+=((write_cols_do+1)*bpp);				\
			rda+=(wms_dma_8pos>>3);								\
		}														\
	}															\


void wms_dma2_w(int offset, int data)
{
	/*
	 * This is a MESS! -- lots to do
	 * one pixel every 41 ns (1e-9 sec)
	 * 50,000,000 cycles per second
	 * --> approximately 2 cycles per pixel
	 */

	UINT32 i, pal, write_data=0, line_skip, dma_skip=0;
	int j, write_cols, write_cols_do;
	UINT8 *rda;
	UINT16 *wrva, *wrvatop, *wrvabot;

	switch (offset)
	{
	case 0:
		break;
	case 2:
		if (wms_dma_bclip >= 512) wms_dma_bclip = 512;
		wms_dma_write_rows = wms_dma_rows;
		wrvatop = &(wms_videoram[(wms_dma_tclip<<9)]);
		wrvabot = &(wms_videoram[(wms_dma_bclip<<9)+0x1ff]);

		write_cols = wms_dma_cols-1;
		wms_dma_postclip = (wms_dma_cols+wms_dma_x >= 512?wms_dma_cols+wms_dma_x - 512:0);
		wms_dma_preclip = 0;
		wms_dma_postclip = 0;


		wms_dma_dst = ((wms_dma_y<<9) + (wms_dma_x)); /* byte addr */

		rda = &(GFX_ROM[wms_dma_bank+wms_dma_subbank]);
		wms_dma_stat = data;
		if (wms_dma_bank+wms_dma_subbank > wms_gfx_rom_size) data = 0x8000;
		wms_dma_postskip = 1<<((data&0x0c00)>>10);
		wms_dma_preskip = 1<<((data&0x0300)>>8);

		if (0)
		{
			if (errorlog) fprintf(errorlog, "\nCPU #0 PC %08x: DMA command %04x: (offs%02x) unk2=%04x sr%04x\n",cpu_get_pc(), data, wms_dma_8pos, wms_unk2, wms_sysreg2);
			if (errorlog) fprintf(errorlog, "%08x x%04x y%04x c%04x r%04x p%04x c%04x  %04x %04x %04x %04x %04x %04x\n",wms_dma_subbank+wms_dma_bank+0x400000, wms_dma_x, wms_dma_y, wms_dma_cols, wms_dma_rows, wms_dma_pal, wms_dma_fgcol, wms_dma_14, wms_dma_16, wms_dma_tclip, wms_dma_bclip, wms_dma_1c, wms_dma_1e );
		}
		/*
		 * DMA registers
		 * ------------------
		 *
		 *  Register | Bit              | Use
		 * ----------+-FEDCBA9876543210-+------------
		 *     0     | x--------------- | trigger write (or clear if zero)
		 *           | -421------------ | image bpp (0=8)
		 *           | ----84---------- | post skip size = (1<<x)
		 *           | ------21-------- | pre skip size = (1<<x)
		 *           | --------8------- | pre/post skip enable
		 *           | ----------2----- | flip y
		 *           | -----------1---- | flip x
		 *           | ------------8--- | blit nonzero pixels as color
		 *           | -------------4-- | blit zero pixels as color
		 *           | --------------2- | blit nonzero pixels
		 *           | ---------------1 | blit zero pixels
		 *     1     | xxxxxxxxxxxxxxxx | width offset
		 *     2     | xxxxxxxxxxxxxxxx | source address low word
		 *     3     | xxxxxxxxxxxxxxxx | source address high word
		 *     4     | xxxxxxxxxxxxxxxx | detination x
		 *     5     | xxxxxxxxxxxxxxxx | destination y
		 *     6     | xxxxxxxxxxxxxxxx | image columns
		 *     7     | xxxxxxxxxxxxxxxx | image rows
		 *     8     | xxxxxxxxxxxxxxxx | palette
		 *     9     | xxxxxxxxxxxxxxxx | color
		 */
		switch(data&0xf07f)
		{
		case 0x0000: /* clear registers */
			dma_skip=0;
			wms_dma_cols=1;
			wms_dma_rows=0;
//			wms_dma_pal=0;
			wms_dma_fgcol=0;
			break;
		case 0xf000: /* draw nothing ???*/
		case 0xb000: /* draw nothing ???*/
		case 0xa000: /* draw nothing ???*/
		case 0xe000: /* draw nothing ???*/
		case 0x8000: /* draw nothing */
			break;
		case 0x8002: /* draw only nonzero pixels (8bpp) */
			DMA2_DRAW(8, +, +, write_data, write_data, ++);
			break;
		case 0x9002: /* draw only nonzero pixels (1bpp) */
			DMA2_DRAW(1, +, +, write_data, write_data, ++);
			break;
		case 0xa002: /* draw only nonzero pixels (2bpp) */
			DMA2_DRAW(2, +, +, write_data, write_data, ++);
			break;
		case 0xb002: /* draw only nonzero pixels (3bpp) */
			DMA2_DRAW(3, +, +, write_data, write_data, ++);
			break;
		case 0xc002: /* draw only nonzero pixels (4bpp) */
			DMA2_DRAW(4, +, +, write_data, write_data, ++);
			break;
		case 0xd002: /* draw only nonzero pixels (5bpp) */
			DMA2_DRAW(5, +, +, write_data, write_data, ++);
			break;
		case 0xe042: /* draw only nonzero pixels (6bpp) ????? */
			// fixme 0x0040 ?????
		case 0xe002: /* draw only nonzero pixels (6bpp) */
			DMA2_DRAW(6, +, +, write_data, write_data, ++);
			break;
		case 0xf002: /* draw only nonzero pixels (7bpp) */
			DMA2_DRAW(7, +, +, write_data, write_data, ++);
			break;
		case 0x8003: /* draw all pixels */
			dma_skip = ((wms_dma_cols + wms_dma_woffset)) - wms_dma_cols;
			DMA_DRAW_ALL_BYTES(*BYTE_XOR_LE(rda++));
			if (errorlog&&((data&0x0080)||(wms_dma_8pos&0x07))) fprintf(errorlog, "\nCPU #0 PC %08x: unhandled DMA command %04x:  (off%x)\n",cpu_get_pc(), data, wms_dma_8pos&0x07);
			break;
		case 0x8008: /* draw nonzero pixels as color (8bpp) */
			DMA2_DRAW(8, +, +, write_data, wms_dma_fgcol, ++);
			break;
		case 0x9008: /* draw nonzero pixels as color (1bpp) */
			DMA2_DRAW(1, +, +, write_data, wms_dma_fgcol, ++);
			break;
		case 0xc008: /* draw nonzero pixels as color (4bpp) */
			DMA2_DRAW(4, +, +, write_data, wms_dma_fgcol, ++);
			break;
		case 0xe008: /* draw nonzero pixels as color (6bpp) */
			DMA2_DRAW(6, +, +, write_data, wms_dma_fgcol, ++);
			break;
		case 0x8009: /* draw nonzero pixels as color, zero as zero */
			dma_skip = ((wms_dma_cols + wms_dma_woffset)) - wms_dma_cols;
			DMA_DRAW_ALL_BYTES((*BYTE_XOR_LE(rda++)?wms_dma_fgcol:0));
			if (errorlog&&((data&0x0080)||(wms_dma_8pos&0x07))) fprintf(errorlog, "\nCPU #0 PC %08x: unhandled DMA command %04x:  (off%x)\n",cpu_get_pc(), data, wms_dma_8pos&0x07);
			break;
		case 0xe00c: /* draw all pixels as color (fill) */
		case 0xd00c: /* draw all pixels as color (fill) */
		case 0xc00c: /* draw all pixels as color (fill) */
		case 0xb00c: /* draw all pixels as color (fill) */
		case 0xa00c: /* draw all pixels as color (fill) */
		case 0x900c: /* draw all pixels as color (fill) */
		case 0x800c: /* draw all pixels as color (fill) */
			DMA_DRAW_ALL_BYTES(wms_dma_fgcol);
			break;
		case 0x8010: /* draw nothing ???? */
			break;
		case 0x8012: /* draw nonzero pixels x-flipped (8bpp) */
			DMA2_DRAW(8, -, +, write_data, write_data, --);
			break;
		case 0xc012: /* draw nonzero pixels x-flipped (4bpp) */
			DMA2_DRAW(4, -, +, write_data, write_data, --);
			break;
		case 0xd012: /* draw nonzero pixels x-flipped (5bpp) */
			DMA2_DRAW(5, -, +, write_data, write_data, --);
			break;
		case 0xe012: /* draw nonzero pixels x-flipped (6bpp) */
			DMA2_DRAW(6, -, +, write_data, write_data, --);
			break;
		case 0x8013: /* draw all pixels x-flipped (8bpp) */
			dma_skip = ((wms_dma_woffset - wms_dma_cols)) + wms_dma_cols;
			DMA_DRAW_ALL_BYTES(*BYTE_XOR_LE(rda--));
			if (errorlog&&((data&0x0080)||(wms_dma_8pos&0x07))) fprintf(errorlog, "\nCPU #0 PC %08x: unhandled DMA command %04x:  (off%x)\n",cpu_get_pc(), data, wms_dma_8pos&0x07);
			break;
		case 0x8018: /* draw nonzero pixels as color x-flipped (8bpp)*/
			DMA2_DRAW(8, -, +, write_data, wms_dma_fgcol, --);
			break;
		case 0xe018: /* draw only nonzero pixels (6bpp) */
			DMA2_DRAW(6, -, +, write_data, wms_dma_fgcol, --);
			break;
		case 0x8022: /* draw nonzero pixels y-flipped (8bpp) */
			DMA2_DRAW(8, +, -, write_data, write_data, ++);
			break;
		case 0xe022: /* draw nonzero pixels y-flipped (6bpp) */
			DMA2_DRAW(6, +, -, write_data, write_data, ++);
			break;
		case 0x8032: /* draw nonzero pixels x- and y-flipped (8pp) */
			DMA2_DRAW(8, -, -, write_data, write_data, --);
			break;
		case 0xe032: /* draw nonzero pixels x- and y-flipped (6bpp) */
			DMA2_DRAW(6, -, -, write_data, write_data, --);
			break;
		default:
			if (errorlog) fprintf(errorlog, "\nCPU #0 PC %08x: unhandled DMA command %04x:\n",cpu_get_pc(), data);
			if (errorlog) fprintf(errorlog, "%08x x%04x y%04x c%04x r%04x p%04x c%04x  %04x %04x %04x %04x %04x %04x\n",wms_dma_subbank+wms_dma_bank+0x400000, wms_dma_x, wms_dma_y, wms_dma_cols, wms_dma_rows, wms_dma_pal, wms_dma_fgcol, wms_dma_14, wms_dma_16, wms_dma_tclip, wms_dma_bclip, wms_dma_1c, wms_dma_1e );
			break;
		}
		if (FAST_DMA)
			dma_callback(1);
		else
			timer_set(TIME_IN_NSEC(41*wms_dma_cols*wms_dma_rows), 0, dma_callback);
		break;
	case 4:
		wms_dma_subbank = data>>3;
		wms_dma_8pos = data&0x07;
		break;
	case 6:
		wms_dma_bank = ((data&0xfe00)?(data-0x200)*0x2000:data*0x2000);
		break;
	case 8:
		wms_dma_x = data&0x1ff;
		break;
	case 0x0a:
		wms_dma_y = data&0x1ff;
		break;
	case 0x0c:
		wms_dma_cols = data;
		dma_skip = data;
		break;
	case 0x0e:
		wms_dma_rows = data;
		break;
	case 0x10:  /* set palette */
		wms_dma_pal = (data&0xff00);  /* changed from rev1? */
//		wms_dma_pal = (data&0xffff);
		wms_dma_pal_word = data;
		break;
	case 0x12:  /* set color for 1-bit */
		wms_dma_fgcol = data&0xff;
		break;
	case 0x14:
		wms_dma_14 = data;
		break;
	case 0x16:
		wms_dma_16 = data;
		break;
	case 0x18:
		wms_dma_tclip = data;
		break;
	case 0x1a:
		wms_dma_bclip = data;
		break;
	case 0x1c:
		wms_dma_1c = data;
		break;
	case 0x1e:
		wms_dma_1e = data;
		break;
	default:
		if (errorlog) fprintf(errorlog,"dma offset %x: %x\n",offset, data);
		break;
	}
}

void wms_to_shiftreg(UINT32 address, UINT16* shiftreg)
{
	memcpy(shiftreg, &wms_videoram[address>>3], 2*512*sizeof(UINT16));
}

void wms_from_shiftreg(UINT32 address, UINT16* shiftreg)
{
	memcpy(&wms_videoram[address>>3], shiftreg, 2*512*sizeof(UINT16));
}

void wms_01c00060_w(int offset, int data) /* protection and more */
{
	if ((data&0xfdff) == 0x0000) /* enable CMOS write */
	{
		smashtv_cmos_w_enable = 1;
//		if (errorlog) fprintf(errorlog, "Enable CMOS writes\n");
	}
	else if ((data&0xfdff) == 0x0100) /* disable CMOS write */
	{
//		smashtv_cmos_w_enable = 0;
//		if (errorlog) fprintf(errorlog, "Disable CMOS writes\n");
	}
	else
	if (errorlog) fprintf(errorlog, "CPU #0 PC %08x: warning - write %x to protection chip\n", cpu_get_pc(), data);
}
int wms_01c00060_r(int offset) /* protection and more */
{
	if (cpu_get_pc() == wms_protect_s) /* protection */
	{
		cpu_set_pc(wms_protect_d); /* skip it! */
		return 0xffffffff;
	}
	if (errorlog) fprintf(errorlog, "CPU #0 PC %08x: warning - unhandled read from protection chip\n",cpu_get_pc());
	return 0xffffffff;
}

void wms_sysreg_w(int offset, int data)
{
	/*
	 * Narc system register
	 * ------------------
	 *
	 *   | Bit              | Use
	 * --+-FEDCBA9876543210-+------------
	 *   | xxxxxxxx-------- |   7 segment led on CPU board
	 *   | --------xx------ |   CMOS page
	 *   | ----------x----- | - OBJ PAL RAM select
	 *   | -----------x---- | - autoerase enable
	 *   | ---------------- | - watchdog
	 *
	 */
	wms_cmos_page = (data&0xc0)<<7; /* 0x2000 offsets */
	if((data&0x20)&&wms_objpalram_select) /* access VRAM */
	{
		install_mem_write_handler(0, TOBYTE(0x00000000), TOBYTE(0x001fffff), wms_vram_w);
		install_mem_read_handler (0, TOBYTE(0x00000000), TOBYTE(0x001fffff), wms_vram_r);
	}
	else if(!(data&0x20)&&!wms_objpalram_select) /* access OBJPALRAM */
	{
		install_mem_write_handler(0, TOBYTE(0x00000000), TOBYTE(0x001fffff), wms_objpalram_w);
		install_mem_read_handler (0, TOBYTE(0x00000000), TOBYTE(0x001fffff), wms_objpalram_r);
	}
	wms_objpalram_select = ((data&0x20)?0:0x40000);

	if (data&0x10) /* turn off auto-erase */
	{
		if (wms_autoerase_enable)
			wms_update_partial(cpu_getscanline());
		wms_autoerase_enable = 0;
	}
	else /* enable auto-erase */
	{
		if (!wms_autoerase_enable)
			wms_update_partial(cpu_getscanline());
		wms_autoerase_enable = 1;
	}
}

void wms_sysreg2_w(int offset, int data)
{
	wms_sysreg2 = data;
//	wms_cmos_page = (data&0xc0)<<8; /* 0x4000 offsets */
	wms_cmos_page = 0; /* 0x4000 offsets */
	if(data&0x20&&wms_objpalram_select) /* access VRAM */
	{
		install_mem_write_handler(0, TOBYTE(0x00000000), TOBYTE(0x003fffff), wms_vram_w);
		install_mem_read_handler (0, TOBYTE(0x00000000), TOBYTE(0x003fffff), wms_vram_r);
	}
	else if(!(data&0x20)&&!wms_objpalram_select) /* access OBJPALRAM */
	{
		install_mem_write_handler(0, TOBYTE(0x00000000), TOBYTE(0x003fffff), wms_objpalram_w);
		install_mem_read_handler (0, TOBYTE(0x00000000), TOBYTE(0x003fffff), wms_objpalram_r);
	}
	wms_objpalram_select = ((data&0x20)?0:0x40000);
//	if (data&0x10) /* turn off auto-erase */
//	{
//		wms_autoerase_enable = 0;
//	}
//	else /* enable auto-erase */
//	{
//		wms_autoerase_enable = 1;
//		wms_autoerase_start  = cpu_getscanline();
//	}
}

void wms_unk1_w(int offset, int data)
{
//	INT8 buf[80];
	wms_unk1 = data;
	if (data == 0x4472)
	{
		/* 0x4472 --> load ug14, ug16  */
		cpu_bankbase[8] = &(gfxrombackup[0x000000]);
	}
	else if (data == 0x44f2)
	{
		/* 0x44f2 --> load ug17 low ?*/
		cpu_bankbase[8] = &(gfxrombackup[0x400000]);
	}
	else
	/* 0x00f2 --> load ug17 high */
	{
		cpu_bankbase[8] = &(gfxrombackup[0x000000]);
	}
	wms_sysreg2 = data;
//	wms_cmos_page = (data&0xc0)<<8; /* 0x4000 offsets */
	wms_cmos_page = 0; /* 0x4000 offsets */
	if(data&0x20&&wms_objpalram_select) /* access VRAM */
	{
		install_mem_write_handler(0, TOBYTE(0x00000000), TOBYTE(0x003fffff), wms_vram_w);
		install_mem_read_handler (0, TOBYTE(0x00000000), TOBYTE(0x003fffff), wms_vram_r);
	}
	else if(!(data&0x20)&&!wms_objpalram_select) /* access OBJPALRAM */
	{
		install_mem_write_handler(0, TOBYTE(0x00000000), TOBYTE(0x003fffff), wms_objpalram_w);
		install_mem_read_handler (0, TOBYTE(0x00000000), TOBYTE(0x003fffff), wms_objpalram_r);
	}
	wms_objpalram_select = ((data&0x20)?0:0x40000);
//	if (data&0x10) /* turn off auto-erase */
//	{
//		wms_autoerase_enable = 0;
//	}
//	else /* enable auto-erase */
//	{
//		wms_autoerase_enable = 1;
//		wms_autoerase_start  = cpu_getscanline();
//	}

//	if (errorlog) fprintf(errorlog, "wms_unk1:       %04x\n", data);
//#ifdef MAME_DEBUG
//	sprintf(buf,"write: 0x%04x",data);
//	usrintf_showmessage(buf);
//#endif
}

extern int debug_key_pressed;
void wms_unk2_w(int offset, int data)
{
//	INT8 buf[80];
	if (offset==2)
	{
		wms_unk2 = data;
//#ifdef MAME_DEBUG
//		sprintf(buf,"unk2 write: 0x%04x",data);
//		usrintf_showmessage(buf);
//#endif
	}
//	if (offset == 2) debug_key_pressed = 1;
	if (errorlog&&(offset!=6)) fprintf(errorlog, "wms_unk2(%04x): %04x\n", offset<<3, data);
}

static int narc_unknown_r (int offset)
{
	int ans = 0xffff;
	return ans;
}

void wms_cmos_w(int offset, int data)
{
	if (smashtv_cmos_w_enable)
	{
		COMBINE_WORD_MEM(&wms_cmos_ram[(offset)+wms_cmos_page], data);
	}
	else
	if (errorlog) fprintf(errorlog, "CPU #0 PC %08x: warning - write %x to disabled CMOS address %x\n", cpu_get_pc(), data, offset);

}
int wms_cmos_r(int offset)
{
	return READ_WORD(&wms_cmos_ram[(offset)+wms_cmos_page]);
}

void wms_load_code_roms(void)
{
	memcpy(CODE_ROM,memory_region(REGION_CPU1),wms_code_rom_size);
}


#define READ_INT8(REG)			(*(INT8 *)BYTE_XOR_LE(&SCRATCH_RAM[TOBYTE((REG) & 0xffffff)]))
#define READ_INT16(REG)			(*(INT16 *)&SCRATCH_RAM[TOBYTE((REG) & 0xffffff)])
#define READ_INT32(REG)			BIG_DWORD_LE(*(INT32 *)&SCRATCH_RAM[TOBYTE((REG) & 0xffffff)])
#define WRITE_INT8(REG,DATA)	(*(INT8 *)BYTE_XOR_LE(&SCRATCH_RAM[TOBYTE((REG) & 0xffffff)]) = (DATA))
#define WRITE_INT16(REG,DATA)	(*(INT16 *)&SCRATCH_RAM[TOBYTE((REG) & 0xffffff)] = (DATA))
#define WRITE_INT32(REG,DATA)	(*(INT32 *)&SCRATCH_RAM[TOBYTE((REG) & 0xffffff)] = BIG_DWORD_LE(DATA))

#define BURN_TIME(INST_CNT)		tms34010_ICount -= INST_CNT * TMS34010_AVGCYCLES

/* Speed up loop body */

#define DO_SPEEDUP_LOOP(OFFS1, OFFS2, A8SIZE, A7SIZE)		\
															\
	a8 = READ_##A8SIZE(a2+OFFS1);							\
	a7 = READ_##A7SIZE(a2+OFFS2);							\
															\
	if (a8 > a1) 											\
	{ 														\
		a4 = a0; 											\
		a0 = a2; 											\
		a1 = a8; 											\
		a5 = a7; 											\
        BURN_TIME(10);										\
		continue; 											\
	} 														\
															\
	if ((a8 == a1) && (a7 >= a5)) 							\
	{ 														\
		a4 = a0; 											\
		a0 = a2; 											\
		a1 = a8; 											\
		a5 = a7; 											\
        BURN_TIME(13);										\
		continue; 											\
	} 														\
															\
	WRITE_INT32(a4, a2);									\
	WRITE_INT32(a0, READ_INT32(a2));						\
	WRITE_INT32(a2, a0);									\
	a4 = a2; 												\
	BURN_TIME(17);


/* Speedup catch for games using 1 location */

#define DO_SPEEDUP_LOOP_1(LOC, OFFS1, OFFS2, A8SIZE, A7SIZE)	\
															\
	UINT32 a0 = LOC;										\
	UINT32 a2;												\
	UINT32 a4 = 0;											\
	 INT32 a1 = 0x80000000;									\
	 INT32 a5 = 0x80000000;									\
	 INT32 a7,a8;											\
	while (tms34010_ICount > 0) 							\
	{														\
		a2 = READ_INT32(a0);								\
		if (!a2)											\
		{													\
			cpu_spinuntil_int();							\
			break;											\
		}													\
		DO_SPEEDUP_LOOP(OFFS1, OFFS2, A8SIZE, A7SIZE);		\
	}


/* Speedup catch for games using 3 locations */

#define DO_SPEEDUP_LOOP_3(LOC1, LOC2, LOC3)					\
															\
	UINT32 a0,a2,temp1,temp2,temp3;							\
	UINT32 a4 = 0;                               			\
	 INT32 a1,a5,a7,a8;										\
															\
	while (tms34010_ICount > 0) 							\
	{														\
		temp1 = READ_INT32(LOC1);							\
		temp2 = READ_INT32(LOC2);							\
		temp3 = READ_INT32(LOC3);							\
		if (!temp1 && !temp2 && !temp3)						\
		{													\
			cpu_spinuntil_int();							\
			break;											\
		}													\
		a0 = LOC1;											\
		a1 = 0x80000000;									\
		a5 = 0x80000000;									\
		do													\
		{													\
			a2 = READ_INT32(a0);							\
			if (!a2)										\
			{												\
				tms34010_ICount -= 20;						\
				break;										\
			}												\
			DO_SPEEDUP_LOOP(0xc0, 0xa0, INT32, INT32);		\
		} while (a2);										\
															\
		a0 = LOC2;											\
		a1 = 0x80000000;									\
		a5 = 0x80000000;									\
		do													\
		{													\
			a2 = READ_INT32(a0);							\
			if (!a2)										\
			{												\
				tms34010_ICount -= 20;						\
				break;										\
			}												\
			DO_SPEEDUP_LOOP(0xc0, 0xa0, INT32, INT32);		\
		} while (a2);										\
															\
		a0 = LOC3;											\
		a1 = 0x80000000;									\
		a5 = 0x80000000;									\
		do													\
		{													\
			a2 = READ_INT32(a0);							\
			if (!a2)										\
			{												\
				tms34010_ICount -= 20;						\
				break;										\
			}												\
			DO_SPEEDUP_LOOP(0xc0, 0xa0, INT32, INT32);		\
		} while (a2);										\
	}


static int narc_speedup_r(int offset)
{
	if (offset)
	{
		UINT32 value1 = READ_WORD(&SCRATCH_RAM[TOBYTE(0x1b310)]);

		/* Suspend cpu if it's waiting for an interrupt */
		if (cpu_get_pc() == 0xffde33e0 && !value1)
		{
			DO_SPEEDUP_LOOP_1(0x1000040, 0xc0, 0xa0, INT32, INT32);
		}
		return value1;
	}
	else
	{
		return READ_WORD(&SCRATCH_RAM[TOBYTE(0x1b300)]);
	}
}
static int smashtv_speedup_r(int offset)
{
	if (offset)
	{
		return READ_WORD(&SCRATCH_RAM[TOBYTE(0x86770)]);
	}
	else
	{
		UINT32 value1 = READ_WORD(&SCRATCH_RAM[TOBYTE(0x86760)]);

		/* Suspend cpu if it's waiting for an interrupt */
		if (cpu_get_pc() == 0xffe0a340 && !value1)
		{
			DO_SPEEDUP_LOOP_1(0x1000040, 0xa0, 0x80, INT16, INT32);
		}
		return value1;
	}
}
static int smashtv4_speedup_r(int offset)
{
	if (offset)
	{
		UINT32 value1 = READ_WORD(&SCRATCH_RAM[TOBYTE(0x86790)]);

		/* Suspend cpu if it's waiting for an interrupt */
		if (cpu_get_pc() == 0xffe0a320 && !value1)
		{
			DO_SPEEDUP_LOOP_1(0x1000040, 0xa0, 0x80, INT16, INT32);
		}
		return value1;
	}
	else
	{
		return READ_WORD(&SCRATCH_RAM[TOBYTE(0x86780)]);
	}
}
static int totcarn_speedup_r(int offset)
{
	if (offset)
	{
		return READ_WORD(&SCRATCH_RAM[TOBYTE(0x7ddf0)]);
	}
	else
	{
		UINT32 value1 = READ_WORD(&SCRATCH_RAM[TOBYTE(0x7dde0)]);

		/* Suspend cpu if it's waiting for an interrupt */
		if (cpu_get_pc() == 0xffc0c970 && !value1)
		{
			DO_SPEEDUP_LOOP_1(0x1000040, 0xa0, 0x90, INT16, INT16);
		}
		return value1;
	}
}
static int trogp_speedup_r(int offset)
{
	if (offset)
	{
		return READ_WORD(&SCRATCH_RAM[TOBYTE(0xa1ef0)]);
	}
	else
	{
		UINT32 value1 = READ_WORD(&SCRATCH_RAM[TOBYTE(0xa1ee0)]);

		/* Suspend cpu if it's waiting for an interrupt */
		if (cpu_get_pc() == 0xffe210d0 && !value1)
		{
			DO_SPEEDUP_LOOP_1(0x1000040, 0xc0, 0xa0, INT32, INT32);
		}
		return value1;
	}
}
static int trog_speedup_r(int offset)
{
	if (offset)
	{
		return READ_WORD(&SCRATCH_RAM[TOBYTE(0xa20b0)]);
	}
	else
	{
		UINT32 value1 = READ_WORD(&SCRATCH_RAM[TOBYTE(0xa20a0)]);

		/* Suspend cpu if it's waiting for an interrupt */
		if (cpu_get_pc() == 0xffe20630 && !value1)
		{
			DO_SPEEDUP_LOOP_1(0x1000040, 0xc0, 0xa0, INT32, INT32);
		}
		return value1;
	}
}
static int trog3_speedup_r(int offset)
{
	if (offset)
	{
		UINT32 value1 = READ_WORD(&SCRATCH_RAM[TOBYTE(0xa2090)]);

		/* Suspend cpu if it's waiting for an interrupt */
		if (cpu_get_pc() == 0xffe20660 && !value1)
		{
			DO_SPEEDUP_LOOP_1(0x1000040, 0xc0, 0xa0, INT32, INT32);
		}
		return value1;
	}
	else
	{
		return READ_WORD(&SCRATCH_RAM[TOBYTE(0xa2080)]);
	}
}
static int mk_speedup_r(int offset)
{
	if (offset)
	{
		return READ_WORD(&SCRATCH_RAM[TOBYTE(0x53370)]);
	}
	else
	{
		UINT32 value1 = READ_WORD(&SCRATCH_RAM[TOBYTE(0x53360)]);

		/* Suspend cpu if it's waiting for an interrupt */
		if (cpu_get_pc() == 0xffce2100 && !value1)
		{
			DO_SPEEDUP_LOOP_3(0x104f9d0, 0x104fa10, 0x104fa30);
		}
		return value1;
	}
}
static int mkla1_speedup_r(int offset)
{
	if (offset)
	{
		return READ_WORD(&SCRATCH_RAM[TOBYTE(0x4f010)]);
	}
	else
	{
		UINT32 value1 = READ_WORD(&SCRATCH_RAM[TOBYTE(0x4f000)]);

		/* Suspend cpu if it's waiting for an interrupt */
		if (cpu_get_pc() == 0xffcddc00 && !value1)
		{
			DO_SPEEDUP_LOOP_3(0x104b6b0, 0x104b6f0, 0x104b710);
		}
		return value1;
	}
}
static int mkla2_speedup_r(int offset)
{
	if (offset)
	{
		return READ_WORD(&SCRATCH_RAM[TOBYTE(0x4f030)]);
	}
	else
	{
		UINT32 value1 = READ_WORD(&SCRATCH_RAM[TOBYTE(0x4f020)]);

		/* Suspend cpu if it's waiting for an interrupt */
		if (cpu_get_pc() == 0xffcde000 && !value1)
		{
			DO_SPEEDUP_LOOP_3(0x104b6b0, 0x104b6f0, 0x104b710);
		}
		return value1;
	}
}
static int mkla3_speedup_r(int offset)
{
	if (offset)
	{
		return READ_WORD(&SCRATCH_RAM[TOBYTE(0x4f050)]);
	}
	else
	{
		UINT32 value1 = READ_WORD(&SCRATCH_RAM[TOBYTE(0x4f040)]);

		/* Suspend cpu if it's waiting for an interrupt */
		if (cpu_get_pc() == 0xffce1ec0 && !value1)
		{
			DO_SPEEDUP_LOOP_3(0x104b6b0, 0x104b6f0, 0x104b710);
		}
		return value1;
	}
}
static int mkla4_speedup_r(int offset)
{
	if (!offset)
	{
		return READ_WORD(&SCRATCH_RAM[TOBYTE(0x4f040)]);
	}
	else
	{
		UINT32 value1 = READ_WORD(&SCRATCH_RAM[TOBYTE(0x4f050)]);

		/* Suspend cpu if it's waiting for an interrupt */
		if (cpu_get_pc() == 0xffce21d0 && !value1)
		{
			DO_SPEEDUP_LOOP_3(0x104b6b0, 0x104b6f0, 0x104b710);
		}
		return value1;
	}
}
static int hiimpact_speedup_r(int offset)
{
	if (!offset)
	{
		return READ_WORD(&SCRATCH_RAM[TOBYTE(0x53140)]);
	}
	else
	{
		UINT32 value1 = READ_WORD(&SCRATCH_RAM[TOBYTE(0x53150)]);

		/* Suspend cpu if it's waiting for an interrupt */
		if (cpu_get_pc() == 0xffe28bb0 && !value1)
		{
			DO_SPEEDUP_LOOP_3(0x1000080, 0x10000a0, 0x10000c0);
		}
		return value1;
	}
}
static int shimpact_speedup_r(int offset)
{
	if (!offset)
	{
		return READ_WORD(&SCRATCH_RAM[TOBYTE(0x52060)]);
	}
	else
	{
		UINT32 value1 = READ_WORD(&SCRATCH_RAM[TOBYTE(0x52070)]);

		/* Suspend cpu if it's waiting for an interrupt */
		if (cpu_get_pc() == 0xffe27f00 && !value1)
		{
			DO_SPEEDUP_LOOP_3(0x1000000, 0x1000020, 0x1000040);
		}
		return value1;
	}
}

#define T2_FFC08C40																\
	a5x = (INT32)(READ_INT8(a1+0x2d0));				/* MOVB   *A1(2D0h),A5  */  \
	WRITE_INT8(a1+0x2d0, a2 & 0xff);				/* MOVB   A2,*A1(2D0h)  */  \
	a3x = 0xf0;										/* MOVI   F0h,A3		*/	\
	a5x = (UINT32)a5x * (UINT32)a3x;				/* MPYU   A3,A5			*/	\
	a5x += 0x1008000;								/* ADDI   1008000h,A5	*/  \
	a3x = (UINT32)a2  * (UINT32)a3x;				/* MPYU   A2,A3			*/	\
	a3x += 0x1008000;								/* ADDI   1008000h,A3	*/  \
	a7x = (INT32)(READ_INT16(a1+0x190));			/* MOVE   *A1(190h),A7,0*/	\
	a6x = (INT32)(READ_INT16(a5x+0x50));			/* MOVE   *A5(50h),A6,0 */	\
	a7x -= a6x;										/* SUB    A6,A7			*/  \
	a6x = (INT32)(READ_INT16(a3x+0x50));			/* MOVE   *A3(50h),A6,0 */	\
	a7x += a6x;										/* ADD    A6,A7			*/  \
	WRITE_INT16(a1+0x190, a7x & 0xffff);			/* MOVE   A7,*A1(190h),0*/	\
	a5x = READ_INT32(a5x+0xa0);						/* MOVE   *A5(A0h),A5,1 */	\
	a3x = READ_INT32(a3x+0xa0);						/* MOVE   *A3(A0h),A3,1 */	\
	a6x = READ_INT32(a1+0x140);						/* MOVE   *A1(140h),A6,1*/	\
	a6xa7x = (INT64)a6x * a3x / a5x;				/* MPYS   A3,A6			*/  \
													/* DIVS   A5,A6			*/  \
	WRITE_INT32(a1+0x140, a6xa7x & 0xffffffff);		/* MOVE   A6,*A1(140h),1*/

static int term2_speedup_r(int offset)
{
	if (offset)
	{
		return READ_WORD(&SCRATCH_RAM[TOBYTE(0xaa050)]);
	}
	else
	{
		UINT32 value1 = READ_WORD(&SCRATCH_RAM[TOBYTE(0xaa040)]);

		/* Suspend cpu if it's waiting for an interrupt */
		if (cpu_get_pc() == 0xffcdc270 && !value1)
		{
			INT32 a0,a1,a2,a3,a4,a5,a6,a7,a8,a9,a10,a14,b0,b1,b2;
			INT32 a3x,a5x,a6x,a7x;
			INT64 a6xa7x;

			b1 = 0;			 									// CLR    B1
			b2 = (INT32)(READ_INT16(0x100F640));				// MOVE   @100F640h,B2,0
			if (!b2)											// JREQ   FFC029F0h
			{
				cpu_spinuntil_int();
				return value1;
			}
			b2--;												// DEC    B2
			b0  = 0x01008000;									// MOVI   1008000h,B0
			a4  = 0x7fffffff;									// MOVI   7FFFFFFFh,A4

			while (1)
			{
				// FFC07770
				a10 = b0;										// MOVE   B0,A10
				a0  = a10;										// MOVE   A10,A0
				a3  = a4;										// MOVE   A4,A3
																// CMP    B2,B1
				if (b1 < b2)									// JRLT   FFC07800h
				{
					// FFC07800
					a4 = (INT32)(READ_INT16(a10+0xc0));			// MOVE   *A10(C0h),A4,0
					a4 <<= 16;									// SLL    10h,A4
				}
				else
				{
					// FFC077C0
					a4 = 0x80000000;							// MOVI   80000000h,A4
				}												// JR     FFC07830h

				// FFC07830
				b0 += 0xf0;										// ADDI   F0h,B0
				a6 = 0x80000000;								// MOVI   80000000h,A6
				a5 = 0x80000000;								// MOVI   80000000h,A5
				goto t2_FFC07DD0;								// JR     FFC07DD0h

			t2_FFC078C0:
				a8  = READ_INT32(a1+0x1c0);						// MOVE   *A1(1C0h),A8,1
				a7  = READ_INT32(a1+0x1a0);						// MOVE   *A1(1A0h),A7,1
				a14 = (INT32)(READ_INT16(a1+0x220));			// MOVE   *A1(220h),A14,0
				if (a14 & 0x6000)								// BTST   Eh,A14
				{												// JRNE   FFC07C50h
					goto t2_FFC07C50;							// BTST   Dh,A14
				}												// JRNE   FFC07C50h

				if (a8 <= a3)									// CMP    A3,A8
				{
					goto t2_FFC07AE0;							// JRLE   FFC07AE0h
				}

				a2 = b1 - 1;									// MOVE   B1,A2;  DEC    A2
				T2_FFC08C40										// CALLR  FFC08C40h
				a14 = READ_INT32(a1);							// MOVE   *A1,A14,1
				WRITE_INT32(a0, a14);							// MOVE   A14,*A0,1
				WRITE_INT32(a14+0x20, a0);						// MOVE   A0,*A14(20h),1
				a14 = b0 - 0x1e0;								// MOVE   B0,A14; SUBI   1E0h,A14
				WRITE_INT32(a1+0x20, a14);						// MOVE   A14,*A1(20h),1
				a9 = READ_INT32(a14);							// MOVE   *A14,A9,1
				WRITE_INT32(a14, a1);							// MOVE   A1,*A14,1
				WRITE_INT32(a9+0x20, a1);						// MOVE   A1,*A9(20h),1
				WRITE_INT32(a1, a9);							// MOVE   A9,*A1,1
				goto t2_FFC07DD0;								// JR     FFC07DD0h

			t2_FFC07AE0:
				if (a8 >= a4)									// CMP    A4,A8
				{
					goto t2_FFC07C50;							// JRGE   FFC07C50h
				}

				a2 = b1 + 1;									// MOVE   B1,A2; INC    A2
				T2_FFC08C40										// CALLR  FFC08C40h
				a14 = READ_INT32(a1);							// MOVE   *A1,A14,1
				WRITE_INT32(a0, a14);							// MOVE   A14,*A0,1
				WRITE_INT32(a14+0x20, a0);						// MOVE   A0,*A14(20h),1
				a14 = b0;										// MOVE   B0,A14
				a9 = READ_INT32(a14+0x20);						// MOVE   *A14(20h),A9,1
				WRITE_INT32(a1, a14);							// MOVE   A14,*A1,1
				WRITE_INT32(a14+0x20, a1);						// MOVE   A1,*A14(20h),1
				WRITE_INT32(a9, a1);							// MOVE   A1,*A9,1
				WRITE_INT32(a1+0x20, a9);						// MOVE   A9,*A1(20h),1
				goto t2_FFC07DD0;

			t2_FFC07C50:
				if (a8 > a6) 									// CMP    A6,A8
				{
					a1 = a0; 									// MOVE   A1,A0
					a6 = a8; 									// MOVE   A8,A6
					a5 = a7; 									// MOVE   A7,A5
					goto t2_FFC07DD0;
				}

				if ((a8 == a6) && (a7 >= a5)) 					// CMP    A5,A7
				{
					a1 = a0; 									// MOVE   A1,A0
					a6 = a8; 									// MOVE   A8,A6
					a5 = a7; 									// MOVE   A7,A5
					goto t2_FFC07DD0;
				}

				// FFC07CC0
				a14 = READ_INT32(a0+0x20);						// MOVE   *A0(20h),A14,1
				WRITE_INT32(a14, a1);							// MOVE   A1,*A14,1
				WRITE_INT32(a1+0x20, a14);						// MOVE   A14,*A1(20h),1
				a14 = READ_INT32(a1);							// MOVE   *A1,A14,1
				WRITE_INT32(a0, a14);							// MOVE   A14,*A0,1
				WRITE_INT32(a1, a0);							// MOVE   A0,*A1,1
				WRITE_INT32(a0 +0x20, a1);						// MOVE   A1,*A0(20h),1
				WRITE_INT32(a14+0x20, a0);						// MOVE   A0,*A14(20h),1

			t2_FFC07DD0:
				BURN_TIME(50);
				if (tms34010_ICount <= 0)
				{
					break;
				}

				a1 = READ_INT32(a0);							// MOVE   *A0,A1,1
				if (a10 != a1)									// CMP    A1,A10
				{
					goto t2_FFC078C0;							// JRNE   FFC078C0h
				}

				b1++;											// INC    B1
				if (b1 > b2)									// CMP    B2,B1
				{
					cpu_spinuntil_int();
					return value1;								// JRLE   FFC07770h; RTS
				}
			}
		}

		return value1;
	}
}
int strkforc_speedup_r(int offset)
{
	if (!offset)
	{
		return READ_WORD(&SCRATCH_RAM[TOBYTE(0x71dc0)]);
	}
	else
	{
		UINT32 value1 = READ_WORD(&SCRATCH_RAM[TOBYTE(0x71dd0)]);

		/* Suspend cpu if it's waiting for an interrupt */
		if (cpu_get_pc() == 0xffe0a290 && !value1)
		{
			DO_SPEEDUP_LOOP_1(0x1000060, 0xc0, 0xa0, INT32, INT32);
		}
		return value1;
	}
}
static int mk2_speedup_r(int offset)
{
	if (!offset)
	{
		return READ_WORD(&SCRATCH_RAM[TOBYTE(0x68e60)]);
	}
	else
	{
		UINT32 value1 = READ_WORD(&SCRATCH_RAM[TOBYTE(0x68e70)]);

		/* Suspend cpu if it's waiting for an interrupt */
		if (cpu_get_pc() == 0xff80db70 && !value1)
		{
			DO_SPEEDUP_LOOP_3(0x105d480, 0x105d4a0, 0x105d4c0);
		}
		return value1;
	}
}
static int mk2r14_speedup_r(int offset)
{
	if (offset)
	{
		return READ_WORD(&SCRATCH_RAM[TOBYTE(0x68df0)]);
	}
	else
	{
		UINT32 value1 = READ_WORD(&SCRATCH_RAM[TOBYTE(0x68de0)]);

		/* Suspend cpu if it's waiting for an interrupt */
		if (cpu_get_pc() == 0xff80d960 && !value1)
		{
			DO_SPEEDUP_LOOP_3(0x105d480, 0x105d4a0, 0x105d4c0);
		}
		return value1;
	}
}
static int nbajam_speedup_r(int offset)
{
	if (offset)
	{
		return READ_WORD(&SCRATCH_RAM[TOBYTE(0x754d0)]);
	}
	else
	{
		UINT32 value1 = READ_WORD(&SCRATCH_RAM[TOBYTE(0x754c0)]);

		/* Suspend cpu if it's waiting for an interrupt */
		if (cpu_get_pc() == 0xff833480 && !value1)
		{
			DO_SPEEDUP_LOOP_1(0x1008040, 0xd0, 0xb0, INT16, INT16);
		}
		return value1;
	}
}

static void remove_access_errors(void)
{
	/* get rid of unmapped access errors during tests */
	install_mem_write_handler(0, TOBYTE(0x00200000), TOBYTE(0x0020003f), MWA_NOP);
	install_mem_write_handler(0, TOBYTE(0x01100000), TOBYTE(0x0110003f), MWA_NOP);
	install_mem_read_handler(0, TOBYTE(0x00200000), TOBYTE(0x0020003f), MRA_NOP);
	install_mem_read_handler(0, TOBYTE(0x01100000), TOBYTE(0x0110003f), MRA_NOP);
}


static void load_gfx_roms_4bit(void)
{
	int i;
	UINT8 d1,d2,d3,d4;
	UINT8 *mem_rom;
	memset(GFX_ROM,0,wms_gfx_rom_size);
	mem_rom = memory_region(REGION_GFX1);
	/* load the graphics ROMs -- quadruples 2 bits each */
	for (i=0;i<wms_gfx_rom_size;i+=2)
	{
		d1 = ((mem_rom[					  (i  )/4])>>(2*((i  )%4)))&0x03;
		d2 = ((mem_rom[wms_gfx_rom_size/4+(i  )/4])>>(2*((i  )%4)))&0x03;
		d3 = ((mem_rom[					  (i+1)/4])>>(2*((i+1)%4)))&0x03;
		d4 = ((mem_rom[wms_gfx_rom_size/4+(i+1)/4])>>(2*((i+1)%4)))&0x03;
		WRITE_WORD(&GFX_ROM[i],d1|(d2<<2)|(d1<<4)|(d2<<6)|(d3<<8)|(d4<<10)|(d3<<12)|(d4<<14));
	}
	free_memory_region(REGION_GFX1);
}
static void load_gfx_roms_6bit(void)
{
	int i;
	UINT8 d1,d2,d3,d4,d5,d6;
	UINT8 *mem_rom;
	memset(GFX_ROM,0,wms_gfx_rom_size);
	mem_rom = memory_region(REGION_GFX1);
	/* load the graphics ROMs -- quadruples 2 bits each */
	for (i=0;i<wms_gfx_rom_size;i+=2)
	{
		d1 = ((mem_rom[						(i  )/4])>>(2*((i  )%4)))&0x03;
		d2 = ((mem_rom[  wms_gfx_rom_size/4+(i  )/4])>>(2*((i  )%4)))&0x03;
		d3 = ((mem_rom[2*wms_gfx_rom_size/4+(i  )/4])>>(2*((i  )%4)))&0x03;
		d4 = ((mem_rom[						(i+1)/4])>>(2*((i+1)%4)))&0x03;
		d5 = ((mem_rom[  wms_gfx_rom_size/4+(i+1)/4])>>(2*((i+1)%4)))&0x03;
		d6 = ((mem_rom[2*wms_gfx_rom_size/4+(i+1)/4])>>(2*((i+1)%4)))&0x03;
		WRITE_WORD(&GFX_ROM[i],d1|(d2<<2)|(d3<<4)|(d4<<8)|(d5<<10)|(d6<<12));
	}
	free_memory_region(REGION_GFX1);
}
static void load_gfx_roms_8bit(void)
{
	int i;
	UINT8 d1,d2;
	UINT8 *mem_rom;
	memset(GFX_ROM,0,wms_gfx_rom_size);
	mem_rom = memory_region(REGION_GFX1);
	/* load the graphics ROMs -- quadruples */
	for (i=0;i<wms_gfx_rom_size;i+=4)
	{
		d1 = mem_rom[					  i/4];
		d2 = mem_rom[  wms_gfx_rom_size/4+i/4];
		WRITE_WORD(&GFX_ROM[i  ],(UINT32)((UINT32)(d1) | ((UINT32)(d2)<<8)));
		d1 = mem_rom[2*wms_gfx_rom_size/4+i/4];
		d2 = mem_rom[3*wms_gfx_rom_size/4+i/4];
		WRITE_WORD(&GFX_ROM[i+2],(UINT32)((UINT32)(d1) | ((UINT32)(d2)<<8)));
	}
	free_memory_region(REGION_GFX1);
}

static void load_adpcm_roms_512k(void)
{
	UINT8 *base = memory_region(REGION_SOUND1);

	memcpy(base + 0xa0000, base + 0x20000, 0x20000);
	memcpy(base + 0x80000, base + 0x60000, 0x20000);
	memcpy(base + 0x60000, base + 0x20000, 0x20000);
}

static void wms_modify_pen(int i, int rgb)
{
	extern UINT16 *shrinked_pens;

#define rgbpenindex(r,g,b) ((Machine->scrbitmap->depth==16) ? ((((r)>>3)<<10)+(((g)>>3)<<5)+((b)>>3)) : ((((r)>>5)<<5)+(((g)>>5)<<2)+((b)>>6)))

	int r,g,b;

	r = (rgb >> 10) & 0x1f;
	g = (rgb >>  5) & 0x1f;
	b = (rgb >>  0) & 0x1f;

	r = (r << 3) | (r >> 2);
	g = (g << 3) | (g >> 2);
	b = (b << 3) | (b >> 2);

	Machine->pens[i] = shrinked_pens[rgbpenindex(r,g,b)];
}

static void wms_8bit_t_paletteram_xRRRRRGGGGGBBBBB_word_w(int offset,int data)
{
	int i;
	int oldword = READ_WORD(&paletteram[offset]);
	int newword = COMBINE_WORD(oldword,data);

	int base = offset / 2;

	WRITE_WORD(&paletteram[offset],newword);

	for (i = 0; i < 1; i++)
	{
		wms_modify_pen(base | (i << 16), newword);
	}
}

static void wms_8bit_paletteram_xRRRRRGGGGGBBBBB_word_w(int offset,int data)
{
	int i;
	int oldword = READ_WORD(&paletteram[offset]);
	int newword = COMBINE_WORD(oldword,data);

	int base = offset / 2;

	WRITE_WORD(&paletteram[offset],newword);

	for (i = 0; i < 8; i++)
	{
		wms_modify_pen(base | (i << 13), newword);
	}
}

static void wms_6bit_paletteram_xRRRRRGGGGGBBBBB_word_w(int offset,int data)
{
	/*
	 * the palette entry to find is mapped like this:
	 * Bit 15 - Not Used
	 * Bit 14 - Not Used
	 * Bit 13 - Not Used
	 * Bit 12 - Not Used
	 * Bit 11 - PAL Bit 03
	 * Bit 10 - PAL Bit 02
	 * Bit 09 - PAL Bit 01
	 * Bit 08 - PAL Bit 00
	 * Bit 07 - PAL Bit 07
	 * Bit 06 - PAL Bit 06
	 * Bit 05 - DATA Bit 05
	 * Bit 04 - DATA Bit 04
	 * Bit 03 - DATA Bit 03
	 * Bit 02 - DATA Bit 02
	 * Bit 01 - DATA Bit 01
	 * Bit 00 - DATA Bit 00
	 */

	int i;
	int oldword = READ_WORD(&paletteram[offset]);
	int newword = COMBINE_WORD(oldword,data);

	int base = offset / 2;
	base = (base & 0xf3f) | ((base & 0xc0) << 8);

	WRITE_WORD(&paletteram[offset],newword);

	for (i = 0; i < 16; i++)
	{
		wms_modify_pen(base | ((i & 3) << 6) | ((i & 0x0c) << 10), newword);
	}
}

static void wms_4bit_paletteram_xRRRRRGGGGGBBBBB_word_w(int offset,int data)
{
	/*
	 * the palette entry to find is mapped like this:
	 * Bit 07 - PAL Bit 07
	 * Bit 06 - PAL Bit 06
	 * Bit 05 - PAL Bit 05
	 * Bit 04 - PAL Bit 04
	 * Bit 03 - DATA Bit 03
	 * Bit 02 - DATA Bit 02
	 * Bit 01 - DATA Bit 01
	 * Bit 00 - DATA Bit 00
	 */

	int i;
	int oldword = READ_WORD(&paletteram[offset]);
	int newword = COMBINE_WORD(oldword,data);

	int base = offset / 2;
	base = (base & 0x0f) | ((base & 0xf0) << 8);

	WRITE_WORD(&paletteram[offset],newword);

	for (i = 0; i < 256; i++)
	{
		wms_modify_pen(base | (i << 4), newword);
	}
}

static void init_8bit_t(void)
{
	install_mem_write_handler(0, TOBYTE(0x01800000), TOBYTE(0x018fffff), wms_8bit_t_paletteram_xRRRRRGGGGGBBBBB_word_w);
	install_mem_read_handler (0, TOBYTE(0x01800000), TOBYTE(0x018fffff), paletteram_word_r);
}
static void init_8bit(void)
{
	install_mem_write_handler(0, TOBYTE(0x01800000), TOBYTE(0x0181ffff), wms_8bit_paletteram_xRRRRRGGGGGBBBBB_word_w);
	install_mem_read_handler (0, TOBYTE(0x01800000), TOBYTE(0x0181ffff), paletteram_word_r);
}
static void init_6bit(void)
{
	install_mem_write_handler(0, TOBYTE(0x01810000), TOBYTE(0x0181ffff), wms_6bit_paletteram_xRRRRRGGGGGBBBBB_word_w);
	install_mem_read_handler (0, TOBYTE(0x01810000), TOBYTE(0x0181ffff), paletteram_word_r);
}
static void init_4bit(void)
{
	install_mem_write_handler(0, TOBYTE(0x0181f000), TOBYTE(0x0181ffff), wms_4bit_paletteram_xRRRRRGGGGGBBBBB_word_w);
	install_mem_read_handler (0, TOBYTE(0x0181f000), TOBYTE(0x0181ffff), paletteram_word_r);
}

/* driver_init functions */

void init_narc(void)
{
	/* set up speedup loops */
	install_mem_read_handler(0, TOBYTE(0x0101b300), TOBYTE(0x0101b31f), narc_speedup_r);

	TMS34010_set_stack_base(0, SCRATCH_RAM, TOBYTE(0x1000000));
	wms_autoerase_reset = 1;

	/* This just causes the init_machine to copy the images again */
	wms_rom_loaded = 0;
}
void init_trog(void)
{
	/* set up speedup loops */
	install_mem_read_handler(0, TOBYTE(0x010a20a0), TOBYTE(0x010a20bf), trog_speedup_r);
	wms_protect_s = 0xffe47c40;
	wms_protect_d = 0xffe47af0;

	TMS34010_set_stack_base(0, SCRATCH_RAM, TOBYTE(0x1000000));
	wms_autoerase_reset = 0;

	/* expand the sound ROMs */
	memcpy(&memory_region(REGION_CPU2)[0x20000], &memory_region(REGION_CPU2)[0x10000], 0x10000);
	memcpy(&memory_region(REGION_CPU2)[0x40000], &memory_region(REGION_CPU2)[0x30000], 0x10000);
	memcpy(&memory_region(REGION_CPU2)[0x60000], &memory_region(REGION_CPU2)[0x50000], 0x10000);

	/* This just causes the init_machine to copy the images again */
	wms_rom_loaded = 0;
}
void init_trog3(void)
{
	/* set up speedup loops */
	install_mem_read_handler(0, TOBYTE(0x010a2080), TOBYTE(0x010a209f), trog3_speedup_r);
	wms_protect_s = 0xffe47c70;
	wms_protect_d = 0xffe47b20;

	TMS34010_set_stack_base(0, SCRATCH_RAM, TOBYTE(0x1000000));
	wms_autoerase_reset = 0;

	/* expand the sound ROMs */
	memcpy(&memory_region(REGION_CPU2)[0x20000], &memory_region(REGION_CPU2)[0x10000], 0x10000);
	memcpy(&memory_region(REGION_CPU2)[0x40000], &memory_region(REGION_CPU2)[0x30000], 0x10000);
	memcpy(&memory_region(REGION_CPU2)[0x60000], &memory_region(REGION_CPU2)[0x50000], 0x10000);

	/* This just causes the init_machine to copy the images again */
	wms_rom_loaded = 0;
}
void init_trogp(void)
{
	/* set up speedup loops */
	install_mem_read_handler(0, TOBYTE(0x010a1ee0), TOBYTE(0x010a1eff), trogp_speedup_r);

	TMS34010_set_stack_base(0, SCRATCH_RAM, TOBYTE(0x1000000));
	wms_autoerase_reset = 0;

	/* expand the sound ROMs */
	memcpy(&memory_region(REGION_CPU2)[0x20000], &memory_region(REGION_CPU2)[0x10000], 0x10000);
	memcpy(&memory_region(REGION_CPU2)[0x40000], &memory_region(REGION_CPU2)[0x30000], 0x10000);
	memcpy(&memory_region(REGION_CPU2)[0x60000], &memory_region(REGION_CPU2)[0x50000], 0x10000);

	/* This just causes the init_machine to copy the images again */
	wms_rom_loaded = 0;
}
void init_smashtv(void)
{
	/* set up speedup loops */
	install_mem_read_handler(0, TOBYTE(0x01086760), TOBYTE(0x0108677f), smashtv_speedup_r);

	TMS34010_set_stack_base(0, SCRATCH_RAM, TOBYTE(0x01000000));
	wms_autoerase_reset = 0;

	/* expand the sound ROMs */
	memcpy(&memory_region(REGION_CPU2)[0x20000], &memory_region(REGION_CPU2)[0x10000], 0x10000);
	memcpy(&memory_region(REGION_CPU2)[0x40000], &memory_region(REGION_CPU2)[0x30000], 0x10000);
	memcpy(&memory_region(REGION_CPU2)[0x60000], &memory_region(REGION_CPU2)[0x50000], 0x10000);

	/* This just causes the init_machine to copy the images again */
	wms_rom_loaded = 0;
}
void init_smashtv4(void)
{
	/* set up speedup loops */
	install_mem_read_handler(0, TOBYTE(0x01086780), TOBYTE(0x0108679f), smashtv4_speedup_r);

	TMS34010_set_stack_base(0, SCRATCH_RAM, TOBYTE(0x01000000));
	wms_autoerase_reset = 0;

	/* expand the sound ROMs */
	memcpy(&memory_region(REGION_CPU2)[0x20000], &memory_region(REGION_CPU2)[0x10000], 0x10000);
	memcpy(&memory_region(REGION_CPU2)[0x40000], &memory_region(REGION_CPU2)[0x30000], 0x10000);
	memcpy(&memory_region(REGION_CPU2)[0x60000], &memory_region(REGION_CPU2)[0x50000], 0x10000);

	/* This just causes the init_machine to copy the images again */
	wms_rom_loaded = 0;
}
void init_hiimpact(void)
{
	/* set up speedup loops */
	install_mem_read_handler(0, TOBYTE(0x01053140), TOBYTE(0x0105315f), hiimpact_speedup_r);
	wms_protect_s = 0xffe77c20;
	wms_protect_d = 0xffe77ad0;

	TMS34010_set_stack_base(0, SCRATCH_RAM, TOBYTE(0x1000000));
	wms_autoerase_reset = 0;

	/* This just causes the init_machine to copy the images again */
	wms_rom_loaded = 0;
}
void init_shimpact(void)
{
	/* set up speedup loops */
	install_mem_read_handler(0, TOBYTE(0x01052060), TOBYTE(0x0105207f), shimpact_speedup_r);
	wms_protect_s = 0xffe07a40;
	wms_protect_d = 0xffe078f0;

	TMS34010_set_stack_base(0, SCRATCH_RAM, TOBYTE(0x1000000));
	wms_autoerase_reset = 0;

	/* This just causes the init_machine to copy the images again */
	wms_rom_loaded = 0;
}
void init_strkforc(void)
{
	/* set up speedup loops */
	install_mem_read_handler(0, TOBYTE(0x01071dc0), TOBYTE(0x01071ddf), strkforc_speedup_r);
	wms_protect_s = 0xffe4c100;
	wms_protect_d = 0xffe4c1d0;

	TMS34010_set_stack_base(0, SCRATCH_RAM, TOBYTE(0x1000000));
	wms_autoerase_reset = 0;

	/* expand the sound ROMs */
	memcpy(&memory_region(REGION_CPU2)[0x20000], &memory_region(REGION_CPU2)[0x10000], 0x10000);
	memcpy(&memory_region(REGION_CPU2)[0x40000], &memory_region(REGION_CPU2)[0x30000], 0x10000);
	memcpy(&memory_region(REGION_CPU2)[0x60000], &memory_region(REGION_CPU2)[0x50000], 0x10000);

	/* This just causes the init_machine to copy the images again */
	wms_rom_loaded = 0;
}
void init_mk(void)
{
	/* set up speedup loops */
	install_mem_read_handler(0, TOBYTE(0x01053360), TOBYTE(0x0105337f), mk_speedup_r);

	TMS34010_set_stack_base(0, SCRATCH_RAM, TOBYTE(0x1000000));
	wms_autoerase_reset = 0;

	/* This just causes the init_machine to copy the images again */
	wms_rom_loaded = 0;
}
void init_mkla1(void)
{
	/* set up speedup loops */
	install_mem_read_handler(0, TOBYTE(0x0104f000), TOBYTE(0x0104f01f), mkla1_speedup_r);
	wms_protect_s = 0xffc96e20;
	wms_protect_d = 0xffc96ce0;

	TMS34010_set_stack_base(0, SCRATCH_RAM, TOBYTE(0x1000000));
	wms_autoerase_reset = 0;

	/* This just causes the init_machine to copy the images again */
	wms_rom_loaded = 0;
}
void init_mkla2(void)
{
	/* set up speedup loops */
	install_mem_read_handler(0, TOBYTE(0x0104f020), TOBYTE(0x0104f03f), mkla2_speedup_r);
	wms_protect_s = 0xffc96d00;
	wms_protect_d = 0xffc96bc0;

	TMS34010_set_stack_base(0, SCRATCH_RAM, TOBYTE(0x1000000));
	wms_autoerase_reset = 0;

	/* This just causes the init_machine to copy the images again */
	wms_rom_loaded = 0;
}
void init_mkla3(void)
{
	/* set up speedup loops */
	install_mem_read_handler(0, TOBYTE(0x0104f040), TOBYTE(0x0104f05f), mkla3_speedup_r);
	wms_protect_s = 0xffc98930;
	wms_protect_d = 0xffc987f0;

	TMS34010_set_stack_base(0, SCRATCH_RAM, TOBYTE(0x1000000));
	wms_autoerase_reset = 0;

	/* This just causes the init_machine to copy the images again */
	wms_rom_loaded = 0;
}
void init_mkla4(void)
{
	/* set up speedup loops */
	install_mem_read_handler(0, TOBYTE(0x0104f040), TOBYTE(0x0104f05f), mkla4_speedup_r);
	wms_protect_s = 0xffc989d0;
	wms_protect_d = 0xffc98890;

	TMS34010_set_stack_base(0, SCRATCH_RAM, TOBYTE(0x1000000));
	wms_autoerase_reset = 0;

	/* This just causes the init_machine to copy the images again */
	wms_rom_loaded = 0;
}
void init_term2(void)
{
	/* set up speedup loops */
	install_mem_read_handler(0, TOBYTE(0x010aa040), TOBYTE(0x010aa05f), term2_speedup_r);
	wms_protect_s = 0xffd64f30;
	wms_protect_d = 0xffd64de0;

	TMS34010_set_stack_base(0, SCRATCH_RAM, TOBYTE(0x1000000));
	wms_autoerase_reset = 0;

	/* This just causes the init_machine to copy the images again */
	wms_rom_loaded = 0;
}
void init_totcarn(void)
{
	/* set up speedup loops */
	install_mem_read_handler(0, TOBYTE(0x0107dde0), TOBYTE(0x0107ddff), totcarn_speedup_r);
	wms_protect_s = 0xffd1fd30;
	wms_protect_d = 0xffd1fbf0;

	TMS34010_set_stack_base(0, SCRATCH_RAM, TOBYTE(0x1000000));
	wms_autoerase_reset = 0;

	/* This just causes the init_machine to copy the images again */
	wms_rom_loaded = 0;
}
void init_totcarnp(void)
{
	/* set up speedup loops */
	install_mem_read_handler(0, TOBYTE(0x0107dde0), TOBYTE(0x0107ddff), totcarn_speedup_r);
	wms_protect_s = 0xffd1edd0;
	wms_protect_d = 0xffd1ec90;

	TMS34010_set_stack_base(0, SCRATCH_RAM, TOBYTE(0x1000000));
	wms_autoerase_reset = 0;

	/* This just causes the init_machine to copy the images again */
	wms_rom_loaded = 0;
}
void init_mk2(void)
{
	/* set up speedup loops */
	install_mem_read_handler(0, TOBYTE(0x01068e60), TOBYTE(0x01068e7f), mk2_speedup_r);

	TMS34010_set_stack_base(0, SCRATCH_RAM, TOBYTE(0x1000000));
	wms_autoerase_reset = 0;

	cpu_bankbase[7] = &(GFX_ROM[0x800000]);
	install_mem_read_handler(0, TOBYTE(0x04000000), TOBYTE(0x05ffffff), MRA_BANK7);

	/* This just causes the init_machine to copy the images again */
	wms_rom_loaded = 0;
}
void init_mk2r14(void)
{
	/* set up speedup loops */
	install_mem_read_handler(0, TOBYTE(0x01068de0), TOBYTE(0x01068dff), mk2r14_speedup_r);

	TMS34010_set_stack_base(0, SCRATCH_RAM, TOBYTE(0x1000000));
	wms_autoerase_reset = 0;

	cpu_bankbase[7] = &(GFX_ROM[0x800000]);
	install_mem_read_handler(0, TOBYTE(0x04000000), TOBYTE(0x05ffffff), MRA_BANK7);

	/* This just causes the init_machine to copy the images again */
	wms_rom_loaded = 0;
}
void init_nbajam(void)
{
	/* set up speedup loops */
	install_mem_read_handler(0, TOBYTE(0x010754c0), TOBYTE(0x010754df), nbajam_speedup_r);

	TMS34010_set_stack_base(0, SCRATCH_RAM, TOBYTE(0x1000000));
	wms_autoerase_reset = 0;

	install_mem_read_handler(0, TOBYTE(0x04000000), TOBYTE(0x05ffffff), MRA_BANK8);

	/* This just causes the init_machine to copy the images again */
	wms_rom_loaded = 0;
}

/* init_machine functions */

void narc_init_machine(void)
{
	/*
	 * Z-Unit
	 *
	 * music board is 6809 driving YM2151, DAC
	 * effect board is 6809 driving CVSD, DAC
	 *
	 */

	/* set up ROMs */
	if (!wms_rom_loaded)
	{
		wms_load_code_roms();
		load_gfx_roms_8bit();
		wms_rom_loaded = 1;
	}

	/* set up palette */
	init_8bit();

	/* get rid of unmapped access errors during tests */
	remove_access_errors();

	/* set up sound board */
	williams_narc_init(1);
	install_mem_write_handler(0, TOBYTE(0x01e00000), TOBYTE(0x01e0001f), narc_sound_w);

	/* special input handler */
	install_mem_read_handler(0, TOBYTE(0x01c00000), TOBYTE(0x01c0005f), narc_input_r );

	install_mem_read_handler(0, TOBYTE(0x09afffc0), TOBYTE(0x09afffff), narc_unknown_r); /* bug? */
	install_mem_read_handler(0, TOBYTE(0x38383900), TOBYTE(0x383839ff), narc_unknown_r); /* bug? */
}
void smashtv_init_machine(void)
{
	/*
	 * Y-Unit
	 *
	 * sound board is 6809 driving YM2151, DAC, and CVSD
	 *
	 */

	/* set up ROMs */
	if (!wms_rom_loaded)
	{
		wms_load_code_roms();
		load_gfx_roms_6bit();
		wms_rom_loaded = 1;
	}

	/* set up palette */
	init_6bit();

	/* get rid of unmapped access errors during tests */
	remove_access_errors();

	/* set up sound board */
	pia_unconfig();
	williams_cvsd_init(1, 0);
	pia_reset();
	install_mem_write_handler(0, TOBYTE(0x01e00000), TOBYTE(0x01e0001f), cvsd_sound_w);
}
void mk_init_machine(void)
{
	/*
	 * Y-Unit
	 *
	 * sound board is 6809 driving YM2151, DAC, and OKIM6295
	 *
	 */

	/* set up ROMs */
	if (!wms_rom_loaded)
	{
		wms_load_code_roms();
		load_gfx_roms_6bit();
		load_adpcm_roms_512k();
		wms_rom_loaded = 1;
	}

	/* set up palette */
	init_6bit();

	/* get rid of unmapped access errors during tests */
	remove_access_errors();

	/* set up sound board */
	williams_adpcm_init(1);
	install_mem_write_handler(0, TOBYTE(0x01e00000), TOBYTE(0x01e0001f), adpcm_sound_w);
}
void term2_init_machine(void)
{
	/*
	 * Y-Unit
	 *
	 * same as Mortal Kombat, but with the gun controllers
	 *
	 */

	mk_init_machine();

	/* special input handler */
	install_mem_read_handler(0, TOBYTE(0x01c00000), TOBYTE(0x01c0005f), term2_input_r );
	install_mem_read_handler(0, TOBYTE(0x01600020), TOBYTE(0x0160005f), term2_input_lo_r ); /* ??? */

	install_mem_write_handler(0, TOBYTE(0x01e00000), TOBYTE(0x01e0001f), term2_sound_w);
}
void trog_init_machine(void)
{
	/*
	 * Y-Unit
	 *
	 * sound board is 6809 driving YM2151, DAC, and OKIM6295
	 *
	 */

	/* set up ROMs */
	if (!wms_rom_loaded)
	{
		wms_load_code_roms();
		load_gfx_roms_4bit();
		wms_rom_loaded = 1;
	}

	/* set up palette */
	init_4bit();

	/* get rid of unmapped access errors during tests */
	remove_access_errors();

	/* set up sound board */
	pia_unconfig();
	williams_cvsd_init(1, 0);
	pia_reset();
	/* fix sound (hack) */
	install_mem_write_handler(0, TOBYTE(0x01e00000), TOBYTE(0x01e0001f), cvsd_sound_w);
}
void mk2_init_machine(void)
{
	if (!wms_rom_loaded)
	{
		wms_load_code_roms();
		load_gfx_roms_8bit();
		wms_rom_loaded = 1;
		gfxrombackup = GFX_ROM;
	}

	/* set up palette */
	init_8bit_t();

	/*for (i=0;i<256;i++)
	{
		for (j=0;j<256;j++)
		{
			wms_conv_table[(i<<8)+j] = ((i&0x1f)<<8) | j;
		}
	}*/
	wms_videoram_size = 0x80000*2;
}
void nbajam_init_machine(void)
{
	if (!wms_rom_loaded)
	{
		wms_load_code_roms();
		load_gfx_roms_8bit();
		wms_rom_loaded = 1;
		gfxrombackup = GFX_ROM;
	}

	/* set up palette */
	init_8bit_t();

	/*for (i=0;i<256;i++)
	{
		for (j=0;j<256;j++)
		{
			wms_conv_table[(i<<8)+j] = ((i&0x1f)<<8) | j;
		}
	}*/
	wms_videoram_size = 0x80000*2;

	/* set up sound board */
	williams_adpcm_init(1);
	install_mem_write_handler(0, TOBYTE(0x01d01020), TOBYTE(0x01d0103f), adpcm_sound_w);
}

static void cvsd_sound_w(int offset, int data)
{
	if (errorlog) fprintf(errorlog, "CPU #0 PC %08x: ", cpu_get_pc());
	if (errorlog) fprintf(errorlog, "sound write %x\n", data);
	williams_cvsd_data_w(0, (data & 0xff) | ((data & 0x200) >> 1));
}

static void adpcm_sound_w(int offset, int data)
{
//	if (errorlog) fprintf(errorlog, "CPU #0 PC %08x: ", cpu_get_pc());
//	if (errorlog) fprintf(errorlog, "sound write %x\n", data);
	williams_adpcm_data_w(0, data);
}

static void narc_sound_w(int offset, int data)
{
//	if (errorlog) fprintf(errorlog, "CPU #0 PC %08x: ", cpu_get_pc());
//	if (errorlog) fprintf(errorlog, "sound write %x\n", data);
	williams_narc_data_w(0, data);
}

