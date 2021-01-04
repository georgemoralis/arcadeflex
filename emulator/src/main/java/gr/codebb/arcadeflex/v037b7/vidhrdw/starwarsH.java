package gr.codebb.arcadeflex.v037b7.vidhrdw;

/************************************************************
This file is Copyright 1997, Steve Baines.

Release 1.0 (21 July 1997)

See drivers\starwars.c for notes

************************************************************/

public class starwarsH {

    public static int VECMEM_TYPE;

    public static final int X_RES = 800; /* Used to define game window size */
    public static final int Y_RES = 600;

    public static final int VECDEBUG = 0; /* If 1 then log operation of vector unit */
    public static final int TEXTMODE = 0;
    
    /* Text only test mode. Runs as usual but just reports  */
    /* what would be happening rather than actually doing it */

/*TODO*///    void vector_engine(VECMEM_TYPE *, long, short, short);

/*TODO*///    void starwars_set_palette(UBytePtr , UBytePtr ,const UBytePtr );
/*TODO*///    void starwars_vh_screenrefresh(struct osd_bitmap *);

/*TODO*///    void draw_vector(short, short, short, short, short, short, char);
    
}
