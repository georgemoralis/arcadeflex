/*
 * 
 *   Most of intial stuff has been done this class should be ok for the first tests
 * 
 */
package gr.codebb.arcadeflex.v036.mame;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.mameH.*;
import static gr.codebb.arcadeflex.v036.platform.fileio.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.common.Util.*;
import static common.libc.cstdlib.rand;
import static gr.codebb.arcadeflex.v036.platform.video.*;
public class common {
    
    /* These globals are only kept on a machine basis - LBO 042898 */
    
    public static int dispensed_tickets;
    public static int coins[]=new int[COIN_COUNTERS];
    public static int lastcoin[]=new int[COIN_COUNTERS];
    public static int coinlockedout[]=new int[COIN_COUNTERS];
    
    public static void showdisclaimer()   /* MAURY_BEGIN: dichiarazione */
    {
	printf("MAME is an emulator: it reproduces, more or less faithfully, the behaviour of\n"
		+ "several arcade machines. But hardware is useless without software, so an image\n"
		+ "of the ROMs which run on that hardware is required. Such ROMs, like any other\n"
		+ "commercial software, are copyrighted material and it is therefore illegal to\n"
		+ "use them if you don't own the original arcade machine. Needless to say, ROMs\n"
		+ "are not distributed together with MAME. Distribution of MAME together with ROM\n"
		+ "images is a violation of copyright law and should be promptly reported to the\n"
		+ "authors so that appropriate legal action can be taken.\n\n");
    }

	
    /***************************************************************************

      Read ROMs into memory.

      Arguments:
      const struct RomModule *romp - pointer to an array of Rommodule structures,
                                     as defined in common.h.

    ***************************************************************************/
    public static int readroms()
    {
            int region;
            RomModule[] romp;
            int romp_ptr=0;
            int warning = 0;
            int fatalerror = 0;
            int total_roms,current_rom;
            String buf="";


            total_roms = current_rom = 0;
            romp = Machine.gamedrv.rom;

            if (romp==null) return 0;

            while (romp[romp_ptr].name!=null || romp[romp_ptr].offset!=0 || romp[romp_ptr].length!=0)
            {
                    if (romp[romp_ptr].name!=null && romp[romp_ptr].name != "-1")
                            total_roms++;

                    romp_ptr++;
            }


            romp_ptr=0;//romp = Machine.gamedrv.rom;

            for (region = 0;region < MAX_MEMORY_REGIONS;region++)
                    Machine.memory_region[region] = null;

            region = 0;

            while (romp[romp_ptr].name!=null || romp[romp_ptr].offset!=0 || romp[romp_ptr].length!=0)
            {
                    int region_size;
                    String name;

                    /* Mish:  An 'optional' rom region, only loaded if sound emulation is turned on */
                    if (Machine.sample_rate==0 && ((romp[romp_ptr].crc & REGIONFLAG_SOUNDONLY)!=0)) 
                    {
                        
                            if (errorlog!=null) fprintf(errorlog,"readroms():  Ignoring rom region %d\n",region);
                            Machine.memory_region_type[region] = romp[romp_ptr].crc;
                            region++;

                            romp_ptr++;
                            while (romp[romp_ptr].name!=null || romp[romp_ptr].length!=0)
                                    romp_ptr++;

                           continue;
                     }
                    if (romp[romp_ptr].name!=null || romp[romp_ptr].length!=0)
                    {
                            printf("Error in RomModule definition: expecting ROM_REGION\n");
                            return getout(current_rom,total_roms);
                    }

                    region_size = romp[romp_ptr].offset;
                    if ((Machine.memory_region[region] = new char[region_size]) == null)
                    {
                            printf("readroms():  Unable to allocate %d bytes of RAM\n",region_size);
                            return getout(current_rom,total_roms);
                    }
                    Machine.memory_region_length[region] = region_size;
                    Machine.memory_region_type[region] = romp[romp_ptr].crc;

                    /* some games (i.e. Pleiades) want the memory clear on startup */
                    if (region_size <= 0x400000)	/* don't clear large regions which will be filled anyway */
                            memset(Machine.memory_region[region],0,region_size);

                    romp_ptr++;

                    while (romp[romp_ptr].length!=0)
                    {
                            Object f;
                            int expchecksum = romp[romp_ptr].crc;
                            int	explength = 0;


                            if (romp[romp_ptr].name == null)
                            {
                                    printf("Error in RomModule definition: ROM_CONTINUE not preceded by ROM_LOAD\n");
                                    return getout(current_rom,total_roms);
                            }
                            else if (romp[romp_ptr].name == "-1")
                            {
                                    printf("Error in RomModule definition: ROM_RELOAD not preceded by ROM_LOAD\n");
                                    return getout(current_rom,total_roms);
                            }

                            name = romp[romp_ptr].name;

                            /* update status display */
                            if (osd_display_loading_rom_message(name,++current_rom,total_roms) != 0)
                            {
                                return getout(current_rom,total_roms);
                            }
                            else
                            {
                                    GameDriver drv;

                                    drv = Machine.gamedrv;
                                    do
                                    {
                                            f = osd_fopen(drv.name,name,OSD_FILETYPE_ROM,0);
                                            drv = drv.clone_of;
                                    } while (f == null && drv!=null);

                                    if (f == null)
                                    {
                                           /* NS981003: support for "load by CRC" */
                                           String crc = sprintf("%08x", romp[romp_ptr].crc);

                                           drv = Machine.gamedrv;
                                           do
                                            {
                                                   f = osd_fopen(drv.name,crc,OSD_FILETYPE_ROM,0);
                                                     drv = drv.clone_of;
                                           } while (f ==null && drv!=null);
                                    }
                            }

                            if (f!=null)
                            {
                                    do
                                    {
                                            int i;
                                            int length = romp[romp_ptr].length & ~ROMFLAG_MASK;

                                            if ((romp[romp_ptr].name != null) && (romp[romp_ptr].name.compareTo("-1") == 0))
                                            {
                                                osd_fseek(f,0,SEEK_SET);	/* ROM_RELOAD */
                                            }                                       
                                            else
                                                    explength += length;

                                            if (romp[romp_ptr].offset + length > region_size ||
                                                    (((romp[romp_ptr].length & ROMFLAG_NIBBLE)==0) && ((romp[romp_ptr].length & ROMFLAG_ALTERNATE)!=0)
                                                                    && (romp[romp_ptr].offset&~1) + 2*length > region_size))
                                            {
                                                   printf("Error in RomModule definition: %s out of memory region space\n",name);
                                                   osd_fclose(f);
                                                   return getout(current_rom,total_roms);
                                            }

                                            if ((romp[romp_ptr].length & ROMFLAG_NIBBLE)!=0)
                                            {
                                                    UBytePtr temp;


                                                    temp = new  UBytePtr(length);

                                                    if (temp==null)
                                                    {
                                                            printf("Out of memory reading ROM %s\n",name);
                                                            osd_fclose(f);
                                                            return getout(current_rom,total_roms);
                                                    }

                                                    if (osd_fread(f,temp,length) != length)
                                                    {
                                                            printf("Unable to read ROM %s\n",name);
                                                    }

                                                    /* ROM_LOAD_NIB_LOW and ROM_LOAD_NIB_HIGH */
                                                    UBytePtr c = new UBytePtr(Machine.memory_region[region],romp[romp_ptr].offset);
                                                   if ((romp[romp_ptr].length & ROMFLAG_ALTERNATE)!=0)
                                                   {
                                                            /* Load into the high nibble */
                                                          for (i = 0;i < length;i ++)
                                                           {
                                                                   c.write(i, (c.read(i) & 0x0f) | ((temp.read(i) & 0x0f) << 4));
                                                           }
                                                   }
                                                   else
                                                   {
                                                            /* Load into the low nibble */
                                                           for (i = 0;i < length;i ++)
                                                           {
                                                                   c.write(i,(c.read(i) & 0xf0) | (temp.read(i) & 0x0f));
                                                           }
                                                   }

                                                   temp=null;
                                            }
                                            else if ((romp[romp_ptr].length & ROMFLAG_ALTERNATE)!=0)    
                                            {
                                                
                                                    /* ROM_LOAD_EVEN and ROM_LOAD_ODD */
                                                    /* copy the ROM data */                    
                                                CharPtr c = new CharPtr(Machine.memory_region[region],(romp[romp_ptr].offset ^ 1));
                                           
                                                    if (osd_fread_scatter(f,c,length,2) != length)
                                                    {
                                                            printf("Unable to read ROM %s\n",name);
                                                    }
                                            }
                                            else if ((romp[romp_ptr].length & ROMFLAG_QUAD)!=0) {
                                              throw new UnsupportedOperationException("Unsupported readrom() ROMFLAG_QUAD");  
/*TODO*/ //                                                    static int which_quad=0; /* This is multi session friendly, as we only care about the modulus */
/*TODO*/ //                                                    unsigned char *temp;
/*TODO*/ //                                                    int base=0;

/*TODO*/ //                                                    temp = malloc(length);	/* Need to load rom to temporary space */
/*TODO*/ //                                                   osd_fread(f,temp,length);

                                                    /* Copy quad to region */
/*TODO*/ //                                                    c = Machine.memory_region[region] + romp[romp_ptr].offset;

                             
/*TODO*/ //                                                    switch (which_quad%4) {
/*TODO*/ //                                                            case 0: base=1; break;
/*TODO*/ //                                                            case 1: base=0; break;
/*TODO*/ //                                                            case 2: base=3; break;
/*TODO*/ //                                                            case 3: base=2; break;
/*TODO*/ //                                                    }
                                           

/*TODO*/ //                                                    for (i=base; i< length*4; i += 4)
/*TODO*/ //                                                            c[i]=temp[i/4];

/*TODO*/ //                                                    which_quad++;
/*TODO*/ //                                                    free(temp);
                                            }
                                            else
                                            {
                                                    int wide = romp[romp_ptr].length & ROMFLAG_WIDE;
                                                    int swap = (romp[romp_ptr].length & ROMFLAG_SWAP) ^ ROMFLAG_SWAP;

                                                    osd_fread(f,new CharPtr(Machine.memory_region[region],romp[romp_ptr].offset),length);

                                                    /* apply swappage */
                                                    CharPtr c = new CharPtr(Machine.memory_region[region],(romp[romp_ptr].offset));
                                                   if (wide!=0 && swap!=0)
                                                    {
                                                          
                                                            for (i = 0; i < length; i += 2)
                                                            {
                                                                    int temp = c.read(i);
                                                                    c.write(i, c.read(i+1));
                                                                    c.write(i+1, temp);
                                                            }
                                                    }
                                            }

                                            romp_ptr++;
                                    } while (romp[romp_ptr].length!=0 && (romp[romp_ptr].name == null || romp[romp_ptr].name.compareTo("-1") == 0));

                                    if (explength != osd_fsize (f))
                                    {
                                        buf += sprintf("%-12s WRONG LENGTH (expected: %08x found: %08x)\n", name, explength, osd_fsize(f));
                                        warning = 1;
                                    }

                                    if (expchecksum != osd_fcrc (f))
                                    {
                                        warning = 1;
                                        if (expchecksum == 0)
                                            buf += sprintf("%-12s NO GOOD DUMP KNOWN\n", name);
                                        else if (expchecksum == BADCRC(osd_fcrc(f)))
                                            buf += sprintf("%-12s ROM NEEDS REDUMP\n", name);
                                        else
                                            buf += sprintf("%-12s WRONG CRC (expected: %08x found: %08x)\n", name, expchecksum, osd_fcrc(f));
                                   }
                                   osd_fclose(f);
                            }
                            else
                            {
                                    /* allow for a NO GOOD DUMP KNOWN rom to be missing */
                                    if (expchecksum == 0)
                                    {
                                        buf +=sprintf("%-12s NOT FOUND (NO GOOD DUMP KNOWN)\n",name);
                                        warning = 1;
                                    }
                                    else
                                    {
                                        buf +=sprintf("%-12s NOT FOUND\n",name);
                                        fatalerror = 1;
                                    }

                                    do
                                    {
                                            if (fatalerror == 0)
                                            {
                                                    int i;
                                                //throw new UnsupportedOperationException("Unsupported readrom() case fatalerror");  
                                                    /* fill space with random data */
                                                     if ((romp[romp_ptr].length & ROMFLAG_ALTERNATE) != 0)
                                                     {
                                                             UBytePtr c;

                                                            /* ROM_LOAD_EVEN and ROM_LOAD_ODD */
                                                             c = new UBytePtr(Machine.memory_region[region], (romp[romp_ptr].offset ^ 1));
                                                  
                                                             for (i = 0;i < (romp[romp_ptr].length & ~ROMFLAG_MASK);i++)
                                                                     c.write(2*i, rand());
                                                     }
                                                     else
                                                     {
                                                             for (i = 0;i < (romp[romp_ptr].length & ~ROMFLAG_MASK);i++)
                                                                     Machine.memory_region[region][romp[romp_ptr].offset + i] = (char) rand();
                                                     }
                                            }
                                            romp_ptr++;
                                    } while (romp[romp_ptr].length!=0 && (romp[romp_ptr].name == null || romp[romp_ptr].name.compareTo("-1") == 0));
                            
                            }
                    }

                    region++;
            }
                        //debug stuff not for use
                     /*               char dump[] = Machine.memory_region[0];
                                                    int counter=0;
                                                    for(int x=0; x<dump.length; x++)
                                                    {
                                                        
                                                        fprintf(errorlog,"%x",Integer.valueOf(dump[x]));
                                                        if(counter==7)
                                                        {
                                                             fprintf(errorlog,"\n");
                                                             counter=0;
                                                        }   
                                                        counter++;
                                                           
                                                    }*/
            /* final status display */
            osd_display_loading_rom_message(null,current_rom,total_roms);

            if (warning!=0 || fatalerror!=0)
            {
                    if (fatalerror!=0)
                    {
                            buf +="ERROR: required files are missing, the game cannot be run.\n";
                            bailing = 1;
                    }
                    else
                    {
                            buf +="WARNING: the game might not run correctly.\n";
                    }
                    printf ("%s", buf);

/*TODO*/ //                    if (!options.gui_host && !bailing)
 /*TODO*/ //                   {
 /*TODO*/ //                           printf ("Press any key to continue\n");
/*TODO*/ //                            keyboard_read_sync();
/*TODO*/ //                            if (keyboard_pressed(KEYCODE_LCONTROL) && keyboard_pressed(KEYCODE_C))
/*TODO*/ //                                    return 1;
 /*TODO*/ //                   }
                   }

            if (fatalerror!=0) return 1;
            else return 0;
    }
    static int getout(int current_rom,int total_roms) {
        /* final status display */
        osd_display_loading_rom_message(null,current_rom,total_roms);
        
        for (int region = 0; region < MAX_MEMORY_REGIONS; region++) {
            Machine.memory_region[region] = null;
        }
        return 1;
    }
    public static void printromlist(RomModule[] romp, String basename)
    {
            if (romp==null) return;

            printf("This is the list of the ROMs required for driver \"%s\".\n"
                  +"Name              Size       Checksum\n",basename);
            int rom_ptr=0;
            while (romp[rom_ptr].name!=null || romp[rom_ptr].offset!=0 || romp[rom_ptr].length!=0)
            {
                    rom_ptr++;	/* skip memory region definition */

                    while (romp[rom_ptr].length!=0)
                    {
                            String name;
                            int length=0,expchecksum=0;


                            name = romp[rom_ptr].name;
                            expchecksum = romp[rom_ptr].crc;

                            length = 0;

                            do
                            {
                                    /* ROM_RELOAD */
                                    if ((romp[rom_ptr].name != null) && (romp[rom_ptr].name.compareTo("-1") == 0))
                                            length = 0;	/* restart */

                                    length += romp[rom_ptr].length & ~ROMFLAG_MASK;

                                    rom_ptr++;
                            } while (romp[rom_ptr].length!=0 && (romp[rom_ptr].name == null || romp[rom_ptr].name.compareTo("-1") == 0));

                            if (expchecksum!=0)
                                    printf("%-12s  %7d bytes  %08x\n",name,length,expchecksum);
                            else
                                    printf("%-12s  %7d bytes  NO GOOD DUMP KNOWN\n",name,length);
                    }
            }
    }
    /* ************************************************************************
     * <p>
     * Read samples into memory.
     * This function is different from readroms() because it doesn't fail if
     * it doesn't find a file: it will load as many samples as it can find.
     * <p>
     * *************************************************************************
     */
    static GameSample read_wav_sample(Object f) {
        long /*unsigned*/ offset = 0;
        long /*UINT32*/ length, rate, filesize, temp32;
        int /*UINT16*/ bits, temp16;
        char[] /*UINT8*/ buf = new char[32];
        GameSample result = null;


	/* read the core header and make sure it's a WAVE file */
        offset += osd_fread(f, buf, 4);
        if (offset < 4)
            return null;
        if (memcmp(buf, 0, "RIFF", 4) != 0)
            return null;

	/* get the total size */
        offset += osd_fread(f, buf, 4);
        if (offset < 8)
            return null;
        filesize = charArrayToLong(buf);

	/* read the RIFF file type and make sure it's a WAVE file */
        offset += osd_fread(f, buf, 4);
        if (offset < 12)
            return null;
        if (memcmp(buf, 0, "WAVE", 4) != 0)
            return null;


	/* seek until we find a format tag */
        while (true) {
            offset += osd_fread(f, buf, 4);
            char[] tmp = new char[buf.length];//temp creation
            System.arraycopy(buf, 0, tmp, 0, buf.length);//temp creation
            offset += osd_fread(f, buf, 4);//offset += osd_fread(f, &length, 4);
            length = charArrayToLong(buf);
            if (memcmp(tmp, 0, "fmt ", 4) == 0)
                break;

		/* seek to the next block */
            osd_fseek(f, (int) length, SEEK_CUR);
            offset += length;
            if (offset >= filesize)
                return null;
        }
    /* read the format -- make sure it is PCM */
        offset += osd_fread_lsbfirst(f, buf, 2);
        temp16 = charArrayToInt(buf);
        if (temp16 != 1)
            return null;

	/* number of channels -- only mono is supported */
        offset += osd_fread_lsbfirst(f, buf, 2);
        temp16 = charArrayToInt(buf);
        if (temp16 != 1)
            return null;

	/* sample rate */
        offset += osd_fread(f, buf, 4);
        rate = charArrayToLong(buf);

	/* bytes/second and block alignment are ignored */
        offset += osd_fread(f, buf, 6);

	/* bits/sample */
        offset += osd_fread_lsbfirst(f, buf, 2);
        bits = charArrayToInt(buf);
        if (bits != 8 && bits != 16)
            return null;


	/* seek past any extra data */
        osd_fseek(f, (int) length - 16, SEEK_CUR);
        offset += length - 16;

	/* seek until we find a data tag */
        while (true) {
            offset += osd_fread(f, buf, 4);
            char[] tmp = new char[buf.length];//temp creation
            System.arraycopy(buf, 0, tmp, 0, buf.length);//temp creation
            offset += osd_fread(f, buf, 4);//offset += osd_fread(f, &length, 4);
            length = charArrayToLong(buf);
            if (memcmp(tmp, 0, "data", 4) == 0)
                break;

		/* seek to the next block */
            osd_fseek(f, (int) length, SEEK_CUR);
            offset += length;
            if (offset >= filesize)
                return null;
        }
    /* allocate the game sample */
        result = new GameSample((int) length);
    /* fill in the sample data */
        result.length = (int) length;
        result.smpfreq = (int) rate;
        result.resolution = bits;

	/* read the data in */
        if (bits == 8) {
            osd_fread(f, result.data, (int) length);

		/* convert 8-bit data to signed samples */
            for (temp32 = 0; temp32 < length; temp32++)
                result.data[(int) temp32] ^= 0x80;
        } else {
        /* 16-bit data is fine as-is */
            osd_fread_lsbfirst(f, result.data, (int) length);
        }

        return result;
    }
    
    public static osd_bitmap bitmap_alloc_depth(int width, int height, int depth) {
        if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
            int temp;

            temp = width;
            width = height;
            height = temp;
        }

        return osd_new_bitmap(width, height, depth);
    }

    public static GameSamples readsamples(String[] samplenames, String basename)
/* V.V - avoids samples duplication */
/* if first samplename is *dir, looks for samples into "basename" first, then "dir" */ {
        int i;
        GameSamples samples = new GameSamples();
        int skipfirst = 0;

	/* if the user doesn't want to use samples, bail */
        if (options.use_samples == 0) return null;

        if (samplenames == null || samplenames[0] == null) return null;


        if (samplenames[0].charAt(0) == '*')
            skipfirst = 1;

        i = 0;
        while (samplenames[i + skipfirst] != null) i++;

        if (i == 0) return null;

        samples = new GameSamples(i);

        samples.total = i;
        for (i = 0; i < samples.total; i++)
            samples.sample[i] = null;

        for (i = 0; i < samples.total; i++) {
            Object f;

            if (samplenames[i + skipfirst].length()>0 && samplenames[i + skipfirst].charAt(0) != '\0') {
                if ((f = osd_fopen(basename, samplenames[i + skipfirst], OSD_FILETYPE_SAMPLE, 0)) == null)
                    if (skipfirst != 0)
                        f = osd_fopen(samplenames[0].substring(1, samplenames[0].length())/*samplenames[0] + 1*/, samplenames[i + skipfirst], OSD_FILETYPE_SAMPLE, 0);
                if (f != null) {
                    samples.sample[i] = read_wav_sample(f);
                    osd_fclose(f);
                }
            }
        }

        return samples;
    }


    public static void freesamples(GameSamples samples) {
        int i;

        if (samples == null) return;

        for (i = 0; i < samples.total; i++)
            samples.sample[i] = null;

        samples = null;
    }



 public static UBytePtr memory_region(int num)
{
            int i;

            if (num < MAX_MEMORY_REGIONS)
                    return new UBytePtr(Machine.memory_region[num]);
            else
            {
                    for (i = 0;i < MAX_MEMORY_REGIONS;i++)
                    {
                            if ((Machine.memory_region_type[i] & ~REGIONFLAG_MASK) == num)
                                    return new UBytePtr(Machine.memory_region[i]);
                    }
            }

            return null;
}

    public static int memory_region_length(int num)
    {
            int i;

            if (num < MAX_MEMORY_REGIONS)
                    return Machine.memory_region_length[num];
            else
            {
                    for (i = 0;i < MAX_MEMORY_REGIONS;i++)
                    {
                            if ((Machine.memory_region_type[i] & ~REGIONFLAG_MASK) == num)
                                    return Machine.memory_region_length[i];
                    }
            }

            return 0;
    }

/*TODO*/ //    int new_memory_region(int num, int length)
/*TODO*/ //    {
/*TODO*/ //        int i;
/*TODO*/ //
/*TODO*/ //        if (num < MAX_MEMORY_REGIONS)
/*TODO*/ //        {
/*TODO*/ //            Machine.memory_region_length[num] = length;
/*TODO*/ //            Machine.memory_region[num] = malloc(length);
/*TODO*/ //            return (Machine.memory_region[num] == NULL) ? 1 : 0;
/*TODO*/ //        }
/*TODO*/ //        else
/*TODO*/ //        {
/*TODO*/ //            for (i = 0;i < MAX_MEMORY_REGIONS;i++)
/*TODO*/ //            {
/*TODO*/ //                if (Machine.memory_region[i] == NULL)
/*TODO*/ //                {
/*TODO*/ //                    Machine.memory_region_length[i] = length;
/*TODO*/ //                    Machine.memory_region_type[i] = num;
/*TODO*/ //                    Machine.memory_region[i] = malloc(length);
/*TODO*/ //                    return (Machine.memory_region[i] == NULL) ? 1 : 0;
/*TODO*/ //                }
/*TODO*/ //            }
/*TODO*/ //        }
/*TODO*/ //            return 1;
/*TODO*/ //    }

/*TODO*/ //    void free_memory_region(int num)
/*TODO*/ //    {
/*TODO*/ //            int i;
/*TODO*/ //
 /*TODO*/ //           if (num < MAX_MEMORY_REGIONS)
/*TODO*/ //            {
/*TODO*/ //                    free(Machine.memory_region[num]);
/*TODO*/ //                    Machine.memory_region[num] = 0;
 /*TODO*/ //           }
/*TODO*/ //            else
 /*TODO*/ //           {
 /*TODO*/ //                   for (i = 0;i < MAX_MEMORY_REGIONS;i++)
 /*TODO*/ //                   {
/*TODO*/ //                            if ((Machine.memory_region_type[i] & ~REGIONFLAG_MASK) == num)
/*TODO*/ //                            {
/*TODO*/ //                                    free(Machine.memory_region[i]);
/*TODO*/ //                                    Machine.memory_region[i] = 0;
/*TODO*/ //                                    return;
/*TODO*/ //                            }
/*TODO*/ //                    }
/*TODO*/ //            }
 /*TODO*/ //   }


    /* LBO 042898 - added coin counters */
    public static WriteHandlerPtr coin_counter_w = new WriteHandlerPtr() { public void handler(int offset, int data)
    {
            if (offset >= COIN_COUNTERS) return;
            /* Count it only if the data has changed from 0 to non-zero */
            if (data!=0 && (lastcoin[offset] == 0))
            {
                    coins[offset] ++;
            }
            lastcoin[offset] = data;
    }};
    public static WriteHandlerPtr coin_lockout_w = new WriteHandlerPtr() { public void handler(int offset, int data)
    {
            if (offset >= COIN_COUNTERS) return;

            coinlockedout[offset] = data;
    }};

    /* Locks out all the coin inputs */
    public static WriteHandlerPtr coin_lockout_global_w = new WriteHandlerPtr() { public void handler(int offset, int data)
    {
            int i;

            for (i = 0; i < COIN_COUNTERS; i++)
            {
                    coin_lockout_w.handler(i, data);
            }
    }};



/*TODO*/ //    int snapno;

/*TODO*/ //    void save_screen_snapshot(void)
/*TODO*/ //    {
/*TODO*/ //            void *fp;
/*TODO*/ //            char name[20];


            /* avoid overwriting existing files */
            /* first of all try with "gamename.png" */
/*TODO*/ //            sprintf(name,"%.8s", Machine.gamedrv.name);
/*TODO*/ //            if (osd_faccess(name,OSD_FILETYPE_SCREENSHOT))
/*TODO*/ //            {
/*TODO*/ //                    do
/*TODO*/ //                    {
                            /* otherwise use "nameNNNN.png" */
/*TODO*/ //                            sprintf(name,"%.4s%04d",Machine.gamedrv.name,snapno++);
/*TODO*/ //                    } while (osd_faccess(name, OSD_FILETYPE_SCREENSHOT));
/*TODO*/ //            }

/*TODO*/ //            if ((fp = osd_fopen(Machine.gamedrv.name, name, OSD_FILETYPE_SCREENSHOT, 1)) != NULL)
/*TODO*/ //            {
/*TODO*/ //                    if (Machine.drv.video_attributes & VIDEO_TYPE_VECTOR)
/*TODO*/ //                            png_write_bitmap(fp,Machine.scrbitmap);
/*TODO*/ //                    else
/*TODO*/ //                    {
/*TODO*/ //                            struct osd_bitmap *bitmap;

/*TODO*/ //                            bitmap = osd_new_bitmap(
/*TODO*/ //                                            Machine.drv.visible_area.max_x - Machine.drv.visible_area.min_x + 1,
/*TODO*/ //                                            Machine.drv.visible_area.max_y - Machine.drv.visible_area.min_y + 1,
/*TODO*/ //                                            Machine.scrbitmap.depth);

/*TODO*/ //                            if (bitmap)
/*TODO*/ //                            {
/*TODO*/ //                                    copybitmap(bitmap,Machine.scrbitmap,0,0,
/*TODO*/ //                                                    -Machine.drv.visible_area.min_x,-Machine.drv.visible_area.min_y,0,TRANSPARENCY_NONE,0);
/*TODO*/ //                                    png_write_bitmap(fp,bitmap);
/*TODO*/ //                                    osd_free_bitmap(bitmap);
/*TODO*/ //                            }
/*TODO*/ //                    }

/*TODO*/ //                    osd_fclose(fp);
/*TODO*/ //            }
/*TODO*/ //    }
    /**
     * WIP 0.37b7 stuff
     */
    public static void bitmap_free(osd_bitmap bitmap) {
        osd_free_bitmap(bitmap);
    }
 
    public static osd_bitmap bitmap_alloc(int width, int height) {
        return osd_create_bitmap(width, height);
    }
}
