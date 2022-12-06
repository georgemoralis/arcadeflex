/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.machine;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.sound.namco.*;
import static gr.codebb.arcadeflex.v037b7.mame.timer.*;
import static gr.codebb.arcadeflex.v037b7.mame.timerH.*;
import static gr.codebb.arcadeflex.v037b16.vidhrdw.namcos1.*;

public class namcos1
{

/*TODO*///	#define NEW_TIMER 0 /* CPU slice optimize with new timer system */

	public static final int NAMCOS1_MAX_BANK = 0x400;

/*TODO*///	/* from vidhrdw */
/*TODO*///	extern void namcos1_set_scroll_offsets( const int *bgx, const int *bgy, int negative, int optimize );
/*TODO*///	extern void namcos1_set_optimize( int optimize );
/*TODO*///	extern void namcos1_set_sprite_offsets( int x, int y );

	public static final int NAMCOS1_MAX_KEY = 0x100;
	static int[] key = new int[NAMCOS1_MAX_KEY];

	static UBytePtr s1ram=new UBytePtr();

	static int namcos1_cpu1_banklatch;
	static int namcos1_reset = 0;


	static int berabohm_input_counter;
	
	
	
	/*******************************************************************************
	*																			   *
	*	Key emulation (CUS136) Rev1 (Pacmania & Galaga 88)						   *
	*																			   *
	*******************************************************************************/
	
	static int key_id;
	static int key_id_query;
	
	public static ReadHandlerPtr rev1_key_r  = new ReadHandlerPtr() { public int handler(int offset) {
	//	logerror("CPU #%d PC %08x: keychip read %04X=%02x\n",cpu_getactivecpu(),cpu_get_pc(),offset,key[offset]);
		if(offset >= NAMCOS1_MAX_KEY)
		{
			logerror("CPU #%d PC %08x: unmapped keychip read %04x\n",cpu_getactivecpu(),cpu_get_pc(),offset);
			return 0;
		}
		return key[offset];
	} };
        
        static int divider, divide_32 = 0;
        static int d;
	
	public static WriteHandlerPtr rev1_key_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		
		//logerror("CPU #%d PC %08x: keychip write %04X=%02x\n",cpu_getactivecpu(),cpu_get_pc(),offset,data);
		if(offset >= NAMCOS1_MAX_KEY)
		{
			logerror("CPU #%d PC %08x: unmapped keychip write %04x=%04x\n",cpu_getactivecpu(),cpu_get_pc(),offset,data);
			return;
		}
	
		key[offset] = data;
	
		switch ( offset )
		{
		case 0x01:
			divider = ( key[0] << 8 ) + key[1];
			break;
		case 0x03:
			{
				
				int	v1, v2;
				long	l=0;
	
				if (divide_32 != 0)
					l = d << 16;
	
				d = ( key[2] << 8 ) + key[3];
	
				if ( divider == 0 ) {
					v1 = 0xffff;
					v2 = 0;
				} else {
					if (divide_32 != 0) {
						l |= d;
	
						v1 = (int) (l / divider);
						v2 = (int) (l % divider);
					} else {
						v1 = d / divider;
						v2 = d % divider;
					}
				}
	
				key[2] = v1 >> 8;
				key[3] = v1;
				key[0] = v2 >> 8;
				key[1] = v2;
			}
			break;
		case 0x04:
			if ( key[4] == key_id_query ) /* get key number */
				key[4] = key_id;
	
			if ( key[4] == 0x0c )
				divide_32 = 1;
			else
				divide_32 = 0;
			break;
		}
	} };
	
/*TODO*///	/*******************************************************************************
/*TODO*///	*																			   *
/*TODO*///	*	Key emulation (CUS136) Rev2 (Dragon Spirit, Blazer, World Court)		   *
/*TODO*///	*																			   *
/*TODO*///	*******************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr rev2_key_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		//logerror("CPU #%d PC %08x: keychip read %04X=%02x\n",cpu_getactivecpu(),cpu_get_pc(),offset,key[offset]);
/*TODO*///		if(offset >= NAMCOS1_MAX_KEY)
/*TODO*///		{
/*TODO*///			logerror("CPU #%d PC %08x: unmapped keychip read %04x\n",cpu_getactivecpu(),cpu_get_pc(),offset);
/*TODO*///			return 0;
/*TODO*///		}
/*TODO*///		return key[offset];
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr rev2_key_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		//logerror("CPU #%d PC %08x: keychip write %04X=%02x\n",cpu_getactivecpu(),cpu_get_pc(),offset,data);
/*TODO*///		if(offset >= NAMCOS1_MAX_KEY)
/*TODO*///		{
/*TODO*///			logerror("CPU #%d PC %08x: unmapped keychip write %04x=%04x\n",cpu_getactivecpu(),cpu_get_pc(),offset,data);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		key[offset] = data;
/*TODO*///	
/*TODO*///		switch(offset)
/*TODO*///		{
/*TODO*///		case 0x00:
/*TODO*///			if ( data == 1 )
/*TODO*///			{
/*TODO*///				/* fetch key ID */
/*TODO*///				key[3] = key_id;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		case 0x02:
/*TODO*///			/* $f2 = Dragon Spirit, $b7 = Blazer , $35($d9) = worldcourt */
/*TODO*///			if ( key[3] == 0xf2 || key[3] == 0xb7 || key[3] == 0x35 )
/*TODO*///			{
/*TODO*///				switch( key[0] )
/*TODO*///				{
/*TODO*///					case 0x10: key[0] = 0x05; key[1] = 0x00; key[2] = 0xc6; break;
/*TODO*///					case 0x12: key[0] = 0x09; key[1] = 0x00; key[2] = 0x96; break;
/*TODO*///					case 0x15: key[0] = 0x0a; key[1] = 0x00; key[2] = 0x8f; break;
/*TODO*///					case 0x22: key[0] = 0x14; key[1] = 0x00; key[2] = 0x39; break;
/*TODO*///					case 0x32: key[0] = 0x31; key[1] = 0x00; key[2] = 0x12; break;
/*TODO*///					case 0x3d: key[0] = 0x35; key[1] = 0x00; key[2] = 0x27; break;
/*TODO*///					case 0x54: key[0] = 0x10; key[1] = 0x00; key[2] = 0x03; break;
/*TODO*///					case 0x58: key[0] = 0x49; key[1] = 0x00; key[2] = 0x23; break;
/*TODO*///					case 0x7b: key[0] = 0x48; key[1] = 0x00; key[2] = 0xd4; break;
/*TODO*///					case 0xc7: key[0] = 0xbf; key[1] = 0x00; key[2] = 0xe8; break;
/*TODO*///				}
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		case 0x03:
/*TODO*///			/* $c2 = Dragon Spirit, $b6 = Blazer */
/*TODO*///			if ( key[3] == 0xc2 || key[3] == 0xb6 ) {
/*TODO*///				key[3] = 0x36;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			/* $d9 = World court */
/*TODO*///			if ( key[3] == 0xd9 )
/*TODO*///			{
/*TODO*///				key[3] = 0x35;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		case 0x3f:	/* Splatter House */
/*TODO*///			key[0x3f] = 0xb5;
/*TODO*///			key[0x36] = 0xb5;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* ?? */
/*TODO*///		if ( key[3] == 0x01 ) {
/*TODO*///			if ( key[0] == 0x40 && key[1] == 0x04 && key[2] == 0x00 ) {
/*TODO*///				key[1] = 0x00; key[2] = 0x10;
/*TODO*///				return;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************************************
/*TODO*///	*																			   *
/*TODO*///	*	Key emulation (CUS136) for Dangerous Seed								   *
/*TODO*///	*																			   *
/*TODO*///	*******************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr dangseed_key_r  = new ReadHandlerPtr() { public int handler(int offset) {
/*TODO*///	//	logerror("CPU #%d PC %08x: keychip read %04X=%02x\n",cpu_getactivecpu(),cpu_get_pc(),offset,key[offset]);
/*TODO*///		if(offset >= NAMCOS1_MAX_KEY)
/*TODO*///		{
/*TODO*///			logerror("CPU #%d PC %08x: unmapped keychip read %04x\n",cpu_getactivecpu(),cpu_get_pc(),offset);
/*TODO*///			return 0;
/*TODO*///		}
/*TODO*///		return key[offset];
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr dangseed_key_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
/*TODO*///		int i;
/*TODO*///	//	logerror("CPU #%d PC %08x: keychip write %04X=%02x\n",cpu_getactivecpu(),cpu_get_pc(),offset,data);
/*TODO*///		if(offset >= NAMCOS1_MAX_KEY)
/*TODO*///		{
/*TODO*///			logerror("CPU #%d PC %08x: unmapped keychip write %04x=%04x\n",cpu_getactivecpu(),cpu_get_pc(),offset,data);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		key[offset] = data;
/*TODO*///	
/*TODO*///		switch ( offset )
/*TODO*///		{
/*TODO*///			case 0x50:
/*TODO*///				for ( i = 0; i < 0x50; i++ ) {
/*TODO*///					key[i] = ( data >> ( ( i >> 4 ) & 0x0f ) ) & 0x0f;
/*TODO*///					key[i] |= ( i & 0x0f ) << 4;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x57:
/*TODO*///				key[3] = key_id;
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************************************
/*TODO*///	*																			   *
/*TODO*///	*	Key emulation (CUS136) for Dragon Spirit								   *
/*TODO*///	*																			   *
/*TODO*///	*******************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr dspirit_key_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		//logerror("CPU #%d PC %08x: keychip read %04X=%02x\n",cpu_getactivecpu(),cpu_get_pc(),offset,key[offset]);
/*TODO*///		if(offset >= NAMCOS1_MAX_KEY)
/*TODO*///		{
/*TODO*///			logerror("CPU #%d PC %08x: unmapped keychip read %04x\n",cpu_getactivecpu(),cpu_get_pc(),offset);
/*TODO*///			return 0;
/*TODO*///		}
/*TODO*///		return key[offset];
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr dspirit_key_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		static unsigned short divisor;
/*TODO*///	//	logerror("CPU #%d PC %08x: keychip write %04X=%02x\n",cpu_getactivecpu(),cpu_get_pc(),offset,data);
/*TODO*///		if(offset >= NAMCOS1_MAX_KEY)
/*TODO*///		{
/*TODO*///			logerror("CPU #%d PC %08x: unmapped keychip write %04x=%04x\n",cpu_getactivecpu(),cpu_get_pc(),offset,data);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		key[offset] = data;
/*TODO*///	
/*TODO*///		switch(offset)
/*TODO*///		{
/*TODO*///		case 0x00:
/*TODO*///			if ( data == 1 )
/*TODO*///			{
/*TODO*///				/* fetch key ID */
/*TODO*///				key[3] = key_id;
/*TODO*///			} else
/*TODO*///				divisor = data;
/*TODO*///			break;
/*TODO*///	
/*TODO*///		case 0x01:
/*TODO*///			if ( key[3] == 0x01 ) { /* division gets resolved on latch to $1 */
/*TODO*///				unsigned short d, v1, v2;
/*TODO*///	
/*TODO*///				d = ( key[1] << 8 ) + key[2];
/*TODO*///	
/*TODO*///				if ( divisor == 0 ) {
/*TODO*///					v1 = 0xffff;
/*TODO*///					v2 = 0;
/*TODO*///				} else {
/*TODO*///					v1 = d / divisor;
/*TODO*///					v2 = d % divisor;
/*TODO*///				}
/*TODO*///	
/*TODO*///				key[0] = v2 & 0xff;
/*TODO*///				key[1] = v1 >> 8;
/*TODO*///				key[2] = v1 & 0xff;
/*TODO*///	
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			if ( key[3] != 0xf2 ) { /* if its an invalid mode, clear regs */
/*TODO*///				key[0] = 0;
/*TODO*///				key[1] = 0;
/*TODO*///				key[2] = 0;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		case 0x02:
/*TODO*///			if ( key[3] == 0xf2 ) { /* division gets resolved on latch to $2 */
/*TODO*///				unsigned short d, v1, v2;
/*TODO*///	
/*TODO*///				d = ( key[1] << 8 ) + key[2];
/*TODO*///	
/*TODO*///				if ( divisor == 0 ) {
/*TODO*///					v1 = 0xffff;
/*TODO*///					v2 = 0;
/*TODO*///				} else {
/*TODO*///					v1 = d / divisor;
/*TODO*///					v2 = d % divisor;
/*TODO*///				}
/*TODO*///	
/*TODO*///				key[0] = v2 & 0xff;
/*TODO*///				key[1] = v1 >> 8;
/*TODO*///				key[2] = v1 & 0xff;
/*TODO*///	
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			if ( key[3] != 0x01 ) { /* if its an invalid mode, clear regs */
/*TODO*///				key[0] = 0;
/*TODO*///				key[1] = 0;
/*TODO*///				key[2] = 0;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		case 0x03:
/*TODO*///			if ( key[3] != 0xf2 && key[3] != 0x01 ) /* if the mode is unknown return the id on $3 */
/*TODO*///				key[3] = key_id;
/*TODO*///			break;
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************************************
/*TODO*///	*																			   *
/*TODO*///	*	Key emulation (CUS136) for Blazer										   *
/*TODO*///	*																			   *
/*TODO*///	*******************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr blazer_key_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		logerror("CPU #%d PC %08x: keychip read %04X=%02x\n",cpu_getactivecpu(),cpu_get_pc(),offset,key[offset]);
/*TODO*///		if(offset >= NAMCOS1_MAX_KEY)
/*TODO*///		{
/*TODO*///			logerror("CPU #%d PC %08x: unmapped keychip read %04x\n",cpu_getactivecpu(),cpu_get_pc(),offset);
/*TODO*///			return 0;
/*TODO*///		}
/*TODO*///		return key[offset];
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr blazer_key_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		static unsigned short divisor;
/*TODO*///		logerror("CPU #%d PC %08x: keychip write %04X=%02x\n",cpu_getactivecpu(),cpu_get_pc(),offset,data);
/*TODO*///		if(offset >= NAMCOS1_MAX_KEY)
/*TODO*///		{
/*TODO*///			logerror("CPU #%d PC %08x: unmapped keychip write %04x=%04x\n",cpu_getactivecpu(),cpu_get_pc(),offset,data);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		key[offset] = data;
/*TODO*///	
/*TODO*///		switch(offset)
/*TODO*///		{
/*TODO*///		case 0x00:
/*TODO*///			if ( data == 1 )
/*TODO*///			{
/*TODO*///				/* fetch key ID */
/*TODO*///				key[3] = key_id;
/*TODO*///			} else
/*TODO*///				divisor = data;
/*TODO*///			break;
/*TODO*///	
/*TODO*///		case 0x01:
/*TODO*///			if ( key[3] != 0xb7 ) { /* if its an invalid mode, clear regs */
/*TODO*///				key[0] = 0;
/*TODO*///				key[1] = 0;
/*TODO*///				key[2] = 0;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///	
/*TODO*///		case 0x02:
/*TODO*///			if ( key[3] == 0xb7 ) { /* division gets resolved on latch to $2 */
/*TODO*///				unsigned short d, v1, v2;
/*TODO*///	
/*TODO*///				d = ( key[1] << 8 ) + key[2];
/*TODO*///	
/*TODO*///				if ( divisor == 0 ) {
/*TODO*///					v1 = 0xffff;
/*TODO*///					v2 = 0;
/*TODO*///				} else {
/*TODO*///					v1 = d / divisor;
/*TODO*///					v2 = d % divisor;
/*TODO*///				}
/*TODO*///	
/*TODO*///				key[0] = v2 & 0xff;
/*TODO*///				key[1] = v1 >> 8;
/*TODO*///				key[2] = v1 & 0xff;
/*TODO*///	
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* if its an invalid mode, clear regs */
/*TODO*///			key[0] = 0;
/*TODO*///			key[1] = 0;
/*TODO*///			key[2] = 0;
/*TODO*///			break;
/*TODO*///		case 0x03:
/*TODO*///			if ( key[3] != 0xb7 ) { /* if the mode is unknown return the id on $3 */
/*TODO*///				key[3] = key_id;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************************************
/*TODO*///	*																			   *
/*TODO*///	*	Key emulation (CUS136) for World Stadium								   *
/*TODO*///	*																			   *
/*TODO*///	*******************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr ws_key_r  = new ReadHandlerPtr() { public int handler(int offset) {
/*TODO*///	//	logerror("CPU #%d PC %08x: keychip read %04X=%02x\n",cpu_getactivecpu(),cpu_get_pc(),offset,key[offset]);
/*TODO*///		if(offset >= NAMCOS1_MAX_KEY)
/*TODO*///		{
/*TODO*///			logerror("CPU #%d PC %08x: unmapped keychip read %04x\n",cpu_getactivecpu(),cpu_get_pc(),offset);
/*TODO*///			return 0;
/*TODO*///		}
/*TODO*///		return key[offset];
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr ws_key_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
/*TODO*///		static unsigned short divider;
/*TODO*///		//logerror("CPU #%d PC %08x: keychip write %04X=%02x\n",cpu_getactivecpu(),cpu_get_pc(),offset,data);
/*TODO*///		if(offset >= NAMCOS1_MAX_KEY)
/*TODO*///		{
/*TODO*///			logerror("CPU #%d PC %08x: unmapped keychip write %04x=%04x\n",cpu_getactivecpu(),cpu_get_pc(),offset,data);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		key[offset] = data;
/*TODO*///	
/*TODO*///		switch ( offset )
/*TODO*///		{
/*TODO*///		case 0x01:
/*TODO*///			divider = ( key[0] << 8 ) + key[1];
/*TODO*///			break;
/*TODO*///		case 0x03:
/*TODO*///			{
/*TODO*///				static unsigned short d;
/*TODO*///				unsigned short	v1, v2;
/*TODO*///	
/*TODO*///				d = ( key[2] << 8 ) + key[3];
/*TODO*///	
/*TODO*///				if ( divider == 0 ) {
/*TODO*///					v1 = 0xffff;
/*TODO*///					v2 = 0;
/*TODO*///				} else {
/*TODO*///					v1 = d / divider;
/*TODO*///					v2 = d % divider;
/*TODO*///				}
/*TODO*///	
/*TODO*///				key[2] = v1 >> 8;
/*TODO*///				key[3] = v1;
/*TODO*///				key[0] = v2 >> 8;
/*TODO*///				key[1] = v2;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		case 0x04:
/*TODO*///			key[4] = key_id;
/*TODO*///			break;
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************************************
/*TODO*///	*																			   *
/*TODO*///	*	Key emulation (CUS181) for SplatterHouse								   *
/*TODO*///	*																			   *
/*TODO*///	*******************************************************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr splatter_key_r  = new ReadHandlerPtr() { public int handler(int offset) {
/*TODO*///	//	logerror("CPU #%d PC %08x: keychip read %04X=%02x\n",cpu_getactivecpu(),cpu_get_pc(),offset,key[offset]);
/*TODO*///		switch( ( offset >> 4 ) & 0x07 ) {
/*TODO*///			case 0x00:
/*TODO*///			case 0x06:
/*TODO*///				return 0xff;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x01:
/*TODO*///			case 0x02:
/*TODO*///			case 0x05:
/*TODO*///			case 0x07:
/*TODO*///				return ( ( offset & 0x0f ) << 4 ) | 0x0f;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x03:
/*TODO*///				return 0xb5;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x04:
/*TODO*///				{
/*TODO*///					int data = 0x29;
/*TODO*///	
/*TODO*///					if ( offset >= 0x1000 )
/*TODO*///						data |= 0x80;
/*TODO*///					if ( offset >= 0x2000 )
/*TODO*///						data |= 0x04;
/*TODO*///	
/*TODO*///					return data;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* make compiler happy */
/*TODO*///		return 0;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr splatter_key_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
/*TODO*///	//	logerror("CPU #%d PC %08x: keychip write %04X=%02x\n",cpu_getactivecpu(),cpu_get_pc(),offset,data);
/*TODO*///		/* ignored */
/*TODO*///	} };
	
	
	/*******************************************************************************
	*																			   *
	*	Banking emulation (CUS117)												   *
	*																			   *
	*******************************************************************************/
	
	public static ReadHandlerPtr soundram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		if(offset<0x100)
			return namcos1_wavedata_r.handler(offset);
		if(offset<0x140)
			return namcos1_sound_r.handler(offset-0x100);
	
		/* shared ram */
		return namco_wavedata.read(offset);
	} };
	
	public static WriteHandlerPtr soundram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if(offset<0x100)
		{
			namcos1_wavedata_w.handler(offset,data);
			return;
		}
		if(offset<0x140)
		{
			namcos1_sound_w.handler(offset-0x100,data);
			return;
		}
		/* shared ram */
		namco_wavedata.write(offset, data);
	
		//if(offset>=0x1000)
		//	logerror("CPU #%d PC %04x: write shared ram %04x=%02x\n",cpu_getactivecpu(),cpu_get_pc(),offset,data);
	} };
	
	/* ROM handlers */
	
	public static WriteHandlerPtr rom_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		logerror("CPU #%d PC %04x: warning - write %02x to rom address %04x\n",cpu_getactivecpu(),cpu_get_pc(),data,offset);
	} };
	
	/* error handlers */
	public static ReadHandlerPtr unknown_r  = new ReadHandlerPtr() { public int handler(int offset) {
		logerror("CPU #%d PC %04x: warning - read from unknown chip\n",cpu_getactivecpu(),cpu_get_pc() );
		return 0;
	} };
	
	public static WriteHandlerPtr unknown_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		logerror("CPU #%d PC %04x: warning - wrote to unknown chip\n",cpu_getactivecpu(),cpu_get_pc() );
	} };
	
	/* Bank handler definitions */
	
	static class bankhandler {
		public ReadHandlerPtr bank_handler_r;
		public WriteHandlerPtr bank_handler_w;
		public int bank_offset;
		public UBytePtr bank_pointer=new UBytePtr(1024 * 128);
	};
	
	static bankhandler[] namcos1_bank_element = new bankhandler[NAMCOS1_MAX_BANK];
	
	/* This is where we store our handlers */
	/* 2 cpus with 8 banks of 8k each	   */
	static bankhandler[][] namcos1_banks = new bankhandler[2][8];
        
        static int chip = 0;

	/* Main bankswitching routine */
	public static WriteHandlerPtr namcos1_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
	
		if ((offset & 1) != 0) {
			int bank = ( offset >> 9 ) & 0x07; //0x0f;
			int cpu = cpu_getactivecpu();
			chip &= 0x0300;
			chip |= ( data & 0xff );
			/* copy bank handler */
			namcos1_banks[cpu][bank].bank_handler_r = namcos1_bank_element[chip].bank_handler_r;
			namcos1_banks[cpu][bank].bank_handler_w = namcos1_bank_element[chip].bank_handler_w;
			namcos1_banks[cpu][bank].bank_offset	= namcos1_bank_element[chip].bank_offset;
			namcos1_banks[cpu][bank].bank_pointer	= namcos1_bank_element[chip].bank_pointer;
			//memcpy( &namcos1_banks[cpu][bank] , &namcos1_bank_element[chip] , sizeof(bankhandler));
	
			/* unmapped bank warning */
			if( namcos1_banks[cpu][bank].bank_handler_r == unknown_r)
			{
				logerror("CPU #%d PC %04x:warning unknown chip selected bank %x=$%04x\n", cpu , cpu_get_pc(), bank , chip );
			}
	
			/* renew pc base */
	//		change_pc16(cpu_get_pc());
		} else {
			chip &= 0x00ff;
			chip |= ( data & 0xff ) << 8;
		}
	} };
	
	/* Sub cpu set start bank port */
	public static WriteHandlerPtr namcos1_subcpu_bank_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldcpu = cpu_getactivecpu();
	
		//logerror("cpu1 bank selected %02x=%02x\n",offset,data);
		namcos1_cpu1_banklatch = (namcos1_cpu1_banklatch&0x300)|data;
		/* Prepare code for Cpu 1 */
		cpu_setactivecpu( 1 );
		namcos1_bankswitch_w.handler( 0x0e00, namcos1_cpu1_banklatch>>8  );
		namcos1_bankswitch_w.handler( 0x0e01, namcos1_cpu1_banklatch&0xff);
		/* cpu_set_reset_line(1,PULSE_LINE); */
	
		cpu_setactivecpu( oldcpu );
	} };
	
/*TODO*///	#define MR_HANDLER(cpu,bank) \
/*TODO*///	public static ReadHandlerPtr namcos1_##cpu##_banked_area##bank##_r  = new ReadHandlerPtr() { public int handler(int offset) {\
/*TODO*///		if( namcos1_banks[cpu][bank].bank_handler_r) \
/*TODO*///			return (*namcos1_banks[cpu][bank].bank_handler_r)( offset+namcos1_banks[cpu][bank].bank_offset); \
/*TODO*///		return namcos1_banks[cpu][bank].bank_pointer[offset]; } };

    //	MR_HANDLER(0,0)
    public static ReadHandlerPtr namcos1_0_banked_area0_r  = new ReadHandlerPtr() { public int handler(int offset) {
		if( namcos1_banks[0][0].bank_handler_r != null)
			return (namcos1_banks[0][0].bank_handler_r).handler( offset+namcos1_banks[0][0].bank_offset);
		return namcos1_banks[0][0].bank_pointer.read(offset); } };
    
    //	MR_HANDLER(0,1)
    public static ReadHandlerPtr namcos1_0_banked_area1_r  = new ReadHandlerPtr() { public int handler(int offset) {
            if( namcos1_banks[0][1].bank_handler_r != null ) 
                    return (namcos1_banks[0][1].bank_handler_r).handler( offset+namcos1_banks[0][1].bank_offset);
            return namcos1_banks[0][1].bank_pointer.read(offset); } };
    
    //	MR_HANDLER(0,2)
    public static ReadHandlerPtr namcos1_0_banked_area2_r  = new ReadHandlerPtr() { public int handler(int offset) {
            if( namcos1_banks[0][2].bank_handler_r != null ) 
                    return (namcos1_banks[0][2].bank_handler_r).handler( offset+namcos1_banks[0][2].bank_offset);
            return namcos1_banks[0][2].bank_pointer.read(offset); } };
    
    //	MR_HANDLER(0,3)
    public static ReadHandlerPtr namcos1_0_banked_area3_r  = new ReadHandlerPtr() { public int handler(int offset) {
            if( namcos1_banks[0][3].bank_handler_r != null ) 
                    return (namcos1_banks[0][3].bank_handler_r).handler( offset+namcos1_banks[0][3].bank_offset);
            return namcos1_banks[0][3].bank_pointer.read(offset); } };
    
    //	MR_HANDLER(0,4)
    public static ReadHandlerPtr namcos1_0_banked_area4_r  = new ReadHandlerPtr() { public int handler(int offset) {
            if( namcos1_banks[0][4].bank_handler_r != null ) 
                    return (namcos1_banks[0][4].bank_handler_r).handler( offset+namcos1_banks[0][4].bank_offset);
            return namcos1_banks[0][4].bank_pointer.read(offset); } };
    
    //	MR_HANDLER(0,5)
    public static ReadHandlerPtr namcos1_0_banked_area5_r  = new ReadHandlerPtr() { public int handler(int offset) {
            if( namcos1_banks[0][5].bank_handler_r != null ) 
                    return (namcos1_banks[0][5].bank_handler_r).handler( offset+namcos1_banks[0][5].bank_offset);
            return namcos1_banks[0][5].bank_pointer.read(offset); } };
    
    //	MR_HANDLER(0,6)
    public static ReadHandlerPtr namcos1_0_banked_area6_r  = new ReadHandlerPtr() { public int handler(int offset) {
            if( namcos1_banks[0][6].bank_handler_r != null ) 
                    return (namcos1_banks[0][6].bank_handler_r).handler( offset+namcos1_banks[0][6].bank_offset);
            return namcos1_banks[0][6].bank_pointer.read(offset); } };
    
    //	MR_HANDLER(0,7)
    public static ReadHandlerPtr namcos1_0_banked_area7_r  = new ReadHandlerPtr() { public int handler(int offset) {
            if( namcos1_banks[0][7].bank_handler_r != null ) 
                    return (namcos1_banks[0][7].bank_handler_r).handler( offset+namcos1_banks[0][7].bank_offset);
            return namcos1_banks[0][7].bank_pointer.read(offset); } };
    
    //	MR_HANDLER(1,0)
    public static ReadHandlerPtr namcos1_1_banked_area0_r  = new ReadHandlerPtr() { public int handler(int offset) {
            if( namcos1_banks[1][0].bank_handler_r != null ) 
                    return (namcos1_banks[1][0].bank_handler_r).handler( offset+namcos1_banks[1][0].bank_offset);
            return namcos1_banks[1][0].bank_pointer.read(offset); } };
    
    //	MR_HANDLER(1,1)
    public static ReadHandlerPtr namcos1_1_banked_area1_r  = new ReadHandlerPtr() { public int handler(int offset) {
            if( namcos1_banks[1][1].bank_handler_r != null ) 
                    return (namcos1_banks[1][1].bank_handler_r).handler( offset+namcos1_banks[1][1].bank_offset);
            return namcos1_banks[1][1].bank_pointer.read(offset); } };
    
    //	MR_HANDLER(1,2)
    public static ReadHandlerPtr namcos1_1_banked_area2_r  = new ReadHandlerPtr() { public int handler(int offset) {
            if( namcos1_banks[1][2].bank_handler_r != null ) 
                    return (namcos1_banks[1][2].bank_handler_r).handler( offset+namcos1_banks[1][2].bank_offset);
            return namcos1_banks[1][2].bank_pointer.read(offset); } };
    
    //	MR_HANDLER(1,3)
    public static ReadHandlerPtr namcos1_1_banked_area3_r  = new ReadHandlerPtr() { public int handler(int offset) {
            if( namcos1_banks[1][3].bank_handler_r != null ) 
                    return (namcos1_banks[1][3].bank_handler_r).handler( offset+namcos1_banks[1][3].bank_offset);
            return namcos1_banks[1][3].bank_pointer.read(offset); } };
    
    //	MR_HANDLER(1,4)
    public static ReadHandlerPtr namcos1_1_banked_area4_r  = new ReadHandlerPtr() { public int handler(int offset) {
            if( namcos1_banks[1][4].bank_handler_r != null ) 
                    return (namcos1_banks[1][4].bank_handler_r).handler( offset+namcos1_banks[1][4].bank_offset);
            return namcos1_banks[1][4].bank_pointer.read(offset); } };
    
    //	MR_HANDLER(1,5)
    public static ReadHandlerPtr namcos1_1_banked_area5_r  = new ReadHandlerPtr() { public int handler(int offset) {
            if( namcos1_banks[1][5].bank_handler_r != null ) 
                    return (namcos1_banks[1][5].bank_handler_r).handler( offset+namcos1_banks[1][5].bank_offset);
            return namcos1_banks[1][5].bank_pointer.read(offset); } };
    
    //	MR_HANDLER(1,6)
    public static ReadHandlerPtr namcos1_1_banked_area6_r  = new ReadHandlerPtr() { public int handler(int offset) {
            if( namcos1_banks[1][6].bank_handler_r != null ) 
                    return (namcos1_banks[1][6].bank_handler_r).handler( offset+namcos1_banks[1][6].bank_offset);
            return namcos1_banks[1][6].bank_pointer.read(offset); } };
    
    //	MR_HANDLER(1,7)
    public static ReadHandlerPtr namcos1_1_banked_area7_r  = new ReadHandlerPtr() { public int handler(int offset) {
            if( namcos1_banks[1][7].bank_handler_r != null ) 
                    return (namcos1_banks[1][7].bank_handler_r).handler( offset+namcos1_banks[1][7].bank_offset);
            return namcos1_banks[1][7].bank_pointer.read(offset); } };
    
/*TODO*///	#undef MR_HANDLER
/*TODO*///	
/*TODO*///	#define MW_HANDLER(cpu,bank) \
/*TODO*///	public static WriteHandlerPtr namcos1_##cpu##_banked_area##bank##_w = new WriteHandlerPtr() {public void handler(int offset, int data) {\
/*TODO*///		if( namcos1_banks[cpu][bank].bank_handler_w) \
/*TODO*///		{ \
/*TODO*///			(*namcos1_banks[cpu][bank].bank_handler_w)( offset+ namcos1_banks[cpu][bank].bank_offset,data ); \
/*TODO*///			return; \
/*TODO*///		}\
/*TODO*///		namcos1_banks[cpu][bank].bank_pointer[offset]=data; \
/*TODO*///	} };
/*TODO*///	
    //	MW_HANDLER(0,0)
    public static WriteHandlerPtr namcos1_0_banked_area0_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		if( namcos1_banks[0][0].bank_handler_w != null)
		{
			(namcos1_banks[0][0].bank_handler_w).handler( offset+ namcos1_banks[0][0].bank_offset,data );
			return;
		}
		namcos1_banks[0][0].bank_pointer.write(offset, data);
    } };
    
    //	MW_HANDLER(0,1)
    public static WriteHandlerPtr namcos1_0_banked_area1_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		if( namcos1_banks[0][1].bank_handler_w != null)
		{
			(namcos1_banks[0][1].bank_handler_w).handler( offset+ namcos1_banks[0][1].bank_offset,data );
			return;
		}
		namcos1_banks[0][1].bank_pointer.write(offset, data);
    } };
    
    //	MW_HANDLER(0,2)
    public static WriteHandlerPtr namcos1_0_banked_area2_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		if( namcos1_banks[0][2].bank_handler_w != null)
		{
			(namcos1_banks[0][2].bank_handler_w).handler( offset+ namcos1_banks[0][2].bank_offset,data );
			return;
		}
		namcos1_banks[0][2].bank_pointer.write(offset, data);
    } };
    
    //	MW_HANDLER(0,3)
    public static WriteHandlerPtr namcos1_0_banked_area3_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		if( namcos1_banks[0][3].bank_handler_w != null)
		{
			(namcos1_banks[0][3].bank_handler_w).handler( offset+ namcos1_banks[0][3].bank_offset,data );
			return;
		}
		namcos1_banks[0][3].bank_pointer.write(offset, data);
    } };
    
    //	MW_HANDLER(0,4)
    public static WriteHandlerPtr namcos1_0_banked_area4_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		if( namcos1_banks[0][4].bank_handler_w != null)
		{
			(namcos1_banks[0][4].bank_handler_w).handler( offset+ namcos1_banks[0][4].bank_offset,data );
			return;
		}
		namcos1_banks[0][4].bank_pointer.write(offset, data);
    } };
    
    //	MW_HANDLER(0,5)
    public static WriteHandlerPtr namcos1_0_banked_area5_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		if( namcos1_banks[0][5].bank_handler_w != null)
		{
			(namcos1_banks[0][5].bank_handler_w).handler( offset+ namcos1_banks[0][5].bank_offset,data );
			return;
		}
		namcos1_banks[0][5].bank_pointer.write(offset, data);
    } };
    
    //	MW_HANDLER(0,6)
    public static WriteHandlerPtr namcos1_0_banked_area6_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		if( namcos1_banks[0][6].bank_handler_w != null)
		{
			(namcos1_banks[0][6].bank_handler_w).handler( offset+ namcos1_banks[0][6].bank_offset,data );
			return;
		}
		namcos1_banks[0][6].bank_pointer.write(offset, data);
    } };
    
    //	MW_HANDLER(1,0)
    public static WriteHandlerPtr namcos1_1_banked_area0_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		if( namcos1_banks[1][0].bank_handler_w != null)
		{
			(namcos1_banks[1][0].bank_handler_w).handler( offset+ namcos1_banks[1][0].bank_offset,data );
			return;
		}
		namcos1_banks[1][0].bank_pointer.write(offset, data);
    } };
    
    //	MW_HANDLER(1,1)
    public static WriteHandlerPtr namcos1_1_banked_area1_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		if( namcos1_banks[1][1].bank_handler_w != null)
		{
			(namcos1_banks[1][1].bank_handler_w).handler( offset+ namcos1_banks[1][1].bank_offset,data );
			return;
		}
		namcos1_banks[1][1].bank_pointer.write(offset, data);
    } };
    
    //	MW_HANDLER(1,2)
    public static WriteHandlerPtr namcos1_1_banked_area2_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		if( namcos1_banks[1][2].bank_handler_w != null)
		{
			(namcos1_banks[1][2].bank_handler_w).handler( offset+ namcos1_banks[1][2].bank_offset,data );
			return;
		}
		namcos1_banks[1][2].bank_pointer.write(offset, data);
    } };
    
    //	MW_HANDLER(1,3)
    public static WriteHandlerPtr namcos1_1_banked_area3_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		if( namcos1_banks[1][3].bank_handler_w != null)
		{
			(namcos1_banks[1][3].bank_handler_w).handler( offset+ namcos1_banks[1][3].bank_offset,data );
			return;
		}
		namcos1_banks[1][3].bank_pointer.write(offset, data);
    } };
    
    //	MW_HANDLER(1,4)
    public static WriteHandlerPtr namcos1_1_banked_area4_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		if( namcos1_banks[1][4].bank_handler_w != null)
		{
			(namcos1_banks[1][4].bank_handler_w).handler( offset+ namcos1_banks[1][4].bank_offset,data );
			return;
		}
		namcos1_banks[1][4].bank_pointer.write(offset, data);
    } };
    
    //	MW_HANDLER(1,5)
    public static WriteHandlerPtr namcos1_1_banked_area5_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		if( namcos1_banks[1][5].bank_handler_w != null)
		{
			(namcos1_banks[1][5].bank_handler_w).handler( offset+ namcos1_banks[1][5].bank_offset,data );
			return;
		}
		namcos1_banks[1][5].bank_pointer.write(offset, data);
    } };
    
    //	MW_HANDLER(1,6)
    public static WriteHandlerPtr namcos1_1_banked_area6_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		if( namcos1_banks[1][6].bank_handler_w != null)
		{
			(namcos1_banks[1][6].bank_handler_w).handler( offset+ namcos1_banks[1][6].bank_offset,data );
			return;
		}
		namcos1_banks[1][6].bank_pointer.write(offset, data);
    } };
    
/*TODO*///	#undef MW_HANDLER
	
	/*******************************************************************************
	*																			   *
	*	63701 MCU emulation (CUS64) 											   *
	*																			   *
	*******************************************************************************/
	
	static int mcu_patch_data;
	
	public static WriteHandlerPtr namcos1_cpu_control_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	//	logerror("reset control pc=%04x %02x\n",cpu_get_pc(),data);
		if(( (data&1)^namcos1_reset) != 0)
		{
			namcos1_reset = data&1;
			if (namcos1_reset != 0)
			{
				cpu_set_reset_line(1,CLEAR_LINE);
				cpu_set_reset_line(2,CLEAR_LINE);
				cpu_set_reset_line(3,CLEAR_LINE);
				mcu_patch_data = 0;
			}
			else
			{
				cpu_set_reset_line(1,ASSERT_LINE);
				cpu_set_reset_line(2,ASSERT_LINE);
				cpu_set_reset_line(3,ASSERT_LINE);
			}
		}
	} };
	
	/*******************************************************************************
	*																			   *
	*	Sound banking emulation (CUS121)										   *
	*																			   *
	*******************************************************************************/
	
	public static WriteHandlerPtr namcos1_sound_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU3);
		int bank = ( data >> 4 ) & 0x07;
	
		cpu_setbank( 1, new UBytePtr(RAM,  0x0c000 + ( 0x4000 * bank ) ) );
	} };
	
	/*******************************************************************************
	*																			   *
	*	CPU idling spinlock routine 											   *
	*																			   *
	*******************************************************************************/
	static UBytePtr sound_spinlock_ram=new UBytePtr();
	static int sound_spinlock_pc;

	/* sound cpu */
	public static ReadHandlerPtr namcos1_sound_spinlock_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		if(cpu_get_pc()==sound_spinlock_pc && sound_spinlock_ram == null)
			cpu_spinuntil_int();
		return sound_spinlock_ram.read();
	} };
	
	/*******************************************************************************
	*																			   *
	*	MCU banking emulation and patch 										   *
	*																			   *
	*******************************************************************************/
	
	/* mcu banked rom area select */
	public static WriteHandlerPtr namcos1_mcu_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int addr;
		/* bit 2-7 : chip select line of ROM chip */
		switch(data&0xfc)
		{
		case 0xf8: addr = 0x10000; break; /* bit 2 : ROM 0 */
		case 0xf4: addr = 0x30000; break; /* bit 3 : ROM 1 */
		case 0xec: addr = 0x50000; break; /* bit 4 : ROM 2 */
		case 0xdc: addr = 0x70000; break; /* bit 5 : ROM 3 */
		case 0xbc: addr = 0x90000; break; /* bit 6 : ROM 4 */
		case 0x7c: addr = 0xb0000; break; /* bit 7 : ROM 5 */
		default:   addr = 0x100000; /* illegal */
		}
		/* bit 0-1 : address line A15-A16 */
		addr += (data&3)*0x8000;
		if( addr >= memory_region_length(REGION_CPU4))
		{
			logerror("unmapped mcu bank selected pc=%04x bank=%02x\n",cpu_get_pc(),data);
			addr = 0x4000;
		}
		cpu_setbank( 4, new UBytePtr(memory_region(REGION_CPU4), addr) );
	} };
	
	/* This point is very obscure, but i havent found any better way yet. */
	/* Works with all games so far. 									  */
	
	/* patch points of memory address */
	/* CPU0/1 bank[17f][1000] */
	/* CPU2   [7000]	  */
	/* CPU3   [c000]	  */
	
	/* This memory point should be set $A6 by anywhere, but 		*/
	/* I found set $A6 only initialize in MCU						*/
	/* This patch kill write this data by MCU case $A6 to xx(clear) */
	
	
	public static WriteHandlerPtr namcos1_mcu_patch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		//logerror("mcu C000 write pc=%04x data=%02x\n",cpu_get_pc(),data);
		if(mcu_patch_data == 0xa6) return;
		mcu_patch_data = data;
	
		mwh_bank3.handler( offset, data );
	} };
	
	/*******************************************************************************
	*																			   *
	*	Initialization															   *
	*																			   *
	*******************************************************************************/
	
	static opbase_handlerPtr namcos1_setopbase_0 = new opbase_handlerPtr() {
            @Override
            public int handler(int address) {
                int bank = (address>>13)&7;
		OP_RAM = new UBytePtr(namcos1_banks[0][bank].bank_pointer, namcos1_banks[0][bank].bank_pointer.offset- (bank<<13));
                OP_ROM = new UBytePtr(namcos1_banks[0][bank].bank_pointer, namcos1_banks[0][bank].bank_pointer.offset- (bank<<13));
		/* memory.c output warning - op-code execute on mapped i/o	*/
		/* but it is necessary to continue cpu_setOPbase16 function */
		/* for update current operationhardware(ophw) code			*/
		return address;
            }
        };
	
	
	static opbase_handlerPtr namcos1_setopbase_1 = new opbase_handlerPtr() {
            @Override
            public int handler(int address) {
		int bank = (address>>13)&7;
		OP_RAM = new UBytePtr(namcos1_banks[1][bank].bank_pointer, namcos1_banks[1][bank].bank_pointer.offset- (bank<<13));
                OP_ROM = new UBytePtr(namcos1_banks[1][bank].bank_pointer, namcos1_banks[1][bank].bank_pointer.offset- (bank<<13));
		/* memory.c output warning - op-code execute on mapped i/o	*/
		/* but it is necessary to continue cpu_setOPbase16 function */
		/* for update current operationhardware(ophw) code			*/
		return address;
            }
        };
	
	static void namcos1_install_bank(int start,int end,ReadHandlerPtr hr,WriteHandlerPtr hw,
				  int offset,UBytePtr pointer)
	{
		int i;
		for(i=start;i<=end;i++)
		{
			namcos1_bank_element[i] = new bankhandler();
                        
                        namcos1_bank_element[i].bank_handler_r = hr;
			namcos1_bank_element[i].bank_handler_w = hw;
			namcos1_bank_element[i].bank_offset    = offset;
			namcos1_bank_element[i].bank_pointer   = pointer;
			offset	+= 0x2000;
			if (pointer != null) pointer.inc( 0x2000 );
		}
	}
	
	static void namcos1_install_rom_bank(int start,int end,int size,int offset)
	{
		UBytePtr BROM = memory_region(REGION_USER1);
		int step = size/0x2000;
		while(start < end)
		{
			namcos1_install_bank(start,start+step-1,null,rom_w,0,new UBytePtr(BROM, offset));
			start += step;
		}
	}
	
	static void namcos1_build_banks(ReadHandlerPtr key_r,WriteHandlerPtr key_w)
	{
		int i;
	
		/* S1 RAM pointer set */
		s1ram = memory_region(REGION_USER2);
	
		/* clear all banks to unknown area */
		for(i=0;i<NAMCOS1_MAX_BANK;i++)
			namcos1_install_bank(i,i,unknown_r,unknown_w,0,null);
	
		/* RAM 6 banks - palette */
		namcos1_install_bank(0x170,0x172,namcos1_paletteram_r,namcos1_paletteram_w,0,s1ram);
		/* RAM 6 banks - work ram */
		namcos1_install_bank(0x173,0x173,null,null,0,new UBytePtr(s1ram, 0x6000));
		/* RAM 5 banks - videoram */
		namcos1_install_bank(0x178,0x17b,namcos1_videoram_r,namcos1_videoram_w,0,null);
		/* key chip bank (rev1_key_w / rev2_key_w ) */
		namcos1_install_bank(0x17c,0x17c,key_r,key_w,0,null);
		/* RAM 7 banks - display control, playfields, sprites */
		namcos1_install_bank(0x17e,0x17e,null,namcos1_videocontrol_w,0,new UBytePtr(s1ram, 0x8000));
		/* RAM 1 shared ram, PSG device */
		namcos1_install_bank(0x17f,0x17f,soundram_r,soundram_w,0,namco_wavedata);
		/* RAM 3 banks */
		namcos1_install_bank(0x180,0x183,null,null,0,new UBytePtr(s1ram, 0xc000));
		/* PRG0 */
		namcos1_install_rom_bank(0x200,0x23f,0x20000 , 0xe0000);
		/* PRG1 */
		namcos1_install_rom_bank(0x240,0x27f,0x20000 , 0xc0000);
		/* PRG2 */
		namcos1_install_rom_bank(0x280,0x2bf,0x20000 , 0xa0000);
		/* PRG3 */
		namcos1_install_rom_bank(0x2c0,0x2ff,0x20000 , 0x80000);
		/* PRG4 */
		namcos1_install_rom_bank(0x300,0x33f,0x20000 , 0x60000);
		/* PRG5 */
		namcos1_install_rom_bank(0x340,0x37f,0x20000 , 0x40000);
		/* PRG6 */
		namcos1_install_rom_bank(0x380,0x3bf,0x20000 , 0x20000);
		/* PRG7 */
		namcos1_install_rom_bank(0x3c0,0x3ff,0x20000 , 0x00000);
	}
	
	public static InitMachinePtr init_namcos1 = new InitMachinePtr() { public void handler()  {
	
		int oldcpu = cpu_getactivecpu(), i;
	
		/* Point all of our bankhandlers to the error handlers */
		for ( i = 0; i < 8; i++ ) {
                        namcos1_banks[0][i] = new bankhandler();
			
                        namcos1_banks[0][i].bank_handler_r = unknown_r;
			namcos1_banks[0][i].bank_handler_w = unknown_w;
			namcos1_banks[0][i].bank_offset = 0;
                        
                        namcos1_banks[1][i] = new bankhandler();
			
                        namcos1_banks[1][i].bank_handler_r = unknown_r;
			namcos1_banks[1][i].bank_handler_w = unknown_w;
			namcos1_banks[1][i].bank_offset = 0;
		}
	
		/* Prepare code for Cpu 0 */
		cpu_setactivecpu( 0 );
		namcos1_bankswitch_w.handler( 0x0e00, 0x03 ); /* bank7 = 0x3ff(PRG7) */
		namcos1_bankswitch_w.handler( 0x0e01, 0xff );
	
		/* Prepare code for Cpu 1 */
		cpu_setactivecpu( 1 );
		namcos1_bankswitch_w.handler( 0x0e00, 0x03);
		namcos1_bankswitch_w.handler( 0x0e01, 0xff);
	
		namcos1_cpu1_banklatch = 0x03ff;
	
		/* reset starting Cpu */
		cpu_setactivecpu( oldcpu );
	
		/* Point mcu & sound shared RAM to destination */
		{
			UBytePtr RAM = new UBytePtr(namco_wavedata, 0x1000); /* Ram 1, bank 1, offset 0x1000 */
			cpu_setbank( 2, RAM );
			cpu_setbank( 3, RAM );
		}
	
		/* In case we had some cpu's suspended, resume them now */
		cpu_set_reset_line(1,ASSERT_LINE);
		cpu_set_reset_line(2,ASSERT_LINE);
		cpu_set_reset_line(3,ASSERT_LINE);
	
		namcos1_reset = 0;
		/* mcu patch data clear */
		mcu_patch_data = 0;
	
		berabohm_input_counter = 4;	/* for berabohm pressure sensitive buttons */
	} };
	
	
	/*******************************************************************************
	*																			   *
	*	driver specific initialize routine										   *
	*																			   *
	*******************************************************************************/
	static class namcos1_slice_timer
	{
		public int sync_cpu;	/* synchronus cpu attribute */
		public int sliceHz;	/* slice cycle				*/
		public int delayHz;	/* delay>=0 : delay cycle	*/
						/* delay<0	: slide cycle	*/
                
                public namcos1_slice_timer(int sync_cpu, int sliceHz, int delayHz) {
                    this.sync_cpu=sync_cpu;
                    this.sliceHz=sliceHz;
                    this.delayHz=delayHz;
                }
	};
	
	public static class namcos1_specific
	{
		/* keychip */
		public int key_id_query , key_id;
		public ReadHandlerPtr key_r;
		public WriteHandlerPtr key_w;
		/* cpu slice timer */
		public namcos1_slice_timer[] slice_timer;
		/* optimize flag , use tilemap for playfield */
		public int tilemap_use;
                
                public namcos1_specific(int key_id_query , int key_id, ReadHandlerPtr key_r, WriteHandlerPtr key_w, namcos1_slice_timer[] _namcos1_slice_timer, int tilemap_use){
                   this.key_id_query=key_id_query;
                   this.key_id=key_id;
                   this.key_r=key_r;
                   this.key_w=key_w;
                   this.slice_timer=_namcos1_slice_timer;
                   this.tilemap_use=tilemap_use;
                }
	};
	
	static void namcos1_driver_init(namcos1_specific specific )
	{
		/* keychip id */
		key_id_query = specific.key_id_query;
		key_id		 = specific.key_id;
	
		/* tilemap use optimize option */
/*TODO*///		namcos1_set_optimize( specific.tilemap_use );
	
		/* build bank elements */
		namcos1_build_banks(specific.key_r,specific.key_w);
	
		/* override opcode handling for extended memory bank handler */
		cpu_setOPbaseoverride( 0,namcos1_setopbase_0 );
		cpu_setOPbaseoverride( 1,namcos1_setopbase_1 );
	
		/* sound cpu speedup optimize (auto detect) */
		{
			UBytePtr RAM = memory_region(REGION_CPU3); /* sound cpu */
			int addr,flag_ptr;
	
			for(addr=0xd000;addr<0xd0ff;addr++)
			{
				if(RAM.read(addr+0)==0xb6 &&   /* lda xxxx */
				   RAM.read(addr+3)==0x27 &&   /* BEQ addr */
				   RAM.read(addr+4)==0xfb )
				{
					flag_ptr = RAM.read(addr+1)*256 + RAM.read(addr+2);
					if(flag_ptr>0x5140 && flag_ptr<0x5400)
					{
						sound_spinlock_pc	= addr+3;
						sound_spinlock_ram	= install_mem_read_handler(2,flag_ptr,flag_ptr,namcos1_sound_spinlock_r);
						logerror("Set sound cpu spinlock : pc=%04x , addr = %04x\n",sound_spinlock_pc,flag_ptr);
						break;
					}
				}
			}
		}
/*TODO*///	#if NEW_TIMER
/*TODO*///		/* all cpu's does not need synchronization to all timers */
/*TODO*///		cpu_set_full_synchronize(SYNC_NO_CPU);
/*TODO*///		{
/*TODO*///			const struct namcos1_slice_timer *slice = specific.slice_timer;
/*TODO*///			while(slice.sync_cpu != SYNC_NO_CPU)
/*TODO*///			{
/*TODO*///				/* start CPU slice timer */
/*TODO*///				cpu_start_extend_time_slice(slice.sync_cpu,
/*TODO*///					TIME_IN_HZ(slice.delayHz),TIME_IN_HZ(slice.sliceHz) );
/*TODO*///				slice++;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	#else
		/* compatible with old timer system */
		timer_pulse(TIME_IN_HZ(60*25),0,null);
/*TODO*///	#endif
	}
	
/*TODO*///	#if NEW_TIMER
/*TODO*///	/* normaly CPU slice optimize */
/*TODO*///	/* slice order is 0:2:1:x:0:3:1:x */
/*TODO*///	static const struct namcos1_slice_timer normal_slice[]={
/*TODO*///		{ SYNC_2CPU(0,1),60*20,-60*20*2 },	/* CPU 0,1 20/vblank , slide slice */
/*TODO*///		{ SYNC_2CPU(2,3),60*5,-(60*5*2+60*20*4) },	/* CPU 2,3 10/vblank */
/*TODO*///		{ SYNC_NO_CPU }
/*TODO*///	};
/*TODO*///	#else
	static namcos1_slice_timer normal_slice[]={null};
/*TODO*///	#endif
	
	/*******************************************************************************
	*	Shadowland / Youkai Douchuuki specific									   *
	*******************************************************************************/
	public static InitDriverPtr init_shadowld = new InitDriverPtr() { public void handler() 
	{
		namcos1_specific shadowld_specific= new namcos1_specific
		(
			0x00,0x00,				/* key query , key id */
			rev1_key_r,rev1_key_w,	/* key handler */
			normal_slice,			/* CPU slice normal */
			1						/* use tilemap flag : speedup optimize */
		);
		namcos1_driver_init(shadowld_specific);
	} };
	
/*TODO*///	/*******************************************************************************
/*TODO*///	*	Dragon Spirit specific													   *
/*TODO*///	*******************************************************************************/
/*TODO*///	public static InitDriverPtr init_dspirit = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		const struct namcos1_specific dspirit_specific=
/*TODO*///		{
/*TODO*///			0x00,0x36,						/* key query , key id */
/*TODO*///			dspirit_key_r,dspirit_key_w,	/* key handler */
/*TODO*///			normal_slice,					/* CPU slice normal */
/*TODO*///			1								/* use tilemap flag : speedup optimize */
/*TODO*///		};
/*TODO*///		namcos1_driver_init(&dspirit_specific);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************************************
/*TODO*///	*	Quester specific														   *
/*TODO*///	*******************************************************************************/
/*TODO*///	public static InitDriverPtr init_quester = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		const struct namcos1_specific quester_specific=
/*TODO*///		{
/*TODO*///			0x00,0x00,				/* key query , key id */
/*TODO*///			rev1_key_r,rev1_key_w,	/* key handler */
/*TODO*///			normal_slice,			/* CPU slice normal */
/*TODO*///			1						/* use tilemap flag : speedup optimize */
/*TODO*///		};
/*TODO*///		namcos1_driver_init(&quester_specific);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************************************
/*TODO*///	*	Blazer specific 														   *
/*TODO*///	*******************************************************************************/
/*TODO*///	public static InitDriverPtr init_blazer = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		const struct namcos1_specific blazer_specific=
/*TODO*///		{
/*TODO*///			0x00,0x13,					/* key query , key id */
/*TODO*///			blazer_key_r,blazer_key_w,	/* key handler */
/*TODO*///			normal_slice,				/* CPU slice normal */
/*TODO*///			1							/* use tilemap flag : speedup optimize */
/*TODO*///		};
/*TODO*///		namcos1_driver_init(&blazer_specific);
/*TODO*///	} };
	
	/*******************************************************************************
	*	Pac-Mania / Pac-Mania (Japan) specific									   *
	*******************************************************************************/
	public static InitDriverPtr init_pacmania = new InitDriverPtr() { public void handler() 
	{
		namcos1_specific pacmania_specific=new namcos1_specific
		(
			0x4b,0x12,				/* key query , key id */
			rev1_key_r,rev1_key_w,	/* key handler */
			normal_slice,			/* CPU slice normal */
			1						/* use tilemap flag : speedup optimize */
		);
		namcos1_driver_init(pacmania_specific);
	} };
	
	/*******************************************************************************
	*	Galaga '88 / Galaga '88 (Japan) specific								   *
	*******************************************************************************/
	public static InitDriverPtr init_galaga88 = new InitDriverPtr() { public void handler() 
	{
		namcos1_specific galaga88_specific=new namcos1_specific
		(
			0x2d,0x31,				/* key query , key id */
			rev1_key_r,rev1_key_w,	/* key handler */
			normal_slice,			/* CPU slice normal */
			1						/* use tilemap flag : speedup optimize */
		);
		namcos1_driver_init(galaga88_specific);
	} };
	
/*TODO*///	/*******************************************************************************
/*TODO*///	*	World Stadium specific													   *
/*TODO*///	*******************************************************************************/
/*TODO*///	public static InitDriverPtr init_ws = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		const struct namcos1_specific ws_specific=
/*TODO*///		{
/*TODO*///			0xd3,0x07,				/* key query , key id */
/*TODO*///			ws_key_r,ws_key_w,		/* key handler */
/*TODO*///			normal_slice,			/* CPU slice normal */
/*TODO*///			1						/* use tilemap flag : speedup optimize */
/*TODO*///		};
/*TODO*///		namcos1_driver_init(&ws_specific);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************************************
/*TODO*///	*	Beraboh Man specific													   *
/*TODO*///	*******************************************************************************/
/*TODO*///	public static ReadHandlerPtr berabohm_buttons_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int res;
/*TODO*///	
/*TODO*///	
/*TODO*///		if (offset == 0)
/*TODO*///		{
/*TODO*///			if (berabohm_input_counter == 0) res = readinputport(0);
/*TODO*///			else
/*TODO*///			{
/*TODO*///				static int counter[4];
/*TODO*///	
/*TODO*///				res = readinputport(4 + (berabohm_input_counter-1));
/*TODO*///				if ((res & 0x80) != 0)
/*TODO*///				{
/*TODO*///					if (counter[berabohm_input_counter-1] >= 0)
/*TODO*///	//					res = 0x40 | counter[berabohm_input_counter-1];	I can't get max power with this...
/*TODO*///						res = 0x40 | (counter[berabohm_input_counter-1]>>1);
/*TODO*///					else
/*TODO*///					{
/*TODO*///						if ((res & 0x40) != 0) res = 0x40;
/*TODO*///						else res = 0x00;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else if ((res & 0x40) != 0)
/*TODO*///				{
/*TODO*///					if (counter[berabohm_input_counter-1] < 0x3f)
/*TODO*///					{
/*TODO*///						counter[berabohm_input_counter-1]++;
/*TODO*///						res = 0x00;
/*TODO*///					}
/*TODO*///					else res = 0x7f;
/*TODO*///				}
/*TODO*///				else
/*TODO*///					counter[berabohm_input_counter-1] = -1;
/*TODO*///			}
/*TODO*///			berabohm_input_counter = (berabohm_input_counter+1) % 5;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			static int clk;
/*TODO*///	
/*TODO*///			res = 0;
/*TODO*///			clk++;
/*TODO*///			if ((clk & 1) != 0) res |= 0x40;
/*TODO*///			else if (berabohm_input_counter == 4) res |= 0x10;
/*TODO*///	
/*TODO*///			res |= (readinputport(1) & 0x8f);
/*TODO*///		}
/*TODO*///	
/*TODO*///		return res;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static InitDriverPtr init_berabohm = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		const struct namcos1_specific berabohm_specific=
/*TODO*///		{
/*TODO*///			0x00,0x00,				/* key query , key id */
/*TODO*///			rev1_key_r,rev1_key_w,	/* key handler */
/*TODO*///			normal_slice,			/* CPU slice normal */
/*TODO*///			1						/* use tilemap flag : speedup optimize */
/*TODO*///		};
/*TODO*///		namcos1_driver_init(&berabohm_specific);
/*TODO*///		install_mem_read_handler(3,0x1400,0x1401,berabohm_buttons_r);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************************************
/*TODO*///	*	Alice in Wonderland / Marchen Maze specific 							   *
/*TODO*///	*******************************************************************************/
/*TODO*///	public static InitDriverPtr init_alice = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		const struct namcos1_specific alice_specific=
/*TODO*///		{
/*TODO*///			0x5b,0x25,				/* key query , key id */
/*TODO*///			rev1_key_r,rev1_key_w,	/* key handler */
/*TODO*///			normal_slice,			/* CPU slice normal */
/*TODO*///			1						/* use tilemap flag : speedup optimize */
/*TODO*///		};
/*TODO*///		namcos1_driver_init(&alice_specific);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************************************
/*TODO*///	*	Bakutotsu Kijuutei specific 											   *
/*TODO*///	*******************************************************************************/
/*TODO*///	public static InitDriverPtr init_bakutotu = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		const struct namcos1_specific bakutotu_specific=
/*TODO*///		{
/*TODO*///			0x03,0x22,				/* key query , key id */
/*TODO*///			rev1_key_r,rev1_key_w,	/* key handler */
/*TODO*///			normal_slice,			/* CPU slice normal */
/*TODO*///			1						/* use tilemap flag : speedup optimize */
/*TODO*///		};
/*TODO*///		namcos1_driver_init(&bakutotu_specific);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************************************
/*TODO*///	*	World Court specific													   *
/*TODO*///	*******************************************************************************/
/*TODO*///	public static InitDriverPtr init_wldcourt = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		const struct namcos1_specific worldcourt_specific=
/*TODO*///		{
/*TODO*///			0x00,0x35,				/* key query , key id */
/*TODO*///			rev2_key_r,rev2_key_w,	/* key handler */
/*TODO*///			normal_slice,			/* CPU slice normal */
/*TODO*///			1						/* use tilemap flag : speedup optimize */
/*TODO*///		};
/*TODO*///		namcos1_driver_init(&worldcourt_specific);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************************************
/*TODO*///	*	Splatter House specific 												   *
/*TODO*///	*******************************************************************************/
/*TODO*///	public static InitDriverPtr init_splatter = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		const struct namcos1_specific splatter_specific=
/*TODO*///		{
/*TODO*///			0x00,0x00,						/* key query , key id */
/*TODO*///			splatter_key_r,splatter_key_w,	/* key handler */
/*TODO*///			normal_slice,					/* CPU slice normal */
/*TODO*///			1								/* use tilemap flag : speedup optimize */
/*TODO*///		};
/*TODO*///		namcos1_driver_init(&splatter_specific);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************************************
/*TODO*///	*	Face Off specific														   *
/*TODO*///	*******************************************************************************/
/*TODO*///	public static InitDriverPtr init_faceoff = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		const struct namcos1_specific faceoff_specific=
/*TODO*///		{
/*TODO*///			0x00,0x00,				/* key query , key id */
/*TODO*///			rev1_key_r,rev1_key_w,	/* key handler */
/*TODO*///			normal_slice,			/* CPU slice normal */
/*TODO*///			1						/* use tilemap flag : speedup optimize */
/*TODO*///		};
/*TODO*///		namcos1_driver_init(&faceoff_specific);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************************************
/*TODO*///	*	Rompers specific														   *
/*TODO*///	*******************************************************************************/
/*TODO*///	public static InitDriverPtr init_rompers = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		const struct namcos1_specific rompers_specific=
/*TODO*///		{
/*TODO*///			0x00,0x00,				/* key query , key id */
/*TODO*///			rev1_key_r,rev1_key_w,	/* key handler */
/*TODO*///			normal_slice,			/* CPU slice normal */
/*TODO*///			1						/* use tilemap flag : speedup optimize */
/*TODO*///		};
/*TODO*///		namcos1_driver_init(&rompers_specific);
/*TODO*///		key[0x70] = 0xb6;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************************************
/*TODO*///	*	Blast Off specific														   *
/*TODO*///	*******************************************************************************/
/*TODO*///	public static InitDriverPtr init_blastoff = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		const struct namcos1_specific blastoff_specific=
/*TODO*///		{
/*TODO*///			0x00,0x00,				/* key query , key id */
/*TODO*///			rev1_key_r,rev1_key_w,	/* key handler */
/*TODO*///			normal_slice,			/* CPU slice normal */
/*TODO*///			1						/* use tilemap flag : speedup optimize */
/*TODO*///		};
/*TODO*///		namcos1_driver_init(&blastoff_specific);
/*TODO*///		key[0] = 0xb7;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************************************
/*TODO*///	*	World Stadium '89 specific                                                 *
/*TODO*///	*******************************************************************************/
/*TODO*///	public static InitDriverPtr init_ws89 = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		const struct namcos1_specific ws89_specific=
/*TODO*///		{
/*TODO*///			0x00,0x00,				/* key query , key id */
/*TODO*///			rev1_key_r,rev1_key_w,	/* key handler */
/*TODO*///			normal_slice,			/* CPU slice normal */
/*TODO*///			1						/* use tilemap flag : speedup optimize */
/*TODO*///		};
/*TODO*///		namcos1_driver_init(&ws89_specific);
/*TODO*///	
/*TODO*///		key[0x20] = 0xb8;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************************************
/*TODO*///	*	Dangerous Seed specific 												   *
/*TODO*///	*******************************************************************************/
/*TODO*///	public static InitDriverPtr init_dangseed = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		const struct namcos1_specific dangseed_specific=
/*TODO*///		{
/*TODO*///			0x00,0x34,						/* key query , key id */
/*TODO*///			dangseed_key_r,dangseed_key_w,	/* key handler */
/*TODO*///			normal_slice,					/* CPU slice normal */
/*TODO*///			1								/* use tilemap flag : speedup optimize */
/*TODO*///		};
/*TODO*///		namcos1_driver_init(&dangseed_specific);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************************************
/*TODO*///	*	World Stadium '90 specific                                                 *
/*TODO*///	*******************************************************************************/
/*TODO*///	public static InitDriverPtr init_ws90 = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		const struct namcos1_specific ws90_specific=
/*TODO*///		{
/*TODO*///			0x00,0x00,				/* key query , key id */
/*TODO*///			rev1_key_r,rev1_key_w,	/* key handler */
/*TODO*///			normal_slice,			/* CPU slice normal */
/*TODO*///			1						/* use tilemap flag : speedup optimize */
/*TODO*///		};
/*TODO*///		namcos1_driver_init(&ws90_specific);
/*TODO*///	
/*TODO*///		key[0x47] = 0x36;
/*TODO*///		key[0x40] = 0x36;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************************************
/*TODO*///	*	Pistol Daimyo no Bouken specific										   *
/*TODO*///	*******************************************************************************/
/*TODO*///	public static InitDriverPtr init_pistoldm = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		const struct namcos1_specific pistoldm_specific=
/*TODO*///		{
/*TODO*///			0x00,0x00,				/* key query , key id */
/*TODO*///			rev1_key_r,rev1_key_w,	/* key handler */
/*TODO*///			normal_slice,			/* CPU slice normal */
/*TODO*///			1						/* use tilemap flag : speedup optimize */
/*TODO*///		};
/*TODO*///		namcos1_driver_init(&pistoldm_specific);
/*TODO*///		//key[0x17] = ;
/*TODO*///		//key[0x07] = ;
/*TODO*///		key[0x43] = 0x35;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************************************
/*TODO*///	*	Souko Ban DX specific													   *
/*TODO*///	*******************************************************************************/
/*TODO*///	public static InitDriverPtr init_soukobdx = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		const struct namcos1_specific soukobdx_specific=
/*TODO*///		{
/*TODO*///			0x00,0x00,				/* key query , key id */
/*TODO*///			rev1_key_r,rev1_key_w,	/* key handler */
/*TODO*///			normal_slice,			/* CPU slice normal */
/*TODO*///			1						/* use tilemap flag : speedup optimize */
/*TODO*///		};
/*TODO*///		namcos1_driver_init(&soukobdx_specific);
/*TODO*///		//key[0x27] = ;
/*TODO*///		//key[0x07] = ;
/*TODO*///		key[0x43] = 0x37;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************************************
/*TODO*///	*	Puzzle Club specific													   *
/*TODO*///	*******************************************************************************/
/*TODO*///	public static InitDriverPtr init_puzlclub = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		const struct namcos1_specific puzlclub_specific=
/*TODO*///		{
/*TODO*///			0x00,0x00,				/* key query , key id */
/*TODO*///			rev1_key_r,rev1_key_w,	/* key handler */
/*TODO*///			normal_slice,			/* CPU slice normal */
/*TODO*///			1						/* use tilemap flag : speedup optimize */
/*TODO*///		};
/*TODO*///		namcos1_driver_init(&puzlclub_specific);
/*TODO*///		key[0x03] = 0x35;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	/*******************************************************************************
/*TODO*///	*	Tank Force specific 													   *
/*TODO*///	*******************************************************************************/
/*TODO*///	public static InitDriverPtr init_tankfrce = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		const struct namcos1_specific tankfrce_specific=
/*TODO*///		{
/*TODO*///			0x00,0x00,				/* key query , key id */
/*TODO*///			rev1_key_r,rev1_key_w,	/* key handler */
/*TODO*///			normal_slice,			/* CPU slice normal */
/*TODO*///			1						/* use tilemap flag : speedup optimize */
/*TODO*///		};
/*TODO*///		namcos1_driver_init(&tankfrce_specific);
/*TODO*///		//key[0x57] = ;
/*TODO*///		//key[0x17] = ;
/*TODO*///		key[0x2b] = 0xb9;
/*TODO*///		key[0x50] = 0xb9;
/*TODO*///	} };
}
