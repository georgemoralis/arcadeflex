/*
	gfx object manager

	Games currently using the Sprite Manager:
		Namco System1

	Graphic Object Manager controls all Graphic object and the priority.

	The thing to draw in tile of gfx element by an arbitrary size can be done.
	That is,  it is useful for the sprite system which shares a different size.

	The drawing routine can be replaced with callbacking user routine.
	The control of priority becomes easy if tilemap draws by using this function.
	If gfx like special drawing routine is made,complexity overlapping sprite can be include.

	Currently supported features include:
	- The position and the size can be arbitrarily specified from gfx tile.
	  ( It is achieved by using clipping.)
	-  priority ordering.
	- resource tracking (gfxobj_init and gfxobj_close must be called by mame.c)
	- offscreen object skipping

	Schedule for the future
	- zoomming gfx with selectable tile position and size
	- palette usage tracking
	- uniting with Sprite Manager
	- priority link system in object list.

	Hint:

	-The position and the size select :

	This is similar to SPRITE_TYPE_UNPACK of Sprite Manager.
	(see also comments in sprite.c.)

	objects.top
	objects.left    . The display beginning position in gfx tile is specified.
	objects.width
	objects.height  . The display size is specified.

	Gfx object manager adjust the draw position from the value of top and left.
	And,the clipping position is adjusted according to the size.

	-The palette resource tracking :

	sprite manager it not supported yet.
	User should control palette resource for yourself.
	If you use sort mode,the object not seen has exclusion from the object link.
	Therefore,  examining objectlink will decrease the overhead.

	example:

	gfxobj_update();
	if(object=objectlist.first_object ; object != 0 ; object=object.next)
	{
		color = object.color;
		mark_palette_used_flag(color);
	}

	-Priority order

	Gfx object manager can sort the object by priority.
	The condition of sorting is decided by the flag.

	gfxobject_list.sort_type;

	GFXOBJ_DONT_SORT:

	Sorting is not done.
	The user should be build object link list.
	excluded objects from link are not updated.

	gfxobject_list.first_object; . first object point (lower object)
	gfxobject.next               . next object point ,0 = end of link(higher)

	GFXOBJ_DOT_SORT:

	Sorting is done.

	The condition by object.priority is given to priority most.
		object.priority = p    . p=0 : lower , p=object_list.max_priority-1 : higher
	If 'GFXOBJ_SORT_PRIORITY_BACK' is specified,  order is reversed.

	Next,  the array of object is given to priority.
		object_list.objects[x] . x=0 : lower , x=object_list.nums-1 = higher
	If 'GFXOBJ_SORT_OBJECT_BACK' is specified,  order is reversed.

	-The handling of tilemap :

	Tilemap can be treated by the callback function as one object.

	If the user function is registered in object.special_handler,
	the user function is called instead of drawgfx.

	Call tilemap_draw() in your callback handler.
	If object.code is numbered with two or more tilemap,it might be good.

	If you adjust objects.gfx to 0,Useless processing to tilemap is omitted.
	clipping,drawposition,visible,dirty_flag are not updated.

	If one tilemap is displayed with background and foreground
	Use two objects behind in front of sprite.

	Example:

	object resource:

	objects[0]   ==  tilemap1 , higher background
	objects[1]   ==  tilemap2 , lower  background
	objects[3]   ==  lower sprite
	objects[4]   ==  middle sprite
	objects[5]   ==  higher sprite
	objects[2]   ==  tilemap2 , foreground

	objects[0].code  = 1;  number of tilemap_2
	objects[0].color = 0;  background mark
	objects[1].code  = 0;  number of tilemap_1
	objects[1].color = 0;  background mark
	objects[5].code  = 0;  number of tilemap_1
	objects[5].color = 1;  foreground mark

	objects[0,1,5].special_handler = object_draw_tile;
	objects[0,1,5].gfx = 0;

	callback handler:

	void object_draw_tile(struct osd_bitmap *bitmap,struct gfx_object *object)
	{
		tilemap_draw( bitmap , tilemap[object.code] , object.color );
	}

*/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.mame;

import static gr.codebb.arcadeflex.v037b7.mame.gfxobjH.*;

public class gfxobj
{
/*TODO*///	/* 
	static gfx_object_list first_object_list;
/*TODO*///	
/*TODO*///	void gfxobj_init(void)
/*TODO*///	{
/*TODO*///		first_object_list = 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	void gfxobj_close(void)
/*TODO*///	{
/*TODO*///		struct gfx_object_list *object_list,*object_next;
/*TODO*///		for(object_list = first_object_list ; object_list != 0 ; object_list=object_next)
/*TODO*///		{
/*TODO*///			free(object_list.objects);
/*TODO*///			object_next = object_list.next;
/*TODO*///			free(object_list);
/*TODO*///		}
/*TODO*///	}

	public static final int MAX_PRIORITY = 16;
        
	public static gfx_object_list gfxobj_create(int nums,int max_priority, gfx_object def_object)
	{
		gfx_object_list object_list;
		int i;
	
		/* priority limit check */
		if(max_priority >= MAX_PRIORITY)
			return null;
	
		/* allocate object liset */
		if( (object_list = new gfx_object_list()) == null )
			return null;
/*TODO*///		memset(object_list,0,sizeof(struct gfx_object_list));
	
		/* allocate objects */
		if( (object_list.objects = new gfx_object[nums]) == null)
		{
			object_list=null;
			return null;
		}
		if(def_object == null)
		{	/* clear all objects */
			object_list.objects=new gfx_object[nums];
		}
		else
		{	/* preset with default objects */
			for(i=0;i<nums;i++)
				object_list.objects[i] = def_object;
		}
		/* setup objects */
		for(i=0;i<nums;i++)
		{
			/*	dirty flag */
			object_list.objects[i].dirty_flag = GFXOBJ_DIRTY_ALL;
			/* link map */
			object_list.objects[i].next = object_list.objects[i+1];
		}
		/* setup object_list */
		object_list.max_priority = max_priority;
		object_list.nums = nums;
                System.out.println("Check this dude!");
		object_list.first_object = object_list.objects[0]; /* top of link */
		object_list.objects[nums-1].next = null; /* bottom of link */
		object_list.sort_type = GFXOBJ_SORT_DEFAULT;
		/* resource tracking */
		object_list.next = first_object_list;
		first_object_list = object_list;
	
		return object_list;
	}
	
/*TODO*///	/* set pixel dirty flag */
/*TODO*///	void gfxobj_mark_all_pixels_dirty(struct gfx_object_list *object_list)
/*TODO*///	{
/*TODO*///		/* don't care , gfx object manager don't keep color remapped bitmap */
/*TODO*///	}
	
	/* update object */
	public static void object_update(gfx_object object)
	{
            System.out.println("object_update NOT IMPLEMENTED!!!!");
/*TODO*///		int min_x,min_y,max_x,max_y;
/*TODO*///	
/*TODO*///		/* clear dirty flag */
/*TODO*///		object.dirty_flag = 0;
/*TODO*///	
/*TODO*///		/* if gfx == 0 ,then bypass (for special_handler ) */
/*TODO*///		if(object.gfx == 0)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* check visible area */
/*TODO*///		min_x = Machine.visible_area.min_x;
/*TODO*///		max_x = Machine.visible_area.max_x;
/*TODO*///		min_y = Machine.visible_area.min_y;
/*TODO*///		max_y = Machine.visible_area.max_y;
/*TODO*///		if(
/*TODO*///			(object.width==0)  ||
/*TODO*///			(object.height==0) ||
/*TODO*///			(object.sx > max_x) ||
/*TODO*///			(object.sy > max_y) ||
/*TODO*///			(object.sx+object.width  <= min_x) ||
/*TODO*///			(object.sy+object.height <= min_y) )
/*TODO*///		{	/* outside of visible area */
/*TODO*///			object.visible = 0;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		object.visible = 1;
/*TODO*///		/* set draw position with adjust source offset */
/*TODO*///		object.draw_x = object.sx -
/*TODO*///			(	object.flipx ?
/*TODO*///				(object.gfx.width - (object.left + object.width)) : /* flip */
/*TODO*///				(object.left) /* non flip */
/*TODO*///			);
/*TODO*///	
/*TODO*///		object.draw_y = object.sy -
/*TODO*///			(	object.flipy ?
/*TODO*///				(object.gfx.height - (object.top + object.height)) : /* flip */
/*TODO*///				(object.top) /* non flip */
/*TODO*///			);
/*TODO*///		/* set clipping point to object draw area */
/*TODO*///		object.clip.min_x = object.sx;
/*TODO*///		object.clip.max_x = object.sx + object.width -1;
/*TODO*///		object.clip.min_y = object.sy;
/*TODO*///		object.clip.max_y = object.sy + object.height -1;
/*TODO*///		/* adjust clipping point with visible area */
/*TODO*///		if (object.clip.min_x < min_x) object.clip.min_x = min_x;
/*TODO*///		if (object.clip.max_x > max_x) object.clip.max_x = max_x;
/*TODO*///		if (object.clip.min_y < min_y) object.clip.min_y = min_y;
/*TODO*///		if (object.clip.max_y > max_y) object.clip.max_y = max_y;
	}
	
	/* update one of object list */
	public static void gfxobj_update_one(gfx_object_list object_list)
	{
		gfx_object object;
                int _object=0;
		gfx_object start_object, last_object;
                int _start_object=0, _last_object=0;
		int dx,start_priority,end_priority;
		int priorities = object_list.max_priority;
		int priority;
	
		if((object_list.sort_type&GFXOBJ_DO_SORT) != 0)
		{
			gfx_object[] top_object=new gfx_object[MAX_PRIORITY], end_object=new gfx_object[MAX_PRIORITY];
			/* object sort direction */
			if((object_list.sort_type&GFXOBJ_SORT_OBJECT_BACK) != 0)
			{
				start_object   = object_list.objects[ object_list.nums-1 ];
                                _start_object  = object_list.nums-1;
				last_object    = object_list.objects[object_list.objects.length-1];
                                _last_object   = object_list.objects.length-1;
				dx = -1;
			}
			else
			{
				start_object = object_list.objects[0];
                                _start_object = 0;
				last_object  = object_list.objects[ object_list.nums ];
                                _last_object  = object_list.nums;
				dx = 1;
			}
			/* reset each priority point */
			for( priority = 0; priority < priorities; priority++ )
				end_object[priority] = null;
			/* update and sort */
			for(_object =_start_object ; _object != _last_object ; _object+=dx)
			{
                                object = object_list.objects[_object];
                                
				/* update all objects */
				if(object.dirty_flag != 0)
					object_update(object);
				/* store link */
				if(object.visible != 0)
				{
					priority = object.priority;
					if(end_object[priority] != null)
						end_object[priority].next = object;
					else
						top_object[priority] = object;
					end_object[priority] = object;
				}
			}
	
			/* priority sort direction */
			if((object_list.sort_type&GFXOBJ_SORT_PRIORITY_BACK) != 0)
			{
				start_priority = priorities-1;
				end_priority   = -1;
				dx = -1;
			}
			else
			{
				start_priority = 0;
				end_priority   = priorities;
				dx = 1;
			}
			/* link between priority */
			last_object = null;
			for( priority = start_priority; priority != end_priority; priority+=dx )
			{
				if(end_object[priority] != null)
				{
					if (last_object != null)
						last_object.next = top_object[priority];
					else
						object_list.first_object = top_object[priority];
					last_object = end_object[priority];
				}
			}
			if(last_object == null )
				object_list.first_object = null;
			else
				last_object.next = null;
		}
		else
		{	/* non sort , update only linked object */
                    System.out.println("ELSE NOT IMPLEMENTED!!!!");
/*TODO*///			for(object=object_list.first_object ; object !=0 ; object=object.next)
/*TODO*///			{
/*TODO*///				/* update all objects */
/*TODO*///				if(object.dirty_flag != 0)
/*TODO*///					object_update(object);
/*TODO*///			}
		}
		/* palette resource */
/*TODO*///		if(object.palette_flag != 0)
/*TODO*///		{
/*TODO*///			/* !!!!! do not supported yet !!!!! */
/*TODO*///		}
	}
	
	public static void gfxobj_update()
	{
            System.out.println("gfxobj_update void NOT IMPLEMENTED!!!!");
/*TODO*///		struct gfx_object_list *object_list;
/*TODO*///	
/*TODO*///		for(object_list=first_object_list ; object_list != 0 ; object_list=object_list.next)
/*TODO*///			gfxobj_update_one(object_list);
	}
	
/*TODO*///	static void draw_object_one(struct osd_bitmap *bitmap,struct gfx_object *object)
/*TODO*///	{
/*TODO*///		if(object.special_handler)
/*TODO*///		{	/* special object , callback user draw handler */
/*TODO*///			object.special_handler(bitmap,object);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{	/* normaly gfx object */
/*TODO*///			drawgfx(bitmap,object.gfx,
/*TODO*///					object.code,
/*TODO*///					object.color,
/*TODO*///					object.flipx,
/*TODO*///					object.flipy,
/*TODO*///					object.draw_x,
/*TODO*///					object.draw_y,
/*TODO*///					&object.clip,
/*TODO*///					object.transparency,
/*TODO*///					object.transparet_color);
/*TODO*///		}
/*TODO*///	}
	
	public static void gfxobj_draw(gfx_object_list object_list)
	{
            System.out.println("gfxobj_draw NOT IMPLEMENTED!!!!");
/*TODO*///		struct osd_bitmap *bitmap = Machine.scrbitmap;
/*TODO*///		struct gfx_object *object;
/*TODO*///	
/*TODO*///		for(object=object_list.first_object ; object ; object=object.next)
/*TODO*///		{
/*TODO*///			if(object.visible )
/*TODO*///				draw_object_one(bitmap,object);
/*TODO*///		}
	}
}
