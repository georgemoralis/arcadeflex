/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

#include "driver.h"

/* These are defined in vidhrdw/exidy.c */
extern unsigned char *exidy_sprite_enable;
extern int exidy_collision;
extern int exidy_collision_counter;

/* These are defined in sndhrdw/targ.c */
extern unsigned char targ_spec_flag;


/* also in drivers/exidy.c */
#define PALETTE_LEN 8
#define COLORTABLE_LEN 20

static unsigned char *palette;
static unsigned short *colortable;

/* Arbitrary starting colors, modified by the game */

static unsigned char sidetrac_palette[3*PALETTE_LEN] =
{
	0x00,0x00,0x00,   /* BACKGND */
	0x00,0x00,0x00,   /* CSPACE0 */
	0x00,0xff,0x00,   /* CSPACE1 */
	0xff,0xff,0xff,   /* CSPACE2 */
	0xff,0xff,0xff,   /* CSPACE3 */
	0xff,0x00,0xff,   /* 5LINES (unused?) */
	0xff,0xff,0x00,   /* 5MO2VID  */
	0xff,0xff,0xff    /* 5MO1VID  */
};

static unsigned short sidetrac_colortable[COLORTABLE_LEN] =
{
	/* one-bit characters */
	0, 4,  /* chars 0x00-0x3F */
	0, 3,  /* chars 0x40-0x7F */
	0, 2,  /* chars 0x80-0xBF */
	0, 1,  /* chars 0xC0-0xFF */

	/* Motion Object 1 */
	0, 7,

	/* Motion Object 2 */
	0, 6,

};

static unsigned char venture_palette[3*PALETTE_LEN] =
{
	0x00,0x00,0x00,   /* BACKGND */
	0x00,0x00,0xff,   /* CSPACE0 */
	0x00,0xff,0x00,   /* CSPACE1 */
	0x00,0xff,0xff,   /* CSPACE2 */
	0xff,0x00,0x00,   /* CSPACE3 */
	0xff,0x00,0xff,   /* 5LINES (unused?) */
	0xff,0xff,0x00,   /* 5MO2VID */
	0xff,0xff,0xff    /* 5MO1VID */
};

/* Targ doesn't have a color PROM; colors are changed by the means of 8x3 */
/* dip switches on the board. Here are the colors they map to. */
static unsigned char targ_palette[3*PALETTE_LEN] =
{
					/* color   use                            */
	0x00,0x00,0xFF, /* blue    background             */
	0x00,0xFF,0xFF, /* cyan    characters 192-255 */
	0xFF,0xFF,0x00, /* yellow  characters 128-191 */
	0xFF,0xFF,0xFF, /* white   characters  64-127 */
	0xFF,0x00,0x00, /* red     characters   0- 63 */
	0x00,0xFF,0xFF, /* cyan    not used               */
	0xFF,0xFF,0xFF, /* white   bullet sprite          */
	0x00,0xFF,0x00, /* green   wummel sprite          */
};

/* Spectar has different colors */
static unsigned char spectar_palette[3*PALETTE_LEN] =
{
					/* color   use                            */
	0x00,0x00,0xFF, /* blue    background             */
	0x00,0xFF,0x00, /* green   characters 192-255 */
	0x00,0xFF,0x00, /* green   characters 128-191 */
	0xFF,0xFF,0xFF, /* white   characters  64-127 */
	0xFF,0x00,0x00, /* red     characters   0- 63 */
	0x00,0xFF,0x00, /* green   not used               */
	0xFF,0xFF,0x00, /* yellow  bullet sprite          */
	0x00,0xFF,0x00, /* green   wummel sprite          */
};


static unsigned short venture_colortable[COLORTABLE_LEN] =
{
	/* one-bit characters */
	0, 4,  /* chars 0x00-0x3F */
	0, 3,  /* chars 0x40-0x7F */
	0, 2,  /* chars 0x80-0xBF */
	0, 1,  /* chars 0xC0-0xFF */

	/* Motion Object 1 */
	0, 7,

	/* Motion Object 2 */
	0, 6,

};

static unsigned short pepper2_colortable[COLORTABLE_LEN] =
{
	/* two-bit characters */
	/* (Because this is 2-bit color, the colorspace is only divided
		in half instead of in quarters.  That's why 00-3F = 40-7F and
		80-BF = C0-FF) */
	0, 0, 4, 3,  /* chars 0x00-0x3F */
	0, 0, 4, 3,  /* chars 0x40-0x7F */
	0, 0, 2, 1,  /* chars 0x80-0xBF */
	0, 0, 2, 1,  /* chars 0xC0-0xFF */

	/* Motion Object 1 */
	0, 7,

	/* Motion Object 2 */
	0, 6,

};

void exidy_vh_init_palette(unsigned char *game_palette, unsigned short *game_colortable,const unsigned char *color_prom)
{
	memcpy(game_palette,palette,3*PALETTE_LEN);
	memcpy(game_colortable,colortable,COLORTABLE_LEN * sizeof(unsigned short));
}


unsigned char exidy_collision_mask = 0x00;

void fax_bank_select_w(int offset,int data)
{
	unsigned char *RAM = memory_region(REGION_CPU1);

	cpu_setbank (1, &RAM[0x10000 + (0x2000 * (data & 0x1F))]);
	cpu_setbankhandler_r (1, MRA_BANK1);
	cpu_setbankhandler_w (1, MWA_ROM);

	if ((data & 0x1F)>0x17)
	{
		if (errorlog)
			fprintf(errorlog,"Banking to unpopulated ROM bank %02X!\n",data & 0x1F);
	}
}

int exidy_input_port_2_r(int offset)
{
	int value;

	/* Get 2 coin inputs and VBLANK */
	value = readinputport(2);

	/* Combine with collision bits */
	value = value | ((exidy_collision) & (exidy_collision_mask));

	/* Reset collision bits */
	exidy_collision &= 0xEB;

	return value;
}

void init_sidetrac(void) {
	*exidy_sprite_enable = 0x10;
	exidy_collision_mask = 0x00;
	targ_spec_flag = 1;

	palette = sidetrac_palette;
	colortable = sidetrac_colortable;
}

void init_targ(void) {
	*exidy_sprite_enable = 0x10;
	exidy_collision_mask = 0x00;
	targ_spec_flag = 1;

	palette = targ_palette;
	colortable = venture_colortable;
}

void init_spectar(void) {
	/* Spectar does not have a sprite enable register so we have to fake it out */
	*exidy_sprite_enable = 0x10;
	exidy_collision_mask = 0x00;
	targ_spec_flag = 0;

	palette = spectar_palette;
	colortable = venture_colortable;
}

void init_mtrap(void) {
	/* Disable ROM Check for quicker startup */
	#if 0
	unsigned char *RAM = memory_region(REGION_CPU1);
	RAM[0xF439]=0xEA;
	RAM[0xF43A]=0xEA;
	RAM[0xF43B]=0xEA;
	#endif

	exidy_collision_mask = 0x14;

	palette = venture_palette;
	colortable = venture_colortable;
}

void init_pepper2(void) {
	/* Disable ROM Check for quicker startup */
	#if 0
	unsigned char *RAM = memory_region(REGION_CPU1);
	RAM[0xF52D]=0xEA;
	RAM[0xF52E]=0xEA;
	RAM[0xF52F]=0xEA;
	#endif

	exidy_collision_mask = 0x14;

	palette = venture_palette;
	colortable = pepper2_colortable;
}

void init_venture(void) {
	/* Disable ROM Check for quicker startup (Venture)*/
	#if 0
	unsigned char *RAM = memory_region(REGION_CPU1);
	RAM[0x8AF4]=0xEA;
	RAM[0x8AF5]=0xEA;
	RAM[0x8AF6]=0xEA;
	#endif
	/* Disable ROM Check for quicker startup (Venture2)*/
	#if 0
	unsigned char *RAM = memory_region(REGION_CPU1);
	RAM[0x8B04]=0xEA;
	RAM[0x8B05]=0xEA;
	RAM[0x8B06]=0xEA;
	#endif

	exidy_collision_mask = 0x80;

	palette = venture_palette;
	colortable = venture_colortable;
}

void init_fax(void) {
	/* Disable ROM Check for quicker startup */
	#if 0
	unsigned char *RAM = memory_region(REGION_CPU1);
	RAM[0xFBFC]=0xEA;
	RAM[0xFBFD]=0xEA;
	RAM[0xFBFE]=0xEA;
	/* Disable Question ROM Check for quicker startup */
	RAM[0xFC00]=0xEA;
	RAM[0xFC01]=0xEA;
	RAM[0xFC02]=0xEA;
	#endif

	exidy_collision_mask = 0x00;

	/* Initialize our ROM question bank */
	fax_bank_select_w(0,0);

	palette = venture_palette;
	colortable = pepper2_colortable;
}

void exidy_init_machine(void)
{
	/* Nothing to do here now */
}

int venture_interrupt(void)
{
	static int first_time = 1;
	static int interrupt_counter = 0;

	exidy_collision = (exidy_collision | 0x80) & exidy_collision_mask;

	if (first_time)
	{
		first_time=0;
		return nmi_interrupt();
	}

	interrupt_counter = (interrupt_counter + 1) % 32;

	if (interrupt_counter == 0)
	{
		exidy_collision &= 0x7F;
		return interrupt();
	}

	if (exidy_collision_counter>0)
	{
		exidy_collision_counter--;
		return interrupt();
	}

	return 0;
}

int exidy_interrupt(void)
{
	static int first_time = 1;

	exidy_collision = (exidy_collision | 0x80) & exidy_collision_mask;

	if (first_time)
	{
		first_time=0;
		return nmi_interrupt();
	}

	return interrupt();
}


