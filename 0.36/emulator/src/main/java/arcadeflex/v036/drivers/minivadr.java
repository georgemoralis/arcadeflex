/**
 * ported to 0.36
 */
/**
 * Changelog
 * =========
 * 21/12/2022 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.drivers;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.drawgfxH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.vidhrdw.minivadr.*;

public class minivadr {

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
                new MemoryReadAddress(0xa000, 0xbfff, MRA_RAM),
                new MemoryReadAddress(0xe008, 0xe008, input_port_0_r),
                new MemoryReadAddress(-1) /* end of table */};
    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x1fff, MWA_ROM),
                new MemoryWriteAddress(0xa000, 0xbfff, minivadr_videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xe008, 0xe008, MWA_NOP), // ???
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_minivadr = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_minivadr = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        24000000 / 6, /* 4 Mhz ? */
                        readmem, writemem, null, null,
                        interrupt, 1
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            256, 256, new rectangle(0, 256 - 1, 16, 240 - 1),
            null,
            2, 0,
            minivadr_init_palette,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY,
            null,
            null,
            null,
            minivadr_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0, null
    );

    /**
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadHandlerPtr rom_minivadr = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("d26-01.bin", 0x0000, 0x2000, 0xa96c823d);
            ROM_END();
        }
    };

    public static GameDriver driver_minivadr = new GameDriver("1990", "minivadr", "minivadr.java", rom_minivadr, null, machine_driver_minivadr, input_ports_minivadr, null, ROT0, "Taito Corporation", "Minivader");
}
