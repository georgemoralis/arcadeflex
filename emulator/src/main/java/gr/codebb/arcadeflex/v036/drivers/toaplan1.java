/**
 * *************************************************************************
 *
 * ToaPlan game hardware from 1988-1991
 * ------------------------------------
 * MAME Driver by: Darren Olafson
 * Technical info: Carl-Henrik Sk�rstedt    Magnus Danielsson
 * Driver updates: Quench
 * Video updates : SUZ
 *
 *
 * Supported games:
 *
 * ROM set		Toaplan
 * name		board No		Game name
 * --------------------------------------------------
 * rallybik	TP-012		Rally Bike/Dash Yarou
 * truxton		TP-013B		Truxton/Tatsujin
 * hellfire	TP-???		HellFire
 * zerowing	TP-015		Zero Wing
 * demonwld	TP-016		Demon's World/Horror Story
 * samesame	TP-017		Same Same Same! (Japan)	 [1989]
 * fireshrk	TP-017		Fire Shark (World)		 [1990]
 * outzone		TP-018		Out Zone
 * outzonep	??????		Out Zone (Pirate).  Note this uses different ROM
 * layout for GFX ROMs. Result is the same though.
 * See ROM load for details.
 * vimana		TP-019		Vimana
 * vimana2		TP-019		Vimana (alternate)
 * vimanan		TP-019		Vimana (Nova Apparate GMBH  Co  license)
 *
 * To Do:
 * Add support for HD647180 (Z180) sound CPUs (once their internal
 * ROMS are dumped). These are:
 * Fire Shark
 * Vimana
 *
 **************************************************************************
 */

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package gr.codebb.arcadeflex.v036.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.sound._3812intfH.*;
import static gr.codebb.arcadeflex.v037b7.sound._3526intf.*;
import static gr.codebb.arcadeflex.v036.machine.toaplan1.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.toaplan1.*;

public class toaplan1 {

    public static UBytePtr toaplan1_sharedram = new UBytePtr();

    static MemoryReadAddress rallybik_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x07ffff, MRA_ROM),
                new MemoryReadAddress(0x080000, 0x083fff, MRA_BANK1),
                new MemoryReadAddress(0x0c0000, 0x0c0fff, MRA_BANK2), /* sprite ram data */
                new MemoryReadAddress(0x100002, 0x100003, video_ofs3_r),
                new MemoryReadAddress(0x100004, 0x100007, rallybik_videoram3_r), /* tile layers */
                new MemoryReadAddress(0x100010, 0x10001f, scrollregs_r),
                new MemoryReadAddress(0x140000, 0x140001, input_port_0_r),
                new MemoryReadAddress(0x144000, 0x1447ff, toaplan1_colorram1_r),
                new MemoryReadAddress(0x146000, 0x1467ff, toaplan1_colorram2_r),
                new MemoryReadAddress(0x180000, 0x180fff, toaplan1_shared_r),
                new MemoryReadAddress(-1)
            };
    static MemoryWriteAddress rallybik_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x07ffff, MWA_ROM),
                new MemoryWriteAddress(0x080000, 0x083fff, MWA_BANK1),
                new MemoryWriteAddress(0x0c0000, 0x0c0fff, MWA_BANK2, spriteram, spriteram_size), /* sprite ram data */
                new MemoryWriteAddress(0x100002, 0x100003, video_ofs3_w),
                new MemoryWriteAddress(0x100004, 0x100007, toaplan1_videoram3_w), /* tile layers */
                new MemoryWriteAddress(0x100010, 0x10001f, scrollregs_w),
                new MemoryWriteAddress(0x140000, 0x140001, toaplan1_int_enable_w),
                new MemoryWriteAddress(0x140008, 0x14000f, layers_offset_w),
                new MemoryWriteAddress(0x144000, 0x1447ff, toaplan1_colorram1_w, toaplan1_colorram1, colorram1_size),
                new MemoryWriteAddress(0x146000, 0x1467ff, toaplan1_colorram2_w, toaplan1_colorram2, colorram2_size),
                new MemoryWriteAddress(0x180000, 0x180fff, toaplan1_shared_w, toaplan1_sharedram),
                new MemoryWriteAddress(0x1c0000, 0x1c0003, offsetregs_w),
                new MemoryWriteAddress(-1)
            };

    static MemoryReadAddress truxton_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x07ffff, MRA_ROM),
                new MemoryReadAddress(0x080000, 0x083fff, MRA_BANK1),
                new MemoryReadAddress(0x0c0000, 0x0c0001, input_port_0_r),
                new MemoryReadAddress(0x0c0002, 0x0c0003, video_ofs_r),
                new MemoryReadAddress(0x0c0004, 0x0c0005, toaplan1_videoram1_r), /* sprites info */
                new MemoryReadAddress(0x0c0006, 0x0c0007, toaplan1_videoram2_r), /* sprite size ? */
                new MemoryReadAddress(0x100002, 0x100003, video_ofs3_r),
                new MemoryReadAddress(0x100004, 0x100007, toaplan1_videoram3_r), /* tile layers */
                new MemoryReadAddress(0x100010, 0x10001f, scrollregs_r),
                new MemoryReadAddress(0x144000, 0x1447ff, toaplan1_colorram1_r),
                new MemoryReadAddress(0x146000, 0x1467ff, toaplan1_colorram2_r),
                new MemoryReadAddress(0x180000, 0x180fff, toaplan1_shared_r),
                new MemoryReadAddress(-1)
            };
    static MemoryWriteAddress truxton_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x07ffff, MWA_ROM),
                new MemoryWriteAddress(0x080000, 0x083fff, MWA_BANK1),
                new MemoryWriteAddress(0x0c0002, 0x0c0003, video_ofs_w),
                new MemoryWriteAddress(0x0c0004, 0x0c0005, toaplan1_videoram1_w), /* sprites info */
                new MemoryWriteAddress(0x0c0006, 0x0c0007, toaplan1_videoram2_w), /* sprite size ? */
                new MemoryWriteAddress(0x100002, 0x100003, video_ofs3_w),
                new MemoryWriteAddress(0x100004, 0x100007, toaplan1_videoram3_w), /* tile layers */
                new MemoryWriteAddress(0x100010, 0x10001f, scrollregs_w),
                new MemoryWriteAddress(0x140000, 0x140001, toaplan1_int_enable_w),
                new MemoryWriteAddress(0x140008, 0x14000f, layers_offset_w),
                new MemoryWriteAddress(0x144000, 0x1447ff, toaplan1_colorram1_w, toaplan1_colorram1, colorram1_size),
                new MemoryWriteAddress(0x146000, 0x1467ff, toaplan1_colorram2_w, toaplan1_colorram2, colorram2_size),
                new MemoryWriteAddress(0x180000, 0x180fff, toaplan1_shared_w, toaplan1_sharedram),
                new MemoryWriteAddress(0x1c0000, 0x1c0003, offsetregs_w),
                new MemoryWriteAddress(0x1c0006, 0x1c0007, toaplan1_flipscreen_w),
                new MemoryWriteAddress(-1)
            };

    static MemoryReadAddress hellfire_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x03ffff, MRA_ROM),
                new MemoryReadAddress(0x040000, 0x047fff, MRA_BANK1),
                new MemoryReadAddress(0x084000, 0x0847ff, toaplan1_colorram1_r),
                new MemoryReadAddress(0x086000, 0x0867ff, toaplan1_colorram2_r),
                new MemoryReadAddress(0x0c0000, 0x0c0fff, toaplan1_shared_r),
                new MemoryReadAddress(0x100002, 0x100003, video_ofs3_r),
                new MemoryReadAddress(0x100004, 0x100007, toaplan1_videoram3_r), /* tile layers */
                new MemoryReadAddress(0x100010, 0x10001f, scrollregs_r),
                new MemoryReadAddress(0x140000, 0x140001, input_port_0_r),
                new MemoryReadAddress(0x140002, 0x140003, video_ofs_r),
                new MemoryReadAddress(0x140004, 0x140005, toaplan1_videoram1_r), /* sprites info */
                new MemoryReadAddress(0x140006, 0x140007, toaplan1_videoram2_r), /* sprite size ? */
                new MemoryReadAddress(-1) /* end of table */};
    static MemoryWriteAddress hellfire_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x03ffff, MWA_ROM),
                new MemoryWriteAddress(0x040000, 0x047fff, MWA_BANK1),
                new MemoryWriteAddress(0x080002, 0x080003, toaplan1_int_enable_w),
                new MemoryWriteAddress(0x080008, 0x08000f, layers_offset_w),
                new MemoryWriteAddress(0x084000, 0x0847ff, toaplan1_colorram1_w, toaplan1_colorram1, colorram1_size),
                new MemoryWriteAddress(0x086000, 0x0867ff, toaplan1_colorram2_w, toaplan1_colorram2, colorram2_size),
                new MemoryWriteAddress(0x0c0000, 0x0c0fff, toaplan1_shared_w, toaplan1_sharedram),
                new MemoryWriteAddress(0x100002, 0x100003, video_ofs3_w),
                new MemoryWriteAddress(0x100004, 0x100007, toaplan1_videoram3_w), /* tile layers */
                new MemoryWriteAddress(0x100010, 0x10001f, scrollregs_w),
                new MemoryWriteAddress(0x140002, 0x140003, video_ofs_w),
                new MemoryWriteAddress(0x140004, 0x140005, toaplan1_videoram1_w), /* sprites info */
                new MemoryWriteAddress(0x140006, 0x140007, toaplan1_videoram2_w), /* sprite size ? */
                new MemoryWriteAddress(0x180000, 0x180003, offsetregs_w),
                new MemoryWriteAddress(0x180006, 0x180007, toaplan1_flipscreen_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress zerowing_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x07ffff, MRA_ROM),
                new MemoryReadAddress(0x080000, 0x087fff, MRA_BANK1),
                new MemoryReadAddress(0x400000, 0x400005, toaplan1_unk_r),
                new MemoryReadAddress(0x404000, 0x4047ff, toaplan1_colorram1_r),
                new MemoryReadAddress(0x406000, 0x4067ff, toaplan1_colorram2_r),
                new MemoryReadAddress(0x440000, 0x440fff, toaplan1_shared_r),
                new MemoryReadAddress(0x480002, 0x480003, video_ofs3_r),
                new MemoryReadAddress(0x480004, 0x480007, toaplan1_videoram3_r), /* tile layers */
                new MemoryReadAddress(0x480010, 0x48001f, scrollregs_r),
                new MemoryReadAddress(0x4c0000, 0x4c0001, input_port_0_r),
                new MemoryReadAddress(0x4c0002, 0x4c0003, video_ofs_r),
                new MemoryReadAddress(0x4c0004, 0x4c0005, toaplan1_videoram1_r), /* sprites info */
                new MemoryReadAddress(0x4c0006, 0x4c0007, toaplan1_videoram2_r), /* sprite size ? */
                new MemoryReadAddress(-1) /* end of table */};
    static MemoryWriteAddress zerowing_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x07ffff, MWA_ROM),
                new MemoryWriteAddress(0x080000, 0x087fff, MWA_BANK1),
                new MemoryWriteAddress(0x0c0000, 0x0c0003, offsetregs_w),
                new MemoryWriteAddress(0x0c0006, 0x0c0007, toaplan1_flipscreen_w),
                new MemoryWriteAddress(0x400002, 0x400003, toaplan1_int_enable_w),
                new MemoryWriteAddress(0x400008, 0x40000f, layers_offset_w),
                new MemoryWriteAddress(0x404000, 0x4047ff, toaplan1_colorram1_w, toaplan1_colorram1, colorram1_size),
                new MemoryWriteAddress(0x406000, 0x4067ff, toaplan1_colorram2_w, toaplan1_colorram2, colorram2_size),
                new MemoryWriteAddress(0x440000, 0x440fff, toaplan1_shared_w, toaplan1_sharedram),
                new MemoryWriteAddress(0x480002, 0x480003, video_ofs3_w),
                new MemoryWriteAddress(0x480004, 0x480007, toaplan1_videoram3_w), /* tile layers */
                new MemoryWriteAddress(0x480010, 0x48001f, scrollregs_w),
                new MemoryWriteAddress(0x4c0002, 0x4c0003, video_ofs_w),
                new MemoryWriteAddress(0x4c0004, 0x4c0005, toaplan1_videoram1_w), /* sprites info */
                new MemoryWriteAddress(0x4c0006, 0x4c0007, toaplan1_videoram2_w), /* sprite size ? */
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress demonwld_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x03ffff, MRA_ROM),
                new MemoryReadAddress(0x400000, 0x400001, input_port_0_r),
                new MemoryReadAddress(0x404000, 0x4047ff, toaplan1_colorram1_r),
                new MemoryReadAddress(0x406000, 0x4067ff, toaplan1_colorram2_r),
                new MemoryReadAddress(0x600000, 0x600fff, toaplan1_shared_r),
                new MemoryReadAddress(0x800002, 0x800003, video_ofs3_r),
                new MemoryReadAddress(0x800004, 0x800007, toaplan1_videoram3_r), /* tile layers */
                new MemoryReadAddress(0x800010, 0x80001f, scrollregs_r),
                new MemoryReadAddress(0xa00000, 0xa00001, input_port_0_r),
                new MemoryReadAddress(0xa00002, 0xa00003, video_ofs_r),
                new MemoryReadAddress(0xa00004, 0xa00005, toaplan1_videoram1_r), /* sprites info */
                new MemoryReadAddress(0xa00006, 0xa00007, toaplan1_videoram2_r), /* sprite size ? */
                new MemoryReadAddress(0xc00000, 0xc03fff, MRA_BANK1),
                new MemoryReadAddress(-1)
            };
    static MemoryWriteAddress demonwld_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x03ffff, MWA_ROM),
                new MemoryWriteAddress(0x340006, 0x340007, toaplan1_flipscreen_w),
                new MemoryWriteAddress(0x404000, 0x4047ff, toaplan1_colorram1_w, toaplan1_colorram1, colorram1_size),
                new MemoryWriteAddress(0x406000, 0x4067ff, toaplan1_colorram2_w, toaplan1_colorram2, colorram2_size),
                new MemoryWriteAddress(0x600000, 0x600fff, toaplan1_shared_w, toaplan1_sharedram),
                new MemoryWriteAddress(0x800002, 0x800003, video_ofs3_w),
                new MemoryWriteAddress(0x800004, 0x800007, toaplan1_videoram3_w), /* tile layers */
                new MemoryWriteAddress(0x800010, 0x80001f, scrollregs_w),
                new MemoryWriteAddress(0x400000, 0x400001, toaplan1_int_enable_w),
                new MemoryWriteAddress(0x400008, 0x40000f, layers_offset_w),
                new MemoryWriteAddress(0xa00002, 0xa00003, video_ofs_w),
                new MemoryWriteAddress(0xa00004, 0xa00005, toaplan1_videoram1_w), /* sprites info */
                new MemoryWriteAddress(0xa00006, 0xa00007, toaplan1_videoram2_w), /* sprite size ? */
                new MemoryWriteAddress(0xc00000, 0xc03fff, MWA_BANK1),
                new MemoryWriteAddress(0xe00000, 0xe00003, offsetregs_w),
                new MemoryWriteAddress(0xe0000a, 0xe0000b, demonwld_dsp_w), /* DSP Comms control */
                new MemoryWriteAddress(-1)
            };

    static MemoryReadAddress samesame_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x00ffff, MRA_ROM),
                new MemoryReadAddress(0x040000, 0x07ffff, MRA_ROM),
                new MemoryReadAddress(0x0c0000, 0x0c3fff, MRA_BANK1),
                new MemoryReadAddress(0x100000, 0x100001, toaplan1_vblank_r),
                new MemoryReadAddress(0x104000, 0x1047ff, toaplan1_colorram1_r),
                new MemoryReadAddress(0x106000, 0x1067ff, toaplan1_colorram2_r),
                new MemoryReadAddress(0x140000, 0x140001, input_port_1_r),
                new MemoryReadAddress(0x140002, 0x140003, input_port_2_r),
                new MemoryReadAddress(0x140004, 0x140005, input_port_3_r),
                new MemoryReadAddress(0x140006, 0x140007, input_port_4_r),
                new MemoryReadAddress(0x140008, 0x140009, input_port_5_r),
                new MemoryReadAddress(0x14000a, 0x14000b, samesame_port_6_r), /* Territory, and MCU ready */
                new MemoryReadAddress(0x180002, 0x180003, video_ofs3_r),
                new MemoryReadAddress(0x180004, 0x180007, toaplan1_videoram3_r), /* tile layers */
                new MemoryReadAddress(0x180010, 0x18001f, scrollregs_r),
                new MemoryReadAddress(0x1c0000, 0x1c0001, input_port_0_r),
                new MemoryReadAddress(0x1c0002, 0x1c0003, video_ofs_r),
                new MemoryReadAddress(0x1c0004, 0x1c0005, toaplan1_videoram1_r), /* sprites info */
                new MemoryReadAddress(0x1c0006, 0x1c0007, toaplan1_videoram2_r), /* sprite size ? */
                new MemoryReadAddress(-1) /* end of table */};
    static MemoryWriteAddress samesame_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x00ffff, MWA_ROM),
                new MemoryWriteAddress(0x040000, 0x07ffff, MWA_ROM),
                new MemoryWriteAddress(0x080000, 0x080003, offsetregs_w),
                new MemoryWriteAddress(0x080006, 0x080007, toaplan1_flipscreen_w),
                new MemoryWriteAddress(0x0c0000, 0x0c3fff, MWA_BANK1), /* Frame done at $c1ada */
                /*	new MemoryWriteAddress( 0x100000, 0x100001, ??? ),				disable palette refresh ? */
                new MemoryWriteAddress(0x100002, 0x100003, toaplan1_int_enable_w),
                new MemoryWriteAddress(0x100008, 0x10000f, layers_offset_w),
                new MemoryWriteAddress(0x104000, 0x1047ff, toaplan1_colorram1_w, toaplan1_colorram1, colorram1_size),
                new MemoryWriteAddress(0x106000, 0x1067ff, toaplan1_colorram2_w, toaplan1_colorram2, colorram2_size),
                new MemoryWriteAddress(0x14000c, 0x14000d, toaplan1_coin_w), /* Coin counter/lockout */
                //	new MemoryWriteAddress( 0x14000e, 0x14000f, samesame_mcu_w ),		/* Commands sent to HD647180 */
                new MemoryWriteAddress(0x180002, 0x180003, video_ofs3_w),
                new MemoryWriteAddress(0x180004, 0x180007, toaplan1_videoram3_w), /* tile layers */
                new MemoryWriteAddress(0x180010, 0x18001f, scrollregs_w),
                /*	new MemoryWriteAddress( 0x1c0000, 0x1c0001, ??? ),				disable sprite refresh ? */
                new MemoryWriteAddress(0x1c0002, 0x1c0003, video_ofs_w),
                new MemoryWriteAddress(0x1c0004, 0x1c0005, toaplan1_videoram1_w), /* sprites info */
                new MemoryWriteAddress(0x1c0006, 0x1c0007, toaplan1_videoram2_w), /* sprite size ? */
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress outzone_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x07ffff, MRA_ROM),
                new MemoryReadAddress(0x100000, 0x100001, input_port_0_r),
                new MemoryReadAddress(0x100002, 0x100003, video_ofs_r),
                new MemoryReadAddress(0x100004, 0x100005, toaplan1_videoram1_r), /* sprites info */
                new MemoryReadAddress(0x100006, 0x100007, toaplan1_videoram2_r),
                new MemoryReadAddress(0x140000, 0x140fff, toaplan1_shared_r),
                new MemoryReadAddress(0x200002, 0x200003, video_ofs3_r),
                new MemoryReadAddress(0x200004, 0x200007, toaplan1_videoram3_r), /* tile layers */
                new MemoryReadAddress(0x200010, 0x20001f, scrollregs_r),
                new MemoryReadAddress(0x240000, 0x243fff, MRA_BANK1),
                new MemoryReadAddress(0x300000, 0x300001, toaplan1_vblank_r),
                new MemoryReadAddress(0x304000, 0x3047ff, toaplan1_colorram1_r),
                new MemoryReadAddress(0x306000, 0x3067ff, toaplan1_colorram2_r),
                new MemoryReadAddress(-1)
            };
    static MemoryWriteAddress outzone_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x07ffff, MWA_ROM),
                new MemoryWriteAddress(0x100002, 0x100003, video_ofs_w),
                new MemoryWriteAddress(0x100004, 0x100005, toaplan1_videoram1_w), /* sprites info */
                new MemoryWriteAddress(0x100006, 0x100007, toaplan1_videoram2_w),
                new MemoryWriteAddress(0x140000, 0x140fff, toaplan1_shared_w, toaplan1_sharedram),
                new MemoryWriteAddress(0x200002, 0x200003, video_ofs3_w),
                new MemoryWriteAddress(0x200004, 0x200007, toaplan1_videoram3_w), /* tile layers */
                new MemoryWriteAddress(0x200010, 0x20001f, scrollregs_w),
                new MemoryWriteAddress(0x240000, 0x243fff, MWA_BANK1),
                new MemoryWriteAddress(0x300000, 0x300001, toaplan1_int_enable_w),
                new MemoryWriteAddress(0x300008, 0x30000f, layers_offset_w),
                new MemoryWriteAddress(0x304000, 0x3047ff, toaplan1_colorram1_w, toaplan1_colorram1, colorram1_size),
                new MemoryWriteAddress(0x306000, 0x3067ff, toaplan1_colorram2_w, toaplan1_colorram2, colorram2_size),
                new MemoryWriteAddress(0x340000, 0x340003, offsetregs_w),
                new MemoryWriteAddress(0x340006, 0x340007, toaplan1_flipscreen_w),
                new MemoryWriteAddress(-1)
            };

    static MemoryReadAddress vimana_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x03ffff, MRA_ROM),
                new MemoryReadAddress(0x0c0000, 0x0c0001, input_port_0_r),
                new MemoryReadAddress(0x0c0002, 0x0c0003, video_ofs_r),
                new MemoryReadAddress(0x0c0004, 0x0c0005, toaplan1_videoram1_r), /* sprites info */
                new MemoryReadAddress(0x0c0006, 0x0c0007, toaplan1_videoram2_r), /* sprite size ? */
                new MemoryReadAddress(0x400000, 0x400001, toaplan1_vblank_r),
                new MemoryReadAddress(0x404000, 0x4047ff, toaplan1_colorram1_r),
                new MemoryReadAddress(0x406000, 0x4067ff, toaplan1_colorram2_r),
                new MemoryReadAddress(0x440000, 0x440005, vimana_mcu_r),
                new MemoryReadAddress(0x440006, 0x440007, input_port_3_r),
                new MemoryReadAddress(0x440008, 0x440009, vimana_input_port_5_r),
                new MemoryReadAddress(0x44000a, 0x44000b, input_port_1_r),
                new MemoryReadAddress(0x44000c, 0x44000d, input_port_2_r),
                new MemoryReadAddress(0x44000e, 0x44000f, input_port_4_r),
                new MemoryReadAddress(0x440010, 0x440011, input_port_6_r),
                new MemoryReadAddress(0x480000, 0x487fff, MRA_BANK1),
                new MemoryReadAddress(0x4c0000, 0x4c0001, toaplan1_unk_r),
                new MemoryReadAddress(0x4c0002, 0x4c0003, video_ofs3_r),
                new MemoryReadAddress(0x4c0004, 0x4c0007, toaplan1_videoram3_r), /* tile layers */
                new MemoryReadAddress(0x4c0010, 0x4c001f, scrollregs_r),
                new MemoryReadAddress(-1) /* end of table */};
    static MemoryWriteAddress vimana_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x03ffff, MWA_ROM),
                new MemoryWriteAddress(0x080000, 0x080003, offsetregs_w),
                new MemoryWriteAddress(0x080006, 0x080007, toaplan1_flipscreen_w),
                new MemoryWriteAddress(0x0c0002, 0x0c0003, video_ofs_w),
                new MemoryWriteAddress(0x0c0004, 0x0c0005, toaplan1_videoram1_w), /* sprites info */
                new MemoryWriteAddress(0x0c0006, 0x0c0007, toaplan1_videoram2_w), /* sprite size ? */
                new MemoryWriteAddress(0x400002, 0x400003, toaplan1_int_enable_w), /* IRQACK? */
                new MemoryWriteAddress(0x400008, 0x40000f, layers_offset_w),
                new MemoryWriteAddress(0x404000, 0x4047ff, toaplan1_colorram1_w, toaplan1_colorram1, colorram1_size),
                new MemoryWriteAddress(0x406000, 0x4067ff, toaplan1_colorram2_w, toaplan1_colorram2, colorram2_size),
                new MemoryWriteAddress(0x440000, 0x440005, vimana_mcu_w),
                new MemoryWriteAddress(0x480000, 0x487fff, MWA_BANK1),
                new MemoryWriteAddress(0x4c0002, 0x4c0003, video_ofs3_w),
                new MemoryWriteAddress(0x4c0004, 0x4c0007, toaplan1_videoram3_w), /* tile layers */
                new MemoryWriteAddress(0x4c0010, 0x4c001f, scrollregs_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0xffff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};
    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0xffff, MWA_RAM, toaplan1_sharedram),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort truxton_sound_readport[]
            = {
                new IOReadPort(0x00, 0x00, input_port_1_r), /* Player 1 */
                new IOReadPort(0x10, 0x10, input_port_2_r), /* Player 2 */
                new IOReadPort(0x20, 0x20, input_port_5_r), /* Coin/Start inputs */
                new IOReadPort(0x40, 0x40, input_port_3_r), /* DSW1 */
                new IOReadPort(0x50, 0x50, input_port_4_r), /* DSW2 */
                new IOReadPort(0x60, 0x60, YM3812_status_port_0_r),
                new IOReadPort(0x70, 0x70, input_port_6_r), /* Territory Jumper Block for Truxton */
                new IOReadPort(-1) /* end of table */};
    static IOWritePort truxton_sound_writeport[]
            = {
                new IOWritePort(0x30, 0x30, toaplan1_coin_w), /* Coin counter/lockout */
                new IOWritePort(0x60, 0x60, YM3812_control_port_0_w),
                new IOWritePort(0x61, 0x61, YM3812_write_port_0_w),
                new IOWritePort(-1) /* end of table */};
    static IOWritePort rallybik_sound_writeport[]
            = {
                new IOWritePort(0x30, 0x30, rallybik_coin_w), /* Coin counter/lockout */
                new IOWritePort(0x60, 0x60, YM3812_control_port_0_w),
                new IOWritePort(0x61, 0x61, YM3812_write_port_0_w),
                new IOWritePort(-1) /* end of table */};

    static IOReadPort hellfire_sound_readport[]
            = {
                new IOReadPort(0x00, 0x00, input_port_3_r), /* DSW1 */
                new IOReadPort(0x10, 0x10, input_port_4_r), /* DSW2 */
                new IOReadPort(0x20, 0x20, input_port_6_r), /* Territory Jumper Block */
                new IOReadPort(0x40, 0x40, input_port_1_r), /* Player 1 */
                new IOReadPort(0x50, 0x50, input_port_2_r), /* Player 2 */
                new IOReadPort(0x60, 0x60, input_port_5_r), /* Coin/Start inputs */
                new IOReadPort(0x70, 0x70, YM3812_status_port_0_r),
                new IOReadPort(-1) /* end of table */};
    static IOWritePort hellfire_sound_writeport[]
            = {
                new IOWritePort(0x30, 0x30, toaplan1_coin_w), /* Coin counter/lockout */
                new IOWritePort(0x70, 0x70, YM3812_control_port_0_w),
                new IOWritePort(0x71, 0x71, YM3812_write_port_0_w),
                new IOWritePort(-1) /* end of table */};

    static IOReadPort zerowing_sound_readport[]
            = {
                new IOReadPort(0x00, 0x00, input_port_1_r), /* Player 1 */
                new IOReadPort(0x08, 0x08, input_port_2_r), /* Player 2 */
                new IOReadPort(0x20, 0x20, input_port_3_r), /* DSW1 */
                new IOReadPort(0x28, 0x28, input_port_4_r), /* DSW2 */
                new IOReadPort(0x80, 0x80, input_port_5_r), /* Coin/Start inputs */
                new IOReadPort(0x88, 0x88, input_port_6_r), /* Territory Jumper Block */
                new IOReadPort(0xa8, 0xa8, YM3812_status_port_0_r),
                new IOReadPort(-1) /* end of table */};
    static IOWritePort zerowing_sound_writeport[]
            = {
                new IOWritePort(0xa0, 0xa0, toaplan1_coin_w), /* Coin counter/lockout */
                new IOWritePort(0xa8, 0xa8, YM3812_control_port_0_w),
                new IOWritePort(0xa9, 0xa9, YM3812_write_port_0_w),
                new IOWritePort(-1) /* end of table */};

    static IOReadPort demonwld_sound_readport[]
            = {
                new IOReadPort(0x00, 0x00, YM3812_status_port_0_r),
                new IOReadPort(0x20, 0x20, input_port_6_r),
                new IOReadPort(0x60, 0x60, input_port_5_r),
                new IOReadPort(0x80, 0x80, input_port_1_r),
                new IOReadPort(0xa0, 0xa0, input_port_4_r),
                new IOReadPort(0xc0, 0xc0, input_port_2_r),
                new IOReadPort(0xe0, 0xe0, input_port_3_r),
                new IOReadPort(-1) /* end of table */};
    static IOWritePort demonwld_sound_writeport[]
            = {
                new IOWritePort(0x00, 0x00, YM3812_control_port_0_w),
                new IOWritePort(0x01, 0x01, YM3812_write_port_0_w),
                new IOWritePort(0x40, 0x40, toaplan1_coin_w), /* Coin counter/lockout */
                new IOWritePort(-1) /* end of table */};

    static IOReadPort outzone_sound_readport[]
            = {
                new IOReadPort(0x08, 0x08, input_port_3_r),
                new IOReadPort(0x0c, 0x0c, input_port_4_r),
                new IOReadPort(0x10, 0x10, input_port_5_r),
                new IOReadPort(0x14, 0x14, input_port_1_r),
                new IOReadPort(0x18, 0x18, input_port_2_r),
                new IOReadPort(0x1c, 0x1c, input_port_6_r),
                new IOReadPort(0x00, 0x00, YM3812_status_port_0_r),
                new IOReadPort(-1) /* end of table */};
    static IOWritePort outzone_sound_writeport[]
            = {
                new IOWritePort(0x00, 0x00, YM3812_control_port_0_w),
                new IOWritePort(0x01, 0x01, YM3812_write_port_0_w),
                new IOWritePort(0x04, 0x04, toaplan1_coin_w), /* Coin counter/lockout */
                new IOWritePort(-1) /* end of table */};

    static MemoryReadAddress DSP_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x0fff, MRA_ROM), /* 0x800 words */
                new MemoryReadAddress(0x8000, 0x811f, MRA_RAM), /* The real DSP has this at address 0 */
                /* View this at 4000h in the debugger */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress DSP_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0fff, MWA_ROM), /* 0x800 words */
                new MemoryWriteAddress(0x8000, 0x811f, MWA_RAM), /* The real DSP has this at address 0 */
                /* View this at 4000h in the debugger */
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort DSP_readport[]
            = {
                new IOReadPort(0x01, 0x01, demonwld_dsp_in),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort DSP_writeport[]
            = {
                new IOWritePort(0x00, 0x03, demonwld_dsp_out),
                new IOWritePort(-1) /* end of table */};

    /**
     * ***************************************************************************
     * Input Port definitions
     * ***************************************************************************
     */
    static InputPortPtr input_ports_rallybik = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* VBlank */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_VBLANK);
            PORT_BIT(0xfe, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN | IPF_PLAYER1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW A */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x01, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_SERVICE(0x04, IP_ACTIVE_HIGH);
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x20, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_2C"));
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x80, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_2C"));

            PORT_START();
            /* DSW B */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x01, "Easy");
            PORT_DIPSETTING(0x00, "Medium");
            PORT_DIPSETTING(0x02, "Hard");
            PORT_DIPSETTING(0x03, "Hardest");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_DIPNAME(0x30, 0x20, "Territory/Copyright");
            PORT_DIPSETTING(0x20, "World/Taito Corp Japan");
            PORT_DIPSETTING(0x10, "USA/Taito America");
            PORT_DIPSETTING(0x00, "Japan/Taito Corp");
            PORT_DIPSETTING(0x30, "USA/Taito America (Romstar)");
            PORT_DIPNAME(0x40, 0x00, "Dip Switch Display");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, "Allow Continue");
            PORT_DIPSETTING(0x80, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_truxton = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* VBlank */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_VBLANK);
            PORT_BIT(0xfe, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN | IPF_PLAYER1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW A */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x01, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_SERVICE(0x04, IP_ACTIVE_HIGH);
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            /* credits per coin options change, depending on the territory setting */
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x20, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_2C"));
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x80, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_2C"));
            /* The following are coin settings for Japan
		PORT_DIPNAME( 0x30, 0x00, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x30, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0xc0, 0x00, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_6C") );
             */

            PORT_START();
            /* DSW B */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x01, "Easy");
            PORT_DIPSETTING(0x00, "Medium");
            PORT_DIPSETTING(0x02, "Hard");
            PORT_DIPSETTING(0x03, "Hardest");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x04, "50K, every 150K");
            PORT_DIPSETTING(0x00, "70K, every 200K");
            PORT_DIPSETTING(0x08, "100K only");
            PORT_DIPSETTING(0x0c, "None");
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x30, "2");
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x20, "4");
            PORT_DIPSETTING(0x10, "5");
            PORT_DIPNAME(0x40, 0x00, "Show Dip Switches");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, "Allow Continue");
            PORT_DIPSETTING(0x80, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* Territory Jumper Block */
            PORT_DIPNAME(0x07, 0x02, "Territory/Copyright");
            PORT_DIPSETTING(0x02, "World/Taito Corp");
            PORT_DIPSETTING(0x06, "World/Taito America");
            PORT_DIPSETTING(0x04, "US/Taito America");
            PORT_DIPSETTING(0x01, "US/Romstar");
            PORT_DIPSETTING(0x00, "Japan/Taito Corp");
            //	PORT_DIPSETTING(    0x05, "Same as 0x04" );
            //	PORT_DIPSETTING(    0x03, "Same as 0x02" );
            //	PORT_DIPSETTING(    0x07, "Same as 0x06" );
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_hellfire = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* VBlank */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_VBLANK);
            PORT_BIT(0xfe, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN | IPF_PLAYER1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW A */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_SERVICE(0x04, IP_ACTIVE_HIGH);
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x30, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_6C"));

            PORT_START();
            /* DSWB */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x01, "Easy");
            PORT_DIPSETTING(0x00, "Medium");
            PORT_DIPSETTING(0x02, "Hard");
            PORT_DIPSETTING(0x03, "Hardest");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "70K, every 200K");
            PORT_DIPSETTING(0x04, "100K, every 250K");
            PORT_DIPSETTING(0x08, "100K");
            PORT_DIPSETTING(0x0c, "200K");
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x30, "2");
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x20, "4");
            PORT_DIPSETTING(0x10, "5");
            PORT_BITX(0x40, 0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* Territory Jumper block */
            PORT_DIPNAME(0x03, 0x02, "Territory");
            PORT_DIPSETTING(0x02, "Europe");
            //	PORT_DIPSETTING(    0x03, "Europe" );
            PORT_DIPSETTING(0x01, "US");
            PORT_DIPSETTING(0x00, "Japan");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_zerowing = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* VBlank */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_VBLANK);
            PORT_BIT(0xfe, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN | IPF_PLAYER1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW A */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x01, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_SERVICE(0x04, IP_ACTIVE_HIGH);
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x30, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_6C"));

            PORT_START();
            /* DSW B */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x01, "Easy");
            PORT_DIPSETTING(0x00, "Medium");
            PORT_DIPSETTING(0x02, "Hard");
            PORT_DIPSETTING(0x03, "Hardest");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "200K, every 500K");
            PORT_DIPSETTING(0x04, "500K, every 1M");
            PORT_DIPSETTING(0x08, "500K");
            PORT_DIPSETTING(0x0c, "None");
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x30, "2");
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x20, "4");
            PORT_DIPSETTING(0x10, "5");
            PORT_BITX(0x40, 0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, "Allow Continue");
            PORT_DIPSETTING(0x80, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* Territory Jumper block */
            PORT_DIPNAME(0x03, 0x02, "Territory");
            PORT_DIPSETTING(0x02, "Europe");
            //	PORT_DIPSETTING(    0x03, "Europe" );
            PORT_DIPSETTING(0x01, "US");
            PORT_DIPSETTING(0x00, "Japan");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_demonwld = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* VBlank */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_VBLANK);
            PORT_BIT(0xfe, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW A */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_SERVICE(0x04, IP_ACTIVE_HIGH);
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x30, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_6C"));

            PORT_START();
            /* DSW B */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x01, "Easy");
            PORT_DIPSETTING(0x00, "Medium");
            PORT_DIPSETTING(0x02, "Hard");
            PORT_DIPSETTING(0x03, "Hardest");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "30K, every 100K");
            PORT_DIPSETTING(0x04, "50K and 100K");
            PORT_DIPSETTING(0x08, "100K only");
            PORT_DIPSETTING(0x0c, "None");
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x30, "1");
            PORT_DIPSETTING(0x20, "2");
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x10, "5");
            PORT_BITX(0x40, 0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* Territory Jumper Block */
            PORT_DIPNAME(0x03, 0x02, "Territory/Copyright");
            PORT_DIPSETTING(0x02, "World/Taito Japan");
            PORT_DIPSETTING(0x03, "US/Toaplan");
            PORT_DIPSETTING(0x01, "US/Taito America");
            PORT_DIPSETTING(0x00, "Japan/Taito Corp");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_samesame = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* VBlank */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_VBLANK);
            PORT_BIT(0xfe, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN | IPF_PLAYER1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW A */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x01, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_SERVICE(0x04, IP_ACTIVE_HIGH);
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));
            /* settings listed in service mode, but not actually used ???
		PORT_DIPNAME( 0x30, 0x00, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0xc0, 0x00, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_2C") );
             */

            PORT_START();
            /* DSW B */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x01, "Easy");
            PORT_DIPSETTING(0x00, "Medium");
            PORT_DIPSETTING(0x02, "Hard");
            PORT_DIPSETTING(0x03, "Hardest");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x04, "50K, every 150K");
            PORT_DIPSETTING(0x00, "70K, every 200K");
            PORT_DIPSETTING(0x08, "100K");
            PORT_DIPSETTING(0x0c, "None");
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x30, "2");
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x20, "4");
            PORT_DIPSETTING(0x10, "5");
            PORT_BITX(0x40, 0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPSETTING(0x00, "Normal");
            PORT_DIPSETTING(0x40, "Unknown");
            PORT_DIPNAME(0x80, 0x00, "Allow Continue");
            PORT_DIPSETTING(0x80, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* Territory Jumper Block */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_fireshrk = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* VBlank */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_VBLANK);
            PORT_BIT(0xfe, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN | IPF_PLAYER1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW A */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_SERVICE(0x04, IP_ACTIVE_HIGH);
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x30, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_6C"));

            PORT_START();
            /* DSW B */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x01, "Easy");
            PORT_DIPSETTING(0x00, "Medium");
            PORT_DIPSETTING(0x02, "Hard");
            PORT_DIPSETTING(0x03, "Hardest");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x04, "50K, every 150K");
            PORT_DIPSETTING(0x00, "70K, every 200K");
            PORT_DIPSETTING(0x08, "100K");
            PORT_DIPSETTING(0x0c, "None");
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x30, "2");
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x20, "4");
            PORT_DIPSETTING(0x10, "5");
            PORT_BITX(0x40, 0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPSETTING(0x00, "Normal");
            PORT_DIPSETTING(0x40, "Unknown");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* Territory Jumper Block */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x06, 0x02, "Territory");
            PORT_DIPSETTING(0x02, "Europe");
            PORT_DIPSETTING(0x04, "USA");
            PORT_DIPSETTING(0x00, "USA (Romstar)");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_outzone = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* VBlank */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_VBLANK);
            PORT_BIT(0xfe, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW A */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_SERVICE(0x04, IP_ACTIVE_HIGH);
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x30, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_6C"));

            PORT_START();
            /* DSW B */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x01, "Easy");
            PORT_DIPSETTING(0x00, "Medium");
            PORT_DIPSETTING(0x02, "Hard");
            PORT_DIPSETTING(0x03, "Hardest");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "Every 300K");
            PORT_DIPSETTING(0x04, "200K and 500K");
            PORT_DIPSETTING(0x08, "300K only");
            PORT_DIPSETTING(0x0c, "None");
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x30, "1");
            PORT_DIPSETTING(0x20, "2");
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x10, "5");
            PORT_BITX(0x40, 0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", 0, 0);
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* Territory Jumper Block */
            PORT_DIPNAME(0x07, 0x02, "Territory");
            PORT_DIPSETTING(0x02, "Europe");
            PORT_DIPSETTING(0x01, "US");
            PORT_DIPSETTING(0x00, "Japan");
            PORT_DIPSETTING(0x03, "Hong Kong");
            PORT_DIPSETTING(0x04, "Korea");
            PORT_DIPSETTING(0x05, "Taiwan");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_vimana = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* VBlank */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_VBLANK);
            PORT_BIT(0xfe, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW A */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_SERVICE(0x04, IP_ACTIVE_HIGH);
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x30, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_6C"));

            PORT_START();
            /* DSW B */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x01, "Easy");
            PORT_DIPSETTING(0x00, "Medium");
            PORT_DIPSETTING(0x02, "Hard");
            PORT_DIPSETTING(0x03, "Hardest");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "70K and 200K");
            PORT_DIPSETTING(0x04, "100K and 250K");
            PORT_DIPSETTING(0x08, "100K");
            PORT_DIPSETTING(0x0c, "200K");
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x30, "2");
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x20, "4");
            PORT_DIPSETTING(0x10, "5");
            PORT_BITX(0x40, 0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, "Allow Continue");
            PORT_DIPSETTING(0x80, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* Territory Jumper Block */
            PORT_DIPNAME(0x0f, 0x02, "Territory");
            PORT_DIPSETTING(0x02, "Europe");
            PORT_DIPSETTING(0x01, "US");
            PORT_DIPSETTING(0x00, "Japan");
            PORT_DIPSETTING(0x03, "Hong Kong");
            PORT_DIPSETTING(0x04, "Korea");
            PORT_DIPSETTING(0x05, "Taiwan");
            //	PORT_DIPSETTING(    0x06, "Taiwan" );
            //	PORT_DIPSETTING(    0x07, "US" );
            //	PORT_DIPSETTING(    0x08, "Hong Kong" );
            //	PORT_DIPSETTING(    0x09, DEF_STR( "Unused") );
            //	PORT_DIPSETTING(    0x0a, DEF_STR( "Unused") );
            //	PORT_DIPSETTING(    0x0b, DEF_STR( "Unused") );
            //	PORT_DIPSETTING(    0x0c, DEF_STR( "Unused") );
            //	PORT_DIPSETTING(    0x0d, DEF_STR( "Unused") );
            //	PORT_DIPSETTING(    0x0e, DEF_STR( "Unused") );
            //	PORT_DIPSETTING(    0x0f, "Japan" );
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_vimanan = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* VBlank */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_VBLANK);
            PORT_BIT(0xfe, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW A */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_SERVICE(0x04, IP_ACTIVE_HIGH);
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));
            /* settings for other territories (non Nova license)
		PORT_DIPNAME( 0x30, 0x00, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x30, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0xc0, 0x00, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_6C") );
             */

            PORT_START();
            /* DSW B */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x01, "Easy");
            PORT_DIPSETTING(0x00, "Medium");
            PORT_DIPSETTING(0x02, "Hard");
            PORT_DIPSETTING(0x03, "Hardest");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "70K and 200K");
            PORT_DIPSETTING(0x04, "100K and 250K");
            PORT_DIPSETTING(0x08, "100K");
            PORT_DIPSETTING(0x0c, "200K");
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x30, "2");
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x20, "4");
            PORT_DIPSETTING(0x10, "5");
            PORT_BITX(0x40, 0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, "Allow Continue");
            PORT_DIPSETTING(0x80, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* Territory Jumper Block */
            PORT_DIPNAME(0x0f, 0x02, "Territory");
            PORT_DIPSETTING(0x02, "Europe");
            PORT_DIPSETTING(0x01, "US");
            PORT_DIPSETTING(0x00, "Japan");
            PORT_DIPSETTING(0x03, "Hong Kong");
            PORT_DIPSETTING(0x04, "Korea");
            PORT_DIPSETTING(0x05, "Taiwan");
            //	PORT_DIPSETTING(    0x06, "Taiwan" );
            //	PORT_DIPSETTING(    0x07, "US" );
            //	PORT_DIPSETTING(    0x08, "Hong Kong" );
            //	PORT_DIPSETTING(    0x09, DEF_STR( "Unused") );
            //	PORT_DIPSETTING(    0x0a, DEF_STR( "Unused") );
            //	PORT_DIPSETTING(    0x0b, DEF_STR( "Unused") );
            //	PORT_DIPSETTING(    0x0c, DEF_STR( "Unused") );
            //	PORT_DIPSETTING(    0x0d, DEF_STR( "Unused") );
            //	PORT_DIPSETTING(    0x0e, DEF_STR( "Unused") );
            //	PORT_DIPSETTING(    0x0f, "Japan" );
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout tilelayout = new GfxLayout(
            8, 8, /* 8x8 */
            16384, /* 16384 tiles */
            4, /* 4 bits per pixel */
            new int[]{3 * 8 * 0x20000, 2 * 8 * 0x20000, 1 * 8 * 0x20000, 0 * 8 * 0x20000},
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0, 0x08, 0x10, 0x18, 0x20, 0x28, 0x30, 0x38},
            64
    );

    static GfxLayout rallybik_spr_layout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            2048, /* 2048 sprites */
            4, /* 4 bits per pixel */
            new int[]{0 * 2048 * 32 * 8, 1 * 2048 * 32 * 8, 2 * 2048 * 32 * 8, 3 * 2048 * 32 * 8},
            new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                8 * 16, 9 * 16, 10 * 16, 11 * 16, 12 * 16, 13 * 16, 14 * 16, 15 * 16},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );

    static GfxLayout vm_tilelayout = new GfxLayout(
            8, 8, /* 8x8 */
            32768, /* 32768 tiles */
            4, /* 4 bits per pixel */
            new int[]{8 * 0x80000 + 8, 8 * 0x80000, 8, 0},
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70},
            128
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x00000, tilelayout, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0x00000, tilelayout, 64 * 16, 64),
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo rallybik_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x00000, tilelayout, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0x00000, rallybik_spr_layout, 64 * 16, 64),
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo outzone_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x00000, vm_tilelayout, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0x00000, tilelayout, 64 * 16, 64),
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo vm_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x00000, tilelayout, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0x00000, vm_tilelayout, 64 * 16, 64),
                new GfxDecodeInfo(-1) /* end of array */};

    public static WriteYmHandlerPtr irqhandler = new WriteYmHandlerPtr() {
        public void handler(int linestate) {
            cpu_set_irq_line(1, 0, linestate);
        }
    };

    static YM3812interface ym3812_interface = new YM3812interface(
            1,
            28000000 / 8, /* 3.5Mhz (28Mhz Oscillator) */
            new int[]{255},
            new WriteYmHandlerPtr[]{irqhandler}
    );

    static MachineDriver machine_driver_rallybik = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000,
                        rallybik_readmem, rallybik_writemem, null, null,
                        toaplan1_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        28000000 / 8, /* 3.5Mhz (28Mhz Oscillator) */
                        sound_readmem, sound_writemem, truxton_sound_readport, rallybik_sound_writeport,
                        ignore_interrupt, 0
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            10,
            toaplan1_init_machine,
            /* video hardware */
            320, 240, new rectangle(0, 319, 0, 239),
            rallybik_gfxdecodeinfo,
            64 * 16 + 64 * 16, 64 * 16 + 64 * 16,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK | VIDEO_BUFFERS_SPRITERAM,
            rallybik_eof_callback,
            rallybik_vh_start,
            rallybik_vh_stop,
            rallybik_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
                ),}
    );

    static MachineDriver machine_driver_truxton = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000,
                        truxton_readmem, truxton_writemem, null, null,
                        toaplan1_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        28000000 / 8, /* 3.5Mhz (28Mhz Oscillator) */
                        sound_readmem, sound_writemem, truxton_sound_readport, truxton_sound_writeport,
                        ignore_interrupt, 0
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            10,
            toaplan1_init_machine,
            /* video hardware */
            320, 240, new rectangle(0, 319, 0, 239),
            gfxdecodeinfo,
            64 * 16 + 64 * 16, 64 * 16 + 64 * 16,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
            toaplan1_eof_callback,
            toaplan1_vh_start,
            toaplan1_vh_stop,
            toaplan1_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
                ),}
    );

    static MachineDriver machine_driver_hellfire = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000,
                        hellfire_readmem, hellfire_writemem, null, null,
                        toaplan1_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        28000000 / 8, /* 3.5Mhz (28Mhz Oscillator) */
                        sound_readmem, sound_writemem, hellfire_sound_readport, hellfire_sound_writeport,
                        ignore_interrupt, 0
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            10,
            toaplan1_init_machine,
            /* video hardware */
            320, 256, new rectangle(0, 319, 16, 255),
            gfxdecodeinfo,
            64 * 16 + 64 * 16, 64 * 16 + 64 * 16,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
            toaplan1_eof_callback,
            toaplan1_vh_start,
            toaplan1_vh_stop,
            toaplan1_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
                ),}
    );

    static MachineDriver machine_driver_zerowing = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000,
                        zerowing_readmem, zerowing_writemem, null, null,
                        toaplan1_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        28000000 / 8, /* 3.5Mhz (28Mhz Oscillator) */
                        sound_readmem, sound_writemem, zerowing_sound_readport, zerowing_sound_writeport,
                        ignore_interrupt, 0
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            10,
            toaplan1_init_machine,
            /* video hardware */
            320, 256, new rectangle(0, 319, 16, 255),
            gfxdecodeinfo,
            64 * 16 + 64 * 16, 64 * 16 + 64 * 16,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
            toaplan1_eof_callback,
            toaplan1_vh_start,
            toaplan1_vh_stop,
            toaplan1_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
                ),}
    );

    static MachineDriver machine_driver_demonwld = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000,
                        demonwld_readmem, demonwld_writemem, null, null,
                        toaplan1_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        28000000 / 8, /* 3.5Mhz (28Mhz Oscillator) */
                        sound_readmem, sound_writemem, demonwld_sound_readport, demonwld_sound_writeport,
                        ignore_interrupt, 0
                ),
                new MachineCPU(
                        CPU_TMS320C10,
                        28000000 / 8, /* 3.5 MHz */
                        DSP_readmem, DSP_writemem, DSP_readport, DSP_writeport,
                        ignore_interrupt, 0 /* IRQs are caused by 68000 */
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            10,
            toaplan1_init_machine,
            /* video hardware */
            320, 256, new rectangle(0, 319, 16, 255),
            gfxdecodeinfo,
            64 * 16 + 64 * 16, 64 * 16 + 64 * 16,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
            toaplan1_eof_callback,
            toaplan1_vh_start,
            toaplan1_vh_stop,
            toaplan1_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
                ),}
    );

    static MachineDriver machine_driver_samesame = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000,
                        samesame_readmem, samesame_writemem, null, null,
                        toaplan1_interrupt, 1
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1,
            toaplan1_init_machine,
            /* video hardware */
            320, 240, new rectangle(0, 319, 0, 239),
            gfxdecodeinfo,
            64 * 16 + 64 * 16, 64 * 16 + 64 * 16,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
            samesame_eof_callback,
            toaplan1_vh_start,
            toaplan1_vh_stop,
            toaplan1_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
                ),}
    );

    static MachineDriver machine_driver_outzone = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000,
                        outzone_readmem, outzone_writemem, null, null,
                        toaplan1_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        28000000 / 8, /* 3.5Mhz (28Mhz Oscillator) */
                        sound_readmem, sound_writemem, outzone_sound_readport, outzone_sound_writeport,
                        ignore_interrupt, 0
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            10,
            toaplan1_init_machine,
            /* video hardware */
            320, 240, new rectangle(0, 319, 0, 239),
            outzone_gfxdecodeinfo,
            64 * 16 + 64 * 16, 64 * 16 + 64 * 16,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
            toaplan1_eof_callback,
            toaplan1_vh_start,
            toaplan1_vh_stop,
            toaplan1_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
                ),}
    );

    static MachineDriver machine_driver_vimana = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000,
                        vimana_readmem, vimana_writemem, null, null,
                        toaplan1_interrupt, 1
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1,
            toaplan1_init_machine,
            /* video hardware */
            320, 240, new rectangle(0, 319, 0, 239),
            vm_gfxdecodeinfo,
            64 * 16 + 64 * 16, 64 * 16 + 64 * 16,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
            toaplan1_eof_callback,
            toaplan1_vh_start,
            toaplan1_vh_stop,
            toaplan1_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
                ),}
    );

    /**
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadPtr rom_rallybik = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x080000, REGION_CPU1);/* Main 68K code */
            ROM_LOAD_EVEN("b45-02.rom", 0x000000, 0x08000, 0x383386d7);
            ROM_LOAD_ODD("b45-01.rom", 0x000000, 0x08000, 0x7602f6a7);
            ROM_LOAD_EVEN("b45-04.rom", 0x040000, 0x20000, 0xe9b005b1);
            ROM_LOAD_ODD("b45-03.rom", 0x040000, 0x20000, 0x555344ce);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound Z80 code */
            ROM_LOAD("b45-05.rom", 0x0000, 0x4000, 0x10814601);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("b45-09.bin", 0x00000, 0x20000, 0x1dc7b010);
            ROM_LOAD("b45-08.bin", 0x20000, 0x20000, 0xfab661ba);
            ROM_LOAD("b45-07.bin", 0x40000, 0x20000, 0xcd3748b4);
            ROM_LOAD("b45-06.bin", 0x60000, 0x20000, 0x144b085c);

            ROM_REGION(0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("b45-11.rom", 0x00000, 0x10000, 0x0d56e8bb);
            ROM_LOAD("b45-10.rom", 0x10000, 0x10000, 0xdbb7c57e);
            ROM_LOAD("b45-12.rom", 0x20000, 0x10000, 0xcf5aae4e);
            ROM_LOAD("b45-13.rom", 0x30000, 0x10000, 0x1683b07c);

            ROM_REGION(0x240, REGION_PROMS);
            /* nibble bproms, lo/hi order to be determined */
            ROM_LOAD("b45-15.bpr", 0x000, 0x100, 0x24e7d62f);/* sprite priority control ?? */
            ROM_LOAD("b45-16.bpr", 0x100, 0x100, 0xa50cef09);/* sprite priority control ?? */
            ROM_LOAD("b45-14.bpr", 0x200, 0x020, 0xf72482db);/* sprite control ?? */
            ROM_LOAD("b45-17.bpr", 0x220, 0x020, 0xbc88cced);/* sprite attribute (flip/position) ?? */
            ROM_END();
        }
    };

    static RomLoadPtr rom_truxton = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x040000, REGION_CPU1);/* Main 68K code */
            ROM_LOAD_EVEN("b65_11.bin", 0x000000, 0x20000, 0x1a62379a);
            ROM_LOAD_ODD("b65_10.bin", 0x000000, 0x20000, 0xaff5195d);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound Z80 code */
            ROM_LOAD("b65_09.bin", 0x0000, 0x8000, 0xf1c0f410);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("b65_08.bin", 0x00000, 0x20000, 0xd2315b37);
            ROM_LOAD("b65_07.bin", 0x20000, 0x20000, 0xfb83252a);
            ROM_LOAD("b65_06.bin", 0x40000, 0x20000, 0x36cedcbe);
            ROM_LOAD("b65_05.bin", 0x60000, 0x20000, 0x81cd95f1);

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("b65_04.bin", 0x00000, 0x20000, 0x8c6ff461);
            ROM_LOAD("b65_03.bin", 0x20000, 0x20000, 0x58b1350b);
            ROM_LOAD("b65_02.bin", 0x40000, 0x20000, 0x1dd55161);
            ROM_LOAD("b65_01.bin", 0x60000, 0x20000, 0xe974937f);
            ROM_END();
        }
    };

    static RomLoadPtr rom_hellfire = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x040000, REGION_CPU1);/* Main 68K code */
            ROM_LOAD_EVEN("b90-14.bin", 0x000000, 0x20000, 0x101df9f5);
            ROM_LOAD_ODD("b90-15.bin", 0x000000, 0x20000, 0xe67fd452);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound Z80 code */
            ROM_LOAD("b90-03.bin", 0x0000, 0x8000, 0x4058fa67);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("b90-04.bin", 0x00000, 0x20000, 0xea6150fc);
            ROM_LOAD("b90-05.bin", 0x20000, 0x20000, 0xbb52c507);
            ROM_LOAD("b90-06.bin", 0x40000, 0x20000, 0xcf5b0252);
            ROM_LOAD("b90-07.bin", 0x60000, 0x20000, 0xb98af263);

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("b90-11.bin", 0x00000, 0x20000, 0xc33e543c);
            ROM_LOAD("b90-10.bin", 0x20000, 0x20000, 0x35fd1092);
            ROM_LOAD("b90-09.bin", 0x40000, 0x20000, 0xcf01009e);
            ROM_LOAD("b90-08.bin", 0x60000, 0x20000, 0x3404a5e3);
            ROM_END();
        }
    };

    static RomLoadPtr rom_zerowing = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x080000, REGION_CPU1);/* Main 68K code */
            ROM_LOAD_EVEN("o15-11.rom", 0x000000, 0x08000, 0x6ff2b9a0);
            ROM_LOAD_ODD("o15-12.rom", 0x000000, 0x08000, 0x9773e60b);
            ROM_LOAD_EVEN("o15-09.rom", 0x040000, 0x20000, 0x13764e95);
            ROM_LOAD_ODD("o15-10.rom", 0x040000, 0x20000, 0x351ba71a);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound Z80 code */
            ROM_LOAD("o15-13.rom", 0x0000, 0x8000, 0xe7b72383);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("o15-05.rom", 0x00000, 0x20000, 0x4e5dd246);
            ROM_LOAD("o15-06.rom", 0x20000, 0x20000, 0xc8c6d428);
            ROM_LOAD("o15-07.rom", 0x40000, 0x20000, 0xefc40e99);
            ROM_LOAD("o15-08.rom", 0x60000, 0x20000, 0x1b019eab);

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("o15-03.rom", 0x00000, 0x20000, 0x7f245fd3);
            ROM_LOAD("o15-04.rom", 0x20000, 0x20000, 0x0b1a1289);
            ROM_LOAD("o15-01.rom", 0x40000, 0x20000, 0x70570e43);
            ROM_LOAD("o15-02.rom", 0x60000, 0x20000, 0x724b487f);
            ROM_END();
        }
    };

    static RomLoadPtr rom_demonwld = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x040000, REGION_CPU1);/* Main 68K code */
            ROM_LOAD_EVEN("rom10", 0x000000, 0x20000, 0x036ee46c);
            ROM_LOAD_ODD("rom09", 0x000000, 0x20000, 0xbed746e3);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound Z80 code */
            ROM_LOAD("rom11", 0x0000, 0x8000, 0x397eca1b);

            ROM_REGION(0x10000, REGION_CPU3);/* Co-Processor TMS320C10 MCU code */
            ROM_LOAD_EVEN("dsp_22.bin", 0x0000, 0x0800, 0x79389a71);
            ROM_LOAD_ODD("dsp_21.bin", 0x0000, 0x0800, 0x2d135376);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("rom05", 0x00000, 0x20000, 0x6506c982);
            ROM_LOAD("rom07", 0x20000, 0x20000, 0xa3a0d993);
            ROM_LOAD("rom06", 0x40000, 0x20000, 0x4fc5e5f3);
            ROM_LOAD("rom08", 0x60000, 0x20000, 0xeb53ab09);

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("rom01", 0x00000, 0x20000, 0x1b3724e9);
            ROM_LOAD("rom02", 0x20000, 0x20000, 0x7b20a44d);
            ROM_LOAD("rom03", 0x40000, 0x20000, 0x2cacdcd0);
            ROM_LOAD("rom04", 0x60000, 0x20000, 0x76fd3201);

            ROM_REGION(0x40, REGION_PROMS);
            /* nibble bproms, lo/hi order to be determined */
            ROM_LOAD("prom12.bpr", 0x00, 0x20, 0xbc88cced);/* sprite attribute (flip/position) ?? */
            ROM_LOAD("prom13.bpr", 0x20, 0x20, 0xa1e17492);/* ??? */
            ROM_END();
        }
    };

    static RomLoadPtr rom_samesame = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x080000, REGION_CPU1);/* Main 68K code */
            ROM_LOAD_EVEN("o17_09.bin", 0x000000, 0x08000, 0x3f69e437);
            ROM_LOAD_ODD("o17_10.bin", 0x000000, 0x08000, 0x4e723e0a);
            ROM_LOAD_EVEN("o17_11.bin", 0x040000, 0x20000, 0xbe07d101);
            ROM_LOAD_ODD("o17_12.bin", 0x040000, 0x20000, 0xef698811);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound HD647180 code */
 /* sound CPU is a HD647180 (Z180) with internal ROM - not yet supported */
            ROM_LOAD("hd647180.017", 0x00000, 0x08000, 0x00000000);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("o17_05.bin", 0x00000, 0x20000, 0x565315f8);
            ROM_LOAD("o17_06.bin", 0x20000, 0x20000, 0x95262d4c);
            ROM_LOAD("o17_07.bin", 0x40000, 0x20000, 0x4c4b735c);
            ROM_LOAD("o17_08.bin", 0x60000, 0x20000, 0x95c6586c);

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("o17_01.bin", 0x00000, 0x20000, 0xea12e491);
            ROM_LOAD("o17_02.bin", 0x20000, 0x20000, 0x32a13a9f);
            ROM_LOAD("o17_03.bin", 0x40000, 0x20000, 0x68723dc9);
            ROM_LOAD("o17_04.bin", 0x60000, 0x20000, 0xfe0ecb13);
            ROM_END();
        }
    };

    static RomLoadPtr rom_fireshrk = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x080000, REGION_CPU1);/* Main 68K code */
            ROM_LOAD_EVEN("9.bin", 0x000000, 0x08000, 0xf0c70e6f);
            ROM_LOAD_ODD("10.bin", 0x000000, 0x08000, 0x9d253d77);
            ROM_LOAD_EVEN("11.bin", 0x040000, 0x20000, 0x6beac378);
            ROM_LOAD_ODD("12.bin", 0x040000, 0x20000, 0x6adb6eb5);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound HD647180 code */
 /* sound CPU is a HD647180 (Z180) with internal ROM - not yet supported */
            ROM_LOAD("hd647180.017", 0x00000, 0x08000, 0x00000000);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("o17_05.bin", 0x00000, 0x20000, 0x565315f8);
            ROM_LOAD("o17_06.bin", 0x20000, 0x20000, 0x95262d4c);
            ROM_LOAD("o17_07.bin", 0x40000, 0x20000, 0x4c4b735c);
            ROM_LOAD("o17_08.bin", 0x60000, 0x20000, 0x95c6586c);

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("o17_01.bin", 0x00000, 0x20000, 0xea12e491);
            ROM_LOAD("o17_02.bin", 0x20000, 0x20000, 0x32a13a9f);
            ROM_LOAD("o17_03.bin", 0x40000, 0x20000, 0x68723dc9);
            ROM_LOAD("o17_04.bin", 0x60000, 0x20000, 0xfe0ecb13);
            ROM_END();
        }
    };

    static RomLoadPtr rom_outzone = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x040000, REGION_CPU1);/* Main 68K code */
            ROM_LOAD_EVEN("rom7.bin", 0x000000, 0x20000, 0x936e25d8);
            ROM_LOAD_ODD("rom8.bin", 0x000000, 0x20000, 0xd19b3ecf);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound Z80 code */
            ROM_LOAD("rom9.bin", 0x0000, 0x8000, 0x73d8e235);

            ROM_REGION(0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("rom5.bin", 0x00000, 0x80000, 0xc64ec7b6);
            ROM_LOAD("rom6.bin", 0x80000, 0x80000, 0x64b6c5ac);

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("rom2.bin", 0x00000, 0x20000, 0x6bb72d16);
            ROM_LOAD("rom1.bin", 0x20000, 0x20000, 0x0934782d);
            ROM_LOAD("rom3.bin", 0x40000, 0x20000, 0xec903c07);
            ROM_LOAD("rom4.bin", 0x60000, 0x20000, 0x50cbf1a8);
            ROM_END();
        }
    };

    static RomLoadPtr rom_outzonep = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x040000, REGION_CPU1);/* Main 68K code */
            ROM_LOAD_EVEN("18.bin", 0x000000, 0x20000, 0x31a171bb);
            ROM_LOAD_ODD("19.bin", 0x000000, 0x20000, 0x804ecfd1);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound Z80 code */
            ROM_LOAD("rom9.bin", 0x0000, 0x8000, 0x73d8e235);

            ROM_REGION(0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("rom5.bin", 0x00000, 0x80000, 0xc64ec7b6);
            ROM_LOAD("rom6.bin", 0x80000, 0x80000, 0x64b6c5ac);
            /* same data, different layout
		ROM_LOAD_GFX_EVEN( "04.bin",  0x000000, 0x10000, 0x3d11eae0 );
		ROM_LOAD_GFX_ODD ( "08.bin",  0x000000, 0x10000, 0xc7628891 );
		ROM_LOAD_GFX_EVEN( "13.bin",  0x080000, 0x10000, 0xb23dd87e );
		ROM_LOAD_GFX_ODD ( "09.bin",  0x080000, 0x10000, 0x445651ba );
		ROM_LOAD_GFX_EVEN( "03.bin",  0x020000, 0x10000, 0x6b347646 );
		ROM_LOAD_GFX_ODD ( "07.bin",  0x020000, 0x10000, 0x461b47f9 );
		ROM_LOAD_GFX_EVEN( "14.bin",  0x0a0000, 0x10000, 0xb28ae37a );
		ROM_LOAD_GFX_ODD ( "10.bin",  0x0a0000, 0x10000, 0x6596a076 );
		ROM_LOAD_GFX_EVEN( "02.bin",  0x040000, 0x10000, 0x11a781c3 );
		ROM_LOAD_GFX_ODD ( "06.bin",  0x040000, 0x10000, 0x1055da17 );
		ROM_LOAD_GFX_EVEN( "15.bin",  0x0c0000, 0x10000, 0x9c9e811b );
		ROM_LOAD_GFX_ODD ( "11.bin",  0x0c0000, 0x10000, 0x4c4d44dc );
		ROM_LOAD_GFX_EVEN( "01.bin",  0x060000, 0x10000, 0xe8c46aea );
		ROM_LOAD_GFX_ODD ( "05.bin",  0x060000, 0x10000, 0xf8a2fe01 );
		ROM_LOAD_GFX_EVEN( "16.bin",  0x0e0000, 0x10000, 0xcffcb99b );
		ROM_LOAD_GFX_ODD ( "12.bin",  0x0e0000, 0x10000, 0x90d37ded );
             */

            ROM_REGION(0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("rom2.bin", 0x00000, 0x20000, 0x6bb72d16);
            ROM_LOAD("rom1.bin", 0x20000, 0x20000, 0x0934782d);
            ROM_LOAD("rom3.bin", 0x40000, 0x20000, 0xec903c07);
            ROM_LOAD("rom4.bin", 0x60000, 0x20000, 0x50cbf1a8);
            ROM_END();
        }
    };

    static RomLoadPtr rom_vimana = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x040000, REGION_CPU1);/* Main 68K code */
            ROM_LOAD_EVEN("vim07.bin", 0x000000, 0x20000, 0x1efaea84);
            ROM_LOAD_ODD("vim08.bin", 0x000000, 0x20000, 0xe45b7def);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound HD647180 code */
 /* sound CPU is a HD647180 (Z180) with internal ROM - not yet supported */
            ROM_LOAD("hd647180.019", 0x00000, 0x08000, 0x00000000);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("vim6.bin", 0x00000, 0x20000, 0x2886878d);
            ROM_LOAD("vim5.bin", 0x20000, 0x20000, 0x61a63d7a);
            ROM_LOAD("vim4.bin", 0x40000, 0x20000, 0xb0515768);
            ROM_LOAD("vim3.bin", 0x60000, 0x20000, 0x0b539131);

            ROM_REGION(0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("vim1.bin", 0x00000, 0x80000, 0xcdde26cd);
            ROM_LOAD("vim2.bin", 0x80000, 0x80000, 0x1dbfc118);

            ROM_REGION(0x40, REGION_PROMS);
            /* nibble bproms, lo/hi order to be determined */
            ROM_LOAD("tp019-09.bpr", 0x00, 0x20, 0xbc88cced);/* sprite attribute (flip/position) ?? */
            ROM_LOAD("tp019-10.bpr", 0x20, 0x20, 0xa1e17492);/* ??? */
            ROM_END();
        }
    };

    static RomLoadPtr rom_vimana2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x040000, REGION_CPU1);/* Main 68K code */
            ROM_LOAD_EVEN("vimana07.bin", 0x000000, 0x20000, 0x5a4bf73e);
            ROM_LOAD_ODD("vimana08.bin", 0x000000, 0x20000, 0x03ba27e8);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound HD647180 code */
 /* sound CPU is a HD647180 (Z180) with internal ROM - not yet supported */
            ROM_LOAD("hd647180.019", 0x00000, 0x08000, 0x00000000);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("vim6.bin", 0x00000, 0x20000, 0x2886878d);
            ROM_LOAD("vim5.bin", 0x20000, 0x20000, 0x61a63d7a);
            ROM_LOAD("vim4.bin", 0x40000, 0x20000, 0xb0515768);
            ROM_LOAD("vim3.bin", 0x60000, 0x20000, 0x0b539131);

            ROM_REGION(0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("vim1.bin", 0x00000, 0x80000, 0xcdde26cd);
            ROM_LOAD("vim2.bin", 0x80000, 0x80000, 0x1dbfc118);

            ROM_REGION(0x40, REGION_PROMS);
            /* nibble bproms, lo/hi order to be determined */
            ROM_LOAD("tp019-09.bpr", 0x00, 0x20, 0xbc88cced);/* sprite attribute (flip/position) ?? */
            ROM_LOAD("tp019-10.bpr", 0x20, 0x20, 0xa1e17492);/* ??? */
            ROM_END();
        }
    };

    static RomLoadPtr rom_vimanan = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x040000, REGION_CPU1);/* Main 68K code */
            ROM_LOAD_EVEN("tp019-07.rom", 0x000000, 0x20000, 0x78888ff2);
            ROM_LOAD_ODD("tp019-08.rom", 0x000000, 0x20000, 0x6cd2dc3c);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound HD647180 code */
 /* sound CPU is a HD647180 (Z180) with internal ROM - not yet supported */
            ROM_LOAD("hd647180.019", 0x00000, 0x08000, 0x00000000);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("vim6.bin", 0x00000, 0x20000, 0x2886878d);
            ROM_LOAD("vim5.bin", 0x20000, 0x20000, 0x61a63d7a);
            ROM_LOAD("vim4.bin", 0x40000, 0x20000, 0xb0515768);
            ROM_LOAD("vim3.bin", 0x60000, 0x20000, 0x0b539131);

            ROM_REGION(0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("vim1.bin", 0x00000, 0x80000, 0xcdde26cd);
            ROM_LOAD("vim2.bin", 0x80000, 0x80000, 0x1dbfc118);

            ROM_REGION(0x40, REGION_PROMS);
            /* nibble bproms, lo/hi order to be determined */
            ROM_LOAD("tp019-09.bpr", 0x00, 0x20, 0xbc88cced);/* sprite attribute (flip/position) ?? */
            ROM_LOAD("tp019-10.bpr", 0x20, 0x20, 0xa1e17492);/* ??? */
            ROM_END();
        }
    };

    public static GameDriver driver_rallybik = new GameDriver("1988", "rallybik", "toaplan1.java", rom_rallybik, null, machine_driver_rallybik, input_ports_rallybik, null, ROT270, "[Toaplan] Taito Corporation", "Rally Bike / Dash Yarou");
    public static GameDriver driver_truxton = new GameDriver("1988", "truxton", "toaplan1.java", rom_truxton, null, machine_driver_truxton, input_ports_truxton, null, ROT270, "[Toaplan] Taito Corporation", "Truxton / Tatsujin");
    public static GameDriver driver_hellfire = new GameDriver("1989", "hellfire", "toaplan1.java", rom_hellfire, null, machine_driver_hellfire, input_ports_hellfire, null, ROT0, "Toaplan (Taito license)", "Hellfire");
    public static GameDriver driver_zerowing = new GameDriver("1989", "zerowing", "toaplan1.java", rom_zerowing, null, machine_driver_zerowing, input_ports_zerowing, null, ROT0, "Toaplan", "Zero Wing");
    public static GameDriver driver_demonwld = new GameDriver("1989", "demonwld", "toaplan1.java", rom_demonwld, null, machine_driver_demonwld, input_ports_demonwld, null, ROT0, "Toaplan (Taito license)", "Demon's World / Horror Story");
    public static GameDriver driver_fireshrk = new GameDriver("1990", "fireshrk", "toaplan1.java", rom_fireshrk, null, machine_driver_samesame, input_ports_fireshrk, null, ROT270, "Toaplan", "Fire Shark", GAME_NO_SOUND);
    public static GameDriver driver_samesame = new GameDriver("1989", "samesame", "toaplan1.java", rom_samesame, driver_fireshrk, machine_driver_samesame, input_ports_samesame, null, ROT270, "Toaplan", "Same! Same! Same!", GAME_NO_SOUND);
    public static GameDriver driver_outzone = new GameDriver("1990", "outzone", "toaplan1.java", rom_outzone, null, machine_driver_outzone, input_ports_outzone, null, ROT270, "Toaplan", "Out Zone");
    public static GameDriver driver_outzonep = new GameDriver("1990", "outzonep", "toaplan1.java", rom_outzonep, driver_outzone, machine_driver_outzone, input_ports_outzone, null, ROT270, "bootleg", "Out Zone (bootleg)");
    public static GameDriver driver_vimana = new GameDriver("1991", "vimana", "toaplan1.java", rom_vimana, null, machine_driver_vimana, input_ports_vimana, null, ROT270, "Toaplan", "Vimana (set 1)", GAME_NO_SOUND);
    public static GameDriver driver_vimana2 = new GameDriver("1991", "vimana2", "toaplan1.java", rom_vimana2, driver_vimana, machine_driver_vimana, input_ports_vimana, null, ROT270, "Toaplan", "Vimana (set 2)", GAME_NO_SOUND);
    public static GameDriver driver_vimanan = new GameDriver("1991", "vimanan", "toaplan1.java", rom_vimanan, driver_vimana, machine_driver_vimana, input_ports_vimanan, null, ROT270, "Toaplan (Nova Apparate GMBH  Co license)", "Vimana (Nova Apparate GMBH  Co)", GAME_NO_SOUND);
}
