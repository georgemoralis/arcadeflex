/**
 * ported to v0.36
 */
package arcadeflex.v036.mame;

public class gfxobj {

    /*TODO*////*
/*TODO*///	gfx object manager
/*TODO*///
/*TODO*///	Games currently using the Sprite Manager:
/*TODO*///		Namco System1
/*TODO*///
/*TODO*///	Graphic Object Manager controls all Graphic object and the priority.
/*TODO*///
/*TODO*///	The thing to draw in tile of gfx element by an arbitrary size can be done.
/*TODO*///	That is,  it is useful for the sprite system which shares a different size.
/*TODO*///
/*TODO*///	The drawing routine can be replaced with callbacking user routine.
/*TODO*///	The control of priority becomes easy if tilemap draws by using this function.
/*TODO*///	If gfx like special drawing routine is made,complexity overlapping sprite can be include.
/*TODO*///
/*TODO*///	Currently supported features include:
/*TODO*///	- The position and the size can be arbitrarily specified from gfx tile.
/*TODO*///	  ( It is achieved by using clipping.)
/*TODO*///	-  priority ordering.
/*TODO*///	- resource tracking (gfxobj_init and gfxobj_close must be called by mame.c)
/*TODO*///	- offscreen object skipping
/*TODO*///
/*TODO*///	Schedule for the future
/*TODO*///	- zoomming gfx with selectable tile position and size
/*TODO*///	- palette usage tracking
/*TODO*///	- uniting with Sprite Manager
/*TODO*///	- priority link system in object list.
/*TODO*///
/*TODO*///	Hint:
/*TODO*///
/*TODO*///	-The position and the size select :
/*TODO*///
/*TODO*///	This is similar to SPRITE_TYPE_UNPACK of Sprite Manager.
/*TODO*///	(see also comments in sprite.c.)
/*TODO*///
/*TODO*///	objects->top
/*TODO*///	objects->left    -> The display beginning position in gfx tile is specified.
/*TODO*///	objects->width
/*TODO*///	objects->height  -> The display size is specified.
/*TODO*///
/*TODO*///	Gfx object manager adjust the draw position from the value of top and left.
/*TODO*///	And,the clipping position is adjusted according to the size.
/*TODO*///
/*TODO*///	-The palette resource tracking :
/*TODO*///
/*TODO*///	sprite manager it not supported yet.
/*TODO*///	User should control palette resource for yourself.
/*TODO*///	If you use sort mode,the object not seen has exclusion from the object link.
/*TODO*///	Therefore,  examining objectlink will decrease the overhead.
/*TODO*///
/*TODO*///	example:
/*TODO*///
/*TODO*///	gfxobj_update();
/*TODO*///	if(object=objectlist->first_object ; object != 0 ; object=object->next)
/*TODO*///	{
/*TODO*///		color = object->color;
/*TODO*///		mark_palette_used_flag(color);
/*TODO*///	}
/*TODO*///
/*TODO*///	-Priority order
/*TODO*///
/*TODO*///	Gfx object manager can sort the object by priority.
/*TODO*///	The condition of sorting is decided by the flag.
/*TODO*///
/*TODO*///	gfxobject_list->sort_type;
/*TODO*///
/*TODO*///	GFXOBJ_DONT_SORT:
/*TODO*///
/*TODO*///	Sorting is not done.
/*TODO*///	The user should be build object link list.
/*TODO*///	excluded objects from link are not updated.
/*TODO*///
/*TODO*///	gfxobject_list->first_object; -> first object point (lower object)
/*TODO*///	gfxobject->next               -> next object point ,0 = end of link(higher)
/*TODO*///
/*TODO*///	GFXOBJ_DOT_SORT:
/*TODO*///
/*TODO*///	Sorting is done.
/*TODO*///
/*TODO*///	The condition by object->priority is given to priority most.
/*TODO*///		object->priority = p    -> p=0 : lower , p=object_list->max_priority-1 : higher
/*TODO*///	If 'GFXOBJ_SORT_PRIORITY_BACK' is specified,  order is reversed.
/*TODO*///
/*TODO*///	Next,  the array of object is given to priority.
/*TODO*///		object_list->objects[x] -> x=0 : lower , x=object_list->nums-1 = higher
/*TODO*///	If 'GFXOBJ_SORT_OBJECT_BACK' is specified,  order is reversed.
/*TODO*///
/*TODO*///	-The handling of tilemap :
/*TODO*///
/*TODO*///	Tilemap can be treated by the callback function as one object.
/*TODO*///
/*TODO*///	If the user function is registered in object->special_handler,
/*TODO*///	the user function is called instead of drawgfx.
/*TODO*///
/*TODO*///	Call tilemap_draw() in your callback handler.
/*TODO*///	If object->code is numbered with two or more tilemap,it might be good.
/*TODO*///
/*TODO*///	If you adjust objects->gfx to 0,Useless processing to tilemap is omitted.
/*TODO*///	clipping,drawposition,visible,dirty_flag are not updated.
/*TODO*///
/*TODO*///	If one tilemap is displayed with background and foreground
/*TODO*///	Use two objects behind in front of sprite.
/*TODO*///
/*TODO*///	Example:
/*TODO*///
/*TODO*///	object resource:
/*TODO*///
/*TODO*///	objects[0]   ==  tilemap1 , higher background
/*TODO*///	objects[1]   ==  tilemap2 , lower  background
/*TODO*///	objects[3]   ==  lower sprite
/*TODO*///	objects[4]   ==  middle sprite
/*TODO*///	objects[5]   ==  higher sprite
/*TODO*///	objects[2]   ==  tilemap2 , foreground
/*TODO*///
/*TODO*///	objects[0].code  = 1;  number of tilemap_2
/*TODO*///	objects[0].color = 0;  background mark
/*TODO*///	objects[1].code  = 0;  number of tilemap_1
/*TODO*///	objects[1].color = 0;  background mark
/*TODO*///	objects[5].code  = 0;  number of tilemap_1
/*TODO*///	objects[5].color = 1;  foreground mark
/*TODO*///
/*TODO*///	objects[0,1,5].special_handler = object_draw_tile;
/*TODO*///	objects[0,1,5].gfx = 0;
/*TODO*///
/*TODO*///	callback handler:
/*TODO*///
/*TODO*///	void object_draw_tile(struct osd_bitmap *bitmap,struct gfx_object *object)
/*TODO*///	{
/*TODO*///		tilemap_draw( bitmap , tilemap[object->code] , object->color );
/*TODO*///	}
/*TODO*///
/*TODO*///*/
/*TODO*///
/*TODO*///#include "driver.h"
/*TODO*////* #include "gfxobj.h" */
/*TODO*///
/*TODO*///static struct gfx_object_list *first_object_list;
/*TODO*///
    public static void gfxobj_init() {
        /*TODO*///	first_object_list = 0;
    }

    public static void gfxobj_close() {
        /*TODO*///	struct gfx_object_list *object_list,*object_next;
/*TODO*///	for(object_list = first_object_list ; object_list != 0 ; object_list=object_next)
/*TODO*///	{
/*TODO*///		free(object_list->objects);
/*TODO*///		object_next = object_list->next;
/*TODO*///		free(object_list);
/*TODO*///	}
    }
    /*TODO*///
/*TODO*///#define MAX_PRIORITY 16
/*TODO*///struct gfx_object_list *gfxobj_create(int nums,int max_priority,const struct gfx_object *def_object)
/*TODO*///{
/*TODO*///	struct gfx_object_list *object_list;
/*TODO*///	int i;
/*TODO*///
/*TODO*///	/* priority limit check */
/*TODO*///	if(max_priority >= MAX_PRIORITY)
/*TODO*///		return 0;
/*TODO*///
/*TODO*///	/* allocate object liset */
/*TODO*///	if( (object_list = malloc(sizeof(struct gfx_object_list))) == 0 )
/*TODO*///		return 0;
/*TODO*///	memset(object_list,0,sizeof(struct gfx_object_list));
/*TODO*///
/*TODO*///	/* allocate objects */
/*TODO*///	if( (object_list->objects = malloc(nums*sizeof(struct gfx_object))) == 0)
/*TODO*///	{
/*TODO*///		free(object_list);
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	if(def_object == 0)
/*TODO*///	{	/* clear all objects */
/*TODO*///		memset(object_list->objects,0,nums*sizeof(struct gfx_object));
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{	/* preset with default objects */
/*TODO*///		for(i=0;i<nums;i++)
/*TODO*///			memcpy(&object_list->objects[i],def_object,sizeof(struct gfx_object));
/*TODO*///	}
/*TODO*///	/* setup objects */
/*TODO*///	for(i=0;i<nums;i++)
/*TODO*///	{
/*TODO*///		/*	dirty flag */
/*TODO*///		object_list->objects[i].dirty_flag = GFXOBJ_DIRTY_ALL;
/*TODO*///		/* link map */
/*TODO*///		object_list->objects[i].next = &object_list->objects[i+1];
/*TODO*///	}
/*TODO*///	/* setup object_list */
/*TODO*///	object_list->max_priority = max_priority;
/*TODO*///	object_list->nums = nums;
/*TODO*///	object_list->first_object = object_list->objects; /* top of link */
/*TODO*///	object_list->objects[nums-1].next = 0; /* bottom of link */
/*TODO*///	object_list->sort_type = GFXOBJ_SORT_DEFAULT;
/*TODO*///	/* resource tracking */
/*TODO*///	object_list->next = first_object_list;
/*TODO*///	first_object_list = object_list;
/*TODO*///
/*TODO*///	return object_list;
/*TODO*///}
/*TODO*///
/*TODO*////* set pixel dirty flag */
/*TODO*///void gfxobj_mark_all_pixels_dirty(struct gfx_object_list *object_list)
/*TODO*///{
/*TODO*///	/* don't care , gfx object manager don't keep color remapped bitmap */
/*TODO*///}
/*TODO*///
/*TODO*////* update object */
/*TODO*///static void object_update(struct gfx_object *object)
/*TODO*///{
/*TODO*///	int min_x,min_y,max_x,max_y;
/*TODO*///
/*TODO*///	/* clear dirty flag */
/*TODO*///	object->dirty_flag = 0;
/*TODO*///
/*TODO*///	/* if gfx == 0 ,then bypass (for special_handler ) */
/*TODO*///	if(object->gfx == 0)
/*TODO*///		return;
/*TODO*///
/*TODO*///	/* check visible area */
/*TODO*///	min_x = Machine->drv->visible_area.min_x;
/*TODO*///	max_x = Machine->drv->visible_area.max_x;
/*TODO*///	min_y = Machine->drv->visible_area.min_y;
/*TODO*///	max_y = Machine->drv->visible_area.max_y;
/*TODO*///	if(
/*TODO*///		(object->width==0)  ||
/*TODO*///		(object->height==0) ||
/*TODO*///		(object->sx > max_x) ||
/*TODO*///		(object->sy > max_y) ||
/*TODO*///		(object->sx+object->width  <= min_x) ||
/*TODO*///		(object->sy+object->height <= min_y) )
/*TODO*///	{	/* outside of visible area */
/*TODO*///		object->visible = 0;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	object->visible = 1;
/*TODO*///	/* set draw position with adjust source offset */
/*TODO*///	object->draw_x = object->sx -
/*TODO*///		(	object->flipx ?
/*TODO*///			(object->gfx->width - (object->left + object->width)) : /* flip */
/*TODO*///			(object->left) /* non flip */
/*TODO*///		);
/*TODO*///
/*TODO*///	object->draw_y = object->sy -
/*TODO*///		(	object->flipy ?
/*TODO*///			(object->gfx->height - (object->top + object->height)) : /* flip */
/*TODO*///			(object->top) /* non flip */
/*TODO*///		);
/*TODO*///	/* set clipping point to object draw area */
/*TODO*///	object->clip.min_x = object->sx;
/*TODO*///	object->clip.max_x = object->sx + object->width -1;
/*TODO*///	object->clip.min_y = object->sy;
/*TODO*///	object->clip.max_y = object->sy + object->height -1;
/*TODO*///	/* adjust clipping point with visible area */
/*TODO*///	if (object->clip.min_x < min_x) object->clip.min_x = min_x;
/*TODO*///	if (object->clip.max_x > max_x) object->clip.max_x = max_x;
/*TODO*///	if (object->clip.min_y < min_y) object->clip.min_y = min_y;
/*TODO*///	if (object->clip.max_y > max_y) object->clip.max_y = max_y;
/*TODO*///}
/*TODO*///
/*TODO*////* update one of object list */
/*TODO*///static void gfxobj_update_one(struct gfx_object_list *object_list)
/*TODO*///{
/*TODO*///	struct gfx_object *object;
/*TODO*///	struct gfx_object *start_object,*last_object;
/*TODO*///	int dx,start_priority,end_priority;
/*TODO*///	int priorities = object_list->max_priority;
/*TODO*///	int priority;
/*TODO*///
/*TODO*///	if(object_list->sort_type&GFXOBJ_DO_SORT)
/*TODO*///	{
/*TODO*///		struct gfx_object *top_object[MAX_PRIORITY],*end_object[MAX_PRIORITY];
/*TODO*///		/* object sort direction */
/*TODO*///		if(object_list->sort_type&GFXOBJ_SORT_OBJECT_BACK)
/*TODO*///		{
/*TODO*///			start_object   = object_list->objects + object_list->nums-1;
/*TODO*///			last_object    = object_list->objects-1;
/*TODO*///			dx = -1;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			start_object = object_list->objects;
/*TODO*///			last_object  = object_list->objects + object_list->nums;
/*TODO*///			dx = 1;
/*TODO*///		}
/*TODO*///		/* reset each priority point */
/*TODO*///		for( priority = 0; priority < priorities; priority++ )
/*TODO*///			end_object[priority] = 0;
/*TODO*///		/* update and sort */
/*TODO*///		for(object=start_object ; object != last_object ; object+=dx)
/*TODO*///		{
/*TODO*///			/* update all objects */
/*TODO*///			if(object->dirty_flag)
/*TODO*///				object_update(object);
/*TODO*///			/* store link */
/*TODO*///			if(object->visible)
/*TODO*///			{
/*TODO*///				priority = object->priority;
/*TODO*///				if(end_object[priority])
/*TODO*///					end_object[priority]->next = object;
/*TODO*///				else
/*TODO*///					top_object[priority] = object;
/*TODO*///				end_object[priority] = object;
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		/* priority sort direction */
/*TODO*///		if(object_list->sort_type&GFXOBJ_SORT_PRIORITY_BACK)
/*TODO*///		{
/*TODO*///			start_priority = priorities-1;
/*TODO*///			end_priority   = -1;
/*TODO*///			dx = -1;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			start_priority = 0;
/*TODO*///			end_priority   = priorities;
/*TODO*///			dx = 1;
/*TODO*///		}
/*TODO*///		/* link between priority */
/*TODO*///		last_object = 0;
/*TODO*///		for( priority = start_priority; priority != end_priority; priority+=dx )
/*TODO*///		{
/*TODO*///			if(end_object[priority])
/*TODO*///			{
/*TODO*///				if(last_object)
/*TODO*///					last_object->next = top_object[priority];
/*TODO*///				else
/*TODO*///					object_list->first_object = top_object[priority];
/*TODO*///				last_object = end_object[priority];
/*TODO*///			}
/*TODO*///		}
/*TODO*///		if(last_object == 0 )
/*TODO*///			object_list->first_object = 0;
/*TODO*///		else
/*TODO*///			last_object->next = 0;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{	/* non sort , update only linked object */
/*TODO*///		for(object=object_list->first_object ; object !=0 ; object=object->next)
/*TODO*///		{
/*TODO*///			/* update all objects */
/*TODO*///			if(object->dirty_flag)
/*TODO*///				object_update(object);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	/* palette resource */
/*TODO*///	if(object->palette_flag)
/*TODO*///	{
/*TODO*///		/* !!!!! do not supported yet !!!!! */
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void gfxobj_update(void)
/*TODO*///{
/*TODO*///	struct gfx_object_list *object_list;
/*TODO*///
/*TODO*///	for(object_list=first_object_list ; object_list != 0 ; object_list=object_list->next)
/*TODO*///		gfxobj_update_one(object_list);
/*TODO*///}
/*TODO*///
/*TODO*///static void draw_object_one(struct osd_bitmap *bitmap,struct gfx_object *object)
/*TODO*///{
/*TODO*///	if(object->special_handler)
/*TODO*///	{	/* special object , callback user draw handler */
/*TODO*///		object->special_handler(bitmap,object);
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{	/* normaly gfx object */
/*TODO*///		drawgfx(bitmap,object->gfx,
/*TODO*///				object->code,
/*TODO*///				object->color,
/*TODO*///				object->flipx,
/*TODO*///				object->flipy,
/*TODO*///				object->draw_x,
/*TODO*///				object->draw_y,
/*TODO*///				&object->clip,
/*TODO*///				object->transparency,
/*TODO*///				object->transparet_color);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void gfxobj_draw(struct gfx_object_list *object_list)
/*TODO*///{
/*TODO*///	struct osd_bitmap *bitmap = Machine->scrbitmap;
/*TODO*///	struct gfx_object *object;
/*TODO*///
/*TODO*///	for(object=object_list->first_object ; object ; object=object->next)
/*TODO*///	{
/*TODO*///		if(object->visible )
/*TODO*///			draw_object_one(bitmap,object);
/*TODO*///	}
/*TODO*///}
/*TODO*///  
}
