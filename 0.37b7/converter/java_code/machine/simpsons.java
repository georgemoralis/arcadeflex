/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package machine;

public class simpsons
{
	
	/* from vidhrdw */
	extern void simpsons_video_banking( int select );
	extern UBytePtr simpsons_xtraram;
	
	int simpsons_firq_enabled;
	
	/***************************************************************************
	
	  EEPROM
	
	***************************************************************************/
	
	static int init_eeprom_count;
	
	
	static EEPROM_interface eeprom_interface = new EEPROM_interface
	(
		7,				/* address bits */
		8,				/* data bits */
		"011000",		/*  read command */
		"011100",		/* write command */
		0,				/* erase command */
		"0100000000000",/* lock command */
		"0100110000000" /* unlock command */
	);
	
	public static nvramPtr simpsons_nvram_handler  = new nvramPtr() { public void handler(Object file, int read_or_write) 
	{
		if (read_or_write != 0)
			EEPROM_save(file);
		else
		{
			EEPROM_init(&eeprom_interface);
	
			if (file != 0)
			{
				init_eeprom_count = 0;
				EEPROM_load(file);
			}
			else
				init_eeprom_count = 10;
		}
	} };
	
	public static ReadHandlerPtr simpsons_eeprom_r  = new ReadHandlerPtr() { public int handler(int offset)
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
	
	public static WriteHandlerPtr simpsons_eeprom_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ( data == 0xff )
			return;
	
		EEPROM_write_bit(data & 0x80);
		EEPROM_set_cs_line((data & 0x08) ? CLEAR_LINE : ASSERT_LINE);
		EEPROM_set_clock_line((data & 0x10) ? ASSERT_LINE : CLEAR_LINE);
	
		simpsons_video_banking( data & 3 );
	
		simpsons_firq_enabled = data & 0x04;
	} };
	
	/***************************************************************************
	
	  Coin Counters, Sound Interface
	
	***************************************************************************/
	
	public static WriteHandlerPtr simpsons_coin_counter_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* bit 0,1 coin counters */
		coin_counter_w.handler(0,data & 0x01);
		coin_counter_w.handler(1,data & 0x02);
		/* bit 2 selects mono or stereo sound */
		/* bit 3 = enable char ROM reading through the video RAM */
		K052109_set_RMRD_line((data & 0x08) ? ASSERT_LINE : CLEAR_LINE);
		/* bit 4 = INIT (unknown) */
		/* bit 5 = enable sprite ROM reading */
		K053246_set_OBJCHA_line((~data & 0x20) ? ASSERT_LINE : CLEAR_LINE);
	} };
	
	public static ReadHandlerPtr simpsons_sound_interrupt_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		cpu_cause_interrupt( 1, 0xff );
		return 0x00;
	} };
	
	public static ReadHandlerPtr simpsons_sound_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* If the sound CPU is running, read the status, otherwise
		   just make it pass the test */
		if (Machine.sample_rate != 0) 	return K053260_r(2 + offset);
		else
		{
			static int res = 0x80;
	
			res = (res & 0xfc) | ((res + 1) & 0x03);
			return offset ? res : 0x00;
		}
	} };
	
	/***************************************************************************
	
	  Speed up memory handlers
	
	***************************************************************************/
	
	public static ReadHandlerPtr simpsons_speedup1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		int data1 = RAM[0x486a];
	
		if ( data1 == 0 )
		{
			int data2 = ( RAM[0x4942] << 8 ) | RAM[0x4943];
	
			if ( data2 < memory_region_length(REGION_CPU1) )
			{
				data2 = ( RAM[data2] << 8 ) | RAM[data2 + 1];
	
				if ( data2 == 0xffff )
					cpu_spinuntil_int();
	
				return RAM[0x4942];
			}
	
			return RAM[0x4942];
		}
	
		if ( data1 == 1 )
			RAM[0x486a]--;
	
		return RAM[0x4942];
	} };
	
	public static ReadHandlerPtr simpsons_speedup2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int data = memory_region(REGION_CPU1)[0x4856];
	
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
				logerror("PC = %04x : Unknown bank selected (%02x)\n", cpu_get_pc(), lines );
			break;
		}
	
		cpu_setbank( 1, &RAM[offs] );
	} };
	
	public static InitMachinePtr simpsons_init_machine = new InitMachinePtr() { public void handler() 
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		konami_cpu_setlines_callback = simpsons_banking;
	
		paletteram = &RAM[0x88000];
		simpsons_xtraram = &RAM[0x89000];
		simpsons_firq_enabled = 0;
	
		/* init the default banks */
		cpu_setbank( 1, &RAM[0x10000] );
	
		RAM = memory_region(REGION_CPU2);
	
		cpu_setbank( 2, &RAM[0x10000] );
	
		simpsons_video_banking( 0 );
	} };
}
