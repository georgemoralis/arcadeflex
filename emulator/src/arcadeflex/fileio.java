package arcadeflex;

import static arcadeflex.libc_old.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;
import static mame.osdependH.*;
import static mame.mame.*;

public class fileio {
    
/*TODO*/ //    char *roms = NULL;
/*TODO*/ //    char **rompathv = NULL;
/*TODO*/ //    int rompathc = 0;

/*TODO*/ //    char *samples = NULL;
/*TODO*/ //    char **samplepathv = NULL;
/*TODO*/ //    int samplepathc = 0;

/*TODO*/ //    char *cfgdir, *nvdir, *hidir, *inpdir, *stadir;
 /*TODO*/ //   char *memcarddir, *artworkdir, *screenshotdir;

 /*TODO*/ //     char *alternate_name;				   /* for "-romdir" */


    public static final int kPlainFile=1;
    public static final int kRAMFile=2;
    public static final int kZippedFile=3;
    
    static class FakeFileHandle
    {
            public FILE file;
            public char[] data = new char[1];
            public int offset;
            public int length;
            public int type;
            public int crc;
    }
 /*TODO*/ //     typedef struct
 /*TODO*/ //     {
 /*TODO*/ //             FILE *file;
  /*TODO*/ //            unsigned char *data;
  /*TODO*/ //            unsigned int offset;
  /*TODO*/ //            unsigned int length;
  /*TODO*/ //            eFileType type;
  /*TODO*/ //            unsigned int crc;
  /*TODO*/ //    }	FakeFileHandle;
    public static Object osd_fopen (String game, String filename, int filetype, int _write)
    {
        String name="";
	String gamename;
	int found = 0;
	int indx;
	FakeFileHandle f;
	int pathc=0;
	String[] pathv=null;


	f = new FakeFileHandle();
	if( f==null )
	{
		//LOG((errorlog, "osd_fopen: failed to malloc FakeFileHandle!\n"));
            return null;
	}
        gamename = game;

	/* Support "-romdir" yuck. */
/*TODO*///	if( alternate_name )
/*TODO*///	{
/*TODO*///		LOG((errorlog, "osd_fopen: -romdir overrides '%s' by '%s'\n", gamename, alternate_name));
 /*TODO*///       gamename = alternate_name;
/*TODO*///	}
	switch( filetype )
	{
            case OSD_FILETYPE_ROM:
            case OSD_FILETYPE_SAMPLE:

                    /* only for reading */
                    if( _write!=0 )
                    {
                        fprintf(errorlog, "osd_fopen: OSD_FILETYPE_ROM/SAMPLE/ROM_CART write not supported\n");
                        break;
                    }

                    if( filetype == OSD_FILETYPE_SAMPLE )
                    {
 /*TODO*///                        LOG((errorlog, "osd_fopen: using samplepath\n"));
 /*TODO*///                        pathc = samplepathc;
  /*TODO*///                       pathv = samplepathv;
                    }
                    else
                    {
  /*TODO*///                       LOG((errorlog, "osd_fopen: using rompath\n"));
  /*TODO*///                       pathc = rompathc;
  /*TODO*///                       pathv = rompathv;
  /*TEMPHACK*/ //just added manually roms directory since else we need to implement conf class (in future)                      
                                   pathc = 1;
                                   pathv = new String[1];
                                   pathv[0] = "roms";
                    }

                    for( indx = 0; indx < pathc && found==0; ++indx )
                    {
                          String dir_name = pathv[indx];  
                          if( found==0 )
                          {
                                    name= sprintf ("%s/%s", dir_name, gamename);
                                    fprintf(errorlog, "Trying %s\n", name);
                                    //java code to emulate stat command (shadow)
                                    if(new File(name).isDirectory() && new File(name).exists()) // if( cache_stat (name, &stat_buffer) == 0 && (stat_buffer.st_mode & S_IFDIR) )               
                                    {
                                            name = sprintf ("%s/%s/%s", dir_name, gamename, filename);
                                            //System.out.println(name);
                                            if(new File(name).exists())
                                            {
                                                if( filetype == OSD_FILETYPE_ROM )
                                                {
                                                    //java issue since there is no way to pass by reference the data table 
                                                    //get it here
                                                    f.file = fopen(name,"rb");
                                                    long size = ftell(f.file);
                                                    f.data= new char[(int)size];
                                                    fclose(f.file);
                                                    // http://www.java-tips.org/java-se-tips/java.lang/pass-an-integer-by-reference.html
                                                    int tlen[] = new int[1];
                                                    int tcrc[] = new int[1];
                                                    if( checksum_file (name, f.data, tlen, tcrc) == 0 )
                                                    {
                                                        f.type = kRAMFile;
                                                        f.offset = 0;
                                                        found = 1;
                                                    }
                                                    //copy values where they belong
                                                    f.length=tlen[0];
                                                    f.crc=tcrc[0];
                                               }
                                               else
                                               {
                                                       f.type = kPlainFile;
                                                       f.file = fopen (name, "rb");
                                                       found = (f.file != null)? 1 :0; //found = f.file !=0;
                                               }
                                            }
                                 }
                            }

 /*TODO*///                           if( !found )
/*TODO*///                            {
 /*TODO*///                                   /* try with a .zip extension */
/*TODO*///                                    sprintf (name, "%s/%s.zip", dir_name, gamename);
/*TODO*///                                    LOG((errorlog, "Trying %s file\n", name));
/*TODO*///                    if( cache_stat (name, &stat_buffer) == 0 )
/*TODO*///                                    {
/*TODO*///                                            if( load_zipped_file (name, filename, &f->data, &f->length) == 0 )
/*TODO*///                                            {
/*TODO*///                                                    LOG((errorlog, "Using (osd_fopen) zip file for %s\n", filename));
/*TODO*///                                                    f->type = kZippedFile;
/*TODO*///                                                    f->offset = 0;
/*TODO*///                                                    f->crc = crc32 (0L, f->data, f->length);
/*TODO*///                                                    found = 1;
/*TODO*///                                            }
/*TODO*///                                    }
/*TODO*///                            }
                    }

                    break;

/*TODO*///            case OSD_FILETYPE_NVRAM:
/*TODO*///                    if( !found )
/*TODO*///                    {
/*TODO*///                            sprintf (name, "%s/%s.nv", nvdir, gamename);
/*TODO*///                            f->type = kPlainFile;
/*TODO*///                            f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///                            found = f->file != 0;
/*TODO*///                    }
/*TODO*///                    break;

 /*TODO*///           case OSD_FILETYPE_HIGHSCORE:
 /*TODO*///                   if( mame_highscore_enabled () )
 /*TODO*///                   {
 /*TODO*///                           if( !found )
 /*TODO*///                           {
 /*TODO*///                                   sprintf (name, "%s/%s.hi", hidir, gamename);
 /*TODO*///                                   f->type = kPlainFile;
 /*TODO*///                                   f->file = fopen (name, _write ? "wb" : "rb");
 /*TODO*///                                   found = f->file != 0;
 /*TODO*///                           }                      
 /*TODO*///                   }
 /*TODO*///                   break;

 /*TODO*///       case OSD_FILETYPE_CONFIG:
 /*TODO*///                   sprintf (name, "%s/%s.cfg", cfgdir, gamename);
 /*TODO*///                   f->type = kPlainFile;
 /*TODO*///                   f->file = fopen (name, _write ? "wb" : "rb");
 /*TODO*///                   found = f->file != 0;
 /*TODO*///                   break;

/*TODO*///            case OSD_FILETYPE_INPUTLOG:
/*TODO*///                    sprintf (name, "%s/%s.inp", inpdir, gamename);
/*TODO*///                    f->type = kPlainFile;
/*TODO*///                    f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///                    found = f->file != 0;
/*TODO*///                    if( !_write )
/*TODO*///                    {
/*TODO*///                            char file[256];
/*TODO*///                            sprintf (file, "%s.inp", gamename);
/*TODO*///                sprintf (name, "%s/%s.zip", inpdir, gamename);
/*TODO*///                            LOG((errorlog, "Trying %s in %s\n", file, name));
/*TODO*///                if( cache_stat (name, &stat_buffer) == 0 )
/*TODO*///                            {
/*TODO*///                                    if( load_zipped_file (name, file, &f->data, &f->length) == 0 )
/*TODO*///                                    {
/*TODO*///                                            LOG((errorlog, "Using (osd_fopen) zip file %s for %s\n", name, file));
/*TODO*///                                            f->type = kZippedFile;
/*TODO*///                                            f->offset = 0;
/*TODO*///                                            found = 1;
/*TODO*///                                    }
/*TODO*///                            }
/*TODO*///                    }

/*TODO*///            break;

/*TODO*///            case OSD_FILETYPE_STATE:
/*TODO*///                    sprintf (name, "%s/%s.sta", stadir, gamename);
/*TODO*///                    f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///                    found = !(f->file == 0);
/*TODO*///                    if( !found )
/*TODO*///                    {
/*TODO*///                            /* try with a .zip directory (if ZipMagic is installed) */
/*TODO*///                            sprintf (name, "%s.zip/%s.sta", stadir, gamename);
/*TODO*///                            f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///                            found = !(f->file == 0);
/*TODO*///                    }
/*TODO*///                    if( !found )
/*TODO*///                    {
/*TODO*///                            /* try with a .zif directory (if ZipFolders is installed) */
/*TODO*///                            sprintf (name, "%s.zif/%s.sta", stadir, gamename);
/*TODO*///                            f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///                            found = !(f->file == 0);
/*TODO*///                    }
/*TODO*///                    break;

/*TODO*///            case OSD_FILETYPE_ARTWORK:
                    /* only for reading */
/*TODO*///                    if( _write )
/*TODO*///                    {
/*TODO*///                            LOG((errorlog, "osd_fopen: OSD_FILETYPE_ARTWORK write not supported\n"));
/*TODO*///                break;
/*TODO*///                    }
/*TODO*///                    sprintf (name, "%s/%s", artworkdir, filename);
/*TODO*///                    f->type = kPlainFile;
/*TODO*///                    f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///                    found = f->file != 0;
 /*TODO*///                   if( !found )
/*TODO*///                    {
/*TODO*///                            char file[256], *extension;
 /*TODO*///                           sprintf(file, "%s", filename);
 /*TODO*///               sprintf(name, "%s/%s", artworkdir, filename);
/*TODO*///                extension = strrchr(name, '.');
/*TODO*///                            if( extension )
/*TODO*///                                    strcpy (extension, ".zip");
/*TODO*///                            else
/*TODO*///                                    strcat (name, ".zip");
/*TODO*///                            LOG((errorlog, "Trying %s in %s\n", file, name));
/*TODO*///                if( cache_stat (name, &stat_buffer) == 0 )
/*TODO*///                            {
/*TODO*///                                    if( load_zipped_file (name, file, &f->data, &f->length) == 0 )
/*TODO*///                                    {
/*TODO*///                                            LOG((errorlog, "Using (osd_fopen) zip file %s\n", name));
 /*TODO*///                                           f->type = kZippedFile;
 /*TODO*///                                           f->offset = 0;
/*TODO*///                                            found = 1;
/*TODO*///                                    }
 /*TODO*///                           }
/*TODO*///                            if( !found )
/*TODO*///                            {
/*TODO*///                                    sprintf(name, "%s/%s.zip", artworkdir, game);
/*TODO*///                                    LOG((errorlog, "Trying %s in %s\n", file, name));
/*TODO*///                                    if( cache_stat (name, &stat_buffer) == 0 )
/*TODO*///                                    {
/*TODO*///                                            if( load_zipped_file (name, file, &f->data, &f->length) == 0 )
/*TODO*///                                            {
/*TODO*///                                                    LOG((errorlog, "Using (osd_fopen) zip file %s\n", name));
/*TODO*///                                                    f->type = kZippedFile;
/*TODO*///                                                    f->offset = 0;
/*TODO*///                                                    found = 1;
/*TODO*///                                            }
/*TODO*///                                    }
/*TODO*///                }
/*TODO*///            }
/*TODO*///            break;

/*TODO*///            case OSD_FILETYPE_MEMCARD:
/*TODO*///                    sprintf (name, "%s/%s", memcarddir, filename);
/*TODO*///                    f->type = kPlainFile;
/*TODO*///                    f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///                    found = f->file != 0;
/*TODO*///                    break;

/*TODO*///            case OSD_FILETYPE_SCREENSHOT:
                    /* only for writing */
/*TODO*///                    if( !_write )
/*TODO*///                    {
/*TODO*///                            LOG((errorlog, "osd_fopen: OSD_FILETYPE_SCREENSHOT read not supported\n"));
/*TODO*///                            break;
/*TODO*///                    }

/*TODO*///                    sprintf (name, "%s/%s.png", screenshotdir, filename);
/*TODO*///                    f->type = kPlainFile;
/*TODO*///                    f->file = fopen (name, _write ? "wb" : "rb");
/*TODO*///                    found = f->file != 0;
/*TODO*///                    break;
	}

	if( found==0 )
	{
		f=null;
		return null;
	}

	return f;
    }
    public static int osd_fread (Object file, char []buffer,int offset, int length)
    {
             FakeFileHandle f = (FakeFileHandle) file;

            switch( f.type )
            {
            case kPlainFile:
                    return fread(buffer,offset,1,length,f.file);               
                    //break;
            case kZippedFile:
            case kRAMFile:
                    /* reading from the RAM image of a file */
                    if( f.data!=null )
                    {
                            if( length + f.offset > f.length )
                                    length = f.length - f.offset;
                           memcpy (buffer,offset, f.data,f.offset,length);
                            f.offset += length;
                            return length;
                    }
                    break;
            }

            return 0;
    }
    public static int osd_fread (Object file, CharPtr buffer,int length)
    {
        osd_fread(file,buffer.memory,buffer.base,length);
        return 0;
    }
    /* JB 980920 update */
    public static int osd_fseek (Object file, int offset, int whence)
    {
            FakeFileHandle f = (FakeFileHandle) file;
            int err = 0;

            switch( f.type )
            {
            case kPlainFile:
                    if(whence==SEEK_SET)
                    {
                      fseek (f.file, offset);
                      return 0;
                    }
                    else
                    {
                        throw new UnsupportedOperationException("FSEEK other than SEEK_SET NOT SUPPORTED.");
                    }
                    //break;
            case kZippedFile:
            case kRAMFile:
                    /* seeking within the RAM image of a file */
                    switch( whence )
                    {
                    case SEEK_SET:
                            f.offset = offset;
                            break;
                    case SEEK_CUR:
                            f.offset += offset;
                            break;
                    case SEEK_END:
                            f.offset = f.length + offset;
                            break;
                    }
                    break;
            }

            return err;
    }

    /* JB 980920 update */
    public static void osd_fclose (Object file)
    {
            FakeFileHandle f = (FakeFileHandle) file;

            switch( f.type )
            {
            case kPlainFile:
                    fclose (f.file);
                    break;
            case kZippedFile:
            case kRAMFile:
                    if( f.data!=null )
                           f.data=null;
                    break;
            }
            f=null;
    }
    public static int checksum_file (String file, char[] p, int[] size,int[] crc)
    {
        FILE f;
        f = fopen(file,"rb");
        if(f==null)
            return -1;
        
        long length = ftell(f);
       
        
        if( fread (p, 0, 1, (int)length, f) != length )
        {
             fclose (f);
             return -1;
        }
         size[0] = (int)length;
         /*Checksum crcal = new CRC32();
         String temp = new String(p);//make string to be able to get the bytes
        crcal.update(temp.getBytes(), 0, size[0]);
         long result =crcal.getValue();*/
         try {

            CheckedInputStream cis = null;
            long fileSize = 0;
            try {
                // Computer CRC32 checksum
                cis = new CheckedInputStream(
                        new FileInputStream(file), new CRC32());

                fileSize = new File(file).length();
               
            } catch (FileNotFoundException e) {
                System.err.println("File not found.");
                System.exit(1);
            }

            byte[] buf = new byte[(int)fileSize];
            while(cis.read(buf) >= 0) {
            }

            long checksum = cis.getChecksum().getValue();
            crc[0] = (int)checksum;
            //System.out.println(checksum + " " + fileSize + " " + file);

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
          
         
         fclose(f);
      /*     int length;
           unsigned char *data;
           FILE *f;

           f = fopen (file, "rb");
           if( !f )
                   return -1;

           /* determine length of file */
      /*     if( fseek (f, 0L, SEEK_END) != 0 )
           {
                   fclose (f);
                   return -1;
           }

           length = ftell (f);
           if( length == -1L )
           {
                   fclose (f);
                   return -1;
           }

           /* allocate space for entire file */
       /*    data = (unsigned char *) malloc (length);
           if( !data )
           {
                   fclose (f);
                   return -1;
           }

           /* read entire file into memory */
      /*     if( fseek (f, 0L, SEEK_SET) != 0 )
           {
                   free (data);
                   fclose (f);
                   return -1;
           }

           if( fread (data, sizeof (unsigned char), length, f) != length )
           {
                   free (data);
                   fclose (f);
                   return -1;
           }

           *size = length;
           *crc = crc32 (0L, data, length);
           if( p )
                   *p = data;
           else
                   free (data);

           fclose (f);*/

           return 0;
   }   
   public static int osd_fsize (Object file)
   {
            FakeFileHandle f = (FakeFileHandle) file;

            if( f.type == kRAMFile || f.type == kZippedFile )
                    return f.length;

            if( f.file!=null )
            {
                    int size, offs;
                    /*offs = ftell( f->file );
                    fseek( f->file, 0, SEEK_END );
                    size = ftell( f->file );
                    fseek( f->file, offs, SEEK_SET );*/
                    size = (int)ftell(f.file); //don't need the above just get the file size
                    return size;
            }

        return 0;
   }

/* JB 980920 */
   public static int osd_fcrc (Object file)
   {
            FakeFileHandle f = (FakeFileHandle) file;

            return f.crc;
   } 
    /* called while loading ROMs. It is called a last time with name == 0 to signal */
    /* that the ROM loading process is finished. */
    /* return non-zero to abort loading */
    public static int osd_display_loading_rom_message (String name, int current, int total)
    {
            if( name!=null )
                    System.out.print("loading " + name + "\r");
            else
                    System.out.print("                    \r");


/*TODO*///            if( keyboard_pressed (KEYCODE_LCONTROL) && keyboard_pressed (KEYCODE_C) )
/*TODO*///                    return 1;

            return 0;
    }    
}
