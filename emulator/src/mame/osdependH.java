
package mame;

import static arcadeflex.libc.*;

public class osdependH {
        /******************************************************************************

          Display

        ******************************************************************************/
         public static class osd_bitmap {

            public osd_bitmap() {}
            public int width, height;	/* width and hegiht of the bitmap */
            public int depth;		/* bits per pixel*/
            public Object _private; /* don't touch! - reserved for osdepend use */
            public UBytePtr[] line;		/* pointers to the start of each line */
        };

/*TODO*/ //         #define osd_create_bitmap(w,h) osd_new_bitmap((w),(h),Machine->scrbitmap->depth)		/* ASG 980209 */


        /******************************************************************************

          Keyboard

        ******************************************************************************/

        /* Code returned by the function osd_wait_keypress() if no key available */
        public static final int OSD_KEY_NONE= 0xffffffff;


        /******************************************************************************

          Joystick & Mouse/Trackball

        ******************************************************************************/

        /* We support 4 players for each analog control */
        public static final int OSD_MAX_JOY_ANALOG =	4;
        public static final int X_AXIS             =    1;
        public static final int Y_AXIS             =    2;


        /******************************************************************************

          File I/O

        ******************************************************************************/

        /* inp header */
 /*TODO*/ //        typedef struct {
 /*TODO*/ //            char name[9];      /* 8 bytes for game->name + NULL */
 /*TODO*/ //            char version[3];   /* byte[0] = 0, byte[1] = version byte[2] = beta_version */
 /*TODO*/ //            char reserved[20]; /* for future use, possible store game options? */
  /*TODO*/ //       } INP_HEADER;


        /* file handling routines */

        public static final int OSD_FILETYPE_ROM        = 1;
        public static final int OSD_FILETYPE_SAMPLE     = 2;
        public static final int OSD_FILETYPE_NVRAM      = 3;
        public static final int OSD_FILETYPE_HIGHSCORE  = 4;
        public static final int OSD_FILETYPE_CONFIG     = 5;
        public static final int OSD_FILETYPE_INPUTLOG   = 6;
        public static final int OSD_FILETYPE_STATE      = 7;
        public static final int OSD_FILETYPE_ARTWORK    = 8;
        public static final int OSD_FILETYPE_MEMCARD    = 9;
        public static final int OSD_FILETYPE_SCREENSHOT =10;
   
}
