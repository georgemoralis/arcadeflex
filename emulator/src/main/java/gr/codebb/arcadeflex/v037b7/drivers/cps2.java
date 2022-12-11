package gr.codebb.arcadeflex.v037b7.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.cps1.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.machine.eepromH.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static arcadeflex.v036.sound._2151intf.*;
import static arcadeflex.v036.sound._2151intfH.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static gr.codebb.arcadeflex.v037b7.sound.qsoundH.*;
import static gr.codebb.arcadeflex.v037b7.sound.qsound.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295H.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.machine.kabuki.*;
import static gr.codebb.arcadeflex.v036.machine.eeprom.*;
import static gr.codebb.arcadeflex.v037b7.drivers.cps1.cps1_gfxdecodeinfo;
import static gr.codebb.arcadeflex.v037b7.drivers.cps1.cps1_qsound_interrupt;
import static gr.codebb.arcadeflex.v037b7.drivers.cps1.cps1_readmem;
import static gr.codebb.arcadeflex.v037b7.drivers.cps1.cps1_writemem;
import static gr.codebb.arcadeflex.v037b7.drivers.cps1.qsound_interface;
import static gr.codebb.arcadeflex.v037b7.drivers.cps1.qsound_readmem;
import static gr.codebb.arcadeflex.v037b7.drivers.cps1.qsound_sharedram1_w;
import static gr.codebb.arcadeflex.v037b7.drivers.cps1.qsound_writemem;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.cps2.*;

public class cps2 {
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Capcom System 2
/*TODO*///  ===============
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*///#if 1
/*TODO*////* Graphics viewer functions */
/*TODO*///extern extern extern #else
/*TODO*////* Dummy CPS1 functions */
/*TODO*///#define cps2_vh_start			cps1_ch_start
/*TODO*///#define cps2_vh_stop			cps2_vh_stop
/*TODO*///#define cps2_vh_screenrefresh	cps2_vh_screenrefresh
/*TODO*///#endif
/*TODO*///
/*TODO*////* Export this function so that the vidhrdw routine can drive the
/*TODO*///Q-Sound hardware
/*TODO*///*/
    public static WriteHandlerPtr cps2_qsound_sharedram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
    {
            qsound_sharedram1_w.handler(offset, data);
    } };


    /* Maximum size of Q Sound Z80 region */
    public static final int QSOUND_SIZE = 0x50000;

    /* Maximum 680000 code size */
/*TODO*///#undef  CODE_SIZE
    public static final int CODE_SIZE   = 0x0800000;


    public static InitDriverHandlerPtr init_cps2 = new InitDriverHandlerPtr() { public void handler() 
    {
            UBytePtr RAM = memory_region(REGION_CPU1);
/*TODO*///	FILE *fp;
            int i;
/*TODO*///	const int decode=CODE_SIZE/2;
/*TODO*///
/*TODO*///	fp = fopen ("ROM.DMP", "w+b");
/*TODO*///	if (fp != 0)
/*TODO*///	{
/*TODO*///		for (i=0; i<decode; i+=2)
/*TODO*///		{
/*TODO*///			int value=READ_WORD(&RAM[i]);
/*TODO*///			fputc(value>>8,   fp);
/*TODO*///			fputc(value&0xff, fp);
/*TODO*///		}
/*TODO*///		fclose(fp);
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	/* Decrypt it ! */
/*TODO*///
/*TODO*///	fp=fopen ("ROMD.DMP", "w+b");
/*TODO*///	if (fp != 0)
/*TODO*///	{
/*TODO*///		for (i=0; i<decode; i+=2)
/*TODO*///		{
/*TODO*///			int value=READ_WORD(&RAM[decode+i]);
/*TODO*///			fputc(value>>8,   fp);
/*TODO*///			fputc(value&0xff, fp);
/*TODO*///		}
/*TODO*///
/*TODO*///		fclose(fp);
/*TODO*///	}


	/*
	Poke in a dummy program to stop the 68K core from crashing due
	to silly addresses.
	*/
	RAM.WRITE_WORD(0x0000, 0x00ff);
	RAM.WRITE_WORD(0x0002, 0x8000);  /* Dummy stack pointer */
	RAM.WRITE_WORD(0x0004, 0x0000);
	RAM.WRITE_WORD(0x0006, 0x00c2);  /* Dummy start vector */

	for (i=0x0008; i<0x00c0; i+=4)
	{
		RAM.WRITE_WORD(i+0, 0x0000);
		RAM.WRITE_WORD(i+2, 0x00c0);
	}

	RAM.WRITE_WORD(0x00c0, 0x4e73);   /* RETE */
	RAM.WRITE_WORD(0x00c2, 0x6000);
	RAM.WRITE_WORD(0x00c4, 0x00c2);   /* BRA 00c2 */
    } };

    static MachineDriver machine_driver_cps2 = new MachineDriver
    (
            /* basic machine hardware */
            new MachineCPU[] {
                    new MachineCPU(
                            CPU_M68000,
                            10000000,
                            cps1_readmem,cps1_writemem,null,null,
                            cps1_qsound_interrupt, 1  /* ??? interrupts per frame */
                    ),
                    new MachineCPU(
                            CPU_Z80 | CPU_AUDIO_CPU,
                            8000000,  /* 4 MHz ??? TODO: find real FRQ */
                            qsound_readmem,qsound_writemem,null,null,
                            interrupt,4
                    )
            },
            60, 0,
            1,
            null,
    /*TODO*///	#ifdef MESS
    /*TODO*///	null,
    /*TODO*///	#endif

            /* video hardware */
            0x30*8+32*2, 0x1c*8+32*3, new rectangle( 32, 32+0x30*8-1, 32+16, 32+16+0x1c*8-1 ),

            cps1_gfxdecodeinfo,
            32*16+32*16+32*16+32*16,   /* lotsa colours */
            32*16+32*16+32*16+32*16,   /* Colour table length */
            null,

            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            cps2_vh_start,
            cps2_vh_stop,
            cps2_vh_screenrefresh,

            /* sound hardware */
            SOUND_SUPPORTS_STEREO,0,0,0,
            new MachineSound[] {
                    new MachineSound(
                            SOUND_QSOUND,
                            qsound_interface
                    )
            }
    );

    static InputPortHandlerPtr input_ports_cps2 = new InputPortHandlerPtr(){ public void handler() { 
            PORT_START();       /* IN0 */
            PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER1 );
            PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER2 );
            PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE );/* pause */
            PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_SERVICE  );/* pause */
            PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
            PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
            PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON6 | IPF_PLAYER1 );
            PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON6 | IPF_PLAYER2  );

            PORT_START();       /* DSWA */
            PORT_DIPNAME( 0xff, 0xff, DEF_STR( "Unknown") );
            PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
            PORT_DIPSETTING(    0x00, DEF_STR( "On") );

            PORT_START();       /* DSWB */
            PORT_DIPNAME( 0xff, 0xff, DEF_STR( "Unknown") );
            PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
            PORT_DIPSETTING(    0x00, DEF_STR( "On") );

            PORT_START();       /* DSWC */
            PORT_DIPNAME( 0xff, 0xff, DEF_STR( "Unknown") );
            PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
            PORT_DIPSETTING(    0x00, DEF_STR( "On") );

            PORT_START();       /* Player 1 */
            PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
            PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1 );
            PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1 );
            PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1 );
            PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
            PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
            PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
            PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );

            PORT_START();       /* Player 2 */
            PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
            PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2 );
            PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2 );
            PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2 );
            PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
            PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
            PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
            PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
    INPUT_PORTS_END(); }}; 



/*TODO*///static RomLoadHandlerPtr rom_19xx = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "19xu.03", 0x000000, 0x80000, 0x05955268 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "19xu.04", 0x080000, 0x80000, 0x3111ab7f );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "19xu.05", 0x100000, 0x80000, 0x38df4a63 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "19xu.06", 0x180000, 0x80000, 0x5c7e60d3 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "19xu.07", 0x200000, 0x80000, 0x61c0296c );
/*TODO*///
/*TODO*///	ROM_REGION( 0x0a00000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "19x.18",   0x0000000, 0x200000, 0x2213e798 );
/*TODO*///	ROM_LOAD( "19x.17",   0x0200000, 0x080000, 0x2dfe18b5 );
/*TODO*///	ROM_LOAD( "19x.14",   0x0280000, 0x200000, 0xe916967c );
/*TODO*///	ROM_LOAD( "19x.13",   0x0480000, 0x080000, 0x427aeb18 );
/*TODO*///	ROM_LOAD( "19x.20",   0x0500000, 0x200000, 0xab75ffbe );
/*TODO*///	ROM_LOAD( "19x.19",   0x0700000, 0x080000, 0xcbef9579 );
/*TODO*///	ROM_LOAD( "19x.16",   0x0780000, 0x200000, 0x6e75f3db );
/*TODO*///	ROM_LOAD( "19x.15",   0x0980000, 0x080000, 0x63bdbf54 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "19x.01",   0x00000, 0x08000, 0xef55195e );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "19x.11",   0x000000, 0x200000, 0xd38beef3 );
/*TODO*///	ROM_LOAD( "19x.12",   0x200000, 0x200000, 0xd47c96e2 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_armwar = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pwgu.03b", 0x000000, 0x80000, 0x8b95497a );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pwgu.04b", 0x080000, 0x80000, 0x29eb5661 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pwgu.05b", 0x100000, 0x80000, 0xa54e9e44 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pwg.06",  0x180000, 0x80000, 0x87a60ce8 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pwg.07",  0x200000, 0x80000, 0xf7b148df );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pwg.08",  0x280000, 0x80000, 0xcc62823e );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pwg.09a",  0x300000, 0x80000, 0x4c26baee );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pwg.10",  0x380000, 0x80000, 0x07c4fb28 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x1400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "pwg.18",   0x0000000, 0x100000, 0x0109c71b );
/*TODO*///	ROM_LOAD( "pwg.17",   0x0100000, 0x400000, 0xbc475b94 );
/*TODO*///	ROM_LOAD( "pwg.14",   0x0500000, 0x100000, 0xc3f9ba63 );
/*TODO*///	ROM_LOAD( "pwg.13",   0x0600000, 0x400000, 0xae8fe08e );
/*TODO*///	ROM_LOAD( "pwg.20",   0x0a00000, 0x100000, 0xeb75ffbe );
/*TODO*///	ROM_LOAD( "pwg.19",   0x0b00000, 0x400000, 0x07439ff7 );
/*TODO*///	ROM_LOAD( "pwg.16",   0x0f00000, 0x100000, 0x815b0e7b );
/*TODO*///	ROM_LOAD( "pwg.15",   0x1000000, 0x400000, 0xdb560f58 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "pwg.01",   0x00000, 0x08000, 0x18a5c0e4 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "pwg.02",   0x28000, 0x20000, 0xc9dfffa6 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "pwg.11",   0x000000, 0x200000, 0xa78f7433 );
/*TODO*///	ROM_LOAD( "pwg.12",   0x200000, 0x200000, 0x77438ed0 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_armwara = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pwga.03a", 0x000000, 0x80000, 0x8d474ab1 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pwga.04a", 0x080000, 0x80000, 0x81b5aec7 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pwga.05a", 0x100000, 0x80000, 0x2618e819 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pwg.06",  0x180000, 0x80000, 0x87a60ce8 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pwg.07",  0x200000, 0x80000, 0xf7b148df );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pwg.08",  0x280000, 0x80000, 0xcc62823e );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pwg.09",  0x300000, 0x80000, 0xddc85ca6 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pwg.10",  0x380000, 0x80000, 0x07c4fb28 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x1400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "pwg.18",   0x0000000, 0x100000, 0x0109c71b );
/*TODO*///	ROM_LOAD( "pwg.17",   0x0100000, 0x400000, 0xbc475b94 );
/*TODO*///	ROM_LOAD( "pwg.14",   0x0500000, 0x100000, 0xc3f9ba63 );
/*TODO*///	ROM_LOAD( "pwg.13",   0x0600000, 0x400000, 0xae8fe08e );
/*TODO*///	ROM_LOAD( "pwg.20",   0x0a00000, 0x100000, 0xeb75ffbe );
/*TODO*///	ROM_LOAD( "pwg.19",   0x0b00000, 0x400000, 0x07439ff7 );
/*TODO*///	ROM_LOAD( "pwg.16",   0x0f00000, 0x100000, 0x815b0e7b );
/*TODO*///	ROM_LOAD( "pwg.15",   0x1000000, 0x400000, 0xdb560f58 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "pwg.01",   0x00000, 0x08000, 0x18a5c0e4 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "pwg.02",   0x28000, 0x20000, 0xc9dfffa6 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "pwg.11",   0x000000, 0x200000, 0xa78f7433 );
/*TODO*///	ROM_LOAD( "pwg.12",   0x200000, 0x200000, 0x77438ed0 );
/*TODO*///ROM_END(); }}; 

    static RomLoadHandlerPtr rom_avsp = new RomLoadHandlerPtr(){ public void handler(){ 
            ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
            ROM_LOAD_WIDE_SWAP( "avpu.03d", 0x000000, 0x80000, 0x42757950 );
            ROM_LOAD_WIDE_SWAP( "avpu.04d", 0x080000, 0x80000, 0x5abcdee6 );
            ROM_LOAD_WIDE_SWAP( "avp.05d", 0x100000, 0x80000, 0xfbfb5d7a );
            ROM_LOAD_WIDE_SWAP( "avp.06d", 0x180000, 0x80000, 0x190b817f );

            ROM_REGION( 0x1000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
            ROM_LOAD( "avp.18",   0x000000, 0x200000, /*0xd92b6fc0*/0x34fb7232 );
            ROM_LOAD( "avp.17",   0x200000, 0x200000, 0x94403195 );
            ROM_LOAD( "avp.14",   0x400000, 0x200000, 0xebba093e );
            ROM_LOAD( "avp.13",   0x600000, 0x200000, 0x8f8b5ae4 );
            ROM_LOAD( "avp.20",   0x800000, 0x200000, 0xf90baa21 );
            ROM_LOAD( "avp.19",   0xa00000, 0x200000, 0xe1981245 );
            ROM_LOAD( "avp.16",   0xc00000, 0x200000, 0xfb228297 );
            ROM_LOAD( "avp.15",   0xe00000, 0x200000, 0xb00280df );

            ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
            ROM_LOAD( "avp.01",   0x00000, 0x08000, 0x2d3b4220 );
            ROM_CONTINUE(         0x10000, 0x18000 );

            ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
            ROM_LOAD( "avp.11",   0x000000, 0x200000, 0x83499817 );
            ROM_LOAD( "avp.12",   0x200000, 0x200000, 0xf4110d49 );
    ROM_END(); }}; 

/*TODO*///static RomLoadHandlerPtr rom_avspa = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "avpa.03d", 0x000000, 0x80000, 0x6c1c1858 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "avpa.04d", 0x080000, 0x80000, 0x94f50b0c );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "avp.05d", 0x100000, 0x80000, 0xfbfb5d7a );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "avp.06d", 0x180000, 0x80000, 0x190b817f );
/*TODO*///
/*TODO*///	ROM_REGION( 0x1000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "avp.18",   0x000000, 0x200000, 0xd92b6fc0 );
/*TODO*///	ROM_LOAD( "avp.17",   0x200000, 0x200000, 0x94403195 );
/*TODO*///	ROM_LOAD( "avp.14",   0x400000, 0x200000, 0xebba093e );
/*TODO*///	ROM_LOAD( "avp.13",   0x600000, 0x200000, 0x8f8b5ae4 );
/*TODO*///	ROM_LOAD( "avp.20",   0x800000, 0x200000, 0xf90baa21 );
/*TODO*///	ROM_LOAD( "avp.19",   0xa00000, 0x200000, 0xe1981245 );
/*TODO*///	ROM_LOAD( "avp.16",   0xc00000, 0x200000, 0xfb228297 );
/*TODO*///	ROM_LOAD( "avp.15",   0xe00000, 0x200000, 0xb00280df );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "avp.01",   0x00000, 0x08000, 0x2d3b4220 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "avp.11",   0x000000, 0x200000, 0x83499817 );
/*TODO*///	ROM_LOAD( "avp.12",   0x200000, 0x200000, 0xf4110d49 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_batcirj = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "btcj.03", 0x000000, 0x80000, 0x6b7e168d );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "btcj.04", 0x080000, 0x80000, 0x46ba3467 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "btcj.05", 0x100000, 0x80000, 0x0e23a859 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "btcj.06", 0x180000, 0x80000, 0xa853b59c );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "btc.07",  0x200000, 0x80000, 0x7322d5db );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "btc.08",  0x280000, 0x80000, 0x6aac85ab );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "btc.09",  0x300000, 0x80000, 0x1203db08 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x1000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "btc.17",   0x000000, 0x400000, 0xb33f4112 );
/*TODO*///	ROM_LOAD( "btc.13",   0x400000, 0x400000, 0xdc705bad );
/*TODO*///	ROM_LOAD( "btc.19",   0x800000, 0x400000, 0xa6fcdb7e );
/*TODO*///	ROM_LOAD( "btc.15",   0xC00000, 0x400000, 0xe5779a3c );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "btc.01",   0x00000, 0x08000, 0x1e194310 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "btc.02",   0x28000, 0x20000, 0x01aeb8e6 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "btc.11",   0x000000, 0x200000, 0xc27f2229 );
/*TODO*///	ROM_LOAD( "btc.12",   0x200000, 0x200000, 0x418a2e33 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_batcira = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "btca.03", 0x000000, 0x80000, 0x1ad20d87 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "btca.04", 0x080000, 0x80000, 0x2b3f4dbe );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "btca.05", 0x100000, 0x80000, 0x8238a3d9 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "btca.06", 0x180000, 0x80000, 0x446c7c02 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "btc.07",  0x200000, 0x80000, 0x7322d5db );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "btc.08",  0x280000, 0x80000, 0x6aac85ab );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "btc.09",  0x300000, 0x80000, 0x1203db08 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x1000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "btc.17",   0x000000, 0x400000, 0xb33f4112 );
/*TODO*///	ROM_LOAD( "btc.13",   0x400000, 0x400000, 0xdc705bad );
/*TODO*///	ROM_LOAD( "btc.19",   0x800000, 0x400000, 0xa6fcdb7e );
/*TODO*///	ROM_LOAD( "btc.15",   0xC00000, 0x400000, 0xe5779a3c );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "btc.01",   0x00000, 0x08000, 0x1e194310 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "btc.02",   0x28000, 0x20000, 0x01aeb8e6 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "btc.11",   0x000000, 0x200000, 0xc27f2229 );
/*TODO*///	ROM_LOAD( "btc.12",   0x200000, 0x200000, 0x418a2e33 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_cybotsj = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "cybj.03", 0x000000, 0x80000, 0x6096eada );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "cybj.04", 0x080000, 0x80000, 0x7b0ffaa9 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "cybj.05", 0x100000, 0x80000, 0xec40408e );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "cybj.06", 0x180000, 0x80000, 0x1ad0bed2 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "cybj.07", 0x200000, 0x80000, 0x6245a39a );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "cybj.08", 0x280000, 0x80000, 0x4b48e223 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "cybj.09", 0x300000, 0x80000, 0xe15238f6 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "cybj.10", 0x380000, 0x80000, 0x75f4003b );
/*TODO*///
/*TODO*///	ROM_REGION( 0x2000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "cyb.18",   0x0000000, 0x400000, 0x06a05c14 );
/*TODO*///	ROM_LOAD( "cyb.17",   0x0400000, 0x400000, 0x514a5ae0 );
/*TODO*///	ROM_LOAD( "cyb.14",   0x0800000, 0x400000, 0x15c339d0 );
/*TODO*///	ROM_LOAD( "cyb.13",   0x0c00000, 0x400000, 0x49d1de79 );
/*TODO*///	ROM_LOAD( "cyb.20",   0x1000000, 0x400000, 0x3c9d7033 );
/*TODO*///	ROM_LOAD( "cyb.19",   0x1400000, 0x400000, 0x74d6327e );
/*TODO*///	ROM_LOAD( "cyb.16",   0x1800000, 0x400000, 0xB6b56ca4 );
/*TODO*///	ROM_LOAD( "cyb.15",   0x1c00000, 0x400000, 0x3852535f );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "cyb.01",   0x00000, 0x08000, 0x9c0fb079 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "cyb.02",   0x28000, 0x20000, 0x51cb0c4e );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "cyb.11",   0x000000, 0x200000, 0x362ccab2 );
/*TODO*///	ROM_LOAD( "cyb.12",   0x200000, 0x200000, 0x7066e9cc );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_ddtod = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dadu.03a", 0x000000, 0x80000, 0x4413f177 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dadu.04a", 0x080000, 0x80000, 0x168de230 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dadu.05a", 0x100000, 0x80000, 0x03d39e91 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dad.06",   0x180000, 0x80000, 0x13aa3e56 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dad.07",   0x200000, 0x80000, 0x431cb6dd );
/*TODO*///
/*TODO*///	ROM_REGION( 0xc00000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "dad.18",   0x000000, 0x100000, 0xcef393ef );
/*TODO*///	ROM_LOAD( "dad.17",   0x100000, 0x200000, 0xb98757f5 );
/*TODO*///	ROM_LOAD( "dad.14",   0x300000, 0x100000, 0x837e6f3f );
/*TODO*///	ROM_LOAD( "dad.13",   0x400000, 0x200000, 0xda3cb7d6 );
/*TODO*///	ROM_LOAD( "dad.20",   0x600000, 0x100000, 0x8953fe9e );
/*TODO*///	ROM_LOAD( "dad.19",   0x700000, 0x200000, 0x8121ce46 );
/*TODO*///	ROM_LOAD( "dad.16",   0x900000, 0x100000, 0xf0916bdb );
/*TODO*///	ROM_LOAD( "dad.15",   0xa00000, 0x200000, 0x92b63172 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "dad.01",   0x00000, 0x08000, 0x3f5e2424 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "dad.11",   0x000000, 0x200000, 0x0c499b67 );
/*TODO*///	ROM_LOAD( "dad.12",   0x200000, 0x200000, 0x2f0b5a4e );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_ddtoda = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dada.03a", 0x000000, 0x80000, 0xfc6f2dd7 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dada.04a", 0x080000, 0x80000, 0xd4be4009 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dada.05a", 0x100000, 0x80000, 0x6712d1cf );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dad.06",   0x180000, 0x80000, 0x13aa3e56 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dad.07",   0x200000, 0x80000, 0x431cb6dd );
/*TODO*///
/*TODO*///	ROM_REGION( 0xc00000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "dad.18",   0x000000, 0x100000, 0xcef393ef );
/*TODO*///	ROM_LOAD( "dad.17",   0x100000, 0x200000, 0xb98757f5 );
/*TODO*///	ROM_LOAD( "dad.14",   0x300000, 0x100000, 0x837e6f3f );
/*TODO*///	ROM_LOAD( "dad.13",   0x400000, 0x200000, 0xda3cb7d6 );
/*TODO*///	ROM_LOAD( "dad.20",   0x600000, 0x100000, 0x8953fe9e );
/*TODO*///	ROM_LOAD( "dad.19",   0x700000, 0x200000, 0x8121ce46 );
/*TODO*///	ROM_LOAD( "dad.16",   0x900000, 0x100000, 0xf0916bdb );
/*TODO*///	ROM_LOAD( "dad.15",   0xa00000, 0x200000, 0x92b63172 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "dad.01",   0x00000, 0x08000, 0x3f5e2424 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "dad.11",   0x000000, 0x200000, 0x0c499b67 );
/*TODO*///	ROM_LOAD( "dad.12",   0x200000, 0x200000, 0x2f0b5a4e );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_ddtodr1 = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dadu.03b", 0x000000, 0x80000, 0xa519905f );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dadu.04b", 0x080000, 0x80000, 0x52562d38 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dadu.05b", 0x100000, 0x80000, 0xee1cfbfe );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dad.06",   0x180000, 0x80000, 0x13aa3e56 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dad.07",   0x200000, 0x80000, 0x431cb6dd );
/*TODO*///
/*TODO*///	ROM_REGION( 0xc00000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "dad.18",   0x000000, 0x100000, 0xcef393ef );
/*TODO*///	ROM_LOAD( "dad.17",   0x100000, 0x200000, 0xb98757f5 );
/*TODO*///	ROM_LOAD( "dad.14",   0x300000, 0x100000, 0x837e6f3f );
/*TODO*///	ROM_LOAD( "dad.13",   0x400000, 0x200000, 0xda3cb7d6 );
/*TODO*///	ROM_LOAD( "dad.20",   0x600000, 0x100000, 0x8953fe9e );
/*TODO*///	ROM_LOAD( "dad.19",   0x700000, 0x200000, 0x8121ce46 );
/*TODO*///	ROM_LOAD( "dad.16",   0x900000, 0x100000, 0xf0916bdb );
/*TODO*///	ROM_LOAD( "dad.15",   0xa00000, 0x200000, 0x92b63172 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "dad.01",   0x00000, 0x08000, 0x3f5e2424 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "dad.11",   0x000000, 0x200000, 0x0c499b67 );
/*TODO*///	ROM_LOAD( "dad.12",   0x200000, 0x200000, 0x2f0b5a4e );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_ddsom = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dd2u.03d", 0x000000, 0x80000, 0x0f700d84 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dd2u.04d", 0x080000, 0x80000, 0xb99eb254 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dd2u.05d", 0x100000, 0x80000, 0xb23061f3 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dd2u.06d", 0x180000, 0x80000, 0x8bf1d8ce );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dd2.07",   0x200000, 0x80000, 0x909a0b8b );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dd2.08",   0x280000, 0x80000, 0xe53c4d01 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dd2.09",   0x300000, 0x80000, 0x5f86279f );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "dd2.10",   0x380000, 0x80000, 0xad954c26 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x1800000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "dd2.18",   0x0000000, 0x200000, 0xacddd149 );
/*TODO*///	ROM_LOAD( "dd2.17",   0x0200000, 0x400000, 0x837c0867 );
/*TODO*///	ROM_LOAD( "dd2.14",   0x0600000, 0x200000, 0x6d824ce2 );
/*TODO*///	ROM_LOAD( "dd2.13",   0x0800000, 0x400000, 0xa46b4e6e );
/*TODO*///	ROM_LOAD( "dd2.20",   0x0c00000, 0x200000, 0x117fb0c0 );
/*TODO*///	ROM_LOAD( "dd2.19",   0x0e00000, 0x400000, 0xbb0ec21c );
/*TODO*///	ROM_LOAD( "dd2.16",   0x1200000, 0x200000, 0x79682ae5 );
/*TODO*///	ROM_LOAD( "dd2.15",   0x1400000, 0x400000, 0xd5fc50fc );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "dd2.01",   0x00000, 0x08000, 0x99d657e5 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "dd2.02",   0x28000, 0x20000, 0x117a3824 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "dd2.11",   0x000000, 0x200000, 0x98d0c325 );
/*TODO*///	ROM_LOAD( "dd2.12",   0x200000, 0x200000, 0x5ea2e7fa );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_dstlk = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vamu.03b", 0x000000, 0x80000, 0x68a6343f );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vamu.04b", 0x080000, 0x80000, 0x58161453 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vamu.05b", 0x100000, 0x80000, 0xdfc038b8 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vamu.06b", 0x180000, 0x80000, 0xc3842c89 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vamu.07b", 0x200000, 0x80000, 0x25b60b6e );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vamu.08b", 0x280000, 0x80000, 0x2113c596 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vamu.09b", 0x300000, 0x80000, 0x2d1e9ae5 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vamu.10b", 0x380000, 0x80000, 0x81145622 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x1400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "vam.18",   0x0000000, 0x100000, 0x3a033625 );
/*TODO*///	ROM_LOAD( "vam.17",   0x0100000, 0x400000, 0x4f2408e0 );
/*TODO*///	ROM_LOAD( "vam.14",   0x0500000, 0x100000, 0xbd87243c );
/*TODO*///	ROM_LOAD( "vam.13",   0x0600000, 0x400000, 0xc51baf99 );
/*TODO*///	ROM_LOAD( "vam.20",   0x0a00000, 0x100000, 0x2bff6a89 );
/*TODO*///	ROM_LOAD( "vam.19",   0x0b00000, 0x400000, 0x9ff60250 );
/*TODO*///	ROM_LOAD( "vam.16",   0x0f00000, 0x100000, 0xafec855f );
/*TODO*///	ROM_LOAD( "vam.15",   0x1000000, 0x400000, 0x3ce83c77 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "vam.01",   0x00000, 0x08000, 0x64b685d5 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "vam.02",   0x28000, 0x20000, 0xcf7c97c7 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "vam.11",   0x000000, 0x200000, 0x4a39deb2 );
/*TODO*///	ROM_LOAD( "vam.12",   0x200000, 0x200000, 0x1a3e5c03 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_vampj = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vamj.03a", 0x000000, 0x80000, 0xf55d3722 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vamj.04b", 0x080000, 0x80000, 0x4d9c43c4 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vamj.05a", 0x100000, 0x80000, 0x6c497e92 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vamj.06a", 0x180000, 0x80000, 0xf1bbecb6 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vamj.07a", 0x200000, 0x80000, 0x1067ad84 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vamj.08a", 0x280000, 0x80000, 0x4b89f41f );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vamj.09a", 0x300000, 0x80000, 0xfc0a4aac );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vamj.10a", 0x380000, 0x80000, 0x9270c26b );
/*TODO*///
/*TODO*///	ROM_REGION( 0x1400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "vam.18",   0x0000000, 0x100000, 0x3a033625 );
/*TODO*///	ROM_LOAD( "vam.17",   0x0100000, 0x400000, 0x4f2408e0 );
/*TODO*///	ROM_LOAD( "vam.14",   0x0500000, 0x100000, 0xbd87243c );
/*TODO*///	ROM_LOAD( "vam.13",   0x0600000, 0x400000, 0xc51baf99 );
/*TODO*///	ROM_LOAD( "vam.20",   0x0a00000, 0x100000, 0x2bff6a89 );
/*TODO*///	ROM_LOAD( "vam.19",   0x0b00000, 0x400000, 0x9ff60250 );
/*TODO*///	ROM_LOAD( "vam.16",   0x0f00000, 0x100000, 0xafec855f );
/*TODO*///	ROM_LOAD( "vam.15",   0x1000000, 0x400000, 0x3ce83c77 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "vam.01",   0x00000, 0x08000, 0x64b685d5 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "vam.02",   0x28000, 0x20000, 0xcf7c97c7 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "vam.11",   0x000000, 0x200000, 0x4a39deb2 );
/*TODO*///	ROM_LOAD( "vam.12",   0x200000, 0x200000, 0x1a3e5c03 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_vampa = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vama.03a", 0x000000, 0x80000, 0x294e0bec );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vama.04a", 0x080000, 0x80000, 0xbc18e128 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vama.05a", 0x100000, 0x80000, 0xe709fa59 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vama.06a", 0x180000, 0x80000, 0x55e4d387 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vama.07a", 0x200000, 0x80000, 0x24e8f981 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vama.08a", 0x280000, 0x80000, 0x743f3a8e );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vama.09a", 0x300000, 0x80000, 0x67fa5573 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vama.10a", 0x380000, 0x80000, 0x5e03d747 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x1400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "vam.18",   0x0000000, 0x100000, 0x3a033625 );
/*TODO*///	ROM_LOAD( "vam.17",   0x0100000, 0x400000, 0x4f2408e0 );
/*TODO*///	ROM_LOAD( "vam.14",   0x0500000, 0x100000, 0xbd87243c );
/*TODO*///	ROM_LOAD( "vam.13",   0x0600000, 0x400000, 0xc51baf99 );
/*TODO*///	ROM_LOAD( "vam.20",   0x0a00000, 0x100000, 0x2bff6a89 );
/*TODO*///	ROM_LOAD( "vam.19",   0x0b00000, 0x400000, 0x9ff60250 );
/*TODO*///	ROM_LOAD( "vam.16",   0x0f00000, 0x100000, 0xafec855f );
/*TODO*///	ROM_LOAD( "vam.15",   0x1000000, 0x400000, 0x3ce83c77 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "vam.01",   0x00000, 0x08000, 0x64b685d5 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "vam.02",   0x28000, 0x20000, 0xcf7c97c7 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "vam.11",   0x000000, 0x200000, 0x4a39deb2 );
/*TODO*///	ROM_LOAD( "vam.12",   0x200000, 0x200000, 0x1a3e5c03 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_ecofe = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "uece.03", 0x000000, 0x80000, 0xec2c1137 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "uece.04", 0x080000, 0x80000, 0xb35f99db );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "uece.05", 0x100000, 0x80000, 0xd9d42d31 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "uece.06", 0x180000, 0x80000, 0x9d9771cf );
/*TODO*///
/*TODO*///	ROM_REGION( 0xc00000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "uec.18",   0x000000, 0x100000, 0xb167ae12 );
/*TODO*///	ROM_LOAD( "uec.17",   0x100000, 0x200000, 0x8a708d02 );
/*TODO*///	ROM_LOAD( "uec.14",   0x300000, 0x100000, 0x1a003558 );
/*TODO*///	ROM_LOAD( "uec.13",   0x400000, 0x200000, 0xdcaf1436 );
/*TODO*///	ROM_LOAD( "uec.20",   0x600000, 0x100000, 0x1064bdc2 );
/*TODO*///	ROM_LOAD( "uec.19",   0x700000, 0x200000, 0xde7be0ef );
/*TODO*///	ROM_LOAD( "uec.16",   0x900000, 0x100000, 0x4ff8a6f9 );
/*TODO*///	ROM_LOAD( "uec.15",   0xa00000, 0x200000, 0x2807df41 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "uec.01",   0x00000, 0x08000, 0xc235bd15 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "uec.11",   0x000000, 0x200000, 0x81b25d39 );
/*TODO*///	ROM_LOAD( "uec.12",   0x200000, 0x200000, 0x27729e52 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_uecology = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "uecj.03", 0x000000, 0x80000, 0x94c40a4c );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "uecj.04", 0x080000, 0x80000, 0x8d6e3a09 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "uecj.05", 0x100000, 0x80000, 0x8604ecd7 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "uecj.06", 0x180000, 0x80000, 0xb7e1d31f );
/*TODO*///
/*TODO*///	ROM_REGION( 0xc00000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "uec.18",   0x000000, 0x100000, 0xb167ae12 );
/*TODO*///	ROM_LOAD( "uec.17",   0x100000, 0x200000, 0x8a708d02 );
/*TODO*///	ROM_LOAD( "uec.14",   0x300000, 0x100000, 0x1a003558 );
/*TODO*///	ROM_LOAD( "uec.13",   0x400000, 0x200000, 0xdcaf1436 );
/*TODO*///	ROM_LOAD( "uec.20",   0x600000, 0x100000, 0x1064bdc2 );
/*TODO*///	ROM_LOAD( "uec.19",   0x700000, 0x200000, 0xde7be0ef );
/*TODO*///	ROM_LOAD( "uec.16",   0x900000, 0x100000, 0x4ff8a6f9 );
/*TODO*///	ROM_LOAD( "uec.15",   0xa00000, 0x200000, 0x2807df41 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "uec.01",   0x00000, 0x08000, 0xc235bd15 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "uec.11",   0x000000, 0x200000, 0x81b25d39 );
/*TODO*///	ROM_LOAD( "uec.12",   0x200000, 0x200000, 0x27729e52 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_msh = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mshu.03", 0x000000, 0x80000, 0xd2805bdd );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mshu.04", 0x080000, 0x80000, 0x743f96ff );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mshu.05", 0x100000, 0x80000, 0x6a091b9e );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mshu.06", 0x180000, 0x80000, 0x803e3fa4 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mshu.07", 0x200000, 0x80000, 0xc45f8e27 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mshu.08", 0x280000, 0x80000, 0x9ca6f12c );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mshu.09", 0x300000, 0x80000, 0x82ec27af );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mshu.10", 0x380000, 0x80000, 0x8d931196 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x2000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "msh.18",   0x0000000, 0x400000, 0x4db92d94 );
/*TODO*///	ROM_LOAD( "msh.17",   0x0400000, 0x400000, 0x604ece14 );
/*TODO*///	ROM_LOAD( "msh.14",   0x0800000, 0x400000, 0x4197973e );
/*TODO*///	ROM_LOAD( "msh.13",   0x0c00000, 0x400000, 0x0031a54e );
/*TODO*///	ROM_LOAD( "msh.20",   0x1000000, 0x400000, 0xa2b0c6c0 );
/*TODO*///	ROM_LOAD( "msh.19",   0x1400000, 0x400000, 0x94a731e8 );
/*TODO*///	ROM_LOAD( "msh.16",   0x1800000, 0x400000, 0x438da4a0 );
/*TODO*///	ROM_LOAD( "msh.15",   0x1c00000, 0x400000, 0xee962057 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "msh.01",   0x00000, 0x08000, 0xc976e6f9 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "msh.02",   0x28000, 0x20000, 0xce67d0d9 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "msh.11",   0x000000, 0x200000, 0x37ac6d30 );
/*TODO*///	ROM_LOAD( "msh.12",   0x200000, 0x200000, 0xde092570 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_mshvsf = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvsu.03d", 0x000000, 0x80000, 0xae60a66a );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvsu.04d", 0x080000, 0x80000, 0x91f67d8a );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvsu.05a", 0x100000, 0x80000, 0x1a5de0cb );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvs.06a", 0x180000, 0x80000, 0x959f3030 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvs.07b", 0x200000, 0x80000, 0x7f915bdb );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvs.08a", 0x280000, 0x80000, 0xc2813884 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvs.09b", 0x300000, 0x80000, 0x3ba08818 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvs.10b", 0x380000, 0x80000, 0xcf0dba98 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x2000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "mvs.18",   0x0000000, 0x400000, 0xc1228b35 );
/*TODO*///	ROM_LOAD( "mvs.17",   0x0400000, 0x400000, 0x97aaf4c7 );
/*TODO*///	ROM_LOAD( "mvs.14",   0x0800000, 0x400000, 0xb3b1972d );
/*TODO*///	ROM_LOAD( "mvs.13",   0x0c00000, 0x400000, 0x29b05fd9 );
/*TODO*///	ROM_LOAD( "mvs.20",   0x1000000, 0x400000, 0x366cc6c2 );
/*TODO*///	ROM_LOAD( "mvs.19",   0x1400000, 0x400000, 0xcb70e915 );
/*TODO*///	ROM_LOAD( "mvs.16",   0x1800000, 0x400000, 0x08aadb5d );
/*TODO*///	ROM_LOAD( "mvs.15",   0x1c00000, 0x400000, 0xfaddccf1 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "mvs.01",   0x00000, 0x08000, 0x68252324 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "mvs.02",   0x28000, 0x20000, 0xb34e773d );
/*TODO*///
/*TODO*///	ROM_REGION( 0x800000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "mvs.11",   0x000000, 0x400000, 0x86219770 );
/*TODO*///	ROM_LOAD( "mvs.12",   0x400000, 0x400000, 0xf2fd7f68 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_mshvsfj = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvsj.03i", 0x000000, 0x80000, 0xd8cbb691 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvsj.04i", 0x080000, 0x80000, 0x32741ace );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvsj.05h", 0x100000, 0x80000, 0x77870dc3 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvs.06a", 0x180000, 0x80000, 0x959f3030 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvs.07b", 0x200000, 0x80000, 0x7f915bdb );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvs.08a", 0x280000, 0x80000, 0xc2813884 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvs.09b", 0x300000, 0x80000, 0x3ba08818 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvs.10b", 0x380000, 0x80000, 0xcf0dba98 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x2000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "mvs.18",   0x0000000, 0x400000, 0xc1228b35 );
/*TODO*///	ROM_LOAD( "mvs.17",   0x0400000, 0x400000, 0x97aaf4c7 );
/*TODO*///	ROM_LOAD( "mvs.14",   0x0800000, 0x400000, 0xb3b1972d );
/*TODO*///	ROM_LOAD( "mvs.13",   0x0c00000, 0x400000, 0x29b05fd9 );
/*TODO*///	ROM_LOAD( "mvs.20",   0x1000000, 0x400000, 0x366cc6c2 );
/*TODO*///	ROM_LOAD( "mvs.19",   0x1400000, 0x400000, 0xcb70e915 );
/*TODO*///	ROM_LOAD( "mvs.16",   0x1800000, 0x400000, 0x08aadb5d );
/*TODO*///	ROM_LOAD( "mvs.15",   0x1c00000, 0x400000, 0xfaddccf1 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "mvs.01",   0x00000, 0x08000, 0x68252324 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "mvs.02",   0x28000, 0x20000, 0xb34e773d );
/*TODO*///
/*TODO*///	ROM_REGION( 0x800000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "mvs.11",   0x000000, 0x400000, 0x86219770 );
/*TODO*///	ROM_LOAD( "mvs.12",   0x400000, 0x400000, 0xf2fd7f68 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_mvsc = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvcu.03d", 0x000000, 0x80000, 0xc6007557 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvcu.04d", 0x080000, 0x80000, 0x724b2b20 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvc.05a",  0x100000, 0x80000, 0x2d8c8e86 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvc.06",   0x180000, 0x80000, 0x8528e1f5 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvc.07",   0x200000, 0x80000, 0xc3baa32b );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvc.08",   0x280000, 0x80000, 0xbc002fcd );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvc.09",   0x300000, 0x80000, 0xc67b26df );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvc.10",   0x380000, 0x80000, 0x0fdd1e26 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x2000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "mvc.18",   0x0000000, 0x400000, 0x67aaf727 );
/*TODO*///	ROM_LOAD( "mvc.17",   0x0400000, 0x400000, 0x92741d07 );
/*TODO*///	ROM_LOAD( "mvc.14",   0x0800000, 0x400000, 0x7f1df4e4 );
/*TODO*///	ROM_LOAD( "mvc.13",   0x0c00000, 0x400000, 0xfa5f74bc );
/*TODO*///	ROM_LOAD( "mvc.20",   0x1000000, 0x400000, 0x8b0bade8 );
/*TODO*///	ROM_LOAD( "mvc.19",   0x1400000, 0x400000, 0xbcb72fc6 );
/*TODO*///	ROM_LOAD( "mvc.16",   0x1800000, 0x400000, 0x90bd3203 );
/*TODO*///	ROM_LOAD( "mvc.15",   0x1c00000, 0x400000, 0x71938a8f );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "mvc.01",   0x00000, 0x08000, 0x41629e95 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "mvc.02",   0x28000, 0x20000, 0x963abf6b );
/*TODO*///
/*TODO*///	ROM_REGION( 0x800000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "mvc.11",   0x000000, 0x400000, 0x85981e0b );
/*TODO*///	ROM_LOAD( "mvc.12",   0x400000, 0x400000, 0x7ccb1896 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_mvscj = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvcj.03a", 0x000000, 0x80000, 0x3df18879 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvcj.04a", 0x080000, 0x80000, 0x07d212e8 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvc.05a",  0x100000, 0x80000, 0x2d8c8e86 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvc.06",   0x180000, 0x80000, 0x8528e1f5 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvc.07",   0x200000, 0x80000, 0xc3baa32b );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvc.08",   0x280000, 0x80000, 0xbc002fcd );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvc.09",   0x300000, 0x80000, 0xc67b26df );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "mvc.10",   0x380000, 0x80000, 0x0fdd1e26 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x2000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "mvc.18",   0x0000000, 0x400000, 0x67aaf727 );
/*TODO*///	ROM_LOAD( "mvc.17",   0x0400000, 0x400000, 0x92741d07 );
/*TODO*///	ROM_LOAD( "mvc.14",   0x0800000, 0x400000, 0x7f1df4e4 );
/*TODO*///	ROM_LOAD( "mvc.13",   0x0c00000, 0x400000, 0xfa5f74bc );
/*TODO*///	ROM_LOAD( "mvc.20",   0x1000000, 0x400000, 0x8b0bade8 );
/*TODO*///	ROM_LOAD( "mvc.19",   0x1400000, 0x400000, 0xbcb72fc6 );
/*TODO*///	ROM_LOAD( "mvc.16",   0x1800000, 0x400000, 0x90bd3203 );
/*TODO*///	ROM_LOAD( "mvc.15",   0x1c00000, 0x400000, 0x71938a8f );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "mvc.01",   0x00000, 0x08000, 0x41629e95 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "mvc.02",   0x28000, 0x20000, 0x963abf6b );
/*TODO*///
/*TODO*///	ROM_REGION( 0x800000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "mvc.11",   0x000000, 0x400000, 0x85981e0b );
/*TODO*///	ROM_LOAD( "mvc.12",   0x400000, 0x400000, 0x7ccb1896 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_nwarr = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vphu.03f", 0x000000, 0x80000, 0x85d6a359 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vphu.04c", 0x080000, 0x80000, 0xcb7fce77 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vphu.05e", 0x100000, 0x80000, 0xe08f2bba );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vphu.06c", 0x180000, 0x80000, 0x08c04cdb );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vphu.07b", 0x200000, 0x80000, 0xb5a5ab19 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vphu.08b", 0x280000, 0x80000, 0x51bb20fb );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vphu.09b", 0x300000, 0x80000, 0x41a64205 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vphu.10b", 0x380000, 0x80000, 0x2b1d43ae );
/*TODO*///
/*TODO*///	ROM_REGION( 0x2000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "vph.18",   0x0000000, 0x400000, 0x64498eed );
/*TODO*///	ROM_LOAD( "vph.17",   0x0400000, 0x400000, 0x4f2408e0 );
/*TODO*///	ROM_LOAD( "vph.14",   0x0800000, 0x400000, 0x7a0e1add );
/*TODO*///	ROM_LOAD( "vph.13",   0x0c00000, 0x400000, 0xc51baf99 );
/*TODO*///	ROM_LOAD( "vph.20",   0x1000000, 0x400000, 0x17f2433f );
/*TODO*///	ROM_LOAD( "vph.19",   0x1400000, 0x400000, 0x9ff60250 );
/*TODO*///	ROM_LOAD( "vph.16",   0x1800000, 0x400000, 0x2f41ca75 );
/*TODO*///	ROM_LOAD( "vph.15",   0x1c00000, 0x400000, 0x3ce83c77 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "vph.01",   0x00000, 0x08000, 0x5045dcac );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "vph.02",   0x28000, 0x20000, 0x86b60e59 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "vph.11",   0x000000, 0x200000, 0xe1837d33 );
/*TODO*///	ROM_LOAD( "vph.12",   0x200000, 0x200000, 0xfbd3cd90 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_vhuntj = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vphj.03b", 0x000000, 0x80000, 0x679c3fa9 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vphj.04a", 0x080000, 0x80000, 0xeb6e71e4 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vphj.05a", 0x100000, 0x80000, 0xeaf634ea );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vphj.06a", 0x180000, 0x80000, 0xb70cc6be );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vphj.07a", 0x200000, 0x80000, 0x46ab907d );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vphj.08a", 0x280000, 0x80000, 0x1c00355e );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vphj.09a", 0x300000, 0x80000, 0x026e6f82 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vphj.10a", 0x380000, 0x80000, 0xaadfb3ea );
/*TODO*///
/*TODO*///	ROM_REGION( 0x2000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "vph.18",   0x0000000, 0x400000, 0x64498eed );
/*TODO*///	ROM_LOAD( "vph.17",   0x0400000, 0x400000, 0x4f2408e0 );
/*TODO*///	ROM_LOAD( "vph.14",   0x0800000, 0x400000, 0x7a0e1add );
/*TODO*///	ROM_LOAD( "vph.13",   0x0c00000, 0x400000, 0xc51baf99 );
/*TODO*///	ROM_LOAD( "vph.20",   0x1000000, 0x400000, 0x17f2433f );
/*TODO*///	ROM_LOAD( "vph.19",   0x1400000, 0x400000, 0x9ff60250 );
/*TODO*///	ROM_LOAD( "vph.16",   0x1800000, 0x400000, 0x2f41ca75 );
/*TODO*///	ROM_LOAD( "vph.15",   0x1c00000, 0x400000, 0x3ce83c77 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "vph.01",   0x00000, 0x08000, 0x5045dcac );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "vph.02",   0x28000, 0x20000, 0x86b60e59 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "vph.11",   0x000000, 0x200000, 0xe1837d33 );
/*TODO*///	ROM_LOAD( "vph.12",   0x200000, 0x200000, 0xfbd3cd90 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_rckman2j = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION(CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "rm2j.03",  0x000000, 0x80000, 0xdbaa1437 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "rm2j.04",  0x080000, 0x80000, 0xcf5ba612 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "rm2j.05",  0x100000, 0x80000, 0x02ee9efc );
/*TODO*///
/*TODO*///	ROM_REGION( 0x800000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "rm2.18",   0x000000, 0x200000, 0x12257251 );
/*TODO*///	ROM_LOAD( "rm2.14",   0x200000, 0x200000, 0x9b1f00b4 );
/*TODO*///	ROM_LOAD( "rm2.20",   0x400000, 0x200000, 0xf9b6e786 );
/*TODO*///	ROM_LOAD( "rm2.16",   0x600000, 0x200000, 0xc2bb0c24 );
/*TODO*///
/*TODO*///	ROM_REGION(QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "rm2.01",   0x00000, 0x08000, 0xd18e7859 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "rm2.02",   0x28000, 0x20000, 0xc463ece0 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "rm2.11",   0x000000, 0x200000, 0x2106174d );
/*TODO*///	ROM_LOAD( "rm2.12",   0x200000, 0x200000, 0x546c1636 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_qndream = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION(CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "tqzj.03",  0x000000, 0x80000, 0x7acf3e30 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "tqzj.04",  0x080000, 0x80000, 0xf1044a87 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "tqzj.05",  0x100000, 0x80000, 0x4105ba0e );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "tqzj.06",  0x100000, 0x80000, 0xc371e8a5 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x800000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "tqz.18",   0x000000, 0x200000, 0x42f132ff );
/*TODO*///	ROM_LOAD( "tqz.14",   0x200000, 0x200000, 0x98af88a2 );
/*TODO*///	ROM_LOAD( "tqz.20",   0x400000, 0x200000, 0xb2e128a3 );
/*TODO*///	ROM_LOAD( "tqz.16",   0x600000, 0x200000, 0xdf82d491 );
/*TODO*///
/*TODO*///	ROM_REGION(QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "tqz.01",   0x00000, 0x08000, 0xe9ce9d0a );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "tqz.11",   0x000000, 0x200000, 0x78e7884f );
/*TODO*///	ROM_LOAD( "tqz.12",   0x200000, 0x200000, 0x2e049b13 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_sfa = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfzu.03a",  0x000000, 0x80000, 0x49fc7db9 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfz.04a",   0x080000, 0x80000, 0x5f99e9a5 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfz.05",    0x100000, 0x80000, 0x0810544d );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfz.06",    0x180000, 0x80000, 0x806e8f38 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x800000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "sfz.18",   0x000000, 0x200000, 0x41a1e790 );
/*TODO*///	ROM_LOAD( "sfz.14",   0x200000, 0x200000, 0x90fefdb3 );
/*TODO*///	ROM_LOAD( "sfz.20",   0x400000, 0x200000, 0xa549df98 );
/*TODO*///	ROM_LOAD( "sfz.16",   0x600000, 0x200000, 0x5354c948 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "sfz.01",   0x00000, 0x08000, 0xffffec7d );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "sfz.02",   0x28000, 0x20000, 0x45f46a08 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "sfz.11",   0x000000, 0x200000, 0xc4b093cd );
/*TODO*///	ROM_LOAD( "sfz.12",   0x200000, 0x200000, 0x8bdbc4b4 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_sfzj = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfzj.03a",  0x000000, 0x80000, 0x844220c2 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfz.04a",   0x080000, 0x80000, 0x5f99e9a5 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfz.05",    0x100000, 0x80000, 0x0810544d );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfz.06",    0x180000, 0x80000, 0x806e8f38 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x800000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "sfz.18",   0x000000, 0x200000, 0x41a1e790 );
/*TODO*///	ROM_LOAD( "sfz.14",   0x200000, 0x200000, 0x90fefdb3 );
/*TODO*///	ROM_LOAD( "sfz.20",   0x400000, 0x200000, 0xa549df98 );
/*TODO*///	ROM_LOAD( "sfz.16",   0x600000, 0x200000, 0x5354c948 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "sfz.01",   0x00000, 0x08000, 0xffffec7d );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "sfz.02",   0x28000, 0x20000, 0x45f46a08 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "sfz.11",   0x000000, 0x200000, 0xc4b093cd );
/*TODO*///	ROM_LOAD( "sfz.12",   0x200000, 0x200000, 0x8bdbc4b4 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_sfar1 = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfzu.03b",  0x000000, 0x80000, 0xebf2054d );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfz.04b",   0x080000, 0x80000, 0x8b73b0e5 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfz.05",    0x100000, 0x80000, 0x0810544d );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfz.06",    0x180000, 0x80000, 0x806e8f38 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x800000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "sfz.18",   0x000000, 0x200000, 0x41a1e790 );
/*TODO*///	ROM_LOAD( "sfz.14",   0x200000, 0x200000, 0x90fefdb3 );
/*TODO*///	ROM_LOAD( "sfz.20",   0x400000, 0x200000, 0xa549df98 );
/*TODO*///	ROM_LOAD( "sfz.16",   0x600000, 0x200000, 0x5354c948 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "sfz.01",   0x00000, 0x08000, 0xffffec7d );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "sfz.02",   0x28000, 0x20000, 0x45f46a08 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "sfz.11",   0x000000, 0x200000, 0xc4b093cd );
/*TODO*///	ROM_LOAD( "sfz.12",   0x200000, 0x200000, 0x8bdbc4b4 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_sfzjr1 = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfzj.03b",  0x000000, 0x80000, 0xf5444120 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfz.04b",   0x080000, 0x80000, 0x8b73b0e5 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfz.05",    0x100000, 0x80000, 0x0810544d );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfz.06",    0x180000, 0x80000, 0x806e8f38 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x800000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "sfz.18",   0x000000, 0x200000, 0x41a1e790 );
/*TODO*///	ROM_LOAD( "sfz.14",   0x200000, 0x200000, 0x90fefdb3 );
/*TODO*///	ROM_LOAD( "sfz.20",   0x400000, 0x200000, 0xa549df98 );
/*TODO*///	ROM_LOAD( "sfz.16",   0x600000, 0x200000, 0x5354c948 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "sfz.01",   0x00000, 0x08000, 0xffffec7d );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "sfz.02",   0x28000, 0x20000, 0x45f46a08 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "sfz.11",   0x000000, 0x200000, 0xc4b093cd );
/*TODO*///	ROM_LOAD( "sfz.12",   0x200000, 0x200000, 0x8bdbc4b4 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_sfz2j = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sz2j.03a", 0x000000, 0x80000, 0x97461e28 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sz2j.04a", 0x080000, 0x80000, 0xae4851a9 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sz2j.05a", 0x100000, 0x80000, 0x98e8e992 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sz2j.06a", 0x180000, 0x80000, 0x5b1d49c0 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sz2j.07a", 0x200000, 0x80000, 0xd910b2a2 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sz2j.08a", 0x280000, 0x80000, 0x0fe8585d );
/*TODO*///
/*TODO*///	ROM_REGION( 0x1400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///    /* One of these planes is corrupt */
/*TODO*///	ROM_LOAD( "sz2.18",   0x0000000, 0x100000, 0x4bc3c8bc );
/*TODO*///	ROM_LOAD( "sz2.17",   0x0100000, 0x400000, 0xe01b4588 );
/*TODO*///	ROM_LOAD( "sz2.14",   0x0500000, 0x100000, 0x08c6bb9c );
/*TODO*///	ROM_LOAD( "sz2.13",   0x0600000, 0x400000, 0x1858afce );
/*TODO*///	ROM_LOAD( "sz2.20",   0x0a00000, 0x100000, 0x39e674c0 );
/*TODO*///	ROM_LOAD( "sz2.19",   0x0b00000, 0x400000, 0x0feeda64 );
/*TODO*///	ROM_LOAD( "sz2.16",   0x0f00000, 0x100000, 0xca161c9c );
/*TODO*///	ROM_LOAD( "sz2.15",   0x1000000, 0x400000, 0x96fcf35c );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "sz2.01",   0x00000, 0x08000, 0x1bc323cf );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "sz2.02",   0x28000, 0x20000, 0xba6a5013 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "sz2.11",   0x000000, 0x200000, 0xaa47a601 );
/*TODO*///	ROM_LOAD( "sz2.12",   0x200000, 0x200000, 0x2237bc53 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_sfz2a = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "szaj.03a", 0x000000, 0x80000, 0xa3802fe3 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "szaj.04a", 0x080000, 0x80000, 0xe7ca87c7 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "szaj.05a", 0x100000, 0x80000, 0xc88ebf88 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "szaj.06a", 0x180000, 0x80000, 0x35ed5b7a );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "szaj.07a", 0x200000, 0x80000, 0x975dcb3e );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "szaj.08a", 0x280000, 0x80000, 0xdc73f2d7 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x1400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///    /* One of these planes is corrupt */
/*TODO*///	ROM_LOAD( "sza.18",   0x0000000, 0x100000, 0x4bc3c8bc );
/*TODO*///	ROM_LOAD( "sza.17",   0x0100000, 0x400000, 0xe01b4588 );
/*TODO*///	ROM_LOAD( "sza.14",   0x0500000, 0x100000, 0x0560c6aa );
/*TODO*///	ROM_LOAD( "sza.13",   0x0600000, 0x400000, 0x4d1f1f22 );
/*TODO*///	ROM_LOAD( "sza.20",   0x0a00000, 0x100000, 0x39e674c0 );
/*TODO*///	ROM_LOAD( "sza.19",   0x0b00000, 0x400000, 0x0feeda64 );
/*TODO*///	ROM_LOAD( "sza.16",   0x0f00000, 0x100000, 0xae940f87 );
/*TODO*///	ROM_LOAD( "sza.15",   0x1000000, 0x400000, 0x19cea680 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "sza.01",   0x00000, 0x08000, 0x1bc323cf );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "sza.02",   0x28000, 0x20000, 0xba6a5013 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "sza.11",   0x000000, 0x200000, 0xaa47a601 );
/*TODO*///	ROM_LOAD( "sza.12",   0x200000, 0x200000, 0x2237bc53 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_sfa3 = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sz3u.03", 0x000000, 0x80000, 0xb5984a19 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sz3u.04", 0x080000, 0x80000, 0x7e8158ba );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sz3u.05", 0x100000, 0x80000, 0x9b21518a );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sz3u.06", 0x180000, 0x80000, 0xe7abc3a7 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sz3u.07", 0x200000, 0x80000, 0xec4c0cfd );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sz3u.08", 0x280000, 0x80000, 0x5c7e7240 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sz3u.09", 0x300000, 0x80000, 0xc5589553 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sz3.10", 0x380000, 0x80000, 0xa9717252 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x2000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "sz3.18",   0x0000000, 0x400000, 0x40631ed5 );
/*TODO*///	ROM_LOAD( "sz3.17",   0x0400000, 0x400000, 0xd6e98147 );
/*TODO*///	ROM_LOAD( "sz3.14",   0x0800000, 0x400000, 0x5ff98297 );
/*TODO*///	ROM_LOAD( "sz3.13",   0x0c00000, 0x400000, 0x0f7a60d9 );
/*TODO*///	ROM_LOAD( "sz3.20",   0x1000000, 0x400000, 0x763409b4 );
/*TODO*///	ROM_LOAD( "sz3.19",   0x1400000, 0x400000, 0xf31a728a );
/*TODO*///	ROM_LOAD( "sz3.16",   0x1800000, 0x400000, 0x52b5bdee );
/*TODO*///	ROM_LOAD( "sz3.15",   0x1c00000, 0x400000, 0x8e933741 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "sz3.01",   0x00000, 0x08000, 0xde810084 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "sz3.02",   0x28000, 0x20000, 0x72445dc4 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x800000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "sz3.11",   0x000000, 0x400000, 0x1c89eed1 );
/*TODO*///	ROM_LOAD( "sz3.12",   0x400000, 0x400000, 0xf392b13a );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_sfz3 = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sz3j.03", 0x000000, 0x80000, 0x6ee0beae );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sz3j.04", 0x080000, 0x80000, 0xa6e2978d );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sz3j.05", 0x100000, 0x80000, 0x05964b7d );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sz3j.06", 0x180000, 0x80000, 0x78ce2179 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sz3j.07", 0x200000, 0x80000, 0x398bf52f );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sz3j.08", 0x280000, 0x80000, 0x866d0588 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sz3j.09", 0x300000, 0x80000, 0x2180892c );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sz3.10", 0x380000, 0x80000, 0xa9717252 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x2000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "sz3.18",   0x0000000, 0x400000, 0x40631ed5 );
/*TODO*///	ROM_LOAD( "sz3.17",   0x0400000, 0x400000, 0xd6e98147 );
/*TODO*///	ROM_LOAD( "sz3.14",   0x0800000, 0x400000, 0x5ff98297 );
/*TODO*///	ROM_LOAD( "sz3.13",   0x0c00000, 0x400000, 0x0f7a60d9 );
/*TODO*///	ROM_LOAD( "sz3.20",   0x1000000, 0x400000, 0x763409b4 );
/*TODO*///	ROM_LOAD( "sz3.19",   0x1400000, 0x400000, 0xf31a728a );
/*TODO*///	ROM_LOAD( "sz3.16",   0x1800000, 0x400000, 0x52b5bdee );
/*TODO*///	ROM_LOAD( "sz3.15",   0x1c00000, 0x400000, 0x8e933741 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "sz3.01",   0x00000, 0x08000, 0xde810084 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "sz3.02",   0x28000, 0x20000, 0x72445dc4 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x800000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "sz3.11",   0x000000, 0x400000, 0x1c89eed1 );
/*TODO*///	ROM_LOAD( "sz3.12",   0x400000, 0x400000, 0xf392b13a );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_sgemf = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pcfu.03", 0x000000, 0x80000, 0xac2e8566 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pcf.04",  0x080000, 0x80000, 0xf4314c96 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pcf.05",  0x100000, 0x80000, 0x215655f6 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pcf.06",  0x180000, 0x80000, 0xea6f13ea );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pcf.07",  0x200000, 0x80000, 0x5ac6d5ea );
/*TODO*///
/*TODO*///	ROM_REGION( 0x1400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///    /* One of these planes is corrupt */
/*TODO*///	ROM_LOAD( "pcf.18",   0x0000000, 0x100000, 0x756c3754 );
/*TODO*///	ROM_LOAD( "pcf.17",   0x0100000, 0x400000, 0x1097e035 );
/*TODO*///	ROM_LOAD( "pcf.14",   0x0500000, 0x100000, 0x0383897c );
/*TODO*///	ROM_LOAD( "pcf.13",   0x0600000, 0x400000, 0x22d72ab9 );
/*TODO*///	ROM_LOAD( "pcf.20",   0x0a00000, 0x100000, 0x9ec9277d );
/*TODO*///	ROM_LOAD( "pcf.19",   0x0b00000, 0x400000, 0xd362d874 );
/*TODO*///	ROM_LOAD( "pcf.16",   0x0f00000, 0x100000, 0x76f91084 );
/*TODO*///	ROM_LOAD( "pcf.15",   0x1000000, 0x400000, 0x16a4813c );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "pcf.01",   0x00000, 0x08000, 0xe5af9fcd );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "pcf.02",   0x28000, 0x20000, 0x8630e818 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x800000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "pcf.11",   0x000000, 0x400000, 0xa5dea005 );
/*TODO*///	ROM_LOAD( "pcf.12",   0x400000, 0x400000, 0x4ce235fe );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_pfghtj = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pcfj.03", 0x000000, 0x80000, 0x681da43e );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pcf.04",  0x080000, 0x80000, 0xf4314c96 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pcf.05",  0x100000, 0x80000, 0x215655f6 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pcf.06",  0x180000, 0x80000, 0xea6f13ea );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pcf.07",  0x200000, 0x80000, 0x5ac6d5ea );
/*TODO*///
/*TODO*///	ROM_REGION( 0x1400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///    /* One of these planes is corrupt */
/*TODO*///	ROM_LOAD( "pcf.18",   0x0000000, 0x100000, 0x756c3754 );
/*TODO*///	ROM_LOAD( "pcf.17",   0x0100000, 0x400000, 0x1097e035 );
/*TODO*///	ROM_LOAD( "pcf.14",   0x0500000, 0x100000, 0x0383897c );
/*TODO*///	ROM_LOAD( "pcf.13",   0x0600000, 0x400000, 0x22d72ab9 );
/*TODO*///	ROM_LOAD( "pcf.20",   0x0a00000, 0x100000, 0x9ec9277d );
/*TODO*///	ROM_LOAD( "pcf.19",   0x0b00000, 0x400000, 0xd362d874 );
/*TODO*///	ROM_LOAD( "pcf.16",   0x0f00000, 0x100000, 0x76f91084 );
/*TODO*///	ROM_LOAD( "pcf.15",   0x1000000, 0x400000, 0x16a4813c );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "pcf.01",   0x00000, 0x08000, 0xe5af9fcd );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "pcf.02",   0x28000, 0x20000, 0x8630e818 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x800000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "pcf.11",   0x000000, 0x400000, 0xa5dea005 );
/*TODO*///	ROM_LOAD( "pcf.12",   0x400000, 0x400000, 0x4ce235fe );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_slam2e = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "smbe.03b", 0x000000, 0x80000, 0xb8016278 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "smbe.04b", 0x080000, 0x80000, 0x18c4c447 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "smbe.05b", 0x100000, 0x80000, 0x18ebda7f );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "smbe.06b", 0x180000, 0x80000, 0x89c80007 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "smb.07b",  0x200000, 0x80000, 0xb9a11577 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "smb.08b",  0x280000, 0x80000, 0xf931b76b );
/*TODO*///
/*TODO*///	ROM_REGION( 0x1200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "smb.25",   0x0000000, 0x080000, 0x82d6c4ec );
/*TODO*///	ROM_LOAD( "smb.18",   0x0080000, 0x200000, 0x4ded3910 );
/*TODO*///	ROM_LOAD( "smb.17",   0x0280000, 0x200000, 0x51800f0f );
/*TODO*///	ROM_LOAD( "smb.21",   0x0480000, 0x080000, 0x0a08c5fc );
/*TODO*///	ROM_LOAD( "smb.14",   0x0500000, 0x200000, 0xe5bfd0e7 );
/*TODO*///	ROM_LOAD( "smb.13",   0x0700000, 0x200000, 0xd9b2d1de );
/*TODO*///	ROM_LOAD( "smb.27",   0x0900000, 0x080000, 0x9b48678b );
/*TODO*///	ROM_LOAD( "smb.20",   0x0980000, 0x200000, 0x26ea1ec5 );
/*TODO*///	ROM_LOAD( "smb.19",   0x0b80000, 0x200000, 0x35757e96 );
/*TODO*///	ROM_LOAD( "smb.23",   0x0d80000, 0x080000, 0x0911b6c4 );
/*TODO*///	ROM_LOAD( "smb.16",   0x0e00000, 0x200000, 0xc56c0866 );
/*TODO*///	ROM_LOAD( "smb.15",   0x1000000, 0x200000, 0x9a766d92 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "smb.01",   0x00000, 0x08000, 0x0abc229a );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "smb.02",   0x28000, 0x20000, 0xd051679a );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "smb.11",   0x000000, 0x200000, 0xC56935f9 );
/*TODO*///	ROM_LOAD( "smb.12",   0x200000, 0x200000, 0x955b0782 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_smbomber = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "smbj.03", 0x000000, 0x80000, 0x52eafb10 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "smbj.04", 0x080000, 0x80000, 0xaa6e8078 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "smbj.05", 0x100000, 0x80000, 0xb69e7d5f );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "smbj.06", 0x180000, 0x80000, 0x8d857b56 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "smb.07b",  0x200000, 0x80000, 0xb9a11577 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "smb.08b",  0x280000, 0x80000, 0xf931b76b );
/*TODO*///
/*TODO*///	ROM_REGION( 0x1200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "smb.25",   0x0000000, 0x080000, 0x82d6c4ec );
/*TODO*///	ROM_LOAD( "smb.18",   0x0080000, 0x200000, 0x4ded3910 );
/*TODO*///	ROM_LOAD( "smb.17",   0x0280000, 0x200000, 0x51800f0f );
/*TODO*///	ROM_LOAD( "smb.21",   0x0480000, 0x080000, 0x0a08c5fc );
/*TODO*///	ROM_LOAD( "smb.14",   0x0500000, 0x200000, 0xe5bfd0e7 );
/*TODO*///	ROM_LOAD( "smb.13",   0x0700000, 0x200000, 0xd9b2d1de );
/*TODO*///	ROM_LOAD( "smb.27",   0x0900000, 0x080000, 0x9b48678b );
/*TODO*///	ROM_LOAD( "smb.20",   0x0980000, 0x200000, 0x26ea1ec5 );
/*TODO*///	ROM_LOAD( "smb.19",   0x0b80000, 0x200000, 0x35757e96 );
/*TODO*///	ROM_LOAD( "smb.23",   0x0d80000, 0x080000, 0x0911b6c4 );
/*TODO*///	ROM_LOAD( "smb.16",   0x0e00000, 0x200000, 0xc56c0866 );
/*TODO*///	ROM_LOAD( "smb.15",   0x1000000, 0x200000, 0x9a766d92 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "smb.01",   0x00000, 0x08000, 0x0abc229a );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "smb.02",   0x28000, 0x20000, 0xd051679a );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "smb.11",   0x000000, 0x200000, 0xC56935f9 );
/*TODO*///	ROM_LOAD( "smb.12",   0x200000, 0x200000, 0x955b0782 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_spf2t = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION(CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pzfu.03a",  0x000000, 0x80000, 0x346e62ef );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pzf.04a",   0x080000, 0x80000, 0xb80649e2 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "pzf.18",   0x000000, 0x100000, 0xe43aac33 );
/*TODO*///	ROM_LOAD( "pzf.14",   0x100000, 0x100000, 0x2d4881cb );
/*TODO*///	ROM_LOAD( "pzf.20",   0x200000, 0x100000, 0x7f536ff1 );
/*TODO*///	ROM_LOAD( "pzf.16",   0x300000, 0x100000, 0x4b0fd1be );
/*TODO*///
/*TODO*///	ROM_REGION(QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "pzf.01",   0x00000, 0x08000, 0x600fb2a3 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "pzf.02",   0x28000, 0x20000, 0x496076e0 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "pzf.11",   0x000000, 0x200000, 0x78442743 );
/*TODO*///	ROM_LOAD( "pzf.12",   0x200000, 0x200000, 0x399d2c7b );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_spf2xj = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION(CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pzfj.03a",  0x000000, 0x80000, 0x2070554a );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "pzf.04a",   0x080000, 0x80000, 0xb80649e2 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "pzf.18",   0x000000, 0x100000, 0xe43aac33 );
/*TODO*///	ROM_LOAD( "pzf.14",   0x100000, 0x100000, 0x2d4881cb );
/*TODO*///	ROM_LOAD( "pzf.20",   0x200000, 0x100000, 0x7f536ff1 );
/*TODO*///	ROM_LOAD( "pzf.16",   0x300000, 0x100000, 0x4b0fd1be );
/*TODO*///
/*TODO*///	ROM_REGION(QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "pzf.01",   0x00000, 0x08000, 0x600fb2a3 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "pzf.02",   0x28000, 0x20000, 0x496076e0 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "pzf.11",   0x000000, 0x200000, 0x78442743 );
/*TODO*///	ROM_LOAD( "pzf.12",   0x200000, 0x200000, 0x399d2c7b );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_ssf2 = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "ssfu.03a", 0x000000, 0x80000, 0x72f29c33 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "ssfu.04a", 0x080000, 0x80000, 0x935cea44 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "ssfu.05",  0x100000, 0x80000, 0xa0acb28a );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "ssfu.06",  0x180000, 0x80000, 0x47413dcf );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "ssfu.07",  0x200000, 0x80000, 0xe6066077 );
/*TODO*///
/*TODO*///	ROM_REGION( 0xc00000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "ssf.18",   0x000000, 0x100000, 0xf5b1b336 );
/*TODO*///	ROM_LOAD( "ssf.17",   0x100000, 0x200000, 0x59f5267b );
/*TODO*///	ROM_LOAD( "ssf.14",   0x300000, 0x100000, 0xb7cc32e7 );
/*TODO*///	ROM_LOAD( "ssf.13",   0x400000, 0x200000, 0xcf94d275 );
/*TODO*///	ROM_LOAD( "ssf.20",   0x600000, 0x100000, 0x459d5c6b );
/*TODO*///	ROM_LOAD( "ssf.19",   0x700000, 0x200000, 0x8dc0d86e );
/*TODO*///	ROM_LOAD( "ssf.16",   0x900000, 0x100000, 0x8376ad18 );
/*TODO*///	ROM_LOAD( "ssf.15",   0xa00000, 0x200000, 0x9073a0d4 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "ssf.01",   0x00000, 0x08000, 0xeb247e8c );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "ssf.q01",  0x000000, 0x080000, 0xa6f9da5c );
/*TODO*///	ROM_LOAD( "ssf.q02",  0x080000, 0x080000, 0x8c66ae26 );
/*TODO*///	ROM_LOAD( "ssf.q03",  0x100000, 0x080000, 0x695cc2ca );
/*TODO*///	ROM_LOAD( "ssf.q04",  0x180000, 0x080000, 0x9d9ebe32 );
/*TODO*///	ROM_LOAD( "ssf.q05",  0x200000, 0x080000, 0x4770e7b7 );
/*TODO*///	ROM_LOAD( "ssf.q06",  0x280000, 0x080000, 0x4e79c951 );
/*TODO*///	ROM_LOAD( "ssf.q07",  0x300000, 0x080000, 0xcdd14313 );
/*TODO*///	ROM_LOAD( "ssf.q08",  0x380000, 0x080000, 0x6f5a088c );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_ssf2a = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "ssfa.03a", 0x000000, 0x80000, 0xd2a3c520 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "ssfa.04a", 0x080000, 0x80000, 0x5d873642 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "ssfa.05a", 0x100000, 0x80000, 0xf8fb4de2 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "ssfa.06a", 0x180000, 0x80000, 0xaa8acee7 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "ssfa.07a", 0x200000, 0x80000, 0x36e29217 );
/*TODO*///
/*TODO*///	ROM_REGION( 0xc00000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "ssf.18",   0x000000, 0x100000, 0xf5b1b336 );
/*TODO*///	ROM_LOAD( "ssf.17",   0x100000, 0x200000, 0x59f5267b );
/*TODO*///	ROM_LOAD( "ssf.14",   0x300000, 0x100000, 0xb7cc32e7 );
/*TODO*///	ROM_LOAD( "ssf.13",   0x400000, 0x200000, 0xcf94d275 );
/*TODO*///	ROM_LOAD( "ssf.20",   0x600000, 0x100000, 0x459d5c6b );
/*TODO*///	ROM_LOAD( "ssf.19",   0x700000, 0x200000, 0x8dc0d86e );
/*TODO*///	ROM_LOAD( "ssf.16",   0x900000, 0x100000, 0x8376ad18 );
/*TODO*///	ROM_LOAD( "ssf.15",   0xa00000, 0x200000, 0x9073a0d4 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "ssf.01",   0x00000, 0x08000, 0xeb247e8c );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "ssf.q01",  0x000000, 0x080000, 0xa6f9da5c );
/*TODO*///	ROM_LOAD( "ssf.q02",  0x080000, 0x080000, 0x8c66ae26 );
/*TODO*///	ROM_LOAD( "ssf.q03",  0x100000, 0x080000, 0x695cc2ca );
/*TODO*///	ROM_LOAD( "ssf.q04",  0x180000, 0x080000, 0x9d9ebe32 );
/*TODO*///	ROM_LOAD( "ssf.q05",  0x200000, 0x080000, 0x4770e7b7 );
/*TODO*///	ROM_LOAD( "ssf.q06",  0x280000, 0x080000, 0x4e79c951 );
/*TODO*///	ROM_LOAD( "ssf.q07",  0x300000, 0x080000, 0xcdd14313 );
/*TODO*///	ROM_LOAD( "ssf.q08",  0x380000, 0x080000, 0x6f5a088c );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_ssf2j = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "ssfj.03", 0x000000, 0x80000, 0x7eb0efed );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "ssfj.04", 0x080000, 0x80000, 0xd7322164 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "ssfj.05", 0x100000, 0x80000, 0x0918d19a );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "ssfj.06", 0x180000, 0x80000, 0xd808a6cd );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "ssfj.07", 0x200000, 0x80000, 0xeb6a9b1b );
/*TODO*///
/*TODO*///	ROM_REGION( 0xc00000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "ssf.18",   0x000000, 0x100000, 0xf5b1b336 );
/*TODO*///	ROM_LOAD( "ssf.17",   0x100000, 0x200000, 0x59f5267b );
/*TODO*///	ROM_LOAD( "ssf.14",   0x300000, 0x100000, 0xb7cc32e7 );
/*TODO*///	ROM_LOAD( "ssf.13",   0x400000, 0x200000, 0xcf94d275 );
/*TODO*///	ROM_LOAD( "ssf.20",   0x600000, 0x100000, 0x459d5c6b );
/*TODO*///	ROM_LOAD( "ssf.19",   0x700000, 0x200000, 0x8dc0d86e );
/*TODO*///	ROM_LOAD( "ssf.16",   0x900000, 0x100000, 0x8376ad18 );
/*TODO*///	ROM_LOAD( "ssf.15",   0xa00000, 0x200000, 0x9073a0d4 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "ssf.01",   0x00000, 0x08000, 0xeb247e8c );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "ssf.q01",  0x000000, 0x080000, 0xa6f9da5c );
/*TODO*///	ROM_LOAD( "ssf.q02",  0x080000, 0x080000, 0x8c66ae26 );
/*TODO*///	ROM_LOAD( "ssf.q03",  0x100000, 0x080000, 0x695cc2ca );
/*TODO*///	ROM_LOAD( "ssf.q04",  0x180000, 0x080000, 0x9d9ebe32 );
/*TODO*///	ROM_LOAD( "ssf.q05",  0x200000, 0x080000, 0x4770e7b7 );
/*TODO*///	ROM_LOAD( "ssf.q06",  0x280000, 0x080000, 0x4e79c951 );
/*TODO*///	ROM_LOAD( "ssf.q07",  0x300000, 0x080000, 0xcdd14313 );
/*TODO*///	ROM_LOAD( "ssf.q08",  0x380000, 0x080000, 0x6f5a088c );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_ssf2t = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfxu.03c", 0x000000, 0x80000, 0x86e4a335 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfxu.04a", 0x080000, 0x80000, 0x532b5ffd );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfxu.05",  0x100000, 0x80000, 0xffa3c6de );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfxu.06a", 0x180000, 0x80000, 0xe4c04c99 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfxu.07",  0x200000, 0x80000, 0xd8199e41 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfxu.08",  0x280000, 0x80000, 0xb3c71810 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfx.09",   0x300000, 0x80000, 0x642fae3f );
/*TODO*///
/*TODO*///	ROM_REGION( 0x1000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "sfx.25",   0x0000000, 0x100000, 0x1ee90208 );
/*TODO*///	ROM_LOAD( "sfx.18",   0x0100000, 0x100000, 0xf5b1b336 );
/*TODO*///	ROM_LOAD( "sfx.17",   0x0200000, 0x200000, 0x59f5267b );
/*TODO*///	ROM_LOAD( "sfx.21",   0x0400000, 0x100000, 0xe32854af );
/*TODO*///	ROM_LOAD( "sfx.14",   0x0500000, 0x100000, 0xb7cc32e7 );
/*TODO*///	ROM_LOAD( "sfx.13",   0x0600000, 0x200000, 0xcf94d275 );
/*TODO*///	ROM_LOAD( "sfx.27",   0x0800000, 0x100000, 0xf814400f );
/*TODO*///	ROM_LOAD( "sfx.20",   0x0900000, 0x100000, 0x459d5c6b );
/*TODO*///	ROM_LOAD( "sfx.19",   0x0a00000, 0x200000, 0x8dc0d86e );
/*TODO*///	ROM_LOAD( "sfx.23",   0x0c00000, 0x100000, 0x760f2927 );
/*TODO*///	ROM_LOAD( "sfx.16",   0x0d00000, 0x100000, 0x8376ad18 );
/*TODO*///	ROM_LOAD( "sfx.15",   0x0e00000, 0x200000, 0x9073a0d4 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "sfx.01",   0x00000, 0x08000, 0xb47b8835 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "sfx.02",   0x28000, 0x20000, 0x0022633f );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "sfx.11",   0x000000, 0x200000, 0x9bdbd476 );
/*TODO*///	ROM_LOAD( "sfx.12",   0x200000, 0x200000, 0xa05e3aab );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_ssf2xj = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfxj.03c", 0x000000, 0x80000, 0xa7417b79 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfxj.04a", 0x080000, 0x80000, 0xaf7767b4 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfxj.05",  0x100000, 0x80000, 0xf4ff18f5 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfxj.06a", 0x180000, 0x80000, 0x260d0370 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfxj.07",  0x200000, 0x80000, 0x1324d02a );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfxj.08",  0x280000, 0x80000, 0x2de76f10 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "sfx.09",   0x300000, 0x80000, 0x642fae3f );
/*TODO*///
/*TODO*///	ROM_REGION( 0x1000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "sfx.25",   0x0000000, 0x100000, 0x1ee90208 );
/*TODO*///	ROM_LOAD( "sfx.18",   0x0100000, 0x100000, 0xf5b1b336 );
/*TODO*///	ROM_LOAD( "sfx.17",   0x0200000, 0x200000, 0x59f5267b );
/*TODO*///	ROM_LOAD( "sfx.21",   0x0400000, 0x100000, 0xe32854af );
/*TODO*///	ROM_LOAD( "sfx.14",   0x0500000, 0x100000, 0xb7cc32e7 );
/*TODO*///	ROM_LOAD( "sfx.13",   0x0600000, 0x200000, 0xcf94d275 );
/*TODO*///	ROM_LOAD( "sfx.27",   0x0800000, 0x100000, 0xf814400f );
/*TODO*///	ROM_LOAD( "sfx.20",   0x0900000, 0x100000, 0x459d5c6b );
/*TODO*///	ROM_LOAD( "sfx.19",   0x0a00000, 0x200000, 0x8dc0d86e );
/*TODO*///	ROM_LOAD( "sfx.23",   0x0c00000, 0x100000, 0x760f2927 );
/*TODO*///	ROM_LOAD( "sfx.16",   0x0d00000, 0x100000, 0x8376ad18 );
/*TODO*///	ROM_LOAD( "sfx.15",   0x0e00000, 0x200000, 0x9073a0d4 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "sfx.01",   0x00000, 0x08000, 0xb47b8835 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "sfx.02",   0x28000, 0x20000, 0x0022633f );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "sfx.11",   0x000000, 0x200000, 0x9bdbd476 );
/*TODO*///	ROM_LOAD( "sfx.12",   0x200000, 0x200000, 0xa05e3aab );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_vhunt2 = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vh2j.03", 0x000000, 0x80000, 0x1a5feb13 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vh2j.04", 0x080000, 0x80000, 0x434611a5 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vh2j.05", 0x100000, 0x80000, 0xde34f624 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vh2j.06", 0x180000, 0x80000, 0x6a3b9897 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vh2j.07", 0x200000, 0x80000, 0xb021c029 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vh2j.08", 0x280000, 0x80000, 0xac873dff );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vh2j.09", 0x300000, 0x80000, 0xeaefce9c );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vh2j.10", 0x380000, 0x80000, 0x11730952 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x2000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "vh2.18",  0x0000000, 0x400000, 0x778dc4f6 );
/*TODO*///	ROM_LOAD( "vh2.17",  0x0400000, 0x400000, 0xb31d00c9 );
/*TODO*///	ROM_LOAD( "vh2.14",  0x0800000, 0x400000, 0xcd09bd63 );
/*TODO*///	ROM_LOAD( "vh2.13",  0x0c00000, 0x400000, 0x3b02ddaa );
/*TODO*///	ROM_LOAD( "vh2.20",  0x1000000, 0x400000, 0x605d9d1d );
/*TODO*///	ROM_LOAD( "vh2.19",  0x1400000, 0x400000, 0x149be3ab );
/*TODO*///	ROM_LOAD( "vh2.16",  0x1800000, 0x400000, 0xe0182c15 );
/*TODO*///	ROM_LOAD( "vh2.15",  0x1c00000, 0x400000, 0x4e40de66 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "vh2.01",  0x00000, 0x08000, 0x67b9f779 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "vh2.02",  0x28000, 0x20000, 0xaaf15fcb );
/*TODO*///
/*TODO*///	ROM_REGION( 0x800000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "vh2.11",  0x000000, 0x400000, 0x38922efd );
/*TODO*///	ROM_LOAD( "vh2.12",  0x400000, 0x400000, 0x6e2430af );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_vsav = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vm3u.03d", 0x000000, 0x80000, 0x1f295274 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vm3u.04d", 0x080000, 0x80000, 0xc46adf81 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vm3u.05a", 0x100000, 0x80000, 0x4118e00f );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vm3u.06a", 0x180000, 0x80000, 0x2f4fd3a9 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vm3u.07b", 0x200000, 0x80000, 0xcbda91b8 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vm3u.08a", 0x280000, 0x80000, 0x6ca47259 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vm3u.09b", 0x300000, 0x80000, 0xf4a339e3 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vm3u.10b", 0x380000, 0x80000, 0xfffbb5b8 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x2000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "vm3.18",   0x0000000, 0x400000, 0xdf9a9f47 );
/*TODO*///	ROM_LOAD( "vm3.17",   0x0400000, 0x400000, 0x6b89445e );
/*TODO*///	ROM_LOAD( "vm3.14",   0x0800000, 0x400000, 0xc1a28e6c );
/*TODO*///	ROM_LOAD( "vm3.13",   0x0c00000, 0x400000, 0xfd8a11eb );
/*TODO*///	ROM_LOAD( "vm3.20",   0x1000000, 0x400000, 0xc22fc3d9 );
/*TODO*///	ROM_LOAD( "vm3.19",   0x1400000, 0x400000, 0x3830fdc7 );
/*TODO*///	ROM_LOAD( "vm3.16",   0x1800000, 0x400000, 0x194a7304 );
/*TODO*///	ROM_LOAD( "vm3.15",   0x1c00000, 0x400000, 0xdd1e7d4e );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "vm3.01",   0x00000, 0x08000, 0xf778769b );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "vm3.02",   0x28000, 0x20000, 0xcc09faa1 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x800000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "vm3.11",   0x000000, 0x400000, 0xe80e956e );
/*TODO*///	ROM_LOAD( "vm3.12",   0x400000, 0x400000, 0x9cd71557 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_vsavj = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vm3j.03d", 0x000000, 0x80000, 0x2a2e74a4 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vm3j.04d", 0x080000, 0x80000, 0x1c2427bc );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vm3j.05a", 0x100000, 0x80000, 0x95ce88d5 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vm3j.06b", 0x180000, 0x80000, 0x2c4297e0 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vm3j.07b", 0x200000, 0x80000, 0xa38aaae7 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vm3j.08a", 0x280000, 0x80000, 0x5773e5c9 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vm3j.09b", 0x300000, 0x80000, 0xd064f8b9 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vm3j.10b", 0x380000, 0x80000, 0x434518e9 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x2000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "vm3.18",   0x0000000, 0x400000, 0xdf9a9f47 );
/*TODO*///	ROM_LOAD( "vm3.17",   0x0400000, 0x400000, 0x6b89445e );
/*TODO*///	ROM_LOAD( "vm3.14",   0x0800000, 0x400000, 0xc1a28e6c );
/*TODO*///	ROM_LOAD( "vm3.13",   0x0c00000, 0x400000, 0xfd8a11eb );
/*TODO*///	ROM_LOAD( "vm3.20",   0x1000000, 0x400000, 0xc22fc3d9 );
/*TODO*///	ROM_LOAD( "vm3.19",   0x1400000, 0x400000, 0x3830fdc7 );
/*TODO*///	ROM_LOAD( "vm3.16",   0x1800000, 0x400000, 0x194a7304 );
/*TODO*///	ROM_LOAD( "vm3.15",   0x1c00000, 0x400000, 0xdd1e7d4e );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "vm3.01",   0x00000, 0x08000, 0xf778769b );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "vm3.02",   0x28000, 0x20000, 0xcc09faa1 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x800000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "vm3.11",   0x000000, 0x400000, 0xe80e956e );
/*TODO*///	ROM_LOAD( "vm3.12",   0x400000, 0x400000, 0x9cd71557 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_vsav2 = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vs2j.03", 0x000000, 0x80000, 0x89fd86b4 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vs2j.04", 0x080000, 0x80000, 0x107c091b );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vs2j.05", 0x100000, 0x80000, 0x61979638 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vs2j.06", 0x180000, 0x80000, 0xf37c5bc2 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vs2j.07", 0x200000, 0x80000, 0x8f885809 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vs2j.08", 0x280000, 0x80000, 0x2018c120 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vs2j.09", 0x300000, 0x80000, 0xfac3c217 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "vs2j.10", 0x380000, 0x80000, 0xeb490213 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x2000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "vs2.18",   0x0000000, 0x400000, 0x778dc4f6 );
/*TODO*///	ROM_LOAD( "vs2.17",   0x0400000, 0x400000, 0x39db59ad );
/*TODO*///	ROM_LOAD( "vs2.14",   0x0800000, 0x400000, 0xcd09bd63 );
/*TODO*///	ROM_LOAD( "vs2.13",   0x0c00000, 0x400000, 0x5c852f52 );
/*TODO*///	ROM_LOAD( "vs2.20",   0x1000000, 0x400000, 0x605d9d1d );
/*TODO*///	ROM_LOAD( "vs2.19",   0x1400000, 0x400000, 0x00c763a7 );
/*TODO*///	ROM_LOAD( "vs2.16",   0x1800000, 0x400000, 0xe0182c15 );
/*TODO*///	ROM_LOAD( "vs2.15",   0x1c00000, 0x400000, 0xa20f58af );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "vs2.01",   0x00000, 0x08000, 0x35190139 );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "vs2.02",   0x28000, 0x20000, 0xc32dba09 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x800000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "vs2.11",   0x000000, 0x400000, 0xd67e47b7 );
/*TODO*///	ROM_LOAD( "vs2.12",   0x400000, 0x400000, 0x6d020a14 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_xmcota = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xmnu.03e", 0x000000, 0x80000, 0x0bafeb0e );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xmnu.04e", 0x080000, 0x80000, 0xc29bdae3 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xmnu.05a", 0x100000, 0x80000, 0xac0d7759 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xmnu.06a", 0x180000, 0x80000, 0x6a3f0924 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xmnu.07a", 0x200000, 0x80000, 0x2c142a44 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xmnu.08a", 0x280000, 0x80000, 0xf712d44f );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xmnu.09a", 0x300000, 0x80000, 0xc24db29a );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xmnu.10a", 0x380000, 0x80000, 0x53c0eab9 );
/*TODO*///
/*TODO*///	ROM_REGION( 0x2000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "xmn.18",   0x0000000, 0x400000, 0x015a7c4c );
/*TODO*///	ROM_LOAD( "xmn.17",   0x0400000, 0x400000, 0x513eea17 );
/*TODO*///	ROM_LOAD( "xmn.14",   0x0800000, 0x400000, 0x778237b7 );
/*TODO*///	ROM_LOAD( "xmn.13",   0x0c00000, 0x400000, 0xbf4df073 );
/*TODO*///	ROM_LOAD( "xmn.20",   0x1000000, 0x400000, 0x9dde2758 );
/*TODO*///	ROM_LOAD( "xmn.19",   0x1400000, 0x400000, 0xd23897fc );
/*TODO*///	ROM_LOAD( "xmn.16",   0x1800000, 0x400000, 0x67b36948 );
/*TODO*///	ROM_LOAD( "xmn.15",   0x1c00000, 0x400000, 0x4d7e4cef );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "xmn.01",   0x00000, 0x08000, 0x40f479ea );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "xmn.02",   0x28000, 0x20000, 0x39d9b5ad );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "xmn.11",   0x000000, 0x200000, 0xc848a6bc );
/*TODO*///	ROM_LOAD( "xmn.12",   0x200000, 0x200000, 0x729c188f );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_xmcotaj = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xmnj.03b", 0x000000, 0x80000, 0xc8175fb3 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xmnj.04b", 0x080000, 0x80000, 0x54b3fba3 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xmnj.05",  0x100000, 0x80000, 0xc3ed62a2 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xmnj.06",  0x180000, 0x80000, 0xf03c52e1 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xmnj.07",  0x200000, 0x80000, 0x325626b1 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xmnj.08",  0x280000, 0x80000, 0x7194ea10 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xmnj.09",  0x300000, 0x80000, 0xae946df3 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xmnj.10",  0x380000, 0x80000, 0x32a6be1d );
/*TODO*///
/*TODO*///	ROM_REGION( 0x2000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "xmn.18",   0x0000000, 0x400000, 0x015a7c4c );
/*TODO*///	ROM_LOAD( "xmn.17",   0x0400000, 0x400000, 0x513eea17 );
/*TODO*///	ROM_LOAD( "xmn.14",   0x0800000, 0x400000, 0x778237b7 );
/*TODO*///	ROM_LOAD( "xmn.13",   0x0c00000, 0x400000, 0xbf4df073 );
/*TODO*///	ROM_LOAD( "xmn.20",   0x1000000, 0x400000, 0x9dde2758 );
/*TODO*///	ROM_LOAD( "xmn.19",   0x1400000, 0x400000, 0xd23897fc );
/*TODO*///	ROM_LOAD( "xmn.16",   0x1800000, 0x400000, 0x67b36948 );
/*TODO*///	ROM_LOAD( "xmn.15",   0x1c00000, 0x400000, 0x4d7e4cef );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "xmn.01",   0x00000, 0x08000, 0x40f479ea );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "xmn.02",   0x28000, 0x20000, 0x39d9b5ad );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "xmn.11",   0x000000, 0x200000, 0xc848a6bc );
/*TODO*///	ROM_LOAD( "xmn.12",   0x200000, 0x200000, 0x729c188f );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///static RomLoadHandlerPtr rom_xmvsf = new RomLoadHandlerPtr(){ public void handler(){ 
/*TODO*///	ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xvsu.03i", 0x000000, 0x80000, 0x5481155a );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xvsu.04i", 0x080000, 0x80000, 0x1e236388 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xvsu.05a", 0x100000, 0x80000, 0x7db6025d );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xvsu.06a", 0x180000, 0x80000, 0xe8e2c75c );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xvs.07",  0x200000, 0x80000, 0x08f0abed );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xvs.08",  0x280000, 0x80000, 0x81929675 );
/*TODO*///	ROM_LOAD_WIDE_SWAP( "xvs.09",  0x300000, 0x80000, 0x9641f36b );
/*TODO*///
/*TODO*///	ROM_REGION( 0x2000000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///	ROM_LOAD( "xvs.18",   0x0000000, 0x400000, 0xb0def86a );
/*TODO*///	ROM_LOAD( "xvs.17",   0x0400000, 0x400000, 0x92db3474 );
/*TODO*///	ROM_LOAD( "xvs.14",   0x0800000, 0x400000, 0xbcac2e41 );
/*TODO*///	ROM_LOAD( "xvs.13",   0x0c00000, 0x400000, 0xf6684efd );
/*TODO*///	ROM_LOAD( "xvs.20",   0x1000000, 0x400000, 0x4b40ff9f );
/*TODO*///	ROM_LOAD( "xvs.19",   0x1400000, 0x400000, 0x3733473c );
/*TODO*///	ROM_LOAD( "xvs.16",   0x1800000, 0x400000, 0xea04a272 );
/*TODO*///	ROM_LOAD( "xvs.15",   0x1c00000, 0x400000, 0x29109221 );
/*TODO*///
/*TODO*///	ROM_REGION( QSOUND_SIZE, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///	ROM_LOAD( "xvs.01",   0x00000, 0x08000, 0x3999e93a );
/*TODO*///	ROM_CONTINUE(         0x10000, 0x18000 );
/*TODO*///	ROM_LOAD( "xvs.02",   0x28000, 0x20000, 0x19272e4c );
/*TODO*///
/*TODO*///	ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///	ROM_LOAD( "xvs.11",   0x000000, 0x200000, 0x9cadcdbc );
/*TODO*///	ROM_LOAD( "xvs.12",   0x200000, 0x200000, 0x7b11e460 );
/*TODO*///ROM_END(); }}; 
/*TODO*///
/*TODO*///
/*TODO*///public static GameDriver driver_19xx	   = new GameDriver("1995"	,"19xx"	,"cps2.java"	,rom_19xx,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "19XX: The Battle Against Destiny (USA 951207)" )
/*TODO*///public static GameDriver driver_armwar	   = new GameDriver("1994"	,"armwar"	,"cps2.java"	,rom_armwar,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Armoured Warriors (USA 941024)" )
/*TODO*///public static GameDriver driver_armwara	   = new GameDriver("1994"	,"armwara"	,"cps2.java"	,rom_armwara,driver_armwar	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Armoured Warriors (Asia 940920)" )
    public static GameDriver driver_avsp	   = new GameDriver("1994"	,"avspu"	,"cps2.java"	,rom_avsp,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Aliens Vs. Predator (USA 940520)" );
/*TODO*///public static GameDriver driver_avspa	   = new GameDriver("1994"	,"avspa"	,"cps2.java"	,rom_avspa,driver_avsp	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Aliens Vs. Predator (Asia 940520)" )
/*TODO*///public static GameDriver driver_batcirj	   = new GameDriver("1997"	,"batcirj"	,"cps2.java"	,rom_batcirj,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Battle Circuit (Japan 970319)" )
/*TODO*///public static GameDriver driver_batcira	   = new GameDriver("1997"	,"batcira"	,"cps2.java"	,rom_batcira,driver_batcirj	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Battle Circuit (Asia 970319)" )
/*TODO*///public static GameDriver driver_cybotsj	   = new GameDriver("1995"	,"cybotsj"	,"cps2.java"	,rom_cybotsj,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Cyberbots: Full Metal Madness (Japan 950420)" )
/*TODO*///public static GameDriver driver_ddtod	   = new GameDriver("1994"	,"ddtod"	,"cps2.java"	,rom_ddtod,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Dungeons & Dragons: Tower of Doom (USA 940113)" )
/*TODO*///public static GameDriver driver_ddtoda	   = new GameDriver("1994"	,"ddtoda"	,"cps2.java"	,rom_ddtoda,driver_ddtod	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Dungeons & Dragons: Tower of Doom (Asia 940113)" )
/*TODO*///public static GameDriver driver_ddtodr1	   = new GameDriver("1994"	,"ddtodr1"	,"cps2.java"	,rom_ddtodr1,driver_ddtod	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Dungeons & Dragons: Tower of Doom (USA 940125)" )
/*TODO*///public static GameDriver driver_ddsom	   = new GameDriver("1996"	,"ddsom"	,"cps2.java"	,rom_ddsom,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Dungeons & Dragons 2: Shadow over Mystara (USA 960209)" )
/*TODO*///public static GameDriver driver_dstlk	   = new GameDriver("1994"	,"dstlk"	,"cps2.java"	,rom_dstlk,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "DarkStalkers: The Night Warriors (USA 940818)" )
/*TODO*///public static GameDriver driver_vampj	   = new GameDriver("1994"	,"vampj"	,"cps2.java"	,rom_vampj,driver_dstlk	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Vampire: The Night Warriors (Japan 940705)" )
/*TODO*///public static GameDriver driver_vampa	   = new GameDriver("1994"	,"vampa"	,"cps2.java"	,rom_vampa,driver_dstlk	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Vampire: The Night Warriors (Asia 940705)" )
/*TODO*///public static GameDriver driver_ecofe	   = new GameDriver("1993"	,"ecofe"	,"cps2.java"	,rom_ecofe,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Eco Fighters (Etc 931203)" )
/*TODO*///public static GameDriver driver_uecology	   = new GameDriver("1993"	,"uecology"	,"cps2.java"	,rom_uecology,driver_ecofe	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Ultimate Ecology (Japan 931203)" )
/*TODO*///public static GameDriver driver_msh	   = new GameDriver("1995"	,"msh"	,"cps2.java"	,rom_msh,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Marvel Super Heroes (USA 951024)" )
/*TODO*///public static GameDriver driver_mshvsf	   = new GameDriver("1997"	,"mshvsf"	,"cps2.java"	,rom_mshvsf,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Marvel Super Heroes Vs. Street Fighter (USA 970625)" )
/*TODO*///public static GameDriver driver_mshvsfj	   = new GameDriver("1997"	,"mshvsfj"	,"cps2.java"	,rom_mshvsfj,driver_mshvsf	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Marvel Super Heroes Vs. Street Fighter (Japan 970707)" )
/*TODO*///public static GameDriver driver_mvsc	   = new GameDriver("1998"	,"mvsc"	,"cps2.java"	,rom_mvsc,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Marvel Super Heroes vs. Capcom: Clash of Super Heroes (USA 980123)" )
/*TODO*///public static GameDriver driver_mvscj	   = new GameDriver("1998"	,"mvscj"	,"cps2.java"	,rom_mvscj,driver_mvsc	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Marvel Super Heroes vs. Capcom: Clash of Super Heroes (Japan 980123)" )
/*TODO*///public static GameDriver driver_nwarr	   = new GameDriver("1995"	,"nwarr"	,"cps2.java"	,rom_nwarr,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Night Warriors: DarkStalkers Revenge (USA 950406)" )
/*TODO*///public static GameDriver driver_vhuntj	   = new GameDriver("1995"	,"vhuntj"	,"cps2.java"	,rom_vhuntj,driver_nwarr	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Vampire Hunter: DarkStalkers Revenge (Japan 950302)" )
/*TODO*///public static GameDriver driver_rckman2j	   = new GameDriver("1996"	,"rckman2j"	,"cps2.java"	,rom_rckman2j,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Rockman 2: The Power Fighters (Japan 960708)" )
/*TODO*///public static GameDriver driver_qndream	   = new GameDriver("1996"	,"qndream"	,"cps2.java"	,rom_qndream,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Quiz Nanairo Dreams: Miracle of the Rainbow Colored Town (Japan 960626)" )
/*TODO*///public static GameDriver driver_sfa	   = new GameDriver("1995"	,"sfa"	,"cps2.java"	,rom_sfa,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Street Fighter Alpha: The Warriors Dream (USA 950627)" )
/*TODO*///public static GameDriver driver_sfar1	   = new GameDriver("1995"	,"sfar1"	,"cps2.java"	,rom_sfar1,driver_sfa	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Street Fighter Alpha: The Warriors Dream (USA 950727)" )
/*TODO*///public static GameDriver driver_sfzj	   = new GameDriver("1995"	,"sfzj"	,"cps2.java"	,rom_sfzj,driver_sfa	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Street Fighter Zero (Japan 950627)" )
/*TODO*///public static GameDriver driver_sfzjr1	   = new GameDriver("1995"	,"sfzjr1"	,"cps2.java"	,rom_sfzjr1,driver_sfa	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Street Fighter Zero (Japan 950727)" )
/*TODO*///public static GameDriver driver_sfz2j	   = new GameDriver("1996"	,"sfz2j"	,"cps2.java"	,rom_sfz2j,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Street Fighter Zero 2 (Japan 960227)" )
/*TODO*///public static GameDriver driver_sfz2a	   = new GameDriver("1996"	,"sfz2a"	,"cps2.java"	,rom_sfz2a,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Street Fighter Zero 2 Alpha (Japan 960805)" )
/*TODO*///public static GameDriver driver_sfa3	   = new GameDriver("1998"	,"sfa3"	,"cps2.java"	,rom_sfa3,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Street Fighter Alpha 3 (USA 980629)" )
/*TODO*///public static GameDriver driver_sfz3	   = new GameDriver("1998"	,"sfz3"	,"cps2.java"	,rom_sfz3,driver_sfa3	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Street Fighter Zero 3 (USA 980727)" )
/*TODO*///public static GameDriver driver_sgemf	   = new GameDriver("1997"	,"sgemf"	,"cps2.java"	,rom_sgemf,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Super Gem Fighter Mini Mix (USA 970904)" )
/*TODO*///public static GameDriver driver_pfghtj	   = new GameDriver("1997"	,"pfghtj"	,"cps2.java"	,rom_pfghtj,driver_sgemf	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Pocket Fighter (Japan 970904)" )
/*TODO*///public static GameDriver driver_slam2e	   = new GameDriver("1994"	,"slam2e"	,"cps2.java"	,rom_slam2e,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Saturday Night Slammasters II: Ring of Destruction (Euro 940902)" )
/*TODO*///public static GameDriver driver_smbomber	   = new GameDriver("1994"	,"smbomber"	,"cps2.java"	,rom_smbomber,driver_slam2e	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Super Muscle Bomber: The International Blowout (Japan 940808)" )
/*TODO*///public static GameDriver driver_spf2t	   = new GameDriver("1996"	,"spf2t"	,"cps2.java"	,rom_spf2t,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Super Puzzle Fighter 2 Turbo (USA 960620)" )
/*TODO*///public static GameDriver driver_spf2xj	   = new GameDriver("1996"	,"spf2xj"	,"cps2.java"	,rom_spf2xj,driver_spf2t	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Super Puzzle Fighter 2 X (Japan 960531)" )
/*TODO*///public static GameDriver driver_ssf2	   = new GameDriver("1993"	,"ssf2"	,"cps2.java"	,rom_ssf2,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Super Street Fighter 2: The New Challengers (USA 930911)" )
/*TODO*///public static GameDriver driver_ssf2a	   = new GameDriver("1993"	,"ssf2a"	,"cps2.java"	,rom_ssf2a,driver_ssf2	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Super Street Fighter 2: The New Challengers (Asia 930911)" )
/*TODO*///public static GameDriver driver_ssf2j	   = new GameDriver("1993"	,"ssf2j"	,"cps2.java"	,rom_ssf2j,driver_ssf2	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Super Street Fighter 2: The New Challengers (Japan 930910)" )
/*TODO*///public static GameDriver driver_ssf2t	   = new GameDriver("1994"	,"ssf2t"	,"cps2.java"	,rom_ssf2t,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Super Street Fighter 2 Turbo (USA 940223)" )
/*TODO*///public static GameDriver driver_ssf2xj	   = new GameDriver("1994"	,"ssf2xj"	,"cps2.java"	,rom_ssf2xj,driver_ssf2t	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Super Street Fighter 2 X: Grand Master Challenge (Japan 940223)" )
/*TODO*///public static GameDriver driver_vhunt2	   = new GameDriver("1997"	,"vhunt2"	,"cps2.java"	,rom_vhunt2,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Vampire Hunter 2: Darkstalkers Revenge (Japan 970913)" )
/*TODO*///public static GameDriver driver_vsav	   = new GameDriver("1997"	,"vsav"	,"cps2.java"	,rom_vsav,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Vampire Savior: The Lord of Vampire (USA 970519)" )
/*TODO*///public static GameDriver driver_vsavj	   = new GameDriver("1997"	,"vsavj"	,"cps2.java"	,rom_vsavj,driver_vsav	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Vampire Savior: The Lord of Vampire (Japan 970519)" )
/*TODO*///public static GameDriver driver_vsav2	   = new GameDriver("1997"	,"vsav2"	,"cps2.java"	,rom_vsav2,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "Vampire Savior 2: The Lord of Vampire (Japan 970913)" )
/*TODO*///public static GameDriver driver_xmcota	   = new GameDriver("1995"	,"xmcota"	,"cps2.java"	,rom_xmcota,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "X-Men: Children of the Atom (USA 950105)" )
/*TODO*///public static GameDriver driver_xmcotaj	   = new GameDriver("1994"	,"xmcotaj"	,"cps2.java"	,rom_xmcotaj,driver_xmcota	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "X-Men: Children of the Atom (Japan 941219)" )
/*TODO*///public static GameDriver driver_xmvsf	   = new GameDriver("1996"	,"xmvsf"	,"cps2.java"	,rom_xmvsf,null	,machine_driver_cps2	,input_ports_cps2	,init_cps2	,ROT0	,	"Capcom", "X-Men Vs. Street Fighter (USA 961004)" )
    
}
