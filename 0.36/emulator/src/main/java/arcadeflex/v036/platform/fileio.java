/**
 * ported to v0.36
 */
package arcadeflex.v036.platform;

import static arcadeflex.v036.mame.osdependH.OSD_FILETYPE_SCREENSHOT;
import static gr.codebb.arcadeflex.v036.platform.fileio.screenshotdir;
import gr.codebb.arcadeflex.v036.platform.libc_old.FILE;
import static gr.codebb.arcadeflex.v036.platform.libc_old.fclose;
import static gr.codebb.arcadeflex.v036.platform.libc_old.fopen;
import static gr.codebb.arcadeflex.v036.platform.libc_old.sprintf;

public class fileio {

    /*TODO*///#include "mamalleg.h"
/*TODO*///#include "driver.h"
/*TODO*///#include "unzip.h"
/*TODO*///#include <sys/stat.h>
/*TODO*///#include <unistd.h>
/*TODO*///#include <signal.h>
/*TODO*///
/*TODO*///#ifdef MESS
/*TODO*///#include "mess/msdos.h"
/*TODO*///#endif
/*TODO*///
/*TODO*////* Verbose outputs to error.log ? */
/*TODO*///#define VERBOSE 	0
/*TODO*///
/*TODO*////* Use the file cache ? */
/*TODO*///#define FILE_CACHE	1
/*TODO*///
/*TODO*///#if VERBOSE
/*TODO*///#define LOG(x)	if(errorlog) fprintf x
/*TODO*///#else
/*TODO*///#define LOG(x)	/* x */
/*TODO*///#endif
/*TODO*///
/*TODO*///char *roms = NULL;
/*TODO*///char **rompathv = NULL;
/*TODO*///int rompathc = 0;
/*TODO*///
/*TODO*///char *samples = NULL;
/*TODO*///char **samplepathv = NULL;
/*TODO*///int samplepathc = 0;
/*TODO*///
/*TODO*///char *cfgdir, *nvdir, *hidir, *inpdir, *stadir;
/*TODO*///char *memcarddir, *artworkdir, *screenshotdir;
/*TODO*///
/*TODO*///char *alternate_name;				   /* for "-romdir" */
/*TODO*///
/*TODO*///typedef enum
/*TODO*///{
/*TODO*///	kPlainFile,
/*TODO*///	kRAMFile,
/*TODO*///	kZippedFile
/*TODO*///}	eFileType;
/*TODO*///
/*TODO*///typedef struct
/*TODO*///{
/*TODO*///	FILE *file;
/*TODO*///	unsigned char *data;
/*TODO*///	unsigned int offset;
/*TODO*///	unsigned int length;
/*TODO*///	eFileType type;
/*TODO*///	unsigned int crc;
/*TODO*///}	FakeFileHandle;
/*TODO*///
/*TODO*///
/*TODO*///extern unsigned int crc32 (unsigned int crc, const unsigned char *buf, unsigned int len);
/*TODO*///static int checksum_file (const char *file, unsigned char **p, unsigned int *size, unsigned int *crc);
/*TODO*///
/*TODO*////*
/*TODO*/// * File stat cache LRU (Last Recently Used)
/*TODO*/// */
/*TODO*///
/*TODO*///#if FILE_CACHE
/*TODO*///struct file_cache_entry
/*TODO*///{
/*TODO*///	struct stat stat_buffer;
/*TODO*///	int result;
/*TODO*///	char *file;
/*TODO*///};
/*TODO*///
/*TODO*////* File cache buffer */
/*TODO*///static struct file_cache_entry **file_cache_map = 0;
/*TODO*///
/*TODO*////* File cache size */
/*TODO*///static unsigned int file_cache_max = 0;
/*TODO*///
/*TODO*////* AM 980919 */
/*TODO*///static int cache_stat (const char *path, struct stat *statbuf)
/*TODO*///{
/*TODO*///	if( file_cache_max )
/*TODO*///	{
/*TODO*///		unsigned i;
/*TODO*///		struct file_cache_entry *entry;
/*TODO*///
/*TODO*///		/* search in the cache */
/*TODO*///		for( i = 0; i < file_cache_max; ++i )
/*TODO*///		{
/*TODO*///			if( file_cache_map[i]->file && strcmp (file_cache_map[i]->file, path) == 0 )
/*TODO*///			{	/* found */
/*TODO*///				unsigned j;
/*TODO*///
/*TODO*/////				LOG((errorlog,"File cache HIT  for %s\n", path));
/*TODO*///                /* store */
/*TODO*///				entry = file_cache_map[i];
/*TODO*///
/*TODO*///				/* shift */
/*TODO*///				for( j = i; j > 0; --j )
/*TODO*///					file_cache_map[j] = file_cache_map[j - 1];
/*TODO*///
/*TODO*///				/* set the first entry */
/*TODO*///				file_cache_map[0] = entry;
/*TODO*///
/*TODO*///				if( entry->result == 0 )
/*TODO*///					memcpy (statbuf, &entry->stat_buffer, sizeof (struct stat));
/*TODO*///
/*TODO*///				return entry->result;
/*TODO*///			}
/*TODO*///		}
/*TODO*/////		LOG((errorlog,"File cache FAIL for %s\n", path));
/*TODO*///
/*TODO*///		/* oldest entry */
/*TODO*///		entry = file_cache_map[file_cache_max - 1];
/*TODO*///		free (entry->file);
/*TODO*///
/*TODO*///		/* shift */
/*TODO*///		for( i = file_cache_max - 1; i > 0; --i )
/*TODO*///			file_cache_map[i] = file_cache_map[i - 1];
/*TODO*///
/*TODO*///		/* set the first entry */
/*TODO*///		file_cache_map[0] = entry;
/*TODO*///
/*TODO*///		/* file */
/*TODO*///		entry->file = (char *) malloc (strlen (path) + 1);
/*TODO*///		strcpy (entry->file, path);
/*TODO*///
/*TODO*///		/* result and stat */
/*TODO*///		entry->result = stat (path, &entry->stat_buffer);
/*TODO*///
/*TODO*///		if( entry->result == 0 )
/*TODO*///			memcpy (statbuf, &entry->stat_buffer, sizeof (struct stat));
/*TODO*///
/*TODO*///		return entry->result;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		return stat (path, statbuf);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* AM 980919 */
/*TODO*///static void cache_allocate (unsigned entries)
/*TODO*///{
/*TODO*///	if( entries )
/*TODO*///	{
/*TODO*///		unsigned i;
/*TODO*///
/*TODO*///		file_cache_max = entries;
/*TODO*///		file_cache_map = (struct file_cache_entry **) malloc (file_cache_max * sizeof (struct file_cache_entry *));
/*TODO*///
/*TODO*///		for( i = 0; i < file_cache_max; ++i )
/*TODO*///		{
/*TODO*///			file_cache_map[i] = (struct file_cache_entry *) malloc (sizeof (struct file_cache_entry));
/*TODO*///			memset (file_cache_map[i], 0, sizeof (struct file_cache_entry));
/*TODO*///		}
/*TODO*///		LOG((errorlog, "File cache allocated for %d entries\n", file_cache_max));
/*TODO*///	}
/*TODO*///}
/*TODO*///#else
/*TODO*///
/*TODO*///#define cache_stat(a,b) stat(a,b)
/*TODO*///
/*TODO*///#endif
/*TODO*///
/*TODO*////* This function can be called several times with different parameters,
/*TODO*/// * for example by "mame -verifyroms *". */
/*TODO*///void decompose_rom_sample_path (char *rompath, char *samplepath)
/*TODO*///{
/*TODO*///	char *token;
/*TODO*///
/*TODO*///	/* start with zero path components */
/*TODO*///	rompathc = samplepathc = 0;
/*TODO*///
/*TODO*///	if (!roms)
/*TODO*///		roms = malloc( strlen(rompath) + 1);
/*TODO*///	else
/*TODO*///		roms = realloc( roms, strlen(rompath) + 1);
/*TODO*///
/*TODO*///	if (!samples)
/*TODO*///		samples = malloc( strlen(samplepath) + 1);
/*TODO*///	else
/*TODO*///		samples = realloc( samples, strlen(samplepath) + 1);
/*TODO*///
/*TODO*///	if( !roms || !samples )
/*TODO*///	{
/*TODO*///		if( errorlog ) fprintf(errorlog, "decompose_rom_sample_path: failed to malloc!\n");
/*TODO*///		raise(SIGABRT);
/*TODO*///	}
/*TODO*///
/*TODO*///	strcpy (roms, rompath);
/*TODO*///	token = strtok (roms, ";");
/*TODO*///	while( token )
/*TODO*///	{
/*TODO*///		if( rompathc )
/*TODO*///			rompathv = realloc (rompathv, (rompathc + 1) * sizeof(char *));
/*TODO*///		else
/*TODO*///			rompathv = malloc (sizeof(char *));
/*TODO*///		if( !rompathv )
/*TODO*///			break;
/*TODO*///		rompathv[rompathc++] = token;
/*TODO*///		token = strtok (NULL, ";");
/*TODO*///	}
/*TODO*///
/*TODO*///	strcpy (samples, samplepath);
/*TODO*///	token = strtok (samples, ";");
/*TODO*///	while( token )
/*TODO*///	{
/*TODO*///		if( samplepathc )
/*TODO*///			samplepathv = realloc (samplepathv, (samplepathc + 1) * sizeof(char *));
/*TODO*///		else
/*TODO*///			samplepathv = malloc (sizeof(char *));
/*TODO*///		if( !samplepathv )
/*TODO*///			break;
/*TODO*///		samplepathv[samplepathc++] = token;
/*TODO*///		token = strtok (NULL, ";");
/*TODO*///	}
/*TODO*///
/*TODO*///#if FILE_CACHE
/*TODO*///    /* AM 980919 */
/*TODO*///    if( file_cache_max == 0 )
/*TODO*///    {
/*TODO*///        /* (rom path directories + 1 buffer)==rompathc+1 */
/*TODO*///        /* (dir + .zip + .zif)==3 */
/*TODO*///        /* (clone+parent)==2 */
/*TODO*///        cache_allocate ((rompathc + 1) * 3 * 2);
/*TODO*///    }
/*TODO*///#endif
/*TODO*///
/*TODO*///}
/*TODO*///
/*TODO*////*
/*TODO*/// * file handling routines
/*TODO*/// *
/*TODO*/// * gamename holds the driver name, filename is only used for ROMs and samples.
/*TODO*/// * if 'write' is not 0, the file is opened for write. Otherwise it is opened
/*TODO*/// * for read.
/*TODO*/// */
    /**
     * check if roms/samples for a game exist at all return index+1 of the path
     * vector component on success, otherwise 0
     */
    public static int osd_faccess(String newfilename, int filetype) {
        /*TODO*///	static int indx;
/*TODO*///	static const char *filename;
        String name = "";
        /*TODO*///    char **pathv;
/*TODO*///    int pathc;
/*TODO*///	char *dir_name;
/*TODO*///
/*TODO*///	/* if filename == NULL, continue the search */
/*TODO*///	if( newfilename != NULL )
/*TODO*///	{
/*TODO*///		indx = 0;
/*TODO*///		filename = newfilename;
/*TODO*///	}
/*TODO*///	else
/*TODO*///		indx++;
/*TODO*///
/*TODO*///#ifdef MESS
/*TODO*///	if( filetype == OSD_FILETYPE_ROM ||
/*TODO*///		filetype == OSD_FILETYPE_IMAGE_R ||
/*TODO*///		filetype == OSD_FILETYPE_IMAGE_RW )
/*TODO*///#else
/*TODO*///	if( filetype == OSD_FILETYPE_ROM )
/*TODO*///#endif
/*TODO*///	{
/*TODO*///		pathv = rompathv;
/*TODO*///		pathc = rompathc;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	if( filetype == OSD_FILETYPE_SAMPLE )
/*TODO*///	{
/*TODO*///		pathv = samplepathv;
/*TODO*///		pathc = samplepathc;
/*TODO*///	}
/*TODO*///	else
        if (filetype == OSD_FILETYPE_SCREENSHOT) {
            FILE f;

            name = sprintf("%s/%s.png", screenshotdir, newfilename);
            f = fopen(name, "rb");
            if (f != null) {
                fclose(f);
                return 1;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
        /*TODO*///
/*TODO*///	for( ; indx < pathc; indx++ )
/*TODO*///	{
/*TODO*///		struct stat stat_buffer;
/*TODO*///
/*TODO*///		dir_name = pathv[indx];
/*TODO*///
/*TODO*///		/* does such a directory (or file) exist? */
/*TODO*///		sprintf (name, "%s/%s", dir_name, filename);
/*TODO*///		if( cache_stat (name, &stat_buffer) == 0 )
/*TODO*///			return indx + 1;
/*TODO*///
/*TODO*///		/* try again with a .zip extension */
/*TODO*///		sprintf (name, "%s/%s.zip", dir_name, filename);
/*TODO*///		if( cache_stat (name, &stat_buffer) == 0 )
/*TODO*///			return indx + 1;
/*TODO*///
/*TODO*///		/* try again with a .zif extension */
/*TODO*///		sprintf (name, "%s/%s.zif", dir_name, filename);
/*TODO*///		if( cache_stat (name, &stat_buffer) == 0 )
/*TODO*///			return indx + 1;
/*TODO*///	}
/*TODO*///
/*TODO*///        /* no match */
/*TODO*///        return 0;
    }
    /*TODO*///
/*TODO*////* JB 980920 update */
/*TODO*////* AM 980919 update */
/*TODO*///void *osd_fopen (const char *game, const char *filename, int filetype, int _write)
/*TODO*///{
/*TODO*///	char name[256];
/*TODO*///	char *gamename;
/*TODO*///	int found = 0;
/*TODO*///	int indx;
/*TODO*///	struct stat stat_buffer;
/*TODO*///	FakeFileHandle *f;
/*TODO*///	int pathc;
/*TODO*///	char **pathv;
/*TODO*///
/*TODO*///
/*TODO*///	f = (FakeFileHandle *) malloc (sizeof (FakeFileHandle));
/*TODO*///	if( !f )
/*TODO*///	{
/*TODO*///		LOG((errorlog, "osd_fopen: failed to malloc FakeFileHandle!\n"));
/*TODO*///        return 0;
/*TODO*///	}
/*TODO*///	memset (f, 0, sizeof (FakeFileHandle));
/*TODO*///
/*TODO*///	gamename = (char *) game;
/*TODO*///
/*TODO*///	/* Support "-romdir" yuck. */
/*TODO*///	if( alternate_name )
/*TODO*///	{
/*TODO*///		LOG((errorlog, "osd_fopen: -romdir overrides '%s' by '%s'\n", gamename, alternate_name));
/*TODO*///        gamename = alternate_name;
/*TODO*///	}
/*TODO*///
/*TODO*///	switch( filetype )
/*TODO*///	{
/*TODO*///	case OSD_FILETYPE_ROM:
/*TODO*///	case OSD_FILETYPE_SAMPLE:
/*TODO*///
/*TODO*///		/* only for reading */
/*TODO*///		if( _write )
/*TODO*///		{
/*TODO*///			LOG((errorlog, "osd_fopen: OSD_FILETYPE_ROM/SAMPLE/ROM_CART write not supported\n"));
/*TODO*///            break;
/*TODO*///		}
/*TODO*///
/*TODO*///		if( filetype == OSD_FILETYPE_SAMPLE )
/*TODO*///		{
/*TODO*///			LOG((errorlog, "osd_fopen: using samplepath\n"));
/*TODO*///            pathc = samplepathc;
/*TODO*///            pathv = samplepathv;
/*TODO*///        }
/*TODO*///		else
/*TODO*///		{
/*TODO*///			LOG((errorlog, "osd_fopen: using rompath\n"));
/*TODO*///            pathc = rompathc;
/*TODO*///            pathv = rompathv;
/*TODO*///		}
/*TODO*///
/*TODO*///		for( indx = 0; indx < pathc && !found; ++indx )
/*TODO*///		{
/*TODO*///			const char *dir_name = pathv[indx];
/*TODO*///
/*TODO*///			if( !found )
/*TODO*///			{
/*TODO*///				sprintf (name, "%s/%s", dir_name, gamename);
/*TODO*///				LOG((errorlog, "Trying %s\n", name));
/*TODO*///                if( cache_stat (name, &stat_buffer) == 0 && (stat_buffer.st_mode & S_IFDIR) )
/*TODO*///				{
/*TODO*///					sprintf (name, "%s/%s/%s", dir_name, gamename, filename);
/*TODO*///					if( filetype == OSD_FILETYPE_ROM )
/*TODO*///					{
/*TODO*///						if( checksum_file (name, &f->data, &f->length, &f->crc) == 0 )
/*TODO*///						{
/*TODO*///							f->type = kRAMFile;
/*TODO*///							f->offset = 0;
/*TODO*///							found = 1;
/*TODO*///						}
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						f->type = kPlainFile;
/*TODO*///						f->file = fopen (name, "rb");
/*TODO*///						found = f->file != 0;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			if( !found )
/*TODO*///			{
/*TODO*///				/* try with a .zip extension */
/*TODO*///				sprintf (name, "%s/%s.zip", dir_name, gamename);
/*TODO*///				LOG((errorlog, "Trying %s file\n", name));
/*TODO*///                if( cache_stat (name, &stat_buffer) == 0 )
/*TODO*///				{
/*TODO*///					if( load_zipped_file (name, filename, &f->data, &f->length) == 0 )
/*TODO*///					{
/*TODO*///						LOG((errorlog, "Using (osd_fopen) zip file for %s\n", filename));
/*TODO*///						f->type = kZippedFile;
/*TODO*///						f->offset = 0;
/*TODO*///						f->crc = crc32 (0L, f->data, f->length);
/*TODO*///						found = 1;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			if( !found )
/*TODO*///			{
/*TODO*///				/* try with a .zip directory (if ZipMagic is installed) */
/*TODO*///				sprintf (name, "%s/%s.zip", dir_name, gamename);
/*TODO*///				LOG((errorlog, "Trying %s directory\n", name));
/*TODO*///                if( cache_stat (name, &stat_buffer) == 0 && (stat_buffer.st_mode & S_IFDIR) )
/*TODO*///				{
/*TODO*///					sprintf (name, "%s/%s.zip/%s", dir_name, gamename, filename);
/*TODO*///					if( filetype == OSD_FILETYPE_ROM )
/*TODO*///					{
/*TODO*///						if( checksum_file (name, &f->data, &f->length, &f->crc) == 0 )
/*TODO*///						{
/*TODO*///							f->type = kRAMFile;
/*TODO*///							f->offset = 0;
/*TODO*///							found = 1;
/*TODO*///						}
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						f->type = kPlainFile;
/*TODO*///						f->file = fopen (name, "rb");
/*TODO*///						found = f->file != 0;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		break;
/*TODO*///
/*TODO*///#ifdef MESS
/*TODO*///	case OSD_FILETYPE_IMAGE_R:
/*TODO*///
/*TODO*///		/* only for reading */
/*TODO*///		if( _write )
/*TODO*///		{
/*TODO*///			LOG((errorlog, "osd_fopen: OSD_FILETYPE_ROM/SAMPLE/ROM_CART write not supported\n"));
/*TODO*///            break;
/*TODO*///		}
/*TODO*///        else
/*TODO*///		{
/*TODO*///			LOG((errorlog, "osd_fopen: using rompath\n"));
/*TODO*///            pathc = rompathc;
/*TODO*///            pathv = rompathv;
/*TODO*///		}
/*TODO*///
/*TODO*///		LOG((errorlog, "Open IMAGE_R '%s' for %s\n", filename, game));
/*TODO*///        for( indx = 0; indx < pathc && !found; ++indx )
/*TODO*///		{
/*TODO*///			const char *dir_name = pathv[indx];
/*TODO*///
/*TODO*///			/* this section allows exact path from .cfg */
/*TODO*///			if( !found )
/*TODO*///			{
/*TODO*///				sprintf(name,"%s",dir_name);
/*TODO*///				if( cache_stat(name,&stat_buffer) == 0 && (stat_buffer.st_mode & S_IFDIR) )
/*TODO*///				{
/*TODO*///					sprintf(name,"%s/%s",dir_name,filename);
/*TODO*///					if( filetype == OSD_FILETYPE_ROM )
/*TODO*///					{
/*TODO*///						if( checksum_file (name, &f->data, &f->length, &f->crc) == 0 )
/*TODO*///						{
/*TODO*///							f->type = kRAMFile;
/*TODO*///							f->offset = 0;
/*TODO*///							found = 1;
/*TODO*///						}
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						f->type = kPlainFile;
/*TODO*///						f->file = fopen(name,"rb");
/*TODO*///						found = f->file!=0;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			if( !found )
/*TODO*///			{
/*TODO*///				sprintf (name, "%s/%s", dir_name, gamename);
/*TODO*///				LOG((errorlog, "Trying %s directory\n", name));
/*TODO*///                if( cache_stat (name, &stat_buffer) == 0 && (stat_buffer.st_mode & S_IFDIR) )
/*TODO*///				{
/*TODO*///					sprintf (name, "%s/%s/%s", dir_name, gamename, filename);
/*TODO*///					LOG((errorlog, "Trying %s file\n", name));
/*TODO*///                    if( filetype == OSD_FILETYPE_ROM )
/*TODO*///					{
/*TODO*///						if( checksum_file(name, &f->data, &f->length, &f->crc) == 0 )
/*TODO*///						{
/*TODO*///							f->type = kRAMFile;
/*TODO*///							f->offset = 0;
/*TODO*///							found = 1;
/*TODO*///						}
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						f->type = kPlainFile;
/*TODO*///						f->file = fopen (name, "rb");
/*TODO*///						found = f->file != 0;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			/* Zip cart support for MESS */
/*TODO*///			if( !found && filetype == OSD_FILETYPE_IMAGE_R )
/*TODO*///			{
/*TODO*///				char *extension = strrchr (name, '.');    /* find extension */
/*TODO*///				if( extension )
/*TODO*///					strcpy (extension, ".zip");
/*TODO*///				else
/*TODO*///					strcat (name, ".zip");
/*TODO*///				LOG((errorlog, "Trying %s file\n", name));
/*TODO*///				if( cache_stat(name, &stat_buffer) == 0 )
/*TODO*///				{
/*TODO*///					if( load_zipped_file(name, filename, &f->data, &f->length) == 0 )
/*TODO*///					{
/*TODO*///						LOG((errorlog, "Using (osd_fopen) zip file for %s\n", filename));
/*TODO*///						f->type = kZippedFile;
/*TODO*///						f->offset = 0;
/*TODO*///						f->crc = crc32 (0L, f->data, f->length);
/*TODO*///						found = 1;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			if( !found )
/*TODO*///			{
/*TODO*///				/* try with a .zip extension */
/*TODO*///				sprintf (name, "%s/%s.zip", dir_name, gamename);
/*TODO*///				LOG((errorlog, "Trying %s file\n", name));
/*TODO*///				if( cache_stat(name, &stat_buffer) == 0 )
/*TODO*///				{
/*TODO*///					if( load_zipped_file(name, filename, &f->data, &f->length) == 0 )
/*TODO*///					{
/*TODO*///						LOG((errorlog, "Using (osd_fopen) zip file for %s\n", filename));
/*TODO*///						f->type = kZippedFile;
/*TODO*///						f->offset = 0;
/*TODO*///						f->crc = crc32 (0L, f->data, f->length);
/*TODO*///						found = 1;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///    	}
/*TODO*///    break; /* end of IMAGE_R */
/*TODO*///
/*TODO*///	case OSD_FILETYPE_IMAGE_RW:
/*TODO*///		{
/*TODO*///			static char *write_modes[] = {"rb","wb","r+b","r+b","w+b"};
/*TODO*///            char file[256];
/*TODO*///			char *extension;
/*TODO*///
/*TODO*///			LOG((errorlog, "Open IMAGE_RW '%s' for %s mode '%s'\n", filename, game, write_modes[_write]));
/*TODO*///			strcpy (file, filename);
/*TODO*///
/*TODO*///			do
/*TODO*///			{
/*TODO*///				for( indx = 0; indx < rompathc && !found; ++indx )
/*TODO*///				{
/*TODO*///					const char *dir_name = rompathv[indx];
/*TODO*///
/*TODO*///					/* Exact path support */
/*TODO*///					if (!found)
/*TODO*///					{
/*TODO*///						sprintf(name, "%s", dir_name);
/*TODO*///						LOG((errorlog,"Trying %s directory\n", name));
/*TODO*///						if( cache_stat(name,&stat_buffer) == 0 && (stat_buffer.st_mode & S_IFDIR) )
/*TODO*///						{
/*TODO*///							sprintf(name,"%s/%s", dir_name, file);
/*TODO*///							LOG((errorlog, "Trying %s file\n", name));
/*TODO*///                            f->file = fopen(name, write_modes[_write]);
/*TODO*///							found = f->file != 0;
/*TODO*///							if( !found && _write == 3 )
/*TODO*///							{
/*TODO*///								f->file = fopen(name, write_modes[4]);
/*TODO*///								found = f->file != 0;
/*TODO*///                            }
/*TODO*///						}
/*TODO*///					}
/*TODO*///
/*TODO*///					if( !found )
/*TODO*///					{
/*TODO*///						sprintf (name, "%s/%s", dir_name, gamename);
/*TODO*///						LOG((errorlog, "Trying %s directory\n", name));
/*TODO*///						if( cache_stat(name, &stat_buffer) == 0 && (stat_buffer.st_mode & S_IFDIR) )
/*TODO*///						{
/*TODO*///							sprintf (name, "%s/%s/%s", dir_name, gamename, file);
/*TODO*///							LOG((errorlog, "Trying %s file\n", name));
/*TODO*///                            f->file = fopen (name, write_modes[_write]);
/*TODO*///							found = f->file != 0;
/*TODO*///							if( !found && _write == 3 )
/*TODO*///							{
/*TODO*///								f->file = fopen(name, write_modes[4]);
/*TODO*///								found = f->file != 0;
/*TODO*///                            }
/*TODO*///                        }
/*TODO*///					}
/*TODO*///
/*TODO*///                    if( !found && !_write )
/*TODO*///                    {
/*TODO*///                        extension = strrchr (name, '.');    /* find extension */
/*TODO*///                        /* add .zip for zipfile */
/*TODO*///                        if( extension )
/*TODO*///                            strcpy(extension, ".zip");
/*TODO*///                        else
/*TODO*///                            strcat(extension, ".zip");
/*TODO*///						LOG((errorlog, "Trying %s file\n", name));
/*TODO*///						if( cache_stat(name, &stat_buffer) == 0 )
/*TODO*///                        {
/*TODO*///							if( load_zipped_file(name, filename, &f->data, &f->length) == 0 )
/*TODO*///                            {
/*TODO*///                                LOG((errorlog, "Using (osd_fopen) zip file for %s\n", filename));
/*TODO*///                                f->type = kZippedFile;
/*TODO*///                                f->offset = 0;
/*TODO*///								f->crc = crc32(0L, f->data, f->length);
/*TODO*///                                found = 1;
/*TODO*///                            }
/*TODO*///                        }
/*TODO*///                    }
/*TODO*///
/*TODO*///					if( !found && !_write )
/*TODO*///					{
/*TODO*///						/* try with a .zip extension */
/*TODO*///						sprintf (name, "%s/%s.zip", dir_name, gamename);
/*TODO*///						LOG((errorlog, "Trying %s file\n", name));
/*TODO*///						if( cache_stat (name, &stat_buffer) == 0 )
/*TODO*///						{
/*TODO*///							if( load_zipped_file (name, file, &f->data, &f->length) == 0 )
/*TODO*///							{
/*TODO*///								LOG((errorlog, "Using (osd_fopen) zip file for %s\n", filename));
/*TODO*///								f->type = kZippedFile;
/*TODO*///								f->offset = 0;
/*TODO*///								f->crc = crc32 (0L, f->data, f->length);
/*TODO*///								found = 1;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///
/*TODO*///					if( !found )
/*TODO*///					{
/*TODO*///						/* try with a .zip directory (if ZipMagic is installed) */
/*TODO*///						sprintf (name, "%s/%s.zip", dir_name, gamename);
/*TODO*///						LOG((errorlog, "Trying %s ZipMagic directory\n", name));
/*TODO*///						if( cache_stat (name, &stat_buffer) == 0 && (stat_buffer.st_mode & S_IFDIR) )
/*TODO*///						{
/*TODO*///							sprintf (name, "%s/%s.zip/%s", dir_name, gamename, file);
/*TODO*///							LOG((errorlog, "Trying %s\n", name));
/*TODO*///							f->file = fopen (name, write_modes[_write]);
/*TODO*///							found = f->file != 0;
/*TODO*///							if( !found && _write == 3 )
/*TODO*///							{
/*TODO*///								f->file = fopen(name, write_modes[4]);
/*TODO*///								found = f->file != 0;
/*TODO*///                            }
/*TODO*///                        }
/*TODO*///					}
/*TODO*///					if( found )
/*TODO*///						LOG((errorlog, "IMAGE_RW %s FOUND in %s!\n", file, name));
/*TODO*///				}
/*TODO*///
/*TODO*///				extension = strrchr (file, '.');
/*TODO*///				if( extension )
/*TODO*///					*extension = '\0';
/*TODO*///			} while( !found && extension );
/*TODO*///		}
/*TODO*///		break;
/*TODO*///#endif	/* MESS */
/*TODO*///
/*TODO*///
/*TODO*///	case OSD_FILETYPE_NVRAM:
/*TODO*///		if( !found )
/*TODO*///		{
/*TODO*///			sprintf (name, "%s/%s.nv", nvdir, gamename);
/*TODO*///			f->type = kPlainFile;
/*TODO*///			f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///			found = f->file != 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		if( !found )
/*TODO*///		{
/*TODO*///			/* try with a .zip directory (if ZipMagic is installed) */
/*TODO*///			sprintf (name, "%s.zip/%s.nv", nvdir, gamename);
/*TODO*///			f->type = kPlainFile;
/*TODO*///			f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///			found = f->file != 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		if( !found )
/*TODO*///		{
/*TODO*///			/* try with a .zif directory (if ZipFolders is installed) */
/*TODO*///			sprintf (name, "%s.zif/%s.nv", nvdir, gamename);
/*TODO*///			f->type = kPlainFile;
/*TODO*///			f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///			found = f->file != 0;
/*TODO*///		}
/*TODO*///		break;
/*TODO*///
/*TODO*///	case OSD_FILETYPE_HIGHSCORE:
/*TODO*///		if( mame_highscore_enabled () )
/*TODO*///		{
/*TODO*///			if( !found )
/*TODO*///			{
/*TODO*///				sprintf (name, "%s/%s.hi", hidir, gamename);
/*TODO*///				f->type = kPlainFile;
/*TODO*///				f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///				found = f->file != 0;
/*TODO*///			}
/*TODO*///
/*TODO*///			if( !found )
/*TODO*///			{
/*TODO*///				/* try with a .zip directory (if ZipMagic is installed) */
/*TODO*///				sprintf (name, "%s.zip/%s.hi", hidir, gamename);
/*TODO*///				f->type = kPlainFile;
/*TODO*///				f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///				found = f->file != 0;
/*TODO*///			}
/*TODO*///
/*TODO*///			if( !found )
/*TODO*///			{
/*TODO*///				/* try with a .zif directory (if ZipFolders is installed) */
/*TODO*///				sprintf (name, "%s.zif/%s.hi", hidir, gamename);
/*TODO*///				f->type = kPlainFile;
/*TODO*///				f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///				found = f->file != 0;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		break;
/*TODO*///
/*TODO*///    case OSD_FILETYPE_CONFIG:
/*TODO*///		sprintf (name, "%s/%s.cfg", cfgdir, gamename);
/*TODO*///		f->type = kPlainFile;
/*TODO*///		f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///		found = f->file != 0;
/*TODO*///
/*TODO*///		if( !found )
/*TODO*///		{
/*TODO*///			/* try with a .zip directory (if ZipMagic is installed) */
/*TODO*///			sprintf (name, "%s.zip/%s.cfg", cfgdir, gamename);
/*TODO*///			f->type = kPlainFile;
/*TODO*///			f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///			found = f->file != 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		if( !found )
/*TODO*///		{
/*TODO*///			/* try with a .zif directory (if ZipFolders is installed) */
/*TODO*///			sprintf (name, "%s.zif/%s.cfg", cfgdir, gamename);
/*TODO*///			f->type = kPlainFile;
/*TODO*///			f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///			found = f->file != 0;
/*TODO*///		}
/*TODO*///		break;
/*TODO*///
/*TODO*///	case OSD_FILETYPE_INPUTLOG:
/*TODO*///		sprintf (name, "%s/%s.inp", inpdir, gamename);
/*TODO*///		f->type = kPlainFile;
/*TODO*///		f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///		found = f->file != 0;
/*TODO*///
/*TODO*///        if( !found )
/*TODO*///		{
/*TODO*///			/* try with a .zip directory (if ZipMagic is installed) */
/*TODO*///			sprintf (name, "%s.zip/%s.cfg", inpdir, gamename);
/*TODO*///			f->type = kPlainFile;
/*TODO*///			f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///			found = f->file != 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		if( !found )
/*TODO*///		{
/*TODO*///			/* try with a .zif directory (if ZipFolders is installed) */
/*TODO*///			sprintf (name, "%s.zif/%s.cfg", inpdir, gamename);
/*TODO*///			f->type = kPlainFile;
/*TODO*///			f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///			found = f->file != 0;
/*TODO*///        }
/*TODO*///
/*TODO*///		if( !_write )
/*TODO*///		{
/*TODO*///			char file[256];
/*TODO*///			sprintf (file, "%s.inp", gamename);
/*TODO*///            sprintf (name, "%s/%s.zip", inpdir, gamename);
/*TODO*///			LOG((errorlog, "Trying %s in %s\n", file, name));
/*TODO*///            if( cache_stat (name, &stat_buffer) == 0 )
/*TODO*///			{
/*TODO*///				if( load_zipped_file (name, file, &f->data, &f->length) == 0 )
/*TODO*///				{
/*TODO*///					LOG((errorlog, "Using (osd_fopen) zip file %s for %s\n", name, file));
/*TODO*///					f->type = kZippedFile;
/*TODO*///					f->offset = 0;
/*TODO*///					found = 1;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///        break;
/*TODO*///
/*TODO*///	case OSD_FILETYPE_STATE:
/*TODO*///		sprintf (name, "%s/%s.sta", stadir, gamename);
/*TODO*///		f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///		found = !(f->file == 0);
/*TODO*///		if( !found )
/*TODO*///		{
/*TODO*///			/* try with a .zip directory (if ZipMagic is installed) */
/*TODO*///			sprintf (name, "%s.zip/%s.sta", stadir, gamename);
/*TODO*///			f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///			found = !(f->file == 0);
/*TODO*///		}
/*TODO*///		if( !found )
/*TODO*///		{
/*TODO*///			/* try with a .zif directory (if ZipFolders is installed) */
/*TODO*///			sprintf (name, "%s.zif/%s.sta", stadir, gamename);
/*TODO*///			f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///			found = !(f->file == 0);
/*TODO*///		}
/*TODO*///		break;
/*TODO*///
/*TODO*///	case OSD_FILETYPE_ARTWORK:
/*TODO*///		/* only for reading */
/*TODO*///		if( _write )
/*TODO*///		{
/*TODO*///			LOG((errorlog, "osd_fopen: OSD_FILETYPE_ARTWORK write not supported\n"));
/*TODO*///            break;
/*TODO*///		}
/*TODO*///		sprintf (name, "%s/%s", artworkdir, filename);
/*TODO*///		f->type = kPlainFile;
/*TODO*///		f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///		found = f->file != 0;
/*TODO*///		if( !found )
/*TODO*///		{
/*TODO*///			/* try with a .zip directory (if ZipMagic is installed) */
/*TODO*///			sprintf (name, "%s.zip/%s.png", artworkdir, filename);
/*TODO*///			f->type = kPlainFile;
/*TODO*///			f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///			found = f->file != 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		if( !found )
/*TODO*///		{
/*TODO*///			/* try with a .zif directory (if ZipFolders is installed) */
/*TODO*///			sprintf (name, "%s.zif/%s.png", artworkdir, filename);
/*TODO*///			f->type = kPlainFile;
/*TODO*///			f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///			found = f->file != 0;
/*TODO*///        }
/*TODO*///
/*TODO*///		if( !found )
/*TODO*///		{
/*TODO*///			char file[256], *extension;
/*TODO*///			sprintf(file, "%s", filename);
/*TODO*///            sprintf(name, "%s/%s", artworkdir, filename);
/*TODO*///            extension = strrchr(name, '.');
/*TODO*///			if( extension )
/*TODO*///				strcpy (extension, ".zip");
/*TODO*///			else
/*TODO*///				strcat (name, ".zip");
/*TODO*///			LOG((errorlog, "Trying %s in %s\n", file, name));
/*TODO*///            if( cache_stat (name, &stat_buffer) == 0 )
/*TODO*///			{
/*TODO*///				if( load_zipped_file (name, file, &f->data, &f->length) == 0 )
/*TODO*///				{
/*TODO*///					LOG((errorlog, "Using (osd_fopen) zip file %s\n", name));
/*TODO*///					f->type = kZippedFile;
/*TODO*///					f->offset = 0;
/*TODO*///					found = 1;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			if( !found )
/*TODO*///			{
/*TODO*///				sprintf(name, "%s/%s.zip", artworkdir, game);
/*TODO*///				LOG((errorlog, "Trying %s in %s\n", file, name));
/*TODO*///				if( cache_stat (name, &stat_buffer) == 0 )
/*TODO*///				{
/*TODO*///					if( load_zipped_file (name, file, &f->data, &f->length) == 0 )
/*TODO*///					{
/*TODO*///						LOG((errorlog, "Using (osd_fopen) zip file %s\n", name));
/*TODO*///						f->type = kZippedFile;
/*TODO*///						f->offset = 0;
/*TODO*///						found = 1;
/*TODO*///					}
/*TODO*///				}
/*TODO*///            }
/*TODO*///        }
/*TODO*///        break;
/*TODO*///
/*TODO*///	case OSD_FILETYPE_MEMCARD:
/*TODO*///		sprintf (name, "%s/%s", memcarddir, filename);
/*TODO*///		f->type = kPlainFile;
/*TODO*///		f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///		found = f->file != 0;
/*TODO*///		break;
/*TODO*///
/*TODO*///	case OSD_FILETYPE_SCREENSHOT:
/*TODO*///		/* only for writing */
/*TODO*///		if( !_write )
/*TODO*///		{
/*TODO*///			LOG((errorlog, "osd_fopen: OSD_FILETYPE_SCREENSHOT read not supported\n"));
/*TODO*///			break;
/*TODO*///		}
/*TODO*///
/*TODO*///		sprintf (name, "%s/%s.png", screenshotdir, filename);
/*TODO*///		f->type = kPlainFile;
/*TODO*///		f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///		found = f->file != 0;
/*TODO*///		break;
/*TODO*///	}
/*TODO*///
/*TODO*///	if( !found )
/*TODO*///	{
/*TODO*///		free (f);
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	return f;
/*TODO*///}
/*TODO*///
/*TODO*////* JB 980920 update */
/*TODO*///int osd_fread (void *file, void *buffer, int length)
/*TODO*///{
/*TODO*///	FakeFileHandle *f = (FakeFileHandle *) file;
/*TODO*///
/*TODO*///	switch( f->type )
/*TODO*///	{
/*TODO*///	case kPlainFile:
/*TODO*///		return fread (buffer, 1, length, f->file);
/*TODO*///		break;
/*TODO*///	case kZippedFile:
/*TODO*///	case kRAMFile:
/*TODO*///		/* reading from the RAM image of a file */
/*TODO*///		if( f->data )
/*TODO*///		{
/*TODO*///			if( length + f->offset > f->length )
/*TODO*///				length = f->length - f->offset;
/*TODO*///			memcpy (buffer, f->offset + f->data, length);
/*TODO*///			f->offset += length;
/*TODO*///			return length;
/*TODO*///		}
/*TODO*///		break;
/*TODO*///	}
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///int osd_fread_swap (void *file, void *buffer, int length)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	unsigned char *buf;
/*TODO*///	unsigned char temp;
/*TODO*///	int res;
/*TODO*///
/*TODO*///
/*TODO*///	res = osd_fread (file, buffer, length);
/*TODO*///
/*TODO*///	buf = buffer;
/*TODO*///	for( i = 0; i < length; i += 2 )
/*TODO*///	{
/*TODO*///		temp = buf[i];
/*TODO*///		buf[i] = buf[i + 1];
/*TODO*///		buf[i + 1] = temp;
/*TODO*///	}
/*TODO*///
/*TODO*///	return res;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////* AM 980919 update */
/*TODO*///int osd_fwrite (void *file, const void *buffer, int length)
/*TODO*///{
/*TODO*///	FakeFileHandle *f = (FakeFileHandle *) file;
/*TODO*///
/*TODO*///	switch( f->type )
/*TODO*///	{
/*TODO*///	case kPlainFile:
/*TODO*///		return fwrite (buffer, 1, length, ((FakeFileHandle *) file)->file);
/*TODO*///	default:
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///int osd_fwrite_swap (void *file, const void *buffer, int length)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	unsigned char *buf;
/*TODO*///	unsigned char temp;
/*TODO*///	int res;
/*TODO*///
/*TODO*///
/*TODO*///	buf = (unsigned char *) buffer;
/*TODO*///	for( i = 0; i < length; i += 2 )
/*TODO*///	{
/*TODO*///		temp = buf[i];
/*TODO*///		buf[i] = buf[i + 1];
/*TODO*///		buf[i + 1] = temp;
/*TODO*///	}
/*TODO*///
/*TODO*///	res = osd_fwrite (file, buffer, length);
/*TODO*///
/*TODO*///	for( i = 0; i < length; i += 2 )
/*TODO*///	{
/*TODO*///		temp = buf[i];
/*TODO*///		buf[i] = buf[i + 1];
/*TODO*///		buf[i + 1] = temp;
/*TODO*///	}
/*TODO*///
/*TODO*///	return res;
/*TODO*///}
/*TODO*///
/*TODO*///int osd_fread_scatter (void *file, void *buffer, int length, int increment)
/*TODO*///{
/*TODO*///	unsigned char *buf = buffer;
/*TODO*///	FakeFileHandle *f = (FakeFileHandle *) file;
/*TODO*///	unsigned char tempbuf[4096];
/*TODO*///	int totread, r, i;
/*TODO*///
/*TODO*///	switch( f->type )
/*TODO*///	{
/*TODO*///	case kPlainFile:
/*TODO*///		totread = 0;
/*TODO*///		while (length)
/*TODO*///		{
/*TODO*///			r = length;
/*TODO*///			if( r > 4096 )
/*TODO*///				r = 4096;
/*TODO*///			r = fread (tempbuf, 1, r, f->file);
/*TODO*///			if( r == 0 )
/*TODO*///				return totread;		   /* error */
/*TODO*///			for( i = 0; i < r; i++ )
/*TODO*///			{
/*TODO*///				*buf = tempbuf[i];
/*TODO*///				buf += increment;
/*TODO*///			}
/*TODO*///			totread += r;
/*TODO*///			length -= r;
/*TODO*///		}
/*TODO*///		return totread;
/*TODO*///		break;
/*TODO*///	case kZippedFile:
/*TODO*///	case kRAMFile:
/*TODO*///		/* reading from the RAM image of a file */
/*TODO*///		if( f->data )
/*TODO*///		{
/*TODO*///			if( length + f->offset > f->length )
/*TODO*///				length = f->length - f->offset;
/*TODO*///			for( i = 0; i < length; i++ )
/*TODO*///			{
/*TODO*///				*buf = f->data[f->offset + i];
/*TODO*///				buf += increment;
/*TODO*///			}
/*TODO*///			f->offset += length;
/*TODO*///			return length;
/*TODO*///		}
/*TODO*///		break;
/*TODO*///	}
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////* JB 980920 update */
/*TODO*///int osd_fseek (void *file, int offset, int whence)
/*TODO*///{
/*TODO*///	FakeFileHandle *f = (FakeFileHandle *) file;
/*TODO*///	int err = 0;
/*TODO*///
/*TODO*///	switch( f->type )
/*TODO*///	{
/*TODO*///	case kPlainFile:
/*TODO*///		return fseek (f->file, offset, whence);
/*TODO*///		break;
/*TODO*///	case kZippedFile:
/*TODO*///	case kRAMFile:
/*TODO*///		/* seeking within the RAM image of a file */
/*TODO*///		switch( whence )
/*TODO*///		{
/*TODO*///		case SEEK_SET:
/*TODO*///			f->offset = offset;
/*TODO*///			break;
/*TODO*///		case SEEK_CUR:
/*TODO*///			f->offset += offset;
/*TODO*///			break;
/*TODO*///		case SEEK_END:
/*TODO*///			f->offset = f->length + offset;
/*TODO*///			break;
/*TODO*///		}
/*TODO*///		break;
/*TODO*///	}
/*TODO*///
/*TODO*///	return err;
/*TODO*///}
/*TODO*///
/*TODO*////* JB 980920 update */
/*TODO*///void osd_fclose (void *file)
/*TODO*///{
/*TODO*///	FakeFileHandle *f = (FakeFileHandle *) file;
/*TODO*///
/*TODO*///	switch( f->type )
/*TODO*///	{
/*TODO*///	case kPlainFile:
/*TODO*///		fclose (f->file);
/*TODO*///		break;
/*TODO*///	case kZippedFile:
/*TODO*///	case kRAMFile:
/*TODO*///		if( f->data )
/*TODO*///			free (f->data);
/*TODO*///		break;
/*TODO*///	}
/*TODO*///	free (f);
/*TODO*///}
/*TODO*///
/*TODO*////* JB 980920 update */
/*TODO*////* AM 980919 */
/*TODO*///static int checksum_file (const char *file, unsigned char **p, unsigned int *size, unsigned int *crc)
/*TODO*///{
/*TODO*///	int length;
/*TODO*///	unsigned char *data;
/*TODO*///	FILE *f;
/*TODO*///
/*TODO*///	f = fopen (file, "rb");
/*TODO*///	if( !f )
/*TODO*///		return -1;
/*TODO*///
/*TODO*///	/* determine length of file */
/*TODO*///	if( fseek (f, 0L, SEEK_END) != 0 )
/*TODO*///	{
/*TODO*///		fclose (f);
/*TODO*///		return -1;
/*TODO*///	}
/*TODO*///
/*TODO*///	length = ftell (f);
/*TODO*///	if( length == -1L )
/*TODO*///	{
/*TODO*///		fclose (f);
/*TODO*///		return -1;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* allocate space for entire file */
/*TODO*///	data = (unsigned char *) malloc (length);
/*TODO*///	if( !data )
/*TODO*///	{
/*TODO*///		fclose (f);
/*TODO*///		return -1;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* read entire file into memory */
/*TODO*///	if( fseek (f, 0L, SEEK_SET) != 0 )
/*TODO*///	{
/*TODO*///		free (data);
/*TODO*///		fclose (f);
/*TODO*///		return -1;
/*TODO*///	}
/*TODO*///
/*TODO*///	if( fread (data, sizeof (unsigned char), length, f) != length )
/*TODO*///	{
/*TODO*///		free (data);
/*TODO*///		fclose (f);
/*TODO*///		return -1;
/*TODO*///	}
/*TODO*///
/*TODO*///	*size = length;
/*TODO*///	*crc = crc32 (0L, data, length);
/*TODO*///	if( p )
/*TODO*///		*p = data;
/*TODO*///	else
/*TODO*///		free (data);
/*TODO*///
/*TODO*///	fclose (f);
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////* JB 980920 updated */
/*TODO*////* AM 980919 updated */
/*TODO*///int osd_fchecksum (const char *game, const char *filename, unsigned int *length, unsigned int *sum)
/*TODO*///{
/*TODO*///	char name[256];
/*TODO*///	int indx;
/*TODO*///	struct stat stat_buffer;
/*TODO*///	int found = 0;
/*TODO*///	const char *gamename = game;
/*TODO*///
/*TODO*///	/* Support "-romdir" yuck. */
/*TODO*///	if( alternate_name )
/*TODO*///		gamename = alternate_name;
/*TODO*///
/*TODO*///	for( indx = 0; indx < rompathc && !found; ++indx )
/*TODO*///	{
/*TODO*///		const char *dir_name = rompathv[indx];
/*TODO*///
/*TODO*///		if( !found )
/*TODO*///		{
/*TODO*///			sprintf (name, "%s/%s", dir_name, gamename);
/*TODO*///			if( cache_stat (name, &stat_buffer) == 0 && (stat_buffer.st_mode & S_IFDIR) )
/*TODO*///			{
/*TODO*///				sprintf (name, "%s/%s/%s", dir_name, gamename, filename);
/*TODO*///				if( checksum_file (name, 0, length, sum) == 0 )
/*TODO*///                {
/*TODO*///					found = 1;
/*TODO*///                }
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		if( !found )
/*TODO*///		{
/*TODO*///			/* try with a .zip extension */
/*TODO*///			sprintf (name, "%s/%s.zip", dir_name, gamename);
/*TODO*///			if( cache_stat (name, &stat_buffer) == 0 )
/*TODO*///			{
/*TODO*///				if( checksum_zipped_file (name, filename, length, sum) == 0 )
/*TODO*///				{
/*TODO*///					LOG((errorlog, "Using (osd_fchecksum) zip file for %s\n", filename));
/*TODO*///					found = 1;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		if( !found )
/*TODO*///		{
/*TODO*///			/* try with a .zif directory (if ZipFolders is installed) */
/*TODO*///			sprintf (name, "%s/%s.zif", dir_name, gamename);
/*TODO*///			if( cache_stat (name, &stat_buffer) == 0 )
/*TODO*///			{
/*TODO*///				sprintf (name, "%s/%s.zif/%s", dir_name, gamename, filename);
/*TODO*///				if( checksum_file (name, 0, length, sum) == 0 )
/*TODO*///				{
/*TODO*///					found = 1;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	if( !found )
/*TODO*///		return -1;
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////* JB 980920 */
/*TODO*///int osd_fsize (void *file)
/*TODO*///{
/*TODO*///	FakeFileHandle *f = (FakeFileHandle *) file;
/*TODO*///
/*TODO*///	if( f->type == kRAMFile || f->type == kZippedFile )
/*TODO*///		return f->length;
/*TODO*///
/*TODO*///	if( f->file )
/*TODO*///	{
/*TODO*///		int size, offs;
/*TODO*///		offs = ftell( f->file );
/*TODO*///		fseek( f->file, 0, SEEK_END );
/*TODO*///		size = ftell( f->file );
/*TODO*///		fseek( f->file, offs, SEEK_SET );
/*TODO*///		return size;
/*TODO*///	}
/*TODO*///
/*TODO*///    return 0;
/*TODO*///}
/*TODO*///
/*TODO*////* JB 980920 */
/*TODO*///unsigned int osd_fcrc (void *file)
/*TODO*///{
/*TODO*///	FakeFileHandle *f = (FakeFileHandle *) file;
/*TODO*///
/*TODO*///	return f->crc;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////* called while loading ROMs. It is called a last time with name == 0 to signal */
/*TODO*////* that the ROM loading process is finished. */
/*TODO*////* return non-zero to abort loading */
/*TODO*///int osd_display_loading_rom_message (const char *name, int current, int total)
/*TODO*///{
/*TODO*///	if( name )
/*TODO*///		fprintf (stdout, "loading %-12s\r", name);
/*TODO*///	else
/*TODO*///		fprintf (stdout, "                    \r");
/*TODO*///	fflush (stdout);
/*TODO*///
/*TODO*///	if( keyboard_pressed (KEYCODE_LCONTROL) && keyboard_pressed (KEYCODE_C) )
/*TODO*///		return 1;
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///#ifdef MESS
/*TODO*////* Function to handle aliases in the MESS.CFG file */
/*TODO*///char *get_alias(const char *driver_name, char *alias)
/*TODO*///{
/*TODO*///	char driver[8+1];
/*TODO*///	/* Allegro's get_config_string() first argument is not a 'const char*' */
/*TODO*///    strcpy(driver, driver_name);
/*TODO*///	return get_config_string(driver,alias,"");
/*TODO*///}
/*TODO*///#endif
/*TODO*///    
}
