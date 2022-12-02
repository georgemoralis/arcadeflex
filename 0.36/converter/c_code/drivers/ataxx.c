/***************************************************************************

  Ataxx
  Indy Heat
  World Soccer Finals

  Driver provided by Paul Leaman (paul@vortexcomputing.demon.co.uk)

  The ATAXX series of hardware is significantly different from the other
  leland games.

***************************************************************************/

#include "driver.h"

#include "vidhrdw/generic.h"
#include "machine/8254pit.h"
#include "machine/eeprom.h"

/* Debugging */

#define NOISY_EEPROM 0

/* "core" routines in leland.c */
extern int cpu_get_halt_line(int num);
extern void cpu_set_reset_line(int num, int reset);
extern void cpu_set_test_line(int num, int test);


/* Helps document the input ports. */
#define IPT_SLAVEHALT IPT_UNKNOWN

#define USE_EEPROM 0

/* Video externals */
extern int  ataxx_vh_start(void);
extern void ataxx_vh_stop(void);
extern void ataxx_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh);
extern unsigned char *ataxx_bk_ram;
extern unsigned char *ataxx_tram;
extern int ataxx_tram_size;
extern unsigned char *ataxx_qram1;
extern unsigned char *ataxx_qram2;
extern int  ataxx_vram_port_r(int offset, int num);
extern void ataxx_vram_port_w(int offset, int data, int num);
extern void ataxx_mvram_port_w(int offset, int data);
extern void ataxx_svram_port_w(int offset, int data);
extern int  ataxx_mvram_port_r(int offset);
extern int  ataxx_svram_port_r(int offset);

/* These leland externs should all be reviewed */
extern void leland_graphics_ram_w(int offset, int data);
extern void leland_bk_xlow_w(int offset, int data);
extern void leland_bk_xhigh_w(int offset, int data);
extern void leland_bk_ylow_w(int offset, int data);
extern void leland_bk_yhigh_w(int offset, int data);
extern void leland_rearrange_bank_swap(int, int);
extern int  leland_sh_start(const struct MachineSound *msound);
extern void leland_sh_stop(void);
extern void leland_sh_update(void);
extern int  leland_master_interrupt(void);
extern void leland_master_video_addr_w(int offset, int data);
extern void leland_slave_video_addr_w(int offset, int data);

/* Globals  */
static int ataxx_palette_bank=0;    /* Palette / video RAM register bank */
static int ataxx_addrmask=0x0fffff; /* I86 Address mask (constant) */

/* Machine driver */
#define ATAXX_MACHINE_DRIVER(DRV, MRP, MWP, MR, MW, INITMAC, GFXD, VRF, SLR, SLW)\
static struct MachineDriver machine_driver_##DRV =                                 \
{                                                                 \
	{                                                             \
		{                                                         \
			CPU_Z80,        /* Master game processor */           \
			6000000,        /* 6.000 Mhz  */                      \
			MR,MW,                                                \
			MRP,MWP,                                              \
			ataxx_master_interrupt,2                              \
		},                                                        \
		{                                                         \
			CPU_Z80, /* Slave graphics processor*/                \
			6000000, /* 6.000 Mhz */                              \
			SLR,SLW,                                              \
			ataxx_slave_readport,ataxx_slave_writeport,           \
			ignore_interrupt,1                                    \
		},                                                        \
		{                                                         \
			CPU_I86|CPU_AUDIO_CPU, /* Sound processor */          \
			16000000,        /* 16 Mhz  */                        \
			ataxx_i86_readmem,ataxx_i86_writemem,                 \
			ataxx_i86_readport,ataxx_i86_writeport,               \
			ataxx_i86_interrupt,1,                                \
			0,0,&ataxx_addrmask                                   \
		},                                                        \
	},                                                            \
	60, 6000  /*DEFAULT_60HZ_VBLANK_DURATION */,1,                \
	INITMAC,                                                      \
	0x28*8, 0x20*8, { 0*8, 0x28*8-1, 0*8, 0x1e*8-1 },             \
	GFXD,                                                         \
	1024,1024,                                                    \
	0,                                                            \
	VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,                   \
	0,                                                            \
	ataxx_vh_start,ataxx_vh_stop,VRF,                             \
	0,0,0,0,                                                      \
	{                                                             \
		{SOUND_DAC,    &dac_interface },                          \
		{SOUND_CUSTOM, &custom_interface }                        \
	},                                                            \
	nvram_handler                                                 \
}

/***********************************************************************

 There is a bit in one of the input port registers that indicates
 the halt status of the slave Z80. This function reads the input port
 and sets the halt bit.

************************************************************************/

INLINE int ataxx_input_port_0_r(int offset)
{
    int halt=cpu_get_halt_line(1) ? 0x01 : 0x00;
    return (input_port_0_r(0)&0xfe)|halt;
}

/***********************************************************************

   EEPROM

   EEPROM accessed via serial protocol.

   TODO: Sit down and work this out properly !

    0x04 = Service mode switch on/off (WSF)

************************************************************************/

#if USE_EEPROM
/*
Commands:
0x0005 = Write
0x0006 = Read
0x0400
0x04c0
*/
static struct EEPROM_interface ataxx_eeprom_interface =
{
    7,                  /* EEPROM has 2^address_bits cells */
    16,                 /* every cell has this many bits (8 or 16) */
    "0000000000000110", /* read command string, e.g. "0110" */
    "0000000000000101", /* write command string, e.g. "0111" */
    "0000010000001100", /* erase command string, or 0 if n/a */
    0,                  /* lock command string, or 0 if n/a */
    0,                  /* unlock command string, or 0 if n/a */
};
#endif

static int ataxx_eeprom[0x0080];

int ataxx_eeprom_r(int offset)
{
    static int ret;
#if USE_EEPROM
    ret=EEPROM_read_bit() ? 0x01 : 0x00;
#else
    ret^=0x01;
#endif

#if NOISY_EEPROM
	if (errorlog)
	{
		fprintf(errorlog, "PC=%04x EEPROM read\n", cpu_get_pc());
	}
#endif

    /* World Soccer Finals uses bit 0x04 for the service switch */
    ret &= (~0x04);
    if (!keyboard_pressed(KEYCODE_F2))
    {
        ret |=0x04;
    }
    return ret;
}

void ataxx_eeprom_w(int offset, int data)
{
	/*
	ATAXX - Routine at 0x5ab9 tests the EEPROM. Appears to be 16 bits.
	ATAXX - Routine at 0x7587 also does something with the EEPROM
	*/
#if USE_EEPROM
    EEPROM_set_cs_line(~data & 0x40);
    EEPROM_set_clock_line(data &  0x20 );
    EEPROM_write_bit(data & 0x10);
#endif

#if NOISY_EEPROM
	if (errorlog)
	{
		fprintf(errorlog, "PC=%04x EEPROM write = %02x\n",
			cpu_get_pc(), data);
	}
#endif
}

/***********************************************************************

   BATTERY BACKED RAM

************************************************************************/

#define ataxx_battery_ram_size 0x4000
static unsigned char ataxx_battery_ram[ataxx_battery_ram_size];

void ataxx_battery_w(int offset, int data)
{
    ataxx_battery_ram[offset]=data;
}

static void nvram_handler(void *file,int read_or_write)
{
	if (read_or_write)
	{
		/* Battery backed RAM */
		osd_fwrite (file, ataxx_battery_ram, ataxx_battery_ram_size);
		/* EEPROM */
		osd_fwrite_msbfirst (file, ataxx_eeprom, sizeof(ataxx_eeprom));
#if USE_EEPROM
		EEPROM_save(file);
#endif
	}
	else
	{
		if (file)
		{
			/* Battery backed RAM */
			osd_fread (file, ataxx_battery_ram, ataxx_battery_ram_size);
			/* EEPROM */
			osd_fread_msbfirst (file, ataxx_eeprom, sizeof(ataxx_eeprom));
#if USE_EEPROM
			EEPROM_load(file);
#endif
		}
		else
		{
			memset(ataxx_battery_ram, 0, ataxx_battery_ram_size);
			memset(ataxx_eeprom, 0, sizeof(ataxx_eeprom));
#if USE_EEPROM
			EEPROM_init(&ataxx_eeprom_interface);
#endif
		}
	}
}

#ifdef MAME_DEBUG
void ataxx_debug_dump_driver(void)
{
    if (keyboard_pressed(KEYCODE_M))
	{
		static int marker=1;
        while (keyboard_pressed(KEYCODE_M))       ;

		if (errorlog)
		{
			fprintf(errorlog, "Marker %d\n", marker);
			marker++;
		}
	}
    if (keyboard_pressed(KEYCODE_F))
	{
		FILE *fp=fopen("MASTER.DMP", "w+b");
		if (fp)
		{
			unsigned char *RAM = memory_region(REGION_CPU1);
			fwrite(RAM, 0x10000, 1, fp);
			fclose(fp);
		}
		fp=fopen("SLAVE.DMP", "w+b");
		if (fp)
		{
			unsigned char *RAM = memory_region(REGION_CPU2);
			fwrite(RAM, 0x10000, 1, fp);
			fclose(fp);
		}
		fp=fopen("SOUND.DMP", "w+b");
		if (fp)
		{
			unsigned char *RAM = memory_region(REGION_CPU3);
			int size = memory_region_length(REGION_CPU3);
			if (size != 1)
			{
				fwrite(RAM, size, 1, fp);
			}
			fclose(fp);
		}

		fp=fopen("BATTERY.DMP", "w+b");
		if (fp)
		{
            fwrite(ataxx_battery_ram, ataxx_battery_ram_size, 1, fp);
			fclose(fp);
		}

	}
}
#else
#define ataxx_debug_dump_driver()
#endif


/***********************************************************************

   SLAVE (16MHz 80186 MUSIC CPU)

   3 x  8 bit DAC
   1 x 10 bit DAC

************************************************************************/

static int ataxx_sound_cmd_low;
static int ataxx_sound_cmd_high;
static int ataxx_sound_response;

static struct DACinterface dac_interface =
{
	MAX_DAC,  /* 8 chips */
	{255,255,255,255,},    /* Volume */
};

static struct CustomSound_interface custom_interface =
{
    leland_sh_start,
    leland_sh_stop,
    leland_sh_update
};

void ataxx_sound_init(void)
{
    ataxx_sound_cmd_low=0x55;
    ataxx_sound_cmd_high=0x55;
    ataxx_sound_response=0x55;
}

void ataxx_sound_cmd_low_w(int offset, int data)
{
	/* Z80 sound command low byte write */
    ataxx_sound_cmd_low=data;
}

void ataxx_sound_cmd_high_w(int offset, int data)
{
	/* Z80 sound command high byte write */
    ataxx_sound_cmd_high=data;
}

int ataxx_sound_cmd_r(int offset)
{
	/* 80186 sound command word read */
	if (!offset)
        return ataxx_sound_cmd_low;
	else
        return ataxx_sound_cmd_high;
}

int ataxx_sound_response_r(int offset)
{
	/* Z80 sound response byte read */
    return ataxx_sound_response;
}

void ataxx_sound_response_w(int offset, int data)
{
	/* 80186 sound response byte write */
	if (!offset)
        ataxx_sound_response=data;
}

int ataxx_i86_ram_r(int offset)
{
	/*
	Not very tidy, but it works for now...
	*/
	unsigned char *RAM = memory_region(REGION_CPU3);
    return RAM[0x0c000+offset];
}

void ataxx_i86_ram_w(int offset, int data)
{
	/*
	Not very tidy, but it works for now...
	*/
	unsigned char *RAM = memory_region(REGION_CPU3);
    RAM[0x0c000+offset]=data;
}

static int ataxx_i86_unknown_port1_r(int offset)
{
    static int s;

    //  Seems to be a status bit (0x10 from a word port read)
    if (!offset)
    {
        s^=0x10;
    }
    return s;
}

static struct IOReadPort ataxx_i86_readport[] =
{
    { 0x4000, 0x4001, MRA_NOP },
    { 0x4080, 0x4081, ataxx_sound_cmd_r }, /* Sound command word */
    { 0x432e, 0x432f, ataxx_i86_unknown_port1_r },

    { -1 }  /* end of table */
};

static struct IOWritePort ataxx_i86_writeport[] =
{
    { 0x4000, 0x4001, MWA_NOP },
    { 0x4080, 0x4081, ataxx_sound_response_w },    /* Sound response byte */
	{ -1 }  /* end of table */
};

static struct MemoryReadAddress ataxx_i86_readmem[] =
{
    { 0x000000, 0x003fff, ataxx_i86_ram_r },   /* vectors live here */
    { 0x00c000, 0x00ffff, MRA_RAM },
    { 0x020000, 0x07ffff, MRA_ROM },    /* Program ROM */
    { 0x0c0000, 0x0fffff, MRA_ROM },    /* Program ROM */

	{ -1 }  /* end of table */
};

static struct MemoryWriteAddress ataxx_i86_writemem[] =
{
    { 0x000000, 0x003fff, ataxx_i86_ram_w },   /* vectors live here */
    { 0x00c000, 0x00ffff, MWA_RAM }, /* 64K RAM (also appears at 0x00000) */
    { 0x020000, 0x0fffff, MWA_ROM }, /* Program ROM */
	{ -1 }  /* end of table */
};

int ataxx_i86_interrupt(void)
{
	return ignore_interrupt();
}

void ataxx_sound_control_w(int offset, int data)
{
	/*
		0x01=Reset
		0x02=NMI
		0x04=Int0
		0x08=Int1
		0x10=Test
	*/

	int intnum;

    cpu_set_reset_line(2, data&0x01  ? CLEAR_LINE : ASSERT_LINE);
    cpu_set_nmi_line(2,   data&0x02  ? CLEAR_LINE : ASSERT_LINE);
    cpu_set_test_line(2,  data&0x10  ? CLEAR_LINE : ASSERT_LINE);

	/* No idea about the int 0 and int 1 pins (do they give int number?) */
	intnum =(data&0x04)>>2;  /* Int 0 */
	intnum|=(data&0x08)>>2;  /* Int 1 */

#ifdef NOISY_SOUND_CPU
	if (errorlog)
	{
		 fprintf(errorlog, "PC=%04x Sound CPU intnum=%02x\n",
			cpu_get_pc(), intnum);
	}
#endif
}

/***********************************************************************

  Graphic tiles

************************************************************************/

static struct GfxLayout ataxx_tilelayout =
{
  8,8,  /* 8 wide by 8 high */
  16*1024, /* 128k/8 characters */
  6,    /* 6 bits per pixel, each ROM holds one bit */
  { 8*0xa0000, 8*0x80000, 8*0x60000, 8*0x40000, 8*0x20000, 8*0x00000 }, /* plane */
	{ 0, 1, 2, 3, 4, 5, 6, 7 },
	{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
	8*8
};

static struct GfxDecodeInfo ataxx_gfxdecodeinfo[] =
{
  { REGION_GFX1, 0, &ataxx_tilelayout, 0, 64 },
  { -1 } /* end of array */
};


/***********************************************************************

   SLAVE (Z80 graphics blitter)

************************************************************************/

void ataxx_slave_banksw_w(int offset, int data)
{
	unsigned char *RAM = memory_region(REGION_CPU2);
    int bank=data&0x0f;
    int bankaddress=0x10000*bank;
    if (!bank)
    {
        bankaddress=0x2000;
    }
    else
    {
        if (data&0x10)
        {
            bankaddress+=0x8000;
        }
    }
    cpu_setbank(3, &RAM[bankaddress]);

/*    if (errorlog)
    {
        fprintf(errorlog, "BANK=%02x\n", data );
    }
 */
}

int ataxx_raster_r(int offset)
{
    static int r;
    r++;
    //return r;
    return cpu_getscanline();
}


static struct IOReadPort ataxx_slave_readport[] =
{
    { 0x60, 0x7b, ataxx_svram_port_r },
	{ -1 }  /* end of table */
};

static struct IOWritePort ataxx_slave_writeport[] =
{
    { 0x60, 0x7b, ataxx_svram_port_w },
//    { Z80_HALT_PORT, Z80_HALT_PORT, ataxx_slave_halt_w },
	{ -1 }  /* end of table */
};

static struct MemoryReadAddress ataxx_slave_readmem[] =
{
	{ 0x0000, 0x1fff, MRA_ROM },        /* Resident program ROM */
	{ 0x2000, 0x9fff, MRA_BANK3 },      /* Paged graphics ROM */
    { 0xa000, 0xdfff, MRA_ROM },        /* bank 0 ROM */
	{ 0xe000, 0xefff, MRA_RAM },
    { 0xfffe, 0xfffe, ataxx_raster_r },
	{ -1 }  /* end of table */
};

static struct MemoryWriteAddress ataxx_slave_writemem[] =
{
	{ 0x0000, 0x9fff, MWA_ROM },
	{ 0xe000, 0xefff, MWA_RAM },
    { 0xfffc, 0xfffd, leland_slave_video_addr_w },
	{ 0xffff, 0xffff, ataxx_slave_banksw_w },
	{ -1 }  /* end of table */
};


/***********************************************************************

   MASTER (game)

************************************************************************/

void ataxx_slave_cmd_w(int offset, int data)
{
	cpu_set_irq_line(1, 0, data&0x01 ? CLEAR_LINE : ASSERT_LINE);
	cpu_set_nmi_line(1,    data&0x04 ? CLEAR_LINE : ASSERT_LINE);
	cpu_set_reset_line(1,  data&0x10 ? CLEAR_LINE : ASSERT_LINE);
}

void ataxx_banksw_w(int offset, int data)
{
	unsigned char *RAM = memory_region(REGION_CPU1);
    int bank=data & 0x0f;
    /* BANK1: Program ROM bank */
    if (!bank)
    {
        cpu_setbank(1, &RAM[0x2000]);   /* First bank */
    }
    else
    {
        cpu_setbank(1, &RAM[0x8000*bank]);
    }

    /* BANK2: Battery/QRAM/ROM bank */
    cpu_setbank(2, &RAM[0xa000]); /* Program ROM */
    if (data & 0x10)              /* Battery RAM */
	{
        cpu_setbank(2, &ataxx_battery_ram[0]);
	}
	else
	{
        if (data & 0x20)          /* QRAM selector */
        {
            int qbank=data & 0x80 ? 0x4000 : 0; /* QRAM high bank */
            if (data & 0x40)      /* QRAM 2 */
            {
                cpu_setbank(2, &ataxx_qram2[qbank]);
            }
            else                 /* QRAM 1 */
            {
                cpu_setbank(2, &ataxx_qram1[qbank]);
            }
        }
	}

    if ((data & 0x30) == 0x30)
	{
		ataxx_palette_bank=1;   /* Palette ram write */
	}
	else
	{
		ataxx_palette_bank=0;
	}
    /*
    if (errorlog)
    {
        fprintf(errorlog, "MASTER BANK=%02x\n", data);
    }
    */
}

void ataxx_master_video_addr_w(int offset, int data)
{
	if (ataxx_palette_bank)
	{
		/* Writing to the palette */
		paletteram_xxxxRRRRGGGGBBBB_w(offset+0x7f8, data);
	}
	else
	{
		/* Writing to video ram registers */
		leland_master_video_addr_w(offset, data);
	}
}

int ataxx_master_interrupt(void)
{
    /* Debug dumping */
    ataxx_debug_dump_driver();

	/* Generate an interrupt */
	return interrupt();
}

static int ataxx_xrom_address;

int ataxx_xrom1_data_r(int offset)
{
    if (ataxx_palette_bank)
    {
		/* Writing to the palette */
        return paletteram_r(offset+0x7fc);
    }
    else
    {
        unsigned char *XROM = memory_region(REGION_USER1);
        int ret=XROM[ataxx_xrom_address];
        if (errorlog)
        {
            fprintf(errorlog, "XROM1 READ %04x=%02x\n",  ataxx_xrom_address, ret);
        }
        ataxx_xrom_address++;
        ataxx_xrom_address&=0x1ffff;
        return ret;
    }
}

int ataxx_xrom2_data_r(int offset)
{
    if (ataxx_palette_bank)
    {
		/* Writing to the palette */
        return paletteram_r(offset+0x7fd);
    }
    else
    {
        unsigned char *XROM = memory_region(REGION_USER1);
        int ret=XROM[0x20000+ataxx_xrom_address];
        if (errorlog)
        {
            fprintf(errorlog, "XROM2 READ %04x=%02x\n",  ataxx_xrom_address, ret);
        }
        ataxx_xrom_address++;
        ataxx_xrom_address&=0x1ffff;
        return ret;
    }
}

void ataxx_xrom_addr_w(int offset, int data)
{
    if (ataxx_palette_bank)
    {
		/* Writing to the palette */
        paletteram_xxxxRRRRGGGGBBBB_w(offset+0x7fc, data);
    }
    else
    {
        if (!offset)
        {
            ataxx_xrom_address=(ataxx_xrom_address&0xff00)|data;
        }
        else
        {
            ataxx_xrom_address=(ataxx_xrom_address&0x00ff)|(data<<8);
        }

        if (errorlog)
        {
            fprintf(errorlog, "XROM ADDRESS = %04x\n", data);
        }
    }
}

static struct MemoryReadAddress ataxx_readmem[] =
{
	{ 0x0000, 0x1fff, MRA_ROM },
    { 0x2000, 0x9fff, MRA_BANK1 },
    { 0xa000, 0xdfff, MRA_BANK2 },
	{ 0xe000, 0xefff, MRA_RAM },
	{ 0xf000, 0xf7ff, MRA_RAM },
    { 0xfffc, 0xfffc, ataxx_xrom1_data_r },
    { 0xfffd, 0xfffd, ataxx_xrom2_data_r },
	{ 0xf800, 0xffff, paletteram_r },
	{ -1 }  /* end of table */
};

static struct MemoryWriteAddress ataxx_writemem[] =
{
	{ 0xa000, 0xdfff, MWA_BANK2 },
	{ 0x0000, 0xdfff, MWA_ROM },
	{ 0xe000, 0xefff, MWA_RAM },
	{ 0xf000, 0xf7ff, MWA_RAM, &ataxx_tram, &ataxx_tram_size },
	{ 0xfff8, 0xfff9, ataxx_master_video_addr_w },
    { 0xfffc, 0xfffd, ataxx_xrom_addr_w },
	{ 0xf800, 0xffff, paletteram_xxxxRRRRGGGGBBBB_w, &paletteram },
	{ -1 }  /* end of table */
};

static struct IOReadPort ataxx_readport[] =
{
	{ 0x00, 0x00, input_port_2_r },         /* Player 1 Track X */
	{ 0x00, 0x01, input_port_3_r },         /* Player 1 Track Y */
	{ 0x00, 0x02, input_port_4_r },         /* Player 2 Track X */
	{ 0x00, 0x03, input_port_5_r },         /* Player 2 Track Y */
    { 0x04, 0x04, ataxx_sound_response_r }, /* Sound comms read port */
    { 0x0d, 0x0d, input_port_6_r },         /* INDY HEAT */
    { 0x0e, 0x0e, input_port_7_r },         /* INDY HEAT */
    { 0x0f, 0x0f, input_port_8_r },         /* INDY HEAT */

    { 0x20, 0x20, ataxx_eeprom_r },         /* EEPROM */
	{ 0xd0, 0xe7, ataxx_mvram_port_r },     /* Master video RAM ports */
	{ 0xf6, 0xf6, input_port_1_r },         /* Buttons */
    { 0xf7, 0xf7, ataxx_input_port_0_r },   /* Slave block (slvblk) */
	{ -1 }  /* end of table */
};

static struct IOWritePort ataxx_writeport[] =
{
    { 0x05, 0x05, ataxx_sound_cmd_high_w },
    { 0x06, 0x06, ataxx_sound_cmd_low_w },
    { 0x0c, 0x0c, ataxx_sound_control_w },  /* Sound CPU control register */
    { 0x20, 0x20, ataxx_eeprom_w },         /* EEPROM */
	{ 0xd0, 0xe7, ataxx_mvram_port_w },     /* Master video RAM */
	{ 0xf0, 0xf0, leland_bk_xlow_w },       /* Probably ... always zero */
	{ 0xf1, 0xf1, leland_bk_xhigh_w },
	{ 0xf2, 0xf2, leland_bk_ylow_w },
	{ 0xf3, 0xf3, leland_bk_yhigh_w },
	{ 0xf4, 0xf4, ataxx_banksw_w },         /* Bank switch */
    { 0xf5, 0xf5, ataxx_slave_cmd_w },      /* Slave output (slvo) */
	{ 0xf8, 0xf8, MWA_NOP },                /* Unknown */
	{ -1 }  /* end of table */
};


void ataxx_init_machine(void)
{
	leland_rearrange_bank_swap(0, 0x10000);
	leland_rearrange_bank_swap(1, 0x10000);
}

/***********************************************************************

   ATAXX

************************************************************************/

INPUT_PORTS_START( ataxx )
	PORT_START /* (0xf7) */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_SLAVEHALT )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_VBLANK )
	PORT_BIT( 0xfc, IP_ACTIVE_LOW, IPT_UNUSED )

	PORT_START  /* (0xf6) */
	PORT_BIT (0x01, IP_ACTIVE_LOW, IPT_COIN1 )
	PORT_BIT (0x02, IP_ACTIVE_LOW, IPT_COIN2 )
	PORT_BIT (0x04, IP_ACTIVE_LOW, IPT_BUTTON3 )
    PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_SERVICE, "Service", KEYCODE_F2, IP_JOY_NONE)
	PORT_BIT (0x10, IP_ACTIVE_LOW, IPT_START1 )
	PORT_BIT (0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 )
	PORT_BIT (0x40, IP_ACTIVE_LOW, IPT_START2 )
	PORT_BIT (0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 )

	PORT_START
	PORT_ANALOG( 0xff, 0x00, IPT_TRACKBALL_X | IPF_PLAYER1, 100, 10, 0, 255 ) /* Sensitivity, clip, min, max */

	PORT_START
	PORT_ANALOG( 0xff, 0x00, IPT_TRACKBALL_Y | IPF_PLAYER1, 100, 10, 0, 255 )

	PORT_START
	PORT_ANALOG( 0xff, 0x00, IPT_TRACKBALL_X | IPF_PLAYER2, 100, 10, 0, 255 ) /* Sensitivity, clip, min, max */

	PORT_START
	PORT_ANALOG( 0xff, 0x00, IPT_TRACKBALL_Y | IPF_PLAYER2, 100, 10, 0, 255 )

    /* PORT 6, 7, 8 not used */
INPUT_PORTS_END


void ataxx_kludge_init_machine(void)
{
    unsigned char *RAM = memory_region(REGION_CPU1);
    ataxx_init_machine();

    /* Hack!!!! Patch the code to get the game to start */
    RAM[0x3593]=0;
    RAM[0x3594]=0;
}

ATAXX_MACHINE_DRIVER(kludge,ataxx_readport, ataxx_writeport,
    ataxx_readmem, ataxx_writemem, ataxx_kludge_init_machine, ataxx_gfxdecodeinfo,
	ataxx_vh_screenrefresh, ataxx_slave_readmem, ataxx_slave_writemem);

ATAXX_MACHINE_DRIVER(ataxx,ataxx_readport, ataxx_writeport,
    ataxx_readmem, ataxx_writemem, ataxx_init_machine, ataxx_gfxdecodeinfo,
	ataxx_vh_screenrefresh, ataxx_slave_readmem, ataxx_slave_writemem);

ROM_START( ataxx )
    ROM_REGION( 0x80000, REGION_CPU1 )
	ROM_LOAD( "ataxx.038",   0x00000, 0x20000, 0x0e1cf6236 )

	ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    ROM_LOAD( "ataxx.098",  0x00000, 0x20000, 0x059d0f2ae )
	ROM_LOAD( "ataxx.099",  0x20000, 0x20000, 0x06ab7db25 )
	ROM_LOAD( "ataxx.100",  0x40000, 0x20000, 0x02352849e )
	ROM_LOAD( "ataxx.101",  0x60000, 0x20000, 0x04c31e02b )
	ROM_LOAD( "ataxx.102",  0x80000, 0x20000, 0x0a951228c )
	ROM_LOAD( "ataxx.103",  0xa0000, 0x20000, 0x0ed326164 )

    ROM_REGION( 0x100000, REGION_CPU2 ) /* 1M for secondary cpu */
    ROM_LOAD( "ataxx.111",  0x00000, 0x20000, 0x09a3297cc )
    ROM_LOAD( "ataxx.112",  0x20000, 0x20000, 0x07e7c3e2f )
    ROM_LOAD( "ataxx.113",  0x40000, 0x20000, 0x08cf3e101 )

	ROM_REGION( 0x100000, REGION_CPU3 ) /* 1M for sound cpu */
    ROM_LOAD_V20_EVEN( "ataxx.015",  0x80000, 0x20000, 0x08bb3233b )
    ROM_LOAD_V20_ODD ( "ataxx.001",  0x80000, 0x20000, 0x0728d75f2 )
    ROM_LOAD_V20_EVEN( "ataxx.016",  0xC0000, 0x20000, 0x0f2bdff48 ) /* BAD in self-test */
    ROM_LOAD_V20_ODD ( "ataxx.002",  0xC0000, 0x20000, 0x0ca06a394 ) /* BAD in self-test */

    ROM_REGION( 0x40000, REGION_USER1 ) /* X-ROM (data used by main processor) */
    /* Empty / not used */
ROM_END

ROM_START( ataxxa )
    ROM_REGION( 0x80000, REGION_CPU1 )
    ROM_LOAD( "u38",   0x00000, 0x20000, 0x3378937d )

	ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    ROM_LOAD( "ataxx.098",  0x00000, 0x20000, 0x059d0f2ae )
	ROM_LOAD( "ataxx.099",  0x20000, 0x20000, 0x06ab7db25 )
	ROM_LOAD( "ataxx.100",  0x40000, 0x20000, 0x02352849e )
	ROM_LOAD( "ataxx.101",  0x60000, 0x20000, 0x04c31e02b )
	ROM_LOAD( "ataxx.102",  0x80000, 0x20000, 0x0a951228c )
	ROM_LOAD( "ataxx.103",  0xa0000, 0x20000, 0x0ed326164 )

    ROM_REGION( 0x100000, REGION_CPU2 ) /* 1M for secondary cpu */
    ROM_LOAD( "ataxx.111",  0x00000, 0x20000, 0x09a3297cc )
    ROM_LOAD( "ataxx.112",  0x20000, 0x20000, 0x07e7c3e2f )
    ROM_LOAD( "ataxx.113",  0x40000, 0x20000, 0x08cf3e101 )

	ROM_REGION( 0x100000, REGION_CPU3 ) /* 1M for sound cpu */
    ROM_LOAD_V20_EVEN( "ataxx.015",  0x80000, 0x20000, 0x08bb3233b )
    ROM_LOAD_V20_ODD ( "ataxx.001",  0x80000, 0x20000, 0x0728d75f2 )
    ROM_LOAD_V20_EVEN( "ataxx.016",  0xc0000, 0x20000, 0x0f2bdff48 ) /* BAD in self-test */
    ROM_LOAD_V20_ODD ( "ataxx.002",  0xc0000, 0x20000, 0x0ca06a394 ) /* BAD in self-test */

    ROM_REGION( 0x40000, REGION_USER1 ) /* X-ROM (data used by main processor) */
    /* Empty / not used */
ROM_END

/***************************************************************************

   Indy Heat

***************************************************************************/

INPUT_PORTS_START( indyheat )
	PORT_START /* (0xf7) */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_SLAVEHALT )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_VBLANK )
    PORT_BIT( 0xfc, IP_ACTIVE_LOW, IPT_COIN3 )

	PORT_START  /* (0xf6) */
    PORT_BIT (0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT (0x02, IP_ACTIVE_LOW, IPT_COIN2 )
    PORT_BIT (0x04, IP_ACTIVE_LOW, IPT_COIN1 )
    PORT_BIT (0x08, IP_ACTIVE_LOW, IPT_COIN3 )
	PORT_BIT (0x10, IP_ACTIVE_LOW, IPT_START1 )
    PORT_BIT (0x20, IP_ACTIVE_LOW, IPT_COIN2 )
    PORT_BIT (0x40, IP_ACTIVE_LOW, IPT_START1 )
    PORT_BIT (0x80, IP_ACTIVE_LOW, IPT_COIN2 )

	PORT_START
	PORT_ANALOG( 0xff, 0x00, IPT_TRACKBALL_X | IPF_PLAYER1, 100, 10, 0, 255 ) /* Sensitivity, clip, min, max */

	PORT_START
	PORT_ANALOG( 0xff, 0x00, IPT_TRACKBALL_Y | IPF_PLAYER1, 100, 10, 0, 255 )

	PORT_START
	PORT_ANALOG( 0xff, 0x00, IPT_TRACKBALL_X | IPF_PLAYER2, 100, 10, 0, 255 ) /* Sensitivity, clip, min, max */

	PORT_START
	PORT_ANALOG( 0xff, 0x00, IPT_TRACKBALL_Y | IPF_PLAYER2, 100, 10, 0, 255 )

    PORT_START /* (0xff) */
    PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 )
    PORT_START /* (0xff) */
    PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 )
    PORT_START /* (0xff) */
    PORT_BITX(0xf0, IP_ACTIVE_LOW, IPT_SERVICE, "Service", KEYCODE_F2, IP_JOY_NONE )
    PORT_BIT( 0x0f, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 )
INPUT_PORTS_END

ROM_START( indyheat )
    ROM_REGION( 0x80000, REGION_CPU1 )
    ROM_LOAD( "u64_27c.010",   0x00000, 0x20000, 0x00000000)  /* 0,1,2,3 - SUSPECT */
    ROM_LOAD( "u65_27c.010",   0x20000, 0x20000, 0x71301d74)  /* 4,5,6,7 */
    ROM_LOAD( "u66_27c.010",   0x40000, 0x20000, 0xc9612072)  /* 8,9,a,b */
    ROM_LOAD( "u67_27c.010",   0x60000, 0x20000, 0x4c4b25e0)  /* c,d,e,f */

	ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    ROM_LOAD( "u145_27c.010",  0x00000, 0x20000, 0x612d4bf8 )
    ROM_LOAD( "u146_27c.010",  0x20000, 0x20000, 0x77a725f6 )
    ROM_LOAD( "u147_27c.010",  0x40000, 0x20000, 0xd6aac372 )
    ROM_LOAD( "u148_27c.010",  0x60000, 0x20000, 0x5d19723e )
    ROM_LOAD( "u149_27c.010",  0x80000, 0x20000, 0x29056791 )
    ROM_LOAD( "u150_27c.010",  0xa0000, 0x20000, 0xcb73dd6a )

    ROM_REGION( 0x100000, REGION_CPU2 ) /* 1M for secondary cpu */
    ROM_LOAD( "u151_27c.010",  0x00000, 0x20000, 0x2622dfa4 )
    ROM_LOAD( "u152_27c.010",  0x20000, 0x20000, 0x00000000 ) /* BAD in self-test */
    ROM_LOAD( "u153_27c.010",  0x40000, 0x20000, 0x00000000 ) /* BAD in self-test */
    ROM_LOAD( "u154_27c.010",  0x60000, 0x20000, 0x76d3c235 )
    ROM_LOAD( "u155_27c.010",  0x80000, 0x20000, 0xd5d866b3 )
    ROM_LOAD( "u156_27c.010",  0xa0000, 0x20000, 0x7fe71842 )
    ROM_LOAD( "u157_27c.010",  0xc0000, 0x20000, 0xa6462adc )
    ROM_LOAD( "u158_27c.010",  0xe0000, 0x20000, 0xd6ef27a3 )

    ROM_REGION( 0x100000, REGION_CPU3 ) /* 1M for sound cpu */
    ROM_LOAD_V20_EVEN( "u6_27c.010",  0x20000, 0x20000, 0x15a89962 )  /* BAD in self-test */
    ROM_LOAD_V20_ODD ( "u3_27c.010",  0x20000, 0x20000, 0x97413818 )  /* BAD in self-test */
    ROM_LOAD_V20_WIDE( "u8_27c.010",  0x40000, 0x20000, 0x9f16e5b6 )  /* BAD in self-test */
    ROM_LOAD_V20_WIDE( "u9_27c.010",  0x60000, 0x20000, 0x0dc8f488 )  /* BAD in self-test */
    /* 0x80000-0xfffff = blank */
    ROM_LOAD_V20_EVEN( "u4_27c.010",  0xc0000, 0x20000, 0xfa7bfa04 )  /* BAD in self-test */
    ROM_LOAD_V20_ODD ( "u5_27c.010",  0xc0000, 0x20000, 0x198285d4 )  /* BAD in self-test */

    ROM_REGION( 0x40000, REGION_USER1 ) /* X-ROM (data used by main processor) */
    ROM_LOAD( "u68_27c.010",   0x00000, 0x20000, 0x9e88efb3)
    ROM_LOAD( "u69_27c.010",   0x20000, 0x20000, 0xaa39fcb3)
ROM_END

/***************************************************************************

    World Soccer Finals

***************************************************************************/

INPUT_PORTS_START( wsf )
	PORT_START /* (0xf7) */
    PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_SLAVEHALT )
    PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_VBLANK )
    PORT_BIT( 0xfc, IP_ACTIVE_LOW, IPT_BUTTON1 )

	PORT_START  /* (0xf6) */
	PORT_BIT (0x01, IP_ACTIVE_LOW, IPT_COIN1 )
    PORT_BIT (0x02, IP_ACTIVE_LOW, IPT_COIN2 )
    PORT_BIT (0x04, IP_ACTIVE_LOW, IPT_COIN3 )
    PORT_BIT (0x08, IP_ACTIVE_LOW, IPT_COIN4 )
    PORT_BIT (0xF0, IP_ACTIVE_LOW, IPT_BUTTON2|IPF_PLAYER1 )

	PORT_START
	PORT_ANALOG( 0xff, 0x00, IPT_TRACKBALL_X | IPF_PLAYER1, 100, 10, 0, 255 ) /* Sensitivity, clip, min, max */

	PORT_START
	PORT_ANALOG( 0xff, 0x00, IPT_TRACKBALL_Y | IPF_PLAYER1, 100, 10, 0, 255 )

	PORT_START
	PORT_ANALOG( 0xff, 0x00, IPT_TRACKBALL_X | IPF_PLAYER2, 100, 10, 0, 255 ) /* Sensitivity, clip, min, max */

	PORT_START
	PORT_ANALOG( 0xff, 0x00, IPT_TRACKBALL_Y | IPF_PLAYER2, 100, 10, 0, 255 )

    PORT_START /*  */
    PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER3)
    PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3)
    PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER3)
    PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER3)
    PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1)
    PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1)
    PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1)
    PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1)

    PORT_START /*  */
    PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER4)
    PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4)
    PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER4)
    PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER4)
    PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2)
    PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2)
    PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2)
    PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2)

    PORT_START /* (0xff) */
    PORT_BIT (0x80, IP_ACTIVE_LOW, IPT_START2 )
    PORT_BIT (0x40, IP_ACTIVE_LOW, IPT_BUTTON1|IPF_PLAYER2 )  /* A Button */
    PORT_BIT (0x20, IP_ACTIVE_LOW, IPT_START4 )
    PORT_BIT (0x10, IP_ACTIVE_LOW, IPT_BUTTON1|IPF_PLAYER4 )  /* A Button */
    PORT_BIT (0x08, IP_ACTIVE_LOW, IPT_START1 )
    PORT_BIT (0x04, IP_ACTIVE_LOW, IPT_BUTTON1|IPF_PLAYER1 )  /* A Button */
    PORT_BIT (0x02, IP_ACTIVE_LOW, IPT_START3 )
    PORT_BIT (0x01, IP_ACTIVE_LOW, IPT_BUTTON1|IPF_PLAYER3 )  /* A Button */
INPUT_PORTS_END

ROM_START( wsf )
    ROM_REGION( 0x80000, REGION_CPU1 )
    ROM_LOAD( "30022-03.u64",  0x00000, 0x20000, 0x2e7faa96)
    ROM_LOAD( "30023-03.u65",  0x20000, 0x20000, 0x7146328f)

	ROM_REGION( 0xc0000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    ROM_LOAD( "30011-02.145",  0x00000, 0x10000, 0x6153569b )
    ROM_LOAD( "30012-02.146",  0x20000, 0x10000, 0x52d65e21 )
    ROM_LOAD( "30013-02.147",  0x40000, 0x10000, 0xb3afda12 )
    ROM_LOAD( "30014-02.148",  0x60000, 0x10000, 0x624e6c64 )
    ROM_LOAD( "30015-01.149",  0x80000, 0x10000, 0x5d9064f2 )
    ROM_LOAD( "30016-01.150",  0xa0000, 0x10000, 0xd76389cd )

    ROM_REGION( 0x100000, REGION_CPU2 ) /* 1M for secondary cpu */
    ROM_LOAD( "30001-01.151",  0x00000, 0x20000, 0x31c63af5 )
    ROM_LOAD( "30002-01.152",  0x20000, 0x20000, 0xa53e88a6 )
    ROM_LOAD( "30003-01.153",  0x40000, 0x20000, 0x12afad1d )
    ROM_LOAD( "30004-01.154",  0x60000, 0x20000, 0xb8b3d59c )
    ROM_LOAD( "30005-01.155",  0x80000, 0x20000, 0x505724b9 )
    ROM_LOAD( "30006-01.156",  0xa0000, 0x20000, 0xc86b5c4d )
    ROM_LOAD( "30007-01.157",  0xc0000, 0x20000, 0x451321ae )
    ROM_LOAD( "30008-01.158",  0xe0000, 0x20000, 0x4d23836f )

    ROM_REGION( 0x100000, REGION_CPU3 ) /* 1M for sound cpu */
    ROM_LOAD_V20_EVEN( "30020-01.u6",  0x20000, 0x20000, 0x031a06d7 )
    ROM_LOAD_V20_ODD ( "30017-01.u3",  0x20000, 0x20000, 0x39ec13c1 )
    ROM_LOAD_V20_WIDE( "30021-01.u8",  0x40000, 0x20000, 0xbb91dc10 )
    /* U9 = empty ? */
    ROM_LOAD_V20_EVEN( "30018-01.u4",  0xc0000, 0x20000, 0x1ec16735 )
    ROM_LOAD_V20_ODD ( "30019-01.u5",  0xc0000, 0x20000, 0x2881f73b )

    ROM_REGION( 0x40000, REGION_USER1 ) /* X-ROM (data used by main processor) */
    ROM_LOAD( "30009-01.u68",   0x00000, 0x10000, 0xf2fbfc15)
    ROM_LOAD( "30010-01.u69",   0x20000, 0x10000, 0xb4ed2d3b)
ROM_END



GAMEX( 1990, ataxx,    0,     kludge, ataxx,    0, ROT0, "Leland Corp.", "Ataxx (set 1)", GAME_NOT_WORKING )
GAMEX( 1990, ataxxa,   ataxx, ataxx,  ataxx,    0, ROT0, "Leland Corp.", "Ataxx (set 2)", GAME_NOT_WORKING )
GAMEX( 1991, indyheat, 0,     ataxx,  indyheat, 0, ROT0, "Leland Corp.", "Indy Heat", GAME_NOT_WORKING )
GAMEX( 1990, wsf,      0,     ataxx,  wsf,      0, ROT0, "Leland Corp. USA", "World Soccer Finals", GAME_NOT_WORKING )
