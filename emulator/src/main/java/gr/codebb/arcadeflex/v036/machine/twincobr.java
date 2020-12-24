/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */
package gr.codebb.arcadeflex.v036.machine;

import static gr.codebb.arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.platform.fileio.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.timer.*;
import static gr.codebb.arcadeflex.v037b7.mame.timerH.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.twincobr.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.cpu.tms32010.tms32010H.*;



public class twincobr {
    public static final int LOG_DSP_CALLS =0;
    public static final int CLEAR =0;
    public static final int ASSERT=1;
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	unsigned char *twincobr_68k_dsp_ram;
/*TODO*///	unsigned char *twincobr_sharedram;

    public static UBytePtr wardner_mainram = new UBytePtr();
    /*TODO*///	
/*TODO*///	extern unsigned char *spriteram;
/*TODO*///	extern unsigned char *paletteram;
/*TODO*///	
/*TODO*///	
/*TODO*///	extern int twincobr_fg_rom_bank;
/*TODO*///	extern int twincobr_bg_ram_bank;
/*TODO*///	extern int twincobr_display_on;
/*TODO*///	extern int twincobr_flip_screen;
/*TODO*///	extern int twincobr_flip_x_base;
/*TODO*///	extern int twincobr_flip_y_base;
/*TODO*///	extern int wardner_sprite_hack;
/*TODO*///	
    static int coin_count;	/* coin count increments on startup ? , so stop it */

    static int dsp_execute;
    static /*unsigned*/ int dsp_addr_w, main_ram_seg;
    public static int toaplan_main_cpu;   /* Main CPU type.  0 = 68000, 1 = Z80 */
    static String toaplan_cpu_type[] = { "68K" , "Z80" };

    public static int twincobr_intenable;
    public static int fsharkbt_8741;
    /*TODO*///	
/*TODO*///	
/*TODO*///	void fsharkbt_reset_8741_mcu(void)
/*TODO*///	{
/*TODO*///		/* clean out high score tables in these game hardware */
/*TODO*///		int twincobr_cnt;
/*TODO*///		int twinc_hisc_addr[12] =
/*TODO*///		{
/*TODO*///			0x15a4, 0x15a8, 0x170a, 0x170c, /* Twin Cobra */
/*TODO*///			0x1282, 0x1284, 0x13ea, 0x13ec, /* Kyukyo Tiger */
/*TODO*///			0x016c, 0x0170, 0x02d2, 0x02d4	/* Flying shark */
/*TODO*///		};
/*TODO*///		for (twincobr_cnt=0; twincobr_cnt < 12; twincobr_cnt++)
/*TODO*///		{
/*TODO*///			WRITE_WORD(&twincobr_68k_dsp_ram[(twinc_hisc_addr[twincobr_cnt])],0xffff);
/*TODO*///		}
/*TODO*///	
/*TODO*///		toaplan_main_cpu = 0;		/* 68000 */
/*TODO*///		twincobr_display_on = 0;
/*TODO*///		fsharkbt_8741 = -1;
/*TODO*///		twincobr_intenable = 0;
/*TODO*///		dsp_addr_w = dsp_execute = 0;
/*TODO*///		main_ram_seg = 0;
/*TODO*///	
/*TODO*///		/* coin count increments on startup ? , so stop it */
/*TODO*///		coin_count = 0;
/*TODO*///	
/*TODO*///		/* blank out the screen */
/*TODO*///		osd_clearbitmap(Machine.scrbitmap);
/*TODO*///	}
    public static InitMachinePtr wardner_reset = new InitMachinePtr() {
        public void handler() {
            /* clean out high score tables in these game hardware */
            wardner_mainram.write(0x0117, 0xff);
            wardner_mainram.write(0x0118, 0xff);
            wardner_mainram.write(0x0119, 0xff);
            wardner_mainram.write(0x011a, 0xff);
            wardner_mainram.write(0x011b, 0xff);
            wardner_mainram.write(0x0170, 0xff);
            wardner_mainram.write(0x0171, 0xff);
            wardner_mainram.write(0x0172, 0xff);

            toaplan_main_cpu = 1;		/* Z80 */

            twincobr_intenable = 0;
            twincobr_display_on = 1;
            dsp_addr_w = dsp_execute = 0;
            main_ram_seg = 0;

            /* coin count increments on startup ? , so stop it */
            coin_count = 0;

            /* blank out the screen */
            osd_clearbitmap(Machine.scrbitmap);
        }
    };

    	
	public static ReadHandlerPtr twincobr_dsp_in = new ReadHandlerPtr() { public int handler(int offset)
	{
            
		/* DSP can read data from main CPU RAM via DSP IO port 1 */
	
		/*unsigned*/ int input_data = 0;
		switch (main_ram_seg) {
/*TODO*///			case 0x30000:	input_data = READ_WORD(&twincobr_68k_dsp_ram[dsp_addr_w]); break;
/*TODO*///			case 0x40000:	input_data = READ_WORD(&spriteram[dsp_addr_w]); break;
/*TODO*///			case 0x50000:	input_data = READ_WORD(&paletteram[dsp_addr_w]); break;
			case 0x7000:	input_data = wardner_mainram.read(dsp_addr_w) + (wardner_mainram.read(dsp_addr_w+1)<<8); break;
			case 0x8000:	input_data = spriteram.read(dsp_addr_w) + (spriteram.read(dsp_addr_w+1)<<8); break;
			case 0xa000:	input_data = paletteram.read(dsp_addr_w) + (paletteram.read(dsp_addr_w+1)<<8); break;
			default:		if (errorlog != null)
								fprintf(errorlog,"DSP PC:%04x Warning !!! IO reading from %08x (port 1)\n",cpu_getpreviouspc(),main_ram_seg + dsp_addr_w);
		}
		if (errorlog != null) fprintf(errorlog,"DSP PC:%04x IO read %04x at %08x (port 1)\n",cpu_getpreviouspc(),input_data,main_ram_seg + dsp_addr_w);
		return input_data;
	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr fsharkbt_dsp_in = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		/* Flying Shark bootleg uses IO port 2 */
/*TODO*///		/* DSP reads data from an extra MCU (8741) at IO port 2 */
/*TODO*///		/* Boot-leggers using their own copy protection ?? */
/*TODO*///		/* Port is read three times during startup. First and last data */
/*TODO*///		/*	 read must equal, but second read data must be different */
/*TODO*///		fsharkbt_8741 += 1;
/*TODO*///	#if LOG_DSP_CALLS
/*TODO*///		if (errorlog != 0) fprintf(errorlog,"DSP PC:%04x IO read %04x from 8741 MCU (port 2)\n",cpu_getpreviouspc(),(fsharkbt_8741 & 0x08));
/*TODO*///	#endif
/*TODO*///		return (fsharkbt_8741 & 1);
/*TODO*///	} };
/*TODO*///	
	public static WriteHandlerPtr twincobr_dsp_out = new WriteHandlerPtr() { public void handler(int fnction, int data)
	{
            
		if (fnction == 0) {
			/* This sets the main CPU RAM address the DSP should */
			/*		read/write, via the DSP IO port 0 */
			/* Top three bits of data need to be shifted left 3 places */
			/*		to select which memory bank from main CPU address */
			/*		space to use */
			/* Lower thirteen bits of this data is shifted left one position */
			/*		to move it to an even address word boundary */
	
			dsp_addr_w = ((data & 0x1fff) << 1);
			main_ram_seg = ((data & 0xe000) << 3);
			if (toaplan_main_cpu == 1) {		/* Z80 */
				dsp_addr_w &= 0xfff;
				if (main_ram_seg == 0x30000) main_ram_seg = 0x7000;
				if (main_ram_seg == 0x40000) main_ram_seg = 0x8000;
				if (main_ram_seg == 0x50000) main_ram_seg = 0xa000;
			}
                if (errorlog != null) fprintf(errorlog,"DSP PC:%04x IO write %04x (%08x) at port 0\n",cpu_getpreviouspc(),data,main_ram_seg + dsp_addr_w);

		}
		if (fnction == 1) {
			/* Data written to main CPU RAM via DSP IO port 1*/
			dsp_execute = 0;
			switch (main_ram_seg) {
/*TODO*///				case 0x30000:	WRITE_WORD(&twincobr_68k_dsp_ram[dsp_addr_w],data);
/*TODO*///									if ((dsp_addr_w < 3) && (data == 0)) dsp_execute = 1; break;
/*TODO*///				case 0x40000:	WRITE_WORD(&spriteram[dsp_addr_w],data); break;
/*TODO*///				case 0x50000:	WRITE_WORD(&paletteram[dsp_addr_w],data); break;
				case 0x7000:	wardner_mainram.write(dsp_addr_w,data & 0xff);
								wardner_mainram.write(dsp_addr_w + 1,(data >> 8) & 0xff);
								if ((dsp_addr_w < 3) && (data == 0)) dsp_execute = 1; break;
				case 0x8000:	spriteram.write(dsp_addr_w,data & 0xff);
								spriteram.write(dsp_addr_w + 1,(data >> 8) & 0xff);break;
				case 0xa000:	paletteram.write(dsp_addr_w,data & 0xff);
								paletteram.write(dsp_addr_w + 1,(data >> 8) & 0xff); break;
				default:		if (errorlog != null)
									fprintf(errorlog,"DSP PC:%04x Warning !!! IO writing to %08x (port 1)\n",cpu_getpreviouspc(),main_ram_seg + dsp_addr_w);
			}

			if (errorlog != null) fprintf(errorlog,"DSP PC:%04x IO write %04x at %08x (port 1)\n",cpu_getpreviouspc(),data,main_ram_seg + dsp_addr_w);

		}
		if (fnction == 2) {
			/* Flying Shark bootleg DSP writes data to an extra MCU (8741) at IO port 2 */
		}
		if (fnction == 3) {
			/* data 0xffff	means inhibit BIO line to DSP and enable  */
			/*				communication to main processor */
			/*				Actually only DSP data bit 15 controls this */
			/* data 0x0000	means set DSP BIO line active and disable */
			/*				communication to main processor*/
			if (errorlog != null) fprintf(errorlog,"DSP PC:%04x IO write %04x at port 3\n",cpu_getpreviouspc(),data);

			if ((data & 0x8000) != 0) {
				cpu_set_irq_line(2, TMS320C10_ACTIVE_BIO, CLEAR_LINE);
			}
			if (data == 0) {
				if (dsp_execute != 0) {
                            if (errorlog != null) fprintf(errorlog,"Turning %s on\n",toaplan_cpu_type[toaplan_main_cpu]);
					timer_suspendcpu(0, CLEAR, SUSPEND_REASON_HALT);
					dsp_execute = 0;
				}
				cpu_set_irq_line(2, TMS320C10_ACTIVE_BIO, ASSERT_LINE);
			}
		}
	} };
	
/*TODO*///	public static ReadHandlerPtr twincobr_68k_dsp_r = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		return READ_WORD(&twincobr_68k_dsp_ram[offset]);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr twincobr_68k_dsp_w = new WriteHandlerPtr() { public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///	#if LOG_DSP_CALLS
/*TODO*///		if (errorlog != 0) if (offset < 10) fprintf(errorlog,"%s:%08x write %08x at %08x\n",toaplan_cpu_type[toaplan_main_cpu],cpu_get_pc(),data,0x30000+offset);
/*TODO*///	#endif
/*TODO*///		COMBINE_WORD_MEM(&twincobr_68k_dsp_ram[offset],data);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
    public static WriteHandlerPtr wardner_mainram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /*TODO*///	#if 0
/*TODO*///		if (errorlog != 0)
/*TODO*///			if ((offset == 4) && (data != 4)) fprintf(errorlog,"CPU #0:%04x  Writing %02x to %04x of main RAM (DSP command number)\n",cpu_get_pc(),data, offset + 0x7000);
/*TODO*///	#endif
            wardner_mainram.write(offset, data);

        }
    };
    public static ReadHandlerPtr wardner_mainram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return wardner_mainram.read(offset);
        }
    };

    public static WriteHandlerPtr twincobr_7800c_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /*TODO*///	#if 0
/*TODO*///		if (errorlog != 0) fprintf(errorlog,"%s:%08x  Writing %08x to %08x.\n",toaplan_cpu_type[toaplan_main_cpu],cpu_get_pc(),data,toaplan_port_type[toaplan_main_cpu] - offset);
/*TODO*///	#endif

            if (toaplan_main_cpu == 1) {
                if (data == 0x0c) {
                    data = 0x1c;
                    wardner_sprite_hack = 0;
                }	/* Z80 ? */

                if (data == 0x0d) {
                    data = 0x1d;
                    wardner_sprite_hack = 1;
                }	/* Z80 ? */

            }

            switch (data) {
                case 0x0004:
                    twincobr_intenable = 0;
                    break;
                case 0x0005:
                    twincobr_intenable = 1;
                    break;
                case 0x0006:
                    twincobr_flip_screen = 0;
                    twincobr_flip_x_base = 0x037;
                    twincobr_flip_y_base = 0x01e;
                    break;
                case 0x0007:
                    twincobr_flip_screen = 1;
                    twincobr_flip_x_base = 0x085;
                    twincobr_flip_y_base = 0x0f2;
                    break;
                case 0x0008:
                    twincobr_bg_ram_bank = 0x0000;
                    break;
                case 0x0009:
                    twincobr_bg_ram_bank = 0x2000;
                    break;
                case 0x000a:
                    twincobr_fg_rom_bank = 0x0000;
                    break;
                case 0x000b:
                    twincobr_fg_rom_bank = 0x1000;
                    break;
                case 0x000e:
                    twincobr_display_on = 0x0000;
                    break; /* Turn display off */

                case 0x000f:
                    twincobr_display_on = 0x0001;
                    break; /* Turn display on */

                case 0x000c:
                    if (twincobr_display_on != 0) {
                        /* This means assert the INT line to the DSP */
                    if (errorlog != null) fprintf(errorlog,"Turning DSP on and %s off\n",toaplan_cpu_type[toaplan_main_cpu]);
                        timer_suspendcpu(2, CLEAR, SUSPEND_REASON_HALT);
                        cpu_set_irq_line(2, TMS320C10_ACTIVE_INT, ASSERT_LINE);
                        timer_suspendcpu(0, ASSERT, SUSPEND_REASON_HALT);
                    }
                    break;
                case 0x000d:
                    if (twincobr_display_on != 0) {
                        /* This means inhibit the INT line to the DSP */

			if (errorlog != null) fprintf(errorlog,"Turning DSP off\n");
                        cpu_set_irq_line(2, TMS320C10_ACTIVE_INT, CLEAR_LINE);
                        timer_suspendcpu(2, ASSERT, SUSPEND_REASON_HALT);
                    }
                    break;
            }
        }
    };

    /*TODO*///	
/*TODO*///	public static ReadHandlerPtr twincobr_sharedram_r = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		return twincobr_sharedram[offset / 2];
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr twincobr_sharedram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		twincobr_sharedram[offset / 2] = data;
/*TODO*///	} };
	
	public static WriteHandlerPtr fshark_coin_dsp_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
/*TODO*///	#if 0
/*TODO*///		if (errorlog != 0)
/*TODO*///			if (data > 1)
/*TODO*///				fprintf(errorlog,"%s:%08x  Writing %08x to %08x.\n",toaplan_cpu_type[toaplan_main_cpu],cpu_get_pc(),data,toaplan_port_type[toaplan_main_cpu] - offset);
/*TODO*///	#endif
		switch (data) {
			case 0x08: if (coin_count != 0) { coin_counter_w.handler(0,1); coin_counter_w.handler(0,0); } break;
			case 0x09: if (coin_count != 0) { coin_counter_w.handler(2,1); coin_counter_w.handler(2,0); } break;
			case 0x0a: if (coin_count != 0) { coin_counter_w.handler(1,1); coin_counter_w.handler(1,0); } break;
			case 0x0b: if (coin_count != 0) { coin_counter_w.handler(3,1); coin_counter_w.handler(3,0); } break;
			case 0x0c: coin_lockout_w.handler(0,1); coin_lockout_w.handler(2,1); break;
			case 0x0d: coin_lockout_w.handler(0,0); coin_lockout_w.handler(2,0); break;
			case 0x0e: coin_lockout_w.handler(1,1); coin_lockout_w.handler(3,1); break;
			case 0x0f: coin_lockout_w.handler(1,0); coin_lockout_w.handler(3,0); coin_count=1; break;
			case 0x00:	/* This means assert the INT line to the DSP */
                            if (errorlog != null) fprintf(errorlog,"Turning DSP on and %s off\n",toaplan_cpu_type[toaplan_main_cpu]);

						timer_suspendcpu(2, CLEAR, SUSPEND_REASON_HALT);
						cpu_set_irq_line(2, TMS320C10_ACTIVE_INT, ASSERT_LINE);
						timer_suspendcpu(0, ASSERT, SUSPEND_REASON_HALT);
						break;
			case 0x01:	/* This means inhibit the INT line to the DSP */

				if (errorlog !=null) fprintf(errorlog,"Turning DSP off\n");

						cpu_set_irq_line(2, TMS320C10_ACTIVE_INT, CLEAR_LINE);
						timer_suspendcpu(2, ASSERT, SUSPEND_REASON_HALT);
						break;
		}
	} };
}
