/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 */ 
package gr.codebb.arcadeflex.v036.drivers;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.punchout.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.memory.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;
import static gr.codebb.arcadeflex.v036.cpu.m6502.n2a03.*;
import gr.codebb.arcadeflex.v036.mame.sndintrfH.MachineSound;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.SOUND_NES;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.SOUND_VLM5030;
import static gr.codebb.arcadeflex.v036.sound.nes_apu.*;
import static gr.codebb.arcadeflex.v036.sound.nes_apuH.*;
import static gr.codebb.arcadeflex.v036.sound.vlm5030.*;
import static gr.codebb.arcadeflex.v036.sound.vlm5030H.*;
import static gr.codebb.arcadeflex.v036.sndhrdw.punchout.*;

public class punchout
{

        
	static UBytePtr nvram=new UBytePtr();
	static int[] nvram_size=new int[1];
	
	public static nvramPtr nvram_handler = new nvramPtr(){ public void handler(Object file,int read_or_write)
        {
		/*if (read_or_write)
			osd_fwrite(file,nvram,nvram_size);
		else
		(
			if (file)
				osd_fread(file,nvram,nvram_size);
			else
				memset(nvram,null,nvram_size);
		)*/
	}};
	
	
	public static WriteHandlerPtr punchout_2a03_reset_w = new WriteHandlerPtr() { public void handler(int offset, int data)
        {
		if ((data & 1)!=0)
			cpu_set_reset_line(1,ASSERT_LINE);
		else
			cpu_set_reset_line(1,CLEAR_LINE);
	}};
	
	static int prot_mode_sel = -1; /* Mode selector */
	static int[] prot_mem=new int[16];
	
        public static ReadHandlerPtr spunchout_prot_r = new ReadHandlerPtr() { public int handler(int offset)
        {
	
		switch ( offset ) {
			case 0x00:
				if ( prot_mode_sel == 0x0a )
					return cpu_readmem16(0xd012);
	
				if ( prot_mode_sel == 0x0b || prot_mode_sel == 0x23 )
					return cpu_readmem16(0xd7c1);
	
				return prot_mem[offset];
			//break;
	
			case 0x01:
				if ( prot_mode_sel == 0x08 ) /* PC = 0x0b6a */
					return 0x00; /* under 6 */
			break;
	
			case 0x02:
				if ( prot_mode_sel == 0x0b ) /* PC = 0x0613 */
					return 0x09; /* write "JMP (HL)"code to 0d79fh */
				if ( prot_mode_sel == 0x09 ) /* PC = 0x20f9, 0x22d9 */
					return prot_mem[offset]; /* act as registers */
			break;
	
			case 0x03:
				if ( prot_mode_sel == 0x09 ) /* PC = 0x1e4c */
					return prot_mem[offset] & 0x07; /* act as registers with mask */
			break;
	
			case 0x05:
				if ( prot_mode_sel == 0x09 ) /* PC = 0x29D1 */
					return prot_mem[offset] & 0x03; /* AND 0FH . AND 06H */
			break;
	
			case 0x06:
				if ( prot_mode_sel == 0x0b ) /* PC = 0x2dd8 */
					return 0x0a; /* E=00, HL=23E6, D = (ret and 0x0f), HL+DE = 2de6 */
	
				if ( prot_mode_sel == 0x09 ) /* PC = 0x2289 */
					return prot_mem[offset] & 0x07; /* act as registers with mask */
			break;
	
			case 0x09:
				if ( prot_mode_sel == 0x09 ) /* PC = 0x0313 */
					return ( prot_mem[15] << 4 ); /* pipe through register 0xf7 << 4 */
					/* (ret or 0x10) . (D7DF),(D7A0) - (D7DF),(D7A0) = 0d0h(ret nc) */
			break;
	
			case 0x0a:
				if ( prot_mode_sel == 0x0b ) /* PC = 0x060a */
					return 0x05; /* write "JMP (IX)"code to 0d79eh */
				if ( prot_mode_sel == 0x09 ) /* PC = 0x1bd7 */
					return prot_mem[offset] & 0x01; /* AND 0FH . AND 01H */
			break;
	
			case 0x0b:
				if ( prot_mode_sel == 0x09 ) /* PC = 0x2AA3 */
					return prot_mem[11] & 0x03;	/* AND 0FH . AND 03H */
			break;
	
			case 0x0c:
				/* PC = 0x2162 */
				/* B = 0(return value) */
				return 0x00;
			case 0x0d:
				return prot_mode_sel;
			//break;
		}
	
		if ( errorlog!=null )
			fprintf( errorlog, "Read from unknown protection? port %02x ( selector = %02x )\n", offset, prot_mode_sel );
	
		return prot_mem[offset];
	}};
	public static WriteHandlerPtr spunchout_prot_w = new WriteHandlerPtr() { public void handler(int offset, int data)
        {
	
		switch ( offset ) {
			case 0x00:
				if ( prot_mode_sel == 0x0a ) {
					cpu_writemem16(0xd012, data);
					return;
				}
	
				if ( prot_mode_sel == 0x0b || prot_mode_sel == 0x23 ) {
					cpu_writemem16(0xd7c1, data);
					return;
				}
	
				prot_mem[offset] = data;
				return;
			//break;
	
			case 0x02:
				if ( prot_mode_sel == 0x09 ) { /* PC = 0x20f7, 0x22d7 */
					prot_mem[offset] = data;
					return;
				}
			break;
	
			case 0x03:
				if ( prot_mode_sel == 0x09 ) { /* PC = 0x1e4c */
					prot_mem[offset] = data;
					return;
				}
			break;
	
			case 0x05:
				prot_mem[offset] = data;
				return;
	
			case 0x06:
				if ( prot_mode_sel == 0x09 ) { /* PC = 0x2287 */
					prot_mem[offset] = data;
					return;
				}
			break;
	
			case 0x0b:
				prot_mem[offset] = data;
				return;
	
			case 0x0d: /* PC = all over the code */
				prot_mode_sel = data;
				return;
			case 0x0f:
				prot_mem[offset] = data;
				return;
		}
	
		if ( errorlog!=null )
			fprintf( errorlog, "Wrote to unknown protection? port %02x ( %02x )\n", offset, data );
	
		prot_mem[offset] = data;
	}};
	
	public static ReadHandlerPtr spunchout_prot_0_r = new ReadHandlerPtr() { public int handler(int offset){
		return spunchout_prot_r.handler(0 );
	}};
	
	public static WriteHandlerPtr spunchout_prot_0_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		spunchout_prot_w.handler( 0, data );
	}};
	
	public static ReadHandlerPtr spunchout_prot_1_r= new ReadHandlerPtr() { public int handler(int offset){
		return spunchout_prot_r.handler( 1 );
	}};
	
	public static WriteHandlerPtr spunchout_prot_1_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		spunchout_prot_w.handler( 1, data );
	}};
	
	public static ReadHandlerPtr spunchout_prot_2_r= new ReadHandlerPtr() { public int handler(int offset){
		return spunchout_prot_r.handler( 2 );
	}};
	
	public static WriteHandlerPtr spunchout_prot_2_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		spunchout_prot_w.handler( 2, data );
	}};
	
	public static ReadHandlerPtr spunchout_prot_3_r= new ReadHandlerPtr() { public int handler(int offset){
		return spunchout_prot_r.handler( 3 );
	}};
	
	public static WriteHandlerPtr spunchout_prot_3_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		spunchout_prot_w.handler( 3, data );
	}};
	
	public static ReadHandlerPtr spunchout_prot_5_r= new ReadHandlerPtr() { public int handler(int offset){
		return spunchout_prot_r.handler( 5 );
	}};
	
	public static WriteHandlerPtr spunchout_prot_5_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		spunchout_prot_w.handler( 5, data );
	}};
	
	
	public static ReadHandlerPtr spunchout_prot_6_r= new ReadHandlerPtr() { public int handler(int offset){
		return spunchout_prot_r.handler( 6 );
	}};
	
	public static WriteHandlerPtr spunchout_prot_6_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		spunchout_prot_w.handler( 6, data );
	}};
	
	public static ReadHandlerPtr spunchout_prot_9_r= new ReadHandlerPtr() { public int handler(int offset){
		return spunchout_prot_r.handler( 9 );
	}};
	
	public static ReadHandlerPtr spunchout_prot_b_r= new ReadHandlerPtr() { public int handler(int offset){
		return spunchout_prot_r.handler( 11 );
	}};
	
	public static WriteHandlerPtr spunchout_prot_b_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		spunchout_prot_w.handler( 11, data );
	}};
	
	public static ReadHandlerPtr spunchout_prot_c_r= new ReadHandlerPtr() { public int handler(int offset){
		return spunchout_prot_r.handler( 12 );
	}};
	
	public static WriteHandlerPtr spunchout_prot_d_w = new WriteHandlerPtr() { public void handler(int offset, int data) {
		spunchout_prot_w.handler( 13, data );
	}};
	
	public static ReadHandlerPtr spunchout_prot_a_r= new ReadHandlerPtr() { public int handler(int offset){
		return spunchout_prot_r.handler( 10 );
	}};
	
	public static WriteHandlerPtr spunchout_prot_a_w = new WriteHandlerPtr() { public void handler(int offset, int data) {
		spunchout_prot_w.handler( 10, data );
	}};
	
	//#if 0
	//static int spunchout_prot_f_r( int offset ) {
	//	return spunchout_prot_r( 15 );
	//}
	//#endif
	
	public static WriteHandlerPtr spunchout_prot_f_w = new WriteHandlerPtr() { public void handler(int offset, int data) {
		spunchout_prot_w.handler( 15, data );
	}};
	

	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0xbfff, MRA_ROM ),
		new MemoryReadAddress( 0xc000, 0xc3ff, MRA_RAM ),
		new MemoryReadAddress( 0xd000, 0xffff, MRA_RAM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new MemoryWriteAddress( 0xc000, 0xc3ff, MWA_RAM, nvram, nvram_size ),
		new MemoryWriteAddress( 0xd000, 0xd7ff, MWA_RAM ),
		new MemoryWriteAddress( 0xdff0, 0xdff7, MWA_RAM, punchout_bigsprite1 ),
		new MemoryWriteAddress( 0xdff8, 0xdffc, MWA_RAM, punchout_bigsprite2 ),
		new MemoryWriteAddress( 0xdffd, 0xdffd, punchout_palettebank_w, punchout_palettebank ),
		new MemoryWriteAddress( 0xd800, 0xdfff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0xe000, 0xe7ff, punchout_bigsprite1ram_w, punchout_bigsprite1ram, punchout_bigsprite1ram_size ),
		new MemoryWriteAddress( 0xe800, 0xefff, punchout_bigsprite2ram_w, punchout_bigsprite2ram, punchout_bigsprite2ram_size ),
		new MemoryWriteAddress( 0xf000, 0xf03f, MWA_RAM, punchout_scroll ),
		new MemoryWriteAddress( 0xf000, 0xffff, punchout_videoram2_w, punchout_videoram2, punchout_videoram2_size ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static IOReadPort readport[] =
	{
		new IOReadPort( 0x00, 0x00, input_port_0_r ),
		new IOReadPort( 0x01, 0x01, input_port_1_r ),
		new IOReadPort( 0x02, 0x02, input_port_2_r ),
		new IOReadPort( 0x03, 0x03, punchout_input_3_r ),
	
		/* protection ports */
		new IOReadPort( 0x07, 0x07, spunchout_prot_0_r ),
		new IOReadPort( 0x17, 0x17, spunchout_prot_1_r ),
		new IOReadPort( 0x27, 0x27, spunchout_prot_2_r ),
		new IOReadPort( 0x37, 0x37, spunchout_prot_3_r ),
		new IOReadPort( 0x57, 0x57, spunchout_prot_5_r ),
		new IOReadPort( 0x67, 0x67, spunchout_prot_6_r ),
		new IOReadPort( 0x97, 0x97, spunchout_prot_9_r ),
		new IOReadPort( 0xa7, 0xa7, spunchout_prot_a_r ),
		new IOReadPort( 0xb7, 0xb7, spunchout_prot_b_r ),
		new IOReadPort( 0xc7, 0xc7, spunchout_prot_c_r ),
		/* new IOReadPort( 0xf7, 0xf7, spunchout_prot_f_r ), */
		new IOReadPort( -1 )	/* end of table */
	};
	
	static IOWritePort writeport[] =
	{
		new IOWritePort( 0x00, 0x01, IOWP_NOP ),	/* the 2A03 #1 is not present */
		new IOWritePort( 0x02, 0x02, soundlatch_w ),
		new IOWritePort( 0x03, 0x03, soundlatch2_w ),
		new IOWritePort( 0x04, 0x04, VLM5030_data_w ),	/* VLM5030 */
		new IOWritePort( 0x05, 0x05, IOWP_NOP ),	/* unused */
		new IOWritePort( 0x08, 0x08, interrupt_enable_w ),
		new IOWritePort( 0x09, 0x09, IOWP_NOP ),	/* watchdog reset, seldom used because 08 clears the watchdog as well */
		new IOWritePort( 0x0a, 0x0a, IOWP_NOP ),	/* ?? */
		new IOWritePort( 0x0b, 0x0b, punchout_2a03_reset_w ),
		new IOWritePort( 0x0c, 0x0c, punchout_speech_reset ),	/* VLM5030 */
		new IOWritePort( 0x0d, 0x0d, punchout_speech_st    ),	/* VLM5030 */
		new IOWritePort( 0x0e, 0x0e, punchout_speech_vcu   ),	/* VLM5030 */
		new IOWritePort( 0x0f, 0x0f, IOWP_NOP ),	/* enable NVRAM ? */
	
		new IOWritePort( 0x06, 0x06, IOWP_NOP),
	
		/* protection ports */
		new IOWritePort( 0x07, 0x07, spunchout_prot_0_w ),
		new IOWritePort( 0x17, 0x17, spunchout_prot_1_w ),
		new IOWritePort( 0x27, 0x27, spunchout_prot_2_w ),
		new IOWritePort( 0x37, 0x37, spunchout_prot_3_w ),
		new IOWritePort( 0x57, 0x57, spunchout_prot_5_w ),
		new IOWritePort( 0x67, 0x67, spunchout_prot_6_w ),
		new IOWritePort( 0xa7, 0xa7, spunchout_prot_a_w ),
		new IOWritePort( 0xb7, 0xb7, spunchout_prot_b_w ),
		new IOWritePort( 0xd7, 0xd7, spunchout_prot_d_w ),
		new IOWritePort( 0xf7, 0xf7, spunchout_prot_f_w ),
		new IOWritePort( -1 )	/* end of table */
	};
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x07ff, MRA_RAM ),
		new MemoryReadAddress( 0x4016, 0x4016, soundlatch_r ),
		new MemoryReadAddress( 0x4017, 0x4017, soundlatch2_r ),
		new MemoryReadAddress( 0x4000, 0x4017, NESPSG_0_r ),
		new MemoryReadAddress( 0xe000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x07ff, MWA_RAM ),
		new MemoryWriteAddress( 0x4000, 0x4017, NESPSG_0_w ),
		new MemoryWriteAddress( 0xe000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	
	static InputPortPtr input_ports_punchout = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_BUTTON2 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_BUTTON3 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_COIN1 );
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x00, "Easy" );
		PORT_DIPSETTING(    0x01, "Medium" );
		PORT_DIPSETTING(    0x02, "Hard" );
		PORT_DIPSETTING(    0x03, "Hardest" );
		PORT_DIPNAME( 0x0c, 0x00, "Time" );
		PORT_DIPSETTING(    0x00, "Longest" );
		PORT_DIPSETTING(    0x04, "Long" );
		PORT_DIPSETTING(    0x08, "Short" );
		PORT_DIPSETTING(    0x0c, "Shortest" );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, "Rematch at a Discount" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_SERVICE( 0x80, IP_ACTIVE_HIGH );
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x0f, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
	/*	PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );*/
	/*	PORT_DIPSETTING(    0x08, "1 Coin/2 Credits (2 min.); )*/
		PORT_DIPSETTING(    0x0d, "1 Coin/3 Credits (2 min.)");
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
	/*	PORT_DIPSETTING(    0x04, DEF_STR( "1C_2C") );*/
	/*	PORT_DIPSETTING(    0x09, DEF_STR( "1C_2C") );*/
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "Free_Play") );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );/* VLM5030 busy signal */
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_DIPNAME( 0x80, 0x00, "Copyright" );
		PORT_DIPSETTING(    0x00, "Nintendo" );
		PORT_DIPSETTING(    0x80, "Nintendo of America" );
		PORT_START(); 
	INPUT_PORTS_END(); }}; 
	
	/* same as punchout with additional duck button */
	static InputPortPtr input_ports_spnchout = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_BUTTON2 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_BUTTON3 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW,  IPT_BUTTON4 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_COIN1 );
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x00, "Easy" );
		PORT_DIPSETTING(    0x01, "Medium" );
		PORT_DIPSETTING(    0x02, "Hard" );
		PORT_DIPSETTING(    0x03, "Hardest" );
		PORT_DIPNAME( 0x0c, 0x00, "Time" );
		PORT_DIPSETTING(    0x00, "Longest" );
		PORT_DIPSETTING(    0x04, "Long" );
		PORT_DIPSETTING(    0x08, "Short" );
		PORT_DIPSETTING(    0x0c, "Shortest" );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, "Rematch at a Discount" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_SERVICE( 0x80, IP_ACTIVE_HIGH );
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x0f, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
	/*	PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );*/
	/*	PORT_DIPSETTING(    0x08, "1 Coin/2 Credits (2 min.); )*/
		PORT_DIPSETTING(    0x0d, "1 Coin/3 Credits (2 min.)"); 
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
	/*	PORT_DIPSETTING(    0x04, DEF_STR( "1C_2C") );*/
	/*	PORT_DIPSETTING(    0x09, DEF_STR( "1C_2C") );*/
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "Free_Play") );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );/* VLM5030 busy signal */
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_DIPNAME( 0x80, 0x00, "Copyright" );
		PORT_DIPSETTING(    0x00, "Nintendo" );
		PORT_DIPSETTING(    0x80, "Nintendo of America" );
		PORT_START(); 
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_armwrest = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_COIN3 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_COIN1 );
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x00, "Easy" );
		PORT_DIPSETTING(    0x01, "Medium" );
		PORT_DIPSETTING(    0x02, "Hard" );
		PORT_DIPSETTING(    0x03, "Hardest" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, "Rematches" );
		PORT_DIPSETTING(    0x40, "3" );
		PORT_DIPSETTING(    0x00, "7" );
		PORT_SERVICE( 0x80, IP_ACTIVE_HIGH );
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );/* VLM5030 busy signal */
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		1024,	/* 1024 characters */
		2,	/* 2 bits per pixel */
		new int[] { 1024*8*8, 0 },	/* the bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout armwrest_charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		2048,	/* 2048 characters */
		2,	/* 2 bits per pixel */
		new int[] { 2048*8*8, 0 },	/* the bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout armwrest_charlayout2 = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		2048,	/* 2048 characters */
		3,	/* 3 bits per pixel */
		new int[] { 2*2048*8*8, 2048*8*8, 0 },	/* the bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout charlayout1 = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		8192,	/* 8192 characters */
		3,	/* 3 bits per pixel */
		new int[] { 2*8192*8*8, 8192*8*8, 0 },	/* the bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout charlayout2 = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		4096,	/* 4096 characters */
		2,	/* 2 bits per pixel */
		new int[] { 4096*8*8, 0 },	/* the bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every char takes 8 consecutive bytes */
	);
	
	static GfxDecodeInfo punchout_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,                 0, 128 ),
		new GfxDecodeInfo( REGION_GFX2, 0, charlayout,             128*4, 128 ),
		new GfxDecodeInfo( REGION_GFX3, 0, charlayout1,      128*4+128*4,  64 ),
		new GfxDecodeInfo( REGION_GFX4, 0, charlayout2, 128*4+128*4+64*8, 128 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo armwrest_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, armwrest_charlayout,        0, 256 ),
		new GfxDecodeInfo( REGION_GFX2, 0, armwrest_charlayout2,   256*4,  64 ),
		new GfxDecodeInfo( REGION_GFX3, 0, charlayout1,       256*4+64*8,  64 ),
		new GfxDecodeInfo( REGION_GFX4, 0, charlayout2,  256*4+64*8+64*8, 128 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static  NESinterface nes_interface = new NESinterface
	(
		1,
		new int[]{ REGION_CPU2 },
		new int[]{ 50 }
        );
	
	/* filename for speech sample files */
	static String punchout_sample_names[] =
	{
		"*punchout",
		"00.wav","01.wav","02.wav","03.wav","04.wav","05.wav","06.wav","07.wav",
		"08.wav","09.wav","0a.wav","0b.wav","0c.wav","0d.wav","0e.wav","0f.wav",
		"10.wav","11.wav","12.wav","13.wav","14.wav","15.wav","16.wav","17.wav",
		"18.wav","19.wav","1a.wav","1b.wav","1c.wav","1d.wav","1e.wav","1f.wav",
		"20.wav","21.wav","22.wav","23.wav","24.wav","25.wav","26.wav","27.wav",
		"28.wav","29.wav","2a.wav","2b.wav",
		null
	};
	
	static  VLM5030interface vlm5030_interface = new VLM5030interface
        (
		3580000,    /* master clock */
		50,        /* volume       */
		REGION_SOUND1,	/* memory region of speech rom */
		0,          /* memory size of speech rom */
		0,           /* VCU pin level (default)     */
		punchout_sample_names
        );
	
	
											
	static MachineDriver machine_driver_punchout = new MachineDriver
	(																					
		/* basic machine hardware */													
		new MachineCPU[] {																				
			new MachineCPU(																			
				CPU_Z80,																
				8000000/2,	/* 4 Mhz */													
				readmem,writemem,readport,writeport,									
				nmi_interrupt,1															
			),																			
			new MachineCPU(																			
				CPU_N2A03 | CPU_AUDIO_CPU,												
				(int)N2A03_DEFAULTCLOCK,														
				sound_readmem,sound_writemem,null,null,										
				nmi_interrupt,1															
			)																			
		},																				
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */	
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */	
		null,																				
																						
		/* video hardware */															
		32*8, 60*8, new rectangle( 0*8, 32*8-1, 0*8, 60*8-1 ),										
		punchout_gfxdecodeinfo,															
		1024+1, 128*4+128*4+64*8+128*4,																
		punchout_vh_convert_color_prom,													
																						
		VIDEO_TYPE_RASTER | VIDEO_DUAL_MONITOR,											
		null,																				
		punchout_vh_start,																	
		punchout_vh_stop,																
		punchout_vh_screenrefresh,															
																						
		/* sound hardware */
                0, 0, 0, 0,
                new MachineSound[] {
			new MachineSound(
				SOUND_NES,																
				nes_interface
			),
                        new MachineSound(
                                SOUND_VLM5030,															
				vlm5030_interface)
		},
		/*0,0,0,0,																		
		{																				
			{																			
				SOUND_NES,																
				&nes_interface															
			},																			
			{																			
				SOUND_VLM5030,															
				&vlm5030_interface														
			}																			
		},*/																				
																						
		nvram_handler																	
	);
	static MachineDriver machine_driver_armwrest = new MachineDriver
	(																					
		/* basic machine hardware */													
		new MachineCPU[] {																				
			new MachineCPU(																			
				CPU_Z80,																
				8000000/2,	/* 4 Mhz */													
				readmem,writemem,readport,writeport,									
				nmi_interrupt,1															
			),																			
			new MachineCPU(																			
				CPU_N2A03 | CPU_AUDIO_CPU,												
				(int)N2A03_DEFAULTCLOCK,														
				sound_readmem,sound_writemem,null,null,										
				nmi_interrupt,1															
			)																			
		},																				
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */	
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */	
		null,																				
																						
		/* video hardware */															
		32*8, 60*8, new rectangle( 0*8, 32*8-1, 0*8, 60*8-1 ),										
		armwrest_gfxdecodeinfo,															
		1024+1, 256*4+64*8+64*8+128*4,																
		armwrest_vh_convert_color_prom,													
																						
		VIDEO_TYPE_RASTER | VIDEO_DUAL_MONITOR,											
		null,																				
		armwrest_vh_start,																	
		punchout_vh_stop,																
		armwrest_vh_screenrefresh,															
																						
		/* sound hardware */
                0, 0, 0, 0,
                null,
		/*0,0,0,0,																		
		{																				
			{																			
				SOUND_NES,																
				&nes_interface															
			},																			
			{																			
				SOUND_VLM5030,															
				&vlm5030_interface														
			}																			
		},*/																				
																						
		nvram_handler																	
	);
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_punchout = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "chp1-c.8l",    0x0000, 0x2000, 0xa4003adc );
		ROM_LOAD( "chp1-c.8k",    0x2000, 0x2000, 0x745ecf40 );
		ROM_LOAD( "chp1-c.8j",    0x4000, 0x2000, 0x7a7f870e );
		ROM_LOAD( "chp1-c.8h",    0x6000, 0x2000, 0x5d8123d7 );
		ROM_LOAD( "chp1-c.8f",    0x8000, 0x4000, 0xc8a55ddb );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the sound CPU */
		ROM_LOAD( "chp1-c.4k",    0xe000, 0x2000, 0xcb6ef376 );
	
		ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "chp1-b.4c",    0x00000, 0x2000, 0xe26dc8b3 );/* chars #1 */
		ROM_LOAD( "chp1-b.4d",    0x02000, 0x2000, 0xdd1310ca );
	
		ROM_REGION( 0x04000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "chp1-b.4a",    0x00000, 0x2000, 0x20fb4829 );/* chars #2 */
		ROM_LOAD( "chp1-b.4b",    0x02000, 0x2000, 0xedc34594 );
	
		ROM_REGION( 0x30000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "chp1-v.2r",    0x00000, 0x4000, 0xbd1d4b2e );/* chars #3 */
		ROM_LOAD( "chp1-v.2t",    0x04000, 0x4000, 0xdd9a688a );
		ROM_LOAD( "chp1-v.2u",    0x08000, 0x2000, 0xda6a3c4b );
		/* 0a000-0bfff empty (space for 16k ROM) */
		ROM_LOAD( "chp1-v.2v",    0x0c000, 0x2000, 0x8c734a67 );
		/* 0e000-0ffff empty (space for 16k ROM) */
		ROM_LOAD( "chp1-v.3r",    0x10000, 0x4000, 0x2e74ad1d );
		ROM_LOAD( "chp1-v.3t",    0x14000, 0x4000, 0x630ba9fb );
		ROM_LOAD( "chp1-v.3u",    0x18000, 0x2000, 0x6440321d );
		/* 1a000-1bfff empty (space for 16k ROM) */
		ROM_LOAD( "chp1-v.3v",    0x1c000, 0x2000, 0xbb7b7198 );
		/* 1e000-1ffff empty (space for 16k ROM) */
		ROM_LOAD( "chp1-v.4r",    0x20000, 0x4000, 0x4e5b0fe9 );
		ROM_LOAD( "chp1-v.4t",    0x24000, 0x4000, 0x37ffc940 );
		ROM_LOAD( "chp1-v.4u",    0x28000, 0x2000, 0x1a7521d4 );
		/* 2a000-2bfff empty (space for 16k ROM) */
		/* 2c000-2ffff empty (4v doesn't exist, it is seen as a 0xff fill) */
	
		ROM_REGION( 0x10000, REGION_GFX4 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "chp1-v.6p",    0x00000, 0x2000, 0x16588f7a );/* chars #4 */
		ROM_LOAD( "chp1-v.6n",    0x02000, 0x2000, 0xdc743674 );
		/* 04000-07fff empty (space for 6l and 6k) */
		ROM_LOAD( "chp1-v.8p",    0x08000, 0x2000, 0xc2db5b4e );
		ROM_LOAD( "chp1-v.8n",    0x0a000, 0x2000, 0xe6af390e );
		/* 0c000-0ffff empty (space for 8l and 8k) */
	
		ROM_REGION( 0x0d00, REGION_PROMS );
		ROM_LOAD( "chp1-b.6e",    0x0000, 0x0200, 0xe9ca3ac6 );/* red component */
		ROM_LOAD( "chp1-b.7e",    0x0200, 0x0200, 0x47adf7a2 );/* red component */
		ROM_LOAD( "chp1-b.6f",    0x0400, 0x0200, 0x02be56ab );/* green component */
		ROM_LOAD( "chp1-b.8e",    0x0600, 0x0200, 0xb0fc15a8 );/* green component */
		ROM_LOAD( "chp1-b.7f",    0x0800, 0x0200, 0x11de55f1 );/* blue component */
		ROM_LOAD( "chp1-b.8f",    0x0a00, 0x0200, 0x1ffd894a );/* blue component */
		ROM_LOAD( "chp1-v.2d",    0x0c00, 0x0100, 0x71dc0d48 );/* timing - not used */
	
		ROM_REGION( 0x4000, REGION_SOUND1 );/* 16k for the VLM5030 data */
		ROM_LOAD( "chp1-c.6p",    0x0000, 0x4000, 0xea0bbb31 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_spnchout = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "chs1-c.8l",    0x0000, 0x2000, 0x703b9780 );
		ROM_LOAD( "chs1-c.8k",    0x2000, 0x2000, 0xe13719f6 );
		ROM_LOAD( "chs1-c.8j",    0x4000, 0x2000, 0x1fa629e8 );
		ROM_LOAD( "chs1-c.8h",    0x6000, 0x2000, 0x15a6c068 );
		ROM_LOAD( "chs1-c.8f",    0x8000, 0x4000, 0x4ff3cdd9 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the sound CPU */
		ROM_LOAD( "chp1-c.4k",    0xe000, 0x2000, 0xcb6ef376 );
	
		ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "chs1-b.4c",    0x00000, 0x0800, 0x9f2ede2d );/* chars #1 */
		ROM_CONTINUE(             0x01000, 0x0800 );
		ROM_CONTINUE(             0x00800, 0x0800 );
		ROM_CONTINUE(             0x01800, 0x0800 );
		ROM_LOAD( "chs1-b.4d",    0x02000, 0x0800, 0x143ae5c6 );
		ROM_CONTINUE(             0x03000, 0x0800 );
		ROM_CONTINUE(             0x02800, 0x0800 );
		ROM_CONTINUE(             0x03800, 0x0800 );
	
		ROM_REGION( 0x04000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "chp1-b.4a",    0x00000, 0x0800, 0xc075f831 );/* chars #2 */
		ROM_CONTINUE(             0x01000, 0x0800 );
		ROM_CONTINUE(             0x00800, 0x0800 );
		ROM_CONTINUE(             0x01800, 0x0800 );
		ROM_LOAD( "chp1-b.4b",    0x02000, 0x0800, 0xc4cc2b5a );
		ROM_CONTINUE(             0x03000, 0x0800 );
		ROM_CONTINUE(             0x02800, 0x0800 );
		ROM_CONTINUE(             0x03800, 0x0800 );
	
		ROM_REGION( 0x30000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "chs1-v.2r",    0x00000, 0x4000, 0xff33405d );/* chars #3 */
		ROM_LOAD( "chs1-v.2t",    0x04000, 0x4000, 0xf507818b );
		ROM_LOAD( "chs1-v.2u",    0x08000, 0x4000, 0x0995fc95 );
		ROM_LOAD( "chs1-v.2v",    0x0c000, 0x2000, 0xf44d9878 );
		/* 0e000-0ffff empty (space for 16k ROM) */
		ROM_LOAD( "chs1-v.3r",    0x10000, 0x4000, 0x09570945 );
		ROM_LOAD( "chs1-v.3t",    0x14000, 0x4000, 0x42c6861c );
		ROM_LOAD( "chs1-v.3u",    0x18000, 0x4000, 0xbf5d02dd );
		ROM_LOAD( "chs1-v.3v",    0x1c000, 0x2000, 0x5673f4fc );
		/* 1e000-1ffff empty (space for 16k ROM) */
		ROM_LOAD( "chs1-v.4r",    0x20000, 0x4000, 0x8e155758 );
		ROM_LOAD( "chs1-v.4t",    0x24000, 0x4000, 0xb4e43448 );
		ROM_LOAD( "chs1-v.4u",    0x28000, 0x4000, 0x74e0d956 );
		/* 2c000-2ffff empty (4v doesn't exist, it is seen as a 0xff fill) */
	
		ROM_REGION( 0x10000, REGION_GFX4 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "chp1-v.6p",    0x00000, 0x0800, 0x75be7aae );/* chars #4 */
		ROM_CONTINUE(             0x01000, 0x0800 );
		ROM_CONTINUE(             0x00800, 0x0800 );
		ROM_CONTINUE(             0x01800, 0x0800 );
		ROM_LOAD( "chp1-v.6n",    0x02000, 0x0800, 0xdaf74de0 );
		ROM_CONTINUE(             0x03000, 0x0800 );
		ROM_CONTINUE(             0x02800, 0x0800 );
		ROM_CONTINUE(             0x03800, 0x0800 );
		/* 04000-07fff empty (space for 6l and 6k) */
		ROM_LOAD( "chp1-v.8p",    0x08000, 0x0800, 0x4cb7ea82 );
		ROM_CONTINUE(             0x09000, 0x0800 );
		ROM_CONTINUE(             0x08800, 0x0800 );
		ROM_CONTINUE(             0x09800, 0x0800 );
		ROM_LOAD( "chp1-v.8n",    0x0a000, 0x0800, 0x1c0d09aa );
		ROM_CONTINUE(             0x0b000, 0x0800 );
		ROM_CONTINUE(             0x0a800, 0x0800 );
		ROM_CONTINUE(             0x0b800, 0x0800 );
		/* 0c000-0ffff empty (space for 8l and 8k) */
	
		ROM_REGION( 0x0d00, REGION_PROMS );
		ROM_LOAD( "chs1-b.6e",    0x0000, 0x0200, 0x0ad4d727 );/* red component */
		ROM_LOAD( "chs1-b.7e",    0x0200, 0x0200, 0x9e170f64 );/* red component */
		ROM_LOAD( "chs1-b.6f",    0x0400, 0x0200, 0x86f5cfdb );/* green component */
		ROM_LOAD( "chs1-b.8e",    0x0600, 0x0200, 0x3a2e333b );/* green component */
		ROM_LOAD( "chs1-b.7f",    0x0800, 0x0200, 0x8bd406f8 );/* blue component */
		ROM_LOAD( "chs1-b.8f",    0x0a00, 0x0200, 0x1663eed7 );/* blue component */
		ROM_LOAD( "chs1-v.2d",    0x0c00, 0x0100, 0x71dc0d48 );/* timing - not used */
	
		ROM_REGION( 0x10000, REGION_SOUND1 );/* 64k for the VLM5030 data */
		ROM_LOAD( "chs1-c.6p",    0x0000, 0x4000, 0xad8b64b8 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_spnchotj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "chs1c8la.bin", 0x0000, 0x2000, 0xdc2a592b );
		ROM_LOAD( "chs1c8ka.bin", 0x2000, 0x2000, 0xce687182 );
		ROM_LOAD( "chs1-c.8j",    0x4000, 0x2000, 0x1fa629e8 );
		ROM_LOAD( "chs1-c.8h",    0x6000, 0x2000, 0x15a6c068 );
		ROM_LOAD( "chs1c8fa.bin", 0x8000, 0x4000, 0xf745b5d5 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the sound CPU */
		ROM_LOAD( "chp1-c.4k",    0xe000, 0x2000, 0xcb6ef376 );
	
		ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "b_4c_01a.bin", 0x00000, 0x2000, 0xb017e1e9 );/* chars #1 */
		ROM_LOAD( "b_4d_01a.bin", 0x02000, 0x2000, 0xe3de9d18 );
	
		ROM_REGION( 0x04000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "chp1-b.4a",    0x00000, 0x0800, 0xc075f831 );/* chars #2 */
		ROM_CONTINUE(             0x01000, 0x0800 );
		ROM_CONTINUE(             0x00800, 0x0800 );
		ROM_CONTINUE(             0x01800, 0x0800 );
		ROM_LOAD( "chp1-b.4b",    0x02000, 0x0800, 0xc4cc2b5a );
		ROM_CONTINUE(             0x03000, 0x0800 );
		ROM_CONTINUE(             0x02800, 0x0800 );
		ROM_CONTINUE(             0x03800, 0x0800 );
	
		ROM_REGION( 0x30000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "chs1-v.2r",    0x00000, 0x4000, 0xff33405d );/* chars #3 */
		ROM_LOAD( "chs1-v.2t",    0x04000, 0x4000, 0xf507818b );
		ROM_LOAD( "chs1-v.2u",    0x08000, 0x4000, 0x0995fc95 );
		ROM_LOAD( "chs1-v.2v",    0x0c000, 0x2000, 0xf44d9878 );
		/* 0e000-0ffff empty (space for 16k ROM) */
		ROM_LOAD( "chs1-v.3r",    0x10000, 0x4000, 0x09570945 );
		ROM_LOAD( "chs1-v.3t",    0x14000, 0x4000, 0x42c6861c );
		ROM_LOAD( "chs1-v.3u",    0x18000, 0x4000, 0xbf5d02dd );
		ROM_LOAD( "chs1-v.3v",    0x1c000, 0x2000, 0x5673f4fc );
		/* 1e000-1ffff empty (space for 16k ROM) */
		ROM_LOAD( "chs1-v.4r",    0x20000, 0x4000, 0x8e155758 );
		ROM_LOAD( "chs1-v.4t",    0x24000, 0x4000, 0xb4e43448 );
		ROM_LOAD( "chs1-v.4u",    0x28000, 0x4000, 0x74e0d956 );
		/* 2c000-2ffff empty (4v doesn't exist, it is seen as a 0xff fill) */
	
		ROM_REGION( 0x10000, REGION_GFX4 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "chp1-v.6p",    0x00000, 0x0800, 0x75be7aae );/* chars #4 */
		ROM_CONTINUE(             0x01000, 0x0800 );
		ROM_CONTINUE(             0x00800, 0x0800 );
		ROM_CONTINUE(             0x01800, 0x0800 );
		ROM_LOAD( "chp1-v.6n",    0x02000, 0x0800, 0xdaf74de0 );
		ROM_CONTINUE(             0x03000, 0x0800 );
		ROM_CONTINUE(             0x02800, 0x0800 );
		ROM_CONTINUE(             0x03800, 0x0800 );
		/* 04000-07fff empty (space for 6l and 6k) */
		ROM_LOAD( "chp1-v.8p",    0x08000, 0x0800, 0x4cb7ea82 );
		ROM_CONTINUE(             0x09000, 0x0800 );
		ROM_CONTINUE(             0x08800, 0x0800 );
		ROM_CONTINUE(             0x09800, 0x0800 );
		ROM_LOAD( "chp1-v.8n",    0x0a000, 0x0800, 0x1c0d09aa );
		ROM_CONTINUE(             0x0b000, 0x0800 );
		ROM_CONTINUE(             0x0a800, 0x0800 );
		ROM_CONTINUE(             0x0b800, 0x0800 );
		/* 0c000-0ffff empty (space for 8l and 8k) */
	
		ROM_REGION( 0x0d00, REGION_PROMS );
		ROM_LOAD( "chs1b_6e.bpr", 0x0000, 0x0200, 0x8efd867f );/* red component */
		ROM_LOAD( "chs1-b.7e",    0x0200, 0x0200, 0x9e170f64 );/* red component */
		ROM_LOAD( "chs1b_6f.bpr", 0x0400, 0x0200, 0x279d6cbc );/* green component */
		ROM_LOAD( "chs1-b.8e",    0x0600, 0x0200, 0x3a2e333b );/* green component */
		ROM_LOAD( "chs1b_7f.bpr", 0x0800, 0x0200, 0xcad6b7ad );/* blue component */
		ROM_LOAD( "chs1-b.8f",    0x0a00, 0x0200, 0x1663eed7 );/* blue component */
		ROM_LOAD( "chs1-v.2d",    0x0c00, 0x0100, 0x71dc0d48 );/* timing - not used */
	
		ROM_REGION( 0x10000, REGION_SOUND1 );/* 64k for the VLM5030 data */
		ROM_LOAD( "chs1c6pa.bin", 0x0000, 0x4000, 0xd05fb730 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_armwrest = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "chv1-c.8l",    0x0000, 0x2000, 0xb09764c1 );
		ROM_LOAD( "chv1-c.8k",    0x2000, 0x2000, 0x0e147ff7 );
		ROM_LOAD( "chv1-c.8j",    0x4000, 0x2000, 0xe7365289 );
		ROM_LOAD( "chv1-c.8h",    0x6000, 0x2000, 0xa2118eec );
		ROM_LOAD( "chpv-c.8f",    0x8000, 0x4000, 0x664a07c4 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the sound CPU */
		ROM_LOAD( "chp1-c.4k",    0xe000, 0x2000, 0xcb6ef376 );/* same as Punch Out */
	
		ROM_REGION( 0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "chpv-b.2e",    0x00000, 0x4000, 0x8b45f365 );/* chars #1 */
		ROM_LOAD( "chpv-b.2d",    0x04000, 0x4000, 0xb1a2850c );
	
		ROM_REGION( 0x0c000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "chpv-b.2m",    0x00000, 0x4000, 0x19245b37 );/* chars #2 */
		ROM_LOAD( "chpv-b.2l",    0x04000, 0x4000, 0x46797941 );
		ROM_LOAD( "chpv-b.2k",    0x08000, 0x4000, 0x24c4c26d );
	
		ROM_REGION( 0x30000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "chv1-v.2r",    0x00000, 0x4000, 0xd86056d9 );/* chars #3 */
		ROM_LOAD( "chv1-v.2t",    0x04000, 0x4000, 0x5ad77059 );
		/* 08000-0bfff empty */
		ROM_LOAD( "chv1-v.2v",    0x0c000, 0x4000, 0xa0fd7338 );
		ROM_LOAD( "chv1-v.3r",    0x10000, 0x4000, 0x690e26fb );
		ROM_LOAD( "chv1-v.3t",    0x14000, 0x4000, 0xea5d7759 );
		/* 18000-1bfff empty */
		ROM_LOAD( "chv1-v.3v",    0x1c000, 0x4000, 0xceb37c05 );
		ROM_LOAD( "chv1-v.4r",    0x20000, 0x4000, 0xe291cba0 );
		ROM_LOAD( "chv1-v.4t",    0x24000, 0x4000, 0xe01f3b59 );
		/* 28000-2bfff empty */
		/* 2c000-2ffff empty (4v doesn't exist, it is seen as a 0xff fill) */
	
		ROM_REGION( 0x10000, REGION_GFX4 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "chv1-v.6p",    0x00000, 0x2000, 0xd834e142 );/* chars #4 */
		/* 02000-03fff empty (space for 16k ROM) */
		/* 04000-07fff empty (space for 6l and 6k) */
		ROM_LOAD( "chv1-v.8p",    0x08000, 0x2000, 0xa2f531db );
		/* 0a000-0bfff empty (space for 16k ROM) */
		/* 0c000-0ffff empty (space for 8l and 8k) */
	
		ROM_REGION( 0x0e00, REGION_PROMS );
		ROM_LOAD( "chpv-b.7b",    0x0000, 0x0200, 0xdf6fdeb3 );/* red component */
		ROM_LOAD( "chpv-b.4b",    0x0200, 0x0200, 0x9d51416e );/* red component */
		ROM_LOAD( "chpv-b.7c",    0x0400, 0x0200, 0xb1da5f42 );/* green component */
		ROM_LOAD( "chpv-b.4c",    0x0600, 0x0200, 0xb8a25795 );/* green component */
		ROM_LOAD( "chpv-b.7d",    0x0800, 0x0200, 0x4ede813e );/* blue component */
		ROM_LOAD( "chpv-b.4d",    0x0a00, 0x0200, 0x474fc3b1 );/* blue component */
		ROM_LOAD( "chv1-b.3c",    0x0c00, 0x0100, 0xc3f92ea2 );/* priority encoder - not used */
		ROM_LOAD( "chpv-v.2d",    0x0d00, 0x0100, 0x71dc0d48 );/* timing - not used */
	
		ROM_REGION( 0x10000, REGION_SOUND1 );/* 64k for the VLM5030 data */
		ROM_LOAD( "chv1-c.6p",    0x0000, 0x4000, 0x31b52896 );
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_punchout	   = new GameDriver("1984"	,"punchout"	,"punchout.java"	,rom_punchout,null	,machine_driver_punchout	,input_ports_punchout	,init_punchout	,ROT0	,	"Nintendo", "Punch-Out!!" );
	public static GameDriver driver_spnchout	   = new GameDriver("1984"	,"spnchout"	,"punchout.java"	,rom_spnchout,null	,machine_driver_punchout	,input_ports_spnchout	,init_spnchout	,ROT0	,	"Nintendo", "Super Punch-Out!!" );
	public static GameDriver driver_spnchotj	   = new GameDriver("1984"	,"spnchotj"	,"punchout.java"	,rom_spnchotj,driver_spnchout	,machine_driver_punchout	,input_ports_spnchout	,init_spnchotj	,ROT0	,	"Nintendo", "Super Punch-Out!! (Japan)" );
	public static GameDriver driver_armwrest	   = new GameDriver("1985"	,"armwrest"	,"punchout.java"	,rom_armwrest,null	,machine_driver_armwrest	,input_ports_armwrest	,init_armwrest	,ROT0	,	"Nintendo", "Arm Wrestling" );
}
