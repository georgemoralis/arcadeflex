
package mame;

import static arcadeflex.libc.*;
import static mame.commonH.*;

public class common {
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
}
