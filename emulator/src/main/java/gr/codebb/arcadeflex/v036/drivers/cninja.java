/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package gr.codebb.arcadeflex.v036.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295H.*;
import static arcadeflex.v036.sound._2151intf.*;
import static arcadeflex.v036.sound._2151intfH.*;
import static arcadeflex.v036.cpu.z80.z80H.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.cninja.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.sound._2203intf.*;
import static arcadeflex.v036.sound._2203intfH.*;
import static gr.codebb.arcadeflex.v036.cpu.h6280.h6280.*;
import static gr.codebb.arcadeflex.v036.cpu.h6280.h6280H.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static gr.codebb.arcadeflex.v037b7.mame.memory.install_mem_write_handler;

public class cninja {

    static UBytePtr loopback = new UBytePtr(0x100);
    static UBytePtr cninja_ram = new UBytePtr();

    /**
     * *******************************************************************************
     */
    public static WriteHandlerPtr cninja_sound_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(0, data & 0xff);
            cpu_cause_interrupt(1, H6280_INT_IRQ1);
        }
    };

    public static WriteHandlerPtr stoneage_sound_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(0, data & 0xff);
            cpu_cause_interrupt(1, Z80_NMI_INT);
        }
    };

    public static WriteHandlerPtr cninja_loopback_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            //	WRITE_WORD(&loopback[offset],data);
            COMBINE_WORD_MEM(loopback, offset, data);
        }
    };

    public static ReadHandlerPtr cninja_prot_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            switch (offset) {
                case 0x80:
                    /* Master level control */
                    return loopback.READ_WORD(0);

                case 0xde:
                    /* Restart position control */
                    return loopback.READ_WORD(2);

                case 0xe6:
                    /* The number of credits in the system. */
                    return loopback.READ_WORD(4);

                case 0x86:
                    /* End of game check.  See 0x1814 */
                    return loopback.READ_WORD(6);

                /* Video registers */
                case 0x5a:
                    /* Moved to 0x140000 on int */
                    return loopback.READ_WORD(0x10);
                case 0x84:
                    /* Moved to 0x14000a on int */
                    return loopback.READ_WORD(0x12);
                case 0x20:
                    /* Moved to 0x14000c on int */
                    return loopback.READ_WORD(0x14);
                case 0x72:
                    /* Moved to 0x14000e on int */
                    return loopback.READ_WORD(0x16);
                case 0xdc:
                    /* Moved to 0x150000 on int */
                    return loopback.READ_WORD(0x18);
                case 0x6e:
                    /* Moved to 0x15000a on int */
                    return loopback.READ_WORD(0x1a);
                /* Not used on bootleg */
                case 0x6c:
                    /* Moved to 0x15000c on int */
                    return loopback.READ_WORD(0x1c);
                case 0x08:
                    /* Moved to 0x15000e on int */
                    return loopback.READ_WORD(0x1e);

                case 0x36:
                    /* Dip switches */
                    return (readinputport(3) + (readinputport(4) << 8));

                case 0x1c8:
                    /* Coins */
                    return readinputport(2);

                case 0x22c:
                    /* Player 1 & 2 input ports */
                    return (readinputport(0) + (readinputport(1) << 8));
            }
            if (errorlog != null) {
                fprintf(errorlog, "Protection PC %06x: warning - read unmapped memory address %04x\n", cpu_get_pc(), offset);
            }
            return 0;
        }
    };

    public static ReadHandlerPtr edrandy_prot_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            switch (offset) {
                /* Video registers */
                case 0x32a:
                    /* Moved to 0x140006 on int */
                    return loopback.READ_WORD(0x80);
                case 0x380:
                    /* Moved to 0x140008 on int */
                    return loopback.READ_WORD(0x84);
                case 0x63a:
                    /* Moved to 0x150002 on int */
                    return loopback.READ_WORD(0x88);
                case 0x42a:
                    /* Moved to 0x150004 on int */
                    return loopback.READ_WORD(0x8c);
                case 0x030:
                    /* Moved to 0x150006 on int */
                    return loopback.READ_WORD(0x90);
                case 0x6b2:
                    /* Moved to 0x150008 on int */
                    return loopback.READ_WORD(0x94);

                case 0x6c4:
                /* dma enable, bit 7 set, below bit 5 */
                case 0x33e:
                    return loopback.READ_WORD(0x2c);
                /* allows video registers */


 /* memcpy selectors, transfer occurs in interrupt */
                case 0x32e:
                    return loopback.READ_WORD(0x8);
                /* src msb */
                case 0x6d8:
                    return loopback.READ_WORD(0xa);
                /* src lsb */
                case 0x010:
                    return loopback.READ_WORD(0xc);
                /* dst msb */
                case 0x07a:
                    return loopback.READ_WORD(0xe);
                /* src lsb */

                case 0x37c:
                    return loopback.READ_WORD(0x10);
                /* src msb */
                case 0x250:
                    return loopback.READ_WORD(0x12);
                case 0x04e:
                    return loopback.READ_WORD(0x14);
                case 0x5ba:
                    return loopback.READ_WORD(0x16);
                case 0x5f4:
                    return loopback.READ_WORD(0x18);
                /* length */

                case 0x38c:
                    return loopback.READ_WORD(0x1a);
                /* src msb */
                case 0x02c:
                    return loopback.READ_WORD(0x1c);
                case 0x1e6:
                    return loopback.READ_WORD(0x1e);
                case 0x3e4:
                    return loopback.READ_WORD(0x20);
                case 0x174:
                    return loopback.READ_WORD(0x22);
                /* length */

 /* Player 1 & 2 controls, read in IRQ then written *back* to protection device */
                case 0x50:
                    /* written to 9e byte */
                    return readinputport(0);
                case 0x6f8:
                    /* written to 76 byte */
                    return readinputport(1);
                /* Controls are *really* read here! */
                case 0x6fa:
                    return (loopback.READ_WORD(0x9e) & 0xff00) | ((loopback.READ_WORD(0x76) >> 8) & 0xff);
                /* These two go to the low bytes of 9e and 76.. */
                case 0xc6:
                    return 0;
                case 0x7bc:
                    return 0;
                case 0x5c:
                    /* After coin insert, high 0x8000 bit set starts game */
                    return loopback.READ_WORD(0x76);
                case 0x3a6:
                    /* Top byte OR'd with above, masked to 7 */
                    return loopback.READ_WORD(0x9e);

                //		case 0xac: /* Dip switches */
                case 0xc2:
                    /* Dip switches */
                    return (readinputport(3) + (readinputport(4) << 8));
                case 0x5d4:
                    /* The state of the dips _last_ frame */
                    return loopback.READ_WORD(0x34);

                case 0x76a:
                    /* Coins */
                    return readinputport(2);

                case 0x156:
                    /* Interrupt regulate */

                    if (errorlog != null) {
                        fprintf(errorlog, "Int stop %04x\n", loopback.READ_WORD(0x1a));
                    }

                    cpu_spinuntil_int();
                    //return readinputport(2);

                    /* 4058 or 4056? */
                    return loopback.READ_WORD(0x36) >> 8;

            }

            //	if (errorlog) fprintf(errorlog,"Protection PC %06x: warning - read unmapped memory address %04x\n",cpu_get_pc(),offset);
            return 0;
        }
    };

    /**
     * *******************************************************************************
     */
    static MemoryReadAddress cninja_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x0bffff, MRA_ROM),
                new MemoryReadAddress(0x144000, 0x144fff, cninja_pf1_data_r),
                new MemoryReadAddress(0x15c000, 0x15c7ff, cninja_pf3_rowscroll_r),
                new MemoryReadAddress(0x184000, 0x187fff, MRA_BANK1),
                new MemoryReadAddress(0x190004, 0x190005, MRA_NOP), /* Seemingly unused */
                new MemoryReadAddress(0x19c000, 0x19dfff, paletteram_word_r),
                new MemoryReadAddress(0x1a4000, 0x1a47ff, MRA_BANK2), /* Sprites */
                new MemoryReadAddress(0x1bc000, 0x1bcfff, cninja_prot_r), /* Protection device */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress cninja_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x0bffff, MWA_ROM),
                new MemoryWriteAddress(0x140000, 0x14000f, cninja_control_1_w),
                new MemoryWriteAddress(0x144000, 0x144fff, cninja_pf1_data_w, cninja_pf1_data),
                new MemoryWriteAddress(0x146000, 0x146fff, cninja_pf4_data_w, cninja_pf4_data),
                new MemoryWriteAddress(0x14c000, 0x14c7ff, cninja_pf1_rowscroll_w, cninja_pf1_rowscroll),
                new MemoryWriteAddress(0x14e000, 0x14e7ff, cninja_pf4_rowscroll_w, cninja_pf4_rowscroll),
                new MemoryWriteAddress(0x150000, 0x15000f, cninja_control_0_w),
                new MemoryWriteAddress(0x154000, 0x154fff, cninja_pf3_data_w, cninja_pf3_data),
                new MemoryWriteAddress(0x156000, 0x156fff, cninja_pf2_data_w, cninja_pf2_data),
                new MemoryWriteAddress(0x15c000, 0x15c7ff, cninja_pf3_rowscroll_w, cninja_pf3_rowscroll),
                new MemoryWriteAddress(0x15e000, 0x15e7ff, cninja_pf2_rowscroll_w, cninja_pf2_rowscroll),
                new MemoryWriteAddress(0x184000, 0x187fff, MWA_BANK1, cninja_ram), /* Main ram */
                new MemoryWriteAddress(0x190000, 0x190007, MWA_NOP), /* IRQ Ack + DMA flags? */
                new MemoryWriteAddress(0x19c000, 0x19dfff, cninja_palette_24bit_w, paletteram),
                new MemoryWriteAddress(0x1a4000, 0x1a47ff, MWA_BANK2, spriteram),
                new MemoryWriteAddress(0x1b4000, 0x1b4001, cninja_update_sprites), /* DMA flag */
                new MemoryWriteAddress(0x1bc000, 0x1bc0ff, cninja_loopback_w), /* Protection writes */
                new MemoryWriteAddress(0x308000, 0x308fff, MWA_NOP), /* Bootleg only */
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress edrandy_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x0fffff, MRA_ROM),
                new MemoryReadAddress(0x144000, 0x144fff, cninja_pf1_data_r),
                new MemoryReadAddress(0x15c000, 0x15c7ff, cninja_pf3_rowscroll_r),
                new MemoryReadAddress(0x188000, 0x189fff, paletteram_word_r),
                new MemoryReadAddress(0x194000, 0x197fff, MRA_BANK1),
                new MemoryReadAddress(0x198000, 0x198fff, edrandy_prot_r), /* Protection device */
                new MemoryReadAddress(0x1bc000, 0x1bc7ff, MRA_BANK2), /* Sprites */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress edrandy_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x0fffff, MWA_ROM),
                new MemoryWriteAddress(0x140000, 0x14000f, cninja_control_1_w),
                new MemoryWriteAddress(0x144000, 0x144fff, cninja_pf1_data_w, cninja_pf1_data),
                new MemoryWriteAddress(0x146000, 0x146fff, cninja_pf4_data_w, cninja_pf4_data),
                new MemoryWriteAddress(0x14c000, 0x14c7ff, cninja_pf1_rowscroll_w, cninja_pf1_rowscroll),
                new MemoryWriteAddress(0x14e000, 0x14e7ff, cninja_pf4_rowscroll_w, cninja_pf4_rowscroll),
                new MemoryWriteAddress(0x150000, 0x15000f, cninja_control_0_w),
                new MemoryWriteAddress(0x154000, 0x154fff, cninja_pf3_data_w, cninja_pf3_data),
                new MemoryWriteAddress(0x156000, 0x156fff, cninja_pf2_data_w, cninja_pf2_data),
                new MemoryWriteAddress(0x15c000, 0x15c7ff, cninja_pf3_rowscroll_w, cninja_pf3_rowscroll),
                new MemoryWriteAddress(0x15e000, 0x15e7ff, cninja_pf2_rowscroll_w, cninja_pf2_rowscroll),
                new MemoryWriteAddress(0x188000, 0x189fff, cninja_palette_24bit_w, paletteram),
                new MemoryWriteAddress(0x194000, 0x197fff, MWA_BANK1, cninja_ram), /* Main ram */
                new MemoryWriteAddress(0x198064, 0x198065, cninja_sound_w), /* Soundlatch is amongst protection */
                new MemoryWriteAddress(0x198000, 0x1980ff, cninja_loopback_w), /* Protection writes */
                new MemoryWriteAddress(0x1a4000, 0x1a4007, MWA_NOP), /* IRQ Ack + DMA flags? */
                new MemoryWriteAddress(0x1ac000, 0x1ac001, cninja_update_sprites), /* DMA flag */
                new MemoryWriteAddress(0x1bc000, 0x1bc7ff, MWA_BANK2, spriteram),
                new MemoryWriteAddress(-1) /* end of table */};

    /**
     * ***************************************************************************
     */
    public static WriteHandlerPtr YM2151_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            switch (offset) {
                case 0:
                    YM2151_register_port_0_w.handler(0, data);
                    break;
                case 1:
                    YM2151_data_port_0_w.handler(0, data);
                    break;
            }
        }
    };

    public static WriteHandlerPtr YM2203_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            switch (offset) {
                case 0:
                    YM2203_control_port_0_w.handler(0, data);
                    break;
                case 1:
                    YM2203_write_port_0_w.handler(0, data);
                    break;
            }
        }
    };

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x00ffff, MRA_ROM),
                new MemoryReadAddress(0x100000, 0x100001, YM2203_status_port_0_r),
                new MemoryReadAddress(0x110000, 0x110001, YM2151_status_port_0_r),
                new MemoryReadAddress(0x120000, 0x120001, OKIM6295_status_0_r),
                new MemoryReadAddress(0x130000, 0x130001, OKIM6295_status_1_r),
                new MemoryReadAddress(0x140000, 0x140001, soundlatch_r),
                new MemoryReadAddress(0x1f0000, 0x1f1fff, MRA_BANK8),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x00ffff, MWA_ROM),
                new MemoryWriteAddress(0x100000, 0x100001, YM2203_w),
                new MemoryWriteAddress(0x110000, 0x110001, YM2151_w),
                new MemoryWriteAddress(0x120000, 0x120001, OKIM6295_data_0_w),
                new MemoryWriteAddress(0x130000, 0x130001, OKIM6295_data_1_w),
                new MemoryWriteAddress(0x1f0000, 0x1f1fff, MWA_BANK8),
                new MemoryWriteAddress(0x1fec00, 0x1fec01, H6280_timer_w),
                new MemoryWriteAddress(0x1ff402, 0x1ff403, H6280_irq_status_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress stoneage_s_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x87ff, MRA_RAM),
                new MemoryReadAddress(0x8801, 0x8801, YM2151_status_port_0_r),
                new MemoryReadAddress(0xa000, 0xa000, soundlatch_r),
                new MemoryReadAddress(0x9800, 0x9800, OKIM6295_status_0_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress stoneage_s_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM),
                new MemoryWriteAddress(0x8800, 0x8800, YM2151_register_port_0_w),
                new MemoryWriteAddress(0x8801, 0x8801, YM2151_data_port_0_w),
                new MemoryWriteAddress(0x9800, 0x9800, OKIM6295_data_0_w),
                new MemoryWriteAddress(-1) /* end of table */};

    /**
     * *******************************************************************************
     */
    static InputPortHandlerPtr input_ports_cninja = new InputPortHandlerPtr() {
        public void handler() {

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_VBLANK);
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* Dip switch bank 1 */
            PORT_DIPNAME(0x07, 0x07, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_6C"));
            PORT_DIPNAME(0x38, 0x38, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x38, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x28, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x20, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x18, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_6C"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* Dip switch bank 2 */
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "1");
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x02, "4");
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x08, "Easy");
            PORT_DIPSETTING(0x0c, "Normal");
            PORT_DIPSETTING(0x04, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_cninjau = new InputPortHandlerPtr() {
        public void handler() {

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_VBLANK);
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* Dip switch bank 1 */
            PORT_DIPNAME(0x07, 0x07, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_6C"));
            PORT_DIPNAME(0x38, 0x38, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x38, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x28, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x20, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x18, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_6C"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, "Credit(s) to Start");
            PORT_DIPSETTING(0x80, "1");
            PORT_DIPSETTING(0x00, "2");
            /* Also, if Coin A and B are on 1/1, 0x00 gives 2 to start, 1 to continue */

            PORT_START();
            /* Dip switch bank 2 */
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "1");
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x02, "4");
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x08, "Easy");
            PORT_DIPSETTING(0x0c, "Normal");
            PORT_DIPSETTING(0x04, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unused"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unused"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    /**
     * *******************************************************************************
     */
    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 chars */
            4096,
            4, /* 4 bits per pixel  */
            new int[]{0x08000 * 8, 0x18000 * 8, 0x00000 * 8, 0x10000 * 8},
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16,
            0x4000, /* A lotta sprites.. */
            4,
            new int[]{8, 0, 0x100000 * 8 + 8, 0x100000 * 8},
            new int[]{32 * 8 + 0, 32 * 8 + 1, 32 * 8 + 2, 32 * 8 + 3, 32 * 8 + 4, 32 * 8 + 5, 32 * 8 + 6, 32 * 8 + 7,
                0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                8 * 16, 9 * 16, 10 * 16, 11 * 16, 12 * 16, 13 * 16, 14 * 16, 15 * 16},
            64 * 8
    );

    static GfxLayout spritelayout2 = new GfxLayout(
            16, 16,
            0xa000, /* A lotta sprites.. */
            4,
            new int[]{8, 0, 0x280000 * 8 + 8, 0x280000 * 8},
            new int[]{32 * 8 + 0, 32 * 8 + 1, 32 * 8 + 2, 32 * 8 + 3, 32 * 8 + 4, 32 * 8 + 5, 32 * 8 + 6, 32 * 8 + 7,
                0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                8 * 16, 9 * 16, 10 * 16, 11 * 16, 12 * 16, 13 * 16, 14 * 16, 15 * 16},
            64 * 8
    );

    static GfxLayout tilelayout = new GfxLayout(
            16, 16,
            4096,
            4,
            new int[]{0x40000 * 8 + 8, 0x40000 * 8, 8, 0,},
            new int[]{32 * 8 + 0, 32 * 8 + 1, 32 * 8 + 2, 32 * 8 + 3, 32 * 8 + 4, 32 * 8 + 5, 32 * 8 + 6, 32 * 8 + 7,
                0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                8 * 16, 9 * 16, 10 * 16, 11 * 16, 12 * 16, 13 * 16, 14 * 16, 15 * 16},
            64 * 8
    );

    /* Areas 1536-2048 & 1024-1280 seem to be always empty */
    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 16), /* Characters 8x8 */
                new GfxDecodeInfo(REGION_GFX2, 0, tilelayout, 512, 64), /* Tiles 16x16 (BASE 1280) */
                new GfxDecodeInfo(REGION_GFX3, 0, tilelayout, 512, 64), /* Tiles 16x16 */
                new GfxDecodeInfo(REGION_GFX4, 0, tilelayout, 256, 16), /* Tiles 16x16 */
                new GfxDecodeInfo(REGION_GFX5, 0, spritelayout, 768, 16), /* Sprites 16x16 */
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo gfxdecodeinfo2[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 16), /* Characters 8x8 */
                new GfxDecodeInfo(REGION_GFX2, 0, tilelayout, 512, 64), /* Tiles 16x16 (BASE 1280) */
                new GfxDecodeInfo(REGION_GFX3, 0, tilelayout, 512, 64), /* Tiles 16x16 */
                new GfxDecodeInfo(REGION_GFX4, 0, tilelayout, 256, 16), /* Tiles 16x16 */
                new GfxDecodeInfo(REGION_GFX5, 0, spritelayout2, 768, 16), /* Sprites 16x16 */
                new GfxDecodeInfo(-1) /* end of array */};

    /**
     * *******************************************************************************
     */
    static YM2203interface ym2203_interface = new YM2203interface(
            1,
            32220000 / 8, /* Accurate, audio section crystal is 32.220 MHz */
            new int[]{YM2203_VOL(40, 40)},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );
    public static WriteYmHandlerPtr sound_irq = new WriteYmHandlerPtr() {
        public void handler(int state) {
            cpu_set_irq_line(1, 1, state);
            /* IRQ 2 */
        }
    };
    public static WriteYmHandlerPtr sound_irq2 = new WriteYmHandlerPtr() {
        public void handler(int state) {
            cpu_set_irq_line(1, 0, state);
        }
    };

    public static WriteHandlerPtr sound_bankswitch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* the second OKIM6295 ROM is bank switched */
            OKIM6295_set_bank_base(1, ALL_VOICES, (data & 1) * 0x40000);
        }
    };

    static YM2151interface ym2151_interface = new YM2151interface(
            1,
            32220000 / 9, /* Accurate, audio section crystal is 32.220 MHz */
            new int[]{YM3012_VOL(45, MIXER_PAN_LEFT, 45, MIXER_PAN_RIGHT)},
            new WriteYmHandlerPtr[]{sound_irq},
            new WriteHandlerPtr[]{sound_bankswitch_w}
    );

    static YM2151interface ym2151_interface2 = new YM2151interface(
            1,
            3579545, /* 3.579545 Mhz (?) */
            new int[]{YM3012_VOL(50, MIXER_PAN_CENTER, 50, MIXER_PAN_CENTER)},
            new WriteYmHandlerPtr[]{sound_irq2}
    );

    static OKIM6295interface okim6295_interface = new OKIM6295interface(
            2, /* 2 chips */
            new int[]{7757, 15514},/* Frequency */
            new int[]{REGION_SOUND1, REGION_SOUND2}, /* memory regions 3 & 4 */
            new int[]{50, 25} /* Note!  Keep chip 1 (voices) louder than chip 2 */
    );

    /**
     * *******************************************************************************
     */
    static MachineDriver machine_driver_cninja = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        12000000,
                        cninja_readmem, cninja_writemem, null, null,
                        m68_level5_irq, 1
                ),
                new MachineCPU(
                        CPU_H6280 | CPU_AUDIO_CPU,
                        32220000 / 8, /* Accurate */
                        sound_readmem, sound_writemem, null, null,
                        ignore_interrupt, 0
                )
            },
            58, 529,
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 1 * 8, 31 * 8 - 1),
            gfxdecodeinfo,
            2048, 2048,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
            null,
            cninja_vh_start,
            cninja_vh_stop,
            cninja_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0, /* Mono */
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2203,
                        ym2203_interface
                ),
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface
                ),
                new MachineSound(
                        SOUND_OKIM6295,
                        okim6295_interface
                )
            }
    );

    static MachineDriver machine_driver_stoneage = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        12000000,
                        cninja_readmem, cninja_writemem, null, null,
                        m68_level5_irq, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        3579545,
                        stoneage_s_readmem, stoneage_s_writemem, null, null,
                        ignore_interrupt, 0
                )
            },
            58, 529,
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 1 * 8, 31 * 8 - 1),
            gfxdecodeinfo,
            2048, 2048,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
            null,
            cninja_vh_start,
            cninja_vh_stop,
            cninja_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0, /* Mono */
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface2
                ),
                new MachineSound(
                        SOUND_OKIM6295,
                        okim6295_interface
                )
            }
    );

    static MachineDriver machine_driver_edrandy = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        12000000,
                        edrandy_readmem, edrandy_writemem, null, null,
                        m68_level5_irq, 1
                ),
                new MachineCPU(
                        CPU_H6280 | CPU_AUDIO_CPU,
                        32220000 / 8, /* Accurate */
                        sound_readmem, sound_writemem, null, null,
                        ignore_interrupt, 0
                )
            },
            58, 529,
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 1 * 8, 31 * 8 - 1),
            gfxdecodeinfo2,
            2048, 2048,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
            null,
            cninja_vh_start,
            cninja_vh_stop,
            cninja_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0, /* Mono */
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2203,
                        ym2203_interface
                ),
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface
                ),
                new MachineSound(
                        SOUND_OKIM6295,
                        okim6295_interface
                )
            }
    );

    /**
     * *******************************************************************************
     */
    static RomLoadHandlerPtr rom_cninja = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0xc0000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("gn02rev3.bin", 0x00000, 0x20000, 0x39aea12a);
            ROM_LOAD_ODD("gn05rev2.bin", 0x00000, 0x20000, 0x0f4360ef);
            ROM_LOAD_EVEN("gn01rev2.bin", 0x40000, 0x20000, 0xf740ef7e);
            ROM_LOAD_ODD("gn04rev2.bin", 0x40000, 0x20000, 0xc98fcb62);
            ROM_LOAD_EVEN("gn-00.rom", 0x80000, 0x20000, 0x0b110b16);
            ROM_LOAD_ODD("gn-03.rom", 0x80000, 0x20000, 0x1e28e697);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound CPU */
            ROM_LOAD("gl-07.rom", 0x00000, 0x10000, 0xca8bef96);

            ROM_REGION(0x020000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gl-08.rom", 0x00000, 0x10000, 0x33a2b400);/* chars */
            ROM_LOAD("gl-09.rom", 0x10000, 0x10000, 0x5a2d4752);

            ROM_REGION(0x080000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mag-00.rom", 0x000000, 0x80000, 0xa8f05d33);/* tiles 1 */

            ROM_REGION(0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mag-01.rom", 0x000000, 0x80000, 0x5b399eed);/* tiles 2 */

            ROM_REGION(0x080000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mag-02.rom", 0x000000, 0x80000, 0xde89c69a);/* tiles 3 */

            ROM_REGION(0x200000, REGION_GFX5 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mag-03.rom", 0x000000, 0x80000, 0x2220eb9f);/* sprites */
            ROM_LOAD("mag-04.rom", 0x080000, 0x80000, 0x144b94cc);
            ROM_LOAD("mag-05.rom", 0x100000, 0x80000, 0x56a53254);
            ROM_LOAD("mag-06.rom", 0x180000, 0x80000, 0x82d44749);

            ROM_REGION(0x20000, REGION_SOUND1);/* Oki samples */
            ROM_LOAD("gl-06.rom", 0x00000, 0x20000, 0xd92e519d);

            ROM_REGION(0x80000, REGION_SOUND2);/* Extra Oki samples */
            ROM_LOAD("mag-07.rom", 0x00000, 0x80000, 0x08eb5264);/* banked */
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_cninja0 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0xc0000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("gn-02.rom", 0x00000, 0x20000, 0xccc59524);
            ROM_LOAD_ODD("gn-05.rom", 0x00000, 0x20000, 0xa002cbe4);
            ROM_LOAD_EVEN("gn-01.rom", 0x40000, 0x20000, 0x18f0527c);
            ROM_LOAD_ODD("gn-04.rom", 0x40000, 0x20000, 0xea4b6d53);
            ROM_LOAD_EVEN("gn-00.rom", 0x80000, 0x20000, 0x0b110b16);
            ROM_LOAD_ODD("gn-03.rom", 0x80000, 0x20000, 0x1e28e697);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound CPU */
            ROM_LOAD("gl-07.rom", 0x00000, 0x10000, 0xca8bef96);

            ROM_REGION(0x020000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gl-08.rom", 0x00000, 0x10000, 0x33a2b400);/* chars */
            ROM_LOAD("gl-09.rom", 0x10000, 0x10000, 0x5a2d4752);

            ROM_REGION(0x080000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mag-00.rom", 0x000000, 0x80000, 0xa8f05d33);/* tiles 1 */

            ROM_REGION(0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mag-01.rom", 0x000000, 0x80000, 0x5b399eed);/* tiles 2 */

            ROM_REGION(0x080000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mag-02.rom", 0x000000, 0x80000, 0xde89c69a);/* tiles 3 */

            ROM_REGION(0x200000, REGION_GFX5 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mag-03.rom", 0x000000, 0x80000, 0x2220eb9f);/* sprites */
            ROM_LOAD("mag-04.rom", 0x080000, 0x80000, 0x144b94cc);
            ROM_LOAD("mag-05.rom", 0x100000, 0x80000, 0x56a53254);
            ROM_LOAD("mag-06.rom", 0x180000, 0x80000, 0x82d44749);

            ROM_REGION(0x20000, REGION_SOUND1);/* Oki samples */
            ROM_LOAD("gl-06.rom", 0x00000, 0x20000, 0xd92e519d);

            ROM_REGION(0x80000, REGION_SOUND2);/* Extra Oki samples */
            ROM_LOAD("mag-07.rom", 0x00000, 0x80000, 0x08eb5264);/* banked */
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_cninjau = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0xc0000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("gm02-3.1k", 0x00000, 0x20000, 0xd931c3b1);
            ROM_LOAD_ODD("gm05-2.3k", 0x00000, 0x20000, 0x7417d3fb);
            ROM_LOAD_EVEN("gm01-2.1j", 0x40000, 0x20000, 0x72041f7e);
            ROM_LOAD_ODD("gm04-2.3j", 0x40000, 0x20000, 0x2104d005);
            ROM_LOAD_EVEN("gn-00.rom", 0x80000, 0x20000, 0x0b110b16);
            ROM_LOAD_ODD("gn-03.rom", 0x80000, 0x20000, 0x1e28e697);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound CPU */
            ROM_LOAD("gl-07.rom", 0x00000, 0x10000, 0xca8bef96);

            ROM_REGION(0x020000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gl-08.rom", 0x00000, 0x10000, 0x33a2b400);/* chars */
            ROM_LOAD("gl-09.rom", 0x10000, 0x10000, 0x5a2d4752);

            ROM_REGION(0x080000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mag-00.rom", 0x000000, 0x80000, 0xa8f05d33);/* tiles 1 */

            ROM_REGION(0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mag-01.rom", 0x000000, 0x80000, 0x5b399eed);/* tiles 2 */

            ROM_REGION(0x080000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mag-02.rom", 0x000000, 0x80000, 0xde89c69a);/* tiles 3 */

            ROM_REGION(0x200000, REGION_GFX5 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mag-03.rom", 0x000000, 0x80000, 0x2220eb9f);/* sprites */
            ROM_LOAD("mag-04.rom", 0x080000, 0x80000, 0x144b94cc);
            ROM_LOAD("mag-05.rom", 0x100000, 0x80000, 0x56a53254);
            ROM_LOAD("mag-06.rom", 0x180000, 0x80000, 0x82d44749);

            ROM_REGION(0x20000, REGION_SOUND1);/* Oki samples */
            ROM_LOAD("gl-06.rom", 0x00000, 0x20000, 0xd92e519d);

            ROM_REGION(0x80000, REGION_SOUND2);/* Extra Oki samples */
            ROM_LOAD("mag-07.rom", 0x00000, 0x80000, 0x08eb5264);/* banked */
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_joemac = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0xc0000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("gl02-2.k1", 0x00000, 0x20000, 0x80da12e2);
            ROM_LOAD_ODD("gl05-2.k3", 0x00000, 0x20000, 0xfe4dbbbb);
            ROM_LOAD_EVEN("gl01-2.j1", 0x40000, 0x20000, 0x0b245307);
            ROM_LOAD_ODD("gl04-2.j3", 0x40000, 0x20000, 0x1b331f61);
            ROM_LOAD_EVEN("gn-00.rom", 0x80000, 0x20000, 0x0b110b16);
            ROM_LOAD_ODD("gn-03.rom", 0x80000, 0x20000, 0x1e28e697);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound CPU */
            ROM_LOAD("gl-07.rom", 0x00000, 0x10000, 0xca8bef96);

            ROM_REGION(0x020000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gl-08.rom", 0x00000, 0x10000, 0x33a2b400);/* chars */
            ROM_LOAD("gl-09.rom", 0x10000, 0x10000, 0x5a2d4752);

            ROM_REGION(0x080000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mag-00.rom", 0x000000, 0x80000, 0xa8f05d33);/* tiles 1 */

            ROM_REGION(0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mag-01.rom", 0x000000, 0x80000, 0x5b399eed);/* tiles 2 */

            ROM_REGION(0x080000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mag-02.rom", 0x000000, 0x80000, 0xde89c69a);/* tiles 3 */

            ROM_REGION(0x200000, REGION_GFX5 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mag-03.rom", 0x000000, 0x80000, 0x2220eb9f);/* sprites */
            ROM_LOAD("mag-04.rom", 0x080000, 0x80000, 0x144b94cc);
            ROM_LOAD("mag-05.rom", 0x100000, 0x80000, 0x56a53254);
            ROM_LOAD("mag-06.rom", 0x180000, 0x80000, 0x82d44749);

            ROM_REGION(0x20000, REGION_SOUND1);/* Oki samples */
            ROM_LOAD("gl-06.rom", 0x00000, 0x20000, 0xd92e519d);

            ROM_REGION(0x80000, REGION_SOUND2);/* Extra Oki samples */
            ROM_LOAD("mag-07.rom", 0x00000, 0x80000, 0x08eb5264);/* banked */
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_stoneage = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0xc0000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("sa_1_019.bin", 0x00000, 0x20000, 0x7fb8c44f);
            ROM_LOAD_ODD("sa_1_033.bin", 0x00000, 0x20000, 0x961c752b);
            ROM_LOAD_EVEN("sa_1_018.bin", 0x40000, 0x20000, 0xa4043022);
            ROM_LOAD_ODD("sa_1_032.bin", 0x40000, 0x20000, 0xf52a3286);
            ROM_LOAD_EVEN("sa_1_017.bin", 0x80000, 0x20000, 0x08d6397a);
            ROM_LOAD_ODD("sa_1_031.bin", 0x80000, 0x20000, 0x103079f5);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound CPU */
            ROM_LOAD("sa_1_012.bin", 0x00000, 0x10000, 0x56058934);

            ROM_REGION(0x020000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gl-08.rom", 0x00000, 0x10000, 0x33a2b400);/* chars */
            ROM_LOAD("gl-09.rom", 0x10000, 0x10000, 0x5a2d4752);

            /* The bootleg graphics are stored in a different arrangement but
			seem to be the same as the original set */
            ROM_REGION(0x080000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mag-00.rom", 0x000000, 0x80000, 0xa8f05d33);/* tiles 1 */

            ROM_REGION(0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mag-01.rom", 0x000000, 0x80000, 0x5b399eed);/* tiles 2 */

            ROM_REGION(0x080000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mag-02.rom", 0x000000, 0x80000, 0xde89c69a);/* tiles 3 */

            ROM_REGION(0x200000, REGION_GFX5 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mag-03.rom", 0x000000, 0x80000, 0x2220eb9f);/* sprites */
            ROM_LOAD("mag-04.rom", 0x080000, 0x80000, 0x144b94cc);
            ROM_LOAD("mag-05.rom", 0x100000, 0x80000, 0x56a53254);
            ROM_LOAD("mag-06.rom", 0x180000, 0x80000, 0x82d44749);

            ROM_REGION(0x40000, REGION_SOUND1);/* Oki samples */
            ROM_LOAD("sa_1_069.bin", 0x00000, 0x40000, 0x2188f3ca);

            /* No extra Oki samples in the bootleg */
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_edrandy = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x100000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("gg-00-2", 0x00000, 0x20000, 0xce1ba964);
            ROM_LOAD_ODD("gg-04-2", 0x00000, 0x20000, 0x24caed19);
            ROM_LOAD_EVEN("gg-01-2", 0x40000, 0x20000, 0x33677b80);
            ROM_LOAD_ODD("gg-05-2", 0x40000, 0x20000, 0x79a68ca6);
            ROM_LOAD_EVEN("ge-02", 0x80000, 0x20000, 0xc2969fbb);
            ROM_LOAD_ODD("ge-06", 0x80000, 0x20000, 0x5c2e6418);
            ROM_LOAD_EVEN("ge-03", 0xc0000, 0x20000, 0x5e7b19a8);
            ROM_LOAD_ODD("ge-07", 0xc0000, 0x20000, 0x5eb819a1);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound CPU */
            ROM_LOAD("ge-09", 0x00000, 0x10000, 0x9f94c60b);

            ROM_REGION(0x020000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gg-11", 0x000000, 0x10000, 0xee567448);
            ROM_LOAD("gg-10", 0x010000, 0x10000, 0xb96c6cbe);

            ROM_REGION(0x080000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mad-00", 0x000000, 0x80000, 0x3735b22d);/* tiles 1 */

            ROM_REGION(0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mad-01", 0x000000, 0x80000, 0x7bb13e1c);/* tiles 2 */

            ROM_REGION(0x080000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mad-02", 0x000000, 0x80000, 0x6c76face);/* tiles 3 */

            ROM_REGION(0x500000, REGION_GFX5 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mad-03", 0x000000, 0x80000, 0xc0bff892);/* sprites */
            ROM_LOAD("mad-04", 0x080000, 0x80000, 0x464f3eb9);
            ROM_LOAD("mad-07", 0x100000, 0x80000, 0xac03466e);
            ROM_LOAD("mad-10", 0x180000, 0x80000, 0x42da8ef0);
            ROM_LOAD("mad-09", 0x200000, 0x80000, 0x930f4900);
            ROM_LOAD("mad-05", 0x280000, 0x80000, 0x3f2ccf95);
            ROM_LOAD("mad-06", 0x300000, 0x80000, 0x60871f77);
            ROM_LOAD("mad-08", 0x380000, 0x80000, 0x1b420ec8);
            ROM_LOAD("mad-11", 0x400000, 0x80000, 0x03c1f982);
            ROM_LOAD("mad-12", 0x480000, 0x80000, 0xa0bd62b6);

            ROM_REGION(0x20000, REGION_SOUND1);/* ADPCM samples */
            ROM_LOAD("ge-08", 0x00000, 0x20000, 0xdfe28c7b);

            ROM_REGION(0x80000, REGION_SOUND2);/* Extra Oki samples */
            ROM_LOAD("mad-13", 0x00000, 0x80000, 0x6ab28eba);/* banked */
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_edrandyj = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x100000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("ge-00-2", 0x00000, 0x20000, 0xb3d2403c);
            ROM_LOAD_ODD("ge-04-2", 0x00000, 0x20000, 0x8a9624d6);
            ROM_LOAD_EVEN("ge-01-2", 0x40000, 0x20000, 0x84360123);
            ROM_LOAD_ODD("ge-05-2", 0x40000, 0x20000, 0x0bf85d9d);
            ROM_LOAD_EVEN("ge-02", 0x80000, 0x20000, 0xc2969fbb);
            ROM_LOAD_ODD("ge-06", 0x80000, 0x20000, 0x5c2e6418);
            ROM_LOAD_EVEN("ge-03", 0xc0000, 0x20000, 0x5e7b19a8);
            ROM_LOAD_ODD("ge-07", 0xc0000, 0x20000, 0x5eb819a1);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound CPU */
            ROM_LOAD("ge-09", 0x00000, 0x10000, 0x9f94c60b);

            ROM_REGION(0x020000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ge-10", 0x000000, 0x10000, 0x2528d795);
            ROM_LOAD("ge-11", 0x010000, 0x10000, 0xe34a931e);

            ROM_REGION(0x080000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mad-00", 0x000000, 0x80000, 0x3735b22d);/* tiles 1 */

            ROM_REGION(0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mad-01", 0x000000, 0x80000, 0x7bb13e1c);/* tiles 2 */

            ROM_REGION(0x080000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mad-02", 0x000000, 0x80000, 0x6c76face);/* tiles 3 */

            ROM_REGION(0x500000, REGION_GFX5 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mad-03", 0x000000, 0x80000, 0xc0bff892);/* sprites */
            ROM_LOAD("mad-04", 0x080000, 0x80000, 0x464f3eb9);
            ROM_LOAD("mad-07", 0x100000, 0x80000, 0xac03466e);
            ROM_LOAD("mad-10", 0x180000, 0x80000, 0x42da8ef0);
            ROM_LOAD("mad-09", 0x200000, 0x80000, 0x930f4900);
            ROM_LOAD("mad-05", 0x280000, 0x80000, 0x3f2ccf95);
            ROM_LOAD("mad-06", 0x300000, 0x80000, 0x60871f77);
            ROM_LOAD("mad-08", 0x380000, 0x80000, 0x1b420ec8);
            ROM_LOAD("mad-11", 0x400000, 0x80000, 0x03c1f982);
            ROM_LOAD("mad-12", 0x480000, 0x80000, 0xa0bd62b6);

            ROM_REGION(0x20000, REGION_SOUND1);/* ADPCM samples */
            ROM_LOAD("ge-08", 0x00000, 0x20000, 0xdfe28c7b);

            ROM_REGION(0x80000, REGION_SOUND2);/* Extra Oki samples */
            ROM_LOAD("mad-13", 0x00000, 0x80000, 0x6ab28eba);/* banked */
            ROM_END();
        }
    };

    /**
     * *******************************************************************************
     */
    static void cninja_patch() {
        UBytePtr RAM = memory_region(REGION_CPU1);
        int i;

        for (i = 0; i < 0x80000; i += 2) {
            int aword = RAM.READ_WORD(i);

            if (aword == 0x66ff || aword == 0x67ff) {
                int doublecheck = RAM.READ_WORD(i - 8);

                /* Cmpi + btst controlling opcodes */
                if (doublecheck == 0xc39 || doublecheck == 0x839) {

                    RAM.WRITE_WORD(i, 0x4E71);
                    RAM.WRITE_WORD(i - 2, 0x4E71);
                    RAM.WRITE_WORD(i - 4, 0x4E71);
                    RAM.WRITE_WORD(i - 6, 0x4E71);
                    RAM.WRITE_WORD(i - 8, 0x4E71);
                }
            }
        }
    }

    /**
     * *******************************************************************************
     */
    public static InitDriverHandlerPtr init_cninja = new InitDriverHandlerPtr() {
        public void handler() {
            install_mem_write_handler(0, 0x1bc0a8, 0x1bc0a9, cninja_sound_w);
            cninja_patch();
        }
    };

    public static InitDriverHandlerPtr init_stoneage = new InitDriverHandlerPtr() {
        public void handler() {
            install_mem_write_handler(0, 0x1bc0a8, 0x1bc0a9, stoneage_sound_w);
        }
    };

    /**
     * *******************************************************************************
     */
    public static GameDriver driver_cninja = new GameDriver("1991", "cninja", "cninja.java", rom_cninja, null, machine_driver_cninja, input_ports_cninja, init_cninja, ROT0, "Data East Corporation", "Caveman Ninja (World revision 3)");
    public static GameDriver driver_cninja0 = new GameDriver("1991", "cninja0", "cninja.java", rom_cninja0, driver_cninja, machine_driver_cninja, input_ports_cninja, init_cninja, ROT0, "Data East Corporation", "Caveman Ninja (World revision null)");
    public static GameDriver driver_cninjau = new GameDriver("1991", "cninjau", "cninja.java", rom_cninjau, driver_cninja, machine_driver_cninja, input_ports_cninjau, init_cninja, ROT0, "Data East Corporation", "Caveman Ninja (US)");
    public static GameDriver driver_joemac = new GameDriver("1991", "joemac", "cninja.java", rom_joemac, driver_cninja, machine_driver_cninja, input_ports_cninja, init_cninja, ROT0, "Data East Corporation", "Joe  Mac (Japan)");
    public static GameDriver driver_stoneage = new GameDriver("1991", "stoneage", "cninja.java", rom_stoneage, driver_cninja, machine_driver_stoneage, input_ports_cninja, init_stoneage, ROT0, "bootleg", "Stoneage");
    /*TODO*///	GAMEX(1990, edrandy,  null,       edrandy,  cninja,  null,        ROT0, "Data East Corporation", "Edward Randy (World)", GAME_NOT_WORKING )
/*TDODO*///	GAMEX(1990, edrandyj, edrandy, edrandy,  cninja,  null,        ROT0, "Data East Corporation", "Edward Randy (Japan)", GAME_NOT_WORKING )
}
