/***************************************************************************

Atari Audio Board II
--------------------

6502 MEMORY MAP

Function                                  Address     R/W  Data
---------------------------------------------------------------
Program RAM                               0000-1FFF   R/W  D0-D7

Music (YM-2151)                           2000-2001   R/W  D0-D7

Read 68010 Port (Input Buffer)            280A        R    D0-D7

Self-test                                 280C        R    D7
Output Buffer Full (@2A02) (Active High)              R    D5
Left Coin Switch                                      R    D1
Right Coin Switch                                     R    D0

Interrupt acknowledge                     2A00        W    xx
Write 68010 Port (Outbut Buffer)          2A02        W    D0-D7
Banked ROM select (at 3000-3FFF)          2A04        W    D6-D7
???                                       2A06        W

Effects                                   2C00-2C0F   R/W  D0-D7

Banked Program ROM (4 pages)              3000-3FFF   R    D0-D7
Static Program ROM (48K bytes)            4000-FFFF   R    D0-D7

****************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.sndhrdw;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigen.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigenH.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.sound._2151intf.*;
import static gr.codebb.arcadeflex.v036.sound.pokey.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.*;
import static arcadeflex.v036.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295H.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static arcadeflex.v036.sound._2151intfH.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.sound._5220intf.*;
import gr.codebb.arcadeflex.v037b7.sound._5220intfH.TMS5220interface;

public class atarijsa
{

	static UBytePtr bank_base=new UBytePtr();
	static UBytePtr bank_source_data=new UBytePtr();

	static int speech_data;
	static int last_ctl;

	static int cpu_num;
	static int input_port;
	static int test_port;
	static int test_mask;

        static int has_pokey;
        static int has_ym2151;
        static int has_tms5220;
	static int has_oki6295;

	static int oki6295_bank_base;

	static int overall_volume;
	static int pokey_volume;
	static int ym2151_volume;
	static int tms5220_volume;
	static int oki6295_volume;
	
	
	/*************************************
	 *
	 *	External interfaces
	 *
	 *************************************/
	
	public static void atarijsa_init(int cpunum, int inputport, int testport, int testmask)
	{
		int i;
	
		/* copy in the parameters */
		cpu_num = cpunum;
		input_port = inputport;
		test_port = testport;
		test_mask = testmask;
	
		/* predetermine the bank base */
		bank_base = new UBytePtr(memory_region(REGION_CPU1+cpunum), 0x03000);
		bank_source_data = new UBytePtr(memory_region(REGION_CPU1+cpunum), 0x10000);
	
		/* determine which sound hardware is installed */
		has_tms5220 = has_oki6295 = has_pokey = has_ym2151 = 0;
		for (i = 0; i < MAX_SOUND; i++)
		{
			switch (Machine.drv.sound[i].sound_type)
			{
				case SOUND_TMS5220:
					has_tms5220 = 1;
					break;
				case SOUND_OKIM6295:
					has_oki6295 = 1;
					break;
				case SOUND_POKEY:
					has_pokey = 1;
					break;
				case SOUND_YM2151:
					has_ym2151 = 1;
					break;
			}
		}
	
		/* install POKEY memory handlers */
		if (has_pokey != 0)
		{
			install_mem_read_handler(cpunum, 0x2c00, 0x2c0f, pokey1_r);
			install_mem_write_handler(cpunum, 0x2c00, 0x2c0f, pokey1_w);
		}
	
		atarijsa_reset();
	}
	
	
	public static void atarijsa_reset()
	{
		/* reset the sound I/O system */
		atarigen_sound_io_reset(cpu_num);
	
		/* reset the static states */
		speech_data = 0;
		last_ctl = 0;
		oki6295_bank_base = 0x00000;
		overall_volume = 100;
		pokey_volume = 100;
		ym2151_volume = 100;
		tms5220_volume = 100;
		oki6295_volume = 100;
	
		/* Guardians of the Hood assumes we're reset to bank 0 on startup */
		memcpy(bank_base, new UBytePtr(bank_source_data, 0x0000), 0x1000);
	}
	
	
	
	/*************************************
	 *
	 *	JSA I I/O handlers
	 *
	 *************************************/
	
	public static ReadHandlerPtr jsa1_io_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int result = 0xff;
	
		switch (offset & 0x206)
		{
			case 0x000:		/* n/c */
				logerror("atarijsa: Unknown read at %04X\n", offset & 0x206);
				break;
	
			case 0x002:		/* /RDP */
				result = atarigen_6502_sound_r.handler(offset);
				break;
	
			case 0x004:		/* /RDIO */
				/*
					0x80 = self test
					0x40 = NMI line state (active low)
					0x20 = sound output full
					0x10 = TMS5220 ready (active low)
					0x08 = +5V
					0x04 = +5V
					0x02 = coin 2
					0x01 = coin 1
				*/
				result = readinputport(input_port);
				if ((readinputport(test_port) & test_mask)==0) result ^= 0x80;
				if (atarigen_cpu_to_sound_ready != 0) result ^= 0x40;
				if (atarigen_sound_to_cpu_ready != 0) result ^= 0x20;
				if (has_tms5220==0 || tms5220_ready_r()!=0) result ^= 0x10;
				break;
	
			case 0x006:		/* /IRQACK */
				atarigen_6502_irq_ack_r.handler(0);
				break;
	
			case 0x200:		/* /VOICE */
			case 0x202:		/* /WRP */
			case 0x204:		/* /WRIO */
			case 0x206:		/* /MIX */
				logerror("atarijsa: Unknown read at %04X\n", offset & 0x206);
				break;
		}
	
		return result;
	} };
	
	
	public static WriteHandlerPtr jsa1_io_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch (offset & 0x206)
		{
			case 0x000:		/* n/c */
			case 0x002:		/* /RDP */
			case 0x004:		/* /RDIO */
				logerror("atarijsa: Unknown write (%02X) at %04X\n", data & 0xff, offset & 0x206);
				break;
	
			case 0x006:		/* /IRQACK */
				atarigen_6502_irq_ack_r.handler(0);
				break;
	
			case 0x200:		/* /VOICE */
				speech_data = data;
				break;
	
			case 0x202:		/* /WRP */
				atarigen_6502_sound_w.handler(offset, data);
				break;
	
			case 0x204:		/* WRIO */
				/*
					0xc0 = bank address
					0x20 = coin counter 2
					0x10 = coin counter 1
					0x08 = squeak (tweaks the 5220 frequency)
					0x04 = TMS5220 reset (active low)
					0x02 = TMS5220 write strobe (active low)
					0x01 = YM2151 reset (active low)
				*/
	
				/* handle TMS5220 I/O */
				if (has_tms5220 != 0)
				{
					int count;
	
					if (((data ^ last_ctl) & 0x02)!=0 && (data & 0x02)!=0)
						tms5220_data_w.handler(0, speech_data);
					count = 5 | ((data >> 2) & 2);
					tms5220_set_frequency(ATARI_CLOCK_14MHz/2 / (16 - count));
				}
	
				/* update the bank */
				memcpy(bank_base, new UBytePtr(bank_source_data, 0x1000 * ((data >> 6) & 3)), 0x1000);
				last_ctl = data;
				break;
	
			case 0x206:		/* MIX */
				/*
					0xc0 = TMS5220 volume (0-3)
					0x30 = POKEY volume (0-3)
					0x0e = YM2151 volume (0-7)
					0x01 = low-pass filter enable
				*/
				tms5220_volume = ((data >> 6) & 3) * 100 / 3;
				pokey_volume = ((data >> 4) & 3) * 100 / 3;
				ym2151_volume = ((data >> 1) & 7) * 100 / 7;
				update_all_volumes();
				break;
		}
	} };
	
	
	
	/*************************************
	 *
	 *	JSA II I/O handlers
	 *
	 *************************************/
	
	public static ReadHandlerPtr jsa2_io_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int result = 0xff;
	
		switch (offset & 0x206)
		{
			case 0x000:		/* /RDV */
				if (has_oki6295 != 0)
					result = OKIM6295_status_0_r.handler(offset);
				else
					logerror("atarijsa: Unknown read at %04X\n", offset & 0x206);
				break;
	
			case 0x002:		/* /RDP */
				result = atarigen_6502_sound_r.handler(offset);
				break;
	
			case 0x004:		/* /RDIO */
				/*
					0x80 = self test
					0x40 = NMI line state (active low)
					0x20 = sound output full
					0x10 = +5V
					0x08 = +5V
					0x04 = +5V
					0x02 = coin 2
					0x01 = coin 1
				*/
				result = readinputport(input_port);
				if ((readinputport(test_port)!=0?0:1 & test_mask)!=0) result ^= 0x80;
				if (atarigen_cpu_to_sound_ready != 0) result ^= 0x40;
				if (atarigen_sound_to_cpu_ready != 0) result ^= 0x20;
				break;
	
			case 0x006:		/* /IRQACK */
				atarigen_6502_irq_ack_r.handler(0);
				break;
	
			case 0x200:		/* /WRV */
			case 0x202:		/* /WRP */
			case 0x204:		/* /WRIO */
			case 0x206:		/* /MIX */
				logerror("atarijsa: Unknown read at %04X\n", offset & 0x206);
				break;
		}
	
		return result;
	} };
	
	
	public static WriteHandlerPtr jsa2_io_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch (offset & 0x206)
		{
			case 0x000:		/* /RDV */
			case 0x002:		/* /RDP */
			case 0x004:		/* /RDIO */
				logerror("atarijsa: Unknown write (%02X) at %04X\n", data & 0xff, offset & 0x206);
				break;
	
			case 0x006:		/* /IRQACK */
				atarigen_6502_irq_ack_r.handler(0);
				break;
	
			case 0x200:		/* /WRV */
				if (has_oki6295 != 0)
					OKIM6295_data_0_w.handler(offset, data);
				else
					logerror("atarijsa: Unknown write (%02X) at %04X\n", data & 0xff, offset & 0x206);
				break;
	
			case 0x202:		/* /WRP */
				atarigen_6502_sound_w.handler(offset, data);
				break;
	
			case 0x204:		/* /WRIO */
				/*
					0xc0 = bank address
					0x20 = coin counter 2
					0x10 = coin counter 1
					0x08 = voice frequency (tweaks the OKI6295 frequency)
					0x04 = OKI6295 reset (active low)
					0x02 = n/c
					0x01 = YM2151 reset (active low)
				*/
	
				/* update the bank */
				memcpy(new UBytePtr(bank_base), new UBytePtr(bank_source_data, 0x1000 * ((data >> 6) & 3)), 0x1000);
				last_ctl = data;
	
				/* update the OKI frequency */
				OKIM6295_set_frequency(0, ALL_VOICES, ATARI_CLOCK_14MHz/4/3 / ((data & 8)!=0 ? 132 : 165));
				break;
	
			case 0x206:		/* /MIX */
				/*
					0xc0 = n/c
					0x20 = low-pass filter enable
					0x10 = n/c
					0x0e = YM2151 volume (0-7)
					0x01 = OKI6295 volume (0-1)
				*/
				ym2151_volume = ((data >> 1) & 7) * 100 / 7;
				oki6295_volume = 50 + (data & 1) * 50;
				update_all_volumes();
				break;
		}
	} };
	
	
	
	/*************************************
	 *
	 *	JSA III I/O handlers
	 *
	 *************************************/
	
	public static ReadHandlerPtr jsa3_io_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int result = 0xff;
	
		switch (offset & 0x206)
		{
			case 0x000:		/* /RDV */
				if (has_oki6295 != 0)
					result = OKIM6295_status_0_r.handler(offset);
				break;
	
			case 0x002:		/* /RDP */
				result = atarigen_6502_sound_r.handler(offset);
				break;
	
			case 0x004:		/* /RDIO */
				/*
					0x80 = self test (active high)
					0x40 = NMI line state (active high)
					0x20 = sound output full (active high)
					0x10 = self test (active high)
					0x08 = service (active high)
					0x04 = tilt (active high)
					0x02 = coin L (active high)
					0x01 = coin R (active high)
				*/
				result = readinputport(input_port);
				if ((readinputport(test_port) & test_mask)==0) result ^= 0x90;
				if (atarigen_cpu_to_sound_ready != 0) result ^= 0x40;
				if (atarigen_sound_to_cpu_ready != 0) result ^= 0x20;
				break;
	
			case 0x006:		/* /IRQACK */
				atarigen_6502_irq_ack_r.handler(0);
				break;
	
			case 0x200:		/* /WRV */
			case 0x202:		/* /WRP */
			case 0x204:		/* /WRIO */
			case 0x206:		/* /MIX */
				logerror("atarijsa: Unknown read at %04X\n", offset & 0x206);
				break;
		}
	
		return result;
	} };
	
	
	public static WriteHandlerPtr jsa3_io_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch (offset & 0x206)
		{
			case 0x000:		/* /RDV */
				overall_volume = data * 100 / 127;
				update_all_volumes();
				break;
	
			case 0x002:		/* /RDP */
			case 0x004:		/* /RDIO */
				logerror("atarijsa: Unknown write (%02X) at %04X\n", data & 0xff, offset & 0x206);
				break;
	
			case 0x006:		/* /IRQACK */
				atarigen_6502_irq_ack_r.handler(0);
				break;
	
			case 0x200:		/* /WRV */
				if (has_oki6295 != 0)
					OKIM6295_data_0_w.handler(offset, data);
				break;
	
			case 0x202:		/* /WRP */
				atarigen_6502_sound_w.handler(offset, data);
				break;
	
			case 0x204:		/* /WRIO */
				/*
					0xc0 = bank address
					0x20 = coin counter 2
					0x10 = coin counter 1
					0x08 = voice frequency (tweaks the OKI6295 frequency)
					0x04 = OKI6295 reset (active low)
					0x02 = OKI6295 bank bit 0
					0x01 = YM2151 reset (active low)
				*/
	
				/* update the OKI bank */
				oki6295_bank_base = (0x40000 * ((data >> 1) & 1)) | (oki6295_bank_base & 0x80000);
				OKIM6295_set_bank_base(0, ALL_VOICES, oki6295_bank_base);
	
				/* update the bank */
				memcpy(bank_base, new UBytePtr(bank_source_data, 0x1000 * ((data >> 6) & 3)), 0x1000);
				last_ctl = data;
	
				/* update the OKI frequency */
				OKIM6295_set_frequency(0, ALL_VOICES, ATARI_CLOCK_14MHz/4/3 / ((data & 8) != 0 ? 132 : 165));
				break;
	
			case 0x206:		/* /MIX */
				/*
					0xc0 = n/c
					0x20 = low-pass filter enable
					0x10 = OKI6295 bank bit 1
					0x0e = YM2151 volume (0-7)
					0x01 = OKI6295 volume (0-1)
				*/
	
				/* update the OKI bank */
				oki6295_bank_base = (0x80000 * ((data >> 4) & 1)) | (oki6295_bank_base & 0x40000);
				OKIM6295_set_bank_base(0, ALL_VOICES, oki6295_bank_base);
	
				/* update the volumes */
				ym2151_volume = ((data >> 1) & 7) * 100 / 7;
				oki6295_volume = 50 + (data & 1) * 50;
				update_all_volumes();
				break;
		}
	} };
	
	
	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	JSA IIIS I/O handlers
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr jsa3s_io_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int result = 0xff;
/*TODO*///	
/*TODO*///		switch (offset & 0x206)
/*TODO*///		{
/*TODO*///			case 0x000:		/* /RDV */
/*TODO*///				if (has_oki6295 != 0)
/*TODO*///				{
/*TODO*///					if ((offset & 1) != 0)
/*TODO*///						result = OKIM6295_status_1_r(offset);
/*TODO*///					else
/*TODO*///						result = OKIM6295_status_0_r(offset);
/*TODO*///				}
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x002:		/* /RDP */
/*TODO*///				result = atarigen_6502_sound_r(offset);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x004:		/* /RDIO */
/*TODO*///				/*
/*TODO*///					0x80 = self test (active high)
/*TODO*///					0x40 = NMI line state (active high)
/*TODO*///					0x20 = sound output full (active high)
/*TODO*///					0x10 = self test (active high)
/*TODO*///					0x08 = service (active high)
/*TODO*///					0x04 = tilt (active high)
/*TODO*///					0x02 = coin L (active high)
/*TODO*///					0x01 = coin R (active high)
/*TODO*///				*/
/*TODO*///				result = readinputport(input_port);
/*TODO*///				if (!(readinputport(test_port) & test_mask)) result ^= 0x90;
/*TODO*///				if (atarigen_cpu_to_sound_ready != 0) result ^= 0x40;
/*TODO*///				if (atarigen_sound_to_cpu_ready != 0) result ^= 0x20;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x006:		/* /IRQACK */
/*TODO*///				atarigen_6502_irq_ack_r(0);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x200:		/* /WRV */
/*TODO*///			case 0x202:		/* /WRP */
/*TODO*///			case 0x204:		/* /WRIO */
/*TODO*///			case 0x206:		/* /MIX */
/*TODO*///				logerror("atarijsa: Unknown read at %04X\n", offset & 0x206);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return result;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr jsa3s_io_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		switch (offset & 0x206)
/*TODO*///		{
/*TODO*///			case 0x000:		/* /RDV */
/*TODO*///				overall_volume = data * 100 / 127;
/*TODO*///				update_all_volumes();
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x002:		/* /RDP */
/*TODO*///			case 0x004:		/* /RDIO */
/*TODO*///				logerror("atarijsa: Unknown write (%02X) at %04X\n", data & 0xff, offset & 0x206);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x006:		/* /IRQACK */
/*TODO*///				atarigen_6502_irq_ack_r(0);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x200:		/* /WRV */
/*TODO*///				if (has_oki6295 != 0)
/*TODO*///				{
/*TODO*///					if ((offset & 1) != 0)
/*TODO*///						OKIM6295_data_1_w(offset, data);
/*TODO*///					else
/*TODO*///						OKIM6295_data_0_w(offset, data);
/*TODO*///				}
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x202:		/* /WRP */
/*TODO*///				atarigen_6502_sound_w(offset, data);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x204:		/* /WRIO */
/*TODO*///				/*
/*TODO*///					0xc0 = bank address
/*TODO*///					0x20 = coin counter 2
/*TODO*///					0x10 = coin counter 1
/*TODO*///					0x08 = voice frequency (tweaks the OKI6295 frequency)
/*TODO*///					0x04 = OKI6295 reset (active low)
/*TODO*///					0x02 = left OKI6295 bank bit 0
/*TODO*///					0x01 = YM2151 reset (active low)
/*TODO*///				*/
/*TODO*///	
/*TODO*///				/* update the OKI bank */
/*TODO*///				oki6295_bank_base = (0x40000 * ((data >> 1) & 1)) | (oki6295_bank_base & 0x80000);
/*TODO*///				OKIM6295_set_bank_base(0, ALL_VOICES, oki6295_bank_base);
/*TODO*///	
/*TODO*///				/* update the bank */
/*TODO*///				memcpy(bank_base, &bank_source_data[0x1000 * ((data >> 6) & 3)], 0x1000);
/*TODO*///				last_ctl = data;
/*TODO*///	
/*TODO*///				/* update the OKI frequency */
/*TODO*///				OKIM6295_set_frequency(0, ALL_VOICES, ATARI_CLOCK_14MHz/4/3 / ((data & 8) ? 132 : 165));
/*TODO*///				OKIM6295_set_frequency(1, ALL_VOICES, ATARI_CLOCK_14MHz/4/3 / ((data & 8) ? 132 : 165));
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x206:		/* /MIX */
/*TODO*///				/*
/*TODO*///					0xc0 = right OKI6295 bank bits 0-1
/*TODO*///					0x20 = low-pass filter enable
/*TODO*///					0x10 = left OKI6295 bank bit 1
/*TODO*///					0x0e = YM2151 volume (0-7)
/*TODO*///					0x01 = OKI6295 volume (0-1)
/*TODO*///				*/
/*TODO*///	
/*TODO*///				/* update the OKI bank */
/*TODO*///				oki6295_bank_base = (0x80000 * ((data >> 4) & 1)) | (oki6295_bank_base & 0x40000);
/*TODO*///				OKIM6295_set_bank_base(0, ALL_VOICES, oki6295_bank_base);
/*TODO*///				OKIM6295_set_bank_base(1, ALL_VOICES, 0x40000 * (data >> 6));
/*TODO*///	
/*TODO*///				/* update the volumes */
/*TODO*///				ym2151_volume = ((data >> 1) & 7) * 100 / 7;
/*TODO*///				oki6295_volume = 50 + (data & 1) * 50;
/*TODO*///				update_all_volumes();
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
	
	/*************************************
	 *
	 *	Volume helpers
	 *
	 *************************************/
	
	static void update_all_volumes()
	{
/*TODO*///		if (has_pokey != 0) atarigen_set_pokey_vol(overall_volume * pokey_volume / 100);
/*TODO*///		if (has_ym2151 != 0) atarigen_set_ym2151_vol(overall_volume * ym2151_volume / 100);
/*TODO*///		if (has_tms5220 != 0) atarigen_set_tms5220_vol(overall_volume * tms5220_volume / 100);
/*TODO*///		if (has_oki6295 != 0) atarigen_set_oki6295_vol(overall_volume * oki6295_volume / 100);
	}
	
	
	
	/*************************************
	 *
	 *	Sound CPU memory handlers
	 *
	 *************************************/
	
	public static MemoryReadAddress atarijsa1_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x1fff, MRA_RAM ),
		new MemoryReadAddress( 0x2000, 0x2001, YM2151_status_port_0_r ),
		new MemoryReadAddress( 0x2800, 0x2bff, jsa1_io_r ),
		new MemoryReadAddress( 0x3000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	
	public static MemoryWriteAddress atarijsa1_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x1fff, MWA_RAM ),
		new MemoryWriteAddress( 0x2000, 0x2000, YM2151_register_port_0_w ),
		new MemoryWriteAddress( 0x2001, 0x2001, YM2151_data_port_0_w ),
		new MemoryWriteAddress( 0x2800, 0x2bff, jsa1_io_w ),
		new MemoryWriteAddress( 0x3000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	static MemoryReadAddress atarijsa2_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x1fff, MRA_RAM ),
		new MemoryReadAddress( 0x2000, 0x2001, YM2151_status_port_0_r ),
		new MemoryReadAddress( 0x2800, 0x2bff, jsa2_io_r ),
		new MemoryReadAddress( 0x3000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	
	static MemoryWriteAddress atarijsa2_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x1fff, MWA_RAM ),
		new MemoryWriteAddress( 0x2000, 0x2000, YM2151_register_port_0_w ),
		new MemoryWriteAddress( 0x2001, 0x2001, YM2151_data_port_0_w ),
		new MemoryWriteAddress( 0x2800, 0x2bff, jsa2_io_w ),
		new MemoryWriteAddress( 0x3000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	public static MemoryReadAddress atarijsa3_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x1fff, MRA_RAM ),
		new MemoryReadAddress( 0x2000, 0x2001, YM2151_status_port_0_r ),
		new MemoryReadAddress( 0x2800, 0x2bff, jsa3_io_r ),
		new MemoryReadAddress( 0x3000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	
	public static MemoryWriteAddress atarijsa3_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x1fff, MWA_RAM ),
		new MemoryWriteAddress( 0x2000, 0x2000, YM2151_register_port_0_w ),
		new MemoryWriteAddress( 0x2001, 0x2001, YM2151_data_port_0_w ),
		new MemoryWriteAddress( 0x2800, 0x2bff, jsa3_io_w ),
		new MemoryWriteAddress( 0x3000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
/*TODO*///	static MemoryReadAddress atarijsa3s_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x0000, 0x1fff, MRA_RAM ),
/*TODO*///		new MemoryReadAddress( 0x2000, 0x2001, YM2151_status_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0x2800, 0x2bff, jsa3s_io_r ),
/*TODO*///		new MemoryReadAddress( 0x3000, 0xffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	static MemoryWriteAddress atarijsa3s_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x0000, 0x1fff, MWA_RAM ),
/*TODO*///		new MemoryWriteAddress( 0x2000, 0x2000, YM2151_register_port_0_w ),
/*TODO*///		new MemoryWriteAddress( 0x2001, 0x2001, YM2151_data_port_0_w ),
/*TODO*///		new MemoryWriteAddress( 0x2800, 0x2bff, jsa3s_io_w ),
/*TODO*///		new MemoryWriteAddress( 0x3000, 0xffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( -1 )  /* end of table */
/*TODO*///	};
	
	
	
	/*************************************
	 *
	 *	Sound definitions
	 *
	 *************************************/
	
	public static TMS5220interface atarijsa_tms5220_interface = new TMS5220interface
	(
		ATARI_CLOCK_14MHz/2/11,	/* potentially ATARI_CLOCK_14MHz/2/9 as well */
		100,
		null
	);
	
	
/*TODO*///	static POKEYinterface atarijsa_pokey_interface = new POKEYinterface
/*TODO*///	(
/*TODO*///		1,			/* 1 chip */
/*TODO*///		ATARI_CLOCK_14MHz/8,
/*TODO*///		new int[] { 40 },
/*TODO*///	);
/*TODO*///	
	
	public static YM2151interface atarijsa_ym2151_interface_mono = new YM2151interface
	(
		1,			/* 1 chip */
		ATARI_CLOCK_14MHz/4,
		new int[] { YM3012_VOL(30,MIXER_PAN_CENTER,30,MIXER_PAN_CENTER) },
		new WriteYmHandlerPtr[] { atarigen_ym2151_irq_gen }
	);
	
	
	public static YM2151interface atarijsa_ym2151_interface_stereo = new YM2151interface
	(
		1,			/* 1 chip */
		ATARI_CLOCK_14MHz/4,
		new int[] { YM3012_VOL(60,MIXER_PAN_LEFT,60,MIXER_PAN_RIGHT) },
		new WriteYmHandlerPtr[] { atarigen_ym2151_irq_gen }
	);
	
	
	public static YM2151interface atarijsa_ym2151_interface_stereo_swapped = new YM2151interface
	(
		1,			/* 1 chip */
		ATARI_CLOCK_14MHz/4,
		new int[] { YM3012_VOL(60,MIXER_PAN_RIGHT,60,MIXER_PAN_LEFT) },
		new WriteYmHandlerPtr[] { atarigen_ym2151_irq_gen }
	);
	
	
	public static OKIM6295interface atarijsa_okim6295_interface_REGION_SOUND1 = new OKIM6295interface
	(
		1,              /* 1 chip */
		new int[] { ATARI_CLOCK_14MHz/4/3/132 },
		new int[] { REGION_SOUND1 },
		new int[] { 75 }
	);
	
	
/*TODO*///	static OKIM6295interface atarijsa_okim6295s_interface_REGION_SOUND1 = new OKIM6295interface
/*TODO*///	(
/*TODO*///		2, 				/* 2 chips */
/*TODO*///		new int[] { ATARI_CLOCK_14MHz/4/3/132, ATARI_CLOCK_14MHz/4/3/132 },
/*TODO*///		new int[] { REGION_SOUND1, REGION_SOUND1 },
/*TODO*///		new int[] { MIXER(75,MIXER_PAN_LEFT), MIXER(75,MIXER_PAN_RIGHT) }
/*TODO*///	);
}
