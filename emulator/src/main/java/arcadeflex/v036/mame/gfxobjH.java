/**
 * ported to v0.36
 */
package arcadeflex.v036.mame;

public class gfxobjH {
    /*TODO*///#ifndef GFX_OBJECT_MANAGER
/*TODO*///#define GFX_OBJECT_MANAGER
/*TODO*////*
/*TODO*///	gfx object manager
/*TODO*///*/
/*TODO*////* dirty flag */
/*TODO*///#define GFXOBJ_DIRTY_ALL      0xff
/*TODO*///#define GFXOBJ_DIRTY_SX_SY    0xff
/*TODO*///#define GFXOBJ_DIRTY_SIZE     0xff
/*TODO*///#define GFXOBJ_DIRTY_PRIORITY 0xff
/*TODO*///#define GFXOBJ_DIRTY_CODE     0xff
/*TODO*///#define GFXOBJ_DIRTY_COLOR    0xff
/*TODO*///#define GFXOBJ_DIRTY_FLIP     0xff
/*TODO*///
/*TODO*////* sort(priority) flag */
/*TODO*///#define GFXOBJ_DONT_SORT          0x00
/*TODO*///#define GFXOBJ_DO_SORT            0x01
/*TODO*///#define GFXOBJ_SORT_OBJECT_BACK   0x02
/*TODO*///#define GFXOBJ_SORT_PRIORITY_BACK 0x04
/*TODO*///
/*TODO*///#define GFXOBJ_SORT_DEFAULT GFXOBJ_DO_SORT
/*TODO*///
/*TODO*////* one of object */
/*TODO*///struct gfx_object {
/*TODO*///	int		transparency;		/* transparency of gfx */
/*TODO*///	int		transparet_color;	/* transparet color of gfx */
/*TODO*///	struct	GfxElement *gfx;	/* source gfx , if gfx==0 then not calcrate sx,sy,visible,clip */
/*TODO*///	int		code;				/* code of gfx */
/*TODO*///	int		color;				/* color of gfx */
/*TODO*///	int		priority;			/* priority 0=lower */
/*TODO*///	int		sx;					/* x position */
/*TODO*///	int		sy;					/* y position */
/*TODO*///	int		flipx;				/* x flip */
/*TODO*///	int		flipy;				/* y flip */
/*TODO*///	/* source window in gfx tile : only non zooming gfx */
/*TODO*///	/* if use zooming gfx , top,left should be set 0, */
/*TODO*///	/* and width,height should be set same as gfx element */
/*TODO*///	int		top;					/* x offset of source data */
/*TODO*///	int		left;					/* y offset of source data */
/*TODO*///	int		width;				/* x size */
/*TODO*///	int		height;				/* y size */
/*TODO*///	int		palette_flag;		/* !! not supported !! , palette usage flag tracking */
/*TODO*///	/* zooming */
/*TODO*///	int scalex;					/* zommscale , if 0 then non zooming gfx */
/*TODO*///	int scaley;					/* */
/*TODO*///	/* link */
/*TODO*///	struct gfx_object *next;	/* next object point */
/*TODO*///	/* exrernal draw handler , (for tilemap,special sprite,etc) */
/*TODO*///	void	(*special_handler)(struct osd_bitmap *bitmap,struct gfx_object *object);
/*TODO*///								/* !!! not suppored yet !!! */
/*TODO*///	int		dirty_flag;			/* dirty flag */
/*TODO*///	/* !! only drawing routine !! */
/*TODO*///	int		visible;		/* visible flag        */
/*TODO*///	int		draw_x;			/* x adjusted position */
/*TODO*///	int		draw_y;			/* y adjusted position */
/*TODO*///	struct rectangle clip; /* clipping object size with visible area */
/*TODO*///};
/*TODO*///
/*TODO*////* object list */
/*TODO*///struct gfx_object_list {
/*TODO*///	int nums;						/* read only */
/*TODO*///	int max_priority;				/* read only */
/*TODO*///	struct gfx_object *objects;
/*TODO*///									/* priority : objects[0]=lower       */
/*TODO*///									/*          : objects[nums-1]=higher */
/*TODO*///	struct gfx_object *first_object; /* pointer of first(lower) link object */
/*TODO*///	/* !! private area !! */
/*TODO*///	int sort_type;					/* priority order type */
/*TODO*///	struct gfx_object_list *next;	/* resource tracking */
/*TODO*///};
/*TODO*///
/*TODO*///void gfxobj_init(void);		/* called by core - don't call this in drivers */
/*TODO*///void gfxobj_close(void);	/* called by core - don't call this in drivers */
/*TODO*///
/*TODO*///void gfxobj_mark_all_pixels_dirty(struct gfx_object_list *object_list);
/*TODO*///struct gfx_object_list *gfxobj_create(int nums,int max_priority,const struct gfx_object *def_object);
/*TODO*///void gfxobj_update(void);
/*TODO*///void gfxobj_draw(struct gfx_object_list *object_list);
/*TODO*///
/*TODO*///#endif
/*TODO*///
}
