/*****************************************************************************

		ToaPlan game hardware from 1991-1994
		------------------------------------
		Driver by: Quench and Yochizo


Supported games:

	Name		Board No		Maker		Game name
	----------------------------------------------------------------------------
	tekipaki	TP-020			Toaplan		Teki Paki
	ghox		TP-021			Toaplan		Ghox
	dogyuun		TP-022			Toaplan		Dogyuun
	kbash		TP-023			Toaplan		Knuckle Bash
	tatsujn2	TP-024			Toaplan		Truxton 2 / Tatsujin 2
	pipibibs	TP-025			Toaplan		Pipi & Bibis
	whoopee		TP-025			Toaplan		Whoopee
	pipibibi	bootleg?		Toaplan		Pipi & Bibis
	fixeight	TP-026			Toaplan		FixEight
	grindstm	TP-027			Toaplan		Grind Stormer  (1992)
	vfive		TP-027			Toaplan		V-V  (V-Five)  (1993 - Japan only)
	batsugun	TP-030			Toaplan		Batsugun
	batugnsp	TP-030			Toaplan		Batsugun  (Special Version)
	snowbro2	??????			Toaplan		Snow Bros. 2 - With New Elves
	mahoudai	??????			Raizing		Mahou Daisakusen
	shippu		??????			Raizing		Shippu Mahou Daisakusen

Not supported games yet:

	Name		Board No		Maker		Game name
	----------------------------------------------------------------------------
	batrider	??????			Raizing		Armed Police Batrider
	btlgaleg	??????			Raizing		Battle Galegga

Game status:

Teki Paki     Working, but no sound. Missing sound MCU dump
Ghox          Working, but no sound. Missing sound MCU dump
Dogyuun       Working, but no sound. MCU type unknown - its a Z?80 of some sort.
Knuckle Bash  Working, but no sound. MCU dump exists, its a Z?80 of some sort.
Tatsujin 2    Working.
Pipi & Bibis  Working.
Whoopee       Working. Missing sound MCU dump. Using bootleg sound CPU dump for now
Pipi & Bibis  Ryouta Kikaku - Working.
FixEight      Not working.           MCU type unknown - its a Z?80 of some sort.
Grind Stormer Working, but no sound. MCU type unknown - its a Z?80 of some sort.
VFive         Working, but no sound. MCU type unknown - its a Z?80 of some sort.
Batsugun      Working, but no sound and wrong GFX priorities. MCU type unknown - its a Z?80 of some sort.
Batsugun Sp'  Working, but no sound and wrong GFX priorities. MCU type unknown - its a Z?80 of some sort.
Snow Bros. 2  Working.
Mahou Daisaks Working.
Shippu Mahou  Working.

Notes:
	See Input Port definition header below, for instructions
	  on how to enter pause/slow motion modes.

To Do / Unknowns:
	- Whoopee/Teki Paki sometimes tests bit 5 of the territory port
		just after testing for vblank. Why ?
	- Whoppee is currently using the sound CPU ROM (Z80) from a differnt
		(pirate ?) version of Pipi and Bibis (Ryouta Kikaku copyright).
		It really has a HD647180 CPU, and its internal ROM needs to be dumped.
	- Code at $20A26 forces territory to Japan in V-Five. Some stuff
		NOP'd at reset vector, and Z?80 CPU post test is skipped (bootleg ?)
    - Fixed top-character-layer.
    - Added unsupported games which work in this driver.

*****************************************************************************/


/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class toaplan2
{
	
	
	/**************** Machine stuff ******************/
	#define HD64x180 0		/* Define if CPU support is available */
	#define Zx80     0
	
	#define CPU_2_NONE		0x00
	#define CPU_2_Z80		0x5a
	#define CPU_2_HD647180	0xa5
	#define CPU_2_Zx80		0xff
	
	static UBytePtr toaplan2_shared_ram;
	static UBytePtr raizing_shared_ram;	/* Added by Yochizo */
	static UBytePtr Zx80_shared_ram;
	
	static int mcu_data = 0;
	int toaplan2_sub_cpu = 0;
	static INT8 old_p1_paddle_h;
	static INT8 old_p1_paddle_v;
	static INT8 old_p2_paddle_h;
	static INT8 old_p2_paddle_v;
	
	
	/********* Video wrappers for PIPIBIBI *********/
	READ_HANDLER ( pipibibi_videoram_r );
	READ_HANDLER ( pipibibi_spriteram_r );
	
	/**************** Video stuff ******************/
	READ_HANDLER ( toaplan2_0_videoram_r );
	READ_HANDLER ( toaplan2_1_videoram_r );
	
	
	
	/* Added by Yochizo 2000/08/19 */
	/* --------------------------- */
	
	
	/* Added by Yochizo 2000/08/19 */
	extern UBytePtr textvideoram;			 /* Video ram for extra-text layer */
	/* --------------------------- */
	
	static int video_status = 0;
	
	
	static public static InitDriverPtr init_toaplan2 = new InitDriverPtr() { public void handler() 
	{
		old_p1_paddle_h = 0;
		old_p1_paddle_v = 0;
		old_p2_paddle_h = 0;
		old_p2_paddle_v = 0;
		toaplan2_sub_cpu = CPU_2_HD647180;
		mcu_data = 0;
	} };
	
	static public static InitDriverPtr init_toaplan3 = new InitDriverPtr() { public void handler() 
	{
		toaplan2_sub_cpu = CPU_2_Zx80;
		mcu_data = 0;
	} };
	
	static public static InitDriverPtr init_pipibibs = new InitDriverPtr() { public void handler() 
	{
		toaplan2_sub_cpu = CPU_2_Z80;
	} };
	
	static public static InitDriverPtr init_pipibibi = new InitDriverPtr() { public void handler() 
	{
		int A;
		int oldword, newword;
	
		UBytePtr pipibibi_68k_rom = memory_region(REGION_CPU1);
	
		toaplan2_sub_cpu = CPU_2_Z80;
	
		/* unscramble the 68K ROM data. */
	
		for (A = 0; A < 0x040000; A+=8)
		{
			newword = 0;
			oldword = READ_WORD (&pipibibi_68k_rom[A]);
			newword |= ((oldword & 0x0001) << 9);
			newword |= ((oldword & 0x0002) << 14);
			newword |= ((oldword & 0x0004) << 8);
			newword |= ((oldword & 0x0018) << 1);
			newword |= ((oldword & 0x0020) << 9);
			newword |= ((oldword & 0x0040) << 7);
			newword |= ((oldword & 0x0080) << 5);
			newword |= ((oldword & 0x0100) << 3);
			newword |= ((oldword & 0x0200) >> 1);
			newword |= ((oldword & 0x0400) >> 8);
			newword |= ((oldword & 0x0800) >> 10);
			newword |= ((oldword & 0x1000) >> 12);
			newword |= ((oldword & 0x6000) >> 7);
			newword |= ((oldword & 0x8000) >> 12);
			WRITE_WORD (&pipibibi_68k_rom[A],newword);
	
			newword = 0;
			oldword = READ_WORD (&pipibibi_68k_rom[A+2]);
			newword |= ((oldword & 0x0001) << 8);
			newword |= ((oldword & 0x0002) << 12);
			newword |= ((oldword & 0x0004) << 5);
			newword |= ((oldword & 0x0008) << 11);
			newword |= ((oldword & 0x0010) << 2);
			newword |= ((oldword & 0x0020) << 10);
			newword |= ((oldword & 0x0040) >> 1);
			newword |= ((oldword & 0x0080) >> 7);
			newword |= ((oldword & 0x0100) >> 4);
			newword |= ((oldword & 0x0200) << 0);
			newword |= ((oldword & 0x0400) >> 7);
			newword |= ((oldword & 0x0800) >> 1);
			newword |= ((oldword & 0x1000) >> 10);
			newword |= ((oldword & 0x2000) >> 2);
			newword |= ((oldword & 0x4000) >> 13);
			newword |= ((oldword & 0x8000) >> 3);
			WRITE_WORD (&pipibibi_68k_rom[A+2],newword);
	
			newword = 0;
			oldword = READ_WORD (&pipibibi_68k_rom[A+4]);
			newword |= ((oldword & 0x000f) << 4);
			newword |= ((oldword & 0x00f0) >> 4);
			newword |= ((oldword & 0x0100) << 3);
			newword |= ((oldword & 0x0200) << 1);
			newword |= ((oldword & 0x0400) >> 1);
			newword |= ((oldword & 0x0800) >> 3);
			newword |= ((oldword & 0x1000) << 3);
			newword |= ((oldword & 0x2000) << 1);
			newword |= ((oldword & 0x4000) >> 1);
			newword |= ((oldword & 0x8000) >> 3);
			WRITE_WORD (&pipibibi_68k_rom[A+4],newword);
	
			newword = 0;
			oldword = READ_WORD (&pipibibi_68k_rom[A+6]);
			newword |= ((oldword & 0x000f) << 4);
			newword |= ((oldword & 0x00f0) >> 4);
			newword |= ((oldword & 0x0100) << 7);
			newword |= ((oldword & 0x0200) << 5);
			newword |= ((oldword & 0x0400) << 3);
			newword |= ((oldword & 0x0800) << 1);
			newword |= ((oldword & 0x1000) >> 1);
			newword |= ((oldword & 0x2000) >> 3);
			newword |= ((oldword & 0x4000) >> 5);
			newword |= ((oldword & 0x8000) >> 7);
			WRITE_WORD (&pipibibi_68k_rom[A+6],newword);
		}
	} };
	
	/* Added by Yochizo 2000/08/16 */
	static public static InitDriverPtr init_tatsujn2 = new InitDriverPtr() { public void handler() 
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		/* Fix checksum from the source of Raine. */
		WRITE_WORD(&RAM[0x2EC10],0x4E71);		// NOP
	
		/* Fix 60000 from the source of Raine. */
		WRITE_WORD(&RAM[0x1F6E8],0x4E75);		// RTS
	
		WRITE_WORD(&RAM[0x009FE],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x01276],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x012BA],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x03150],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x031B4],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x0F20C],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1EBFA],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1EC7A],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1ECE8],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1ED00],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1ED5E],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1ED9A],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1EDB2],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1EEA2],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1EEBA],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1EEE6],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1EF72],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1EFD0],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1F028],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1F05E],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1F670],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1F6C0],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1FA08],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1FAA4],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1FADA],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1FBA2],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1FBDC],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1FC1C],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1FC8A],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1FD24],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x1FD7A],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x2775A],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x277D8],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x2788C],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x278BA],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x2EC54],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x2EC90],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x2ECAC],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x2ECCA],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x2ECE6],0x4E71);		// NOP
		WRITE_WORD(&RAM[0x2ED1E],0x4E71);		// NOP
	
		WRITE_WORD(&RAM[0x2EC26],0x4E71);		// NOP
	
	} };
	
	static public static InitDriverPtr init_snowbro2 = new InitDriverPtr() { public void handler() 
	{
		toaplan2_sub_cpu = CPU_2_NONE;
	} };
	
	/* Added by Yochizo 2000/08/16 */
	public static InterruptPtr tatsujn2_interrupt = new InterruptPtr() { public int handler() 
	{
		return MC68000_IRQ_2;	/* Tatsujin2 uses IRQ level 2 */
	} };
	
	public static InterruptPtr toaplan2_interrupt = new InterruptPtr() { public int handler() 
	{
		return MC68000_IRQ_4;
	} };
	
	public static WriteHandlerPtr toaplan2_coin_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		data &= 0xffff;
		switch (data & 0x0f)
		{
			case 0x00:	coin_lockout_global_w(0,1); break;	/* Lock all coin slots */
			case 0x0c:	coin_lockout_global_w(0,0); break;	/* Unlock all coin slots */
			case 0x0d:	coin_counter_w.handler(0,1); coin_counter_w.handler(0,0);	/* Count slot A */
						logerror("Count coin slot A\n"); break;
			case 0x0e:	coin_counter_w.handler(1,1); coin_counter_w.handler(1,0);	/* Count slot B */
						logerror("Count coin slot B\n"); break;
		/* The following are coin counts after coin-lock active (faulty coin-lock ?) */
			case 0x01:	coin_counter_w.handler(0,1); coin_counter_w.handler(0,0); coin_lockout_w.handler(0,1); break;
			case 0x02:	coin_counter_w.handler(1,1); coin_counter_w.handler(1,0); coin_lockout_global_w(0,1); break;
	
			default:	logerror("Writing unknown command (%04x) to coin control\n",data);
						break;
		}
		if (data > 0xf)
		{
			logerror("Writing unknown upper bits command (%04x) to coin control\n",data);
		}
	} };
	
	public static WriteHandlerPtr oki_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		OKIM6295_set_bank_base(0, ALL_VOICES, (data & 1) * 0x40000);
	} };
	
	public static ReadHandlerPtr toaplan2_shared_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return toaplan2_shared_ram[offset>>1];
	} };
	
	public static WriteHandlerPtr toaplan2_shared_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		toaplan2_shared_ram[offset>>1] = data;
	} };
	
	public static WriteHandlerPtr toaplan2_hd647180_cpu_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* Command sent to secondary CPU. Support for HD647180 will be
		   required when a ROM dump becomes available for this hardware */
	
		if (toaplan2_sub_cpu == CPU_2_Z80)		/* Whoopee */
		{
			toaplan2_shared_ram[0] = data;
		}
		else									/* Teki Paki */
		{
			mcu_data = data;
			logerror("PC:%08x Writing command (%04x) to secondary CPU shared port\n",cpu_getpreviouspc(),mcu_data);
		}
	} };
	
	public static ReadHandlerPtr c2map_port_6_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* bit 4 high signifies secondary CPU is ready */
		/* bit 5 is tested low before V-Blank bit ??? */
		switch (toaplan2_sub_cpu)
		{
			case CPU_2_Z80:			mcu_data = toaplan2_shared_ram[0]; break;
			case CPU_2_HD647180:	mcu_data = 0xff; break;
			default:				mcu_data = 0x00; break;
		}
		if (mcu_data == 0xff) mcu_data = 0x10;
		else mcu_data = 0x00;
		return ( mcu_data | input_port_6_r.handler(0) );
	} };
	
	public static ReadHandlerPtr pipibibi_z80_status_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return toaplan2_shared_ram[0];
	} };
	
	public static WriteHandlerPtr pipibibi_z80_task_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		toaplan2_shared_ram[0] = data;
	} };
	
	public static ReadHandlerPtr video_count_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* Maybe this should return which scan line number is being drawn */
		/* Raizing games wait until its between F0h (240) and FFh (255) ??? */
		/* Video busy while bit 8 is low. Maybe once it clocks */
		/* over to 100h (256) or higher, it means frame done ??? */
	
		video_status += 0x50;	/* use a value to help prevent tight loop hangs */
		video_status &= 0x1f0;
		if ((video_status & 0xf0) == 0x40) video_status += 0x10;
		return video_status;
	} };
	
	public static ReadHandlerPtr ghox_p1_h_analog_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		INT8 value, new_value;
		new_value = input_port_7_r.handler(0);
		if (new_value == old_p1_paddle_h) return 0;
		value = new_value - old_p1_paddle_h;
		old_p1_paddle_h = new_value;
		return value;
	} };
	public static ReadHandlerPtr ghox_p1_v_analog_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		INT8 new_value;
		new_value = input_port_9_r.handler(0);		/* fake vertical movement */
		if (new_value == old_p1_paddle_v) return input_port_1_r.handler(0);
		if (new_value >  old_p1_paddle_v)
		{
			old_p1_paddle_v = new_value;
			return (input_port_1_r.handler(0) | 2);
		}
		old_p1_paddle_v = new_value;
		return (input_port_1_r.handler(0) | 1);
	} };
	public static ReadHandlerPtr ghox_p2_h_analog_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		INT8 value, new_value;
		new_value = input_port_8_r.handler(0);
		if (new_value == old_p2_paddle_h) return 0;
		value = new_value - old_p2_paddle_h;
		old_p2_paddle_h = new_value;
		return value;
	} };
	public static ReadHandlerPtr ghox_p2_v_analog_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		INT8 new_value;
		new_value = input_port_10_r.handler(0);		/* fake vertical movement */
		if (new_value == old_p2_paddle_v) return input_port_2_r.handler(0);
		if (new_value >  old_p2_paddle_v)
		{
			old_p2_paddle_v = new_value;
			return (input_port_2_r.handler(0) | 2);
		}
		old_p2_paddle_v = new_value;
		return (input_port_2_r.handler(0) | 1);
	} };
	
	public static ReadHandlerPtr ghox_mcu_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return 0xff;
	} };
	public static WriteHandlerPtr ghox_mcu_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		data &= 0xffff;
		mcu_data = data;
		if ((data >= 0xd0) && (data < 0xe0))
		{
			offset = ((data & 0x0f) * 4) + 0x38;
			WRITE_WORD (&toaplan2_shared_ram[offset  ],0x05);	/* Return address for */
			WRITE_WORD (&toaplan2_shared_ram[offset-2],0x56);	/*   RTS instruction */
		}
		else
		{
			logerror("PC:%08x Writing %08x to HD647180 cpu shared ram status port\n",cpu_getpreviouspc(),mcu_data);
		}
		WRITE_WORD (&toaplan2_shared_ram[0x56],0x4e);	/* Return a RTS instruction */
		WRITE_WORD (&toaplan2_shared_ram[0x58],0x75);
	
		if (data == 0xd3)
		{
		WRITE_WORD (&toaplan2_shared_ram[0x56],0x3a);	//	move.w  d1,d5
		WRITE_WORD (&toaplan2_shared_ram[0x58],0x01);
		WRITE_WORD (&toaplan2_shared_ram[0x5a],0x08);	//	bclr.b  #0,d5
		WRITE_WORD (&toaplan2_shared_ram[0x5c],0x85);
		WRITE_WORD (&toaplan2_shared_ram[0x5e],0x00);
		WRITE_WORD (&toaplan2_shared_ram[0x60],0x00);
		WRITE_WORD (&toaplan2_shared_ram[0x62],0xcb);	//	muls.w  #3,d5
		WRITE_WORD (&toaplan2_shared_ram[0x64],0xfc);
		WRITE_WORD (&toaplan2_shared_ram[0x66],0x00);
		WRITE_WORD (&toaplan2_shared_ram[0x68],0x03);
		WRITE_WORD (&toaplan2_shared_ram[0x6a],0x90);	//	sub.w   d5,d0
		WRITE_WORD (&toaplan2_shared_ram[0x6c],0x45);
		WRITE_WORD (&toaplan2_shared_ram[0x6e],0xe5);   //  lsl.b   #2,d1
		WRITE_WORD (&toaplan2_shared_ram[0x70],0x09);
		WRITE_WORD (&toaplan2_shared_ram[0x72],0x4e);	//	rts
		WRITE_WORD (&toaplan2_shared_ram[0x74],0x75);
		}
	
	} };
	
	public static ReadHandlerPtr ghox_shared_ram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* Ghox 68K reads data from MCU shared RAM and writes it to main RAM.
		   It then subroutine jumps to main RAM and executes this code.
		   Here, i'm just returning a RTS instruction for now.
		   See above ghox_mcu_w routine.
	
		   Offset $56 and $58 is accessed around PC:F814
	
		   Offset $38 and $36 is accessed from around PC:DA7C
		   Offset $3c and $3a is accessed from around PC:2E3C
		   Offset $40 and $3E is accessed from around PC:103EE
		   Offset $44 and $42 is accessed from around PC:FB52
		   Offset $48 and $46 is accessed from around PC:6776
		*/
	
		int data = READ_WORD (&toaplan2_shared_ram[offset]);
	
		return data;
	} };
	public static WriteHandlerPtr ghox_shared_ram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WRITE_WORD (&toaplan2_shared_ram[offset],data);
	} };
	
	
	public static ReadHandlerPtr kbash_sub_cpu_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	/*	Knuckle Bash's  68000 reads secondary CPU status via an I/O port.
		If a value of 2 is read, then secondary CPU is busy.
		Secondary CPU must report 0xff when no longer busy, to signify that it
		has passed POST.
	*/
		mcu_data=0xff;
		return mcu_data;
	} };
	
	public static WriteHandlerPtr kbash_sub_cpu_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		logerror("PC:%08x writing %04x to Zx80 secondary CPU status port %02x\n",cpu_getpreviouspc(),mcu_data,offset/2);
	} };
	
	public static ReadHandlerPtr shared_ram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	/*	Other games using a Zx80 based secondary CPU, have shared memory between
		the 68000 and the Zx80 CPU. The 68000 reads the status of the Zx80
		via a location of the shared memory.
	*/
		int data = READ_WORD (&toaplan2_shared_ram[offset]);
		return data;
	} };
	
	public static WriteHandlerPtr shared_ram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (offset == 0x9e8)
		{
			WRITE_WORD (&toaplan2_shared_ram[offset + 2],data);
		}
		if (offset == 0xff8)
		{
			WRITE_WORD (&toaplan2_shared_ram[offset + 2],data);
			logerror("PC:%08x Writing  (%04x) to secondary CPU\n",cpu_getpreviouspc(),data);
			if ((data & 0xffff) == 0x81) data = 0x01;
		}
		WRITE_WORD (&toaplan2_shared_ram[offset],data);
	} };
	public static ReadHandlerPtr Zx80_status_port_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	/*** Status port includes Zx80 CPU POST codes. ************
	 *** This is actually a part of the 68000/Zx80 Shared RAM */
	
		int data;
	
		if (mcu_data == 0x800000aa) mcu_data = 0xff;		/* dogyuun */
		if (mcu_data == 0x00) mcu_data = 0x800000aa;		/* dogyuun */
	
		if (mcu_data == 0x8000ffaa) mcu_data = 0xffff;		/* fixeight */
		if (mcu_data == 0xffaa) mcu_data = 0x8000ffaa;		/* fixeight */
		if (mcu_data == 0xff00) mcu_data = 0xffaa;			/* fixeight */
	
		logerror("PC:%08x reading %08x from Zx80 secondary CPU command/status port\n",cpu_getpreviouspc(),mcu_data);
		data = mcu_data & 0x0000ffff;
		return data;
	} };
	public static WriteHandlerPtr Zx80_command_port_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		mcu_data = data;
		logerror("PC:%08x Writing command (%04x) to Zx80 secondary CPU command/status port\n",cpu_getpreviouspc(),mcu_data);
	} };
	
	public static ReadHandlerPtr Zx80_sharedram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return Zx80_shared_ram[offset / 2];
	} };
	
	public static WriteHandlerPtr Zx80_sharedram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		Zx80_shared_ram[offset / 2] = data;
	} };
	public static ReadHandlerPtr raizing_shared_ram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		offset >>= 1;
		offset &= 0x3fff;
	
		return raizing_shared_ram[offset];
	
	} };
	public static WriteHandlerPtr raizing_shared_ram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		offset >>= 1;
		offset &= 0x3fff;
	
		raizing_shared_ram[offset] = data & 0xff;
	} };
	
	
	static MemoryReadAddress tekipaki_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x01ffff, MRA_ROM ),
		new MemoryReadAddress( 0x020000, 0x03ffff, MRA_ROM ),				/* extra for Whoopee */
		new MemoryReadAddress( 0x080000, 0x082fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x0c0000, 0x0c0fff, paletteram_word_r ),
		new MemoryReadAddress( 0x140004, 0x140007, toaplan2_0_videoram_r ),
		new MemoryReadAddress( 0x14000c, 0x14000d, input_port_0_r ),			/* VBlank */
		new MemoryReadAddress( 0x180000, 0x180001, input_port_4_r ),			/* Dip Switch A */
		new MemoryReadAddress( 0x180010, 0x180011, input_port_5_r ),			/* Dip Switch B */
		new MemoryReadAddress( 0x180020, 0x180021, input_port_3_r ),			/* Coin/System inputs */
		new MemoryReadAddress( 0x180030, 0x180031, c2map_port_6_r ),			/* CPU 2 busy and Territory Jumper block */
		new MemoryReadAddress( 0x180050, 0x180051, input_port_1_r ),			/* Player 1 controls */
		new MemoryReadAddress( 0x180060, 0x180061, input_port_2_r ),			/* Player 2 controls */
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress tekipaki_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x01ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x020000, 0x03ffff, MWA_ROM ),				/* extra for Whoopee */
		new MemoryWriteAddress( 0x080000, 0x082fff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x0c0000, 0x0c0fff, paletteram_xBBBBBGGGGGRRRRR_word_w, paletteram ),
		new MemoryWriteAddress( 0x140000, 0x140001, toaplan2_0_voffs_w ),
		new MemoryWriteAddress( 0x140004, 0x140007, toaplan2_0_videoram_w ),	/* Tile/Sprite VideoRAM */
		new MemoryWriteAddress( 0x140008, 0x140009, toaplan2_0_scroll_reg_select_w ),
		new MemoryWriteAddress( 0x14000c, 0x14000d, toaplan2_0_scroll_reg_data_w ),
		new MemoryWriteAddress( 0x180040, 0x180041, toaplan2_coin_w ),		/* Coin count/lock */
		new MemoryWriteAddress( 0x180070, 0x180071, toaplan2_hd647180_cpu_w ),
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress ghox_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
		new MemoryReadAddress( 0x040000, 0x040001, ghox_p2_h_analog_r ),		/* Paddle 2 */
		new MemoryReadAddress( 0x080000, 0x083fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x0c0000, 0x0c0fff, paletteram_word_r ),
		new MemoryReadAddress( 0x100000, 0x100001, ghox_p1_h_analog_r ),		/* Paddle 1 */
		new MemoryReadAddress( 0x140004, 0x140007, toaplan2_0_videoram_r ),
		new MemoryReadAddress( 0x14000c, 0x14000d, input_port_0_r ),			/* VBlank */
		new MemoryReadAddress( 0x180000, 0x180001, ghox_mcu_r ),				/* really part of shared RAM */
		new MemoryReadAddress( 0x180006, 0x180007, input_port_4_r ),			/* Dip Switch A */
		new MemoryReadAddress( 0x180008, 0x180009, input_port_5_r ),			/* Dip Switch B */
		new MemoryReadAddress( 0x180010, 0x180011, input_port_3_r ),			/* Coin/System inputs */
	//	new MemoryReadAddress( 0x18000c, 0x18000d, input_port_1_r ),			/* Player 1 controls (real) */
	//	new MemoryReadAddress( 0x18000e, 0x18000f, input_port_2_r ),			/* Player 2 controls (real) */
		new MemoryReadAddress( 0x18000c, 0x18000d, ghox_p1_v_analog_r ),		/* Player 1 controls */
		new MemoryReadAddress( 0x18000e, 0x18000f, ghox_p2_v_analog_r ),		/* Player 2 controls */
		new MemoryReadAddress( 0x180500, 0x180fff, ghox_shared_ram_r ),
		new MemoryReadAddress( 0x18100c, 0x18100d, input_port_6_r ),			/* Territory Jumper block */
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress ghox_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x080000, 0x083fff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x0c0000, 0x0c0fff, paletteram_xBBBBBGGGGGRRRRR_word_w, paletteram ),
		new MemoryWriteAddress( 0x140000, 0x140001, toaplan2_0_voffs_w ),
		new MemoryWriteAddress( 0x140004, 0x140007, toaplan2_0_videoram_w ),	/* Tile/Sprite VideoRAM */
		new MemoryWriteAddress( 0x140008, 0x140009, toaplan2_0_scroll_reg_select_w ),
		new MemoryWriteAddress( 0x14000c, 0x14000d, toaplan2_0_scroll_reg_data_w ),
		new MemoryWriteAddress( 0x180000, 0x180001, ghox_mcu_w ),				/* really part of shared RAM */
		new MemoryWriteAddress( 0x180500, 0x180fff, ghox_shared_ram_w, toaplan2_shared_ram ),
		new MemoryWriteAddress( 0x181000, 0x181001, toaplan2_coin_w ),
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress dogyuun_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x103fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x200010, 0x200011, input_port_1_r ),			/* Player 1 controls */
		new MemoryReadAddress( 0x200014, 0x200015, input_port_2_r ),			/* Player 2 controls */
		new MemoryReadAddress( 0x200018, 0x200019, input_port_3_r ),			/* Coin/System inputs */
	#if Zx80
		new MemoryReadAddress( 0x21e000, 0x21fbff, shared_ram_r ),			/* $21f000 status port */
		new MemoryReadAddress( 0x21fc00, 0x21ffff, Zx80_sharedram_r ),		/* 16-bit on 68000 side, 8-bit on Zx80 side */
	#else
		new MemoryReadAddress( 0x21e000, 0x21efff, shared_ram_r ),
		new MemoryReadAddress( 0x21f000, 0x21f001, Zx80_status_port_r ),		/* Zx80 status port */
		new MemoryReadAddress( 0x21f004, 0x21f005, input_port_4_r ),			/* Dip Switch A */
		new MemoryReadAddress( 0x21f006, 0x21f007, input_port_5_r ),			/* Dip Switch B */
		new MemoryReadAddress( 0x21f008, 0x21f009, input_port_6_r ),			/* Territory Jumper block */
		new MemoryReadAddress( 0x21fc00, 0x21ffff, Zx80_sharedram_r ),		/* 16-bit on 68000 side, 8-bit on Zx80 side */
	#endif
		/***** The following in 0x30000x are for video controller 1 ******/
		new MemoryReadAddress( 0x300004, 0x300007, toaplan2_0_videoram_r ),	/* tile layers */
		new MemoryReadAddress( 0x30000c, 0x30000d, input_port_0_r ),			/* VBlank */
		new MemoryReadAddress( 0x400000, 0x400fff, paletteram_word_r ),
		/***** The following in 0x50000x are for video controller 2 ******/
		new MemoryReadAddress( 0x500004, 0x500007, toaplan2_1_videoram_r ),	/* tile layers 2 */
		new MemoryReadAddress( 0x700000, 0x700001, video_count_r ),			/* test bit 8 */
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress dogyuun_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x103fff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x200008, 0x200009, OKIM6295_data_0_w ),
		new MemoryWriteAddress( 0x20001c, 0x20001d, toaplan2_coin_w ),
	#if Zx80
		new MemoryWriteAddress( 0x21e000, 0x21fbff, shared_ram_w, toaplan2_shared_ram ),	/* $21F000 */
		new MemoryWriteAddress( 0x21fc00, 0x21ffff, Zx80_sharedram_w, Zx80_shared_ram ),	/* 16-bit on 68000 side, 8-bit on Zx80 side */
	#else
		new MemoryWriteAddress( 0x21e000, 0x21efff, shared_ram_w, toaplan2_shared_ram ),
		new MemoryWriteAddress( 0x21f000, 0x21f001, Zx80_command_port_w ),	/* Zx80 command port */
		new MemoryWriteAddress( 0x21fc00, 0x21ffff, Zx80_sharedram_w, Zx80_shared_ram ),	/* 16-bit on 68000 side, 8-bit on Zx80 side */
	#endif
		/***** The following in 0x30000x are for video controller 1 ******/
		new MemoryWriteAddress( 0x300000, 0x300001, toaplan2_0_voffs_w ),		/* VideoRAM selector/offset */
		new MemoryWriteAddress( 0x300004, 0x300007, toaplan2_0_videoram_w ),	/* Tile/Sprite VideoRAM */
		new MemoryWriteAddress( 0x300008, 0x300009, toaplan2_0_scroll_reg_select_w ),
		new MemoryWriteAddress( 0x30000c, 0x30000d, toaplan2_0_scroll_reg_data_w ),
		new MemoryWriteAddress( 0x400000, 0x400fff, paletteram_xBBBBBGGGGGRRRRR_word_w, paletteram ),
		/***** The following in 0x50000x are for video controller 2 ******/
		new MemoryWriteAddress( 0x500000, 0x500001, toaplan2_1_voffs_w ),		/* VideoRAM selector/offset */
		new MemoryWriteAddress( 0x500004, 0x500007, toaplan2_1_videoram_w ),	/* Tile/Sprite VideoRAM */
		new MemoryWriteAddress( 0x500008, 0x500009, toaplan2_1_scroll_reg_select_w ),
		new MemoryWriteAddress( 0x50000c, 0x50000d, toaplan2_1_scroll_reg_data_w ),
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress kbash_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x103fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x200000, 0x200001, kbash_sub_cpu_r ),
		new MemoryReadAddress( 0x200004, 0x200005, input_port_4_r ),			/* Dip Switch A */
		new MemoryReadAddress( 0x200006, 0x200007, input_port_5_r ),			/* Dip Switch B */
		new MemoryReadAddress( 0x200008, 0x200009, input_port_6_r ),			/* Territory Jumper block */
		new MemoryReadAddress( 0x208010, 0x208011, input_port_1_r ),			/* Player 1 controls */
		new MemoryReadAddress( 0x208014, 0x208015, input_port_2_r ),			/* Player 2 controls */
		new MemoryReadAddress( 0x208018, 0x208019, input_port_3_r ),			/* Coin/System inputs */
		new MemoryReadAddress( 0x300004, 0x300007, toaplan2_0_videoram_r ),	/* tile layers */
		new MemoryReadAddress( 0x30000c, 0x30000d, input_port_0_r ),			/* VBlank */
		new MemoryReadAddress( 0x400000, 0x400fff, paletteram_word_r ),
		new MemoryReadAddress( 0x700000, 0x700001, video_count_r ),			/* test bit 8 */
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress kbash_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x103fff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x200000, 0x200003, kbash_sub_cpu_w ),		/* sound number to play */
	//	new MemoryWriteAddress( 0x200002, 0x200003, kbash_sub_cpu_w2 ),		/* ??? */
		new MemoryWriteAddress( 0x20801c, 0x20801d, toaplan2_coin_w ),
		new MemoryWriteAddress( 0x300000, 0x300001, toaplan2_0_voffs_w ),
		new MemoryWriteAddress( 0x300004, 0x300007, toaplan2_0_videoram_w ),
		new MemoryWriteAddress( 0x300008, 0x300009, toaplan2_0_scroll_reg_select_w ),
		new MemoryWriteAddress( 0x30000c, 0x30000d, toaplan2_0_scroll_reg_data_w ),
		new MemoryWriteAddress( 0x400000, 0x400fff, paletteram_xBBBBBGGGGGRRRRR_word_w, paletteram ),
		new MemoryWriteAddress( -1 )
	};
	
	/* Fixed by Yochizo 2000/08/16 */
	static MemoryReadAddress tatsujn2_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x200004, 0x200007, toaplan2_0_videoram_r ),
		new MemoryReadAddress( 0x20000c, 0x20000d, input_port_0_r ),			/* VBlank */
		new MemoryReadAddress( 0x300000, 0x300fff, paletteram_word_r ),
	//	new MemoryReadAddress( 0x400000, 0x403fff, MRA_BANK2 ),
		new MemoryReadAddress( 0x400000, 0x403fff, raizing_textram_r ),
		new MemoryReadAddress( 0x500000, 0x50ffff, MRA_BANK3 ),
		new MemoryReadAddress( 0x600000, 0x600001, video_count_r ),
		new MemoryReadAddress( 0x700000, 0x700001, input_port_4_r ),			/* Dip Switch A */
		new MemoryReadAddress( 0x700002, 0x700003, input_port_5_r ),			/* Dip Switch B */
		new MemoryReadAddress( 0x700004, 0x700005, input_port_6_r ),			/* Territory Jumper block */
		new MemoryReadAddress( 0x700006, 0x700007, input_port_1_r ),			/* Player 1 controls */
		new MemoryReadAddress( 0x700008, 0x700009, input_port_2_r ),			/* Player 2 controls */
		new MemoryReadAddress( 0x70000a, 0x70000b, input_port_3_r ),			/* Coin/System inputs */
		new MemoryReadAddress( 0x700016, 0x700017, YM2151_status_port_0_r ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress tatsujn2_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x200000, 0x200001, toaplan2_0_voffs_w ),		/* VideoRAM selector/offset */
		new MemoryWriteAddress( 0x200004, 0x200007, toaplan2_0_videoram_w ),	/* Tile/Sprite VideoRAM */
		new MemoryWriteAddress( 0x200008, 0x200009, toaplan2_0_scroll_reg_select_w ),
		new MemoryWriteAddress( 0x20000c, 0x20000d, toaplan2_0_scroll_reg_data_w ),
		new MemoryWriteAddress( 0x300000, 0x300fff, paletteram_xBBBBBGGGGGRRRRR_word_w, paletteram ),
	//	new MemoryWriteAddress( 0x400000, 0x403fff, MWA_BANK2 ),				/* TEXT RAM */
		new MemoryWriteAddress( 0x400000, 0x403fff, raizing_textram_w, textvideoram ),
		new MemoryWriteAddress( 0x500000, 0x50ffff, MWA_BANK3 ),
		new MemoryWriteAddress( 0x700010, 0x700011, OKIM6295_data_0_w ),
		new MemoryWriteAddress( 0x700014, 0x700015, YM2151_register_port_0_w ),
		new MemoryWriteAddress( 0x700016, 0x700017, YM2151_data_port_0_w ),
		new MemoryWriteAddress( 0x70001e, 0x70001f, toaplan2_coin_w ),		/* Coin count/lock */
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress pipibibs_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
		new MemoryReadAddress( 0x080000, 0x082fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x0c0000, 0x0c0fff, paletteram_word_r ),
		new MemoryReadAddress( 0x140004, 0x140007, toaplan2_0_videoram_r ),
		new MemoryReadAddress( 0x14000c, 0x14000d, input_port_0_r ),			/* VBlank */
		new MemoryReadAddress( 0x190000, 0x190fff, toaplan2_shared_r ),
		new MemoryReadAddress( 0x19c020, 0x19c021, input_port_4_r ),			/* Dip Switch A */
		new MemoryReadAddress( 0x19c024, 0x19c025, input_port_5_r ),			/* Dip Switch B */
		new MemoryReadAddress( 0x19c028, 0x19c029, input_port_6_r ),			/* Territory Jumper block */
		new MemoryReadAddress( 0x19c02c, 0x19c02d, input_port_3_r ),			/* Coin/System inputs */
		new MemoryReadAddress( 0x19c030, 0x19c031, input_port_1_r ),			/* Player 1 controls */
		new MemoryReadAddress( 0x19c034, 0x19c035, input_port_2_r ),			/* Player 2 controls */
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress pipibibs_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x080000, 0x082fff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x0c0000, 0x0c0fff, paletteram_xBBBBBGGGGGRRRRR_word_w, paletteram ),
		new MemoryWriteAddress( 0x140000, 0x140001, toaplan2_0_voffs_w ),
		new MemoryWriteAddress( 0x140004, 0x140007, toaplan2_0_videoram_w ),	/* Tile/Sprite VideoRAM */
		new MemoryWriteAddress( 0x140008, 0x140009, toaplan2_0_scroll_reg_select_w ),
		new MemoryWriteAddress( 0x14000c, 0x14000d, toaplan2_0_scroll_reg_data_w ),
		new MemoryWriteAddress( 0x190000, 0x190fff, toaplan2_shared_w, toaplan2_shared_ram ),
		new MemoryWriteAddress( 0x19c01c, 0x19c01d, toaplan2_coin_w ),		/* Coin count/lock */
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress pipibibi_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
		new MemoryReadAddress( 0x080000, 0x082fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x083000, 0x0837ff, pipibibi_spriteram_r ),
		new MemoryReadAddress( 0x083800, 0x087fff, MRA_BANK2 ),
		new MemoryReadAddress( 0x0c0000, 0x0c0fff, paletteram_word_r ),
		new MemoryReadAddress( 0x120000, 0x120fff, MRA_BANK3 ),
		new MemoryReadAddress( 0x180000, 0x182fff, pipibibi_videoram_r ),
		new MemoryReadAddress( 0x190002, 0x190003, pipibibi_z80_status_r ),	/* Z80 ready ? */
		new MemoryReadAddress( 0x19c020, 0x19c021, input_port_4_r ),			/* Dip Switch A */
		new MemoryReadAddress( 0x19c024, 0x19c025, input_port_5_r ),			/* Dip Switch B */
		new MemoryReadAddress( 0x19c028, 0x19c029, input_port_6_r ),			/* Territory Jumper block */
		new MemoryReadAddress( 0x19c02c, 0x19c02d, input_port_3_r ),			/* Coin/System inputs */
		new MemoryReadAddress( 0x19c030, 0x19c031, input_port_1_r ),			/* Player 1 controls */
		new MemoryReadAddress( 0x19c034, 0x19c035, input_port_2_r ),			/* Player 2 controls */
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress pipibibi_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x080000, 0x082fff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x083000, 0x0837ff, pipibibi_spriteram_w ),   /* SpriteRAM */
		new MemoryWriteAddress( 0x083800, 0x087fff, MWA_BANK2 ),				/* SpriteRAM (unused) */
		new MemoryWriteAddress( 0x0c0000, 0x0c0fff, paletteram_xBBBBBGGGGGRRRRR_word_w, paletteram ),
		new MemoryWriteAddress( 0x120000, 0x120fff, MWA_BANK3 ),				/* Copy of SpriteRAM ? */
	//	new MemoryWriteAddress( 0x13f000, 0x13f001, MWA_NOP ),				/* ??? */
		new MemoryWriteAddress( 0x180000, 0x182fff, pipibibi_videoram_w ),	/* TileRAM */
		new MemoryWriteAddress( 0x188000, 0x18800f, pipibibi_scroll_w ),
		new MemoryWriteAddress( 0x190010, 0x190011, pipibibi_z80_task_w ),	/* Z80 task to perform */
		new MemoryWriteAddress( 0x19c01c, 0x19c01d, toaplan2_coin_w ),		/* Coin count/lock */
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress fixeight_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x103fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x200008, 0x200009, input_port_4_r ),			/* Dip Switch A */
		new MemoryReadAddress( 0x20000c, 0x20000d, input_port_5_r ),			/* Dip Switch B */
		new MemoryReadAddress( 0x200010, 0x200011, input_port_1_r ),			/* Player 1 controls */
		new MemoryReadAddress( 0x200014, 0x200015, input_port_2_r ),			/* Player 2 controls */
		new MemoryReadAddress( 0x200018, 0x200019, input_port_3_r ),			/* Coin/System inputs */
		new MemoryReadAddress( 0x280000, 0x28dfff, MRA_BANK2 ),				/* part of shared ram ? */
	#if Zx80
		new MemoryReadAddress( 0x28e000, 0x28fbff, shared_ram_r ),			/* $21f000 status port */
		new MemoryReadAddress( 0x28fc00, 0x28ffff, Zx80_sharedram_r ),		/* 16-bit on 68000 side, 8-bit on Zx80 side */
	#else
		new MemoryReadAddress( 0x28e000, 0x28efff, shared_ram_r ),
		new MemoryReadAddress( 0x28f000, 0x28f001, Zx80_status_port_r ),		/* Zx80 status port */
		new MemoryReadAddress( 0x28f002, 0x28fbff, MRA_BANK3 ),				/* part of shared ram ? */
		new MemoryReadAddress( 0x28fc00, 0x28ffff, Zx80_sharedram_r ),		/* 16-bit on 68000 side, 8-bit on Zx80 side */
	#endif
		new MemoryReadAddress( 0x300004, 0x300007, toaplan2_0_videoram_r ),
		new MemoryReadAddress( 0x30000c, 0x30000d, input_port_0_r ),
		new MemoryReadAddress( 0x400000, 0x400fff, paletteram_word_r ),
		new MemoryReadAddress( 0x500000, 0x501fff, MRA_BANK4 ),
		new MemoryReadAddress( 0x502000, 0x5021ff, MRA_BANK5 ),
		new MemoryReadAddress( 0x503000, 0x5031ff, MRA_BANK6 ),
		new MemoryReadAddress( 0x600000, 0x60ffff, MRA_BANK7 ),
		new MemoryReadAddress( 0x800000, 0x800001, video_count_r ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress fixeight_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x103fff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x20001c, 0x20001d, toaplan2_coin_w ),		/* Coin count/lock */
		new MemoryWriteAddress( 0x280000, 0x28dfff, MWA_BANK2 ),				/* part of shared ram ? */
	#if Zx80
		new MemoryWriteAddress( 0x28e000, 0x28fbff, shared_ram_w, toaplan2_shared_ram ),	/* $21F000 */
		new MemoryWriteAddress( 0x28fc00, 0x28ffff, Zx80_sharedram_w, Zx80_shared_ram ),	/* 16-bit on 68000 side, 8-bit on Zx80 side */
	#else
		new MemoryWriteAddress( 0x28e000, 0x28efff, shared_ram_w, toaplan2_shared_ram ),
		new MemoryWriteAddress( 0x28f000, 0x28f001, Zx80_command_port_w ),	/* Zx80 command port */
		new MemoryWriteAddress( 0x28f002, 0x28fbff, MWA_BANK3 ),				/* part of shared ram ? */
		new MemoryWriteAddress( 0x28fc00, 0x28ffff, Zx80_sharedram_w, Zx80_shared_ram ),	/* 16-bit on 68000 side, 8-bit on Zx80 side */
	#endif
		new MemoryWriteAddress( 0x300000, 0x300001, toaplan2_0_voffs_w ),		/* VideoRAM selector/offset */
		new MemoryWriteAddress( 0x300004, 0x300007, toaplan2_0_videoram_w ),	/* Tile/Sprite VideoRAM */
		new MemoryWriteAddress( 0x300008, 0x300009, toaplan2_0_scroll_reg_select_w ),
		new MemoryWriteAddress( 0x30000c, 0x30000d, toaplan2_0_scroll_reg_data_w ),
		new MemoryWriteAddress( 0x400000, 0x400fff, paletteram_xBBBBBGGGGGRRRRR_word_w, paletteram ),
		new MemoryWriteAddress( 0x500000, 0x501fff, MWA_BANK4 ),
		new MemoryWriteAddress( 0x502000, 0x5021ff, MWA_BANK5 ),
		new MemoryWriteAddress( 0x503000, 0x5031ff, MWA_BANK6 ),
		new MemoryWriteAddress( 0x600000, 0x60ffff, MWA_BANK7 ),
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress vfive_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x103fff, MRA_BANK1 ),
	//	new MemoryReadAddress( 0x200000, 0x20ffff, MRA_ROM ),				/* Sound ROM is here ??? */
		new MemoryReadAddress( 0x200010, 0x200011, input_port_1_r ),			/* Player 1 controls */
		new MemoryReadAddress( 0x200014, 0x200015, input_port_2_r ),			/* Player 2 controls */
		new MemoryReadAddress( 0x200018, 0x200019, input_port_3_r ),			/* Coin/System inputs */
	#if Zx80
		new MemoryReadAddress( 0x21e000, 0x21fbff, shared_ram_r ),			/* $21f000 status port */
		new MemoryReadAddress( 0x21fc00, 0x21ffff, Zx80_sharedram_r ),		/* 16-bit on 68000 side, 8-bit on Zx80 side */
	#else
		new MemoryReadAddress( 0x21e000, 0x21efff, shared_ram_r ),
		new MemoryReadAddress( 0x21f000, 0x21f001, Zx80_status_port_r ),		/* Zx80 status port */
		new MemoryReadAddress( 0x21f004, 0x21f005, input_port_4_r ),			/* Dip Switch A */
		new MemoryReadAddress( 0x21f006, 0x21f007, input_port_5_r ),			/* Dip Switch B */
		new MemoryReadAddress( 0x21f008, 0x21f009, input_port_6_r ),			/* Territory Jumper block */
		new MemoryReadAddress( 0x21fc00, 0x21ffff, Zx80_sharedram_r ),		/* 16-bit on 68000 side, 8-bit on Zx80 side */
	#endif
		new MemoryReadAddress( 0x300004, 0x300007, toaplan2_0_videoram_r ),
		new MemoryReadAddress( 0x30000c, 0x30000d, input_port_0_r ),
		new MemoryReadAddress( 0x400000, 0x400fff, paletteram_word_r ),
		new MemoryReadAddress( 0x700000, 0x700001, video_count_r ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress vfive_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x103fff, MWA_BANK1 ),
	//	new MemoryWriteAddress( 0x200000, 0x20ffff, MWA_ROM ),				/* Sound ROM is here ??? */
		new MemoryWriteAddress( 0x20001c, 0x20001d, toaplan2_coin_w ),		/* Coin count/lock */
	#if Zx80
		new MemoryWriteAddress( 0x21e000, 0x21fbff, shared_ram_w, toaplan2_shared_ram ),	/* $21F000 */
		new MemoryWriteAddress( 0x21fc00, 0x21ffff, Zx80_sharedram_w, Zx80_shared_ram ),	/* 16-bit on 68000 side, 8-bit on Zx80 side */
	#else
		new MemoryWriteAddress( 0x21e000, 0x21efff, shared_ram_w, toaplan2_shared_ram ),
		new MemoryWriteAddress( 0x21f000, 0x21f001, Zx80_command_port_w ),	/* Zx80 command port */
		new MemoryWriteAddress( 0x21fc00, 0x21ffff, Zx80_sharedram_w, Zx80_shared_ram ),	/* 16-bit on 68000 side, 8-bit on Zx80 side */
	#endif
		new MemoryWriteAddress( 0x300000, 0x300001, toaplan2_0_voffs_w ),		/* VideoRAM selector/offset */
		new MemoryWriteAddress( 0x300004, 0x300007, toaplan2_0_videoram_w ),	/* Tile/Sprite VideoRAM */
		new MemoryWriteAddress( 0x300008, 0x300009, toaplan2_0_scroll_reg_select_w ),
		new MemoryWriteAddress( 0x30000c, 0x30000d, toaplan2_0_scroll_reg_data_w ),
		new MemoryWriteAddress( 0x400000, 0x400fff, paletteram_xBBBBBGGGGGRRRRR_word_w, paletteram ),
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress batsugun_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x200010, 0x200011, input_port_1_r ),			/* Player 1 controls */
		new MemoryReadAddress( 0x200014, 0x200015, input_port_2_r ),			/* Player 2 controls */
		new MemoryReadAddress( 0x200018, 0x200019, input_port_3_r ),			/* Coin/System inputs */
		new MemoryReadAddress( 0x210000, 0x21bbff, MRA_BANK2 ),
	#if Zx80
		new MemoryReadAddress( 0x21e000, 0x21fbff, shared_ram_r ),			/* $21f000 status port */
		new MemoryReadAddress( 0x21fc00, 0x21ffff, Zx80_sharedram_r ),		/* 16-bit on 68000 side, 8-bit on Zx80 side */
	#else
		new MemoryReadAddress( 0x21e000, 0x21efff, shared_ram_r ),
		new MemoryReadAddress( 0x21f000, 0x21f001, Zx80_status_port_r ),		/* Zx80 status port */
		new MemoryReadAddress( 0x21f004, 0x21f005, input_port_4_r ),			/* Dip Switch A */
		new MemoryReadAddress( 0x21f006, 0x21f007, input_port_5_r ),			/* Dip Switch B */
		new MemoryReadAddress( 0x21f008, 0x21f009, input_port_6_r ),			/* Territory Jumper block */
		new MemoryReadAddress( 0x21fc00, 0x21ffff, Zx80_sharedram_r ),		/* 16-bit on 68000 side, 8-bit on Zx80 side */
	#endif
		/***** The following in 0x30000x are for video controller 2 ******/
		new MemoryReadAddress( 0x300004, 0x300007, toaplan2_1_videoram_r ),	/* tile layers */
		new MemoryReadAddress( 0x30000c, 0x30000d, input_port_0_r ),			/* VBlank */
		new MemoryReadAddress( 0x400000, 0x400fff, paletteram_word_r ),
		/***** The following in 0x50000x are for video controller 1 ******/
		new MemoryReadAddress( 0x500004, 0x500007, toaplan2_0_videoram_r ),	/* tile layers 2 */
		new MemoryReadAddress( 0x700000, 0x700001, video_count_r ),			/* test bit 8 */
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress batsugun_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x20001c, 0x20001d, toaplan2_coin_w ),		/* Coin count/lock */
		new MemoryWriteAddress( 0x210000, 0x21bbff, MWA_BANK2 ),
	#if Zx80
		new MemoryWriteAddress( 0x21e000, 0x21fbff, shared_ram_w, toaplan2_shared_ram ),	/* $21F000 */
		new MemoryWriteAddress( 0x21fc00, 0x21ffff, Zx80_sharedram_w, Zx80_shared_ram ),	/* 16-bit on 68000 side, 8-bit on Zx80 side */
	#else
		new MemoryWriteAddress( 0x21e000, 0x21efff, shared_ram_w, toaplan2_shared_ram ),
		new MemoryWriteAddress( 0x21f000, 0x21f001, Zx80_command_port_w ),	/* Zx80 command port */
		new MemoryWriteAddress( 0x21fc00, 0x21ffff, Zx80_sharedram_w, Zx80_shared_ram ),	/* 16-bit on 68000 side, 8-bit on Zx80 side */
	#endif
		/***** The following in 0x30000x are for video controller 2 ******/
		new MemoryWriteAddress( 0x300000, 0x300001, toaplan2_1_voffs_w ),		/* VideoRAM selector/offset */
		new MemoryWriteAddress( 0x300004, 0x300007, toaplan2_1_videoram_w ),	/* Tile/Sprite VideoRAM */
		new MemoryWriteAddress( 0x300008, 0x300009, toaplan2_1_scroll_reg_select_w ),
		new MemoryWriteAddress( 0x30000c, 0x30000d, toaplan2_1_scroll_reg_data_w ),
		new MemoryWriteAddress( 0x400000, 0x400fff, paletteram_xBBBBBGGGGGRRRRR_word_w, paletteram ),
		/***** The following in 0x50000x are for video controller 1 ******/
		new MemoryWriteAddress( 0x500000, 0x500001, toaplan2_0_voffs_w ),		/* VideoRAM selector/offset */
		new MemoryWriteAddress( 0x500004, 0x500007, toaplan2_0_videoram_w ),	/* Tile/Sprite VideoRAM */
		new MemoryWriteAddress( 0x500008, 0x500009, toaplan2_0_scroll_reg_select_w ),
		new MemoryWriteAddress( 0x50000c, 0x50000d, toaplan2_0_scroll_reg_data_w ),
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress snowbro2_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x300004, 0x300007, toaplan2_0_videoram_r ),	/* tile layers */
		new MemoryReadAddress( 0x30000c, 0x30000d, input_port_0_r ),			/* VBlank */
		new MemoryReadAddress( 0x400000, 0x400fff, paletteram_word_r ),
		new MemoryReadAddress( 0x500002, 0x500003, YM2151_status_port_0_r ),
		new MemoryReadAddress( 0x600000, 0x600001, OKIM6295_status_0_r ),
		new MemoryReadAddress( 0x700000, 0x700001, input_port_8_r ),			/* Territory Jumper block */
		new MemoryReadAddress( 0x700004, 0x700005, input_port_6_r ),			/* Dip Switch A */
		new MemoryReadAddress( 0x700008, 0x700009, input_port_7_r ),			/* Dip Switch B */
		new MemoryReadAddress( 0x70000c, 0x70000d, input_port_1_r ),			/* Player 1 controls */
		new MemoryReadAddress( 0x700010, 0x700011, input_port_2_r ),			/* Player 2 controls */
		new MemoryReadAddress( 0x700014, 0x700015, input_port_3_r ),			/* Player 3 controls */
		new MemoryReadAddress( 0x700018, 0x700019, input_port_4_r ),			/* Player 4 controls */
		new MemoryReadAddress( 0x70001c, 0x70001d, input_port_5_r ),			/* Coin/System inputs */
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress snowbro2_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x300000, 0x300001, toaplan2_0_voffs_w ),		/* VideoRAM selector/offset */
		new MemoryWriteAddress( 0x300004, 0x300007, toaplan2_0_videoram_w ),	/* Tile/Sprite VideoRAM */
		new MemoryWriteAddress( 0x300008, 0x300009, toaplan2_0_scroll_reg_select_w ),
		new MemoryWriteAddress( 0x30000c, 0x30000d, toaplan2_0_scroll_reg_data_w ),
		new MemoryWriteAddress( 0x400000, 0x400fff, paletteram_xBBBBBGGGGGRRRRR_word_w, paletteram ),
		new MemoryWriteAddress( 0x500000, 0x500001, YM2151_register_port_0_w ),
		new MemoryWriteAddress( 0x500002, 0x500003, YM2151_data_port_0_w ),
		new MemoryWriteAddress( 0x600000, 0x600001, OKIM6295_data_0_w ),
		new MemoryWriteAddress( 0x700030, 0x700031, oki_bankswitch_w ),		/* Sample bank switch */
		new MemoryWriteAddress( 0x700034, 0x700035, toaplan2_coin_w ),		/* Coin count/lock */
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress mahoudai_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x218000, 0x21bfff, raizing_shared_ram_r ),
		new MemoryReadAddress( 0x21c002, 0x21c003, YM2151_status_port_0_r ),
	//	new MemoryReadAddress( 0x21c008, 0x21c009, OKIM6295_status_0_r ),
		new MemoryReadAddress( 0x21c020, 0x21c021, input_port_1_r ),
		new MemoryReadAddress( 0x21c024, 0x21c025, input_port_2_r ),
		new MemoryReadAddress( 0x21c028, 0x21c029, input_port_3_r ),
		new MemoryReadAddress( 0x21c02c, 0x21c02d, input_port_4_r ),
		new MemoryReadAddress( 0x21c030, 0x21c031, input_port_5_r ),
		new MemoryReadAddress( 0x21c034, 0x21c035, input_port_6_r ),
		new MemoryReadAddress( 0x21c03c, 0x21c03d, video_count_r ),
		new MemoryReadAddress( 0x300004, 0x300007, toaplan2_0_videoram_r ),	/* tile layers */
		new MemoryReadAddress( 0x30000c, 0x30000d, input_port_0_r ),			/* VBlank */
		new MemoryReadAddress( 0x400000, 0x400fff, paletteram_word_r ),
		new MemoryReadAddress( 0x500000, 0x503fff, raizing_textram_r ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress mahoudai_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x218000, 0x21bfff, raizing_shared_ram_w, raizing_shared_ram ),
		new MemoryWriteAddress( 0x21c000, 0x21c001, YM2151_register_port_0_w ),
		new MemoryWriteAddress( 0x21c004, 0x21c005, YM2151_data_port_0_w ),
		new MemoryWriteAddress( 0x21c008, 0x21c009, OKIM6295_data_0_w ),
		new MemoryWriteAddress( 0x21c01c, 0x21c01d, toaplan2_coin_w ),
		new MemoryWriteAddress( 0x300000, 0x300001, toaplan2_0_voffs_w ),		/* VideoRAM selector/offset */
		new MemoryWriteAddress( 0x300004, 0x300007, toaplan2_0_videoram_w ),	/* Tile/Sprite VideoRAM */
		new MemoryWriteAddress( 0x300008, 0x300009, toaplan2_0_scroll_reg_select_w ),
		new MemoryWriteAddress( 0x30000c, 0x30000d, toaplan2_0_scroll_reg_data_w ),
		new MemoryWriteAddress( 0x400000, 0x400fff, paletteram_xBBBBBGGGGGRRRRR_word_w, paletteram ),
		new MemoryWriteAddress( 0x500000, 0x503fff, raizing_textram_w, textvideoram),
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress shippumd_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x0fffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x218000, 0x21bfff, raizing_shared_ram_r ),
		new MemoryReadAddress( 0x21c002, 0x21c003, YM2151_status_port_0_r ),
	//	new MemoryReadAddress( 0x21c008, 0x21c009, OKIM6295_status_0_r ),
		new MemoryReadAddress( 0x21c020, 0x21c021, input_port_1_r ),
		new MemoryReadAddress( 0x21c024, 0x21c025, input_port_2_r ),
		new MemoryReadAddress( 0x21c028, 0x21c029, input_port_3_r ),
		new MemoryReadAddress( 0x21c02c, 0x21c02d, input_port_4_r ),
		new MemoryReadAddress( 0x21c030, 0x21c031, input_port_5_r ),
		new MemoryReadAddress( 0x21c034, 0x21c035, input_port_6_r ),
		new MemoryReadAddress( 0x21c03c, 0x21c03d, video_count_r ),
		new MemoryReadAddress( 0x300004, 0x300007, toaplan2_0_videoram_r ),	/* tile layers */
		new MemoryReadAddress( 0x30000c, 0x30000d, input_port_0_r ),			/* VBlank */
		new MemoryReadAddress( 0x400000, 0x400fff, paletteram_word_r ),
		new MemoryReadAddress( 0x500000, 0x503fff, raizing_textram_r ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress shippumd_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x0fffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x218000, 0x21bfff, raizing_shared_ram_w, raizing_shared_ram ),
		new MemoryWriteAddress( 0x21c000, 0x21c001, YM2151_register_port_0_w ),
		new MemoryWriteAddress( 0x21c004, 0x21c005, YM2151_data_port_0_w ),
		new MemoryWriteAddress( 0x21c008, 0x21c009, OKIM6295_data_0_w ),
		new MemoryWriteAddress( 0x21c01c, 0x21c01d, toaplan2_coin_w ),
		new MemoryWriteAddress( 0x300000, 0x300001, toaplan2_0_voffs_w ),		/* VideoRAM selector/offset */
		new MemoryWriteAddress( 0x300004, 0x300007, toaplan2_0_videoram_w ),	/* Tile/Sprite VideoRAM */
		new MemoryWriteAddress( 0x300008, 0x300009, toaplan2_0_scroll_reg_select_w ),
		new MemoryWriteAddress( 0x30000c, 0x30000d, toaplan2_0_scroll_reg_data_w ),
		new MemoryWriteAddress( 0x400000, 0x400fff, paletteram_xBBBBBGGGGGRRRRR_word_w, paletteram ),
		new MemoryWriteAddress( 0x500000, 0x503fff, raizing_textram_w, textvideoram),
		new MemoryWriteAddress( -1 )
	};
	
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x87ff, MRA_RAM ),
		new MemoryReadAddress( 0xe000, 0xe000, YM3812_status_port_0_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0x87ff, MWA_RAM, toaplan2_shared_ram ),
		new MemoryWriteAddress( 0xe000, 0xe000, YM3812_control_port_0_w ),
		new MemoryWriteAddress( 0xe001, 0xe001, YM3812_write_port_0_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	/* Added by Yochizo 2000/08/20 */
	
	static MemoryReadAddress raizing_sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0xbfff, MRA_ROM ),
		new MemoryReadAddress( 0xc000, 0xdfff, MRA_RAM ),
		new MemoryReadAddress( 0xe001, 0xe001, YM2151_status_port_0_r ),
	//	new MemoryReadAddress( 0xe004, 0xe004, OKIM6295_status_0_r ),
		new MemoryReadAddress( 0xe010, 0xe010, input_port_1_r ),
		new MemoryReadAddress( 0xe012, 0xe012, input_port_2_r ),
		new MemoryReadAddress( 0xe014, 0xe014, input_port_3_r ),
		new MemoryReadAddress( 0xe016, 0xe016, input_port_4_r ),
		new MemoryReadAddress( 0xe018, 0xe018, input_port_5_r ),
		new MemoryReadAddress( 0xe01a, 0xe01a, input_port_6_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress raizing_sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new MemoryWriteAddress( 0xc000, 0xdfff, MWA_RAM, raizing_shared_ram ),
		new MemoryWriteAddress( 0xe000, 0xe000, YM2151_register_port_0_w ),
		new MemoryWriteAddress( 0xe001, 0xe001, YM2151_data_port_0_w ),
		new MemoryWriteAddress( 0xe004, 0xe004, OKIM6295_data_0_w ),
		new MemoryWriteAddress( 0xe00e, 0xe00e, toaplan2_coin_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	#if HD64x180
	static MemoryReadAddress hd647180_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0xfe00, 0xffff, MRA_RAM ),			/* Internal 512 bytes of RAM */
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress hd647180_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0xfe00, 0xffff, MWA_RAM ),			/* Internal 512 bytes of RAM */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	#endif
	
	
	#if Zx80
	static MemoryReadAddress Zx80_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_RAM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress Zx80_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x07fff, MWA_RAM, Zx80_sharedram ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	#endif
	
	
	
	/*****************************************************************************
		Input Port definitions
		Service input of the TOAPLAN2_SYSTEM_INPUTS is used as a Pause type input.
		If you press then release the following buttons, the following occurs:
		Service & P2 start            : The game will pause.
		P1 start                      : The game will continue.
		Service & P1 start & P2 start : The game will play in slow motion.
	*****************************************************************************/
	
	#define  TOAPLAN2_PLAYER_INPUT( player, button3 )								\
		PORT_START();  																	\
		PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | player );\
		PORT_BIT( 0x0002, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | player );\
		PORT_BIT( 0x0004, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | player );\
		PORT_BIT( 0x0008, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | player );\
		PORT_BIT( 0x0010, IP_ACTIVE_HIGH, IPT_BUTTON1 | player);					\
		PORT_BIT( 0x0020, IP_ACTIVE_HIGH, IPT_BUTTON2 | player);					\
		PORT_BIT( 0x0040, IP_ACTIVE_HIGH, button3 );								\
		PORT_BIT( 0xff80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	#define  TOAPLAN2_SYSTEM_INPUTS						\
		PORT_START(); 										\
		PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_COIN3 );	\
		PORT_BIT( 0x0002, IP_ACTIVE_HIGH, IPT_TILT );\
		PORT_BIT( 0x0004, IP_ACTIVE_HIGH, IPT_SERVICE1 );
		PORT_BIT( 0x0008, IP_ACTIVE_HIGH, IPT_COIN1 );\
		PORT_BIT( 0x0010, IP_ACTIVE_HIGH, IPT_COIN2 );\
		PORT_BIT( 0x0020, IP_ACTIVE_HIGH, IPT_START1 );\
		PORT_BIT( 0x0040, IP_ACTIVE_HIGH, IPT_START2 );\
		PORT_BIT( 0xff80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	#define  TOAPLAN2_DSW_A												\
		PORT_START(); 														\
		PORT_DIPNAME( 0x01,	0x00, DEF_STR( "Unused") );					\
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );						\
		PORT_DIPSETTING(	0x01, DEF_STR( "On") );						\
		PORT_DIPNAME( 0x02,	0x00, DEF_STR( "Flip_Screen") );				\
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );						\
		PORT_DIPSETTING(	0x02, DEF_STR( "On") );						\
		PORT_SERVICE( 0x04, IP_ACTIVE_HIGH );	/* Service Mode */	\
		PORT_DIPNAME( 0x08,	0x00, DEF_STR( "Demo_Sounds") );				\
		PORT_DIPSETTING(	0x08, DEF_STR( "Off") );						\
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );						\
		PORT_DIPNAME( 0x30,	0x00, DEF_STR( "Coin_A") );					\
		PORT_DIPSETTING(	0x30, DEF_STR( "4C_1C") );					\
		PORT_DIPSETTING(	0x20, DEF_STR( "3C_1C") );					\
		PORT_DIPSETTING(	0x10, DEF_STR( "2C_1C") );					\
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );					\
		PORT_DIPNAME( 0xc0,	0x00, DEF_STR( "Coin_B") );					\
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_2C") );					\
		PORT_DIPSETTING(	0x40, DEF_STR( "1C_3C") );					\
		PORT_DIPSETTING(	0x80, DEF_STR( "1C_4C") );					\
		PORT_DIPSETTING(	0xc0, DEF_STR( "1C_6C") );					\
	/*	Non-European territories coin setups							\
		PORT_DIPNAME( 0x30,	0x00, DEF_STR( "Coin_A") );					\
		PORT_DIPSETTING(	0x20, DEF_STR( "2C_1C") );					\
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );					\
		PORT_DIPSETTING(	0x30, DEF_STR( "2C_3C") );					\
		PORT_DIPSETTING(	0x10, DEF_STR( "1C_2C") );					\
		PORT_DIPNAME( 0xc0,	0x00, DEF_STR( "Coin_B") );					\
		PORT_DIPSETTING(	0x80, DEF_STR( "2C_1C") );					\
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );					\
		PORT_DIPSETTING(	0xc0, DEF_STR( "2C_3C") );					\
		PORT_DIPSETTING(	0x40, DEF_STR( "1C_2C") );					\
	*/
	
	
	
	
	static InputPortPtr input_ports_tekipaki = new InputPortPtr(){ public void handler() { 
	PORT_START(); 		/* (0) VBlank */
		PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_VBLANK );	PORT_BIT( 0xfffe, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER1, IPT_UNKNOWN )
	
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER2, IPT_UNKNOWN )
	
		TOAPLAN2_SYSTEM_INPUTS
	
		TOAPLAN2_DSW_A
	
		PORT_START(); 		/* (5) DSWB */
		PORT_DIPNAME( 0x03,	0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x01, "Easy" );	PORT_DIPSETTING(	0x00, "Medium" );	PORT_DIPSETTING(	0x02, "Hard" );	PORT_DIPSETTING(	0x03, "Hardest" );	PORT_DIPNAME( 0x04,	0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08,	0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10,	0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20,	0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40,	0x00, "Game Mode" );	PORT_DIPSETTING(	0x00, "Normal" );	PORT_DIPSETTING(	0x40, "Stop" );	PORT_DIPNAME( 0x80,	0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	
		PORT_START(); 		/* (6) Territory Jumper block */
		PORT_DIPNAME( 0x0f,	0x02, "Territory" );	PORT_DIPSETTING(	0x02, "Europe" );	PORT_DIPSETTING(	0x01, "USA" );	PORT_DIPSETTING(	0x00, "Japan" );	PORT_DIPSETTING(	0x03, "Hong Kong" );	PORT_DIPSETTING(	0x05, "Taiwan" );	PORT_DIPSETTING(	0x04, "Korea" );	PORT_DIPSETTING(	0x07, "USA (Romstar); )
		PORT_DIPSETTING(	0x08, "Hong Kong (Honest Trading Co.); )
		PORT_DIPSETTING(	0x06, "Taiwan (Spacy Co. Ltd); )
		PORT_BIT( 0xf0, IP_ACTIVE_HIGH, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_ghox = new InputPortPtr(){ public void handler() { 
	PORT_START(); 		/* (0) VBlank */
		PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_VBLANK );	PORT_BIT( 0xfffe, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER1, IPT_UNKNOWN )
	
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER2, IPT_UNKNOWN )
	
		TOAPLAN2_SYSTEM_INPUTS
	
		TOAPLAN2_DSW_A
	
		PORT_START(); 		/* (5) DSWB */
		PORT_DIPNAME( 0x03,	0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x01, "Easy" );	PORT_DIPSETTING(	0x00, "Medium" );	PORT_DIPSETTING(	0x02, "Hard" );	PORT_DIPSETTING(	0x03, "Hardest" );	PORT_DIPNAME( 0x0c,	0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(	0x00, "100K, every 200K" );	PORT_DIPSETTING(	0x04, "100K, every 300K" );	PORT_DIPSETTING(	0x08, "100K" );	PORT_DIPSETTING(	0x0c, "None" );	PORT_DIPNAME( 0x30,	0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x30, "1" );	PORT_DIPSETTING(	0x20, "2" );	PORT_DIPSETTING(	0x00, "3" );	PORT_DIPSETTING(	0x10, "5" );	PORT_BITX(	  0x40,	0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80,	0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	
		PORT_START(); 		/* (6) Territory Jumper block */
		PORT_DIPNAME( 0x0f,	0x02, "Territory" );	PORT_DIPSETTING(	0x02, "Europe" );	PORT_DIPSETTING(	0x01, "USA" );	PORT_DIPSETTING(	0x00, "Japan" );	PORT_DIPSETTING(	0x04, "Korea" );	PORT_DIPSETTING(	0x03, "Hong Kong (Honest Trading Co." );	PORT_DIPSETTING(	0x05, "Taiwan" );	PORT_DIPSETTING(	0x06, "Spain & Portugal (APM Electronics SA); )
		PORT_DIPSETTING(	0x07, "Italy (Star Electronica SRL); )
		PORT_DIPSETTING(	0x08, "UK (JP Leisure Ltd); )
		PORT_DIPSETTING(	0x0a, "Europe (Nova Apparate GMBH & Co); )
		PORT_DIPSETTING(	0x0d, "Europe (Taito Corporation Japan); )
		PORT_DIPSETTING(	0x09, "USA (Romstar); )
		PORT_DIPSETTING(	0x0b, "USA (Taito America Corporation); )
		PORT_DIPSETTING(	0x0c, "USA (Taito Corporation Japan); )
		PORT_DIPSETTING(	0x0e, "Japan (Taito Corporation); )
		PORT_BIT( 0xf0, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START(); 		/* (7)  Paddle 1 (left-right)  read at $100000 */
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER1, 25, 15, 0, 0xff );
		PORT_START(); 		/* (8)  Paddle 2 (left-right)  read at $040000 */
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER2, 25, 15, 0, 0xff );
		PORT_START(); 		/* (9)  Paddle 1 (fake up-down) */
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL_V | IPF_PLAYER1, 15, 0, 0, 0xff );
		PORT_START(); 		/* (10) Paddle 2 (fake up-down) */
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL_V | IPF_PLAYER2, 15, 0, 0, 0xff );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_dogyuun = new InputPortPtr(){ public void handler() { 
	PORT_START(); 		/* (0) VBlank */
		PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_VBLANK );	PORT_BIT( 0xfffe, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER1, IPT_BUTTON3 | IPF_PLAYER1 )
	
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER2, IPT_BUTTON3 | IPF_PLAYER2 )
	
		TOAPLAN2_SYSTEM_INPUTS
	
		PORT_START(); 		/* (4) DSWA */
		PORT_DIPNAME( 0x0001,	0x0000, "Play Mode" );	PORT_DIPSETTING(		0x0000, "Coin Play" );	PORT_DIPSETTING(		0x0001, DEF_STR( "Free_Play"));
		PORT_DIPNAME( 0x0002,	0x0000, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0002, DEF_STR( "On") );
		PORT_SERVICE( 0x0004,	IP_ACTIVE_HIGH );	/* Service Mode */
		PORT_DIPNAME( 0x0008,	0x0000, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(		0x0008, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0030,	0x0000, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(		0x0030, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(		0x0020, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(		0x0010, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0x00c0,	0x0000, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(		0x0080, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(		0x00c0, DEF_STR( "1C_6C") );
	/*	Non-European territories coin setups
		PORT_DIPNAME( 0x0030,	0x0000, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(		0x0020, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(		0x0030, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(		0x0010, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0xc0,		0x0000, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(		0x0080, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(		0x00c0, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "1C_2C") );
	*/
		PORT_BIT( 0xff00, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START(); 		/* (5) DSWB */
		PORT_DIPNAME( 0x0003,	0x0000, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(		0x0001, "Easy" );	PORT_DIPSETTING(		0x0000, "Medium" );	PORT_DIPSETTING(		0x0002, "Hard" );	PORT_DIPSETTING(		0x0003, "Hardest" );	PORT_DIPNAME( 0x000c,	0x0000, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(		0x0004, "200K, 400K, 600K" );	PORT_DIPSETTING(		0x0000, "200K" );	PORT_DIPSETTING(		0x0008, "400K" );	PORT_DIPSETTING(		0x000c, "None" );	PORT_DIPNAME( 0x0030,	0x0000, DEF_STR( "Lives") );
		PORT_DIPSETTING(		0x0030, "1" );	PORT_DIPSETTING(		0x0020, "2" );	PORT_DIPSETTING(		0x0000, "3" );	PORT_DIPSETTING(		0x0010, "5" );	PORT_BITX(	  0x0040,	0x0000, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPSETTING(		0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "On") );
		PORT_DIPNAME( 0x0080,	0x0000, "Allow Continue" );	PORT_DIPSETTING(		0x0080, DEF_STR( "No") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "Yes") );
	
		PORT_START(); 		/* (6) Territory Jumper block */
		PORT_DIPNAME( 0x0f,	0x03, "Territory" );	PORT_DIPSETTING(	0x03, "Europe" );	PORT_DIPSETTING(	0x01, "USA" );	PORT_DIPSETTING(	0x00, "Japan" );	PORT_DIPSETTING(	0x05, "Korea (Unite Trading license); )
		PORT_DIPSETTING(	0x04, "Hong Kong (Charterfield license); )
		PORT_DIPSETTING(	0x06, "Taiwan" );	PORT_DIPSETTING(	0x08, "South East Asia (Charterfield license); )
		PORT_DIPSETTING(	0x0c, "USA (Atari Games Corp license); )
		PORT_DIPSETTING(	0x0f, "Japan (Taito Corp license); )
	/*	Duplicate settings
		PORT_DIPSETTING(	0x0b, "Europe" );	PORT_DIPSETTING(	0x07, "USA" );	PORT_DIPSETTING(	0x0a, "Korea (Unite Trading license); )
		PORT_DIPSETTING(	0x09, "Hong Kong (Charterfield license); )
		PORT_DIPSETTING(	0x0b, "Taiwan" );	PORT_DIPSETTING(	0x0d, "South East Asia (Charterfield license); )
		PORT_DIPSETTING(	0x0c, "USA (Atari Games Corp license); )
	*/
		PORT_BIT( 0xf0, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* bit 0x10 sound ready */
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_kbash = new InputPortPtr(){ public void handler() { 
	PORT_START(); 		/* (0) VBlank */
		PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_VBLANK );	PORT_BIT( 0xfffe, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER1, IPT_BUTTON3 | IPF_PLAYER1 )
	
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER2, IPT_BUTTON3 | IPF_PLAYER2 )
	
		TOAPLAN2_SYSTEM_INPUTS
	
		PORT_START(); 		/* (4) DSWA */
		PORT_DIPNAME( 0x0001,	0x0000, "Continue Mode" );	PORT_DIPSETTING(		0x0000, "Normal" );	PORT_DIPSETTING(		0x0001, "Discount" );	PORT_DIPNAME( 0x0002,	0x0000, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0002, DEF_STR( "On") );
		PORT_SERVICE( 0x0004,	IP_ACTIVE_HIGH );	/* Service Mode */
		PORT_DIPNAME( 0x0008,	0x0000, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(		0x0008, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0030,	0x0000, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(		0x0030, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(		0x0020, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(		0x0010, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0x00c0,	0x0000, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(		0x0080, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(		0x00c0, DEF_STR( "1C_6C") );
	/*	Non-European territories coin setup
		PORT_DIPNAME( 0x0030,	0x0000, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(		0x0020, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(		0x0030, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(		0x0010, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x00c0,	0x0000, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(		0x0080, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(		0x00c0, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "1C_2C") );
	*/
		PORT_BIT( 0xff00, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START(); 		/* (5) DSWB */
		PORT_DIPNAME( 0x0003,	0x0000, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(		0x0001, "Easy" );	PORT_DIPSETTING(		0x0000, "Medium" );	PORT_DIPSETTING(		0x0002, "Hard" );	PORT_DIPSETTING(		0x0003, "Hardest" );	PORT_DIPNAME( 0x000c,	0x0000, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(		0x0000, "100K, every 400K" );	PORT_DIPSETTING(		0x0004, "100K" );	PORT_DIPSETTING(		0x0008, "200K" );	PORT_DIPSETTING(		0x000c, "None" );	PORT_DIPNAME( 0x0030,	0x0020, DEF_STR( "Lives") );
		PORT_DIPSETTING(		0x0030, "1" );	PORT_DIPSETTING(		0x0000, "2" );	PORT_DIPSETTING(		0x0020, "3" );	PORT_DIPSETTING(		0x0010, "4" );	PORT_BITX(	  0x0040,	0x0000, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPSETTING(		0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "On") );
		PORT_DIPNAME( 0x0080,	0x0000, "Allow Continue" );	PORT_DIPSETTING(		0x0080, DEF_STR( "No") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "Yes") );
		PORT_BIT( 0xff00, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START(); 		/* (6) Territory Jumper block */
		PORT_DIPNAME( 0x000f,	0x000a, "Territory" );	PORT_DIPSETTING(		0x000a, "Europe" );	PORT_DIPSETTING(		0x0009, "USA" );	PORT_DIPSETTING(		0x0000, "Japan" );	PORT_DIPSETTING(		0x0003, "Korea" );	PORT_DIPSETTING(		0x0004, "Hong Kong" );	PORT_DIPSETTING(		0x0007, "Taiwan" );	PORT_DIPSETTING(		0x0006, "South East Asia" );	PORT_DIPSETTING(		0x0002, "Europe, USA (Atari License); )
		PORT_DIPSETTING(		0x0001, "USA, Europe (Atari License); )
		PORT_BIT( 0xfff0, IP_ACTIVE_HIGH, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	/* Added by Yochizo 2000/08/16 */
	static InputPortPtr input_ports_tatsujn2 = new InputPortPtr(){ public void handler() { 
	PORT_START(); 		/* (0) VBlank */
		PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_VBLANK );	PORT_BIT( 0xfffe, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER1, IPT_UNKNOWN )
	
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER2, IPT_UNKNOWN )
	
		TOAPLAN2_SYSTEM_INPUTS
	
		PORT_START(); 		/* (4) DSWA */
		PORT_DIPNAME( 0x0002,	0x0000, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0002, DEF_STR( "On") );
		PORT_DIPNAME( 0x0004,	0x0000, "Test Switch" );	PORT_DIPSETTING(		0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0004, DEF_STR( "On") );
		PORT_DIPNAME( 0x0008,	0x0000, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(		0x0008, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0030,	0x0000, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(		0x0020, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(		0x0030, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(		0x0010, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x00c0,	0x0000, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(		0x0080, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(		0x00c0, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "1C_2C") );
		PORT_BIT( 0xff00, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START(); 		/* (5) DSWB */
		PORT_DIPNAME( 0x0003,	0x0000, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(		0x0001, "Easy" );	PORT_DIPSETTING(		0x0000, "Medium" );	PORT_DIPSETTING(		0x0002, "Hard" );	PORT_DIPSETTING(		0x0003, "Hardest" );	PORT_DIPNAME( 0x000c,	0x0000, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(		0x0000, "70K, and 200K" );	PORT_DIPSETTING(		0x0004, "100K, and 250K" );	PORT_DIPSETTING(		0x0008, "100K" );	PORT_DIPSETTING(		0x000c, "200K" );	PORT_DIPNAME( 0x0030,	0x0000, DEF_STR( "Lives") );
		PORT_DIPSETTING(		0x0030, "2" );	PORT_DIPSETTING(		0x0020, "4" );	PORT_DIPSETTING(		0x0000, "3" );	PORT_DIPSETTING(		0x0010, "5" );	PORT_BITX(	  0x0040,	0x0000, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPSETTING(		0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "On") );
		PORT_DIPNAME( 0x0080,	0x0000, "Allow Continue" );	PORT_DIPSETTING(		0x0080, DEF_STR( "No") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "Yes") );
	
		PORT_START(); 		/* (6) Territory Jumper block */
		PORT_DIPNAME( 0x07,	0x02, "Territory" );	PORT_DIPSETTING(	0x02, "Europe" );	PORT_DIPSETTING(	0x01, "USA" );	PORT_DIPSETTING(	0x00, "Japan" );	PORT_DIPSETTING(	0x03, "Hong Kong" );	PORT_DIPSETTING(	0x05, "Taiwan" );	PORT_DIPSETTING(	0x06, "Asia" );	PORT_DIPSETTING(	0x04, "Korea" );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_pipibibs = new InputPortPtr(){ public void handler() { 
	PORT_START(); 		/* (0) VBlank */
		PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_VBLANK );	PORT_BIT( 0xfffe, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER1, IPT_UNKNOWN )
	
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER2, IPT_UNKNOWN )
	
		TOAPLAN2_SYSTEM_INPUTS
	
		TOAPLAN2_DSW_A
	
		PORT_START(); 		/* (5) DSWB */
		PORT_DIPNAME( 0x03,	0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x01, "Easy" );	PORT_DIPSETTING(	0x00, "Medium" );	PORT_DIPSETTING(	0x02, "Hard" );	PORT_DIPSETTING(	0x03, "Hardest" );	PORT_DIPNAME( 0x0c,	0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(	0x04, "150K, every 200K" );	PORT_DIPSETTING(	0x00, "200K, every 300K" );	PORT_DIPSETTING(	0x08, "200K" );	PORT_DIPSETTING(	0x0c, "None" );	PORT_DIPNAME( 0x30,	0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x30, "1" );	PORT_DIPSETTING(	0x20, "2" );	PORT_DIPSETTING(	0x00, "3" );	PORT_DIPSETTING(	0x10, "5" );	PORT_BITX(	  0x40,	0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80,	0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	
		PORT_START(); 		/* (6) Territory Jumper block */
		PORT_DIPNAME( 0x07,	0x06, "Territory" );	PORT_DIPSETTING(	0x06, "Europe" );	PORT_DIPSETTING(	0x04, "USA" );	PORT_DIPSETTING(	0x00, "Japan" );	PORT_DIPSETTING(	0x02, "Hong Kong" );	PORT_DIPSETTING(	0x03, "Taiwan" );	PORT_DIPSETTING(	0x01, "Asia" );	PORT_DIPSETTING(	0x07, "Europe (Nova Apparate GMBH & Co); )
		PORT_DIPSETTING(	0x05, "USA (Romstar); )
		PORT_BIT( 0xf8, IP_ACTIVE_HIGH, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_whoopee = new InputPortPtr(){ public void handler() { 
	PORT_START(); 		/* (0) VBlank */
		PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_VBLANK );	PORT_BIT( 0xfffe, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER1, IPT_UNKNOWN )
	
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER2, IPT_UNKNOWN )
	
		TOAPLAN2_SYSTEM_INPUTS
	
		PORT_START(); 		/* (4) DSWA */
		PORT_DIPNAME( 0x01,	0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02,	0x00, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x02, DEF_STR( "On") );
		PORT_SERVICE( 0x04,	IP_ACTIVE_HIGH );	/* Service Mode */
		PORT_DIPNAME( 0x08,	0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30,	0x00, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(	0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x30, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(	0x10, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0xc0,	0x00, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(	0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0xc0, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(	0x40, DEF_STR( "1C_2C") );
	/*	European territories coin setups
		PORT_DIPNAME( 0x30,	0x00, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(	0x30, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x20, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0xc0,	0x00, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x40, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x80, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0xc0, DEF_STR( "1C_6C") );
	*/
	
		PORT_START(); 		/* (5) DSWB */
		PORT_DIPNAME( 0x03,	0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x01, "Easy" );	PORT_DIPSETTING(	0x00, "Medium" );	PORT_DIPSETTING(	0x02, "Hard" );	PORT_DIPSETTING(	0x03, "Hardest" );	PORT_DIPNAME( 0x0c,	0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(	0x04, "150K, every 200K" );	PORT_DIPSETTING(	0x00, "200K, every 300K" );	PORT_DIPSETTING(	0x08, "200K" );	PORT_DIPSETTING(	0x0c, "None" );	PORT_DIPNAME( 0x30,	0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x30, "1" );	PORT_DIPSETTING(	0x20, "2" );	PORT_DIPSETTING(	0x00, "3" );	PORT_DIPSETTING(	0x10, "5" );	PORT_BITX(	  0x40,	0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80,	0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	
		PORT_START(); 		/* (6) Territory Jumper block */
		PORT_DIPNAME( 0x07,	0x06, "Territory" );	PORT_DIPSETTING(	0x06, "Europe" );	PORT_DIPSETTING(	0x04, "USA" );	PORT_DIPSETTING(	0x00, "Japan" );	PORT_DIPSETTING(	0x02, "Hong Kong" );	PORT_DIPSETTING(	0x03, "Taiwan" );	PORT_DIPSETTING(	0x01, "Asia" );	PORT_DIPSETTING(	0x07, "Europe (Nova Apparate GMBH & Co); )
		PORT_DIPSETTING(	0x05, "USA (Romstar); )
		PORT_BIT( 0xf8, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* bit 0x10 sound ready */
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_pipibibi = new InputPortPtr(){ public void handler() { 
	PORT_START(); 		/* (0) VBlank */
	//	PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_VBLANK );	/* This video HW */
	//	PORT_BIT( 0xfffe, IP_ACTIVE_HIGH, IPT_UNKNOWN );	/* doesnt wait for VBlank */
	
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER1, IPT_UNKNOWN )
	
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER2, IPT_UNKNOWN )
	
		TOAPLAN2_SYSTEM_INPUTS
	
		PORT_START(); 		/* (4) DSWA */
		PORT_DIPNAME( 0x01,	0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x01, DEF_STR( "On") );
	//	PORT_DIPNAME( 0x02,	0x00, DEF_STR( "Flip_Screen") );	/* This video HW	*/
	//	PORT_DIPSETTING(	0x00, DEF_STR( "Off") );			/*	doesn't support	*/
	//	PORT_DIPSETTING(	0x02, DEF_STR( "On") );			/*	flip screen		*/
		PORT_SERVICE( 0x04,	IP_ACTIVE_HIGH );	/* Service Mode */
		PORT_DIPNAME( 0x08,	0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30,	0x00, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(	0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x30, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(	0x10, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0xc0,	0x00, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(	0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0xc0, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(	0x40, DEF_STR( "1C_2C") );
	/*	European territories coin setups
		PORT_DIPNAME( 0x30,	0x00, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(	0x30, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x20, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0xc0,	0x00, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x40, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x80, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0xc0, DEF_STR( "1C_6C") );
	*/
	
		PORT_START(); 		/* (5) DSWB */
		PORT_DIPNAME( 0x03,	0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x01, "Easy" );	PORT_DIPSETTING(	0x00, "Medium" );	PORT_DIPSETTING(	0x02, "Hard" );	PORT_DIPSETTING(	0x03, "Hardest" );	PORT_DIPNAME( 0x0c,	0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(	0x04, "150K, every 200K" );	PORT_DIPSETTING(	0x00, "200K, every 300K" );	PORT_DIPSETTING(	0x08, "200K" );	PORT_DIPSETTING(	0x0c, "None" );	PORT_DIPNAME( 0x30,	0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x30, "1" );	PORT_DIPSETTING(	0x20, "2" );	PORT_DIPSETTING(	0x00, "3" );	PORT_DIPSETTING(	0x10, "5" );	PORT_BITX(	  0x40,	0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80,	0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	
		PORT_START(); 		/* (6) Territory Jumper block */
		PORT_DIPNAME( 0x07,	0x05, "Territory" );	PORT_DIPSETTING(	0x07, "World (Ryouta Kikaku); )
		PORT_DIPSETTING(	0x00, "Japan (Ryouta Kikaku); )
		PORT_DIPSETTING(	0x02, "World" );	PORT_DIPSETTING(	0x05, "Europe" );	PORT_DIPSETTING(	0x04, "USA" );	PORT_DIPSETTING(	0x01, "Hong Kong (Honest Trading Co." );	PORT_DIPSETTING(	0x06, "Spain & Portugal (APM Electronics SA); )
	//	PORT_DIPSETTING(	0x03, "World" );	PORT_BIT( 0xf8, IP_ACTIVE_HIGH, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_grindstm = new InputPortPtr(){ public void handler() { 
	PORT_START(); 		/* (0) VBlank */
		PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_VBLANK );	PORT_BIT( 0xfffe, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER1, IPT_UNKNOWN )
	
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER2, IPT_UNKNOWN )
	
		TOAPLAN2_SYSTEM_INPUTS
	
		PORT_START(); 		/* (4) DSWA */
		PORT_DIPNAME( 0x0001,	0x0000, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "Upright") );
		PORT_DIPSETTING(		0x0001, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x0002,	0x0000, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0002, DEF_STR( "On") );
		PORT_SERVICE( 0x0004,	IP_ACTIVE_HIGH );	/* Service Mode */
		PORT_DIPNAME( 0x0008,	0x0000, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(		0x0008, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0030,	0x0000, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(		0x0030, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(		0x0020, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(		0x0010, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0x00c0,	0x0000, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(		0x0080, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(		0x00c0, DEF_STR( "1C_6C") );
	/*	Non-European territories coin setups
		PORT_DIPNAME( 0x0030,	0x0000, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(		0x0020, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(		0x0030, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(		0x0010, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x00c0,	0x0000, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(		0x0080, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(		0x00c0, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "1C_2C") );
	*/
		PORT_BIT( 0xff00, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START(); 		/* (5) DSWB */
		PORT_DIPNAME( 0x0003,	0x0000, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(		0x0001, "Easy" );	PORT_DIPSETTING(		0x0000, "Medium" );	PORT_DIPSETTING(		0x0002, "Hard" );	PORT_DIPSETTING(		0x0003, "Hardest" );	PORT_DIPNAME( 0x000c,	0x0000, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(		0x0008, "200K" );	PORT_DIPSETTING(		0x0004, "300K, then every 800K" );	PORT_DIPSETTING(		0x0000, "300K, and 800K" );	PORT_DIPSETTING(		0x000c, "None" );	PORT_DIPNAME( 0x0030,	0x0000, DEF_STR( "Lives") );
		PORT_DIPSETTING(		0x0030, "1" );	PORT_DIPSETTING(		0x0020, "2" );	PORT_DIPSETTING(		0x0000, "3" );	PORT_DIPSETTING(		0x0010, "5" );	PORT_BITX(	  0x0040,	0x0000, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPSETTING(		0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "On") );
		PORT_DIPNAME( 0x0080,	0x0000, "Allow Continue" );	PORT_DIPSETTING(		0x0080, DEF_STR( "No") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "Yes") );
	
		PORT_START(); 		/* (6) Territory Jumper block */
		PORT_DIPNAME( 0x0f,	0x08, "Territory" );	PORT_DIPSETTING(	0x08, "Europe" );	PORT_DIPSETTING(	0x0b, "USA" );	PORT_DIPSETTING(	0x01, "Korea" );	PORT_DIPSETTING(	0x03, "Hong Kong" );	PORT_DIPSETTING(	0x05, "Taiwan" );	PORT_DIPSETTING(	0x07, "South East Asia" );	PORT_DIPSETTING(	0x0a, "USA (American Sammy Corporation License); )
		PORT_DIPSETTING(	0x00, "Korea (Unite Trading License); )
		PORT_DIPSETTING(	0x02, "Hong Kong (Charterfield License); )
		PORT_DIPSETTING(	0x04, "Taiwan (Anomoto International Inc License); )
		PORT_DIPSETTING(	0x06, "South East Asia (Charterfield License); )
	/*	Duplicate settings
		PORT_DIPSETTING(	0x09, "Europe" );	PORT_DIPSETTING(	0x0d, "USA" );	PORT_DIPSETTING(	0x0e, "Korea" );	PORT_DIPSETTING(	0x0f, "Korea" );	PORT_DIPSETTING(	0x0c, "USA (American Sammy Corporation License); )
	*/
		PORT_BIT( 0xf0, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* bit 0x10 sound ready */
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_vfive = new InputPortPtr(){ public void handler() { 
	PORT_START(); 		/* (0) VBlank */
		PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_VBLANK );	PORT_BIT( 0xfffe, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER1, IPT_UNKNOWN )
	
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER2, IPT_UNKNOWN )
	
		TOAPLAN2_SYSTEM_INPUTS
	
		PORT_START(); 		/* (4) DSWA */
		PORT_DIPNAME( 0x0001,	0x0000, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "Upright") );
		PORT_DIPSETTING(		0x0001, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x0002,	0x0000, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0002, DEF_STR( "On") );
		PORT_SERVICE( 0x0004,	IP_ACTIVE_HIGH );	/* Service Mode */
		PORT_DIPNAME( 0x0008,	0x0000, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(		0x0008, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0030,	0x0000, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(		0x0020, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(		0x0030, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(		0x0010, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x00c0,	0x0000, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(		0x0080, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(		0x00c0, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "1C_2C") );
		PORT_BIT( 0xff00, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START(); 		/* (5) DSWB */
		PORT_DIPNAME( 0x0003,	0x0000, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(		0x0001, "Easy" );	PORT_DIPSETTING(		0x0000, "Medium" );	PORT_DIPSETTING(		0x0002, "Hard" );	PORT_DIPSETTING(		0x0003, "Hardest" );	PORT_DIPNAME( 0x000c,	0x0000, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(		0x0004, "300K, then every 800K" );	PORT_DIPSETTING(		0x0000, "300K, and 800K" );	PORT_DIPSETTING(		0x0008, "200K" );	PORT_DIPSETTING(		0x000c, "None" );	PORT_DIPNAME( 0x0030,	0x0000, DEF_STR( "Lives") );
		PORT_DIPSETTING(		0x0030, "1" );	PORT_DIPSETTING(		0x0020, "2" );	PORT_DIPSETTING(		0x0000, "3" );	PORT_DIPSETTING(		0x0010, "5" );	PORT_BITX(	  0x0040,	0x0000, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPSETTING(		0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "On") );
		PORT_DIPNAME( 0x0080,	0x0000, "Allow Continue" );	PORT_DIPSETTING(		0x0080, DEF_STR( "No") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "Yes") );
	
		PORT_START(); 		/* (6) Territory Jumper block */
		/* Territory is forced to Japan in this set. */
		PORT_BIT( 0xff, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* bit 0x10 sound ready */
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_batsugun = new InputPortPtr(){ public void handler() { 
	PORT_START(); 		/* (0) VBlank */
		PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_VBLANK );	PORT_BIT( 0xfffe, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER1, IPT_UNKNOWN )
	
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER2, IPT_UNKNOWN )
	
		TOAPLAN2_SYSTEM_INPUTS
	
		PORT_START(); 		/* (4) DSWA */
		PORT_DIPNAME( 0x0001,	0x0000, "Continue Mode" );	PORT_DIPSETTING(		0x0000, "Normal" );	PORT_DIPSETTING(		0x0001, "Discount" );	PORT_DIPNAME( 0x0002,	0x0000, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0002, DEF_STR( "On") );
		PORT_SERVICE( 0x0004,	IP_ACTIVE_HIGH );	/* Service Mode */
		PORT_DIPNAME( 0x0008,	0x0000, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(		0x0008, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0030,	0x0000, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(		0x0030, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(		0x0020, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(		0x0010, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0x00c0,	0x0000, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(		0x0080, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(		0x00c0, DEF_STR( "1C_6C") );
	/*	Non-European territories coin setups
		PORT_DIPNAME( 0x0030,	0x0000, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(		0x0020, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(		0x0030, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(		0x0010, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x00c0,	0x0000, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(		0x0080, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(		0x00c0, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "1C_2C") );
	*/
		PORT_BIT( 0xff00, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START(); 		/* (5) DSWB */
		PORT_DIPNAME( 0x0003,	0x0000, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(		0x0001, "Easy" );	PORT_DIPSETTING(		0x0000, "Medium" );	PORT_DIPSETTING(		0x0002, "Hard" );	PORT_DIPSETTING(		0x0003, "Hardest" );	PORT_DIPNAME( 0x000c,	0x0000, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(		0x0004, "500K, then every 600K" );	PORT_DIPSETTING(		0x0000, "1 Million" );	PORT_DIPSETTING(		0x0008, "1.5 Million" );	PORT_DIPSETTING(		0x000c, "None" );	PORT_DIPNAME( 0x0030,	0x0000, DEF_STR( "Lives") );
		PORT_DIPSETTING(		0x0030, "1" );	PORT_DIPSETTING(		0x0020, "2" );	PORT_DIPSETTING(		0x0000, "3" );	PORT_DIPSETTING(		0x0010, "5" );	PORT_BITX(	  0x0040,	0x0000, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPSETTING(		0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "On") );
		PORT_DIPNAME( 0x0080,	0x0000, "Allow Continue" );	PORT_DIPSETTING(		0x0080, DEF_STR( "No") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "Yes") );
	
		PORT_START(); 		/* (6) Territory Jumper block */
		PORT_DIPNAME( 0x000f,	0x0009, "Territory" );	PORT_DIPSETTING(		0x0009, "Europe" );	PORT_DIPSETTING(		0x000b, "USA" );	PORT_DIPSETTING(		0x000e, "Japan" );//	PORT_DIPSETTING(		0x000f, "Japan" );	PORT_DIPSETTING(		0x0001, "Korea" );	PORT_DIPSETTING(		0x0003, "Hong Kong" );	PORT_DIPSETTING(		0x0005, "Taiwan" );	PORT_DIPSETTING(		0x0007, "South East Asia" );	PORT_DIPSETTING(		0x0008, "Europe (Taito Corp License); )
		PORT_DIPSETTING(		0x000a, "USA (Taito Corp License); )
		PORT_DIPSETTING(		0x000c, "Japan (Taito Corp License); )
	//	PORT_DIPSETTING(		0x000d, "Japan (Taito Corp License); )
		PORT_DIPSETTING(		0x0000, "Korea (Unite Trading License); )
		PORT_DIPSETTING(		0x0002, "Hong Kong (Taito Corp License); )
		PORT_DIPSETTING(		0x0004, "Taiwan (Taito Corp License); )
		PORT_DIPSETTING(		0x0006, "South East Asia (Taito Corp License); )
		PORT_BIT( 0xfff0, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* bit 0x10 sound ready */
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_snowbro2 = new InputPortPtr(){ public void handler() { 
	PORT_START(); 		/* (0) VBlank */
		PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_VBLANK );	PORT_BIT( 0xfffe, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER1, IPT_UNKNOWN )
	
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER2, IPT_UNKNOWN )
	
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER3, IPT_START3 )
	
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER4, IPT_START4 )
	
		TOAPLAN2_SYSTEM_INPUTS
	
		PORT_START(); 		/* (6) DSWA */
		PORT_DIPNAME( 0x0001,	0x0000, "Continue Mode" );	PORT_DIPSETTING(		0x0000, "Normal" );	PORT_DIPSETTING(		0x0001, "Discount" );	PORT_DIPNAME( 0x0002,	0x0000, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0002, DEF_STR( "On") );
		PORT_SERVICE( 0x0004,	IP_ACTIVE_HIGH );	/* Service Mode */
		PORT_DIPNAME( 0x0008,	0x0000, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(		0x0008, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0030,	0x0000, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(		0x0020, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(		0x0030, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(		0x0010, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x00c0,	0x0000, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(		0x0080, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(		0x00c0, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "1C_2C") );
	/*	The following are listed in service mode for European territory,
		but are not actually used in game play.
		PORT_DIPNAME( 0x0030,	0x0000, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(		0x0030, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(		0x0020, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(		0x0010, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0x00c0,	0x0000, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(		0x0080, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(		0x00c0, DEF_STR( "1C_6C") );
	*/
		PORT_BIT( 0xff00, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START(); 		/* (7) DSWB */
		PORT_DIPNAME( 0x0003,	0x0000, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(		0x0001, "Easy" );	PORT_DIPSETTING(		0x0000, "Medium" );	PORT_DIPSETTING(		0x0002, "Hard" );	PORT_DIPSETTING(		0x0003, "Hardest" );	PORT_DIPNAME( 0x000c,	0x0000, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(		0x0004, "100K, every 500K" );	PORT_DIPSETTING(		0x0000, "100K" );	PORT_DIPSETTING(		0x0008, "200K" );	PORT_DIPSETTING(		0x000c, "None" );	PORT_DIPNAME( 0x0030,	0x0000, DEF_STR( "Lives") );
		PORT_DIPSETTING(		0x0030, "1" );	PORT_DIPSETTING(		0x0020, "2" );	PORT_DIPSETTING(		0x0000, "3" );	PORT_DIPSETTING(		0x0010, "4" );	PORT_BITX(	  0x0040,	0x0000, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPSETTING(		0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "On") );
		PORT_DIPNAME( 0x0080,	0x0000, "Maximum Players" );	PORT_DIPSETTING(		0x0080, "2" );	PORT_DIPSETTING(		0x0000, "4" );	PORT_BIT( 0xff00, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START(); 		/* (8) Territory Jumper block */
		PORT_DIPNAME( 0x1c00,	0x0800, "Territory" );	PORT_DIPSETTING(		0x0800, "Europe" );	PORT_DIPSETTING(		0x0400, "USA" );	PORT_DIPSETTING(		0x0000, "Japan" );	PORT_DIPSETTING(		0x0c00, "Korea" );	PORT_DIPSETTING(		0x1000, "Hong Kong" );	PORT_DIPSETTING(		0x1400, "Taiwan" );	PORT_DIPSETTING(		0x1800, "South East Asia" );	PORT_DIPSETTING(		0x1c00, DEF_STR( "Unused") );
		PORT_DIPNAME( 0x2000,	0x0000, "Show All Rights Reserved" );	PORT_DIPSETTING(		0x0000, DEF_STR( "No") );
		PORT_DIPSETTING(		0x2000, DEF_STR( "Yes") );
		PORT_BIT( 0xc3ff, IP_ACTIVE_HIGH, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_mahoudai = new InputPortPtr(){ public void handler() { 
	PORT_START(); 		/* (0) VBlank */
		PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_VBLANK );	PORT_BIT( 0xfffe, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER1, IPT_UNKNOWN )
	
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER2, IPT_UNKNOWN )
	
		TOAPLAN2_SYSTEM_INPUTS
	
		PORT_START(); 		/* (4) DSWA */
		PORT_DIPNAME( 0x0001,	0x0000, DEF_STR( "Free_Play") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0001, DEF_STR( "On") );
		PORT_DIPNAME( 0x0002,	0x0000, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0002, DEF_STR( "On") );
		PORT_SERVICE( 0x0004,	IP_ACTIVE_HIGH );	/* Service Mode */
		PORT_DIPNAME( 0x0008,	0x0000, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(		0x0008, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0030,	0x0000, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(		0x0020, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(		0x0030, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(		0x0010, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x00c0,	0x0000, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(		0x0080, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(		0x00c0, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "1C_2C") );
		PORT_BIT( 0xff00, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START(); 		/* (5) DSWB */
		PORT_DIPNAME( 0x0003,	0x0000, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(		0x0001, "Easy" );	PORT_DIPSETTING(		0x0000, "Medium" );	PORT_DIPSETTING(		0x0002, "Hard" );	PORT_DIPSETTING(		0x0003, "Hardest" );	PORT_DIPNAME( 0x000c,	0x0000, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(		0x0004, "200K, 500K" );	PORT_DIPSETTING(		0x0000, "every 300K" );	PORT_DIPSETTING(		0x0008, "200K" );	PORT_DIPSETTING(		0x000c, "None" );	PORT_DIPNAME( 0x0030,	0x0000, DEF_STR( "Lives") );
		PORT_DIPSETTING(		0x0030, "1" );	PORT_DIPSETTING(		0x0020, "2" );	PORT_DIPSETTING(		0x0000, "3" );	PORT_DIPSETTING(		0x0010, "5" );	PORT_BITX(	  0x0040,	0x0000, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPSETTING(		0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "On") );
		PORT_DIPNAME( 0x0080,	0x0000, "Allow Continue" );	PORT_DIPSETTING(		0x0080, DEF_STR( "No") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "Yes") );
	
		PORT_START(); 		/* (6) Territory Jumper block */
		PORT_BIT( 0xffff, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* not used, it seems */
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_shippumd = new InputPortPtr(){ public void handler() { 
	PORT_START(); 		/* (0) VBlank */
		PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_VBLANK );	PORT_BIT( 0xfffe, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER1, IPT_UNKNOWN )
	
		TOAPLAN2_PLAYER_INPUT( IPF_PLAYER2, IPT_UNKNOWN )
	
		TOAPLAN2_SYSTEM_INPUTS
	
		PORT_START(); 		/* (4) DSWA */
		PORT_DIPNAME( 0x0001,	0x0000, DEF_STR( "Free_Play") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0001, DEF_STR( "On") );
		PORT_DIPNAME( 0x0002,	0x0000, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0002, DEF_STR( "On") );
		PORT_SERVICE( 0x0004,	IP_ACTIVE_HIGH );	/* Service Mode */
		PORT_DIPNAME( 0x0008,	0x0000, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(		0x0008, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0030,	0x0000, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(		0x0020, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(		0x0030, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(		0x0010, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x00c0,	0x0000, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(		0x0080, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(		0x00c0, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "1C_2C") );
		PORT_BIT( 0xff00, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START(); 		/* (5) DSWB */
		PORT_DIPNAME( 0x0003,	0x0000, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(		0x0001, "Easy" );	PORT_DIPSETTING(		0x0000, "Medium" );	PORT_DIPSETTING(		0x0002, "Hard" );	PORT_DIPSETTING(		0x0003, "Hardest" );	PORT_DIPNAME( 0x000c,	0x0000, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(		0x0004, "200K, 500K" );	PORT_DIPSETTING(		0x0000, "every 300K" );	PORT_DIPSETTING(		0x0008, "200K" );	PORT_DIPSETTING(		0x000c, "None" );	PORT_DIPNAME( 0x0030,	0x0000, DEF_STR( "Lives") );
		PORT_DIPSETTING(		0x0030, "1" );	PORT_DIPSETTING(		0x0020, "2" );	PORT_DIPSETTING(		0x0000, "3" );	PORT_DIPSETTING(		0x0010, "5" );	PORT_BITX(	  0x0040,	0x0000, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPSETTING(		0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(		0x0040, DEF_STR( "On") );
		PORT_DIPNAME( 0x0080,	0x0000, "Allow Continue" );	PORT_DIPSETTING(		0x0080, DEF_STR( "No") );
		PORT_DIPSETTING(		0x0000, DEF_STR( "Yes") );
	
		PORT_START(); 		/* (6) Territory Jumper block */
		/* title screen is wrong when set to other countries */
		PORT_DIPNAME( 0x000e,	0x0000, "Territory" );	PORT_DIPSETTING(		0x0004, "Europe" );	PORT_DIPSETTING(		0x0002, "USA" );	PORT_DIPSETTING(		0x0000, "Japan" );	PORT_DIPSETTING(		0x0006, "South East Asia" );	PORT_DIPSETTING(		0x0008, "China" );	PORT_DIPSETTING(		0x000a, "Korea" );	PORT_DIPSETTING(		0x000c, "Hong Kong" );	PORT_DIPSETTING(		0x000e, "Taiwan" );	PORT_BIT( 0xfff1, IP_ACTIVE_HIGH, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout tilelayout = new GfxLayout
	(
		16,16,	/* 16x16 */
		RGN_FRAC(1,2),	/* Number of tiles */
		4,		/* 4 bits per pixel */
		new int[] { RGN_FRAC(1,2)+8, RGN_FRAC(1,2), 8, 0 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,
			8*16+0, 8*16+1, 8*16+2, 8*16+3, 8*16+4, 8*16+5, 8*16+6, 8*16+7 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
			16*16, 17*16, 18*16, 19*16, 20*16, 21*16, 22*16, 23*16 },
		8*4*16
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		8,8,	/* 8x8 */
		RGN_FRAC(1,2),	/* Number of 8x8 sprites */
		4,		/* 4 bits per pixel */
		new int[] { RGN_FRAC(1,2)+8, RGN_FRAC(1,2), 8, 0 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		8*16
	);
	
	
	#ifdef LSB_FIRST
	static GfxLayout tatsujn2_textlayout = new GfxLayout
	(
		8,8,	/* 8x8 characters */
		1024,	/* 1024 characters */
		4,		/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 8, 12, 0, 4, 24, 28, 16, 20 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		8*32
	);
	#else
	static GfxLayout tatsujn2_textlayout = new GfxLayout
	(
		8,8,	/* 8x8 characters */
		1024,	/* 1024 characters */
		4,		/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 0, 4, 8, 12, 16, 20, 24, 28 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		8*32
	);
	#endif
	
	
	static GfxLayout raizing_textlayout = new GfxLayout
	(
		8,8,	/* 8x8 characters */
		1024,	/* 1024 characters */
		4,		/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 0, 4, 8, 12, 16, 20, 24, 28 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		8*32
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, tilelayout,   0, 128 ),
		new GfxDecodeInfo( REGION_GFX1, 0, spritelayout, 0,  64 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo gfxdecodeinfo_2[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, tilelayout,   0, 128 ),
		new GfxDecodeInfo( REGION_GFX1, 0, spritelayout, 0,  64 ),
		new GfxDecodeInfo( REGION_GFX2, 0, tilelayout,   0, 128 ),
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout, 0,  64 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	/* Added by Yochizo 2000/08/19 */
	static GfxDecodeInfo tatsujn2_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0,       tilelayout,   0, 128 ),
		new GfxDecodeInfo( REGION_GFX1, 0,       spritelayout, 0,  64 ),
		/* Tatsujin2 have extra-text tile data in CPU rom */
		new GfxDecodeInfo( REGION_CPU1, 0x40000, tatsujn2_textlayout,  0, 128 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo raizing_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, tilelayout,   0, 128 ),
		new GfxDecodeInfo( REGION_GFX1, 0, spritelayout, 0,  64 ),
		new GfxDecodeInfo( REGION_GFX2, 0, raizing_textlayout,   0, 128 ),		/* Extra-text layer */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	//  Not used yet
	//static GfxDecodeInfo raizing_gfxdecodeinfo_2[] =
	//{
	//	new GfxDecodeInfo( REGION_GFX1, 0, tilelayout,   0, 128 ),
	//	new GfxDecodeInfo( REGION_GFX1, 0, spritelayout, 0,  64 ),
	//	new GfxDecodeInfo( REGION_GFX2, 0, tilelayout,   0, 128 ),
	//	new GfxDecodeInfo( REGION_GFX2, 0, spritelayout, 0,  64 ),
	//	new GfxDecodeInfo( REGION_GFX3, 0, textlayout,   0, 128 ),		/* Extra-text layer */
	//	new GfxDecodeInfo( -1 ) /* end of array */
	//};
	
	
	static void irqhandler(int linestate)
	{
		cpu_set_irq_line(1,0,linestate);
	}
	
	static YM3812interface ym3812_interface = new YM3812interface
	(
		1,				/* 1 chip  */
		27000000/8,		/* 3.375MHz , 27MHz Oscillator */
		new WriteYmHandlerPtr[] { 100 },		/* volume */
		{ irqhandler },
	);
	
	static YM2151interface ym2151_interface = new YM2151interface
	(
		1,				/* 1 chip */
		27000000/8,		/* 3.375MHz , 27MHz Oscillator */
		new WriteYmHandlerPtr[] { YM3012_VOL(45,MIXER_PAN_LEFT,45,MIXER_PAN_RIGHT) },
		new WriteHandlerPtr[] { 0 }
	);
	
	static OKIM6295interface okim6295_interface = new OKIM6295interface
	(
		1,					/* 1 chip */
		new int[] { 22050 },			/* frequency (Hz). M6295 has 1MHz on its clock input */
		new int[] { REGION_SOUND1 },	/* memory region */
		new int[] { 47 }
	);
	
	/* Added by Yochizo */
	/* This is M6295 interface for Raizing games. Raizing games use lower sampling */
	/* frequency probably but I don't know real number.                            */
	static OKIM6295interface okim6295_raizing_interface = new OKIM6295interface
	(
		1,					/* 1 chip */
		new int[] { 11025 },			/* frequency (Hz). M6295 has 1MHz on its clock input */
		new int[] { REGION_SOUND1 },	/* memory region */
		new int[] { 47 }
	);
	
	
	static MachineDriver machine_driver_tekipaki = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				10000000,			/* 10MHz Oscillator */
				tekipaki_readmem,tekipaki_writemem,null,null,
				toaplan2_interrupt,1
			),
	#if HD64x180
			new MachineCPU(
				CPU_Z80,			/* HD647180 CPU actually */
				10000000,			/* 10MHz Oscillator */
				hd647180_readmem,hd647180_writemem,null,null,
				ignore_interrupt,0
			)
	#endif
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		init_toaplan2,
	
		/* video hardware */
		32*16, 32*16, new rectangle( 0, 319, 0, 239 ),
		gfxdecodeinfo,
		(64*16)+(64*16), (64*16)+(64*16),
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK, /* Sprites are buffered too */
		toaplan2_0_eof_callback,
		toaplan2_0_vh_start,
		toaplan2_0_vh_stop,
		toaplan2_0_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM3812,
				ym3812_interface
			),
		}
	);
	
	static MachineDriver machine_driver_ghox = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				10000000,			/* 10MHz Oscillator */
				ghox_readmem,ghox_writemem,null,null,
				toaplan2_interrupt,1
			),
	#if HD64x180
			new MachineCPU(
				CPU_Z80,			/* HD647180 CPU actually */
				10000000,			/* 10MHz Oscillator */
				hd647180_readmem,hd647180_writemem,null,null,
				ignore_interrupt,0
			)
	#endif
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		init_toaplan2,
	
		/* video hardware */
		32*16, 32*16, new rectangle( 0, 319, 0, 239 ),
		gfxdecodeinfo,
		(64*16)+(64*16), (64*16)+(64*16),
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK, /* Sprites are buffered too */
		toaplan2_0_eof_callback,
		toaplan2_0_vh_start,
		toaplan2_0_vh_stop,
		toaplan2_0_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			)
		}
	);
	
	static MachineDriver machine_driver_dogyuun = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				16000000,			/* 16MHz Oscillator */
				dogyuun_readmem,dogyuun_writemem,null,null,
				toaplan2_interrupt,1
			),
	#if Zx80
			new MachineCPU(
				CPU_Z80,			/* Z?80 type Toaplan marked CPU ??? */
				16000000,			/* 16MHz Oscillator */
				Zx80_readmem,Zx80_writemem,null,null,
				ignore_interrupt,0
			)
	#endif
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		init_toaplan3,
	
		/* video hardware */
		32*16, 32*16, new rectangle( 0, 319, 0, 239 ),
		gfxdecodeinfo_2,
		(64*16)+(64*16), (64*16)+(64*16),
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK, /* Sprites are buffered too */
		toaplan2_1_eof_callback,
		toaplan2_1_vh_start,
		toaplan2_1_vh_stop,
		toaplan2_1_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			),
			new MachineSound(
				SOUND_OKIM6295,
				okim6295_interface
			)
		}
	);
	
	static MachineDriver machine_driver_kbash = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				16000000,			/* 16MHz Oscillator */
				kbash_readmem,kbash_writemem,null,null,
				toaplan2_interrupt,1
			),
	#if Zx80
			new MachineCPU(
				CPU_Z80,			/* Z?80 type Toaplan marked CPU ??? */
				16000000,			/* 16MHz Oscillator */
				Zx80_readmem,Zx80_writemem,null,null,
				ignore_interrupt,0
			)
	#endif
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		init_toaplan2,
	
		/* video hardware */
		32*16, 32*16, new rectangle( 0, 319, 0, 239 ),
		gfxdecodeinfo,
		(64*16)+(64*16), (64*16)+(64*16),
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK, /* Sprites are buffered too */
		toaplan2_0_eof_callback,
		toaplan2_0_vh_start,
		toaplan2_0_vh_stop,
		toaplan2_0_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			),
			new MachineSound(
				SOUND_OKIM6295,
				okim6295_interface
			)
		}
	);
	
	/* Fixed by Yochizo 2000/08/16 */
	static MachineDriver machine_driver_tatsujn2 = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				16000000,			/* 16MHz Oscillator */
				tatsujn2_readmem,tatsujn2_writemem,null,null,
				tatsujn2_interrupt,1
			),
	#if Zx80
			new MachineCPU(
				CPU_Z80,			/* Z?80 type Toaplan marked CPU ??? */
				16000000,			/* 16MHz Oscillator */
				Zx80_readmem,Zx80_writemem,null,null,
				ignore_interrupt,0
			)
	#endif
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		init_tatsujn2,
	
		/* video hardware */
		32*16, 32*16, new rectangle( 0, 319, 0, 239 ),
		tatsujn2_gfxdecodeinfo,
		(64*16)+(64*16), (64*16)+(64*16),
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK, /* Sprites are buffered too */
		toaplan2_0_eof_callback,
		raizing_0_vh_start,
		toaplan2_0_vh_stop,
		raizing_0_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			),
			new MachineSound(
				SOUND_OKIM6295,
				okim6295_interface
			)
		}
	);
	
	static MachineDriver machine_driver_pipibibs = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				10000000,			/* 10MHz Oscillator */
				pipibibs_readmem,pipibibs_writemem,null,null,
				toaplan2_interrupt,1
			),
			new MachineCPU(
				CPU_Z80,
				27000000/8,			/* ??? 3.37MHz , 27MHz Oscillator */
				sound_readmem,sound_writemem,0,null,
				ignore_interrupt,0
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		10,
		init_pipibibs,
	
		/* video hardware */
		32*16, 32*16, new rectangle( 0, 319, 0, 239 ),
		gfxdecodeinfo,
		(128*16), (128*16),
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK, /* Sprites are buffered too */
		toaplan2_0_eof_callback,
		toaplan2_0_vh_start,
		toaplan2_0_vh_stop,
		toaplan2_0_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM3812,
				ym3812_interface
			),
		}
	);
	
	static MachineDriver machine_driver_whoopee = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				10000000,			/* 10MHz Oscillator */
				tekipaki_readmem,tekipaki_writemem,null,null,
				toaplan2_interrupt,1
			),
			new MachineCPU(
				CPU_Z80,			/* This should be a HD647180 */
				27000000/8,			/* Change this to 10MHz when HD647180 gets dumped. 10MHz Oscillator */
				sound_readmem,sound_writemem,null,null,
				ignore_interrupt,0
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		10,
		init_pipibibs,
	
		/* video hardware */
		32*16, 32*16, new rectangle( 0, 319, 0, 239 ),
		gfxdecodeinfo,
		(128*16), (128*16),
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK, /* Sprites are buffered too */
		toaplan2_0_eof_callback,
		toaplan2_0_vh_start,
		toaplan2_0_vh_stop,
		toaplan2_0_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM3812,
				ym3812_interface
			),
		}
	);
	
	static MachineDriver machine_driver_pipibibi = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				10000000,			/* 10MHz Oscillator */
				pipibibi_readmem,pipibibi_writemem,null,null,
				toaplan2_interrupt,1
			),
			new MachineCPU(
				CPU_Z80,
				27000000/8,			/* ??? 3.37MHz */
				sound_readmem,sound_writemem,null,null,
				ignore_interrupt,0
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		10,
		init_pipibibs,
	
		/* video hardware */
		32*16, 32*16, new rectangle( 0, 319, 0, 239 ),
		gfxdecodeinfo,
		(128*16), (128*16),
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK, /* Sprites are buffered too */
		toaplan2_0_eof_callback,
		toaplan2_0_vh_start,
		toaplan2_0_vh_stop,
		toaplan2_0_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM3812,
				ym3812_interface
			),
		}
	);
	
	static MachineDriver machine_driver_fixeight = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				16000000,			/* 16MHz Oscillator */
				fixeight_readmem,fixeight_writemem,null,null,
				toaplan2_interrupt,1
			),
	#if Zx80
			new MachineCPU(
				CPU_Z80,			/* Z?80 type Toaplan marked CPU ??? */
				16000000,			/* 16MHz Oscillator */
				Zx80_readmem,Zx80_writemem,null,null,
				ignore_interrupt,0
			)
	#endif
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		init_toaplan3,
	
		/* video hardware */
		32*16, 32*16, new rectangle( 0, 319, 0, 239 ),
		gfxdecodeinfo,
		(64*16)+(64*16), (64*16)+(64*16),
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK, /* Sprites are buffered too */
		toaplan2_0_eof_callback,
		toaplan2_0_vh_start,
		toaplan2_0_vh_stop,
		toaplan2_0_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			)
		}
	);
	
	static MachineDriver machine_driver_vfive = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				10000000,			/* 10MHz Oscillator */
				vfive_readmem,vfive_writemem,null,null,
				toaplan2_interrupt,1
			),
	#if Zx80
			new MachineCPU(
				CPU_Z80,			/* Z?80 type Toaplan marked CPU ??? */
				10000000,			/* 10MHz Oscillator */
				Zx80_readmem,Zx80_writemem,null,null,
				ignore_interrupt,0
			)
	#endif
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		init_toaplan3,
	
		/* video hardware */
		32*16, 32*16, new rectangle( 0, 319, 0, 239 ),
		gfxdecodeinfo,
		(64*16)+(64*16), (64*16)+(64*16),
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK, /* Sprites are buffered too */
		toaplan2_0_eof_callback,
		toaplan2_0_vh_start,
		toaplan2_0_vh_stop,
		toaplan2_0_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			)
		}
	);
	
	static MachineDriver machine_driver_batsugun = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				32000000/2,			/* 16MHz , 32MHz Oscillator */
				batsugun_readmem,batsugun_writemem,0,null,
				toaplan2_interrupt,1
			),
	#if Zx80
			new MachineCPU(
				CPU_Z80,			/* Z?80 type Toaplan marked CPU ??? */
				32000000/2,			/* 16MHz , 32MHz Oscillator */
				Zx80_readmem,Zx80_writemem,0,null,
				ignore_interrupt,0
			)
	#endif
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		init_toaplan3,
	
		/* video hardware */
		32*16, 32*16, new rectangle( 0, 319, 0, 239 ),
		gfxdecodeinfo_2,
		(64*16)+(64*16), (64*16)+(64*16),
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK, /* Sprites are buffered too */
		toaplan2_1_eof_callback,
		toaplan2_1_vh_start,
		toaplan2_1_vh_stop,
		batsugun_1_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			),
			new MachineSound(
				SOUND_OKIM6295,
				okim6295_interface
			)
		}
	);
	
	static MachineDriver machine_driver_snowbro2 = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				16000000,
				snowbro2_readmem,snowbro2_writemem,null,null,
				toaplan2_interrupt,1
			),
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		init_toaplan2,
	
		/* video hardware */
		32*16, 32*16, new rectangle( 0, 319, 0, 239 ),
		gfxdecodeinfo,
		(64*16)+(64*16), (64*16)+(64*16),
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK, /* Sprites are buffered too */
		toaplan2_0_eof_callback,
		toaplan2_0_vh_start,
		toaplan2_0_vh_stop,
		toaplan2_0_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			),
			new MachineSound(
				SOUND_OKIM6295,
				okim6295_interface
			)
		}
	);
	
	static MachineDriver machine_driver_mahoudai = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				16000000,
				mahoudai_readmem,mahoudai_writemem,null,null,
				toaplan2_interrupt,1
			),
			new MachineCPU(
				CPU_Z80,
				4000000,		/* ??? */
				raizing_sound_readmem,raizing_sound_writemem,null,null,
				ignore_interrupt,0
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		10,
		null,
	
		/* video hardware */
		32*16, 32*16, new rectangle( 0, 319, 0, 239 ),
		raizing_gfxdecodeinfo,
		(128*16), (128*16),
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK, /* Sprites are buffered too */
		toaplan2_0_eof_callback,
		raizing_0_vh_start,
		toaplan2_0_vh_stop,
		raizing_0_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			),
			new MachineSound(
				SOUND_OKIM6295,
				okim6295_raizing_interface
			)
		}
	);
	
	static MachineDriver machine_driver_shippumd = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				16000000,
				shippumd_readmem,shippumd_writemem,null,null,
				toaplan2_interrupt,1
			),
			new MachineCPU(
				CPU_Z80,
				4000000,		/* ??? */
				raizing_sound_readmem,raizing_sound_writemem,null,null,
				ignore_interrupt,0
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		10,
		null,
	
		/* video hardware */
		32*16, 32*16, new rectangle( 0, 319, 0, 239 ),
		raizing_gfxdecodeinfo,
		(128*16), (128*16),
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK, /* Sprites are buffered too */
		toaplan2_0_eof_callback,
		raizing_0_vh_start,
		toaplan2_0_vh_stop,
		raizing_0_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			),
			new MachineSound(
				SOUND_OKIM6295,
				okim6295_raizing_interface
			)
		}
	);
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	/* -------------------------- Toaplan games ------------------------- */
	static RomLoadPtr rom_tekipaki = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x020000, REGION_CPU1 );		/* Main 68K code */
		ROM_LOAD_EVEN( "tp020-1.bin", 0x000000, 0x010000, 0xd8420bd5 );	ROM_LOAD_ODD ( "tp020-2.bin", 0x000000, 0x010000, 0x7222de8e );
	#if HD64x180
		ROM_REGION( 0x10000, REGION_CPU2 );		/* Sound 647180 code */
		/* sound CPU is a HD647180 (Z180) with internal ROM - not yet supported */
		ROM_LOAD( "hd647180.020", 0x00000, 0x08000, 0x00000000 );#endif
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tp020-4.bin", 0x000000, 0x080000, 0x3ebbe41e );	ROM_LOAD( "tp020-3.bin", 0x080000, 0x080000, 0x2d5e2201 );ROM_END(); }}; 
	
	static RomLoadPtr rom_ghox = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x040000, REGION_CPU1 );		/* Main 68K code */
		ROM_LOAD_EVEN( "tp021-01.u10", 0x000000, 0x020000, 0x9e56ac67 );	ROM_LOAD_ODD ( "tp021-02.u11", 0x000000, 0x020000, 0x15cac60f );
	#if HD64x180
		ROM_REGION( 0x10000, REGION_CPU2 );		/* Sound 647180 code */
		/* sound CPU is a HD647180 (Z180) with internal ROM - not yet supported */
		ROM_LOAD( "hd647180.021", 0x00000, 0x08000, 0x00000000 );#endif
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tp021-03.u36", 0x000000, 0x080000, 0xa15d8e9d );	ROM_LOAD( "tp021-04.u37", 0x080000, 0x080000, 0x26ed1c9a );ROM_END(); }}; 
	
	static RomLoadPtr rom_dogyuun = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x080000, REGION_CPU1 );		/* Main 68K code */
		ROM_LOAD_WIDE( "tp022_1.r16", 0x000000, 0x080000, 0x72f18907 );
	#if Zx80
		ROM_REGION( 0x10000, REGION_CPU2 );		/* Secondary CPU code */
		/* Secondary CPU is a Toaplan marked chip ??? */
	//	ROM_LOAD( "tp022.mcu", 0x00000, 0x08000, 0x00000000 );#endif
	
		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD_GFX_SWAP( "tp022_3.r16", 0x000000, 0x100000, 0x191b595f )
		ROM_LOAD_GFX_SWAP( "tp022_4.r16", 0x100000, 0x100000, 0xd58d29ca )
	
		ROM_REGION( 0x400000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD_GFX_SWAP( "tp022_5.r16", 0x000000, 0x200000, 0xd4c1db45 )
		ROM_LOAD_GFX_SWAP( "tp022_6.r16", 0x200000, 0x200000, 0xd48dc74f )
	
		ROM_REGION( 0x40000, REGION_SOUND1 );	/* ADPCM Samples */
		ROM_LOAD( "tp022_2.rom", 0x00000, 0x40000, 0x043271b3 );ROM_END(); }}; 
	
	static RomLoadPtr rom_kbash = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x080000, REGION_CPU1 );		/* Main 68K code */
		ROM_LOAD_WIDE_SWAP( "kbash01.bin", 0x000000, 0x080000, 0x2965f81d );
		/* Secondary CPU is a Toaplan marked chip, (TS-004-Dash  TOA PLAN) */
		/* Its a Z?80 of some sort - 94 pin chip. */
	#if Zx80
		ROM_REGION( 0x10000, REGION_CPU2 );		/* Sound Z?80 code */
	#else
		ROM_REGION( 0x8000, REGION_USER1 );#endif
		ROM_LOAD( "kbash02.bin", 0x00200, 0x07e00, 0x4cd882a1 );	ROM_CONTINUE(			 0x00000, 0x00200 );
		ROM_REGION( 0x800000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "kbash03.bin", 0x000000, 0x200000, 0x32ad508b );	ROM_LOAD( "kbash05.bin", 0x200000, 0x200000, 0xb84c90eb );	ROM_LOAD( "kbash04.bin", 0x400000, 0x200000, 0xe493c077 );	ROM_LOAD( "kbash06.bin", 0x600000, 0x200000, 0x9084b50a );
		ROM_REGION( 0x40000, REGION_SOUND1 );	/* ADPCM Samples */
		ROM_LOAD( "kbash07.bin", 0x00000, 0x40000, 0x3732318f );ROM_END(); }}; 
	
	static RomLoadPtr rom_tatsujn2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x080000, REGION_CPU1 );		/* Main 68K code */
		ROM_LOAD_WIDE( "tsj2rom1.bin", 0x000000, 0x080000, 0xf5cfe6ee );
	#if Zx80
		ROM_REGION( 0x10000, REGION_CPU2 );		/* Secondary CPU code */
		/* Secondary CPU is a Toaplan marked chip ??? */
	//	ROM_LOAD( "tp024.mcu", 0x00000, 0x08000, 0x00000000 );#endif
	
		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tsj2rom4.bin", 0x000000, 0x100000, 0x805c449e );	ROM_LOAD( "tsj2rom3.bin", 0x100000, 0x100000, 0x47587164 );
		ROM_REGION( 0x80000, REGION_SOUND1 );		/* ADPCM Samples */
		ROM_LOAD( "tsj2rom2.bin", 0x00000, 0x80000, 0xf2f6cae4 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pipibibs = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x040000, REGION_CPU1 );		/* Main 68K code */
		ROM_LOAD_EVEN( "tp025-1.bin", 0x000000, 0x020000, 0xb2ea8659 );	ROM_LOAD_ODD ( "tp025-2.bin", 0x000000, 0x020000, 0xdc53b939 );
		ROM_REGION( 0x10000, REGION_CPU2 );		/* Sound Z80 code */
		ROM_LOAD( "tp025-5.bin", 0x0000, 0x8000, 0xbf8ffde5 );
		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tp025-4.bin", 0x000000, 0x100000, 0xab97f744 );	ROM_LOAD( "tp025-3.bin", 0x100000, 0x100000, 0x7b16101e );ROM_END(); }}; 
	
	static RomLoadPtr rom_whoopee = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x040000, REGION_CPU1 );		/* Main 68K code */
		ROM_LOAD_EVEN( "whoopee.1", 0x000000, 0x020000, 0x28882e7e );	ROM_LOAD_ODD ( "whoopee.2", 0x000000, 0x020000, 0x6796f133 );
		ROM_REGION( 0x10000, REGION_CPU2 );		/* Sound Z80 code */
		/* sound CPU is a HD647180 (Z180) with internal ROM - not yet supported */
		/* use the Z80 version from the bootleg Pipi & Bibis set for now */
		ROM_LOAD( "hd647180.025", 0x00000, 0x08000, BADCRC( 0x101c0358 ));
	
		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tp025-4.bin", 0x000000, 0x100000, 0xab97f744 );	ROM_LOAD( "tp025-3.bin", 0x100000, 0x100000, 0x7b16101e );ROM_END(); }}; 
	
	static RomLoadPtr rom_pipibibi = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x040000, REGION_CPU1 );		/* Main 68K code */
		ROM_LOAD_EVEN( "ppbb06.bin", 0x000000, 0x020000, 0x14c92515 );	ROM_LOAD_ODD ( "ppbb05.bin", 0x000000, 0x020000, 0x3d51133c );
		ROM_REGION( 0x10000, REGION_CPU2 );		/* Sound Z80 code */
		ROM_LOAD( "ppbb08.bin", 0x0000, 0x8000, 0x101c0358 );
		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );	/* GFX data differs slightly from Toaplan boards ??? */
		ROM_LOAD_GFX_EVEN( "ppbb01.bin", 0x000000, 0x080000, 0x0fcae44b )
		ROM_LOAD_GFX_ODD ( "ppbb02.bin", 0x000000, 0x080000, 0x8bfcdf87 )
		ROM_LOAD_GFX_EVEN( "ppbb03.bin", 0x100000, 0x080000, 0xabdd2b8b )
		ROM_LOAD_GFX_ODD ( "ppbb04.bin", 0x100000, 0x080000, 0x70faa734 )
	
		ROM_REGION( 0x8000, REGION_USER1 );		/* ??? Some sort of table */
		ROM_LOAD( "ppbb07.bin", 0x0000, 0x8000, 0x456dd16e );ROM_END(); }}; 
	
	static RomLoadPtr rom_fixeight = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x080000, REGION_CPU1 );		/* Main 68K code */
		ROM_LOAD_WIDE_SWAP( "tp-026-1", 0x000000, 0x080000, 0xf7b1746a );
	#if Zx80
		ROM_REGION( 0x10000, REGION_CPU2 );		/* Secondary CPU code */
		/* Secondary CPU is a Toaplan marked chip, (TS-001-Turbo  TOA PLAN) */
		/* Its a Z?80 of some sort - 94 pin chip. */
	//	ROM_LOAD( "tp-026.mcu", 0x0000, 0x8000, 0x00000000 );#endif
	
		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tp-026-3", 0x000000, 0x200000, 0xe5578d98 );	ROM_LOAD( "tp-026-4", 0x200000, 0x200000, 0xb760cb53 );
		ROM_REGION( 0x40000, REGION_SOUND1 );	/* ADPCM Samples */
		ROM_LOAD( "tp-026-2", 0x00000, 0x40000, 0x85063f1f );
		ROM_REGION( 0x80, REGION_USER1 );	/* Serial EEPROM (93C45) connected to Secondary CPU */
		ROM_LOAD( "93c45.u21", 0x00, 0x80, 0x40d75df0 );ROM_END(); }}; 
	
	static RomLoadPtr rom_grindstm = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x080000, REGION_CPU1 );		/* Main 68K code */
		ROM_LOAD_WIDE( "01.bin", 0x000000, 0x080000, 0x99af8749 );
	#if Zx80
		ROM_REGION( 0x10000, REGION_CPU2 );		/* Sound CPU code */
		/* Secondary CPU is a Toaplan marked chip, (TS-007-Spy  TOA PLAN) */
		/* Its a Z?80 of some sort - 94 pin chip. */
	//	ROM_LOAD( "tp027.mcu", 0x8000, 0x8000, 0x00000000 );#endif
	
		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tp027_02.bin", 0x000000, 0x100000, 0x877b45e8 );	ROM_LOAD( "tp027_03.bin", 0x100000, 0x100000, 0xb1fc6362 );ROM_END(); }}; 
	
	static RomLoadPtr rom_vfive = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x080000, REGION_CPU1 );		/* Main 68K code */
		ROM_LOAD_WIDE( "tp027_01.bin", 0x000000, 0x080000, 0x98dd1919 );
	#if Zx80
		ROM_REGION( 0x10000, REGION_CPU2 );		/* Sound CPU code */
		/* Secondary CPU is a Toaplan marked chip, (TS-007-Spy  TOA PLAN) */
		/* Its a Z?80 of some sort - 94 pin chip. */
	//	ROM_LOAD( "tp027.mcu", 0x8000, 0x8000, 0x00000000 );#endif
	
		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tp027_02.bin", 0x000000, 0x100000, 0x877b45e8 );	ROM_LOAD( "tp027_03.bin", 0x100000, 0x100000, 0xb1fc6362 );ROM_END(); }}; 
	
	static RomLoadPtr rom_batsugun = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x080000, REGION_CPU1 );		/* Main 68K code */
		ROM_LOAD_WIDE( "tp030_1.bin", 0x000000, 0x080000, 0xe0cd772b );
	#if Zx80
		ROM_REGION( 0x10000, REGION_CPU2 );		/* Sound CPU code */
		/* Secondary CPU is a Toaplan marked chip, (TS-007-Spy  TOA PLAN) */
		/* Its a Z?80 of some sort - 94 pin chip. */
	//	ROM_LOAD( "tp030.mcu", 0x8000, 0x8000, 0x00000000 );#endif
	
		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tp030_5.bin",  0x000000, 0x100000, 0xbcf5ba05 );	ROM_LOAD( "tp030_6.bin",  0x100000, 0x100000, 0x0666fecd );
		ROM_REGION( 0x400000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tp030_3l.bin", 0x000000, 0x100000, 0x3024b793 );	ROM_LOAD( "tp030_3h.bin", 0x100000, 0x100000, 0xed75730b );	ROM_LOAD( "tp030_4l.bin", 0x200000, 0x100000, 0xfedb9861 );	ROM_LOAD( "tp030_4h.bin", 0x300000, 0x100000, 0xd482948b );
		ROM_REGION( 0x40000, REGION_SOUND1 );	/* ADPCM Samples */
		ROM_LOAD( "tp030_2.bin", 0x00000, 0x40000, 0x276146f5 );ROM_END(); }}; 
	
	static RomLoadPtr rom_batugnsp = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x080000, REGION_CPU1 );		/* Main 68K code */
		ROM_LOAD_WIDE( "tp030-sp.u69", 0x000000, 0x080000, 0xaeca7811 );
	#if Zx80
		ROM_REGION( 0x10000, REGION_CPU2 );		/* Sound CPU code */
		/* Secondary CPU is a Toaplan marked chip, (TS-007-Spy  TOA PLAN) */
		/* Its a Z?80 of some sort - 94 pin chip. */
	//	ROM_LOAD( "tp030.mcu", 0x8000, 0x8000, 0x00000000 );#endif
	
		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tp030_5.bin",  0x000000, 0x100000, 0xbcf5ba05 );	ROM_LOAD( "tp030_6.bin",  0x100000, 0x100000, 0x0666fecd );
		ROM_REGION( 0x400000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tp030_3l.bin", 0x000000, 0x100000, 0x3024b793 );	ROM_LOAD( "tp030_3h.bin", 0x100000, 0x100000, 0xed75730b );	ROM_LOAD( "tp030_4l.bin", 0x200000, 0x100000, 0xfedb9861 );	ROM_LOAD( "tp030_4h.bin", 0x300000, 0x100000, 0xd482948b );
		ROM_REGION( 0x40000, REGION_SOUND1 );	/* ADPCM Samples */
		ROM_LOAD( "tp030_2.bin", 0x00000, 0x40000, 0x276146f5 );ROM_END(); }}; 
	
	static RomLoadPtr rom_snowbro2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x080000, REGION_CPU1 );		/* Main 68K code */
		ROM_LOAD_WIDE_SWAP( "pro-4", 0x000000, 0x080000, 0x4c7ee341 );
		ROM_REGION( 0x300000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "rom2-l", 0x000000, 0x100000, 0xe9d366a9 );	ROM_LOAD( "rom2-h", 0x100000, 0x080000, 0x9aab7a62 );	ROM_LOAD( "rom3-l", 0x180000, 0x100000, 0xeb06e332 );	ROM_LOAD( "rom3-h", 0x280000, 0x080000, 0xdf4a952a );
		ROM_REGION( 0x80000, REGION_SOUND1 );	/* ADPCM Samples */
		ROM_LOAD( "rom4", 0x00000, 0x80000, 0x638f341e );ROM_END(); }}; 
	
	/* -------------------------- Raizing games ------------------------- */
	static RomLoadPtr rom_mahoudai = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x080000, REGION_CPU1 );		/* Main 68K code */
		ROM_LOAD_WIDE_SWAP( "ra_ma_01.01", 0x000000, 0x080000, 0x970ccc5c );
		ROM_REGION( 0x10000, REGION_CPU2 );		/* Sound Z80 code */
		ROM_LOAD( "ra_ma_01.02", 0x00000, 0x10000, 0xeabfa46d );
		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "ra_ma_01.03",  0x000000, 0x100000, 0x54e2bd95 );	ROM_LOAD( "ra_ma_01.04",  0x100000, 0x100000, 0x21cd378f );
		ROM_REGION( 0x008000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "ra_ma_01.05",  0x000000, 0x008000, 0xc00d1e80 );
		ROM_REGION( 0x40000, REGION_SOUND1 );		/* ADPCM Samples */
		ROM_LOAD( "ra_ma_01.06", 0x00000, 0x40000, 0x6edb2ab8 );ROM_END(); }}; 
	
	static RomLoadPtr rom_shippumd = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );		/* Main 68K code */
		ROM_LOAD_EVEN( "ma02rom1.bin", 0x000000, 0x080000, 0xa678b149 );	ROM_LOAD_ODD ( "ma02rom0.bin", 0x000000, 0x080000, 0xf226a212 );
		ROM_REGION( 0x10000, REGION_CPU2 );		/* Sound Z80 code */
		ROM_LOAD( "ma02rom2.bin", 0x00000, 0x10000, 0xdde8a57e );
		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "ma02rom3.bin",  0x000000, 0x200000, 0x0e797142 );	ROM_LOAD( "ma02rom4.bin",  0x200000, 0x200000, 0x72a6fa53 );
		ROM_REGION( 0x008000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "ma02rom5.bin",  0x000000, 0x008000, 0x116ae559 );
		ROM_REGION( 0x80000, REGION_SOUND1 );		/* ADPCM Samples */
		ROM_LOAD( "ma02rom6.bin", 0x00000, 0x80000, 0x199e7cae );ROM_END(); }}; 
	
	
	/* The following is in order of Toaplan Board/game numbers */
	/* See list at top of file */
	/* Whoopee machine to be changed to Teki Paki when (if) HD647180 is dumped */
	
	/*   ( YEAR  NAME      PARENT    MACHINE   INPUT     INIT      MONITOR       COMPANY    FULLNAME     FLAGS ) */
	public static GameDriver driver_tekipaki	   = new GameDriver("1991"	,"tekipaki"	,"toaplan2.java"	,rom_tekipaki,null	,machine_driver_tekipaki	,input_ports_tekipaki	,init_toaplan2	,ROT0	,	"Toaplan", "Teki Paki", GAME_NO_SOUND )
	public static GameDriver driver_ghox	   = new GameDriver("1991"	,"ghox"	,"toaplan2.java"	,rom_ghox,null	,machine_driver_ghox	,input_ports_ghox	,init_toaplan2	,ROT270	,	"Toaplan", "Ghox", GAME_NO_SOUND )
	public static GameDriver driver_dogyuun	   = new GameDriver("1991"	,"dogyuun"	,"toaplan2.java"	,rom_dogyuun,null	,machine_driver_dogyuun	,input_ports_dogyuun	,init_toaplan3	,ROT270	,	"Toaplan", "Dogyuun", GAME_NO_SOUND )
	public static GameDriver driver_kbash	   = new GameDriver("1993"	,"kbash"	,"toaplan2.java"	,rom_kbash,null	,machine_driver_kbash	,input_ports_kbash	,init_toaplan2	,ROT0_16BIT	,	"Toaplan", "Knuckle Bash", GAME_NO_SOUND )
	public static GameDriver driver_tatsujn2	   = new GameDriver("1992"	,"tatsujn2"	,"toaplan2.java"	,rom_tatsujn2,null	,machine_driver_tatsujn2	,input_ports_tatsujn2	,init_tatsujn2	,ROT270	,	"Toaplan", "Truxton II / Tatsujin II / Tatsujin Oh (Japan)" )
	public static GameDriver driver_pipibibs	   = new GameDriver("1991"	,"pipibibs"	,"toaplan2.java"	,rom_pipibibs,null	,machine_driver_pipibibs	,input_ports_pipibibs	,init_pipibibs	,ROT0	,	"Toaplan", "Pipi & Bibis / Whoopee (Japan)" )
	public static GameDriver driver_whoopee	   = new GameDriver("1991"	,"whoopee"	,"toaplan2.java"	,rom_whoopee,driver_pipibibs	,machine_driver_whoopee	,input_ports_whoopee	,init_pipibibs	,ROT0	,	"Toaplan", "Whoopee (Japan) / Pipi & Bibis (World)" )
	public static GameDriver driver_pipibibi	   = new GameDriver("1991"	,"pipibibi"	,"toaplan2.java"	,rom_pipibibi,driver_pipibibs	,machine_driver_pipibibi	,input_ports_pipibibi	,init_pipibibi	,ROT0	,	"[Toaplan] Ryouta Kikaku", "Pipi & Bibis / Whoopee (Japan) [bootleg ?]" )
	public static GameDriver driver_fixeight	   = new GameDriver("1992"	,"fixeight"	,"toaplan2.java"	,rom_fixeight,null	,machine_driver_fixeight	,input_ports_vfive	,init_toaplan3	,ROT270	,	"Toaplan", "FixEight", GAME_NOT_WORKING )
	public static GameDriver driver_grindstm	   = new GameDriver("1992"	,"grindstm"	,"toaplan2.java"	,rom_grindstm,driver_vfive	,machine_driver_vfive	,input_ports_grindstm	,init_toaplan3	,ROT270	,	"Toaplan", "Grind Stormer", GAME_NO_SOUND )
	public static GameDriver driver_vfive	   = new GameDriver("1993"	,"vfive"	,"toaplan2.java"	,rom_vfive,null	,machine_driver_vfive	,input_ports_vfive	,init_toaplan3	,ROT270	,	"Toaplan", "V-Five (Japan)", GAME_NO_SOUND )
	public static GameDriver driver_batsugun	   = new GameDriver("1993"	,"batsugun"	,"toaplan2.java"	,rom_batsugun,null	,machine_driver_batsugun	,input_ports_batsugun	,init_toaplan3	,ROT270_16BIT	,	"Toaplan", "Batsugun", GAME_NO_SOUND )
	public static GameDriver driver_batugnsp	   = new GameDriver("1993"	,"batugnsp"	,"toaplan2.java"	,rom_batugnsp,driver_batsugun	,machine_driver_batsugun	,input_ports_batsugun	,init_toaplan3	,ROT270_16BIT	,	"Toaplan", "Batsugun Special Ver.", GAME_NO_SOUND )
	public static GameDriver driver_snowbro2	   = new GameDriver("1994"	,"snowbro2"	,"toaplan2.java"	,rom_snowbro2,null	,machine_driver_snowbro2	,input_ports_snowbro2	,init_snowbro2	,ROT0_16BIT	,	"[Toaplan] Hanafram", "Snow Bros. 2 - With New Elves" )
	public static GameDriver driver_mahoudai	   = new GameDriver("1993"	,"mahoudai"	,"toaplan2.java"	,rom_mahoudai,null	,machine_driver_mahoudai	,input_ports_mahoudai	,null	,ROT270	,	"Raizing (Able license)", "Mahou Daisakusen (Japan)" )
	public static GameDriver driver_shippumd	   = new GameDriver("1994"	,"shippumd"	,"toaplan2.java"	,rom_shippumd,null	,machine_driver_shippumd	,input_ports_shippumd	,null	,ROT270	,	"Raizing", "Shippu Mahou Daisakusen (Japan)" )
}
