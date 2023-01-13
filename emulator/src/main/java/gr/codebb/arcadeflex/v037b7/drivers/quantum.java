/*
  quantum

  Paul Forgey, 1997

  This code is donated to the MAME team, and inherits all copyrights
  and restrictions from MAME
 */
 /*
	QUANTUM MEMORY MAP (per schem):

	000000-003FFF	ROM0
	004000-004FFF	ROM1
	008000-00BFFF	ROM2
	00C000-00FFFF	ROM3
	010000-013FFF	ROM4

	018000-01BFFF	RAM0
	01C000-01CFFF	RAM1

	940000			TRACKBALL
	948000			SWITCHES
	950000			COLORRAM
	958000			CONTROL (LED and coin control)
	960000-970000	RECALL (nvram read)
	968000			VGRST (vector reset)
	970000			VGGO (vector go)
	978000			WDCLR (watchdog)
	900000			NVRAM (nvram write)
	840000			I/OS (sound and dip switches)
	800000-801FFF	VMEM (vector display list)
	940000			I/O (shematic label really - covered above)
	900000			DTACK1

 */
 /*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.drivers;
//generic imports

import static arcadeflex.v036.generic.funcPtr.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.sndintrfH.SOUND_POKEY;
import static gr.codebb.arcadeflex.v036.sound.pokeyH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.avgdvg.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.vector.*;
import static arcadeflex.v036.machine.foodf.*;
import static gr.codebb.arcadeflex.v037b7.machine.quantum.*;

public class quantum {

    static MemoryReadAddress quantum_read[]
            = {
                new MemoryReadAddress(0x000000, 0x013fff, MRA_ROM),
                new MemoryReadAddress(0x018000, 0x01cfff, MRA_BANK1),
                new MemoryReadAddress(0x800000, 0x801fff, MRA_BANK2),
                new MemoryReadAddress(0x840000, 0x84003f, quantum_snd_r),
                new MemoryReadAddress(0x900000, 0x9001ff, foodf_nvram_r),
                new MemoryReadAddress(0x940000, 0x940001, quantum_trackball_r), /* trackball */
                new MemoryReadAddress(0x948000, 0x948001, quantum_switches_r),
                new MemoryReadAddress(0x978000, 0x978001, MRA_NOP), /* ??? */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress quantum_write[]
            = {
                new MemoryWriteAddress(0x000000, 0x013fff, MWA_ROM),
                new MemoryWriteAddress(0x018000, 0x01cfff, MWA_BANK1),
                new MemoryWriteAddress(0x800000, 0x801fff, MWA_BANK2, vectorram, vectorram_size),
                new MemoryWriteAddress(0x840000, 0x84003f, quantum_snd_w),
                new MemoryWriteAddress(0x900000, 0x9001ff, foodf_nvram_w),
                new MemoryWriteAddress(0x950000, 0x95001f, quantum_colorram_w),
                new MemoryWriteAddress(0x958000, 0x958001, quantum_led_w),
                new MemoryWriteAddress(0x960000, 0x960001, MWA_NOP), /* enable NVRAM? */
                new MemoryWriteAddress(0x968000, 0x968001, avgdvg_reset_w),
                //	new MemoryWriteAddress( 0x970000, 0x970001, avgdvg_go_w ),
                //	new MemoryWriteAddress( 0x978000, 0x978001, watchdog_reset_w ),
                /* the following is wrong, but it's the only way I found to fix the service mode */
                new MemoryWriteAddress(0x978000, 0x978001, avgdvg_go_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_quantum = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
 /* YHALT here MUST BE ALWAYS 0  */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* vg YHALT */
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_SERVICE(0x80, IP_ACTIVE_LOW);

            /* first POKEY is SW2, second is SW1 -- more confusion! */
            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x80, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x40, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x30, 0x00, "Right Coin");
            PORT_DIPSETTING(0x00, "*1");
            PORT_DIPSETTING(0x20, "*4");
            PORT_DIPSETTING(0x10, "*5");
            PORT_DIPSETTING(0x30, "*6");
            PORT_DIPNAME(0x08, 0x00, "Left Coin");
            PORT_DIPSETTING(0x00, "*1");
            PORT_DIPSETTING(0x08, "*2");
            PORT_DIPNAME(0x07, 0x00, "Bonus Coins");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPSETTING(0x01, "1 each 5");
            PORT_DIPSETTING(0x02, "1 each 4");
            PORT_DIPSETTING(0x05, "1 each 3");
            PORT_DIPSETTING(0x06, "2 each 4");

            PORT_START();
            /* DSW1 */
            PORT_BIT(0xff, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN2 */
            PORT_ANALOG(0x0f, 0, IPT_TRACKBALL_Y | IPF_REVERSE, 10, 10, 0, 0);

            PORT_START();
            /* IN3 */
            PORT_ANALOG(0x0f, 0, IPT_TRACKBALL_X, 10, 10, 0, 0);
            INPUT_PORTS_END();
        }
    };

    static POKEYinterface pokey_interface = new POKEYinterface(
            2, /* 2 chips */
            600000, /* .6 MHz? (hand tuned) */
            new int[]{50, 50},
            /* The 8 pot handlers */
            new ReadHandlerPtr[]{quantum_input_1_r, quantum_input_2_r},
            new ReadHandlerPtr[]{quantum_input_1_r, quantum_input_2_r},
            new ReadHandlerPtr[]{quantum_input_1_r, quantum_input_2_r},
            new ReadHandlerPtr[]{quantum_input_1_r, quantum_input_2_r},
            new ReadHandlerPtr[]{quantum_input_1_r, quantum_input_2_r},
            new ReadHandlerPtr[]{quantum_input_1_r, quantum_input_2_r},
            new ReadHandlerPtr[]{quantum_input_1_r, quantum_input_2_r},
            new ReadHandlerPtr[]{quantum_input_1_r, quantum_input_2_r},
            /* The allpot handler */
            new ReadHandlerPtr[]{null, null}
    );

    static MachineDriver machine_driver_quantum = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        6000000, /* 6MHz */
                        quantum_read, quantum_write, null, null,
                        quantum_interrupt, 3 /* IRQ rate = 750kHz/4096 */
                )
            },
            60, 0, /* frames per second, vblank duration (vector game, so no vblank) */
            1,
            null,
            /* video hardware */
            300, 400, new rectangle(0, 600, 0, 900),
            null,
            256, 0,
            avg_init_palette_multi,
            VIDEO_TYPE_VECTOR | VIDEO_SUPPORTS_DIRTY,
            null,
            avg_start_quantum,
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
            foodf_nvram_handler
    );

    /**
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadHandlerPtr rom_quantum = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x014000, REGION_CPU1);
            ROM_LOAD_EVEN("136016.201", 0x000000, 0x002000, 0x7e7be63a);
            ROM_LOAD_ODD("136016.206", 0x000000, 0x002000, 0x2d8f5759);
            ROM_LOAD_EVEN("136016.102", 0x004000, 0x002000, 0x408d34f4);
            ROM_LOAD_ODD("136016.107", 0x004000, 0x002000, 0x63154484);
            ROM_LOAD_EVEN("136016.203", 0x008000, 0x002000, 0xbdc52fad);
            ROM_LOAD_ODD("136016.208", 0x008000, 0x002000, 0xdab4066b);
            ROM_LOAD_EVEN("136016.104", 0x00C000, 0x002000, 0xbf271e5c);
            ROM_LOAD_ODD("136016.109", 0x00C000, 0x002000, 0xd2894424);
            ROM_LOAD_EVEN("136016.105", 0x010000, 0x002000, 0x13ec512c);
            ROM_LOAD_ODD("136016.110", 0x010000, 0x002000, 0xacb50363);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_quantum1 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x014000, REGION_CPU1);
            ROM_LOAD_EVEN("136016.101", 0x000000, 0x002000, 0x5af0bd5b);
            ROM_LOAD_ODD("136016.106", 0x000000, 0x002000, 0xf9724666);
            ROM_LOAD_EVEN("136016.102", 0x004000, 0x002000, 0x408d34f4);
            ROM_LOAD_ODD("136016.107", 0x004000, 0x002000, 0x63154484);
            ROM_LOAD_EVEN("136016.103", 0x008000, 0x002000, 0x948f228b);
            ROM_LOAD_ODD("136016.108", 0x008000, 0x002000, 0xe4c48e4e);
            ROM_LOAD_EVEN("136016.104", 0x00C000, 0x002000, 0xbf271e5c);
            ROM_LOAD_ODD("136016.109", 0x00C000, 0x002000, 0xd2894424);
            ROM_LOAD_EVEN("136016.105", 0x010000, 0x002000, 0x13ec512c);
            ROM_LOAD_ODD("136016.110", 0x010000, 0x002000, 0xacb50363);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_quantump = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x014000, REGION_CPU1);
            ROM_LOAD_EVEN("quantump.2e", 0x000000, 0x002000, 0x176d73d3);
            ROM_LOAD_ODD("quantump.3e", 0x000000, 0x002000, 0x12fc631f);
            ROM_LOAD_EVEN("quantump.2f", 0x004000, 0x002000, 0xb64fab48);
            ROM_LOAD_ODD("quantump.3f", 0x004000, 0x002000, 0xa52a9433);
            ROM_LOAD_EVEN("quantump.2h", 0x008000, 0x002000, 0x5b29cba3);
            ROM_LOAD_ODD("quantump.3h", 0x008000, 0x002000, 0xc64fc03a);
            ROM_LOAD_EVEN("quantump.2k", 0x00C000, 0x002000, 0x854f9c09);
            ROM_LOAD_ODD("quantump.3k", 0x00C000, 0x002000, 0x1aac576c);
            ROM_LOAD_EVEN("quantump.2l", 0x010000, 0x002000, 0x1285b5e7);
            ROM_LOAD_ODD("quantump.3l", 0x010000, 0x002000, 0xe19de844);
            ROM_END();
        }
    };

    public static GameDriver driver_quantum = new GameDriver("1982", "quantum", "quantum.java", rom_quantum, null, machine_driver_quantum, input_ports_quantum, null, ROT0, "Atari", "Quantum (rev 2)", GAME_WRONG_COLORS);
    public static GameDriver driver_quantum1 = new GameDriver("1982", "quantum1", "quantum.java", rom_quantum1, driver_quantum, machine_driver_quantum, input_ports_quantum, null, ROT0, "Atari", "Quantum (rev 1)", GAME_WRONG_COLORS);
    public static GameDriver driver_quantump = new GameDriver("1982", "quantump", "quantum.java", rom_quantump, driver_quantum, machine_driver_quantum, input_ports_quantum, null, ROT0, "Atari", "Quantum (prototype)", GAME_WRONG_COLORS);

}
