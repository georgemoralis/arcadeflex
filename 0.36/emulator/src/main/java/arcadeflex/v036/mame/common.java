/*
 * ported to v0.36
 */
package arcadeflex.v036.mame;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.commonH.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.common.coinlockedout;
import static gr.codebb.arcadeflex.v036.mame.common.coins;
import static gr.codebb.arcadeflex.v036.mame.common.lastcoin;

public class common {

    /*TODO*////*********************************************************************
/*TODO*///
/*TODO*///  common.c
/*TODO*///
/*TODO*///  Generic functions, mostly ROM and graphics related.
/*TODO*///
/*TODO*///*********************************************************************/
/*TODO*///
/*TODO*///#include "driver.h"
/*TODO*///#include "png.h"
/*TODO*///
/*TODO*////* These globals are only kept on a machine basis - LBO 042898 */
/*TODO*///unsigned int dispensed_tickets;
/*TODO*///unsigned int coins[COIN_COUNTERS];
/*TODO*///unsigned int lastcoin[COIN_COUNTERS];
/*TODO*///unsigned int coinlockedout[COIN_COUNTERS];
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///void showdisclaimer(void)   /* MAURY_BEGIN: dichiarazione */
/*TODO*///{
/*TODO*///	printf("MAME is an emulator: it reproduces, more or less faithfully, the behaviour of\n"
/*TODO*///		 "several arcade machines. But hardware is useless without software, so an image\n"
/*TODO*///		 "of the ROMs which run on that hardware is required. Such ROMs, like any other\n"
/*TODO*///		 "commercial software, are copyrighted material and it is therefore illegal to\n"
/*TODO*///		 "use them if you don't own the original arcade machine. Needless to say, ROMs\n"
/*TODO*///		 "are not distributed together with MAME. Distribution of MAME together with ROM\n"
/*TODO*///		 "images is a violation of copyright law and should be promptly reported to the\n"
/*TODO*///		 "authors so that appropriate legal action can be taken.\n\n");
/*TODO*///}                           /* MAURY_END: dichiarazione */
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Read ROMs into memory.
/*TODO*///
/*TODO*///  Arguments:
/*TODO*///  const struct RomModule *romp - pointer to an array of Rommodule structures,
/*TODO*///                                 as defined in common.h.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*///int readroms(void)
/*TODO*///{
/*TODO*///	int region;
/*TODO*///	const struct RomModule *romp;
/*TODO*///	int warning = 0;
/*TODO*///	int fatalerror = 0;
/*TODO*///	int total_roms,current_rom;
/*TODO*///	char buf[4096] = "";
/*TODO*///
/*TODO*///
/*TODO*///	total_roms = current_rom = 0;
/*TODO*///	romp = Machine->gamedrv->rom;
/*TODO*///
/*TODO*///	if (!romp) return 0;
/*TODO*///
/*TODO*///	while (romp->name || romp->offset || romp->length)
/*TODO*///	{
/*TODO*///		if (romp->name && romp->name != (char *)-1)
/*TODO*///			total_roms++;
/*TODO*///
/*TODO*///		romp++;
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	romp = Machine->gamedrv->rom;
/*TODO*///
/*TODO*///	for (region = 0;region < MAX_MEMORY_REGIONS;region++)
/*TODO*///		Machine->memory_region[region] = 0;
/*TODO*///
/*TODO*///	region = 0;
/*TODO*///
/*TODO*///	while (romp->name || romp->offset || romp->length)
/*TODO*///	{
/*TODO*///		unsigned int region_size;
/*TODO*///		const char *name;
/*TODO*///
/*TODO*///		/* Mish:  An 'optional' rom region, only loaded if sound emulation is turned on */
/*TODO*///		if (Machine->sample_rate==0 && (romp->crc & REGIONFLAG_SOUNDONLY)) {
/*TODO*///			if (errorlog) fprintf(errorlog,"readroms():  Ignoring rom region %d\n",region);
/*TODO*///			Machine->memory_region_type[region] = romp->crc;
/*TODO*///			region++;
/*TODO*///
/*TODO*///			romp++;
/*TODO*///			while (romp->name || romp->length)
/*TODO*///				romp++;
/*TODO*///
/*TODO*///			continue;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (romp->name || romp->length)
/*TODO*///		{
/*TODO*///			printf("Error in RomModule definition: expecting ROM_REGION\n");
/*TODO*///			goto getout;
/*TODO*///		}
/*TODO*///
/*TODO*///		region_size = romp->offset;
/*TODO*///		if ((Machine->memory_region[region] = malloc(region_size)) == 0)
/*TODO*///		{
/*TODO*///			printf("readroms():  Unable to allocate %d bytes of RAM\n",region_size);
/*TODO*///			goto getout;
/*TODO*///		}
/*TODO*///		Machine->memory_region_length[region] = region_size;
/*TODO*///		Machine->memory_region_type[region] = romp->crc;
/*TODO*///
/*TODO*///		/* some games (i.e. Pleiades) want the memory clear on startup */
/*TODO*///		if (region_size <= 0x400000)	/* don't clear large regions which will be filled anyway */
/*TODO*///			memset(Machine->memory_region[region],0,region_size);
/*TODO*///
/*TODO*///		romp++;
/*TODO*///
/*TODO*///		while (romp->length)
/*TODO*///		{
/*TODO*///			void *f;
/*TODO*///			int expchecksum = romp->crc;
/*TODO*///			int	explength = 0;
/*TODO*///
/*TODO*///
/*TODO*///			if (romp->name == 0)
/*TODO*///			{
/*TODO*///				printf("Error in RomModule definition: ROM_CONTINUE not preceded by ROM_LOAD\n");
/*TODO*///				goto getout;
/*TODO*///			}
/*TODO*///			else if (romp->name == (char *)-1)
/*TODO*///			{
/*TODO*///				printf("Error in RomModule definition: ROM_RELOAD not preceded by ROM_LOAD\n");
/*TODO*///				goto getout;
/*TODO*///			}
/*TODO*///
/*TODO*///			name = romp->name;
/*TODO*///
/*TODO*///			/* update status display */
/*TODO*///			if (osd_display_loading_rom_message(name,++current_rom,total_roms) != 0)
/*TODO*///               goto getout;
/*TODO*///
/*TODO*///			{
/*TODO*///				const struct GameDriver *drv;
/*TODO*///
/*TODO*///				drv = Machine->gamedrv;
/*TODO*///				do
/*TODO*///				{
/*TODO*///					f = osd_fopen(drv->name,name,OSD_FILETYPE_ROM,0);
/*TODO*///					drv = drv->clone_of;
/*TODO*///				} while (f == 0 && drv);
/*TODO*///
/*TODO*///				if (f == 0)
/*TODO*///				{
/*TODO*///					/* NS981003: support for "load by CRC" */
/*TODO*///					char crc[9];
/*TODO*///
/*TODO*///					sprintf(crc,"%08x",romp->crc);
/*TODO*///					drv = Machine->gamedrv;
/*TODO*///					do
/*TODO*///					{
/*TODO*///						f = osd_fopen(drv->name,crc,OSD_FILETYPE_ROM,0);
/*TODO*///						drv = drv->clone_of;
/*TODO*///					} while (f == 0 && drv);
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			if (f)
/*TODO*///			{
/*TODO*///				do
/*TODO*///				{
/*TODO*///					unsigned char *c;
/*TODO*///					unsigned int i;
/*TODO*///					int length = romp->length & ~ROMFLAG_MASK;
/*TODO*///
/*TODO*///
/*TODO*///					if (romp->name == (char *)-1)
/*TODO*///						osd_fseek(f,0,SEEK_SET);	/* ROM_RELOAD */
/*TODO*///					else
/*TODO*///						explength += length;
/*TODO*///
/*TODO*///					if (romp->offset + length > region_size ||
/*TODO*///						(!(romp->length & ROMFLAG_NIBBLE) && (romp->length & ROMFLAG_ALTERNATE)
/*TODO*///								&& (romp->offset&~1) + 2*length > region_size))
/*TODO*///					{
/*TODO*///						printf("Error in RomModule definition: %s out of memory region space\n",name);
/*TODO*///						osd_fclose(f);
/*TODO*///						goto getout;
/*TODO*///					}
/*TODO*///
/*TODO*///					if (romp->length & ROMFLAG_NIBBLE)
/*TODO*///					{
/*TODO*///						unsigned char *temp;
/*TODO*///
/*TODO*///
/*TODO*///						temp = malloc(length);
/*TODO*///
/*TODO*///						if (!temp)
/*TODO*///						{
/*TODO*///							printf("Out of memory reading ROM %s\n",name);
/*TODO*///							osd_fclose(f);
/*TODO*///							goto getout;
/*TODO*///						}
/*TODO*///
/*TODO*///						if (osd_fread(f,temp,length) != length)
/*TODO*///						{
/*TODO*///							printf("Unable to read ROM %s\n",name);
/*TODO*///						}
/*TODO*///
/*TODO*///						/* ROM_LOAD_NIB_LOW and ROM_LOAD_NIB_HIGH */
/*TODO*///						c = Machine->memory_region[region] + romp->offset;
/*TODO*///						if (romp->length & ROMFLAG_ALTERNATE)
/*TODO*///						{
/*TODO*///							/* Load into the high nibble */
/*TODO*///							for (i = 0;i < length;i ++)
/*TODO*///							{
/*TODO*///								c[i] = (c[i] & 0x0f) | ((temp[i] & 0x0f) << 4);
/*TODO*///							}
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							/* Load into the low nibble */
/*TODO*///							for (i = 0;i < length;i ++)
/*TODO*///							{
/*TODO*///								c[i] = (c[i] & 0xf0) | (temp[i] & 0x0f);
/*TODO*///							}
/*TODO*///						}
/*TODO*///
/*TODO*///						free (temp);
/*TODO*///					}
/*TODO*///					else if (romp->length & ROMFLAG_ALTERNATE)
/*TODO*///					{
/*TODO*///						/* ROM_LOAD_EVEN and ROM_LOAD_ODD */
/*TODO*///						/* copy the ROM data */
/*TODO*///					#ifdef LSB_FIRST
/*TODO*///						c = Machine->memory_region[region] + (romp->offset ^ 1);
/*TODO*///					#else
/*TODO*///						c = Machine->memory_region[region] + romp->offset;
/*TODO*///					#endif
/*TODO*///
/*TODO*///						if (osd_fread_scatter(f,c,length,2) != length)
/*TODO*///						{
/*TODO*///							printf("Unable to read ROM %s\n",name);
/*TODO*///						}
/*TODO*///					}
/*TODO*///					else if (romp->length & ROMFLAG_QUAD) {
/*TODO*///						static int which_quad=0; /* This is multi session friendly, as we only care about the modulus */
/*TODO*///						unsigned char *temp;
/*TODO*///						int base=0;
/*TODO*///
/*TODO*///						temp = malloc(length);	/* Need to load rom to temporary space */
/*TODO*///						osd_fread(f,temp,length);
/*TODO*///
/*TODO*///						/* Copy quad to region */
/*TODO*///						c = Machine->memory_region[region] + romp->offset;
/*TODO*///
/*TODO*///					#ifdef LSB_FIRST
/*TODO*///						switch (which_quad%4) {
/*TODO*///							case 0: base=1; break;
/*TODO*///							case 1: base=0; break;
/*TODO*///							case 2: base=3; break;
/*TODO*///							case 3: base=2; break;
/*TODO*///						}
/*TODO*///					#else
/*TODO*///						switch (which_quad%4) {
/*TODO*///							case 0: base=0; break;
/*TODO*///							case 1: base=1; break;
/*TODO*///							case 2: base=2; break;
/*TODO*///							case 3: base=3; break;
/*TODO*///						}
/*TODO*///					#endif
/*TODO*///
/*TODO*///						for (i=base; i< length*4; i += 4)
/*TODO*///							c[i]=temp[i/4];
/*TODO*///
/*TODO*///						which_quad++;
/*TODO*///						free(temp);
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						int wide = romp->length & ROMFLAG_WIDE;
/*TODO*///					#ifdef LSB_FIRST
/*TODO*///						int swap = (romp->length & ROMFLAG_SWAP) ^ ROMFLAG_SWAP;
/*TODO*///					#else
/*TODO*///						int swap = romp->length & ROMFLAG_SWAP;
/*TODO*///					#endif
/*TODO*///
/*TODO*///						osd_fread(f,Machine->memory_region[region] + romp->offset,length);
/*TODO*///
/*TODO*///						/* apply swappage */
/*TODO*///						c = Machine->memory_region[region] + romp->offset;
/*TODO*///						if (wide && swap)
/*TODO*///						{
/*TODO*///							for (i = 0; i < length; i += 2)
/*TODO*///							{
/*TODO*///								int temp = c[i];
/*TODO*///								c[i] = c[i+1];
/*TODO*///								c[i+1] = temp;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///
/*TODO*///					romp++;
/*TODO*///				} while (romp->length && (romp->name == 0 || romp->name == (char *)-1));
/*TODO*///
/*TODO*///				if (explength != osd_fsize (f))
/*TODO*///				{
/*TODO*///					sprintf (&buf[strlen(buf)], "%-12s WRONG LENGTH (expected: %08x found: %08x)\n",
/*TODO*///							name,explength,osd_fsize(f));
/*TODO*///					warning = 1;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (expchecksum != osd_fcrc (f))
/*TODO*///				{
/*TODO*///					warning = 1;
/*TODO*///					if (expchecksum == 0)
/*TODO*///						sprintf(&buf[strlen(buf)],"%-12s NO GOOD DUMP KNOWN\n",name);
/*TODO*///					else if (expchecksum == BADCRC(osd_fcrc(f)))
/*TODO*///						sprintf(&buf[strlen(buf)],"%-12s ROM NEEDS REDUMP\n",name);
/*TODO*///					else
/*TODO*///						sprintf(&buf[strlen(buf)], "%-12s WRONG CRC (expected: %08x found: %08x)\n",
/*TODO*///								name,expchecksum,osd_fcrc(f));
/*TODO*///				}
/*TODO*///
/*TODO*///				osd_fclose(f);
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				/* allow for a NO GOOD DUMP KNOWN rom to be missing */
/*TODO*///				if (expchecksum == 0)
/*TODO*///				{
/*TODO*///					sprintf (&buf[strlen(buf)], "%-12s NOT FOUND (NO GOOD DUMP KNOWN)\n",name);
/*TODO*///					warning = 1;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					sprintf (&buf[strlen(buf)], "%-12s NOT FOUND\n",name);
/*TODO*///					fatalerror = 1;
/*TODO*///				}
/*TODO*///
/*TODO*///				do
/*TODO*///				{
/*TODO*///					if (fatalerror == 0)
/*TODO*///					{
/*TODO*///						int i;
/*TODO*///
/*TODO*///						/* fill space with random data */
/*TODO*///						if (romp->length & ROMFLAG_ALTERNATE)
/*TODO*///						{
/*TODO*///							unsigned char *c;
/*TODO*///
/*TODO*///							/* ROM_LOAD_EVEN and ROM_LOAD_ODD */
/*TODO*///						#ifdef LSB_FIRST
/*TODO*///							c = Machine->memory_region[region] + (romp->offset ^ 1);
/*TODO*///						#else
/*TODO*///							c = Machine->memory_region[region] + romp->offset;
/*TODO*///						#endif
/*TODO*///
/*TODO*///							for (i = 0;i < (romp->length & ~ROMFLAG_MASK);i++)
/*TODO*///								c[2*i] = rand();
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							for (i = 0;i < (romp->length & ~ROMFLAG_MASK);i++)
/*TODO*///								Machine->memory_region[region][romp->offset + i] = rand();
/*TODO*///						}
/*TODO*///					}
/*TODO*///					romp++;
/*TODO*///				} while (romp->length && (romp->name == 0 || romp->name == (char *)-1));
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		region++;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* final status display */
/*TODO*///	osd_display_loading_rom_message(0,current_rom,total_roms);
/*TODO*///
/*TODO*///	if (warning || fatalerror)
/*TODO*///	{
/*TODO*///		extern int bailing;
/*TODO*///
/*TODO*///		if (fatalerror)
/*TODO*///		{
/*TODO*///			strcat (buf, "ERROR: required files are missing, the game cannot be run.\n");
/*TODO*///			bailing = 1;
/*TODO*///		}
/*TODO*///		else
/*TODO*///			strcat (buf, "WARNING: the game might not run correctly.\n");
/*TODO*///		printf ("%s", buf);
/*TODO*///
/*TODO*///		if (!options.gui_host && !bailing)
/*TODO*///		{
/*TODO*///			printf ("Press any key to continue\n");
/*TODO*///			keyboard_read_sync();
/*TODO*///			if (keyboard_pressed(KEYCODE_LCONTROL) && keyboard_pressed(KEYCODE_C))
/*TODO*///				return 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	if (fatalerror) return 1;
/*TODO*///	else return 0;
/*TODO*///
/*TODO*///
/*TODO*///getout:
/*TODO*///	/* final status display */
/*TODO*///	osd_display_loading_rom_message(0,current_rom,total_roms);
/*TODO*///
/*TODO*///	for (region = 0;region < MAX_MEMORY_REGIONS;region++)
/*TODO*///	{
/*TODO*///		free(Machine->memory_region[region]);
/*TODO*///		Machine->memory_region[region] = 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void printromlist(const struct RomModule *romp,const char *basename)
/*TODO*///{
/*TODO*///	if (!romp) return;
/*TODO*///
/*TODO*///#ifdef MESS
/*TODO*///	if (!strcmp(basename,"nes")) return;
/*TODO*///#endif
/*TODO*///
/*TODO*///	printf("This is the list of the ROMs required for driver \"%s\".\n"
/*TODO*///			"Name              Size       Checksum\n",basename);
/*TODO*///
/*TODO*///	while (romp->name || romp->offset || romp->length)
/*TODO*///	{
/*TODO*///		romp++;	/* skip memory region definition */
/*TODO*///
/*TODO*///		while (romp->length)
/*TODO*///		{
/*TODO*///			const char *name;
/*TODO*///			int length,expchecksum;
/*TODO*///
/*TODO*///
/*TODO*///			name = romp->name;
/*TODO*///			expchecksum = romp->crc;
/*TODO*///
/*TODO*///			length = 0;
/*TODO*///
/*TODO*///			do
/*TODO*///			{
/*TODO*///				/* ROM_RELOAD */
/*TODO*///				if (romp->name == (char *)-1)
/*TODO*///					length = 0;	/* restart */
/*TODO*///
/*TODO*///				length += romp->length & ~ROMFLAG_MASK;
/*TODO*///
/*TODO*///				romp++;
/*TODO*///			} while (romp->length && (romp->name == 0 || romp->name == (char *)-1));
/*TODO*///
/*TODO*///			if (expchecksum)
/*TODO*///				printf("%-12s  %7d bytes  %08x\n",name,length,expchecksum);
/*TODO*///			else
/*TODO*///				printf("%-12s  %7d bytes  NO GOOD DUMP KNOWN\n",name,length);
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Read samples into memory.
/*TODO*///  This function is different from readroms() because it doesn't fail if
/*TODO*///  it doesn't find a file: it will load as many samples as it can find.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*///#ifdef LSB_FIRST
/*TODO*///#define intelLong(x) (x)
/*TODO*///#else
/*TODO*///#define intelLong(x) (((x << 24) | (((unsigned long) x) >> 24) | (( x & 0x0000ff00) << 8) | (( x & 0x00ff0000) >> 8)))
/*TODO*///#endif
/*TODO*///
/*TODO*///static struct GameSample *read_wav_sample(void *f)
/*TODO*///{
/*TODO*///	unsigned long offset = 0;
/*TODO*///	UINT32 length, rate, filesize, temp32;
/*TODO*///	UINT16 bits, temp16;
/*TODO*///	char buf[32];
/*TODO*///	struct GameSample *result;
/*TODO*///
/*TODO*///	/* read the core header and make sure it's a WAVE file */
/*TODO*///	offset += osd_fread(f, buf, 4);
/*TODO*///	if (offset < 4)
/*TODO*///		return NULL;
/*TODO*///	if (memcmp(&buf[0], "RIFF", 4) != 0)
/*TODO*///		return NULL;
/*TODO*///
/*TODO*///	/* get the total size */
/*TODO*///	offset += osd_fread(f, &filesize, 4);
/*TODO*///	if (offset < 8)
/*TODO*///		return NULL;
/*TODO*///	filesize = intelLong(filesize);
/*TODO*///
/*TODO*///	/* read the RIFF file type and make sure it's a WAVE file */
/*TODO*///	offset += osd_fread(f, buf, 4);
/*TODO*///	if (offset < 12)
/*TODO*///		return NULL;
/*TODO*///	if (memcmp(&buf[0], "WAVE", 4) != 0)
/*TODO*///		return NULL;
/*TODO*///
/*TODO*///	/* seek until we find a format tag */
/*TODO*///	while (1)
/*TODO*///	{
/*TODO*///		offset += osd_fread(f, buf, 4);
/*TODO*///		offset += osd_fread(f, &length, 4);
/*TODO*///		length = intelLong(length);
/*TODO*///		if (memcmp(&buf[0], "fmt ", 4) == 0)
/*TODO*///			break;
/*TODO*///
/*TODO*///		/* seek to the next block */
/*TODO*///		osd_fseek(f, length, SEEK_CUR);
/*TODO*///		offset += length;
/*TODO*///		if (offset >= filesize)
/*TODO*///			return NULL;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* read the format -- make sure it is PCM */
/*TODO*///	offset += osd_fread_lsbfirst(f, &temp16, 2);
/*TODO*///	if (temp16 != 1)
/*TODO*///		return NULL;
/*TODO*///
/*TODO*///	/* number of channels -- only mono is supported */
/*TODO*///	offset += osd_fread_lsbfirst(f, &temp16, 2);
/*TODO*///	if (temp16 != 1)
/*TODO*///		return NULL;
/*TODO*///
/*TODO*///	/* sample rate */
/*TODO*///	offset += osd_fread(f, &rate, 4);
/*TODO*///	rate = intelLong(rate);
/*TODO*///
/*TODO*///	/* bytes/second and block alignment are ignored */
/*TODO*///	offset += osd_fread(f, buf, 6);
/*TODO*///
/*TODO*///	/* bits/sample */
/*TODO*///	offset += osd_fread_lsbfirst(f, &bits, 2);
/*TODO*///	if (bits != 8 && bits != 16)
/*TODO*///		return NULL;
/*TODO*///
/*TODO*///	/* seek past any extra data */
/*TODO*///	osd_fseek(f, length - 16, SEEK_CUR);
/*TODO*///	offset += length - 16;
/*TODO*///
/*TODO*///	/* seek until we find a data tag */
/*TODO*///	while (1)
/*TODO*///	{
/*TODO*///		offset += osd_fread(f, buf, 4);
/*TODO*///		offset += osd_fread(f, &length, 4);
/*TODO*///		length = intelLong(length);
/*TODO*///		if (memcmp(&buf[0], "data", 4) == 0)
/*TODO*///			break;
/*TODO*///
/*TODO*///		/* seek to the next block */
/*TODO*///		osd_fseek(f, length, SEEK_CUR);
/*TODO*///		offset += length;
/*TODO*///		if (offset >= filesize)
/*TODO*///			return NULL;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* allocate the game sample */
/*TODO*///	result = malloc(sizeof(struct GameSample) + length);
/*TODO*///	if (result == NULL)
/*TODO*///		return NULL;
/*TODO*///
/*TODO*///	/* fill in the sample data */
/*TODO*///	result->length = length;
/*TODO*///	result->smpfreq = rate;
/*TODO*///	result->resolution = bits;
/*TODO*///
/*TODO*///	/* read the data in */
/*TODO*///	if (bits == 8)
/*TODO*///	{
/*TODO*///		osd_fread(f, result->data, length);
/*TODO*///
/*TODO*///		/* convert 8-bit data to signed samples */
/*TODO*///		for (temp32 = 0; temp32 < length; temp32++)
/*TODO*///			result->data[temp32] ^= 0x80;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		/* 16-bit data is fine as-is */
/*TODO*///		osd_fread_lsbfirst(f, result->data, length);
/*TODO*///	}
/*TODO*///
/*TODO*///	return result;
/*TODO*///}
/*TODO*///
/*TODO*///struct GameSamples *readsamples(const char **samplenames,const char *basename)
/*TODO*////* V.V - avoids samples duplication */
/*TODO*////* if first samplename is *dir, looks for samples into "basename" first, then "dir" */
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	struct GameSamples *samples;
/*TODO*///	int skipfirst = 0;
/*TODO*///
/*TODO*///	/* if the user doesn't want to use samples, bail */
/*TODO*///	if (!options.use_samples) return 0;
/*TODO*///
/*TODO*///	if (samplenames == 0 || samplenames[0] == 0) return 0;
/*TODO*///
/*TODO*///	if (samplenames[0][0] == '*')
/*TODO*///		skipfirst = 1;
/*TODO*///
/*TODO*///	i = 0;
/*TODO*///	while (samplenames[i+skipfirst] != 0) i++;
/*TODO*///
/*TODO*///	if (!i) return 0;
/*TODO*///
/*TODO*///	if ((samples = malloc(sizeof(struct GameSamples) + (i-1)*sizeof(struct GameSample))) == 0)
/*TODO*///		return 0;
/*TODO*///
/*TODO*///	samples->total = i;
/*TODO*///	for (i = 0;i < samples->total;i++)
/*TODO*///		samples->sample[i] = 0;
/*TODO*///
/*TODO*///	for (i = 0;i < samples->total;i++)
/*TODO*///	{
/*TODO*///		void *f;
/*TODO*///
/*TODO*///		if (samplenames[i+skipfirst][0])
/*TODO*///		{
/*TODO*///			if ((f = osd_fopen(basename,samplenames[i+skipfirst],OSD_FILETYPE_SAMPLE,0)) == 0)
/*TODO*///				if (skipfirst)
/*TODO*///					f = osd_fopen(samplenames[0]+1,samplenames[i+skipfirst],OSD_FILETYPE_SAMPLE,0);
/*TODO*///			if (f != 0)
/*TODO*///			{
/*TODO*///				samples->sample[i] = read_wav_sample(f);
/*TODO*///				osd_fclose(f);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	return samples;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void freesamples(struct GameSamples *samples)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///
/*TODO*///	if (samples == 0) return;
/*TODO*///
/*TODO*///	for (i = 0;i < samples->total;i++)
/*TODO*///		free(samples->sample[i]);
/*TODO*///
/*TODO*///	free(samples);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///unsigned char *memory_region(int num)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///	if (num < MAX_MEMORY_REGIONS)
/*TODO*///		return Machine->memory_region[num];
/*TODO*///	else
/*TODO*///	{
/*TODO*///		for (i = 0;i < MAX_MEMORY_REGIONS;i++)
/*TODO*///		{
/*TODO*///			if ((Machine->memory_region_type[i] & ~REGIONFLAG_MASK) == num)
/*TODO*///				return Machine->memory_region[i];
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///int memory_region_length(int num)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///	if (num < MAX_MEMORY_REGIONS)
/*TODO*///		return Machine->memory_region_length[num];
/*TODO*///	else
/*TODO*///	{
/*TODO*///		for (i = 0;i < MAX_MEMORY_REGIONS;i++)
/*TODO*///		{
/*TODO*///			if ((Machine->memory_region_type[i] & ~REGIONFLAG_MASK) == num)
/*TODO*///				return Machine->memory_region_length[i];
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///int new_memory_region(int num, int length)
/*TODO*///{
/*TODO*///    int i;
/*TODO*///
/*TODO*///    if (num < MAX_MEMORY_REGIONS)
/*TODO*///    {
/*TODO*///        Machine->memory_region_length[num] = length;
/*TODO*///        Machine->memory_region[num] = malloc(length);
/*TODO*///        return (Machine->memory_region[num] == NULL) ? 1 : 0;
/*TODO*///    }
/*TODO*///    else
/*TODO*///    {
/*TODO*///        for (i = 0;i < MAX_MEMORY_REGIONS;i++)
/*TODO*///        {
/*TODO*///            if (Machine->memory_region[i] == NULL)
/*TODO*///            {
/*TODO*///                Machine->memory_region_length[i] = length;
/*TODO*///                Machine->memory_region_type[i] = num;
/*TODO*///                Machine->memory_region[i] = malloc(length);
/*TODO*///                return (Machine->memory_region[i] == NULL) ? 1 : 0;
/*TODO*///            }
/*TODO*///        }
/*TODO*///    }
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*///void free_memory_region(int num)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///	if (num < MAX_MEMORY_REGIONS)
/*TODO*///	{
/*TODO*///		free(Machine->memory_region[num]);
/*TODO*///		Machine->memory_region[num] = 0;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		for (i = 0;i < MAX_MEMORY_REGIONS;i++)
/*TODO*///		{
/*TODO*///			if ((Machine->memory_region_type[i] & ~REGIONFLAG_MASK) == num)
/*TODO*///			{
/*TODO*///				free(Machine->memory_region[i]);
/*TODO*///				Machine->memory_region[i] = 0;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
    /* LBO 042898 - added coin counters */
    public static WriteHandlerPtr coin_counter_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset >= COIN_COUNTERS) {
                return;
            }
            /* Count it only if the data has changed from 0 to non-zero */
            if (data != 0 && (lastcoin[offset] == 0)) {
                coins[offset]++;
            }
            lastcoin[offset] = data;
        }
    };

    public static WriteHandlerPtr coin_lockout_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset >= COIN_COUNTERS) {
                return;
            }
            coinlockedout[offset] = data;
        }
    };

    /* Locks out all the coin inputs */
    public static WriteHandlerPtr coin_lockout_global_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int i;

            for (i = 0; i < COIN_COUNTERS; i++) {
                coin_lockout_w.handler(i, data);
            }
        }
    };

    /*TODO*////* Locks out all the coin inputs */
/*TODO*///void coin_lockout_global_w (int offset, int data)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///	for (i = 0; i < COIN_COUNTERS; i++)
/*TODO*///	{
/*TODO*///		coin_lockout_w(i, data);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
    public static int snapno;

    public static void save_screen_snapshot() {
        /*TODO*///	void *fp;
/*TODO*///	char name[20];
/*TODO*///
/*TODO*///
/*TODO*///	/* avoid overwriting existing files */
/*TODO*///	/* first of all try with "gamename.png" */
/*TODO*///	sprintf(name,"%.8s", Machine->gamedrv->name);
/*TODO*///	if (osd_faccess(name,OSD_FILETYPE_SCREENSHOT))
/*TODO*///	{
/*TODO*///		do
/*TODO*///		{
/*TODO*///			/* otherwise use "nameNNNN.png" */
/*TODO*///			sprintf(name,"%.4s%04d",Machine->gamedrv->name,snapno++);
/*TODO*///		} while (osd_faccess(name, OSD_FILETYPE_SCREENSHOT));
/*TODO*///	}
/*TODO*///
/*TODO*///	if ((fp = osd_fopen(Machine->gamedrv->name, name, OSD_FILETYPE_SCREENSHOT, 1)) != NULL)
/*TODO*///	{
/*TODO*///		if (Machine->drv->video_attributes & VIDEO_TYPE_VECTOR)
/*TODO*///			png_write_bitmap(fp,Machine->scrbitmap);
/*TODO*///		else
/*TODO*///		{
/*TODO*///			struct osd_bitmap *bitmap;
/*TODO*///
/*TODO*///			bitmap = osd_new_bitmap(
/*TODO*///					Machine->drv->visible_area.max_x - Machine->drv->visible_area.min_x + 1,
/*TODO*///					Machine->drv->visible_area.max_y - Machine->drv->visible_area.min_y + 1,
/*TODO*///					Machine->scrbitmap->depth);
/*TODO*///
/*TODO*///			if (bitmap)
/*TODO*///			{
/*TODO*///				copybitmap(bitmap,Machine->scrbitmap,0,0,
/*TODO*///						-Machine->drv->visible_area.min_x,-Machine->drv->visible_area.min_y,0,TRANSPARENCY_NONE,0);
/*TODO*///				png_write_bitmap(fp,bitmap);
/*TODO*///				osd_free_bitmap(bitmap);
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		osd_fclose(fp);
/*TODO*///	}
    }
}
