package gr.codebb.arcadeflex.v036.platform;

import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import gr.codebb.arcadeflex.common.CRC;

public class fileio {

    /*TODO*/ //    char *roms = NULL;
/*TODO*/ //    char **rompathv = NULL;
/*TODO*/ //    int rompathc = 0;

    /*TODO*/ //    char *samples = NULL;
/*TODO*/ //    char **samplepathv = NULL;
/*TODO*/ //    int samplepathc = 0;
    //public static final String romUrl = "http://www.arcadeflex.com/roms/";
    //public static String romUrl = "http://www.jnodes.net/roms/";
    /*TODO*/ //    char *cfgdir, *nvdir, *hidir, *inpdir, *stadir;
    /*TODO*/ //   char *memcarddir, *artworkdir, *screenshotdir;
    /*temp nvdir, will be configurable lator*/ static String nvdir = "nvram";
    /*TODO*/ //     char *alternate_name;				   /* for "-romdir" */
    public static final int kPlainFile = 1;
    public static final int kRAMFile = 2;
    public static final int kZippedFile = 3;
    private static boolean cacheExist = false, checkedExists = false;
    private static byte[] zipOnlineCacheData1 = null;
    private static String zipOnlineCacheName1 = "";
    private static byte[] zipOnlineCacheData2 = null;
    private static String zipOnlineCacheName2 = "";

    static class FakeFileHandle {

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
    
    public static void downloadFile(String _rom, String _dstDir) {
        String _url_ROM = settings.romUrl+_rom+".zip";
        System.out.println("Downloading "+_url_ROM);
        try (BufferedInputStream inputStream = new BufferedInputStream(new URL(_url_ROM).openStream());
            FileOutputStream fileOS = new FileOutputStream(_dstDir+"/"+_rom+".zip")) {
              byte data[] = new byte[1024];
              int byteContent;
              while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
                  fileOS.write(data, 0, byteContent);
              }
              fileOS.close();
              
        } catch (IOException e) {
              e.printStackTrace(System.out);
        }
    }
    
    public static Object osd_fopen(String game, String filename, int filetype, int _write) {
        //System.out.println("entering osd_fopen for "+game+" "+filename);
        String name = "";
        String gamename;
        int found = 0;
        int indx;
        FakeFileHandle f;
        int pathc = 0;
        String[] pathv = null;

        f = new FakeFileHandle();
        if (f == null) {
            //LOG((errorlog, "osd_fopen: failed to malloc FakeFileHandle!\n"));
            return null;
        }
        gamename = game;
        gr.codebb.arcadeflex.v036.platform.settings._current_gamename = game;

        /* Support "-romdir" yuck. */
 /*TODO*///	if( alternate_name )
/*TODO*///	{
/*TODO*///		LOG((errorlog, "osd_fopen: -romdir overrides '%s' by '%s'\n", gamename, alternate_name));
        /*TODO*///       gamename = alternate_name;
/*TODO*///	}
        switch (filetype) {
            case OSD_FILETYPE_ROM:
            case OSD_FILETYPE_SAMPLE:

                /* only for reading */
                if (_write != 0) {
                    fprintf(errorlog, "osd_fopen: OSD_FILETYPE_ROM/SAMPLE/ROM_CART write not supported\n");
                    break;
                }

                if (filetype == OSD_FILETYPE_SAMPLE) {
                    /*TODO*///                        LOG((errorlog, "osd_fopen: using samplepath\n"));
                    /*TODO*///                        pathc = samplepathc;
                    /*TODO*///                       pathv = samplepathv;
                    pathc = 1;
                    pathv = new String[1];
                    pathv[0] = "samples";
                } else {
                    /*TODO*///                       LOG((errorlog, "osd_fopen: using rompath\n"));
                    /*TODO*///                       pathc = rompathc;
                    /*TODO*///                       pathv = rompathv;
                    /*TEMPHACK*/ //just added manually roms directory since else we need to implement conf class (in future)                      
                    pathc = 1;
                    pathv = new String[1];
                    pathv[0] = "roms";
                    
                    if (!(new File(pathv[0] + File.separator + gamename + ".zip").exists())){
                        //found=1;
                        System.out.println(gamename+" not FOUND! Trying to download it");
                        downloadFile(gamename, pathv[0]);
                    }
                    
                }
                
                //System.out.println(gamename);
                //System.out.println(filename);

                for (indx = 0; indx < pathc && found == 0; ++indx) {
                    String dir_name = pathv[indx];
                    //unZipIt(dir_name + File.separator + gamename + ".zip", dir_name + File.separator + gamename, filename);
                    
                    if (found == 0) {
                        name = sprintf("%s/%s", dir_name, gamename);
                        fprintf(errorlog, "Trying %s\n", name);
                        //java code to emulate stat command (shadow)
                        if (MainStream.inst == null) {
                            osdepend.dlprogress.setFileName("loading file: " + name);
                        }
                        //case where file exists in rom folder
                        if (new File(name).isDirectory() && new File(name).exists()) // if( cache_stat (name, &stat_buffer) == 0 && (stat_buffer.st_mode & S_IFDIR) )               
                        {
                            //System.out.println("case where file exists in rom folder");
                            name = sprintf("%s/%s/%s", dir_name, gamename, filename);
                            //System.out.println(name);
                            if (new File(name).exists()) {
                                if (filetype == OSD_FILETYPE_ROM) {
                                    //java issue since there is no way to pass by reference the data table 
                                    //get it here
                                    f.file = fopen(name, "rb");
                                    long size = ftell(f.file);
                                    f.data = new char[(int) size];
                                    fclose(f.file);
                                    // http://www.java-tips.org/java-se-tips/java.lang/pass-an-integer-by-reference.html
                                    int tlen[] = new int[1];
                                    int tcrc[] = new int[1];
                                    if (checksum_file(name, f.data, tlen, tcrc) == 0) {
                                        f.type = kRAMFile;
                                        f.offset = 0;
                                        found = 1;
                                    }
                                    //copy values where they belong
                                    f.length = tlen[0];
                                    f.crc = tcrc[0];
                                } else {
                                    f.type = kPlainFile;
                                    f.file = fopen(name, "rb");
                                    found = (f.file != null) ? 1 : 0; //found = f.file !=0;
                                }
                            } else {
                                System.out.println(filename + " does not seem to exist as a file");
                            }
                        } else if (new File(dir_name + File.separator + gamename + ".zip").exists()) { //case where file exists in rom zip file
                            //System.out.println("case where file exists in rom zip file");
                            System.out.println("loading " + filename + " from zip");
                            //File thefile = unZipIt2(dir_name + File.separator + gamename + ".zip", filename);
                            byte[] bytes = unZipIt3(dir_name + File.separator + gamename + ".zip", filename);
                            if (bytes != null) {
                                name = sprintf("%s/%s/%s", dir_name, gamename, filename);
                                //System.out.println(name);
                                //if (new File(name).exists()) {
                                if (filetype == OSD_FILETYPE_ROM) {
                                    //java issue since there is no way to pass by reference the data table 
                                    //get it here
                                    f.file = fopen(bytes, filename, "rb");
                                    long size = ftell(f.file);
                                    f.data = new char[(int) size];
                                    fclose(f.file);
                                    // http://www.java-tips.org/java-se-tips/java.lang/pass-an-integer-by-reference.html
                                    int tlen[] = new int[1];
                                    int tcrc[] = new int[1];
                                    if (checksum_file_zipped(bytes, filename, f.data, tlen, tcrc) == 0) {
                                        f.type = kRAMFile;
                                        f.offset = 0;
                                        found = 1;
                                    }
                                    //copy values where they belong
                                    f.length = tlen[0];
                                    f.crc = tcrc[0];
                                } else {
                                    f.type = kPlainFile;
                                    f.file = fopen(bytes, filename, "rb");
                                    found = (f.file != null) ? 1 : 0; //found = f.file !=0;
                                }
                                //thefile.delete();
                                //thefile=null;
                                //}
                            } else {
                                System.out.println(filename + " does not seem to exist in the zip file");
                            }

                        } else if (URLexistsWithCache(settings.romUrl + gamename + ".zip")) {// url loading here, the last resort of finding the rom. *todo.
                            System.out.println("loading " + filename + " from zip online");
                            byte[] bytes = unZipItOnlineWithCache(settings.romUrl + gamename + ".zip", filename);
                            if (bytes != null) {
                                name = sprintf("%s/%s/%s", dir_name, gamename, filename);
                                //System.out.println(name);
                                //if (new File(name).exists()) {
                                if (filetype == OSD_FILETYPE_ROM) {
                                    //java issue since there is no way to pass by reference the data table 
                                    //get it here
                                    f.file = fopen(bytes, filename, "rb");
                                    long size = ftell(f.file);
                                    f.data = new char[(int) size];
                                    fclose(f.file);
                                    // http://www.java-tips.org/java-se-tips/java.lang/pass-an-integer-by-reference.html
                                    int tlen[] = new int[1];
                                    int tcrc[] = new int[1];
                                    if (checksum_file_zipped(bytes, filename, f.data, tlen, tcrc) == 0) {
                                        f.type = kRAMFile;
                                        f.offset = 0;
                                        found = 1;
                                    }
                                    //copy values where they belong
                                    f.length = tlen[0];
                                    f.crc = tcrc[0];
                                } else {
                                    f.type = kPlainFile;
                                    f.file = fopen(bytes, filename, "rb");
                                    found = (f.file != null) ? 1 : 0; //found = f.file !=0;
                                }

                            } else {
                                System.out.println(filename + " does not seem to exist in the zip file online");
                                //System.out.println("possibly it is in parent rom: "+Machine.gamedrv.clone_of.name);
                            }
                        } else if (URLexists(settings.romUrl + gamename + "/" + filename)) {
                            System.out.println("(loading file online)");
                            byte[] bytes = FetchOnlineFile(settings.romUrl + gamename + "/" + filename);
                            if (bytes != null) {
                                name = sprintf("%s/%s/%s", dir_name, gamename, filename);
                                //System.out.println(name);
                                //if (new File(name).exists()) {
                                if (filetype == OSD_FILETYPE_ROM) {
                                    //java issue since there is no way to pass by reference the data table 
                                    //get it here
                                    f.file = fopen(bytes, filename, "rb");
                                    long size = ftell(f.file);
                                    f.data = new char[(int) size];
                                    fclose(f.file);
                                    // http://www.java-tips.org/java-se-tips/java.lang/pass-an-integer-by-reference.html
                                    int tlen[] = new int[1];
                                    int tcrc[] = new int[1];
                                    if (checksum_file_zipped(bytes, filename, f.data, tlen, tcrc) == 0) {
                                        f.type = kRAMFile;
                                        f.offset = 0;
                                        found = 1;
                                    }
                                    //copy values where they belong
                                    f.length = tlen[0];
                                    f.crc = tcrc[0];
                                } else {
                                    f.type = kPlainFile;
                                    f.file = fopen(bytes, filename, "rb");
                                    found = (f.file != null) ? 1 : 0; //found = f.file !=0;
                                }
                                //thefile.delete();
                                //thefile=null;
                                //}
                            } else {
                                System.out.println(filename + " does not seem to exist in the zip file");
                                if (MainStream.inst == null) {
                                    osdepend.dlprogress.setFileName(filename + " does not seem to exist in the zip file");
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

            case OSD_FILETYPE_NVRAM:
                if (found == 0) {
                    name = sprintf("%s/%s.nv", nvdir, gamename);
                    f.type = kPlainFile;
                    f.file = fopen(name, _write != 0 ? "wb" : "rb");
                    found = (f.file != null) ? 1 : 0; //found = f.file !=0;
                }
                break;

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

        if (found == 0) {
            f = null;
            //System.out.println("null returned osd_fopen for "+game+" "+filename);
            return null;
        }

        return f;
    }

    public static int osd_fread(Object file, char[] buffer, int offset, int length) {
        FakeFileHandle f = (FakeFileHandle) file;

        switch (f.type) {
            case kPlainFile:
                return fread(buffer, offset, 1, length, f.file);
            //break;
            case kZippedFile:
            case kRAMFile:
                /* reading from the RAM image of a file */
                if (f.data != null) {
                    if (length + f.offset > f.length) {
                        length = f.length - f.offset;
                    }
                    memcpy(buffer, offset, f.data, f.offset, length);
                    f.offset += length;
                    return length;
                }
                break;
        }

        return 0;
    }

    public static int osd_fread_lsbfirst(Object file, char[] buffer, int length) {
        return osd_fread(file, buffer, 0, length);
    }

    public static int osd_fread_lsbfirst(Object file, byte[] buffer, int length) {
        char[] buf = new char[length];
        int r = osd_fread(file, buf, 0, length);
        for (int i = 0; i < buf.length; i++) {
            buffer[i] = (byte) buf[i];
        }
        return r;
    }

    public static int osd_fread(Object file, char[] buffer, int length) {
        return osd_fread(file, buffer, 0, length);
    }

    public static int osd_fread(Object file, byte[] buffer, int length) {
        char[] buf = new char[length];
        int r = osd_fread(file, buf, 0, length);
        for (int i = 0; i < buf.length; i++) {
            buffer[i] = (byte) buf[i];
        }
        return r;
    }

    public static int osd_fread(Object file, CharPtr buffer, int length) {
        osd_fread(file, buffer.memory, buffer.base, length);
        return 0;
    }

    public static int osd_fread(Object file, UBytePtr buffer, int length) {
        osd_fread(file, buffer.memory, buffer.offset, length);
        return 0;
    }

    public static int osd_fread(Object file, UBytePtr buffer, int offset, int length) {
        osd_fread(file, buffer.memory, buffer.offset + offset, length);
        return 0;
    }

    public static int osd_fread_scatter(Object file, CharPtr buffer, int length, int increment) {
        //unsigned char *buf = buffer;
        FakeFileHandle f = (FakeFileHandle) file;
        char[] tempbuf = new char[4096];
        int totread, r, i;
        int buf = 0;
        switch (f.type) {
            case kPlainFile:
                totread = 0;
                while (length != 0) {
                    r = length;
                    if (r > 4096) {
                        r = 4096;
                    }
                    r = fread(tempbuf, buffer.base, 1, r, f.file);
                    if (r == 0) {
                        return totread;
                        /* error */
                    }
                    for (i = 0; i < r; i++) {
                        buffer.write(buf, tempbuf[i]);
                        buf += increment;
                    }
                    totread += r;
                    length -= r;
                }
                return totread;
            //break;
            case kZippedFile:
            case kRAMFile:
                /* reading from the RAM image of a file */
                if (f.data != null) {
                    if (length + f.offset > f.length) {
                        length = f.length - f.offset;
                    }
                    for (i = 0; i < length; i++) {
                        buffer.write(buf, f.data[f.offset + i]);
                        buf += increment;
                    }
                    f.offset += length;
                    return length;
                }
                break;
        }

        return 0;
    }

    /* JB 980920 update */
    public static int osd_fwrite(Object file, CharPtr buffer, int length) {
        osd_fwrite(file, buffer.memory, buffer.base, length);
        return 0;
    }

    public static int osd_fwrite(Object file, UBytePtr buffer, int length) {
        osd_fwrite(file, buffer.memory, buffer.offset, length);
        return 0;
    }

    public static int osd_fwrite(Object file, UBytePtr buffer, int offset, int length) {
        osd_fwrite(file, buffer.memory, buffer.offset + offset, length);
        return 0;
    }

    public static void osd_fwrite(Object file, char[] buffer, int offset, int length) {
        FakeFileHandle f = (FakeFileHandle) file;

        switch (f.type) {
            case kPlainFile:
                fwrite(buffer, offset, 1, length, f.file);
            default:
                return;
        }
    }

    public static int osd_fseek(Object file, int offset, int whence) {
        FakeFileHandle f = (FakeFileHandle) file;
        int err = 0;

        switch (f.type) {
            case kPlainFile:
                if (whence == SEEK_SET) {
                    fseek(f.file, offset, SEEK_SET);
                    return 0;
                } else if (whence == SEEK_CUR) {
                    fseek(f.file, offset, SEEK_CUR);
                    return 0;
                } else {
                    throw new UnsupportedOperationException("FSEEK other than SEEK_SET NOT SUPPORTED.");
                }
            //break;
            case kZippedFile:
            case kRAMFile:
                /* seeking within the RAM image of a file */
                switch (whence) {
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
    public static void osd_fclose(Object file) {
        FakeFileHandle f = (FakeFileHandle) file;

        switch (f.type) {
            case kPlainFile:
                fclose(f.file);
                break;
            case kZippedFile:
            case kRAMFile:
                if (f.data != null) {
                    f.data = null;
                }
                break;
        }
        f = null;
    }

    public static int checksum_file(String file, char[] p, int[] size, int[] crc) {
        FILE f;
        f = fopen(file, "rb");
        if (f == null) {
            return -1;
        }

        long length = ftell(f);

        if (fread(p, 1, (int) length, f) != length) {
            fclose(f);
            return -1;
        }
        size[0] = (int) length;
        crc[0] = (int) CRC.crc(p, size[0]);

        return 0;
    }
        public static int checksum_file_zipped(byte[] bytes, String filename, char[] p, int[] size, int[] crc) {
        FILE f;
        f = fopen(bytes, filename, "rb");
        if (f == null) {
            return -1;
        }

        long length = ftell(f);

        if (fread(p, 0, 1, (int) length, f) != length) {
            fclose(f);
            return -1;
        }
        size[0] = (int) length;
        crc[0] = (int) CRC.crc(p, size[0]);
        return 0;
    }

    public static int osd_fsize(Object file) {
        FakeFileHandle f = (FakeFileHandle) file;

        if (f.type == kRAMFile || f.type == kZippedFile) {
            return f.length;
        }

        if (f.file != null) {
            int size, offs;
            /*offs = ftell( f->file );
             fseek( f->file, 0, SEEK_END );
             size = ftell( f->file );
             fseek( f->file, offs, SEEK_SET );*/
            size = (int) ftell(f.file); //don't need the above just get the file size
            return size;
        }

        return 0;
    }

    /* JB 980920 */
    public static int osd_fcrc(Object file) {
        FakeFileHandle f = (FakeFileHandle) file;

        return f.crc;
    }

    /* called while loading ROMs. It is called a last time with name == 0 to signal */
 /* that the ROM loading process is finished. */
 /* return non-zero to abort loading */
    public static int osd_display_loading_rom_message(String name, int current, int total) {
        if (name != null) {
            System.out.print("loading " + name + "\r");
        } else {
            System.out.print("                    \r");
        }


        /*TODO*///            if( keyboard_pressed (KEYCODE_LCONTROL) && keyboard_pressed (KEYCODE_C) )
/*TODO*///                    return 1;
        return 0;
    }

    public static void unZipIt(String zipFile, String outputFolder, String filename) {
        if (!new File(zipFile).exists()) {
            System.out.println("unzip failed");
        } else //System.out.println("entered unzip");
        {
            if (new File(outputFolder + File.separator + filename).exists()) {
                //System.out.println("(file already there)");
            } else {
                byte[] buffer = new byte[1024];

                try {

                    //create output directory is not exists
                    File folder = new File(outputFolder);
                    if (!folder.exists()) {
                        folder.mkdir();
                    }

                    //get the zip file content
                    ZipInputStream zis
                            = new ZipInputStream(new FileInputStream(zipFile));
                    //get the zipped file list entry
                    ZipEntry ze = zis.getNextEntry();

                    while (ze != null) {

                        String fileName = ze.getName();
                        //System.out.println("[zip] fileName: "+fileName+" while filename:"+filename+" and output folder is: "+outputFolder);
                        if (fileName.equalsIgnoreCase(filename)) {
                            //System.out.println("extracting!!!!!!!");
                            File newFile = new File(outputFolder + File.separator + fileName);

                            System.out.println("file unzip : " + newFile.getAbsoluteFile() + " ");

                            //create all non exists folders
                            //else you will hit FileNotFoundException for compressed folder
                            new File(newFile.getParent()).mkdirs();

                            FileOutputStream fos = new FileOutputStream(newFile);

                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }

                            fos.close();
                        }

                        ze = zis.getNextEntry();
                    }

                    zis.closeEntry();
                    zis.close();

                    //System.out.println("Done ");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

    public static File unZipIt2(String zipFile, String filename) {
        File out = null;

        if (!new File(zipFile).exists()) {
            System.out.println("unzip failed");
        } else {
            //System.out.println("entered unzipit2 for "+filename);

            byte[] buffer = new byte[1024];

            try {

                //get the zip file content
                ZipInputStream zis
                        = new ZipInputStream(new FileInputStream(zipFile));
                //get the zipped file list entry
                ZipEntry ze = zis.getNextEntry();

                while (ze != null) {

                    String fileName = ze.getName();
                    //System.out.println("[zip] fileName: "+fileName+" while filename:"+filename+" and output folder is: "+outputFolder);
                    if (fileName.equalsIgnoreCase(filename)) {
                        //System.out.println("extracting!!!!!!!");
                        out = new File(System.getProperty("java.io.tmpdir") + "tmp");
                        //System.out.println(System.getProperty("java.io.tmpdir")+"tmp");
                        //System.out.println("file unzip : " + newFile.getAbsoluteFile() + " ");

                        //create all non exists folders
                        //else you will hit FileNotFoundException for compressed folder
                        //new File(out.getParent()).mkdirs();
                        FileOutputStream fos = new FileOutputStream(out);

                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }

                        fos.close();
                    }
                    ze = zis.getNextEntry();
                }

                zis.closeEntry();
                zis.close();

                //System.out.println("Done ");
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
        return out;
    }

    public static byte[] unZipIt3(String zipFile, String filename) {
        byte[] out = null;

        if (!new File(zipFile).exists()) {
            System.out.println("unzip failed");
        } else {
            //System.out.println("entered unzipit2 for "+filename);

            byte[] buffer = new byte[1024];

            try {
                //get the zip file content
                ZipInputStream zis
                        = new ZipInputStream(new FileInputStream(zipFile));
                //get the zipped file list entry
                ZipEntry ze = zis.getNextEntry();

                while (ze != null) {

                    String fileName = ze.getName();
                    //System.out.println("[zip] fileName: "+fileName+" while filename:"+filename+" and output folder is: "+outputFolder);
                    if (fileName.equalsIgnoreCase(filename)) {
                        //System.out.println("extracting!!!!!!!");
                        //out = new File(System.getProperty("java.io.tmpdir")+"tmp");
                        //System.out.println(System.getProperty("java.io.tmpdir")+"tmp");
                        //System.out.println("file unzip : " + newFile.getAbsoluteFile() + " ");

                        //create all non exists folders
                        //else you will hit FileNotFoundException for compressed folder
                        //new File(out.getParent()).mkdirs();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        //FileOutputStream fos = new FileOutputStream(out);

                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            baos.write(buffer, 0, len);
                        }

                        baos.close();
                        out = baos.toByteArray();
                    }
                    ze = zis.getNextEntry();
                }

                zis.closeEntry();
                zis.close();

                //System.out.println("Done ");
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
        return out;
    }

    public static byte[] unZipItOnlineWithCache(String zipFileURL, String filename) {
        byte[] out = null;
        //fetching if not already fetched
        byte[] zipbytes = null;
        if (zipOnlineCacheName1.equals(zipFileURL)) {
            zipbytes = zipOnlineCacheData1;
        } else if (zipOnlineCacheName2.equals(zipFileURL)) {
            zipbytes = zipOnlineCacheData2;
        } else if (zipOnlineCacheName1.isEmpty()) {
            zipbytes = FetchOnlineFile(zipFileURL);
            zipOnlineCacheData1 = zipbytes;
            zipOnlineCacheName1 = zipFileURL;
        } else if (zipOnlineCacheName2.isEmpty()) {
            zipbytes = FetchOnlineFile(zipFileURL);
            zipOnlineCacheData2 = zipbytes;
            zipOnlineCacheName2 = zipFileURL;
        } else {
            System.out.println("Both caches missed! weird!");
            zipbytes = FetchOnlineFile(zipFileURL);
        }

        //System.out.println("entered unzipit2 for "+filename);
        byte[] buffer = new byte[1024];
        try {
            //get the zip file content
            ZipInputStream zis
                    = new ZipInputStream(new ByteArrayInputStream(zipbytes));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                //System.out.println("[zip] fileName: "+fileName+" while filename:"+filename+" and output folder is: "+outputFolder);
                if (fileName.equalsIgnoreCase(filename)) {
                    //System.out.println("extracting!!!!!!!");
                    //out = new File(System.getProperty("java.io.tmpdir")+"tmp");
                    //System.out.println(System.getProperty("java.io.tmpdir")+"tmp");
                    //System.out.println("file unzip : " + newFile.getAbsoluteFile() + " ");

                    //create all non exists folders
                    //else you will hit FileNotFoundException for compressed folder
                    //new File(out.getParent()).mkdirs();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    //FileOutputStream fos = new FileOutputStream(out);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                    baos.close();
                    out = baos.toByteArray();
                }
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            //System.out.println("Done ");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return out;
    }

    public static byte[] FetchOnlineFile(String fileURL) {
        byte[] out = null;
        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        InputStream is = null;
        URL url = null;
        try {
            url = new URL(fileURL);
            is = url.openStream();
            byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
            int n;

            while ((n = is.read(byteChunk)) > 0) {
                bais.write(byteChunk, 0, n);
            }
        } catch (IOException e) {
            System.err.printf("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
            e.printStackTrace();
            // Perform any other exception handling that's appropriate.
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                }
            }
        }
        out = bais.toByteArray();
        return out;
    }

    public static ByteArrayInputStream unZipIt4(String zipFile, String filename) { //not used yet. needs major changes in libc_old..
        ByteArrayInputStream inb = null;
        ByteArrayOutputStream outb = null;

        if (!new File(zipFile).exists()) {
            System.out.println("unzip failed");
        } else {
            //System.out.println("entered unzipit2 for "+filename);

            byte[] buffer = new byte[1024];

            try {

                //get the zip file content
                ZipInputStream zis
                        = new ZipInputStream(new FileInputStream(zipFile));
                //get the zipped file list entry
                ZipEntry ze = zis.getNextEntry();

                while (ze != null) {

                    String fileName = ze.getName();
                    //System.out.println("[zip] fileName: "+fileName+" while filename:"+filename+" and output folder is: "+outputFolder);
                    if (fileName.equalsIgnoreCase(filename)) {
                        //System.out.println("extracting!!!!!!!");
                        outb = new ByteArrayOutputStream();
                        //System.out.println(System.getProperty("java.io.tmpdir")+"tmp");
                        //System.out.println("file unzip : " + newFile.getAbsoluteFile() + " ");

                        //create all non exists folders
                        //else you will hit FileNotFoundException for compressed folder
                        //new File(out.getParent()).mkdirs();
                        int len;
                        while ((len = zis.read(buffer)) > 0) {

                            outb.write(buffer, 0, len);
                        }

                        outb.close();
                        inb = new ByteArrayInputStream(outb.toByteArray());
                    }

                    ze = zis.getNextEntry();
                }

                zis.closeEntry();
                zis.close();

                System.out.println("Done ");

            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
        return inb;
    }

    public static boolean URLexistsWithCache(String URLName) {
        if (checkedExists) {
            return cacheExist;
        }
        cacheExist = URLexists(URLName);
        checkedExists = true;
        return cacheExist;
    }

    public static boolean URLexists(String URLName) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con
                    = (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("HEAD");
            boolean exists = (con.getResponseCode() == HttpURLConnection.HTTP_OK);
            return exists;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
