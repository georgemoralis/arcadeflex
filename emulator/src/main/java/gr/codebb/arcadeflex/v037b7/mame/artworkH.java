/**
 * ported to 0.37b7
 */
package gr.codebb.arcadeflex.v037b7.mame;

public class artworkH {

    public static class artwork_info {
        /*TODO*///	/* Publically accessible */
/*TODO*///	struct osd_bitmap *artwork;
/*TODO*///	struct osd_bitmap *artwork1;
/*TODO*///	struct osd_bitmap *alpha;
/*TODO*///	struct osd_bitmap *orig_artwork;   /* needed for palette recalcs */
/*TODO*///	UINT8 *orig_palette;               /* needed for restoring the colors after special effects? */
/*TODO*///	int num_pens_used;
/*TODO*///	UINT8 *transparency;
/*TODO*///	int num_pens_trans;
/*TODO*///	int start_pen;
/*TODO*///	UINT8 *brightness;                 /* brightness of each palette entry */
/*TODO*///	UINT64 *rgb;
/*TODO*///	UINT8 *pTable;                     /* Conversion table usually used for mixing colors */
    }
    /*TODO*///
/*TODO*///
/*TODO*///struct artwork_element
/*TODO*///{
/*TODO*///	struct rectangle box;
/*TODO*///	UINT8 red,green,blue;
/*TODO*///	UINT16 alpha;   /* 0x00-0xff or OVERLAY_DEFAULT_OPACITY */
/*TODO*///};
/*TODO*///
/*TODO*///struct artwork_size_info
/*TODO*///{
/*TODO*///	int width, height;         /* widht and height of the artwork */
/*TODO*///	struct rectangle screen;   /* location of the screen relative to the artwork */
/*TODO*///};
/*TODO*///
/*TODO*///#define OVERLAY_DEFAULT_OPACITY         0xffff
/*TODO*///
/*TODO*///  
}
