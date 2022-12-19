/************************************************************

vidhrdw\starwars.h

This file is Copyright 1997, Steve Baines.

Release 1.0 (21 July 1997)

See drivers\starwars.c for notes

************************************************************/

#define VECMEM_TYPE unsigned char

#define X_RES 800 /* Used to define game window size */
#define Y_RES 600

#define VECDEBUG 0 /* If 1 then log operation of vector unit */
#define TEXTMODE 0
/* Text only test mode. Runs as usual but just reports  */
/* what would be happening rather than actually doing it */

void vector_engine(VECMEM_TYPE *, long, short, short);

void starwars_set_palette(UBytePtr , UBytePtr ,const UBytePtr );
void starwars_vh_screenrefresh(struct osd_bitmap *);

void draw_vector(short, short, short, short, short, short, char);



