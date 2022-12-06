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
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.platform.input.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.memory.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.simpsons.*;
import static gr.codebb.arcadeflex.v036.machine.eeprom.*;
import static gr.codebb.arcadeflex.v036.machine.eepromH.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konami.K053247.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;
import static gr.codebb.arcadeflex.v036.cpu.konami.konami.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.sound.k053260.*;

public class simpsons
{

	
	public static int simpsons_firq_enabled;
	
	/***************************************************************************
	
	  EEPROM
	
	***************************************************************************/
	
	public static int init_eeprom_count;
	
	
	static EEPROM_interface eeprom_interface = new EEPROM_interface
    (
		7,				/* address bits */
		8,				/* data bits */
		"011000",		/*  read command */
		"011100",		/* write command */
		null,				/* erase command */
		"0100000000000",/* lock command */
		"0100110000000" /* unlock command */
    );
	public static nvramPtr simpsons_nvram_handler = new nvramPtr(){ public void handler(Object file,int read_or_write)
    {
		if (read_or_write != 0)
			EEPROM_save(file);
		else
		{
			EEPROM_init(eeprom_interface);
	
			if (file != null)
			{
				init_eeprom_count = 0;
				EEPROM_load(file);
			}
			else
				init_eeprom_count = 10;
		}
	}};
	
	public static ReadHandlerPtr simpsons_eeprom_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		int res;
	
		res = (EEPROM_read_bit() << 4);
	
		res |= 0x20;//konami_eeprom_ack() << 5; /* add the ack */
	
		res |= readinputport( 5 ) & 1; /* test switch */
	
		if (init_eeprom_count != 0)
		{
			init_eeprom_count--;
			res &= 0xfe;
		}
		return res;
	} };
	
	public static WriteHandlerPtr simpsons_eeprom_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if ( data == 0xff )
			return;
	
		EEPROM_write_bit(data & 0x80);
		EEPROM_set_cs_line((data & 0x08)!=0 ? CLEAR_LINE : ASSERT_LINE);
		EEPROM_set_clock_line((data & 0x10)!=0 ? ASSERT_LINE : CLEAR_LINE);
	
		simpsons_video_banking( data & 3 );
	
		simpsons_firq_enabled = data & 0x04;
	} };
	
	/***************************************************************************
	
	  Coin Counters, Sound Interface
	
	***************************************************************************/
	
	public static WriteHandlerPtr simpsons_coin_counter_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* bit 0,1 coin counters */
		coin_counter_w.handler(0,data & 0x01);
		coin_counter_w.handler(1,data & 0x02);
		/* bit 2 selects mono or stereo sound */
		/* bit 3 = enable char ROM reading through the video RAM */
		K052109_set_RMRD_line((data & 0x08)!=0 ? ASSERT_LINE : CLEAR_LINE);
		/* bit 4 = INIT (unknown) */
		/* bit 5 = enable sprite ROM reading */
		K053246_set_OBJCHA_line((~data & 0x20)!=0 ? ASSERT_LINE : CLEAR_LINE);
	} };
	
	public static ReadHandlerPtr simpsons_sound_interrupt_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		cpu_cause_interrupt( 1, 0xff );
		return 0x00;
	} };
	static int res = 0x80;
	public static ReadHandlerPtr simpsons_sound_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* If the sound CPU is running, read the status, otherwise
		   just make it pass the test */
		if (Machine.sample_rate != 0) 	return K053260_ReadReg.handler(2 + offset);
		else
		{
			
	
			res = (res & 0xfc) | ((res + 1) & 0x03);
			return offset!=0 ? res : 0x00;
		}
	} };
	
	/***************************************************************************
	
	  Speed up memory handlers
	
	***************************************************************************/
	
	public static ReadHandlerPtr simpsons_speedup1_r = new ReadHandlerPtr() { public int handler(int offs)
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		int data1 = RAM.read(0x486a);
	
		if ( data1 == 0 )
		{
			int data2 = ( RAM.read(0x4942) << 8 ) | RAM.read(0x4943);
	
			if ( data2 < memory_region_length(REGION_CPU1) )
			{
				data2 = ( RAM.read(data2) << 8 ) | RAM.read(data2 + 1);
	
				if ( data2 == 0xffff )
					cpu_spinuntil_int();
	
				return RAM.read(0x4942);
			}
	
			return RAM.read(0x4942);
		}
	
		if ( data1 == 1 )
                {                
                    RAM.write(0x486a,RAM.read(0x486a)-1); //RAM[0x486a]--;  //probably correct TODO recheck?		   
                }
	
		return RAM.read(0x4942);
	} };
	
	public static ReadHandlerPtr simpsons_speedup2_r = new ReadHandlerPtr() { public int handler(int offs)
	{
		int data = memory_region(REGION_CPU1).read(0x4856);
	
		if ( data == 1 )
			cpu_spinuntil_int();
	
		return data;
	} };
	
	/***************************************************************************
	
	  Banking, initialization
	
	***************************************************************************/
	
	public static konami_cpu_setlines_callbackPtr simpsons_banking = new konami_cpu_setlines_callbackPtr() { public void handler(int lines)
        {
		UBytePtr RAM = memory_region(REGION_CPU1);
		int offs = 0;
	
		switch ( lines & 0xf0 )
		{
			case 0x00: /* simp_g02.rom */
				offs = 0x10000 + ( ( lines & 0x0f ) * 0x2000 );
			break;
	
			case 0x10: /* simp_p01.rom */
				offs = 0x30000 + ( ( lines & 0x0f ) * 0x2000 );
			break;
	
			case 0x20: /* simp_013.rom */
				offs = 0x50000 + ( ( lines & 0x0f ) * 0x2000 );
			break;
	
			case 0x30: /* simp_012.rom ( lines goes from 0x00 to 0x0c ) */
				offs = 0x70000 + ( ( lines & 0x0f ) * 0x2000 );
			break;
	
			default:
				if (errorlog != null)
					fprintf( errorlog, "PC = %04x : Unknown bank selected (%02x)\n", cpu_get_pc(), lines );
			break;
		}
	
		cpu_setbank( 1, new UBytePtr(RAM,offs) );
	}};
	public static InitMachinePtr simpsons_init_machine = new InitMachinePtr() {
        public void handler() {

		UBytePtr RAM = memory_region(REGION_CPU1);
	
		konami_cpu_setlines_callback = simpsons_banking;
	
		paletteram = new UBytePtr(RAM,0x88000);
		simpsons_xtraram = new UBytePtr(RAM,0x89000);
		simpsons_firq_enabled = 0;
	
		/* init the default banks */
		cpu_setbank( 1, new UBytePtr(RAM,0x10000) );
	
		RAM = memory_region(REGION_CPU2);
	
		cpu_setbank( 2, new UBytePtr(RAM,0x10000) );
	
		simpsons_video_banking( 0 );
	}};
}
