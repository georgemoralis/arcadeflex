/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package sndhrdw;

public class trackfld
{
	
	
	#define TIMER_RATE (4096/4)
	
	
	static VLM5030interface konami_vlm5030_interface = new VLM5030interface
	(
	    3580000,    /* master clock  */
	    255,        /* volume        */
	    4,         /* memory region  */
	    0,         /* memory size    */
	    0,         /* VCU            */
	);
	
	static SN76496interface konami_sn76496_interface = new SN76496interface
	(
	    1,  /* 1 chip */
	    new int[] { 14318180/8 }, /*  1.7897725 MHz */
	    new int[] { 0x2064 }
	);
	
	static DACinterface konami_dac_interface = new DACinterface
	(
	    1,
	    new int[] { 80 }
	);
	
	struct ADPCMinterface hyprolyb_adpcm_interface =
	{
		1,          /* 1 channel */
		4000,       /* 4000Hz playback */
		REGION_CPU3,	/* memory region */
		{ 100 }
	};
	
	
	static int SN76496_latch;
	
	/* The timer port on TnF and HyperSports sound hardware is derived from
	   a 14.318 MHz clock crystal which is passed  through a couple of 74ls393
	    ripple counters.
	    Various outputs of the ripper counters clock the various chips.
	    The Z80 uses 14.318 MHz / 4 (3.4MHz)
	    The SN chip uses 14.318 MHz / 8 (1.7MHz)
	    And the timer is connected to 14.318 MHz / 4096
	    As we are using the Z80 clockrate as a base value we need to multiply
	    the no of cycles by 4 to undo the 14.318/4 operation
	*/
	
	public static ReadHandlerPtr trackfld_sh_timer_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    int clock = cpu_gettotalcycles() / TIMER_RATE;
	
	    return clock & 0xF;
	} };
	
	public static ReadHandlerPtr trackfld_speech_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    return VLM5030_BSY() ? 0x10 : 0;
	} };
	
	static int last_addr = 0;
	
	public static WriteHandlerPtr trackfld_sound_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	    if( (offset & 0x07) == 0x03 )
	    {
	        int changes = offset^last_addr;
	        /* A7 = data enable for VLM5030 (don't care )          */
	        /* A8 = STA pin (1.0 data data  , 0.1 start speech   */
	        /* A9 = RST pin 1=reset                                */
	
	        /* A8 VLM5030 ST pin */
	        if ((changes & 0x100) != 0)
	            VLM5030_ST( offset&0x100 );
	        /* A9 VLM5030 RST pin */
	        if ((changes & 0x200) != 0)
	            VLM5030_RST( offset&0x200 );
	    }
	    last_addr = offset;
	} };
	
	public static ReadHandlerPtr hyperspt_sh_timer_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    int clock = cpu_gettotalcycles() / TIMER_RATE;
	
	    return (clock & 0x3) | (VLM5030_BSY()? 0x04 : 0);
	} };
	
	public static WriteHandlerPtr hyperspt_sound_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	    int changes = offset^last_addr;
	    /* A3 = data enable for VLM5030 (don't care )          */
	    /* A4 = STA pin (1.0 data data  , 0.1 start speech   */
	    /* A5 = RST pin 1=reset                                */
	    /* A6 = VLM5030    output disable (don't care ) */
	    /* A7 = kONAMI DAC output disable (don't care ) */
	    /* A8 = SN76489    output disable (don't care ) */
	
	    /* A4 VLM5030 ST pin */
	    if ((changes & 0x10) != 0)
	        VLM5030_ST( offset&0x10 );
	    /* A5 VLM5030 RST pin */
	    if ((changes & 0x20) != 0)
	        VLM5030_RST( offset&0x20 );
	
	    last_addr = offset;
	} };
	
	
	
	public static WriteHandlerPtr konami_sh_irqtrigger_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	    static int last;
	
	    if (last == 0 && data)
	    {
	        /* setting bit 0 low then high triggers IRQ on the sound CPU */
	        cpu_cause_interrupt(1,0xff);
	    }
	
	    last = data;
	} };
	
	
	public static WriteHandlerPtr konami_SN76496_latch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	    SN76496_latch = data;
	} };
	
	
	public static WriteHandlerPtr konami_SN76496_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	    SN76496_0_w(offset, SN76496_latch);
	} };
	
	
	
	
	public static ReadHandlerPtr hyprolyb_speech_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    return ADPCM_playing(0) ? 0x10 : 0x00;
	} };
	
	public static WriteHandlerPtr hyprolyb_ADPCM_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	    int cmd,start,end;
	    UBytePtr RAM = memory_region(REGION_CPU3);
	
	
	    /* simulate the operation of the 6802 */
	    cmd = RAM[0xfe01 + data] + 256 * RAM[0xfe00 + data];
	    start = RAM[cmd + 1] + 256 * RAM[cmd];
	    end = RAM[cmd + 3] + 256 * RAM[cmd + 2];
	    if (end > start)
	        ADPCM_play(0,start,(end - start)*2);
	} };
}
