/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.drivers;

import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.inputH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.pokeyH.*;
import static gr.codebb.arcadeflex.v036.sound.pokey.*;
import static gr.codebb.arcadeflex.v037b7.machine.tempest.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.avgdvg.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.vector.*;
import static gr.codebb.arcadeflex.v037b7.machine.atari_vg.*;
import static gr.codebb.arcadeflex.v037b7.machine.mathbox.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.inptportH.*;

public class tempest {


    public static ReadHandlerPtr tempest_IN0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int res;

            res = readinputport(0);

            if (avgdvg_done() != 0)
                res |= 0x40;

		/* Emulate the 3Khz source on bit 7 (divide 1.5MHz by 512) */
            if ((cpu_gettotalcycles() & 0x100) != 0)
                res |= 0x80;

            return res;
        }
    };

    public static WriteHandlerPtr tempest_led_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
/*TODO*///            set_led_status(0, ~data & 0x02);
/*TODO*///            set_led_status(1, ~data & 0x01);
		/* FLIP is bit 0x04 */
        }
    };
    static int lastval;
    public static WriteHandlerPtr tempest_coin_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            if (lastval == data) return;
            coin_counter_w.handler(0, (data & 0x01));
            coin_counter_w.handler(1, (data & 0x02));
            coin_counter_w.handler(2, (data & 0x04));
            lastval = data;
        }
    };

    static MemoryReadAddress readmem[] =
            {
                    new MemoryReadAddress(0x0000, 0x07ff, MRA_RAM),
                    new MemoryReadAddress(0x0c00, 0x0c00, tempest_IN0_r),	/* IN0 */
                    new MemoryReadAddress(0x0d00, 0x0d00, input_port_3_r),	/* DSW1 */
                    new MemoryReadAddress(0x0e00, 0x0e00, input_port_4_r),	/* DSW2 */
                    new MemoryReadAddress(0x2000, 0x2fff, MRA_RAM),
                    new MemoryReadAddress(0x3000, 0x3fff, MRA_ROM),
                    new MemoryReadAddress(0x6040, 0x6040, mb_status_r),
                    new MemoryReadAddress(0x6050, 0x6050, atari_vg_earom_r),
                    new MemoryReadAddress(0x6060, 0x6060, mb_lo_r),
                    new MemoryReadAddress(0x6070, 0x6070, mb_hi_r),
                    new MemoryReadAddress(0x60c0, 0x60cf, pokey1_r),
                    new MemoryReadAddress(0x60d0, 0x60df, pokey2_r),
                    new MemoryReadAddress(0x9000, 0xdfff, MRA_ROM),
                    new MemoryReadAddress(0xf000, 0xffff, MRA_ROM),	/* for the reset / interrupt vectors */
                    new MemoryReadAddress(-1)	/* end of table */
            };

    static MemoryWriteAddress writemem[] =
            {
                    new MemoryWriteAddress(0x0000, 0x07ff, MWA_RAM),
                    new MemoryWriteAddress(0x0800, 0x080f, tempest_colorram_w),
                    new MemoryWriteAddress(0x2000, 0x2fff, MWA_RAM, vectorram, vectorram_size),
                    new MemoryWriteAddress(0x3000, 0x3fff, MWA_ROM),
                    new MemoryWriteAddress(0x4000, 0x4000, tempest_coin_w),
                    new MemoryWriteAddress(0x4800, 0x4800, avgdvg_go_w),
                    new MemoryWriteAddress(0x5000, 0x5000, watchdog_reset_w),
                    new MemoryWriteAddress(0x5800, 0x5800, avgdvg_reset_w),
                    new MemoryWriteAddress(0x6000, 0x603f, atari_vg_earom_w),
                    new MemoryWriteAddress(0x6040, 0x6040, atari_vg_earom_ctrl_w),
                    new MemoryWriteAddress(0x6080, 0x609f, mb_go_w),
                    new MemoryWriteAddress(0x60c0, 0x60cf, pokey1_w),
                    new MemoryWriteAddress(0x60d0, 0x60df, pokey2_w),
                    new MemoryWriteAddress(0x60e0, 0x60e0, tempest_led_w),
                    new MemoryWriteAddress(0x9000, 0xdfff, MWA_ROM),
                    new MemoryWriteAddress(-1)	/* end of table */
            };

    static InputPortPtr input_ports_tempest = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_TILT);
            PORT_SERVICE(0x10, IP_ACTIVE_LOW);
            PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_SERVICE, "Diagnostic Step", KEYCODE_F1, IP_JOY_NONE);
		/* bit 6 is the VG HALT bit. We set it to "low" */
		/* per default (busy vector processor). */
	 	/* handled by tempest_IN0_r() */
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
		/* bit 7 is tied to a 3kHz (?) clock */
	 	/* handled by tempest_IN0_r() */
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START(); 	/* IN1/DSW0 */
		/* This is the Tempest spinner input. It only uses 4 bits. */
            PORT_ANALOG(0x0f, 0x00, IPT_DIAL, 25, 20, 0, 0);
		/* The next one is reponsible for cocktail mode.
		 * According to the documentation, this is not a switch, although
		 * it may have been planned to put it on the Math Box PCB, D/E2 )
		 */
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x10, DEF_STR("Cocktail"));
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN2 */
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x02, "Easy");
            PORT_DIPSETTING(0x03, "Medium1");
            PORT_DIPSETTING(0x00, "Medium2");
            PORT_DIPSETTING(0x01, "Hard");
            PORT_DIPNAME(0x04, 0x04, "Rating");
            PORT_DIPSETTING(0x04, "1, 3, 5, 7, 9");
            PORT_DIPSETTING(0x00, "tied to high score");
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* DSW1 - (N13 on analog vector generator PCB */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x02, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x0c, 0x00, "Right Coin");
            PORT_DIPSETTING(0x00, "*1");
            PORT_DIPSETTING(0x04, "*4");
            PORT_DIPSETTING(0x08, "*5");
            PORT_DIPSETTING(0x0c, "*6");
            PORT_DIPNAME(0x10, 0x00, "Left Coin");
            PORT_DIPSETTING(0x00, "*1");
            PORT_DIPSETTING(0x10, "*2");
            PORT_DIPNAME(0xe0, 0x00, "Bonus Coins");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPSETTING(0x80, "1 each 5");
            PORT_DIPSETTING(0x40, "1 each 4 (+Demo)");
            PORT_DIPSETTING(0xa0, "1 each 3");
            PORT_DIPSETTING(0x60, "2 each 4 (+Demo)");
            PORT_DIPSETTING(0x20, "1 each 2");
            PORT_DIPSETTING(0xc0, "Freeze Mode");
            PORT_DIPSETTING(0xe0, "Freeze Mode");

            PORT_START(); 	/* DSW2 - (L12 on analog vector generator PCB */
            PORT_DIPNAME(0x01, 0x00, "Minimum");
            PORT_DIPSETTING(0x00, "1 Credit");
            PORT_DIPSETTING(0x01, "2 Credit");
            PORT_DIPNAME(0x06, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x02, "French");
            PORT_DIPSETTING(0x04, "German");
            PORT_DIPSETTING(0x06, "Spanish");
            PORT_DIPNAME(0x38, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x08, "10000");
            PORT_DIPSETTING(0x00, "20000");
            PORT_DIPSETTING(0x10, "30000");
            PORT_DIPSETTING(0x18, "40000");
            PORT_DIPSETTING(0x20, "50000");
            PORT_DIPSETTING(0x28, "60000");
            PORT_DIPSETTING(0x30, "70000");
            PORT_DIPSETTING(0x38, "None");
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0xc0, "2");
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x40, "4");
            PORT_DIPSETTING(0x80, "5");
            INPUT_PORTS_END();
        }
    };


    public static ReadHandlerPtr input_port_1_bit_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (readinputport(1) & (1 << offset)) != 0 ? 0 : 228;
        }
    };
    public static ReadHandlerPtr input_port_2_bit_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (readinputport(2) & (1 << offset)) != 0 ? 0 : 228;
        }
    };

    static POKEYinterface pokey_interface = new POKEYinterface
            (
                    2,	/* 2 chips */
                    12096000 / 8,	/* 1.512 MHz */
                    new int[]{50, 50},
		/* The 8 pot handlers */
                    new ReadHandlerPtr[]{input_port_1_bit_r, input_port_2_bit_r},
                    new ReadHandlerPtr[]{input_port_1_bit_r, input_port_2_bit_r},
                    new ReadHandlerPtr[]{input_port_1_bit_r, input_port_2_bit_r},
                    new ReadHandlerPtr[]{input_port_1_bit_r, input_port_2_bit_r},
                    new ReadHandlerPtr[]{input_port_1_bit_r, input_port_2_bit_r},
                    new ReadHandlerPtr[]{input_port_1_bit_r, input_port_2_bit_r},
                    new ReadHandlerPtr[]{input_port_1_bit_r, input_port_2_bit_r},
                    new ReadHandlerPtr[]{input_port_1_bit_r, input_port_2_bit_r},
		/* The allpot handler */
                    new ReadHandlerPtr[]{null, null}
            );


    static MachineDriver machine_driver_tempest = new MachineDriver
            (
		/* basic machine hardware */
                    new MachineCPU[]{
                            new MachineCPU(
                                    CPU_M6502,
                                    12096000 / 8,	/* 1.512 MHz */
                                    readmem, writemem, null, null,
                                    interrupt, 4 /* 4.1ms */
                            )
                    },
                    60, 0,	/* frames per second, vblank duration (vector game, so no vblank) */
                    1,
                    null,
	
		/* video hardware */
                    300, 400, new rectangle(0, 550, 0, 580),
                    null,
                    256, 0,
                    avg_init_palette_multi,

                    VIDEO_TYPE_VECTOR | VIDEO_SUPPORTS_DIRTY,
                    null,
                    avg_start_tempest,
                    avg_stop,
                    vector_vh_screenrefresh,
	
		/* sound hardware */
                    0, 0, 0, 0,
                    new MachineSound[]{
                            new MachineSound(
                                    SOUND_POKEY,
                                    pokey_interface
                            )
                    },

                    atari_vg_earom_handler
            );


    /***************************************************************************
     * Game driver(s)
     ***************************************************************************/
	
	/*
	 * Tempest now uses the EAROM routines to load/save scores.
	 * Just in case, here is a snippet of the old code:
	 * if (memcmp(&RAM[0x0606],"\x07\x04\x01",3))
	 *	osd_fread(f,&RAM[0x0600],0x200);
	 */


    static RomLoadPtr rom_tempest = new RomLoadPtr() {
        public void handler() {  /* rev 3 */
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("136002.113", 0x9000, 0x0800, 0x65d61fe7);
            ROM_LOAD("136002.114", 0x9800, 0x0800, 0x11077375);
            ROM_LOAD("136002.115", 0xa000, 0x0800, 0xf3e2827a);
            ROM_LOAD("136002.316", 0xa800, 0x0800, 0xaeb0f7e9);
            ROM_LOAD("136002.217", 0xb000, 0x0800, 0xef2eb645);
            ROM_LOAD("136002.118", 0xb800, 0x0800, 0xbeb352ab);
            ROM_LOAD("136002.119", 0xc000, 0x0800, 0xa4de050f);
            ROM_LOAD("136002.120", 0xc800, 0x0800, 0x35619648);
            ROM_LOAD("136002.121", 0xd000, 0x0800, 0x73d38e47);
            ROM_LOAD("136002.222", 0xd800, 0x0800, 0x707bd5c3);
            ROM_RELOAD(0xf800, 0x0800);/* for reset/interrupt vectors */
		/* Mathbox ROMs */
            ROM_LOAD("136002.123", 0x3000, 0x0800, 0x29f7e937);
            ROM_LOAD("136002.124", 0x3800, 0x0800, 0xc16ec351);
            ROM_END();
        }
    };

    static RomLoadPtr rom_tempest1 = new RomLoadPtr() {
        public void handler() {  /* rev 1 */
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("136002.113", 0x9000, 0x0800, 0x65d61fe7);
            ROM_LOAD("136002.114", 0x9800, 0x0800, 0x11077375);
            ROM_LOAD("136002.115", 0xa000, 0x0800, 0xf3e2827a);
            ROM_LOAD("136002.116", 0xa800, 0x0800, 0x7356896c);
            ROM_LOAD("136002.117", 0xb000, 0x0800, 0x55952119);
            ROM_LOAD("136002.118", 0xb800, 0x0800, 0xbeb352ab);
            ROM_LOAD("136002.119", 0xc000, 0x0800, 0xa4de050f);
            ROM_LOAD("136002.120", 0xc800, 0x0800, 0x35619648);
            ROM_LOAD("136002.121", 0xd000, 0x0800, 0x73d38e47);
            ROM_LOAD("136002.122", 0xd800, 0x0800, 0x796a9918);
            ROM_RELOAD(0xf800, 0x0800);/* for reset/interrupt vectors */
		/* Mathbox ROMs */
            ROM_LOAD("136002.123", 0x3000, 0x0800, 0x29f7e937);
            ROM_LOAD("136002.124", 0x3800, 0x0800, 0xc16ec351);
            ROM_END();
        }
    };

    static RomLoadPtr rom_tempest2 = new RomLoadPtr() {
        public void handler() {  /* rev 2 */
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("136002.113", 0x9000, 0x0800, 0x65d61fe7);
            ROM_LOAD("136002.114", 0x9800, 0x0800, 0x11077375);
            ROM_LOAD("136002.115", 0xa000, 0x0800, 0xf3e2827a);
            ROM_LOAD("136002.116", 0xa800, 0x0800, 0x7356896c);
            ROM_LOAD("136002.217", 0xb000, 0x0800, 0xef2eb645);
            ROM_LOAD("136002.118", 0xb800, 0x0800, 0xbeb352ab);
            ROM_LOAD("136002.119", 0xc000, 0x0800, 0xa4de050f);
            ROM_LOAD("136002.120", 0xc800, 0x0800, 0x35619648);
            ROM_LOAD("136002.121", 0xd000, 0x0800, 0x73d38e47);
            ROM_LOAD("136002.222", 0xd800, 0x0800, 0x707bd5c3);
            ROM_RELOAD(0xf800, 0x0800);/* for reset/interrupt vectors */
		/* Mathbox ROMs */
            ROM_LOAD("136002.123", 0x3000, 0x0800, 0x29f7e937);
            ROM_LOAD("136002.124", 0x3800, 0x0800, 0xc16ec351);
            ROM_END();
        }
    };

    static RomLoadPtr rom_temptube = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("136002.113", 0x9000, 0x0800, 0x65d61fe7);
            ROM_LOAD("136002.114", 0x9800, 0x0800, 0x11077375);
            ROM_LOAD("136002.115", 0xa000, 0x0800, 0xf3e2827a);
            ROM_LOAD("136002.316", 0xa800, 0x0800, 0xaeb0f7e9);
            ROM_LOAD("136002.217", 0xb000, 0x0800, 0xef2eb645);
            ROM_LOAD("tube.118", 0xb800, 0x0800, 0xcefb03f0);
            ROM_LOAD("136002.119", 0xc000, 0x0800, 0xa4de050f);
            ROM_LOAD("136002.120", 0xc800, 0x0800, 0x35619648);
            ROM_LOAD("136002.121", 0xd000, 0x0800, 0x73d38e47);
            ROM_LOAD("136002.222", 0xd800, 0x0800, 0x707bd5c3);
            ROM_RELOAD(0xf800, 0x0800);/* for reset/interrupt vectors */
		/* Mathbox ROMs */
            ROM_LOAD("136002.123", 0x3000, 0x0800, 0x29f7e937);
            ROM_LOAD("136002.124", 0x3800, 0x0800, 0xc16ec351);
            ROM_END();
        }
    };


    public static GameDriver driver_tempest = new GameDriver("1980", "tempest", "tempest.java", rom_tempest, null, machine_driver_tempest, input_ports_tempest, null, ROT0, "Atari", "Tempest (rev 3)", GAME_NO_COCKTAIL);
    public static GameDriver driver_tempest1 = new GameDriver("1980", "tempest1", "tempest.java", rom_tempest1, driver_tempest, machine_driver_tempest, input_ports_tempest, null, ROT0, "Atari", "Tempest (rev 1)", GAME_NO_COCKTAIL);
    public static GameDriver driver_tempest2 = new GameDriver("1980", "tempest2", "tempest.java", rom_tempest2, driver_tempest, machine_driver_tempest, input_ports_tempest, null, ROT0, "Atari", "Tempest (rev 2)", GAME_NO_COCKTAIL);
    public static GameDriver driver_temptube = new GameDriver("1980", "temptube", "tempest.java", rom_temptube, driver_tempest, machine_driver_tempest, input_ports_tempest, null, ROT0, "hack", "Tempest Tubes", GAME_NO_COCKTAIL);
}
