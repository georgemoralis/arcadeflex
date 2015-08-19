package mame;

import static arcadeflex.libc_old.*;
import static arcadeflex.ptrlib.*;

public class spriteH {

    public static final int SPRITE_FLIPX = 0x01;
    public static final int SPRITE_FLIPY = 0x02;
    public static final int SPRITE_FLICKER = 0x04;
    public static final int SPRITE_VISIBLE = 0x08;
    public static final int SPRITE_TRANSPARENCY_THROUGH = 0x10;
    public static final int SPRITE_SPECIAL = 0x20;

    public static final int SPRITE_SHADOW = 0x40;
    public static final int SPRITE_PARTIAL_SHADOW = 0x80;

//SpriteType
    public static final int SPRITE_TYPE_STACK = 0;
    public static final int SPRITE_TYPE_UNPACK = 1;
    public static final int SPRITE_TYPE_ZOOM = 2;

    public static class sprite {

        public int priority, flags;

        public UBytePtr pen_data;	/* points to top left corner of tile data */

        public int line_offset;

        public CharPtr pal_data;
        public int/*UINT32*/ pen_usage;

        public int x_offset, y_offset;
        public int tile_width, tile_height;
        public int total_width, total_height;	/* in screen coordinates */

        public int x, y;

        public int shadow_pen;

        public sprite next;
        public int mask_offset;
    };

    /* sprite list flags */
    public static final int SPRITE_LIST_BACK_TO_FRONT = 0x0;
    public static final int SPRITE_LIST_FRONT_TO_BACK = 0x1;
    public static final int SPRITE_LIST_RAW_DATA = 0x2;
    public static final int SPRITE_LIST_FLIPX = 0x4;
    public static final int SPRITE_LIST_FLIPY = 0x8;

    public static class sprite_list {

        public int/*SpriteType*/ sprite_type;
        public int num_sprites;
        public int flags;
        public int max_priority;
        public int transparent_pen;
        public int special_pen;

        public sprite[] sprite;
        public sprite_list next; /* resource tracking */

    };
}
