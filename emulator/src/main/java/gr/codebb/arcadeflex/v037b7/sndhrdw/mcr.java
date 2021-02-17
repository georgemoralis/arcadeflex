/** *************************************************************************
 *
 * sndhrdw/mcr.c
 *
 * Functions to emulate general the various MCR sound cards.
 *
 ************************************************************************** */

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.sndhrdw;

import static gr.codebb.arcadeflex.v036.cpu.m6800.m6800H.M6800_IRQ_LINE;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.M6809_IRQ_LINE;
import static gr.codebb.arcadeflex.v036.machine._6812piaH.*;
import static gr.codebb.arcadeflex.v037b7.sndhrdw.mcrH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.timer.*;
import static gr.codebb.arcadeflex.v037b7.mame.timerH.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v037b7.sound.dacH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v036.machine._6821pia.*;
import static gr.codebb.arcadeflex.v037b7.sound.dac.DAC_signed_data_16_w;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;

public class mcr {

    /**
     * ***********************************
     *
     * Global variables
     *
     ************************************
     */
    public static int mcr_sound_config;
    /*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Statics
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
	static int dacval;
/*TODO*///	
    /* SSIO-specific globals */
    static int /*UINT8*/ ssio_sound_cpu;
    static /*UINT8*/ int[] u8_ssio_data = new int[4];
    static /*UINT8*/ int u8_ssio_status;
    static /*UINT8*/ int[][] u8_ssio_duty_cycle = new int[2][3];

    /*TODO*///	
/*TODO*///	/* Chip Squeak Deluxe-specific globals */
	static int csdeluxe_sound_cpu;
	static int csdeluxe_dac_index;
/*TODO*///	extern struct pia6821_interface csdeluxe_pia_intf;
	
	/* Turbo Chip Squeak-specific globals */
	static int turbocs_sound_cpu;
	static int turbocs_dac_index;
	static int turbocs_status;
/*TODO*///	extern struct pia6821_interface turbocs_pia_intf;

	/* Sounds Good-specific globals */
	static int soundsgood_sound_cpu;
	static int soundsgood_dac_index;
	static int soundsgood_status;
/*TODO*///	extern struct pia6821_interface soundsgood_pia_intf;
	
	/* Squawk n' Talk-specific globals */
	static int squawkntalk_sound_cpu;
	static int squawkntalk_tms_command;
	static int squawkntalk_tms_strobes;
/*TODO*///	extern struct pia6821_interface squawkntalk_pia0_intf;
/*TODO*///	extern struct pia6821_interface squawkntalk_pia1_intf;



    /**
     * ***********************************
     *
     * Generic MCR sound initialization
     *
     ************************************
     */
    public static void mcr_sound_init() {
        int sound_cpu = 1;
        int dac_index = 0;

        /* SSIO */
        if ((mcr_sound_config & MCR_SSIO) != 0) {
            ssio_sound_cpu = sound_cpu++;
            ssio_reset_w(1);
            ssio_reset_w(0);
        }
        	
		/* Turbo Chip Squeak */
		if ((mcr_sound_config & MCR_TURBO_CHIP_SQUEAK) != 0)
		{
			pia_config(0, PIA_ALTERNATE_ORDERING, turbocs_pia_intf);
			turbocs_dac_index = dac_index++;
			turbocs_sound_cpu = sound_cpu++;
			turbocs_reset_w(1);
			turbocs_reset_w(0);
		}
	
		/* Chip Squeak Deluxe */
		if ((mcr_sound_config & MCR_CHIP_SQUEAK_DELUXE) != 0)
		{
			pia_config(0, PIA_ALTERNATE_ORDERING | PIA_16BIT_AUTO, csdeluxe_pia_intf);
			csdeluxe_dac_index = dac_index++;
			csdeluxe_sound_cpu = sound_cpu++;
			csdeluxe_reset_w(1);
			csdeluxe_reset_w(0);
		}
	
		/* Sounds Good */
		if ((mcr_sound_config & MCR_SOUNDS_GOOD) != 0)
		{
			/* special case: Spy Hunter 2 has both Turbo CS and Sounds Good, so we use PIA slot 1 */
			pia_config(1, PIA_ALTERNATE_ORDERING | PIA_16BIT_UPPER, soundsgood_pia_intf);
			soundsgood_dac_index = dac_index++;
			soundsgood_sound_cpu = sound_cpu++;
			soundsgood_reset_w(1);
			soundsgood_reset_w(0);
		}
	
		/* Squawk n Talk */
		if ((mcr_sound_config & MCR_SQUAWK_N_TALK) != 0)
		{
			pia_config(0, PIA_STANDARD_ORDERING | PIA_8BIT, squawkntalk_pia0_intf);
			pia_config(1, PIA_STANDARD_ORDERING | PIA_8BIT, squawkntalk_pia1_intf);
			squawkntalk_sound_cpu = sound_cpu++;
			squawkntalk_reset_w(1);
			squawkntalk_reset_w(0);
		}
	
		/* Advanced Audio */
		if ((mcr_sound_config & MCR_WILLIAMS_SOUND) != 0)
		{
/*TODO*///			williams_cvsd_init(sound_cpu++, 0);
			dac_index++;
/*TODO*///			williams_cvsd_reset_w(1);
/*TODO*///			williams_cvsd_reset_w(0);
		}
	
		/* reset any PIAs */
		pia_reset();
    }

    /**
     * ***********************************
     *
     * MCR SSIO communications
     *
     * Z80, 2 AY-3812
     *
     ************************************
     */
    /**
     * ******* internal interfaces **********
     */
    public static WriteHandlerPtr ssio_status_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            u8_ssio_status = data & 0xFF;
        }
    };

    public static ReadHandlerPtr ssio_data_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return u8_ssio_data[offset] & 0xFF;
        }
    };
    public static timer_callback ssio_delayed_data_w = new timer_callback() {
        public void handler(int param) {

            u8_ssio_data[param >> 8] = param & 0xff;
        }
    };

    static void ssio_update_volumes() {
        int chip, chan;
        for (chip = 0; chip < 2; chip++) {
            for (chan = 0; chan < 3; chan++) {
                AY8910_set_volume(chip, chan, (u8_ssio_duty_cycle[chip][chan] ^ 15) * 100 / 15);
            }
        }
    }

    public static WriteHandlerPtr ssio_porta0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            u8_ssio_duty_cycle[0][0] = data & 15;
            u8_ssio_duty_cycle[0][1] = (data >> 4) & 0xFF;
            ssio_update_volumes();
        }
    };

    public static WriteHandlerPtr ssio_portb0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            u8_ssio_duty_cycle[0][2] = data & 15;
            ssio_update_volumes();
        }
    };

    public static WriteHandlerPtr ssio_porta1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            u8_ssio_duty_cycle[1][0] = data & 15;
            u8_ssio_duty_cycle[1][1] = (data >> 4) & 0xFF;
            ssio_update_volumes();
        }
    };

    public static WriteHandlerPtr ssio_portb1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            u8_ssio_duty_cycle[1][2] = data & 15;
/*TODO*///            mixer_sound_enable_global_w.handler(NOT(data & 0x80));
            ssio_update_volumes();
        }
    };

    /**
     * ******* external interfaces **********
     */
    public static WriteHandlerPtr ssio_data_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            timer_set(TIME_NOW, (offset << 8) | (data & 0xff), ssio_delayed_data_w);
        }
    };

    public static ReadHandlerPtr ssio_status_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return u8_ssio_status;
        }
    };

    public static void ssio_reset_w(int state) {
        /* going high halts the CPU */
        if (state != 0) {
            int i;

            cpu_set_reset_line(ssio_sound_cpu, ASSERT_LINE);

            /* latches also get reset */
            for (i = 0; i < 4; i++) {
                u8_ssio_data[i] = 0;
            }
            u8_ssio_status = 0;
        } /* going low resets and reactivates the CPU */ else {
            cpu_set_reset_line(ssio_sound_cpu, CLEAR_LINE);
        }
    }

    /**
     * ******* sound interfaces **********
     */
    public static AY8910interface ssio_ay8910_interface = new AY8910interface(
            2, /* 2 chips */
            2000000, /* 2 MHz ?? */
            new int[]{MIXER(33, MIXER_PAN_LEFT), MIXER(33, MIXER_PAN_RIGHT)}, /* dotron clips with anything higher */
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{ssio_porta0_w, ssio_porta1_w},
            new WriteHandlerPtr[]{ssio_portb0_w, ssio_portb1_w}
    );

    /**
     * ******* memory interfaces **********
     */
    public static MemoryReadAddress ssio_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x83ff, MRA_RAM),
                new MemoryReadAddress(0x9000, 0x9003, ssio_data_r),
                new MemoryReadAddress(0xa001, 0xa001, AY8910_read_port_0_r),
                new MemoryReadAddress(0xb001, 0xb001, AY8910_read_port_1_r),
                new MemoryReadAddress(0xe000, 0xe000, MRA_NOP),
                new MemoryReadAddress(0xf000, 0xf000, input_port_5_r),
                new MemoryReadAddress(-1) /* end of table */};

    public static MemoryWriteAddress ssio_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x83ff, MWA_RAM),
                new MemoryWriteAddress(0xa000, 0xa000, AY8910_control_port_0_w),
                new MemoryWriteAddress(0xa002, 0xa002, AY8910_write_port_0_w),
                new MemoryWriteAddress(0xb000, 0xb000, AY8910_control_port_1_w),
                new MemoryWriteAddress(0xb002, 0xb002, AY8910_write_port_1_w),
                new MemoryWriteAddress(0xc000, 0xc000, ssio_status_w),
                new MemoryWriteAddress(0xe000, 0xe000, MWA_NOP),
                new MemoryWriteAddress(-1) /* end of table */};
    	
	
	
	/*************************************
	 *
	 *	Chip Squeak Deluxe communications
	 *
	 *	MC68000, 1 PIA, 10-bit DAC
	 *
	 *************************************/
	
	/********* internal interfaces ***********/
	public static WriteHandlerPtr csdeluxe_porta_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		dacval = (dacval & ~0x3fc) | (data << 2);
		DAC_signed_data_16_w.handler(csdeluxe_dac_index, dacval << 6);
	} };
	
	public static WriteHandlerPtr csdeluxe_portb_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		dacval = (dacval & ~0x003) | (data >> 6);
		DAC_signed_data_16_w.handler(csdeluxe_dac_index, dacval << 6);
	} };
	
	static irqfuncPtr csdeluxe_irq = new irqfuncPtr() {
            @Override
            public void handler(int state) {
                cpu_set_irq_line(csdeluxe_sound_cpu, 4, state!=0 ? ASSERT_LINE : CLEAR_LINE);
            }
        };
	
	static timer_callback csdeluxe_delayed_data_w = new timer_callback() {
            @Override
            public void handler(int param) {
                pia_0_portb_w.handler(0, param & 0x0f);
		pia_0_ca1_w.handler(0, ~param & 0x10);
            }
        };
        
	
	/********* external interfaces ***********/
	public static WriteHandlerPtr csdeluxe_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		timer_set(TIME_NOW, data, csdeluxe_delayed_data_w);
	} };
	
	static void csdeluxe_reset_w(int state)
	{
		cpu_set_reset_line(csdeluxe_sound_cpu, state!=0 ? ASSERT_LINE : CLEAR_LINE);
	}
	
	
	/********* sound interfaces ***********/
	public static DACinterface mcr_dac_interface = new DACinterface
	(
		1,
		new int[] { 100 }
	);
	
/*TODO*///	static DACinterface mcr_dual_dac_interface = new DACinterface
/*TODO*///	(
/*TODO*///		2,
/*TODO*///		new int[] { 75, 75 }
/*TODO*///	);
	
	
	/********* memory interfaces ***********/
	static MemoryReadAddress csdeluxe_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x007fff, MRA_ROM ),
		new MemoryReadAddress( 0x018000, 0x018007, pia_0_r ),
		new MemoryReadAddress( 0x01c000, 0x01cfff, MRA_BANK1 ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress csdeluxe_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x007fff, MWA_ROM ),
		new MemoryWriteAddress( 0x018000, 0x018007, pia_0_w ),
		new MemoryWriteAddress( 0x01c000, 0x01cfff, MWA_BANK1 ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	/********* PIA interfaces ***********/
	static pia6821_interface csdeluxe_pia_intf = new pia6821_interface
	(
		/*inputs : A/B,CA/B1,CA/B2 */ null, null, null, null, null, null,
		/*outputs: A/B,CA/B2       */ csdeluxe_porta_w, csdeluxe_portb_w, null, null,
		/*irqs   : A/B             */ csdeluxe_irq, csdeluxe_irq
	);
	
	
	
	/*************************************
	 *
	 *	MCR Sounds Good communications
	 *
	 *	MC68000, 1 PIA, 10-bit DAC
	 *
	 *************************************/
	
	/********* internal interfaces ***********/
	public static WriteHandlerPtr soundsgood_porta_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		dacval = (dacval & ~0x3fc) | (data << 2);
		DAC_signed_data_16_w.handler(soundsgood_dac_index, dacval << 6);
	} };
	
	public static WriteHandlerPtr soundsgood_portb_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		dacval = (dacval & ~0x003) | (data >> 6);
		DAC_signed_data_16_w.handler(soundsgood_dac_index, dacval << 6);
		soundsgood_status = (data >> 4) & 3;
	} };
	
	static irqfuncPtr soundsgood_irq = new irqfuncPtr() {
            @Override
            public void handler(int state) {
	  	cpu_set_irq_line(soundsgood_sound_cpu, 4, state!=0 ? ASSERT_LINE : CLEAR_LINE);
            }
        };
	
/*TODO*///	static void soundsgood_delayed_data_w(int param)
/*TODO*///	{
/*TODO*///		pia_1_portb_w(0, (param >> 1) & 0x0f);
/*TODO*///		pia_1_ca1_w(0, ~param & 0x01);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/********* external interfaces ***********/
/*TODO*///	public static WriteHandlerPtr soundsgood_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		timer_set(TIME_NOW, data, soundsgood_delayed_data_w);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr soundsgood_status_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		return soundsgood_status;
/*TODO*///	} };
	
	static void soundsgood_reset_w(int state)
	{
		cpu_set_reset_line(soundsgood_sound_cpu, state!=0 ? ASSERT_LINE : CLEAR_LINE);
	}
	
	
/*TODO*///	/********* sound interfaces ***********/
/*TODO*///	static DACinterface turbocs_plus_soundsgood_dac_interface = new DACinterface
/*TODO*///	(
/*TODO*///		2,
/*TODO*///		new int[] { 80, 80 }
/*TODO*///	);
/*TODO*///	
/*TODO*///	
/*TODO*///	/********* memory interfaces ***********/
/*TODO*///	static MemoryReadAddress soundsgood_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x060000, 0x060007, pia_1_r ),
/*TODO*///		new MemoryReadAddress( 0x070000, 0x070fff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress( -1 )	/* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress soundsgood_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x060000, 0x060007, pia_1_w ),
/*TODO*///		new MemoryWriteAddress( 0x070000, 0x070fff, MWA_BANK1 ),
/*TODO*///		new MemoryWriteAddress( -1 )	/* end of table */
/*TODO*///	};
	
	
	/********* PIA interfaces ***********/
	/* Note: we map this board to PIA #1. It is only used in Spy Hunter and Spy Hunter 2 */
	/* For Spy Hunter 2, we also have a Turbo Chip Squeak in PIA slot 0, so we don't want */
	/* to interfere */
	static pia6821_interface soundsgood_pia_intf = new pia6821_interface
	(
		/*inputs : A/B,CA/B1,CA/B2 */ null, null, null, null, null, null,
		/*outputs: A/B,CA/B2       */ soundsgood_porta_w, soundsgood_portb_w, null, null,
		/*irqs   : A/B             */ soundsgood_irq, soundsgood_irq
	);
	
	
	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	MCR Turbo Chip Squeak communications
/*TODO*///	 *
/*TODO*///	 *	MC6809, 1 PIA, 8-bit DAC
/*TODO*///	 *
/*TODO*///	 *************************************/
	
	/********* internal interfaces ***********/
	public static WriteHandlerPtr turbocs_porta_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		dacval = (dacval & ~0x3fc) | (data << 2);
		DAC_signed_data_16_w.handler(turbocs_dac_index, dacval << 6);
	} };
	
	public static WriteHandlerPtr turbocs_portb_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		dacval = (dacval & ~0x003) | (data >> 6);
		DAC_signed_data_16_w.handler(turbocs_dac_index, dacval << 6);
		turbocs_status = (data >> 4) & 3;
	} };
	
	static irqfuncPtr turbocs_irq = new irqfuncPtr() {
            @Override
            public void handler(int state) {
                cpu_set_irq_line(turbocs_sound_cpu, M6809_IRQ_LINE, state!=0 ? ASSERT_LINE : CLEAR_LINE);
            }
        };
	
/*TODO*///	static void turbocs_delayed_data_w(int param)
/*TODO*///	{
/*TODO*///		pia_0_portb_w(0, (param >> 1) & 0x0f);
/*TODO*///		pia_0_ca1_w(0, ~param & 0x01);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/********* external interfaces ***********/
/*TODO*///	public static WriteHandlerPtr turbocs_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		timer_set(TIME_NOW, data, turbocs_delayed_data_w);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr turbocs_status_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		return turbocs_status;
/*TODO*///	} };
	
	static void turbocs_reset_w(int state)
	{
		cpu_set_reset_line(turbocs_sound_cpu, state!=0 ? ASSERT_LINE : CLEAR_LINE);
	}
	
	
/*TODO*///	/********* memory interfaces ***********/
/*TODO*///	static MemoryReadAddress turbocs_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x0000, 0x07ff, MRA_RAM ),
/*TODO*///		new MemoryReadAddress( 0x4000, 0x4003, pia_0_r ),	/* Max RPM accesses the PIA here */
/*TODO*///		new MemoryReadAddress( 0x6000, 0x6003, pia_0_r ),
/*TODO*///		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( -1 )	/* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress turbocs_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x0000, 0x07ff, MWA_RAM ),
/*TODO*///		new MemoryWriteAddress( 0x4000, 0x4003, pia_0_w ),	/* Max RPM accesses the PIA here */
/*TODO*///		new MemoryWriteAddress( 0x6000, 0x6003, pia_0_w ),
/*TODO*///		new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( -1 )	/* end of table */
/*TODO*///	};
	
	
	/********* PIA interfaces ***********/
	static pia6821_interface turbocs_pia_intf = new pia6821_interface
	(
		/*inputs : A/B,CA/B1,CA/B2 */ null, null, null, null, null, null,
		/*outputs: A/B,CA/B2       */ turbocs_porta_w, turbocs_portb_w, null, null,
		/*irqs   : A/B             */ turbocs_irq, turbocs_irq
	);
	
	
	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	MCR Squawk n Talk communications
/*TODO*///	 *
/*TODO*///	 *	MC6802, 2 PIAs, TMS5220, AY8912 (not used), 8-bit DAC (not used)
/*TODO*///	 *
/*TODO*///	 *************************************/
	
	/********* internal interfaces ***********/
	public static WriteHandlerPtr squawkntalk_porta1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		logerror("Write to AY-8912\n");
	} };
	
	public static WriteHandlerPtr squawkntalk_porta2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		squawkntalk_tms_command = data;
	} };
	
	public static WriteHandlerPtr squawkntalk_portb2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* bits 0-1 select read/write strobes on the TMS5220 */
		data &= 0x03;
	
		/* write strobe -- pass the current command to the TMS5220 */
		if (((data ^ squawkntalk_tms_strobes) & 0x02)!=0 && (data & 0x02)==0)
		{
/*TODO*///			tms5220_data_w(offset, squawkntalk_tms_command);
	
			/* DoT expects the ready line to transition on a command/write here, so we oblige */
			pia_1_ca2_w.handler(0, 1);
			pia_1_ca2_w.handler(0, 0);
		}
	
		/* read strobe -- read the current status from the TMS5220 */
		else if (((data ^ squawkntalk_tms_strobes) & 0x01)!=0 && (data & 0x01)==0)
		{
/*TODO*///			pia_1_porta_w(0, tms5220_status_r(offset));
	
			/* DoT expects the ready line to transition on a command/write here, so we oblige */
			pia_1_ca2_w.handler(0, 1);
			pia_1_ca2_w.handler(0, 0);
		}
	
		/* remember the state */
		squawkntalk_tms_strobes = data;
	} };
	
	static irqfuncPtr squawkntalk_irq = new irqfuncPtr() {
            @Override
            public void handler(int state) {
		cpu_set_irq_line(squawkntalk_sound_cpu, M6800_IRQ_LINE, state!=0 ? ASSERT_LINE : CLEAR_LINE);
            }
        };
	
/*TODO*///	static void squawkntalk_delayed_data_w(int param)
/*TODO*///	{
/*TODO*///		pia_0_porta_w(0, ~param & 0x0f);
/*TODO*///		pia_0_cb1_w(0, ~param & 0x10);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/********* external interfaces ***********/
/*TODO*///	public static WriteHandlerPtr squawkntalk_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		timer_set(TIME_NOW, data, squawkntalk_delayed_data_w);
/*TODO*///	} };
	
	static void squawkntalk_reset_w(int state)
	{
		cpu_set_reset_line(squawkntalk_sound_cpu, state!=0 ? ASSERT_LINE : CLEAR_LINE);
	}
	
	
/*TODO*///	/********* sound interfaces ***********/
/*TODO*///	struct TMS5220interface squawkntalk_tms5220_interface =
/*TODO*///	{
/*TODO*///		640000,
/*TODO*///		MIXER(60,MIXER_PAN_LEFT),
/*TODO*///		0
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	/********* memory interfaces ***********/
/*TODO*///	static MemoryReadAddress squawkntalk_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x0000, 0x007f, MRA_RAM ),
/*TODO*///		new MemoryReadAddress( 0x0080, 0x0083, pia_0_r ),
/*TODO*///		new MemoryReadAddress( 0x0090, 0x0093, pia_1_r ),
/*TODO*///		new MemoryReadAddress( 0xd000, 0xffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( -1 )	/* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	static MemoryWriteAddress squawkntalk_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x0000, 0x007f, MWA_RAM ),
/*TODO*///		new MemoryWriteAddress( 0x0080, 0x0083, pia_0_w ),
/*TODO*///		new MemoryWriteAddress( 0x0090, 0x0093, pia_1_w ),
/*TODO*///		new MemoryWriteAddress( 0xd000, 0xffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( -1 )	/* end of table */
/*TODO*///	};
	
	
	/********* PIA interfaces ***********/
	static pia6821_interface squawkntalk_pia0_intf = new pia6821_interface
	(
		/*inputs : A/B,CA/B1,CA/B2 */ null, null, null, null, null, null,
		/*outputs: A/B,CA/B2       */ squawkntalk_porta1_w, null, null, null,
		/*irqs   : A/B             */ squawkntalk_irq, squawkntalk_irq
	);
	
	static pia6821_interface squawkntalk_pia1_intf = new pia6821_interface
	(
		/*inputs : A/B,CA/B1,CA/B2 */ null, null, null, null, null, null,
		/*outputs: A/B,CA/B2       */ squawkntalk_porta2_w, squawkntalk_portb2_w, null, null,
		/*irqs   : A/B             */ squawkntalk_irq, squawkntalk_irq
	);
}
