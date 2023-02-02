/*
 * ported to v0.36
 */
package arcadeflex.v036.machine;

public class slapstic {

    /**
     * ***********************************
     *
     * Structure of slapstic params
     *
     ************************************
     */
    public static class slapstic_params {

        public int reset;
        public int bank0, bank1, bank2, bank3;
        public int disable;
        public int ignore;
        public int senable;
        public int sbank0, sbank1, sbank2, sbank3;

        public slapstic_params(int reset, int bank0, int bank1, int bank2, int bank3, int disable, int ignore, int senable, int sbank0, int sbank1, int sbank2, int sbank3) {
            this.reset = reset;
            this.bank0 = bank0;
            this.bank1 = bank1;
            this.bank2 = bank2;
            this.bank3 = bank3;
            this.disable = disable;
            this.ignore = ignore;
            this.senable = senable;
            this.sbank0 = sbank0;
            this.sbank1 = sbank1;
            this.sbank2 = sbank2;
            this.sbank3 = sbank3;
        }
    };

    /**
     * ***********************************
     *
     * Constants
     *
     ************************************
     */
    public static final int DISABLE_MASK = 0x3ff0;
    public static final int IGNORE_MASK = 0x007f;
    public static final int UNKNOWN = 0xffff;

    public static enum state_type {
        ENABLED, DISABLED, IGNORE, SPECIAL
    };

    /*TODO*///	#define LOG_SLAPSTIC 0
    /**
     * ***********************************
     *
     * The master table
     *
     ************************************
     */
    static slapstic_params slapstic_table[]
            = {
                /* 137412-101 ESB/Tetris */
                new slapstic_params(0x0000, 0x0080, 0x0090, 0x00a0, 0x00b0, 0x1540, UNKNOWN, 0x1dfe, 0x1b5c, 0x1b5d, 0x1b5e, 0x1b5f),
                /* 137412-102 ???? */
                new slapstic_params(0x0000, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN),
                /* 137412-103 Marble Madness */
                new slapstic_params(0x0000, 0x0040, 0x0050, 0x0060, 0x0070, 0x34c0, 0x002d, 0x3d14, 0x3d24, 0x3d25, 0x3d26, 0x3d27),
                /* 137412-104 Gauntlet */
                /*	{ 0x0000, 0x0020, 0x0028, 0x0030, 0x0038, 0x3d90, 0x0069, 0x3735, 0x3764, 0x3765, 0x3766, 0x3767 },*/
                /* EC990621 Gauntlet fix */
                new slapstic_params(0x0000, 0x0020, 0x0028, 0x0030, 0x0038, 0x3da0, 0x0069, 0x3735, 0x3764, 0x3765, 0x3766, 0x3767),
                /* EC990621 end of Gauntlet fix */
                /* 137412-105 Indiana Jones/Paperboy */
                new slapstic_params(0x0000, 0x0010, 0x0014, 0x0018, 0x001c, 0x35b0, 0x003d, 0x0092, 0x00a4, 0x00a5, 0x00a6, 0x00a7),
                /* 137412-106 Gauntlet II */
                /*	{ 0x0000, 0x0008, 0x000a, 0x000c, 0x000e, 0x3da0, 0x002b, 0x0052, 0x0064, 0x0065, 0x0066, 0x0067 },*/
                /* NS990620 Gauntlet II fix */
                new slapstic_params(0x0000, 0x0008, 0x000a, 0x000c, 0x000e, 0x3db0, 0x002b, 0x0052, 0x0064, 0x0065, 0x0066, 0x0067),
                /* NS990620 end of Gauntlet II fix */
                /* 137412-107 Peter Packrat/Xybots/2-player Gauntlet/720 Degrees */
                /*	{ 0x0000, 0x0018, 0x001a, 0x001c, 0x001e, 0x00a0, 0x006b, 0x3d52, 0x3d64, 0x3d65, 0x3d66, 0x3d67 },*/
                /* NS990622 Xybots fix */
                new slapstic_params(0x0000, 0x0018, 0x001a, 0x001c, 0x001e, 0x00b0, 0x006b, 0x3d52, 0x3d64, 0x3d65, 0x3d66, 0x3d67),
                /* NS990622 end of Xybots fix */
                /* 137412-108 Road Runner/Super Sprint */
                new slapstic_params(0x0000, 0x0028, 0x002a, 0x002c, 0x002e, 0x0060, 0x001f, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN),
                /* 137412-109 Championship Sprint */
                new slapstic_params(0x0000, 0x0008, 0x000a, 0x000c, 0x000e, 0x3da0, 0x002b, 0x0052, 0x0064, 0x0065, 0x0066, 0x0067),
                /* 137412-110 Road Blasters/APB */
                new slapstic_params(0x0000, 0x0040, 0x0050, 0x0060, 0x0070, 0x34c0, 0x002d, 0x3d14, 0x3d24, 0x3d25, 0x3d26, 0x3d27),
                /* 137412-111 Pit Fighter */
                new slapstic_params(0x0000, 0x0042, 0x0052, 0x0062, 0x0072, UNKNOWN, 0x000a, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN),
                /* 137412-112 ???? */
                new slapstic_params(0x0000, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN),
                /* 137412-113 ???? */
                new slapstic_params(0x0000, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN),
                /* 137412-114 ???? */
                new slapstic_params(0x0000, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN),
                /* 137412-115 ???? */
                new slapstic_params(0x0000, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN),
                /* 137412-116 Hydra/Cyberball 2072 Tournament */
                new slapstic_params(0x0000, 0x0044, 0x004c, 0x0054, 0x005c, UNKNOWN, 0x0069, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN),
                /* 137412-117 Race Drivin' */
                new slapstic_params(0x0000, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN),
                /* 137412-118 Vindicators II/Rampart */
                /*	{ 0x0000, 0x0014, 0x0034, 0x0054, 0x0074,UNKNOWN, 0x0002, 0x1950, 0x1958, 0x1960, 0x1968, 0x1970 },*/
                /* EC990622 Rampart fix */
                new slapstic_params(0x0000, 0x0014, 0x0034, 0x0054, 0x0074, 0x30e0, 0x0002, 0x1958, 0x1959, 0x195a, 0x195b, 0x195c), /* EC990622 end of Rampart fix */};

    /**
     * ***********************************
     *
     * Statics
     *
     ************************************
     */
    static slapstic_params slapstic;

    public static state_type state;
    static int next_bank;
    static int extra_bank;
    static int current_bank;
    static int version;

    /*TODO*///	#if LOG_SLAPSTIC
/*TODO*///		static void slapstic_log(offs_t offset);
/*TODO*///		static FILE *slapsticlog;
/*TODO*///	#else
/*TODO*///		#define slapstic_log(o)
/*TODO*///	#endif
    /**
     * ***********************************
     *
     * Initialization
     *
     ************************************
     */
    public static void slapstic_init(int chip) {
        /* only a small number of chips are known to exist */
        if (chip < 101 || chip > 118) {
            return;
        }

        /* set up a pointer to the parameters */
        version = chip;
        slapstic = slapstic_table[chip - 101];

        /* reset the chip */
        state = state_type.ENABLED;
        next_bank = extra_bank = -1;

        /* the 111 and later chips seem to reset to bank 0 */
        if (chip < 111) {
            current_bank = 3;
        } else {
            current_bank = 0;
        }
    }

    public static void slapstic_reset() {
        slapstic_init(version);
    }

    /**
     * ***********************************
     *
     * Returns active bank without tweaking
     *
     ************************************
     */
    public static int slapstic_bank() {
        return current_bank;
    }

    /**
     * ***********************************
     *
     * Call this before every access
     *
     ************************************
     */
    public static int slapstic_tweak(int offset) {
        /* switch banks now if one is pending */
        if (next_bank != -1) {
            current_bank = next_bank;
            next_bank = -1;
            extra_bank = -1;
        }

        /* state machine */
        switch (state) {
            /* ENABLED state: the chip has been activated and is ready for a bankswitch */
            case ENABLED:
                if ((offset & DISABLE_MASK) == slapstic.disable) {
                    state = state_type.DISABLED;
                    /* NS990620 Gauntlet II fix */
                    if (extra_bank != -1) {
                        next_bank = extra_bank;
                    }
                    /* NS990620 end of Gauntlet II fix */
                } else if ((offset & IGNORE_MASK) == slapstic.ignore) {
                    state = state_type.IGNORE;
                } else if (offset == slapstic.bank0) {
                    state = state_type.DISABLED;
                    if (extra_bank == -1) {
                        next_bank = 0;
                    } else {
                        next_bank = extra_bank;
                    }
                } else if (offset == slapstic.bank1) {
                    state = state_type.DISABLED;
                    if (extra_bank == -1) {
                        next_bank = 1;
                    } else {
                        next_bank = extra_bank;
                    }
                } else if (offset == slapstic.bank2) {
                    state = state_type.DISABLED;
                    if (extra_bank == -1) {
                        next_bank = 2;
                    } else {
                        next_bank = extra_bank;
                    }
                } else if (offset == slapstic.bank3) {
                    state = state_type.DISABLED;
                    if (extra_bank == -1) {
                        next_bank = 3;
                    } else {
                        next_bank = extra_bank;
                    }
                } else if (offset == slapstic.reset) {
                    next_bank = -1;
                    extra_bank = -1;
                } /* This is the transition which has */ /* not been verified on the HW yet */ else if (offset == slapstic.senable) {
                    state = state_type.SPECIAL;
                }
                break;

            /* DISABLED state: everything is ignored except a reset */
            case DISABLED:
                if (offset == slapstic.reset) {
                    state = state_type.ENABLED;
                    next_bank = -1;
                    extra_bank = -1;
                }
                break;

            /* IGNORE state: next access is interpreted differently */
            case IGNORE:
                if (offset == slapstic.senable) {
                    state = state_type.SPECIAL;
                } else {
                    state = state_type.ENABLED;
                }
                break;

            /* SPECIAL state: the special alternate bank switch override method is being used */
            case SPECIAL:
                if (offset == slapstic.sbank0) {
                    state = state_type.ENABLED;
                    extra_bank = 0;
                } else if (offset == slapstic.sbank1) {
                    state = state_type.ENABLED;
                    extra_bank = 1;
                } else if (offset == slapstic.sbank2) {
                    state = state_type.ENABLED;
                    extra_bank = 2;
                } else if (offset == slapstic.sbank3) {
                    state = state_type.ENABLED;
                    extra_bank = 3;
                } else if (offset == slapstic.reset) {
                    state = state_type.ENABLED;
                    next_bank = -1;
                    extra_bank = -1;
                } else {
                    state = state_type.ENABLED;
                }
                break;
        }

        /* log this access */
        slapstic_log(offset);

        /* return the active bank */
        return current_bank;
    }

    /**
     * ***********************************
     *
     * Debugging
     *
     ************************************
     */
    /*TODO*///	#if LOG_SLAPSTIC
    static void slapstic_log(int offset) {
        /*TODO*///		if (!slapsticlog)
/*TODO*///			slapsticlog = fopen("slapstic.log", "w");
/*TODO*///		if (slapsticlog != 0)
/*TODO*///		{
/*TODO*///			fprintf(slapsticlog, "%06X: %04X B=%d ", cpu_getpreviouspc(), offset, current_bank);
/*TODO*///			switch (state)
/*TODO*///			{
/*TODO*///				case ENABLED:
/*TODO*///					fprintf(slapsticlog, "ENABLED\n");
/*TODO*///					break;
/*TODO*///				case DISABLED:
/*TODO*///					fprintf(slapsticlog, "DISABLED\n");
/*TODO*///					break;
/*TODO*///				case SPECIAL:
/*TODO*///					fprintf(slapsticlog, "SPECIAL\n");
/*TODO*///					break;
/*TODO*///				case IGNORE:
/*TODO*///					fprintf(slapsticlog, "IGNORE\n");
/*TODO*///					break;
/*TODO*///			}
/*TODO*///		}
    }
    /*TODO*///	#endif
}
