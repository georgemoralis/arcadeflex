package gr.codebb.arcadeflex.v037b7.vidhrdw;

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
public class avgdvgH {
    
    /* vector engine types, passed to vg_init */

    public static final int AVGDVG_MIN          = 1;
    public static final int USE_DVG             = 1;
    public static final int USE_AVG_RBARON      = 2;
    public static final int USE_AVG_BZONE       = 3;
    public static final int USE_AVG             = 4;
    public static final int USE_AVG_TEMPEST     = 5;
    public static final int USE_AVG_MHAVOC      = 6;
    public static final int USE_AVG_SWARS       = 7;
    public static final int USE_AVG_QUANTUM     = 8;
    public static final int AVGDVG_MAX          = 8;

    /* Apart from the color mentioned below, the vector games will make additional
     * entries for translucency/antialiasing and for backdrop/overlay artwork */

    /* Black and White vector colors for Asteroids, Lunar Lander, Omega Race */
    /* Monochrome Aqua vector colors for Red Baron */
    /* Red and Green vector colors for Battlezone */
    /* Basic 8 rgb vector colors for Tempest, Gravitar, Major Havoc etc. */
    /* Special case for Star Wars and Empire strikes back */
    /* Monochrome Aqua vector colors for Asteroids Deluxe */

    /* Some games use a colorram. This is not handled via the Mame core functions
     * right now, but in src/vidhrdw/avgdvg.c itself. */
    
}
