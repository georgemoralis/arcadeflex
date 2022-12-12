/***************************************************************************

	mcr.c

	Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
	I/O ports)

	Tapper machine started by Chris Kirmse

***************************************************************************/

#include "machine/6821pia.h"


extern INT16 spyhunt_scrollx, spyhunt_scrolly;
extern double mcr68_timing_factor;



/************ Generic MCR routines ***************/

extern Z80_DaisyChain mcr_daisy_chain[];

void mcr_init_machine(void);
void mcr68_init_machine(void);
void zwackery_init_machine(void);

int mcr_interrupt(void);
int mcr68_interrupt(void);

void mcr_port_01_w(int offset, int data);
void mcr_port_47_dispatch_w(int offset, int value);
void mcr_scroll_value_w(int offset, int value);

int mcr_port_04_dispatch_r(int offset);

void mcr68_6840_upper_w(int offset, int data);
void mcr68_6840_lower_w(int offset, int data);
int mcr68_6840_upper_r(int offset);
int mcr68_6840_lower_r(int offset);



/************ I/O Port Configuration ***************/

extern int (*mcr_port04_r[5])(int offset);
extern void (*mcr_port47_w[4])(int offset, int data);
extern UINT8 mcr_cocktail_flip;

extern void mcr_dummy_w(int offset, int data);

#define MCR_CONFIGURE_PORT_04_READS(p0,p1,p2,p3,p4) \
	mcr_port04_r[0] = p0 ? p0 : input_port_0_r;\
	mcr_port04_r[1] = p1 ? p1 : input_port_1_r;\
	mcr_port04_r[2] = p2 ? p2 : input_port_2_r;\
	mcr_port04_r[3] = p3 ? p3 : input_port_3_r;\
	mcr_port04_r[4] = p4 ? p4 : input_port_4_r;

#define MCR_CONFIGURE_PORT_47_WRITES(p4,p5,p6,p7) \
	mcr_port47_w[0] = p4 ? p4 : mcr_dummy_w;\
	mcr_port47_w[1] = p5 ? p5 : mcr_dummy_w;\
	mcr_port47_w[2] = p6 ? p6 : mcr_dummy_w;\
	mcr_port47_w[3] = p7 ? p7 : mcr_dummy_w;

#define MCR_CONFIGURE_DEFAULT_PORTS \
	MCR_CONFIGURE_PORT_04_READS(NULL, NULL, NULL, NULL, NULL);\
	MCR_CONFIGURE_PORT_47_WRITES(NULL, NULL, NULL, NULL)



/************ High Score Configuration ***************/

extern UINT16 mcr_hiscore_start;
extern UINT16 mcr_hiscore_length;
extern const UINT8 *mcr_hiscore_init;
extern UINT16 mcr_hiscore_init_length;

#define MCR_CONFIGURE_HISCORE(st,len,init) \
	mcr_hiscore_start = st;\
	mcr_hiscore_length = len;\
	mcr_hiscore_init = init;\
	if (init) mcr_hiscore_init_length = sizeof(init)
#define MCR_CONFIGURE_NO_HISCORE \
	MCR_CONFIGURE_HISCORE(0,0,NULL)

void mcr_nvram_handler(void *file,int read_or_write);



/************ Generic character and sprite definition ***************/

/*
 * 	Note that characters are half the
 *	resolution of sprites in each
 *	direction, so we generate them at
 *	double size
 */

#define MCR_CHAR_LAYOUT(name, count) 				\
	static struct GfxLayout name =					\
	{												\
		16,16,										\
		count,										\
		4,											\
		{ count*128, count*128+1, 0, 1 },			\
		{  0,  0,  2,  2,  4,  4,  6,  6, 			\
		   8,  8, 10, 10, 12, 12, 14, 14 },			\
		{ 0*8,  0*8,  2*8,  2*8,					\
		  4*8,  4*8,  6*8,  6*8, 					\
		  8*8,  8*8, 10*8, 10*8,					\
		 12*8, 12*8, 14*8, 14*8 },					\
		16*8										\
	}

#define MCR_SPRITE_LAYOUT(name, count) 				\
	static struct GfxLayout name = 					\
	{												\
		32,32,										\
		count,										\
		4,											\
		{ 0, 1, 2, 3 },								\
		{ count*0*1024+0, count*0*1024+4,			\
		  count*1*1024+0, count*1*1024+4, 			\
		  count*2*1024+0, count*2*1024+4, 			\
		  count*3*1024+0, count*3*1024+4, 			\
		  count*0*1024+8, count*0*1024+12,			\
		  count*1*1024+8, count*1*1024+12, 			\
		  count*2*1024+8, count*2*1024+12, 			\
		  count*3*1024+8, count*3*1024+12, 			\
		  count*0*1024+16, count*0*1024+20,			\
		  count*1*1024+16, count*1*1024+20, 		\
		  count*2*1024+16, count*2*1024+20, 		\
		  count*3*1024+16, count*3*1024+20, 		\
		  count*0*1024+24, count*0*1024+28,			\
		  count*1*1024+24, count*1*1024+28, 		\
		  count*2*1024+24, count*2*1024+28, 		\
		  count*3*1024+24, count*3*1024+28 },		\
		{ 32*0,  32*1,  32*2,  32*3,				\
		  32*4,  32*5,  32*6,  32*7,				\
		  32*8,  32*9,  32*10, 32*11,				\
		  32*12, 32*13, 32*14, 32*15,				\
		  32*16, 32*17, 32*18, 32*19,				\
		  32*20, 32*21, 32*22, 32*23,				\
		  32*24, 32*25, 32*26, 32*27,				\
		  32*28, 32*29, 32*30, 32*31 },				\
		32*32										\
	}
