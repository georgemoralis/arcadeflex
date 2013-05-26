/*
This file is part of Arcadeflex.

Arcadeflex is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Arcadeflex is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Arcadeflex.  If not, see <http://www.gnu.org/licenses/>.
 */
package osdepend;

import static arcadeflex.libc.*;
import arcadeflex.settings;
import static mame.version.*;
import static mame.driver.*;
import static mame.driverH.*;
import static mame.commonH.*;
import static mame.common.*;
/**
 *
 * @author george
 */
public class fronthlp {
    
    public static int silentident,knownstatus;

    public static final int KNOWN_START  = 0;
    public static final int KNOWN_ALL    = 1;
    public static final int KNOWN_NONE   = 2;
    public static final int KNOWN_SOME   = 3;

    public static GameDriver gamedrv;

    /* compare string[8] using standard(?) DOS wildchars ('?' & '*')      */
    /* for this to work correctly, the shells internal wildcard expansion */
    /* mechanism has to be disabled. Look into msdos.c */
    
   public static int strwildcmp(String sp1,String sp2) //To be checked!
   {
           char[] s1 = new char[9];
           char[] s2 = new char[9];
           int i, l1, l2;
           //char *p;

           strncpy(s1, sp1, 8); s1[8] = 0; if (s1[0] == 0) strcpy(s1, "*");

           strncpy(s2, sp2, 8); s2[8] = 0; if (s2[0] == 0) strcpy(s2, "*");

           /*p = strchr(s1, '*');
           if (p)
           {
                   for (i = p - s1; i < 8; i++) s1[i] = '?';
                   s1[8] = 0;
           }

           p = strchr(s2, '*');
           if (p)
           {
                   for (i = p - s2; i < 8; i++) s2[i] = '?';
                   s2[8] = 0;
           }*/
           l1 = strlen(s1);
           if (l1 < 8)
           {
                   for (i = l1 + 1; i < 8; i++) s1[i] = ' ';
                   s1[8] = 0;
           }

           l2 = strlen(s2);
           if (l2 < 8)
           {
                   for (i = l2 + 1; i < 8; i++) s2[i] = ' ';
                   s2[8] = 0;
           }

           for (i = 0; i < 8; i++)
           {
                   if (s1[i] == '?' && s2[i] != '?') s1[i] = s2[i];
                   if (s2[i] == '?' && s1[i] != '?') s2[i] = s1[i];
           }

           return stricmp(s1, s2);
   }


    /* Identifies a rom from from this checksum */
 /*TODO*/ //     void identify_rom(const char* name, int checksum, int length)
 /*TODO*/ //     {
    /* Nicola output format */
  /*TODO*/ //            int found = 0;

            /* remove directory name */
  /*TODO*/ //            int i;
  /*TODO*/ //            for (i = strlen(name)-1;i >= 0;i--)
  /*TODO*/ //            {
   /*TODO*/ //                   if (name[i] == '/' || name[i] == '\\')
   /*TODO*/ //                   {
   /*TODO*/ //                           i++;
   /*TODO*/ //                           break;
   /*TODO*/ //                   }
   /*TODO*/ //           }
   /*TODO*/ //           if (!silentident)
     /*TODO*/ //                 printf("%-12s ",&name[i]);

    /*TODO*/ //          for (i = 0; drivers[i]; i++)
    /*TODO*/ //          {
    /*TODO*/ //                  const struct RomModule *romp;

    /*TODO*/ //                  romp = drivers[i].rom;

    /*TODO*/ //                  while (romp && (romp.name || romp.offset || romp.length))
    /*TODO*/ //                  {
   /*TODO*/ //                           if (romp.name && romp.name != (char *)-1 && checksum == romp.crc)
     /*TODO*/ //                         {
     /*TODO*/ //                                 if (!silentident)
     /*TODO*/ //                                 {
      /*TODO*/ //                                        if (found != 0)
      /*TODO*/ //                                                printf("             ");
     /*TODO*/ //                                         printf("= %-12s  %s\n",romp.name,drivers[i].description);
     /*TODO*/ //                                 }
     /*TODO*/ //                                 found++;
      /*TODO*/ //                        }
     /*TODO*/ //                         romp++;
     /*TODO*/ //                 }
 /*TODO*/ //             }
 /*TODO*/ //             if (found == 0)
 /*TODO*/ //             {
 /*TODO*/ //                     unsigned size = length;
  /*TODO*/ //                    while (size && (size & 1) == 0) size >>= 1;
  /*TODO*/ //                    if (size & ~1)
  /*TODO*/ //                    {
  /*TODO*/ //                            if (!silentident)
  /*TODO*/ //                                    printf("NOT A ROM\n");
  /*TODO*/ //                    }
  /*TODO*/ //                    else
  /*TODO*/ //                    {
  /*TODO*/ //                            if (!silentident)
  /*TODO*/ //                                    printf("NO MATCH\n");
 /*TODO*/ //                             if (knownstatus == KNOWN_START)
 /*TODO*/ //                                     knownstatus = KNOWN_NONE;
 /*TODO*/ //                             else if (knownstatus == KNOWN_ALL)
 /*TODO*/ //                                     knownstatus = KNOWN_SOME;
 /*TODO*/ //                     }
 /*TODO*/ //             }
 /*TODO*/ //             else
 /*TODO*/ //             {
 /*TODO*/ //                     if (knownstatus == KNOWN_START)
 /*TODO*/ //                             knownstatus = KNOWN_ALL;
 /*TODO*/ //                     else if (knownstatus == KNOWN_NONE)
 /*TODO*/ //                             knownstatus = KNOWN_SOME;
 /*TODO*/ //             }
 /*TODO*/ //     }

    /* Identifies a file from from this checksum */
  /*TODO*/ //    void identify_file(const char* name)
  /*TODO*/ //    {
  /*TODO*/ //            FILE *f;
  /*TODO*/ //            int length;
  /*TODO*/ //            char* data;
 /*TODO*/ // 
  /*TODO*/ //            f = fopen(name,"rb");
  /*TODO*/ //            if (!f) {
  /*TODO*/ //                    return;
   /*TODO*/ //           }

            /* determine length of file */
   /*TODO*/ //           if (fseek (f, 0L, SEEK_END)!=0)	{
   /*TODO*/ //                   fclose(f);
   /*TODO*/ //                   return;
   /*TODO*/ //           }

   /*TODO*/ //           length = ftell(f);
   /*TODO*/ //           if (length == -1L) {
   /*TODO*/ //                   fclose(f);
   /*TODO*/ //                   return;
   /*TODO*/ //           }

            /* empty file */
    /*TODO*/ //          if (!length) {
    /*TODO*/ //                  fclose(f);
    /*TODO*/ //                  return;
    /*TODO*/ //          }

            /* allocate space for entire file */
  /*TODO*/ //            data = (char*)malloc(length);
  /*TODO*/ //            if (!data) {
  /*TODO*/ //                    fclose(f);
  /*TODO*/ //                    return;
  /*TODO*/ //            }

  /*TODO*/ //            if (fseek (f, 0L, SEEK_SET)!=0) {
  /*TODO*/ //                    free(data);
  /*TODO*/ //                    fclose(f);
  /*TODO*/ //                    return;
  /*TODO*/ //            }

  /*TODO*/ //            if (fread(data, 1, length, f) != length) {
  /*TODO*/ //                    free(data);
  /*TODO*/ //                    fclose(f);
  /*TODO*/ //                    return;
  /*TODO*/ //            }

   /*TODO*/ //           fclose(f);

   /*TODO*/ //           identify_rom(name, crc32(0L,(const unsigned char*)data,length),length);

   /*TODO*/ //           free(data);
   /*TODO*/ //   }

  /*TODO*/ //    void identify_zip(const char* zipname)
  /*TODO*/ //    {
  /*TODO*/ //            struct zipent* ent;

  /*TODO*/ //            ZIP* zip = openzip( zipname );
  /*TODO*/ //            if (!zip)
  /*TODO*/ //                    return;

   /*TODO*/ //           while ((ent = readzip(zip))) {
                    /* Skip empty file and directory */
   /*TODO*/ //                   if (ent.uncompressed_size!=0) {
   /*TODO*/ //                           char* buf = (char*)malloc(strlen(zipname)+1+strlen(ent.name)+1);
   /*TODO*/ //                           sprintf(buf,"%s/%s",zipname,ent.name);
   /*TODO*/ //                           identify_rom(buf,ent.crc32,ent.uncompressed_size);
   /*TODO*/ //                           free(buf);
    /*TODO*/ //                  }
    /*TODO*/ //          }

    /*TODO*/ //          closezip(zip);
   /*TODO*/ //   }

  

  /*TODO*/ //    void identify_dir(const char* dirname)
  /*TODO*/ //    {
  /*TODO*/ //            DIR *dir;
  /*TODO*/ //            struct dirent *ent;

  /*TODO*/ //            dir = opendir(dirname);
  /*TODO*/ //            if (!dir) {
   /*TODO*/ //                   return;
  /*TODO*/ //            }

  /*TODO*/ //            ent = readdir(dir);
  /*TODO*/ //            while (ent) {
                    /* Skip special files */
  /*TODO*/ //                    if (ent.d_name[0]!='.') {
 /*TODO*/ //                             char* buf = (char*)malloc(strlen(dirname)+1+strlen(ent.d_name)+1);
  /*TODO*/ //                            sprintf(buf,"%s/%s",dirname,ent.d_name);
  /*TODO*/ //                            romident(buf,0);
  /*TODO*/ //                            free(buf);
  /*TODO*/ //                    }

   /*TODO*/ //                   ent = readdir(dir);
   /*TODO*/ //           }
  /*TODO*/ //            closedir(dir);
  /*TODO*/ //    }

  /*TODO*/ //    void romident(const char* name,int enter_dirs) {
  /*TODO*/ //            struct stat s;

  /*TODO*/ //            if (stat(name,&s) != 0)	{
  /*TODO*/ //                    printf("%s: %s\n",name,strerror(errno));
  /*TODO*/ //                    return;
  /*TODO*/ //            }

  /*TODO*/ //            if (S_ISDIR(s.st_mode)) {
  /*TODO*/ //                    if (enter_dirs)
   /*TODO*/ //                           identify_dir(name);
  /*TODO*/ //            } else {
  /*TODO*/ //                    unsigned l = strlen(name);
   /*TODO*/ //                   if (l>=4 && stricmp(name+l-4,".zip")==0)
   /*TODO*/ //                           identify_zip(name);
   /*TODO*/ //                   else
   /*TODO*/ //                           identify_file(name);
   /*TODO*/ //                   return;
   /*TODO*/ //           }
   /*TODO*/ //   }


    public static final int LIST_LIST = 1; 
    public static final int LIST_LISTINFO=2; 
    public static final int LIST_LISTFULL=3; 
    public static final int LIST_LISTSAMDIR=4; 
    public static final int LIST_LISTROMS=5; 
    public static final int LIST_LISTSAMPLES=6;
    public static final int LIST_LMR=7;
    public static final int LIST_LISTDETAILS=8; 
    public static final int LIST_GAMELISTHEADER=9; 
    public static final int LIST_GAMELISTFOOTER=10; 
    public static final int LIST_GAMELIST=11;
    public static final int LIST_LISTGAMES=12; 
    public static final int LIST_LISTCLONES=13;
    public static final int LIST_WRONGORIENTATION=14;
    public static final int LIST_WRONGFPS=15; 
    public static final int LIST_LISTCRC=16; 
    public static final int LIST_LISTDUPCRC=17; 
    public static final int LIST_WRONGMERGE=18;
    public static final int LIST_LISTROMSIZE=19;
    public static final int LIST_LISTCPU=20; 
    public static final int LIST_SOURCEFILE=21; 


    public static final int VERIFY_ROMS		= 0x00000001;
    public static final int VERIFY_SAMPLES	= 0x00000002;
    public static final int VERIFY_VERBOSE	= 0x00000004;
    public static final int VERIFY_TERSE	= 0x00000008;

    public static int frontend_help(int argc, String[] argv)
    {
            int i, j;
            int list = 0;
            int listclones = 1;
            int verify = 0;
            int ident = 0;
            int help = 1;    /* by default is TRUE */
            String gamename="";//char gamename[9];

            /* covert '/' in '-' */
            for (i = 1; i < argc; i++) {
                if (argv[i].charAt(0) != '/') {
                    argv[i].replaceFirst("/", "-");  
                }
            }
            /* by default display the help unless */
            /* a game or an utility are specified */

           // strcpy(gamename, "");//not neccesary

            for (i = 1;i < argc;i++)
            {
                    /* find the FIRST "gamename" field (without '-') */
                    if ((strlen(gamename) == 0) && (argv[i].charAt(0) != '-'))
                    {
                            /* check if a filename was entered as the game name */
                            /* and remove any remaining portion of file extension */
/*TODO*/ //                     for (j = 0;j < 8 && argv[i][j] && argv[i][j] != '.';j++)
/*TODO*/ //				gamename[j] = argv[i][j];
/*TODO*/ //                     gamename[j] = 0;
/*TEMPHACK*/                 gamename=argv[1];//TO_BE_REMOVED (temp solution but works ok)
                       
                    }
            }

            for (i = 1; i < argc; i++)
            {
                    /* check for front-end utilities */
                    if (stricmp(argv[i],"-list")==0) list = LIST_LIST;
                    if (stricmp(argv[i],"-listinfo")==0) list = LIST_LISTINFO;
                    if (stricmp(argv[i],"-listfull")==0) list = LIST_LISTFULL;
                    if (stricmp(argv[i],"-listdetails")==0) list = LIST_LISTDETAILS; /* A detailed MAMELIST.TXT type roms lister */
                    if (stricmp(argv[i],"-gamelistheader")==0) list = LIST_GAMELISTHEADER; /* GAMELIST.TXT */
                    if (stricmp(argv[i],"-gamelistfooter")==0) list = LIST_GAMELISTFOOTER; /* GAMELIST.TXT */
                    if (stricmp(argv[i],"-gamelist")==0) list = LIST_GAMELIST; /* GAMELIST.TXT */
                    if (stricmp(argv[i],"-listgames")==0) list = LIST_LISTGAMES;
                    if (stricmp(argv[i],"-listclones")==0) list = LIST_LISTCLONES;
                    if (stricmp(argv[i],"-listsamdir")==0) list = LIST_LISTSAMDIR;
                    if (stricmp(argv[i],"-listcrc")==0) list = LIST_LISTCRC;
                    if (stricmp(argv[i],"-listdupcrc")==0) list = LIST_LISTDUPCRC;
                    if (stricmp(argv[i],"-listwrongmerge")==0) list = LIST_WRONGMERGE;
                    if (stricmp(argv[i],"-listromsize")==0) list = LIST_LISTROMSIZE;
                    if (stricmp(argv[i],"-listcpu")==0) list = LIST_LISTCPU;
                    if (stricmp(argv[i],"-lmr")==0) list = LIST_LMR;
                    if (stricmp(argv[i],"-wrongorientation")==0) list = LIST_WRONGORIENTATION;
                    if (stricmp(argv[i],"-wrongfps")==0) list = LIST_WRONGFPS;
                    if (stricmp(argv[i],"-noclones")==0) listclones = 0;

                   /* these options REQUIRES gamename field to work */
                    if (strlen(gamename) > 0)
                    {
                            if (stricmp(argv[i],"-listroms")==0) list = LIST_LISTROMS;
                            if (stricmp(argv[i],"-listsamples")==0) list = LIST_LISTSAMPLES;
                            if (stricmp(argv[i],"-verifyroms")==0) verify = VERIFY_ROMS;
                            if (stricmp(argv[i],"-verifysets")==0) verify = VERIFY_ROMS|VERIFY_VERBOSE|VERIFY_TERSE;
                            if (stricmp(argv[i],"-vset")==0) verify = VERIFY_ROMS|VERIFY_VERBOSE;
                            if (stricmp(argv[i],"-verifysamples")==0) verify = VERIFY_SAMPLES|VERIFY_VERBOSE;
                            if (stricmp(argv[i],"-vsam")==0) verify = VERIFY_SAMPLES|VERIFY_VERBOSE;
                            if (stricmp(argv[i],"-romident")==0) ident = 1;
                            if (stricmp(argv[i],"-isknown")==0) ident = 2;
                            if (stricmp(argv[i],"-sourcefile")==0) list = LIST_SOURCEFILE;
                    }
            }

            if ((strlen(gamename)> 0) || list!=0 || verify!=0) help = 0;

            for (i = 1;i < argc;i++)
            {
                    /* ...however, I WANT the help! */
                    if (stricmp(argv[i],"-?")==0 || stricmp(argv[i],"-h")==0 || stricmp(argv[i],"-help")==0)
                            help = 1;
            }

            if (help!=0)  /* brief help - useful to get current version info */
            {
                printf(settings.version +" (based on mame v%s)\n", new Object[] { build_version });

                    showdisclaimer();
                    printf("Usage:  java -jar arcadeflex.jar gamename [options]\n"
                           +         "        java -jar arcadeflex.jar -list      for a brief list of supported games\n"
                           +         "        java -jar arcadeflex.jar -listfull  for a full list of supported games\n\n"
                           +         "See readme.txt for a complete list of options.\n");
                    return 0;
            }
            switch (list)  /* front-end utilities ;) */
            {
                case LIST_LIST: /* simple games list */
			printf("\nMAME currently supports the following games:\n\n");
			i = 0; j = 0;
			while (drivers[i]!=null)
			{
                            if ((listclones!=0 || drivers[i].clone_of == null
						|| ((drivers[i].clone_of.flags & NOT_A_DRIVER)!=0)
/*TODO*/				 ) /*&& !strwildcmp(gamename, drivers[i].name)*/)
				{
					printf("%-8s",drivers[i].name);
					j++;
					if ((j % 8)==0) printf("\n");
					else printf("  ");
				}
				i++;
			}
			if ((j % 8)!=0) printf("\n");
			printf("\n");
			if (j != i) printf("Total ROM sets displayed: %4d - ", j);
			printf("Total ROM sets supported: %4d\n", i);
                        return 0;
                case LIST_LISTFULL: /* games list with descriptions */
			printf("Name:     Description:\n");
			i = 0;
			while (drivers[i]!=null)
			{
				if ((listclones!=0 || drivers[i].clone_of == null
						|| (drivers[i].clone_of.flags & NOT_A_DRIVER)!=0
/*TODO*/				 ) /*&& !strwildcmp(gamename, drivers[i].name)*/)
				{
					char[] name=new char[200];

					printf("%-10s",drivers[i].name);

					strcpy(name,drivers[i].description);

					/* Move leading "The" to the end */
 /*TODO*/ //					if (strstr(name," (")) *strstr(name," (") = 0;
 /*TODO*/ //					if (strncmp(name,"The ",4) == 0)
 /*TODO*/ //					{
 /*TODO*/ //						printf("\"%s",name+4);
 /*TODO*/ //						printf(", The");
 /*TODO*/ //					}
 /*TODO*/ //					else
						printf("\"%s",new String(name));

					/* print the additional description only if we are listing clones */
					if (listclones!=0)
					{
 /*TODO*/ //						if (strchr(drivers[i].description,'('))
 /*TODO*/ //							printf(" %s",strchr(drivers[i].description,'('));
					}
					printf("\"\n");
				}
				i++;
			}
			return 0;
                case LIST_LISTSAMDIR: /* games list with samples directories */
			printf("Name:     Samples dir:\n");
			i = 0;
			while (drivers[i]!=null)
			{
				if ((listclones!=0 || drivers[i].clone_of == null
						|| (drivers[i].clone_of.flags & NOT_A_DRIVER)!=0
/*TODO*/				 ) /*&& !strwildcmp(gamename, drivers[i].name)*/)
				{
/*TODO*/ //					for( j = 0; drivers[i].drv.sound[j].sound_type && j < MAX_SOUND; j++ )
/*TODO*/ //					{
/*TODO*/ //						const char **samplenames;
/*TODO*/ //						if( drivers[i].drv.sound[j].sound_type != SOUND_SAMPLES )
/*TODO*/ //							continue;
/*TODO*/ //						samplenames = ((struct Samplesinterface *)drivers[i].drv.sound[j].sound_interface).samplenames;
/*TODO*/ //						if (samplenames != 0 && samplenames[0] != 0)
/*TODO*/ //						{
/*TODO*/ //							printf("%-10s",drivers[i].name);
/*TODO*/ //							if (samplenames[0][0] == '*')
/*TODO*/ //								printf("%s\n",samplenames[0]+1);
/*TODO*/ //							else
/*TODO*/ //								printf("%s\n",drivers[i].name);
/*TODO*/ //						}
/*TODO*/ //					}
				}
				i++;
			}
			return 0;
                case LIST_LISTROMS: /* game roms list or */
		case LIST_LISTSAMPLES: /* game samples list */
			j = 0;
			while (drivers[j]!=null && (stricmp(gamename,drivers[j].name) != 0))
				j++;
			if (drivers[j] == null)
			{
				printf("Game \"%s\" not supported!\n",gamename);
				return 1;
			}
			gamedrv = drivers[j];
			if (list == LIST_LISTROMS)
                        {
	   		   printromlist(gamedrv.rom,gamename);
                        }
			else
			{
				int k;
/*TODO*/ //				for( k = 0; gamedrv.drv.sound[k].sound_type && k < MAX_SOUND; k++ )
/*TODO*/ //				{
/*TODO*/ //					const char **samplenames;
/*TODO*/ //					if( gamedrv.drv.sound[k].sound_type != SOUND_SAMPLES )
/*TODO*/ //						continue;
/*TODO*/ //					samplenames = ((struct Samplesinterface *)gamedrv.drv.sound[k].sound_interface).samplenames;
/*TODO*/ //					if (samplenames != 0 && samplenames[0] != 0)
/*TODO*/ //					{
/*TODO*/ //						i = 0;
/*TODO*/ //						while (samplenames[i] != 0)
/*TODO*/ //						{
/*TODO*/ //							printf("%s\n",samplenames[i]);
/*TODO*/ //							i++;
/*TODO*/ //						}
/*TODO*/ //					}
/*TODO*/ //                                }
			}
			return 0;
                 case LIST_LMR:
			{
				int total;

				total = 0;
				for (i = 0; drivers[i]!=null; i++)
						total++;
				for (i = 0; drivers[i]!=null; i++)
				{
/*TODO*/ // 					static int first_missing = 1;
/*TODO*/ // 					get_rom_sample_path (argc, argv, i);
/*TODO*/ // 					if (RomsetMissing (i))
/*TODO*/ // 					{
/*TODO*/ // 						if (first_missing)
/*TODO*/ // 						{
/*TODO*/ // 							first_missing = 0;
/*TODO*/ // 							printf ("game      clone of  description\n");
/*TODO*/ // 							printf ("--------  --------  -----------\n");
/*TODO*/ // 						}
/*TODO*/ // 						printf ("%-10s%-10s%s\n",
/*TODO*/ // 								drivers[i].name,
/*TODO*/ // 								(drivers[i].clone_of) ? drivers[i].clone_of.name : "",
/*TODO*/ // 								drivers[i].description);
/*TODO*/ // 					}
/*TODO*/ // 					fprintf(stderr,"%d%%\r",100 * (i+1) / total);
				}
			}
			return 0;
                   case LIST_LISTDETAILS: /* A detailed MAMELIST.TXT type roms lister */

			/* First, we shall print the header */

			printf(" romname driver     ");
			for(j=0;j<MAX_CPU;j++) printf("cpu %d    ",j+1);
			for(j=0;j<MAX_SOUND;j++) printf("sound %d     ",j+1);
			printf("name\n");
			printf("-------- ---------- ");
			for(j=0;j<MAX_CPU;j++) printf("-------- ");
			for(j=0;j<MAX_SOUND;j++) printf("----------- ");
			printf("--------------------------\n");

			/* Let's cycle through the drivers */

			i = 0;

			while (drivers[i]!=null)
			{
				if ((listclones!=0 || drivers[i].clone_of == null
						|| (drivers[i].clone_of.flags & NOT_A_DRIVER)!=0
/*TODO*/					) /*&& !strwildcmp(gamename, drivers[i].name)*/)
				{
					/* Dummy structs to fetch the information from */

      				        MachineDriver x_driver = drivers[i].drv;
                                        MachineCPU[] x_cpu = x_driver.cpu;
/*TODO*/ //					const struct MachineSound *x_sound = x_driver.sound;

                                        
                                        
					/* First, the rom name */

					printf("%-8s ",drivers[i].name);

					/* source file (skip the leading "src/drivers/" */
					printf("%-10s ",drivers[i].source_file);

					/* Then, cpus */

					for(j=0;j<MAX_CPU;j++)
					{
/*TODO*/ //						if ((x_cpu[j].cpu_type & CPU_AUDIO_CPU)!=0)                                                
/*TODO*/ //							printf("[%-6s] ",cputype_name(x_cpu[j].cpu_type));
/*TODO*/ //						else
/*TODO*/ //							printf("%-8s ",cputype_name(x_cpu[j].cpu_type));
			/*tempHACK*/        printf("%-8s ","");
                                        }

					/* Then, sound chips */

					for(j=0;j<MAX_SOUND;j++)
					{
/*TODO*/ //						if (sound_num(&x_sound[j]))
/*TODO*/ //						{
/*TODO*/ //							printf("%dx",sound_num(&x_sound[j]));
/*TODO*/ //							printf("%-9s ",sound_name(&x_sound[j]));
/*TODO*/ //						}
/*TODO*/ //						else
/*TODO*/ //							printf("%-11s ",sound_name(&x_sound[j]));
                    /*tempHACK*/           printf("%-11s ","");
					}

					/* Lastly, the name of the game and a \newline */

					printf("%s\n",drivers[i].description);
				}
				i++;
			}
			return 0;
		case LIST_GAMELISTHEADER: /* GAMELIST.TXT */
			printf("This is the complete list of games supported by MAME %s\n",build_version);
			if (listclones==0)
				printf("Variants of the same game are not included, you can use the -listclones command\n"
				+	"to get a list of the alternate versions of a given game.\n");
			printf("\n"
			+	"The list is generated automatically and is not 100%% accurate, particularly in\n"
			+	"the \"Screen Flip\" column. Please let us know of any errors you find so we can\n"
			+	"correct them.\n"
			+	"\n"
			+	"The meanings of the columns are as follows:\n"
			+	"Working - \"No\" means that the emulation has shortcomings that cause the game\n"
			+	"  not to work correctly. This can be anywhere from just showing a black screen\n"
			+	"  to being playable with major problems.\n"
			+	"Correct Colors - \"Yes\" means that colors should be identical to the original,\n"
			+	"  \"Close\" that they are very similar but wrong in places, \"No\" that they are\n"
			+	"  completely wrong. In some cases, we were not able to find the color PROMs of\n"
			+	"  the game. Those PROMs will be reported as \"NO GOOD DUMP KNOWN\" on startup,\n"
			+	"  and the game will have wrong colors. The game is still reported as \"Yes\" in\n"
			+	"  this column, because the code to handle the color PROMs is in the driver and\n"
			+	"  if you provide them colors will be correct.\n"
			+	"Sound - \"Partial\" means that sound support is either incomplete or not entirely\n"
			+	"  accurate. Note that, due to analog circuitry which is difficult to emulate,\n"
			+	"  sound may be significantly different from the real board. A common case is\n"
			+	"  the presence of low pass filters that make the real board sound less harsh\n"
			+	"  than the emulation.\n"
			+	"Screen Flip - A large number of games have a dip switch setting for \"Cocktail\"\n"
			+	"  cabinet, meaning that the players sit in front of each other, and the screen\n"
			+	"  is flipped when player 2 is playing. Some games also have a \"Flip Screen\" dip\n"
			+	"  switch. Those need special support in the driver, which is missing in many\n"
			+	"  cases.\n"
			+	"Internal Name - This is the unique name that should be specified on the command\n"
			+	"  line to run the game. ROMs must be placed in the ROM path, either in a .zip\n"
			+	"  file or in a subdirectory of the same name. The former is suggested, because\n"
			+	"  the files will be identified by their CRC instead of requiring specific\n"
			+	"  names.\n\n");
			printf("+----------------------------------+-------+-------+-------+-------+----------+\n");
			printf("|                                  |       |Correct|       |Screen | Internal |\n");
			printf("| Game Name                        |Working|Colors | Sound | Flip  |   Name   |\n");
			printf("+----------------------------------+-------+-------+-------+-------+----------+\n");
			return 0;

		case LIST_GAMELISTFOOTER: /* GAMELIST.TXT */
			printf("+----------------------------------+-------+-------+-------+-------+----------+\n\n");
			printf("(1) There are variants of the game (usually bootlegs) that work correctly\n");
			printf("(2) Needs samples provided separately\n");
			return 0;

		case LIST_GAMELIST: /* GAMELIST.TXT */
			i = 0;

			while (drivers[i]!=null)
			{
/*TODO*/ //				if ((listclones || drivers[i].clone_of == 0
/*TODO*/ //						|| (drivers[i].clone_of.flags & NOT_A_DRIVER)
/*TODO*/ //						) && !strwildcmp(gamename, drivers[i].name))
/*TODO*/ //				{
/*TODO*/ //					char name[200],name_ref[200];
/*TODO*/ //
/*TODO*/ //					strcpy(name,drivers[i].description);
/*TODO*/ //
/*TODO*/ //					/* Move leading "The" to the end */
/*TODO*/ //					if (strstr(name," (")) *strstr(name," (") = 0;
/*TODO*/ //					if (strncmp(name,"The ",4) == 0)
/*TODO*/ //					{
/*TODO*/ //						sprintf(name_ref,"%s, The ",name+4);
/*TODO*/ //					}
/*TODO*/ //					else
/*TODO*/ //						sprintf(name_ref,"%s ",name);
/*TODO*/ //
/*TODO*/ //					/* print the additional description only if we are listing clones */
/*TODO*/ //					if (listclones)
/*TODO*/ //					{
/*TODO*/ //						if (strchr(drivers[i].description,'('))
/*TODO*/ //							strcat(name_ref,strchr(drivers[i].description,'('));
/*TODO*/ //					}
/*TODO*/ //
/*TODO*/ //					printf("| %-33.33s",name_ref);

/*TODO*/ //					if (drivers[i].flags & GAME_NOT_WORKING)
/*TODO*/ //					{
/*TODO*/ //						const struct GameDriver *maindrv;
/*TODO*/ //						int foundworking;

/*TODO*/ //						if (drivers[i].clone_of && !(drivers[i].clone_of.flags & NOT_A_DRIVER))
/*TODO*/ //							maindrv = drivers[i].clone_of;
/*TODO*/ //						else maindrv = drivers[i];

/*TODO*/ //						foundworking = 0;
/*TODO*/ //						j = 0;
/*TODO*/ //						while (drivers[j])
/*TODO*/ //						{
/*TODO*/ //							if (drivers[j] == maindrv || drivers[j].clone_of == maindrv)
/*TODO*/ //							{
/*TODO*/ //								if ((drivers[j].flags & GAME_NOT_WORKING) == 0)
/*TODO*/ //								{
/*TODO*/ //									foundworking = 1;
/*TODO*/ //									break;
/*TODO*/ //								}
/*TODO*/ //							}
/*TODO*/ //							j++;
/*TODO*/ //						}
/*TODO*/ //
/*TODO*/ //						if (foundworking)
/*TODO*/ //							printf("| No(1) ");
/*TODO*/ //						else
/*TODO*/ //							printf("|   No  ");
/*TODO*/ //					}
/*TODO*/ //					else
/*TODO*/ //						printf("|  Yes  ");
/*TODO*/ //
/*TODO*/ //					if (drivers[i].flags & GAME_WRONG_COLORS)
/*TODO*/ //						printf("|   No  ");
/*TODO*/ //					else if (drivers[i].flags & GAME_IMPERFECT_COLORS)
/*TODO*/ //						printf("| Close ");
/*TODO*/ //					else
/*TODO*/ //						printf("|  Yes  ");

/*TODO*/ //					{
/*TODO*/ //						const char **samplenames = 0;
/*TODO*/ //						for (j = 0;drivers[i].drv.sound[j].sound_type && j < MAX_SOUND; j++)
/*TODO*/ //						{
/*TODO*/ //							if (drivers[i].drv.sound[j].sound_type == SOUND_SAMPLES)
/*TODO*/ //							{
/*TODO*/ //								samplenames = ((struct Samplesinterface *)drivers[i].drv.sound[j].sound_interface).samplenames;
/*TODO*/ //								break;
/*TODO*/ //							}
/*TODO*/ //						}
/*TODO*/ //						if (drivers[i].flags & GAME_NO_SOUND)
/*TODO*/ //							printf("|   No  ");
/*TODO*/ //						else if (drivers[i].flags & GAME_IMPERFECT_SOUND)
/*TODO*/ //						{
/*TODO*/ //							if (samplenames)
/*TODO*/ //								printf("|Part(2)");
/*TODO*/ //							else
/*TODO*/ //								printf("|Partial");
/*TODO*/ //						}
/*TODO*/ //						else
/*TODO*/ //						{
/*TODO*/ //							if (samplenames)
/*TODO*/ //								printf("| Yes(2)");
/*TODO*/ //							else
/*TODO*/ //								printf("|  Yes  ");
/*TODO*/ //						}
/*TODO*/ //					}
/*TODO*/ //
/*TODO*/ //					if (drivers[i].flags & GAME_NO_COCKTAIL)
/*TODO*/ //						printf("|   No  ");
/*TODO*/ //					else
/*TODO*/ //						printf("|  Yes  ");
/*TODO*/ //
/*TODO*/ //					printf("| %-8s |\n",drivers[i].name);
/*TODO*/ //				}
				i++;
			}
			return 0;


		case LIST_LISTGAMES: /* list games, production year, manufacturer */
			i = 0;
			while (drivers[i]!=null)
			{
				if ((listclones!=0 || drivers[i].clone_of == null
						|| ((drivers[i].clone_of.flags & NOT_A_DRIVER)!=0)
/*TODO*/					) /*&& !strwildcmp(gamename, drivers[i].description)*/)
				{
     					       char name[]=new char[200];

					printf("%-5s%-36s ",drivers[i].year,drivers[i].manufacturer);
 
					strcpy(name,drivers[i].description);

					/* Move leading "The" to the end */
/*TODO*/ //					if (strstr(name," (")) *strstr(name," (") = 0;
/*TODO*/ //					if (strncmp(name,"The ",4) == 0)
/*TODO*/ //					{
/*TODO*/ //						printf("%s",name+4);
/*TODO*/ //						printf(", The");
/*TODO*/ //					}
/*TODO*/ //					else
   						   printf("%s",new String(name).trim());

					/* print the additional description only if we are listing clones */
/*TODO*/ //					if (listclones)
/*TODO*/ //					{
/*TODO*/ //						if (strchr(drivers[i].description,'('))
/*TODO*/ //							printf(" %s",strchr(drivers[i].description,'('));
/*TODO*/ //					}
   					        printf("\n");
				}
				i++;
			}
			return 0;
		case LIST_LISTCLONES: /* list clones */
			printf("Name:    Clone of:\n");
			i = 0;
			while (drivers[i]!=null)
			{
				if (drivers[i].clone_of!=null && (drivers[i].clone_of.flags & NOT_A_DRIVER)==0 /*&&
/*TODO*/ 						/*(!strwildcmp(gamename,drivers[i].name)
/*TODO*/ 								/*|| !strwildcmp(gamename,drivers[i].clone_of.name))*/)
					printf("%-8s %-8s\n",drivers[i].name,drivers[i].clone_of.name);
				i++;
			}
			return 0;

		case LIST_WRONGORIENTATION: /* list drivers which incorrectly use the orientation and visible area fields */
			while (drivers[i]!=null)
			{
/*TODO*/ //				if ((drivers[i].drv.video_attributes & VIDEO_TYPE_VECTOR) == 0 &&
/*TODO*/ //						(drivers[i].clone_of == 0
/*TODO*/ //								|| (drivers[i].clone_of.flags & NOT_A_DRIVER)) &&
/*TODO*/ //						drivers[i].drv.visible_area.max_x - drivers[i].drv.visible_area.min_x + 1 <=
/*TODO*/ //						drivers[i].drv.visible_area.max_y - drivers[i].drv.visible_area.min_y + 1)
/*TODO*/ //				{
/*TODO*/ //					if (strcmp(drivers[i].name,"crater") &&
/*TODO*/ //						strcmp(drivers[i].name,"mpatrol") &&
/*TODO*/ //						strcmp(drivers[i].name,"troangel") &&
/*TODO*/ //						strcmp(drivers[i].name,"travrusa") &&
/*TODO*/ //						strcmp(drivers[i].name,"kungfum") &&
/*TODO*/ //						strcmp(drivers[i].name,"battroad") &&
/*TODO*/ //						strcmp(drivers[i].name,"vigilant") &&
/*TODO*/ //						strcmp(drivers[i].name,"sonson") &&
/*TODO*/ //						strcmp(drivers[i].name,"brkthru") &&
/*TODO*/ //						strcmp(drivers[i].name,"darwin") &&
/*TODO*/ //						strcmp(drivers[i].name,"exprraid") &&
/*TODO*/ //						strcmp(drivers[i].name,"sidetrac") &&
/*TODO*/ //						strcmp(drivers[i].name,"targ") &&
/*TODO*/ //						strcmp(drivers[i].name,"spectar") &&
/*TODO*/ //						strcmp(drivers[i].name,"venture") &&
/*TODO*/ //						strcmp(drivers[i].name,"mtrap") &&
/*TODO*/ //						strcmp(drivers[i].name,"pepper2") &&
/*TODO*/ //						strcmp(drivers[i].name,"hardhat") &&
/*TODO*/ //						strcmp(drivers[i].name,"fax") &&
/*TODO*/ //						strcmp(drivers[i].name,"circus") &&
/*TODO*/ //						strcmp(drivers[i].name,"robotbwl") &&
/*TODO*/ //						strcmp(drivers[i].name,"crash") &&
/*TODO*/ //						strcmp(drivers[i].name,"ripcord") &&
/*TODO*/ //						strcmp(drivers[i].name,"starfire") &&
/*TODO*/ //						strcmp(drivers[i].name,"fireone") &&
/*TODO*/ //						strcmp(drivers[i].name,"renegade") &&
/*TODO*/ //						strcmp(drivers[i].name,"battlane") &&
/*TODO*/ //						strcmp(drivers[i].name,"megatack") &&
/*TODO*/ //						strcmp(drivers[i].name,"killcom") &&
/*TODO*/ //						strcmp(drivers[i].name,"challeng") &&
/*TODO*/ //						strcmp(drivers[i].name,"kaos") &&
/*TODO*/ //						strcmp(drivers[i].name,"formatz") &&
/*TODO*/ //						strcmp(drivers[i].name,"bankp") &&
/*TODO*/ //						strcmp(drivers[i].name,"liberatr") &&
/*TODO*/ //						strcmp(drivers[i].name,"toki") &&
/*TODO*/ //						strcmp(drivers[i].name,"stactics") &&
/*TODO*/ //						strcmp(drivers[i].name,"sprint1") &&
/*TODO*/ //						strcmp(drivers[i].name,"sprint2") &&
/*TODO*/ //						strcmp(drivers[i].name,"nitedrvr") &&
/*TODO*/ //						strcmp(drivers[i].name,"punchout") &&
/*TODO*/ //						strcmp(drivers[i].name,"spnchout") &&
/*TODO*/ //						strcmp(drivers[i].name,"armwrest") &&
/*TODO*/ //						strcmp(drivers[i].name,"route16") &&
/*TODO*/ //						strcmp(drivers[i].name,"stratvox") &&
/*TODO*/ //						strcmp(drivers[i].name,"irobot") &&
/*TODO*/ //						strcmp(drivers[i].name,"leprechn") &&
/*TODO*/ //						strcmp(drivers[i].name,"starcrus") &&
/*TODO*/ //						strcmp(drivers[i].name,"astrof") &&
/*TODO*/ //						strcmp(drivers[i].name,"tomahawk") &&
/*TODO*/ //						1)
/*TODO*/ //						printf("%s %dx%d\n",drivers[i].name,
/*TODO*/ //								drivers[i].drv.visible_area.max_x - drivers[i].drv.visible_area.min_x + 1,
/*TODO*/ //								drivers[i].drv.visible_area.max_y - drivers[i].drv.visible_area.min_y + 1);
/*TODO*/ //				}
				i++;
			}
			return 0;

		case LIST_WRONGFPS: /* list drivers with too high frame rate */
/*TODO*/ //			while (drivers[i])
/*TODO*/ //			{
/*TODO*/ //				if ((drivers[i].drv.video_attributes & VIDEO_TYPE_VECTOR) == 0 &&
/*TODO*/ //						(drivers[i].clone_of == 0
/*TODO*/ //								|| (drivers[i].clone_of.flags & NOT_A_DRIVER)) &&
/*TODO*/ //						drivers[i].drv.frames_per_second > 57 &&
/*TODO*/ //						drivers[i].drv.visible_area.max_y - drivers[i].drv.visible_area.min_y + 1 > 244 &&
/*TODO*/ //						drivers[i].drv.visible_area.max_y - drivers[i].drv.visible_area.min_y + 1 <= 256)
/*TODO*/ //				{
/*TODO*/ //					printf("%s %dx%d %fHz\n",drivers[i].name,
/*TODO*/ //							drivers[i].drv.visible_area.max_x - drivers[i].drv.visible_area.min_x + 1,
/*TODO*/ //							drivers[i].drv.visible_area.max_y - drivers[i].drv.visible_area.min_y + 1,
/*TODO*/ //							drivers[i].drv.frames_per_second);
/*TODO*/ //				}
/*TODO*/ //				i++;
/*TODO*/ //			}
			return 0;

		case LIST_SOURCEFILE:
/*TODO*/ //			i = 0;
/*TODO*/ //			while (drivers[i])
/*TODO*/ //			{
/*TODO*/ //				if (!strwildcmp(gamename,drivers[i].name))
/*TODO*/ //					printf("%-8s %s\n",drivers[i].name,drivers[i].source_file);
/*TODO*/ //				i++;
/*TODO*/ //			}
			return 0;

		case LIST_LISTCRC: /* list all crc-32 */
			i = 0;
			while (drivers[i]!=null)
			{
                              
   				
                                RomModule[] romp;
                                int romp_ptr=0;
				romp = drivers[i].rom;

				while (romp!=null && (romp[romp_ptr].name!=null || romp[romp_ptr].offset!=0 || romp[romp_ptr].length!=0))
				{
					if (romp[romp_ptr].name!=null && romp[romp_ptr].name != "-1")
						printf("%08x %-12s %s\n",romp[romp_ptr].crc,romp[romp_ptr].name,drivers[i].description);

					romp_ptr++;
				}
				i++;
			}
			return 0;

		case LIST_LISTDUPCRC: /* list duplicate crc-32 (with different ROM name) */
			i = 0;
			while (drivers[i]!=null)
			{
				RomModule[] romp;
                                 int romp_ptr=0;

				romp = drivers[i].rom;

				while (romp!=null && (romp[romp_ptr].name!=null || romp[romp_ptr].offset!=0 || romp[romp_ptr].length!=0))
				{
					if (romp[romp_ptr].name!=null && romp[romp_ptr].name != "-1" && romp[romp_ptr].crc!=0)
					{
						j = i+1;
						while (drivers[j]!=null)
						{
							RomModule[] romp1;

							romp1 = drivers[j].rom;
                                                         int romp1_ptr=0;
                                                        while (romp1!=null && (romp1[romp1_ptr].name!=null || romp1[romp1_ptr].offset!=0 || romp1[romp1_ptr].length!=0))
							{
								if (romp1[romp1_ptr].name!=null && romp1[romp1_ptr].name!="-1" &&
										strcmp(romp[romp_ptr].name,romp1[romp1_ptr].name)!=0 &&
										romp1[romp1_ptr].crc == romp[romp_ptr].crc)
								{
									printf("%08x %-12s %-8s <. %-12s %-8s\n",romp[romp_ptr].crc,
											romp[romp_ptr].name,drivers[i].name,
											romp1[romp1_ptr].name,drivers[j].name);
								}

								romp1_ptr++;
							}

							j++;
						}
					}

					romp_ptr++;
				}

				i++;
			}
			return 0;

		case LIST_WRONGMERGE: /* list duplicate crc-32 with different ROM name in clone sets */
			i = 0;
			while (drivers[i]!=null)
			{
				RomModule[] romp;
                                int romp_ptr=0;

				romp = drivers[i].rom;

				while (romp!=null && (romp[romp_ptr].name!=null || romp[romp_ptr].offset!=0 || romp[romp_ptr].length!=0))
				{
					if (romp[romp_ptr].name!=null && romp[romp_ptr].name != "-1" && romp[romp_ptr].crc!=0)
					{
						j = 0;
						while (drivers[j]!=null)
						{
							if (j != i &&
								drivers[j].clone_of!=null &&
								(drivers[j].clone_of.flags & NOT_A_DRIVER) == 0 &&
								(drivers[j].clone_of == drivers[i] ||
								(i < j && drivers[j].clone_of == drivers[i].clone_of)))
							{
								RomModule[] romp1;

								int match;
                                                                

							        romp1 = drivers[j].rom;
                                                                int romp1_ptr=0;
								match = 0;

								while (romp1!=null && (romp1[romp1_ptr].name!=null || romp1[romp1_ptr].offset!=0 || romp1[romp1_ptr].length!=0))
								{
									if (romp1[romp1_ptr].name!=null && romp1[romp1_ptr].name != "-1" &&
											(strcmp(romp[romp_ptr].name,romp1[romp1_ptr].name)==0))
									{
										match = 1;
										break;
									}

									romp1_ptr++;
								}

								if (match == 0)
								{
									romp1_ptr=0;//romp1 = drivers[j].rom;

									while (romp1!=null && (romp1[romp1_ptr].name!=null || romp1[romp1_ptr].offset!=0 || romp1[romp1_ptr].length!=0))
									{
                                                                            if (romp1[romp1_ptr].name!=null && romp1[romp1_ptr].name != "-1" &&
												(strcmp(romp[romp_ptr].name,romp1[romp1_ptr].name)!=0) &&
												romp1[romp1_ptr].crc == romp[romp_ptr].crc)
										{
											printf("%08x %-12s %-8s <. %-12s %-8s\n",romp[romp_ptr].crc,
													romp[romp_ptr].name,drivers[i].name,
													romp1[romp1_ptr].name,drivers[j].name);
										}

										romp1_ptr++;
									}
								}
							}
							j++;
						}
					}

					romp_ptr++;
				}

				i++;
			}
			return 0;

		case LIST_LISTROMSIZE: /* I used this for statistical analysis */
			i = 0;
			while (drivers[i]!=null)
			{
				if (drivers[i].clone_of == null || ((drivers[i].clone_of.flags & NOT_A_DRIVER)!=0))
				{
                                        RomModule[] romp;
                                        int romp_ptr=0;
                                        
                                        j = 0;
                                        
                                        romp = drivers[i].rom;
					while (romp!=null && (romp[romp_ptr].name!=null || romp[romp_ptr].offset!=0 || romp[romp_ptr].length!=0))
					{
						j += romp[romp_ptr].length & ~ROMFLAG_MASK;

						romp_ptr++;
					}
					printf("%-8s\t%-5s\t%d\n",drivers[i].name,drivers[i].year,j);
				}

				i++;
			}
			return 0;

		case LIST_LISTCPU: /* I used this for statistical analysis */
/*TODO*/ //			{
/*TODO*/ //				int year;

/*TODO*/ //				for (j = 1;j < CPU_COUNT;j++)
/*TODO*/ //					printf("\t%s",cputype_name(j));
/*TODO*/ //				printf("\n");
/*TODO*/ //
/*TODO*/ //				for (year = 1980;year <= 1995;year++)
/*TODO*/ //				{
/*TODO*/ //					int count[CPU_COUNT];
/*TODO*/ //
/*TODO*/ //					for (j = 0;j < CPU_COUNT;j++)
/*TODO*/ //						count[j] = 0;
/*TODO*/ //
/*TODO*/ //					i = 0;
/*TODO*/ //					while (drivers[i])
/*TODO*/ //					{
/*TODO*/ //						if (drivers[i].clone_of == 0 || (drivers[i].clone_of.flags & NOT_A_DRIVER))
/*TODO*/ //						{
/*TODO*/ //							const struct MachineDriver *x_driver = drivers[i].drv;
/*TODO*/ //							const struct MachineCPU *x_cpu = x_driver.cpu;
/*TODO*/ //
/*TODO*/ //							if (atoi(drivers[i].year) == year)
/*TODO*/ //							{
/*TODO*/ //                                                            //		for (j = 0;j < MAX_CPU;j++)
/*TODO*/ //                                                                        j = 0;	// count only the main cpu
/*TODO*/ //									count[x_cpu[j].cpu_type & ~CPU_FLAGS_MASK]++;
/*TODO*/ //							}
/*TODO*/ //						}
/*TODO*/ //
/*TODO*/ //						i++;
/*TODO*/ //					}
/*TODO*/ //
/*TODO*/ //					printf("%d",year);
/*TODO*/ //					for (j = 1;j < CPU_COUNT;j++)
/*TODO*/ //						printf("\t%d",count[j]);
/*TODO*/ //					printf("\n");
/*TODO*/ //				}
/*TODO*/ //			}

			return 0;

		case LIST_LISTINFO: /* list all info */
/*TODO*/ //			print_mame_info( stdout, drivers );
			return 0;
	}

	if (verify!=0)  /* "verify" utilities */
	{
		int err = 0;
		int correct = 0;
		int incorrect = 0;
		int res = 0;
		int total = 0;
		int checked = 0;
		int notfound = 0;


/*TODO*/ //		for (i = 0; drivers[i]; i++)
/*TODO*/ //		{
/*TODO*/ //			if (!strwildcmp(gamename, drivers[i].name))
/*TODO*/ //				total++;
/*TODO*/ //		}

/*TODO*/ //		for (i = 0; drivers[i]; i++)
/*TODO*/ //		{
/*TODO*/ //			if (strwildcmp(gamename, drivers[i].name))
/*TODO*/ //				continue;

			/* set rom and sample path correctly */
/*TODO*/ //			get_rom_sample_path (argc, argv, i);

/*TODO*/ //			if (verify & VERIFY_ROMS)
/*TODO*/ //			{
/*TODO*/ //				res = VerifyRomSet (i,(verify & VERIFY_TERSE) ? terse_printf : (verify_printf_proc)printf);

/*TODO*/ //				if (res == CLONE_NOTFOUND || res == NOTFOUND)
/*TODO*/ //				{
/*TODO*/ //					notfound++;
/*TODO*/ //					goto nextloop;
/*TODO*/ //				}

/*TODO*/ //				if (res == INCORRECT || res == BEST_AVAILABLE || (verify & VERIFY_VERBOSE))
/*TODO*/ //				{
/*TODO*/ //					printf ("romset %s ", drivers[i].name);
/*TODO*/ //					if (drivers[i].clone_of && !(drivers[i].clone_of.flags & NOT_A_DRIVER))
/*TODO*/ //						printf ("[%s] ", drivers[i].clone_of.name);
/*TODO*/ //				}
/*TODO*/ //			}
/*TODO*/ //			if (verify & VERIFY_SAMPLES)
/*TODO*/ //			{
/*TODO*/ //				const char **samplenames = NULL;
/*TODO*/ //				for( j = 0; drivers[i].drv.sound[j].sound_type && j < MAX_SOUND; j++ )
/*TODO*/ //					if( drivers[i].drv.sound[j].sound_type == SOUND_SAMPLES )
/*TODO*/ //						samplenames = ((struct Samplesinterface *)drivers[i].drv.sound[j].sound_interface).samplenames;
/*TODO*/ //
/*TODO*/ //				/* ignore games that need no samples */
/*TODO*/ //				if (samplenames == 0 || samplenames[0] == 0)
/*TODO*/ //					goto nextloop;
/*TODO*/ //
/*TODO*/ //				res = VerifySampleSet (i,(verify_printf_proc)printf);
/*TODO*/ //				if (res == NOTFOUND)
/*TODO*/ //				{
/*TODO*/ //					notfound++;
/*TODO*/ //					goto nextloop;
/*TODO*/ //				}
/*TODO*/ //				printf ("sampleset %s ", drivers[i].name);
/*TODO*/ //			}
/*TODO*/ //
/*TODO*/ //			if (res == NOTFOUND)
/*TODO*/ //			{
/*TODO*/ //				printf ("oops, should never come along here\n");
/*TODO*/ //			}
/*TODO*/ //			else if (res == INCORRECT)
/*TODO*/ //			{
/*TODO*/ //				printf ("is bad\n");
/*TODO*/ //				incorrect++;
/*TODO*/ //			}
/*TODO*/ //			else if (res == CORRECT)
/*TODO*/ //			{
/*TODO*/ //				if (verify & VERIFY_VERBOSE)
/*TODO*/ //					printf ("is good\n");
/*TODO*/ //				correct++;
/*TODO*/ //			}
/*TODO*/ //			else if (res == BEST_AVAILABLE)
/*TODO*/ //			{
/*TODO*/ //				printf ("is best available\n");
/*TODO*/ //				correct++;
/*TODO*/ //			}
/*TODO*/ //			if (res)
/*TODO*/ //				err = res;
/*TODO*/ //
/*TODO*/ //nextloop:
/*TODO*/ //			checked++;
/*TODO*/ //			fprintf(stderr,"%d%%\r",100 * checked / total);
/*TODO*/ //		}
/*TODO*/ //
/*TODO*/ //		if (correct+incorrect == 0)
/*TODO*/ //		{
/*TODO*/ //			printf ("%s ", (verify & VERIFY_ROMS)!=0 ? "romset" : "sampleset" );
/*TODO*/ //			if (notfound > 0)
/*TODO*/ //				printf("\"%8s\" not found!\n",gamename);
/*TODO*/ //			else
/*TODO*/ //				printf("\"%8s\" not supported!\n",gamename);
/*TODO*/ //			return 1;
/*TODO*/ //		}
/*TODO*/ //		else
/*TODO*/ //		{
/*TODO*/ //			printf("%d %s found, %d were OK.\n", correct+incorrect,
/*TODO*/ //					(verify & VERIFY_ROMS)!=0? "romsets" : "samplesets", correct);
/*TODO*/ //			if (incorrect > 0)
/*TODO*/ //				return 2;
/*TODO*/ //			else
/*TODO*/ //				return 0;
/*TODO*/ //		}
	}
	if (ident!=0)
	{
		if (ident == 2) silentident = 1;
		else silentident = 0;

		for (i = 1;i < argc;i++)
		{
			/* find the FIRST "name" field (without '-') */
			if (argv[i].charAt(0) != '-')
			{
				knownstatus = KNOWN_START;
/*TODO*/ //				romident(argv[i],1);
				if (ident == 2)
				{
					switch (knownstatus)
					{
						case KNOWN_START: printf("ERROR     %s\n",argv[i]); break;
						case KNOWN_ALL:   printf("KNOWN     %s\n",argv[i]); break;
						case KNOWN_NONE:  printf("UNKNOWN   %s\n",argv[i]); break;
						case KNOWN_SOME:  printf("PARTKNOWN %s\n",argv[i]); break;
					}
				}
				break;
			}
		}
		return 0;

            }


            /* use a special return value if no frontend function used */

            return 1234;
    }   
}
