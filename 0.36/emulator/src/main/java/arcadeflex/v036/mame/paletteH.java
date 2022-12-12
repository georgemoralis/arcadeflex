/**
 * ported to v0.36
 */
package arcadeflex.v036.mame;

public class paletteH {

    public static final int DYNAMIC_MAX_PENS = 254;/* the Mac cannot handle more than 254 dynamic pens */
    public static final int STATIC_MAX_PENS = 256;/* but 256 static pens can be handled */

    public static final int PALETTE_COLOR_UNUSED = 0;/* This color is not needed for this frame */
    public static final int PALETTE_COLOR_VISIBLE = 1;/* This color is currently visible */
    public static final int PALETTE_COLOR_CACHED = 2;/* This color is cached in temporary bitmaps */
 /* palette_recalc() will try to use always the same pens for the used colors; */
 /* if it is forced to rearrange the pens, it will return TRUE to signal the */
 /* driver that it must refresh the display. */
    public static final int PALETTE_COLOR_TRANSPARENT_FLAG = 4;/* All colors using this attribute will be */
 /* mapped to the same pen, and no other colors will be mapped to that pen. */
 /* This way, transparencies can be handled by copybitmap(). */

 /* backwards compatibility */
    public static final int PALETTE_COLOR_USED = (PALETTE_COLOR_VISIBLE | PALETTE_COLOR_CACHED);
    public static final int PALETTE_COLOR_TRANSPARENT = (PALETTE_COLOR_TRANSPARENT_FLAG | PALETTE_COLOR_USED);
}
