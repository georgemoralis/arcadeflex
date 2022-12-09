/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package gr.codebb.arcadeflex.v036.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static common.libc.cstring.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static common.libc.cstdlib.rand;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.kaneko16.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.platform.fileio.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295H.*;
import static arcadeflex.v036.sound.ay8910H.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.vidhrdw.generic.*;



public class kaneko16
{
	
	/* Variables only used here: */
	
	static int shogwarr_mcu_status, shogwarr_mcu_command_offset;
	public static UBytePtr mcu_ram= new UBytePtr(8);
        public static UBytePtr gtmr_mcu_com= new UBytePtr(8);
	
	
	
	public static InitMachineHandlerPtr gtmr_init_machine = new InitMachineHandlerPtr() { public void handler() 
	{
		kaneko16_bgram = new UBytePtr(kaneko16_fgram , 0x1000);
		kaneko16_spritetype = 1;	// "standard" sprites
	
		memset(gtmr_mcu_com,0,8);
	} };
	
	
	public static InitMachineHandlerPtr shogwarr_init_machine = new InitMachineHandlerPtr() { public void handler() 
	{
		kaneko16_bgram =  new UBytePtr(kaneko16_fgram , 0x1000);
		kaneko16_spritetype = 0;	// differently mapped attribute word
	
		shogwarr_mcu_status = 0;
		shogwarr_mcu_command_offset = 0;
	} };
	
	
	public static InitMachineHandlerPtr berlwall_init_machine = new InitMachineHandlerPtr() { public void handler() 
	{
		kaneko16_bgram =  new UBytePtr(kaneko16_fgram , 0x1000);
		kaneko16_spritetype = 2;	// like type 0, but using 16 instead of 8 bytes
	} };
	
	
	
	
	/***************************************************************************
	
								MCU Code simulation
	
	***************************************************************************/
	
	
	
	/***************************************************************************
								[ The Berlin Wall ]
	***************************************************************************/
	
	/* I think the MCU is in charge of the sound too */
	
	public static ReadHandlerPtr berlwall_mcu_ram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (offset)
		{
			case 0x001c:	return readinputport(4);
			case 0x001e:	return readinputport(5);
			case 0x0400:	return 0x0000;
			default:		return mcu_ram.READ_WORD(offset);
		}
	} };
	
	
	
	
	/***************************************************************************
							[ Great 1000 Miles Rally ]
	***************************************************************************/
	
	
	/* The MCU has access to NVRAM */
	public static void gtmr_mcu_run()
	{
		int mcu_command	=	mcu_ram.READ_WORD(0x0010);
		int mcu_offset	=	mcu_ram.READ_WORD(0x0012);
		int mcu_data	=	mcu_ram.READ_WORD(0x0014);
	
		if (errorlog!=null) fprintf(errorlog,
		 "CPU #0 PC %06X : MCU executed command: %04X %04X %04X\n",cpu_get_pc(),mcu_command,mcu_offset,mcu_data);
	
		switch (mcu_command >> 8)
		{
	
			case 0x02:	// Read from NVRAM
			{
				Object f;
				if ((f = osd_fopen(Machine.gamedrv.name,null,OSD_FILETYPE_NVRAM,0)) != null)
				{
					osd_fread(f,mcu_ram,mcu_offset, 128);
					osd_fclose(f);
				}
			}
			break;
	
			case 0x42:	// Write to NVRAM
			{
				Object f;
				if ((f = osd_fopen(Machine.gamedrv.name,null,OSD_FILETYPE_NVRAM,1)) != null)
				{
					osd_fwrite(f,mcu_ram,mcu_offset, 128);
					osd_fclose(f);
				}
			}
			break;
	
			case 0x03:	// DSW
			{
				mcu_ram.WRITE_WORD(mcu_offset, readinputport(4));
			}
			break;
	
			case 0x04:	// TEST (2 versions)
			{
				if (Machine.gamedrv == driver_gtmr)
				{
					/* MCU writes the string "MM0525-TOYBOX199" to shared ram */
					mcu_ram.WRITE_WORD(mcu_offset+0x00, 0x4d4d );
					mcu_ram.WRITE_WORD(mcu_offset+0x02, 0x3035 );
					mcu_ram.WRITE_WORD(mcu_offset+0x04, 0x3235 );
					mcu_ram.WRITE_WORD(mcu_offset+0x06, 0x2d54 );
					mcu_ram.WRITE_WORD(mcu_offset+0x08, 0x4f59 );
					mcu_ram.WRITE_WORD(mcu_offset+0x0a, 0x424f );
					mcu_ram.WRITE_WORD(mcu_offset+0x0c, 0x5831 );
					mcu_ram.WRITE_WORD(mcu_offset+0x0e, 0x3939 );
				}
	
				if (Machine.gamedrv == driver_gtmre)
				{
					/* MCU writes the string "USMM0713-TB1994 " to shared ram */
					mcu_ram.WRITE_WORD(mcu_offset+0x00, 0x5553 );
					mcu_ram.WRITE_WORD(mcu_offset+0x02, 0x4d4d );
					mcu_ram.WRITE_WORD(mcu_offset+0x04, 0x3037 );
					mcu_ram.WRITE_WORD(mcu_offset+0x06, 0x3133 );
					mcu_ram.WRITE_WORD(mcu_offset+0x08, 0x2d54 );
					mcu_ram.WRITE_WORD(mcu_offset+0x0a, 0x4231 );
					mcu_ram.WRITE_WORD(mcu_offset+0x0c, 0x3939 );
					mcu_ram.WRITE_WORD(mcu_offset+0x0e, 0x3420 );
				}
			}
			break;
		}
	
	}
	
	
	public static WriteHandlerPtr gtmr_mcu_com0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{ 
		COMBINE_WORD_MEM(gtmr_mcu_com,0 * 2, data); 
		if (gtmr_mcu_com.READ_WORD(0) != 0xFFFF)	return; 
		if (gtmr_mcu_com.READ_WORD(2) != 0xFFFF)	return; 
		if (gtmr_mcu_com.READ_WORD(4) != 0xFFFF)	return; 
		if (gtmr_mcu_com.READ_WORD(6) != 0xFFFF)	return; 
	
		memset(gtmr_mcu_com,0,8); 
		gtmr_mcu_run(); 
	} };
        public static WriteHandlerPtr gtmr_mcu_com1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{ 
		COMBINE_WORD_MEM(gtmr_mcu_com,1 * 2, data); 
		if (gtmr_mcu_com.READ_WORD(0) != 0xFFFF)	return; 
		if (gtmr_mcu_com.READ_WORD(2) != 0xFFFF)	return; 
		if (gtmr_mcu_com.READ_WORD(4) != 0xFFFF)	return; 
		if (gtmr_mcu_com.READ_WORD(6) != 0xFFFF)	return; 
	
		memset(gtmr_mcu_com,0,8); 
		gtmr_mcu_run(); 
	} };
        public static WriteHandlerPtr gtmr_mcu_com2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{ 
		COMBINE_WORD_MEM(gtmr_mcu_com,2 * 2, data); 
		if (gtmr_mcu_com.READ_WORD(0) != 0xFFFF)	return; 
		if (gtmr_mcu_com.READ_WORD(2) != 0xFFFF)	return; 
		if (gtmr_mcu_com.READ_WORD(4) != 0xFFFF)	return; 
		if (gtmr_mcu_com.READ_WORD(6) != 0xFFFF)	return; 
	
		memset(gtmr_mcu_com,0,8); 
		gtmr_mcu_run(); 
	} };
        public static WriteHandlerPtr gtmr_mcu_com3_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{ 
		COMBINE_WORD_MEM(gtmr_mcu_com,3 * 2, data); 
		if (gtmr_mcu_com.READ_WORD(0) != 0xFFFF)	return; 
		if (gtmr_mcu_com.READ_WORD(2) != 0xFFFF)	return; 
		if (gtmr_mcu_com.READ_WORD(4) != 0xFFFF)	return; 
		if (gtmr_mcu_com.READ_WORD(6) != 0xFFFF)	return; 
	
		memset(gtmr_mcu_com,0,8); 
		gtmr_mcu_run(); 
	} };

	
	/***************************************************************************
								[ Shogun Warriors ]
	***************************************************************************/
	
	/* Preliminary simulation: the game doesn't work */
	
	/* The MCU has access to NVRAM */
	public static void shogwarr_mcu_run()
	{
		int mcu_command;
	
		if ( shogwarr_mcu_status != (1|2|4|8) )	return;
	
		mcu_command = mcu_ram.READ_WORD(shogwarr_mcu_command_offset);
	
		if (mcu_command==0) return;
	
		if (errorlog!=null) fprintf(errorlog,
		 "CPU #0 PC %06X : MCU executed command at %04X: %04X\n",
		 	cpu_get_pc(),shogwarr_mcu_command_offset,mcu_command);
	
		switch (mcu_command)
		{
	
			case 0x00ff:
			{
				int param1 = mcu_ram.READ_WORD(shogwarr_mcu_command_offset + 0x0002);
				int param2 = mcu_ram.READ_WORD(shogwarr_mcu_command_offset + 0x0004);
				int param3 = mcu_ram.READ_WORD(shogwarr_mcu_command_offset + 0x0006);
	//			int param4 = READ_WORD(&mcu_ram[shogwarr_mcu_command_offset + 0x0008]);
				int param5 = mcu_ram.READ_WORD(shogwarr_mcu_command_offset + 0x000a);
	//			int param6 = READ_WORD(&mcu_ram[shogwarr_mcu_command_offset + 0x000c]);
	//			int param7 = READ_WORD(&mcu_ram[shogwarr_mcu_command_offset + 0x000e]);
	
				// clear old command (handshake to main cpu)
				mcu_ram.WRITE_WORD(shogwarr_mcu_command_offset, 0x0000);
	
				// execute the command:
	
				COMBINE_WORD_MEM(mcu_ram,param1 & (~1),
					(param1 & 1)!=0 ?
						(0xff000000 | (~readinputport(4)<<0) ) :
						(0x00ff0000 | (~readinputport(4)<<8) ) );	// DSW
	
				COMBINE_WORD_MEM(mcu_ram,param2 & (~1),
					(param2 & 1)!=0 ?
						(0xff000000 | (0xff<<0) ) :
						(0x00ff0000 | (0xff<<8) ) );	// ? -1 / anything else
	
				shogwarr_mcu_command_offset = param3;	// where next command will be written?
				// param 4?
				mcu_ram.WRITE_WORD(param5, 0x8ee4);	// MCU Rom Checksum
				// param 6&7 = address.l
			}
			break;
	
	
			case 0x0001:
			{
	//			int param1 = READ_WORD(&mcu_ram[shogwarr_mcu_command_offset + 0x0002]);
				int param2 = mcu_ram.READ_WORD(shogwarr_mcu_command_offset + 0x0004);
	
				// clear old command (handshake to main cpu)
				mcu_ram.WRITE_WORD(shogwarr_mcu_command_offset, 0x0000);
	
				// execute the command:
	
				// param1 ?
				mcu_ram.WRITE_WORD(param2+0x0000, 0x0000 );	// ?
				mcu_ram.WRITE_WORD(param2+0x0002, 0x0000 );	// ?
				mcu_ram.WRITE_WORD(param2+0x0004, 0x0000 );	// ?
	
				mcu_ram.WRITE_WORD(param2+0x0006, 0x0000 );	// ? addr.l
				mcu_ram.WRITE_WORD(param2+0x0008, 0x00e0 );	// 0000e0: 4e73 rte
	
			}
			break;
	
	
			case 0x0002:
			{
	//			int param1 = READ_WORD(&mcu_ram[shogwarr_mcu_command_offset + 0x0002]);
	//			int param2 = READ_WORD(&mcu_ram[shogwarr_mcu_command_offset + 0x0004]);
	//			int param3 = READ_WORD(&mcu_ram[shogwarr_mcu_command_offset + 0x0006]);
	//			int param4 = READ_WORD(&mcu_ram[shogwarr_mcu_command_offset + 0x0008]);
	//			int param5 = READ_WORD(&mcu_ram[shogwarr_mcu_command_offset + 0x000a]);
	//			int param6 = READ_WORD(&mcu_ram[shogwarr_mcu_command_offset + 0x000c]);
	//			int param7 = READ_WORD(&mcu_ram[shogwarr_mcu_command_offset + 0x000e]);
	
				// clear old command (handshake to main cpu)
				mcu_ram.WRITE_WORD(shogwarr_mcu_command_offset, 0x0000);
	
				// execute the command:
	
			}
			break;
	
		}
	
	}
	
	
	
	public static WriteHandlerPtr shogwarr_mcu_ram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(mcu_ram,offset, data);
		shogwarr_mcu_run();
	} };
	
	
	

	public static WriteHandlerPtr shogwarr_mcu_com0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{ 
		shogwarr_mcu_status |= (1 << 0); 
		shogwarr_mcu_run(); 
	} };
	public static WriteHandlerPtr shogwarr_mcu_com1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{ 
		shogwarr_mcu_status |= (1 << 1); 
		shogwarr_mcu_run(); 
	} };
        public static WriteHandlerPtr shogwarr_mcu_com2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{ 
		shogwarr_mcu_status |= (1 << 2); 
		shogwarr_mcu_run(); 
	} };
        public static WriteHandlerPtr shogwarr_mcu_com3_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{ 
		shogwarr_mcu_status |= (1 << 3); 
		shogwarr_mcu_run(); 
	} };

		
	
	/***************************************************************************
	
									Memory Maps
	
	***************************************************************************/
	
	
	public static ReadHandlerPtr kaneko16_rnd_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return rand();
	} };
	
	/* bit 0 of this byte is set after a coin insertion,
	   then reset after a short while */
	public static WriteHandlerPtr kaneko16_coin_lockout_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if ((data & 0xff000000)==0)
		{
			coin_lockout_w.handler(0, ((~data) >> 11) & 1 );
			coin_lockout_w.handler(1, ((~data) >> 10) & 1 );
		}
	} };
	
	
	/***************************************************************************
								[ The Berlin Wall ]
	***************************************************************************/
	
	static MemoryReadAddress berlwall_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM					),	// ROM
		new MemoryReadAddress( 0x200000, 0x20ffff, MRA_BANK1					),	// RAM
		new MemoryReadAddress( 0x30e000, 0x30ffff, MRA_BANK2					),	// Sprites
		new MemoryReadAddress( 0x400000, 0x400fff, MRA_BANK3					),	// Palette
	//	new MemoryReadAddress( 0x480000, 0x480001, MRA_BANK4					),	// ?
		new MemoryReadAddress( 0x500000, 0x500001, kaneko16_bg15_reg_r		),	// High Color Background
		new MemoryReadAddress( 0x580000, 0x580001, kaneko16_bg15_select_r	),
		new MemoryReadAddress( 0x600000, 0x60003f, MRA_BANK5					),	// Screen Regs ?
		new MemoryReadAddress( 0x680000, 0x680001, input_port_0_r			),	// Inputs
		new MemoryReadAddress( 0x680002, 0x680003, input_port_1_r			),
		new MemoryReadAddress( 0x680004, 0x680005, input_port_2_r			),
	//	new MemoryReadAddress( 0x680006, 0x680007, input_port_3_r			),
		new MemoryReadAddress( 0x780000, 0x780001, watchdog_reset_r			),	// Watchdog
		new MemoryReadAddress( 0x800000, 0x80ffff, berlwall_mcu_ram_r		),	// Shared With MCU
		new MemoryReadAddress( 0xc00000, 0xc03fff, MRA_BANK7					),	// Layers 1
		new MemoryReadAddress( 0xd00000, 0xd0001f, MRA_BANK8					),	// Layers 1 Regs
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress berlwall_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM											),	// ROM
		new MemoryWriteAddress( 0x200000, 0x20ffff, MWA_BANK1											),	// RAM
		new MemoryWriteAddress( 0x30e000, 0x30ffff, MWA_BANK2, spriteram, spriteram_size			),	// Sprites
		new MemoryWriteAddress( 0x400000, 0x400fff, kaneko16_paletteram_w, paletteram				),	// Palette
	//	new MemoryWriteAddress( 0x480000, 0x480001, MWA_BANK4											),	// ?
		new MemoryWriteAddress( 0x500000, 0x500001, kaneko16_bg15_reg_w, kaneko16_bg15_reg			),	// High Color Background
		new MemoryWriteAddress( 0x580000, 0x580001, kaneko16_bg15_select_w, kaneko16_bg15_select		),
		new MemoryWriteAddress( 0x600000, 0x60003f, kaneko16_screen_regs_w, kaneko16_screen_regs		),	// Screen Regs ?
		new MemoryWriteAddress( 0x700000, 0x700001, kaneko16_coin_lockout_w							),	// Coin Lockout
		new MemoryWriteAddress( 0x800000, 0x80ffff, MWA_BANK6, mcu_ram								),	// Shared With MCU
		new MemoryWriteAddress( 0xc00000, 0xc03fff, kaneko16_layers1_w, kaneko16_fgram				),	// Layers 1
		new MemoryWriteAddress( 0xd00000, 0xd0001f, kaneko16_layers1_regs_w, kaneko16_layers1_regs	),	// Layers 1 Regs
		new MemoryWriteAddress( -1 )
	};
	
	
	
	
	/***************************************************************************
							[ Great 1000 Miles Rally ]
	***************************************************************************/
	
	
	public static ReadHandlerPtr gtmr_wheel_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if ( (readinputport(4) & 0x1800) == 0x10)	// DSW setting
			return	readinputport(5)<<8;			// 360� Wheel
		else
			return	readinputport(5);				// 270� Wheel
	} };
	
	static int bank0;
	public static WriteHandlerPtr gtmr_oki_0_bank_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		OKIM6295_set_bank_base(0, ALL_VOICES, 0x10000 * (data & 0xF) );
		bank0 = (data & 0xF);
	//	if (errorlog) fprintf(errorlog, "CPU #0 PC %06X : OKI0 bank %08X\n",cpu_get_pc(),data);
	} };
	
	public static WriteHandlerPtr gtmr_oki_1_bank_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		OKIM6295_set_bank_base(1, ALL_VOICES, 0x40000 * (data & 0x1) );
	//	if (errorlog) fprintf(errorlog, "CPU #0 PC %06X : OKI1 bank %08X\n",cpu_get_pc(),data);
	} };
	
	/*
		If you look at the samples ROM for the OKI chip #0, you'll see
		it's divided into 16 chunks, each chunk starting with the header
		holding the samples	addresses. But, except for chunk 0, the first
		$100 bytes ($20 samples) of each chunk are empty, and despite that,
		samples in the range $0-1f are played. So, whenever a samples in
		this range is requested, we use the address and sample from chunk 0,
		otherwise we use those from the selected bank. By using this scheme
		the sound improves, but I wouldn't bet it's correct..
	*/
	static int pend_oki = 0;
	public static WriteHandlerPtr gtmr_oki_0_data_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		
	
		if (pend_oki!=0)	pend_oki = 0;
		else
		{
			if ((data & 0x80)!=0)
			{
				int samp = data &0x7f;
	
				pend_oki = 1;
				if (samp < 0x20)
				{
					OKIM6295_set_bank_base(0, ALL_VOICES, 0);
	//				if (errorlog) fprintf(errorlog, "Setting OKI0 bank to zero\n");
				}
				else
					OKIM6295_set_bank_base(0, ALL_VOICES, 0x10000 * bank0 );
			}
		}
	
		OKIM6295_data_0_w.handler(offset,data);
	//	if (errorlog) fprintf(errorlog, "CPU #0 PC %06X : OKI0 <- %08X\n",cpu_get_pc(),data);
	} };
	
	public static WriteHandlerPtr gtmr_oki_1_data_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		OKIM6295_data_1_w.handler(offset,data);
	//	if (errorlog) fprintf(errorlog, "CPU #0 PC %06X : OKI1 <- %08X\n",cpu_get_pc(),data);
	} };
	
	
	
	
	static MemoryReadAddress gtmr_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x0ffffd, MRA_ROM					),	// ROM
		new MemoryReadAddress( 0x0ffffe, 0x0fffff, gtmr_wheel_r				),	// Wheel Value
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1					),	// RAM
		new MemoryReadAddress( 0x200000, 0x20ffff, MRA_BANK2					),	// Shared With MCU
		new MemoryReadAddress( 0x300000, 0x327fff, MRA_BANK3					),	// Palette (300000-30ffff)
		new MemoryReadAddress( 0x400000, 0x401fff, MRA_BANK4					),	// Sprites
		new MemoryReadAddress( 0x500000, 0x503fff, MRA_BANK5					),	// Layers 1
		new MemoryReadAddress( 0x580000, 0x583fff, MRA_BANK6					),	// Layers 2
		new MemoryReadAddress( 0x600000, 0x60000f, MRA_BANK7					),	// Layers 1 Regs
		new MemoryReadAddress( 0x680000, 0x68000f, MRA_BANK8					),	// Layers 2 Regs
		new MemoryReadAddress( 0x700000, 0x70001f, kaneko16_screen_regs_r	),	// Screen Regs ?
		new MemoryReadAddress( 0x800000, 0x800001, OKIM6295_status_0_r		),	// Samples
		new MemoryReadAddress( 0x880000, 0x880001, OKIM6295_status_1_r		),
		new MemoryReadAddress( 0x900014, 0x900015, kaneko16_rnd_r			),	// Random Number ?
		new MemoryReadAddress( 0xa00000, 0xa00001, watchdog_reset_r			),	// Watchdog
		new MemoryReadAddress( 0xb00000, 0xb00001, input_port_0_r			),	// Inputs
		new MemoryReadAddress( 0xb00002, 0xb00003, input_port_1_r			),
		new MemoryReadAddress( 0xb00004, 0xb00005, input_port_2_r			),
		new MemoryReadAddress( 0xb00006, 0xb00007, input_port_3_r			),
		new MemoryReadAddress( 0xd00000, 0xd00001, MRA_NOP					),	// ? (bit 0)
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress gtmr_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x0fffff, MWA_ROM					),	// ROM
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1					),	// RAM
		new MemoryWriteAddress( 0x200000, 0x20ffff, MWA_BANK2, mcu_ram		),	// Shared With MCU
		new MemoryWriteAddress( 0x2a0000, 0x2a0001, gtmr_mcu_com0_w			),	// To MCU ?
		new MemoryWriteAddress( 0x2b0000, 0x2b0001, gtmr_mcu_com1_w			),
		new MemoryWriteAddress( 0x2c0000, 0x2c0001, gtmr_mcu_com2_w			),
		new MemoryWriteAddress( 0x2d0000, 0x2d0001, gtmr_mcu_com3_w			),
		new MemoryWriteAddress( 0x300000, 0x327fff, gtmr_paletteram_w, paletteram					),	// Palette
		new MemoryWriteAddress( 0x400000, 0x401fff, MWA_BANK4, spriteram, spriteram_size			),	// Sprites
		new MemoryWriteAddress( 0x500000, 0x503fff, kaneko16_layers1_w, kaneko16_fgram				),	// Layers 1
		new MemoryWriteAddress( 0x580000, 0x583fff, MWA_BANK6											),	// Layers 2
		new MemoryWriteAddress( 0x600000, 0x60000f, kaneko16_layers1_regs_w, kaneko16_layers1_regs	),	// Layers 1 Regs
		new MemoryWriteAddress( 0x680000, 0x68000f, kaneko16_layers2_regs_w, kaneko16_layers2_regs	),	// Layers 2 Regs
		new MemoryWriteAddress( 0x700000, 0x70001f, kaneko16_screen_regs_w, kaneko16_screen_regs		),	// Screen Regs ?
		new MemoryWriteAddress( 0x800000, 0x800001, gtmr_oki_0_data_w			),	// Samples
		new MemoryWriteAddress( 0x880000, 0x880001, gtmr_oki_1_data_w			),
		new MemoryWriteAddress( 0xa00000, 0xa00001, watchdog_reset_w			),	// Watchdog
		new MemoryWriteAddress( 0xb80000, 0xb80001, kaneko16_coin_lockout_w	),	// Coin Lockout
	//	new MemoryWriteAddress( 0xc00000, 0xc00001, MWA_NOP					),	// ?
		new MemoryWriteAddress( 0xe00000, 0xe00001, gtmr_oki_0_bank_w			),	// Samples Bankswitching
		new MemoryWriteAddress( 0xe80000, 0xe80001, gtmr_oki_1_bank_w			),
		new MemoryWriteAddress( -1 )
	};
	
	
	
	
	/***************************************************************************
								[ Shogun Warriors ]
	***************************************************************************/
	
	/* Untested */
	public static WriteHandlerPtr shogwarr_oki_bank_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		OKIM6295_set_bank_base(0, ALL_VOICES, 0x10000 * ((data >> 0) & 0x3) );
		OKIM6295_set_bank_base(1, ALL_VOICES, 0x10000 * ((data >> 4) & 0x3) );
	} };
	
	static MemoryReadAddress shogwarr_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM				),	// ROM
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1				),	// RAM
		new MemoryReadAddress( 0x200000, 0x20ffff, MRA_BANK2				),	// Shared With MCU
		new MemoryReadAddress( 0x380000, 0x380fff, MRA_BANK3				),	// Palette
		new MemoryReadAddress( 0x400000, 0x400001, OKIM6295_status_0_r	),	// Samples
		new MemoryReadAddress( 0x480000, 0x480001, OKIM6295_status_1_r	),
		new MemoryReadAddress( 0x580000, 0x581fff, MRA_BANK4				),	// Sprites
		new MemoryReadAddress( 0x600000, 0x603fff, MRA_BANK6				),	// Layers 1
		new MemoryReadAddress( 0x800000, 0x80000f, MRA_BANK7				),	// Layers 1 Regs
		new MemoryReadAddress( 0x900000, 0x90001f, MRA_BANK8				),	// Screen Regs ?
		new MemoryReadAddress( 0xa00014, 0xa00015, kaneko16_rnd_r		),	// Random Number ?
		new MemoryReadAddress( 0xa80000, 0xa80001, watchdog_reset_r		),	// Watchdog
		new MemoryReadAddress( 0xb80000, 0xb80001, input_port_0_r		),	// Inputs
		new MemoryReadAddress( 0xb80002, 0xb80003, input_port_1_r		),
		new MemoryReadAddress( 0xb80004, 0xb80005, input_port_2_r		),
		new MemoryReadAddress( 0xb80006, 0xb80007, input_port_3_r		),
		new MemoryReadAddress( 0xd00000, 0xd00001, MRA_NOP				),	// ? (bit 0)
		new MemoryReadAddress( -1 )
	};
	static MemoryWriteAddress shogwarr_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM								),	// ROM
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1								),	// RAM
		new MemoryWriteAddress( 0x200000, 0x20ffff, shogwarr_mcu_ram_w, mcu_ram			),	// Shared With MCU
		new MemoryWriteAddress( 0x280000, 0x280001, shogwarr_mcu_com0_w					),	// To MCU ?
		new MemoryWriteAddress( 0x290000, 0x290001, shogwarr_mcu_com1_w					),
		new MemoryWriteAddress( 0x2b0000, 0x2b0001, shogwarr_mcu_com2_w					),
		new MemoryWriteAddress( 0x2d0000, 0x2d0001, shogwarr_mcu_com3_w					),
		new MemoryWriteAddress( 0x380000, 0x380fff, kaneko16_paletteram_w, paletteram	),	// Palette
		new MemoryWriteAddress( 0x400000, 0x400001, OKIM6295_data_0_w						),	// Samples
		new MemoryWriteAddress( 0x480000, 0x480001, OKIM6295_data_1_w						),
		new MemoryWriteAddress( 0x580000, 0x581fff, MWA_BANK4, spriteram, spriteram_size			),	// Sprites
		new MemoryWriteAddress( 0x600000, 0x603fff, kaneko16_layers1_w, kaneko16_fgram				),	// Layers 1
		new MemoryWriteAddress( 0x800000, 0x80000f, kaneko16_layers1_regs_w, kaneko16_layers1_regs	),	// Layers 1 Regs
		new MemoryWriteAddress( 0x900000, 0x90001f, kaneko16_screen_regs_w, kaneko16_screen_regs		),	// Screen Regs ?
		new MemoryWriteAddress( 0xa80000, 0xa80001, watchdog_reset_w						),	// Watchdog
		new MemoryWriteAddress( 0xd00000, 0xd00001, MWA_NOP								),	// ?
		new MemoryWriteAddress( 0xe00000, 0xe00001, shogwarr_oki_bank_w					),	// Samples Bankswitching
		new MemoryWriteAddress( -1 )
	};
	
	
	
	
	/***************************************************************************
	
									Input Ports
	
	***************************************************************************/
	
	
	
	/***************************************************************************
							[ The Berlin Wall (set 1) ]
	***************************************************************************/
	
	//	Input Ports:	[0] Joy 1			[1] Joy 2
	//					[2] Coins			[3] ?
	//					[4] DSW	1			[5] DSW 2
	
	static InputPortHandlerPtr input_ports_berlwall = new InputPortHandlerPtr(){ public void handler() { 
	
		PORT_START(); 	// IN0 - Player 1 - 680000.w
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT(  0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT(  0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT(  0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT(  0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT(  0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT(  0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	// IN1 - Player 2 - 680002.w
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT(  0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT(  0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT(  0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT(  0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT(  0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT(  0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	// IN2 - Coins - 680004.w
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_START1	);
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_START2	);
		PORT_BIT(  0x0400, IP_ACTIVE_LOW, IPT_COIN1		);
		PORT_BIT(  0x0800, IP_ACTIVE_LOW, IPT_COIN2		);
		PORT_BIT(  0x1000, IP_ACTIVE_LOW, IPT_SERVICE	);// test
		PORT_BIT(  0x2000, IP_ACTIVE_LOW, IPT_TILT		);
		PORT_BIT(  0x4000, IP_ACTIVE_LOW, IPT_COIN3		);// operator's facility
		PORT_BIT(  0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN	);
	
		PORT_START(); 	// IN3 - ? - 680006.w
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x0400, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x0800, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x1000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	// IN4 - DSW 1 - $200018.b <- ! $80001d.b
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Reserved" );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x1c, 0x1c, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x1c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x14, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0xe0, 0xe0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
	
		PORT_START(); 	// IN5 - DSW 2 - $200019.b <- $80001f.b
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy"    );
		PORT_DIPSETTING(    0x03, "Normal"  );
		PORT_DIPSETTING(    0x01, "Hard"    );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Lives") );	// 1p lives at 202982.b
		PORT_DIPSETTING(    0x00, "1" );
		PORT_DIPSETTING(    0x0c, "2" );
		PORT_DIPSETTING(    0x08, "3" );
		PORT_DIPSETTING(    0x04, "5" );
		PORT_DIPNAME( 0x30, 0x30, "Country"   );
		PORT_DIPSETTING(    0x30, "England" );
		PORT_DIPSETTING(    0x20, "Italy"   );
		PORT_DIPSETTING(    0x10, "Germany" );
		PORT_DIPSETTING(    0x00, "Freeze Screen" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
	INPUT_PORTS_END(); }}; 
	
	
	
	/***************************************************************************
							[ The Berlin Wall (set 2) ]
	***************************************************************************/
	
	//	Same as berlwall, but for a different lives setting
	//
	//	Input Ports:	[0] Joy 1			[1] Joy 2
	//					[2] Coins			[3] ?
	//					[4] DSW	1			[5] DSW 2
	
	static InputPortHandlerPtr input_ports_berlwalt = new InputPortHandlerPtr(){ public void handler() { 
	
		PORT_START(); 	// IN0 - Player 1 - 680000.w
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT(  0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT(  0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT(  0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT(  0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT(  0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT(  0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	// IN1 - Player 2 - 680002.w
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT(  0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT(  0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT(  0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT(  0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT(  0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT(  0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	// IN2 - Coins - 680004.w
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_START1	);
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_START2	);
		PORT_BIT(  0x0400, IP_ACTIVE_LOW, IPT_COIN1		);
		PORT_BIT(  0x0800, IP_ACTIVE_LOW, IPT_COIN2		);
		PORT_BIT(  0x1000, IP_ACTIVE_LOW, IPT_SERVICE	);// test
		PORT_BIT(  0x2000, IP_ACTIVE_LOW, IPT_TILT		);
		PORT_BIT(  0x4000, IP_ACTIVE_LOW, IPT_COIN3		);// operator's facility
		PORT_BIT(  0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN	);
	
		PORT_START(); 	// IN3 - ? - 680006.w
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x0400, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x0800, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x1000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	// IN4 - DSW 1 - $80001d.b
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Reserved" );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x1c, 0x1c, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x1c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x14, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0xe0, 0xe0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
	
		PORT_START(); 	// IN5 - DSW 2 - $80001f.b
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy"    );
		PORT_DIPSETTING(    0x03, "Normal"  );
		PORT_DIPSETTING(    0x01, "Hard"    );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "7" );
		PORT_DIPSETTING(    0x04, "5" );
		PORT_DIPSETTING(    0x0c, "3" );
		PORT_DIPSETTING(    0x08, "2" );
		PORT_DIPNAME( 0x30, 0x30, "Country"   );
		PORT_DIPSETTING(    0x30, "England" );
		PORT_DIPSETTING(    0x20, "Italy"   );
		PORT_DIPSETTING(    0x10, "Germany" );
		PORT_DIPSETTING(    0x00, "Freeze Screen" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
	INPUT_PORTS_END(); }}; 
	
	
	
	/***************************************************************************
							[ Great 1000 Miles Rally ]
	***************************************************************************/
	
	//	Input Ports:	[0] Joy 1			[1] Joy 2
	//					[2] Coins			[3] ?
	//					[4] DSW				[5] Driving Wheel
	
	static InputPortHandlerPtr input_ports_gtmr = new InputPortHandlerPtr(){ public void handler() { 
	
		PORT_START(); 	// IN0 - Player 1 - b00000.w
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT(  0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT(  0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT(  0x1000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );// swapped for consistency:
		PORT_BIT(  0x2000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );// button1 is usually accel.
		PORT_BIT(  0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	// IN1 - Player 2 - b00002.w
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT(  0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT(  0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT(  0x1000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );// swapped for consistency:
		PORT_BIT(  0x2000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );// button1 is usually accel.
		PORT_BIT(  0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	// IN2 - Coins - b00004.w
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_START1	);
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_START2	);
		PORT_BIT(  0x0400, IP_ACTIVE_LOW, IPT_COIN1		);
		PORT_BIT(  0x0800, IP_ACTIVE_LOW, IPT_COIN2		);
		PORT_BIT(  0x1000, IP_ACTIVE_LOW, IPT_SERVICE	);// test
		PORT_BIT(  0x2000, IP_ACTIVE_LOW, IPT_TILT		);
		PORT_BIT(  0x4000, IP_ACTIVE_LOW, IPT_COIN3		);// operator's facility
		PORT_BIT(  0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN	);
	
		PORT_START(); 	// IN3 - Seems unused ! - b00006.w
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x0400, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x0800, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x1000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	// IN4 - DSW from the MCU - 101265.b <- 206000.b
		PORT_SERVICE( 0x0100, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x0200, 0x0200, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(      0x0200, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On")  );
		PORT_DIPNAME( 0x0400, 0x0400, DEF_STR( "Cabinet")  );
		PORT_DIPSETTING(      0x0400, DEF_STR( "Upright")  );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x1800, 0x1800, "Controls"    );
		PORT_DIPSETTING(      0x1800, "1 Joystick"  );
		PORT_DIPSETTING(      0x0800, "2 Joysticks" );
		PORT_DIPSETTING(      0x1000, "Wheel (360)");
		PORT_DIPSETTING(      0x0000, "Wheel (270)");
		PORT_DIPNAME( 0x2000, 0x2000, "Use Brake"    );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x2000, DEF_STR( "On")  );
		PORT_DIPNAME( 0xc000, 0xc000, "National Anthem & Flag" );
		PORT_DIPSETTING(      0xc000, "Use Memory"  );
		PORT_DIPSETTING(      0x8000, "Anthem Only" );
		PORT_DIPSETTING(      0x4000, "Flag Only"   );
		PORT_DIPSETTING(      0x0000, "None"        );
	
		PORT_START(); 	// IN5 - Wheel - 100015.b <- ffffe.b
		PORT_ANALOG ( 0x00ff, 0x0080, IPT_AD_STICK_X | IPF_CENTER, 30, 1, 0x00, 0xff );
	
	INPUT_PORTS_END(); }}; 
	
	
	
	
	
	/***************************************************************************
								[ Shogun Warriors ]
	***************************************************************************/
	
	//	Input Ports:	[0] Joy 1			[1] Joy 2
	//					[2] Coins			[3] ?
	//					[4] DSW
	
	static InputPortHandlerPtr input_ports_shogwarr = new InputPortHandlerPtr(){ public void handler() { 
	
		PORT_START(); 	// IN0 - - b80000.w
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT(  0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT(  0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT(  0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT(  0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT(  0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT(  0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );// ? tested
	
		PORT_START(); 	// IN1 - - b80002.w
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT(  0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT(  0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT(  0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT(  0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT(  0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT(  0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );// ? tested
	
		PORT_START(); 	// IN2 - Coins - b80004.w
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_START1	);
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_START2	);
		PORT_BIT(  0x0400, IP_ACTIVE_LOW, IPT_COIN1		);
		PORT_BIT(  0x0800, IP_ACTIVE_LOW, IPT_COIN2		);
		PORT_BIT(  0x1000, IP_ACTIVE_LOW, IPT_SERVICE	);// test
		PORT_BIT(  0x2000, IP_ACTIVE_LOW, IPT_TILT		);
		PORT_BIT(  0x4000, IP_ACTIVE_LOW, IPT_COIN3		);// operator's facility
		PORT_BIT(  0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN	);// ? tested
	
		PORT_START(); 	// IN3 - ? - b80006.w
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x0400, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x0800, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x1000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT(  0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	// IN4 - DSW from the MCU - 102e15.b <- 200059.b
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x02, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x38, "1" );// easy
		PORT_DIPSETTING(    0x30, "2" );
		PORT_DIPSETTING(    0x28, "3" );
		PORT_DIPSETTING(    0x20, "4" );
		PORT_DIPSETTING(    0x18, "5" );
		PORT_DIPSETTING(    0x10, "6" );
		PORT_DIPSETTING(    0x08, "7" );
		PORT_DIPSETTING(    0x00, "8" );
		PORT_DIPNAME( 0x40, 0x40, "Can Join During Game" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );	//	2 credits		winner vs computer
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );	//	1 credit		game over
		PORT_DIPNAME( 0x80, 0x80, "Special Continue Mode" );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
	INPUT_PORTS_END(); }}; 
	
	
	
	
	
	
	/***************************************************************************
	
									Graphics Layouts
	
	***************************************************************************/
	
	
	/*
		16x16x4 made of 4 8x8x4 blocks arrenged like:		 	01
	 	(nibbles are swapped for tiles, not for sprites)		23
	*/

	static GfxLayout layout_4bit_HM = new GfxLayout
	(
		16,16,
		(0x080000)*8/(16*16*4),
		4,
		new int[] {0, 1, 2, 3},
		new int[] {0*4,1*4,2*4,3*4,4*4,5*4,6*4,7*4, 
		 0*4+32*8,1*4+32*8,2*4+32*8,3*4+32*8,4*4+32*8,5*4+32*8,6*4+32*8,7*4+32*8}, 
		new int[] {0*32,1*32,2*32,3*32,4*32,5*32,6*32,7*32,
		 0*32+32*16,1*32+32*16,2*32+32*16,3*32+32*16,4*32+32*16,5*32+32*16,6*32+32*16,7*32+32*16},
		16*16*4
	);
        static GfxLayout layout_4bit_2M = new GfxLayout
	(
		16,16,
		(0x200000)*8/(16*16*4),
		4,
		new int[] {0, 1, 2, 3},
		new int[] {0*4,1*4,2*4,3*4,4*4,5*4,6*4,7*4, 
		 0*4+32*8,1*4+32*8,2*4+32*8,3*4+32*8,4*4+32*8,5*4+32*8,6*4+32*8,7*4+32*8}, 
		new int[] {0*32,1*32,2*32,3*32,4*32,5*32,6*32,7*32,
		 0*32+32*16,1*32+32*16,2*32+32*16,3*32+32*16,4*32+32*16,5*32+32*16,6*32+32*16,7*32+32*16},
		16*16*4
	);
        static GfxLayout layout_4bit_4M = new GfxLayout
	(
		16,16,
		(0x400000)*8/(16*16*4),
		4,
		new int[] {0, 1, 2, 3},
		new int[] {0*4,1*4,2*4,3*4,4*4,5*4,6*4,7*4, 
		 0*4+32*8,1*4+32*8,2*4+32*8,3*4+32*8,4*4+32*8,5*4+32*8,6*4+32*8,7*4+32*8}, 
		new int[] {0*32,1*32,2*32,3*32,4*32,5*32,6*32,7*32,
		 0*32+32*16,1*32+32*16,2*32+32*16,3*32+32*16,4*32+32*16,5*32+32*16,6*32+32*16,7*32+32*16},
		16*16*4
	);
        static GfxLayout layout_4bit_6M = new GfxLayout
	(
		16,16,
		(0x500000)*8/(16*16*4),
		4,
		new int[] {0, 1, 2, 3},
		new int[] {0*4,1*4,2*4,3*4,4*4,5*4,6*4,7*4, 
		 0*4+32*8,1*4+32*8,2*4+32*8,3*4+32*8,4*4+32*8,5*4+32*8,6*4+32*8,7*4+32*8}, 
		new int[] {0*32,1*32,2*32,3*32,4*32,5*32,6*32,7*32,
		 0*32+32*16,1*32+32*16,2*32+32*16,3*32+32*16,4*32+32*16,5*32+32*16,6*32+32*16,7*32+32*16},
		16*16*4
	);
        static GfxLayout layout_4bit_1_2M = new GfxLayout
	(
		16,16,
		(0x120000)*8/(16*16*4),
		4,
		new int[] {0, 1, 2, 3},
		new int[] {0*4,1*4,2*4,3*4,4*4,5*4,6*4,7*4, 
		 0*4+32*8,1*4+32*8,2*4+32*8,3*4+32*8,4*4+32*8,5*4+32*8,6*4+32*8,7*4+32*8}, 
		new int[] {0*32,1*32,2*32,3*32,4*32,5*32,6*32,7*32,
		 0*32+32*16,1*32+32*16,2*32+32*16,3*32+32*16,4*32+32*16,5*32+32*16,6*32+32*16,7*32+32*16},
		16*16*4
	);

	/*
		16x16x8 made of 4 8x8x8 blocks arrenged like:	01
														23
	*/
	static GfxLayout layout_8bit_8M = new GfxLayout
	(
		16,16,
		(0x800000)*8/(16*16*8),
		8,
		new int[] {0, 1, 2, 3, 4, 5, 6, 7},
		new int[] {0*8,1*8,2*8,3*8,4*8,5*8,6*8,7*8, 
		 0*8+64*8,1*8+64*8,2*8+64*8,3*8+64*8,4*8+64*8,5*8+64*8,6*8+64*8,7*8+64*8}, 
		new int[] {0*64,1*64,2*64,3*64,4*64,5*64,6*64,7*64,
		 0*64+64*16,1*64+64*16,2*64+64*16,3*64+64*16,4*64+64*16,5*64+64*16,6*64+64*16,7*64+64*16},
		16*16*8
	);
	
	

	
	
	/***************************************************************************
								[ The Berlin Wall ]
	***************************************************************************/

	
	static GfxDecodeInfo berlwall_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, layout_4bit_HM,		0x40 * 16,	0x40 ), // [0] Layers
		new GfxDecodeInfo( REGION_GFX2, 0, layout_4bit_1_2M,	0,			0x40 ), // [1] Sprites
		new GfxDecodeInfo( -1 )
	};
	
	/***************************************************************************
							[ Great 1000 Miles Rally ]
	***************************************************************************/
	
	static GfxDecodeInfo gtmr_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, layout_4bit_2M,	0,			0x40 ), // [0] Layers
		new GfxDecodeInfo( REGION_GFX2, 0, layout_8bit_8M,	0x40 * 256,	0x40 ), // [1] Sprites
		new GfxDecodeInfo( -1 )
	};
	
	/***************************************************************************
								[ Shogun Warriors ]
	***************************************************************************/
	
	static GfxDecodeInfo shogwarr_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, layout_4bit_4M,	0x40 * 16,	0x40 ), // [0] Layers
		new GfxDecodeInfo( REGION_GFX2, 0, layout_4bit_6M,	0,			0x40 ), // [1] Sprites
		new GfxDecodeInfo( -1 )
	};
	
	
	
	/***************************************************************************
	
									Machine Drivers
	
	***************************************************************************/
	
	
	
	/***************************************************************************
								[ The Berlin Wall ]
	***************************************************************************/
	
	static  OKIM6295interface berlwall_okim6295_interface = new OKIM6295interface
	(
		1,
		new int[]{ 12000 },		/* ? */
		new int[]{ REGION_SOUND1 },
		new int[]{ 33 }
	);
	
	static AY8910interface berlwall_ay8910_interface = new AY8910interface
	(
		2,
		1500000,	/* ? */
		new int[] { 33, 33 },
		new ReadHandlerPtr[] { null,null },	/* input A */
		new ReadHandlerPtr[] { null,null },	/* input B */
		new WriteHandlerPtr[] { null,null },
		new WriteHandlerPtr[] { null,null }
	);
	
	
	/*
		1-3] e8c:
		4]   e54:
		5]   de4:
		6-7] rte
	*/
	public static final int BERLWALL_INTERRUPTS_NUM	=3;
	public static InterruptHandlerPtr berlwall_interrupt = new InterruptHandlerPtr() { public int handler() 
	{
		switch ( cpu_getiloops() )
		{
			case 2:  return 3;
			case 1:  return 4;
			case 0:  return 5;
			default: return 0;
		}
	} };
	
	
	static MachineDriver machine_driver_berlwall = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				12000000,	/* like shogwarr? */
				berlwall_readmem,berlwall_writemem,null,null,
				berlwall_interrupt, BERLWALL_INTERRUPTS_NUM
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
		1,
		berlwall_init_machine,
	
		/* video hardware */
		256, 256, new rectangle( 0, 256-1, 0 + 16, 256 -1 - 16),
		berlwall_gfxdecodeinfo,
		0x1000 / 2 + 32768, 0x1000 / 2,	/* 32768 static colors for the bg */
		berlwall_init_palette,
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE  | VIDEO_UPDATE_AFTER_VBLANK,	// mangled sprites otherwise
		null,
		berlwall_vh_start,
		berlwall_vh_stop,
		kaneko16_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				berlwall_ay8910_interface
			),
			new MachineSound(
				SOUND_OKIM6295,
				berlwall_okim6295_interface
			)
		}
	);
	
	
	
	
	/***************************************************************************
							[ Great 1000 Miles Rally ]
	***************************************************************************/
	
	static  OKIM6295interface gtmr_okim6295_interface = new OKIM6295interface
	(
		2,
		new int[]{12000,12000},	/* ? everything seems synced, using 12KHz */
		new int[]{ REGION_SOUND1, REGION_SOUND2 },
		new int[]{ 50, 50 }
        );
	
	/*
		3] 476:			time, input ports, scroll registers
		4] 466.258e:	set sprite ram
		5] 438:			set sprite colors
	
		VIDEO_UPDATE_AFTER_VBLANK fixes the mangled/wrong colored sprites
	*/
	public static final int GTMR_INTERRUPTS_NUM	=3;
	public static InterruptHandlerPtr gtmr_interrupt = new InterruptHandlerPtr() { public int handler() 
	{
		switch ( cpu_getiloops() )
		{
			case 2:  return 3;
			case 1:  return 4;
			case 0:  return 5;
			default: return 0;
		}
	} };
	
	static MachineDriver machine_driver_gtmr = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				16000000,	/* ? Most likely a 68000-HC16 */
				gtmr_readmem,gtmr_writemem,null,null,
				gtmr_interrupt, GTMR_INTERRUPTS_NUM
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
		1,
		gtmr_init_machine,
	
		/* video hardware */
		320, 240, new rectangle( 0, 320-1, 0, 240-1 ),
		gtmr_gfxdecodeinfo,
		0x10000 / 2, 0x10000 / 2,
		null,
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_AFTER_VBLANK,
		null,
		kaneko16_vh_start,
		null,
		kaneko16_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_OKIM6295,
				gtmr_okim6295_interface
			)
		}
	);
	
	
	
	
	
	
	
	/***************************************************************************
								[ Shogun Warriors ]
	***************************************************************************/
	
	static  OKIM6295interface shogwarr_okim6295_interface = new OKIM6295interface
	(
		2,
		new int[]{12000,12000},		/* ? */
		new int[]{ REGION_SOUND1, REGION_SOUND2 },
		new int[]{ 50, 50 }
                );
	
	
	/*
		2] 100:	rte
		3] 102:
		4] 136:
			movem.l D0-D7/A0-A6, -(A7)
			movea.l $207808.l, A0	; from mcu?
			jmp     ($4,A0)
	
		other: busy loop
	*/
	public static final int SHOGWARR_INTERRUPTS_NUM	=3;
	public static InterruptHandlerPtr shogwarr_interrupt = new InterruptHandlerPtr() { public int handler() 
	{
		switch ( cpu_getiloops() )
		{
			case 2:  return 2;
			case 1:  return 3;
	//		case 0:  return 4;
			default: return 0;
		}
	} };
	
	static MachineDriver machine_driver_shogwarr = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				12000000,
				shogwarr_readmem,shogwarr_writemem,null,null,
				shogwarr_interrupt, SHOGWARR_INTERRUPTS_NUM
			)
		},
		60,DEFAULT_60HZ_VBLANK_DURATION,
		1,
		shogwarr_init_machine,
	
		/* video hardware */
		320, 240, new rectangle( 0, 320-1, 0, 240-1 ),
		shogwarr_gfxdecodeinfo,
		0x1000 / 2, 0x1000 / 2,
		null,
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		kaneko16_vh_start,
		null,
		kaneko16_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_OKIM6295,
				shogwarr_okim6295_interface
			)
		}
	);
	
	
	
	
	/***************************************************************************
	
									ROMs Loading
	
	***************************************************************************/
	
	
	/*
	 Sprites and tiles are stored in the ROMs using the same layout. But tiles
	 have the even and odd pixels swapped. So we use this function to untangle
	 them and have one single gfxlayout for both tiles and sprites.
	*/
	public static void kaneko16_unscramble_tiles(int region)
	{
		UBytePtr RAM	=	memory_region(region);
		int size			=	memory_region_length(region);
		int i;
	
		for (i = 0; i < size; i ++)
		{
			RAM.write(i,  ((RAM.read(i) & 0xF0)>>4) + ((RAM.read(i) & 0x0F)<<4));
		}
	}
	
	public static InitDriverHandlerPtr init_kaneko16 = new InitDriverHandlerPtr() { public void handler() 
	{
		kaneko16_unscramble_tiles(REGION_GFX1);
	} };
	
	
	/***************************************************************************
	
								[ The Berlin Wall ]
	
	The Berlin Wall, Kaneko 1991, BW-002
	
	----
	
	BW-004 BW-008                    VU-003
	BW-005 BW-009                    VU-003
	BW-006 BW-00A                    VU-003
	BW-007 BW-00B                          6116-90
	                                       6116-90
	BW-003                           52256  52256
	                                 BW101A BW100A
	5864
	5864                   MUX2      68000
	            VIEW2
	BW300
	BW-002
	BW-001                      42101
	                            42101
	41464 41464      VU-002
	41464 41464                      YM2149  IU-004
	41464 41464                      YM2149
	                           SWB             BW-000  6295
	                           SWA
	
	
	***************************************************************************/
	
	static RomLoadHandlerPtr rom_berlwall = new RomLoadHandlerPtr(){ public void handler(){ 
	
	 	ROM_REGION( 0x040000, REGION_CPU1 );		/* 68000 Code */
		ROM_LOAD_EVEN( "bw100a", 0x000000, 0x020000, 0xe6bcb4eb );
		ROM_LOAD_ODD(  "bw101a", 0x000000, 0x020000, 0x38056fb2 );
	
	 	ROM_REGION( 0x010000, REGION_CPU2 );		/* MCU Code */
		ROM_LOAD( "mcu_code",  0x000000, 0x010000, 0x00000000 );
	
		ROM_REGION( 0x080000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* Tiles (Scrambled) */
		ROM_LOAD( "bw003",  0x000000, 0x080000, 0xfbb4b72d );
	
		ROM_REGION( 0x120000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* Sprites */
		ROM_LOAD( "bw001",  0x000000, 0x080000, 0xbc927260 );
		ROM_LOAD( "bw002",  0x080000, 0x080000, 0x223f5465 );
		ROM_LOAD( "bw300",  0x100000, 0x020000, 0xb258737a );
	
		ROM_REGION( 0x400000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* High Color Background */
		ROM_LOAD_GFX_EVEN( "bw004",  0x000000, 0x080000, 0x5300c34d );
		ROM_LOAD_GFX_ODD(  "bw008",  0x000000, 0x080000, 0x9aaf2f2f );// FIXED BITS (xxxxxxx0)
		ROM_LOAD_GFX_EVEN( "bw005",  0x100000, 0x080000, 0x16db6d43 );
		ROM_LOAD_GFX_ODD(  "bw009",  0x100000, 0x080000, 0x1151a0b0 );// FIXED BITS (xxxxxxx0)
		ROM_LOAD_GFX_EVEN( "bw006",  0x200000, 0x080000, 0x73a35d1f );
		ROM_LOAD_GFX_ODD(  "bw00a",  0x200000, 0x080000, 0xf447dfc2 );// FIXED BITS (xxxxxxx0)
		ROM_LOAD_GFX_EVEN( "bw007",  0x300000, 0x080000, 0x97f85c87 );
		ROM_LOAD_GFX_ODD(  "bw00b",  0x300000, 0x080000, 0xb0a48225 );// FIXED BITS (xxxxxxx0)
	
		ROM_REGION( 0x040000, REGION_SOUND1 );/* Samples */
		ROM_LOAD( "bw000",  0x000000, 0x040000, 0xd8fe869d );
	
	ROM_END(); }}; 
	
	
	
	
	static RomLoadHandlerPtr rom_berlwalt = new RomLoadHandlerPtr(){ public void handler(){ 
	
	 	ROM_REGION( 0x040000, REGION_CPU1 );		/* 68000 Code */
		ROM_LOAD_EVEN( "u23_01.bin", 0x000000, 0x020000, 0x76b526ce );
		ROM_LOAD_ODD(  "u39_01.bin", 0x000000, 0x020000, 0x78fa7ef2 );
	
	 	ROM_REGION( 0x010000, REGION_CPU2 );		/* MCU Code */
		ROM_LOAD( "mcu_code",  0x000000, 0x010000, 0x00000000 );
	
		ROM_REGION( 0x080000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* Tiles (Scrambled) */
		ROM_LOAD( "bw003",  0x000000, 0x080000, 0xfbb4b72d );
	
		ROM_REGION( 0x120000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* Sprites */
		ROM_LOAD( "bw001",  0x000000, 0x080000, 0xbc927260 );
		ROM_LOAD( "bw002",  0x080000, 0x080000, 0x223f5465 );
		ROM_LOAD( "bw300",  0x100000, 0x020000, 0xb258737a );
	
		ROM_REGION( 0x400000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* High Color Background */
		ROM_LOAD_GFX_EVEN( "bw004",  0x000000, 0x080000, 0x5300c34d );
		ROM_LOAD_GFX_ODD(  "bw008",  0x000000, 0x080000, 0x9aaf2f2f );// FIXED BITS (xxxxxxx0)
		ROM_LOAD_GFX_EVEN( "bw005",  0x100000, 0x080000, 0x16db6d43 );
		ROM_LOAD_GFX_ODD(  "bw009",  0x100000, 0x080000, 0x1151a0b0 );// FIXED BITS (xxxxxxx0)
		ROM_LOAD_GFX_EVEN( "bw006",  0x200000, 0x080000, 0x73a35d1f );
		ROM_LOAD_GFX_ODD(  "bw00a",  0x200000, 0x080000, 0xf447dfc2 );// FIXED BITS (xxxxxxx0)
		ROM_LOAD_GFX_EVEN( "bw007",  0x300000, 0x080000, 0x97f85c87 );
		ROM_LOAD_GFX_ODD(  "bw00b",  0x300000, 0x080000, 0xb0a48225 );// FIXED BITS (xxxxxxx0)
	
		ROM_REGION( 0x040000, REGION_SOUND1 );/* Samples */
		ROM_LOAD( "bw000",  0x000000, 0x040000, 0xd8fe869d );
	
	ROM_END(); }}; 
	
	
	
	
	/***************************************************************************
	
							[ Great 1000 Miles Rally ]
	
	GMMU2+1	512K * 2	68k
	GMMU23	1M		OKI6295: 00000-2ffff + chunks of 0x10000 with headers
	GMMU24	1M		OKI6295: chunks of 0x40000 with headers - FIRST AND SECOND HALF IDENTICAL
	
	GMMU27	2M		sprites
	GMMU28	2M		sprites
	GMMU29	2M		sprites
	GMMU30	512k	sprites
	
	GMMU64	1M		sprites - FIRST AND SECOND HALF IDENTICAL
	GMMU65	1M		sprites - FIRST AND SECOND HALF IDENTICAL
	
	GMMU52	2M		tiles
	
	
	---------------------------------------------------------------------------
									Game code
	---------------------------------------------------------------------------
	
	100000.b	<- (!b00000.b) & 7f	[1p]
	    01.b	previous value of the above
	    02.b	bits gone high
	
	100008.b	<- (!b00002.b) & 7f	[2p]
	
	100010.b	<- !b00004.b [coins]
	    11.b	previous value of the above
	    12.b	bits gone high
	
	100013.b	<- b00006.b	(both never accessed again?)
	
	100015.b	<- wheel value
	
	600000.w	<- 100a20.w + 100a30.w		600002.w	<- 100a22.w + 100a32.w
	600004.w	<- 100a24.w + 100a34.w		600006.w	<- 100a26.w + 100a36.w
	
	680000.w	<- 100a28.w + 100a38.w		680002.w	<- 100a2a.w + 100a3a.w
	680004.w	<- 100a2c.w + 100a3c.w		680006.w	<- 100a2e.w + 100a3e.w
	
	101265.b	<- DSW (from 206000)
	101266		<- Settings from NVRAM (0x80 bytes from 208000)
	
	1034f8.b	credits
	103502.b	coins x ..
	103503.b	.. credits
	
	1035ec.l	*** Time (BCD: seconds * 10000) ***
	103e64.w	*** Speed << 4 ***
	
	10421a.b	bank for the oki mapped at 800000
	104216.b	last value of the above
	
	10421c.b	bank for the oki mapped at 880000
	104218.b	last value of the above
	
	ROUTINES:
	
	dd6	print string: a2.scr ; a1.string ; d1.l = xpos.w<<6|ypos.w<<6
	
	Trap #2 = 43a0 ; d0.w = routine index ; (where not specified: 43c0):
	1:  43C4	2:  43F8	3:  448E	4:  44EE
	5:  44D2	6:  4508	7:  453A	10: 0AF6
	18: 4580	19: 4604
	20> 2128	writes 700000-70001f
	21: 21F6
	24> 2346	clears 400000-401407 (641*8 = $281*8)
	30> 282A	writes 600008/9/b/e-f, 680008/9/b/e-f
	31: 295A
	32> 2B36	100a30-f <- 100a10-f
	34> 2B4C	clears 500000-503fff, 580000-583fff
	35> 2B9E	d1.w = selects between:	500000;501000;580000;581000.
				Fill 0x1000 bytes from there with d2.l
	
	70: 2BCE>	11d8a
	71: 2BD6
	74: 2BDE	90: 3D44
	91> 3D4C	wait for bit 0 of d00000 to be 0
	92> 3D5C	200010.w<-D1	200012.w<-D2	200014.w<-D3
	f1: 10F6
	
	
	***************************************************************************/
	
	/*	This version displays:
	
		tb05mm-eu "1000 miglia"
		master up= 94/07/18 15:12:35			*/
	
	static RomLoadHandlerPtr rom_gtmr = new RomLoadHandlerPtr(){ public void handler(){ 
	
	 	ROM_REGION( 0x100000, REGION_CPU1 );		/* 68000 Code */
		ROM_LOAD_EVEN( "u2.bin", 0x000000, 0x080000, 0x031799f7 );
		ROM_LOAD_ODD(  "u1.bin", 0x000000, 0x080000, 0x6238790a );
	
	 	ROM_REGION( 0x010000, REGION_CPU2 );		/* MCU Code */
		ROM_LOAD( "mcu_code",  0x000000, 0x010000, 0x00000000 );
	
		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* Tiles (scrambled) */
		ROM_LOAD( "gmmu52.bin",  0x000000, 0x200000, 0xb15f6b7f );
	
		ROM_REGION( 0x900000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* Sprites */
		ROM_LOAD( "gmmu27.bin",  0x000000, 0x200000, 0xc0ab3efc );
		ROM_LOAD( "gmmu28.bin",  0x200000, 0x200000, 0xcf6b23dc );
		ROM_LOAD( "gmmu29.bin",  0x400000, 0x200000, 0x8f27f5d3 );
		ROM_LOAD( "gmmu30.bin",  0x600000, 0x080000, 0xe9747c8c );
		/* codes 6800-7fff are explicitly skipped */
	//	ROM_LOAD_GFX_EVEN( "gmmu64.bin",  0x700000, 0x100000, 0x57d77b33 );// HALVES IDENTICAL
	//	ROM_LOAD_GFX_ODD(  "gmmu65.bin",  0x700000, 0x100000, 0x05b8bdca );// HALVES IDENTICAL
		/* wrong tiles: 	gtmr	77e0 ; gtmralt	81c4 81e0 81c4 */
		ROM_LOAD( "sprites",     0x700000, 0x100000, 0x00000000 );
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* Samples */
		ROM_LOAD( "gmmu23.bin",  0x000000, 0x100000, 0xb9cbfbee );// 16 x $10000
	
		ROM_REGION( 0x100000, REGION_SOUND2 );/* Samples */
		ROM_LOAD( "gmmu24.bin",  0x000000, 0x100000, 0x380cdc7c );//  2 x $40000 - HALVES IDENTICAL
	
	ROM_END(); }}; 
	
	
	/*	This version displays:
	
		tb05mm-eu "1000 miglia"
		master up= 94/09/06 14:49:19			*/
	
	static RomLoadHandlerPtr rom_gtmre = new RomLoadHandlerPtr(){ public void handler(){ 
	
	 	ROM_REGION( 0x100000, REGION_CPU1 );		/* 68000 Code */
		ROM_LOAD_EVEN( "gmmu2.bin", 0x000000, 0x080000, 0x36dc4aa9 );
		ROM_LOAD_ODD(  "gmmu1.bin", 0x000000, 0x080000, 0x8653c144 );
	
	 	ROM_REGION( 0x010000, REGION_CPU2 );		/* MCU Code */
		ROM_LOAD( "mcu_code",  0x000000, 0x010000, 0x00000000 );
	
		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* Tiles (scrambled) */
		ROM_LOAD( "gmmu52.bin",  0x000000, 0x200000, 0xb15f6b7f );
	
		ROM_REGION( 0x900000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* Sprites */
		ROM_LOAD( "gmmu27.bin",  0x000000, 0x200000, 0xc0ab3efc );
		ROM_LOAD( "gmmu28.bin",  0x200000, 0x200000, 0xcf6b23dc );
		ROM_LOAD( "gmmu29.bin",  0x400000, 0x200000, 0x8f27f5d3 );
		ROM_LOAD( "gmmu30.bin",  0x600000, 0x080000, 0xe9747c8c );
		/* codes 6800-6fff are explicitly skipped */
		ROM_LOAD_GFX_EVEN( "gmmu64.bin",  0x700000, 0x100000, 0x57d77b33 );// HALVES IDENTICAL
		ROM_LOAD_GFX_ODD(  "gmmu65.bin",  0x700000, 0x100000, 0x05b8bdca );// HALVES IDENTICAL
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* Samples */
		ROM_LOAD( "gmmu23.bin",  0x000000, 0x100000, 0xb9cbfbee );// 16 x $10000
	
		ROM_REGION( 0x100000, REGION_SOUND2 );/* Samples */
		ROM_LOAD( "gmmu24.bin",  0x000000, 0x100000, 0x380cdc7c );//  2 x $40000 - HALVES IDENTICAL
	
	ROM_END(); }}; 
	
	
	/***************************************************************************
	
							[ Great 1000 Miles Rally 2 ]
	
	GMR2U48	1M		OKI6295: 00000-3ffff + chunks of 0x10000 with headers
	
	GMR2U49	2M		sprites
	GMR2U50	2M		sprites
	GMR2U51	2M		sprites - FIRST AND SECOND HALF IDENTICAL
	
	GMR2U89	2M		tiles
	GMR1U90	2M		tiles
	GMR2U90	IDENTICAL TO GMR1U90
	
	
	***************************************************************************/
	
	static RomLoadHandlerPtr rom_gtmr2 = new RomLoadHandlerPtr(){ public void handler(){ 
	
	 	ROM_REGION( 0x100000, REGION_CPU1 );		/* 68000 Code */
		ROM_LOAD_EVEN( "maincode.1", 0x000000, 0x080000, 0x00000000 );
		ROM_LOAD_ODD(  "maincode.2", 0x000000, 0x080000, 0x00000000 );
	
		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* Tiles (scrambled) */
		ROM_LOAD( "gmr1u90.bin", 0x000000, 0x200000, 0xf4e894f2 );// These are IDENTICAL
		ROM_LOAD( "gmr2u90.bin", 0x000000, 0x200000, 0xf4e894f2 );
	
		ROM_REGION( 0x800000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* Sprites */
		ROM_LOAD( "gmr2u49.bin",  0x000000, 0x200000, 0xd50f9d80 );
		ROM_LOAD( "gmr2u50.bin",  0x200000, 0x200000, 0x39b60a83 );
		ROM_LOAD( "gmr2u51.bin",  0x400000, 0x200000, 0xfd06b339 );
		ROM_LOAD( "gmr2u89.bin",  0x600000, 0x200000, 0x4dc42fbb );
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* Samples */
		ROM_LOAD( "gmr2u48.bin", 0x000000, 0x100000, 0x1 );
	
		ROM_REGION( 0x100000, REGION_SOUND2 );/* Samples */
		ROM_LOAD( "samples",  0x000000, 0x100000, 0x00000000 );
	
	ROM_END(); }}; 
	
	
	
	
	/***************************************************************************
	
								[ Shogun Warriors ]
	
	Shogun Warriors, Kaneko 1992
	
	   fb010.u65           fb040.u33
	   fb011.u66
	   rb012.u67
	   rb013.u68
	
	                         fb001.u43
	     68000-12            fb000.u42  m6295
	    51257     fb030.u61  fb002.u44  m6295
	    51257     fb031.u62  fb003.u45
	
	
	                fb021a.u3
	                fb021b.u4
	                fb022a.u5
	   fb023.u7     fb022b.u6
	   fb020a.u1    fb020b.u2
	
	
	fb022b.u6               FIXED BITS (11111111)
	
	
	---------------------------------------------------------------------------
									Game code
	---------------------------------------------------------------------------
	
	102e04-7	<- !b80004-7
	102e18.w	. $800000
	102e1c.w	. $800002 , $800006
	102e1a.w	. $800004
	102e20.w	. $800008
	
	ROUTINES:
	
	6622	print ($600000)
	
	***************************************************************************/
	
	static RomLoadHandlerPtr rom_shogwarr = new RomLoadHandlerPtr(){ public void handler(){ 
	
	 	ROM_REGION( 0x040000, REGION_CPU1 );		/* 68000 Code */
		ROM_LOAD_EVEN( "fb030a.u61", 0x000000, 0x020000, 0xa04106c6 );
		ROM_LOAD_ODD(  "fb031a.u62", 0x000000, 0x020000, 0xd1def5e2 );
	
	 	ROM_REGION( 0x020000, REGION_CPU2 );		/* MCU Code */
		ROM_LOAD( "fb040a.u33",  0x000000, 0x020000, 0x4b62c4d9 );
	
		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* Tiles (scrambled) */
		ROM_LOAD( "fb010.u65",  0x000000, 0x100000, 0x296ffd92 );
		ROM_LOAD( "fb011.u66",  0x100000, 0x080000, 0x500a0367 );// ?!
		ROM_LOAD( "rb012.u67",  0x200000, 0x100000, 0xbfdbe0d1 );
		ROM_LOAD( "rb013.u68",  0x300000, 0x100000, 0x28c37fe8 );
	
		ROM_REGION( 0x600000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* Sprites */
		ROM_LOAD( "fb020a.u1",  0x000000, 0x080000, 0xda1b7373 );
		ROM_LOAD( "fb022a.u5",  0x080000, 0x080000, 0x60aa1282 );
		ROM_LOAD( "fb020b.u2",  0x100000, 0x100000, 0x1 );
		ROM_LOAD( "fb021a.u3",  0x200000, 0x100000, 0x1 );
		ROM_LOAD( "fb021b.u4",  0x300000, 0x100000, 0x1 );
		ROM_LOAD( "fb023.u7",   0x400000, 0x100000, 0x132794bd );
		ROM_LOAD( "fb022b.u6",  0x500000, 0x100000, 0x00000000 );
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* Samples */
		ROM_LOAD( "fb000e.u42",  0x000000, 0x080000, 0x969f1465 );// 2 x $40000
		ROM_LOAD( "fb001e.u43",  0x080000, 0x080000, 0xf524aaa1 );// 2 x $40000
	
		ROM_REGION( 0x100000, REGION_SOUND2 );/* Samples */
		ROM_LOAD( "fb002.u44",   0x000000, 0x080000, 0x05d7c2a9 );// 2 x $40000
		ROM_LOAD( "fb003.u45",   0x080000, 0x080000, 0x405722e9 );// 2 x $40000
	
	ROM_END(); }}; 
	
	
	public static InitDriverHandlerPtr init_shogwarr = new InitDriverHandlerPtr() { public void handler() 
	{
		/* Code patches */
	
		init_kaneko16.handler();
	
	/*
		ROM test at 2237e:
	
		the chksum of 00000-03fffd = $657f is added to ($200042).w
		[from shared ram]. The result must be $f463 [=($3fffe).w]
	
		Now, $f463-$657f = $8ee4 = byte sum of FB040A.U33 !!
	
		So, there's probably the MCU's code in there, though
		I can't id what kind of CPU should run it :-(
	*/
	} };
	
	
	
	/***************************************************************************
	
									Game drivers
	
	***************************************************************************/
	
	public static GameDriver driver_berlwall	   = new GameDriver("1991"	,"berlwall"	,"kaneko16.java"	,rom_berlwall,null	,machine_driver_berlwall	,input_ports_berlwall	,init_kaneko16	,ROT0_16BIT	,	"Kaneko", "The Berlin Wall (set 1)", GAME_NO_SOUND | GAME_WRONG_COLORS );
	public static GameDriver driver_berlwalt	   = new GameDriver("1991"	,"berlwalt"	,"kaneko16.java"	,rom_berlwalt,driver_berlwall	,machine_driver_berlwall	,input_ports_berlwalt	,init_kaneko16	,ROT0_16BIT	,	"Kaneko", "The Berlin Wall (set 2)", GAME_NO_SOUND | GAME_WRONG_COLORS );
	public static GameDriver driver_shogwarr	   = new GameDriver("1992"	,"shogwarr"	,"kaneko16.java"	,rom_shogwarr,null	,machine_driver_shogwarr	,input_ports_shogwarr	,init_shogwarr	,ROT0	,	"Kaneko", "Shogun Warriors", GAME_NOT_WORKING );
	public static GameDriver driver_gtmr  = new GameDriver("1994", "gtmr","kaneko16.java"	,rom_gtmr,     null,        machine_driver_gtmr,     input_ports_gtmr,     init_kaneko16, ROT0_16BIT, "Kaneko", "Great 1000 Miles Rally" );
	public static GameDriver driver_gtmre  = new GameDriver("1994", "gtmre","kaneko16.java"	,rom_gtmre,    driver_gtmr,     machine_driver_gtmr,     input_ports_gtmr,     init_kaneko16, ROT0_16BIT, "Kaneko", "Great 1000 Miles Rally (Evolution Model)" );
	public static GameDriver driver_gtmr2	   = new GameDriver("1995"	,"gtmr2"	,"kaneko16.java"	,rom_gtmr2,null	,machine_driver_gtmr	,input_ports_gtmr	,init_kaneko16	,ROT0	,	"Kaneko", "Great 1000 Miles Rally 2", GAME_NOT_WORKING );
}
