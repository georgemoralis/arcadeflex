/*********************************************************************

  common.c

  Generic functions, mostly ROM and graphics related.

*********************************************************************/

#include "driver.h"
#include "png.h"

/* These globals are only kept on a machine basis - LBO 042898 */
unsigned int dispensed_tickets;
unsigned int coins[COIN_COUNTERS];
unsigned int lastcoin[COIN_COUNTERS];
unsigned int coinlockedout[COIN_COUNTERS];



void showdisclaimer(void)   /* MAURY_BEGIN: dichiarazione */
{
	printf("MAME is an emulator: it reproduces, more or less faithfully, the behaviour of\n"
		 "several arcade machines. But hardware is useless without software, so an image\n"
		 "of the ROMs which run on that hardware is required. Such ROMs, like any other\n"
		 "commercial software, are copyrighted material and it is therefore illegal to\n"
		 "use them if you don't own the original arcade machine. Needless to say, ROMs\n"
		 "are not distributed together with MAME. Distribution of MAME together with ROM\n"
		 "images is a violation of copyright law and should be promptly reported to the\n"
		 "authors so that appropriate legal action can be taken.\n\n");
}                           /* MAURY_END: dichiarazione */


/***************************************************************************

  Read ROMs into memory.

  Arguments:
  const struct RomModule *romp - pointer to an array of Rommodule structures,
                                 as defined in common.h.

***************************************************************************/

int readroms(void)
{
	int region;
	const struct RomModule *romp;
	int warning = 0;
	int fatalerror = 0;
	int total_roms,current_rom;
	char buf[4096] = "";


	total_roms = current_rom = 0;
	romp = Machine->gamedrv->rom;

	if (!romp) return 0;

	while (romp->name || romp->offset || romp->length)
	{
		if (romp->name && romp->name != (char *)-1)
			total_roms++;

		romp++;
	}


	romp = Machine->gamedrv->rom;

	for (region = 0;region < MAX_MEMORY_REGIONS;region++)
		Machine->memory_region[region] = 0;

	region = 0;

	while (romp->name || romp->offset || romp->length)
	{
		unsigned int region_size;
		const char *name;

		/* Mish:  An 'optional' rom region, only loaded if sound emulation is turned on */
		if (Machine->sample_rate==0 && (romp->crc & REGIONFLAG_SOUNDONLY)) {
			if (errorlog) fprintf(errorlog,"readroms():  Ignoring rom region %d\n",region);
			Machine->memory_region_type[region] = romp->crc;
			region++;

			romp++;
			while (romp->name || romp->length)
				romp++;

			continue;
		}

		if (romp->name || romp->length)
		{
			printf("Error in RomModule definition: expecting ROM_REGION\n");
			goto getout;
		}

		region_size = romp->offset;
		if ((Machine->memory_region[region] = malloc(region_size)) == 0)
		{
			printf("readroms():  Unable to allocate %d bytes of RAM\n",region_size);
			goto getout;
		}
		Machine->memory_region_length[region] = region_size;
		Machine->memory_region_type[region] = romp->crc;

		/* some games (i.e. Pleiades) want the memory clear on startup */
		if (region_size <= 0x400000)	/* don't clear large regions which will be filled anyway */
			memset(Machine->memory_region[region],0,region_size);

		romp++;

		while (romp->length)
		{
			void *f;
			int expchecksum = romp->crc;
			int	explength = 0;


			if (romp->name == 0)
			{
				printf("Error in RomModule definition: ROM_CONTINUE not preceded by ROM_LOAD\n");
				goto getout;
			}
			else if (romp->name == (char *)-1)
			{
				printf("Error in RomModule definition: ROM_RELOAD not preceded by ROM_LOAD\n");
				goto getout;
			}

			name = romp->name;

			/* update status display */
			if (osd_display_loading_rom_message(name,++current_rom,total_roms) != 0)
               goto getout;

			{
				const struct GameDriver *drv;

				drv = Machine->gamedrv;
				do
				{
					f = osd_fopen(drv->name,name,OSD_FILETYPE_ROM,0);
					drv = drv->clone_of;
				} while (f == 0 && drv);

				if (f == 0)
				{
					/* NS981003: support for "load by CRC" */
					char crc[9];

					sprintf(crc,"%08x",romp->crc);
					drv = Machine->gamedrv;
					do
					{
						f = osd_fopen(drv->name,crc,OSD_FILETYPE_ROM,0);
						drv = drv->clone_of;
					} while (f == 0 && drv);
				}
			}

			if (f)
			{
				do
				{
					unsigned char *c;
					unsigned int i;
					int length = romp->length & ~ROMFLAG_MASK;


					if (romp->name == (char *)-1)
						osd_fseek(f,0,SEEK_SET);	/* ROM_RELOAD */
					else
						explength += length;

					if (romp->offset + length > region_size ||
						(!(romp->length & ROMFLAG_NIBBLE) && (romp->length & ROMFLAG_ALTERNATE)
								&& (romp->offset&~1) + 2*length > region_size))
					{
						printf("Error in RomModule definition: %s out of memory region space\n",name);
						osd_fclose(f);
						goto getout;
					}

					if (romp->length & ROMFLAG_NIBBLE)
					{
						unsigned char *temp;


						temp = malloc(length);

						if (!temp)
						{
							printf("Out of memory reading ROM %s\n",name);
							osd_fclose(f);
							goto getout;
						}

						if (osd_fread(f,temp,length) != length)
						{
							printf("Unable to read ROM %s\n",name);
						}

						/* ROM_LOAD_NIB_LOW and ROM_LOAD_NIB_HIGH */
						c = Machine->memory_region[region] + romp->offset;
						if (romp->length & ROMFLAG_ALTERNATE)
						{
							/* Load into the high nibble */
							for (i = 0;i < length;i ++)
							{
								c[i] = (c[i] & 0x0f) | ((temp[i] & 0x0f) << 4);
							}
						}
						else
						{
							/* Load into the low nibble */
							for (i = 0;i < length;i ++)
							{
								c[i] = (c[i] & 0xf0) | (temp[i] & 0x0f);
							}
						}

						free (temp);
					}
					else if (romp->length & ROMFLAG_ALTERNATE)
					{
						/* ROM_LOAD_EVEN and ROM_LOAD_ODD */
						/* copy the ROM data */
					#ifdef LSB_FIRST
						c = Machine->memory_region[region] + (romp->offset ^ 1);
					#else
						c = Machine->memory_region[region] + romp->offset;
					#endif

						if (osd_fread_scatter(f,c,length,2) != length)
						{
							printf("Unable to read ROM %s\n",name);
						}
					}
					else if (romp->length & ROMFLAG_QUAD) {
						static int which_quad=0; /* This is multi session friendly, as we only care about the modulus */
						unsigned char *temp;
						int base=0;

						temp = malloc(length);	/* Need to load rom to temporary space */
						osd_fread(f,temp,length);

						/* Copy quad to region */
						c = Machine->memory_region[region] + romp->offset;

					#ifdef LSB_FIRST
						switch (which_quad%4) {
							case 0: base=1; break;
							case 1: base=0; break;
							case 2: base=3; break;
							case 3: base=2; break;
						}
					#else
						switch (which_quad%4) {
							case 0: base=0; break;
							case 1: base=1; break;
							case 2: base=2; break;
							case 3: base=3; break;
						}
					#endif

						for (i=base; i< length*4; i += 4)
							c[i]=temp[i/4];

						which_quad++;
						free(temp);
					}
					else
					{
						int wide = romp->length & ROMFLAG_WIDE;
					#ifdef LSB_FIRST
						int swap = (romp->length & ROMFLAG_SWAP) ^ ROMFLAG_SWAP;
					#else
						int swap = romp->length & ROMFLAG_SWAP;
					#endif

						osd_fread(f,Machine->memory_region[region] + romp->offset,length);

						/* apply swappage */
						c = Machine->memory_region[region] + romp->offset;
						if (wide && swap)
						{
							for (i = 0; i < length; i += 2)
							{
								int temp = c[i];
								c[i] = c[i+1];
								c[i+1] = temp;
							}
						}
					}

					romp++;
				} while (romp->length && (romp->name == 0 || romp->name == (char *)-1));

				if (explength != osd_fsize (f))
				{
					sprintf (&buf[strlen(buf)], "%-12s WRONG LENGTH (expected: %08x found: %08x)\n",
							name,explength,osd_fsize(f));
					warning = 1;
				}

				if (expchecksum != osd_fcrc (f))
				{
					warning = 1;
					if (expchecksum == 0)
						sprintf(&buf[strlen(buf)],"%-12s NO GOOD DUMP KNOWN\n",name);
					else if (expchecksum == BADCRC(osd_fcrc(f)))
						sprintf(&buf[strlen(buf)],"%-12s ROM NEEDS REDUMP\n",name);
					else
						sprintf(&buf[strlen(buf)], "%-12s WRONG CRC (expected: %08x found: %08x)\n",
								name,expchecksum,osd_fcrc(f));
				}

				osd_fclose(f);
			}
			else
			{
				/* allow for a NO GOOD DUMP KNOWN rom to be missing */
				if (expchecksum == 0)
				{
					sprintf (&buf[strlen(buf)], "%-12s NOT FOUND (NO GOOD DUMP KNOWN)\n",name);
					warning = 1;
				}
				else
				{
					sprintf (&buf[strlen(buf)], "%-12s NOT FOUND\n",name);
					fatalerror = 1;
				}

				do
				{
					if (fatalerror == 0)
					{
						int i;

						/* fill space with random data */
						if (romp->length & ROMFLAG_ALTERNATE)
						{
							unsigned char *c;

							/* ROM_LOAD_EVEN and ROM_LOAD_ODD */
						#ifdef LSB_FIRST
							c = Machine->memory_region[region] + (romp->offset ^ 1);
						#else
							c = Machine->memory_region[region] + romp->offset;
						#endif

							for (i = 0;i < (romp->length & ~ROMFLAG_MASK);i++)
								c[2*i] = rand();
						}
						else
						{
							for (i = 0;i < (romp->length & ~ROMFLAG_MASK);i++)
								Machine->memory_region[region][romp->offset + i] = rand();
						}
					}
					romp++;
				} while (romp->length && (romp->name == 0 || romp->name == (char *)-1));
			}
		}

		region++;
	}

	/* final status display */
	osd_display_loading_rom_message(0,current_rom,total_roms);

	if (warning || fatalerror)
	{
		extern int bailing;

		if (fatalerror)
		{
			strcat (buf, "ERROR: required files are missing, the game cannot be run.\n");
			bailing = 1;
		}
		else
			strcat (buf, "WARNING: the game might not run correctly.\n");
		printf ("%s", buf);

		if (!options.gui_host && !bailing)
		{
			printf ("Press any key to continue\n");
			keyboard_read_sync();
			if (keyboard_pressed(KEYCODE_LCONTROL) && keyboard_pressed(KEYCODE_C))
				return 1;
		}
	}

	if (fatalerror) return 1;
	else return 0;


getout:
	/* final status display */
	osd_display_loading_rom_message(0,current_rom,total_roms);

	for (region = 0;region < MAX_MEMORY_REGIONS;region++)
	{
		free(Machine->memory_region[region]);
		Machine->memory_region[region] = 0;
	}

	return 1;
}


void printromlist(const struct RomModule *romp,const char *basename)
{
	if (!romp) return;

#ifdef MESS
	if (!strcmp(basename,"nes")) return;
#endif

	printf("This is the list of the ROMs required for driver \"%s\".\n"
			"Name              Size       Checksum\n",basename);

	while (romp->name || romp->offset || romp->length)
	{
		romp++;	/* skip memory region definition */

		while (romp->length)
		{
			const char *name;
			int length,expchecksum;


			name = romp->name;
			expchecksum = romp->crc;

			length = 0;

			do
			{
				/* ROM_RELOAD */
				if (romp->name == (char *)-1)
					length = 0;	/* restart */

				length += romp->length & ~ROMFLAG_MASK;

				romp++;
			} while (romp->length && (romp->name == 0 || romp->name == (char *)-1));

			if (expchecksum)
				printf("%-12s  %7d bytes  %08x\n",name,length,expchecksum);
			else
				printf("%-12s  %7d bytes  NO GOOD DUMP KNOWN\n",name,length);
		}
	}
}



/***************************************************************************

  Read samples into memory.
  This function is different from readroms() because it doesn't fail if
  it doesn't find a file: it will load as many samples as it can find.

***************************************************************************/

#ifdef LSB_FIRST
#define intelLong(x) (x)
#else
#define intelLong(x) (((x << 24) | (((unsigned long) x) >> 24) | (( x & 0x0000ff00) << 8) | (( x & 0x00ff0000) >> 8)))
#endif

static struct GameSample *read_wav_sample(void *f)
{
	unsigned long offset = 0;
	UINT32 length, rate, filesize, temp32;
	UINT16 bits, temp16;
	char buf[32];
	struct GameSample *result;

	/* read the core header and make sure it's a WAVE file */
	offset += osd_fread(f, buf, 4);
	if (offset < 4)
		return NULL;
	if (memcmp(&buf[0], "RIFF", 4) != 0)
		return NULL;

	/* get the total size */
	offset += osd_fread(f, &filesize, 4);
	if (offset < 8)
		return NULL;
	filesize = intelLong(filesize);

	/* read the RIFF file type and make sure it's a WAVE file */
	offset += osd_fread(f, buf, 4);
	if (offset < 12)
		return NULL;
	if (memcmp(&buf[0], "WAVE", 4) != 0)
		return NULL;

	/* seek until we find a format tag */
	while (1)
	{
		offset += osd_fread(f, buf, 4);
		offset += osd_fread(f, &length, 4);
		length = intelLong(length);
		if (memcmp(&buf[0], "fmt ", 4) == 0)
			break;

		/* seek to the next block */
		osd_fseek(f, length, SEEK_CUR);
		offset += length;
		if (offset >= filesize)
			return NULL;
	}

	/* read the format -- make sure it is PCM */
	offset += osd_fread_lsbfirst(f, &temp16, 2);
	if (temp16 != 1)
		return NULL;

	/* number of channels -- only mono is supported */
	offset += osd_fread_lsbfirst(f, &temp16, 2);
	if (temp16 != 1)
		return NULL;

	/* sample rate */
	offset += osd_fread(f, &rate, 4);
	rate = intelLong(rate);

	/* bytes/second and block alignment are ignored */
	offset += osd_fread(f, buf, 6);

	/* bits/sample */
	offset += osd_fread_lsbfirst(f, &bits, 2);
	if (bits != 8 && bits != 16)
		return NULL;

	/* seek past any extra data */
	osd_fseek(f, length - 16, SEEK_CUR);
	offset += length - 16;

	/* seek until we find a data tag */
	while (1)
	{
		offset += osd_fread(f, buf, 4);
		offset += osd_fread(f, &length, 4);
		length = intelLong(length);
		if (memcmp(&buf[0], "data", 4) == 0)
			break;

		/* seek to the next block */
		osd_fseek(f, length, SEEK_CUR);
		offset += length;
		if (offset >= filesize)
			return NULL;
	}

	/* allocate the game sample */
	result = malloc(sizeof(struct GameSample) + length);
	if (result == NULL)
		return NULL;

	/* fill in the sample data */
	result->length = length;
	result->smpfreq = rate;
	result->resolution = bits;

	/* read the data in */
	if (bits == 8)
	{
		osd_fread(f, result->data, length);

		/* convert 8-bit data to signed samples */
		for (temp32 = 0; temp32 < length; temp32++)
			result->data[temp32] ^= 0x80;
	}
	else
	{
		/* 16-bit data is fine as-is */
		osd_fread_lsbfirst(f, result->data, length);
	}

	return result;
}

struct GameSamples *readsamples(const char **samplenames,const char *basename)
/* V.V - avoids samples duplication */
/* if first samplename is *dir, looks for samples into "basename" first, then "dir" */
{
	int i;
	struct GameSamples *samples;
	int skipfirst = 0;

	/* if the user doesn't want to use samples, bail */
	if (!options.use_samples) return 0;

	if (samplenames == 0 || samplenames[0] == 0) return 0;

	if (samplenames[0][0] == '*')
		skipfirst = 1;

	i = 0;
	while (samplenames[i+skipfirst] != 0) i++;

	if (!i) return 0;

	if ((samples = malloc(sizeof(struct GameSamples) + (i-1)*sizeof(struct GameSample))) == 0)
		return 0;

	samples->total = i;
	for (i = 0;i < samples->total;i++)
		samples->sample[i] = 0;

	for (i = 0;i < samples->total;i++)
	{
		void *f;

		if (samplenames[i+skipfirst][0])
		{
			if ((f = osd_fopen(basename,samplenames[i+skipfirst],OSD_FILETYPE_SAMPLE,0)) == 0)
				if (skipfirst)
					f = osd_fopen(samplenames[0]+1,samplenames[i+skipfirst],OSD_FILETYPE_SAMPLE,0);
			if (f != 0)
			{
				samples->sample[i] = read_wav_sample(f);
				osd_fclose(f);
			}
		}
	}

	return samples;
}


void freesamples(struct GameSamples *samples)
{
	int i;


	if (samples == 0) return;

	for (i = 0;i < samples->total;i++)
		free(samples->sample[i]);

	free(samples);
}



unsigned char *memory_region(int num)
{
	int i;

	if (num < MAX_MEMORY_REGIONS)
		return Machine->memory_region[num];
	else
	{
		for (i = 0;i < MAX_MEMORY_REGIONS;i++)
		{
			if ((Machine->memory_region_type[i] & ~REGIONFLAG_MASK) == num)
				return Machine->memory_region[i];
		}
	}

	return 0;
}

int memory_region_length(int num)
{
	int i;

	if (num < MAX_MEMORY_REGIONS)
		return Machine->memory_region_length[num];
	else
	{
		for (i = 0;i < MAX_MEMORY_REGIONS;i++)
		{
			if ((Machine->memory_region_type[i] & ~REGIONFLAG_MASK) == num)
				return Machine->memory_region_length[i];
		}
	}

	return 0;
}

int new_memory_region(int num, int length)
{
    int i;

    if (num < MAX_MEMORY_REGIONS)
    {
        Machine->memory_region_length[num] = length;
        Machine->memory_region[num] = malloc(length);
        return (Machine->memory_region[num] == NULL) ? 1 : 0;
    }
    else
    {
        for (i = 0;i < MAX_MEMORY_REGIONS;i++)
        {
            if (Machine->memory_region[i] == NULL)
            {
                Machine->memory_region_length[i] = length;
                Machine->memory_region_type[i] = num;
                Machine->memory_region[i] = malloc(length);
                return (Machine->memory_region[i] == NULL) ? 1 : 0;
            }
        }
    }
	return 1;
}

void free_memory_region(int num)
{
	int i;

	if (num < MAX_MEMORY_REGIONS)
	{
		free(Machine->memory_region[num]);
		Machine->memory_region[num] = 0;
	}
	else
	{
		for (i = 0;i < MAX_MEMORY_REGIONS;i++)
		{
			if ((Machine->memory_region_type[i] & ~REGIONFLAG_MASK) == num)
			{
				free(Machine->memory_region[i]);
				Machine->memory_region[i] = 0;
				return;
			}
		}
	}
}


/* LBO 042898 - added coin counters */
void coin_counter_w (int offset, int data)
{
	if (offset >= COIN_COUNTERS) return;
	/* Count it only if the data has changed from 0 to non-zero */
	if (data && (lastcoin[offset] == 0))
	{
		coins[offset] ++;
	}
	lastcoin[offset] = data;
}

void coin_lockout_w (int offset, int data)
{
	if (offset >= COIN_COUNTERS) return;

	coinlockedout[offset] = data;
}

/* Locks out all the coin inputs */
void coin_lockout_global_w (int offset, int data)
{
	int i;

	for (i = 0; i < COIN_COUNTERS; i++)
	{
		coin_lockout_w(i, data);
	}
}



int snapno;

void save_screen_snapshot(void)
{
	void *fp;
	char name[20];


	/* avoid overwriting existing files */
	/* first of all try with "gamename.png" */
	sprintf(name,"%.8s", Machine->gamedrv->name);
	if (osd_faccess(name,OSD_FILETYPE_SCREENSHOT))
	{
		do
		{
			/* otherwise use "nameNNNN.png" */
			sprintf(name,"%.4s%04d",Machine->gamedrv->name,snapno++);
		} while (osd_faccess(name, OSD_FILETYPE_SCREENSHOT));
	}

	if ((fp = osd_fopen(Machine->gamedrv->name, name, OSD_FILETYPE_SCREENSHOT, 1)) != NULL)
	{
		if (Machine->drv->video_attributes & VIDEO_TYPE_VECTOR)
			png_write_bitmap(fp,Machine->scrbitmap);
		else
		{
			struct osd_bitmap *bitmap;

			bitmap = osd_new_bitmap(
					Machine->drv->visible_area.max_x - Machine->drv->visible_area.min_x + 1,
					Machine->drv->visible_area.max_y - Machine->drv->visible_area.min_y + 1,
					Machine->scrbitmap->depth);

			if (bitmap)
			{
				copybitmap(bitmap,Machine->scrbitmap,0,0,
						-Machine->drv->visible_area.min_x,-Machine->drv->visible_area.min_y,0,TRANSPARENCY_NONE,0);
				png_write_bitmap(fp,bitmap);
				osd_free_bitmap(bitmap);
			}
		}

		osd_fclose(fp);
	}
}
