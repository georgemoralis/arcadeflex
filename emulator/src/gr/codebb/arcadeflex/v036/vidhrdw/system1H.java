package gr.codebb.arcadeflex.v036.vidhrdw;

public class system1H {

    public static final int SPR_Y_TOP = 0;
    public static final int SPR_Y_BOTTOM = 1;
    public static final int SPR_X_LO = 2;
    public static final int SPR_X_HI = 3;
    public static final int SPR_SKIP_LO = 4;
    public static final int SPR_SKIP_HI = 5;
    public static final int SPR_GFXOFS_LO = 6;
    public static final int SPR_GFXOFS_HI = 7;

    public static final int system1_SPRITE_PIXEL_MODE1 = 0;	// mode in which coordinates Y of sprites are using for priority checking
    // (pitfall2,upndown,wb deluxe)
    public static final int system1_SPRITE_PIXEL_MODE2 = 1;	// mode in which sprites are always drawing in order (0.1.2...31)
    // (choplifter,wonder boy in monster land)

    public static final int system1_BACKGROUND_MEMORY_SINGLE = 0;
    public static final int system1_BACKGROUND_MEMORY_BANKED = 1;
}
