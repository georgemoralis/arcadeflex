/*********************************************************************

  artwork.h

  Generic backdrop/overlay functions.

  Be sure to include driver.h before including this file.

  Created by Mike Balfour - 10/01/1998
*********************************************************************/

#ifndef ARTWORK_H

#define ARTWORK_H 1

/*********************************************************************
  artwork

  This structure is a generic structure used to hold both backdrops
  and overlays.
*********************************************************************/

struct artwork
{
	/* Publically accessible */
	struct osd_bitmap *artwork;

	/* Private - don't touch! */
	struct osd_bitmap *orig_artwork;	/* needed for palette recalcs */
	struct osd_bitmap *vector_bitmap;	/* needed to buffer the vector image in vg with overlays */
	unsigned char *orig_palette;		/* needed for restoring the colors after special effects? */
	int num_pens_used;
	unsigned char *transparency;
	int num_pens_trans;
	int start_pen;
	unsigned char *brightness;              /* brightness of each palette entry */
	unsigned char *pTable;                  /* Conversion table usually used for mixing colors */
};


struct artwork_element
{
	struct rectangle box;
	unsigned char red,green,blue,alpha;
};

/*********************************************************************
  functions that apply to backdrops AND overlays
*********************************************************************/
struct artwork *artwork_load(const char *filename, int start_pen, int max_pens);
struct artwork *artwork_load_size(const char *filename, int start_pen, int max_pens, int width, int height);
struct artwork *artwork_create(const struct artwork_element *ae, int start_pen, int max_pens);
void artwork_elements_scale(struct artwork_element *ae, int width, int height);
void artwork_free(struct artwork *a);

/*********************************************************************
  functions that are backdrop-specific
*********************************************************************/
void backdrop_refresh(struct artwork *a);
void backdrop_refresh_tables (struct artwork *a);
void backdrop_set_palette(struct artwork *a, unsigned char *palette);
int backdrop_black_recalc(void);
void draw_backdrop(struct osd_bitmap *dest,const struct osd_bitmap *src,int sx,int sy,
		   const struct rectangle *clip);
void drawgfx_backdrop(struct osd_bitmap *dest,const struct GfxElement *gfx,
		      unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
		      const struct rectangle *clip,const struct osd_bitmap *back);

/*********************************************************************
  functions that are overlay-specific
*********************************************************************/
int overlay_set_palette (struct artwork *a, unsigned char *palette, int num_shades);
void overlay_remap(struct artwork *a);
void overlay_draw(struct osd_bitmap *dest,const struct artwork *overlay);

#endif

