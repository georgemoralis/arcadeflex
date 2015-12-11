package drivers.WIP;

import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.commonH.*;
import static mame.inputport.*;
import static arcadeflex.ptrlib.*;
import static mame.drawgfxH.*;
import static vidhrdw.generic.*;
import static mame.cpuintrf.*;
import static mame.cpuintrfH.*;
import static mame.inputportH.*;
import static mame.mame.*;
import static arcadeflex.libc_old.*;
import static arcadeflex.libc.*;
import arcadeflex.libc_v2.ShortPtr;
import static mame.sndintrf.soundlatch_r;
import static mame.sndintrf.soundlatch_w;
import static cpu.m6809.m6809H.*;
import static cpu.z80.z80H.*;
import static mame.common.*;
import static mame.commonH.*;
import static mame.palette.*;
import static mame.memory.*;
import mame.sndintrfH.MachineSound;
import static mame.sndintrfH.SOUND_K007232;
import static mame.sndintrfH.SOUND_YM2151;
import static vidhrdw.konamiic.*;
import static sound.k007232.*;
import static sound.k007232H.*;
import static sound._2151intf.*;
import static sound._2151intfH.*;
import static sound.mixerH.*;
import static vidhrdw.konami.K053247.*;
import static mame.timer.*;
import static mame.timerH.*;
import static mame.inputH.*;
import static vidhrdw.tmnt.*;
import static cpu.konami.konamiH.*;
import static mame.sndintrfH.*;
import static sound.k053260.*;
import static sound.k053260H.*;
import static machine.eeprom.*;
import static machine.eepromH.*;
import static sound.upd7759.*;
import static sound.upd7759H.*;
import static vidhrdw.konami.K054000.*;
import static sound.samples.*;
import static sound.samplesH.*;

public class tmnt {

    static int tmnt_soundlatch;

    public static ReadHandlerPtr K052109_word_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            offset >>>= 1;
            return K052109_r.handler(offset + 0x2000) | (K052109_r.handler(offset) << 8);
        }
    };

    public static WriteHandlerPtr K052109_word_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            offset >>>= 1;
            if ((data & 0xff000000) == 0) {
                K052109_w.handler(offset, (data >> 8) & 0xff);
            }
            if ((data & 0x00ff0000) == 0) {
                K052109_w.handler(offset + 0x2000, data & 0xff);
            }
        }
    };
    public static ReadHandlerPtr K052109_word_noA12_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* some games have the A12 line not connected, so the chip spans */
            /* twice the memory range, with mirroring */
            offset = ((offset & 0x6000) >>> 1) | (offset & 0x0fff);
            return K052109_word_r.handler(offset);
        }
    };

    public static WriteHandlerPtr K052109_word_noA12_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* some games have the A12 line not connected, so the chip spans */
            /* twice the memory range, with mirroring */
            offset = ((offset & 0x6000) >>> 1) | (offset & 0x0fff);
            K052109_word_w.handler(offset, data);
        }
    };
    /* the interface with the 053245 is weird. The chip can address only 0x800 bytes */
    /* of RAM, but they put 0x4000 there. The CPU can access them all. Address lines */
    /* A1, A5 and A6 don't go to the 053245. */
    public static ReadHandlerPtr K053245_scattered_word_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if ((offset & 0x0062) != 0) {
                return spriteram.READ_WORD(offset);
            } else {
                offset = ((offset & 0x001c) >>> 1) | ((offset & 0x3f80) >>> 3);
                return K053245_word_r.handler(offset);
            }
        }
    };
    public static WriteHandlerPtr K053245_scattered_word_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((offset & 0x0062) != 0) {
                COMBINE_WORD_MEM(spriteram, offset, data);
            } else {
                offset = ((offset & 0x001c) >>> 1) | ((offset & 0x3f80) >>> 3);
                //if (errorlog && (offset&0xf) == 0)
                //	fprintf(errorlog,"%04x: write %02x to spriteram %04x\n",cpu_get_pc(),data,offset);
                K053245_word_w.handler(offset, data);
            }
        }
    };
    /*TODO*///
/*TODO*///static int K053244_halfword_r(int offset)
/*TODO*///{
/*TODO*///	return K053244_r(offset >> 1);
/*TODO*///}
/*TODO*///
/*TODO*///static void K053244_halfword_w(int offset,int data)
/*TODO*///{
/*TODO*///	if ((data & 0x00ff0000) == 0)
/*TODO*///		K053244_w(offset >> 1,data & 0xff);
/*TODO*///}
/*TODO*///
    public static ReadHandlerPtr K053244_word_noA1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            offset &= ~2;	/* handle mirror address */

            return K053244_r.handler(offset / 2 + 1) | (K053244_r.handler(offset / 2) << 8);
        }
    };
    public static WriteHandlerPtr K053244_word_noA1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            offset &= ~2;	/* handle mirror address */

            if ((data & 0xff000000) == 0) {
                K053244_w.handler(offset / 2, (data >>> 8) & 0xff);
            }
            if ((data & 0x00ff0000) == 0) {
                K053244_w.handler(offset / 2 + 1, data & 0xff);
            }
        }
    };
    public static WriteHandlerPtr K053251_halfword_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 0x00ff0000) == 0) {
                K053251_w.handler(offset >>> 1, data & 0xff);
            }
        }
    };
    /*TODO*///
/*TODO*///static void K053251_halfword_swap_w(int offset,int data)
/*TODO*///{
/*TODO*///	if ((data & 0xff000000) == 0)
/*TODO*///		K053251_w(offset >> 1,(data >> 8) & 0xff);
/*TODO*///}
/*TODO*///
    public static ReadHandlerPtr K054000_halfword_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return K054000_r.handler(offset >>> 1);
        }
    };

    public static WriteHandlerPtr K054000_halfword_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 0x00ff0000) == 0) {
                K054000_w.handler(offset >>> 1, data & 0xff);
            }
        }
    };

    public static InterruptPtr punkshot_interrupt = new InterruptPtr() {
        public int handler() {
            if (K052109_is_IRQ_enabled() != 0) {
                return m68_level4_irq.handler();
            } else {
                return ignore_interrupt.handler();
            }

        }
    };
	
	public static InterruptPtr lgtnfght_interrupt = new InterruptPtr() { public int handler() 
	{
		if (K052109_is_IRQ_enabled()!=0) return m68_level5_irq.handler();
		else return ignore_interrupt.handler();
	
	} };
    public static WriteHandlerPtr tmnt_sound_command_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(0, data & 0xff);
        }
    };
    public static ReadHandlerPtr punkshot_sound_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* If the sound CPU is running, read the status, otherwise
             just make it pass the test */
            if (Machine.sample_rate != 0) {
                return K053260_ReadReg.handler(2 + offset / 2);
            } else {
                return 0x80;
            }
        }
    };

    /*TODO*///static int detatwin_sound_r(int offset)
/*TODO*///{
/*TODO*///	/* If the sound CPU is running, read the status, otherwise
/*TODO*///	   just make it pass the test */
/*TODO*///	if (Machine->sample_rate != 0) 	return K053260_ReadReg(2 + offset/2);
/*TODO*///	else return offset ? 0xfe : 0x00;
/*TODO*///}
/*TODO*///
/*TODO*///static int glfgreat_sound_r(int offset)
/*TODO*///{
/*TODO*///	/* If the sound CPU is running, read the status, otherwise
/*TODO*///	   just make it pass the test */
/*TODO*///	if (Machine->sample_rate != 0) 	return K053260_ReadReg(2 + offset/2) << 8;
/*TODO*///	else return 0;
/*TODO*///}
/*TODO*///
/*TODO*///static void glfgreat_sound_w(int offset,int data)
/*TODO*///{
/*TODO*///	if ((data & 0xff000000) == 0)
/*TODO*///		K053260_WriteReg(offset >> 1,(data >> 8) & 0xff);
/*TODO*///
/*TODO*///	if (offset == 2) cpu_cause_interrupt(1,0xff);
/*TODO*///}
/*TODO*///
/*TODO*///static int tmnt2_sound_r(int offset)
/*TODO*///{
/*TODO*///	/* If the sound CPU is running, read the status, otherwise
/*TODO*///	   just make it pass the test */
/*TODO*///	if (Machine->sample_rate != 0) 	return K053260_ReadReg(2 + offset/2);
/*TODO*///	else return offset ? 0x00 : 0x80;
/*TODO*///}
/*TODO*///
/*TODO*///
    public static ReadHandlerPtr tmnt_sres_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return tmnt_soundlatch;
        }
    };

    public static WriteHandlerPtr tmnt_sres_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bit 1 resets the UPD7795C sound_old chip */
            if ((data & 0x02) == 0) {
                UPD7759_reset_w.handler(0, (data & 0x02) >> 1);
            }

            /* bit 2 plays the title music */
            		if ((data & 0x04)!=0)
		{
			if (sample_playing(0)==0)	sample_start(0,0,0);
		}
		else sample_stop(0);
            tmnt_soundlatch = data;
        }
    };
    public static ShStartPtr tmnt_decode_sample = new ShStartPtr() {
        public int handler(MachineSound msound) {
	int i;
	ShortPtr dest;
	UBytePtr source = memory_region(REGION_SOUND3);
	GameSamples samples;


	Machine.samples = new GameSamples();


	//samples = Machine.samples;
    Machine.samples.sample[0]=new GameSample(0x40000*2);

    Machine.samples.sample[0].length = 0x40000*2;
    Machine.samples.sample[0].smpfreq = 20000;	/* 20 kHz */
    Machine.samples.sample[0].resolution = 16;
	dest = new ShortPtr(Machine.samples.sample[0].data);
	Machine.samples.total = 1;

	/*	Sound sample for TMNT.D05 is stored in the following mode:
	 *
	 *	Bit 15-13:	Exponent (2 ^ x)
	 *	Bit 12-4 :	Sound data (9 bit)
	 *
	 *	(Sound info courtesy of Dave <dayvee@rocketmail.com>)
	 */

	for (i = 0;i < 0x40000;i++)
	{
		int val = source.read(2*i) + source.read(2*i+1) * 256;
		int exp = val >> 13;

	  	val = (val >> 4) & (0x1ff);	/* 9 bit, Max Amplitude 0x200 */
		val -= 0x100;					/* Centralize value	*/

		val <<= exp;

		dest.write(i, (short)val);
	}

	/*	The sample is now ready to be used.  It's a 16 bit, 22khz sample.
	 */

	return 0;
}};
    static int sound_nmi_enabled;

    public static timer_callback sound_nmi_callback = new timer_callback() {
        public void handler(int param) {
            cpu_set_nmi_line(1, (sound_nmi_enabled) != 0 ? CLEAR_LINE : ASSERT_LINE);
            sound_nmi_enabled = 0;
        }
    };
    public static timer_callback nmi_callback = new timer_callback() {
        public void handler(int param) {

            cpu_set_nmi_line(1, ASSERT_LINE);
        }
    };
    public static WriteHandlerPtr sound_arm_nmi = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            //	sound_nmi_enabled = 1;
            cpu_set_nmi_line(1, CLEAR_LINE);
            timer_set(TIME_IN_USEC(50), 0, nmi_callback);	/* kludge until the K053260 is emulated correctly */

        }
    };
    public static ReadHandlerPtr punkshot_kludge = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* I don't know what's going on here; at one point, the code reads location */
            /* 0xffffff, and returning 0 causes the game to mess up - locking up in a */
            /* loop where the ball is continuously bouncing from the basket. Returning */
            /* a random number seems to prevent that. */
            return rand();
        }
    };
    public static ReadHandlerPtr ssriders_kludge = new ReadHandlerPtr() {
        public int handler(int offset) {
            int data = cpu_readmem24_word(0x105a0a);

            //if (errorlog!=null) fprintf(errorlog,"%06x: read 1c0800 (D7=%02x 105a0a=%02x)\n",cpu_get_pc(),cpu_get_reg(M68K_D7),data);
            if (data == 0x075c) {
                data = 0x0064;
            }

            if (cpu_readmem24_word(cpu_get_pc()) == 0x45f9) {
                /*TODO*///               /*data = -((cpu_get_reg(M68K_D7) & 0xff) + 32);
  /*TODO*///              data = ((data / 8) & 0x1f) * 0x40;
  /*TODO*///              data += (((cpu_get_reg(M68K_D6) & 0xffff) + (K052109_r(0x1a01) * 256)
  /*TODO*///                      + K052109_r(0x1a00) + 96) / 8) & 0x3f;*/
                System.out.println("TODO");
            }

            return data;
        }
    };
    /**
     * *************************************************************************
     *
     * EEPROM
     *
     **************************************************************************
     */

    static int init_eeprom_count;

    static EEPROM_interface eeprom_interface = new EEPROM_interface(
            7, /* address bits */
            8, /* data bits */
            "011000", /*  read command */
            "011100", /* write command */
            null, /* erase command */
            "0100000000000",/* lock command */
            "0100110000000" /* unlock command */
    );

    public static nvramPtr nvram_handler = new nvramPtr() {
        public void handler(Object file, int read_or_write) {
            if (read_or_write != 0) {
                EEPROM_save(file);
            } else {
                EEPROM_init(eeprom_interface);

                if (file != null) {
                    init_eeprom_count = 0;
                    EEPROM_load(file);
                } else {
                    init_eeprom_count = 10;
                }
            }
        }
    };
    /*TODO*///
/*TODO*///static int detatwin_coin_r(int offset)
/*TODO*///{
/*TODO*///	int res;
/*TODO*///	static int toggle;
/*TODO*///
/*TODO*///	/* bit 3 is service button */
/*TODO*///	/* bit 6 is ??? VBLANK? OBJMPX? */
/*TODO*///	res = input_port_2_r(0);
/*TODO*///	if (init_eeprom_count)
/*TODO*///	{
/*TODO*///		init_eeprom_count--;
/*TODO*///		res &= 0xf7;
/*TODO*///	}
/*TODO*///	toggle ^= 0x40;
/*TODO*///	return res ^ toggle;
/*TODO*///}
/*TODO*///
/*TODO*///static int detatwin_eeprom_r(int offset)
/*TODO*///{
/*TODO*///	int res;
/*TODO*///
/*TODO*///	/* bit 0 is EEPROM data */
/*TODO*///	/* bit 1 is EEPROM ready */
/*TODO*///	res = EEPROM_read_bit() | input_port_3_r(0);
/*TODO*///	return res;
/*TODO*///}
/*TODO*///
    static int toggle_ss;
    public static ReadHandlerPtr ssriders_eeprom_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int res;
            /* bit 0 is EEPROM data */
            /* bit 1 is EEPROM ready */
            /* bit 2 is VBLANK (???) */
            /* bit 7 is service button */
            res = EEPROM_read_bit() | input_port_3_r.handler(0);
            if (init_eeprom_count != 0) {
                init_eeprom_count--;
                res &= 0x7f;
            }
            toggle_ss ^= 0x04;
            return res ^ toggle_ss;
        }
    };
    public static WriteHandlerPtr ssriders_eeprom_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bit 0 is data */
            /* bit 1 is cs (active low) */
            /* bit 2 is clock (active high) */
            EEPROM_write_bit(data & 0x01);
            EEPROM_set_cs_line((data & 0x02) != 0 ? CLEAR_LINE : ASSERT_LINE);
            EEPROM_set_clock_line((data & 0x04) != 0 ? ASSERT_LINE : CLEAR_LINE);

            /* bit 5 selects sprite ROM for testing in TMNT2 */
            K053244_bankselect((data & 0x20) >> 5);
        }
    };

    static EEPROM_interface thndrx2_eeprom_interface = new EEPROM_interface(
            7, /* address bits */
            8, /* data bits */
            "011000", /*  read command */
            "010100", /* write command */
            null, /* erase command */
            "0100000000000",/* lock command */
            "0100110000000" /* unlock command */
    );
    public static nvramPtr thndrx2_nvram_handler = new nvramPtr() {
        public void handler(Object file, int read_or_write) {
            if (read_or_write != 0) {
                EEPROM_save(file);
            } else {
                EEPROM_init(thndrx2_eeprom_interface);

                if (file != null) {
                    init_eeprom_count = 0;
                    EEPROM_load(file);
                } else {
                    init_eeprom_count = 10;
                }
            }
        }
    };
    public static ReadHandlerPtr thndrx2_in0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int res;

            res = input_port_0_r.handler(0);
            if (init_eeprom_count != 0) {
                init_eeprom_count--;
                res &= 0xf7ff;
            }
            return res;
        }
    };
    static int toggle_thndrx2;
    public static ReadHandlerPtr thndrx2_eeprom_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int res;

            /* bit 0 is EEPROM data */
            /* bit 1 is EEPROM ready */
            /* bit 3 is VBLANK (???) */
            /* bit 7 is service button */
            res = (EEPROM_read_bit() << 8) | input_port_1_r.handler(0);
            toggle_thndrx2 ^= 0x0800;
            return (res ^ toggle_thndrx2);
        }
    };
    static int last_thndrx2;
    public static WriteHandlerPtr thndrx2_eeprom_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            if ((data & 0x00ff0000) == 0) {
                /* bit 0 is data */
                /* bit 1 is cs (active low) */
                /* bit 2 is clock (active high) */
                EEPROM_write_bit(data & 0x01);
                EEPROM_set_cs_line((data & 0x02) != 0 ? CLEAR_LINE : ASSERT_LINE);
                EEPROM_set_clock_line((data & 0x04) != 0 ? ASSERT_LINE : CLEAR_LINE);

                /* bit 5 triggers IRQ on sound cpu */
                if (last_thndrx2 == 0 && (data & 0x20) != 0) {
                    cpu_cause_interrupt(1, 0xff);
                }
                last_thndrx2 = data & 0x20;

                /* bit 6 = enable char ROM reading through the video RAM */
                K052109_set_RMRD_line((data & 0x40) != 0 ? ASSERT_LINE : CLEAR_LINE);
            }
        }
    };
    static MemoryReadAddress mia_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x03ffff, MRA_ROM),
                new MemoryReadAddress(0x040000, 0x043fff, MRA_BANK3), /* main RAM */
                new MemoryReadAddress(0x060000, 0x063fff, MRA_BANK1), /* main RAM */
                new MemoryReadAddress(0x080000, 0x080fff, paletteram_word_r),
                new MemoryReadAddress(0x0a0000, 0x0a0001, input_port_0_r),
                new MemoryReadAddress(0x0a0002, 0x0a0003, input_port_1_r),
                new MemoryReadAddress(0x0a0004, 0x0a0005, input_port_2_r),
                new MemoryReadAddress(0x0a0010, 0x0a0011, input_port_3_r),
                new MemoryReadAddress(0x0a0012, 0x0a0013, input_port_4_r),
                new MemoryReadAddress(0x0a0018, 0x0a0019, input_port_5_r),
                new MemoryReadAddress(0x100000, 0x107fff, K052109_word_noA12_r),
                new MemoryReadAddress(0x140000, 0x140007, K051937_word_r),
                new MemoryReadAddress(0x140400, 0x1407ff, K051960_word_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress mia_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x03ffff, MWA_ROM),
                new MemoryWriteAddress(0x040000, 0x043fff, MWA_BANK3), /* main RAM */
                new MemoryWriteAddress(0x060000, 0x063fff, MWA_BANK1), /* main RAM */
                new MemoryWriteAddress(0x080000, 0x080fff, tmnt_paletteram_w, paletteram),
                new MemoryWriteAddress(0x0a0000, 0x0a0001, tmnt_0a0000_w),
                new MemoryWriteAddress(0x0a0008, 0x0a0009, tmnt_sound_command_w),
                new MemoryWriteAddress(0x0a0010, 0x0a0011, watchdog_reset_w),
                new MemoryWriteAddress(0x100000, 0x107fff, K052109_word_noA12_w),
                new MemoryWriteAddress(0x140000, 0x140007, K051937_word_w),
                new MemoryWriteAddress(0x140400, 0x1407ff, K051960_word_w),
                //	new MemoryWriteAddress( 0x10e800, 0x10e801, MWA_NOP ), ???
                //#if 0
                //	new MemoryWriteAddress( 0x0c0000, 0x0c0001, tmnt_priority_w ),
                ///#endif
                new MemoryWriteAddress(-1) /* end of table */};
    static MemoryReadAddress tmnt_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x05ffff, MRA_ROM),
                new MemoryReadAddress(0x060000, 0x063fff, MRA_BANK1), /* main RAM */
                new MemoryReadAddress(0x080000, 0x080fff, paletteram_word_r),
                new MemoryReadAddress(0x0a0000, 0x0a0001, input_port_0_r),
                new MemoryReadAddress(0x0a0002, 0x0a0003, input_port_1_r),
                new MemoryReadAddress(0x0a0004, 0x0a0005, input_port_2_r),
                new MemoryReadAddress(0x0a0006, 0x0a0007, input_port_3_r),
                new MemoryReadAddress(0x0a0010, 0x0a0011, input_port_4_r),
                new MemoryReadAddress(0x0a0012, 0x0a0013, input_port_5_r),
                new MemoryReadAddress(0x0a0014, 0x0a0015, input_port_6_r),
                new MemoryReadAddress(0x0a0018, 0x0a0019, input_port_7_r),
                new MemoryReadAddress(0x100000, 0x107fff, K052109_word_noA12_r),
                new MemoryReadAddress(0x140000, 0x140007, K051937_word_r),
                new MemoryReadAddress(0x140400, 0x1407ff, K051960_word_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress tmnt_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x05ffff, MWA_ROM),
                new MemoryWriteAddress(0x060000, 0x063fff, MWA_BANK1), /* main RAM */
                new MemoryWriteAddress(0x080000, 0x080fff, tmnt_paletteram_w, paletteram),
                new MemoryWriteAddress(0x0a0000, 0x0a0001, tmnt_0a0000_w),
                new MemoryWriteAddress(0x0a0008, 0x0a0009, tmnt_sound_command_w),
                new MemoryWriteAddress(0x0a0010, 0x0a0011, watchdog_reset_w),
                new MemoryWriteAddress(0x0c0000, 0x0c0001, tmnt_priority_w),
                new MemoryWriteAddress(0x100000, 0x107fff, K052109_word_noA12_w),
                //	new MemoryWriteAddress( 0x10e800, 0x10e801, MWA_NOP ), ???
                new MemoryWriteAddress(0x140000, 0x140007, K051937_word_w),
                new MemoryWriteAddress(0x140400, 0x1407ff, K051960_word_w),
                new MemoryWriteAddress(-1) /* end of table */};
    static MemoryReadAddress punkshot_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x03ffff, MRA_ROM),
                new MemoryReadAddress(0x080000, 0x083fff, MRA_BANK1), /* main RAM */
                new MemoryReadAddress(0x090000, 0x090fff, paletteram_word_r),
                new MemoryReadAddress(0x0a0000, 0x0a0001, input_port_0_r),
                new MemoryReadAddress(0x0a0002, 0x0a0003, input_port_1_r),
                new MemoryReadAddress(0x0a0004, 0x0a0005, input_port_3_r),
                new MemoryReadAddress(0x0a0006, 0x0a0007, input_port_2_r),
                new MemoryReadAddress(0x0a0040, 0x0a0043, punkshot_sound_r), /* K053260 */
                new MemoryReadAddress(0x100000, 0x107fff, K052109_word_noA12_r),
                new MemoryReadAddress(0x110000, 0x110007, K051937_word_r),
                new MemoryReadAddress(0x110400, 0x1107ff, K051960_word_r),
                new MemoryReadAddress(0xfffffc, 0xffffff, punkshot_kludge),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress punkshot_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x03ffff, MWA_ROM),
                new MemoryWriteAddress(0x080000, 0x083fff, MWA_BANK1), /* main RAM */
                new MemoryWriteAddress(0x090000, 0x090fff, paletteram_xBBBBBGGGGGRRRRR_word_w, paletteram),
                new MemoryWriteAddress(0x0a0020, 0x0a0021, punkshot_0a0020_w),
                new MemoryWriteAddress(0x0a0040, 0x0a0041, K053260_WriteReg),
                new MemoryWriteAddress(0x0a0060, 0x0a007f, K053251_halfword_w),
                new MemoryWriteAddress(0x0a0080, 0x0a0081, watchdog_reset_w),
                new MemoryWriteAddress(0x100000, 0x107fff, K052109_word_noA12_w),
                new MemoryWriteAddress(0x110000, 0x110007, K051937_word_w),
                new MemoryWriteAddress(0x110400, 0x1107ff, K051960_word_w),
                new MemoryWriteAddress(-1) /* end of table */};
    static MemoryReadAddress lgtnfght_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x03ffff, MRA_ROM),
                new MemoryReadAddress(0x080000, 0x080fff, paletteram_word_r),
                new MemoryReadAddress(0x090000, 0x093fff, MRA_BANK1), /* main RAM */
                new MemoryReadAddress(0x0a0000, 0x0a0001, input_port_0_r),
                new MemoryReadAddress(0x0a0002, 0x0a0003, input_port_1_r),
                new MemoryReadAddress(0x0a0004, 0x0a0005, input_port_2_r),
                new MemoryReadAddress(0x0a0006, 0x0a0007, input_port_3_r),
                new MemoryReadAddress(0x0a0008, 0x0a0009, input_port_4_r),
                new MemoryReadAddress(0x0a0010, 0x0a0011, input_port_5_r),
                new MemoryReadAddress(0x0a0020, 0x0a0023, punkshot_sound_r), /* K053260 */
                new MemoryReadAddress(0x0b0000, 0x0b3fff, K053245_scattered_word_r),
                new MemoryReadAddress(0x0c0000, 0x0c001f, K053244_word_noA1_r),
                new MemoryReadAddress(0x100000, 0x107fff, K052109_word_noA12_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress lgtnfght_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x03ffff, MWA_ROM),
                new MemoryWriteAddress(0x080000, 0x080fff, paletteram_xBBBBBGGGGGRRRRR_word_w, paletteram),
                new MemoryWriteAddress(0x090000, 0x093fff, MWA_BANK1), /* main RAM */
                new MemoryWriteAddress(0x0a0018, 0x0a0019, lgtnfght_0a0018_w),
                new MemoryWriteAddress(0x0a0020, 0x0a0021, K053260_WriteReg),
                new MemoryWriteAddress(0x0a0028, 0x0a0029, watchdog_reset_w),
                new MemoryWriteAddress(0x0b0000, 0x0b3fff, K053245_scattered_word_w, spriteram),
                new MemoryWriteAddress(0x0c0000, 0x0c001f, K053244_word_noA1_w),
                new MemoryWriteAddress(0x0e0000, 0x0e001f, K053251_halfword_w),
                new MemoryWriteAddress(0x100000, 0x107fff, K052109_word_noA12_w),
                new MemoryWriteAddress(-1) /* end of table */};
    public static WriteHandlerPtr ssriders_soundkludge_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* I think this is more than just a trigger */
            cpu_cause_interrupt(1, 0xff);
        }
    };
    /*TODO*///
/*TODO*///static struct MemoryReadAddress detatwin_readmem[] =
/*TODO*///{
/*TODO*///	{ 0x000000, 0x07ffff, MRA_ROM },
/*TODO*///	{ 0x180000, 0x183fff, K052109_word_r },
/*TODO*///	{ 0x204000, 0x207fff, MRA_BANK1 },	/* main RAM */
/*TODO*///	{ 0x300000, 0x303fff, K053245_scattered_word_r },
/*TODO*///	{ 0x400000, 0x400fff, paletteram_word_r },
/*TODO*///	{ 0x500000, 0x50003f, K054000_halfword_r },
/*TODO*///	{ 0x680000, 0x68001f, K053244_word_noA1_r },
/*TODO*///	{ 0x700000, 0x700001, input_port_0_r },
/*TODO*///	{ 0x700002, 0x700003, input_port_1_r },
/*TODO*///	{ 0x700004, 0x700005, detatwin_coin_r },
/*TODO*///	{ 0x700006, 0x700007, detatwin_eeprom_r },
/*TODO*///	{ 0x780600, 0x780603, detatwin_sound_r },	/* K053260 */
/*TODO*///	{ -1 }	/* end of table */
/*TODO*///};
/*TODO*///
/*TODO*///static struct MemoryWriteAddress detatwin_writemem[] =
/*TODO*///{
/*TODO*///	{ 0x000000, 0x07ffff, MWA_ROM },
/*TODO*///	{ 0x180000, 0x183fff, K052109_word_w },
/*TODO*///	{ 0x204000, 0x207fff, MWA_BANK1 },	/* main RAM */
/*TODO*///	{ 0x300000, 0x303fff, K053245_scattered_word_w, &spriteram },
/*TODO*///	{ 0x500000, 0x50003f, K054000_halfword_w },
/*TODO*///	{ 0x400000, 0x400fff, paletteram_xBBBBBGGGGGRRRRR_word_w, &paletteram },
/*TODO*///	{ 0x680000, 0x68001f, K053244_word_noA1_w },
/*TODO*///	{ 0x700200, 0x700201, ssriders_eeprom_w },
/*TODO*///	{ 0x700400, 0x700401, watchdog_reset_w },
/*TODO*///	{ 0x700300, 0x700301, detatwin_700300_w },
/*TODO*///	{ 0x780600, 0x780601, K053260_WriteReg },
/*TODO*///	{ 0x780604, 0x780605, ssriders_soundkludge_w },
/*TODO*///	{ 0x780700, 0x78071f, K053251_halfword_w },
/*TODO*///	{ -1 }	/* end of table */
/*TODO*///};
/*TODO*///
/*TODO*///
/*TODO*///static int ball(int offset)
/*TODO*///{
/*TODO*///	return 0x11;
/*TODO*///}
/*TODO*///
/*TODO*///static struct MemoryReadAddress glfgreat_readmem[] =
/*TODO*///{
/*TODO*///	{ 0x000000, 0x03ffff, MRA_ROM },
/*TODO*///	{ 0x100000, 0x103fff, MRA_BANK1 },	/* main RAM */
/*TODO*///	{ 0x104000, 0x107fff, K053245_scattered_word_r },
/*TODO*///	{ 0x108000, 0x108fff, paletteram_word_r },
/*TODO*///	{ 0x10c000, 0x10cfff, MRA_BANK2 },	/* 053936? */
/*TODO*///	{ 0x114000, 0x11401f, K053244_halfword_r },
/*TODO*///	{ 0x120000, 0x120001, input_port_0_r },
/*TODO*///	{ 0x120002, 0x120003, input_port_1_r },
/*TODO*///	{ 0x120004, 0x120005, input_port_3_r },
/*TODO*///	{ 0x120006, 0x120007, input_port_2_r },
/*TODO*///	{ 0x121000, 0x121001, ball },	/* protection? returning 0, every shot is a "water" */
/*TODO*///	{ 0x125000, 0x125003, glfgreat_sound_r },	/* K053260 */
/*TODO*///	{ 0x200000, 0x207fff, K052109_word_noA12_r },
/*TODO*///	{ -1 }	/* end of table */
/*TODO*///};
/*TODO*///
/*TODO*///static struct MemoryWriteAddress glfgreat_writemem[] =
/*TODO*///{
/*TODO*///	{ 0x000000, 0x03ffff, MWA_ROM },
/*TODO*///	{ 0x100000, 0x103fff, MWA_BANK1 },	/* main RAM */
/*TODO*///	{ 0x104000, 0x107fff, K053245_scattered_word_w, &spriteram },
/*TODO*///	{ 0x108000, 0x108fff, paletteram_xBBBBBGGGGGRRRRR_word_w, &paletteram },
/*TODO*///	{ 0x10c000, 0x10cfff, MWA_BANK2 },	/* 053936? */
/*TODO*///	{ 0x110000, 0x11001f, K053244_word_noA1_w },	/* duplicate! */
/*TODO*///	{ 0x114000, 0x11401f, K053244_halfword_w },		/* duplicate! */
/*TODO*///	{ 0x118000, 0x11801f, MWA_NOP },	/* 053936 control? */
/*TODO*///	{ 0x11c000, 0x11c01f, K053251_halfword_swap_w },
/*TODO*///	{ 0x122000, 0x122001, glfgreat_122000_w },
/*TODO*///	{ 0x124000, 0x124001, watchdog_reset_w },
/*TODO*///	{ 0x125000, 0x125003, glfgreat_sound_w },	/* K053260 */
/*TODO*///	{ 0x200000, 0x207fff, K052109_word_noA12_w },
/*TODO*///	{ -1 }	/* end of table */
/*TODO*///};
/*TODO*///
/*TODO*///
/*TODO*///static struct MemoryReadAddress tmnt2_readmem[] =
/*TODO*///{
/*TODO*///	{ 0x000000, 0x07ffff, MRA_ROM },
/*TODO*///	{ 0x104000, 0x107fff, MRA_BANK1 },	/* main RAM */
/*TODO*///	{ 0x140000, 0x140fff, paletteram_word_r },
/*TODO*///	{ 0x180000, 0x183fff, K053245_scattered_word_r },
/*TODO*///	{ 0x1c0000, 0x1c0001, input_port_0_r },
/*TODO*///	{ 0x1c0002, 0x1c0003, input_port_1_r },
/*TODO*///	{ 0x1c0004, 0x1c0005, input_port_4_r },
/*TODO*///	{ 0x1c0006, 0x1c0007, input_port_5_r },
/*TODO*///	{ 0x1c0100, 0x1c0101, input_port_2_r },
/*TODO*///	{ 0x1c0102, 0x1c0103, ssriders_eeprom_r },
/*TODO*///	{ 0x1c0400, 0x1c0401, watchdog_reset_r },
/*TODO*///	{ 0x1c0500, 0x1c057f, MRA_BANK3 },	/* TMNT2 only (1J); unknown */
/*TODO*/////	{ 0x1c0800, 0x1c0801, ssriders_kludge },	/* protection device */
/*TODO*///	{ 0x5a0000, 0x5a001f, K053244_word_noA1_r },
/*TODO*///	{ 0x5c0600, 0x5c0603, tmnt2_sound_r },	/* K053260 */
/*TODO*///	{ 0x600000, 0x603fff, K052109_word_r },
/*TODO*///	{ -1 }	/* end of table */
/*TODO*///};
/*TODO*///
/*TODO*///static unsigned char *tmnt2_1c0800,*sunset_104000;
/*TODO*///
/*TODO*///void tmnt2_1c0800_w( int offset, int data )
/*TODO*///{
/*TODO*///    COMBINE_WORD_MEM( &tmnt2_1c0800[offset], data );
/*TODO*///    if ( offset == 0x0010 && ( ( READ_WORD( &tmnt2_1c0800[0x10] ) & 0xff00 ) == 0x8200 ) )
/*TODO*///	{
/*TODO*///		unsigned int CellSrc;
/*TODO*///		unsigned int CellVar;
/*TODO*///		unsigned char *src;
/*TODO*///		int dst;
/*TODO*///		int x,y;
/*TODO*///
/*TODO*///		CellVar = ( READ_WORD( &tmnt2_1c0800[0x08] ) | ( READ_WORD( &tmnt2_1c0800[0x0A] ) << 16 ) );
/*TODO*///		dst = ( READ_WORD( &tmnt2_1c0800[0x04] ) | ( READ_WORD( &tmnt2_1c0800[0x06] ) << 16 ) );
/*TODO*///		CellSrc = ( READ_WORD( &tmnt2_1c0800[0x00] ) | ( READ_WORD( &tmnt2_1c0800[0x02] ) << 16 ) );
/*TODO*/////        if ( CellDest >= 0x180000 && CellDest < 0x183fe0 ) {
/*TODO*///        CellVar -= 0x104000;
/*TODO*///		src = &memory_region(REGION_CPU1)[CellSrc];
/*TODO*///
/*TODO*///		cpu_writemem24_word(dst+0x00,0x8000 | ((READ_WORD(src+2) & 0xfc00) >> 2));	/* size, flip xy */
/*TODO*///        cpu_writemem24_word(dst+0x04,READ_WORD(src+0));	/* code */
/*TODO*///        cpu_writemem24_word(dst+0x18,(READ_WORD(src+2) & 0x3ff) ^		/* color, mirror, priority */
/*TODO*///				(READ_WORD( &sunset_104000[CellVar + 0x00] ) & 0x0060));
/*TODO*///
/*TODO*///		/* base color modifier */
/*TODO*///		/* TODO: this is wrong, e.g. it breaks the explosions when you kill an */
/*TODO*///		/* enemy, or surfs in the sewer level (must be blue for all enemies). */
/*TODO*///		/* It fixes the enemies, though, they are not all purple when you throw them around. */
/*TODO*///		/* Also, the bosses don't blink when they are about to die - don't know */
/*TODO*///		/* if this is correct or not. */
/*TODO*/////		if (READ_WORD( &sunset_104000[CellVar + 0x2a] ) & 0x001f)
/*TODO*/////			cpu_writemem24_word(dst+0x18,(cpu_readmem24_word(dst+0x18) & 0xffe0) |
/*TODO*/////					(READ_WORD( &sunset_104000[CellVar + 0x2a] ) & 0x001f));
/*TODO*///
/*TODO*///		x = READ_WORD(src+4);
/*TODO*///		if (READ_WORD( &sunset_104000[CellVar + 0x00] ) & 0x4000)
/*TODO*///		{
/*TODO*///			/* flip x */
/*TODO*///			cpu_writemem24_word(dst+0x00,cpu_readmem24_word(dst+0x00) ^ 0x1000);
/*TODO*///			x = -x;
/*TODO*///		}
/*TODO*///		x += READ_WORD( &sunset_104000[CellVar + 0x0C] );
/*TODO*///		cpu_writemem24_word(dst+0x0c,x);
/*TODO*///		y = READ_WORD(src+6);
/*TODO*///		y += READ_WORD( &sunset_104000[CellVar + 0x0E] );
/*TODO*///		/* don't do second offset for shadows */
/*TODO*///		if ((READ_WORD(&tmnt2_1c0800[0x10]) & 0x00ff) != 0x01)
/*TODO*///			y += READ_WORD( &sunset_104000[CellVar + 0x10] );
/*TODO*///		cpu_writemem24_word(dst+0x08,y);
/*TODO*///#if 0
/*TODO*///if (errorlog) fprintf(errorlog,"copy command %04x sprite %08x data %08x: %04x%04x %04x%04x  modifiers %08x:%04x%04x %04x%04x %04x%04x %04x%04x %04x%04x %04x%04x %04x%04x %04x%04x %04x%04x %04x%04x %04x%04x %04x%04x\n",
/*TODO*///	READ_WORD( &tmnt2_1c0800[0x10] ),
/*TODO*///	CellDest,CellSrc,
/*TODO*///	READ_WORD(src+0),READ_WORD(src+2),READ_WORD(src+4),READ_WORD(src+6),
/*TODO*///	CellVar,
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x00]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x02]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x04]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x06]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x08]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x0a]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x0c]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x0e]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x10]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x12]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x14]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x16]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x18]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x1a]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x1c]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x1e]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x20]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x22]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x24]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x26]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x28]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x2a]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x2c]),
/*TODO*///	READ_WORD( &sunset_104000[CellVar + 0x2e])
/*TODO*///	);
/*TODO*///#endif
/*TODO*/////        }
/*TODO*///    }
/*TODO*///}
/*TODO*///
/*TODO*///static struct MemoryWriteAddress tmnt2_writemem[] =
/*TODO*///{
/*TODO*///	{ 0x000000, 0x07ffff, MWA_ROM },
/*TODO*///	{ 0x104000, 0x107fff, MWA_BANK1, &sunset_104000 },	/* main RAM */
/*TODO*///	{ 0x140000, 0x140fff, paletteram_xBBBBBGGGGGRRRRR_word_w, &paletteram },
/*TODO*///	{ 0x180000, 0x183fff, K053245_scattered_word_w, &spriteram },
/*TODO*///	{ 0x1c0200, 0x1c0201, ssriders_eeprom_w },
/*TODO*///	{ 0x1c0300, 0x1c0301, ssriders_1c0300_w },
/*TODO*///	{ 0x1c0400, 0x1c0401, watchdog_reset_w },
/*TODO*///	{ 0x1c0500, 0x1c057f, MWA_BANK3 },	/* unknown: TMNT2 only (1J) */
/*TODO*///	{ 0x1c0800, 0x1c081f, tmnt2_1c0800_w, &tmnt2_1c0800 },	/* protection device */
/*TODO*///	{ 0x5a0000, 0x5a001f, K053244_word_noA1_w },
/*TODO*///	{ 0x5c0600, 0x5c0601, K053260_WriteReg },
/*TODO*///	{ 0x5c0604, 0x5c0605, ssriders_soundkludge_w },
/*TODO*///	{ 0x5c0700, 0x5c071f, K053251_halfword_w },
/*TODO*///	{ 0x600000, 0x603fff, K052109_word_w },
/*TODO*///	{ -1 }	/* end of table */
/*TODO*///};
/*TODO*///
/*TODO*///
    static MemoryReadAddress ssriders_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x0bffff, MRA_ROM),
                new MemoryReadAddress(0x104000, 0x107fff, MRA_BANK1), /* main RAM */
                new MemoryReadAddress(0x140000, 0x140fff, paletteram_word_r),
                new MemoryReadAddress(0x180000, 0x183fff, K053245_scattered_word_r),
                new MemoryReadAddress(0x1c0000, 0x1c0001, input_port_0_r),
                new MemoryReadAddress(0x1c0002, 0x1c0003, input_port_1_r),
                new MemoryReadAddress(0x1c0004, 0x1c0005, input_port_4_r),
                new MemoryReadAddress(0x1c0006, 0x1c0007, input_port_5_r),
                new MemoryReadAddress(0x1c0100, 0x1c0101, input_port_2_r),
                new MemoryReadAddress(0x1c0102, 0x1c0103, ssriders_eeprom_r),
                new MemoryReadAddress(0x1c0400, 0x1c0401, watchdog_reset_r),
                new MemoryReadAddress(0x1c0500, 0x1c057f, MRA_BANK3), /* TMNT2 only (1J); unknown */
                new MemoryReadAddress(0x1c0800, 0x1c0801, ssriders_kludge), /* protection device */
                new MemoryReadAddress(0x5a0000, 0x5a001f, K053244_word_noA1_r),
                new MemoryReadAddress(0x5c0600, 0x5c0603, punkshot_sound_r), /* K053260 */
                new MemoryReadAddress(0x600000, 0x603fff, K052109_word_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress ssriders_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x0bffff, MWA_ROM),
                new MemoryWriteAddress(0x104000, 0x107fff, MWA_BANK1), /* main RAM */
                new MemoryWriteAddress(0x140000, 0x140fff, paletteram_xBBBBBGGGGGRRRRR_word_w, paletteram),
                new MemoryWriteAddress(0x180000, 0x183fff, K053245_scattered_word_w, spriteram),
                new MemoryWriteAddress(0x1c0200, 0x1c0201, ssriders_eeprom_w),
                new MemoryWriteAddress(0x1c0300, 0x1c0301, ssriders_1c0300_w),
                new MemoryWriteAddress(0x1c0400, 0x1c0401, watchdog_reset_w),
                new MemoryWriteAddress(0x1c0500, 0x1c057f, MWA_BANK3), /* TMNT2 only (1J); unknown */
                //	new MemoryWriteAddress( 0x1c0800, 0x1c081f,  ),	/* protection device */
                new MemoryWriteAddress(0x5a0000, 0x5a001f, K053244_word_noA1_w),
                new MemoryWriteAddress(0x5c0600, 0x5c0601, K053260_WriteReg),
                new MemoryWriteAddress(0x5c0604, 0x5c0605, ssriders_soundkludge_w),
                new MemoryWriteAddress(0x5c0700, 0x5c071f, K053251_halfword_w),
                new MemoryWriteAddress(0x600000, 0x603fff, K052109_word_w),
                new MemoryWriteAddress(-1) /* end of table */};
    static MemoryReadAddress thndrx2_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x03ffff, MRA_ROM),
                new MemoryReadAddress(0x100000, 0x103fff, MRA_BANK1), /* main RAM */
                new MemoryReadAddress(0x200000, 0x200fff, paletteram_word_r),
                new MemoryReadAddress(0x400000, 0x400003, punkshot_sound_r), /* K053260 */
                new MemoryReadAddress(0x500000, 0x50003f, K054000_halfword_r),
                new MemoryReadAddress(0x500200, 0x500201, thndrx2_in0_r),
                new MemoryReadAddress(0x500202, 0x500203, thndrx2_eeprom_r),
                new MemoryReadAddress(0x600000, 0x607fff, K052109_word_noA12_r),
                new MemoryReadAddress(0x700000, 0x700007, K051937_word_r),
                new MemoryReadAddress(0x700400, 0x7007ff, K051960_word_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress thndrx2_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x03ffff, MWA_ROM),
                new MemoryWriteAddress(0x100000, 0x103fff, MWA_BANK1), /* main RAM */
                new MemoryWriteAddress(0x200000, 0x200fff, paletteram_xBBBBBGGGGGRRRRR_word_w, paletteram),
                new MemoryWriteAddress(0x300000, 0x30001f, K053251_halfword_w),
                new MemoryWriteAddress(0x400000, 0x400001, K053260_WriteReg),
                new MemoryWriteAddress(0x500000, 0x50003f, K054000_halfword_w),
                new MemoryWriteAddress(0x500100, 0x500101, thndrx2_eeprom_w),
                new MemoryWriteAddress(0x500300, 0x500301, MWA_NOP), /* watchdog reset? irq enable? */
                new MemoryWriteAddress(0x600000, 0x607fff, K052109_word_noA12_w),
                new MemoryWriteAddress(0x700000, 0x700007, K051937_word_w),
                new MemoryWriteAddress(0x700400, 0x7007ff, K051960_word_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress mia_s_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x87ff, MRA_RAM),
                new MemoryReadAddress(0xa000, 0xa000, soundlatch_r),
                new MemoryReadAddress(0xb000, 0xb00d, K007232_read_port_0_r),
                new MemoryReadAddress(0xc001, 0xc001, YM2151_status_port_0_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress mia_s_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM),
                new MemoryWriteAddress(0xb000, 0xb00d, K007232_write_port_0_w),
                new MemoryWriteAddress(0xc000, 0xc000, YM2151_register_port_0_w),
                new MemoryWriteAddress(0xc001, 0xc001, YM2151_data_port_0_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress tmnt_s_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x87ff, MRA_RAM),
                new MemoryReadAddress(0x9000, 0x9000, tmnt_sres_r), /* title music  UPD7759C reset */
                new MemoryReadAddress(0xa000, 0xa000, soundlatch_r),
                new MemoryReadAddress(0xb000, 0xb00d, K007232_read_port_0_r),
                new MemoryReadAddress(0xc001, 0xc001, YM2151_status_port_0_r),
                new MemoryReadAddress(0xf000, 0xf000, UPD7759_busy_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress tmnt_s_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM),
                new MemoryWriteAddress(0x9000, 0x9000, tmnt_sres_w), /* title music  UPD7759C reset */
                new MemoryWriteAddress(0xb000, 0xb00d, K007232_write_port_0_w),
                new MemoryWriteAddress(0xc000, 0xc000, YM2151_register_port_0_w),
                new MemoryWriteAddress(0xc001, 0xc001, YM2151_data_port_0_w),
                new MemoryWriteAddress(0xd000, 0xd000, UPD7759_message_w),
                new MemoryWriteAddress(0xe000, 0xe000, UPD7759_start_w),
                new MemoryWriteAddress(-1) /* end of table */};
    static MemoryReadAddress punkshot_s_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0xf000, 0xf7ff, MRA_RAM),
                new MemoryReadAddress(0xf801, 0xf801, YM2151_status_port_0_r),
                new MemoryReadAddress(0xfc00, 0xfc2f, K053260_ReadReg),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress punkshot_s_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0xf000, 0xf7ff, MWA_RAM),
                new MemoryWriteAddress(0xf800, 0xf800, YM2151_register_port_0_w),
                new MemoryWriteAddress(0xf801, 0xf801, YM2151_data_port_0_w),
                new MemoryWriteAddress(0xfa00, 0xfa00, sound_arm_nmi),
                new MemoryWriteAddress(0xfc00, 0xfc2f, K053260_WriteReg),
                new MemoryWriteAddress(-1) /* end of table */};
    static MemoryReadAddress lgtnfght_s_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x87ff, MRA_RAM),
                new MemoryReadAddress(0xa001, 0xa001, YM2151_status_port_0_r),
                new MemoryReadAddress(0xc000, 0xc02f, K053260_ReadReg),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress lgtnfght_s_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM),
                new MemoryWriteAddress(0xa000, 0xa000, YM2151_register_port_0_w),
                new MemoryWriteAddress(0xa001, 0xa001, YM2151_data_port_0_w),
                new MemoryWriteAddress(0xc000, 0xc02f, K053260_WriteReg),
                new MemoryWriteAddress(-1) /* end of table */};
    /*TODO*///
/*TODO*///static struct MemoryReadAddress glfgreat_s_readmem[] =
/*TODO*///{
/*TODO*///	{ 0x0000, 0x7fff, MRA_ROM },
/*TODO*///	{ 0xf000, 0xf7ff, MRA_RAM },
/*TODO*///	{ 0xf800, 0xf82f, K053260_ReadReg },
/*TODO*///	{ -1 }	/* end of table */
/*TODO*///};
/*TODO*///
/*TODO*///static struct MemoryWriteAddress glfgreat_s_writemem[] =
/*TODO*///{
/*TODO*///	{ 0x0000, 0x7fff, MWA_ROM },
/*TODO*///	{ 0xf000, 0xf7ff, MWA_RAM },
/*TODO*///	{ 0xf800, 0xf82f, K053260_WriteReg },
/*TODO*///	{ 0xfa00, 0xfa00, sound_arm_nmi },
/*TODO*///	{ -1 }	/* end of table */
/*TODO*///};
/*TODO*///
    static MemoryReadAddress ssriders_s_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0xefff, MRA_ROM),
                new MemoryReadAddress(0xf000, 0xf7ff, MRA_RAM),
                new MemoryReadAddress(0xf801, 0xf801, YM2151_status_port_0_r),
                new MemoryReadAddress(0xfa00, 0xfa2f, K053260_ReadReg),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress ssriders_s_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xefff, MWA_ROM),
                new MemoryWriteAddress(0xf000, 0xf7ff, MWA_RAM),
                new MemoryWriteAddress(0xf800, 0xf800, YM2151_register_port_0_w),
                new MemoryWriteAddress(0xf801, 0xf801, YM2151_data_port_0_w),
                new MemoryWriteAddress(0xfa00, 0xfa2f, K053260_WriteReg),
                new MemoryWriteAddress(0xfc00, 0xfc00, sound_arm_nmi),
                new MemoryWriteAddress(-1) /* end of table */};
    static MemoryReadAddress thndrx2_s_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0xefff, MRA_ROM),
                new MemoryReadAddress(0xf000, 0xf7ff, MRA_RAM),
                new MemoryReadAddress(0xf801, 0xf801, YM2151_status_port_0_r),
                new MemoryReadAddress(0xfc00, 0xfc2f, K053260_ReadReg),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress thndrx2_s_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xefff, MWA_ROM),
                new MemoryWriteAddress(0xf000, 0xf7ff, MWA_RAM),
                new MemoryWriteAddress(0xf800, 0xf800, YM2151_register_port_0_w),
                new MemoryWriteAddress(0xf801, 0xf801, YM2151_data_port_0_w),
                new MemoryWriteAddress(0xf811, 0xf811, YM2151_data_port_0_w), /* mirror */
                new MemoryWriteAddress(0xfa00, 0xfa00, sound_arm_nmi),
                new MemoryWriteAddress(0xfc00, 0xfc2f, K053260_WriteReg),
                new MemoryWriteAddress(-1) /* end of table */};
    static InputPortPtr input_ports_mia = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* COINS */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();       /* PLAYER 1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1 | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1 | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1 | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();       /* PLAYER 2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* DSW1 */

            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x02, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x05, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x01, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x07, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x06, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x09, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x20, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x50, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x10, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x70, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x60, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0xa0, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x90, DEF_STR("1C_7C"));
            //	PORT_DIPSETTING(    0x00, "Invalid" );

            PORT_START(); 	/* DSW2 */

            PORT_DIPNAME(0x03, 0x02, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "2");
            PORT_DIPSETTING(0x02, "3");
            PORT_DIPSETTING(0x01, "5");
            PORT_DIPSETTING(0x00, "7");
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x18, 0x18, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x18, "30000 80000");
            PORT_DIPSETTING(0x10, "50000 100000");
            PORT_DIPSETTING(0x08, "50000");
            PORT_DIPSETTING(0x00, "100000");
            PORT_DIPNAME(0x60, 0x40, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x60, "Easy");
            PORT_DIPSETTING(0x40, "Normal");
            PORT_DIPSETTING(0x20, "Difficult");
            PORT_DIPSETTING(0x00, "Very Difficult");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START(); 	/* DSW3 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, "Character Test");
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_SERVICE(0x04, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };
    static InputPortPtr input_ports_tmnt = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* COINS */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN4);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_SERVICE1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_SERVICE2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_SERVICE3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_SERVICE4);

            PORT_START();       /* PLAYER 1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1 | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1 | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1 | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* button 3 - unused */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();       /* PLAYER 2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* button 3 - unused */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START();       /* PLAYER 3 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER3 | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER3 | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER3 | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER3 | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* button 3 - unused */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START3);

            PORT_START(); 	/* DSW1 */

            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x05, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x01, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x07, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x06, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x09, DEF_STR("1C_7C"));

            PORT_START(); 	/* DSW2 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "1");
            PORT_DIPSETTING(0x02, "2");
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x60, 0x40, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x60, "Easy");
            PORT_DIPSETTING(0x40, "Normal");
            PORT_DIPSETTING(0x20, "Difficult");
            PORT_DIPSETTING(0x00, "Very Difficult");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();       /* PLAYER 4 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER4 | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER4 | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER4 | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER4 | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* button 3 - unused */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START4);

            PORT_START(); 	/* DSW3 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_SERVICE(0x04, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_tmnt2p = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* COINS */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN4);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();       /* PLAYER 1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1 | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1 | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1 | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* button 3 - unused */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();       /* PLAYER 2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* button 3 - unused */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START();       /* PLAYER 3 */
            //	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER3 | IPF_8WAY );
            //	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER3 | IPF_8WAY );
            //	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER3 | IPF_8WAY );
            //	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER3 | IPF_8WAY );
            //	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );
            //	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
            //	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* button 3 - unused */
            //	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START3 );

            PORT_START(); 	/* DSW1 */

            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x02, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x05, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x01, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x07, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x06, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x09, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x20, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x50, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x10, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x70, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x60, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0xa0, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x90, DEF_STR("1C_7C"));
            //	PORT_DIPSETTING(    0x00, "Invalid" );

            PORT_START(); 	/* DSW2 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "1");
            PORT_DIPSETTING(0x02, "2");
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x60, 0x40, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x60, "Easy");
            PORT_DIPSETTING(0x40, "Normal");
            PORT_DIPSETTING(0x20, "Difficult");
            PORT_DIPSETTING(0x00, "Very Difficult");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();       /* PLAYER 4 */
            //	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER4 | IPF_8WAY );
            //	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER4 | IPF_8WAY );
            //	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER4 | IPF_8WAY );
            //	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER4 | IPF_8WAY );
            //	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 );
            //	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );
            //	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* button 3 - unused */
            //	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START4 );

            PORT_START(); 	/* DSW3 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_SERVICE(0x04, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_punkshot = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* DSW1/DSW2 */

            PORT_DIPNAME(0x000f, 0x000f, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x0000, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x0002, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x0005, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x0008, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0004, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x0001, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x000f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0003, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x0007, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x000e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0006, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x000d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x000c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x000b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x000a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x0009, DEF_STR("1C_7C"));
            PORT_DIPNAME(0x0010, 0x0010, "Continue");
            PORT_DIPSETTING(0x0010, "Normal");
            PORT_DIPSETTING(0x0000, "1 Coin");
            PORT_DIPNAME(0x0020, 0x0020, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0020, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0040, 0x0040, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0040, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0080, 0x0080, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0080, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0300, 0x0300, "Energy");
            PORT_DIPSETTING(0x0300, "30");
            PORT_DIPSETTING(0x0200, "40");
            PORT_DIPSETTING(0x0100, "50");
            PORT_DIPSETTING(0x0000, "60");
            PORT_DIPNAME(0x0c00, 0x0c00, "Period Length");
            PORT_DIPSETTING(0x0c00, "2 Minutes");
            PORT_DIPSETTING(0x0800, "3 Minutes");
            PORT_DIPSETTING(0x0400, "4 Minutes");
            PORT_DIPSETTING(0x0000, "5 Minutes");
            PORT_DIPNAME(0x1000, 0x1000, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x1000, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x6000, 0x6000, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x6000, "Easy");
            PORT_DIPSETTING(0x4000, "Medium");
            PORT_DIPSETTING(0x2000, "Hard");
            PORT_DIPSETTING(0x0000, "Hardest");
            PORT_DIPNAME(0x8000, 0x0000, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x8000, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));

            PORT_START(); 	/* COIN/DSW3 */

            PORT_BIT(0x0001, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x0002, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x0004, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x0008, IP_ACTIVE_LOW, IPT_COIN4);
            PORT_BIT(0x0010, IP_ACTIVE_LOW, IPT_SERVICE1);
            PORT_BIT(0x0020, IP_ACTIVE_LOW, IPT_SERVICE2);
            PORT_BIT(0x0040, IP_ACTIVE_LOW, IPT_SERVICE3);
            PORT_BIT(0x0080, IP_ACTIVE_LOW, IPT_SERVICE4);
            PORT_BIT(0x0100, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0200, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0400, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0800, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_DIPNAME(0x1000, 0x1000, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x1000, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x2000, 0x2000, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x2000, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_SERVICE(0x4000, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x8000, 0x8000, "Freeze");
            PORT_DIPSETTING(0x8000, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));

            PORT_START(); 	/* IN0/IN1 */

            PORT_BIT(0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN2/IN3 */

            PORT_BIT(0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3);
            PORT_BIT(0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3);
            PORT_BIT(0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER4);
            PORT_BIT(0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4);
            PORT_BIT(0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER4);
            PORT_BIT(0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER4);
            PORT_BIT(0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4);
            PORT_BIT(0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4);
            PORT_BIT(0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_punksht2 = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* DSW1/DSW2 */

            PORT_DIPNAME(0x000f, 0x000f, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x0000, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x0002, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x0005, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x0008, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0004, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x0001, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x000f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0003, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x0007, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x000e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0006, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x000d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x000c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x000b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x000a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x0009, DEF_STR("1C_7C"));
            PORT_DIPNAME(0x0010, 0x0010, "Continue");
            PORT_DIPSETTING(0x0010, "Normal");
            PORT_DIPSETTING(0x0000, "1 Coin");
            PORT_DIPNAME(0x0020, 0x0020, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0020, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0040, 0x0040, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0040, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0080, 0x0080, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0080, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0300, 0x0300, "Energy");
            PORT_DIPSETTING(0x0300, "40");
            PORT_DIPSETTING(0x0200, "50");
            PORT_DIPSETTING(0x0100, "60");
            PORT_DIPSETTING(0x0000, "70");
            PORT_DIPNAME(0x0c00, 0x0c00, "Period Length");
            PORT_DIPSETTING(0x0c00, "3 Minutes");
            PORT_DIPSETTING(0x0800, "4 Minutes");
            PORT_DIPSETTING(0x0400, "5 Minutes");
            PORT_DIPSETTING(0x0000, "6 Minutes");
            PORT_DIPNAME(0x1000, 0x1000, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x1000, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x6000, 0x6000, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x6000, "Easy");
            PORT_DIPSETTING(0x4000, "Medium");
            PORT_DIPSETTING(0x2000, "Hard");
            PORT_DIPSETTING(0x0000, "Hardest");
            PORT_DIPNAME(0x8000, 0x0000, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x8000, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));

            PORT_START(); 	/* COIN/DSW3 */

            PORT_BIT(0x0001, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x0002, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x0004, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0008, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0010, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x0020, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0100, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x0200, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x0400, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0800, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_DIPNAME(0x1000, 0x1000, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x1000, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x2000, 0x2000, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x2000, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_SERVICE(0x4000, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x8000, 0x8000, "Freeze");
            PORT_DIPSETTING(0x8000, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));

            PORT_START(); 	/* IN0/IN1 */

            PORT_BIT(0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };
    static InputPortPtr input_ports_ssridr4p = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* COIN */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN4);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_SERVICE1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_SERVICE2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_SERVICE3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_SERVICE4);

            PORT_START(); 	/* EEPROM and service */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* EEPROM data */

            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* EEPROM status? - always 1 */

            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);/* ?? TMNT2: OBJMPX */

            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);/* ?? TMNT2: NVBLK */

            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);/* ?? TMNT2: IPL0 */

            PORT_BIT(0x60, IP_ACTIVE_LOW, IPT_UNKNOWN);/* unused? */

            PORT_BITX(0x80, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN3 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER4);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER4);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER4);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_ssriders = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START(); 	/* COIN */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN4);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* EEPROM and service */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* EEPROM data */

            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* EEPROM status? - always 1 */

            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);/* ?? TMNT2: OBJMPX */

            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);/* ?? TMNT2: NVBLK */

            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);/* ?? TMNT2: IPL0 */

            PORT_BIT(0x60, IP_ACTIVE_LOW, IPT_UNKNOWN);/* unused? */

            PORT_BITX(0x80, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_tmnt2a = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START(); 	/* COIN */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN4);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_SERVICE1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_SERVICE2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_SERVICE3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_SERVICE4);

            PORT_START(); 	/* EEPROM and service */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* EEPROM data */

            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* EEPROM status? - always 1 */

            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);/* ?? TMNT2: OBJMPX */

            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);/* ?? TMNT2: NVBLK */

            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);/* ?? TMNT2: IPL0 */

            PORT_BIT(0x60, IP_ACTIVE_LOW, IPT_UNKNOWN);/* unused? */

            PORT_BITX(0x80, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START3);

            PORT_START(); 	/* IN3 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER4);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER4);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER4);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START4);
            INPUT_PORTS_END();
        }
    };
    static InputPortPtr input_ports_lgtnfght = new InputPortPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* vblank? checked during boot */

            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2);

            PORT_START(); 	/* DSW1 */

            PORT_DIPNAME(0x03, 0x02, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "2");
            PORT_DIPSETTING(0x02, "3");
            PORT_DIPSETTING(0x01, "5");
            PORT_DIPSETTING(0x00, "7");
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x18, 0x18, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x18, "100000 400000");
            PORT_DIPSETTING(0x10, "150000 500000");
            PORT_DIPSETTING(0x08, "200000");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0x60, 0x40, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x60, "Easy");
            PORT_DIPSETTING(0x40, "Medium");
            PORT_DIPSETTING(0x20, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START(); 	/* DSW2 */

            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x02, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x05, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x01, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x07, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x06, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x09, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x20, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x50, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x10, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x70, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x60, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0xa0, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x90, DEF_STR("1C_7C"));
            //	PORT_DIPSETTING(    0x00, "Invalid" );

            PORT_START(); 	/* DSW3 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, "Sound");
            PORT_DIPSETTING(0x02, "Mono");
            PORT_DIPSETTING(0x00, "Stereo");
            PORT_SERVICE(0x04, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_detatwin = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* COIN */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* VBLANK? OBJMPX? */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* EEPROM */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* EEPROM data */

            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* EEPROM status? - always 1 */

            PORT_BIT(0xfc, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_glfgreat = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1);
            PORT_BIT(0x0080, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1);
            PORT_BIT(0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x8000, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2);

            PORT_START(); 	/* IN1 */

            PORT_BIT(0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3);
            PORT_BIT(0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3);
            PORT_BIT(0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER3);
            PORT_BIT(0x0080, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER3);
            PORT_BIT(0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER4);
            PORT_BIT(0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4);
            PORT_BIT(0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER4);
            PORT_BIT(0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER4);
            PORT_BIT(0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4);
            PORT_BIT(0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4);
            PORT_BIT(0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER4);
            PORT_BIT(0x8000, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER4);

            PORT_START();
            PORT_DIPNAME(0x000f, 0x000f, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x0002, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x0005, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x0008, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0004, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x0001, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x000f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0003, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x0007, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x000e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0006, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x000d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x000c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x000b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x000a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x0009, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x0000, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x00f0, 0x00f0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x0020, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x0050, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x0080, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0040, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x0010, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x00f0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0030, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x0070, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x00e0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0060, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x00d0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x00c0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x00b0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x00a0, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x0090, DEF_STR("1C_7C"));
            //	PORT_DIPSETTING(      0x0000, "Invalid" );
            PORT_DIPNAME(0x0300, 0x0100, "Players/Controllers");
            PORT_DIPSETTING(0x0300, "4/1");
            PORT_DIPSETTING(0x0200, "4/2");
            PORT_DIPSETTING(0x0100, "4/4");
            PORT_DIPSETTING(0x0000, "3/3");
            PORT_DIPNAME(0x0400, 0x0000, "Sound");
            PORT_DIPSETTING(0x0400, "Mono");
            PORT_DIPSETTING(0x0000, "Stereo");
            PORT_DIPNAME(0x1800, 0x1800, "Initial/Maximum Credit");
            PORT_DIPSETTING(0x1800, "2/3");
            PORT_DIPSETTING(0x1000, "2/4");
            PORT_DIPSETTING(0x0800, "2/5");
            PORT_DIPSETTING(0x0000, "3/5");
            PORT_DIPNAME(0x6000, 0x4000, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x6000, "Easy");
            PORT_DIPSETTING(0x4000, "Normal");
            PORT_DIPSETTING(0x2000, "Hard");
            PORT_DIPSETTING(0x0000, "Hardest");
            PORT_DIPNAME(0x8000, 0x0000, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x8000, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));

            PORT_START();
            PORT_BIT(0x0001, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x0002, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x0004, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x0008, IP_ACTIVE_LOW, IPT_COIN4);
            PORT_BIT(0x0010, IP_ACTIVE_LOW, IPT_SERVICE1);/* service coin */

            PORT_BIT(0x0020, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0100, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x0200, IP_ACTIVE_LOW, IPT_START2);
            PORT_BITX(0x0400, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);
            PORT_DIPNAME(0x8000, 0x0000, "Freeze");/* ?? VBLANK ?? */

            PORT_DIPSETTING(0x0000, DEF_STR("Off"));
            PORT_DIPSETTING(0x8000, DEF_STR("On"));
            PORT_DIPNAME(0x1000, 0x1000, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x1000, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x2000, 0x2000, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x2000, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            //	PORT_SERVICE( 0x4000, IP_ACTIVE_LOW );
            PORT_BIT(0x4000, IP_ACTIVE_LOW, IPT_SERVICE);
            PORT_DIPNAME(0x8000, 0x8000, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x8000, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_thndrx2 = new InputPortPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0080, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x0100, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x0200, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x0400, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BITX(0x0800, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);
            PORT_BIT(0x1000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* EEPROM and service */

            PORT_BIT(0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0080, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x0100, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* EEPROM data */

            PORT_BIT(0x0200, IP_ACTIVE_LOW, IPT_UNKNOWN);/* EEPROM status? - always 1 */

            PORT_BIT(0x0400, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0800, IP_ACTIVE_LOW, IPT_UNKNOWN);/* VBLK?? */

            PORT_BIT(0x1000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static YM2151interface ym2151_interface = new YM2151interface(
            1, /* 1 chip */
            3579545, /* 3.579545 MHz */
            new int[]{YM3012_VOL(100, MIXER_PAN_LEFT, 100, MIXER_PAN_RIGHT)},
            new WriteYmHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );

    public static portwritehandlerPtr volume_callback = new portwritehandlerPtr() {
        public void handler(int v) {
            K007232_set_volume(0, 0, (v >> 4) * 0x11, 0);
            K007232_set_volume(0, 1, 0, (v & 0x0f) * 0x11);
        }
    };

    static K007232_interface k007232_interface = new K007232_interface(
            1, /* number of chips */
            new int[]{REGION_SOUND1}, /* memory regions */
            new int[]{K007232_VOL(20, MIXER_PAN_CENTER, 20, MIXER_PAN_CENTER)}, /* volume */
            new portwritehandlerPtr[]{volume_callback} /* external port callback */
    );

    static UPD7759_interface upd7759_interface = new UPD7759_interface(
            1, /* number of chips */
            UPD7759_STANDARD_CLOCK,
            new int[]{60}, /* volume */
            new int[]{REGION_SOUND2}, /* memory region */
            UPD7759_STANDALONE_MODE, /* chip mode */
            new irqcallbackPtr[]{null}
    );
static Samplesinterface samples_interface = new Samplesinterface(
	1,	/* 1 channel for the title music */
	25,	/* volume */
        null
);

    static CustomSound_interface custom_interface = new CustomSound_interface(
	tmnt_decode_sample,
	null,
	null
    );
    static K053260_interface k053260_interface_nmi = new K053260_interface(
            3579545,
            REGION_SOUND1, /* memory region */
            new int[]{MIXER(70, MIXER_PAN_LEFT), MIXER(70, MIXER_PAN_RIGHT)},
            null//sound_nmi_callback
    );
    static K053260_interface k053260_interface = new K053260_interface(
            3579545,
            REGION_SOUND1, /* memory region */
            new int[]{MIXER(70, MIXER_PAN_LEFT), MIXER(70, MIXER_PAN_RIGHT)},
            null
    );
    /*TODO*///
/*TODO*///static struct K053260_interface glfgreat_k053260_interface =
/*TODO*///{
/*TODO*///	3579545,
/*TODO*///	REGION_SOUND1, /* memory region */
/*TODO*///	{ MIXER(100,MIXER_PAN_LEFT), MIXER(100,MIXER_PAN_RIGHT) },
/*TODO*/////	sound_nmi_callback,
/*TODO*///};
/*TODO*///
/*TODO*///
    static MachineDriver machine_driver_mia = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        8000000, /* 8 MHz */
                        mia_readmem, mia_writemem, null, null,
                        m68_level5_irq, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        3579545, /* 3.579545 MHz */
                        mia_s_readmem, mia_s_writemem, null, null,
                        ignore_interrupt, 0 /* IRQs are triggered by the main CPU */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            64 * 8, 32 * 8, new rectangle(14 * 8, (64 - 14) * 8 - 1, 2 * 8, 30 * 8 - 1),
            null, /* gfx decoded by konamiic.c */
            1024, 1024,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            mia_vh_start,
            punkshot_vh_stop,
            mia_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface
                ),
                new MachineSound(
                        SOUND_K007232,
                        k007232_interface
                )
            }
    );
    static MachineDriver machine_driver_tmnt = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        8000000, /* 8 MHz */
                        tmnt_readmem, tmnt_writemem, null, null,
                        m68_level5_irq, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        3579545, /* 3.579545 MHz */
                        tmnt_s_readmem, tmnt_s_writemem, null, null,
                        ignore_interrupt, 0 /* IRQs are triggered by the main CPU */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            64 * 8, 32 * 8, new rectangle(14 * 8, (64 - 14) * 8 - 1, 2 * 8, 30 * 8 - 1),
            null, /* gfx decoded by konamiic.c */
            1024, 1024,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            tmnt_vh_start,
            punkshot_vh_stop,
            tmnt_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface
                ),
                new MachineSound(
                        SOUND_K007232,
                        k007232_interface
                ),
                new MachineSound(
                        SOUND_UPD7759,
                        upd7759_interface
                ),
                new MachineSound(
         				SOUND_SAMPLES,
	 			samples_interface
	 		),
		 	new MachineSound(
		 		SOUND_CUSTOM,	/* actually initializes the samples */
		 		custom_interface
		 	)
            }
    );
    static MachineDriver machine_driver_punkshot = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        12000000, /* CPU is 68000/12, but this doesn't necessarily mean it's */
                        /* running at 12MHz. TMNT uses 8MHz */
                        punkshot_readmem, punkshot_writemem, null, null,
                        punkshot_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        3579545, /* 3.579545 MHz */
                        punkshot_s_readmem, punkshot_s_writemem, null, null,
                        ignore_interrupt, 0 /* IRQs are triggered by the main CPU */
                /* NMIs are generated by the 053260 */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            64 * 8, 32 * 8, new rectangle(14 * 8, (64 - 14) * 8 - 1, 2 * 8, 30 * 8 - 1),
            null, /* gfx decoded by konamiic.c */
            2048, 2048,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            punkshot_vh_start,
            punkshot_vh_stop,
            punkshot_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface
                ),
                new MachineSound(
                        SOUND_K053260,
                        k053260_interface_nmi
                )
            }
    );
    static MachineDriver machine_driver_lgtnfght = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        12000000, /* 12 MHz */
                        lgtnfght_readmem, lgtnfght_writemem, null, null,
                        lgtnfght_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        3579545, /* 3.579545 MHz */
                        lgtnfght_s_readmem, lgtnfght_s_writemem, null, null,
                        ignore_interrupt, 0 /* IRQs are triggered by the main CPU */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            64 * 8, 32 * 8, new rectangle(14 * 8, (64 - 14) * 8 - 1, 2 * 8, 30 * 8 - 1),
            null, /* gfx decoded by konamiic.c */
            2048, 2048,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            lgtnfght_vh_start,
            lgtnfght_vh_stop,
            lgtnfght_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,//SOUND_SUPPORTS_STEREO,0,0,0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface
                ),
                new MachineSound(
                        SOUND_K053260,
                        k053260_interface
                )
            }
    );
    /*TODO*///
/*TODO*///static struct MachineDriver machine_driver_detatwin =
/*TODO*///{
/*TODO*///	/* basic machine hardware */
/*TODO*///	{
/*TODO*///		{
/*TODO*///			CPU_M68000,
/*TODO*///			16000000,	/* 16 MHz */
/*TODO*///			detatwin_readmem,detatwin_writemem,0,0,
/*TODO*///			punkshot_interrupt,1
/*TODO*///		},
/*TODO*///		{
/*TODO*///			CPU_Z80 | CPU_AUDIO_CPU,
/*TODO*///			3579545,	/* ????? */
/*TODO*///			ssriders_s_readmem,ssriders_s_writemem,0,0,
/*TODO*///			ignore_interrupt,0	/* IRQs are triggered by the main CPU */
/*TODO*///								/* NMIs are generated by the 053260 */
/*TODO*///		}
/*TODO*///	},
/*TODO*///	60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
/*TODO*///	1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
/*TODO*///	0,
/*TODO*///
/*TODO*///	/* video hardware */
/*TODO*///	64*8, 32*8, { 14*8, (64-14)*8-1, 2*8, 30*8-1 },
/*TODO*///	0,	/* gfx decoded by konamiic.c */
/*TODO*///	2048, 2048,
/*TODO*///	0,
/*TODO*///
/*TODO*///	VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
/*TODO*///	0,
/*TODO*///	detatwin_vh_start,
/*TODO*///	detatwin_vh_stop,
/*TODO*///	lgtnfght_vh_screenrefresh,
/*TODO*///
/*TODO*///	/* sound hardware */
/*TODO*///	SOUND_SUPPORTS_STEREO,0,0,0,
/*TODO*///	{
/*TODO*///		{
/*TODO*///			SOUND_YM2151,
/*TODO*///			&ym2151_interface
/*TODO*///		},
/*TODO*///		{
/*TODO*///			SOUND_K053260,
/*TODO*///			&k053260_interface_nmi
/*TODO*///		}
/*TODO*///	},
/*TODO*///
/*TODO*///	nvram_handler
/*TODO*///};
/*TODO*///
/*TODO*///static struct MachineDriver machine_driver_glfgreat =
/*TODO*///{
/*TODO*///	/* basic machine hardware */
/*TODO*///	{
/*TODO*///		{
/*TODO*///			CPU_M68000,
/*TODO*///			12000000,	/* ? */
/*TODO*///			glfgreat_readmem,glfgreat_writemem,0,0,
/*TODO*///			lgtnfght_interrupt,1
/*TODO*///		},
/*TODO*///		{
/*TODO*///			CPU_Z80 | CPU_AUDIO_CPU,
/*TODO*///			3579545,	/* ? */
/*TODO*///			glfgreat_s_readmem,glfgreat_s_writemem,0,0,
/*TODO*///			ignore_interrupt,0	/* IRQs are triggered by the main CPU */
/*TODO*///								/* NMIs are generated by the 053260 */
/*TODO*///		}
/*TODO*///	},
/*TODO*///	60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
/*TODO*///	1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
/*TODO*///	0,
/*TODO*///
/*TODO*///	/* video hardware */
/*TODO*///	64*8, 32*8, { 14*8, (64-14)*8-1, 2*8, 30*8-1 },
/*TODO*///	0,	/* gfx decoded by konamiic.c */
/*TODO*///	2048, 2048,
/*TODO*///	0,
/*TODO*///
/*TODO*///	VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
/*TODO*///	0,
/*TODO*///	glfgreat_vh_start,
/*TODO*///	glfgreat_vh_stop,
/*TODO*///	glfgreat_vh_screenrefresh,
/*TODO*///
/*TODO*///	/* sound hardware */
/*TODO*///	SOUND_SUPPORTS_STEREO,0,0,0,
/*TODO*///	{
/*TODO*///		{
/*TODO*///			SOUND_K053260,
/*TODO*///			&glfgreat_k053260_interface
/*TODO*///		}
/*TODO*///	}
/*TODO*///};
/*TODO*///
/*TODO*///static struct MachineDriver machine_driver_tmnt2 =
/*TODO*///{
/*TODO*///	/* basic machine hardware */
/*TODO*///	{
/*TODO*///		{
/*TODO*///			CPU_M68000,
/*TODO*///			16000000,	/* 16 MHz */
/*TODO*///			tmnt2_readmem,tmnt2_writemem,0,0,
/*TODO*///			punkshot_interrupt,1
/*TODO*///		},
/*TODO*///		{
/*TODO*///			CPU_Z80 | CPU_AUDIO_CPU,
/*TODO*///			2*3579545,	/* makes the ROM test sync */
/*TODO*///			ssriders_s_readmem,ssriders_s_writemem,0,0,
/*TODO*///			ignore_interrupt,0	/* IRQs are triggered by the main CPU */
/*TODO*///								/* NMIs are generated by the 053260 */
/*TODO*///		}
/*TODO*///	},
/*TODO*///	60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
/*TODO*///	1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
/*TODO*///	0,
/*TODO*///
/*TODO*///	/* video hardware */
/*TODO*///	64*8, 32*8, { 14*8, (64-14)*8-1, 2*8, 30*8-1 },
/*TODO*///	0,	/* gfx decoded by konamiic.c */
/*TODO*///	2048, 2048,
/*TODO*///	0,
/*TODO*///
/*TODO*///	VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
/*TODO*///	0,
/*TODO*///	lgtnfght_vh_start,
/*TODO*///	lgtnfght_vh_stop,
/*TODO*///	lgtnfght_vh_screenrefresh,
/*TODO*///
/*TODO*///	/* sound hardware */
/*TODO*///	SOUND_SUPPORTS_STEREO,0,0,0,
/*TODO*///	{
/*TODO*///		{
/*TODO*///			SOUND_YM2151,
/*TODO*///			&ym2151_interface
/*TODO*///		},
/*TODO*///		{
/*TODO*///			SOUND_K053260,
/*TODO*///			&k053260_interface_nmi
/*TODO*///		}
/*TODO*///	},
/*TODO*///
/*TODO*///	nvram_handler
/*TODO*///};
/*TODO*///
    static MachineDriver machine_driver_ssriders = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        16000000, /* 16 MHz */
                        ssriders_readmem, ssriders_writemem, null, null,
                        punkshot_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000, /* ????? makes the ROM test sync */
                        ssriders_s_readmem, ssriders_s_writemem, null, null,
                        ignore_interrupt, 0 /* IRQs are triggered by the main CPU */
                /* NMIs are generated by the 053260 */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            64 * 8, 32 * 8, new rectangle(14 * 8, (64 - 14) * 8 - 1, 2 * 8, 30 * 8 - 1),
            null, /* gfx decoded by konamiic.c */
            2048, 2048,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            lgtnfght_vh_start,
            lgtnfght_vh_stop,
            ssriders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,//SOUND_SUPPORTS_STEREO,0,0,0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface
                ),
                new MachineSound(
                        SOUND_K053260,
                        k053260_interface_nmi
                )
            },
            nvram_handler
    );
    static MachineDriver machine_driver_thndrx2 = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        12000000, /* 12 MHz */
                        thndrx2_readmem, thndrx2_writemem, null, null,
                        punkshot_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        3579545, /* ????? */
                        thndrx2_s_readmem, thndrx2_s_writemem, null, null,
                        ignore_interrupt, 0 /* IRQs are triggered by the main CPU */
                /* NMIs are generated by the 053260 */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            64 * 8, 32 * 8, new rectangle(14 * 8, (64 - 14) * 8 - 1, 2 * 8, 30 * 8 - 1),
            null, /* gfx decoded by konamiic.c */
            2048, 2048,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            thndrx2_vh_start,
            thndrx2_vh_stop,
            thndrx2_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,//SOUND_SUPPORTS_STEREO,0,0,0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface
                ),
                new MachineSound(
                        SOUND_K053260,
                        k053260_interface_nmi
                )
            },
            thndrx2_nvram_handler
    );

    /**
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadPtr rom_mia = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);/* 2*128k and 2*64k for 68000 code */

            ROM_LOAD_EVEN("808t20.h17", 0x00000, 0x20000, 0x6f0acb1d);
            ROM_LOAD_ODD("808t21.j17", 0x00000, 0x20000, 0x42a30416);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("808e03.f4", 0x00000, 0x08000, 0x3d93a7cd);

            ROM_REGION(0x40000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD_GFX_EVEN("808e12.f28", 0x000000, 0x10000, 0xd62f1fde);       /* 8x8 tiles */

            ROM_LOAD_GFX_ODD("808e13.h28", 0x000000, 0x10000, 0x1fa708f4);       /* 8x8 tiles */

            ROM_LOAD_GFX_EVEN("808e22.i28", 0x020000, 0x10000, 0x73d758f6);       /* 8x8 tiles */

            ROM_LOAD_GFX_ODD("808e23.k28", 0x020000, 0x10000, 0x8ff08b21);       /* 8x8 tiles */

            ROM_REGION(0x100000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("808d17.j4", 0x00000, 0x80000, 0xd1299082);/* sprites */

            ROM_LOAD("808d15.h4", 0x80000, 0x80000, 0x2b22a6b6);

            ROM_REGION(0x0100, REGION_PROMS);
            ROM_LOAD("808a18.f16", 0x0000, 0x0100, 0xeb95aede);/* priority encoder (not used) */

            ROM_REGION(0x20000, REGION_SOUND1);/* 128k for the samples */

            ROM_LOAD("808d01.d4", 0x00000, 0x20000, 0xfd4d37c0);/* samples for 007232 */

            ROM_END();
        }
    };

    static RomLoadPtr rom_mia2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);/* 2*128k and 2*64k for 68000 code */

            ROM_LOAD_EVEN("808s20.h17", 0x00000, 0x20000, 0xcaa2897f);
            ROM_LOAD_ODD("808s21.j17", 0x00000, 0x20000, 0x3d892ffb);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("808e03.f4", 0x00000, 0x08000, 0x3d93a7cd);

            ROM_REGION(0x40000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD_GFX_EVEN("808e12.f28", 0x000000, 0x10000, 0xd62f1fde);       /* 8x8 tiles */

            ROM_LOAD_GFX_ODD("808e13.h28", 0x000000, 0x10000, 0x1fa708f4);       /* 8x8 tiles */

            ROM_LOAD_GFX_EVEN("808e22.i28", 0x020000, 0x10000, 0x73d758f6);       /* 8x8 tiles */

            ROM_LOAD_GFX_ODD("808e23.k28", 0x020000, 0x10000, 0x8ff08b21);       /* 8x8 tiles */

            ROM_REGION(0x100000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("808d17.j4", 0x00000, 0x80000, 0xd1299082);/* sprites */

            ROM_LOAD("808d15.h4", 0x80000, 0x80000, 0x2b22a6b6);

            ROM_REGION(0x0100, REGION_PROMS);
            ROM_LOAD("808a18.f16", 0x0000, 0x0100, 0xeb95aede);/* priority encoder (not used) */

            ROM_REGION(0x20000, REGION_SOUND1);/* 128k for the samples */

            ROM_LOAD("808d01.d4", 0x00000, 0x20000, 0xfd4d37c0);/* samples for 007232 */

            ROM_END();
        }
    };
    static RomLoadPtr rom_tmnt = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x60000, REGION_CPU1);/* 2*128k and 2*64k for 68000 code */

            ROM_LOAD_EVEN("963-r23", 0x00000, 0x20000, 0xa7f61195);
            ROM_LOAD_ODD("963-r24", 0x00000, 0x20000, 0x661e056a);
            ROM_LOAD_EVEN("963-r21", 0x40000, 0x10000, 0xde047bb6);
            ROM_LOAD_ODD("963-r22", 0x40000, 0x10000, 0xd86a0888);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("963-e20", 0x00000, 0x08000, 0x1692a6d6);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD("963-a28", 0x000000, 0x80000, 0xdb4769a8);       /* 8x8 tiles */

            ROM_LOAD("963-a29", 0x080000, 0x80000, 0x8069cd2e);       /* 8x8 tiles */

            ROM_REGION(0x200000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("963-a17", 0x000000, 0x80000, 0xb5239a44);       /* sprites */

            ROM_LOAD("963-a18", 0x080000, 0x80000, 0xdd51adef);       /* sprites */

            ROM_LOAD("963-a15", 0x100000, 0x80000, 0x1f324eed);       /* sprites */

            ROM_LOAD("963-a16", 0x180000, 0x80000, 0xd4bd9984);       /* sprites */

            ROM_REGION(0x0200, REGION_PROMS);
            ROM_LOAD("tmnt.g7", 0x0000, 0x0100, 0xabd82680);/* sprite address decoder */

            ROM_LOAD("tmnt.g19", 0x0100, 0x0100, 0xf8004a1c);/* priority encoder (not used) */

            ROM_REGION(0x20000, REGION_SOUND1);/* 128k for the samples */

            ROM_LOAD("963-a26", 0x00000, 0x20000, 0xe2ac3063);/* samples for 007232 */

            ROM_REGION(0x20000, REGION_SOUND2);/* 128k for the samples */

            ROM_LOAD("963-a27", 0x00000, 0x20000, 0x2dfd674b);/* samples for UPD7759C */

            ROM_REGION(0x80000, REGION_SOUND3);/* 512k for the title music sample */

            ROM_LOAD("963-a25", 0x00000, 0x80000, 0xfca078c7);
            ROM_END();
        }
    };

    static RomLoadPtr rom_tmht = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x60000, REGION_CPU1);/* 2*128k and 2*64k for 68000 code */

            ROM_LOAD_EVEN("963f23.j17", 0x00000, 0x20000, 0x9cb5e461);
            ROM_LOAD_ODD("963f24.k17", 0x00000, 0x20000, 0x2d902fab);
            ROM_LOAD_EVEN("963f21.j15", 0x40000, 0x10000, 0x9fa25378);
            ROM_LOAD_ODD("963f22.k15", 0x40000, 0x10000, 0x2127ee53);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("963-e20", 0x00000, 0x08000, 0x1692a6d6);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD("963-a28", 0x000000, 0x80000, 0xdb4769a8);       /* 8x8 tiles */

            ROM_LOAD("963-a29", 0x080000, 0x80000, 0x8069cd2e);       /* 8x8 tiles */

            ROM_REGION(0x200000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("963-a17", 0x000000, 0x80000, 0xb5239a44);       /* sprites */

            ROM_LOAD("963-a18", 0x080000, 0x80000, 0xdd51adef);       /* sprites */

            ROM_LOAD("963-a15", 0x100000, 0x80000, 0x1f324eed);       /* sprites */

            ROM_LOAD("963-a16", 0x180000, 0x80000, 0xd4bd9984);       /* sprites */

            ROM_REGION(0x0200, REGION_PROMS);
            ROM_LOAD("tmnt.g7", 0x0000, 0x0100, 0xabd82680);/* sprite address decoder */

            ROM_LOAD("tmnt.g19", 0x0100, 0x0100, 0xf8004a1c);/* priority encoder (not used) */

            ROM_REGION(0x20000, REGION_SOUND1);/* 128k for the samples */

            ROM_LOAD("963-a26", 0x00000, 0x20000, 0xe2ac3063);/* samples for 007232 */

            ROM_REGION(0x20000, REGION_SOUND2);/* 128k for the samples */

            ROM_LOAD("963-a27", 0x00000, 0x20000, 0x2dfd674b);/* samples for UPD7759C */

            ROM_REGION(0x80000, REGION_SOUND3);/* 512k for the title music sample */

            ROM_LOAD("963-a25", 0x00000, 0x80000, 0xfca078c7);
            ROM_END();
        }
    };

    static RomLoadPtr rom_tmntj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x60000, REGION_CPU1);/* 2*128k and 2*64k for 68000 code */

            ROM_LOAD_EVEN("963-x23", 0x00000, 0x20000, 0xa9549004);
            ROM_LOAD_ODD("963-x24", 0x00000, 0x20000, 0xe5cc9067);
            ROM_LOAD_EVEN("963-x21", 0x40000, 0x10000, 0x5789cf92);
            ROM_LOAD_ODD("963-x22", 0x40000, 0x10000, 0x0a74e277);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("963-e20", 0x00000, 0x08000, 0x1692a6d6);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD("963-a28", 0x000000, 0x80000, 0xdb4769a8);       /* 8x8 tiles */

            ROM_LOAD("963-a29", 0x080000, 0x80000, 0x8069cd2e);       /* 8x8 tiles */

            ROM_REGION(0x200000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("963-a17", 0x000000, 0x80000, 0xb5239a44);       /* sprites */

            ROM_LOAD("963-a18", 0x080000, 0x80000, 0xdd51adef);       /* sprites */

            ROM_LOAD("963-a15", 0x100000, 0x80000, 0x1f324eed);       /* sprites */

            ROM_LOAD("963-a16", 0x180000, 0x80000, 0xd4bd9984);       /* sprites */

            ROM_REGION(0x0200, REGION_PROMS);
            ROM_LOAD("tmnt.g7", 0x0000, 0x0100, 0xabd82680);/* sprite address decoder */

            ROM_LOAD("tmnt.g19", 0x0100, 0x0100, 0xf8004a1c);/* priority encoder (not used) */

            ROM_REGION(0x20000, REGION_SOUND1);/* 128k for the samples */

            ROM_LOAD("963-a26", 0x00000, 0x20000, 0xe2ac3063);/* samples for 007232 */

            ROM_REGION(0x20000, REGION_SOUND2);/* 128k for the samples */

            ROM_LOAD("963-a27", 0x00000, 0x20000, 0x2dfd674b);/* samples for UPD7759C */

            ROM_REGION(0x80000, REGION_SOUND3);/* 512k for the title music sample */

            ROM_LOAD("963-a25", 0x00000, 0x80000, 0xfca078c7);
            ROM_END();
        }
    };

    static RomLoadPtr rom_tmht2p = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x60000, REGION_CPU1);/* 2*128k and 2*64k for 68000 code */

            ROM_LOAD_EVEN("963-u23", 0x00000, 0x20000, 0x58bec748);
            ROM_LOAD_ODD("963-u24", 0x00000, 0x20000, 0xdce87c8d);
            ROM_LOAD_EVEN("963-u21", 0x40000, 0x10000, 0xabce5ead);
            ROM_LOAD_ODD("963-u22", 0x40000, 0x10000, 0x4ecc8d6b);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("963-e20", 0x00000, 0x08000, 0x1692a6d6);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD("963-a28", 0x000000, 0x80000, 0xdb4769a8);       /* 8x8 tiles */

            ROM_LOAD("963-a29", 0x080000, 0x80000, 0x8069cd2e);       /* 8x8 tiles */

            ROM_REGION(0x200000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("963-a17", 0x000000, 0x80000, 0xb5239a44);       /* sprites */

            ROM_LOAD("963-a18", 0x080000, 0x80000, 0xdd51adef);       /* sprites */

            ROM_LOAD("963-a15", 0x100000, 0x80000, 0x1f324eed);       /* sprites */

            ROM_LOAD("963-a16", 0x180000, 0x80000, 0xd4bd9984);       /* sprites */

            ROM_REGION(0x0200, REGION_PROMS);
            ROM_LOAD("tmnt.g7", 0x0000, 0x0100, 0xabd82680);/* sprite address decoder */

            ROM_LOAD("tmnt.g19", 0x0100, 0x0100, 0xf8004a1c);/* priority encoder (not used) */

            ROM_REGION(0x20000, REGION_SOUND1);/* 128k for the samples */

            ROM_LOAD("963-a26", 0x00000, 0x20000, 0xe2ac3063);/* samples for 007232 */

            ROM_REGION(0x20000, REGION_SOUND2);/* 128k for the samples */

            ROM_LOAD("963-a27", 0x00000, 0x20000, 0x2dfd674b);/* samples for UPD7759C */

            ROM_REGION(0x80000, REGION_SOUND3);/* 512k for the title music sample */

            ROM_LOAD("963-a25", 0x00000, 0x80000, 0xfca078c7);
            ROM_END();
        }
    };

    static RomLoadPtr rom_tmnt2pj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x60000, REGION_CPU1);/* 2*128k and 2*64k for 68000 code */

            ROM_LOAD_EVEN("963-123", 0x00000, 0x20000, 0x6a3527c9);
            ROM_LOAD_ODD("963-124", 0x00000, 0x20000, 0x2c4bfa15);
            ROM_LOAD_EVEN("963-121", 0x40000, 0x10000, 0x4181b733);
            ROM_LOAD_ODD("963-122", 0x40000, 0x10000, 0xc64eb5ff);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("963-e20", 0x00000, 0x08000, 0x1692a6d6);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD("963-a28", 0x000000, 0x80000, 0xdb4769a8);       /* 8x8 tiles */

            ROM_LOAD("963-a29", 0x080000, 0x80000, 0x8069cd2e);       /* 8x8 tiles */

            ROM_REGION(0x200000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("963-a17", 0x000000, 0x80000, 0xb5239a44);       /* sprites */

            ROM_LOAD("963-a18", 0x080000, 0x80000, 0xdd51adef);       /* sprites */

            ROM_LOAD("963-a15", 0x100000, 0x80000, 0x1f324eed);       /* sprites */

            ROM_LOAD("963-a16", 0x180000, 0x80000, 0xd4bd9984);       /* sprites */

            ROM_REGION(0x0200, REGION_PROMS);
            ROM_LOAD("tmnt.g7", 0x0000, 0x0100, 0xabd82680);/* sprite address decoder */

            ROM_LOAD("tmnt.g19", 0x0100, 0x0100, 0xf8004a1c);/* priority encoder (not used) */

            ROM_REGION(0x20000, REGION_SOUND1);/* 128k for the samples */

            ROM_LOAD("963-a26", 0x00000, 0x20000, 0xe2ac3063);/* samples for 007232 */

            ROM_REGION(0x20000, REGION_SOUND2);/* 128k for the samples */

            ROM_LOAD("963-a27", 0x00000, 0x20000, 0x2dfd674b);/* samples for UPD7759C */

            ROM_REGION(0x80000, REGION_SOUND3);/* 512k for the title music sample */

            ROM_LOAD("963-a25", 0x00000, 0x80000, 0xfca078c7);
            ROM_END();
        }
    };
    static RomLoadPtr rom_punkshot = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);/* 4*64k for 68000 code */

            ROM_LOAD_EVEN("907-j02.i7", 0x00000, 0x20000, 0xdbb3a23b);
            ROM_LOAD_ODD("907-j03.i10", 0x00000, 0x20000, 0x2151d1ab);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("907f01.e8", 0x0000, 0x8000, 0xf040c484);

            ROM_REGION(0x80000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD("907d06.e23", 0x000000, 0x40000, 0xf5cc38f4);
            ROM_LOAD("907d05.e22", 0x040000, 0x40000, 0xe25774c1);

            ROM_REGION(0x200000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("907d07l.k2", 0x000000, 0x80000, 0xfeeb345a);
            ROM_LOAD("907d07h.k2", 0x080000, 0x80000, 0x0bff4383);
            ROM_LOAD("907d08l.k7", 0x100000, 0x80000, 0x05f3d196);
            ROM_LOAD("907d08h.k7", 0x180000, 0x80000, 0xeaf18c22);

            ROM_REGION(0x80000, REGION_SOUND1);/* samples for 053260 */

            ROM_LOAD("907d04.d3", 0x0000, 0x80000, 0x090feb5e);
            ROM_END();
        }
    };

    static RomLoadPtr rom_punksht2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);/* 4*64k for 68000 code */

            ROM_LOAD_EVEN("907m02.i7", 0x00000, 0x20000, 0x59e14575);
            ROM_LOAD_ODD("907m03.i10", 0x00000, 0x20000, 0xadb14b1e);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("907f01.e8", 0x0000, 0x8000, 0xf040c484);

            ROM_REGION(0x80000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD("907d06.e23", 0x000000, 0x40000, 0xf5cc38f4);
            ROM_LOAD("907d05.e22", 0x040000, 0x40000, 0xe25774c1);

            ROM_REGION(0x200000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("907d07l.k2", 0x000000, 0x80000, 0xfeeb345a);
            ROM_LOAD("907d07h.k2", 0x080000, 0x80000, 0x0bff4383);
            ROM_LOAD("907d08l.k7", 0x100000, 0x80000, 0x05f3d196);
            ROM_LOAD("907d08h.k7", 0x180000, 0x80000, 0xeaf18c22);

            ROM_REGION(0x80000, REGION_SOUND1);/* samples for the 053260 */

            ROM_LOAD("907d04.d3", 0x0000, 0x80000, 0x090feb5e);
            ROM_END();
        }
    };
    static RomLoadPtr rom_lgtnfght = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);/* 4*64k for 68000 code */

            ROM_LOAD_EVEN("939m02.e11", 0x00000, 0x20000, 0x61a12184);
            ROM_LOAD_ODD("939m03.e15", 0x00000, 0x20000, 0x6db6659d);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("939e01.d7", 0x0000, 0x8000, 0x4a5fc848);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD("939a07.k14", 0x000000, 0x80000, 0x7955dfcf);
            ROM_LOAD("939a08.k19", 0x080000, 0x80000, 0xed95b385);

            ROM_REGION(0x100000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("939a06.k8", 0x000000, 0x80000, 0xe393c206);
            ROM_LOAD("939a05.k2", 0x080000, 0x80000, 0x3662d47a);

            ROM_REGION(0x80000, REGION_SOUND1);/* samples for the 053260 */

            ROM_LOAD("939a04.c5", 0x0000, 0x80000, 0xc24e2b6e);
            ROM_END();
        }
    };

    static RomLoadPtr rom_trigon = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);/* 4*64k for 68000 code */

            ROM_LOAD_EVEN("939j02.bin", 0x00000, 0x20000, 0x38381d1b);
            ROM_LOAD_ODD("939j03.bin", 0x00000, 0x20000, 0xb5beddcd);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("939e01.d7", 0x0000, 0x8000, 0x4a5fc848);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD("939a07.k14", 0x000000, 0x80000, 0x7955dfcf);
            ROM_LOAD("939a08.k19", 0x080000, 0x80000, 0xed95b385);

            ROM_REGION(0x100000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("939a06.k8", 0x000000, 0x80000, 0xe393c206);
            ROM_LOAD("939a05.k2", 0x080000, 0x80000, 0x3662d47a);

            ROM_REGION(0x80000, REGION_SOUND1);/* samples for the 053260 */

            ROM_LOAD("939a04.c5", 0x0000, 0x80000, 0xc24e2b6e);
            ROM_END();
        }
    };

    static RomLoadPtr rom_blswhstl = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x80000, REGION_CPU1);
            ROM_LOAD_EVEN("e09.bin", 0x000000, 0x20000, 0xe8b7b234);
            ROM_LOAD_ODD("g09.bin", 0x000000, 0x20000, 0x3c26d281);
            ROM_LOAD_EVEN("e11.bin", 0x040000, 0x20000, 0x14628736);
            ROM_LOAD_ODD("g11.bin", 0x040000, 0x20000, 0xf738ad4a);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("060_j01.rom", 0x0000, 0x10000, 0xf9d9a673);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD_WIDE("060_e07.r16", 0x000000, 0x080000, 0xc400edf3); /* tiles */

            ROM_LOAD_WIDE("060_e08.r16", 0x080000, 0x080000, 0x70dddba1);

            ROM_REGION(0x100000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD_WIDE("060_e06.r16", 0x000000, 0x080000, 0x09381492); /* sprites */

            ROM_LOAD_WIDE("060_e05.r16", 0x080000, 0x080000, 0x32454241);

            ROM_REGION(0x100000, REGION_SOUND1);/* samples for the 053260 */

            ROM_LOAD("060_e04.r16", 0x0000, 0x100000, 0xc680395d);
            ROM_END();
        }
    };

    static RomLoadPtr rom_detatwin = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x80000, REGION_CPU1);
            ROM_LOAD_EVEN("060_j02.rom", 0x000000, 0x20000, 0x11b761ac);
            ROM_LOAD_ODD("060_j03.rom", 0x000000, 0x20000, 0x8d0b588c);
            ROM_LOAD_EVEN("060_j09.rom", 0x040000, 0x20000, 0xf2a5f15f);
            ROM_LOAD_ODD("060_j10.rom", 0x040000, 0x20000, 0x36eefdbc);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("060_j01.rom", 0x0000, 0x10000, 0xf9d9a673);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD_WIDE("060_e07.r16", 0x000000, 0x080000, 0xc400edf3); /* tiles */

            ROM_LOAD_WIDE("060_e08.r16", 0x080000, 0x080000, 0x70dddba1);

            ROM_REGION(0x100000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD_WIDE("060_e06.r16", 0x000000, 0x080000, 0x09381492); /* sprites */

            ROM_LOAD_WIDE("060_e05.r16", 0x080000, 0x080000, 0x32454241);

            ROM_REGION(0x100000, REGION_SOUND1);/* samples for the 053260 */

            ROM_LOAD("060_e04.r16", 0x0000, 0x100000, 0xc680395d);
            ROM_END();
        }
    };

    static RomLoadPtr rom_glfgreat = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);
            ROM_LOAD_EVEN("061l02.1h", 0x000000, 0x20000, 0xac7399f4);
            ROM_LOAD_ODD("061l03.4h", 0x000000, 0x20000, 0x77b0ff5c);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("061f01.4e", 0x0000, 0x8000, 0xab9a2a57);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD("061d14.12l", 0x000000, 0x080000, 0xb9440924);/* tiles */

            ROM_LOAD("061d13.12k", 0x080000, 0x080000, 0x9f999f0b);

            ROM_REGION(0x200000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("061d11.3k", 0x000000, 0x100000, 0xc45b66a3);/* sprites */

            ROM_LOAD("061d12.8k", 0x100000, 0x100000, 0xd305ecd1);

            ROM_REGION(0x300000, REGION_GFX3);/* unknown (data for the 053936?) */

            ROM_LOAD("061b05.15d", 0x000000, 0x020000, 0x2456fb11);/* gfx */

            ROM_LOAD("061b06.16d", 0x080000, 0x080000, 0x41ada2ad);
            ROM_LOAD("061b07.18d", 0x100000, 0x080000, 0x517887e2);
            ROM_LOAD("061b08.14g", 0x180000, 0x080000, 0x6ab739c3);
            ROM_LOAD("061b09.15g", 0x200000, 0x080000, 0x42c7a603);
            ROM_LOAD("061b10.17g", 0x280000, 0x080000, 0x10f89ce7);

            ROM_REGION(0x100000, REGION_SOUND1);/* samples for the 053260 */

            ROM_LOAD("061e04.1d", 0x0000, 0x100000, 0x7921d8df);
            ROM_END();
        }
    };

    static RomLoadPtr rom_tmnt2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x80000, REGION_CPU1);
            ROM_LOAD_EVEN("uaa02", 0x000000, 0x20000, 0x58d5c93d);
            ROM_LOAD_ODD("uaa03", 0x000000, 0x20000, 0x0541fec9);
            ROM_LOAD_EVEN("uaa04", 0x040000, 0x20000, 0x1d441a7d);
            ROM_LOAD_ODD("uaa05", 0x040000, 0x20000, 0x9c428273);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("b01", 0x0000, 0x10000, 0x364f548a);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD("b12", 0x000000, 0x080000, 0xd3283d19);/* tiles */

            ROM_LOAD("b11", 0x080000, 0x080000, 0x6ebc0c15);

            ROM_REGION(0x400000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("b09", 0x000000, 0x100000, 0x2d7a9d2a);/* sprites */

            ROM_LOAD("b10", 0x100000, 0x080000, 0xf2dd296e);
            /* second half empty */
            ROM_LOAD("b07", 0x200000, 0x100000, 0xd9bee7bf);
            ROM_LOAD("b08", 0x300000, 0x080000, 0x3b1ae36f);
            /* second half empty */

            ROM_REGION(0x200000, REGION_SOUND1);/* samples for the 053260 */

            ROM_LOAD("063b06", 0x0000, 0x200000, 0x1e510aa5);
            ROM_END();
        }
    };

    static RomLoadPtr rom_tmnt22p = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x80000, REGION_CPU1);
            ROM_LOAD_EVEN("a02", 0x000000, 0x20000, 0xaadffe3a);
            ROM_LOAD_ODD("a03", 0x000000, 0x20000, 0x125687a8);
            ROM_LOAD_EVEN("a04", 0x040000, 0x20000, 0xfb5c7ded);
            ROM_LOAD_ODD("a05", 0x040000, 0x20000, 0x3c40fe66);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("b01", 0x0000, 0x10000, 0x364f548a);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD("b12", 0x000000, 0x080000, 0xd3283d19);/* tiles */

            ROM_LOAD("b11", 0x080000, 0x080000, 0x6ebc0c15);

            ROM_REGION(0x400000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("b09", 0x000000, 0x100000, 0x2d7a9d2a);/* sprites */

            ROM_LOAD("b10", 0x100000, 0x080000, 0xf2dd296e);
            /* second half empty */
            ROM_LOAD("b07", 0x200000, 0x100000, 0xd9bee7bf);
            ROM_LOAD("b08", 0x300000, 0x080000, 0x3b1ae36f);
            /* second half empty */

            ROM_REGION(0x200000, REGION_SOUND1);/* samples for the 053260 */

            ROM_LOAD("063b06", 0x0000, 0x200000, 0x1e510aa5);
            ROM_END();
        }
    };

    static RomLoadPtr rom_tmnt2a = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x80000, REGION_CPU1);
            ROM_LOAD_EVEN("ada02", 0x000000, 0x20000, 0x4f11b587);
            ROM_LOAD_ODD("ada03", 0x000000, 0x20000, 0x82a1b9ac);
            ROM_LOAD_EVEN("ada04", 0x040000, 0x20000, 0x05ad187a);
            ROM_LOAD_ODD("ada05", 0x040000, 0x20000, 0xd4826547);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("b01", 0x0000, 0x10000, 0x364f548a);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD("b12", 0x000000, 0x080000, 0xd3283d19);/* tiles */

            ROM_LOAD("b11", 0x080000, 0x080000, 0x6ebc0c15);

            ROM_REGION(0x400000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("b09", 0x000000, 0x100000, 0x2d7a9d2a);/* sprites */

            ROM_LOAD("b10", 0x100000, 0x080000, 0xf2dd296e);
            /* second half empty */
            ROM_LOAD("b07", 0x200000, 0x100000, 0xd9bee7bf);
            ROM_LOAD("b08", 0x300000, 0x080000, 0x3b1ae36f);
            /* second half empty */

            ROM_REGION(0x200000, REGION_SOUND1);/* samples for the 053260 */

            ROM_LOAD("063b06", 0x0000, 0x200000, 0x1e510aa5);
            ROM_END();
        }
    };

    static RomLoadPtr rom_ssriders = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0xc0000, REGION_CPU1);
            ROM_LOAD_EVEN("064eac02", 0x000000, 0x40000, 0x5a5425f4);
            ROM_LOAD_ODD("064eac03", 0x000000, 0x40000, 0x093c00fb);
            ROM_LOAD_EVEN("sr_b04.rom", 0x080000, 0x20000, 0xef2315bd);
            ROM_LOAD_ODD("sr_b05.rom", 0x080000, 0x20000, 0x51d6fbc4);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("sr_e01.rom", 0x0000, 0x10000, 0x44b9bc52);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD("sr_16k.rom", 0x000000, 0x080000, 0xe2bdc619);/* tiles */

            ROM_LOAD("sr_12k.rom", 0x080000, 0x080000, 0x2d8ca8b0);

            ROM_REGION(0x200000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("sr_7l.rom", 0x000000, 0x100000, 0x4160c372);/* sprites */

            ROM_LOAD("sr_3l.rom", 0x100000, 0x100000, 0x64dd673c);

            ROM_REGION(0x100000, REGION_SOUND1);/* samples for the 053260 */

            ROM_LOAD("sr_1d.rom", 0x0000, 0x100000, 0x59810df9);
            ROM_END();
        }
    };

    static RomLoadPtr rom_ssrdrebd = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0xc0000, REGION_CPU1);
            ROM_LOAD_EVEN("064ebd02", 0x000000, 0x40000, 0x8deef9ac);
            ROM_LOAD_ODD("064ebd03", 0x000000, 0x40000, 0x2370c107);
            ROM_LOAD_EVEN("sr_b04.rom", 0x080000, 0x20000, 0xef2315bd);
            ROM_LOAD_ODD("sr_b05.rom", 0x080000, 0x20000, 0x51d6fbc4);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("sr_e01.rom", 0x0000, 0x10000, 0x44b9bc52);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD("sr_16k.rom", 0x000000, 0x080000, 0xe2bdc619);/* tiles */

            ROM_LOAD("sr_12k.rom", 0x080000, 0x080000, 0x2d8ca8b0);

            ROM_REGION(0x200000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("sr_7l.rom", 0x000000, 0x100000, 0x4160c372);/* sprites */

            ROM_LOAD("sr_3l.rom", 0x100000, 0x100000, 0x64dd673c);

            ROM_REGION(0x100000, REGION_SOUND1);/* samples for the 053260 */

            ROM_LOAD("sr_1d.rom", 0x0000, 0x100000, 0x59810df9);
            ROM_END();
        }
    };

    static RomLoadPtr rom_ssrdrebc = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0xc0000, REGION_CPU1);
            ROM_LOAD_EVEN("sr_c02.rom", 0x000000, 0x40000, 0x9bd7d164);
            ROM_LOAD_ODD("sr_c03.rom", 0x000000, 0x40000, 0x40fd4165);
            ROM_LOAD_EVEN("sr_b04.rom", 0x080000, 0x20000, 0xef2315bd);
            ROM_LOAD_ODD("sr_b05.rom", 0x080000, 0x20000, 0x51d6fbc4);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("sr_e01.rom", 0x0000, 0x10000, 0x44b9bc52);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD("sr_16k.rom", 0x000000, 0x080000, 0xe2bdc619);/* tiles */

            ROM_LOAD("sr_12k.rom", 0x080000, 0x080000, 0x2d8ca8b0);

            ROM_REGION(0x200000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("sr_7l.rom", 0x000000, 0x100000, 0x4160c372);/* sprites */

            ROM_LOAD("sr_3l.rom", 0x100000, 0x100000, 0x64dd673c);

            ROM_REGION(0x100000, REGION_SOUND1);/* samples for the 053260 */

            ROM_LOAD("sr_1d.rom", 0x0000, 0x100000, 0x59810df9);
            ROM_END();
        }
    };

    static RomLoadPtr rom_ssrdruda = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0xc0000, REGION_CPU1);
            ROM_LOAD_EVEN("064uda02", 0x000000, 0x40000, 0x5129a6b7);
            ROM_LOAD_ODD("064uda03", 0x000000, 0x40000, 0x9f887214);
            ROM_LOAD_EVEN("sr_b04.rom", 0x080000, 0x20000, 0xef2315bd);
            ROM_LOAD_ODD("sr_b05.rom", 0x080000, 0x20000, 0x51d6fbc4);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("sr_e01.rom", 0x0000, 0x10000, 0x44b9bc52);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD("sr_16k.rom", 0x000000, 0x080000, 0xe2bdc619);/* tiles */

            ROM_LOAD("sr_12k.rom", 0x080000, 0x080000, 0x2d8ca8b0);

            ROM_REGION(0x200000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("sr_7l.rom", 0x000000, 0x100000, 0x4160c372);/* sprites */

            ROM_LOAD("sr_3l.rom", 0x100000, 0x100000, 0x64dd673c);

            ROM_REGION(0x100000, REGION_SOUND1);/* samples for the 053260 */

            ROM_LOAD("sr_1d.rom", 0x0000, 0x100000, 0x59810df9);
            ROM_END();
        }
    };

    static RomLoadPtr rom_ssrdruac = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0xc0000, REGION_CPU1);
            ROM_LOAD_EVEN("064uac02", 0x000000, 0x40000, 0x870473b6);
            ROM_LOAD_ODD("064uac03", 0x000000, 0x40000, 0xeadf289a);
            ROM_LOAD_EVEN("sr_b04.rom", 0x080000, 0x20000, 0xef2315bd);
            ROM_LOAD_ODD("sr_b05.rom", 0x080000, 0x20000, 0x51d6fbc4);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("sr_e01.rom", 0x0000, 0x10000, 0x44b9bc52);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD("sr_16k.rom", 0x000000, 0x080000, 0xe2bdc619);/* tiles */

            ROM_LOAD("sr_12k.rom", 0x080000, 0x080000, 0x2d8ca8b0);

            ROM_REGION(0x200000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("sr_7l.rom", 0x000000, 0x100000, 0x4160c372);/* sprites */

            ROM_LOAD("sr_3l.rom", 0x100000, 0x100000, 0x64dd673c);

            ROM_REGION(0x100000, REGION_SOUND1);/* samples for the 053260 */

            ROM_LOAD("sr_1d.rom", 0x0000, 0x100000, 0x59810df9);
            ROM_END();
        }
    };

    static RomLoadPtr rom_ssrdrubc = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0xc0000, REGION_CPU1);
            ROM_LOAD_EVEN("2pl.8e", 0x000000, 0x40000, 0xaca7fda5);
            ROM_LOAD_ODD("2pl.8g", 0x000000, 0x40000, 0xbb1fdeff);
            ROM_LOAD_EVEN("sr_b04.rom", 0x080000, 0x20000, 0xef2315bd);
            ROM_LOAD_ODD("sr_b05.rom", 0x080000, 0x20000, 0x51d6fbc4);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("sr_e01.rom", 0x0000, 0x10000, 0x44b9bc52);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD("sr_16k.rom", 0x000000, 0x080000, 0xe2bdc619);/* tiles */

            ROM_LOAD("sr_12k.rom", 0x080000, 0x080000, 0x2d8ca8b0);

            ROM_REGION(0x200000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("sr_7l.rom", 0x000000, 0x100000, 0x4160c372);/* sprites */

            ROM_LOAD("sr_3l.rom", 0x100000, 0x100000, 0x64dd673c);

            ROM_REGION(0x100000, REGION_SOUND1);/* samples for the 053260 */

            ROM_LOAD("sr_1d.rom", 0x0000, 0x100000, 0x59810df9);
            ROM_END();
        }
    };

    static RomLoadPtr rom_ssrdrabd = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0xc0000, REGION_CPU1);
            ROM_LOAD_EVEN("064abd02.8e", 0x000000, 0x40000, 0x713406cb);
            ROM_LOAD_ODD("064abd03.8g", 0x000000, 0x40000, 0x680feb3c);
            ROM_LOAD_EVEN("sr_b04.rom", 0x080000, 0x20000, 0xef2315bd);
            ROM_LOAD_ODD("sr_b05.rom", 0x080000, 0x20000, 0x51d6fbc4);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("sr_e01.rom", 0x0000, 0x10000, 0x44b9bc52);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD("sr_16k.rom", 0x000000, 0x080000, 0xe2bdc619);/* tiles */

            ROM_LOAD("sr_12k.rom", 0x080000, 0x080000, 0x2d8ca8b0);

            ROM_REGION(0x200000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("sr_7l.rom", 0x000000, 0x100000, 0x4160c372);/* sprites */

            ROM_LOAD("sr_3l.rom", 0x100000, 0x100000, 0x64dd673c);

            ROM_REGION(0x100000, REGION_SOUND1);/* samples for the 053260 */

            ROM_LOAD("sr_1d.rom", 0x0000, 0x100000, 0x59810df9);
            ROM_END();
        }
    };

    static RomLoadPtr rom_ssrdrjbd = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0xc0000, REGION_CPU1);
            ROM_LOAD_EVEN("064jbd02.8e", 0x000000, 0x40000, 0x7acdc1e3);
            ROM_LOAD_ODD("064jbd03.8g", 0x000000, 0x40000, 0x6a424918);
            ROM_LOAD_EVEN("sr_b04.rom", 0x080000, 0x20000, 0xef2315bd);
            ROM_LOAD_ODD("sr_b05.rom", 0x080000, 0x20000, 0x51d6fbc4);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("sr_e01.rom", 0x0000, 0x10000, 0x44b9bc52);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD("sr_16k.rom", 0x000000, 0x080000, 0xe2bdc619);/* tiles */

            ROM_LOAD("sr_12k.rom", 0x080000, 0x080000, 0x2d8ca8b0);

            ROM_REGION(0x200000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("sr_7l.rom", 0x000000, 0x100000, 0x4160c372);/* sprites */

            ROM_LOAD("sr_3l.rom", 0x100000, 0x100000, 0x64dd673c);

            ROM_REGION(0x100000, REGION_SOUND1);/* samples for the 053260 */

            ROM_LOAD("sr_1d.rom", 0x0000, 0x100000, 0x59810df9);
            ROM_END();
        }
    };
    static RomLoadPtr rom_thndrx2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);
            ROM_LOAD_EVEN("073-k02.11c", 0x000000, 0x20000, 0x0c8b2d3f);
            ROM_LOAD_ODD("073-k03.12c", 0x000000, 0x20000, 0x3803b427);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("073-c01.4f", 0x0000, 0x10000, 0x44ebe83c);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics (addressable by the main CPU) */

            ROM_LOAD("073-c06.16k", 0x000000, 0x080000, 0x24e22b42);/* tiles */

            ROM_LOAD("073-c05.12k", 0x080000, 0x080000, 0x952a935f);

            ROM_REGION(0x100000, REGION_GFX2);/* graphics (addressable by the main CPU) */

            ROM_LOAD("073-c07.7k", 0x000000, 0x080000, 0x14e93f38);/* sprites */

            ROM_LOAD("073-c08.3k", 0x080000, 0x080000, 0x09fab3ab);

            ROM_REGION(0x80000, REGION_SOUND1);/* samples for the 053260 */

            ROM_LOAD("073-b04.2d", 0x0000, 0x80000, BADCRC(0x7f7f2fd3));
            ROM_END();
        }
    };
    public static InitDriverPtr init_gfx = new InitDriverPtr() {
        public void handler() {
            konami_rom_deinterleave_2(REGION_GFX1);
            konami_rom_deinterleave_2(REGION_GFX2);
        }
    };
    public static InitDriverPtr init_mia = new InitDriverPtr() {
        public void handler() {
            UBytePtr gfxdata;
            int len;
            int i, j, k, A, B;
            int[] bits = new int[32];
            UBytePtr temp;

            init_gfx.handler();

            /*
             along with the normal byte reordering, TMNT also needs the bits to
             be shuffled around because the ROMs are connected differently to the
             051962 custom IC.
             */
            gfxdata = memory_region(REGION_GFX1);
            len = memory_region_length(REGION_GFX1);
            for (i = 0; i < len; i += 4) {
                for (j = 0; j < 4; j++) {
                    for (k = 0; k < 8; k++) {
                        bits[8 * j + k] = (gfxdata.read(i + j) >> k) & 1;
                    }
                }

                for (j = 0; j < 4; j++) {
                    gfxdata.write(i + j, 0);
                    for (k = 0; k < 8; k++) {
                        gfxdata.write(i + j, gfxdata.read(i + j) | bits[j + 4 * k] << k);
                    }
                }
            }

            /*
             along with the normal byte reordering, MIA also needs the bits to
             be shuffled around because the ROMs are connected differently to the
             051937 custom IC.
             */
            gfxdata = memory_region(REGION_GFX2);
            len = memory_region_length(REGION_GFX2);
            for (i = 0; i < len; i += 4) {
                for (j = 0; j < 4; j++) {
                    for (k = 0; k < 8; k++) {
                        bits[8 * j + k] = (gfxdata.read(i + j) >> k) & 1;
                    }
                }

                for (j = 0; j < 4; j++) {
                    gfxdata.write(i + j, 0);
                    for (k = 0; k < 8; k++) {
                        gfxdata.write(i + j, gfxdata.read(i + j) | bits[j + 4 * k] << k);
                    }
                }
            }

            temp = new UBytePtr(len);
            if (temp == null) {
                return;	/* bad thing! */

            }
            memcpy(temp, gfxdata, len);
            for (A = 0; A < len / 4; A++) {
                /* the bits to scramble are the low 8 ones */
                for (i = 0; i < 8; i++) {
                    bits[i] = (A >> i) & 0x01;
                }

                B = A & 0x3ff00;

                if ((A & 0x3c000) == 0x3c000) {
                    B |= bits[3] << 0;
                    B |= bits[5] << 1;
                    B |= bits[0] << 2;
                    B |= bits[1] << 3;
                    B |= bits[2] << 4;
                    B |= bits[4] << 5;
                    B |= bits[6] << 6;
                    B |= bits[7] << 7;
                } else {
                    B |= bits[3] << 0;
                    B |= bits[5] << 1;
                    B |= bits[7] << 2;
                    B |= bits[0] << 3;
                    B |= bits[1] << 4;
                    B |= bits[2] << 5;
                    B |= bits[4] << 6;
                    B |= bits[6] << 7;
                }

                gfxdata.write(4 * A + 0, temp.read(4 * B + 0));
                gfxdata.write(4 * A + 1, temp.read(4 * B + 1));
                gfxdata.write(4 * A + 2, temp.read(4 * B + 2));
                gfxdata.write(4 * A + 3, temp.read(4 * B + 3));
            }
            temp = null;
        }
    };
    public static InitDriverPtr init_tmnt = new InitDriverPtr() {
        public void handler() {
            UBytePtr gfxdata;
            int len;
            int i, j, k, A, B, entry;
            int[] bits = new int[32];
            UBytePtr temp;

            init_gfx.handler();

            /*
             along with the normal byte reordering, TMNT also needs the bits to
             be shuffled around because the ROMs are connected differently to the
             051962 custom IC.
             */
            gfxdata = memory_region(REGION_GFX1);
            len = memory_region_length(REGION_GFX1);
            for (i = 0; i < len; i += 4) {
                for (j = 0; j < 4; j++) {
                    for (k = 0; k < 8; k++) {
                        bits[8 * j + k] = (gfxdata.read(i + j) >> k) & 1;
                    }
                }

                for (j = 0; j < 4; j++) {
                    gfxdata.write(i + j, 0);
                    for (k = 0; k < 8; k++) {
                        gfxdata.write(i + j, gfxdata.read(i + j) | bits[j + 4 * k] << k);
                    }
                }
            }

            /*
             along with the normal byte reordering, TMNT also needs the bits to
             be shuffled around because the ROMs are connected differently to the
             051937 custom IC.
             */
            gfxdata = memory_region(REGION_GFX2);
            len = memory_region_length(REGION_GFX2);
            for (i = 0; i < len; i += 4) {
                for (j = 0; j < 4; j++) {
                    for (k = 0; k < 8; k++) {
                        bits[8 * j + k] = (gfxdata.read(i + j) >> k) & 1;
                    }
                }

                for (j = 0; j < 4; j++) {
                    gfxdata.write(i + j, 0);
                    for (k = 0; k < 8; k++) {
                        gfxdata.write(i + j, gfxdata.read(i + j) | bits[j + 4 * k] << k);
                    }
                }
            }

            temp = new UBytePtr(len);
            memcpy(temp, gfxdata, len);
            for (A = 0; A < len / 4; A++) {
                UBytePtr code_conv_table = new UBytePtr(memory_region(REGION_PROMS), 0x0000);
                int CA0 = 0;
                int CA1 = 1;
                int CA2 = 2;
                int CA3 = 3;
                int CA4 = 4;
                int CA5 = 5;
                int CA6 = 6;
                int CA7 = 7;
                int CA8 = 8;
                int CA9 = 9;

                /* following table derived from the schematics. It indicates, for each of the */
                /* 9 low bits of the sprite line address, which bit to pick it from. */
                /* For example, when the PROM contains 4, which applies to 4x2 sprites, */
                /* bit OA1 comes from CA5, OA2 from CA0, and so on. */
                int bit_pick_table[][]
                        = {
                            /*0(1x1) 1(2x1) 2(1x2) 3(2x2) 4(4x2) 5(2x4) 6(4x4) 7(8x8) */
                            {CA3, CA3, CA3, CA3, CA3, CA3, CA3, CA3}, /* CA3 */
                            {CA0, CA0, CA5, CA5, CA5, CA5, CA5, CA5}, /* OA1 */
                            {CA1, CA1, CA0, CA0, CA0, CA7, CA7, CA7}, /* OA2 */
                            {CA2, CA2, CA1, CA1, CA1, CA0, CA0, CA9}, /* OA3 */
                            {CA4, CA4, CA2, CA2, CA2, CA1, CA1, CA0}, /* OA4 */
                            {CA5, CA6, CA4, CA4, CA4, CA2, CA2, CA1}, /* OA5 */
                            {CA6, CA5, CA6, CA6, CA6, CA4, CA4, CA2}, /* OA6 */
                            {CA7, CA7, CA7, CA7, CA8, CA6, CA6, CA4}, /* OA7 */
                            {CA8, CA8, CA8, CA8, CA7, CA8, CA8, CA6}, /* OA8 */
                            {CA9, CA9, CA9, CA9, CA9, CA9, CA9, CA8} /* OA9 */};

                /* pick the correct entry in the PROM (top 8 bits of the address) */
                entry = code_conv_table.read((A & 0x7f800) >> 11) & 7;

                /* the bits to scramble are the low 10 ones */
                for (i = 0; i < 10; i++) {
                    bits[i] = (A >> i) & 0x01;
                }

                B = A & 0x7fc00;

                for (i = 0; i < 10; i++) {
                    B |= bits[bit_pick_table[i][entry]] << i;
                }

                gfxdata.write(4 * A + 0, temp.read(4 * B + 0));
                gfxdata.write(4 * A + 1, temp.read(4 * B + 1));
                gfxdata.write(4 * A + 2, temp.read(4 * B + 2));
                gfxdata.write(4 * A + 3, temp.read(4 * B + 3));
            }
            temp = null;
        }
    };

    static void shuffle(UBytePtr buf, int len) {
        int i;
        int t;

        if (len == 2) {
            return;
        }

        //if (len % 4) exit(1);	/* must not happen */
        len /= 2;

        for (i = 0; i < len / 2; i++) {
            t = buf.read(len / 2 + i);
            buf.write(len / 2 + i, buf.read(len + i));
            buf.write(len + i, t);
        }

        shuffle(buf, len);
        shuffle(new UBytePtr(buf, len), len);
    }

    public static InitDriverPtr init_glfgreat = new InitDriverPtr() {
        public void handler() {
            /* ROMs are interleaved at byte level */
            shuffle(memory_region(REGION_GFX1), memory_region_length(REGION_GFX1));
            shuffle(memory_region(REGION_GFX2), memory_region_length(REGION_GFX2));
        }
    };
    public static GameDriver driver_mia = new GameDriver("1989", "mia", "tmnt.java", rom_mia, null, machine_driver_mia, input_ports_mia, init_mia, ROT0, "Konami", "Missing in Action (version T)");
    public static GameDriver driver_mia2 = new GameDriver("1989", "mia2", "tmnt.java", rom_mia2, driver_mia, machine_driver_mia, input_ports_mia, init_mia, ROT0, "Konami", "Missing in Action (version S)");

    public static GameDriver driver_tmnt = new GameDriver("1989", "tmnt", "tmnt.java", rom_tmnt, null, machine_driver_tmnt, input_ports_tmnt, init_tmnt, ROT0, "Konami", "Teenage Mutant Ninja Turtles (4 Players US)");
    public static GameDriver driver_tmht = new GameDriver("1989", "tmht", "tmnt.java", rom_tmht, driver_tmnt, machine_driver_tmnt, input_ports_tmnt, init_tmnt, ROT0, "Konami", "Teenage Mutant Hero Turtles (4 Players UK)");
    public static GameDriver driver_tmntj = new GameDriver("1989", "tmntj", "tmnt.java", rom_tmntj, driver_tmnt, machine_driver_tmnt, input_ports_tmnt, init_tmnt, ROT0, "Konami", "Teenage Mutant Ninja Turtles (4 Players Japan)");
    public static GameDriver driver_tmht2p = new GameDriver("1989", "tmht2p", "tmnt.java", rom_tmht2p, driver_tmnt, machine_driver_tmnt, input_ports_tmnt2p, init_tmnt, ROT0, "Konami", "Teenage Mutant Hero Turtles (2 Players UK)");
    public static GameDriver driver_tmnt2pj = new GameDriver("1990", "tmnt2pj", "tmnt.java", rom_tmnt2pj, driver_tmnt, machine_driver_tmnt, input_ports_tmnt2p, init_tmnt, ROT0, "Konami", "Teenage Mutant Ninja Turtles (2 Players Japan)");

    public static GameDriver driver_punkshot = new GameDriver("1990", "punkshot", "tmnt.java", rom_punkshot, null, machine_driver_punkshot, input_ports_punkshot, init_gfx, ROT0, "Konami", "Punk Shot (4 Players)");
    public static GameDriver driver_punksht2 = new GameDriver("1990", "punksht2", "tmnt.java", rom_punksht2, driver_punkshot, machine_driver_punkshot, input_ports_punksht2, init_gfx, ROT0, "Konami", "Punk Shot (2 Players)");

    public static GameDriver driver_lgtnfght = new GameDriver("1990", "lgtnfght", "tmnt.java", rom_lgtnfght, null, machine_driver_lgtnfght, input_ports_lgtnfght, init_gfx, ROT90, "Konami", "Lightning Fighters (US)");
    public static GameDriver driver_trigon = new GameDriver("1990", "trigon", "tmnt.java", rom_trigon, driver_lgtnfght, machine_driver_lgtnfght, input_ports_lgtnfght, init_gfx, ROT90, "Konami", "Trigon (Japan)");

    /*TODO*///GAME( 1991, blswhstl, 0,        detatwin, detatwin, gfx,      ROT90, "Konami", "Bells & Whistles" )
            /*TODO*///GAME( 1991, detatwin, blswhstl, detatwin, detatwin, gfx,      ROT90, "Konami", "Detana!! Twin Bee (Japan)" )
            /*TODO*///
            /*TODO*///GAMEX(1991, glfgreat, 0,        glfgreat, glfgreat, glfgreat, ROT0,  "Konami", "Golfing Greats", GAME_NOT_WORKING )
            /*TODO*///
            /*TODO*///GAMEX(1991, tmnt2,    0,        tmnt2,    ssridr4p, gfx,      ROT0,  "Konami", "Teenage Mutant Ninja Turtles - Turtles in Time (4 Players US)", GAME_IMPERFECT_COLORS )
            /*TODO*///GAMEX(1991, tmnt22p,  tmnt2,    tmnt2,    ssriders, gfx,      ROT0,  "Konami", "Teenage Mutant Ninja Turtles - Turtles in Time (2 Players US)", GAME_IMPERFECT_COLORS )
            /*TODO*///GAMEX(1991, tmnt2a,   tmnt2,    tmnt2,    tmnt2a,   gfx,      ROT0,  "Konami", "Teenage Mutant Ninja Turtles - Turtles in Time (4 Players Asia)", GAME_IMPERFECT_COLORS )
            /*TODO*///
    public static GameDriver driver_ssriders = new GameDriver("1991", "ssriders", "tmnt.java", rom_ssriders, null, machine_driver_ssriders, input_ports_ssridr4p, init_gfx, ROT0, "Konami", "Sunset Riders (World 4 Players ver. EAC)");
    public static GameDriver driver_ssrdrebd = new GameDriver("1991", "ssrdrebd", "tmnt.java", rom_ssrdrebd, driver_ssriders, machine_driver_ssriders, input_ports_ssriders, init_gfx, ROT0, "Konami", "Sunset Riders (World 2 Players ver. EBD)");
    public static GameDriver driver_ssrdrebc = new GameDriver("1991", "ssrdrebc", "tmnt.java", rom_ssrdrebc, driver_ssriders, machine_driver_ssriders, input_ports_ssriders, init_gfx, ROT0, "Konami", "Sunset Riders (World 2 Players ver. EBC)");
    public static GameDriver driver_ssrdruda = new GameDriver("1991", "ssrdruda", "tmnt.java", rom_ssrdruda, driver_ssriders, machine_driver_ssriders, input_ports_ssriders, init_gfx, ROT0, "Konami", "Sunset Riders (US 4 Players ver. UDA)");
    public static GameDriver driver_ssrdruac = new GameDriver("1991", "ssrdruac", "tmnt.java", rom_ssrdruac, driver_ssriders, machine_driver_ssriders, input_ports_ssriders, init_gfx, ROT0, "Konami", "Sunset Riders (US 4 Players ver. UAC)");
    public static GameDriver driver_ssrdrubc = new GameDriver("1991", "ssrdrubc", "tmnt.java", rom_ssrdrubc, driver_ssriders, machine_driver_ssriders, input_ports_ssriders, init_gfx, ROT0, "Konami", "Sunset Riders (US 2 Players ver. UBC)");
    public static GameDriver driver_ssrdrabd = new GameDriver("1991", "ssrdrabd", "tmnt.java", rom_ssrdrabd, driver_ssriders, machine_driver_ssriders, input_ports_ssriders, init_gfx, ROT0, "Konami", "Sunset Riders (Asia 2 Players ver. ABD)");
    public static GameDriver driver_ssrdrjbd = new GameDriver("1991", "ssrdrjbd", "tmnt.java", rom_ssrdrjbd, driver_ssriders, machine_driver_ssriders, input_ports_ssriders, init_gfx, ROT0, "Konami", "Sunset Riders (Japan 2 Players ver. JBD)");

    public static GameDriver driver_thndrx2 = new GameDriver("1991", "thndrx2", "tmnt.java", rom_thndrx2, null, machine_driver_thndrx2, input_ports_thndrx2, init_gfx, ROT0, "Konami", "Thunder Cross II (Japan)");

}
