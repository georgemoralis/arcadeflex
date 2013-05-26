/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package osdepend;

/**
 *
 * @author shadow
 */
public class fileio {
    public static Object osd_fopen (String game, String filename, int filetype, int _write)
    {
        return null;
    }
    
    /* called while loading ROMs. It is called a last time with name == 0 to signal */
    /* that the ROM loading process is finished. */
    /* return non-zero to abort loading */
    public static int osd_display_loading_rom_message (String name, int current, int total)
    {
/*            if( name )
                    fprintf (stdout, "loading %-12s\r", name);
            else
                    fprintf (stdout, "                    \r");
            fflush (stdout);

            if( keyboard_pressed (KEYCODE_LCONTROL) && keyboard_pressed (KEYCODE_C) )
                    return 1;*/

            return 0;
    }    
}
