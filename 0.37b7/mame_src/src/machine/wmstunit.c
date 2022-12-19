/*************************************************************************

	Driver for Williams/Midway T-unit games.

	Hints for finding speedups:
	
		search disassembly for ": CAF9"

**************************************************************************/

#include "driver.h"
#include "cpu/tms34010/tms34010.h"
#include "cpu/m6809/m6809.h"
#include "sndhrdw/williams.h"



/* constant definitions */
#define SOUND_ADPCM					1
#define SOUND_ADPCM_LARGE			2
#define SOUND_DCS					3


/* speedup installation macros */
#define INSTALL_SPEEDUP_1_16BIT(addr, pc, spin1, offs1, offs2) \
	wms_speedup_pc = (pc); \
	wms_speedup_offset = ((addr) & 0x10) >> 3; \
	wms_speedup_spin[0] = spin1; \
	wms_speedup_spin[1] = offs1; \
	wms_speedup_spin[2] = offs2; \
	wms_speedup_base = install_mem_read_handler(0, TOBYTE((addr) & ~0x1f), TOBYTE((addr) | 0x1f), wms_generic_speedup_1_16bit);
	
#define INSTALL_SPEEDUP_3(addr, pc, spin1, spin2, spin3) \
	wms_speedup_pc = (pc); \
	wms_speedup_offset = ((addr) & 0x10) >> 3; \
	wms_speedup_spin[0] = spin1; \
	wms_speedup_spin[1] = spin2; \
	wms_speedup_spin[2] = spin3; \
	wms_speedup_base = install_mem_read_handler(0, TOBYTE((addr) & ~0x1f), TOBYTE((addr) | 0x1f), wms_generic_speedup_3);
	

/* code-related variables */
extern UINT8 *	wms_code_rom;

/* CMOS-related variables */
extern UINT8 *	wms_cmos_ram;
static UINT8	cmos_write_enable;

/* graphics-related variables */
extern UINT8 *	wms_gfx_rom;
extern size_t	wms_gfx_rom_size;
extern UINT8	wms_gfx_rom_large;

/* sound-related variables */
static UINT8	sound_type;
static UINT8	fake_sound_state;

/* speedup-related variables */
extern offs_t 	wms_speedup_pc;
extern offs_t 	wms_speedup_offset;
extern offs_t 	wms_speedup_spin[3];
extern UINT8 *	wms_speedup_base;


/* speedup-related prototypes */
extern READ_HANDLER( wms_generic_speedup_1_16bit );
extern READ_HANDLER( wms_generic_speedup_3 );



/*************************************
 *
 *	CMOS reads/writes
 *
 *************************************/

WRITE_HANDLER( wms_tunit_cmos_enable_w )
{
	cmos_write_enable = 1;
}

WRITE_HANDLER( wms_tunit_cmos_w )
{
	if (1)/*cmos_write_enable)*/
	{
		COMBINE_WORD_MEM(&wms_cmos_ram[offset], data);
		cmos_write_enable = 0;
	}
	else
	{
		logerror("%08X:Unexpected CMOS W @ %05X\n", cpu_get_pc(), offset);
		usrintf_showmessage("Bad CMOS write");
	}
}

READ_HANDLER( wms_tunit_cmos_r )
{
	return READ_WORD(&wms_cmos_ram[offset]);
}



/*************************************
 *
 *	Generic input ports
 *
 *************************************/

READ_HANDLER( wms_tunit_input_r )
{
	return readinputport(offset / 2);
}



/*************************************
 *
 *	Mortal Kombat (T-unit) protection
 *
 *************************************/

static const UINT8 mk_prot_values[] =
{
	0x13, 0x27, 0x0f, 0x1f, 0x3e, 0x3d, 0x3b, 0x37,
	0x2e, 0x1c, 0x38, 0x31, 0x22, 0x05, 0x0a, 0x15,
	0x2b, 0x16, 0x2d, 0x1a, 0x34, 0x28, 0x10, 0x21,
	0x03, 0x06, 0x0c, 0x19, 0x32, 0x24, 0x09, 0x13,
	0x27, 0x0f, 0x1f, 0x3e, 0x3d, 0x3b, 0x37, 0x2e,
	0x1c, 0x38, 0x31, 0x22, 0x05, 0x0a, 0x15, 0x2b,
	0x16, 0x2d, 0x1a, 0x34, 0x28, 0x10, 0x21, 0x03,
	0xff
};
static UINT8 mk_prot_index;

static READ_HANDLER( mk_prot_r )
{
	logerror("%08X:Protection R @ %05X = %04X\n", cpu_get_pc(), offset, mk_prot_values[mk_prot_index] << 9);

	/* just in case */
	if (mk_prot_index >= sizeof(mk_prot_values))
	{
		logerror("%08X:Unexpected protection R @ %05X\n", cpu_get_pc(), offset);
		mk_prot_index = 0;
	}

	return mk_prot_values[mk_prot_index++] << 9;
}

static WRITE_HANDLER( mk_prot_w )
{
	int first_val = (data >> 9) & 0x3f;
	int i;

	/* find the desired first value and stop then */	
	for (i = 0; i < sizeof(mk_prot_values); i++)
		if (mk_prot_values[i] == first_val)
		{
			mk_prot_index = i;
			break;
		}
	
	/* just in case */
	if (i == sizeof(mk_prot_values))
	{
		logerror("%08X:Unhandled protection W @ %05X = %04X\n", cpu_get_pc(), offset, data);
		mk_prot_index = 0;
	}

	logerror("%08X:Protection W @ %05X = %04X\n", cpu_get_pc(), offset, data);
}

static READ_HANDLER( mk_mirror_r )
{
	/* probably not protection, just a bug in the code */
	return READ_WORD(&wms_code_rom[offset]);
}



/*************************************
 *
 *	Mortal Kombat 2 protection
 *
 *************************************/

static UINT16 mk2_prot_data;

static READ_HANDLER( mk2_prot_const_r )
{
	return 2;
}

static READ_HANDLER( mk2_prot_r )
{
	return mk2_prot_data;
}

static READ_HANDLER( mk2_prot_shift_r )
{
	return mk2_prot_data >> 1;
}

static WRITE_HANDLER( mk2_prot_w )
{
	mk2_prot_data = data;
}



/*************************************
 *
 *	NBA Jam protection
 *
 *************************************/

static const UINT32 nbajam_prot_values[128] =
{
	0x21283b3b, 0x2439383b, 0x31283b3b, 0x302b3938, 0x31283b3b, 0x302b3938, 0x232f2f2f, 0x26383b3b,
	0x21283b3b, 0x2439383b, 0x312a1224, 0x302b1120, 0x312a1224, 0x302b1120, 0x232d283b, 0x26383b3b,
	0x2b3b3b3b, 0x2e2e2e2e, 0x39383b1b, 0x383b3b1b, 0x3b3b3b1b, 0x3a3a3a1a, 0x2b3b3b3b, 0x2e2e2e2e,
	0x2b39383b, 0x2e2e2e2e, 0x393a1a18, 0x383b1b1b, 0x3b3b1b1b, 0x3a3a1a18, 0x2b39383b, 0x2e2e2e2e,
	0x01202b3b, 0x0431283b, 0x11202b3b, 0x1021283b, 0x11202b3b, 0x1021283b, 0x03273b3b, 0x06302b39,
	0x09302b39, 0x0c232f2f, 0x19322e06, 0x18312a12, 0x19322e06, 0x18312a12, 0x0b31283b, 0x0e26383b,
	0x03273b3b, 0x06302b39, 0x11202b3b, 0x1021283b, 0x13273938, 0x12243938, 0x03273b3b, 0x06302b39,
	0x0b31283b, 0x0e26383b, 0x19322e06, 0x18312a12, 0x1b332f05, 0x1a302b11, 0x0b31283b,	0x0e26383b,
	0x21283b3b, 0x2439383b, 0x31283b3b, 0x302b3938, 0x31283b3b, 0x302b3938, 0x232f2f2f, 0x26383b3b,
	0x21283b3b, 0x2439383b, 0x312a1224, 0x302b1120, 0x312a1224, 0x302b1120, 0x232d283b, 0x26383b3b,
	0x2b3b3b3b, 0x2e2e2e2e, 0x39383b1b, 0x383b3b1b, 0x3b3b3b1b, 0x3a3a3a1a, 0x2b3b3b3b, 0x2e2e2e2e,
	0x2b39383b, 0x2e2e2e2e, 0x393a1a18, 0x383b1b1b, 0x3b3b1b1b, 0x3a3a1a18, 0x2b39383b, 0x2e2e2e2e,
	0x01202b3b, 0x0431283b, 0x11202b3b, 0x1021283b, 0x11202b3b, 0x1021283b, 0x03273b3b, 0x06302b39,
	0x09302b39, 0x0c232f2f, 0x19322e06, 0x18312a12, 0x19322e06, 0x18312a12, 0x0b31283b, 0x0e26383b,
	0x03273b3b, 0x06302b39, 0x11202b3b, 0x1021283b, 0x13273938, 0x12243938, 0x03273b3b, 0x06302b39,
	0x0b31283b, 0x0e26383b, 0x19322e06, 0x18312a12, 0x1b332f05, 0x1a302b11, 0x0b31283b,	0x0e26383b
};

static const UINT32 nbajamte_prot_values[128] =
{
	0x00000000, 0x04081020, 0x08102000, 0x0c183122, 0x10200000, 0x14281020, 0x18312204, 0x1c393326,
	0x20000001, 0x24081021, 0x28102000, 0x2c183122, 0x30200001, 0x34281021, 0x38312204, 0x3c393326,
	0x00000102, 0x04081122, 0x08102102, 0x0c183122, 0x10200000, 0x14281020, 0x18312204, 0x1c393326,
	0x20000103, 0x24081123, 0x28102102, 0x2c183122, 0x30200001, 0x34281021, 0x38312204, 0x3c393326,
	0x00010204, 0x04091224, 0x08112204, 0x0c193326, 0x10210204, 0x14291224, 0x18312204, 0x1c393326,
	0x20000001, 0x24081021, 0x28102000, 0x2c183122, 0x30200001, 0x34281021, 0x38312204, 0x3c393326,
	0x00010306, 0x04091326, 0x08112306, 0x0c193326, 0x10210204, 0x14291224, 0x18312204, 0x1c393326,
	0x20000103, 0x24081123, 0x28102102, 0x2c183122, 0x30200001, 0x34281021, 0x38312204, 0x3c393326,
	0x00000000, 0x01201028, 0x02213018, 0x03012030, 0x04223138, 0x05022110, 0x06030120, 0x07231108,
	0x08042231, 0x09243219, 0x0a251229, 0x0b050201, 0x0c261309, 0x0d060321, 0x0e072311, 0x0f273339,
	0x10080422, 0x1128140a, 0x1229343a, 0x13092412, 0x142a351a, 0x150a2532, 0x160b0502, 0x172b152a,
	0x180c2613, 0x192c363b, 0x1a2d160b, 0x1b0d0623, 0x1c2e172b, 0x1d0e0703, 0x1e0f2733, 0x1f2f371b,
	0x20100804, 0x2130182c, 0x2231381c, 0x23112834, 0x2432393c, 0x25122914, 0x26130924, 0x2733190c,
	0x28142a35, 0x29343a1d, 0x2a351a2d, 0x2b150a05, 0x2c361b0d, 0x2d160b25, 0x2e172b15, 0x2f373b3d,
	0x30180c26, 0x31381c0e, 0x32393c3e, 0x33192c16, 0x343a3d1e, 0x351a2d36, 0x361b0d06, 0x373b1d2e,
	0x381c2e17, 0x393c3e3f, 0x3a3d1e0f, 0x3b1d0e27, 0x3c3e1f2f, 0x3d1e0f07, 0x3e1f2f37, 0x3f3f3f1f
};

static const UINT32 *nbajam_prot_table;
static UINT16 nbajam_prot_queue[5];
static UINT8 nbajam_prot_index;

static READ_HANDLER( nbajam_prot_r )
{
	int result = nbajam_prot_queue[nbajam_prot_index];
	if (nbajam_prot_index < 4)
		nbajam_prot_index++;
	return result;
}

static WRITE_HANDLER( nbajam_prot_w )
{
	int table_index = (offset >> 7) & 0x7f;
	UINT32 protval = nbajam_prot_table[table_index];
	
	nbajam_prot_queue[0] = data;
	nbajam_prot_queue[1] = ((protval >> 24) & 0xff) << 9;
	nbajam_prot_queue[2] = ((protval >> 16) & 0xff) << 9;
	nbajam_prot_queue[3] = ((protval >> 8) & 0xff) << 9;
	nbajam_prot_queue[4] = ((protval >> 0) & 0xff) << 9;
	nbajam_prot_index = 0;
}



/*************************************
 *
 *	Generic driver init
 *
 *************************************/

static void init_tunit_generic(int sound)
{
	offs_t gfx_chunk = wms_gfx_rom_size / 4;
	UINT8 *base;
	int i;
	
	/* set up code ROMs */
	memcpy(wms_code_rom, memory_region(REGION_USER1), memory_region_length(REGION_USER1));

	/* load the graphics ROMs -- quadruples */
	base = memory_region(REGION_GFX1);
	for (i = 0; i < wms_gfx_rom_size; i += 4)
	{
		wms_gfx_rom[i + 0] = base[0 * gfx_chunk + i / 4];
		wms_gfx_rom[i + 1] = base[1 * gfx_chunk + i / 4];
		wms_gfx_rom[i + 2] = base[2 * gfx_chunk + i / 4];
		wms_gfx_rom[i + 3] = base[3 * gfx_chunk + i / 4];
	}
	
	/* load sound ROMs and set up sound handlers */
	sound_type = sound;
	switch (sound)
	{
		case SOUND_ADPCM:
			base = memory_region(REGION_SOUND1);
			memcpy(base + 0xa0000, base + 0x20000, 0x20000);
			memcpy(base + 0x80000, base + 0x60000, 0x20000);
			memcpy(base + 0x60000, base + 0x20000, 0x20000);
			break;
			
		case SOUND_ADPCM_LARGE:
			base = memory_region(REGION_SOUND1);
			memcpy(base + 0x1a0000, base + 0x060000, 0x20000);	/* save common bank */

			memcpy(base + 0x180000, base + 0x080000, 0x20000);	/* expand individual banks */
			memcpy(base + 0x140000, base + 0x0a0000, 0x20000);
			memcpy(base + 0x100000, base + 0x0c0000, 0x20000);
			memcpy(base + 0x0c0000, base + 0x0e0000, 0x20000);
			memcpy(base + 0x080000, base + 0x000000, 0x20000);
			memcpy(base + 0x000000, base + 0x040000, 0x20000);
			memcpy(base + 0x040000, base + 0x020000, 0x20000);

			memcpy(base + 0x160000, base + 0x1a0000, 0x20000);	/* copy common bank */
			memcpy(base + 0x120000, base + 0x1a0000, 0x20000);
			memcpy(base + 0x0e0000, base + 0x1a0000, 0x20000);
			memcpy(base + 0x0a0000, base + 0x1a0000, 0x20000);
			memcpy(base + 0x020000, base + 0x1a0000, 0x20000);
			break;
		
		case SOUND_DCS:
			break;
	}

	/* default graphics functionality */
	wms_gfx_rom_large = 0;
}



/*************************************
 *
 *	T-unit init (ADPCM)
 *
 * 	music: 6809 driving YM2151, DAC, and OKIM6295
 *
 *************************************/

void init_mk(void)
{
	/* common init */
	init_tunit_generic(SOUND_ADPCM);

	/* protection */
	install_mem_read_handler (0, TOBYTE(0x1b00000), TOBYTE(0x1b6ffff), mk_prot_r);
	install_mem_write_handler(0, TOBYTE(0x1b00000), TOBYTE(0x1b6ffff), mk_prot_w);
	install_mem_read_handler (0, TOBYTE(0x1f800000), TOBYTE(0x1fffffff), mk_mirror_r);

	/* sound chip protection (hidden RAM) */
	install_mem_write_handler(1, 0xfb9c, 0xfbc6, MWA_RAM);

	/* speedups */
	INSTALL_SPEEDUP_3(0x01053360, 0xffce2100, 0x104f9d0, 0x104fa10, 0x104fa30);
}

static void init_nbajam_common(int te_protection)
{
	/* common init */
	init_tunit_generic(SOUND_ADPCM_LARGE);

	/* protection */
	if (!te_protection)
	{
		nbajam_prot_table = nbajam_prot_values;
		install_mem_read_handler (0, TOBYTE(0x1b14020), TOBYTE(0x1b2503f), nbajam_prot_r);
		install_mem_write_handler(0, TOBYTE(0x1b14020), TOBYTE(0x1b2503f), nbajam_prot_w);
	}
	else
	{
		nbajam_prot_table = nbajamte_prot_values;
		install_mem_read_handler (0, TOBYTE(0x1b15f40), TOBYTE(0x1b37f5f), nbajam_prot_r);
		install_mem_read_handler (0, TOBYTE(0x1b95f40), TOBYTE(0x1bb7f5f), nbajam_prot_r);
		install_mem_write_handler(0, TOBYTE(0x1b15f40), TOBYTE(0x1b37f5f), nbajam_prot_w);
		install_mem_write_handler(0, TOBYTE(0x1b95f40), TOBYTE(0x1bb7f5f), nbajam_prot_w);
	}

	/* sound chip protection (hidden RAM) */
	if (!te_protection)
		install_mem_write_handler(1, 0xfbaa, 0xfbd4, MWA_RAM);
	else
		install_mem_write_handler(1, 0xfbec, 0xfc16, MWA_RAM);
} 

void init_nbajam(void)
{
	init_nbajam_common(0);
	INSTALL_SPEEDUP_1_16BIT(0x010754c0, 0xff833480, 0x1008040, 0xd0, 0xb0);
}

void init_nbajam20(void)
{
	init_nbajam_common(0);
	INSTALL_SPEEDUP_1_16BIT(0x010754c0, 0xff833520, 0x1008040, 0xd0, 0xb0);
}

void init_nbajamte(void)
{
	init_nbajam_common(1);
	INSTALL_SPEEDUP_1_16BIT(0x0106d480, 0xff84e480, 0x1000040, 0xd0, 0xb0);
}



/*************************************
 *
 *	T-unit init (DCS)
 *
 * 	music: ADSP2105
 *
 *************************************/

static void init_mk2_common(void)
{
	/* common init */
	init_tunit_generic(SOUND_DCS);
	wms_gfx_rom_large = 1;

	/* protection */
	install_mem_write_handler(0, TOBYTE(0x00f20c60), TOBYTE(0x00f20c7f), mk2_prot_w);
	install_mem_write_handler(0, TOBYTE(0x00f42820), TOBYTE(0x00f4283f), mk2_prot_w);
	install_mem_read_handler (0, TOBYTE(0x01a190e0), TOBYTE(0x01a190ff), mk2_prot_r);
	install_mem_read_handler (0, TOBYTE(0x01a191c0), TOBYTE(0x01a191df), mk2_prot_shift_r);
	install_mem_read_handler (0, TOBYTE(0x01a3d0c0), TOBYTE(0x01a3d0ff), mk2_prot_r);
	install_mem_read_handler (0, TOBYTE(0x01d9d1e0), TOBYTE(0x01d9d1ff), mk2_prot_const_r);
	install_mem_read_handler (0, TOBYTE(0x01def920), TOBYTE(0x01def93f), mk2_prot_const_r);
} 

void init_mk2(void)
{
	init_mk2_common();
	INSTALL_SPEEDUP_3(0x01068e70, 0xff80db70, 0x105d480, 0x105d4a0, 0x105d4c0);
}

void init_mk2r14(void)
{
	init_mk2_common();
	INSTALL_SPEEDUP_3(0x01068de0, 0xff80d960, 0x105d480, 0x105d4a0, 0x105d4c0);
}

void init_mk2r32(void)
{
	init_mk2_common();
	INSTALL_SPEEDUP_3(0x01068e70, 0xff80db70, 0x105d480, 0x105d4a0, 0x105d4c0);
}



/*************************************
 *
 *	Machine init
 *
 *************************************/

void wms_tunit_init_machine(void)
{
	/* reset sound */
	switch (sound_type)
	{
		case SOUND_ADPCM:
		case SOUND_ADPCM_LARGE:
			williams_adpcm_init(1);
			break;
		
		case SOUND_DCS:
			williams_dcs_init(1);
			break;
	}
}



/*************************************
 *
 *	Sound write handlers
 *
 *************************************/

READ_HANDLER( wms_tunit_sound_state_r )
{
	logerror("%08X:Sound status read\n", cpu_get_pc());

	if ( sound_type == SOUND_DCS && Machine->sample_rate )
		return williams_dcs_control_r(0) >> 4;

	if (fake_sound_state)
	{
		fake_sound_state--;
		return 0;
	}
	return 0xffff;
}

READ_HANDLER( wms_tunit_sound_r )
{
	logerror("%08X:Sound data read\n", cpu_get_pc());

	if ( sound_type == SOUND_DCS && Machine->sample_rate )
		return williams_dcs_data_r(0);

	return 0xffff;
}

WRITE_HANDLER( wms_tunit_sound_w )
{
	/* check for out-of-bounds accesses */
	if (!offset)
	{
		logerror("%08X:Unexpected write to sound (lo) = %04X\n", cpu_get_pc(), data);
		return;
	}
	
	/* call through based on the sound type */
	switch (sound_type)
	{
		case SOUND_ADPCM:
		case SOUND_ADPCM_LARGE:
			williams_adpcm_reset_w(~data & 0x100);
			williams_adpcm_data_w(0, data & 0xff);

			/* the games seem to check for $82 loops, so this should be just barely enough */
			fake_sound_state = 128;
			break;
			
		case SOUND_DCS:
			logerror("%08X:Sound write = %04X\n", cpu_get_pc(), data);
			williams_dcs_reset_w(~data & 0x100);
			williams_dcs_data_w(0, data & 0xff);
			/* the games seem to check for $82 loops, so this should be just barely enough */
			fake_sound_state = 128;
			break;
	}
}
