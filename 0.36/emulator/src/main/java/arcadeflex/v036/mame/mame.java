/**
 * ported to v0.36
 */
package arcadeflex.v036.mame;

import static arcadeflex.v036.mame.cheat.he_did_cheat;
import static gr.codebb.arcadeflex.v036.mame.mame.playback;
import static gr.codebb.arcadeflex.v036.mame.mame.record;

public class mame {

    /*TODO*///#include "driver.h"
/*TODO*///#include <ctype.h>
/*TODO*///
/*TODO*///
/*TODO*///static struct RunningMachine machine;
/*TODO*///struct RunningMachine *Machine = &machine;
/*TODO*///static const struct GameDriver *gamedrv;
/*TODO*///static const struct MachineDriver *drv;
/*TODO*///
/*TODO*////* Variables to hold the status of various game options */
/*TODO*///struct GameOptions	options;
/*TODO*///
/*TODO*///FILE *errorlog;
/*TODO*///void *record;   /* for -record */
/*TODO*///void *playback; /* for -playback */
/*TODO*///int mame_debug; /* !0 when -debug option is specified */
/*TODO*///
/*TODO*///int bailing;	/* set to 1 if the startup is aborted to prevent multiple error messages */
/*TODO*///
/*TODO*///static int settingsloaded;
/*TODO*///
/*TODO*///int bitmap_dirty;	/* set by osd_clearbitmap() */
/*TODO*///
/*TODO*///
/*TODO*////* Used in vh_open */
/*TODO*///extern unsigned char *spriteram,*spriteram_2;
/*TODO*///extern unsigned char *buffered_spriteram,*buffered_spriteram_2;
/*TODO*///extern int spriteram_size,spriteram_2_size;
/*TODO*///
/*TODO*///int init_machine(void);
/*TODO*///void shutdown_machine(void);
/*TODO*///int run_machine(void);
/*TODO*///
/*TODO*///
/*TODO*///#ifdef MAME_DEBUG
/*TODO*///
/*TODO*///INLINE int my_stricmp( const char *dst, const char *src)
/*TODO*///{
/*TODO*///	while (*src && *dst)
/*TODO*///	{
/*TODO*///		if( tolower(*src) != tolower(*dst) ) return *dst - *src;
/*TODO*///		src++;
/*TODO*///		dst++;
/*TODO*///	}
/*TODO*///	return *dst - *src;
/*TODO*///}
/*TODO*///
/*TODO*///static int validitychecks(void)
/*TODO*///{
/*TODO*///	int i,j;
/*TODO*///	UINT8 a,b;
/*TODO*///	int error = 0;
/*TODO*///
/*TODO*///
/*TODO*///	a = 0xff;
/*TODO*///	b = a + 1;
/*TODO*///	if (b > a)	{ printf("UINT8 must be 8 bits\n"); error = 1; }
/*TODO*///
/*TODO*///	if (sizeof(INT8)   != 1)	{ printf("INT8 must be 8 bits\n"); error = 1; }
/*TODO*///	if (sizeof(UINT8)  != 1)	{ printf("UINT8 must be 8 bits\n"); error = 1; }
/*TODO*///	if (sizeof(INT16)  != 2)	{ printf("INT16 must be 16 bits\n"); error = 1; }
/*TODO*///	if (sizeof(UINT16) != 2)	{ printf("UINT16 must be 16 bits\n"); error = 1; }
/*TODO*///	if (sizeof(INT32)  != 4)	{ printf("INT32 must be 32 bits\n"); error = 1; }
/*TODO*///	if (sizeof(UINT32) != 4)	{ printf("UINT32 must be 32 bits\n"); error = 1; }
/*TODO*///	if (sizeof(INT64)  != 8)	{ printf("INT64 must be 64 bits\n"); error = 1; }
/*TODO*///	if (sizeof(UINT64) != 8)	{ printf("UINT64 must be 64 bits\n"); error = 1; }
/*TODO*///
/*TODO*///	for (i = 0;drivers[i];i++)
/*TODO*///	{
/*TODO*///		const struct RomModule *romp;
/*TODO*///		const struct InputPortTiny *inp;
/*TODO*///
/*TODO*///		if (drivers[i]->clone_of == drivers[i])
/*TODO*///		{
/*TODO*///			printf("%s is set as a clone of itself\n",drivers[i]->name);
/*TODO*///			error = 1;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (drivers[i]->clone_of && drivers[i]->clone_of->clone_of)
/*TODO*///		{
/*TODO*///			if ((drivers[i]->clone_of->clone_of->flags & NOT_A_DRIVER) == 0)
/*TODO*///			{
/*TODO*///				printf("%s is a clone of a clone\n",drivers[i]->name);
/*TODO*///				error = 1;
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		for (j = i+1;drivers[j];j++)
/*TODO*///		{
/*TODO*///			if (!strcmp(drivers[i]->name,drivers[j]->name))
/*TODO*///			{
/*TODO*///				printf("%s is a duplicate name (%s, %s)\n",drivers[i]->name,drivers[i]->source_file,drivers[j]->source_file);
/*TODO*///				error = 1;
/*TODO*///			}
/*TODO*///			if (!strcmp(drivers[i]->description,drivers[j]->description))
/*TODO*///			{
/*TODO*///				printf("%s is a duplicate description (%s, %s)\n",drivers[i]->description,drivers[i]->name,drivers[j]->name);
/*TODO*///				error = 1;
/*TODO*///			}
/*TODO*///			if (drivers[i]->rom && drivers[i]->rom == drivers[j]->rom
/*TODO*///					&& (drivers[i]->flags & NOT_A_DRIVER) == 0
/*TODO*///					&& (drivers[j]->flags & NOT_A_DRIVER) == 0)
/*TODO*///			{
/*TODO*///				printf("%s and %s use the same ROM set\n",drivers[i]->name,drivers[j]->name);
/*TODO*///				error = 1;
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		romp = drivers[i]->rom;
/*TODO*///
/*TODO*///		if (romp)
/*TODO*///		{
/*TODO*///			int region_type_used[REGION_MAX];
/*TODO*///			int region_length[REGION_MAX];
/*TODO*///			const char *last_name = 0;
/*TODO*///			int count = -1;
/*TODO*///
/*TODO*///			for (j = 0;j < REGION_MAX;j++)
/*TODO*///			{
/*TODO*///				region_type_used[j] = 0;
/*TODO*///				region_length[j] = 0;
/*TODO*///			}
/*TODO*///
/*TODO*///			while (romp->name || romp->offset || romp->length)
/*TODO*///			{
/*TODO*///				const char *c;
/*TODO*///
/*TODO*///				if (romp->name == 0 && romp->length == 0)	/* ROM_REGION() */
/*TODO*///				{
/*TODO*///					int type = romp->crc & ~REGIONFLAG_MASK;
/*TODO*///
/*TODO*///
/*TODO*///					count++;
/*TODO*///					if (type && (type >= REGION_MAX || type <= REGION_INVALID))
/*TODO*///					{
/*TODO*///						printf("%s has invalid ROM_REGION type %x\n",drivers[i]->name,type);
/*TODO*///						error = 1;
/*TODO*///					}
/*TODO*///
/*TODO*///					region_type_used[type]++;
/*TODO*///					region_length[type] = region_length[count] = romp->offset;
/*TODO*///				}
/*TODO*///				if (romp->name && romp->name != (char *)-1)
/*TODO*///				{
/*TODO*///					int pre,post;
/*TODO*///
/*TODO*///					last_name = c = romp->name;
/*TODO*///					while (*c)
/*TODO*///					{
/*TODO*///						if (tolower(*c) != *c)
/*TODO*///						{
/*TODO*///							printf("%s has upper case ROM name %s\n",drivers[i]->name,romp->name);
/*TODO*///							error = 1;
/*TODO*///						}
/*TODO*///						c++;
/*TODO*///					}
/*TODO*///
/*TODO*///					c = romp->name;
/*TODO*///					pre = 0;
/*TODO*///					post = 0;
/*TODO*///					while (*c && *c != '.')
/*TODO*///					{
/*TODO*///						pre++;
/*TODO*///						c++;
/*TODO*///					}
/*TODO*///					while (*c)
/*TODO*///					{
/*TODO*///						post++;
/*TODO*///						c++;
/*TODO*///					}
/*TODO*///					if (pre > 8 || post > 4)
/*TODO*///					{
/*TODO*///						printf("%s has >8.3 ROM name %s\n",drivers[i]->name,romp->name);
/*TODO*///						error = 1;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				if (romp->length != 0)						/* ROM_LOAD_XXX() */
/*TODO*///				{
/*TODO*///					if (romp->offset + (romp->length & ~ROMFLAG_MASK) > region_length[count])
/*TODO*///					{
/*TODO*///						printf("%s has ROM %s extending past the defined memory region\n",drivers[i]->name,last_name);
/*TODO*///						error = 1;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				romp++;
/*TODO*///			}
/*TODO*///
/*TODO*///			for (j = 1;j < REGION_MAX;j++)
/*TODO*///			{
/*TODO*///				if (region_type_used[j] > 1)
/*TODO*///				{
/*TODO*///					printf("%s has duplicated ROM_REGION type %x\n",drivers[i]->name,j);
/*TODO*///					error = 1;
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///
/*TODO*///			if (drivers[i]->drv->gfxdecodeinfo)
/*TODO*///			{
/*TODO*///				for (j = 0;j < MAX_GFX_ELEMENTS && drivers[i]->drv->gfxdecodeinfo[j].memory_region != -1;j++)
/*TODO*///				{
/*TODO*///					int len,avail,k,start;
/*TODO*///					int type = drivers[i]->drv->gfxdecodeinfo[j].memory_region;
/*TODO*///
/*TODO*///
/*TODO*////*
/*TODO*///					if (type && (type >= REGION_MAX || type <= REGION_INVALID))
/*TODO*///					{
/*TODO*///						printf("%s has invalid memory region for gfx[%d]\n",drivers[i]->name,j);
/*TODO*///						error = 1;
/*TODO*///					}
/*TODO*///*/
/*TODO*///
/*TODO*///					if (!IS_FRAC(drivers[i]->drv->gfxdecodeinfo[j].gfxlayout->total))
/*TODO*///					{
/*TODO*///						start = 0;
/*TODO*///						for (k = 0;k < MAX_GFX_PLANES;k++)
/*TODO*///						{
/*TODO*///							if (drivers[i]->drv->gfxdecodeinfo[j].gfxlayout->planeoffset[k] > start)
/*TODO*///								start = drivers[i]->drv->gfxdecodeinfo[j].gfxlayout->planeoffset[k];
/*TODO*///						}
/*TODO*///						start &= ~(drivers[i]->drv->gfxdecodeinfo[j].gfxlayout->charincrement-1);
/*TODO*///						len = drivers[i]->drv->gfxdecodeinfo[j].gfxlayout->total *
/*TODO*///								drivers[i]->drv->gfxdecodeinfo[j].gfxlayout->charincrement;
/*TODO*///						avail = region_length[type]
/*TODO*///								- (drivers[i]->drv->gfxdecodeinfo[j].start & ~(drivers[i]->drv->gfxdecodeinfo[j].gfxlayout->charincrement/8-1));
/*TODO*///						if ((start + len) / 8 > avail)
/*TODO*///						{
/*TODO*///							printf("%s has gfx[%d] extending past allocated memory\n",drivers[i]->name,j);
/*TODO*///							error = 1;
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///
/*TODO*///		inp = drivers[i]->input_ports;
/*TODO*///
/*TODO*///		if (inp)
/*TODO*///		{
/*TODO*///			while (inp->type != IPT_END)
/*TODO*///			{
/*TODO*///				if (inp->name && inp->name != IP_NAME_DEFAULT)
/*TODO*///				{
/*TODO*///					j = 0;
/*TODO*///
/*TODO*///					while (ipdn_defaultstrings[j] != DEF_STR( Unknown ))
/*TODO*///					{
/*TODO*///						if (inp->name == ipdn_defaultstrings[j]) break;
/*TODO*///						else if (!my_stricmp(inp->name,ipdn_defaultstrings[j]))
/*TODO*///						{
/*TODO*///							printf("%s must use DEF_STR( %s )\n",drivers[i]->name,inp->name);
/*TODO*///							error = 1;
/*TODO*///						}
/*TODO*///						j++;
/*TODO*///					}
/*TODO*///
/*TODO*///					if (inp->name == DEF_STR( On ) && (inp+1)->name == DEF_STR( Off ))
/*TODO*///					{
/*TODO*///						printf("%s has inverted Off/On dipswitch order\n",drivers[i]->name);
/*TODO*///						error = 1;
/*TODO*///					}
/*TODO*///
/*TODO*///					if (inp->name == DEF_STR( Yes ) && (inp+1)->name == DEF_STR( No ))
/*TODO*///					{
/*TODO*///						printf("%s has inverted No/Yes dipswitch order\n",drivers[i]->name);
/*TODO*///						error = 1;
/*TODO*///					}
/*TODO*///
/*TODO*///					if (!my_stricmp(inp->name,"table"))
/*TODO*///					{
/*TODO*///						printf("%s must use DEF_STR( Cocktail ), not %s\n",drivers[i]->name,inp->name);
/*TODO*///						error = 1;
/*TODO*///					}
/*TODO*///
/*TODO*///					if (inp->name == DEF_STR( Cocktail ) && (inp+1)->name == DEF_STR( Upright ))
/*TODO*///					{
/*TODO*///						printf("%s has inverted Upright/Cocktail dipswitch order\n",drivers[i]->name);
/*TODO*///						error = 1;
/*TODO*///					}
/*TODO*///
/*TODO*///					if (inp->name >= DEF_STR( 9C_1C ) && inp->name <= DEF_STR( Free_Play )
/*TODO*///							&& (inp+1)->name >= DEF_STR( 9C_1C ) && (inp+1)->name <= DEF_STR( Free_Play )
/*TODO*///							&& inp->name >= (inp+1)->name)
/*TODO*///					{
/*TODO*///						printf("%s has unsorted coinage %s > %s\n",drivers[i]->name,inp->name,(inp+1)->name);
/*TODO*///						error = 1;
/*TODO*///					}
/*TODO*///
/*TODO*///					if (inp->name == DEF_STR( Flip_Screen ) && (inp+1)->name != DEF_STR( Off ))
/*TODO*///					{
/*TODO*///						printf("%s has wrong Flip Screen option %s\n",drivers[i]->name,(inp+1)->name);
/*TODO*///						error = 1;
/*TODO*///					}
/*TODO*///
/*TODO*///				}
/*TODO*///
/*TODO*///				inp++;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	return error;
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///int run_game(int game)
/*TODO*///{
/*TODO*///	int err;
/*TODO*///
/*TODO*///
/*TODO*///#ifdef MAME_DEBUG
/*TODO*///	/* validity checks */
/*TODO*///	if (validitychecks()) return 1;
/*TODO*///#endif
/*TODO*///
/*TODO*///
/*TODO*///	/* copy some settings into easier-to-handle variables */
/*TODO*///	errorlog   = options.errorlog;
/*TODO*///	record     = options.record;
/*TODO*///	playback   = options.playback;
/*TODO*///	mame_debug = options.mame_debug;
/*TODO*///
/*TODO*///	Machine->gamedrv = gamedrv = drivers[game];
/*TODO*///	Machine->drv = drv = gamedrv->drv;
/*TODO*///
/*TODO*///	/* copy configuration */
/*TODO*///	if (options.color_depth == 16 ||
/*TODO*///			(options.color_depth != 8 && (Machine->gamedrv->flags & GAME_REQUIRES_16BIT)))
/*TODO*///		Machine->color_depth = 16;
/*TODO*///	else
/*TODO*///		Machine->color_depth = 8;
/*TODO*///	Machine->sample_rate = options.samplerate;
/*TODO*///
/*TODO*///	/* get orientation right */
/*TODO*///	Machine->orientation = gamedrv->flags & ORIENTATION_MASK;
/*TODO*///	Machine->ui_orientation = ROT0;
/*TODO*///	if (options.norotate)
/*TODO*///		Machine->orientation = ROT0;
/*TODO*///	if (options.ror)
/*TODO*///	{
/*TODO*///		/* if only one of the components is inverted, switch them */
/*TODO*///		if ((Machine->orientation & ROT180) == ORIENTATION_FLIP_X ||
/*TODO*///				(Machine->orientation & ROT180) == ORIENTATION_FLIP_Y)
/*TODO*///			Machine->orientation ^= ROT180;
/*TODO*///
/*TODO*///		Machine->orientation ^= ROT90;
/*TODO*///
/*TODO*///		/* if only one of the components is inverted, switch them */
/*TODO*///		if ((Machine->ui_orientation & ROT180) == ORIENTATION_FLIP_X ||
/*TODO*///				(Machine->ui_orientation & ROT180) == ORIENTATION_FLIP_Y)
/*TODO*///			Machine->ui_orientation ^= ROT180;
/*TODO*///
/*TODO*///		Machine->ui_orientation ^= ROT90;
/*TODO*///	}
/*TODO*///	if (options.rol)
/*TODO*///	{
/*TODO*///		/* if only one of the components is inverted, switch them */
/*TODO*///		if ((Machine->orientation & ROT180) == ORIENTATION_FLIP_X ||
/*TODO*///				(Machine->orientation & ROT180) == ORIENTATION_FLIP_Y)
/*TODO*///			Machine->orientation ^= ROT180;
/*TODO*///
/*TODO*///		Machine->orientation ^= ROT270;
/*TODO*///
/*TODO*///		/* if only one of the components is inverted, switch them */
/*TODO*///		if ((Machine->ui_orientation & ROT180) == ORIENTATION_FLIP_X ||
/*TODO*///				(Machine->ui_orientation & ROT180) == ORIENTATION_FLIP_Y)
/*TODO*///			Machine->ui_orientation ^= ROT180;
/*TODO*///
/*TODO*///		Machine->ui_orientation ^= ROT270;
/*TODO*///	}
/*TODO*///	if (options.flipx)
/*TODO*///	{
/*TODO*///		Machine->orientation ^= ORIENTATION_FLIP_X;
/*TODO*///		Machine->ui_orientation ^= ORIENTATION_FLIP_X;
/*TODO*///	}
/*TODO*///	if (options.flipy)
/*TODO*///	{
/*TODO*///		Machine->orientation ^= ORIENTATION_FLIP_Y;
/*TODO*///		Machine->ui_orientation ^= ORIENTATION_FLIP_Y;
/*TODO*///	}
/*TODO*///
/*TODO*///	set_pixel_functions();
/*TODO*///
/*TODO*///	/* Do the work*/
/*TODO*///	err = 1;
/*TODO*///	bailing = 0;
/*TODO*///
/*TODO*///	#ifdef MESS
/*TODO*///	if (get_filenames())
/*TODO*///		return err;
/*TODO*///	#endif
/*TODO*///
/*TODO*///	if (osd_init() == 0)
/*TODO*///	{
/*TODO*///		if (init_machine() == 0)
/*TODO*///		{
/*TODO*///			if (run_machine() == 0)
/*TODO*///				err = 0;
/*TODO*///			else if (!bailing)
/*TODO*///			{
/*TODO*///				bailing = 1;
/*TODO*///				printf("Unable to start machine emulation\n");
/*TODO*///			}
/*TODO*///
/*TODO*///			shutdown_machine();
/*TODO*///		}
/*TODO*///		else if (!bailing)
/*TODO*///		{
/*TODO*///			bailing = 1;
/*TODO*///			printf("Unable to initialize machine emulation\n");
/*TODO*///		}
/*TODO*///
/*TODO*///		osd_exit();
/*TODO*///	}
/*TODO*///	else if (!bailing)
/*TODO*///	{
/*TODO*///		bailing = 1;
/*TODO*///		printf ("Unable to initialize system\n");
/*TODO*///	}
/*TODO*///
/*TODO*///	return err;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Initialize the emulated machine (load the roms, initialize the various
/*TODO*///  subsystems...). Returns 0 if successful.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///int init_machine(void)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///	if (code_init() != 0)
/*TODO*///		goto out;
/*TODO*///
/*TODO*///	for (i = 0;i < MAX_MEMORY_REGIONS;i++)
/*TODO*///	{
/*TODO*///		Machine->memory_region[i] = 0;
/*TODO*///		Machine->memory_region_length[i] = 0;
/*TODO*///		Machine->memory_region_type[i] = 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (gamedrv->input_ports)
/*TODO*///	{
/*TODO*///		Machine->input_ports = input_port_allocate(gamedrv->input_ports);
/*TODO*///		if (!Machine->input_ports)
/*TODO*///			goto out_code;
/*TODO*///		Machine->input_ports_default = input_port_allocate(gamedrv->input_ports);
/*TODO*///		if (!Machine->input_ports_default)
/*TODO*///		{
/*TODO*///			input_port_free(Machine->input_ports);
/*TODO*///			Machine->input_ports = 0;
/*TODO*///			goto out_code;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///    #ifdef MESS
/*TODO*///	if (!gamedrv->rom)
/*TODO*///	{
/*TODO*///		if(errorlog) fprintf(errorlog, "Going to load_next tag\n");
/*TODO*///		goto load_next;
/*TODO*///	}
/*TODO*///    #endif
/*TODO*///
/*TODO*///	if (readroms() != 0)
/*TODO*///		goto out_free;
/*TODO*///
/*TODO*///	#ifdef MESS
/*TODO*///	load_next:
/*TODO*///		if (init_devices(gamedrv))
/*TODO*///			goto out_free;
/*TODO*///	#endif
/*TODO*///
/*TODO*///	/* Mish:  Multi-session safety - set spriteram size to zero before memory map is set up */
/*TODO*///	spriteram_size=spriteram_2_size=0;
/*TODO*///
/*TODO*///	/* first of all initialize the memory handlers, which could be used by the */
/*TODO*///	/* other initialization routines */
/*TODO*///	cpu_init();
/*TODO*///
/*TODO*///	/* load input ports settings (keys, dip switches, and so on) */
/*TODO*///	settingsloaded = load_input_port_settings();
/*TODO*///
/*TODO*///	if( !memory_init() )
/*TODO*///		goto out_free;
/*TODO*///
/*TODO*///	if (gamedrv->driver_init) (*gamedrv->driver_init)();
/*TODO*///
/*TODO*///	return 0;
/*TODO*///
/*TODO*///out_free:
/*TODO*///	input_port_free(Machine->input_ports);
/*TODO*///	Machine->input_ports = 0;
/*TODO*///	input_port_free(Machine->input_ports_default);
/*TODO*///	Machine->input_ports_default = 0;
/*TODO*///out_code:
/*TODO*///	code_close();
/*TODO*///out:
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///void shutdown_machine(void)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///
/*TODO*///	#ifdef MESS
/*TODO*///	exit_devices();
/*TODO*///	#endif
/*TODO*///
/*TODO*///    /* ASG 971007 free memory element map */
/*TODO*///	memory_shutdown();
/*TODO*///
/*TODO*///	/* free the memory allocated for ROM and RAM */
/*TODO*///	for (i = 0;i < MAX_MEMORY_REGIONS;i++)
/*TODO*///	{
/*TODO*///		free(Machine->memory_region[i]);
/*TODO*///		Machine->memory_region[i] = 0;
/*TODO*///		Machine->memory_region_length[i] = 0;
/*TODO*///		Machine->memory_region_type[i] = 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* free the memory allocated for input ports definition */
/*TODO*///	input_port_free(Machine->input_ports);
/*TODO*///	Machine->input_ports = 0;
/*TODO*///	input_port_free(Machine->input_ports_default);
/*TODO*///	Machine->input_ports_default = 0;
/*TODO*///
/*TODO*///	code_close();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///static void vh_close(void)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///
/*TODO*///	for (i = 0;i < MAX_GFX_ELEMENTS;i++)
/*TODO*///	{
/*TODO*///		freegfx(Machine->gfx[i]);
/*TODO*///		Machine->gfx[i] = 0;
/*TODO*///	}
/*TODO*///	freegfx(Machine->uifont);
/*TODO*///	Machine->uifont = 0;
/*TODO*///	osd_close_display();
/*TODO*///	palette_stop();
/*TODO*///
/*TODO*///	if (drv->video_attributes & VIDEO_BUFFERS_SPRITERAM) {
/*TODO*///		if (buffered_spriteram) free(buffered_spriteram);
/*TODO*///		if (buffered_spriteram_2) free(buffered_spriteram_2);
/*TODO*///		buffered_spriteram=NULL;
/*TODO*///		buffered_spriteram_2=NULL;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///static int vh_open(void)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///
/*TODO*///	for (i = 0;i < MAX_GFX_ELEMENTS;i++) Machine->gfx[i] = 0;
/*TODO*///	Machine->uifont = 0;
/*TODO*///
/*TODO*///	if (palette_start() != 0)
/*TODO*///	{
/*TODO*///		vh_close();
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	/* convert the gfx ROMs into character sets. This is done BEFORE calling the driver's */
/*TODO*///	/* convert_color_prom() routine (in palette_init()) because it might need to check the */
/*TODO*///	/* Machine->gfx[] data */
/*TODO*///	if (drv->gfxdecodeinfo)
/*TODO*///	{
/*TODO*///		for (i = 0;i < MAX_GFX_ELEMENTS && drv->gfxdecodeinfo[i].memory_region != -1;i++)
/*TODO*///		{
/*TODO*///			int reglen = 8*memory_region_length(drv->gfxdecodeinfo[i].memory_region);
/*TODO*///			struct GfxLayout glcopy;
/*TODO*///			int j;
/*TODO*///
/*TODO*///
/*TODO*///			memcpy(&glcopy,drv->gfxdecodeinfo[i].gfxlayout,sizeof(glcopy));
/*TODO*///
/*TODO*///			if (IS_FRAC(glcopy.total))
/*TODO*///				glcopy.total = reglen / glcopy.charincrement * FRAC_NUM(glcopy.total) / FRAC_DEN(glcopy.total);
/*TODO*///			for (j = 0;j < MAX_GFX_PLANES;j++)
/*TODO*///			{
/*TODO*///				if (IS_FRAC(glcopy.planeoffset[j]))
/*TODO*///				{
/*TODO*///					glcopy.planeoffset[j] = FRAC_OFFSET(glcopy.planeoffset[j]) +
/*TODO*///							reglen * FRAC_NUM(glcopy.planeoffset[j]) / FRAC_DEN(glcopy.planeoffset[j]);
/*TODO*///				}
/*TODO*///			}
/*TODO*///			for (j = 0;j < MAX_GFX_SIZE;j++)
/*TODO*///			{
/*TODO*///				if (IS_FRAC(glcopy.xoffset[j]))
/*TODO*///				{
/*TODO*///					glcopy.xoffset[j] = FRAC_OFFSET(glcopy.xoffset[j]) +
/*TODO*///							reglen * FRAC_NUM(glcopy.xoffset[j]) / FRAC_DEN(glcopy.xoffset[j]);
/*TODO*///				}
/*TODO*///				if (IS_FRAC(glcopy.yoffset[j]))
/*TODO*///				{
/*TODO*///					glcopy.yoffset[j] = FRAC_OFFSET(glcopy.yoffset[j]) +
/*TODO*///							reglen * FRAC_NUM(glcopy.yoffset[j]) / FRAC_DEN(glcopy.yoffset[j]);
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			if ((Machine->gfx[i] = decodegfx(memory_region(drv->gfxdecodeinfo[i].memory_region)
/*TODO*///					+ drv->gfxdecodeinfo[i].start,
/*TODO*///					&glcopy)) == 0)
/*TODO*///			{
/*TODO*///				vh_close();
/*TODO*///				return 1;
/*TODO*///			}
/*TODO*///			if (Machine->remapped_colortable)
/*TODO*///				Machine->gfx[i]->colortable = &Machine->remapped_colortable[drv->gfxdecodeinfo[i].color_codes_start];
/*TODO*///			Machine->gfx[i]->total_colors = drv->gfxdecodeinfo[i].total_color_codes;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	/* create the display bitmap, and allocate the palette */
/*TODO*///	if ((Machine->scrbitmap = osd_create_display(
/*TODO*///			drv->screen_width,drv->screen_height,
/*TODO*///			Machine->color_depth,
/*TODO*///			drv->video_attributes)) == 0)
/*TODO*///	{
/*TODO*///		vh_close();
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* create spriteram buffers if necessary */
/*TODO*///	if (drv->video_attributes & VIDEO_BUFFERS_SPRITERAM) {
/*TODO*///		if (spriteram_size!=0) {
/*TODO*///			buffered_spriteram= malloc(spriteram_size);
/*TODO*///			if (!buffered_spriteram) { vh_close(); return 1; }
/*TODO*///			if (spriteram_2_size!=0) buffered_spriteram_2 = malloc(spriteram_2_size);
/*TODO*///			if (spriteram_2_size && !buffered_spriteram_2) { vh_close(); return 1; }
/*TODO*///		} else {
/*TODO*///			if (errorlog) fprintf(errorlog,"vh_open():  Video buffers spriteram but spriteram_size is 0\n");
/*TODO*///			buffered_spriteram=NULL;
/*TODO*///			buffered_spriteram_2=NULL;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* build our private user interface font */
/*TODO*///	/* This must be done AFTER osd_create_display() so the function knows the */
/*TODO*///	/* resolution we are running at and can pick a different font depending on it. */
/*TODO*///	/* It must be done BEFORE palette_init() because that will also initialize */
/*TODO*///	/* (through osd_allocate_colors()) the uifont colortable. */
/*TODO*///	if ((Machine->uifont = builduifont()) == 0)
/*TODO*///	{
/*TODO*///		vh_close();
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* initialize the palette - must be done after osd_create_display() */
/*TODO*///	if (palette_init())
/*TODO*///	{
/*TODO*///		vh_close();
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  This function takes care of refreshing the screen, processing user input,
/*TODO*///  and throttling the emulation speed to obtain the required frames per second.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*///int need_to_clear_bitmap;	/* set by the user interface */
/*TODO*///
/*TODO*///int updatescreen(void)
/*TODO*///{
/*TODO*///	/* update sound */
/*TODO*///	sound_update();
/*TODO*///
/*TODO*///	if (osd_skip_this_frame() == 0)
/*TODO*///	{
/*TODO*///		profiler_mark(PROFILER_VIDEO);
/*TODO*///		if (need_to_clear_bitmap)
/*TODO*///		{
/*TODO*///			osd_clearbitmap(Machine->scrbitmap);
/*TODO*///			need_to_clear_bitmap = 0;
/*TODO*///		}
/*TODO*///		(*drv->vh_update)(Machine->scrbitmap,bitmap_dirty);  /* update screen */
/*TODO*///		bitmap_dirty = 0;
/*TODO*///		profiler_mark(PROFILER_END);
/*TODO*///	}
/*TODO*///
/*TODO*///	/* the user interface must be called between vh_update() and osd_update_video_and_audio(), */
/*TODO*///	/* to allow it to overlay things on the game display. We must call it even */
/*TODO*///	/* if the frame is skipped, to keep a consistent timing. */
/*TODO*///	if (handle_user_interface())
/*TODO*///		/* quit if the user asked to */
/*TODO*///		return 1;
/*TODO*///
/*TODO*///	osd_update_video_and_audio();
/*TODO*///
/*TODO*///	if (drv->vh_eof_callback) (*drv->vh_eof_callback)();
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Run the emulation. Start the various subsystems and the CPU emulation.
/*TODO*///  Returns non zero in case of error.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///int run_machine(void)
/*TODO*///{
/*TODO*///	int res = 1;
/*TODO*///
/*TODO*///
/*TODO*///	if (vh_open() == 0)
/*TODO*///	{
/*TODO*///		tilemap_init();
/*TODO*///		sprite_init();
/*TODO*///		gfxobj_init();
/*TODO*///		if (drv->vh_start == 0 || (*drv->vh_start)() == 0)      /* start the video hardware */
/*TODO*///		{
/*TODO*///			if (sound_start() == 0) /* start the audio hardware */
/*TODO*///			{
/*TODO*///				int	region;
/*TODO*///
/*TODO*///				/* free memory regions allocated with REGIONFLAG_DISPOSE (typically gfx roms) */
/*TODO*///				for (region = 0; region < MAX_MEMORY_REGIONS; region++)
/*TODO*///				{
/*TODO*///					if (Machine->memory_region_type[region] & REGIONFLAG_DISPOSE)
/*TODO*///					{
/*TODO*///						int i;
/*TODO*///
/*TODO*///						/* invalidate contents to avoid subtle bugs */
/*TODO*///						for (i = 0;i < memory_region_length(region);i++)
/*TODO*///							memory_region(region)[i] = rand();
/*TODO*///						free(Machine->memory_region[region]);
/*TODO*///						Machine->memory_region[region] = 0;
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				if (settingsloaded == 0)
/*TODO*///				{
/*TODO*///					/* if there is no saved config, it must be first time we run this game, */
/*TODO*///					/* so show the disclaimer. */
/*TODO*///					if (showcopyright()) goto userquit;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (showgamewarnings() == 0)  /* show info about incorrect behaviour (wrong colors etc.) */
/*TODO*///				{
/*TODO*///					/* shut down the leds (work around Allegro hanging bug in the DOS port) */
/*TODO*///					osd_led_w(0,1);
/*TODO*///					osd_led_w(1,1);
/*TODO*///					osd_led_w(2,1);
/*TODO*///					osd_led_w(3,1);
/*TODO*///					osd_led_w(0,0);
/*TODO*///					osd_led_w(1,0);
/*TODO*///					osd_led_w(2,0);
/*TODO*///					osd_led_w(3,0);
/*TODO*///
/*TODO*///					init_user_interface();
/*TODO*///
/*TODO*///					/* disable cheat if no roms */
/*TODO*///					if (!gamedrv->rom) options.cheat = 0;
/*TODO*///
/*TODO*///					if (options.cheat) InitCheat();
/*TODO*///
/*TODO*///					if (drv->nvram_handler)
/*TODO*///					{
/*TODO*///						void *f;
/*TODO*///
/*TODO*///						f = osd_fopen(Machine->gamedrv->name,0,OSD_FILETYPE_NVRAM,0);
/*TODO*///						(*drv->nvram_handler)(f,0);
/*TODO*///						if (f) osd_fclose(f);
/*TODO*///					}
/*TODO*///
/*TODO*///					cpu_run();      /* run the emulation! */
/*TODO*///
/*TODO*///					if (drv->nvram_handler)
/*TODO*///					{
/*TODO*///						void *f;
/*TODO*///
/*TODO*///						if ((f = osd_fopen(Machine->gamedrv->name,0,OSD_FILETYPE_NVRAM,1)) != 0)
/*TODO*///						{
/*TODO*///							(*drv->nvram_handler)(f,1);
/*TODO*///							osd_fclose(f);
/*TODO*///						}
/*TODO*///					}
/*TODO*///
/*TODO*///					if (options.cheat) StopCheat();
/*TODO*///
/*TODO*///					/* save input ports settings */
/*TODO*///					save_input_port_settings();
/*TODO*///				}
/*TODO*///
/*TODO*///userquit:
/*TODO*///				/* the following MUST be done after hiscore_save() otherwise */
/*TODO*///				/* some 68000 games will not work */
/*TODO*///				sound_stop();
/*TODO*///				if (drv->vh_stop) (*drv->vh_stop)();
/*TODO*///
/*TODO*///				res = 0;
/*TODO*///			}
/*TODO*///			else if (!bailing)
/*TODO*///			{
/*TODO*///				bailing = 1;
/*TODO*///				printf("Unable to start audio emulation\n");
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else if (!bailing)
/*TODO*///		{
/*TODO*///			bailing = 1;
/*TODO*///			printf("Unable to start video emulation\n");
/*TODO*///		}
/*TODO*///
/*TODO*///		gfxobj_close();
/*TODO*///		sprite_close();
/*TODO*///		tilemap_close();
/*TODO*///		vh_close();
/*TODO*///	}
/*TODO*///	else if (!bailing)
/*TODO*///	{
/*TODO*///		bailing = 1;
/*TODO*///		printf("Unable to initialize display\n");
/*TODO*///	}
/*TODO*///
/*TODO*///	return res;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
    public static int mame_highscore_enabled() {
        /* disable high score when record/playback is on */
        if (record != null || playback != null) {
            return 0;
        }

        /* disable high score when cheats are used */
        if (he_did_cheat != 0) {
            return 0;
        }

        return 1;
    }

}
