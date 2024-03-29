/*
 * ported to v0.36
 *
 */
/**
 * Changelog
 * =========
 * 25/12/2022 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.machine;

//drivers imports
import static arcadeflex.v036.drivers.tnzs.*;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.mame.errorlog;
import static arcadeflex.v036.mame.memory.*;
import static arcadeflex.v036.mame.memoryH.*;
//common imports
import static common.libc.cstring.*;
//TODO
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.platform.libc_old.fprintf;
//to be organized

public class tnzs {

    static int mcu_type;

    public static final int MCU_NONE = 0;
    public static final int MCU_EXTRMATN = 1;
    public static final int MCU_ARKANOID = 2;
    public static final int MCU_DRTOPPEL = 3;
    public static final int MCU_CHUKATAI = 4;
    public static final int MCU_TNZS = 5;

    static int mcu_initializing, mcu_coinage_init, mcu_command, mcu_readcredits;
    static int mcu_reportcoin;
    static int tnzs_workram_backup;
    static char[] mcu_coinage = new char[4];
    static char mcu_coinsA, mcu_coinsB, mcu_credits;

    public static ReadHandlerPtr arkanoi2_sh_f000_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int val;

            if (errorlog != null) {
                fprintf(errorlog, "PC %04x: read input %04x\n", cpu_get_pc(), 0xf000 + offset);
            }

            val = readinputport(5 + offset / 2);
            if ((offset & 1) != 0) {
                return ((val >> 8) & 0xff);
            } else {
                return val & 0xff;
            }
        }
    };

    static void mcu_reset() {
        mcu_initializing = 3;
        mcu_coinage_init = 0;
        mcu_coinage[0] = 1;
        mcu_coinage[1] = 1;
        mcu_coinage[2] = 1;
        mcu_coinage[3] = 1;
        mcu_coinsA = 0;
        mcu_coinsB = 0;
        mcu_credits = 0;
        mcu_reportcoin = 0;
        mcu_command = 0;
        tnzs_workram_backup = -1;
    }
    static int insertcoin;

    static void mcu_handle_coins(int coin) {

        /* The coin inputs and coin counter is managed by the i8742 mcu. */
 /* Here we simulate it. */
 /* Chuka Taisen has a limit of 9 credits, so any */
 /* coins that could push it over 9 should be rejected */
 /* Coin/Play settings must also be taken into consideration */
        if ((coin & 0x08) != 0) /* tilt */ {
            mcu_reportcoin = coin;
        } else if (coin != 0 && coin != insertcoin) {
            if ((coin & 0x01) != 0) /* coin A */ {
                if ((mcu_type == MCU_CHUKATAI) && ((mcu_credits + mcu_coinage[1]) > 9)) {
                    coin_lockout_global_w.handler(0, 1);
                    /* Lock all coin slots */
                } else {
                    if (errorlog != null) {
                        fprintf(errorlog, "Coin dropped into slot A\n");
                    }
                    coin_lockout_global_w.handler(0, 0);
                    /* Unlock all coin slots */
                    coin_counter_w.handler(0, 1);
                    coin_counter_w.handler(0, 0);
                    /* Count slot A */
                    mcu_coinsA++;
                    if (mcu_coinsA >= mcu_coinage[0]) {
                        mcu_coinsA -= mcu_coinage[0];
                        mcu_credits += mcu_coinage[1];
                    }
                }
            }
            if ((coin & 0x02) != 0) /* coin B */ {
                if ((mcu_type == MCU_CHUKATAI) && ((mcu_credits + mcu_coinage[3]) > 9)) {
                    coin_lockout_global_w.handler(0, 1);
                    /* Lock all coin slots */
                } else {
                    if (errorlog != null) {
                        fprintf(errorlog, "Coin dropped into slot B\n");
                    }
                    coin_lockout_global_w.handler(0, 0);
                    /* Unlock all coin slots */
                    coin_counter_w.handler(1, 1);
                    coin_counter_w.handler(1, 0);
                    /* Count slot B */
                    mcu_coinsB++;
                    if (mcu_coinsB >= mcu_coinage[2]) {
                        mcu_coinsB -= mcu_coinage[2];
                        mcu_credits += mcu_coinage[3];
                    }
                }
            }
            if ((coin & 0x04) != 0) /* service */ {
                if (errorlog != null) {
                    fprintf(errorlog, "Coin dropped into service slot C\n");
                }
                mcu_credits++;
            }
            mcu_reportcoin = coin;
        } else {
            coin_lockout_global_w.handler(0, 0);
            /* Unlock all coin slots */
            mcu_reportcoin = 0;
        }
        insertcoin = coin;
    }

    public static ReadHandlerPtr mcu_arkanoi2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            //char *mcu_startup = "\x55\xaa\x5a";
            char[] mcu_startup = {0x55, 0xaa, 0x5a};

            //	if (errorlog != 0) fprintf (errorlog, "PC %04x: read mcu %04x\n", cpu_get_pc(), 0xc000 + offset);
            if (offset == 0) {
                /* if the mcu has just been reset, return startup code */
                if (mcu_initializing != 0) {
                    mcu_initializing--;
                    return mcu_startup[2 - mcu_initializing];
                }

                switch (mcu_command) {
                    case 0x41:
                        return mcu_credits;

                    case 0xc1:
                        /* Read the credit counter or the inputs */
                        if (mcu_readcredits == 0) {
                            mcu_readcredits = 1;
                            if ((mcu_reportcoin & 0x08) != 0) {
                                mcu_initializing = 3;
                                return 0xee;
                                /* tilt */
                            } else {
                                return mcu_credits;
                            }
                        } else {
                            return readinputport(2);
                            /* buttons */
                        }

                    default:
                        if (errorlog != null) {
                            fprintf(errorlog, "error, unknown mcu command\n");
                        }
                        /* should not happen */
                        return 0xff;
                    //break;
                }
            } else {
                /*
			status bits:
			0 = mcu is ready to send data (read from c000)
			1 = mcu has read data (from c000)
			2 = unused
			3 = unused
			4-7 = coin code
			      0 = nothing
			      1,2,3 = coin switch pressed
			      e = tilt
                 */
                if ((mcu_reportcoin & 0x08) != 0) {
                    return 0xe1;
                    /* tilt */
                }
                if ((mcu_reportcoin & 0x01) != 0) {
                    return 0x11;
                    /* coin 1 (will trigger "coin inserted" sound) */
                }
                if ((mcu_reportcoin & 0x02) != 0) {
                    return 0x21;
                    /* coin 2 (will trigger "coin inserted" sound) */
                }
                if ((mcu_reportcoin & 0x04) != 0) {
                    return 0x31;
                    /* coin 3 (will trigger "coin inserted" sound) */
                }
                return 0x01;
            }
        }
    };

    public static WriteHandlerPtr mcu_arkanoi2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset == 0) {
                //	if (errorlog != 0) fprintf (errorlog, "PC %04x (re %04x): write %02x to mcu %04x\n", cpu_get_pc(), cpu_geturnpc(), data, 0xc000 + offset);
                if (mcu_command == 0x41) {
                    mcu_credits = (char) ((mcu_credits + data) & 0xff);
                }
            } else {
                /*
			0xc1: read number of credits, then buttons
			0x54+0x41: add value to number of credits
			0x84: coin 1 lockout (issued only in test mode)
			0x88: coin 2 lockout (issued only in test mode)
			0x80: release coin lockout (issued only in test mode)
			during initialization, a sequence of 4 bytes sets coin/credit settings
                 */
                //	if (errorlog != 0) fprintf (errorlog, "PC %04x (re %04x): write %02x to mcu %04x\n", cpu_get_pc(), cpu_geturnpc(), data, 0xc000 + offset);

                if (mcu_initializing != 0) {
                    /* set up coin/credit settings */
                    mcu_coinage[mcu_coinage_init++] = (char) data;
                    if (mcu_coinage_init == 4) {
                        mcu_coinage_init = 0;
                        /* must not happen */
                    }
                }

                if (data == 0xc1) {
                    mcu_readcredits = 0;
                    /* reset input port number */
                }

                mcu_command = data;
            }
        }
    };

    public static ReadHandlerPtr mcu_chukatai_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            //char *mcu_startup = "\xa5\x5a\xaa";
            char[] mcu_startup = {0xa5, 0x5a, 0xaa};

            /*TODO*///		if (errorlog != null) fprintf (errorlog, "PC %04x (re %04x): read mcu %04x\n", cpu_get_pc(), cpu_geturnpc(), 0xc000 + offset);
            if (offset == 0) {
                /* if the mcu has just been reset, return startup code */
                if (mcu_initializing != 0) {
                    mcu_initializing--;
                    return mcu_startup[2 - mcu_initializing];
                }

                switch (mcu_command) {
                    case 0x1f:
                        return (readinputport(4) >> 4) ^ 0x0f;

                    case 0x03:
                        return readinputport(4) & 0x0f;

                    case 0x41:
                        return mcu_credits;

                    case 0x93:
                        /* Read the credit counter or the inputs */
                        if (mcu_readcredits == 0) {
                            mcu_readcredits += 1;
                            if ((mcu_reportcoin & 0x08) != 0) {
                                mcu_initializing = 3;
                                return 0xee;
                                /* tilt */
                            } else {
                                return mcu_credits;
                            }
                        }
                        /* player 1 joystick and buttons */
                        if (mcu_readcredits == 1) {
                            mcu_readcredits += 1;
                            return readinputport(2);
                        }
                        /* player 2 joystick and buttons */
                        if (mcu_readcredits == 2) {
                            return readinputport(3);
                        }

                    default:
                        if (errorlog != null) {
                            fprintf(errorlog, "error, unknown mcu command (%02x)\n", mcu_command);
                        }
                        /* should not happen */
                        return 0xff;
                    //break;
                }
            } else {
                /*
			status bits:
			0 = mcu is ready to send data (read from c000)
			1 = mcu has read data (from c000)
			2 = mcu is busy
			3 = unused
			4-7 = coin code
			      0 = nothing
			      1,2,3 = coin switch pressed
			      e = tilt
                 */
                if ((mcu_reportcoin & 0x08) != 0) {
                    return 0xe1;
                    /* tilt */
                }
                if ((mcu_reportcoin & 0x01) != 0) {
                    return 0x11;
                    /* coin A */
                }
                if ((mcu_reportcoin & 0x02) != 0) {
                    return 0x21;
                    /* coin B */
                }
                if ((mcu_reportcoin & 0x04) != 0) {
                    return 0x31;
                    /* coin C */
                }
                return 0x01;
            }
        }
    };

    public static WriteHandlerPtr mcu_chukatai_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /*TODO*///		if (errorlog != null) fprintf (errorlog, "PC %04x (re %04x): write %02x to mcu %04x\n", cpu_get_pc(), cpu_geturnpc(), data, 0xc000 + offset);

            if (offset == 0) {
                if (mcu_command == 0x41) {
                    mcu_credits = (char) ((mcu_credits + data) & 0xff);
                }
            } else {
                /*
			0x93: read number of credits, then joysticks/buttons
			0x03: read service & tilt switches
			0x1f: read coin switches
			0x4f+0x41: add value to number of credits
	
			during initialization, a sequence of 4 bytes sets coin/credit settings
                 */

                if (mcu_initializing != 0) {
                    /* set up coin/credit settings */
                    mcu_coinage[mcu_coinage_init++] = (char) data;
                    if (mcu_coinage_init == 4) {
                        mcu_coinage_init = 0;
                        /* must not happen */
                    }
                }

                if (data == 0x93) {
                    mcu_readcredits = 0;
                    /* reset input port number */
                }

                mcu_command = data;
            }
        }
    };

    public static ReadHandlerPtr mcu_tnzs_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            //char *mcu_startup = "\x5a\xa5\x55";
            char[] mcu_startup = {0x5a, 0xa5, 0x55};

            /*TODO*///		if (errorlog != null) fprintf (errorlog, "PC %04x (re %04x): read mcu %04x\n", cpu_get_pc(), cpu_geturnpc(), 0xc000 + offset);
            if (offset == 0) {
                /* if the mcu has just been reset, return startup code */
                if (mcu_initializing != 0) {
                    mcu_initializing--;
                    return mcu_startup[2 - mcu_initializing];
                }

                switch (mcu_command) {
                    case 0x01:
                        return readinputport(2) ^ 0xff;
                    /* player 1 joystick + buttons */

                    case 0x02:
                        return readinputport(3) ^ 0xff;
                    /* player 2 joystick + buttons */

                    case 0x1a:
                        return readinputport(4) >> 4;

                    case 0x21:
                        return readinputport(4) & 0x0f;

                    case 0x41:
                        return mcu_credits;

                    case 0xa0:
                        /* Read the credit counter */
                        if ((mcu_reportcoin & 0x08) != 0) {
                            mcu_initializing = 3;
                            return 0xee;
                            /* tilt */
                        } else {
                            return mcu_credits;
                        }

                    case 0xa1:
                        /* Read the credit counter or the inputs */
                        if (mcu_readcredits == 0) {
                            mcu_readcredits = 1;
                            if ((mcu_reportcoin & 0x08) != 0) {
                                mcu_initializing = 3;
                                return 0xee;
                                /* tilt */
                                //						return 0x64;	/* theres a reset input somewhere */
                            } else {
                                return mcu_credits;
                            }
                        } /* buttons */ else {
                            return ((readinputport(2) & 0xf0) | (readinputport(3) >> 4)) ^ 0xff;
                        }

                    default:
                        if (errorlog != null) {
                            fprintf(errorlog, "error, unknown mcu command\n");
                        }
                        /* should not happen */
                        return 0xff;
                    //break;
                }
            } else {
                /*
			status bits:
			0 = mcu is ready to send data (read from c000)
			1 = mcu has read data (from c000)
			2 = unused
			3 = unused
			4-7 = coin code
			      0 = nothing
			      1,2,3 = coin switch pressed
			      e = tilt
                 */
                if ((mcu_reportcoin & 0x08) != 0) {
                    return 0xe1;
                    /* tilt */
                }
                if (mcu_type == MCU_TNZS) {
                    if ((mcu_reportcoin & 0x01) != 0) {
                        return 0x31;
                        /* coin 1 (will trigger "coin inserted" sound) */
                    }
                    if ((mcu_reportcoin & 0x02) != 0) {
                        return 0x21;
                        /* coin 2 (will trigger "coin inserted" sound) */
                    }
                    if ((mcu_reportcoin & 0x04) != 0) {
                        return 0x11;
                        /* coin 3 (will NOT trigger "coin inserted" sound) */
                    }
                } else {
                    if ((mcu_reportcoin & 0x01) != 0) {
                        return 0x11;
                        /* coin 1 (will trigger "coin inserted" sound) */
                    }
                    if ((mcu_reportcoin & 0x02) != 0) {
                        return 0x21;
                        /* coin 2 (will trigger "coin inserted" sound) */
                    }
                    if ((mcu_reportcoin & 0x04) != 0) {
                        return 0x31;
                        /* coin 3 (will trigger "coin inserted" sound) */
                    }
                }
                return 0x01;
            }
        }
    };

    public static WriteHandlerPtr mcu_tnzs_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset == 0) {
                /*TODO*///			if (errorlog != 0) fprintf (errorlog, "PC %04x (re %04x): write %02x to mcu %04x\n", cpu_get_pc(), cpu_geturnpc(), data, 0xc000 + offset);
                if (mcu_command == 0x41) {
                    mcu_credits = (char) ((mcu_credits + data) & 0xff);
                }
            } else {
                /*
			0xa0: read number of credits
			0xa1: read number of credits, then buttons
			0x01: read player 1 joystick + buttons
			0x02: read player 2 joystick + buttons
			0x1a: read coin switches
			0x21: read service & tilt switches
			0x4a+0x41: add value to number of credits
			0x84: coin 1 lockout (issued only in test mode)
			0x88: coin 2 lockout (issued only in test mode)
			0x80: release coin lockout (issued only in test mode)
			during initialization, a sequence of 4 bytes sets coin/credit settings
                 */

 /*TODO*///			if (errorlog != null) fprintf (errorlog, "PC %04x (re %04x): write %02x to mcu %04x\n", cpu_get_pc(), cpu_geturnpc(), data, 0xc000 + offset);
                if (mcu_initializing != 0) {
                    /* set up coin/credit settings */
                    mcu_coinage[mcu_coinage_init++] = (char) data;
                    if (mcu_coinage_init == 4) {
                        mcu_coinage_init = 0;
                        /* must not happen */
                    }
                }

                if (data == 0xa1) {
                    mcu_readcredits = 0;
                    /* reset input port number */
                }

                /* Dr Toppel decrements credits differently. So handle it */
                if ((data == 0x09) && (mcu_type == MCU_DRTOPPEL)) {
                    mcu_credits = (char) ((mcu_credits - 1) & 0xff);
                    /* Player 1 start */
                }
                if ((data == 0x18) && (mcu_type == MCU_DRTOPPEL)) {
                    mcu_credits = (char) ((mcu_credits - 2) & 0xff);
                    /* Player 2 start */
                }

                mcu_command = data;
            }
        }
    };

    public static InitDriverHandlerPtr init_extrmatn = new InitDriverHandlerPtr() {
        public void handler() {
            UBytePtr RAM = memory_region(REGION_CPU1);

            mcu_type = MCU_EXTRMATN;

            /* there's code which falls through from the fixed ROM to bank #7, I have to */
 /* copy it there otherwise the CPU bank switching support will not catch it. */
            memcpy(new UBytePtr(RAM, 0x08000), new UBytePtr(RAM, 0x2c000), 0x4000);
        }
    };
    public static InitDriverHandlerPtr init_arkanoi2 = new InitDriverHandlerPtr() {
        public void handler() {
            UBytePtr RAM = memory_region(REGION_CPU1);

            mcu_type = MCU_ARKANOID;

            /* there's code which falls through from the fixed ROM to bank #2, I have to */
 /* copy it there otherwise the CPU bank switching support will not catch it. */
            memcpy(new UBytePtr(RAM, 0x08000), new UBytePtr(RAM, 0x18000), 0x4000);
        }
    };
    public static InitDriverHandlerPtr init_drtoppel = new InitDriverHandlerPtr() {
        public void handler() {
            UBytePtr RAM = memory_region(REGION_CPU1);

            mcu_type = MCU_DRTOPPEL;

            /* there's code which falls through from the fixed ROM to bank #0, I have to */
 /* copy it there otherwise the CPU bank switching support will not catch it. */
            memcpy(new UBytePtr(RAM, 0x08000), new UBytePtr(RAM, 0x18000), 0x4000);
        }
    };
    public static InitDriverHandlerPtr init_chukatai = new InitDriverHandlerPtr() {
        public void handler() {
            UBytePtr RAM = memory_region(REGION_CPU1);

            mcu_type = MCU_CHUKATAI;

            /* there's code which falls through from the fixed ROM to bank #0, I have to */
 /* copy it there otherwise the CPU bank switching support will not catch it. */
            memcpy(new UBytePtr(RAM, 0x08000), new UBytePtr(RAM, 0x18000), 0x4000);
        }
    };
    public static InitDriverHandlerPtr init_tnzs = new InitDriverHandlerPtr() {
        public void handler() {
            UBytePtr RAM = memory_region(REGION_CPU1);
            mcu_type = MCU_TNZS;

            /* there's code which falls through from the fixed ROM to bank #0, I have to */
 /* copy it there otherwise the CPU bank switching support will not catch it. */
            memcpy(new UBytePtr(RAM, 0x08000), new UBytePtr(RAM, 0x18000), 0x4000);
        }
    };
    public static InitDriverHandlerPtr init_insectx = new InitDriverHandlerPtr() {
        public void handler() {
            mcu_type = MCU_NONE;

            /* this game has no mcu, replace the handler with plain input port handlers */
            install_mem_read_handler(1, 0xc000, 0xc000, input_port_2_r);
            install_mem_read_handler(1, 0xc001, 0xc001, input_port_3_r);
            install_mem_read_handler(1, 0xc002, 0xc002, input_port_4_r);
        }
    };
    public static InitDriverHandlerPtr init_kageki = new InitDriverHandlerPtr() {
        public void handler() {
            /* this game has no mcu */
            mcu_type = MCU_NONE;
        }
    };

    public static ReadHandlerPtr tnzs_mcu_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            switch (mcu_type) {
                case MCU_ARKANOID:
                    return mcu_arkanoi2_r.handler(offset);
                //break;
                case MCU_CHUKATAI:
                    return mcu_chukatai_r.handler(offset);
                //break;
                case MCU_EXTRMATN:
                case MCU_DRTOPPEL:
                case MCU_TNZS:
                default:
                    return mcu_tnzs_r.handler(offset);
                //break;
            }
        }
    };

    public static WriteHandlerPtr tnzs_mcu_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            switch (mcu_type) {
                case MCU_ARKANOID:
                    mcu_arkanoi2_w.handler(offset, data);
                    break;
                case MCU_CHUKATAI:
                    mcu_chukatai_w.handler(offset, data);
                    break;
                case MCU_EXTRMATN:
                case MCU_DRTOPPEL:
                case MCU_TNZS:
                default:
                    mcu_tnzs_w.handler(offset, data);
                    break;
            }
        }
    };

    public static InterruptHandlerPtr tnzs_interrupt = new InterruptHandlerPtr() {
        public int handler() {
            int coin;

            switch (mcu_type) {
                case MCU_ARKANOID:
                    coin = ((readinputport(5) & 0xf000) ^ 0xd000) >> 12;
                    coin = (coin & 0x08) | ((coin & 0x03) << 1) | ((coin & 0x04) >> 2);
                    mcu_handle_coins(coin);
                    break;

                case MCU_EXTRMATN:
                case MCU_DRTOPPEL:
                    coin = (((readinputport(4) & 0x30) >> 4) | ((readinputport(4) & 0x03) << 2)) ^ 0x0c;
                    mcu_handle_coins(coin);
                    break;

                case MCU_CHUKATAI:
                case MCU_TNZS:
                    coin = (((readinputport(4) & 0x30) >> 4) | ((readinputport(4) & 0x03) << 2)) ^ 0x0f;
                    mcu_handle_coins(coin);
                    break;

                case MCU_NONE:
                default:
                    break;
            }

            return 0;
        }
    };

    public static InitMachineHandlerPtr tnzs_init_machine = new InitMachineHandlerPtr() {
        public void handler() {
            /* initialize the mcu simulation */
            mcu_reset();

            /* preset the banks */
            {
                UBytePtr RAM;

                RAM = memory_region(REGION_CPU1);
                cpu_setbank(1, new UBytePtr(RAM, 0x18000));

                RAM = memory_region(REGION_CPU2);
                cpu_setbank(2, new UBytePtr(RAM, 0x10000));
            }
        }
    };

    public static ReadHandlerPtr tnzs_workram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* Location $EF10 workaround required to stop TNZS getting */
 /* caught in and endless loop due to shared ram sync probs */

            if ((offset == 0xf10) && (mcu_type == MCU_TNZS)) {
                int tnzs_cpu0_pc;

                tnzs_cpu0_pc = cpu_get_pc();
                switch (tnzs_cpu0_pc) {
                    case 0xc66:
                    /* tnzs */
                    case 0xc64:
                    /* tnzsb */
                    case 0xab8:
                        /* tnzs2 */
                        tnzs_workram.write(offset, (tnzs_workram_backup & 0xff));
                        return tnzs_workram_backup;
                    //break;
                    default:
                        break;
                }
            }
            return tnzs_workram.read(offset);
        }
    };

    public static ReadHandlerPtr tnzs_workram_sub_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return tnzs_workram.read(offset);
        }
    };

    public static WriteHandlerPtr tnzs_workram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* Location $EF10 workaround required to stop TNZS getting */
 /* caught in and endless loop due to shared ram sync probs */

            tnzs_workram_backup = -1;

            if ((offset == 0xf10) && (mcu_type == MCU_TNZS)) {
                int tnzs_cpu0_pc;

                tnzs_cpu0_pc = cpu_get_pc();
                switch (tnzs_cpu0_pc) {
                    case 0xab5:
                        /* tnzs2 */
                        if (cpu_getpreviouspc() == 0xab4) {
                            break;
                            /* unfortunantly tnzsb is true here too, so stop it */
                        }
                    case 0xc63:
                    /* tnzs */
                    case 0xc61:
                        /* tnzsb */
                        tnzs_workram_backup = data;
                        break;
                    default:
                        break;
                }
            }
            if (tnzs_workram_backup == -1) {
                tnzs_workram.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr tnzs_workram_sub_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            tnzs_workram.write(offset, data);
        }
    };

    public static WriteHandlerPtr tnzs_bankswitch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UBytePtr RAM = memory_region(REGION_CPU1);

            /* bit 4 resets the second CPU */
            if ((data & 0x10) != 0) {
                cpu_set_reset_line(1, CLEAR_LINE);
            } else {
                cpu_set_reset_line(1, ASSERT_LINE);
            }

            /* bits 0-2 select RAM/ROM bank */
            //	if (errorlog != 0) fprintf(errorlog, "PC %04x: writing %02x to bankswitch\n", cpu_get_pc(),data);
            cpu_setbank(1, new UBytePtr(RAM, 0x10000 + 0x4000 * (data & 0x07)));
        }
    };

    public static WriteHandlerPtr tnzs_bankswitch1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UBytePtr RAM = memory_region(REGION_CPU2);

            //	if (errorlog != 0) fprintf(errorlog, "PC %04x: writing %02x to bankswitch 1\n", cpu_get_pc(),data);
            /* bit 2 resets the mcu */
            if ((data & 0x04) != 0) {
                mcu_reset();
            }

            /* bits 0-1 select ROM bank */
            cpu_setbank(2, new UBytePtr(RAM, 0x10000 + 0x2000 * (data & 3)));
        }
    };
}
