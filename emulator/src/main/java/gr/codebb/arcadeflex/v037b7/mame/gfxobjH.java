package gr.codebb.arcadeflex.v037b7.mame;

import gr.codebb.arcadeflex.v036.mame.osdependH.osd_bitmap;
import gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;


public class gfxobjH {
/*TODO*///#ifndef GFX_OBJECT_MANAGER
/*TODO*///#define GFX_OBJECT_MANAGER
/*TODO*////*
/*TODO*///	gfx object manager
/*TODO*///*/
    /* dirty flag */
    public static final int GFXOBJ_DIRTY_ALL      = 0xff;
/*TODO*///#define GFXOBJ_DIRTY_SX_SY    0xff
/*TODO*///#define GFXOBJ_DIRTY_SIZE     0xff
/*TODO*///#define GFXOBJ_DIRTY_PRIORITY 0xff
/*TODO*///#define GFXOBJ_DIRTY_CODE     0xff
/*TODO*///#define GFXOBJ_DIRTY_COLOR    0xff
/*TODO*///#define GFXOBJ_DIRTY_FLIP     0xff

    /* sort(priority) flag */
    public static final int GFXOBJ_DONT_SORT          = 0x00;
    public static final int GFXOBJ_DO_SORT            = 0x01;
    public static final int GFXOBJ_SORT_OBJECT_BACK   = 0x02;
    public static final int GFXOBJ_SORT_PRIORITY_BACK = 0x04;

    public static int GFXOBJ_SORT_DEFAULT = GFXOBJ_DO_SORT;
    
    //JAVA HELPERS
    public static abstract interface gfx_objectHandlerPtr { public abstract void handler(osd_bitmap bitmap, gfx_object object); }

    /* one of object */
    public static class gfx_object {
            public int		transparency;		/* transparency of gfx */
            public int		transparet_color;	/* transparet color of gfx */
            public GfxElement  gfx;	/* source gfx , if gfx==0 then not calcrate sx,sy,visible,clip */
            public int		code;				/* code of gfx */
            public int		color;				/* color of gfx */
            public int		priority;			/* priority 0=lower */
            public int		sx;					/* x position */
            public int		sy;					/* y position */
            public int		flipx;				/* x flip */
            public int		flipy;				/* y flip */
            /* source window in gfx tile : only non zooming gfx */
            /* if use zooming gfx , top,left should be set 0, */
            /* and width,height should be set same as gfx element */
            public int		top;					/* x offset of source data */
            public int		left;					/* y offset of source data */
            public int		width;				/* x size */
            public int		height;				/* y size */
            public int		palette_flag;		/* !! not supported !! , palette usage flag tracking */
            /* zooming */
            public int scalex;					/* zommscale , if 0 then non zooming gfx */
            public int scaley;					/* */
            /* link */
            public gfx_object next;	/* next object point */
            /* external draw handler , (for tilemap,special sprite,etc) */
            public gfx_objectHandlerPtr special_handler;
                                                                    /* !!! not suppored yet !!! */
            public int		dirty_flag;			/* dirty flag */
            /* !! only drawing routine !! */
            public int		visible;		/* visible flag        */
            public int		draw_x;			/* x adjusted position */
            public int		draw_y;			/* y adjusted position */
            public rectangle clip; /* clipping object size with visible area */
    };

    /* object list */
    public static class gfx_object_list {
            int nums;						/* read only */
            int max_priority;				/* read only */
            public gfx_object[] objects;
                                                                            /* priority : objects[0]=lower       */
                                                                            /*          : objects[nums-1]=higher */
            gfx_object first_object; /* pointer of first(lower) link object */
            /* !! private area !! */
            int sort_type;					/* priority order type */
            gfx_object_list next;	/* resource tracking */
    };


/*TODO*///void gfxobj_mark_all_pixels_dirty(struct gfx_object_list *object_list);
/*TODO*///struct gfx_object_list *gfxobj_create(int nums,int max_priority,const struct gfx_object *def_object);
/*TODO*///void gfxobj_draw(struct gfx_object_list *object_list);
/*TODO*///
/*TODO*///#endif
    
}
