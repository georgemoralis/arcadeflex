package vidhrdw.konami;

import static arcadeflex.libc_old.*;
import static arcadeflex.ptrlib.*;
import static mame.drawgfxH.*;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.mame.*;
import static mame.tilemapC.*;
import static mame.tilemapH.*;
import static mame.palette.*;
import static mame.cpuintrf.*;
import static mame.common.*;
import static vidhrdw.konamiic.*;
import static mame.mameH.*;
import static mame.cpuintrfH.*;
import static mame.memoryH.*;
import static mame.paletteH.PALETTE_COLOR_VISIBLE;

public class K053247 {

    //K053247_callback interface
    public static abstract interface K053247_callbackProcPtr {

        public abstract void handler(int[] code, int[] color, int[] priority);
    }

    public static int K053247_memory_region;
    public static GfxElement K053247_gfx;
    public static K053247_callbackProcPtr K053247_callback;
    public static int K053246_OBJCHA_line;
    public static int K053246_romoffset;
    public static int K053247_flipscreenX, K053247_flipscreenY;
    public static int K053247_spriteoffsX, K053247_spriteoffsY;
    public static UBytePtr K053247_ram;
    public static int K053247_irq_enabled;

    public static int K053247_vh_start(int gfx_memory_region, int plane0, int plane1, int plane2, int plane3, K053247_callbackProcPtr callback) {
        int gfx_index;
        GfxLayout spritelayout = new GfxLayout(
                16, 16,
                0, /* filled in later */
                4,
                new int[]{0, 0, 0, 0}, /* filled in later */
                new int[]{2 * 4, 3 * 4, 0 * 4, 1 * 4, 6 * 4, 7 * 4, 4 * 4, 5 * 4,
                    10 * 4, 11 * 4, 8 * 4, 9 * 4, 14 * 4, 15 * 4, 12 * 4, 13 * 4},
                new int[]{0 * 64, 1 * 64, 2 * 64, 3 * 64, 4 * 64, 5 * 64, 6 * 64, 7 * 64,
                    8 * 64, 9 * 64, 10 * 64, 11 * 64, 12 * 64, 13 * 64, 14 * 64, 15 * 64},
                128 * 8
        );


        /* find first empty slot to decode gfx */
        for (gfx_index = 0; gfx_index < MAX_GFX_ELEMENTS; gfx_index++) {
            if (Machine.gfx[gfx_index] == null) {
                break;
            }
        }
        if (gfx_index == MAX_GFX_ELEMENTS) {
            return 1;
        }

        /* tweak the structure for the number of tiles we have */
        spritelayout.total = memory_region_length(gfx_memory_region) / 128;
        spritelayout.planeoffset[0] = plane0;
        spritelayout.planeoffset[1] = plane1;
        spritelayout.planeoffset[2] = plane2;
        spritelayout.planeoffset[3] = plane3;

        /* decode the graphics */
        Machine.gfx[gfx_index] = decodegfx(memory_region(gfx_memory_region), spritelayout);
        if (Machine.gfx[gfx_index] == null) {
            return 1;
        }

        /* set the color information */
        Machine.gfx[gfx_index].colortable = new CharPtr(Machine.remapped_colortable);
        Machine.gfx[gfx_index].total_colors = Machine.drv.color_table_len / 16;

        K053247_memory_region = gfx_memory_region;
        K053247_gfx = Machine.gfx[gfx_index];
        K053247_callback = callback;
        K053246_OBJCHA_line = CLEAR_LINE;
        K053247_ram = new UBytePtr(0x1000);

        memset(K053247_ram, 0, 0x1000);

        return 0;
    }

    public static void K053247_vh_stop() {
        K053247_ram = null;
    }
    public static ReadHandlerPtr K053247_word_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int read = K053247_ram.READ_WORD(offset); 
            if(konamiicclog!=null) fprintf( konamiicclog,"K053247_word_r offset=%d return=%d\n",offset,read);
            return read;
        }
    };
    public static WriteHandlerPtr K053247_word_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            COMBINE_WORD_MEM(K053247_ram, offset, data);
        }
    };
    public static ReadHandlerPtr K053247_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int shift = ((offset & 1) ^ 1) << 3;
            int read = (K053247_ram.READ_WORD(offset & ~1) >>> shift) & 0xff;  //unsigned shift?
            if(konamiicclog!=null) fprintf( konamiicclog,"K053247_r offset=%d shift=%d return=%d\n",offset,shift,read);
            return read;
        }
    };
    public static WriteHandlerPtr K053247_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int shift = ((offset & 1) ^ 1) << 3;
            offset &= ~1;
            COMBINE_WORD_MEM(K053247_ram, offset, (0xff000000 >>> shift) | ((data & 0xff) << shift));//unsigned shift
        }
    };

    public static ReadHandlerPtr K053246_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (K053246_OBJCHA_line == ASSERT_LINE) {
                int addr;

                addr = 2 * K053246_romoffset + ((offset & 1) ^ 1);
                addr &= memory_region_length(K053247_memory_region) - 1;

                return memory_region(K053247_memory_region).read(addr);
            } else {
                if (errorlog != null) {
                    fprintf(errorlog, "%04x: read from unknown 053244 address %x\n", cpu_get_pc(), offset);
                }
                return 0;
            }
        }
    };

    public static WriteHandlerPtr K053246_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset == 0x00) {
                K053247_spriteoffsX = (K053247_spriteoffsX & 0x00ff) | (data << 8);
            } else if (offset == 0x01) {
                K053247_spriteoffsX = (K053247_spriteoffsX & 0xff00) | data;
            } else if (offset == 0x02) {
                K053247_spriteoffsY = (K053247_spriteoffsY & 0x00ff) | (data << 8);
            } else if (offset == 0x03) {
                K053247_spriteoffsY = (K053247_spriteoffsY & 0xff00) | data;
            } else if (offset == 0x05) {
                /* bit 0/1 = flip screen */
                K053247_flipscreenX = data & 0x01;
                K053247_flipscreenY = data & 0x02;

                /* bit 2 = unknown */

                /* bit 4 = interrupt enable */
                K053247_irq_enabled = data & 0x10;

                /* bit 5 = unknown */
                if (errorlog != null) {
                    fprintf(errorlog, "%04x: write %02x to 053246 address 5\n", cpu_get_pc(), data);
                }
            } else if (offset >= 0x04 && offset < 0x08) /* only 4,6,7 - 5 is handled above */ {
                offset = 8 * (((offset & 0x03) ^ 0x01) - 1);
                K053246_romoffset = (K053246_romoffset & ~(0xff << offset)) | (data << offset);
                return;
            } else if (errorlog != null) {
                fprintf(errorlog, "%04x: write %02x to unknown 053246 address %x\n", cpu_get_pc(), data, offset);
            }
        }
    };

    /*TODO*////*TODO*///int K053246_word_r(int offset)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	return K053246_r(offset + 1) | (K053246_r(offset) << 8);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K053246_word_w(int offset,int data)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	if ((data & 0xff000000) == 0)
/*TODO*////*TODO*///		K053246_w(offset,(data >> 8) & 0xff);
/*TODO*////*TODO*///	if ((data & 0x00ff0000) == 0)
/*TODO*////*TODO*///		K053246_w(offset + 1,data & 0xff);
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
    public static void K053246_set_OBJCHA_line(int state) {
        K053246_OBJCHA_line = state;
    }

    /*TODO*////*TODO*///
/*TODO*////*TODO*////*
/*TODO*////*TODO*/// * Sprite Format
/*TODO*////*TODO*/// * ------------------
/*TODO*////*TODO*/// *
/*TODO*////*TODO*/// * Word | Bit(s)           | Use
/*TODO*////*TODO*/// * -----+-fedcba9876543210-+----------------
/*TODO*////*TODO*/// *   0  | x--------------- | active (show this sprite)
/*TODO*////*TODO*/// *   0  | -x-------------- | maintain aspect ratio (when set, zoom y acts on both axis)
/*TODO*////*TODO*/// *   0  | --x------------- | flip y
/*TODO*////*TODO*/// *   0  | ---x------------ | flip x
/*TODO*////*TODO*/// *   0  | ----xxxx-------- | sprite size (see below)
/*TODO*////*TODO*/// *   0  | ---------xxxxxxx | priority order
/*TODO*////*TODO*/// *   1  | xxxxxxxxxxxxxxxx | sprite code
/*TODO*////*TODO*/// *   2  | ------xxxxxxxxxx | y position
/*TODO*////*TODO*/// *   3  | ------xxxxxxxxxx | x position
/*TODO*////*TODO*/// *   4  | xxxxxxxxxxxxxxxx | zoom y (0x40 = normal, <0x40 = enlarge, >0x40 = reduce)
/*TODO*////*TODO*/// *   5  | xxxxxxxxxxxxxxxx | zoom x (0x40 = normal, <0x40 = enlarge, >0x40 = reduce)
/*TODO*////*TODO*/// *   6  | x--------------- | mirror y (top half is drawn as mirror image of the bottom)
/*TODO*////*TODO*/// *   6  | -x-------------- | mirror x (right half is drawn as mirror image of the left)
/*TODO*////*TODO*/// *   6  | -----x---------- | shadow
/*TODO*////*TODO*/// *   6  | xxxxxxxxxxxxxxxx | "color", but depends on external connections
/*TODO*////*TODO*/// *   7  | ---------------- |
/*TODO*////*TODO*/// *
/*TODO*////*TODO*/// * shadow enables transparent shadows. Note that it applies to pen 0x0f ONLY.
/*TODO*////*TODO*/// * The rest of the sprite remains normal.
/*TODO*////*TODO*/// */
/*TODO*////*TODO*///
/*TODO*////*TODO*///void K053247_sprites_draw(struct osd_bitmap *bitmap,int min_priority,int max_priority)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///#define NUM_SPRITES 256
/*TODO*////*TODO*///	int offs,pri_code;
/*TODO*////*TODO*///	int sortedlist[NUM_SPRITES];
/*TODO*////*TODO*///
/*TODO*////*TODO*///	for (offs = 0;offs < NUM_SPRITES;offs++)
/*TODO*////*TODO*///		sortedlist[offs] = -1;
/*TODO*////*TODO*///
/*TODO*////*TODO*///	/* prebuild a sorted table */
/*TODO*////*TODO*///	for (offs = 0;offs < 0x1000;offs += 16)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*/////		if (READ_WORD(&K053247_ram[offs]) & 0x8000)
/*TODO*////*TODO*///			sortedlist[READ_WORD(&K053247_ram[offs]) & 0x00ff] = offs;
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///
/*TODO*////*TODO*///	for (pri_code = NUM_SPRITES-1;pri_code >= 0;pri_code--)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		int ox,oy,color,code,size,w,h,x,y,xa,ya,flipx,flipy,mirrorx,mirrory,zoomx,zoomy,pri;
/*TODO*////*TODO*///		/* sprites can be grouped up to 8x8. The draw order is
/*TODO*////*TODO*///			 0  1  4  5 16 17 20 21
/*TODO*////*TODO*///			 2  3  6  7 18 19 22 23
/*TODO*////*TODO*///			 8  9 12 13 24 25 28 29
/*TODO*////*TODO*///			10 11 14 15 26 27 30 31
/*TODO*////*TODO*///			32 33 36 37 48 49 52 53
/*TODO*////*TODO*///			34 35 38 39 50 51 54 55
/*TODO*////*TODO*///			40 41 44 45 56 57 60 61
/*TODO*////*TODO*///			42 43 46 47 58 59 62 63
/*TODO*////*TODO*///		*/
/*TODO*////*TODO*///		static int xoffset[8] = { 0, 1, 4, 5, 16, 17, 20, 21 };
/*TODO*////*TODO*///		static int yoffset[8] = { 0, 2, 8, 10, 32, 34, 40, 42 };
/*TODO*////*TODO*///		static int offsetkludge;
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///		offs = sortedlist[pri_code];
/*TODO*////*TODO*///		if (offs == -1) continue;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		if ((READ_WORD(&K053247_ram[offs]) & 0x8000) == 0) continue;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		code = READ_WORD(&K053247_ram[offs+0x02]);
/*TODO*////*TODO*///		color = READ_WORD(&K053247_ram[offs+0x0c]);
/*TODO*////*TODO*///		pri = 0;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		(*K053247_callback)(&code,&color,&pri);
/*TODO*////*TODO*///
/*TODO*////*TODO*///		if (pri < min_priority || pri > max_priority) continue;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		size = (READ_WORD(&K053247_ram[offs]) & 0x0f00) >> 8;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		w = 1 << (size & 0x03);
/*TODO*////*TODO*///		h = 1 << ((size >> 2) & 0x03);
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* the sprite can start at any point in the 8x8 grid. We have to */
/*TODO*////*TODO*///		/* adjust the offsets to draw it correctly. Simpsons does this all the time. */
/*TODO*////*TODO*///		xa = 0;
/*TODO*////*TODO*///		ya = 0;
/*TODO*////*TODO*///		if (code & 0x01) xa += 1;
/*TODO*////*TODO*///		if (code & 0x02) ya += 1;
/*TODO*////*TODO*///		if (code & 0x04) xa += 2;
/*TODO*////*TODO*///		if (code & 0x08) ya += 2;
/*TODO*////*TODO*///		if (code & 0x10) xa += 4;
/*TODO*////*TODO*///		if (code & 0x20) ya += 4;
/*TODO*////*TODO*///		code &= ~0x3f;
/*TODO*////*TODO*///
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* zoom control:
/*TODO*////*TODO*///		   0x40 = normal scale
/*TODO*////*TODO*///		  <0x40 enlarge (0x20 = double size)
/*TODO*////*TODO*///		  >0x40 reduce (0x80 = half size)
/*TODO*////*TODO*///		*/
/*TODO*////*TODO*///		zoomy = READ_WORD(&K053247_ram[offs+0x08]);
/*TODO*////*TODO*///		if (zoomy > 0x2000) continue;
/*TODO*////*TODO*///		if (zoomy) zoomy = (0x400000+zoomy/2) / zoomy;
/*TODO*////*TODO*///		else zoomy = 2 * 0x400000;
/*TODO*////*TODO*///		if ((READ_WORD(&K053247_ram[offs]) & 0x4000) == 0)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			zoomx = READ_WORD(&K053247_ram[offs+0x0a]);
/*TODO*////*TODO*///			if (zoomx > 0x2000) continue;
/*TODO*////*TODO*///			if (zoomx) zoomx = (0x400000+zoomx/2) / zoomx;
/*TODO*////*TODO*///			else zoomx = 2 * 0x400000;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///		else zoomx = zoomy;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		ox = READ_WORD(&K053247_ram[offs+0x06]);
/*TODO*////*TODO*///		oy = READ_WORD(&K053247_ram[offs+0x04]);
/*TODO*////*TODO*///
/*TODO*////*TODO*////* TODO: it is not known how the global Y offset works */
/*TODO*////*TODO*///switch (K053247_spriteoffsY)
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	case 0x0261:	/* simpsons */
/*TODO*////*TODO*///	case 0x0262:	/* simpsons (dreamland) */
/*TODO*////*TODO*///	case 0x0263:	/* simpsons (dreamland) */
/*TODO*////*TODO*///	case 0x0264:	/* simpsons (dreamland) */
/*TODO*////*TODO*///	case 0x0265:	/* simpsons (dreamland) */
/*TODO*////*TODO*///	case 0x006d:	/* simpsons flip (dreamland) */
/*TODO*////*TODO*///	case 0x006e:	/* simpsons flip (dreamland) */
/*TODO*////*TODO*///	case 0x006f:	/* simpsons flip (dreamland) */
/*TODO*////*TODO*///	case 0x0070:	/* simpsons flip (dreamland) */
/*TODO*////*TODO*///	case 0x0071:	/* simpsons flip */
/*TODO*////*TODO*///		offsetkludge = 0x017;
/*TODO*////*TODO*///		break;
/*TODO*////*TODO*///	case 0x02f7:	/* vendetta (level 4 boss) */
/*TODO*////*TODO*///	case 0x02f8:	/* vendetta (level 4 boss) */
/*TODO*////*TODO*///	case 0x02f9:	/* vendetta (level 4 boss) */
/*TODO*////*TODO*///	case 0x02fa:	/* vendetta */
/*TODO*////*TODO*///	case 0x02fb:	/* vendetta (fat guy jumping) */
/*TODO*////*TODO*///	case 0x02fc:	/* vendetta (fat guy jumping) */
/*TODO*////*TODO*///	case 0x02fd:	/* vendetta (fat guy jumping) */
/*TODO*////*TODO*///	case 0x02fe:	/* vendetta (fat guy jumping) */
/*TODO*////*TODO*///	case 0x02ff:	/* vendetta (fat guy jumping) */
/*TODO*////*TODO*///	case 0x03f7:	/* vendetta flip (level 4 boss) */
/*TODO*////*TODO*///	case 0x03f8:	/* vendetta flip (level 4 boss) */
/*TODO*////*TODO*///	case 0x03f9:	/* vendetta flip (level 4 boss) */
/*TODO*////*TODO*///	case 0x03fa:	/* vendetta flip */
/*TODO*////*TODO*///	case 0x03fb:	/* vendetta flip (fat guy jumping) */
/*TODO*////*TODO*///	case 0x03fc:	/* vendetta flip (fat guy jumping) */
/*TODO*////*TODO*///	case 0x03fd:	/* vendetta flip (fat guy jumping) */
/*TODO*////*TODO*///	case 0x03fe:	/* vendetta flip (fat guy jumping) */
/*TODO*////*TODO*///	case 0x03ff:	/* vendetta flip (fat guy jumping) */
/*TODO*////*TODO*///		offsetkludge = 0x006;
/*TODO*////*TODO*///		break;
/*TODO*////*TODO*///	case 0x0292:	/* xmen */
/*TODO*////*TODO*///	case 0x0072:	/* xmen flip */
/*TODO*////*TODO*///		offsetkludge = -0x002;
/*TODO*////*TODO*///		break;
/*TODO*////*TODO*///	default:
/*TODO*////*TODO*///		offsetkludge = 0;
/*TODO*////*TODO*///			usrintf_showmessage("unknown spriteoffsY %04x",K053247_spriteoffsY);
/*TODO*////*TODO*///		break;
/*TODO*////*TODO*///}
/*TODO*////*TODO*///
/*TODO*////*TODO*///		flipx = READ_WORD(&K053247_ram[offs]) & 0x1000;
/*TODO*////*TODO*///		flipy = READ_WORD(&K053247_ram[offs]) & 0x2000;
/*TODO*////*TODO*///		mirrorx = READ_WORD(&K053247_ram[offs+0x0c]) & 0x4000;
/*TODO*////*TODO*///		mirrory = READ_WORD(&K053247_ram[offs+0x0c]) & 0x8000;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		if (K053247_flipscreenX)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			ox = -ox;
/*TODO*////*TODO*///			if (!mirrorx) flipx = !flipx;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///		if (K053247_flipscreenY)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			oy = -oy;
/*TODO*////*TODO*///			if (!mirrory) flipy = !flipy;
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///
/*TODO*////*TODO*///		ox = (ox + 0x35 - K053247_spriteoffsX) & 0x3ff;
/*TODO*////*TODO*///		if (ox >= 768) ox -= 1024;
/*TODO*////*TODO*///		oy = (-(oy + K053247_spriteoffsY + offsetkludge)) & 0x3ff;
/*TODO*////*TODO*///		if (oy >= 640) oy -= 1024;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		/* the coordinates given are for the *center* of the sprite */
/*TODO*////*TODO*///		ox -= (zoomx * w) >> 13;
/*TODO*////*TODO*///		oy -= (zoomy * h) >> 13;
/*TODO*////*TODO*///
/*TODO*////*TODO*///		for (y = 0;y < h;y++)
/*TODO*////*TODO*///		{
/*TODO*////*TODO*///			int sx,sy,zw,zh;
/*TODO*////*TODO*///
/*TODO*////*TODO*///			sy = oy + ((zoomy * y + (1<<11)) >> 12);
/*TODO*////*TODO*///			zh = (oy + ((zoomy * (y+1) + (1<<11)) >> 12)) - sy;
/*TODO*////*TODO*///
/*TODO*////*TODO*///			for (x = 0;x < w;x++)
/*TODO*////*TODO*///			{
/*TODO*////*TODO*///				int c,fx,fy;
/*TODO*////*TODO*///
/*TODO*////*TODO*///				sx = ox + ((zoomx * x + (1<<11)) >> 12);
/*TODO*////*TODO*///				zw = (ox + ((zoomx * (x+1) + (1<<11)) >> 12)) - sx;
/*TODO*////*TODO*///				c = code;
/*TODO*////*TODO*///				if (mirrorx)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					if ((flipx == 0) ^ (2*x < w))
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						/* mirror left/right */
/*TODO*////*TODO*///						c += xoffset[(w-1-x+xa)&7];
/*TODO*////*TODO*///						fx = 1;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///					else
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						c += xoffset[(x+xa)&7];
/*TODO*////*TODO*///						fx = 0;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///				else
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					if (flipx) c += xoffset[(w-1-x+xa)&7];
/*TODO*////*TODO*///					else c += xoffset[(x+xa)&7];
/*TODO*////*TODO*///					fx = flipx;
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///				if (mirrory)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					if ((flipy == 0) ^ (2*y >= h))
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						/* mirror top/bottom */
/*TODO*////*TODO*///						c += yoffset[(h-1-y+ya)&7];
/*TODO*////*TODO*///						fy = 1;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///					else
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						c += yoffset[(y+ya)&7];
/*TODO*////*TODO*///						fy = 0;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///				else
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					if (flipy) c += yoffset[(h-1-y+ya)&7];
/*TODO*////*TODO*///					else c += yoffset[(y+ya)&7];
/*TODO*////*TODO*///					fy = flipy;
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///
/*TODO*////*TODO*///				if (zoomx == 0x10000 && zoomy == 0x10000)
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					/* hack to simulate shadow */
/*TODO*////*TODO*///					if (READ_WORD(&K053247_ram[offs+0x0c]) & 0x0400)
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						int o = K053247_gfx->colortable[16*color+15];
/*TODO*////*TODO*///						K053247_gfx->colortable[16*color+15] = palette_transparent_pen;
/*TODO*////*TODO*///						drawgfx(bitmap,K053247_gfx,
/*TODO*////*TODO*///								c,
/*TODO*////*TODO*///								color,
/*TODO*////*TODO*///								fx,fy,
/*TODO*////*TODO*///								sx,sy,
/*TODO*////*TODO*///								&Machine->drv->visible_area,TRANSPARENCY_PENS,(cpu_getcurrentframe() & 1) ? 0x8001 : 0x0001);
/*TODO*////*TODO*///						K053247_gfx->colortable[16*color+15] = o;
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///					else
/*TODO*////*TODO*///						drawgfx(bitmap,K053247_gfx,
/*TODO*////*TODO*///								c,
/*TODO*////*TODO*///								color,
/*TODO*////*TODO*///								fx,fy,
/*TODO*////*TODO*///								sx,sy,
/*TODO*////*TODO*///								&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///				else
/*TODO*////*TODO*///					drawgfxzoom(bitmap,K053247_gfx,
/*TODO*////*TODO*///							c,
/*TODO*////*TODO*///							color,
/*TODO*////*TODO*///							fx,fy,
/*TODO*////*TODO*///							sx,sy,
/*TODO*////*TODO*///							&Machine->drv->visible_area,TRANSPARENCY_PEN,0,
/*TODO*////*TODO*///							(zw << 16) / 16,(zh << 16) / 16);
/*TODO*////*TODO*///
/*TODO*////*TODO*///				if (mirrory && h == 1)  /* Simpsons shadows */
/*TODO*////*TODO*///				{
/*TODO*////*TODO*///					if (zoomx == 0x10000 && zoomy == 0x10000)
/*TODO*////*TODO*///					{
/*TODO*////*TODO*///						/* hack to simulate shadow */
/*TODO*////*TODO*///						if (READ_WORD(&K053247_ram[offs+0x0c]) & 0x0400)
/*TODO*////*TODO*///						{
/*TODO*////*TODO*///							int o = K053247_gfx->colortable[16*color+15];
/*TODO*////*TODO*///							K053247_gfx->colortable[16*color+15] = palette_transparent_pen;
/*TODO*////*TODO*///							drawgfx(bitmap,K053247_gfx,
/*TODO*////*TODO*///									c,
/*TODO*////*TODO*///									color,
/*TODO*////*TODO*///									fx,!fy,
/*TODO*////*TODO*///									sx,sy,
/*TODO*////*TODO*///									&Machine->drv->visible_area,TRANSPARENCY_PENS,(cpu_getcurrentframe() & 1) ? 0x8001 : 0x0001);
/*TODO*////*TODO*///							K053247_gfx->colortable[16*color+15] = o;
/*TODO*////*TODO*///						}
/*TODO*////*TODO*///						else
/*TODO*////*TODO*///							drawgfx(bitmap,K053247_gfx,
/*TODO*////*TODO*///									c,
/*TODO*////*TODO*///									color,
/*TODO*////*TODO*///									fx,!fy,
/*TODO*////*TODO*///									sx,sy,
/*TODO*////*TODO*///									&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
/*TODO*////*TODO*///					}
/*TODO*////*TODO*///					else
/*TODO*////*TODO*///						drawgfxzoom(bitmap,K053247_gfx,
/*TODO*////*TODO*///								c,
/*TODO*////*TODO*///								color,
/*TODO*////*TODO*///								fx,!fy,
/*TODO*////*TODO*///								sx,sy,
/*TODO*////*TODO*///								&Machine->drv->visible_area,TRANSPARENCY_PEN,0,
/*TODO*////*TODO*///								(zw << 16) / 16,(zh << 16) / 16);
/*TODO*////*TODO*///				}
/*TODO*////*TODO*///			}
/*TODO*////*TODO*///		}
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///#if 0
/*TODO*////*TODO*///if (keyboard_pressed(KEYCODE_D))
/*TODO*////*TODO*///{
/*TODO*////*TODO*///	FILE *fp;
/*TODO*////*TODO*///	fp=fopen("SPRITE.DMP", "w+b");
/*TODO*////*TODO*///	if (fp)
/*TODO*////*TODO*///	{
/*TODO*////*TODO*///		fwrite(K053247_ram, 0x1000, 1, fp);
/*TODO*////*TODO*///		usrintf_showmessage("saved");
/*TODO*////*TODO*///		fclose(fp);
/*TODO*////*TODO*///	}
/*TODO*////*TODO*///}
/*TODO*////*TODO*///#endif
/*TODO*////*TODO*///#undef NUM_SPRITES
/*TODO*////*TODO*///}

public static void K053247_mark_sprites_colors()
{
	int offs,i;

	/*unsigned short*/int[] palette_map=new int[512];

	//memset (palette_map, 0, sizeof (palette_map));

	/* sprites */
	for (offs = 0x1000-16;offs >= 0;offs -= 16)
	{
		if ((K053247_ram.READ_WORD(offs) & 0x8000)!=0)
		{
			int[] code=new int[1];
                        int[] color=new int[1];
                        int[] pri=new int[1];

			code[0] = K053247_ram.READ_WORD(offs+0x02);
			color[0] = K053247_ram.READ_WORD(offs+0x0c);
			pri[0] = 0;
			K053247_callback.handler(code,color,pri);
			palette_map[color[0]] |= 0xffff;
		}
	}

	/* now build the final table */
	for (i = 0; i < 512; i++)
	{
		int usage = palette_map[i], j;
		if (usage!=0)
		{
			for (j = 1; j < 16; j++)
				if ((usage & (1 << j))!=0)
					palette_used_colors.write(i * 16 + j,palette_used_colors.read(i * 16 + j) | PALETTE_COLOR_VISIBLE);
		}
	}
}

    public static int K053247_is_IRQ_enabled() {
        return K053247_irq_enabled;
    }
}
