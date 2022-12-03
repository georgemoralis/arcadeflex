/*
 * ported to 0.36
 */
package arcadeflex.v036.mame;

import gr.codebb.arcadeflex.v037b7.mame.drawgfxH.rectangle;

public class artworkH {

    /*TODO*////*********************************************************************
/*TODO*///  artwork
/*TODO*///
/*TODO*///  This structure is a generic structure used to hold both backdrops
/*TODO*///  and overlays.
/*TODO*///*********************************************************************/
/*TODO*///
public static class  struct_artwork
{
/*TODO*///	/* Publically accessible */
/*TODO*///	struct osd_bitmap *artwork;
/*TODO*///
/*TODO*///	/* Private - don't touch! */
/*TODO*///	struct osd_bitmap *orig_artwork;	/* needed for palette recalcs */
/*TODO*///	struct osd_bitmap *vector_bitmap;	/* needed to buffer the vector image in vg with overlays */
/*TODO*///	unsigned char *orig_palette;		/* needed for restoring the colors after special effects? */
/*TODO*///	int num_pens_used;
/*TODO*///	unsigned char *transparency;
/*TODO*///	int num_pens_trans;
/*TODO*///	int start_pen;
/*TODO*///	unsigned char *brightness;              /* brightness of each palette entry */
/*TODO*///	unsigned char *pTable;                  /* Conversion table usually used for mixing colors */
}
/*TODO*///
/*TODO*///
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
