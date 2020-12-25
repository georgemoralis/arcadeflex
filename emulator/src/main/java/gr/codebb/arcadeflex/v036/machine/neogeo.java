/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */
package gr.codebb.arcadeflex.v036.machine;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.drivers.neogeo.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.install_mem_read_handler;
import static gr.codebb.arcadeflex.v037b7.mame.memory.install_mem_write_handler;
import static gr.codebb.arcadeflex.common.libc.cstring.*;

public class neogeo {

    //static unsigned char *memcard;
    public static UBytePtr neogeo_ram;
    public static UBytePtr neogeo_sram = new UBytePtr();

    static int sram_locked;
    static int sram_protection_hack;

    /**
     * *************** MEMCARD GLOBAL VARIABLES *****************
     */
    static int mcd_action = 0;
    static int mcd_number = 0;
    public static int memcard_status = 0;	/* 1=Inserted 0=No card */

    static int memcard_number = 0;	/* 000...999, -1=None */

    static int memcard_manager = 0;	/* 0=Normal boot 1=Call memcard manager */

    public static UBytePtr neogeo_memcard;	/* Pointer to 2kb RAM zone */


    /* This function is called on every reset */
    public static InitMachinePtr neogeo_init_machine = new InitMachinePtr() {
        public void handler() {
            int src, res;
            /*TODO*///		time_t		ltime;
/*TODO*///		struct tm		*today;

            /* Reset variables & RAM */
            memset(neogeo_ram, 0, 0x10000);

            /* Set up machine country */
            src = readinputport(5);
            res = src & 0x3;

            /* Console/arcade mode */
            if ((src & 0x04) != 0) {
                res |= 0x8000;
            }

            /* write the ID in the system BIOS ROM */
            memory_region(REGION_USER1).WRITE_WORD(0x0400, res);

            if (memcard_manager == 1) {
                memcard_manager = 0;
                memory_region(REGION_USER1).WRITE_WORD(0x11b1a, 0x500a);
            } else {
                memory_region(REGION_USER1).WRITE_WORD(0x11b1a, 0x1b6a);
            }

            /*TODO*///		time(&ltime);
/*TODO*///		today = localtime(&ltime);
            /*TODO*///		seconds = ((today.tm_sec/10)<<4) + (today.tm_sec%10);
/*TODO*///		minutes = ((today.tm_min/10)<<4) + (today.tm_min%10);
/*TODO*///		hours = ((today.tm_hour/10)<<4) + (today.tm_hour%10);
/*TODO*///		days = ((today.tm_mday/10)<<4) + (today.tm_mday%10);
/*TODO*///		month = (today.tm_mon + 1);
/*TODO*///		year = ((today.tm_year/10)<<4) + (today.tm_year%10);
/*TODO*///		weekday = today.tm_wday;
        }
    };

    /* This function is only called once per game. */
    public static InitDriverPtr init_neogeo = new InitDriverPtr() {
        public void handler() {
            UBytePtr RAM = memory_region(REGION_CPU1);
		//YM2610interface neogeo_ym2610_interface;

            if (memory_region(REGION_SOUND2) != null) {
                if (errorlog != null) {
                    fprintf(errorlog, "using memory region %d for Delta T samples\n", REGION_SOUND2);
                }
                neogeo_ym2610_interface.pcmromb[0] = REGION_SOUND2;
            } else {
                if (errorlog != null) {
                    fprintf(errorlog, "using memory region %d for Delta T samples\n", REGION_SOUND1);
                }
                neogeo_ym2610_interface.pcmromb[0] = REGION_SOUND1;
            }

            /* Allocate ram banks */
            neogeo_ram = new UBytePtr(0x10000);
            cpu_setbank(1, neogeo_ram);

            /* Set the biosbank */
            cpu_setbank(3, memory_region(REGION_USER1));

            /* Set the 2nd ROM bank */
            RAM = memory_region(REGION_CPU1);
            if (memory_region_length(REGION_CPU1) > 0x100000) {
                cpu_setbank(4, new UBytePtr(RAM, 0x100000));
            } else {
                cpu_setbank(4, new UBytePtr(RAM, 0));
            }

            /* Set the sound CPU ROM banks */
            RAM = memory_region(REGION_CPU2);
            cpu_setbank(5, new UBytePtr(RAM, 0x08000));
            cpu_setbank(6, new UBytePtr(RAM, 0x0c000));
            cpu_setbank(7, new UBytePtr(RAM, 0x0e000));
            cpu_setbank(8, new UBytePtr(RAM, 0x0f000));

            /* Allocate and point to the memcard - bank 5 */
            neogeo_memcard = new UBytePtr(0x800);//calloc (0x800, 1);
            memcard_status = 0;
            memcard_number = 0;

            RAM = memory_region(REGION_USER1);

            if (RAM.READ_WORD(0x11b00) == 0x4eba) {
                /* standard bios */
                neogeo_has_trackball = 0;

                /* Remove memory check for now */
                RAM.WRITE_WORD(0x11b00, 0x4e71);
                RAM.WRITE_WORD(0x11b02, 0x4e71);
                RAM.WRITE_WORD(0x11b16, 0x4ef9);
                RAM.WRITE_WORD(0x11b18, 0x00c1);
                RAM.WRITE_WORD(0x11b1a, 0x1b6a);

                /* Patch bios rom, for Calendar errors */
                RAM.WRITE_WORD(0x11c14, 0x4e71);
                RAM.WRITE_WORD(0x11c16, 0x4e71);
                RAM.WRITE_WORD(0x11c1c, 0x4e71);
                RAM.WRITE_WORD(0x11c1e, 0x4e71);

                /* Rom internal checksum fails for now.. */
                RAM.WRITE_WORD(0x11c62, 0x4e71);
                RAM.WRITE_WORD(0x11c64, 0x4e71);
            } else {
                /* special bios with trackball support */
                neogeo_has_trackball = 1;

                /* TODO: check the memcard manager patch in neogeo_init_machine(), */
                /* it probably has to be moved as well */
                /* Remove memory check for now */
                RAM.WRITE_WORD(0x10c2a, 0x4e71);
                RAM.WRITE_WORD(0x10c2c, 0x4e71);
                RAM.WRITE_WORD(0x10c40, 0x4ef9);
                RAM.WRITE_WORD(0x10c42, 0x00c1);
                RAM.WRITE_WORD(0x10c44, 0x0c94);

                /* Patch bios rom, for Calendar errors */
                RAM.WRITE_WORD(0x10d3e, 0x4e71);
                RAM.WRITE_WORD(0x10d40, 0x4e71);
                RAM.WRITE_WORD(0x10d46, 0x4e71);
                RAM.WRITE_WORD(0x10d48, 0x4e71);

                /* Rom internal checksum fails for now.. */
                RAM.WRITE_WORD(0x10d8c, 0x4e71);
                RAM.WRITE_WORD(0x10d8e, 0x4e71);
            }

            /* Install custom memory handlers */
            neogeo_custom_memory();

            /* Flag how to handle IRQ2 raster effect */
            /* 0=write 0,2   1=write2,0 */
            if (strcmp(Machine.gamedrv.name, "neocup98") == 0
                    || strcmp(Machine.gamedrv.name, "ssideki3") == 0
                    || strcmp(Machine.gamedrv.name, "ssideki4") == 0) {
                neogeo_irq2type = 1;
            }
        }
    };

    /**
     * ***************************************************************************
     */
    public static ReadHandlerPtr bios_cycle_skip_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            cpu_spinuntil_int();
            return 0;
        }
    };

    /**
     * ***************************************************************************
     */
    /* Routines to speed up the main processor 				      */
    /**
     * ***************************************************************************
     */
    public static ReadHandlerPtr puzzledp_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x12f2) {
                cpu_spinuntil_int();
                return 1;
            }
            return neogeo_ram.READ_WORD(0x0000);
        }
    };
    public static ReadHandlerPtr samsho4_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0xaffc) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x830c);
        }
    };
    public static ReadHandlerPtr karnovr_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x5b56) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x3466);
        }
    };
    public static ReadHandlerPtr wjammers_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x1362e) {
                cpu_spinuntil_int();
                return neogeo_ram.READ_WORD(0x5a) & 0x7fff;
            }
            return neogeo_ram.READ_WORD(0x005a);
        }
    };
    public static ReadHandlerPtr strhoops_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x029a) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x1200);
        }
    };
    //NEO_CYCLE_R(magdrop3,0xa378,READ_WORD(&neogeo_ram[0x60])&0x7fff,READ_WORD(&neogeo_ram[0x0060]))
    public static ReadHandlerPtr neobombe_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x09f2) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x448c);
        }
    };
    public static ReadHandlerPtr trally_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x1295c) {
                cpu_spinuntil_int();
                return neogeo_ram.READ_WORD(0x206) - 1;
            }
            return neogeo_ram.READ_WORD(0x0206);
        }
    };
        //NEO_CYCLE_R(joyjoy,  0x122c,0xffff,							READ_WORD(&neogeo_ram[0x0554]))

    public static ReadHandlerPtr blazstar_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x3b62) {
                if (neogeo_ram.READ_WORD(0x1000) == 0) {
                    cpu_spinuntil_int();
                }
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x1000);
        }
    };
    //NEO_CYCLE_R(ridhero, 0xedb0,0,								READ_WORD(&neogeo_ram[0x00ca]))
    public static ReadHandlerPtr cyberlip_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x2218) {
                cpu_spinuntil_int();
                return 0x0f0f;
            }
            return neogeo_ram.READ_WORD(0x7bb4);
        }
    };
    public static ReadHandlerPtr lbowling_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x37b0) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x0098);
        }
    };
    public static ReadHandlerPtr superspy_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x07ca) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x108c);
        }
    };
    public static ReadHandlerPtr ttbb_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x0a58) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x000e);
        }
    };
    public static ReadHandlerPtr alpham2_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x076e) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0xe2fe);
        }
    };
    public static ReadHandlerPtr eightman_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x12fa) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x046e);
        }
    };
    public static ReadHandlerPtr roboarmy_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x08e8) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x4010);
        }
    };
    public static ReadHandlerPtr fatfury1_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x133c) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x4282);
        }
    };
    public static ReadHandlerPtr burningf_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x0736) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x000e);
        }
    };
    public static ReadHandlerPtr bstars_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x133c) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x000a);
        }
    };
    public static ReadHandlerPtr kotm_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x1284) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x0020);
        }
    };
    public static ReadHandlerPtr gpilots_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x0474) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0xa682);
        }
    };
    public static ReadHandlerPtr lresort_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x256a) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x4102);
        }
    };
    public static ReadHandlerPtr fbfrenzy_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x07dc) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x0020);
        }
    };
    public static ReadHandlerPtr socbrawl_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0xa8dc) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0xb20c);
        }
    };
    public static ReadHandlerPtr mutnat_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x1456) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x1042);
        }
    };
    public static ReadHandlerPtr aof_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x6798) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x8100);
        }
    };
    //NEO_CYCLE_R(countb,  0x16a2,0,								READ_WORD(&neogeo_ram[0x8002))
    public static ReadHandlerPtr ncombat_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0xcb3e) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x0206);
        }
    };
    public static ReadHandlerPtr sengoku_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x12f4) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x0088);
        }
    };
    //NEO_CYCLE_R(ncommand,0x11b44,0,								neogeo_ram.READ_WORD(0x8206))
    public static ReadHandlerPtr wh1_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0xf62d4) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x8206);
        }
    };
    public static ReadHandlerPtr androdun_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x26d6) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x0080);
        }
    };
    public static ReadHandlerPtr bjourney_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0xe8aa) {
                cpu_spinuntil_int();
                return neogeo_ram.READ_WORD(0x206) + 1;
            }
            return neogeo_ram.READ_WORD(0x0206);
        }
    };
    public static ReadHandlerPtr maglord_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0xb16a) {
                cpu_spinuntil_int();
                return neogeo_ram.READ_WORD(0x206) + 1;
            }
            return neogeo_ram.READ_WORD(0x0206);
        }
    };
    //NEO_CYCLE_R(janshin, 0x06a0,0,								neogeo_ram.READ_WORD(0x0026))
    public static ReadHandlerPtr pulstar_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x2052) {
                if (neogeo_ram.READ_WORD(0x1000) == 0) {
                    cpu_spinuntil_int();
                }
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x1000);
        }
    };
    //NEO_CYCLE_R(mslug   ,0x200a,0xffff,							neogeo_ram.READ_WORD(0x6ed8))
    public static ReadHandlerPtr neodrift_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x0b76) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x0424);
        }
    };
    public static ReadHandlerPtr spinmast_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x00f6) {
                cpu_spinuntil_int();
                return neogeo_ram.READ_WORD(0xf0) + 1;
            }
            return neogeo_ram.READ_WORD(0x00f0);
        }
    };
    public static ReadHandlerPtr sonicwi2_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x1e6c8) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0xe5b6);
        }
    };
    public static ReadHandlerPtr sonicwi3_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x20bac) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0xea2e);
        }
    };
    public static ReadHandlerPtr goalx3_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x5298) {
                cpu_spinuntil_int();
                return neogeo_ram.READ_WORD(0x6) + 1;
            }
            return neogeo_ram.READ_WORD(0x0006);
        }
    };
    //NEO_CYCLE_R(turfmast,0xd5a8,0xffff,							neogeo_ram.READ_WORD(0x2e54))
    public static ReadHandlerPtr kabukikl_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x10b0) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x428a);
        }
    };
    public static ReadHandlerPtr panicbom_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x3ee6) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x009c);
        }
    };
    public static ReadHandlerPtr wh2_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x2063fc) {
                cpu_spinuntil_int();
                return neogeo_ram.READ_WORD(0x8206) + 1;
            }
            return neogeo_ram.READ_WORD(0x8206);
        }
    };
    public static ReadHandlerPtr wh2j_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x109f4) {
                cpu_spinuntil_int();
                return neogeo_ram.READ_WORD(0x8206) + 1;
            }
            return neogeo_ram.READ_WORD(0x8206);
        }
    };
    public static ReadHandlerPtr aodk_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0xea62) {
                cpu_spinuntil_int();
                return neogeo_ram.READ_WORD(0x8206) + 1;
            }
            return neogeo_ram.READ_WORD(0x8206);
        }
    };
    public static ReadHandlerPtr whp_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0xeace) {
                cpu_spinuntil_int();
                return neogeo_ram.READ_WORD(0x8206) + 1;
            }
            return neogeo_ram.READ_WORD(0x8206);
        }
    };
    public static ReadHandlerPtr overtop_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x1736) {
                cpu_spinuntil_int();
                return neogeo_ram.READ_WORD(0x8202) + 1;
            }
            return neogeo_ram.READ_WORD(0x8202);
        }
    };
    public static ReadHandlerPtr twinspri_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x492e) {
                cpu_spinuntil_int();
                return neogeo_ram.READ_WORD(0x8206) + 1;
            }
            return neogeo_ram.READ_WORD(0x8206);
        }
    };
    public static ReadHandlerPtr stakwin_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x0596) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x0b92);
        }
    };
    public static ReadHandlerPtr shocktro_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0xdd28) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x8344);
        }
    };
    public static ReadHandlerPtr tws96_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x17f4) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x010e);
        }
    };
	//static public static ReadHandlerPtr zedblade_cycle_r = new ReadHandlerPtr() { public int handler(int offset)
    //{
    //	int pc=cpu_get_pc();
    //	if (pc==0xa2fa || pc==0xa2a0 || pc==0xa2ce || pc==0xa396 || pc==0xa3fa) {cpu_spinuntil_int(); return 0;}
    //	return READ_WORD(&neogeo_ram[0x9004]);
    //} };
    //NEO_CYCLE_R(doubledr,0x3574,0,								READ_WORD(&neogeo_ram[0x1c30]))
    public static ReadHandlerPtr galaxyfg_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x09ea) {
                cpu_spinuntil_int();
                return neogeo_ram.READ_WORD(0x1858) + 1;
            }
            return neogeo_ram.READ_WORD(0x1858);
        }
    };
    public static ReadHandlerPtr wakuwak7_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x1a3c) {
                cpu_spinuntil_int();
                return neogeo_ram.READ_WORD(0x0bd4) + 1;
            }
            return neogeo_ram.READ_WORD(0x0bd4);
        }
    };
    public static ReadHandlerPtr mahretsu_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int pc = cpu_get_pc();
            if (pc == 0x1580 || pc == 0xf3ba) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x13b2);
        }
    };
    public static ReadHandlerPtr nam1975_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x0a1c) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x12e0);
        }
    };
    public static ReadHandlerPtr tpgolf_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x105c) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x00a4);
        }
    };
    public static ReadHandlerPtr legendos_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x1864) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x0002);
        }
    };
    //NEO_CYCLE_R(viewpoin,0x0c16,0,								neogeo_ram.READ_WORD(0x1216))
    public static ReadHandlerPtr fatfury2_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x10ea) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x418c);
        }
    };
    public static ReadHandlerPtr bstars2_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x7e30) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x001c);
        }
    };
    public static ReadHandlerPtr ssideki_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x20b0) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x8c84);
        }
    };
    public static ReadHandlerPtr kotm2_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x045a) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x1000);
        }
    };
    public static ReadHandlerPtr samsho_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int pc = cpu_get_pc();
            if (pc == 0x3580 || pc == 0x0f84) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x0a76);
        }
    };
    public static ReadHandlerPtr fatfursp_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x10da) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x418c);
        }
    };
    public static ReadHandlerPtr fatfury3_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x9c50) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x418c);
        }
    };
    public static ReadHandlerPtr tophuntr_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x0ce0) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x008e);
        }
    };
    public static ReadHandlerPtr savagere_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x056e) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x8404);
        }
    };
    public static ReadHandlerPtr aof2_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x8c74) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x8280);
        }
    };
    //NEO_CYCLE_R(ssideki2,0x7850,0xffff,							neogeo_ram.READ_WORD(0x4292))
    public static ReadHandlerPtr samsho2_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x1432) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x0a30);
        }
    };
    public static ReadHandlerPtr samsho3_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x0858) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x8408);
        }
    };
    public static ReadHandlerPtr kof95_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x39474) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0xa784);
        }
    };
    public static ReadHandlerPtr rbff1_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x80a2) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x418c);
        }
    };
    //NEO_CYCLE_R(aof3,    0x15d6,0,								neogeo_ram.READ_WORD(0x4ee8))
    public static ReadHandlerPtr ninjamas_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x2436) {
                cpu_spinuntil_int();
                return neogeo_ram.READ_WORD(0x8206) + 1;
            }
            return neogeo_ram.READ_WORD(0x8206);
        }
    };
    public static ReadHandlerPtr kof96_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x8fc4) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0xa782);
        }
    };
    public static ReadHandlerPtr rbffspec_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x8704) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x418c);
        }
    };
    public static ReadHandlerPtr kizuna_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x0840) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x8808);
        }
    };
    public static ReadHandlerPtr kof97_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x9c54) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0xa784);
        }
    };
    //NEO_CYCLE_R(mslug2,  0x1656,0xffff,						neogeo_ram.READ_WORD(0x008c))
    public static ReadHandlerPtr rbff2_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0xc5d0) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x418c);
        }
    };
    public static ReadHandlerPtr ragnagrd_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0xc6c0) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x0042);
        }
    };
    public static ReadHandlerPtr lastblad_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x1868) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x9d4e);
        }
    };
    public static ReadHandlerPtr gururin_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x0604) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x1002);
        }
    };
	//NEO_CYCLE_R(magdrop2,0x1cf3a,0,								neogeo_ram.READ_WORD(0x0064))
    //NEO_CYCLE_R(miexchng,0x,,neogeo_ram.READ_WORD(0x))

    public static ReadHandlerPtr kof98_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0xa146) {
                cpu_spinuntil_int();
                return 0xfff;
            }
            return neogeo_ram.READ_WORD(0xa784);
        }
    };
    public static ReadHandlerPtr marukodq_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x070e) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x0210);
        }
    };
    public static ReadHandlerPtr minasan_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int mem;
            if (cpu_get_pc() == 0x17766) {
                cpu_spinuntil_int();
                mem = neogeo_ram.READ_WORD(0x00ca);
                mem--;
                neogeo_ram.WRITE_WORD(0x00ca, mem);
                return mem;
            }
            return neogeo_ram.READ_WORD(0x00ca);
        }
    };
    public static ReadHandlerPtr stakwin2_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x0b8c) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x0002);
        }
    };
    public static ReadHandlerPtr bakatono_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int mem;
            if (cpu_get_pc() == 0x197cc) {
                cpu_spinuntil_int();
                mem = neogeo_ram.READ_WORD(0x00fa);
                mem--;
                neogeo_ram.WRITE_WORD(0x00fa, mem);
                return mem;
            }
            return neogeo_ram.READ_WORD(0x00fa);
        }
    };
    public static ReadHandlerPtr quizkof_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x0450) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x4464);
        }
    };
    public static ReadHandlerPtr quizdais_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x0730) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x59f2);
        }
    };
    public static ReadHandlerPtr quizdai2_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x1afa) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x0960);
        }
    };
    public static ReadHandlerPtr popbounc_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x1196) {
                cpu_spinuntil_int();
                return 0xffff;
            }
            return neogeo_ram.READ_WORD(0x1008);
        }
    };
    public static ReadHandlerPtr sdodgeb_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0xc22e) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x1104);
        }
    };
    public static ReadHandlerPtr shocktr2_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0xf410) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x8348);
        }
    };
    public static ReadHandlerPtr figfever_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x20c60) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x8100);
        }
    };
    public static ReadHandlerPtr irrmaze_cycle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x104e) {
                cpu_spinuntil_int();
                return 0;
            }
            return neogeo_ram.READ_WORD(0x4b6e);
        }
    };
    /**
     * ***************************************************************************
     */
    /* Routines to speed up the sound processor AVDB 24-10-1998		      */
    /**
     * ***************************************************************************
     */

    /*
     *	Sound V3.0
     *
     *	Used by puzzle de pon and Super Sidekicks 2
     *
     */
    public static ReadHandlerPtr cycle_v3_sr = new ReadHandlerPtr() {
        public int handler(int offset) {
            UBytePtr RAM = memory_region(REGION_CPU2);

            if (cpu_get_pc() == 0x0137) {
                cpu_spinuntil_int();
                return RAM.read(0xfeb1);
            }
            return RAM.read(0xfeb1);
        }
    };

    /*
     *	Also sound revision no 3.0, but different types.
     */
    public static ReadHandlerPtr ssideki_cycle_sr = new ReadHandlerPtr() {
        public int handler(int offset) {
            UBytePtr RAM = memory_region(REGION_CPU2);

            if (cpu_get_pc() == 0x015A) {
                cpu_spinuntil_int();
                return RAM.read(0xfef3);
            }
            return RAM.read(0xfef3);
        }
    };

    public static ReadHandlerPtr aof_cycle_sr = new ReadHandlerPtr() {
        public int handler(int offset) {
            UBytePtr RAM = memory_region(REGION_CPU2);

            if (cpu_get_pc() == 0x0143) {
                cpu_spinuntil_int();
                return RAM.read(0xfef3);
            }
            return RAM.read(0xfef3);
        }
    };

    /*
     *	Sound V2.0
     *
     *	Used by puzzle Bobble and Goal Goal Goal
     *
     */
    public static ReadHandlerPtr cycle_v2_sr = new ReadHandlerPtr() {
        public int handler(int offset) {
            UBytePtr RAM = memory_region(REGION_CPU2);

            if (cpu_get_pc() == 0x0143) {
                cpu_spinuntil_int();
                return RAM.read(0xfeef);
            }
            return RAM.read(0xfeef);
        }
    };

    public static ReadHandlerPtr vwpoint_cycle_sr = new ReadHandlerPtr() {
        public int handler(int offset) {
            UBytePtr RAM = memory_region(REGION_CPU2);

            if (cpu_get_pc() == 0x0143) {
                cpu_spinuntil_int();
                return RAM.read(0xfe46);
            }
            return RAM.read(0xfe46);
        }
    };

    /*
     *	Sound revision no 1.5, and some 2.0 versions,
     *	are not fit for speedups, it results in sound drops !
     *	Games that use this one are : Ghost Pilots, Joy Joy, Nam 1975
     */
    /*
     static public static ReadHandlerPtr cycle_v15_sr = new ReadHandlerPtr() { public int handler(int offset)
     {
     UBytePtr RAM = memory_region(REGION_CPU2);
	
     if (cpu_get_pc()==0x013D) {
     cpu_spinuntil_int();
     return RAM[0xfe46];
     }
     return RAM[0xfe46];
     } };
     */
    /*
     *	Magician Lord uses a different sound core from all other
     *	Neo Geo Games.
     */
    public static ReadHandlerPtr maglord_cycle_sr = new ReadHandlerPtr() {
        public int handler(int offset) {
            UBytePtr RAM = memory_region(REGION_CPU2);

            if (cpu_get_pc() == 0xd487) {
                cpu_spinuntil_int();
                return RAM.read(0xfb91);
            }
            return RAM.read(0xfb91);
        }
    };

    /**
     * ***************************************************************************
     */
    static int prot_data;

    public static ReadHandlerPtr fatfury2_protection_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int res = (prot_data >> 24) & 0xff;

            switch (offset) {
                case 0x55550:
                case 0xffff0:
                case 0x00000:
                case 0xff000:
                case 0x36000:
                case 0x36008:
                    return res;

                case 0x36004:
                case 0x3600c:
                    return ((res & 0xf0) >> 4) | ((res & 0x0f) << 4);

                default:
                    if (errorlog != null) {
                        fprintf(errorlog, "unknown protection read at pc %06x, offset %08x\n", cpu_get_pc(), offset);
                    }
                    return 0;
            }
        }
    };

    public static WriteHandlerPtr fatfury2_protection_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            switch (offset) {
                case 0x55552:	/* data == 0x5555; read back from 55550, ffff0, 00000, ff000 */

                    prot_data = 0xff00ff00;
                    break;

                case 0x56782:	/* data == 0x1234; read back from 36000 *or* 36004 */

                    prot_data = 0xf05a3601;
                    break;

                case 0x42812:	/* data == 0x1824; read back from 36008 *or* 3600c */

                    prot_data = 0x81422418;
                    break;

                case 0x55550:
                case 0xffff0:
                case 0xff000:
                case 0x36000:
                case 0x36004:
                case 0x36008:
                case 0x3600c:
                    prot_data <<= 8;
                    break;

                default:
                    if (errorlog != null) {
                        fprintf(errorlog, "unknown protection write at pc %06x, offset %08x, data %02x\n", cpu_get_pc(), offset, data);
                    }
                    break;
            }
        }
    };

    public static ReadHandlerPtr popbounc_sfix_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (cpu_get_pc() == 0x6b10) {
                return 0;
            }
            return neogeo_ram.READ_WORD(0x4fbc);
        }
    };

    static void neogeo_custom_memory() {
        /* NeoGeo intro screen cycle skip, used by all games */
	//	install_mem_read_handler(0, 0x10fe8c, 0x10fe8d, bios_cycle_skip_r);

        /* Individual games can go here... */
	//#if 1
        //	if (!strcmp(Machine.gamedrv.name,"joyjoy"))   install_mem_read_handler(0, 0x100554, 0x100555, joyjoy_cycle_r);	// Slower
        //	if (!strcmp(Machine.gamedrv.name,"ridhero"))  install_mem_read_handler(0, 0x1000ca, 0x1000cb, ridhero_cycle_r);
        if (strcmp(Machine.gamedrv.name, "bstars") == 0) {
            install_mem_read_handler(0, 0x10000a, 0x10000b, bstars_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "cyberlip") == 0) {
            install_mem_read_handler(0, 0x107bb4, 0x107bb4, cyberlip_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "lbowling") == 0) {
            install_mem_read_handler(0, 0x100098, 0x100099, lbowling_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "superspy") == 0) {
            install_mem_read_handler(0, 0x10108c, 0x10108d, superspy_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "ttbb") == 0) {
            install_mem_read_handler(0, 0x10000e, 0x10000f, ttbb_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "alpham2") == 0) {
            install_mem_read_handler(0, 0x10e2fe, 0x10e2ff, alpham2_cycle_r);	// Very little increase.
        }
        if (strcmp(Machine.gamedrv.name, "eightman") == 0) {
            install_mem_read_handler(0, 0x10046e, 0x10046f, eightman_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "roboarmy") == 0) {
            install_mem_read_handler(0, 0x104010, 0x104011, roboarmy_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "fatfury1") == 0) {
            install_mem_read_handler(0, 0x104282, 0x104283, fatfury1_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "burningf") == 0) {
            install_mem_read_handler(0, 0x10000e, 0x10000f, burningf_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "kotm") == 0) {
            install_mem_read_handler(0, 0x100020, 0x100021, kotm_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "gpilots") == 0) {
            install_mem_read_handler(0, 0x10a682, 0x10a683, gpilots_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "lresort") == 0) {
            install_mem_read_handler(0, 0x104102, 0x104103, lresort_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "fbfrenzy") == 0) {
            install_mem_read_handler(0, 0x100020, 0x100021, fbfrenzy_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "socbrawl") == 0) {
            install_mem_read_handler(0, 0x10b20c, 0x10b20d, socbrawl_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "mutnat") == 0) {
            install_mem_read_handler(0, 0x101042, 0x101043, mutnat_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "aof") == 0) {
            install_mem_read_handler(0, 0x108100, 0x108101, aof_cycle_r);
        }
        //	if (strcmp(Machine.gamedrv.name,"countb")==0)   install_mem_read_handler(0, 0x108002, 0x108003, countb_cycle_r);   // doesn't seem to speed it up.
        if (strcmp(Machine.gamedrv.name, "ncombat") == 0) {
            install_mem_read_handler(0, 0x100206, 0x100207, ncombat_cycle_r);
        }
        //**	if (strcmp(Machine.gamedrv.name,"crsword")==0)  install_mem_read_handler(0, 0x10, 0x10, crsword_cycle_r);			// Can't find this one :-(
        if (strcmp(Machine.gamedrv.name, "trally") == 0) {
            install_mem_read_handler(0, 0x100206, 0x100207, trally_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "sengoku") == 0) {
            install_mem_read_handler(0, 0x100088, 0x100089, sengoku_cycle_r);
        }
        //	if (strcmp(Machine.gamedrv.name,"ncommand")==0) install_mem_read_handler(0, 0x108206, 0x108207, ncommand_cycle_r);	// Slower
        if (strcmp(Machine.gamedrv.name, "wh1") == 0) {
            install_mem_read_handler(0, 0x108206, 0x108207, wh1_cycle_r);
        }
        //**	if (strcmp(Machine.gamedrv.name,"sengoku2")==0) install_mem_read_handler(0, 0x10, 0x10, sengoku2_cycle_r);
        if (strcmp(Machine.gamedrv.name, "androdun") == 0) {
            install_mem_read_handler(0, 0x100080, 0x100081, androdun_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "bjourney") == 0) {
            install_mem_read_handler(0, 0x100206, 0x100207, bjourney_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "maglord") == 0) {
            install_mem_read_handler(0, 0x100206, 0x100207, maglord_cycle_r);
        }
        //	if (strcmp(Machine.gamedrv.name,"janshin")==0)  install_mem_read_handler(0, 0x100026, 0x100027, janshin_cycle_r);	// No speed difference
        if (strcmp(Machine.gamedrv.name, "pulstar") == 0) {
            install_mem_read_handler(0, 0x101000, 0x101001, pulstar_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "blazstar") == 0) {
            install_mem_read_handler(0, 0x101000, 0x101001, blazstar_cycle_r);
        }
        //**	if (strcmp(Machine.gamedrv.name,"pbobble")==0)  install_mem_read_handler(0, 0x10, 0x10, pbobble_cycle_r);		// Can't find this one :-(
        if (strcmp(Machine.gamedrv.name, "puzzledp") == 0) {
            install_mem_read_handler(0, 0x100000, 0x100001, puzzledp_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "neodrift") == 0) {
            install_mem_read_handler(0, 0x100424, 0x100425, neodrift_cycle_r);
        }
        //**	if (strcmp(Machine.gamedrv.name,"neomrdo")==0)  install_mem_read_handler(0, 0x10, 0x10, neomrdo_cycle_r);		// Can't find this one :-(
        if (strcmp(Machine.gamedrv.name, "spinmast") == 0) {
            install_mem_read_handler(0, 0x100050, 0x100051, spinmast_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "karnovr") == 0) {
            install_mem_read_handler(0, 0x103466, 0x103467, karnovr_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "wjammers") == 0) {
            install_mem_read_handler(0, 0x10005a, 0x10005b, wjammers_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "strhoops") == 0) {
            install_mem_read_handler(0, 0x101200, 0x101201, strhoops_cycle_r);
        }
	//	if (strcmp(Machine.gamedrv.name,"magdrop3")==0) install_mem_read_handler(0, 0x100060, 0x100061, magdrop3_cycle_r);	// The game starts glitching.
        //**	if (strcmp(Machine.gamedrv.name,"pspikes2")==0) install_mem_read_handler(0, 0x10, 0x10, pspikes2_cycle_r);		// Can't find this one :-(
        if (strcmp(Machine.gamedrv.name, "sonicwi2") == 0) {
            install_mem_read_handler(0, 0x10e5b6, 0x10e5b7, sonicwi2_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "sonicwi3") == 0) {
            install_mem_read_handler(0, 0x10ea2e, 0x10ea2f, sonicwi3_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "goalx3") == 0) {
            install_mem_read_handler(0, 0x100006, 0x100007, goalx3_cycle_r);
        }
	//	if (strcmp(Machine.gamedrv.name,"mslug")==0)    install_mem_read_handler(0, 0x106ed8, 0x106ed9, mslug_cycle_r);		// Doesn't work properly.
        //	if (strcmp(Machine.gamedrv.name,"turfmast")==0) install_mem_read_handler(0, 0x102e54, 0x102e55, turfmast_cycle_r);
        if (strcmp(Machine.gamedrv.name, "kabukikl") == 0) {
            install_mem_read_handler(0, 0x10428a, 0x10428b, kabukikl_cycle_r);
        }

        if (strcmp(Machine.gamedrv.name, "panicbom") == 0) {
            install_mem_read_handler(0, 0x10009c, 0x10009d, panicbom_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "neobombe") == 0) {
            install_mem_read_handler(0, 0x10448c, 0x10448d, neobombe_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "wh2") == 0) {
            install_mem_read_handler(0, 0x108206, 0x108207, wh2_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "wh2j") == 0) {
            install_mem_read_handler(0, 0x108206, 0x108207, wh2j_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "aodk") == 0) {
            install_mem_read_handler(0, 0x108206, 0x108207, aodk_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "whp") == 0) {
            install_mem_read_handler(0, 0x108206, 0x108207, whp_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "overtop") == 0) {
            install_mem_read_handler(0, 0x108202, 0x108203, overtop_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "twinspri") == 0) {
            install_mem_read_handler(0, 0x108206, 0x108207, twinspri_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "stakwin") == 0) {
            install_mem_read_handler(0, 0x100b92, 0x100b93, stakwin_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "shocktro") == 0) {
            install_mem_read_handler(0, 0x108344, 0x108345, shocktro_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "tws96") == 0) {
            install_mem_read_handler(0, 0x10010e, 0x10010f, tws96_cycle_r);
        }
	//	if (strcmp(Machine.gamedrv.name,"zedblade")==0) install_mem_read_handler(0, 0x109004, 0x109005, zedblade_cycle_r);
        //	if (strcmp(Machine.gamedrv.name,"doubledr")==0) install_mem_read_handler(0, 0x101c30, 0x101c31, doubledr_cycle_r);
        //**	if (strcmp(Machine.gamedrv.name,"gowcaizr")==0) install_mem_read_handler(0, 0x10, 0x10, gowcaizr_cycle_r);		// Can't find this one :-(
        if (strcmp(Machine.gamedrv.name, "galaxyfg") == 0) {
            install_mem_read_handler(0, 0x101858, 0x101859, galaxyfg_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "wakuwak7") == 0) {
            install_mem_read_handler(0, 0x100bd4, 0x100bd5, wakuwak7_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "mahretsu") == 0) {
            install_mem_read_handler(0, 0x1013b2, 0x1013b3, mahretsu_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "nam1975") == 0) {
            install_mem_read_handler(0, 0x1012e0, 0x1012e1, nam1975_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "tpgolf") == 0) {
            install_mem_read_handler(0, 0x1000a4, 0x1000a5, tpgolf_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "legendos") == 0) {
            install_mem_read_handler(0, 0x100002, 0x100003, legendos_cycle_r);
        }
        //	if (strcmp(Machine.gamedrv.name,"viewpoin")==0) install_mem_read_handler(0, 0x101216, 0x101217, viewpoin_cycle_r);	// Doesn't work
        if (strcmp(Machine.gamedrv.name, "fatfury2") == 0) {
            install_mem_read_handler(0, 0x10418c, 0x10418d, fatfury2_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "bstars2") == 0) {
            install_mem_read_handler(0, 0x10001c, 0x10001c, bstars2_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "ssideki") == 0) {
            install_mem_read_handler(0, 0x108c84, 0x108c85, ssideki_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "kotm2") == 0) {
            install_mem_read_handler(0, 0x101000, 0x101001, kotm2_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "samsho") == 0) {
            install_mem_read_handler(0, 0x100a76, 0x100a77, samsho_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "fatfursp") == 0) {
            install_mem_read_handler(0, 0x10418c, 0x10418d, fatfursp_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "fatfury3") == 0) {
            install_mem_read_handler(0, 0x10418c, 0x10418d, fatfury3_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "tophuntr") == 0) {
            install_mem_read_handler(0, 0x10008e, 0x10008f, tophuntr_cycle_r);	// Can't test this at the moment, it crashes.
        }
        if (strcmp(Machine.gamedrv.name, "savagere") == 0) {
            install_mem_read_handler(0, 0x108404, 0x108405, savagere_cycle_r);
        }
        //	if (strcmp(Machine.gamedrv.name,"kof94")==0)    install_mem_read_handler(0, 0x10, 0x10, kof94_cycle_r);				// Can't do this I think. There seems to be too much code in the idle loop.
        if (strcmp(Machine.gamedrv.name, "aof2") == 0) {
            install_mem_read_handler(0, 0x108280, 0x108281, aof2_cycle_r);
        }
        //	if (strcmp(Machine.gamedrv.name,"ssideki2")==0) install_mem_read_handler(0, 0x104292, 0x104293, ssideki2_cycle_r);
        if (strcmp(Machine.gamedrv.name, "samsho2") == 0) {
            install_mem_read_handler(0, 0x100a30, 0x100a31, samsho2_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "samsho3") == 0) {
            install_mem_read_handler(0, 0x108408, 0x108409, samsho3_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "kof95") == 0) {
            install_mem_read_handler(0, 0x10a784, 0x10a785, kof95_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "rbff1") == 0) {
            install_mem_read_handler(0, 0x10418c, 0x10418d, rbff1_cycle_r);
        }
        //	if (strcmp(Machine.gamedrv.name,"aof3")==0)     install_mem_read_handler(0, 0x104ee8, 0x104ee9, aof3_cycle_r);		// Doesn't work properly.
        if (strcmp(Machine.gamedrv.name, "ninjamas") == 0) {
            install_mem_read_handler(0, 0x108206, 0x108207, ninjamas_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "kof96") == 0) {
            install_mem_read_handler(0, 0x10a782, 0x10a783, kof96_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "samsho4") == 0) {
            install_mem_read_handler(0, 0x10830c, 0x10830d, samsho4_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "rbffspec") == 0) {
            install_mem_read_handler(0, 0x10418c, 0x10418d, rbffspec_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "kizuna") == 0) {
            install_mem_read_handler(0, 0x108808, 0x108809, kizuna_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "kof97") == 0) {
            install_mem_read_handler(0, 0x10a784, 0x10a785, kof97_cycle_r);
        }
        //	if (strcmp(Machine.gamedrv.name,"mslug2")==0)   install_mem_read_handler(0, 0x10008c, 0x10008d, mslug2_cycle_r);	// Breaks the game
        if (strcmp(Machine.gamedrv.name, "rbff2") == 0) {
            install_mem_read_handler(0, 0x10418c, 0x10418d, rbff2_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "ragnagrd") == 0) {
            install_mem_read_handler(0, 0x100042, 0x100043, ragnagrd_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "lastblad") == 0) {
            install_mem_read_handler(0, 0x109d4e, 0x109d4f, lastblad_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "gururin") == 0) {
            install_mem_read_handler(0, 0x101002, 0x101003, gururin_cycle_r);
        }
	//	if (strcmp(Machine.gamedrv.name,"magdrop2")==0) install_mem_read_handler(0, 0x100064, 0x100065, magdrop2_cycle_r);	// Graphic Glitches
        //	if (strcmp(Machine.gamedrv.name,"miexchng")==0) install_mem_read_handler(0, 0x10, 0x10, miexchng_cycle_r);			// Can't do this.
        if (strcmp(Machine.gamedrv.name, "kof98") == 0) {
            install_mem_read_handler(0, 0x10a784, 0x10a785, kof98_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "marukodq") == 0) {
            install_mem_read_handler(0, 0x100210, 0x100211, marukodq_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "minasan") == 0) {
            install_mem_read_handler(0, 0x1000ca, 0x1000cb, minasan_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "stakwin2") == 0) {
            install_mem_read_handler(0, 0x100002, 0x100003, stakwin2_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "bakatono") == 0) {
            install_mem_read_handler(0, 0x1000fa, 0x1000fb, bakatono_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "quizkof") == 0) {
            install_mem_read_handler(0, 0x104464, 0x104465, quizkof_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "quizdais") == 0) {
            install_mem_read_handler(0, 0x1059f2, 0x1059f3, quizdais_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "quizdai2") == 0) {
            install_mem_read_handler(0, 0x100960, 0x100961, quizdai2_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "popbounc") == 0) {
            install_mem_read_handler(0, 0x101008, 0x101009, popbounc_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "sdodgeb") == 0) {
            install_mem_read_handler(0, 0x101104, 0x101105, sdodgeb_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "shocktr2") == 0) {
            install_mem_read_handler(0, 0x108348, 0x108349, shocktr2_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "figfever") == 0) {
            install_mem_read_handler(0, 0x108100, 0x108101, figfever_cycle_r);
        }
        if (strcmp(Machine.gamedrv.name, "irrmaze") == 0) {
            install_mem_read_handler(0, 0x104b6e, 0x104b6f, irrmaze_cycle_r);
        }

	//#endif
        /* AVDB cpu spins based on sound processor status */
        if (strcmp(Machine.gamedrv.name, "puzzledp") == 0) {
            install_mem_read_handler(1, 0xfeb1, 0xfeb1, cycle_v3_sr);
        }
	//	if (strcmp(Machine.gamedrv.name,"ssideki2")==0) install_mem_read_handler(1, 0xfeb1, 0xfeb1, cycle_v3_sr);

        if (strcmp(Machine.gamedrv.name, "ssideki") == 0) {
            install_mem_read_handler(1, 0xfef3, 0xfef3, ssideki_cycle_sr);
        }
        if (strcmp(Machine.gamedrv.name, "aof") == 0) {
            install_mem_read_handler(1, 0xfef3, 0xfef3, aof_cycle_sr);
        }

        if (strcmp(Machine.gamedrv.name, "pbobble") == 0) {
            install_mem_read_handler(1, 0xfeef, 0xfeef, cycle_v2_sr);
        }
        if (strcmp(Machine.gamedrv.name, "goalx3") == 0) {
            install_mem_read_handler(1, 0xfeef, 0xfeef, cycle_v2_sr);
        }
        if (strcmp(Machine.gamedrv.name, "fatfury1") == 0) {
            install_mem_read_handler(1, 0xfeef, 0xfeef, cycle_v2_sr);
        }
        if (strcmp(Machine.gamedrv.name, "mutnat") == 0) {
            install_mem_read_handler(1, 0xfeef, 0xfeef, cycle_v2_sr);
        }

        if (strcmp(Machine.gamedrv.name, "maglord") == 0) {
            install_mem_read_handler(1, 0xfb91, 0xfb91, maglord_cycle_sr);
        }
        if (strcmp(Machine.gamedrv.name, "vwpoint") == 0) {
            install_mem_read_handler(1, 0xfe46, 0xfe46, vwpoint_cycle_sr);
        }

	//	if (strcmp(Machine.gamedrv.name,"joyjoy")==0) install_mem_read_handler(1, 0xfe46, 0xfe46, cycle_v15_sr);
        //	if (strcmp(Machine.gamedrv.name,"nam1975")==0) install_mem_read_handler(1, 0xfe46, 0xfe46, cycle_v15_sr);
        //	if (strcmp(Machine.gamedrv.name,"gpilots")==0) install_mem_read_handler(1, 0xfe46, 0xfe46, cycle_v15_sr);
        /* kludges */
        if (strcmp(Machine.gamedrv.name, "gururin") == 0) {
            /* Fix a really weird problem. The game clears the video RAM but goes */
            /* beyond the tile RAM, corrupting the zoom control RAM. After that it */
            /* initializes the control RAM, but then corrupts it again! */
            UBytePtr RAM = memory_region(REGION_CPU1);
            RAM.WRITE_WORD(0x1328, 0x4e71);
            RAM.WRITE_WORD(0x132a, 0x4e71);
            RAM.WRITE_WORD(0x132c, 0x4e71);
            RAM.WRITE_WORD(0x132e, 0x4e71);
        }

        if (Machine.sample_rate == 0
                && strcmp(Machine.gamedrv.name, "popbounc") == 0) /* the game hangs after a while without this patch */ {
            install_mem_read_handler(0, 0x104fbc, 0x104fbd, popbounc_sfix_r);
        }

        /* hacks to make the games which do protection checks run in arcade mode */
        /* we write protect a SRAM location so it cannot be set to 1 */
        sram_protection_hack = -1;
        if (strcmp(Machine.gamedrv.name, "fatfury3") == 0
                || strcmp(Machine.gamedrv.name, "samsho3") == 0
                || strcmp(Machine.gamedrv.name, "samsho4") == 0
                || strcmp(Machine.gamedrv.name, "aof3") == 0
                || strcmp(Machine.gamedrv.name, "rbff1") == 0
                || strcmp(Machine.gamedrv.name, "rbffspec") == 0
                || strcmp(Machine.gamedrv.name, "kof95") == 0
                || strcmp(Machine.gamedrv.name, "kof96") == 0
                || strcmp(Machine.gamedrv.name, "kof97") == 0
                || strcmp(Machine.gamedrv.name, "kof98") == 0
                || strcmp(Machine.gamedrv.name, "kof99") == 0
                || strcmp(Machine.gamedrv.name, "kizuna") == 0
                || strcmp(Machine.gamedrv.name, "lastblad") == 0
                || strcmp(Machine.gamedrv.name, "lastbld2") == 0
                || strcmp(Machine.gamedrv.name, "rbff2") == 0
                || strcmp(Machine.gamedrv.name, "mslug2") == 0
                || strcmp(Machine.gamedrv.name, "garou") == 0) {
            sram_protection_hack = 0x100;
        }

        if (strcmp(Machine.gamedrv.name, "pulstar") == 0) {
            sram_protection_hack = 0x35a;
        }

        if (strcmp(Machine.gamedrv.name, "ssideki") == 0) {
            /* patch out protection check */
            /* the protection routines are at 0x25dcc and involve reading and writing */
            /* addresses in the 0x2xxxxx range */
            UBytePtr RAM = memory_region(REGION_CPU1);
            RAM.WRITE_WORD(0x2240, 0x4e71);
        }

        /* Hacks the program rom of Fatal Fury 2, needed either in arcade or console mode */
        /* otherwise at level 2 you cannot hit the opponent and other problems */
        if (strcmp(Machine.gamedrv.name, "fatfury2") == 0) {
            /* there seems to also be another protection check like the countless ones */
            /* patched above by protectiong a SRAM location, but that trick doesn't work */
            /* here (or maybe the SRAM location to protect is different), so I patch out */
            /* the routine which trashes memory. Without this, the game goes nuts after */
            /* the first bonus stage. */
            UBytePtr RAM = memory_region(REGION_CPU1);
            RAM.WRITE_WORD(0xb820, 0x4e71);
            RAM.WRITE_WORD(0xb822, 0x4e71);

            /* again, the protection involves reading and writing addresses in the */
            /* 0x2xxxxx range. There are several checks all around the code. */
            install_mem_read_handler(0, 0x200000, 0x2fffff, fatfury2_protection_r);
            install_mem_write_handler(0, 0x200000, 0x2fffff, fatfury2_protection_w);
        }

        if (strcmp(Machine.gamedrv.name, "fatfury3") == 0) {
            /* patch the first word, it must be 0x0010 not 0x0000 (initial stack pointer) */
            UBytePtr RAM = memory_region(REGION_CPU1);
            RAM.WRITE_WORD(0x0000, 0x0010);
        }

        if (strcmp(Machine.gamedrv.name, "mslugx") == 0) {
            /* patch out protection checks */
            int i;
            UBytePtr RAM = memory_region(REGION_CPU1);

            for (i = 0; i < 0x100000; i += 2) {
                if (RAM.READ_WORD(i + 0) == 0x0243
                        && RAM.READ_WORD(i + 2) == 0x0001
                        && /* andi.w  #$1, D3 */ RAM.READ_WORD(i + 4) == 0x6600) /* bne xxxx */ {
                    RAM.WRITE_WORD(i + 4, 0x4e71);
                    RAM.WRITE_WORD(i + 6, 0x4e71);
                }
            }

            RAM.WRITE_WORD(0x3bdc, 0x4e71);
            RAM.WRITE_WORD(0x3bde, 0x4e71);
            RAM.WRITE_WORD(0x3be0, 0x4e71);
            RAM.WRITE_WORD(0x3c0c, 0x4e71);
            RAM.WRITE_WORD(0x3c0e, 0x4e71);
            RAM.WRITE_WORD(0x3c10, 0x4e71);

            RAM.WRITE_WORD(0x3c36, 0x4e71);
            RAM.WRITE_WORD(0x3c38, 0x4e71);
        }
    }

    public static WriteHandlerPtr neogeo_sram_lock_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            sram_locked = 1;
        }
    };

    public static WriteHandlerPtr neogeo_sram_unlock_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            sram_locked = 0;
        }
    };

    public static ReadHandlerPtr neogeo_sram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return neogeo_sram.READ_WORD(offset);
        }
    };

    public static WriteHandlerPtr neogeo_sram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (sram_locked != 0) {
                if (errorlog != null) {
                    fprintf(errorlog, "PC %06x: warning: write %02x to SRAM %04x while it was protected\n", cpu_get_pc(), data, offset);
                }
            } else {
                if (offset == sram_protection_hack) {
                    if (data == 0x0001 || data == 0xff000001) {
                        return;	/* fake protection pass */
                    }
                }

                COMBINE_WORD_MEM(neogeo_sram, offset, data);
            }
        }
    };
    public static nvramPtr neogeo_nvram_handler = new nvramPtr() {
        public void handler(Object file, int read_or_write) {
            if (read_or_write != 0) {
                /* Save the SRAM settings */
                /*TODO*///		osd_fwrite_msbfirst(file,neogeo_sram,0x2000);

                /* save the memory card */
                /*TODO*///		neogeo_memcard_save();
            } else {
                /* Load the SRAM settings for this game */
                /*TODO*///		if (file != null)
/*TODO*///				osd_fread_msbfirst(file,neogeo_sram,0x2000);
	/*TODO*///		else
                memset(neogeo_sram, 0, 0x10000);

                /* load the memory card */
                /*TODO*///		neogeo_memcard_load(memcard_number);
            }
        }
    };

    /*
     INFORMATION:
	
     Memory card is a 2kb battery backed RAM.
     It is accessed thru 0x800000-0x800FFF.
     Even bytes are always 0xFF
     Odd bytes are memcard data (0x800 bytes)
	
     Status byte at 0x380000: (BITS ARE ACTIVE *LOW*)
	
     0 PAD1 START
     1 PAD1 SELECT
     2 PAD2 START
     3 PAD2 SELECT
     4 --\  MEMORY CARD
     5 --/  INSERTED
     6 MEMORY CARD WRITE PROTECTION
     7 UNUSED (?)
     */
    /**
     * ******************* MEMCARD ROUTINES *********************
     */
    public static ReadHandlerPtr neogeo_memcard_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (memcard_status == 1) {
                return (neogeo_memcard.read(offset >> 1) | 0xFF00);
            } else {
                return 0xFFFF;
            }
        }
    };

    public static WriteHandlerPtr neogeo_memcard_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (memcard_status == 1) {
                neogeo_memcard.write(offset >> 1, (data & 0xFF));
            }
        }
    };

    public static int neogeo_memcard_load(int number) {
        /*TODO*///	    char name[16];
/*TODO*///	    void *f;
/*TODO*///	
/*TODO*///	    sprintf(name, "MEMCARD.%03d", number);
/*TODO*///	    if ((f=osd_fopen(0, name, OSD_FILETYPE_MEMCARD,0))!=0)
/*TODO*///	    {
/*TODO*///	        osd_fread(f,neogeo_memcard,0x800);
/*TODO*///	        osd_fclose(f);
/*TODO*///	        return 1;
/*TODO*///	    }
        return 0;
    }

    public static void neogeo_memcard_save() {
        /*TODO*///	    char name[16];
/*TODO*///	    void *f;

        /*TODO*///	    if (memcard_number!=-1)
/*TODO*///	    {
/*TODO*///	        sprintf(name, "MEMCARD.%03d", memcard_number);
/*TODO*///	        if ((f=osd_fopen(0, name, OSD_FILETYPE_MEMCARD,1))!=0)
/*TODO*///	        {
/*TODO*///	            osd_fwrite(f,neogeo_memcard,0x800);
/*TODO*///	            osd_fclose(f);
/*TODO*///	        }
/*TODO*///	    }
    }

    public static void neogeo_memcard_eject() {
        /*TODO*///	   if (memcard_number!=-1)
/*TODO*///	   {
/*TODO*///	       neogeo_memcard_save();
/*TODO*///	       memset(neogeo_memcard, 0, 0x800);
/*TODO*///	       memcard_status=0;
/*TODO*///	       memcard_number=-1;
/*TODO*///	   }
    }

    public int neogeo_memcard_create(int number) {
        /*TODO*///	    char buf[0x800];
/*TODO*///	    char name[16];
/*TODO*///	    void *f1, *f2;

        /*TODO*///	    sprintf(name, "MEMCARD.%03d", number);
/*TODO*///	    if ((f1=osd_fopen(0, name, OSD_FILETYPE_MEMCARD,0))==0)
/*TODO*///	    {
/*TODO*///	        if ((f2=osd_fopen(0, name, OSD_FILETYPE_MEMCARD,1))!=0)
/*TODO*///	        {
/*TODO*///	            osd_fwrite(f2,buf,0x800);
/*TODO*///	            osd_fclose(f2);
/*TODO*///	            return 1;
/*TODO*///	        }
/*TODO*///	    }
/*TODO*///	    else
/*TODO*///	        osd_fclose(f1);
        return 0;
    }

}
