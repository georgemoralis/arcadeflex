#ifndef __AVGDVG__
#define __AVGDVG__

/* vector engine types, passed to vg_init */

#define AVGDVG_MIN          1
#define USE_DVG             1
#define USE_AVG_RBARON      2
#define USE_AVG_BZONE       3
#define USE_AVG             4
#define USE_AVG_TEMPEST     5
#define USE_AVG_MHAVOC      6
#define USE_AVG_SWARS       7
#define USE_AVG_QUANTUM     8
#define AVGDVG_MAX          8

int avgdvg_init(int vgType);

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


#endif
