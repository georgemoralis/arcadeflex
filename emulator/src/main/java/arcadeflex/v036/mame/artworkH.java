/*
 * ported to 0.36
 */
package arcadeflex.v036.mame;

//mame imports
import static arcadeflex.v036.mame.osdependH.*;
//TODO
import gr.codebb.arcadeflex.v037b7.mame.drawgfxH.rectangle;

public class artworkH {

    /**
     * *******************************************************************
     * artwork
     *
     * This structure is a generic structure used to hold both backdrops and
     * overlays.
     * *******************************************************************
     */
    public static class struct_artwork {

        /* Publically accessible */
        osd_bitmap artwork;

        /* Private - don't touch! */
        osd_bitmap orig_artwork;/* needed for palette recalcs */
        osd_bitmap vector_bitmap;/* needed to buffer the vector image in vg with overlays */
        char[] /*unsigned char * */ orig_palette;/* needed for restoring the colors after special effects? */
        int num_pens_used;
        char[] /*unsigned char * */ transparency;
        int num_pens_trans;
        int start_pen;
        char[] /* unsigned char * */ brightness;/* brightness of each palette entry */
        char[] /* unsigned char * */ pTable;/* Conversion table usually used for mixing colors */
    }

    public static class artwork_element {

        rectangle box;
        char /*unsigned*/ red, green, blue, alpha;

        public artwork_element(rectangle box, char[] colors, char alpha) {
            this.box = box;
            this.red = colors[0];
            this.green = colors[1];
            this.blue = colors[2];
            this.alpha = alpha;
        }
    }

}
